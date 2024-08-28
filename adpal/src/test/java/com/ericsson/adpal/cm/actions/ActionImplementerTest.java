package com.ericsson.adpal.cm.actions;

import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ericsson.utilities.http.HelperHttp;
import com.ericsson.utilities.http.WebClientProvider;
import com.ericsson.utilities.http.WebServer;
import com.ericsson.utilities.json.Jackson;
import com.ericsson.utilities.reactivex.VertxBuilder;

import io.reactivex.Single;
import io.vertx.reactivex.core.Vertx;

public class ActionImplementerTest
{

    private static final Logger log = LoggerFactory.getLogger(ActionImplementerTest.class);
    private static final String SCHEMA_NAME = "ericsson-bsf";
    private static final String LOCAL_HOST = "127.0.0.70";
    private static final String ACTION_ID = "container1::container2:conteiner3";

    private final Vertx vertx = VertxBuilder.newInstance().build();

    @Test
    public void testActionImplementerRegistration()
    {
        final var actionHandler = new ActionHandler()
        {

            @Override
            public Single<ActionResult> executeAction(Single<ActionInput> actionContext)
            {
                return actionContext.doOnSubscribe((disp) -> log.info("Action1")) //
                                    .map(input ->
                                    {
                                        log.info("echfari Action1 {}", input);
                                        return ActionResult.success(Jackson.om()
                                                                           .createObjectNode()
                                                                           .put("ericsson-bsf:info", "BSF database initialized. Input was: " + input));
                                    });
            }
        };

        final var wc = WebClientProvider.builder().build(vertx);

        final var portA = HelperHttp.getAvailablePort(LOCAL_HOST);
        final var portB = HelperHttp.getAvailablePort(LOCAL_HOST);
        final var actionSpec = new ActionSpec(actionHandler, ACTION_ID);
        final var implementerWebServer = WebServer.builder().withHost(LOCAL_HOST).withPort(portA).build(vertx);
        final var implementer = new ActionImplementer(SCHEMA_NAME,
                                                      implementerWebServer,
                                                      URI.create("http://" + LOCAL_HOST + ":" + portA + "/"),
                                                      List.of(actionSpec),
                                                      LOCAL_HOST,
                                                      portB,
                                                      wc);

        final var cmypWebServer = WebServer.builder().withHost(LOCAL_HOST).withPort(portB).build(vertx);
        cmypWebServer.getRouter().put(ActionImplementer.CMM_API_BASE + "/" + SCHEMA_NAME + "/actions").blockingHandler(ctx -> ctx.response().end(), true);
        cmypWebServer.startListener().blockingAwait();

        implementer.registerActionImplementer().blockingAwait();
        cmypWebServer.stopListener();
        wc.close();
    }
}
