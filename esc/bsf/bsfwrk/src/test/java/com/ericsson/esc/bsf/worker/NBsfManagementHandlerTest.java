/**
 * COPYRIGHT ERICSSON GMBH 2020
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Apr 12, 2021
 *     Author: echfari
 */
package com.ericsson.esc.bsf.worker;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.PlmnId;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.Snssai;
import com.ericsson.esc.bsf.db.MockNbsfManagementService;
import com.ericsson.esc.bsf.db.MockNbsfManagementService.State;
import com.ericsson.esc.jwt.AdditionalClaims;
import com.ericsson.esc.jwt.JOSEHeader;
import com.ericsson.esc.jwt.JWSPayload;
import com.ericsson.esc.jwt.JWTGenerator;
import com.ericsson.sc.bsf.model.Nrf;
import com.ericsson.sc.nfm.model.AllowedPlmn;
import com.ericsson.sc.nfm.model.NfProfile;
import com.ericsson.sc.nfm.model.Oauth2KeyProfile;
import com.ericsson.sc.nfm.model.Oauth2KeyProfile.Alg;
import com.ericsson.sc.nfm.model.Oauth2KeyProfile.Type;
import com.ericsson.sc.nfm.model.Plmn;
import com.ericsson.sc.nfm.model.Snssai1;
import com.ericsson.utilities.cassandra.RxSession;
import com.ericsson.utilities.http.WebServer;
import com.ericsson.utilities.reactivex.VertxInstance;
import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.jwk.RSAKey;

import io.prometheus.client.Counter.Child;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.reactivex.core.MultiMap;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.http.HttpServerRequest;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import io.vertx.reactivex.ext.web.client.WebClient;

public class NBsfManagementHandlerTest
{
    private enum ServerType
    {
        WEBSERVER,
        NFPEERINFO,
        OAUTH2
    }

    private static final Logger log = LoggerFactory.getLogger(NBsfManagementHandlerTest.class);
    private static final String LOCAL_HOST = "127.0.0.69";
    private static final String LOCAL_HOST_PEER_INFO = "127.0.6.66";
    private static final String LOCAL_HOST_OAUTH2 = "127.0.7.77";
    private static final String PEER_INFO_HEADER = "3gpp-Sbi-NF-Peer-Info";
    private static final String OAUTH2_HEADER = "Authorization";
    private static final String REQ_PEER_INFO = "srcinst=DummySrcInst; srcservinst=DummySrcservinst; srcscp=DummySrcscp";
    private static final String EXPECTED_PEER_INFO = "srcinst=nfInstanceId;dstinst=DummySrcInst;dstservinst=DummySrcservinst;dstscp=DummySrcscp";
    private static final String URI = "/nbsf-management/v1/pcfBindings";
    private static final String BEARER = "Bearer %s";

    private static final short OAUTH_HANDLER_SLEEP = 250;

    private final WebServer webServer = WebServer.builder() //
                                                 .withHost(LOCAL_HOST)
                                                 .withPort(0) // use ephemeral port
                                                 .withHttpTracing(true)
                                                 .build(VertxInstance.get());

    private final WebServer webServerPeerInfo = WebServer.builder() //
                                                         .withHost(LOCAL_HOST_PEER_INFO)
                                                         .withPort(0) // use ephemeral port
                                                         .withHttpTracing(true)
                                                         .build(VertxInstance.get());

    private final WebServer webServerOAuth2 = WebServer.builder() //
                                                       .withHost(LOCAL_HOST_OAUTH2)
                                                       .withPort(0) // use ephemeral port
                                                       .withHttpTracing(true)
                                                       .build(VertxInstance.get());

    private final boolean validateCounters = false;

    private static final int bindingTimeoutDefault = 720 * 3600;
    private final String nfInstanceName = "bsf_" + this;

    private final Vertx vertx = VertxInstance.get();

    AsyncCache<String, String> tokenCache = Caffeine.newBuilder().maximumSize(100).buildAsync();

    private final Pair<Boolean, Boolean> oauthDisabled = new Pair<Boolean, Boolean>(Boolean.FALSE, Boolean.FALSE);

    private final Observable<BsfCmConfig> bsfConfig = Observable.just(new BsfCmConfig.Builder().bindingTimeout(bindingTimeoutDefault)
                                                                                               .nfInstanceName(nfInstanceName)
                                                                                               .outMessageHandling(false)
                                                                                               .oAuth(oauthDisabled)
                                                                                               .build());

    private final Observable<BsfCmConfig> bsfConfigPeerInfo = Observable.just(new BsfCmConfig.Builder().bindingTimeout(bindingTimeoutDefault)
                                                                                                       .nfInstanceName(nfInstanceName)
                                                                                                       .nfInstanceId("nfInstanceId")
                                                                                                       .outMessageHandling(true)
                                                                                                       .oAuth(oauthDisabled)
                                                                                                       .build());

    private final MockNbsfManagementService nbsfManagementService = new MockNbsfManagementService();

    private final RxSession rxSession = RxSession.builder().withConfig(DriverConfigLoader.programmaticBuilder().build()).build();
    private final String keyspace = "sample_keyspace";

    private final NBsfManagementHandler bsfManagementHandler = new NBsfManagementHandler(webServer,
                                                                                         nbsfManagementService,
                                                                                         new BindingCleanupManager(this.rxSession,
                                                                                                                   this.keyspace,
                                                                                                                   Flowable.just("sample")),
                                                                                         bsfConfig,
                                                                                         false,
                                                                                         tokenCache);

    private final NBsfManagementHandler bsfManagementHandlerPeerInfo = new NBsfManagementHandler(webServerPeerInfo,
                                                                                                 nbsfManagementService,
                                                                                                 new BindingCleanupManager(this.rxSession,
                                                                                                                           this.keyspace,
                                                                                                                           Flowable.just("sample")),
                                                                                                 bsfConfigPeerInfo,
                                                                                                 false,
                                                                                                 tokenCache);

    private final WebClientOptions httpOptions = new WebClientOptions().setProtocolVersion(HttpVersion.HTTP_2);
    private final WebClient client = WebClient.create(vertx, httpOptions);

    //
    private final RoutingContext ctx = Mockito.mock(RoutingContext.class);
    private final HttpServerRequest req = Mockito.mock(HttpServerRequest.class);
    private final HttpServerResponse resp = Mockito.mock(HttpServerResponse.class);
    private MultiMap reqheaders = MultiMap.caseInsensitiveMultiMap();
    private MultiMap respheaders = MultiMap.caseInsensitiveMultiMap();

    @BeforeClass()
    private void startServer()
    {

        bsfManagementHandler.start().blockingAwait();
        webServer.startListener().blockingAwait();
        log.info("Started web server");

        bsfManagementHandlerPeerInfo.start().blockingAwait();
        webServerPeerInfo.startListener().blockingAwait();
        log.info("Started web server that implements peerInfo");

        webServerOAuth2.startListener().blockingAwait();
        log.info("Started web server that implements oAuth2");
    }

    @BeforeMethod()
    private void setupMock()
    {
        Mockito.when(ctx.request()).thenReturn(req);
        Mockito.when(ctx.response()).thenReturn(resp);
        Mockito.when(req.headers()).thenReturn(reqheaders);
        Mockito.when(resp.headers()).thenReturn(respheaders);

        Mockito.when(resp.putHeader(Mockito.anyString(), Mockito.anyString())).thenAnswer(invocation ->
        {
            String headerName = invocation.getArgument(0);
            String headerValue = invocation.getArgument(1);
            respheaders.add(headerName, headerValue);
            return resp;
        });
        reqheaders.clear();
        respheaders.clear();
    }

    @Test(enabled = true, invocationCount = 1)
    private void successfulRegistrationTest() throws URISyntaxException
    {
        // json formatted data
        // '{"supi":"imsi-12345","gpsi":"msisdn-306972909290","ipv4Addr":"10.0.0.1","dnn":"testDnn","pcfFqdn":"pcf.ericsson.se","pcfIpEndPoints":[{"ipv4Address":"10.0.0.1","transport":"TCP","port":1024}],"pcfDiamHost":"pcf.ericsson.com","pcfDiamRealm":"ericsson.com","snssai":{"sst":2,"sd":"DEADF0"}}'

        final var ccInReq = new Counter(this.bsfManagementHandler.getCounters().getCcInReq("POST", nfInstanceName));

        List<JsonObject> pcfIpEndPointsJson = new ArrayList<>();
        pcfIpEndPointsJson.add(new JsonObject().put("ipv4Address", "10.0.0.1").put("transport", "TCP").put("port", 1024));

        JsonObject snssaiJson = new JsonObject().put("sst", 2).put("sd", "DEADF0");

        JsonObject bindingJson = new JsonObject().put("supi", "imsi-12345")
                                                 .put("gpsi", "msisdn-306972909290")
                                                 .put("ipv4Addr", "10.0.0.1")
                                                 .put("dnn", "testDnn")
                                                 .put("pcfIpEndPoints", pcfIpEndPointsJson)
                                                 .put("pcfDiamHost", "pcf.ericsson.com")
                                                 .put("pcfDiamRealm", "ericsson.com")
                                                 .put("snssai", snssaiJson)
                                                 .put("blablakey", "blablavalue");

        HttpResponse<io.vertx.reactivex.core.buffer.Buffer> result = register(bindingJson).blockingGet();

        assertEquals(result.statusCode(), 201, "Wrong status code");
        assertEquals(result.statusMessage(), "Created", "Wrong status message");
        validateBindingUri(result);

        assertStepped(ccInReq);

    }

    @Test(enabled = true)
    private void successfulRegistrationSuppFeatTest() throws URISyntaxException
    {
        // json formatted data
        // '{"supi":"imsi-12345","gpsi":"msisdn-306972909290","ipv4Addr":"10.0.0.1","dnn":"testDnn","pcfFqdn":"pcf.ericsson.se","pcfIpEndPoints":[{"ipv4Address":"10.0.0.1","transport":"TCP","port":1024}],"pcfDiamHost":"pcf.ericsson.com","pcfDiamRealm":"ericsson.com","snssai":{"sst":2,"sd":"DEADF0"},"suppFeat":"1"}'

        final var ccInReq = new Counter(this.bsfManagementHandler.getCounters().getCcInReq("POST", nfInstanceName));

        List<JsonObject> pcfIpEndPointsJson = new ArrayList<>();
        pcfIpEndPointsJson.add(new JsonObject().put("ipv4Address", "10.0.0.1").put("transport", "TCP").put("port", 1024));

        JsonObject snssaiJson = new JsonObject().put("sst", 2).put("sd", "DEADF0");

        JsonObject bindingJson = new JsonObject().put("supi", "imsi-12345")
                                                 .put("gpsi", "msisdn-306972909290")
                                                 .put("ipv4Addr", "10.0.0.1")
                                                 .put("dnn", "testDnn")
                                                 .put("pcfIpEndPoints", pcfIpEndPointsJson)
                                                 .put("pcfDiamHost", "pcf.ericsson.com")
                                                 .put("pcfDiamRealm", "ericsson.com")
                                                 .put("snssai", snssaiJson)
                                                 .put("suppFeat", "3");// The random 3 value means that the client supports "MultiUeaddr"(as bsf does)
                                                                       // and "BindingUpdate" features

        HttpResponse<io.vertx.reactivex.core.buffer.Buffer> result = register(bindingJson).blockingGet();

        assertEquals(result.statusCode(), 201, "Wrong status code");
        assertEquals(result.statusMessage(), "Created", "Wrong status message");
        assertEquals(result.body().toJsonObject().getString("suppFeat"), "1");
        validateResponseBinding(result.body().toJsonObject(), bindingJson);

        assertStepped(ccInReq);
    }

    @Test(enabled = true)
    private void successfulRegistrationWithoutSuppFeatTest() throws URISyntaxException
    {
        // json formatted data
        // '{"supi":"imsi-12345","gpsi":"msisdn-306972909290","ipv4Addr":"10.0.0.1","dnn":"testDnn","pcfFqdn":"pcf.ericsson.se","pcfIpEndPoints":[{"ipv4Address":"10.0.0.1","transport":"TCP","port":1024}],"pcfDiamHost":"pcf.ericsson.com","pcfDiamRealm":"ericsson.com","snssai":{"sst":2,"sd":"DEADF0"},"suppFeat":"1"}'

        final var ccInReq = new Counter(this.bsfManagementHandler.getCounters().getCcInReq("POST", nfInstanceName));

        List<JsonObject> pcfIpEndPointsJson = new ArrayList<>();
        pcfIpEndPointsJson.add(new JsonObject().put("ipv4Address", "10.0.0.1").put("transport", "TCP").put("port", 1024));

        JsonObject snssaiJson = new JsonObject().put("sst", 2).put("sd", "DEADF0");

        JsonObject bindingJson = new JsonObject().put("supi", "imsi-12345")
                                                 .put("gpsi", "msisdn-306972909290")
                                                 .put("ipv4Addr", "10.0.0.1")
                                                 .put("dnn", "testDnn")
                                                 .put("pcfIpEndPoints", pcfIpEndPointsJson)
                                                 .put("pcfDiamHost", "pcf.ericsson.com")
                                                 .put("pcfDiamRealm", "ericsson.com")
                                                 .put("snssai", snssaiJson);

        HttpResponse<io.vertx.reactivex.core.buffer.Buffer> result = register(bindingJson).blockingGet();

        assertEquals(result.statusCode(), 201, "Wrong status code");
        assertEquals(result.statusMessage(), "Created", "Wrong status message");
        assertTrue(result.body().toJsonObject().getString("suppFeat") == null);
        validateResponseBinding(result.body().toJsonObject(), bindingJson);

        assertStepped(ccInReq);
    }

    public void assertStepped(Counter... cnts)
    {
        if (validateCounters)
        {
            try
            {
                this.webServer.stopListener().blockingAwait();
                for (var i = 0; i < cnts.length; i++)
                {
                    assertTrue(cnts[i].stepped(), "Counter not stepped:" + i);
                }
            }
            finally
            {
                this.webServer.startListener().blockingAwait();
            }
        }
    }

    static class Counter
    {
        private final Child child;
        private final double initialValue;

        Counter(Child child)
        {
            this.child = child;
            this.initialValue = child.get();
        }

        public Child get()
        {
            return this.child;
        }

        public boolean stepped()
        {
            return Math.round(child.get() - initialValue) == 1;
        }

        @Override
        public String toString()
        {
            return "initialValue: " + this.initialValue + " cnt: " + child;
        }
    }

    @Test(enabled = true, invocationCount = 1)
    private void unexpectedErrorTest()
    {
        try
        {
            this.nbsfManagementService.setState(State.ERROR);
            final var ccInReq = new Counter(this.bsfManagementHandler.getCounters().getCcInReq("POST", nfInstanceName));
            final var ccOutAns = new Counter(this.bsfManagementHandler.getCounters().getCcOutAns("POST", "500 Internal Server Error", nfInstanceName));

            List<JsonObject> pcfIpEndPointsJson = new ArrayList<>();
            pcfIpEndPointsJson.add(new JsonObject().put("ipv4Address", "10.0.0.1").put("transport", "TCP").put("port", 1024));

            JsonObject snssaiJson = new JsonObject().put("sst", 2).put("sd", "DEADF0");

            JsonObject bindingJson = new JsonObject().put("supi", "imsi-12345")
                                                     .put("gpsi", "msisdn-306972909290")
                                                     .put("ipv4Addr", "10.0.0.1")
                                                     .put("dnn", "testDnn")
                                                     .put("pcfIpEndPoints", pcfIpEndPointsJson)
                                                     .put("pcfDiamHost", "pcf.ericsson.com")
                                                     .put("pcfDiamRealm", "ericsson.com")
                                                     .put("snssai", snssaiJson);

            HttpResponse<io.vertx.reactivex.core.buffer.Buffer> result = register(bindingJson).blockingGet();

            assertEquals(result.statusCode(), 500, "Wrong status code");
            assertStepped(ccInReq, ccOutAns);
        }
        finally
        {
            this.nbsfManagementService.setState(State.OK);
        }
    }

    private final void validateBindingUri(HttpResponse<Buffer> result)
    {
        try
        {
            final var bindingUri = new URI(result.getHeader("Location"));

            log.info("Expected: {}", "http://" + LOCAL_HOST + ":" + this.webServer.actualPort() + "/nbsf-management/v1/pcfBindings/");

            log.info("Found: {}", bindingUri);

            assertTrue(bindingUri.toASCIIString().startsWith("http://" + LOCAL_HOST + ":" + this.webServer.actualPort() + "/nbsf-management/v1/pcfBindings/"),
                       "Unexpected binding URI " + bindingUri.toASCIIString());
        }
        catch (Exception e)
        {
            assertTrue(false, "Failed to validate binding URI: " + e);
        }
    }

    private final void validateResponseBinding(JsonObject resultBinding,
                                               JsonObject requestBinding)
    {
        try
        {
            if (resultBinding.getString("suppFeat") != null)
            {
                resultBinding.remove("suppFeat");
                requestBinding.remove("suppFeat");
            }
            assertTrue(resultBinding.equals(requestBinding), "The JSON object of the request and the response does not match");
        }
        catch (Exception e)
        {
            assertTrue(false, "Failed to validate binding : " + e);
        }
    }

