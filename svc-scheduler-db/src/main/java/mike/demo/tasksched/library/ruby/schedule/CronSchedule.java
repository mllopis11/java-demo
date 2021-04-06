package mike.demo.tasksched.library.ruby.schedule;

import java.time.ZonedDateTime;
import java.util.Locale;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

/**
 * A {@link Schedule} based on a <a href="https://en.wikipedia.org/wiki/Cron#CRON_expression">
 * cron expression</a>.<br/>
 * <br/>
 * This class depends on <a href="https://github.com/jmrozanec/cron-utils">cron-utils</a>,
 * so this dependency have to be in the classpath in order to be able to use {@link CronSchedule}.
 * Since cron-utils is marked as optional, it has to be explicitly referenced in the
 * project dependency configuration (pom.xml, build.gradle, build.sbt etc.).
 */
class CronSchedule implements Schedule {

	private static final CronParser QUARTZ_CRON_PARSER = new CronParser(
		CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ)
	);

	private static final CronDescriptor ENGLISH_DESCRIPTOR = CronDescriptor.instance(Locale.ENGLISH);

	private final ExecutionTime cronExpression;
	private final String description;

	private CronSchedule(Cron cronExpression) {
		this.cronExpression = ExecutionTime.forCron(cronExpression);
		this.description = ENGLISH_DESCRIPTOR.describe(cronExpression);
	}

	@Override
	public ZonedDateTime nextExecutionDateTime(ZonedDateTime currentDateTime) {
		return cronExpression.timeToNextExecution(currentDateTime)
					.map(currentDateTime::plus)
					.orElseGet(() -> WILL_NOT_BE_EXECUTED_AGAIN);
	}

	@Override
	public String description() {
		return description;
	}
	
	@Override
	public String toString() {
		return description;
	}

	/**
	 * Create a {@link Schedule} from a cron expression based on the Quartz format,
	 * e.g. 0 * * * * ? * for each minute.
	 */
	public static CronSchedule parseQuartzCron(String cronExpression) {
		return new CronSchedule(QUARTZ_CRON_PARSER.parse(cronExpression));
	}
}
