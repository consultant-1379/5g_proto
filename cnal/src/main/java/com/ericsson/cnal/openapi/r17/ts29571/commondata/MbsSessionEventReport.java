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
 * MBS session event report
 */
@ApiModel(description = "MBS session event report")
@JsonPropertyOrder({ MbsSessionEventReport.JSON_PROPERTY_EVENT_TYPE,
                     MbsSessionEventReport.JSON_PROPERTY_TIME_STAMP,
                     MbsSessionEventReport.JSON_PROPERTY_INGRESS_TUN_ADDR_INFO,
                     MbsSessionEventReport.JSON_PROPERTY_BROADCAST_DEL_STATUS })
public class MbsSessionEventReport
{
    public static final String JSON_PROPERTY_EVENT_TYPE = "eventType";
    private String eventType;

    public static final String JSON_PROPERTY_TIME_STAMP = "timeStamp";
    private OffsetDateTime timeStamp;

    public static final String JSON_PROPERTY_INGRESS_TUN_ADDR_INFO = "ingressTunAddrInfo";
    private IngressTunAddrInfo ingressTunAddrInfo;

    public static final String JSON_PROPERTY_BROADCAST_DEL_STATUS = "broadcastDelStatus";
    private String broadcastDelStatus;

    public MbsSessionEventReport()
    {
    }

    public MbsSessionEventReport eventType(String eventType)
    {

        this.eventType = eventType;
        return this;
    }

    /**
     * MBS Session Event Type
     * 
     * @return eventType
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "MBS Session Event Type")
    @JsonProperty(JSON_PROPERTY_EVENT_TYPE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getEventType()
    {
        return eventType;
    }

    @JsonProperty(JSON_PROPERTY_EVENT_TYPE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setEventType(String eventType)
    {
        this.eventType = eventType;
    }

    public MbsSessionEventReport timeStamp(OffsetDateTime timeStamp)
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

    public MbsSessionEventReport ingressTunAddrInfo(IngressTunAddrInfo ingressTunAddrInfo)
    {

        this.ingressTunAddrInfo = ingressTunAddrInfo;
        return this;
    }

    /**
     * Get ingressTunAddrInfo
     * 
     * @return ingressTunAddrInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_INGRESS_TUN_ADDR_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public IngressTunAddrInfo getIngressTunAddrInfo()
    {
        return ingressTunAddrInfo;
    }

    @JsonProperty(JSON_PROPERTY_INGRESS_TUN_ADDR_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIngressTunAddrInfo(IngressTunAddrInfo ingressTunAddrInfo)
    {
        this.ingressTunAddrInfo = ingressTunAddrInfo;
    }

    public MbsSessionEventReport broadcastDelStatus(String broadcastDelStatus)
    {

        this.broadcastDelStatus = broadcastDelStatus;
        return this;
    }

    /**
     * Broadcast MBS Session&#39;s Delivery Status
     * 
     * @return broadcastDelStatus
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Broadcast MBS Session's Delivery Status")
    @JsonProperty(JSON_PROPERTY_BROADCAST_DEL_STATUS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getBroadcastDelStatus()
    {
        return broadcastDelStatus;
    }

    @JsonProperty(JSON_PROPERTY_BROADCAST_DEL_STATUS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBroadcastDelStatus(String broadcastDelStatus)
    {
        this.broadcastDelStatus = broadcastDelStatus;
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
        MbsSessionEventReport mbsSessionEventReport = (MbsSessionEventReport) o;
        return Objects.equals(this.eventType, mbsSessionEventReport.eventType) && Objects.equals(this.timeStamp, mbsSessionEventReport.timeStamp)
               && Objects.equals(this.ingressTunAddrInfo, mbsSessionEventReport.ingressTunAddrInfo)
               && Objects.equals(this.broadcastDelStatus, mbsSessionEventReport.broadcastDelStatus);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(eventType, timeStamp, ingressTunAddrInfo, broadcastDelStatus);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class MbsSessionEventReport {\n");
        sb.append("    eventType: ").append(toIndentedString(eventType)).append("\n");
        sb.append("    timeStamp: ").append(toIndentedString(timeStamp)).append("\n");
        sb.append("    ingressTunAddrInfo: ").append(toIndentedString(ingressTunAddrInfo)).append("\n");
        sb.append("    broadcastDelStatus: ").append(toIndentedString(broadcastDelStatus)).append("\n");
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
