
package com.ericsson.sc.sepp.model;

import java.util.ArrayList;
import java.util.List;
import com.ericsson.sc.glue.IfProxy;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Defines the required data for tapping on the proxy traffic
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "ingress", "egress" })
public class Proxy implements IfProxy
{

    /**
     * Defines the configuration data required for tapping ingress traffic
     * 
     */
    @JsonProperty("ingress")
    @JsonPropertyDescription("Defines the configuration data required for tapping ingress traffic")
    private List<Ingress> ingress = new ArrayList<Ingress>();
    /**
     * Defines the configuration data required for tapping egress traffic
     * 
     */
    @JsonProperty("egress")
    @JsonPropertyDescription("Defines the configuration data required for tapping egress traffic")
    private List<Egress> egress = new ArrayList<Egress>();

    /**
     * Defines the configuration data required for tapping ingress traffic
     * 
     */
    @JsonProperty("ingress")
    public List<Ingress> getIngress()
    {
        return ingress;
    }

    /**
     * Defines the configuration data required for tapping ingress traffic
     * 
     */
    @JsonProperty("ingress")
    public void setIngress(List<Ingress> ingress)
    {
        this.ingress = ingress;
    }

    public Proxy withIngress(List<Ingress> ingress)
    {
        this.ingress = ingress;
        return this;
    }

    /**
     * Defines the configuration data required for tapping egress traffic
     * 
     */
    @JsonProperty("egress")
    public List<Egress> getEgress()
    {
        return egress;
    }

    /**
     * Defines the configuration data required for tapping egress traffic
     * 
     */
    @JsonProperty("egress")
    public void setEgress(List<Egress> egress)
    {
        this.egress = egress;
    }

    public Proxy withEgress(List<Egress> egress)
    {
        this.egress = egress;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(Proxy.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("ingress");
        sb.append('=');
        sb.append(((this.ingress == null) ? "<null>" : this.ingress));
        sb.append(',');
        sb.append("egress");
        sb.append('=');
        sb.append(((this.egress == null) ? "<null>" : this.egress));
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
        result = ((result * 31) + ((this.ingress == null) ? 0 : this.ingress.hashCode()));
        result = ((result * 31) + ((this.egress == null) ? 0 : this.egress.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof Proxy) == false)
        {
            return false;
        }
        Proxy rhs = ((Proxy) other);
        return (((this.ingress == rhs.ingress) || ((this.ingress != null) && this.ingress.equals(rhs.ingress)))
                && ((this.egress == rhs.egress) || ((this.egress != null) && this.egress.equals(rhs.egress))));
    }

}
