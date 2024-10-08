/*
 * NRF NFManagement Service
 * NRF NFManagement Service.   © 2022, 3GPP Organizational Partners (ARIB, ATIS, CCSA, ETSI, TSDSI, TTA, TTC).   All rights reserved. 
 *
 * The version of the OpenAPI document: 1.2.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement;

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
 * IP addressing information of a given NFService; it consists on, e.g. IP
 * address, TCP port, transport protocol...
 */
@ApiModel(description = "IP addressing information of a given NFService; it consists on, e.g. IP address, TCP port, transport protocol... ")
@JsonPropertyOrder({ IpEndPoint.JSON_PROPERTY_IPV4_ADDRESS,
                     IpEndPoint.JSON_PROPERTY_IPV6_ADDRESS,
                     IpEndPoint.JSON_PROPERTY_TRANSPORT,
                     IpEndPoint.JSON_PROPERTY_PORT })
public class IpEndPoint
{
    public static final String JSON_PROPERTY_IPV4_ADDRESS = "ipv4Address";
    private String ipv4Address;

    public static final String JSON_PROPERTY_IPV6_ADDRESS = "ipv6Address";
    private String ipv6Address;

    public static final String JSON_PROPERTY_TRANSPORT = "transport";
    private String transport;

    public static final String JSON_PROPERTY_PORT = "port";
    private Integer port;

    public IpEndPoint()
    {
    }

    public IpEndPoint ipv4Address(String ipv4Address)
    {

        this.ipv4Address = ipv4Address;
        return this;
    }

    /**
     * String identifying a IPv4 address formatted in the &#39;dotted decimal&#39;
     * notation as defined in RFC 1166.
     * 
     * @return ipv4Address
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(example = "198.51.100.1", value = "String identifying a IPv4 address formatted in the 'dotted decimal' notation as defined in RFC 1166. ")
    @JsonProperty(JSON_PROPERTY_IPV4_ADDRESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getIpv4Address()
    {
        return ipv4Address;
    }

    @JsonProperty(JSON_PROPERTY_IPV4_ADDRESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIpv4Address(String ipv4Address)
    {
        this.ipv4Address = ipv4Address;
    }

    public IpEndPoint ipv6Address(String ipv6Address)
    {

        this.ipv6Address = ipv6Address;
        return this;
    }

    /**
     * Get ipv6Address
     * 
     * @return ipv6Address
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_IPV6_ADDRESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getIpv6Address()
    {
        return ipv6Address;
    }

    @JsonProperty(JSON_PROPERTY_IPV6_ADDRESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIpv6Address(String ipv6Address)
    {
        this.ipv6Address = ipv6Address;
    }

    public IpEndPoint transport(String transport)
    {

        this.transport = transport;
        return this;
    }

    /**
     * Types of transport protocol used in a given IP endpoint of an NF Service
     * Instance
     * 
     * @return transport
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Types of transport protocol used in a given IP endpoint of an NF Service Instance")
    @JsonProperty(JSON_PROPERTY_TRANSPORT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getTransport()
    {
        return transport;
    }

    @JsonProperty(JSON_PROPERTY_TRANSPORT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTransport(String transport)
    {
        this.transport = transport;
    }

    public IpEndPoint port(Integer port)
    {

        this.port = port;
        return this;
    }

    /**
     * Get port minimum: 0 maximum: 65535
     * 
     * @return port
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "")
    @JsonProperty(JSON_PROPERTY_PORT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getPort()
    {
        return port;
    }

    @JsonProperty(JSON_PROPERTY_PORT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPort(Integer port)
    {
        this.port = port;
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
        IpEndPoint ipEndPoint = (IpEndPoint) o;
        return Objects.equals(this.ipv4Address, ipEndPoint.ipv4Address) && Objects.equals(this.ipv6Address, ipEndPoint.ipv6Address)
               && Objects.equals(this.transport, ipEndPoint.transport) && Objects.equals(this.port, ipEndPoint.port);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(ipv4Address, ipv6Address, transport, port);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("class IpEndPoint {\n");
        sb.append("    ipv4Address: ").append(toIndentedString(ipv4Address)).append("\n");
        sb.append("    ipv6Address: ").append(toIndentedString(ipv6Address)).append("\n");
        sb.append("    transport: ").append(toIndentedString(transport)).append("\n");
        sb.append("    port: ").append(toIndentedString(port)).append("\n");
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
