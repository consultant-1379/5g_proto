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
 * Created on: Oct 5, 2022
 *     Author: eiiarlf
 */

package com.ericsson.esc.bsf.manager;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.internal.core.auth.PlainTextAuthProvider;
import com.ericsson.adpal.cm.actions.ActionInput;
import com.ericsson.esc.bsf.db.DbConfiguration;
import com.ericsson.esc.bsf.db.DbConfiguration.SizeTieredCompactionStrategy;
import com.ericsson.esc.bsf.db.FullTableScanParameters;
import com.ericsson.esc.bsf.db.MockPcfRtService;
import com.ericsson.esc.bsf.db.metrics.CqlMessages;
import com.ericsson.esc.bsf.db.metrics.CqlRequests;
import com.ericsson.esc.bsf.db.metrics.MetricsConfigurator;
import com.ericsson.esc.bsf.db.metrics.Throttling;
import com.ericsson.esc.bsf.openapi.model.BindingLevel;
import com.ericsson.esc.bsf.openapi.model.IpEndPoint;
import com.ericsson.esc.bsf.openapi.model.Ipv6Prefix;
import com.ericsson.esc.bsf.openapi.model.MacAddr48;
import com.ericsson.esc.bsf.openapi.model.PcfBinding;
import com.ericsson.esc.bsf.openapi.model.RecoveryTime;
import com.ericsson.esc.bsf.openapi.model.Snssai;
import com.ericsson.esc.bsf.worker.BindingCleanupManager;
import com.ericsson.esc.bsf.worker.MultipleBindingResolver;
import com.ericsson.esc.bsf.worker.NBsfManagementServiceImpl;
import com.ericsson.sc.bsf.etcd.PcfRt;
import com.ericsson.sc.bsf.model.EricssonBsfBsfFunctionNfInstanceBsfServicePcfRecoveryTimeBindingDatabaseScanCheckScanStatusOutput;
import com.ericsson.sc.bsf.model.EricssonBsfCurrentScan;
import com.ericsson.sc.bsf.model.EricssonBsfLastScan;
import com.ericsson.sc.bsf.model.HttpLookup;
import com.ericsson.sc.bsf.model.HttpLookup.ResolutionType;
import com.ericsson.utilities.cassandra.CassandraMetricsExporter;
import com.ericsson.utilities.cassandra.CassandraTestServer;
import com.ericsson.utilities.cassandra.RxSession;
import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.subjects.BehaviorSubject;

/**
 * 
 */
public class CheckScanStatusActionTest
{
    private static final Logger log = LoggerFactory.getLogger(CheckScanStatusActionTest.class);
    private static final int TTL_CONFIG_DEFAULT = 720 * 3600;
    private static final String IPV4_ADDR = "10.0.0.1";
    private static final String PCF1_DIAM_HOST = "pcf1.ericsson.com";

    private static final String TEST_PCF_DIAM_HOST = "testpcf.ericsson.com";
    private static final String TEST_PCF_DIAM_REALM = "ericsson.com";

    private static final String KEYSPACE = "nbsf_management_keyspace";
    private static final String TRUNCATE_OPERATION = String.format("TRUNCATE %s.pcf_bindings", KEYSPACE);
    private static final String LOCAL_DC_NAME = "datacenter1";
    private static final String DROP_KEYSPACE_OPERATION = "DROP KEYSPACE " + KEYSPACE;

    private static final Map<String, Object> replicationFactorSettings = Map.of("class", "NetworkTopologyStrategy", LOCAL_DC_NAME, 2);
    private static final HttpLookup httpMostRecent = new HttpLookup().withResolutionType(ResolutionType.MOST_RECENT);
    private static final MultipleBindingResolver multipleBindingResolver = new MultipleBindingResolver(httpMostRecent);
    private final BehaviorSubject<MultipleBindingResolver> mbrConfigFlow = BehaviorSubject.create();

    private final CassandraTestServer testBed = new CassandraTestServer();
    private NBsfManagementServiceImpl service;
    private DbConfiguration cassandraCfg;
    private RxSession rxSession;
    private BindingCleanupManager bindingCleanup;
    private DriverConfigLoader cassandraDriverConf;

    @BeforeClass
    public void beforeClass()
    {
        this.initializeTestEnvironment();
    }

