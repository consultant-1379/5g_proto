
package com.ericsson.sc.scp.model;

import com.ericsson.sc.glue.IfActionRouteBase;
import com.ericsson.sc.glue.IfActionRouteTarget;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Route to the targeted host which belongs to the specified nf-pool. In case of
 * failure, re-select among the rest of the hosts in this nf-pool
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "keep-authority-header",
                     "preserve-if-indirect-routing",
                     "preserve-disc-param-if-indirect",
                     "failover-profile-ref",
                     "target-nf-pool",
                     "last-resort-nf-pool-ref",
                     "from-var-name",
                     "from-target-api-root-header",
                     "from-authority-header" })
public class ActionRoutePreferred implements IfActionRouteBase, IfActionRouteTarget
{

    /**
     * If present, keep the authority header of the request without modifying
     * 
     */
    @JsonProperty("keep-authority-header")
    @JsonPropertyDescription("If present, keep the authority header of the request without modifying")
    private KeepAuthorityHeader keepAuthorityHeader;
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
     * Route the request based on the defined variable
     * 
     */
    @JsonProperty("from-var-name")
    @JsonPropertyDescription("Route the request based on the defined variable")
    private String fromVarName;
    /**
     * If present, route the request based on the target-api-root-header's value
     * 
     */
    @JsonProperty("from-target-api-root-header")
    @JsonPropertyDescription("If present, route the request based on the target-api-root-header's value")
    private FromTargetApiRootHeader fromTargetApiRootHeader;
    /**
     * If present, route the request based on the authority-header's value
     * 
     */
    @JsonProperty("from-authority-header")
    @JsonPropertyDescription("If present, route the request based on the authority-header's value")
    private FromAuthorityHeader fromAuthorityHeader;

    /**
     * If present, keep the authority header of the request without modifying
     * 
     */
    @JsonProperty("keep-authority-header")
    public KeepAuthorityHeader getKeepAuthorityHeader()
    {
        return keepAuthorityHeader;
    }

    /**
     * If present, keep the authority header of the request without modifying
     * 
     */
    @JsonProperty("keep-authority-header")
    public void setKeepAuthorityHeader(KeepAuthorityHeader keepAuthorityHeader)
    {
        this.keepAuthorityHeader = keepAuthorityHeader;
    }

    public ActionRoutePreferred withKeepAuthorityHeader(KeepAuthorityHeader keepAuthorityHeader)
    {
        this.keepAuthorityHeader = keepAuthorityHeader;
        return this;
    }

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

    public ActionRoutePreferred withPreserveIfIndirectRouting(PreserveIfIndirectRouting preserveIfIndirectRouting)
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

    public ActionRoutePreferred withPreserveDiscParamIfIndirect(PreserveDiscParamIfIndirect preserveDiscParamIfIndirect)
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

    public ActionRoutePreferred withFromTargetApiRootHeader(FromTargetApiRootHeader fromTargetApiRootHeader)
    {
        this.fromTargetApiRootHeader = fromTargetApiRootHeader;
        return this;
    }

    /**
     * If present, route the request based on the authority-header's value
     * 
     */
    @JsonProperty("from-authority-header")
    public FromAuthorityHeader getFromAuthorityHeader()
    {
        return fromAuthorityHeader;
    }

    /**
     * If present, route the request based on the authority-header's value
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
        sb.append("keepAuthorityHeader");
        sb.append('=');
        sb.append(((this.keepAuthorityHeader == null) ? "<null>" : this.keepAuthorityHeader));
        sb.append(',');
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
        result = ((result * 31) + ((this.keepAuthorityHeader == null) ? 0 : this.keepAuthorityHeader.hashCode()));
        result = ((result * 31) + ((this.preserveDiscParamIfIndirect == null) ? 0 : this.preserveDiscParamIfIndirect.hashCode()));
        result = ((result * 31) + ((this.fromVarName == null) ? 0 : this.fromVarName.hashCode()));
        result = ((result * 31) + ((this.failoverProfileRef == null) ? 0 : this.failoverProfileRef.hashCode()));
        result = ((result * 31) + ((this.fromTargetApiRootHeader == null) ? 0 : this.fromTargetApiRootHeader.hashCode()));
        result = ((result * 31) + ((this.fromAuthorityHeader == null) ? 0 : this.fromAuthorityHeader.hashCode()));
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
        if ((other instanceof ActionRoutePreferred) == false)
        {
            return false;
        }
        ActionRoutePreferred rhs = ((ActionRoutePreferred) other);
        return ((((((((((this.keepAuthorityHeader == rhs.keepAuthorityHeader)
                        || ((this.keepAuthorityHeader != null) && this.keepAuthorityHeader.equals(rhs.keepAuthorityHeader)))
                       && ((this.preserveDiscParamIfIndirect == rhs.preserveDiscParamIfIndirect)
                           || ((this.preserveDiscParamIfIndirect != null) && this.preserveDiscParamIfIndirect.equals(rhs.preserveDiscParamIfIndirect))))
                      && ((this.fromVarName == rhs.fromVarName) || ((this.fromVarName != null) && this.fromVarName.equals(rhs.fromVarName))))
                     && ((this.failoverProfileRef == rhs.failoverProfileRef)
                         || ((this.failoverProfileRef != null) && this.failoverProfileRef.equals(rhs.failoverProfileRef))))
                    && ((this.fromTargetApiRootHeader == rhs.fromTargetApiRootHeader)
                        || ((this.fromTargetApiRootHeader != null) && this.fromTargetApiRootHeader.equals(rhs.fromTargetApiRootHeader))))
                   && ((this.fromAuthorityHeader == rhs.fromAuthorityHeader)
                       || ((this.fromAuthorityHeader != null) && this.fromAuthorityHeader.equals(rhs.fromAuthorityHeader))))
                  && ((this.lastResortNfPoolRef == rhs.lastResortNfPoolRef)
                      || ((this.lastResortNfPoolRef != null) && this.lastResortNfPoolRef.equals(rhs.lastResortNfPoolRef))))
                 && ((this.preserveIfIndirectRouting == rhs.preserveIfIndirectRouting)
                     || ((this.preserveIfIndirectRouting != null) && this.preserveIfIndirectRouting.equals(rhs.preserveIfIndirectRouting))))
                && ((this.targetNfPool == rhs.targetNfPool) || ((this.targetNfPool != null) && this.targetNfPool.equals(rhs.targetNfPool))));
    }

}
