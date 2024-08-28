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
import com.ericsson.cnal.openapi.r17.ts29571.commondata.Tai;
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
 * Information of a NSACF NF Instance
 */
@ApiModel(description = "Information of a NSACF NF Instance")
@JsonPropertyOrder({ NsacfInfo.JSON_PROPERTY_NSACF_CAPABILITY, NsacfInfo.JSON_PROPERTY_TAI_LIST, NsacfInfo.JSON_PROPERTY_TAI_RANGE_LIST })
public class NsacfInfo
{
    public static final String JSON_PROPERTY_NSACF_CAPABILITY = "nsacfCapability";
    private NsacfCapability nsacfCapability;

    public static final String JSON_PROPERTY_TAI_LIST = "taiList";
    private List<Tai> taiList = null;

    public static final String JSON_PROPERTY_TAI_RANGE_LIST = "taiRangeList";
    private List<TaiRange> taiRangeList = null;

    public NsacfInfo()
    {
    }

    public NsacfInfo nsacfCapability(NsacfCapability nsacfCapability)
    {

        this.nsacfCapability = nsacfCapability;
        return this;
    }

    /**
     * Get nsacfCapability
     * 
     * @return nsacfCapability
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_NSACF_CAPABILITY)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public NsacfCapability getNsacfCapability()
    {
        return nsacfCapability;
    }

    @JsonProperty(JSON_PROPERTY_NSACF_CAPABILITY)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setNsacfCapability(NsacfCapability nsacfCapability)
    {
        this.nsacfCapability = nsacfCapability;
    }

    public NsacfInfo taiList(List<Tai> taiList)
    {

        this.taiList = taiList;
        return this;
    }

    public NsacfInfo addTaiListItem(Tai taiListItem)
    {
        if (this.taiList == null)
        {
            this.taiList = new ArrayList<>();
        }
        this.taiList.add(taiListItem);
        return this;
    }

    /**
     * Get taiList
     * 
     * @return taiList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_TAI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<Tai> getTaiList()
    {
        return taiList;
    }

    @JsonProperty(JSON_PROPERTY_TAI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTaiList(List<Tai> taiList)
    {
        this.taiList = taiList;
    }

    public NsacfInfo taiRangeList(List<TaiRange> taiRangeList)
    {

        this.taiRangeList = taiRangeList;
        return this;
    }

    public NsacfInfo addTaiRangeListItem(TaiRange taiRangeListItem)
    {
        if (this.taiRangeList == null)
        {
            this.taiRangeList = new ArrayList<>();
        }
        this.taiRangeList.add(taiRangeListItem);
        return this;
    }

    /**
     * Get taiRangeList
     * 
     * @return taiRangeList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_TAI_RANGE_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<TaiRange> getTaiRangeList()
    {
        return taiRangeList;
    }

    @JsonProperty(JSON_PROPERTY_TAI_RANGE_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTaiRangeList(List<TaiRange> taiRangeList)
    {
        this.taiRangeList = taiRangeList;
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
        NsacfInfo nsacfInfo = (NsacfInfo) o;
        return Objects.equals(this.nsacfCapability, nsacfInfo.nsacfCapability) && Objects.equals(this.taiList, nsacfInfo.taiList)
               && Objects.equals(this.taiRangeList, nsacfInfo.taiRangeList);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(nsacfCapability, taiList, taiRangeList);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class NsacfInfo {\n");
        sb.append("    nsacfCapability: ").append(toIndentedString(nsacfCapability)).append("\n");
        sb.append("    taiList: ").append(toIndentedString(taiList)).append("\n");
        sb.append("    taiRangeList: ").append(toIndentedString(taiRangeList)).append("\n");
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
