
package com.ericsson.sc.sepp.model;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.sc.glue.IfDnsProfile;
import com.ericsson.sc.model.IpFamilyResolution;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "ip-family-resolution" })
public class DnsProfile implements IfDnsProfile
{

    /**
     * Name identifying the dns-profile (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name identifying the dns-profile")
    private String name;
    /**
     * IP family for the DNS resolution of an FQDN
     * 
     */
    @JsonProperty("ip-family-resolution")
    @JsonPropertyDescription("IP family for the DNS resolution of an FQDN")
    private List<IpFamilyResolution> ipFamilyResolution = new ArrayList<IpFamilyResolution>();

    /**
     * Name identifying the dns-profile (Required)
     * 
     */
    @Override
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name identifying the dns-profile (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public DnsProfile withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * IP family for the DNS resolution of an FQDN
     * 
     */
    @Override
    @JsonProperty("ip-family-resolution")
    public List<IpFamilyResolution> getIpFamilyResolution()
    {
        return ipFamilyResolution;
    }

    /**
     * IP family for the DNS resolution of an FQDN
     * 
     */
    @JsonProperty("ip-family-resolution")
    public void setIpFamilyResolution(List<IpFamilyResolution> ipFamilyResolution)
    {
        this.ipFamilyResolution = ipFamilyResolution;
    }

    public DnsProfile withIpFamilyResolution(List<IpFamilyResolution> ipFamilyResolution)
    {
        this.ipFamilyResolution = ipFamilyResolution;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(DnsProfile.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("ipFamilyResolution");
        sb.append('=');
        sb.append(((this.ipFamilyResolution == null) ? "<null>" : this.ipFamilyResolution));
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
        result = ((result * 31) + ((this.ipFamilyResolution == null) ? 0 : this.ipFamilyResolution.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof DnsProfile) == false)
        {
            return false;
        }
        DnsProfile rhs = ((DnsProfile) other);
        return (((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
                && ((this.ipFamilyResolution == rhs.ipFamilyResolution)
                    || ((this.ipFamilyResolution != null) && this.ipFamilyResolution.equals(rhs.ipFamilyResolution))));
    }

}
