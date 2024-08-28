
package com.ericsson.sc.sepp.model;

import com.ericsson.sc.glue.IfVtapConfiguration;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Defines the required settings for traffic tapping
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "proxy" })
public class VtapConfiguration implements IfVtapConfiguration
{

    /**
     * Defines the required data for tapping on the proxy traffic
     * 
     */
    @JsonProperty("proxy")
    @JsonPropertyDescription("Defines the required data for tapping on the proxy traffic")
    private Proxy proxy;

    /**
     * Defines the required data for tapping on the proxy traffic
     * 
     */
    @JsonProperty("proxy")
    public Proxy getProxy()
    {
        return proxy;
    }

    /**
     * Defines the required data for tapping on the proxy traffic
     * 
     */
    @JsonProperty("proxy")
    public void setProxy(Proxy proxy)
    {
        this.proxy = proxy;
    }

    public VtapConfiguration withProxy(Proxy proxy)
    {
        this.proxy = proxy;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(VtapConfiguration.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("proxy");
        sb.append('=');
        sb.append(((this.proxy == null) ? "<null>" : this.proxy));
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
        result = ((result * 31) + ((this.proxy == null) ? 0 : this.proxy.hashCode()));
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
        return ((this.proxy == rhs.proxy) || ((this.proxy != null) && this.proxy.equals(rhs.proxy)));
    }

}
