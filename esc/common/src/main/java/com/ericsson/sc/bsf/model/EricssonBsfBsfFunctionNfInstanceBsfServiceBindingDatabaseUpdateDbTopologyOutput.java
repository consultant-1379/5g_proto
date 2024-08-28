
package com.ericsson.sc.bsf.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Output schema for action
 * ericsson-bsf:bsf-function::nf-instance::bsf-service::binding-database::update-db-topology
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "ericsson-bsf:info" })
public class EricssonBsfBsfFunctionNfInstanceBsfServiceBindingDatabaseUpdateDbTopologyOutput
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("ericsson-bsf:info")
    private String ericssonBsfInfo;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("ericsson-bsf:info")
    public String getEricssonBsfInfo()
    {
        return ericssonBsfInfo;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("ericsson-bsf:info")
    public void setEricssonBsfInfo(String ericssonBsfInfo)
    {
        this.ericssonBsfInfo = ericssonBsfInfo;
    }

    public EricssonBsfBsfFunctionNfInstanceBsfServiceBindingDatabaseUpdateDbTopologyOutput withEricssonBsfInfo(String ericssonBsfInfo)
    {
        this.ericssonBsfInfo = ericssonBsfInfo;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(EricssonBsfBsfFunctionNfInstanceBsfServiceBindingDatabaseUpdateDbTopologyOutput.class.getName())
          .append('@')
          .append(Integer.toHexString(System.identityHashCode(this)))
          .append('[');
        sb.append("ericssonBsfInfo");
        sb.append('=');
        sb.append(((this.ericssonBsfInfo == null) ? "<null>" : this.ericssonBsfInfo));
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
        result = ((result * 31) + ((this.ericssonBsfInfo == null) ? 0 : this.ericssonBsfInfo.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof EricssonBsfBsfFunctionNfInstanceBsfServiceBindingDatabaseUpdateDbTopologyOutput) == false)
        {
            return false;
        }
        EricssonBsfBsfFunctionNfInstanceBsfServiceBindingDatabaseUpdateDbTopologyOutput rhs = ((EricssonBsfBsfFunctionNfInstanceBsfServiceBindingDatabaseUpdateDbTopologyOutput) other);
        return ((this.ericssonBsfInfo == rhs.ericssonBsfInfo) || ((this.ericssonBsfInfo != null) && this.ericssonBsfInfo.equals(rhs.ericssonBsfInfo)));
    }

}
