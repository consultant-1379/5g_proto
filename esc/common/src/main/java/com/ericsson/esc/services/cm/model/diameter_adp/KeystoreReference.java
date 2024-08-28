
package com.ericsson.esc.services.cm.model.diameter_adp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Reference to a Diameter specific certificate and its associated private key
 * in the keystore, but only those printed by action show-deployed-certificates
 * can be referenced. TLS host name validation in Diameter Peer may require
 * using a certificate where included subject alternative name is matching the
 * originHost of the Diameter Own Node (see node origin-host). Update Effect:
 * All established Diameter Peer connections linked to related tls-profile are
 * dropped and reestablished with updated information.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "asymmetric-key", "certificate" })
public class KeystoreReference
{

    /**
     * A reference to an asymmetric key in the keystore.
     * 
     */
    @JsonProperty("asymmetric-key")
    @JsonPropertyDescription("A reference to an asymmetric key in the keystore.")
    private String asymmetricKey;
    /**
     * A reference to a specific certificate of the asymmetric key in the keystore.
     * 
     */
    @JsonProperty("certificate")
    @JsonPropertyDescription("A reference to a specific certificate of the asymmetric key in the keystore.")
    private String certificate;

    /**
     * A reference to an asymmetric key in the keystore.
     * 
     */
    @JsonProperty("asymmetric-key")
    public String getAsymmetricKey()
    {
        return asymmetricKey;
    }

    /**
     * A reference to an asymmetric key in the keystore.
     * 
     */
    @JsonProperty("asymmetric-key")
    public void setAsymmetricKey(String asymmetricKey)
    {
        this.asymmetricKey = asymmetricKey;
    }

    public KeystoreReference withAsymmetricKey(String asymmetricKey)
    {
        this.asymmetricKey = asymmetricKey;
        return this;
    }

    /**
     * A reference to a specific certificate of the asymmetric key in the keystore.
     * 
     */
    @JsonProperty("certificate")
    public String getCertificate()
    {
        return certificate;
    }

    /**
     * A reference to a specific certificate of the asymmetric key in the keystore.
     * 
     */
    @JsonProperty("certificate")
    public void setCertificate(String certificate)
    {
        this.certificate = certificate;
    }

    public KeystoreReference withCertificate(String certificate)
    {
        this.certificate = certificate;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(KeystoreReference.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("asymmetricKey");
        sb.append('=');
        sb.append(((this.asymmetricKey == null) ? "<null>" : this.asymmetricKey));
        sb.append(',');
        sb.append("certificate");
        sb.append('=');
        sb.append(((this.certificate == null) ? "<null>" : this.certificate));
        sb.append(',');
        if (sb.charAt((sb.length() - 1)) == ',')
        {
            sb.setCharAt((sb.length() - 1), ']');
        }
        else
        {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode()
    {
        int result = 1;
        result = ((result * 31) + ((this.certificate == null) ? 0 : this.certificate.hashCode()));
        result = ((result * 31) + ((this.asymmetricKey == null) ? 0 : this.asymmetricKey.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof KeystoreReference) == false)
        {
            return false;
        }
        KeystoreReference rhs = ((KeystoreReference) other);
        return (((this.certificate == rhs.certificate) || ((this.certificate != null) && this.certificate.equals(rhs.certificate)))
                && ((this.asymmetricKey == rhs.asymmetricKey) || ((this.asymmetricKey != null) && this.asymmetricKey.equals(rhs.asymmetricKey))));
    }

}
