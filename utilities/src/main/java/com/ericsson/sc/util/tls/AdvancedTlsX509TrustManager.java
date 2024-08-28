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
 * Created on: Oct 21, 2021
 *     Author: echfari
 */
package com.ericsson.sc.util.tls;

import java.io.IOException;
import java.net.Socket;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedTrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AdvancedTlsX509TrustManager is an {@code X509ExtendedTrustManager} that
 * allows users to configure advanced TLS features, such as root certificate
 * reloading, peer cert custom verification, etc.
 */
public final class AdvancedTlsX509TrustManager extends X509ExtendedTrustManager
{
    private static final Logger log = LoggerFactory.getLogger(AdvancedTlsX509TrustManager.class.getName());

    private final Verification verification;
    private final SslSocketAndEnginePeerVerifier socketAndEnginePeerVerifier;

    // The delegated trust manager used to perform traditional certificate
    // verification.
    private AtomicReference<X509ExtendedTrustManager> delegateManager = new AtomicReference<>();
    private final boolean suppressCertificateAuthoritiesList;

    private AdvancedTlsX509TrustManager(Verification verification,
                                        SslSocketAndEnginePeerVerifier socketAndEnginePeerVerifier,
                                        boolean suppressCertificateAuthoritiesList)
    {
        this.verification = verification;
        this.socketAndEnginePeerVerifier = socketAndEnginePeerVerifier;
        this.suppressCertificateAuthoritiesList = suppressCertificateAuthoritiesList;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain,
                                   String authType) throws CertificateException
    {
        throw new CertificateException("Not enough information to validate Client peer. SSLEngine or Socket required.");
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain,
                                   String authType,
                                   Socket socket) throws CertificateException
    {
        checkTrusted(chain, authType, null, socket, false);
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain,
                                   String authType,
                                   SSLEngine engine) throws CertificateException
    {
        checkTrusted(chain, authType, engine, null, false);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain,
                                   String authType,
                                   SSLEngine engine) throws CertificateException
    {
        checkTrusted(chain, authType, engine, null, true);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain,
                                   String authType) throws CertificateException
    {
        throw new CertificateException("Not enough information to validate Server peer. SSLEngine or Socket required.");
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain,
                                   String authType,
                                   Socket socket) throws CertificateException
    {
        checkTrusted(chain, authType, null, socket, true);
    }

    @Override
    public X509Certificate[] getAcceptedIssuers()
    {
        final var dm = this.delegateManager.get();
        // If CA suppression is enabled, an empty array shall be returned. This will
        // result in TLS handshake message not containing a list of accepted CAs.This
        // list is optional according TLS 1.2 and 1.3 spec
        return this.suppressCertificateAuthoritiesList || dm == null ? new X509Certificate[0] : dm.getAcceptedIssuers();
    }

    /**
     * Uses the default trust certificates stored on user's local system. After this
     * is used, functions that will provide new credential data(e.g.
     * updateTrustCredentials(), updateTrustCredentialsFromFile()) should not be
     * called.
     */
    public void useSystemDefaultTrustCerts() throws CertificateException, KeyStoreException, NoSuchAlgorithmException
    {
        this.delegateManager.set(createDelegateTrustManager(null));
    }

    /**
     * Updates the current cached trust certificates as well as the key store.
     *
     * @param trustCerts the trust certificates that are going to be used
     */
    public void updateTrustCredentials(List<X509Certificate> trustCerts) throws CertificateException, KeyStoreException, NoSuchAlgorithmException, IOException
    {
        final var keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        var i = 1;
        for (var cert : trustCerts)
        {
            final var alias = Integer.toString(i);
            keyStore.setCertificateEntry(alias, cert);
            i++;
        }
        final var newDelegateManager = createDelegateTrustManager(keyStore);
        this.delegateManager.set(newDelegateManager);
    }

    /**
     * 
     * @param keyStore The keystore to use, or null if system-default keystore is
     *                 desired
     * @return A newly created trust namanger
     */
    private static X509ExtendedTrustManager createDelegateTrustManager(KeyStore keyStore) throws CertificateException, KeyStoreException, NoSuchAlgorithmException
    {
        final var tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);
        final var tms = tmf.getTrustManagers();
        // Iterate over the returned trust managers, looking for an instance of
        // X509TrustManager.
        // If found, use that as the delegate trust manager.
        final var x509trustManager = Arrays.stream(tms) //
                                           .filter(X509ExtendedTrustManager.class::isInstance)
                                           .map(X509ExtendedTrustManager.class::cast)
                                           .findFirst();

