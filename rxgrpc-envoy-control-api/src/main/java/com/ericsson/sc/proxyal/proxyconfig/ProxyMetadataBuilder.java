package com.ericsson.sc.proxyal.proxyconfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig.ProxyCluster;
import com.ericsson.sc.proxyal.proxyconfig.proxyendpointconfig.ProxyEndpoint;
import com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig.ProxyRetryPolicy;
import com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig.ProxyRoute;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;

import io.envoyproxy.envoy.config.core.v3.Metadata;
import io.envoyproxy.envoy.config.core.v3.Metadata.Builder;

public class ProxyMetadataBuilder
{

    public enum MetaDataType
    {

        LUA("envoy.filters.http.lua"),
        ERIC_PROXY("envoy.filters.http.eric_proxy"),
        LB("envoy.lb"),
        TRANSPORT_SOCKET("envoy.transport_socket_match"),
        ERIC_PROXY_ENDPOINT("envoy.eric_proxy"),
        CLUSTER("envoy.eric_proxy.cluster");

        String name;

        MetaDataType(String name)
        {
            this.name = name;
        }

        @Override
        public String toString()
        {
            return this.name;
        }

    }

    public static final String HOST_KEY = "host";
    public static final String VTAP_ENABLED = "vtap_enabled";
    public static final String matchTLS = "matchTLS";

    private ProxyMetadataMap metadata;

    public ProxyMetadataBuilder()
    {

    }

    public ProxyMetadataBuilder(ProxyMetadataMap metadata)
    {
        this.metadata = metadata;
    }

    public ProxyMetadataMap getMetadata()
    {
        return metadata;
    }

    public Builder initMdBuilder()
    {
        var metaDataBuilder = Metadata.newBuilder();
        var metadataMap = metadata.getMetadataMap();

        if (metadataMap != null && !metadataMap.isEmpty())
        {
            metadataMap.entrySet().stream().forEach(root ->
            {
                Map<String, Value> mdMap = new HashMap<>();
                root.getValue().entrySet().stream().forEach(entry ->
                {
                    mdMap.put(entry.getKey(), entry.getValue().getMetadataValue());
                });

                Struct jsonFormat = Struct.newBuilder().putAllFields(mdMap).build();
                metaDataBuilder.putFilterMetadata(root.getKey().toString(), jsonFormat);
            });
        }

        return metaDataBuilder;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ProxyMetadataBuidler [proxyMetadata=" + metadata + "]";
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(metadata);
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
        ProxyMetadataBuilder other = (ProxyMetadataBuilder) obj;
        return Objects.equals(metadata, other.metadata);

    }

}
