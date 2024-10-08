/*
 * Nchf_OfflineOnlyCharging
 * OfflineOnlyCharging Service © 20212022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC). All rights reserved. 
 *
 * The version of the OpenAPI document: 1.0.12
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts32291.nchf.offlineonlycharging;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.PresenceInfo;
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
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * PDUSessionChargingInformation
 */
@JsonPropertyOrder({ PDUSessionChargingInformation.JSON_PROPERTY_CHARGING_ID,
                     PDUSessionChargingInformation.JSON_PROPERTY_S_M_F_CHARGING_ID,
                     PDUSessionChargingInformation.JSON_PROPERTY_USER_INFORMATION,
                     PDUSessionChargingInformation.JSON_PROPERTY_USER_LOCATIONINFO,
                     PDUSessionChargingInformation.JSON_PROPERTY_M_A_P_D_U_NON3_G_P_P_USER_LOCATION_INFO,
                     PDUSessionChargingInformation.JSON_PROPERTY_USER_LOCATION_TIME,
                     PDUSessionChargingInformation.JSON_PROPERTY_PRESENCE_REPORTING_AREA_INFORMATION,
                     PDUSessionChargingInformation.JSON_PROPERTY_UETIME_ZONE,
                     PDUSessionChargingInformation.JSON_PROPERTY_PDU_SESSION_INFORMATION,
                     PDUSessionChargingInformation.JSON_PROPERTY_UNIT_COUNT_INACTIVITY_TIMER,
                     PDUSessionChargingInformation.JSON_PROPERTY_R_A_N_SECONDARY_R_A_T_USAGE_REPORT })
public class PDUSessionChargingInformation
{
    public static final String JSON_PROPERTY_CHARGING_ID = "chargingId";
    private Integer chargingId;

    public static final String JSON_PROPERTY_S_M_F_CHARGING_ID = "sMFChargingId";
    private String sMFChargingId;

    public static final String JSON_PROPERTY_USER_INFORMATION = "userInformation";
    private UserInformation userInformation;

    public static final String JSON_PROPERTY_USER_LOCATIONINFO = "userLocationinfo";
    private UserLocation userLocationinfo;

    public static final String JSON_PROPERTY_M_A_P_D_U_NON3_G_P_P_USER_LOCATION_INFO = "mAPDUNon3GPPUserLocationInfo";
    private UserLocation mAPDUNon3GPPUserLocationInfo;

    public static final String JSON_PROPERTY_USER_LOCATION_TIME = "userLocationTime";
    private OffsetDateTime userLocationTime;

    public static final String JSON_PROPERTY_PRESENCE_REPORTING_AREA_INFORMATION = "presenceReportingAreaInformation";
    private Map<String, PresenceInfo> presenceReportingAreaInformation = null;

    public static final String JSON_PROPERTY_UETIME_ZONE = "uetimeZone";
    private String uetimeZone;

    public static final String JSON_PROPERTY_PDU_SESSION_INFORMATION = "pduSessionInformation";
    private PDUSessionInformation pduSessionInformation;

    public static final String JSON_PROPERTY_UNIT_COUNT_INACTIVITY_TIMER = "unitCountInactivityTimer";
    private Integer unitCountInactivityTimer;

    public static final String JSON_PROPERTY_R_A_N_SECONDARY_R_A_T_USAGE_REPORT = "rANSecondaryRATUsageReport";
    private RANSecondaryRATUsageReport rANSecondaryRATUsageReport;

    public PDUSessionChargingInformation()
    {
    }

    public PDUSessionChargingInformation chargingId(Integer chargingId)
    {

        this.chargingId = chargingId;
        return this;
    }

