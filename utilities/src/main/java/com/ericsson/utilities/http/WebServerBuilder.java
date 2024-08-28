/** 
 * COPYRIGHT ERICSSON GMBH 2019
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Feb 26, 2020
 *     Author: echfari
 */
package com.ericsson.utilities.http;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import com.ericsson.sc.util.tls.DynamicTlsCertManager;

import io.vertx.core.http.ClientAuth;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.KeyCertOptions;
import io.vertx.core.net.OpenSSLEngineOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.core.net.TrustOptions;
import io.vertx.reactivex.core.Vertx;

/**
 * Builder for {@link WebServer} objects
 */
public class WebServerBuilder
{
    private static final Set<String> ALLOWED_TLS_PROTOCOLS = Set.of("TLSv1.2", "TLSv1.3");

    public static void setEnabledCipherSuites(final HttpServerOptions options)
    {
        // Bug DND-32353
        // Vertx seems to have a buggy implementation of the selection of cipher suites,
        // so unless configured explicitly with a list of enabled cipher suites it picks
        // up the default list from boringssl (the underlying SSL engine used by the
        // SslContext for vertx handlers). To override that behavior simple clear the
        // list of enabled cipher suites and then explicitly add the allowed cipher
        // suites.
        // The default cipher suites in boringssl can be found there:
        // https://boringssl.googlesource.com/boringssl/+/refs/heads/master/ssl/ssl_cipher.cc
        // Below code has been generated from that list, all unsupported or unsafe
        // suites have been commented out.

        options.getEnabledCipherSuites().clear();

        final List<String> suites = new ArrayList<>();
        suites.add("TLS_CHACHA20_POLY1305_SHA256"); // suiteTLS13
        suites.add("TLS_AES_128_GCM_SHA256"); // suiteTLS13
        suites.add("TLS_AES_256_GCM_SHA384"); // suiteTLS13
        suites.add("TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256"); // suiteECDHE | suiteECDSA | suiteTLS12
        suites.add("TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256"); // suiteECDHE | suiteTLS12
        suites.add("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"); // suiteECDHE | suiteTLS12
        suites.add("TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256"); // suiteECDHE | suiteECDSA | suiteTLS12
        suites.add("TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384"); // suiteECDHE | suiteTLS12 | suiteSHA384
        suites.add("TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384"); // suiteECDHE | suiteECDSA | suiteTLS12 | suiteSHA384
//        suites.add("TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256");  // suiteECDHE | suiteTLS12, (unsupported cipher suite)
//        suites.add("TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256");  // suiteECDHE | suiteECDSA | suiteTLS12, (unsupported cipher suite)
//        suites.add("TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"); // suiteECDHE , (unsupported cipher suite)
//        suites.add("TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA"); // suiteECDHE | suiteECDSA, (unsupported cipher suite)
//        suites.add("TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384"); // suiteECDHE | suiteTLS12 | suiteSHA384, (unsupported cipher suite)
//        suites.add("TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384"); // suiteECDHE | suiteECDSA | suiteTLS12 | suiteSHA384, (unsupported cipher suite)
//        suites.add("TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA"); // suiteECDHE, (unsupported cipher suite)
//        suites.add("TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA"); // suiteECDHE | suiteECDSA, (unsupported cipher suite)
        suites.add("TLS_RSA_WITH_AES_128_GCM_SHA256"); // suiteTLS12
        suites.add("TLS_RSA_WITH_AES_256_GCM_SHA384"); // suiteTLS12 | suiteSHA384
//        suites.add("TLS_RSA_WITH_AES_128_CBC_SHA256"); // suiteTLS12, (unsupported cipher suite)
//        suites.add("TLS_RSA_WITH_AES_256_CBC_SHA256"); // suiteTLS12, (unsupported cipher suite)
//        suites.add("TLS_RSA_WITH_AES_128_CBC_SHA"); // 0, (unsupported cipher suite)
//        suites.add("TLS_RSA_WITH_AES_256_CBC_SHA"); // 0, (unsupported cipher suite)
//        suites.add("TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA"); // suiteECDHE, (unsupported cipher suite)
//        suites.add("TLS_RSA_WITH_3DES_EDE_CBC_SHA"); // 0, (unsupported cipher suite)
        suites.add("TLS_ECDHE_PSK_WITH_CHACHA20_POLY1305_SHA256"); // suiteECDHE | suitePSK | suiteTLS12
//        suites.add("TLS_ECDHE_PSK_WITH_AES_128_CBC_SHA"); // suiteECDHE | suitePSK, (unsupported cipher suite)
//        suites.add("TLS_ECDHE_PSK_WITH_AES_256_CBC_SHA"); // suiteECDHE | suitePSK, (unsupported cipher suite)
//        suites.add("TLS_PSK_WITH_AES_128_CBC_SHA"); // suitePSK, (unsupported cipher suite)
//        suites.add("TLS_PSK_WITH_AES_256_CBC_SHA"); // suitePSK, (unsupported cipher suite)
//        suites.add("TLS_RSA_WITH_NULL_SHA"); // 0, (unsupported cipher suite)

        suites.forEach(options::addEnabledCipherSuite);
    }

