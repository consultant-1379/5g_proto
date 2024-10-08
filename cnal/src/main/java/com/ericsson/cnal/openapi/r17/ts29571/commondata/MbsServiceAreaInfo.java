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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * MBS Service Area Information for location dependent MBS session
 */
@ApiModel(description = "MBS Service Area Information for location dependent MBS session")
@JsonPropertyOrder({ MbsServiceAreaInfo.JSON_PROPERTY_AREA_SESSION_ID, MbsServiceAreaInfo.JSON_PROPERTY_MBS_SERVICE_AREA })
public class MbsServiceAreaInfo
{
    public static final String JSON_PROPERTY_AREA_SESSION_ID = "areaSessionId";
    private Integer areaSessionId;

    public static final String JSON_PROPERTY_MBS_SERVICE_AREA = "mbsServiceArea";
    private MbsServiceArea mbsServiceArea;

    public MbsServiceAreaInfo()
    {
    }

    public MbsServiceAreaInfo areaSessionId(Integer areaSessionId)
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
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Integer where the allowed values correspond to the value range of an unsigned 16-bit integer.")
    @JsonProperty(JSON_PROPERTY_AREA_SESSION_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Integer getAreaSessionId()
    {
        return areaSessionId;
    }

    @JsonProperty(JSON_PROPERTY_AREA_SESSION_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setAreaSessionId(Integer areaSessionId)
    {
        this.areaSessionId = areaSessionId;
    }

    public MbsServiceAreaInfo mbsServiceArea(MbsServiceArea mbsServiceArea)
    {

        this.mbsServiceArea = mbsServiceArea;
        return this;
    }

    /**
     * Get mbsServiceArea
     * 
     * @return mbsServiceArea
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_MBS_SERVICE_AREA)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public MbsServiceArea getMbsServiceArea()
    {
        return mbsServiceArea;
    }

    @JsonProperty(JSON_PROPERTY_MBS_SERVICE_AREA)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setMbsServiceArea(MbsServiceArea mbsServiceArea)
    {
        this.mbsServiceArea = mbsServiceArea;
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
        MbsServiceAreaInfo mbsServiceAreaInfo = (MbsServiceAreaInfo) o;
        return Objects.equals(this.areaSessionId, mbsServiceAreaInfo.areaSessionId) && Objects.equals(this.mbsServiceArea, mbsServiceAreaInfo.mbsServiceArea);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(areaSessionId, mbsServiceArea);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class MbsServiceAreaInfo {\n");
        sb.append("    areaSessionId: ").append(toIndentedString(areaSessionId)).append("\n");
        sb.append("    mbsServiceArea: ").append(toIndentedString(mbsServiceArea)).append("\n");
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
