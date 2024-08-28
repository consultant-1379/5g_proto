/**
 * COPYRIGHT ERICSSON GMBH 2020
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Apr 20, 2020
 *     Author: eedstl
 */

package com.ericsson.cnal.common;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cnal.common.CertificateObserver.Secret;
import com.ericsson.utilities.exceptions.Utils;
import com.ericsson.utilities.http.ReconfigurableWebServer;
import com.ericsson.utilities.http.WebServerBuilder;
import com.ericsson.utilities.http.WebServerRouter;
import com.ericsson.utilities.reactivex.VertxInstance;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.Router;

public class WebServer implements ReconfigurableWebServer
{
    private static class MountedRouter
    {
        final String mountPoint;
        final Router router;

        MountedRouter(final String mountPoint,
                      final Router router)
        {
            this.mountPoint = mountPoint;
            this.router = router;
        }
    }

    private static final Logger log = LoggerFactory.getLogger(WebServer.class);

    private static final Set<String> ALLOWED_TLS_PROTOCOLS = Set.of("TLSv1.2", "TLSv1.3");

    private final Disposable disposable;
    private com.ericsson.utilities.http.WebServer delegate;
    private HttpServerOptions options;

    private final List<MountedRouter> routers;

    private final Consumer<HttpServerOptions> optionsSetter = options ->
    {
        final HttpServerOptions rhs = this.getOptions();

        options.setInitialSettings(rhs.getInitialSettings())
               .setHost(rhs.getHost())
               .setPort(rhs.getPort())
               .setSsl(rhs.isSsl())
               .setUseAlpn(rhs.isUseAlpn())
               .setLogActivity(true)
               .setEnabledSecureTransportProtocols(rhs.getEnabledSecureTransportProtocols())
               .setKeyCertOptions(rhs.getKeyCertOptions())
               .setPemTrustOptions(rhs.getPemTrustOptions());

        options.getEnabledCipherSuites().clear();
        rhs.getEnabledCipherSuites().forEach(options::addEnabledCipherSuite);
    };

    public WebServer(final HttpServerOptions options)
    {
        this.options = options;
        this.routers = new ArrayList<>();

        final Secret secret = new CertificateObserver.Secret();

        log.debug("secret={}", secret);

        this.createOptions(secret);
        this.delegate = com.ericsson.utilities.http.WebServer.builder().withOptions(this.optionsSetter).build(VertxInstance.get());
        this.disposable = null;
    }

    public WebServer(final HttpServerOptions options,
                     final Flowable<CertificateObserver.Secret> secrets)
    {
        this.options = options;
        this.routers = new ArrayList<>();

        this.createOptions(new CertificateObserver.Secret()); // This will cause the client to not use TLS.
        this.delegate = com.ericsson.utilities.http.WebServer.builder().withOptions(this.optionsSetter).build(VertxInstance.get());
        this.disposable = secrets.subscribe(this::update, t -> log.warn("Error updating web-client. Cause: {}", Utils.toString(t, log.isDebugEnabled())));
    }

