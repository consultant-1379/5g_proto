/*
 * AMInfluence
 * AMInfluence API Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.0.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29522.aminfluence;

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
 * Represents an AM influence event notification.
 */
@ApiModel(description = "Represents an AM influence event notification.")
@JsonPropertyOrder({ AmInfluEventNotif.JSON_PROPERTY_AF_TRANS_ID, AmInfluEventNotif.JSON_PROPERTY_EVENT, AmInfluEventNotif.JSON_PROPERTY_GEO_AREAS })
public class AmInfluEventNotif
{
    public static final String JSON_PROPERTY_AF_TRANS_ID = "afTransId";
    private String afTransId;

    public static final String JSON_PROPERTY_EVENT = "event";
    private String event;

    public static final String JSON_PROPERTY_GEO_AREAS = "geoAreas";
    private List<Object> geoAreas = null;

    public AmInfluEventNotif()
    {
    }

    public AmInfluEventNotif afTransId(String afTransId)
    {

        this.afTransId = afTransId;
        return this;
    }

    /**
     * Get afTransId
     * 
     * @return afTransId
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_AF_TRANS_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getAfTransId()
    {
        return afTransId;
    }

    @JsonProperty(JSON_PROPERTY_AF_TRANS_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setAfTransId(String afTransId)
    {
        this.afTransId = afTransId;
    }

    public AmInfluEventNotif event(String event)
    {

        this.event = event;
        return this;
    }

    /**
     * Represents the service area coverage outcome event.
     * 
     * @return event
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Represents the service area coverage outcome event.")
    @JsonProperty(JSON_PROPERTY_EVENT)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getEvent()
    {
        return event;
    }

    @JsonProperty(JSON_PROPERTY_EVENT)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setEvent(String event)
    {
        this.event = event;
    }

    public AmInfluEventNotif geoAreas(List<Object> geoAreas)
    {

        this.geoAreas = geoAreas;
        return this;
    }

    public AmInfluEventNotif addGeoAreasItem(Object geoAreasItem)
    {
        if (this.geoAreas == null)
        {
            this.geoAreas = new ArrayList<>();
        }
        this.geoAreas.add(geoAreasItem);
        return this;
    }

    /**
     * Identifies geographic areas of the user where the request is applicable.
     * 
     * @return geoAreas
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Identifies geographic areas of the user where the request is applicable.")
    @JsonProperty(JSON_PROPERTY_GEO_AREAS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<Object> getGeoAreas()
    {
        return geoAreas;
    }

    @JsonProperty(JSON_PROPERTY_GEO_AREAS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setGeoAreas(List<Object> geoAreas)
    {
        this.geoAreas = geoAreas;
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
        AmInfluEventNotif amInfluEventNotif = (AmInfluEventNotif) o;
        return Objects.equals(this.afTransId, amInfluEventNotif.afTransId) && Objects.equals(this.event, amInfluEventNotif.event)
               && Objects.equals(this.geoAreas, amInfluEventNotif.geoAreas);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(afTransId, event, geoAreas);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class AmInfluEventNotif {\n");
        sb.append("    afTransId: ").append(toIndentedString(afTransId)).append("\n");
        sb.append("    event: ").append(toIndentedString(event)).append("\n");
        sb.append("    geoAreas: ").append(toIndentedString(geoAreas)).append("\n");
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
