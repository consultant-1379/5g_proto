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
import com.ericsson.cnal.openapi.r17.ts29122.cpprovisioning.ScheduledCommunicationTime;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.OffsetDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Represents UE communication information.
 */
@ApiModel(description = "Represents UE communication information.")
@JsonPropertyOrder({ UeCommunication.JSON_PROPERTY_COMM_DUR,
                     UeCommunication.JSON_PROPERTY_COMM_DUR_VARIANCE,
                     UeCommunication.JSON_PROPERTY_PERIO_TIME,
                     UeCommunication.JSON_PROPERTY_PERIO_TIME_VARIANCE,
                     UeCommunication.JSON_PROPERTY_TS,
                     UeCommunication.JSON_PROPERTY_TS_VARIANCE,
                     UeCommunication.JSON_PROPERTY_RECURRING_TIME,
                     UeCommunication.JSON_PROPERTY_TRAF_CHAR,
                     UeCommunication.JSON_PROPERTY_RATIO,
                     UeCommunication.JSON_PROPERTY_PERIO_COMM_IND,
                     UeCommunication.JSON_PROPERTY_CONFIDENCE,
                     UeCommunication.JSON_PROPERTY_ANA_OF_APP_LIST,
                     UeCommunication.JSON_PROPERTY_SESS_INACT_TIMER })
public class UeCommunication
{
    public static final String JSON_PROPERTY_COMM_DUR = "commDur";
    private Integer commDur;

    public static final String JSON_PROPERTY_COMM_DUR_VARIANCE = "commDurVariance";
    private Float commDurVariance;

    public static final String JSON_PROPERTY_PERIO_TIME = "perioTime";
    private Integer perioTime;

    public static final String JSON_PROPERTY_PERIO_TIME_VARIANCE = "perioTimeVariance";
    private Float perioTimeVariance;

    public static final String JSON_PROPERTY_TS = "ts";
    private OffsetDateTime ts;

    public static final String JSON_PROPERTY_TS_VARIANCE = "tsVariance";
    private Float tsVariance;

    public static final String JSON_PROPERTY_RECURRING_TIME = "recurringTime";
    private ScheduledCommunicationTime recurringTime;

    public static final String JSON_PROPERTY_TRAF_CHAR = "trafChar";
    private TrafficCharacterization trafChar;

    public static final String JSON_PROPERTY_RATIO = "ratio";
    private Integer ratio;

    public static final String JSON_PROPERTY_PERIO_COMM_IND = "perioCommInd";
    private Boolean perioCommInd;

    public static final String JSON_PROPERTY_CONFIDENCE = "confidence";
    private Integer confidence;

    public static final String JSON_PROPERTY_ANA_OF_APP_LIST = "anaOfAppList";
    private AppListForUeComm anaOfAppList;

    public static final String JSON_PROPERTY_SESS_INACT_TIMER = "sessInactTimer";
    private SessInactTimerForUeComm sessInactTimer;

    public UeCommunication()
    {
    }

    public UeCommunication commDur(Integer commDur)
    {

        this.commDur = commDur;
        return this;
    }

