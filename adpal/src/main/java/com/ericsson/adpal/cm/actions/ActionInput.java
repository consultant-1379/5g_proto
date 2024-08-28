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
 * Created on: Nov 17, 2020
 *     Author: echfari
 */
package com.ericsson.adpal.cm.actions;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Represents the input and context of a pending YANG action
 */
public class ActionInput
{

    private final String configurationName;
    private final String context;
    private final String eTag;
    private final JsonNode input;

    public static ActionInput fromJson(JsonNode json)
    {
        final var configurationName = json.get("configurationName").asText();
        final var context = json.get("context").asText();
        final var eTag = json.get("configETag").asText();
        final var input = json.get("input");

        return new ActionInput(configurationName, context, eTag, input);
    }

    public ActionInput(String configurationName,
                       String context,
                       String eTag,
                       JsonNode input)
    {
        super();
        this.configurationName = configurationName;
        this.context = context;
        this.eTag = eTag;
        this.input = input;
    }

    public String getConfigurationName()
    {
        return configurationName;
    }

    public String getContext()
    {
        return context;
    }

    public String geteTag()
    {
        return eTag;
    }

    public JsonNode getInput()
    {
        return input;
    }

    @Override
    public String toString()
    {
        var builder = new StringBuilder();
        builder.append("ActionInput [configurationName=");
        builder.append(configurationName);
        builder.append(", context=");
        builder.append(context);
        builder.append(", eTag=");
        builder.append(eTag);
        builder.append(", input=");
        builder.append(input);
        builder.append("]");
        return builder.toString();
    }

}
