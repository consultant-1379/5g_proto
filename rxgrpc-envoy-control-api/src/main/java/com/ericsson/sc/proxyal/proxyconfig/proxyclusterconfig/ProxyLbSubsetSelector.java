package com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig.ProxyLbSubset.FallbackPolicy;

public class ProxyLbSubsetSelector
{

    private FallbackPolicy fallbackPolicy = FallbackPolicy.NOT_DEFINED; // Global fallback policy applied.
    private List<String> keys = new ArrayList<>();
    private List<String> fallbackKeys = new ArrayList<String>();
    private boolean singleHostPerSubset = false;

    /**
     * Create a load balancer subset selector from the lists of keys given.
     * 
     * @param keys
     */
    public ProxyLbSubsetSelector(List<String> keys)
    {
        super();
        this.keys = keys;
    }

    /**
     * Create a load balancer subset selector, given the keys and the fallback
     * subset keys.
     * 
     * @param keys
     */
    public ProxyLbSubsetSelector(List<String> keys,
                                 List<String> fallbackKeys)
    {
        super();
        this.keys = keys;
        this.fallbackKeys = fallbackKeys;
        this.fallbackPolicy = FallbackPolicy.KEYS_SUBSET;

    }

    /**
     * Copy constructor
     * 
     * @param anotherLbSubset
     */
    public ProxyLbSubsetSelector(ProxyLbSubsetSelector anotherLbSubset)
    {
        this.fallbackPolicy = anotherLbSubset.fallbackPolicy;
        anotherLbSubset.keys.forEach(key -> this.keys.add(key));
        this.fallbackKeys = new ArrayList<>(anotherLbSubset.getFallbackKeys());
        this.singleHostPerSubset = anotherLbSubset.getSingleHostPerSubset();
    }

    public FallbackPolicy getFallbackPolicy()
    {
        return fallbackPolicy;
    }

    public List<String> getKeys()
    {
        return keys;
    }

    public List<String> getFallbackKeys()
    {
        return fallbackKeys;
    }

    public void setFallbackPolicy(FallbackPolicy fallbackPolicy)
    {
        this.fallbackPolicy = fallbackPolicy;
    }

    public void setKeys(List<String> keys)
    {
        this.keys = keys;
    }

    public void setFallbackKeys(List<String> fallbackKeys)
    {
        this.fallbackKeys = fallbackKeys;
    }

    /**
     * @return the singleHostPerSubset
     */
    public Boolean getSingleHostPerSubset()
    {
        return singleHostPerSubset;
    }

    /**
     * @param singleHostPerSubset the singleHostPerSubset to set
     */
    public void setSingleHostPerSubset(Boolean singleHostPerSubset)
    {
        this.singleHostPerSubset = singleHostPerSubset;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ProxyLbSubsetSelector [fallbackPolicy=" + fallbackPolicy + ", keys=" + keys + ", fallbackKeys=" + fallbackKeys + ", singleHostPerSubset="
               + singleHostPerSubset + "]";
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(fallbackKeys, fallbackPolicy, keys, singleHostPerSubset);
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
        ProxyLbSubsetSelector other = (ProxyLbSubsetSelector) obj;
        return Objects.equals(fallbackKeys, other.fallbackKeys) && fallbackPolicy == other.fallbackPolicy && Objects.equals(keys, other.keys)
               && singleHostPerSubset == other.singleHostPerSubset;
    }

}