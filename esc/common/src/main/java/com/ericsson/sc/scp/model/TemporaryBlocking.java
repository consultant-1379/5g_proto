
package com.ericsson.sc.scp.model;

import com.ericsson.sc.glue.IfTemporaryBlocking;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Temporary blocking allows the dynamic blocking of NFs which are not
 * reachable, based on configurable and preset attributes
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "blocking-time", "consecutive-failures", "consecutive-local-failures", "consecutive-gateway-failures" })
public class TemporaryBlocking implements IfTemporaryBlocking
{

    /**
     * The duration of time for which a NF is blocked
     * 
     */
    @JsonProperty("blocking-time")
    @JsonPropertyDescription("The duration of time for which a NF is blocked")
    private Integer blockingTime = 5;
    /**
     * The integer of consecutive 5xx errors from a NF node before it is temporarily
     * blocked. If consecutive-local-failures parameter is not configured, then this
     * value also represents the locally originated failures
     * 
     */
    @JsonProperty("consecutive-failures")
    @JsonPropertyDescription("The integer of consecutive 5xx errors from a NF node before it is temporarily blocked. If consecutive-local-failures parameter is not configured, then this value also represents the locally originated failures")
    private Integer consecutiveFailures = 5;
    /**
     * The integer of consecutive locally originated failures per worker pod before
     * an NF is temporary blocked. If this parameter is configured, then the locally
     * originated failures are counted separately from the consecutive 5xx errors
     * for temporary blocking. If this parameter is set to zero then locally
     * originated failures do not lead to temporary blocking in this pool
     * 
     */
    @JsonProperty("consecutive-local-failures")
    @JsonPropertyDescription("The integer of consecutive locally originated failures per worker pod before an NF is temporary blocked. If this parameter is configured, then the locally originated failures are counted separately from the consecutive 5xx errors for temporary blocking. If this parameter is set to zero then locally originated failures do not lead to temporary blocking in this pool")
    private Integer consecutiveLocalFailures;
    /**
     * The integer of consecutive gateway failures (502, 503, 504 status codes)
     * before a consecutive gateway failure ejection occurs. If this parameter is
     * configured, then the consecutive gateway failures are counted separately from
     * the consecutive 5xx errors for temporary blocking. If this parameter is set
     * to zero then consecutive gateway failures do not lead to temporary blocking
     * in this pool
     * 
     */
    @JsonProperty("consecutive-gateway-failures")
    @JsonPropertyDescription("The integer of consecutive gateway failures (502, 503, 504 status codes) before a consecutive gateway failure ejection occurs. If this parameter is configured, then the consecutive gateway failures are counted separately from the consecutive 5xx errors for temporary blocking. If this parameter is set to zero then consecutive gateway failures do not lead to temporary blocking in this pool")
    private Integer consecutiveGatewayFailures;

    /**
     * The duration of time for which a NF is blocked
     * 
     */
    @JsonProperty("blocking-time")
    public Integer getBlockingTime()
    {
        return blockingTime;
    }

    /**
     * The duration of time for which a NF is blocked
     * 
     */
    @JsonProperty("blocking-time")
    public void setBlockingTime(Integer blockingTime)
    {
        this.blockingTime = blockingTime;
    }

    public TemporaryBlocking withBlockingTime(Integer blockingTime)
    {
        this.blockingTime = blockingTime;
        return this;
    }

    /**
     * The integer of consecutive 5xx errors from a NF node before it is temporarily
     * blocked. If consecutive-local-failures parameter is not configured, then this
     * value also represents the locally originated failures
     * 
     */
    @JsonProperty("consecutive-failures")
    public Integer getConsecutiveFailures()
    {
        return consecutiveFailures;
    }

    /**
     * The integer of consecutive 5xx errors from a NF node before it is temporarily
     * blocked. If consecutive-local-failures parameter is not configured, then this
     * value also represents the locally originated failures
     * 
     */
    @JsonProperty("consecutive-failures")
    public void setConsecutiveFailures(Integer consecutiveFailures)
    {
        this.consecutiveFailures = consecutiveFailures;
    }

    public TemporaryBlocking withConsecutiveFailures(Integer consecutiveFailures)
    {
        this.consecutiveFailures = consecutiveFailures;
        return this;
    }

    /**
     * The integer of consecutive locally originated failures per worker pod before
     * an NF is temporary blocked. If this parameter is configured, then the locally
     * originated failures are counted separately from the consecutive 5xx errors
     * for temporary blocking. If this parameter is set to zero then locally
     * originated failures do not lead to temporary blocking in this pool
     * 
     */
    @JsonProperty("consecutive-local-failures")
    public Integer getConsecutiveLocalFailures()
    {
        return consecutiveLocalFailures;
    }

