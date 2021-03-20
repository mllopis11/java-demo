package mike.demo.tasksched.module.scheduler;

import java.time.ZonedDateTime;

public class TimeProviderDefault implements TimeProvider {

	@Override
	public ZonedDateTime currentDateTime() {
		return ZonedDateTime.now();
	}
}
