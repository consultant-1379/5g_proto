/**
 * COPYRIGHT ERICSSON GMBH 2019
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jan 25, 2019
 *     Author: zmelpan
 */

package com.ericsson.esc.bsf.openapi.model;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.util.List;
import java.util.Objects;

import com.ericsson.esc.lib.InvalidParam;
import com.ericsson.esc.lib.ValidationException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.net.InetAddresses;

@JsonInclude(Include.NON_NULL)
public class IpEndPoint
{

    private final Inet4Address ipv4Address;
    private final Inet6Address ipv6Address;
    private final TransportProtocol transport;
    private final Integer port;

    public IpEndPoint(Inet4Address ipv4Address,
                      Inet6Address ipv6Address,
                      TransportProtocol transport,
                      Integer port)
    {
        this.ipv4Address = ipv4Address;
        this.ipv6Address = ipv6Address;
        this.transport = transport;
        this.port = port;
    }

    public static IpEndPoint create(Inet4Address ipv4Address,
                                    Inet6Address ipv6Address,
                                    TransportProtocol transport,
                                    Integer port)
    {
        if (ipv4Address == null && ipv6Address == null)
        {

            final String errorMsg = "Either ipv4Address or ipv6Address parameter must be defined";
            throw new ValidationException(ValidationException.ErrorType.SEMANTIC_ERROR_MISSING_PARAM,
                                          List.of(new InvalidParam("ipv4Address", errorMsg), new InvalidParam("ipv6Address", errorMsg)));
        }
        if (ipv4Address != null && ipv6Address != null)
        {

            final String errorMsg = "Either ipv4Address or ipv6Address parameter must be defined";
            throw new ValidationException(ValidationException.ErrorType.SEMANTIC_ERROR_WRONG_PARAM,
                                          List.of(new InvalidParam("ipv4Address", errorMsg), new InvalidParam("ipv6Address", errorMsg)));
        }

        return new IpEndPoint(ipv4Address, ipv6Address, transport, port);
    }

    @JsonCreator
    public static IpEndPoint createJson(@JsonProperty("ipv4Address") String ipv4Address,
                                        @JsonProperty("ipv6Address") String ipv6Address,
                                        @JsonProperty("transport") String transport,
                                        @JsonProperty("port") Integer port)
    {
        // Parse IP addresses.
        Inet4Address ipv4 = null;
        Inet6Address ipv6 = null;

        if (ipv4Address != null)
        {
            try
            {
                ipv4 = (Inet4Address) InetAddresses.forString(ipv4Address);
            }
            catch (Exception e)
            {
                throw new ValidationException(ValidationException.ErrorType.SYNTAX_ERROR, "ipv4Address", "Invalid ipv4Address in IpEndPoint", e);
            }
        }

        if (ipv6Address != null)
        {
            try
            {
                ipv6 = (Inet6Address) InetAddresses.forString(ipv6Address);
            }
            catch (Exception e)
            {
                throw new ValidationException(ValidationException.ErrorType.SYNTAX_ERROR, "ipv6Address", "Invalid ipv6Address in IpEndPoint", e);
            }
        }

        TransportProtocol transportFromString = null;
        if (transport != null)
        {
            try
            {
                transportFromString = TransportProtocol.fromValue(transport);
            }
            catch (Exception e)
            {
                throw new ValidationException(ValidationException.ErrorType.SYNTAX_ERROR_OPTIONAL, "transport", "Invalid transport in IpEndPoint", e);
            }
        }

        if (port != null && (port < 0 || port > 65535))
        {
            throw new ValidationException(ValidationException.ErrorType.SYNTAX_ERROR_OPTIONAL, "port", "Invalid port in IpEndPoint");
        }

        return IpEndPoint.create(ipv4, ipv6, transportFromString, port);
    }

    public Inet4Address getIpv4Address()
    {
        return ipv4Address;
    }

    public Inet6Address getIpv6Address()
    {
        return ipv6Address;
    }

    public TransportProtocol getTransport()
    {
        return transport;
    }

    public Integer getPort()
    {
        return port;
    }

    @Override
    public boolean equals(java.lang.Object o)
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
        return Objects.equals(ipv4Address, ipEndPoint.ipv4Address) && Objects.equals(ipv6Address, ipEndPoint.ipv6Address)
               && Objects.equals(transport, ipEndPoint.transport) && Objects.equals(port, ipEndPoint.port);
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
        sb.append("{\n");

        sb.append("    ipv4Address: ").append(String.valueOf(ipv4Address)).append("\n");
        sb.append("    ipv6Address: ").append(String.valueOf(ipv6Address)).append("\n");
        sb.append("    transport: ").append(String.valueOf(transport)).append("\n");
        sb.append("    port: ").append(String.valueOf(port)).append("\n");
        sb.append("}");

        return sb.toString();
    }
}
