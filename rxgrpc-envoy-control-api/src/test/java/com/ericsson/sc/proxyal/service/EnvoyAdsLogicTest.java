/**
 * COPYRIGHT ERICSSON GMBH 2019
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Apr 18, 2019
 *     Author: eedrak
 */
package com.ericsson.sc.proxyal.service;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.Rule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.fm.Alarm;
import com.ericsson.sc.proxyal.proxyconfig.ProxyCfg;
import com.ericsson.utilities.common.EnvVars;

//import io.envoyproxy.envoy.api.v2.core.Node;
import io.envoyproxy.envoy.config.core.v3.Node;
//import io.envoyproxy.envoy.api.v2.DiscoveryRequest;
//import io.envoyproxy.envoy.api.v2.DiscoveryResponse;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryRequest;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subscribers.TestSubscriber;

class EnvoyAdsLogicTest
{
    private static final Logger log = LoggerFactory.getLogger(EnvoyAdsLogicTest.class);

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    AdsAlarm badConfigAlarmStub = new AdsAlarm(new Alarm(null, null, "badConfigAlarmStub", "faultyResource", "faultDescription", 60));

    @BeforeEach
    public void setEnvironmentVariable()
    {
        environmentVariables.set("ERIC_PM_SERVER_SERVICE_PORT", "5003");
        assertEquals("5003", EnvVars.get("ERIC_PM_SERVER_SERVICE_PORT"));
    }

//TODO: do  we really need this one ?
    //
    @Disabled
    @Test
    void testLocalIntegrationWithEnvoy() throws InterruptedException
    {
//TODO: do  we really need this one ?
// commenting out to safe time fixing compile errors 
//        
//        final BehaviorSubject<Optional<EricssonScp>> configFlow = BehaviorSubject.createDefault(Optional.<EricssonScp>empty());
//        final RxServer server = new RxServer(9900, new AggregatedDiscoveryService(null, configFlow, 0L));
//
//        Flowable.interval(5, TimeUnit.SECONDS)/* .filter(i -> i < 10).delay(1, TimeUnit.SECONDS) */.doOnNext(__ ->
//        {
//            log.info("NEW CONFIGURATION");
//            configFlow.onNext(createConfig());
//
//        }).subscribe();
//
//        server.start().blockingAwait();
//
//        Thread.sleep(1000000);
//
//        server.stop().blockingAwait();
//
//        fail("Not yet implemented");
    }

    @Test
    @Tag("slow")
    @Tag("integration")
    void testConfigFlowWithClientStub() throws InterruptedException
    {
        final BehaviorSubject<Optional<ProxyCfg>> configFlow = BehaviorSubject.createDefault(Optional.<ProxyCfg>empty());

        final EnvoyAdsLogic adsLogic = new EnvoyAdsLogic(badConfigAlarmStub, configFlow.toFlowable(BackpressureStrategy.LATEST));

        final ADSv2Impl adsImpl = new ADSv2Impl(adsLogic);

        final RxServer server = new RxServer(8888, adsImpl);
        final RxClient client = new RxClient();

        Flowable.interval(1, TimeUnit.SECONDS).filter(i -> i < 5).delay(1, TimeUnit.SECONDS).doOnNext(__ ->
        {
            log.info("NEW CONFIGURATION");
            configFlow.onNext(createConfig());
        }).subscribe();

        server.start().andThen(client.start()).blockingAwait();

        client.update().subscribe();

        Thread.sleep(10000);

        client.stop().andThen(server.stop()).blockingAwait();
    }

    @Test
    @Tag("slow")
    @Tag("integration")
    void testConfigFlowWith2ClientStubs() throws InterruptedException
    {
        final BehaviorSubject<Optional<ProxyCfg>> configFlow = BehaviorSubject.createDefault(Optional.<ProxyCfg>empty());
        final EnvoyAdsLogic adsLogic = new EnvoyAdsLogic(badConfigAlarmStub, configFlow.toFlowable(BackpressureStrategy.LATEST));
        final ADSv2Impl adsV2Impl = new ADSv2Impl(adsLogic);

        final RxServer server = new RxServer(8888, adsV2Impl);

        final RxClient client1 = new RxClient("client1");
        final RxClient client2 = new RxClient("client2");

        Flowable.interval(1, TimeUnit.SECONDS).filter(i -> i < 5).delay(1, TimeUnit.SECONDS).doOnNext(__ ->
        {
            log.info("NEW CONFIGURATION");
            configFlow.onNext(createConfig());
        }).subscribe();

        server.start().andThen(client1.start().andThen(client2.start())).blockingAwait();

        client1.update().subscribe();
        client2.update().subscribe();

        Thread.sleep(10000);

        client1.stop().andThen(server.stop()).blockingAwait();
        client2.stop().andThen(server.stop()).blockingAwait();
    }

