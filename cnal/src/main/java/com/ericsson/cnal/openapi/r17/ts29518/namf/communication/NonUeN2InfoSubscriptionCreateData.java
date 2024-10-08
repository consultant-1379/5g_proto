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
import com.ericsson.cnal.openapi.r17.ts29571.commondata.AccessType;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.GlobalRanNodeId;
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
 * Data within a create subscription request for non-UE specific N2 information
 * notification
 */
@ApiModel(description = "Data within a create subscription request for non-UE specific N2 information notification")
@JsonPropertyOrder({ NonUeN2InfoSubscriptionCreateData.JSON_PROPERTY_GLOBAL_RAN_NODE_LIST,
                     NonUeN2InfoSubscriptionCreateData.JSON_PROPERTY_AN_TYPE_LIST,
                     NonUeN2InfoSubscriptionCreateData.JSON_PROPERTY_N2_INFORMATION_CLASS,
                     NonUeN2InfoSubscriptionCreateData.JSON_PROPERTY_N2_NOTIFY_CALLBACK_URI,
                     NonUeN2InfoSubscriptionCreateData.JSON_PROPERTY_NF_ID,
                     NonUeN2InfoSubscriptionCreateData.JSON_PROPERTY_SUPPORTED_FEATURES })
public class NonUeN2InfoSubscriptionCreateData
{
    public static final String JSON_PROPERTY_GLOBAL_RAN_NODE_LIST = "globalRanNodeList";
    private List<GlobalRanNodeId> globalRanNodeList = null;

    public static final String JSON_PROPERTY_AN_TYPE_LIST = "anTypeList";
    private List<AccessType> anTypeList = null;

    public static final String JSON_PROPERTY_N2_INFORMATION_CLASS = "n2InformationClass";
    private String n2InformationClass;

    public static final String JSON_PROPERTY_N2_NOTIFY_CALLBACK_URI = "n2NotifyCallbackUri";
    private String n2NotifyCallbackUri;

    public static final String JSON_PROPERTY_NF_ID = "nfId";
    private UUID nfId;

    public static final String JSON_PROPERTY_SUPPORTED_FEATURES = "supportedFeatures";
    private String supportedFeatures;

    public NonUeN2InfoSubscriptionCreateData()
    {
    }

    public NonUeN2InfoSubscriptionCreateData globalRanNodeList(List<GlobalRanNodeId> globalRanNodeList)
    {

        this.globalRanNodeList = globalRanNodeList;
        return this;
    }

    public NonUeN2InfoSubscriptionCreateData addGlobalRanNodeListItem(GlobalRanNodeId globalRanNodeListItem)
    {
        if (this.globalRanNodeList == null)
        {
            this.globalRanNodeList = new ArrayList<>();
        }
        this.globalRanNodeList.add(globalRanNodeListItem);
        return this;
    }

