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
 * Created on: Feb 17, 2021
 *     Author: echfari
 */
package com.ericsson.esc.bsf.openapi.model;

import java.net.Inet4Address;
import java.util.Objects;
import java.util.Optional;

/**
 * Holds UE address elements. Objects of this class are not necessarily valid UE
 * addresses.
 */
public class BaseUeAddress
{
    private final Optional<Inet4Address> ipv4Addr;
    private final Optional<Ipv6Prefix> ipv6Prefix;
    private final Optional<String> ipDomain;
    private final Optional<MacAddr48> macAddr48;

    protected BaseUeAddress(Optional<Inet4Address> ipv4Addr,
                            Optional<Ipv6Prefix> ipv6Prefix,
                            Optional<String> ipDomain,
                            Optional<MacAddr48> macAddr48)
    {
        Objects.requireNonNull(ipv4Addr);
        Objects.requireNonNull(ipv6Prefix);
        Objects.requireNonNull(ipDomain);
        Objects.requireNonNull(macAddr48);

        this.ipv4Addr = ipv4Addr;
        this.ipv6Prefix = ipv6Prefix;
        this.ipDomain = ipDomain;
        this.macAddr48 = macAddr48;
    }

    /**
     * Create a BaseUeAddress object
     * 
     * @param ipv4Addr   An IPv4 address or null
     * @param ipv6Prefix An IPv6 prefix or null
     * @param ipDomain   An ipDomain or null
     * @param macAddr48  A MAC address, or null
     * @return A non null, BaseUeAddress
     */
    public static BaseUeAddress create(Inet4Address ipv4Addr,
                                       Ipv6Prefix ipv6Prefix,
                                       String ipDomain,
                                       MacAddr48 macAddr48)
    {
        return new BaseUeAddress(Optional.ofNullable(ipv4Addr), Optional.ofNullable(ipv6Prefix), Optional.ofNullable(ipDomain), Optional.ofNullable(macAddr48));
    }

    /**
     * Copy constructor
     * 
     * @param other The BaseUeAddress to copy
     * @return A copy of the given address
     */
    public static BaseUeAddress create(BaseUeAddress other)
    {
        return new BaseUeAddress(other.ipv4Addr, other.ipv6Prefix, other.ipDomain, other.macAddr48);
    }

    public Optional<Inet4Address> getIpv4Addr()
    {
        return ipv4Addr;
    }

    /**
     * 
     * @return The IP domain
     */
    public Optional<String> getIpDomain()
    {
        return this.ipDomain;
    }

    /**
     * 
     * @return The IPv6 prefix
     */
    public Optional<Ipv6Prefix> getIpv6Prefix()
    {
        return this.ipv6Prefix;
    }

    /**
     * 
     * @return The MAC address
     */
    public Optional<MacAddr48> getMacAddr48()
    {
        return this.macAddr48;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(ipDomain, ipv4Addr, ipv6Prefix, macAddr48);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BaseUeAddress other = (BaseUeAddress) obj;
        return Objects.equals(ipDomain, other.ipDomain) && Objects.equals(ipv4Addr, other.ipv4Addr) && Objects.equals(ipv6Prefix, other.ipv6Prefix)
               && Objects.equals(macAddr48, other.macAddr48);
    }

    @Override
    public String toString()
    {
        final var builder = new StringBuilder();
        builder.append("BaseUeAddress [ipv4Addr=");
        builder.append(ipv4Addr);
        builder.append(", ipv6Prefix=");
        builder.append(ipv6Prefix);
        builder.append(", ipDomain=");
        builder.append(ipDomain);
        builder.append(", macAddr48=");
        builder.append(macAddr48);
        builder.append("]");
        return builder.toString();
    }

}
