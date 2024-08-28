/*
 * Nchf_ConvergedCharging
 * ConvergedCharging Service    © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC). All rights reserved. 
 *
 * The version of the OpenAPI document: 3.1.12
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts32291.nchf.convergedcharging;

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
 * Trigger
 */
@JsonPropertyOrder({ Trigger.JSON_PROPERTY_TRIGGER_TYPE,
                     Trigger.JSON_PROPERTY_TRIGGER_CATEGORY,
                     Trigger.JSON_PROPERTY_TIME_LIMIT,
                     Trigger.JSON_PROPERTY_VOLUME_LIMIT,
                     Trigger.JSON_PROPERTY_VOLUME_LIMIT64,
                     Trigger.JSON_PROPERTY_EVENT_LIMIT,
                     Trigger.JSON_PROPERTY_MAX_NUMBER_OFCCC,
                     Trigger.JSON_PROPERTY_TARIFF_TIME_CHANGE })
public class Trigger
{
    public static final String JSON_PROPERTY_TRIGGER_TYPE = "triggerType";
    private String triggerType;

    public static final String JSON_PROPERTY_TRIGGER_CATEGORY = "triggerCategory";
    private String triggerCategory;

    public static final String JSON_PROPERTY_TIME_LIMIT = "timeLimit";
    private Integer timeLimit;

    public static final String JSON_PROPERTY_VOLUME_LIMIT = "volumeLimit";
    private Integer volumeLimit;

    public static final String JSON_PROPERTY_VOLUME_LIMIT64 = "volumeLimit64";
    private Integer volumeLimit64;

    public static final String JSON_PROPERTY_EVENT_LIMIT = "eventLimit";
    private Integer eventLimit;

    public static final String JSON_PROPERTY_MAX_NUMBER_OFCCC = "maxNumberOfccc";
    private Integer maxNumberOfccc;

    public static final String JSON_PROPERTY_TARIFF_TIME_CHANGE = "tariffTimeChange";
    private OffsetDateTime tariffTimeChange;

    public Trigger()
    {
    }

    public Trigger triggerType(String triggerType)
    {

        this.triggerType = triggerType;
        return this;
    }

