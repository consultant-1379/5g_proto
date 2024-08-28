package com.ericsson.adpal.cm.state;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ericsson.adpal.cm.state.StateDataInput;
import com.ericsson.adpal.cm.state.StateDataProvider;
import com.ericsson.utilities.http.WebClientProvider;
import com.ericsson.utilities.http.WebServer;
import com.ericsson.utilities.reactivex.VertxInstance;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.net.SocketAddress;
import static org.testng.Assert.assertTrue;

public class StateDataProviderTest
{

    private static final Logger log = LoggerFactory.getLogger(StateDataProviderTest.class);

    @Test
    public void testSecurityNegotiationDataRetrieval()
    {

        String localAddress;
        try
        {
            localAddress = InetAddress.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException e)
        {
            log.error("State data provider test failed with UnknownHostException ", e);
            localAddress = "0.0.0.0";
        }
        int port = 8082;
        String clientHostName = "localClient";
        String requestURI_n32 = "http://" + localAddress + ":" + port
                                + "/ericsson-sepp:sepp-function/nf-instance/name=instance_1/external-network/name=externalNetwork/roaming-partner/name=RP_A/n32-c/security-negotiation-data?configETag=36gss63/";
        String requestURI_non_N32 = "http://" + localAddress + ":" + port
                                    + "/ericsson-sepp:sepp-function/nf-instance/name=instance_1/external-network/name=externalNetwork/roaming-partner/name=RP_A/";

        WebServer server = WebServer.builder() // Non-TLS web server
                                    .withHost(localAddress)
                                    .withPort(port)
                                    .build(VertxInstance.get());

        StateDataInput input1 = new StateDataInput(new MockHandler(), RoutingParameter.n32_c);
        StateDataInput input2 = new StateDataInput(new MockHandler(), RoutingParameter.external_network);
        StateDataProvider sdp = new StateDataProvider(List.of(input1, input2));
        sdp.configureStateDataHandler(server);
        server.configureRouter(router -> router.getRoutes().forEach(r -> log.info("route122:  " + r)));
        server.startListener().subscribe();
        WebClientProvider webClientProvider = WebClientProvider.builder().withHostName(clientHostName).build(VertxInstance.get());
        for (int i = 0; i < 1; i++)
        {
            var response = webClientProvider.getWebClient()
                                            .blockingGet()
                                            .requestAbs(HttpMethod.GET, SocketAddress.inetSocketAddress(port, localAddress), requestURI_n32)
                                            .rxSend();
            log.debug("result:  " + response.blockingGet().bodyAsString());
            assertTrue(MockHandler.constructMockData().equals(new JsonObject(response.blockingGet().bodyAsString())),
                       "Test Security Negotiation Data Retrieval failed ");
        }

        for (int i = 0; i < 1; i++)
        {
            var response = webClientProvider.getWebClient()
                                            .blockingGet()
                                            .requestAbs(HttpMethod.GET, SocketAddress.inetSocketAddress(port, localAddress), requestURI_non_N32)
                                            .rxSend();
            log.debug("result:  " + response.blockingGet().bodyAsString());
            assertTrue(MockHandler.constructMockData().equals(new JsonObject(response.blockingGet().bodyAsString())),
                       "Test Security Negotiation Data Retrieval failed ");
        }
        server.stopListener().subscribe();
    }

}
