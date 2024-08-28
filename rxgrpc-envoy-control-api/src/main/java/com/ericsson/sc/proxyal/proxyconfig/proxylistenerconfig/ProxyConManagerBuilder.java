package com.ericsson.sc.proxyal.proxyconfig.proxylistenerconfig;

import java.util.Objects;

import com.google.protobuf.UInt32Value;

import io.envoyproxy.envoy.config.core.v3.Http2ProtocolOptions;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager.Builder;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager.ServerHeaderTransformation;

public class ProxyConManagerBuilder
{

    private String nfType;
    private ProxyListener proxyListener;

    public ProxyConManagerBuilder(String nfType,
                                  ProxyListener proxyListener)
    {
        this.nfType = nfType;
        this.proxyListener = proxyListener;
    }

    public ProxyConManagerBuilder(ProxyConManagerBuilder that)
    {
        this(that.getNfType(), that.getProxyListener());
    }

    public String getNfType()
    {
        return nfType;
    }

    public void setNfType(String nfType)
    {
        this.nfType = nfType;
    }

    public ProxyListener getProxyListener()
    {
        return proxyListener;
    }

    public void setProxyListener(ProxyListener proxyListener)
    {
        this.proxyListener = proxyListener;
    }

    public ProxyConManagerBuilder withProxyListener(ProxyListener proxyListener)
    {
        this.setProxyListener(proxyListener);
        return this;
    }

    public ProxyConManagerBuilder withNfType(String nfType)
    {
        this.setNfType(nfType);
        return this;
    }

    public Builder initBuilder()
    {

        String serverName = proxyListener.getScpServiceAddress() != null
                            && proxyListener.getScpServiceAddress().size() > 0 ? proxyListener.getScpServiceAddress().get(0) : null;

        var nwFilterTypedConfigBuilder = HttpConnectionManager.newBuilder()
                                                              .setServerHeaderTransformation(ServerHeaderTransformation.APPEND_IF_ABSENT)
                                                              .setServerName(nfType + "-" + serverName)
                                                              .setStatPrefix("ingress.n8e." + proxyListener.getScpInstanceName() + ".g3p.ingress");

        var http2Connection = Http2ProtocolOptions.newBuilder().setAllowConnect(true);
        proxyListener.getHpackTableSize().ifPresent(size -> http2Connection.setHpackTableSize(UInt32Value.of(size)));
        http2Connection.setMaxConcurrentStreams(UInt32Value.of(proxyListener.getMaxConcurrentStreams()));

        nwFilterTypedConfigBuilder.setHttp2ProtocolOptions(http2Connection);

        for (var proxy : proxyListener.getHttpFilterSet())
        {
            proxy.buildHttpFilter(proxyListener, nwFilterTypedConfigBuilder);
        }

        return nwFilterTypedConfigBuilder;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ProxyConManagerBuilder [nfType=" + nfType + ", proxyListener=" + proxyListener + "]";
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(nfType, proxyListener);
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
        ProxyConManagerBuilder other = (ProxyConManagerBuilder) obj;
        return Objects.equals(nfType, other.nfType) && Objects.equals(proxyListener, other.proxyListener);
    }

}
