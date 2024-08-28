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
 * Created on: Aug 25, 2021
 *     Author: emldpng
 */

package com.ericsson.esc.bsf.load.server;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.esc.bsf.load.configuration.BsfLoadConfiguration;
import com.ericsson.esc.bsf.load.configuration.IPrange;
import com.ericsson.esc.bsf.load.configuration.MetricsConfiguration;
import com.ericsson.esc.bsf.load.configuration.MetricsConfiguration.ExportMetrics;
import com.ericsson.esc.bsf.load.configuration.TrafficSetConfiguration;
import com.ericsson.esc.bsf.load.core.WorkLoad;
import com.ericsson.esc.bsf.load.metrics.MetricsHandler;
import com.ericsson.utilities.reactivex.VertxBuilder;

import io.reactivex.Completable;
import io.vertx.core.VertxOptions;

/**
 * Circumvents the BSF load API server to execute traffic directly.
 * 
 * Modify the buildConfiguration method and start the traffic by running the
 * main method.
 */
public class LocalBsfLoad
{
    private static final Logger log = LoggerFactory.getLogger(LocalBsfLoad.class);

    // Fill the IPs with the correct value based on deployment e.g. 10.10.10.10
    private static final String startIPAddressRangeA = "";
    // Fill the IPs with the correct value based on deployment e.g. 20.10.10.10
    private static final String startIPAddressRangeB = "";
    // Fill the IPs with the correct value based on deployment e.g.10.63.136.179
    private static final String targetHostIPAddress = "";
    // Fill the traffic port
    private static final int trafficPort = 80;

    public static BsfLoadConfiguration buildConfiguration()
    {
        var ipRange = new IPrange();
        ipRange.setStartIP(startIPAddressRangeA);
        ipRange.setRange(10000);

        var setupLoadSet = new TrafficSetConfiguration.Builder().name("setup1")
                                                                .ipRange(ipRange)
                                                                .order(1)
                                                                .pcfId(null)
                                                                .recoveryTimeStr("2011-04-13T23:50:50.52Z")
                                                                .tps(100)
                                                                .type(TrafficSetConfiguration.LoadType.REGISTER)
                                                                .numRequests(1000L)
                                                                .build();

        var loadSet = new TrafficSetConfiguration.Builder().name("set1")
                                                           .ipRange(ipRange)
                                                           .order(1)
                                                           .tps(100)
                                                           .targetHost(targetHostIPAddress)
                                                           .type(TrafficSetConfiguration.LoadType.DISCOVERY)
                                                           .numRequests(1000L)
                                                           .build();

        // For local use enable only LOG_FILE metrics and change the default metrics
        // directory to "/tmp", since the default value is only viable for the
        // containerized version.
        var metrics = new MetricsConfiguration.Builder().exportMetrics(List.of(ExportMetrics.LOG_FILE))
                                                        .csvMetricsDirectory("/tmp")
                                                        .logMetricsDirectory("/tmp")
                                                        .build();

        return new BsfLoadConfiguration.Builder().metrics(metrics)
                                                 .duration(50L)
                                                 .targetPort(trafficPort)
                                                 .timeout(2000)
                                                 .setupTrafficMix(List.of(setupLoadSet))
                                                 .trafficMix(List.of(loadSet))
                                                 .build();
    }

