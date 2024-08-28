/*
 * Nudm_EE
 * Nudm Event Exposure Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29503.nudm.ee;

import java.util.Objects;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Contains the event type and failed cause of the failed Monitoring
 * Configuration in the EE subscription
 */
@ApiModel(description = "Contains the event type and failed cause of the failed Monitoring Configuration in the EE subscription")
@JsonPropertyOrder({ FailedMonitoringConfiguration.JSON_PROPERTY_EVENT_TYPE, FailedMonitoringConfiguration.JSON_PROPERTY_FAILED_CAUSE })
public class FailedMonitoringConfiguration
{
    public static final String JSON_PROPERTY_EVENT_TYPE = "eventType";
    private String eventType;

    public static final String JSON_PROPERTY_FAILED_CAUSE = "failedCause";
    private String failedCause;

    public FailedMonitoringConfiguration()
    {
    }

    public FailedMonitoringConfiguration eventType(String eventType)
    {

        this.eventType = eventType;
        return this;
    }

    /**
     * Get eventType
     * 
     * @return eventType
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_EVENT_TYPE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getEventType()
    {
        return eventType;
    }

    @JsonProperty(JSON_PROPERTY_EVENT_TYPE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setEventType(String eventType)
    {
        this.eventType = eventType;
    }

    public FailedMonitoringConfiguration failedCause(String failedCause)
    {

        this.failedCause = failedCause;
        return this;
    }

    /**
     * Indicates the Failed cause of the failed Monitoring Configuration in the EE
     * subscription
     * 
     * @return failedCause
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Indicates the Failed cause of the failed Monitoring Configuration in the EE subscription")
    @JsonProperty(JSON_PROPERTY_FAILED_CAUSE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getFailedCause()
    {
        return failedCause;
    }

    @JsonProperty(JSON_PROPERTY_FAILED_CAUSE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setFailedCause(String failedCause)
    {
        this.failedCause = failedCause;
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
        FailedMonitoringConfiguration failedMonitoringConfiguration = (FailedMonitoringConfiguration) o;
        return Objects.equals(this.eventType, failedMonitoringConfiguration.eventType)
               && Objects.equals(this.failedCause, failedMonitoringConfiguration.failedCause);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(eventType, failedCause);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class FailedMonitoringConfiguration {\n");
        sb.append("    eventType: ").append(toIndentedString(eventType)).append("\n");
        sb.append("    failedCause: ").append(toIndentedString(failedCause)).append("\n");
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
