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
 * Created on: Jul 14, 2020
 *     Author: eedala
 */

package com.ericsson.utilities.common;

/**
 * Utility functions to fetch information about the runtime environment
 */
public class RuntimeEnvironment
{
    private RuntimeEnvironment()
    {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Return the IP version (IP_VERSION.IPV4 or IP_VERSION.IPV6 or
     * IP_VERSION.IPV4_IPV6)
     * 
     * @return IP_VERSION.IPV4 or IP_VERSION.IPV6 or IP_VERSION.IPV4_IPV6 depending
     *         on the value of the environment variable "IP_VERSION". Default is
     *         IP_VERSION.IPV4_IPV6.
     */
    public static IP_VERSION getDeployedIpVersion()
    {
        return "ipv4".equalsIgnoreCase(EnvVars.get("IP_FAMILY")) ? IP_VERSION.IPV4
                                                                 : "ipv6".equalsIgnoreCase(EnvVars.get("IP_FAMILY")) ? IP_VERSION.IPV6 : IP_VERSION.IPV4_IPV6;
    }
}
