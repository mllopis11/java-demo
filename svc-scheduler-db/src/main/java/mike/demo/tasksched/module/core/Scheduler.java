package mike.demo.tasksched.module.core;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mike.bootstrap.utilities.helpers.Dates;
import mike.bootstrap.utilities.helpers.Utils;
import mike.demo.tasksched.module.core.time.TimeProvider;
import mike.demo.tasksched.module.core.time.TimeProviderFactory;
import mike.demo.tasksched.module.core.repository.TaskRepository;
import mike.demo.tasksched.module.core.repository.TaskRepositoryFactory;
import mike.demo.tasksched.module.core.schedule.Schedule;

public class Scheduler {

	private static final Logger log = LoggerFactory.getLogger(Scheduler.class);
	
	private static final AtomicInteger threadCounter = new AtomicInteger(0);
	
	private final String name;
	private final ThreadPoolExecutor threadPoolExecutor;
	private final Duration shutdownMaxWaitInSeconds = Duration.ofSeconds(20); 
	private final TaskManager taskManager;
	private final SchedulerState schedulerState;
	
	// Launcher
	private Thread launcherThread;
	private final AtomicBoolean launcherThreadAlive = new AtomicBoolean(false);
	private final AtomicBoolean launcherThreadSuspended = new AtomicBoolean(false);
	private final int launcherScanIntervalInSeconds; 
	
	private Scheduler(Builder builder) {
		
		this.name = builder.name;
		this.launcherScanIntervalInSeconds = builder.launcherScanIntervalInSeconds; 

		this.taskManager = new TaskManager(builder.taskRepository, builder.timeProvider);
		
		this.threadPoolExecutor = new ThreadPoolExecutor(
				builder.minThreads, builder.maxThreads, 
				10, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new DefaultThreadFactory());
		
		this.schedulerState = new SchedulerState(name, threadPoolExecutor);
		
		log.info("{}", this.state());
	}
	
	public SchedulerState state() {
		this.schedulerState.setStatistics(threadPoolExecutor);
		return this.schedulerState;
	}
	
	public SchedulerStatus status() {
		return this.schedulerState.getStatus();
	}
	
	public Optional<Task> task(String name) {
		return this.taskManager.find(name);
	}
	
	public Stream<Task> tasks() {
		return this.taskManager.findAll();
	}
	
	public synchronized boolean start() {
		
		if ( launcherThread != null ) {
			log.warn("[{}] {} (already started)", this.name, this.status());
			return false;
		}
		
		// Run job launcher background thread
		
		launcherThread = new Thread(this::launcher, "TaskLauncher");
		launcherThread.setDaemon(false);
		launcherThread.setPriority(Thread.NORM_PRIORITY);
		launcherThread.start();	
		
		while ( this.status().isReady() ) {}
		
		log.info("{}", this.state());
		
		return this.launcherThreadAlive.get();
	}
	
	public boolean suspend() {
		
		if ( launcherThread == null ) {
			log.warn("[{}] {} (NotStarted) cannot invoke suspend action on unstarted scheduler !!!", this.name, this.status());
			return false;
		}
		
		this.schedulerState.setStatus(SchedulerStatus.SUSPENDED);
		
		while ( ! this.launcherThreadSuspended.get() ) {
			// Wait
		}
		
		log.warn("{}", this.state());
		
		return this.launcherThreadSuspended.get();
	}
	
	public boolean release() {
		
		if ( launcherThread == null ) {
			log.warn("[{}] {} (NotStarted) cannot invoke release action on unstarted scheduler !!!", this.name, this.status());
			return false;
		}
		
		this.schedulerState.setStatus(SchedulerStatus.LISTEN);
		
		while ( this.launcherThreadSuspended.get() ) { 
			// Wait
		}
		
		log.info("{}", this.state());
		
		return ! this.launcherThreadSuspended.get();
	}
	
