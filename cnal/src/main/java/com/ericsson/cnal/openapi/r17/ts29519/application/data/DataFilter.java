/*
 * Unified Data Repository Service API file for Application Data
 * The API version is defined in 3GPP TS 29.504   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: -
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29519.application.data;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.Snssai;
import com.ericsson.cnal.openapi.r17.ts29522.aminfluence.DnnSnssaiInformation;
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
 * Identifies a data filter.
 */
@ApiModel(description = "Identifies a data filter.")
@JsonPropertyOrder({ DataFilter.JSON_PROPERTY_DATA_IND,
                     DataFilter.JSON_PROPERTY_DNNS,
                     DataFilter.JSON_PROPERTY_SNSSAIS,
                     DataFilter.JSON_PROPERTY_INTERNAL_GROUP_IDS,
                     DataFilter.JSON_PROPERTY_SUPIS,
                     DataFilter.JSON_PROPERTY_APP_IDS,
                     DataFilter.JSON_PROPERTY_UE_IPV4S,
                     DataFilter.JSON_PROPERTY_UE_IPV6S,
                     DataFilter.JSON_PROPERTY_UE_MACS,
                     DataFilter.JSON_PROPERTY_ANY_UE_IND,
                     DataFilter.JSON_PROPERTY_DNN_SNSSAI_INFOS })
public class DataFilter
{
    public static final String JSON_PROPERTY_DATA_IND = "dataInd";
    private String dataInd;

    public static final String JSON_PROPERTY_DNNS = "dnns";
    private List<String> dnns = null;

    public static final String JSON_PROPERTY_SNSSAIS = "snssais";
    private List<Snssai> snssais = null;

    public static final String JSON_PROPERTY_INTERNAL_GROUP_IDS = "internalGroupIds";
    private List<String> internalGroupIds = null;

    public static final String JSON_PROPERTY_SUPIS = "supis";
    private List<String> supis = null;

    public static final String JSON_PROPERTY_APP_IDS = "appIds";
    private List<String> appIds = null;

    public static final String JSON_PROPERTY_UE_IPV4S = "ueIpv4s";
    private List<String> ueIpv4s = null;

    public static final String JSON_PROPERTY_UE_IPV6S = "ueIpv6s";
    private List<String> ueIpv6s = null;

    public static final String JSON_PROPERTY_UE_MACS = "ueMacs";
    private List<String> ueMacs = null;

    public static final String JSON_PROPERTY_ANY_UE_IND = "anyUeInd";
    private Boolean anyUeInd;

    public static final String JSON_PROPERTY_DNN_SNSSAI_INFOS = "dnnSnssaiInfos";
    private List<DnnSnssaiInformation> dnnSnssaiInfos = null;

    public DataFilter()
    {
    }

    public DataFilter dataInd(String dataInd)
    {

        this.dataInd = dataInd;
        return this;
    }

