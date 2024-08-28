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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * MBS session subscription
 */
@ApiModel(description = "MBS session subscription")
@JsonPropertyOrder({ MbsSessionSubscription.JSON_PROPERTY_MBS_SESSION_ID,
                     MbsSessionSubscription.JSON_PROPERTY_AREA_SESSION_ID,
                     MbsSessionSubscription.JSON_PROPERTY_EVENT_LIST,
                     MbsSessionSubscription.JSON_PROPERTY_NOTIFY_URI,
                     MbsSessionSubscription.JSON_PROPERTY_NOTIFY_CORRELATION_ID,
                     MbsSessionSubscription.JSON_PROPERTY_EXPIRY_TIME,
                     MbsSessionSubscription.JSON_PROPERTY_NFC_INSTANCE_ID,
                     MbsSessionSubscription.JSON_PROPERTY_MBS_SESSION_SUBSC_URI })
public class MbsSessionSubscription
{
    public static final String JSON_PROPERTY_MBS_SESSION_ID = "mbsSessionId";
    private MbsSessionId mbsSessionId;

    public static final String JSON_PROPERTY_AREA_SESSION_ID = "areaSessionId";
    private Integer areaSessionId;

    public static final String JSON_PROPERTY_EVENT_LIST = "eventList";
    private List<MbsSessionEvent> eventList = new ArrayList<>();

    public static final String JSON_PROPERTY_NOTIFY_URI = "notifyUri";
    private String notifyUri;

    public static final String JSON_PROPERTY_NOTIFY_CORRELATION_ID = "notifyCorrelationId";
    private String notifyCorrelationId;

    public static final String JSON_PROPERTY_EXPIRY_TIME = "expiryTime";
    private OffsetDateTime expiryTime;

    public static final String JSON_PROPERTY_NFC_INSTANCE_ID = "nfcInstanceId";
    private UUID nfcInstanceId;

    public static final String JSON_PROPERTY_MBS_SESSION_SUBSC_URI = "mbsSessionSubscUri";
    private String mbsSessionSubscUri;

    public MbsSessionSubscription()
    {
    }

    public MbsSessionSubscription mbsSessionId(MbsSessionId mbsSessionId)
    {

        this.mbsSessionId = mbsSessionId;
        return this;
    }

