package mike.demo.tasksched.module.scheduler;

import java.time.ZonedDateTime;

import mike.bootstrap.utilities.helpers.Dates;
import mike.demo.tasksched.module.scheduler.schedule.Schedule;


/**
 * A {@code Job} is the association of a {@link Runnable} process
 * and its running {@link Schedule}.<br/>
 * <br/>
 * A {@code Job} also contains information about its status and its running
 * statistics.
 */
public class Job {

	private JobStatus status;
	private volatile ZonedDateTime nextExecutionDateTime;
	private volatile int executionsCount;
	private ZonedDateTime lastExecutionStartDateTime;
	private ZonedDateTime lastExecutionEndDateTime;
	private Thread threadRunningJob;
	private Schedule schedule;
	private final JobTask jobTask;
	private Runnable runningJob;

	// public API

	public JobStatus status() {
		return status;
	}

	public ZonedDateTime nextExecutionDateTime() {
		return this.nextExecutionDateTime;
	}

	public int executionsCount() {
		return this.executionsCount;
	}

	/**
	 * The timestamp of when the job has last been started.
	 */
	public ZonedDateTime lastExecutionStartDateTime() {
		return this.lastExecutionStartDateTime;
	}

	/**
	 * The timestamp of when the job has last finished executing.
	 */
	public ZonedDateTime lastExecutionEndDateTime() {
		return lastExecutionEndDateTime;
	}

	public Thread threadRunningJob() {
		return threadRunningJob;
	}

	public String name() {
		return this.jobTask.jobName();
	}

	public Schedule schedule() {
		return schedule;
	}

	public JobTask jobTask() {
		return jobTask;
	}

	// package API

	Job(JobStatus status, ZonedDateTime nextExecutionDateTime, int executionsCount,
			ZonedDateTime lastExecutionStartDateTime, ZonedDateTime lastExecutionEndDateTime,
			JobTask jobTask, Schedule schedule) {
		this.setStatus(status);
		this.setNextExecutionDateTime(nextExecutionDateTime);
		this.setExecutionsCount(executionsCount);
		this.setLastExecutionStartDateTime(lastExecutionStartDateTime);
		this.setLastExecutionEndDateTime(lastExecutionEndDateTime);;
		this.setSchedule(schedule);
		this.jobTask = jobTask;
	}

	void setStatus(JobStatus status) {
		this.status = status;
	}

	void setNextExecutionDateTime(ZonedDateTime nextExecutionDateTime) {
		this.nextExecutionDateTime = nextExecutionDateTime;
	}

	void setExecutionsCount(int executionsCount) {
		this.executionsCount = executionsCount;
	}

	void setLastExecutionStartDateTime(ZonedDateTime lastExecutionStartDateTime) {
		this.lastExecutionStartDateTime = lastExecutionStartDateTime;
	}

	void setLastExecutionEndDateTime(ZonedDateTime lastExecutionEndDateTime) {
		this.lastExecutionEndDateTime = lastExecutionEndDateTime;
	}

	void setThreadRunningJob(Thread threadRunningJob) {
		this.threadRunningJob = threadRunningJob;
	}

	void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

	void setRunningJob(Runnable runningJob) {
		this.runningJob = runningJob;
	}

	Runnable runningJob() {
		return runningJob;
	}

	// toString

	@Override
	public String toString() {
		return String.format("Job %s [%s] nextExecutionAt: %s (schedule: %s)", 
				this.name(), status, Dates.format(nextExecutionDateTime), schedule);
	}

}
