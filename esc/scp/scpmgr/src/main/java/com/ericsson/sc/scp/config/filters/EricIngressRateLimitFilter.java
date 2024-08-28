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
 * Created on: Mar 15, 2022
 *     Author: eodnouk
 */

package com.ericsson.sc.scp.config.filters;

import java.util.Optional;

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
import com.ericsson.sc.scp.config.Egress;
import com.ericsson.sc.scp.model.NfInstance;
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
    private final NfInstance scpInst;
    private final IfNetwork network;

    public EricIngressRateLimitFilter(NfInstance scpInstance,
                                      IfNetwork network)
    {
        this.scpInst = scpInstance;
        this.network = network;
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

        var rateLimitFilter = new ProxyGlobalRateLimitFilter(Namespace.SCP,
                                                             500,
                                                             CommonConfigUtils.getGrlPriorityPercentages(),
                                                             Egress.RLF_SERVICE_CLUSTER_NAME,
                                                             rlfServiceError);

        if (network.getGlobalIngressRateLimitProfileRef().isEmpty())
        {
            log.debug("network has no GLRef");
            return Optional.empty();
        }
        log.debug("GLRef found on network");
        setNetworkforRateLimitFilter(rateLimitFilter, RateLimitedEntity.OWN_NETWORK);

        return Optional.of(rateLimitFilter);
    }

    private void setNetworkforRateLimitFilter(ProxyGlobalRateLimitFilter rateLimitFilter,
                                              RateLimitedEntity rlEntity)
    {
        var gRLRefs = Utils.getListByNames(scpInst.getGlobalRateLimitProfile(), network.getGlobalIngressRateLimitProfileRef());
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
            log.debug("Network Backet name:{}", bucketName);
            rateLimitFilter.setNetwork(Optional.of(Pair.of(bucketName, actionProfile)));
            rateLimitFilter.setRoamingPartners(null);
        }
    }

}
