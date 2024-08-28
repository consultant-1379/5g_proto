package com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig;

import static com.ericsson.sc.proxyal.proxyconfig.ProxyMetadataBuilder.VTAP_ENABLED;
import static com.ericsson.sc.proxyal.proxyconfig.ProxyMetadataBuilder.matchTLS;

import java.util.Objects;

import com.ericsson.sc.proxyal.proxyconfig.ProxyVtapSettings;
import com.google.protobuf.Any;
import com.google.protobuf.ListValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;

import io.envoyproxy.envoy.config.cluster.v3.Cluster.Builder;
import io.envoyproxy.envoy.config.cluster.v3.Cluster.TransportSocketMatch;
import io.envoyproxy.envoy.config.core.v3.TransportSocket;
import io.envoyproxy.envoy.extensions.transport_sockets.raw_buffer.v3.RawBuffer;
import io.envoyproxy.envoy.extensions.transport_sockets.tls.v3.UpstreamTlsContext;

public class ProxyUpstreamTransportSocketBuilder
{
    private ProxyCluster proxyCluster;
    private Builder clusterBuilder;

    public ProxyUpstreamTransportSocketBuilder(ProxyCluster proxyCluster,
                                               Builder clusterBuilder)
    {
        this.proxyCluster = proxyCluster;
        this.clusterBuilder = clusterBuilder;
    }

    public ProxyUpstreamTransportSocketBuilder(ProxyUpstreamTransportSocketBuilder that)
    {
        this(that.getProxyCluster(), that.getClusterBuilder());
    }

    public ProxyCluster getProxyCluster()
    {
        return proxyCluster;
    }

    public void setProxyCluster(ProxyCluster proxyCluster)
    {
        this.proxyCluster = proxyCluster;
    }

    public ProxyUpstreamTransportSocketBuilder withProxyCluster(ProxyCluster proxyCluster)
    {
        this.proxyCluster = proxyCluster;
        return this;
    }

    public Builder getClusterBuilder()
    {
        return clusterBuilder;
    }

    public void setClusterBuilder(Builder clusterBuilder)
    {
        this.clusterBuilder = clusterBuilder;
    }

    public ProxyUpstreamTransportSocketBuilder withClusterBuilder(Builder clusterBuilder)
    {
        this.clusterBuilder = clusterBuilder;
        return this;
    }

