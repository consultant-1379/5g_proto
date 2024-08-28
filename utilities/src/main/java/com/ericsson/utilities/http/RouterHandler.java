package com.ericsson.utilities.http;

import io.reactivex.Completable;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.reactivex.ext.web.Router;

//TODO rename interface: IWebServer
public interface RouterHandler extends WebServerRouter
{
    HttpServerOptions getHttpOptions();

    void mountRouter(String mountPoint,
                     Router router);

    Completable startListener();

    Completable stopListener();

}
