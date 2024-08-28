
package com.ericsson.esc.services.cm.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "ipv6-prefix-end", "ipv6-prefix-start" })
public class Ipv6PrefixRange
{

    @JsonProperty("ipv6-prefix-end")
    private String ipv6PrefixEnd;
    @JsonProperty("ipv6-prefix-start")
    private String ipv6PrefixStart;

    @JsonProperty("ipv6-prefix-end")
    public String getIpv6PrefixEnd()
    {
        return ipv6PrefixEnd;
    }

    @JsonProperty("ipv6-prefix-end")
    public void setIpv6PrefixEnd(String ipv6PrefixEnd)
    {
        this.ipv6PrefixEnd = ipv6PrefixEnd;
    }

    public Ipv6PrefixRange withIpv6PrefixEnd(String ipv6PrefixEnd)
    {
        this.ipv6PrefixEnd = ipv6PrefixEnd;
        return this;
    }

    @JsonProperty("ipv6-prefix-start")
    public String getIpv6PrefixStart()
    {
        return ipv6PrefixStart;
    }

    @JsonProperty("ipv6-prefix-start")
    public void setIpv6PrefixStart(String ipv6PrefixStart)
    {
        this.ipv6PrefixStart = ipv6PrefixStart;
    }

    public Ipv6PrefixRange withIpv6PrefixStart(String ipv6PrefixStart)
    {
        this.ipv6PrefixStart = ipv6PrefixStart;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(Ipv6PrefixRange.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("ipv6PrefixEnd");
        sb.append('=');
        sb.append(((this.ipv6PrefixEnd == null) ? "<null>" : this.ipv6PrefixEnd));
        sb.append(',');
        sb.append("ipv6PrefixStart");
        sb.append('=');
        sb.append(((this.ipv6PrefixStart == null) ? "<null>" : this.ipv6PrefixStart));
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
        result = ((result * 31) + ((this.ipv6PrefixStart == null) ? 0 : this.ipv6PrefixStart.hashCode()));
        result = ((result * 31) + ((this.ipv6PrefixEnd == null) ? 0 : this.ipv6PrefixEnd.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof Ipv6PrefixRange) == false)
        {
            return false;
        }
        Ipv6PrefixRange rhs = ((Ipv6PrefixRange) other);
        return (((this.ipv6PrefixStart == rhs.ipv6PrefixStart) || ((this.ipv6PrefixStart != null) && this.ipv6PrefixStart.equals(rhs.ipv6PrefixStart)))
                && ((this.ipv6PrefixEnd == rhs.ipv6PrefixEnd) || ((this.ipv6PrefixEnd != null) && this.ipv6PrefixEnd.equals(rhs.ipv6PrefixEnd))));
    }

}
