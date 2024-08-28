
package com.ericsson.sc.bsf.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "binding-timeout", "nf-pool", "binding-database", "diameter-routing", "multiple-binding-resolution", "pcf-recovery-time" })
public class BsfService
{

    /**
     * Name uniquely identifying the BSF service (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name uniquely identifying the BSF service")
    private String name;
    /**
     * Obsolete, Session binding expiration timer in hours
     * 
     */
    @JsonProperty("binding-timeout")
    @JsonPropertyDescription("Obsolete, Session binding expiration timer in hours")
    private Integer bindingTimeout = 720;
    /**
     * Grouping of dynamically discovered NF instances
     * 
     */
    @JsonProperty("nf-pool")
    @JsonPropertyDescription("Grouping of dynamically discovered NF instances")
    private List<NfPool> nfPool = new ArrayList<NfPool>();
    /**
     * Database related properties and actions
     * 
     */
    @JsonProperty("binding-database")
    @JsonPropertyDescription("Database related properties and actions")
    private BindingDatabase bindingDatabase;
    /**
     * Configuration settings for routing of diameter messages.
     * 
     */
    @JsonProperty("diameter-routing")
    @JsonPropertyDescription("Configuration settings for routing of diameter messages.")
    private DiameterRouting diameterRouting;
    /**
     * Configuration settings for multiple bindings handling.
     * 
     */
    @JsonProperty("multiple-binding-resolution")
    @JsonPropertyDescription("Configuration settings for multiple bindings handling.")
    private MultipleBindingResolution multipleBindingResolution;
    /**
     * Configuration settings for cleanups based on pcf recovery time feature.
     * 
     */
    @JsonProperty("pcf-recovery-time")
    @JsonPropertyDescription("Configuration settings for cleanups based on pcf recovery time feature.")
    private PcfRecoveryTime pcfRecoveryTime;

    /**
     * Name uniquely identifying the BSF service (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name uniquely identifying the BSF service (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public BsfService withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Obsolete, Session binding expiration timer in hours
     * 
     */
    @JsonProperty("binding-timeout")
    public Integer getBindingTimeout()
    {
        return bindingTimeout;
    }

    /**
     * Obsolete, Session binding expiration timer in hours
     * 
     */
    @JsonProperty("binding-timeout")
    public void setBindingTimeout(Integer bindingTimeout)
    {
        this.bindingTimeout = bindingTimeout;
    }

    public BsfService withBindingTimeout(Integer bindingTimeout)
    {
        this.bindingTimeout = bindingTimeout;
        return this;
    }

    /**
     * Grouping of dynamically discovered NF instances
     * 
     */
    @JsonProperty("nf-pool")
    public List<NfPool> getNfPool()
    {
        return nfPool;
    }

    /**
     * Grouping of dynamically discovered NF instances
     * 
     */
    @JsonProperty("nf-pool")
    public void setNfPool(List<NfPool> nfPool)
    {
        this.nfPool = nfPool;
    }

    public BsfService withNfPool(List<NfPool> nfPool)
    {
        this.nfPool = nfPool;
        return this;
    }

    /**
     * Database related properties and actions
     * 
     */
    @JsonProperty("binding-database")
    public BindingDatabase getBindingDatabase()
    {
        return bindingDatabase;
    }

    /**
     * Database related properties and actions
     * 
     */
    @JsonProperty("binding-database")
    public void setBindingDatabase(BindingDatabase bindingDatabase)
    {
        this.bindingDatabase = bindingDatabase;
    }

    public BsfService withBindingDatabase(BindingDatabase bindingDatabase)
    {
        this.bindingDatabase = bindingDatabase;
        return this;
    }

    /**
     * Configuration settings for routing of diameter messages.
     * 
     */
    @JsonProperty("diameter-routing")
    public DiameterRouting getDiameterRouting()
    {
        return diameterRouting;
    }

    /**
     * Configuration settings for routing of diameter messages.
     * 
     */
    @JsonProperty("diameter-routing")
    public void setDiameterRouting(DiameterRouting diameterRouting)
    {
        this.diameterRouting = diameterRouting;
    }

    public BsfService withDiameterRouting(DiameterRouting diameterRouting)
    {
        this.diameterRouting = diameterRouting;
        return this;
    }

