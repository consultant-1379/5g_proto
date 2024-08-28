/**
 * COPYRIGHT ERICSSON GMBH 2020
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: May 1, 2020
 *     Author: echfari
 */
package com.ericsson.sc.diameter;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

import com.google.common.net.InetAddresses;

public class FramedIpv6Prefix
{
    // INDEX_RESERVED = 0
    private static final int INDEX_PREFIX_LENGTH = 1;
    private static final int INDEX_PREFIX = 2;
    public static final int MAX_PREFIX_LENGTH = 128;

    private final int prefixLength;
    private final byte[] normalizedPrefix;
    private final Inet6Address prefix;

    /**
     * @throws IllegalArgumentException
     * @param radiusPrefix
     */
    public static FramedIpv6Prefix fromAvpOctets(byte[] radiusPrefix)
    {
        Objects.requireNonNull(radiusPrefix);
        if (radiusPrefix.length > 18 || radiusPrefix.length < 2)
            throw new IllegalArgumentException("Invalid ipv6prefix size: " + radiusPrefix.length);
        // Parse Prefix-Length
        final var prefixLength = Byte.toUnsignedInt(radiusPrefix[INDEX_PREFIX_LENGTH]);
        validatePrefixLength(prefixLength);
        // Parse binary encoded prefix
        final var prefixOctets = Arrays.copyOfRange(radiusPrefix, INDEX_PREFIX, radiusPrefix.length);

        return new FramedIpv6Prefix(prefixLength, prefixOctets);
    }

    public static FramedIpv6Prefix fromInet6Address(int prefixLength,
                                                    Inet6Address inet6Address)
    {
        Objects.requireNonNull(inet6Address);
        validatePrefixLength(prefixLength);
        return new FramedIpv6Prefix(prefixLength, inet6Address.getAddress());
    }

    public int getPrefixLength()
    {
        return prefixLength;
    }

    public Inet6Address getPrefix()
    {
        return prefix;
    }

    public static byte[] normalizePrefix(byte[] addrBytes,
                                         int prefixLength)
    {

        final int octets = prefixLength / 8;
        final int suboctet = (prefixLength % 8);
        final int requiredSize = (octets + (suboctet == 0 ? 0 : 1));
        if (addrBytes.length < requiredSize)
            throw new IllegalArgumentException("Number of bits(" + addrBytes.length * 8 + ") less than Prefix-Length(" + prefixLength + ")");
        var result = ByteBuffer.allocate(requiredSize);
        for (var i = 0; i < octets; i++)
        {
            result.put(i, addrBytes[i]);
        }
        if (suboctet > 0)
        {
            final int mask = ~(0xff >> suboctet);
            final int maskedByte = Byte.toUnsignedInt(addrBytes[octets]) & mask;

            result.put(octets, (byte) maskedByte);
        }

        return result.array();
    }

    public byte[] encode()
    {
        var bb = ByteBuffer.allocate(normalizedPrefix.length + 2);
        bb.put((byte) 0);
        bb.put((byte) this.prefixLength);
        bb.put(normalizedPrefix);
        return bb.array();
    }

    private FramedIpv6Prefix(int prefixLength,
                             byte[] prefixOctets)
    {
        this.normalizedPrefix = normalizePrefix(prefixOctets, prefixLength);
        this.prefixLength = prefixLength;

        var bb = ByteBuffer.allocate(16);
        bb.put(this.normalizedPrefix);
        try
        {
            this.prefix = (Inet6Address) InetAddress.getByAddress(bb.array());
        }
        catch (UnknownHostException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    private static void validatePrefixLength(int prefixLength)
    {
        if (prefixLength > MAX_PREFIX_LENGTH || prefixLength < 0)
        {
            throw new IllegalArgumentException("Invalid IPv6 prefix prefixLength: " + prefixLength);
        }
    }

    @Override
    public int hashCode()
    {
        final var prime = 31;
        var result = 1;
        result = prime * result + Arrays.hashCode(normalizedPrefix);
        result = prime * result + Objects.hash(prefixLength);
        return result;
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
        FramedIpv6Prefix other = (FramedIpv6Prefix) obj;
        return Arrays.equals(normalizedPrefix, other.normalizedPrefix) && prefixLength == other.prefixLength;
    }

    @Override
    public String toString()
    {
        return InetAddresses.toAddrString(this.prefix) + "/" + this.prefixLength;
    }

}
