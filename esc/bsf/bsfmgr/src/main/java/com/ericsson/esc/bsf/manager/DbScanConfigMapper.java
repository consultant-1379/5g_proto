package com.ericsson.esc.bsf.manager;

import java.text.ParseException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cronutils.mapper.CronMapper;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import com.ericsson.sc.bsf.model.BindingDatabaseScan;
import com.ericsson.utilities.common.EnvVars;
import io.reactivex.Flowable;

public class DbScanConfigMapper
{
    private static final long AUTO_SCAN_INTERVAL_MINUTES = EnvVars.get("AUTO_SCAN_INTERVAL_MINUTES") != null ? Long.parseLong(EnvVars.get("AUTO_SCAN_INTERVAL_MINUTES"))
                                                                                                             : 30L;

    // This is due to a sonarQube warning
    private DbScanConfigMapper()
    {
        throw new IllegalStateException();
    }

    private static final Logger log = LoggerFactory.getLogger(DbScanConfigMapper.class);

    static Flowable<Boolean> generateScanCommands(Flowable<BindingDatabaseScan> config)
    {

        return config.switchMap(bindingDbScan ->
        {

            switch (bindingDbScan.getConfiguration())
            {
                case AUTO:
                    return autoTriggering.startWithArray(Boolean.FALSE, Boolean.TRUE);
                case DISABLED:
                    return neverTriggering.startWith(Boolean.FALSE);
                case SCHEDULED:
                    final var cron = toValidQuartz(bindingDbScan.getSchedule());
                    return cron.isEmpty() ? neverTriggering : CronScheduler.create(cron.get()).map(tick -> Boolean.TRUE).startWith(Boolean.FALSE);
                default:
                    return neverTriggering.startWith(Boolean.FALSE);
            }
        });

    }

    private static Flowable<Boolean> autoTriggering = Flowable.interval(AUTO_SCAN_INTERVAL_MINUTES, TimeUnit.MINUTES).map(tick -> Boolean.TRUE);
    private static Flowable<Boolean> neverTriggering = Flowable.never();

    public static Optional<CronExpression> toValidQuartz(String cronString)
    {

        final var cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
        final var unixCronParser = new CronParser(cronDefinition);
        CronExpression cronExp = null;
        final var cronMapper = CronMapper.fromUnixToQuartz();

        if (cronString == null)
        {
            return Optional.empty();
        }

        try
        {
            // Check if user gave Quartz cron expression. If this is true return an Optional
            // of this cron expression
            if (CronExpression.isValidExpression(cronString))
            {
                return Optional.of(new CronExpression(cronString));
            }
            // If the input is not a valid Quartz cron expression try to parse it to Unix
            // cron.
            // Then map it to Quartz cron and create a Quartz cron expression.
            else
            {
                final var cron = unixCronParser.parse(cronString);
                final var quartzCronExp = cronMapper.map(cron).asString();
                cronExp = new CronExpression(quartzCronExp);
            }
        }
        catch (IllegalArgumentException | ParseException e)
        {
            log.error("Invalid cron expression '{}'", cronString, e);
        }
        // If everything went well cronExp is a valid Quartz expression an Optional of
        // this obj will be returned.
        // Otherwise this variable remains null and Optional empty will be returned.
        return Optional.ofNullable(cronExp);
    }

    public static boolean isValidCron(String cronString)
    {
        if (cronString == null)
        {
            log.debug("Error: Cron expression is null. Used expression: {}", cronString);
            return false;
        }

        if (CronExpression.isValidExpression(cronString))
        {
            return true;
        }
//              If the input is not a valid Quartz cron expression try to parse it to Unix cron.
//              Then map it to Quartz cron and create a Quartz cron expression. 
        else
        {
            try
            {
                final var cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
                final var unixCronParser = new CronParser(cronDefinition);
                final var cronMapper = CronMapper.fromUnixToQuartz();
                final var cron = unixCronParser.parse(cronString);
                final var quartzCronExp = cronMapper.map(cron).asString();

                return CronExpression.isValidExpression(quartzCronExp);
            }
            catch (IllegalArgumentException | NullPointerException e)
            {
                log.debug("Error: Cron expression is not valid. Used expression: {}", cronString);
                return false;
            }
        }
    }
}
