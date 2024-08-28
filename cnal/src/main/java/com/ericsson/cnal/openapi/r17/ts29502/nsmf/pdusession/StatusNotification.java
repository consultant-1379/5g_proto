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
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Data within Notify Status Request
 */
@ApiModel(description = "Data within Notify Status Request")
@JsonPropertyOrder({ StatusNotification.JSON_PROPERTY_STATUS_INFO,
                     StatusNotification.JSON_PROPERTY_SMALL_DATA_RATE_STATUS,
                     StatusNotification.JSON_PROPERTY_APN_RATE_STATUS,
                     StatusNotification.JSON_PROPERTY_TARGET_DNAI_INFO,
                     StatusNotification.JSON_PROPERTY_OLD_PDU_SESSION_REF,
                     StatusNotification.JSON_PROPERTY_NEW_SMF_ID,
                     StatusNotification.JSON_PROPERTY_EPS_PDN_CNX_INFO,
                     StatusNotification.JSON_PROPERTY_INTER_PLMN_API_ROOT,
                     StatusNotification.JSON_PROPERTY_INTRA_PLMN_API_ROOT })
public class StatusNotification
{
    public static final String JSON_PROPERTY_STATUS_INFO = "statusInfo";
    private StatusInfo statusInfo;

    public static final String JSON_PROPERTY_SMALL_DATA_RATE_STATUS = "smallDataRateStatus";
    private SmallDataRateStatus smallDataRateStatus;

    public static final String JSON_PROPERTY_APN_RATE_STATUS = "apnRateStatus";
    private ApnRateStatus apnRateStatus;

    public static final String JSON_PROPERTY_TARGET_DNAI_INFO = "targetDnaiInfo";
    private TargetDnaiInfo targetDnaiInfo;

    public static final String JSON_PROPERTY_OLD_PDU_SESSION_REF = "oldPduSessionRef";
    private String oldPduSessionRef;

    public static final String JSON_PROPERTY_NEW_SMF_ID = "newSmfId";
    private UUID newSmfId;

    public static final String JSON_PROPERTY_EPS_PDN_CNX_INFO = "epsPdnCnxInfo";
    private EpsPdnCnxInfo epsPdnCnxInfo;

    public static final String JSON_PROPERTY_INTER_PLMN_API_ROOT = "interPlmnApiRoot";
    private String interPlmnApiRoot;

    public static final String JSON_PROPERTY_INTRA_PLMN_API_ROOT = "intraPlmnApiRoot";
    private String intraPlmnApiRoot;

    public StatusNotification()
    {
    }

    public StatusNotification statusInfo(StatusInfo statusInfo)
    {

        this.statusInfo = statusInfo;
        return this;
    }

