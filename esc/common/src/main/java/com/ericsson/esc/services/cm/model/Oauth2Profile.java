
package com.ericsson.esc.services.cm.model;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "key-id", "algorithm", "public-key" })
public class Oauth2Profile
{

    /**
     * The identifier of the OAuth2.0 key (Required)
     * 
     */
    @JsonProperty("key-id")
    @JsonPropertyDescription("The identifier of the OAuth2.0 key")
    private String keyId;
    /**
     * Name of the algorithm to be used for OAuth2 (Required)
     * 
     */
    @JsonProperty("algorithm")
    @JsonPropertyDescription("Name of the algorithm to be used for OAuth2")
    private Oauth2Profile.Algorithm algorithm;
    /**
     * Public key for the specified algorithm (Required)
     * 
     */
    @JsonProperty("public-key")
    @JsonPropertyDescription("Public key for the specified algorithm")
    private String publicKey;

    /**
     * The identifier of the OAuth2.0 key (Required)
     * 
     */
    @JsonProperty("key-id")
    public String getKeyId()
    {
        return keyId;
    }

    /**
     * The identifier of the OAuth2.0 key (Required)
     * 
     */
    @JsonProperty("key-id")
    public void setKeyId(String keyId)
    {
        this.keyId = keyId;
    }

    public Oauth2Profile withKeyId(String keyId)
    {
        this.keyId = keyId;
        return this;
    }

    /**
     * Name of the algorithm to be used for OAuth2 (Required)
     * 
     */
    @JsonProperty("algorithm")
    public Oauth2Profile.Algorithm getAlgorithm()
    {
        return algorithm;
    }

    /**
     * Name of the algorithm to be used for OAuth2 (Required)
     * 
     */
    @JsonProperty("algorithm")
    public void setAlgorithm(Oauth2Profile.Algorithm algorithm)
    {
        this.algorithm = algorithm;
    }

    public Oauth2Profile withAlgorithm(Oauth2Profile.Algorithm algorithm)
    {
        this.algorithm = algorithm;
        return this;
    }

    /**
     * Public key for the specified algorithm (Required)
     * 
     */
    @JsonProperty("public-key")
    public String getPublicKey()
    {
        return publicKey;
    }

    /**
     * Public key for the specified algorithm (Required)
     * 
     */
    @JsonProperty("public-key")
    public void setPublicKey(String publicKey)
    {
        this.publicKey = publicKey;
    }

    public Oauth2Profile withPublicKey(String publicKey)
    {
        this.publicKey = publicKey;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(Oauth2Profile.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("keyId");
        sb.append('=');
        sb.append(((this.keyId == null) ? "<null>" : this.keyId));
        sb.append(',');
        sb.append("algorithm");
        sb.append('=');
        sb.append(((this.algorithm == null) ? "<null>" : this.algorithm));
        sb.append(',');
        sb.append("publicKey");
        sb.append('=');
        sb.append(((this.publicKey == null) ? "<null>" : this.publicKey));
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
        result = ((result * 31) + ((this.keyId == null) ? 0 : this.keyId.hashCode()));
        result = ((result * 31) + ((this.publicKey == null) ? 0 : this.publicKey.hashCode()));
        result = ((result * 31) + ((this.algorithm == null) ? 0 : this.algorithm.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof Oauth2Profile) == false)
        {
            return false;
        }
        Oauth2Profile rhs = ((Oauth2Profile) other);
        return ((((this.keyId == rhs.keyId) || ((this.keyId != null) && this.keyId.equals(rhs.keyId)))
                 && ((this.publicKey == rhs.publicKey) || ((this.publicKey != null) && this.publicKey.equals(rhs.publicKey))))
                && ((this.algorithm == rhs.algorithm) || ((this.algorithm != null) && this.algorithm.equals(rhs.algorithm))));
    }

    public enum Algorithm
    {

        ES_256("ES256");

        private final String value;
        private final static Map<String, Oauth2Profile.Algorithm> CONSTANTS = new HashMap<String, Oauth2Profile.Algorithm>();

        static
        {
            for (Oauth2Profile.Algorithm c : values())
            {
                CONSTANTS.put(c.value, c);
            }
        }

        private Algorithm(String value)
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
        public static Oauth2Profile.Algorithm fromValue(String value)
        {
            Oauth2Profile.Algorithm constant = CONSTANTS.get(value);
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
