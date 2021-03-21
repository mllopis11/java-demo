package mike.demo.tasksched.module.scheduler.schedule;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.cronutils.model.field.expression.Weekdays;

/**
 * Static helpers to build {@link Schedule}
 */
public class ScheduleFactory {

	private ScheduleFactory() {}
	
	public static final Schedule willNeverBeExecuted = new Schedule() {
		
		@Override
		public ZonedDateTime nextExecutionDateTime(ZonedDateTime currentDateTime) {
			return Schedule.WILL_NOT_BE_EXECUTED_AGAIN;
		}
		
		@Override
		public String description() {
			return "Disabled schedule";
		}
	};
	
	public static Schedule withCronExpression(String cronExpression) {
		return CronSchedule.parseQuartzCron(cronExpression);
	}
	
	public static Schedule atFixedTimeFromMondayToFriday(LocalTime localTime) {
		String cronExpression = buildCronExpression(localTime, "MON-FRI");
		return ScheduleFactory.withCronExpression(cronExpression);
	}
	
	public static Schedule atFixedTimeEveryDay(LocalTime localTime) {
		String cronExpression = buildCronExpression(localTime, "MON-SUN");
		return ScheduleFactory.withCronExpression(cronExpression);
	}
	
	public static Schedule atFixedTimeOnWeekDays(LocalTime localTime, Weekdays... weekdays) {
		return ScheduleFactory.atFixedTimeOnWeekDays(localTime, List.of(weekdays));
	}
	
	public static Schedule atFixedTimeOnWeekDays(LocalTime localTime, List<Weekdays> weekdays) {
		return weekdays.isEmpty() ? 
						ScheduleFactory.atFixedTimeEveryDay(localTime)
						: ScheduleFactory.withCronExpression(buildCronExpression(localTime, buildCronDays(weekdays)));
	}
	
	private static String buildCronExpression(LocalTime localTime, String cronDays) {
		return String.format("0 %d %d ? * %s *", localTime.getMinute(), localTime.getHour(), cronDays);
	}
	
	private static String buildCronDays(List<Weekdays> weekDays) {
		return weekDays.stream().map(day -> StringUtils.substring(day.name(), 0, 3)).collect(Collectors.joining(","));
	}
}
