package mike.demo.tasksched.module.scheduler;

import java.time.Duration;

import mike.bootstrap.utilities.helpers.Utils;

/**
 * The configuration used by the scheduler
 */
public class SchedulerConfig {

	private final String name;
	private final int minThreads;
	private final int maxThreads;
	private final Duration threadsKeepAliveTime;
	private final TimeProvider timeProvider;

	private SchedulerConfig(Builder builder) {
		this.name = Utils.trim(builder.name, "WispScheduler");
		this.minThreads = builder.minThreads > 0 ? builder.minThreads : 1;
		this.maxThreads = builder.maxThreads >= this.minThreads ? builder.maxThreads : this.minThreads;
		this.threadsKeepAliveTime = builder.threadsKeepAliveTime != null ? builder.threadsKeepAliveTime : Duration.ofSeconds(30); 
		this.timeProvider = builder.timeProvider != null ? builder.timeProvider : new TimeProviderDefault();
	}
	
	public String getName() {
		return name;
	}

	public int getMinThreads() {
		return minThreads;
	}

	public int getMaxThreads() {
		return maxThreads;
	}

	public Duration getThreadsKeepAliveTime() {
		return threadsKeepAliveTime;
	}

	public TimeProvider getTimeProvider() {
		return timeProvider;
	}

	@Override
	public String toString() {
		return String.format("SchedulerConfig [name=%s, minThreads=%d, maxThreads=%d, threadsKeepAliveTime=%s, timeProvider=%s]",
				name, minThreads, maxThreads, threadsKeepAliveTime, timeProvider);
	}

	public static class Builder {
		private String name;
		private int minThreads;
		private int maxThreads;
		private Duration threadsKeepAliveTime;
		private TimeProvider timeProvider;
		
		public Builder() {}
		
		public Builder withName(String name) {
			this.name = name;
			return this;
		}
		
		public Builder withMinThreads(int minThreads) {
			this.minThreads = minThreads;
			return this;
		}
		
		public Builder wuthMaxThreads(int maxThreads) {
			this.maxThreads = maxThreads;
			return this;
		}
		
		public Builder withThreadsKeepAliveTime(Duration duration) {
			this.threadsKeepAliveTime = duration;
			return this;
		}
		
		public Builder withTimeProvider(TimeProvider timeProvider) {
			this.timeProvider = timeProvider;
			return this;
		}
		
		public SchedulerConfig build() {
			return new SchedulerConfig(this);
		}
	}
}
