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
package com.ericsson.sc.proxy.clusters;

import java.util.Arrays;
import java.util.Optional;

import com.ericsson.sc.glue.IfNfInstance;
import com.ericsson.sc.glue.IfNfPool;
import com.ericsson.sc.proxy.ProxyConstants.METADATA;
import com.ericsson.sc.proxy.endpoints.EndpointCollector;
import com.ericsson.sc.proxyal.proxyconfig.ProxyTls;
import com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig.ProxyCluster;
import com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig.ProxyLbSubset;
import com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig.ProxyLbSubsetSelector;
import com.ericsson.sc.sepp.model.NfInstance;
import com.ericsson.sc.sepp.model.NfPool;
import com.ericsson.utilities.common.Utils;

public final class N32ClusterCreator extends SubsetClusterCreator
{
    private static final String SUFFIX_N32C = "#!_#N32C_#!_#";
    private final String rpName;

    public N32ClusterCreator(IfNfPool pool,
                             IfNfInstance configInst,
                             EndpointCollector endpointCollector,
                             String rpName)
    {
        super(pool, configInst, endpointCollector);
        this.rpName = rpName;
        this.suffix = generateSuffix();
        this.altStatPoolName = ""; // keep the cluster name for emitting statistics as these are internal clusters
                                   // and should not be confused
                                   // with their egress counterparts
    }

    @Override
    public void createCluster()
    {
        var n32cCluster = new ProxyCluster(pool.getName() + suffix);
        n32cCluster.makeInternalCluster();
        n32cCluster.setLbSubset(createLbSubsetConfig());
        n32cCluster.setStatName(altStatPoolName);

        for (var ep : endpointCollector.getEndpoints())
        {
            n32cCluster.addEndpoint(ep);
        }
        addTlsConfiguration(n32cCluster);
        setClusterVtapSettings(n32cCluster);
        this.cluster = n32cCluster;
    }

    @Override
    protected void createTlsForSeppCluster(ProxyCluster cluster,
                                           final NfPool pool,
                                           final NfInstance seppInst)
    {

        fetchNetworkForRp(seppInst.getExternalNetwork(),
                          rpName).ifPresent(nw -> Optional.ofNullable(Utils.getByName(nw.getRoamingPartner(), this.rpName)).ifPresent(rp -> getTrustedCaList(seppInst, rp).ifPresent(tcaListRp -> Optional.ofNullable(Utils.getByName(seppInst.getServiceAddress(), nw.getServiceAddressRef())).map(svcAddress -> getAsymetricKeyForSvcAddress(seppInst, svcAddress)).filter(Optional::isPresent).map(asymKey -> new ProxyTls(tcaListRp, asymKey.get())).ifPresent(cluster::setTls))));
    }

    @Override
    public String generateSuffix()
    {
        return new StringBuilder(SUFFIX_N32C).append(rpName).toString();
    }

    @Override
    protected ProxyLbSubset createLbSubsetConfig()
    {
        var lbSubset = new ProxyLbSubset(Arrays.asList(new ProxyLbSubsetSelector(Arrays.asList(METADATA.HOST))));
        lbSubset.setFbPolicy(getDefaultFallbackPolicy());
        return lbSubset;
    }

    @Override
    protected ProxyLbSubset.FallbackPolicy getDefaultFallbackPolicy()
    {
        return ProxyLbSubset.FallbackPolicy.NO_FALLBACK; // NOT DEFINED Leads to exception
    }

}
