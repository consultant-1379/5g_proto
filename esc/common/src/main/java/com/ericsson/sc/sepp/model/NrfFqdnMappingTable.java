
package com.ericsson.sc.sepp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "internalFqdn", "externalFqdn" })
public class NrfFqdnMappingTable
{

    /**
     * The FQDN used in the internal network. (Required)
     * 
     */
    @JsonProperty("internalFqdn")
    @JsonPropertyDescription("The FQDN used in the internal network.")
    private String internalFqdn;
    /**
     * The new value to map the FQDN to, in order to be used by the Roaming
     * Parteners (Required)
     * 
     */
    @JsonProperty("externalFqdn")
    @JsonPropertyDescription("The new value to map the FQDN to, in order to be used by the Roaming Parteners")
    private String externalFqdn;

    /**
     * The FQDN used in the internal network. (Required)
     * 
     */
    @JsonProperty("internalFqdn")
    public String getInternalFqdn()
    {
        return internalFqdn;
    }

    /**
     * The FQDN used in the internal network. (Required)
     * 
     */
    @JsonProperty("internalFqdn")
    public void setInternalFqdn(String internalFqdn)
    {
        this.internalFqdn = internalFqdn;
    }

    public NrfFqdnMappingTable withInternalFqdn(String internalFqdn)
    {
        this.internalFqdn = internalFqdn;
        return this;
    }

    /**
     * The new value to map the FQDN to, in order to be used by the Roaming
     * Parteners (Required)
     * 
     */
    @JsonProperty("externalFqdn")
    public String getExternalFqdn()
    {
        return externalFqdn;
    }

    /**
     * The new value to map the FQDN to, in order to be used by the Roaming
     * Parteners (Required)
     * 
     */
    @JsonProperty("externalFqdn")
    public void setExternalFqdn(String externalFqdn)
    {
        this.externalFqdn = externalFqdn;
    }

    public NrfFqdnMappingTable withExternalFqdn(String externalFqdn)
    {
        this.externalFqdn = externalFqdn;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(NrfFqdnMappingTable.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("internalFqdn");
        sb.append('=');
        sb.append(((this.internalFqdn == null) ? "<null>" : this.internalFqdn));
        sb.append(',');
        sb.append("externalFqdn");
        sb.append('=');
        sb.append(((this.externalFqdn == null) ? "<null>" : this.externalFqdn));
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
        result = ((result * 31) + ((this.externalFqdn == null) ? 0 : this.externalFqdn.hashCode()));
        result = ((result * 31) + ((this.internalFqdn == null) ? 0 : this.internalFqdn.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof NrfFqdnMappingTable) == false)
        {
            return false;
        }
        NrfFqdnMappingTable rhs = ((NrfFqdnMappingTable) other);
        return (((this.externalFqdn == rhs.externalFqdn) || ((this.externalFqdn != null) && this.externalFqdn.equals(rhs.externalFqdn)))
                && ((this.internalFqdn == rhs.internalFqdn) || ((this.internalFqdn != null) && this.internalFqdn.equals(rhs.internalFqdn))));
    }

}
