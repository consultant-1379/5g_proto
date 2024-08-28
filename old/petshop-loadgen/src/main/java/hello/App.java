package hello;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpVersion;
import io.vertx.ext.web.RoutingContext;
import io.vertx.reactivex.RxHelper;
import io.vertx.ext.web.client.WebClientOptions;


import java.util.Random;

public class App {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        if((args.length == 1) && args[0].equals("client")) {

            System.out.println("PetStore client:");

            int nRequests = 1000000;
            WebClientOptions options = new WebClientOptions()
                    .setProtocolVersion(HttpVersion.HTTP_2) //enable http2
                    .setHttp2ClearTextUpgrade(false)        //http-prior-knowledge (no http1.1 upgrade)
                    .setHttp2MultiplexingLimit(5)          //max streams
                    .setHttp2MaxPoolSize(10);                //max connections

            Client client = new Client(vertx, 8080, "localhost", options);

            Flowable<Integer> randGenerator = Flowable.generate(Random::new, (random, emitter) -> {
                emitter.onNext(random.nextInt(1000));
            });

            Long startTime = System.nanoTime();
            Double sumDelay = randGenerator//.map((some) -> { System.out.println(some); return some;})
                    //note: observeOn pulls 128 items in one go
                    //.observeOn(RxHelper.scheduler(vertx))
                    .map((n)-> new ClientRequestState(n))
                    .flatMap(client::rxGetFlowable,client.getMaxParallel())
                    .take(nRequests)
                    .map((requestState) -> {
                        if (!requestState.isResponseOk()) {
                            System.out.println("Oh, oh, ...");
                        }
                        return requestState;
                    })
                    .map(requestState -> requestState.duration())
                    .reduce(0d,(x,y) -> x+y)
                    .blockingGet();
            Long stopTime = System.nanoTime();
            Double seconds = (stopTime - startTime) / 1000000000.0d ;

            System.out.println("---------------------------");
            System.out.println("Seconds: " + seconds + ", Requests " + nRequests);
            System.out.println("Req/sec: " + nRequests/seconds + ", Delay in ms: " + sumDelay/nRequests);
            System.out.println("---------------------------");

            client.close();
            //Observable.interval(1, TimeUnit.SECONDS).take(5).blockingLast();
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
