/*
 * Unified Data Repository Service API file for structured data for exposure
 * The API version is defined in 3GPP TS 29.504   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: -
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29519.exposure.data;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.AccessType;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.PlmnId;
import com.ericsson.cnal.openapi.r17.ts29518.namf.eventexposure.CmInfo;
import com.ericsson.cnal.openapi.r17.ts29518.namf.eventexposure.RmInfo;
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
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Represents Access and Mobility data for a UE.
 */
@ApiModel(description = "Represents Access and Mobility data for a UE.")
@JsonPropertyOrder({ AccessAndMobilityData.JSON_PROPERTY_LOCATION,
                     AccessAndMobilityData.JSON_PROPERTY_LOCATION_TS,
                     AccessAndMobilityData.JSON_PROPERTY_TIME_ZONE,
                     AccessAndMobilityData.JSON_PROPERTY_TIME_ZONE_TS,
                     AccessAndMobilityData.JSON_PROPERTY_ACCESS_TYPE,
                     AccessAndMobilityData.JSON_PROPERTY_REG_STATES,
                     AccessAndMobilityData.JSON_PROPERTY_REG_STATES_TS,
                     AccessAndMobilityData.JSON_PROPERTY_CONN_STATES,
                     AccessAndMobilityData.JSON_PROPERTY_CONN_STATES_TS,
                     AccessAndMobilityData.JSON_PROPERTY_REACHABILITY_STATUS,
                     AccessAndMobilityData.JSON_PROPERTY_REACHABILITY_STATUS_TS,
                     AccessAndMobilityData.JSON_PROPERTY_SMS_OVER_NAS_STATUS,
                     AccessAndMobilityData.JSON_PROPERTY_SMS_OVER_NAS_STATUS_TS,
                     AccessAndMobilityData.JSON_PROPERTY_ROAMING_STATUS,
                     AccessAndMobilityData.JSON_PROPERTY_ROAMING_STATUS_TS,
                     AccessAndMobilityData.JSON_PROPERTY_CURRENT_PLMN,
                     AccessAndMobilityData.JSON_PROPERTY_CURRENT_PLMN_TS,
                     AccessAndMobilityData.JSON_PROPERTY_RAT_TYPE,
                     AccessAndMobilityData.JSON_PROPERTY_RAT_TYPES_TS,
                     AccessAndMobilityData.JSON_PROPERTY_SUPP_FEAT,
                     AccessAndMobilityData.JSON_PROPERTY_RESET_IDS })
public class AccessAndMobilityData
{
    public static final String JSON_PROPERTY_LOCATION = "location";
    private UserLocation location;

    public static final String JSON_PROPERTY_LOCATION_TS = "locationTs";
    private OffsetDateTime locationTs;

    public static final String JSON_PROPERTY_TIME_ZONE = "timeZone";
    private String timeZone;

    public static final String JSON_PROPERTY_TIME_ZONE_TS = "timeZoneTs";
    private OffsetDateTime timeZoneTs;

    public static final String JSON_PROPERTY_ACCESS_TYPE = "accessType";
    private AccessType accessType;

    public static final String JSON_PROPERTY_REG_STATES = "regStates";
    private List<RmInfo> regStates = null;

    public static final String JSON_PROPERTY_REG_STATES_TS = "regStatesTs";
    private OffsetDateTime regStatesTs;

    public static final String JSON_PROPERTY_CONN_STATES = "connStates";
    private List<CmInfo> connStates = null;

    public static final String JSON_PROPERTY_CONN_STATES_TS = "connStatesTs";
    private OffsetDateTime connStatesTs;

    public static final String JSON_PROPERTY_REACHABILITY_STATUS = "reachabilityStatus";
    private String reachabilityStatus;

    public static final String JSON_PROPERTY_REACHABILITY_STATUS_TS = "reachabilityStatusTs";
    private OffsetDateTime reachabilityStatusTs;

    public static final String JSON_PROPERTY_SMS_OVER_NAS_STATUS = "smsOverNasStatus";
    private String smsOverNasStatus;

