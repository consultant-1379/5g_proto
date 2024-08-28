
package com.ericsson.sc.sepp.model;

import com.ericsson.sc.glue.IfTokenBucket;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Token bucket configuration based on which local rate limiting will be applied
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "max-tokens", "tokens-per-fill", "fill-interval" })
public class TokenBucket implements IfTokenBucket
{

    /**
     * The maximum tokens that the bucket can hold, as well ass the integer of
     * tokens the bucket initially contains (Required)
     * 
     */
    @JsonProperty("max-tokens")
    @JsonPropertyDescription("The maximum tokens that the bucket can hold, as well as the integer of tokens the bucket initially contains")
    private Integer maxTokens;
    /**
     * The integer of tokens added to the bucket at the beginning of each
     * fill-interval (Required)
     * 
     */
    @JsonProperty("tokens-per-fill")
    @JsonPropertyDescription("The integer of tokens added to the bucket at the beginning of each fill-interval")
    private Integer tokensPerFill;
    /**
     * The fill interval at which tokens are added to the bucket, defined in
     * milliseconds (Required)
     * 
     */
    @JsonProperty("fill-interval")
    @JsonPropertyDescription("The fill interval at which tokens are added to the bucket, defined in milliseconds")
    private Integer fillInterval;

    /**
     * The maximum tokens that the bucket can hold, as well ass the integer of
     * tokens the bucket initially contains (Required)
     * 
     */
    @JsonProperty("max-tokens")
    public Integer getMaxTokens()
    {
        return maxTokens;
    }

    /**
     * The maximum tokens that the bucket can hold, as well ass the integer of
     * tokens the bucket initially contains (Required)
     * 
     */
    @JsonProperty("max-tokens")
    public void setMaxTokens(Integer maxTokens)
    {
        this.maxTokens = maxTokens;
    }

    public TokenBucket withMaxTokens(Integer maxTokens)
    {
        this.maxTokens = maxTokens;
        return this;
    }

    /**
     * The integer of tokens added to the bucket at the beginning of each
     * fill-interval (Required)
     * 
     */
    @JsonProperty("tokens-per-fill")
    public Integer getTokensPerFill()
    {
        return tokensPerFill;
    }

    /**
     * The integer of tokens added to the bucket at the beginning of each
     * fill-interval (Required)
     * 
     */
    @JsonProperty("tokens-per-fill")
    public void setTokensPerFill(Integer tokensPerFill)
    {
        this.tokensPerFill = tokensPerFill;
    }

    public TokenBucket withTokensPerFill(Integer tokensPerFill)
    {
        this.tokensPerFill = tokensPerFill;
        return this;
    }

    /**
     * The fill interval at which tokens are added to the bucket, defined in
     * milliseconds (Required)
     * 
     */
    @JsonProperty("fill-interval")
    public Integer getFillInterval()
    {
        return fillInterval;
    }

    /**
     * The fill interval at which tokens are added to the bucket, defined in
     * milliseconds (Required)
     * 
     */
    @JsonProperty("fill-interval")
    public void setFillInterval(Integer fillInterval)
    {
        this.fillInterval = fillInterval;
    }

    public TokenBucket withFillInterval(Integer fillInterval)
    {
        this.fillInterval = fillInterval;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(TokenBucket.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("maxTokens");
        sb.append('=');
        sb.append(((this.maxTokens == null) ? "<null>" : this.maxTokens));
        sb.append(',');
        sb.append("tokensPerFill");
        sb.append('=');
        sb.append(((this.tokensPerFill == null) ? "<null>" : this.tokensPerFill));
        sb.append(',');
        sb.append("fillInterval");
        sb.append('=');
        sb.append(((this.fillInterval == null) ? "<null>" : this.fillInterval));
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
        result = ((result * 31) + ((this.maxTokens == null) ? 0 : this.maxTokens.hashCode()));
        result = ((result * 31) + ((this.fillInterval == null) ? 0 : this.fillInterval.hashCode()));
        result = ((result * 31) + ((this.tokensPerFill == null) ? 0 : this.tokensPerFill.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof TokenBucket) == false)
        {
            return false;
        }
        TokenBucket rhs = ((TokenBucket) other);
        return ((((this.maxTokens == rhs.maxTokens) || ((this.maxTokens != null) && this.maxTokens.equals(rhs.maxTokens)))
                 && ((this.fillInterval == rhs.fillInterval) || ((this.fillInterval != null) && this.fillInterval.equals(rhs.fillInterval))))
                && ((this.tokensPerFill == rhs.tokensPerFill) || ((this.tokensPerFill != null) && this.tokensPerFill.equals(rhs.tokensPerFill))));
    }

}
