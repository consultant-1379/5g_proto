package com.ericsson.utilities.http;

import java.net.URI;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.Router;

/**
 * A Pool of webservers listening on the same IP endpoint
 */
public class WebServerPool implements ReconfigurableWebServer
{
    private static final Logger log = LoggerFactory.getLogger(WebServerPool.class);
    private final List<? extends WebServer> servers;

    WebServerPool(Vertx vertx,
                  int cardinality,
                  WebServerBuilder builder)
    {
        if (cardinality < 1)
        {
            throw new IllegalArgumentException("Invalid cardinality: " + cardinality);
        }
        if (builder.port == 0)
            throw new IllegalArgumentException("Ephemeral listening port not suppoprter for a pool of servers");
        this.servers = IntStream.range(0, cardinality) //
                                .mapToObj(i -> builder.build(vertx))
                                .collect(Collectors.toUnmodifiableList());
    }

    private WebServerPool(List<WebServer> servers)
    {
        this.servers = servers;
    }

    @Override
    public void mountRouter(String mountPoint,
                            Router router)
    {
        this.servers.forEach(webServer -> webServer.mountRouter(mountPoint, router));
    }

    @Override
    public URI baseUri()
    {
        return this.servers.get(0).baseUri();
    }

    @Override
    public void configureRouter(Consumer<Router> consumer)
    {
        this.servers.forEach(webServer -> webServer.configureRouter(consumer));
    }

    @Override
    public HttpServerOptions getHttpOptions()
    {
        return this.servers.get(0).getHttpOptions();
    }

    @Override
    public Vertx getVertx()
    {
        return this.servers.get(0).getVertx();
    }

    @Override
    public Completable startListener()
    {
        return Observable.fromIterable(this.servers)
                         .doOnSubscribe(disp -> log.info("Starting Web Server pool of size: {}", this.servers.size()))
                         .flatMapCompletable(WebServer::startListener);
    }

    @Override
    public Completable stopListener()
    {
        return Observable.fromIterable(this.servers)
                         .doOnSubscribe(disp -> log.info("Stopping Web Server pool of size: {}", this.servers.size()))
                         .flatMapCompletable(WebServer::stopListener);
    }

    @Override
    public Completable shutdown(long timeoutMillis)
    {
        return Observable.fromIterable(this.servers) //
                         .concatMapCompletable(server -> server.shutdown(timeoutMillis));
    }

    @Override
    public Completable shutdown()
    {

        return Observable.fromIterable(this.servers) //
                         .concatMapCompletable(WebServer::shutdown);
    }

    @Override
    public int actualPort()
    {
        return this.servers.get(0).actualPort();
    }

    @Override
    public Single<RouterHandler> reconfigure(WebServerBuilder builder)
    {
        return shutdown().doOnSubscribe(disp -> log.info("Reconfiguring Web server pool of size {}, shutting down all servers", this.servers.size()))
                         .andThen(Observable.fromIterable(this.servers)
                                            .concatMapSingle(server -> server.reconfigure(builder)) //
                                            .toList()
                                            .doOnSubscribe(disp -> log.info("Server Pool reconfiguration old->{} new->{}", this, builder))
                                            .map(WebServerPool::new));
    }

    @Override
    public List<WebServerRouter> childRouters()
    {
        return this.servers.stream().map(r -> (WebServerRouter) r).collect(Collectors.toUnmodifiableList());
        // TODO: return (List<WebServerRouter>) this.servers;
    }

    public List<? extends WebServer> getServers()
    {
        return this.servers;
    }
}
