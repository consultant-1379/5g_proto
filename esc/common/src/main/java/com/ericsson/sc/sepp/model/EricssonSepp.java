
package com.ericsson.sc.sepp.model;

import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * ericsson-sepp
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "ericsson-sepp:sepp-function" })
public class EricssonSepp
{
    @JsonIgnore
    private static final ObjectMapper json = Jackson.om();

    /**
     * Configuration settings for the Security Edge Protection Proxy NF
     * 
     */
    @JsonProperty("ericsson-sepp:sepp-function")
    @JsonPropertyDescription("Configuration settings for the Security Edge Protection Proxy NF")
    private EricssonSeppSeppFunction ericssonSeppSeppFunction;

    /**
     * Configuration settings for the Security Edge Protection Proxy NF
     * 
     */
    @JsonProperty("ericsson-sepp:sepp-function")
    public EricssonSeppSeppFunction getEricssonSeppSeppFunction()
    {
        return ericssonSeppSeppFunction;
    }

    /**
     * Configuration settings for the Security Edge Protection Proxy NF
     * 
     */
    @JsonProperty("ericsson-sepp:sepp-function")
    public void setEricssonSeppSeppFunction(EricssonSeppSeppFunction ericssonSeppSeppFunction)
    {
        this.ericssonSeppSeppFunction = ericssonSeppSeppFunction;
    }

    public EricssonSepp withEricssonSeppSeppFunction(EricssonSeppSeppFunction ericssonSeppSeppFunction)
    {
        this.ericssonSeppSeppFunction = ericssonSeppSeppFunction;
        return this;
    }

    @Override
    public String toString()
    {
        try
        {
            return json.writeValueAsString(this);
        }
        catch (JsonProcessingException e)
        {
            return e.toString();
        }
    }

    @Override
    public int hashCode()
    {
        int result = 1;
        result = ((result * 31) + ((this.ericssonSeppSeppFunction == null) ? 0 : this.ericssonSeppSeppFunction.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof EricssonSepp) == false)
        {
            return false;
        }
        EricssonSepp rhs = ((EricssonSepp) other);
        return ((this.ericssonSeppSeppFunction == rhs.ericssonSeppSeppFunction)
                || ((this.ericssonSeppSeppFunction != null) && this.ericssonSeppSeppFunction.equals(rhs.ericssonSeppSeppFunction)));
    }

}
