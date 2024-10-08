/*
 * N32 Handshake API
 * N32-c Handshake Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29573.n32.handshake;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.PlmnIdNid;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.PlmnId;
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
 * Defines the security capabilities of a SEPP sent to a receiving SEPP
 */
@ApiModel(description = "Defines the security capabilities of a SEPP sent to a receiving SEPP")
@JsonPropertyOrder({ SecNegotiateReqData.JSON_PROPERTY_SENDER,
                     SecNegotiateReqData.JSON_PROPERTY_SUPPORTED_SEC_CAPABILITY_LIST,
                     SecNegotiateReqData.JSON_PROPERTY_3GPP_SBI_TARGET_API_ROOT_SUPPORTED,
                     SecNegotiateReqData.JSON_PROPERTY_PLMN_ID_LIST,
                     SecNegotiateReqData.JSON_PROPERTY_SNPN_ID_LIST,
                     SecNegotiateReqData.JSON_PROPERTY_TARGET_PLMN_ID,
                     SecNegotiateReqData.JSON_PROPERTY_TARGET_SNPN_ID,
                     SecNegotiateReqData.JSON_PROPERTY_INTENDED_USAGE_PURPOSE,
                     SecNegotiateReqData.JSON_PROPERTY_SUPPORTED_FEATURES })
public class SecNegotiateReqData
{
    public static final String JSON_PROPERTY_SENDER = "sender";
    private String sender;

    public static final String JSON_PROPERTY_SUPPORTED_SEC_CAPABILITY_LIST = "supportedSecCapabilityList";
    private List<String> supportedSecCapabilityList = new ArrayList<>();

    public static final String JSON_PROPERTY_3GPP_SBI_TARGET_API_ROOT_SUPPORTED = "3GppSbiTargetApiRootSupported";
    private Boolean _3gppSbiTargetApiRootSupported = false;

    public static final String JSON_PROPERTY_PLMN_ID_LIST = "plmnIdList";
    private List<PlmnId> plmnIdList = null;

    public static final String JSON_PROPERTY_SNPN_ID_LIST = "snpnIdList";
    private List<PlmnIdNid> snpnIdList = null;

    public static final String JSON_PROPERTY_TARGET_PLMN_ID = "targetPlmnId";
    private PlmnId targetPlmnId;

    public static final String JSON_PROPERTY_TARGET_SNPN_ID = "targetSnpnId";
    private PlmnIdNid targetSnpnId;

    public static final String JSON_PROPERTY_INTENDED_USAGE_PURPOSE = "intendedUsagePurpose";
    private List<IntendedN32Purpose> intendedUsagePurpose = null;

    public static final String JSON_PROPERTY_SUPPORTED_FEATURES = "supportedFeatures";
    private String supportedFeatures;

    public SecNegotiateReqData()
    {
    }

    public SecNegotiateReqData sender(String sender)
    {

        this.sender = sender;
        return this;
    }