    /**
     * Configuration settings for multiple bindings handling.
     * 
     */
    @JsonProperty("multiple-binding-resolution")
    public MultipleBindingResolution getMultipleBindingResolution()
    {
        return multipleBindingResolution;
    }

    /**
     * Configuration settings for multiple bindings handling.
     * 
     */
    @JsonProperty("multiple-binding-resolution")
    public void setMultipleBindingResolution(MultipleBindingResolution multipleBindingResolution)
    {
        this.multipleBindingResolution = multipleBindingResolution;
    }

    public BsfService withMultipleBindingResolution(MultipleBindingResolution multipleBindingResolution)
    {
        this.multipleBindingResolution = multipleBindingResolution;
        return this;
    }

    /**
     * Configuration settings for cleanups based on pcf recovery time feature.
     * 
     */
    @JsonProperty("pcf-recovery-time")
    public PcfRecoveryTime getPcfRecoveryTime()
    {
        return pcfRecoveryTime;
    }

    /**
     * Configuration settings for cleanups based on pcf recovery time feature.
     * 
     */
    @JsonProperty("pcf-recovery-time")
    public void setPcfRecoveryTime(PcfRecoveryTime pcfRecoveryTime)
    {
        this.pcfRecoveryTime = pcfRecoveryTime;
    }

    public BsfService withPcfRecoveryTime(PcfRecoveryTime pcfRecoveryTime)
    {
        this.pcfRecoveryTime = pcfRecoveryTime;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(BsfService.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("bindingTimeout");
        sb.append('=');
        sb.append(((this.bindingTimeout == null) ? "<null>" : this.bindingTimeout));
        sb.append(',');
        sb.append("nfPool");
        sb.append('=');
        sb.append(((this.nfPool == null) ? "<null>" : this.nfPool));
        sb.append(',');
        sb.append("bindingDatabase");
        sb.append('=');
        sb.append(((this.bindingDatabase == null) ? "<null>" : this.bindingDatabase));
        sb.append(',');
        sb.append("diameterRouting");
        sb.append('=');
        sb.append(((this.diameterRouting == null) ? "<null>" : this.diameterRouting));
        sb.append(',');
        sb.append("multipleBindingResolution");
        sb.append('=');
        sb.append(((this.multipleBindingResolution == null) ? "<null>" : this.multipleBindingResolution));
        sb.append(',');
        sb.append("pcfRecoveryTime");
        sb.append('=');
        sb.append(((this.pcfRecoveryTime == null) ? "<null>" : this.pcfRecoveryTime));
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
        result = ((result * 31) + ((this.pcfRecoveryTime == null) ? 0 : this.pcfRecoveryTime.hashCode()));
        result = ((result * 31) + ((this.diameterRouting == null) ? 0 : this.diameterRouting.hashCode()));
        result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
        result = ((result * 31) + ((this.multipleBindingResolution == null) ? 0 : this.multipleBindingResolution.hashCode()));
        result = ((result * 31) + ((this.bindingTimeout == null) ? 0 : this.bindingTimeout.hashCode()));
        result = ((result * 31) + ((this.nfPool == null) ? 0 : this.nfPool.hashCode()));
        result = ((result * 31) + ((this.bindingDatabase == null) ? 0 : this.bindingDatabase.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof BsfService) == false)
        {
            return false;
        }
        BsfService rhs = ((BsfService) other);
        return ((((((((this.pcfRecoveryTime == rhs.pcfRecoveryTime) || ((this.pcfRecoveryTime != null) && this.pcfRecoveryTime.equals(rhs.pcfRecoveryTime)))
                     && ((this.diameterRouting == rhs.diameterRouting) || ((this.diameterRouting != null) && this.diameterRouting.equals(rhs.diameterRouting))))
                    && ((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name))))
                   && ((this.multipleBindingResolution == rhs.multipleBindingResolution)
                       || ((this.multipleBindingResolution != null) && this.multipleBindingResolution.equals(rhs.multipleBindingResolution))))
                  && ((this.bindingTimeout == rhs.bindingTimeout) || ((this.bindingTimeout != null) && this.bindingTimeout.equals(rhs.bindingTimeout))))
                 && ((this.nfPool == rhs.nfPool) || ((this.nfPool != null) && this.nfPool.equals(rhs.nfPool))))
                && ((this.bindingDatabase == rhs.bindingDatabase) || ((this.bindingDatabase != null) && this.bindingDatabase.equals(rhs.bindingDatabase))));
    }

}
