/**
 * COPYRIGHT ERICSSON GMBH 2019
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Aug 15, 2019
 *     Author: eedrak
 */
package com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig;

import java.util.Objects;

import com.google.protobuf.Duration;
import com.google.protobuf.UInt32Value;

import io.envoyproxy.envoy.config.cluster.v3.OutlierDetection;

/**
 * Simple container class. Holds data for host ejection/hibernation. Some values
 * are hard-coded here, some come from the configuration uploaded to the CM
 * Mediator.
 */
public class ProxyEjectionPolicy
{

    /**
     * The number of consecutive 5xx responses or local origin errors that are
     * mapped to 5xx error codes before a consecutive 5xx ejection occurs.
     * 
     * Envoy default=5
     */
    private Integer outlierConsecutive5xx = 5;

    /**
     * The number of consecutive gateway failures (502, 503, 504 status codes)
     * before a consecutive gateway failure ejection occurs.
     * 
     * Envoy default=5
     */
    private Integer outlierConsecutiveGatewayFailure = 5;

    /**
     * The base time that a host is ejected for. The real time is equal to the base
     * time multiplied by the number of times the host has been ejected. The max
     * ejection time will be set with the same value.
     * 
     * Envoy default = 5s
     */
    private Integer baseEjectionTime = 5;

    /**
     * Determines whether to distinguish local origin failures from external errors.
     * If set to true the following configuration parameters are taken into account:
     * consecutive_local_origin_failure, enforcing_consecutive_local_origin_failure
     * and enforcing_local_origin_success_rate.
     * 
     * Envoy default = false.
     */
    private boolean splitExternalLocalOriginErrors = false;
    /**
     * The number of consecutive locally originated failures before ejection occurs.
     * 
     * Envoy default = 5.
     * 
     * Parameter takes effect only when split_external_local_origin_errors is set to
     * true.
     */
    private Integer consecutiveLocalOriginFailure = 5;

    /**
     * 
     * The % chance that a host will be actually ejected when an outlier status is
     * detected through consecutive locally originated failures.
     * 
     * This setting can be used to disable ejection or to ramp it up slowly.
     * 
     * Envoy default = 100. Parameter takes effect only when
     * split_external_local_origin_errors is set to true.
     * 
     */
    private Integer enforcingConsecutiveLocalOriginFailure = 100;

    /*
     * Envoy "Outlier Detection" parameters currently not exposed
     * 
     */

    /**
     * The maximum % of an upstream cluster that can be ejected due to outlier
     * detection. Envoy defaults to 10% but will eject at least one host regardless
     * of the value. * Envoy default = 10% SCP default = 100%
     */
    private final Integer MAX_EJECTION_PERCENT = 100;

    /**
     * The time interval between ejection analysis sweeps. This can result in both
     * new ejections as well as hosts being returned to service.
     * 
     * Envoy default = 10s.
     */
    private final Integer INTERVAL = 1;

    /**
     * The % chance that a host will be actually ejected when an outlier status is
     * detected through consecutive 5xx. This setting can be used to disable
     * ejection or to ramp it up slowly.
     * 
     * Envoy default = 100.
     */
    private final Integer ENFORCING_CONSECUTIVE_5XX = 100;

    /**
     * The % chance that a host will be actually ejected when an outlier status is
     * detected through success rate statistics. This setting can be used to disable
     * ejection or to ramp it up slowly.
     * 
     * Envoy default = 100.
     */
    private final Integer ENFORCING_SUCCESS_RATE = 0;

    /**
     * The number of hosts in a cluster that must have enough request volume to
     * detect success rate outliers. If the number of hosts is less than this
     * setting, outlier detection via success rate statistics is not performed for
     * any host in the cluster.
     * 
     * Envoy default = 5. SCP default = 0.
     */
    private final Integer SUCCESS_RATE_MINIMUM_HOSTS = 5;

    /**
     * The minimum number of total requests that must be collected in one interval
     * (as defined by the interval duration above) to include this host in success
     * rate based outlier detection. If the volume is lower than this setting,
     * outlier detection via success rate statistics is not performed for that host.
     * 
     * Envoy default = 100. SCP default = 100000
     */
    private final Integer SUCCESS_RATE_REQUEST_VOLUME = 100000;

    /**
     * This factor is used to determine the ejection threshold for success rate
     * outlier ejection. The ejection threshold is the difference between the mean
     * success rate, and the product of this factor and the standard deviation of
     * the mean success rate: mean - (stdev * success_rate_stdev_factor). This
     * factor is divided by a thousand to get a double. That is, if the desired
     * factor is 1.9, the runtime value should be 1900.
     * 
     * Envoy default = 1900.
     */
    private final Integer SUCCESS_RATE_STDEV_FACTOR = 1900;

    /**
     * The % chance that a host will be actually ejected when an outlier status is
     * detected through consecutive gateway failures. This setting can be used to
     * disable ejection or to ramp it up slowly.
     * 
     * Envoy default = 0.
     */
    private Integer enforcingConsecutiveGatewayFailure = 0;

