/**
 * COPYRIGHT ERICSSON GMBH 2023
 *
 * The copyright to the computer program(s) herein is the property of Ericsson
 * GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written permission of
 * Ericsson GmbH in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 *
 * Created on: Jan 16, 2024 Author: zstoioa
 */

package com.ericsson.esc.bsf.worker;

import static org.junit.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.ericsson.esc.bsf.db.MockNbsfManagementService;
import com.ericsson.sc.util.tls.AdvancedTlsX509TrustManager;
import com.ericsson.sc.util.tls.AdvancedTlsX509TrustManager.Verification;
import com.ericsson.sc.util.tls.DynamicTlsCertManager;
import com.ericsson.supreme.api.GeneratedCert;
import com.ericsson.supreme.kernel.CertificateTool;
import com.ericsson.utilities.cassandra.RxSession;
import com.ericsson.utilities.file.KeyCert;
import com.ericsson.utilities.file.TrustedCert;
import com.ericsson.utilities.http.HelperHttp;
import com.ericsson.utilities.http.KubeProbe;
import com.ericsson.utilities.http.WebServer;
import com.ericsson.utilities.reactivex.VertxBuilder;
import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.vertx.reactivex.core.Vertx;

/**
 * Unit Tests for the ServerController class
 */
public class ServerControllerTest
{
    private static final Logger log = LoggerFactory.getLogger(ServerControllerTest.class);

    private static final String HOST_IPV4 = "127.0.0.50";
    private static final String HOST_IPV6 = "::1";
    private static final String DECIDE_READINESS_TO_FALSE_EXPECTED_TRUE = "DecideToSetReadinessToFalse expected to be true";
    private static final String DECIDE_READINESS_TO_FALSE_EXPECTED_FALSE = "DecideToSetReadinessToFalse expected to be false";
    private static final String SRV_NOT_UP = "At least one server is not up";
    private static final String SRV_UP = "At least one server is up while it should not";
    private static final String READINESS_EXPECTED_TRUE = "Should be ready but it is not";

    private final Vertx vertx = VertxBuilder.newInstance().modifyRxSchedulers(false).build();
    private final KubeProbe kubeProbe = KubeProbe.of();
    private final NBsfManagementService nbsfManagementService = new MockNbsfManagementService();
    private final Observable<BsfCmConfig> bsfConfig = Observable.just(new BsfCmConfig.Builder().bindingTimeout(50)
                                                                                               .nfInstanceName("bsf")
                                                                                               .outMessageHandling(false)
                                                                                               .build());
    private final RxSession rxSession = RxSession.builder().withConfig(DriverConfigLoader.programmaticBuilder().build()).build();

    private final BindingCleanupManager bindingCleanup = new BindingCleanupManager(this.rxSession, "sample_keyspace", Flowable.just("sample"));

    private static final int LOCAL_NON_TLS_PORT = 1111;
    private static final int LOCAL_TLS_PORT = 1112;

    private final GeneratedCert rootCa = generateCaCert();
    private final GeneratedCert clientCert = generateClientCert(rootCa);

    private @NonNull AsyncCache<String, String> tokenCache = Caffeine.newBuilder().maximumSize(100).buildAsync();

    @Test(enabled = true)
    public void updateReadinessStateTest()
    {
        final var serverController = new ServerController(this.kubeProbe);

        serverController.updateReadinessState(true).blockingGet();
        assertTrue(this.kubeProbe.isReady(), READINESS_EXPECTED_TRUE);

        serverController.updateReadinessState(false).blockingGet();
        assertFalse(this.kubeProbe.isReady(), "Should not be ready but it is");
    }

