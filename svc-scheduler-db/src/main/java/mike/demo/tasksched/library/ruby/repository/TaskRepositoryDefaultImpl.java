package mike.demo.tasksched.library.ruby.repository;

import java.util.Optional;
import java.util.stream.Stream;

import mike.demo.tasksched.library.ruby.Task;
import mike.demo.tasksched.library.ruby.TaskHistory;

public class TaskRepositoryDefaultImpl implements TaskRepository {

	private final DefaultTaskScheduledRespository scheduled = new DefaultTaskScheduledRespository();
	private final DefaultTaskHistoryRepository history = new DefaultTaskHistoryRepository();
	
	@Override
	public Stream<Task> findAllTask() {
		return this.scheduled.findAll();
	}
	
	@Override
	public Optional<Task> findTask(String name) {
		return this.scheduled.findByName(name);
	}
	
	@Override
	public Task insertTask(Task task) {
		return this.scheduled.insert(task);
	}
	
	@Override
	public Task updateTask(Task task) {
		return this.scheduled.update(task);
	}
	
	@Override
	public boolean deleteTask(String name) {
		return this.scheduled.delete(name);
	}
	
	@Override
	public Stream<TaskHistory> findAllHistory() {
		return this.history.findAll();
	}
	
	@Override
	public Optional<TaskHistory> findHistoryByUuid(String uuid) {
		return this.history.findByUuid(uuid);
	}
	
	@Override
	public TaskHistory insertHistory(TaskHistory task) {
		return this.history.insert(task);
	}
	
	@Override
	public TaskHistory updateHistory(TaskHistory task) {
		return this.history.update(task);
	}
}
