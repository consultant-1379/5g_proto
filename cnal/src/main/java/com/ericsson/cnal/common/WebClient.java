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
 * Created on: Feb 14, 2020
 *     Author: eedstl
 */

package com.ericsson.cnal.common;

import java.io.File;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cnal.common.CertificateObserver.Secret;
import com.ericsson.cnal.common.NrfCertificateInfo.ExternalCertificate;
import com.ericsson.cnal.common.NrfCertificateInfo.NrfCertificateHandling;
import com.ericsson.utilities.exceptions.Utils;
import com.ericsson.utilities.http.IfWebClient;
import com.ericsson.utilities.http.KubeProbe;
import com.ericsson.utilities.reactivex.VertxInstance;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.vertx.core.http.Http2Settings;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.net.OpenSSLEngineOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.net.SocketAddress;
import io.vertx.reactivex.ext.web.client.HttpRequest;

public class WebClient implements IfWebClient
{
    private static final Logger log = LoggerFactory.getLogger(WebClient.class);

    private final Disposable disposableSecrets;
    private final Disposable disposableExtCertHandling;
    private final Disposable combinedDisposable;
    private io.vertx.reactivex.ext.web.client.WebClient delegate;
    private Instant delegateCreationTime;
    private WebClientOptions options;
    private KubeProbe kubeProbe;
    private Optional<CertificateObserver.Secret> latestOldSecret;
    private BehaviorSubject<Optional<NrfCertificateInfo>> nrfCertInfoSubj;

    public WebClient()
    {
        final Secret secret = new CertificateObserver.Secret();

        log.debug("secret={}", secret);

        this.options = this.createOptions(secret);
        this.delegate = io.vertx.reactivex.ext.web.client.WebClient.create(VertxInstance.get(), this.options);
        this.delegateCreationTime = Instant.now();
        this.disposableSecrets = null;
        this.disposableExtCertHandling = null;
        this.combinedDisposable = null;
        this.kubeProbe = KubeProbe.Handler.singleton().register(KubeProbe.of().setAlive(true).setReady(true));
    }

    public WebClient(final Flowable<CertificateObserver.Secret> secrets)
    {
        this.options = this.createOptions(new CertificateObserver.Secret()); // This will cause the client to not use TLS.
        this.delegate = io.vertx.reactivex.ext.web.client.WebClient.create(VertxInstance.get(), this.options);
        this.delegateCreationTime = Instant.now();
        this.disposableSecrets = secrets != null ? secrets.subscribe(this::update,
                                                                     t -> log.warn("Error updating web-client. Cause: {}",
                                                                                   Utils.toString(t, log.isDebugEnabled())))
                                                 : null;
        this.disposableExtCertHandling = null;
        this.combinedDisposable = null;
        this.kubeProbe = KubeProbe.Handler.singleton().register(KubeProbe.of().setAlive(true).setReady(true));
    }

