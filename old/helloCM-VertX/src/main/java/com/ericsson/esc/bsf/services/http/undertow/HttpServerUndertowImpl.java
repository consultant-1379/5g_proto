package com.ericsson.esc.bsf.services.http.undertow;

import com.ericsson.esc.bsf.main.RestEasyApp;
import com.ericsson.esc.bsf.services.http.HttpServer;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.servlet.api.DeploymentInfo;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import rx.Completable;

import javax.servlet.ServletException;

public class HttpServerUndertowImpl implements HttpServer {
    private static UndertowJaxrsServer server;

    @Override
    public Completable startService(int port) {
        return Completable.create(emitter -> {
            try {
                startContainer(8080);
                emitter.onCompleted();
            } catch(ServletException e) {
                emitter.onError(e);
            }
        });
    }

    @Override
    public Completable startService() {
        return startService(80);
    }

    @Override
    public Completable stopService() {
        return Completable.create(emitter -> {
            server.stop();
            emitter.onCompleted();
        });
    }

    private void startContainer(int port) throws ServletException {
        server = new UndertowJaxrsServer();

        Undertow.Builder serverBuilder = Undertow.builder()
                                                  .setServerOption(UndertowOptions.ENABLE_HTTP2, true)
                                                  .addHttpListener(port, "localhost");
        server.start(serverBuilder);

        DeploymentInfo di = server.undertowDeployment(RestEasyApp.class);
        di.setClassLoader(com.ericsson.esc.bsf.main.App.class.getClassLoader())
                .setDeploymentName("helloCM")
                .setContextPath("/helloCM");

        server.deploy(di);
    }
}
