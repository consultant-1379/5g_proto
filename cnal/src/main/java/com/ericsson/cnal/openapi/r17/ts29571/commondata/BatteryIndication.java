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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Parameters \&quot;replaceableInd\&quot; and \&quot;rechargeableInd\&quot; are
 * only included if the value of Parameter \&quot;batteryInd\&quot; is true.
 */
@ApiModel(description = "Parameters \"replaceableInd\" and \"rechargeableInd\" are only included if the value of Parameter \"batteryInd\" is true. ")
@JsonPropertyOrder({ BatteryIndication.JSON_PROPERTY_BATTERY_IND,
                     BatteryIndication.JSON_PROPERTY_REPLACEABLE_IND,
                     BatteryIndication.JSON_PROPERTY_RECHARGEABLE_IND })
public class BatteryIndication
{
    public static final String JSON_PROPERTY_BATTERY_IND = "batteryInd";
    private Boolean batteryInd;

    public static final String JSON_PROPERTY_REPLACEABLE_IND = "replaceableInd";
    private Boolean replaceableInd;

    public static final String JSON_PROPERTY_RECHARGEABLE_IND = "rechargeableInd";
    private Boolean rechargeableInd;

    public BatteryIndication()
    {
    }

    public BatteryIndication batteryInd(Boolean batteryInd)
    {

        this.batteryInd = batteryInd;
        return this;
    }

    /**
     * This IE shall indicate whether the UE is battery powered or not. true: the UE
     * is battery powered; false or absent: the UE is not battery powered
     * 
     * @return batteryInd
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "This IE shall indicate whether the UE is battery powered or not. true: the UE is battery powered; false or absent: the UE is not battery powered ")
    @JsonProperty(JSON_PROPERTY_BATTERY_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getBatteryInd()
    {
        return batteryInd;
    }

    @JsonProperty(JSON_PROPERTY_BATTERY_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBatteryInd(Boolean batteryInd)
    {
        this.batteryInd = batteryInd;
    }

    public BatteryIndication replaceableInd(Boolean replaceableInd)
    {

        this.replaceableInd = replaceableInd;
        return this;
    }

    /**
     * This IE shall indicate whether the battery of the UE is replaceable or not.
     * true: the battery of the UE is replaceable; false or absent: the battery of
     * the UE is not replaceable.
     * 
     * @return replaceableInd
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "This IE shall indicate whether the battery of the UE is replaceable or not. true: the battery of the UE is replaceable; false or absent: the battery of the UE is not replaceable. ")
    @JsonProperty(JSON_PROPERTY_REPLACEABLE_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getReplaceableInd()
    {
        return replaceableInd;
    }

    @JsonProperty(JSON_PROPERTY_REPLACEABLE_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setReplaceableInd(Boolean replaceableInd)
    {
        this.replaceableInd = replaceableInd;
    }

    public BatteryIndication rechargeableInd(Boolean rechargeableInd)
    {

        this.rechargeableInd = rechargeableInd;
        return this;
    }

    /**
     * This IE shall indicate whether the battery of the UE is rechargeable or not.
     * true: the battery of UE is rechargeable; false or absent: the battery of the
     * UE is not rechargeable.
     * 
     * @return rechargeableInd
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "This IE shall indicate whether the battery of the UE is rechargeable or not. true: the battery of UE is rechargeable; false or absent: the battery of the UE is not rechargeable. ")
    @JsonProperty(JSON_PROPERTY_RECHARGEABLE_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getRechargeableInd()
    {
        return rechargeableInd;
    }

    @JsonProperty(JSON_PROPERTY_RECHARGEABLE_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRechargeableInd(Boolean rechargeableInd)
    {
        this.rechargeableInd = rechargeableInd;
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
        BatteryIndication batteryIndication = (BatteryIndication) o;
        return Objects.equals(this.batteryInd, batteryIndication.batteryInd) && Objects.equals(this.replaceableInd, batteryIndication.replaceableInd)
               && Objects.equals(this.rechargeableInd, batteryIndication.rechargeableInd);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(batteryInd, replaceableInd, rechargeableInd);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class BatteryIndication {\n");
        sb.append("    batteryInd: ").append(toIndentedString(batteryInd)).append("\n");
        sb.append("    replaceableInd: ").append(toIndentedString(replaceableInd)).append("\n");
        sb.append("    rechargeableInd: ").append(toIndentedString(rechargeableInd)).append("\n");
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
