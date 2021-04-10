package mike.demo.tasksched.library.ruby.web.rest;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import mike.bootstrap.springboot.openapi.problem.ProblemResponsesReference;
import mike.demo.tasksched.library.ruby.RubyScheduler;
import mike.demo.tasksched.library.ruby.web.model.TaskView;

@RestController
@RequestMapping(value = "/v1/tasks", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Tasks", description = "Scheduler task operations")
class SchedulerTaskResources {

	private static final Logger log = LoggerFactory.getLogger(SchedulerTaskResources.class);
			
	private final RubyScheduler scheduler;
	
	public SchedulerTaskResources(RubyScheduler scheduler) {
		this.scheduler = scheduler;
	}
	
	@Operation(
			summary = "Retrieve all tasks", 
			description = "Retrieve all configured tasks.")
	@ApiResponse(responseCode = "200", description = "Tasks", 
			content = @Content(
					mediaType = APPLICATION_JSON_VALUE, 
					array = @ArraySchema(schema = @Schema(implementation = TaskView.class))))
	@ApiResponse(responseCode = "500", ref = ProblemResponsesReference.INTERNAL_SERVER_ERROR_500)
	@GetMapping("/")
	public List<TaskView> findTasks() {
		log.info("Task::request: retrieve all tasks ...");
		return this.scheduler.taskService().findAllTask().map(TaskView::new).collect(Collectors.toList());
	}
}
