package mike.demo.tasksched.library.ruby.repository;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import mike.demo.tasksched.library.ruby.Task;
import mike.demo.tasksched.library.ruby.TaskHistory;

public interface TaskRepository {

	// Scheduled tasks
	Stream<Task> findAllTask();
	
	default Stream<Task> findTaskToRun(ZonedDateTime currentDateTime) {
		return this.findAllTask()
				.filter(t -> t.isEnabled())
				.filter(t -> ! t.isExecuting())
				.filter(t -> t.getNextExecutionDateTime().isBefore(currentDateTime));
	}
	
	Optional<Task> findTask(String name);
	
	Task insertTask(Task task);
	
	Task updateTask(Task task);
	
	boolean deleteTask(String name);
	
	// Task History
	Stream<TaskHistory> findAllHistory();
	
	Optional<TaskHistory> findHistoryByUuid(String uuid);
	
	default Stream<TaskHistory> findHistoryByName(String name) {
		return this.findAllHistory().filter(t -> t.getName().equals(name));
	}
	
	TaskHistory insertHistory(TaskHistory task);
	
	TaskHistory updateHistory(TaskHistory task);
}
