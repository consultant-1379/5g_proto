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
 * QFIContainerInformation
 */
@JsonPropertyOrder({ QFIContainerInformation.JSON_PROPERTY_Q_F_I,
                     QFIContainerInformation.JSON_PROPERTY_REPORT_TIME,
                     QFIContainerInformation.JSON_PROPERTY_TIMEOF_FIRST_USAGE,
                     QFIContainerInformation.JSON_PROPERTY_TIMEOF_LAST_USAGE,
                     QFIContainerInformation.JSON_PROPERTY_QO_S_INFORMATION,
                     QFIContainerInformation.JSON_PROPERTY_QO_S_CHARACTERISTICS,
                     QFIContainerInformation.JSON_PROPERTY_USER_LOCATION_INFORMATION,
                     QFIContainerInformation.JSON_PROPERTY_UETIME_ZONE,
                     QFIContainerInformation.JSON_PROPERTY_PRESENCE_REPORTING_AREA_INFORMATION,
                     QFIContainerInformation.JSON_PROPERTY_R_A_T_TYPE,
                     QFIContainerInformation.JSON_PROPERTY_SERVING_NETWORK_FUNCTION_I_D,
                     QFIContainerInformation.JSON_PROPERTY_3GPP_P_S_DATA_OFF_STATUS,
                     QFIContainerInformation.JSON_PROPERTY_3GPP_CHARGING_ID,
                     QFIContainerInformation.JSON_PROPERTY_DIAGNOSTICS,
                     QFIContainerInformation.JSON_PROPERTY_ENHANCED_DIAGNOSTICS })
public class QFIContainerInformation
{
    public static final String JSON_PROPERTY_Q_F_I = "qFI";
    private Integer qFI;

    public static final String JSON_PROPERTY_REPORT_TIME = "reportTime";
    private OffsetDateTime reportTime;

    public static final String JSON_PROPERTY_TIMEOF_FIRST_USAGE = "timeofFirstUsage";
    private OffsetDateTime timeofFirstUsage;

    public static final String JSON_PROPERTY_TIMEOF_LAST_USAGE = "timeofLastUsage";
    private OffsetDateTime timeofLastUsage;

    public static final String JSON_PROPERTY_QO_S_INFORMATION = "qoSInformation";
    private JsonNullable<QosData> qoSInformation = JsonNullable.<QosData>undefined();

    public static final String JSON_PROPERTY_QO_S_CHARACTERISTICS = "qoSCharacteristics";
    private QosCharacteristics qoSCharacteristics;

    public static final String JSON_PROPERTY_USER_LOCATION_INFORMATION = "userLocationInformation";
    private UserLocation userLocationInformation;

    public static final String JSON_PROPERTY_UETIME_ZONE = "uetimeZone";
    private String uetimeZone;

    public static final String JSON_PROPERTY_PRESENCE_REPORTING_AREA_INFORMATION = "presenceReportingAreaInformation";
    private Map<String, PresenceInfo> presenceReportingAreaInformation = null;

    public static final String JSON_PROPERTY_R_A_T_TYPE = "rATType";
    private String rATType;

    public static final String JSON_PROPERTY_SERVING_NETWORK_FUNCTION_I_D = "servingNetworkFunctionID";
    private List<ServingNetworkFunctionID> servingNetworkFunctionID = null;

    public static final String JSON_PROPERTY_3GPP_P_S_DATA_OFF_STATUS = "3gppPSDataOffStatus";
    private String _3gppPSDataOffStatus;

    public static final String JSON_PROPERTY_3GPP_CHARGING_ID = "3gppChargingId";
    private Integer _3gppChargingId;

    public static final String JSON_PROPERTY_DIAGNOSTICS = "diagnostics";
    private Integer diagnostics;

    public static final String JSON_PROPERTY_ENHANCED_DIAGNOSTICS = "enhancedDiagnostics";
    private List<String> enhancedDiagnostics = null;

    public QFIContainerInformation()
    {
    }

    public QFIContainerInformation qFI(Integer qFI)
    {

        this.qFI = qFI;
        return this;
    }

