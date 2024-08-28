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
package com.ericsson.sc.proxyal.proxyconfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig.ProxyCluster;
import com.ericsson.sc.proxyal.proxyconfig.proxyendpointconfig.ProxyEndpoint;
import com.ericsson.sc.proxyal.proxyconfig.proxylistenerconfig.ProxyListener;
import com.ericsson.sc.utilities.dns.IpFamily;
import com.ericsson.utilities.common.Rdn;

/**
 * Simple container class. Holds the top-level config data for a proxy
 * configuration in Envoy. Some values are hard-coded here, some come from the
 * configuration uploaded to the CM Mediator.
 */
public class ProxyCfg
{
    private static final Logger log = LoggerFactory.getLogger(ProxyCfg.class);

    private final List<ProxyCluster> clusters = new ArrayList<>();
    private final Set<ProxyListener> listeners = new HashSet<>();
    private final Set<IpFamily> defaultIpFamilies = new HashSet<>();
    public static final int DYN_CLUSTER_TTL_SECONDS = 60 * 60 * 24 * 365;
    private String nfType;

    private Rdn rdnOfNfInstance;

    public ProxyCfg(String nfType)
    {
        this.nfType = nfType;
        this.rdnOfNfInstance = new Rdn("nf", this.nfType.toLowerCase() + "-function");
    }

    public ProxyCfg(final ProxyCfg anotherPxCfg)
    {
        this.defaultIpFamilies.addAll(anotherPxCfg.defaultIpFamilies);
        anotherPxCfg.getClusters().forEach(cluster -> this.addCluster(new ProxyCluster(cluster)));
        anotherPxCfg.getListeners().forEach(listener -> this.addListener(new ProxyListener(listener)));
        this.rdnOfNfInstance = anotherPxCfg.getRdnOfNfInstance();
        this.nfType = anotherPxCfg.nfType;
    }

    public void addDefaultIpFamilies(final Set<IpFamily> defaultIpFamilies)
    {
        this.defaultIpFamilies.addAll(defaultIpFamilies);
    }

    public Set<IpFamily> getDefaultIpFamilies()
    {
        return this.defaultIpFamilies;
    }

    public void addCluster(ProxyCluster cluster)
    {
        this.clusters.add(cluster);
    }

    public List<ProxyCluster> getClusters()
    {
        return this.clusters;
    }

    public List<String> getClusterNames()
    {
        return this.clusters.stream().map(ProxyCluster::getName).collect(Collectors.toList());
    }

    public int getNumEndpointsInCluster(String name)
    {
        Optional<ProxyCluster> elems = this.clusters.stream().filter(c -> c.getName().equals(name)).findFirst();
        return (elems.isPresent() ? elems.get().getEndpoints().size() : 0);
    }

    public boolean hasCluster(String name)
    {
        Optional<ProxyCluster> elems = this.clusters.stream().filter(c -> c.getName().equals(name)).findFirst();
        return elems.isPresent();
    }

    public Optional<ProxyCluster> getClusterWithName(String name)
    {
        return this.clusters.stream().filter(c -> c.getName().equals(name)).findFirst();
    }

    public void addListener(ProxyListener listener)
    {
        this.listeners.add(listener);
    }

    public Set<ProxyListener> getListeners()
    {
        return this.listeners;
    }

    public Optional<ProxyListener> getListenerWithName(String name)
    {
        return this.listeners.stream().filter(l -> l.getName().equals(name)).findFirst();
    }

    public Rdn getRdnOfNfInstance()
    {
        return this.rdnOfNfInstance;
    }

    public ProxyCfg setRdnOfNfInstance(String name)
    {
        this.rdnOfNfInstance = new Rdn("nf", this.nfType.toLowerCase() + "-function").add("nf-instance", name);
        return this;
    }

    public List<ProxyEndpoint> getProxyEndpoints()
    {
        return this.clusters.stream()
                            .filter(cluster -> !cluster.getEndpoints().isEmpty())
                            .flatMap(cluster -> cluster.getEndpoints().stream())
                            .collect(Collectors.toList());
    }

    /**
     * @return the nfType
     */
    public String getNfType()
    {
        return nfType;
    }

    /**
     * @param nfType the nfType to set
     */
    public void setNfType(String nfType)
    {
        this.nfType = nfType;
    }

    public ProxyCfg withNfType(String nfType)
    {
        this.nfType = nfType;
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ProxyCfg [defaultIpFamilies=" + defaultIpFamilies + ", clusters=" + clusters + ", listeners=" + listeners + ", nfType=" + nfType
               + ", rdnOfNfInstance=" + rdnOfNfInstance + "]";
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(clusters, listeners, nfType, rdnOfNfInstance);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProxyCfg other = (ProxyCfg) obj;
        return Objects.equals(clusters, other.clusters) && Objects.equals(listeners, other.listeners) && Objects.equals(nfType, other.nfType)
               && Objects.equals(rdnOfNfInstance, other.rdnOfNfInstance);
    }

}
