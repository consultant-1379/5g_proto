
package com.ericsson.sc.scp.model;

import com.ericsson.sc.glue.IfTargetNfPool;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Route via the referenced configured nf-pool or variable that holds the
 * nf-pool name
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "var-name", "nf-pool-ref" })
public class TargetNfPool implements IfTargetNfPool
{

    /**
     * Variable that holds the pool name
     * 
     */
    @JsonProperty("var-name")
    @JsonPropertyDescription("Variable that holds the pool name")
    private String varName;
    /**
     * Name of the referenced configured nf-pool
     * 
     */
    @JsonProperty("nf-pool-ref")
    @JsonPropertyDescription("Name of the referenced configured nf-pool")
    private String nfPoolRef;

    /**
     * Variable that holds the pool name
     * 
     */
    @JsonProperty("var-name")
    public String getVarName()
    {
        return varName;
    }

    /**
     * Variable that holds the pool name
     * 
     */
    @JsonProperty("var-name")
    public void setVarName(String varName)
    {
        this.varName = varName;
    }

    public TargetNfPool withVarName(String varName)
    {
        this.varName = varName;
        return this;
    }

    /**
     * Name of the referenced configured nf-pool
     * 
     */
    @JsonProperty("nf-pool-ref")
    public String getNfPoolRef()
    {
        return nfPoolRef;
    }

    /**
     * Name of the referenced configured nf-pool
     * 
     */
    @JsonProperty("nf-pool-ref")
    public void setNfPoolRef(String nfPoolRef)
    {
        this.nfPoolRef = nfPoolRef;
    }

    public TargetNfPool withNfPoolRef(String nfPoolRef)
    {
        this.nfPoolRef = nfPoolRef;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(TargetNfPool.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("varName");
        sb.append('=');
        sb.append(((this.varName == null) ? "<null>" : this.varName));
        sb.append(',');
        sb.append("nfPoolRef");
        sb.append('=');
        sb.append(((this.nfPoolRef == null) ? "<null>" : this.nfPoolRef));
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
        result = ((result * 31) + ((this.nfPoolRef == null) ? 0 : this.nfPoolRef.hashCode()));
        result = ((result * 31) + ((this.varName == null) ? 0 : this.varName.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof TargetNfPool) == false)
        {
            return false;
        }
        TargetNfPool rhs = ((TargetNfPool) other);
        return (((this.nfPoolRef == rhs.nfPoolRef) || ((this.nfPoolRef != null) && this.nfPoolRef.equals(rhs.nfPoolRef)))
                && ((this.varName == rhs.varName) || ((this.varName != null) && this.varName.equals(rhs.varName))));
    }

}
