package com.ericsson.esc.bsf.services.cm.adp;

import com.ericsson.esc.bsf.services.cm.CMProvider;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
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
    private static UndertowJaxrsServer server;

    public void CMProviderImpl() throws ServletException {
    }

    @Override
    public Completable startService() {
        return Completable.create(emitter -> {
            try {
                startContainer(8080);
                emitter.onCompleted();
            } catch(ServletException e) {
                emitter.onError(e);
            }
        });

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
        return Completable.create(emitter -> {
            server.stop();
            emitter.onCompleted();
        });
    };

    private void startContainer(int port) throws ServletException {
        server = new UndertowJaxrsServer();

        Undertow.Builder serverBuilder = Undertow.builder().setServerOption(UndertowOptions.ENABLE_HTTP2, true)
                                                            .addHttpListener(port, "localhost");
        server.start(serverBuilder);

        DeploymentInfo di = server.undertowDeployment(RestEasyApp.class);
        di.setClassLoader(com.ericsson.esc.bsf.main.App.class.getClassLoader())
                .setDeploymentName("helloCM")
                .setContextPath("/helloCM");

        server.deploy(di);
    }
}
