package mike.demo.test.module.core;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mike.bootstrap.utilities.exceptions.ResourceAlreadyExistException;
import mike.bootstrap.utilities.helpers.Dates;
import mike.bootstrap.utilities.helpers.Utils;
import mike.demo.tasksched.module.core.Scheduler;
import mike.demo.tasksched.module.core.Task;
import mike.demo.tasksched.module.core.TaskState;
import mike.demo.tasksched.module.core.TaskWorker;
import mike.demo.tasksched.module.core.schedule.Schedule;
import mike.demo.tasksched.module.core.schedule.ScheduleFactory;
import mike.demo.tasksched.module.core.time.TimeProvider;
import mike.demo.tasksched.module.core.time.TimeProviderFactory;

@DisplayName("Scheduler::ScheduleTask")
class ScheduleTaskTest {

	private static final Logger log = LoggerFactory.getLogger(ScheduleTaskTest.class);
	
	@Test
	void should_schedule_task_for_the_next_day() {
		
		log.debug("***** Schedule Task For the Next Day *****");
		
		Scheduler scheduler = new Scheduler.Builder().build();
		scheduler.start();
		
		TaskWorker fooWorker = new FooWorker("Foo");
		LocalTime scheduleTime = LocalTime.now().minusHours(1);
		Schedule fooSchedule = ScheduleFactory.atFixedTimeMondayToFriday(scheduleTime);
				
		Task task = scheduler.schedule(fooWorker, fooSchedule);

		assertThat(task.getName()).isEqualTo("Foo");
		assertThat(task.getExecutionCount()).isZero();
		assertThat(task.getState()).isEqualTo(TaskState.SCHEDULED);
		assertThat(task.getNextExecutionDateTime()).isAfter(ZonedDateTime.now());
		assertThat(task.getLastExecutionStartDateTime()).isEqualTo(Dates.toZonedDateTimeEpoch());
		assertThat(task.getLastExecutionEndDateTime()).isEqualTo(Dates.toZonedDateTimeEpoch());
		
		Utils.pause(2);
		
		scheduler.shutdown();
	}
	
	@Test
	void should_not_schedule_task_with_same_name() {
		
		log.debug("***** Do Not Schedule Task With the Same Name *****");
		
		String fooName = "Foo";
		
		Scheduler scheduler = new Scheduler.Builder().build();
		
		// Schedule FooTask#1
		TaskWorker fooWorker1 = new FooWorker(fooName);
		LocalTime scheduleTime1 = LocalTime.now().minusHours(1);
		Schedule fooSchedule1 = ScheduleFactory.atFixedTimeMondayToFriday(scheduleTime1);
		scheduler.schedule(fooWorker1, fooSchedule1);
		
		assertThat(scheduler.task(fooName)).isPresent();
		assertThat(scheduler.tasks()).hasSize(1);
		
		// Schedule FooTask#2
		TaskWorker fooWorker2 = new FooWorker(fooName);
		LocalTime scheduleTime2 = LocalTime.now().minusHours(2);
		Schedule fooSchedule2 = ScheduleFactory.atFixedTimeMondayToFriday(scheduleTime2);
		
		assertThatExceptionOfType(ResourceAlreadyExistException.class)
			.isThrownBy(() -> scheduler.schedule(fooWorker2, fooSchedule2));
		
		assertThat(scheduler.tasks()).hasSize(1);
		
		scheduler.shutdown();
	}
	
	@Test
	void should_invoke_task_when_schedule_time_is_reached() {
		
		log.debug("***** Invoke Task When Schedule Time is Reached *****");
		
		TimeProvider timeProvider = TimeProviderFactory.newAdjustedTimeProvider(Duration.ofSeconds(-10));
		
		Scheduler scheduler = new Scheduler.Builder().withTimeProvider(timeProvider).build();
		scheduler.start();
		
		TaskWorker fooWorker = new FooWorker("Foo");
		LocalTime scheduleTime = LocalTime.now();
		Schedule fooSchedule = ScheduleFactory.atFixedTimeEveryDay(scheduleTime);
		
		Task taskToRun = scheduler.schedule(fooWorker, fooSchedule);
		
		assertThat(scheduler.tasks()).hasSize(1);

		log.debug("Waiting for task being executed ...");
		TestUtils.waitFor(taskToRun, () -> taskToRun.getExecutionCount() > 0, 120);

		ZonedDateTime expectedDateTime = ZonedDateTime
				.of(LocalDate.now().plusDays(1), scheduleTime, ZoneId.systemDefault())
				.truncatedTo(ChronoUnit.SECONDS);
		
		assertThat(scheduler.task("Foo")).isPresent().get().satisfies( t -> {
			assertThat(t.getState()).isEqualTo(TaskState.SCHEDULED);
			assertThat(t.getNextExecutionDateTime()).isEqualTo(expectedDateTime);
		});
		
		scheduler.shutdown();
	}
}

class FooWorker implements TaskWorker {

	private static final Logger log = LoggerFactory.getLogger(FooWorker.class);
	
	private final String name;
	private final int waitInSeconds;
	
	FooWorker(String name) {
		this(name, 1);
	}
	
	FooWorker(String name, int waitInSeconds) {
		this.name = name;
		this.waitInSeconds = waitInSeconds;
	}
	
	@Override
	public void invoke() {
		log.debug("[Task::{}] wait {} second(s)", this.name, this.waitInSeconds);
		Utils.pause(waitInSeconds);
	}
	
	@Override
	public String getName() {
		return name;
	}
}

