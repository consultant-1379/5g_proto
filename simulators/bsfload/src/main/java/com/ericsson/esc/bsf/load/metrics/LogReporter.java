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
 * Created on: Apr 27, 2022
 *     Author: emldpng
 */

package com.ericsson.esc.bsf.load.metrics;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.ConsoleReporter;
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
 * Exports metrics in a log file.
 */
public class LogReporter implements MetricsReporter
{
    private static final Logger log = LoggerFactory.getLogger(LogReporter.class);

    private final ConsoleReporter reporter;
    private final MetricsConfiguration configuration;
    private final MeterRegistry registry;
    private final Path metricsLogFile;
    private final PrintStream outputStream;
    private final UUID runId;

    public LogReporter(MetricsConfiguration configuration,
                       UUID runId)
    {
        this.configuration = configuration;
        this.runId = runId;

        // Create a DropWizard registry to plug-in the Console reporter.
        this.registry = createDropWizardRegistry();
        // Deny server metrics, only client-related are of interest.
        this.registry.config().meterFilter(MeterFilter.denyNameStartsWith("vertx.http.server"));

        try
        {
            // Create the metrics directory to store the CSV files.
            final var metricsDir = Files.createDirectories(Path.of(configuration.getLogMetricsDirectory()));
            this.metricsLogFile = Path.of(metricsDir.toString(), configuration.getLogNamePrefix() + "-" + runId + ".log");
            this.outputStream = new PrintStream(metricsLogFile.toString(), StandardCharsets.UTF_8);

            // Configure the log reporter.
            this.reporter = ConsoleReporter.forRegistry(((DropwizardMeterRegistry) this.registry).getDropwizardRegistry())
                                           .convertRatesTo(TimeUnit.valueOf(configuration.getLogConvertRatesTo()))
                                           .convertDurationsTo(TimeUnit.valueOf(configuration.getLogConvertDurationsTo()))
                                           .outputTo(outputStream)
                                           .build();
        }
        catch (SecurityException e)
        {
            throw new MetricsException("Access to log file denied.", e);
        }
        catch (IOException e)
        {
            throw new MetricsException("Unable to create the log file to export the metrics.", e);
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
            log.info("Starting LOG_FILE metrics exporter for run: {}. The metrics will be exported in {}.", this.runId, this.metricsLogFile);
            reporter.start(configuration.getLogPollInterval(), TimeUnit.SECONDS);
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
            if (this.outputStream != null)
            {
                this.outputStream.close();
            }
            log.info("Stopping LOG_FILE metrics exporter for run: {}. The exported metrics are located in {}.", this.runId, this.metricsLogFile);
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
                return "log";
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