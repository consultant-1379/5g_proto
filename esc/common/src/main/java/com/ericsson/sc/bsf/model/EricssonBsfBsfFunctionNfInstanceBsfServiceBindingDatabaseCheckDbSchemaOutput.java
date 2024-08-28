
package com.ericsson.sc.bsf.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Output schema for action
 * ericsson-bsf:bsf-function::nf-instance::bsf-service::binding-database::check-db-schema
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "ericsson-bsf:status", "ericsson-bsf:topology" })
public class EricssonBsfBsfFunctionNfInstanceBsfServiceBindingDatabaseCheckDbSchemaOutput
{

    /**
     * Schema status related properties (Required)
     * 
     */
    @JsonProperty("ericsson-bsf:status")
    @JsonPropertyDescription("Schema status related properties")
    private EricssonBsfStatus ericssonBsfStatus;
    /**
     * Schema topology related properties
     * 
     */
    @JsonProperty("ericsson-bsf:topology")
    @JsonPropertyDescription("Schema topology related properties")
    private EricssonBsfTopology ericssonBsfTopology;

    /**
     * Schema status related properties (Required)
     * 
     */
    @JsonProperty("ericsson-bsf:status")
    public EricssonBsfStatus getEricssonBsfStatus()
    {
        return ericssonBsfStatus;
    }

    /**
     * Schema status related properties (Required)
     * 
     */
    @JsonProperty("ericsson-bsf:status")
    public void setEricssonBsfStatus(EricssonBsfStatus ericssonBsfStatus)
    {
        this.ericssonBsfStatus = ericssonBsfStatus;
    }

    public EricssonBsfBsfFunctionNfInstanceBsfServiceBindingDatabaseCheckDbSchemaOutput withEricssonBsfStatus(EricssonBsfStatus ericssonBsfStatus)
    {
        this.ericssonBsfStatus = ericssonBsfStatus;
        return this;
    }

    /**
     * Schema topology related properties
     * 
     */
    @JsonProperty("ericsson-bsf:topology")
    public EricssonBsfTopology getEricssonBsfTopology()
    {
        return ericssonBsfTopology;
    }

    /**
     * Schema topology related properties
     * 
     */
    @JsonProperty("ericsson-bsf:topology")
    public void setEricssonBsfTopology(EricssonBsfTopology ericssonBsfTopology)
    {
        this.ericssonBsfTopology = ericssonBsfTopology;
    }

    public EricssonBsfBsfFunctionNfInstanceBsfServiceBindingDatabaseCheckDbSchemaOutput withEricssonBsfTopology(EricssonBsfTopology ericssonBsfTopology)
    {
        this.ericssonBsfTopology = ericssonBsfTopology;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(EricssonBsfBsfFunctionNfInstanceBsfServiceBindingDatabaseCheckDbSchemaOutput.class.getName())
          .append('@')
          .append(Integer.toHexString(System.identityHashCode(this)))
          .append('[');
        sb.append("ericssonBsfStatus");
        sb.append('=');
        sb.append(((this.ericssonBsfStatus == null) ? "<null>" : this.ericssonBsfStatus));
        sb.append(',');
        sb.append("ericssonBsfTopology");
        sb.append('=');
        sb.append(((this.ericssonBsfTopology == null) ? "<null>" : this.ericssonBsfTopology));
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
        result = ((result * 31) + ((this.ericssonBsfStatus == null) ? 0 : this.ericssonBsfStatus.hashCode()));
        result = ((result * 31) + ((this.ericssonBsfTopology == null) ? 0 : this.ericssonBsfTopology.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof EricssonBsfBsfFunctionNfInstanceBsfServiceBindingDatabaseCheckDbSchemaOutput) == false)
        {
            return false;
        }
        EricssonBsfBsfFunctionNfInstanceBsfServiceBindingDatabaseCheckDbSchemaOutput rhs = ((EricssonBsfBsfFunctionNfInstanceBsfServiceBindingDatabaseCheckDbSchemaOutput) other);
        return (((this.ericssonBsfStatus == rhs.ericssonBsfStatus)
                 || ((this.ericssonBsfStatus != null) && this.ericssonBsfStatus.equals(rhs.ericssonBsfStatus)))
                && ((this.ericssonBsfTopology == rhs.ericssonBsfTopology)
                    || ((this.ericssonBsfTopology != null) && this.ericssonBsfTopology.equals(rhs.ericssonBsfTopology))));
    }

}
