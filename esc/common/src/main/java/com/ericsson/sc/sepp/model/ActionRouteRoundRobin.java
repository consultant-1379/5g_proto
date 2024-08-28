
package com.ericsson.sc.sepp.model;

import com.ericsson.sc.glue.IfActionRouteBase;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Route to the any of the hosts in the specified nf-pool
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "failover-profile-ref", "last-resort-nf-pool-ref", "target-nf-pool", "target-roaming-partner" })
public class ActionRouteRoundRobin implements IfActionRouteBase
{

    /**
     * Name of the referenced failover-profile
     * 
     */
    @JsonProperty("failover-profile-ref")
    @JsonPropertyDescription("Name of the referenced failover-profile")
    private String failoverProfileRef;
    /**
     * Last nf-pool of hosts to try in case of multiple failures
     * 
     */
    @JsonProperty("last-resort-nf-pool-ref")
    @JsonPropertyDescription("Last nf-pool of hosts to try in case of multiple failures")
    private String lastResortNfPoolRef;
    /**
     * Route via the referenced configured nf-pool or variable that holds the
     * nf-pool name
     * 
     */
    @JsonProperty("target-nf-pool")
    @JsonPropertyDescription("Route via the referenced configured nf-pool or variable that holds the nf-pool name")
    private TargetNfPool targetNfPool;
    /**
     * Route via the referenced roaming-partner
     * 
     */
    @JsonProperty("target-roaming-partner")
    @JsonPropertyDescription("Route via the referenced roaming-partner")
    private TargetRoamingPartner targetRoamingPartner;

    /**
     * Name of the referenced failover-profile
     * 
     */
    @JsonProperty("failover-profile-ref")
    public String getFailoverProfileRef()
    {
        return failoverProfileRef;
    }

    /**
     * Name of the referenced failover-profile
     * 
     */
    @JsonProperty("failover-profile-ref")
    public void setFailoverProfileRef(String failoverProfileRef)
    {
        this.failoverProfileRef = failoverProfileRef;
    }

    public ActionRouteRoundRobin withFailoverProfileRef(String failoverProfileRef)
    {
        this.failoverProfileRef = failoverProfileRef;
        return this;
    }

    /**
     * Last nf-pool of hosts to try in case of multiple failures
     * 
     */
    @JsonProperty("last-resort-nf-pool-ref")
    public String getLastResortNfPoolRef()
    {
        return lastResortNfPoolRef;
    }

    /**
     * Last nf-pool of hosts to try in case of multiple failures
     * 
     */
    @JsonProperty("last-resort-nf-pool-ref")
    public void setLastResortNfPoolRef(String lastResortNfPoolRef)
    {
        this.lastResortNfPoolRef = lastResortNfPoolRef;
    }

    public ActionRouteRoundRobin withLastResortNfPoolRef(String lastResortNfPoolRef)
    {
        this.lastResortNfPoolRef = lastResortNfPoolRef;
        return this;
    }

    /**
     * Route via the referenced configured nf-pool or variable that holds the
     * nf-pool name
     * 
     */
    @JsonProperty("target-nf-pool")
    public TargetNfPool getTargetNfPool()
    {
        return targetNfPool;
    }

    /**
     * Route via the referenced configured nf-pool or variable that holds the
     * nf-pool name
     * 
     */
    @JsonProperty("target-nf-pool")
    public void setTargetNfPool(TargetNfPool targetNfPool)
    {
        this.targetNfPool = targetNfPool;
    }

    public ActionRouteRoundRobin withTargetNfPool(TargetNfPool targetNfPool)
    {
        this.targetNfPool = targetNfPool;
        return this;
    }

    /**
     * Route via the referenced roaming-partner
     * 
     */
    @JsonProperty("target-roaming-partner")
    public TargetRoamingPartner getTargetRoamingPartner()
    {
        return targetRoamingPartner;
    }

    /**
     * Route via the referenced roaming-partner
     * 
     */
    @JsonProperty("target-roaming-partner")
    public void setTargetRoamingPartner(TargetRoamingPartner targetRoamingPartner)
    {
        this.targetRoamingPartner = targetRoamingPartner;
    }

    public ActionRouteRoundRobin withTargetRoamingPartner(TargetRoamingPartner targetRoamingPartner)
    {
        this.targetRoamingPartner = targetRoamingPartner;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ActionRouteRoundRobin.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("failoverProfileRef");
        sb.append('=');
        sb.append(((this.failoverProfileRef == null) ? "<null>" : this.failoverProfileRef));
        sb.append(',');
        sb.append("lastResortNfPoolRef");
        sb.append('=');
        sb.append(((this.lastResortNfPoolRef == null) ? "<null>" : this.lastResortNfPoolRef));
        sb.append(',');
        sb.append("targetNfPool");
        sb.append('=');
        sb.append(((this.targetNfPool == null) ? "<null>" : this.targetNfPool));
        sb.append(',');
        sb.append("targetRoamingPartner");
        sb.append('=');
        sb.append(((this.targetRoamingPartner == null) ? "<null>" : this.targetRoamingPartner));
        sb.append(',');
        if (sb.charAt((sb.length() - 1)) == ',')
        {
            sb.setCharAt((sb.length() - 1), ']');
        }
        else
        {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode()
    {
        int result = 1;
        result = ((result * 31) + ((this.targetRoamingPartner == null) ? 0 : this.targetRoamingPartner.hashCode()));
        result = ((result * 31) + ((this.failoverProfileRef == null) ? 0 : this.failoverProfileRef.hashCode()));
        result = ((result * 31) + ((this.lastResortNfPoolRef == null) ? 0 : this.lastResortNfPoolRef.hashCode()));
        result = ((result * 31) + ((this.targetNfPool == null) ? 0 : this.targetNfPool.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof ActionRouteRoundRobin) == false)
        {
            return false;
        }
        ActionRouteRoundRobin rhs = ((ActionRouteRoundRobin) other);
        return (((((this.targetRoamingPartner == rhs.targetRoamingPartner)
                   || ((this.targetRoamingPartner != null) && this.targetRoamingPartner.equals(rhs.targetRoamingPartner)))
                  && ((this.failoverProfileRef == rhs.failoverProfileRef)
                      || ((this.failoverProfileRef != null) && this.failoverProfileRef.equals(rhs.failoverProfileRef))))
                 && ((this.lastResortNfPoolRef == rhs.lastResortNfPoolRef)
                     || ((this.lastResortNfPoolRef != null) && this.lastResortNfPoolRef.equals(rhs.lastResortNfPoolRef))))
                && ((this.targetNfPool == rhs.targetNfPool) || ((this.targetNfPool != null) && this.targetNfPool.equals(rhs.targetNfPool))));
    }

}