    /**
     * Fully Qualified Domain Name
     * 
     * @return sender
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Fully Qualified Domain Name")
    @JsonProperty(JSON_PROPERTY_SENDER)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getSender()
    {
        return sender;
    }

    @JsonProperty(JSON_PROPERTY_SENDER)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setSender(String sender)
    {
        this.sender = sender;
    }

    public SecNegotiateReqData supportedSecCapabilityList(List<String> supportedSecCapabilityList)
    {

        this.supportedSecCapabilityList = supportedSecCapabilityList;
        return this;
    }

    public SecNegotiateReqData addSupportedSecCapabilityListItem(String supportedSecCapabilityListItem)
    {
        this.supportedSecCapabilityList.add(supportedSecCapabilityListItem);
        return this;
    }

    /**
     * Get supportedSecCapabilityList
     * 
     * @return supportedSecCapabilityList
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_SUPPORTED_SEC_CAPABILITY_LIST)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public List<String> getSupportedSecCapabilityList()
    {
        return supportedSecCapabilityList;
    }

    @JsonProperty(JSON_PROPERTY_SUPPORTED_SEC_CAPABILITY_LIST)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setSupportedSecCapabilityList(List<String> supportedSecCapabilityList)
    {
        this.supportedSecCapabilityList = supportedSecCapabilityList;
    }

    public SecNegotiateReqData _3gppSbiTargetApiRootSupported(Boolean _3gppSbiTargetApiRootSupported)
    {

        this._3gppSbiTargetApiRootSupported = _3gppSbiTargetApiRootSupported;
        return this;
    }

    /**
     * Get _3gppSbiTargetApiRootSupported
     * 
     * @return _3gppSbiTargetApiRootSupported
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_3GPP_SBI_TARGET_API_ROOT_SUPPORTED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean get3gppSbiTargetApiRootSupported()
    {
        return _3gppSbiTargetApiRootSupported;
    }

    @JsonProperty(JSON_PROPERTY_3GPP_SBI_TARGET_API_ROOT_SUPPORTED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void set3gppSbiTargetApiRootSupported(Boolean _3gppSbiTargetApiRootSupported)
    {
        this._3gppSbiTargetApiRootSupported = _3gppSbiTargetApiRootSupported;
    }

    public SecNegotiateReqData plmnIdList(List<PlmnId> plmnIdList)
    {

        this.plmnIdList = plmnIdList;
        return this;
    }

    public SecNegotiateReqData addPlmnIdListItem(PlmnId plmnIdListItem)
    {
        if (this.plmnIdList == null)
        {
            this.plmnIdList = new ArrayList<>();
        }
        this.plmnIdList.add(plmnIdListItem);
        return this;
    }

    /**
     * Get plmnIdList
     * 
     * @return plmnIdList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_PLMN_ID_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<PlmnId> getPlmnIdList()
    {
        return plmnIdList;
    }

    @JsonProperty(JSON_PROPERTY_PLMN_ID_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlmnIdList(List<PlmnId> plmnIdList)
    {
        this.plmnIdList = plmnIdList;
    }

    public SecNegotiateReqData snpnIdList(List<PlmnIdNid> snpnIdList)
    {

        this.snpnIdList = snpnIdList;
        return this;
    }

    public SecNegotiateReqData addSnpnIdListItem(PlmnIdNid snpnIdListItem)
    {
        if (this.snpnIdList == null)
        {
            this.snpnIdList = new ArrayList<>();
        }
        this.snpnIdList.add(snpnIdListItem);
        return this;
    }

    /**
     * Get snpnIdList
     * 
     * @return snpnIdList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SNPN_ID_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<PlmnIdNid> getSnpnIdList()
    {
        return snpnIdList;
    }

    @JsonProperty(JSON_PROPERTY_SNPN_ID_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSnpnIdList(List<PlmnIdNid> snpnIdList)
    {
        this.snpnIdList = snpnIdList;
    }

    public SecNegotiateReqData targetPlmnId(PlmnId targetPlmnId)
    {

        this.targetPlmnId = targetPlmnId;
        return this;
    }

    /**
     * Get targetPlmnId
     * 
     * @return targetPlmnId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_TARGET_PLMN_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public PlmnId getTargetPlmnId()
    {
        return targetPlmnId;
    }

    @JsonProperty(JSON_PROPERTY_TARGET_PLMN_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTargetPlmnId(PlmnId targetPlmnId)
    {
        this.targetPlmnId = targetPlmnId;
    }

    public SecNegotiateReqData targetSnpnId(PlmnIdNid targetSnpnId)
    {

        this.targetSnpnId = targetSnpnId;
        return this;
    }

    /**
     * Get targetSnpnId
     * 
     * @return targetSnpnId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_TARGET_SNPN_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public PlmnIdNid getTargetSnpnId()
    {
        return targetSnpnId;
    }

    @JsonProperty(JSON_PROPERTY_TARGET_SNPN_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTargetSnpnId(PlmnIdNid targetSnpnId)
    {
        this.targetSnpnId = targetSnpnId;
    }

    public SecNegotiateReqData intendedUsagePurpose(List<IntendedN32Purpose> intendedUsagePurpose)
    {

        this.intendedUsagePurpose = intendedUsagePurpose;
        return this;
    }

    public SecNegotiateReqData addIntendedUsagePurposeItem(IntendedN32Purpose intendedUsagePurposeItem)
    {
        if (this.intendedUsagePurpose == null)
        {
            this.intendedUsagePurpose = new ArrayList<>();
        }
        this.intendedUsagePurpose.add(intendedUsagePurposeItem);
        return this;
    }

    /**
     * Get intendedUsagePurpose
     * 
     * @return intendedUsagePurpose
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_INTENDED_USAGE_PURPOSE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<IntendedN32Purpose> getIntendedUsagePurpose()
    {
        return intendedUsagePurpose;
    }

    @JsonProperty(JSON_PROPERTY_INTENDED_USAGE_PURPOSE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIntendedUsagePurpose(List<IntendedN32Purpose> intendedUsagePurpose)
    {
        this.intendedUsagePurpose = intendedUsagePurpose;
    }

    public SecNegotiateReqData supportedFeatures(String supportedFeatures)
    {

        this.supportedFeatures = supportedFeatures;
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
     * @return supportedFeatures
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "A string used to indicate the features supported by an API that is used as defined in clause  6.6 in 3GPP TS 29.500. The string shall contain a bitmask indicating supported features in  hexadecimal representation Each character in the string shall take a value of \"0\" to \"9\",  \"a\" to \"f\" or \"A\" to \"F\" and shall represent the support of 4 features as described in  table 5.2.2-3. The most significant character representing the highest-numbered features shall  appear first in the string, and the character representing features 1 to 4 shall appear last  in the string. The list of features and their numbering (starting with 1) are defined  separately for each API. If the string contains a lower number of characters than there are  defined features for an API, all features that would be represented by characters that are not  present in the string are not supported. ")
    @JsonProperty(JSON_PROPERTY_SUPPORTED_FEATURES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getSupportedFeatures()
    {
        return supportedFeatures;
    }

    @JsonProperty(JSON_PROPERTY_SUPPORTED_FEATURES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSupportedFeatures(String supportedFeatures)
    {
        this.supportedFeatures = supportedFeatures;
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
        SecNegotiateReqData secNegotiateReqData = (SecNegotiateReqData) o;
        return Objects.equals(this.sender, secNegotiateReqData.sender)
               && Objects.equals(this.supportedSecCapabilityList, secNegotiateReqData.supportedSecCapabilityList)
               && Objects.equals(this._3gppSbiTargetApiRootSupported, secNegotiateReqData._3gppSbiTargetApiRootSupported)
               && Objects.equals(this.plmnIdList, secNegotiateReqData.plmnIdList) && Objects.equals(this.snpnIdList, secNegotiateReqData.snpnIdList)
               && Objects.equals(this.targetPlmnId, secNegotiateReqData.targetPlmnId) && Objects.equals(this.targetSnpnId, secNegotiateReqData.targetSnpnId)
               && Objects.equals(this.intendedUsagePurpose, secNegotiateReqData.intendedUsagePurpose)
               && Objects.equals(this.supportedFeatures, secNegotiateReqData.supportedFeatures);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(sender,
                            supportedSecCapabilityList,
                            _3gppSbiTargetApiRootSupported,
                            plmnIdList,
                            snpnIdList,
                            targetPlmnId,
                            targetSnpnId,
                            intendedUsagePurpose,
                            supportedFeatures);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class SecNegotiateReqData {\n");
        sb.append("    sender: ").append(toIndentedString(sender)).append("\n");
        sb.append("    supportedSecCapabilityList: ").append(toIndentedString(supportedSecCapabilityList)).append("\n");
        sb.append("    _3gppSbiTargetApiRootSupported: ").append(toIndentedString(_3gppSbiTargetApiRootSupported)).append("\n");
        sb.append("    plmnIdList: ").append(toIndentedString(plmnIdList)).append("\n");
        sb.append("    snpnIdList: ").append(toIndentedString(snpnIdList)).append("\n");
        sb.append("    targetPlmnId: ").append(toIndentedString(targetPlmnId)).append("\n");
        sb.append("    targetSnpnId: ").append(toIndentedString(targetSnpnId)).append("\n");
        sb.append("    intendedUsagePurpose: ").append(toIndentedString(intendedUsagePurpose)).append("\n");
        sb.append("    supportedFeatures: ").append(toIndentedString(supportedFeatures)).append("\n");
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
