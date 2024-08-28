
package com.ericsson.sc.bsf.model;

import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "ingress" })
public class BsfServiceTap
{

    /**
     * Defines the configuration data required for tapping the BSF service
     * 
     */
    @JsonProperty("ingress")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("Defines the configuration data required for tapping the BSF service")
    private Set<Ingress> ingress = new LinkedHashSet<Ingress>();

    /**
     * Defines the configuration data required for tapping the BSF service
     * 
     */
    @JsonProperty("ingress")
    public Set<Ingress> getIngress()
    {
        return ingress;
    }

    /**
     * Defines the configuration data required for tapping the BSF service
     * 
     */
    @JsonProperty("ingress")
    public void setIngress(Set<Ingress> ingress)
    {
        this.ingress = ingress;
    }

    public BsfServiceTap withIngress(Set<Ingress> ingress)
    {
        this.ingress = ingress;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(BsfServiceTap.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("ingress");
        sb.append('=');
        sb.append(((this.ingress == null) ? "<null>" : this.ingress));
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
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof BsfServiceTap) == false)
        {
            return false;
        }
        BsfServiceTap rhs = ((BsfServiceTap) other);
        return ((this.ingress == rhs.ingress) || ((this.ingress != null) && this.ingress.equals(rhs.ingress)));
    }

}
