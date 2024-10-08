/*
 * Common Data Types
 * Common Data Types for Service Based Interfaces.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved.   
 *
 * The version of the OpenAPI document: 1.4.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29571.commondata;

import java.util.Objects;
import java.util.Arrays;

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
 * This data type is defined in the same way as the &#39;TnapId&#39; data type,
 * but with the OpenAPI &#39;nullable: true&#39; property.
 */
@ApiModel(description = "This data type is defined in the same way as the 'TnapId' data type, but with the OpenAPI 'nullable: true' property.  ")
@JsonPropertyOrder({ TnapIdRm.JSON_PROPERTY_SS_ID, TnapIdRm.JSON_PROPERTY_BSS_ID, TnapIdRm.JSON_PROPERTY_CIVIC_ADDRESS })
public class TnapIdRm
{
    public static final String JSON_PROPERTY_SS_ID = "ssId";
    private String ssId;

    public static final String JSON_PROPERTY_BSS_ID = "bssId";
    private String bssId;

    public static final String JSON_PROPERTY_CIVIC_ADDRESS = "civicAddress";
    private byte[] civicAddress;

    public TnapIdRm()
    {
    }

    public TnapIdRm ssId(String ssId)
    {

        this.ssId = ssId;
        return this;
    }

    /**
     * This IE shall be present if the UE is accessing the 5GC via a trusted WLAN
     * access network.When present, it shall contain the SSID of the access point to
     * which the UE is attached, that is received over NGAP, see IEEE Std
     * 802.11-2012.
     * 
     * @return ssId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "This IE shall be present if the UE is accessing the 5GC via a trusted WLAN access network.When present, it shall contain the SSID of the access point to which the UE is attached, that is received over NGAP,  see IEEE Std 802.11-2012.  ")
    @JsonProperty(JSON_PROPERTY_SS_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getSsId()
    {
        return ssId;
    }

    @JsonProperty(JSON_PROPERTY_SS_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSsId(String ssId)
    {
        this.ssId = ssId;
    }

    public TnapIdRm bssId(String bssId)
    {

        this.bssId = bssId;
        return this;
    }

    /**
     * When present, it shall contain the BSSID of the access point to which the UE
     * is attached, that is received over NGAP, see IEEE Std 802.11-2012.
     * 
     * @return bssId
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "When present, it shall contain the BSSID of the access point to which the UE is attached, that is received over NGAP, see IEEE Std 802.11-2012.  ")
    @JsonProperty(JSON_PROPERTY_BSS_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getBssId()
    {
        return bssId;
    }

    @JsonProperty(JSON_PROPERTY_BSS_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBssId(String bssId)
    {
        this.bssId = bssId;
    }

    public TnapIdRm civicAddress(byte[] civicAddress)
    {

        this.civicAddress = civicAddress;
        return this;
    }

    /**
     * string with format &#39;bytes&#39; as defined in OpenAPI
     * 
     * @return civicAddress
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "string with format 'bytes' as defined in OpenAPI")
    @JsonProperty(JSON_PROPERTY_CIVIC_ADDRESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public byte[] getCivicAddress()
    {
        return civicAddress;
    }

    @JsonProperty(JSON_PROPERTY_CIVIC_ADDRESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCivicAddress(byte[] civicAddress)
    {
        this.civicAddress = civicAddress;
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
        TnapIdRm tnapIdRm = (TnapIdRm) o;
        return Objects.equals(this.ssId, tnapIdRm.ssId) && Objects.equals(this.bssId, tnapIdRm.bssId)
               && Arrays.equals(this.civicAddress, tnapIdRm.civicAddress);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(ssId, bssId, Arrays.hashCode(civicAddress));
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class TnapIdRm {\n");
        sb.append("    ssId: ").append(toIndentedString(ssId)).append("\n");
        sb.append("    bssId: ").append(toIndentedString(bssId)).append("\n");
        sb.append("    civicAddress: ").append(toIndentedString(civicAddress)).append("\n");
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
