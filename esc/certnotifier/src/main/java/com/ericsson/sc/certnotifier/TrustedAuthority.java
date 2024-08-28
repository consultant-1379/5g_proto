package com.ericsson.sc.certnotifier;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "certificateAuthority" })
public class TrustedAuthority
{

    @JsonProperty("name")
    private String name;
    @JsonProperty("certificateAuthority")
    private Boolean certificateAuthority;

    /**
     * No args constructor for use in serialization
     *
     */
    public TrustedAuthority()
    {
    }

    /**
     *
     * @param name
     * @param certificateAuthority
     */
    public TrustedAuthority(String name,
                            Boolean certificateAuthority)
    {
        super();
        this.name = name;
        this.certificateAuthority = certificateAuthority;
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

    public TrustedAuthority withName(String name)
    {
        this.name = name;
        return this;
    }

    @JsonProperty("certificateAuthority")
    public Boolean getCertificateAuthority()
    {
        return certificateAuthority;
    }

    @JsonProperty("certificateAuthority")
    public void setCertificateAuthority(Boolean certificateAuthority)
    {
        this.certificateAuthority = certificateAuthority;
    }

    public TrustedAuthority withCertificateAuthority(Boolean certificateAuthority)
    {
        this.certificateAuthority = certificateAuthority;
        return this;
    }

    @Override
    public String toString()
    {
        var sb = new StringBuilder();
        sb.append(TrustedAuthority.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("certificateAuthority");
        sb.append('=');
        sb.append(((this.certificateAuthority == null) ? "<null>" : this.certificateAuthority));
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
        result = ((result * 31) + ((this.certificateAuthority == null) ? 0 : this.certificateAuthority.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof TrustedAuthority) == false)
        {
            return false;
        }
        TrustedAuthority rhs = ((TrustedAuthority) other);
        return (((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
                && ((this.certificateAuthority == rhs.certificateAuthority)
                    || ((this.certificateAuthority != null) && this.certificateAuthority.equals(rhs.certificateAuthority))));
    }
}