package hello;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.api.RequestParameter;
import io.vertx.ext.web.api.RequestParameters;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class RestServer extends AbstractVerticle {

    private List<String> names = new ArrayList<>();
    String uuid = UUID.randomUUID().toString();

    @Override
    public void start() {

        OpenAPI3RouterFactory.create(vertx, "src/main/resources/petstore.yaml", ar -> {
            if (ar.succeeded()) {
                OpenAPI3RouterFactory routerFactory = ar.result();
                routerFactory.addHandlerByOperationId("listPets", rc -> {
                    JsonArray pets = new JsonArray();
                    rc.response().setStatusCode(201).end(pets.encode());
                });
                routerFactory.addFailureHandlerByOperationId("listPets", rc -> {
                    rc.response().setStatusCode(400).end();
                });
                Router apirouter = routerFactory.getRouter();

                HttpServer server = vertx.createHttpServer(new HttpServerOptions().setPort(8080).setHost("localhost"));
                server.requestHandler(apirouter::accept).listen();

            } else {
                Throwable exception = ar.cause();
            }
        });

    }
}

