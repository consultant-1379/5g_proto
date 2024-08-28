package com.ericsson.sc.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.tapcol.TapCol;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServer;

public class PrometheusClient
{
    private static final Logger log = LoggerFactory.getLogger(PrometheusClient.class);
    private int port;
    private String path;
    private PrometheusMeterRegistry prometheusRegistry;
    private String host;

    public PrometheusClient(String host,
                            int port,
                            PrometheusMeterRegistry prometheusRegistry,
                            String path)
    {
        this.port = port;
        this.prometheusRegistry = prometheusRegistry;
        this.path = path;
        this.host = host;
    }

    public void run()
    {
        HttpServer.create()
                  .host(host)
                  .port(port)
                  .route(routes -> routes.get(path,
                                              (request,
                                               response) -> response.sendString(Mono.just(this.prometheusRegistry.scrape()))))
                  .bindNow();
        log.debug("Started Prometheus Client. Listening on {}:{}", host, port);
    }

}
