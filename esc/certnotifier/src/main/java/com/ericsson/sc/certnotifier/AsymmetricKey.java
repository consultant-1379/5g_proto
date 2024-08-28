package com.ericsson.sc.certnotifier;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "key", "certificate" })
public class AsymmetricKey
{

    @JsonProperty("name")
    private String name;
    @JsonProperty("key")
    private Boolean key;
    @JsonProperty("certificate")
    private Boolean certificate;

    /**
     * No args constructor for use in serialization
     *
     */
    public AsymmetricKey()
    {
    }

    /**
     *
     * @param name
     * @param certificate
     * @param key
     */
    public AsymmetricKey(String name,
                         Boolean key,
                         Boolean certificate)
    {
        super();
        this.name = name;
        this.key = key;
        this.certificate = certificate;
    }

    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

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

    @JsonProperty("key")
    public Boolean getKey()
    {
        return key;
    }

    @JsonProperty("key")
    public void setKey(Boolean key)
    {
        this.key = key;
    }

    public AsymmetricKey withKey(Boolean key)
    {
        this.key = key;
        return this;
    }

    @JsonProperty("certificate")
    public Boolean getCertificate()
    {
        return certificate;
    }

    @JsonProperty("certificate")
    public void setCertificate(Boolean certificate)
    {
        this.certificate = certificate;
    }

    public AsymmetricKey withCertificate(Boolean certificate)
    {
        this.certificate = certificate;
        return this;
    }

    @Override
    public String toString()
    {
        var sb = new StringBuilder();
        sb.append(AsymmetricKey.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("key");
        sb.append('=');
        sb.append(((this.key == null) ? "<null>" : this.key));
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
        var result = 1;
        result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
        result = ((result * 31) + ((this.certificate == null) ? 0 : this.certificate.hashCode()));
        result = ((result * 31) + ((this.key == null) ? 0 : this.key.hashCode()));
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
                && ((this.key == rhs.key) || ((this.key != null) && this.key.equals(rhs.key))));
    }

}