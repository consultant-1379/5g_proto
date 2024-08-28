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
 * Created on: Mar 2, 2022
 *     Author: eodnouk
 */

package com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.proxyal.proxyconfig.proxylistenerconfig.ProxyListener;
import com.ericsson.utilities.common.Pair;
import com.google.protobuf.Any;
import com.google.protobuf.Duration;

import io.envoyproxy.envoy.extensions.filters.http.eric_ingress_ratelimit.v3.ActionProfile;
import io.envoyproxy.envoy.extensions.filters.http.eric_ingress_ratelimit.v3.BucketActionPair;
import io.envoyproxy.envoy.extensions.filters.http.eric_ingress_ratelimit.v3.IngressRateLimit;
import io.envoyproxy.envoy.extensions.filters.http.eric_ingress_ratelimit.v3.MapEntry;
import io.envoyproxy.envoy.extensions.filters.http.eric_ingress_ratelimit.v3.Namespace;
import io.envoyproxy.envoy.extensions.filters.http.eric_ingress_ratelimit.v3.RateLimit;
import io.envoyproxy.envoy.extensions.filters.http.eric_ingress_ratelimit.v3.RateLimit.Network;
import io.envoyproxy.envoy.extensions.filters.http.eric_ingress_ratelimit.v3.RateLimit.RoamingPartner;
import io.envoyproxy.envoy.extensions.filters.http.eric_ingress_ratelimit.v3.RateLimitServiceConfig;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager.Builder;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpFilter;

/**
 * 
 */
public class ProxyGlobalRateLimitFilter implements IfHttpFilter
{
    private static final Logger log = LoggerFactory.getLogger(ProxyGlobalRateLimitFilter.class);
    private static final String FILTER_NAME = "envoy.filters.http.eric_ingress_ratelimit";
    private Namespace namespace;
    private Integer timeout = 20;
    private String clusterName;
    private List<Float> watermarkList;
    private Optional<Pair<String, ProxyRateLimitActionProfile>> network = Optional.empty();
    private Map<String, Pair<String, Optional<Pair<String, ProxyRateLimitActionProfile>>>> roamingPartners = new HashMap<>();
    private ProxyRateLimitActionProfile rlfServiceError;

    /**
     * @param namespace
     * @param timeout
     */
    public ProxyGlobalRateLimitFilter(Namespace namespace,
                                      Integer timeout,
                                      List<Float> wml,
                                      String clusterName,
                                      ProxyRateLimitActionProfile rlfServiceError)
    {
        this.namespace = namespace;
        this.timeout = timeout;
        this.watermarkList = wml;
        this.clusterName = clusterName;
        this.rlfServiceError = rlfServiceError;
    }

    /**
     * @param network the network to set
     */
    public void setNetwork(Optional<Pair<String, ProxyRateLimitActionProfile>> network)
    {
        this.network = network;
    }

    /**
     * @param timeout the timeout to set
     */
    public void setTimeout(Integer timeout)
    {
        this.timeout = timeout;
    }

    /**
     * @param namespace the namespace to set
     */
    public void setNamespace(Namespace namespace)
    {
        this.namespace = namespace;
    }

    /**
     * @param rps the rps to set
     */
    public void setRoamingPartners(Map<String, Pair<String, Optional<Pair<String, ProxyRateLimitActionProfile>>>> rps)
    {
        this.roamingPartners = rps;
    }

    /**
     * @return the namespace
     */
    public Namespace getNamespace()
    {
        return namespace;
    }

    /**
     * @param clusterName the clusterName to set
     */
    public void setClusterName(String clusterName)
    {
        this.clusterName = clusterName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ericsson.sc.proxyal.proxyconnmanager.ProxyConnManagerConfig#appendConfig(
     * com.ericsson.sc.proxyal.proxyconfig.ProxyListener,
     * io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.
     * HttpConnectionManager.Builder)
     */
    @Override
    public void buildHttpFilter(ProxyListener proxyListener,
                                Builder builder)
    {

        long timeoutSeconds = TimeUnit.MILLISECONDS.toSeconds(timeout);
        int timeoutNanos = (int) TimeUnit.MILLISECONDS.toNanos(timeout - TimeUnit.SECONDS.toMillis(timeoutSeconds));
        var ingressRateLimitBuilder = IngressRateLimit.newBuilder().setNamespace(namespace);

        ingressRateLimitBuilder.setTimeout(Duration.newBuilder().setSeconds(timeoutSeconds).setNanos(timeoutNanos).build());
        // Set RateLimitServiceConfig
        ingressRateLimitBuilder.setRateLimitService(RateLimitServiceConfig.newBuilder()
                                                                          .setServiceClusterName(clusterName)
                                                                          .setServiceErrorAction(rlfServiceError.buildAction())
                                                                          .build());

        // Set Priorities watermarkList
        for (int i = 0; i < watermarkList.size(); i++)
        {
            ingressRateLimitBuilder.addWatermarks(watermarkList.get(i));
        }
        log.debug("watermarkList = {}", ingressRateLimitBuilder.getWatermarksList());
        // Set Network Limits when available
        if (network.isPresent())
        {
            ingressRateLimitBuilder.addLimits(RateLimit.newBuilder()
                                                       .setNetwork(Network.newBuilder()
                                                                          .setBucketAction(BucketActionPair.newBuilder()
                                                                                                           .setBucketName(network.get().getFirst())
                                                                                                           .setOverLimitAction(network.get()
                                                                                                                                      .getSecond()
                                                                                                                                      .buildAction()))));
        }
        // Set Roaming Partner Limits when available
        else if (roamingPartners != null && !roamingPartners.isEmpty())
        {
            var rpLimit = RoamingPartner.newBuilder();
            rpLimit.setRpNotFoundAction(ActionProfile.newBuilder().setActionPassMessage(true).build());
            for (var rp : roamingPartners.entrySet())
            {
                var mapEntry = MapEntry.newBuilder().setRpName(rp.getValue().getFirst());
                var bucketActionPair = rp.getValue().getSecond();
                if (bucketActionPair.isPresent())
                {
                    mapEntry.setBucketActionPair(BucketActionPair.newBuilder()
                                                                 .setBucketName(bucketActionPair.get().getFirst())
                                                                 .setOverLimitAction(bucketActionPair.get().getSecond().buildAction()));
                }
                rpLimit.putRpBucketActionTable(rp.getKey(), mapEntry.build());

            }
            ingressRateLimitBuilder.addLimits(RateLimit.newBuilder().setRoamingPartner(rpLimit.build()));

        }
        log.debug("Limits!! = {}", ingressRateLimitBuilder.getLimitsList());

        builder.addHttpFilters(HttpFilter.newBuilder().setName(FILTER_NAME).setTypedConfig(Any.pack(ingressRateLimitBuilder.build())));

    }

    @Override
    public Priorities getPriority()
    {
        return Priorities.GLOBAL_RATE_LIMIT;
    }

}
