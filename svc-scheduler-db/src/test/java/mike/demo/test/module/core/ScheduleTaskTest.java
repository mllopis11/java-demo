package mike.demo.test.module.core;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mike.bootstrap.utilities.exceptions.ResourceAlreadyExistException;
import mike.bootstrap.utilities.helpers.Dates;
import mike.bootstrap.utilities.helpers.Timer;
import mike.demo.tasksched.library.ruby.RubyScheduler;
import mike.demo.tasksched.library.ruby.RubySchedulerBuilder;
import mike.demo.tasksched.library.ruby.Task;
import mike.demo.tasksched.library.ruby.TaskHistory;
import mike.demo.tasksched.library.ruby.TaskStatus;
import mike.demo.tasksched.library.ruby.TaskWorker;
import mike.demo.tasksched.library.ruby.schedule.Schedule;
import mike.demo.tasksched.library.ruby.schedule.ScheduleFactory;
import mike.demo.tasksched.library.ruby.time.TimeProvider;
import mike.demo.tasksched.library.ruby.time.TimeProviderFactory;

@DisplayName("Scheduler::ScheduleTask")
class ScheduleTaskTest {

	private static final Logger log = LoggerFactory.getLogger(ScheduleTaskTest.class);
	
	@Test
	void should_schedule_task_for_the_next_day() {
		
		log.debug("***** Schedule Task For the Next Day *****");
		
		RubyScheduler scheduler = new RubySchedulerBuilder().build();
		
		TaskWorker fooWorker = new FooWorker("Foo");
		LocalTime scheduleTime = LocalTime.now().minusHours(1);
		Schedule fooSchedule = ScheduleFactory.atFixedTimeMondayToFriday(scheduleTime);
				
		Task task = scheduler.taskService().scheduleTask(fooWorker, fooSchedule, true);

		assertThat(task.getName()).isEqualTo("Foo");
		assertThat(task.getExecutionCount()).isZero();
		assertThat(task.isEnabled()).isTrue();
		assertThat(task.isExecuting()).isFalse();
		assertThat(task.getNextExecutionDateTime()).isAfter(ZonedDateTime.now());
		assertThat(task.getLastExecutionEndDateTime()).isEqualTo(Dates.toZonedDateTimeEpoch());
	}
	
	@Test
	void should_not_schedule_task_with_same_name() {
		
		log.debug("***** Do Not Schedule Task With the Same Name *****");
		
		String fooName = "Foo";
		
		RubyScheduler scheduler = new RubySchedulerBuilder().build();
		
		// Schedule FooTask#1
		TaskWorker fooWorker1 = new FooWorker(fooName);
		LocalTime scheduleTime1 = LocalTime.now().minusHours(1);
		Schedule fooSchedule1 = ScheduleFactory.atFixedTimeMondayToFriday(scheduleTime1);
		scheduler.taskService().scheduleTask(fooWorker1, fooSchedule1, true);
		
		assertThat(scheduler.taskService().findTask(fooName)).isPresent();
		assertThat(scheduler.taskService().findAllTask()).hasSize(1);
		
		// Schedule FooTask#2
		TaskWorker fooWorker2 = new FooWorker(fooName);
		LocalTime scheduleTime2 = LocalTime.now().minusHours(2);
		Schedule fooSchedule2 = ScheduleFactory.atFixedTimeMondayToFriday(scheduleTime2);
		
		assertThatExceptionOfType(ResourceAlreadyExistException.class)
			.isThrownBy(() -> scheduler.taskService().scheduleTask(fooWorker2, fooSchedule2, true));
		
		assertThat(scheduler.taskService().findAllTask()).hasSize(1);
	}
	
	@Test
	void should_invoke_task_when_schedule_time_is_reached() {
		
		log.debug("***** Invoke Task When Schedule Time is Reached *****");
		
		TimeProvider timeProvider = TimeProviderFactory.newAdjustedTimeProvider(Duration.ofSeconds(-10));
		
		RubyScheduler scheduler = new RubySchedulerBuilder().withTimeProvider(timeProvider).build();
		scheduler.start();
		
		TaskWorker fooWorker = new FooWorker("Foo");
		LocalTime scheduleTime = LocalTime.now();
		Schedule fooSchedule = ScheduleFactory.atFixedTimeEveryDay(scheduleTime);
		
		Task taskToRun = scheduler.taskService().scheduleTask(fooWorker, fooSchedule, true);
		
		assertThat(scheduler.taskService().findAllTask()).hasSize(1);

		log.debug("Waiting for task being executed ...");
		TestUtils.waitFor(taskToRun, () -> taskToRun.getExecutionCount() > 0, 60);

		ZonedDateTime expectedDateTime = ZonedDateTime
				.of(LocalDate.now().plusDays(1), scheduleTime, ZoneId.systemDefault())
				.truncatedTo(ChronoUnit.SECONDS);
		
		assertThat(scheduler.taskService().findTask("Foo")).isPresent().get().satisfies( t -> {
			assertThat(t.isEnabled()).isTrue();
			assertThat(t.isExecuting()).isFalse();
			assertThat(t.getNextExecutionDateTime()).isEqualTo(expectedDateTime);
			assertThat(t.getLastExecutionEndDateTime()).isNotNull();
			assertThat(t.getLastExecutionUuid()).isNotNull().isNotEmpty();
		});
		
		List<TaskHistory> fooHistory = scheduler.taskService().findHistoryByName("Foo")
				.collect(Collectors.toUnmodifiableList());
		
		assertThat(fooHistory).hasSize(1);
		assertThat(fooHistory.get(0)).satisfies( h -> {
			assertThat(h.getStatus()).isEqualTo(TaskStatus.SUCCESS);
			assertThat(h.getCreatedAtDttm()).isNotNull();
			assertThat(h.getStartedAtDttm()).isNotNull().isAfter(h.getCreatedAtDttm());
			assertThat(h.getEndedAtDttm()).isNotNull().isAfter(h.getStartedAtDttm());
			assertThat(h.getDuration()).isGreaterThan(Duration.ZERO);
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
		Timer.pause(waitInSeconds);
	}
	
	@Override
	public String getName() {
		return name;
	}
}