    @Disabled // instable
    @Test
    @Tag("slow")
    @Tag("integration")
    void testConfigFlowWithClientStubAndAsserts() throws InterruptedException
    {
        int serverPort = getFreeServerPort();

        final BehaviorSubject<Optional<ProxyCfg>> configFlow = BehaviorSubject.createDefault(Optional.<ProxyCfg>empty());
        final EnvoyAdsLogic adsLogic = new EnvoyAdsLogic(badConfigAlarmStub, configFlow.toFlowable(BackpressureStrategy.LATEST));
        final ADSv2Impl adsImpl = new ADSv2Impl(adsLogic);

        final RxServer server = new RxServer(serverPort, adsImpl);
        final RxClient client = new RxClient("localhost", serverPort);

        server.start().andThen(client.start()).blockingAwait();

        Flowable.interval(1, TimeUnit.SECONDS).filter(i -> i < 5).delay(1, TimeUnit.MILLISECONDS).doOnNext(__ ->
        {
            log.info("NEW CONFIGURATION");
            configFlow.onNext(createConfig());
        }).subscribe();

        Flowable<DiscoveryResponse> respFlow = client.update();

        TestSubscriber<DiscoveryResponse> responseTester = respFlow.test().awaitCount(5);

        responseTester.assertValueAt(0, e -> e.getTypeUrl().equals(CdsHelper.TYPE_URL));
        responseTester.assertValueAt(1, e -> e.getTypeUrl().equals(CdsHelper.TYPE_URL));
        responseTester.assertValueAt(2, e -> e.getTypeUrl().equals(CdsHelper.TYPE_URL));
        responseTester.assertValueAt(3, e -> e.getTypeUrl().equals(CdsHelper.TYPE_URL));
        responseTester.assertValueAt(4, e -> e.getTypeUrl().equals(CdsHelper.TYPE_URL));

        responseTester.assertValueAt(0, e -> e.getVersionInfo().equals("1"));
        responseTester.assertValueAt(1, e -> e.getVersionInfo().equals("2"));
        responseTester.assertValueAt(2, e -> e.getVersionInfo().equals("3"));
        responseTester.assertValueAt(3, e -> e.getVersionInfo().equals("4"));
        responseTester.assertValueAt(4, e -> e.getVersionInfo().equals("5"));

        responseTester.assertNotComplete();
        responseTester.assertNoErrors();
        responseTester.assertNoTimeout();

        client.stop().andThen(server.stop()).blockingAwait();
    }

    @Test
    void testInitialConfigFlowWithoutClient_Cds_Rep_Ack_Lds_Rep_Ack() throws InterruptedException
    {
        final BehaviorSubject<Optional<ProxyCfg>> configFlow = BehaviorSubject.createDefault(Optional.<ProxyCfg>empty());
        final EnvoyAdsLogic ads = new EnvoyAdsLogic(badConfigAlarmStub, configFlow.toFlowable(BackpressureStrategy.LATEST));

        ads.start().subscribe();

        log.info("NEW CONFIGURATION");
        configFlow.onNext(createConfig());

        final BehaviorSubject<DiscoveryRequest> reqFlow = BehaviorSubject.create();

        Flowable<DiscoveryResponse> respFlow = ads.streamAggregatedResources(reqFlow.toFlowable(BackpressureStrategy.LATEST));

        TestSubscriber<DiscoveryResponse> responseTester = respFlow.test();

        Integer initVersion = 1;
        Integer initNonce = 1;

        reqFlow.onNext(buildReq(CdsHelper.TYPE_URL, "", "")); // send CDS initial request

        responseTester.awaitCount(1);
        responseTester.assertValueAt(0, e -> e.getTypeUrl().equals(CdsHelper.TYPE_URL));
        responseTester.assertValueAt(0, e -> e.getVersionInfo().equals(initVersion.toString()));

        reqFlow.onNext(buildReq(CdsHelper.TYPE_URL, initVersion.toString(), initNonce.toString())); // send CDS ACK request
        reqFlow.onNext(buildReq(LdsHelper.TYPE_URL, "", "")); // send LDS initial request

        responseTester.awaitCount(1);
        responseTester.assertValueAt(1, e -> e.getTypeUrl().equals(LdsHelper.TYPE_URL));
        responseTester.assertValueAt(1, e -> e.getVersionInfo().equals(initVersion.toString()));

        reqFlow.onNext(buildReq(LdsHelper.TYPE_URL, initVersion.toString(), initNonce.toString())); // send LDS ACK request

        responseTester.assertValueCount(2);

        responseTester.assertNotComplete();
        responseTester.assertNoErrors();
        responseTester.assertNoTimeout();
    }

