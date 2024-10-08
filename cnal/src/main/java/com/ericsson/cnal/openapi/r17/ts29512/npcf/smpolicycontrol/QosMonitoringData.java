/*
 * Npcf_SMPolicyControl API
 * Session Management Policy Control Service   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29512.npcf.smpolicycontrol;

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
import org.openapitools.jackson.nullable.JsonNullable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.openapitools.jackson.nullable.JsonNullable;
import java.util.NoSuchElementException;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Contains QoS monitoring related control information.
 */
@ApiModel(description = "Contains QoS monitoring related control information.")
@JsonPropertyOrder({ QosMonitoringData.JSON_PROPERTY_QM_ID,
                     QosMonitoringData.JSON_PROPERTY_REQ_QOS_MON_PARAMS,
                     QosMonitoringData.JSON_PROPERTY_REP_FREQS,
                     QosMonitoringData.JSON_PROPERTY_REP_THRESH_DL,
                     QosMonitoringData.JSON_PROPERTY_REP_THRESH_UL,
                     QosMonitoringData.JSON_PROPERTY_REP_THRESH_RP,
                     QosMonitoringData.JSON_PROPERTY_WAIT_TIME,
                     QosMonitoringData.JSON_PROPERTY_REP_PERIOD,
                     QosMonitoringData.JSON_PROPERTY_NOTIFY_URI,
                     QosMonitoringData.JSON_PROPERTY_NOTIFY_CORRE_ID,
                     QosMonitoringData.JSON_PROPERTY_DIRECT_NOTIF_IND })
public class QosMonitoringData
{
    public static final String JSON_PROPERTY_QM_ID = "qmId";
    private String qmId;

    public static final String JSON_PROPERTY_REQ_QOS_MON_PARAMS = "reqQosMonParams";
    private List<String> reqQosMonParams = new ArrayList<>();

    public static final String JSON_PROPERTY_REP_FREQS = "repFreqs";
    private List<String> repFreqs = new ArrayList<>();

    public static final String JSON_PROPERTY_REP_THRESH_DL = "repThreshDl";
    private JsonNullable<Integer> repThreshDl = JsonNullable.<Integer>undefined();

    public static final String JSON_PROPERTY_REP_THRESH_UL = "repThreshUl";
    private JsonNullable<Integer> repThreshUl = JsonNullable.<Integer>undefined();

    public static final String JSON_PROPERTY_REP_THRESH_RP = "repThreshRp";
    private JsonNullable<Integer> repThreshRp = JsonNullable.<Integer>undefined();

    public static final String JSON_PROPERTY_WAIT_TIME = "waitTime";
    private JsonNullable<Integer> waitTime = JsonNullable.<Integer>undefined();

    public static final String JSON_PROPERTY_REP_PERIOD = "repPeriod";
    private JsonNullable<Integer> repPeriod = JsonNullable.<Integer>undefined();

    public static final String JSON_PROPERTY_NOTIFY_URI = "notifyUri";
    private JsonNullable<String> notifyUri = JsonNullable.<String>undefined();

    public static final String JSON_PROPERTY_NOTIFY_CORRE_ID = "notifyCorreId";
    private JsonNullable<String> notifyCorreId = JsonNullable.<String>undefined();

    public static final String JSON_PROPERTY_DIRECT_NOTIF_IND = "directNotifInd";
    private Boolean directNotifInd;

    public QosMonitoringData()
    {
    }

    public QosMonitoringData qmId(String qmId)
    {

        this.qmId = qmId;
        return this;
    }

