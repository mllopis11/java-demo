package mike.demo.tasksched.library.ruby;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mike.bootstrap.utilities.helpers.Dates;
import mike.bootstrap.utilities.helpers.Timer;
import mike.demo.tasksched.library.ruby.repository.TaskRepository;
import mike.demo.tasksched.library.ruby.time.TimeProvider;

class Scheduler implements RubyScheduler {

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
	
	Scheduler(
			String name, int minThreads, int maxThreads, 
			int launcherScanIntervalInSeconds, TimeProvider timeProvider,
			TaskRepository taskRepository) {
		
		this.name = name;
		this.launcherScanIntervalInSeconds = launcherScanIntervalInSeconds; 

		this.taskManager = new TaskManager(taskRepository, timeProvider);
		
		this.threadPoolExecutor = new ThreadPoolExecutor(
				minThreads, maxThreads, 30, TimeUnit.SECONDS, 
				new LinkedBlockingQueue<>(), new DefaultThreadFactory());
		
		this.schedulerState = new SchedulerState(name, threadPoolExecutor);
		
		log.info("{}", this.state());
	}
	
	@Override
	public String name() {
		return this.name;
	}
	
	@Override
	public SchedulerState state() {
		this.schedulerState.setStatistics(threadPoolExecutor);
		return this.schedulerState;
	}
	
	@Override
	public TaskManagerService taskService() {
		return this.taskManager;
	}
	
	@Override
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
	
	@Override
	public synchronized boolean suspend() {
		
		if ( launcherThread == null ) {
			log.warn("[{}] {} (NotStarted) cannot invoke suspend action on unstarted scheduler !!!", this.name, this.status());
			return false;
		}
		
		if ( this.status().isSuspended() ) {
			return true;
		}
		
		this.schedulerState.setStatus(SchedulerStatus.SUSPENDED);
		
		while ( ! this.launcherThreadSuspended.get() ) {
			Timer.pause(1);
		}
		
		log.warn("{}", this.state());
		
		return this.launcherThreadSuspended.get();
	}
	
	@Override
	public synchronized boolean release() {
		
		if ( launcherThread == null ) {
			log.warn("[{}] {} (NotStarted) cannot invoke release action on unstarted scheduler !!!", this.name, this.status());
			return false;
		}
		
		if ( ! this.status().isSuspended() ) {
			return true;
		}
		
		log.info("[{}] {} looking for misfired tasks (currentDateTime: {})", 
				this.name, this.status(), Dates.format(taskManager.currentDateTime()));
		
		this.taskManager.findTaskToRun().forEach( task -> {
			ZonedDateTime wasScheduledAt = task.getNextExecutionDateTime();
			
			this.taskManager.rescheduleTask(task);
			
			log.info("[{}] misfired tasks {} recheduledAt {} (wasScheduledAt: {})", this.name, task.getName(), 
					Dates.format(task.getNextExecutionDateTime()), Dates.format(wasScheduledAt));
		});
		
		this.schedulerState.setStatus(SchedulerStatus.LISTEN);
		
		while ( this.launcherThreadSuspended.get() ) { 
			Timer.pause(1);
		}
		
		log.info("{}", this.state());
		
		return ! this.launcherThreadSuspended.get();
	}
	
	/**
	 * Cancel queued jobs and Wait until the current running jobs are executed.
	 * @throws InterruptedException 
	 */
	@Override
	public synchronized void shutdown()  {
		
		if ( this.schedulerState.getStatus().isShuttingDown() ) {
			return;
		}
		
		this.schedulerState.setStatus(SchedulerStatus.SHUTDOWN);
		this.threadPoolExecutor.shutdown();

		log.info("{}", this.state());
		
		while ( this.launcherThreadAlive.get() ) {
			Timer.pause(1);
		}
		
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
	
	/**
	 * The daemon that will be in charge of placing the jobs in the thread pool
	 * when they are ready to be executed.
	 */
	private void launcher() { 

		this.schedulerState.setStatus(SchedulerStatus.LISTEN);
		this.launcherThreadAlive.set(true);
		
		while ( this.status().isNotStopping() ) {
			
			if ( this.status().isListening() ) {
				
				this.schedulerState.setLastScan();
				this.launcherThreadSuspended.set(false);
				
				log.debug("[{}] {} Looking for task to execute (currentDateTime: {})", 
						this.name, this.status(), Dates.format(taskManager.currentDateTime()));
				
				this.taskManager.findTaskToRun().forEach( task -> {
					log.info("[{}] submit {} (scheduledAt: {})", this.name, task.getName(), Dates.format(task.getNextExecutionDateTime()));
					TaskRunner taskRunner = new TaskRunner(task, this.taskManager);
					this.threadPoolExecutor.execute(taskRunner);
				});
				
			} else if ( this.status().isSuspended() && ! launcherThreadSuspended.get() ) {
				this.launcherThreadSuspended.set(true);
			} 
			
			if ( this.status().isNotStopping() ) {
				Timer.pause( ! this.launcherThreadSuspended.get() ? launcherScanIntervalInSeconds : 1);
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
}