    // DND-21682
    @Test
    void testInitialConfigFlowWithoutClient_Cds_Rep_Nack_Lds_Rep_Ack() throws InterruptedException
    {
        final BehaviorSubject<Optional<ProxyCfg>> configFlow = BehaviorSubject.createDefault(Optional.<ProxyCfg>empty());
        final EnvoyAdsLogic ads = new EnvoyAdsLogic(badConfigAlarmStub, configFlow.toFlowable(BackpressureStrategy.LATEST));

        ads.start().subscribe();

        log.info("NEW CONFIGURATION");
        configFlow.onNext(createConfig());

        final BehaviorSubject<DiscoveryRequest> reqFlow = BehaviorSubject.create();

        Flowable<DiscoveryResponse> respFlow = ads.streamAggregatedResources(reqFlow.toFlowable(BackpressureStrategy.LATEST));

        TestSubscriber<DiscoveryResponse> responseTester = respFlow.test();

        Integer initVersion = 1;
        Integer initNonce = 1;

        reqFlow.onNext(buildReq(CdsHelper.TYPE_URL, "", "")); // send CDS initial request

        responseTester.awaitCount(1);
        responseTester.assertValueAt(0, e -> e.getTypeUrl().equals(CdsHelper.TYPE_URL));
        responseTester.assertValueAt(0, e -> e.getVersionInfo().equals(initVersion.toString()));

        reqFlow.onNext(buildReq(CdsHelper.TYPE_URL, "", initNonce.toString())); // send CDS NACK! request
        reqFlow.onNext(buildReq(LdsHelper.TYPE_URL, "", "")); // send LDS initial request

        // verify that no LDS resp. was received
        responseTester.assertValueCount(1);
        reqFlow.onNext(buildReq(CdsHelper.TYPE_URL, initVersion.toString(), initNonce.toString())); // send CDS ACK! request

        // verify LDS resp. was received
        responseTester.awaitCount(1);
        responseTester.assertValueAt(1, e -> e.getTypeUrl().equals(LdsHelper.TYPE_URL));
        responseTester.assertValueAt(1, e -> e.getVersionInfo().equals(initVersion.toString()));

        reqFlow.onNext(buildReq(LdsHelper.TYPE_URL, initVersion.toString(), initNonce.toString())); // send LDS ACK request

        responseTester.assertValueCount(2);

        responseTester.assertNotComplete();
        responseTester.assertNoErrors();
        responseTester.assertNoTimeout();
    }

    @Test
    void testInitialConfigFlowWithoutClient_Cds_Rep_Lds_Ack_Rep_Ack() throws InterruptedException
    {
        final BehaviorSubject<Optional<ProxyCfg>> configFlow = BehaviorSubject.createDefault(Optional.<ProxyCfg>empty());
        final EnvoyAdsLogic ads = new EnvoyAdsLogic(badConfigAlarmStub, configFlow.toFlowable(BackpressureStrategy.LATEST));

        ads.start().subscribe();

        log.info("NEW CONFIGURATION");
        configFlow.onNext(createConfig());

        final BehaviorSubject<DiscoveryRequest> reqFlow = BehaviorSubject.create();

        Flowable<DiscoveryResponse> respFlow = ads.streamAggregatedResources(reqFlow.toFlowable(BackpressureStrategy.LATEST));

        TestSubscriber<DiscoveryResponse> responseTester = respFlow.test();

        Integer initVersion = 1;
        Integer initNonce = 1;

        reqFlow.onNext(buildReq(CdsHelper.TYPE_URL, "", "")); // send CDS initial request

        responseTester.awaitCount(1);
        responseTester.assertValueAt(0, e -> e.getTypeUrl().equals(CdsHelper.TYPE_URL));
        responseTester.assertValueAt(0, e -> e.getVersionInfo().equals(initVersion.toString()));

        reqFlow.onNext(buildReq(LdsHelper.TYPE_URL, "", "")); // send LDS initial request
        reqFlow.onNext(buildReq(CdsHelper.TYPE_URL, initVersion.toString(), initNonce.toString())); // send CDS ACK request

        responseTester.awaitCount(1);
        responseTester.assertValueAt(1, e -> e.getTypeUrl().equals(LdsHelper.TYPE_URL));
        responseTester.assertValueAt(1, e -> e.getVersionInfo().equals(initVersion.toString()));

        reqFlow.onNext(buildReq(LdsHelper.TYPE_URL, initVersion.toString(), initNonce.toString())); // send LDS ACK request

        responseTester.assertValueCount(2);

        responseTester.assertNotComplete();
        responseTester.assertNoErrors();
        responseTester.assertNoTimeout();
    }

