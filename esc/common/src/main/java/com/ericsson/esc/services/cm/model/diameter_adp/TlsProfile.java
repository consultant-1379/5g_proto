
package com.ericsson.esc.services.cm.model.diameter_adp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id",
                     "keystore-reference",
                     "truststore-reference",
                     "tls-local-version",
                     "tls-1_2-local-cipher-list",
                     "tls-1_3-local-cipher-suites",
                     "security-level",
                     "user-label" })
public class TlsProfile
{

    /**
     * Used to specify the key of the tls-profile instance. (Required)
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("Used to specify the key of the tls-profile instance.")
    private String id;
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
    @JsonProperty("keystore-reference")
    @JsonPropertyDescription("Reference to a Diameter specific certificate and its associated private key in the keystore, but only those printed by action show-deployed-certificates can be referenced. TLS host name validation in Diameter Peer may require using a certificate where included subject alternative name is matching the originHost of the Diameter Own Node (see node origin-host). Update Effect: All established Diameter Peer connections linked to related tls-profile are dropped and reestablished with updated information.")
    private KeystoreReference keystoreReference;
    /**
     * Reference to a set of Diameter trust anchors that exists in the truststore,
     * but only those printed by action show-deployed-certificates can be
     * referenced. These optional trusts are used when authenticating the Peer.
     * Update Effect: All established Diameter Peer connections linked to related
     * tls-profile are dropped and reestablished with updated information.
     * 
     */
    @JsonProperty("truststore-reference")
    @JsonPropertyDescription("Reference to a set of Diameter trust anchors that exists in the truststore, but only those printed by action show-deployed-certificates can be referenced. These optional trusts are used when authenticating the Peer. Update Effect: All established Diameter Peer connections linked to related tls-profile are dropped and reestablished with updated information.")
    private String truststoreReference;
    /**
     * Specifies the supported TLS versions. Only TLS 1.2 or newer is allowed for
     * Diameter, while TLS 1.3 must always be supported. Hardening procedure can
     * switch to TLS 1.3 only. Update Effect: All established Diameter Peer
     * connections linked to related tls-profile are dropped and reestablished with
     * updated information.
     * 
     */
    @JsonProperty("tls-local-version")
    @JsonPropertyDescription("Specifies the supported TLS versions. Only TLS 1.2 or newer is allowed for Diameter, while TLS 1.3 must always be supported. Hardening procedure can switch to TLS 1.3 only. Update Effect: All established Diameter Peer connections linked to related tls-profile are dropped and reestablished with updated information.")
    private TlsProfile.TlsLocalVersion tlsLocalVersion = TlsProfile.TlsLocalVersion.fromValue("tls-1_2-tls-1_3");
    /**
     * Specifies the list of ciphers for TLS 1.2 based on ietf-tls-common. Due to
     * TLS 1.2 being the minimum supported protocol version, some of the hereby
     * listed ciphers can not be used. If a cipher excluded by RFC7540 is
     * configured, alarm DIA Diameter Transport Vulnerability is raised for the
     * related tls-profile. Chosen ciphers must match the deployed certificate.
     * Update Effect: All established Diameter Peer connections linked to related
     * tls-profile are dropped and reestablished with updated information.
     * 
     */
    @JsonProperty("tls-1_2-local-cipher-list")
    @JsonPropertyDescription("Specifies the list of ciphers for TLS 1.2 based on ietf-tls-common. Due to TLS 1.2 being the minimum supported protocol version, some of the hereby listed ciphers can not be used. If a cipher excluded by RFC7540 is configured, alarm DIA Diameter Transport Vulnerability is raised for the related tls-profile. Chosen ciphers must match the deployed certificate. Update Effect: All established Diameter Peer connections linked to related tls-profile are dropped and reestablished with updated information.")
    private List<Tls12LocalCipher> tls12LocalCipherList = new ArrayList<Tls12LocalCipher>();
    /**
     * Specifies the cipher suites for TLS 1.3 in order of preference. Update
     * Effect: All established Diameter Peer connections linked to related
     * tls-profile are dropped and reestablished with updated information.
     * 
     */
    @JsonProperty("tls-1_3-local-cipher-suites")
    @JsonPropertyDescription("Specifies the cipher suites for TLS 1.3 in order of preference. Update Effect: All established Diameter Peer connections linked to related tls-profile are dropped and reestablished with updated information.")
    private List<Tls13LocalCipherSuite> tls13LocalCipherSuites = new ArrayList<Tls13LocalCipherSuite>();
    /**
     * Specifies the TLS security level used with this profile. Hardening procedure
     * can increase the applied security level. Level 3 Security level set to 128
     * bits of security. As a result RSA, DSA and DH keys shorter than 3072 bits and
     * ECC keys shorter than 256 bits are prohibited. In addition to the level 2
     * exclusions cipher suites not offering forward secrecy are prohibited. TLS
     * versions below 1.1 are not permitted. Session tickets are disabled. Level 4
     * Security level set to 192 bits of security. As a result RSA, DSA and DH keys
     * shorter than 7680 bits and ECC keys shorter than 384 bits are prohibited.
     * Cipher suites using SHA1 for the MAC are prohibited. TLS versions below 1.2
     * are not permitted. Level 5 Security level set to 256 bits of security. As a
     * result RSA, DSA and DH keys shorter than 15360 bits and ECC keys shorter than
     * 512 bits are prohibited. Update Effect: All established Diameter Peer
     * connections linked to related tls-profile are dropped and reestablished with
     * updated information.
     * 
     */
    @JsonProperty("security-level")
    @JsonPropertyDescription("Specifies the TLS security level used with this profile. Hardening procedure can increase the applied security level. Level 3 Security level set to 128 bits of security. As a result RSA, DSA and DH keys shorter than 3072 bits and ECC keys shorter than 256 bits are prohibited. In addition to the level 2 exclusions cipher suites not offering forward secrecy are prohibited. TLS versions below 1.1 are not permitted. Session tickets are disabled. Level 4 Security level set to 192 bits of security. As a result RSA, DSA and DH keys shorter than 7680 bits and ECC keys shorter than 384 bits are prohibited. Cipher suites using SHA1 for the MAC are prohibited. TLS versions below 1.2 are not permitted. Level 5 Security level set to 256 bits of security. As a result RSA, DSA and DH keys shorter than 15360 bits and ECC keys shorter than 512 bits are prohibited. Update Effect: All established Diameter Peer connections linked to related tls-profile are dropped and reestablished with updated information.")
    private Integer securityLevel = 3;
    /**
     * Label for free use.
     * 
     */
    @JsonProperty("user-label")
    @JsonPropertyDescription("Label for free use.")
    private String userLabel;

