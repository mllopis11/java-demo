package mike.demo.test.module.core;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mike.demo.tasksched.module.core.Scheduler;
import mike.demo.tasksched.module.core.SchedulerState;
import mike.demo.tasksched.module.core.SchedulerStats;

@DisplayName("Scheduler::Scheduler")
@TestMethodOrder(OrderAnnotation.class)
class SchedulerTest {

	private static final Logger log = LoggerFactory.getLogger(SchedulerTest.class);
	
	@Test
	@Order(1)
	void should_return_scheduler_stats_when_default_config() {
		Scheduler scheduler = new Scheduler.Builder().build();
		SchedulerStats stats = scheduler.stats();
		
		assertThat(stats.isTerminating()).isFalse();
		assertThat(stats.getState().isReady()).isTrue();
		assertThat(stats.getMinThreads()).isEqualTo(1);
		assertThat(stats.getMaxThreads()).isEqualTo(5);
		assertThat(stats.getActiveThreads()).isZero();
		assertThat(stats.getIdleThreads()).isZero();
		assertThat(stats.getLargestPoolSize()).isZero();
		
		scheduler.shutdown();
	}
	
	@Test
	@Order(10)
	void should_manager_scheduler() {
		
		Scheduler scheduler = new Scheduler.Builder().build();
		
		assertThat(scheduler.state()).isEqualTo(SchedulerState.READY);

		scheduler.start();
		assertThat(scheduler.state()).isEqualTo(SchedulerState.LISTEN);

		scheduler.suspend();
		
		scheduler.shutdown();
	}
}
