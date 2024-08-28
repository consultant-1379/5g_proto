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
 * Created on: Dec 6, 2018
 *     Author: eedstl
 */

package com.ericsson.adpal.ext.monitor.api.v0.commands;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Monitor Counter Schema
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "help", "instances" })
public class Counter
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    private String name;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("help")
    private String help;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("instances")
    private List<Instance> instances = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Counter()
    {
    }

    /**
     * 
     * @param help
     * @param name
     * @param instances
     */
    public Counter(String name,
                   String help,
                   List<Instance> instances)
    {
        super();
        this.name = name;
        this.help = help;
        this.instances = instances;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("help")
    public String getHelp()
    {
        return help;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("help")
    public void setHelp(String help)
    {
        this.help = help;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("instances")
    public List<Instance> getInstances()
    {
        return instances;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("instances")
    public void setInstances(List<Instance> instances)
    {
        this.instances = instances;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).append("name", name).append("help", help).append("instances", instances).toString();
    }

}
