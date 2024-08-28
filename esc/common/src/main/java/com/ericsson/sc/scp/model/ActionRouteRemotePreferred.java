
package com.ericsson.sc.scp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ericsson.sc.glue.IfActionRouteBase;

/**
 * Route indirectly to the targeted host as specified in the
 * target-api-root-header. In cases where re-selection is needed, select the
 * host name to include in the target-api-root header based on the result of the
 * preceded nf-discovery action. The target-api-root header is replaced in a
 * round-robin fashion for every subsequent re-selection
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "preserve-if-indirect-routing",
                     "preserve-disc-param-if-indirect",
                     "failover-profile-ref",
                     "target-nf-pool",
                     "last-resort-nf-pool-ref",
                     "from-target-api-root-header" })
public class ActionRouteRemotePreferred implements IfActionRouteBase
{

    /**
     * Selected attribute's value to preserve in case of indirect routing
     * 
     */
    @JsonProperty("preserve-if-indirect-routing")
    @JsonPropertyDescription("Selected attribute's value to preserve in case of indirect routing")
    private PreserveIfIndirectRouting preserveIfIndirectRouting;
    /**
     * The discovery parameters of the received request to preserve in case of
     * indirect routing. By default no parameters are preserved
     * 
     */
    @JsonProperty("preserve-disc-param-if-indirect")
    @JsonPropertyDescription("The discovery parameters of the received request to preserve in case of indirect routing. By default no parameters are preserved")
    private PreserveDiscParamIfIndirect preserveDiscParamIfIndirect;
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
     * If present, route the request based on the target-api-root-header's value
     * 
     */
    @JsonProperty("from-target-api-root-header")
    @JsonPropertyDescription("If present, route the request based on the target-api-root-header's value")
    private FromTargetApiRootHeader fromTargetApiRootHeader;

    /**
     * Selected attribute's value to preserve in case of indirect routing
     * 
     */
    @JsonProperty("preserve-if-indirect-routing")
    public PreserveIfIndirectRouting getPreserveIfIndirectRouting()
    {
        return preserveIfIndirectRouting;
    }

    /**
     * Selected attribute's value to preserve in case of indirect routing
     * 
     */
    @JsonProperty("preserve-if-indirect-routing")
    public void setPreserveIfIndirectRouting(PreserveIfIndirectRouting preserveIfIndirectRouting)
    {
        this.preserveIfIndirectRouting = preserveIfIndirectRouting;
    }

    public ActionRouteRemotePreferred withPreserveIfIndirectRouting(PreserveIfIndirectRouting preserveIfIndirectRouting)
    {
        this.preserveIfIndirectRouting = preserveIfIndirectRouting;
        return this;
    }

    /**
     * The discovery parameters of the received request to preserve in case of
     * indirect routing. By default no parameters are preserved
     * 
     */
    @JsonProperty("preserve-disc-param-if-indirect")
    public PreserveDiscParamIfIndirect getPreserveDiscParamIfIndirect()
    {
        return preserveDiscParamIfIndirect;
    }

    /**
     * The discovery parameters of the received request to preserve in case of
     * indirect routing. By default no parameters are preserved
     * 
     */
    @JsonProperty("preserve-disc-param-if-indirect")
    public void setPreserveDiscParamIfIndirect(PreserveDiscParamIfIndirect preserveDiscParamIfIndirect)
    {
        this.preserveDiscParamIfIndirect = preserveDiscParamIfIndirect;
    }

    public ActionRouteRemotePreferred withPreserveDiscParamIfIndirect(PreserveDiscParamIfIndirect preserveDiscParamIfIndirect)
    {
        this.preserveDiscParamIfIndirect = preserveDiscParamIfIndirect;
        return this;
    }

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

    public ActionRouteRemotePreferred withFailoverProfileRef(String failoverProfileRef)
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

    public ActionRouteRemotePreferred withTargetNfPool(TargetNfPool targetNfPool)
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

    public ActionRouteRemotePreferred withLastResortNfPoolRef(String lastResortNfPoolRef)
    {
        this.lastResortNfPoolRef = lastResortNfPoolRef;
        return this;
    }

    /**
     * If present, route the request based on the target-api-root-header's value
     * 
     */
    @JsonProperty("from-target-api-root-header")
    public FromTargetApiRootHeader getFromTargetApiRootHeader()
    {
        return fromTargetApiRootHeader;
    }

    /**
     * If present, route the request based on the target-api-root-header's value
     * 
     */
    @JsonProperty("from-target-api-root-header")
    public void setFromTargetApiRootHeader(FromTargetApiRootHeader fromTargetApiRootHeader)
    {
        this.fromTargetApiRootHeader = fromTargetApiRootHeader;
    }

    public ActionRouteRemotePreferred withFromTargetApiRootHeader(FromTargetApiRootHeader fromTargetApiRootHeader)
    {
        this.fromTargetApiRootHeader = fromTargetApiRootHeader;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ActionRouteRemotePreferred.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("preserveIfIndirectRouting");
        sb.append('=');
        sb.append(((this.preserveIfIndirectRouting == null) ? "<null>" : this.preserveIfIndirectRouting));
        sb.append(',');
        sb.append("preserveDiscParamIfIndirect");
        sb.append('=');
        sb.append(((this.preserveDiscParamIfIndirect == null) ? "<null>" : this.preserveDiscParamIfIndirect));
        sb.append(',');
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
        sb.append("fromTargetApiRootHeader");
        sb.append('=');
        sb.append(((this.fromTargetApiRootHeader == null) ? "<null>" : this.fromTargetApiRootHeader));
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
        result = ((result * 31) + ((this.preserveDiscParamIfIndirect == null) ? 0 : this.preserveDiscParamIfIndirect.hashCode()));
        result = ((result * 31) + ((this.failoverProfileRef == null) ? 0 : this.failoverProfileRef.hashCode()));
        result = ((result * 31) + ((this.fromTargetApiRootHeader == null) ? 0 : this.fromTargetApiRootHeader.hashCode()));
        result = ((result * 31) + ((this.lastResortNfPoolRef == null) ? 0 : this.lastResortNfPoolRef.hashCode()));
        result = ((result * 31) + ((this.preserveIfIndirectRouting == null) ? 0 : this.preserveIfIndirectRouting.hashCode()));
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
        if ((other instanceof ActionRouteRemotePreferred) == false)
        {
            return false;
        }
        ActionRouteRemotePreferred rhs = ((ActionRouteRemotePreferred) other);
        return (((((((this.preserveDiscParamIfIndirect == rhs.preserveDiscParamIfIndirect)
                     || ((this.preserveDiscParamIfIndirect != null) && this.preserveDiscParamIfIndirect.equals(rhs.preserveDiscParamIfIndirect)))
                    && ((this.failoverProfileRef == rhs.failoverProfileRef)
                        || ((this.failoverProfileRef != null) && this.failoverProfileRef.equals(rhs.failoverProfileRef))))
                   && ((this.fromTargetApiRootHeader == rhs.fromTargetApiRootHeader)
                       || ((this.fromTargetApiRootHeader != null) && this.fromTargetApiRootHeader.equals(rhs.fromTargetApiRootHeader))))
                  && ((this.lastResortNfPoolRef == rhs.lastResortNfPoolRef)
                      || ((this.lastResortNfPoolRef != null) && this.lastResortNfPoolRef.equals(rhs.lastResortNfPoolRef))))
                 && ((this.preserveIfIndirectRouting == rhs.preserveIfIndirectRouting)
                     || ((this.preserveIfIndirectRouting != null) && this.preserveIfIndirectRouting.equals(rhs.preserveIfIndirectRouting))))
                && ((this.targetNfPool == rhs.targetNfPool) || ((this.targetNfPool != null) && this.targetNfPool.equals(rhs.targetNfPool))));
    }

}
