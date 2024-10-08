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
 * Indicates the Inter Frequency Target information.
 */
@ApiModel(description = "Indicates the Inter Frequency Target information.")
@JsonPropertyOrder({ InterFreqTargetInfo.JSON_PROPERTY_DL_CARRIER_FREQ, InterFreqTargetInfo.JSON_PROPERTY_CELL_ID_LIST })
public class InterFreqTargetInfo
{
    public static final String JSON_PROPERTY_DL_CARRIER_FREQ = "dlCarrierFreq";
    private Integer dlCarrierFreq;

    public static final String JSON_PROPERTY_CELL_ID_LIST = "cellIdList";
    private List<Integer> cellIdList = null;

    public InterFreqTargetInfo()
    {
    }

    public InterFreqTargetInfo dlCarrierFreq(Integer dlCarrierFreq)
    {

        this.dlCarrierFreq = dlCarrierFreq;
        return this;
    }

    /**
     * Integer value indicating the ARFCN applicable for a downlink, uplink or
     * bi-directional (TDD) NR global frequency raster, as definition of
     * \&quot;ARFCN-ValueNR\&quot; IE in clause 6.3.2 of 3GPP TS 38.331. minimum: 0
     * maximum: 3279165
     * 
     * @return dlCarrierFreq
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true,
                      value = "Integer value indicating the ARFCN applicable for a downlink, uplink or bi-directional (TDD) NR global frequency raster, as definition of \"ARFCN-ValueNR\" IE in clause 6.3.2 of 3GPP TS 38.331. ")
    @JsonProperty(JSON_PROPERTY_DL_CARRIER_FREQ)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Integer getDlCarrierFreq()
    {
        return dlCarrierFreq;
    }

    @JsonProperty(JSON_PROPERTY_DL_CARRIER_FREQ)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setDlCarrierFreq(Integer dlCarrierFreq)
    {
        this.dlCarrierFreq = dlCarrierFreq;
    }

    public InterFreqTargetInfo cellIdList(List<Integer> cellIdList)
    {

        this.cellIdList = cellIdList;
        return this;
    }

    public InterFreqTargetInfo addCellIdListItem(Integer cellIdListItem)
    {
        if (this.cellIdList == null)
        {
            this.cellIdList = new ArrayList<>();
        }
        this.cellIdList.add(cellIdListItem);
        return this;
    }

    /**
     * When present, this IE shall contain a list of the physical cell identities
     * where the UE is requested to perform measurement logging for the indicated
     * frequency.
     * 
     * @return cellIdList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "When present, this IE shall contain a list of the physical cell identities where the UE is requested to perform measurement logging for the indicated frequency. ")
    @JsonProperty(JSON_PROPERTY_CELL_ID_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<Integer> getCellIdList()
    {
        return cellIdList;
    }

    @JsonProperty(JSON_PROPERTY_CELL_ID_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCellIdList(List<Integer> cellIdList)
    {
        this.cellIdList = cellIdList;
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
        InterFreqTargetInfo interFreqTargetInfo = (InterFreqTargetInfo) o;
        return Objects.equals(this.dlCarrierFreq, interFreqTargetInfo.dlCarrierFreq) && Objects.equals(this.cellIdList, interFreqTargetInfo.cellIdList);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(dlCarrierFreq, cellIdList);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class InterFreqTargetInfo {\n");
        sb.append("    dlCarrierFreq: ").append(toIndentedString(dlCarrierFreq)).append("\n");
        sb.append("    cellIdList: ").append(toIndentedString(cellIdList)).append("\n");
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
