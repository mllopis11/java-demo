package mike.demo.tasksched.library.ruby;

import java.util.Optional;
import java.util.stream.Stream;

import mike.demo.tasksched.library.ruby.schedule.Schedule;

public interface TaskManagerService {

	// ***** Tasks *****
	Stream<Task> findAllTask();
	
	Optional<Task> findTask(String name);
	
	Task scheduleTask(TaskWorker worker, Schedule when, boolean enabled);
	
	Task enableTask(String name, boolean enabled);
	
	// ***** History *****
	Stream<TaskHistory> findHistoryByName(String name);

	Optional<TaskHistory> findHistoryByUuid(String uuid);
	
	Stream<TaskHistory> findAllHistory();
}
