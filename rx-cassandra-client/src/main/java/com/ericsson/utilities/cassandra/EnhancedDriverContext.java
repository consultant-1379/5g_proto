package com.ericsson.utilities.cassandra;

import java.util.Objects;
import java.util.Optional;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;

import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.session.ProgrammaticArguments;
import com.datastax.oss.driver.internal.core.context.DefaultDriverContext;
import com.datastax.oss.driver.internal.core.ssl.SslHandlerFactory;

/**
 * A Custom Cassandra driver context, that supports additional configuration
 * options.
 * 
 * @see #EnhancedDriverOption
 */
public class EnhancedDriverContext extends DefaultDriverContext
{
    private final boolean tlsEnabled;
    private final String trusedCerts;
    private final String clientCert;
    private final String clientKey;
    private final boolean verifyHost;
    private final KeyManager km;
    private final TrustManager tm;

    public EnhancedDriverContext(DriverConfigLoader configLoader,
                                 ProgrammaticArguments programmaticArguments,
                                 KeyManager km,
                                 TrustManager tm)
    {
        super(configLoader, programmaticArguments);
        final var cfg = configLoader.getInitialConfig().getDefaultProfile();
        this.tlsEnabled = cfg.getBoolean(EnhancedDriverOption.TLS_ENABLED, false);

        this.trusedCerts = cfg.getString(EnhancedDriverOption.TRUSTED_CERTS, null);
        this.clientCert = cfg.getString(EnhancedDriverOption.CLIENT_CERT, null);
        this.clientKey = cfg.getString(EnhancedDriverOption.CLIENT_KEY, null);
        this.verifyHost = cfg.getBoolean(EnhancedDriverOption.VERIFY_HOST, true);
        this.km = km;
        this.tm = tm;

        if (tlsEnabled)
        {
            if (km == null)
            {
                Objects.requireNonNull(this.clientCert);
                Objects.requireNonNull(this.clientKey);
            }
            if (tm == null)
            {
                Objects.requireNonNull(this.trusedCerts);
            }
        }
    }

    @Override
    protected Optional<SslHandlerFactory> buildSslHandlerFactory()
    {
        try
        {
            return this.tlsEnabled ? Optional.of(new NettySslHandlerFactory(trusedCerts, verifyHost, clientCert, clientKey, km, tm))
                                   : super.buildSslHandlerFactory();
        }
        catch (SSLException e)
        {
            throw new IllegalArgumentException("Failed to configure Cassandra driver for TLS", e);
        }

    }
}