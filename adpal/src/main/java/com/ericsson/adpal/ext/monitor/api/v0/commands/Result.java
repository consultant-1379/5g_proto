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

import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "timeStamp", "source", "statusCode" })
public class Result
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
    @JsonProperty("source")
    private String source;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("statusCode")
    private Integer statusCode;
    @JsonIgnore
    private SortedMap<String, Object> additionalProperties = new TreeMap<>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Result()
    {
    }

    /**
     * 
     * @param statusCode
     * @param source
     * @param timeStamp
     */
    public Result(Long timeStamp,
                  String source,
                  Integer statusCode)
    {
        super();
        this.timeStamp = timeStamp;
        this.source = source;
        this.statusCode = statusCode;
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
    @JsonProperty("source")
    public String getSource()
    {
        return source;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("source")
    public void setSource(String source)
    {
        this.source = source;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("statusCode")
    public Integer getStatusCode()
    {
        return statusCode;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("statusCode")
    public void setStatusCode(Integer statusCode)
    {
        this.statusCode = statusCode;
    }

    @JsonAnyGetter
    public SortedMap<String, Object> getAdditionalProperties()
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
        return new ToStringBuilder(this).append("timeStamp", timeStamp)
                                        .append("source", source)
                                        .append("statusCode", statusCode)
                                        .append("additionalProperties", additionalProperties)
                                        .toString();
    }

}
