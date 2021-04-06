package mike.demo.tasksched.library.ruby;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mike.bootstrap.utilities.helpers.Timer;

class TaskRunner implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(TaskRunner.class);

	private final TaskManager taskManager;
	private final Task task;
	
	private final String uuid;
	
	TaskRunner(Task task, TaskManager taskManager) {
		this.task = task;
		this.taskManager = taskManager;
		
		uuid = this.taskManager.setTaskQueued(task.getName());
		
		log.info("[TaskRunner] Task '{}' (uuid: {}) created", task.getName(), uuid);;
	}
	
	@Override
	public void run() {
		
		Timer tm = new Timer();
		
		this.taskManager.setTaskRunning(uuid);
		
		log.info("[TaskRunner] Task '{}' (uuid: {}) started ...", task.getName(), uuid);
		
		this.task.getWorker().invoke();
		
		this.taskManager.setTaskSuccess(uuid);
		
		log.info("[TaskRunner] Task '{}' (uuid: {}) completed (elapsed: {})", task.getName(), uuid, tm.toSeconds());
	}
}
