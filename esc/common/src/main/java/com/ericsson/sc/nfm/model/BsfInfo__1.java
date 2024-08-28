
package com.ericsson.sc.nfm.model;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Specific data for the BSF NF
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "dnn", "ip-domain", "ipv4-addr-range", "ipv6-prefix-range" })
public class BsfInfo__1
{

    @JsonProperty("dnn")
    private List<String> dnn = new ArrayList<String>();
    @JsonProperty("ip-domain")
    private List<String> ipDomain = new ArrayList<String>();
    @JsonProperty("ipv4-addr-range")
    private Ipv4AddrRange ipv4AddrRange;
    @JsonProperty("ipv6-prefix-range")
    private Ipv6PrefixRange ipv6PrefixRange;

    @JsonProperty("dnn")
    public List<String> getDnn()
    {
        return dnn;
    }

    @JsonProperty("dnn")
    public void setDnn(List<String> dnn)
    {
        this.dnn = dnn;
    }

    public BsfInfo__1 withDnn(List<String> dnn)
    {
        this.dnn = dnn;
        return this;
    }

    @JsonProperty("ip-domain")
    public List<String> getIpDomain()
    {
        return ipDomain;
    }

    @JsonProperty("ip-domain")
    public void setIpDomain(List<String> ipDomain)
    {
        this.ipDomain = ipDomain;
    }

    public BsfInfo__1 withIpDomain(List<String> ipDomain)
    {
        this.ipDomain = ipDomain;
        return this;
    }

    @JsonProperty("ipv4-addr-range")
    public Ipv4AddrRange getIpv4AddrRange()
    {
        return ipv4AddrRange;
    }

    @JsonProperty("ipv4-addr-range")
    public void setIpv4AddrRange(Ipv4AddrRange ipv4AddrRange)
    {
        this.ipv4AddrRange = ipv4AddrRange;
    }

    public BsfInfo__1 withIpv4AddrRange(Ipv4AddrRange ipv4AddrRange)
    {
        this.ipv4AddrRange = ipv4AddrRange;
        return this;
    }

    @JsonProperty("ipv6-prefix-range")
    public Ipv6PrefixRange getIpv6PrefixRange()
    {
        return ipv6PrefixRange;
    }

    @JsonProperty("ipv6-prefix-range")
    public void setIpv6PrefixRange(Ipv6PrefixRange ipv6PrefixRange)
    {
        this.ipv6PrefixRange = ipv6PrefixRange;
    }

    public BsfInfo__1 withIpv6PrefixRange(Ipv6PrefixRange ipv6PrefixRange)
    {
        this.ipv6PrefixRange = ipv6PrefixRange;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(BsfInfo__1.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
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
        if ((other instanceof BsfInfo__1) == false)
        {
            return false;
        }
        BsfInfo__1 rhs = ((BsfInfo__1) other);
        return (((((this.ipv6PrefixRange == rhs.ipv6PrefixRange) || ((this.ipv6PrefixRange != null) && this.ipv6PrefixRange.equals(rhs.ipv6PrefixRange)))
                  && ((this.dnn == rhs.dnn) || ((this.dnn != null) && this.dnn.equals(rhs.dnn))))
                 && ((this.ipDomain == rhs.ipDomain) || ((this.ipDomain != null) && this.ipDomain.equals(rhs.ipDomain))))
                && ((this.ipv4AddrRange == rhs.ipv4AddrRange) || ((this.ipv4AddrRange != null) && this.ipv4AddrRange.equals(rhs.ipv4AddrRange))));
    }

}
