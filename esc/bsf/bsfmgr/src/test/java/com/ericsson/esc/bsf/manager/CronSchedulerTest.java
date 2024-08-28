package com.ericsson.esc.bsf.manager;

import static org.testng.Assert.assertEquals;

import java.text.ParseException;

import org.quartz.CronExpression;
import org.testng.annotations.Test;

public class CronSchedulerTest
{

    @Test(groups = "functest")
    public void checkCreateWithValidCron() throws ParseException
    {
        var period = new CronExpression("*/1 * * * * ?");
        var res = CronScheduler.create(period).take(3).toList().blockingGet();

//        Each tick emitted after 1s
        assertEquals(res.get(2).toInstant().getEpochSecond() - res.get(1).toInstant().getEpochSecond(), 1);
        assertEquals(res.get(1).toInstant().getEpochSecond() - res.get(0).toInstant().getEpochSecond(), 1);
    }

}
