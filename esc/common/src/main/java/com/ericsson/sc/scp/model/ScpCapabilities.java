
package com.ericsson.sc.scp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * If present, scp capabilities are published in nrf during registration. If
 * present but no scp capabilities are defined, then an empty array is sent to
 * NRF.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "indirect-com-with-deleg-disc" })
public class ScpCapabilities
{

    /**
     * If true, scp has the capability for delegated discovery support
     * 
     */
    @JsonProperty("indirect-com-with-deleg-disc")
    @JsonPropertyDescription("If true, scp has the capability for delegated discovery support")
    private Boolean indirectComWithDelegDisc;

    /**
     * If true, scp has the capability for delegated discovery support
     * 
     */
    @JsonProperty("indirect-com-with-deleg-disc")
    public Boolean getIndirectComWithDelegDisc()
    {
        return indirectComWithDelegDisc;
    }

    /**
     * If true, scp has the capability for delegated discovery support
     * 
     */
    @JsonProperty("indirect-com-with-deleg-disc")
    public void setIndirectComWithDelegDisc(Boolean indirectComWithDelegDisc)
    {
        this.indirectComWithDelegDisc = indirectComWithDelegDisc;
    }

    public ScpCapabilities withIndirectComWithDelegDisc(Boolean indirectComWithDelegDisc)
    {
        this.indirectComWithDelegDisc = indirectComWithDelegDisc;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ScpCapabilities.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("indirectComWithDelegDisc");
        sb.append('=');
        sb.append(((this.indirectComWithDelegDisc == null) ? "<null>" : this.indirectComWithDelegDisc));
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
        result = ((result * 31) + ((this.indirectComWithDelegDisc == null) ? 0 : this.indirectComWithDelegDisc.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof ScpCapabilities) == false)
        {
            return false;
        }
        ScpCapabilities rhs = ((ScpCapabilities) other);
        return ((this.indirectComWithDelegDisc == rhs.indirectComWithDelegDisc)
                || ((this.indirectComWithDelegDisc != null) && this.indirectComWithDelegDisc.equals(rhs.indirectComWithDelegDisc)));
    }

    @JsonIgnore
    public boolean isEmpty()
    {
        return indirectComWithDelegDisc == null;
    }

}
