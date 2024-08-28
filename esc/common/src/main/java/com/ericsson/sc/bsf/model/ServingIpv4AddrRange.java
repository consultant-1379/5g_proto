
package com.ericsson.sc.bsf.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "ipv4-addr-start", "ipv4-addr-end" })
public class ServingIpv4AddrRange
{

    @JsonProperty("ipv4-addr-start")
    private String ipv4AddrStart;
    @JsonProperty("ipv4-addr-end")
    private String ipv4AddrEnd;

    @JsonProperty("ipv4-addr-start")
    public String getIpv4AddrStart()
    {
        return ipv4AddrStart;
    }

    @JsonProperty("ipv4-addr-start")
    public void setIpv4AddrStart(String ipv4AddrStart)
    {
        this.ipv4AddrStart = ipv4AddrStart;
    }

    public ServingIpv4AddrRange withIpv4AddrStart(String ipv4AddrStart)
    {
        this.ipv4AddrStart = ipv4AddrStart;
        return this;
    }

    @JsonProperty("ipv4-addr-end")
    public String getIpv4AddrEnd()
    {
        return ipv4AddrEnd;
    }

    @JsonProperty("ipv4-addr-end")
    public void setIpv4AddrEnd(String ipv4AddrEnd)
    {
        this.ipv4AddrEnd = ipv4AddrEnd;
    }

    public ServingIpv4AddrRange withIpv4AddrEnd(String ipv4AddrEnd)
    {
        this.ipv4AddrEnd = ipv4AddrEnd;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ServingIpv4AddrRange.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("ipv4AddrStart");
        sb.append('=');
        sb.append(((this.ipv4AddrStart == null) ? "<null>" : this.ipv4AddrStart));
        sb.append(',');
        sb.append("ipv4AddrEnd");
        sb.append('=');
        sb.append(((this.ipv4AddrEnd == null) ? "<null>" : this.ipv4AddrEnd));
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
        result = ((result * 31) + ((this.ipv4AddrStart == null) ? 0 : this.ipv4AddrStart.hashCode()));
        result = ((result * 31) + ((this.ipv4AddrEnd == null) ? 0 : this.ipv4AddrEnd.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof ServingIpv4AddrRange) == false)
        {
            return false;
        }
        ServingIpv4AddrRange rhs = ((ServingIpv4AddrRange) other);
        return (((this.ipv4AddrStart == rhs.ipv4AddrStart) || ((this.ipv4AddrStart != null) && this.ipv4AddrStart.equals(rhs.ipv4AddrStart)))
                && ((this.ipv4AddrEnd == rhs.ipv4AddrEnd) || ((this.ipv4AddrEnd != null) && this.ipv4AddrEnd.equals(rhs.ipv4AddrEnd))));
    }

}
