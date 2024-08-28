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
 * Created on: Jul 26, 2021
 *     Author: eaoknkr
 */

package com.ericsson.sc.proxyal.proxyconfig;

import io.envoyproxy.envoy.config.core.v3.ApiConfigSource;
import io.envoyproxy.envoy.config.core.v3.ApiConfigSource.ApiType;
import io.envoyproxy.envoy.config.core.v3.ConfigSource;
import io.envoyproxy.envoy.config.core.v3.GrpcService;
import io.envoyproxy.envoy.config.core.v3.GrpcService.EnvoyGrpc;
import io.envoyproxy.envoy.extensions.transport_sockets.tls.v3.CommonTlsContext;
import io.envoyproxy.envoy.extensions.transport_sockets.tls.v3.SdsSecretConfig;
import io.envoyproxy.envoy.extensions.transport_sockets.tls.v3.TlsParameters;
import io.envoyproxy.envoy.extensions.transport_sockets.tls.v3.TlsParameters.TlsProtocol;

/**
 * 
 */
public class ProxyTls
{
    private String trustedAuthority;
    private String asymKey;
    private static final String DEFAULT_ASYM = "default_asym";
    private static final String DEFAULT_ROOTCA = "default_ca";

    public ProxyTls()
    {
        this.trustedAuthority = DEFAULT_ROOTCA;
        this.asymKey = DEFAULT_ASYM;
    }

    // to be used for internal communication where the certificate names
    // requests by envoy from the sdsServer are hardcoded
    public ProxyTls(String trustedAuthority,
                    String asymKey)
    {
        this.trustedAuthority = trustedAuthority;
        this.asymKey = asymKey;
    }

    public ProxyTls(ProxyTls proxyTls)
    {
        this.trustedAuthority = proxyTls.getTrustedAuthority();
        this.asymKey = proxyTls.getAsymKey();
    }

    /**
     * @return the single value of trustedAuthority to be mapped
     */
    public String getTrustedAuthority()
    {
        return trustedAuthority;
    }

    public void setTrustedAuthority(String tauth)
    {
        this.trustedAuthority = tauth;
    }

    public String getAsymKey()
    {
        return this.asymKey;
    }

    public void setAsymKey(String ak)
    {
        this.asymKey = ak;
    }

    public CommonTlsContext buildTlsContext()
    {
        var sdsConfig = ConfigSource.newBuilder()
                                    .setResourceApiVersion(io.envoyproxy.envoy.config.core.v3.ApiVersion.V3)
                                    .setApiConfigSource(ApiConfigSource.newBuilder()
                                                                       .setTransportApiVersion(io.envoyproxy.envoy.config.core.v3.ApiVersion.V3)
                                                                       .setApiType(ApiType.GRPC)
                                                                       .addGrpcServices(GrpcService.newBuilder()
                                                                                                   .setEnvoyGrpc(EnvoyGrpc.newBuilder()
                                                                                                                          .setClusterName("internal_sds")
                                                                                                                          .build())
                                                                                                   .build())
                                                                       .build())
                                    .build();

        return CommonTlsContext.newBuilder() //
                               .setTlsParams(TlsParameters.newBuilder()
                                                          .setTlsMinimumProtocolVersion(TlsProtocol.TLSv1_2)
                                                          .setTlsMaximumProtocolVersion(TlsProtocol.TLSv1_3)
                                                          .build())
                               .addTlsCertificateSdsSecretConfigs(SdsSecretConfig.newBuilder().setName(this.asymKey).setSdsConfig(sdsConfig).build())
                               .setValidationContextSdsSecretConfig(SdsSecretConfig.newBuilder().setName(this.trustedAuthority).setSdsConfig(sdsConfig).build())
                               .addAlpnProtocols("h2") //
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
        final var prime = 31;
        var result = 1;
        result = prime * result + ((asymKey == null) ? 0 : asymKey.hashCode());
        result = prime * result + ((trustedAuthority == null) ? 0 : trustedAuthority.hashCode());
        return result;
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
        ProxyTls other = (ProxyTls) obj;
        if (asymKey == null)
        {
            if (other.asymKey != null)
            {
                return false;
            }
        }
        else if (!asymKey.equals(other.asymKey))
        {
            return false;
        }
        if (trustedAuthority == null)
        {
            if (other.trustedAuthority != null)
            {
                return false;
            }
        }
        else if (!trustedAuthority.equals(other.trustedAuthority))
        {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ProxyTls [trustedAuthority=" + trustedAuthority + ", asymKey=" + asymKey + "]";
    }
}
