/*
 * Nchf_ConvergedCharging
 * ConvergedCharging Service    © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC). All rights reserved. 
 *
 * The version of the OpenAPI document: 3.1.12
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts32291.nchf.convergedcharging;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29512.npcf.smpolicycontrol.QosData;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.PresenceInfo;
import com.ericsson.cnal.openapi.r17.ts29512.npcf.smpolicycontrol.QosCharacteristics;
import com.ericsson.cnal.openapi.r17.ts29512.npcf.smpolicycontrol.SteeringMode;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.UserLocation;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.OffsetDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openapitools.jackson.nullable.JsonNullable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.openapitools.jackson.nullable.JsonNullable;
import java.util.NoSuchElementException;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * PDUContainerInformation
 */
@JsonPropertyOrder({ PDUContainerInformation.JSON_PROPERTY_TIMEOF_FIRST_USAGE,
                     PDUContainerInformation.JSON_PROPERTY_TIMEOF_LAST_USAGE,
                     PDUContainerInformation.JSON_PROPERTY_QO_S_INFORMATION,
                     PDUContainerInformation.JSON_PROPERTY_QO_S_CHARACTERISTICS,
                     PDUContainerInformation.JSON_PROPERTY_AF_CHARGING_IDENTIFIER,
                     PDUContainerInformation.JSON_PROPERTY_AF_CHARGING_ID_STRING,
                     PDUContainerInformation.JSON_PROPERTY_USER_LOCATION_INFORMATION,
                     PDUContainerInformation.JSON_PROPERTY_UETIME_ZONE,
                     PDUContainerInformation.JSON_PROPERTY_R_A_T_TYPE,
                     PDUContainerInformation.JSON_PROPERTY_SERVING_NODE_I_D,
                     PDUContainerInformation.JSON_PROPERTY_PRESENCE_REPORTING_AREA_INFORMATION,
                     PDUContainerInformation.JSON_PROPERTY_3GPP_P_S_DATA_OFF_STATUS,
                     PDUContainerInformation.JSON_PROPERTY_SPONSOR_IDENTITY,
                     PDUContainerInformation.JSON_PROPERTY_APPLICATIONSERVICE_PROVIDER_IDENTITY,
                     PDUContainerInformation.JSON_PROPERTY_CHARGING_RULE_BASE_NAME,
                     PDUContainerInformation.JSON_PROPERTY_M_A_P_D_U_STEERING_FUNCTIONALITY,
                     PDUContainerInformation.JSON_PROPERTY_M_A_P_D_U_STEERING_MODE,
                     PDUContainerInformation.JSON_PROPERTY_TRAFFIC_FORWARDING_WAY,
                     PDUContainerInformation.JSON_PROPERTY_QOS_MONITORING_REPORT })
public class PDUContainerInformation
{
    public static final String JSON_PROPERTY_TIMEOF_FIRST_USAGE = "timeofFirstUsage";
    private OffsetDateTime timeofFirstUsage;

    public static final String JSON_PROPERTY_TIMEOF_LAST_USAGE = "timeofLastUsage";
    private OffsetDateTime timeofLastUsage;

    public static final String JSON_PROPERTY_QO_S_INFORMATION = "qoSInformation";
    private JsonNullable<QosData> qoSInformation = JsonNullable.<QosData>undefined();

    public static final String JSON_PROPERTY_QO_S_CHARACTERISTICS = "qoSCharacteristics";
    private QosCharacteristics qoSCharacteristics;

    public static final String JSON_PROPERTY_AF_CHARGING_IDENTIFIER = "afChargingIdentifier";
    private Integer afChargingIdentifier;

    public static final String JSON_PROPERTY_AF_CHARGING_ID_STRING = "afChargingIdString";
    private String afChargingIdString;

    public static final String JSON_PROPERTY_USER_LOCATION_INFORMATION = "userLocationInformation";
    private UserLocation userLocationInformation;

    public static final String JSON_PROPERTY_UETIME_ZONE = "uetimeZone";
    private String uetimeZone;

    public static final String JSON_PROPERTY_R_A_T_TYPE = "rATType";
    private String rATType;

    public static final String JSON_PROPERTY_SERVING_NODE_I_D = "servingNodeID";
    private List<ServingNetworkFunctionID> servingNodeID = null;

    public static final String JSON_PROPERTY_PRESENCE_REPORTING_AREA_INFORMATION = "presenceReportingAreaInformation";
    private Map<String, PresenceInfo> presenceReportingAreaInformation = null;