    /**
     * Used to specify the key of the tls-profile instance. (Required)
     * 
     */
    @JsonProperty("id")
    public String getId()
    {
        return id;
    }

    /**
     * Used to specify the key of the tls-profile instance. (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(String id)
    {
        this.id = id;
    }

    public TlsProfile withId(String id)
    {
        this.id = id;
        return this;
    }

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
    @JsonProperty("keystore-reference")
    public KeystoreReference getKeystoreReference()
    {
        return keystoreReference;
    }

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
    @JsonProperty("keystore-reference")
    public void setKeystoreReference(KeystoreReference keystoreReference)
    {
        this.keystoreReference = keystoreReference;
    }

    public TlsProfile withKeystoreReference(KeystoreReference keystoreReference)
    {
        this.keystoreReference = keystoreReference;
        return this;
    }

    /**
     * Reference to a set of Diameter trust anchors that exists in the truststore,
     * but only those printed by action show-deployed-certificates can be
     * referenced. These optional trusts are used when authenticating the Peer.
     * Update Effect: All established Diameter Peer connections linked to related
     * tls-profile are dropped and reestablished with updated information.
     * 
     */
    @JsonProperty("truststore-reference")
    public String getTruststoreReference()
    {
        return truststoreReference;
    }

    /**
     * Reference to a set of Diameter trust anchors that exists in the truststore,
     * but only those printed by action show-deployed-certificates can be
     * referenced. These optional trusts are used when authenticating the Peer.
     * Update Effect: All established Diameter Peer connections linked to related
     * tls-profile are dropped and reestablished with updated information.
     * 
     */
    @JsonProperty("truststore-reference")
    public void setTruststoreReference(String truststoreReference)
    {
        this.truststoreReference = truststoreReference;
    }

