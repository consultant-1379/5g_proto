
package com.ericsson.sc.sepp.model;

import com.ericsson.utilities.common.IfNamedListItem;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "asymmetric-key", "certificate" })
public class AsymKey implements IfNamedListItem
{

    /**
     * The name that uniquely identifies the pair of an asymmetric-key and a
     * certificate. (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("The name that uniquely identifies the pair of an asymmetric-key and a certificate.")
    private String name;
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
     * The name that uniquely identifies the pair of an asymmetric-key and a
     * certificate. (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * The name that uniquely identifies the pair of an asymmetric-key and a
     * certificate. (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public AsymKey withName(String name)
    {
        this.name = name;
        return this;
    }

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

    public AsymKey withAsymmetricKey(String asymmetricKey)
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

    public AsymKey withCertificate(String certificate)
    {
        this.certificate = certificate;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(AsymKey.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
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
        result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
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
        if ((other instanceof AsymKey) == false)
        {
            return false;
        }
        AsymKey rhs = ((AsymKey) other);
        return ((((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
                 && ((this.certificate == rhs.certificate) || ((this.certificate != null) && this.certificate.equals(rhs.certificate))))
                && ((this.asymmetricKey == rhs.asymmetricKey) || ((this.asymmetricKey != null) && this.asymmetricKey.equals(rhs.asymmetricKey))));
    }

}
