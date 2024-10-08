/*
 * Nsmf_PDUSession
 * SMF PDU Session Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29502.nsmf.pdusession;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.NgApCause;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.SecondaryRatUsageReport;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.SecondaryRatUsageInfo;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.UserLocation;
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
 * Data within Release Request
 */
@ApiModel(description = "Data within Release Request")
@JsonPropertyOrder({ ReleaseData.JSON_PROPERTY_CAUSE,
                     ReleaseData.JSON_PROPERTY_NG_AP_CAUSE,
                     ReleaseData.JSON_PROPERTY_5G_MM_CAUSE_VALUE,
                     ReleaseData.JSON_PROPERTY_UE_LOCATION,
                     ReleaseData.JSON_PROPERTY_UE_TIME_ZONE,
                     ReleaseData.JSON_PROPERTY_ADD_UE_LOCATION,
                     ReleaseData.JSON_PROPERTY_SECONDARY_RAT_USAGE_REPORT,
                     ReleaseData.JSON_PROPERTY_SECONDARY_RAT_USAGE_INFO,
                     ReleaseData.JSON_PROPERTY_N4_INFO,
                     ReleaseData.JSON_PROPERTY_N4_INFO_EXT1,
                     ReleaseData.JSON_PROPERTY_N4_INFO_EXT2 })
public class ReleaseData
{
    public static final String JSON_PROPERTY_CAUSE = "cause";
    private String cause;

    public static final String JSON_PROPERTY_NG_AP_CAUSE = "ngApCause";
    private NgApCause ngApCause;

    public static final String JSON_PROPERTY_5G_MM_CAUSE_VALUE = "5gMmCauseValue";
    private Integer _5gMmCauseValue;

    public static final String JSON_PROPERTY_UE_LOCATION = "ueLocation";
    private UserLocation ueLocation;

    public static final String JSON_PROPERTY_UE_TIME_ZONE = "ueTimeZone";
    private String ueTimeZone;

    public static final String JSON_PROPERTY_ADD_UE_LOCATION = "addUeLocation";
    private UserLocation addUeLocation;

    public static final String JSON_PROPERTY_SECONDARY_RAT_USAGE_REPORT = "secondaryRatUsageReport";
    private List<SecondaryRatUsageReport> secondaryRatUsageReport = null;

    public static final String JSON_PROPERTY_SECONDARY_RAT_USAGE_INFO = "secondaryRatUsageInfo";
    private List<SecondaryRatUsageInfo> secondaryRatUsageInfo = null;

    public static final String JSON_PROPERTY_N4_INFO = "n4Info";
    private N4Information n4Info;

    public static final String JSON_PROPERTY_N4_INFO_EXT1 = "n4InfoExt1";
    private N4Information n4InfoExt1;

    public static final String JSON_PROPERTY_N4_INFO_EXT2 = "n4InfoExt2";
    private N4Information n4InfoExt2;

    public ReleaseData()
    {
    }

    public ReleaseData cause(String cause)
    {

        this.cause = cause;
        return this;
    }

