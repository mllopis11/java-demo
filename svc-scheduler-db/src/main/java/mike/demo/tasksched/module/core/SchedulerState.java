package mike.demo.tasksched.module.core;

public enum SchedulerState {

	READY,
	LISTEN,
	SUSPENDED,
	SHUTDOWN,
	STOPPED;
	
	public boolean isReady() {
		return this == READY;
	}
	
	public boolean isListening() {
		return this == LISTEN;
	}
	
	public boolean isSuspended() {
		return this == SUSPENDED;
	}
	
	public boolean isStopping() {
		return this.isShuttingDown() || this.isStopped();
	}
	
	public boolean isNotStopping() {
		return ! this.isStopping();
	}
	
	public boolean isShuttingDown() {
		return this == SHUTDOWN;
	}
	
	public boolean isStopped() {
		return this == STOPPED;
	}
}
