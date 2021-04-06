package mike.demo.tasksched.library.ruby;

public enum TaskStatus {

	QUEUED,
	RUNNING,
	SUCCESS,
	WARNING,
	ERROR;
	
	public boolean isRunning() {
		return this == RUNNING;
	}
	
	/**
	 * Indicates that the task is placed in the queued.
	 * 
	 * @return true when task state is QUEUED or RUNNING
	 */
	public boolean isExecuting() {
		return this == QUEUED || this == RUNNING;
	}
	
	public boolean isSuccess() {
		return this == SUCCESS;
	}
	
	public boolean isWarning() {
		return this == WARNING;
	}
	
	public boolean isError() {
		return this == ERROR;
	}
	
	public boolean isTerminated() {
		return this.isSuccess() || this.isWarning() || this.isError();
	}
}