    /**
     * Integer where the allowed values correspond to the value range of an unsigned
     * 32-bit integer. minimum: 0 maximum: 4294967295
     * 
     * @return chargingId
     * @deprecated
     **/
    @Deprecated
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Integer where the allowed values correspond to the value range of an unsigned 32-bit integer. ")
    @JsonProperty(JSON_PROPERTY_CHARGING_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getChargingId()
    {
        return chargingId;
    }

    @JsonProperty(JSON_PROPERTY_CHARGING_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setChargingId(Integer chargingId)
    {
        this.chargingId = chargingId;
    }

    public PDUSessionChargingInformation sMFChargingId(String sMFChargingId)
    {

        this.sMFChargingId = sMFChargingId;
        return this;
    }

    /**
     * Get sMFChargingId
     * 
     * @return sMFChargingId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_S_M_F_CHARGING_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getsMFChargingId()
    {
        return sMFChargingId;
    }

    @JsonProperty(JSON_PROPERTY_S_M_F_CHARGING_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setsMFChargingId(String sMFChargingId)
    {
        this.sMFChargingId = sMFChargingId;
    }

    public PDUSessionChargingInformation userInformation(UserInformation userInformation)
    {

        this.userInformation = userInformation;
        return this;
    }

    /**
     * Get userInformation
     * 
     * @return userInformation
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_USER_INFORMATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UserInformation getUserInformation()
    {
        return userInformation;
    }

    @JsonProperty(JSON_PROPERTY_USER_INFORMATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUserInformation(UserInformation userInformation)
    {
        this.userInformation = userInformation;
    }

    public PDUSessionChargingInformation userLocationinfo(UserLocation userLocationinfo)
    {

        this.userLocationinfo = userLocationinfo;
        return this;
    }

    /**
     * Get userLocationinfo
     * 
     * @return userLocationinfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_USER_LOCATIONINFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UserLocation getUserLocationinfo()
    {
        return userLocationinfo;
    }

    @JsonProperty(JSON_PROPERTY_USER_LOCATIONINFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUserLocationinfo(UserLocation userLocationinfo)
    {
        this.userLocationinfo = userLocationinfo;
    }

    public PDUSessionChargingInformation mAPDUNon3GPPUserLocationInfo(UserLocation mAPDUNon3GPPUserLocationInfo)
    {

        this.mAPDUNon3GPPUserLocationInfo = mAPDUNon3GPPUserLocationInfo;
        return this;
    }

    /**
     * Get mAPDUNon3GPPUserLocationInfo
     * 
     * @return mAPDUNon3GPPUserLocationInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_M_A_P_D_U_NON3_G_P_P_USER_LOCATION_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UserLocation getmAPDUNon3GPPUserLocationInfo()
    {
        return mAPDUNon3GPPUserLocationInfo;
    }

    @JsonProperty(JSON_PROPERTY_M_A_P_D_U_NON3_G_P_P_USER_LOCATION_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setmAPDUNon3GPPUserLocationInfo(UserLocation mAPDUNon3GPPUserLocationInfo)
    {
        this.mAPDUNon3GPPUserLocationInfo = mAPDUNon3GPPUserLocationInfo;
    }

    public PDUSessionChargingInformation userLocationTime(OffsetDateTime userLocationTime)
    {

        this.userLocationTime = userLocationTime;
        return this;
    }

    /**
     * string with format &#39;date-time&#39; as defined in OpenAPI.
     * 
     * @return userLocationTime
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'date-time' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_USER_LOCATION_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getUserLocationTime()
    {
        return userLocationTime;
    }

    @JsonProperty(JSON_PROPERTY_USER_LOCATION_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUserLocationTime(OffsetDateTime userLocationTime)
    {
        this.userLocationTime = userLocationTime;
    }

    public PDUSessionChargingInformation presenceReportingAreaInformation(Map<String, PresenceInfo> presenceReportingAreaInformation)
    {

        this.presenceReportingAreaInformation = presenceReportingAreaInformation;
        return this;
    }

    public PDUSessionChargingInformation putPresenceReportingAreaInformationItem(String key,
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

    public PDUSessionChargingInformation uetimeZone(String uetimeZone)
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

    public PDUSessionChargingInformation pduSessionInformation(PDUSessionInformation pduSessionInformation)
    {

        this.pduSessionInformation = pduSessionInformation;
        return this;
    }

    /**
     * Get pduSessionInformation
     * 
     * @return pduSessionInformation
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_PDU_SESSION_INFORMATION)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public PDUSessionInformation getPduSessionInformation()
    {
        return pduSessionInformation;
    }

    @JsonProperty(JSON_PROPERTY_PDU_SESSION_INFORMATION)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setPduSessionInformation(PDUSessionInformation pduSessionInformation)
    {
        this.pduSessionInformation = pduSessionInformation;
    }

    public PDUSessionChargingInformation unitCountInactivityTimer(Integer unitCountInactivityTimer)
    {

        this.unitCountInactivityTimer = unitCountInactivityTimer;
        return this;
    }

    /**
     * indicating a time in seconds.
     * 
     * @return unitCountInactivityTimer
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "indicating a time in seconds.")
    @JsonProperty(JSON_PROPERTY_UNIT_COUNT_INACTIVITY_TIMER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getUnitCountInactivityTimer()
    {
        return unitCountInactivityTimer;
    }

    @JsonProperty(JSON_PROPERTY_UNIT_COUNT_INACTIVITY_TIMER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUnitCountInactivityTimer(Integer unitCountInactivityTimer)
    {
        this.unitCountInactivityTimer = unitCountInactivityTimer;
    }

    public PDUSessionChargingInformation rANSecondaryRATUsageReport(RANSecondaryRATUsageReport rANSecondaryRATUsageReport)
    {

        this.rANSecondaryRATUsageReport = rANSecondaryRATUsageReport;
        return this;
    }

    /**
     * Get rANSecondaryRATUsageReport
     * 
     * @return rANSecondaryRATUsageReport
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_R_A_N_SECONDARY_R_A_T_USAGE_REPORT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public RANSecondaryRATUsageReport getrANSecondaryRATUsageReport()
    {
        return rANSecondaryRATUsageReport;
    }

    @JsonProperty(JSON_PROPERTY_R_A_N_SECONDARY_R_A_T_USAGE_REPORT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setrANSecondaryRATUsageReport(RANSecondaryRATUsageReport rANSecondaryRATUsageReport)
    {
        this.rANSecondaryRATUsageReport = rANSecondaryRATUsageReport;
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
        PDUSessionChargingInformation pdUSessionChargingInformation = (PDUSessionChargingInformation) o;
        return Objects.equals(this.chargingId, pdUSessionChargingInformation.chargingId)
               && Objects.equals(this.sMFChargingId, pdUSessionChargingInformation.sMFChargingId)
               && Objects.equals(this.userInformation, pdUSessionChargingInformation.userInformation)
               && Objects.equals(this.userLocationinfo, pdUSessionChargingInformation.userLocationinfo)
               && Objects.equals(this.mAPDUNon3GPPUserLocationInfo, pdUSessionChargingInformation.mAPDUNon3GPPUserLocationInfo)
               && Objects.equals(this.userLocationTime, pdUSessionChargingInformation.userLocationTime)
               && Objects.equals(this.presenceReportingAreaInformation, pdUSessionChargingInformation.presenceReportingAreaInformation)
               && Objects.equals(this.uetimeZone, pdUSessionChargingInformation.uetimeZone)
               && Objects.equals(this.pduSessionInformation, pdUSessionChargingInformation.pduSessionInformation)
               && Objects.equals(this.unitCountInactivityTimer, pdUSessionChargingInformation.unitCountInactivityTimer)
               && Objects.equals(this.rANSecondaryRATUsageReport, pdUSessionChargingInformation.rANSecondaryRATUsageReport);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(chargingId,
                            sMFChargingId,
                            userInformation,
                            userLocationinfo,
                            mAPDUNon3GPPUserLocationInfo,
                            userLocationTime,
                            presenceReportingAreaInformation,
                            uetimeZone,
                            pduSessionInformation,
                            unitCountInactivityTimer,
                            rANSecondaryRATUsageReport);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class PDUSessionChargingInformation {\n");
        sb.append("    chargingId: ").append(toIndentedString(chargingId)).append("\n");
        sb.append("    sMFChargingId: ").append(toIndentedString(sMFChargingId)).append("\n");
        sb.append("    userInformation: ").append(toIndentedString(userInformation)).append("\n");
        sb.append("    userLocationinfo: ").append(toIndentedString(userLocationinfo)).append("\n");
        sb.append("    mAPDUNon3GPPUserLocationInfo: ").append(toIndentedString(mAPDUNon3GPPUserLocationInfo)).append("\n");
        sb.append("    userLocationTime: ").append(toIndentedString(userLocationTime)).append("\n");
        sb.append("    presenceReportingAreaInformation: ").append(toIndentedString(presenceReportingAreaInformation)).append("\n");
        sb.append("    uetimeZone: ").append(toIndentedString(uetimeZone)).append("\n");
        sb.append("    pduSessionInformation: ").append(toIndentedString(pduSessionInformation)).append("\n");
        sb.append("    unitCountInactivityTimer: ").append(toIndentedString(unitCountInactivityTimer)).append("\n");
        sb.append("    rANSecondaryRATUsageReport: ").append(toIndentedString(rANSecondaryRATUsageReport)).append("\n");
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
