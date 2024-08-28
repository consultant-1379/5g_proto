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
 * Created on: Jul 12, 2021
 *     Author: emldpng
 */

package com.ericsson.esc.bsf.load.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ericsson.esc.bsf.load.configuration.BsfLoadConfiguration;
import com.ericsson.esc.bsf.load.configuration.IPrange;
import com.ericsson.esc.bsf.load.configuration.MetricsConfiguration;
import com.ericsson.esc.bsf.load.configuration.MetricsConfiguration.ExportMetrics;
import com.ericsson.esc.bsf.load.configuration.TlsConfiguration;
import com.ericsson.esc.bsf.load.configuration.TrafficSetConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

/**
 * Tests the bsf-load configuration unmarshalling.
 */
public class BsfLoadConfigurationTest
{
    private static final Logger log = LoggerFactory.getLogger(BsfLoadConfigurationTest.class);
    private static final JsonMapper jm = JsonMapper.builder().configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true).build();

    @Test(groups = "functest")
    public void validTrafficSet()
    {
        var ipRange = new IPrange();
        ipRange.setStartIP("10.10.10.10");
        ipRange.setRange(100);

        var set = new TrafficSetConfiguration.Builder().name("set1")
                                                       .ipRange(ipRange)
                                                       .order(1)
                                                       .tps(2000)
                                                       .targetHost("192.168.1.1")
                                                       .type(TrafficSetConfiguration.LoadType.REGISTER)
                                                       .numRequests(100L)
                                                       .build();

        assertTrue(set.validate().isEmpty(), "Expected a valid traffic set");
    }

    @Test(groups = "functest")
    public void invalidTrafficSetInvalidIpRange()
    {
        var ipRange = new IPrange();
        ipRange.setStartIP("10.10.10.10");
        ipRange.setRange(-2);

        var set = new TrafficSetConfiguration.Builder().name("set1")
                                                       .ipRange(ipRange)
                                                       .order(1)
                                                       .tps(2000)
                                                       .type(TrafficSetConfiguration.LoadType.REGISTER)
                                                       .numRequests(100L)
                                                       .build();

        var ipRange2 = new IPrange();
        ipRange2.setStartIP("10");
        ipRange2.setRange(100);

        var set2 = new TrafficSetConfiguration.Builder().name("set2")
                                                        .ipRange(ipRange2)
                                                        .order(1)
                                                        .tps(2000)
                                                        .type(TrafficSetConfiguration.LoadType.REGISTER)
                                                        .numRequests(100L)
                                                        .build();

        assertFalse(set.validate().isEmpty(), "Expected invalid traffic set due to invalid IP range.");
        assertFalse(set2.validate().isEmpty(), "Expected invalid traffic set due to invalid starting IP.");
    }

    @Test(groups = "functest")
    public void invalidTrafficSetEmptyName()
    {
        var ipRange = new IPrange();
        ipRange.setStartIP("10.10.10.10");
        ipRange.setRange(100);

        var set = new TrafficSetConfiguration.Builder().ipRange(ipRange)
                                                       .order(1)
                                                       .tps(2000)
                                                       .type(TrafficSetConfiguration.LoadType.REGISTER)
                                                       .numRequests(100L)
                                                       .build();

        assertFalse(set.validate().isEmpty(), "Expected invalid traffic set due to empty name.");
    }

    @Test(groups = "functest")
    public void invalidRevoveryTime()
    {
        var ipRange = new IPrange();
        ipRange.setStartIP("10.10.10.10");
        ipRange.setRange(100);

        var set = new TrafficSetConfiguration.Builder().name("set1")
                                                       .ipRange(ipRange)
                                                       .order(1)
                                                       .tps(2000)
                                                       .type(TrafficSetConfiguration.LoadType.REGISTER)
                                                       .numRequests(100L)
                                                       .recoveryTimeStr("dummy")
                                                       .build();

        assertFalse(set.validate().isEmpty(), "Expected invalid traffic set due to recovery-time.");
    }

    @Test(groups = "functest")
    public void invalidTrafficSetInvalidOrder()
    {
        var ipRange = new IPrange();
        ipRange.setStartIP("10.10.10.10");
        ipRange.setRange(100);

        var set = new TrafficSetConfiguration.Builder().name("set1")
                                                       .ipRange(ipRange)
                                                       .tps(2000)
                                                       .type(TrafficSetConfiguration.LoadType.REGISTER)
                                                       .numRequests(100L)
                                                       .build();

        var set2 = new TrafficSetConfiguration.Builder().name("set2")
                                                        .order(-2)
                                                        .ipRange(ipRange)
                                                        .tps(2000)
                                                        .type(TrafficSetConfiguration.LoadType.REGISTER)
                                                        .numRequests(100L)
                                                        .build();

        assertFalse(set.validate().isEmpty(), "Expected invalid traffic set due to null order.");
        assertFalse(set2.validate().isEmpty(), "Expected invalid traffic set due to negative order.");
    }

    @Test(groups = "functest")
    public void invalidTrafficSetInvalidTps()
    {
        var ipRange = new IPrange();
        ipRange.setStartIP("10.10.10.10");
        ipRange.setRange(100);

        var set = new TrafficSetConfiguration.Builder().name("set1")
                                                       .order(1)
                                                       .ipRange(ipRange)
                                                       .type(TrafficSetConfiguration.LoadType.REGISTER)
                                                       .numRequests(100L)
                                                       .build();

        var set2 = new TrafficSetConfiguration.Builder().name("set2")
                                                        .order(2)
                                                        .ipRange(ipRange)
                                                        .tps(-10)
                                                        .type(TrafficSetConfiguration.LoadType.REGISTER)
                                                        .numRequests(100L)
                                                        .build();

        assertFalse(set.validate().isEmpty(), "Expected invalid traffic set due to null tps.");
        assertFalse(set2.validate().isEmpty(), "Expected invalid traffic set due to negative tps.");
    }

    @Test(groups = "functest")
    public void invalidTrafficSetEmtpyType()
    {
        var ipRange = new IPrange();
        ipRange.setStartIP("10.10.10.10");
        ipRange.setRange(100);

        var set = new TrafficSetConfiguration.Builder().name("set1").ipRange(ipRange).order(1).tps(2000).numRequests(100L).build();

        assertFalse(set.validate().isEmpty(), "Expected invalid traffic set due to empty type.");
    }

    @Test(groups = "functest")
    public void invalidTrafficSetInvalidTimeout()
    {
        var ipRange = new IPrange();
        ipRange.setStartIP("10.10.10.10");
        ipRange.setRange(100);

        var set = new TrafficSetConfiguration.Builder().name("set1")
                                                       .ipRange(ipRange)
                                                       .order(1)
                                                       .tps(2000)
                                                       .type(TrafficSetConfiguration.LoadType.REGISTER)
                                                       .numRequests(100L)
                                                       .timeout(-100L)
                                                       .build();

        assertFalse(set.validate().isEmpty(), "Expected invalid traffic set due to negative timeout.");
    }

    @Test(groups = "functest")
    public void invalidTrafficSetInvalidNumRequests()
    {
        var ipRange = new IPrange();
        ipRange.setStartIP("10.10.10.10");
        ipRange.setRange(100);

        // Null numRequest, type register should fail.
        var set = new TrafficSetConfiguration.Builder().name("set1")
                                                       .ipRange(ipRange)
                                                       .order(1)
                                                       .tps(2000)
                                                       .type(TrafficSetConfiguration.LoadType.REGISTER)
                                                       .build();

        // Null numRequest, type deregister, no trafficSetRef should fail.
        var set2 = new TrafficSetConfiguration.Builder().name("set2")
                                                        .ipRange(ipRange)
                                                        .order(1)
                                                        .tps(2000)
                                                        .type(TrafficSetConfiguration.LoadType.DEREGISTER)
                                                        .build();

        // Null numRequest, type deregister, with trafficSetRef should succeed.
        var set3 = new TrafficSetConfiguration.Builder().name("set3")
                                                        .ipRange(ipRange)
                                                        .order(1)
                                                        .tps(2000)
                                                        .targetHost("192.168.1.1")
                                                        .type(TrafficSetConfiguration.LoadType.DEREGISTER)
                                                        .trafficSetRef("set1")
                                                        .build();

        // Negative numRequest, should fail.
        var set4 = new TrafficSetConfiguration.Builder().name("set4")
                                                        .ipRange(ipRange)
                                                        .order(1)
                                                        .tps(2000)
                                                        .type(TrafficSetConfiguration.LoadType.REGISTER)
                                                        .numRequests(-10L)
                                                        .build();

        assertFalse(set.validate().isEmpty(), "Expected invalid traffic set due to null numRequest.");
        assertFalse(set2.validate().isEmpty(), "Expected invalid traffic set due to null numRequest.");
        assertTrue(set3.validate().isEmpty(), "Expected valid traffic set.");
        assertFalse(set4.validate().isEmpty(), "Expected invalid traffic set due to negative numRequest.");
    }

    @Test(groups = "functest")
    public void validConfigurationSingleTrafficSet()
    {
        var ipRange = new IPrange();
        ipRange.setStartIP("10.10.10.10");
        ipRange.setRange(100);

        var set = new TrafficSetConfiguration.Builder().name("set1")
                                                       .ipRange(ipRange)
                                                       .order(1)
                                                       .tps(2000)
                                                       .targetHost("192.168.1.1")
                                                       .type(TrafficSetConfiguration.LoadType.REGISTER)
                                                       .numRequests(100L)
                                                       .pcfId(UUID.randomUUID().toString())
                                                       .recoveryTimeStr(Instant.now().toString())
                                                       .build();

        var conf = new BsfLoadConfiguration.Builder().duration(120L)
                                                     .executionType(BsfLoadConfiguration.ExecutionType.SERIAL)
                                                     .targetPort(80)
                                                     .http2Streams(40)
                                                     .maxTcpConnectionsPerClient(2)
                                                     .maxParallelTransactions(20000)
                                                     .timeout(2000L)
                                                     .trafficMix(List.of(set))
                                                     .build();

        assertTrue(conf.validate().isEmpty(), "Expected valid configuration.");
    }

    @Test(groups = "functest")
    public void validConfigurationMultipleTrafficSet()
    {
        var ipRange = new IPrange();
        ipRange.setStartIP("10.10.10.10");
        ipRange.setRange(100);

        var ipRange2 = new IPrange();
        ipRange2.setStartIP("10.0.0.1");
        ipRange2.setRange(100);

        var setupSet = new TrafficSetConfiguration.Builder().name("setup1")
                                                            .ipRange(ipRange)
                                                            .order(1)
                                                            .tps(2000)
                                                            .targetHost("192.168.1.1")
                                                            .type(TrafficSetConfiguration.LoadType.REGISTER)
                                                            .numRequests(100L)
                                                            .build();

        var set1 = new TrafficSetConfiguration.Builder().name("set1")
                                                        .ipRange(ipRange2)
                                                        .order(1)
                                                        .tps(2000)
                                                        .targetHost("192.168.1.1")
                                                        .type(TrafficSetConfiguration.LoadType.REGISTER)
                                                        .numRequests(100L)
                                                        .build();

        var set2 = new TrafficSetConfiguration.Builder().name("set2")
                                                        .ipRange(ipRange)
                                                        .order(2)
                                                        .tps(4000)
                                                        .targetHost("192.168.1.1")
                                                        .type(TrafficSetConfiguration.LoadType.DISCOVERY)
                                                        .numRequests(10000L)
                                                        .build();

        var set3 = new TrafficSetConfiguration.Builder().name("set3")
                                                        .order(3)
                                                        .tps(2000)
                                                        .targetHost("192.168.1.1")
                                                        .type(TrafficSetConfiguration.LoadType.DEREGISTER)
                                                        .trafficSetRef("set1")
                                                        .build();

        var conf = new BsfLoadConfiguration.Builder().duration(120L)
                                                     .executionType(BsfLoadConfiguration.ExecutionType.SERIAL)
                                                     .targetPort(80)
                                                     .http2Streams(40)
                                                     .maxTcpConnectionsPerClient(2)
                                                     .maxParallelTransactions(20000)
                                                     .timeout(2000L)
                                                     .setupTrafficMix(List.of(setupSet))
                                                     .trafficMix(List.of(set1, set2, set3))
                                                     .build();

        assertTrue(conf.validate().isEmpty(), "Expected valid configuration.");
    }

    @Test(groups = "functest")
    public void invalidConfigurationInvalidDuration()
    {
        var conf = new BsfLoadConfiguration.Builder().duration(-1L).targetPort(80).build();

        assertFalse(conf.validate().isEmpty(), "Expected invalid configuration due to negative duration.");
    }

    @Test(groups = "functest")
    public void invalidConfigurationInvalidTargetEndpoints()
    {
        var conf = new TrafficSetConfiguration.Builder().tps(500).build();

        var conf2 = new BsfLoadConfiguration.Builder().targetPort(-80).build();

        assertFalse(conf.validate().isEmpty(), "Expected invalid configuration due to empty targetHost.");
        assertFalse(conf2.validate().isEmpty(), "Expected invalid configuration due to negative targetPort.");
    }

    @Test(groups = "functest")
    public void invalidConfigurationInvalidResources()
    {
        var conf = new BsfLoadConfiguration.Builder().targetPort(80).http2Streams(-2).build();

        var conf2 = new BsfLoadConfiguration.Builder().targetPort(80).maxTcpConnectionsPerClient(-2).build();

        var conf3 = new BsfLoadConfiguration.Builder().targetPort(80).maxParallelTransactions(-2).build();

        var conf4 = new BsfLoadConfiguration.Builder().targetPort(80).tcpClients(0).build();

        assertFalse(conf.validate().isEmpty(), "Expected invalid configuration due to negative http2Streams.");
        assertFalse(conf2.validate().isEmpty(), "Expected invalid configuration due to negative tcpConnections.");
        assertFalse(conf3.validate().isEmpty(), "Expected invalid configuration due to negative maxParallelTransactions.");
        assertFalse(conf4.validate().isEmpty(), "Expected invalid configuration due to zero tcpClients.");
    }

    @Test(groups = "functest")
    public void invalidConfigurationNonUniqueNames()
    {
        var ipRange = new IPrange();
        ipRange.setStartIP("10.10.10.10");
        ipRange.setRange(100);

        var set = new TrafficSetConfiguration.Builder().name("set1")
                                                       .ipRange(ipRange)
                                                       .order(1)
                                                       .tps(2000)
                                                       .targetHost("192.168.1.1")
                                                       .type(TrafficSetConfiguration.LoadType.REGISTER)
                                                       .numRequests(100L)
                                                       .build();

        var set2 = new TrafficSetConfiguration.Builder().name("set1")
                                                        .ipRange(ipRange)
                                                        .order(3)
                                                        .tps(4000)
                                                        .targetHost("192.168.1.1")
                                                        .type(TrafficSetConfiguration.LoadType.DISCOVERY)
                                                        .numRequests(10000L)
                                                        .build();

        var conf = new BsfLoadConfiguration.Builder().targetPort(80).trafficMix(List.of(set, set2)).build();

        assertFalse(conf.validate().isEmpty(), "Expected invalid configuration due to non unique names.");
    }

    @Test(groups = "functest")
    public void invalidConfigurationNonUniqueOrders()
    {
        var ipRange = new IPrange();
        ipRange.setStartIP("10.10.10.10");
        ipRange.setRange(100);

        var set = new TrafficSetConfiguration.Builder().name("set1")
                                                       .ipRange(ipRange)
                                                       .order(1)
                                                       .tps(2000)
                                                       .targetHost("192.168.1.1")
                                                       .type(TrafficSetConfiguration.LoadType.REGISTER)
                                                       .numRequests(100L)
                                                       .build();

        var set2 = new TrafficSetConfiguration.Builder().name("set2")
                                                        .ipRange(ipRange)
                                                        .order(1)
                                                        .tps(4000)
                                                        .targetHost("192.168.1.1")
                                                        .type(TrafficSetConfiguration.LoadType.DISCOVERY)
                                                        .numRequests(10000L)
                                                        .build();

        var conf = new BsfLoadConfiguration.Builder().targetPort(80).trafficMix(List.of(set, set2)).build();

        assertFalse(conf.validate().isEmpty(), "Expected invalid configuration due to non unique orders.");
    }

    @Test(groups = "functest")
    public void invalidConfigurationWrongSetReference()
    {
        var set = new TrafficSetConfiguration.Builder().name("set1")
                                                       .order(1)
                                                       .tps(200)
                                                       .targetHost("192.168.1.1")
                                                       .trafficSetRef("unknownSet")
                                                       .type(TrafficSetConfiguration.LoadType.DEREGISTER)
                                                       .build();

        var conf = new BsfLoadConfiguration.Builder().targetPort(80).trafficMix(List.of(set)).build();

        assertFalse(conf.validate().isEmpty(), "Expected invalid configuration due to invalid trafficSetRef.");
    }

    @Test(groups = "functest")
    public void invalidConfigurationWrongOrderOfReferencedSet()
    {
        var ipRange = new IPrange();
        ipRange.setStartIP("10.10.10.10");
        ipRange.setRange(100);

        var set = new TrafficSetConfiguration.Builder().name("set1")
                                                       .ipRange(ipRange)
                                                       .order(3)
                                                       .tps(2000)
                                                       .targetHost("192.168.1.1")
                                                       .type(TrafficSetConfiguration.LoadType.REGISTER)
                                                       .numRequests(100L)
                                                       .build();

        var set2 = new TrafficSetConfiguration.Builder().name("set2")
                                                        .order(2)
                                                        .tps(200)
                                                        .targetHost("192.168.1.1")
                                                        .type(TrafficSetConfiguration.LoadType.DEREGISTER)
                                                        .trafficSetRef("set1")
                                                        .build();

        var conf = new BsfLoadConfiguration.Builder().targetPort(80).trafficMix(List.of(set, set2)).build();

        assertFalse(conf.validate().isEmpty(), "Expected invalid configuration due to wrong order of referenced set.");
    }

    @Test(groups = "functest")
    public void invalidConfigurationWrongTypeOfReferencedSet()
    {
        var ipRange = new IPrange();
        ipRange.setStartIP("10.10.10.10");
        ipRange.setRange(100);

        var set = new TrafficSetConfiguration.Builder().name("set1")
                                                       .ipRange(ipRange)
                                                       .order(1)
                                                       .tps(2000)
                                                       .targetHost("192.168.1.1")
                                                       .type(TrafficSetConfiguration.LoadType.DISCOVERY)
                                                       .numRequests(100L)
                                                       .build();

        var set2 = new TrafficSetConfiguration.Builder().name("set2")
                                                        .order(2)
                                                        .tps(200)
                                                        .targetHost("192.168.1.1")
                                                        .type(TrafficSetConfiguration.LoadType.DEREGISTER)
                                                        .trafficSetRef("set1")
                                                        .build();

        var conf = new BsfLoadConfiguration.Builder().targetPort(80).trafficMix(List.of(set, set2)).build();

        assertFalse(conf.validate().isEmpty(), "Expected invalid configuration due to wrong order of referenced set.");
    }

    @Test(groups = "functest")
    public void parseSingleLoadSetSuccess() throws JsonMappingException, JsonProcessingException
    {
        var json = readConfigFromResources("single-traffic-set.json");
        assertTrue(json.isPresent(), "Configuration file not available.");

        var setupIpRange = new IPrange();
        setupIpRange.setStartIP("127.10.10.10");
        setupIpRange.setRange(100);

        var ipRange = new IPrange();
        ipRange.setStartIP("10.10.10.10");
        ipRange.setRange(100);

        var setupLoadSet = new TrafficSetConfiguration.Builder().id(UUID.fromString("b5659381-336f-45cd-b985-50720490154d"))
                                                                .name("setup1")
                                                                .ipRange(setupIpRange)
                                                                .order(1)
                                                                .tps(2000)
                                                                .targetHost("192.168.1.1")
                                                                .type(TrafficSetConfiguration.LoadType.REGISTER)
                                                                .numRequests(100L)
                                                                .build();

        var loadSet = new TrafficSetConfiguration.Builder().id(UUID.fromString("1a144b98-bbdf-11ec-8422-0242ac120002"))
                                                           .name("set1")
                                                           .ipRange(ipRange)
                                                           .order(1)
                                                           .tps(2000)
                                                           .targetHost("192.168.1.1")
                                                           .type(TrafficSetConfiguration.LoadType.REGISTER)
                                                           .numRequests(2000000L)
                                                           .build();

        var targetConfig = new BsfLoadConfiguration.Builder().duration(1500L)
                                                             .executionType(BsfLoadConfiguration.ExecutionType.SERIAL)
                                                             .targetPort(80)
                                                             .http2Streams(40)
                                                             .maxTcpConnectionsPerClient(2)
                                                             .tcpClients(40)
                                                             .maxParallelTransactions(20000)
                                                             .timeout(2000L)
                                                             .setupTrafficMix(List.of(setupLoadSet))
                                                             .trafficMix(List.of(loadSet))
                                                             .build();

        BsfLoadConfiguration fromFileConfig = jm.readValue(json.get(), BsfLoadConfiguration.class);

        assertTrue(targetConfig.equals(fromFileConfig), "The BsfLoadConfiguration is not correct.");
        assertTrue(fromFileConfig.validate().isEmpty(), "The BsfLoadConfiguration is invalid.");
    }

    @Test(groups = "functest")
    public void parseSingleLoadTlsSetSuccess() throws JsonMappingException, JsonProcessingException
    {
        var json = readConfigFromResources("single-traffic-tls.json");
        assertTrue(json.isPresent(), "Configuration file not available.");

        var ipRange = new IPrange();
        ipRange.setStartIP("10.0.0.1");
        ipRange.setRange(100);

        var loadSet = new TrafficSetConfiguration.Builder().id(UUID.fromString("b5659381-336f-45cd-b985-50720490154d"))
                                                           .name("set1")
                                                           .ipRange(ipRange)
                                                           .order(1)
                                                           .tps(500)
                                                           .targetHost("192.168.1.1")
                                                           .type(TrafficSetConfiguration.LoadType.DISCOVERY)
                                                           .numRequests(3000L)
                                                           .build();

        var tlsConfig = new TlsConfiguration.Builder().enabled(true)
                                                      .verifyHost(false)
                                                      .certPath("./certificates/cert.pem")
                                                      .keyPath("./certificates/key.pem")
                                                      .build();

        var targetConfig = new BsfLoadConfiguration.Builder().duration(20L)
                                                             .executionType(BsfLoadConfiguration.ExecutionType.SERIAL)
                                                             .targetPort(80)
                                                             .trafficMix(List.of(loadSet))
                                                             .tls(tlsConfig)
                                                             .build();

        BsfLoadConfiguration fromFileConfig = jm.readValue(json.get(), BsfLoadConfiguration.class);

        assertTrue(targetConfig.equals(fromFileConfig), "The BsfLoadConfiguration is not correct.");
        assertTrue(fromFileConfig.validate().isEmpty(), "The BsfLoadConfiguration is invalid.");
    }

    @Test(groups = "functest")
    public void parseCompleteMetricsConfigurationSuccess() throws IOException
    {
        var json = readConfigFromResources("valid-complete-metrics.json");
        assertTrue(json.isPresent(), "Configuration file not available.");

        var targetConfig = new MetricsConfiguration.Builder().exportMetrics(List.of(ExportMetrics.PROMETHEUS, ExportMetrics.CSV_FILE))
                                                             .csvConvertDurationsTo("MILLISECONDS")
                                                             .csvConvertRatesTo("SECONDS")
                                                             .csvNamePrefix("metrics")
                                                             .csvPollInterval(1)
                                                             .pmEnablePercentiles(true)
                                                             .pmTargetPercentiles(List.of("0.5", "0.9", "0.99"))
                                                             .pmPercentilePrecision(1)
                                                             .pmPercentileMetrics(List.of("vertx.metric.alpha", "vertx.metric.beta"))
                                                             .pmEnableHistogramBuckets(false)
                                                             .pmHistogramBucketsMetrics(List.of("vertx.metric.delta"))
                                                             .build();

        MetricsConfiguration fromFileConfig = jm.readValue(json.get(), MetricsConfiguration.class);

        assertTrue(targetConfig.equals(fromFileConfig), "The MetricsConfiguration is not correct.");
        assertTrue(fromFileConfig.validate().isEmpty(), "The MetricsConfiguration is invalid.");
    }

    @Test(groups = "functest")
    public void parseMultipleLoadSetsSuccess() throws JsonMappingException, JsonProcessingException
    {
        var json = readConfigFromResources("multiple-traffic-sets.json");
        assertTrue(json.isPresent(), "Configuration file not available.");

        var ipRangeA = new IPrange();
        ipRangeA.setStartIP("10.10.10.10");
        ipRangeA.setRange(2000000);

        var ipRangeB = new IPrange();
        ipRangeB.setStartIP("10.10.10.10");
        ipRangeB.setRange(100);

        var loadSetA = new TrafficSetConfiguration.Builder().id(UUID.fromString("b5659381-336f-45cd-b985-50720490154d"))
                                                            .name("set1")
                                                            .ipRange(ipRangeA)
                                                            .order(1)
                                                            .tps(2000)
                                                            .targetHost("192.168.1.1")
                                                            .type(TrafficSetConfiguration.LoadType.REGISTER)
                                                            .numRequests(2000000L)
                                                            .build();

        var loadSetB = new TrafficSetConfiguration.Builder().id(UUID.fromString("1a144b98-bbdf-11ec-8422-0242ac120002"))
                                                            .name("set2")
                                                            .ipRange(ipRangeB)
                                                            .order(2)
                                                            .tps(500)
                                                            .targetHost("192.168.1.1")
                                                            .type(TrafficSetConfiguration.LoadType.DISCOVERY)
                                                            .numRequests(2000000L)
                                                            .build();

        var loadSetC = new TrafficSetConfiguration.Builder().id(UUID.fromString("9b98a920-bbdf-11ec-8422-0242ac120002"))
                                                            .name("set3")
                                                            .order(3)
                                                            .tps(100)
                                                            .targetHost("192.168.1.1")
                                                            .type(TrafficSetConfiguration.LoadType.DEREGISTER)
                                                            .trafficSetRef("set1")
                                                            .build();

        var targetConfig = new BsfLoadConfiguration.Builder().duration(1500L)
                                                             .executionType(BsfLoadConfiguration.ExecutionType.SERIAL)
                                                             .targetPort(80)
                                                             .http2Streams(40)
                                                             .maxTcpConnectionsPerClient(2)
                                                             .tcpClients(40)
                                                             .maxParallelTransactions(20000)
                                                             .timeout(2000L)
                                                             .trafficMix(List.of(loadSetA, loadSetB, loadSetC))
                                                             .http2KeepAliveTimeout(40)
                                                             .build();

        BsfLoadConfiguration fromFileConfig = jm.readValue(json.get(), BsfLoadConfiguration.class);

        assertTrue(targetConfig.equals(fromFileConfig), "The BsfLoadConfiguration is not correct.");
        assertTrue(fromFileConfig.validate().isEmpty(), "The BsfLoadConfiguration is invalid.");
    }

    @Test(enabled = true, expectedExceptions = com.fasterxml.jackson.databind.exc.MismatchedInputException.class)
    public void parseWrongInputFail() throws JsonMappingException, JsonProcessingException
    {
        var json = readConfigFromResources("wrong-input.json");
        assertTrue(json.isPresent(), "Configuration file not available.");

        jm.readValue(json.get(), BsfLoadConfiguration.class);
    }

    @Test(enabled = true, expectedExceptions = com.fasterxml.jackson.databind.exc.InvalidFormatException.class)
    public void parseWrongLoadSetTypeFail() throws JsonMappingException, JsonProcessingException
    {
        var json = readConfigFromResources("wrong-traffic-set-type.json");
        assertTrue(json.isPresent(), "Configuration file not available.");

        jm.readValue(json.get(), BsfLoadConfiguration.class);
    }

    @Test(enabled = true, expectedExceptions = com.fasterxml.jackson.core.JsonParseException.class)
    public void parseInvalidJsonFail() throws JsonMappingException, JsonProcessingException
    {
        var json = readConfigFromResources("invalid-json.json");
        assertTrue(json.isPresent(), "Configuration file not available.");

        jm.readValue(json.get(), BsfLoadConfiguration.class);
    }

    @Test(groups = "functest")
    public void assertEmptyBsfLoadConfigurationHttp2KeepAliveTimeout()
    {
        var conf = new BsfLoadConfiguration.Builder().targetPort(80).build();

        assertEquals(conf.getHttp2KeepAliveTimeout(), 20);
        assertTrue(conf.validate().isEmpty(), "Expected true but configuration was invalid.");

        conf = new BsfLoadConfiguration.Builder().targetPort(80).http2KeepAliveTimeout(10).build();

        assertEquals(conf.getHttp2KeepAliveTimeout(), 10);
        assertTrue(conf.validate().isEmpty(), "Expected true but configuration was invalid.");

    }

    @Test(groups = "functest")
    public void invalidBsfLoadConfigurationHttp2KeepAliveTimeout()
    {
        var conf = new BsfLoadConfiguration.Builder().targetPort(80).http2KeepAliveTimeout(0).build();

        assertEquals(conf.getHttp2KeepAliveTimeout(), 0);
        assertFalse(conf.validate().isEmpty(), "Expected invalid configuration due to non-positive http2KeepAliveTimeout.");
    }

    private Optional<String> readConfigFromResources(String fileName)
    {
        Optional<String> json = Optional.empty();
        InputStream is = BsfLoadConfigurationTest.class.getClassLoader().getResourceAsStream(fileName);

        try
        {
            if (is != null)
                json = Optional.of(new String(is.readAllBytes(), StandardCharsets.UTF_8));
        }
        catch (IOException e)
        {
            log.info("Failed to fetch configuration file {} from resources.", fileName);
        }

        return json;
    }
}
