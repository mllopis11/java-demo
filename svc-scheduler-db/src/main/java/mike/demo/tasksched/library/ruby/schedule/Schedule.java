package mike.demo.tasksched.library.ruby.schedule;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Provide the time of the next executions of a job.
 * The implementations should be thread-safe.
 * Moreover the same instance of a schedule should be usable on multiple jobs.
 */
public interface Schedule {

	static final ZonedDateTime EPOCH_ZONED_DATE_TIME = ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.systemDefault());
	static final ZonedDateTime WILL_NOT_BE_EXECUTED_AGAIN = EPOCH_ZONED_DATE_TIME;
	
	ZonedDateTime nextExecutionDateTime(ZonedDateTime currentTimeInMillis);
	
	/**
	 * @return human understandable description of the schedule
	 */
	String description();
	
	String cronExpression();
}
