package mike.demo.tasksched.module.core.repository;

public class TaskRepositoryFactory {

	private TaskRepositoryFactory() {}
	
	public static TaskRepository newDefaultTaskRepository() {
		return new TaskRepositoryDefault();
	}
}
