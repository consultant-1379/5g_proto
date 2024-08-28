package com.ericsson.adpal.cm.state;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.cm.state.StateDataInput;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class MockHandler implements StateDataHandler
{
    private static final Logger log = LoggerFactory.getLogger(MockHandler.class);

    private ConcurrentHashMap<String, String> pathParameters = new ConcurrentHashMap<String, String>();
    private String secondStageVerdict;

    @Override
    public Completable handleRequest(Single<StateDataInput> input)
    {
        return input.flatMapCompletable(in ->
        {
            return extractPathParameters(in.getCtx().normalizedPath()).concatWith(secondStageRouter(in.getRoutingParameter()))
                                                                      .concatWith(Completable.create(emitter ->
                                                                      {
                                                                          if (secondStageVerdict == null)
                                                                          {
                                                                              pathParameters.values()
                                                                                            .forEach(e -> log.info("Path parameters handleRequest: {}", e));
                                                                              in.getCtx().next();
                                                                              emitter.onError(new Exception("No suitable route found"));
                                                                          }
                                                                          else
                                                                          {
                                                                              log.info("Response sent");
                                                                              in.getCtx()
                                                                                .response()
                                                                                .putHeader("content-type", "application/json")
                                                                                .rxEnd(constructMockData().toString())
                                                                                .cache()
                                                                                .subscribe();
                                                                              emitter.onComplete();
                                                                          }
                                                                      }));

        });
    }

    public String handlerPath()
    {
        return "/ericsson-sepp:sepp-function/nf-instance/";
    }

    public Completable secondStageRouter(RoutingParameter routeParam)
    {
        return Completable.create(emitter ->
        {
            if (pathParameters.isEmpty())
            {
                log.info("pathparameters is empty");
                emitter.onError(new Exception("No parameters found"));

            }
            else
            {
                pathParameters.values().forEach(e -> log.info("Path parameters secondStage: {}", e));
            }
            if (pathParameters.containsValue(routeParam.toString()))
            {
                log.info("Path parameters match");
                secondStageVerdict = routeParam.toString();
            }
            else
            {
                log.info("Path parameters not match");
                secondStageVerdict = null;
            }
            emitter.onComplete();
        });
    }

    private Completable extractPathParameters(String path)
    {
        return Completable.fromAction(() ->
        {
            List<String> pathParams = Arrays.asList(path.split("/"));
            for (int count = 1; count < pathParams.size(); count++)
            {
                pathParameters.put("param" + String.valueOf(count + 1), pathParams.get(count));

            }
            // pathParameters.forEach((k,v) -> log.info(k + ": " + v));
        });
    }

    public static JsonObject constructMockData()
    {
        var json = new JsonObject();
        var snd = new JsonObject();
        var plmns = new JsonObject();
        var array = new JsonArray();
        var plmn = new JsonArray();

        plmns.put("mcc", 123);
        plmns.put("mnc", 123);
        plmn.add(plmns);

        snd.put("static-nf-instance-ref", "example");
        snd.put("last-update", "example");
        snd.put("operation-state", new JsonObject().put("value", "active"));
        snd.put("security-capability", "TLS");
        snd.put("supports-target-apiroot", false);
        snd.put("received-plm-id", plmn);

        array.add(snd);

        return json.put("security-negotiation-data", array);
    }

    public enum MockRoutes
    {
        n32("n32-c"),
        external_network("external-network");

        private final String t;

        MockRoutes(final String mockRoute)
        {
            t = mockRoute;
        }

        public String toString()
        {
            return t;
        }
    }

    @Override
    public String handlerName()
    {
        return "Mock Handler";
    }
}
