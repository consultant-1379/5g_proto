/*
 * Nsmf_EventExposure
 * Session Management Event Exposure Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29508.nsmf.eventexposure;

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
 * Represents notifications on events that occurred.
 */
@ApiModel(description = "Represents notifications on events that occurred.")
@JsonPropertyOrder({ NsmfEventExposureNotification.JSON_PROPERTY_NOTIF_ID,
                     NsmfEventExposureNotification.JSON_PROPERTY_EVENT_NOTIFS,
                     NsmfEventExposureNotification.JSON_PROPERTY_ACK_URI })
public class NsmfEventExposureNotification
{
    public static final String JSON_PROPERTY_NOTIF_ID = "notifId";
    private String notifId;

    public static final String JSON_PROPERTY_EVENT_NOTIFS = "eventNotifs";
    private List<EventNotification> eventNotifs = new ArrayList<>();

    public static final String JSON_PROPERTY_ACK_URI = "ackUri";
    private String ackUri;

    public NsmfEventExposureNotification()
    {
    }

    public NsmfEventExposureNotification notifId(String notifId)
    {

        this.notifId = notifId;
        return this;
    }

    /**
     * Notification correlation ID
     * 
     * @return notifId
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Notification correlation ID")
    @JsonProperty(JSON_PROPERTY_NOTIF_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getNotifId()
    {
        return notifId;
    }

    @JsonProperty(JSON_PROPERTY_NOTIF_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setNotifId(String notifId)
    {
        this.notifId = notifId;
    }

    public NsmfEventExposureNotification eventNotifs(List<EventNotification> eventNotifs)
    {

        this.eventNotifs = eventNotifs;
        return this;
    }

    public NsmfEventExposureNotification addEventNotifsItem(EventNotification eventNotifsItem)
    {
        this.eventNotifs.add(eventNotifsItem);
        return this;
    }

    /**
     * Notifications about Individual Events
     * 
     * @return eventNotifs
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Notifications about Individual Events")
    @JsonProperty(JSON_PROPERTY_EVENT_NOTIFS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public List<EventNotification> getEventNotifs()
    {
        return eventNotifs;
    }

    @JsonProperty(JSON_PROPERTY_EVENT_NOTIFS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setEventNotifs(List<EventNotification> eventNotifs)
    {
        this.eventNotifs = eventNotifs;
    }

    public NsmfEventExposureNotification ackUri(String ackUri)
    {

        this.ackUri = ackUri;
        return this;
    }

    /**
     * String providing an URI formatted according to RFC 3986.
     * 
     * @return ackUri
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String providing an URI formatted according to RFC 3986.")
    @JsonProperty(JSON_PROPERTY_ACK_URI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getAckUri()
    {
        return ackUri;
    }

    @JsonProperty(JSON_PROPERTY_ACK_URI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAckUri(String ackUri)
    {
        this.ackUri = ackUri;
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
        NsmfEventExposureNotification nsmfEventExposureNotification = (NsmfEventExposureNotification) o;
        return Objects.equals(this.notifId, nsmfEventExposureNotification.notifId)
               && Objects.equals(this.eventNotifs, nsmfEventExposureNotification.eventNotifs)
               && Objects.equals(this.ackUri, nsmfEventExposureNotification.ackUri);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(notifId, eventNotifs, ackUri);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class NsmfEventExposureNotification {\n");
        sb.append("    notifId: ").append(toIndentedString(notifId)).append("\n");
        sb.append("    eventNotifs: ").append(toIndentedString(eventNotifs)).append("\n");
        sb.append("    ackUri: ").append(toIndentedString(ackUri)).append("\n");
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
