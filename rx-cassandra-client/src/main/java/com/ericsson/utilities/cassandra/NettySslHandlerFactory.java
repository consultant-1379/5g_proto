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
 * Created on: Jul 29, 2021
 *     Author: echfari
 */
package com.ericsson.utilities.cassandra;

import java.io.ByteArrayInputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.oss.driver.api.core.metadata.EndPoint;
import com.datastax.oss.driver.internal.core.ssl.SslHandlerFactory;
import com.ericsson.sc.util.LogLimitter;
import com.ericsson.sc.util.LogLimitter.Loggers;

import io.netty.channel.Channel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslProvider;

/**
 * Enables using netty-tc-native for Cassandra TLS
 */
public class NettySslHandlerFactory implements SslHandlerFactory
{

    private enum Lbl
    {
        DB_OVERLOAD
    }

    private static final Logger log = LoggerFactory.getLogger(NettySslHandlerFactory.class);
    private final SslContext sslContext;

    private final boolean verifyHost;
    private Loggers<Lbl> safeLog = LogLimitter.create(Lbl.class, log, 2000);

    /**
     * Create a new instance
     * 
     * @param trustedCerts Trusted Certificates in PEM format
     * @param verifyHost   True if server endpoint identification should be
     *                     performed
     * @param clientCert   Client certificate in PEM format
     * @param clientKey    Client key in PEM format
     * @throws SSLException If there is an issue with provided certificates
     */
    public NettySslHandlerFactory(String trustedCerts,
                                  boolean verifyHost,
                                  String clientCert,
                                  String clientKey,
                                  KeyManager km,
                                  TrustManager tm) throws SSLException
    {
        this.verifyHost = verifyHost;
        final var builder = SslContextBuilder.forClient().sslProvider(SslProvider.OPENSSL); // Use openSSL/boringSSL TLS backend
        if (km != null)
        {
            builder.keyManager(km);
        }
        else
        {
            builder.keyManager(new ByteArrayInputStream(clientCert.getBytes(StandardCharsets.UTF_8)),
                               new ByteArrayInputStream(clientKey.getBytes(StandardCharsets.UTF_8)));
        }
        if (tm != null)
        {
            builder.trustManager(tm);
        }
        else
        {
            builder.trustManager(new ByteArrayInputStream(trustedCerts.getBytes(StandardCharsets.UTF_8)));
        }
        this.sslContext = builder.build();
    }

    @Override
    public void close() throws Exception
    {
        // Nothing to close
    }

    @Override
    public SslHandler newSslHandler(Channel channel,
                                    EndPoint remoteEndpoint)
    {
        final var remoteEpSocketAddress = remoteEndpoint.resolve();
        // TODO revisit endpoint verification
        safeLog.log(Lbl.DB_OVERLOAD, logger -> logger.info("Creating Cassandra TLS connection for remoteEndpoint: {}", remoteEpSocketAddress));

        final var remoteEp = (InetSocketAddress) remoteEpSocketAddress;
        final var peerHost = remoteEp.getHostString();
        final var peerPort = remoteEp.getPort();

        final var sslHandler = this.sslContext.newHandler(channel.alloc(), peerHost, peerPort); // Create new SSL Handler
        // Enable hostname verification
        if (verifyHost)
        {
            final var sslEngine = sslHandler.engine();
            final var sslParameters = sslEngine.getSSLParameters();
            sslParameters.setEndpointIdentificationAlgorithm("HTTPS");
            sslEngine.setSSLParameters(sslParameters);
        }

        return sslHandler;
    }

}
