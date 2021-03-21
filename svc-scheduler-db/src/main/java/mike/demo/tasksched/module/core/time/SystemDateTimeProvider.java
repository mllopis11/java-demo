package mike.demo.tasksched.module.core.time;

import java.time.ZonedDateTime;

public class SystemDateTimeProvider implements TimeProvider {

	@Override
	public ZonedDateTime currentDateTime() {
		return ZonedDateTime.now();
	}
}
