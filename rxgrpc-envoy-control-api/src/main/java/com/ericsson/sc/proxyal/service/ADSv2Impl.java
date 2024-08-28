/**
 * COPYRIGHT ERICSSON GMBH 2020
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jun 16, 2020
 *     Author: eedrak
 */

package com.ericsson.sc.proxyal.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.envoyproxy.envoy.api.v2.DiscoveryRequest;
import io.envoyproxy.envoy.api.v2.DiscoveryResponse;
import io.envoyproxy.envoy.service.discovery.v2.RxAggregatedDiscoveryServiceGrpc.AggregatedDiscoveryServiceImplBase;

import io.envoyproxy.envoy.config.core.v3.Node;

import io.reactivex.Flowable;

class ADSv2Impl extends AggregatedDiscoveryServiceImplBase
{
    private static final Logger log = LoggerFactory.getLogger(ADSv2Impl.class);

    private final EnvoyAdsLogic adsLogicInstance;

    /**
     * 
     */
    ADSv2Impl(EnvoyAdsLogic adsLogic)
    {
        adsLogicInstance = adsLogic;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * io.envoyproxy.envoy.service.discovery.v2.RxAggregatedDiscoveryServiceGrpc.
     * AggregatedDiscoveryServiceImplBase#streamAggregatedResources(io.reactivex.
     * Flowable)
     */
    @Override
    public Flowable<DiscoveryResponse> streamAggregatedResources(Flowable<DiscoveryRequest> request)
    {
        // Needed for troubleshooting.
        request = request.doOnNext(req ->
        {
            if (log.isDebugEnabled())
            {
                log.debug("{}: Received request details ({} (version {}, nonce {}):\n{}",
                          req.getNode().getId(),
                          lastWordOf(req.getTypeUrl()),
                          req.getVersionInfo(),
                          req.getResponseNonce(),
                          req);
            }
            else
            {
                log.info("{}: Received request ({} (version {}, nonce {})",
                         req.getNode().getId(),
                         lastWordOf(req.getTypeUrl()),
                         req.getVersionInfo(),
                         req.getResponseNonce());
            }
        });
        Flowable<io.envoyproxy.envoy.service.discovery.v3.DiscoveryRequest> v3req = request.map(this::mapV2ReqToV3);
        return adsLogicInstance.streamAggregatedResources(v3req).map(this::mapV3RespToV2);
    }

    io.envoyproxy.envoy.service.discovery.v3.DiscoveryRequest mapV2ReqToV3(DiscoveryRequest v2Req)
    {
        io.envoyproxy.envoy.service.discovery.v3.DiscoveryRequest v3Request = io.envoyproxy.envoy.service.discovery.v3.DiscoveryRequest.newBuilder()
                                                                                                                                       .setTypeUrl(v2Req.getTypeUrl())
                                                                                                                                       .setVersionInfo(v2Req.getVersionInfo())
                                                                                                                                       .setResponseNonce(v2Req.getResponseNonce())
                                                                                                                                       .setNode(Node.newBuilder()
                                                                                                                                                    .setId(v2Req.getNode()
                                                                                                                                                                .getId())
                                                                                                                                                    .build())
                                                                                                                                       .build();

        log.info("v2Request:{}.", v3Request);
        return v3Request;
    }

    DiscoveryResponse mapV3RespToV2(io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse v3Resp)
    {
        DiscoveryResponse v3Response = DiscoveryResponse.newBuilder()
                                                        .setTypeUrl(v3Resp.getTypeUrl())
                                                        .setVersionInfo(v3Resp.getVersionInfo())
                                                        .setNonce(v3Resp.getNonce())
                                                        .addAllResources(v3Resp.getResourcesList())
                                                        .build();

        log.info("v3Response:{}.", v3Response);
        return v3Response;
    }

    /**
     * Give a text with words separated by dots, return the last word. This is
     * typically used to get the subsystem to be configured from a typeUrl, for
     * example: TypeUrl: type.googleapis.com/envoy.api.v2.Cluster Returns: Cluster
     *
     * @param text
     * @return last word of text
     */
    private String lastWordOf(final String text)
    {
        return text.substring(text.lastIndexOf('.') + 1);
    }
}
