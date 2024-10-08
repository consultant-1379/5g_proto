/*
 * Npcf_BDTPolicyControl Service API
 * PCF BDT Policy Control Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29554.npcf.bdtpolicycontrol;

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
 * Describes the updates in authorization data of an Individual BDT Policy
 * created by the PCF.
 */
@ApiModel(description = "Describes the updates in authorization data of an Individual BDT Policy created by the PCF. ")
@JsonPropertyOrder({ PatchBdtPolicy.JSON_PROPERTY_BDT_POL_DATA, PatchBdtPolicy.JSON_PROPERTY_BDT_REQ_DATA })
public class PatchBdtPolicy
{
    public static final String JSON_PROPERTY_BDT_POL_DATA = "bdtPolData";
    private BdtPolicyDataPatch bdtPolData;

    public static final String JSON_PROPERTY_BDT_REQ_DATA = "bdtReqData";
    private BdtReqDataPatch bdtReqData;

    public PatchBdtPolicy()
    {
    }

    public PatchBdtPolicy bdtPolData(BdtPolicyDataPatch bdtPolData)
    {

        this.bdtPolData = bdtPolData;
        return this;
    }

    /**
     * Get bdtPolData
     * 
     * @return bdtPolData
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_BDT_POL_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public BdtPolicyDataPatch getBdtPolData()
    {
        return bdtPolData;
    }

    @JsonProperty(JSON_PROPERTY_BDT_POL_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBdtPolData(BdtPolicyDataPatch bdtPolData)
    {
        this.bdtPolData = bdtPolData;
    }

    public PatchBdtPolicy bdtReqData(BdtReqDataPatch bdtReqData)
    {

        this.bdtReqData = bdtReqData;
        return this;
    }

    /**
     * Get bdtReqData
     * 
     * @return bdtReqData
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_BDT_REQ_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public BdtReqDataPatch getBdtReqData()
    {
        return bdtReqData;
    }

    @JsonProperty(JSON_PROPERTY_BDT_REQ_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBdtReqData(BdtReqDataPatch bdtReqData)
    {
        this.bdtReqData = bdtReqData;
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
        PatchBdtPolicy patchBdtPolicy = (PatchBdtPolicy) o;
        return Objects.equals(this.bdtPolData, patchBdtPolicy.bdtPolData) && Objects.equals(this.bdtReqData, patchBdtPolicy.bdtReqData);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(bdtPolData, bdtReqData);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class PatchBdtPolicy {\n");
        sb.append("    bdtPolData: ").append(toIndentedString(bdtPolData)).append("\n");
        sb.append("    bdtReqData: ").append(toIndentedString(bdtReqData)).append("\n");
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
