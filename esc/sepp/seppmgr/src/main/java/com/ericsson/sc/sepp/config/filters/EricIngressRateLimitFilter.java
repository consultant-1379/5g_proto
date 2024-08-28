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

package com.ericsson.sc.sepp.config.filters;

import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.configutil.CommonConfigUtils;
import com.ericsson.sc.configutil.CommonConfigUtils.RateLimiting.RateLimitType;
import com.ericsson.sc.configutil.CommonConfigUtils.RateLimiting.RateLimitedEntity;
import com.ericsson.sc.glue.IEricProxyFilter;
import com.ericsson.sc.glue.IfNetwork;
import com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig.IfHttpFilter;
import com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig.ProxyGlobalRateLimitFilter;
import com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig.ProxyRateLimitActionProfile;
import com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig.ProxyRateLimitActionProfile.Type;
import com.ericsson.sc.sepp.config.ConfigUtils;
import com.ericsson.sc.sepp.config.Egress;
import com.ericsson.sc.sepp.model.NfInstance;
import com.ericsson.sc.sepp.model.OwnNetwork;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.common.Pair;
import com.ericsson.utilities.common.Utils;

import io.envoyproxy.envoy.extensions.filters.http.eric_ingress_ratelimit.v3.Namespace;

/**
 * 
 */
public class EricIngressRateLimitFilter implements IEricProxyFilter
{
    private static final Logger log = LoggerFactory.getLogger(EricIngressRateLimitFilter.class);
    private final NfInstance seppInst;
    private final IfNetwork network;
    private final boolean isTls;

    public EricIngressRateLimitFilter(NfInstance seppInstance,
                                      IfNetwork network,
                                      boolean isTls)
    {
        this.seppInst = seppInstance;
        this.network = network;
        this.isTls = isTls;
    }

    public Optional<IfHttpFilter> create()
    {
        var GRLEnabled = Boolean.parseBoolean(EnvVars.get("GLOBAL_RATE_LIMIT_ENABLED", false));
        if (GRLEnabled == false)
        {
            log.debug("GRL is disabled");
            return Optional.empty();

        }
        var rlfServiceError = CommonConfigUtils.getSvcNotFoundErrorAction();

        var rateLimitFilter = new ProxyGlobalRateLimitFilter(Namespace.SEPP,
                                                             500,
                                                             CommonConfigUtils.getGrlPriorityPercentages(),
                                                             Egress.RLF_SERVICE_CLUSTER_NAME,
                                                             rlfServiceError);

        if (network instanceof OwnNetwork)
        {
            if (network.getGlobalIngressRateLimitProfileRef().isEmpty())
            {
                log.debug("network has no GLRef");
                return Optional.empty();
            }
            log.debug("GLRef found on network");
            setNetworkforRateLimitFilter(rateLimitFilter, RateLimitedEntity.OWN_NETWORK);
        }
        else // external-network
        {
            var rps = ConfigUtils.getAllRoamingPartners(seppInst.getExternalNetwork());
            var roamingPartnersWithRL = rps.stream().filter(rp -> !rp.getGlobalIngressRateLimitProfileRef().isEmpty()).collect(Collectors.toList());
            if (!network.getGlobalIngressRateLimitProfileRef().isEmpty())
            {
                log.debug("GLRef found on Ext-network");
                setNetworkforRateLimitFilter(rateLimitFilter, RateLimitedEntity.EXTERNAL_NETWORK);
            }
            else if (!roamingPartnersWithRL.isEmpty()) // Roaming partner limit
            {
                // check if RP has nonTls then return an empty filter
                if (!isTls)
                {
                    return Optional.empty();
                }
                log.debug("GLRef found on Roaming-partner");
                ProxyRateLimitActionProfile actionProfile;
                var kvTable = new HashMap<String, Pair<String, Optional<Pair<String, ProxyRateLimitActionProfile>>>>();
                for (var rp : rps)
                {
                    var gRLRefs = Utils.getListByNames(seppInst.getGlobalRateLimitProfile(), rp.getGlobalIngressRateLimitProfileRef());
                    var rpName = rp.getName();
                    Optional<Pair<String, ProxyRateLimitActionProfile>> bucketPair = Optional.empty();
                    var mapEntry = Pair.of(rpName, bucketPair);
                    for (var gRLRef : gRLRefs)
                    {
                        log.debug("GLRef found on Roaming-partner");
                        var actionRejectMsg = gRLRef.getActionRejectMessage();
                        if (actionRejectMsg == null)// then we have getActionDropMessage
                        {
                            actionProfile = new ProxyRateLimitActionProfile(Type.DROP);

                        }
                        else
                        {
                            actionProfile = new ProxyRateLimitActionProfile(Type.REJECT,
                                                                            actionRejectMsg.getStatus(),
                                                                            Optional.ofNullable(actionRejectMsg.getTitle()),
                                                                            Optional.ofNullable(actionRejectMsg.getDetail()),
                                                                            Optional.ofNullable(actionRejectMsg.getCause()),
                                                                            actionRejectMsg.getFormat().toString(),
                                                                            actionRejectMsg.getRetryAfterHeader().toString());
                        }
                        var bucketName = CommonConfigUtils.RateLimiting.createBucketName(RateLimitedEntity.ROAMING_PARTNER,
                                                                                         rp.getName(),
                                                                                         RateLimitType.INGRESS,
                                                                                         gRLRef.getName());
                        mapEntry = Pair.of(rpName, Optional.of(Pair.of(bucketName, actionProfile)));
                    }
                    for (var domainName : rp.getDomainName())
                    {
                        kvTable.put(domainName, mapEntry);
                    }

                }
                log.debug("RP HashMap:{}", kvTable.toString());
                rateLimitFilter.setRoamingPartners(kvTable);

            }
            else // no limit
            {
                log.debug("Ext-network has no GLRef");
                return Optional.empty();
            }
        }

        return Optional.of(rateLimitFilter);
    }

    private void setNetworkforRateLimitFilter(ProxyGlobalRateLimitFilter rateLimitFilter,
                                              RateLimitedEntity rlEntity)
    {
        var gRLRefs = Utils.getListByNames(seppInst.getGlobalRateLimitProfile(), network.getGlobalIngressRateLimitProfileRef());
        ProxyRateLimitActionProfile actionProfile;
        for (var gRLRef : gRLRefs)
        {
            var actionRejectMsg = gRLRef.getActionRejectMessage();
            if (actionRejectMsg == null)// then we have getActionDropMessage
            {
                actionProfile = new ProxyRateLimitActionProfile(Type.DROP);

            }
            else
            {
                actionProfile = new ProxyRateLimitActionProfile(Type.REJECT,
                                                                actionRejectMsg.getStatus(),
                                                                Optional.ofNullable(actionRejectMsg.getTitle()),
                                                                Optional.ofNullable(actionRejectMsg.getDetail()),
                                                                Optional.ofNullable(actionRejectMsg.getCause()),
                                                                actionRejectMsg.getFormat().toString(),
                                                                actionRejectMsg.getRetryAfterHeader().toString());
            }
            var bucketName = CommonConfigUtils.RateLimiting.createBucketName(rlEntity, network.getName(), RateLimitType.INGRESS, gRLRef.getName());
            log.debug("Network Bucket name:{}", bucketName);
            rateLimitFilter.setNetwork(Optional.of(Pair.of(bucketName, actionProfile)));
        }
    }

}