    /**
     * Get mbsSessionId
     * 
     * @return mbsSessionId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_MBS_SESSION_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public MbsSessionId getMbsSessionId()
    {
        return mbsSessionId;
    }

    @JsonProperty(JSON_PROPERTY_MBS_SESSION_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMbsSessionId(MbsSessionId mbsSessionId)
    {
        this.mbsSessionId = mbsSessionId;
    }

    public MbsSessionSubscription areaSessionId(Integer areaSessionId)
    {

        this.areaSessionId = areaSessionId;
        return this;
    }

    /**
     * Integer where the allowed values correspond to the value range of an unsigned
     * 16-bit integer. minimum: 0 maximum: 65535
     * 
     * @return areaSessionId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Integer where the allowed values correspond to the value range of an unsigned 16-bit integer.")
    @JsonProperty(JSON_PROPERTY_AREA_SESSION_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getAreaSessionId()
    {
        return areaSessionId;
    }

    @JsonProperty(JSON_PROPERTY_AREA_SESSION_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAreaSessionId(Integer areaSessionId)
    {
        this.areaSessionId = areaSessionId;
    }

    public MbsSessionSubscription eventList(List<MbsSessionEvent> eventList)
    {

        this.eventList = eventList;
        return this;
    }

    public MbsSessionSubscription addEventListItem(MbsSessionEvent eventListItem)
    {
        this.eventList.add(eventListItem);
        return this;
    }

    /**
     * Get eventList
     * 
     * @return eventList
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_EVENT_LIST)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public List<MbsSessionEvent> getEventList()
    {
        return eventList;
    }

    @JsonProperty(JSON_PROPERTY_EVENT_LIST)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setEventList(List<MbsSessionEvent> eventList)
    {
        this.eventList = eventList;
    }

    public MbsSessionSubscription notifyUri(String notifyUri)
    {

        this.notifyUri = notifyUri;
        return this;
    }

    /**
     * String providing an URI formatted according to RFC 3986.
     * 
     * @return notifyUri
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "String providing an URI formatted according to RFC 3986.")
    @JsonProperty(JSON_PROPERTY_NOTIFY_URI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getNotifyUri()
    {
        return notifyUri;
    }

    @JsonProperty(JSON_PROPERTY_NOTIFY_URI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setNotifyUri(String notifyUri)
    {
        this.notifyUri = notifyUri;
    }

    public MbsSessionSubscription notifyCorrelationId(String notifyCorrelationId)
    {

        this.notifyCorrelationId = notifyCorrelationId;
        return this;
    }

    /**
     * Get notifyCorrelationId
     * 
     * @return notifyCorrelationId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_NOTIFY_CORRELATION_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getNotifyCorrelationId()
    {
        return notifyCorrelationId;
    }

    @JsonProperty(JSON_PROPERTY_NOTIFY_CORRELATION_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNotifyCorrelationId(String notifyCorrelationId)
    {
        this.notifyCorrelationId = notifyCorrelationId;
    }

    public MbsSessionSubscription expiryTime(OffsetDateTime expiryTime)
    {

        this.expiryTime = expiryTime;
        return this;
    }

    /**
     * string with format &#39;date-time&#39; as defined in OpenAPI.
     * 
     * @return expiryTime
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'date-time' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_EXPIRY_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getExpiryTime()
    {
        return expiryTime;
    }

    @JsonProperty(JSON_PROPERTY_EXPIRY_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setExpiryTime(OffsetDateTime expiryTime)
    {
        this.expiryTime = expiryTime;
    }

    public MbsSessionSubscription nfcInstanceId(UUID nfcInstanceId)
    {

        this.nfcInstanceId = nfcInstanceId;
        return this;
    }

    /**
     * String uniquely identifying a NF instance. The format of the NF Instance ID
     * shall be a Universally Unique Identifier (UUID) version 4, as described in
     * IETF RFC 4122.
     * 
     * @return nfcInstanceId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String uniquely identifying a NF instance. The format of the NF Instance ID shall be a  Universally Unique Identifier (UUID) version 4, as described in IETF RFC 4122.  ")
    @JsonProperty(JSON_PROPERTY_NFC_INSTANCE_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UUID getNfcInstanceId()
    {
        return nfcInstanceId;
    }

    @JsonProperty(JSON_PROPERTY_NFC_INSTANCE_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNfcInstanceId(UUID nfcInstanceId)
    {
        this.nfcInstanceId = nfcInstanceId;
    }

    public MbsSessionSubscription mbsSessionSubscUri(String mbsSessionSubscUri)
    {

        this.mbsSessionSubscUri = mbsSessionSubscUri;
        return this;
    }

    /**
     * String providing an URI formatted according to RFC 3986.
     * 
     * @return mbsSessionSubscUri
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String providing an URI formatted according to RFC 3986.")
    @JsonProperty(JSON_PROPERTY_MBS_SESSION_SUBSC_URI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getMbsSessionSubscUri()
    {
        return mbsSessionSubscUri;
    }

    @JsonProperty(JSON_PROPERTY_MBS_SESSION_SUBSC_URI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMbsSessionSubscUri(String mbsSessionSubscUri)
    {
        this.mbsSessionSubscUri = mbsSessionSubscUri;
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
        MbsSessionSubscription mbsSessionSubscription = (MbsSessionSubscription) o;
        return Objects.equals(this.mbsSessionId, mbsSessionSubscription.mbsSessionId)
               && Objects.equals(this.areaSessionId, mbsSessionSubscription.areaSessionId) && Objects.equals(this.eventList, mbsSessionSubscription.eventList)
               && Objects.equals(this.notifyUri, mbsSessionSubscription.notifyUri)
               && Objects.equals(this.notifyCorrelationId, mbsSessionSubscription.notifyCorrelationId)
               && Objects.equals(this.expiryTime, mbsSessionSubscription.expiryTime) && Objects.equals(this.nfcInstanceId, mbsSessionSubscription.nfcInstanceId)
               && Objects.equals(this.mbsSessionSubscUri, mbsSessionSubscription.mbsSessionSubscUri);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(mbsSessionId, areaSessionId, eventList, notifyUri, notifyCorrelationId, expiryTime, nfcInstanceId, mbsSessionSubscUri);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class MbsSessionSubscription {\n");
        sb.append("    mbsSessionId: ").append(toIndentedString(mbsSessionId)).append("\n");
        sb.append("    areaSessionId: ").append(toIndentedString(areaSessionId)).append("\n");
        sb.append("    eventList: ").append(toIndentedString(eventList)).append("\n");
        sb.append("    notifyUri: ").append(toIndentedString(notifyUri)).append("\n");
        sb.append("    notifyCorrelationId: ").append(toIndentedString(notifyCorrelationId)).append("\n");
        sb.append("    expiryTime: ").append(toIndentedString(expiryTime)).append("\n");
        sb.append("    nfcInstanceId: ").append(toIndentedString(nfcInstanceId)).append("\n");
        sb.append("    mbsSessionSubscUri: ").append(toIndentedString(mbsSessionSubscUri)).append("\n");
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
