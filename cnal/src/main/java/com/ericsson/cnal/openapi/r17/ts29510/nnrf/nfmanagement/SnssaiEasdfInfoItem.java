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
import com.ericsson.cnal.openapi.r17.ts29571.commondata.ExtSnssai;
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
 * Set of parameters supported by EASDF for a given S-NSSAI
 */
@ApiModel(description = "Set of parameters supported by EASDF for a given S-NSSAI")
@JsonPropertyOrder({ SnssaiEasdfInfoItem.JSON_PROPERTY_S_NSSAI, SnssaiEasdfInfoItem.JSON_PROPERTY_DNN_EASDF_INFO_LIST })
public class SnssaiEasdfInfoItem
{
    public static final String JSON_PROPERTY_S_NSSAI = "sNssai";
    private ExtSnssai sNssai;

    public static final String JSON_PROPERTY_DNN_EASDF_INFO_LIST = "dnnEasdfInfoList";
    private List<DnnEasdfInfoItem> dnnEasdfInfoList = new ArrayList<>();

    public SnssaiEasdfInfoItem()
    {
    }

    public SnssaiEasdfInfoItem sNssai(ExtSnssai sNssai)
    {

        this.sNssai = sNssai;
        return this;
    }

    /**
     * Get sNssai
     * 
     * @return sNssai
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_S_NSSAI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public ExtSnssai getsNssai()
    {
        return sNssai;
    }

    @JsonProperty(JSON_PROPERTY_S_NSSAI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setsNssai(ExtSnssai sNssai)
    {
        this.sNssai = sNssai;
    }

    public SnssaiEasdfInfoItem dnnEasdfInfoList(List<DnnEasdfInfoItem> dnnEasdfInfoList)
    {

        this.dnnEasdfInfoList = dnnEasdfInfoList;
        return this;
    }

    public SnssaiEasdfInfoItem addDnnEasdfInfoListItem(DnnEasdfInfoItem dnnEasdfInfoListItem)
    {
        this.dnnEasdfInfoList.add(dnnEasdfInfoListItem);
        return this;
    }

    /**
     * Get dnnEasdfInfoList
     * 
     * @return dnnEasdfInfoList
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_DNN_EASDF_INFO_LIST)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public List<DnnEasdfInfoItem> getDnnEasdfInfoList()
    {
        return dnnEasdfInfoList;
    }

    @JsonProperty(JSON_PROPERTY_DNN_EASDF_INFO_LIST)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setDnnEasdfInfoList(List<DnnEasdfInfoItem> dnnEasdfInfoList)
    {
        this.dnnEasdfInfoList = dnnEasdfInfoList;
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
        SnssaiEasdfInfoItem snssaiEasdfInfoItem = (SnssaiEasdfInfoItem) o;
        return Objects.equals(this.sNssai, snssaiEasdfInfoItem.sNssai) && Objects.equals(this.dnnEasdfInfoList, snssaiEasdfInfoItem.dnnEasdfInfoList);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(sNssai, dnnEasdfInfoList);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class SnssaiEasdfInfoItem {\n");
        sb.append("    sNssai: ").append(toIndentedString(sNssai)).append("\n");
        sb.append("    dnnEasdfInfoList: ").append(toIndentedString(dnnEasdfInfoList)).append("\n");
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
