
package com.ericsson.adpal.cm.model;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.utilities.exceptions.Utils;
import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The configuration data
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({

})
public class Data
{
    private static final Logger log = LoggerFactory.getLogger(Data.class);
    private static final ObjectMapper json = Jackson.om(); // create once, reuse

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties()
    {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name,
                                      Object value)
    {
        this.additionalProperties.put(name, value);
    }

    public Data withAdditionalProperty(String name,
                                       Object value)
    {
        this.additionalProperties.put(name, value);
        return this;
    }

    public String toString()
    {
        try
        {
            return json.writeValueAsString(this);
        }
        catch (JsonProcessingException e)
        {
            log.error("Could not convert Data object to String. Cause: {}", Utils.toString(e, log.isDebugEnabled()));
        }

        return "";
    }
}
