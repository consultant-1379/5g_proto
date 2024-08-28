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
 * Created on: Oct 22, 2021
 *     Author: emldpng
 */

package com.ericsson.esc.bsf.load.metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.esc.bsf.load.configuration.MetricsConfiguration;
import com.ericsson.esc.bsf.load.configuration.MetricsConfiguration.ExportMetrics;
import com.ericsson.esc.bsf.load.server.BsfLoadParameters;
import com.ericsson.utilities.http.WebServer;

import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.vertx.core.Handler;
import io.vertx.core.metrics.MetricsOptions;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;

/**
 * The metrics server.
 * <ul>
 * <li>Management of metrics server's life cycle.</li>
 * <li>Configuration of the request handler of the metrics route.</li>
 * <li>Selection of the appropriate metrics reporter implementation.</li>
 * </ul>
 */
public class MetricsHandler
{
    private static final Logger log = LoggerFactory.getLogger(MetricsHandler.class);

    private final String hostAddress;
    private final boolean metricsEnabled;
    private final boolean metricsJvmEnabled;
    private final String metricsPath;
    private final int metricsPort;
    private final MetricsOptions metricsOptions;
    private final AtomicReference<CompositeMeterRegistry> registry;

    private AtomicReference<List<MetricsReporter>> reporters;
    private WebServer server;

    public MetricsHandler(String hostAddress,
                          BsfLoadParameters params)
    {
        this.hostAddress = hostAddress;
        this.metricsEnabled = params.getMetricsEnabled();
        this.metricsJvmEnabled = params.getMetricsJvmEnabled();
        this.metricsPath = params.getMetricsPath();
        this.metricsPort = params.getMetricsPort();
        this.reporters = new AtomicReference<>(new ArrayList<>());
        this.registry = new AtomicReference<>(new CompositeMeterRegistry());

        this.metricsOptions = new MicrometerMetricsOptions().setMicrometerRegistry(this.registry.get()) //
                                                            .setJvmMetricsEnabled(this.metricsJvmEnabled)
                                                            .setEnabled(this.metricsEnabled);

        if (!this.metricsEnabled)
        {
            log.info("Metrics are disabled. No metrics will be exported");
        }
    }

    /**
     * Metrics server must be initialized after the creation of the object, because
     * Vert.x instance is not ready yet.
     * 
     * @param vertx The Vert.x instance.
     */
    public void initServer(Vertx vertx)
    {
        this.server = WebServer.builder().withHost(hostAddress).withPort(metricsPort).build(vertx);
        this.server.configureRouter(router -> router.route(metricsPath).handler(defaultHandler()));
    }

    public Completable start()
    {
        return this.metricsEnabled ? this.server.startListener() : Completable.complete();
    }

    public Completable stop()
    {
        return this.metricsEnabled ? this.server.stopListener() : Completable.complete();
    }

    /**
     * Creates the metrics reporters according to the metrics configuration and
     * starts them.
     * 
     * Note: This implementation assumes that this can be called only for one
     * workload execution at a time. This is enforced in the BsfLoadService.
     * 
     * @param metricsConfiguration The metrics configuration.
     * @param runId                The uniqueId of this workload execution.
     * @return Completable that completes when the reporters are started.
     */
    public Completable startReporters(MetricsConfiguration metricsConfiguration,
                                      UUID runId)
    {
        if (this.metricsEnabled)
        {
            // Clear all registries to avoid leftovers from failed runs.
            clearRegistries();

            final var exportMetrics = metricsConfiguration.getExportMetrics();
            final var activeReporters = new ArrayList<MetricsReporter>();

            // Create reporters and add registries to the Composite registry.
            if (exportMetrics.contains(ExportMetrics.CSV_FILE))
            {
                final var csvFileReporter = new CsvFileReporter(metricsConfiguration, runId);
                activeReporters.add(csvFileReporter);
                this.registry.get().add(csvFileReporter.getRegistry());
            }
            if (exportMetrics.contains(ExportMetrics.LOG_FILE))
            {
                final var logReporter = new LogReporter(metricsConfiguration, runId);
                activeReporters.add(logReporter);
                this.registry.get().add(logReporter.getRegistry());
            }
            if (exportMetrics.contains(ExportMetrics.PROMETHEUS))
            {
                final var prometheusReporter = new PrometheusReporter(metricsConfiguration, runId, this);
                activeReporters.add(prometheusReporter);
                this.registry.get().add(prometheusReporter.getRegistry());
            }

            this.reporters.set(activeReporters);

            return Flowable.fromIterable(reporters.get()).concatMapCompletable(MetricsReporter::start);
        }
        else
        {
            return Completable.complete();
        }
    }

    /**
     * Stops the existing metrics reporters and clears the registries of the
     * reporters.
     * 
     * Note: This implementation assumes that this can be called only for one
     * Workload execution at a time. This is enforced in the BsfLoadService.
     * 
     * @return Completable that completes when the reporters are stopped.
     */
    public Completable stopReporters()
    {
        if (this.metricsEnabled)
        {

            return Flowable.fromIterable(reporters.get()) //
                           .concatMapCompletable(MetricsReporter::stop)
                           .andThen(Completable.fromAction(this::clearRegistries));
        }
        else
        {
            return Completable.complete();
        }
    }

    /**
     * This method returns the required Vert.x metrics options to configure and
     * enable the metrics. This method returns the metrics registry used by this
     * metrics reporter.
     * 
     * @return MetricsOptions The Vert.x metrics options.
     */
    public MetricsOptions getMetricsOptions()
    {
        return this.metricsOptions;
    }

    /**
     * Set a new request handler for the metrics route.
     * 
     * @param requestHandler The request handler to be applied.
     * @return Completable
     */
    public Completable setMetricsHandler(Handler<RoutingContext> requestHandler)
    {
        return Completable.fromAction(() ->
        {
            this.server.configureRouter(Router::clear);
            this.server.configureRouter(router -> router.route(this.metricsPath).handler(requestHandler));
        });
    }

    /**
     * Reset the request handler of the metrics route to the default one.
     * 
     * @return Completable
     */
    public Completable resetMetricsHandler()
    {
        return Completable.fromAction(() ->
        {
            this.server.configureRouter(Router::clear);
            this.server.configureRouter(router -> router.route(this.metricsPath).handler(defaultHandler()));
        });
    }

    /**
     * Responds with HTTP 200 OK and no content.
     * 
     * @return The default request handler.
     */
    private Handler<RoutingContext> defaultHandler()
    {
        return rc -> rc.response().putHeader("content-type", "text/html").end("");
    }

    private void clearRegistries()
    {
        reporters.get().stream().forEach(reporter -> registry.get().remove(reporter.getRegistry()));
        reporters.set(new ArrayList<>());
    }
}
