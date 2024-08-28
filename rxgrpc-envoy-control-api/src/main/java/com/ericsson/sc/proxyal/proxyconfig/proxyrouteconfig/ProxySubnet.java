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
 * Created on: Apr 18, 2019
 *     Author: eedrak
 */
package com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig;

/**
 * Simple container class. Holds all the data needed for the ipv6 subnet defined
 * optionally in some consumers for RCC (CT). Some values are hard-coded here,
 * some come from the configuration uploaded to the CM Mediator.
 */
public class ProxySubnet
{
    private final String ipv6Prefix;

    public ProxySubnet(String address)
    {
        this.ipv6Prefix = address;
    }

    public ProxySubnet(ProxySubnet anotherSubnet)
    {
        this.ipv6Prefix = anotherSubnet.ipv6Prefix;

    }

    public String getAddress()
    {
        return ipv6Prefix;
    }

    /**
     * Converts the ipv6 address prefix we get from CM to a form suitable for
     * matching in envoy's virtual hosts domain list. for example
     * "abcd:abcd:abcd:abcd::/64" -> "[abcd:abcd:abcd:abcd:*"
     * 
     * @return String suitable for virtuahostb.
     */
    public String addressForDomainMatching()
    {
        String addrForDomain = ipv6Prefix.split("/")[0];
        if (!addrForDomain.startsWith("["))
            addrForDomain = "[" + addrForDomain;
        if (addrForDomain.endsWith("::"))
            addrForDomain = addrForDomain.substring(0, addrForDomain.length() - 1);
        return addrForDomain + "*";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ipv6Prefix == null) ? 0 : ipv6Prefix.hashCode());
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
        ProxySubnet other = (ProxySubnet) obj;
        if (ipv6Prefix == null)
        {
            if (other.ipv6Prefix != null)
                return false;
        }
        else if (!ipv6Prefix.equals(other.ipv6Prefix))
            return false;
        return true;
    }
}
