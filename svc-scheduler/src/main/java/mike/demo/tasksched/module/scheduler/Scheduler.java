package mike.demo.tasksched.module.scheduler;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mike.bootstrap.utilities.helpers.Dates;
import mike.bootstrap.utilities.helpers.Timer;
import mike.bootstrap.utilities.helpers.Utils;
import mike.demo.tasksched.module.scheduler.schedule.Schedule;
import mike.demo.tasksched.module.scheduler.schedule.ScheduleFactory;

/**
 * A {@code Scheduler} instance reference a group of jobs
 * and is responsible to schedule these jobs at the expected time.<br/>
 * <br/>
 * A job is executed only once at a time.
 * The scheduler will never execute the same job twice at a time.
 */
public final class Scheduler {

	private static final Logger log = LoggerFactory.getLogger(Scheduler.class);

	private static final AtomicInteger threadCounter = new AtomicInteger(0);

	private final String name;
	private final TimeProvider timeProvider;
	private final ThreadPoolExecutor threadPoolExecutor;
	private final AtomicBoolean launcherNotifier = new AtomicBoolean(true);

	// jobs
	private final Map<String, Job> indexedJobsByName = new ConcurrentHashMap<>();
	private final List<Job> nextExecutionsOrder = new ArrayList<>();
	private final Map<String, CompletableFuture<Job>> cancelHandles = new ConcurrentHashMap<>();

	private volatile boolean shuttingDown;

	// constructors
	
	/**
	 * Create a scheduler according to the configuration
	 */
	private Scheduler(Builder builder) {
		
		this.name = Utils.trim(builder.name, "WispScheduler");
		int minThreads = builder.minThreads > 0 ? builder.minThreads : 1;
		int maxThreads = builder.maxThreads >= minThreads ? builder.maxThreads : minThreads + 4;
		Duration threadsKeepAliveTime = builder.threadsKeepAliveTime != null ? builder.threadsKeepAliveTime : Duration.ofSeconds(30); 
		this.timeProvider = builder.timeProvider != null ? builder.timeProvider : new TimeProviderDefault();
		
		this.threadPoolExecutor = new ScalingThreadPoolExecutor(
				minThreads, maxThreads, 
				threadsKeepAliveTime.toSeconds(), TimeUnit.SECONDS, 
				new DefaultThreadFactory());
		
		// run job launcher thread
		Thread launcherThread = new Thread(this::launcher, "WispLauncher");
		
		if (launcherThread.isDaemon()) {
			launcherThread.setDaemon(false);
		}
		
		launcherThread.start();
	}

	// public API

	public Job schedule(JobTask task, Schedule when) {
		
		Objects.requireNonNull(task, "Runnable task must not be null");
		Objects.requireNonNull(task.jobName(), "Name must not be null");
		Objects.requireNonNull(when, "Schedule must not be null");

		Job job = prepareJob(task, when);
		
		ZonedDateTime currentDateTime = timeProvider.currentDateTime();
		
		if ( when.nextExecutionDateTime(currentDateTime).isBefore(currentDateTime) ) {
			log.warn("The job '{}' is scheduled at a paste date: it will never be executed", job.name());
		}

		log.info("Scheduling job '{}' to run {}", job.name(), job.schedule());
		this.scheduleNextExecution(job);

		return job;
	}

	/**
	 * Fetch the status of all the jobs that has been registered on the {@code Scheduler}
	 * including the {@link JobStatus#DONE} jobs
	 */
	public Collection<Job> jobStatus() {
		return indexedJobsByName.values();
	}

	/**
	 * Find a job by its name
	 */
	public Optional<Job> findJob(String name) {
		return Optional.ofNullable(indexedJobsByName.get(name));
	}

	/**
	 * Issue a cancellation order for a job and
	 * returns immediately a promise that enables to follow the job cancellation status<br>
	 * <br>
	 * If the job is running, the scheduler will wait until it is finished to remove it
	 * from the jobs pool.
	 * If the job is not running, the job will just be removed from the pool.<br>
	 * After the job is cancelled, the job has the status {@link JobStatus#DONE}.
	 *
	 * @param jobName The job name to cancel
	 * @return The promise that succeed when the job is correctly cancelled
	 * and will not be executed again. If the job is running when {@link #cancel(String)}
	 * is called, the promise will succeed when the job has finished executing.
	 * @throws IllegalArgumentException if there is no job corresponding to the job name.
	 */
	public CompletionStage<Job> cancel(String jobName) {
		
		Job job = findJob(jobName).orElseThrow(IllegalArgumentException::new);

		synchronized (this) {
			JobStatus jobStatus = job.status();
			
			if (jobStatus == JobStatus.DONE) {
				return CompletableFuture.completedFuture(job);
			}
			
			CompletableFuture<Job> existingHandle = cancelHandles.get(jobName);
			
			if (existingHandle != null) {
				return existingHandle;
			}

			job.setSchedule(ScheduleFactory.willNeverBeExecuted);
			
			if (jobStatus == JobStatus.READY && threadPoolExecutor.remove(job.runningJob())) {
				scheduleNextExecution(job);
				return CompletableFuture.completedFuture(job);
			}

			if (jobStatus == JobStatus.RUNNING
				// if the job status is/was READY but could not be removed from the thread pool,
				// then we have to wait for it to finish
				|| jobStatus == JobStatus.READY) {
				CompletableFuture<Job> promise = new CompletableFuture<>();
				cancelHandles.put(jobName, promise);
				return promise;
			} else {
				for (Iterator<Job> iterator = nextExecutionsOrder.iterator(); iterator.hasNext();) {
					Job nextJob = iterator.next();
					if(nextJob == job) {
						iterator.remove();
						job.setStatus(JobStatus.DONE);
						return CompletableFuture.completedFuture(job);
					}
				}
				
				throw new IllegalStateException(
					"Cannot find the job " + job + " in " + nextExecutionsOrder
					+ ". Please open an issue on https://github.com/Coreoz/Wisp/issues"
				);
			}
		}
	}
	

