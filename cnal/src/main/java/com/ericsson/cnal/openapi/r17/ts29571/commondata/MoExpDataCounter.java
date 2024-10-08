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
import java.time.OffsetDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Contain the MO Exception Data Counter.
 */
@ApiModel(description = "Contain the MO Exception Data Counter.")
@JsonPropertyOrder({ MoExpDataCounter.JSON_PROPERTY_COUNTER, MoExpDataCounter.JSON_PROPERTY_TIME_STAMP })
public class MoExpDataCounter
{
    public static final String JSON_PROPERTY_COUNTER = "counter";
    private Integer counter;

    public static final String JSON_PROPERTY_TIME_STAMP = "timeStamp";
    private OffsetDateTime timeStamp;

    public MoExpDataCounter()
    {
    }

    public MoExpDataCounter counter(Integer counter)
    {

        this.counter = counter;
        return this;
    }

    /**
     * Unsigned integer identifying the MO Exception Data Counter, as specified in
     * clause 5.31.14.3 of 3GPP TS 23.501.
     * 
     * @return counter
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true,
                      value = "Unsigned integer identifying the MO Exception Data Counter, as specified in clause 5.31.14.3 of 3GPP TS 23.501. ")
    @JsonProperty(JSON_PROPERTY_COUNTER)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Integer getCounter()
    {
        return counter;
    }

    @JsonProperty(JSON_PROPERTY_COUNTER)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setCounter(Integer counter)
    {
        this.counter = counter;
    }

    public MoExpDataCounter timeStamp(OffsetDateTime timeStamp)
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
        MoExpDataCounter moExpDataCounter = (MoExpDataCounter) o;
        return Objects.equals(this.counter, moExpDataCounter.counter) && Objects.equals(this.timeStamp, moExpDataCounter.timeStamp);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(counter, timeStamp);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class MoExpDataCounter {\n");
        sb.append("    counter: ").append(toIndentedString(counter)).append("\n");
        sb.append("    timeStamp: ").append(toIndentedString(timeStamp)).append("\n");
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
