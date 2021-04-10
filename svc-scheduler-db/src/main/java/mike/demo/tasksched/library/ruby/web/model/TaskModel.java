package mike.demo.tasksched.library.ruby.web.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class TaskModel {

	@NotBlank
	@Size(min = 5, max = 64)
	private String name;
	
	@NotBlank
	@Size(min = 10, max = 128)
	private String description;
	
	@NotBlank
	private String cronExpression;
	
	private boolean enabled = true;
	
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

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public String toString() {
		return String.format("TaskModel [name=%s, description=%s, cronExpression=%s, enabled=%s]", 
				name, description, cronExpression, enabled);
	}
}
