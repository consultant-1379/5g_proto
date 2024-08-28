/**
 * COPYRIGHT ERICSSON GMBH 2018
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Dec 10, 2018
 *     Author: eedstl
 */

package com.ericsson.adpal.ext.monitor.api.v0.commands;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "timeStamp", "id" })
public class Command
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("timeStamp")
    private Long timeStamp;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    private String id;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Command()
    {
    }

    /**
     * 
     * @param id
     * @param timeStamp
     */
    public Command(Long timeStamp,
                   String id)
    {
        super();
        this.timeStamp = timeStamp;
        this.id = id;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("timeStamp")
    public Long getTimeStamp()
    {
        return timeStamp;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("timeStamp")
    public void setTimeStamp(Long timeStamp)
    {
        this.timeStamp = timeStamp;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    public String getId()
    {
        return id;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(String id)
    {
        this.id = id;
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

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).append("timeStamp", timeStamp).append("id", id).append("additionalProperties", additionalProperties).toString();
    }

}
