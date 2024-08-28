/*
 * Namf_Communication
 * AMF Communication Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29518.namf.communication;

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
 * NPN Access Information.
 */
@ApiModel(description = "NPN Access Information.")
@JsonPropertyOrder({ NpnAccessInfo.JSON_PROPERTY_CELL_CAG_INFO })
public class NpnAccessInfo
{
    public static final String JSON_PROPERTY_CELL_CAG_INFO = "cellCagInfo";
    private List<String> cellCagInfo = null;

    public NpnAccessInfo()
    {
    }

    public NpnAccessInfo cellCagInfo(List<String> cellCagInfo)
    {

        this.cellCagInfo = cellCagInfo;
        return this;
    }

    public NpnAccessInfo addCellCagInfoItem(String cellCagInfoItem)
    {
        if (this.cellCagInfo == null)
        {
            this.cellCagInfo = new ArrayList<>();
        }
        this.cellCagInfo.add(cellCagInfoItem);
        return this;
    }

    /**
     * Get cellCagInfo
     * 
     * @return cellCagInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_CELL_CAG_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getCellCagInfo()
    {
        return cellCagInfo;
    }

    @JsonProperty(JSON_PROPERTY_CELL_CAG_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCellCagInfo(List<String> cellCagInfo)
    {
        this.cellCagInfo = cellCagInfo;
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
        NpnAccessInfo npnAccessInfo = (NpnAccessInfo) o;
        return Objects.equals(this.cellCagInfo, npnAccessInfo.cellCagInfo);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(cellCagInfo);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class NpnAccessInfo {\n");
        sb.append("    cellCagInfo: ").append(toIndentedString(cellCagInfo)).append("\n");
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