    @BeforeMethod
    public void beforeMethod()
    {
        final var bsfNfInstance = String.format("bsf%s", new Random().nextInt());
        this.bindingCleanup = new BindingCleanupManager(this.rxSession, KEYSPACE, Flowable.just(bsfNfInstance));
    }

    @AfterMethod
    public void afterMethod()
    {
        this.rxSession.sessionHolder().blockingGet().getCqlSession().execute(TRUNCATE_OPERATION);
    }

    @AfterClass
    public void afterClass()
    {
        log.info("Closing testBed session");
        this.rxSession.close().blockingAwait();
        assertTrue(this.rxSession.sessionHolder().map(RxSession.SessionHolder::isClosed).blockingGet());
        testBed.stopCassandra();
    }

    @Test(enabled = true, groups = "functest")
    public void checkScanStatusNonStaleTest() throws JsonProcessingException, IllegalArgumentException
    {

        final var session = this.rxSession.sessionHolder().blockingGet();
        final var pcfId1 = UUID.randomUUID();
        final var recTime1 = new RecoveryTime("2010-08-05T11:56:12.5Z");
        final var pcfRtService = new MockPcfRtService();

        pcfRtService.setPcfRt(pcfId1, new PcfRt(pcfId1, Instant.parse(recTime1.getRecoveryTimeStr())));

        final var binding1 = generateTestBinding(IPV4_ADDR, null, "IP_Domain1", null, null, null, null, PCF1_DIAM_HOST, pcfId1.toString());
        this.registerBinding(binding1, TTL_CONFIG_DEFAULT);

        final var paramsValues = new FullTableScanParameters.Builder().setPageSize(10).setPageThrottlingMillis(1).setDeleteThrottlingMillis(1).build();

        final var fullTableScan = Optional.of((new FullTableScanManager(pcfRtService, paramsValues, this.bindingCleanup)));
        fullTableScan.get().fullTableScan(session).blockingAwait();

        final var checkScanStatus = new CheckScanStatusActionHandler(fullTableScan);
        final var actionParams = List.of();
        final var actionResult = checkScanStatus.executeAction(Single.just(new ActionInput("configuratioName",
                                                                                           "context",
                                                                                           "etag",
                                                                                           Jackson.om().valueToTree(actionParams))));
        final var result = actionResult.blockingGet();
        log.info("ActionResult: {}", result);

        final var parsedOutput = Jackson.om()
                                        .treeToValue(result.getOutput().get(),
                                                     EricssonBsfBsfFunctionNfInstanceBsfServicePcfRecoveryTimeBindingDatabaseScanCheckScanStatusOutput.class);

        assertTrue(result.getError().isEmpty());

        assertEquals(parsedOutput.getEricssonBsfCurrentScan().getCurrentStatus(), EricssonBsfCurrentScan.CurrentStatus.NOT_RUNNING);
        assertEquals(parsedOutput.getEricssonBsfCurrentScan().getScannedBindings(), 0L);
        assertEquals(parsedOutput.getEricssonBsfCurrentScan().getDeletedBindings(), 0L);
        assertEquals(parsedOutput.getEricssonBsfCurrentScan().getStaleBindings(), 0L);
        assertEquals(parsedOutput.getEricssonBsfCurrentScan().getStarted(), null);

        assertEquals(parsedOutput.getEricssonBsfLastScan().getStatus(), EricssonBsfLastScan.Status.COMPLETED);
        assertEquals(parsedOutput.getEricssonBsfLastScan().getScannedBindings(), 1L);
        assertEquals(parsedOutput.getEricssonBsfLastScan().getDeletedBindings(), 0L);
        assertEquals(parsedOutput.getEricssonBsfLastScan().getStaleBindings(), 0L);

    }

