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
 * Created on: Nov 15, 2018
 *     Author: eedstl
 */

package com.ericsson.adpal.ext.monitor.api.v0.register;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Monitor Register Schema
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "callbacks", "result" })
public class Register
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    private String id;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("callbacks")
    private List<Callback> callbacks = null;
    @JsonProperty("result")
    private String result;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Register()
    {
    }

    /**
     * 
     * @param id
     * @param result
     * @param callbacks
     */
    public Register(String id,
                    List<Callback> callbacks,
                    String result)
    {
        super();
        this.id = id;
        this.callbacks = callbacks;
        this.result = result;
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

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("callbacks")
    public List<Callback> getCallbacks()
    {
        return callbacks;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("callbacks")
    public void setCallbacks(List<Callback> callbacks)
    {
        this.callbacks = callbacks;
    }

    @JsonProperty("result")
    public String getResult()
    {
        return result;
    }

    @JsonProperty("result")
    public void setResult(String result)
    {
        this.result = result;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).append("id", id).append("callbacks", callbacks).append("result", result).toString();
    }

}
