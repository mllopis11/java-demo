package mike.demo.tasksched.library.ruby.web.model;

import java.time.ZonedDateTime;

import mike.demo.tasksched.library.ruby.Task;

public class TaskView {

	private final String name;
	private final String cronExpresion;
	private final String cronDescription;
	private final boolean enabled;
	private final boolean executing;
	private final ZonedDateTime nextExecutionDateTime;
	private final ZonedDateTime lastExecutionEndDateTime;
	private final String lastExecutionUuid;
	private final int executionCount;
	private final ZonedDateTime updatedAtDttm;
	private final String updatedByUser;
	
	public TaskView(Task task) {
		this.name = task.getName();
		this.cronExpresion = task.getSchedule().cronExpression();
		this.cronDescription = task.getSchedule().description();
		this.enabled = task.isEnabled();
		this.executing = task.isExecuting();
		this.nextExecutionDateTime = task.getNextExecutionDateTime();
		this.lastExecutionEndDateTime = task.getLastExecutionEndDateTime();
		this.lastExecutionUuid = task.getLastExecutionUuid();
		this.executionCount = task.getExecutionCount();
		this.updatedAtDttm = task.getUpdatedAtDttm();
		this.updatedByUser = task.getUpdatedByUser();
	}
	
	public String getName() {
		return name;
	}

	public String getCronExpresion() {
		return cronExpresion;
	}

	public String getCronDescription() {
		return cronDescription;
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
		return lastExecutionUuid;
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("TaskView [");
		
		// @formatter:off
		builder.append("name=").append(name)
				.append(", cronExpresion=").append(cronExpresion)
				.append(", enabled=").append(enabled)
				.append(", executing=").append(executing)
				.append(", nextExecutionDateTime=").append(nextExecutionDateTime)
				.append(", lastExecutionEndDateTime=").append(lastExecutionEndDateTime)
				.append(", lastExecutionUuid=").append(lastExecutionUuid)
				.append(", executionCount=").append(executionCount)
				.append(", updatedAtDttm=").append(updatedAtDttm)
				.append(", updatedByUser=").append(updatedByUser);
		// @formatter:on
		
		return builder.append("]").toString();
	}
}
