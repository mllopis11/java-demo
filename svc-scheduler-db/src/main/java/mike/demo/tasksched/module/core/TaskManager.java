package mike.demo.tasksched.module.core;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import mike.demo.tasksched.module.core.schedule.Schedule;
import mike.demo.tasksched.module.core.time.TimeProvider;

class TaskManager {

	private final TaskRepository taskRepository;
	private final TimeProvider timeProvider;
	
	TaskManager(TaskRepository taskRepository, TimeProvider timeProvider) {
		this.taskRepository = taskRepository;
		this.timeProvider = timeProvider;
	}
	
	Stream<Task> findTasksToRun() {
		return this.taskRepository.findTasksToRun(timeProvider.currentDateTime());
	}
	
	Stream<Task> findAll() {
		return this.taskRepository.findAll();
	}
	
	Optional<Task> find(String name) {
		return this.taskRepository.find(name);
	}
	
	Task schedule(TaskWorker worker, Schedule when) {
		Objects.requireNonNull(worker.getName(), "Task name must not be null");

		this.find(worker.getName()).ifPresent(t -> new IllegalArgumentException("Task already exists: " + t.getName()));
		
		Task task = new Task(worker, when);
		task.computeNextExecutionDateTime(timeProvider.currentDateTime());
		this.taskRepository.insert(task);
		
		return task;
	}
	
	Task setTaskQueued(Task task) {
		task.setState(TaskState.QUEUED);
		return this.taskRepository.update(task);
	}
	
	Task setTaskRunning(Task task) {
		task.setState(TaskState.RUNNING);
		task.setLastExecutionStartDateTime(this.timeProvider.currentDateTime());
		return this.taskRepository.update(task);
	}
	
	Task setTaskCompleted(Task task) {
		task.setLastExecutionEndDateTime(this.timeProvider.currentDateTime());
		task.computeNextExecutionDateTime(timeProvider.currentDateTime());
		task.incrementExecutionCount();
		return this.taskRepository.update(task);
	}
}
