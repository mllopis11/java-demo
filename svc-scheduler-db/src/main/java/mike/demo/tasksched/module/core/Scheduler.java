package mike.demo.tasksched.module.core;

import java.time.Duration;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mike.bootstrap.utilities.helpers.Dates;
import mike.bootstrap.utilities.helpers.Utils;
import mike.demo.tasksched.module.core.time.TimeProvider;
import mike.demo.tasksched.module.core.schedule.Schedule;
import mike.demo.tasksched.module.core.time.SystemDateTimeProvider;

public class Scheduler {

	private static final Logger log = LoggerFactory.getLogger(Scheduler.class);
	
	private static final AtomicInteger threadCounter = new AtomicInteger(0);
	
	private final String name;
	private final ThreadPoolExecutor threadPoolExecutor;
	private final Duration shutdownMaxWaitInSeconds = Duration.ofSeconds(20); 
	private final TaskManager taskManager;
	
	private volatile SchedulerState schedulerState = SchedulerState.READY;
	private AtomicBoolean launcherThreadStarted = new AtomicBoolean(false);
	
	private Scheduler(Builder builder) {
		
		this.name = Utils.trim(builder.name, "JobScheduler");
		int minThreads = builder.minThreads > 0 ? builder.minThreads : 1;
		int maxThreads = builder.maxThreads >= minThreads ? builder.maxThreads : minThreads + 4;
		Duration threadsKeepAliveTime = builder.threadsKeepAliveTime != null ? builder.threadsKeepAliveTime : Duration.ofSeconds(30); 
		TimeProvider timeProvider = builder.timeProvider != null ? builder.timeProvider : new SystemDateTimeProvider();
		TaskRepository taskRepository = builder.taskRepository != null ? builder.taskRepository : new TaskRepositoryDefault();
		
		this.taskManager = new TaskManager(taskRepository, timeProvider);
		
		this.threadPoolExecutor = new ThreadPoolExecutor(
				minThreads, maxThreads, 
				threadsKeepAliveTime.toSeconds(), TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new DefaultThreadFactory());
		
		log.info("{}", this.stats());
	}
	
	public SchedulerStats stats() {
		return new SchedulerStats(this.name, this.schedulerState, this.threadPoolExecutor);
	}
	
	public SchedulerState state() {
		return this.schedulerState;
	}
	
	public void start() {
		// Run job launcher background thread
		Thread launcherThread = new Thread(this::launcher, "JobLauncher");
		launcherThread.setDaemon(false);
		launcherThread.setPriority(Thread.MAX_PRIORITY);
		launcherThread.start();	
		
		while ( this.schedulerState.isReady() ) {}
		
		log.info("[{}] {} ({} started)", this.name, this.schedulerState, launcherThread.getName());
	}
	
	public void suspend() {
		this.schedulerState = SchedulerState.SUSPENDED;
	}
	
	/**
	 * Cancel queued jobs and Wait until the current running jobs are executed.
	 * @throws InterruptedException 
	 */
	public void shutdown()  {
		
		this.schedulerState = SchedulerState.SHUTDOWN;
		this.threadPoolExecutor.shutdown();
		
		log.info("{}", this.stats());

		while (this.launcherThreadStarted.get() ) {}
		
		try {
			this.threadPoolExecutor.awaitTermination(shutdownMaxWaitInSeconds.toSeconds(), TimeUnit.SECONDS);
			this.schedulerState = SchedulerState.STOPPED;
		} catch (InterruptedException ie) {
			if ( this.threadPoolExecutor.getActiveCount() > 0 ) {
				log.warn("[{}] Shutdown timeout (some threads can be still running (cause: {})", this.name, ie.getMessage());
			}
		}
		
		log.info("{}", this.stats());
	}
	
	public Task schedule(TaskWorker worker, Schedule when) {
		return this.taskManager.schedule(worker, when);
	}
	
	/**
	 * The daemon that will be in charge of placing the jobs in the thread pool
	 * when they are ready to be executed.
	 */
	private void launcher() { 
		
		this.schedulerState = SchedulerState.LISTEN;
		
		this.launcherThreadStarted.set(true);
		
		while ( this.schedulerState.isNotStopping() ) {
			
			if ( this.schedulerState.isListening() ) {
				
				log.debug("[{}] looking for job to execute ...", this.name);
				
				this.taskManager.findTasksToRun().forEach( task -> {
					log.info("[{}] submit {} (scheduledAt: {})", this.name, task.getName(), Dates.format(task.getNextExecutionDateTime()));
					TaskRunner taskRunner = new TaskRunner(task, this.taskManager);
					this.threadPoolExecutor.submit(taskRunner);
				});
				
			} else {
				log.warn("[{}] Launcher {}", this.name, this.schedulerState);
			}
			
			if ( this.schedulerState.isNotStopping() ) {
				Utils.pause(5);
			}
		}
		
		this.launcherThreadStarted.set(false);
		
		log.info("[{}] Launcher stopped", this.name);
	}
	
	/**
	 * Thread factory
	 * 
	 * @author Mike (2021-03)
	 */
	private static class DefaultThreadFactory implements ThreadFactory {
		
		@Override
		public Thread newThread(Runnable r) {
			
			Thread thread = new Thread(r, "JobWorker#" + threadCounter.getAndIncrement());
			
			if (thread.isDaemon()) {
				thread.setDaemon(false);
			}
			
			if (thread.getPriority() != Thread.NORM_PRIORITY) {
				thread.setPriority(Thread.NORM_PRIORITY);
			}
			
			return thread;
		}
	}

	/**
	 * Scheduler builder
	 * 
	 * @author Mike (2021-03)
	 */
	public static class Builder {
		
		private String name;
		private int minThreads;
		private int maxThreads;
		private Duration threadsKeepAliveTime;
		private TimeProvider timeProvider;
		private TaskRepository taskRepository;
		
		public Builder withName(String name) {
			this.name = name;
			return this;
		}
		
		public Builder withMinThreads(int minThreads) {
			this.minThreads = minThreads;
			return this;
		}
		
		public Builder withMaxThreads(int maxThreads) {
			this.maxThreads = maxThreads;
			return this;
		}
		
		public Builder withThreadsKeepAliveTime(Duration duration) {
			this.threadsKeepAliveTime = duration;
			return this;
		}
		
		public Builder withTimeProvider(TimeProvider timeProvider) {
			this.timeProvider = timeProvider;
			return this;
		}
		
		public Builder withTaskRepository(TaskRepository taskRepository) {
			this.taskRepository = taskRepository;
			return this;
		}
		
		public Scheduler build() {
			return new Scheduler(this);
		}
	}
}