    /**
     * Possible values are - PFD - IPTV - BDT - SVC_PARAM - AM
     * 
     * @return dataInd
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Possible values are - PFD - IPTV - BDT - SVC_PARAM - AM ")
    @JsonProperty(JSON_PROPERTY_DATA_IND)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getDataInd()
    {
        return dataInd;
    }

    @JsonProperty(JSON_PROPERTY_DATA_IND)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setDataInd(String dataInd)
    {
        this.dataInd = dataInd;
    }

    public DataFilter dnns(List<String> dnns)
    {

        this.dnns = dnns;
        return this;
    }

    public DataFilter addDnnsItem(String dnnsItem)
    {
        if (this.dnns == null)
        {
            this.dnns = new ArrayList<>();
        }
        this.dnns.add(dnnsItem);
        return this;
    }

    /**
     * Get dnns
     * 
     * @return dnns
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_DNNS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getDnns()
    {
        return dnns;
    }

    @JsonProperty(JSON_PROPERTY_DNNS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDnns(List<String> dnns)
    {
        this.dnns = dnns;
    }

    public DataFilter snssais(List<Snssai> snssais)
    {

        this.snssais = snssais;
        return this;
    }

    public DataFilter addSnssaisItem(Snssai snssaisItem)
    {
        if (this.snssais == null)
        {
            this.snssais = new ArrayList<>();
        }
        this.snssais.add(snssaisItem);
        return this;
    }

    /**
     * Get snssais
     * 
     * @return snssais
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SNSSAIS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<Snssai> getSnssais()
    {
        return snssais;
    }

    @JsonProperty(JSON_PROPERTY_SNSSAIS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSnssais(List<Snssai> snssais)
    {
        this.snssais = snssais;
    }

    public DataFilter internalGroupIds(List<String> internalGroupIds)
    {

        this.internalGroupIds = internalGroupIds;
        return this;
    }

    public DataFilter addInternalGroupIdsItem(String internalGroupIdsItem)
    {
        if (this.internalGroupIds == null)
        {
            this.internalGroupIds = new ArrayList<>();
        }
        this.internalGroupIds.add(internalGroupIdsItem);
        return this;
    }

    /**
     * Get internalGroupIds
     * 
     * @return internalGroupIds
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_INTERNAL_GROUP_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getInternalGroupIds()
    {
        return internalGroupIds;
    }

    @JsonProperty(JSON_PROPERTY_INTERNAL_GROUP_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setInternalGroupIds(List<String> internalGroupIds)
    {
        this.internalGroupIds = internalGroupIds;
    }

    public DataFilter supis(List<String> supis)
    {

        this.supis = supis;
        return this;
    }

    public DataFilter addSupisItem(String supisItem)
    {
        if (this.supis == null)
        {
            this.supis = new ArrayList<>();
        }
        this.supis.add(supisItem);
        return this;
    }

    /**
     * Get supis
     * 
     * @return supis
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SUPIS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getSupis()
    {
        return supis;
    }

    @JsonProperty(JSON_PROPERTY_SUPIS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSupis(List<String> supis)
    {
        this.supis = supis;
    }

    public DataFilter appIds(List<String> appIds)
    {

        this.appIds = appIds;
        return this;
    }

    public DataFilter addAppIdsItem(String appIdsItem)
    {
        if (this.appIds == null)
        {
            this.appIds = new ArrayList<>();
        }
        this.appIds.add(appIdsItem);
        return this;
    }

    /**
     * Get appIds
     * 
     * @return appIds
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_APP_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getAppIds()
    {
        return appIds;
    }

    @JsonProperty(JSON_PROPERTY_APP_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAppIds(List<String> appIds)
    {
        this.appIds = appIds;
    }

    public DataFilter ueIpv4s(List<String> ueIpv4s)
    {

        this.ueIpv4s = ueIpv4s;
        return this;
    }

    public DataFilter addUeIpv4sItem(String ueIpv4sItem)
    {
        if (this.ueIpv4s == null)
        {
            this.ueIpv4s = new ArrayList<>();
        }
        this.ueIpv4s.add(ueIpv4sItem);
        return this;
    }

    /**
     * Get ueIpv4s
     * 
     * @return ueIpv4s
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_UE_IPV4S)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getUeIpv4s()
    {
        return ueIpv4s;
    }

    @JsonProperty(JSON_PROPERTY_UE_IPV4S)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUeIpv4s(List<String> ueIpv4s)
    {
        this.ueIpv4s = ueIpv4s;
    }

    public DataFilter ueIpv6s(List<String> ueIpv6s)
    {

        this.ueIpv6s = ueIpv6s;
        return this;
    }

    public DataFilter addUeIpv6sItem(String ueIpv6sItem)
    {
        if (this.ueIpv6s == null)
        {
            this.ueIpv6s = new ArrayList<>();
        }
        this.ueIpv6s.add(ueIpv6sItem);
        return this;
    }

    /**
     * Get ueIpv6s
     * 
     * @return ueIpv6s
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_UE_IPV6S)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getUeIpv6s()
    {
        return ueIpv6s;
    }

    @JsonProperty(JSON_PROPERTY_UE_IPV6S)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUeIpv6s(List<String> ueIpv6s)
    {
        this.ueIpv6s = ueIpv6s;
    }

    public DataFilter ueMacs(List<String> ueMacs)
    {

        this.ueMacs = ueMacs;
        return this;
    }

    public DataFilter addUeMacsItem(String ueMacsItem)
    {
        if (this.ueMacs == null)
        {
            this.ueMacs = new ArrayList<>();
        }
        this.ueMacs.add(ueMacsItem);
        return this;
    }

    /**
     * Get ueMacs
     * 
     * @return ueMacs
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_UE_MACS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getUeMacs()
    {
        return ueMacs;
    }

    @JsonProperty(JSON_PROPERTY_UE_MACS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUeMacs(List<String> ueMacs)
    {
        this.ueMacs = ueMacs;
    }

    public DataFilter anyUeInd(Boolean anyUeInd)
    {

        this.anyUeInd = anyUeInd;
        return this;
    }

    /**
     * Indicates the request is for any UE.
     * 
     * @return anyUeInd
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Indicates the request is for any UE.")
    @JsonProperty(JSON_PROPERTY_ANY_UE_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getAnyUeInd()
    {
        return anyUeInd;
    }

    @JsonProperty(JSON_PROPERTY_ANY_UE_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAnyUeInd(Boolean anyUeInd)
    {
        this.anyUeInd = anyUeInd;
    }

    public DataFilter dnnSnssaiInfos(List<DnnSnssaiInformation> dnnSnssaiInfos)
    {

        this.dnnSnssaiInfos = dnnSnssaiInfos;
        return this;
    }

    public DataFilter addDnnSnssaiInfosItem(DnnSnssaiInformation dnnSnssaiInfosItem)
    {
        if (this.dnnSnssaiInfos == null)
        {
            this.dnnSnssaiInfos = new ArrayList<>();
        }
        this.dnnSnssaiInfos.add(dnnSnssaiInfosItem);
        return this;
    }

    /**
     * Indicates the request is for any DNN and S-NSSAI combination present in the
     * array.
     * 
     * @return dnnSnssaiInfos
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Indicates the request is for any DNN and S-NSSAI combination present in the array.")
    @JsonProperty(JSON_PROPERTY_DNN_SNSSAI_INFOS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<DnnSnssaiInformation> getDnnSnssaiInfos()
    {
        return dnnSnssaiInfos;
    }

    @JsonProperty(JSON_PROPERTY_DNN_SNSSAI_INFOS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDnnSnssaiInfos(List<DnnSnssaiInformation> dnnSnssaiInfos)
    {
        this.dnnSnssaiInfos = dnnSnssaiInfos;
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
        DataFilter dataFilter = (DataFilter) o;
        return Objects.equals(this.dataInd, dataFilter.dataInd) && Objects.equals(this.dnns, dataFilter.dnns)
               && Objects.equals(this.snssais, dataFilter.snssais) && Objects.equals(this.internalGroupIds, dataFilter.internalGroupIds)
               && Objects.equals(this.supis, dataFilter.supis) && Objects.equals(this.appIds, dataFilter.appIds)
               && Objects.equals(this.ueIpv4s, dataFilter.ueIpv4s) && Objects.equals(this.ueIpv6s, dataFilter.ueIpv6s)
               && Objects.equals(this.ueMacs, dataFilter.ueMacs) && Objects.equals(this.anyUeInd, dataFilter.anyUeInd)
               && Objects.equals(this.dnnSnssaiInfos, dataFilter.dnnSnssaiInfos);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(dataInd, dnns, snssais, internalGroupIds, supis, appIds, ueIpv4s, ueIpv6s, ueMacs, anyUeInd, dnnSnssaiInfos);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class DataFilter {\n");
        sb.append("    dataInd: ").append(toIndentedString(dataInd)).append("\n");
        sb.append("    dnns: ").append(toIndentedString(dnns)).append("\n");
        sb.append("    snssais: ").append(toIndentedString(snssais)).append("\n");
        sb.append("    internalGroupIds: ").append(toIndentedString(internalGroupIds)).append("\n");
        sb.append("    supis: ").append(toIndentedString(supis)).append("\n");
        sb.append("    appIds: ").append(toIndentedString(appIds)).append("\n");
        sb.append("    ueIpv4s: ").append(toIndentedString(ueIpv4s)).append("\n");
        sb.append("    ueIpv6s: ").append(toIndentedString(ueIpv6s)).append("\n");
        sb.append("    ueMacs: ").append(toIndentedString(ueMacs)).append("\n");
        sb.append("    anyUeInd: ").append(toIndentedString(anyUeInd)).append("\n");
        sb.append("    dnnSnssaiInfos: ").append(toIndentedString(dnnSnssaiInfos)).append("\n");
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
