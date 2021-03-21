package mike.demo.tasksched.module.core;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

import mike.bootstrap.utilities.exceptions.ResourceAlreadyExistException;
import mike.bootstrap.utilities.exceptions.ResourceNotFoundException;

class TaskRepositoryDefault implements TaskRepository {

	private final Map<String, Task> scheduledTasks = Collections.synchronizedMap(new HashMap<>());
	
	TaskRepositoryDefault() {}
	
	@Override
	public Stream<Task> findAll() {
		return this.scheduledTasks.entrySet().stream().map(Entry::getValue);
	}
	
	@Override
	public Stream<Task> findTasksToRun(ZonedDateTime currentDateTime) {
		return this.findAll()
						.filter(t -> t.getState().isScheduled())
						.filter(t -> t.getNextExecutionDateTime().isBefore(currentDateTime));
	}
	
	@Override
	public Optional<Task> find(String name) {
		return Optional.ofNullable(this.scheduledTasks.get(name));
	}
	
	@Override
	public Task insert(Task task) {
		this.find(task.getName())
			.ifPresent( t -> new ResourceAlreadyExistException("task already exists: %s", t.getName()));
		
		this.save(task);
		
		return task;
	}

	@Override
	public Task update(Task task) {
		Task existingTask = this.find(task.getName())
				.orElseThrow(() -> new ResourceNotFoundException("no such task: %s", task.getName()));
		
		this.save(existingTask);
		
		return existingTask;
	}

	@Override
	public boolean delete(String name) {
		return this.scheduledTasks.remove(name) != null;
	}
	
	private void save(Task task) {
		this.scheduledTasks.put(task.getName(), task);
	}
}
