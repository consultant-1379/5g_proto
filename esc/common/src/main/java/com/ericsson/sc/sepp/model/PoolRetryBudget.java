
package com.ericsson.sc.sepp.model;

import com.ericsson.sc.glue.IfPoolRetryBudget;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Specifies a limit per pool on concurrent retries in relation to the integer
 * of active requests​
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "concurrent-retries-percentage", "min-concurrent-retries" })
public class PoolRetryBudget implements IfPoolRetryBudget
{

    /**
     * Specifies the limit on ​concurrent retries as a percentage of the sum of​
     * active requests and active pending requests.​For example, if there are 100
     * active requests and​ the budget_percent is set to 25, there may be 25​ active
     * retries
     * 
     */
    @JsonProperty("concurrent-retries-percentage")
    @JsonPropertyDescription("Specifies the limit on \u200bconcurrent retries as a percentage of the sum of\u200b active requests and active pending requests.\u200bFor example, if there are 100 active requests and\u200b the budget_percent is set to 25, there may be 25\u200b active retries")
    private Integer concurrentRetriesPercentage = 20;
    /**
     * Specifies the minimum retry concurrency allowed for​ the retry budget. The
     * limit on the integer of active​ retries may never go below this integer
     * 
     */
    @JsonProperty("min-concurrent-retries")
    @JsonPropertyDescription("Specifies the minimum retry concurrency allowed for\u200b the retry budget. The limit on the integer of active\u200b retries may never go below this integer")
    private Integer minConcurrentRetries = 10;

    /**
     * Specifies the limit on ​concurrent retries as a percentage of the sum of​
     * active requests and active pending requests.​For example, if there are 100
     * active requests and​ the budget_percent is set to 25, there may be 25​ active
     * retries
     * 
     */
    @JsonProperty("concurrent-retries-percentage")
    public Integer getConcurrentRetriesPercentage()
    {
        return concurrentRetriesPercentage;
    }

    /**
     * Specifies the limit on ​concurrent retries as a percentage of the sum of​
     * active requests and active pending requests.​For example, if there are 100
     * active requests and​ the budget_percent is set to 25, there may be 25​ active
     * retries
     * 
     */
    @JsonProperty("concurrent-retries-percentage")
    public void setConcurrentRetriesPercentage(Integer concurrentRetriesPercentage)
    {
        this.concurrentRetriesPercentage = concurrentRetriesPercentage;
    }

    public PoolRetryBudget withConcurrentRetriesPercentage(Integer concurrentRetriesPercentage)
    {
        this.concurrentRetriesPercentage = concurrentRetriesPercentage;
        return this;
    }

    /**
     * Specifies the minimum retry concurrency allowed for​ the retry budget. The
     * limit on the integer of active​ retries may never go below this integer
     * 
     */
    @JsonProperty("min-concurrent-retries")
    public Integer getMinConcurrentRetries()
    {
        return minConcurrentRetries;
    }

    /**
     * Specifies the minimum retry concurrency allowed for​ the retry budget. The
     * limit on the integer of active​ retries may never go below this integer
     * 
     */
    @JsonProperty("min-concurrent-retries")
    public void setMinConcurrentRetries(Integer minConcurrentRetries)
    {
        this.minConcurrentRetries = minConcurrentRetries;
    }

    public PoolRetryBudget withMinConcurrentRetries(Integer minConcurrentRetries)
    {
        this.minConcurrentRetries = minConcurrentRetries;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(PoolRetryBudget.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("concurrentRetriesPercentage");
        sb.append('=');
        sb.append(((this.concurrentRetriesPercentage == null) ? "<null>" : this.concurrentRetriesPercentage));
        sb.append(',');
        sb.append("minConcurrentRetries");
        sb.append('=');
        sb.append(((this.minConcurrentRetries == null) ? "<null>" : this.minConcurrentRetries));
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
        result = ((result * 31) + ((this.minConcurrentRetries == null) ? 0 : this.minConcurrentRetries.hashCode()));
        result = ((result * 31) + ((this.concurrentRetriesPercentage == null) ? 0 : this.concurrentRetriesPercentage.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof PoolRetryBudget) == false)
        {
            return false;
        }
        PoolRetryBudget rhs = ((PoolRetryBudget) other);
        return (((this.minConcurrentRetries == rhs.minConcurrentRetries)
                 || ((this.minConcurrentRetries != null) && this.minConcurrentRetries.equals(rhs.minConcurrentRetries)))
                && ((this.concurrentRetriesPercentage == rhs.concurrentRetriesPercentage)
                    || ((this.concurrentRetriesPercentage != null) && this.concurrentRetriesPercentage.equals(rhs.concurrentRetriesPercentage))));
    }

}
