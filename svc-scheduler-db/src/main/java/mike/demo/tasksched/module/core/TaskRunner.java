package mike.demo.tasksched.module.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mike.bootstrap.utilities.helpers.Timer;

class TaskRunner implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(TaskRunner.class);

	private final Task task;
	private final TaskManager taskManager;
	
	TaskRunner(Task task, TaskManager taskManager) {
		this.task = task;
		this.taskManager = taskManager;
		
		this.taskManager.setTaskQueued(task);
	}
	
	@Override
	public void run() {
		
		Timer tm = new Timer();
		
		this.taskManager.setTaskRunning(task);
		
		log.info("[TaskRunner] Task '{}' started ...", task.getName());
		
		this.task.getWorker().invoke();
		
		this.taskManager.setTaskCompleted(task);
		
		log.info("[TaskRunner] Task '{}' completed (elapsed: {})", task.getName(), tm.toSeconds());
	}
}
