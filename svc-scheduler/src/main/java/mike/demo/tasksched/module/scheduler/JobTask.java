package mike.demo.tasksched.module.scheduler;

public interface JobTask extends Runnable {

	String jobName();
}
