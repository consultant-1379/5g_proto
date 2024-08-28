/**
 * COPYRIGHT ERICSSON GMBH 2021
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jul 12, 2021
 *     Author: emldpng
 */

package com.ericsson.esc.bsf.load.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ericsson.esc.bsf.load.configuration.TrafficSetConfiguration.LoadType;
import com.ericsson.esc.bsf.load.server.InvalidParameter;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.json.JsonMapper;

/**
 * Defines a mix of TrafficSets. The configuration is divided into global
 * configuration that affects all traffic sets and the individual traffic set
 * configuration.
 */
@JsonDeserialize(builder = BsfLoadConfiguration.Builder.class)
public class BsfLoadConfiguration
{
    private static final JsonMapper jm = JsonMapper.builder().configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true).build();

    // Configuration that applies to all traffic sets.
    private Long duration;
    private ExecutionType executionType;
    private Integer http2Streams;
    private Integer maxParallelTransactions;
    private Integer targetPort;
    private TlsConfiguration tls;
    private Integer maxTcpConnectionsPerClient;
    private Integer tcpClients;
    private Long timeout;
    private MetricsConfiguration metrics;
    private Integer http2KeepAliveTimeout;

    // Individual traffic set configuration.
    private List<TrafficSetConfiguration> setupTrafficMix = List.of();
    private List<TrafficSetConfiguration> trafficMix = List.of();

    public enum ExecutionType
    {
        SERIAL,
        PARALLEL
    }

    private BsfLoadConfiguration(Builder builder)
    {
        this.duration = builder.duration;
        this.executionType = builder.executionType;
        this.http2Streams = builder.http2Streams;
        this.maxParallelTransactions = builder.maxParallelTransactions;
        this.targetPort = builder.targetPort;
        this.tls = builder.tls;
        this.maxTcpConnectionsPerClient = builder.maxTcpConnectionsPerClient;
        this.tcpClients = builder.tcpClients;
        this.timeout = builder.timeout;
        this.metrics = builder.metrics;
        this.setupTrafficMix = builder.setupTrafficMix;
        this.trafficMix = builder.trafficMix;
        this.http2KeepAliveTimeout = builder.http2KeepAliveTimeout;
    }

    /**
     * 
     * @param json
     * @return BsfLoadConfiguration
     * @throws IllegalArgumentException
     */
    public static BsfLoadConfiguration fromRequest(Map<String, Object> objectMap) throws IllegalArgumentException
    {
        return jm.convertValue(objectMap, BsfLoadConfiguration.class);
    }

    /**
     * Get the duration of the traffic produced by the tool.
     * 
     * @return the duration
     */
    public Long getDuration()
    {
        return duration;
    }

    /**
     * Get the execution type, which can be serial or parallel.
     * 
     * @return the executionType
     */
    @JsonGetter("execution-type")
    public ExecutionType getExecutionType()
    {
        return executionType;
    }

    /**
     * Get the number of http2 streams used to generate traffic
     * 
     * @return the http2Streams
     */
    @JsonGetter("http2-streams")
    public Integer getHttp2Streams()
    {
        return http2Streams;
    }

    /**
     * Get the maximum number of parallel transactions.
     * 
     * @return the maxParallelTransactions
     */
    @JsonGetter("max-parallel-transactions")
    public Integer getMaxParallelTransactions()
    {
        return maxParallelTransactions;
    }

    /**
     * Get the port of the target BSF.
     * 
     * @return the targetPort
     */
    @JsonGetter("target-port")
    public Integer getTargetPort()
    {
        return targetPort;
    }

    /**
     * Get the TLS configuration options.
     * 
     * @return the tls
     */
    @JsonGetter("tls")
    public TlsConfiguration getTls()
    {
        return tls;
    }

    /**
     * Get the maximum number of TCP connections per client used to generate
     * traffic.
     * 
     * @return the maxTcpConnectionsPerClient
     */
    @JsonGetter("max-tcp-connections-per-client")
    public Integer getMaxTcpConnectionsPerClient()
    {
        return maxTcpConnectionsPerClient;
    }

    /**
     * Get the number of TCP clients used to generate traffic.
     * 
     * @return the tcpClients
     */
    @JsonGetter("tcp-clients")
    public Integer getTcpClients()
    {
        return tcpClients;
    }

    /**
     * Get the timeout period (in milliseconds) before a request fails.
     * 
     * @return the timeout
     */
    public Long getTimeout()
    {
        return timeout;
    }

    /**
     * Get the list of traffic sets that are executed only once before the actual
     * traffic mix.
     * 
     * @return the setupTrafficMix
     */
    @JsonGetter("setup-traffic-mix")
    public List<TrafficSetConfiguration> getSetupTrafficMix()
    {
        return setupTrafficMix;
    }

    /**
     * Get the list of traffic sets that are executed repeatedly.
     * 
     * @return the trafficMix
     */
    @JsonProperty("traffic-mix")
    public List<TrafficSetConfiguration> getTrafficMix()
    {
        return trafficMix;
    }

    /**
     * Get the metrics configuration options.
     * 
     * @return the metrics.
     */
    public MetricsConfiguration getMetrics()
    {
        return metrics;
    }

    /**
     * Get the http2 Keep Alive Timeout.
     * 
     * @return Http2KeepAliveTimeout.
     */
    @JsonGetter("http2-keep-alive-timeout")
    public Integer getHttp2KeepAliveTimeout()
    {
        return http2KeepAliveTimeout;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((duration == null) ? 0 : duration.hashCode());
        result = prime * result + ((executionType == null) ? 0 : executionType.hashCode());
        result = prime * result + ((http2Streams == null) ? 0 : http2Streams.hashCode());
        result = prime * result + ((maxParallelTransactions == null) ? 0 : maxParallelTransactions.hashCode());
        result = prime * result + ((setupTrafficMix == null) ? 0 : setupTrafficMix.hashCode());
        result = prime * result + ((targetPort == null) ? 0 : targetPort.hashCode());
        result = prime * result + ((maxTcpConnectionsPerClient == null) ? 0 : maxTcpConnectionsPerClient.hashCode());
        result = prime * result + ((tcpClients == null) ? 0 : tcpClients.hashCode());
        result = prime * result + ((timeout == null) ? 0 : timeout.hashCode());
        result = prime * result + ((trafficMix == null) ? 0 : trafficMix.hashCode());
        result = prime * result + ((tls == null) ? 0 : tls.hashCode());
        result = prime * result + ((http2KeepAliveTimeout == null) ? 0 : http2KeepAliveTimeout.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BsfLoadConfiguration other = (BsfLoadConfiguration) obj;
        if (duration == null)
        {
            if (other.duration != null)
                return false;
        }
        else if (!duration.equals(other.duration))
            return false;
        if (executionType != other.executionType)
            return false;
        if (http2Streams == null)
        {
            if (other.http2Streams != null)
                return false;
        }
        else if (!http2Streams.equals(other.http2Streams))
            return false;
        if (maxParallelTransactions == null)
        {
            if (other.maxParallelTransactions != null)
                return false;
        }
        else if (!maxParallelTransactions.equals(other.maxParallelTransactions))
            return false;
        if (setupTrafficMix == null)
        {
            if (other.setupTrafficMix != null)
                return false;
        }
        else if (!setupTrafficMix.equals(other.setupTrafficMix))
            return false;
        if (targetPort == null)
        {
            if (other.targetPort != null)
                return false;
        }
        else if (!targetPort.equals(other.targetPort))
            return false;
        if (tls == null)
        {
            if (other.tls != null)
                return false;
        }
        else if (!tls.equals(other.tls))
            return false;
        if (maxTcpConnectionsPerClient == null)
        {
            if (other.maxTcpConnectionsPerClient != null)
                return false;
        }
        else if (!maxTcpConnectionsPerClient.equals(other.maxTcpConnectionsPerClient))
            return false;
        if (tcpClients == null)
        {
            if (other.tcpClients != null)
                return false;
        }
        else if (!tcpClients.equals(other.tcpClients))
            return false;
        if (timeout == null)
        {
            if (other.timeout != null)
                return false;
        }
        else if (!timeout.equals(other.timeout))
            return false;
        if (trafficMix == null)
        {
            if (other.trafficMix != null)
                return false;
        }
        else if (!trafficMix.equals(other.trafficMix))
            return false;
        if (http2KeepAliveTimeout == null)
        {
            if (other.http2KeepAliveTimeout != null)
                return false;
        }
        else if (!http2KeepAliveTimeout.equals(other.http2KeepAliveTimeout))
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "BsfLoadConfiguration [duration=" + duration + ", executionType=" + executionType + ", http2Streams=" + http2Streams
               + ", maxParallelTransactions=" + maxParallelTransactions + ", targetPort=" + targetPort + ", maxTcpConnectionsPerClient="
               + maxTcpConnectionsPerClient + ", tcpClients=" + tcpClients + ", timeout=" + timeout + ", setupTrafficMix=" + setupTrafficMix + ", trafficMix="
               + trafficMix + ", http2KeepAliveTimeout=" + http2KeepAliveTimeout + "]";
    }

    public String prettyJsonString()
    {
        try
        {
            return jm.writerWithDefaultPrettyPrinter().writeValueAsString(this);
        }
        catch (JsonProcessingException e)
        {
            return this.toString();
        }
    }

    /**
     * Validates and logs all errors in the configuration. Does not stop at the
     * first invalid configuration option.
     * 
     * @return Returns false when there is at least one invalid configuration
     *         option, otherwise true.
     */
    public List<InvalidParameter> validate()
    {
        final var cv = new ConfigurationValidator();

        cv.checkNullOrNonNullPositive(duration, "duration", "Empty or a positive number value is required for 'duration'");
        cv.checkNonNull(executionType, "execution-type", "Parameter 'execution-type' must not be null");
        cv.checkNonNullPositive(http2Streams, "http2-streams", "A positive number value is required for 'http2-streams'");
        cv.checkNonNullPositive(maxParallelTransactions, "max-parallel-transactions", "A positive number value is required for 'max-parallel-transactions'");
        cv.checkNonNullPositive(targetPort, "target-port", "Parameter 'target-port' is mandatory. A positive number value is required");
        cv.checkNonNullPositive(maxTcpConnectionsPerClient,
                                "max-tcp-connections-per-client",
                                "A positive number value is required for 'max-tcp-connections-per-client'");
        cv.checkNonNullPositive(tcpClients, "tcp-clients", "A positive number value is required for 'tcp-clients'");
        cv.checkNonNullZeroOrPositive(timeout, "timeout", "A zero value or a positive number value is required for 'timeout'");
        cv.checkNullOrNonNullPositive(Long.valueOf(http2KeepAliveTimeout),
                                      "http2-keep-alive-timeout",
                                      "Empty or a positive number value is required for 'http2-keep-alive-timeout'");

        // Validate tls and metrics configurations.
        cv.addInvalidParameters(tls.validate());
        cv.addInvalidParameters(metrics.validate());

        // Validate traffic sets.
        setupTrafficMix.stream() //
                       .map(TrafficSetConfiguration::validate)
                       .forEach(cv::addInvalidParameters);
        trafficMix.stream() //
                  .map(TrafficSetConfiguration::validate)
                  .forEach(cv::addInvalidParameters);

        // Traffic set relational checks if there are no other problems.
        if (cv.getInvalidParam().isEmpty())
        {
            var setRefMessage = "Invalid traffic set reference for set. Either the referenced set was not found or the matching set had wrong 'type' or 'order'";
            cv.check(checkNameUniqueness(), "name", "The parameter 'name' must be unique among all traffic sets");
            cv.check(checkOrderingUniqueness(), "order", "The parameter 'order' must be unique among all traffic sets");
            cv.check(checkTrafficSetReference(setupTrafficMix), "setup-traffic-mix", setRefMessage);
            cv.check(checkTrafficSetReference(trafficMix), "traffic-mix", setRefMessage);
        }

        return cv.getInvalidParam();
    }

    /**
     * Checks that the traffic set names are unique.
     * 
     * @return True if all names are unique, otherwise false.
     */
    private boolean checkNameUniqueness()
    {
        var valid = true;
        List<TrafficSetConfiguration> allSets = new ArrayList<>();
        allSets.addAll(setupTrafficMix);
        allSets.addAll(trafficMix);

        // Name uniqueness check.
        var uniqueSetNames = allSets.stream().map(TrafficSetConfiguration::getName).distinct().count();
        if (uniqueSetNames != allSets.size())
        {
            valid = false;
        }
        return valid;
    }

    /**
     * Checks that the traffic set orders are unique for each traffic set list.
     * 
     * @return True if all orders are unique in their respective list, otherwise
     *         false.
     */
    private boolean checkOrderingUniqueness()
    {
        var valid = true;
        // Ordering uniqueness check.
        var uniqueOrder1 = setupTrafficMix.stream().map(TrafficSetConfiguration::getOrder).distinct().count();
        var uniqueOrder2 = trafficMix.stream().map(TrafficSetConfiguration::getOrder).distinct().count();
        if ((uniqueOrder1 != setupTrafficMix.size()) || (uniqueOrder2 != trafficMix.size()))
        {
            valid = false;
        }
        return valid;
    }

    /**
     * Checks that the deregister traffic sets of the given list reference a valid
     * register traffic set in the same list that has lower order.
     * 
     * @param setList
     * @return
     */
    private boolean checkTrafficSetReference(List<TrafficSetConfiguration> setList)
    {
        var validRef = true;

        // Check all setup traffic sets.
        for (TrafficSetConfiguration deregisterSet : setList)
        {
            // If the trafficSetRef is null, then numRequests is used to deregister with
            // randomly generated UUIDs.
            if (deregisterSet.getType().equals(LoadType.DEREGISTER) && deregisterSet.getTrafficSetRef() != null)
            {
                // If there is a register traffic set with this name that precedes the
                // deregister set, returns true.
                validRef = setList.stream() //
                                  .filter(ts -> ts != deregisterSet)
                                  .anyMatch(ts -> ts.getName().equals(deregisterSet.getTrafficSetRef()) && //
                                                  ts.getType().equals(LoadType.REGISTER) && //
                                                  ts.getOrder() < deregisterSet.getOrder());
            }
        }
        return validRef;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder
    {
        private Long duration = null;
        @JsonProperty("execution-type")
        private ExecutionType executionType = ExecutionType.SERIAL;
        @JsonProperty("http2-streams")
        private Integer http2Streams = 40;
        @JsonProperty("max-parallel-transactions")
        private Integer maxParallelTransactions = 20000;
        @JsonProperty("target-host")
        private String targetHost;
        @JsonProperty("target-port")
        private Integer targetPort;
        @JsonProperty("tls")
        private TlsConfiguration tls = new TlsConfiguration.Builder().build();
        @JsonProperty("max-tcp-connections-per-client")
        private Integer maxTcpConnectionsPerClient = 2;
        @JsonProperty("tcp-clients")
        private Integer tcpClients = 40;
        private Long timeout = 0L;
        @JsonProperty("http2-keep-alive-timeout")
        private Integer http2KeepAliveTimeout = 20;

        private MetricsConfiguration metrics = new MetricsConfiguration.Builder().build();

        // Individual traffic set configuration.
        @JsonProperty("setup-traffic-mix")
        private List<TrafficSetConfiguration> setupTrafficMix = List.of();
        @JsonProperty("traffic-mix")
        private List<TrafficSetConfiguration> trafficMix = List.of();

        /**
         * Set the duration. When defined, the load tool produces traffic for this
         * duration (in seconds) and then stops, regardless if there are additional
         * traffic sets. If the traffic sets are executed faster than the required
         * duration, then they are repeated until the duration goal is met. If the
         * duration is omitted, then all traffic sets are executed only once.
         * 
         * @param duration The duration until bsf-load stops.
         * @return Builder The builder.
         */
        public Builder duration(Long duration)
        {
            this.duration = duration;
            return this;
        }

        /**
         * Set the execution type, which defines if the provided TrafficSets are
         * executed serially or in parallel. Currently only serial execution is
         * supported.
         * 
         * @param executionType The execution type, which can be serial or parallel.
         * @return Builder The Builder.
         */
        public Builder executionType(ExecutionType executionType)
        {
            this.executionType = executionType;
            return this;
        }

        /**
         * Set the number of http2 streams used to generate traffic. If set too low, it
         * might hinder BSF load from yielding the required TPS.
         * 
         * @param http2Streams The number of http2 streams.
         * @return Builder The Builder.
         */
        public Builder http2Streams(int http2Streams)
        {
            this.http2Streams = http2Streams;
            return this;
        }

        /**
         * Set the maximum number of allowed parallel transactions. If set too low, it
         * might hinder BSF load from yielding the required TPS.
         * 
         * @param maxParallelTransactions The maximum number of allowed parallel
         *                                transactions.
         * @return Builder The Builder.
         */
        public Builder maxParallelTransactions(int maxParallelTransactions)
        {
            this.maxParallelTransactions = maxParallelTransactions;
            return this;
        }

        /**
         * Set the port of the target BSF.
         * 
         * @param targetPort The target port.
         * @return Builder The Builder.
         */
        public Builder targetPort(int targetPort)
        {
            this.targetPort = targetPort;
            return this;
        }

        /**
         * Set the TLS configuration options.
         * 
         * @param tls A TlsConfiguration object.
         * @return Builder The Builder.
         */
        public Builder tls(TlsConfiguration tls)
        {
            this.tls = tls;
            return this;
        }

        /**
         * Set the number of maximum TCP connections used per client to generate
         * traffic. If set too low, it might hinder BSF load from yielding the required
         * TPS.
         * 
         * @param tcpConnections The number of TCP connections.
         * @return Builder The Builder.
         */
        public Builder maxTcpConnectionsPerClient(int maxTcpConnectionsPerClient)
        {
            this.maxTcpConnectionsPerClient = maxTcpConnectionsPerClient;
            return this;
        }

        /**
         * Set the number of TCP clients used to generate traffic.
         * 
         * @param tcpClients The number of TCP clients.
         * @return Builder The Builder.
         */
        public Builder tcpClients(int tcpClients)
        {
            this.tcpClients = tcpClients;
            return this;
        }

        /**
         * Set the timeout period (in milliseconds) before a request fails. This is
         * applied to all traffic sets that do not have an individual timeout parameter
         * configured.
         * 
         * @param timeout The timeout period.
         * @return Builder The Builder.
         */
        public Builder timeout(long timeout)
        {
            this.timeout = timeout;
            return this;
        }

        /**
         * Set the timeout period (in milliseconds) before a request fails. This is
         * applied to all traffic sets that do not have an individual timeout parameter
         * configured.
         * 
         * @param timeout The timeout period.
         * @return Builder The Builder.
         */
        public Builder http2KeepAliveTimeout(int http2KeepAliveTimeout)
        {
            this.http2KeepAliveTimeout = http2KeepAliveTimeout;
            return this;
        }

        /**
         * Set the metrics configuration options.
         * 
         * @param metrics A MetricsConfiguration object.
         * @return Builder The Builder.
         */
        public Builder metrics(MetricsConfiguration metrics)
        {
            this.metrics = metrics;
            return this;
        }

        /**
         * Set the list of traffic sets that are executed only once before the actual
         * traffic mix. The execution of the setup traffic mix is not taken into
         * consideration when counting down the duration of the traffic mix execution.
         * 
         * @param setupTrafficMix A list of traffic set configuration options.
         * @return Builder The Builder.
         */
        public Builder setupTrafficMix(List<TrafficSetConfiguration> setupTrafficMix)
        {
            this.setupTrafficMix = setupTrafficMix;
            return this;
        }

        /**
         * Set the list of traffic sets that are executed repeatedly until the duration
         * is met. If duration is not defined, then the traffic sets are executed only
         * once.
         * 
         * @param trafficMix A list of traffic set configuration options.
         * @return Builder The Builder.
         */
        public Builder trafficMix(List<TrafficSetConfiguration> trafficMix)
        {
            this.trafficMix = trafficMix;
            return this;
        }

        /**
         * Creates the BsfLoadConfiguration object.
         * 
         * @return BsfLoadConfiguration The configuration options for the bsf-load tool.
         */
        public BsfLoadConfiguration build()
        {
            return new BsfLoadConfiguration(this);
        }
    }
}
