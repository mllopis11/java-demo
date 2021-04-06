package mike.demo.tasksched.library.ruby.repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Stream;

import mike.bootstrap.utilities.exceptions.ResourceAlreadyExistException;
import mike.bootstrap.utilities.exceptions.ResourceNotFoundException;
import mike.demo.tasksched.library.ruby.TaskHistory;

class DefaultTaskHistoryRepository {

	private final Map<String, TaskHistory> tasks = Collections.synchronizedMap(new HashMap<>());
	
	public Stream<TaskHistory> findAll() {
		return this.tasks.entrySet().stream().map(Entry::getValue);
	}
	
	public Optional<TaskHistory> findByUuid(String uuid) {
		return Optional.ofNullable(tasks.get(uuid));
	}
	
	public TaskHistory insert(TaskHistory task) {		
		this.findByUuid(task.getUuid()).ifPresent( t -> { 
			throw new ResourceAlreadyExistException("task already exists: %s", t.getName()); 
		});
		
		this.tasks.put(task.getUuid(), task);
		
		return task;
	}

	public TaskHistory update(TaskHistory task) {
		this.findByUuid(task.getUuid())
				.orElseThrow(() -> new ResourceNotFoundException("no such task: %s", task.getName()));
		
		this.tasks.put(task.getUuid(), task);
		
		return task; 
	}

	public boolean delete(String uuid) {
		return this.tasks.remove(uuid) != null;
	}
}