    @Test(enabled = true)
    private void successfulRegistrationIPv6Test() throws URISyntaxException
    {
        // json formatted data
        // '{"supi":"imsi-12345","gpsi":"msisdn-306972909290","ipv6Prefix":"2001:db8:abcd:0012::0/64","dnn":"testDnn","pcfFqdn":"pcf.ericsson.se","pcfIpEndPoints":[{"ipv4Address":"10.0.0.1","transport":"TCP","port":1024}],"pcfDiamHost":"pcf.ericsson.com","pcfDiamRealm":"ericsson.com","snssai":{"sst":2,"sd":"DEADF0"}}'

        List<JsonObject> pcfIpEndPointsJson = new ArrayList<>();
        pcfIpEndPointsJson.add(new JsonObject().put("ipv4Address", "10.0.0.1").put("transport", "TCP").put("port", 1024));

        JsonObject snssaiJson = new JsonObject().put("sst", 2).put("sd", "DEADF0");

        JsonObject bindingJson = new JsonObject().put("supi", "imsi-12345")
                                                 .put("gpsi", "msisdn-306972909290")
                                                 .put("ipv6Prefix", "2001:db8:abcd:0012::0/64")
                                                 .put("dnn", "testDnn")
                                                 .put("pcfIpEndPoints", pcfIpEndPointsJson)
                                                 .put("pcfDiamHost", "pcf.ericsson.com")
                                                 .put("pcfDiamRealm", "ericsson.com")
                                                 .put("snssai", snssaiJson);

        HttpResponse<io.vertx.reactivex.core.buffer.Buffer> result = register(bindingJson).blockingGet();

        assertEquals(result.statusCode(), 201, "Wrong status code");
        assertEquals(result.statusMessage(), "Created", "Wrong status message");

        validateBindingUri(result);
    }

    @Test(enabled = true)
    private void successfulRegistrationAddIPv6PcfSetIdBindLevelTest() throws URISyntaxException
    {
        // json formatted data
        // '{"supi":"imsi-12345","gpsi":"msisdn-306972909290","ipv6Prefix":"2001:db8:abcd:0012::0/64","dnn":"testDnn",
        // "pcfIpEndPoints":[{"ipv4Address":"10.0.0.1","transport":"TCP","port":"1024"}],"pcfDiamHost":"pcf.ericsson.com",
        // "pcfDiamRealm":"ericsson.com","snssai":{"sst":2,"sd":"DEADF0"},"addIpv6Prefixes":["2002:1234:5678:1234::/64","2002:1234:5678:1235::/64"],
        // "pcfSetId":"set12.pcfset.5gc.mnc012.mcc345","bindLevel":"NF_INSTANCE"}'

        List<JsonObject> pcfIpEndPointsJson = new ArrayList<>();
        pcfIpEndPointsJson.add(new JsonObject().put("ipv4Address", "10.0.0.1").put("transport", "TCP").put("port", 1024));
        List<String> addIpv6Prefixes = new ArrayList<>();
        addIpv6Prefixes.add("2002:1234:5678:1234:0:0:0:0/64");
        addIpv6Prefixes.add("2002:1234:5678:1235:0:0:0:0/64");

        JsonObject snssaiJson = new JsonObject().put("sst", 2).put("sd", "DEADF0");

        JsonObject bindingJson = new JsonObject().put("supi", "imsi-12345")
                                                 .put("gpsi", "msisdn-306972909290")
                                                 .put("ipv6Prefix", "2001:db8:abcd:12:0:0:0:0/64")
                                                 .put("dnn", "testDnn")
                                                 .put("pcfIpEndPoints", pcfIpEndPointsJson)
                                                 .put("pcfDiamHost", "pcf.ericsson.com")
                                                 .put("pcfDiamRealm", "ericsson.com")
                                                 .put("snssai", snssaiJson)
                                                 .put("addIpv6Prefixes", addIpv6Prefixes)
                                                 .put("pcfSetId", "set12.pcfset.5gc.mnc012.mcc345")
                                                 .put("bindLevel", "NF_INSTANCE");

        HttpResponse<io.vertx.reactivex.core.buffer.Buffer> result = register(bindingJson).blockingGet();

        log.info("register query json: {}", bindingJson);
        log.info("result json: {}", result.body());

        assertEquals(result.statusCode(), 201, "Wrong status code");
        assertEquals(result.statusMessage(), "Created", "Wrong status message");
        validateResponseBinding(result.body().toJsonObject(), bindingJson);

        validateBindingUri(result);
    }

    @Test(enabled = true)
    private void successfulRegistrationAddMacAddressPcfSetIdBindLevelTest() throws URISyntaxException
    {
        // json formatted data
        // '{"supi":"imsi-12345","gpsi":"msisdn-306972909290","macAddr48":"10-65-30-69-46-8D","dnn":"testDnn",
        // "pcfIpEndPoints":[{"ipv4Address":"10.0.0.1","transport":"TCP","port":"1024"}],"pcfDiamHost":"pcf.ericsson.com",
        // "pcfDiamRealm":"ericsson.com","snssai":{"sst":2,"sd":"DEADF0"},"addMacAddrs":["10-65-30-69-46-6D","10-65-30-69-46-7D"],
        // "pcfSetId":"set12.pcfset.5gc.mnc012.mcc345","bindLevel":"NF_INSTANCE"}'

        List<JsonObject> pcfIpEndPointsJson = new ArrayList<>();
        pcfIpEndPointsJson.add(new JsonObject().put("ipv4Address", "10.0.0.1").put("transport", "TCP").put("port", 1024));
        List<String> addMacAddrs = new ArrayList<>();
        addMacAddrs.add("10-65-30-69-46-6D");
        addMacAddrs.add("10-65-30-69-46-7D");

        JsonObject snssaiJson = new JsonObject().put("sst", 2).put("sd", "DEADF0");

        JsonObject bindingJson = new JsonObject().put("supi", "imsi-12345")
                                                 .put("gpsi", "msisdn-306972909290")
                                                 .put("macAddr48", "10-65-30-69-46-8D")
                                                 .put("dnn", "testDnn")
                                                 .put("pcfIpEndPoints", pcfIpEndPointsJson)
                                                 .put("pcfDiamHost", "pcf.ericsson.com")
                                                 .put("pcfDiamRealm", "ericsson.com")
                                                 .put("snssai", snssaiJson)
                                                 .put("addMacAddrs", addMacAddrs)
                                                 .put("pcfSetId", "set12.pcfset.5gc.mnc012.mcc345")
                                                 .put("bindLevel", "NF_INSTANCE");

        HttpResponse<io.vertx.reactivex.core.buffer.Buffer> result = register(bindingJson).blockingGet();

        log.info("register query json: {}", bindingJson);

        log.info("result json: {}", result.body());

        assertEquals(result.statusCode(), 201, "Wrong status code");
        assertEquals(result.statusMessage(), "Created", "Wrong status message");
        validateResponseBinding(result.body().toJsonObject(), bindingJson);
        validateBindingUri(result);
    }

    @Test(enabled = true)
    private void unsuccessfulRegistrationIPv6InvalidPrefix128Test()
    {
        // json formatted data
        // '{"supi":"imsi-12345","gpsi":"msisdn-306972909290","ipv6Prefix":"2001:db8:abcd:0012:1234:abcd:12ab:34cd/128","dnn":"testDnn","pcfFqdn":"pcf.ericsson.se","pcfIpEndPoints":[{"ipv4Address":"10.0.0.1","transport":"TCP","port":1024}],"pcfDiamHost":"pcf.ericsson.com","pcfDiamRealm":"ericsson.com","snssai":{"sst":2,"sd":"DEADF0"}}'

        List<JsonObject> pcfIpEndPointsJson = new ArrayList<>();
        pcfIpEndPointsJson.add(new JsonObject().put("ipv4Address", "10.0.0.1").put("transport", "TCP").put("port", 1024));

        JsonObject snssaiJson = new JsonObject().put("sst", 2).put("sd", "DEADF0");

        JsonObject bindingJson = new JsonObject().put("supi", "imsi-12345")
                                                 .put("gpsi", "msisdn-306972909290")
                                                 .put("ipv6Prefix", "2001:db8:abcd:0012:1234:abcd:12ab:34cd/128")
                                                 .put("dnn", "testDnn")
                                                 .put("pcfIpEndPoints", pcfIpEndPointsJson)
                                                 .put("pcfDiamHost", "pcf.ericsson.com")
                                                 .put("pcfDiamRealm", "ericsson.com")
                                                 .put("snssai", snssaiJson);

        HttpResponse<io.vertx.reactivex.core.buffer.Buffer> result = register(bindingJson).blockingGet();

        assertEquals(result.statusCode(), 400, "Wrong status code");
        assertEquals(result.statusMessage(), "Bad Request", "Wrong status message");
        assertEquals(result.body().toJsonObject().getString("cause"), "MANDATORY_IE_INCORRECT");

        JsonObject params = result.body().toJsonObject().getJsonArray("invalidParams").getJsonObject(0);
        assertEquals(params.getString("param"), "ipv6Prefix");
        assertEquals(params.getString("reason"), "Invalid ipv6 prefix length 128. Only 64 is supported");

    }

    @Test(enabled = true)
    private void unsuccessfulRegistrationIPV6InvalidPrefix32Test()
    {
        // json formatted data
        // '{"supi":"imsi-12345","gpsi":"msisdn-306972909290","ipv6Prefix":"2001:db8::0/32","dnn":"testDnn","pcfFqdn":"pcf.ericsson.se","pcfIpEndPoints":[{"ipv4Address":"10.0.0.1","transport":"TCP","port":1024}],"pcfDiamHost":"pcf.ericsson.com","pcfDiamRealm":"ericsson.com","snssai":{"sst":2,"sd":"DEADF0"}}'

        List<JsonObject> pcfIpEndPointsJson = new ArrayList<>();
        pcfIpEndPointsJson.add(new JsonObject().put("ipv4Address", "10.0.0.1").put("transport", "TCP").put("port", 1024));

        JsonObject snssaiJson = new JsonObject().put("sst", 2).put("sd", "DEADF0");

        JsonObject bindingJson = new JsonObject().put("supi", "imsi-12345")
                                                 .put("gpsi", "msisdn-306972909290")
                                                 .put("ipv6Prefix", "2001:db8::0/32")
                                                 .put("dnn", "testDnn")
                                                 .put("pcfIpEndPoints", pcfIpEndPointsJson)
                                                 .put("pcfDiamHost", "pcf.ericsson.com")
                                                 .put("pcfDiamRealm", "ericsson.com")
                                                 .put("snssai", snssaiJson);

        HttpResponse<io.vertx.reactivex.core.buffer.Buffer> result = register(bindingJson).blockingGet();

        assertEquals(result.statusCode(), 400, "Wrong status code");
        assertEquals(result.statusMessage(), "Bad Request", "Wrong status message");
        assertEquals(result.body().toJsonObject().getString("cause"), "MANDATORY_IE_INCORRECT");

        JsonObject params = result.body().toJsonObject().getJsonArray("invalidParams").getJsonObject(0);
        assertEquals(params.getString("param"), "ipv6Prefix");
        assertEquals(params.getString("reason"), "Invalid ipv6 prefix length 32. Only 64 is supported");

    }

    @Test(enabled = true)
    private void unsuccessfulRegistrationAddIPV6InvalidPrefix32Test()
    {
        // json formatted data
        // '{"supi":"imsi-12345","gpsi":"msisdn-306972909290","ipv6Prefix":"2001:db8::0/64","dnn":"testDnn",
        // "pcfIpEndPoints":[{"ipv4Address":"10.0.0.1","transport":"TCP","port":"1024"}],"pcfDiamHost":"pcf.ericsson.com",
        // "pcfDiamRealm":"ericsson.com","snssai":{"sst":2,"sd":"DEADF0"},"addIpv6Prefixes":["2002:1234:5678:1234::/64","2002:1234:5678:1235::/32"]}'

        List<JsonObject> pcfIpEndPointsJson = new ArrayList<>();
        pcfIpEndPointsJson.add(new JsonObject().put("ipv4Address", "10.0.0.1").put("transport", "TCP").put("port", 1024));
        List<String> addIpv6Prefixes = new ArrayList<>();
        addIpv6Prefixes.add("2002:1234:5678:1234::/64");
        addIpv6Prefixes.add("2002:1234:5678:1235::/32");

        JsonObject snssaiJson = new JsonObject().put("sst", 2).put("sd", "DEADF0");

        JsonObject bindingJson = new JsonObject().put("supi", "imsi-12345")
                                                 .put("gpsi", "msisdn-306972909290")
                                                 .put("ipv6Prefix", "2001:db8::0/64")
                                                 .put("dnn", "testDnn")
                                                 .put("pcfIpEndPoints", pcfIpEndPointsJson)
                                                 .put("pcfDiamHost", "pcf.ericsson.com")
                                                 .put("pcfDiamRealm", "ericsson.com")
                                                 .put("snssai", snssaiJson)
                                                 .put("addIpv6Prefixes", addIpv6Prefixes);

        log.info("register query json: {}", bindingJson);

        HttpResponse<io.vertx.reactivex.core.buffer.Buffer> result = register(bindingJson).blockingGet();

        assertEquals(result.statusCode(), 400, "Wrong status code");
        assertEquals(result.statusMessage(), "Bad Request", "Wrong status message");
        assertEquals(result.body().toJsonObject().getString("cause"), "MANDATORY_IE_INCORRECT");

        JsonObject params = result.body().toJsonObject().getJsonArray("invalidParams").getJsonObject(0);
        assertEquals(params.getString("param"), "addIpv6Prefixes");
        assertEquals(params.getString("reason"), "Invalid ipv6 prefix length 32. Only 64 is supported");

    }

    @Test(enabled = true)
    private void unsuccessfulRegistrationAddIPV6InvalidPrefixTest()
    {
        // json formatted data
        // '{"supi":"imsi-12345","gpsi":"msisdn-306972909290","ipv6Prefix":"2001:db8::0/64","dnn":"testDnn",
        // "pcfIpEndPoints":[{"ipv4Address":"10.0.0.1","transport":"TCP","port":"1024"}],"pcfDiamHost":"pcf.ericsson.com",
        // "pcfDiamRealm":"ericsson.com","snssai":{"sst":2,"sd":"DEADF0"},"addIpv6Prefixes":["2002:1234:5678:1234::/64","blabla"]}'

        List<JsonObject> pcfIpEndPointsJson = new ArrayList<>();
        pcfIpEndPointsJson.add(new JsonObject().put("ipv4Address", "10.0.0.1").put("transport", "TCP").put("port", 1024));
        List<String> addIpv6Prefixes = new ArrayList<>();
        addIpv6Prefixes.add("2002:1234:5678:1234::/64");
        addIpv6Prefixes.add("blabla");

        JsonObject snssaiJson = new JsonObject().put("sst", 2).put("sd", "DEADF0");

        JsonObject bindingJson = new JsonObject().put("supi", "imsi-12345")
                                                 .put("gpsi", "msisdn-306972909290")
                                                 .put("ipv6Prefix", "2001:db8::0/64")
                                                 .put("dnn", "testDnn")
                                                 .put("pcfIpEndPoints", pcfIpEndPointsJson)
                                                 .put("pcfDiamHost", "pcf.ericsson.com")
                                                 .put("pcfDiamRealm", "ericsson.com")
                                                 .put("snssai", snssaiJson)
                                                 .put("addIpv6Prefixes", addIpv6Prefixes);

        log.info("register query json: {}", bindingJson);

        HttpResponse<io.vertx.reactivex.core.buffer.Buffer> result = register(bindingJson).blockingGet();

        assertEquals(result.statusCode(), 400, "Wrong status code");
        assertEquals(result.statusMessage(), "Bad Request", "Wrong status message");
        assertEquals(result.body().toJsonObject().getString("cause"), "OPTIONAL_IE_INCORRECT");

        JsonObject params = result.body().toJsonObject().getJsonArray("invalidParams").getJsonObject(0);
        assertEquals(params.getString("param"), "addIpv6Prefixes");
        assertEquals(params.getString("reason"), "Invalid addIpv6Prefixes");

    }

    @Test(enabled = true)
    private void unsuccessfulRegistrationAddMacAddressPcfSetIdBindLevelTest() throws URISyntaxException
    {
        // json formatted data
        // '{"supi":"imsi-12345","gpsi":"msisdn-306972909290","macAddr48":"10-65-30-69-46-5D","dnn":"testDnn",
        // "pcfIpEndPoints":[{"ipv4Address":"10.0.0.1","transport":"TCP","port":"1024"}],"pcfDiamHost":"pcf.ericsson.com",
        // "pcfDiamRealm":"ericsson.com","snssai":{"sst":2,"sd":"DEADF0"},"addMacAddrs":["10-65-30-69-46-6D","blabla"],
        // "pcfSetId":"set12.pcfset.5gc.mnc012.mcc345","bindLevel":"NF_INSTANCE"}'

        List<JsonObject> pcfIpEndPointsJson = new ArrayList<>();
        pcfIpEndPointsJson.add(new JsonObject().put("ipv4Address", "10.0.0.1").put("transport", "TCP").put("port", 1024));
        List<String> addMacAddrs = new ArrayList<>();
        addMacAddrs.add("10-65-30-69-46-6D");
        addMacAddrs.add("blabla");

        JsonObject snssaiJson = new JsonObject().put("sst", 2).put("sd", "DEADF0");

        JsonObject bindingJson = new JsonObject().put("supi", "imsi-12345")
                                                 .put("gpsi", "msisdn-306972909290")
                                                 .put("macAddr48", "10-65-30-69-46-5D")
                                                 .put("dnn", "testDnn")
                                                 .put("pcfIpEndPoints", pcfIpEndPointsJson)
                                                 .put("pcfDiamHost", "pcf.ericsson.com")
                                                 .put("pcfDiamRealm", "ericsson.com")
                                                 .put("snssai", snssaiJson)
                                                 .put("addMacAddrs", addMacAddrs)
                                                 .put("pcfSetId", "set12.pcfset.5gc.mnc012.mcc345")
                                                 .put("bindLevel", "NF_INSTANCE");

        HttpResponse<io.vertx.reactivex.core.buffer.Buffer> result = register(bindingJson).blockingGet();

        log.info("register query json: {}", bindingJson);

        log.info("result json: {}", result.body());

        assertEquals(result.statusCode(), 400, "Wrong status code");
        assertEquals(result.statusMessage(), "Bad Request", "Wrong status message");
        assertEquals(result.body().toJsonObject().getString("cause"), "OPTIONAL_IE_INCORRECT");

        JsonObject params = result.body().toJsonObject().getJsonArray("invalidParams").getJsonObject(0);
        assertEquals(params.getString("param"), "addMacAddrs");
        assertEquals(params.getString("reason"), "Invalid addMacAddrs");
    }

