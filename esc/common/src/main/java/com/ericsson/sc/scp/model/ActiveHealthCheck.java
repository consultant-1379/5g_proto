
package com.ericsson.sc.scp.model;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.sc.glue.IfActiveHealthCheck;
import com.ericsson.sc.nfm.model.ExpectedResponseHttpStatusCode;
import com.ericsson.sc.nfm.model.HttpMethod;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Active health checking per nf-pool monitors continuously the health of all
 * static and discovered nf-instances associated with an nf-pool.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "time-interval",
                     "no-traffic-time-interval",
                     "timeout",
                     "unhealthy-threshold",
                     "http-method",
                     "http-path",
                     "expected-response-http-status-code" })
public class ActiveHealthCheck implements IfActiveHealthCheck
{

    /**
     * The time interval applied between two active health checks in one worker pod.
     * 
     */
    @JsonProperty("time-interval")
    @JsonPropertyDescription("The time interval applied between two active health checks in one worker pod.")
    private Integer timeInterval = 10;
    /**
     * The time interval applied between two active health checks in one worker pod
     * when no traffic has been routed to this nf-pool.
     * 
     */
    @JsonProperty("no-traffic-time-interval")
    @JsonPropertyDescription("The time interval applied between two active health checks in one worker pod when no traffic has been routed to this nf-pool.")
    private Integer noTrafficTimeInterval = 60;
    /**
     * The maximum time in milliseconds an active health check request is allowed to
     * take before considered to be timed out.
     * 
     */
    @JsonProperty("timeout")
    @JsonPropertyDescription("The maximum time in milliseconds an active health check request is allowed to take before considered to be timed out.")
    private Integer timeout = 2000;
    /**
     * The integer of consecutive failed active health check responses per worker
     * pod required before an NF is marked unhealthy. Note that the
     * unhealthy_threshold is considered only for hosts that have been marked
     * previously as healthy. If the first health check attempt fails for a host it
     * will immediately be marked as unhealthy and an alarm will be raised.
     * 
     */
    @JsonProperty("unhealthy-threshold")
    @JsonPropertyDescription("The integer of consecutive failed active health check responses per worker pod required before an NF is marked unhealthy. Note that the unhealthy_threshold is considered only for hosts that have been marked previously as healthy. If the first health check attempt fails for a host it will immediately be marked as unhealthy and an alarm will be raised.")
    private Integer unhealthyThreshold = 2;
    /**
     * The HTTP method that is used to send an active health check message.
     * 
     */
    @JsonProperty("http-method")
    @JsonPropertyDescription("The HTTP method that is used to send an active health check message.")
    private HttpMethod httpMethod = HttpMethod.fromValue("get");
    /**
     * The HTTP path that is used to send an active health check message. (Required)
     * 
     */
    @JsonProperty("http-path")
    @JsonPropertyDescription("The HTTP path that is used to send an active health check message.")
    private String httpPath;
    /**
     * Specifies a list of HTTP response status ranges considered healthy.
     * 
     */
    @JsonProperty("expected-response-http-status-code")
    @JsonPropertyDescription("Specifies a list of HTTP response status ranges considered healthy.")
    private List<ExpectedResponseHttpStatusCode> expectedResponseHttpStatusCode = new ArrayList<ExpectedResponseHttpStatusCode>();

    /**
     * The time interval applied between two active health checks in one worker pod.
     * 
     */
    @JsonProperty("time-interval")
    public Integer getTimeInterval()
    {
        return timeInterval;
    }

    /**
     * The time interval applied between two active health checks in one worker pod.
     * 
     */
    @JsonProperty("time-interval")
    public void setTimeInterval(Integer timeInterval)
    {
        this.timeInterval = timeInterval;
    }

    public ActiveHealthCheck withTimeInterval(Integer timeInterval)
    {
        this.timeInterval = timeInterval;
        return this;
    }

    /**
     * The time interval applied between two active health checks in one worker pod
     * when no traffic has been routed to this nf-pool.
     * 
     */
    @JsonProperty("no-traffic-time-interval")
    public Integer getNoTrafficTimeInterval()
    {
        return noTrafficTimeInterval;
    }

