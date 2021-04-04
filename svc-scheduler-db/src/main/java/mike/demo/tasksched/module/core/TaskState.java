package mike.demo.tasksched.module.core;

public enum TaskState {

	DISABLED,
	SCHEDULED,
	QUEUED,
	RUNNING;
	
	public boolean isRunning() {
		return this == RUNNING;
	}
	
	/**
	 * Indicates that the task is placed in the queued.
	 * 
	 * @return true when task state is QUEUED or RUNNING
	 */
	public boolean isActive() {
		return this == QUEUED || this == RUNNING;
	}
	
	public boolean isDisabled() {
		return this == DISABLED;
	}
	
	public boolean isScheduled() {
		return this == SCHEDULED;
	}
}
