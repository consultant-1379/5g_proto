
package com.ericsson.sc.bsf.model;

import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * ericsson-bsf
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "ericsson-bsf:bsf-function" })
public class EricssonBsf
{
    @JsonIgnore
    private static final ObjectMapper json = Jackson.om();

    /**
     * Configuration settings for the Binding Support Function
     * 
     */
    @JsonProperty("ericsson-bsf:bsf-function")
    @JsonPropertyDescription("Configuration settings for the Binding Support Function")
    private EricssonBsfBsfFunction ericssonBsfBsfFunction;

    /**
     * Configuration settings for the Binding Support Function
     * 
     */
    @JsonProperty("ericsson-bsf:bsf-function")
    public EricssonBsfBsfFunction getEricssonBsfBsfFunction()
    {
        return ericssonBsfBsfFunction;
    }

    /**
     * Configuration settings for the Binding Support Function
     * 
     */
    @JsonProperty("ericsson-bsf:bsf-function")
    public void setEricssonBsfBsfFunction(EricssonBsfBsfFunction ericssonBsfBsfFunction)
    {
        this.ericssonBsfBsfFunction = ericssonBsfBsfFunction;
    }

    public EricssonBsf withEricssonBsfBsfFunction(EricssonBsfBsfFunction ericssonBsfBsfFunction)
    {
        this.ericssonBsfBsfFunction = ericssonBsfBsfFunction;
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
        result = ((result * 31) + ((this.ericssonBsfBsfFunction == null) ? 0 : this.ericssonBsfBsfFunction.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof EricssonBsf) == false)
        {
            return false;
        }
        EricssonBsf rhs = ((EricssonBsf) other);
        return ((this.ericssonBsfBsfFunction == rhs.ericssonBsfBsfFunction)
                || ((this.ericssonBsfBsfFunction != null) && this.ericssonBsfBsfFunction.equals(rhs.ericssonBsfBsfFunction)));
    }

}