    public static final String JSON_PROPERTY_3GPP_P_S_DATA_OFF_STATUS = "3gppPSDataOffStatus";
    private String _3gppPSDataOffStatus;

    public static final String JSON_PROPERTY_SPONSOR_IDENTITY = "sponsorIdentity";
    private String sponsorIdentity;

    public static final String JSON_PROPERTY_APPLICATIONSERVICE_PROVIDER_IDENTITY = "applicationserviceProviderIdentity";
    private String applicationserviceProviderIdentity;

    public static final String JSON_PROPERTY_CHARGING_RULE_BASE_NAME = "chargingRuleBaseName";
    private String chargingRuleBaseName;

    public static final String JSON_PROPERTY_M_A_P_D_U_STEERING_FUNCTIONALITY = "mAPDUSteeringFunctionality";
    private String mAPDUSteeringFunctionality;

    public static final String JSON_PROPERTY_M_A_P_D_U_STEERING_MODE = "mAPDUSteeringMode";
    private SteeringMode mAPDUSteeringMode;

    public static final String JSON_PROPERTY_TRAFFIC_FORWARDING_WAY = "trafficForwardingWay";
    private String trafficForwardingWay;

    public static final String JSON_PROPERTY_QOS_MONITORING_REPORT = "qosMonitoringReport";
    private List<QosMonitoringReport> qosMonitoringReport = null;

    public PDUContainerInformation()
    {
    }

    public PDUContainerInformation timeofFirstUsage(OffsetDateTime timeofFirstUsage)
    {

        this.timeofFirstUsage = timeofFirstUsage;
        return this;
    }