    final HttpServerOptions options;
    String host;
    int port;
    boolean httpTracing = false;
    boolean globalTracing = false;
    boolean listenAll = false;
    DynamicTlsCertManager dynamicTls;

    WebServerBuilder()
    {
        // Always enable netty-tcnative for TLS for high performance. This requires
        // netty-tcnative as build dependency
        this.options = new HttpServerOptions() //
                                              .setOpenSslEngineOptions(new OpenSSLEngineOptions());

        this.host = options.getHost();
        this.port = options.getPort();
    }

    /**
     * Copy constructor
     * 
     * @param builder The WebServerBuilder to copy
     */
    WebServerBuilder(WebServerBuilder builder)
    {
        this.options = new HttpServerOptions(builder.options);
        this.host = builder.host;
        this.port = builder.port;
        this.httpTracing = builder.httpTracing;
        this.globalTracing = builder.globalTracing;
        this.listenAll = builder.listenAll;
    }

    /**
     * Build a new {@link WebServer}
     * 
     * @param vertx The {@code Vertx} instance
     * @return The created {@code WebServer}
     */
    public WebServer build(Vertx vertx)
    {
        return new WebServer(vertx, this);
    }

    /**
     * Build a new {@link WebServerPool}
     * 
     * @param vertx    A {@code Vertx} instance
     * @param replicas The desired number of {@link WebServer} replicas
     * @return The created {@code WebServerPool}
     */
    public WebServerPool build(Vertx vertx,
                               int replicas)
    {
        if (replicas <= 0)
            throw new IllegalArgumentException("Invalid number of replicas: " + replicas);

        return new WebServerPool(vertx, replicas, this);
    }

    @Deprecated
    public WebServerBuilder withDynamicTls(String certPath,
                                           List<String> caList)
    {
        Objects.requireNonNull(certPath);
        if (caList == null || caList.isEmpty())
        {
            throw new IllegalArgumentException("At least one trusted certificate required");
        }
        withTls();

        final var watcher = new CertificateWatch(certPath, caList);
        this.dynamicTls = new DynamicTlsCertManager(watcher.getCertificates().getFirst(), watcher.getCertificates().getSecond());
        this.options //
                    .setKeyCertOptions(KeyCertOptions.wrap(this.dynamicTls.getKeyManager())) // Mandatory for all TLS servers
                    .setTrustOptions(TrustOptions.wrap(this.dynamicTls.getTrustManager())) // For Client certificate authentication
                    .setClientAuth(ClientAuth.REQUIRED);

        return this;
    }

    @Deprecated
    public WebServerBuilder withDynamicTls(String certPath) // No client auth
    {
        Objects.requireNonNull(certPath);
        withTls();

        // No client auth
        final var watcher = new CertificateWatch(certPath, List.of());
        this.dynamicTls = new DynamicTlsCertManager(watcher.getCertificates().getFirst());
        this.options //
                    .setKeyCertOptions(KeyCertOptions.wrap(this.dynamicTls.getKeyManager()))
                    .setClientAuth(ClientAuth.NONE);

        return this;
    }

