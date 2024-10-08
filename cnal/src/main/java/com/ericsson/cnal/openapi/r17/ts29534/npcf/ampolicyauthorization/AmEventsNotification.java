/*
 * Npcf_AMPolicyAuthorization Service API
 * PCF Access and Mobility Policy Authorization Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.0.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29534.npcf.ampolicyauthorization;

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
 * Describes the notification about the events occurred within an Individual
 * Application AM Context resource.
 */
@ApiModel(description = "Describes the notification about the events occurred within an Individual Application AM Context resource.")
@JsonPropertyOrder({ AmEventsNotification.JSON_PROPERTY_APP_AM_CONTEXT_ID, AmEventsNotification.JSON_PROPERTY_REP_EVENTS })
public class AmEventsNotification
{
    public static final String JSON_PROPERTY_APP_AM_CONTEXT_ID = "appAmContextId";
    private String appAmContextId;

    public static final String JSON_PROPERTY_REP_EVENTS = "repEvents";
    private List<AmEventNotification> repEvents = new ArrayList<>();

    public AmEventsNotification()
    {
    }

    public AmEventsNotification appAmContextId(String appAmContextId)
    {

        this.appAmContextId = appAmContextId;
        return this;
    }

    /**
     * Contains the AM Policy Events Subscription resource identifier related to the
     * event notification.
     * 
     * @return appAmContextId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Contains the AM Policy Events Subscription resource identifier related to the event notification.")
    @JsonProperty(JSON_PROPERTY_APP_AM_CONTEXT_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getAppAmContextId()
    {
        return appAmContextId;
    }

    @JsonProperty(JSON_PROPERTY_APP_AM_CONTEXT_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAppAmContextId(String appAmContextId)
    {
        this.appAmContextId = appAmContextId;
    }

    public AmEventsNotification repEvents(List<AmEventNotification> repEvents)
    {

        this.repEvents = repEvents;
        return this;
    }

    public AmEventsNotification addRepEventsItem(AmEventNotification repEventsItem)
    {
        this.repEvents.add(repEventsItem);
        return this;
    }

    /**
     * Get repEvents
     * 
     * @return repEvents
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_REP_EVENTS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public List<AmEventNotification> getRepEvents()
    {
        return repEvents;
    }

    @JsonProperty(JSON_PROPERTY_REP_EVENTS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setRepEvents(List<AmEventNotification> repEvents)
    {
        this.repEvents = repEvents;
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
        AmEventsNotification amEventsNotification = (AmEventsNotification) o;
        return Objects.equals(this.appAmContextId, amEventsNotification.appAmContextId) && Objects.equals(this.repEvents, amEventsNotification.repEvents);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(appAmContextId, repEvents);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class AmEventsNotification {\n");
        sb.append("    appAmContextId: ").append(toIndentedString(appAmContextId)).append("\n");
        sb.append("    repEvents: ").append(toIndentedString(repEvents)).append("\n");
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
