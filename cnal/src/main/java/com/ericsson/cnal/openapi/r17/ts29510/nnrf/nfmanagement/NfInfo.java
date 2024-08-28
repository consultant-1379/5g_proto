/*
 * NRF NFManagement Service
 * NRF NFManagement Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement;

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
 * Information of a generic NF Instance
 */
@ApiModel(description = "Information of a generic NF Instance")
@JsonPropertyOrder({ NfInfo.JSON_PROPERTY_NF_TYPE })
public class NfInfo
{
    public static final String JSON_PROPERTY_NF_TYPE = "nfType";
    private String nfType;

    public NfInfo()
    {
    }

    public NfInfo nfType(String nfType)
    {

        this.nfType = nfType;
        return this;
    }

    /**
     * NF types known to NRF
     * 
     * @return nfType
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "NF types known to NRF")
    @JsonProperty(JSON_PROPERTY_NF_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getNfType()
    {
        return nfType;
    }

    @JsonProperty(JSON_PROPERTY_NF_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNfType(String nfType)
    {
        this.nfType = nfType;
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
        NfInfo nfInfo = (NfInfo) o;
        return Objects.equals(this.nfType, nfInfo.nfType);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(nfType);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class NfInfo {\n");
        sb.append("    nfType: ").append(toIndentedString(nfType)).append("\n");
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
