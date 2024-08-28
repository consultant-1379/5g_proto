package com.ericsson.utilities.http;

import java.net.URI;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.util.tls.DynamicTlsCertManager;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.http.impl.HttpUtils;
import io.vertx.core.net.KeyCertOptions;
import io.vertx.core.net.OpenSSLEngineOptions;
import io.vertx.core.net.TrustOptions;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.http.HttpClient;
import io.vertx.reactivex.core.http.HttpClientResponse;
import io.vertx.reactivex.core.http.HttpHeaders;
import io.vertx.reactivex.ext.web.client.WebClient;

public class WebClientProvider
{
    private static final Logger log = LoggerFactory.getLogger(WebClientProvider.class);

    private static final Set<String> ALLOWED_TLS_PROTOCOLS = Set.of("TLSv1.2", "TLSv1.3");

    private final Single<WebClient> webClient;
    private final DynamicTlsCertManager dynamicTls;
    private final boolean ssl;
    private final String clienthostname;

    public static Builder builder()
    {
        return new Builder();
    }

    private WebClientProvider(Vertx vertx,
                              Builder builder)
    {
        this.ssl = builder.options.isSsl();
        this.dynamicTls = builder.dynamicTls;
        final var init = dynamicTls == null ? Completable.complete() : dynamicTls.start();
        this.webClient = init.andThen(Single.fromCallable(() ->
        {
            return WebClient.create(vertx, builder.options);
        })).cache();
        this.clienthostname = builder.clienthostname;
    }

    private WebClientProvider(HttpClient httpClient,
                              Builder builder)
    {
        this.ssl = builder.options.isSsl();
        this.dynamicTls = builder.dynamicTls;
        final var init = dynamicTls == null ? Completable.complete() : dynamicTls.start();
        this.webClient = init.andThen(Single.fromCallable(() ->
        {
            return WebClient.wrap(httpClient, builder.options);
        })).cache();
        this.clienthostname = builder.clienthostname;
    }

    public boolean isSsl()
    {
        return this.ssl;

    }

    public Single<WebClient> getWebClient()
    {
        return this.webClient;
    }

    public Completable close()
    {
        final var finalize = (this.dynamicTls == null ? Completable.complete() : this.dynamicTls.terminate());
        return finalize //
                       .andThen(this.webClient.flatMapCompletable(WebClientProvider::closeWebClient))
                       .cache();
    }

    private static Completable closeWebClient(WebClient wc)
    {
        return Completable.fromAction(wc::close) //
                          .subscribeOn(Schedulers.io())
                          .doOnError(err -> log.warn("Failed to close WebClient", err))
                          .onErrorComplete();
    }

    public static class Builder
    {
        private final WebClientOptions options = new WebClientOptions() //
                                                                       .setProtocolVersion(HttpVersion.HTTP_2) // Enable HTTP2 by default
                                                                       .setUseAlpn(true) // Enable ALPN by default
        ;
        private String clienthostname;
        private boolean globalTracing;

        private DynamicTlsCertManager dynamicTls;

        public Builder withOpenSsl()
        {
            this.options.setOpenSslEngineOptions(new OpenSSLEngineOptions());
            return this;
        }

        /**
         * @deprecated (when after SC1.7 release and onwards, why DynamicTlsCertManager
         *             introduced and should be removed when no more dependencies exist)
         */
        @Deprecated(since = "SC1.7", forRemoval = true)
        public Builder withDynamicTls(String certPath,
                                      String caPath)
        {
            Objects.requireNonNull(certPath, caPath);

            final var certWatch = new CertificateWatch(certPath, caPath).getCertificates();
            this.dynamicTls = new DynamicTlsCertManager(certWatch.getFirst(), certWatch.getSecond());
            final var trustOptions = TrustOptions.wrap(dynamicTls.getTrustManager());
            final var keyCertOptions = KeyCertOptions.wrap(dynamicTls.getKeyManager());
            withOpenSsl();
            withTls();
            this.options //
                        .setTrustOptions(trustOptions)
                        .setKeyCertOptions(keyCertOptions);
            return this;
        }

