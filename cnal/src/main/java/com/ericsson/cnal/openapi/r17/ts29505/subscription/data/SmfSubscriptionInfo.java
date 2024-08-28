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
 * Information related to active subscriptions at the SMF(s)
 */
@ApiModel(description = "Information related to active subscriptions at the SMF(s)")
@JsonPropertyOrder({ SmfSubscriptionInfo.JSON_PROPERTY_SMF_SUBSCRIPTION_LIST })
public class SmfSubscriptionInfo
{
    public static final String JSON_PROPERTY_SMF_SUBSCRIPTION_LIST = "smfSubscriptionList";
    private List<SmfSubscriptionItem> smfSubscriptionList = new ArrayList<>();

    public SmfSubscriptionInfo()
    {
    }

    public SmfSubscriptionInfo smfSubscriptionList(List<SmfSubscriptionItem> smfSubscriptionList)
    {

        this.smfSubscriptionList = smfSubscriptionList;
        return this;
    }

    public SmfSubscriptionInfo addSmfSubscriptionListItem(SmfSubscriptionItem smfSubscriptionListItem)
    {
        this.smfSubscriptionList.add(smfSubscriptionListItem);
        return this;
    }

    /**
     * Get smfSubscriptionList
     * 
     * @return smfSubscriptionList
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_SMF_SUBSCRIPTION_LIST)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public List<SmfSubscriptionItem> getSmfSubscriptionList()
    {
        return smfSubscriptionList;
    }

    @JsonProperty(JSON_PROPERTY_SMF_SUBSCRIPTION_LIST)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setSmfSubscriptionList(List<SmfSubscriptionItem> smfSubscriptionList)
    {
        this.smfSubscriptionList = smfSubscriptionList;
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
        SmfSubscriptionInfo smfSubscriptionInfo = (SmfSubscriptionInfo) o;
        return Objects.equals(this.smfSubscriptionList, smfSubscriptionInfo.smfSubscriptionList);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(smfSubscriptionList);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class SmfSubscriptionInfo {\n");
        sb.append("    smfSubscriptionList: ").append(toIndentedString(smfSubscriptionList)).append("\n");
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
