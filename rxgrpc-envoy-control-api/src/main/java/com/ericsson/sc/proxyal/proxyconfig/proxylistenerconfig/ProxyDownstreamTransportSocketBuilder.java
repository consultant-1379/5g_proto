package com.ericsson.sc.proxyal.proxyconfig.proxylistenerconfig;

import java.util.Objects;

import com.ericsson.sc.proxyal.proxyconfig.ProxyVtapSettings;
import com.google.protobuf.Any;
import com.google.protobuf.BoolValue;

import io.envoyproxy.envoy.config.core.v3.TransportSocket;
import io.envoyproxy.envoy.config.listener.v3.Listener.Builder;
import io.envoyproxy.envoy.extensions.transport_sockets.raw_buffer.v3.RawBuffer;
import io.envoyproxy.envoy.extensions.transport_sockets.tls.v3.DownstreamTlsContext;

public class ProxyDownstreamTransportSocketBuilder
{

    private ProxyListener proxyListener;
    private Builder listenerBuilder;
    private io.envoyproxy.envoy.config.listener.v3.FilterChain.Builder filterChainBuilder;

    public ProxyDownstreamTransportSocketBuilder(ProxyListener proxyListener,
                                                 Builder listenerBuilder,
                                                 io.envoyproxy.envoy.config.listener.v3.FilterChain.Builder filterChainBuilder)
    {
        this.proxyListener = proxyListener;
        this.listenerBuilder = listenerBuilder;
        this.filterChainBuilder = filterChainBuilder;
    }

    public ProxyDownstreamTransportSocketBuilder(ProxyDownstreamTransportSocketBuilder that)
    {
        this(that.getProxyListener(), that.getListenerBuilder(), that.getFilterChainBuilder());
    }

    public ProxyListener getProxyListener()
    {
        return proxyListener;
    }

    public void setProxyListener(ProxyListener proxyListener)
    {
        this.proxyListener = proxyListener;
    }

    public ProxyDownstreamTransportSocketBuilder withProxyListener(ProxyListener proxyListener)
    {
        this.proxyListener = proxyListener;
        return this;
    }

    public Builder getListenerBuilder()
    {
        return listenerBuilder;
    }

    public void setListenerBuilder(Builder listenerBuilder)
    {
        this.listenerBuilder = listenerBuilder;
    }

    public ProxyDownstreamTransportSocketBuilder withListenerBuilder(Builder listenerBuilder)
    {
        this.listenerBuilder = listenerBuilder;
        return this;
    }

    public io.envoyproxy.envoy.config.listener.v3.FilterChain.Builder getFilterChainBuilder()
    {
        return filterChainBuilder;
    }

    public void setFilterChainBuilder(io.envoyproxy.envoy.config.listener.v3.FilterChain.Builder filterChainBuilder)
    {
        this.filterChainBuilder = filterChainBuilder;
    }

    public ProxyDownstreamTransportSocketBuilder withFilterChainBuilder(io.envoyproxy.envoy.config.listener.v3.FilterChain.Builder filterChainBuilder)
    {
        this.filterChainBuilder = filterChainBuilder;
        return this;
    }

    public Builder initBuilder()
    {
        var proxyListenerTls = proxyListener.getTls();
        if (proxyListenerTls.isPresent()) // because we use Boolean, not boolean
        {

            var downstreamTlsContext = DownstreamTlsContext.newBuilder()
                                                           .setRequireClientCertificate(BoolValue.of(true))
                                                           .setCommonTlsContext(proxyListenerTls.get().buildTlsContext())
                                                           .build();

            // =================== VTAP START ====================================
            var tsc = TransportSocket.newBuilder().setName("envoy.transport_sockets.tls").setTypedConfig(Any.pack(downstreamTlsContext)).build();

            filterChainBuilder.setTransportSocket(tsc);

            proxyListener.getVtapSettings().filter(ProxyVtapSettings::getVtapEnabled).ifPresent(vTap ->
            {

                vTap.setTransportSocket(tsc);
                var vTapBuilder = vTap.initBuilder();
                filterChainBuilder.setTransportSocket(TransportSocket.newBuilder()
                                                                     .setName("envoy.transport_sockets.tap")
                                                                     .setTypedConfig(Any.pack(vTapBuilder.build()))
                                                                     .build());
            });

            // =================== VTAP END ====================================

            listenerBuilder.addFilterChains(filterChainBuilder.build());

        }
        else
        {
            // =================== VTAP START ====================================
            proxyListener.getVtapSettings().filter(ProxyVtapSettings::getVtapEnabled).ifPresent(vTap ->
            {
                var tsc = TransportSocket.newBuilder()
                                         .setName("envoy.transport_sockets.raw_buffer")
                                         .setTypedConfig(Any.pack(RawBuffer.newBuilder().build()))
                                         .build();
                vTap.setTransportSocket(tsc);
                var vTapBuilder = vTap.initBuilder();
                filterChainBuilder.setTransportSocket(TransportSocket.newBuilder()
                                                                     .setName("envoy.transport_sockets.tap")
                                                                     .setTypedConfig(Any.pack(vTapBuilder.build()))
                                                                     .build());

            });
            // =================== VTAP END ====================================

            listenerBuilder.addFilterChains(filterChainBuilder.build());
        }

        return listenerBuilder;

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ProxyDownstreamTransportSocketBuilder [proxyListener=" + proxyListener + ", listenerBuilder=" + listenerBuilder + ", filterChainBuilder="
               + filterChainBuilder + "]";
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(filterChainBuilder, listenerBuilder, proxyListener);
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
        ProxyDownstreamTransportSocketBuilder other = (ProxyDownstreamTransportSocketBuilder) obj;
        return Objects.equals(filterChainBuilder, other.filterChainBuilder) && Objects.equals(listenerBuilder, other.listenerBuilder)
               && Objects.equals(proxyListener, other.proxyListener);
    }

}
