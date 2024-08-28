/*
 * Namf_Communication
 * AMF Communication Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29518.namf.communication;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.PlmnIdNid;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.NgApCause;
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
 * Data within a request to create an individual ueContext resource
 */
@ApiModel(description = "Data within a request to create an individual ueContext resource")
@JsonPropertyOrder({ UeContextCreateData.JSON_PROPERTY_UE_CONTEXT,
                     UeContextCreateData.JSON_PROPERTY_TARGET_ID,
                     UeContextCreateData.JSON_PROPERTY_SOURCE_TO_TARGET_DATA,
                     UeContextCreateData.JSON_PROPERTY_PDU_SESSION_LIST,
                     UeContextCreateData.JSON_PROPERTY_N2_NOTIFY_URI,
                     UeContextCreateData.JSON_PROPERTY_UE_RADIO_CAPABILITY,
                     UeContextCreateData.JSON_PROPERTY_UE_RADIO_CAPABILITY_FOR_PAGING,
                     UeContextCreateData.JSON_PROPERTY_NGAP_CAUSE,
                     UeContextCreateData.JSON_PROPERTY_SUPPORTED_FEATURES,
                     UeContextCreateData.JSON_PROPERTY_SERVING_NETWORK })
public class UeContextCreateData
{
    public static final String JSON_PROPERTY_UE_CONTEXT = "ueContext";
    private UeContext ueContext;

    public static final String JSON_PROPERTY_TARGET_ID = "targetId";
    private NgRanTargetId targetId;

    public static final String JSON_PROPERTY_SOURCE_TO_TARGET_DATA = "sourceToTargetData";
    private N2InfoContent sourceToTargetData;

    public static final String JSON_PROPERTY_PDU_SESSION_LIST = "pduSessionList";
    private List<N2SmInformation> pduSessionList = new ArrayList<>();

    public static final String JSON_PROPERTY_N2_NOTIFY_URI = "n2NotifyUri";
    private String n2NotifyUri;

    public static final String JSON_PROPERTY_UE_RADIO_CAPABILITY = "ueRadioCapability";
    private N2InfoContent ueRadioCapability;

    public static final String JSON_PROPERTY_UE_RADIO_CAPABILITY_FOR_PAGING = "ueRadioCapabilityForPaging";
    private N2InfoContent ueRadioCapabilityForPaging;

    public static final String JSON_PROPERTY_NGAP_CAUSE = "ngapCause";
    private NgApCause ngapCause;

    public static final String JSON_PROPERTY_SUPPORTED_FEATURES = "supportedFeatures";
    private String supportedFeatures;

    public static final String JSON_PROPERTY_SERVING_NETWORK = "servingNetwork";
    private PlmnIdNid servingNetwork;

    public UeContextCreateData()
    {
    }

    public UeContextCreateData ueContext(UeContext ueContext)
    {

        this.ueContext = ueContext;
        return this;
    }

