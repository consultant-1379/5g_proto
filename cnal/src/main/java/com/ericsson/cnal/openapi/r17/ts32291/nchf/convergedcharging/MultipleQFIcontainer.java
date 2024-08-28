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
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * MultipleQFIcontainer
 */
@JsonPropertyOrder({ MultipleQFIcontainer.JSON_PROPERTY_TRIGGERS,
                     MultipleQFIcontainer.JSON_PROPERTY_TRIGGER_TIMESTAMP,
                     MultipleQFIcontainer.JSON_PROPERTY_TIME,
                     MultipleQFIcontainer.JSON_PROPERTY_TOTAL_VOLUME,
                     MultipleQFIcontainer.JSON_PROPERTY_UPLINK_VOLUME,
                     MultipleQFIcontainer.JSON_PROPERTY_DOWNLINK_VOLUME,
                     MultipleQFIcontainer.JSON_PROPERTY_LOCAL_SEQUENCE_NUMBER,
                     MultipleQFIcontainer.JSON_PROPERTY_Q_F_I_CONTAINER_INFORMATION })
public class MultipleQFIcontainer
{
    public static final String JSON_PROPERTY_TRIGGERS = "triggers";
    private List<Trigger> triggers = null;

    public static final String JSON_PROPERTY_TRIGGER_TIMESTAMP = "triggerTimestamp";
    private OffsetDateTime triggerTimestamp;

    public static final String JSON_PROPERTY_TIME = "time";
    private Integer time;

    public static final String JSON_PROPERTY_TOTAL_VOLUME = "totalVolume";
    private Integer totalVolume;

    public static final String JSON_PROPERTY_UPLINK_VOLUME = "uplinkVolume";
    private Integer uplinkVolume;

    public static final String JSON_PROPERTY_DOWNLINK_VOLUME = "downlinkVolume";
    private Integer downlinkVolume;

    public static final String JSON_PROPERTY_LOCAL_SEQUENCE_NUMBER = "localSequenceNumber";
    private Integer localSequenceNumber;

    public static final String JSON_PROPERTY_Q_F_I_CONTAINER_INFORMATION = "qFIContainerInformation";
    private QFIContainerInformation qFIContainerInformation;

    public MultipleQFIcontainer()
    {
    }

    public MultipleQFIcontainer triggers(List<Trigger> triggers)
    {

        this.triggers = triggers;
        return this;
    }

    public MultipleQFIcontainer addTriggersItem(Trigger triggersItem)
    {
        if (this.triggers == null)
        {
            this.triggers = new ArrayList<>();
        }
        this.triggers.add(triggersItem);
        return this;
    }

