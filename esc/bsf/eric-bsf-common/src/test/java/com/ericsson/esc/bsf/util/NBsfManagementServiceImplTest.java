package com.ericsson.esc.bsf.util;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.net.Inet4Address;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.internal.core.auth.PlainTextAuthProvider;
import com.ericsson.esc.bsf.db.DbConfiguration;
import com.ericsson.esc.bsf.db.DbConfiguration.SizeTieredCompactionStrategy;
import com.ericsson.esc.bsf.db.metrics.CqlMessages;
import com.ericsson.esc.bsf.db.metrics.CqlRequests;
import com.ericsson.esc.bsf.db.metrics.MetricsConfigurator;
import com.ericsson.esc.bsf.db.metrics.Throttling;
import com.ericsson.esc.bsf.manager.BsfSchemaHandler;
import com.ericsson.esc.bsf.openapi.model.BindingLevel;
import com.ericsson.esc.bsf.openapi.model.DiscoveryQuery;
import com.ericsson.esc.bsf.openapi.model.IpEndPoint;
import com.ericsson.esc.bsf.openapi.model.Ipv6Prefix;
import com.ericsson.esc.bsf.openapi.model.MacAddr48;
import com.ericsson.esc.bsf.openapi.model.PcfBinding;
import com.ericsson.esc.bsf.openapi.model.Snssai;
import com.ericsson.esc.bsf.openapi.model.UeAddress;
import com.ericsson.esc.bsf.worker.BindingCleanupManager;
import com.ericsson.esc.bsf.worker.BsfQuery;
import com.ericsson.esc.bsf.worker.MultipleBindingResolver;
import com.ericsson.esc.bsf.worker.NBsfManagementService.DiscoveryResult.Status;
import com.ericsson.esc.bsf.worker.NBsfManagementService.RegisterResult;
import com.ericsson.esc.bsf.worker.NBsfManagementServiceImpl;
import com.ericsson.sc.bsf.model.Combination;
import com.ericsson.sc.bsf.model.HttpLookup;
import com.ericsson.sc.bsf.model.HttpLookup.ResolutionType;
import com.ericsson.sc.bsf.model.QueryParameterCombination;
import com.ericsson.utilities.cassandra.CassandraMetricsExporter;
import com.ericsson.utilities.cassandra.CassandraTestServer;
import com.ericsson.utilities.cassandra.RxSession;
import com.google.common.net.InetAddresses;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.vertx.ext.web.api.RequestParameter;
import io.vertx.ext.web.api.impl.RequestParameterImpl;
import io.vertx.ext.web.api.impl.RequestParametersImpl;

public class NBsfManagementServiceImplTest
{

    private static final Logger log = LoggerFactory.getLogger(NBsfManagementServiceImplTest.class);

    private static final String KEYSPACE = "nbsf_management_keyspace";
    private static final String IPV4_ADDR = "10.0.0.1";
    private static final String IP_DOMAIN = "IP_DOMAIN";
    private static final Ipv6Prefix IPV6_PREFIX = new Ipv6Prefix("2001:db8:a::123/64");
    private static final String IPV6_PREFIX_2 = new String("2001:db8:a:1::123/64");
    private static final String IPV6_PREFIX_3 = new String("2001:db8:a:2::123/64");
    private static final MacAddr48 MAC_ADDR_48 = new MacAddr48("10-65-30-69-45-8D");
    private static final String MAC_ADDR_48_2 = new String("10-65-30-69-46-8D");
    private static final String MAC_ADDR_48_3 = new String("10-65-30-69-47-8D");
    private static final String PCF1_DIAM_HOST = new String("pcf1.ericsson.com");
    private static final String PCF2_DIAM_HOST = new String("pcf2.ericsson.com");
    private static final String PCF3_DIAM_HOST = new String("pcf3.ericsson.com");
    private static final String TEST_PCF_DIAM_HOST = new String("testpcf.ericsson.com");
    private static final String TEST_PCF_DIAM_REALM = new String("ericsson.com");

    /* Parameters to be used for Binding Cleanup Mechanism (TTL) */

    private static final int TTL_CONFIG_DEFAULT = 720 * 3600;
    private static final int TTL_CONFIG_2SECS = 2;
    private static final int TTL_CONFIG_4SECS = 4;

    private static final String IPV4_ADDR1 = "10.1.1.1";
    private static final String IPV4_ADDR2 = "10.2.2.2";
    private static final String IPV4_ADDR3 = "10.3.3.3";
    private static final String IPV4_ADDR4 = "10.4.4.4";
    private static final Map<String, Object> replicationFactorSettings = Map.of("class", "NetworkTopologyStrategy", "datacenter1", 2);
    // "'datacenter1':2";

    private static final String LOCAL_DC_NAME = "datacenter1";

    private RxSession rxSession;
    private NBsfManagementServiceImpl service;
    private BindingCleanupManager bindingCleanup;
    private final CassandraTestServer testBed = new CassandraTestServer();
    private BehaviorSubject<MultipleBindingResolver> mbrConfigFlow = BehaviorSubject.create();
    private static final HttpLookup httpMostRecent = new HttpLookup().withResolutionType(ResolutionType.MOST_RECENT);
    private static final MultipleBindingResolver multipleBindingResolver = new MultipleBindingResolver(httpMostRecent);
    private DriverConfigLoader cassandraDriverConf;

    private DbConfiguration createDbConfiguration()
    {
        final var dbConfig = new DbConfiguration(KEYSPACE, List.of(testBed.getContactPoint()), "datacenter1");
        // dbConfig.setAdminCredentials(testBed.getUsername(), testBed.getPassword());
        dbConfig.setUserCredentials(testBed.getUsername(), testBed.getPassword());
        dbConfig.setConsistency("ONE");
        dbConfig.setUserCredentials("bsfUser", "bsfPassword");
        dbConfig.setGcGrace(864000);
        dbConfig.setCompactionStrategy(new SizeTieredCompactionStrategy(2.0, 0.5, 32, 3));
        return dbConfig;
    }

    @BeforeClass
    public void beforeClass()
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

        MetricsConfigurator metricsCfg = new MetricsConfigurator.MetricsConfiguratorBuilder("sessionName", "domainName").withCqlRequests(cqlRequests)
                                                                                                                        .withCqlMessages(cqlMessages)
                                                                                                                        .withThrottling(throttling)
                                                                                                                        .build();
        testBed.startCassandra();

        final var cassandraCfg = createDbConfiguration();

        this.cassandraDriverConf = DriverConfigLoader.programmaticBuilder() //
                                                     .withStringList(DefaultDriverOption.CONTACT_POINTS, Arrays.asList(testBed.getContactPoint()))
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
        rxSession = RxSession.builder().withConfig(this.cassandraDriverConf).build();
        rxSession.sessionHolder().blockingGet();

        CassandraMetricsExporter rxMetrics = new CassandraMetricsExporter(rxSession, "rxmetrics_domain");

        rxMetrics.start().blockingAwait();

        final var success = new BsfSchemaHandler(rxSession, cassandraCfg).createAndVerifySchema(replicationFactorSettings).blockingGet();
        assertTrue(success);

