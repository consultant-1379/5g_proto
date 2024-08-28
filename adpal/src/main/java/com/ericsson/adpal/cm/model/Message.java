
package com.ericsson.adpal.cm.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Message
 * <p>
 * Definition of a message
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "message" })
public class Message
{

    /**
     * A readable message that describes the result of a certain action (Required)
     *
     */
    @JsonProperty("message")
    @JsonPropertyDescription("A readable message that describes the result of a certain action")
    private String message;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * A readable message that describes the result of a certain action (Required)
     *
     */
    @JsonProperty("message")
    public String getMessage()
    {
        return message;
    }

    /**
     * A readable message that describes the result of a certain action (Required)
     *
     */
    @JsonProperty("message")
    public void setMessage(String message)
    {
        this.message = message;
    }

    public Message withMessage(String message)
    {
        this.message = message;
        return this;
    }

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

    public Message withAdditionalProperty(String name,
                                          Object value)
    {
        this.additionalProperties.put(name, value);
        return this;
    }

}
