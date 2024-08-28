/**
 * COPYRIGHT ERICSSON GMBH 2021
 * <p>
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 * <p>
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 * <p>
 * Created on: Nov 29, 2021
 * Author: eavapsr
 */

package com.ericsson.sc.proxyal.proxyconfig.proxyroutingconfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.PseudoProfilePerNfType;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.TopologyHiding;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.TopologyHiding.Builder;

/**
 *
 */
public class ProxyPseudoSearchResult implements IfProxyTopologyHiding
{
    private Map<String, String> nfTypePseudoSearchResultMap = new HashMap<>();
    protected List<String> fqdnList = new ArrayList<>();

    public ProxyPseudoSearchResult()
    {
    }

    /**
     * Copy constructor
     *
     * @param pth
     */
    public ProxyPseudoSearchResult(ProxyPseudoSearchResult pth)
    {
        this.nfTypePseudoSearchResultMap = new HashMap<>(pth.nfTypePseudoSearchResultMap);
        this.fqdnList = new ArrayList<>(pth.fqdnList);

    }

    public void putToMap(String nfType,
                         String searchResult)
    {
        nfTypePseudoSearchResultMap.put(nfType, searchResult);
    }

    public void addFqdn(String fqdn)
    {
        fqdnList.add(fqdn);
    }

    public Builder initBuilder()
    {
        var thBuilder = TopologyHiding.newBuilder();
        return initBuilder(thBuilder);
    }

    public Builder initBuilder(Builder thBuilder)
    {

        nfTypePseudoSearchResultMap.forEach((nfType,
                                             pseudoSearchResult) -> thBuilder.addPseudoProfiles(PseudoProfilePerNfType.newBuilder()
                                                                                                                      .setNfType(nfType.toUpperCase())
                                                                                                                      .setPseudoProfile(pseudoSearchResult)));
        fqdnList.forEach(thBuilder::addPseudoFqdn);

        return thBuilder;
    }

    public IfProxyTopologyHiding clone()
    {

        return new ProxyPseudoSearchResult(this);

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(fqdnList, nfTypePseudoSearchResultMap);
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
        ProxyPseudoSearchResult other = (ProxyPseudoSearchResult) obj;
        return Objects.equals(fqdnList, other.fqdnList) && Objects.equals(nfTypePseudoSearchResultMap, other.nfTypePseudoSearchResultMap);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ProxyTopologyHiding [nfTypePseudoProfileMap=" + nfTypePseudoSearchResultMap + ", fqdnList=" + fqdnList + "]";
    }
}
