/*
 * Npcf_AMPolicyControl
 * Access and Mobility Policy Control Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29507.npcf.ampolicycontrol;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.Snssai;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.SliceMbr;
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
 * Contains a UE-Slice-MBR and the related information.
 */
@ApiModel(description = "Contains a UE-Slice-MBR and the related information.")
@JsonPropertyOrder({ UeSliceMbr.JSON_PROPERTY_SLICE_MBR, UeSliceMbr.JSON_PROPERTY_SERVING_SNSSAI, UeSliceMbr.JSON_PROPERTY_MAPPED_HOME_SNSSAI })
public class UeSliceMbr
{
    public static final String JSON_PROPERTY_SLICE_MBR = "sliceMbr";
    private Map<String, SliceMbr> sliceMbr = new HashMap<>();

    public static final String JSON_PROPERTY_SERVING_SNSSAI = "servingSnssai";
    private Snssai servingSnssai;

    public static final String JSON_PROPERTY_MAPPED_HOME_SNSSAI = "mappedHomeSnssai";
    private Snssai mappedHomeSnssai;

    public UeSliceMbr()
    {
    }

    public UeSliceMbr sliceMbr(Map<String, SliceMbr> sliceMbr)
    {

        this.sliceMbr = sliceMbr;
        return this;
    }

    public UeSliceMbr putSliceMbrItem(String key,
                                      SliceMbr sliceMbrItem)
    {
        this.sliceMbr.put(key, sliceMbrItem);
        return this;
    }

    /**
     * Contains the MBR for uplink and the MBR for downlink.
     * 
     * @return sliceMbr
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Contains the MBR for uplink and the MBR for downlink.")
    @JsonProperty(JSON_PROPERTY_SLICE_MBR)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Map<String, SliceMbr> getSliceMbr()
    {
        return sliceMbr;
    }

    @JsonProperty(JSON_PROPERTY_SLICE_MBR)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setSliceMbr(Map<String, SliceMbr> sliceMbr)
    {
        this.sliceMbr = sliceMbr;
    }

    public UeSliceMbr servingSnssai(Snssai servingSnssai)
    {

        this.servingSnssai = servingSnssai;
        return this;
    }

    /**
     * Get servingSnssai
     * 
     * @return servingSnssai
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_SERVING_SNSSAI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Snssai getServingSnssai()
    {
        return servingSnssai;
    }

    @JsonProperty(JSON_PROPERTY_SERVING_SNSSAI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setServingSnssai(Snssai servingSnssai)
    {
        this.servingSnssai = servingSnssai;
    }

    public UeSliceMbr mappedHomeSnssai(Snssai mappedHomeSnssai)
    {

        this.mappedHomeSnssai = mappedHomeSnssai;
        return this;
    }

    /**
     * Get mappedHomeSnssai
     * 
     * @return mappedHomeSnssai
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_MAPPED_HOME_SNSSAI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Snssai getMappedHomeSnssai()
    {
        return mappedHomeSnssai;
    }

    @JsonProperty(JSON_PROPERTY_MAPPED_HOME_SNSSAI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMappedHomeSnssai(Snssai mappedHomeSnssai)
    {
        this.mappedHomeSnssai = mappedHomeSnssai;
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
        UeSliceMbr ueSliceMbr = (UeSliceMbr) o;
        return Objects.equals(this.sliceMbr, ueSliceMbr.sliceMbr) && Objects.equals(this.servingSnssai, ueSliceMbr.servingSnssai)
               && Objects.equals(this.mappedHomeSnssai, ueSliceMbr.mappedHomeSnssai);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(sliceMbr, servingSnssai, mappedHomeSnssai);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class UeSliceMbr {\n");
        sb.append("    sliceMbr: ").append(toIndentedString(sliceMbr)).append("\n");
        sb.append("    servingSnssai: ").append(toIndentedString(servingSnssai)).append("\n");
        sb.append("    mappedHomeSnssai: ").append(toIndentedString(mappedHomeSnssai)).append("\n");
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
