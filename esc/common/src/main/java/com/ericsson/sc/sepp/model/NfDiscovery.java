
package com.ericsson.sc.sepp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Enables the usage of the Nnrf_NfDiscovery service
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "nrf-group-ref" })
public class NfDiscovery
{

    /**
     * The group of NRFs providing the Nnrf_NFDiscovery service (Required)
     * 
     */
    @JsonProperty("nrf-group-ref")
    @JsonPropertyDescription("The group of NRFs providing the Nnrf_NFDiscovery service")
    private String nrfGroupRef;

    /**
     * The group of NRFs providing the Nnrf_NFDiscovery service (Required)
     * 
     */
    @JsonProperty("nrf-group-ref")
    public String getNrfGroupRef()
    {
        return nrfGroupRef;
    }

    /**
     * The group of NRFs providing the Nnrf_NFDiscovery service (Required)
     * 
     */
    @JsonProperty("nrf-group-ref")
    public void setNrfGroupRef(String nrfGroupRef)
    {
        this.nrfGroupRef = nrfGroupRef;
    }

    public NfDiscovery withNrfGroupRef(String nrfGroupRef)
    {
        this.nrfGroupRef = nrfGroupRef;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(NfDiscovery.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("nrfGroupRef");
        sb.append('=');
        sb.append(((this.nrfGroupRef == null) ? "<null>" : this.nrfGroupRef));
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
        result = ((result * 31) + ((this.nrfGroupRef == null) ? 0 : this.nrfGroupRef.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof NfDiscovery) == false)
        {
            return false;
        }
        NfDiscovery rhs = ((NfDiscovery) other);
        return ((this.nrfGroupRef == rhs.nrfGroupRef) || ((this.nrfGroupRef != null) && this.nrfGroupRef.equals(rhs.nrfGroupRef)));
    }

}