    /**
     * Cause information. Possible values are - REL_DUE_TO_HO - EPS_FALLBACK -
     * REL_DUE_TO_UP_SEC - DNN_CONGESTION - S_NSSAI_CONGESTION -
     * REL_DUE_TO_REACTIVATION - 5G_AN_NOT_RESPONDING -
     * REL_DUE_TO_SLICE_NOT_AVAILABLE - REL_DUE_TO_DUPLICATE_SESSION_ID -
     * PDU_SESSION_STATUS_MISMATCH - HO_FAILURE - INSUFFICIENT_UP_RESOURCES -
     * PDU_SESSION_HANDED_OVER - PDU_SESSION_RESUMED -
     * CN_ASSISTED_RAN_PARAMETER_TUNING - ISMF_CONTEXT_TRANSFER -
     * SMF_CONTEXT_TRANSFER - REL_DUE_TO_PS_TO_CS_HO -
     * REL_DUE_TO_SUBSCRIPTION_CHANGE - HO_CANCEL - REL_DUE_TO_SLICE_NOT_AUTHORIZED
     * - PDU_SESSION_HAND_OVER_FAILURE - DDN_FAILURE_STATUS -
     * REL_DUE_TO_CP_ONLY_NOT_APPLICABLE - NOT_SUPPORTED_WITH_ISMF -
     * CHANGED_ANCHOR_SMF - CHANGED_INTERMEDIATE_SMF - TARGET_DNAI_NOTIFICATION -
     * REL_DUE_TO_VPLMN_QOS_FAILURE - REL_DUE_TO_SMF_NOT_SUPPORT_PSETR -
     * REL_DUE_TO_SNPN_SNPN_MOBILITY - REL_DUE_TO_NO_HR_AGREEMENT -
     * REL_DUE_TO_UNSPECIFIED_REASON
     * 
     * @return cause
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Cause information. Possible values are - REL_DUE_TO_HO - EPS_FALLBACK - REL_DUE_TO_UP_SEC - DNN_CONGESTION - S_NSSAI_CONGESTION - REL_DUE_TO_REACTIVATION - 5G_AN_NOT_RESPONDING - REL_DUE_TO_SLICE_NOT_AVAILABLE - REL_DUE_TO_DUPLICATE_SESSION_ID - PDU_SESSION_STATUS_MISMATCH - HO_FAILURE - INSUFFICIENT_UP_RESOURCES - PDU_SESSION_HANDED_OVER - PDU_SESSION_RESUMED - CN_ASSISTED_RAN_PARAMETER_TUNING - ISMF_CONTEXT_TRANSFER - SMF_CONTEXT_TRANSFER - REL_DUE_TO_PS_TO_CS_HO - REL_DUE_TO_SUBSCRIPTION_CHANGE - HO_CANCEL - REL_DUE_TO_SLICE_NOT_AUTHORIZED - PDU_SESSION_HAND_OVER_FAILURE - DDN_FAILURE_STATUS - REL_DUE_TO_CP_ONLY_NOT_APPLICABLE - NOT_SUPPORTED_WITH_ISMF - CHANGED_ANCHOR_SMF - CHANGED_INTERMEDIATE_SMF - TARGET_DNAI_NOTIFICATION - REL_DUE_TO_VPLMN_QOS_FAILURE - REL_DUE_TO_SMF_NOT_SUPPORT_PSETR - REL_DUE_TO_SNPN_SNPN_MOBILITY - REL_DUE_TO_NO_HR_AGREEMENT - REL_DUE_TO_UNSPECIFIED_REASON ")
    @JsonProperty(JSON_PROPERTY_CAUSE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getCause()
    {
        return cause;
    }

    @JsonProperty(JSON_PROPERTY_CAUSE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCause(String cause)
    {
        this.cause = cause;
    }

    public ReleaseData ngApCause(NgApCause ngApCause)
    {

        this.ngApCause = ngApCause;
        return this;
    }

    /**
     * Get ngApCause
     * 
     * @return ngApCause
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_NG_AP_CAUSE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public NgApCause getNgApCause()
    {
        return ngApCause;
    }

    @JsonProperty(JSON_PROPERTY_NG_AP_CAUSE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNgApCause(NgApCause ngApCause)
    {
        this.ngApCause = ngApCause;
    }

    public ReleaseData _5gMmCauseValue(Integer _5gMmCauseValue)
    {

        this._5gMmCauseValue = _5gMmCauseValue;
        return this;
    }

    /**
     * Unsigned Integer, i.e. only value 0 and integers above 0 are permissible.
     * minimum: 0
     * 
     * @return _5gMmCauseValue
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Unsigned Integer, i.e. only value 0 and integers above 0 are permissible.")
    @JsonProperty(JSON_PROPERTY_5G_MM_CAUSE_VALUE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer get5gMmCauseValue()
    {
        return _5gMmCauseValue;
    }

    @JsonProperty(JSON_PROPERTY_5G_MM_CAUSE_VALUE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void set5gMmCauseValue(Integer _5gMmCauseValue)
    {
        this._5gMmCauseValue = _5gMmCauseValue;
    }

    public ReleaseData ueLocation(UserLocation ueLocation)
    {

        this.ueLocation = ueLocation;
        return this;
    }

    /**
     * Get ueLocation
     * 
     * @return ueLocation
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_UE_LOCATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UserLocation getUeLocation()
    {
        return ueLocation;
    }

    @JsonProperty(JSON_PROPERTY_UE_LOCATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUeLocation(UserLocation ueLocation)
    {
        this.ueLocation = ueLocation;
    }

    public ReleaseData ueTimeZone(String ueTimeZone)
    {

        this.ueTimeZone = ueTimeZone;
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
     * @return ueTimeZone
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(example = "-08:00+1",
                      value = "String with format \"time-numoffset\" optionally appended by \"daylightSavingTime\", where  - \"time-numoffset\" shall represent the time zone adjusted for daylight saving time and be    encoded as time-numoffset as defined in clause 5.6 of IETF RFC 3339;  - \"daylightSavingTime\" shall represent the adjustment that has been made and shall be    encoded as \"+1\" or \"+2\" for a +1 or +2 hours adjustment.  The example is for 8 hours behind UTC, +1 hour adjustment for Daylight Saving Time. ")
    @JsonProperty(JSON_PROPERTY_UE_TIME_ZONE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getUeTimeZone()
    {
        return ueTimeZone;
    }

    @JsonProperty(JSON_PROPERTY_UE_TIME_ZONE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUeTimeZone(String ueTimeZone)
    {
        this.ueTimeZone = ueTimeZone;
    }

    public ReleaseData addUeLocation(UserLocation addUeLocation)
    {

        this.addUeLocation = addUeLocation;
        return this;
    }

    /**
     * Get addUeLocation
     * 
     * @return addUeLocation
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_ADD_UE_LOCATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UserLocation getAddUeLocation()
    {
        return addUeLocation;
    }

    @JsonProperty(JSON_PROPERTY_ADD_UE_LOCATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAddUeLocation(UserLocation addUeLocation)
    {
        this.addUeLocation = addUeLocation;
    }

    public ReleaseData secondaryRatUsageReport(List<SecondaryRatUsageReport> secondaryRatUsageReport)
    {

        this.secondaryRatUsageReport = secondaryRatUsageReport;
        return this;
    }

    public ReleaseData addSecondaryRatUsageReportItem(SecondaryRatUsageReport secondaryRatUsageReportItem)
    {
        if (this.secondaryRatUsageReport == null)
        {
            this.secondaryRatUsageReport = new ArrayList<>();
        }
        this.secondaryRatUsageReport.add(secondaryRatUsageReportItem);
        return this;
    }

    /**
     * Get secondaryRatUsageReport
     * 
     * @return secondaryRatUsageReport
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SECONDARY_RAT_USAGE_REPORT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<SecondaryRatUsageReport> getSecondaryRatUsageReport()
    {
        return secondaryRatUsageReport;
    }

    @JsonProperty(JSON_PROPERTY_SECONDARY_RAT_USAGE_REPORT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSecondaryRatUsageReport(List<SecondaryRatUsageReport> secondaryRatUsageReport)
    {
        this.secondaryRatUsageReport = secondaryRatUsageReport;
    }

    public ReleaseData secondaryRatUsageInfo(List<SecondaryRatUsageInfo> secondaryRatUsageInfo)
    {

        this.secondaryRatUsageInfo = secondaryRatUsageInfo;
        return this;
    }

    public ReleaseData addSecondaryRatUsageInfoItem(SecondaryRatUsageInfo secondaryRatUsageInfoItem)
    {
        if (this.secondaryRatUsageInfo == null)
        {
            this.secondaryRatUsageInfo = new ArrayList<>();
        }
        this.secondaryRatUsageInfo.add(secondaryRatUsageInfoItem);
        return this;
    }

    /**
     * Get secondaryRatUsageInfo
     * 
     * @return secondaryRatUsageInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SECONDARY_RAT_USAGE_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<SecondaryRatUsageInfo> getSecondaryRatUsageInfo()
    {
        return secondaryRatUsageInfo;
    }

    @JsonProperty(JSON_PROPERTY_SECONDARY_RAT_USAGE_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSecondaryRatUsageInfo(List<SecondaryRatUsageInfo> secondaryRatUsageInfo)
    {
        this.secondaryRatUsageInfo = secondaryRatUsageInfo;
    }

    public ReleaseData n4Info(N4Information n4Info)
    {

        this.n4Info = n4Info;
        return this;
    }

    /**
     * Get n4Info
     * 
     * @return n4Info
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_N4_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public N4Information getN4Info()
    {
        return n4Info;
    }

    @JsonProperty(JSON_PROPERTY_N4_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setN4Info(N4Information n4Info)
    {
        this.n4Info = n4Info;
    }

    public ReleaseData n4InfoExt1(N4Information n4InfoExt1)
    {

        this.n4InfoExt1 = n4InfoExt1;
        return this;
    }

    /**
     * Get n4InfoExt1
     * 
     * @return n4InfoExt1
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_N4_INFO_EXT1)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public N4Information getN4InfoExt1()
    {
        return n4InfoExt1;
    }

    @JsonProperty(JSON_PROPERTY_N4_INFO_EXT1)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setN4InfoExt1(N4Information n4InfoExt1)
    {
        this.n4InfoExt1 = n4InfoExt1;
    }

    public ReleaseData n4InfoExt2(N4Information n4InfoExt2)
    {

        this.n4InfoExt2 = n4InfoExt2;
        return this;
    }

    /**
     * Get n4InfoExt2
     * 
     * @return n4InfoExt2
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_N4_INFO_EXT2)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public N4Information getN4InfoExt2()
    {
        return n4InfoExt2;
    }

    @JsonProperty(JSON_PROPERTY_N4_INFO_EXT2)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setN4InfoExt2(N4Information n4InfoExt2)
    {
        this.n4InfoExt2 = n4InfoExt2;
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
        ReleaseData releaseData = (ReleaseData) o;
        return Objects.equals(this.cause, releaseData.cause) && Objects.equals(this.ngApCause, releaseData.ngApCause)
               && Objects.equals(this._5gMmCauseValue, releaseData._5gMmCauseValue) && Objects.equals(this.ueLocation, releaseData.ueLocation)
               && Objects.equals(this.ueTimeZone, releaseData.ueTimeZone) && Objects.equals(this.addUeLocation, releaseData.addUeLocation)
               && Objects.equals(this.secondaryRatUsageReport, releaseData.secondaryRatUsageReport)
               && Objects.equals(this.secondaryRatUsageInfo, releaseData.secondaryRatUsageInfo) && Objects.equals(this.n4Info, releaseData.n4Info)
               && Objects.equals(this.n4InfoExt1, releaseData.n4InfoExt1) && Objects.equals(this.n4InfoExt2, releaseData.n4InfoExt2);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(cause,
                            ngApCause,
                            _5gMmCauseValue,
                            ueLocation,
                            ueTimeZone,
                            addUeLocation,
                            secondaryRatUsageReport,
                            secondaryRatUsageInfo,
                            n4Info,
                            n4InfoExt1,
                            n4InfoExt2);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class ReleaseData {\n");
        sb.append("    cause: ").append(toIndentedString(cause)).append("\n");
        sb.append("    ngApCause: ").append(toIndentedString(ngApCause)).append("\n");
        sb.append("    _5gMmCauseValue: ").append(toIndentedString(_5gMmCauseValue)).append("\n");
        sb.append("    ueLocation: ").append(toIndentedString(ueLocation)).append("\n");
        sb.append("    ueTimeZone: ").append(toIndentedString(ueTimeZone)).append("\n");
        sb.append("    addUeLocation: ").append(toIndentedString(addUeLocation)).append("\n");
        sb.append("    secondaryRatUsageReport: ").append(toIndentedString(secondaryRatUsageReport)).append("\n");
        sb.append("    secondaryRatUsageInfo: ").append(toIndentedString(secondaryRatUsageInfo)).append("\n");
        sb.append("    n4Info: ").append(toIndentedString(n4Info)).append("\n");
        sb.append("    n4InfoExt1: ").append(toIndentedString(n4InfoExt1)).append("\n");
        sb.append("    n4InfoExt2: ").append(toIndentedString(n4InfoExt2)).append("\n");
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
