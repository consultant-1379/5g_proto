/*
 * TS 29.122 Common Data Types
 * Data types applicable to several APIs.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29122.commondata;

import java.util.Objects;
import java.util.Arrays;

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
 * Represents an event report.
 */
@ApiModel(description = "Represents an event report.")
@JsonPropertyOrder({ EventReport.JSON_PROPERTY_EVENT, EventReport.JSON_PROPERTY_ACCUMULATED_USAGE, EventReport.JSON_PROPERTY_FLOW_IDS })
public class EventReport
{
    public static final String JSON_PROPERTY_EVENT = "event";
    private String event;

    public static final String JSON_PROPERTY_ACCUMULATED_USAGE = "accumulatedUsage";
    private AccumulatedUsage accumulatedUsage;

    public static final String JSON_PROPERTY_FLOW_IDS = "flowIds";
    private List<Integer> flowIds = null;

    public EventReport()
    {
    }

    public EventReport event(String event)
    {

        this.event = event;
        return this;
    }

    /**
     * Possible values are - SESSION_TERMINATION: Indicates that Rx session is
     * terminated. - LOSS_OF_BEARER : Indicates a loss of a bearer. -
     * RECOVERY_OF_BEARER: Indicates a recovery of a bearer. - RELEASE_OF_BEARER:
     * Indicates a release of a bearer. - USAGE_REPORT: Indicates the usage report
     * event. - FAILED_RESOURCES_ALLOCATION: Indicates the resource allocation is
     * failed. - SUCCESSFUL_RESOURCES_ALLOCATION: Indicates the resource allocation
     * is successful.
     * 
     * @return event
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true,
                      value = "Possible values are - SESSION_TERMINATION: Indicates that Rx session is terminated. - LOSS_OF_BEARER : Indicates a loss of a bearer. - RECOVERY_OF_BEARER: Indicates a recovery of a bearer. - RELEASE_OF_BEARER: Indicates a release of a bearer. - USAGE_REPORT: Indicates the usage report event.  - FAILED_RESOURCES_ALLOCATION: Indicates the resource allocation is failed. - SUCCESSFUL_RESOURCES_ALLOCATION: Indicates the resource allocation is successful. ")
    @JsonProperty(JSON_PROPERTY_EVENT)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getEvent()
    {
        return event;
    }

    @JsonProperty(JSON_PROPERTY_EVENT)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setEvent(String event)
    {
        this.event = event;
    }

    public EventReport accumulatedUsage(AccumulatedUsage accumulatedUsage)
    {

        this.accumulatedUsage = accumulatedUsage;
        return this;
    }

    /**
     * Get accumulatedUsage
     * 
     * @return accumulatedUsage
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_ACCUMULATED_USAGE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public AccumulatedUsage getAccumulatedUsage()
    {
        return accumulatedUsage;
    }

    @JsonProperty(JSON_PROPERTY_ACCUMULATED_USAGE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAccumulatedUsage(AccumulatedUsage accumulatedUsage)
    {
        this.accumulatedUsage = accumulatedUsage;
    }

    public EventReport flowIds(List<Integer> flowIds)
    {

        this.flowIds = flowIds;
        return this;
    }

    public EventReport addFlowIdsItem(Integer flowIdsItem)
    {
        if (this.flowIds == null)
        {
            this.flowIds = new ArrayList<>();
        }
        this.flowIds.add(flowIdsItem);
        return this;
    }

    /**
     * Identifies the IP flows that were sent during event subscription
     * 
     * @return flowIds
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Identifies the IP flows that were sent during event subscription")
    @JsonProperty(JSON_PROPERTY_FLOW_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<Integer> getFlowIds()
    {
        return flowIds;
    }

    @JsonProperty(JSON_PROPERTY_FLOW_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setFlowIds(List<Integer> flowIds)
    {
        this.flowIds = flowIds;
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
        EventReport eventReport = (EventReport) o;
        return Objects.equals(this.event, eventReport.event) && Objects.equals(this.accumulatedUsage, eventReport.accumulatedUsage)
               && Objects.equals(this.flowIds, eventReport.flowIds);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(event, accumulatedUsage, flowIds);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class EventReport {\n");
        sb.append("    event: ").append(toIndentedString(event)).append("\n");
        sb.append("    accumulatedUsage: ").append(toIndentedString(accumulatedUsage)).append("\n");
        sb.append("    flowIds: ").append(toIndentedString(flowIds)).append("\n");
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