    public WebServerBuilder withDynamicTls(DynamicTlsCertManager dynamicTls)
    {
        Objects.requireNonNull(dynamicTls);
        if (dynamicTls.getType() != DynamicTlsCertManager.Type.MUTUAL)
        {
            throw new IllegalArgumentException("Dynamic TLS configuration not appropriate for mutual TLS");
        }
        withTls();
        this.dynamicTls = dynamicTls;
        this.options //
                    .setKeyCertOptions(KeyCertOptions.wrap(this.dynamicTls.getKeyManager())) // Mandatory for all TLS servers
                    .setTrustOptions(TrustOptions.wrap(this.dynamicTls.getTrustManager())) // For Client certificate authentication
                    .setClientAuth(ClientAuth.REQUIRED);
        return this;
    }

    public WebServerBuilder withDynamicTlsNonMutal(DynamicTlsCertManager dynamicTls)
    {
        Objects.requireNonNull(dynamicTls);
        if (dynamicTls.getType() != DynamicTlsCertManager.Type.KEYCERT)
        {
            throw new IllegalArgumentException("Dynamic TLS configuration not appropriate for non mutual TLS");
        }
        this.withTls();
        this.dynamicTls = dynamicTls;
        this.options //
                    .setKeyCertOptions(KeyCertOptions.wrap(this.dynamicTls.getKeyManager())) // Mandatory for all TLS servers
                    .setClientAuth(ClientAuth.NONE);
        return this;
    }

    public WebServerBuilder withTls()
    {
        this.options //
                    .setSsl(true)
                    .setUseAlpn(true)
                    .setEnabledSecureTransportProtocols(ALLOWED_TLS_PROTOCOLS);

        setEnabledCipherSuites(this.options);

        return this;
    }

    public WebServerBuilder withListenAll()
    {
        this.listenAll = true;
        return this;
    }

    public boolean isListenAll()
    {
        return this.listenAll;
    }

    public WebServerBuilder withHttpTracing(boolean enabled)
    {
        this.httpTracing = enabled;
        return this;
    }

    public WebServerBuilder withGlobalTracing(boolean enabled)
    {
        this.globalTracing = enabled;
        this.options.setGlobalTracing(enabled);
        return this;
    }

    /**
     * 
     * @param port The TCP port to listen to
     */
    public WebServerBuilder withPort(final int port)
    {
        if (port < 0)
            throw new IllegalArgumentException("Invalid port " + port);

        this.port = port;
        this.options.setPort(port);
        return this;
    }

    /**
     * 
     * @param host The server host name or IP address
     */
    public WebServerBuilder withHost(final String host)
    {
        Objects.requireNonNull(host);

        this.host = host;
        this.options.setHost(host);
        return this;
    }

    /**
     * 
     * @param httpServerOptionSetter A function that sets desired
     *                               {@link HttpServerOptions}
     */
    public WebServerBuilder withOptions(Consumer<HttpServerOptions> httpServerOptionSetter)
    {
        Objects.requireNonNull(httpServerOptionSetter);

        httpServerOptionSetter.accept(this.options);
        return this;
    }

    /**
     * Configure WebServer with TLS. This call sets multiple options.
     * 
     * @param certificatesPath
     * @return A directory containing "ca.pem","key.pem" and "certificate.pem"
     *         certificate files
     */
    @Deprecated
    public WebServerBuilder withTls(String certificatesPath)
    {
        Objects.requireNonNull(certificatesPath);

        if (certificatesPath.isEmpty())
        {
            throw new IllegalArgumentException("Empty certificatesPath");
        }

        this.options.setSsl(true)
                    .setUseAlpn(true)
                    .setEnabledSecureTransportProtocols(ALLOWED_TLS_PROTOCOLS)
                    .setPemTrustOptions(new PemTrustOptions().addCertPath(certificatesPath + "/ca.pem"))
                    .setKeyCertOptions(new PemKeyCertOptions().setKeyPath(certificatesPath + "/key.pem").setCertPath(certificatesPath + "/certificate.pem"))
                    .setLogActivity(true)
                    .setClientAuth(ClientAuth.REQUIRED);

        setEnabledCipherSuites(this.options);

        return this;
    }
}