    @Tag("integration")
    @Test
    void testInitialConfigFlowWithoutClient_Cds_Rep_Lds_Ack_Rep_Ack_Rds_Rep() throws InterruptedException
    {
        final BehaviorSubject<Optional<ProxyCfg>> configFlow = BehaviorSubject.createDefault(Optional.<ProxyCfg>empty());
        final EnvoyAdsLogic ads = new EnvoyAdsLogic(badConfigAlarmStub, configFlow.toFlowable(BackpressureStrategy.LATEST));

        ads.start().subscribe();

        log.info("NEW CONFIGURATION");
        configFlow.onNext(createConfig());

        final BehaviorSubject<DiscoveryRequest> reqFlow = BehaviorSubject.create();

        Flowable<DiscoveryResponse> respFlow = ads.streamAggregatedResources(reqFlow.toFlowable(BackpressureStrategy.LATEST));

        TestSubscriber<DiscoveryResponse> responseTester = respFlow.test();

        Integer initVersion = 1;
        Integer initNonce = 1;

        reqFlow.onNext(buildReq(CdsHelper.TYPE_URL, "", "")); // send CDS initial request

        responseTester.awaitCount(1);
        responseTester.assertValueAt(0, e -> e.getTypeUrl().equals(CdsHelper.TYPE_URL));
        responseTester.assertValueAt(0, e -> e.getVersionInfo().equals(initVersion.toString()));

        reqFlow.onNext(buildReq(LdsHelper.TYPE_URL, "", "")); // send LDS initial request
        reqFlow.onNext(buildReq(CdsHelper.TYPE_URL, initVersion.toString(), initNonce.toString())); // send CDS ACK request

        responseTester.awaitCount(1);
        responseTester.assertValueAt(1, e -> e.getTypeUrl().equals(LdsHelper.TYPE_URL));
        responseTester.assertValueAt(1, e -> e.getVersionInfo().equals(initVersion.toString()));

        reqFlow.onNext(buildReq(LdsHelper.TYPE_URL, initVersion.toString(), initNonce.toString())); // send LDS ACK request

        reqFlow.onNext(buildReq(RdsHelper.TYPE_URL, "", "")); // send LDS initial request
        responseTester.awaitCount(2);
        responseTester.assertValueAt(2, e -> e.getTypeUrl().equals(RdsHelper.TYPE_URL));
        responseTester.assertValueAt(2, e -> e.getVersionInfo().equals(initVersion.toString()));

        responseTester.assertValueCount(3);

        responseTester.assertNotComplete();
        responseTester.assertNoErrors();
        responseTester.assertNoTimeout();
    }

    @Tag("integration")
    @Test
    void testInitialConfigFlowWithoutClient_Cds_Rep_Lds_Ack_Eds_Req_Ack_Rep_Ack_Rds_Rep() throws InterruptedException
    {
        final BehaviorSubject<Optional<ProxyCfg>> configFlow = BehaviorSubject.createDefault(Optional.<ProxyCfg>empty());
        final EnvoyAdsLogic ads = new EnvoyAdsLogic(badConfigAlarmStub, configFlow.toFlowable(BackpressureStrategy.LATEST));

        ads.start().subscribe();

        log.info("NEW CONFIGURATION");
        configFlow.onNext(createConfig());

        final BehaviorSubject<DiscoveryRequest> reqFlow = BehaviorSubject.create();

        Flowable<DiscoveryResponse> respFlow = ads.streamAggregatedResources(reqFlow.toFlowable(BackpressureStrategy.LATEST));

        TestSubscriber<DiscoveryResponse> responseTester = respFlow.test();

        Integer initVersion = 1;
        Integer initNonce = 1;

        reqFlow.onNext(buildReq(CdsHelper.TYPE_URL, "", "")); // send CDS initial request

        responseTester.awaitCount(1);
        responseTester.assertValueAt(0, e -> e.getTypeUrl().equals(CdsHelper.TYPE_URL));
        responseTester.assertValueAt(0, e -> e.getVersionInfo().equals(initVersion.toString()));

        reqFlow.onNext(buildReq(LdsHelper.TYPE_URL, "", "")); // send LDS initial request
        reqFlow.onNext(buildReq(CdsHelper.TYPE_URL, initVersion.toString(), initNonce.toString())); // send CDS ACK request

        responseTester.awaitCount(1);
        responseTester.assertValueAt(1, e -> e.getTypeUrl().equals(LdsHelper.TYPE_URL));
        responseTester.assertValueAt(1, e -> e.getVersionInfo().equals(initVersion.toString()));

        reqFlow.onNext(buildReq(EdsHelper.TYPE_URL, "", "")); // send EDS initial request
        responseTester.awaitCount(1);
        responseTester.assertValueAt(2, e -> e.getTypeUrl().equals(EdsHelper.TYPE_URL));
        responseTester.assertValueAt(2, e -> e.getVersionInfo().equals(initVersion.toString()));

        reqFlow.onNext(buildReq(LdsHelper.TYPE_URL, initVersion.toString(), initNonce.toString())); // send LDS ACK request

        reqFlow.onNext(buildReq(RdsHelper.TYPE_URL, "", "")); // send LDS initial request
        responseTester.awaitCount(1);
        responseTester.assertValueAt(3, e -> e.getTypeUrl().equals(RdsHelper.TYPE_URL));
        responseTester.assertValueAt(3, e -> e.getVersionInfo().equals(initVersion.toString()));

        responseTester.assertValueCount(4);

        responseTester.assertNotComplete();
        responseTester.assertNoErrors();
        responseTester.assertNoTimeout();
    }

