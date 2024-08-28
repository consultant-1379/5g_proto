/*
 * Namf_EventExposure
 * AMF Event Exposure Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29518.namf.eventexposure;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.Guami;
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
 * Data within a create AMF event subscription request
 */
@ApiModel(description = "Data within a create AMF event subscription request")
@JsonPropertyOrder({ AmfCreateEventSubscription.JSON_PROPERTY_SUBSCRIPTION,
                     AmfCreateEventSubscription.JSON_PROPERTY_SUPPORTED_FEATURES,
                     AmfCreateEventSubscription.JSON_PROPERTY_OLD_GUAMI })
public class AmfCreateEventSubscription
{
    public static final String JSON_PROPERTY_SUBSCRIPTION = "subscription";
    private AmfEventSubscription subscription;

    public static final String JSON_PROPERTY_SUPPORTED_FEATURES = "supportedFeatures";
    private String supportedFeatures;

    public static final String JSON_PROPERTY_OLD_GUAMI = "oldGuami";
    private Guami oldGuami;

    public AmfCreateEventSubscription()
    {
    }

    public AmfCreateEventSubscription subscription(AmfEventSubscription subscription)
    {

        this.subscription = subscription;
        return this;
    }

    /**
     * Get subscription
     * 
     * @return subscription
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_SUBSCRIPTION)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public AmfEventSubscription getSubscription()
    {
        return subscription;
    }

    @JsonProperty(JSON_PROPERTY_SUBSCRIPTION)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setSubscription(AmfEventSubscription subscription)
    {
        this.subscription = subscription;
    }

    public AmfCreateEventSubscription supportedFeatures(String supportedFeatures)
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

    public AmfCreateEventSubscription oldGuami(Guami oldGuami)
    {

        this.oldGuami = oldGuami;
        return this;
    }

    /**
     * Get oldGuami
     * 
     * @return oldGuami
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_OLD_GUAMI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Guami getOldGuami()
    {
        return oldGuami;
    }

    @JsonProperty(JSON_PROPERTY_OLD_GUAMI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setOldGuami(Guami oldGuami)
    {
        this.oldGuami = oldGuami;
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
        AmfCreateEventSubscription amfCreateEventSubscription = (AmfCreateEventSubscription) o;
        return Objects.equals(this.subscription, amfCreateEventSubscription.subscription)
               && Objects.equals(this.supportedFeatures, amfCreateEventSubscription.supportedFeatures)
               && Objects.equals(this.oldGuami, amfCreateEventSubscription.oldGuami);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(subscription, supportedFeatures, oldGuami);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class AmfCreateEventSubscription {\n");
        sb.append("    subscription: ").append(toIndentedString(subscription)).append("\n");
        sb.append("    supportedFeatures: ").append(toIndentedString(supportedFeatures)).append("\n");
        sb.append("    oldGuami: ").append(toIndentedString(oldGuami)).append("\n");
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
