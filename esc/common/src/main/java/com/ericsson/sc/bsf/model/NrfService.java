
package com.ericsson.sc.bsf.model;

import com.ericsson.sc.glue.IfNrfService;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Defines which NRF services are used by this NF instance, referencing groups
 * of NRFs that provide the respective service.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "nf-management", "nf-discovery" })
public class NrfService implements IfNrfService
{

    @JsonProperty("nf-management")
    private NfManagement nfManagement;
    @JsonProperty("nf-discovery")
    private NfDiscovery nfDiscovery;

    @JsonProperty("nf-management")
    public NfManagement getNfManagement()
    {
        return nfManagement;
    }

    @JsonProperty("nf-management")
    public void setNfManagement(NfManagement nfManagement)
    {
        this.nfManagement = nfManagement;
    }

    public NrfService withNfManagement(NfManagement nfManagement)
    {
        this.nfManagement = nfManagement;
        return this;
    }

    @JsonProperty("nf-discovery")
    public NfDiscovery getNfDiscovery()
    {
        return nfDiscovery;
    }

    @JsonProperty("nf-discovery")
    public void setNfDiscovery(NfDiscovery nfDiscovery)
    {
        this.nfDiscovery = nfDiscovery;
    }

    public NrfService withNfDiscovery(NfDiscovery nfDiscovery)
    {
        this.nfDiscovery = nfDiscovery;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(NrfService.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("nfManagement");
        sb.append('=');
        sb.append(((this.nfManagement == null) ? "<null>" : this.nfManagement));
        sb.append(',');
        sb.append("nfDiscovery");
        sb.append('=');
        sb.append(((this.nfDiscovery == null) ? "<null>" : this.nfDiscovery));
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
        result = ((result * 31) + ((this.nfDiscovery == null) ? 0 : this.nfDiscovery.hashCode()));
        result = ((result * 31) + ((this.nfManagement == null) ? 0 : this.nfManagement.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof NrfService) == false)
        {
            return false;
        }
        NrfService rhs = ((NrfService) other);
        return (((this.nfDiscovery == rhs.nfDiscovery) || ((this.nfDiscovery != null) && this.nfDiscovery.equals(rhs.nfDiscovery)))
                && ((this.nfManagement == rhs.nfManagement) || ((this.nfManagement != null) && this.nfManagement.equals(rhs.nfManagement))));
    }

}
