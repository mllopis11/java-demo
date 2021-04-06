package mike.demo.tasksched.module.scheduler.web.rest;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
import mike.demo.tasksched.library.ruby.SchedulerState;

@RestController
@RequestMapping(value = "/v1/scheduler", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Scheduler", description = "Scheduler operations")
class SchedulerResources {

	private static final Logger log = LoggerFactory.getLogger(SchedulerResources.class);
	
	private final RubyScheduler scheduler;
	
	public SchedulerResources(RubyScheduler scheduler) {
		this.scheduler = scheduler;
	}
	
	@Operation(
			summary = "Scheduler State", 
			description = "Retrieve the scheduler current state.")
	@ApiResponse(responseCode = "200", description = "Current scheduler state", 
			content = @Content(
					mediaType = APPLICATION_JSON_VALUE, 
					array = @ArraySchema(schema = @Schema(implementation = SchedulerState.class))))
	@ApiResponse(responseCode = "500", ref = ProblemResponsesReference.INTERNAL_SERVER_ERROR_500)
	@GetMapping("/state")
	public SchedulerState state() {
		log.info("Scheduler::request: retrieve current state ...");
		return this.scheduler.state();
	}
	
	@Operation(
			summary = "Suspend scheduler", 
			description = "Place the scheduler in status SUSPENDED.<br>"
					+ "No more task will be executed a RELEASE event is issued.")
	@ApiResponse(responseCode = "200", description = "Current scheduler state", 
			content = @Content(
					mediaType = APPLICATION_JSON_VALUE, 
					array = @ArraySchema(schema = @Schema(implementation = SchedulerState.class))))
	@ApiResponse(responseCode = "500", ref = ProblemResponsesReference.INTERNAL_SERVER_ERROR_500)
	@PutMapping("/suspend")
	public SchedulerState suspend() {
		log.info("Scheduler::request: SUSPEND scheduler request issued ...");
		this.scheduler.suspend();
		return this.scheduler.state();
	}
	
	@Operation(
			summary = "Release scheduler", 
			description = "Place the scheduler in status LISTEN.<br>")
	@ApiResponse(responseCode = "200", description = "Current scheduler state", 
			content = @Content(
					mediaType = APPLICATION_JSON_VALUE, 
					array = @ArraySchema(schema = @Schema(implementation = SchedulerState.class))))
	@ApiResponse(responseCode = "500", ref = ProblemResponsesReference.INTERNAL_SERVER_ERROR_500)
	@PutMapping("/release")
	public SchedulerState release() {
		log.info("Scheduler::request: REELASE scheduler request issued ...");
		this.scheduler.release();
		return this.scheduler.state();
	}
}