    /**
     * Get ueContext
     * 
     * @return ueContext
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_UE_CONTEXT)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public UeContext getUeContext()
    {
        return ueContext;
    }

    @JsonProperty(JSON_PROPERTY_UE_CONTEXT)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setUeContext(UeContext ueContext)
    {
        this.ueContext = ueContext;
    }

    public UeContextCreateData targetId(NgRanTargetId targetId)
    {

        this.targetId = targetId;
        return this;
    }

    /**
     * Get targetId
     * 
     * @return targetId
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_TARGET_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public NgRanTargetId getTargetId()
    {
        return targetId;
    }

    @JsonProperty(JSON_PROPERTY_TARGET_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setTargetId(NgRanTargetId targetId)
    {
        this.targetId = targetId;
    }

    public UeContextCreateData sourceToTargetData(N2InfoContent sourceToTargetData)
    {

        this.sourceToTargetData = sourceToTargetData;
        return this;
    }

    /**
     * Get sourceToTargetData
     * 
     * @return sourceToTargetData
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_SOURCE_TO_TARGET_DATA)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public N2InfoContent getSourceToTargetData()
    {
        return sourceToTargetData;
    }

    @JsonProperty(JSON_PROPERTY_SOURCE_TO_TARGET_DATA)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setSourceToTargetData(N2InfoContent sourceToTargetData)
    {
        this.sourceToTargetData = sourceToTargetData;
    }

    public UeContextCreateData pduSessionList(List<N2SmInformation> pduSessionList)
    {

        this.pduSessionList = pduSessionList;
        return this;
    }

    public UeContextCreateData addPduSessionListItem(N2SmInformation pduSessionListItem)
    {
        this.pduSessionList.add(pduSessionListItem);
        return this;
    }

    /**
     * Get pduSessionList
     * 
     * @return pduSessionList
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_PDU_SESSION_LIST)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public List<N2SmInformation> getPduSessionList()
    {
        return pduSessionList;
    }

    @JsonProperty(JSON_PROPERTY_PDU_SESSION_LIST)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setPduSessionList(List<N2SmInformation> pduSessionList)
    {
        this.pduSessionList = pduSessionList;
    }

    public UeContextCreateData n2NotifyUri(String n2NotifyUri)
    {

        this.n2NotifyUri = n2NotifyUri;
        return this;
    }

    /**
     * String providing an URI formatted according to RFC 3986.
     * 
     * @return n2NotifyUri
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String providing an URI formatted according to RFC 3986.")
    @JsonProperty(JSON_PROPERTY_N2_NOTIFY_URI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getN2NotifyUri()
    {
        return n2NotifyUri;
    }

    @JsonProperty(JSON_PROPERTY_N2_NOTIFY_URI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setN2NotifyUri(String n2NotifyUri)
    {
        this.n2NotifyUri = n2NotifyUri;
    }

    public UeContextCreateData ueRadioCapability(N2InfoContent ueRadioCapability)
    {

        this.ueRadioCapability = ueRadioCapability;
        return this;
    }

    /**
     * Get ueRadioCapability
     * 
     * @return ueRadioCapability
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_UE_RADIO_CAPABILITY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public N2InfoContent getUeRadioCapability()
    {
        return ueRadioCapability;
    }

    @JsonProperty(JSON_PROPERTY_UE_RADIO_CAPABILITY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUeRadioCapability(N2InfoContent ueRadioCapability)
    {
        this.ueRadioCapability = ueRadioCapability;
    }

    public UeContextCreateData ueRadioCapabilityForPaging(N2InfoContent ueRadioCapabilityForPaging)
    {

        this.ueRadioCapabilityForPaging = ueRadioCapabilityForPaging;
        return this;
    }

    /**
     * Get ueRadioCapabilityForPaging
     * 
     * @return ueRadioCapabilityForPaging
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_UE_RADIO_CAPABILITY_FOR_PAGING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public N2InfoContent getUeRadioCapabilityForPaging()
    {
        return ueRadioCapabilityForPaging;
    }

    @JsonProperty(JSON_PROPERTY_UE_RADIO_CAPABILITY_FOR_PAGING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUeRadioCapabilityForPaging(N2InfoContent ueRadioCapabilityForPaging)
    {
        this.ueRadioCapabilityForPaging = ueRadioCapabilityForPaging;
    }

    public UeContextCreateData ngapCause(NgApCause ngapCause)
    {

        this.ngapCause = ngapCause;
        return this;
    }

    /**
     * Get ngapCause
     * 
     * @return ngapCause
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_NGAP_CAUSE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public NgApCause getNgapCause()
    {
        return ngapCause;
    }

    @JsonProperty(JSON_PROPERTY_NGAP_CAUSE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNgapCause(NgApCause ngapCause)
    {
        this.ngapCause = ngapCause;
    }

    public UeContextCreateData supportedFeatures(String supportedFeatures)
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

    public UeContextCreateData servingNetwork(PlmnIdNid servingNetwork)
    {

        this.servingNetwork = servingNetwork;
        return this;
    }

    /**
     * Get servingNetwork
     * 
     * @return servingNetwork
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SERVING_NETWORK)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public PlmnIdNid getServingNetwork()
    {
        return servingNetwork;
    }

    @JsonProperty(JSON_PROPERTY_SERVING_NETWORK)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setServingNetwork(PlmnIdNid servingNetwork)
    {
        this.servingNetwork = servingNetwork;
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
        UeContextCreateData ueContextCreateData = (UeContextCreateData) o;
        return Objects.equals(this.ueContext, ueContextCreateData.ueContext) && Objects.equals(this.targetId, ueContextCreateData.targetId)
               && Objects.equals(this.sourceToTargetData, ueContextCreateData.sourceToTargetData)
               && Objects.equals(this.pduSessionList, ueContextCreateData.pduSessionList) && Objects.equals(this.n2NotifyUri, ueContextCreateData.n2NotifyUri)
               && Objects.equals(this.ueRadioCapability, ueContextCreateData.ueRadioCapability)
               && Objects.equals(this.ueRadioCapabilityForPaging, ueContextCreateData.ueRadioCapabilityForPaging)
               && Objects.equals(this.ngapCause, ueContextCreateData.ngapCause) && Objects.equals(this.supportedFeatures, ueContextCreateData.supportedFeatures)
               && Objects.equals(this.servingNetwork, ueContextCreateData.servingNetwork);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(ueContext,
                            targetId,
                            sourceToTargetData,
                            pduSessionList,
                            n2NotifyUri,
                            ueRadioCapability,
                            ueRadioCapabilityForPaging,
                            ngapCause,
                            supportedFeatures,
                            servingNetwork);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class UeContextCreateData {\n");
        sb.append("    ueContext: ").append(toIndentedString(ueContext)).append("\n");
        sb.append("    targetId: ").append(toIndentedString(targetId)).append("\n");
        sb.append("    sourceToTargetData: ").append(toIndentedString(sourceToTargetData)).append("\n");
        sb.append("    pduSessionList: ").append(toIndentedString(pduSessionList)).append("\n");
        sb.append("    n2NotifyUri: ").append(toIndentedString(n2NotifyUri)).append("\n");
        sb.append("    ueRadioCapability: ").append(toIndentedString(ueRadioCapability)).append("\n");
        sb.append("    ueRadioCapabilityForPaging: ").append(toIndentedString(ueRadioCapabilityForPaging)).append("\n");
        sb.append("    ngapCause: ").append(toIndentedString(ngapCause)).append("\n");
        sb.append("    supportedFeatures: ").append(toIndentedString(supportedFeatures)).append("\n");
        sb.append("    servingNetwork: ").append(toIndentedString(servingNetwork)).append("\n");
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
