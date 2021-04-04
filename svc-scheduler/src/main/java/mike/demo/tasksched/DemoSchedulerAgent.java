package mike.demo.tasksched;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import mike.bootstrap.springboot.application.Application;

@SpringBootApplication
@ComponentScan(basePackages = { Application.BOOT_BASE_PACKAGE, "mike.demo.tasksched"})
public class DemoSchedulerAgent {

	public static void main(String[] args) {
		Application.servlet(DemoSchedulerAgent.class, args);
	}
}
