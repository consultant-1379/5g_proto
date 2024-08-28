
package com.ericsson.sc.scp.model;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.sc.nfm.model.Ipv4AddrRange;
import com.ericsson.sc.nfm.model.Ipv6PrefixRange;
import com.ericsson.sc.nfm.model.RemotePlmn;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Specific data for the SCP NF
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "scp-prefix",
                     "ipv4-address",
                     "ipv6-prefix",
                     "ipv4-addr-range",
                     "ipv6-prefix-range",
                     "served-nf-set-id",
                     "address-domain",
                     "remote-plmn",
                     "scp-capabilities",
                     "ip-reachability",
                     "scp-domain-info" })
public class ScpInfo
{

    /**
     * Optional deployment-specific string used to construct the apiRoot of this SCP
     * 
     */
    @JsonProperty("scp-prefix")
    @JsonPropertyDescription("Optional deployment-specific string used to construct the apiRoot of this SCP")
    private String scpPrefix;
    /**
     * IPv4 address reachable through the SCP
     * 
     */
    @JsonProperty("ipv4-address")
    @JsonPropertyDescription("IPv4 address reachable through the SCP")
    private List<String> ipv4Address = new ArrayList<String>();
    /**
     * IPv6 prefix reachable through the SCP
     * 
     */
    @JsonProperty("ipv6-prefix")
    @JsonPropertyDescription("IPv6 prefix reachable through the SCP")
    private List<String> ipv6Prefix = new ArrayList<String>();
    /**
     * IPv4 address range reachable through the SCP
     * 
     */
    @JsonProperty("ipv4-addr-range")
    @JsonPropertyDescription("IPv4 address range reachable through the SCP")
    private List<Ipv4AddrRange> ipv4AddrRange = new ArrayList<Ipv4AddrRange>();
    /**
     * IPv6 prefix range reachable through the SCP
     * 
     */
    @JsonProperty("ipv6-prefix-range")
    @JsonPropertyDescription("IPv6 prefix range reachable through the SCP")
    private List<Ipv6PrefixRange> ipv6PrefixRange = new ArrayList<Ipv6PrefixRange>();
    /**
     * The set identity of the NF served by the SCP
     * 
     */
    @JsonProperty("served-nf-set-id")
    @JsonPropertyDescription("The set identity of the NF served by the SCP")
    private List<String> servedNfSetId = new ArrayList<String>();
    /**
     * Address domain name(s) reachable through the SCP
     * 
     */
    @JsonProperty("address-domain")
    @JsonPropertyDescription("Address domain name(s) reachable through the SCP")
    private List<String> addressDomain = new ArrayList<String>();
    /**
     * Remote PLMN(s) reachable through the SCP
     * 
     */
    @JsonProperty("remote-plmn")
    @JsonPropertyDescription("Remote PLMN(s) reachable through the SCP")
    private List<RemotePlmn> remotePlmn = new ArrayList<RemotePlmn>();
    /**
     * If present, scp capabilities are published in nrf during registration. If
     * present but no scp capabilities are defined, then an empty array is sent to
     * NRF.
     * 
     */
    @JsonProperty("scp-capabilities")
    @JsonPropertyDescription("If present, scp capabilities are published in nrf during registration. If present but no scp capabilities are defined, then an empty array is sent to NRF.")
    private ScpCapabilities scpCapabilities;
    /**
     * Type of IP addresses reachable through the SCP in the SCP domain(s) it
     * belongs to
     * 
     */
    @JsonProperty("ip-reachability")
    @JsonPropertyDescription("Type of IP addresses reachable through the SCP in the SCP domain(s) it belongs to")
    private String ipReachability;
    /**
     * SCP domain specific information of the SCP
     * 
     */
    @JsonProperty("scp-domain-info")
    @JsonPropertyDescription("SCP domain specific information of the SCP")
    private List<ScpDomainInfo> scpDomainInfo = new ArrayList<ScpDomainInfo>();

    /**
     * Optional deployment-specific string used to construct the apiRoot of this SCP
     * 
     */
    @JsonProperty("scp-prefix")
    public String getScpPrefix()
    {
        return scpPrefix;
    }

