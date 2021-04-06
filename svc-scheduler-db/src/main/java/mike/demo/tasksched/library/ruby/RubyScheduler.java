package mike.demo.tasksched.library.ruby;

public interface RubyScheduler {

	String name();

	SchedulerState state();

	default SchedulerStatus status() {
		return this.state().getStatus();
	}

	TaskManagerService taskService(); 
	
	boolean start();
	
	boolean suspend();
	
	boolean release();
	
	void shutdown();
}