    @Test
    void testSubsequentConfigFlowWithoutClient() throws InterruptedException
    {
        final BehaviorSubject<Optional<ProxyCfg>> configFlow = BehaviorSubject.createDefault(Optional.<ProxyCfg>empty());
        final EnvoyAdsLogic ads = new EnvoyAdsLogic(badConfigAlarmStub, configFlow.toFlowable(BackpressureStrategy.LATEST));

        ads.start().subscribe();

        log.info("NEW CONFIGURATION");
        configFlow.onNext(createConfig("1"));

        final BehaviorSubject<DiscoveryRequest> reqFlow = BehaviorSubject.create();

        Flowable<DiscoveryResponse> respFlow = ads.streamAggregatedResources(reqFlow.toFlowable(BackpressureStrategy.LATEST));

        TestSubscriber<DiscoveryResponse> responseTester = respFlow.test();

        Integer initVersion = 1;
        Integer initNonce = 1;

        Integer expVersion;
        Integer expNonce;

        reqFlow.onNext(buildReq(CdsHelper.TYPE_URL, "", "")); // send CDS initial request

        responseTester.awaitCount(1);
        responseTester.assertValueAt(0, e -> e.getTypeUrl().equals(CdsHelper.TYPE_URL));
        responseTester.assertValueAt(0, e -> e.getVersionInfo().equals(initVersion.toString()));

        reqFlow.onNext(buildReq(CdsHelper.TYPE_URL, initVersion.toString(), initNonce.toString())); // send CDS ACK request

        log.info("NEW CONFIGURATION");

        configFlow.onNext(createConfig("2"));

        expVersion = initVersion.intValue() + 1;
        expNonce = initNonce.intValue() + 1;

        responseTester.awaitCount(1);
        responseTester.assertValueAt(1, e -> e.getTypeUrl().equals(CdsHelper.TYPE_URL));
        responseTester.assertValueAt(1, e -> e.getVersionInfo().equals(expVersion.toString()));

        reqFlow.onNext(buildReq(CdsHelper.TYPE_URL, expVersion.toString(), expNonce.toString())); // send CDS ACK request

        responseTester.assertValueCount(2);

        responseTester.assertNotComplete();
        responseTester.assertNoErrors();
        responseTester.assertNoTimeout();
    }

    @Test
    void testSubsequentConfigFlowWithCdsAndLds() throws InterruptedException
    {
        final BehaviorSubject<Optional<ProxyCfg>> configFlow = BehaviorSubject.createDefault(Optional.<ProxyCfg>empty());
        final EnvoyAdsLogic ads = new EnvoyAdsLogic(badConfigAlarmStub, configFlow.toFlowable(BackpressureStrategy.LATEST));

        ads.start().subscribe();

        log.info("NEW CONFIGURATION");
        configFlow.onNext(createConfig("1"));

        final BehaviorSubject<DiscoveryRequest> reqFlow = BehaviorSubject.create();

        Flowable<DiscoveryResponse> respFlow = ads.streamAggregatedResources(reqFlow.toFlowable(BackpressureStrategy.LATEST));

        TestSubscriber<DiscoveryResponse> responseTester = respFlow.test();

        Integer initVersion = 1;
        Integer initNonce = 1;

        Integer expVersion;
        Integer expNonce;

        reqFlow.onNext(buildReq(CdsHelper.TYPE_URL, "", "")); // send CDS initial request
        reqFlow.onNext(buildReq(LdsHelper.TYPE_URL, "", "")); // send LDS initial request

        responseTester.awaitCount(1);
        responseTester.assertValueAt(0, e -> e.getTypeUrl().equals(CdsHelper.TYPE_URL));
        responseTester.assertValueAt(0, e -> e.getVersionInfo().equals(initVersion.toString()));

        reqFlow.onNext(buildReq(CdsHelper.TYPE_URL, initVersion.toString(), initNonce.toString())); // send CDS ACK request

        responseTester.awaitCount(1);
        responseTester.assertValueAt(1, e -> e.getTypeUrl().equals(LdsHelper.TYPE_URL));
        responseTester.assertValueAt(1, e -> e.getVersionInfo().equals(initVersion.toString()));

        reqFlow.onNext(buildReq(LdsHelper.TYPE_URL, initVersion.toString(), initNonce.toString())); // send LDS ACK request

        log.info("NEW CONFIGURATION");
        configFlow.onNext(createConfig("2"));

        expVersion = initVersion.intValue() + 1;
        expNonce = initNonce.intValue() + 1;

        responseTester.awaitCount(1);
        responseTester.assertValueAt(2, e -> e.getTypeUrl().equals(CdsHelper.TYPE_URL));
        responseTester.assertValueAt(2, e -> e.getVersionInfo().equals(expVersion.toString()));

        responseTester.assertValueCount(3);
        reqFlow.onNext(buildReq(CdsHelper.TYPE_URL, expVersion.toString(), expNonce.toString())); // send CDS ACK request

        responseTester.awaitCount(1);
        responseTester.assertValueAt(3, e -> e.getTypeUrl().equals(LdsHelper.TYPE_URL));
        responseTester.assertValueAt(3, e -> e.getVersionInfo().equals(expVersion.toString()));

        responseTester.assertValueCount(4);

        responseTester.assertNotComplete();
        responseTester.assertNoErrors();
        responseTester.assertNoTimeout();
    }

