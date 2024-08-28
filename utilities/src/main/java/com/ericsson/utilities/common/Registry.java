/**
 * COPYRIGHT ERICSSON GMBH 2018
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Nov 21, 2018
 *     Author: eedstl
 */

package com.ericsson.utilities.common;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;

/**
 * Represents a regularly updated list of two registers, current and previous.
 * The user, however, will see it as a single register. With every update the
 * current register is copied over the previous register, and a new register is
 * stored as current. When accessing the register, the current register is
 * searched first, then the previous, if needed.
 */
public class Registry<K, T>
{
    private static final Logger log = LoggerFactory.getLogger(Registry.class);

    private Map<K, T> curr;
    private Map<K, T> prev;
    private long delayInMillis;
    private final List<Consumer<T>> putHandlers;
    private final List<Consumer<T>> removeHandlers;
    private Disposable updater;
    private long lastUpdateInMillis;
    private final Lock readLock;
    private final Lock writeLock;

    public Registry(final long delayInMillis)
    {
        this(delayInMillis, Arrays.asList(), Arrays.asList());
    }

    @SafeVarargs
    public Registry(final long delayInMillis,
                    final Consumer<T>... removeHandlers)
    {
        this(delayInMillis, Arrays.asList(), Arrays.asList(removeHandlers));
    }

    public Registry(final long delayInMillis,
                    final List<Consumer<T>> putHandlers,
                    final List<Consumer<T>> removeHandlers)
    {
        this.curr = new HashMap<>();
        this.prev = new HashMap<>();
        this.delayInMillis = delayInMillis;
        this.putHandlers = putHandlers;
        this.removeHandlers = removeHandlers;
        this.updater = null;
        this.lastUpdateInMillis = 0l;

        final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
        this.readLock = rwLock.readLock();
        this.writeLock = rwLock.writeLock();
    }

    public void clear()
    {
        this.writeLock.lock();

        try
        {
            this.prev.clear();
            this.curr.clear();
        }
        finally
        {
            this.writeLock.unlock();
        }
    }

    public Set<Entry<K, T>> entrySet()
    {
        this.readLock.lock();

        try
        {
            final Set<Entry<K, T>> entries = new HashSet<>();

            entries.addAll(this.curr.entrySet());

            this.prev.entrySet().forEach(e ->
            {
                if (!this.curr.containsKey(e.getKey()))
                    entries.add(e);
            });

            return entries;
        }
        finally
        {
            this.readLock.unlock();
        }
    }

    public T get(final K key)
    {
        this.readLock.lock();

        try
        {
            T curr = this.curr.get(key);
            T prev = this.prev.get(key);

            return curr != null ? curr : prev;
        }
        finally
        {
            this.readLock.unlock();
        }
    }

    public long getUpdateDelayInMillis()
    {
        this.readLock.lock();

        try
        {
            return this.delayInMillis;
        }
        finally
        {
            this.readLock.unlock();
        }
    }

    public T put(final K key,
                 final T value)
    {
        return this.put(key, value, Object::equals);
    }

    public T put(final K key,
                 final T value,
                 final BiPredicate<T, T> comparator)
    {
        this.writeLock.lock();

        try
        {
            final T prevItem = this.prev.remove(key);
            final T currItem = this.curr.put(key, value);

            final T replacedItem = currItem != null ? currItem : prevItem;

            if (!this.removeHandlers.isEmpty() || !this.putHandlers.isEmpty())
            {
                if (replacedItem != null && !comparator.test(replacedItem, value))
                    this.removeHandlers.forEach(handler -> handler.accept(replacedItem));

                this.putHandlers.forEach(handler -> handler.accept(value));
            }

            return replacedItem;
        }
        finally
        {
            this.writeLock.unlock();
        }
    }

    public T remove(final K key)
    {
        this.writeLock.lock();

        try
        {
            final T currItem = this.curr.remove(key);
            final T prevItem = this.prev.remove(key);

            final T removedItem = currItem != null ? currItem : prevItem;

            if (removedItem != null)
                this.removeHandlers.forEach(handler -> handler.accept(removedItem));

            return removedItem;
        }
        finally
        {
            this.writeLock.unlock();
        }
    }

    public Registry<K, T> setUpdateDelayInMillis(final long delayInMillis)
    {
        this.writeLock.lock();

        try
        {
            this.delayInMillis = delayInMillis;
            return this;
        }
        finally
        {
            this.writeLock.unlock();
        }
    }

    public Completable start()
    {
        return Completable.fromAction(() ->
        {
            if (this.updater == null)
            {
                this.updater = this.update()
                                   .timeout(1500, TimeUnit.MILLISECONDS)
                                   .doOnError(e -> log.error("Updating registry failed: {}", e.toString()))
                                   .onErrorReturn(e -> true)
                                   .repeatWhen(handler -> handler.delay(1, TimeUnit.SECONDS))
                                   .ignoreElements()
                                   .doOnSubscribe(d -> log.info("Started updating registry."))
                                   .subscribe(() -> log.info("Stopped updating registry."),
                                              t -> log.error("Stopped updating registry. Cause: {}", t.toString()));
            }
        });
    }

    public Completable stop()
    {
        return Completable.fromAction(() ->
        {
            if (this.updater != null)
            {
                this.updater.dispose();
                this.updater = null;
            }
        });
    }

    public String toString()
    {
        this.readLock.lock();

        try
        {
            StringBuilder b = new StringBuilder();
            b.append("{ ");
            b.append("curr=").append(this.curr.toString());
            b.append(", prev=").append(this.prev.toString());
            b.append(" }");
            return b.toString();
        }
        finally
        {
            this.readLock.unlock();
        }
    }

    private Single<Boolean> update()
    {
        return Single.just(true).doOnSuccess(b ->
        {
            this.writeLock.lock();

            try
            {
                final long now = System.currentTimeMillis();
                final boolean result = this.lastUpdateInMillis + this.getUpdateDelayInMillis() < now;

                if (result)
                    this.lastUpdateInMillis = now;
                else
                    return;

                log.debug("Updating registry.");

                if (!this.removeHandlers.isEmpty())
                {
                    // Call the remove handlers for each item that is not in curr,
                    // as these items will cease to exist in this registry.

                    this.prev.entrySet().forEach(entry ->
                    {
                        if (!this.curr.containsKey(entry.getKey()))
                            this.removeHandlers.forEach(handler -> handler.accept(entry.getValue()));
                    });
                }

                this.prev = this.curr;
                this.curr = new HashMap<>();
            }
            finally
            {
                this.writeLock.unlock();
            }
        });
    }
}
