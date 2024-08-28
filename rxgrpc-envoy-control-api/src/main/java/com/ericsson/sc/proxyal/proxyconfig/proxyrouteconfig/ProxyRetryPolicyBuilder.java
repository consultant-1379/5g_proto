package com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig;

import java.util.Objects;

import com.google.protobuf.Any;
import com.google.protobuf.Duration;
import com.google.protobuf.UInt32Value;

import io.envoyproxy.envoy.config.route.v3.RetryPolicy;
import io.envoyproxy.envoy.config.route.v3.RetryPolicy.Builder;
import io.envoyproxy.envoy.config.route.v3.RetryPolicy.RetryHostPredicate;
import io.envoyproxy.envoy.config.route.v3.RetryPolicy.RetryPriority;
import io.envoyproxy.envoy.extensions.retry.host.eric_loop_prevention.v3.EricLoopPreventionConfig;
import io.envoyproxy.envoy.extensions.retry.host.eric_omit_host_metadata_dynamic.v3.EricOmitHostMetadataDynamicConfigProto;
import io.envoyproxy.envoy.extensions.retry.host.eric_omit_host_metadata_dynamic.v3.OmitHostDynamicMetadataConfig;
import io.envoyproxy.envoy.extensions.retry.host.previous_hosts.v3.PreviousHostsPredicate;
import io.envoyproxy.envoy.extensions.retry.priority.eric_reselect_priorities.v3.EricReselectPrioritiesConfig;

public class ProxyRetryPolicyBuilder
{

    public final String LOOP_PREVENTION = "envoy.retry_host_predicates.eric_loop_prevention_config";

    private ProxyRoute proxyRoute;

    public ProxyRetryPolicyBuilder(ProxyRoute proxyRoute)
    {
        this.proxyRoute = proxyRoute;
    }

    public ProxyRetryPolicyBuilder(ProxyRetryPolicyBuilder that)
    {
        this.proxyRoute = new ProxyRoute(that.getProxyRoute());
    }

    public ProxyRoute getProxyRoute()
    {
        return proxyRoute;
    }

    public void setProxyRoute(ProxyRoute proxyRoute)
    {
        this.proxyRoute = proxyRoute;
    }

    public ProxyRetryPolicyBuilder withProxyRoute(ProxyRoute proxyRoute)
    {
        this.proxyRoute = proxyRoute;
        return this;
    }

    public Builder initBuilder()
    {

        var pxRetryPolicy = proxyRoute.getRetryPolicy();
        int timeoutSeconds = (int) Math.floor((pxRetryPolicy.getPerTryTimeoutSeconds()));
        int timeoutNanos = (int) (1E9 * (pxRetryPolicy.getPerTryTimeoutSeconds() - timeoutSeconds));

        // RetryPolicy:
        // Reselects for Envoy is always as in our model. But Retries is calculated
        // specifically for each case.
        var retries = pxRetryPolicy.getNumRetries();
        var reselects = pxRetryPolicy.getHostSelectionRetryMaxAttempts();
        var retryHostPredicate = pxRetryPolicy.getRetryHostPredicate();
        var retryOn = pxRetryPolicy.getRetryOn();
        var retryPriority = pxRetryPolicy.getRetryPriority();
        var tempBlocking = pxRetryPolicy.isSupportTemporaryBlocking();
        var loopPrevention = pxRetryPolicy.isSupportLoopPrevention();

        var lastResortReselects = pxRetryPolicy.getLastResortReselects();
        var failoverReselects = pxRetryPolicy.getFailoverReselects();
        var preferredHostRetries = pxRetryPolicy.getPreferredHostRetries();

        var retryPolicyBuilder = RetryPolicy.newBuilder()
                                            .setNumRetries(UInt32Value.of(retries))
                                            .setHostSelectionRetryMaxAttempts(reselects)
                                            .setPerTryTimeout(Duration.newBuilder() //
                                                                      .setSeconds(timeoutSeconds) //
                                                                      .setNanos(timeoutNanos) //
                                                                      .build());

        if (retryHostPredicate != null && !retryHostPredicate.isEmpty())
        {

            retryPolicyBuilder.addRetryHostPredicate(RetryHostPredicate.newBuilder()
                                                                       .setName(retryHostPredicate)
                                                                       .setTypedConfig(Any.pack(PreviousHostsPredicate.newBuilder().build()))
                                                                       .build());
        }

        if ((retryOn != null) && (!retryOn.isEmpty()))
        {
            retryPolicyBuilder.setRetryOn(retryOn);
        }

        if ((pxRetryPolicy.getRetriableStatusCodes() != null) && (!pxRetryPolicy.getRetriableStatusCodes().isEmpty()))
        {
            retryPolicyBuilder.addAllRetriableStatusCodes(pxRetryPolicy.getRetriableStatusCodes());
        }

        if ((retryPriority != null) && (!retryPriority.isEmpty()))
        {
            var ericReselectPriorities = EricReselectPrioritiesConfig.newBuilder();

            ericReselectPriorities.setSupportTemporaryBlocking(tempBlocking);
            if (loopPrevention)
            {
                ericReselectPriorities.setSupportLoopPrevention(loopPrevention);

                retryPolicyBuilder.addRetryHostPredicate(RetryHostPredicate.newBuilder()
                                                                           .setName(LOOP_PREVENTION)
                                                                           .setTypedConfig(Any.pack(EricLoopPreventionConfig.newBuilder().build()))
                                                                           .build());

            }

            ericReselectPriorities.setLastResortReselects(lastResortReselects)
                                  .setFailoverReselects(failoverReselects)
                                  .setPreferredHostRetries(preferredHostRetries);

            var ericReselectPrioritiesAny = Any.pack(ericReselectPriorities.build());
            retryPolicyBuilder.setRetryPriority(RetryPriority.newBuilder().setName(retryPriority).setTypedConfig(ericReselectPrioritiesAny).build());
        }
        return retryPolicyBuilder;

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ProxyRetryPolicyBuilder [proxyRoute=" + proxyRoute + "]";
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(proxyRoute);
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
        ProxyRetryPolicyBuilder other = (ProxyRetryPolicyBuilder) obj;
        return Objects.equals(proxyRoute, other.proxyRoute);
    }

}
