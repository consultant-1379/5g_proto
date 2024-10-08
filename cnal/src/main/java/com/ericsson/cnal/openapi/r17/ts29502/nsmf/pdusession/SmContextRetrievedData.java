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
import com.ericsson.cnal.openapi.r17.ts29571.commondata.SmallDataRateStatus;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.ApnRateStatus;
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
 * Data within Retrieve SM Context Response
 */
@ApiModel(description = "Data within Retrieve SM Context Response")
@JsonPropertyOrder({ SmContextRetrievedData.JSON_PROPERTY_UE_EPS_PDN_CONNECTION,
                     SmContextRetrievedData.JSON_PROPERTY_SM_CONTEXT,
                     SmContextRetrievedData.JSON_PROPERTY_SMALL_DATA_RATE_STATUS,
                     SmContextRetrievedData.JSON_PROPERTY_APN_RATE_STATUS,
                     SmContextRetrievedData.JSON_PROPERTY_DL_DATA_WAITING_IND,
                     SmContextRetrievedData.JSON_PROPERTY_AF_COORDINATION_INFO })
public class SmContextRetrievedData
{
    public static final String JSON_PROPERTY_UE_EPS_PDN_CONNECTION = "ueEpsPdnConnection";
    private String ueEpsPdnConnection;

    public static final String JSON_PROPERTY_SM_CONTEXT = "smContext";
    private SmContext smContext;

    public static final String JSON_PROPERTY_SMALL_DATA_RATE_STATUS = "smallDataRateStatus";
    private SmallDataRateStatus smallDataRateStatus;

    public static final String JSON_PROPERTY_APN_RATE_STATUS = "apnRateStatus";
    private ApnRateStatus apnRateStatus;

    public static final String JSON_PROPERTY_DL_DATA_WAITING_IND = "dlDataWaitingInd";
    private Boolean dlDataWaitingInd = false;

    public static final String JSON_PROPERTY_AF_COORDINATION_INFO = "afCoordinationInfo";
    private AfCoordinationInfo afCoordinationInfo;

    public SmContextRetrievedData()
    {
    }

    public SmContextRetrievedData ueEpsPdnConnection(String ueEpsPdnConnection)
    {

        this.ueEpsPdnConnection = ueEpsPdnConnection;
        return this;
    }

    /**
     * UE EPS PDN Connection container from SMF to AMF
     * 
     * @return ueEpsPdnConnection
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "UE EPS PDN Connection container from SMF to AMF")
    @JsonProperty(JSON_PROPERTY_UE_EPS_PDN_CONNECTION)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getUeEpsPdnConnection()
    {
        return ueEpsPdnConnection;
    }

    @JsonProperty(JSON_PROPERTY_UE_EPS_PDN_CONNECTION)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setUeEpsPdnConnection(String ueEpsPdnConnection)
    {
        this.ueEpsPdnConnection = ueEpsPdnConnection;
    }

    public SmContextRetrievedData smContext(SmContext smContext)
    {

        this.smContext = smContext;
        return this;
    }

    /**
     * Get smContext
     * 
     * @return smContext
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SM_CONTEXT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public SmContext getSmContext()
    {
        return smContext;
    }

    @JsonProperty(JSON_PROPERTY_SM_CONTEXT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSmContext(SmContext smContext)
    {
        this.smContext = smContext;
    }

    public SmContextRetrievedData smallDataRateStatus(SmallDataRateStatus smallDataRateStatus)
    {

        this.smallDataRateStatus = smallDataRateStatus;
        return this;
    }

    /**
     * Get smallDataRateStatus
     * 
     * @return smallDataRateStatus
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SMALL_DATA_RATE_STATUS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public SmallDataRateStatus getSmallDataRateStatus()
    {
        return smallDataRateStatus;
    }

    @JsonProperty(JSON_PROPERTY_SMALL_DATA_RATE_STATUS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSmallDataRateStatus(SmallDataRateStatus smallDataRateStatus)
    {
        this.smallDataRateStatus = smallDataRateStatus;
    }

    public SmContextRetrievedData apnRateStatus(ApnRateStatus apnRateStatus)
    {

        this.apnRateStatus = apnRateStatus;
        return this;
    }

    /**
     * Get apnRateStatus
     * 
     * @return apnRateStatus
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_APN_RATE_STATUS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public ApnRateStatus getApnRateStatus()
    {
        return apnRateStatus;
    }

    @JsonProperty(JSON_PROPERTY_APN_RATE_STATUS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setApnRateStatus(ApnRateStatus apnRateStatus)
    {
        this.apnRateStatus = apnRateStatus;
    }

    public SmContextRetrievedData dlDataWaitingInd(Boolean dlDataWaitingInd)
    {

        this.dlDataWaitingInd = dlDataWaitingInd;
        return this;
    }

    /**
     * Get dlDataWaitingInd
     * 
     * @return dlDataWaitingInd
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_DL_DATA_WAITING_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getDlDataWaitingInd()
    {
        return dlDataWaitingInd;
    }

    @JsonProperty(JSON_PROPERTY_DL_DATA_WAITING_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDlDataWaitingInd(Boolean dlDataWaitingInd)
    {
        this.dlDataWaitingInd = dlDataWaitingInd;
    }

    public SmContextRetrievedData afCoordinationInfo(AfCoordinationInfo afCoordinationInfo)
    {

        this.afCoordinationInfo = afCoordinationInfo;
        return this;
    }

    /**
     * Get afCoordinationInfo
     * 
     * @return afCoordinationInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_AF_COORDINATION_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public AfCoordinationInfo getAfCoordinationInfo()
    {
        return afCoordinationInfo;
    }

    @JsonProperty(JSON_PROPERTY_AF_COORDINATION_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAfCoordinationInfo(AfCoordinationInfo afCoordinationInfo)
    {
        this.afCoordinationInfo = afCoordinationInfo;
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
        SmContextRetrievedData smContextRetrievedData = (SmContextRetrievedData) o;
        return Objects.equals(this.ueEpsPdnConnection, smContextRetrievedData.ueEpsPdnConnection)
               && Objects.equals(this.smContext, smContextRetrievedData.smContext)
               && Objects.equals(this.smallDataRateStatus, smContextRetrievedData.smallDataRateStatus)
               && Objects.equals(this.apnRateStatus, smContextRetrievedData.apnRateStatus)
               && Objects.equals(this.dlDataWaitingInd, smContextRetrievedData.dlDataWaitingInd)
               && Objects.equals(this.afCoordinationInfo, smContextRetrievedData.afCoordinationInfo);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(ueEpsPdnConnection, smContext, smallDataRateStatus, apnRateStatus, dlDataWaitingInd, afCoordinationInfo);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class SmContextRetrievedData {\n");
        sb.append("    ueEpsPdnConnection: ").append(toIndentedString(ueEpsPdnConnection)).append("\n");
        sb.append("    smContext: ").append(toIndentedString(smContext)).append("\n");
        sb.append("    smallDataRateStatus: ").append(toIndentedString(smallDataRateStatus)).append("\n");
        sb.append("    apnRateStatus: ").append(toIndentedString(apnRateStatus)).append("\n");
        sb.append("    dlDataWaitingInd: ").append(toIndentedString(dlDataWaitingInd)).append("\n");
        sb.append("    afCoordinationInfo: ").append(toIndentedString(afCoordinationInfo)).append("\n");
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
