/*
 * Unified Data Repository Service API file for subscription data
 * Unified Data Repository Service (subscription data).   The API version is defined in 3GPP TS 29.504.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: -
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29505.subscription.data;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.sdm.ContextInfo;
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
 * Contains info about a single HSS event subscription
 */
@ApiModel(description = "Contains info about a single HSS event subscription")
@JsonPropertyOrder({ HssSubscriptionItem.JSON_PROPERTY_HSS_INSTANCE_ID,
                     HssSubscriptionItem.JSON_PROPERTY_SUBSCRIPTION_ID,
                     HssSubscriptionItem.JSON_PROPERTY_CONTEXT_INFO })
public class HssSubscriptionItem
{
    public static final String JSON_PROPERTY_HSS_INSTANCE_ID = "hssInstanceId";
    private UUID hssInstanceId;

    public static final String JSON_PROPERTY_SUBSCRIPTION_ID = "subscriptionId";
    private String subscriptionId;

    public static final String JSON_PROPERTY_CONTEXT_INFO = "contextInfo";
    private ContextInfo contextInfo;

    public HssSubscriptionItem()
    {
    }

    public HssSubscriptionItem hssInstanceId(UUID hssInstanceId)
    {

        this.hssInstanceId = hssInstanceId;
        return this;
    }

    /**
     * String uniquely identifying a NF instance. The format of the NF Instance ID
     * shall be a Universally Unique Identifier (UUID) version 4, as described in
     * IETF RFC 4122.
     * 
     * @return hssInstanceId
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true,
                      value = "String uniquely identifying a NF instance. The format of the NF Instance ID shall be a  Universally Unique Identifier (UUID) version 4, as described in IETF RFC 4122.  ")
    @JsonProperty(JSON_PROPERTY_HSS_INSTANCE_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public UUID getHssInstanceId()
    {
        return hssInstanceId;
    }

    @JsonProperty(JSON_PROPERTY_HSS_INSTANCE_ID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setHssInstanceId(UUID hssInstanceId)
    {
        this.hssInstanceId = hssInstanceId;
    }

    public HssSubscriptionItem subscriptionId(String subscriptionId)
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

    public HssSubscriptionItem contextInfo(ContextInfo contextInfo)
    {

        this.contextInfo = contextInfo;
        return this;
    }

    /**
     * Get contextInfo
     * 
     * @return contextInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_CONTEXT_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public ContextInfo getContextInfo()
    {
        return contextInfo;
    }

    @JsonProperty(JSON_PROPERTY_CONTEXT_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setContextInfo(ContextInfo contextInfo)
    {
        this.contextInfo = contextInfo;
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
        HssSubscriptionItem hssSubscriptionItem = (HssSubscriptionItem) o;
        return Objects.equals(this.hssInstanceId, hssSubscriptionItem.hssInstanceId) && Objects.equals(this.subscriptionId, hssSubscriptionItem.subscriptionId)
               && Objects.equals(this.contextInfo, hssSubscriptionItem.contextInfo);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(hssInstanceId, subscriptionId, contextInfo);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class HssSubscriptionItem {\n");
        sb.append("    hssInstanceId: ").append(toIndentedString(hssInstanceId)).append("\n");
        sb.append("    subscriptionId: ").append(toIndentedString(subscriptionId)).append("\n");
        sb.append("    contextInfo: ").append(toIndentedString(contextInfo)).append("\n");
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