    public WebClient(final Flowable<CertificateObserver.Secret> secrets,
                     final Flowable<NrfCertificateInfo> nrfExtCertInfo)
    {
        this.options = this.createTlsBasicOptions(); // This will initialize TLS client without certificates
        this.delegate = io.vertx.reactivex.ext.web.client.WebClient.create(VertxInstance.get(), this.options);
        this.delegateCreationTime = Instant.now();
        this.nrfCertInfoSubj = BehaviorSubject.create();
        // old secret updates for NRF client
        this.disposableSecrets = secrets != null ? secrets.map(Optional::ofNullable)
                                                          .defaultIfEmpty(Optional.empty())
                                                          .subscribe(secret -> latestOldSecret = secret)
                                                 : null;
        // NRF external certificate handling updates
        this.disposableExtCertHandling = nrfExtCertInfo != null ? nrfExtCertInfo.map(Optional::ofNullable)
                                                                                .defaultIfEmpty(Optional.empty())
                                                                                .subscribe(certInfo ->
                                                                                {
                                                                                    nrfCertInfoSubj.onNext(certInfo);
                                                                                    if (certInfo.isPresent())
                                                                                    {
                                                                                        log.debug("Nrf client about to be updated with new certificates");
                                                                                        this.update(latestOldSecret, certInfo);
                                                                                    }

                                                                                },
                                                                                           t -> log.warn("Error updating web-client. Cause: {}",
                                                                                                         Utils.toString(t, log.isDebugEnabled())))
                                                                : null;
        // old secret updates for NRF client, considering the configuration state
        this.combinedDisposable = secrets != null ? secrets.map(Optional::ofNullable)
                                                           .defaultIfEmpty(Optional.empty())
                                                           .withLatestFrom(nrfCertInfoSubj.toFlowable(BackpressureStrategy.LATEST),
                                                                           (oldItem,
                                                                            updatedItem) ->
                                                                           {
                                                                               if (updatedItem.isPresent()
                                                                                   && (updatedItem.get().getHandling().equals(NrfCertificateHandling.HYBRID)
                                                                                       || updatedItem.get().getHandling().equals(NrfCertificateHandling.OLD)))
                                                                               {
                                                                                   log.debug("Nrf client about to be updated with old certificates");
                                                                                   this.update(oldItem, updatedItem);
                                                                               }

                                                                               return oldItem;
                                                                           })
                                                           .subscribe()
                                                  : null;

        this.kubeProbe = KubeProbe.Handler.singleton().register(KubeProbe.of().setAlive(true).setReady(true));
    }

    public WebClient(final String pathToCertificate,
                     final String pathToKey,
                     final String... pathToCas)
    {
        final Secret secret = new CertificateObserver.Secret().setCertificate(new File(pathToCertificate)).setPrivateKey(new File(pathToKey));

        for (final String pathToCa : pathToCas)
            secret.adjustTrustCas(new File(pathToCa));

        log.debug("secret={}", secret);

        this.options = this.createOptions(secret);
        this.delegate = io.vertx.reactivex.ext.web.client.WebClient.create(VertxInstance.get(), this.options);
        this.delegateCreationTime = Instant.now();
        this.disposableSecrets = null;
        this.disposableExtCertHandling = null;
        this.combinedDisposable = null;
        this.kubeProbe = KubeProbe.Handler.singleton().register(KubeProbe.of().setAlive(true).setReady(true));
    }

