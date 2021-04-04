package mike.demo.test.module.core;

import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.function.Supplier;

import mike.bootstrap.utilities.helpers.Utils;

class TestUtils {

	private TestUtils() {}
	
	static String shortDayName(ZonedDateTime dateTime) {
		return dateTime.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
	}
	
	static void waitFor(Object lockOn, Supplier<Boolean> condition, int maxWaitInSeconds) {
		long currentTime = System.currentTimeMillis();
		long waitUntil = currentTime + (maxWaitInSeconds * 1000);
		
		while (! condition.get() && waitUntil > currentTime) {
			synchronized (lockOn) {
				try {
					lockOn.wait(5);
				} catch (InterruptedException e) {}
			}
			
			currentTime = System.currentTimeMillis();
		}
		
		Utils.pause(2);
	}
}