    /**
     * string with format &#39;date-time&#39; as defined in OpenAPI.
     * 
     * @return timeofFirstUsage
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'date-time' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_TIMEOF_FIRST_USAGE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getTimeofFirstUsage()
    {
        return timeofFirstUsage;
    }

    @JsonProperty(JSON_PROPERTY_TIMEOF_FIRST_USAGE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTimeofFirstUsage(OffsetDateTime timeofFirstUsage)
    {
        this.timeofFirstUsage = timeofFirstUsage;
    }

    public PDUContainerInformation timeofLastUsage(OffsetDateTime timeofLastUsage)
    {

        this.timeofLastUsage = timeofLastUsage;
        return this;
    }

    /**
     * string with format &#39;date-time&#39; as defined in OpenAPI.
     * 
     * @return timeofLastUsage
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'date-time' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_TIMEOF_LAST_USAGE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getTimeofLastUsage()
    {
        return timeofLastUsage;
    }

    @JsonProperty(JSON_PROPERTY_TIMEOF_LAST_USAGE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTimeofLastUsage(OffsetDateTime timeofLastUsage)
    {
        this.timeofLastUsage = timeofLastUsage;
    }

    public PDUContainerInformation qoSInformation(QosData qoSInformation)
    {
        this.qoSInformation = JsonNullable.<QosData>of(qoSInformation);

        return this;
    }

    /**
     * Get qoSInformation
     * 
     * @return qoSInformation
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonIgnore

    public QosData getQoSInformation()
    {
        return qoSInformation.orElse(null);
    }

    @JsonProperty(JSON_PROPERTY_QO_S_INFORMATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public JsonNullable<QosData> getQoSInformation_JsonNullable()
    {
        return qoSInformation;
    }

    @JsonProperty(JSON_PROPERTY_QO_S_INFORMATION)
    public void setQoSInformation_JsonNullable(JsonNullable<QosData> qoSInformation)
    {
        this.qoSInformation = qoSInformation;
    }

    public void setQoSInformation(QosData qoSInformation)
    {
        this.qoSInformation = JsonNullable.<QosData>of(qoSInformation);
    }

    public PDUContainerInformation qoSCharacteristics(QosCharacteristics qoSCharacteristics)
    {

        this.qoSCharacteristics = qoSCharacteristics;
        return this;
    }

    /**
     * Get qoSCharacteristics
     * 
     * @return qoSCharacteristics
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_QO_S_CHARACTERISTICS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public QosCharacteristics getQoSCharacteristics()
    {
        return qoSCharacteristics;
    }

    @JsonProperty(JSON_PROPERTY_QO_S_CHARACTERISTICS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setQoSCharacteristics(QosCharacteristics qoSCharacteristics)
    {
        this.qoSCharacteristics = qoSCharacteristics;
    }

    public PDUContainerInformation afChargingIdentifier(Integer afChargingIdentifier)
    {

        this.afChargingIdentifier = afChargingIdentifier;
        return this;
    }

    /**
     * Integer where the allowed values correspond to the value range of an unsigned
     * 32-bit integer. minimum: 0 maximum: 4294967295
     * 
     * @return afChargingIdentifier
     * @deprecated
     **/
    @Deprecated
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Integer where the allowed values correspond to the value range of an unsigned 32-bit integer. ")
    @JsonProperty(JSON_PROPERTY_AF_CHARGING_IDENTIFIER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getAfChargingIdentifier()
    {
        return afChargingIdentifier;
    }

    @JsonProperty(JSON_PROPERTY_AF_CHARGING_IDENTIFIER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAfChargingIdentifier(Integer afChargingIdentifier)
    {
        this.afChargingIdentifier = afChargingIdentifier;
    }

    public PDUContainerInformation afChargingIdString(String afChargingIdString)
    {

        this.afChargingIdString = afChargingIdString;
        return this;
    }

    /**
     * Application provided charging identifier allowing correlation of charging
     * information.
     * 
     * @return afChargingIdString
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Application provided charging identifier allowing correlation of charging information.")
    @JsonProperty(JSON_PROPERTY_AF_CHARGING_ID_STRING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getAfChargingIdString()
    {
        return afChargingIdString;
    }

    @JsonProperty(JSON_PROPERTY_AF_CHARGING_ID_STRING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAfChargingIdString(String afChargingIdString)
    {
        this.afChargingIdString = afChargingIdString;
    }

    public PDUContainerInformation userLocationInformation(UserLocation userLocationInformation)
    {

        this.userLocationInformation = userLocationInformation;
        return this;
    }

    /**
     * Get userLocationInformation
     * 
     * @return userLocationInformation
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_USER_LOCATION_INFORMATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UserLocation getUserLocationInformation()
    {
        return userLocationInformation;
    }

    @JsonProperty(JSON_PROPERTY_USER_LOCATION_INFORMATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUserLocationInformation(UserLocation userLocationInformation)
    {
        this.userLocationInformation = userLocationInformation;
    }

    public PDUContainerInformation uetimeZone(String uetimeZone)
    {

        this.uetimeZone = uetimeZone;
        return this;
    }

    /**
     * String with format \&quot;time-numoffset\&quot; optionally appended by
     * \&quot;daylightSavingTime\&quot;, where - \&quot;time-numoffset\&quot; shall
     * represent the time zone adjusted for daylight saving time and be encoded as
     * time-numoffset as defined in clause 5.6 of IETF RFC 3339; -
     * \&quot;daylightSavingTime\&quot; shall represent the adjustment that has been
     * made and shall be encoded as \&quot;+1\&quot; or \&quot;+2\&quot; for a +1 or
     * +2 hours adjustment. The example is for 8 hours behind UTC, +1 hour
     * adjustment for Daylight Saving Time.
     * 
     * @return uetimeZone
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(example = "-08:00+1",
                      value = "String with format \"time-numoffset\" optionally appended by \"daylightSavingTime\", where  - \"time-numoffset\" shall represent the time zone adjusted for daylight saving time and be    encoded as time-numoffset as defined in clause 5.6 of IETF RFC 3339;  - \"daylightSavingTime\" shall represent the adjustment that has been made and shall be    encoded as \"+1\" or \"+2\" for a +1 or +2 hours adjustment.  The example is for 8 hours behind UTC, +1 hour adjustment for Daylight Saving Time. ")
    @JsonProperty(JSON_PROPERTY_UETIME_ZONE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getUetimeZone()
    {
        return uetimeZone;
    }

    @JsonProperty(JSON_PROPERTY_UETIME_ZONE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUetimeZone(String uetimeZone)
    {
        this.uetimeZone = uetimeZone;
    }

    public PDUContainerInformation rATType(String rATType)
    {

        this.rATType = rATType;
        return this;
    }

    /**
     * Indicates the radio access used.
     * 
     * @return rATType
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Indicates the radio access used.")
    @JsonProperty(JSON_PROPERTY_R_A_T_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getrATType()
    {
        return rATType;
    }

    @JsonProperty(JSON_PROPERTY_R_A_T_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setrATType(String rATType)
    {
        this.rATType = rATType;
    }

    public PDUContainerInformation servingNodeID(List<ServingNetworkFunctionID> servingNodeID)
    {

        this.servingNodeID = servingNodeID;
        return this;
    }

    public PDUContainerInformation addServingNodeIDItem(ServingNetworkFunctionID servingNodeIDItem)
    {
        if (this.servingNodeID == null)
        {
            this.servingNodeID = new ArrayList<>();
        }
        this.servingNodeID.add(servingNodeIDItem);
        return this;
    }

    /**
     * Get servingNodeID
     * 
     * @return servingNodeID
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SERVING_NODE_I_D)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<ServingNetworkFunctionID> getServingNodeID()
    {
        return servingNodeID;
    }

    @JsonProperty(JSON_PROPERTY_SERVING_NODE_I_D)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setServingNodeID(List<ServingNetworkFunctionID> servingNodeID)
    {
        this.servingNodeID = servingNodeID;
    }

    public PDUContainerInformation presenceReportingAreaInformation(Map<String, PresenceInfo> presenceReportingAreaInformation)
    {

        this.presenceReportingAreaInformation = presenceReportingAreaInformation;
        return this;
    }

    public PDUContainerInformation putPresenceReportingAreaInformationItem(String key,
                                                                           PresenceInfo presenceReportingAreaInformationItem)
    {
        if (this.presenceReportingAreaInformation == null)
        {
            this.presenceReportingAreaInformation = new HashMap<>();
        }
        this.presenceReportingAreaInformation.put(key, presenceReportingAreaInformationItem);
        return this;
    }

    /**
     * Get presenceReportingAreaInformation
     * 
     * @return presenceReportingAreaInformation
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_PRESENCE_REPORTING_AREA_INFORMATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Map<String, PresenceInfo> getPresenceReportingAreaInformation()
    {
        return presenceReportingAreaInformation;
    }

    @JsonProperty(JSON_PROPERTY_PRESENCE_REPORTING_AREA_INFORMATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPresenceReportingAreaInformation(Map<String, PresenceInfo> presenceReportingAreaInformation)
    {
        this.presenceReportingAreaInformation = presenceReportingAreaInformation;
    }

    public PDUContainerInformation _3gppPSDataOffStatus(String _3gppPSDataOffStatus)
    {

        this._3gppPSDataOffStatus = _3gppPSDataOffStatus;
        return this;
    }

    /**
     * Get _3gppPSDataOffStatus
     * 
     * @return _3gppPSDataOffStatus
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_3GPP_P_S_DATA_OFF_STATUS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String get3gppPSDataOffStatus()
    {
        return _3gppPSDataOffStatus;
    }

    @JsonProperty(JSON_PROPERTY_3GPP_P_S_DATA_OFF_STATUS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void set3gppPSDataOffStatus(String _3gppPSDataOffStatus)
    {
        this._3gppPSDataOffStatus = _3gppPSDataOffStatus;
    }

    public PDUContainerInformation sponsorIdentity(String sponsorIdentity)
    {

        this.sponsorIdentity = sponsorIdentity;
        return this;
    }

    /**
     * Get sponsorIdentity
     * 
     * @return sponsorIdentity
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SPONSOR_IDENTITY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getSponsorIdentity()
    {
        return sponsorIdentity;
    }

    @JsonProperty(JSON_PROPERTY_SPONSOR_IDENTITY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSponsorIdentity(String sponsorIdentity)
    {
        this.sponsorIdentity = sponsorIdentity;
    }

    public PDUContainerInformation applicationserviceProviderIdentity(String applicationserviceProviderIdentity)
    {

        this.applicationserviceProviderIdentity = applicationserviceProviderIdentity;
        return this;
    }

    /**
     * Get applicationserviceProviderIdentity
     * 
     * @return applicationserviceProviderIdentity
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_APPLICATIONSERVICE_PROVIDER_IDENTITY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getApplicationserviceProviderIdentity()
    {
        return applicationserviceProviderIdentity;
    }

    @JsonProperty(JSON_PROPERTY_APPLICATIONSERVICE_PROVIDER_IDENTITY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setApplicationserviceProviderIdentity(String applicationserviceProviderIdentity)
    {
        this.applicationserviceProviderIdentity = applicationserviceProviderIdentity;
    }

    public PDUContainerInformation chargingRuleBaseName(String chargingRuleBaseName)
    {

        this.chargingRuleBaseName = chargingRuleBaseName;
        return this;
    }

    /**
     * Get chargingRuleBaseName
     * 
     * @return chargingRuleBaseName
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_CHARGING_RULE_BASE_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getChargingRuleBaseName()
    {
        return chargingRuleBaseName;
    }

    @JsonProperty(JSON_PROPERTY_CHARGING_RULE_BASE_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setChargingRuleBaseName(String chargingRuleBaseName)
    {
        this.chargingRuleBaseName = chargingRuleBaseName;
    }

    public PDUContainerInformation mAPDUSteeringFunctionality(String mAPDUSteeringFunctionality)
    {

        this.mAPDUSteeringFunctionality = mAPDUSteeringFunctionality;
        return this;
    }

    /**
     * Possible values are - MPTCP: Indicates that PCF authorizes the MPTCP
     * functionality to support traffic steering, switching and splitting. -
     * ATSSS_LL: Indicates that PCF authorizes the ATSSS-LL functionality to support
     * traffic steering, switching and splitting.
     * 
     * @return mAPDUSteeringFunctionality
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Possible values are   - MPTCP: Indicates that PCF authorizes the MPTCP functionality to support traffic steering, switching and splitting.   - ATSSS_LL: Indicates that PCF authorizes the ATSSS-LL functionality to support traffic steering, switching and splitting. ")
    @JsonProperty(JSON_PROPERTY_M_A_P_D_U_STEERING_FUNCTIONALITY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getmAPDUSteeringFunctionality()
    {
        return mAPDUSteeringFunctionality;
    }

    @JsonProperty(JSON_PROPERTY_M_A_P_D_U_STEERING_FUNCTIONALITY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setmAPDUSteeringFunctionality(String mAPDUSteeringFunctionality)
    {
        this.mAPDUSteeringFunctionality = mAPDUSteeringFunctionality;
    }

    public PDUContainerInformation mAPDUSteeringMode(SteeringMode mAPDUSteeringMode)
    {

        this.mAPDUSteeringMode = mAPDUSteeringMode;
        return this;
    }

    /**
     * Get mAPDUSteeringMode
     * 
     * @return mAPDUSteeringMode
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_M_A_P_D_U_STEERING_MODE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public SteeringMode getmAPDUSteeringMode()
    {
        return mAPDUSteeringMode;
    }

    @JsonProperty(JSON_PROPERTY_M_A_P_D_U_STEERING_MODE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setmAPDUSteeringMode(SteeringMode mAPDUSteeringMode)
    {
        this.mAPDUSteeringMode = mAPDUSteeringMode;
    }

    public PDUContainerInformation trafficForwardingWay(String trafficForwardingWay)
    {

        this.trafficForwardingWay = trafficForwardingWay;
        return this;
    }

    /**
     * Get trafficForwardingWay
     * 
     * @return trafficForwardingWay
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_TRAFFIC_FORWARDING_WAY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getTrafficForwardingWay()
    {
        return trafficForwardingWay;
    }

    @JsonProperty(JSON_PROPERTY_TRAFFIC_FORWARDING_WAY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTrafficForwardingWay(String trafficForwardingWay)
    {
        this.trafficForwardingWay = trafficForwardingWay;
    }

    public PDUContainerInformation qosMonitoringReport(List<QosMonitoringReport> qosMonitoringReport)
    {

        this.qosMonitoringReport = qosMonitoringReport;
        return this;
    }

    public PDUContainerInformation addQosMonitoringReportItem(QosMonitoringReport qosMonitoringReportItem)
    {
        if (this.qosMonitoringReport == null)
        {
            this.qosMonitoringReport = new ArrayList<>();
        }
        this.qosMonitoringReport.add(qosMonitoringReportItem);
        return this;
    }

    /**
     * Get qosMonitoringReport
     * 
     * @return qosMonitoringReport
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_QOS_MONITORING_REPORT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<QosMonitoringReport> getQosMonitoringReport()
    {
        return qosMonitoringReport;
    }

    @JsonProperty(JSON_PROPERTY_QOS_MONITORING_REPORT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setQosMonitoringReport(List<QosMonitoringReport> qosMonitoringReport)
    {
        this.qosMonitoringReport = qosMonitoringReport;
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
        PDUContainerInformation pdUContainerInformation = (PDUContainerInformation) o;
        return Objects.equals(this.timeofFirstUsage, pdUContainerInformation.timeofFirstUsage)
               && Objects.equals(this.timeofLastUsage, pdUContainerInformation.timeofLastUsage)
               && equalsNullable(this.qoSInformation, pdUContainerInformation.qoSInformation)
               && Objects.equals(this.qoSCharacteristics, pdUContainerInformation.qoSCharacteristics)
               && Objects.equals(this.afChargingIdentifier, pdUContainerInformation.afChargingIdentifier)
               && Objects.equals(this.afChargingIdString, pdUContainerInformation.afChargingIdString)
               && Objects.equals(this.userLocationInformation, pdUContainerInformation.userLocationInformation)
               && Objects.equals(this.uetimeZone, pdUContainerInformation.uetimeZone) && Objects.equals(this.rATType, pdUContainerInformation.rATType)
               && Objects.equals(this.servingNodeID, pdUContainerInformation.servingNodeID)
               && Objects.equals(this.presenceReportingAreaInformation, pdUContainerInformation.presenceReportingAreaInformation)
               && Objects.equals(this._3gppPSDataOffStatus, pdUContainerInformation._3gppPSDataOffStatus)
               && Objects.equals(this.sponsorIdentity, pdUContainerInformation.sponsorIdentity)
               && Objects.equals(this.applicationserviceProviderIdentity, pdUContainerInformation.applicationserviceProviderIdentity)
               && Objects.equals(this.chargingRuleBaseName, pdUContainerInformation.chargingRuleBaseName)
               && Objects.equals(this.mAPDUSteeringFunctionality, pdUContainerInformation.mAPDUSteeringFunctionality)
               && Objects.equals(this.mAPDUSteeringMode, pdUContainerInformation.mAPDUSteeringMode)
               && Objects.equals(this.trafficForwardingWay, pdUContainerInformation.trafficForwardingWay)
               && Objects.equals(this.qosMonitoringReport, pdUContainerInformation.qosMonitoringReport);
    }

    private static <T> boolean equalsNullable(JsonNullable<T> a,
                                              JsonNullable<T> b)
    {
        return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(timeofFirstUsage,
                            timeofLastUsage,
                            hashCodeNullable(qoSInformation),
                            qoSCharacteristics,
                            afChargingIdentifier,
                            afChargingIdString,
                            userLocationInformation,
                            uetimeZone,
                            rATType,
                            servingNodeID,
                            presenceReportingAreaInformation,
                            _3gppPSDataOffStatus,
                            sponsorIdentity,
                            applicationserviceProviderIdentity,
                            chargingRuleBaseName,
                            mAPDUSteeringFunctionality,
                            mAPDUSteeringMode,
                            trafficForwardingWay,
                            qosMonitoringReport);
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
        sb.append("class PDUContainerInformation {\n");
        sb.append("    timeofFirstUsage: ").append(toIndentedString(timeofFirstUsage)).append("\n");
        sb.append("    timeofLastUsage: ").append(toIndentedString(timeofLastUsage)).append("\n");
        sb.append("    qoSInformation: ").append(toIndentedString(qoSInformation)).append("\n");
        sb.append("    qoSCharacteristics: ").append(toIndentedString(qoSCharacteristics)).append("\n");
        sb.append("    afChargingIdentifier: ").append(toIndentedString(afChargingIdentifier)).append("\n");
        sb.append("    afChargingIdString: ").append(toIndentedString(afChargingIdString)).append("\n");
        sb.append("    userLocationInformation: ").append(toIndentedString(userLocationInformation)).append("\n");
        sb.append("    uetimeZone: ").append(toIndentedString(uetimeZone)).append("\n");
        sb.append("    rATType: ").append(toIndentedString(rATType)).append("\n");
        sb.append("    servingNodeID: ").append(toIndentedString(servingNodeID)).append("\n");
        sb.append("    presenceReportingAreaInformation: ").append(toIndentedString(presenceReportingAreaInformation)).append("\n");
        sb.append("    _3gppPSDataOffStatus: ").append(toIndentedString(_3gppPSDataOffStatus)).append("\n");
        sb.append("    sponsorIdentity: ").append(toIndentedString(sponsorIdentity)).append("\n");
        sb.append("    applicationserviceProviderIdentity: ").append(toIndentedString(applicationserviceProviderIdentity)).append("\n");
        sb.append("    chargingRuleBaseName: ").append(toIndentedString(chargingRuleBaseName)).append("\n");
        sb.append("    mAPDUSteeringFunctionality: ").append(toIndentedString(mAPDUSteeringFunctionality)).append("\n");
        sb.append("    mAPDUSteeringMode: ").append(toIndentedString(mAPDUSteeringMode)).append("\n");
        sb.append("    trafficForwardingWay: ").append(toIndentedString(trafficForwardingWay)).append("\n");
        sb.append("    qosMonitoringReport: ").append(toIndentedString(qosMonitoringReport)).append("\n");
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
