/*
 * Nudm_EE
 * Nudm Event Exposure Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29503.nudm.ee;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.Snssai;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * MonitoringConfiguration
 */
@JsonPropertyOrder({ MonitoringConfiguration.JSON_PROPERTY_EVENT_TYPE,
                     MonitoringConfiguration.JSON_PROPERTY_IMMEDIATE_FLAG,
                     MonitoringConfiguration.JSON_PROPERTY_LOCATION_REPORTING_CONFIGURATION,
                     MonitoringConfiguration.JSON_PROPERTY_ASSOCIATION_TYPE,
                     MonitoringConfiguration.JSON_PROPERTY_DATALINK_REPORT_CFG,
                     MonitoringConfiguration.JSON_PROPERTY_LOSS_CONNECTIVITY_CFG,
                     MonitoringConfiguration.JSON_PROPERTY_MAXIMUM_LATENCY,
                     MonitoringConfiguration.JSON_PROPERTY_MAXIMUM_RESPONSE_TIME,
                     MonitoringConfiguration.JSON_PROPERTY_SUGGESTED_PACKET_NUM_DL,
                     MonitoringConfiguration.JSON_PROPERTY_DNN,
                     MonitoringConfiguration.JSON_PROPERTY_SINGLE_NSSAI,
                     MonitoringConfiguration.JSON_PROPERTY_PDU_SESSION_STATUS_CFG,
                     MonitoringConfiguration.JSON_PROPERTY_REACHABILITY_FOR_SMS_CFG,
                     MonitoringConfiguration.JSON_PROPERTY_MTC_PROVIDER_INFORMATION,
                     MonitoringConfiguration.JSON_PROPERTY_AF_ID,
                     MonitoringConfiguration.JSON_PROPERTY_REACHABILITY_FOR_DATA_CFG,
                     MonitoringConfiguration.JSON_PROPERTY_IDLE_STATUS_IND })
public class MonitoringConfiguration
{
    public static final String JSON_PROPERTY_EVENT_TYPE = "eventType";
    private String eventType;

    public static final String JSON_PROPERTY_IMMEDIATE_FLAG = "immediateFlag";
    private Boolean immediateFlag;

    public static final String JSON_PROPERTY_LOCATION_REPORTING_CONFIGURATION = "locationReportingConfiguration";
    private LocationReportingConfiguration locationReportingConfiguration;

    public static final String JSON_PROPERTY_ASSOCIATION_TYPE = "associationType";
    private String associationType;

    public static final String JSON_PROPERTY_DATALINK_REPORT_CFG = "datalinkReportCfg";
    private DatalinkReportingConfiguration datalinkReportCfg;

    public static final String JSON_PROPERTY_LOSS_CONNECTIVITY_CFG = "lossConnectivityCfg";
    private LossConnectivityCfg lossConnectivityCfg;

    public static final String JSON_PROPERTY_MAXIMUM_LATENCY = "maximumLatency";
    private Integer maximumLatency;

    public static final String JSON_PROPERTY_MAXIMUM_RESPONSE_TIME = "maximumResponseTime";
    private Integer maximumResponseTime;

    public static final String JSON_PROPERTY_SUGGESTED_PACKET_NUM_DL = "suggestedPacketNumDl";
    private Integer suggestedPacketNumDl;

    public static final String JSON_PROPERTY_DNN = "dnn";
    private String dnn;

    public static final String JSON_PROPERTY_SINGLE_NSSAI = "singleNssai";
    private Snssai singleNssai;

    public static final String JSON_PROPERTY_PDU_SESSION_STATUS_CFG = "pduSessionStatusCfg";
    private PduSessionStatusCfg pduSessionStatusCfg;

    public static final String JSON_PROPERTY_REACHABILITY_FOR_SMS_CFG = "reachabilityForSmsCfg";
    private String reachabilityForSmsCfg;

    public static final String JSON_PROPERTY_MTC_PROVIDER_INFORMATION = "mtcProviderInformation";
    private String mtcProviderInformation;

