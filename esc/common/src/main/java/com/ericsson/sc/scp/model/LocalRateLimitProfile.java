
package com.ericsson.sc.scp.model;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.sc.glue.IfLocalRateLimitProfile;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "token-bucket", "add-response-header" })
public class LocalRateLimitProfile implements IfLocalRateLimitProfile
{

    /**
     * Name identifying the local-rate-limit-profile (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name identifying the local-rate-limit-profile")
    private String name;
    /**
     * Token bucket configuration based on which local rate limiting will be applied
     * (Required)
     * 
     */
    @JsonProperty("token-bucket")
    @JsonPropertyDescription("Token bucket configuration based on which local rate limiting will be applied")
    private TokenBucket tokenBucket;
    /**
     * Headers to be added on the response to a request that is rejected due to rate
     * limiting
     * 
     */
    @JsonProperty("add-response-header")
    @JsonPropertyDescription("Headers to be added on the response to a request that is rejected due to rate limiting")
    private List<AddResponseHeader> addResponseHeader = new ArrayList<AddResponseHeader>();

    /**
     * Name identifying the local-rate-limit-profile (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name identifying the local-rate-limit-profile (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public LocalRateLimitProfile withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Token bucket configuration based on which local rate limiting will be applied
     * (Required)
     * 
     */
    @JsonProperty("token-bucket")
    public TokenBucket getTokenBucket()
    {
        return tokenBucket;
    }

    /**
     * Token bucket configuration based on which local rate limiting will be applied
     * (Required)
     * 
     */
    @JsonProperty("token-bucket")
    public void setTokenBucket(TokenBucket tokenBucket)
    {
        this.tokenBucket = tokenBucket;
    }

    public LocalRateLimitProfile withTokenBucket(TokenBucket tokenBucket)
    {
        this.tokenBucket = tokenBucket;
        return this;
    }

    /**
     * Headers to be added on the response to a request that is rejected due to rate
     * limiting
     * 
     */
    @JsonProperty("add-response-header")
    public List<AddResponseHeader> getAddResponseHeader()
    {
        return addResponseHeader;
    }

    /**
     * Headers to be added on the response to a request that is rejected due to rate
     * limiting
     * 
     */
    @JsonProperty("add-response-header")
    public void setAddResponseHeader(List<AddResponseHeader> addResponseHeader)
    {
        this.addResponseHeader = addResponseHeader;
    }

    public LocalRateLimitProfile withAddResponseHeader(List<AddResponseHeader> addResponseHeader)
    {
        this.addResponseHeader = addResponseHeader;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(LocalRateLimitProfile.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("tokenBucket");
        sb.append('=');
        sb.append(((this.tokenBucket == null) ? "<null>" : this.tokenBucket));
        sb.append(',');
        sb.append("addResponseHeader");
        sb.append('=');
        sb.append(((this.addResponseHeader == null) ? "<null>" : this.addResponseHeader));
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
        result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
        result = ((result * 31) + ((this.addResponseHeader == null) ? 0 : this.addResponseHeader.hashCode()));
        result = ((result * 31) + ((this.tokenBucket == null) ? 0 : this.tokenBucket.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof LocalRateLimitProfile) == false)
        {
            return false;
        }
        LocalRateLimitProfile rhs = ((LocalRateLimitProfile) other);
        return ((((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
                 && ((this.addResponseHeader == rhs.addResponseHeader)
                     || ((this.addResponseHeader != null) && this.addResponseHeader.equals(rhs.addResponseHeader))))
                && ((this.tokenBucket == rhs.tokenBucket) || ((this.tokenBucket != null) && this.tokenBucket.equals(rhs.tokenBucket))));
    }

}