        public Builder withDynamicTls(DynamicTlsCertManager dynamicTls)
        {
            this.dynamicTls = dynamicTls;
            final var trustOptions = TrustOptions.wrap(dynamicTls.getTrustManager());
            final var keyCertOptions = KeyCertOptions.wrap(dynamicTls.getKeyManager());
            withOpenSsl();
            withTls();
            this.options //
                        .setTrustOptions(trustOptions)
                        .setKeyCertOptions(keyCertOptions);
            return this;
        }

        public Builder withDynamicCaCert(DynamicTlsCertManager dynamicTls)
        {
            this.dynamicTls = dynamicTls;
            final var trustOptions = TrustOptions.wrap(dynamicTls.getTrustManager());
            withOpenSsl();
            withTls();
            this.options //
                        .setTrustOptions(trustOptions);
            return this;
        }

        public Builder withTls()
        {
            this.options //
                        .setEnabledSecureTransportProtocols(ALLOWED_TLS_PROTOCOLS)
                        .setSsl(true)
                        .setUseAlpn(true);
            return this;
        }

        public Builder withOptions(Consumer<WebClientOptions> options)
        {
            try
            {
                options.accept(this.options);
            }
            catch (Exception e)
            {
                throw new IllegalArgumentException("Failed to set WebClient otpions", e);
            }
            return this;
        }

        public Builder withHostName(String hostname)
        {
            this.clienthostname = hostname;
            return this;
        }

        public Builder withFollowRedirectPost()
        {
            this.options.setFollowRedirects(true);
            return this;
        }

        public Builder withGlobalTracing(boolean enabled)
        {
            this.globalTracing = enabled;
            this.options.setGlobalTracing(enabled);
            return this;
        }

        public WebClientProvider build(Vertx vertx)
        {
            return new WebClientProvider(vertx, this);
        }

        public WebClientProvider build(io.vertx.core.Vertx vertx)
        {
            return new WebClientProvider(new HttpClient(vertx.createHttpClient(this.options).redirectHandler(NEW_REDIRECT_HANDLER)), this);
        }

        private final Function<io.vertx.core.http.HttpClientResponse, Future<RequestOptions>> NEW_REDIRECT_HANDLER = resp ->
        {
            try
            {
                int statusCode = resp.statusCode();
                String location = resp.getHeader(HttpHeaders.LOCATION);
                log.debug("Redirecting for {} and {}", statusCode, location);
                if (location != null && (statusCode == 301 || statusCode == 302 || statusCode == 303 || statusCode == 307 || statusCode == 308))
                {
                    HttpMethod m = resp.request().getMethod();
                    if (statusCode == 303)
                    {
                        m = HttpMethod.GET;
                    }
                    else if (m != HttpMethod.GET && m != HttpMethod.HEAD && m != HttpMethod.POST)
                    {
                        return null;
                    }
                    URI uri = HttpUtils.resolveURIReference(resp.request().absoluteURI(), location);
                    boolean ssl;
                    int port = uri.getPort();
                    String protocol = uri.getScheme();
                    char chend = protocol.charAt(protocol.length() - 1);
                    if (chend == 'p')
                    {
                        ssl = false;
                        if (port == -1)
                        {
                            port = 80;
                        }
                    }
                    else if (chend == 's')
                    {
                        ssl = true;
                        if (port == -1)
                        {
                            port = 443;
                        }
                    }
                    else
                    {
                        return null;
                    }
                    String requestURI = uri.getPath();
                    if (requestURI == null || requestURI.isEmpty())
                    {
                        requestURI = "/";
                    }
                    String query = uri.getQuery();
                    if (query != null)
                    {
                        requestURI += "?" + query;
                    }
                    RequestOptions options = new RequestOptions();
                    options.setMethod(m);
                    options.setHost(uri.getHost());
                    options.setPort(port);
                    options.setSsl(ssl);
                    options.setURI(requestURI);
                    options.setHeaders(resp.request().headers());
                    options.removeHeader(HttpHeaders.CONTENT_LENGTH);
                    return Future.succeededFuture(options);
                }
                return null;
            }
            catch (Exception e)
            {
                return Future.failedFuture(e);
            }
        };

    }

    /**
     * @return clientHostName
     */
    public String getHostName()
    {
        return this.clienthostname;
    }

}