    /**
     * The time interval applied between two active health checks in one worker pod
     * when no traffic has been routed to this nf-pool.
     * 
     */
    @JsonProperty("no-traffic-time-interval")
    public void setNoTrafficTimeInterval(Integer noTrafficTimeInterval)
    {
        this.noTrafficTimeInterval = noTrafficTimeInterval;
    }

    public ActiveHealthCheck withNoTrafficTimeInterval(Integer noTrafficTimeInterval)
    {
        this.noTrafficTimeInterval = noTrafficTimeInterval;
        return this;
    }

    /**
     * The maximum time in milliseconds an active health check request is allowed to
     * take before considered to be timed out.
     * 
     */
    @JsonProperty("timeout")
    public Integer getTimeout()
    {
        return timeout;
    }

    /**
     * The maximum time in milliseconds an active health check request is allowed to
     * take before considered to be timed out.
     * 
     */
    @JsonProperty("timeout")
    public void setTimeout(Integer timeout)
    {
        this.timeout = timeout;
    }

    public ActiveHealthCheck withTimeout(Integer timeout)
    {
        this.timeout = timeout;
        return this;
    }

    /**
     * The integer of consecutive failed active health check responses per worker
     * pod required before an NF is marked unhealthy. Note that the
     * unhealthy_threshold is considered only for hosts that have been marked
     * previously as healthy. If the first health check attempt fails for a host it
     * will immediately be marked as unhealthy and an alarm will be raised.
     * 
     */
    @JsonProperty("unhealthy-threshold")
    public Integer getUnhealthyThreshold()
    {
        return unhealthyThreshold;
    }

    /**
     * The integer of consecutive failed active health check responses per worker
     * pod required before an NF is marked unhealthy. Note that the
     * unhealthy_threshold is considered only for hosts that have been marked
     * previously as healthy. If the first health check attempt fails for a host it
     * will immediately be marked as unhealthy and an alarm will be raised.
     * 
     */
    @JsonProperty("unhealthy-threshold")
    public void setUnhealthyThreshold(Integer unhealthyThreshold)
    {
        this.unhealthyThreshold = unhealthyThreshold;
    }

    public ActiveHealthCheck withUnhealthyThreshold(Integer unhealthyThreshold)
    {
        this.unhealthyThreshold = unhealthyThreshold;
        return this;
    }

    /**
     * The HTTP method that is used to send an active health check message.
     * 
     */
    @JsonProperty("http-method")
    public HttpMethod getHttpMethod()
    {
        return httpMethod;
    }

    /**
     * The HTTP method that is used to send an active health check message.
     * 
     */
    @JsonProperty("http-method")
    public void setHttpMethod(HttpMethod httpMethod)
    {
        this.httpMethod = httpMethod;
    }

    public ActiveHealthCheck withHttpMethod(HttpMethod httpMethod)
    {
        this.httpMethod = httpMethod;
        return this;
    }

    /**
     * The HTTP path that is used to send an active health check message. (Required)
     * 
     */
    @JsonProperty("http-path")
    public String getHttpPath()
    {
        return httpPath;
    }

    /**
     * The HTTP path that is used to send an active health check message. (Required)
     * 
     */
    @JsonProperty("http-path")
    public void setHttpPath(String httpPath)
    {
        this.httpPath = httpPath;
    }

    public ActiveHealthCheck withHttpPath(String httpPath)
    {
        this.httpPath = httpPath;
        return this;
    }

    /**
     * Specifies a list of HTTP response status ranges considered healthy.
     * 
     */
    @JsonProperty("expected-response-http-status-code")
    public List<ExpectedResponseHttpStatusCode> getExpectedResponseHttpStatusCode()
    {
        return expectedResponseHttpStatusCode;
    }

