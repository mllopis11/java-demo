package mike.demo.tasksched.library.ruby.repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Stream;

import mike.bootstrap.utilities.exceptions.ResourceAlreadyExistException;
import mike.bootstrap.utilities.exceptions.ResourceNotFoundException;
import mike.demo.tasksched.library.ruby.Task;

class DefaultTaskScheduledRespository {

	private final Map<String, Task> tasks = Collections.synchronizedMap(new HashMap<>());
	
	public Stream<Task> findAll() {
		return this.tasks.entrySet().stream().map(Entry::getValue);
	}
	
	public Optional<Task> findByName(String name) {
		return Optional.ofNullable(this.tasks.get(name));
	}
	
	public Task insert(Task task) {
		this.findByName(task.getName())
				.ifPresent( t -> { 
					throw new ResourceAlreadyExistException("task already exists: %s", t.getName()); 
				});
		
		return this.tasks.put(task.getName(), task);
	}

	public Task update(Task task) {
		this.findByName(task.getName())
				.orElseThrow(() -> new ResourceNotFoundException("no such task: %s", task.getName()));
		
		return this.tasks.put(task.getName(), task);
	}

	public boolean delete(String name) {
		return this.tasks.remove(name) != null;
	}
}
