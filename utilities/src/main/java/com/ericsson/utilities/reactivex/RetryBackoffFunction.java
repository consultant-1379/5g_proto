/**
 * COPYRIGHT ERICSSON GMBH 2022
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Nov 8, 2022
 *     Author: echaias
 */

package com.ericsson.utilities.reactivex;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.reactivestreams.Publisher;

import io.reactivex.Flowable;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

/**
 * Creates retry functions for use with Observable.retryWhen() This class is not
 * thread safe. The produced retry function is thread safe
 * 
 * Retries happen in an exponential backoff manner. This class is based on the
 * prexisting RetryFunction
 */
public class RetryBackoffFunction
{
    private final Random random = new Random();
    private int jitterMillis = 0;
    private long maxBackoff = 0;
    private long maxAttempts = 0;
    private long minBackoff = 0;
    private Predicate<Throwable> predicate = t -> true;
    private BiConsumer<Throwable, Integer> action = (t,
                                                     r) ->
    {
    };

    public static Function<Flowable<Throwable>, Publisher<Long>> noRetries()
    {
        return new RetryFunction().create();
    }

    /**
     * 
     * @param retries The number of retries. Negative number means infinite retries.
     * @return
     */

    public RetryBackoffFunction withMaxBackoff(long maxBackoff)
    {
        this.maxBackoff = maxBackoff;
        return this;
    }

    public RetryBackoffFunction withBackoff(long maxAttempts,
                                            long minBackoff)
    {
        this.maxAttempts = maxAttempts;
        this.minBackoff = minBackoff;
        return this;
    }

    /**
     * 
     * @param jitterMillis The jitter in milliseconds or zero for no jitter
     */
    public RetryBackoffFunction withJitter(int jitterMillis)
    {
        this.jitterMillis = jitterMillis;
        return this;
    }

    /**
     * 
     * @param action An optional action that shall be trigger prior to each retry
     */
    public RetryBackoffFunction withRetryAction(BiConsumer<Throwable, Integer> action)
    {
        Objects.requireNonNull(action);
        this.action = action;
        return this;
    }

    /**
     * 
     * @param predicate Retries shall take place only if the predicate returns true
     */
    public RetryBackoffFunction withPredicate(Predicate<Throwable> predicate)
    {
        this.predicate = predicate;
        return this;
    }

    /**
     * Create a retry function taking into account the retry configuration
     * 
     * @return A new retry function
     */
    public Function<Flowable<Throwable>, Publisher<Long>> create()
    {
        final var retr = this.maxAttempts;
        final var jit = this.jitterMillis;
        final var act = this.action;
        final var pred = this.predicate;

        final var maxBo = this.maxBackoff;
        final var minBo = this.minBackoff;

        return new Function<Flowable<Throwable>, Publisher<Long>>()
        {
            @Override
            public Publisher<Long> apply(Flowable<Throwable> errors) throws Exception
            {
                final var retryCounter = new AtomicInteger();
                return errors.flatMap(e ->
                {
                    if (pred.test(e))
                    {
                        final var cnt = retryCounter.getAndIncrement();

                        if (retr < 0 || cnt < retr)
                        {
                            // create random jitter if needed
                            final var jitter = jit > 0 ? random.nextInt(jit) : 0;

                            // Execute the optional retryAction
                            act.accept(e, cnt);

                            // exponential delay, not larger than maxBackoff and not smaller than minBackoff
                            var delay = Math.max(Math.min((long) (Math.pow(2, cnt) * 1000), maxBo), minBo);
                            // Delay, taking into account the jitter

                            return Flowable.timer(delay + jitter, TimeUnit.MILLISECONDS);
                        }
                        else
                        {
                            // Retry count exceeded
                            return Flowable.error(e);
                        }
                    }
                    else
                    {
                        // Non specific exception that should not be retried
                        return Flowable.error(e);

                    }
                });
            }
        };
    }
}
