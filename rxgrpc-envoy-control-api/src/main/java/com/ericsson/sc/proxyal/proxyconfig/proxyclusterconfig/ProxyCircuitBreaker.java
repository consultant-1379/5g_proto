/**
 * COPYRIGHT ERICSSON GMBH 2021
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Apr 16, 2021
 *     Author: esolfot
 */

package com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig;

import java.util.Objects;

/**
 * Simple container class. Holds all the Circuit Breakers data. It is attached
 * to the ProxyCluster object.
 */
public class ProxyCircuitBreaker
{

    private static final int MAX_CONNECTIONS = 16384;
    private int budgetPercent;
    private int minRetriesConcurrency;
    private int maxRequests;
    private int maxPendingRequests;
    private int maxConnections;

    public ProxyCircuitBreaker()
    {
        this.budgetPercent = 20;
        this.minRetriesConcurrency = 10;
        this.maxRequests = 16384;
        this.maxPendingRequests = 16384;
        this.maxConnections = 16384;

    }

    public ProxyCircuitBreaker(int budgetPercent,
                               int minRetriesConcurrency)
    {
        this.budgetPercent = budgetPercent;
        this.minRetriesConcurrency = minRetriesConcurrency;

    }

    public ProxyCircuitBreaker(ProxyCircuitBreaker that)
    {
        this.budgetPercent = that.budgetPercent;
        this.maxConnections = that.maxConnections;
        this.maxPendingRequests = that.maxPendingRequests;
        this.maxRequests = that.maxRequests;
        this.minRetriesConcurrency = that.minRetriesConcurrency;
    }

    /**
     * @return the budgetPercent
     */
    public int getBudgetPercent()
    {
        return budgetPercent;
    }

    /**
     * @param budgetPercent the budgetPercent to set
     */
    public void setBudgetPercent(int budgetPercent)
    {
        this.budgetPercent = budgetPercent;
    }

    /**
     * @return the minRetriesConcurrency
     */
    public int getMinRetriesConcurrency()
    {
        return minRetriesConcurrency;
    }

    /**
     * @param minRetriesConcurrency the minRetriesConcurrency to set
     */
    public void setMinRetriesConcurrency(int minRetriesConcurrency)
    {
        this.minRetriesConcurrency = minRetriesConcurrency;
    }

    public int getMaxRequests()
    {
        return maxRequests;
    }

    public void setMaxRequests(int maxRequests)
    {
        this.maxRequests = maxRequests;
    }

    public int getMaxPendingRequests()
    {
        return maxPendingRequests;
    }

    public void setMaxPendingRequests(int maxPendingRequests)
    {
        this.maxPendingRequests = maxPendingRequests;
    }

    public int getMaxConnections()
    {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections)
    {
        this.maxConnections = maxConnections;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ProxyCircuitBreaker [budgetPercent=" + budgetPercent + ", minRetriesConcurrency=" + minRetriesConcurrency + ", maxRequests=" + maxRequests
               + ", maxPendingRequests=" + maxPendingRequests + ", maxConnections=" + maxConnections + "]";
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(budgetPercent, maxConnections, maxPendingRequests, maxRequests, minRetriesConcurrency);
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
        ProxyCircuitBreaker other = (ProxyCircuitBreaker) obj;
        return budgetPercent == other.budgetPercent && maxConnections == other.maxConnections && maxPendingRequests == other.maxPendingRequests
               && maxRequests == other.maxRequests && minRetriesConcurrency == other.minRetriesConcurrency;
    }

}
