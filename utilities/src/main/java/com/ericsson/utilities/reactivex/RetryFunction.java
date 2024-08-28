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
 * Created on: Feb 3, 2020
 *     Author: echfari
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
 */
public class RetryFunction
{
    private final Random random = new Random();
    private long retries = 0;
    private long delayMillis = 0;
    private int jitterMillis = 0;
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
    public RetryFunction withRetries(long retries)
    {
        this.retries = retries;
        return this;
    }

    /**
     * 
     * @param delayMillis The delay in milliseconds
     */
    public RetryFunction withDelay(long delayMillis)
    {
        this.delayMillis = delayMillis;
        return this;
    }

    /**
     * 
     * @param jitterMillis The jitter in milliseconds or zero for no jitter
     */
    public RetryFunction withJitter(int jitterMillis)
    {
        this.jitterMillis = jitterMillis;
        return this;
    }

    /**
     * 
     * @param action An optional action that shall be trigger prior to each retry
     */
    public RetryFunction withRetryAction(BiConsumer<Throwable, Integer> action)
    {
        Objects.requireNonNull(action);
        this.action = action;
        return this;
    }

    /**
     * 
     * @param predicate Retries shall take place only if the predicate returns true
     */
    public RetryFunction withPredicate(Predicate<Throwable> predicate)
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
        final var retr = this.retries;
        final var jit = this.jitterMillis;
        final var del = this.delayMillis;
        final var act = this.action;
        final var pred = this.predicate;

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

                            // Delay, taking into account the jitter
                            return Flowable.timer(del + jitter, TimeUnit.MILLISECONDS);
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
