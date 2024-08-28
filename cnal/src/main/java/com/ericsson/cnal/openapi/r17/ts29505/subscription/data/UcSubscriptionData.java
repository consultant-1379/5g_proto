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
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Contains the User Consent Subscription Data.
 */
@ApiModel(description = "Contains the User Consent Subscription Data.")
@JsonPropertyOrder({ UcSubscriptionData.JSON_PROPERTY_USER_CONSENT_PER_PURPOSE_LIST })
public class UcSubscriptionData
{
    public static final String JSON_PROPERTY_USER_CONSENT_PER_PURPOSE_LIST = "userConsentPerPurposeList";
    private Map<String, String> userConsentPerPurposeList = null;

    public UcSubscriptionData()
    {
    }

    public UcSubscriptionData userConsentPerPurposeList(Map<String, String> userConsentPerPurposeList)
    {

        this.userConsentPerPurposeList = userConsentPerPurposeList;
        return this;
    }

    public UcSubscriptionData putUserConsentPerPurposeListItem(String key,
                                                               String userConsentPerPurposeListItem)
    {
        if (this.userConsentPerPurposeList == null)
        {
            this.userConsentPerPurposeList = new HashMap<>();
        }
        this.userConsentPerPurposeList.put(key, userConsentPerPurposeListItem);
        return this;
    }

    /**
     * A map(list of key-value pairs) where user consent purpose serves as key of
     * user consent
     * 
     * @return userConsentPerPurposeList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "A map(list of key-value pairs) where user consent purpose serves as key of user consent")
    @JsonProperty(JSON_PROPERTY_USER_CONSENT_PER_PURPOSE_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Map<String, String> getUserConsentPerPurposeList()
    {
        return userConsentPerPurposeList;
    }

    @JsonProperty(JSON_PROPERTY_USER_CONSENT_PER_PURPOSE_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUserConsentPerPurposeList(Map<String, String> userConsentPerPurposeList)
    {
        this.userConsentPerPurposeList = userConsentPerPurposeList;
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
        UcSubscriptionData ucSubscriptionData = (UcSubscriptionData) o;
        return Objects.equals(this.userConsentPerPurposeList, ucSubscriptionData.userConsentPerPurposeList);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(userConsentPerPurposeList);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class UcSubscriptionData {\n");
        sb.append("    userConsentPerPurposeList: ").append(toIndentedString(userConsentPerPurposeList)).append("\n");
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
