/*
 * Nsmf_PDUSession
 * SMF PDU Session Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29502.nsmf.pdusession;

import java.util.Objects;
import java.util.Arrays;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.AccessType;
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
 * Tunnel Information
 */
@ApiModel(description = "Tunnel Information")
@JsonPropertyOrder({ TunnelInfo.JSON_PROPERTY_IPV4_ADDR,
                     TunnelInfo.JSON_PROPERTY_IPV6_ADDR,
                     TunnelInfo.JSON_PROPERTY_GTP_TEID,
                     TunnelInfo.JSON_PROPERTY_AN_TYPE })
public class TunnelInfo
{
    public static final String JSON_PROPERTY_IPV4_ADDR = "ipv4Addr";
    private String ipv4Addr;

    public static final String JSON_PROPERTY_IPV6_ADDR = "ipv6Addr";
    private String ipv6Addr;

    public static final String JSON_PROPERTY_GTP_TEID = "gtpTeid";
    private String gtpTeid;

    public static final String JSON_PROPERTY_AN_TYPE = "anType";
    private AccessType anType;

    public TunnelInfo()
    {
    }

    public TunnelInfo ipv4Addr(String ipv4Addr)
    {

        this.ipv4Addr = ipv4Addr;
        return this;
    }

    /**
     * String identifying a IPv4 address formatted in the &#39;dotted decimal&#39;
     * notation as defined in RFC 1166.
     * 
     * @return ipv4Addr
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(example = "198.51.100.1", value = "String identifying a IPv4 address formatted in the 'dotted decimal' notation as defined in RFC 1166. ")
    @JsonProperty(JSON_PROPERTY_IPV4_ADDR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getIpv4Addr()
    {
        return ipv4Addr;
    }

    @JsonProperty(JSON_PROPERTY_IPV4_ADDR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIpv4Addr(String ipv4Addr)
    {
        this.ipv4Addr = ipv4Addr;
    }

    public TunnelInfo ipv6Addr(String ipv6Addr)
    {

        this.ipv6Addr = ipv6Addr;
        return this;
    }

    /**
     * Get ipv6Addr
     * 
     * @return ipv6Addr
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_IPV6_ADDR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getIpv6Addr()
    {
        return ipv6Addr;
    }

    @JsonProperty(JSON_PROPERTY_IPV6_ADDR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIpv6Addr(String ipv6Addr)
    {
        this.ipv6Addr = ipv6Addr;
    }

    public TunnelInfo gtpTeid(String gtpTeid)
    {

        this.gtpTeid = gtpTeid;
        return this;
    }

    /**
     * GTP Tunnel Endpoint Identifier
     * 
     * @return gtpTeid
     **/
    @javax.annotation.Nonnull
    @ApiModelProperty(required = true, value = "GTP Tunnel Endpoint Identifier")
    @JsonProperty(JSON_PROPERTY_GTP_TEID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getGtpTeid()
    {
        return gtpTeid;
    }

    @JsonProperty(JSON_PROPERTY_GTP_TEID)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setGtpTeid(String gtpTeid)
    {
        this.gtpTeid = gtpTeid;
    }

    public TunnelInfo anType(AccessType anType)
    {

        this.anType = anType;
        return this;
    }

    /**
     * Get anType
     * 
     * @return anType
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_AN_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public AccessType getAnType()
    {
        return anType;
    }

    @JsonProperty(JSON_PROPERTY_AN_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAnType(AccessType anType)
    {
        this.anType = anType;
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
        TunnelInfo tunnelInfo = (TunnelInfo) o;
        return Objects.equals(this.ipv4Addr, tunnelInfo.ipv4Addr) && Objects.equals(this.ipv6Addr, tunnelInfo.ipv6Addr)
               && Objects.equals(this.gtpTeid, tunnelInfo.gtpTeid) && Objects.equals(this.anType, tunnelInfo.anType);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(ipv4Addr, ipv6Addr, gtpTeid, anType);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class TunnelInfo {\n");
        sb.append("    ipv4Addr: ").append(toIndentedString(ipv4Addr)).append("\n");
        sb.append("    ipv6Addr: ").append(toIndentedString(ipv6Addr)).append("\n");
        sb.append("    gtpTeid: ").append(toIndentedString(gtpTeid)).append("\n");
        sb.append("    anType: ").append(toIndentedString(anType)).append("\n");
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