    @Test(enabled = true)
    private void unsuccessfulRegistrationEmptyAddMacAddressTest() throws URISyntaxException
    {
        // json formatted data
        // '{"supi":"imsi-12345","gpsi":"msisdn-306972909290","macAddr48":"10-65-30-69-46-8D","dnn":"testDnn",
        // "pcfIpEndPoints":[{"ipv4Address":"10.0.0.1","transport":"TCP","port":"1024"}],"pcfDiamHost":"pcf.ericsson.com",
        // "pcfDiamRealm":"ericsson.com","snssai":{"sst":2,"sd":"DEADF0"},"addMacAddrs":[],
        // "pcfSetId":"set12.pcfset.5gc.mnc012.mcc345","bindLevel":"NF_INSTANCE"}}'

        List<JsonObject> pcfIpEndPointsJson = new ArrayList<>();
        pcfIpEndPointsJson.add(new JsonObject().put("ipv4Address", "10.0.0.1").put("transport", "TCP").put("port", 1024));
        List<String> addMacAddrs = new ArrayList<>();

        JsonObject snssaiJson = new JsonObject().put("sst", 2).put("sd", "DEADF0");

        JsonObject bindingJson = new JsonObject().put("supi", "imsi-12345")
                                                 .put("gpsi", "msisdn-306972909290")
                                                 .put("macAddr48", "10-65-30-69-46-8D")
                                                 .put("dnn", "testDnn")
                                                 .put("pcfIpEndPoints", pcfIpEndPointsJson)
                                                 .put("pcfDiamHost", "pcf.ericsson.com")
                                                 .put("pcfDiamRealm", "ericsson.com")
                                                 .put("snssai", snssaiJson)
                                                 .put("addMacAddrs", addMacAddrs)
                                                 .put("pcfSetId", "set12.pcfset.5gc.mnc012.mcc345")
                                                 .put("bindLevel", "NF_INSTANCE");

        HttpResponse<io.vertx.reactivex.core.buffer.Buffer> result = register(bindingJson).blockingGet();

        log.info("register query json: {}", bindingJson);

        log.info("result json: {}", result.body());

        assertEquals(result.statusCode(), 400, "Wrong status code");
        assertEquals(result.statusMessage(), "Bad Request", "Wrong status message");
        assertEquals(result.body().toJsonObject().getString("cause"), "OPTIONAL_IE_INCORRECT");

        JsonObject params = result.body().toJsonObject().getJsonArray("invalidParams").getJsonObject(0);
        assertEquals(params.getString("param"), "addMacAddrs");
        assertEquals(params.getString("reason"), "Parameter addMacAddrs cannot be an empty list");
    }

    @Test(enabled = true)
    private void unsuccessfulRegistrationEmptyPcfIpEndPointsTest() throws URISyntaxException
    {
        // json formatted data
        // '{"supi":"imsi-12345","gpsi":"msisdn-306972909290","ipv4Addr":"10.0.0.1","dnn":"testDnn","pcfFqdn":"pcf.ericsson.se","pcfIpEndPoints":[],"pcfDiamHost":"pcf.ericsson.com","pcfDiamRealm":"ericsson.com","snssai":{"sst":2,"sd":"DEADF0"},"suppFeat":"1"}'

        final var ccInReq = new Counter(this.bsfManagementHandler.getCounters().getCcInReq("POST", nfInstanceName));

        List<JsonObject> pcfIpEndPointsJson = new ArrayList<>();

        JsonObject snssaiJson = new JsonObject().put("sst", 2).put("sd", "DEADF0");

        JsonObject bindingJson = new JsonObject().put("supi", "imsi-12345")
                                                 .put("gpsi", "msisdn-306972909290")
                                                 .put("ipv4Addr", "10.0.0.1")
                                                 .put("dnn", "testDnn")
                                                 .put("pcfIpEndPoints", pcfIpEndPointsJson)
                                                 .put("pcfDiamHost", "pcf.ericsson.com")
                                                 .put("pcfDiamRealm", "ericsson.com")
                                                 .put("snssai", snssaiJson)
                                                 .put("suppFeat", "3");// The random 3 value means that the client supports "MultiUeaddr"(as bsf does)
                                                                       // and "BindingUpdate" features

        HttpResponse<io.vertx.reactivex.core.buffer.Buffer> result = register(bindingJson).blockingGet();

        assertEquals(result.statusCode(), 400, "Wrong status code");
        assertEquals(result.statusMessage(), "Bad Request", "Wrong status message");
        assertEquals(result.body().toJsonObject().getString("cause"), "OPTIONAL_IE_INCORRECT");

        JsonObject params = result.body().toJsonObject().getJsonArray("invalidParams").getJsonObject(0);
        assertEquals(params.getString("param"), "pcfIpEndPoints");
        assertEquals(params.getString("reason"), "Parameter pcfIpEndPoints cannot be an empty list");

        assertStepped(ccInReq);
    }

    @Test(enabled = true)
    private void unsuccessfulRegistrationIPV6InvalidPrefixTest()
    {
        // json formatted data
        // '{"supi":"imsi-12345","gpsi":"msisdn-306972909290","ipv6Prefix":"2001:db8::0/32","dnn":"testDnn","pcfFqdn":"pcf.ericsson.se","pcfIpEndPoints":[{"ipv4Address":"10.0.0.1","transport":"TCP","port":1024}],"pcfDiamHost":"pcf.ericsson.com","pcfDiamRealm":"ericsson.com","snssai":{"sst":2,"sd":"DEADF0"}}'

        List<JsonObject> pcfIpEndPointsJson = new ArrayList<>();
        pcfIpEndPointsJson.add(new JsonObject().put("ipv4Address", "10.0.0.1").put("transport", "TCP").put("port", 1024));

        JsonObject snssaiJson = new JsonObject().put("sst", 2).put("sd", "DEADF0");

        JsonObject bindingJson = new JsonObject().put("supi", "imsi-12345")
                                                 .put("gpsi", "msisdn-306972909290")
                                                 .put("ipv6Prefix", "thisIsAstring")
                                                 .put("dnn", "testDnn")
                                                 .put("pcfIpEndPoints", pcfIpEndPointsJson)
                                                 .put("pcfDiamHost", "pcf.ericsson.com")
                                                 .put("pcfDiamRealm", "ericsson.com")
                                                 .put("snssai", snssaiJson);

        HttpResponse<io.vertx.reactivex.core.buffer.Buffer> result = register(bindingJson).blockingGet();

        assertEquals(result.statusCode(), 400, "Wrong status code");
        assertEquals(result.statusMessage(), "Bad Request", "Wrong status message");
        assertEquals(result.body().toJsonObject().getString("detail"), "SYNTAX_ERROR");
        assertEquals(result.body().toJsonObject().getString("cause"), "MANDATORY_IE_INCORRECT");

        JsonObject params = result.body().toJsonObject().getJsonArray("invalidParams").getJsonObject(0);
        assertEquals(params.getString("param"), "ipv6Prefix");
        assertEquals(params.getString("reason"), "Invalid IPv6 Prefix");

    }

    @Test(enabled = true)
    private void unsuccessfulRegistrationPcfDiamHostInvalidTest()
    {
        // json formatted data
        // '{"supi":"imsi-12345","gpsi":"msisdn-306972909290","ipv6Prefix":"2001:db8::0/32","dnn":"testDnn","pcfFqdn":"pcf.ericsson.se","pcfIpEndPoints":[{"ipv4Address":"10.0.0.1","transport":"TCP","port":1024}],"pcfDiamHost":"pcfericssoncom","pcfDiamRealm":"ericsson.com","snssai":{"sst":2,"sd":"DEADF0"}}'

        List<JsonObject> pcfIpEndPointsJson = new ArrayList<>();
        pcfIpEndPointsJson.add(new JsonObject().put("ipv4Address", "10.0.0.1").put("transport", "TCP").put("port", 1024));

        JsonObject snssaiJson = new JsonObject().put("sst", 2).put("sd", "DEADF0");

        JsonObject bindingJson = new JsonObject().put("supi", "imsi-12345")
                                                 .put("gpsi", "msisdn-306972909290")
                                                 .put("ipv6Prefix", "2001:db8::0/64")
                                                 .put("dnn", "testDnn")
                                                 .put("pcfIpEndPoints", pcfIpEndPointsJson)
                                                 .put("pcfDiamHost", "pcfericssoncom")
                                                 .put("pcfDiamRealm", "ericsson.com")
                                                 .put("snssai", snssaiJson);

        HttpResponse<io.vertx.reactivex.core.buffer.Buffer> result = register(bindingJson).blockingGet();

        assertEquals(result.statusCode(), 400, "Wrong status code");
        assertEquals(result.statusMessage(), "Bad Request", "Wrong status message");
        assertEquals(result.body().toJsonObject().getString("detail"), "SYNTAX_ERROR");
        assertEquals(result.body().toJsonObject().getString("cause"), "MANDATORY_IE_INCORRECT");

        JsonObject params = result.body().toJsonObject().getJsonArray("invalidParams").getJsonObject(0);
        assertEquals(params.getString("param"), "pcfDiamHost");
        assertEquals(params.getString("reason"), "Invalid pcfDiamHost. It must be of type DiameterIdentity.");

    }

    @Test(enabled = true)
    private void unsuccessfulRegistrationPcfDiamRealmInvalid()
    {
        // json formatted data
        // '{"supi":"imsi-12345","gpsi":"msisdn-306972909290","ipv6Prefix":"2001:db8::0/32","dnn":"testDnn","pcfFqdn":"pcf.ericsson.se","pcfIpEndPoints":[{"ipv4Address":"10.0.0.1","transport":"TCP","port":1024}],"pcfDiamHost":"pcf.ericsson.com","pcfDiamRealm":"-ericsson.com","snssai":{"sst":2,"sd":"DEADF0"}}'

        List<JsonObject> pcfIpEndPointsJson = new ArrayList<>();
        pcfIpEndPointsJson.add(new JsonObject().put("ipv4Address", "10.0.0.1").put("transport", "TCP").put("port", 1024));

        JsonObject snssaiJson = new JsonObject().put("sst", 2).put("sd", "DEADF0");

        JsonObject bindingJson = new JsonObject().put("supi", "imsi-12345")
                                                 .put("gpsi", "msisdn-306972909290")
                                                 .put("ipv6Prefix", "2001:db8::0/64")
                                                 .put("dnn", "testDnn")
                                                 .put("pcfIpEndPoints", pcfIpEndPointsJson)
                                                 .put("pcfDiamHost", "pcf.ericsson.com")
                                                 .put("pcfDiamRealm", "-ericsson.com")
                                                 .put("snssai", snssaiJson);

        HttpResponse<io.vertx.reactivex.core.buffer.Buffer> result = register(bindingJson).blockingGet();

        assertEquals(result.statusCode(), 400, "Wrong status code");
        assertEquals(result.statusMessage(), "Bad Request", "Wrong status message");
        assertEquals(result.body().toJsonObject().getString("detail"), "SYNTAX_ERROR");
        assertEquals(result.body().toJsonObject().getString("cause"), "MANDATORY_IE_INCORRECT");

        JsonObject params = result.body().toJsonObject().getJsonArray("invalidParams").getJsonObject(0);
        assertEquals(params.getString("param"), "pcfDiamRealm");
        assertEquals(params.getString("reason"), "Invalid pcfDiamRealm. It must be of type DiameterIdentity.");

    }

    @Test(enabled = true)
    private void unsuccessfulRegistrationInvalidBindLevelTest()
    {
        // json formatted data
        // '{"supi":"imsi-12345","gpsi":"msisdn-306972909290","ipv6Prefix":"2001:db8::0/64","dnn":"testDnn",
        // "pcfIpEndPoints":[{"ipv4Address":"10.0.0.1","transport":"TCP","port":"1024"}],"pcfDiamHost":"pcf.ericsson.com",
        // "pcfDiamRealm":"ericsson.com","snssai":{"sst":2,"sd":"DEADF0"},"bindLevel":"blabla"}'

        List<JsonObject> pcfIpEndPointsJson = new ArrayList<>();
        pcfIpEndPointsJson.add(new JsonObject().put("ipv4Address", "10.0.0.1").put("transport", "TCP").put("port", 1024));

        JsonObject snssaiJson = new JsonObject().put("sst", 2).put("sd", "DEADF0");

        JsonObject bindingJson = new JsonObject().put("supi", "imsi-12345")
                                                 .put("gpsi", "msisdn-306972909290")
                                                 .put("ipv6Prefix", "2001:db8::0/64")
                                                 .put("dnn", "testDnn")
                                                 .put("pcfIpEndPoints", pcfIpEndPointsJson)
                                                 .put("pcfDiamHost", "pcf.ericsson.com")
                                                 .put("pcfDiamRealm", "ericsson.com")
                                                 .put("snssai", snssaiJson)
                                                 .put("bindLevel", "blabla");

        HttpResponse<io.vertx.reactivex.core.buffer.Buffer> result = register(bindingJson).blockingGet();

        log.info("register query json: {}", bindingJson);
        log.info("result json: {}", result.body());

        assertEquals(result.statusCode(), 400, "Wrong status code");
        assertEquals(result.statusMessage(), "Bad Request", "Wrong status message");
        assertEquals(result.body().toJsonObject().getString("detail"), "SYNTAX_ERROR_OPTIONAL");
        assertEquals(result.body().toJsonObject().getString("cause"), "OPTIONAL_IE_INCORRECT");

        JsonObject params = result.body().toJsonObject().getJsonArray("invalidParams").getJsonObject(0);
        assertEquals(params.getString("param"), "bindLevel");
        assertEquals(params.getString("reason"), "Invalid bindLevel");
    }

    @Test(enabled = true)
    private void registrationDnnMissingTest()
    {
        // json formatted data
        // '{"supi":"imsi-12345","gpsi":"msisdn-306972909290","ipv4Addr":"10.0.0.1","dnn":"testDnn","pcfFqdn":"pcf.ericsson.se","pcfIpEndPoints":[{"ipv4Address":"10.0.0.1","transport":"TCP","port":1024}],"pcfDiamHost":"pcf.ericsson.com","pcfDiamRealm":"ericsson.com","snssai":{"sst":2,"sd":"DEADF0"}}'

        List<JsonObject> pcfIpEndPointsJson = new ArrayList<>();
        pcfIpEndPointsJson.add(new JsonObject().put("ipv4Address", "10.0.0.1").put("transport", "TCP").put("port", 1024));

        JsonObject snssaiJson = new JsonObject().put("sst", 2).put("sd", "DEADF0");

        JsonObject bindingJson = new JsonObject().put("supi", "imsi-12345")
                                                 .put("gpsi", "msisdn-306972909290")
                                                 .put("ipv4Addr", "10.0.0.1")
                                                 // .put("dnn", "testDnn")
                                                 .put("pcfIpEndPoints", pcfIpEndPointsJson)
                                                 .put("pcfDiamHost", "pcf.ericsson.com")
                                                 .put("pcfDiamRealm", "ericsson.com")
                                                 .put("snssai", snssaiJson);

        HttpResponse<io.vertx.reactivex.core.buffer.Buffer> result = register(bindingJson).blockingGet();

        assertEquals(result.statusCode(), 400, "Wrong status code");
        assertEquals(result.statusMessage(), "Bad Request", "Wrong status message");
        assertEquals(result.body().toJsonObject().getString("detail"), "SEMANTIC_ERROR_MISSING_PARAM");
        assertEquals(result.body().toJsonObject().getString("cause"), "MANDATORY_IE_MISSING");

        JsonObject params = result.body().toJsonObject().getJsonArray("invalidParams").getJsonObject(0);

        assertEquals(params.getString("param"), "dnn");
        assertEquals(params.getString("reason"), "dnn is mandatory");
    }

