package com.ericsson.esc.bsf.services.http.vertX;

import com.ericsson.esc.bsf.services.cm.adp.service.ConfigurationsImpl;

import com.ericsson.esc.bsf.services.http.HttpServer;

import io.vertx.core.AbstractVerticle;
import org.jboss.resteasy.plugins.server.vertx.VertxRequestHandler;
import org.jboss.resteasy.plugins.server.vertx.VertxResteasyDeployment;
import org.jboss.resteasy.plugins.server.vertx.VertxJaxrsServer;

import rx.Completable;

public class HttpServerVertXImpl extends AbstractVerticle implements HttpServer {
    VertxJaxrsServer server;

    public HttpServerVertXImpl() {
    }

    @Override
    public Completable startService(int port) {
        return Completable.create(emitter -> {
            try {
              startDeployment(port);
              emitter.onCompleted();
            } catch(Exception e) {
                emitter.onError(e);
            }
        });
    }

    @Override
    public Completable startService() {
        return this.startService(8080);
    }

    @Override
    public Completable stopService() {
        return Completable.create(emitter -> {
            server.stop();
            emitter.onCompleted();
        });
    }

    public void startDeployment(int port) throws Exception {
        server = new VertxJaxrsServer();

        // Build the Jax-RS hello world deployment
        VertxResteasyDeployment deployment = new VertxResteasyDeployment();
        deployment.start();
        deployment.getRegistry().addPerInstanceResource(ConfigurationsImpl.class);

        // Start the front end server using the Jax-RS controller
        server.setDeployment(deployment);
        server.setPort(port);
        server.setRootResourcePath("/helloCM/cm");
        server.setSecurityDomain(null);
        server.start();

    }
}