    /**
     * Get globalRanNodeList
     * 
     * @return globalRanNodeList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_GLOBAL_RAN_NODE_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<GlobalRanNodeId> getGlobalRanNodeList()
    {
        return globalRanNodeList;
    }

    @JsonProperty(JSON_PROPERTY_GLOBAL_RAN_NODE_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setGlobalRanNodeList(List<GlobalRanNodeId> globalRanNodeList)
    {
        this.globalRanNodeList = globalRanNodeList;
    }

    public NonUeN2InfoSubscriptionCreateData anTypeList(List<AccessType> anTypeList)
    {

        this.anTypeList = anTypeList;
        return this;
    }

    public NonUeN2InfoSubscriptionCreateData addAnTypeListItem(AccessType anTypeListItem)
    {
        if (this.anTypeList == null)
        {
            this.anTypeList = new ArrayList<>();
        }
        this.anTypeList.add(anTypeListItem);
        return this;
    }

    /**
     * Get anTypeList
     * 
     * @return anTypeList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_AN_TYPE_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<AccessType> getAnTypeList()
    {
        return anTypeList;
    }

    @JsonProperty(JSON_PROPERTY_AN_TYPE_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAnTypeList(List<AccessType> anTypeList)
    {
        this.anTypeList = anTypeList;
    }

    public NonUeN2InfoSubscriptionCreateData n2InformationClass(String n2InformationClass)
    {

        this.n2InformationClass = n2InformationClass;
        return this;
    }

    /**
     * Enumeration for N2 Information Class
     * 
     * @return n2InformationClass
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "Enumeration for N2 Information Class")
    @JsonProperty(JSON_PROPERTY_N2_INFORMATION_CLASS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getN2InformationClass()
    {
        return n2InformationClass;
    }

    @JsonProperty(JSON_PROPERTY_N2_INFORMATION_CLASS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setN2InformationClass(String n2InformationClass)
    {
        this.n2InformationClass = n2InformationClass;
    }

    public NonUeN2InfoSubscriptionCreateData n2NotifyCallbackUri(String n2NotifyCallbackUri)
    {

        this.n2NotifyCallbackUri = n2NotifyCallbackUri;
        return this;
    }

    /**
     * String providing an URI formatted according to RFC 3986.
     * 
     * @return n2NotifyCallbackUri
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "String providing an URI formatted according to RFC 3986.")
    @JsonProperty(JSON_PROPERTY_N2_NOTIFY_CALLBACK_URI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getN2NotifyCallbackUri()
    {
        return n2NotifyCallbackUri;
    }

    @JsonProperty(JSON_PROPERTY_N2_NOTIFY_CALLBACK_URI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setN2NotifyCallbackUri(String n2NotifyCallbackUri)
    {
        this.n2NotifyCallbackUri = n2NotifyCallbackUri;
    }

    public NonUeN2InfoSubscriptionCreateData nfId(UUID nfId)
    {

        this.nfId = nfId;
        return this;
    }

    /**
     * String uniquely identifying a NF instance. The format of the NF Instance ID
     * shall be a Universally Unique Identifier (UUID) version 4, as described in
     * IETF RFC 4122.
     * 
     * @return nfId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String uniquely identifying a NF instance. The format of the NF Instance ID shall be a  Universally Unique Identifier (UUID) version 4, as described in IETF RFC 4122.  ")
    @JsonProperty(JSON_PROPERTY_NF_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UUID getNfId()
    {
        return nfId;
    }

    @JsonProperty(JSON_PROPERTY_NF_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNfId(UUID nfId)
    {
        this.nfId = nfId;
    }

    public NonUeN2InfoSubscriptionCreateData supportedFeatures(String supportedFeatures)
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
        NonUeN2InfoSubscriptionCreateData nonUeN2InfoSubscriptionCreateData = (NonUeN2InfoSubscriptionCreateData) o;
        return Objects.equals(this.globalRanNodeList, nonUeN2InfoSubscriptionCreateData.globalRanNodeList)
               && Objects.equals(this.anTypeList, nonUeN2InfoSubscriptionCreateData.anTypeList)
               && Objects.equals(this.n2InformationClass, nonUeN2InfoSubscriptionCreateData.n2InformationClass)
               && Objects.equals(this.n2NotifyCallbackUri, nonUeN2InfoSubscriptionCreateData.n2NotifyCallbackUri)
               && Objects.equals(this.nfId, nonUeN2InfoSubscriptionCreateData.nfId)
               && Objects.equals(this.supportedFeatures, nonUeN2InfoSubscriptionCreateData.supportedFeatures);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(globalRanNodeList, anTypeList, n2InformationClass, n2NotifyCallbackUri, nfId, supportedFeatures);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class NonUeN2InfoSubscriptionCreateData {\n");
        sb.append("    globalRanNodeList: ").append(toIndentedString(globalRanNodeList)).append("\n");
        sb.append("    anTypeList: ").append(toIndentedString(anTypeList)).append("\n");
        sb.append("    n2InformationClass: ").append(toIndentedString(n2InformationClass)).append("\n");
        sb.append("    n2NotifyCallbackUri: ").append(toIndentedString(n2NotifyCallbackUri)).append("\n");
        sb.append("    nfId: ").append(toIndentedString(nfId)).append("\n");
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
