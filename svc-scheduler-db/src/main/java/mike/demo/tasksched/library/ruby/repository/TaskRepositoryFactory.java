package mike.demo.tasksched.library.ruby.repository;

public class TaskRepositoryFactory {

	private TaskRepositoryFactory() {}
	
	public static TaskRepository newDefaultTaskRepository() {
		return new TaskRepositoryDefaultImpl();
	}
}
