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
 * Created on: Dec 16, 2020
 *     Author: eaoknkr
 */

package com.ericsson.sc.proxyal.proxyconfig.proxyroutingconfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.FilterCase;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.FilterCase.Builder;

/**
 * 
 */
public class ProxyFilterCase
{
    private final String name;
    private List<ProxyFilterData> filterData = new ArrayList<>();
    private List<ProxyFilterRule> filterRules = new ArrayList<>();

    public ProxyFilterCase(String name)
    {
        this.name = name;
    }

    public ProxyFilterCase(ProxyFilterCase anotherProxyFilterCase)
    {
        this.name = anotherProxyFilterCase.name;
        this.filterData = new ArrayList<>(anotherProxyFilterCase.filterData);
        this.filterRules = new ArrayList<>(anotherProxyFilterCase.filterRules);
    }

    public void addFilterData(ProxyFilterData rd)
    {
        this.filterData.add(rd);
    }

    public void addFilterRule(ProxyFilterRule rr)
    {
        this.filterRules.add(rr);
    }

    /**
     * @return the filterData
     */
    public List<ProxyFilterData> getFilterData()
    {
        return filterData;
    }

    /**
     * @param filterData the filterData to set
     */
    public void setFilterData(List<ProxyFilterData> filterData)
    {
        this.filterData = filterData;
    }

    /**
     * @return the filterRules
     */
    public List<ProxyFilterRule> getFilterRules()
    {
        return filterRules;
    }

    /**
     * @param filterRules the filterRules to set
     */
    public void setFilterRules(List<ProxyFilterRule> filterRules)
    {
        this.filterRules = filterRules;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ProxyFilterCase [name=" + name + ", filterData=" + filterData + ", filterRules=" + filterRules + "]";
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(filterData, filterRules, name);
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
        ProxyFilterCase other = (ProxyFilterCase) obj;
        return Objects.equals(filterData, other.filterData) && Objects.equals(filterRules, other.filterRules) && Objects.equals(name, other.name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ericsson.sc.proxyal.proxyconfig.ProxyBuilder#build(java.lang.Object[])
     */

    public Builder initBuilder()
    {
        var fcBuilder = FilterCase.newBuilder().setName(this.name);

        for (var fd : this.filterData)
        {
            var fdBuilder = fd.initBuilder();
            fcBuilder.addFilterData(fdBuilder.build());
        }

        for (var fr : this.filterRules)
        {
            var frBuilder = fr.initBuilder();
            fcBuilder.addFilterRules(frBuilder.build());
        }

        return fcBuilder;
    }

}