    /**
     * 
     * The % chance that a host will be actually ejected when an outlier status is
     * detected through success rate statistics for locally originated errors. This
     * setting can be used to disable ejection or to ramp it up slowly.
     * 
     * Envoy default = 100.
     *
     * Parameter takes effect only when split_external_local_origin_errors is set to
     * true.
     * 
     */
    private final Integer ENFORCING_LOCAL_ORIGIN_SUCCESS_RATE = 100;

    public ProxyEjectionPolicy(Integer outlierConsecutive5xx,
                               Integer outlierConsecutiveGatewayFailure,
                               Integer baseEjectionTime)
    {
        super();
        this.outlierConsecutive5xx = outlierConsecutive5xx;
        this.outlierConsecutiveGatewayFailure = outlierConsecutiveGatewayFailure;
        this.setBaseEjectionTime(baseEjectionTime);
    }

    public Integer getOutlierConsecutive5xx()
    {
        return outlierConsecutive5xx;
    }

    public void setOutlierConsecutive5xx(Integer outlierConsecutive5xx)
    {
        this.outlierConsecutive5xx = outlierConsecutive5xx;
    }

    public Integer getOutlierConsecutiveGatewayFailure()
    {
        return outlierConsecutiveGatewayFailure;
    }

    public void setOutlierConsecutiveGatewayFailure(Integer outlierConsecutiveGatewayFailure)
    {
        this.outlierConsecutiveGatewayFailure = outlierConsecutiveGatewayFailure;
    }

    public Integer getBaseEjectionTime()
    {
        return baseEjectionTime;
    }

    public void setEnforceConsecutiveGatewayFailure(Integer enforcingConsecutiveGatewayFailure)
    {
        this.enforcingConsecutiveGatewayFailure = enforcingConsecutiveGatewayFailure;
    }

    public void setBaseEjectionTime(Integer baseEjectionTime)
    {
        this.baseEjectionTime = baseEjectionTime;
    }

    /**
     * @return the splitExternalLocalOriginErrors
     */
    public boolean isSplitExternalLocalOriginErrors()
    {
        return splitExternalLocalOriginErrors;
    }

    /**
     * @param splitExternalLocalOriginErrors the splitExternalLocalOriginErrors to
     *                                       set
     */
    public void setSplitExternalLocalOriginErrors(boolean splitExternalLocalOriginErrors)
    {
        this.splitExternalLocalOriginErrors = splitExternalLocalOriginErrors;
    }

    /**
     * @return the consecutiveLocalOriginFailure
     */
    public Integer getConsecutiveLocalOriginFailure()
    {
        return consecutiveLocalOriginFailure;
    }

    /**
     * @param consecutiveLocalOriginFailure the consecutiveLocalOriginFailure to set
     */
    public void setConsecutiveLocalOriginFailure(Integer consecutiveLocalOriginFailure)
    {
        this.consecutiveLocalOriginFailure = consecutiveLocalOriginFailure;
    }

    /**
     * @return the enforcingConsecutiveLocalOriginFailure
     */
    public Integer getEnforcingConsecutiveLocalOriginFailure()
    {
        return enforcingConsecutiveLocalOriginFailure;
    }

    /**
     * @param enforcingConsecutiveLocalOriginFailure the
     *                                               enforcingConsecutiveLocalOriginFailure
     *                                               to set
     */
    public void setEnforcingConsecutiveLocalOriginFailure(Integer enforcingConsecutiveLocalOriginFailure)
    {
        this.enforcingConsecutiveLocalOriginFailure = enforcingConsecutiveLocalOriginFailure;
    }