	/**
	 * Cancel queued jobs and Wait until the current running jobs are executed.
	 * @throws InterruptedException 
	 */
	public void shutdown()  {
		
		this.schedulerState.setStatus(SchedulerStatus.SHUTDOWN);
		this.threadPoolExecutor.shutdown();

		log.info("{}", this.state());
		
		while ( this.launcherThreadAlive.get() ) {}
		
		try {
			this.threadPoolExecutor.awaitTermination(shutdownMaxWaitInSeconds.toSeconds(), TimeUnit.SECONDS);
			this.schedulerState.setStatus(SchedulerStatus.STOPPED);
		} catch (InterruptedException ie) {
			if ( this.threadPoolExecutor.getActiveCount() > 0 ) {
				log.warn("[{}] Shutdown timeout (some threads can be still running (cause: {})", this.name, ie.getMessage());
			}
		}
		
		log.info("{}", this.state());
	}
	
	public Task schedule(TaskWorker worker, Schedule when) {
		Task task = this.taskManager.schedule(worker, when);
		log.info("[{}] New {}", this.name, task);
		return task;
	}
	
	/**
	 * The daemon that will be in charge of placing the jobs in the thread pool
	 * when they are ready to be executed.
	 */
	private void launcher() { 
		
		this.schedulerState.setStatus(SchedulerStatus.LISTEN);
		this.launcherThreadAlive.set(true);
		
		log.info("[{}] Listener starting ...", this.name);
		
		while ( this.status().isNotStopping() ) {
			
			if ( this.status().isListening() ) {
				
				this.schedulerState.setLastScan();
				this.launcherThreadSuspended.set(false);
				
				log.debug("[{}] looking for job to execute (currentDateTime: {})", this.name, Dates.format(taskManager.currentDateTime()));
				
				this.taskManager.findTasksToRun().forEach( task -> {
					log.info("[{}] submit {} (scheduledAt: {})", this.name, task.getName(), Dates.format(task.getNextExecutionDateTime()));
					TaskRunner taskRunner = new TaskRunner(task, this.taskManager);
					this.threadPoolExecutor.submit(taskRunner);
				});
				
			} else if ( this.status().isSuspended() && ! launcherThreadSuspended.get() ) {
				this.launcherThreadSuspended.set(true);
			} 
			
			if ( this.status().isNotStopping() ) {
				Utils.pause( ! this.launcherThreadSuspended.get() ? launcherScanIntervalInSeconds : 1);
			}
		}
		
		log.info("[{}] Listener stopped", this.name);
		
		this.launcherThreadAlive.set(false);
	}
	
	/**
	 * Thread factory
	 * 
	 * @author Mike (2021-03)
	 */
	private static class DefaultThreadFactory implements ThreadFactory {
		
		@Override
		public Thread newThread(Runnable r) {
			
			Thread thread = new Thread(r, "TaskWorker#" + threadCounter.getAndIncrement());
			
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
		
		private final String name;
		private int minThreads;
		private int maxThreads;
		private int launcherScanIntervalInSeconds; 
		private TimeProvider timeProvider;
		private TaskRepository taskRepository;
		
		public Builder() {
			this(null);
		}

		public Builder(String name) {
			this.name = Utils.trim(name, "TaskScheduler");
		}
		
		public Builder withMinThreads(int minThreads) {
			this.minThreads = minThreads;
			return this;
		}
		
		public Builder withMaxThreads(int maxThreads) {
			this.maxThreads = maxThreads;
			return this;
		}
		
		public Builder withLauncherScanIntervalInSeconds(int seconds) {
			this.launcherScanIntervalInSeconds = seconds;
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
			this.minThreads = this.minThreads > 10 ? this.minThreads : 5;
			this.maxThreads = this.maxThreads >= minThreads ? this.maxThreads : 20;
			this.timeProvider = this.timeProvider != null ? this.timeProvider : TimeProviderFactory.newSystemTimeProvider();
			this.taskRepository = this.taskRepository != null ? this.taskRepository : TaskRepositoryFactory.newDefaultTaskRepository();
			this.launcherScanIntervalInSeconds = this.launcherScanIntervalInSeconds >= 5 ? this.launcherScanIntervalInSeconds : 5;
			
			return new Scheduler(this);
		}
	}
}
