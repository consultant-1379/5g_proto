/**
 * COPYRIGHT ERICSSON GMBH 2024
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Mar 15, 2024
 *     Author: ztsakon
 */

package com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.google.protobuf.Any;
import com.google.protobuf.BoolValue;
import com.google.protobuf.Duration;
import com.google.protobuf.UInt32Value;

import io.envoyproxy.envoy.config.core.v3.HealthCheck;
import io.envoyproxy.envoy.config.core.v3.HealthCheck.HttpHealthCheck;
import io.envoyproxy.envoy.config.core.v3.RequestMethod;
import io.envoyproxy.envoy.config.core.v3.TypedExtensionConfig;
import io.envoyproxy.envoy.extensions.health_check.event_sinks.file.v3.HealthCheckEventFileSink;
import io.envoyproxy.envoy.type.v3.CodecClientType;
import io.envoyproxy.envoy.type.v3.Int64Range;

/**
 * Simple container class. Holds data for active health check. Some values are
 * hard-coded here, some come from the configuration uploaded to the CM
 * Mediator.
 */
public class ProxyActiveHealthCheck
{

    /* The interval between health checks. */
    private Integer timeInterval;

    /*
     * The “no traffic interval” is a special health check interval that is used
     * when a cluster has never had traffic routed to it. This lower interval allows
     * cluster information to be kept up to date, without sending a potentially
     * large amount of active health checking traffic for no reason. Once a cluster
     * has been used for traffic routing, Envoy will shift back to using the
     * standard health check interval that is defined. Note that this interval takes
     * precedence over any other. Envoy default=60
     */
    private Integer noTrafficTimeInterval;

    /*
     * The time to wait for a health check response. If the timeout is reached the
     * health check attempt will be considered a failure.
     */
    private Integer timeout;

    /*
     * The number of unhealthy health checks required before a host is marked
     * unhealthy. Note that for http health checking if a host responds with a code
     * not in expected_statuses or retriable_statuses, this threshold is ignored and
     * the host is considered immediately unhealthy.
     */
    private Integer unhealthyThreshold;
    /*
     * The number of healthy health checks required before a host is marked healthy.
     * Note that during startup, only a single successful health check is required
     * to mark a host healthy.
     */
    private Integer healthyMsgsThreshold = 1;

    /*
     * An optional jitter amount in ms. If specified, the health checking will start
     * after a random time in ms between 0 and initialJitter value. This only
     * applies to the first health check.
     */
    private Integer initialJitter = 500;

    /*
     * An optional jitter amount in ms. If specified, an intervalJitter value will
     * be added in every interval of each health checking .
     */
    private Integer intervalJitter = 500;

    /*
     * HTTP Method that will be used for health checking, default is “GET”. GET,
     * HEAD, POST, PUT, DELETE, OPTIONS, TRACE, PATCH methods are supported, but
     * making request body is not supported. CONNECT method is disallowed because it
     * is not appropriate for health check request.
     */
    private String httpMethod;

    /* Specifies the HTTP path that will be requested during health checking. */
    private String httpPath;

    /*
     * Specifies a list of HTTP response statuses considered healthy. If provided,
     * replaces default 200-only policy - 200 must be included explicitly as needed.
     * Ranges follow half-open semantics of Int64Range. The start and end of each
     * range are required. Only statuses in the range [100, 600) are allowed.
     */
    private Optional<List<Int64Range>> expectedResponseStatuses = Optional.empty();

    /**
     * @param timeInterval
     * @param noTrafficTimeInterval
     * @param timeout
     * @param failedHealthCheckMsgs
     * @param healthyMsgsThreshold
     * @param httpMethod
     * @param httpPath
     * @param expectedStatuses
     * @param retriableStatuses
     */
    public ProxyActiveHealthCheck(Integer timeInterval,
                                  Integer noTrafficTimeInterval,
                                  Integer timeout,
                                  Integer unhealthyThreshold,
                                  String httpMethod,
                                  String httpPath,
                                  Optional<List<Int64Range>> expectedResponseStatuses)
    {
        super();
        this.timeInterval = timeInterval;
        this.noTrafficTimeInterval = noTrafficTimeInterval;
        this.timeout = timeout;
        this.unhealthyThreshold = unhealthyThreshold;
        this.httpMethod = httpMethod;
        this.httpPath = httpPath;
        this.expectedResponseStatuses = expectedResponseStatuses;
    }

    /**
     * @return the timeInterval
     */
    public Integer getTimeInterval()
    {
        return timeInterval;
    }

    /**
     * @param timeInterval the timeInterval to set
     */
    public void setTimeInterval(Integer timeInterval)
    {
        this.timeInterval = timeInterval;
    }

    /**
     * @return the noTrafficTimeInterval
     */
    public Integer getNoTrafficTimeInterval()
    {
        return noTrafficTimeInterval;
    }

    /**
     * @param noTrafficTimeInterval the noTrafficTimeInterval to set
     */
    public void setNoTrafficTimeInterval(Integer noTrafficTimeInterval)
    {
        this.noTrafficTimeInterval = noTrafficTimeInterval;
    }

    /**
     * @return the timeout
     */
    public Integer getTimeout()
    {
        return timeout;
    }

    /**
     * @param timeout the timeout to set
     */
    public void setTimeout(Integer timeout)
    {
        this.timeout = timeout;
    }

    /**
     * @return the failedHealthCheckMsgs
     */
    public Integer getFailedHealthCheckMsgs()
    {
        return unhealthyThreshold;
    }

    /**
     * @param failedHealthCheckMsgs the failedHealthCheckMsgs to set
     */
    public void setFailedHealthCheckMsgs(Integer failedHealthCheckMsgs)
    {
        this.unhealthyThreshold = failedHealthCheckMsgs;
    }