    /**
     * Univocally identifies the QoS monitoring policy data within a PDU session.
     * 
     * @return qmId
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Univocally identifies the QoS monitoring policy data within a PDU session.")
    @JsonProperty(JSON_PROPERTY_QM_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getQmId()
    {
        return qmId;
    }

    @JsonProperty(JSON_PROPERTY_QM_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setQmId(String qmId)
    {
        this.qmId = qmId;
    }

    public QosMonitoringData reqQosMonParams(List<String> reqQosMonParams)
    {

        this.reqQosMonParams = reqQosMonParams;
        return this;
    }

    public QosMonitoringData addReqQosMonParamsItem(String reqQosMonParamsItem)
    {
        this.reqQosMonParams.add(reqQosMonParamsItem);
        return this;
    }

    /**
     * indicates the UL packet delay, DL packet delay and/or round trip packet delay
     * between the UE and the UPF is to be monitored when the QoS Monitoring for
     * URLLC is enabled for the service data flow.
     * 
     * @return reqQosMonParams
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true,
                      value = "indicates the UL packet delay, DL packet delay and/or round trip packet delay between the UE and the UPF is to be monitored when the QoS Monitoring for URLLC is enabled for the service data flow. ")
    @JsonProperty(JSON_PROPERTY_REQ_QOS_MON_PARAMS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public List<String> getReqQosMonParams()
    {
        return reqQosMonParams;
    }

    @JsonProperty(JSON_PROPERTY_REQ_QOS_MON_PARAMS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setReqQosMonParams(List<String> reqQosMonParams)
    {
        this.reqQosMonParams = reqQosMonParams;
    }

    public QosMonitoringData repFreqs(List<String> repFreqs)
    {

        this.repFreqs = repFreqs;
        return this;
    }

    public QosMonitoringData addRepFreqsItem(String repFreqsItem)
    {
        this.repFreqs.add(repFreqsItem);
        return this;
    }

    /**
     * Get repFreqs
     * 
     * @return repFreqs
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_REP_FREQS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public List<String> getRepFreqs()
    {
        return repFreqs;
    }

    @JsonProperty(JSON_PROPERTY_REP_FREQS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setRepFreqs(List<String> repFreqs)
    {
        this.repFreqs = repFreqs;
    }

    public QosMonitoringData repThreshDl(Integer repThreshDl)
    {
        this.repThreshDl = JsonNullable.<Integer>of(repThreshDl);

        return this;
    }

    /**
     * Indicates the period of time in units of miliiseconds for DL packet delay.
     * 
     * @return repThreshDl
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Indicates the period of time in units of miliiseconds for DL packet delay.")
    @JsonIgnore

    public Integer getRepThreshDl()
    {
        return repThreshDl.orElse(null);
    }

    @JsonProperty(JSON_PROPERTY_REP_THRESH_DL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public JsonNullable<Integer> getRepThreshDl_JsonNullable()
    {
        return repThreshDl;
    }

    @JsonProperty(JSON_PROPERTY_REP_THRESH_DL)
    public void setRepThreshDl_JsonNullable(JsonNullable<Integer> repThreshDl)
    {
        this.repThreshDl = repThreshDl;
    }

    public void setRepThreshDl(Integer repThreshDl)
    {
        this.repThreshDl = JsonNullable.<Integer>of(repThreshDl);
    }

    public QosMonitoringData repThreshUl(Integer repThreshUl)
    {
        this.repThreshUl = JsonNullable.<Integer>of(repThreshUl);

        return this;
    }

    /**
     * Indicates the period of time in units of miliiseconds for UL packet delay.
     * 
     * @return repThreshUl
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Indicates the period of time in units of miliiseconds for UL packet delay.")
    @JsonIgnore

    public Integer getRepThreshUl()
    {
        return repThreshUl.orElse(null);
    }

    @JsonProperty(JSON_PROPERTY_REP_THRESH_UL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public JsonNullable<Integer> getRepThreshUl_JsonNullable()
    {
        return repThreshUl;
    }

    @JsonProperty(JSON_PROPERTY_REP_THRESH_UL)
    public void setRepThreshUl_JsonNullable(JsonNullable<Integer> repThreshUl)
    {
        this.repThreshUl = repThreshUl;
    }

    public void setRepThreshUl(Integer repThreshUl)
    {
        this.repThreshUl = JsonNullable.<Integer>of(repThreshUl);
    }

    public QosMonitoringData repThreshRp(Integer repThreshRp)
    {
        this.repThreshRp = JsonNullable.<Integer>of(repThreshRp);

        return this;
    }

    /**
     * Indicates the period of time in units of miliiseconds for round trip packet
     * delay.
     * 
     * @return repThreshRp
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Indicates the period of time in units of miliiseconds for round trip packet delay.")
    @JsonIgnore

    public Integer getRepThreshRp()
    {
        return repThreshRp.orElse(null);
    }

    @JsonProperty(JSON_PROPERTY_REP_THRESH_RP)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public JsonNullable<Integer> getRepThreshRp_JsonNullable()
    {
        return repThreshRp;
    }

    @JsonProperty(JSON_PROPERTY_REP_THRESH_RP)
    public void setRepThreshRp_JsonNullable(JsonNullable<Integer> repThreshRp)
    {
        this.repThreshRp = repThreshRp;
    }

    public void setRepThreshRp(Integer repThreshRp)
    {
        this.repThreshRp = JsonNullable.<Integer>of(repThreshRp);
    }

    public QosMonitoringData waitTime(Integer waitTime)
    {
        this.waitTime = JsonNullable.<Integer>of(waitTime);

        return this;
    }

    /**
     * indicating a time in seconds with OpenAPI defined &#39;nullable: true&#39;
     * property.
     * 
     * @return waitTime
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "indicating a time in seconds with OpenAPI defined 'nullable: true' property.")
    @JsonIgnore

    public Integer getWaitTime()
    {
        return waitTime.orElse(null);
    }

    @JsonProperty(JSON_PROPERTY_WAIT_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public JsonNullable<Integer> getWaitTime_JsonNullable()
    {
        return waitTime;
    }

    @JsonProperty(JSON_PROPERTY_WAIT_TIME)
    public void setWaitTime_JsonNullable(JsonNullable<Integer> waitTime)
    {
        this.waitTime = waitTime;
    }

    public void setWaitTime(Integer waitTime)
    {
        this.waitTime = JsonNullable.<Integer>of(waitTime);
    }

    public QosMonitoringData repPeriod(Integer repPeriod)
    {
        this.repPeriod = JsonNullable.<Integer>of(repPeriod);

        return this;
    }

    /**
     * indicating a time in seconds with OpenAPI defined &#39;nullable: true&#39;
     * property.
     * 
     * @return repPeriod
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "indicating a time in seconds with OpenAPI defined 'nullable: true' property.")
    @JsonIgnore

    public Integer getRepPeriod()
    {
        return repPeriod.orElse(null);
    }

    @JsonProperty(JSON_PROPERTY_REP_PERIOD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public JsonNullable<Integer> getRepPeriod_JsonNullable()
    {
        return repPeriod;
    }

    @JsonProperty(JSON_PROPERTY_REP_PERIOD)
    public void setRepPeriod_JsonNullable(JsonNullable<Integer> repPeriod)
    {
        this.repPeriod = repPeriod;
    }

    public void setRepPeriod(Integer repPeriod)
    {
        this.repPeriod = JsonNullable.<Integer>of(repPeriod);
    }

    public QosMonitoringData notifyUri(String notifyUri)
    {
        this.notifyUri = JsonNullable.<String>of(notifyUri);

        return this;
    }

    /**
     * String providing an URI formatted according to RFC 3986 with the OpenAPI
     * &#39;nullable: true&#39; property.
     * 
     * @return notifyUri
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String providing an URI formatted according to RFC 3986 with the OpenAPI 'nullable: true' property. ")
    @JsonIgnore

    public String getNotifyUri()
    {
        return notifyUri.orElse(null);
    }

    @JsonProperty(JSON_PROPERTY_NOTIFY_URI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public JsonNullable<String> getNotifyUri_JsonNullable()
    {
        return notifyUri;
    }

    @JsonProperty(JSON_PROPERTY_NOTIFY_URI)
    public void setNotifyUri_JsonNullable(JsonNullable<String> notifyUri)
    {
        this.notifyUri = notifyUri;
    }

    public void setNotifyUri(String notifyUri)
    {
        this.notifyUri = JsonNullable.<String>of(notifyUri);
    }

    public QosMonitoringData notifyCorreId(String notifyCorreId)
    {
        this.notifyCorreId = JsonNullable.<String>of(notifyCorreId);

        return this;
    }

    /**
     * Get notifyCorreId
     * 
     * @return notifyCorreId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonIgnore

    public String getNotifyCorreId()
    {
        return notifyCorreId.orElse(null);
    }

    @JsonProperty(JSON_PROPERTY_NOTIFY_CORRE_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public JsonNullable<String> getNotifyCorreId_JsonNullable()
    {
        return notifyCorreId;
    }

    @JsonProperty(JSON_PROPERTY_NOTIFY_CORRE_ID)
    public void setNotifyCorreId_JsonNullable(JsonNullable<String> notifyCorreId)
    {
        this.notifyCorreId = notifyCorreId;
    }

    public void setNotifyCorreId(String notifyCorreId)
    {
        this.notifyCorreId = JsonNullable.<String>of(notifyCorreId);
    }

    public QosMonitoringData directNotifInd(Boolean directNotifInd)
    {

        this.directNotifInd = directNotifInd;
        return this;
    }

    /**
     * Indicates that the direct event notification sent by UPF to the Local NEF or
     * AF is requested if it is included and set to true.
     * 
     * @return directNotifInd
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Indicates that the direct event notification sent by UPF to the Local NEF or AF is requested if it is included and set to true.")
    @JsonProperty(JSON_PROPERTY_DIRECT_NOTIF_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getDirectNotifInd()
    {
        return directNotifInd;
    }

    @JsonProperty(JSON_PROPERTY_DIRECT_NOTIF_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDirectNotifInd(Boolean directNotifInd)
    {
        this.directNotifInd = directNotifInd;
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
        QosMonitoringData qosMonitoringData = (QosMonitoringData) o;
        return Objects.equals(this.qmId, qosMonitoringData.qmId) && Objects.equals(this.reqQosMonParams, qosMonitoringData.reqQosMonParams)
               && Objects.equals(this.repFreqs, qosMonitoringData.repFreqs) && equalsNullable(this.repThreshDl, qosMonitoringData.repThreshDl)
               && equalsNullable(this.repThreshUl, qosMonitoringData.repThreshUl) && equalsNullable(this.repThreshRp, qosMonitoringData.repThreshRp)
               && equalsNullable(this.waitTime, qosMonitoringData.waitTime) && equalsNullable(this.repPeriod, qosMonitoringData.repPeriod)
               && equalsNullable(this.notifyUri, qosMonitoringData.notifyUri) && equalsNullable(this.notifyCorreId, qosMonitoringData.notifyCorreId)
               && Objects.equals(this.directNotifInd, qosMonitoringData.directNotifInd);
    }

    private static <T> boolean equalsNullable(JsonNullable<T> a,
                                              JsonNullable<T> b)
    {
        return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(qmId,
                            reqQosMonParams,
                            repFreqs,
                            hashCodeNullable(repThreshDl),
                            hashCodeNullable(repThreshUl),
                            hashCodeNullable(repThreshRp),
                            hashCodeNullable(waitTime),
                            hashCodeNullable(repPeriod),
                            hashCodeNullable(notifyUri),
                            hashCodeNullable(notifyCorreId),
                            directNotifInd);
    }

    private static <T> int hashCodeNullable(JsonNullable<T> a)
    {
        if (a == null)
        {
            return 1;
        }
        return a.isPresent() ? Arrays.deepHashCode(new Object[] { a.get() }) : 31;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class QosMonitoringData {\n");
        sb.append("    qmId: ").append(toIndentedString(qmId)).append("\n");
        sb.append("    reqQosMonParams: ").append(toIndentedString(reqQosMonParams)).append("\n");
        sb.append("    repFreqs: ").append(toIndentedString(repFreqs)).append("\n");
        sb.append("    repThreshDl: ").append(toIndentedString(repThreshDl)).append("\n");
        sb.append("    repThreshUl: ").append(toIndentedString(repThreshUl)).append("\n");
        sb.append("    repThreshRp: ").append(toIndentedString(repThreshRp)).append("\n");
        sb.append("    waitTime: ").append(toIndentedString(waitTime)).append("\n");
        sb.append("    repPeriod: ").append(toIndentedString(repPeriod)).append("\n");
        sb.append("    notifyUri: ").append(toIndentedString(notifyUri)).append("\n");
        sb.append("    notifyCorreId: ").append(toIndentedString(notifyCorreId)).append("\n");
        sb.append("    directNotifInd: ").append(toIndentedString(directNotifInd)).append("\n");
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