    @Test(enabled = true, invocationCount = 1)
    private void registrationInvalidIpv4addrTest()
    {
        // json formatted data
        // '{"supi":"imsi-12345","gpsi":"msisdn-306972909290","ipv4Addr":"10.0.0.1","dnn":"testDnn","pcfFqdn":"pcf.ericsson.se","pcfIpEndPoints":[{"ipv4Address":"10.0.0.1","transport":"TCP","port":1024}],"pcfDiamHost":"pcf.ericsson.com","pcfDiamRealm":"ericsson.com","snssai":{"sst":2,"sd":"DEADF0"}}'

        final var ccInReq = new Counter(this.bsfManagementHandler.getCounters().getCcInReq("POST", nfInstanceName));
        final var ccOutAns = new Counter(this.bsfManagementHandler.getCounters().getCcOutAns("POST", "400 Bad Request", nfInstanceName));

        List<JsonObject> pcfIpEndPointsJson = new ArrayList<>();
        pcfIpEndPointsJson.add(new JsonObject().put("ipv4Address", "10.0.0.1").put("transport", "TCP").put("port", 1024));

        JsonObject snssaiJson = new JsonObject().put("sst", 2).put("sd", "DEADF0");

        JsonObject bindingJson = new JsonObject().put("supi", "imsi-12345")
                                                 .put("gpsi", "msisdn-306972909290")
                                                 .put("ipv4Addr", "10.400.0.1")
                                                 .put("dnn", "testDnn")
                                                 .put("pcfIpEndPoints", pcfIpEndPointsJson)
                                                 .put("pcfDiamHost", "pcf.ericsson.com")
                                                 .put("pcfDiamRealm", "ericsson.com")
                                                 .put("snssai", snssaiJson);

        HttpResponse<io.vertx.reactivex.core.buffer.Buffer> result = register(bindingJson).blockingGet();

        assertEquals(result.statusCode(), 400, "Wrong status code");
        assertEquals(result.statusMessage(), "Bad Request", "Wrong status message");
        assertEquals(result.body().toJsonObject().getString("detail"), "SYNTAX_ERROR");
        assertEquals(result.body().toJsonObject().getString("cause"), "MANDATORY_IE_INCORRECT");

        JsonObject params = result.body().toJsonObject().getJsonArray("invalidParams").getJsonObject(0);
        assertEquals(params.getString("param"), "ipv4Addr");
        assertEquals(params.getString("reason"), "Invalid IPv4 Address");

        assertStepped(ccInReq, ccOutAns);

    }

    @Test(enabled = true)
    private void discoverIpv4andIpv6Test()
    {
        HashMap<String, String> query = new HashMap<>();

        query.put("ipv4Addr", "10.0.0.1");
        query.put("ipv6Prefix", "2001:db8:abcd:0012:1:2:3:4/128");
        HttpResponse<io.vertx.reactivex.core.buffer.Buffer> result = discover(query).blockingGet();

        assertEquals(result.statusCode(), 400, "Wrong status code");
        assertEquals(result.statusMessage(), "Bad Request", "Wrong status message");
        assertEquals(result.body().toJsonObject().getString("detail"), "SEMANTIC_ERROR_WRONG_PARAM");
        assertEquals(result.body().toJsonObject().getString("cause"), "INVALID_QUERY_PARAM");

        JsonObject params = result.body().toJsonObject().getJsonArray("invalidParams").getJsonObject(0);
        assertEquals(params.getString("param"), "ipv4Addr");
        assertEquals(params.getString("reason"), "Cannot be combined with ipv6Prefix");

    }

    @Test(enabled = true)
    private void discoverIpv6Prefix32Test()
    {
        HashMap<String, String> query = new HashMap<>();

        query.put("ipv6Prefix", "2001:db8::0/32");
        HttpResponse<io.vertx.reactivex.core.buffer.Buffer> result = discover(query).blockingGet();

        assertEquals(result.statusCode(), 400, "Wrong status code");
        assertEquals(result.statusMessage(), "Bad Request", "Wrong status message");
        assertEquals(result.body().toJsonObject().getString("detail"), "SYNTAX_ERROR");
        assertEquals(result.body().toJsonObject().getString("cause"), "MANDATORY_QUERY_PARAM_INCORRECT");

        JsonObject params = result.body().toJsonObject().getJsonArray("invalidParams").getJsonObject(0);
        assertEquals(params.getString("param"), "ipv6Prefix");
        assertEquals(params.getString("reason"), "Only /128 netmask is supported");

    }

    @Test(enabled = true)
    private void discoverInvalidIpv6PrefixTest()
    {
        HashMap<String, String> query = new HashMap<>();

        query.put("ipv6Prefix", "4892:fskj/32");
        HttpResponse<io.vertx.reactivex.core.buffer.Buffer> result = discover(query).blockingGet();

        assertEquals(result.statusCode(), 400, "Wrong status code");
        assertEquals(result.statusMessage(), "Bad Request", "Wrong status message");
        assertEquals(result.body().toJsonObject().getString("detail"), "SYNTAX_ERROR");
        assertEquals(result.body().toJsonObject().getString("cause"), "MANDATORY_QUERY_PARAM_INCORRECT");

        JsonObject params = result.body().toJsonObject().getJsonArray("invalidParams").getJsonObject(0);
        assertEquals(params.getString("param"), "ipv6Prefix");
        assertEquals(params.getString("reason"), "Invalid IPv6 Prefix");

    }

    @Test(enabled = true)
    private void discoverNoIpTest()
    {
        HashMap<String, String> query = new HashMap<>();

        HttpResponse<io.vertx.reactivex.core.buffer.Buffer> result = discover(query).blockingGet();

        assertEquals(result.statusCode(), 400, "Wrong status code");
        assertEquals(result.statusMessage(), "Bad Request", "Wrong status message");
        assertEquals(result.body().toJsonObject().getString("detail"), "SEMANTIC_ERROR_MISSING_PARAM");
        assertEquals(result.body().toJsonObject().getString("cause"), "MANDATORY_QUERY_PARAM_MISSING");

        JsonObject params = result.body().toJsonObject().getJsonArray("invalidParams").getJsonObject(0);
        assertEquals(params.getString("param"), "ipv4Addr");
        assertEquals(params.getString("reason"), "One of ipv4Addr, ipv6Prefix or macAddr48 shall be present as query parameter");

    }

    @Test(enabled = true)
    private void discoverSnssaiNoDnnTest()
    {
        HashMap<String, String> query = new HashMap<>();

        query.put("ipv4Addr", "10.0.0.1");
        query.put("snssai", "{\"sst\":2,\"sd\":\"DEADF0\"}");
        final HttpResponse<io.vertx.reactivex.core.buffer.Buffer> result = discover(query).blockingGet();

        assertEquals(result.statusCode(), 400, "Wrong status code");
        assertEquals(result.statusMessage(), "Bad Request", "Wrong status message");
        assertEquals(result.body().toJsonObject().getString("detail"), "SEMANTIC_ERROR_WRONG_PARAM");
        assertEquals(result.body().toJsonObject().getString("cause"), "INVALID_QUERY_PARAM");

        JsonObject params = result.body().toJsonObject().getJsonArray("invalidParams").getJsonObject(0);
        assertEquals(params.getString("param"), "snssai");
        assertEquals(params.getString("reason"), "snssai requires ipv4Addr and dnn");

    }

    @Test(enabled = true)
    private void discoverSupiAndGpsiTest()
    {
        HashMap<String, String> query = new HashMap<>();

        query.put("ipv4Addr", "10.0.0.1");
        query.put("supi", "imsi-12345");
        query.put("gpsi", "msisdn-306972909290");

        final HttpResponse<io.vertx.reactivex.core.buffer.Buffer> result = discover(query).blockingGet();

        assertEquals(result.statusCode(), 400, "Wrong status code");
        assertEquals(result.statusMessage(), "Bad Request", "Wrong status message");
        assertEquals(result.body().toJsonObject().getString("detail"), "SEMANTIC_ERROR_WRONG_PARAM");
        assertEquals(result.body().toJsonObject().getString("cause"), "INVALID_QUERY_PARAM");

        JsonObject params = result.body().toJsonObject().getJsonArray("invalidParams").getJsonObject(0);
        assertEquals(params.getString("param"), "gpsi");
        assertEquals(params.getString("reason"), "gpsi cannot be combined with supi");

    }

    @Test(enabled = true)
    private void discoverInvalidAddrTest()
    {
        HashMap<String, String> query = new HashMap<>();

        query.put("ipv4Addr", "10.500.0.1");

        HttpResponse<io.vertx.reactivex.core.buffer.Buffer> result = discover(query).blockingGet();

        assertEquals(result.statusCode(), 400, "Wrong status code");
        assertEquals(result.statusMessage(), "Bad Request", "Wrong status message");
        assertEquals(result.body().toJsonObject().getString("detail"), "SYNTAX_ERROR");
        assertEquals(result.body().toJsonObject().getString("cause"), "MANDATORY_QUERY_PARAM_INCORRECT");

        JsonObject params = result.body().toJsonObject().getJsonArray("invalidParams").getJsonObject(0);
        assertEquals(params.getString("param"), "ipv4Addr");
        assertEquals(params.getString("reason"), "Invalid IPv4 Address");

    }

    @Test(enabled = true)
    private void discoverInvalidSnssaiTest()
    {
        HashMap<String, String> query = new HashMap<>();

        query.put("ipv4Addr", "10.0.0.1");
        query.put("snssai", "{\\\"sst\\\":2,\\\"sd\\\":\\\"DEADF01\\\"}");

        HttpResponse<io.vertx.reactivex.core.buffer.Buffer> result = discover(query).blockingGet();

        assertEquals(result.statusCode(), 400, "Wrong status code");
        assertEquals(result.statusMessage(), "Bad Request", "Wrong status message");
        assertEquals(result.body().toJsonObject().getString("detail"), "SYNTAX_ERROR_OPTIONAL");
        assertEquals(result.body().toJsonObject().getString("cause"), "OPTIONAL_QUERY_PARAM_INCORRECT");

        JsonObject params = result.body().toJsonObject().getJsonArray("invalidParams").getJsonObject(0);
        assertEquals(params.getString("param"), "snssai");
        assertEquals(params.getString("reason"), "Invalid S-NSSAI.");

    }

    @Test(enabled = true)
    private void discoverInvalidMacTest()
    {
        HashMap<String, String> query = new HashMap<>();

        query.put("ipv4Addr", "10.0.0.1");
        query.put("macAddr48", "macAddr48");

        HttpResponse<io.vertx.reactivex.core.buffer.Buffer> result = discover(query).blockingGet();

        assertEquals(result.statusCode(), 400, "Wrong status code");
        assertEquals(result.statusMessage(), "Bad Request", "Wrong status message");
        assertEquals(result.body().toJsonObject().getString("detail"), "SYNTAX_ERROR");
        assertEquals(result.body().toJsonObject().getString("cause"), "MANDATORY_QUERY_PARAM_INCORRECT");

        JsonObject params = result.body().toJsonObject().getJsonArray("invalidParams").getJsonObject(0);
        assertEquals(params.getString("param"), "macAddr48");
        assertEquals(params.getString("reason"), "Invalid MAC Address");

    }

    @Test(enabled = true, timeOut = 5000)
    private void jsonHugeNumberDosAttack() throws InterruptedException
    {
        // Send a huge json number(DOS attack test).
        // This TC might hang if huge numbers are not properly rejected
        final var buf = Buffer.buffer(hugeNum(10 * 1000 * 1000));
        final var to = client.post(this.webServer.actualPort(), LOCAL_HOST, URI).putHeader("Content-Type", "application/json").rxSendBuffer(buf).test();
        try
        {
            to.await(2, TimeUnit.SECONDS);
            to.assertComplete();
            to.assertNoErrors();
        }
        finally
        {
            to.dispose();
        }
    }

    // Headers tests
    @Test(enabled = true)
    private void editAll3ValidHeaders()
    {
        final var reqPeerInfo = "srcinst=DummySrcInst; srcservinst=DummySrcservinst; srcscp=DummySrcscp";
        reqheaders.add(PEER_INFO_HEADER, reqPeerInfo);

        bsfManagementHandler.editHeaders(ctx, "nfInstanceId");

        final var respPeerInfo = respheaders.get(PEER_INFO_HEADER);

        assertTrue(respPeerInfo.contains("dstinst=DummySrcInst"), "Header 'dstinst' not found or has different value of expected 'DummySrcInst'");
        assertTrue(respPeerInfo.contains("dstservinst=DummySrcservinst"),
                   "Header 'dstservinst' not found or has different value of expected 'DummySrcservinst'");
        assertTrue(respPeerInfo.contains("dstscp=DummySrcscp"), "Header 'dstscp' not found or has different value of expected 'DummySrcscp'");
        assertTrue(respPeerInfo.contains("srcinst=nfInstanceId"), "Header 'srcinst' not found or has different value of expected 'nfInstanceId'");

    }

    @Test(enabled = true)
    private void editHeadersIgnoreDstscp()
    {
        final var reqPeerInfo = "srcinst=DummySrcInst; srcservinst=DummySrcservinst; srcscp=DummySrcscp; dstscp=DummyDstscpToBeIgnored";
        reqheaders.add(PEER_INFO_HEADER, reqPeerInfo);

        bsfManagementHandler.editHeaders(ctx, "nfInstanceId");

        final var respPeerInfo = respheaders.get(PEER_INFO_HEADER);

        assertTrue(respPeerInfo.contains("dstinst=DummySrcInst"), "Header 'dstinst' not found or has different value of expected 'DummySrcInst'");
        assertTrue(respPeerInfo.contains("dstservinst=DummySrcservinst"),
                   "Header 'dstservinst' not found or has different value of expected 'DummySrcservinst'");
        assertTrue(respPeerInfo.contains("dstscp=DummySrcscp"), "Header 'dstscp' not found or has different value of expected 'DummySrcscp'");
        assertTrue(respPeerInfo.contains("srcinst=nfInstanceId"), "Header 'srcinst' not found or has different value of expected 'nfInstanceId'");

    }

    @Test(enabled = true)
    private void editHeadersSrcinstMissing()
    {
        final var reqPeerInfo = "srcservinst=DummySrcservinst; srcscp=DummySrcscp";
        reqheaders.add(PEER_INFO_HEADER, reqPeerInfo);

        bsfManagementHandler.editHeaders(ctx, "nfInstanceId");

        final var respPeerInfo = respheaders.get(PEER_INFO_HEADER);

        assertFalse("Headers contain 'dstinst', while it shouldn't", respPeerInfo.contains("dstinst"));
        assertTrue(respPeerInfo.contains("dstservinst=DummySrcservinst"),
                   "Header 'dstservinst' not found or has different value of expected 'DummySrcservinst'");
        assertTrue(respPeerInfo.contains("dstscp=DummySrcscp"), "Header 'dstscp' not found or has different value of expected 'DummySrcscp'");
        assertTrue(respPeerInfo.contains("srcinst=nfInstanceId"), "Header 'srcinst' not found or has different value of expected 'nfInstanceId'");

    }

    @Test(enabled = true)
    private void editHeadersOnlySrcscp()
    {
        final var reqPeerInfo = "srcscp=DummySrcscp";
        reqheaders.add(PEER_INFO_HEADER, reqPeerInfo);

        bsfManagementHandler.editHeaders(ctx, "nfInstanceId");

        final var respPeerInfo = respheaders.get(PEER_INFO_HEADER);

        assertFalse("Headers contain 'dstinst', while it shouldn't", respPeerInfo.contains("dstinst"));
        assertFalse("Headers contain 'dstservinst', while it shouldn't", respPeerInfo.contains("dstservinst"));
        assertTrue(respPeerInfo.contains("dstscp=DummySrcscp"), "Header 'dstscp' not found or has different value of expected 'DummySrcscp'");
        assertTrue(respPeerInfo.contains("srcinst=nfInstanceId"), "Header 'srcinst' not found or has different value of expected 'nfInstanceId'");

    }

    @Test(enabled = true)
    private void editHeadersNoextraHeaders()
    {
        bsfManagementHandler.editHeaders(ctx, "nfInstanceId");
        final var respPeerInfo = respheaders.get(PEER_INFO_HEADER);

        assertFalse("Headers contain 'dstinst', while it shouldn't", respPeerInfo.contains("dstinst"));
        assertFalse("Headers contain 'dstservinst', while it shouldn't", respPeerInfo.contains("dstservinst"));
        assertFalse("Headers contain 'dstscp', while it shouldn't", respPeerInfo.contains("dstscp"));
        assertTrue(respPeerInfo.contains("srcinst=nfInstanceId"), "Header 'srcinst' not found or has different value of expected 'nfInstanceId'");

    }

    // Headers tests with register/discovery/derigister operations

    @Test(enabled = true)
    private void successfulRegistrationWithHeaderTest() throws URISyntaxException
    {
        // json formatted data
        // '{"supi":"imsi-12345","gpsi":"msisdn-306972909290","ipv4Addr":"10.0.0.1","dnn":"testDnn","pcfFqdn":"pcf.ericsson.se","pcfIpEndPoints":[{"ipv4Address":"10.0.0.1","transport":"TCP","port":1024}],"pcfDiamHost":"pcf.ericsson.com","pcfDiamRealm":"ericsson.com","snssai":{"sst":2,"sd":"DEADF0"}}'

        final var bindingJson = this.createJsonForSimpleRegister();

        final var result = register(bindingJson, REQ_PEER_INFO).blockingGet();

        final var actualHeaders = result.getHeader(PEER_INFO_HEADER);

        assertEquals(actualHeaders, EXPECTED_PEER_INFO, "Wrong headers in response");
        assertEquals(result.statusCode(), 201, "Wrong status code");
        assertEquals(result.statusMessage(), "Created", "Wrong status message");
    }

