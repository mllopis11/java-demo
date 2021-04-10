package mike.demo.tasksched.library.ruby;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TaskWorkerApi implements TaskWorker {

	private static final Logger log = LoggerFactory.getLogger(TaskWorkerApi.class);
	
	private final String name;
	
	public TaskWorkerApi(String name) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void invoke() {
		log.debug("[Task::{}] ", this.name);
		
	}
}