    /**
     * Get triggerType
     * 
     * @return triggerType
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_TRIGGER_TYPE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getTriggerType()
    {
        return triggerType;
    }

    @JsonProperty(JSON_PROPERTY_TRIGGER_TYPE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setTriggerType(String triggerType)
    {
        this.triggerType = triggerType;
    }

    public Trigger triggerCategory(String triggerCategory)
    {

        this.triggerCategory = triggerCategory;
        return this;
    }

    /**
     * Get triggerCategory
     * 
     * @return triggerCategory
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_TRIGGER_CATEGORY)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getTriggerCategory()
    {
        return triggerCategory;
    }

    @JsonProperty(JSON_PROPERTY_TRIGGER_CATEGORY)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setTriggerCategory(String triggerCategory)
    {
        this.triggerCategory = triggerCategory;
    }

    public Trigger timeLimit(Integer timeLimit)
    {

        this.timeLimit = timeLimit;
        return this;
    }

    /**
     * indicating a time in seconds.
     * 
     * @return timeLimit
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "indicating a time in seconds.")
    @JsonProperty(JSON_PROPERTY_TIME_LIMIT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getTimeLimit()
    {
        return timeLimit;
    }

    @JsonProperty(JSON_PROPERTY_TIME_LIMIT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTimeLimit(Integer timeLimit)
    {
        this.timeLimit = timeLimit;
    }

    public Trigger volumeLimit(Integer volumeLimit)
    {

        this.volumeLimit = volumeLimit;
        return this;
    }

    /**
     * Integer where the allowed values correspond to the value range of an unsigned
     * 32-bit integer. minimum: 0 maximum: 4294967295
     * 
     * @return volumeLimit
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Integer where the allowed values correspond to the value range of an unsigned 32-bit integer. ")
    @JsonProperty(JSON_PROPERTY_VOLUME_LIMIT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getVolumeLimit()
    {
        return volumeLimit;
    }

    @JsonProperty(JSON_PROPERTY_VOLUME_LIMIT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setVolumeLimit(Integer volumeLimit)
    {
        this.volumeLimit = volumeLimit;
    }

    public Trigger volumeLimit64(Integer volumeLimit64)
    {

        this.volumeLimit64 = volumeLimit64;
        return this;
    }

    /**
     * Integer where the allowed values correspond to the value range of an unsigned
     * 64-bit integer. minimum: 0 maximum: -1
     * 
     * @return volumeLimit64
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Integer where the allowed values correspond to the value range of an unsigned 64-bit integer. ")
    @JsonProperty(JSON_PROPERTY_VOLUME_LIMIT64)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getVolumeLimit64()
    {
        return volumeLimit64;
    }

    @JsonProperty(JSON_PROPERTY_VOLUME_LIMIT64)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setVolumeLimit64(Integer volumeLimit64)
    {
        this.volumeLimit64 = volumeLimit64;
    }

    public Trigger eventLimit(Integer eventLimit)
    {

        this.eventLimit = eventLimit;
        return this;
    }

    /**
     * Integer where the allowed values correspond to the value range of an unsigned
     * 32-bit integer. minimum: 0 maximum: 4294967295
     * 
     * @return eventLimit
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Integer where the allowed values correspond to the value range of an unsigned 32-bit integer. ")
    @JsonProperty(JSON_PROPERTY_EVENT_LIMIT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getEventLimit()
    {
        return eventLimit;
    }

    @JsonProperty(JSON_PROPERTY_EVENT_LIMIT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEventLimit(Integer eventLimit)
    {
        this.eventLimit = eventLimit;
    }

    public Trigger maxNumberOfccc(Integer maxNumberOfccc)
    {

        this.maxNumberOfccc = maxNumberOfccc;
        return this;
    }

    /**
     * Integer where the allowed values correspond to the value range of an unsigned
     * 32-bit integer. minimum: 0 maximum: 4294967295
     * 
     * @return maxNumberOfccc
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Integer where the allowed values correspond to the value range of an unsigned 32-bit integer. ")
    @JsonProperty(JSON_PROPERTY_MAX_NUMBER_OFCCC)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getMaxNumberOfccc()
    {
        return maxNumberOfccc;
    }

    @JsonProperty(JSON_PROPERTY_MAX_NUMBER_OFCCC)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMaxNumberOfccc(Integer maxNumberOfccc)
    {
        this.maxNumberOfccc = maxNumberOfccc;
    }

    public Trigger tariffTimeChange(OffsetDateTime tariffTimeChange)
    {

        this.tariffTimeChange = tariffTimeChange;
        return this;
    }

    /**
     * string with format &#39;date-time&#39; as defined in OpenAPI.
     * 
     * @return tariffTimeChange
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'date-time' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_TARIFF_TIME_CHANGE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getTariffTimeChange()
    {
        return tariffTimeChange;
    }

    @JsonProperty(JSON_PROPERTY_TARIFF_TIME_CHANGE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTariffTimeChange(OffsetDateTime tariffTimeChange)
    {
        this.tariffTimeChange = tariffTimeChange;
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
        Trigger trigger = (Trigger) o;
        return Objects.equals(this.triggerType, trigger.triggerType) && Objects.equals(this.triggerCategory, trigger.triggerCategory)
               && Objects.equals(this.timeLimit, trigger.timeLimit) && Objects.equals(this.volumeLimit, trigger.volumeLimit)
               && Objects.equals(this.volumeLimit64, trigger.volumeLimit64) && Objects.equals(this.eventLimit, trigger.eventLimit)
               && Objects.equals(this.maxNumberOfccc, trigger.maxNumberOfccc) && Objects.equals(this.tariffTimeChange, trigger.tariffTimeChange);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(triggerType, triggerCategory, timeLimit, volumeLimit, volumeLimit64, eventLimit, maxNumberOfccc, tariffTimeChange);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class Trigger {\n");
        sb.append("    triggerType: ").append(toIndentedString(triggerType)).append("\n");
        sb.append("    triggerCategory: ").append(toIndentedString(triggerCategory)).append("\n");
        sb.append("    timeLimit: ").append(toIndentedString(timeLimit)).append("\n");
        sb.append("    volumeLimit: ").append(toIndentedString(volumeLimit)).append("\n");
        sb.append("    volumeLimit64: ").append(toIndentedString(volumeLimit64)).append("\n");
        sb.append("    eventLimit: ").append(toIndentedString(eventLimit)).append("\n");
        sb.append("    maxNumberOfccc: ").append(toIndentedString(maxNumberOfccc)).append("\n");
        sb.append("    tariffTimeChange: ").append(toIndentedString(tariffTimeChange)).append("\n");
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