    /**
     * The integer of consecutive locally originated failures per worker pod before
     * an NF is temporary blocked. If this parameter is configured, then the locally
     * originated failures are counted separately from the consecutive 5xx errors
     * for temporary blocking. If this parameter is set to zero then locally
     * originated failures do not lead to temporary blocking in this pool
     * 
     */
    @JsonProperty("consecutive-local-failures")
    public void setConsecutiveLocalFailures(Integer consecutiveLocalFailures)
    {
        this.consecutiveLocalFailures = consecutiveLocalFailures;
    }

    public TemporaryBlocking withConsecutiveLocalFailures(Integer consecutiveLocalFailures)
    {
        this.consecutiveLocalFailures = consecutiveLocalFailures;
        return this;
    }

    /**
     * The integer of consecutive gateway failures (502, 503, 504 status codes)
     * before a consecutive gateway failure ejection occurs. If this parameter is
     * configured, then the consecutive gateway failures are counted separately from
     * the consecutive 5xx errors for temporary blocking. If this parameter is set
     * to zero then consecutive gateway failures do not lead to temporary blocking
     * in this pool
     * 
     */
    @JsonProperty("consecutive-gateway-failures")
    public Integer getConsecutiveGatewayFailures()
    {
        return consecutiveGatewayFailures;
    }

    /**
     * The integer of consecutive gateway failures (502, 503, 504 status codes)
     * before a consecutive gateway failure ejection occurs. If this parameter is
     * configured, then the consecutive gateway failures are counted separately from
     * the consecutive 5xx errors for temporary blocking. If this parameter is set
     * to zero then consecutive gateway failures do not lead to temporary blocking
     * in this pool
     * 
     */
    @JsonProperty("consecutive-gateway-failures")
    public void setConsecutiveGatewayFailures(Integer consecutiveGatewayFailures)
    {
        this.consecutiveGatewayFailures = consecutiveGatewayFailures;
    }

    public TemporaryBlocking withConsecutiveGatewayFailures(Integer consecutiveGatewayFailures)
    {
        this.consecutiveGatewayFailures = consecutiveGatewayFailures;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(TemporaryBlocking.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("blockingTime");
        sb.append('=');
        sb.append(((this.blockingTime == null) ? "<null>" : this.blockingTime));
        sb.append(',');
        sb.append("consecutiveFailures");
        sb.append('=');
        sb.append(((this.consecutiveFailures == null) ? "<null>" : this.consecutiveFailures));
        sb.append(',');
        sb.append("consecutiveLocalFailures");
        sb.append('=');
        sb.append(((this.consecutiveLocalFailures == null) ? "<null>" : this.consecutiveLocalFailures));
        sb.append(',');
        sb.append("consecutiveGatewayFailures");
        sb.append('=');
        sb.append(((this.consecutiveGatewayFailures == null) ? "<null>" : this.consecutiveGatewayFailures));
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
        result = ((result * 31) + ((this.consecutiveFailures == null) ? 0 : this.consecutiveFailures.hashCode()));
        result = ((result * 31) + ((this.blockingTime == null) ? 0 : this.blockingTime.hashCode()));
        result = ((result * 31) + ((this.consecutiveLocalFailures == null) ? 0 : this.consecutiveLocalFailures.hashCode()));
        result = ((result * 31) + ((this.consecutiveGatewayFailures == null) ? 0 : this.consecutiveGatewayFailures.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof TemporaryBlocking) == false)
        {
            return false;
        }
        TemporaryBlocking rhs = ((TemporaryBlocking) other);
        return (((((this.consecutiveFailures == rhs.consecutiveFailures)
                   || ((this.consecutiveFailures != null) && this.consecutiveFailures.equals(rhs.consecutiveFailures)))
                  && ((this.blockingTime == rhs.blockingTime) || ((this.blockingTime != null) && this.blockingTime.equals(rhs.blockingTime))))
                 && ((this.consecutiveLocalFailures == rhs.consecutiveLocalFailures)
                     || ((this.consecutiveLocalFailures != null) && this.consecutiveLocalFailures.equals(rhs.consecutiveLocalFailures))))
                && ((this.consecutiveGatewayFailures == rhs.consecutiveGatewayFailures)
                    || ((this.consecutiveGatewayFailures != null) && this.consecutiveGatewayFailures.equals(rhs.consecutiveGatewayFailures))));
    }

}
