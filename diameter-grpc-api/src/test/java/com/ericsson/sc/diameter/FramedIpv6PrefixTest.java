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

import static org.testng.Assert.assertEquals;

import java.net.Inet6Address;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.google.common.net.InetAddresses;

public class FramedIpv6PrefixTest
{

    private static final Logger log = LoggerFactory.getLogger(FramedIpv6PrefixTest.class);

    @Test(enabled = true)
    public void FramedIpv6PrefixInetTest()
    {
        final var addr = (Inet6Address) InetAddresses.forString("2001:0db8:85a3:0000:0000:8a2e:0370:73ff");
        FramedIpv6Prefix framed = FramedIpv6Prefix.fromInet6Address(128, addr);
        log.info("Address: {}", addr.getCanonicalHostName());
        log.info("Prefix: {}", framed);
        log.info("Prefix length: {}", framed.getPrefixLength());

        assertEquals(addr.getCanonicalHostName().toString(), "2001:db8:85a3:0:0:8a2e:370:73ff", "Invalid canonical Hostname");
        assertEquals(framed.toString(), "2001:db8:85a3::8a2e:370:73ff/128", "Invalid prefix");
        assertEquals(framed.getPrefix().toString(), "/2001:db8:85a3:0:0:8a2e:370:73ff", "Invalid prefix");
        assertEquals(framed.getPrefixLength(), 128, "Invalid prefix length");
    }

    @Test(enabled = true)
    public void FramedIpv6PrefixByteTest()
    {
        final var addr = (Inet6Address) InetAddresses.forString("2001:0db8:85a3:0000:0000:8a2e:0370:73ff");
        final var addr2 = (Inet6Address) InetAddresses.forString("2001:0db8:85a3:0000:0000:8a2e:0300:7000");

        byte[] addres = addr.getAddress();
        byte prefiLength = (byte) 0x80;
        byte reserved = (byte) 0x00;
        byte[] ipv6ReservedAndPrefix = new byte[] { reserved, prefiLength };
        byte[] fullIpv6AddrArray = new byte[ipv6ReservedAndPrefix.length + addres.length];

        System.arraycopy(ipv6ReservedAndPrefix, 0, fullIpv6AddrArray, 0, ipv6ReservedAndPrefix.length);
        System.arraycopy(addres, 0, fullIpv6AddrArray, ipv6ReservedAndPrefix.length, addres.length);

        FramedIpv6Prefix framed = FramedIpv6Prefix.fromAvpOctets(fullIpv6AddrArray);
        log.info("Prefix: {}", framed);
        assertEquals(framed.toString(), "2001:db8:85a3::8a2e:370:73ff/128", "Invalid prefix");

    }

    @Test(enabled = true)
    public void FramedIpv6PrefixByteInvalidLengthTest()
    {
        final var addr = (Inet6Address) InetAddresses.forString("2001:0db8:85a3:0000:0000:8a2e:0370:73ff");

        byte[] addres = addr.getAddress();
        byte prefixLength = (byte) 0x81;
        byte reserved = (byte) 0x00;
        byte[] ipv6ReservedAndPrefix = new byte[] { reserved, prefixLength };
        byte[] fullIpv6AddrArray = new byte[ipv6ReservedAndPrefix.length + addres.length];

        System.arraycopy(ipv6ReservedAndPrefix, 0, fullIpv6AddrArray, 0, ipv6ReservedAndPrefix.length);
        System.arraycopy(addres, 0, fullIpv6AddrArray, ipv6ReservedAndPrefix.length, addres.length);

        try
        {
            FramedIpv6Prefix.fromAvpOctets(fullIpv6AddrArray);
        }
        catch (Exception e)
        {
            log.info(e.getMessage());
            assertEquals(e.getMessage(), "Invalid IPv6 prefix prefixLength: 129");
        }

    }

}