    @Test
    void testSubsequentConfigFlowWithCdsAndLds_offset_10_init_req_lower() throws InterruptedException
    {
        final BehaviorSubject<Optional<ProxyCfg>> configFlow = BehaviorSubject.createDefault(Optional.<ProxyCfg>empty());
        final EnvoyAdsLogic ads = new EnvoyAdsLogic(badConfigAlarmStub, configFlow.toFlowable(BackpressureStrategy.LATEST), 10L);

        ads.start().subscribe();

        log.info("NEW CONFIGURATION");
        configFlow.onNext(createConfig("1"));

        final BehaviorSubject<DiscoveryRequest> reqFlow = BehaviorSubject.create();

        Flowable<DiscoveryResponse> respFlow = ads.streamAggregatedResources(reqFlow.toFlowable(BackpressureStrategy.LATEST));

        TestSubscriber<DiscoveryResponse> responseTester = respFlow.test();

        Integer initVersion = 11;
        Integer initNonce = 11;

        Integer expVersion;
        Integer expNonce;

        reqFlow.onNext(buildReq(CdsHelper.TYPE_URL, "8", "8")); // send CDS initial request with lower version than offset
        reqFlow.onNext(buildReq(LdsHelper.TYPE_URL, "8", "8")); // send LDS initial request with lower version than offset

        responseTester.awaitCount(1);
        responseTester.assertValueAt(0, e -> e.getTypeUrl().equals(CdsHelper.TYPE_URL));
        responseTester.assertValueAt(0, e -> e.getVersionInfo().equals(initVersion.toString()));

        reqFlow.onNext(buildReq(CdsHelper.TYPE_URL, initVersion.toString(), initNonce.toString())); // send CDS ACK request

        responseTester.assertValueAt(1, e -> e.getTypeUrl().equals(LdsHelper.TYPE_URL));
        responseTester.assertValueAt(1, e -> e.getVersionInfo().equals(initVersion.toString()));

        reqFlow.onNext(buildReq(LdsHelper.TYPE_URL, initVersion.toString(), initNonce.toString())); // send LDS ACK request

        log.info("NEW CONFIGURATION");
        configFlow.onNext(createConfig("2"));

        expVersion = initVersion.intValue() + 1;
        expNonce = initNonce.intValue() + 1;

        responseTester.awaitCount(1);
        responseTester.assertValueAt(2, e -> e.getTypeUrl().equals(CdsHelper.TYPE_URL));
        responseTester.assertValueAt(2, e -> e.getVersionInfo().equals(expVersion.toString()));

        responseTester.assertValueCount(3);
        reqFlow.onNext(buildReq(CdsHelper.TYPE_URL, expVersion.toString(), expNonce.toString())); // send CDS ACK request

        responseTester.awaitCount(1);
        responseTester.assertValueAt(3, e -> e.getTypeUrl().equals(LdsHelper.TYPE_URL));
        responseTester.assertValueAt(3, e -> e.getVersionInfo().equals(expVersion.toString()));

        responseTester.assertValueCount(4);

        responseTester.assertNotComplete();
        responseTester.assertNoErrors();
        responseTester.assertNoTimeout();
    }

    // FIXME
    // @Disabled
    @Test
    void testSubsequentConfigFlowWithCdsAndLds_offset_0_init_req_higher() throws InterruptedException
    {
        final BehaviorSubject<Optional<ProxyCfg>> configFlow = BehaviorSubject.createDefault(Optional.<ProxyCfg>empty());
        final EnvoyAdsLogic ads = new EnvoyAdsLogic(badConfigAlarmStub, configFlow.toFlowable(BackpressureStrategy.LATEST));

        ads.start().subscribe();

        log.info("NEW CONFIGURATION");
        configFlow.onNext(createConfig("1"));

        final BehaviorSubject<DiscoveryRequest> reqFlow = BehaviorSubject.create();

        Flowable<DiscoveryResponse> respFlow = ads.streamAggregatedResources(reqFlow.toFlowable(BackpressureStrategy.LATEST));

        TestSubscriber<DiscoveryResponse> responseTester = respFlow.test();
        configFlow.onNext(createConfig("2"));

        Integer initVersion = 2;
        Integer initNonce = 2;

        Integer expVersion;
        Integer expNonce;

        reqFlow.onNext(buildReq(CdsHelper.TYPE_URL, "20", "20")); // send CDS initial request with lower version than offset
        reqFlow.onNext(buildReq(LdsHelper.TYPE_URL, "20", "20")); // send LDS initial request with lower version than offset

        responseTester.awaitCount(1);
        responseTester.assertValueAt(0, e -> e.getTypeUrl().equals(CdsHelper.TYPE_URL));
        responseTester.assertValueAt(0, e -> e.getVersionInfo().equals(initVersion.toString()));

        reqFlow.onNext(buildReq(CdsHelper.TYPE_URL, initVersion.toString(), initNonce.toString())); // send CDS ACK request

        responseTester.awaitCount(1);
        responseTester.assertValueAt(1, e -> e.getTypeUrl().equals(LdsHelper.TYPE_URL));
        responseTester.assertValueAt(1, e -> e.getVersionInfo().equals(initVersion.toString()));

        reqFlow.onNext(buildReq(LdsHelper.TYPE_URL, initVersion.toString(), initNonce.toString())); // send LDS ACK request

        log.info("NEW CONFIGURATION");
        configFlow.onNext(createConfig("3"));

        expVersion = initVersion.intValue() + 1;
        expNonce = initNonce.intValue() + 1;

        responseTester.awaitCount(1);
        responseTester.assertValueAt(2, e -> e.getTypeUrl().equals(CdsHelper.TYPE_URL));
        responseTester.assertValueAt(2, e -> e.getVersionInfo().equals(expVersion.toString()));

        responseTester.assertValueCount(3);
        reqFlow.onNext(buildReq(CdsHelper.TYPE_URL, expVersion.toString(), expNonce.toString())); // send CDS ACK request

        responseTester.awaitCount(1);
        responseTester.assertValueAt(3, e -> e.getTypeUrl().equals(LdsHelper.TYPE_URL));
        responseTester.assertValueAt(3, e -> e.getVersionInfo().equals(expVersion.toString()));

        responseTester.assertValueCount(4);

        responseTester.assertNotComplete();
        responseTester.assertNoErrors();
        responseTester.assertNoTimeout();
    }

