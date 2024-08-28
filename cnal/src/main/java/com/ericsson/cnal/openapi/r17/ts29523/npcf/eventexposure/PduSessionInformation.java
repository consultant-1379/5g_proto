/*
 * Npcf_EventExposure
 * PCF Event Exposure Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29523.npcf.eventexposure;

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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Represents PDU session identification information.
 */
@ApiModel(description = "Represents PDU session identification information.")
@JsonPropertyOrder({ PduSessionInformation.JSON_PROPERTY_SNSSAI,
                     PduSessionInformation.JSON_PROPERTY_DNN,
                     PduSessionInformation.JSON_PROPERTY_UE_IPV4,
                     PduSessionInformation.JSON_PROPERTY_UE_IPV6,
                     PduSessionInformation.JSON_PROPERTY_IP_DOMAIN,
                     PduSessionInformation.JSON_PROPERTY_UE_MAC })
public class PduSessionInformation
{
    public static final String JSON_PROPERTY_SNSSAI = "snssai";
    private Snssai snssai;

    public static final String JSON_PROPERTY_DNN = "dnn";
    private String dnn;

    public static final String JSON_PROPERTY_UE_IPV4 = "ueIpv4";
    private String ueIpv4;

    public static final String JSON_PROPERTY_UE_IPV6 = "ueIpv6";
    private String ueIpv6;

    public static final String JSON_PROPERTY_IP_DOMAIN = "ipDomain";
    private String ipDomain;

    public static final String JSON_PROPERTY_UE_MAC = "ueMac";
    private String ueMac;

    public PduSessionInformation()
    {
    }

    public PduSessionInformation snssai(Snssai snssai)
    {

        this.snssai = snssai;
        return this;
    }

    /**
     * Get snssai
     * 
     * @return snssai
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "")
    @JsonProperty(JSON_PROPERTY_SNSSAI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public Snssai getSnssai()
    {
        return snssai;
    }

    @JsonProperty(JSON_PROPERTY_SNSSAI)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setSnssai(Snssai snssai)
    {
        this.snssai = snssai;
    }

    public PduSessionInformation dnn(String dnn)
    {

        this.dnn = dnn;
        return this;
    }

    /**
     * String representing a Data Network as defined in clause 9A of 3GPP TS 23.003;
     * it shall contain either a DNN Network Identifier, or a full DNN with both the
     * Network Identifier and Operator Identifier, as specified in 3GPP TS 23.003
     * clause 9.1.1 and 9.1.2. It shall be coded as string in which the labels are
     * separated by dots (e.g. \&quot;Label1.Label2.Label3\&quot;).
     * 
     * @return dnn
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true,
                      value = "String representing a Data Network as defined in clause 9A of 3GPP TS 23.003;  it shall contain either a DNN Network Identifier, or a full DNN with both the Network  Identifier and Operator Identifier, as specified in 3GPP TS 23.003 clause 9.1.1 and 9.1.2. It shall be coded as string in which the labels are separated by dots  (e.g. \"Label1.Label2.Label3\"). ")
    @JsonProperty(JSON_PROPERTY_DNN)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getDnn()
    {
        return dnn;
    }

    @JsonProperty(JSON_PROPERTY_DNN)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setDnn(String dnn)
    {
        this.dnn = dnn;
    }

    public PduSessionInformation ueIpv4(String ueIpv4)
    {

        this.ueIpv4 = ueIpv4;
        return this;
    }

    /**
     * String identifying a IPv4 address formatted in the &#39;dotted decimal&#39;
     * notation as defined in RFC 1166.
     * 
     * @return ueIpv4
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(example = "198.51.100.1", value = "String identifying a IPv4 address formatted in the 'dotted decimal' notation as defined in RFC 1166. ")
    @JsonProperty(JSON_PROPERTY_UE_IPV4)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getUeIpv4()
    {
        return ueIpv4;
    }

    @JsonProperty(JSON_PROPERTY_UE_IPV4)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUeIpv4(String ueIpv4)
    {
        this.ueIpv4 = ueIpv4;
    }

    public PduSessionInformation ueIpv6(String ueIpv6)
    {

        this.ueIpv6 = ueIpv6;
        return this;
    }

    /**
     * Get ueIpv6
     * 
     * @return ueIpv6
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_UE_IPV6)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getUeIpv6()
    {
        return ueIpv6;
    }

    @JsonProperty(JSON_PROPERTY_UE_IPV6)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUeIpv6(String ueIpv6)
    {
        this.ueIpv6 = ueIpv6;
    }

    public PduSessionInformation ipDomain(String ipDomain)
    {

        this.ipDomain = ipDomain;
        return this;
    }

    /**
     * Get ipDomain
     * 
     * @return ipDomain
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_IP_DOMAIN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getIpDomain()
    {
        return ipDomain;
    }

    @JsonProperty(JSON_PROPERTY_IP_DOMAIN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIpDomain(String ipDomain)
    {
        this.ipDomain = ipDomain;
    }

    public PduSessionInformation ueMac(String ueMac)
    {

        this.ueMac = ueMac;
        return this;
    }

    /**
     * String identifying a MAC address formatted in the hexadecimal notation
     * according to clause 1.1 and clause 2.1 of RFC 7042.
     * 
     * @return ueMac
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "String identifying a MAC address formatted in the hexadecimal notation according to clause 1.1 and clause 2.1 of RFC 7042. ")
    @JsonProperty(JSON_PROPERTY_UE_MAC)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getUeMac()
    {
        return ueMac;
    }

    @JsonProperty(JSON_PROPERTY_UE_MAC)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUeMac(String ueMac)
    {
        this.ueMac = ueMac;
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
        PduSessionInformation pduSessionInformation = (PduSessionInformation) o;
        return Objects.equals(this.snssai, pduSessionInformation.snssai) && Objects.equals(this.dnn, pduSessionInformation.dnn)
               && Objects.equals(this.ueIpv4, pduSessionInformation.ueIpv4) && Objects.equals(this.ueIpv6, pduSessionInformation.ueIpv6)
               && Objects.equals(this.ipDomain, pduSessionInformation.ipDomain) && Objects.equals(this.ueMac, pduSessionInformation.ueMac);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(snssai, dnn, ueIpv4, ueIpv6, ipDomain, ueMac);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class PduSessionInformation {\n");
        sb.append("    snssai: ").append(toIndentedString(snssai)).append("\n");
        sb.append("    dnn: ").append(toIndentedString(dnn)).append("\n");
        sb.append("    ueIpv4: ").append(toIndentedString(ueIpv4)).append("\n");
        sb.append("    ueIpv6: ").append(toIndentedString(ueIpv6)).append("\n");
        sb.append("    ipDomain: ").append(toIndentedString(ipDomain)).append("\n");
        sb.append("    ueMac: ").append(toIndentedString(ueMac)).append("\n");
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
