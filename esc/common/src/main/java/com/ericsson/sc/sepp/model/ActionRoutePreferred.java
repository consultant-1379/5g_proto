
package com.ericsson.sc.sepp.model;

import com.ericsson.sc.glue.IfActionRouteBase;
import com.ericsson.sc.glue.IfActionRouteTarget;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Route to the targeted host which belongs to the specified pool. In case of
 * failure, reselect among the rest of hosts in this pool
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "failover-profile-ref",
                     "target-nf-pool",
                     "last-resort-nf-pool-ref",
                     "from-var-name",
                     "from-target-api-root-header",
                     "from-authority-header" })
public class ActionRoutePreferred implements IfActionRouteBase, IfActionRouteTarget
{

    /**
     * Name of the referenced failover-profile
     * 
     */
    @JsonProperty("failover-profile-ref")
    @JsonPropertyDescription("Name of the referenced failover-profile")
    private String failoverProfileRef;
    /**
     * Route via the referenced configured nf-pool or variable that holds the
     * nf-pool name
     * 
     */
    @JsonProperty("target-nf-pool")
    @JsonPropertyDescription("Route via the referenced configured nf-pool or variable that holds the nf-pool name")
    private TargetNfPool targetNfPool;
    /**
     * Last nf-pool of hosts to try in case of multiple failures
     * 
     */
    @JsonProperty("last-resort-nf-pool-ref")
    @JsonPropertyDescription("Last nf-pool of hosts to try in case of multiple failures")
    private String lastResortNfPoolRef;
    /**
     * Route the request based on the defined variable
     * 
     */
    @JsonProperty("from-var-name")
    @JsonPropertyDescription("Route the request based on the defined variable")
    private String fromVarName;
    /**
     * Route the request based on the target-api-root-headers value
     * 
     */
    @JsonProperty("from-target-api-root-header")
    @JsonPropertyDescription("Route the request based on the target-api-root-headers value")
    private FromTargetApiRootHeader fromTargetApiRootHeader;
    /**
     * Route the request based on the authority-headers value
     * 
     */
    @JsonProperty("from-authority-header")
    @JsonPropertyDescription("Route the request based on the authority-headers value")
    private FromAuthorityHeader fromAuthorityHeader;

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