    /**
     * 
     * @param typeUrl
     * @param version
     * @param responseNonce
     * @return
     */
    private DiscoveryRequest buildReq(String typeUrl,
                                      String version,
                                      String responseNonce)
    {
        return this.buildReq("ut-envoy-1", typeUrl, version, responseNonce);
    }

    private DiscoveryRequest buildReq(String sender,
                                      String typeUrl,
                                      String version,
                                      String responseNonce)
    {
        if ((typeUrl.equals(RdsHelper.TYPE_URL)) || (typeUrl.equals(EdsHelper.TYPE_URL)))
            return DiscoveryRequest.newBuilder()
                                   .setNode(Node.newBuilder().setId(sender).build())
                                   .setVersionInfo(version)
                                   .setTypeUrl(typeUrl)
                                   .setResponseNonce(responseNonce)
                                   .addResourceNames("someresource")
                                   .build();

        return DiscoveryRequest.newBuilder()
                               .setNode(Node.newBuilder().setId(sender).build())
                               .setVersionInfo(version)
                               .setTypeUrl(typeUrl)
                               .setResponseNonce(responseNonce)
                               .build();
    }

    private int getFreeServerPort()
    {
        int port = 8888;
        try ( // Pick an available and random
             ServerSocket socket = new ServerSocket(0))
        {
            port = socket.getLocalPort();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        log.info("using server port {} for this test.", port);
        return port;

    }

    @Test
    void testInitialConfigFlowWithoutClient() throws InterruptedException
    {
        final BehaviorSubject<Optional<ProxyCfg>> configFlow = BehaviorSubject.createDefault(Optional.<ProxyCfg>empty());
        final EnvoyAdsLogic ads = new EnvoyAdsLogic(badConfigAlarmStub, configFlow.toFlowable(BackpressureStrategy.LATEST));

        ads.start().subscribe();

        log.info("NEW CONFIGURATION");
        configFlow.onNext(createConfig());

        final BehaviorSubject<DiscoveryRequest> reqFlow = BehaviorSubject.create();

        Flowable<DiscoveryResponse> respFlow = ads.streamAggregatedResources(reqFlow.toFlowable(BackpressureStrategy.LATEST));

        TestSubscriber<DiscoveryResponse> responseTester = respFlow.test();

        Integer initVersion = 1;
        Integer initNonce = 1;

        reqFlow.onNext(buildReq(CdsHelper.TYPE_URL, "", "")); // send CDS initial request

        responseTester.awaitCount(1);
        responseTester.assertValueAt(0, e -> e.getTypeUrl().equals(CdsHelper.TYPE_URL));
        responseTester.assertValueAt(0, e -> e.getVersionInfo().equals(initVersion.toString()));

        reqFlow.onNext(buildReq(CdsHelper.TYPE_URL, "", initNonce.toString())); // send CDS NACK request

        reqFlow.onNext(buildReq(LdsHelper.TYPE_URL, "", "")); // send LDS initial request
        reqFlow.onNext(buildReq(CdsHelper.TYPE_URL, initVersion.toString(), initNonce.toString())); // send CDS ACK request

        responseTester.awaitCount(1);
        responseTester.assertValueAt(1, e -> e.getTypeUrl().equals(LdsHelper.TYPE_URL));
        responseTester.assertValueAt(1, e -> e.getVersionInfo().equals(initVersion.toString()));

        reqFlow.onNext(buildReq(LdsHelper.TYPE_URL, "", initNonce.toString())); // send LDS NACK request
        reqFlow.onNext(buildReq(LdsHelper.TYPE_URL, initVersion.toString(), initNonce.toString())); // send LDS ACK request

        responseTester.assertValueCount(2);

        responseTester.assertNotComplete();
        responseTester.assertNoErrors();
        responseTester.assertNoTimeout();
    }

    // FIXME
    @Disabled
    @Test
    void testSubsequentConfigFlowWithCdsAndLdsStrictSequentially() throws InterruptedException
    {
        final BehaviorSubject<Optional<ProxyCfg>> configFlow = BehaviorSubject.createDefault(Optional.<ProxyCfg>empty());
        final EnvoyAdsLogic ads = new EnvoyAdsLogic(badConfigAlarmStub, configFlow.toFlowable(BackpressureStrategy.LATEST));

        ads.start().subscribe();

        log.info("NEW CONFIGURATION");
        configFlow.onNext(createConfig());

        final BehaviorSubject<DiscoveryRequest> reqFlow = BehaviorSubject.create();

        Flowable<DiscoveryResponse> respFlow = ads.streamAggregatedResources(reqFlow.toFlowable(BackpressureStrategy.LATEST));

        TestSubscriber<DiscoveryResponse> responseTester = respFlow.test();

        Integer initVersion = 1;
        Integer initNonce = 1;

        Integer expVersion;
        Integer expNonce;

        reqFlow.onNext(buildReq(CdsHelper.TYPE_URL, "", "")); // send CDS initial request

        responseTester.awaitCount(1);
        responseTester.assertValueAt(0, e -> e.getTypeUrl().equals(CdsHelper.TYPE_URL));
        responseTester.assertValueAt(0, e -> e.getVersionInfo().equals(initVersion.toString()));

        reqFlow.onNext(buildReq(CdsHelper.TYPE_URL, initVersion.toString(), initNonce.toString())); // send CDS ACK request

        reqFlow.onNext(buildReq(LdsHelper.TYPE_URL, "", "")); // send LDS initial request

        responseTester.awaitCount(1);
        responseTester.assertValueAt(1, e -> e.getTypeUrl().equals(LdsHelper.TYPE_URL));
        responseTester.assertValueAt(1, e -> e.getVersionInfo().equals(initVersion.toString()));

        reqFlow.onNext(buildReq(LdsHelper.TYPE_URL, initVersion.toString(), initNonce.toString())); // send LDS ACK request

        log.info("NEW CONFIGURATION");
        configFlow.onNext(createConfig());

        expVersion = initVersion.intValue() + 1;
        expNonce = initNonce.intValue() + 1;

        responseTester.awaitCount(1);
        responseTester.assertValueAt(2, e -> e.getTypeUrl().equals(CdsHelper.TYPE_URL));
        responseTester.assertValueAt(2, e -> e.getVersionInfo().equals(expVersion.toString()));

        responseTester.assertValueCount(3);
        reqFlow.onNext(buildReq(CdsHelper.TYPE_URL, expVersion.toString(), expNonce.toString())); // send CDS ACK request

        responseTester.awaitCount(1);
        responseTester.assertValueAt(3, e -> e.getTypeUrl().equals(LdsHelper.TYPE_URL));
        responseTester.assertValueAt(3, e -> e.getVersionInfo().equals(expVersion.toString()));

        responseTester.assertValueCount(4);

        responseTester.assertNotComplete();
        responseTester.assertNoErrors();
        responseTester.assertNoTimeout();
    }

    // FIXME
    // @Disabled
    @Test
    void testSubsequentConfigFlowWith_CdsRep_NewConfig_CdsAck_CdsRep() throws InterruptedException
    {
        final BehaviorSubject<Optional<ProxyCfg>> configFlow = BehaviorSubject.createDefault(Optional.<ProxyCfg>empty());
        final EnvoyAdsLogic ads = new EnvoyAdsLogic(badConfigAlarmStub, configFlow.toFlowable(BackpressureStrategy.LATEST));

        ads.start().subscribe();

        log.info("NEW CONFIGURATION");
        configFlow.onNext(createConfig("1"));

        final BehaviorSubject<DiscoveryRequest> reqFlow = BehaviorSubject.create();

        Flowable<DiscoveryResponse> respFlow = ads.streamAggregatedResources(reqFlow.toFlowable(BackpressureStrategy.LATEST));

        TestSubscriber<DiscoveryResponse> responseTester = respFlow.test();

        Integer initVersion = 1;
        Integer initNonce = 1;

        Integer expVersion;

        reqFlow.onNext(buildReq(CdsHelper.TYPE_URL, "", "")); // send CDS initial request

        responseTester.awaitCount(1);
        responseTester.assertValueAt(0, e -> e.getTypeUrl().equals(CdsHelper.TYPE_URL));
        responseTester.assertValueAt(0, e -> e.getVersionInfo().equals(initVersion.toString()));

        log.info("NEW CONFIGURATION");
        configFlow.onNext(createConfig("2"));

        expVersion = initVersion.intValue() + 1;

        reqFlow.onNext(buildReq(CdsHelper.TYPE_URL, initVersion.toString(), initNonce.toString())); // send CDS ACK request

        responseTester.awaitCount(1);
        responseTester.assertValueAt(1, e -> e.getTypeUrl().equals(CdsHelper.TYPE_URL));
        responseTester.assertValueAt(1, e -> e.getVersionInfo().equals(expVersion.toString()));

        responseTester.assertValueCount(2);

        responseTester.assertNotComplete();
        responseTester.assertNoErrors();
        responseTester.assertNoTimeout();
    }

    /*
     * 
     * Private Utilities
     * 
     */

    /**
     * @param string
     * @return
     */
    private static Optional<ProxyCfg> createConfig()
    {
        return createConfig("default");
    }

    /**
     * @param string
     * @return
     */
    private static Optional<ProxyCfg> createConfig(String string)
    {
        ProxyCfg cfg = new ProxyCfg("testNf");
        cfg.setRdnOfNfInstance(string);
        return Optional.of(cfg);
    }

}
