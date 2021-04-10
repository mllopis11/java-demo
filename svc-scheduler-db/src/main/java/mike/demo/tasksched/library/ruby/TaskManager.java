package mike.demo.tasksched.library.ruby;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import mike.bootstrap.utilities.exceptions.ResourceAlreadyExistException;
import mike.bootstrap.utilities.exceptions.ResourceNotFoundException;
import mike.demo.tasksched.library.ruby.repository.TaskRepository;
import mike.demo.tasksched.library.ruby.schedule.Schedule;
import mike.demo.tasksched.library.ruby.time.TimeProvider;

class TaskManager implements TaskManagerService {

	private final TaskRepository taskRepository;
	private final TimeProvider timeProvider;
	
	TaskManager(TaskRepository taskRepository, TimeProvider timeProvider) {
		this.taskRepository = taskRepository;
		this.timeProvider = timeProvider;
	}
	
	ZonedDateTime currentDateTime() {
		return this.timeProvider.currentDateTime();
	}
	
	// ********** Scheduled Task **********
	Stream<Task> findTaskToRun() {
		return this.taskRepository.findTaskToRun(timeProvider.currentDateTime());
	}
	
	@Override
	public Stream<Task> findAllTask() {
		return this.taskRepository.findAllTask();
	}
	
	@Override
	public Optional<Task> findTask(String name) {
		return this.taskRepository.findTask(name);
	}
	
	@Override
	public synchronized Task scheduleTask(TaskWorker worker, Schedule when, boolean enabled) {
		Objects.requireNonNull(worker.getName(), "Task name must not be null");

		this.findTask(worker.getName())
				.ifPresent(t -> { 
					throw new ResourceAlreadyExistException("Task already exists: %s", t.getName()); 
				});
		
		Task task = new Task(worker, when, enabled);
		task.computeNextExecutionDateTime(timeProvider.currentDateTime());
		this.taskRepository.insertTask(task);
		
		return task;
	}
	
	@Override
	public synchronized Task enableTask(String name, boolean enabled) {
		Task task = this.findTask(name)
				.orElseThrow( () -> new ResourceNotFoundException("Task not found: %s", name));
		
		if ( enabled ) {
			if ( ! task.isEnabled() ) {
				task.setEnabled(enabled);
				task.computeNextExecutionDateTime(timeProvider.currentDateTime());
				task.setUpdatedAtDttm(ZonedDateTime.now());
			}
		} else {
			task.setEnabled(enabled);
			task.setUpdatedAtDttm(ZonedDateTime.now());
		}
		
		return task;
	}
	
	synchronized Task rescheduleTask(Task task) {
		task.computeNextExecutionDateTime(timeProvider.currentDateTime());
		return this.taskRepository.updateTask(task);
	}
	
	// ********** Task History **********
	
	@Override
	public Stream<TaskHistory> findHistoryByName(String name) {
		return this.taskRepository.findHistoryByName(name)
						.sorted(Comparator.comparing(TaskHistory::getEndedAtDttm))
						.sorted(Collections.reverseOrder());
	}
	
	@Override
	public Optional<TaskHistory> findHistoryByUuid(String uuid) {
		return this.taskRepository.findHistoryByUuid(uuid);
	}
	
	@Override
	public Stream<TaskHistory> findAllHistory() {
		return this.taskRepository.findAllHistory();
	}
	
	/**
	 * Initiate (create) the task history.
	 * 
	 * @param name task name
 	 * @return the task history uuid
	 */
	String setTaskQueued(String name) {
		TaskHistory history = new TaskHistory(name);
		
		history.setStatus(TaskStatus.QUEUED);
		history.setCreatedAtDttm(this.timeProvider.currentDateTime());
		
		this.findTask(history.getName()).ifPresent( task -> task.setExecuting(true) );
		
		this.taskRepository.insertHistory(history);
		
		return history.getUuid();
	}
	
	void setTaskRunning(String uuid) {
		
		this.findHistoryByUuid(uuid)
			.ifPresentOrElse( history -> {
				history.setStatus(TaskStatus.RUNNING);
				history.setStartedAtDttm(this.timeProvider.currentDateTime());
				history.setEndedAtDttm(null);
			
				this.taskRepository.updateHistory(history);
			},
			() -> { throw new ResourceNotFoundException("Task history not found: %s", uuid); });
	}
	
	void setTaskSuccess(String uuid) {
		this.setTaskCompleted(uuid, TaskStatus.SUCCESS);
	}
	
	void setTaskWarning(String uuid) {
		this.setTaskCompleted(uuid, TaskStatus.WARNING);
	}
	
	void setTaskError(String uuid) {
		this.setTaskCompleted(uuid, TaskStatus.ERROR);
	}
	
	private void setTaskCompleted(String uuid, TaskStatus status) {
	
		TaskHistory history = this.findHistoryByUuid(uuid)
				.orElseThrow(() -> new ResourceNotFoundException("Task history not found: %s", uuid));
		
		history.setStatus(status);
		history.setEndedAtDttm(this.timeProvider.currentDateTime());
		
		this.taskRepository.updateHistory(history);
		
		this.findTask(history.getName())
			.ifPresent( task -> {
				task.computeNextExecutionDateTime(timeProvider.currentDateTime());
				task.incrementExecutionCount();
				task.setLastExecutionEndDateTime(history.getEndedAtDttm());
				task.setLastExecutionUuid(history.getUuid());
				task.setExecuting(false);
				this.taskRepository.updateTask(task);
			});
	}
}
