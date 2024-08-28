package com.ericsson.esc.bsf.services.cm.adp;

import com.ericsson.esc.bsf.services.cm.CMProvider;
import com.ericsson.esc.bsf.services.http.HttpServer;
import com.ericsson.esc.bsf.services.http.HttpServerFactory;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;

import javax.servlet.ServletException;
import javax.ws.rs.core.Application;

import com.ericsson.esc.bsf.main.RestEasyApp;
import rx.Completable;

import static rx.observers.Subscribers.create;

import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.jboss.resteasy.spi.ResteasyDeployment;

public class CMProviderADPImpl implements CMProvider {
    private HttpServer httpServer;

    public CMProviderADPImpl() {
        httpServer = HttpServerFactory.getHttpServer();
    }

    @Override
    public Completable startService() {
        return httpServer.startService(8080);
        /*
        // startService client part in background
        Client client = ClientBuilder.newClient();
        client.register(RxObservableInvokerProvider.class);

        Observable<Response> observable = client
                .target("http://example.com/resource")
                .request()
                .rx(RxObservableInvoker.class)
                .get();
        */
    }

    @Override
    public Completable stopService() {
        return httpServer.stopService();
    };
}
