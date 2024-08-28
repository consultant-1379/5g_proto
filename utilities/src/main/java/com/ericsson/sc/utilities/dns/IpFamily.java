/**
 * COPYRIGHT ERICSSON GMBH 2023
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Sep 26, 2023
 *     Author: eedstl
 */

package com.ericsson.sc.utilities.dns;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum IpFamily
{
    IPV4("ipv4"),
    IPV6("ipv6");

    private final static Map<String, IpFamily> ipFamilies = new HashMap<>();
    private final static Map<IpFamily, Integer> ipVersions = new HashMap<>();

    static
    {
        for (IpFamily c : values())
        {
            ipFamilies.put(c.value, c);
            ipVersions.put(c, Integer.parseInt(c.value.substring(c.value.lastIndexOf("v") + 1)));
        }
    }

    @JsonCreator
    public static IpFamily fromValue(final String value)
    {
        final IpFamily ipFamily = ipFamilies.get(value);

        if (ipFamily != null)
            return ipFamily;

        throw new IllegalArgumentException(value);
    }

    public static int toIpVersion(final IpFamily ipFamily)
    {
        return ipVersions.get(ipFamily);
    }

    private final String value;

    private IpFamily(final String value)
    {
        this.value = value;
    }

    @Override
    public String toString()
    {
        return this.value;
    }

    @JsonValue
    public String value()
    {
        return this.value;
    }
}