    /**
     * Optional deployment-specific string used to construct the apiRoot of this SCP
     * 
     */
    @JsonProperty("scp-prefix")
    public void setScpPrefix(String scpPrefix)
    {
        this.scpPrefix = scpPrefix;
    }

    public ScpInfo withScpPrefix(String scpPrefix)
    {
        this.scpPrefix = scpPrefix;
        return this;
    }

    /**
     * IPv4 address reachable through the SCP
     * 
     */
    @JsonProperty("ipv4-address")
    public List<String> getIpv4Address()
    {
        return ipv4Address;
    }

    /**
     * IPv4 address reachable through the SCP
     * 
     */
    @JsonProperty("ipv4-address")
    public void setIpv4Address(List<String> ipv4Address)
    {
        this.ipv4Address = ipv4Address;
    }

    public ScpInfo withIpv4Address(List<String> ipv4Address)
    {
        this.ipv4Address = ipv4Address;
        return this;
    }

    /**
     * IPv6 prefix reachable through the SCP
     * 
     */
    @JsonProperty("ipv6-prefix")
    public List<String> getIpv6Prefix()
    {
        return ipv6Prefix;
    }

    /**
     * IPv6 prefix reachable through the SCP
     * 
     */
    @JsonProperty("ipv6-prefix")
    public void setIpv6Prefix(List<String> ipv6Prefix)
    {
        this.ipv6Prefix = ipv6Prefix;
    }

    public ScpInfo withIpv6Prefix(List<String> ipv6Prefix)
    {
        this.ipv6Prefix = ipv6Prefix;
        return this;
    }

    /**
     * IPv4 address range reachable through the SCP
     * 
     */
    @JsonProperty("ipv4-addr-range")
    public List<Ipv4AddrRange> getIpv4AddrRange()
    {
        return ipv4AddrRange;
    }

    /**
     * IPv4 address range reachable through the SCP
     * 
     */
    @JsonProperty("ipv4-addr-range")
    public void setIpv4AddrRange(List<Ipv4AddrRange> ipv4AddrRange)
    {
        this.ipv4AddrRange = ipv4AddrRange;
    }

    public ScpInfo withIpv4AddrRange(List<Ipv4AddrRange> ipv4AddrRange)
    {
        this.ipv4AddrRange = ipv4AddrRange;
        return this;
    }

    /**
     * IPv6 prefix range reachable through the SCP
     * 
     */
    @JsonProperty("ipv6-prefix-range")
    public List<Ipv6PrefixRange> getIpv6PrefixRange()
    {
        return ipv6PrefixRange;
    }

    /**
     * IPv6 prefix range reachable through the SCP
     * 
     */
    @JsonProperty("ipv6-prefix-range")
    public void setIpv6PrefixRange(List<Ipv6PrefixRange> ipv6PrefixRange)
    {
        this.ipv6PrefixRange = ipv6PrefixRange;
    }

    public ScpInfo withIpv6PrefixRange(List<Ipv6PrefixRange> ipv6PrefixRange)
    {
        this.ipv6PrefixRange = ipv6PrefixRange;
        return this;
    }

    /**
     * The set identity of the NF served by the SCP
     * 
     */
    @JsonProperty("served-nf-set-id")
    public List<String> getServedNfSetId()
    {
        return servedNfSetId;
    }

    /**
     * The set identity of the NF served by the SCP
     * 
     */
    @JsonProperty("served-nf-set-id")
    public void setServedNfSetId(List<String> servedNfSetId)
    {
        this.servedNfSetId = servedNfSetId;
    }

    public ScpInfo withServedNfSetId(List<String> servedNfSetId)
    {
        this.servedNfSetId = servedNfSetId;
        return this;
    }

    /**
     * Address domain name(s) reachable through the SCP
     * 
     */
    @JsonProperty("address-domain")
    public List<String> getAddressDomain()
    {
        return addressDomain;
    }

    /**
     * Address domain name(s) reachable through the SCP
     * 
     */
    @JsonProperty("address-domain")
    public void setAddressDomain(List<String> addressDomain)
    {
        this.addressDomain = addressDomain;
    }

