package com.ericsson.utilities.common;

import java.util.HashSet;
import java.util.Set;

import com.ericsson.sc.utilities.dns.IpFamily;

public enum IP_VERSION
{
    IPV4,
    IPV6,
    IPV4_IPV6;

    public static Set<IpFamily> toIpFamilies(final IP_VERSION ipVersion)
    {
        final Set<IpFamily> ipFamilies = new HashSet<>();

        switch (ipVersion)
        {
            case IPV4:
                ipFamilies.add(IpFamily.IPV4);
                break;

            case IPV6:
                ipFamilies.add(IpFamily.IPV6);
                break;

            case IPV4_IPV6:
            default:
                ipFamilies.add(IpFamily.IPV4);
                ipFamilies.add(IpFamily.IPV6);
                break;
        }

        return ipFamilies;
    }
}