    /**
     * @return the healthyMsgsThreshold
     */
    public Integer getHealthyMsgsThreshold()
    {
        return healthyMsgsThreshold;
    }

    /**
     * @param healthyMsgsThreshold the healthyMsgsThreshold to set
     */
    public void setHealthyMsgsThreshold(Integer healthyMsgsThreshold)
    {
        this.healthyMsgsThreshold = healthyMsgsThreshold;
    }

    /**
     * @return the httpMethod
     */
    public String getHttpMethod()
    {
        return httpMethod;
    }

    /**
     * @param httpMethod the httpMethod to set
     */
    public void setHttpMethod(String httpMethod)
    {
        this.httpMethod = httpMethod;
    }

    /**
     * @return the httpPath
     */
    public String getHttpPath()
    {
        return httpPath;
    }

    /**
     * @param httpPath the httpPath to set
     */
    public void setHttpPath(String httpPath)
    {
        this.httpPath = httpPath;
    }

    /**
     * @return the expectedResponseStatuses
     */
    public Optional<List<Int64Range>> getExpectedResponseStatuses()
    {
        return expectedResponseStatuses;
    }

    /**
     * @param expectedResponseStatuses the expectedStatuses to set
     */
    public void setExpectedResponseStatuses(Optional<List<Int64Range>> expectedResponseStatuses)
    {
        this.expectedResponseStatuses = expectedResponseStatuses;
    }

    public HealthCheck initBuilder()
    {
        RequestMethod rm;
        if (this.httpMethod.equals("get"))
            rm = RequestMethod.GET;
        else if (this.httpMethod.equals("post"))
            rm = RequestMethod.POST;
        else if (this.httpMethod.equals("put"))
            rm = RequestMethod.PUT;
        else if (this.httpMethod.equals("delete"))
            rm = RequestMethod.DELETE;
        else if (this.httpMethod.equals("head"))
            rm = RequestMethod.DELETE;
        else if (this.httpMethod.equals("options"))
            rm = RequestMethod.OPTIONS;
        else if (this.httpMethod.equals("patch"))
            rm = RequestMethod.PATCH;
        else if (this.httpMethod.equals("trace"))
            rm = RequestMethod.TRACE;
        else
            rm = RequestMethod.GET; // set default to GET, CONNECT value is not supported as HTTP method for active
                                    // health
                                    // check

        var httpHealthCheckBuilder = HttpHealthCheck.newBuilder().setMethod(rm).setPath(this.httpPath);
        if (!this.expectedResponseStatuses.isEmpty() && this.expectedResponseStatuses.isPresent())
        {
            Iterable<Int64Range> iter = this.expectedResponseStatuses.get();
            httpHealthCheckBuilder.addAllExpectedStatuses(iter);
        }

        httpHealthCheckBuilder.addRetriableStatuses(Int64Range.newBuilder().setStart(100).setEnd(600).build());
        httpHealthCheckBuilder.setCodecClientType(CodecClientType.HTTP2);
        return HealthCheck.newBuilder()
                          .setAlwaysLogHealthCheckFailures(false)
                          .setTimeout(Duration.newBuilder().setSeconds(this.timeout / 1000).setNanos(this.timeout % 1000 * 1000000))
                          .setInterval(Duration.newBuilder().setSeconds(this.timeInterval))
                          .setReuseConnection(BoolValue.newBuilder().setValue(true))
                          .setInitialJitter(Duration.newBuilder().setSeconds(this.initialJitter / 1000).setNanos(this.initialJitter % 1000 * 1000000))
                          .setIntervalJitter(Duration.newBuilder().setSeconds(this.intervalJitter / 1000).setNanos(this.intervalJitter % 1000 * 1000000))
                          .setNoTrafficInterval(Duration.newBuilder().setSeconds(this.noTrafficTimeInterval))
                          .setUnhealthyThreshold(UInt32Value.newBuilder().setValue(this.unhealthyThreshold))
                          .setHealthyThreshold(UInt32Value.newBuilder().setValue(this.healthyMsgsThreshold))
                          .addEventLogger(TypedExtensionConfig.newBuilder()
                                                              .setName("envoy.health_check.event_sinks.file")
                                                              .setTypedConfig(Any.pack(HealthCheckEventFileSink.newBuilder()
                                                                                                               .setEventLogPath("/mnt/pipe.log")
                                                                                                               .build())))
                          .setHttpHealthCheck(httpHealthCheckBuilder)
                          .build();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(expectedResponseStatuses,
                            unhealthyThreshold,
                            healthyMsgsThreshold,
                            httpMethod,
                            httpPath,
                            noTrafficTimeInterval,
                            timeInterval,
                            timeout);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProxyActiveHealthCheck other = (ProxyActiveHealthCheck) obj;
        return Objects.equals(expectedResponseStatuses, other.expectedResponseStatuses) && Objects.equals(unhealthyThreshold, other.unhealthyThreshold)
               && Objects.equals(healthyMsgsThreshold, other.healthyMsgsThreshold) && Objects.equals(httpMethod, other.httpMethod)
               && Objects.equals(httpPath, other.httpPath) && Objects.equals(noTrafficTimeInterval, other.noTrafficTimeInterval)
               && Objects.equals(timeInterval, other.timeInterval) && Objects.equals(timeout, other.timeout);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ProxyActiveHealthCheck [timeInterval=" + timeInterval + ", noTrafficTimeInterval=" + noTrafficTimeInterval + ", timeout=" + timeout
               + ", failedHealthCheckMsgs=" + unhealthyThreshold + ", healthyMsgsThreshold=" + healthyMsgsThreshold + ", httpMethod=" + httpMethod
               + ", httpPath=" + httpPath + ", expectedStatuses=" + expectedResponseStatuses + "]";
    }

}
