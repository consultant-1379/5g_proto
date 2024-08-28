/**
 * COPYRIGHT ERICSSON GMBH 2022
 * <p>
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 * <p>
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 * <p>
 * Created on: Sep 26, 2022
 * Author: emavoni
 */
package com.ericsson.sc.proxy.endpoints;

import com.ericsson.sc.glue.IfNfInstance;
import com.ericsson.sc.glue.IfNfPool;
import com.ericsson.sc.proxy.ConfigHelper;

public class N32EndpointCollector extends EndpointCollector
{

    private boolean keepOriginalAuthorityHeader;

    public N32EndpointCollector(IfNfInstance configInst,
                                IfNfPool pool)
    {
        super(configInst, pool);
        this.keepOriginalAuthorityHeader = true;
    }

    /**
     * Return a list of remote sepp endpoints belonging to N32-C enabled RPs. One
     * nf-pool-ref with sepp endpoints is referenced inside each roaming partner
     * under N32-c container. The list is created from static-nf-instances.
     * 
     * Each endpoint has the following metadata attached:
     * <ul>
     * <li>envoy.lb
     * <ul>
     * <li>pool -> name of the pool this endpoint is in
     * <li>host -> fqdn of the endpoint
     * </ul>
     * </ul>
     */
    @Override
    public void createEndpoints()
    {

        var servicesPerSeppPool = (pool.getStaticSeppInstanceDataRef() != null
                                   && !pool.getStaticSeppInstanceDataRef().isEmpty()) ? ConfigHelper.getStaticProxyServices(pool, this.configInst).stream()
                                                                                      : ConfigHelper.getAllNfServices(pool, this.configInst).stream();

        // For each SEPP, create the host endpoint. Set their host metadata to
        // the list of the fqdns of their nfServices.
        this.endpoints.addAll(createEndpointsFromServiceStream(servicesPerSeppPool,
                                                               0,
                                                               "n32c_pool#!_#subset_sr:#!_#keep_authority:",
                                                               keepOriginalAuthorityHeader));

        setEndpointVTapFlag(this.endpoints, pool.getName());
    }

}
