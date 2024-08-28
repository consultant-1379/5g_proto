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

import java.util.EnumMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * An Rate limitted SLF4J logger. Default maximum rate is 2 logs/second.Exessive
 * logging is dropped
 */
public class LogLimitter
{
    public static final long DEFAULT_WINDOW_DURATION_MILLIS = 500; // 2 log events per second
    private final Subject<Consumer<Logger>> subject = PublishSubject.<Consumer<Logger>>create().toSerialized();
    private final long windowDuration;

    /**
     * Create a new rate limited logger.
     * 
     * @param logger The SLF4J logger to use for output
     * @param label  A label for this logger
     */
    public LogLimitter(Logger logger,
                       Object label)
    {
        this(logger, label, DEFAULT_WINDOW_DURATION_MILLIS);
    }

    /**
     * Create a new rate limited logger.
     * 
     * @param logger               The SLF4J logger to use for output
     * @param label                A label for this logger
     * @param windowDurationMillis The throttling window in milliseconds.500ms
     *                             corresponds to maximum 2 log events per second
     */
    public LogLimitter(Logger logger,
                       Object label,
                       long windowDurationMillis)
    {
        this.windowDuration = windowDurationMillis;
        subject.throttleFirst(this.windowDuration, TimeUnit.MILLISECONDS)
               .observeOn(Schedulers.io()) // Avoid logging on event loop thread for performance reasons
               .subscribe(logAction -> logAction.accept(logger), err -> logger.error("Rate limited logger {} terminated unexpectedly", label, err));
    }

    /**
     * Log via the rate limited logger
     * 
     * @param logAction A lambda function that uses the provided logger to log
     */
    public void log(Consumer<Logger> logAction)

    {
        subject.onNext(logAction);
    }

    public static <K extends Enum<K>> Loggers<K> create(Class<K> clazz,
                                                        Logger logger)
    {

        return create(clazz, logger, DEFAULT_WINDOW_DURATION_MILLIS);
    }

    public static <K extends Enum<K>> Loggers<K> create(Class<K> clazz,
                                                        Logger logger,
                                                        long windowDurationMillis)
    {
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(logger);

        EnumMap<K, LogLimitter> map = new EnumMap<>(clazz);
        final var keys = clazz.getEnumConstants();
        for (final var key : keys)
        {
            map.put(key, new LogLimitter(logger, key, windowDurationMillis));
        }
        return new Loggers<>(map);
    }

    public static class Loggers<K extends Enum<K>>
    {
        private EnumMap<K, LogLimitter> map;

        private Loggers(EnumMap<K, LogLimitter> map)
        {
            this.map = map;
        }

        public void log(K key,
                        Consumer<Logger> logAction)
        {
            map.get(key).log(logAction);
        }
    }

}
