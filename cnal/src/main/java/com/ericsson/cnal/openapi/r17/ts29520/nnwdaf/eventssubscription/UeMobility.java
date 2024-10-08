/*
 * Nnwdaf_EventsSubscription
 * Nnwdaf_EventsSubscription Service API.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29520.nnwdaf.eventssubscription;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29122.cpprovisioning.ScheduledCommunicationTime;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.OffsetDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Represents UE mobility information.
 */
@ApiModel(description = "Represents UE mobility information.")
@JsonPropertyOrder({ UeMobility.JSON_PROPERTY_TS,
                     UeMobility.JSON_PROPERTY_RECURRING_TIME,
                     UeMobility.JSON_PROPERTY_DURATION,
                     UeMobility.JSON_PROPERTY_DURATION_VARIANCE,
                     UeMobility.JSON_PROPERTY_LOC_INFOS })
public class UeMobility
{
    public static final String JSON_PROPERTY_TS = "ts";
    private OffsetDateTime ts;

    public static final String JSON_PROPERTY_RECURRING_TIME = "recurringTime";
    private ScheduledCommunicationTime recurringTime;

    public static final String JSON_PROPERTY_DURATION = "duration";
    private Integer duration;

    public static final String JSON_PROPERTY_DURATION_VARIANCE = "durationVariance";
    private Float durationVariance;

    public static final String JSON_PROPERTY_LOC_INFOS = "locInfos";
    private List<LocationInfo> locInfos = null;

    public UeMobility()
    {
    }

    public UeMobility ts(OffsetDateTime ts)
    {

        this.ts = ts;
        return this;
    }

    /**
     * string with format &#39;date-time&#39; as defined in OpenAPI.
     * 
     * @return ts
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'date-time' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_TS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getTs()
    {
        return ts;
    }

    @JsonProperty(JSON_PROPERTY_TS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTs(OffsetDateTime ts)
    {
        this.ts = ts;
    }

    public UeMobility recurringTime(ScheduledCommunicationTime recurringTime)
    {

        this.recurringTime = recurringTime;
        return this;
    }

    /**
     * Get recurringTime
     * 
     * @return recurringTime
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_RECURRING_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public ScheduledCommunicationTime getRecurringTime()
    {
        return recurringTime;
    }

    @JsonProperty(JSON_PROPERTY_RECURRING_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRecurringTime(ScheduledCommunicationTime recurringTime)
    {
        this.recurringTime = recurringTime;
    }

    public UeMobility duration(Integer duration)
    {

        this.duration = duration;
        return this;
    }

    /**
     * indicating a time in seconds.
     * 
     * @return duration
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "indicating a time in seconds.")
    @JsonProperty(JSON_PROPERTY_DURATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getDuration()
    {
        return duration;
    }

    @JsonProperty(JSON_PROPERTY_DURATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDuration(Integer duration)
    {
        this.duration = duration;
    }

    public UeMobility durationVariance(Float durationVariance)
    {

        this.durationVariance = durationVariance;
        return this;
    }

    /**
     * string with format &#39;float&#39; as defined in OpenAPI.
     * 
     * @return durationVariance
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'float' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_DURATION_VARIANCE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Float getDurationVariance()
    {
        return durationVariance;
    }

    @JsonProperty(JSON_PROPERTY_DURATION_VARIANCE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDurationVariance(Float durationVariance)
    {
        this.durationVariance = durationVariance;
    }

    public UeMobility locInfos(List<LocationInfo> locInfos)
    {

        this.locInfos = locInfos;
        return this;
    }

    public UeMobility addLocInfosItem(LocationInfo locInfosItem)
    {
        if (this.locInfos == null)
        {
            this.locInfos = new ArrayList<>();
        }
        this.locInfos.add(locInfosItem);
        return this;
    }

    /**
     * Get locInfos
     * 
     * @return locInfos
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_LOC_INFOS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<LocationInfo> getLocInfos()
    {
        return locInfos;
    }

    @JsonProperty(JSON_PROPERTY_LOC_INFOS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLocInfos(List<LocationInfo> locInfos)
    {
        this.locInfos = locInfos;
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
        UeMobility ueMobility = (UeMobility) o;
        return Objects.equals(this.ts, ueMobility.ts) && Objects.equals(this.recurringTime, ueMobility.recurringTime)
               && Objects.equals(this.duration, ueMobility.duration) && Objects.equals(this.durationVariance, ueMobility.durationVariance)
               && Objects.equals(this.locInfos, ueMobility.locInfos);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(ts, recurringTime, duration, durationVariance, locInfos);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class UeMobility {\n");
        sb.append("    ts: ").append(toIndentedString(ts)).append("\n");
        sb.append("    recurringTime: ").append(toIndentedString(recurringTime)).append("\n");
        sb.append("    duration: ").append(toIndentedString(duration)).append("\n");
        sb.append("    durationVariance: ").append(toIndentedString(durationVariance)).append("\n");
        sb.append("    locInfos: ").append(toIndentedString(locInfos)).append("\n");
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
