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
 * Created on: Jan 30, 2020
 *     Author: echfari
 */
package com.ericsson.utilities.reactivex;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.plugins.RxJavaPlugins;
import io.vertx.core.VertxOptions;
import io.vertx.reactivex.core.RxHelper;
import io.vertx.reactivex.core.Vertx;

/**
 * Creates a single Vertx instance according to given options
 */
public class VertxBuilder
{
    private static final Logger log = LoggerFactory.getLogger(VertxBuilder.class);
    private static final AtomicBoolean instanceCreated = new AtomicBoolean(false);
    private boolean rxSchedulers;
    private VertxOptions options;

    /**
     * Create a new Builder that can be used to build Vertx instances
     * 
     * @return The newyly created Vertx Instance
     */
    public static VertxBuilder newInstance()
    {
        return new VertxBuilder();
    }

    public VertxBuilder()
    {
        setOptions(new VertxOptions());
    }

    /**
     * Modify RX java schedulers to use Vertx pools The default is true
     * 
     * @param rxSchedulers
     * @return
     */
    public VertxBuilder modifyRxSchedulers(boolean rxSchedulers)
    {
        this.rxSchedulers = rxSchedulers;
        return this;
    }

    /**
     * Set Additional Vertx options
     * 
     * @param options The Additional Vertx options to set
     */
    public VertxBuilder setOptions(VertxOptions options)
    {
        this.options = options.setPreferNativeTransport(true); // Enable native transport ( requires netty native transport dependency)
        return this;
    }

    /**
     * Create a new Vertx instance. This method should only called once,as soon as
     * possible,preferably at the main thread. It is not allowed to create more than
     * one instances in the same JVM.
     * 
     * @throws IllegalStateException if a Vertx instance has already been created
     * 
     * @return The Unique Vertx instance
     */
    public Vertx build()
    {
        if (!instanceCreated.compareAndSet(false, true))
        {
            log.warn("Vertx instance already created, only one is preferred");
        }

        System.getProperties().setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");

        final var newVertx = Vertx.vertx(this.options);
        // Configure RX java to use VertX thread pools
        if (rxSchedulers)
        {
            RxJavaPlugins.setComputationSchedulerHandler(s -> RxHelper.scheduler(newVertx));
            RxJavaPlugins.setIoSchedulerHandler(s -> RxHelper.blockingScheduler(newVertx));
        }

        log.debug("Initialized Vert.x, native support enabled: {} , metrics enabled: {}", newVertx.isNativeTransportEnabled(), newVertx.isMetricsEnabled());
        if (!newVertx.isNativeTransportEnabled())
        {
            log.error("Vert.x is unable to use native transport, performance will suffer");
        }
        return newVertx;
    }
}
