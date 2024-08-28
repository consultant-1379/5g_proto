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
 * Created on: Jun 18, 2020
 *     Author: eedrak
 */

package com.ericsson.sc.proxyal.service;

import java.util.List;

import com.ericsson.sc.proxyal.proxyconfig.ProxyCfg;
import com.ericsson.utilities.common.Rdn;
import com.google.protobuf.Any;

/**
 * 
 */
public class EnvoyCfg
{

    /*
     * Hold Envoy related configuration data, ready to be placed in
     * DiscoveryResponses
     */
    private final Rdn rdnOfNfInstance;

    private final List<Any> cdsResources;
    private final List<Any> edsResources;
    private final List<Any> ldsResources;
    private final List<Any> rdsResources;

    public EnvoyCfg(ProxyCfg pxCfg)
    {

        cdsResources = CdsHelper.buildResources(pxCfg);
        edsResources = EdsHelper.buildEndpoints(pxCfg);
        ldsResources = LdsHelper.buildResources(pxCfg);
        rdsResources = RdsHelper.buildRoutes(pxCfg);

        rdnOfNfInstance = pxCfg.getRdnOfNfInstance();

    }

    public List<Any> getCdsResources()
    {
        return cdsResources;
    }

    public List<Any> getEdsResources()
    {
        return edsResources;
    }

    public List<Any> getLdsResources()
    {
        return ldsResources;
    }

    public List<Any> getRdsResources()
    {
        return rdsResources;
    }

    public Rdn getRdnOfNfInstance()
    {
        return rdnOfNfInstance;
    }

}
