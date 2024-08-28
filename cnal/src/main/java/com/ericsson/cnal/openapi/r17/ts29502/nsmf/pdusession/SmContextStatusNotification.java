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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Data within Notify SM Context Status Request
 */
@ApiModel(description = "Data within Notify SM Context Status Request")
@JsonPropertyOrder({ SmContextStatusNotification.JSON_PROPERTY_STATUS_INFO,
                     SmContextStatusNotification.JSON_PROPERTY_SMALL_DATA_RATE_STATUS,
                     SmContextStatusNotification.JSON_PROPERTY_APN_RATE_STATUS,
                     SmContextStatusNotification.JSON_PROPERTY_DDN_FAILURE_STATUS,
                     SmContextStatusNotification.JSON_PROPERTY_NOTIFY_CORRELATION_IDS_FORDDN_FAILURE,
                     SmContextStatusNotification.JSON_PROPERTY_NEW_INTERMEDIATE_SMF_ID,
                     SmContextStatusNotification.JSON_PROPERTY_NEW_SMF_ID,
                     SmContextStatusNotification.JSON_PROPERTY_NEW_SMF_SET_ID,
                     SmContextStatusNotification.JSON_PROPERTY_OLD_SMF_ID,
                     SmContextStatusNotification.JSON_PROPERTY_OLD_SM_CONTEXT_REF,
                     SmContextStatusNotification.JSON_PROPERTY_ALT_ANCHOR_SMF_URI,
                     SmContextStatusNotification.JSON_PROPERTY_ALT_ANCHOR_SMF_ID,
                     SmContextStatusNotification.JSON_PROPERTY_TARGET_DNAI_INFO,
                     SmContextStatusNotification.JSON_PROPERTY_OLD_PDU_SESSION_REF,
                     SmContextStatusNotification.JSON_PROPERTY_INTER_PLMN_API_ROOT })
public class SmContextStatusNotification
{
    public static final String JSON_PROPERTY_STATUS_INFO = "statusInfo";
    private StatusInfo statusInfo;

    public static final String JSON_PROPERTY_SMALL_DATA_RATE_STATUS = "smallDataRateStatus";
    private SmallDataRateStatus smallDataRateStatus;

    public static final String JSON_PROPERTY_APN_RATE_STATUS = "apnRateStatus";
    private ApnRateStatus apnRateStatus;

    public static final String JSON_PROPERTY_DDN_FAILURE_STATUS = "ddnFailureStatus";
    private Boolean ddnFailureStatus = false;

    public static final String JSON_PROPERTY_NOTIFY_CORRELATION_IDS_FORDDN_FAILURE = "notifyCorrelationIdsForddnFailure";
    private List<String> notifyCorrelationIdsForddnFailure = null;

    public static final String JSON_PROPERTY_NEW_INTERMEDIATE_SMF_ID = "newIntermediateSmfId";
    private UUID newIntermediateSmfId;

    public static final String JSON_PROPERTY_NEW_SMF_ID = "newSmfId";
    private UUID newSmfId;

    public static final String JSON_PROPERTY_NEW_SMF_SET_ID = "newSmfSetId";
    private String newSmfSetId;

    public static final String JSON_PROPERTY_OLD_SMF_ID = "oldSmfId";
    private UUID oldSmfId;

    public static final String JSON_PROPERTY_OLD_SM_CONTEXT_REF = "oldSmContextRef";
    private String oldSmContextRef;

    public static final String JSON_PROPERTY_ALT_ANCHOR_SMF_URI = "altAnchorSmfUri";
    private String altAnchorSmfUri;

    public static final String JSON_PROPERTY_ALT_ANCHOR_SMF_ID = "altAnchorSmfId";
    private UUID altAnchorSmfId;

    public static final String JSON_PROPERTY_TARGET_DNAI_INFO = "targetDnaiInfo";
    private TargetDnaiInfo targetDnaiInfo;

    public static final String JSON_PROPERTY_OLD_PDU_SESSION_REF = "oldPduSessionRef";
    private String oldPduSessionRef;

    public static final String JSON_PROPERTY_INTER_PLMN_API_ROOT = "interPlmnApiRoot";
    private String interPlmnApiRoot;

    public SmContextStatusNotification()
    {
    }

    public SmContextStatusNotification statusInfo(StatusInfo statusInfo)
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

    public SmContextStatusNotification smallDataRateStatus(SmallDataRateStatus smallDataRateStatus)
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

    public SmContextStatusNotification apnRateStatus(ApnRateStatus apnRateStatus)
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

