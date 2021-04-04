package mike.demo.tasksched.module.core;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Objects;

import mike.bootstrap.utilities.helpers.Dates;
import mike.demo.tasksched.module.core.schedule.Schedule;

public class Task {
 
	private final TaskWorker worker;
	
	private Schedule schedule;
	private TaskState state = TaskState.DISABLED;
	private ZonedDateTime nextExecutionDateTime = Schedule.EPOCH_ZONED_DATE_TIME;
	
	private ZonedDateTime lastExecutionStartDateTime = Schedule.EPOCH_ZONED_DATE_TIME;
	private ZonedDateTime lastExecutionEndDateTime = Schedule.EPOCH_ZONED_DATE_TIME;
	private int executionCount = 0;
	
	Task(TaskWorker worker, Schedule schedule) {
		Objects.requireNonNull(worker, "Worker must not be null");
		Objects.requireNonNull(worker.getName(), "Task name must not be null");
		Objects.requireNonNull(schedule, "Schedule must not be null");
		
		this.worker = worker;
		this.schedule = schedule;
	}

	public String getName() {
		return this.worker.getName();
	}
	
	public Schedule getSchedule() {
		return schedule;
	}

	public TaskState getState() {
		return state;
	}
	
	public ZonedDateTime getNextExecutionDateTime() {
		return nextExecutionDateTime;
	}

	public ZonedDateTime getLastExecutionStartDateTime() {
		return lastExecutionStartDateTime;
	}
	
	public ZonedDateTime getLastExecutionEndDateTime() {
		return lastExecutionEndDateTime;
	}
	
	public Duration getElapsedTime() {
		ZonedDateTime lastExecutionEnd = this.state.isRunning() ? ZonedDateTime.now() : this.lastExecutionEndDateTime;
		
		return Duration.between(this.getLastExecutionStartDateTime(), lastExecutionEnd);
	}

	public int getExecutionCount() {
		return executionCount;
	}

	/* ********** Protected Methods ********** */
	TaskWorker getWorker() {
		return worker;
	}
	
	void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

	void setState(TaskState state) {
		this.state = state;
	}

	void setNextExecutionDateTime(ZonedDateTime nextExecutionDateTime) {
		this.nextExecutionDateTime = nextExecutionDateTime;
	}
	
	void setLastExecutionStartDateTime(ZonedDateTime nextExecutionStartDateTime) {
		this.lastExecutionStartDateTime = nextExecutionStartDateTime;
	}
	
	void setLastExecutionEndDateTime(ZonedDateTime lastExecutionEndDateTime) {
		this.lastExecutionEndDateTime = lastExecutionEndDateTime;
	}

	void incrementExecutionCount() {
		this.executionCount++;
	}
	
	void computeNextExecutionDateTime(ZonedDateTime currentDateTime) {
		ZonedDateTime nextExecution = this.schedule.nextExecutionDateTime(currentDateTime);
		
		if ( nextExecution.isBefore(currentDateTime) ) {
			this.state = TaskState.DISABLED;
		} else {
			this.state = TaskState.SCHEDULED;
		}
		
		this.setNextExecutionDateTime(nextExecution);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("Task [");
		
		// formatter:off
		builder.append("name=").append(this.getName())
				.append(", state=").append(state)
				.append(", nextExecutionDateTime=").append(Dates.format(nextExecutionDateTime))
				.append(", lastExecutionStartDateTime=").append(Dates.format(lastExecutionStartDateTime))
				.append(", lastExecutionEndDateTime=").append(Dates.format(lastExecutionEndDateTime))
				.append(", executionCount=").append(executionCount)
				.append(", schedule=").append(schedule);
		// formatter:on
		
		return builder.append("]").toString();
	}
	
	
}