    @Test(enabled = true, groups = "functest")
    public void checkScanStatusNoScanTest() throws JsonProcessingException, IllegalArgumentException
    {

        final var pcfId1 = UUID.randomUUID();
        final var recTime1 = new RecoveryTime("2010-08-05T11:56:12.5Z");
        final var pcfRtService = new MockPcfRtService();

        pcfRtService.setPcfRt(pcfId1, new PcfRt(pcfId1, Instant.parse(recTime1.getRecoveryTimeStr())));

        final var binding1 = generateTestBinding(IPV4_ADDR, null, "IP_Domain1", null, null, null, null, PCF1_DIAM_HOST, pcfId1.toString());
        this.registerBinding(binding1, TTL_CONFIG_DEFAULT);

        final var paramsValues = new FullTableScanParameters.Builder().setPageSize(10).setPageThrottlingMillis(1).setDeleteThrottlingMillis(1).build();

        final var fullTableScan = Optional.of((new FullTableScanManager(pcfRtService, paramsValues, this.bindingCleanup)));

        final var checkScanStatus = new CheckScanStatusActionHandler(fullTableScan);
        final var actionParams = List.of();
        final var actionResult = checkScanStatus.executeAction(Single.just(new ActionInput("configuratioName",
                                                                                           "context",
                                                                                           "etag",
                                                                                           Jackson.om().valueToTree(actionParams))));
        final var result = actionResult.blockingGet();
        log.info("ActionResult: {}", result);

        final var parsedOutput = Jackson.om()
                                        .treeToValue(result.getOutput().get(),
                                                     EricssonBsfBsfFunctionNfInstanceBsfServicePcfRecoveryTimeBindingDatabaseScanCheckScanStatusOutput.class);

        assertTrue(result.getError().isEmpty());

        assertEquals(parsedOutput.getEricssonBsfCurrentScan().getCurrentStatus(), EricssonBsfCurrentScan.CurrentStatus.NOT_RUNNING);
        assertEquals(parsedOutput.getEricssonBsfCurrentScan().getScannedBindings(), 0L);
        assertEquals(parsedOutput.getEricssonBsfCurrentScan().getDeletedBindings(), 0L);
        assertEquals(parsedOutput.getEricssonBsfCurrentScan().getStaleBindings(), 0L);
        assertEquals(parsedOutput.getEricssonBsfCurrentScan().getStarted(), null);

        assertEquals(parsedOutput.getEricssonBsfLastScan(), null);

    }

    @Test(enabled = true, groups = "functest")
    public void checkScanStatusStaleTest() throws JsonProcessingException, IllegalArgumentException
    {

        final var session = this.rxSession.sessionHolder().blockingGet();
        final var pcfId1 = UUID.randomUUID();
        final var recTime1 = new RecoveryTime("2110-08-05T11:56:12.5Z");
        final var pcfRtService = new MockPcfRtService();

        pcfRtService.setPcfRt(pcfId1, new PcfRt(pcfId1, Instant.parse(recTime1.getRecoveryTimeStr())));

        final var binding1 = generateTestBinding(IPV4_ADDR, null, "IP_Domain1", null, null, null, null, PCF1_DIAM_HOST, pcfId1.toString());
        this.registerBinding(binding1, TTL_CONFIG_DEFAULT);

        final var paramsValues = new FullTableScanParameters.Builder().setPageSize(10).setPageThrottlingMillis(1).setDeleteThrottlingMillis(1).build();

        final var fullTableScan = Optional.of((new FullTableScanManager(pcfRtService, paramsValues, this.bindingCleanup)));
        fullTableScan.get().fullTableScan(session).blockingAwait();

        final var checkScanStatus = new CheckScanStatusActionHandler(fullTableScan);
        final var actionParams = List.of();
        final var actionResult = checkScanStatus.executeAction(Single.just(new ActionInput("configuratioName",
                                                                                           "context",
                                                                                           "etag",
                                                                                           Jackson.om().valueToTree(actionParams))));
        final var result = actionResult.blockingGet();
        log.info("ActionResult: {}", result);

        final var parsedOutput = Jackson.om()
                                        .treeToValue(result.getOutput().get(),
                                                     EricssonBsfBsfFunctionNfInstanceBsfServicePcfRecoveryTimeBindingDatabaseScanCheckScanStatusOutput.class);

        assertTrue(result.getError().isEmpty());

        assertEquals(parsedOutput.getEricssonBsfCurrentScan().getCurrentStatus(), EricssonBsfCurrentScan.CurrentStatus.NOT_RUNNING);
        assertEquals(parsedOutput.getEricssonBsfCurrentScan().getScannedBindings(), 0L);
        assertEquals(parsedOutput.getEricssonBsfCurrentScan().getDeletedBindings(), 0L);
        assertEquals(parsedOutput.getEricssonBsfCurrentScan().getStaleBindings(), 0L);
        assertEquals(parsedOutput.getEricssonBsfCurrentScan().getStarted(), null);

        assertEquals(parsedOutput.getEricssonBsfLastScan().getStatus(), EricssonBsfLastScan.Status.COMPLETED);
        assertEquals(parsedOutput.getEricssonBsfLastScan().getScannedBindings(), 1L);
        assertEquals(parsedOutput.getEricssonBsfLastScan().getDeletedBindings(), 1L);
        assertEquals(parsedOutput.getEricssonBsfLastScan().getStaleBindings(), 1L);

    }

