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

import java.util.ArrayList;
import java.util.List;

import com.ericsson.sc.proxyal.proxyconfig.proxyendpointconfig.ProxyEndpoint;

/**
 * 
 */
public class EndpointCollector
{
    private List<ProxyEndpoint> allEndpoints = new ArrayList<>(); // endpoints + last resort endpoints
    private List<ProxyEndpoint> lastResortEndpoints = new ArrayList<>();
    private List<ProxyEndpoint> endpoints = new ArrayList<>();

    /**
     * @param allEndpoints
     * @param lastResortEndpoints
     * @param endpoints
     */
    public EndpointCollector(List<ProxyEndpoint> endpoints,
                             List<ProxyEndpoint> lastResortEndpoints)
    {
        this.lastResortEndpoints = lastResortEndpoints;
        this.endpoints = endpoints;
        this.allEndpoints.addAll(this.lastResortEndpoints);
        this.allEndpoints.addAll(this.endpoints);

    }

    public EndpointCollector(List<ProxyEndpoint> endpoints)
    {
        this.endpoints = endpoints;
        this.allEndpoints.addAll(this.endpoints);
    }

    /**
     * @return the lastResortEndpoints
     */
    public List<ProxyEndpoint> getLastResortEndpoints()
    {
        return lastResortEndpoints;
    }

    /**
     * @param lastResortEndpoints the lastResortEndpoints to set
     */
    public void setLastResortEndpoints(List<ProxyEndpoint> lastResortEndpoints)
    {
        this.lastResortEndpoints = lastResortEndpoints;
        this.allEndpoints.addAll(this.lastResortEndpoints);
    }

    /**
     * @return the allEndpoints
     */
    public List<ProxyEndpoint> getAllEndpoints()
    {
        return allEndpoints;
    }

    /**
     * @return the endpoints
     */
    public List<ProxyEndpoint> getEndpoints()
    {
        return endpoints;
    }

}
