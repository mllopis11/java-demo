package mike.demo.test.module.core;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cronutils.model.field.expression.Weekdays;

import mike.demo.tasksched.library.ruby.schedule.Schedule;
import mike.demo.tasksched.library.ruby.schedule.ScheduleFactory;
import mike.demo.tasksched.library.ruby.time.TimeProvider;
import mike.demo.tasksched.library.ruby.time.TimeProviderFactory;

@DisplayName("Scheduler::Schedule")
class ScheduleTest {

	private static final Logger log = LoggerFactory.getLogger(ScheduleTest.class);  
		
	@BeforeAll
	static void init() {
		log.debug("***** Schedule Computation *****");
	}
	
	@Test
	void should_return_next_execution_time_minus_one_when_disabled_schedule() {
		
		TimeProvider time = TimeProviderFactory.newSystemTimeProvider();
		Schedule schedule = ScheduleFactory.willNeverBeExecuted;
		
		assertThat(schedule.nextExecutionDateTime(time.currentDateTime())).isEqualTo(Schedule.WILL_NOT_BE_EXECUTED_AGAIN);
		assertThat(schedule.description()).isNotEmpty().isEqualTo("Disabled schedule");
	}
	
	@Test
	void should_return_next_execution_on_monday_07h10_when_monday_to_friday_at_07h10() {
		
		TimeProvider time = TimeProviderFactory.newFixedTimeProvider(toZonedDateTime(2021, 03, 19, 7, 20));
		Schedule schedule = ScheduleFactory.atFixedTimeMondayToFriday(LocalTime.of(7, 10));
		
		ZonedDateTime expectedScheduleTime = toZonedDateTime(2021, 03, 22, 07, 10);
		ZonedDateTime computedScheduleTime = schedule.nextExecutionDateTime(time.currentDateTime());
		
		log.debug("MondayToFridayAt07h10: expected: {} -> {} (computed) Schedule: {}", 
				expectedScheduleTime, computedScheduleTime, schedule);
		
		assertThat(computedScheduleTime).isEqualTo(expectedScheduleTime);
		assertThat(schedule.description()).startsWith("at 07:10").contains("Monday").endsWith("Friday");
	}
	
	@Test
	void should_return_next_execution_tuesday_07h10_when_tuesday_and_friday_at_07h10() {
		
		TimeProvider time = TimeProviderFactory.newFixedTimeProvider(toZonedDateTime(2021, 03, 19, 7, 20));
		Schedule schedule = ScheduleFactory.atFixedTimeOnWeekDays(LocalTime.of(7, 10), Weekdays.TUESDAY, Weekdays.FRIDAY);
		
		ZonedDateTime expectedScheduleTime = toZonedDateTime(2021, 03, 23, 07, 10);
		ZonedDateTime computedScheduleTime = schedule.nextExecutionDateTime(time.currentDateTime());
		
		log.debug("TuesdayAndFridayAt07h10: expected: {} -> {} (computed) Schedule: {}", 
				expectedScheduleTime, computedScheduleTime, schedule);
		
		assertThat(computedScheduleTime).isEqualTo(expectedScheduleTime);
		assertThat(schedule.description()).startsWith("at 07:10").contains("Tuesday").contains("Friday");
	}
	
	@Test
	void should_return_next_execution_saturday_07h10_when_every_day_at_07h10() {
		TimeProvider time = TimeProviderFactory.newFixedTimeProvider(toZonedDateTime(2021, 03, 19, 7, 20));
		Schedule schedule = ScheduleFactory.atFixedTimeEveryDay(LocalTime.of(7, 10));
		
		ZonedDateTime expectedScheduleTime = toZonedDateTime(2021, 03, 20, 07, 10);
		ZonedDateTime computedScheduleTime = schedule.nextExecutionDateTime(time.currentDateTime());
		
		log.debug("TuesdayAndFridayAt07h10: expected: {} -> {} (computed) Schedule: {}", 
				expectedScheduleTime, computedScheduleTime, schedule);
		
		assertThat(computedScheduleTime).isEqualTo(expectedScheduleTime);
		assertThat(schedule.description()).startsWith("at 07:10").contains("Monday").endsWith("Sunday");
	}
	
	private ZonedDateTime toZonedDateTime(int year, int month, int day, int hour, int minute) {
		return ZonedDateTime.of(year, month, day, hour, minute, 0, 0, ZoneId.systemDefault());
	}
}
