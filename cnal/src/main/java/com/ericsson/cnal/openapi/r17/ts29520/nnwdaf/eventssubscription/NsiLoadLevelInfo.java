/*
 * Nnwdaf_EventsSubscription
 * Nnwdaf_EventsSubscription Service API.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29520.nnwdaf.eventssubscription;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29122.commondata.TimeWindow;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.Snssai;
import com.ericsson.cnal.openapi.r17.ts29554.npcf.bdtpolicycontrol.NetworkAreaInfo;
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
 * Represents the network slice and optionally the associated network slice
 * instance and the load level information.
 */
@ApiModel(description = "Represents the network slice and optionally the associated network slice instance and the  load level information. ")
@JsonPropertyOrder({ NsiLoadLevelInfo.JSON_PROPERTY_LOAD_LEVEL_INFORMATION,
                     NsiLoadLevelInfo.JSON_PROPERTY_SNSSAI,
                     NsiLoadLevelInfo.JSON_PROPERTY_NSI_ID,
                     NsiLoadLevelInfo.JSON_PROPERTY_RES_USAGE,
                     NsiLoadLevelInfo.JSON_PROPERTY_NUM_OF_EXCEED_LOAD_LEVEL_THR,
                     NsiLoadLevelInfo.JSON_PROPERTY_EXCEED_LOAD_LEVEL_THR_IND,
                     NsiLoadLevelInfo.JSON_PROPERTY_NETWORK_AREA,
                     NsiLoadLevelInfo.JSON_PROPERTY_TIME_PERIOD,
                     NsiLoadLevelInfo.JSON_PROPERTY_RES_USG_THR_CROSS_TIME_PERIOD,
                     NsiLoadLevelInfo.JSON_PROPERTY_NUM_OF_UES,
                     NsiLoadLevelInfo.JSON_PROPERTY_NUM_OF_PDU_SESS,
                     NsiLoadLevelInfo.JSON_PROPERTY_CONFIDENCE })
public class NsiLoadLevelInfo
{
    public static final String JSON_PROPERTY_LOAD_LEVEL_INFORMATION = "loadLevelInformation";
    private Integer loadLevelInformation;

    public static final String JSON_PROPERTY_SNSSAI = "snssai";
    private Snssai snssai;

    public static final String JSON_PROPERTY_NSI_ID = "nsiId";
    private String nsiId;

    public static final String JSON_PROPERTY_RES_USAGE = "resUsage";
    private ResourceUsage resUsage;

    public static final String JSON_PROPERTY_NUM_OF_EXCEED_LOAD_LEVEL_THR = "numOfExceedLoadLevelThr";
    private Integer numOfExceedLoadLevelThr;

    public static final String JSON_PROPERTY_EXCEED_LOAD_LEVEL_THR_IND = "exceedLoadLevelThrInd";
    private Boolean exceedLoadLevelThrInd;

    public static final String JSON_PROPERTY_NETWORK_AREA = "networkArea";
    private NetworkAreaInfo networkArea;

    public static final String JSON_PROPERTY_TIME_PERIOD = "timePeriod";
    private TimeWindow timePeriod;

    public static final String JSON_PROPERTY_RES_USG_THR_CROSS_TIME_PERIOD = "resUsgThrCrossTimePeriod";
    private List<TimeWindow> resUsgThrCrossTimePeriod = null;

    public static final String JSON_PROPERTY_NUM_OF_UES = "numOfUes";
    private NumberAverage numOfUes;

    public static final String JSON_PROPERTY_NUM_OF_PDU_SESS = "numOfPduSess";
    private NumberAverage numOfPduSess;

    public static final String JSON_PROPERTY_CONFIDENCE = "confidence";
    private Integer confidence;

    public NsiLoadLevelInfo()
    {
    }

    public NsiLoadLevelInfo loadLevelInformation(Integer loadLevelInformation)
    {

        this.loadLevelInformation = loadLevelInformation;
        return this;
    }

