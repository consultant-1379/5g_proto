/**
 * COPYRIGHT ERICSSON GMBH 2018
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Oct 16, 2018
 *     Author: xchrfar
 */

package com.ericsson.utilities.http;

import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.util.tls.DynamicTlsCertManager;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.handler.LoggerFormat;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.http.HttpConnection;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.handler.LoggerHandler;

/**
 * A Vertx based HTTP server, along with a Router
 */
public class WebServer implements ReconfigurableWebServer
{
    private static final Logger log = LoggerFactory.getLogger(WebServer.class);

    private final Router router;
    private final HttpServer httpServer;
    private final HttpServerOptions options;
    private final boolean listenAll;
    private final Vertx vertx;
    private final URI baseUri;
    /**
     * Established HTTP, HTTP/2 connection
     */
    private final Set<HttpConnection> connections = new HashSet<>();
    /**
     * Emits the set of currently established connections, mirrors
     * {@link connections}
     */
    private final Subject<Set<HttpConnection>> connectionSubject = BehaviorSubject.createDefault(Set.<HttpConnection>of()).toSerialized();
    /**
     * The state of the server
     */
    private AtomicBoolean terminating = new AtomicBoolean(false);

    private DynamicTlsCertManager dynamicTls;

    Flowable<WebServer> create(Vertx vertx,
                               Flowable<WebServerBuilder> builder)
    {
        return builder.map(cfg -> new WebServer(vertx, cfg, null));
    }

    WebServer(Vertx vertx,
              WebServerBuilder builder)
    {
        this(vertx, builder, null);
    }

