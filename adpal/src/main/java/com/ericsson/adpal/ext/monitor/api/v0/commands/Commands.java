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

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Monitor Commands Request Schema
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "command", "results" })
public class Commands
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("command")
    private Command command;
    @JsonProperty("results")
    private List<Result> results = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Commands()
    {
    }

    /**
     * 
     * @param results
     * @param command
     */
    public Commands(Command command,
                    List<Result> results)
    {
        super();
        this.command = command;
        this.results = results;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("command")
    public Command getCommand()
    {
        return command;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("command")
    public void setCommand(Command command)
    {
        this.command = command;
    }

    @JsonProperty("results")
    public List<Result> getResults()
    {
        return results;
    }

    @JsonProperty("results")
    public void setResults(List<Result> results)
    {
        this.results = results;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).append("command", command).append("results", results).toString();
    }

}
