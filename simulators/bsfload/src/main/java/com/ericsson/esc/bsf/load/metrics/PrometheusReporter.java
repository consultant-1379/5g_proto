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
 * Created on: Nov 4, 2021
 *     Author: emldpng
 */

package com.ericsson.esc.bsf.load.metrics;

import java.util.UUID;

import com.ericsson.esc.bsf.load.configuration.MetricsConfiguration;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.reactivex.Completable;

/**
 * Exports the Prometheus metrics.
 */
public class PrometheusReporter implements MetricsReporter
{
    private final MeterRegistry registry;
    private final MetricsHandler metricsHandler;

    public PrometheusReporter(MetricsConfiguration configuration,
                              UUID runId,
                              MetricsHandler metricsHandler)
    {
        this.metricsHandler = metricsHandler;

        // Create and configure the Prometheus metrics registry.
        this.registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

        // 1. Deny server metrics, only client-related are of interest.
        // 2. Add filter to enable percentiles and buckets if needed.
        // 3. Add the runId label to all metrics.
        this.registry.config()
                     .meterFilter(MeterFilter.denyNameStartsWith("vertx.http.server"))
                     .meterFilter(enablePercentilesFilter(configuration))
                     .commonTags("run_id", runId.toString());
    }

    @Override
    public MeterRegistry getRegistry()
    {
        return this.registry;
    }

    @Override
    public Completable start()
    {
        return this.metricsHandler.setMetricsHandler(ctx ->
        {
            String response = ((PrometheusMeterRegistry) this.registry).scrape();
            ctx.response().end(response);
        });
    }

    @Override
    public Completable stop()
    {
        return this.metricsHandler.resetMetricsHandler();
    }

    /**
     * Creates a MeterFilter that can enable the calculation of percentiles locally
     * for specific metrics. Additionally, it can publish percentile histogram
     * buckets for server-side percentile calculation. This filter is configured
     * according to the provided metrics configuration.
     * 
     * @param configuration The metrics configuration.
     * @return MeterFilter A filter that applies configuration changes to metrics.
     */
    private MeterFilter enablePercentilesFilter(MetricsConfiguration configuration)
    {
        return new MeterFilter()
        {
            @Override
            public DistributionStatisticConfig configure(Meter.Id id,
                                                         DistributionStatisticConfig config)
            {
                var dscBuilder = DistributionStatisticConfig.builder() //
                                                            .bufferLength(2)
                                                            .percentilePrecision(configuration.getPmPercentilePrecision());

                if (configuration.getPmEnablePercentiles().booleanValue() && configuration.getPmPercentileMetrics().contains(id.getName()))
                {
                    var percentiles = configuration.getPmTargetPercentiles() //
                                                   .stream()
                                                   .map(Double::parseDouble)
                                                   .mapToDouble(i -> i)
                                                   .toArray();
                    dscBuilder.percentiles(percentiles);
                }

                if (configuration.getPmEnableHistogramBuckets().booleanValue() && configuration.getPmHistogramBucketsMetrics().contains(id.getName()))
                {
                    dscBuilder.percentilesHistogram(true);
                }

                return config.merge(dscBuilder.build());
            }
        };
    }
}