    /**
     * Get statusInfo
     * 
     * @return statusInfo
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_STATUS_INFO)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public StatusInfo getStatusInfo()
    {
        return statusInfo;
    }

    @JsonProperty(JSON_PROPERTY_STATUS_INFO)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setStatusInfo(StatusInfo statusInfo)
    {
        this.statusInfo = statusInfo;
    }

    public StatusNotification smallDataRateStatus(SmallDataRateStatus smallDataRateStatus)
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

    public StatusNotification apnRateStatus(ApnRateStatus apnRateStatus)
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

    public StatusNotification targetDnaiInfo(TargetDnaiInfo targetDnaiInfo)
    {

        this.targetDnaiInfo = targetDnaiInfo;
        return this;
    }

    /**
     * Get targetDnaiInfo
     * 
     * @return targetDnaiInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_TARGET_DNAI_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public TargetDnaiInfo getTargetDnaiInfo()
    {
        return targetDnaiInfo;
    }

    @JsonProperty(JSON_PROPERTY_TARGET_DNAI_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTargetDnaiInfo(TargetDnaiInfo targetDnaiInfo)
    {
        this.targetDnaiInfo = targetDnaiInfo;
    }

    public StatusNotification oldPduSessionRef(String oldPduSessionRef)
    {

        this.oldPduSessionRef = oldPduSessionRef;
        return this;
    }

    /**
     * String providing an URI formatted according to RFC 3986.
     * 
     * @return oldPduSessionRef
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String providing an URI formatted according to RFC 3986.")
    @JsonProperty(JSON_PROPERTY_OLD_PDU_SESSION_REF)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getOldPduSessionRef()
    {
        return oldPduSessionRef;
    }

    @JsonProperty(JSON_PROPERTY_OLD_PDU_SESSION_REF)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setOldPduSessionRef(String oldPduSessionRef)
    {
        this.oldPduSessionRef = oldPduSessionRef;
    }

    public StatusNotification newSmfId(UUID newSmfId)
    {

        this.newSmfId = newSmfId;
        return this;
    }

    /**
     * String uniquely identifying a NF instance. The format of the NF Instance ID
     * shall be a Universally Unique Identifier (UUID) version 4, as described in
     * IETF RFC 4122.
     * 
     * @return newSmfId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String uniquely identifying a NF instance. The format of the NF Instance ID shall be a  Universally Unique Identifier (UUID) version 4, as described in IETF RFC 4122.  ")
    @JsonProperty(JSON_PROPERTY_NEW_SMF_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UUID getNewSmfId()
    {
        return newSmfId;
    }

    @JsonProperty(JSON_PROPERTY_NEW_SMF_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNewSmfId(UUID newSmfId)
    {
        this.newSmfId = newSmfId;
    }

    public StatusNotification epsPdnCnxInfo(EpsPdnCnxInfo epsPdnCnxInfo)
    {

        this.epsPdnCnxInfo = epsPdnCnxInfo;
        return this;
    }

    /**
     * Get epsPdnCnxInfo
     * 
     * @return epsPdnCnxInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_EPS_PDN_CNX_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public EpsPdnCnxInfo getEpsPdnCnxInfo()
    {
        return epsPdnCnxInfo;
    }

    @JsonProperty(JSON_PROPERTY_EPS_PDN_CNX_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEpsPdnCnxInfo(EpsPdnCnxInfo epsPdnCnxInfo)
    {
        this.epsPdnCnxInfo = epsPdnCnxInfo;
    }

    public StatusNotification interPlmnApiRoot(String interPlmnApiRoot)
    {

        this.interPlmnApiRoot = interPlmnApiRoot;
        return this;
    }

    /**
     * String providing an URI formatted according to RFC 3986.
     * 
     * @return interPlmnApiRoot
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String providing an URI formatted according to RFC 3986.")
    @JsonProperty(JSON_PROPERTY_INTER_PLMN_API_ROOT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getInterPlmnApiRoot()
    {
        return interPlmnApiRoot;
    }

    @JsonProperty(JSON_PROPERTY_INTER_PLMN_API_ROOT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setInterPlmnApiRoot(String interPlmnApiRoot)
    {
        this.interPlmnApiRoot = interPlmnApiRoot;
    }

    public StatusNotification intraPlmnApiRoot(String intraPlmnApiRoot)
    {

        this.intraPlmnApiRoot = intraPlmnApiRoot;
        return this;
    }

    /**
     * String providing an URI formatted according to RFC 3986.
     * 
     * @return intraPlmnApiRoot
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String providing an URI formatted according to RFC 3986.")
    @JsonProperty(JSON_PROPERTY_INTRA_PLMN_API_ROOT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getIntraPlmnApiRoot()
    {
        return intraPlmnApiRoot;
    }

    @JsonProperty(JSON_PROPERTY_INTRA_PLMN_API_ROOT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIntraPlmnApiRoot(String intraPlmnApiRoot)
    {
        this.intraPlmnApiRoot = intraPlmnApiRoot;
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
        StatusNotification statusNotification = (StatusNotification) o;
        return Objects.equals(this.statusInfo, statusNotification.statusInfo)
               && Objects.equals(this.smallDataRateStatus, statusNotification.smallDataRateStatus)
               && Objects.equals(this.apnRateStatus, statusNotification.apnRateStatus) && Objects.equals(this.targetDnaiInfo, statusNotification.targetDnaiInfo)
               && Objects.equals(this.oldPduSessionRef, statusNotification.oldPduSessionRef) && Objects.equals(this.newSmfId, statusNotification.newSmfId)
               && Objects.equals(this.epsPdnCnxInfo, statusNotification.epsPdnCnxInfo)
               && Objects.equals(this.interPlmnApiRoot, statusNotification.interPlmnApiRoot)
               && Objects.equals(this.intraPlmnApiRoot, statusNotification.intraPlmnApiRoot);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(statusInfo,
                            smallDataRateStatus,
                            apnRateStatus,
                            targetDnaiInfo,
                            oldPduSessionRef,
                            newSmfId,
                            epsPdnCnxInfo,
                            interPlmnApiRoot,
                            intraPlmnApiRoot);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class StatusNotification {\n");
        sb.append("    statusInfo: ").append(toIndentedString(statusInfo)).append("\n");
        sb.append("    smallDataRateStatus: ").append(toIndentedString(smallDataRateStatus)).append("\n");
        sb.append("    apnRateStatus: ").append(toIndentedString(apnRateStatus)).append("\n");
        sb.append("    targetDnaiInfo: ").append(toIndentedString(targetDnaiInfo)).append("\n");
        sb.append("    oldPduSessionRef: ").append(toIndentedString(oldPduSessionRef)).append("\n");
        sb.append("    newSmfId: ").append(toIndentedString(newSmfId)).append("\n");
        sb.append("    epsPdnCnxInfo: ").append(toIndentedString(epsPdnCnxInfo)).append("\n");
        sb.append("    interPlmnApiRoot: ").append(toIndentedString(interPlmnApiRoot)).append("\n");
        sb.append("    intraPlmnApiRoot: ").append(toIndentedString(intraPlmnApiRoot)).append("\n");
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