    public TlsProfile withTruststoreReference(String truststoreReference)
    {
        this.truststoreReference = truststoreReference;
        return this;
    }

    /**
     * Specifies the supported TLS versions. Only TLS 1.2 or newer is allowed for
     * Diameter, while TLS 1.3 must always be supported. Hardening procedure can
     * switch to TLS 1.3 only. Update Effect: All established Diameter Peer
     * connections linked to related tls-profile are dropped and reestablished with
     * updated information.
     * 
     */
    @JsonProperty("tls-local-version")
    public TlsProfile.TlsLocalVersion getTlsLocalVersion()
    {
        return tlsLocalVersion;
    }

    /**
     * Specifies the supported TLS versions. Only TLS 1.2 or newer is allowed for
     * Diameter, while TLS 1.3 must always be supported. Hardening procedure can
     * switch to TLS 1.3 only. Update Effect: All established Diameter Peer
     * connections linked to related tls-profile are dropped and reestablished with
     * updated information.
     * 
     */
    @JsonProperty("tls-local-version")
    public void setTlsLocalVersion(TlsProfile.TlsLocalVersion tlsLocalVersion)
    {
        this.tlsLocalVersion = tlsLocalVersion;
    }

    public TlsProfile withTlsLocalVersion(TlsProfile.TlsLocalVersion tlsLocalVersion)
    {
        this.tlsLocalVersion = tlsLocalVersion;
        return this;
    }

    /**
     * Specifies the list of ciphers for TLS 1.2 based on ietf-tls-common. Due to
     * TLS 1.2 being the minimum supported protocol version, some of the hereby
     * listed ciphers can not be used. If a cipher excluded by RFC7540 is
     * configured, alarm DIA Diameter Transport Vulnerability is raised for the
     * related tls-profile. Chosen ciphers must match the deployed certificate.
     * Update Effect: All established Diameter Peer connections linked to related
     * tls-profile are dropped and reestablished with updated information.
     * 
     */
    @JsonProperty("tls-1_2-local-cipher-list")
    public List<Tls12LocalCipher> getTls12LocalCipherList()
    {
        return tls12LocalCipherList;
    }

    /**
     * Specifies the list of ciphers for TLS 1.2 based on ietf-tls-common. Due to
     * TLS 1.2 being the minimum supported protocol version, some of the hereby
     * listed ciphers can not be used. If a cipher excluded by RFC7540 is
     * configured, alarm DIA Diameter Transport Vulnerability is raised for the
     * related tls-profile. Chosen ciphers must match the deployed certificate.
     * Update Effect: All established Diameter Peer connections linked to related
     * tls-profile are dropped and reestablished with updated information.
     * 
     */
    @JsonProperty("tls-1_2-local-cipher-list")
    public void setTls12LocalCipherList(List<Tls12LocalCipher> tls12LocalCipherList)
    {
        this.tls12LocalCipherList = tls12LocalCipherList;
    }

    public TlsProfile withTls12LocalCipherList(List<Tls12LocalCipher> tls12LocalCipherList)
    {
        this.tls12LocalCipherList = tls12LocalCipherList;
        return this;
    }

    /**
     * Specifies the cipher suites for TLS 1.3 in order of preference. Update
     * Effect: All established Diameter Peer connections linked to related
     * tls-profile are dropped and reestablished with updated information.
     * 
     */
    @JsonProperty("tls-1_3-local-cipher-suites")
    public List<Tls13LocalCipherSuite> getTls13LocalCipherSuites()
    {
        return tls13LocalCipherSuites;
    }

