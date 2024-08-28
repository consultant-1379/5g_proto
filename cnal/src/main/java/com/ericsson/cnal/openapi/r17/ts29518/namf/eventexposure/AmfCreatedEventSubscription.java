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
 * Data within a create AMF event subscription response
 */
@ApiModel(description = "Data within a create AMF event subscription response")
@JsonPropertyOrder({ AmfCreatedEventSubscription.JSON_PROPERTY_SUBSCRIPTION,
                     AmfCreatedEventSubscription.JSON_PROPERTY_SUBSCRIPTION_ID,
                     AmfCreatedEventSubscription.JSON_PROPERTY_REPORT_LIST,
                     AmfCreatedEventSubscription.JSON_PROPERTY_SUPPORTED_FEATURES })
public class AmfCreatedEventSubscription
{
    public static final String JSON_PROPERTY_SUBSCRIPTION = "subscription";
    private AmfEventSubscription subscription;

    public static final String JSON_PROPERTY_SUBSCRIPTION_ID = "subscriptionId";
    private String subscriptionId;

    public static final String JSON_PROPERTY_REPORT_LIST = "reportList";
    private List<AmfEventReport> reportList = null;

    public static final String JSON_PROPERTY_SUPPORTED_FEATURES = "supportedFeatures";
    private String supportedFeatures;

    public AmfCreatedEventSubscription()
    {
    }

    public AmfCreatedEventSubscription subscription(AmfEventSubscription subscription)
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

    public AmfCreatedEventSubscription subscriptionId(String subscriptionId)
    {

        this.subscriptionId = subscriptionId;
        return this;
    }

    /**
     * String providing an URI formatted according to RFC 3986.
     * 
     * @return subscriptionId
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "String providing an URI formatted according to RFC 3986.")
    @JsonProperty(JSON_PROPERTY_SUBSCRIPTION_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getSubscriptionId()
    {
        return subscriptionId;
    }

    @JsonProperty(JSON_PROPERTY_SUBSCRIPTION_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setSubscriptionId(String subscriptionId)
    {
        this.subscriptionId = subscriptionId;
    }

    public AmfCreatedEventSubscription reportList(List<AmfEventReport> reportList)
    {

        this.reportList = reportList;
        return this;
    }

    public AmfCreatedEventSubscription addReportListItem(AmfEventReport reportListItem)
    {
        if (this.reportList == null)
        {
            this.reportList = new ArrayList<>();
        }
        this.reportList.add(reportListItem);
        return this;
    }

    /**
     * Get reportList
     * 
     * @return reportList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_REPORT_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<AmfEventReport> getReportList()
    {
        return reportList;
    }

    @JsonProperty(JSON_PROPERTY_REPORT_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setReportList(List<AmfEventReport> reportList)
    {
        this.reportList = reportList;
    }

    public AmfCreatedEventSubscription supportedFeatures(String supportedFeatures)
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
        AmfCreatedEventSubscription amfCreatedEventSubscription = (AmfCreatedEventSubscription) o;
        return Objects.equals(this.subscription, amfCreatedEventSubscription.subscription)
               && Objects.equals(this.subscriptionId, amfCreatedEventSubscription.subscriptionId)
               && Objects.equals(this.reportList, amfCreatedEventSubscription.reportList)
               && Objects.equals(this.supportedFeatures, amfCreatedEventSubscription.supportedFeatures);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(subscription, subscriptionId, reportList, supportedFeatures);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class AmfCreatedEventSubscription {\n");
        sb.append("    subscription: ").append(toIndentedString(subscription)).append("\n");
        sb.append("    subscriptionId: ").append(toIndentedString(subscriptionId)).append("\n");
        sb.append("    reportList: ").append(toIndentedString(reportList)).append("\n");
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
