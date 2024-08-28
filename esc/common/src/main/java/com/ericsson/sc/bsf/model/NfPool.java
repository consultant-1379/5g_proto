
package com.ericsson.sc.bsf.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "user-label", "nf-pool-discovery" })
public class NfPool
{

    /**
     * Name identifying the nf-pool (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name identifying the nf-pool")
    private String name;
    /**
     * Label for free use
     * 
     */
    @JsonProperty("user-label")
    @JsonPropertyDescription("Label for free use")
    private String userLabel;
    /**
     * Attributes to define discovery of NF instances
     * 
     */
    @JsonProperty("nf-pool-discovery")
    @JsonPropertyDescription("Attributes to define discovery of NF instances")
    private List<NfPoolDiscovery> nfPoolDiscovery = new ArrayList<NfPoolDiscovery>();

    /**
     * Name identifying the nf-pool (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name identifying the nf-pool (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public NfPool withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Label for free use
     * 
     */
    @JsonProperty("user-label")
    public String getUserLabel()
    {
        return userLabel;
    }

    /**
     * Label for free use
     * 
     */
    @JsonProperty("user-label")
    public void setUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
    }

    public NfPool withUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
        return this;
    }

    /**
     * Attributes to define discovery of NF instances
     * 
     */
    @JsonProperty("nf-pool-discovery")
    public List<NfPoolDiscovery> getNfPoolDiscovery()
    {
        return nfPoolDiscovery;
    }

    /**
     * Attributes to define discovery of NF instances
     * 
     */
    @JsonProperty("nf-pool-discovery")
    public void setNfPoolDiscovery(List<NfPoolDiscovery> nfPoolDiscovery)
    {
        this.nfPoolDiscovery = nfPoolDiscovery;
    }

    public NfPool withNfPoolDiscovery(List<NfPoolDiscovery> nfPoolDiscovery)
    {
        this.nfPoolDiscovery = nfPoolDiscovery;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(NfPool.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("userLabel");
        sb.append('=');
        sb.append(((this.userLabel == null) ? "<null>" : this.userLabel));
        sb.append(',');
        sb.append("nfPoolDiscovery");
        sb.append('=');
        sb.append(((this.nfPoolDiscovery == null) ? "<null>" : this.nfPoolDiscovery));
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
        result = ((result * 31) + ((this.nfPoolDiscovery == null) ? 0 : this.nfPoolDiscovery.hashCode()));
        result = ((result * 31) + ((this.userLabel == null) ? 0 : this.userLabel.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof NfPool) == false)
        {
            return false;
        }
        NfPool rhs = ((NfPool) other);
        return ((((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
                 && ((this.nfPoolDiscovery == rhs.nfPoolDiscovery) || ((this.nfPoolDiscovery != null) && this.nfPoolDiscovery.equals(rhs.nfPoolDiscovery))))
                && ((this.userLabel == rhs.userLabel) || ((this.userLabel != null) && this.userLabel.equals(rhs.userLabel))));
    }

}