    @Test(enabled = true)
    private void unsuccessfulRegistrationWithHeaderTest() throws URISyntaxException
    {
        // json formatted data
        // '{"supi":"imsi-12345","gpsi":"msisdn-306972909290","dnn":"testDnn","pcfFqdn":"pcf.ericsson.se","pcfIpEndPoints":[{"ipv4Address":"10.0.0.1","transport":"TCP","port":1024}],"pcfDiamHost":"pcf.ericsson.com","pcfDiamRealm":"ericsson.com","snssai":{"sst":2,"sd":"DEADF0"}}'

        List<JsonObject> pcfIpEndPointsJson = new ArrayList<>();
        pcfIpEndPointsJson.add(new JsonObject().put("transport", "TCP").put("port", 1024));

        JsonObject snssaiJson = new JsonObject().put("sst", 2).put("sd", "DEADF0");

        JsonObject bindingJson = new JsonObject().put("supi", "imsi-12345")
                                                 .put("gpsi", "msisdn-306972909290")
                                                 .put("ipv4Addr", "10.0.0.1")
                                                 .put("dnn", "testDnn")
                                                 .put("pcfIpEndPoints", pcfIpEndPointsJson)
                                                 .put("pcfDiamHost", "pcf.ericsson.com")
                                                 .put("pcfDiamRealm", "ericsson.com")
                                                 .put("snssai", snssaiJson)
                                                 .put("blablakey", "blablavalue");

        final var result = register(bindingJson, REQ_PEER_INFO).blockingGet();

        final var actualHeaders = result.getHeader(PEER_INFO_HEADER);

        assertEquals(actualHeaders, EXPECTED_PEER_INFO, "Wrong headers in response");
        assertEquals(result.statusCode(), 400, "Wrong status code");
        assertEquals(result.statusMessage(), "Bad Request", "Wrong status message");
    }

    @Test(enabled = false) // this should be enabled after the corresponding adaptations that will come in
                           // the future
    private void registrationNoBindingWithHeaderTest() throws URISyntaxException
    {
        HttpResponse<io.vertx.reactivex.core.buffer.Buffer> result = register(null, REQ_PEER_INFO).blockingGet();

        final var actualHeaders = result.getHeader(PEER_INFO_HEADER);

        assertEquals(actualHeaders, EXPECTED_PEER_INFO, "Wrong headers in response");
        assertEquals(result.statusCode(), 400, "Wrong status code");
        assertEquals(result.statusMessage(), "Bad Request", "Wrong status message");
    }

    @Test(enabled = true)
    private void discoverIpv4WithHeaderTest()
    {
        HashMap<String, String> query = new HashMap<>();

        query.put("ipv4Addr", "10.0.0.1");
        HttpResponse<io.vertx.reactivex.core.buffer.Buffer> result = discover(query, REQ_PEER_INFO).blockingGet();
        final var actualHeaders = result.getHeader(PEER_INFO_HEADER);

        assertEquals(actualHeaders, EXPECTED_PEER_INFO, "Wrong headers in response");
        assertEquals(result.statusCode(), 204, "Wrong status code");
        assertEquals(result.statusMessage(), "No Content", "Wrong status message");

    }

    @Test(enabled = true)
    private void discoverIpv4andIpv6WithHeaderTest()
    {
        HashMap<String, String> query = new HashMap<>();

        query.put("ipv4Addr", "10.0.0.1");
        query.put("ipv6Prefix", "2001:db8:abcd:0012:1:2:3:4/128");
        HttpResponse<io.vertx.reactivex.core.buffer.Buffer> result = discover(query, REQ_PEER_INFO).blockingGet();
        final var actualHeaders = result.getHeader(PEER_INFO_HEADER);

        assertEquals(actualHeaders, EXPECTED_PEER_INFO, "Wrong headers in response");
        assertEquals(result.statusCode(), 400, "Wrong status code");
        assertEquals(result.statusMessage(), "Bad Request", "Wrong status message");
        assertEquals(result.body().toJsonObject().getString("detail"), "SEMANTIC_ERROR_WRONG_PARAM");
        assertEquals(result.body().toJsonObject().getString("cause"), "INVALID_QUERY_PARAM");

        JsonObject params = result.body().toJsonObject().getJsonArray("invalidParams").getJsonObject(0);
        assertEquals(params.getString("param"), "ipv4Addr");
        assertEquals(params.getString("reason"), "Cannot be combined with ipv6Prefix");

    }

    @Test(enabled = true)
    private void discoverIpv6Prefix32WithHeaderTest()
    {
        HashMap<String, String> query = new HashMap<>();

        query.put("ipv6Prefix", "2001:db8::0/32");
        HttpResponse<io.vertx.reactivex.core.buffer.Buffer> result = discover(query, REQ_PEER_INFO).blockingGet();
        final var actualHeaders = result.getHeader(PEER_INFO_HEADER);

        assertEquals(actualHeaders, EXPECTED_PEER_INFO, "Wrong headers in response");
        assertEquals(result.statusCode(), 400, "Wrong status code");
        assertEquals(result.statusMessage(), "Bad Request", "Wrong status message");
        assertEquals(result.body().toJsonObject().getString("detail"), "SYNTAX_ERROR");
        assertEquals(result.body().toJsonObject().getString("cause"), "MANDATORY_QUERY_PARAM_INCORRECT");

        JsonObject params = result.body().toJsonObject().getJsonArray("invalidParams").getJsonObject(0);
        assertEquals(params.getString("param"), "ipv6Prefix");
        assertEquals(params.getString("reason"), "Only /128 netmask is supported");

    }

    @Test(enabled = true)
    private void discoverNoIpWithHeaderTest()
    {
        HashMap<String, String> query = new HashMap<>();

        HttpResponse<io.vertx.reactivex.core.buffer.Buffer> result = discover(query, REQ_PEER_INFO).blockingGet();
        final var actualHeaders = result.getHeader(PEER_INFO_HEADER);

        assertEquals(actualHeaders, EXPECTED_PEER_INFO, "Wrong headers in response");
        assertEquals(result.statusCode(), 400, "Wrong status code");
        assertEquals(result.statusMessage(), "Bad Request", "Wrong status message");
        assertEquals(result.body().toJsonObject().getString("detail"), "SEMANTIC_ERROR_MISSING_PARAM");
        assertEquals(result.body().toJsonObject().getString("cause"), "MANDATORY_QUERY_PARAM_MISSING");

        JsonObject params = result.body().toJsonObject().getJsonArray("invalidParams").getJsonObject(0);
        assertEquals(params.getString("param"), "ipv4Addr");
        assertEquals(params.getString("reason"), "One of ipv4Addr, ipv6Prefix or macAddr48 shall be present as query parameter");

    }

    @Test(enabled = true)
    private void deregisterWithHeaderTest()
    {

        final var pcfBindingId = "/" + UUID.randomUUID().toString();
        HttpResponse<io.vertx.reactivex.core.buffer.Buffer> result = deregister(pcfBindingId, REQ_PEER_INFO).blockingGet();
        final var actualHeaders = result.getHeader(PEER_INFO_HEADER);

        assertEquals(actualHeaders, EXPECTED_PEER_INFO, "Wrong headers in response");
        assertEquals(result.statusCode(), 204, "Wrong status code");
        assertEquals(result.statusMessage(), "No Content", "Wrong status message");

    }

    @Test(enabled = true)
    private void deregisterInvalidIdWithHeaderTest()
    {

        HttpResponse<io.vertx.reactivex.core.buffer.Buffer> result = deregister("/thisIsNotaValidId", REQ_PEER_INFO).blockingGet();
        final var actualHeaders = result.getHeader(PEER_INFO_HEADER);

        assertEquals(actualHeaders, EXPECTED_PEER_INFO, "Wrong headers in response");
        assertEquals(result.statusCode(), 400, "Wrong status code");
        assertEquals(result.statusMessage(), "Bad Request", "Wrong status message");

    }

    @Test(enabled = false) // this should be enabled after the corresponding adaptations that will come in
                           // the future
    private void deregisterNoIdWithHeaderTest()
    {

        HttpResponse<io.vertx.reactivex.core.buffer.Buffer> result = deregister("", REQ_PEER_INFO).blockingGet();
        final var actualHeaders = result.getHeader(PEER_INFO_HEADER);

        assertEquals(actualHeaders, EXPECTED_PEER_INFO, "Wrong headers in response");
        assertEquals(result.statusCode(), 405, "Wrong status code");
        assertEquals(result.statusMessage(), "Method Not Allowed", "Wrong status message");

    }

    @Test(enabled = true) // FIXME ES256K is not supported by vert.x

    private void registerWithHttpSuccessfulOauth2Test() throws URISyntaxException, InvalidKeySpecException, NoSuchAlgorithmException, JOSEException, IOException, InterruptedException
    {
        final var oAuthEnabled = new Pair<Boolean, Boolean>(Boolean.TRUE, null);

        final var alg = JWSAlgorithm.HS256;

        final var nfInstanceId = UUID.randomUUID().toString();
        final var nrfInstanceId = UUID.randomUUID().toString();

        final var hmacJwk = JWTGenerator.generateSymmetricKey(alg);
        final var hmacSecretPem = JWTGenerator.generateSecretKeyPemFormatFromJwk(hmacJwk);

        final var joseHeader = this.createSampleJoseHeader(alg);

        final var payload = this.createSampleJwsPayloadBuilderWithIssuer(nrfInstanceId)
                                .withAdditionalClaims(new AdditionalClaims.Builder().withProducerNfSetId("NFSET1")
                                                                                    .withProducerNsiList(List.of("NSI1"))
                                                                                    .withProducerSnssaiList(List.of(new Snssai().sd("ABDD1").sst(2)))
                                                                                    .withProducerPlmnId(new PlmnId().mcc("100").mnc("101"))
                                                                                    .withConsumerPlmnId(new PlmnId().mcc("200").mnc("201"))
                                                                                    .build())
                                .build();

        final var hmacJwt = JWTGenerator.createJwt(joseHeader, payload, hmacJwk, Type.PEM);

        final var keyProfile = new Oauth2KeyProfile().withKeyId("1").withValue(hmacSecretPem).withType(Type.PEM).withAlg(Alg.fromValue(alg.getName()));

        final var bsfConfigOauth2 = Observable.just(this.createSampleOauth2CfgBuilder(nfInstanceId, oAuthEnabled)
                                                        .nfProfiles(List.of(new NfProfile().withNfSetId(List.of("NFSET1"))
                                                                                           .withNsi(List.of("NSI1"))
                                                                                           .withSnssai1(List.of(new Snssai1().withSd("ABDD1").withSst(2)))
                                                                                           .withPlmn(List.of(new Plmn().withMcc("100").withMnc("101")))
                                                                                           .withAllowedPlmn(List.of(new AllowedPlmn().withMcc("200")
                                                                                                                                     .withMnc("201")))))

                                                        .oAuthkeyProfiles(List.of(keyProfile))
                                                        .nrfs(List.of(new Nrf().withNrfInstanceId(nrfInstanceId).withoauth2KeyProfileRef(List.of("1"))))
                                                        .build());

        final var bsfManagementHandlerOauth2 = this.createOauth2Handler(bsfConfigOauth2);

        this.startOauth2Handler(bsfManagementHandlerOauth2);

        try
        {
            final var bindingJson = this.createJsonForSimpleRegister();

            final var result = this.register(bindingJson, String.format(BEARER, hmacJwt), ServerType.OAUTH2).blockingGet();

            assertEquals(result.statusCode(), 201, "Wrong status code");
            assertEquals(result.statusMessage(), "Created", "Wrong status message");
        }
        catch (final Exception ex)
        {
            log.error("TC failed, exception received", ex);
            assertTrue(false, "TC failed due to exception");
        }
        finally
        {
            this.stopOauth2Handler(bsfManagementHandlerOauth2);
        }

    }

    @Test(enabled = true)
    private void registerWithHttpOauth2NullAuthHeaderTest() throws InvalidKeySpecException, NoSuchAlgorithmException, JOSEException, IOException, URISyntaxException, InterruptedException
    {
        final var oAuthEnabled = new Pair<Boolean, Boolean>(Boolean.TRUE, null);

        final var alg = JWSAlgorithm.ES256;

        final var nfInstanceId = UUID.randomUUID().toString();
        final var nrfInstanceId = UUID.randomUUID().toString();

        final var publicKey = ((ECKey) JWTGenerator.generateAsymmetricKeyPair(alg)).toPublicJWK().toJSONString();

        final var keyProfile = new Oauth2KeyProfile().withKeyId("1").withJsonBody(publicKey).withType(Type.JWK);

        final var bsfConfigOauth2 = Observable.just(this.createSampleOauth2CfgBuilder(nfInstanceId, oAuthEnabled)
                                                        .oAuthkeyProfiles(List.of(keyProfile))
                                                        .nrfs(List.of(new Nrf().withNrfInstanceId(nrfInstanceId).withoauth2KeyProfileRef(List.of("1"))))
                                                        .build());

        final var bsfManagementHandlerOauth2 = this.createOauth2Handler(bsfConfigOauth2);

        this.startOauth2Handler(bsfManagementHandlerOauth2);

        final var bindingJson = this.createJsonForSimpleRegister();

        final var result = this.register(bindingJson, null, ServerType.OAUTH2).blockingGet();

        assertEquals(result.statusCode(), 401, "Wrong status code");
        assertEquals(result.statusMessage(), "Unauthorized", "Wrong status message");

        assertEquals(result.bodyAsJsonObject().getValue("detail"),
                     AuthAccessTokenValidator.ErrorType.AUTH_HEADER_MISSING.getProblemDetails(),
                     "Wrong detail message");

        this.stopOauth2Handler(bsfManagementHandlerOauth2);

    }

    @DataProvider
    public Object[] oAuth2HeaderData()
    {
        return new Object[] { "", "Bearer ", "Beaarer " };
    }

    @Test(enabled = true, dataProvider = "oAuth2HeaderData")
    private void registerWithHttpOauthMalformedAuthHeaderTest(final String auth2Header) throws InvalidKeySpecException, NoSuchAlgorithmException, JOSEException, IOException, URISyntaxException, InterruptedException
    {
        final var oAuthEnabled = new Pair<Boolean, Boolean>(Boolean.TRUE, null);

        final var alg = JWSAlgorithm.ES256;

        final var nfInstanceId = UUID.randomUUID().toString();
        final var nrfInstanceId = UUID.randomUUID().toString();

        final var publicKey = ((ECKey) JWTGenerator.generateAsymmetricKeyPair(alg)).toPublicJWK().toJSONString();

        final var keyProfile = new Oauth2KeyProfile().withKeyId("1").withJsonBody(publicKey).withType(Type.JWK);

        final var bsfConfigOauth2 = Observable.just(this.createSampleOauth2CfgBuilder(nfInstanceId, oAuthEnabled)
                                                        .oAuthkeyProfiles(List.of(keyProfile))
                                                        .nrfs(List.of(new Nrf().withNrfInstanceId(nrfInstanceId).withoauth2KeyProfileRef(List.of("1"))))
                                                        .build());

        final var bsfManagementHandlerOauth2 = this.createOauth2Handler(bsfConfigOauth2);

        this.startOauth2Handler(bsfManagementHandlerOauth2);

        final var bindingJson = this.createJsonForSimpleRegister();

        final var result = this.register(bindingJson, auth2Header, ServerType.OAUTH2).blockingGet();

        assertEquals(result.statusCode(), 401, "Wrong status code");
        assertEquals(result.statusMessage(), "Unauthorized", "Wrong status message");

        assertEquals(result.bodyAsJsonObject().getValue("detail"),
                     AuthAccessTokenValidator.ErrorType.INVALID_AUTH_HEADER.getProblemDetails(),
                     "Wrong detail message");

        this.stopOauth2Handler(bsfManagementHandlerOauth2);

    }

    @Test(enabled = true)
    private void registerWithHttpOauthInvalidTokenTest() throws InvalidKeySpecException, NoSuchAlgorithmException, JOSEException, IOException, URISyntaxException, InterruptedException
    {
        final var oAuthEnabled = new Pair<Boolean, Boolean>(Boolean.TRUE, null);

        final var alg = JWSAlgorithm.ES256;

        final var nfInstanceId = UUID.randomUUID().toString();
        final var nrfInstanceId = UUID.randomUUID().toString();

        final var ecJwk = JWTGenerator.generateAsymmetricKeyPair(alg);
        final var ecPublicKeyPem = JWTGenerator.generatePrivatePublicKeysPemFormatFromJwk(ecJwk).getValue1();

        final var joseHeader = this.createSampleJoseHeader(alg);

        final var payload = this.createSampleJwsPayloadBuilderWithIssuer(nrfInstanceId).build();

        final var ecJwt = JWTGenerator.createJwt(joseHeader, payload, ecJwk, Type.PEM).replaceFirst("\\.", "");

        final var keyProfile = new Oauth2KeyProfile().withKeyId("1").withValue(ecPublicKeyPem).withType(Type.PEM);

        final var bsfConfigOauth2 = Observable.just(this.createSampleOauth2CfgBuilder(nfInstanceId, oAuthEnabled)
                                                        .oAuthkeyProfiles(List.of(keyProfile))
                                                        .nrfs(List.of(new Nrf().withNrfInstanceId(nrfInstanceId).withoauth2KeyProfileRef(List.of("1"))))
                                                        .build());

        final var bsfManagementHandlerOauth2 = this.createOauth2Handler(bsfConfigOauth2);

        this.startOauth2Handler(bsfManagementHandlerOauth2);

        final var bindingJson = this.createJsonForSimpleRegister();

        final var result = this.register(bindingJson, String.format(BEARER, ecJwt), ServerType.OAUTH2).blockingGet();

        assertEquals(result.statusCode(), 401, "Wrong status code");
        assertEquals(result.statusMessage(), "Unauthorized", "Wrong status message");

        assertEquals(result.bodyAsJsonObject().getValue("detail"),
                     AuthAccessTokenValidator.ErrorType.TOKEN_INVALID.getProblemDetails(),
                     "Wrong detail message");

        this.stopOauth2Handler(bsfManagementHandlerOauth2);

    }

