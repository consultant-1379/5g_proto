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
 * Created on: Oct 16, 2018
 *     Author: xchrfar
 */
package com.ericsson.utilities.reactivex;

import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;

import io.reactivex.plugins.RxJavaPlugins;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.micrometer.Label;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;
import io.vertx.reactivex.core.RxHelper;
import io.vertx.reactivex.core.Vertx;

/**
 * Singleton that holds a global Vertx instance.The instance is configured to
 * use netty native transport.
 * <p>
 * Upon construction, global side-effects take place:
 * <li>Vertx is configured to use SLF4J logger</li>
 * <li>RxJava is configured to use VertX threadpools</li>
 * <li>Jackson instance that is used internally by Vertx to parse JSON is
 * configured with sane default options</li>
 */
public class VertxInstance
{
    private static final Logger log = LoggerFactory.getLogger(VertxInstance.class);
    private static final VertxInstance instance = new VertxInstance();

    private final Vertx vertx;
    private final io.vertx.core.Vertx coreVertx;
    private final VertxOptions options;

    private VertxInstance()
    {

        // Configure Vertx to use SLF4J for logging
        System.getProperties().setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");

        // Configure Vert json instance
        final var vertxOm = DatabindCodec.mapper();
        final var oldJacksonConfig = vertxOm.getDeserializationConfig();
        // DND-29137: Deserialization of huge JSON numbers as BigIntegers might take too
        // much time.
        // To protect against DoS attacks, numbers are deserialized as longs by default.
        vertxOm.setConfig(oldJacksonConfig.with(DeserializationFeature.USE_LONG_FOR_INTS));

        this.options = new VertxOptions() //
                                         .setMetricsOptions(new MicrometerMetricsOptions().setJvmMetricsEnabled(true)
                                                                                          .setLabels(EnumSet.of(Label.LOCAL, // local endpoint label is disabled
                                                                                                                             // by default
                                                                                                                Label.HTTP_ROUTE, // default label
                                                                                                                Label.HTTP_METHOD, // default label
                                                                                                                Label.HTTP_CODE, // default label
                                                                                                                Label.POOL_TYPE, // default label
                                                                                                                Label.EB_SIDE) // default label
                                                                                          )
                                                                                          .setPrometheusOptions(new VertxPrometheusOptions().setEnabled(true))
                                                                                          .setEnabled(true))
                                         .setWarningExceptionTime(TimeUnit.SECONDS.toNanos(1)) // Thread blocked warning always includes stack trace
                                         .setPreferNativeTransport(true); // Enable native transport (requires netty native transport dependency)
        this.vertx = Vertx.vertx(options);
        this.coreVertx = io.vertx.core.Vertx.vertx(options);
        // Configure RX java to use VertX thread pools
        RxJavaPlugins.setComputationSchedulerHandler(s -> RxHelper.scheduler(vertx));
        RxJavaPlugins.setIoSchedulerHandler(s -> RxHelper.blockingScheduler(vertx));

        log.debug("Initialized Vert.x, native support enabled: {} , metrics enabled: {}", this.vertx.isNativeTransportEnabled(), this.vertx.isMetricsEnabled());
        if (!this.vertx.isNativeTransportEnabled())
        {
            log.error("Vertx is unable to use native transport, performance will suffer");
        }
    }

    /**
     * 
     * @return The unique Vertx instance.
     * 
     */
    public static Vertx get()
    {
        return instance.vertx;
    }

    public static io.vertx.core.Vertx getCore()
    {
        return instance.coreVertx;
    }

    /**
     * 
     * @return The Vertx Options used in the Vertx instance.
     */
    public static VertxOptions getOptions()
    {
        return instance.options;
    }

}
