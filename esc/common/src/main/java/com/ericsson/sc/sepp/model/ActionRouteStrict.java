
package com.ericsson.sc.sepp.model;

import com.ericsson.sc.glue.IfActionRouteBase;
import com.ericsson.sc.glue.IfActionRouteTarget;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Route strictly to the targeted host
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "failover-profile-ref", "target-nf-pool", "from-var-name", "from-target-api-root-header", "from-authority-header" })
public class ActionRouteStrict implements IfActionRouteBase, IfActionRouteTarget
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
     * Route the request based on the defined variable
     * 
     */
    @JsonProperty("from-var-name")
    @JsonPropertyDescription("Route the request based on the defined variable")
    private String fromVarName;
    /**
     * If present, route the request based on the target-api-root-headers value
     * 
     */
    @JsonProperty("from-target-api-root-header")
    @JsonPropertyDescription("If present, route the request based on the target-api-root-headers value")
    private FromTargetApiRootHeader fromTargetApiRootHeader;
    /**
     * If present, route the request based on the authority-headers value
     * 
     */
    @JsonProperty("from-authority-header")
    @JsonPropertyDescription("If present, route the request based on the authority-headers value")
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

    public ActionRouteStrict withFailoverProfileRef(String failoverProfileRef)
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

    public ActionRouteStrict withTargetNfPool(TargetNfPool targetNfPool)
    {
        this.targetNfPool = targetNfPool;
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

    public ActionRouteStrict withFromVarName(String fromVarName)
    {
        this.fromVarName = fromVarName;
        return this;
    }

    /**
     * If present, route the request based on the target-api-root-headers value
     * 
     */
    @JsonProperty("from-target-api-root-header")
    public FromTargetApiRootHeader getFromTargetApiRootHeader()
    {
        return fromTargetApiRootHeader;
    }

    /**
     * If present, route the request based on the target-api-root-headers value
     * 
     */
    @JsonProperty("from-target-api-root-header")
    public void setFromTargetApiRootHeader(FromTargetApiRootHeader fromTargetApiRootHeader)
    {
        this.fromTargetApiRootHeader = fromTargetApiRootHeader;
    }

    public ActionRouteStrict withFromTargetApiRootHeader(FromTargetApiRootHeader fromTargetApiRootHeader)
    {
        this.fromTargetApiRootHeader = fromTargetApiRootHeader;
        return this;
    }

    /**
     * If present, route the request based on the authority-headers value
     * 
     */
    @JsonProperty("from-authority-header")
    public FromAuthorityHeader getFromAuthorityHeader()
    {
        return fromAuthorityHeader;
    }

    /**
     * If present, route the request based on the authority-headers value
     * 
     */
    @JsonProperty("from-authority-header")
    public void setFromAuthorityHeader(FromAuthorityHeader fromAuthorityHeader)
    {
        this.fromAuthorityHeader = fromAuthorityHeader;
    }

    public ActionRouteStrict withFromAuthorityHeader(FromAuthorityHeader fromAuthorityHeader)
    {
        this.fromAuthorityHeader = fromAuthorityHeader;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ActionRouteStrict.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("failoverProfileRef");
        sb.append('=');
        sb.append(((this.failoverProfileRef == null) ? "<null>" : this.failoverProfileRef));
        sb.append(',');
        sb.append("targetNfPool");
        sb.append('=');
        sb.append(((this.targetNfPool == null) ? "<null>" : this.targetNfPool));
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
        result = ((result * 31) + ((this.fromAuthorityHeader == null) ? 0 : this.fromAuthorityHeader.hashCode()));
        result = ((result * 31) + ((this.failoverProfileRef == null) ? 0 : this.failoverProfileRef.hashCode()));
        result = ((result * 31) + ((this.fromTargetApiRootHeader == null) ? 0 : this.fromTargetApiRootHeader.hashCode()));
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
        if ((other instanceof ActionRouteStrict) == false)
        {
            return false;
        }
        ActionRouteStrict rhs = ((ActionRouteStrict) other);
        return ((((((this.fromVarName == rhs.fromVarName) || ((this.fromVarName != null) && this.fromVarName.equals(rhs.fromVarName)))
                   && ((this.fromAuthorityHeader == rhs.fromAuthorityHeader)
                       || ((this.fromAuthorityHeader != null) && this.fromAuthorityHeader.equals(rhs.fromAuthorityHeader))))
                  && ((this.failoverProfileRef == rhs.failoverProfileRef)
                      || ((this.failoverProfileRef != null) && this.failoverProfileRef.equals(rhs.failoverProfileRef))))
                 && ((this.fromTargetApiRootHeader == rhs.fromTargetApiRootHeader)
                     || ((this.fromTargetApiRootHeader != null) && this.fromTargetApiRootHeader.equals(rhs.fromTargetApiRootHeader))))
                && ((this.targetNfPool == rhs.targetNfPool) || ((this.targetNfPool != null) && this.targetNfPool.equals(rhs.targetNfPool))));
    }

}