    @Test(enabled = true, groups = "functest")
    public void checkScanStatusFailedTest() throws JsonProcessingException, IllegalArgumentException
    {

        final var session = this.rxSession.sessionHolder().blockingGet();

        session.getCqlSession().execute(DROP_KEYSPACE_OPERATION);

        final var pcfRtService = new MockPcfRtService();
        final var paramsValues = new FullTableScanParameters.Builder().setPageSize(10).setPageThrottlingMillis(1).setDeleteThrottlingMillis(1).build();

        final var fullTableScan = Optional.of(new FullTableScanManager(pcfRtService, paramsValues, this.bindingCleanup));
        assertThrows(Exception.class, () -> fullTableScan.get().fullTableScan(session).blockingAwait());

        final var checkScanStatus = new CheckScanStatusActionHandler(fullTableScan);
        final var actionParams = List.of();
        final var actionResult = checkScanStatus.executeAction(Single.just(new ActionInput("configuratioName",
                                                                                           "context",
                                                                                           "etag",
                                                                                           Jackson.om().valueToTree(actionParams))));
        final var result = actionResult.blockingGet();
        log.info("ActionResult: {}", result);

        final var parsedOutput = Jackson.om()
                                        .treeToValue(result.getOutput().get(),
                                                     EricssonBsfBsfFunctionNfInstanceBsfServicePcfRecoveryTimeBindingDatabaseScanCheckScanStatusOutput.class);

        assertTrue(result.getError().isEmpty());

        assertEquals(parsedOutput.getEricssonBsfCurrentScan().getCurrentStatus(), EricssonBsfCurrentScan.CurrentStatus.NOT_RUNNING);
        assertEquals(parsedOutput.getEricssonBsfCurrentScan().getScannedBindings(), 0L);
        assertEquals(parsedOutput.getEricssonBsfCurrentScan().getDeletedBindings(), 0L);
        assertEquals(parsedOutput.getEricssonBsfCurrentScan().getStaleBindings(), 0L);
        assertNull(parsedOutput.getEricssonBsfCurrentScan().getStarted());

        assertEquals(parsedOutput.getEricssonBsfLastScan().getStatus(), EricssonBsfLastScan.Status.FAILED);
        assertEquals(parsedOutput.getEricssonBsfLastScan().getScannedBindings(), 0L);
        assertEquals(parsedOutput.getEricssonBsfLastScan().getDeletedBindings(), 0L);
        assertEquals(parsedOutput.getEricssonBsfLastScan().getStaleBindings(), 0L);

        this.initializeTestEnvironment();

    }

    @Test(enabled = true, groups = "functest")
    public void checkScanStatusRunningTest() throws JsonProcessingException, IllegalArgumentException, InterruptedException
    {

        final var pcfId1 = UUID.randomUUID();
        final var pcfId2 = UUID.randomUUID();
        final var recTime1 = new RecoveryTime("2010-08-05T11:56:12.5Z");
        final var pcfRtService = new MockPcfRtService();

        pcfRtService.setPcfRt(pcfId1, new PcfRt(pcfId1, Instant.parse(recTime1.getRecoveryTimeStr())));

        final var binding1 = generateTestBinding(IPV4_ADDR, null, "IP_Domain1", null, null, null, null, PCF1_DIAM_HOST, pcfId1.toString());
        final var binding2 = generateTestBinding(IPV4_ADDR, null, "IP_Domain1", null, null, null, null, PCF1_DIAM_HOST, pcfId2.toString());
        this.registerBinding(binding1, TTL_CONFIG_DEFAULT);
        this.registerBinding(binding2, TTL_CONFIG_DEFAULT);

        final var paramsValues = new FullTableScanParameters.Builder().setPageSize(1).setPageThrottlingMillis(1000000000).setDeleteThrottlingMillis(1).build();

        final var fullTableScan = Optional.of((new FullTableScanManager(pcfRtService, paramsValues, this.bindingCleanup)));
        fullTableScan.get().triggerScan();

        // wait in order to start scanning
        TimeUnit.SECONDS.sleep(1);

        final var checkScanStatus = new CheckScanStatusActionHandler(fullTableScan);
        final var actionParams = List.of();
        final var actionResult = checkScanStatus.executeAction(Single.just(new ActionInput("configuratioName",
                                                                                           "context",
                                                                                           "etag",
                                                                                           Jackson.om().valueToTree(actionParams))));
        final var result = actionResult.blockingGet();
        log.info("ActionResult: {}", result);

        final var parsedOutput = Jackson.om()
                                        .treeToValue(result.getOutput().get(),
                                                     EricssonBsfBsfFunctionNfInstanceBsfServicePcfRecoveryTimeBindingDatabaseScanCheckScanStatusOutput.class);

        assertTrue(result.getError().isEmpty());

        assertEquals(parsedOutput.getEricssonBsfCurrentScan().getCurrentStatus(), EricssonBsfCurrentScan.CurrentStatus.RUNNING);
        assertNotNull(parsedOutput.getEricssonBsfCurrentScan().getStarted());
        assertNull(parsedOutput.getEricssonBsfLastScan());

        fullTableScan.get().cancelScan();

    }