	/**
	 * Wait until the current running jobs are executed
	 * and cancel jobs that are planned to be executed.
	 * @param timeout optional maximum time to wait (Default: 10)
	 * @throws InterruptedException if the shutdown lasts more than maximum time to wait in seconds
	 */
	public void shutdownGracefully(Duration... timeout) {
		long timeoutInSeconds = timeout.length > 0 ? timeout[0].toSeconds() : 10;
		
		log.info("[{}] Shutdown invoked ...", this.name);

		if ( ! shuttingDown ) {
			
			synchronized (this) {
				shuttingDown = true;
				threadPoolExecutor.shutdown();
			}

			// stops jobs that have not yet started to be executed
			for(Job job : jobStatus()) {
				
				Runnable runningJob = job.runningJob();
				
				if (runningJob != null) {
					threadPoolExecutor.remove(runningJob);
				}
				
				job.setStatus(JobStatus.DONE);
			}
			
			synchronized (launcherNotifier) {
				launcherNotifier.set(false);
				launcherNotifier.notify();
			}
		}

		try {
			threadPoolExecutor.awaitTermination(timeoutInSeconds, TimeUnit.SECONDS);
			log.info("[{}] Shutdown completed.", this.name);
		} catch (InterruptedException ie) {
			log.warn("[{}] Shutdown timeout (wome threads can be still running (cause: {})", this.name, ie.getMessage());
		}
	}

	/**
	 * Fetch statistics about the current {@code Scheduler}
	 */
	public SchedulerStats stats() {
		return new SchedulerStats(this.name, this.threadPoolExecutor);
	}

	// internal

	private Job prepareJob(JobTask task, Schedule when) {
		
		// lock needed to make sure 2 jobs with the same name are not submitted at the same time
		synchronized (indexedJobsByName) {
			Job lastJob = findJob(task.jobName()).orElse(null);

			if (lastJob != null && lastJob.status() != JobStatus.DONE) {
				throw new IllegalArgumentException("A job is already scheduled with the name:" + task.jobName());
			}

			Job job = new Job(
				JobStatus.DONE,
				Schedule.EPOCH_ZONED_DATE_TIME,
				lastJob != null ? lastJob.executionsCount() : 0,
				lastJob != null ? lastJob.lastExecutionStartDateTime() : Schedule.EPOCH_ZONED_DATE_TIME,
				lastJob != null ? lastJob.lastExecutionEndDateTime() : Schedule.EPOCH_ZONED_DATE_TIME,
				task,
				when
			);
			
			indexedJobsByName.put(task.jobName(), job);

			return job;
		}
	}

	private synchronized void scheduleNextExecution(Job job) {
		
		// clean up
		job.setRunningJob(null);

		// next execution time calculation
		ZonedDateTime currentDateTime = timeProvider.currentDateTime();
		
		try {
			job.setNextExecutionDateTime(
				job.schedule().nextExecutionDateTime(currentDateTime)
			);
		} catch (Exception ex) {
			log.error(
				"An exception was raised during the job next execution time calculation,"
				+ " therefore the job '{}' will not be executed again.",
				job.name(),
				ex
			);
			
			job.setNextExecutionDateTime(Schedule.WILL_NOT_BE_EXECUTED_AGAIN);
		}

		// next execution planning
		if (job.nextExecutionDateTime().isEqual(currentDateTime) || job.nextExecutionDateTime().isAfter(currentDateTime)) {
			job.setStatus(JobStatus.SCHEDULED);
			nextExecutionsOrder.add(job);
			
			nextExecutionsOrder.sort(Comparator.comparing(Job::nextExecutionDateTime));

			synchronized (launcherNotifier) {
				launcherNotifier.set(false);
				launcherNotifier.notify();
			}
		} else {
			log.info(
				"Job '{}' will not be executed again since its next execution time {}, is planned in the past",
				job.name(),
				Dates.format(job.nextExecutionDateTime()));
			
			job.setStatus(JobStatus.DONE);

			CompletableFuture<Job> cancelHandle = cancelHandles.remove(job.name());
			
			if (cancelHandle != null) {
				cancelHandle.complete(job);
			}
		}
	}

