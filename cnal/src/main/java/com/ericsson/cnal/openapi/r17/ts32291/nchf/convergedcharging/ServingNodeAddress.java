/*
 * Nchf_ConvergedCharging
 * ConvergedCharging Service    © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC). All rights reserved. 
 *
 * The version of the OpenAPI document: 3.1.12
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts32291.nchf.convergedcharging;

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
 * ServingNodeAddress
 */
@JsonPropertyOrder({ ServingNodeAddress.JSON_PROPERTY_IPV4_ADDR, ServingNodeAddress.JSON_PROPERTY_IPV6_ADDR })
public class ServingNodeAddress
{
    public static final String JSON_PROPERTY_IPV4_ADDR = "ipv4Addr";
    private String ipv4Addr;

    public static final String JSON_PROPERTY_IPV6_ADDR = "ipv6Addr";
    private String ipv6Addr;

    public ServingNodeAddress()
    {
    }

    public ServingNodeAddress ipv4Addr(String ipv4Addr)
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

    public ServingNodeAddress ipv6Addr(String ipv6Addr)
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
        ServingNodeAddress servingNodeAddress = (ServingNodeAddress) o;
        return Objects.equals(this.ipv4Addr, servingNodeAddress.ipv4Addr) && Objects.equals(this.ipv6Addr, servingNodeAddress.ipv6Addr);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(ipv4Addr, ipv6Addr);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class ServingNodeAddress {\n");
        sb.append("    ipv4Addr: ").append(toIndentedString(ipv4Addr)).append("\n");
        sb.append("    ipv6Addr: ").append(toIndentedString(ipv6Addr)).append("\n");
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
