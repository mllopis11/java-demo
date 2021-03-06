package mike.demo.test.module.core;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import mike.bootstrap.utilities.helpers.Timer;
import mike.demo.tasksched.library.ruby.RubyScheduler;
import mike.demo.tasksched.library.ruby.RubySchedulerBuilder;
import mike.demo.tasksched.library.ruby.SchedulerState;
import mike.demo.tasksched.library.ruby.SchedulerStatus;

@DisplayName("Scheduler::Scheduler")
@TestMethodOrder(OrderAnnotation.class)
class SchedulerTest {

	private static final Logger log = LoggerFactory.getLogger(SchedulerTest.class);
	
	@Test
	@Order(1)
	void should_return_scheduler_stats_when_default_config() {
		RubyScheduler scheduler = new RubySchedulerBuilder().build();
		SchedulerState stats = scheduler.state();
		
		assertThat(stats.getStatus().isReady()).isTrue();
		assertThat(stats.getMinThreads()).isEqualTo(5);
		assertThat(stats.getMaxThreads()).isEqualTo(20);
		assertThat(stats.getActiveThreads()).isZero();
		assertThat(stats.getIdleThreads()).isZero();
		assertThat(stats.getLargestPoolSize()).isZero();
	}
	
	@Test
	@Order(10)
	void should_suspend_and_release_scheduler() {
		
		log.debug("***** Should Suspend then Release Scheduler *****");
		
		RubyScheduler scheduler = new RubySchedulerBuilder().build();
		
		assertThat(scheduler.status()).isEqualTo(SchedulerStatus.READY);

		scheduler.start();
		assertThat(scheduler.status()).isEqualTo(SchedulerStatus.LISTEN);

		Timer.pause(2);
		
		boolean suspended = scheduler.suspend();
		assertThat(suspended).isTrue();
		assertThat(scheduler.status()).isEqualTo(SchedulerStatus.SUSPENDED);
		
		Timer.pause(2);
		
		boolean released = scheduler.release();
		assertThat(released).isTrue();
		assertThat(scheduler.status()).isEqualTo(SchedulerStatus.LISTEN);
		
		Timer.pause(2);
		
		scheduler.shutdown();
	}
}