	/**
	 * The daemon that will be in charge of placing the jobs in the thread pool
	 * when they are ready to be executed.
	 */
	private void launcher() {
		
		while ( ! shuttingDown ) {
			Long timeBeforeNextExecution = null;
			
			synchronized (nextExecutionsOrder) {
				if (! nextExecutionsOrder.isEmpty()) {
					Job job = nextExecutionsOrder.get(0);
					timeBeforeNextExecution = ChronoUnit.MILLIS.between(timeProvider.currentDateTime(), job.nextExecutionDateTime());
					log.info("[{}] {}", this.name, job);
				}
			}

			if (timeBeforeNextExecution == null || timeBeforeNextExecution > 0L) {
				synchronized (launcherNotifier) {
					if (shuttingDown) {
						return;
					}
					
					// If someone has notified the launcher
					// then the launcher must check again the next job to execute.
					// We must be sure not to miss any changes that would have
					// happened after the timeBeforeNextExecution calculation.
					if (launcherNotifier.get()) {
						
						try {
							if (timeBeforeNextExecution == null) {
								
								launcherNotifier.wait();
							} else {
								launcherNotifier.wait(timeBeforeNextExecution);
							}
						} catch (InterruptedException ie) {
							log.warn("Launcher was interrupted (cause: {})", ie.getMessage());
						}
						
					}
					
					launcherNotifier.set(true);
				}
			} else {
				synchronized (this) {
					if(shuttingDown) {
						return;
					}

					if (! nextExecutionsOrder.isEmpty()) {
						Job jobToRun = nextExecutionsOrder.remove(0);
						jobToRun.setStatus(JobStatus.READY);
						jobToRun.setRunningJob(() -> runJob(jobToRun));
						
						if(threadPoolExecutor.getActiveCount() == threadPoolExecutor.getMaximumPoolSize()) {
							log.warn(
								"Job thread pool is full, either tasks take too much time to execute"
								+ " or either the thread pool is too small"
							);
						}
						
						threadPoolExecutor.execute(jobToRun.runningJob());
					}
				}
			}
		}
	}

	/**
	 * The wrapper around a job that will be executed in the thread pool.
	 * It is especially in charge of logging, changing the job status
	 * and checking for the next job to be executed.
	 * 
	 * @param jobToRun the job to execute
	 */
	private void runJob(Job jobToRun) {
		
		ZonedDateTime startExecutionDateTime = timeProvider.currentDateTime();
		
		if (jobToRun.nextExecutionDateTime().isBefore(startExecutionDateTime)) {
			log.debug("Job '{}' execution was delayed (expected: {})", jobToRun.name(), Dates.format(jobToRun.nextExecutionDateTime()));
		}
		
		jobToRun.setStatus(JobStatus.RUNNING);
		jobToRun.setLastExecutionStartDateTime(startExecutionDateTime);
		jobToRun.setThreadRunningJob(Thread.currentThread());

		Timer tm = new Timer();
		
		try {
			jobToRun.jobTask().run();
		} catch(Exception t) {
			log.error("Job '{}' execution aborded: {}, causedBy: ", jobToRun.name(), t.getMessage(), t);
		}
		
		jobToRun.setExecutionsCount(jobToRun.executionsCount() + 1);
		jobToRun.setLastExecutionEndDateTime(timeProvider.currentDateTime());
		jobToRun.setThreadRunningJob(null);

		log.debug("Job '{}' executed in {}", jobToRun.name(), tm.toSeconds());

		if (shuttingDown) {
			return;
		}
		
		synchronized (this) {
			this.scheduleNextExecution(jobToRun);
		}
	}

	private static class DefaultThreadFactory implements ThreadFactory {
		
		@Override
		public Thread newThread(Runnable r) {
			
			Thread thread = new Thread(r, "WispWorker #" + threadCounter.getAndIncrement());
			
			if (thread.isDaemon()) {
				thread.setDaemon(false);
			}
			
			if (thread.getPriority() != Thread.NORM_PRIORITY) {
				thread.setPriority(Thread.NORM_PRIORITY);
			}
			
			return thread;
		}
	}

	public static class Builder {
		
		private String name;
		private int minThreads;
		private int maxThreads;
		private Duration threadsKeepAliveTime;
		private TimeProvider timeProvider;
		
		public Builder withName(String name) {
			this.name = name;
			return this;
		}
		
		public Builder withMinThreads(int minThreads) {
			this.minThreads = minThreads;
			return this;
		}
		
		public Builder wuthMaxThreads(int maxThreads) {
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
		
		public Scheduler build() {
			return new Scheduler(this);
		}
	}
}
