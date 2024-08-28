/**
 * COPYRIGHT ERICSSON GMBH 2021
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: May 5, 2021
 *     Author: eedstl
 */

package com.ericsson.utilities.logger;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Instances of this class just store the last time a log line was issued. This
 * is then used to control the frequency at which log lines may be written.
 * Useful if logging may take place on the traffic path (e.g. in error
 * conditions).
 * <p>
 * Example:
 * 
 * <pre>
 * LogThrottler logThrottler = new LogThrottler();
 * 
 * ...
 * 
 * void f()
 * {
 *     // Don't throttle in case the log level is DEBUG.
 *     boolean loggingIsDue = this.logThrottler.loggingIsDue(log.isDebugEnabled());
 *     
 *     if (loggingIsDue)
 *         log.info("Hello ");
 *     
 *     ...
 *     
 *     if (loggingIsDue)
 *         log.info("world!");     
 * }
 * </pre>
 */
public class LogThrottler
{
    private static final int LOGGING_INTERVAL_SECS_DEFAULT = 5;

    private static AtomicInteger loggingIntervalMillis = new AtomicInteger(1000 * LOGGING_INTERVAL_SECS_DEFAULT);

    /**
     * @return The logging interval [s] that is currently set.
     */
    public static int getLoggingIntervalSecs()
    {
        return loggingIntervalMillis.get() / 1000;
    }

    /**
     * Set the logging interval after which a log line is issued again.
     * <p>
     * If a value < 1 is passed, the default value
     * {@link #LOGGING_INTERVAL_SECS_DEFAULT} is taken instead.
     * 
     * @param intervalSecs The interval [s] to be set. If < 1,
     *                     {@link #LOGGING_INTERVAL_SECS_DEFAULT} is taken instead.
     */
    public static void setLoggingIntervalSecs(final int intervalSecs)
    {
        loggingIntervalMillis.set(1000 * (intervalSecs < 1 ? LOGGING_INTERVAL_SECS_DEFAULT : intervalSecs));
    }

    private Instant lastLogTime;

    public LogThrottler()
    {
        this.lastLogTime = Instant.now().minusMillis(1l + loggingIntervalMillis.get());
    }

    /**
     * Convenience method for calling {@link #loggingIsDue(boolean)} with argument
     * {@code false}.
     */
    public synchronized boolean loggingIsDue()
    {
        return this.loggingIsDue(false);
    }

    /**
     * Determines if the logging interval has passed since this method was called
     * last. If so, a new logging interval is started.
     * 
     * @param overrule If true, the logging is due immediately.
     * @return True if the logging is due, false otherwise.
     */
    public synchronized boolean loggingIsDue(final boolean overrule)
    {
        final boolean loggingIsDue = Instant.now().isAfter(this.lastLogTime.plusMillis(loggingIntervalMillis.get())) || overrule;

        if (loggingIsDue)
            this.lastLogTime = Instant.now();

        return loggingIsDue;
    }
}
