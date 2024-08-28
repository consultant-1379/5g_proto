
package com.ericsson.sc.scp.model;

import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * ericsson-scp
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "ericsson-scp:scp-function" })
public class EricssonScp
{
    @JsonIgnore
    private static final ObjectMapper json = Jackson.om();

    /**
     * Configuration settings for the Service Communication Proxy NF
     * 
     */
    @JsonProperty("ericsson-scp:scp-function")
    @JsonPropertyDescription("Configuration settings for the Service Communication Proxy NF")
    private EricssonScpScpFunction ericssonScpScpFunction;

    /**
     * Configuration settings for the Service Communication Proxy NF
     * 
     */
    @JsonProperty("ericsson-scp:scp-function")
    public EricssonScpScpFunction getEricssonScpScpFunction()
    {
        return ericssonScpScpFunction;
    }

    /**
     * Configuration settings for the Service Communication Proxy NF
     * 
     */
    @JsonProperty("ericsson-scp:scp-function")
    public void setEricssonScpScpFunction(EricssonScpScpFunction ericssonScpScpFunction)
    {
        this.ericssonScpScpFunction = ericssonScpScpFunction;
    }

    public EricssonScp withEricssonScpScpFunction(EricssonScpScpFunction ericssonScpScpFunction)
    {
        this.ericssonScpScpFunction = ericssonScpScpFunction;
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
        result = ((result * 31) + ((this.ericssonScpScpFunction == null) ? 0 : this.ericssonScpScpFunction.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof EricssonScp) == false)
        {
            return false;
        }
        EricssonScp rhs = ((EricssonScp) other);
        return ((this.ericssonScpScpFunction == rhs.ericssonScpScpFunction)
                || ((this.ericssonScpScpFunction != null) && this.ericssonScpScpFunction.equals(rhs.ericssonScpScpFunction)));
    }

}