    public WebServer(final HttpServerOptions options,
                     final String pathToCertificate,
                     final String pathToKey,
                     final String... pathToCas)
    {
        this.options = options;
        this.routers = new ArrayList<>();

        final Secret secret = new CertificateObserver.Secret().setCertificate(new File(pathToCertificate)).setPrivateKey(new File(pathToKey));

        for (final String pathToCa : pathToCas)
            secret.adjustTrustCas(new File(pathToCa));

        log.debug("secret={}", secret);

        this.createOptions(secret);
        this.delegate = com.ericsson.utilities.http.WebServer.builder().withOptions(this.optionsSetter).build(VertxInstance.get());
        this.disposable = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.utilities.http.RouterHandler#baseUri()
     */
    @Override
    public URI baseUri()
    {
        return this.delegate.baseUri();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ericsson.utilities.http.RouterHandler#configureRouter(java.util.function.
     * Consumer)
     */
    @Override
    public void configureRouter(Consumer<Router> consumer)
    {
        this.delegate.configureRouter(consumer);
    }

    public void dispose()
    {
        log.debug("Disposing the subscription to secret updates, this={}", this);

        if (this.disposable != null && !this.disposable.isDisposed())
            this.disposable.dispose();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.utilities.http.RouterHandler#getHttpOptions()
     */
    @Override
    public HttpServerOptions getHttpOptions()
    {
        return this.options;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.utilities.http.RouterHandler#getVertx()
     */
    @Override
    public Vertx getVertx()
    {
        return this.delegate.getVertx();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.utilities.http.RouterHandler#mountRouter(java.lang.String,
     * io.vertx.reactivex.ext.web.Router)
     */
    @Override
    public void mountRouter(String mountPoint,
                            Router router)
    {
        this.routers.add(new MountedRouter(mountPoint, router));
        this.delegate.mountRouter(mountPoint, router);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.utilities.http.RouterHandler#startListener()
     */
    @Override
    public Completable startListener()
    {
        if (this.options.isSsl())
            return this.delegate.startListener();

        return Completable.complete();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.utilities.http.RouterHandler#stopListener()
     */
    @Override
    public Completable stopListener()
    {
        return this.delegate.stopListener();
    }

    public synchronized void update(final CertificateObserver.Secret secret)
    {
        log.debug("secret={}, this={}", secret, this);

        final com.ericsson.utilities.http.WebServer old = this.delegate;

        if (this.updateOptions(secret))
        {
            old.stopListener().subscribe(() ->
            {
                if (this.options.isSsl())
                {
                    this.delegate = com.ericsson.utilities.http.WebServer.builder().withOptions(this.optionsSetter).build(VertxInstance.get());
                    this.routers.forEach(router -> this.delegate.mountRouter(router.mountPoint, router.router));
                    this.delegate.startListener()//
                                 .onErrorResumeNext(t ->
                                 {
                                     log.info("{}. Retrying.", t.getMessage());
                                     return this.delegate.stopListener().andThen(Completable.error(t));
                                 })
                                 .retryWhen(h -> h.delay(200, TimeUnit.MILLISECONDS))
                                 .subscribe(() ->
                                 {
                                 }, t -> log.error("Error starting web-server.", t));
                }
            });
        }
    }

    private synchronized void createOptions(final CertificateObserver.Secret secret)
    {
        final HttpServerOptions options = new HttpServerOptions();
        this.optionsSetter.accept(options);

        if (secret.useTls())
        {
            log.info("TLS enabled.");

            options.setSsl(true)
                   .setUseAlpn(true)
                   .setLogActivity(true)
                   .setEnabledSecureTransportProtocols(ALLOWED_TLS_PROTOCOLS)
                   .setKeyCertOptions(new PemKeyCertOptions().setKeyPath(secret.getPrivateKey().name()).setCertPath(secret.getCertificate().name()));

            com.ericsson.utilities.http.WebServerBuilder.setEnabledCipherSuites(options);

            if (!secret.getTrustCas().isEmpty())
            {
                final PemTrustOptions pemTrustOptions = new PemTrustOptions();
                secret.getTrustCas().forEach(trustCa -> pemTrustOptions.addCertPath(trustCa.name()));
                options.setPemTrustOptions(pemTrustOptions);
            }
        }
        else
        {
            log.info("TLS disabled.");
        }

        this.options = options;
    }

    private synchronized HttpServerOptions getOptions()
    {
        return this.options;
    }

    private boolean updateOptions(final CertificateObserver.Secret secret)
    {
        if (!secret.useTls() && !this.options.isSsl())
            return false;

        this.createOptions(secret);

        return true;
    }

    /**
     * Hook to modify options suitable for non-TLS or TLS. Default: nothing is
     * modified.
     * 
     * @param options
     * @return options passed
     */
    protected HttpServerOptions modifyOptions(final HttpServerOptions options)
    {
        return options;
    }

    /**
     * Hook to modify options suitable for TLS. Default: nothing is modified.
     * 
     * @param options
     * @return options passed
     */
    protected HttpServerOptions modifyOptionsForTls(final HttpServerOptions options)
    {
        return options;
    }

    @Override
    public Completable shutdown()
    {
        return this.delegate.shutdown();
    }

    @Override
    public Completable shutdown(long timeoutMillis)
    {
        return this.delegate.shutdown(timeoutMillis);
    }

    @Override
    public int actualPort()
    {
        return this.delegate.actualPort();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Single<com.ericsson.utilities.http.WebServer> reconfigure(WebServerBuilder builder)
    {
        return this.delegate.reconfigure(builder);
    }

    @Override
    public List<WebServerRouter> childRouters()
    {
        return delegate.childRouters();
    }
}