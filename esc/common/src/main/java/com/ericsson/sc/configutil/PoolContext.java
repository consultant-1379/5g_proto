/**
 * COPYRIGHT ERICSSON GMBH 2022
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Feb 16, 2022
 *     Author: eaoknkr
 */

package com.ericsson.sc.configutil;

import com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig.ProxyCluster;

/**
 * 
 */
public class PoolContext
{
    private final ProxyCluster proxyCluster;
    private final EndpointCollector endpointCollector;

    public PoolContext(ProxyCluster proxyCluster,
                       EndpointCollector endpointCollector)
    {
        this.proxyCluster = proxyCluster;
        this.endpointCollector = endpointCollector;
    }

    public ProxyCluster getProxyCluster()
    {
        return this.proxyCluster;
    }

    public EndpointCollector getEndpointCollector()
    {
        return this.endpointCollector;
    }
}
