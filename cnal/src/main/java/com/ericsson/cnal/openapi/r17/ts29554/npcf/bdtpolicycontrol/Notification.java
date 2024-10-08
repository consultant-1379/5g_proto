/*
 * Npcf_BDTPolicyControl Service API
 * PCF BDT Policy Control Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29554.npcf.bdtpolicycontrol;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29122.commondata.TimeWindow;
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
 * Describes a BDT notification.
 */
@ApiModel(description = "Describes a BDT notification.")
@JsonPropertyOrder({ Notification.JSON_PROPERTY_BDT_REF_ID,
                     Notification.JSON_PROPERTY_CAND_POLICIES,
                     Notification.JSON_PROPERTY_NW_AREA_INFO,
                     Notification.JSON_PROPERTY_TIME_WINDOW })
public class Notification
{
    public static final String JSON_PROPERTY_BDT_REF_ID = "bdtRefId";
    private String bdtRefId;

    public static final String JSON_PROPERTY_CAND_POLICIES = "candPolicies";
    private List<TransferPolicy> candPolicies = null;

    public static final String JSON_PROPERTY_NW_AREA_INFO = "nwAreaInfo";
    private NetworkAreaInfo nwAreaInfo;

    public static final String JSON_PROPERTY_TIME_WINDOW = "timeWindow";
    private TimeWindow timeWindow;

    public Notification()
    {
    }

    public Notification bdtRefId(String bdtRefId)
    {

        this.bdtRefId = bdtRefId;
        return this;
    }

    /**
     * string identifying a BDT Reference ID as defined in clause 5.3.3 of 3GPP TS
     * 29.154.
     * 
     * @return bdtRefId
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "string identifying a BDT Reference ID as defined in clause 5.3.3 of 3GPP TS 29.154.")
    @JsonProperty(JSON_PROPERTY_BDT_REF_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getBdtRefId()
    {
        return bdtRefId;
    }

    @JsonProperty(JSON_PROPERTY_BDT_REF_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setBdtRefId(String bdtRefId)
    {
        this.bdtRefId = bdtRefId;
    }

    public Notification candPolicies(List<TransferPolicy> candPolicies)
    {

        this.candPolicies = candPolicies;
        return this;
    }

    public Notification addCandPoliciesItem(TransferPolicy candPoliciesItem)
    {
        if (this.candPolicies == null)
        {
            this.candPolicies = new ArrayList<>();
        }
        this.candPolicies.add(candPoliciesItem);
        return this;
    }

    /**
     * Contains a list of the candidate transfer policies from which the AF may
     * select a new transfer policy due to a network performance is below the
     * criteria set by the operator.
     * 
     * @return candPolicies
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Contains a list of the candidate transfer policies from which the AF may select a new transfer policy due to a network performance is below the criteria set by the operator. ")
    @JsonProperty(JSON_PROPERTY_CAND_POLICIES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<TransferPolicy> getCandPolicies()
    {
        return candPolicies;
    }

    @JsonProperty(JSON_PROPERTY_CAND_POLICIES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCandPolicies(List<TransferPolicy> candPolicies)
    {
        this.candPolicies = candPolicies;
    }

    public Notification nwAreaInfo(NetworkAreaInfo nwAreaInfo)
    {

        this.nwAreaInfo = nwAreaInfo;
        return this;
    }

    /**
     * Get nwAreaInfo
     * 
     * @return nwAreaInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_NW_AREA_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public NetworkAreaInfo getNwAreaInfo()
    {
        return nwAreaInfo;
    }

    @JsonProperty(JSON_PROPERTY_NW_AREA_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNwAreaInfo(NetworkAreaInfo nwAreaInfo)
    {
        this.nwAreaInfo = nwAreaInfo;
    }

    public Notification timeWindow(TimeWindow timeWindow)
    {

        this.timeWindow = timeWindow;
        return this;
    }

    /**
     * Get timeWindow
     * 
     * @return timeWindow
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_TIME_WINDOW)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public TimeWindow getTimeWindow()
    {
        return timeWindow;
    }

    @JsonProperty(JSON_PROPERTY_TIME_WINDOW)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTimeWindow(TimeWindow timeWindow)
    {
        this.timeWindow = timeWindow;
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
        Notification notification = (Notification) o;
        return Objects.equals(this.bdtRefId, notification.bdtRefId) && Objects.equals(this.candPolicies, notification.candPolicies)
               && Objects.equals(this.nwAreaInfo, notification.nwAreaInfo) && Objects.equals(this.timeWindow, notification.timeWindow);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(bdtRefId, candPolicies, nwAreaInfo, timeWindow);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class Notification {\n");
        sb.append("    bdtRefId: ").append(toIndentedString(bdtRefId)).append("\n");
        sb.append("    candPolicies: ").append(toIndentedString(candPolicies)).append("\n");
        sb.append("    nwAreaInfo: ").append(toIndentedString(nwAreaInfo)).append("\n");
        sb.append("    timeWindow: ").append(toIndentedString(timeWindow)).append("\n");
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
