/*
 * 3gpp-cp-parameter-provisioning
 * API for provisioning communication pattern parameters.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29122.cpprovisioning;

import java.util.Objects;
import java.util.Arrays;
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
 * Represents an offered communication pattern parameter set.
 */
@ApiModel(description = "Represents an offered communication pattern parameter set.")
@JsonPropertyOrder({ CpParameterSet.JSON_PROPERTY_SET_ID,
                     CpParameterSet.JSON_PROPERTY_SELF,
                     CpParameterSet.JSON_PROPERTY_VALIDITY_TIME,
                     CpParameterSet.JSON_PROPERTY_PERIODIC_COMMUNICATION_INDICATOR,
                     CpParameterSet.JSON_PROPERTY_COMMUNICATION_DURATION_TIME,
                     CpParameterSet.JSON_PROPERTY_PERIODIC_TIME,
                     CpParameterSet.JSON_PROPERTY_SCHEDULED_COMMUNICATION_TIME,
                     CpParameterSet.JSON_PROPERTY_SCHEDULED_COMMUNICATION_TYPE,
                     CpParameterSet.JSON_PROPERTY_STATIONARY_INDICATION,
                     CpParameterSet.JSON_PROPERTY_BATTERY_INDS,
                     CpParameterSet.JSON_PROPERTY_TRAFFIC_PROFILE,
                     CpParameterSet.JSON_PROPERTY_EXPECTED_UMTS,
                     CpParameterSet.JSON_PROPERTY_EXPECTED_UMT_DAYS })
public class CpParameterSet
{
    public static final String JSON_PROPERTY_SET_ID = "setId";
    private String setId;

    public static final String JSON_PROPERTY_SELF = "self";
    private String self;

    public static final String JSON_PROPERTY_VALIDITY_TIME = "validityTime";
    private OffsetDateTime validityTime;

    public static final String JSON_PROPERTY_PERIODIC_COMMUNICATION_INDICATOR = "periodicCommunicationIndicator";
    private String periodicCommunicationIndicator;

    public static final String JSON_PROPERTY_COMMUNICATION_DURATION_TIME = "communicationDurationTime";
    private Integer communicationDurationTime;

    public static final String JSON_PROPERTY_PERIODIC_TIME = "periodicTime";
    private Integer periodicTime;

    public static final String JSON_PROPERTY_SCHEDULED_COMMUNICATION_TIME = "scheduledCommunicationTime";
    private ScheduledCommunicationTime scheduledCommunicationTime;

    public static final String JSON_PROPERTY_SCHEDULED_COMMUNICATION_TYPE = "scheduledCommunicationType";
    private String scheduledCommunicationType;

    public static final String JSON_PROPERTY_STATIONARY_INDICATION = "stationaryIndication";
    private String stationaryIndication;

    public static final String JSON_PROPERTY_BATTERY_INDS = "batteryInds";
    private List<String> batteryInds = null;

    public static final String JSON_PROPERTY_TRAFFIC_PROFILE = "trafficProfile";
    private String trafficProfile;

    public static final String JSON_PROPERTY_EXPECTED_UMTS = "expectedUmts";
    private List<UmtLocationArea5G> expectedUmts = null;

    public static final String JSON_PROPERTY_EXPECTED_UMT_DAYS = "expectedUmtDays";
    private Integer expectedUmtDays;

    public CpParameterSet()
    {
    }

    public CpParameterSet setId(String setId)
    {

        this.setId = setId;
        return this;
    }

