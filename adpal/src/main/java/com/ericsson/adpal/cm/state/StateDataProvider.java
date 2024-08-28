package com.ericsson.adpal.cm.state;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.utilities.http.RouterHandler;

import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;
import io.vertx.core.http.HttpMethod;
import io.vertx.reactivex.ext.web.Route;
import io.vertx.reactivex.ext.web.Router;

public class StateDataProvider
{

    private static final Logger log = LoggerFactory.getLogger(StateDataProvider.class);

    private List<StateDataInput> inputParameters;

    public StateDataProvider(List<StateDataInput> inputParameters)
    {
        super();
        this.inputParameters = inputParameters;

    }

    public void configureStateDataHandler(RouterHandler server)
    {
        server.configureRouter(router -> addStateDataProviderRoutes(router).doOnError(e -> log.error("Error adding routes on State Data provider router ", e))
                                                                           .doOnComplete(() -> log.info("Routes added to State Data Provider"))
                                                                           .subscribeOn(Schedulers.io())
                                                                           .subscribe());

    }

    private Completable addStateDataProviderRoutes(Router router)
    {

        return Completable.fromAction(() -> router.getRoutes().addAll(createStateDataRoutes(router)));
    }

    private List<Route> createStateDataRoutes(Router router)
    {
        List<Route> routeList = new ArrayList<>();
        inputParameters.forEach(p -> routeList.add(router.route()
                                                         .method(HttpMethod.GET)
                                                         .pathRegex(p.getStateDataHandler().handlerPath() + p.getRoutingParameter().stringForRegex())
                                                         .handler(ctx ->
                                                         {
                                                             log.debug("The path that was matched through regex {}", ctx.normalizedPath());

                                                             p.getStateDataHandler()
                                                              .handleRequest(p.withCtx(ctx).getInput())
                                                              .doOnError(err -> log.error("Error while creating response for handler: {} with exception: {}",
                                                                                          p.getStateDataHandler().handlerName(),
                                                                                          err))
                                                              .doOnComplete(() -> log.info("Response for state data succeded"))
                                                              .subscribeOn(Schedulers.io())
                                                              .subscribe();

                                                         })));
        return routeList;
    }

}
