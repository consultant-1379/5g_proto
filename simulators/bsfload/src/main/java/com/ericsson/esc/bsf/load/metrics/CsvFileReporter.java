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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.MetricRegistry;
import com.ericsson.esc.bsf.load.configuration.MetricsConfiguration;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.dropwizard.DropwizardConfig;
import io.micrometer.core.instrument.dropwizard.DropwizardMeterRegistry;
import io.micrometer.core.instrument.util.HierarchicalNameMapper;
import io.reactivex.Completable;

/**
 * Exports the metrics in CSV format.
 */
public class CsvFileReporter implements MetricsReporter
{
    private static final Logger log = LoggerFactory.getLogger(CsvFileReporter.class);

    private final CsvReporter reporter;
    private final MetricsConfiguration configuration;
    private final MeterRegistry registry;
    private final Path metricsDir;
    private final UUID runId;

    public CsvFileReporter(MetricsConfiguration configuration,
                           UUID runId)
    {
        this.configuration = configuration;
        this.runId = runId;

        // Create a DropWizard registry to plug-in the CSV reporter.
        this.registry = createDropWizardRegistry();
        // Deny server metrics, only client-related are of interest.
        this.registry.config().meterFilter(MeterFilter.denyNameStartsWith("vertx.http.server"));

        try
        {
            // Create the metrics directory to store the CSV files.
            final var completePath = Path.of(configuration.getCsvMetricsDirectory(), configuration.getCsvNamePrefix() + "-" + runId);
            this.metricsDir = Files.createDirectories(completePath);

            // Configure the CSV reporter.
            this.reporter = CsvReporter.forRegistry(((DropwizardMeterRegistry) this.registry).getDropwizardRegistry())
                                       .formatFor(Locale.US)
                                       .convertRatesTo(TimeUnit.valueOf(configuration.getCsvConvertRatesTo()))
                                       .convertDurationsTo(TimeUnit.valueOf(configuration.getCsvConvertDurationsTo()))
                                       .build(this.metricsDir.toFile());
        }
        catch (IOException e)
        {
            throw new MetricsException("Unable to create the CSV file to export the metrics.", e);
        }
    }

    @Override
    public MeterRegistry getRegistry()
    {
        return this.registry;
    }

    @Override
    public Completable start()
    {
        return Completable.fromAction(() ->
        {
            log.info("Starting CSV_FILE metrics exporter for run: {}. The metrics will be exported in {}.", this.runId, this.metricsDir);
            reporter.start(this.configuration.getCsvPollInterval(), TimeUnit.SECONDS);
        });
    }

    @Override
    public Completable stop()
    {
        return Completable.fromAction(() ->
        {
            if (this.reporter != null)
            {
                this.reporter.stop();
            }
            log.info("Stopping CSV_FILE metrics exporter for run: {}. The exported metrics are located in {}", this.runId, this.metricsDir);
        });
    }

    /**
     * Create a DropWizard specific MeterRegistry to plug-in the CSV reporter
     * implementation.
     * 
     * @return DropwizardMeterRegistry A DW-adapted MeterRegistry.
     */
    private DropwizardMeterRegistry createDropWizardRegistry()
    {
        DropwizardConfig reporterConfig = new DropwizardConfig()
        {
            @Override
            public String prefix()
            {
                return "csv";
            }

            @Override
            public String get(String key)
            {
                return null;
            }
        };

        return new DropwizardMeterRegistry(reporterConfig, new MetricRegistry(), HierarchicalNameMapper.DEFAULT, Clock.SYSTEM)
        {
            @Override
            protected Double nullGaugeValue()
            {
                return null;
            }
        };
    }
}
