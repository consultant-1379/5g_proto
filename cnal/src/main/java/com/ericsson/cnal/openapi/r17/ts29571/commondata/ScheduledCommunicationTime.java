/*
 * Common Data Types
 * Common Data Types for Service Based Interfaces.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved.   
 *
 * The version of the OpenAPI document: 1.4.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29571.commondata;

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
 * Identifies time and day of the week when the UE is available for
 * communication.
 */
@ApiModel(description = "Identifies time and day of the week when the UE is available for communication.")
@JsonPropertyOrder({ ScheduledCommunicationTime.JSON_PROPERTY_DAYS_OF_WEEK,
                     ScheduledCommunicationTime.JSON_PROPERTY_TIME_OF_DAY_START,
                     ScheduledCommunicationTime.JSON_PROPERTY_TIME_OF_DAY_END })
public class ScheduledCommunicationTime
{
    public static final String JSON_PROPERTY_DAYS_OF_WEEK = "daysOfWeek";
    private List<Integer> daysOfWeek = null;

    public static final String JSON_PROPERTY_TIME_OF_DAY_START = "timeOfDayStart";
    private String timeOfDayStart;

    public static final String JSON_PROPERTY_TIME_OF_DAY_END = "timeOfDayEnd";
    private String timeOfDayEnd;

    public ScheduledCommunicationTime()
    {
    }

    public ScheduledCommunicationTime daysOfWeek(List<Integer> daysOfWeek)
    {

        this.daysOfWeek = daysOfWeek;
        return this;
    }

    public ScheduledCommunicationTime addDaysOfWeekItem(Integer daysOfWeekItem)
    {
        if (this.daysOfWeek == null)
        {
            this.daysOfWeek = new ArrayList<>();
        }
        this.daysOfWeek.add(daysOfWeekItem);
        return this;
    }

    /**
     * Identifies the day(s) of the week. If absent, it indicates every day of the
     * week.
     * 
     * @return daysOfWeek
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Identifies the day(s) of the week. If absent, it indicates every day of the week. ")
    @JsonProperty(JSON_PROPERTY_DAYS_OF_WEEK)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<Integer> getDaysOfWeek()
    {
        return daysOfWeek;
    }

    @JsonProperty(JSON_PROPERTY_DAYS_OF_WEEK)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDaysOfWeek(List<Integer> daysOfWeek)
    {
        this.daysOfWeek = daysOfWeek;
    }

    public ScheduledCommunicationTime timeOfDayStart(String timeOfDayStart)
    {

        this.timeOfDayStart = timeOfDayStart;
        return this;
    }

    /**
     * String with format partial-time or full-time as defined in clause 5.6 of IETF
     * RFC 3339. Examples, 20:15:00, 20:15:00-08:00 (for 8 hours behind UTC).
     * 
     * @return timeOfDayStart
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String with format partial-time or full-time as defined in clause 5.6 of IETF RFC 3339. Examples, 20:15:00, 20:15:00-08:00 (for 8 hours behind UTC).  ")
    @JsonProperty(JSON_PROPERTY_TIME_OF_DAY_START)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getTimeOfDayStart()
    {
        return timeOfDayStart;
    }

    @JsonProperty(JSON_PROPERTY_TIME_OF_DAY_START)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTimeOfDayStart(String timeOfDayStart)
    {
        this.timeOfDayStart = timeOfDayStart;
    }

    public ScheduledCommunicationTime timeOfDayEnd(String timeOfDayEnd)
    {

        this.timeOfDayEnd = timeOfDayEnd;
        return this;
    }

    /**
     * String with format partial-time or full-time as defined in clause 5.6 of IETF
     * RFC 3339. Examples, 20:15:00, 20:15:00-08:00 (for 8 hours behind UTC).
     * 
     * @return timeOfDayEnd
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String with format partial-time or full-time as defined in clause 5.6 of IETF RFC 3339. Examples, 20:15:00, 20:15:00-08:00 (for 8 hours behind UTC).  ")
    @JsonProperty(JSON_PROPERTY_TIME_OF_DAY_END)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getTimeOfDayEnd()
    {
        return timeOfDayEnd;
    }

    @JsonProperty(JSON_PROPERTY_TIME_OF_DAY_END)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTimeOfDayEnd(String timeOfDayEnd)
    {
        this.timeOfDayEnd = timeOfDayEnd;
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
        ScheduledCommunicationTime scheduledCommunicationTime = (ScheduledCommunicationTime) o;
        return Objects.equals(this.daysOfWeek, scheduledCommunicationTime.daysOfWeek)
               && Objects.equals(this.timeOfDayStart, scheduledCommunicationTime.timeOfDayStart)
               && Objects.equals(this.timeOfDayEnd, scheduledCommunicationTime.timeOfDayEnd);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(daysOfWeek, timeOfDayStart, timeOfDayEnd);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class ScheduledCommunicationTime {\n");
        sb.append("    daysOfWeek: ").append(toIndentedString(daysOfWeek)).append("\n");
        sb.append("    timeOfDayStart: ").append(toIndentedString(timeOfDayStart)).append("\n");
        sb.append("    timeOfDayEnd: ").append(toIndentedString(timeOfDayEnd)).append("\n");
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