    /**
     * 
     * @return the ejectionPolicy as OutlierDetection object to be inserted to the
     *         envoy cluster config.
     */
    public OutlierDetection toOutlierDetection()
    {

        return OutlierDetection.newBuilder()
                               .setMaxEjectionTime(Duration.newBuilder().setSeconds(this.baseEjectionTime).build())
                               // public exposed parameters
                               .setConsecutive5Xx(UInt32Value.of(this.getOutlierConsecutive5xx())) //
                               .setConsecutiveGatewayFailure(UInt32Value.of(this.getOutlierConsecutiveGatewayFailure())) //
                               .setBaseEjectionTime(Duration.newBuilder().setSeconds(this.baseEjectionTime).build())
                               // overwritten envoy defaults
                               .setMaxEjectionPercent(UInt32Value.of(MAX_EJECTION_PERCENT))
                               .setInterval(Duration.newBuilder().setSeconds(INTERVAL).build())
                               .setEnforcingConsecutive5Xx(UInt32Value.of(ENFORCING_CONSECUTIVE_5XX))
                               .setEnforcingSuccessRate(UInt32Value.of(ENFORCING_SUCCESS_RATE))
                               .setSuccessRateMinimumHosts(UInt32Value.of(SUCCESS_RATE_MINIMUM_HOSTS))
                               .setSuccessRateRequestVolume(UInt32Value.of(SUCCESS_RATE_REQUEST_VOLUME))
                               .setSuccessRateStdevFactor(UInt32Value.of(SUCCESS_RATE_STDEV_FACTOR))
                               .setEnforcingConsecutiveGatewayFailure(UInt32Value.of(enforcingConsecutiveGatewayFailure))
                               .setSplitExternalLocalOriginErrors(splitExternalLocalOriginErrors)
                               .setEnforcingConsecutiveLocalOriginFailure(UInt32Value.of(enforcingConsecutiveLocalOriginFailure))
                               .setConsecutiveLocalOriginFailure(UInt32Value.of(consecutiveLocalOriginFailure))
                               .setEnforcingLocalOriginSuccessRate(UInt32Value.of(ENFORCING_LOCAL_ORIGIN_SUCCESS_RATE))
                               .build();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ProxyEjectionPolicy [outlierConsecutive5xx=" + outlierConsecutive5xx + ", outlierConsecutiveGatewayFailure=" + outlierConsecutiveGatewayFailure
               + ", baseEjectionTime=" + baseEjectionTime + ", splitExternalLocalOriginErrors=" + splitExternalLocalOriginErrors
               + ", consecutiveLocalOriginFailure=" + consecutiveLocalOriginFailure + ", enforcingConsecutiveLocalOriginFailure="
               + enforcingConsecutiveLocalOriginFailure + ", MAX_EJECTION_PERCENT=" + MAX_EJECTION_PERCENT + ", INTERVAL=" + INTERVAL
               + ", ENFORCING_CONSECUTIVE_5XX=" + ENFORCING_CONSECUTIVE_5XX + ", ENFORCING_SUCCESS_RATE=" + ENFORCING_SUCCESS_RATE
               + ", SUCCESS_RATE_MINIMUM_HOSTS=" + SUCCESS_RATE_MINIMUM_HOSTS + ", SUCCESS_RATE_REQUEST_VOLUME=" + SUCCESS_RATE_REQUEST_VOLUME
               + ", SUCCESS_RATE_STDEV_FACTOR=" + SUCCESS_RATE_STDEV_FACTOR + ", enforcingConsecutiveGatewayFailure=" + enforcingConsecutiveGatewayFailure
               + ", ENFORCING_LOCAL_ORIGIN_SUCCESS_RATE=" + ENFORCING_LOCAL_ORIGIN_SUCCESS_RATE + "]";
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(ENFORCING_CONSECUTIVE_5XX,
                            enforcingConsecutiveGatewayFailure,
                            ENFORCING_LOCAL_ORIGIN_SUCCESS_RATE,
                            ENFORCING_SUCCESS_RATE,
                            INTERVAL,
                            MAX_EJECTION_PERCENT,
                            SUCCESS_RATE_MINIMUM_HOSTS,
                            SUCCESS_RATE_REQUEST_VOLUME,
                            SUCCESS_RATE_STDEV_FACTOR,
                            baseEjectionTime,
                            consecutiveLocalOriginFailure,
                            enforcingConsecutiveLocalOriginFailure,
                            outlierConsecutive5xx,
                            outlierConsecutiveGatewayFailure,
                            splitExternalLocalOriginErrors);
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
        ProxyEjectionPolicy other = (ProxyEjectionPolicy) obj;
        return Objects.equals(ENFORCING_CONSECUTIVE_5XX, other.ENFORCING_CONSECUTIVE_5XX)
               && Objects.equals(enforcingConsecutiveGatewayFailure, other.enforcingConsecutiveGatewayFailure)
               && Objects.equals(ENFORCING_LOCAL_ORIGIN_SUCCESS_RATE, other.ENFORCING_LOCAL_ORIGIN_SUCCESS_RATE)
               && Objects.equals(ENFORCING_SUCCESS_RATE, other.ENFORCING_SUCCESS_RATE) && Objects.equals(INTERVAL, other.INTERVAL)
               && Objects.equals(MAX_EJECTION_PERCENT, other.MAX_EJECTION_PERCENT)
               && Objects.equals(SUCCESS_RATE_MINIMUM_HOSTS, other.SUCCESS_RATE_MINIMUM_HOSTS)
               && Objects.equals(SUCCESS_RATE_REQUEST_VOLUME, other.SUCCESS_RATE_REQUEST_VOLUME)
               && Objects.equals(SUCCESS_RATE_STDEV_FACTOR, other.SUCCESS_RATE_STDEV_FACTOR) && Objects.equals(baseEjectionTime, other.baseEjectionTime)
               && Objects.equals(consecutiveLocalOriginFailure, other.consecutiveLocalOriginFailure)
               && Objects.equals(enforcingConsecutiveLocalOriginFailure, other.enforcingConsecutiveLocalOriginFailure)
               && Objects.equals(outlierConsecutive5xx, other.outlierConsecutive5xx)
               && Objects.equals(outlierConsecutiveGatewayFailure, other.outlierConsecutiveGatewayFailure)
               && splitExternalLocalOriginErrors == other.splitExternalLocalOriginErrors;
    }

}
