/*
 * Nnwdaf_EventsSubscription
 * Nnwdaf_EventsSubscription Service API.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29520.nnwdaf.eventssubscription;

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
 * Represents other WLAN performance analytics requirements.
 */
@ApiModel(description = "Represents other WLAN performance analytics requirements.")
@JsonPropertyOrder({ WlanPerformanceReq.JSON_PROPERTY_SS_IDS,
                     WlanPerformanceReq.JSON_PROPERTY_BSS_IDS,
                     WlanPerformanceReq.JSON_PROPERTY_WLAN_ORDER_CRITER,
                     WlanPerformanceReq.JSON_PROPERTY_ORDER })
public class WlanPerformanceReq
{
    public static final String JSON_PROPERTY_SS_IDS = "ssIds";
    private List<String> ssIds = null;

    public static final String JSON_PROPERTY_BSS_IDS = "bssIds";
    private List<String> bssIds = null;

    public static final String JSON_PROPERTY_WLAN_ORDER_CRITER = "wlanOrderCriter";
    private String wlanOrderCriter;

    public static final String JSON_PROPERTY_ORDER = "order";
    private String order;

    public WlanPerformanceReq()
    {
    }

    public WlanPerformanceReq ssIds(List<String> ssIds)
    {

        this.ssIds = ssIds;
        return this;
    }

    public WlanPerformanceReq addSsIdsItem(String ssIdsItem)
    {
        if (this.ssIds == null)
        {
            this.ssIds = new ArrayList<>();
        }
        this.ssIds.add(ssIdsItem);
        return this;
    }

    /**
     * Get ssIds
     * 
     * @return ssIds
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SS_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getSsIds()
    {
        return ssIds;
    }

    @JsonProperty(JSON_PROPERTY_SS_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSsIds(List<String> ssIds)
    {
        this.ssIds = ssIds;
    }

    public WlanPerformanceReq bssIds(List<String> bssIds)
    {

        this.bssIds = bssIds;
        return this;
    }

    public WlanPerformanceReq addBssIdsItem(String bssIdsItem)
    {
        if (this.bssIds == null)
        {
            this.bssIds = new ArrayList<>();
        }
        this.bssIds.add(bssIdsItem);
        return this;
    }

    /**
     * Get bssIds
     * 
     * @return bssIds
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_BSS_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getBssIds()
    {
        return bssIds;
    }

    @JsonProperty(JSON_PROPERTY_BSS_IDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBssIds(List<String> bssIds)
    {
        this.bssIds = bssIds;
    }

    public WlanPerformanceReq wlanOrderCriter(String wlanOrderCriter)
    {

        this.wlanOrderCriter = wlanOrderCriter;
        return this;
    }

    /**
     * Possible values are: - TIME_SLOT_START: Indicates the order of time slot
     * start. - NUMBER_OF_UES: Indicates the order of number of UEs. - RSSI:
     * Indicates the order of RSSI. - RTT: Indicates the order of RTT. -
     * TRAFFIC_INFO: Indicates the order of Traffic information.
     * 
     * @return wlanOrderCriter
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Possible values are: - TIME_SLOT_START: Indicates the order of time slot start. - NUMBER_OF_UES: Indicates the order of number of UEs. - RSSI: Indicates the order of RSSI. - RTT: Indicates the order of RTT. - TRAFFIC_INFO: Indicates the order of Traffic information. ")
    @JsonProperty(JSON_PROPERTY_WLAN_ORDER_CRITER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getWlanOrderCriter()
    {
        return wlanOrderCriter;
    }

    @JsonProperty(JSON_PROPERTY_WLAN_ORDER_CRITER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setWlanOrderCriter(String wlanOrderCriter)
    {
        this.wlanOrderCriter = wlanOrderCriter;
    }

    public WlanPerformanceReq order(String order)
    {

        this.order = order;
        return this;
    }

    /**
     * Possible values are: - ASCENDING: Threshold is crossed in ascending
     * direction. - DESCENDING: Threshold is crossed in descending direction. -
     * CROSSED: Threshold is crossed either in ascending or descending direction.
     * 
     * @return order
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Possible values are: - ASCENDING: Threshold is crossed in ascending direction. - DESCENDING: Threshold is crossed in descending direction. - CROSSED: Threshold is crossed either in ascending or descending direction. ")
    @JsonProperty(JSON_PROPERTY_ORDER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getOrder()
    {
        return order;
    }

    @JsonProperty(JSON_PROPERTY_ORDER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setOrder(String order)
    {
        this.order = order;
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
        WlanPerformanceReq wlanPerformanceReq = (WlanPerformanceReq) o;
        return Objects.equals(this.ssIds, wlanPerformanceReq.ssIds) && Objects.equals(this.bssIds, wlanPerformanceReq.bssIds)
               && Objects.equals(this.wlanOrderCriter, wlanPerformanceReq.wlanOrderCriter) && Objects.equals(this.order, wlanPerformanceReq.order);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(ssIds, bssIds, wlanOrderCriter, order);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class WlanPerformanceReq {\n");
        sb.append("    ssIds: ").append(toIndentedString(ssIds)).append("\n");
        sb.append("    bssIds: ").append(toIndentedString(bssIds)).append("\n");
        sb.append("    wlanOrderCriter: ").append(toIndentedString(wlanOrderCriter)).append("\n");
        sb.append("    order: ").append(toIndentedString(order)).append("\n");
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
