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
 * One and only one of the \&quot;globLineIds\&quot;, \&quot;hfcNIds\&quot;,
 * \&quot;areaCodeB\&quot; and \&quot;areaCodeC\&quot; attributes shall be
 * included in a WirelineArea data structure
 */
@ApiModel(description = "One and only one of the \"globLineIds\", \"hfcNIds\", \"areaCodeB\" and \"areaCodeC\" attributes shall be included in a WirelineArea data structure ")
@JsonPropertyOrder({ WirelineArea.JSON_PROPERTY_GLOBAL_LINE_IDS,
                     WirelineArea.JSON_PROPERTY_HFC_N_IDS,
                     WirelineArea.JSON_PROPERTY_AREA_CODE_B,
                     WirelineArea.JSON_PROPERTY_AREA_CODE_C })
public class WirelineArea
{
    public static final String JSON_PROPERTY_GLOBAL_LINE_IDS = "globalLineIds";
    private List<byte[]> globalLineIds = null;

    public static final String JSON_PROPERTY_HFC_N_IDS = "hfcNIds";
    private List<String> hfcNIds = null;

    public static final String JSON_PROPERTY_AREA_CODE_B = "areaCodeB";
    private String areaCodeB;

    public static final String JSON_PROPERTY_AREA_CODE_C = "areaCodeC";
    private String areaCodeC;

    public WirelineArea()
    {
    }

    public WirelineArea globalLineIds(List<byte[]> globalLineIds)
    {

        this.globalLineIds = globalLineIds;
        return this;
    }

    public WirelineArea addGlobalLineIdsItem(byte[] globalLineIdsItem)
    {
        if (this.globalLineIds == null)
        {
            this.globalLineIds = new ArrayList<>();
        }
        this.globalLineIds.add(globalLineIdsItem);
        return this;
    }

    /**
     * Get globalLineIds
     * 
     * @return globalLineIds
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_GLOBAL_LINE_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<byte[]> getGlobalLineIds()
    {
        return globalLineIds;
    }

    @JsonProperty(JSON_PROPERTY_GLOBAL_LINE_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setGlobalLineIds(List<byte[]> globalLineIds)
    {
        this.globalLineIds = globalLineIds;
    }

    public WirelineArea hfcNIds(List<String> hfcNIds)
    {

        this.hfcNIds = hfcNIds;
        return this;
    }

    public WirelineArea addHfcNIdsItem(String hfcNIdsItem)
    {
        if (this.hfcNIds == null)
        {
            this.hfcNIds = new ArrayList<>();
        }
        this.hfcNIds.add(hfcNIdsItem);
        return this;
    }

    /**
     * Get hfcNIds
     * 
     * @return hfcNIds
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_HFC_N_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getHfcNIds()
    {
        return hfcNIds;
    }

    @JsonProperty(JSON_PROPERTY_HFC_N_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setHfcNIds(List<String> hfcNIds)
    {
        this.hfcNIds = hfcNIds;
    }

    public WirelineArea areaCodeB(String areaCodeB)
    {

        this.areaCodeB = areaCodeB;
        return this;
    }

    /**
     * Values are operator specific.
     * 
     * @return areaCodeB
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Values are operator specific.")
    @JsonProperty(JSON_PROPERTY_AREA_CODE_B)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getAreaCodeB()
    {
        return areaCodeB;
    }

    @JsonProperty(JSON_PROPERTY_AREA_CODE_B)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAreaCodeB(String areaCodeB)
    {
        this.areaCodeB = areaCodeB;
    }

    public WirelineArea areaCodeC(String areaCodeC)
    {

        this.areaCodeC = areaCodeC;
        return this;
    }

    /**
     * Values are operator specific.
     * 
     * @return areaCodeC
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Values are operator specific.")
    @JsonProperty(JSON_PROPERTY_AREA_CODE_C)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getAreaCodeC()
    {
        return areaCodeC;
    }

    @JsonProperty(JSON_PROPERTY_AREA_CODE_C)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAreaCodeC(String areaCodeC)
    {
        this.areaCodeC = areaCodeC;
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
        WirelineArea wirelineArea = (WirelineArea) o;
        return Objects.equals(this.globalLineIds, wirelineArea.globalLineIds) && Objects.equals(this.hfcNIds, wirelineArea.hfcNIds)
               && Objects.equals(this.areaCodeB, wirelineArea.areaCodeB) && Objects.equals(this.areaCodeC, wirelineArea.areaCodeC);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(globalLineIds, hfcNIds, areaCodeB, areaCodeC);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class WirelineArea {\n");
        sb.append("    globalLineIds: ").append(toIndentedString(globalLineIds)).append("\n");
        sb.append("    hfcNIds: ").append(toIndentedString(hfcNIds)).append("\n");
        sb.append("    areaCodeB: ").append(toIndentedString(areaCodeB)).append("\n");
        sb.append("    areaCodeC: ").append(toIndentedString(areaCodeC)).append("\n");
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
