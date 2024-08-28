
package com.ericsson.sc.bsf.model;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.sc.nfm.model.Ipv4AddrRange;
import com.ericsson.sc.nfm.model.Ipv6PrefixRange;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Specific data for the BSF NF
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "dnn", "ip-domain", "ipv4-addr-range", "ipv6-prefix-range" })
public class BsfInfo
{

    /**
     * DNN(s) handled by the BSF. If not provided, the BSF can serve any DNN
     * 
     */
    @JsonProperty("dnn")
    @JsonPropertyDescription("DNN(s) handled by the BSF. If not provided, the BSF can serve any DNN")
    private List<String> dnn = new ArrayList<String>();
    /**
     * IPv4 address domain(s) handled by the BSF. If not provided, the BSF can serve
     * any IP domain
     * 
     */
    @JsonProperty("ip-domain")
    @JsonPropertyDescription("IPv4 address domain(s) handled by the BSF. If not provided, the BSF can serve any IP domain")
    private List<String> ipDomain = new ArrayList<String>();
    /**
     * Range(s) of IPv4 addresses handled by BSF. If not provided, the BSF can serve
     * any IPv4 address
     * 
     */
    @JsonProperty("ipv4-addr-range")
    @JsonPropertyDescription("Range(s) of IPv4 addresses handled by BSF. If not provided, the BSF can serve any IPv4 address")
    private List<Ipv4AddrRange> ipv4AddrRange = new ArrayList<Ipv4AddrRange>();
    /**
     * Range(s) of IPv6 prefixes handled by the BSF. If not provided, the BSF can
     * serve any IPv6 prefix
     * 
     */
    @JsonProperty("ipv6-prefix-range")
    @JsonPropertyDescription("Range(s) of IPv6 prefixes handled by the BSF. If not provided, the BSF can serve any IPv6 prefix")
    private List<Ipv6PrefixRange> ipv6PrefixRange = new ArrayList<Ipv6PrefixRange>();

    /**
     * DNN(s) handled by the BSF. If not provided, the BSF can serve any DNN
     * 
     */
    @JsonProperty("dnn")
    public List<String> getDnn()
    {
        return dnn;
    }

    /**
     * DNN(s) handled by the BSF. If not provided, the BSF can serve any DNN
     * 
     */
    @JsonProperty("dnn")
    public void setDnn(List<String> dnn)
    {
        this.dnn = dnn;
    }

    public BsfInfo withDnn(List<String> dnn)
    {
        this.dnn = dnn;
        return this;
    }

    /**
     * IPv4 address domain(s) handled by the BSF. If not provided, the BSF can serve
     * any IP domain
     * 
     */
    @JsonProperty("ip-domain")
    public List<String> getIpDomain()
    {
        return ipDomain;
    }

    /**
     * IPv4 address domain(s) handled by the BSF. If not provided, the BSF can serve
     * any IP domain
     * 
     */
    @JsonProperty("ip-domain")
    public void setIpDomain(List<String> ipDomain)
    {
        this.ipDomain = ipDomain;
    }

    public BsfInfo withIpDomain(List<String> ipDomain)
    {
        this.ipDomain = ipDomain;
        return this;
    }

    /**
     * Range(s) of IPv4 addresses handled by BSF. If not provided, the BSF can serve
     * any IPv4 address
     * 
     */
    @JsonProperty("ipv4-addr-range")
    public List<Ipv4AddrRange> getIpv4AddrRange()
    {
        return ipv4AddrRange;
    }

    /**
     * Range(s) of IPv4 addresses handled by BSF. If not provided, the BSF can serve
     * any IPv4 address
     * 
     */
    @JsonProperty("ipv4-addr-range")
    public void setIpv4AddrRange(List<Ipv4AddrRange> ipv4AddrRange)
    {
        this.ipv4AddrRange = ipv4AddrRange;
    }

    public BsfInfo withIpv4AddrRange(List<Ipv4AddrRange> ipv4AddrRange)
    {
        this.ipv4AddrRange = ipv4AddrRange;
        return this;
    }

    /**
     * Range(s) of IPv6 prefixes handled by the BSF. If not provided, the BSF can
     * serve any IPv6 prefix
     * 
     */
    @JsonProperty("ipv6-prefix-range")
    public List<Ipv6PrefixRange> getIpv6PrefixRange()
    {
        return ipv6PrefixRange;
    }

    /**
     * Range(s) of IPv6 prefixes handled by the BSF. If not provided, the BSF can
     * serve any IPv6 prefix
     * 
     */
    @JsonProperty("ipv6-prefix-range")
    public void setIpv6PrefixRange(List<Ipv6PrefixRange> ipv6PrefixRange)
    {
        this.ipv6PrefixRange = ipv6PrefixRange;
    }

    public BsfInfo withIpv6PrefixRange(List<Ipv6PrefixRange> ipv6PrefixRange)
    {
        this.ipv6PrefixRange = ipv6PrefixRange;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(BsfInfo.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("dnn");
        sb.append('=');
        sb.append(((this.dnn == null) ? "<null>" : this.dnn));
        sb.append(',');
        sb.append("ipDomain");
        sb.append('=');
        sb.append(((this.ipDomain == null) ? "<null>" : this.ipDomain));
        sb.append(',');
        sb.append("ipv4AddrRange");
        sb.append('=');
        sb.append(((this.ipv4AddrRange == null) ? "<null>" : this.ipv4AddrRange));
        sb.append(',');
        sb.append("ipv6PrefixRange");
        sb.append('=');
        sb.append(((this.ipv6PrefixRange == null) ? "<null>" : this.ipv6PrefixRange));
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
        result = ((result * 31) + ((this.ipv6PrefixRange == null) ? 0 : this.ipv6PrefixRange.hashCode()));
        result = ((result * 31) + ((this.dnn == null) ? 0 : this.dnn.hashCode()));
        result = ((result * 31) + ((this.ipDomain == null) ? 0 : this.ipDomain.hashCode()));
        result = ((result * 31) + ((this.ipv4AddrRange == null) ? 0 : this.ipv4AddrRange.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof BsfInfo) == false)
        {
            return false;
        }
        BsfInfo rhs = ((BsfInfo) other);
        return (((((this.ipv6PrefixRange == rhs.ipv6PrefixRange) || ((this.ipv6PrefixRange != null) && this.ipv6PrefixRange.equals(rhs.ipv6PrefixRange)))
                  && ((this.dnn == rhs.dnn) || ((this.dnn != null) && this.dnn.equals(rhs.dnn))))
                 && ((this.ipDomain == rhs.ipDomain) || ((this.ipDomain != null) && this.ipDomain.equals(rhs.ipDomain))))
                && ((this.ipv4AddrRange == rhs.ipv4AddrRange) || ((this.ipv4AddrRange != null) && this.ipv4AddrRange.equals(rhs.ipv4AddrRange))));
    }

}
