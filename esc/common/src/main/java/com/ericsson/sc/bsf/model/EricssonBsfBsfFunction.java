
package com.ericsson.sc.bsf.model;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.sc.glue.IfNfFunction;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Configuration settings for the Binding Support Function
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "nf-instance" })
public class EricssonBsfBsfFunction implements IfNfFunction
{

    /**
     * The BSF instance defines the behavior of the BSF NF as well as the NRF
     * registration behavior
     * 
     */
    @JsonProperty("nf-instance")
    @JsonPropertyDescription("The BSF instance defines the behavior of the BSF NF as well as the NRF registration behavior")
    private List<NfInstance> nfInstance = new ArrayList<NfInstance>();

    /**
     * The BSF instance defines the behavior of the BSF NF as well as the NRF
     * registration behavior
     * 
     */
    @JsonProperty("nf-instance")
    public List<NfInstance> getNfInstance()
    {
        return nfInstance;
    }

    /**
     * The BSF instance defines the behavior of the BSF NF as well as the NRF
     * registration behavior
     * 
     */
    @JsonProperty("nf-instance")
    public void setNfInstance(List<NfInstance> nfInstance)
    {
        this.nfInstance = nfInstance;
    }

    public EricssonBsfBsfFunction withNfInstance(List<NfInstance> nfInstance)
    {
        this.nfInstance = nfInstance;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(EricssonBsfBsfFunction.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("nfInstance");
        sb.append('=');
        sb.append(((this.nfInstance == null) ? "<null>" : this.nfInstance));
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
        result = ((result * 31) + ((this.nfInstance == null) ? 0 : this.nfInstance.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof EricssonBsfBsfFunction) == false)
        {
            return false;
        }
        EricssonBsfBsfFunction rhs = ((EricssonBsfBsfFunction) other);
        return ((this.nfInstance == rhs.nfInstance) || ((this.nfInstance != null) && this.nfInstance.equals(rhs.nfInstance)));
    }

}
