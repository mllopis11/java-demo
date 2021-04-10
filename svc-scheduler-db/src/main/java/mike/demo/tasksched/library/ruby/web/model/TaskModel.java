package mike.demo.tasksched.library.ruby.web.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class TaskModel {

	@NotBlank
	@Size(min = 3, max = 32)
	private String group;

	@NotBlank
	@Size(min = 5, max = 32)
	private String name;

	@NotBlank
	@Size(min = 10, max = 128)
	private String description;

	@NotBlank
	private String cronExpression;

	@NotBlank
	private String workerClassName;

	private boolean enabled = true;

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	public String getWorkerClassName() {
		return workerClassName;
	}

	public void setWorkerClassName(String workerClassName) {
		this.workerClassName = workerClassName;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("TaskModel [");

		// @formatter:off
		builder.append("group=").append(group)
				.append(", name=").append(name)
				.append(", description=").append(description)
				.append(", cronExpression=").append(cronExpression)
				.append(", workerClassName=").append(workerClassName)
				.append(", enabled=").append(enabled);
		// @formatter:on

		return builder.append("]").toString();
	}
}
