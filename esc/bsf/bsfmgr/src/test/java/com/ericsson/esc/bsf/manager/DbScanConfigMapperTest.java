package com.ericsson.esc.bsf.manager;

import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ericsson.sc.bsf.model.BindingDatabaseScan;
import com.ericsson.sc.bsf.model.BindingDatabaseScan.Configuration;

import io.reactivex.Flowable;

public class DbScanConfigMapperTest
{
    private static final Logger log = LoggerFactory.getLogger(DbScanConfigMapperTest.class);

    @Test
    public void checkInvalidCron()
    {
        List<String> stringsToTest = Arrays.asList("*****", "+wqdf*", "", " ", "* * * * * ? ?");

        stringsToTest.forEach(stringToTest -> assertEquals(DbScanConfigMapper.toValidQuartz(stringToTest), Optional.empty()));

    }

    @Test
    public void checkQuartzIput()
    {
        var input = "0 * * * * ? *";

        var actualres = DbScanConfigMapper.toValidQuartz(input);

        assertTrue("Result is Optional Empty, expected to be present.", actualres.isPresent());
        assertEquals(actualres.get().getCronExpression(), input);
    }

    @Test
    public void checkNullIput()
    {
        String input = null;

        var actualres = DbScanConfigMapper.toValidQuartz(input);

        assertTrue("Result is Optional present, expected to be Empty.", actualres.isEmpty());

    }

    @Test
    public void checkValidCron() throws ParseException
    {
        List<String> stringsToTest = Arrays.asList("* * * * *", "*/5 * * * *", "0 */50 * * * ? *");
        List<String> expectedStrings = Arrays.asList("0 * * * * ? *", "0 */5 * * * ? *", "0 */50 * * * ? *");

        var actualStrings = stringsToTest.stream()
                                         .map(DbScanConfigMapper::toValidQuartz)
                                         .map(Optional::get)
                                         .map(CronExpression::getCronExpression)
                                         .collect(Collectors.toList());

        assertEquals(expectedStrings, actualStrings);

    }

    @Test
    public void checkCommandGenerator()
    {
        var cfg = new BindingDatabaseScan().withConfiguration(Configuration.AUTO);
        var expectedCommands = Arrays.asList(false, true);
        var actualCommands = DbScanConfigMapper.generateScanCommands(Flowable.just(cfg)).take(2).toList().blockingGet();

        assertEquals(actualCommands, expectedCommands);

        cfg = new BindingDatabaseScan().withConfiguration(Configuration.DISABLED);
        expectedCommands = Arrays.asList(false);
        actualCommands = DbScanConfigMapper.generateScanCommands(Flowable.just(cfg)).take(1).toList().blockingGet();

        assertEquals(actualCommands, expectedCommands);
    }

    @Test
    public void checkCommandGeneratorWithChangableConfig()
    {
        var cfg1 = new BindingDatabaseScan().withConfiguration(Configuration.AUTO);
        var cfg2 = new BindingDatabaseScan().withConfiguration(Configuration.DISABLED);

        var expectedCommands = Arrays.asList(false, true, false);
        var cfg = Flowable.just(cfg1, cfg2);

        var commands = DbScanConfigMapper.generateScanCommands(cfg).doOnNext(cmd -> log.debug("cmd: {}", cmd)).take(3).toList().blockingGet();

        assertEquals(expectedCommands, commands);

    }

}