    @Test(enabled = true)
    private void registerWithHttpOauthNonExistingKidInYangTest() throws JOSEException, IOException, URISyntaxException, InvalidKeySpecException, NoSuchAlgorithmException, InterruptedException
    {

        final var oAuthEnabled = new Pair<Boolean, Boolean>(Boolean.TRUE, null);

        final var alg = JWSAlgorithm.RS256;

        final var nfInstanceId = UUID.randomUUID().toString();
        final var nrfInstanceId = UUID.randomUUID().toString();

        final var rsaJwk = JWTGenerator.generateAsymmetricKeyPair(alg);
        final var rsaPublicKeyPem = JWTGenerator.generatePrivatePublicKeysPemFormatFromJwk(rsaJwk).getValue1();

        final var joseHeader = this.createSampleJoseHeader(alg);

        final var payload = this.createSampleJwsPayloadBuilderWithIssuer(nrfInstanceId).build();

        final var rsaJwt = JWTGenerator.createJwt(joseHeader, payload, rsaJwk, Type.PEM);

        final var keyProfile = new Oauth2KeyProfile().withKeyId("2").withValue(rsaPublicKeyPem).withType(Type.PEM);

        final var bsfConfigOauth2 = Observable.just(this.createSampleOauth2CfgBuilder(nfInstanceId, oAuthEnabled)
                                                        .oAuthkeyProfiles(List.of(keyProfile))
                                                        .nrfs(List.of(new Nrf().withNrfInstanceId(nrfInstanceId).withoauth2KeyProfileRef(List.of("1"))))
                                                        .build());

        final var bsfManagementHandlerOauth2 = this.createOauth2Handler(bsfConfigOauth2);

        this.startOauth2Handler(bsfManagementHandlerOauth2);

        final var bindingJson = this.createJsonForSimpleRegister();

        final var result = this.register(bindingJson, String.format(BEARER, rsaJwt), ServerType.OAUTH2).blockingGet();

        assertEquals(result.statusCode(), 401, "Wrong status code");
        assertEquals(result.statusMessage(), "Unauthorized", "Wrong status message");

        assertEquals(result.bodyAsJsonObject().getValue("detail"),
                     AuthAccessTokenValidator.ErrorType.KEYID_UNKNOWN.getProblemDetails(),
                     "Wrong detail message");

        this.stopOauth2Handler(bsfManagementHandlerOauth2);

    }

    @Test(enabled = true)
    private void registerWithHttpOauthNonExistingJwkInYangTest() throws JOSEException, IOException, URISyntaxException, InvalidKeySpecException, NoSuchAlgorithmException, InterruptedException
    {
        final var oAuthEnabled = new Pair<Boolean, Boolean>(Boolean.TRUE, null);

        final var alg = JWSAlgorithm.EdDSA;

        final var nfInstanceId = UUID.randomUUID().toString();
        final var nrfInstanceId = UUID.randomUUID().toString();

        final var edDsaJwk = JWTGenerator.generateAsymmetricKeyPair(alg);

        final var joseHeader = this.createSampleJoseHeader(alg);

        final var payload = this.createSampleJwsPayloadBuilderWithIssuer(nrfInstanceId).build();

        final var edDsaJwt = JWTGenerator.createJwt(joseHeader, payload, edDsaJwk, Type.JWK);

        final var keyProfile = new Oauth2KeyProfile().withKeyId("1").withJsonBody(null).withType(Type.JWK);

        final var bsfConfigOauth2 = Observable.just(this.createSampleOauth2CfgBuilder(nfInstanceId, oAuthEnabled)
                                                        .oAuthkeyProfiles(List.of(keyProfile))
                                                        .nrfs(List.of(new Nrf().withNrfInstanceId(nrfInstanceId).withoauth2KeyProfileRef(List.of("1"))))
                                                        .build());

        final var bsfManagementHandlerOauth2 = this.createOauth2Handler(bsfConfigOauth2);

        this.startOauth2Handler(bsfManagementHandlerOauth2);

        final var bindingJson = this.createJsonForSimpleRegister();

        final var result = this.register(bindingJson, String.format(BEARER, edDsaJwt), ServerType.OAUTH2).blockingGet();

        assertEquals(result.statusCode(), 401, "Wrong status code");
        assertEquals(result.statusMessage(), "Unauthorized", "Wrong status message");

        assertEquals(result.bodyAsJsonObject().getValue("detail"),
                     AuthAccessTokenValidator.ErrorType.INVALID_SIGNATURE.getProblemDetails(),
                     "Wrong detail message");

        this.stopOauth2Handler(bsfManagementHandlerOauth2);

    }

    @Test(enabled = true)
    private void registerWithHttpOauthNonExistingPemInYangTest() throws JOSEException, IOException, URISyntaxException, InvalidKeySpecException, NoSuchAlgorithmException, InterruptedException
    {
        final var oAuthEnabled = new Pair<Boolean, Boolean>(Boolean.TRUE, null);

        final var alg = JWSAlgorithm.RS256;

        final var nfInstanceId = UUID.randomUUID().toString();
        final var nrfInstanceId = UUID.randomUUID().toString();

        final var rsaJwk = JWTGenerator.generateAsymmetricKeyPair(alg);

        final var joseHeader = this.createSampleJoseHeader(alg);

        final var payload = this.createSampleJwsPayloadBuilderWithIssuer(nrfInstanceId).build();

        final var rsaJwt = JWTGenerator.createJwt(joseHeader, payload, rsaJwk, Type.PEM);

        final var keyProfile = new Oauth2KeyProfile().withKeyId("1").withValue(null).withType(Type.PEM).withAlg(Alg.fromValue(alg.getName()));

        final var bsfConfigOauth2 = Observable.just(this.createSampleOauth2CfgBuilder(nfInstanceId, oAuthEnabled)
                                                        .oAuthkeyProfiles(List.of(keyProfile))
                                                        .nrfs(List.of(new Nrf().withNrfInstanceId(nrfInstanceId).withoauth2KeyProfileRef(List.of("1"))))
                                                        .build());

        final var bsfManagementHandlerOauth2 = this.createOauth2Handler(bsfConfigOauth2);

        this.startOauth2Handler(bsfManagementHandlerOauth2);

        final var bindingJson = this.createJsonForSimpleRegister();

        final var result = this.register(bindingJson, String.format(BEARER, rsaJwt), ServerType.OAUTH2).blockingGet();

        assertEquals(result.statusCode(), 401, "Wrong status code");
        assertEquals(result.statusMessage(), "Unauthorized", "Wrong status message");
        assertEquals(result.bodyAsJsonObject().getValue("detail"),
                     AuthAccessTokenValidator.ErrorType.INVALID_SIGNATURE.getProblemDetails(),
                     "Wrong detail message");

        this.stopOauth2Handler(bsfManagementHandlerOauth2);

    }

    @Test(enabled = true)
    private void registerWithHttpOauthDifferentAlgJwkTest() throws JOSEException, IOException, URISyntaxException, InvalidKeySpecException, NoSuchAlgorithmException, InterruptedException
    {
        final var oAuthEnabled = new Pair<Boolean, Boolean>(Boolean.TRUE, null);

        final var alg = JWSAlgorithm.HS256;

        final var nfInstanceId = UUID.randomUUID().toString();
        final var nrfInstanceId = UUID.randomUUID().toString();

        final var hmacJwk = JWTGenerator.generateSymmetricKey(alg);

        final var joseHeader = this.createSampleJoseHeader(alg);

        final var payload = this.createSampleJwsPayloadBuilderWithIssuer(nrfInstanceId).build();

        final var hmacJwt = JWTGenerator.createJwt(joseHeader, payload, hmacJwk, Type.JWK);

        final var ecKey = ((ECKey) JWTGenerator.generateAsymmetricKeyPair(JWSAlgorithm.ES256)).toPublicJWK().toJSONString();

        final var keyProfile = new Oauth2KeyProfile().withKeyId("1").withJsonBody(ecKey).withType(Type.JWK);

        final var bsfConfigOauth2 = Observable.just(this.createSampleOauth2CfgBuilder(nfInstanceId, oAuthEnabled)
                                                        .oAuthkeyProfiles(List.of(keyProfile))
                                                        .nrfs(List.of(new Nrf().withNrfInstanceId(nrfInstanceId).withoauth2KeyProfileRef(List.of("1"))))
                                                        .build());

        final var bsfManagementHandlerOauth2 = this.createOauth2Handler(bsfConfigOauth2);

        this.startOauth2Handler(bsfManagementHandlerOauth2);

        final var bindingJson = this.createJsonForSimpleRegister();

        final var result = this.register(bindingJson, String.format(BEARER, hmacJwt), ServerType.OAUTH2).blockingGet();

        assertEquals(result.statusCode(), 401, "Wrong status code");
        assertEquals(result.statusMessage(), "Unauthorized", "Wrong status message");

        assertEquals(result.bodyAsJsonObject().getValue("detail"),
                     AuthAccessTokenValidator.ErrorType.INVALID_ALGORITHM.getProblemDetails(),
                     "Wrong detail message");

        this.stopOauth2Handler(bsfManagementHandlerOauth2);

    }

    @Test(enabled = true)
    private void registerWithHttpOauthDifferentAlgPemTest() throws JOSEException, IOException, URISyntaxException, InvalidKeySpecException, NoSuchAlgorithmException, InterruptedException
    {
        final var oAuthEnabled = new Pair<Boolean, Boolean>(Boolean.TRUE, null);

        final var alg = JWSAlgorithm.RS512;

        final var nfInstanceId = UUID.randomUUID().toString();
        final var nrfInstanceId = UUID.randomUUID().toString();

        final var rsaJwk = JWTGenerator.generateAsymmetricKeyPair(alg);

        final var joseHeader = this.createSampleJoseHeader(alg);

        final var payload = this.createSampleJwsPayloadBuilderWithIssuer(nrfInstanceId).build();

        final var rsaJwt = JWTGenerator.createJwt(joseHeader, payload, rsaJwk, Type.PEM);

        final var ecJwk = JWTGenerator.generateAsymmetricKeyPair(JWSAlgorithm.ES256K);
        final var ecKey = JWTGenerator.generatePrivatePublicKeysPemFormatFromJwk(ecJwk).getValue1();

        final var keyProfile = new Oauth2KeyProfile().withKeyId("1").withValue(ecKey).withType(Type.PEM).withAlg(Alg.ES_256_K);

        final var bsfConfigOauth2 = Observable.just(this.createSampleOauth2CfgBuilder(nfInstanceId, oAuthEnabled)
                                                        .oAuthkeyProfiles(List.of(keyProfile))
                                                        .nrfs(List.of(new Nrf().withNrfInstanceId(nrfInstanceId).withoauth2KeyProfileRef(List.of("1"))))
                                                        .build());

        final var bsfManagementHandlerOauth2 = this.createOauth2Handler(bsfConfigOauth2);

        this.startOauth2Handler(bsfManagementHandlerOauth2);

        final var bindingJson = this.createJsonForSimpleRegister();

        final var result = this.register(bindingJson, String.format(BEARER, rsaJwt), ServerType.OAUTH2).blockingGet();

        assertEquals(result.statusCode(), 401, "Wrong status code");
        assertEquals(result.statusMessage(), "Unauthorized", "Wrong status message");

        assertEquals(result.bodyAsJsonObject().getValue("detail"),
                     AuthAccessTokenValidator.ErrorType.INVALID_ALGORITHM.getProblemDetails(),
                     "Wrong detail message");

        this.stopOauth2Handler(bsfManagementHandlerOauth2);

    }

    @Test(enabled = true)
    private void registerWithHttpOauthInvalidSignatureTest() throws JOSEException, IOException, URISyntaxException, InvalidKeySpecException, NoSuchAlgorithmException, InterruptedException
    {
        final var oAuthEnabled = new Pair<Boolean, Boolean>(Boolean.TRUE, null);

        final var alg = JWSAlgorithm.ES384;

        final var nfInstanceId = UUID.randomUUID().toString();
        final var nrfInstanceId = UUID.randomUUID().toString();

        final var ecJwk = JWTGenerator.generateAsymmetricKeyPair(alg);

        final var joseHeader = this.createSampleJoseHeader(alg);

        final var payload = this.createSampleJwsPayloadBuilderWithIssuer(nrfInstanceId).build();

        final var ecJwt = JWTGenerator.createJwt(joseHeader, payload, ecJwk, Type.PEM);

        final var ecDiffKeyJwk = JWTGenerator.generateAsymmetricKeyPair(alg);
        final var ecDiffKeyPem = JWTGenerator.generatePrivatePublicKeysPemFormatFromJwk(ecDiffKeyJwk).getValue1();

        final var keyProfile = new Oauth2KeyProfile().withKeyId("1").withValue(ecDiffKeyPem).withType(Type.PEM).withAlg(Alg.fromValue(alg.getName()));

        final var bsfConfigOauth2 = Observable.just(this.createSampleOauth2CfgBuilder(nfInstanceId, oAuthEnabled)
                                                        .oAuthkeyProfiles(List.of(keyProfile))
                                                        .nrfs(List.of(new Nrf().withNrfInstanceId(nrfInstanceId).withoauth2KeyProfileRef(List.of("1"))))
                                                        .build());

        final var bsfManagementHandlerOauth2 = this.createOauth2Handler(bsfConfigOauth2);

        this.startOauth2Handler(bsfManagementHandlerOauth2);

        final var bindingJson = this.createJsonForSimpleRegister();

        final var result = this.register(bindingJson, String.format(BEARER, ecJwt), ServerType.OAUTH2).blockingGet();

        assertEquals(result.statusCode(), 401, "Wrong status code");
        assertEquals(result.statusMessage(), "Unauthorized", "Wrong status message");

        assertEquals(result.bodyAsJsonObject().getValue("detail"),
                     AuthAccessTokenValidator.ErrorType.INVALID_SIGNATURE.getProblemDetails(),
                     "Wrong detail message");

        this.stopOauth2Handler(bsfManagementHandlerOauth2);

    }

    @Test(enabled = true)
    private void registerWithHttpOauthInvalidNbfTest() throws JOSEException, IOException, URISyntaxException, InvalidKeySpecException, NoSuchAlgorithmException, InterruptedException
    {
        final var oAuthEnabled = new Pair<Boolean, Boolean>(Boolean.TRUE, null);

        final var alg = JWSAlgorithm.HS512;

        final var nfInstanceId = UUID.randomUUID().toString();
        final var nrfInstanceId = UUID.randomUUID().toString();

        final var hmacJwk = JWTGenerator.generateSymmetricKey(alg);

        final var joseHeader = this.createSampleJoseHeader(alg);

        final var payload = this.createSampleJwsPayloadBuilderWithIssuer(nrfInstanceId).withNotValidBefore(OffsetDateTime.now().plusMinutes(30)).build();

        final var hmacJwt = JWTGenerator.createJwt(joseHeader, payload, hmacJwk, Type.JWK);

        final var keyProfile = new Oauth2KeyProfile().withKeyId("1").withJsonBody(hmacJwk.toJSONString()).withType(Type.JWK);

        final var bsfConfigOauth2 = Observable.just(this.createSampleOauth2CfgBuilder(nfInstanceId, oAuthEnabled)
                                                        .oAuthkeyProfiles(List.of(keyProfile))
                                                        .nrfs(List.of(new Nrf().withNrfInstanceId(nrfInstanceId).withoauth2KeyProfileRef(List.of("1"))))
                                                        .build());

        final var bsfManagementHandlerOauth2 = this.createOauth2Handler(bsfConfigOauth2);

        this.startOauth2Handler(bsfManagementHandlerOauth2);

        final var bindingJson = this.createJsonForSimpleRegister();

        final var result = this.register(bindingJson, String.format(BEARER, hmacJwt), ServerType.OAUTH2).blockingGet();

        assertEquals(result.statusCode(), 401, "Wrong status code");
        assertEquals(result.statusMessage(), "Unauthorized", "Wrong status message");

        assertEquals(result.bodyAsJsonObject().getValue("detail"),
                     AuthAccessTokenValidator.ErrorType.TOKEN_NOTBEFORE.getProblemDetails(),
                     "Wrong detail message");

        this.stopOauth2Handler(bsfManagementHandlerOauth2);
    }

    @DataProvider
    public Object[] expirationTimeData()
    {
        return new Object[] { OffsetDateTime.now().minusMinutes(5), null };
    }

