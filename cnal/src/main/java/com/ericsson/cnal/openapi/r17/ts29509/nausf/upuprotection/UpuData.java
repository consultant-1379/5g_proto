/*
 * Nausf_UPUProtection Service
 * AUSF UPU Protection Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29509.nausf.upuprotection;

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
 * Contains UE parameters update data set (e.g., the updated Routing ID Data or
 * the Default configured NSSAI).
 */
@ApiModel(description = "Contains UE parameters update data set (e.g., the updated Routing ID Data or the Default configured NSSAI).")
@JsonPropertyOrder({ UpuData.JSON_PROPERTY_SEC_PACKET, UpuData.JSON_PROPERTY_DEFAULT_CONF_NSSAI, UpuData.JSON_PROPERTY_ROUTING_ID })
public class UpuData
{
    public static final String JSON_PROPERTY_SEC_PACKET = "secPacket";
    private byte[] secPacket;

    public static final String JSON_PROPERTY_DEFAULT_CONF_NSSAI = "defaultConfNssai";
    private List<Snssai> defaultConfNssai = null;

    public static final String JSON_PROPERTY_ROUTING_ID = "routingId";
    private String routingId;

    public UpuData()
    {
    }

    public UpuData secPacket(byte[] secPacket)
    {

        this.secPacket = secPacket;
        return this;
    }

    /**
     * Contains a secure packet.
     * 
     * @return secPacket
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Contains a secure packet.")
    @JsonProperty(JSON_PROPERTY_SEC_PACKET)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public byte[] getSecPacket()
    {
        return secPacket;
    }

    @JsonProperty(JSON_PROPERTY_SEC_PACKET)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSecPacket(byte[] secPacket)
    {
        this.secPacket = secPacket;
    }

    public UpuData defaultConfNssai(List<Snssai> defaultConfNssai)
    {

        this.defaultConfNssai = defaultConfNssai;
        return this;
    }

    public UpuData addDefaultConfNssaiItem(Snssai defaultConfNssaiItem)
    {
        if (this.defaultConfNssai == null)
        {
            this.defaultConfNssai = new ArrayList<>();
        }
        this.defaultConfNssai.add(defaultConfNssaiItem);
        return this;
    }

    /**
     * Get defaultConfNssai
     * 
     * @return defaultConfNssai
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_DEFAULT_CONF_NSSAI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<Snssai> getDefaultConfNssai()
    {
        return defaultConfNssai;
    }

    @JsonProperty(JSON_PROPERTY_DEFAULT_CONF_NSSAI)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDefaultConfNssai(List<Snssai> defaultConfNssai)
    {
        this.defaultConfNssai = defaultConfNssai;
    }

    public UpuData routingId(String routingId)
    {

        this.routingId = routingId;
        return this;
    }

    /**
     * Represents a routing indicator.
     * 
     * @return routingId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Represents a routing indicator.")
    @JsonProperty(JSON_PROPERTY_ROUTING_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getRoutingId()
    {
        return routingId;
    }

    @JsonProperty(JSON_PROPERTY_ROUTING_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRoutingId(String routingId)
    {
        this.routingId = routingId;
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
        UpuData upuData = (UpuData) o;
        return Arrays.equals(this.secPacket, upuData.secPacket) && Objects.equals(this.defaultConfNssai, upuData.defaultConfNssai)
               && Objects.equals(this.routingId, upuData.routingId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(Arrays.hashCode(secPacket), defaultConfNssai, routingId);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class UpuData {\n");
        sb.append("    secPacket: ").append(toIndentedString(secPacket)).append("\n");
        sb.append("    defaultConfNssai: ").append(toIndentedString(defaultConfNssai)).append("\n");
        sb.append("    routingId: ").append(toIndentedString(routingId)).append("\n");
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
