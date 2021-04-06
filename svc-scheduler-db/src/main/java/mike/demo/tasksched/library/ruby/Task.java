package mike.demo.tasksched.library.ruby;

import java.time.ZonedDateTime;
import java.util.Objects;

import mike.bootstrap.utilities.helpers.Dates;
import mike.demo.tasksched.library.ruby.schedule.Schedule;

public class Task {
 
	private final TaskWorker worker;
	
	private Schedule schedule;
	private boolean enabled = true;
	private boolean executing = false;
	private ZonedDateTime nextExecutionDateTime = Schedule.EPOCH_ZONED_DATE_TIME;
	private ZonedDateTime lastExecutionEndDateTime = Schedule.EPOCH_ZONED_DATE_TIME;
	private String lastExecutionUuid;
	private int executionCount = 0;
	private ZonedDateTime updatedAtDttm;
	private String updatedByUser;
	
	Task(TaskWorker worker, Schedule schedule, boolean enabled) {
		Objects.requireNonNull(worker, "Worker must not be null");
		Objects.requireNonNull(worker.getName(), "Task name must not be null");
		Objects.requireNonNull(schedule, "Schedule must not be null");
		
		this.worker = worker;
		this.schedule = schedule;
		this.enabled = enabled;
	}

	public String getName() {
		return this.worker.getName();
	}
	
	public Schedule getSchedule() {
		return schedule;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean isExecuting() {
		return executing;
	}

	public ZonedDateTime getNextExecutionDateTime() {
		return nextExecutionDateTime;
	}

	public ZonedDateTime getLastExecutionEndDateTime() {
		return lastExecutionEndDateTime;
	}
	
	public String getLastExecutionUuid() {
		return lastExecutionUuid != null ? lastExecutionUuid : "";
	}

	public int getExecutionCount() {
		return executionCount;
	}

	public ZonedDateTime getUpdatedAtDttm() {
		return updatedAtDttm;
	}

	public String getUpdatedByUser() {
		return updatedByUser;
	}

	/* ********** Protected Methods ********** */
	TaskWorker getWorker() {
		return worker;
	}
	
	void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

	void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public void setExecuting(boolean executing) {
		this.executing = executing;
	}

	void setLastExecutionEndDateTime(ZonedDateTime datetime) {
		this.lastExecutionEndDateTime = datetime;
	}

	void setLastExecutionUuid(String uuid) {
		this.lastExecutionUuid = uuid;
	}
	
	void incrementExecutionCount() {
		this.executionCount++;
	}
	
	void computeNextExecutionDateTime(ZonedDateTime currentDateTime) {
		this.executing = false;
		
		if ( this.enabled ) {
			ZonedDateTime nextExecution = this.schedule.nextExecutionDateTime(currentDateTime);
			
			if ( nextExecution.isBefore(currentDateTime) ) {
				this.enabled = false;
			} else {
				this.enabled = true;
				this.nextExecutionDateTime = nextExecution;
			}
		}
	}
	
	void setUpdatedAtDttm(ZonedDateTime updatedAtDttm) {
		this.updatedAtDttm = updatedAtDttm;
	}

	void setUpdatedByUser(String updatedByUser) {
		this.updatedByUser = updatedByUser;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("Task [");
		
		// formatter:off
		builder.append("name=").append(this.getName())
				.append(", enabled=").append(enabled)
				.append(", executing=").append(executing)
				.append(", nextExecutionDateTime=").append(Dates.format(nextExecutionDateTime))
				.append(", lastExecutionEndDateTime=").append(Dates.format(lastExecutionEndDateTime))
				.append(", lastExecutionUuid=").append(lastExecutionUuid)
				.append(", executionCount=").append(executionCount)
				.append(", schedule=").append(schedule);
		// formatter:on
		
		return builder.append("]").toString();
	}
	
	
}