    /**
     * Unsigned integer identifying a QoS flow, within the range 0 to 63. minimum: 0
     * maximum: 63
     * 
     * @return qFI
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Unsigned integer identifying a QoS flow, within the range 0 to 63.")
    @JsonProperty(JSON_PROPERTY_Q_F_I)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getqFI()
    {
        return qFI;
    }

    @JsonProperty(JSON_PROPERTY_Q_F_I)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setqFI(Integer qFI)
    {
        this.qFI = qFI;
    }

    public QFIContainerInformation reportTime(OffsetDateTime reportTime)
    {

        this.reportTime = reportTime;
        return this;
    }

    /**
     * string with format &#39;date-time&#39; as defined in OpenAPI.
     * 
     * @return reportTime
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "string with format 'date-time' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_REPORT_TIME)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getReportTime()
    {
        return reportTime;
    }

    @JsonProperty(JSON_PROPERTY_REPORT_TIME)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setReportTime(OffsetDateTime reportTime)
    {
        this.reportTime = reportTime;
    }

    public QFIContainerInformation timeofFirstUsage(OffsetDateTime timeofFirstUsage)
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

    public QFIContainerInformation timeofLastUsage(OffsetDateTime timeofLastUsage)
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

    public QFIContainerInformation qoSInformation(QosData qoSInformation)
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

    public QFIContainerInformation qoSCharacteristics(QosCharacteristics qoSCharacteristics)
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

    public QFIContainerInformation userLocationInformation(UserLocation userLocationInformation)
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

    public QFIContainerInformation uetimeZone(String uetimeZone)
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

    public QFIContainerInformation presenceReportingAreaInformation(Map<String, PresenceInfo> presenceReportingAreaInformation)
    {

        this.presenceReportingAreaInformation = presenceReportingAreaInformation;
        return this;
    }

    public QFIContainerInformation putPresenceReportingAreaInformationItem(String key,
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

    public QFIContainerInformation rATType(String rATType)
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

    public QFIContainerInformation servingNetworkFunctionID(List<ServingNetworkFunctionID> servingNetworkFunctionID)
    {

        this.servingNetworkFunctionID = servingNetworkFunctionID;
        return this;
    }

    public QFIContainerInformation addServingNetworkFunctionIDItem(ServingNetworkFunctionID servingNetworkFunctionIDItem)
    {
        if (this.servingNetworkFunctionID == null)
        {
            this.servingNetworkFunctionID = new ArrayList<>();
        }
        this.servingNetworkFunctionID.add(servingNetworkFunctionIDItem);
        return this;
    }

    /**
     * Get servingNetworkFunctionID
     * 
     * @return servingNetworkFunctionID
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SERVING_NETWORK_FUNCTION_I_D)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<ServingNetworkFunctionID> getServingNetworkFunctionID()
    {
        return servingNetworkFunctionID;
    }

    @JsonProperty(JSON_PROPERTY_SERVING_NETWORK_FUNCTION_I_D)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setServingNetworkFunctionID(List<ServingNetworkFunctionID> servingNetworkFunctionID)
    {
        this.servingNetworkFunctionID = servingNetworkFunctionID;
    }

    public QFIContainerInformation _3gppPSDataOffStatus(String _3gppPSDataOffStatus)
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

    public QFIContainerInformation _3gppChargingId(Integer _3gppChargingId)
    {

        this._3gppChargingId = _3gppChargingId;
        return this;
    }

    /**
     * Integer where the allowed values correspond to the value range of an unsigned
     * 32-bit integer. minimum: 0 maximum: 4294967295
     * 
     * @return _3gppChargingId
     * @deprecated
     **/
    @Deprecated
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Integer where the allowed values correspond to the value range of an unsigned 32-bit integer. ")
    @JsonProperty(JSON_PROPERTY_3GPP_CHARGING_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer get3gppChargingId()
    {
        return _3gppChargingId;
    }

    @JsonProperty(JSON_PROPERTY_3GPP_CHARGING_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void set3gppChargingId(Integer _3gppChargingId)
    {
        this._3gppChargingId = _3gppChargingId;
    }

    public QFIContainerInformation diagnostics(Integer diagnostics)
    {

        this.diagnostics = diagnostics;
        return this;
    }

    /**
     * Get diagnostics
     * 
     * @return diagnostics
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_DIAGNOSTICS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getDiagnostics()
    {
        return diagnostics;
    }

    @JsonProperty(JSON_PROPERTY_DIAGNOSTICS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDiagnostics(Integer diagnostics)
    {
        this.diagnostics = diagnostics;
    }

    public QFIContainerInformation enhancedDiagnostics(List<String> enhancedDiagnostics)
    {

        this.enhancedDiagnostics = enhancedDiagnostics;
        return this;
    }

    public QFIContainerInformation addEnhancedDiagnosticsItem(String enhancedDiagnosticsItem)
    {
        if (this.enhancedDiagnostics == null)
        {
            this.enhancedDiagnostics = new ArrayList<>();
        }
        this.enhancedDiagnostics.add(enhancedDiagnosticsItem);
        return this;
    }

    /**
     * Get enhancedDiagnostics
     * 
     * @return enhancedDiagnostics
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_ENHANCED_DIAGNOSTICS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getEnhancedDiagnostics()
    {
        return enhancedDiagnostics;
    }

    @JsonProperty(JSON_PROPERTY_ENHANCED_DIAGNOSTICS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnhancedDiagnostics(List<String> enhancedDiagnostics)
    {
        this.enhancedDiagnostics = enhancedDiagnostics;
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
        QFIContainerInformation qfIContainerInformation = (QFIContainerInformation) o;
        return Objects.equals(this.qFI, qfIContainerInformation.qFI) && Objects.equals(this.reportTime, qfIContainerInformation.reportTime)
               && Objects.equals(this.timeofFirstUsage, qfIContainerInformation.timeofFirstUsage)
               && Objects.equals(this.timeofLastUsage, qfIContainerInformation.timeofLastUsage)
               && equalsNullable(this.qoSInformation, qfIContainerInformation.qoSInformation)
               && Objects.equals(this.qoSCharacteristics, qfIContainerInformation.qoSCharacteristics)
               && Objects.equals(this.userLocationInformation, qfIContainerInformation.userLocationInformation)
               && Objects.equals(this.uetimeZone, qfIContainerInformation.uetimeZone)
               && Objects.equals(this.presenceReportingAreaInformation, qfIContainerInformation.presenceReportingAreaInformation)
               && Objects.equals(this.rATType, qfIContainerInformation.rATType)
               && Objects.equals(this.servingNetworkFunctionID, qfIContainerInformation.servingNetworkFunctionID)
               && Objects.equals(this._3gppPSDataOffStatus, qfIContainerInformation._3gppPSDataOffStatus)
               && Objects.equals(this._3gppChargingId, qfIContainerInformation._3gppChargingId)
               && Objects.equals(this.diagnostics, qfIContainerInformation.diagnostics)
               && Objects.equals(this.enhancedDiagnostics, qfIContainerInformation.enhancedDiagnostics);
    }

    private static <T> boolean equalsNullable(JsonNullable<T> a,
                                              JsonNullable<T> b)
    {
        return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(qFI,
                            reportTime,
                            timeofFirstUsage,
                            timeofLastUsage,
                            hashCodeNullable(qoSInformation),
                            qoSCharacteristics,
                            userLocationInformation,
                            uetimeZone,
                            presenceReportingAreaInformation,
                            rATType,
                            servingNetworkFunctionID,
                            _3gppPSDataOffStatus,
                            _3gppChargingId,
                            diagnostics,
                            enhancedDiagnostics);
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
        sb.append("class QFIContainerInformation {\n");
        sb.append("    qFI: ").append(toIndentedString(qFI)).append("\n");
        sb.append("    reportTime: ").append(toIndentedString(reportTime)).append("\n");
        sb.append("    timeofFirstUsage: ").append(toIndentedString(timeofFirstUsage)).append("\n");
        sb.append("    timeofLastUsage: ").append(toIndentedString(timeofLastUsage)).append("\n");
        sb.append("    qoSInformation: ").append(toIndentedString(qoSInformation)).append("\n");
        sb.append("    qoSCharacteristics: ").append(toIndentedString(qoSCharacteristics)).append("\n");
        sb.append("    userLocationInformation: ").append(toIndentedString(userLocationInformation)).append("\n");
        sb.append("    uetimeZone: ").append(toIndentedString(uetimeZone)).append("\n");
        sb.append("    presenceReportingAreaInformation: ").append(toIndentedString(presenceReportingAreaInformation)).append("\n");
        sb.append("    rATType: ").append(toIndentedString(rATType)).append("\n");
        sb.append("    servingNetworkFunctionID: ").append(toIndentedString(servingNetworkFunctionID)).append("\n");
        sb.append("    _3gppPSDataOffStatus: ").append(toIndentedString(_3gppPSDataOffStatus)).append("\n");
        sb.append("    _3gppChargingId: ").append(toIndentedString(_3gppChargingId)).append("\n");
        sb.append("    diagnostics: ").append(toIndentedString(diagnostics)).append("\n");
        sb.append("    enhancedDiagnostics: ").append(toIndentedString(enhancedDiagnostics)).append("\n");
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
