
package com.ericsson.sc.scp.model;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.sc.glue.IfRetryCondition;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Criteria for attempting a retry
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "http-status", "reset", "connect-failure", "refused-stream" })
public class RetryCondition implements IfRetryCondition
{

    /**
     * HTTP Status codes to trigger a retry
     * 
     */
    @JsonProperty("http-status")
    @JsonPropertyDescription("HTTP Status codes to trigger a retry")
    private List<Integer> httpStatus = new ArrayList<Integer>();
    /**
     * If defined, retry when the peer does not respond at all
     * 
     */
    @JsonProperty("reset")
    @JsonPropertyDescription("If defined, retry when the peer does not respond at all")
    private Boolean reset = true;
    /**
     * If defined, retry in case of connection timeout or connection refused
     * 
     */
    @JsonProperty("connect-failure")
    @JsonPropertyDescription("If defined, retry in case of connection timeout or connection refused")
    private Boolean connectFailure = true;
    /**
     * If defined, retry in case of response HTTP2 REFUSED_STREAM received from peer
     * 
     */
    @JsonProperty("refused-stream")
    @JsonPropertyDescription("If defined, retry in case of response HTTP2 REFUSED_STREAM received from peer")
    private Boolean refusedStream = true;

    /**
     * @param retryCondition
     */
    public RetryCondition(RetryCondition other)
    {
        this.connectFailure = other.connectFailure;
        this.httpStatus = new ArrayList<>(other.httpStatus);
        this.refusedStream = other.refusedStream;
        this.reset = other.reset;
    }

    /**
     * 
     */
    public RetryCondition()
    {
    }

    /**
     * HTTP Status codes to trigger a retry
     * 
     */
    @JsonProperty("http-status")
    public List<Integer> getHttpStatus()
    {
        return httpStatus;
    }

    /**
     * HTTP Status codes to trigger a retry
     * 
     */
    @JsonProperty("http-status")
    public void setHttpStatus(List<Integer> httpStatus)
    {
        this.httpStatus = httpStatus;
    }

    public RetryCondition withHttpStatus(List<Integer> httpStatus)
    {
        this.httpStatus = httpStatus;
        return this;
    }

    /**
     * If defined, retry when the peer does not respond at all
     * 
     */
    @JsonProperty("reset")
    public Boolean getReset()
    {
        return reset;
    }

    /**
     * If defined, retry when the peer does not respond at all
     * 
     */
    @JsonProperty("reset")
    public void setReset(Boolean reset)
    {
        this.reset = reset;
    }

    public RetryCondition withReset(Boolean reset)
    {
        this.reset = reset;
        return this;
    }

    /**
     * If defined, retry in case of connection timeout or connection refused
     * 
     */
    @JsonProperty("connect-failure")
    public Boolean getConnectFailure()
    {
        return connectFailure;
    }

    /**
     * If defined, retry in case of connection timeout or connection refused
     * 
     */
    @JsonProperty("connect-failure")
    public void setConnectFailure(Boolean connectFailure)
    {
        this.connectFailure = connectFailure;
    }

    public RetryCondition withConnectFailure(Boolean connectFailure)
    {
        this.connectFailure = connectFailure;
        return this;
    }

    /**
     * If defined, retry in case of response HTTP2 REFUSED_STREAM received from peer
     * 
     */
    @JsonProperty("refused-stream")
    public Boolean getRefusedStream()
    {
        return refusedStream;
    }

    /**
     * If defined, retry in case of response HTTP2 REFUSED_STREAM received from peer
     * 
     */
    @JsonProperty("refused-stream")
    public void setRefusedStream(Boolean refusedStream)
    {
        this.refusedStream = refusedStream;
    }

    public RetryCondition withRefusedStream(Boolean refusedStream)
    {
        this.refusedStream = refusedStream;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(RetryCondition.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("httpStatus");
        sb.append('=');
        sb.append(((this.httpStatus == null) ? "<null>" : this.httpStatus));
        sb.append(',');
        sb.append("reset");
        sb.append('=');
        sb.append(((this.reset == null) ? "<null>" : this.reset));
        sb.append(',');
        sb.append("connectFailure");
        sb.append('=');
        sb.append(((this.connectFailure == null) ? "<null>" : this.connectFailure));
        sb.append(',');
        sb.append("refusedStream");
        sb.append('=');
        sb.append(((this.refusedStream == null) ? "<null>" : this.refusedStream));
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
        result = ((result * 31) + ((this.reset == null) ? 0 : this.reset.hashCode()));
        result = ((result * 31) + ((this.connectFailure == null) ? 0 : this.connectFailure.hashCode()));
        result = ((result * 31) + ((this.refusedStream == null) ? 0 : this.refusedStream.hashCode()));
        result = ((result * 31) + ((this.httpStatus == null) ? 0 : this.httpStatus.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof RetryCondition) == false)
        {
            return false;
        }
        RetryCondition rhs = ((RetryCondition) other);
        return (((((this.reset == rhs.reset) || ((this.reset != null) && this.reset.equals(rhs.reset)))
                  && ((this.connectFailure == rhs.connectFailure) || ((this.connectFailure != null) && this.connectFailure.equals(rhs.connectFailure))))
                 && ((this.refusedStream == rhs.refusedStream) || ((this.refusedStream != null) && this.refusedStream.equals(rhs.refusedStream))))
                && ((this.httpStatus == rhs.httpStatus) || ((this.httpStatus != null) && this.httpStatus.equals(rhs.httpStatus))));
    }

}
