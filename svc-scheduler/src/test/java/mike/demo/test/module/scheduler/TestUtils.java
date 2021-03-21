package mike.demo.test.module.scheduler;

import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mike.bootstrap.utilities.exceptions.ApplicationErrorException;
import mike.bootstrap.utilities.helpers.Timer;
import mike.bootstrap.utilities.helpers.Utils;
import mike.demo.tasksched.module.scheduler.JobTask;

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
	
	static class FooTask implements JobTask {

		private static final Logger log = LoggerFactory.getLogger(FooTask.class);
		
		private final String name = "FooTask";
		private final int seconds;
		private final boolean throwException;
		
		FooTask() {
			this(2);
		}
		
		FooTask(int seconds) {
			this(seconds, false);
		}
		
		FooTask(int seconds, boolean throwException) {
			this.seconds = seconds;
			this.throwException = throwException;
		}
		
		@Override
		public void run() {
			
			Timer tm = new Timer();
			
			log.debug("[Job::{}] started (wait: {} seconds)", name, seconds);
			
			tm.pause(seconds);
			
			if ( throwException ) {
				log.debug("[Job::{}] aborted (elapsed: {})", name, tm.elapsTime());
				throw new ApplicationErrorException("%s: raise forced exception", name);
			}
			
			log.debug("[Job::{}] completed (elapsed: {})", name, tm.elapsTime());
		}

		@Override
		public String jobName() {
			return this.name;
		}
	}
}