    public static final String JSON_PROPERTY_SMS_OVER_NAS_STATUS_TS = "smsOverNasStatusTs";
    private OffsetDateTime smsOverNasStatusTs;

    public static final String JSON_PROPERTY_ROAMING_STATUS = "roamingStatus";
    private Boolean roamingStatus;

    public static final String JSON_PROPERTY_ROAMING_STATUS_TS = "roamingStatusTs";
    private OffsetDateTime roamingStatusTs;

    public static final String JSON_PROPERTY_CURRENT_PLMN = "currentPlmn";
    private PlmnId currentPlmn;

    public static final String JSON_PROPERTY_CURRENT_PLMN_TS = "currentPlmnTs";
    private OffsetDateTime currentPlmnTs;

    public static final String JSON_PROPERTY_RAT_TYPE = "ratType";
    private List<String> ratType = null;

    public static final String JSON_PROPERTY_RAT_TYPES_TS = "ratTypesTs";
    private OffsetDateTime ratTypesTs;

    public static final String JSON_PROPERTY_SUPP_FEAT = "suppFeat";
    private String suppFeat;

    public static final String JSON_PROPERTY_RESET_IDS = "resetIds";
    private List<String> resetIds = null;

    public AccessAndMobilityData()
    {
    }

    public AccessAndMobilityData location(UserLocation location)
    {

        this.location = location;
        return this;
    }