        return x509trustManager.orElseThrow(() -> new CertificateException("Failed to find X509ExtendedTrustManager with default TrustManager algorithm "
                                                                           + TrustManagerFactory.getDefaultAlgorithm()));
    }

    private void checkTrusted(X509Certificate[] chain,
                              String authType,
                              SSLEngine sslEngine,
                              Socket socket,
                              boolean checkingServer) throws CertificateException
    {
        log.debug("Checking trusted certificate, isServer:{} ", checkingServer);
        if (chain == null || chain.length == 0)
        {
            throw new IllegalArgumentException("Want certificate verification but got null or empty certificates");
        }
        if (sslEngine == null && socket == null)
        {
            throw new CertificateException("Not enough information to validate peer. SSLEngine or Socket required.");
        }
        if (this.verification != Verification.INSECURELY_SKIP_ALL_VERIFICATION)
        {
            final var currentDelegateManager = this.delegateManager.get();
            if (currentDelegateManager == null)
            {
                throw new CertificateException("No trust roots configured");
            }
            if (checkingServer)
            {
                String algorithm = this.verification == Verification.CERTIFICATE_AND_HOST_NAME_VERIFICATION ? "HTTPS" : "";
                if (sslEngine != null)
                {
                    if (this.verification != Verification.DELEGATE)
                    {
                        final var sslParams = sslEngine.getSSLParameters();
                        sslParams.setEndpointIdentificationAlgorithm(algorithm);
                        sslEngine.setSSLParameters(sslParams);
                    }
                    currentDelegateManager.checkServerTrusted(chain, authType, sslEngine);
                }
                else
                {
                    if (this.verification != Verification.DELEGATE)
                    {
                        if (!(socket instanceof SSLSocket))
                        {
                            throw new CertificateException("socket is not a type of SSLSocket");
                        }
                        final var sslParams = ((SSLSocket) socket).getSSLParameters();
                        sslParams.setEndpointIdentificationAlgorithm(algorithm);
                        ((SSLSocket) socket).setSSLParameters(sslParams);
                    }
                    currentDelegateManager.checkServerTrusted(chain, authType, socket);
                }
            }
            else
            {
                currentDelegateManager.checkClientTrusted(chain, authType, sslEngine);
            }
        }
        // Perform the additional peer cert check.
        if (socketAndEnginePeerVerifier != null)
        {
            if (sslEngine != null)
            {
                socketAndEnginePeerVerifier.verifyPeerCertificate(chain, authType, sslEngine);
            }
            else
            {
                socketAndEnginePeerVerifier.verifyPeerCertificate(chain, authType, socket);
            }
        }
    }

    public static Builder newBuilder()
    {
        return new Builder();
    }

    // The verification mode when authenticating the peer certificate.
    public enum Verification
    {
        // This is the DEFAULT and RECOMMENDED mode for most applications.
        // Setting this on the client side will do the certificate and hostname
        // verification, while
        // setting this on the server side will only do the certificate verification.
        CERTIFICATE_AND_HOST_NAME_VERIFICATION,
        // This SHOULD be chosen only when you know what the implication this will
        // bring, and have a
        // basic understanding about TLS.
        // It SHOULD be accompanied with proper additional peer identity checks set
        // through
        // {@code PeerVerifier}(nit: why this @code not working?). Failing to do so will
        // leave
        // applications to MITM attack.
        // Also note that this will only take effect if the underlying SDK
        // implementation invokes
        // checkClientTrusted/checkServerTrusted with the {@code SSLEngine} parameter
        // while doing
        // verification.
        // Setting this on either side will only do the certificate verification.
        CERTIFICATE_ONLY_VERIFICATION,
        // Setting is very DANGEROUS. Please try to avoid this in a real production
        // environment, unless
        // you are a super advanced user intended to re-implement the whole verification
        // logic on your
        // own. A secure verification might include:
        // 1. proper verification on the peer certificate chain
        // 2. proper checks on the identity of the peer certificate
        INSECURELY_SKIP_ALL_VERIFICATION,

        DELEGATE
    }

    // Additional custom peer verification check.
    // It will be used when checkClientTrusted/checkServerTrusted is called with the
    // {@code Socket} or
    // the {@code SSLEngine} parameter.
    public interface SslSocketAndEnginePeerVerifier
    {
        /**
         * Verifies the peer certificate chain. For more information, please refer to
         * {@code X509ExtendedTrustManager}.
         *
         * @param peerCertChain the certificate chain sent from the peer
         * @param authType      the key exchange algorithm used, e.g. "RSA", "DHE_DSS",
         *                      etc
         * @param socket        the socket used for this connection. This parameter can
         *                      be null, which indicates that implementations need not
         *                      check the ssl parameters
         */
        void verifyPeerCertificate(X509Certificate[] peerCertChain,
                                   String authType,
                                   Socket socket) throws CertificateException;

        /**
         * Verifies the peer certificate chain. For more information, please refer to
         * {@code X509ExtendedTrustManager}.
         *
         * @param peerCertChain the certificate chain sent from the peer
         * @param authType      the key exchange algorithm used, e.g. "RSA", "DHE_DSS",
         *                      etc
         * @param engine        the engine used for this connection. This parameter can
         *                      be null, which indicates that implementations need not
         *                      check the ssl parameters
         */
        void verifyPeerCertificate(X509Certificate[] peerCertChain,
                                   String authType,
                                   SSLEngine engine) throws CertificateException;
    }

    public static final class Builder
    {

        private Verification verification = Verification.CERTIFICATE_AND_HOST_NAME_VERIFICATION;
        private SslSocketAndEnginePeerVerifier socketAndEnginePeerVerifier;
        private boolean suppressCertificateAuthoritiesList = true;

        private Builder()
        {
        }

        public Builder setVerification(Verification verification)
        {
            this.verification = verification;
            return this;
        }

        public Builder setSslSocketAndEnginePeerVerifier(SslSocketAndEnginePeerVerifier verifier)
        {
            this.socketAndEnginePeerVerifier = verifier;
            return this;
        }

        /**
         * Default value is true. Do not send certificate_authorities list in the
         * ClientHello/CertificateRequest messages during TLS handshake. According to
         * TLS spec this list is optional and may be omitted. The reason to enable
         * suppression is the fact that this list is cached and never updated, which
         * will result in TLS handshake failure if CA is changed.
         * 
         * https://datatracker.ietf.org/doc/html/rfc8446#section-4.2.4
         * 
         * @param enabled
         * @return
         */
        public Builder setSuppressCertificateAuthoritiesList(boolean enabled)
        {
            this.suppressCertificateAuthoritiesList = enabled;
            return this;
        }

        public AdvancedTlsX509TrustManager build()
        {
            return new AdvancedTlsX509TrustManager(this.verification, this.socketAndEnginePeerVerifier, this.suppressCertificateAuthoritiesList);
        }
    }
}
