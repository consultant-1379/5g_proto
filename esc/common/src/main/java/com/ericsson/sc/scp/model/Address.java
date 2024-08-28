
package com.ericsson.sc.scp.model;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.sc.glue.IfAddress;
import com.ericsson.sc.nfm.model.Scheme;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Address of the service, at least one of FQDN or IPv4 or IPv6 address must be
 * given
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "scheme", "fqdn", "inter-plmn-fqdn", "multiple-ip-endpoint" })
public class Address implements IfAddress
{

    /**
     * The URI scheme (i.e. 'http' or 'https')
     * 
     */
    @JsonProperty("scheme")
    @JsonPropertyDescription("The URI scheme (i.e. 'http' or 'https')")
    private Scheme scheme;
    /**
     * FQDN of the service. According to RFC 1035 only letters, digits and hyphen
     * (-) are allowed.
     * 
     */
    @JsonProperty("fqdn")
    @JsonPropertyDescription("FQDN of the service. According to RFC 1035 only letters, digits and hyphen (-) are allowed.")
    private String fqdn;
    /**
     * The inter-PLMN FQDN of the service
     * 
     */
    @JsonProperty("inter-plmn-fqdn")
    @JsonPropertyDescription("The inter-PLMN FQDN of the service")
    private String interPlmnFqdn;
    /**
     * Multiple IP endpoint(s) of the service, all with the same port and transport
     * 
     */
    @JsonProperty("multiple-ip-endpoint")
    @JsonPropertyDescription("Multiple IP endpoint(s) of the service, all with the same port and transport")
    private List<MultipleIpEndpoint> multipleIpEndpoint = new ArrayList<MultipleIpEndpoint>();

    /**
     * The URI scheme (i.e. 'http' or 'https')
     * 
     */
    @JsonProperty("scheme")
    public Scheme getScheme()
    {
        return scheme;
    }

    /**
     * The URI scheme (i.e. 'http' or 'https')
     * 
     */
    @JsonProperty("scheme")
    public void setScheme(Scheme scheme)
    {
        this.scheme = scheme;
    }

    public Address withScheme(Scheme scheme)
    {
        this.scheme = scheme;
        return this;
    }

    /**
     * FQDN of the service. According to RFC 1035 only letters, digits and hyphen
     * (-) are allowed.
     * 
     */
    @JsonProperty("fqdn")
    public String getFqdn()
    {
        return fqdn;
    }

    /**
     * FQDN of the service. According to RFC 1035 only letters, digits and hyphen
     * (-) are allowed.
     * 
     */
    @JsonProperty("fqdn")
    public void setFqdn(String fqdn)
    {
        this.fqdn = fqdn;
    }

    public Address withFqdn(String fqdn)
    {
        this.fqdn = fqdn;
        return this;
    }

    /**
     * The inter-PLMN FQDN of the service
     * 
     */
    @JsonProperty("inter-plmn-fqdn")
    public String getInterPlmnFqdn()
    {
        return interPlmnFqdn;
    }

    /**
     * The inter-PLMN FQDN of the service
     * 
     */
    @JsonProperty("inter-plmn-fqdn")
    public void setInterPlmnFqdn(String interPlmnFqdn)
    {
        this.interPlmnFqdn = interPlmnFqdn;
    }

    public Address withInterPlmnFqdn(String interPlmnFqdn)
    {
        this.interPlmnFqdn = interPlmnFqdn;
        return this;
    }

    /**
     * Multiple IP endpoint(s) of the service, all with the same port and transport
     * 
     */
    @JsonProperty("multiple-ip-endpoint")
    public List<MultipleIpEndpoint> getMultipleIpEndpoint()
    {
        return multipleIpEndpoint;
    }

    /**
     * Multiple IP endpoint(s) of the service, all with the same port and transport
     * 
     */
    @JsonProperty("multiple-ip-endpoint")
    public void setMultipleIpEndpoint(List<MultipleIpEndpoint> multipleIpEndpoint)
    {
        this.multipleIpEndpoint = multipleIpEndpoint;
    }

    public Address withMultipleIpEndpoint(List<MultipleIpEndpoint> multipleIpEndpoint)
    {
        this.multipleIpEndpoint = multipleIpEndpoint;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(Address.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("scheme");
        sb.append('=');
        sb.append(((this.scheme == null) ? "<null>" : this.scheme));
        sb.append(',');
        sb.append("fqdn");
        sb.append('=');
        sb.append(((this.fqdn == null) ? "<null>" : this.fqdn));
        sb.append(',');
        sb.append("interPlmnFqdn");
        sb.append('=');
        sb.append(((this.interPlmnFqdn == null) ? "<null>" : this.interPlmnFqdn));
        sb.append(',');
        sb.append("multipleIpEndpoint");
        sb.append('=');
        sb.append(((this.multipleIpEndpoint == null) ? "<null>" : this.multipleIpEndpoint));
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
        result = ((result * 31) + ((this.interPlmnFqdn == null) ? 0 : this.interPlmnFqdn.hashCode()));
        result = ((result * 31) + ((this.multipleIpEndpoint == null) ? 0 : this.multipleIpEndpoint.hashCode()));
        result = ((result * 31) + ((this.scheme == null) ? 0 : this.scheme.hashCode()));
        result = ((result * 31) + ((this.fqdn == null) ? 0 : this.fqdn.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof Address) == false)
        {
            return false;
        }
        Address rhs = ((Address) other);
        return (((((this.interPlmnFqdn == rhs.interPlmnFqdn) || ((this.interPlmnFqdn != null) && this.interPlmnFqdn.equals(rhs.interPlmnFqdn)))
                  && ((this.multipleIpEndpoint == rhs.multipleIpEndpoint)
                      || ((this.multipleIpEndpoint != null) && this.multipleIpEndpoint.equals(rhs.multipleIpEndpoint))))
                 && ((this.scheme == rhs.scheme) || ((this.scheme != null) && this.scheme.equals(rhs.scheme))))
                && ((this.fqdn == rhs.fqdn) || ((this.fqdn != null) && this.fqdn.equals(rhs.fqdn))));
    }
}
