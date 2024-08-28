/*
 * Namf_EventExposure
 * AMF Event Exposure Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29518.namf.eventexposure;

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
 * Represents the idle status indication.
 */
@ApiModel(description = "Represents the idle status indication.")
@JsonPropertyOrder({ IdleStatusIndication.JSON_PROPERTY_TIME_STAMP,
                     IdleStatusIndication.JSON_PROPERTY_ACTIVE_TIME,
                     IdleStatusIndication.JSON_PROPERTY_SUBS_REG_TIMER,
                     IdleStatusIndication.JSON_PROPERTY_EDRX_CYCLE_LENGTH,
                     IdleStatusIndication.JSON_PROPERTY_SUGGESTED_NUM_OF_DL_PACKETS })
public class IdleStatusIndication
{
    public static final String JSON_PROPERTY_TIME_STAMP = "timeStamp";
    private OffsetDateTime timeStamp;

    public static final String JSON_PROPERTY_ACTIVE_TIME = "activeTime";
    private Integer activeTime;

    public static final String JSON_PROPERTY_SUBS_REG_TIMER = "subsRegTimer";
    private Integer subsRegTimer;

    public static final String JSON_PROPERTY_EDRX_CYCLE_LENGTH = "edrxCycleLength";
    private Integer edrxCycleLength;

    public static final String JSON_PROPERTY_SUGGESTED_NUM_OF_DL_PACKETS = "suggestedNumOfDlPackets";
    private Integer suggestedNumOfDlPackets;

    public IdleStatusIndication()
    {
    }

    public IdleStatusIndication timeStamp(OffsetDateTime timeStamp)
    {

        this.timeStamp = timeStamp;
        return this;
    }

    /**
     * string with format &#39;date-time&#39; as defined in OpenAPI.
     * 
     * @return timeStamp
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'date-time' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_TIME_STAMP)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getTimeStamp()
    {
        return timeStamp;
    }

    @JsonProperty(JSON_PROPERTY_TIME_STAMP)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTimeStamp(OffsetDateTime timeStamp)
    {
        this.timeStamp = timeStamp;
    }

    public IdleStatusIndication activeTime(Integer activeTime)
    {

        this.activeTime = activeTime;
        return this;
    }

    /**
     * indicating a time in seconds.
     * 
     * @return activeTime
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "indicating a time in seconds.")
    @JsonProperty(JSON_PROPERTY_ACTIVE_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getActiveTime()
    {
        return activeTime;
    }

    @JsonProperty(JSON_PROPERTY_ACTIVE_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setActiveTime(Integer activeTime)
    {
        this.activeTime = activeTime;
    }

    public IdleStatusIndication subsRegTimer(Integer subsRegTimer)
    {

        this.subsRegTimer = subsRegTimer;
        return this;
    }

    /**
     * indicating a time in seconds.
     * 
     * @return subsRegTimer
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "indicating a time in seconds.")
    @JsonProperty(JSON_PROPERTY_SUBS_REG_TIMER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getSubsRegTimer()
    {
        return subsRegTimer;
    }

    @JsonProperty(JSON_PROPERTY_SUBS_REG_TIMER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSubsRegTimer(Integer subsRegTimer)
    {
        this.subsRegTimer = subsRegTimer;
    }

    public IdleStatusIndication edrxCycleLength(Integer edrxCycleLength)
    {

        this.edrxCycleLength = edrxCycleLength;
        return this;
    }

    /**
     * Get edrxCycleLength
     * 
     * @return edrxCycleLength
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_EDRX_CYCLE_LENGTH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getEdrxCycleLength()
    {
        return edrxCycleLength;
    }

    @JsonProperty(JSON_PROPERTY_EDRX_CYCLE_LENGTH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEdrxCycleLength(Integer edrxCycleLength)
    {
        this.edrxCycleLength = edrxCycleLength;
    }

    public IdleStatusIndication suggestedNumOfDlPackets(Integer suggestedNumOfDlPackets)
    {

        this.suggestedNumOfDlPackets = suggestedNumOfDlPackets;
        return this;
    }

    /**
     * Get suggestedNumOfDlPackets
     * 
     * @return suggestedNumOfDlPackets
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SUGGESTED_NUM_OF_DL_PACKETS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getSuggestedNumOfDlPackets()
    {
        return suggestedNumOfDlPackets;
    }

    @JsonProperty(JSON_PROPERTY_SUGGESTED_NUM_OF_DL_PACKETS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSuggestedNumOfDlPackets(Integer suggestedNumOfDlPackets)
    {
        this.suggestedNumOfDlPackets = suggestedNumOfDlPackets;
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
        IdleStatusIndication idleStatusIndication = (IdleStatusIndication) o;
        return Objects.equals(this.timeStamp, idleStatusIndication.timeStamp) && Objects.equals(this.activeTime, idleStatusIndication.activeTime)
               && Objects.equals(this.subsRegTimer, idleStatusIndication.subsRegTimer)
               && Objects.equals(this.edrxCycleLength, idleStatusIndication.edrxCycleLength)
               && Objects.equals(this.suggestedNumOfDlPackets, idleStatusIndication.suggestedNumOfDlPackets);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(timeStamp, activeTime, subsRegTimer, edrxCycleLength, suggestedNumOfDlPackets);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class IdleStatusIndication {\n");
        sb.append("    timeStamp: ").append(toIndentedString(timeStamp)).append("\n");
        sb.append("    activeTime: ").append(toIndentedString(activeTime)).append("\n");
        sb.append("    subsRegTimer: ").append(toIndentedString(subsRegTimer)).append("\n");
        sb.append("    edrxCycleLength: ").append(toIndentedString(edrxCycleLength)).append("\n");
        sb.append("    suggestedNumOfDlPackets: ").append(toIndentedString(suggestedNumOfDlPackets)).append("\n");
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
