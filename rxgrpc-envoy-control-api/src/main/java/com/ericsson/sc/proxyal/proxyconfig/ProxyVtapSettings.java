/**
 * COPYRIGHT ERICSSON GMBH 2022
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Apr 11, 2022
 *     Author: epitgio
 */

package com.ericsson.sc.proxyal.proxyconfig;

import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.common.RuntimeEnvironment;
import com.ericsson.utilities.common.IP_VERSION;
import com.google.protobuf.Duration;
import com.google.protobuf.UInt32Value;

import io.envoyproxy.envoy.config.common.matcher.v3.MatchPredicate;
import io.envoyproxy.envoy.config.core.v3.GrpcService;
import io.envoyproxy.envoy.config.core.v3.GrpcService.GoogleGrpc;
import io.envoyproxy.envoy.config.core.v3.TransportSocket;
import io.envoyproxy.envoy.config.tap.v3.OutputConfig;
import io.envoyproxy.envoy.config.tap.v3.OutputSink;
import io.envoyproxy.envoy.config.tap.v3.OutputSink.Format;
import io.envoyproxy.envoy.config.tap.v3.StreamingGrpcSink;
import io.envoyproxy.envoy.config.tap.v3.TapConfig;
import io.envoyproxy.envoy.extensions.common.tap.v3.CommonExtensionConfig;
import io.envoyproxy.envoy.extensions.transport_sockets.tap.v3.Tap;
import io.envoyproxy.envoy.extensions.transport_sockets.tap.v3.Tap.Builder;

/**
 * 
 */
public class ProxyVtapSettings
{
    private static final Logger log = LoggerFactory.getLogger(ProxyVtapSettings.class);

    private static final String ENV_TAP_COLLECTOR_PORT = "TAP_COLLECTOR_PORT";
    private static final long GRPC_TIMEOUT_SECONDS = 5;
    private static final UInt32Value OUTPUT_MAX_BUFFERED_BYTES = UInt32Value.of(65536);
    private static final boolean OUTPUT_STREAMING = true;
    private static final String EGRESS_VTAP = "egress-vtap";
    private static final String INGRESS_VTAP = "ingress-vtap";

    private String name;
    private boolean vtapEnabled;
    private String nfInstanceName;
    private final Direction direction;

    public enum Direction
    {
        INGRESS,
        EGRESS
    }

    public enum IpVersion
    {
        IPV4,
        IPV6
    }

    private Optional<TransportSocket> transportSocket = Optional.empty();

    // Internal Variables
    private final Integer tapColPort;
    private final String targetUri;
    private final String group;
    private final String statsPrefix;

    public ProxyVtapSettings(String name,
                             String nfInstanceName,
                             boolean vtapEnabled,
                             Direction direction)
    {
        this.name = name;
        this.vtapEnabled = vtapEnabled;
        this.direction = direction;
        this.nfInstanceName = nfInstanceName;
        this.group = this.direction == Direction.EGRESS ? EGRESS_VTAP : INGRESS_VTAP;
        this.statsPrefix = nfInstanceName + ".#!_#" + group;

        var tapColPortString = EnvVars.get(ENV_TAP_COLLECTOR_PORT);
        if (tapColPortString != null && !tapColPortString.isEmpty())
        {
            this.tapColPort = Integer.valueOf(tapColPortString);
        }
        else
        {
            tapColPort = 9000;
            log.info("Tap Collector port not found in env vars. Using default port {}", tapColPort);
        }

        this.targetUri = RuntimeEnvironment.getDeployedIpVersion()
                                           .equals(IP_VERSION.IPV4) ? new StringBuilder().append("localhost").append(":").append(tapColPort).toString()
                                                                    : new StringBuilder().append("::1").append(":").append(tapColPort).toString();
    }

    public ProxyVtapSettings(ProxyVtapSettings anotherProxyVtapSettings)
    {
        this(anotherProxyVtapSettings.getName(),
             anotherProxyVtapSettings.getNfInstanceName(),
             anotherProxyVtapSettings.getVtapEnabled(),
             anotherProxyVtapSettings.getDirection());
        this.transportSocket = anotherProxyVtapSettings.getTransportSocket().map(ts -> TransportSocket.newBuilder(ts).build());
    }

