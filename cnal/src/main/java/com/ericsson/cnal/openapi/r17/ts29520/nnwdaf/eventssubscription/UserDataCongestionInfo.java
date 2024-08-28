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
import com.ericsson.cnal.openapi.r17.ts29571.commondata.Snssai;
import com.ericsson.cnal.openapi.r17.ts29554.npcf.bdtpolicycontrol.NetworkAreaInfo;
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
 * Represents the user data congestion information.
 */
@ApiModel(description = "Represents the user data congestion information.")
@JsonPropertyOrder({ UserDataCongestionInfo.JSON_PROPERTY_NETWORK_AREA,
                     UserDataCongestionInfo.JSON_PROPERTY_CONGESTION_INFO,
                     UserDataCongestionInfo.JSON_PROPERTY_SNSSAI })
public class UserDataCongestionInfo
{
    public static final String JSON_PROPERTY_NETWORK_AREA = "networkArea";
    private NetworkAreaInfo networkArea;

    public static final String JSON_PROPERTY_CONGESTION_INFO = "congestionInfo";
    private CongestionInfo congestionInfo;

    public static final String JSON_PROPERTY_SNSSAI = "snssai";
    private Snssai snssai;

    public UserDataCongestionInfo()
    {
    }

    public UserDataCongestionInfo networkArea(NetworkAreaInfo networkArea)
    {

        this.networkArea = networkArea;
        return this;
    }

    /**
     * Get networkArea
     * 
     * @return networkArea
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_NETWORK_AREA)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public NetworkAreaInfo getNetworkArea()
    {
        return networkArea;
    }

    @JsonProperty(JSON_PROPERTY_NETWORK_AREA)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setNetworkArea(NetworkAreaInfo networkArea)
    {
        this.networkArea = networkArea;
    }

    public UserDataCongestionInfo congestionInfo(CongestionInfo congestionInfo)
    {

        this.congestionInfo = congestionInfo;
        return this;
    }

    /**
     * Get congestionInfo
     * 
     * @return congestionInfo
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_CONGESTION_INFO)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public CongestionInfo getCongestionInfo()
    {
        return congestionInfo;
    }

    @JsonProperty(JSON_PROPERTY_CONGESTION_INFO)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setCongestionInfo(CongestionInfo congestionInfo)
    {
        this.congestionInfo = congestionInfo;
    }

    public UserDataCongestionInfo snssai(Snssai snssai)
    {

        this.snssai = snssai;
        return this;
    }

    /**
     * Get snssai
     * 
     * @return snssai
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SNSSAI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Snssai getSnssai()
    {
        return snssai;
    }

    @JsonProperty(JSON_PROPERTY_SNSSAI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSnssai(Snssai snssai)
    {
        this.snssai = snssai;
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
        UserDataCongestionInfo userDataCongestionInfo = (UserDataCongestionInfo) o;
        return Objects.equals(this.networkArea, userDataCongestionInfo.networkArea)
               && Objects.equals(this.congestionInfo, userDataCongestionInfo.congestionInfo) && Objects.equals(this.snssai, userDataCongestionInfo.snssai);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(networkArea, congestionInfo, snssai);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class UserDataCongestionInfo {\n");
        sb.append("    networkArea: ").append(toIndentedString(networkArea)).append("\n");
        sb.append("    congestionInfo: ").append(toIndentedString(congestionInfo)).append("\n");
        sb.append("    snssai: ").append(toIndentedString(snssai)).append("\n");
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