    /**
     * Specifies a list of HTTP response status ranges considered healthy.
     * 
     */
    @JsonProperty("expected-response-http-status-code")
    public void setExpectedResponseHttpStatusCode(List<ExpectedResponseHttpStatusCode> expectedResponseHttpStatusCode)
    {
        this.expectedResponseHttpStatusCode = expectedResponseHttpStatusCode;
    }

    public ActiveHealthCheck withExpectedResponseHttpStatusCode(List<ExpectedResponseHttpStatusCode> expectedResponseHttpStatusCode)
    {
        this.expectedResponseHttpStatusCode = expectedResponseHttpStatusCode;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ActiveHealthCheck.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("timeInterval");
        sb.append('=');
        sb.append(((this.timeInterval == null) ? "<null>" : this.timeInterval));
        sb.append(',');
        sb.append("noTrafficTimeInterval");
        sb.append('=');
        sb.append(((this.noTrafficTimeInterval == null) ? "<null>" : this.noTrafficTimeInterval));
        sb.append(',');
        sb.append("timeout");
        sb.append('=');
        sb.append(((this.timeout == null) ? "<null>" : this.timeout));
        sb.append(',');
        sb.append("unhealthyThreshold");
        sb.append('=');
        sb.append(((this.unhealthyThreshold == null) ? "<null>" : this.unhealthyThreshold));
        sb.append(',');
        sb.append("httpMethod");
        sb.append('=');
        sb.append(((this.httpMethod == null) ? "<null>" : this.httpMethod));
        sb.append(',');
        sb.append("httpPath");
        sb.append('=');
        sb.append(((this.httpPath == null) ? "<null>" : this.httpPath));
        sb.append(',');
        sb.append("expectedResponseHttpStatusCode");
        sb.append('=');
        sb.append(((this.expectedResponseHttpStatusCode == null) ? "<null>" : this.expectedResponseHttpStatusCode));
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
        result = ((result * 31) + ((this.httpPath == null) ? 0 : this.httpPath.hashCode()));
        result = ((result * 31) + ((this.noTrafficTimeInterval == null) ? 0 : this.noTrafficTimeInterval.hashCode()));
        result = ((result * 31) + ((this.timeInterval == null) ? 0 : this.timeInterval.hashCode()));
        result = ((result * 31) + ((this.unhealthyThreshold == null) ? 0 : this.unhealthyThreshold.hashCode()));
        result = ((result * 31) + ((this.httpMethod == null) ? 0 : this.httpMethod.hashCode()));
        result = ((result * 31) + ((this.timeout == null) ? 0 : this.timeout.hashCode()));
        result = ((result * 31) + ((this.expectedResponseHttpStatusCode == null) ? 0 : this.expectedResponseHttpStatusCode.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof ActiveHealthCheck) == false)
        {
            return false;
        }
        ActiveHealthCheck rhs = ((ActiveHealthCheck) other);
        return ((((((((this.httpPath == rhs.httpPath) || ((this.httpPath != null) && this.httpPath.equals(rhs.httpPath)))
                     && ((this.noTrafficTimeInterval == rhs.noTrafficTimeInterval)
                         || ((this.noTrafficTimeInterval != null) && this.noTrafficTimeInterval.equals(rhs.noTrafficTimeInterval))))
                    && ((this.timeInterval == rhs.timeInterval) || ((this.timeInterval != null) && this.timeInterval.equals(rhs.timeInterval))))
                   && ((this.unhealthyThreshold == rhs.unhealthyThreshold)
                       || ((this.unhealthyThreshold != null) && this.unhealthyThreshold.equals(rhs.unhealthyThreshold))))
                  && ((this.httpMethod == rhs.httpMethod) || ((this.httpMethod != null) && this.httpMethod.equals(rhs.httpMethod))))
                 && ((this.timeout == rhs.timeout) || ((this.timeout != null) && this.timeout.equals(rhs.timeout))))
                && ((this.expectedResponseHttpStatusCode == rhs.expectedResponseHttpStatusCode)
                    || ((this.expectedResponseHttpStatusCode != null) && this.expectedResponseHttpStatusCode.equals(rhs.expectedResponseHttpStatusCode))));
    }

}