    /**
     * Get triggers
     * 
     * @return triggers
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_TRIGGERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<Trigger> getTriggers()
    {
        return triggers;
    }

    @JsonProperty(JSON_PROPERTY_TRIGGERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTriggers(List<Trigger> triggers)
    {
        this.triggers = triggers;
    }

    public MultipleQFIcontainer triggerTimestamp(OffsetDateTime triggerTimestamp)
    {

        this.triggerTimestamp = triggerTimestamp;
        return this;
    }

    /**
     * string with format &#39;date-time&#39; as defined in OpenAPI.
     * 
     * @return triggerTimestamp
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'date-time' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_TRIGGER_TIMESTAMP)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getTriggerTimestamp()
    {
        return triggerTimestamp;
    }

    @JsonProperty(JSON_PROPERTY_TRIGGER_TIMESTAMP)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTriggerTimestamp(OffsetDateTime triggerTimestamp)
    {
        this.triggerTimestamp = triggerTimestamp;
    }

    public MultipleQFIcontainer time(Integer time)
    {

        this.time = time;
        return this;
    }

    /**
     * Integer where the allowed values correspond to the value range of an unsigned
     * 32-bit integer. minimum: 0 maximum: 4294967295
     * 
     * @return time
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Integer where the allowed values correspond to the value range of an unsigned 32-bit integer. ")
    @JsonProperty(JSON_PROPERTY_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getTime()
    {
        return time;
    }

    @JsonProperty(JSON_PROPERTY_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTime(Integer time)
    {
        this.time = time;
    }

    public MultipleQFIcontainer totalVolume(Integer totalVolume)
    {

        this.totalVolume = totalVolume;
        return this;
    }

    /**
     * Integer where the allowed values correspond to the value range of an unsigned
     * 64-bit integer. minimum: 0 maximum: -1
     * 
     * @return totalVolume
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Integer where the allowed values correspond to the value range of an unsigned 64-bit integer. ")
    @JsonProperty(JSON_PROPERTY_TOTAL_VOLUME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getTotalVolume()
    {
        return totalVolume;
    }

    @JsonProperty(JSON_PROPERTY_TOTAL_VOLUME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTotalVolume(Integer totalVolume)
    {
        this.totalVolume = totalVolume;
    }

    public MultipleQFIcontainer uplinkVolume(Integer uplinkVolume)
    {

        this.uplinkVolume = uplinkVolume;
        return this;
    }

    /**
     * Integer where the allowed values correspond to the value range of an unsigned
     * 64-bit integer. minimum: 0 maximum: -1
     * 
     * @return uplinkVolume
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Integer where the allowed values correspond to the value range of an unsigned 64-bit integer. ")
    @JsonProperty(JSON_PROPERTY_UPLINK_VOLUME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getUplinkVolume()
    {
        return uplinkVolume;
    }

    @JsonProperty(JSON_PROPERTY_UPLINK_VOLUME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUplinkVolume(Integer uplinkVolume)
    {
        this.uplinkVolume = uplinkVolume;
    }

    public MultipleQFIcontainer downlinkVolume(Integer downlinkVolume)
    {

        this.downlinkVolume = downlinkVolume;
        return this;
    }

    /**
     * Integer where the allowed values correspond to the value range of an unsigned
     * 64-bit integer. minimum: 0 maximum: -1
     * 
     * @return downlinkVolume
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Integer where the allowed values correspond to the value range of an unsigned 64-bit integer. ")
    @JsonProperty(JSON_PROPERTY_DOWNLINK_VOLUME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getDownlinkVolume()
    {
        return downlinkVolume;
    }

    @JsonProperty(JSON_PROPERTY_DOWNLINK_VOLUME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDownlinkVolume(Integer downlinkVolume)
    {
        this.downlinkVolume = downlinkVolume;
    }

    public MultipleQFIcontainer localSequenceNumber(Integer localSequenceNumber)
    {

        this.localSequenceNumber = localSequenceNumber;
        return this;
    }

    /**
     * Get localSequenceNumber
     * 
     * @return localSequenceNumber
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_LOCAL_SEQUENCE_NUMBER)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Integer getLocalSequenceNumber()
    {
        return localSequenceNumber;
    }

    @JsonProperty(JSON_PROPERTY_LOCAL_SEQUENCE_NUMBER)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setLocalSequenceNumber(Integer localSequenceNumber)
    {
        this.localSequenceNumber = localSequenceNumber;
    }

    public MultipleQFIcontainer qFIContainerInformation(QFIContainerInformation qFIContainerInformation)
    {

        this.qFIContainerInformation = qFIContainerInformation;
        return this;
    }

    /**
     * Get qFIContainerInformation
     * 
     * @return qFIContainerInformation
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_Q_F_I_CONTAINER_INFORMATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public QFIContainerInformation getqFIContainerInformation()
    {
        return qFIContainerInformation;
    }

    @JsonProperty(JSON_PROPERTY_Q_F_I_CONTAINER_INFORMATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setqFIContainerInformation(QFIContainerInformation qFIContainerInformation)
    {
        this.qFIContainerInformation = qFIContainerInformation;
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
        MultipleQFIcontainer multipleQFIcontainer = (MultipleQFIcontainer) o;
        return Objects.equals(this.triggers, multipleQFIcontainer.triggers) && Objects.equals(this.triggerTimestamp, multipleQFIcontainer.triggerTimestamp)
               && Objects.equals(this.time, multipleQFIcontainer.time) && Objects.equals(this.totalVolume, multipleQFIcontainer.totalVolume)
               && Objects.equals(this.uplinkVolume, multipleQFIcontainer.uplinkVolume)
               && Objects.equals(this.downlinkVolume, multipleQFIcontainer.downlinkVolume)
               && Objects.equals(this.localSequenceNumber, multipleQFIcontainer.localSequenceNumber)
               && Objects.equals(this.qFIContainerInformation, multipleQFIcontainer.qFIContainerInformation);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(triggers, triggerTimestamp, time, totalVolume, uplinkVolume, downlinkVolume, localSequenceNumber, qFIContainerInformation);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class MultipleQFIcontainer {\n");
        sb.append("    triggers: ").append(toIndentedString(triggers)).append("\n");
        sb.append("    triggerTimestamp: ").append(toIndentedString(triggerTimestamp)).append("\n");
        sb.append("    time: ").append(toIndentedString(time)).append("\n");
        sb.append("    totalVolume: ").append(toIndentedString(totalVolume)).append("\n");
        sb.append("    uplinkVolume: ").append(toIndentedString(uplinkVolume)).append("\n");
        sb.append("    downlinkVolume: ").append(toIndentedString(downlinkVolume)).append("\n");
        sb.append("    localSequenceNumber: ").append(toIndentedString(localSequenceNumber)).append("\n");
        sb.append("    qFIContainerInformation: ").append(toIndentedString(qFIContainerInformation)).append("\n");
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
