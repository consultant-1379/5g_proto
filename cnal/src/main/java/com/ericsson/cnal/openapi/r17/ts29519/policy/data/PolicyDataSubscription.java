/*
 * Unified Data Repository Service API file for policy data
 * The API version is defined in 3GPP TS 29.504   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: -
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29519.policy.data;

import java.util.Objects;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.OffsetDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Identifies a subscription to policy data change notification.
 */
@ApiModel(description = "Identifies a subscription to policy data change notification.")
@JsonPropertyOrder({ PolicyDataSubscription.JSON_PROPERTY_NOTIFICATION_URI,
                     PolicyDataSubscription.JSON_PROPERTY_NOTIF_ID,
                     PolicyDataSubscription.JSON_PROPERTY_MONITORED_RESOURCE_URIS,
                     PolicyDataSubscription.JSON_PROPERTY_MON_RES_ITEMS,
                     PolicyDataSubscription.JSON_PROPERTY_EXCLUDED_RES_ITEMS,
                     PolicyDataSubscription.JSON_PROPERTY_EXPIRY,
                     PolicyDataSubscription.JSON_PROPERTY_SUPPORTED_FEATURES,
                     PolicyDataSubscription.JSON_PROPERTY_RESET_IDS })
public class PolicyDataSubscription
{
    public static final String JSON_PROPERTY_NOTIFICATION_URI = "notificationUri";
    private String notificationUri;

    public static final String JSON_PROPERTY_NOTIF_ID = "notifId";
    private String notifId;

    public static final String JSON_PROPERTY_MONITORED_RESOURCE_URIS = "monitoredResourceUris";
    private List<String> monitoredResourceUris = new ArrayList<>();

    public static final String JSON_PROPERTY_MON_RES_ITEMS = "monResItems";
    private List<ResourceItem> monResItems = null;

    public static final String JSON_PROPERTY_EXCLUDED_RES_ITEMS = "excludedResItems";
    private List<ResourceItem> excludedResItems = null;

    public static final String JSON_PROPERTY_EXPIRY = "expiry";
    private OffsetDateTime expiry;

    public static final String JSON_PROPERTY_SUPPORTED_FEATURES = "supportedFeatures";
    private String supportedFeatures;

    public static final String JSON_PROPERTY_RESET_IDS = "resetIds";
    private List<String> resetIds = null;

    public PolicyDataSubscription()
    {
    }

    public PolicyDataSubscription notificationUri(String notificationUri)
    {

        this.notificationUri = notificationUri;
        return this;
    }

