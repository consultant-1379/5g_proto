package com.ericsson.esc.bsf.services.http;

import com.ericsson.esc.bsf.services.http.undertow.HttpServerUndertowImpl;
import com.ericsson.esc.bsf.services.http.vertX.HttpServerVertXImpl;

public class HttpServerFactory {
    private static HttpServer httpServer = null;

    public static HttpServer getHttpServer() {
        if (httpServer == null)
            httpServer = new HttpServerVertXImpl(); //  HttpServerUndertowImpl

        return httpServer;
    }
}
