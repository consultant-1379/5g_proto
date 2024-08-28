
package com.ericsson.sc.bsf.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "bsf-service-tap" })
public class VtapConfiguration
{

    @JsonProperty("bsf-service-tap")
    private BsfServiceTap bsfServiceTap;

    @JsonProperty("bsf-service-tap")
    public BsfServiceTap getBsfServiceTap()
    {
        return bsfServiceTap;
    }

    @JsonProperty("bsf-service-tap")
    public void setBsfServiceTap(BsfServiceTap bsfServiceTap)
    {
        this.bsfServiceTap = bsfServiceTap;
    }

    public VtapConfiguration withBsfServiceTap(BsfServiceTap bsfServiceTap)
    {
        this.bsfServiceTap = bsfServiceTap;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(VtapConfiguration.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("bsfServiceTap");
        sb.append('=');
        sb.append(((this.bsfServiceTap == null) ? "<null>" : this.bsfServiceTap));
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
        result = ((result * 31) + ((this.bsfServiceTap == null) ? 0 : this.bsfServiceTap.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof VtapConfiguration) == false)
        {
            return false;
        }
        VtapConfiguration rhs = ((VtapConfiguration) other);
        return ((this.bsfServiceTap == rhs.bsfServiceTap) || ((this.bsfServiceTap != null) && this.bsfServiceTap.equals(rhs.bsfServiceTap)));
    }

}
