
package com.ericsson.sc.nfm.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "min-http-status-code", "max-http-status-code" })
public class ExpectedResponseHttpStatusCode
{

    /**
     * Min status code of the http status code range.
     * 
     */
    @JsonProperty("min-http-status-code")
    @JsonPropertyDescription("Min status code of the http status code range.")
    private Integer minHttpStatusCode;
    /**
     * Max status code of the http status code range.
     * 
     */
    @JsonProperty("max-http-status-code")
    @JsonPropertyDescription("Max status code of the http status code range.")
    private Integer maxHttpStatusCode;

    /**
     * Min status code of the http status code range.
     * 
     */
    @JsonProperty("min-http-status-code")
    public Integer getMinHttpStatusCode()
    {
        return minHttpStatusCode;
    }

    /**
     * Min status code of the http status code range.
     * 
     */
    @JsonProperty("min-http-status-code")
    public void setMinHttpStatusCode(Integer minHttpStatusCode)
    {
        this.minHttpStatusCode = minHttpStatusCode;
    }

    public ExpectedResponseHttpStatusCode withMinHttpStatusCode(Integer minHttpStatusCode)
    {
        this.minHttpStatusCode = minHttpStatusCode;
        return this;
    }

    /**
     * Max status code of the http status code range.
     * 
     */
    @JsonProperty("max-http-status-code")
    public Integer getMaxHttpStatusCode()
    {
        return maxHttpStatusCode;
    }

    /**
     * Max status code of the http status code range.
     * 
     */
    @JsonProperty("max-http-status-code")
    public void setMaxHttpStatusCode(Integer maxHttpStatusCode)
    {
        this.maxHttpStatusCode = maxHttpStatusCode;
    }

    public ExpectedResponseHttpStatusCode withMaxHttpStatusCode(Integer maxHttpStatusCode)
    {
        this.maxHttpStatusCode = maxHttpStatusCode;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ExpectedResponseHttpStatusCode.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("minHttpStatusCode");
        sb.append('=');
        sb.append(((this.minHttpStatusCode == null) ? "<null>" : this.minHttpStatusCode));
        sb.append(',');
        sb.append("maxHttpStatusCode");
        sb.append('=');
        sb.append(((this.maxHttpStatusCode == null) ? "<null>" : this.maxHttpStatusCode));
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
        result = ((result * 31) + ((this.maxHttpStatusCode == null) ? 0 : this.maxHttpStatusCode.hashCode()));
        result = ((result * 31) + ((this.minHttpStatusCode == null) ? 0 : this.minHttpStatusCode.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof ExpectedResponseHttpStatusCode) == false)
        {
            return false;
        }
        ExpectedResponseHttpStatusCode rhs = ((ExpectedResponseHttpStatusCode) other);
        return (((this.maxHttpStatusCode == rhs.maxHttpStatusCode)
                 || ((this.maxHttpStatusCode != null) && this.maxHttpStatusCode.equals(rhs.maxHttpStatusCode)))
                && ((this.minHttpStatusCode == rhs.minHttpStatusCode)
                    || ((this.minHttpStatusCode != null) && this.minHttpStatusCode.equals(rhs.minHttpStatusCode))));
    }

}
