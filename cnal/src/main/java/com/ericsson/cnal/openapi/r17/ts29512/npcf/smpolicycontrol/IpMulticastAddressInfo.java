/*
 * Npcf_SMPolicyControl API
 * Session Management Policy Control Service   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29512.npcf.smpolicycontrol;

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
 * Contains the IP multicast addressing information.
 */
@ApiModel(description = "Contains the IP multicast addressing information.")
@JsonPropertyOrder({ IpMulticastAddressInfo.JSON_PROPERTY_SRC_IPV4_ADDR,
                     IpMulticastAddressInfo.JSON_PROPERTY_IPV4_MUL_ADDR,
                     IpMulticastAddressInfo.JSON_PROPERTY_SRC_IPV6_ADDR,
                     IpMulticastAddressInfo.JSON_PROPERTY_IPV6_MUL_ADDR })
public class IpMulticastAddressInfo
{
    public static final String JSON_PROPERTY_SRC_IPV4_ADDR = "srcIpv4Addr";
    private String srcIpv4Addr;

    public static final String JSON_PROPERTY_IPV4_MUL_ADDR = "ipv4MulAddr";
    private String ipv4MulAddr;

    public static final String JSON_PROPERTY_SRC_IPV6_ADDR = "srcIpv6Addr";
    private String srcIpv6Addr;

    public static final String JSON_PROPERTY_IPV6_MUL_ADDR = "ipv6MulAddr";
    private String ipv6MulAddr;

    public IpMulticastAddressInfo()
    {
    }

    public IpMulticastAddressInfo srcIpv4Addr(String srcIpv4Addr)
    {

        this.srcIpv4Addr = srcIpv4Addr;
        return this;
    }

    /**
     * String identifying a IPv4 address formatted in the &#39;dotted decimal&#39;
     * notation as defined in RFC 1166.
     * 
     * @return srcIpv4Addr
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(example = "198.51.100.1", value = "String identifying a IPv4 address formatted in the 'dotted decimal' notation as defined in RFC 1166. ")
    @JsonProperty(JSON_PROPERTY_SRC_IPV4_ADDR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getSrcIpv4Addr()
    {
        return srcIpv4Addr;
    }

    @JsonProperty(JSON_PROPERTY_SRC_IPV4_ADDR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSrcIpv4Addr(String srcIpv4Addr)
    {
        this.srcIpv4Addr = srcIpv4Addr;
    }

    public IpMulticastAddressInfo ipv4MulAddr(String ipv4MulAddr)
    {

        this.ipv4MulAddr = ipv4MulAddr;
        return this;
    }

    /**
     * String identifying a IPv4 address formatted in the &#39;dotted decimal&#39;
     * notation as defined in RFC 1166.
     * 
     * @return ipv4MulAddr
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(example = "198.51.100.1", value = "String identifying a IPv4 address formatted in the 'dotted decimal' notation as defined in RFC 1166. ")
    @JsonProperty(JSON_PROPERTY_IPV4_MUL_ADDR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getIpv4MulAddr()
    {
        return ipv4MulAddr;
    }

    @JsonProperty(JSON_PROPERTY_IPV4_MUL_ADDR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIpv4MulAddr(String ipv4MulAddr)
    {
        this.ipv4MulAddr = ipv4MulAddr;
    }

    public IpMulticastAddressInfo srcIpv6Addr(String srcIpv6Addr)
    {

        this.srcIpv6Addr = srcIpv6Addr;
        return this;
    }

    /**
     * Get srcIpv6Addr
     * 
     * @return srcIpv6Addr
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_SRC_IPV6_ADDR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getSrcIpv6Addr()
    {
        return srcIpv6Addr;
    }

    @JsonProperty(JSON_PROPERTY_SRC_IPV6_ADDR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSrcIpv6Addr(String srcIpv6Addr)
    {
        this.srcIpv6Addr = srcIpv6Addr;
    }

    public IpMulticastAddressInfo ipv6MulAddr(String ipv6MulAddr)
    {

        this.ipv6MulAddr = ipv6MulAddr;
        return this;
    }

    /**
     * Get ipv6MulAddr
     * 
     * @return ipv6MulAddr
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_IPV6_MUL_ADDR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getIpv6MulAddr()
    {
        return ipv6MulAddr;
    }

    @JsonProperty(JSON_PROPERTY_IPV6_MUL_ADDR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIpv6MulAddr(String ipv6MulAddr)
    {
        this.ipv6MulAddr = ipv6MulAddr;
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
        IpMulticastAddressInfo ipMulticastAddressInfo = (IpMulticastAddressInfo) o;
        return Objects.equals(this.srcIpv4Addr, ipMulticastAddressInfo.srcIpv4Addr) && Objects.equals(this.ipv4MulAddr, ipMulticastAddressInfo.ipv4MulAddr)
               && Objects.equals(this.srcIpv6Addr, ipMulticastAddressInfo.srcIpv6Addr) && Objects.equals(this.ipv6MulAddr, ipMulticastAddressInfo.ipv6MulAddr);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(srcIpv4Addr, ipv4MulAddr, srcIpv6Addr, ipv6MulAddr);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class IpMulticastAddressInfo {\n");
        sb.append("    srcIpv4Addr: ").append(toIndentedString(srcIpv4Addr)).append("\n");
        sb.append("    ipv4MulAddr: ").append(toIndentedString(ipv4MulAddr)).append("\n");
        sb.append("    srcIpv6Addr: ").append(toIndentedString(srcIpv6Addr)).append("\n");
        sb.append("    ipv6MulAddr: ").append(toIndentedString(ipv6MulAddr)).append("\n");
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
