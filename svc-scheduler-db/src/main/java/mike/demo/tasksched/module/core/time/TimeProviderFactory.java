package mike.demo.tasksched.module.core.time;

import java.time.Duration;
import java.time.ZonedDateTime;

public class TimeProviderFactory {

	private TimeProviderFactory() {}
	
	public static TimeProvider newSystemTimeProvider() {
		return ZonedDateTime::now;
	}
	
	public static TimeProvider newAdjustedTimeProvider(Duration duration) {
		return () -> ZonedDateTime.now().plus(duration);
	}
	
	public static TimeProvider newFixedTimeProvider(ZonedDateTime zonedDatetime) {
		return () -> zonedDatetime;
	}
}
