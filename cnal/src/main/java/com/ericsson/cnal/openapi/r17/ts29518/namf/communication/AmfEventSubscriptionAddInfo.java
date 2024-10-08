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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Additional information received for an AMF event subscription, e.g. binding
 * indications
 */
@ApiModel(description = "Additional information received for an AMF event subscription, e.g. binding indications")
@JsonPropertyOrder({ AmfEventSubscriptionAddInfo.JSON_PROPERTY_BINDING_INFO,
                     AmfEventSubscriptionAddInfo.JSON_PROPERTY_SUBSCRIBING_NF_TYPE,
                     AmfEventSubscriptionAddInfo.JSON_PROPERTY_EVENT_SYNC_IND,
                     AmfEventSubscriptionAddInfo.JSON_PROPERTY_NF_CONSUMER_INFO,
                     AmfEventSubscriptionAddInfo.JSON_PROPERTY_AOI_STATE_LIST })
public class AmfEventSubscriptionAddInfo
{
    public static final String JSON_PROPERTY_BINDING_INFO = "bindingInfo";
    private List<String> bindingInfo = null;

    public static final String JSON_PROPERTY_SUBSCRIBING_NF_TYPE = "subscribingNfType";
    private String subscribingNfType;

    public static final String JSON_PROPERTY_EVENT_SYNC_IND = "eventSyncInd";
    private Boolean eventSyncInd;

    public static final String JSON_PROPERTY_NF_CONSUMER_INFO = "nfConsumerInfo";
    private List<String> nfConsumerInfo = null;

    public static final String JSON_PROPERTY_AOI_STATE_LIST = "aoiStateList";
    private Map<String, AreaOfInterestEventState> aoiStateList = null;

    public AmfEventSubscriptionAddInfo()
    {
    }

    public AmfEventSubscriptionAddInfo bindingInfo(List<String> bindingInfo)
    {

        this.bindingInfo = bindingInfo;
        return this;
    }

    public AmfEventSubscriptionAddInfo addBindingInfoItem(String bindingInfoItem)
    {
        if (this.bindingInfo == null)
        {
            this.bindingInfo = new ArrayList<>();
        }
        this.bindingInfo.add(bindingInfoItem);
        return this;
    }

    /**
     * Get bindingInfo
     * 
     * @return bindingInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_BINDING_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getBindingInfo()
    {
        return bindingInfo;
    }

    @JsonProperty(JSON_PROPERTY_BINDING_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBindingInfo(List<String> bindingInfo)
    {
        this.bindingInfo = bindingInfo;
    }

    public AmfEventSubscriptionAddInfo subscribingNfType(String subscribingNfType)
    {

        this.subscribingNfType = subscribingNfType;
        return this;
    }

    /**
     * NF types known to NRF
     * 
     * @return subscribingNfType
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "NF types known to NRF")
    @JsonProperty(JSON_PROPERTY_SUBSCRIBING_NF_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getSubscribingNfType()
    {
        return subscribingNfType;
    }

    @JsonProperty(JSON_PROPERTY_SUBSCRIBING_NF_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSubscribingNfType(String subscribingNfType)
    {
        this.subscribingNfType = subscribingNfType;
    }

    public AmfEventSubscriptionAddInfo eventSyncInd(Boolean eventSyncInd)
    {

        this.eventSyncInd = eventSyncInd;
        return this;
    }

    /**
     * Get eventSyncInd
     * 
     * @return eventSyncInd
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_EVENT_SYNC_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getEventSyncInd()
    {
        return eventSyncInd;
    }

    @JsonProperty(JSON_PROPERTY_EVENT_SYNC_IND)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEventSyncInd(Boolean eventSyncInd)
    {
        this.eventSyncInd = eventSyncInd;
    }

    public AmfEventSubscriptionAddInfo nfConsumerInfo(List<String> nfConsumerInfo)
    {

        this.nfConsumerInfo = nfConsumerInfo;
        return this;
    }

    public AmfEventSubscriptionAddInfo addNfConsumerInfoItem(String nfConsumerInfoItem)
    {
        if (this.nfConsumerInfo == null)
        {
            this.nfConsumerInfo = new ArrayList<>();
        }
        this.nfConsumerInfo.add(nfConsumerInfoItem);
        return this;
    }

    /**
     * Get nfConsumerInfo
     * 
     * @return nfConsumerInfo
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_NF_CONSUMER_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getNfConsumerInfo()
    {
        return nfConsumerInfo;
    }

    @JsonProperty(JSON_PROPERTY_NF_CONSUMER_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNfConsumerInfo(List<String> nfConsumerInfo)
    {
        this.nfConsumerInfo = nfConsumerInfo;
    }

    public AmfEventSubscriptionAddInfo aoiStateList(Map<String, AreaOfInterestEventState> aoiStateList)
    {

        this.aoiStateList = aoiStateList;
        return this;
    }

    public AmfEventSubscriptionAddInfo putAoiStateListItem(String key,
                                                           AreaOfInterestEventState aoiStateListItem)
    {
        if (this.aoiStateList == null)
        {
            this.aoiStateList = new HashMap<>();
        }
        this.aoiStateList.put(key, aoiStateListItem);
        return this;
    }

    /**
     * Map of subscribed Area of Interest (AoI) Event State in the old AMF. The JSON
     * pointer to an AmfEventArea element in the areaList IE (or a PresenceInfo
     * element in presenceInfoList IE) of the AmfEvent data type shall be the key of
     * the map.
     * 
     * @return aoiStateList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Map of subscribed Area of Interest (AoI) Event State in the old AMF. The JSON pointer to an AmfEventArea element in the areaList IE (or a PresenceInfo element in  presenceInfoList IE) of the AmfEvent data type shall be the key of the map. ")
    @JsonProperty(JSON_PROPERTY_AOI_STATE_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Map<String, AreaOfInterestEventState> getAoiStateList()
    {
        return aoiStateList;
    }

    @JsonProperty(JSON_PROPERTY_AOI_STATE_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAoiStateList(Map<String, AreaOfInterestEventState> aoiStateList)
    {
        this.aoiStateList = aoiStateList;
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
        AmfEventSubscriptionAddInfo amfEventSubscriptionAddInfo = (AmfEventSubscriptionAddInfo) o;
        return Objects.equals(this.bindingInfo, amfEventSubscriptionAddInfo.bindingInfo)
               && Objects.equals(this.subscribingNfType, amfEventSubscriptionAddInfo.subscribingNfType)
               && Objects.equals(this.eventSyncInd, amfEventSubscriptionAddInfo.eventSyncInd)
               && Objects.equals(this.nfConsumerInfo, amfEventSubscriptionAddInfo.nfConsumerInfo)
               && Objects.equals(this.aoiStateList, amfEventSubscriptionAddInfo.aoiStateList);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(bindingInfo, subscribingNfType, eventSyncInd, nfConsumerInfo, aoiStateList);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class AmfEventSubscriptionAddInfo {\n");
        sb.append("    bindingInfo: ").append(toIndentedString(bindingInfo)).append("\n");
        sb.append("    subscribingNfType: ").append(toIndentedString(subscribingNfType)).append("\n");
        sb.append("    eventSyncInd: ").append(toIndentedString(eventSyncInd)).append("\n");
        sb.append("    nfConsumerInfo: ").append(toIndentedString(nfConsumerInfo)).append("\n");
        sb.append("    aoiStateList: ").append(toIndentedString(aoiStateList)).append("\n");
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
