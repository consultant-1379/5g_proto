
package com.ericsson.sc.bsf.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Configuration settings for multiple bindings handling.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "http-lookup", "diameter-lookup" })
public class MultipleBindingResolution
{

    /**
     * Configuration dedicated for multiple binding handling on http-lookup.
     * 
     */
    @JsonProperty("http-lookup")
    @JsonPropertyDescription("Configuration dedicated for multiple binding handling on http-lookup.")
    private HttpLookup httpLookup;
    /**
     * Configuration dedicated for multiple binding handling on diameter-lookup.
     * 
     */
    @JsonProperty("diameter-lookup")
    @JsonPropertyDescription("Configuration dedicated for multiple binding handling on diameter-lookup.")
    private DiameterLookup diameterLookup;

    /**
     * Configuration dedicated for multiple binding handling on http-lookup.
     * 
     */
    @JsonProperty("http-lookup")
    public HttpLookup getHttpLookup()
    {
        return httpLookup;
    }

    /**
     * Configuration dedicated for multiple binding handling on http-lookup.
     * 
     */
    @JsonProperty("http-lookup")
    public void setHttpLookup(HttpLookup httpLookup)
    {
        this.httpLookup = httpLookup;
    }

    public MultipleBindingResolution withHttpLookup(HttpLookup httpLookup)
    {
        this.httpLookup = httpLookup;
        return this;
    }

    /**
     * Configuration dedicated for multiple binding handling on diameter-lookup.
     * 
     */
    @JsonProperty("diameter-lookup")
    public DiameterLookup getDiameterLookup()
    {
        return diameterLookup;
    }

    /**
     * Configuration dedicated for multiple binding handling on diameter-lookup.
     * 
     */
    @JsonProperty("diameter-lookup")
    public void setDiameterLookup(DiameterLookup diameterLookup)
    {
        this.diameterLookup = diameterLookup;
    }

    public MultipleBindingResolution withDiameterLookup(DiameterLookup diameterLookup)
    {
        this.diameterLookup = diameterLookup;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(MultipleBindingResolution.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("httpLookup");
        sb.append('=');
        sb.append(((this.httpLookup == null) ? "<null>" : this.httpLookup));
        sb.append(',');
        sb.append("diameterLookup");
        sb.append('=');
        sb.append(((this.diameterLookup == null) ? "<null>" : this.diameterLookup));
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
        result = ((result * 31) + ((this.httpLookup == null) ? 0 : this.httpLookup.hashCode()));
        result = ((result * 31) + ((this.diameterLookup == null) ? 0 : this.diameterLookup.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof MultipleBindingResolution) == false)
        {
            return false;
        }
        MultipleBindingResolution rhs = ((MultipleBindingResolution) other);
        return (((this.httpLookup == rhs.httpLookup) || ((this.httpLookup != null) && this.httpLookup.equals(rhs.httpLookup)))
                && ((this.diameterLookup == rhs.diameterLookup) || ((this.diameterLookup != null) && this.diameterLookup.equals(rhs.diameterLookup))));
    }

}
