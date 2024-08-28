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

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedKeyManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AdvancedTlsX509KeyManager is an {@code X509ExtendedKeyManager} that allows
 * users to configure advanced TLS features, such as private key and certificate
 * chain reloading, etc.
 */
public final class AdvancedTlsX509KeyManager extends X509ExtendedKeyManager
{
    private static final Logger log = LoggerFactory.getLogger(AdvancedTlsX509KeyManager.class.getName());
    private static final String ALIAS = "default";

    // The credential information sent to peers to prove our identity.
    private AtomicReference<KeyInfo> keyInfo = new AtomicReference<>();

    /**
     * Constructs an AdvancedTlsX509KeyManager.
     */
    public AdvancedTlsX509KeyManager()
    {
        // Override base default constructor
    }

    @Override
    public PrivateKey getPrivateKey(String alias)
    {
        if (alias.equals(ALIAS))
        {
            final var ki = this.keyInfo.get();
            return ki.key;
        }
        else
        {
            log.debug("Wrong alias {}", alias);
            return null;
        }
    }

    @Override
    public X509Certificate[] getCertificateChain(String alias)
    {
        if (alias.equals(ALIAS))
        {
            final var ki = this.keyInfo.get();
            if (ki != null)
            {
                // TODO use clone()
                log.debug("Requested certificate");
                return Arrays.copyOf(ki.certs, ki.certs.length);
            }
            else
            {
                log.debug("Requested certificate not yet available");
                return null;
            }
        }
        else
        {
            log.debug("Unknown alias {}", alias);
            return null;
        }
    }

    @Override
    public String[] getClientAliases(String keyType,
                                     Principal[] issuers)
    {
        return new String[] { ALIAS };
    }

    @Override
    public String chooseClientAlias(String[] keyType,
                                    Principal[] issuers,
                                    Socket socket)
    {
        return ALIAS;
    }

    @Override
    public String chooseEngineClientAlias(String[] keyType,
                                          Principal[] issuers,
                                          SSLEngine engine)
    {
        return ALIAS;
    }

    @Override
    public String[] getServerAliases(String keyType,
                                     Principal[] issuers)
    {
        return new String[] { ALIAS };
    }

    @Override
    public String chooseServerAlias(String keyType,
                                    Principal[] issuers,
                                    Socket socket)
    {
        return ALIAS;
    }

    @Override
    public String chooseEngineServerAlias(String keyType,
                                          Principal[] issuers,
                                          SSLEngine engine)
    {
        return ALIAS;
    }

    /**
     * Updates the current cached private key and cert chains.
     *
     * @param key   the private key that is going to be used
     * @param certs the certificate chain that is going to be used
     */
    public void updateIdentityCredentials(PrivateKey key,
                                          X509Certificate[] certs)
    {
        this.keyInfo.set(new KeyInfo(checkNotNull(key, "key"), checkNotNull(certs, "certs")));
    }

    private static class KeyInfo
    {
        // The private key and the cert chain we will use to send to peers to prove our
        // identity.
        final PrivateKey key;
        final X509Certificate[] certs;

        public KeyInfo(PrivateKey key,
                       X509Certificate[] certs)
        {
            this.key = key;
            this.certs = certs;
        }
    }
}
