package hello;

import io.reactivex.Observable;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpVersion;
import io.vertx.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.rxjava.ext.web.client.WebClient;
import io.vertx.rxjava.core.buffer.Buffer;
import rx.Single;

public class App {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        if((args.length == 1) && args[0].equals("client")) {

            System.out.println("I am a client");

            // see https://vertx.io/docs/vertx-web-client/java/ for more background
            io.vertx.rxjava.core.Vertx rxVertx = new io.vertx.rxjava.core.Vertx(vertx);

            //create a client with http2/tcp (aka h2c) as tls is not enabled
            WebClient client = WebClient.create(rxVertx, new WebClientOptions()
                    .setProtocolVersion(HttpVersion.HTTP_2) //enable http2
                    .setHttp2ClearTextUpgrade(false)        //http-prior-knowledge (no http1.1 upgrade)
                    .setHttp2MultiplexingLimit(20)          //max streams
                    .setHttp2MaxPoolSize(1));               //max connections

            Single<HttpResponse<Buffer>> single = client
                    .get(8080, "localhost","/pets/5")
                    .rxSend();

            //The Single can be flatMap into a flow, but here we simply stop

            HttpResponse resp = single
                    .toBlocking()
                    .value();
            System.out.println(resp.bodyAsJsonObject().encodePrettily());

            System.exit(0);
        }

        RestServer server = new RestServer();
        Observable<RoutingContext> newPets = server.getNewPetObservable();
        Observable<RoutingContext> getPets = server.getGetPetObservable();

        PetStore store = new PetStore(newPets,getPets);
        server.start(vertx);

//        vertx.deployVerticle(RestServer.class.getName()
//                //,new DeploymentOptions().setWorker(true).setInstances(4)
//                ,new DeploymentOptions().setInstances(8)
//
//        );
    }
}
