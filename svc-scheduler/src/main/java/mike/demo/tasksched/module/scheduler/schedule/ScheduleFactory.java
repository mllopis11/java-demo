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
		String days = weekdays.stream().map(day -> StringUtils.substring(day.name(), 0, 3)).collect(Collectors.joining(","));
		String cronExpression = buildCronExpression(localTime, days);
		return ScheduleFactory.withCronExpression(cronExpression);
	}
	
	private static String buildCronExpression(LocalTime localTime, String weekDays) {
		return String.format("0 %d %d ? * %s *", localTime.getMinute(), localTime.getHour(), weekDays.isEmpty() ? "*" : weekDays);
	}
}