    /**
     * Specifies the cipher suites for TLS 1.3 in order of preference. Update
     * Effect: All established Diameter Peer connections linked to related
     * tls-profile are dropped and reestablished with updated information.
     * 
     */
    @JsonProperty("tls-1_3-local-cipher-suites")
    public void setTls13LocalCipherSuites(List<Tls13LocalCipherSuite> tls13LocalCipherSuites)
    {
        this.tls13LocalCipherSuites = tls13LocalCipherSuites;
    }

    public TlsProfile withTls13LocalCipherSuites(List<Tls13LocalCipherSuite> tls13LocalCipherSuites)
    {
        this.tls13LocalCipherSuites = tls13LocalCipherSuites;
        return this;
    }

    /**
     * Specifies the TLS security level used with this profile. Hardening procedure
     * can increase the applied security level. Level 3 Security level set to 128
     * bits of security. As a result RSA, DSA and DH keys shorter than 3072 bits and
     * ECC keys shorter than 256 bits are prohibited. In addition to the level 2
     * exclusions cipher suites not offering forward secrecy are prohibited. TLS
     * versions below 1.1 are not permitted. Session tickets are disabled. Level 4
     * Security level set to 192 bits of security. As a result RSA, DSA and DH keys
     * shorter than 7680 bits and ECC keys shorter than 384 bits are prohibited.
     * Cipher suites using SHA1 for the MAC are prohibited. TLS versions below 1.2
     * are not permitted. Level 5 Security level set to 256 bits of security. As a
     * result RSA, DSA and DH keys shorter than 15360 bits and ECC keys shorter than
     * 512 bits are prohibited. Update Effect: All established Diameter Peer
     * connections linked to related tls-profile are dropped and reestablished with
     * updated information.
     * 
     */
    @JsonProperty("security-level")
    public Integer getSecurityLevel()
    {
        return securityLevel;
    }

    /**
     * Specifies the TLS security level used with this profile. Hardening procedure
     * can increase the applied security level. Level 3 Security level set to 128
     * bits of security. As a result RSA, DSA and DH keys shorter than 3072 bits and
     * ECC keys shorter than 256 bits are prohibited. In addition to the level 2
     * exclusions cipher suites not offering forward secrecy are prohibited. TLS
     * versions below 1.1 are not permitted. Session tickets are disabled. Level 4
     * Security level set to 192 bits of security. As a result RSA, DSA and DH keys
     * shorter than 7680 bits and ECC keys shorter than 384 bits are prohibited.
     * Cipher suites using SHA1 for the MAC are prohibited. TLS versions below 1.2
     * are not permitted. Level 5 Security level set to 256 bits of security. As a
     * result RSA, DSA and DH keys shorter than 15360 bits and ECC keys shorter than
     * 512 bits are prohibited. Update Effect: All established Diameter Peer
     * connections linked to related tls-profile are dropped and reestablished with
     * updated information.
     * 
     */
    @JsonProperty("security-level")
    public void setSecurityLevel(Integer securityLevel)
    {
        this.securityLevel = securityLevel;
    }

    public TlsProfile withSecurityLevel(Integer securityLevel)
    {
        this.securityLevel = securityLevel;
        return this;
    }

    /**
     * Label for free use.
     * 
     */
    @JsonProperty("user-label")
    public String getUserLabel()
    {
        return userLabel;
    }

