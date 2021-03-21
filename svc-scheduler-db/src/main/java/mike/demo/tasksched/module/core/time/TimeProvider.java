package mike.demo.tasksched.module.core.time;

import java.time.ZonedDateTime;

/**
 * The time provider that will be used by the scheduler to plan jobs
 */
@FunctionalInterface
public interface TimeProvider {

	/**
	 * Returns the current time in milliseconds
	 */
	ZonedDateTime currentDateTime();

}