    /**
     * indicating a time in seconds.
     * 
     * @return commDur
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "indicating a time in seconds.")
    @JsonProperty(JSON_PROPERTY_COMM_DUR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getCommDur()
    {
        return commDur;
    }

    @JsonProperty(JSON_PROPERTY_COMM_DUR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCommDur(Integer commDur)
    {
        this.commDur = commDur;
    }

    public UeCommunication commDurVariance(Float commDurVariance)
    {

        this.commDurVariance = commDurVariance;
        return this;
    }

    /**
     * string with format &#39;float&#39; as defined in OpenAPI.
     * 
     * @return commDurVariance
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'float' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_COMM_DUR_VARIANCE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Float getCommDurVariance()
    {
        return commDurVariance;
    }

    @JsonProperty(JSON_PROPERTY_COMM_DUR_VARIANCE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCommDurVariance(Float commDurVariance)
    {
        this.commDurVariance = commDurVariance;
    }

    public UeCommunication perioTime(Integer perioTime)
    {

        this.perioTime = perioTime;
        return this;
    }

    /**
     * indicating a time in seconds.
     * 
     * @return perioTime
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "indicating a time in seconds.")
    @JsonProperty(JSON_PROPERTY_PERIO_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getPerioTime()
    {
        return perioTime;
    }

    @JsonProperty(JSON_PROPERTY_PERIO_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPerioTime(Integer perioTime)
    {
        this.perioTime = perioTime;
    }

    public UeCommunication perioTimeVariance(Float perioTimeVariance)
    {

        this.perioTimeVariance = perioTimeVariance;
        return this;
    }

    /**
     * string with format &#39;float&#39; as defined in OpenAPI.
     * 
     * @return perioTimeVariance
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'float' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_PERIO_TIME_VARIANCE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Float getPerioTimeVariance()
    {
        return perioTimeVariance;
    }

    @JsonProperty(JSON_PROPERTY_PERIO_TIME_VARIANCE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPerioTimeVariance(Float perioTimeVariance)
    {
        this.perioTimeVariance = perioTimeVariance;
    }

    public UeCommunication ts(OffsetDateTime ts)
    {

        this.ts = ts;
        return this;
    }

    /**
     * string with format &#39;date-time&#39; as defined in OpenAPI.
     * 
     * @return ts
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'date-time' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_TS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getTs()
    {
        return ts;
    }

    @JsonProperty(JSON_PROPERTY_TS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTs(OffsetDateTime ts)
    {
        this.ts = ts;
    }

    public UeCommunication tsVariance(Float tsVariance)
    {

        this.tsVariance = tsVariance;
        return this;
    }

    /**
     * string with format &#39;float&#39; as defined in OpenAPI.
     * 
     * @return tsVariance
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'float' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_TS_VARIANCE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Float getTsVariance()
    {
        return tsVariance;
    }

    @JsonProperty(JSON_PROPERTY_TS_VARIANCE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTsVariance(Float tsVariance)
    {
        this.tsVariance = tsVariance;
    }

    public UeCommunication recurringTime(ScheduledCommunicationTime recurringTime)
    {

        this.recurringTime = recurringTime;
        return this;
    }

    /**
     * Get recurringTime
     * 
     * @return recurringTime
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_RECURRING_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public ScheduledCommunicationTime getRecurringTime()
    {
        return recurringTime;
    }

    @JsonProperty(JSON_PROPERTY_RECURRING_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRecurringTime(ScheduledCommunicationTime recurringTime)
    {
        this.recurringTime = recurringTime;
    }

    public UeCommunication trafChar(TrafficCharacterization trafChar)
    {

        this.trafChar = trafChar;
        return this;
    }

    /**
     * Get trafChar
     * 
     * @return trafChar
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_TRAF_CHAR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public TrafficCharacterization getTrafChar()
    {
        return trafChar;
    }

    @JsonProperty(JSON_PROPERTY_TRAF_CHAR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTrafChar(TrafficCharacterization trafChar)
    {
        this.trafChar = trafChar;
    }

    public UeCommunication ratio(Integer ratio)
    {

        this.ratio = ratio;
        return this;
    }

    /**
     * Unsigned integer indicating Sampling Ratio (see clauses 4.15.1 of 3GPP TS
     * 23.502), expressed in percent. minimum: 1 maximum: 100
     * 
     * @return ratio
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Unsigned integer indicating Sampling Ratio (see clauses 4.15.1 of 3GPP TS 23.502), expressed in percent.  ")
    @JsonProperty(JSON_PROPERTY_RATIO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getRatio()
    {
        return ratio;
    }

    @JsonProperty(JSON_PROPERTY_RATIO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRatio(Integer ratio)
    {
        this.ratio = ratio;
    }

    public UeCommunication perioCommInd(Boolean perioCommInd)
    {

        this.perioCommInd = perioCommInd;
        return this;
    }

    /**
     * Get perioCommInd
     * 
     * @return perioCommInd
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_PERIO_COMM_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getPerioCommInd()
    {
        return perioCommInd;
    }

    @JsonProperty(JSON_PROPERTY_PERIO_COMM_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPerioCommInd(Boolean perioCommInd)
    {
        this.perioCommInd = perioCommInd;
    }

    public UeCommunication confidence(Integer confidence)
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

    public UeCommunication anaOfAppList(AppListForUeComm anaOfAppList)
    {

        this.anaOfAppList = anaOfAppList;
        return this;
    }

    /**
     * Get anaOfAppList
     * 
     * @return anaOfAppList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_ANA_OF_APP_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public AppListForUeComm getAnaOfAppList()
    {
        return anaOfAppList;
    }

    @JsonProperty(JSON_PROPERTY_ANA_OF_APP_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAnaOfAppList(AppListForUeComm anaOfAppList)
    {
        this.anaOfAppList = anaOfAppList;
    }

    public UeCommunication sessInactTimer(SessInactTimerForUeComm sessInactTimer)
    {

        this.sessInactTimer = sessInactTimer;
        return this;
    }

    /**
     * Get sessInactTimer
     * 
     * @return sessInactTimer
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SESS_INACT_TIMER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public SessInactTimerForUeComm getSessInactTimer()
    {
        return sessInactTimer;
    }

    @JsonProperty(JSON_PROPERTY_SESS_INACT_TIMER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSessInactTimer(SessInactTimerForUeComm sessInactTimer)
    {
        this.sessInactTimer = sessInactTimer;
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
        UeCommunication ueCommunication = (UeCommunication) o;
        return Objects.equals(this.commDur, ueCommunication.commDur) && Objects.equals(this.commDurVariance, ueCommunication.commDurVariance)
               && Objects.equals(this.perioTime, ueCommunication.perioTime) && Objects.equals(this.perioTimeVariance, ueCommunication.perioTimeVariance)
               && Objects.equals(this.ts, ueCommunication.ts) && Objects.equals(this.tsVariance, ueCommunication.tsVariance)
               && Objects.equals(this.recurringTime, ueCommunication.recurringTime) && Objects.equals(this.trafChar, ueCommunication.trafChar)
               && Objects.equals(this.ratio, ueCommunication.ratio) && Objects.equals(this.perioCommInd, ueCommunication.perioCommInd)
               && Objects.equals(this.confidence, ueCommunication.confidence) && Objects.equals(this.anaOfAppList, ueCommunication.anaOfAppList)
               && Objects.equals(this.sessInactTimer, ueCommunication.sessInactTimer);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(commDur,
                            commDurVariance,
                            perioTime,
                            perioTimeVariance,
                            ts,
                            tsVariance,
                            recurringTime,
                            trafChar,
                            ratio,
                            perioCommInd,
                            confidence,
                            anaOfAppList,
                            sessInactTimer);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class UeCommunication {\n");
        sb.append("    commDur: ").append(toIndentedString(commDur)).append("\n");
        sb.append("    commDurVariance: ").append(toIndentedString(commDurVariance)).append("\n");
        sb.append("    perioTime: ").append(toIndentedString(perioTime)).append("\n");
        sb.append("    perioTimeVariance: ").append(toIndentedString(perioTimeVariance)).append("\n");
        sb.append("    ts: ").append(toIndentedString(ts)).append("\n");
        sb.append("    tsVariance: ").append(toIndentedString(tsVariance)).append("\n");
        sb.append("    recurringTime: ").append(toIndentedString(recurringTime)).append("\n");
        sb.append("    trafChar: ").append(toIndentedString(trafChar)).append("\n");
        sb.append("    ratio: ").append(toIndentedString(ratio)).append("\n");
        sb.append("    perioCommInd: ").append(toIndentedString(perioCommInd)).append("\n");
        sb.append("    confidence: ").append(toIndentedString(confidence)).append("\n");
        sb.append("    anaOfAppList: ").append(toIndentedString(anaOfAppList)).append("\n");
        sb.append("    sessInactTimer: ").append(toIndentedString(sessInactTimer)).append("\n");
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
