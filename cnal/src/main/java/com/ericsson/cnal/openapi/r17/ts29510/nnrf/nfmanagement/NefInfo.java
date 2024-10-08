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
 * Information of an NEF NF Instance
 */
@ApiModel(description = "Information of an NEF NF Instance")
@JsonPropertyOrder({ NefInfo.JSON_PROPERTY_NEF_ID,
                     NefInfo.JSON_PROPERTY_PFD_DATA,
                     NefInfo.JSON_PROPERTY_AF_EE_DATA,
                     NefInfo.JSON_PROPERTY_GPSI_RANGES,
                     NefInfo.JSON_PROPERTY_EXTERNAL_GROUP_IDENTIFIERS_RANGES,
                     NefInfo.JSON_PROPERTY_SERVED_FQDN_LIST,
                     NefInfo.JSON_PROPERTY_TAI_LIST,
                     NefInfo.JSON_PROPERTY_TAI_RANGE_LIST,
                     NefInfo.JSON_PROPERTY_DNAI_LIST,
                     NefInfo.JSON_PROPERTY_UN_TRUST_AF_INFO_LIST,
                     NefInfo.JSON_PROPERTY_UAS_NF_FUNCTIONALITY_IND })
public class NefInfo
{
    public static final String JSON_PROPERTY_NEF_ID = "nefId";
    private String nefId;

    public static final String JSON_PROPERTY_PFD_DATA = "pfdData";
    private PfdData pfdData;

    public static final String JSON_PROPERTY_AF_EE_DATA = "afEeData";
    private AfEventExposureData afEeData;

    public static final String JSON_PROPERTY_GPSI_RANGES = "gpsiRanges";
    private List<IdentityRange> gpsiRanges = null;

    public static final String JSON_PROPERTY_EXTERNAL_GROUP_IDENTIFIERS_RANGES = "externalGroupIdentifiersRanges";
    private List<IdentityRange> externalGroupIdentifiersRanges = null;

    public static final String JSON_PROPERTY_SERVED_FQDN_LIST = "servedFqdnList";
    private List<String> servedFqdnList = null;

    public static final String JSON_PROPERTY_TAI_LIST = "taiList";
    private List<Tai> taiList = null;

    public static final String JSON_PROPERTY_TAI_RANGE_LIST = "taiRangeList";
    private List<TaiRange> taiRangeList = null;

    public static final String JSON_PROPERTY_DNAI_LIST = "dnaiList";
    private List<String> dnaiList = null;

    public static final String JSON_PROPERTY_UN_TRUST_AF_INFO_LIST = "unTrustAfInfoList";
    private List<UnTrustAfInfo> unTrustAfInfoList = null;

    public static final String JSON_PROPERTY_UAS_NF_FUNCTIONALITY_IND = "uasNfFunctionalityInd";
    private Boolean uasNfFunctionalityInd = false;

    public NefInfo()
    {
    }

    public NefInfo nefId(String nefId)
    {

        this.nefId = nefId;
        return this;
    }

