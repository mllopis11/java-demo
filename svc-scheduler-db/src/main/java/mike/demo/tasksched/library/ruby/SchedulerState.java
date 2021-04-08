package mike.demo.tasksched.library.ruby;

import java.time.ZonedDateTime;
import java.util.concurrent.ThreadPoolExecutor;

import com.fasterxml.jackson.annotation.JsonFormat;

import mike.bootstrap.utilities.helpers.Dates;

public class SchedulerState {

	private final String name;
	
	private SchedulerStatus status = SchedulerStatus.READY;
	
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
	private final ZonedDateTime startedAt = ZonedDateTime.now();
	
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
	private ZonedDateTime lastScan = Dates.toZonedDateTimeEpoch();
	
	private int minThreads;
	private int maxThreads;
	private int activeThreads;
	private int idleThreads;
	private int largestPoolSize;
	
	protected SchedulerState(String name, ThreadPoolExecutor executor) {		
		this.name = name;
		this.setStatistics(executor);
	}
	
	public String getName() {
		return name;
	}
	
	public ZonedDateTime getStartedAt() {
		return startedAt;
	}

	public SchedulerStatus getStatus() {
		return this.status;
	}
	
	void setStatus(SchedulerStatus status) {
		this.status = status;
	}

	public ZonedDateTime getLastScan() {
		return lastScan;
	}
	
	void setLastScan(ZonedDateTime lastScan) {
		this.lastScan = lastScan;
	}
	
	void setLastScan() {
		this.lastScan = ZonedDateTime.now();
	}
	
	synchronized void setStatistics(ThreadPoolExecutor executor) {
		this.minThreads = executor.getCorePoolSize();
		this.maxThreads = executor.getMaximumPoolSize();
		this.activeThreads = executor.getActiveCount();
		this.idleThreads = executor.getPoolSize() - this.activeThreads;
		this.largestPoolSize = executor.getLargestPoolSize();
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

	@Override
	public String toString() {
		return String.format(
				"[%s] (%s) lastScan=%s, threadPool={min=%d, max=%d, active=%d, idle=%d, largestPoolSize=%d}, startedAt=%s",
				name, this.status, Dates.format(lastScan), minThreads, maxThreads, activeThreads, idleThreads,
				largestPoolSize, Dates.format(startedAt));
	}

}
