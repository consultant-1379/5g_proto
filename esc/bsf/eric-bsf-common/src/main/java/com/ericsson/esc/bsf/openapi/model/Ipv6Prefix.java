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
 * Created on: Feb 4, 2019
 *     Author: xchrfar
 */

package com.ericsson.esc.bsf.openapi.model;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Objects;

import com.ericsson.esc.lib.ValidationException;
import com.google.common.net.InetAddresses;

public class Ipv6Prefix
{
    private final Inet6Address prefixAddress;
    private final short prefixLength;

    public Ipv6Prefix(Inet6Address prefixAddress,
                      short prefixLength)
    {
        if (prefixLength < 0 || prefixLength > 128)
        {
            throw new IllegalArgumentException("IPv6 prefix length out of range: " + prefixLength);
        }
        if (prefixAddress == null)
        {
            throw new NullPointerException("IPv6 address cannot be null");
        }
        this.prefixAddress = prefixAddress;
        this.prefixLength = prefixLength;
    }

    public Ipv6Prefix(String prefix)
    {

        String[] parts;
        Integer octets;
        Integer lsbits;
        Integer i;
        byte mask = (byte) 0xFFFE;

        try
        {
            // split the prefix into IP and Netmask
            parts = prefix.split("/");
            final var ipAddArr = (InetAddresses.forString(parts[0])).getAddress();

            // validate the Netmask
            prefixLength = Short.parseShort(parts[1]);
            if (prefixLength < 0 || prefixLength > 128)
            {
                throw new IllegalArgumentException("Invalid netmask: " + prefixLength);
            }
            // zero the IP bits according to Netmask and form the Network Address
            octets = (128 - prefixLength) / 8;
            lsbits = prefixLength % 8;

            for (i = 0; i < octets; i++)
            {
                ipAddArr[15 - i] = 0;
            }

            while (lsbits > 0 && lsbits < 8)
            {
                ipAddArr[15 - octets] = (byte) (ipAddArr[15 - octets] & mask);
                mask = (byte) (mask << 1);
                lsbits++;
            }

            // Forming the network address
            prefixAddress = (Inet6Address) InetAddress.getByAddress(ipAddArr);

        }
        catch (Exception e)
        {
            throw new ValidationException(ValidationException.ErrorType.SYNTAX_ERROR, "ipv6Prefix", "Invalid IPv6 Prefix", e);
        }

    }

    @Override
    public String toString()
    {
        return prefixAddress.getHostAddress() + "/" + prefixLength;
    }

    public Inet6Address getPrefixAddress()
    {
        return prefixAddress;
    }

    public short getPrefixLength()
    {
        return prefixLength;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(prefixAddress, prefixLength);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        Ipv6Prefix other = (Ipv6Prefix) obj;
        return Objects.equals(prefixAddress, other.prefixAddress) && prefixLength == other.prefixLength;
    }

}