    /**
     * Load level information of the network slice and the optionally associated
     * network slice instance.
     * 
     * @return loadLevelInformation
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Load level information of the network slice and the optionally associated network slice  instance. ")
    @JsonProperty(JSON_PROPERTY_LOAD_LEVEL_INFORMATION)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Integer getLoadLevelInformation()
    {
        return loadLevelInformation;
    }

    @JsonProperty(JSON_PROPERTY_LOAD_LEVEL_INFORMATION)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setLoadLevelInformation(Integer loadLevelInformation)
    {
        this.loadLevelInformation = loadLevelInformation;
    }

    public NsiLoadLevelInfo snssai(Snssai snssai)
    {

        this.snssai = snssai;
        return this;
    }

    /**
     * Get snssai
     * 
     * @return snssai
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_SNSSAI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Snssai getSnssai()
    {
        return snssai;
    }

    @JsonProperty(JSON_PROPERTY_SNSSAI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setSnssai(Snssai snssai)
    {
        this.snssai = snssai;
    }

    public NsiLoadLevelInfo nsiId(String nsiId)
    {

        this.nsiId = nsiId;
        return this;
    }

    /**
     * Contains the Identifier of the selected Network Slice instance
     * 
     * @return nsiId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Contains the Identifier of the selected Network Slice instance")
    @JsonProperty(JSON_PROPERTY_NSI_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getNsiId()
    {
        return nsiId;
    }

    @JsonProperty(JSON_PROPERTY_NSI_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNsiId(String nsiId)
    {
        this.nsiId = nsiId;
    }

    public NsiLoadLevelInfo resUsage(ResourceUsage resUsage)
    {

        this.resUsage = resUsage;
        return this;
    }

    /**
     * Get resUsage
     * 
     * @return resUsage
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_RES_USAGE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public ResourceUsage getResUsage()
    {
        return resUsage;
    }

    @JsonProperty(JSON_PROPERTY_RES_USAGE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setResUsage(ResourceUsage resUsage)
    {
        this.resUsage = resUsage;
    }

    public NsiLoadLevelInfo numOfExceedLoadLevelThr(Integer numOfExceedLoadLevelThr)
    {

        this.numOfExceedLoadLevelThr = numOfExceedLoadLevelThr;
        return this;
    }

    /**
     * Unsigned Integer, i.e. only value 0 and integers above 0 are permissible.
     * minimum: 0
     * 
     * @return numOfExceedLoadLevelThr
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Unsigned Integer, i.e. only value 0 and integers above 0 are permissible.")
    @JsonProperty(JSON_PROPERTY_NUM_OF_EXCEED_LOAD_LEVEL_THR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getNumOfExceedLoadLevelThr()
    {
        return numOfExceedLoadLevelThr;
    }

    @JsonProperty(JSON_PROPERTY_NUM_OF_EXCEED_LOAD_LEVEL_THR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNumOfExceedLoadLevelThr(Integer numOfExceedLoadLevelThr)
    {
        this.numOfExceedLoadLevelThr = numOfExceedLoadLevelThr;
    }

    public NsiLoadLevelInfo exceedLoadLevelThrInd(Boolean exceedLoadLevelThrInd)
    {

        this.exceedLoadLevelThrInd = exceedLoadLevelThrInd;
        return this;
    }

    /**
     * Get exceedLoadLevelThrInd
     * 
     * @return exceedLoadLevelThrInd
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_EXCEED_LOAD_LEVEL_THR_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getExceedLoadLevelThrInd()
    {
        return exceedLoadLevelThrInd;
    }

    @JsonProperty(JSON_PROPERTY_EXCEED_LOAD_LEVEL_THR_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setExceedLoadLevelThrInd(Boolean exceedLoadLevelThrInd)
    {
        this.exceedLoadLevelThrInd = exceedLoadLevelThrInd;
    }

    public NsiLoadLevelInfo networkArea(NetworkAreaInfo networkArea)
    {

        this.networkArea = networkArea;
        return this;
    }

    /**
     * Get networkArea
     * 
     * @return networkArea
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_NETWORK_AREA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public NetworkAreaInfo getNetworkArea()
    {
        return networkArea;
    }

    @JsonProperty(JSON_PROPERTY_NETWORK_AREA)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNetworkArea(NetworkAreaInfo networkArea)
    {
        this.networkArea = networkArea;
    }

    public NsiLoadLevelInfo timePeriod(TimeWindow timePeriod)
    {

        this.timePeriod = timePeriod;
        return this;
    }

    /**
     * Get timePeriod
     * 
     * @return timePeriod
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_TIME_PERIOD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public TimeWindow getTimePeriod()
    {
        return timePeriod;
    }

    @JsonProperty(JSON_PROPERTY_TIME_PERIOD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTimePeriod(TimeWindow timePeriod)
    {
        this.timePeriod = timePeriod;
    }

    public NsiLoadLevelInfo resUsgThrCrossTimePeriod(List<TimeWindow> resUsgThrCrossTimePeriod)
    {

        this.resUsgThrCrossTimePeriod = resUsgThrCrossTimePeriod;
        return this;
    }

    public NsiLoadLevelInfo addResUsgThrCrossTimePeriodItem(TimeWindow resUsgThrCrossTimePeriodItem)
    {
        if (this.resUsgThrCrossTimePeriod == null)
        {
            this.resUsgThrCrossTimePeriod = new ArrayList<>();
        }
        this.resUsgThrCrossTimePeriod.add(resUsgThrCrossTimePeriodItem);
        return this;
    }

    /**
     * Each element indicates the time elapsed between times each threshold is met
     * or exceeded or crossed. The start time and end time are the exact time stamps
     * of the resource usage threshold is reached or exceeded. May be present if the
     * \&quot;listOfAnaSubsets\&quot; attribute is provided and the maximum number
     * of instances shall not exceed the value provided in the
     * \&quot;numOfExceedLoadLevelThr\&quot; attribute.
     * 
     * @return resUsgThrCrossTimePeriod
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Each element indicates the time elapsed between times each threshold is met or exceeded or crossed. The start time and end time are the exact time stamps of the resource usage threshold is reached or exceeded. May be present if the \"listOfAnaSubsets\" attribute is  provided and the maximum number of instances shall not exceed the value provided in the  \"numOfExceedLoadLevelThr\" attribute. ")
    @JsonProperty(JSON_PROPERTY_RES_USG_THR_CROSS_TIME_PERIOD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<TimeWindow> getResUsgThrCrossTimePeriod()
    {
        return resUsgThrCrossTimePeriod;
    }

    @JsonProperty(JSON_PROPERTY_RES_USG_THR_CROSS_TIME_PERIOD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setResUsgThrCrossTimePeriod(List<TimeWindow> resUsgThrCrossTimePeriod)
    {
        this.resUsgThrCrossTimePeriod = resUsgThrCrossTimePeriod;
    }

    public NsiLoadLevelInfo numOfUes(NumberAverage numOfUes)
    {

        this.numOfUes = numOfUes;
        return this;
    }

    /**
     * Get numOfUes
     * 
     * @return numOfUes
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_NUM_OF_UES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public NumberAverage getNumOfUes()
    {
        return numOfUes;
    }

    @JsonProperty(JSON_PROPERTY_NUM_OF_UES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNumOfUes(NumberAverage numOfUes)
    {
        this.numOfUes = numOfUes;
    }

    public NsiLoadLevelInfo numOfPduSess(NumberAverage numOfPduSess)
    {

        this.numOfPduSess = numOfPduSess;
        return this;
    }

    /**
     * Get numOfPduSess
     * 
     * @return numOfPduSess
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_NUM_OF_PDU_SESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public NumberAverage getNumOfPduSess()
    {
        return numOfPduSess;
    }

    @JsonProperty(JSON_PROPERTY_NUM_OF_PDU_SESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNumOfPduSess(NumberAverage numOfPduSess)
    {
        this.numOfPduSess = numOfPduSess;
    }

    public NsiLoadLevelInfo confidence(Integer confidence)
    {

        this.confidence = confidence;
        return this;
    }

    /**
     * Unsigned Integer, i.e. only value 0 and integers above 0 are permissible.
     * minimum: 0
     * 
     * @return confidence
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Unsigned Integer, i.e. only value 0 and integers above 0 are permissible.")
    @JsonProperty(JSON_PROPERTY_CONFIDENCE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getConfidence()
    {
        return confidence;
    }

    @JsonProperty(JSON_PROPERTY_CONFIDENCE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setConfidence(Integer confidence)
    {
        this.confidence = confidence;
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
        NsiLoadLevelInfo nsiLoadLevelInfo = (NsiLoadLevelInfo) o;
        return Objects.equals(this.loadLevelInformation, nsiLoadLevelInfo.loadLevelInformation) && Objects.equals(this.snssai, nsiLoadLevelInfo.snssai)
               && Objects.equals(this.nsiId, nsiLoadLevelInfo.nsiId) && Objects.equals(this.resUsage, nsiLoadLevelInfo.resUsage)
               && Objects.equals(this.numOfExceedLoadLevelThr, nsiLoadLevelInfo.numOfExceedLoadLevelThr)
               && Objects.equals(this.exceedLoadLevelThrInd, nsiLoadLevelInfo.exceedLoadLevelThrInd)
               && Objects.equals(this.networkArea, nsiLoadLevelInfo.networkArea) && Objects.equals(this.timePeriod, nsiLoadLevelInfo.timePeriod)
               && Objects.equals(this.resUsgThrCrossTimePeriod, nsiLoadLevelInfo.resUsgThrCrossTimePeriod)
               && Objects.equals(this.numOfUes, nsiLoadLevelInfo.numOfUes) && Objects.equals(this.numOfPduSess, nsiLoadLevelInfo.numOfPduSess)
               && Objects.equals(this.confidence, nsiLoadLevelInfo.confidence);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(loadLevelInformation,
                            snssai,
                            nsiId,
                            resUsage,
                            numOfExceedLoadLevelThr,
                            exceedLoadLevelThrInd,
                            networkArea,
                            timePeriod,
                            resUsgThrCrossTimePeriod,
                            numOfUes,
                            numOfPduSess,
                            confidence);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class NsiLoadLevelInfo {\n");
        sb.append("    loadLevelInformation: ").append(toIndentedString(loadLevelInformation)).append("\n");
        sb.append("    snssai: ").append(toIndentedString(snssai)).append("\n");
        sb.append("    nsiId: ").append(toIndentedString(nsiId)).append("\n");
        sb.append("    resUsage: ").append(toIndentedString(resUsage)).append("\n");
        sb.append("    numOfExceedLoadLevelThr: ").append(toIndentedString(numOfExceedLoadLevelThr)).append("\n");
        sb.append("    exceedLoadLevelThrInd: ").append(toIndentedString(exceedLoadLevelThrInd)).append("\n");
        sb.append("    networkArea: ").append(toIndentedString(networkArea)).append("\n");
        sb.append("    timePeriod: ").append(toIndentedString(timePeriod)).append("\n");
        sb.append("    resUsgThrCrossTimePeriod: ").append(toIndentedString(resUsgThrCrossTimePeriod)).append("\n");
        sb.append("    numOfUes: ").append(toIndentedString(numOfUes)).append("\n");
        sb.append("    numOfPduSess: ").append(toIndentedString(numOfPduSess)).append("\n");
        sb.append("    confidence: ").append(toIndentedString(confidence)).append("\n");
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
