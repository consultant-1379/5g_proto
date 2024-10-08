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
 * This data type is defined in the same way as the &#39;HfcNodeId&#39; data
 * type, but with the OpenAPI &#39;nullable: true&#39; property.
 */
@ApiModel(description = "This data type is defined in the same way as the 'HfcNodeId' data type, but with the OpenAPI 'nullable: true' property. ")
@JsonPropertyOrder({ HfcNodeIdRm.JSON_PROPERTY_HFC_N_ID })
public class HfcNodeIdRm
{
    public static final String JSON_PROPERTY_HFC_N_ID = "hfcNId";
    private String hfcNId;

    public HfcNodeIdRm()
    {
    }

    public HfcNodeIdRm hfcNId(String hfcNId)
    {

        this.hfcNId = hfcNId;
        return this;
    }

    /**
     * This IE represents the identifier of the HFC node Id as specified in
     * CableLabs WR-TR-5WWC-ARCH. It is provisioned by the wireline operator as part
     * of wireline operations and may contain up to six characters.
     * 
     * @return hfcNId
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true,
                      value = "This IE represents the identifier of the HFC node Id as specified in CableLabs WR-TR-5WWC-ARCH. It is provisioned by the wireline operator as part of wireline operations and may contain up to six characters. ")
    @JsonProperty(JSON_PROPERTY_HFC_N_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getHfcNId()
    {
        return hfcNId;
    }

    @JsonProperty(JSON_PROPERTY_HFC_N_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setHfcNId(String hfcNId)
    {
        this.hfcNId = hfcNId;
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
        HfcNodeIdRm hfcNodeIdRm = (HfcNodeIdRm) o;
        return Objects.equals(this.hfcNId, hfcNodeIdRm.hfcNId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(hfcNId);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class HfcNodeIdRm {\n");
        sb.append("    hfcNId: ").append(toIndentedString(hfcNId)).append("\n");
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
