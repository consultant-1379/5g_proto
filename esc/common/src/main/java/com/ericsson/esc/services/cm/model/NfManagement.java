
package com.ericsson.esc.services.cm.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "nrf-group-ref" })
public class NfManagement
{

    /**
     * The groups of NRFs providing the Nnrf_NFManagement service (Required)
     * 
     */
    @JsonProperty("nrf-group-ref")
    @JsonPropertyDescription("The groups of NRFs providing the Nnrf_NFManagement service")
    private List<String> nrfGroupRef = new ArrayList<String>();

    /**
     * The groups of NRFs providing the Nnrf_NFManagement service (Required)
     * 
     */
    @JsonProperty("nrf-group-ref")
    public List<String> getNrfGroupRef()
    {
        return nrfGroupRef;
    }

    /**
     * The groups of NRFs providing the Nnrf_NFManagement service (Required)
     * 
     */
    @JsonProperty("nrf-group-ref")
    public void setNrfGroupRef(List<String> nrfGroupRef)
    {
        this.nrfGroupRef = nrfGroupRef;
    }

    public NfManagement withNrfGroupRef(List<String> nrfGroupRef)
    {
        this.nrfGroupRef = nrfGroupRef;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(NfManagement.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
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
        if ((other instanceof NfManagement) == false)
        {
            return false;
        }
        NfManagement rhs = ((NfManagement) other);
        return ((this.nrfGroupRef == rhs.nrfGroupRef) || ((this.nrfGroupRef != null) && this.nrfGroupRef.equals(rhs.nrfGroupRef)));
    }

}
