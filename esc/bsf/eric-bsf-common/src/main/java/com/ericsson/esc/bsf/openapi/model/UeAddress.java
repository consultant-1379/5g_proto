/**
 * COPYRIGHT ERICSSON GMBH 2018
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Feb 4, 2019
 *     Author: xchrfar
 */

package com.ericsson.esc.bsf.openapi.model;

import java.net.Inet4Address;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.ericsson.esc.lib.InvalidParam;
import com.ericsson.esc.lib.ValidationException;
import com.google.common.net.InetAddresses;

import io.vertx.ext.web.api.RequestParameter;

/**
 * Represents the UE address in a NbsfManagement discovery query
 */
public final class UeAddress extends BaseUeAddress
{
    private final UeAddressType type;

    /**
     * @param ipv4Addr
     * @param ipv6Prefix
     * @param ipDomain
     * @param macAddr48
     */
    private UeAddress(Optional<Inet4Address> ipv4Addr,
                      Optional<Ipv6Prefix> ipv6Prefix,
                      Optional<String> ipDomain,
                      Optional<MacAddr48> macAddr48,
                      UeAddressType type)
    {
        super(ipv4Addr, ipv6Prefix, ipDomain, macAddr48);
        this.type = type;
    }

    private enum QueryParameters
    {
        IPV4_ADDR("ipv4Addr"),
        IPV6_PREFIX("ipv6Prefix"),
        MAC_ADDR_48("macAddr48"),
        IP_DOMAIN("ipDomain");

        String name;

        QueryParameters(String name)
        {
            this.name = name;
        }
    }

    /**
     * Construct a UE address from the query parameters of an NBsfManagement
     * discovery HTTP query
     * 
     * @throws IllegalArgumentException
     * @param rp
     * @return The UE address or an empty Optional if query parameters are empty
     */
    public static Optional<UeAddress> fromQueryParameters(io.vertx.ext.web.api.RequestParameters rp)
    {
        EnumSet<QueryParameters> queryParams = EnumSet.noneOf(QueryParameters.class);

        final Optional<Inet4Address> ipv4Addr;

        try
        {
            ipv4Addr = Optional.ofNullable(rp.queryParameter(QueryParameters.IPV4_ADDR.name))
                               .map(RequestParameter::getString)
                               .map(x -> (Inet4Address) InetAddresses.forString(x));
            ipv4Addr.ifPresent(val -> queryParams.add(QueryParameters.IPV4_ADDR));
        }
        catch (Exception e)
        {
            throw new ValidationException(ValidationException.ErrorType.SYNTAX_ERROR, QueryParameters.IPV4_ADDR.name, "Invalid IPv4 Address", e);
        }
        final Optional<Ipv6Prefix> ipv6Prefix = Optional.ofNullable(rp.queryParameter(QueryParameters.IPV6_PREFIX.name))
                                                        .map(RequestParameter::getString)
                                                        .map(Ipv6Prefix::new)
                                                        .map(UeAddress::ipv6Prefix64tranformer);

        ipv6Prefix.ifPresent(val -> queryParams.add(QueryParameters.IPV6_PREFIX));

        // TODO ipDomain is Optional parameter, maybe should be ignored.
        final Optional<String> ipDomain;
        try
        {
            ipDomain = Optional.ofNullable(rp.queryParameter(QueryParameters.IP_DOMAIN.name)).map(RequestParameter::getString);
            ipDomain.ifPresent(val -> queryParams.add(QueryParameters.IP_DOMAIN));
        }
        catch (Exception e)
        {
            throw new ValidationException(ValidationException.ErrorType.SYNTAX_ERROR, QueryParameters.IP_DOMAIN.name, "Invalid IPv4 Domain", e);
        }

        final Optional<MacAddr48> macAddr48;
        try
        {
            macAddr48 = Optional.ofNullable(rp.queryParameter(QueryParameters.MAC_ADDR_48.name)).map(RequestParameter::getString).map(MacAddr48::new);
            macAddr48.ifPresent(val -> queryParams.add(QueryParameters.MAC_ADDR_48));
        }
        catch (Exception e)
        {
            throw new ValidationException(ValidationException.ErrorType.SYNTAX_ERROR, QueryParameters.MAC_ADDR_48.name, "Invalid MAC Address", e);
        }

        final UeAddressType type;
        if (queryParams.isEmpty())
        {
            return Optional.empty();
        }
        else if (queryParams.contains(QueryParameters.MAC_ADDR_48))
        {
            type = UeAddressType.MAC;

            if (queryParams.size() != 1)
            {
                queryParams.remove(QueryParameters.MAC_ADDR_48);
                throw new ValidationException(ValidationException.ErrorType.SEMANTIC_ERROR_WRONG_PARAM,
                                              queryParams.stream()
                                                         .map(ep -> new InvalidParam(ep.name, "Cannot be combined with " + QueryParameters.MAC_ADDR_48.name))
                                                         .collect(Collectors.toList()));
            }
        }
        else if (queryParams.contains(QueryParameters.IPV4_ADDR))
        {
            // No MAC address parameter
            if (queryParams.contains(QueryParameters.IPV6_PREFIX))
            {
                type = UeAddressType.INET4_6;
            }
            else
            {
                type = UeAddressType.INET4;
            }
        }
        else if (queryParams.contains(QueryParameters.IPV6_PREFIX))
        {
            type = UeAddressType.INET6;
            if (queryParams.contains(QueryParameters.IP_DOMAIN))
            {
                throw new ValidationException(ValidationException.ErrorType.SEMANTIC_ERROR_WRONG_PARAM,
                                              QueryParameters.IP_DOMAIN.name,
                                              "IPv4 Domain requires an IPv4 Address");
            }
        }
        // ipDomain is present but NOT any of ipv4Addr, ipv6Prefix, macAddr48
        else
        {
            final String errorMsg = "One of ipv4Addr, ipv6Prefix or macAddr48 shall be present as query parameter";
            throw new ValidationException(ValidationException.ErrorType.SEMANTIC_ERROR_MISSING_PARAM,
                                          List.of(new InvalidParam("ipv4Addr", errorMsg),
                                                  new InvalidParam("ipv6Prefix", errorMsg),
                                                  new InvalidParam("macAddr48", errorMsg)));

        }

        return Optional.of(new UeAddress(ipv4Addr, ipv6Prefix, ipDomain, macAddr48, type));
    }