    private void initializeTestEnvironment()
    {
        log.info("Initializing test environment ");

        final var cqlRequests = new CqlRequests.CqlRequestsBuilder().withHighestLatency(Duration.ofSeconds(10))
                                                                    .withSignificantDigits(2)
                                                                    .withRefreshInterval(Duration.ofSeconds(1))
                                                                    .build();

        final var cqlMessages = new CqlMessages.CqlMessagesBuilder().withHighestLatency(Duration.ofSeconds(10))
                                                                    .withSignificantDigits(2)
                                                                    .withRefreshInterval(Duration.ofSeconds(1))
                                                                    .build();

        final var throttling = new Throttling.ThrottlingBuilder().withHighestLatency(Duration.ofSeconds(10))
                                                                 .withSignificantDigits(2)
                                                                 .withRefreshInterval(Duration.ofSeconds(1))
                                                                 .build();

        final var metricsCfg = new MetricsConfigurator.MetricsConfiguratorBuilder("sessionName", "domainName").withCqlRequests(cqlRequests)
                                                                                                              .withCqlMessages(cqlMessages)
                                                                                                              .withThrottling(throttling)
                                                                                                              .build();
        this.testBed.startCassandra();

        this.cassandraCfg = createDbConfiguration();

        this.cassandraDriverConf = DriverConfigLoader.programmaticBuilder() //
                                                     .withStringList(DefaultDriverOption.CONTACT_POINTS, Arrays.asList(this.testBed.getContactPoint()))
                                                     .withBoolean(DefaultDriverOption.RECONNECT_ON_INIT, true)
                                                     .withString(DefaultDriverOption.LOAD_BALANCING_LOCAL_DATACENTER, LOCAL_DC_NAME)
                                                     .withString(DefaultDriverOption.REQUEST_CONSISTENCY, cassandraCfg.getCL())
                                                     .withClass(DefaultDriverOption.AUTH_PROVIDER_CLASS, PlainTextAuthProvider.class)
                                                     .withString(DefaultDriverOption.AUTH_PROVIDER_USER_NAME, testBed.getUsername())
                                                     .withString(DefaultDriverOption.AUTH_PROVIDER_PASSWORD, testBed.getPassword())
                                                     .withDuration(DefaultDriverOption.REQUEST_TIMEOUT, Duration.ofSeconds(30))

                                                     .withString(DefaultDriverOption.SESSION_NAME, metricsCfg.getSessionName())
                                                     .withStringList(DefaultDriverOption.METRICS_SESSION_ENABLED, metricsCfg.getSessionMetrics())
                                                     .withDuration(DefaultDriverOption.METRICS_SESSION_CQL_REQUESTS_HIGHEST,
                                                                   metricsCfg.getCqlRequests().getHighestLatency())
                                                     .withInt(DefaultDriverOption.METRICS_SESSION_CQL_REQUESTS_DIGITS,
                                                              metricsCfg.getCqlRequests().getSignificantDigits())
                                                     .withDuration(DefaultDriverOption.METRICS_SESSION_CQL_REQUESTS_INTERVAL,
                                                                   metricsCfg.getCqlRequests().getRefreshInterval())
                                                     .withDuration(DefaultDriverOption.METRICS_SESSION_THROTTLING_HIGHEST,
                                                                   metricsCfg.getThrottling().getHighestLatency())
                                                     .withInt(DefaultDriverOption.METRICS_SESSION_THROTTLING_DIGITS,
                                                              metricsCfg.getThrottling().getSignificantDigits())
                                                     .withDuration(DefaultDriverOption.METRICS_SESSION_THROTTLING_INTERVAL,
                                                                   metricsCfg.getThrottling().getRefreshInterval())
                                                     .withStringList(DefaultDriverOption.METRICS_NODE_ENABLED, metricsCfg.getNodeMetrics())
                                                     .withDuration(DefaultDriverOption.METRICS_NODE_CQL_MESSAGES_HIGHEST,
                                                                   metricsCfg.getCqlMessages().getHighestLatency())
                                                     .withInt(DefaultDriverOption.METRICS_NODE_CQL_MESSAGES_DIGITS,
                                                              metricsCfg.getCqlMessages().getSignificantDigits())
                                                     .withDuration(DefaultDriverOption.METRICS_NODE_CQL_MESSAGES_INTERVAL,
                                                                   metricsCfg.getCqlMessages().getRefreshInterval())
                                                     .build();

        this.rxSession = RxSession.builder().withConfig(this.cassandraDriverConf).build();

        final var rxMetrics = new CassandraMetricsExporter(this.rxSession, "rxmetrics_domain");

        rxMetrics.start().blockingAwait();

        final var success = new BsfSchemaHandler(this.rxSession, cassandraCfg).createAndVerifySchema(replicationFactorSettings).blockingGet();
        assertTrue(success);

        this.startNbsfManagementService();

        log.info("Initilization complete");
    }

