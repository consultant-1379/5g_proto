package hello;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.RequestParameter;
import io.vertx.ext.web.api.RequestParameters;


public class PetStore {
    private Observable<RoutingContext> observeNewPet;
    private Observable<RoutingContext> observeGetPet;


    PetStore(final Observable<RoutingContext> newPets
             ,final Observable<RoutingContext> getPets) {

        this.observeNewPet = newPets;
        this.observeGetPet = getPets;


        //Observe Events on the computation thread pool.
        observeNewPet
                .observeOn(Schedulers.computation())
                .subscribeWith(new NewPetObserver());

        observeGetPet
                .observeOn(Schedulers.computation())
                .subscribeWith(new GetPetObserver());
    }

    private class NewPetObserver implements Observer<RoutingContext> {
        @Override
        public void onSubscribe(Disposable d) {
            System.out.println("onSubscribe");
        }

        @Override
        public void onNext(RoutingContext routingContext) {
            //System.out.println(Thread.currentThread().getName());

            // extract body and and add some id for response
            RequestParameters params = routingContext.get("parsedParameters");
            RequestParameter body = params.body();
            JsonObject jsonPet = body.getJsonObject();
            jsonPet.put("id",42);
            routingContext.response()
                    .setStatusCode(201)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(jsonPet.encodePrettily());
        }

        @Override
        public void onError(Throwable e) {
            System.out.println("onError");
        }

        @Override
        public void onComplete() {
            System.out.println("onComplete");

        }
    }

    private class GetPetObserver implements Observer<RoutingContext> {
        @Override
        public void onSubscribe(Disposable d) {
            System.out.println("onSubscribe");
        }

        @Override
        public void onNext(RoutingContext routingContext) {
            //System.out.println(Thread.currentThread().getName());

            // extract parameter id from path
            RequestParameters params = routingContext.get("parsedParameters");
            Long petid = params.pathParameter("id").getLong();

            JsonObject pet = new JsonObject();
            pet.put("id",petid);
            pet.put("name","marvin");
            routingContext.response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(pet.encodePrettily());
        }

        @Override
        public void onError(Throwable e) {
            System.out.println("onError");
        }

        @Override
        public void onComplete() {
            System.out.println("onComplete");

        }
    }

}
