
package com.ericsson.sc.scp.model;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.sc.glue.IfVtapEgress;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "enabled", "all-nf-pools", "nf-pool-ref" })

public class Egress implements IfVtapEgress
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    private String name;
    /**
     * A switch that allows the operator to enable or disable traffic tapping for
     * the specific element
     * 
     */
    @JsonProperty("enabled")
    @JsonPropertyDescription("A switch that allows the operator to enable or disable traffic tapping for the specific element")
    private Boolean enabled = true;
    /**
     * If present , it applies egress vtap configuration to all nf-pools
     * 
     */
    @JsonProperty("all-nf-pools")
    @JsonPropertyDescription("If present , it applies egress vtap configuration to all nf-pools")
    private AllNfPools allNfPools;
    /**
     * 
     * Reference to the nf-pool the traffic of which is taken into account for
     * tapping
     * 
     */
    @JsonProperty("nf-pool-ref")
    @JsonPropertyDescription("Reference to the nf-pool the traffic of which is taken into account for tapping")
    private List<String> nfPoolRef = new ArrayList<String>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public Egress withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * A switch that allows the operator to enable or disable traffic tapping for
     * the specific element
     * 
     */
    @JsonProperty("enabled")
    public Boolean getEnabled()
    {
        return enabled;
    }

    /**
     * A switch that allows the operator to enable or disable traffic tapping for
     * the specific element
     * 
     */
    @JsonProperty("enabled")
    public void setEnabled(Boolean enabled)
    {
        this.enabled = enabled;
    }

    public Egress withEnabled(Boolean enabled)
    {
        this.enabled = enabled;
        return this;
    }

    /**
     * If present , it applies egress vtap configuration to all nf-pools
     * 
     */
    @JsonProperty("all-nf-pools")
    public AllNfPools getAllNfPools()
    {
        return allNfPools;
    }

    /**
     * If present , it applies egress vtap configuration to all nf-pools
     * 
     */
    @JsonProperty("all-nf-pools")
    public void setAllNfPools(AllNfPools allNfPools)
    {
        this.allNfPools = allNfPools;
    }

    public Egress withAllNfPools(AllNfPools allNfPools)
    {
        this.allNfPools = allNfPools;
        return this;
    }

    /**
     * Reference to the nf-pool the traffic of which is taken into account for
     * tapping
     * 
     */
    @JsonProperty("nf-pool-ref")
    public List<String> getNfPoolRef()
    {
        return nfPoolRef;
    }

    /**
     * Reference to the nf-pool the traffic of which is taken into account for
     * tapping
     * 
     */
    @JsonProperty("nf-pool-ref")
    public void setNfPoolRef(List<String> nfPoolRef)
    {
        this.nfPoolRef = nfPoolRef;
    }

    public Egress withNfPoolRef(List<String> nfPoolRef)
    {
        this.nfPoolRef = nfPoolRef;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(Egress.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("enabled");
        sb.append('=');
        sb.append(((this.enabled == null) ? "<null>" : this.enabled));
        sb.append(',');
        sb.append("allNfPools");
        sb.append('=');
        sb.append(((this.allNfPools == null) ? "<null>" : this.allNfPools));
        sb.append(',');

        sb.append("nfPoolRef");
        sb.append('=');
        sb.append(((this.nfPoolRef == null) ? "<null>" : this.nfPoolRef));
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
        result = ((result * 31) + ((this.nfPoolRef == null) ? 0 : this.nfPoolRef.hashCode()));
        result = ((result * 31) + ((this.enabled == null) ? 0 : this.enabled.hashCode()));
        result = ((result * 31) + ((this.allNfPools == null) ? 0 : this.allNfPools.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof Egress) == false)
        {
            return false;
        }
        Egress rhs = ((Egress) other);
        return (((((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
                  && ((this.nfPoolRef == rhs.nfPoolRef) || ((this.nfPoolRef != null) && this.nfPoolRef.equals(rhs.nfPoolRef))))
                 && ((this.enabled == rhs.enabled) || ((this.enabled != null) && this.enabled.equals(rhs.enabled))))
                && ((this.allNfPools == rhs.allNfPools) || ((this.allNfPools != null) && this.allNfPools.equals(rhs.allNfPools))));

    }

}