    public ScpInfo withAddressDomain(List<String> addressDomain)
    {
        this.addressDomain = addressDomain;
        return this;
    }

    /**
     * Remote PLMN(s) reachable through the SCP
     * 
     */
    @JsonProperty("remote-plmn")
    public List<RemotePlmn> getRemotePlmn()
    {
        return remotePlmn;
    }

    /**
     * Remote PLMN(s) reachable through the SCP
     * 
     */
    @JsonProperty("remote-plmn")
    public void setRemotePlmn(List<RemotePlmn> remotePlmn)
    {
        this.remotePlmn = remotePlmn;
    }

    public ScpInfo withRemotePlmn(List<RemotePlmn> remotePlmn)
    {
        this.remotePlmn = remotePlmn;
        return this;
    }

    /**
     * If present, scp capabilities are published in nrf during registration. If
     * present but no scp capabilities are defined, then an empty array is sent to
     * NRF.
     * 
     */
    @JsonProperty("scp-capabilities")
    public ScpCapabilities getScpCapabilities()
    {
        return scpCapabilities;
    }

    /**
     * If present, scp capabilities are published in nrf during registration. If
     * present but no scp capabilities are defined, then an empty array is sent to
     * NRF.
     * 
     */
    @JsonProperty("scp-capabilities")
    public void setScpCapabilities(ScpCapabilities scpCapabilities)
    {
        this.scpCapabilities = scpCapabilities;
    }

    public ScpInfo withScpCapabilities(ScpCapabilities scpCapabilities)
    {
        this.scpCapabilities = scpCapabilities;
        return this;
    }

    /**
     * Type of IP addresses reachable through the SCP in the SCP domain(s) it
     * belongs to
     * 
     */
    @JsonProperty("ip-reachability")
    public String getIpReachability()
    {
        return ipReachability;
    }

    /**
     * Type of IP addresses reachable through the SCP in the SCP domain(s) it
     * belongs to
     * 
     */
    @JsonProperty("ip-reachability")
    public void setIpReachability(String ipReachability)
    {
        this.ipReachability = ipReachability;
    }

    public ScpInfo withIpReachability(String ipReachability)
    {
        this.ipReachability = ipReachability;
        return this;
    }

    /**
     * SCP domain specific information of the SCP
     * 
     */
    @JsonProperty("scp-domain-info")
    public List<ScpDomainInfo> getScpDomainInfo()
    {
        return scpDomainInfo;
    }

    /**
     * SCP domain specific information of the SCP
     * 
     */
    @JsonProperty("scp-domain-info")
    public void setScpDomainInfo(List<ScpDomainInfo> scpDomainInfo)
    {
        this.scpDomainInfo = scpDomainInfo;
    }

