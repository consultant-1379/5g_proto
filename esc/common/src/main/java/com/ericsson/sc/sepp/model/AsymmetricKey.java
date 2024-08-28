
package com.ericsson.sc.sepp.model;

import com.ericsson.utilities.common.IfNamedListItem;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "private-key", "certificate" })
public class AsymmetricKey implements IfNamedListItem
{

    /**
     * Name uniquely identifying the asymmetric key (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name uniquely identifying the asymmetric key")
    private String name;
    /**
     * Name of the installed private key defined in the ietf-keystore (Required)
     * 
     */
    @JsonProperty("private-key")
    @JsonPropertyDescription("Name of the installed private key defined in the ietf-keystore")
    private String privateKey;
    /**
     * Name of the installed certificate defined in the ietf-keystore (Required)
     * 
     */
    @JsonProperty("certificate")
    @JsonPropertyDescription("Name of the installed certificate defined in the ietf-keystore")
    private String certificate;

    /**
     * Name uniquely identifying the asymmetric key (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name uniquely identifying the asymmetric key (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public AsymmetricKey withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Name of the installed private key defined in the ietf-keystore (Required)
     * 
     */
    @JsonProperty("private-key")
    public String getPrivateKey()
    {
        return privateKey;
    }

    /**
     * Name of the installed private key defined in the ietf-keystore (Required)
     * 
     */
    @JsonProperty("private-key")
    public void setPrivateKey(String privateKey)
    {
        this.privateKey = privateKey;
    }

    public AsymmetricKey withPrivateKey(String privateKey)
    {
        this.privateKey = privateKey;
        return this;
    }

    /**
     * Name of the installed certificate defined in the ietf-keystore (Required)
     * 
     */
    @JsonProperty("certificate")
    public String getCertificate()
    {
        return certificate;
    }

    /**
     * Name of the installed certificate defined in the ietf-keystore (Required)
     * 
     */
    @JsonProperty("certificate")
    public void setCertificate(String certificate)
    {
        this.certificate = certificate;
    }

    public AsymmetricKey withCertificate(String certificate)
    {
        this.certificate = certificate;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(AsymmetricKey.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("privateKey");
        sb.append('=');
        sb.append(((this.privateKey == null) ? "<null>" : this.privateKey));
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
        result = ((result * 31) + ((this.privateKey == null) ? 0 : this.privateKey.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof AsymmetricKey) == false)
        {
            return false;
        }
        AsymmetricKey rhs = ((AsymmetricKey) other);
        return ((((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
                 && ((this.certificate == rhs.certificate) || ((this.certificate != null) && this.certificate.equals(rhs.certificate))))
                && ((this.privateKey == rhs.privateKey) || ((this.privateKey != null) && this.privateKey.equals(rhs.privateKey))));
    }

}
