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
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Data within a N2 information notification response
 */
@ApiModel(description = "Data within a N2 information notification response")
@JsonPropertyOrder({ N2InfoNotificationRspData.JSON_PROPERTY_SEC_RAT_DATA_USAGE_LIST })
public class N2InfoNotificationRspData
{
    public static final String JSON_PROPERTY_SEC_RAT_DATA_USAGE_LIST = "secRatDataUsageList";
    private List<N2SmInformation> secRatDataUsageList = null;

    public N2InfoNotificationRspData()
    {
    }

    public N2InfoNotificationRspData secRatDataUsageList(List<N2SmInformation> secRatDataUsageList)
    {

        this.secRatDataUsageList = secRatDataUsageList;
        return this;
    }

    public N2InfoNotificationRspData addSecRatDataUsageListItem(N2SmInformation secRatDataUsageListItem)
    {
        if (this.secRatDataUsageList == null)
        {
            this.secRatDataUsageList = new ArrayList<>();
        }
        this.secRatDataUsageList.add(secRatDataUsageListItem);
        return this;
    }

    /**
     * Get secRatDataUsageList
     * 
     * @return secRatDataUsageList
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SEC_RAT_DATA_USAGE_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<N2SmInformation> getSecRatDataUsageList()
    {
        return secRatDataUsageList;
    }

    @JsonProperty(JSON_PROPERTY_SEC_RAT_DATA_USAGE_LIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSecRatDataUsageList(List<N2SmInformation> secRatDataUsageList)
    {
        this.secRatDataUsageList = secRatDataUsageList;
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
        N2InfoNotificationRspData n2InfoNotificationRspData = (N2InfoNotificationRspData) o;
        return Objects.equals(this.secRatDataUsageList, n2InfoNotificationRspData.secRatDataUsageList);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(secRatDataUsageList);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class N2InfoNotificationRspData {\n");
        sb.append("    secRatDataUsageList: ").append(toIndentedString(secRatDataUsageList)).append("\n");
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
