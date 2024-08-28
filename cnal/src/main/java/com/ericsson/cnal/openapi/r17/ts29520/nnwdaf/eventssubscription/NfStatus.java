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
 * Contains the percentage of time spent on various NF states.
 */
@ApiModel(description = "Contains the percentage of time spent on various NF states.")
@JsonPropertyOrder({ NfStatus.JSON_PROPERTY_STATUS_REGISTERED, NfStatus.JSON_PROPERTY_STATUS_UNREGISTERED, NfStatus.JSON_PROPERTY_STATUS_UNDISCOVERABLE })
public class NfStatus
{
    public static final String JSON_PROPERTY_STATUS_REGISTERED = "statusRegistered";
    private Integer statusRegistered;

    public static final String JSON_PROPERTY_STATUS_UNREGISTERED = "statusUnregistered";
    private Integer statusUnregistered;

    public static final String JSON_PROPERTY_STATUS_UNDISCOVERABLE = "statusUndiscoverable";
    private Integer statusUndiscoverable;

    public NfStatus()
    {
    }

    public NfStatus statusRegistered(Integer statusRegistered)
    {

        this.statusRegistered = statusRegistered;
        return this;
    }

    /**
     * Unsigned integer indicating Sampling Ratio (see clauses 4.15.1 of 3GPP TS
     * 23.502), expressed in percent. minimum: 1 maximum: 100
     * 
     * @return statusRegistered
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Unsigned integer indicating Sampling Ratio (see clauses 4.15.1 of 3GPP TS 23.502), expressed in percent.  ")
    @JsonProperty(JSON_PROPERTY_STATUS_REGISTERED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getStatusRegistered()
    {
        return statusRegistered;
    }

    @JsonProperty(JSON_PROPERTY_STATUS_REGISTERED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStatusRegistered(Integer statusRegistered)
    {
        this.statusRegistered = statusRegistered;
    }

    public NfStatus statusUnregistered(Integer statusUnregistered)
    {

        this.statusUnregistered = statusUnregistered;
        return this;
    }

    /**
     * Unsigned integer indicating Sampling Ratio (see clauses 4.15.1 of 3GPP TS
     * 23.502), expressed in percent. minimum: 1 maximum: 100
     * 
     * @return statusUnregistered
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Unsigned integer indicating Sampling Ratio (see clauses 4.15.1 of 3GPP TS 23.502), expressed in percent.  ")
    @JsonProperty(JSON_PROPERTY_STATUS_UNREGISTERED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getStatusUnregistered()
    {
        return statusUnregistered;
    }

    @JsonProperty(JSON_PROPERTY_STATUS_UNREGISTERED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStatusUnregistered(Integer statusUnregistered)
    {
        this.statusUnregistered = statusUnregistered;
    }

    public NfStatus statusUndiscoverable(Integer statusUndiscoverable)
    {

        this.statusUndiscoverable = statusUndiscoverable;
        return this;
    }

    /**
     * Unsigned integer indicating Sampling Ratio (see clauses 4.15.1 of 3GPP TS
     * 23.502), expressed in percent. minimum: 1 maximum: 100
     * 
     * @return statusUndiscoverable
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Unsigned integer indicating Sampling Ratio (see clauses 4.15.1 of 3GPP TS 23.502), expressed in percent.  ")
    @JsonProperty(JSON_PROPERTY_STATUS_UNDISCOVERABLE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getStatusUndiscoverable()
    {
        return statusUndiscoverable;
    }

    @JsonProperty(JSON_PROPERTY_STATUS_UNDISCOVERABLE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStatusUndiscoverable(Integer statusUndiscoverable)
    {
        this.statusUndiscoverable = statusUndiscoverable;
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
        NfStatus nfStatus = (NfStatus) o;
        return Objects.equals(this.statusRegistered, nfStatus.statusRegistered) && Objects.equals(this.statusUnregistered, nfStatus.statusUnregistered)
               && Objects.equals(this.statusUndiscoverable, nfStatus.statusUndiscoverable);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(statusRegistered, statusUnregistered, statusUndiscoverable);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class NfStatus {\n");
        sb.append("    statusRegistered: ").append(toIndentedString(statusRegistered)).append("\n");
        sb.append("    statusUnregistered: ").append(toIndentedString(statusUnregistered)).append("\n");
        sb.append("    statusUndiscoverable: ").append(toIndentedString(statusUndiscoverable)).append("\n");
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
