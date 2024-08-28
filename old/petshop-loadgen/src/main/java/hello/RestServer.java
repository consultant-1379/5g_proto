package hello;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.RequestParameter;
import io.vertx.ext.web.api.RequestParameters;
import io.vertx.ext.web.api.contract.RouterFactoryOptions;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;



public class RestServer {

    private PublishSubject< RoutingContext> emitNewPet = PublishSubject.create();
    private PublishSubject< RoutingContext> emitGetPet = PublishSubject.create();


    public void start(Vertx vertx) {

        //OpenAPISpec from:
        //https://raw.githubusercontent.com/OAI/OpenAPI-Specification/master/examples/v3.0/petstore-expanded.yaml
        OpenAPI3RouterFactory.create(vertx, "src/main/resources/petstore-expanded.yaml", ar -> {
            if (ar.succeeded()) {
                // Spec loaded with success
                OpenAPI3RouterFactory routerFactory = ar.result();

                routerFactory.setOptions( new RouterFactoryOptions()
                        .setMountNotImplementedHandler(true)
                        .setMountValidationFailureHandler(true)
                );

                // The delete or GET /pets method from the spec is not implemented!!!
                // The default notImplementedHandler kicks-in and sends a 501


                routerFactory.addHandler(HttpMethod.GET, "/pets/{id}", routingContext -> {
                    //emit GetPet event on the thread this already runs on
                    this.emitGetPet.onNext(routingContext);
               });

                routerFactory.addHandler(HttpMethod.POST, "/pets", routingContext -> {
                    //emit NewPet event on the thread this already runs on
                    this.emitNewPet.onNext(routingContext);
                });

                // generate the router
                Router apirouter = routerFactory.getRouter();

                // create server and register route handler
                HttpServer server = vertx.createHttpServer(new HttpServerOptions().setPort(8080).setHost("localhost"));
                server.requestHandler(apirouter::accept).listen();


            } else {
                // Spec loading failed
                Throwable exception = ar.cause();
            }

        });
    }

    public Observable<RoutingContext> getNewPetObservable(){
        return this.emitNewPet;
    }

    public Observable<RoutingContext> getGetPetObservable(){
        return this.emitGetPet;
    }

}