    public SmContextStatusNotification ddnFailureStatus(Boolean ddnFailureStatus)
    {

        this.ddnFailureStatus = ddnFailureStatus;
        return this;
    }

    /**
     * Get ddnFailureStatus
     * 
     * @return ddnFailureStatus
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_DDN_FAILURE_STATUS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getDdnFailureStatus()
    {
        return ddnFailureStatus;
    }

    @JsonProperty(JSON_PROPERTY_DDN_FAILURE_STATUS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDdnFailureStatus(Boolean ddnFailureStatus)
    {
        this.ddnFailureStatus = ddnFailureStatus;
    }

    public SmContextStatusNotification notifyCorrelationIdsForddnFailure(List<String> notifyCorrelationIdsForddnFailure)
    {

        this.notifyCorrelationIdsForddnFailure = notifyCorrelationIdsForddnFailure;
        return this;
    }

    public SmContextStatusNotification addNotifyCorrelationIdsForddnFailureItem(String notifyCorrelationIdsForddnFailureItem)
    {
        if (this.notifyCorrelationIdsForddnFailure == null)
        {
            this.notifyCorrelationIdsForddnFailure = new ArrayList<>();
        }
        this.notifyCorrelationIdsForddnFailure.add(notifyCorrelationIdsForddnFailureItem);
        return this;
    }

    /**
     * Get notifyCorrelationIdsForddnFailure
     * 
     * @return notifyCorrelationIdsForddnFailure
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_NOTIFY_CORRELATION_IDS_FORDDN_FAILURE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getNotifyCorrelationIdsForddnFailure()
    {
        return notifyCorrelationIdsForddnFailure;
    }

    @JsonProperty(JSON_PROPERTY_NOTIFY_CORRELATION_IDS_FORDDN_FAILURE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNotifyCorrelationIdsForddnFailure(List<String> notifyCorrelationIdsForddnFailure)
    {
        this.notifyCorrelationIdsForddnFailure = notifyCorrelationIdsForddnFailure;
    }

    public SmContextStatusNotification newIntermediateSmfId(UUID newIntermediateSmfId)
    {

        this.newIntermediateSmfId = newIntermediateSmfId;
        return this;
    }

    /**
     * String uniquely identifying a NF instance. The format of the NF Instance ID
     * shall be a Universally Unique Identifier (UUID) version 4, as described in
     * IETF RFC 4122.
     * 
     * @return newIntermediateSmfId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String uniquely identifying a NF instance. The format of the NF Instance ID shall be a  Universally Unique Identifier (UUID) version 4, as described in IETF RFC 4122.  ")
    @JsonProperty(JSON_PROPERTY_NEW_INTERMEDIATE_SMF_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UUID getNewIntermediateSmfId()
    {
        return newIntermediateSmfId;
    }

    @JsonProperty(JSON_PROPERTY_NEW_INTERMEDIATE_SMF_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNewIntermediateSmfId(UUID newIntermediateSmfId)
    {
        this.newIntermediateSmfId = newIntermediateSmfId;
    }

    public SmContextStatusNotification newSmfId(UUID newSmfId)
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

    public SmContextStatusNotification newSmfSetId(String newSmfSetId)
    {

        this.newSmfSetId = newSmfSetId;
        return this;
    }

    /**
     * NF Set Identifier (see clause 28.12 of 3GPP TS 23.003), formatted as the
     * following string \&quot;set&lt;Set
     * ID&gt;.&lt;nftype&gt;set.5gc.mnc&lt;MNC&gt;.mcc&lt;MCC&gt;\&quot;, or
     * \&quot;set&lt;SetID&gt;.&lt;NFType&gt;set.5gc.nid&lt;NID&gt;.mnc&lt;MNC&gt;.mcc&lt;MCC&gt;\&quot;
     * with &lt;MCC&gt; encoded as defined in clause 5.4.2 (\&quot;Mcc\&quot; data
     * type definition) &lt;MNC&gt; encoding the Mobile Network Code part of the
     * PLMN, comprising 3 digits. If there are only 2 significant digits in the MNC,
     * one \&quot;0\&quot; digit shall be inserted at the left side to fill the 3
     * digits coding of MNC. Pattern: &#39;^[0-9]{3}$&#39; &lt;NFType&gt; encoded as
     * a value defined in Table 6.1.6.3.3-1 of 3GPP TS 29.510 but with lower case
     * characters &lt;Set ID&gt; encoded as a string of characters consisting of
     * alphabetic characters (A-Z and a-z), digits (0-9) and/or the hyphen (-) and
     * that shall end with either an alphabetic character or a digit.
     * 
     * @return newSmfSetId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "NF Set Identifier (see clause 28.12 of 3GPP TS 23.003), formatted as the following string \"set<Set ID>.<nftype>set.5gc.mnc<MNC>.mcc<MCC>\", or  \"set<SetID>.<NFType>set.5gc.nid<NID>.mnc<MNC>.mcc<MCC>\" with  <MCC> encoded as defined in clause 5.4.2 (\"Mcc\" data type definition)  <MNC> encoding the Mobile Network Code part of the PLMN, comprising 3 digits.    If there are only 2 significant digits in the MNC, one \"0\" digit shall be inserted    at the left side to fill the 3 digits coding of MNC.  Pattern: '^[0-9]{3}$' <NFType> encoded as a value defined in Table 6.1.6.3.3-1 of 3GPP TS 29.510 but    with lower case characters <Set ID> encoded as a string of characters consisting of    alphabetic characters (A-Z and a-z), digits (0-9) and/or the hyphen (-) and that    shall end with either an alphabetic character or a digit.  ")
    @JsonProperty(JSON_PROPERTY_NEW_SMF_SET_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getNewSmfSetId()
    {
        return newSmfSetId;
    }

    @JsonProperty(JSON_PROPERTY_NEW_SMF_SET_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNewSmfSetId(String newSmfSetId)
    {
        this.newSmfSetId = newSmfSetId;
    }

    public SmContextStatusNotification oldSmfId(UUID oldSmfId)
    {

        this.oldSmfId = oldSmfId;
        return this;
    }

    /**
     * String uniquely identifying a NF instance. The format of the NF Instance ID
     * shall be a Universally Unique Identifier (UUID) version 4, as described in
     * IETF RFC 4122.
     * 
     * @return oldSmfId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String uniquely identifying a NF instance. The format of the NF Instance ID shall be a  Universally Unique Identifier (UUID) version 4, as described in IETF RFC 4122.  ")
    @JsonProperty(JSON_PROPERTY_OLD_SMF_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UUID getOldSmfId()
    {
        return oldSmfId;
    }

    @JsonProperty(JSON_PROPERTY_OLD_SMF_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setOldSmfId(UUID oldSmfId)
    {
        this.oldSmfId = oldSmfId;
    }

    public SmContextStatusNotification oldSmContextRef(String oldSmContextRef)
    {

        this.oldSmContextRef = oldSmContextRef;
        return this;
    }

    /**
     * String providing an URI formatted according to RFC 3986.
     * 
     * @return oldSmContextRef
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String providing an URI formatted according to RFC 3986.")
    @JsonProperty(JSON_PROPERTY_OLD_SM_CONTEXT_REF)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getOldSmContextRef()
    {
        return oldSmContextRef;
    }

    @JsonProperty(JSON_PROPERTY_OLD_SM_CONTEXT_REF)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setOldSmContextRef(String oldSmContextRef)
    {
        this.oldSmContextRef = oldSmContextRef;
    }

    public SmContextStatusNotification altAnchorSmfUri(String altAnchorSmfUri)
    {

        this.altAnchorSmfUri = altAnchorSmfUri;
        return this;
    }

    /**
     * String providing an URI formatted according to RFC 3986.
     * 
     * @return altAnchorSmfUri
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String providing an URI formatted according to RFC 3986.")
    @JsonProperty(JSON_PROPERTY_ALT_ANCHOR_SMF_URI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getAltAnchorSmfUri()
    {
        return altAnchorSmfUri;
    }

    @JsonProperty(JSON_PROPERTY_ALT_ANCHOR_SMF_URI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAltAnchorSmfUri(String altAnchorSmfUri)
    {
        this.altAnchorSmfUri = altAnchorSmfUri;
    }

    public SmContextStatusNotification altAnchorSmfId(UUID altAnchorSmfId)
    {

        this.altAnchorSmfId = altAnchorSmfId;
        return this;
    }

    /**
     * String uniquely identifying a NF instance. The format of the NF Instance ID
     * shall be a Universally Unique Identifier (UUID) version 4, as described in
     * IETF RFC 4122.
     * 
     * @return altAnchorSmfId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String uniquely identifying a NF instance. The format of the NF Instance ID shall be a  Universally Unique Identifier (UUID) version 4, as described in IETF RFC 4122.  ")
    @JsonProperty(JSON_PROPERTY_ALT_ANCHOR_SMF_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UUID getAltAnchorSmfId()
    {
        return altAnchorSmfId;
    }

    @JsonProperty(JSON_PROPERTY_ALT_ANCHOR_SMF_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAltAnchorSmfId(UUID altAnchorSmfId)
    {
        this.altAnchorSmfId = altAnchorSmfId;
    }

    public SmContextStatusNotification targetDnaiInfo(TargetDnaiInfo targetDnaiInfo)
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

    public SmContextStatusNotification oldPduSessionRef(String oldPduSessionRef)
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

    public SmContextStatusNotification interPlmnApiRoot(String interPlmnApiRoot)
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
        SmContextStatusNotification smContextStatusNotification = (SmContextStatusNotification) o;
        return Objects.equals(this.statusInfo, smContextStatusNotification.statusInfo)
               && Objects.equals(this.smallDataRateStatus, smContextStatusNotification.smallDataRateStatus)
               && Objects.equals(this.apnRateStatus, smContextStatusNotification.apnRateStatus)
               && Objects.equals(this.ddnFailureStatus, smContextStatusNotification.ddnFailureStatus)
               && Objects.equals(this.notifyCorrelationIdsForddnFailure, smContextStatusNotification.notifyCorrelationIdsForddnFailure)
               && Objects.equals(this.newIntermediateSmfId, smContextStatusNotification.newIntermediateSmfId)
               && Objects.equals(this.newSmfId, smContextStatusNotification.newSmfId)
               && Objects.equals(this.newSmfSetId, smContextStatusNotification.newSmfSetId)
               && Objects.equals(this.oldSmfId, smContextStatusNotification.oldSmfId)
               && Objects.equals(this.oldSmContextRef, smContextStatusNotification.oldSmContextRef)
               && Objects.equals(this.altAnchorSmfUri, smContextStatusNotification.altAnchorSmfUri)
               && Objects.equals(this.altAnchorSmfId, smContextStatusNotification.altAnchorSmfId)
               && Objects.equals(this.targetDnaiInfo, smContextStatusNotification.targetDnaiInfo)
               && Objects.equals(this.oldPduSessionRef, smContextStatusNotification.oldPduSessionRef)
               && Objects.equals(this.interPlmnApiRoot, smContextStatusNotification.interPlmnApiRoot);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(statusInfo,
                            smallDataRateStatus,
                            apnRateStatus,
                            ddnFailureStatus,
                            notifyCorrelationIdsForddnFailure,
                            newIntermediateSmfId,
                            newSmfId,
                            newSmfSetId,
                            oldSmfId,
                            oldSmContextRef,
                            altAnchorSmfUri,
                            altAnchorSmfId,
                            targetDnaiInfo,
                            oldPduSessionRef,
                            interPlmnApiRoot);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class SmContextStatusNotification {\n");
        sb.append("    statusInfo: ").append(toIndentedString(statusInfo)).append("\n");
        sb.append("    smallDataRateStatus: ").append(toIndentedString(smallDataRateStatus)).append("\n");
        sb.append("    apnRateStatus: ").append(toIndentedString(apnRateStatus)).append("\n");
        sb.append("    ddnFailureStatus: ").append(toIndentedString(ddnFailureStatus)).append("\n");
        sb.append("    notifyCorrelationIdsForddnFailure: ").append(toIndentedString(notifyCorrelationIdsForddnFailure)).append("\n");
        sb.append("    newIntermediateSmfId: ").append(toIndentedString(newIntermediateSmfId)).append("\n");
        sb.append("    newSmfId: ").append(toIndentedString(newSmfId)).append("\n");
        sb.append("    newSmfSetId: ").append(toIndentedString(newSmfSetId)).append("\n");
        sb.append("    oldSmfId: ").append(toIndentedString(oldSmfId)).append("\n");
        sb.append("    oldSmContextRef: ").append(toIndentedString(oldSmContextRef)).append("\n");
        sb.append("    altAnchorSmfUri: ").append(toIndentedString(altAnchorSmfUri)).append("\n");
        sb.append("    altAnchorSmfId: ").append(toIndentedString(altAnchorSmfId)).append("\n");
        sb.append("    targetDnaiInfo: ").append(toIndentedString(targetDnaiInfo)).append("\n");
        sb.append("    oldPduSessionRef: ").append(toIndentedString(oldPduSessionRef)).append("\n");
        sb.append("    interPlmnApiRoot: ").append(toIndentedString(interPlmnApiRoot)).append("\n");
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
