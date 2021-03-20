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
	private final String name;
	private Schedule schedule;
	private final Runnable runnable;
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
		return name;
	}

	public Schedule schedule() {
		return schedule;
	}

	public Runnable runnable() {
		return runnable;
	}

	// package API

	Job(JobStatus status, ZonedDateTime nextExecutionDateTime, int executionsCount,
			ZonedDateTime lastExecutionStartDateTime, ZonedDateTime lastExecutionEndDateTime,
			String name, Schedule schedule, Runnable runnable) {
		this.setStatus(status);
		this.setNextExecutionDateTime(nextExecutionDateTime);
		this.setExecutionsCount(executionsCount);
		this.setLastExecutionStartDateTime(lastExecutionStartDateTime);
		this.setLastExecutionEndDateTime(lastExecutionEndDateTime);;
		this.name = name;
		this.setSchedule(schedule);
		this.runnable = runnable;
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
				name, status, Dates.format(nextExecutionDateTime), schedule);
	}

}
