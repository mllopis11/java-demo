package mike.demo.tasksched.module.core.repository;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import mike.demo.tasksched.module.core.Task;

public interface TaskRepository {

	Stream<Task> findAll();
	
	Stream<Task> findTasksToRun(ZonedDateTime currentDateTime);
	
	Optional<Task> find(String name);
	
	Task insert(Task task);
	
	Task update(Task task);
	
	boolean delete(String name);
}