    public ScpInfo withScpDomainInfo(List<ScpDomainInfo> scpDomainInfo)
    {
        this.scpDomainInfo = scpDomainInfo;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ScpInfo.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("scpPrefix");
        sb.append('=');
        sb.append(((this.scpPrefix == null) ? "<null>" : this.scpPrefix));
        sb.append(',');
        sb.append("ipv4Address");
        sb.append('=');
        sb.append(((this.ipv4Address == null) ? "<null>" : this.ipv4Address));
        sb.append(',');
        sb.append("ipv6Prefix");
        sb.append('=');
        sb.append(((this.ipv6Prefix == null) ? "<null>" : this.ipv6Prefix));
        sb.append(',');
        sb.append("ipv4AddrRange");
        sb.append('=');
        sb.append(((this.ipv4AddrRange == null) ? "<null>" : this.ipv4AddrRange));
        sb.append(',');
        sb.append("ipv6PrefixRange");
        sb.append('=');
        sb.append(((this.ipv6PrefixRange == null) ? "<null>" : this.ipv6PrefixRange));
        sb.append(',');
        sb.append("servedNfSetId");
        sb.append('=');
        sb.append(((this.servedNfSetId == null) ? "<null>" : this.servedNfSetId));
        sb.append(',');
        sb.append("addressDomain");
        sb.append('=');
        sb.append(((this.addressDomain == null) ? "<null>" : this.addressDomain));
        sb.append(',');
        sb.append("remotePlmn");
        sb.append('=');
        sb.append(((this.remotePlmn == null) ? "<null>" : this.remotePlmn));
        sb.append(',');
        sb.append("scpCapabilities");
        sb.append('=');
        sb.append(((this.scpCapabilities == null) ? "<null>" : this.scpCapabilities));
        sb.append(',');
        sb.append("ipReachability");
        sb.append('=');
        sb.append(((this.ipReachability == null) ? "<null>" : this.ipReachability));
        sb.append(',');
        sb.append("scpDomainInfo");
        sb.append('=');
        sb.append(((this.scpDomainInfo == null) ? "<null>" : this.scpDomainInfo));
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
        result = ((result * 31) + ((this.scpPrefix == null) ? 0 : this.scpPrefix.hashCode()));
        result = ((result * 31) + ((this.scpDomainInfo == null) ? 0 : this.scpDomainInfo.hashCode()));
        result = ((result * 31) + ((this.ipv4Address == null) ? 0 : this.ipv4Address.hashCode()));
        result = ((result * 31) + ((this.addressDomain == null) ? 0 : this.addressDomain.hashCode()));
        result = ((result * 31) + ((this.ipv6PrefixRange == null) ? 0 : this.ipv6PrefixRange.hashCode()));
        result = ((result * 31) + ((this.remotePlmn == null) ? 0 : this.remotePlmn.hashCode()));
        result = ((result * 31) + ((this.ipReachability == null) ? 0 : this.ipReachability.hashCode()));
        result = ((result * 31) + ((this.ipv6Prefix == null) ? 0 : this.ipv6Prefix.hashCode()));
        result = ((result * 31) + ((this.ipv4AddrRange == null) ? 0 : this.ipv4AddrRange.hashCode()));
        result = ((result * 31) + ((this.servedNfSetId == null) ? 0 : this.servedNfSetId.hashCode()));
        result = ((result * 31) + ((this.scpCapabilities == null) ? 0 : this.scpCapabilities.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof ScpInfo) == false)
        {
            return false;
        }
        ScpInfo rhs = ((ScpInfo) other);
        return ((((((((((((this.scpPrefix == rhs.scpPrefix) || ((this.scpPrefix != null) && this.scpPrefix.equals(rhs.scpPrefix)))
                         && ((this.scpDomainInfo == rhs.scpDomainInfo) || ((this.scpDomainInfo != null) && this.scpDomainInfo.equals(rhs.scpDomainInfo))))
                        && ((this.ipv4Address == rhs.ipv4Address) || ((this.ipv4Address != null) && this.ipv4Address.equals(rhs.ipv4Address))))
                       && ((this.addressDomain == rhs.addressDomain) || ((this.addressDomain != null) && this.addressDomain.equals(rhs.addressDomain))))
                      && ((this.ipv6PrefixRange == rhs.ipv6PrefixRange)
                          || ((this.ipv6PrefixRange != null) && this.ipv6PrefixRange.equals(rhs.ipv6PrefixRange))))
                     && ((this.remotePlmn == rhs.remotePlmn) || ((this.remotePlmn != null) && this.remotePlmn.equals(rhs.remotePlmn))))
                    && ((this.ipReachability == rhs.ipReachability) || ((this.ipReachability != null) && this.ipReachability.equals(rhs.ipReachability))))
                   && ((this.ipv6Prefix == rhs.ipv6Prefix) || ((this.ipv6Prefix != null) && this.ipv6Prefix.equals(rhs.ipv6Prefix))))
                  && ((this.ipv4AddrRange == rhs.ipv4AddrRange) || ((this.ipv4AddrRange != null) && this.ipv4AddrRange.equals(rhs.ipv4AddrRange))))
                 && ((this.servedNfSetId == rhs.servedNfSetId) || ((this.servedNfSetId != null) && this.servedNfSetId.equals(rhs.servedNfSetId))))
                && ((this.scpCapabilities == rhs.scpCapabilities) || ((this.scpCapabilities != null) && this.scpCapabilities.equals(rhs.scpCapabilities))));
    }

}
