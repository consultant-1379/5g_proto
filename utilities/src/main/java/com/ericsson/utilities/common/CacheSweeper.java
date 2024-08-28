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
 * Created on: Sep 17, 2021
 *     Author: eedstl
 */

package com.ericsson.utilities.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CacheSweeper<K, V>
{
    private static final Logger log = LoggerFactory.getLogger(CacheSweeper.class);

    @JsonIgnore
    final String mapName;

    @JsonIgnore
    private final Map<K, V> map;

    @JsonIgnore
    private final int sizeMax;

    @JsonProperty("sweepItemsMax")
    private final int sweepItemsMax;

    @JsonProperty("sweepPeriodMillis")
    private final int sweepPeriodMillis;

    @JsonIgnore
    private final Object iteratorLock = new Object();

    @JsonIgnore
    private final List<Disposable> disposables = new ArrayList<>();

    @JsonIgnore
    private final java.util.function.Predicate<V> validator;

    @JsonIgnore
    private final java.util.function.BiConsumer<K, V> removeHandler;

    @JsonIgnore
    private long lastTimeRoundCompletedMillis = 0;

    @JsonIgnore
    private Iterator<Entry<K, V>> iterator = null;

    public CacheSweeper(final String name,
                        final Map<K, V> map,
                        int sizeMax,
                        int sweepItemsMax,
                        int sweepPeriodMillis,
                        final java.util.function.Predicate<V> validator)
    {
        this(name,
             map,
             sizeMax,
             sweepItemsMax,
             sweepPeriodMillis,
             validator,
             (k,
              v) ->
             {
             });
    }

    public CacheSweeper(final String mapName,
                        final Map<K, V> map,
                        int sizeMax,
                        int sweepItemsMax,
                        int sweepPeriodMillis,
                        final java.util.function.Predicate<V> validator,
                        final java.util.function.BiConsumer<K, V> removeHandler)
    {
        this.mapName = mapName;
        this.map = map;
        this.sizeMax = sizeMax;
        this.sweepItemsMax = sweepItemsMax;
        this.sweepPeriodMillis = sweepPeriodMillis;
        this.validator = validator;
        this.removeHandler = removeHandler;
    }

    @JsonIgnore
    public void reset()
    {
        synchronized (this.iteratorLock)
        {
            this.iterator = this.map.entrySet().iterator(); // Resume from the beginning.
        }
    }

    public Completable start()
    {
        return Completable.fromAction(() ->
        {
            if (this.disposables.isEmpty())
            {
                this.disposables.add(Single.fromCallable(this::sweep).subscribeOn(Schedulers.io())
//                                            .doOnSubscribe(e -> log.debug("Sweeping {}.", this.mapName.toLowerCase()))
                                           .doOnError(e -> log.debug("Sweeping " + this.mapName.toLowerCase() + " failed.", e))
                                           .onErrorReturnItem(0l)
                                           .repeatWhen(handler -> handler.delay(this.sweepPeriodMillis, TimeUnit.MILLISECONDS)) // 100 Hz
                                           .ignoreElements()
                                           .doOnSubscribe(d -> log.info("Started sweeping {}.", this.mapName.toLowerCase()))
                                           .doOnDispose(() -> log.info("Stopped sweeping {}.", this.mapName.toLowerCase()))
                                           .subscribe(() ->
                                           {
                                           }, t -> log.error("Stopped sweeping " + this.mapName.toLowerCase() + ".", t)));
            }
        });
    }

    public Completable stop()
    {
        return Completable.fromAction(() ->
        {
            this.disposables.forEach(Disposable::dispose);
            this.disposables.clear();
        });
    }

    private long sweep()
    {
        final long size = this.map.size();

        if (size == 0)
            return 0;

        final long nowMillis = System.currentTimeMillis();

        if (nowMillis - this.lastTimeRoundCompletedMillis < 1000)
            return 0;

        final long overrun = Math.max(0, size - this.sizeMax);

        boolean hasNext = false;

        synchronized (this.iteratorLock)
        {
            if (this.iterator == null || !this.iterator.hasNext())
            {
                this.iterator = this.map.entrySet().iterator(); // Resume from the beginning.
                this.lastTimeRoundCompletedMillis = nowMillis;
            }

            hasNext = this.iterator.hasNext();
        }

        long numIterations = Math.min(this.sweepItemsMax, overrun > 0 ? overrun : size);

        while (numIterations-- > 0 && hasNext)
        {
            synchronized (this.iteratorLock)
            {
                hasNext = this.iterator.hasNext();

                if (hasNext)
                {
                    final Entry<K, V> next = this.iterator.next();

                    if (overrun > 0 || !this.validator.test(next.getValue()))
                    {
                        this.map.remove(next.getKey());
                        this.removeHandler.accept(next.getKey(), next.getValue());
                    }

                    if (!this.iterator.hasNext())
                    {
                        this.iterator = this.map.entrySet().iterator(); // Resume from the beginning.
                        this.lastTimeRoundCompletedMillis = nowMillis;
                    }
                }
            }
        }

        return this.map.size();
    }
}