    public ActionRoutePreferred withFailoverProfileRef(String failoverProfileRef)
    {
        this.failoverProfileRef = failoverProfileRef;
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

    public ActionRoutePreferred withTargetNfPool(TargetNfPool targetNfPool)
    {
        this.targetNfPool = targetNfPool;
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

    public ActionRoutePreferred withLastResortNfPoolRef(String lastResortNfPoolRef)
    {
        this.lastResortNfPoolRef = lastResortNfPoolRef;
        return this;
    }

    /**
     * Route the request based on the defined variable
     * 
     */
    @JsonProperty("from-var-name")
    public String getFromVarName()
    {
        return fromVarName;
    }

    /**
     * Route the request based on the defined variable
     * 
     */
    @JsonProperty("from-var-name")
    public void setFromVarName(String fromVarName)
    {
        this.fromVarName = fromVarName;
    }

    public ActionRoutePreferred withFromVarName(String fromVarName)
    {
        this.fromVarName = fromVarName;
        return this;
    }

    /**
     * Route the request based on the target-api-root-headers value
     * 
     */
    @JsonProperty("from-target-api-root-header")
    public FromTargetApiRootHeader getFromTargetApiRootHeader()
    {
        return fromTargetApiRootHeader;
    }

    /**
     * Route the request based on the target-api-root-headers value
     * 
     */
    @JsonProperty("from-target-api-root-header")
    public void setFromTargetApiRootHeader(FromTargetApiRootHeader fromTargetApiRootHeader)
    {
        this.fromTargetApiRootHeader = fromTargetApiRootHeader;
    }

    public ActionRoutePreferred withFromTargetApiRootHeader(FromTargetApiRootHeader fromTargetApiRootHeader)
    {
        this.fromTargetApiRootHeader = fromTargetApiRootHeader;
        return this;
    }

    /**
     * Route the request based on the authority-headers value
     * 
     */
    @JsonProperty("from-authority-header")
    public FromAuthorityHeader getFromAuthorityHeader()
    {
        return fromAuthorityHeader;
    }

    /**
     * Route the request based on the authority-headers value
     * 
     */
    @JsonProperty("from-authority-header")
    public void setFromAuthorityHeader(FromAuthorityHeader fromAuthorityHeader)
    {
        this.fromAuthorityHeader = fromAuthorityHeader;
    }

    public ActionRoutePreferred withFromAuthorityHeader(FromAuthorityHeader fromAuthorityHeader)
    {
        this.fromAuthorityHeader = fromAuthorityHeader;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ActionRoutePreferred.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("failoverProfileRef");
        sb.append('=');
        sb.append(((this.failoverProfileRef == null) ? "<null>" : this.failoverProfileRef));
        sb.append(',');
        sb.append("targetNfPool");
        sb.append('=');
        sb.append(((this.targetNfPool == null) ? "<null>" : this.targetNfPool));
        sb.append(',');
        sb.append("lastResortNfPoolRef");
        sb.append('=');
        sb.append(((this.lastResortNfPoolRef == null) ? "<null>" : this.lastResortNfPoolRef));
        sb.append(',');
        sb.append("fromVarName");
        sb.append('=');
        sb.append(((this.fromVarName == null) ? "<null>" : this.fromVarName));
        sb.append(',');
        sb.append("fromTargetApiRootHeader");
        sb.append('=');
        sb.append(((this.fromTargetApiRootHeader == null) ? "<null>" : this.fromTargetApiRootHeader));
        sb.append(',');
        sb.append("fromAuthorityHeader");
        sb.append('=');
        sb.append(((this.fromAuthorityHeader == null) ? "<null>" : this.fromAuthorityHeader));
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
        result = ((result * 31) + ((this.fromVarName == null) ? 0 : this.fromVarName.hashCode()));
        result = ((result * 31) + ((this.failoverProfileRef == null) ? 0 : this.failoverProfileRef.hashCode()));
        result = ((result * 31) + ((this.fromTargetApiRootHeader == null) ? 0 : this.fromTargetApiRootHeader.hashCode()));
        result = ((result * 31) + ((this.fromAuthorityHeader == null) ? 0 : this.fromAuthorityHeader.hashCode()));
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
        if ((other instanceof ActionRoutePreferred) == false)
        {
            return false;
        }
        ActionRoutePreferred rhs = ((ActionRoutePreferred) other);
        return (((((((this.fromVarName == rhs.fromVarName) || ((this.fromVarName != null) && this.fromVarName.equals(rhs.fromVarName)))
                    && ((this.failoverProfileRef == rhs.failoverProfileRef)
                        || ((this.failoverProfileRef != null) && this.failoverProfileRef.equals(rhs.failoverProfileRef))))
                   && ((this.fromTargetApiRootHeader == rhs.fromTargetApiRootHeader)
                       || ((this.fromTargetApiRootHeader != null) && this.fromTargetApiRootHeader.equals(rhs.fromTargetApiRootHeader))))
                  && ((this.fromAuthorityHeader == rhs.fromAuthorityHeader)
                      || ((this.fromAuthorityHeader != null) && this.fromAuthorityHeader.equals(rhs.fromAuthorityHeader))))
                 && ((this.lastResortNfPoolRef == rhs.lastResortNfPoolRef)
                     || ((this.lastResortNfPoolRef != null) && this.lastResortNfPoolRef.equals(rhs.lastResortNfPoolRef))))
                && ((this.targetNfPool == rhs.targetNfPool) || ((this.targetNfPool != null) && this.targetNfPool.equals(rhs.targetNfPool))));
    }

}
