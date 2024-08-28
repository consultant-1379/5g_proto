/**
 * COPYRIGHT ERICSSON GMBH 2021
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jul 12, 2021
 *     Author: ekilagg
 */

package com.ericsson.esc.bsf.load.configuration;

import java.util.List;

import com.ericsson.esc.bsf.load.server.InvalidParameter;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Defines the configuration of TLS traffic.
 */
@JsonDeserialize(builder = TlsConfiguration.Builder.class)
public class TlsConfiguration
{
    private Boolean enabled;
    private String key;
    private String cert;
    private String certPath;
    private String keyPath;
    private Boolean verifyHost;

    private TlsConfiguration(Builder builder)
    {
        this.enabled = builder.enabled;
        this.key = builder.key;
        this.cert = builder.cert;
        this.certPath = builder.certPath;
        this.keyPath = builder.keyPath;
        this.verifyHost = builder.verifyHost;
    }

    public List<InvalidParameter> validate()
    {
        final var cv = new ConfigurationValidator();

        cv.checkNonNull(enabled, "enabled", "Parameter 'tls-enabled' must not be null");
        cv.checkNonNull(verifyHost, "verify-host", "Parameter 'verify-host' must not be null");
        if (key == null && cert == null)
        {
            cv.checkNonNull(keyPath, "key-path", "Parameter 'key-path or key' must not be null");
            cv.checkNonNull(certPath, "cert-path", "Parameter 'cert-path or cert' must not be null");
        }

        return cv.getInvalidParam();
    }

    /**
     * Get a boolean value that indicates that TLS is enabled.
     * 
     * @return the tlsEnabled
     */
    @JsonGetter("enabled")
    public Boolean getEnabled()
    {
        return enabled;
    }

    /**
     * Get the private key.
     * 
     * @return the key
     */
    @JsonGetter("private-key")
    public String getKey()
    {
        return key;
    }

    /**
     * Get the certificate.
     * 
     * @return the cert
     */
    @JsonGetter("certificate")
    public String getCert()
    {
        return cert;
    }

    /**
     * Get a boolean value that indicates that host verification is enabled.
     * 
     * @return the verifyHost
     */
    @JsonGetter("verify-host")
    public Boolean getVerifyHost()
    {
        return verifyHost;
    }

    /**
     * Get the TLS certificate path.
     * 
     * @return the certPath
     */
    @JsonGetter("cert-path")
    public String getCertPath()
    {
        return certPath;
    }

    /**
     * Get the TLS private key path.
     * 
     * @return the keyPath
     */
    @JsonGetter("key-path")
    public String getKeyPath()
    {
        return keyPath;
    }

    @Override
    public String toString()
    {
        return "TlsConfiguration [enabled=" + enabled + ", key=" + key + ", cert=" + cert + ", certPath=" + certPath + ", keyPath=" + keyPath + ", verifyHost="
               + verifyHost + "]";
    }

    @Override
    public int hashCode()
    {
        final var prime = 31;
        var result = 1;
        result = prime * result + ((enabled == null) ? 0 : enabled.hashCode());
        result = prime * result + ((verifyHost == null) ? 0 : verifyHost.hashCode());
        result = prime * result + ((certPath == null) ? 0 : certPath.hashCode());
        result = prime * result + ((keyPath == null) ? 0 : keyPath.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TlsConfiguration other = (TlsConfiguration) obj;
        if (enabled == null)
        {
            if (other.enabled != null)
                return false;
        }
        else if (!enabled.equals(other.enabled))
            return false;
        if (verifyHost == null)
        {
            if (other.verifyHost != null)
                return false;
        }
        else if (!verifyHost.equals(other.verifyHost))
            return false;
        if (certPath == null)
        {
            if (other.certPath != null)
                return false;
        }
        else if (!certPath.equals(other.certPath))
            return false;
        if (keyPath == null)
        {
            if (other.keyPath != null)
                return false;
        }
        else if (!keyPath.equals(other.keyPath))
            return false;
        return true;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder
    {
        @JsonProperty("enabled")
        private Boolean enabled = false;
        @JsonProperty("private-key")
        private String key = null;
        @JsonProperty("certificate")
        private String cert = null;
        @JsonProperty("cert-path")
        private String certPath = "/opt/bsf-load/certificates/cert.pem";
        @JsonProperty("key-path")
        private String keyPath = "/opt/bsf-load/certificates/key.pem";
        @JsonProperty("verify-host")
        private Boolean verifyHost = false;

        /**
         * Enable or disable TLS communication.
         * 
         * @param tlsEnabled Set to true to enable TLS, false otherwise.
         * @return Builder The builder.
         */
        public Builder enabled(Boolean tlsEnabled)
        {
            this.enabled = tlsEnabled;
            return this;
        }

        /**
         * Enable or disable host verification.
         * 
         * @param verifyHost Set to true to enable host verification, false otherwise.
         * @return Builder The builder.
         */
        public Builder verifyHost(Boolean verifyHost)
        {
            this.verifyHost = verifyHost;
            return this;
        }

        /**
         * Set the path of the TLS certificate file.
         * 
         * @param certPath The full path of the certificate file.
         * @return Builder The builder.
         */
        public Builder certPath(String certPath)
        {
            this.certPath = certPath;
            return this;
        }

        /**
         * Set the TLS private key.
         * 
         * @param key The key.
         * @return Builder The builder.
         */
        public Builder key(String key)
        {
            this.key = key;
            return this;
        }

        /**
         * Set the TLS certificate.
         * 
         * @param cert The certificate.
         * @return Builder The builder.
         */
        public Builder cert(String cert)
        {
            this.cert = cert;
            return this;
        }

        /**
         * Set the path of the TLS private key file.
         * 
         * @param keyPath The full path of the key file.
         * @return Builder The builder.
         */
        public Builder keyPath(String keyPath)
        {
            this.keyPath = keyPath;
            return this;
        }

        /**
         * Create the TlsConfiguration object.
         * 
         * @return TlsConfiguration The configuration options for TLS.
         */
        public TlsConfiguration build()
        {
            return new TlsConfiguration(this);
        }
    }
}
