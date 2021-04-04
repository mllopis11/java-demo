package mike.demo.test.module.core;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.stream.IntStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import mike.bootstrap.utilities.helpers.Utils;
import mike.demo.tasksched.module.core.time.TimeProvider;
import mike.demo.tasksched.module.core.time.TimeProviderFactory;

@DisplayName("Scheduler::TimeProvider")
class TimeProviderTest {

	@Test
	void should_return_same_datetime_when_fixed_time_provider() {
		
		ZonedDateTime fixedDateTime = ZonedDateTime.now();
				
		TimeProvider fixedTimeProvider = TimeProviderFactory.newFixedTimeProvider(fixedDateTime);
		
		IntStream.range(0, 3).forEach( i -> {
			assertThat(fixedTimeProvider.currentDateTime()).isEqualTo(fixedDateTime);
			Utils.pause(2);
		});
	}
	
	@Test
	void should_return_ajusted_datetime_when_adjusted_time_provider() {
		
		int adjustSeconds = -2;
		
		Duration adjustDuration = Duration.ofSeconds(adjustSeconds);
		
		TimeProvider ajustedTimeProvider = TimeProviderFactory.newAdjustedTimeProvider(adjustDuration);
		
		IntStream.range(0, 3).forEach( i -> {
			assertThat(ajustedTimeProvider.currentDateTime().truncatedTo(ChronoUnit.SECONDS))
				.isEqualTo(ZonedDateTime.now().plusSeconds(adjustSeconds).truncatedTo(ChronoUnit.SECONDS));
			
			Utils.pause(1);
		});
	}
	
	@Test
	void should_return_current_datetime_when_system_time_provider() {
		
		TimeProvider ajustedTimeProvider = TimeProviderFactory.newSystemTimeProvider();
		
		IntStream.range(0, 3).forEach( i -> {
			assertThat(ajustedTimeProvider.currentDateTime().truncatedTo(ChronoUnit.SECONDS))
				.isEqualTo(ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS));
			
			Utils.pause(1);
		});
	}
}