    public Builder initBuilder()
    {
        /*
         * This method builds each cluster in the envoy configuration. Currently the
         * current implementation assumes that vtap can only be enabled for non internal
         * clusters .For clusters with endpoints, transport socket matches are used to
         * support mixed tls and non-tls traffic with or without vtap enabled For
         * internal clusters we set only a transport socket either with tls or with non
         * tls based on tls property to avoid performance issues due to matching
         * criteria checking.
         * 
         */
        var clusterTls = proxyCluster.getTls();
        if (clusterTls.isPresent())
        {
            // Using this context on the cluster, creates HTTP/2.0 (with TLS) connection
            // between envoy and upstream backend
            var upstreamTlsContext = UpstreamTlsContext.newBuilder() //
                                                       .setAllowRenegotiation(false)
                                                       .setCommonTlsContext(clusterTls.get().buildTlsContext())
                                                       .build();
            var tscTls = TransportSocket.newBuilder().setName("envoy.transport_sockets.tls").setTypedConfig(Any.pack(upstreamTlsContext)).build();
            proxyCluster.getVtapSettings().filter(ProxyVtapSettings::getVtapEnabled).ifPresentOrElse(vTap ->
            {
                vTap.setTransportSocket(tscTls);
                var vTapBuilder = vTap.initBuilder();

                if (Boolean.TRUE.equals(proxyCluster.isDynamicCluster())) // Dynamic Clusters do not have any endpoints
                {
                    clusterBuilder.setTransportSocket(TransportSocket.newBuilder() //
                                                                     .setName("envoy.transport_sockets.tap")
                                                                     .setTypedConfig(Any.pack(vTapBuilder.build()))
                                                                     .build());
                }
                else if (Boolean.FALSE.equals(proxyCluster.isAggregateCluster())) // Don't add TAP config for aggregated clusters
                {
                    var tscCombo = TransportSocket.newBuilder().setName("envoy.transport_sockets.tap").setTypedConfig(Any.pack(vTapBuilder.build())).build();

                    clusterBuilder.addTransportSocketMatches(initTransportSocketMatchBuilder("transport_socket_tls_with_tap",
                                                                                             tscCombo,
                                                                                             "true",
                                                                                             "true").build());
                }
            }, () ->
            {
                if (Boolean.TRUE.equals(proxyCluster.isInternalCluster()) || Boolean.TRUE.equals(proxyCluster.isDynamicCluster()))
                {
                    clusterBuilder.setTransportSocket(tscTls);
                }
                else
                {
                    clusterBuilder.addTransportSocketMatches(initTransportSocketMatchBuilder("transport_socket_tls_without_tap",
                                                                                             tscTls,
                                                                                             "false",
                                                                                             "true").build());
                }
            });
        }
        var tscRawBuffer = TransportSocket.newBuilder()
                                          .setName("envoy.transport_sockets.raw_buffer")
                                          .setTypedConfig(Any.pack(RawBuffer.newBuilder().build()))
                                          .build();
        proxyCluster.getVtapSettings().filter(ProxyVtapSettings::getVtapEnabled).ifPresentOrElse(vTap ->
        {

            vTap.setTransportSocket(tscRawBuffer);
            var vTapBuilder = vTap.initBuilder();

            if (Boolean.TRUE.equals(proxyCluster.isDynamicCluster()) && !clusterTls.isPresent()) // Dynamic Clusters do not have any endpoints
            {
                clusterBuilder.setTransportSocket(TransportSocket.newBuilder() //
                                                                 .setName("envoy.transport_sockets.tap")
                                                                 .setTypedConfig(Any.pack(vTapBuilder.build()))
                                                                 .build());
            }
            else if (Boolean.FALSE.equals(proxyCluster.isAggregateCluster())) // Don't add TAP config for aggregated clusters
            {
                var tscCombo = TransportSocket.newBuilder().setName("envoy.transport_sockets.tap").setTypedConfig(Any.pack(vTapBuilder.build())).build();

                clusterBuilder.addTransportSocketMatches(initTransportSocketMatchBuilder("transport_socket_non_tls_with_tap",
                                                                                         tscCombo,
                                                                                         "true",
                                                                                         "false").build());
            }
        }, () ->
        {
            if ((Boolean.TRUE.equals(proxyCluster.isInternalCluster()) || Boolean.TRUE.equals(proxyCluster.isDynamicCluster())) && !clusterTls.isPresent())
            {
                clusterBuilder.setTransportSocket(tscRawBuffer);
            }
            else if ((Boolean.FALSE.equals(proxyCluster.isInternalCluster()) && Boolean.FALSE.equals(proxyCluster.isDynamicCluster())))
            {
                var socketMatchPlainText = TransportSocketMatch.newBuilder()
                                                               .setName("transport_socket_non_tls_without_tap")
                                                               .setTransportSocket(tscRawBuffer)
                                                               .build();
                clusterBuilder.addTransportSocketMatches(socketMatchPlainText);
            }
        });
        return clusterBuilder;
    }

    private com.google.protobuf.Value.Builder initFieldsBuilder(String stringValue)
    {
        return Value.newBuilder().setListValue(ListValue.newBuilder().addValues(Value.newBuilder().setStringValue(stringValue)));
    }

    private io.envoyproxy.envoy.config.cluster.v3.Cluster.TransportSocketMatch.Builder initTransportSocketMatchBuilder(String name,
                                                                                                                       TransportSocket transportSocket,
                                                                                                                       String stringValue,
                                                                                                                       String stringValue2)
    {
        return TransportSocketMatch.newBuilder()
                                   .setName(name)
                                   .setTransportSocket(transportSocket)
                                   .setMatch(Struct.newBuilder()
                                                   .putFields(VTAP_ENABLED, initFieldsBuilder(stringValue).build())
                                                   .putFields(matchTLS, initFieldsBuilder(stringValue2).build())
                                                   .build());
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ProxyUpstreamTransportSocketBuilder [proxyCluster=" + proxyCluster + ", clusterBuilder=" + clusterBuilder + "]";
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(clusterBuilder, proxyCluster);
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
        ProxyUpstreamTransportSocketBuilder other = (ProxyUpstreamTransportSocketBuilder) obj;
        return Objects.equals(clusterBuilder, other.clusterBuilder) && Objects.equals(proxyCluster, other.proxyCluster);
    }
}
