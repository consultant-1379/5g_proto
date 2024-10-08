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
import java.time.OffsetDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Represents a time window identified by a start time and a stop time.
 */
@ApiModel(description = "Represents a time window identified by a start time and a stop time.")
@JsonPropertyOrder({ TimeWindow.JSON_PROPERTY_START_TIME, TimeWindow.JSON_PROPERTY_STOP_TIME })
public class TimeWindow
{
    public static final String JSON_PROPERTY_START_TIME = "startTime";
    private OffsetDateTime startTime;

    public static final String JSON_PROPERTY_STOP_TIME = "stopTime";
    private OffsetDateTime stopTime;

    public TimeWindow()
    {
    }

    public TimeWindow startTime(OffsetDateTime startTime)
    {

        this.startTime = startTime;
        return this;
    }

    /**
     * string with format \&quot;date-time\&quot; as defined in OpenAPI.
     * 
     * @return startTime
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "string with format \"date-time\" as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_START_TIME)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getStartTime()
    {
        return startTime;
    }

    @JsonProperty(JSON_PROPERTY_START_TIME)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setStartTime(OffsetDateTime startTime)
    {
        this.startTime = startTime;
    }

    public TimeWindow stopTime(OffsetDateTime stopTime)
    {

        this.stopTime = stopTime;
        return this;
    }

    /**
     * string with format \&quot;date-time\&quot; as defined in OpenAPI.
     * 
     * @return stopTime
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "string with format \"date-time\" as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_STOP_TIME)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getStopTime()
    {
        return stopTime;
    }

    @JsonProperty(JSON_PROPERTY_STOP_TIME)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setStopTime(OffsetDateTime stopTime)
    {
        this.stopTime = stopTime;
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
        TimeWindow timeWindow = (TimeWindow) o;
        return Objects.equals(this.startTime, timeWindow.startTime) && Objects.equals(this.stopTime, timeWindow.stopTime);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(startTime, stopTime);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class TimeWindow {\n");
        sb.append("    startTime: ").append(toIndentedString(startTime)).append("\n");
        sb.append("    stopTime: ").append(toIndentedString(stopTime)).append("\n");
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
