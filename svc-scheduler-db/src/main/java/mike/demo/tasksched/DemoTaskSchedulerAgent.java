package mike.demo.tasksched;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import mike.bootstrap.springboot.application.Application;

@SpringBootApplication
@ComponentScan(basePackages = { Application.BOOT_BASE_PACKAGE, "mike.demo.tasksched"})
@OpenAPIDefinition(
		info = @Info(
				title = "Task Scheduler Agent (Demo)",
				description = "Task Scheduler Agent",
				version = "1.0")
)
public class DemoTaskSchedulerAgent {

	public static void main(String[] args) {
		Application.servlet(DemoTaskSchedulerAgent.class, args);
	}

}
