/**
 * COPYRIGHT ERICSSON GMBH 2019
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jun 16, 2021
 *     Author: echfari
 */
package com.ericsson.sc.util;

import static org.testng.Assert.assertTrue;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import io.reactivex.Observable;

public class LogLimitterTest
{
    enum LogLabels
    {
        A,
        B,
        C
    }

    Logger logger = LoggerFactory.getLogger(LogLimitterTest.class);

    @Test
    public void logLimitterTest()
    {
        AtomicLong cnt = new AtomicLong(0);
        final var safeLog = LogLimitter.create(LogLabels.class, logger);
        Observable.interval(1, TimeUnit.MILLISECONDS).doOnNext(tick ->
        {
            safeLog.log(LogLabels.A, l ->
            {
                cnt.incrementAndGet();
                l.info("logline {}", tick);
            });
        }

        ).take(3000).blockingSubscribe();

        final var res = cnt.get();
        final var expected = 6;
        // Expected value is 6
        assertTrue(res < 8 && res > 4, "Expected about " + expected + " log lines, got " + res);
        ;

    }

}
