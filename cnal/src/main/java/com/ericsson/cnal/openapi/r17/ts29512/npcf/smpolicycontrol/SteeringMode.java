/*
 * Npcf_SMPolicyControl API
 * Session Management Policy Control Service   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29512.npcf.smpolicycontrol;

import java.util.Arrays;
import java.util.Objects;

import org.openapitools.jackson.nullable.JsonNullable;

import com.ericsson.cnal.openapi.r17.ts29571.commondata.AccessType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Contains the steering mode value and parameters determined by the PCF.
 */
@ApiModel(description = "Contains the steering mode value and parameters determined by the PCF.")
@JsonPropertyOrder({ SteeringMode.JSON_PROPERTY_STEER_MODE_VALUE,
                     SteeringMode.JSON_PROPERTY_ACTIVE,
                     SteeringMode.JSON_PROPERTY_STANDBY,
                     SteeringMode.JSON_PROPERTY_3G_LOAD,
                     SteeringMode.JSON_PROPERTY_PRIO_ACC,
                     SteeringMode.JSON_PROPERTY_THRES_VALUE,
                     SteeringMode.JSON_PROPERTY_STEER_MODE_IND })
public class SteeringMode
{
    public static final String JSON_PROPERTY_STEER_MODE_VALUE = "steerModeValue";
    private String steerModeValue;

    public static final String JSON_PROPERTY_ACTIVE = "active";
    private AccessType active;

    public static final String JSON_PROPERTY_STANDBY = "standby";
    private Object standby;

    public static final String JSON_PROPERTY_3G_LOAD = "3gLoad";
    private Integer _3gLoad;

    public static final String JSON_PROPERTY_PRIO_ACC = "prioAcc";
    private AccessType prioAcc;

    public static final String JSON_PROPERTY_THRES_VALUE = "thresValue";
    private JsonNullable<ThresholdValue> thresValue = JsonNullable.<ThresholdValue>undefined();

    public static final String JSON_PROPERTY_STEER_MODE_IND = "steerModeInd";
    private String steerModeInd;

    public SteeringMode()
    {
    }

    public SteeringMode steerModeValue(String steerModeValue)
    {

        this.steerModeValue = steerModeValue;
        return this;
    }