        restartNbsfManagementService();
        log.info("Initilization complete");
    }

    private void restartNbsfManagementService()
    {
        this.service = new NBsfManagementServiceImpl(rxSession, KEYSPACE, mbrConfigFlow.toFlowable(BackpressureStrategy.ERROR));
//        cleanup manager gets a Flowable of String representing nfInstanceId that is used to step a counter
//        since we are not checking this counter in this class, a dummy value is used.
        this.bindingCleanup = new BindingCleanupManager(this.rxSession, KEYSPACE, Flowable.just("unknown"));
        this.mbrConfigFlow.onNext(multipleBindingResolver);
        service.init().blockingAwait();
    }

    @AfterClass
    public void afterClass()
    {
        log.info("Closing testBed session");
        rxSession.close().blockingAwait();
        assertTrue(rxSession.sessionHolder().map(RxSession.SessionHolder::isClosed).blockingGet());
        testBed.stopCassandra();
    }

    /**
     * Tests that the new code changes due to DND-31845 work well with older
     * bindings.
     * 
     * Before these changes, when a non-primary key column had no value a cell was
     * inserted with null value. After the changes, the columns without values are
     * unset to avoid the creation of tombstones in Cassandra. This test discovers
     * an existing binding in the database that was inserted with null values.
     */
    @Test(groups = "functest", enabled = true)
    public void testSuccessfulDiscoveryBindingWithNullValues()
    {
        // Insert binding with null fields directly in the database.
        final var session = rxSession.sessionHolder().blockingGet();
        final var query = "INSERT INTO nbsf_management_keyspace.pcf_bindings (binding_id, matching_ue_address, dnn, ue_address, supi, gpsi, pcf_fqdn, pcf_ip_end_points, pcf_diam_host, pcf_diam_realm, snssai, pcf_id, recovery_time, add_ipv6_prefixes, add_mac_addrs,  pcf_set_id, bind_level)\n"
                          + "VALUES (363eb9f4-f2dd-11ec-b939-0242ac120002, {\"ipv4_addr\": '0.0.0.1', \"ipv6_prefix\": null, \"mac_addr48\": null, \"ip_domain\": null}, 'testDnn', {\"ipv4_addr\": '0.0.0.1', \"ipv6_prefix\": null, \"mac_addr48\": null, \"ip_domain\": null}, 'imsi-123410', 'msisdn-1234512', 'pcf.ericsson.se', [{\"ipv4_address\": '0.0.0.1',\"transport\": 'TCP',\"port\": 1024}], 'pcf-diamhost.com', 'pcf-diamrealm.com', {\"sst\":2,\"sd\":'DEADF0'}, 093f345e-f2df-11ec-b939-0242ac120002, null, null, null, null, null) ;";
        session.getCqlSession().execute(query);

        try
        {
            // IPv4 address.
            final var ipv4Address = (Inet4Address) InetAddresses.forString("0.0.0.1");
            var ueAddress = new UeAddress(ipv4Address, Optional.empty());
            log.info("Query with ueAddress: {}", ueAddress);

            // Discovery.
            var discovery_query = new DiscoveryQuery.UeAddr(ueAddress);
            var bindingFromDiscovery = this.service.discovery(discovery_query).blockingGet();

            log.info("result {}", bindingFromDiscovery);
            log.info("binding {}", bindingFromDiscovery.getPcfBinding().get());
        }
        finally
        {
            this.service.deregister(UUID.fromString("363eb9f4-f2dd-11ec-b939-0242ac120002")).blockingGet();
        }
    }

    @Test(groups = "functest", enabled = true)
    public void testSuccessRegisterSimple() throws InterruptedException
    {
        var testBindingIpv4 = generateTestBinding(IPV4_ADDR, null, null, null, null, null);
        var testBindingIpv6 = generateTestBinding(null, IPV6_PREFIX, null, null, null, null);
        var testBindingMac = generateTestBinding(null, null, null, MAC_ADDR_48, null, null);

        var registerResultIpv4 = registerBinding(testBindingIpv4, TTL_CONFIG_DEFAULT);
        var registerResultIpv6 = registerBinding(testBindingIpv6, TTL_CONFIG_DEFAULT);
        var registerResultMac = registerBinding(testBindingMac, TTL_CONFIG_DEFAULT);

        try
        {
            // Ipv4
            var ipv4Addr = registerResultIpv4.getPcfBinding().getIpv4Addr();

            var ueAddress = new UeAddress(ipv4Addr, Optional.empty());
            log.info("Query with ueAddress: {}", ueAddress);

            var query = new DiscoveryQuery.UeAddr(ueAddress);
            var bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.OK);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(testBindingIpv4));

            // Ipv6
            var ipv6Prefix = registerResultIpv6.getPcfBinding().getIpv6Prefix();

            ueAddress = new UeAddress(ipv6Prefix);
            log.info("Query with ueAddress: {}", ueAddress);

            query = new DiscoveryQuery.UeAddr(ueAddress);
            bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.OK);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(testBindingIpv6));

            // MAC
            var macAddr48 = registerResultMac.getPcfBinding().getMacAddr48();

            ueAddress = new UeAddress(macAddr48);
            log.info("Query with ueAddress: {}", ueAddress);

            query = new DiscoveryQuery.UeAddr(ueAddress);
            bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.OK);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(testBindingMac));
        }
        finally
        {
            service.deregister(registerResultIpv4.getBindingId()).blockingGet();
            service.deregister(registerResultIpv6.getBindingId()).blockingGet();
            service.deregister(registerResultMac.getBindingId()).blockingGet();
        }
    }

    @Test(groups = "functest", enabled = true)
    public void testSuccessRegisterIpv4IpDomain()
    {
        var testBinding = generateTestBinding(IPV4_ADDR, null, IP_DOMAIN, null, null, null);
        var registerResult = registerBinding(testBinding, TTL_CONFIG_DEFAULT);

        try
        {
            var ipv4Addr = registerResult.getPcfBinding().getIpv4Addr();
            var ipDomain = registerResult.getPcfBinding().getIpDomain();

            // Query only with ipv4.
            var ueAddress = new UeAddress(ipv4Addr, Optional.empty());
            log.info("Query with ueAddress: {}", ueAddress);

            var query = new DiscoveryQuery.UeAddr(ueAddress);
            var bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.OK);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(testBinding));

            // Query with ipv4 and ipDomain.
            ueAddress = new UeAddress(ipv4Addr, Optional.of(ipDomain));
            log.info("Query with ueAddress: {}", ueAddress);

            query = new DiscoveryQuery.UeAddr(ueAddress);
            bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.OK);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(testBinding));
        }
        finally
        {
            service.deregister(registerResult.getBindingId()).blockingGet();
        }
    }

    @Test(groups = "functest", enabled = true)
    public void testSuccessRegisterIpv4Ipv6()
    {
        var testBinding = generateTestBinding(IPV4_ADDR, IPV6_PREFIX, null, null, null, null);
        var registerResult = registerBinding(testBinding, TTL_CONFIG_DEFAULT);

        try
        {
            var ipv4Addr = registerResult.getPcfBinding().getIpv4Addr();
            var ipv6Prefix = registerResult.getPcfBinding().getIpv6Prefix();

            // Query only with ipv4.
            var ueAddress = new UeAddress(ipv4Addr, Optional.empty());
            log.info("Query with ueAddress: {}", ueAddress);

            var query = new DiscoveryQuery.UeAddr(ueAddress);
            var bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.OK);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(testBinding));

            // Query only with ipv6.
            ueAddress = new UeAddress(ipv6Prefix);
            log.info("Query with ueAddress: {}", ueAddress);

            query = new DiscoveryQuery.UeAddr(ueAddress);
            bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.OK);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(testBinding));

            // Query with both ipv4 & ipv6.
            ueAddress = new UeAddress(ipv4Addr, Optional.empty(), ipv6Prefix);
            log.info("Query with ueAddress: {}", ueAddress);

            query = new DiscoveryQuery.UeAddr(ueAddress);
            bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.NOT_FOUND);
        }
        finally
        {
            service.deregister(registerResult.getBindingId()).blockingGet();
        }
    }

    @Test(groups = "functest", enabled = true)
    public void testSuccessRegisterIpv4Ipv6IpDomain()
    {
        var testBinding = generateTestBinding(IPV4_ADDR, IPV6_PREFIX, IP_DOMAIN, null, null, null);
        var registerResult = registerBinding(testBinding, TTL_CONFIG_DEFAULT);

        try
        {
            var ipv4Addr = registerResult.getPcfBinding().getIpv4Addr();
            var ipv6Prefix = registerResult.getPcfBinding().getIpv6Prefix();
            var ipDomain = registerResult.getPcfBinding().getIpDomain();

            // Query only with ipv4.
            var ueAddress = new UeAddress(ipv4Addr, Optional.empty());
            log.info("Query with ueAddress: {}", ueAddress);

            var query = new DiscoveryQuery.UeAddr(ueAddress);
            var bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.OK);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(testBinding));

            // Query only with ipv6.
            ueAddress = new UeAddress(ipv6Prefix);
            log.info("Query with ueAddress: {}", ueAddress);

            query = new DiscoveryQuery.UeAddr(ueAddress);
            bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.OK);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(testBinding));

            // Query with both ipv4 & ipv6.
            ueAddress = new UeAddress(ipv4Addr, Optional.empty(), ipv6Prefix);
            log.info("Query with ueAddress: {}", ueAddress);

            query = new DiscoveryQuery.UeAddr(ueAddress);
            bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.NOT_FOUND);

            // Query with ipv4 & ipDomain.
            ueAddress = new UeAddress(ipv4Addr, Optional.of(ipDomain));
            log.info("Query with ueAddress: {}", ueAddress);

            query = new DiscoveryQuery.UeAddr(ueAddress);
            bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.OK);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(testBinding));

            // Query with ipv4, ipv6 & ipDomain.
            ueAddress = new UeAddress(ipv4Addr, Optional.of(ipDomain), ipv6Prefix);
            log.info("Query with ueAddress: {}", ueAddress);

            query = new DiscoveryQuery.UeAddr(ueAddress);
            bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.NOT_FOUND);
        }
        finally
        {
            service.deregister(registerResult.getBindingId()).blockingGet();
        }
    }

    @Test(groups = "functest", enabled = true)
    public void testSuccessRegisterAddIpv6Prefixes()
    {
        var testBindingIpv6 = generateTestBinding(null, IPV6_PREFIX, null, null, List.of(IPV6_PREFIX_2, IPV6_PREFIX_3), null);
        var registerResultIpv6 = registerBinding(testBindingIpv6, TTL_CONFIG_DEFAULT);

        try
        {
            // Ipv6
            var ipv6Prefix = registerResultIpv6.getPcfBinding().getIpv6Prefix();
            var ipv6addPrefix = registerResultIpv6.getPcfBinding().getAddIpv6Prefixes().get(0);

            // Query with ipv6 prefix that is stored inside ue_address.ipv6_prefix
            var ueAddress = new UeAddress(ipv6Prefix);
            log.info("Query with ueAddress: {}", ueAddress);

            var query = new DiscoveryQuery.UeAddr(ueAddress);
            var bindingFromDiscovery = this.service.discovery(query).blockingGet();
            log.info("discoveryResult: {}", bindingFromDiscovery);

            assertSame(bindingFromDiscovery.getResult(), Status.OK);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(testBindingIpv6));

            // Query with the first ipv6 prefix that is stored inside add_ipv6_prefixes
            ueAddress = new UeAddress(ipv6addPrefix);
            log.info("Query with ueAddress: {}", ueAddress);

            query = new DiscoveryQuery.UeAddr(ueAddress);
            bindingFromDiscovery = this.service.discovery(query).blockingGet();
            log.info("discoveryResult: {}", bindingFromDiscovery);

            assertSame(bindingFromDiscovery.getResult(), Status.OK);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(testBindingIpv6));

        }
        finally
        {
            service.deregister(registerResultIpv6.getBindingId()).blockingGet();
        }
    }

    @Test(groups = "functest", enabled = true)
    public void testSuccessRegisterAddMacAddrs()
    {
        var testBindingMac = generateTestBinding(null, null, null, MAC_ADDR_48, null, List.of(MAC_ADDR_48_2, MAC_ADDR_48_3));
        var registerResultMac = registerBinding(testBindingMac, TTL_CONFIG_DEFAULT);

        try
        {
            // MAC
            var macAddr48 = registerResultMac.getPcfBinding().getMacAddr48();
            var addMacAddr48 = registerResultMac.getPcfBinding().getAddMacAddrs().get(0);

            // Query with macAddr48 that is stored inside ue_address.mac_addr48
            var ueAddress = new UeAddress(macAddr48);
            log.info("Query with ueAddress: {}", ueAddress);

            var query = new DiscoveryQuery.UeAddr(ueAddress);
            var bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.OK);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(testBindingMac));

            // Query with the first macAddr48 that is stored inside add_mac_addrs
            ueAddress = new UeAddress(addMacAddr48);
            log.info("Query with ueAddress: {}", ueAddress);

            query = new DiscoveryQuery.UeAddr(ueAddress);
            bindingFromDiscovery = this.service.discovery(query).blockingGet();
            log.info("discoveryResult: {}", bindingFromDiscovery);

            assertSame(bindingFromDiscovery.getResult(), Status.OK);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(testBindingMac));
        }
        finally
        {
            service.deregister(registerResultMac.getBindingId()).blockingGet();
        }
    }

    @Test(groups = "functest", enabled = true)
    public void testSuccessDelete()
    {
        var testBinding = generateTestBinding(IPV4_ADDR, IPV6_PREFIX, IP_DOMAIN, null, null, null);
        var registerResult = registerBinding(testBinding, TTL_CONFIG_DEFAULT);

        try
        {
            var ipv4Addr = registerResult.getPcfBinding().getIpv4Addr();
            var ipDomain = registerResult.getPcfBinding().getIpDomain();

            // Query with ipv4 and ipDomain.
            var ueAddress = new UeAddress(ipv4Addr, Optional.of(ipDomain));
            log.info("Query with ueAddress: {}", ueAddress);

            var query = new DiscoveryQuery.UeAddr(ueAddress);
            var bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.OK);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(testBinding));

            // Deregister the binding
            this.service.deregister(registerResult.getBindingId()).blockingGet();

            // Check that it not exists.
            query = new DiscoveryQuery.UeAddr(ueAddress);
            bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.NOT_FOUND);
        }
        finally
        {
            this.service.deregister(registerResult.getBindingId()).blockingGet();
        }
    }

    @Test(groups = "functest", enabled = true)
    public void testSuccessfulDiscoveryUeAddr()
    {
        var testBinding = generateTestBinding(IPV4_ADDR, null, null, null, null, null);
        var registerResult = registerBinding(testBinding, TTL_CONFIG_DEFAULT);

        try
        {
            var ipv4Addr = registerResult.getPcfBinding().getIpv4Addr();
            var ueAddress = new UeAddress(ipv4Addr, Optional.empty());
            log.info("Query with ueAddress: {}", ueAddress);

            var query = new DiscoveryQuery.UeAddr(ueAddress);
            var bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.OK);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(testBinding));
        }
        finally
        {
            this.service.deregister(registerResult.getBindingId()).blockingGet();
        }
    }

    @Test(groups = "functest", enabled = true)
    public void testSuccessfulDiscoveryUeAddrDnn()
    {
        var testBinding = generateTestBinding(IPV4_ADDR, null, null, null, null, null);
        var registerResult = registerBinding(testBinding, TTL_CONFIG_DEFAULT);

        try
        {
            var ipv4Addr = registerResult.getPcfBinding().getIpv4Addr();
            var dnn = registerResult.getPcfBinding().getDnn();
            var ueAddress = new UeAddress(ipv4Addr, Optional.empty());
            log.info("Query with ueAddress: {}", ueAddress);
            log.info("Query with dnn: {}", dnn);

            var query = new DiscoveryQuery.UeAddrDnn(ueAddress, dnn);
            var bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.OK);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(testBinding));
        }
        finally
        {
            this.service.deregister(registerResult.getBindingId()).blockingGet();
        }
    }

    @Test(groups = "functest", enabled = true)
    public void testSuccessfulDiscoveryUeAddrDnnSnssai()
    {
        var testBinding = generateTestBinding(IPV4_ADDR, null, null, null, null, null);
        var registerResult = registerBinding(testBinding, TTL_CONFIG_DEFAULT);

        try
        {
            var ipv4Addr = registerResult.getPcfBinding().getIpv4Addr();
            var dnn = registerResult.getPcfBinding().getDnn();
            var snssai = registerResult.getPcfBinding().getSnssai();
            var ueAddress = new UeAddress(ipv4Addr, Optional.empty());
            log.info("Query with ueAddress: {}", ueAddress);
            log.info("Query with dnn: {}", dnn);
            log.info("Query with snssai: {}", snssai);

            var query = new DiscoveryQuery.UeAddrDnnSnssai(ueAddress, dnn, snssai);
            var bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.OK);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(testBinding));
        }
        finally
        {
            this.service.deregister(registerResult.getBindingId()).blockingGet();
        }
    }

    @Test(groups = "functest", enabled = true)
    public void testSuccessfulDiscoveryUeAddrSupi()
    {
        var testBinding = generateTestBinding(IPV4_ADDR, null, null, null, null, null);
        var registerResult = registerBinding(testBinding, TTL_CONFIG_DEFAULT);

        try
        {
            var ipv4Addr = registerResult.getPcfBinding().getIpv4Addr();
            var supi = registerResult.getPcfBinding().getSupi();
            var ueAddress = new UeAddress(ipv4Addr, Optional.empty());
            log.info("Query with ueAddress: {}", ueAddress);
            log.info("Query with supi: {}", supi);

            var query = new DiscoveryQuery.UeAddrSupi(ueAddress, supi);
            var bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.OK);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(testBinding));
        }
        finally
        {
            this.service.deregister(registerResult.getBindingId()).blockingGet();
        }
    }

    @Test(groups = "functest", enabled = true)
    public void testSuccessfulDiscoveryUeAddrSupiDnn()
    {
        var testBinding = generateTestBinding(IPV4_ADDR, null, null, null, null, null);
        var registerResult = registerBinding(testBinding, TTL_CONFIG_DEFAULT);

        try
        {
            var ipv4Addr = registerResult.getPcfBinding().getIpv4Addr();
            var supi = registerResult.getPcfBinding().getSupi();
            var dnn = registerResult.getPcfBinding().getDnn();
            var ueAddress = new UeAddress(ipv4Addr, Optional.empty());
            log.info("Query with ueAddress: {}", ueAddress);
            log.info("Query with supi: {}", supi);
            log.info("Query with dnn: {}", dnn);

            var query = new DiscoveryQuery.UeAddrSupiDnn(ueAddress, supi, dnn);
            var bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.OK);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(testBinding));
        }
        finally
        {
            this.service.deregister(registerResult.getBindingId()).blockingGet();
        }
    }

    @Test(groups = "functest", enabled = true)
    public void testSuccessfulDiscoveryUeAddrSupiDnnSnssai()
    {
        var testBinding = generateTestBinding(IPV4_ADDR, null, null, null, null, null);
        var registerResult = registerBinding(testBinding, TTL_CONFIG_DEFAULT);

        try
        {
            var ipv4Addr = registerResult.getPcfBinding().getIpv4Addr();
            var supi = registerResult.getPcfBinding().getSupi();
            var dnn = registerResult.getPcfBinding().getDnn();
            var snssai = registerResult.getPcfBinding().getSnssai();
            var ueAddress = new UeAddress(ipv4Addr, Optional.empty());
            log.info("Query with ueAddress: {}", ueAddress);
            log.info("Query with supi: {}", supi);
            log.info("Query with dnn: {}", dnn);
            log.info("Query with snssai: {}", snssai);

            var query = new DiscoveryQuery.UeAddrSupiDnnSnssai(ueAddress, supi, dnn, snssai);
            var bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.OK);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(testBinding));
        }
        finally
        {
            this.service.deregister(registerResult.getBindingId()).blockingGet();
        }
    }

    @Test(groups = "functest", enabled = true)
    public void testSuccessfulDiscoveryUeAddrGpsi()
    {
        var testBinding = generateTestBinding(IPV4_ADDR, null, null, null, null, null);
        var registerResult = registerBinding(testBinding, TTL_CONFIG_DEFAULT);

        try
        {
            var ipv4Addr = registerResult.getPcfBinding().getIpv4Addr();
            var gpsi = registerResult.getPcfBinding().getGpsi();
            var ueAddress = new UeAddress(ipv4Addr, Optional.empty());
            log.info("Query with ueAddress: {}", ueAddress);
            log.info("Query with gpsi: {}", gpsi);

            var query = new DiscoveryQuery.UeAddrGpsi(ueAddress, gpsi);
            var bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.OK);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(testBinding));
        }
        finally
        {
            this.service.deregister(registerResult.getBindingId()).blockingGet();
        }
    }

    @Test(groups = "functest", enabled = true)
    public void testSuccessfulDiscoveryUeAddrGpsiDnn()
    {
        var testBinding = generateTestBinding(IPV4_ADDR, null, null, null, null, null);
        var registerResult = registerBinding(testBinding, TTL_CONFIG_DEFAULT);

        try
        {
            var ipv4Addr = registerResult.getPcfBinding().getIpv4Addr();
            var gpsi = registerResult.getPcfBinding().getGpsi();
            var dnn = registerResult.getPcfBinding().getDnn();
            var ueAddress = new UeAddress(ipv4Addr, Optional.empty());
            log.info("Query with ueAddress: {}", ueAddress);
            log.info("Query with gpsi: {}", gpsi);
            log.info("Query with dnn: {}", dnn);

            var query = new DiscoveryQuery.UeAddrGpsiDnn(ueAddress, gpsi, dnn);
            var bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.OK);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(testBinding));
        }
        finally
        {
            this.service.deregister(registerResult.getBindingId()).blockingGet();
        }
    }

    @Test(groups = "functest", enabled = true)
    public void testSuccessfulDiscoveryUeAddrGpsiDnnSnssai()
    {
        var testBinding = generateTestBinding(IPV4_ADDR, null, null, null, null, null);
        var registerResult = registerBinding(testBinding, TTL_CONFIG_DEFAULT);

        try
        {
            var ipv4Addr = registerResult.getPcfBinding().getIpv4Addr();
            var gpsi = registerResult.getPcfBinding().getGpsi();
            var dnn = registerResult.getPcfBinding().getDnn();
            var snssai = registerResult.getPcfBinding().getSnssai();
            var ueAddress = new UeAddress(ipv4Addr, Optional.empty());
            log.info("Query with ueAddress: {}", ueAddress);
            log.info("Query with gpsi: {}", gpsi);
            log.info("Query with dnn: {}", dnn);
            log.info("Query with snssai: {}", snssai);

            var query = new DiscoveryQuery.UeAddrGpsiDnnSnssai(ueAddress, gpsi, dnn, snssai);
            var bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.OK);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(testBinding));
        }
        finally
        {
            this.service.deregister(registerResult.getBindingId()).blockingGet();
        }
    }

    @Test(groups = "functest", enabled = true)
    public void testSuccessfulDiscoveryMultipleUeAddr()
    {
        var testBinding1 = generateTestBinding(IPV4_ADDR, null, null, null, null, null, null, PCF1_DIAM_HOST);
        var testBinding2 = generateTestBinding(IPV4_ADDR, null, null, null, null, null, null, PCF2_DIAM_HOST);
        var testBinding3 = generateTestBinding(IPV4_ADDR, null, null, null, null, null, null, PCF3_DIAM_HOST);
        var registerResult1 = registerBinding(testBinding1, TTL_CONFIG_DEFAULT);
        var registerResult2 = registerBinding(testBinding2, TTL_CONFIG_DEFAULT);
        var registerResult3 = registerBinding(testBinding3, TTL_CONFIG_DEFAULT);

        try
        {
            var ipv4Addr = registerResult1.getPcfBinding().getIpv4Addr();
            var ueAddress = new UeAddress(ipv4Addr, Optional.empty());
            log.info("Query with ueAddress: {}", ueAddress);

            var query = new DiscoveryQuery.UeAddr(ueAddress);
            var bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.OK_MULTIPLE);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(testBinding3));
        }
        finally
        {
            this.service.deregister(registerResult1.getBindingId()).blockingGet();
            this.service.deregister(registerResult2.getBindingId()).blockingGet();
            this.service.deregister(registerResult3.getBindingId()).blockingGet();
        }
    }

    @Test(groups = "functest", enabled = true)
    public void testFailedDiscoveryUeAddrDnn()
    {
        var testBinding = generateTestBinding(IPV4_ADDR, null, null, null, null, null);
        var registerResult = registerBinding(testBinding, TTL_CONFIG_DEFAULT);

        try
        {
            var ipv4Addr = registerResult.getPcfBinding().getIpv4Addr();
            var dnn = "dnnError";
            var ueAddress = new UeAddress(ipv4Addr, Optional.empty());
            log.info("Query with ueAddress: {}", ueAddress);
            log.info("Query with dnn: {}", dnn);

            var query = new DiscoveryQuery.UeAddrDnn(ueAddress, dnn);
            var bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.NOT_FOUND);
        }
        finally
        {
            this.service.deregister(registerResult.getBindingId()).blockingGet();
        }
    }

    @Test(groups = "functest", enabled = true)
    public void testFailedDiscoveryUeAddrSupiDnnSnssai()
    {
        var testBinding = generateTestBinding(IPV4_ADDR, null, null, null, null, null);
        var registerResult = registerBinding(testBinding, TTL_CONFIG_DEFAULT);

        try
        {
            var ipv4Addr = registerResult.getPcfBinding().getIpv4Addr();
            var supi = "supiError";
            var dnn = registerResult.getPcfBinding().getDnn();
            var snssai = registerResult.getPcfBinding().getSnssai();
            var ueAddress = new UeAddress(ipv4Addr, Optional.empty());
            log.info("Query with ueAddress: {}", ueAddress);
            log.info("Query with supi: {}", supi);
            log.info("Query with dnn: {}", dnn);
            log.info("Query with snssai: {}", snssai);

            var query = new DiscoveryQuery.UeAddrSupiDnnSnssai(ueAddress, supi, dnn, snssai);
            var bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.NOT_FOUND);
        }
        finally
        {
            this.service.deregister(registerResult.getBindingId()).blockingGet();
        }
    }

    @Test(groups = "functest", enabled = true)
    public void testFailedDiscoveryUeAddrGpsi()
    {
        var testBinding = generateTestBinding(IPV4_ADDR, null, null, null, null, null);
        var registerResult = registerBinding(testBinding, TTL_CONFIG_DEFAULT);

        try
        {
            var ipv4Addr = registerResult.getPcfBinding().getIpv4Addr();
            var gpsi = "gpsiError";
            var ueAddress = new UeAddress(ipv4Addr, Optional.empty());
            log.info("Query with ueAddress: {}", ueAddress);
            log.info("Query with gpsi: {}", gpsi);

            var query = new DiscoveryQuery.UeAddrGpsi(ueAddress, gpsi);
            var bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.NOT_FOUND);
        }
        finally
        {
            this.service.deregister(registerResult.getBindingId()).blockingGet();
        }
    }

    @Test(groups = "functest", enabled = true)
    public void testBindingsPresenceAfterRegisterTtl4SecSleep2Sec()
    {

        var testBindingIpv4_1 = generateTestBinding(IPV4_ADDR1, null, null, null, null, null);
        var testBindingIpv4Ipv6_2 = generateTestBinding(IPV4_ADDR2, IPV6_PREFIX, null, null, null, null);
        var testBindingIpv4IpDomain_3 = generateTestBinding(IPV4_ADDR3, null, IP_DOMAIN, null, null, null);
        var testBindingIpv4Ipv6IpDomain_4 = generateTestBinding(IPV4_ADDR4, IPV6_PREFIX, IP_DOMAIN, null, null, null);
        var testBindingMac_5 = generateTestBinding(null, null, null, MAC_ADDR_48, null, null);

        var registerResultIpv4Ttl4 = registerBinding(testBindingIpv4_1, TTL_CONFIG_4SECS);
        var registerResultIpv4Ipv6Ttl4 = registerBinding(testBindingIpv4Ipv6_2, TTL_CONFIG_4SECS);
        var registerResultIpv4IpDomainTtl4 = registerBinding(testBindingIpv4IpDomain_3, TTL_CONFIG_4SECS);
        var registerResultIpv4Ipv6IpDomainTtl4 = registerBinding(testBindingIpv4Ipv6IpDomain_4, TTL_CONFIG_4SECS);
        var registerResultMacTtl4 = registerBinding(testBindingMac_5, TTL_CONFIG_4SECS);

        try
        {
            var ipv4Addr1 = registerResultIpv4Ttl4.getPcfBinding().getIpv4Addr();
            var ipv4Addr2 = registerResultIpv4Ipv6Ttl4.getPcfBinding().getIpv4Addr();
            var ipv4Addr3 = registerResultIpv4IpDomainTtl4.getPcfBinding().getIpv4Addr();
            var ipv4Addr4 = registerResultIpv4Ipv6IpDomainTtl4.getPcfBinding().getIpv4Addr();
            var macAddr5 = registerResultMacTtl4.getPcfBinding().getMacAddr48();

            var ueAddress1 = new UeAddress(ipv4Addr1, Optional.empty());
            var ueAddress2 = new UeAddress(ipv4Addr2, Optional.empty());
            var ueAddress3 = new UeAddress(ipv4Addr3, Optional.empty());
            var ueAddress4 = new UeAddress(ipv4Addr4, Optional.empty());
            var ueAddress5 = new UeAddress(macAddr5);

            log.info("Query with ueAddress: {}", ueAddress1);
            var query = new DiscoveryQuery.UeAddr(ueAddress1);
            var bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.OK);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(testBindingIpv4_1));

            log.info("Query with ueAddress: {}", ueAddress2);
            query = new DiscoveryQuery.UeAddr(ueAddress2);
            bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.OK);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(testBindingIpv4Ipv6_2));

            log.info("Query with ueAddress: {}", ueAddress3);
            query = new DiscoveryQuery.UeAddr(ueAddress3);
            bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.OK);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(testBindingIpv4IpDomain_3));

            log.info("Query with ueAddress: {}", ueAddress4);
            query = new DiscoveryQuery.UeAddr(ueAddress4);
            bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.OK);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(testBindingIpv4Ipv6IpDomain_4));

            log.info("Query with ueAddress: {}", ueAddress5);
            query = new DiscoveryQuery.UeAddr(ueAddress5);
            bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.OK);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(testBindingMac_5));

            // Wait 2 seconds for the possible removal of Bindings according to it's TTL
            // value
            Observable.timer(2, TimeUnit.SECONDS).blockingFirst();

            log.info("Query with ueAddress: {}", ueAddress1);
            query = new DiscoveryQuery.UeAddr(ueAddress1);
            bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.OK);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(testBindingIpv4_1));

            log.info("Query with ueAddress: {}", ueAddress2);
            query = new DiscoveryQuery.UeAddr(ueAddress2);
            bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.OK);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(testBindingIpv4Ipv6_2));

            log.info("Query with ueAddress: {}", ueAddress3);
            query = new DiscoveryQuery.UeAddr(ueAddress3);
            bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.OK);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(testBindingIpv4IpDomain_3));

            log.info("Query with ueAddress: {}", ueAddress4);
            query = new DiscoveryQuery.UeAddr(ueAddress4);
            bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.OK);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(testBindingIpv4Ipv6IpDomain_4));

            log.info("Query with ueAddress: {}", ueAddress5);
            query = new DiscoveryQuery.UeAddr(ueAddress5);
            bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.OK);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(testBindingMac_5));

        }

        finally
        {
            service.deregister(registerResultIpv4Ttl4.getBindingId()).blockingGet();
            service.deregister(registerResultIpv4Ipv6Ttl4.getBindingId()).blockingGet();
            service.deregister(registerResultIpv4IpDomainTtl4.getBindingId()).blockingGet();
            service.deregister(registerResultIpv4Ipv6IpDomainTtl4.getBindingId()).blockingGet();
            service.deregister(registerResultMacTtl4.getBindingId()).blockingGet();
        }

    }

    @Test(groups = "functest", enabled = true)
    public void testBindingsCleanupAfterRegisterTtl2SecSleep4Sec()
    {

        var testBindingIpv4_1 = generateTestBinding(IPV4_ADDR1, null, null, null, null, null);
        var testBindingIpv4Ipv6_2 = generateTestBinding(IPV4_ADDR2, IPV6_PREFIX, null, null, null, null);
        var testBindingIpv4IpDomain_3 = generateTestBinding(IPV4_ADDR3, null, IP_DOMAIN, null, null, null);
        var testBindingIpv4Ipv6IpDomain_4 = generateTestBinding(IPV4_ADDR4, IPV6_PREFIX, IP_DOMAIN, null, null, null);
        var testBindingMac_5 = generateTestBinding(null, null, null, MAC_ADDR_48, null, null);

        var registerResultIpv4Ttl2 = registerBinding(testBindingIpv4_1, TTL_CONFIG_2SECS);
        var registerResultIpv4Ipv6Ttl2 = registerBinding(testBindingIpv4Ipv6_2, TTL_CONFIG_2SECS);
        var registerResultIpv4IpDomainTtl2 = registerBinding(testBindingIpv4IpDomain_3, TTL_CONFIG_2SECS);
        var registerResultIpv4Ipv6IpDomainTtl2 = registerBinding(testBindingIpv4Ipv6IpDomain_4, TTL_CONFIG_2SECS);
        var registerResultMacTtl2 = registerBinding(testBindingMac_5, TTL_CONFIG_2SECS);

        try
        {
            var ipv4Addr1 = registerResultIpv4Ttl2.getPcfBinding().getIpv4Addr();
            var ipv4Addr2 = registerResultIpv4Ipv6Ttl2.getPcfBinding().getIpv4Addr();
            var ipv4Addr3 = registerResultIpv4IpDomainTtl2.getPcfBinding().getIpv4Addr();
            var ipv4Addr4 = registerResultIpv4Ipv6IpDomainTtl2.getPcfBinding().getIpv4Addr();
            var macAddr5 = registerResultMacTtl2.getPcfBinding().getMacAddr48();

            var ueAddress1 = new UeAddress(ipv4Addr1, Optional.empty());
            var ueAddress2 = new UeAddress(ipv4Addr2, Optional.empty());
            var ueAddress3 = new UeAddress(ipv4Addr3, Optional.empty());
            var ueAddress4 = new UeAddress(ipv4Addr4, Optional.empty());
            var ueAddress5 = new UeAddress(macAddr5);

            log.info("Query with ueAddress: {}", ueAddress1);
            var query = new DiscoveryQuery.UeAddr(ueAddress1);
            var bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.OK);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(testBindingIpv4_1));

            log.info("Query with ueAddress: {}", ueAddress2);
            query = new DiscoveryQuery.UeAddr(ueAddress2);
            bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.OK);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(testBindingIpv4Ipv6_2));

            log.info("Query with ueAddress: {}", ueAddress3);
            query = new DiscoveryQuery.UeAddr(ueAddress3);
            bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.OK);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(testBindingIpv4IpDomain_3));

            log.info("Query with ueAddress: {}", ueAddress4);
            query = new DiscoveryQuery.UeAddr(ueAddress4);
            bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.OK);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(testBindingIpv4Ipv6IpDomain_4));

            log.info("Query with ueAddress: {}", ueAddress5);
            query = new DiscoveryQuery.UeAddr(ueAddress5);
            bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.OK);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(testBindingMac_5));

            // Wait 4 seconds for the removal of Bindings according to it's TTL value
            Observable.timer(4, TimeUnit.SECONDS).blockingFirst();

            log.info("Query with ueAddress: {}", ueAddress1);
            query = new DiscoveryQuery.UeAddr(ueAddress1);
            bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.NOT_FOUND);

            log.info("Query with ueAddress: {}", ueAddress2);
            query = new DiscoveryQuery.UeAddr(ueAddress2);
            bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.NOT_FOUND);

            log.info("Query with ueAddress: {}", ueAddress3);
            query = new DiscoveryQuery.UeAddr(ueAddress3);
            bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.NOT_FOUND);

            log.info("Query with ueAddress: {}", ueAddress4);
            query = new DiscoveryQuery.UeAddr(ueAddress4);
            bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.NOT_FOUND);

            log.info("Query with ueAddress: {}", ueAddress5);
            query = new DiscoveryQuery.UeAddr(ueAddress5);
            bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.NOT_FOUND);

        }
        finally
        {
            service.deregister(registerResultIpv4Ttl2.getBindingId()).blockingGet();
            service.deregister(registerResultIpv4Ipv6Ttl2.getBindingId()).blockingGet();
            service.deregister(registerResultIpv4IpDomainTtl2.getBindingId()).blockingGet();
            service.deregister(registerResultIpv4Ipv6IpDomainTtl2.getBindingId()).blockingGet();
            service.deregister(registerResultMacTtl2.getBindingId()).blockingGet();
        }

    }

    @Test(groups = "functest", enabled = true)
    public void testSuccessIpv6prefix128Discovery()
    {

        var testBindingIpv6 = generateTestBinding(null, IPV6_PREFIX, null, null, null, null);
        var registerResultIpv6 = registerBinding(testBindingIpv6, TTL_CONFIG_DEFAULT);

        try
        {

            Map<String, RequestParameter> queryParams = new HashMap<>();
            queryParams.put("ipv6Prefix", new RequestParameterImpl("ipv6Prefix", "2001:db8:a:0:10:2:30:4/128"));

            var rps = new RequestParametersImpl();
            rps.setQueryParameters(queryParams);

            var query = DiscoveryQuery.fromQueryParameters(rps);

            var bindingFromDiscovery = this.service.discovery(query).blockingGet();

            assertSame(bindingFromDiscovery.getResult(), Status.OK);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(testBindingIpv6));

        }
        finally
        {
            service.deregister(registerResultIpv6.getBindingId()).blockingGet();
        }
    }

    /*
     * Multiple binding resolution TCs
     */

    @Test(groups = "functest", enabled = true)
    public void testMostRecentConditional() throws InterruptedException
    {
//      Set Multiple Binding resolution config
//      Most Resent conditional with combination IPv4 and DNN

        List<Combination> queryParameterCombination = new ArrayList<>();
        queryParameterCombination.add(Combination.IPV_4_ADDR);
        queryParameterCombination.add(Combination.DNN);

        List<QueryParameterCombination> queryParameterCombinations = new ArrayList<>();
        queryParameterCombinations.add(new QueryParameterCombination().withName("IPv4AndDnn").withCombination(queryParameterCombination));

        final var httpMostRecent = new HttpLookup().withResolutionType(ResolutionType.MOST_RECENT_CONDITIONAL)
                                                   .withQueryParameterCombination(queryParameterCombinations);

        MultipleBindingResolver conditionalRecentResolver = new MultipleBindingResolver(httpMostRecent);
        this.mbrConfigFlow.onNext(conditionalRecentResolver);

//      Create and push bindings
        var binding1 = generateTestBinding(IPV4_ADDR, null, null, "Dnn1", null, null, null, PCF1_DIAM_HOST);
        var binding2 = generateTestBinding(IPV4_ADDR, null, null, "Dnn1", null, null, null, PCF2_DIAM_HOST);
        var binding3 = generateTestBinding(IPV4_ADDR1, null, null, null, null, null);

        var registerResult1 = registerBinding(binding1, TTL_CONFIG_DEFAULT);
        var registerResult2 = registerBinding(binding2, TTL_CONFIG_DEFAULT);
        var registerResult3 = registerBinding(binding3, TTL_CONFIG_DEFAULT);

        var duplicateIpv4 = registerResult1.getPcfBinding().getIpv4Addr();
        var dnn1 = registerResult1.getPcfBinding().getDnn();
        var duplicateUeAddress = new UeAddress(duplicateIpv4, Optional.empty());

        var uniqueIpv4 = registerResult3.getPcfBinding().getIpv4Addr();
        var uniqueUeAddress = new UeAddress(uniqueIpv4, Optional.empty());

        try
        {
//          discover with only ipv4. There are 2 bindings with the same IP, so an error is expected since the configured condition is for both ipv4 and dnn
            var query = new DiscoveryQuery.UeAddr(duplicateUeAddress);
            var bindingFromDiscovery = this.service.discovery(query).blockingGet();
            assertSame(bindingFromDiscovery.getResult(), Status.REJECT_MULTIPLE);

//          discover with only ipv4. There is only 1 binding with that IP, so the discovery should be successful
            query = new DiscoveryQuery.UeAddr(uniqueUeAddress);
            bindingFromDiscovery = this.service.discovery(query).blockingGet();
            assertSame(bindingFromDiscovery.getResult(), Status.OK);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(binding3));

//          discover with ipv4 and ip_domain. There are 2 bindings with the same IP and dnn, so the discovery should return most recent (Binding2)
            var addrDnnQuery = new DiscoveryQuery.UeAddrDnn(duplicateUeAddress, dnn1);
            bindingFromDiscovery = this.service.discovery(addrDnnQuery).blockingGet();
//            assert stale binding is the binding1
            assertEquals(registerResult1.getBindingId(), bindingFromDiscovery.getStaleBindings().get(0).getBindingId());
//            trigger delete
            bindingFromDiscovery.getStaleBindings().forEach(bindingCleanup::deleteBindingAsync);
            Thread.sleep(1000);
            assertSame(bindingFromDiscovery.getResult(), Status.OK_MULTIPLE);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(binding2));

            this.mbrConfigFlow.onNext(new MultipleBindingResolver(new HttpLookup().withResolutionType(ResolutionType.REJECT)
                                                                                  .withQueryParameterCombination(queryParameterCombinations)));

