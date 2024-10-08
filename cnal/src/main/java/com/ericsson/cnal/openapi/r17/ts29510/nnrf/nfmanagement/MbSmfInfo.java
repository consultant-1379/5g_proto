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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Information of an MB-SMF NF Instance
 */
@ApiModel(description = "Information of an MB-SMF NF Instance")
@JsonPropertyOrder({ MbSmfInfo.JSON_PROPERTY_S_NSSAI_INFO_LIST,
                     MbSmfInfo.JSON_PROPERTY_TMGI_RANGE_LIST,
                     MbSmfInfo.JSON_PROPERTY_TAI_LIST,
                     MbSmfInfo.JSON_PROPERTY_TAI_RANGE_LIST,
                     MbSmfInfo.JSON_PROPERTY_MBS_SESSION_LIST })
public class MbSmfInfo
{
    public static final String JSON_PROPERTY_S_NSSAI_INFO_LIST = "sNssaiInfoList";
    private Map<String, SnssaiMbSmfInfoItem> sNssaiInfoList = null;

    public static final String JSON_PROPERTY_TMGI_RANGE_LIST = "tmgiRangeList";
    private Map<String, TmgiRange> tmgiRangeList = null;

    public static final String JSON_PROPERTY_TAI_LIST = "taiList";
    private List<Tai> taiList = null;

    public static final String JSON_PROPERTY_TAI_RANGE_LIST = "taiRangeList";
    private List<TaiRange> taiRangeList = null;

    public static final String JSON_PROPERTY_MBS_SESSION_LIST = "mbsSessionList";
    private Map<String, MbsSession> mbsSessionList = null;

    public MbSmfInfo()
    {
    }

    public MbSmfInfo sNssaiInfoList(Map<String, SnssaiMbSmfInfoItem> sNssaiInfoList)
    {

        this.sNssaiInfoList = sNssaiInfoList;
        return this;
    }

    public MbSmfInfo putSNssaiInfoListItem(String key,
                                           SnssaiMbSmfInfoItem sNssaiInfoListItem)
    {
        if (this.sNssaiInfoList == null)
        {
            this.sNssaiInfoList = new HashMap<>();
        }
        this.sNssaiInfoList.put(key, sNssaiInfoListItem);
        return this;
    }

    /**
     * A map (list of key-value pairs) where a valid JSON string serves as key
     * 
     * @return sNssaiInfoList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "A map (list of key-value pairs) where a valid JSON string serves as key")
    @JsonProperty(JSON_PROPERTY_S_NSSAI_INFO_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Map<String, SnssaiMbSmfInfoItem> getsNssaiInfoList()
    {
        return sNssaiInfoList;
    }

    @JsonProperty(JSON_PROPERTY_S_NSSAI_INFO_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setsNssaiInfoList(Map<String, SnssaiMbSmfInfoItem> sNssaiInfoList)
    {
        this.sNssaiInfoList = sNssaiInfoList;
    }

    public MbSmfInfo tmgiRangeList(Map<String, TmgiRange> tmgiRangeList)
    {

        this.tmgiRangeList = tmgiRangeList;
        return this;
    }

    public MbSmfInfo putTmgiRangeListItem(String key,
                                          TmgiRange tmgiRangeListItem)
    {
        if (this.tmgiRangeList == null)
        {
            this.tmgiRangeList = new HashMap<>();
        }
        this.tmgiRangeList.put(key, tmgiRangeListItem);
        return this;
    }

    /**
     * A map (list of key-value pairs) where a valid JSON string serves as key
     * 
     * @return tmgiRangeList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "A map (list of key-value pairs) where a valid JSON string serves as key")
    @JsonProperty(JSON_PROPERTY_TMGI_RANGE_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Map<String, TmgiRange> getTmgiRangeList()
    {
        return tmgiRangeList;
    }

    @JsonProperty(JSON_PROPERTY_TMGI_RANGE_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTmgiRangeList(Map<String, TmgiRange> tmgiRangeList)
    {
        this.tmgiRangeList = tmgiRangeList;
    }

    public MbSmfInfo taiList(List<Tai> taiList)
    {

        this.taiList = taiList;
        return this;
    }

    public MbSmfInfo addTaiListItem(Tai taiListItem)
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

    public MbSmfInfo taiRangeList(List<TaiRange> taiRangeList)
    {

        this.taiRangeList = taiRangeList;
        return this;
    }

    public MbSmfInfo addTaiRangeListItem(TaiRange taiRangeListItem)
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

    public MbSmfInfo mbsSessionList(Map<String, MbsSession> mbsSessionList)
    {

        this.mbsSessionList = mbsSessionList;
        return this;
    }

    public MbSmfInfo putMbsSessionListItem(String key,
                                           MbsSession mbsSessionListItem)
    {
        if (this.mbsSessionList == null)
        {
            this.mbsSessionList = new HashMap<>();
        }
        this.mbsSessionList.put(key, mbsSessionListItem);
        return this;
    }

    /**
     * A map (list of key-value pairs) where a valid JSON string serves as key
     * 
     * @return mbsSessionList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "A map (list of key-value pairs) where a valid JSON string serves as key")
    @JsonProperty(JSON_PROPERTY_MBS_SESSION_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Map<String, MbsSession> getMbsSessionList()
    {
        return mbsSessionList;
    }

    @JsonProperty(JSON_PROPERTY_MBS_SESSION_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMbsSessionList(Map<String, MbsSession> mbsSessionList)
    {
        this.mbsSessionList = mbsSessionList;
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
        MbSmfInfo mbSmfInfo = (MbSmfInfo) o;
        return Objects.equals(this.sNssaiInfoList, mbSmfInfo.sNssaiInfoList) && Objects.equals(this.tmgiRangeList, mbSmfInfo.tmgiRangeList)
               && Objects.equals(this.taiList, mbSmfInfo.taiList) && Objects.equals(this.taiRangeList, mbSmfInfo.taiRangeList)
               && Objects.equals(this.mbsSessionList, mbSmfInfo.mbsSessionList);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(sNssaiInfoList, tmgiRangeList, taiList, taiRangeList, mbsSessionList);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class MbSmfInfo {\n");
        sb.append("    sNssaiInfoList: ").append(toIndentedString(sNssaiInfoList)).append("\n");
        sb.append("    tmgiRangeList: ").append(toIndentedString(tmgiRangeList)).append("\n");
        sb.append("    taiList: ").append(toIndentedString(taiList)).append("\n");
        sb.append("    taiRangeList: ").append(toIndentedString(taiRangeList)).append("\n");
        sb.append("    mbsSessionList: ").append(toIndentedString(mbsSessionList)).append("\n");
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