    /**
     * Indicates the steering mode value determined by the PCF.
     * 
     * @return steerModeValue
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Indicates the steering mode value determined by the PCF.")
    @JsonProperty(JSON_PROPERTY_STEER_MODE_VALUE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getSteerModeValue()
    {
        return steerModeValue;
    }

    @JsonProperty(JSON_PROPERTY_STEER_MODE_VALUE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setSteerModeValue(String steerModeValue)
    {
        this.steerModeValue = steerModeValue;
    }

    public SteeringMode active(AccessType active)
    {

        this.active = active;
        return this;
    }

    /**
     * Get active
     * 
     * @return active
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_ACTIVE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public AccessType getActive()
    {
        return active;
    }

    @JsonProperty(JSON_PROPERTY_ACTIVE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setActive(AccessType active)
    {
        this.active = active;
    }

    public SteeringMode standby(Object standby)
    {

        this.standby = standby;
        return this;
    }

    /**
     * Get standby
     * 
     * @return standby
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_STANDBY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Object getStandby()
    {
        return standby;
    }

    @JsonProperty(JSON_PROPERTY_STANDBY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStandby(Object standby)
    {
        this.standby = standby;
    }

    public SteeringMode _3gLoad(Integer _3gLoad)
    {

        this._3gLoad = _3gLoad;
        return this;
    }

    /**
     * Unsigned Integer, i.e. only value 0 and integers above 0 are permissible.
     * minimum: 0
     * 
     * @return _3gLoad
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Unsigned Integer, i.e. only value 0 and integers above 0 are permissible.")
    @JsonProperty(JSON_PROPERTY_3G_LOAD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer get3gLoad()
    {
        return _3gLoad;
    }

    @JsonProperty(JSON_PROPERTY_3G_LOAD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void set3gLoad(Integer _3gLoad)
    {
        this._3gLoad = _3gLoad;
    }

    public SteeringMode prioAcc(AccessType prioAcc)
    {

        this.prioAcc = prioAcc;
        return this;
    }

    /**
     * Get prioAcc
     * 
     * @return prioAcc
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_PRIO_ACC)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public AccessType getPrioAcc()
    {
        return prioAcc;
    }

    @JsonProperty(JSON_PROPERTY_PRIO_ACC)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPrioAcc(AccessType prioAcc)
    {
        this.prioAcc = prioAcc;
    }

    public SteeringMode thresValue(ThresholdValue thresValue)
    {
        this.thresValue = JsonNullable.<ThresholdValue>of(thresValue);

        return this;
    }

    /**
     * Get thresValue
     * 
     * @return thresValue
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonIgnore

    public ThresholdValue getThresValue()
    {
        return thresValue.orElse(null);
    }

    @JsonProperty(JSON_PROPERTY_THRES_VALUE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public JsonNullable<ThresholdValue> getThresValue_JsonNullable()
    {
        return thresValue;
    }

    @JsonProperty(JSON_PROPERTY_THRES_VALUE)
    public void setThresValue_JsonNullable(JsonNullable<ThresholdValue> thresValue)
    {
        this.thresValue = thresValue;
    }

    public void setThresValue(ThresholdValue thresValue)
    {
        this.thresValue = JsonNullable.<ThresholdValue>of(thresValue);
    }

    public SteeringMode steerModeInd(String steerModeInd)
    {

        this.steerModeInd = steerModeInd;
        return this;
    }

    /**
     * Contains Autonomous load-balance indicator or UE-assistance indicator.
     * 
     * @return steerModeInd
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Contains Autonomous load-balance indicator or UE-assistance indicator.")
    @JsonProperty(JSON_PROPERTY_STEER_MODE_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getSteerModeInd()
    {
        return steerModeInd;
    }

    @JsonProperty(JSON_PROPERTY_STEER_MODE_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSteerModeInd(String steerModeInd)
    {
        this.steerModeInd = steerModeInd;
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
        SteeringMode steeringMode = (SteeringMode) o;
        return Objects.equals(this.steerModeValue, steeringMode.steerModeValue) && Objects.equals(this.active, steeringMode.active)
               && Objects.equals(this.standby, steeringMode.standby) && Objects.equals(this._3gLoad, steeringMode._3gLoad)
               && Objects.equals(this.prioAcc, steeringMode.prioAcc) && equalsNullable(this.thresValue, steeringMode.thresValue)
               && Objects.equals(this.steerModeInd, steeringMode.steerModeInd);
    }

    private static <T> boolean equalsNullable(JsonNullable<T> a,
                                              JsonNullable<T> b)
    {
        return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(steerModeValue, active, standby, _3gLoad, prioAcc, hashCodeNullable(thresValue), steerModeInd);
    }

    private static <T> int hashCodeNullable(JsonNullable<T> a)
    {
        if (a == null)
        {
            return 1;
        }
        return a.isPresent() ? Arrays.deepHashCode(new Object[] { a.get() }) : 31;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class SteeringMode {\n");
        sb.append("    steerModeValue: ").append(toIndentedString(steerModeValue)).append("\n");
        sb.append("    active: ").append(toIndentedString(active)).append("\n");
        sb.append("    standby: ").append(toIndentedString(standby)).append("\n");
        sb.append("    _3gLoad: ").append(toIndentedString(_3gLoad)).append("\n");
        sb.append("    prioAcc: ").append(toIndentedString(prioAcc)).append("\n");
        sb.append("    thresValue: ").append(toIndentedString(thresValue)).append("\n");
        sb.append("    steerModeInd: ").append(toIndentedString(steerModeInd)).append("\n");
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
