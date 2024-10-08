/*
 * NSSF NS Selection
 * NSSF Network Slice Selection Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 2.2.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29531.nnssf.nsselection;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.Snssai;
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
 * Contains the subscribed S-NSSAI
 */
@ApiModel(description = "Contains the subscribed S-NSSAI")
@JsonPropertyOrder({ SubscribedSnssai.JSON_PROPERTY_SUBSCRIBED_SNSSAI,
                     SubscribedSnssai.JSON_PROPERTY_DEFAULT_INDICATION,
                     SubscribedSnssai.JSON_PROPERTY_SUBSCRIBED_NS_SRG_LIST })
public class SubscribedSnssai
{
    public static final String JSON_PROPERTY_SUBSCRIBED_SNSSAI = "subscribedSnssai";
    private Snssai subscribedSnssai;

    public static final String JSON_PROPERTY_DEFAULT_INDICATION = "defaultIndication";
    private Boolean defaultIndication;

    public static final String JSON_PROPERTY_SUBSCRIBED_NS_SRG_LIST = "subscribedNsSrgList";
    private List<String> subscribedNsSrgList = null;

    public SubscribedSnssai()
    {
    }

    public SubscribedSnssai subscribedSnssai(Snssai subscribedSnssai)
    {

        this.subscribedSnssai = subscribedSnssai;
        return this;
    }

    /**
     * Get subscribedSnssai
     * 
     * @return subscribedSnssai
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_SUBSCRIBED_SNSSAI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Snssai getSubscribedSnssai()
    {
        return subscribedSnssai;
    }

    @JsonProperty(JSON_PROPERTY_SUBSCRIBED_SNSSAI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setSubscribedSnssai(Snssai subscribedSnssai)
    {
        this.subscribedSnssai = subscribedSnssai;
    }

    public SubscribedSnssai defaultIndication(Boolean defaultIndication)
    {

        this.defaultIndication = defaultIndication;
        return this;
    }

    /**
     * Get defaultIndication
     * 
     * @return defaultIndication
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_DEFAULT_INDICATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getDefaultIndication()
    {
        return defaultIndication;
    }

    @JsonProperty(JSON_PROPERTY_DEFAULT_INDICATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDefaultIndication(Boolean defaultIndication)
    {
        this.defaultIndication = defaultIndication;
    }

    public SubscribedSnssai subscribedNsSrgList(List<String> subscribedNsSrgList)
    {

        this.subscribedNsSrgList = subscribedNsSrgList;
        return this;
    }

    public SubscribedSnssai addSubscribedNsSrgListItem(String subscribedNsSrgListItem)
    {
        if (this.subscribedNsSrgList == null)
        {
            this.subscribedNsSrgList = new ArrayList<>();
        }
        this.subscribedNsSrgList.add(subscribedNsSrgListItem);
        return this;
    }

    /**
     * Get subscribedNsSrgList
     * 
     * @return subscribedNsSrgList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SUBSCRIBED_NS_SRG_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getSubscribedNsSrgList()
    {
        return subscribedNsSrgList;
    }

    @JsonProperty(JSON_PROPERTY_SUBSCRIBED_NS_SRG_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSubscribedNsSrgList(List<String> subscribedNsSrgList)
    {
        this.subscribedNsSrgList = subscribedNsSrgList;
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
        SubscribedSnssai subscribedSnssai = (SubscribedSnssai) o;
        return Objects.equals(this.subscribedSnssai, subscribedSnssai.subscribedSnssai)
               && Objects.equals(this.defaultIndication, subscribedSnssai.defaultIndication)
               && Objects.equals(this.subscribedNsSrgList, subscribedSnssai.subscribedNsSrgList);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(subscribedSnssai, defaultIndication, subscribedNsSrgList);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class SubscribedSnssai {\n");
        sb.append("    subscribedSnssai: ").append(toIndentedString(subscribedSnssai)).append("\n");
        sb.append("    defaultIndication: ").append(toIndentedString(defaultIndication)).append("\n");
        sb.append("    subscribedNsSrgList: ").append(toIndentedString(subscribedNsSrgList)).append("\n");
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