    private DbConfiguration createDbConfiguration()
    {
        final var dbConfig = new DbConfiguration(KEYSPACE, List.of(testBed.getContactPoint()), LOCAL_DC_NAME);
        dbConfig.setUserCredentials(testBed.getUsername(), testBed.getPassword());
        dbConfig.setConsistency("ONE");
        dbConfig.setUserCredentials("bsfUser", "bsfPassword");
        dbConfig.setGcGrace(864000);
        dbConfig.setCompactionStrategy(new SizeTieredCompactionStrategy(2.0, 0.5, 32, 3));
        return dbConfig;
    }

    private void startNbsfManagementService()
    {
        this.service = new NBsfManagementServiceImpl(this.rxSession, KEYSPACE, mbrConfigFlow.toFlowable(BackpressureStrategy.ERROR));
//        cleanup manager gets a Flowable of String representing nfInstanceId that is used to step a counter
//        since we are not checking this counter in this class, a dummy value is used.
        this.mbrConfigFlow.onNext(multipleBindingResolver);
        this.service.init().blockingAwait();
    }

    private void registerBinding(final PcfBinding binding,
                                 final int ttl)
    {
        final var registerResult = service.register(binding, ttl).blockingGet();

        log.info("registerResult: {}", registerResult.getBindingId());
    }

    private static PcfBinding generateTestBinding(final String ipv4Addr,
                                                  final Ipv6Prefix ipv6Prefix,
                                                  final String ipDomain,
                                                  final String dnn,
                                                  final MacAddr48 macAddr48,
                                                  final List<String> addIpv6Prefixes,
                                                  final List<String> addMacAddrs,
                                                  final String pcfDiamHost,
                                                  final String pcfId)
    {
        final var insertedDnn = dnn != null ? dnn : "testDnn";
        final var pcfDhost = pcfDiamHost != null ? pcfDiamHost : TEST_PCF_DIAM_HOST;
        return PcfBinding.createJson("testSupi",
                                     "testGpsi",
                                     ipv4Addr,
                                     ipv6Prefix,
                                     ipDomain,
                                     macAddr48,
                                     insertedDnn,
                                     "testPcfFQDN",
                                     List.of(IpEndPoint.createJson("10.11.12.13", null, "TCP", 3868)),
                                     pcfDhost,
                                     TEST_PCF_DIAM_REALM,
                                     Snssai.create(6, "AF0456"),
                                     pcfId,
                                     null,
                                     null,
                                     addIpv6Prefixes,
                                     addMacAddrs,
                                     "set12.pcfset.5gc.mnc012.mcc345",
                                     new BindingLevel("NF_INSTANCE"));
    }
}
