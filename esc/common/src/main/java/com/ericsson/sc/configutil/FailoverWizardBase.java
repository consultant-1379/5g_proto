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
 * Created on: Feb 15, 2022
 *     Author: eaoknkr
 */

package com.ericsson.sc.configutil;

import java.util.Optional;

import com.ericsson.sc.glue.IfFailoverProfile;
import com.ericsson.sc.glue.IfRetryCondition;
import com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig.ProxyRetryPolicy;
import com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig.ProxyRetryPolicyBuilder;

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.RoutingBehaviour;

/**
 * The FailoverWizard collects all the needed failover information and after
 * doing some sorcery, it calculates the ProxyRetryPolicy parameters depending
 * on the routingBehaviour of the route. Please treat the wizard carefully, he's
 * very sensitive to goblins.
 */
public class FailoverWizardBase<T extends IfFailoverProfile>
{
    protected final T failoverProfile;

    public FailoverWizardBase(T failoverProfile)
    {
        this.failoverProfile = failoverProfile;
    }

    /**
     * Returns the retryPolicy of the route based on the provided routing behavior
     * 
     * @param routingType the route's routing behavior
     * 
     * @return ProxyRetryPolicy which encaptulates all the necessary retry params
     *         and needed host and priority retry predicates
     */
    public ProxyRetryPolicy transform(Optional<RoutingBehaviour> routingType)
    {
        var retryPolicy = this.getBasicRetryPolicy(Optional.ofNullable(this.failoverProfile.getName()),
                                                   this.failoverProfile.getRetryCondition(),
                                                   Double.valueOf(this.failoverProfile.getTargetTimeout() / 1000.0),
                                                   Double.valueOf(this.failoverProfile.getRequestTimeBudget() / 1000.0));
        var lrPoolReselects = this.failoverProfile.getLastResortNfPoolReselectsMax();
        var targetPoolReselects = this.failoverProfile.getTargetNfPoolReselectsMax();
        var prefHostRetries = this.failoverProfile.getPreferredHostRetriesMax();
        if (!routingType.isEmpty())
        {
            if (routingType.get().equals(RoutingBehaviour.ROUND_ROBIN))
            {
                prefHostRetries = 0;
                retryPolicy.setRetryHostPredicate(retryPolicy.PREVIOUS_HOSTS);
            }
            else if (routingType.get().equals(RoutingBehaviour.PREFERRED))
            {
                retryPolicy.setRetryHostPredicate(retryPolicy.PREVIOUS_HOSTS);
            }
            else if (routingType.get().equals(RoutingBehaviour.STRICT))
            {
                lrPoolReselects = 0;
                targetPoolReselects = 0;
            }
            else if (routingType.get().equals(RoutingBehaviour.REMOTE_PREFERRED)) // eric_reselect_priorities is not used for remoted preferred and remote RR
                                                                                  // routing scenarios
            {
                retryPolicy.setNumRetries(this.failoverProfile.getTargetNfPoolReselectsMax() + this.failoverProfile.getPreferredHostRetriesMax());
                return retryPolicy;
            }
            else if (routingType.get().equals(RoutingBehaviour.REMOTE_ROUND_ROBIN))
            {
                retryPolicy.setNumRetries(this.failoverProfile.getTargetNfPoolReselectsMax());
                return retryPolicy;
            }
            else if (routingType.get().equals(RoutingBehaviour.STRICT_DFP))
            {
                retryPolicy.setNumRetries(this.failoverProfile.getPreferredHostRetriesMax());
                return retryPolicy;
            }
        }
        retryPolicy.setLastResortReselects(lrPoolReselects);
        retryPolicy.setFailoverReselects(targetPoolReselects);
        retryPolicy.setPreferredHostRetries(prefHostRetries);

        var numOfRetries = lrPoolReselects + targetPoolReselects + prefHostRetries;
        retryPolicy.setNumRetries(numOfRetries);

        retryPolicy.setRetryPriority(retryPolicy.ERIC_RESELECT_PRIORITIES);
        return retryPolicy;
    }

    /**
     * Get retryPolicy for a given failover-profile, i.e. translate a
     * failover-profile into an Envoy retryPolicy.
     */
    ProxyRetryPolicy getBasicRetryPolicy(Optional<String> name,
                                         IfRetryCondition retryCond,
                                         Double perTryTimeoutSeconds,
                                         Double requestTimeoutSeconds)
    {
        var retryOn = new StringBuilder();

        var httpStatus = retryCond.getHttpStatus();
        if (!httpStatus.isEmpty())
        {
            retryOn.append("retriable-status-codes");
        }
        var connectFailure = retryCond.getConnectFailure();
        if (Boolean.TRUE.equals(connectFailure))
        {
            retryOn.append(",connect-failure");
        }
        var refusedStream = retryCond.getRefusedStream();
        if (Boolean.TRUE.equals(refusedStream))
        {
            retryOn.append(",refused-stream");
        }
        var reset = retryCond.getReset();
        if (Boolean.TRUE.equals(reset))
        {
            retryOn.append(",reset");
        }

        var retryPolicy = new ProxyRetryPolicy(name, retryOn.toString(), perTryTimeoutSeconds, requestTimeoutSeconds);

        if (!httpStatus.isEmpty())
        {
            retryPolicy.setRetriableStatusCodes(httpStatus);
        }
        return retryPolicy;
    }

}