    @Test(enabled = true)
    public void decideToSetReadinessToTrueTest()
    {
        final var currentSrvs = new ArrayList<ServerContainer>();
        final var globalSrvOpt = new BsfSrvOptions();

        final var serverController = new ServerController(this.kubeProbe, globalSrvOpt, currentSrvs);

        final var res1 = serverController.decideToSetReadinessToTrue().blockingGet();
        assertFalse(res1, "decideToSetReadinessToTrue should be false since currentSrvs list is empty");

        currentSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV4));

        final var serverController2 = new ServerController(this.kubeProbe, globalSrvOpt, currentSrvs);
        final var res2 = serverController2.decideToSetReadinessToTrue().blockingGet();

        assertTrue(res2, "decideToSetReadinessToTrue should be true since currentSrvs list is not empty");
    }

    @Test(enabled = true)
    public void decideToSetReadinessToFalseUpdateExistingSrvsTest()
    {
        final var currentSrvs = new ArrayList<ServerContainer>();
        currentSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV4));
        currentSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV4TLS));
        currentSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV6));
        currentSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV6TLS));

        final var globalSrvOpt = new BsfSrvOptions(false, 2048L, 0);

        final var srvController = new ServerController(this.kubeProbe, globalSrvOpt, currentSrvs);

        final var newGlobalSrvOpt = new BsfSrvOptions(true, 2048L, 0);

        // srv list does not matter in this test so we can re-use currentSrvs list.
        final var res = srvController.decideToSetReadinessToFalse(newGlobalSrvOpt, currentSrvs).blockingGet();

        assertTrue(res, DECIDE_READINESS_TO_FALSE_EXPECTED_TRUE);

    }

    @Test(enabled = true)
    public void decideToSetReadinessToFalseMoveTlsToNoTlsTest()
    {
        final var currentSrvs = new ArrayList<ServerContainer>();
        currentSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV4TLS));
        currentSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV6TLS));

        final var newSrvs = new ArrayList<ServerContainer>();
        newSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV4));
        newSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV6));

        final var globalSrvOpt = new BsfSrvOptions();

        final var srvController = new ServerController(this.kubeProbe, globalSrvOpt, currentSrvs);

        // globalSrvOpt does not matter in this test so we can re-use it.
        final var res = srvController.decideToSetReadinessToFalse(globalSrvOpt, newSrvs).blockingGet();

        assertTrue(res, DECIDE_READINESS_TO_FALSE_EXPECTED_TRUE);

    }

    @Test(enabled = true)
    public void decideToSetReadinessToFalseMoveNoTlsToTlsTest()
    {
        final var currentSrvs = new ArrayList<ServerContainer>();
        currentSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV4));
        currentSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV6));

        final var newSrvs = new ArrayList<ServerContainer>();
        newSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV4TLS));
        newSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV6TLS));

        final var globalSrvOpt = new BsfSrvOptions();

        final var srvController = new ServerController(this.kubeProbe, globalSrvOpt, currentSrvs);

        // globalSrvOpt does not matter in this test so we can re-use it.
        final var res = srvController.decideToSetReadinessToFalse(globalSrvOpt, newSrvs).blockingGet();

        assertTrue(res, DECIDE_READINESS_TO_FALSE_EXPECTED_TRUE);

    }

    @Test(enabled = true)
    public void decideToSetReadinessToFalseMoveIPv4ToIPv6Test()
    {
        final var currentSrvs = new ArrayList<ServerContainer>();
        currentSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV4));
        currentSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV4TLS));

        final var newSrvs = new ArrayList<ServerContainer>();
        newSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV6));
        newSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV6TLS));

        final var globalSrvOpt = new BsfSrvOptions();

        final var srvController = new ServerController(this.kubeProbe, globalSrvOpt, currentSrvs);

        // globalSrvOpt does not matter in this test so we can re-use it.
        final var res = srvController.decideToSetReadinessToFalse(globalSrvOpt, newSrvs).blockingGet();

        assertTrue(res, DECIDE_READINESS_TO_FALSE_EXPECTED_TRUE);

    }

    @Test(enabled = true)
    public void decideToSetReadinessToFalseMoveIPv6ToIPv4Test()
    {
        final var currentSrvs = new ArrayList<ServerContainer>();
        currentSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV6));
        currentSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV6TLS));

        final var newSrvs = new ArrayList<ServerContainer>();
        newSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV4));
        newSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV4TLS));

        final var globalSrvOpt = new BsfSrvOptions();

        final var srvController = new ServerController(this.kubeProbe, globalSrvOpt, currentSrvs);

        // globalSrvOpt does not matter in this test so we can re-use it.
        final var res = srvController.decideToSetReadinessToFalse(globalSrvOpt, newSrvs).blockingGet();

        assertTrue(res, DECIDE_READINESS_TO_FALSE_EXPECTED_TRUE);

    }

    @Test(enabled = true)
    public void decideToSetReadinessToFalseStopSrvsTest()
    {
        final var currentSrvs = new ArrayList<ServerContainer>();
        currentSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV6));
        currentSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV6TLS));
        currentSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV4));
        currentSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV4TLS));

        final var newSrvs = new ArrayList<ServerContainer>();

        final var globalSrvOpt = new BsfSrvOptions();

        final var srvController = new ServerController(this.kubeProbe, globalSrvOpt, currentSrvs);

        // globalSrvOpt does not matter in this test so we can re-use it.
        final var res = srvController.decideToSetReadinessToFalse(globalSrvOpt, newSrvs).blockingGet();

        assertTrue(res, DECIDE_READINESS_TO_FALSE_EXPECTED_TRUE);

    }

    @Test(enabled = true)
    public void decideToSetReadinessToFalseInreaseSrvsTest()
    {
        final var currentSrvs = new ArrayList<ServerContainer>();
        currentSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV6));
        currentSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV6TLS));

        final var newSrvs = new ArrayList<ServerContainer>(currentSrvs);
        newSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV4));
        newSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV4TLS));

        final var globalSrvOpt = new BsfSrvOptions();

        final var srvController = new ServerController(this.kubeProbe, globalSrvOpt, currentSrvs);

        // globalSrvOpt does not matter in this test so we can re-use it.
        final var res = srvController.decideToSetReadinessToFalse(globalSrvOpt, newSrvs).blockingGet();

        assertFalse(res, DECIDE_READINESS_TO_FALSE_EXPECTED_FALSE);

    }

    @Test(enabled = true)
    public void decideToSetReadinessToFalseReduceSrvsTest()
    {
        final var currentSrvs = new ArrayList<ServerContainer>();
        currentSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV4));
        currentSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV4TLS));
        currentSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV6));
        currentSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV6TLS));

        final var newSrvs = new ArrayList<ServerContainer>(currentSrvs);
        // remove first two servers
        newSrvs.remove(0);
        newSrvs.remove(0);

        final var globalSrvOpt = new BsfSrvOptions();

        final var srvController = new ServerController(this.kubeProbe, globalSrvOpt, currentSrvs);

        // globalSrvOpt does not matter in this test so we can re-use it.
        final var res = srvController.decideToSetReadinessToFalse(globalSrvOpt, newSrvs).blockingGet();

        assertFalse(res, DECIDE_READINESS_TO_FALSE_EXPECTED_FALSE);

    }

    @Test(enabled = true, groups = "functest")
    public void startServersTest()
    {
        final var globalSrvOpt = new BsfSrvOptions();
        final var srvs = new ArrayList<ServerContainer>();

        // initialize without any server running
        final var serverController = new ServerController(this.kubeProbe, globalSrvOpt, srvs);

        srvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV4));
        srvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV4TLS));
        srvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV6));
        srvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV6TLS));

        serverController.startServers(globalSrvOpt, srvs).blockingGet();

        srvs.stream().map(ServerContainer::getTag).map(this::checkServerUp).forEach(res -> assertTrue(res.booleanValue(), SRV_NOT_UP));

        final var startedSrvs = serverController.getCurrentServers();

        final var updatedStartedSrvs = srvs.stream().allMatch(startedSrvs::contains);
        assertTrue(updatedStartedSrvs, "Current Servers are not updated with all started servers");

        serverController.stopAllRunningServers().blockingGet();
    }

    @Test(enabled = true, groups = "functest")
    public void startNoServersTest()
    {
        final var oldGlobalSrvOpt = new BsfSrvOptions();
        final var currentSrvs = new ArrayList<ServerContainer>();

        currentSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV4));
        currentSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV6));

        // initialize
        final var serverController = new ServerController(this.kubeProbe, oldGlobalSrvOpt, currentSrvs);
        // start current servers
        currentSrvs.forEach(server -> serverController.startServer(server).blockingGet());

        final var newSrvs = new ArrayList<ServerContainer>();

        newSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV4));
        newSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV6));
        newSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV4TLS));
        newSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV6TLS));

        serverController.startServers(null, newSrvs).blockingGet();

        // assert all old servers are still running
        currentSrvs.stream().map(ServerContainer::getTag).map(this::checkServerUp).forEach(res -> assertTrue(res.booleanValue(), SRV_NOT_UP));

        // assert no new (TLS) server is running
        currentSrvs.stream()
                   .map(ServerContainer::getTag)
                   .filter(tag -> tag.getDescription().contains("TLS"))
                   .map(this::checkServerUp)
                   .forEach(res -> assertFalse(res.booleanValue(), SRV_UP));

        final var updatedCurrentSrvs = serverController.getCurrentServers();

        // assert old servers exist in updated currentServers
        final var allOldServersFound = updatedCurrentSrvs.containsAll(currentSrvs);
        assertTrue(allOldServersFound, "Old valid server is missing from updated CurrentServers");

        // assert no new server exist in updated currentServers
        final var newServerFound = newSrvs.stream().anyMatch(updatedCurrentSrvs::contains);
        assertFalse(newServerFound, "new invalid Servers found in updated currentServers");

        // assert that globalServerOptions is not updated with the invalid value (null)
        assertEquals(oldGlobalSrvOpt, serverController.getServerGlobalOptions());

        serverController.stopAllRunningServers().blockingGet();
    }

    @Test(enabled = true, groups = "functest")
    public void startServersAddNewServersTest()
    {
        final var globalSrvOpt = new BsfSrvOptions();
        final var currentSrvs = new ArrayList<ServerContainer>();
        currentSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV4));
        currentSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV6));

        // initialize with only nonTls servers running
        final var serverController = new ServerController(this.kubeProbe, globalSrvOpt, currentSrvs);

        // start current servers
        currentSrvs.forEach(server -> serverController.startServer(server).blockingGet());

        // prepare new servers
        final var newSrvs = new ArrayList<ServerContainer>();
        // re-generate nonTls servers as the actual code do in bsf-worker
        // they are different java objects but should not replace the old, since the old
        // are still valid.
        newSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV4));
        newSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV6));
        newSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV4TLS));
        newSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV6TLS));

        serverController.startServers(globalSrvOpt, newSrvs).blockingGet();

        // assert all new servers are up
        newSrvs.stream().map(ServerContainer::getTag).map(this::checkServerUp).forEach(res -> assertTrue(res.booleanValue(), SRV_NOT_UP));

        final var updatedCurrentServers = serverController.getCurrentServers();
        final var updatedCurrentServerTags = updatedCurrentServers.stream().map(ServerContainer::getTag).toList();
        // assert current server are updated with all started servers
        final var allServerTypesFound = newSrvs.stream().map(ServerContainer::getTag).allMatch(updatedCurrentServerTags::contains);
        assertTrue(allServerTypesFound, "Current Servers are not updated with all started servers");

        // verify that the new nonTls servers did not replace old ones
        final var newNonTlsSrvsNotStarted = newSrvs.stream()
                                                   .filter(srv -> !srv.getTag().getDescription().contains("TLS"))
                                                   .anyMatch(updatedCurrentServers::contains);
        assertFalse(newNonTlsSrvsNotStarted, "New nonTls servers replaced the valid old ones");

        // new TLS servers exist in updated CurrentServers
        final var newTlsSrvsStarted = newSrvs.stream().filter(srv -> srv.getTag().getDescription().contains("TLS")).allMatch(updatedCurrentServers::contains);
        assertTrue(newTlsSrvsStarted, "New Tls servers not started");
        serverController.stopAllRunningServers().blockingGet();
    }

    @Test(enabled = true, groups = "functest")
    public void stopServersTest()
    {
        final var currentSrvs = new ArrayList<ServerContainer>();
        final var globalSrvOpt = new BsfSrvOptions();
        currentSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV4));
        currentSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV6));

        // initialize with only nonTls servers running
        final var serverController = new ServerController(this.kubeProbe, globalSrvOpt, currentSrvs);

        // start current servers
        currentSrvs.forEach(server -> serverController.startServer(server).blockingGet());

        // stop all running servers
        final var newSrvs = new ArrayList<ServerContainer>();

        serverController.stopServers(globalSrvOpt, newSrvs).blockingGet();

        currentSrvs.stream()
                   .map(ServerContainer::getTag)
                   .map(this::checkServerUp)
                   .forEach(res -> assertFalse(res.booleanValue(), "At least one server is still up"));

        final var updatedCurrentSrvs = serverController.getCurrentServers();
        assertTrue(updatedCurrentSrvs.isEmpty(), "Current srvs did not updated successfully, all stopped but updatedCurrentSrvs is not empty");
    }

    @Test(enabled = true, groups = "functest")
    public void stopServersRemoveOldServersTest()
    {
        final var globalSrvOpt = new BsfSrvOptions();
        final var currentSrvs = new ArrayList<ServerContainer>();
        currentSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV4));
        currentSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV4TLS));
        currentSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV6));
        currentSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV6TLS));

        // initialize with only nonTls servers running
        final var serverController = new ServerController(this.kubeProbe, globalSrvOpt, currentSrvs);

        // start current servers
        currentSrvs.forEach(server -> serverController.startServer(server).blockingGet());

        // stop TLS servers. NewServers contain only what we want to be up and running
        final var newSrvs = new ArrayList<ServerContainer>();
        newSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV4));
        newSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV6));

        serverController.stopServers(globalSrvOpt, newSrvs).blockingGet();

        // Assert TLS servers has been stopped
        currentSrvs.stream()
                   .map(ServerContainer::getTag)
                   .filter(tag -> tag.getDescription().contains("TLS"))
                   .map(this::checkServerUp)
                   .forEach(res -> assertFalse(res.booleanValue(), "At least one server is still up"));

        // Assert NonTLS servers are still running
        currentSrvs.stream()
                   .map(ServerContainer::getTag)
                   .filter(tag -> !tag.getDescription().contains("TLS"))
                   .map(this::checkServerUp)
                   .forEach(res -> assertTrue(res.booleanValue(), SRV_NOT_UP));

        final var updatedCurrentSrvs = serverController.getCurrentServers();

        // Assert that the old nonTLS servers exist in the updated currentSrvs
        final var foundAllNonTls = currentSrvs.stream().filter(srv -> !srv.getTag().getDescription().contains("TLS")).allMatch(updatedCurrentSrvs::contains);
        assertTrue(foundAllNonTls, "At least one old nonTLS srv is missing from the updated CurrentServers.");

        // Assert that the old TLS servers don't exist in the updated currentSrvs
        final var foundTls = currentSrvs.stream().filter(srv -> srv.getTag().getDescription().contains("TLS")).anyMatch(updatedCurrentSrvs::contains);
        assertFalse(foundTls, "At least one TLS srv found in updated CurrentServers");

        serverController.stopAllRunningServers().blockingGet();
    }

    @Test(enabled = true, groups = "functest")
    public void end2endServerControllerTest()
    {
        final var currentGlobalSrvOpt = new BsfSrvOptions(false, 2048L, 0);
        final var currentSrvs = new ArrayList<ServerContainer>();
        currentSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV4));
        currentSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV4TLS));
        currentSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV6));
        currentSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV6TLS));

        // initialize with only nonTls servers running
        final var serverController = new ServerController(this.kubeProbe, currentGlobalSrvOpt, currentSrvs);

        // start current servers
        currentSrvs.forEach(server -> serverController.startServer(server).blockingGet());

        // initialize readiness
        serverController.decideToSetReadinessToTrue().flatMapCompletable(res -> serverController.updateReadinessState(res.booleanValue())).blockingGet();
        assertTrue(this.kubeProbe.isReady(), READINESS_EXPECTED_TRUE);

        // simulate configuration change and run the functions bsf-worker uses
        // enable tracing
        final var newGlobalSrvOpt = new BsfSrvOptions(true, 2048L, 0);
        final var newSrvs = new ArrayList<ServerContainer>();
        newSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV4));
        newSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV4TLS));
        newSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV6));
        newSrvs.add(generateWebServerContainer(ServerContainer.ServerTag.IPV6TLS));

        // assert readiness behavior - readiness -> false
        final var readinessToFalse = serverController.decideToSetReadinessToFalse(newGlobalSrvOpt, newSrvs).blockingGet();

        assertTrue(readinessToFalse.booleanValue(), "Expected readiness switch to False");
        serverController.updateReadinessState(false).blockingGet();
        assertFalse(this.kubeProbe.isReady(), "Should not be ready but it is");

        // stop/start servers
        serverController.stopServers(newGlobalSrvOpt, newSrvs).andThen(serverController.startServers(newGlobalSrvOpt, newSrvs)).blockingGet();

        // assert all servers are up and running
        newSrvs.stream().map(ServerContainer::getTag).map(this::checkServerUp).forEach(res -> assertTrue(res.booleanValue(), SRV_NOT_UP));

        // assert updated CurrentServers (serverController) is updated accordingly (only
        // new servers are there)
        final var updatedCurrentSrvs = serverController.getCurrentServers();

        // assert all new servers exist in updated CurrentServers (serverController)
        final var allNewServersFound = updatedCurrentSrvs.containsAll(newSrvs);
        assertTrue(allNewServersFound, "At least one new server is missing from updated CurrentServers");

        // assert none of ols servers exist in updated CurrentServers (serverController)
        final var oldServersFound = currentSrvs.stream().anyMatch(updatedCurrentSrvs::contains);
        assertFalse(oldServersFound, "At least one old server found in updated CurrentServers");

        // assert updated BsfSrvOptions
        final var updatedOptions = serverController.getServerGlobalOptions();
        assertEquals(newGlobalSrvOpt, updatedOptions);

        // assert readiness behavior - readiness -> true
        serverController.decideToSetReadinessToTrue().test().assertValue(Boolean.TRUE);
        serverController.updateReadinessState(true).blockingGet();
        assertTrue(this.kubeProbe.isReady(), READINESS_EXPECTED_TRUE);

        serverController.stopAllRunningServers().blockingGet();

    }

    private ServerContainer generateWebServerContainer(final ServerContainer.ServerTag tag)
    {
        final var tls = tag.getDescription().contains("TLS");
        final var ipv4 = tag.getDescription().contains("IPv4");
        final var port = tls ? LOCAL_TLS_PORT : LOCAL_NON_TLS_PORT;
        final var host = ipv4 ? HOST_IPV4 : HOST_IPV6;

        var webSrvPoolBuilder = WebServer.builder() //
                                         .withHost(host)
                                         .withPort(port);
        if (tls)
        {
            webSrvPoolBuilder.withDynamicTls(DynamicTlsCertManager.create(() -> Flowable.just(KeyCert.create(this.clientCert.getPrivateKey(),
                                                                                                             this.clientCert.getCertificate())),
                                                                          () -> Flowable.just(TrustedCert.create(List.of(this.rootCa.getCertificate()))),
                                                                          AdvancedTlsX509TrustManager.newBuilder()
                                                                                                     .setVerification(Verification.CERTIFICATE_ONLY_VERIFICATION)
                                                                                                     .build()));
        }

        final var webSrvPool = webSrvPoolBuilder.build(vertx, 1);

        final var handlers = new ArrayList<NBsfManagementHandler>();
        webSrvPool.childRouters()
                  .forEach(router -> handlers.add(new NBsfManagementHandler(router,
                                                                            this.nbsfManagementService,
                                                                            this.bindingCleanup,
                                                                            this.bsfConfig,
                                                                            false,
                                                                            this.tokenCache)));

        return new ServerContainer(tag, webSrvPool, handlers);
    }

    private boolean checkServerUp(final ServerContainer.ServerTag tag)
    {
        final var tls = tag.getDescription().contains("TLS");
        final var ipv4 = tag.getDescription().contains("IPv4");
        final var port = tls ? LOCAL_TLS_PORT : LOCAL_NON_TLS_PORT;
        final var host = ipv4 ? HOST_IPV4 : HOST_IPV6;
        // if port is not available this means that is used by the server, so server is
        // up
        final var res = HelperHttp.isPortAvailable(port, host);
        log.debug("Srv {}: is up: {}", tag.getDescription(), !res);
        return !res;
    }

    private GeneratedCert generateClientCert(final GeneratedCert rootCa)
    {
        try
        {
            return CertificateTool.createCertificateSignedByRoot("testtest", 2048, 365, "client.ericsson.com", List.of(), rootCa);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private GeneratedCert generateCaCert()
    {
        try
        {
            return CertificateTool.createCertificateAuthority("testca", 2048, 365, "rootca.ericsson.com");
        }
        catch (Exception e)
        {
            log.error("Exception caught while generating root CA: ", e);
            throw new RuntimeException(e);
        }
    }

}