    @Test(enabled = true, dataProvider = "expirationTimeData")
    private void registerWithHttpOauthInvalidExpTest(final OffsetDateTime exp) throws JOSEException, IOException, URISyntaxException, InvalidKeySpecException, NoSuchAlgorithmException, InterruptedException
    {
        final var oAuthEnabled = new Pair<Boolean, Boolean>(Boolean.TRUE, null);

        final var alg = JWSAlgorithm.PS512;

        final var nfInstanceId = UUID.randomUUID().toString();
        final var nrfInstanceId = UUID.randomUUID().toString();

        final var rsaSsaJwk = (RSAKey) JWTGenerator.generateAsymmetricKeyPair(alg);

        final var joseHeader = this.createSampleJoseHeader(alg);

        final var payload = this.createSampleJwsPayloadBuilderWithIssuer(nrfInstanceId).withExpirationTime(exp).build();

        final var rsaJwt = JWTGenerator.createJwt(joseHeader, payload, rsaSsaJwk, Type.JWK);

        final var keyProfile = new Oauth2KeyProfile().withKeyId("1").withJsonBody(rsaSsaJwk.toPublicJWK().toJSONString()).withType(Type.JWK);

        final var bsfConfigOauth2 = Observable.just(this.createSampleOauth2CfgBuilder(nfInstanceId, oAuthEnabled)
                                                        .oAuthkeyProfiles(List.of(keyProfile))
                                                        .nrfs(List.of(new Nrf().withNrfInstanceId(nrfInstanceId).withoauth2KeyProfileRef(List.of("1"))))
                                                        .build());

        final var bsfManagementHandlerOauth2 = this.createOauth2Handler(bsfConfigOauth2);

        this.startOauth2Handler(bsfManagementHandlerOauth2);

        final var bindingJson = this.createJsonForSimpleRegister();

        final var result = this.register(bindingJson, String.format(BEARER, rsaJwt), ServerType.OAUTH2).blockingGet();

        assertEquals(result.statusCode(), 401, "Wrong status code");
        assertEquals(result.statusMessage(), "Unauthorized", "Wrong status message");

        assertEquals(result.bodyAsJsonObject().getValue("detail"),
                     exp != null ? AuthAccessTokenValidator.ErrorType.TOKEN_EXPIRED.getProblemDetails()
                                 : AuthAccessTokenValidator.ErrorType.TOKEN_NOEXPR_VALUE.getProblemDetails(),
                     "Wrong detail message");

        this.stopOauth2Handler(bsfManagementHandlerOauth2);
    }

    @Test(enabled = true)
    private void registerWithHttpOauthInvalidIssuerTest() throws JOSEException, IOException, URISyntaxException, InvalidKeySpecException, NoSuchAlgorithmException, InterruptedException
    {
        final var oAuthEnabled = new Pair<Boolean, Boolean>(Boolean.TRUE, null);

        final var alg = JWSAlgorithm.HS384;

        final var nfInstanceId = UUID.randomUUID().toString();

        final var hmacJwk = JWTGenerator.generateSymmetricKey(alg);

        final var joseHeader = this.createSampleJoseHeader(alg);

        final var payload = this.createSampleJwsPayloadBuilderWithIssuer(null).build();

        final var hmacJwt = JWTGenerator.createJwt(joseHeader, payload, hmacJwk, Type.JWK);

        final var keyProfile = new Oauth2KeyProfile().withKeyId("1").withJsonBody(hmacJwk.toJSONString()).withType(Type.JWK);

        final var bsfConfigOauth2 = Observable.just(this.createSampleOauth2CfgBuilder(nfInstanceId, oAuthEnabled)
                                                        .oAuthkeyProfiles(List.of(keyProfile))
                                                        .nrfs(List.of(new Nrf().withoauth2KeyProfileRef(List.of("1"))))
                                                        .build());

        final var bsfManagementHandlerOauth2 = this.createOauth2Handler(bsfConfigOauth2);

        this.startOauth2Handler(bsfManagementHandlerOauth2);

        final var bindingJson = this.createJsonForSimpleRegister();

        final var result = this.register(bindingJson, String.format(BEARER, hmacJwt), ServerType.OAUTH2).blockingGet();

        assertEquals(result.statusCode(), 401, "Wrong status code");
        assertEquals(result.statusMessage(), "Unauthorized", "Wrong status message");

        assertEquals(result.bodyAsJsonObject().getValue("detail"),
                     AuthAccessTokenValidator.ErrorType.ISSUER_MISSING.getProblemDetails(),
                     "Wrong detail message");

        this.stopOauth2Handler(bsfManagementHandlerOauth2);

    }

    @DataProvider
    public Object[] nrfData()
    {
        return new Object[] { List.of(new Nrf().withNrfInstanceId(UUID.randomUUID().toString()).withoauth2KeyProfileRef(List.of("1"))),
                              List.of(new Nrf().withNrfInstanceId(UUID.randomUUID().toString()).withoauth2KeyProfileRef(List.of("2"))),
                              new ArrayList<>() };
    }

    @Test(enabled = true, dataProvider = "nrfData")
    private void registerWithHttpOauthInvalidNrfTest(final List<Nrf> nrfs) throws JOSEException, IOException, URISyntaxException, InvalidKeySpecException, NoSuchAlgorithmException, InterruptedException
    {
        final var oAuthEnabled = new Pair<Boolean, Boolean>(Boolean.TRUE, null);

        final var alg = JWSAlgorithm.ES512;

        final var nfInstanceId = UUID.randomUUID().toString();
        final var nrfInstanceId = UUID.randomUUID().toString();

        final var ecJwk = (ECKey) JWTGenerator.generateAsymmetricKeyPair(alg);

        final var joseHeader = this.createSampleJoseHeader(alg);

        final var payload = this.createSampleJwsPayloadBuilderWithIssuer(nrfInstanceId).build();

        final var ecJwt = JWTGenerator.createJwt(joseHeader, payload, ecJwk, Type.JWK);

        final var keyProfile = new Oauth2KeyProfile().withKeyId("1").withJsonBody(ecJwk.toPublicJWK().toJSONString()).withType(Type.JWK);

        final var bsfConfigOauth2 = Observable.just(this.createSampleOauth2CfgBuilder(nfInstanceId, oAuthEnabled)
                                                        .oAuthkeyProfiles(List.of(keyProfile))
                                                        .nrfs(nrfs)
                                                        .build());

        final var bsfManagementHandlerOauth2 = this.createOauth2Handler(bsfConfigOauth2);

        this.startOauth2Handler(bsfManagementHandlerOauth2);

        final var bindingJson = this.createJsonForSimpleRegister();

        final var result = this.register(bindingJson, String.format(BEARER, ecJwt), ServerType.OAUTH2).blockingGet();

        assertEquals(result.statusCode(), 401, "Wrong status code");
        assertEquals(result.statusMessage(), "Unauthorized", "Wrong status message");

        assertEquals(result.bodyAsJsonObject().getValue("detail"),
                     AuthAccessTokenValidator.ErrorType.ISSUER_UNKNOWN.getProblemDetails(),
                     "Wrong detail message");

        this.stopOauth2Handler(bsfManagementHandlerOauth2);

    }

    @DataProvider
    public Object[] scopeData()
    {
        return new Object[] { "nbsf", null };
    }

    @Test(enabled = true, dataProvider = "scopeData")
    private void registerWithHttpOauthInvalidScopeTest(final String scope) throws JOSEException, IOException, URISyntaxException, InvalidKeySpecException, NoSuchAlgorithmException, InterruptedException
    {
        final var oAuthEnabled = new Pair<Boolean, Boolean>(Boolean.TRUE, null);

        final var alg = JWSAlgorithm.ES512;

        final var nfInstanceId = UUID.randomUUID().toString();
        final var nrfInstanceId = UUID.randomUUID().toString();

        final var ecJwk = JWTGenerator.generateAsymmetricKeyPair(alg);
        final var ecPublicKeyPem = JWTGenerator.generatePrivatePublicKeysPemFormatFromJwk(ecJwk).getValue1();

        final var joseHeader = this.createSampleJoseHeader(alg);

        final var payload = this.createSampleJwsPayloadBuilderWithIssuer(nrfInstanceId).withScope(scope).build();

        final var ecJwt = JWTGenerator.createJwt(joseHeader, payload, ecJwk, Type.PEM);

        final var keyProfile = new Oauth2KeyProfile().withKeyId("1").withValue(ecPublicKeyPem).withType(Type.PEM).withAlg(Alg.fromValue(alg.getName()));

        final var bsfConfigOauth2 = Observable.just(this.createSampleOauth2CfgBuilder(nfInstanceId, oAuthEnabled)
                                                        .oAuthkeyProfiles(List.of(keyProfile))
                                                        .nrfs(List.of(new Nrf().withNrfInstanceId(nrfInstanceId).withoauth2KeyProfileRef(List.of("1"))))
                                                        .build());

        final var bsfManagementHandlerOauth2 = this.createOauth2Handler(bsfConfigOauth2);

        this.startOauth2Handler(bsfManagementHandlerOauth2);

        final var bindingJson = this.createJsonForSimpleRegister();

        final var result = this.register(bindingJson, String.format(BEARER, ecJwt), ServerType.OAUTH2).blockingGet();

        assertEquals(result.statusCode(), scope != null ? 403 : 401, "Wrong status code");
        assertEquals(result.statusMessage(), scope != null ? "Forbidden" : "Unauthorized", "Wrong status message");

        assertEquals(result.bodyAsJsonObject().getValue("detail"),
                     scope != null ? AuthAccessTokenValidator.ErrorType.INSUFFICIENT_SCOPE.getProblemDetails()
                                   : AuthAccessTokenValidator.ErrorType.SCOPE_MISSING.getProblemDetails(),
                     "Wrong detail message");

        this.stopOauth2Handler(bsfManagementHandlerOauth2);

    }

    @DataProvider
    public Object[] audienceData()
    {
        return new Object[] { "BSF2", JsonArray.of("nfInstanceId2").encode(), null };
    }

    @Test(enabled = true, dataProvider = "audienceData")
    private void registerWithHttpOauthInvalidAudienceTest(final String audience) throws URISyntaxException, InvalidKeySpecException, NoSuchAlgorithmException, JOSEException, IOException, InterruptedException
    {
        final var oAuthEnabled = new Pair<Boolean, Boolean>(Boolean.TRUE, null);

        final var alg = JWSAlgorithm.ES512;

        final var nfInstanceId = UUID.randomUUID().toString();
        final var nrfInstanceId = UUID.randomUUID().toString();

        final var ecJwk = JWTGenerator.generateAsymmetricKeyPair(alg);
        final var ecPublicKeyPem = JWTGenerator.generatePrivatePublicKeysPemFormatFromJwk(ecJwk).getValue1();

        final var joseHeader = this.createSampleJoseHeader(alg);

        final var payload = this.createSampleJwsPayloadBuilderWithIssuer(nrfInstanceId).withAudience(audience).build();

        final var ecJwt = JWTGenerator.createJwt(joseHeader, payload, ecJwk, Type.PEM);

        final var keyProfile = new Oauth2KeyProfile().withKeyId("1").withValue(ecPublicKeyPem).withType(Type.PEM).withAlg(Alg.fromValue(alg.getName()));

        final var bsfConfigOauth2 = Observable.just(this.createSampleOauth2CfgBuilder(nfInstanceId, oAuthEnabled)
                                                        .oAuthkeyProfiles(List.of(keyProfile))
                                                        .nrfs(List.of(new Nrf().withNrfInstanceId(nrfInstanceId).withoauth2KeyProfileRef(List.of("1"))))
                                                        .build());

        final var bsfManagementHandlerOauth2 = this.createOauth2Handler(bsfConfigOauth2);

        this.startOauth2Handler(bsfManagementHandlerOauth2);

        final var bindingJson = this.createJsonForSimpleRegister();

        final var result = this.register(bindingJson, String.format(BEARER, ecJwt), ServerType.OAUTH2).blockingGet();

        assertEquals(result.statusCode(), 401, "Wrong status code");
        assertEquals(result.statusMessage(), "Unauthorized", "Wrong status message");
        assertEquals(result.bodyAsJsonObject().getValue("detail"),
                     audience != null ? AuthAccessTokenValidator.ErrorType.INVALID_AUDIENCE.getProblemDetails()
                                      : AuthAccessTokenValidator.ErrorType.AUD_MISSING.getProblemDetails(),
                     "Wrong detail message");

        this.stopOauth2Handler(bsfManagementHandlerOauth2);
    }

    @DataProvider
    public Object[] configuredNfSetIdData()
    {
        return new Object[] { List.of("NFSET2"), new ArrayList<String>() };
    }

    @Test(enabled = true, dataProvider = "configuredNfSetIdData")

    private void registerWithHttpOauthProducerNfSetIdInvalidConfigTest(final List<String> configuredNfSetId) throws URISyntaxException, InvalidKeySpecException, NoSuchAlgorithmException, JOSEException, IOException, InterruptedException
    {
        final var oAuthEnabled = new Pair<Boolean, Boolean>(Boolean.TRUE, null);

        final var alg = JWSAlgorithm.RS512;

        final var nfInstanceId = UUID.randomUUID().toString();
        final var nrfInstanceId = UUID.randomUUID().toString();

        final var rsaJwk = JWTGenerator.generateAsymmetricKeyPair(alg);
        final var rsaPublicKeyPem = JWTGenerator.generatePrivatePublicKeysPemFormatFromJwk(rsaJwk).getValue1();

        final var joseHeader = this.createSampleJoseHeader(alg);

        final var payload = this.createSampleJwsPayloadBuilderWithIssuer(nrfInstanceId)
                                .withAdditionalClaims(new AdditionalClaims.Builder().withProducerNfSetId("NFSET1").build())
                                .build();

        final var rsaJwt = JWTGenerator.createJwt(joseHeader, payload, rsaJwk, Type.PEM);

        final var keyProfile = new Oauth2KeyProfile().withKeyId("1").withValue(rsaPublicKeyPem).withType(Type.PEM).withAlg(Alg.fromValue(alg.getName()));

        final var bsfConfigOauth2 = Observable.just(this.createSampleOauth2CfgBuilder(nfInstanceId, oAuthEnabled)
                                                        .nfProfiles(List.of(new NfProfile().withNfSetId(configuredNfSetId)))
                                                        .oAuthkeyProfiles(List.of(keyProfile))
                                                        .nrfs(List.of(new Nrf().withNrfInstanceId(nrfInstanceId).withoauth2KeyProfileRef(List.of("1"))))
                                                        .build());

        final var bsfManagementHandlerOauth2 = this.createOauth2Handler(bsfConfigOauth2);

        this.startOauth2Handler(bsfManagementHandlerOauth2);

        final var bindingJson = this.createJsonForSimpleRegister();

        final var result = this.register(bindingJson, String.format(BEARER, rsaJwt), ServerType.OAUTH2).blockingGet();

        assertEquals(result.statusCode(), 401, "Wrong status code");
        assertEquals(result.statusMessage(), "Unauthorized", "Wrong status message");

        assertEquals(result.bodyAsJsonObject().getValue("detail"),
                     AuthAccessTokenValidator.ErrorType.INVALID_NFSETID.getProblemDetails(),
                     "Wrong detail message");

        this.stopOauth2Handler(bsfManagementHandlerOauth2);

    }

    @DataProvider
    public Object[] configuredNsiData()
    {
        return new Object[] { List.of("NSI2") };
    }

    @Test(enabled = true, dataProvider = "configuredNsiData")

    private void registerWithHttpOauthProducerNsiInvalidConfigTest(final List<String> configuredNsi) throws URISyntaxException, InvalidKeySpecException, NoSuchAlgorithmException, JOSEException, IOException, InterruptedException
    {
        final var oAuthEnabled = new Pair<Boolean, Boolean>(Boolean.TRUE, null);

        final var alg = JWSAlgorithm.RS384;

        final var nfInstanceId = UUID.randomUUID().toString();
        final var nrfInstanceId = UUID.randomUUID().toString();

        final var rsaJwk = JWTGenerator.generateAsymmetricKeyPair(alg);
        final var rsaPublicKeyPem = JWTGenerator.generatePrivatePublicKeysPemFormatFromJwk(rsaJwk).getValue1();

        final var joseHeader = this.createSampleJoseHeader(alg);

        final var payload = this.createSampleJwsPayloadBuilderWithIssuer(nrfInstanceId)
                                .withAdditionalClaims(new AdditionalClaims.Builder().withProducerNfSetId("NFSET1").withProducerNsiList(List.of("NSI1")).build())
                                .build();

        final var rsaJwt = JWTGenerator.createJwt(joseHeader, payload, rsaJwk, Type.PEM);

        final var keyProfile = new Oauth2KeyProfile().withKeyId("1").withValue(rsaPublicKeyPem).withType(Type.PEM).withAlg(Alg.fromValue(alg.getName()));

        final var bsfConfigOauth2 = Observable.just(this.createSampleOauth2CfgBuilder(nfInstanceId, oAuthEnabled)
                                                        .nfProfiles(List.of(new NfProfile().withNfSetId(List.of("NFSET1")).withNsi(configuredNsi)))
                                                        .oAuthkeyProfiles(List.of(keyProfile))
                                                        .nrfs(List.of(new Nrf().withNrfInstanceId(nrfInstanceId).withoauth2KeyProfileRef(List.of("1"))))
                                                        .build());

        final var bsfManagementHandlerOauth2 = this.createOauth2Handler(bsfConfigOauth2);

        this.startOauth2Handler(bsfManagementHandlerOauth2);

        final var bindingJson = this.createJsonForSimpleRegister();

        final var result = this.register(bindingJson, String.format(BEARER, rsaJwt), ServerType.OAUTH2).blockingGet();

        assertEquals(result.statusCode(), 401, "Wrong status code");
        assertEquals(result.statusMessage(), "Unauthorized", "Wrong status message");
        assertEquals(result.bodyAsJsonObject().getValue("detail"),
                     AuthAccessTokenValidator.ErrorType.INVALID_NSILIST.getProblemDetails(),
                     "Wrong detail message");

        this.stopOauth2Handler(bsfManagementHandlerOauth2);

    }

    @DataProvider
    public Object[] configuredSnssaiData()
    {
        return new Object[] { List.of(new Snssai1().withSd("ABDC1").withSst(1)) };
    }