    /**
     * Create a new {@code WebServer}
     * 
     * @param vertx   A {@code Vertx} instance
     * @param builder The builder to use for configuration
     */
    WebServer(Vertx vertx,
              WebServerBuilder builder,
              Router router)
    {
        log.info("Creating new server: {}", this);
        Objects.requireNonNull(vertx);
        Objects.requireNonNull(builder);

        this.vertx = vertx;
        this.dynamicTls = builder.dynamicTls;
        this.options = new HttpServerOptions(builder.options);
        this.listenAll = builder.listenAll;
        log.debug("Options for server {} are: {}", this, options.isSsl());
        this.baseUri = createBaseUri(options);
        this.router = router != null ? router : Router.router(vertx);
        this.httpServer = vertx.createHttpServer(this.options);

        this.httpServer.connectionHandler(connection ->
        {
            if (!this.terminating.get())
            {
                addConnection(connection);
                connection.closeHandler(event -> removeConnection(connection));
            }
            else
            {
                log.warn("Server is terminating, closing down newly established connection {}", connection.remoteAddress());

                drainConnection(5 * 1000L, connection);
            }
        });

        // Enable HTTP tracing, if configured in builder
        if (builder.httpTracing)

        {
            final var loggerHandler = LoggerHandler.create(LoggerFormat.SHORT);
            this.configureRouter(r -> r.route().handler(loggerHandler));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Single<WebServer> reconfigure(final WebServerBuilder builder)
    {
        return Single.just(new WebServer(this.vertx, builder, this.router))
                     .doOnError(e -> log.error("error creating new WebServer", e))
                     .doOnSuccess(newServer -> log.info("Reconfiguring Server old: {} -> new: {}", this, newServer))
                     .flatMap(newServer -> this.shutdown() //
                                               .andThen(newServer.startListener())
                                               .toSingleDefault(newServer))
                     .doOnError(e -> log.error("error shuting dow old server new WebServer", e))
                     .doOnSubscribe(disp -> log.info("Updating server"));

    }

    /**
     * 
     * @return A new builder that can be used to create {@code WebServer} instances
     */
    public static WebServerBuilder builder()
    {
        return new WebServerBuilder();
    }

    @Override
    public Completable shutdown()
    {
        return shutdown(30 * 1000L);
    }

    @Override
    public Completable shutdown(long timeoutMillis)
    {
        return this.shutdownAllConnections(timeoutMillis) //
                   .timeout(timeoutMillis * 2, TimeUnit.MILLISECONDS)
                   .doOnError(err -> log.warn("Error while shutting down HTTP server", err))
                   .onErrorComplete()
                   .andThen(this.stopListener())
                   .doOnComplete(() -> this.terminating.set(false));
    }

    /**
     * The actual port the server is listening on. This is useful if you bound the
     * server specifying 0 as port number signifying an ephemeral port
     * 
     * @return the actual port the server is listening on.
     */
    @Override
    public int actualPort()
    {
        return this.httpServer.actualPort();
    }

    @Override
    public void mountRouter(String mountPoint,
                            Router router)
    {
        this.router.mountSubRouter(mountPoint, router);
    }

    @Override
    public void configureRouter(Consumer<Router> consumer)
    {
        consumer.accept(this.router);
    }

    @Override
    public URI baseUri()
    {
        return this.baseUri;
    }

    @Override
    public Completable startListener()
    {
        Completable init = this.dynamicTls != null ? this.dynamicTls.start() : Completable.complete();
        return init.andThen(prepareListener()) //
                   .andThen((listenAll ? //
                                       httpServer.rxListen(options.getPort()) : httpServer.rxListen()))
                   .doOnError(e -> log.error("error starting listener", e)) //
                   .ignoreElement()
                   .doOnComplete(() -> log.info("Server started {}", this));
    }

    @Override
    public Completable stopListener()
    {
        return this.httpServer.rxClose().doOnComplete(() -> log.info("Server {} terminated", this));
    }

    @Override
    public Vertx getVertx()
    {
        return this.vertx;
    }

    public DynamicTlsCertManager getDynamicTls()
    {
        return this.dynamicTls;
    }

    private static URI createBaseUri(HttpServerOptions options)
    {
        try
        {
            return new URL(options.isSsl() ? "https" : "http", options.getHost(), options.getPort(), "").toURI();
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    private Completable prepareListener()
    {
        return Completable.fromAction(() ->
        {
            log.info("Starting HTTP server{}, host: {} port: {} , configuration: {}",
                     this.listenAll ? " on 0.0.0.0" : "",
                     this.options.getHost(),
                     this.options.getPort(),
                     this.options.toJson().encode());

            if (log.isDebugEnabled())
                router.getRoutes().forEach(route -> log.debug(" path: {} route: {}", route.getPath(), route));

            this.httpServer.requestHandler(router);
        });
    }

    private Completable shutdownAllConnections(long timeoutMillis)
    {
        return Completable.defer(() ->
        {
            this.terminating.set(true);
            getConnections().forEach(conn -> drainConnection(timeoutMillis, conn));
            return this.connectionSubject //
                                         .map(Set::isEmpty)
                                         .filter(Boolean::booleanValue)
                                         .firstOrError()
                                         .ignoreElement();
        });
    }

    private void addConnection(HttpConnection conn)
    {

        synchronized (this)
        {
            final var added = this.connections.add(conn);
            if (!added)
            {
                log.error("Connection already exists {}", conn);
            }
            this.connectionSubject.onNext(Set.copyOf(this.connections));
        }
    }

    private void removeConnection(HttpConnection conn)
    {
        synchronized (this)
        {
            final var removed = this.connections.remove(conn);
            if (!removed)
            {
                log.error("Cannot remove non existent connection {}", conn);
            }
            this.connectionSubject.onNext(Set.copyOf(this.connections));
        }
    }

    private Set<HttpConnection> getConnections()
    {
        synchronized (this)
        {
            return Set.copyOf(this.connections);
        }
    }

    private void drainConnection(long timeoutMillis,
                                 HttpConnection conn)
    {
        log.info("Draining connection {}->{}", conn.remoteAddress(), conn.localAddress());
        var success = false;
        try
        {
            conn.shutdown(timeoutMillis);
            success = true;
        }
        catch (UnsupportedOperationException e)
        {
            // Vertx does not support for non HTTP/2 connections
            // close() shall be used instead
        }
        catch (Exception e)
        {
            log.warn("Failed to drain connection {}->{}: {} , will close instead", conn.remoteAddress(), conn.localAddress(), e.getMessage());
        }
        if (!success)
        {
            try
            {
                conn.close();
            }
            catch (Exception ex)
            {
                log.warn("Failed to close connection {}->{}: {}", conn.remoteAddress(), conn.localAddress(), ex.getMessage());
            }
        }
    }

    public Router getRouter()
    {
        return this.router;
    }

    @Override
    public List<WebServerRouter> childRouters()
    {
        return List.of(this);
    }

    public HttpServerOptions getHttpOptions()
    {
        return this.options;
    }
}
