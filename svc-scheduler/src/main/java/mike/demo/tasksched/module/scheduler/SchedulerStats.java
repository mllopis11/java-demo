package mike.demo.tasksched.module.scheduler;

import java.util.concurrent.ThreadPoolExecutor;

public class SchedulerStats {

	private final String name;
	private final int minThreads;
	private final int maxThreads;
	private final int activeThreads;
	private final int idleThreads;
	private final int largestPoolSize;
	private final boolean terminating;
	
	SchedulerStats(String name, ThreadPoolExecutor executor) {
		this.name = name;
		this.minThreads = executor.getCorePoolSize();
		this.maxThreads = executor.getMaximumPoolSize();
		this.activeThreads = executor.getActiveCount();
		this.idleThreads = executor.getPoolSize() - this.activeThreads;
		this.largestPoolSize = executor.getLargestPoolSize();
		this.terminating = executor.isTerminating();
	}
	
	public int getMinThreads() {
		return minThreads;
	}

	public int getMaxThreads() {
		return maxThreads;
	}

	public int getActiveThreads() {
		return activeThreads;
	}

	public int getIdleThreads() {
		return idleThreads;
	}

	public int getLargestPoolSize() {
		return largestPoolSize;
	}

	public boolean isTerminating() {
		return terminating;
	}

	@Override
	public String toString() {
		return String.format(
				"%s (terminating: %s) threads={min=%d, max=%d, active=%d, idle=%d, largestPoolSize=%d}",
				name, terminating, minThreads, maxThreads, activeThreads, idleThreads, largestPoolSize);
	}

}