    @Test(enabled = true, dataProvider = "configuredSnssaiData")

    private void registerWithHttpOauthProducerSnssaiInvalidConfigTest(final List<Snssai1> configuredSnssai) throws URISyntaxException, InvalidKeySpecException, NoSuchAlgorithmException, JOSEException, IOException, InterruptedException
    {
        final var oAuthEnabled = new Pair<Boolean, Boolean>(Boolean.TRUE, null);

        final var alg = JWSAlgorithm.HS384;

        final var nfInstanceId = UUID.randomUUID().toString();
        final var nrfInstanceId = UUID.randomUUID().toString();

        final var hmacJwk = JWTGenerator.generateSymmetricKey(alg);

        final var joseHeader = this.createSampleJoseHeader(alg);

        final var payload = this.createSampleJwsPayloadBuilderWithIssuer(nrfInstanceId)
                                .withAdditionalClaims(new AdditionalClaims.Builder().withProducerNfSetId("NFSET1")
                                                                                    .withProducerNsiList(List.of("NSI1"))
                                                                                    .withProducerSnssaiList(List.of(new Snssai().sd("ABDD1").sst(2)))
                                                                                    .build())
                                .build();

        final var hmacJwt = JWTGenerator.createJwt(joseHeader, payload, hmacJwk, Type.JWK);

        final var keyProfile = new Oauth2KeyProfile().withKeyId("1").withJsonBody(hmacJwk.toJSONString()).withType(Type.JWK);

        final var bsfConfigOauth2 = Observable.just(this.createSampleOauth2CfgBuilder(nfInstanceId, oAuthEnabled)
                                                        .nfProfiles(List.of(new NfProfile().withNfSetId(List.of("NFSET1"))
                                                                                           .withNsi(List.of("NSI1"))
                                                                                           .withSnssai1(configuredSnssai)))
                                                        .oAuthkeyProfiles(List.of(keyProfile))
                                                        .nrfs(List.of(new Nrf().withNrfInstanceId(nrfInstanceId).withoauth2KeyProfileRef(List.of("1"))))
                                                        .build());

        final var bsfManagementHandlerOauth2 = this.createOauth2Handler(bsfConfigOauth2);

        this.startOauth2Handler(bsfManagementHandlerOauth2);

        final var bindingJson = this.createJsonForSimpleRegister();

        final var result = this.register(bindingJson, String.format(BEARER, hmacJwt), ServerType.OAUTH2).blockingGet();

        assertEquals(result.statusCode(), 401, "Wrong status code");
        assertEquals(result.statusMessage(), "Unauthorized", "Wrong status message");
        assertEquals(result.bodyAsJsonObject().getValue("detail"),
                     AuthAccessTokenValidator.ErrorType.INVALID_SNSSAILIST.getProblemDetails(),
                     "Wrong detail message");

        this.stopOauth2Handler(bsfManagementHandlerOauth2);

    }

    @DataProvider
    public Object[] configuredProducerPlmnData()
    {
        return new Object[] { List.of(new Plmn().withMcc("200").withMnc("201")) };
    }

    @Test(enabled = true, dataProvider = "configuredProducerPlmnData")

    private void registerWithHttpOauthProducerPlmnInvalidConfigTest(final List<Plmn> producerPlmnList) throws URISyntaxException, InvalidKeySpecException, NoSuchAlgorithmException, JOSEException, IOException, InterruptedException
    {
        final var oAuthEnabled = new Pair<Boolean, Boolean>(Boolean.TRUE, null);

        final var alg = JWSAlgorithm.EdDSA;

        final var nfInstanceId = UUID.randomUUID().toString();
        final var nrfInstanceId = UUID.randomUUID().toString();

        final var edDsaJwk = (OctetKeyPair) JWTGenerator.generateAsymmetricKeyPair(alg);

        final var joseHeader = this.createSampleJoseHeader(alg);

        final var payload = this.createSampleJwsPayloadBuilderWithIssuer(nrfInstanceId)
                                .withAdditionalClaims(new AdditionalClaims.Builder().withProducerNfSetId("NFSET1")
                                                                                    .withProducerNsiList(List.of("NSI1"))
                                                                                    .withProducerSnssaiList(List.of(new Snssai().sd("ABDD1").sst(2)))
                                                                                    .withProducerPlmnId(new PlmnId().mcc("100").mnc("101"))
                                                                                    .build())
                                .build();

        final var edDsaJwt = JWTGenerator.createJwt(joseHeader, payload, edDsaJwk, Type.JWK);

        final var keyProfile = new Oauth2KeyProfile().withKeyId("1").withJsonBody(edDsaJwk.toPublicJWK().toJSONString()).withType(Type.JWK);

        final var bsfConfigOauth2 = Observable.just(this.createSampleOauth2CfgBuilder(nfInstanceId, oAuthEnabled)
                                                        .nfProfiles(List.of(new NfProfile().withNfSetId(List.of("NFSET1"))
                                                                                           .withNsi(List.of("NSI1"))
                                                                                           .withSnssai1(List.of(new Snssai1().withSd("ABDD1").withSst(2)))
                                                                                           .withPlmn(producerPlmnList)))
                                                        .oAuthkeyProfiles(List.of(keyProfile))
                                                        .nrfs(List.of(new Nrf().withNrfInstanceId(nrfInstanceId).withoauth2KeyProfileRef(List.of("1"))))
                                                        .build());

        final var bsfManagementHandlerOauth2 = this.createOauth2Handler(bsfConfigOauth2);

        this.startOauth2Handler(bsfManagementHandlerOauth2);

        final var bindingJson = this.createJsonForSimpleRegister();

        final var result = this.register(bindingJson, String.format(BEARER, edDsaJwt), ServerType.OAUTH2).blockingGet();

        assertEquals(result.statusCode(), 401, "Wrong status code");
        assertEquals(result.statusMessage(), "Unauthorized", "Wrong status message");
        assertEquals(result.bodyAsJsonObject().getValue("detail"),
                     AuthAccessTokenValidator.ErrorType.INVALID_PRODUCER_PLMNID.getProblemDetails(),
                     "Wrong detail message");

        this.stopOauth2Handler(bsfManagementHandlerOauth2);

    }

    @DataProvider
    public Object[] configuredConsumerPlmnData()
    {
        return new Object[] { List.of(new AllowedPlmn().withMcc("300").withMnc("301")) };
    }

    @Test(enabled = true, dataProvider = "configuredConsumerPlmnData")

    private void registerWithHttpOauthConsumerPlmnInvalidConfigTest(final List<AllowedPlmn> consumerPlmnList) throws URISyntaxException, InvalidKeySpecException, NoSuchAlgorithmException, JOSEException, IOException, InterruptedException
    {
        final var oAuthEnabled = new Pair<Boolean, Boolean>(Boolean.TRUE, null);

        final var alg = JWSAlgorithm.HS256;

        final var nfInstanceId = UUID.randomUUID().toString();
        final var nrfInstanceId = UUID.randomUUID().toString();

        final var hmacJwk = JWTGenerator.generateSymmetricKey(alg);

        final var joseHeader = this.createSampleJoseHeader(alg);

        final var payload = this.createSampleJwsPayloadBuilderWithIssuer(nrfInstanceId)
                                .withAdditionalClaims(new AdditionalClaims.Builder().withProducerNfSetId("NFSET1")
                                                                                    .withProducerNsiList(List.of("NSI1"))
                                                                                    .withProducerSnssaiList(List.of(new Snssai().sd("ABDD1").sst(2)))
                                                                                    .withProducerPlmnId(new PlmnId().mcc("100").mnc("101"))
                                                                                    .withConsumerPlmnId(new PlmnId().mcc("200").mnc("201"))
                                                                                    .build())
                                .build();

        final var hmacJwt = JWTGenerator.createJwt(joseHeader, payload, hmacJwk, Type.JWK);

        final var keyProfile = new Oauth2KeyProfile().withKeyId("1").withJsonBody(hmacJwk.toJSONString()).withType(Type.JWK);

        final var bsfConfigOauth2 = Observable.just(this.createSampleOauth2CfgBuilder(nfInstanceId, oAuthEnabled)
                                                        .nfProfiles(List.of(new NfProfile().withNfSetId(List.of("NFSET1"))
                                                                                           .withNsi(List.of("NSI1"))
                                                                                           .withSnssai1(List.of(new Snssai1().withSd("ABDD1").withSst(2)))
                                                                                           .withPlmn(List.of(new Plmn().withMcc("100").withMnc("101")))
                                                                                           .withAllowedPlmn(consumerPlmnList)))
                                                        .oAuthkeyProfiles(List.of(keyProfile))
                                                        .nrfs(List.of(new Nrf().withNrfInstanceId(nrfInstanceId).withoauth2KeyProfileRef(List.of("1"))))
                                                        .build());

        final var bsfManagementHandlerOauth2 = this.createOauth2Handler(bsfConfigOauth2);

        this.startOauth2Handler(bsfManagementHandlerOauth2);

        final var bindingJson = this.createJsonForSimpleRegister();

        final var result = this.register(bindingJson, String.format(BEARER, hmacJwt), ServerType.OAUTH2).blockingGet();

        assertEquals(result.statusCode(), 401, "Wrong status code");
        assertEquals(result.statusMessage(), "Unauthorized", "Wrong status message");
        assertEquals(result.bodyAsJsonObject().getValue("detail"),
                     AuthAccessTokenValidator.ErrorType.INVALID_CONSUMER_PLMNID.getProblemDetails(),
                     "Wrong detail message");

        this.stopOauth2Handler(bsfManagementHandlerOauth2);

    }

    private JsonObject createJsonForSimpleRegister()
    {
        final var pcfIpEndPointsJson = new ArrayList<>();
        pcfIpEndPointsJson.add(new JsonObject().put("ipv4Address", "10.0.0.1").put("transport", "TCP").put("port", 1024));

        final var snssaiJson = new JsonObject().put("sst", 2).put("sd", "DEADF0");

        return new JsonObject().put("supi", "imsi-12345")
                               .put("gpsi", "msisdn-306972909290")
                               .put("ipv4Addr", "10.0.0.1")
                               .put("dnn", "testDnn")
                               .put("pcfIpEndPoints", pcfIpEndPointsJson)
                               .put("pcfDiamHost", "pcf.ericsson.com")
                               .put("pcfDiamRealm", "ericsson.com")
                               .put("snssai", snssaiJson)
                               .put("blablakey", "blablavalue");
    }

    private JOSEHeader createSampleJoseHeader(final JWSAlgorithm alg)
    {
        return new JOSEHeader.Builder().withAlgorithm(alg).withKeyId("1").build();
    }

    private JWSPayload.Builder createSampleJwsPayloadBuilderWithIssuer(final String nrfInstanceId)
    {
        return new JWSPayload.Builder().withAudience("BSF")
                                       .withIssuedAt(OffsetDateTime.now())
                                       .withExpirationTime(OffsetDateTime.now().plusMinutes(30))
                                       .withNotValidBefore(OffsetDateTime.now().minusMinutes(30))
                                       .withIssuer(nrfInstanceId)
                                       .withScope("nbsf-management")
                                       .withJwtId("jwt-uuid");
    }

    private NBsfManagementHandler createOauth2Handler(final Observable<BsfCmConfig> bsfConfigOauth2)
    {
        return new NBsfManagementHandler(this.webServerOAuth2,
                                         this.nbsfManagementService,
                                         new BindingCleanupManager(this.rxSession, this.keyspace, Flowable.just("sample")),
                                         bsfConfigOauth2,
                                         false,
                                         tokenCache);
    }

    private BsfCmConfig.Builder createSampleOauth2CfgBuilder(final String nfInstanceId,
                                                             final Pair<Boolean, Boolean> oAuthEnabled)
    {
        return new BsfCmConfig.Builder().bindingTimeout(bindingTimeoutDefault)
                                        .nfInstanceName(nfInstanceName)
                                        .nfInstanceId(nfInstanceId)
                                        .outMessageHandling(false)
                                        .oAuth(oAuthEnabled);
    }

    private void startOauth2Handler(final NBsfManagementHandler handler) throws InterruptedException
    {
        handler.start().blockingAwait();
        TimeUnit.MILLISECONDS.sleep(OAUTH_HANDLER_SLEEP);
    }

    private void stopOauth2Handler(final NBsfManagementHandler handler) throws InterruptedException
    {
        handler.stop().blockingAwait();
        TimeUnit.MILLISECONDS.sleep(OAUTH_HANDLER_SLEEP);
    }

    private static String hugeNum(int count)
    {
        StringBuilder b = new StringBuilder();
        b.append("{\"\": ");
        b.append("1".repeat(count));
        b.append(",\"\":false}");
        return b.toString();
    }

    private final Single<HttpResponse<io.vertx.reactivex.core.buffer.Buffer>> register(final JsonObject bindingJson)
    {
        return this.register(bindingJson, null, ServerType.WEBSERVER);
    }

    private final Single<HttpResponse<io.vertx.reactivex.core.buffer.Buffer>> register(final JsonObject bindingJson,
                                                                                       final String headers)
    {
        return this.register(bindingJson, headers, ServerType.NFPEERINFO);
    }

    private final Single<HttpResponse<io.vertx.reactivex.core.buffer.Buffer>> register(final JsonObject bindingJson,
                                                                                       final String headers,
                                                                                       final ServerType serverType)
    {
        final var srvInfo = inferSocketAndHeaderForServer(serverType, headers);

        return client.post(srvInfo.getValue1(), srvInfo.getValue0(), URI)
                     .putHeader(srvInfo.getValue2().getValue0(), srvInfo.getValue2().getValue1())
                     .rxSendJsonObject(bindingJson)
                     .doOnSuccess(resp -> log.info("register result: {}", resp.getHeader("Location")))
                     .doOnError(ar -> log.error("Something went wrong: " + ar.getMessage()))
                     .doOnSuccess(ar -> log.debug("Result code: " + ar.statusCode() + " Result Message: " + ar.statusMessage() + " Error Message: "
                                                  + ar.bodyAsString()));
    }

    private final Single<HttpResponse<io.vertx.reactivex.core.buffer.Buffer>> deregister(final String id,
                                                                                         final String headers)
    {
        return this.deregister(id, headers, ServerType.NFPEERINFO);
    }

    private final Single<HttpResponse<io.vertx.reactivex.core.buffer.Buffer>> deregister(final String id,
                                                                                         final String headers,
                                                                                         final ServerType serverType)
    {
        final var srvInfo = inferSocketAndHeaderForServer(serverType, headers);

        return client.delete(srvInfo.getValue1(), srvInfo.getValue0(), URI + id)
                     .putHeader(srvInfo.getValue2().getValue0(), srvInfo.getValue2().getValue1())
                     .rxSend()
                     .doOnSuccess(resp -> log.info("deregister result: {}", resp.getHeader("Location")))
                     .doOnError(ar -> log.error("Something went wrong: " + ar.getMessage()))
                     .doOnSuccess(ar -> log.debug("Result code: " + ar.statusCode() + " Result Message: " + ar.statusMessage() + " Error Message: "
                                                  + ar.bodyAsString()));
    }

    private final Single<HttpResponse<io.vertx.reactivex.core.buffer.Buffer>> discover(final HashMap<String, String> query)
    {
        return this.discover(query, null, ServerType.WEBSERVER);
    }

    private final Single<HttpResponse<io.vertx.reactivex.core.buffer.Buffer>> discover(final HashMap<String, String> query,
                                                                                       final String headers)
    {
        return this.discover(query, headers, ServerType.NFPEERINFO);
    }

    private final Single<HttpResponse<io.vertx.reactivex.core.buffer.Buffer>> discover(final HashMap<String, String> query,
                                                                                       final String headers,
                                                                                       final ServerType serverType)
    {
        final var uri = URI.concat("?");
        final var mapAsString = query.keySet().stream().map(key -> key + "=" + query.get(key)).collect(Collectors.joining("&"));
        final var requestURI = uri + mapAsString;

        final var srvInfo = inferSocketAndHeaderForServer(serverType, headers);

        return client.get(srvInfo.getValue1(), srvInfo.getValue0(), requestURI)
                     .putHeader(srvInfo.getValue2().getValue0(), (srvInfo.getValue2().getValue1()))
                     .rxSend()
                     .doOnError(ar -> log.error("Something went wrong: " + ar.getMessage()))
                     .doOnSuccess(ar -> log.info("Result code: " + ar.statusCode() + " Result Message: " + ar.statusMessage() + " Error Message: "
                                                 + ar.bodyAsString()));
    }

    private final Triplet<String, Integer, Pair<String, String>> inferSocketAndHeaderForServer(final ServerType serverType,
                                                                                               final String headers)
    {
        return switch (serverType)
        {
            case WEBSERVER -> Triplet.with(LOCAL_HOST, this.webServer.actualPort(), new Pair<String, String>(null, null));
            case NFPEERINFO -> Triplet.with(LOCAL_HOST_PEER_INFO, this.webServerPeerInfo.actualPort(), new Pair<>(PEER_INFO_HEADER, headers));
            case OAUTH2 -> Triplet.with(LOCAL_HOST_OAUTH2, this.webServerOAuth2.actualPort(), new Pair<>(OAUTH2_HEADER, headers));
        };
    }

    @AfterClass
    private void stopServer()
    {
        log.info("Stopping servers");
        webServer.stopListener().blockingGet();
        webServerPeerInfo.stopListener().blockingGet();
        webServerOAuth2.stopListener().blockingGet();
        vertx.close();
    }
}