    public static BsfLoadConfiguration buildFullTableScanConfiguration()
    {
        final var numberOfValid = 3000000L;
        final var numberOfStale = 1000000L;
        final var numberOfRotational = 250000L;

        var ipRangeA = new IPrange();
        ipRangeA.setStartIP(startIPAddressRangeA);
        ipRangeA.setRange(numberOfValid);

        var ipRangeB = new IPrange();
        ipRangeB.setStartIP(startIPAddressRangeB);
        ipRangeB.setRange(numberOfStale);

        var setupLoadSetValid = new TrafficSetConfiguration.Builder().name("setup1")
                                                                     .ipRange(ipRangeA)
                                                                     .order(1)
                                                                     .tps(2000)
                                                                     .type(TrafficSetConfiguration.LoadType.REGISTER)
                                                                     .numRequests(numberOfValid)
                                                                     .pcfId("4937cf76-0ccf-11ed-861d-0242ac120002")
                                                                     .recoveryTimeStr("2011-04-13T23:50:50.52Z")
                                                                     .build();

        var setupLoadSetStaleFromRt = new TrafficSetConfiguration.Builder().name("setup2")
                                                                           .ipRange(ipRangeB)
                                                                           .order(2)
                                                                           .tps(2000)
                                                                           .type(TrafficSetConfiguration.LoadType.REGISTER)
                                                                           .numRequests(numberOfStale)
                                                                           .pcfId("4937cf76-0ccf-11ed-861d-0242ac120002")
                                                                           .recoveryTimeStr("2016-04-13T23:50:50.52Z")
                                                                           .build();

        var setupLoadSetStaleFromWriteTime = new TrafficSetConfiguration.Builder().name("setup3")
                                                                                  .ipRange(ipRangeB)
                                                                                  .order(3)
                                                                                  .tps(2000)
                                                                                  .type(TrafficSetConfiguration.LoadType.REGISTER)
                                                                                  .numRequests(numberOfStale)
                                                                                  .pcfId("4937cf76-0ccf-11ed-861d-0242ac120002")
                                                                                  .build();

        var loadSetDiscovery = new TrafficSetConfiguration.Builder().name("set1")
                                                                    .ipRange(ipRangeA)
                                                                    .order(1)
                                                                    .tps(2000)
                                                                    .type(TrafficSetConfiguration.LoadType.DISCOVERY)
                                                                    .numRequests(numberOfRotational)
                                                                    .build();

        var loadSetRegister = new TrafficSetConfiguration.Builder().name("set2")
                                                                   .ipRange(ipRangeB)
                                                                   .order(2)
                                                                   .tps(2000)
                                                                   .type(TrafficSetConfiguration.LoadType.REGISTER)
                                                                   .numRequests(numberOfRotational)
                                                                   .build();

        var loadSetDeregister = new TrafficSetConfiguration.Builder().name("set3")
                                                                     .ipRange(ipRangeB)
                                                                     .trafficSetRef("set2")
                                                                     .order(3)
                                                                     .tps(2000)
                                                                     .targetHost(targetHostIPAddress)
                                                                     .type(TrafficSetConfiguration.LoadType.DEREGISTER)
                                                                     .build();

        var metrics = new MetricsConfiguration.Builder().exportMetrics(List.of(ExportMetrics.PROMETHEUS))
                                                        .csvMetricsDirectory("/tmp/")
                                                        .pmEnablePercentiles(false)
                                                        .pmEnableHistogramBuckets(false)
                                                        .build();

        return new BsfLoadConfiguration.Builder().metrics(metrics)
                                                 .duration(3600L)
                                                 .targetPort(31245)
                                                 .timeout(2000)
                                                 .setupTrafficMix(List.of(setupLoadSetValid, setupLoadSetStaleFromRt, setupLoadSetStaleFromWriteTime))
                                                 .trafficMix(List.of(loadSetDiscovery, loadSetRegister, loadSetDeregister))
                                                 .build();
    }

    public static void main(String[] args)
    {
        final var bsfLoadPort = 30080;
        final var metricsEnabled = true;
        final var metricsJvmEnabled = false;
        final var metricsPath = "/metrics";
        final var metricsPort = 30081;

        final var params = new BsfLoadParameters(bsfLoadPort, metricsEnabled, metricsJvmEnabled, metricsPath, metricsPort);

        final var runId = UUID.randomUUID();
        final var configuration = buildConfiguration();
        final var metricsHandler = new MetricsHandler("0.0.0.0", params);
        final var vertxOptions = new VertxOptions().setMetricsOptions(metricsHandler.getMetricsOptions());
        final var vertx = VertxBuilder.newInstance().setOptions(vertxOptions).modifyRxSchedulers(false).build();
        final var workload = new WorkLoad(configuration, runId, metricsHandler, vertx);
        metricsHandler.initServer(vertx);

        // Validate configuration.
        final var invalidParams = configuration.validate();
        if (!invalidParams.isEmpty())
        {
            invalidParams.stream().forEach(param -> log.info("{}", param));
            throw new IllegalArgumentException("Invalid configuration");
        }

        Completable.complete()
                   .andThen(metricsHandler.start()) //
                   .andThen(workload.run())
                   .andThen(metricsHandler.stop())
                   .blockingGet();

        System.exit(0);
    }
}
