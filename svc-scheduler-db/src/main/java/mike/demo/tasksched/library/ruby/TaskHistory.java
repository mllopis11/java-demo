package mike.demo.tasksched.library.ruby;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.UUID;

public class TaskHistory {

	private final String uuid;
	private final String name;
	private TaskStatus status = TaskStatus.QUEUED;
	private ZonedDateTime createdAtDttm = ZonedDateTime.now();
	private ZonedDateTime startedAtDttm;
	private ZonedDateTime endedAtDttm;

	public TaskHistory(String name) {
		this.uuid = UUID.randomUUID().toString();
		this.name = name;
	}

	public String getUuid() {
		return uuid;
	}

	public String getName() {
		return name;
	}

	public TaskStatus getStatus() {
		return status;
	}

	public void setStatus(TaskStatus status) {
		this.status = status;
	}

	public ZonedDateTime getCreatedAtDttm() {
		return createdAtDttm;
	}

	public void setCreatedAtDttm(ZonedDateTime datetime) {
		this.createdAtDttm = datetime;
	}

	public ZonedDateTime getStartedAtDttm() {
		return startedAtDttm;
	}

	public void setStartedAtDttm(ZonedDateTime datetime) {
		this.startedAtDttm = datetime;
	}

	public ZonedDateTime getEndedAtDttm() {
		return endedAtDttm;
	}

	public void setEndedAtDttm(ZonedDateTime datetime) {
		this.endedAtDttm = datetime;
	}

	public Duration getDuration() {
		ZonedDateTime now = ZonedDateTime.now();
		
		return Duration.between(
				this.startedAtDttm == null ? now : this.startedAtDttm, 
				this.endedAtDttm == null ? now : this.endedAtDttm);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("TaskHistory [");

		// @formatter:off
		builder.append("uuid=").append(uuid)
				.append(", name=").append(name)
				.append(", status=").append(status)
				.append(", createdAtDttm=").append(createdAtDttm)
				.append(", startedAtDttm=").append(startedAtDttm)
				.append(", endedAtDttm=").append(endedAtDttm)
				.append(", duration=").append(this.getDuration());
		// @formmatter::on
		
		return builder.append("]").toString();
	}
}