    /**
     * SCS/AS-chosen correlator provided by the SCS/AS in the request to create a
     * resource fo CP parameter set(s).
     * 
     * @return setId
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "SCS/AS-chosen correlator provided by the SCS/AS in the request to create a resource fo CP parameter set(s).")
    @JsonProperty(JSON_PROPERTY_SET_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getSetId()
    {
        return setId;
    }

    @JsonProperty(JSON_PROPERTY_SET_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setSetId(String setId)
    {
        this.setId = setId;
    }

    public CpParameterSet self(String self)
    {

        this.self = self;
        return this;
    }

    /**
     * string formatted according to IETF RFC 3986 identifying a referenced
     * resource.
     * 
     * @return self
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string formatted according to IETF RFC 3986 identifying a referenced resource.")
    @JsonProperty(JSON_PROPERTY_SELF)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getSelf()
    {
        return self;
    }

    @JsonProperty(JSON_PROPERTY_SELF)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSelf(String self)
    {
        this.self = self;
    }

    public CpParameterSet validityTime(OffsetDateTime validityTime)
    {

        this.validityTime = validityTime;
        return this;
    }

    /**
     * string with format \&quot;date-time\&quot; as defined in OpenAPI.
     * 
     * @return validityTime
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format \"date-time\" as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_VALIDITY_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getValidityTime()
    {
        return validityTime;
    }

    @JsonProperty(JSON_PROPERTY_VALIDITY_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setValidityTime(OffsetDateTime validityTime)
    {
        this.validityTime = validityTime;
    }

    public CpParameterSet periodicCommunicationIndicator(String periodicCommunicationIndicator)
    {

        this.periodicCommunicationIndicator = periodicCommunicationIndicator;
        return this;
    }

    /**
     * Possible values are - PERIODICALLY: Identifies the UE communicates
     * periodically - ON_DEMAND: Identifies the UE communicates on demand
     * 
     * @return periodicCommunicationIndicator
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Possible values are - PERIODICALLY: Identifies the UE communicates periodically - ON_DEMAND: Identifies the UE communicates on demand ")
    @JsonProperty(JSON_PROPERTY_PERIODIC_COMMUNICATION_INDICATOR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPeriodicCommunicationIndicator()
    {
        return periodicCommunicationIndicator;
    }

    @JsonProperty(JSON_PROPERTY_PERIODIC_COMMUNICATION_INDICATOR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPeriodicCommunicationIndicator(String periodicCommunicationIndicator)
    {
        this.periodicCommunicationIndicator = periodicCommunicationIndicator;
    }

    public CpParameterSet communicationDurationTime(Integer communicationDurationTime)
    {

        this.communicationDurationTime = communicationDurationTime;
        return this;
    }

    /**
     * Unsigned integer identifying a period of time in units of seconds. minimum: 0
     * 
     * @return communicationDurationTime
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Unsigned integer identifying a period of time in units of seconds.")
    @JsonProperty(JSON_PROPERTY_COMMUNICATION_DURATION_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getCommunicationDurationTime()
    {
        return communicationDurationTime;
    }

    @JsonProperty(JSON_PROPERTY_COMMUNICATION_DURATION_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCommunicationDurationTime(Integer communicationDurationTime)
    {
        this.communicationDurationTime = communicationDurationTime;
    }

    public CpParameterSet periodicTime(Integer periodicTime)
    {

        this.periodicTime = periodicTime;
        return this;
    }

    /**
     * Unsigned integer identifying a period of time in units of seconds. minimum: 0
     * 
     * @return periodicTime
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Unsigned integer identifying a period of time in units of seconds.")
    @JsonProperty(JSON_PROPERTY_PERIODIC_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getPeriodicTime()
    {
        return periodicTime;
    }

    @JsonProperty(JSON_PROPERTY_PERIODIC_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPeriodicTime(Integer periodicTime)
    {
        this.periodicTime = periodicTime;
    }

    public CpParameterSet scheduledCommunicationTime(ScheduledCommunicationTime scheduledCommunicationTime)
    {

        this.scheduledCommunicationTime = scheduledCommunicationTime;
        return this;
    }

    /**
     * Get scheduledCommunicationTime
     * 
     * @return scheduledCommunicationTime
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SCHEDULED_COMMUNICATION_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public ScheduledCommunicationTime getScheduledCommunicationTime()
    {
        return scheduledCommunicationTime;
    }

    @JsonProperty(JSON_PROPERTY_SCHEDULED_COMMUNICATION_TIME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setScheduledCommunicationTime(ScheduledCommunicationTime scheduledCommunicationTime)
    {
        this.scheduledCommunicationTime = scheduledCommunicationTime;
    }

    public CpParameterSet scheduledCommunicationType(String scheduledCommunicationType)
    {

        this.scheduledCommunicationType = scheduledCommunicationType;
        return this;
    }

    /**
     * Possible values are - DOWNLINK: Downlink only. - UPLINK: Uplink only. -
     * BIDIRECTIONAL: Bi-directional.
     * 
     * @return scheduledCommunicationType
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Possible values are - DOWNLINK: Downlink only. - UPLINK: Uplink only. - BIDIRECTIONAL: Bi-directional. ")
    @JsonProperty(JSON_PROPERTY_SCHEDULED_COMMUNICATION_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getScheduledCommunicationType()
    {
        return scheduledCommunicationType;
    }

    @JsonProperty(JSON_PROPERTY_SCHEDULED_COMMUNICATION_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setScheduledCommunicationType(String scheduledCommunicationType)
    {
        this.scheduledCommunicationType = scheduledCommunicationType;
    }

    public CpParameterSet stationaryIndication(String stationaryIndication)
    {

        this.stationaryIndication = stationaryIndication;
        return this;
    }

    /**
     * Possible values are - STATIONARY: Identifies the UE is stationary - MOBILE:
     * Identifies the UE is mobile
     * 
     * @return stationaryIndication
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Possible values are - STATIONARY: Identifies the UE is stationary - MOBILE: Identifies the UE is mobile ")
    @JsonProperty(JSON_PROPERTY_STATIONARY_INDICATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getStationaryIndication()
    {
        return stationaryIndication;
    }

    @JsonProperty(JSON_PROPERTY_STATIONARY_INDICATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStationaryIndication(String stationaryIndication)
    {
        this.stationaryIndication = stationaryIndication;
    }

    public CpParameterSet batteryInds(List<String> batteryInds)
    {

        this.batteryInds = batteryInds;
        return this;
    }

    public CpParameterSet addBatteryIndsItem(String batteryIndsItem)
    {
        if (this.batteryInds == null)
        {
            this.batteryInds = new ArrayList<>();
        }
        this.batteryInds.add(batteryIndsItem);
        return this;
    }

    /**
     * Get batteryInds
     * 
     * @return batteryInds
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_BATTERY_INDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getBatteryInds()
    {
        return batteryInds;
    }

    @JsonProperty(JSON_PROPERTY_BATTERY_INDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBatteryInds(List<String> batteryInds)
    {
        this.batteryInds = batteryInds;
    }

    public CpParameterSet trafficProfile(String trafficProfile)
    {

        this.trafficProfile = trafficProfile;
        return this;
    }

    /**
     * Possible values are - SINGLE_TRANS_UL: Uplink single packet transmission. -
     * SINGLE_TRANS_DL: Downlink single packet transmission. - DUAL_TRANS_UL_FIRST:
     * Dual packet transmission, firstly uplink packet transmission with subsequent
     * downlink packet transmission. - DUAL_TRANS_DL_FIRST: Dual packet
     * transmission, firstly downlink packet transmission with subsequent uplink
     * packet transmission. - MULTI_TRANS: Multiple packet transmission.
     * 
     * @return trafficProfile
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Possible values are - SINGLE_TRANS_UL: Uplink single packet transmission. - SINGLE_TRANS_DL: Downlink single packet transmission. - DUAL_TRANS_UL_FIRST: Dual packet transmission, firstly uplink packet transmission with subsequent downlink packet transmission. - DUAL_TRANS_DL_FIRST: Dual packet transmission, firstly downlink packet transmission with subsequent uplink packet transmission. - MULTI_TRANS: Multiple packet transmission. ")
    @JsonProperty(JSON_PROPERTY_TRAFFIC_PROFILE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getTrafficProfile()
    {
        return trafficProfile;
    }

    @JsonProperty(JSON_PROPERTY_TRAFFIC_PROFILE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTrafficProfile(String trafficProfile)
    {
        this.trafficProfile = trafficProfile;
    }

    public CpParameterSet expectedUmts(List<UmtLocationArea5G> expectedUmts)
    {

        this.expectedUmts = expectedUmts;
        return this;
    }

    public CpParameterSet addExpectedUmtsItem(UmtLocationArea5G expectedUmtsItem)
    {
        if (this.expectedUmts == null)
        {
            this.expectedUmts = new ArrayList<>();
        }
        this.expectedUmts.add(expectedUmtsItem);
        return this;
    }

    /**
     * Identifies the UE&#39;s expected geographical movement. The attribute is only
     * applicable in 5G.
     * 
     * @return expectedUmts
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Identifies the UE's expected geographical movement. The attribute is only applicable in 5G.")
    @JsonProperty(JSON_PROPERTY_EXPECTED_UMTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<UmtLocationArea5G> getExpectedUmts()
    {
        return expectedUmts;
    }

    @JsonProperty(JSON_PROPERTY_EXPECTED_UMTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setExpectedUmts(List<UmtLocationArea5G> expectedUmts)
    {
        this.expectedUmts = expectedUmts;
    }

    public CpParameterSet expectedUmtDays(Integer expectedUmtDays)
    {

        this.expectedUmtDays = expectedUmtDays;
        return this;
    }

    /**
     * integer between and including 1 and 7 denoting a weekday. 1 shall indicate
     * Monday, and the subsequent weekdays shall be indicated with the next higher
     * numbers. 7 shall indicate Sunday. minimum: 1 maximum: 7
     * 
     * @return expectedUmtDays
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "integer between and including 1 and 7 denoting a weekday. 1 shall indicate Monday, and the subsequent weekdays shall be indicated with the next higher numbers. 7 shall indicate Sunday.")
    @JsonProperty(JSON_PROPERTY_EXPECTED_UMT_DAYS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getExpectedUmtDays()
    {
        return expectedUmtDays;
    }

    @JsonProperty(JSON_PROPERTY_EXPECTED_UMT_DAYS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setExpectedUmtDays(Integer expectedUmtDays)
    {
        this.expectedUmtDays = expectedUmtDays;
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
        CpParameterSet cpParameterSet = (CpParameterSet) o;
        return Objects.equals(this.setId, cpParameterSet.setId) && Objects.equals(this.self, cpParameterSet.self)
               && Objects.equals(this.validityTime, cpParameterSet.validityTime)
               && Objects.equals(this.periodicCommunicationIndicator, cpParameterSet.periodicCommunicationIndicator)
               && Objects.equals(this.communicationDurationTime, cpParameterSet.communicationDurationTime)
               && Objects.equals(this.periodicTime, cpParameterSet.periodicTime)
               && Objects.equals(this.scheduledCommunicationTime, cpParameterSet.scheduledCommunicationTime)
               && Objects.equals(this.scheduledCommunicationType, cpParameterSet.scheduledCommunicationType)
               && Objects.equals(this.stationaryIndication, cpParameterSet.stationaryIndication) && Objects.equals(this.batteryInds, cpParameterSet.batteryInds)
               && Objects.equals(this.trafficProfile, cpParameterSet.trafficProfile) && Objects.equals(this.expectedUmts, cpParameterSet.expectedUmts)
               && Objects.equals(this.expectedUmtDays, cpParameterSet.expectedUmtDays);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(setId,
                            self,
                            validityTime,
                            periodicCommunicationIndicator,
                            communicationDurationTime,
                            periodicTime,
                            scheduledCommunicationTime,
                            scheduledCommunicationType,
                            stationaryIndication,
                            batteryInds,
                            trafficProfile,
                            expectedUmts,
                            expectedUmtDays);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class CpParameterSet {\n");
        sb.append("    setId: ").append(toIndentedString(setId)).append("\n");
        sb.append("    self: ").append(toIndentedString(self)).append("\n");
        sb.append("    validityTime: ").append(toIndentedString(validityTime)).append("\n");
        sb.append("    periodicCommunicationIndicator: ").append(toIndentedString(periodicCommunicationIndicator)).append("\n");
        sb.append("    communicationDurationTime: ").append(toIndentedString(communicationDurationTime)).append("\n");
        sb.append("    periodicTime: ").append(toIndentedString(periodicTime)).append("\n");
        sb.append("    scheduledCommunicationTime: ").append(toIndentedString(scheduledCommunicationTime)).append("\n");
        sb.append("    scheduledCommunicationType: ").append(toIndentedString(scheduledCommunicationType)).append("\n");
        sb.append("    stationaryIndication: ").append(toIndentedString(stationaryIndication)).append("\n");
        sb.append("    batteryInds: ").append(toIndentedString(batteryInds)).append("\n");
        sb.append("    trafficProfile: ").append(toIndentedString(trafficProfile)).append("\n");
        sb.append("    expectedUmts: ").append(toIndentedString(expectedUmts)).append("\n");
        sb.append("    expectedUmtDays: ").append(toIndentedString(expectedUmtDays)).append("\n");
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
