
package com.ericsson.adpal.ext.monitor.api.v0.register;

import java.net.URI;
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

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "operation", "source", "uri" })
public class Callback
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("operation")
    private String operation;
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
    @JsonProperty("uri")
    private URI uri;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Callback()
    {
    }

    /**
     * 
     * @param operation
     * @param source
     * @param uri
     */
    public Callback(String operation,
                    String source,
                    URI uri)
    {
        super();
        this.operation = operation;
        this.source = source;
        this.uri = uri;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("operation")
    public String getOperation()
    {
        return operation;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("operation")
    public void setOperation(String operation)
    {
        this.operation = operation;
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
    @JsonProperty("uri")
    public URI getUri()
    {
        return uri;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("uri")
    public void setUri(URI uri)
    {
        this.uri = uri;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).append("operation", operation).append("source", source).append("uri", uri).toString();
    }

}