    /**
     * Get location
     * 
     * @return location
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_LOCATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UserLocation getLocation()
    {
        return location;
    }

    @JsonProperty(JSON_PROPERTY_LOCATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLocation(UserLocation location)
    {
        this.location = location;
    }

    public AccessAndMobilityData locationTs(OffsetDateTime locationTs)
    {

        this.locationTs = locationTs;
        return this;
    }

    /**
     * string with format &#39;date-time&#39; as defined in OpenAPI.
     * 
     * @return locationTs
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'date-time' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_LOCATION_TS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getLocationTs()
    {
        return locationTs;
    }

    @JsonProperty(JSON_PROPERTY_LOCATION_TS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLocationTs(OffsetDateTime locationTs)
    {
        this.locationTs = locationTs;
    }

    public AccessAndMobilityData timeZone(String timeZone)
    {

        this.timeZone = timeZone;
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
     * @return timeZone
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(example = "-08:00+1",
                      value = "String with format \"time-numoffset\" optionally appended by \"daylightSavingTime\", where  - \"time-numoffset\" shall represent the time zone adjusted for daylight saving time and be    encoded as time-numoffset as defined in clause 5.6 of IETF RFC 3339;  - \"daylightSavingTime\" shall represent the adjustment that has been made and shall be    encoded as \"+1\" or \"+2\" for a +1 or +2 hours adjustment.  The example is for 8 hours behind UTC, +1 hour adjustment for Daylight Saving Time. ")
    @JsonProperty(JSON_PROPERTY_TIME_ZONE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getTimeZone()
    {
        return timeZone;
    }

    @JsonProperty(JSON_PROPERTY_TIME_ZONE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTimeZone(String timeZone)
    {
        this.timeZone = timeZone;
    }

    public AccessAndMobilityData timeZoneTs(OffsetDateTime timeZoneTs)
    {

        this.timeZoneTs = timeZoneTs;
        return this;
    }

    /**
     * string with format &#39;date-time&#39; as defined in OpenAPI.
     * 
     * @return timeZoneTs
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'date-time' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_TIME_ZONE_TS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getTimeZoneTs()
    {
        return timeZoneTs;
    }

    @JsonProperty(JSON_PROPERTY_TIME_ZONE_TS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTimeZoneTs(OffsetDateTime timeZoneTs)
    {
        this.timeZoneTs = timeZoneTs;
    }

    public AccessAndMobilityData accessType(AccessType accessType)
    {

        this.accessType = accessType;
        return this;
    }

    /**
     * Get accessType
     * 
     * @return accessType
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_ACCESS_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public AccessType getAccessType()
    {
        return accessType;
    }

    @JsonProperty(JSON_PROPERTY_ACCESS_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAccessType(AccessType accessType)
    {
        this.accessType = accessType;
    }

    public AccessAndMobilityData regStates(List<RmInfo> regStates)
    {

        this.regStates = regStates;
        return this;
    }

    public AccessAndMobilityData addRegStatesItem(RmInfo regStatesItem)
    {
        if (this.regStates == null)
        {
            this.regStates = new ArrayList<>();
        }
        this.regStates.add(regStatesItem);
        return this;
    }

    /**
     * Get regStates
     * 
     * @return regStates
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_REG_STATES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<RmInfo> getRegStates()
    {
        return regStates;
    }

    @JsonProperty(JSON_PROPERTY_REG_STATES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRegStates(List<RmInfo> regStates)
    {
        this.regStates = regStates;
    }

    public AccessAndMobilityData regStatesTs(OffsetDateTime regStatesTs)
    {

        this.regStatesTs = regStatesTs;
        return this;
    }

    /**
     * string with format &#39;date-time&#39; as defined in OpenAPI.
     * 
     * @return regStatesTs
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'date-time' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_REG_STATES_TS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getRegStatesTs()
    {
        return regStatesTs;
    }

    @JsonProperty(JSON_PROPERTY_REG_STATES_TS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRegStatesTs(OffsetDateTime regStatesTs)
    {
        this.regStatesTs = regStatesTs;
    }

    public AccessAndMobilityData connStates(List<CmInfo> connStates)
    {

        this.connStates = connStates;
        return this;
    }

    public AccessAndMobilityData addConnStatesItem(CmInfo connStatesItem)
    {
        if (this.connStates == null)
        {
            this.connStates = new ArrayList<>();
        }
        this.connStates.add(connStatesItem);
        return this;
    }

    /**
     * Get connStates
     * 
     * @return connStates
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_CONN_STATES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<CmInfo> getConnStates()
    {
        return connStates;
    }

    @JsonProperty(JSON_PROPERTY_CONN_STATES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setConnStates(List<CmInfo> connStates)
    {
        this.connStates = connStates;
    }

    public AccessAndMobilityData connStatesTs(OffsetDateTime connStatesTs)
    {

        this.connStatesTs = connStatesTs;
        return this;
    }

    /**
     * string with format &#39;date-time&#39; as defined in OpenAPI.
     * 
     * @return connStatesTs
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'date-time' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_CONN_STATES_TS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getConnStatesTs()
    {
        return connStatesTs;
    }

    @JsonProperty(JSON_PROPERTY_CONN_STATES_TS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setConnStatesTs(OffsetDateTime connStatesTs)
    {
        this.connStatesTs = connStatesTs;
    }

    public AccessAndMobilityData reachabilityStatus(String reachabilityStatus)
    {

        this.reachabilityStatus = reachabilityStatus;
        return this;
    }

    /**
     * Describes the reachability of the UE
     * 
     * @return reachabilityStatus
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Describes the reachability of the UE")
    @JsonProperty(JSON_PROPERTY_REACHABILITY_STATUS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getReachabilityStatus()
    {
        return reachabilityStatus;
    }

    @JsonProperty(JSON_PROPERTY_REACHABILITY_STATUS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setReachabilityStatus(String reachabilityStatus)
    {
        this.reachabilityStatus = reachabilityStatus;
    }

    public AccessAndMobilityData reachabilityStatusTs(OffsetDateTime reachabilityStatusTs)
    {

        this.reachabilityStatusTs = reachabilityStatusTs;
        return this;
    }

    /**
     * string with format &#39;date-time&#39; as defined in OpenAPI.
     * 
     * @return reachabilityStatusTs
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'date-time' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_REACHABILITY_STATUS_TS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getReachabilityStatusTs()
    {
        return reachabilityStatusTs;
    }

    @JsonProperty(JSON_PROPERTY_REACHABILITY_STATUS_TS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setReachabilityStatusTs(OffsetDateTime reachabilityStatusTs)
    {
        this.reachabilityStatusTs = reachabilityStatusTs;
    }

    public AccessAndMobilityData smsOverNasStatus(String smsOverNasStatus)
    {

        this.smsOverNasStatus = smsOverNasStatus;
        return this;
    }

    /**
     * Indicates the supported SMS delivery of a UE
     * 
     * @return smsOverNasStatus
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Indicates the supported SMS delivery of a UE")
    @JsonProperty(JSON_PROPERTY_SMS_OVER_NAS_STATUS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getSmsOverNasStatus()
    {
        return smsOverNasStatus;
    }

    @JsonProperty(JSON_PROPERTY_SMS_OVER_NAS_STATUS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSmsOverNasStatus(String smsOverNasStatus)
    {
        this.smsOverNasStatus = smsOverNasStatus;
    }

    public AccessAndMobilityData smsOverNasStatusTs(OffsetDateTime smsOverNasStatusTs)
    {

        this.smsOverNasStatusTs = smsOverNasStatusTs;
        return this;
    }

    /**
     * string with format &#39;date-time&#39; as defined in OpenAPI.
     * 
     * @return smsOverNasStatusTs
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'date-time' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_SMS_OVER_NAS_STATUS_TS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getSmsOverNasStatusTs()
    {
        return smsOverNasStatusTs;
    }

    @JsonProperty(JSON_PROPERTY_SMS_OVER_NAS_STATUS_TS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSmsOverNasStatusTs(OffsetDateTime smsOverNasStatusTs)
    {
        this.smsOverNasStatusTs = smsOverNasStatusTs;
    }

    public AccessAndMobilityData roamingStatus(Boolean roamingStatus)
    {

        this.roamingStatus = roamingStatus;
        return this;
    }

    /**
     * True The serving PLMN of the UE is different from the HPLMN of the UE; False
     * The serving PLMN of the UE is the HPLMN of the UE.
     * 
     * @return roamingStatus
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "True  The serving PLMN of the UE is different from the HPLMN of the UE; False The serving PLMN of the UE is the HPLMN of the UE. ")
    @JsonProperty(JSON_PROPERTY_ROAMING_STATUS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getRoamingStatus()
    {
        return roamingStatus;
    }

    @JsonProperty(JSON_PROPERTY_ROAMING_STATUS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRoamingStatus(Boolean roamingStatus)
    {
        this.roamingStatus = roamingStatus;
    }

    public AccessAndMobilityData roamingStatusTs(OffsetDateTime roamingStatusTs)
    {

        this.roamingStatusTs = roamingStatusTs;
        return this;
    }

    /**
     * string with format &#39;date-time&#39; as defined in OpenAPI.
     * 
     * @return roamingStatusTs
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'date-time' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_ROAMING_STATUS_TS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getRoamingStatusTs()
    {
        return roamingStatusTs;
    }

    @JsonProperty(JSON_PROPERTY_ROAMING_STATUS_TS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRoamingStatusTs(OffsetDateTime roamingStatusTs)
    {
        this.roamingStatusTs = roamingStatusTs;
    }

    public AccessAndMobilityData currentPlmn(PlmnId currentPlmn)
    {

        this.currentPlmn = currentPlmn;
        return this;
    }

    /**
     * Get currentPlmn
     * 
     * @return currentPlmn
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_CURRENT_PLMN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public PlmnId getCurrentPlmn()
    {
        return currentPlmn;
    }

    @JsonProperty(JSON_PROPERTY_CURRENT_PLMN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCurrentPlmn(PlmnId currentPlmn)
    {
        this.currentPlmn = currentPlmn;
    }

    public AccessAndMobilityData currentPlmnTs(OffsetDateTime currentPlmnTs)
    {

        this.currentPlmnTs = currentPlmnTs;
        return this;
    }

    /**
     * string with format &#39;date-time&#39; as defined in OpenAPI.
     * 
     * @return currentPlmnTs
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'date-time' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_CURRENT_PLMN_TS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getCurrentPlmnTs()
    {
        return currentPlmnTs;
    }

    @JsonProperty(JSON_PROPERTY_CURRENT_PLMN_TS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCurrentPlmnTs(OffsetDateTime currentPlmnTs)
    {
        this.currentPlmnTs = currentPlmnTs;
    }

    public AccessAndMobilityData ratType(List<String> ratType)
    {

        this.ratType = ratType;
        return this;
    }

    public AccessAndMobilityData addRatTypeItem(String ratTypeItem)
    {
        if (this.ratType == null)
        {
            this.ratType = new ArrayList<>();
        }
        this.ratType.add(ratTypeItem);
        return this;
    }

    /**
     * Get ratType
     * 
     * @return ratType
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_RAT_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getRatType()
    {
        return ratType;
    }

    @JsonProperty(JSON_PROPERTY_RAT_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRatType(List<String> ratType)
    {
        this.ratType = ratType;
    }

    public AccessAndMobilityData ratTypesTs(OffsetDateTime ratTypesTs)
    {

        this.ratTypesTs = ratTypesTs;
        return this;
    }

    /**
     * string with format &#39;date-time&#39; as defined in OpenAPI.
     * 
     * @return ratTypesTs
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'date-time' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_RAT_TYPES_TS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getRatTypesTs()
    {
        return ratTypesTs;
    }

    @JsonProperty(JSON_PROPERTY_RAT_TYPES_TS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRatTypesTs(OffsetDateTime ratTypesTs)
    {
        this.ratTypesTs = ratTypesTs;
    }

    public AccessAndMobilityData suppFeat(String suppFeat)
    {

        this.suppFeat = suppFeat;
        return this;
    }

    /**
     * A string used to indicate the features supported by an API that is used as
     * defined in clause 6.6 in 3GPP TS 29.500. The string shall contain a bitmask
     * indicating supported features in hexadecimal representation Each character in
     * the string shall take a value of \&quot;0\&quot; to \&quot;9\&quot;,
     * \&quot;a\&quot; to \&quot;f\&quot; or \&quot;A\&quot; to \&quot;F\&quot; and
     * shall represent the support of 4 features as described in table 5.2.2-3. The
     * most significant character representing the highest-numbered features shall
     * appear first in the string, and the character representing features 1 to 4
     * shall appear last in the string. The list of features and their numbering
     * (starting with 1) are defined separately for each API. If the string contains
     * a lower number of characters than there are defined features for an API, all
     * features that would be represented by characters that are not present in the
     * string are not supported.
     * 
     * @return suppFeat
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "A string used to indicate the features supported by an API that is used as defined in clause  6.6 in 3GPP TS 29.500. The string shall contain a bitmask indicating supported features in  hexadecimal representation Each character in the string shall take a value of \"0\" to \"9\",  \"a\" to \"f\" or \"A\" to \"F\" and shall represent the support of 4 features as described in  table 5.2.2-3. The most significant character representing the highest-numbered features shall  appear first in the string, and the character representing features 1 to 4 shall appear last  in the string. The list of features and their numbering (starting with 1) are defined  separately for each API. If the string contains a lower number of characters than there are  defined features for an API, all features that would be represented by characters that are not  present in the string are not supported. ")
    @JsonProperty(JSON_PROPERTY_SUPP_FEAT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getSuppFeat()
    {
        return suppFeat;
    }

    @JsonProperty(JSON_PROPERTY_SUPP_FEAT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSuppFeat(String suppFeat)
    {
        this.suppFeat = suppFeat;
    }

    public AccessAndMobilityData resetIds(List<String> resetIds)
    {

        this.resetIds = resetIds;
        return this;
    }

    public AccessAndMobilityData addResetIdsItem(String resetIdsItem)
    {
        if (this.resetIds == null)
        {
            this.resetIds = new ArrayList<>();
        }
        this.resetIds.add(resetIdsItem);
        return this;
    }

    /**
     * Get resetIds
     * 
     * @return resetIds
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_RESET_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getResetIds()
    {
        return resetIds;
    }

    @JsonProperty(JSON_PROPERTY_RESET_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setResetIds(List<String> resetIds)
    {
        this.resetIds = resetIds;
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
        AccessAndMobilityData accessAndMobilityData = (AccessAndMobilityData) o;
        return Objects.equals(this.location, accessAndMobilityData.location) && Objects.equals(this.locationTs, accessAndMobilityData.locationTs)
               && Objects.equals(this.timeZone, accessAndMobilityData.timeZone) && Objects.equals(this.timeZoneTs, accessAndMobilityData.timeZoneTs)
               && Objects.equals(this.accessType, accessAndMobilityData.accessType) && Objects.equals(this.regStates, accessAndMobilityData.regStates)
               && Objects.equals(this.regStatesTs, accessAndMobilityData.regStatesTs) && Objects.equals(this.connStates, accessAndMobilityData.connStates)
               && Objects.equals(this.connStatesTs, accessAndMobilityData.connStatesTs)
               && Objects.equals(this.reachabilityStatus, accessAndMobilityData.reachabilityStatus)
               && Objects.equals(this.reachabilityStatusTs, accessAndMobilityData.reachabilityStatusTs)
               && Objects.equals(this.smsOverNasStatus, accessAndMobilityData.smsOverNasStatus)
               && Objects.equals(this.smsOverNasStatusTs, accessAndMobilityData.smsOverNasStatusTs)
               && Objects.equals(this.roamingStatus, accessAndMobilityData.roamingStatus)
               && Objects.equals(this.roamingStatusTs, accessAndMobilityData.roamingStatusTs)
               && Objects.equals(this.currentPlmn, accessAndMobilityData.currentPlmn) && Objects.equals(this.currentPlmnTs, accessAndMobilityData.currentPlmnTs)
               && Objects.equals(this.ratType, accessAndMobilityData.ratType) && Objects.equals(this.ratTypesTs, accessAndMobilityData.ratTypesTs)
               && Objects.equals(this.suppFeat, accessAndMobilityData.suppFeat) && Objects.equals(this.resetIds, accessAndMobilityData.resetIds);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(location,
                            locationTs,
                            timeZone,
                            timeZoneTs,
                            accessType,
                            regStates,
                            regStatesTs,
                            connStates,
                            connStatesTs,
                            reachabilityStatus,
                            reachabilityStatusTs,
                            smsOverNasStatus,
                            smsOverNasStatusTs,
                            roamingStatus,
                            roamingStatusTs,
                            currentPlmn,
                            currentPlmnTs,
                            ratType,
                            ratTypesTs,
                            suppFeat,
                            resetIds);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class AccessAndMobilityData {\n");
        sb.append("    location: ").append(toIndentedString(location)).append("\n");
        sb.append("    locationTs: ").append(toIndentedString(locationTs)).append("\n");
        sb.append("    timeZone: ").append(toIndentedString(timeZone)).append("\n");
        sb.append("    timeZoneTs: ").append(toIndentedString(timeZoneTs)).append("\n");
        sb.append("    accessType: ").append(toIndentedString(accessType)).append("\n");
        sb.append("    regStates: ").append(toIndentedString(regStates)).append("\n");
        sb.append("    regStatesTs: ").append(toIndentedString(regStatesTs)).append("\n");
        sb.append("    connStates: ").append(toIndentedString(connStates)).append("\n");
        sb.append("    connStatesTs: ").append(toIndentedString(connStatesTs)).append("\n");
        sb.append("    reachabilityStatus: ").append(toIndentedString(reachabilityStatus)).append("\n");
        sb.append("    reachabilityStatusTs: ").append(toIndentedString(reachabilityStatusTs)).append("\n");
        sb.append("    smsOverNasStatus: ").append(toIndentedString(smsOverNasStatus)).append("\n");
        sb.append("    smsOverNasStatusTs: ").append(toIndentedString(smsOverNasStatusTs)).append("\n");
        sb.append("    roamingStatus: ").append(toIndentedString(roamingStatus)).append("\n");
        sb.append("    roamingStatusTs: ").append(toIndentedString(roamingStatusTs)).append("\n");
        sb.append("    currentPlmn: ").append(toIndentedString(currentPlmn)).append("\n");
        sb.append("    currentPlmnTs: ").append(toIndentedString(currentPlmnTs)).append("\n");
        sb.append("    ratType: ").append(toIndentedString(ratType)).append("\n");
        sb.append("    ratTypesTs: ").append(toIndentedString(ratTypesTs)).append("\n");
        sb.append("    suppFeat: ").append(toIndentedString(suppFeat)).append("\n");
        sb.append("    resetIds: ").append(toIndentedString(resetIds)).append("\n");
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
