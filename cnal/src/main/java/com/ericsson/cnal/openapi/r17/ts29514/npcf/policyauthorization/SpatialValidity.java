/*
 * Npcf_PolicyAuthorization Service API
 * PCF Policy Authorization Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29514.npcf.policyauthorization;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.PresenceInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Describes explicitly the route to an Application location.
 */
@ApiModel(description = "Describes explicitly the route to an Application location.")
@JsonPropertyOrder({ SpatialValidity.JSON_PROPERTY_PRESENCE_INFO_LIST })
public class SpatialValidity
{
    public static final String JSON_PROPERTY_PRESENCE_INFO_LIST = "presenceInfoList";
    private Map<String, PresenceInfo> presenceInfoList = new HashMap<>();

    public SpatialValidity()
    {
    }

    public SpatialValidity presenceInfoList(Map<String, PresenceInfo> presenceInfoList)
    {

        this.presenceInfoList = presenceInfoList;
        return this;
    }

    public SpatialValidity putPresenceInfoListItem(String key,
                                                   PresenceInfo presenceInfoListItem)
    {
        this.presenceInfoList.put(key, presenceInfoListItem);
        return this;
    }

    /**
     * Defines the presence information provisioned by the AF. The praId attribute
     * within the PresenceInfo data type is the key of the map.
     * 
     * @return presenceInfoList
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true,
                      value = "Defines the presence information provisioned by the AF. The praId attribute within the PresenceInfo data type is the key of the map.")
    @JsonProperty(JSON_PROPERTY_PRESENCE_INFO_LIST)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Map<String, PresenceInfo> getPresenceInfoList()
    {
        return presenceInfoList;
    }

    @JsonProperty(JSON_PROPERTY_PRESENCE_INFO_LIST)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setPresenceInfoList(Map<String, PresenceInfo> presenceInfoList)
    {
        this.presenceInfoList = presenceInfoList;
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
        SpatialValidity spatialValidity = (SpatialValidity) o;
        return Objects.equals(this.presenceInfoList, spatialValidity.presenceInfoList);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(presenceInfoList);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class SpatialValidity {\n");
        sb.append("    presenceInfoList: ").append(toIndentedString(presenceInfoList)).append("\n");
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
