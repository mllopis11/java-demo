package mike.demo.tasksched.container;

import org.springframework.stereotype.Component;

import mike.bootstrap.springboot.application.ContainerBootstrapReady;
import mike.demo.tasksched.library.ruby.RubyScheduler;

@Component
class ContainerReady implements ContainerBootstrapReady{

	private final RubyScheduler scheduler;
	
	public ContainerReady(RubyScheduler scheduler) {
		this.scheduler = scheduler;
	}
	
	@Override
	public void onContainerReady() {
		this.scheduler.start();
	}
}