    public WebClient(final String certvalue,
                     final String keyValue,
                     final List<String> caValue)
    {

        this.options = this.createOptions(certvalue, keyValue, caValue);
        this.delegate = io.vertx.reactivex.ext.web.client.WebClient.create(VertxInstance.get(), this.options);
        this.delegateCreationTime = Instant.now();
        this.disposableSecrets = null;
        this.disposableExtCertHandling = null;
        this.combinedDisposable = null;
        this.kubeProbe = KubeProbe.Handler.singleton().register(KubeProbe.of().setAlive(true).setReady(true));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.esc.scp.manager.file.IfWebClient#close()
     */
    @Override
    public synchronized void close(boolean reopen)
    {
        if (!reopen)
        {
            try
            {
                this.dispose();

                log.debug("Closing connection {}, this={}", this.delegate, this);
                this.delegate.close();
            }
            catch (Exception e)
            {
                log.warn("Error closing connection {}. Cause: {}", this.delegate, Utils.toString(e, log.isDebugEnabled()));
            }

            KubeProbe.Handler.singleton().deregister(this.kubeProbe.setAlive(true));
        }
        else
        {
            try
            {
                if (Instant.now().isAfter(this.delegateCreationTime.plusSeconds(5))) // Allow delegate to live for at least some seconds.
                {
                    log.debug("Trying to open new connection.");
                    final io.vertx.reactivex.ext.web.client.WebClient delegate = io.vertx.reactivex.ext.web.client.WebClient.create(VertxInstance.get(),
                                                                                                                                    this.options);

                    try
                    {
                        log.debug("Successfully opened new connection {}, closing old connection {}, this={}", delegate, this.delegate, this);

                        this.delegate.close();
                    }
                    catch (Exception e)
                    {
                        log.warn("Error closing connection {}. Cause: {}", this.delegate, Utils.toString(e, log.isDebugEnabled()));
                    }

                    this.delegate = delegate;
                    this.delegateCreationTime = Instant.now();
                    this.kubeProbe.setAlive(true);
                }
            }
            catch (Exception e)
            {
                log.warn("Error opening new connection. Cause: {}", Utils.toString(e, log.isDebugEnabled()));
                this.kubeProbe.setAlive(false);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.esc.scp.manager.file.IfWebClient#delete(int,
     * java.lang.String, java.lang.String)
     */
    @Override
    public HttpRequest<Buffer> delete(int port,
                                      String host,
                                      String requestURI)
    {
        return this.getDelegate().delete(port, host, requestURI);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.esc.scp.manager.file.IfWebClient#delete(java.lang.String)
     */
    @Override
    public HttpRequest<Buffer> delete(String requestURI)
    {
        return this.getDelegate().delete(requestURI);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.esc.scp.manager.file.IfWebClient#delete(java.lang.String,
     * java.lang.String)
     */
    @Override
    public HttpRequest<Buffer> delete(String host,
                                      String requestURI)
    {
        return this.getDelegate().delete(host, requestURI);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ericsson.esc.scp.manager.file.IfWebClient#deleteAbs(java.lang.String)
     */
    @Override
    public HttpRequest<Buffer> deleteAbs(String absoluteURI)
    {
        return this.getDelegate().deleteAbs(absoluteURI);
    }

    public void dispose()
    {
        log.debug("Disposing the subscription to secret updates, this={}", this);

        if (this.disposableSecrets != null && !this.disposableSecrets.isDisposed())
            this.disposableSecrets.dispose();
        if (this.disposableExtCertHandling != null && !this.disposableExtCertHandling.isDisposed())
            this.disposableExtCertHandling.dispose();
        if (this.combinedDisposable != null && !this.combinedDisposable.isDisposed())
            this.combinedDisposable.dispose();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.esc.scp.manager.file.IfWebClient#get(int, java.lang.String,
     * java.lang.String)
     */
    @Override
    public HttpRequest<Buffer> get(int port,
                                   String host,
                                   String requestURI)
    {
        return this.getDelegate().get(port, host, requestURI);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.esc.scp.manager.file.IfWebClient#get(java.lang.String)
     */
    @Override
    public HttpRequest<Buffer> get(String requestURI)
    {
        return this.getDelegate().get(requestURI);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.esc.scp.manager.file.IfWebClient#get(java.lang.String,
     * java.lang.String)
     */
    @Override
    public HttpRequest<Buffer> get(String host,
                                   String requestURI)
    {
        return this.getDelegate().get(host, requestURI);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.esc.scp.manager.file.IfWebClient#getAbs(java.lang.String)
     */
    @Override
    public HttpRequest<Buffer> getAbs(String absoluteURI)
    {
        return this.getDelegate().getAbs(absoluteURI);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.esc.scp.manager.file.IfWebClient#head(int,
     * java.lang.String, java.lang.String)
     */
    @Override
    public HttpRequest<Buffer> head(int port,
                                    String host,
                                    String requestURI)
    {
        return this.getDelegate().head(port, host, requestURI);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.esc.scp.manager.file.IfWebClient#head(java.lang.String)
     */
    @Override
    public HttpRequest<Buffer> head(String requestURI)
    {
        return this.getDelegate().head(requestURI);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.esc.scp.manager.file.IfWebClient#head(java.lang.String,
     * java.lang.String)
     */
    @Override
    public HttpRequest<Buffer> head(String host,
                                    String requestURI)
    {
        return this.getDelegate().head(host, requestURI);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.esc.scp.manager.file.IfWebClient#headAbs(java.lang.String)
     */
    @Override
    public HttpRequest<Buffer> headAbs(String absoluteURI)
    {
        return this.getDelegate().headAbs(absoluteURI);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.esc.scp.manager.file.IfWebClient#patch(int,
     * java.lang.String, java.lang.String)
     */
    @Override
    public HttpRequest<Buffer> patch(int port,
                                     String host,
                                     String requestURI)
    {
        return this.getDelegate().patch(port, host, requestURI);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.esc.scp.manager.file.IfWebClient#patch(java.lang.String)
     */
    @Override
    public HttpRequest<Buffer> patch(String requestURI)
    {
        return this.getDelegate().patch(requestURI);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.esc.scp.manager.file.IfWebClient#patch(java.lang.String,
     * java.lang.String)
     */
    @Override
    public HttpRequest<Buffer> patch(String host,
                                     String requestURI)
    {
        return this.getDelegate().patch(host, requestURI);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.esc.scp.manager.file.IfWebClient#patchAbs(java.lang.String)
     */
    @Override
    public HttpRequest<Buffer> patchAbs(String absoluteURI)
    {
        return this.getDelegate().patchAbs(absoluteURI);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.esc.scp.manager.file.IfWebClient#post(int,
     * java.lang.String, java.lang.String)
     */
    @Override
    public HttpRequest<Buffer> post(int port,
                                    String host,
                                    String requestURI)
    {
        return this.getDelegate().post(port, host, requestURI);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.esc.scp.manager.file.IfWebClient#post(java.lang.String)
     */
    @Override
    public HttpRequest<Buffer> post(String requestURI)
    {
        return this.getDelegate().post(requestURI);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.esc.scp.manager.file.IfWebClient#post(java.lang.String,
     * java.lang.String)
     */
    @Override
    public HttpRequest<Buffer> post(String host,
                                    String requestURI)
    {
        return this.getDelegate().post(host, requestURI);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.esc.scp.manager.file.IfWebClient#postAbs(java.lang.String)
     */
    @Override
    public HttpRequest<Buffer> postAbs(String absoluteURI)
    {
        return this.getDelegate().postAbs(absoluteURI);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.esc.scp.manager.file.IfWebClient#put(int, java.lang.String,
     * java.lang.String)
     */
    @Override
    public HttpRequest<Buffer> put(int port,
                                   String host,
                                   String requestURI)
    {
        return this.getDelegate().put(port, host, requestURI);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.esc.scp.manager.file.IfWebClient#put(java.lang.String)
     */
    @Override
    public HttpRequest<Buffer> put(String requestURI)
    {
        return this.getDelegate().put(requestURI);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.esc.scp.manager.file.IfWebClient#put(java.lang.String,
     * java.lang.String)
     */
    @Override
    public HttpRequest<Buffer> put(String host,
                                   String requestURI)
    {
        return this.getDelegate().put(host, requestURI);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.esc.scp.manager.file.IfWebClient#putAbs(java.lang.String)
     */
    @Override
    public HttpRequest<Buffer> putAbs(String absoluteURI)
    {
        return this.getDelegate().putAbs(absoluteURI);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ericsson.esc.scp.manager.file.IfWebClient#request(io.vertx.core.http.
     * HttpMethod, int, java.lang.String, java.lang.String)
     */
    @Override
    public HttpRequest<Buffer> request(HttpMethod method,
                                       int port,
                                       String host,
                                       String requestURI)
    {
        return this.getDelegate().request(method, port, host, requestURI);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ericsson.esc.scp.manager.file.IfWebClient#request(io.vertx.core.http.
     * HttpMethod, io.vertx.core.http.RequestOptions)
     */
    @Override
    public HttpRequest<Buffer> request(HttpMethod method,
                                       RequestOptions options)
    {
        return this.getDelegate().request(method, options);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ericsson.esc.scp.manager.file.IfWebClient#request(io.vertx.core.http.
     * HttpMethod, io.vertx.reactivex.core.net.SocketAddress, int, java.lang.String,
     * java.lang.String)
     */
    @Override
    public HttpRequest<Buffer> request(HttpMethod method,
                                       SocketAddress serverAddress,
                                       int port,
                                       String host,
                                       String requestURI)
    {
        return this.getDelegate().request(method, port, host, requestURI);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ericsson.esc.scp.manager.file.IfWebClient#request(io.vertx.core.http.
     * HttpMethod, io.vertx.reactivex.core.net.SocketAddress,
     * io.vertx.core.http.RequestOptions)
     */
    @Override
    public HttpRequest<Buffer> request(HttpMethod method,
                                       SocketAddress serverAddress,
                                       RequestOptions options)
    {
        return this.getDelegate().request(method, serverAddress, options);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ericsson.esc.scp.manager.file.IfWebClient#request(io.vertx.core.http.
     * HttpMethod, io.vertx.reactivex.core.net.SocketAddress, java.lang.String)
     */
    @Override
    public HttpRequest<Buffer> request(HttpMethod method,
                                       SocketAddress serverAddress,
                                       String requestURI)
    {
        return this.getDelegate().request(method, serverAddress, requestURI);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ericsson.esc.scp.manager.file.IfWebClient#request(io.vertx.core.http.
     * HttpMethod, io.vertx.reactivex.core.net.SocketAddress, java.lang.String,
     * java.lang.String)
     */
    @Override
    public HttpRequest<Buffer> request(HttpMethod method,
                                       SocketAddress serverAddress,
                                       String host,
                                       String requestURI)
    {
        return this.getDelegate().request(method, host, requestURI);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ericsson.esc.scp.manager.file.IfWebClient#request(io.vertx.core.http.
     * HttpMethod, java.lang.String)
     */
    @Override
    public HttpRequest<Buffer> request(HttpMethod method,
                                       String requestURI)
    {
        return this.getDelegate().request(method, requestURI);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ericsson.esc.scp.manager.file.IfWebClient#request(io.vertx.core.http.
     * HttpMethod, java.lang.String, java.lang.String)
     */
    @Override
    public HttpRequest<Buffer> request(HttpMethod method,
                                       String host,
                                       String requestURI)
    {
        return this.getDelegate().request(method, host, requestURI);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ericsson.esc.scp.manager.file.IfWebClient#requestAbs(io.vertx.core.http.
     * HttpMethod, io.vertx.reactivex.core.net.SocketAddress, java.lang.String)
     */
    @Override
    public HttpRequest<Buffer> requestAbs(HttpMethod method,
                                          SocketAddress serverAddress,
                                          String absoluteURI)
    {
        return this.getDelegate().requestAbs(method, serverAddress, absoluteURI);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ericsson.esc.scp.manager.file.IfWebClient#requestAbs(io.vertx.core.http.
     * HttpMethod, java.lang.String)
     */
    @Override
    public HttpRequest<Buffer> requestAbs(HttpMethod method,
                                          String absoluteURI)
    {
        log.debug("requestAbs: delegate={}", this.getDelegate());
        return this.getDelegate().requestAbs(method, absoluteURI);
    }

    public synchronized void update(final CertificateObserver.Secret secret)
    {
        log.debug("secret={}, this={}", secret, this);

        final io.vertx.reactivex.ext.web.client.WebClient old = this.delegate;

        if (this.updateOptions(secret))
        {
            old.close();
            this.delegate = io.vertx.reactivex.ext.web.client.WebClient.create(VertxInstance.get(), this.options);
            this.delegateCreationTime = Instant.now();
        }
    }

    public synchronized void update(final Optional<CertificateObserver.Secret> oldSecret,
                                    final Optional<NrfCertificateInfo> nrfExtCertInfo)
    {
        final io.vertx.reactivex.ext.web.client.WebClient old = this.delegate;

        if (this.updateOptions(oldSecret, nrfExtCertInfo))
        {
            old.close();
            this.delegate = io.vertx.reactivex.ext.web.client.WebClient.create(VertxInstance.get(), this.options);
            this.delegateCreationTime = Instant.now();
        }
        else
        {
            log.warn("NRF client not able to be updated due to invalid certificates. Old healthy client is preserved, if existing.");
        }
    }

    private WebClientOptions createTlsBasicOptions()
    {
        return new WebClientOptions().setProtocolVersion(HttpVersion.HTTP_2) // enable http2
                                     .setHttp2ClearTextUpgrade(false) // http-prior-knowledge (no http1.1 upgrade)
                                     .setEnabledSecureTransportProtocols(Set.of("TLSv1.2", "TLSv1.3")) // Set TLS versions already
                                                                                                       // here to suppress printout
                                                                                                       // of TLS1/1.1 on client
                                                                                                       // startup
                                     .setSslEngineOptions(new OpenSSLEngineOptions())
                                     .setInitialSettings(new Http2Settings().setPushEnabled(false))
                                     .setSsl(true)
                                     .setUseAlpn(true)
                                     .setLogActivity(true)
                                     .setTrustAll(false)
                                     .setVerifyHost(true);
    }

    private WebClientOptions createOptions(final CertificateObserver.Secret secret)
    {
        final WebClientOptions options = new WebClientOptions().setProtocolVersion(HttpVersion.HTTP_2) // enable http2
                                                               .setHttp2ClearTextUpgrade(false) // http-prior-knowledge (no http1.1 upgrade)
                                                               .setEnabledSecureTransportProtocols(Set.of("TLSv1.2", "TLSv1.3")) // Set TLS versions already
                                                                                                                                 // here to suppress printout
                                                                                                                                 // of TLS1/1.1 on client
                                                                                                                                 // startup
                                                               .setSslEngineOptions(new OpenSSLEngineOptions())
                                                               .setInitialSettings(new Http2Settings().setPushEnabled(false));

        this.modifyOptions(options);

        if (secret.useTls())
        {
            log.info("TLS enabled.");

            options.setSsl(true)
                   .setUseAlpn(true)
                   .setLogActivity(true)
                   .setTrustAll(false)
                   .setVerifyHost(true)
                   .setKeyCertOptions(new PemKeyCertOptions().setKeyPath(secret.getPrivateKey().name()).setCertPath(secret.getCertificate().name()));

            if (!secret.getTrustCas().isEmpty())
            {
                final PemTrustOptions pemTrustOptions = new PemTrustOptions();
                secret.getTrustCas().forEach(trustCa -> pemTrustOptions.addCertPath(trustCa.name()));
                options.setPemTrustOptions(pemTrustOptions);
            }

            this.modifyOptionsForTls(options);
        }
        else
        {
            log.info("TLS disabled.");
        }

        return options;
    }

    private WebClientOptions createOptions(final String certValue,
                                           final String keyValue,
                                           final List<String> caValue)
    {
        final WebClientOptions wcOptions = new WebClientOptions().setProtocolVersion(HttpVersion.HTTP_2) // enable http2
                                                                 .setHttp2ClearTextUpgrade(false) // http-prior-knowledge (no http1.1 upgrade)
                                                                 .setEnabledSecureTransportProtocols(Set.of("TLSv1.2", "TLSv1.3")) // Set TLS versions already
                                                                                                                                   // here to suppress printout
                                                                                                                                   // of TLS1/1.1 on client
                                                                                                                                   // startup
                                                                 .setSslEngineOptions(new OpenSSLEngineOptions())
                                                                 .setInitialSettings(new Http2Settings().setPushEnabled(false));

        this.modifyOptions(wcOptions);

        log.info("TLS enabled.");

        wcOptions.setSsl(true)
                 .setUseAlpn(true)
                 .setLogActivity(true)
                 .setTrustAll(false)
                 .setVerifyHost(true)
                 .setKeyCertOptions(new PemKeyCertOptions().addCertValue(io.vertx.core.buffer.Buffer.buffer(certValue))
                                                           .addKeyValue(io.vertx.core.buffer.Buffer.buffer(keyValue)));

        final var pemTrustOptions = new PemTrustOptions();
        caValue.forEach(ca -> pemTrustOptions.addCertValue(io.vertx.core.buffer.Buffer.buffer(ca)));
        wcOptions.setPemTrustOptions(pemTrustOptions);

        this.modifyOptionsForTls(wcOptions);
        log.info("TLS disabled.");

        return wcOptions;
    }

    private WebClientOptions createOptions(final Optional<CertificateObserver.Secret> oldSecret,
                                           final Optional<NrfCertificateInfo> nrfExtCertInfo)
    {
        final WebClientOptions wcOptions = new WebClientOptions().setProtocolVersion(HttpVersion.HTTP_2) // enable http2
                                                                 .setHttp2ClearTextUpgrade(false) // http-prior-knowledge (no http1.1 upgrade)
                                                                 .setEnabledSecureTransportProtocols(Set.of("TLSv1.2", "TLSv1.3")) // Set TLS versions already
                                                                                                                                   // here to suppress printout
                                                                                                                                   // of TLS1/1.1 on client
                                                                                                                                   // startup
                                                                 .setSslEngineOptions(new OpenSSLEngineOptions())
                                                                 .setInitialSettings(new Http2Settings().setPushEnabled(false));

        this.modifyOptions(wcOptions);

        log.info("TLS enabled.");

        wcOptions.setSsl(true).setUseAlpn(true).setLogActivity(true).setTrustAll(false).setVerifyHost(true);

        final var pemKeyCertOptions = new PemKeyCertOptions();
        final var pemTrustOptions = new PemTrustOptions();
        // this will always be present according to parent method
        final var certInfo = nrfExtCertInfo.get();
        final var extCertificate = certInfo.getExtCertificate();

        final var isNewAsymKeyHandling = ((certInfo.getHandling().equals(NrfCertificateHandling.NEW)
                                           || certInfo.getHandling().equals(NrfCertificateHandling.HYBRID))
                                          && extCertificate.isPresent() && extCertificate.get().getCertificate() != null
                                          && extCertificate.get().getKey() != null);

        final var isOldHandling = certInfo.getHandling().equals(NrfCertificateHandling.HYBRID) || certInfo.getHandling().equals(NrfCertificateHandling.OLD);

        final var isNewCaHandling = (certInfo.getHandling().equals(NrfCertificateHandling.NEW) || certInfo.getHandling().equals(NrfCertificateHandling.HYBRID))
                                    && extCertificate.isPresent() && extCertificate.get().getTrustCa() != null;
        if (isNewAsymKeyHandling) // new or hybrid certificate handling
        {
            log.debug("New certificate handling for Nrf key and cert");
            pemKeyCertOptions.addCertValue(io.vertx.core.buffer.Buffer.buffer(new String(Base64.getDecoder().decode(extCertificate.get().getCertificate()))))
                             .addKeyValue(io.vertx.core.buffer.Buffer.buffer(new String(Base64.getDecoder().decode(extCertificate.get().getKey()))));
        }
        else if (isOldHandling) // hybrid mode or old certificate handling
        {
            log.debug("Old certificate handling for Nrf key and cert");
            pemKeyCertOptions.setKeyPath(oldSecret.get().getPrivateKey().name()).setCertPath(oldSecret.get().getCertificate().name());
        }

        if (isNewCaHandling) // new or hybrid ca handling
        {
            log.debug("New certificate handling for Nrf CA");
            pemTrustOptions.addCertValue(io.vertx.core.buffer.Buffer.buffer(new String(Base64.getDecoder().decode(extCertificate.get().getTrustCa()))));
        }
        else if (isOldHandling) // old or hybrid ca handling
        {
            log.debug("Old certificate handling for Nrf CA");
            oldSecret.get().getTrustCas().forEach(trustCa -> pemTrustOptions.addCertPath(trustCa.name()));
        }

        wcOptions.setKeyCertOptions(pemKeyCertOptions).setPemTrustOptions(pemTrustOptions);

        this.modifyOptionsForTls(wcOptions);

        return wcOptions;
    }

    private synchronized io.vertx.reactivex.ext.web.client.WebClient getDelegate()
    {
        return this.delegate;
    }

    private boolean updateOptions(final CertificateObserver.Secret secret)
    {
        if (!secret.useTls() && !this.options.isSsl())
            return false;

        this.options = this.createOptions(secret);

        return true;
    }

    private boolean updateOptions(final Optional<CertificateObserver.Secret> oldSecret,
                                  final Optional<NrfCertificateInfo> nrfExtCertInfo)
    {
        if (nrfExtCertInfo.isEmpty())
        {
            log.debug("NRF certificate handling information is not available");
            return false;
        }

        final NrfCertificateHandling handling = nrfExtCertInfo.get().getHandling();
        final var extCertificateInfo = nrfExtCertInfo.get().getExtCertificate();

        // if new/hybrid cert handling, then the new certs must exist
        if ((handling.equals(NrfCertificateHandling.NEW) || handling.equals(NrfCertificateHandling.HYBRID)) && extCertificateInfo.isEmpty())
        {
            log.debug("NRF external certificates are not available for configured new/hybrid certificate handling");
            return false;
        }
        // if new cert handling, then all the new cert, key and ca must exist
        if (handling.equals(NrfCertificateHandling.NEW))
        {
            var extCert = extCertificateInfo.map(ExternalCertificate::getCertificate).orElse(null);
            var extKey = extCertificateInfo.map(ExternalCertificate::getKey).orElse(null);
            var trustCa = extCertificateInfo.map(ExternalCertificate::getTrustCa).orElse(null);

            if (extCert == null || extKey == null || trustCa == null)
            {
                log.debug("NRF external certificates are not available for configured new certificate handling");

                return false;
            }
        }

        // if new old/hybrid handling, then all the old secret must exist
        if ((handling.equals(NrfCertificateHandling.OLD) || handling.equals(NrfCertificateHandling.HYBRID)) && oldSecret.isEmpty())
        {
            log.debug("NRF certificates are not available for configured old certificate handling");

            return false;
        }

        // if old handling, then old cert, key and ca path must exist
        if (handling.equals(NrfCertificateHandling.OLD)
            && (!oldSecret.get().getCertificate().exists || !oldSecret.get().getPrivateKey().exists || oldSecret.get().getTrustCas().isEmpty()))
        {
            log.debug("NRF external certificates are not available for configured old certificate handling");

            return false;
        }

        // if hybrid handling, then either old or new cert, key and ca must exist
        if (handling.equals(NrfCertificateHandling.HYBRID))
        {
            var extCert = extCertificateInfo.get().getCertificate();
            var extKey = extCertificateInfo.get().getKey();
            var trustCa = extCertificateInfo.get().getTrustCa();

            if ((!oldSecret.get().getCertificate().exists && extCert == null) || (!oldSecret.get().getPrivateKey().exists && extKey == null)
                || (oldSecret.get().getTrustCas().isEmpty() && trustCa == null))
            {
                log.debug("NRF external certificates are not available for configured hybrid certificate handling");

                return false;
            }
        }

        this.options = this.createOptions(oldSecret, nrfExtCertInfo);

        return true;
    }

    /**
     * Hook to modify options suitable for non-TLS or TLS. Default: nothing is
     * modified.
     * 
     * @param options
     * @return options passed
     */
    protected WebClientOptions modifyOptions(final WebClientOptions options)
    {
        return options;
    }

    /**
     * Hook to modify options suitable for TLS. Default: nothing is modified.
     * 
     * @param options
     * @return options passed
     */
    protected WebClientOptions modifyOptionsForTls(final WebClientOptions options)
    {
        return options;
    }
}