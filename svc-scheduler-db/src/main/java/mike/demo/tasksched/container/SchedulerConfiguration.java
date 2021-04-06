package mike.demo.tasksched.container;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import mike.demo.tasksched.library.ruby.RubyScheduler;
import mike.demo.tasksched.library.ruby.RubySchedulerBuilder;

@Configuration
class SchedulerConfiguration {
	
	@Bean
	public RubyScheduler rebyScheduler() {
		return new RubySchedulerBuilder().build();
	}
}
