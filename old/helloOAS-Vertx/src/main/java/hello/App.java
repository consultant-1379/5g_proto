package hello;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

public class App {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(RestServer.class.getName()
                //,new DeploymentOptions().setWorker(true).setInstances(4)
                  ,new DeploymentOptions().setInstances(8)

	);
   }
}
