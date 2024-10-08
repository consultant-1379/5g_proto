/*
 * Namf_EventExposure
 * AMF Event Exposure Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29518.namf.eventexposure;

import java.util.Objects;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Additional filters for UE in Area Report event
 */
@ApiModel(description = "Additional filters for UE in Area Report event")
@JsonPropertyOrder({ UeInAreaFilter.JSON_PROPERTY_UE_TYPE, UeInAreaFilter.JSON_PROPERTY_AERIAL_SRV_DNN_IND })
public class UeInAreaFilter
{
    public static final String JSON_PROPERTY_UE_TYPE = "ueType";
    private String ueType;

    public static final String JSON_PROPERTY_AERIAL_SRV_DNN_IND = "aerialSrvDnnInd";
    private Boolean aerialSrvDnnInd = false;

    public UeInAreaFilter()
    {
    }

    public UeInAreaFilter ueType(String ueType)
    {

        this.ueType = ueType;
        return this;
    }

    /**
     * Describes the type of UEs
     * 
     * @return ueType
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Describes the type of UEs")
    @JsonProperty(JSON_PROPERTY_UE_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getUeType()
    {
        return ueType;
    }

    @JsonProperty(JSON_PROPERTY_UE_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUeType(String ueType)
    {
        this.ueType = ueType;
    }

    public UeInAreaFilter aerialSrvDnnInd(Boolean aerialSrvDnnInd)
    {

        this.aerialSrvDnnInd = aerialSrvDnnInd;
        return this;
    }

    /**
     * Get aerialSrvDnnInd
     * 
     * @return aerialSrvDnnInd
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_AERIAL_SRV_DNN_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getAerialSrvDnnInd()
    {
        return aerialSrvDnnInd;
    }

    @JsonProperty(JSON_PROPERTY_AERIAL_SRV_DNN_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAerialSrvDnnInd(Boolean aerialSrvDnnInd)
    {
        this.aerialSrvDnnInd = aerialSrvDnnInd;
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
        UeInAreaFilter ueInAreaFilter = (UeInAreaFilter) o;
        return Objects.equals(this.ueType, ueInAreaFilter.ueType) && Objects.equals(this.aerialSrvDnnInd, ueInAreaFilter.aerialSrvDnnInd);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(ueType, aerialSrvDnnInd);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class UeInAreaFilter {\n");
        sb.append("    ueType: ").append(toIndentedString(ueType)).append("\n");
        sb.append("    aerialSrvDnnInd: ").append(toIndentedString(aerialSrvDnnInd)).append("\n");
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