    /**
     * Create an IPv4 UeAddress with optional IPv4 domain
     * 
     * @param ipv4Addr A non null IPv4 address
     * @param ipDomain An optional IPv4 domain
     */
    public UeAddress(Inet4Address ipv4Addr,
                     Optional<String> ipDomain)
    {
        super(Optional.of(ipv4Addr), Optional.empty(), ipDomain, Optional.empty());
        this.type = UeAddressType.INET4;
    }

    /**
     * Create an IPv4-IPv6 UE Address with optional IPv4 domain
     * 
     * @param Non null IPv4 Address
     * @param An  optional IPv4 domain
     * @param A   non null IPv6 prefix
     */
    public UeAddress(Inet4Address ipv4Addr,
                     Optional<String> ipDomain,
                     Ipv6Prefix ipv6Prefix)
    {
        super(Optional.of(ipv4Addr), Optional.of(ipv6Prefix), ipDomain, Optional.empty());
        this.type = UeAddressType.INET4_6;
    }

    /**
     * Create an IPv6 UE Address
     * 
     * @param A non null IPv6 prefix
     */
    public UeAddress(Ipv6Prefix ipv6Prefix)
    {
        super(Optional.empty(), Optional.of(ipv6Prefix), Optional.empty(), Optional.empty());
        this.type = UeAddressType.INET6;
    }

    /**
     * Create a MAC UE Address
     * 
     * @param A non null MAC address
     */
    public UeAddress(MacAddr48 macAddr48)
    {
        super(Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(macAddr48));
        this.type = UeAddressType.MAC;
    }

//FIXME rename this method and move it somewhere else
    public static Ipv6Prefix ipv6Prefix64tranformer(Ipv6Prefix ipv6)
    {

        if (ipv6.getPrefixLength() != 128)
        {
            throw new ValidationException(ValidationException.ErrorType.SYNTAX_ERROR, QueryParameters.IPV6_PREFIX.name, "Only /128 netmask is supported");
        }
        else
        {
            String ip = ipv6.getPrefixAddress().getHostAddress() + "/64";
            ipv6 = new Ipv6Prefix(ip);
        }
        return ipv6;

    }

    /**
     * 
     * @return The UE Address type
     */
    public UeAddressType getType()
    {
        return this.type;
    }

    @Override
    public int hashCode()
    {
        final var prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(type);
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        UeAddress other = (UeAddress) obj;
        return type == other.type;
    }

}
