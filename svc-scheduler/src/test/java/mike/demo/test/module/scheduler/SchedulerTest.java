package mike.demo.test.module.scheduler;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mike.demo.tasksched.module.scheduler.Job;
import mike.demo.tasksched.module.scheduler.Scheduler;
import mike.demo.tasksched.module.scheduler.schedule.Schedule;
import mike.demo.tasksched.module.scheduler.schedule.ScheduleFactory;

@DisplayName("Scheduler::Scheduler")
class SchedulerTest {

	private static final Logger log = LoggerFactory.getLogger(SchedulerTest.class);
	
	@Test
	void sould_not_schedule_jobs_with_the_same_name() {
		
		Scheduler scheduler = new Scheduler();
		Schedule schedule = ScheduleFactory.atFixedTimeEveryDay(LocalTime.of(7, 10));
		
		scheduler.schedule("Foo", new TestUtils.FooTask(), schedule);
		
		Optional<Job> job = scheduler.findJob("Foo");
		assertThat(job).isPresent();
		
		log.debug("{}", scheduler.findJob("Foo").get());
		
		assertThatIllegalArgumentException().isThrownBy( () -> scheduler.schedule("Foo", new TestUtils.FooTask(), schedule));
		
		scheduler.shutdownGracefully();
	}
}
