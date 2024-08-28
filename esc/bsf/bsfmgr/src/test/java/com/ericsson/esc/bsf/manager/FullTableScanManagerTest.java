package com.ericsson.esc.bsf.manager;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.internal.core.auth.PlainTextAuthProvider;
import com.ericsson.esc.bsf.db.DbConfiguration;
import com.ericsson.esc.bsf.db.DbConfiguration.SizeTieredCompactionStrategy;
import com.ericsson.esc.bsf.db.FullTableScanParameters;
import com.ericsson.esc.bsf.db.FullTableScanParameters.Builder;
import com.ericsson.esc.bsf.db.MockPcfRtService;
import com.ericsson.esc.bsf.db.metrics.CqlMessages;
import com.ericsson.esc.bsf.db.metrics.CqlRequests;
import com.ericsson.esc.bsf.db.metrics.MetricsConfigurator;
import com.ericsson.esc.bsf.db.metrics.Throttling;
import com.ericsson.esc.bsf.manager.FullTableScanManager.BindingDatabaseScanException;
import com.ericsson.esc.bsf.manager.FullTableScanManager.FailureReason;
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
import com.ericsson.esc.bsf.worker.StalePcfBinding.Reason;
import com.ericsson.sc.bsf.etcd.PcfRt;
import com.ericsson.sc.bsf.etcd.PcfRtService.Source;
import com.ericsson.sc.bsf.model.HttpLookup;
import com.ericsson.sc.bsf.model.HttpLookup.ResolutionType;
import com.ericsson.utilities.cassandra.CassandraMetricsExporter;
import com.ericsson.utilities.cassandra.CassandraTestServer;
import com.ericsson.utilities.cassandra.RxSession;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.subjects.BehaviorSubject;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class FullTableScanManagerTest
{
    private static final Logger log = LoggerFactory.getLogger(FullTableScanManagerTest.class);

    private static final String KEYSPACE = "nbsf_management_keyspace";
    private static final String SELECT_OPERATION = String.format("SELECT binding_id, writetime(ue_address), pcf_id, recovery_time FROM %s.pcf_bindings",
                                                                 KEYSPACE);
    private static final String TRUNCATE_OPERATION = String.format("TRUNCATE %s.pcf_bindings", KEYSPACE);
    private static final String DROP_KEYSPACE_OPERATION = "DROP KEYSPACE " + KEYSPACE;

    private static final String PCF1_DIAM_HOST = "pcf1.ericsson.com";
    private static final String PCF2_DIAM_HOST = "pcf2.ericsson.com";

    private static final String TEST_PCF_DIAM_HOST = "testpcf.ericsson.com";
    private static final String TEST_PCF_DIAM_REALM = "ericsson.com";

    private static final String LOCAL_DC_NAME = "datacenter1";
    private static final String IPV4_ADDR = "10.0.0.1";

    /* Parameters to be used for Binding Cleanup Mechanism (TTL) */

    private static final int TTL_CONFIG_DEFAULT = 720 * 3600;

    private static final Map<String, Object> replicationFactorSettings = Map.of("class", "NetworkTopologyStrategy", LOCAL_DC_NAME, 2);

    private RxSession rxSession;
    private NBsfManagementServiceImpl service;
    private BindingCleanupManager bindingCleanup;

    private final CassandraTestServer testBed = new CassandraTestServer();
    private final BehaviorSubject<MultipleBindingResolver> mbrConfigFlow = BehaviorSubject.create();
    private static final HttpLookup httpMostRecent = new HttpLookup().withResolutionType(ResolutionType.MOST_RECENT);
    private static final MultipleBindingResolver multipleBindingResolver = new MultipleBindingResolver(httpMostRecent);
    private DriverConfigLoader cassandraDriverConf;
    private DbConfiguration cassandraCfg;

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
    private void testDeletedStalePreservedValidBinding()
    {
        final var session = this.rxSession.sessionHolder().blockingGet();

        final var pcfId1 = "093f345e-f2df-11ec-b939-0242ac120001";
        final var pcfId2 = "093f345e-f2df-11ec-b939-0242ac120002";

        final var binding1 = generateTestBinding(IPV4_ADDR, null, "IP_Domain1", null, null, null, null, PCF1_DIAM_HOST, pcfId1);
        final var binding2 = generateTestBinding(IPV4_ADDR, null, "IP_Domain1", null, null, null, null, PCF2_DIAM_HOST, pcfId2);

        this.registerBinding(binding1, TTL_CONFIG_DEFAULT);
        this.registerBinding(binding2, TTL_CONFIG_DEFAULT);

        final var stalePcfId = UUID.fromString(pcfId1);
        final var recTime1 = new RecoveryTime("2900-08-05T11:56:12.5Z");

        final var pcfRtService = new MockPcfRtService();
        pcfRtService.setPcfRt(stalePcfId, new PcfRt(stalePcfId, Instant.parse(recTime1.getRecoveryTimeStr())));

        final var paramsValues = new Builder().setPageSize(10).setPageThrottlingMillis(1).setDeleteThrottlingMillis(1).build();

        final var fullTableScan = new FullTableScanManager(pcfRtService, paramsValues, this.bindingCleanup);

        assertTrue(fullTableScan.getCounters().getCcBindingsScannedTotal(Optional.of(stalePcfId), this.bindingCleanup.getNfInstanceId()).get() == 0);
        assertTrue(fullTableScan.getCounters()
                                .getCcBindingsScannedTotal(Optional.of(UUID.fromString(pcfId2)), this.bindingCleanup.getNfInstanceId())
                                .get() == 0);
        assertTrue(fullTableScan.getCounters().getCcBindingsStaleTotal(stalePcfId, this.bindingCleanup.getNfInstanceId()).get() == 0);
        assertTrue(this.bindingCleanup.getCounters()
                                      .getCcStaleBindingsDeleted(Source.BINDING_DATABASE_SCAN.toString(),
                                                                 Reason.PCF_RECOVERY_TIME.toString(),
                                                                 stalePcfId,
                                                                 this.bindingCleanup.getNfInstanceId())
                                      .get() == 0);
        assertTrue(this.bindingCleanup.getCounters()
                                      .getCcStaleBindingsDeleted(Source.BINDING_DATABASE_SCAN.toString(),
                                                                 Reason.PCF_RECOVERY_TIME.toString(),
                                                                 UUID.fromString(pcfId2),
                                                                 this.bindingCleanup.getNfInstanceId())
                                      .get() == 0);

        this.validateFullTableScanCountersAreZero(fullTableScan);

        fullTableScan.fullTableScan(session).blockingAwait();

        final var rs = session.getCqlSession().execute(SELECT_OPERATION);

        final var validPcfId = rs.iterator().next().getUuid("pcf_id");

        assertNotEquals(validPcfId, stalePcfId, "Expected stale pcfId to be deleted");
        assertEquals(validPcfId, UUID.fromString(pcfId2), "Expected valid pcfId to be preserved");

        // Counters stepping validation
        assertTrue(fullTableScan.getCounters().getCcBindingsScannedTotal(Optional.of(stalePcfId), bindingCleanup.getNfInstanceId()).get() == 1);
        assertTrue(fullTableScan.getCounters().getCcBindingsScannedTotal(Optional.of(UUID.fromString(pcfId2)), bindingCleanup.getNfInstanceId()).get() == 1);
        assertTrue(fullTableScan.getCounters().getCcBindingsStaleTotal(stalePcfId, this.bindingCleanup.getNfInstanceId()).get() == 1);
        assertTrue(this.bindingCleanup.getCounters()
                                      .getCcStaleBindingsDeleted(Source.BINDING_DATABASE_SCAN.toString(),
                                                                 Reason.PCF_RECOVERY_TIME.toString(),
                                                                 stalePcfId,
                                                                 this.bindingCleanup.getNfInstanceId())
                                      .get() == 1);
        assertTrue(this.bindingCleanup.getCounters()
                                      .getCcStaleBindingsDeleted(Source.BINDING_DATABASE_SCAN.toString(),
                                                                 Reason.PCF_RECOVERY_TIME.toString(),
                                                                 UUID.fromString(pcfId2),
                                                                 this.bindingCleanup.getNfInstanceId())
                                      .get() == 0);

        assertTrue(fullTableScan.getCounters().getCcScansStartedTotal(this.bindingCleanup.getNfInstanceId()).get() == 1);
        assertTrue(fullTableScan.getCounters().getCcScansCompletedTotal(this.bindingCleanup.getNfInstanceId()).get() == 1);
        assertTrue(fullTableScan.getCounters().getCcScansFailedTotal(FailureReason.OTHER.toString(), this.bindingCleanup.getNfInstanceId()).get() == 0);
        assertTrue(fullTableScan.getCounters()
                                .getCcScansFailedTotal(FailureReason.BINDING_DATABASE_UNREACHABLE.toString(), this.bindingCleanup.getNfInstanceId())
                                .get() == 0);
        assertTrue(fullTableScan.getCounters().getCcScansFailedTotal(FailureReason.READ_FAILURE.toString(), this.bindingCleanup.getNfInstanceId()).get() == 0);
    }

    @Test(enabled = true, groups = "functest")
    private void testTwoPreservedValidBinding()
    {
        final var session = this.rxSession.sessionHolder().blockingGet();

        final var pcfId1 = "093f345e-f2df-11ec-b939-0242ac120003";
        final var pcfId2 = "093f345e-f2df-11ec-b939-0242ac120004";

        final var binding1 = generateTestBinding(IPV4_ADDR, null, null, null, null, null, null, PCF1_DIAM_HOST, pcfId1);
        final var binding2 = generateTestBinding(IPV4_ADDR, null, null, null, null, null, null, PCF2_DIAM_HOST, pcfId2);

        this.registerBinding(binding1, TTL_CONFIG_DEFAULT);
        this.registerBinding(binding2, TTL_CONFIG_DEFAULT);

        final var validPcfId1 = UUID.fromString(pcfId1);
        final var validPcfId2 = UUID.fromString(pcfId2);

        final var recTime1 = new RecoveryTime("2021-08-05T11:56:12.5Z");
        final var recTime2 = new RecoveryTime("2021-07-05T11:56:12.5Z");

        final var pcfRtService = new MockPcfRtService();
        pcfRtService.setPcfRt(validPcfId1, new PcfRt(validPcfId1, Instant.parse(recTime1.getRecoveryTimeStr())));
        pcfRtService.setPcfRt(validPcfId2, new PcfRt(validPcfId2, Instant.parse(recTime2.getRecoveryTimeStr())));

        final var paramsValues = new Builder().setPageSize(10).setPageThrottlingMillis(1).setDeleteThrottlingMillis(1).build();

        final var fullTableScan = new FullTableScanManager(pcfRtService, paramsValues, this.bindingCleanup);

        assertTrue(fullTableScan.getCounters().getCcBindingsScannedTotal(Optional.of(validPcfId1), bindingCleanup.getNfInstanceId()).get() == 0);
        assertTrue(fullTableScan.getCounters().getCcBindingsScannedTotal(Optional.of(validPcfId2), bindingCleanup.getNfInstanceId()).get() == 0);
        assertTrue(fullTableScan.getCounters().getCcBindingsStaleTotal(validPcfId1, this.bindingCleanup.getNfInstanceId()).get() == 0);
        assertTrue(fullTableScan.getCounters().getCcBindingsStaleTotal(validPcfId2, this.bindingCleanup.getNfInstanceId()).get() == 0);
        assertTrue(this.bindingCleanup.getCounters()
                                      .getCcStaleBindingsDeleted(Source.BINDING_DATABASE_SCAN.toString(),
                                                                 Reason.PCF_RECOVERY_TIME.toString(),
                                                                 validPcfId1,
                                                                 this.bindingCleanup.getNfInstanceId())
                                      .get() == 0);
        assertTrue(this.bindingCleanup.getCounters()
                                      .getCcStaleBindingsDeleted(Source.BINDING_DATABASE_SCAN.toString(),
                                                                 Reason.PCF_RECOVERY_TIME.toString(),
                                                                 validPcfId2,
                                                                 this.bindingCleanup.getNfInstanceId())
                                      .get() == 0);

        this.validateFullTableScanCountersAreZero(fullTableScan);

        fullTableScan.fullTableScan(session).blockingAwait();

        final var rs = session.getCqlSession().execute(SELECT_OPERATION);

        final var uuidElements = new ArrayList<>();
        rs.iterator().forEachRemaining(t -> uuidElements.add(t.getUuid("pcf_id")));

        assertEquals(uuidElements.size(), 2, "Expected 2 bindings");

        final var indexOfPcfId1 = uuidElements.indexOf(validPcfId1);
        assertEquals(uuidElements.get(indexOfPcfId1), validPcfId1, "Expected valid pcfId1 to be preserved");

        final var indexOfPcfId2 = uuidElements.indexOf(validPcfId2);
        assertEquals(uuidElements.get(indexOfPcfId2), validPcfId2, "Expected valid pcfId2 to be preserved");

        // Counter stepping validation
        assertTrue(fullTableScan.getCounters().getCcBindingsScannedTotal(Optional.of(validPcfId1), bindingCleanup.getNfInstanceId()).get() == 1);
        assertTrue(fullTableScan.getCounters().getCcBindingsScannedTotal(Optional.of(validPcfId2), bindingCleanup.getNfInstanceId()).get() == 1);
        assertTrue(fullTableScan.getCounters().getCcBindingsStaleTotal(validPcfId1, this.bindingCleanup.getNfInstanceId()).get() == 0);
        assertTrue(fullTableScan.getCounters().getCcBindingsStaleTotal(validPcfId2, this.bindingCleanup.getNfInstanceId()).get() == 0);

        assertTrue(this.bindingCleanup.getCounters()
                                      .getCcStaleBindingsDeleted(Source.BINDING_DATABASE_SCAN.toString(),
                                                                 Reason.PCF_RECOVERY_TIME.toString(),
                                                                 validPcfId1,
                                                                 this.bindingCleanup.getNfInstanceId())
                                      .get() == 0);
        assertTrue(this.bindingCleanup.getCounters()
                                      .getCcStaleBindingsDeleted(Source.BINDING_DATABASE_SCAN.toString(),
                                                                 Reason.PCF_RECOVERY_TIME.toString(),
                                                                 validPcfId2,
                                                                 this.bindingCleanup.getNfInstanceId())
                                      .get() == 0);

        assertTrue(fullTableScan.getCounters().getCcScansStartedTotal(this.bindingCleanup.getNfInstanceId()).get() == 1);
        assertTrue(fullTableScan.getCounters().getCcScansCompletedTotal(this.bindingCleanup.getNfInstanceId()).get() == 1);
        assertTrue(fullTableScan.getCounters().getCcScansFailedTotal(FailureReason.OTHER.toString(), this.bindingCleanup.getNfInstanceId()).get() == 0);
        assertTrue(fullTableScan.getCounters()
                                .getCcScansFailedTotal(FailureReason.BINDING_DATABASE_UNREACHABLE.toString(), this.bindingCleanup.getNfInstanceId())
                                .get() == 0);
        assertTrue(fullTableScan.getCounters().getCcScansFailedTotal(FailureReason.READ_FAILURE.toString(), this.bindingCleanup.getNfInstanceId()).get() == 0);
    }

    @Test(enabled = true, groups = "functest")
    private void testTwoDeletedStaleBindings()
    {
        final var session = this.rxSession.sessionHolder().blockingGet();

        final var pcfId1 = "093f345e-f2df-11ec-b939-0242ac120005";
        final var pcfId2 = "093f345e-f2df-11ec-b939-0242ac120006";

        final var binding1 = generateTestBinding(IPV4_ADDR, null, "IP_Domain1", null, null, null, null, PCF1_DIAM_HOST, pcfId1);
        final var binding2 = generateTestBinding(IPV4_ADDR, null, "IP_Domain1", null, null, null, null, PCF2_DIAM_HOST, pcfId2);

        this.registerBinding(binding1, TTL_CONFIG_DEFAULT);
        this.registerBinding(binding2, TTL_CONFIG_DEFAULT);

        final var stalePcfId1 = UUID.fromString(pcfId1);
        final var recTime1 = new RecoveryTime("2900-08-05T11:56:12.5Z");

        final var stalePcfId2 = UUID.fromString(pcfId2);
        final var recTime2 = new RecoveryTime("2900-08-04T11:56:12.5Z");

        final var pcfRtService = new MockPcfRtService();
        pcfRtService.setPcfRt(stalePcfId1, new PcfRt(stalePcfId1, Instant.parse(recTime1.getRecoveryTimeStr())));
        pcfRtService.setPcfRt(stalePcfId2, new PcfRt(stalePcfId2, Instant.parse(recTime2.getRecoveryTimeStr())));

        final var paramsValues = new FullTableScanParameters.Builder().setPageSize(10).setPageThrottlingMillis(1).setDeleteThrottlingMillis(1).build();

        final var fullTableScan = new FullTableScanManager(pcfRtService, paramsValues, this.bindingCleanup);

        assertTrue(fullTableScan.getCounters().getCcBindingsScannedTotal(Optional.of(stalePcfId1), bindingCleanup.getNfInstanceId()).get() == 0);
        assertTrue(fullTableScan.getCounters().getCcBindingsScannedTotal(Optional.of(stalePcfId2), bindingCleanup.getNfInstanceId()).get() == 0);
        assertTrue(fullTableScan.getCounters().getCcBindingsStaleTotal(stalePcfId1, this.bindingCleanup.getNfInstanceId()).get() == 0);
        assertTrue(fullTableScan.getCounters().getCcBindingsStaleTotal(stalePcfId2, this.bindingCleanup.getNfInstanceId()).get() == 0);

        assertTrue(this.bindingCleanup.getCounters()
                                      .getCcStaleBindingsDeleted(Source.BINDING_DATABASE_SCAN.toString(),
                                                                 Reason.PCF_RECOVERY_TIME.toString(),
                                                                 stalePcfId1,
                                                                 this.bindingCleanup.getNfInstanceId())
                                      .get() == 0);
        assertTrue(this.bindingCleanup.getCounters()
                                      .getCcStaleBindingsDeleted(Source.BINDING_DATABASE_SCAN.toString(),
                                                                 Reason.PCF_RECOVERY_TIME.toString(),
                                                                 stalePcfId2,
                                                                 this.bindingCleanup.getNfInstanceId())
                                      .get() == 0);

        this.validateFullTableScanCountersAreZero(fullTableScan);

        fullTableScan.fullTableScan(session).blockingAwait();

        final var rs = session.getCqlSession().execute(SELECT_OPERATION);

        final var uuidElements = new ArrayList<>();
        rs.iterator().forEachRemaining(t -> uuidElements.add(t.getUuid("pcf_id")));

        assertEquals(uuidElements.size(), 0, "Expected empty list");

        // Counters stepping validation
        assertTrue(fullTableScan.getCounters().getCcBindingsScannedTotal(Optional.of(stalePcfId1), bindingCleanup.getNfInstanceId()).get() == 1);
        assertTrue(fullTableScan.getCounters().getCcBindingsScannedTotal(Optional.of(stalePcfId2), bindingCleanup.getNfInstanceId()).get() == 1);
        assertTrue(fullTableScan.getCounters().getCcBindingsStaleTotal(stalePcfId1, this.bindingCleanup.getNfInstanceId()).get() == 1);
        assertTrue(fullTableScan.getCounters().getCcBindingsStaleTotal(stalePcfId2, this.bindingCleanup.getNfInstanceId()).get() == 1);

        assertTrue(this.bindingCleanup.getCounters()
                                      .getCcStaleBindingsDeleted(Source.BINDING_DATABASE_SCAN.toString(),
                                                                 Reason.PCF_RECOVERY_TIME.toString(),
                                                                 stalePcfId1,
                                                                 this.bindingCleanup.getNfInstanceId())
                                      .get() == 1);
        assertTrue(this.bindingCleanup.getCounters()
                                      .getCcStaleBindingsDeleted(Source.BINDING_DATABASE_SCAN.toString(),
                                                                 Reason.PCF_RECOVERY_TIME.toString(),
                                                                 stalePcfId2,
                                                                 this.bindingCleanup.getNfInstanceId())
                                      .get() == 1);

        assertTrue(fullTableScan.getCounters().getCcScansStartedTotal(this.bindingCleanup.getNfInstanceId()).get() == 1);
        assertTrue(fullTableScan.getCounters().getCcScansCompletedTotal(this.bindingCleanup.getNfInstanceId()).get() == 1);
        assertTrue(fullTableScan.getCounters().getCcScansFailedTotal(FailureReason.OTHER.toString(), this.bindingCleanup.getNfInstanceId()).get() == 0);
        assertTrue(fullTableScan.getCounters()
                                .getCcScansFailedTotal(FailureReason.BINDING_DATABASE_UNREACHABLE.toString(), this.bindingCleanup.getNfInstanceId())
                                .get() == 0);
        assertTrue(fullTableScan.getCounters().getCcScansFailedTotal(FailureReason.READ_FAILURE.toString(), this.bindingCleanup.getNfInstanceId()).get() == 0);
    }

    @Test(enabled = true, groups = "functest")
    private void testInvalidBsfSchema()
    {
        final var session = this.rxSession.sessionHolder().blockingGet();

        session.getCqlSession().execute(DROP_KEYSPACE_OPERATION);

        final var pcfRtService = new MockPcfRtService();
        final var paramsValues = new FullTableScanParameters.Builder().setPageSize(10).setPageThrottlingMillis(1).setDeleteThrottlingMillis(1).build();

        final var fullTableScan = new FullTableScanManager(pcfRtService, paramsValues, this.bindingCleanup);

        this.validateFullTableScanCountersAreZero(fullTableScan);

        try
        {
            fullTableScan.fullTableScan(session).blockingAwait();

        }
        catch (BindingDatabaseScanException ex)
        {
            assertTrue(ex.getReason().equals(FailureReason.BSF_SCHEMA_INVALID));
        }

        // Counters stepping validation
        assertTrue(fullTableScan.getCounters().getCcScansStartedTotal(this.bindingCleanup.getNfInstanceId()).get() == 1);
        assertTrue(fullTableScan.getCounters().getCcScansCompletedTotal(this.bindingCleanup.getNfInstanceId()).get() == 0);
        assertTrue(fullTableScan.getCounters().getCcScansFailedTotal(FailureReason.OTHER.toString(), this.bindingCleanup.getNfInstanceId()).get() == 1);
        assertTrue(fullTableScan.getCounters()
                                .getCcScansFailedTotal(FailureReason.BINDING_DATABASE_UNREACHABLE.toString(), this.bindingCleanup.getNfInstanceId())
                                .get() == 0);
        assertTrue(fullTableScan.getCounters().getCcScansFailedTotal(FailureReason.READ_FAILURE.toString(), this.bindingCleanup.getNfInstanceId()).get() == 0);

        final var success = new BsfSchemaHandler(this.rxSession, this.cassandraCfg).createAndVerifySchema(replicationFactorSettings).blockingGet();
        assertTrue(success);
    }

    @Test(enabled = true, groups = "functest")
    private void testCassandraUnreachable() throws InterruptedException
    {
        final var session = this.rxSession.sessionHolder().blockingGet();

        final var pcfRtService = new MockPcfRtService();
        final var paramsValues = new FullTableScanParameters.Builder().setPageSize(10).setPageThrottlingMillis(1).setDeleteThrottlingMillis(1).build();
        final var fullTableScan = new FullTableScanManager(pcfRtService, paramsValues, this.bindingCleanup);

        this.validateFullTableScanCountersAreZero(fullTableScan);

        this.testBed.stopCassandra();
        try
        {
            fullTableScan.fullTableScan(session).blockingAwait();
        }
        catch (BindingDatabaseScanException ex)
        {
            assertTrue(ex.getReason().equals(FailureReason.BINDING_DATABASE_UNREACHABLE));
        }

        // Counters stepping validation
        assertTrue(fullTableScan.getCounters().getCcScansStartedTotal(this.bindingCleanup.getNfInstanceId()).get() == 1);
        assertTrue(fullTableScan.getCounters().getCcScansCompletedTotal(this.bindingCleanup.getNfInstanceId()).get() == 0);
        assertTrue(fullTableScan.getCounters().getCcScansFailedTotal(FailureReason.OTHER.toString(), this.bindingCleanup.getNfInstanceId()).get() == 0);
        assertTrue(fullTableScan.getCounters()
                                .getCcScansFailedTotal(FailureReason.BINDING_DATABASE_UNREACHABLE.toString(), this.bindingCleanup.getNfInstanceId())
                                .get() == 1);
        assertTrue(fullTableScan.getCounters().getCcScansFailedTotal(FailureReason.READ_FAILURE.toString(), this.bindingCleanup.getNfInstanceId()).get() == 0);

        this.initializeTestEnvironment();
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

    private void startNbsfManagementService()
    {
        this.service = new NBsfManagementServiceImpl(this.rxSession, KEYSPACE, mbrConfigFlow.toFlowable(BackpressureStrategy.ERROR));
//        cleanup manager gets a Flowable of String representing nfInstanceId that is used to step a counter
//        since we are not checking this counter in this class, a dummy value is used.
        this.mbrConfigFlow.onNext(multipleBindingResolver);
        this.service.init().blockingAwait();
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

    private void registerBinding(final PcfBinding binding,
                                 final int ttl)
    {
        final var registerResult = service.register(binding, ttl).blockingGet();

        log.info("registerResult: {}", registerResult.getBindingId());
    }

    private void validateFullTableScanCountersAreZero(final FullTableScanManager fullTableScan)
    {
        assertTrue(fullTableScan.getCounters().getCcScansStartedTotal(this.bindingCleanup.getNfInstanceId()).get() == 0);
        assertTrue(fullTableScan.getCounters().getCcScansCompletedTotal(this.bindingCleanup.getNfInstanceId()).get() == 0);
        assertTrue(fullTableScan.getCounters().getCcScansFailedTotal(FailureReason.OTHER.toString(), this.bindingCleanup.getNfInstanceId()).get() == 0);
        assertTrue(fullTableScan.getCounters()
                                .getCcScansFailedTotal(FailureReason.BINDING_DATABASE_UNREACHABLE.toString(), this.bindingCleanup.getNfInstanceId())
                                .get() == 0);
        assertTrue(fullTableScan.getCounters().getCcScansFailedTotal(FailureReason.READ_FAILURE.toString(), this.bindingCleanup.getNfInstanceId()).get() == 0);
    }
}