    /**
     * Identity of the NEF
     * 
     * @return nefId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Identity of the NEF")
    @JsonProperty(JSON_PROPERTY_NEF_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getNefId()
    {
        return nefId;
    }

    @JsonProperty(JSON_PROPERTY_NEF_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNefId(String nefId)
    {
        this.nefId = nefId;
    }

    public NefInfo pfdData(PfdData pfdData)
    {

        this.pfdData = pfdData;
        return this;
    }

    /**
     * Get pfdData
     * 
     * @return pfdData
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_PFD_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public PfdData getPfdData()
    {
        return pfdData;
    }

    @JsonProperty(JSON_PROPERTY_PFD_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPfdData(PfdData pfdData)
    {
        this.pfdData = pfdData;
    }

    public NefInfo afEeData(AfEventExposureData afEeData)
    {

        this.afEeData = afEeData;
        return this;
    }

    /**
     * Get afEeData
     * 
     * @return afEeData
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_AF_EE_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public AfEventExposureData getAfEeData()
    {
        return afEeData;
    }

    @JsonProperty(JSON_PROPERTY_AF_EE_DATA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAfEeData(AfEventExposureData afEeData)
    {
        this.afEeData = afEeData;
    }

    public NefInfo gpsiRanges(List<IdentityRange> gpsiRanges)
    {

        this.gpsiRanges = gpsiRanges;
        return this;
    }

    public NefInfo addGpsiRangesItem(IdentityRange gpsiRangesItem)
    {
        if (this.gpsiRanges == null)
        {
            this.gpsiRanges = new ArrayList<>();
        }
        this.gpsiRanges.add(gpsiRangesItem);
        return this;
    }

    /**
     * Get gpsiRanges
     * 
     * @return gpsiRanges
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_GPSI_RANGES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<IdentityRange> getGpsiRanges()
    {
        return gpsiRanges;
    }

    @JsonProperty(JSON_PROPERTY_GPSI_RANGES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setGpsiRanges(List<IdentityRange> gpsiRanges)
    {
        this.gpsiRanges = gpsiRanges;
    }

    public NefInfo externalGroupIdentifiersRanges(List<IdentityRange> externalGroupIdentifiersRanges)
    {

        this.externalGroupIdentifiersRanges = externalGroupIdentifiersRanges;
        return this;
    }

    public NefInfo addExternalGroupIdentifiersRangesItem(IdentityRange externalGroupIdentifiersRangesItem)
    {
        if (this.externalGroupIdentifiersRanges == null)
        {
            this.externalGroupIdentifiersRanges = new ArrayList<>();
        }
        this.externalGroupIdentifiersRanges.add(externalGroupIdentifiersRangesItem);
        return this;
    }

    /**
     * Get externalGroupIdentifiersRanges
     * 
     * @return externalGroupIdentifiersRanges
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_EXTERNAL_GROUP_IDENTIFIERS_RANGES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<IdentityRange> getExternalGroupIdentifiersRanges()
    {
        return externalGroupIdentifiersRanges;
    }

    @JsonProperty(JSON_PROPERTY_EXTERNAL_GROUP_IDENTIFIERS_RANGES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setExternalGroupIdentifiersRanges(List<IdentityRange> externalGroupIdentifiersRanges)
    {
        this.externalGroupIdentifiersRanges = externalGroupIdentifiersRanges;
    }

    public NefInfo servedFqdnList(List<String> servedFqdnList)
    {

        this.servedFqdnList = servedFqdnList;
        return this;
    }

    public NefInfo addServedFqdnListItem(String servedFqdnListItem)
    {
        if (this.servedFqdnList == null)
        {
            this.servedFqdnList = new ArrayList<>();
        }
        this.servedFqdnList.add(servedFqdnListItem);
        return this;
    }

    /**
     * Get servedFqdnList
     * 
     * @return servedFqdnList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SERVED_FQDN_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getServedFqdnList()
    {
        return servedFqdnList;
    }

    @JsonProperty(JSON_PROPERTY_SERVED_FQDN_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setServedFqdnList(List<String> servedFqdnList)
    {
        this.servedFqdnList = servedFqdnList;
    }

    public NefInfo taiList(List<Tai> taiList)
    {

        this.taiList = taiList;
        return this;
    }

    public NefInfo addTaiListItem(Tai taiListItem)
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

    public NefInfo taiRangeList(List<TaiRange> taiRangeList)
    {

        this.taiRangeList = taiRangeList;
        return this;
    }

    public NefInfo addTaiRangeListItem(TaiRange taiRangeListItem)
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

    public NefInfo dnaiList(List<String> dnaiList)
    {

        this.dnaiList = dnaiList;
        return this;
    }

    public NefInfo addDnaiListItem(String dnaiListItem)
    {
        if (this.dnaiList == null)
        {
            this.dnaiList = new ArrayList<>();
        }
        this.dnaiList.add(dnaiListItem);
        return this;
    }

    /**
     * Get dnaiList
     * 
     * @return dnaiList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_DNAI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getDnaiList()
    {
        return dnaiList;
    }

    @JsonProperty(JSON_PROPERTY_DNAI_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDnaiList(List<String> dnaiList)
    {
        this.dnaiList = dnaiList;
    }

    public NefInfo unTrustAfInfoList(List<UnTrustAfInfo> unTrustAfInfoList)
    {

        this.unTrustAfInfoList = unTrustAfInfoList;
        return this;
    }

    public NefInfo addUnTrustAfInfoListItem(UnTrustAfInfo unTrustAfInfoListItem)
    {
        if (this.unTrustAfInfoList == null)
        {
            this.unTrustAfInfoList = new ArrayList<>();
        }
        this.unTrustAfInfoList.add(unTrustAfInfoListItem);
        return this;
    }

    /**
     * Get unTrustAfInfoList
     * 
     * @return unTrustAfInfoList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_UN_TRUST_AF_INFO_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<UnTrustAfInfo> getUnTrustAfInfoList()
    {
        return unTrustAfInfoList;
    }

    @JsonProperty(JSON_PROPERTY_UN_TRUST_AF_INFO_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUnTrustAfInfoList(List<UnTrustAfInfo> unTrustAfInfoList)
    {
        this.unTrustAfInfoList = unTrustAfInfoList;
    }

    public NefInfo uasNfFunctionalityInd(Boolean uasNfFunctionalityInd)
    {

        this.uasNfFunctionalityInd = uasNfFunctionalityInd;
        return this;
    }

    /**
     * Get uasNfFunctionalityInd
     * 
     * @return uasNfFunctionalityInd
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_UAS_NF_FUNCTIONALITY_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getUasNfFunctionalityInd()
    {
        return uasNfFunctionalityInd;
    }

    @JsonProperty(JSON_PROPERTY_UAS_NF_FUNCTIONALITY_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUasNfFunctionalityInd(Boolean uasNfFunctionalityInd)
    {
        this.uasNfFunctionalityInd = uasNfFunctionalityInd;
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
        NefInfo nefInfo = (NefInfo) o;
        return Objects.equals(this.nefId, nefInfo.nefId) && Objects.equals(this.pfdData, nefInfo.pfdData) && Objects.equals(this.afEeData, nefInfo.afEeData)
               && Objects.equals(this.gpsiRanges, nefInfo.gpsiRanges)
               && Objects.equals(this.externalGroupIdentifiersRanges, nefInfo.externalGroupIdentifiersRanges)
               && Objects.equals(this.servedFqdnList, nefInfo.servedFqdnList) && Objects.equals(this.taiList, nefInfo.taiList)
               && Objects.equals(this.taiRangeList, nefInfo.taiRangeList) && Objects.equals(this.dnaiList, nefInfo.dnaiList)
               && Objects.equals(this.unTrustAfInfoList, nefInfo.unTrustAfInfoList)
               && Objects.equals(this.uasNfFunctionalityInd, nefInfo.uasNfFunctionalityInd);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(nefId,
                            pfdData,
                            afEeData,
                            gpsiRanges,
                            externalGroupIdentifiersRanges,
                            servedFqdnList,
                            taiList,
                            taiRangeList,
                            dnaiList,
                            unTrustAfInfoList,
                            uasNfFunctionalityInd);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class NefInfo {\n");
        sb.append("    nefId: ").append(toIndentedString(nefId)).append("\n");
        sb.append("    pfdData: ").append(toIndentedString(pfdData)).append("\n");
        sb.append("    afEeData: ").append(toIndentedString(afEeData)).append("\n");
        sb.append("    gpsiRanges: ").append(toIndentedString(gpsiRanges)).append("\n");
        sb.append("    externalGroupIdentifiersRanges: ").append(toIndentedString(externalGroupIdentifiersRanges)).append("\n");
        sb.append("    servedFqdnList: ").append(toIndentedString(servedFqdnList)).append("\n");
        sb.append("    taiList: ").append(toIndentedString(taiList)).append("\n");
        sb.append("    taiRangeList: ").append(toIndentedString(taiRangeList)).append("\n");
        sb.append("    dnaiList: ").append(toIndentedString(dnaiList)).append("\n");
        sb.append("    unTrustAfInfoList: ").append(toIndentedString(unTrustAfInfoList)).append("\n");
        sb.append("    uasNfFunctionalityInd: ").append(toIndentedString(uasNfFunctionalityInd)).append("\n");
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
