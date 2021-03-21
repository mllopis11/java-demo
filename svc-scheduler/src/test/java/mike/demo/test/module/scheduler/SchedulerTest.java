package mike.demo.test.module.scheduler;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mike.demo.tasksched.module.scheduler.Job;
import mike.demo.tasksched.module.scheduler.Scheduler;
import mike.demo.tasksched.module.scheduler.TimeProvider;
import mike.demo.tasksched.module.scheduler.schedule.Schedule;
import mike.demo.tasksched.module.scheduler.schedule.ScheduleFactory;

@DisplayName("Scheduler::Scheduler")
class SchedulerTest {

	private static final Logger log = LoggerFactory.getLogger(SchedulerTest.class);
	
	private static final String FooTaskName = "FooTask";
	
	@Test
	void should_not_schedule_job_for_the_nextday() {
		
		Scheduler scheduler = new Scheduler.Builder().build();
				
		LocalTime scheduleTime = LocalTime.now();
		Schedule schedule = ScheduleFactory.atFixedTimeEveryDay(scheduleTime);
		
		scheduler.schedule(new TestUtils.FooTask(), schedule);
		
		Job job = scheduler.findJob(FooTaskName).get();
		log.debug("{}", job);
		
		ZonedDateTime expectedDateTime = ZonedDateTime
					.of(LocalDate.now().plusDays(1), scheduleTime, ZoneId.systemDefault())
					.truncatedTo(ChronoUnit.MINUTES);
		
		assertThat(job.nextExecutionDateTime()).isEqualTo(expectedDateTime);
		assertThat(job.executionsCount()).isZero();
		assertThat(job.lastExecutionStartDateTime()).isEqualTo(Schedule.EPOCH_ZONED_DATE_TIME);
		assertThat(job.lastExecutionEndDateTime()).isEqualTo(Schedule.EPOCH_ZONED_DATE_TIME);
		
		scheduler.shutdownGracefully();
	}
	
	@Test
	void should_not_schedule_jobs_with_the_same_name() {
		
		Scheduler scheduler = new Scheduler.Builder().build();
		Schedule schedule = ScheduleFactory.atFixedTimeEveryDay(LocalTime.of(7, 10));
		
		scheduler.schedule(new TestUtils.FooTask(), schedule);
		
		Job job = scheduler.findJob(FooTaskName).get();
		log.debug("{}", job);
		
		assertThatIllegalArgumentException()
			.isThrownBy(() -> scheduler.schedule(new TestUtils.FooTask(), schedule));
		
		scheduler.shutdownGracefully();
	}
	
	@Test
	void should_run_scheduled_job_with_when_shedule_time_is_reached() {
		
		TimeProvider systemTimeProviderMinus1min = () -> ZonedDateTime.now().minusMinutes(1);
		 
		Scheduler scheduler = new Scheduler.Builder()
				.withTimeProvider(systemTimeProviderMinus1min)
				.build();
		
		LocalTime scheduleTime = LocalTime.now();
		Schedule schedule = ScheduleFactory.atFixedTimeEveryDay(scheduleTime);
		
		scheduler.schedule(new TestUtils.FooTask(1), schedule);
		
		final Job jobBefore = scheduler.findJob(FooTaskName).get();
		log.debug("[Scheduler::run:wait] {}", jobBefore);
		
		TestUtils.waitFor(jobBefore, () -> jobBefore.executionsCount() > 0, 60);
		
		//int timeToWaitInSeconds = (int) ChronoUnit.SECONDS.between(systemTimeProviderMinus1min.currentDateTime(), jobBefore.nextExecutionDateTime()) + 5;
		//log.debug("[Scheduler::run:wait] wait for execution: {} sec.", timeToWaitInSeconds);
		//Utils.pause(timeToWaitInSeconds);
		
		Job jobAfter = scheduler.findJob(FooTaskName).get();
		log.debug("[Scheduler::run:done] {}", jobAfter);
		
		ZonedDateTime expectedDateTime = ZonedDateTime
					.of(LocalDate.now().plusDays(1), scheduleTime, ZoneId.systemDefault())
					.truncatedTo(ChronoUnit.MINUTES);
		
		assertThat(jobAfter.executionsCount()).isEqualTo(1);
		assertThat(jobAfter.nextExecutionDateTime()).isEqualTo(expectedDateTime);
		
		scheduler.shutdownGracefully();
	}
}
