/*
 * Namf_EventExposure
 * AMF Event Exposure Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29518.namf.eventexposure;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.Tai;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.Ecgi;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.Ncgi;
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
 * Dispersion Area
 */
@ApiModel(description = "Dispersion Area")
@JsonPropertyOrder({ DispersionArea.JSON_PROPERTY_TAI_LIST,
                     DispersionArea.JSON_PROPERTY_NCGI_LIST,
                     DispersionArea.JSON_PROPERTY_ECGI_LIST,
                     DispersionArea.JSON_PROPERTY_N3GA_IND })
public class DispersionArea
{
    public static final String JSON_PROPERTY_TAI_LIST = "taiList";
    private List<Tai> taiList = null;

    public static final String JSON_PROPERTY_NCGI_LIST = "ncgiList";
    private List<Ncgi> ncgiList = null;

    public static final String JSON_PROPERTY_ECGI_LIST = "ecgiList";
    private List<Ecgi> ecgiList = null;

    public static final String JSON_PROPERTY_N3GA_IND = "n3gaInd";
    private Boolean n3gaInd = false;

    public DispersionArea()
    {
    }

    public DispersionArea taiList(List<Tai> taiList)
    {

        this.taiList = taiList;
        return this;
    }

    public DispersionArea addTaiListItem(Tai taiListItem)
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

    public DispersionArea ncgiList(List<Ncgi> ncgiList)
    {

        this.ncgiList = ncgiList;
        return this;
    }

    public DispersionArea addNcgiListItem(Ncgi ncgiListItem)
    {
        if (this.ncgiList == null)
        {
            this.ncgiList = new ArrayList<>();
        }
        this.ncgiList.add(ncgiListItem);
        return this;
    }

    /**
     * Get ncgiList
     * 
     * @return ncgiList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_NCGI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<Ncgi> getNcgiList()
    {
        return ncgiList;
    }

    @JsonProperty(JSON_PROPERTY_NCGI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNcgiList(List<Ncgi> ncgiList)
    {
        this.ncgiList = ncgiList;
    }

    public DispersionArea ecgiList(List<Ecgi> ecgiList)
    {

        this.ecgiList = ecgiList;
        return this;
    }

    public DispersionArea addEcgiListItem(Ecgi ecgiListItem)
    {
        if (this.ecgiList == null)
        {
            this.ecgiList = new ArrayList<>();
        }
        this.ecgiList.add(ecgiListItem);
        return this;
    }

    /**
     * Get ecgiList
     * 
     * @return ecgiList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_ECGI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<Ecgi> getEcgiList()
    {
        return ecgiList;
    }

    @JsonProperty(JSON_PROPERTY_ECGI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEcgiList(List<Ecgi> ecgiList)
    {
        this.ecgiList = ecgiList;
    }

    public DispersionArea n3gaInd(Boolean n3gaInd)
    {

        this.n3gaInd = n3gaInd;
        return this;
    }

    /**
     * Get n3gaInd
     * 
     * @return n3gaInd
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_N3GA_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getN3gaInd()
    {
        return n3gaInd;
    }

    @JsonProperty(JSON_PROPERTY_N3GA_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setN3gaInd(Boolean n3gaInd)
    {
        this.n3gaInd = n3gaInd;
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
        DispersionArea dispersionArea = (DispersionArea) o;
        return Objects.equals(this.taiList, dispersionArea.taiList) && Objects.equals(this.ncgiList, dispersionArea.ncgiList)
               && Objects.equals(this.ecgiList, dispersionArea.ecgiList) && Objects.equals(this.n3gaInd, dispersionArea.n3gaInd);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(taiList, ncgiList, ecgiList, n3gaInd);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class DispersionArea {\n");
        sb.append("    taiList: ").append(toIndentedString(taiList)).append("\n");
        sb.append("    ncgiList: ").append(toIndentedString(ncgiList)).append("\n");
        sb.append("    ecgiList: ").append(toIndentedString(ecgiList)).append("\n");
        sb.append("    n3gaInd: ").append(toIndentedString(n3gaInd)).append("\n");
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