//          discover with ipv4 and ip_domain. Due to previous step, duplicate binding has been deleted. Thus, same query returns a single binding only
//            this.service.getBindiningCleanupManager().stop().blockingAwait(); // Stop cleanup service to ensure that queues have drained
            bindingFromDiscovery = this.service.discovery(addrDnnQuery).blockingGet();
            assertSame(bindingFromDiscovery.getResult(), Status.OK);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(binding2));
        }
        finally
        {
            restartNbsfManagementService(); // Since cleanup service was stopped, service no longer works properly
            service.deregister(registerResult1.getBindingId()).blockingGet();
            service.deregister(registerResult2.getBindingId()).blockingGet();
            service.deregister(registerResult3.getBindingId()).blockingGet();
            this.mbrConfigFlow.onNext(multipleBindingResolver);
        }

    }

    @Test(groups = "functest", enabled = true)
    public void testMostRecentConditionalIpDomain() throws InterruptedException
    {
//      Set Multiple Binding resolution config
//      Most Resent conditional with combination IPv4 and IP_Domain

        List<Combination> queryParameterCombination = new ArrayList<>();
        queryParameterCombination.add(Combination.IPV_4_ADDR);
        queryParameterCombination.add(Combination.IP_DOMAIN);

        List<QueryParameterCombination> queryParameterCombinations = new ArrayList<>();
        queryParameterCombinations.add(new QueryParameterCombination().withName("IPv4AndDomain").withCombination(queryParameterCombination));

        HttpLookup httpMostRecent = new HttpLookup().withResolutionType(ResolutionType.MOST_RECENT_CONDITIONAL)
                                                    .withQueryParameterCombination(queryParameterCombinations);

        MultipleBindingResolver conditionalRecentResolver = new MultipleBindingResolver(httpMostRecent);
        this.mbrConfigFlow.onNext(conditionalRecentResolver);

//      Create and push bindings
        var binding1 = generateTestBinding(IPV4_ADDR, null, "IP_Domain1", null, null, null, null, PCF1_DIAM_HOST);
        var binding2 = generateTestBinding(IPV4_ADDR, null, "IP_Domain1", null, null, null, null, PCF2_DIAM_HOST);
        var binding3 = generateTestBinding(IPV4_ADDR, null, "IP_Domain1", null, null, null, null, PCF3_DIAM_HOST);

        var registerResult1 = registerBinding(binding1, TTL_CONFIG_DEFAULT);
        var registerResult2 = registerBinding(binding2, TTL_CONFIG_DEFAULT);
        var registerResult3 = registerBinding(binding3, TTL_CONFIG_DEFAULT);

        var duplicateIpv4 = registerResult1.getPcfBinding().getIpv4Addr();
        var duplicateUeAddress = new UeAddress(duplicateIpv4, Optional.of("IP_Domain1"));

        try
        {

//          discover with ipv4 and ip_domain. There are 3 bindings with the same IP and IP_Domain, so the discovery should return the most recent (Binding3)
            var query = new DiscoveryQuery.UeAddr(duplicateUeAddress);
            var bindingFromDiscovery = this.service.discovery(query).blockingGet();

            log.info("Discovery Result: {}", bindingFromDiscovery);
            assertSame(bindingFromDiscovery.getResult(), Status.OK_MULTIPLE);
            assertTrue(bindingFromDiscovery.getPcfBinding().get().equals(binding3));
        }
        finally
        {
            service.deregister(registerResult1.getBindingId()).blockingGet();
            service.deregister(registerResult2.getBindingId()).blockingGet();
            service.deregister(registerResult3.getBindingId()).blockingGet();
            this.mbrConfigFlow.onNext(multipleBindingResolver);
        }

    }

    @Test(groups = "functest", enabled = true)
    public void testMostRecentConditionalAlwaysLatest() throws InterruptedException
    {
//      Set Multiple Binding resolution config
//      Most Resent conditional with combination IPv4 and IP_Domain

        List<Combination> queryParameterCombination = new ArrayList<>();
        queryParameterCombination.add(Combination.IPV_4_ADDR);
        queryParameterCombination.add(Combination.IP_DOMAIN);

        List<QueryParameterCombination> queryParameterCombinations = new ArrayList<>();
        queryParameterCombinations.add(new QueryParameterCombination().withName("IPv4AndDomain").withCombination(queryParameterCombination));

        HttpLookup httpMostRecent = new HttpLookup().withResolutionType(ResolutionType.MOST_RECENT_CONDITIONAL)
                                                    .withQueryParameterCombination(queryParameterCombinations);

        MultipleBindingResolver conditionalRecentResolver = new MultipleBindingResolver(httpMostRecent);
        this.mbrConfigFlow.onNext(conditionalRecentResolver);

        final int numOfBindings = BsfQuery.LIMIT_WITH_WRITE_TIME;
        List<RegisterResult> registerResults = new ArrayList<>();

//      Create and push bindings
        for (int i = 0; i < numOfBindings; i++)
        {
            var binding = generateTestBinding(IPV4_ADDR, null, "IP_Domain1", null, null, null, null, new String("pcfDiamHost" + i + ".ericsson.com"));

            var registerResult = registerBinding(binding, TTL_CONFIG_DEFAULT);
            registerResults.add(registerResult);
        }

        var duplicateIpv4 = registerResults.get(0).getPcfBinding().getIpv4Addr();
        var duplicateUeAddress = new UeAddress(duplicateIpv4, Optional.of("IP_Domain1"));

        try
        {

//          discover with ipv4 and ip_domain. There are multiple bindings with the same IP and IP_Domain, so the discovery should return the most recent (the last one of the list)
            var query = new DiscoveryQuery.UeAddr(duplicateUeAddress);
            var bindingFromDiscovery = this.service.discovery(query).blockingGet();
//            
            log.info("Discovery Result: {}", bindingFromDiscovery);
            assertSame(bindingFromDiscovery.getResult(), Status.TOO_MANY);
        }
        finally
        {
            registerResults.forEach(registerResult -> service.deregister(registerResult.getBindingId()).blockingGet());
            this.mbrConfigFlow.onNext(multipleBindingResolver);
        }

    }

    @Test(groups = "functest", enabled = true)
    public void testReject() throws InterruptedException
    {
//      Set Multiple Binding resolution config
//      Reject if multiple bindings found

        HttpLookup httpReject = new HttpLookup().withResolutionType(ResolutionType.REJECT);

        MultipleBindingResolver rejectResolver = new MultipleBindingResolver(httpReject);
        this.mbrConfigFlow.onNext(rejectResolver);

//      Create and push bindings
        var binding1 = generateTestBinding(IPV4_ADDR, null, "IP_Domain1", null, null, null, null, PCF1_DIAM_HOST);
        var binding2 = generateTestBinding(IPV4_ADDR, null, "IP_Domain1", null, null, null, null, PCF2_DIAM_HOST);

        var registerResult1 = registerBinding(binding1, TTL_CONFIG_DEFAULT);
        var registerResult2 = registerBinding(binding2, TTL_CONFIG_DEFAULT);

        var duplicateIpv4 = registerResult1.getPcfBinding().getIpv4Addr();
        var duplicateUeAddress = new UeAddress(duplicateIpv4, Optional.of("IP_Domain1"));

        try
        {

//          There are 2 bindings with the same ueAddress. Since multiple binding resolution is configured to reject, "Multiple found" error is expected
            var query = new DiscoveryQuery.UeAddr(duplicateUeAddress);
            var bindingFromDiscovery = this.service.discovery(query).blockingGet();

            log.info("Discovery Result: {}", bindingFromDiscovery);
            assertSame(bindingFromDiscovery.getResult(), Status.REJECT_MULTIPLE);
        }
        finally
        {
            service.deregister(registerResult1.getBindingId()).blockingGet();
            service.deregister(registerResult2.getBindingId()).blockingGet();
            this.mbrConfigFlow.onNext(multipleBindingResolver);
        }

    }

    private static PcfBinding generateTestBinding(String ipv4Addr,
                                                  Ipv6Prefix ipv6Prefix,
                                                  String ipDomain,
                                                  MacAddr48 macAddr48,
                                                  List<String> addIpv6Prefixes,
                                                  List<String> addMacAddrs)
    {
        return generateTestBinding(ipv4Addr, ipv6Prefix, ipDomain, null, macAddr48, addIpv6Prefixes, addMacAddrs, TEST_PCF_DIAM_HOST);
    }

    private static PcfBinding generateTestBinding(String ipv4Addr,
                                                  Ipv6Prefix ipv6Prefix,
                                                  String ipDomain,
                                                  String Dnn,
                                                  MacAddr48 macAddr48,
                                                  List<String> addIpv6Prefixes,
                                                  List<String> addMacAddrs,
                                                  String pcfDiamHost)
    {
        var dnn = Dnn != null ? Dnn : "testDnn";
        var pcfDhost = pcfDiamHost != null ? pcfDiamHost : TEST_PCF_DIAM_HOST;
        return PcfBinding.createJson("testSupi",
                                     "testGpsi",
                                     ipv4Addr,
                                     ipv6Prefix,
                                     ipDomain,
                                     macAddr48,
                                     dnn,
                                     "testPcfFQDN",
                                     List.of(IpEndPoint.createJson("10.11.12.13", null, "TCP", 3868)),
                                     pcfDhost,
                                     TEST_PCF_DIAM_REALM,
                                     Snssai.create(6, "AF0456"),
                                     null,
                                     null,
                                     null,
                                     addIpv6Prefixes,
                                     addMacAddrs,
                                     "set12.pcfset.5gc.mnc012.mcc345",
                                     new BindingLevel("NF_INSTANCE"));
    }

    private RegisterResult registerBinding(PcfBinding binding,
                                           int ttl)
    {
        RegisterResult registerResult = service.register(binding, ttl).blockingGet();

        log.info("registerResult: {}", registerResult);

        return registerResult;
    }

}