    public static final String JSON_PROPERTY_AF_ID = "afId";
    private String afId;

    public static final String JSON_PROPERTY_REACHABILITY_FOR_DATA_CFG = "reachabilityForDataCfg";
    private ReachabilityForDataConfiguration reachabilityForDataCfg;

    public static final String JSON_PROPERTY_IDLE_STATUS_IND = "idleStatusInd";
    private Boolean idleStatusInd = false;

    public MonitoringConfiguration()
    {
    }

    public MonitoringConfiguration eventType(String eventType)
    {

        this.eventType = eventType;
        return this;
    }

    /**
     * Get eventType
     * 
     * @return eventType
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_EVENT_TYPE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getEventType()
    {
        return eventType;
    }

    @JsonProperty(JSON_PROPERTY_EVENT_TYPE)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setEventType(String eventType)
    {
        this.eventType = eventType;
    }

    public MonitoringConfiguration immediateFlag(Boolean immediateFlag)
    {

        this.immediateFlag = immediateFlag;
        return this;
    }

    /**
     * Get immediateFlag
     * 
     * @return immediateFlag
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_IMMEDIATE_FLAG)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getImmediateFlag()
    {
        return immediateFlag;
    }

    @JsonProperty(JSON_PROPERTY_IMMEDIATE_FLAG)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setImmediateFlag(Boolean immediateFlag)
    {
        this.immediateFlag = immediateFlag;
    }

    public MonitoringConfiguration locationReportingConfiguration(LocationReportingConfiguration locationReportingConfiguration)
    {

        this.locationReportingConfiguration = locationReportingConfiguration;
        return this;
    }

    /**
     * Get locationReportingConfiguration
     * 
     * @return locationReportingConfiguration
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_LOCATION_REPORTING_CONFIGURATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public LocationReportingConfiguration getLocationReportingConfiguration()
    {
        return locationReportingConfiguration;
    }

    @JsonProperty(JSON_PROPERTY_LOCATION_REPORTING_CONFIGURATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLocationReportingConfiguration(LocationReportingConfiguration locationReportingConfiguration)
    {
        this.locationReportingConfiguration = locationReportingConfiguration;
    }

    public MonitoringConfiguration associationType(String associationType)
    {

        this.associationType = associationType;
        return this;
    }

    /**
     * Get associationType
     * 
     * @return associationType
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_ASSOCIATION_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getAssociationType()
    {
        return associationType;
    }

    @JsonProperty(JSON_PROPERTY_ASSOCIATION_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAssociationType(String associationType)
    {
        this.associationType = associationType;
    }

    public MonitoringConfiguration datalinkReportCfg(DatalinkReportingConfiguration datalinkReportCfg)
    {

        this.datalinkReportCfg = datalinkReportCfg;
        return this;
    }

    /**
     * Get datalinkReportCfg
     * 
     * @return datalinkReportCfg
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_DATALINK_REPORT_CFG)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public DatalinkReportingConfiguration getDatalinkReportCfg()
    {
        return datalinkReportCfg;
    }

    @JsonProperty(JSON_PROPERTY_DATALINK_REPORT_CFG)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDatalinkReportCfg(DatalinkReportingConfiguration datalinkReportCfg)
    {
        this.datalinkReportCfg = datalinkReportCfg;
    }

    public MonitoringConfiguration lossConnectivityCfg(LossConnectivityCfg lossConnectivityCfg)
    {

        this.lossConnectivityCfg = lossConnectivityCfg;
        return this;
    }

    /**
     * Get lossConnectivityCfg
     * 
     * @return lossConnectivityCfg
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_LOSS_CONNECTIVITY_CFG)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public LossConnectivityCfg getLossConnectivityCfg()
    {
        return lossConnectivityCfg;
    }

    @JsonProperty(JSON_PROPERTY_LOSS_CONNECTIVITY_CFG)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLossConnectivityCfg(LossConnectivityCfg lossConnectivityCfg)
    {
        this.lossConnectivityCfg = lossConnectivityCfg;
    }

    public MonitoringConfiguration maximumLatency(Integer maximumLatency)
    {

        this.maximumLatency = maximumLatency;
        return this;
    }

    /**
     * indicating a time in seconds.
     * 
     * @return maximumLatency
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "indicating a time in seconds.")
    @JsonProperty(JSON_PROPERTY_MAXIMUM_LATENCY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getMaximumLatency()
    {
        return maximumLatency;
    }

    @JsonProperty(JSON_PROPERTY_MAXIMUM_LATENCY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMaximumLatency(Integer maximumLatency)
    {
        this.maximumLatency = maximumLatency;
    }

    public MonitoringConfiguration maximumResponseTime(Integer maximumResponseTime)
    {

        this.maximumResponseTime = maximumResponseTime;
        return this;
    }

    /**
     * indicating a time in seconds.
     * 
     * @return maximumResponseTime
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "indicating a time in seconds.")
    @JsonProperty(JSON_PROPERTY_MAXIMUM_RESPONSE_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getMaximumResponseTime()
    {
        return maximumResponseTime;
    }

    @JsonProperty(JSON_PROPERTY_MAXIMUM_RESPONSE_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMaximumResponseTime(Integer maximumResponseTime)
    {
        this.maximumResponseTime = maximumResponseTime;
    }

    public MonitoringConfiguration suggestedPacketNumDl(Integer suggestedPacketNumDl)
    {

        this.suggestedPacketNumDl = suggestedPacketNumDl;
        return this;
    }

    /**
     * Get suggestedPacketNumDl minimum: 1
     * 
     * @return suggestedPacketNumDl
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SUGGESTED_PACKET_NUM_DL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getSuggestedPacketNumDl()
    {
        return suggestedPacketNumDl;
    }

    @JsonProperty(JSON_PROPERTY_SUGGESTED_PACKET_NUM_DL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSuggestedPacketNumDl(Integer suggestedPacketNumDl)
    {
        this.suggestedPacketNumDl = suggestedPacketNumDl;
    }

    public MonitoringConfiguration dnn(String dnn)
    {

        this.dnn = dnn;
        return this;
    }

    /**
     * String representing a Data Network as defined in clause 9A of 3GPP TS 23.003;
     * it shall contain either a DNN Network Identifier, or a full DNN with both the
     * Network Identifier and Operator Identifier, as specified in 3GPP TS 23.003
     * clause 9.1.1 and 9.1.2. It shall be coded as string in which the labels are
     * separated by dots (e.g. \&quot;Label1.Label2.Label3\&quot;).
     * 
     * @return dnn
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String representing a Data Network as defined in clause 9A of 3GPP TS 23.003;  it shall contain either a DNN Network Identifier, or a full DNN with both the Network  Identifier and Operator Identifier, as specified in 3GPP TS 23.003 clause 9.1.1 and 9.1.2. It shall be coded as string in which the labels are separated by dots  (e.g. \"Label1.Label2.Label3\"). ")
    @JsonProperty(JSON_PROPERTY_DNN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getDnn()
    {
        return dnn;
    }

    @JsonProperty(JSON_PROPERTY_DNN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDnn(String dnn)
    {
        this.dnn = dnn;
    }

    public MonitoringConfiguration singleNssai(Snssai singleNssai)
    {

        this.singleNssai = singleNssai;
        return this;
    }

    /**
     * Get singleNssai
     * 
     * @return singleNssai
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SINGLE_NSSAI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Snssai getSingleNssai()
    {
        return singleNssai;
    }

    @JsonProperty(JSON_PROPERTY_SINGLE_NSSAI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSingleNssai(Snssai singleNssai)
    {
        this.singleNssai = singleNssai;
    }

    public MonitoringConfiguration pduSessionStatusCfg(PduSessionStatusCfg pduSessionStatusCfg)
    {

        this.pduSessionStatusCfg = pduSessionStatusCfg;
        return this;
    }

    /**
     * Get pduSessionStatusCfg
     * 
     * @return pduSessionStatusCfg
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_PDU_SESSION_STATUS_CFG)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public PduSessionStatusCfg getPduSessionStatusCfg()
    {
        return pduSessionStatusCfg;
    }

    @JsonProperty(JSON_PROPERTY_PDU_SESSION_STATUS_CFG)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPduSessionStatusCfg(PduSessionStatusCfg pduSessionStatusCfg)
    {
        this.pduSessionStatusCfg = pduSessionStatusCfg;
    }

    public MonitoringConfiguration reachabilityForSmsCfg(String reachabilityForSmsCfg)
    {

        this.reachabilityForSmsCfg = reachabilityForSmsCfg;
        return this;
    }

    /**
     * Get reachabilityForSmsCfg
     * 
     * @return reachabilityForSmsCfg
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_REACHABILITY_FOR_SMS_CFG)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getReachabilityForSmsCfg()
    {
        return reachabilityForSmsCfg;
    }

    @JsonProperty(JSON_PROPERTY_REACHABILITY_FOR_SMS_CFG)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setReachabilityForSmsCfg(String reachabilityForSmsCfg)
    {
        this.reachabilityForSmsCfg = reachabilityForSmsCfg;
    }

    public MonitoringConfiguration mtcProviderInformation(String mtcProviderInformation)
    {

        this.mtcProviderInformation = mtcProviderInformation;
        return this;
    }

    /**
     * String uniquely identifying MTC provider information.
     * 
     * @return mtcProviderInformation
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String uniquely identifying MTC provider information.")
    @JsonProperty(JSON_PROPERTY_MTC_PROVIDER_INFORMATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getMtcProviderInformation()
    {
        return mtcProviderInformation;
    }

    @JsonProperty(JSON_PROPERTY_MTC_PROVIDER_INFORMATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMtcProviderInformation(String mtcProviderInformation)
    {
        this.mtcProviderInformation = mtcProviderInformation;
    }

    public MonitoringConfiguration afId(String afId)
    {

        this.afId = afId;
        return this;
    }

    /**
     * Get afId
     * 
     * @return afId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_AF_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getAfId()
    {
        return afId;
    }

    @JsonProperty(JSON_PROPERTY_AF_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAfId(String afId)
    {
        this.afId = afId;
    }

    public MonitoringConfiguration reachabilityForDataCfg(ReachabilityForDataConfiguration reachabilityForDataCfg)
    {

        this.reachabilityForDataCfg = reachabilityForDataCfg;
        return this;
    }

    /**
     * Get reachabilityForDataCfg
     * 
     * @return reachabilityForDataCfg
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_REACHABILITY_FOR_DATA_CFG)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public ReachabilityForDataConfiguration getReachabilityForDataCfg()
    {
        return reachabilityForDataCfg;
    }

    @JsonProperty(JSON_PROPERTY_REACHABILITY_FOR_DATA_CFG)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setReachabilityForDataCfg(ReachabilityForDataConfiguration reachabilityForDataCfg)
    {
        this.reachabilityForDataCfg = reachabilityForDataCfg;
    }

    public MonitoringConfiguration idleStatusInd(Boolean idleStatusInd)
    {

        this.idleStatusInd = idleStatusInd;
        return this;
    }

    /**
     * Get idleStatusInd
     * 
     * @return idleStatusInd
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_IDLE_STATUS_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getIdleStatusInd()
    {
        return idleStatusInd;
    }

    @JsonProperty(JSON_PROPERTY_IDLE_STATUS_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIdleStatusInd(Boolean idleStatusInd)
    {
        this.idleStatusInd = idleStatusInd;
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
        MonitoringConfiguration monitoringConfiguration = (MonitoringConfiguration) o;
        return Objects.equals(this.eventType, monitoringConfiguration.eventType) && Objects.equals(this.immediateFlag, monitoringConfiguration.immediateFlag)
               && Objects.equals(this.locationReportingConfiguration, monitoringConfiguration.locationReportingConfiguration)
               && Objects.equals(this.associationType, monitoringConfiguration.associationType)
               && Objects.equals(this.datalinkReportCfg, monitoringConfiguration.datalinkReportCfg)
               && Objects.equals(this.lossConnectivityCfg, monitoringConfiguration.lossConnectivityCfg)
               && Objects.equals(this.maximumLatency, monitoringConfiguration.maximumLatency)
               && Objects.equals(this.maximumResponseTime, monitoringConfiguration.maximumResponseTime)
               && Objects.equals(this.suggestedPacketNumDl, monitoringConfiguration.suggestedPacketNumDl)
               && Objects.equals(this.dnn, monitoringConfiguration.dnn) && Objects.equals(this.singleNssai, monitoringConfiguration.singleNssai)
               && Objects.equals(this.pduSessionStatusCfg, monitoringConfiguration.pduSessionStatusCfg)
               && Objects.equals(this.reachabilityForSmsCfg, monitoringConfiguration.reachabilityForSmsCfg)
               && Objects.equals(this.mtcProviderInformation, monitoringConfiguration.mtcProviderInformation)
               && Objects.equals(this.afId, monitoringConfiguration.afId)
               && Objects.equals(this.reachabilityForDataCfg, monitoringConfiguration.reachabilityForDataCfg)
               && Objects.equals(this.idleStatusInd, monitoringConfiguration.idleStatusInd);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(eventType,
                            immediateFlag,
                            locationReportingConfiguration,
                            associationType,
                            datalinkReportCfg,
                            lossConnectivityCfg,
                            maximumLatency,
                            maximumResponseTime,
                            suggestedPacketNumDl,
                            dnn,
                            singleNssai,
                            pduSessionStatusCfg,
                            reachabilityForSmsCfg,
                            mtcProviderInformation,
                            afId,
                            reachabilityForDataCfg,
                            idleStatusInd);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class MonitoringConfiguration {\n");
        sb.append("    eventType: ").append(toIndentedString(eventType)).append("\n");
        sb.append("    immediateFlag: ").append(toIndentedString(immediateFlag)).append("\n");
        sb.append("    locationReportingConfiguration: ").append(toIndentedString(locationReportingConfiguration)).append("\n");
        sb.append("    associationType: ").append(toIndentedString(associationType)).append("\n");
        sb.append("    datalinkReportCfg: ").append(toIndentedString(datalinkReportCfg)).append("\n");
        sb.append("    lossConnectivityCfg: ").append(toIndentedString(lossConnectivityCfg)).append("\n");
        sb.append("    maximumLatency: ").append(toIndentedString(maximumLatency)).append("\n");
        sb.append("    maximumResponseTime: ").append(toIndentedString(maximumResponseTime)).append("\n");
        sb.append("    suggestedPacketNumDl: ").append(toIndentedString(suggestedPacketNumDl)).append("\n");
        sb.append("    dnn: ").append(toIndentedString(dnn)).append("\n");
        sb.append("    singleNssai: ").append(toIndentedString(singleNssai)).append("\n");
        sb.append("    pduSessionStatusCfg: ").append(toIndentedString(pduSessionStatusCfg)).append("\n");
        sb.append("    reachabilityForSmsCfg: ").append(toIndentedString(reachabilityForSmsCfg)).append("\n");
        sb.append("    mtcProviderInformation: ").append(toIndentedString(mtcProviderInformation)).append("\n");
        sb.append("    afId: ").append(toIndentedString(afId)).append("\n");
        sb.append("    reachabilityForDataCfg: ").append(toIndentedString(reachabilityForDataCfg)).append("\n");
        sb.append("    idleStatusInd: ").append(toIndentedString(idleStatusInd)).append("\n");
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