    /**
     * Label for free use.
     * 
     */
    @JsonProperty("user-label")
    public void setUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
    }

    public TlsProfile withUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(TlsProfile.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null) ? "<null>" : this.id));
        sb.append(',');
        sb.append("keystoreReference");
        sb.append('=');
        sb.append(((this.keystoreReference == null) ? "<null>" : this.keystoreReference));
        sb.append(',');
        sb.append("truststoreReference");
        sb.append('=');
        sb.append(((this.truststoreReference == null) ? "<null>" : this.truststoreReference));
        sb.append(',');
        sb.append("tlsLocalVersion");
        sb.append('=');
        sb.append(((this.tlsLocalVersion == null) ? "<null>" : this.tlsLocalVersion));
        sb.append(',');
        sb.append("tls12LocalCipherList");
        sb.append('=');
        sb.append(((this.tls12LocalCipherList == null) ? "<null>" : this.tls12LocalCipherList));
        sb.append(',');
        sb.append("tls13LocalCipherSuites");
        sb.append('=');
        sb.append(((this.tls13LocalCipherSuites == null) ? "<null>" : this.tls13LocalCipherSuites));
        sb.append(',');
        sb.append("securityLevel");
        sb.append('=');
        sb.append(((this.securityLevel == null) ? "<null>" : this.securityLevel));
        sb.append(',');
        sb.append("userLabel");
        sb.append('=');
        sb.append(((this.userLabel == null) ? "<null>" : this.userLabel));
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
        result = ((result * 31) + ((this.securityLevel == null) ? 0 : this.securityLevel.hashCode()));
        result = ((result * 31) + ((this.userLabel == null) ? 0 : this.userLabel.hashCode()));
        result = ((result * 31) + ((this.truststoreReference == null) ? 0 : this.truststoreReference.hashCode()));
        result = ((result * 31) + ((this.tls12LocalCipherList == null) ? 0 : this.tls12LocalCipherList.hashCode()));
        result = ((result * 31) + ((this.tls13LocalCipherSuites == null) ? 0 : this.tls13LocalCipherSuites.hashCode()));
        result = ((result * 31) + ((this.tlsLocalVersion == null) ? 0 : this.tlsLocalVersion.hashCode()));
        result = ((result * 31) + ((this.id == null) ? 0 : this.id.hashCode()));
        result = ((result * 31) + ((this.keystoreReference == null) ? 0 : this.keystoreReference.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof TlsProfile) == false)
        {
            return false;
        }
        TlsProfile rhs = ((TlsProfile) other);
        return (((((((((this.securityLevel == rhs.securityLevel) || ((this.securityLevel != null) && this.securityLevel.equals(rhs.securityLevel)))
                      && ((this.userLabel == rhs.userLabel) || ((this.userLabel != null) && this.userLabel.equals(rhs.userLabel))))
                     && ((this.truststoreReference == rhs.truststoreReference)
                         || ((this.truststoreReference != null) && this.truststoreReference.equals(rhs.truststoreReference))))
                    && ((this.tls12LocalCipherList == rhs.tls12LocalCipherList)
                        || ((this.tls12LocalCipherList != null) && this.tls12LocalCipherList.equals(rhs.tls12LocalCipherList))))
                   && ((this.tls13LocalCipherSuites == rhs.tls13LocalCipherSuites)
                       || ((this.tls13LocalCipherSuites != null) && this.tls13LocalCipherSuites.equals(rhs.tls13LocalCipherSuites))))
                  && ((this.tlsLocalVersion == rhs.tlsLocalVersion) || ((this.tlsLocalVersion != null) && this.tlsLocalVersion.equals(rhs.tlsLocalVersion))))
                 && ((this.id == rhs.id) || ((this.id != null) && this.id.equals(rhs.id))))
                && ((this.keystoreReference == rhs.keystoreReference)
                    || ((this.keystoreReference != null) && this.keystoreReference.equals(rhs.keystoreReference))));
    }

    public enum TlsLocalVersion
    {

        TLS_1_2_TLS_1_3("tls-1_2-tls-1_3"),
        TLS_1_3_ONLY("tls-1_3-only");

        private final String value;
        private final static Map<String, TlsProfile.TlsLocalVersion> CONSTANTS = new HashMap<String, TlsProfile.TlsLocalVersion>();

        static
        {
            for (TlsProfile.TlsLocalVersion c : values())
            {
                CONSTANTS.put(c.value, c);
            }
        }

        private TlsLocalVersion(String value)
        {
            this.value = value;
        }

        @Override
        public String toString()
        {
            return this.value;
        }

        @JsonValue
        public String value()
        {
            return this.value;
        }

        @JsonCreator
        public static TlsProfile.TlsLocalVersion fromValue(String value)
        {
            TlsProfile.TlsLocalVersion constant = CONSTANTS.get(value);
            if (constant == null)
            {
                throw new IllegalArgumentException(value);
            }
            else
            {
                return constant;
            }
        }

    }

}
