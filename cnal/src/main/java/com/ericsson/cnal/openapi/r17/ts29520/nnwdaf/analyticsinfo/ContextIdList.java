/*
 * Nnwdaf_AnalyticsInfo
 * Nnwdaf_AnalyticsInfo Service API.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29520.nnwdaf.analyticsinfo;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29520.nnwdaf.eventssubscription.AnalyticsContextIdentifier;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Contains a list of context identifiers of context information of analytics
 * subscriptions.
 */
@ApiModel(description = "Contains a list of context identifiers of context information of analytics subscriptions. ")
@JsonPropertyOrder({ ContextIdList.JSON_PROPERTY_CONTEXT_IDS })
public class ContextIdList
{
    public static final String JSON_PROPERTY_CONTEXT_IDS = "contextIds";
    private List<AnalyticsContextIdentifier> contextIds = new ArrayList<>();

    public ContextIdList()
    {
    }

    public ContextIdList contextIds(List<AnalyticsContextIdentifier> contextIds)
    {

        this.contextIds = contextIds;
        return this;
    }

    public ContextIdList addContextIdsItem(AnalyticsContextIdentifier contextIdsItem)
    {
        this.contextIds.add(contextIdsItem);
        return this;
    }

    /**
     * Get contextIds
     * 
     * @return contextIds
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_CONTEXT_IDS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public List<AnalyticsContextIdentifier> getContextIds()
    {
        return contextIds;
    }

    @JsonProperty(JSON_PROPERTY_CONTEXT_IDS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setContextIds(List<AnalyticsContextIdentifier> contextIds)
    {
        this.contextIds = contextIds;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        ContextIdList contextIdList = (ContextIdList) o;
        return Objects.equals(this.contextIds, contextIdList.contextIds);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(contextIds);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class ContextIdList {\n");
        sb.append("    contextIds: ").append(toIndentedString(contextIds)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o)
    {
        if (o == null)
        {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

}
