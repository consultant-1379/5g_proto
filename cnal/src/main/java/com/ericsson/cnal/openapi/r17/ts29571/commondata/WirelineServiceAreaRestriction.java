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
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * The \&quot;restrictionType\&quot; attribute and the \&quot;areas\&quot;
 * attribute shall be either both present or absent. The empty array of areas is
 * used when service is allowed/restricted nowhere.
 */
@ApiModel(description = "The \"restrictionType\" attribute and the \"areas\" attribute shall be either both present or absent.  The empty array of areas is used when service is allowed/restricted nowhere. ")
@JsonPropertyOrder({ WirelineServiceAreaRestriction.JSON_PROPERTY_RESTRICTION_TYPE, WirelineServiceAreaRestriction.JSON_PROPERTY_AREAS })
public class WirelineServiceAreaRestriction
{
    public static final String JSON_PROPERTY_RESTRICTION_TYPE = "restrictionType";
    private String restrictionType;

    public static final String JSON_PROPERTY_AREAS = "areas";
    private List<WirelineArea> areas = null;

    public WirelineServiceAreaRestriction()
    {
    }

    public WirelineServiceAreaRestriction restrictionType(String restrictionType)
    {

        this.restrictionType = restrictionType;
        return this;
    }

    /**
     * It contains the restriction type ALLOWED_AREAS or NOT_ALLOWED_AREAS.
     * 
     * @return restrictionType
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "It contains the restriction type ALLOWED_AREAS or NOT_ALLOWED_AREAS.")
    @JsonProperty(JSON_PROPERTY_RESTRICTION_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getRestrictionType()
    {
        return restrictionType;
    }

    @JsonProperty(JSON_PROPERTY_RESTRICTION_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRestrictionType(String restrictionType)
    {
        this.restrictionType = restrictionType;
    }

    public WirelineServiceAreaRestriction areas(List<WirelineArea> areas)
    {

        this.areas = areas;
        return this;
    }

    public WirelineServiceAreaRestriction addAreasItem(WirelineArea areasItem)
    {
        if (this.areas == null)
        {
            this.areas = new ArrayList<>();
        }
        this.areas.add(areasItem);
        return this;
    }

    /**
     * Get areas
     * 
     * @return areas
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_AREAS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<WirelineArea> getAreas()
    {
        return areas;
    }

    @JsonProperty(JSON_PROPERTY_AREAS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAreas(List<WirelineArea> areas)
    {
        this.areas = areas;
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
        WirelineServiceAreaRestriction wirelineServiceAreaRestriction = (WirelineServiceAreaRestriction) o;
        return Objects.equals(this.restrictionType, wirelineServiceAreaRestriction.restrictionType)
               && Objects.equals(this.areas, wirelineServiceAreaRestriction.areas);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(restrictionType, areas);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class WirelineServiceAreaRestriction {\n");
        sb.append("    restrictionType: ").append(toIndentedString(restrictionType)).append("\n");
        sb.append("    areas: ").append(toIndentedString(areas)).append("\n");
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