    /**
     * @param transportSocket the transportSocket to set
     */
    public void setTransportSocket(TransportSocket transportSocket)
    {
        this.transportSocket = Optional.ofNullable(transportSocket);
    }

    /**
     * @return the transportSocket
     */
    public Optional<TransportSocket> getTransportSocket()
    {
        return this.transportSocket;
    }

    /**
     * @return the nfInstanceName
     */
    public String getNfInstanceName()
    {
        return nfInstanceName;
    }

    /**
     * @param nfInstanceName the nfInstanceName to set
     */
    public void setNfInstanceName(String nfInstanceName)
    {
        this.nfInstanceName = nfInstanceName;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    public ProxyVtapSettings withVtapName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * @return the vtapEnabled
     */
    public boolean getVtapEnabled()
    {
        return vtapEnabled;
    }

    /**
     * @param vtapEnabled the vtapEnabled to set
     */
    public void setVtapEnabled(boolean vtapEnabled)
    {
        this.vtapEnabled = vtapEnabled;
    }

    public ProxyVtapSettings withVtapEnabled(Boolean vtapEnabled)
    {
        this.vtapEnabled = vtapEnabled;
        return this;
    }

    public int getTapColPort()
    {
        return tapColPort;
    }

    public String getStatsPrefix()
    {
        return statsPrefix;
    }

    /**
     * @return the direction
     */
    public Direction getDirection()
    {
        return direction;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(direction, group, name, nfInstanceName, statsPrefix, tapColPort, targetUri, transportSocket, vtapEnabled);
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
        ProxyVtapSettings other = (ProxyVtapSettings) obj;
        return direction == other.direction && Objects.equals(group, other.group) && Objects.equals(name, other.name)
               && Objects.equals(nfInstanceName, other.nfInstanceName) && Objects.equals(statsPrefix, other.statsPrefix)
               && Objects.equals(tapColPort, other.tapColPort) && Objects.equals(targetUri, other.targetUri)
               && Objects.equals(transportSocket, other.transportSocket) && vtapEnabled == other.vtapEnabled;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ProxyVtapSettings [name=" + name + ", vtapEnabled=" + vtapEnabled + ", nfInstanceName=" + nfInstanceName + ", direction=" + direction
               + ", transportSocket=" + transportSocket + ", tapColPort=" + tapColPort + ", targetUri=" + targetUri + ", group=" + group + ", statsPrefix="
               + statsPrefix + "]";
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ericsson.sc.proxyal.proxyconfig.ProxyBuilder#build(java.lang.Object[])
     */
    public Builder initBuilder()
    {

        var sink = OutputSink.newBuilder()
                             .setFormat(Format.PROTO_BINARY_LENGTH_DELIMITED)
                             .setStreamingGrpc(StreamingGrpcSink.newBuilder()
                                                                .setGrpcService(GrpcService.newBuilder()
                                                                                           .setGoogleGrpc(GoogleGrpc.newBuilder()
                                                                                                                    .setTargetUri(targetUri)
                                                                                                                    .setStatPrefix(this.statsPrefix))
                                                                                           .setTimeout(Duration.newBuilder().setSeconds(GRPC_TIMEOUT_SECONDS))))
                             .build();

        return Tap.newBuilder()
                  .setCommonConfig(CommonExtensionConfig.newBuilder()
                                                        .setStaticConfig(TapConfig.newBuilder()
                                                                                  .setMatch(MatchPredicate.newBuilder().setAnyMatch(true).build())
                                                                                  .setOutputConfig(OutputConfig.newBuilder()
                                                                                                               .addSinks(sink)
                                                                                                               .setMaxBufferedRxBytes(OUTPUT_MAX_BUFFERED_BYTES)
                                                                                                               .setMaxBufferedTxBytes(OUTPUT_MAX_BUFFERED_BYTES)
                                                                                                               .setStreaming(OUTPUT_STREAMING))))
                  .setTransportSocket(transportSocket.orElse(TransportSocket.getDefaultInstance()));

    }

}
