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
 * UE policy delivery related N1 message notification subscription data.
 */
@ApiModel(description = "UE policy delivery related N1 message notification subscription data.")
@JsonPropertyOrder({ UpdpSubscriptionData.JSON_PROPERTY_UPDP_NOTIFY_SUBSCRIPTION_ID,
                     UpdpSubscriptionData.JSON_PROPERTY_UPDP_NOTIFY_CALLBACK_URI,
                     UpdpSubscriptionData.JSON_PROPERTY_SUPPORTED_FEATURES,
                     UpdpSubscriptionData.JSON_PROPERTY_UPDP_CALLBACK_BINDING })
public class UpdpSubscriptionData
{
    public static final String JSON_PROPERTY_UPDP_NOTIFY_SUBSCRIPTION_ID = "updpNotifySubscriptionId";
    private String updpNotifySubscriptionId;

    public static final String JSON_PROPERTY_UPDP_NOTIFY_CALLBACK_URI = "updpNotifyCallbackUri";
    private String updpNotifyCallbackUri;

    public static final String JSON_PROPERTY_SUPPORTED_FEATURES = "supportedFeatures";
    private String supportedFeatures;

    public static final String JSON_PROPERTY_UPDP_CALLBACK_BINDING = "updpCallbackBinding";
    private String updpCallbackBinding;

    public UpdpSubscriptionData()
    {
    }

    public UpdpSubscriptionData updpNotifySubscriptionId(String updpNotifySubscriptionId)
    {

        this.updpNotifySubscriptionId = updpNotifySubscriptionId;
        return this;
    }

    /**
     * Get updpNotifySubscriptionId
     * 
     * @return updpNotifySubscriptionId
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_UPDP_NOTIFY_SUBSCRIPTION_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getUpdpNotifySubscriptionId()
    {
        return updpNotifySubscriptionId;
    }

    @JsonProperty(JSON_PROPERTY_UPDP_NOTIFY_SUBSCRIPTION_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setUpdpNotifySubscriptionId(String updpNotifySubscriptionId)
    {
        this.updpNotifySubscriptionId = updpNotifySubscriptionId;
    }

    public UpdpSubscriptionData updpNotifyCallbackUri(String updpNotifyCallbackUri)
    {

        this.updpNotifyCallbackUri = updpNotifyCallbackUri;
        return this;
    }

    /**
     * String providing an URI formatted according to RFC 3986.
     * 
     * @return updpNotifyCallbackUri
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "String providing an URI formatted according to RFC 3986.")
    @JsonProperty(JSON_PROPERTY_UPDP_NOTIFY_CALLBACK_URI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getUpdpNotifyCallbackUri()
    {
        return updpNotifyCallbackUri;
    }

    @JsonProperty(JSON_PROPERTY_UPDP_NOTIFY_CALLBACK_URI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setUpdpNotifyCallbackUri(String updpNotifyCallbackUri)
    {
        this.updpNotifyCallbackUri = updpNotifyCallbackUri;
    }

    public UpdpSubscriptionData supportedFeatures(String supportedFeatures)
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

    public UpdpSubscriptionData updpCallbackBinding(String updpCallbackBinding)
    {

        this.updpCallbackBinding = updpCallbackBinding;
        return this;
    }

    /**
     * Get updpCallbackBinding
     * 
     * @return updpCallbackBinding
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_UPDP_CALLBACK_BINDING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getUpdpCallbackBinding()
    {
        return updpCallbackBinding;
    }

    @JsonProperty(JSON_PROPERTY_UPDP_CALLBACK_BINDING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUpdpCallbackBinding(String updpCallbackBinding)
    {
        this.updpCallbackBinding = updpCallbackBinding;
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
        UpdpSubscriptionData updpSubscriptionData = (UpdpSubscriptionData) o;
        return Objects.equals(this.updpNotifySubscriptionId, updpSubscriptionData.updpNotifySubscriptionId)
               && Objects.equals(this.updpNotifyCallbackUri, updpSubscriptionData.updpNotifyCallbackUri)
               && Objects.equals(this.supportedFeatures, updpSubscriptionData.supportedFeatures)
               && Objects.equals(this.updpCallbackBinding, updpSubscriptionData.updpCallbackBinding);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(updpNotifySubscriptionId, updpNotifyCallbackUri, supportedFeatures, updpCallbackBinding);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class UpdpSubscriptionData {\n");
        sb.append("    updpNotifySubscriptionId: ").append(toIndentedString(updpNotifySubscriptionId)).append("\n");
        sb.append("    updpNotifyCallbackUri: ").append(toIndentedString(updpNotifyCallbackUri)).append("\n");
        sb.append("    supportedFeatures: ").append(toIndentedString(supportedFeatures)).append("\n");
        sb.append("    updpCallbackBinding: ").append(toIndentedString(updpCallbackBinding)).append("\n");
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
