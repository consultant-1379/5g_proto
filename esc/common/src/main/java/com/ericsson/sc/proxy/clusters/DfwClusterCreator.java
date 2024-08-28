/**
 * COPYRIGHT ERICSSON GMBH 2020
 * <p>
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 * <p>
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 * <p>
 * Created on: Aug 8, 2022
 * Author: enocakh
 */
package com.ericsson.sc.proxy.clusters;

import java.util.Optional;
import java.util.Set;

import com.ericsson.sc.configutil.CommonConfigUtils;
import com.ericsson.sc.glue.IfNfInstance;
import com.ericsson.sc.glue.IfNfPool;
import com.ericsson.sc.proxy.ConfigHelper;
import com.ericsson.sc.proxyal.proxyconfig.ProxyTls;
import com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig.ProxyCluster;
import com.ericsson.utilities.common.RuntimeEnvironment;
import com.ericsson.utilities.common.IP_VERSION;

import io.envoyproxy.envoy.config.cluster.v3.Cluster.DnsLookupFamily;

public class DfwClusterCreator extends ClusterCreator
{

    private final boolean tlsOn;

    public DfwClusterCreator(IfNfPool pool,
                             boolean tlsOn,
                             IfNfInstance configInst)
    {
        super(pool, configInst);
        this.tlsOn = tlsOn;
        this.suffix = generateSuffix();
        this.altStatPoolName = generateAltStatName();
    }

    @Override
    public void createCluster()
    {
        ProxyCluster cluster = new ProxyCluster(pool.getName() + suffix, super.getClusterIpFamilies());
        cluster.setLbPolicy("CLUSTER_PROVIDED");
        cluster.makeDynamicCluster();
        cluster.setStatName(generateAltStatName());
        IP_VERSION internalIpVersion = RuntimeEnvironment.getDeployedIpVersion();
        cluster.setDnsLookupFamily(internalIpVersion.equals(IP_VERSION.IPV4) ? DnsLookupFamily.V4_ONLY
                                                                             : internalIpVersion.equals(IP_VERSION.IPV6) ? DnsLookupFamily.V6_ONLY
                                                                                                                         : DnsLookupFamily.ALL);
        addEgressConnectionProfile(cluster);
        addTlsConfiguration(cluster);
        setClusterVtapSettings(cluster);
        this.cluster = cluster;
    }

    @Override
    protected void addEgressConnectionProfile(ProxyCluster cluster)
    {
        var epc = CommonConfigUtils.getReferencedEgressConnectionProfile(pool, this.configInst);
        CommonConfigUtils.setHpackTableSize(cluster, epc);
        CommonConfigUtils.setMaxConcurrentStreams(cluster, epc);
        CommonConfigUtils.setMaxConnectionDuration(cluster, epc);
        CommonConfigUtils.setTcpConnectTimeout(cluster, epc);
        CommonConfigUtils.setTcpKeepalive(cluster, epc);
        CommonConfigUtils.setIdleTimeout(cluster, epc);
        CommonConfigUtils.setTrackClusterStats(cluster, epc);
        CommonConfigUtils.setDscpMarking(cluster, epc);
    }

    @Override
    public String generateAltStatName()
    {

        var altStatPoolName = "SR_" + pool.getName() + (tlsOn ? "_tls" : "_no_tls");
        return ConfigHelper.getClusterAltStatName(configInst.getName(), altStatPoolName);
    }

    @Override
    public String generateSuffix()
    {
        return CommonConfigUtils.buildClusterNameSuffix(Optional.empty(), tlsOn);
    }

    @Override
    protected void addTlsConfiguration(ProxyCluster cluster)
    {
        if (tlsOn)
        {
            cluster.setTls(new ProxyTls());
        }
    }

    @Override
    public void appendClusters(Set<ProxyCluster> clusters)
    {
        clusters.add(cluster);
    }
}
