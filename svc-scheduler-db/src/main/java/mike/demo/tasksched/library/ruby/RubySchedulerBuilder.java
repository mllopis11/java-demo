package mike.demo.tasksched.library.ruby;

import mike.bootstrap.utilities.helpers.Utils;
import mike.demo.tasksched.library.ruby.repository.TaskRepository;
import mike.demo.tasksched.library.ruby.repository.TaskRepositoryFactory;
import mike.demo.tasksched.library.ruby.time.TimeProvider;
import mike.demo.tasksched.library.ruby.time.TimeProviderFactory;

public class RubySchedulerBuilder {

	private final String name;
	
	private int minThreads;
	private int maxThreads;
	private int launcherScanIntervalInSeconds; 
	private TimeProvider timeProvider;
	private TaskRepository taskRepository;
	
	public RubySchedulerBuilder() {
		this(null);
	}
	
	public RubySchedulerBuilder(String name) {
		this.name =  Utils.trim(name, "RubyScheduler");
	}
	
	public RubySchedulerBuilder withMinThreads(int minThreads) {
		this.minThreads = minThreads;
		return this;
	}
	
	public RubySchedulerBuilder withMaxThreads(int maxThreads) {
		this.maxThreads = maxThreads;
		return this;
	}
	
	public RubySchedulerBuilder withLauncherScanIntervalInSeconds(int seconds) {
		this.launcherScanIntervalInSeconds = seconds;
		return this;
	}
	
	public RubySchedulerBuilder withTimeProvider(TimeProvider timeProvider) {
		this.timeProvider = timeProvider;
		return this;
	}
	
	public RubySchedulerBuilder withTaskRepository(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
		return this;
	}
	
	public RubyScheduler build() {
		
		this.minThreads = this.minThreads > 10 ? this.minThreads : 5;
		this.maxThreads = this.maxThreads >= minThreads ? this.maxThreads : 20;
		this.timeProvider = this.timeProvider != null ? this.timeProvider : TimeProviderFactory.newSystemTimeProvider();
		this.taskRepository = this.taskRepository != null ? this.taskRepository : TaskRepositoryFactory.newDefaultTaskRepository();
		this.launcherScanIntervalInSeconds = this.launcherScanIntervalInSeconds >= 5 ? this.launcherScanIntervalInSeconds : 5;
		
		return new Scheduler(name, minThreads, maxThreads, launcherScanIntervalInSeconds, timeProvider, taskRepository);
	}
}
