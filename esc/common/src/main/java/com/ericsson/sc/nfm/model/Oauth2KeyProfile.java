
package com.ericsson.sc.nfm.model;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "key-id", "type", "alg", "value", "json-body" })
public class Oauth2KeyProfile
{

    /**
     * The identifier of the oAuth2 key (Required)
     * 
     */
    @JsonProperty("key-id")
    @JsonPropertyDescription("The identifier of the oAuth2 key")
    private String keyId;
    /**
     * The type of the oAuth2 key profile
     * 
     */
    @JsonProperty("type")
    @JsonPropertyDescription("The type of the oAuth2 key profile")
    private Oauth2KeyProfile.Type type;
    /**
     * The encryption algorithm of the oAuth2 key profile. This parameter is
     * necessary for JWK transformation
     * 
     */
    @JsonProperty("alg")
    @JsonPropertyDescription("The encryption algorithm of the oAuth2 key profile. This parameter is necessary for JWK transformation")
    private Oauth2KeyProfile.Alg alg;
    /**
     * The value of the oAuth2 key profile
     * 
     */
    @JsonProperty("value")
    @JsonPropertyDescription("The value of the oAuth2 key profile")
    private String value;
    /**
     * The json body of the oAuth2 key profile
     * 
     */
    @JsonProperty("json-body")
    @JsonPropertyDescription("The json body of the oAuth2 key profile")
    private String jsonBody;

    /**
     * The identifier of the oAuth2 key (Required)
     * 
     */
    @JsonProperty("key-id")
    public String getKeyId()
    {
        return keyId;
    }

    /**
     * The identifier of the oAuth2 key (Required)
     * 
     */
    @JsonProperty("key-id")
    public void setKeyId(String keyId)
    {
        this.keyId = keyId;
    }

    public Oauth2KeyProfile withKeyId(String keyId)
    {
        this.keyId = keyId;
        return this;
    }

    /**
     * The type of the oAuth2 key profile
     * 
     */
    @JsonProperty("type")
    public Oauth2KeyProfile.Type getType()
    {
        return type;
    }

    /**
     * The type of the oAuth2 key profile
     * 
     */
    @JsonProperty("type")
    public void setType(Oauth2KeyProfile.Type type)
    {
        this.type = type;
    }

    public Oauth2KeyProfile withType(Oauth2KeyProfile.Type type)
    {
        this.type = type;
        return this;
    }

    /**
     * The encryption algorithm of the oAuth2 key profile. This parameter is
     * necessary for JWK transformation
     * 
     */
    @JsonProperty("alg")
    public Oauth2KeyProfile.Alg getAlg()
    {
        return alg;
    }

    /**
     * The encryption algorithm of the oAuth2 key profile. This parameter is
     * necessary for JWK transformation
     * 
     */
    @JsonProperty("alg")
    public void setAlg(Oauth2KeyProfile.Alg alg)
    {
        this.alg = alg;
    }

    public Oauth2KeyProfile withAlg(Oauth2KeyProfile.Alg alg)
    {
        this.alg = alg;
        return this;
    }

    /**
     * The value of the oAuth2 key profile
     * 
     */
    @JsonProperty("value")
    public String getValue()
    {
        return value;
    }

    /**
     * The value of the oAuth2 key profile
     * 
     */
    @JsonProperty("value")
    public void setValue(String value)
    {
        this.value = value;
    }

    public Oauth2KeyProfile withValue(String value)
    {
        this.value = value;
        return this;
    }

    /**
     * The json body of the oAuth2 key profile
     * 
     */
    @JsonProperty("json-body")
    public String getJsonBody()
    {
        return jsonBody;
    }

    /**
     * The json body of the oAuth2 key profile
     * 
     */
    @JsonProperty("json-body")
    public void setJsonBody(String jsonBody)
    {
        this.jsonBody = jsonBody;
    }

    public Oauth2KeyProfile withJsonBody(String jsonBody)
    {
        this.jsonBody = jsonBody;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(Oauth2KeyProfile.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("keyId");
        sb.append('=');
        sb.append(((this.keyId == null) ? "<null>" : this.keyId));
        sb.append(',');
        sb.append("type");
        sb.append('=');
        sb.append(((this.type == null) ? "<null>" : this.type));
        sb.append(',');
        sb.append("alg");
        sb.append('=');
        sb.append(((this.alg == null) ? "<null>" : this.alg));
        sb.append(',');
        sb.append("value");
        sb.append('=');
        sb.append(((this.value == null) ? "<null>" : this.value));
        sb.append(',');
        sb.append("jsonBody");
        sb.append('=');
        sb.append(((this.jsonBody == null) ? "<null>" : this.jsonBody));
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
        result = ((result * 31) + ((this.jsonBody == null) ? 0 : this.jsonBody.hashCode()));
        result = ((result * 31) + ((this.alg == null) ? 0 : this.alg.hashCode()));
        result = ((result * 31) + ((this.type == null) ? 0 : this.type.hashCode()));
        result = ((result * 31) + ((this.value == null) ? 0 : this.value.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof Oauth2KeyProfile) == false)
        {
            return false;
        }
        Oauth2KeyProfile rhs = ((Oauth2KeyProfile) other);
        return (((((this.keyId == rhs.keyId) || ((this.keyId != null) && this.keyId.equals(rhs.keyId)))
                  && ((this.jsonBody == rhs.jsonBody) || ((this.jsonBody != null) && this.jsonBody.equals(rhs.jsonBody))))
                 && ((this.type == rhs.type) || ((this.type != null) && this.type.equals(rhs.type))))
                && ((this.alg == rhs.alg) || ((this.alg != null) && this.alg.equals(rhs.alg)))
                && ((this.value == rhs.value) || ((this.value != null) && this.value.equals(rhs.value))));
    }

    public enum Alg
    {

        HS_256("HS256"),
        HS_384("HS384"),
        HS_512("HS512"),
        RS_256("RS256"),
        RS_384("RS384"),
        RS_512("RS512"),
        ES_256("ES256"),
        ES_256_K("ES256K"),
        ES_384("ES384"),
        ES_512("ES512"),
        PS_256("PS256"),
        PS_384("PS384"),
        PS_512("PS512"),
        ED_DSA("EdDSA");

        private final String value;
        private final static Map<String, Oauth2KeyProfile.Alg> CONSTANTS = new HashMap<String, Oauth2KeyProfile.Alg>();

        static
        {
            for (Oauth2KeyProfile.Alg c : values())
            {
                CONSTANTS.put(c.value, c);
            }
        }

        private Alg(String value)
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
        public static Oauth2KeyProfile.Alg fromValue(String value)
        {
            Oauth2KeyProfile.Alg constant = CONSTANTS.get(value);
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

    public enum Type
    {

        JWK("jwk"),
        PEM("pem");

        private final String value;
        private final static Map<String, Oauth2KeyProfile.Type> CONSTANTS = new HashMap<String, Oauth2KeyProfile.Type>();

        static
        {
            for (Oauth2KeyProfile.Type c : values())
            {
                CONSTANTS.put(c.value, c);
            }
        }

        private Type(String value)
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
        public static Oauth2KeyProfile.Type fromValue(String value)
        {
            Oauth2KeyProfile.Type constant = CONSTANTS.get(value);
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