    /**
     * String providing an URI formatted according to RFC 3986.
     * 
     * @return notificationUri
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "String providing an URI formatted according to RFC 3986.")
    @JsonProperty(JSON_PROPERTY_NOTIFICATION_URI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getNotificationUri()
    {
        return notificationUri;
    }

    @JsonProperty(JSON_PROPERTY_NOTIFICATION_URI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setNotificationUri(String notificationUri)
    {
        this.notificationUri = notificationUri;
    }

    public PolicyDataSubscription notifId(String notifId)
    {

        this.notifId = notifId;
        return this;
    }

    /**
     * Get notifId
     * 
     * @return notifId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_NOTIF_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getNotifId()
    {
        return notifId;
    }

    @JsonProperty(JSON_PROPERTY_NOTIF_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNotifId(String notifId)
    {
        this.notifId = notifId;
    }

    public PolicyDataSubscription monitoredResourceUris(List<String> monitoredResourceUris)
    {

        this.monitoredResourceUris = monitoredResourceUris;
        return this;
    }

    public PolicyDataSubscription addMonitoredResourceUrisItem(String monitoredResourceUrisItem)
    {
        this.monitoredResourceUris.add(monitoredResourceUrisItem);
        return this;
    }

    /**
     * Get monitoredResourceUris
     * 
     * @return monitoredResourceUris
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_MONITORED_RESOURCE_URIS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public List<String> getMonitoredResourceUris()
    {
        return monitoredResourceUris;
    }

    @JsonProperty(JSON_PROPERTY_MONITORED_RESOURCE_URIS)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setMonitoredResourceUris(List<String> monitoredResourceUris)
    {
        this.monitoredResourceUris = monitoredResourceUris;
    }

    public PolicyDataSubscription monResItems(List<ResourceItem> monResItems)
    {

        this.monResItems = monResItems;
        return this;
    }

    public PolicyDataSubscription addMonResItemsItem(ResourceItem monResItemsItem)
    {
        if (this.monResItems == null)
        {
            this.monResItems = new ArrayList<>();
        }
        this.monResItems.add(monResItemsItem);
        return this;
    }

    /**
     * Get monResItems
     * 
     * @return monResItems
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_MON_RES_ITEMS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<ResourceItem> getMonResItems()
    {
        return monResItems;
    }

    @JsonProperty(JSON_PROPERTY_MON_RES_ITEMS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMonResItems(List<ResourceItem> monResItems)
    {
        this.monResItems = monResItems;
    }

    public PolicyDataSubscription excludedResItems(List<ResourceItem> excludedResItems)
    {

        this.excludedResItems = excludedResItems;
        return this;
    }

    public PolicyDataSubscription addExcludedResItemsItem(ResourceItem excludedResItemsItem)
    {
        if (this.excludedResItems == null)
        {
            this.excludedResItems = new ArrayList<>();
        }
        this.excludedResItems.add(excludedResItemsItem);
        return this;
    }

    /**
     * Get excludedResItems
     * 
     * @return excludedResItems
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_EXCLUDED_RES_ITEMS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<ResourceItem> getExcludedResItems()
    {
        return excludedResItems;
    }

    @JsonProperty(JSON_PROPERTY_EXCLUDED_RES_ITEMS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setExcludedResItems(List<ResourceItem> excludedResItems)
    {
        this.excludedResItems = excludedResItems;
    }

    public PolicyDataSubscription expiry(OffsetDateTime expiry)
    {

        this.expiry = expiry;
        return this;
    }

    /**
     * string with format &#39;date-time&#39; as defined in OpenAPI.
     * 
     * @return expiry
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'date-time' as defined in OpenAPI.")
    @JsonProperty(JSON_PROPERTY_EXPIRY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    public OffsetDateTime getExpiry()
    {
        return expiry;
    }

    @JsonProperty(JSON_PROPERTY_EXPIRY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setExpiry(OffsetDateTime expiry)
    {
        this.expiry = expiry;
    }

    public PolicyDataSubscription supportedFeatures(String supportedFeatures)
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

    public PolicyDataSubscription resetIds(List<String> resetIds)
    {

        this.resetIds = resetIds;
        return this;
    }

    public PolicyDataSubscription addResetIdsItem(String resetIdsItem)
    {
        if (this.resetIds == null)
        {
            this.resetIds = new ArrayList<>();
        }
        this.resetIds.add(resetIdsItem);
        return this;
    }

    /**
     * Get resetIds
     * 
     * @return resetIds
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_RESET_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getResetIds()
    {
        return resetIds;
    }

    @JsonProperty(JSON_PROPERTY_RESET_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setResetIds(List<String> resetIds)
    {
        this.resetIds = resetIds;
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
        PolicyDataSubscription policyDataSubscription = (PolicyDataSubscription) o;
        return Objects.equals(this.notificationUri, policyDataSubscription.notificationUri) && Objects.equals(this.notifId, policyDataSubscription.notifId)
               && Objects.equals(this.monitoredResourceUris, policyDataSubscription.monitoredResourceUris)
               && Objects.equals(this.monResItems, policyDataSubscription.monResItems)
               && Objects.equals(this.excludedResItems, policyDataSubscription.excludedResItems) && Objects.equals(this.expiry, policyDataSubscription.expiry)
               && Objects.equals(this.supportedFeatures, policyDataSubscription.supportedFeatures)
               && Objects.equals(this.resetIds, policyDataSubscription.resetIds);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(notificationUri, notifId, monitoredResourceUris, monResItems, excludedResItems, expiry, supportedFeatures, resetIds);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class PolicyDataSubscription {\n");
        sb.append("    notificationUri: ").append(toIndentedString(notificationUri)).append("\n");
        sb.append("    notifId: ").append(toIndentedString(notifId)).append("\n");
        sb.append("    monitoredResourceUris: ").append(toIndentedString(monitoredResourceUris)).append("\n");
        sb.append("    monResItems: ").append(toIndentedString(monResItems)).append("\n");
        sb.append("    excludedResItems: ").append(toIndentedString(excludedResItems)).append("\n");
        sb.append("    expiry: ").append(toIndentedString(expiry)).append("\n");
        sb.append("    supportedFeatures: ").append(toIndentedString(supportedFeatures)).append("\n");
        sb.append("    resetIds: ").append(toIndentedString(resetIds)).append("\n");
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
