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
 * Created on: Sep 14, 2020
 *     Author: eedrak
 */

package com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A helper-class to be attached to a ProxyCluster to sub-divide it into
 * subsets.
 */
public class ProxyLbSubset
{

    public enum FallbackPolicy
    {
        ANY_ENDPOINT("ANY_ENDPOINT"),
        NO_FALLBACK("NO_FALLBACK"),
        DEFAULT_SUBSET("DEFAULT_SUBSET"),
        KEYS_SUBSET("KEYS_SUBSET"),
        NOT_DEFINED("NOT_DEFINED");

        String name;

        private FallbackPolicy(String name)
        {
            this.name = name;
        }

        @Override
        public String toString()
        {
            return this.name;
        }
    }

    private FallbackPolicy fallBackPolicy = FallbackPolicy.ANY_ENDPOINT;
    private Map<String, String> defaultSubset = new HashMap<>();
    private List<ProxyLbSubsetSelector> subsets = new ArrayList<>();

    /**
     * Create all load-balancing subsets from the lists of keys given.
     * 
     * @param keys
     */
    public ProxyLbSubset(List<ProxyLbSubsetSelector> subsets)
    {
        super();
        this.subsets = subsets;
        this.fallBackPolicy = FallbackPolicy.ANY_ENDPOINT;
    }

    /**
     * Create all load-balancing subsets from the lists of keys given, with DEFAULT
     * SUBSET set as fallback policy.
     * 
     * @param keys
     */
    public ProxyLbSubset(List<ProxyLbSubsetSelector> subsets,
                         Map<String, String> defaultSubset)
    {
        super();
        this.subsets = subsets;
        this.defaultSubset = defaultSubset;
        this.fallBackPolicy = FallbackPolicy.DEFAULT_SUBSET;
    }

    /**
     * Copy constructor
     * 
     * @param anotherLbSubset
     */
    public ProxyLbSubset(ProxyLbSubset anotherLbSubset)
    {
        this.fallBackPolicy = anotherLbSubset.fallBackPolicy;
        anotherLbSubset.subsets.forEach(key -> this.subsets.add(new ProxyLbSubsetSelector(key)));
        anotherLbSubset.defaultSubset.forEach((k,
                                               v) -> this.defaultSubset.put(k, v));
    }

    /**
     * Return all subsets and their keys.
     * 
     * @return subset-selector keys
     */
    public List<ProxyLbSubsetSelector> getSubsetSelectorKeys()
    {
        return this.subsets;
    }

    public Map<String, String> getDefaultSubset()
    {
        return defaultSubset;
    }

    public void setDefaultSubset(Map<String, String> defaultSubset)
    {
        this.defaultSubset = defaultSubset;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ProxyLbSubset [fallBackPolicy=" + fallBackPolicy + ", defaultSubset=" + defaultSubset + ", subsets=" + subsets + "]";
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(defaultSubset, fallBackPolicy, subsets);
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
        ProxyLbSubset other = (ProxyLbSubset) obj;
        return Objects.equals(defaultSubset, other.defaultSubset) && fallBackPolicy == other.fallBackPolicy && Objects.equals(subsets, other.subsets);
    }

    public FallbackPolicy getFbPolicy()
    {
        return this.fallBackPolicy;
    }

    public void setFbPolicy(FallbackPolicy fallbackPolicy)
    {
        this.fallBackPolicy = fallbackPolicy;
    }

}
