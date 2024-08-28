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
 * Created on: Dec 18, 2020
 *     Author: eaoknkr
 */

package com.ericsson.sc.configutil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.glue.IfActionRouteBase;
import com.ericsson.sc.glue.IfActionRouteTarget;
import com.ericsson.sc.glue.IfAddResponseHeader;
import com.ericsson.sc.glue.IfDiscoveredNfInstance;
import com.ericsson.sc.glue.IfEgressConnectionProfile;
import com.ericsson.sc.glue.IfFailoverProfile;
import com.ericsson.sc.glue.IfGenericNfInstance;
import com.ericsson.sc.glue.IfIngressConnectionProfile;
import com.ericsson.sc.glue.IfLocalRateLimitProfile;
import com.ericsson.sc.glue.IfNetwork;
import com.ericsson.sc.glue.IfNfFunction;
import com.ericsson.sc.glue.IfNfInstance;
import com.ericsson.sc.glue.IfNfPool;
import com.ericsson.sc.glue.IfPriorityGroup;
import com.ericsson.sc.glue.IfRoutingAction;
import com.ericsson.sc.glue.IfServiceAddress;
import com.ericsson.sc.glue.IfStaticNfInstanceDatum;
import com.ericsson.sc.glue.IfStaticScpInstanceDatum;
import com.ericsson.sc.glue.IfStaticSeppInstanceDatum;
import com.ericsson.sc.glue.IfTypedNfAddressProperties;
import com.ericsson.sc.glue.IfTypedNfInstance;
import com.ericsson.sc.glue.IfTypedNfService;
import com.ericsson.sc.glue.IfTypedScpInstance;
import com.ericsson.sc.glue.IfVtapIngress;
import com.ericsson.sc.nfm.model.ExpectedResponseHttpStatusCode;
import com.ericsson.sc.nfm.model.Scheme;
import com.ericsson.sc.proxy.ConfigHelper;
import com.ericsson.sc.proxyal.proxyconfig.ProxyTcpKeepalive;
import com.ericsson.sc.proxyal.proxyconfig.ProxyVtapSettings;
import com.ericsson.sc.proxyal.proxyconfig.proxyactionconfig.ProxyActionAddHeader;
import com.ericsson.sc.proxyal.proxyconfig.proxyactionconfig.ProxyActionLog;
import com.ericsson.sc.proxyal.proxyconfig.proxyactionconfig.ProxyActionRejectMessage;
import com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig.ProxyActiveHealthCheck;
import com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig.ProxyCircuitBreaker;
import com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig.ProxyCluster;
import com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig.ProxyEjectionPolicy;
import com.ericsson.sc.proxyal.proxyconfig.proxyendpointconfig.ProxyEndpoint;
import com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig.ProxyLocalRateLimiter;
import com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig.ProxyRateLimitActionProfile;
import com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig.ProxyRateLimitActionProfile.Type;
import com.ericsson.sc.proxyal.proxyconfig.proxylistenerconfig.ProxyListener;
import com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig.ProxyRoute;
import com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig.ProxyVirtualHost;
import com.ericsson.sc.proxyal.proxyconfig.proxyroutingconfig.ProxyRoutingAction;
import com.ericsson.sc.sepp.model.ExternalNetwork;
import com.ericsson.sc.sepp.model.FirewallProfile;
import com.ericsson.sc.sepp.model.NfInstance;
import com.ericsson.sc.utilities.dns.IpFamily;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.common.IP_VERSION;
import com.ericsson.utilities.common.IfNamedListItem;
import com.ericsson.utilities.common.Rdn;
import com.ericsson.utilities.common.RuntimeEnvironment;
import com.ericsson.utilities.common.Utils;
import com.ericsson.utilities.exceptions.BadConfigurationException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.IfExists;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.LogValue;
import io.envoyproxy.envoy.type.v3.Int64Range;

/**
 * 
 */
public class CommonConfigUtils
{
    public static class RateLimiting
    {
        public enum RateLimitedEntity
        {
            EXTERNAL_NETWORK("ext"),
            OWN_NETWORK("own"),
            ROAMING_PARTNER("rp");

            private String value;

            RateLimitedEntity(String value)
            {
                this.value = value;
            }

            @JsonValue
            public String getValue()
            {
                return this.value;
            }

            @Override
            public String toString()
            {
                return this.value;
            }

            @JsonCreator
            public static RateLimitedEntity fromValue(String value)
            {
                for (RateLimitedEntity b : RateLimitedEntity.values())
                {
                    if (b.value.equals(value))
                        return b;
                }

                throw new IllegalArgumentException("Unexpected value '" + value + "'");
            }
        }

        public enum RateLimitType
        {
            EGRESS("egress"),
            INGRESS("ingress");

            private String value;

            RateLimitType(String value)
            {
                this.value = value;
            }

            @JsonValue
            public String getValue()
            {
                return this.value;
            }

            @Override
            public String toString()
            {
                return this.value;
            }

            @JsonCreator
            public static RateLimitType fromValue(String value)
            {
                for (RateLimitType b : RateLimitType.values())
                {
                    if (b.value.equals(value))
                        return b;
                }

                throw new IllegalArgumentException("Unexpected value '" + value + "'");
            }
        }

        private RateLimiting()
        {
        }

        /**
         * Creates a bucket name.
         * 
         * @param entityKind
         * @param entityName
         * @param limitKind
         * @param limitName
         * 
         * @return The bucket name
         */
        public static String createBucketName(final RateLimitedEntity entityKind,
                                              final String entityName,
                                              final RateLimitType limitKind,
                                              final String limitName)
        {
            return new Rdn(entityKind.getValue(), entityName).add(limitKind.getValue(), limitName).toString().replace(",", ".");
        }
    }

    private static final Logger log = LoggerFactory.getLogger(CommonConfigUtils.class);

    public static final String HEADER_HOST = "x-host";
    public static final String HEADER_CLUSTER = "x-cluster";
    public static final String METADATA_HOST = "host";
    public static final String METADATA_POOL = "pool";
    public static final String METADATA_SEPP_TYPE = "type";

    public static final String TARGET_API_ROOT_HEADER = "3gpp-Sbi-target-apiRoot";
    public static final String AUTHORITY_HEADER = ":authority";
    public static final String HEADER_FAILOVER_PROFILE = "x-eric-fop";

    private static final String CLUSTER_NAME_SUFFIX_PATTERN = "#!_#%s:%s";
    private static final String LAST_RESORT_POOL_TYPE = "LRP";

    public static final String TLS = "tls";

    private CommonConfigUtils()
    {
    }

    public static Set<IpFamily> getDefaultIpFamilies(final IfGenericNfInstance nfInstance)
    {
        final Set<IpFamily> ipFamilies = new HashSet<>();

        nfInstance.getServiceAddress().forEach(address ->
        {
            if (address.getIpv4Address() != null)
                ipFamilies.add(IpFamily.IPV4);

            if (address.getIpv6Address() != null)
                ipFamilies.add(IpFamily.IPV6);
        });

        if (ipFamilies.isEmpty())
        {
            ipFamilies.addAll(IP_VERSION.toIpFamilies(RuntimeEnvironment.getDeployedIpVersion()));
        }

        log.debug("ipFamilies={}", ipFamilies);

        return ipFamilies;
    }

    public static String normalizeExtractorRegexp(String extractorRegexp)
    {
        // we need to double backslashes, so that the user has to enter single
        // backslashes
        return extractorRegexp.replace("\\", "\\\\");
    }

    public static boolean isRoundRobinRule(IfRoutingAction rr)
    {
        return rr.getActionRouteRoundRobin() != null;
    }

    public static boolean isPreferredRoutingRule(IfRoutingAction rr)
    {
        return rr.getActionRoutePreferred() != null;
    }

    public static boolean isStrictRoutingRule(IfRoutingAction rr)
    {
        return rr.getActionRouteStrict() != null;
    }

    public static boolean isOtherRoutingRule(IfRoutingAction rr)
    {
        return rr.getActionRejectMessage() != null || rr.getActionLog() != null || rr.getActionDropMessage() != null;
    }

    public static List<IfTypedNfInstance> getScpInstances(IfNfPool pool,
                                                          IfNfInstance seppInst)
    {
        return Utils.streamIfExists(pool.getStaticScpInstanceDataRef())
                    .<IfStaticScpInstanceDatum>map(instanceRef -> Utils.getByName(seppInst.getStaticScpInstanceData(), instanceRef))
                    .filter(Objects::nonNull)
                    .<IfTypedNfInstance>flatMap(datum -> Utils.streamIfExists(datum.getStaticScpInstance()))
                    .toList();
    }

    public static List<IfTypedNfInstance> getScpInstances(IfPriorityGroup subpool,
                                                          IfNfInstance seppInst)
    {
        return Utils.streamIfExists(subpool.getStaticScpInstanceDataRef())
                    .<IfStaticScpInstanceDatum>map(instanceRef -> Utils.getByName(seppInst.getStaticScpInstanceData(), instanceRef))
                    .filter(Objects::nonNull)
                    .flatMap(datum -> datum.getStaticScpInstance().stream())
                    .toList();
    }

    /**
     * Add a list of routes to the list of routers per VHost.
     * 
     * @param vHostName          The VHost to which the route shall be added
     * @param routes             The routes to add
     * @param routesPerVHostName
     */
    public static void addRoutesUnderVHost(String vHostName,
                                           List<ProxyRoute> routes,
                                           Map<String, Set<ProxyRoute>> routesPerVHostName)
    {
        var existingRoutes = routesPerVHostName.getOrDefault(vHostName, new HashSet<>());
        existingRoutes.addAll(routes);
        routesPerVHostName.put(vHostName, existingRoutes);
    }

    /**
     * Try to get the failover-profile settings for a given routing-action. If the
     * referenced failover-profile does not exist, the returned Optional is empty.
     * 
     * @param failoverProfileName for which the failover-profile shall be fetched
     * @param failoverProfiles
     * @return Optional failoverBehavior
     */
    public static final <T extends IfFailoverProfile> Optional<T> tryToGetFailoverProfile(String failoverProfileName,
                                                                                          List<T> failoverProfiles)
    {
        return failoverProfiles.stream().filter(profile -> profile.getName().equals(failoverProfileName)).findFirst();
    }

    /**
     * Return true if (mutual)Tls shall be enabled for the pool(~cluster)
     * 
     * @param pxEndpoints
     * @return true if (mutula)tls shall be enabled. Currently always returns false
     *         for dynamic pools for now
     */
    public static boolean isMutualTlsEnabledForCluster(List<ProxyEndpoint> pxEndpoints)
    {
        return pxEndpoints.stream().anyMatch(ProxyEndpoint::isHttps);
    }

    /**
     * Sets Temporary Blocking to a cluster, if the Temporary blocking values are
     * set on the FailoverBehavior.
     * 
     * @param pool    The pool to search for the FailoverBehavior
     * @param cluster The cluster to which we set Temporary Blocking
     */
    public static void setTempBlockingForCluster(IfNfPool pool,
                                                 ProxyCluster cluster)
    {
        if (pool.getTemporaryBlocking() != null)
        {
            var ep = new ProxyEjectionPolicy(pool.getTemporaryBlocking().getConsecutiveFailures(),
                                             pool.getTemporaryBlocking().getConsecutiveFailures(),
                                             pool.getTemporaryBlocking().getBlockingTime());
            if (pool.getTemporaryBlocking().getConsecutiveGatewayFailures() != null)
            {
                ep.setEnforceConsecutiveGatewayFailure(100);
                ep.setOutlierConsecutiveGatewayFailure(pool.getTemporaryBlocking().getConsecutiveGatewayFailures());
            }
            if (pool.getTemporaryBlocking().getConsecutiveLocalFailures() != null)
            {
                ep.setSplitExternalLocalOriginErrors(true);
                ep.setConsecutiveLocalOriginFailure(pool.getTemporaryBlocking().getConsecutiveLocalFailures());
                if (pool.getTemporaryBlocking().getConsecutiveLocalFailures() == 0)
                {
                    ep.setEnforcingConsecutiveLocalOriginFailure(0);

                }
            }
            cluster.setEjectionPolicy(ep);
        }
    }

    /*
     * Based on yang model ,codeRanges list can contain an integer range if
     * min-http-status-code and max-http-status-code are set Envoy can set multiple
     * Int64Range elements as a list of expected response http status code The
     * ranges are handled in envoy as semi closed, but the decision is to handle
     * them as closed in yang model This is the reason why in this method the upper
     * limit is increased by one
     */
    public static List<Int64Range> convertExpectedCodeRangeToIntRange(List<ExpectedResponseHttpStatusCode> codeRanges)
    {
        if (codeRanges != null && !codeRanges.isEmpty())
        {
            List<Int64Range> list = new ArrayList<Int64Range>();
            codeRanges.stream().forEach(range ->
            {

                list.add(Int64Range.newBuilder().setStart(range.getMinHttpStatusCode()).setEnd(range.getMaxHttpStatusCode() + 1).build());

            });
            return list;
        }

        return null;

    }

    /**
     * Sets Active health Check to a cluster, if the Active Health Check values are
     * set on the Nf-pool.
     * 
     * @param pool    The pool to search for the Active Health Check
     * @param cluster The cluster to which we set Active Health Checking
     */
    public static void setActiveHealthCheckForCluster(IfNfInstance instance,
                                                      IfNfPool pool,
                                                      ProxyCluster cluster)
    {
        if (pool.getActiveHealthCheck() != null)
        {

            var expectedCodeRanges = pool.getActiveHealthCheck().getExpectedResponseHttpStatusCode();
            var ah = new ProxyActiveHealthCheck(pool.getActiveHealthCheck().getTimeInterval(),
                                                pool.getActiveHealthCheck().getNoTrafficTimeInterval(),
                                                pool.getActiveHealthCheck().getTimeout(),
                                                pool.getActiveHealthCheck().getUnhealthyThreshold(),
                                                pool.getActiveHealthCheck().getHttpMethod().toString(),
                                                pool.getActiveHealthCheck().getHttpPath(),
                                                Optional.ofNullable(convertExpectedCodeRangeToIntRange(expectedCodeRanges)));

            cluster.setActiveHealthCheck(ah);
        }

    }

    /**
     * Sets Connection Timeout for new network connections to hosts in the cluster.
     * The default value is 2s.
     * 
     * @param cluster
     * @param egressConnectionProfile
     */
    public static void setTcpConnectTimeout(ProxyCluster cluster,
                                            IfEgressConnectionProfile egressConnectionProfile)
    {

        if (egressConnectionProfile != null && egressConnectionProfile.getTcpConnectTimeout() != null)
        {
            cluster.setConnectTimeout(egressConnectionProfile.getTcpConnectTimeout().doubleValue());
        }
    }

    /**
     * Sets Max Connection Duration to a listener, if the maximum connection
     * duration is not set the default value is 0 meaning it has no limit
     * 
     * @param listener
     * @param ingressConnectionProfile
     */
    public static void setMaxConnectionDuration(ProxyListener listener,
                                                IfIngressConnectionProfile ingressConnectionProfile)
    {

        if (ingressConnectionProfile.getMaxConnectionDuration() != null)
        {
            listener.setMaxConnectionDuration(ingressConnectionProfile.getMaxConnectionDuration());
        }
    }

    /**
     * Sets Max Connection Duration to a cluster, if the maximum connection duration
     * is not set the default value is 0 meaning it has no limit
     * 
     * @param cluster
     * @param egressConnectionProfile
     */
    public static void setMaxConnectionDuration(ProxyCluster cluster,
                                                IfEgressConnectionProfile egressConnectionProfile)
    {

        if (egressConnectionProfile != null && egressConnectionProfile.getMaxConnectionDuration() != null)
        {
            cluster.setMaxConnectionDuration(egressConnectionProfile.getMaxConnectionDuration());
        }
    }

    /**
     * Sets Max Concurrent Streams to a listener, if the maximum concurrent streams
     * is not set the default value is 2147483647
     * 
     * @param listener
     * @param ingressConnectionProfile
     */
    public static void setMaxConcurrentStreams(ProxyListener listener,
                                               IfIngressConnectionProfile ingressConnectionProfile)
    {
        if (ingressConnectionProfile != null && ingressConnectionProfile.getMaxConcurrentStreams() != null)
        {
            listener.setMaxConcurrentStreams(ingressConnectionProfile.getMaxConcurrentStreams());
        }
    }

    /**
     * Sets Max Concurrent Streams to a cluster, if the maximum concurrent streams
     * is not set the default value is 2147483647
     * 
     * @param cluster
     * @param egressConnectionProfile
     */
    public static void setMaxConcurrentStreams(ProxyCluster cluster,
                                               IfEgressConnectionProfile egressConnectionProfile)
    {

        if (egressConnectionProfile != null && egressConnectionProfile.getMaxConcurrentStreams() != null)
        {
            cluster.setMaxConcurrentStreams(egressConnectionProfile.getMaxConcurrentStreams());
        }
    }

    /**
     * Sets hpack-table-size to a listener. This value is used by Envoy for the HTTP
     * header compression
     * 
     * @param listener
     * @param ingressConnectionProfile
     */
    public static void setHpackTableSize(ProxyListener listener,
                                         IfIngressConnectionProfile ingressConnectionProfile)
    {
        if (ingressConnectionProfile.getHpackTableSize() != null)
        {
            listener.setHpackTableSize(Optional.ofNullable(ingressConnectionProfile.getHpackTableSize()));
        }

    }

    /**
     * Sets hpack-table-size to a cluster. This value is used by Envoy for the HTTP
     * header compression
     * 
     * @param cluster
     * @param egressConnectionProfile
     */
    public static void setHpackTableSize(ProxyCluster cluster,
                                         IfEgressConnectionProfile egressConnectionProfile)
    {
        if (egressConnectionProfile != null && egressConnectionProfile.getHpackTableSize() != null)
        {
            cluster.setHpackTableSize(Optional.ofNullable(egressConnectionProfile.getHpackTableSize()));
        }

    }

    /**
     * Sets TCP keepalive to a cluster
     * 
     * @param cluster
     * @param egressConnectionProfile
     */
    public static void setTcpKeepalive(ProxyCluster cluster,
                                       IfEgressConnectionProfile egressConnectionProfile)
    {
        if (egressConnectionProfile != null && egressConnectionProfile.getTcpKeepalive() != null)
        {
            cluster.setTcpKeepalive(new ProxyTcpKeepalive(egressConnectionProfile.getTcpKeepalive().getProbes(),
                                                          egressConnectionProfile.getTcpKeepalive().getTime(),
                                                          egressConnectionProfile.getTcpKeepalive().getInterval()));
        }

    }

    /**
     * Sets http connection idletimeout to a cluster
     * 
     * @param cluster
     * @param egressConnectionProfile
     */
    public static void setIdleTimeout(ProxyCluster cluster,
                                      IfEgressConnectionProfile egressConnectionProfile)
    {
        if (egressConnectionProfile != null && egressConnectionProfile.getConnectionIdleTimeout() != null)
        {
            cluster.setIdleTimeout(egressConnectionProfile.getConnectionIdleTimeout());
        }

    }

    public static void setTrackClusterStats(ProxyCluster cluster,
                                            IfEgressConnectionProfile egressConnectionProfile)
    {
        if (egressConnectionProfile != null && egressConnectionProfile.getEnableMessageSizeMeasurement() != null)
        {
            cluster.setTrackClusterStats(egressConnectionProfile.getEnableMessageSizeMeasurement());
        }

    }

    /**
     * Sets dscp value to a cluster
     * 
     * @param cluster
     * @param egressConnectionProfile
     */
    public static void setDscpMarking(ProxyCluster cluster,
                                      IfEgressConnectionProfile egressConnectionProfile)
    {
        if (egressConnectionProfile != null)
        {
            cluster.setDscpMarking(egressConnectionProfile.getDscpMarking());
        }
    }

    /**
     * Sets TCP keepalive to a listener, if not configured in
     * ingressConnectionProfile, it's disabled (default behavior).
     * 
     * @param listener
     * @param ingressConnectionProfile
     */
    public static void setTcpKeepalive(ProxyListener listener,
                                       IfIngressConnectionProfile ingressConnectionProfile)
    {
        if (ingressConnectionProfile != null && ingressConnectionProfile.getTcpKeepalive() != null)
        {
            listener.setTcpKeepalive(new ProxyTcpKeepalive(ingressConnectionProfile.getTcpKeepalive().getProbes(),
                                                           ingressConnectionProfile.getTcpKeepalive().getTime(),
                                                           ingressConnectionProfile.getTcpKeepalive().getInterval()));
            listener.setTcpKeepaliveEnabled(true);
        }
        else
        {
            listener.setTcpKeepaliveEnabled(false);
        }

    }

    /**
     * Sets http connection idletimeout to a listener
     * 
     * @param listener
     * @param ingressConnectionProfile
     */
    public static void setIdleTimeout(ProxyListener listener,
                                      IfIngressConnectionProfile ingressConnectionProfile)
    {
        if (ingressConnectionProfile != null && ingressConnectionProfile.getConnectionIdleTimeout() != null)
        {
            listener.setIdleTimeout(ingressConnectionProfile.getConnectionIdleTimeout());
        }

    }

    /**
     * Sets pool retry budget to a cluster, if the pool-retry-budget parameters are
     * set on the FailoverBehavior.
     * 
     * @param pool    The pool to search for the FailoverBehavior
     * @param cluster The cluster to which we set pool-retry-budget
     */
    public static void setPoolRetryBudgetForCluster(IfNfPool pool,
                                                    ProxyCluster cluster)
    {
        if (pool.getPoolRetryBudget() != null)
        {
            cluster.setCircuitBreaker(new ProxyCircuitBreaker(pool.getPoolRetryBudget().getConcurrentRetriesPercentage(),
                                                              pool.getPoolRetryBudget().getMinConcurrentRetries()));

        }

    }

    /**
     * Sets circuit breaker to a cluster, if the circuit breaker parameters are set
     * on the EgressConnectionProfile.
     * 
     * @param cluster                 The cluster to which we set circuit breaker
     * @param egressConnectionProfile The egressConnectionProfile to search for the
     *                                FailoverBehavior
     */
    public static void setCircuitBreakerForCluster(ProxyCluster cluster,
                                                   IfEgressConnectionProfile egressConnectionProfile)
    {
        if (egressConnectionProfile != null)
        {
            cluster.getCircuitBreaker().setMaxConnections(egressConnectionProfile.getMaxConnections());
            cluster.getCircuitBreaker().setMaxRequests(egressConnectionProfile.getMaxRequests());
            cluster.getCircuitBreaker().setMaxPendingRequests(egressConnectionProfile.getMaxPendingRequests());
        }
    }

    public static boolean enableAccessLogging(ExternalNetwork network,
                                              NfInstance seppFunction)
    {

        if (network.getFirewallProfileRef() != null && checkReportEventEnablingByFirewallRef(network.getFirewallProfileRef(), seppFunction))
        {
            return true;
        }
        return network.getRoamingPartner()
                      .stream()
                      .filter(rp -> rp.getFirewallProfileRef() != null && checkReportEventEnablingByFirewallRef(rp.getFirewallProfileRef(), seppFunction))
                      .anyMatch(rp -> true);

    }

    public static boolean checkReportEventEnablingByFirewallRef(String firewallRef,
                                                                NfInstance seppFunction)
    {
        return seppFunction.getFirewallProfile().stream().filter(fwP -> fwP.getName().equals(firewallRef) && isReportEventEnabled(fwP)).anyMatch(fw -> true);
    }

    public static boolean isReportEventEnabled(FirewallProfile firewallProfile)
    {
        return isReportEventEnabledForRequest(firewallProfile) || isReportEventEnabledForResponse(firewallProfile);
    }

    public static boolean isReportEventEnabledForRequest(FirewallProfile firewallProfile)
    {
        return Stream.of(firewallProfile)

                     .flatMap(profile -> Stream.of(profile.getRequest()))
                     .filter(profile -> profile != null)
                     .flatMap(profile -> Stream.of(profile.getValidateServiceOperation(),

                                                   profile.getValidateMessageBodySize(),

                                                   profile.getValidateMessageHeaders(),

                                                   profile.getValidateMessageJsonBodyDepth(),

                                                   profile.getValidateMessageJsonBodyLeaves(),

                                                   profile.getValidateMessageJsonBodySyntax()))

                     .anyMatch(eventConfig -> eventConfig != null && eventConfig.getReportEvent() != null && eventConfig.getReportEvent());
    }

    public static boolean isReportEventEnabledForResponse(FirewallProfile firewallProfile)
    {
        return Stream.of(firewallProfile)

                     .flatMap(profile -> Stream.of(profile.getResponse()))
                     .filter(profile -> profile != null)
                     .flatMap(profile -> Stream.of(

                                                   profile.getValidateMessageBodySize(),

                                                   profile.getValidateMessageHeaders(),

                                                   profile.getValidateMessageJsonBodyDepth(),

                                                   profile.getValidateMessageJsonBodyLeaves(),

                                                   profile.getValidateMessageJsonBodySyntax()))

                     .anyMatch(eventConfig -> eventConfig != null && eventConfig.getReportEvent() != null && eventConfig.getReportEvent());
    }

    /**
     * Add one internal VHost (for C-SEPP purposes) to the listener. The domains are
     * taken from the FQDN if it exists, if not the IPv4-address, otherwise the
     * IPv6-address. Handling of default-ports (80 and 443) is done in RdsHelper.
     * 
     * @param svcAddr
     * @param listener
     */
    public static void addIntVHostToListener(IfServiceAddress svcAddr,
                                             ProxyListener listener)
    {
        var vHost = new ProxyVirtualHost(ServiceConfig.INT_SERVICE);

        // VHost domains are taken from FQDN if exists, otherwise IPv4, and IPv6 as last
        // resort.
        var port = Boolean.TRUE.equals(listener.getTls().isPresent()) ? svcAddr.getTlsPort() : svcAddr.getPort();

        if (svcAddr.getFqdn() != null && !svcAddr.getFqdn().isBlank())
        {
            vHost.addEndpoint(new ProxyEndpoint(svcAddr.getFqdn(), port));
        }
        if (svcAddr.getIpv4Address() != null && !svcAddr.getIpv4Address().isBlank())
        {
            vHost.addEndpoint(new ProxyEndpoint(svcAddr.getIpv4Address(), port));
        }
        if (svcAddr.getIpv6Address() != null && !svcAddr.getIpv6Address().isBlank())
        {
            vHost.addEndpoint(new ProxyEndpoint(svcAddr.getIpv6Address(), port));
        }
        // else
        // throw new BadConfigurationException("No IP adress found for service address
        // {} ", svcAddr);
        listener.addVirtualHost(vHost);
    }

    /**
     * Add one VHost to forward to the internal dynamic-forwarding proxy, with a
     * catch-call domain. This is needed because after the target-api-handling in
     * the LUA code, the authority-header can be anything and we need a VHost that
     * can handle that.
     * 
     * @param listener
     */
    public static void addIntForwardingVHostToListener(ProxyListener listener)
    {
        var vHost = new ProxyVirtualHost(ServiceConfig.INT_FORWARD_SERVICE);
        listener.addVirtualHost(vHost);
    }

    /**
     * Add one VHost to forward to the internal n32c proxy, with a catch-call
     * domain.
     * 
     * @param listener
     */
    public static void addIntN32cVHostToListener(ProxyListener listener)
    {
        var vHost = new ProxyVirtualHost(ServiceConfig.INT_N32C_SERVICE);
        listener.addVirtualHost(vHost);
    }

    /**
     * Returns the name of a ProxyListener based on the service-address it is
     * related to and if it's TLS or not.
     * 
     * This is not the name of the resulting envoy listener which will also contain
     * the port it listens to.
     * 
     * @param svcAddr The configured service address the listener corresponds to
     * @param isTls   If the listener is tls or not
     * 
     * @return String the ProxyListener's name
     */
    public static String getListenerName(IfServiceAddress svcAddr,
                                         boolean isTls)
    {
        String ipVersion = svcAddr.getIpv4Address() != null && !svcAddr.getIpv4Address().isEmpty() ? "_v4" : "";
        ipVersion += svcAddr.getIpv6Address() != null && !svcAddr.getIpv6Address().isEmpty() ? "_v6" : "";
        String serviceAddressName = svcAddr.getName();
        return isTls ? serviceAddressName + ipVersion + "_tls" : serviceAddressName + ipVersion;
    }

    /**
     * Returns a listener's name on the format that it's going to be configured in
     * envoy. Envoy Listener's name follows this convention: <serviceAddressName> +
     * <_ipVersion> + <_port> + [_tls]
     * 
     * > serviceAddressName: The name of the corresponding service address >
     * ipVersion: 'v4'/'v6' according to the ip of the service address > port: The
     * port the listener listens to > the _tls sufix is included if the listener
     * listens to a tls port
     * 
     * @param proxyListenerName The name of the PROXYListener
     * @param targetPort        The port the resulting envoy listener listens to
     * 
     * @return String the resulting envoy listener's name
     */
    public static String getEnvoyListenerName(String proxyListenerName,
                                              Integer targetPort)
    {

        String targetPortStr = "_" + targetPort.toString();
        return proxyListenerName.endsWith("_tls") ? proxyListenerName.substring(0, proxyListenerName.lastIndexOf("_tls")) + targetPortStr + "_tls"
                                                  : proxyListenerName + targetPortStr;

    }

    /**
     * Returns a list of hashmaps containing all the information about the NF
     * Instance Services that match provided uri from SEPP/SCP configuration. The
     * IP-address or FQDN and the port configured that is indicated in the URL of
     * the event is compared to configured NF Instance Services.
     * 
     * Static, Discovered Nf instances as well as static-SCPs are all considered.
     * Called from the NfInstanceAlarmHandler.
     * 
     * @param seppFunction SEPP/SCP configuration
     * @param url          Url in the form {@code <IPv4>[:<port>][|<FQDN>:<port>]}
     *                     parts in [] are optional
     * @param clusterName  the Name of the envoy cluster the blocked host belongs to
     * @return A list of maps containing information about NF instance Services that
     *         matched.
     * 
     */
    public static List<Map<String, String>> getNfInstanceRdn(final IfNfFunction seppFunction,
                                                             String url,
                                                             String clusterName)
    {
        Optional<Integer> port = Optional.empty();
        String hostIp;
        String hostFqdn = "";
        final List<String> poolNames = getpoolNameFromCluster(clusterName);
        var ipFqdn = url.split("\\|");
        var arrayLength = ipFqdn.length;

        // find out if we have a port and not just an ipV6 address
        if (ipFqdn[0].contains(":") && !(ipFqdn[0].startsWith("[") && ipFqdn[0].endsWith("]")))
        {
            port = Optional.of(Integer.parseInt(ipFqdn[0].substring(ipFqdn[0].lastIndexOf(':') + 1, ipFqdn[0].length())));
            hostIp = ipFqdn[0].substring(0, ipFqdn[0].lastIndexOf(':'));

            if (arrayLength > 1)
            {
                hostFqdn = ipFqdn[1].substring(0, ipFqdn[1].lastIndexOf(':'));
            }
        }
        else
        {
            hostIp = ipFqdn[0];
            if (arrayLength > 1)
            {

                hostFqdn = ipFqdn[1];

            }
        }

        // remove square brackets from IPv6 addresses
        if (hostIp.startsWith("[") && hostIp.endsWith("]"))
        {
            hostIp = hostIp.replace("[", "");
            hostIp = hostIp.replace("]", "");
        }

        final String finalHostIp = hostIp;
        final Optional<Integer> finalPort = port;
        final String finalHostFqdn = hostFqdn;
        log.info("received from envoy status: ip={} fqdn={} port={}", finalHostIp, finalHostFqdn, finalPort);
        List<Map<String, String>> names = new ArrayList<>();

        if (seppFunction == null)
        {
            log.info("No NfFunction found in configuration. NF Instance with URL {} cannot be found.", url);
            return names;
        }
        // simple container to hold NF service and NF instance names to be used in the
        // rdn

        for (IfNfInstance seppInst : seppFunction.getNfInstance())
        {
            seppInst.getNfPool().stream().filter(pool -> poolNames.contains(pool.getName())).forEach(pool ->
            {

                // Search for static (= configured) NF Instances:
                pool.getNfPoolDiscovery()
                    .stream()
                    .flatMap(p -> Utils.streamIfExists(p.getStaticNfInstanceDataRef()))
                    .map(nfInstance -> Utils.getByName(seppInst.getStaticNfInstanceData(), nfInstance))
                    .filter(Objects::nonNull)
                    .map(d -> (IfStaticNfInstanceDatum) d)
                    .flatMap(datum -> datum.getStaticNfInstance().stream())
                    .forEach(nfInstance -> nfInstance.getStaticNfService()
                                                     .stream()
                                                     .forEach(svc -> constructRdn(svc,
                                                                                  names,
                                                                                  finalPort,
                                                                                  finalHostIp,
                                                                                  finalHostFqdn,
                                                                                  seppInst,
                                                                                  pool,
                                                                                  nfInstance,
                                                                                  NfType.STATIC.toString())));

                // search for discovered instances
                pool.getNfPoolDiscovery()
                    .stream()
                    .flatMap(p -> Utils.streamIfExists(p.getDiscoveredNfInstance()))
                    .map(i -> (IfDiscoveredNfInstance) i)
                    .forEach(nfInstance -> nfInstance.getDiscoveredNfService()
                                                     .stream()
                                                     .forEach(svc -> constructRdn(svc,
                                                                                  names,
                                                                                  finalPort,
                                                                                  finalHostIp,
                                                                                  finalHostFqdn,
                                                                                  seppInst,
                                                                                  pool,
                                                                                  nfInstance,
                                                                                  NfType.DISCOVERED.toString())));

                // search in statically defined scp instances
                var scpList = ConfigHelper.getStaticScpInstances(Stream.concat(pool.getStaticScpInstanceDataRef().stream(),
                                                                               pool.getPriorityGroup()
                                                                                   .stream()
                                                                                   .flatMap(prioGroup -> prioGroup.getStaticScpInstanceDataRef().stream()))
                                                                       .toList(),
                                                                 seppInst);

                scpList.stream()
                       .forEach(nf -> nf.fetchNfService()
                                        .stream()
                                        .forEach(nfService -> constructRdn(nfService,
                                                                           names,
                                                                           finalPort,
                                                                           finalHostIp,
                                                                           finalHostFqdn,
                                                                           seppInst,
                                                                           pool,
                                                                           nf,
                                                                           NfType.STATIC_SCP_WITHOUT_DOMAIN.toString())));
                scpList.stream()
                       .forEach(nf -> nf.fetchScpDomainInfo()
                                        .stream()
                                        .forEach(domain -> constructRdn(domain,
                                                                        names,
                                                                        finalPort,
                                                                        finalHostIp,
                                                                        finalHostFqdn,
                                                                        seppInst,
                                                                        pool,
                                                                        nf,
                                                                        NfType.STATIC_SCP.toString())));

                ConfigHelper.getDiscoveredScpInstances(pool)
                            .stream()
                            .forEach(nfInstance -> nfInstance.fetchScpDomainInfo()
                                                             .stream()
                                                             .forEach(svc -> constructRdn(svc,
                                                                                          names,
                                                                                          finalPort,
                                                                                          finalHostIp,
                                                                                          finalHostFqdn,
                                                                                          seppInst,
                                                                                          pool,
                                                                                          nfInstance,
                                                                                          NfType.DISC_SCP.toString())));
                ConfigHelper.getStaticSeppInstances(pool.getStaticSeppInstanceDataRef(), seppInst)
                            .stream()
                            .forEach(seppInstance -> constructRdn(seppInstance,
                                                                  names,
                                                                  finalPort,
                                                                  finalHostIp,
                                                                  finalHostFqdn,
                                                                  seppInst,
                                                                  pool,
                                                                  seppInstance,
                                                                  NfType.STATIC_SEPP.toString()));

            });
        }
        if (names.isEmpty())
        {
            log.warn("NF Instance {} for nf-pool name {} not found in the configuration {}", url, poolNames, seppFunction);

        }
        return names; // return empty list
    }

    private static void constructRdn(IfTypedNfAddressProperties svc,
                                     List<Map<String, String>> names,
                                     Optional<Integer> finalPort,
                                     String finalHostIp,
                                     String finalHostFqdn,
                                     IfNfInstance seppInst,
                                     IfNfPool pool,
                                     IfNamedListItem nfInstance,
                                     String type)
    {

        var address = svc.getAddress();
        if (address != null)
        {
            int initialNfListSize = names.size();
            if (address.getMultipleIpEndpoint() != null && !address.getMultipleIpEndpoint().isEmpty())   // check multipleIpEndpoints
            {
                address.getMultipleIpEndpoint()
                       .stream()
                       .filter(multIpEp -> finalPort.isEmpty() || (multIpEp.getPort() == null && (finalPort.get().equals(80) || finalPort.get().equals(443)))
                                           || finalPort.get().equals(multIpEp.getPort())) // true if no port is specified, multiIpEp is there
                                                                                          // but doesnt contain a port so the defaults are
                                                                                          // used
                                                                                          // or port is there and they are equal
                       .flatMap(multIpEp -> Stream.concat(multIpEp.getIpv4Address().stream(), multIpEp.getIpv6Address().stream()))
                       .filter(ipAddr -> ipAddr.equalsIgnoreCase(finalHostIp))
                       .forEach(ipAddr -> names.add(constructNfRdnMap(seppInst.getName(),
                                                                      pool.getName(),
                                                                      nfInstance.getName(),
                                                                      svc.getName(),
                                                                      ipAddr + (finalPort.isPresent() ? ":" + finalPort.get() : ""),
                                                                      type)));

            }
            boolean foundIp = names.size() - initialNfListSize > 0;
            if (!foundIp && address.getFqdn() != null && address.getFqdn().equalsIgnoreCase(finalHostFqdn))
            {
                // we got an fqdn, no need to go through the ips
                if (finalPort.isEmpty())
                {
                    names.add(constructNfRdnMap(seppInst.getName(), pool.getName(), nfInstance.getName(), svc.getName(), "", type));
                }
                else if ((address.getMultipleIpEndpoint() == null || address.getMultipleIpEndpoint().isEmpty())
                         && (finalPort.get().equals(80) || finalPort.get().equals(443)))
                {
                    names.add(constructNfRdnMap(seppInst.getName(), pool.getName(), nfInstance.getName(), svc.getName(), "", type));
                }
                else if (!address.getMultipleIpEndpoint().isEmpty())
                    // if a port is provided it needs to be checked with configured ones
                    address.getMultipleIpEndpoint()
                           .stream()
                           .filter(multIpEp -> multIpEp.getPort().equals(finalPort.get()))
                           .forEach(multIpEp -> names.add(constructNfRdnMap(seppInst.getName(),
                                                                            pool.getName(),
                                                                            nfInstance.getName(),
                                                                            svc.getName(),
                                                                            "",
                                                                            type)));
            }
        }

    }

    private static Map<String, String> constructNfRdnMap(String seppInst,
                                                         String pool,
                                                         String nfInst,
                                                         String svc,
                                                         String ipPort,
                                                         String type)
    {
        Map<String, String> map = new HashMap<>();
        map.put(RdnKeys.SEPP_SCP_NF_INSTANCE.toString(), seppInst);
        map.put(RdnKeys.POOL.toString(), pool);
        map.put(RdnKeys.NF_INSTANCE.toString(), nfInst);
        map.put(RdnKeys.NF_SERVICE.toString(), svc);
        map.put(RdnKeys.IP_ADDRESS.toString(), ipPort);
        map.put(RdnKeys.NF_TYPE.toString(), type);

        return map;
    }

    private static List<String> getpoolNameFromCluster(String clusterName)
    {
        /*
         * ==================================== POOL NAMING CONVENTIONS:
         * =================================== Round Robin: Without last-resort: PoolA
         * With last-resort: PoolA#!_#LRP:PoolB Preferred Routing: Aggregated cluster:
         * Without last-resort: PoolA#!_#aggr: With last-resort:
         * PoolA#!_#LRP:PoolB#!_#aggr: Subset cluster: Same with and without
         * last-resort: PoolA#!_#subset: Round Robin cluster: Without last-resort:
         * PoolA#!_#all: With last-resort: PoolA#!_#LRP:PoolB#!_#all: Strict Routing:
         * PoolA#!_#subset_sr: subset_sr:/ aggr: / subset: we should never be getting
         * this for Temp blocking as it's only added to RR clusters
         */
        List<String> pools = new ArrayList<>();
        var separator = "#!_#";
        var lastResortToken = "LRP:";
        List<String> tokens = Arrays.asList(clusterName.split(separator));
        // tokens[0] has always the first pool we should look into for blocked hosts
        pools.add(tokens.get(0));
        tokens.stream().filter(token -> token.contains(lastResortToken)).findAny().ifPresent(pool -> pools.add(pool.substring(lastResortToken.length())));

        return pools;

    }

    /**
     * Check if the NF Service Instance name is configured in the SCP configuration.
     * Called from the NfInstanceAlarmHandler.
     * 
     * @param seppFunction SEPP/SCP NfFunction
     * @param rdn          Map containing info about the NF Service Instance
     * @return true if NF Instance/Service combination is found in one of the
     *         configured pools
     */
    public static boolean isNfServiceNameValid(final IfNfFunction seppFunction,
                                               final Map<String, String> rdn)
    {
        if (rdn.isEmpty())
        {
            log.error("cannot get nf-pool, nf-instance and/or nf-service names. Provided rdn is empty");
            return false;
        }
        final String ipAddressWithPort = rdn.get(RdnKeys.IP_ADDRESS.toString());
        final String poolName = rdn.get(RdnKeys.POOL.toString());
        final String nfType = rdn.get(RdnKeys.NF_TYPE.toString());
        final String nfInstName = rdn.get(RdnKeys.NF_INSTANCE.toString());
        final String nfSvcName = rdn.get(RdnKeys.NF_SERVICE.toString());
        final String ipAddr;
        final String ipPort;
        if (ipAddressWithPort != null && !ipAddressWithPort.isEmpty())
        {
            ipAddr = ipAddressWithPort.substring(0, ipAddressWithPort.lastIndexOf(':'));

            ipPort = ipAddressWithPort.substring(ipAddressWithPort.lastIndexOf(':') + 1, ipAddressWithPort.length());
        }
        else
        {
            ipAddr = null;
            ipPort = null;
        }

        log.debug("Checking validity for nf-pool name:{}, NF instance name: {}, NF service name: {}", poolName, nfInstName, nfSvcName);

        if (seppFunction == null)
            return false;

        boolean found = false;

        for (var seppInst : seppFunction.getNfInstance())
        {
            Stream<IfTypedNfAddressProperties> svcStream = Stream.empty();
            var poolStream = seppInst.getNfPool().stream().filter(pool -> pool.getName().equals(poolName));
            if (nfType.equals(NfType.STATIC.toString()))
            {
                svcStream = poolStream.flatMap(pool -> pool.getNfPoolDiscovery().stream())
                                      .flatMap(p -> Utils.streamIfExists(p.getStaticNfInstanceDataRef()))
                                      .map(nfInstance -> Utils.getByName(seppInst.getStaticNfInstanceData(), nfInstance))
                                      .filter(Objects::nonNull)
                                      .map(IfStaticNfInstanceDatum.class::cast)
                                      .flatMap(datum -> datum.getStaticNfInstance().stream())
                                      .filter(nfInstance -> nfInstance.getName().equals(nfInstName))
                                      .flatMap(nfInst -> nfInst.getStaticNfService().stream())
                                      .filter(nfSvc -> nfSvc.getName().equals(nfSvcName))
                                      .map(IfTypedNfAddressProperties.class::cast);

            }
            else if (nfType.equals(NfType.DISCOVERED.toString()))
            {
                svcStream = poolStream.flatMap(pool -> pool.getNfPoolDiscovery().stream())
                                      .flatMap(poolDisc -> Utils.streamIfExists(poolDisc.getDiscoveredNfInstance()))
                                      .map(IfDiscoveredNfInstance.class::cast)
                                      .filter(nfInstance -> nfInstance.getName().equals(nfInstName))
                                      .flatMap(nfInst -> nfInst.getDiscoveredNfService().stream())
                                      .filter(nfSvc -> nfSvc.getName().equals(nfSvcName))
                                      .map(IfTypedNfAddressProperties.class::cast);

            }
            else if (nfType.equals(NfType.STATIC_SEPP.toString()))
            {
                svcStream = poolStream.flatMap(pool -> pool.getStaticSeppInstanceDataRef().stream())
                                      .map(nfInstance -> Utils.getByName(seppInst.getStaticSeppInstanceData(), nfInstance))
                                      .filter(Objects::nonNull)
                                      .map(IfStaticSeppInstanceDatum.class::cast)
                                      .filter(nfInstance -> nfInstance.getName().equals(nfInstName))
                                      .flatMap(nfInst -> nfInst.getStaticSeppInstance().stream())
                                      .filter(Objects::nonNull)
                                      .filter(nfSvc -> nfSvc.getName().equals(nfSvcName))
                                      .map(IfTypedNfAddressProperties.class::cast);

            }
            else if (nfType.equals(NfType.STATIC_SCP_WITHOUT_DOMAIN.toString()))
            {
                // static-scps
                svcStream = poolStream.flatMap(pool -> Stream.concat(CommonConfigUtils.getScpInstances(pool, seppInst).stream(),
                                                                     Utils.streamIfExists(pool.getPriorityGroup())
                                                                          .flatMap(sp -> CommonConfigUtils.getScpInstances(sp, seppInst).stream())))
                                      .filter(nfInstance -> nfInstance.getName().equals(nfInstName))
                                      .flatMap(nfInst -> nfInst.fetchNfService().stream())
                                      .filter(nfSvc -> nfSvc.getName().equals(nfSvcName))
                                      .map(IfTypedNfAddressProperties.class::cast);

            }
            else if (nfType.equals(NfType.STATIC_SCP.toString()))
            {

                svcStream = poolStream.flatMap(pool -> ConfigHelper.getStaticScpInstances(Stream.concat(pool.getStaticScpInstanceDataRef().stream(),
                                                                                                        pool.getPriorityGroup()
                                                                                                            .stream()
                                                                                                            .flatMap(prioGroup -> prioGroup.getStaticScpInstanceDataRef()
                                                                                                                                           .stream()))
                                                                                                .toList(),
                                                                                          seppInst)
                                                                   .stream())
                                      .filter(nfInstance -> nfInstance.getName().equals(nfInstName))
                                      .flatMap(nfInst -> nfInst.fetchScpDomainInfo().stream())
                                      .filter(domainInfo -> domainInfo.getName().equals(nfSvcName))
                                      .map(IfTypedNfAddressProperties.class::cast);
            }
            else
            {
                // Discovered-scps
                svcStream = poolStream.flatMap(pool -> ConfigHelper.getDiscoveredScpInstances(pool).stream())
                                      .filter(nfInstance -> nfInstance.getName().equals(nfInstName))
                                      .flatMap(nfInst -> nfInst.fetchScpDomainInfo().stream())
                                      .filter(domainInfo -> domainInfo.getName().equals(nfSvcName))
                                      .map(IfTypedNfAddressProperties.class::cast);
            }
            // check if we parsed an IP
            if (ipAddr != null)
            {
                Integer port = (ipPort != null) ? Integer.valueOf(ipPort) : null;
                var ipEpStream = svcStream.flatMap(svc -> svc.getAddress().getMultipleIpEndpoint().stream());
                if (port != null)
                {
                    // either ports match or these is no port configured but it's the defaults
                    ipEpStream = ipEpStream.filter(ipEp -> (port.equals(ipEp.getPort())) || ((ipEp.getPort() == null) && port.equals(80) || port.equals(443)));
                }
                found = ipEpStream.flatMap(ipEp -> Stream.concat(ipEp.getIpv4Address().stream(), ipEp.getIpv6Address().stream()))
                                  .anyMatch(ip -> ip.equalsIgnoreCase(ipAddr));

            }
            else
            {

                found = svcStream.findAny().isPresent();
            }
            if (found)
                return found;
        }
        return found; // false
    }

    /**
     * Returns the size of a pool, or zero if no pool found. Current implementation
     * assumes there are no duplicate endpoints in said pool. To be used when pool
     * unavailable alarm is added. TODO: needs adaptation for static scps
     * 
     * @param seppCfg
     * @param poolRdn
     * @return
     */
    public static int getPoolSize(final IfNfFunction seppCfg,
                                  String poolRdn)
    {

        log.debug("Checking size of the nf-pool: {}", poolRdn);

        String delimiter = "pool=";
        int pos = poolRdn.lastIndexOf(delimiter);
        String poolName = poolRdn.substring(pos + delimiter.length());
        // container to keep "created" endpoints. If we care about duplicates an
        // approach would be to use a set container.
        List<ProxyEndpoint> pxEndpoints = new ArrayList<>();

        for (var seppInst : seppCfg.getNfInstance())
        {
            for (var pool : seppInst.getNfPool())
            {

                if (poolName.equals(pool.getName()))
                {
                    var staticNfIServices = pool.getNfPoolDiscovery()
                                                .stream()
                                                .flatMap(p -> Utils.streamIfExists(p.getStaticNfInstanceDataRef()))
                                                .map(nfInstance -> Utils.getByName(seppInst.getStaticNfInstanceData(), nfInstance))
                                                .filter(Objects::nonNull)
                                                .map(d -> (IfStaticNfInstanceDatum) d)
                                                .flatMap(datum -> datum.getStaticNfInstance().stream())
                                                .map(staticNfInstance -> (IfTypedNfInstance) staticNfInstance)
                                                .flatMap(nfInstance -> nfInstance.fetchNfService().stream());

                    var discoveredNfServices = pool.getNfPoolDiscovery()
                                                   .stream()
                                                   .flatMap(poolDisc -> Utils.streamIfExists(poolDisc.getDiscoveredNfInstance()))
                                                   .map(discNfInstance -> (IfTypedNfInstance) discNfInstance)
                                                   .flatMap(nfInstance -> nfInstance.fetchNfService().stream());

                    Stream.concat(staticNfIServices, discoveredNfServices).forEach(nfServ ->
                    {
                        // address is mandatory since fqdn inside it is
                        var address = nfServ.getAddress();
                        String addr;
                        if (address.getMultipleIpEndpoint() == null || address.getMultipleIpEndpoint().isEmpty())
                        {
                            addr = address.getFqdn();
                            int addrPort = address.getScheme() == Scheme.HTTPS ? 443 : 80;
                            var pxEp = new ProxyEndpoint(addr, addrPort);
                            pxEndpoints.add(pxEp);
                        }
                        else
                        {
                            address.getMultipleIpEndpoint().forEach(multIpEp ->
                            {
                                boolean ipAbsent = (multIpEp.getIpv4Address() == null || multIpEp.getIpv4Address().isEmpty())
                                                   && (multIpEp.getIpv6Address() == null || multIpEp.getIpv6Address().isEmpty());
                                ArrayList<String> ipOrFqdn = new ArrayList<>();
                                ipOrFqdn.addAll(multIpEp.getIpv4Address());
                                ipOrFqdn.addAll(multIpEp.getIpv6Address());

                                if (ipAbsent)
                                {
                                    ipOrFqdn.add(address.getFqdn());
                                }

                                ipOrFqdn.stream().forEach(ipAddrOrFqdn ->
                                {
                                    int addrPort = (multIpEp.getPort() == null) ? (address.getScheme() == Scheme.HTTPS ? 443 : 80) : multIpEp.getPort();
                                    var pxEp = new ProxyEndpoint(ipAddrOrFqdn, addrPort);
                                    pxEndpoints.add(pxEp);
                                });
                            });
                        }
                    });

                    log.debug("size of nf-pool: {}", pxEndpoints.size());
                    return pxEndpoints.size();
                }
            }
        }
        return pxEndpoints.size();

    }

    /**
     * returns the tempBlockingTime for the provided cluster. If no temporary
     * blocking configuration is found, or no pool match in the SEPP/SCP config,
     * returns 0
     */
    public static int getTempBlockingTime(final IfNfFunction seppCfg,
                                          String clusterName)
    {
        var poolNames = getpoolNameFromCluster(clusterName);
        // poolNames[0] has always the first pool we should look into for blocking time
        log.debug("PoolName From Cluster:{}", poolNames.get(0));
        for (var seppInst : seppCfg.getNfInstance())
        {
            var poolMatch = seppInst.getNfPool().stream().filter(pool -> pool.getName().equals(poolNames.get(0))).findAny();
            if (poolMatch.isPresent())
            {
                if (poolMatch.get().getTemporaryBlocking() != null)
                    return poolMatch.get().getTemporaryBlocking().getBlockingTime();

            }
        }

        log.warn("Couldn't find temporary blocking configuration for cluster {}. ", clusterName);
        return 0;
    }

    /**
     * Given the configuration, it returns a Map consisting of nf instance id's
     * accompanied with pool's configured threshold and nf-type of each NF. If a
     * pool does not have a threshold configured, it will not be added to Map.
     */
    public static Map<String, List<String>> getDataPerNf(final IfNfFunction cfg)
    {
        Map<String, List<String>> dataMap = new HashMap<>();
        for (var inst : cfg.getNfInstance())
        {
            inst.getNfPool().stream().filter(Objects::nonNull).forEach(pool ->
            {
                if (pool.getThresholdForNfUnavailableAlarm() != null && pool.getThresholdForNfUnavailableAlarm() > 0)
                {
                    var threshold = pool.getThresholdForNfUnavailableAlarm().toString();

                    var listStaticSeppInstanceName = new ArrayList<>();
                    pool.getStaticSeppInstanceDataRef().stream().filter(Objects::nonNull).forEach(listStaticSeppInstanceName::add);
                    pool.getPriorityGroup()
                        .stream()
                        .forEach(pg -> pg.getStaticSeppInstanceDataRef().stream().filter(Objects::nonNull).forEach(listStaticSeppInstanceName::add));

                    inst.getStaticSeppInstanceData()
                        .stream()
                        .filter(Objects::nonNull)
                        .forEach(stNfInst -> stNfInst.getStaticSeppInstance().stream().filter(Objects::nonNull).forEach(nf ->
                        {
                            var nfType = "SEPP";
                            var id = nf.getNfInstanceId();
                            var nfName = nf.getName();
                            if (id != null && listStaticSeppInstanceName.contains(stNfInst.getName()))
                            {
                                dataMap.put(id, List.of(threshold, nfType, nfName, inst.getName()));
                            }

                        }));

                    var listStaticScpInstanceName = new ArrayList<>();
                    pool.getStaticScpInstanceDataRef().stream().filter(Objects::nonNull).forEach(listStaticScpInstanceName::add);
                    pool.getPriorityGroup()
                        .stream()
                        .forEach(pg -> pg.getStaticScpInstanceDataRef().stream().filter(Objects::nonNull).forEach(listStaticScpInstanceName::add));

                    inst.getStaticScpInstanceData()
                        .stream()
                        .filter(Objects::nonNull)
                        .forEach(stNfInst -> stNfInst.getStaticScpInstance().stream().filter(Objects::nonNull).forEach(nf ->
                        {
                            var nfType = "STATIC_SCP";
                            var id = nf.getNfInstanceId();
                            var nfName = nf.getName();
                            if (id != null && listStaticScpInstanceName.contains(stNfInst.getName()))
                            {
                                dataMap.put(id, List.of(threshold, nfType, nfName, inst.getName()));
                            }

                        }));

                    var listStaticNfInstanceName = new ArrayList<>();
                    pool.getNfPoolDiscovery()
                        .stream()
                        .filter(Objects::nonNull)
                        .forEach(dp -> dp.getStaticNfInstanceDataRef().stream().filter(Objects::nonNull).forEach(listStaticNfInstanceName::add));

                    inst.getStaticNfInstanceData()
                        .stream()
                        .filter(Objects::nonNull)
                        .forEach(stNfInst -> stNfInst.getStaticNfInstance().stream().filter(Objects::nonNull).forEach(nf ->
                        {
                            var nfType = "STATIC_NF";
                            var id = nf.getNfInstanceId();
                            var nfName = nf.getName();

                            if (id != null && listStaticNfInstanceName.contains(stNfInst.getName()))
                            {
                                dataMap.put(id, List.of(threshold, nfType, nfName, inst.getName()));
                            }
                        }));

                    // Discovered
                    pool.getNfPoolDiscovery().stream().filter(Objects::nonNull).forEach(dp ->
                    {
                        if (dp.getDiscoveredNfInstance() != null)
                        {
                            dp.getDiscoveredNfInstance().stream().filter(Objects::nonNull).forEach(discNfInst ->
                            {
                                var nfType = "DISCOVERED_NF";
                                var id = discNfInst.getNfInstanceId();
                                var nfName = discNfInst.getName();
                                if (id != null)
                                {
                                    dataMap.put(id, List.of(threshold, nfType, nfName, inst.getName()));
                                }
                            });
                        }
                        if (dp.getDiscoveredNfInstance() != null)
                        {
                            dp.getDiscoveredScpInstance().stream().filter(Objects::nonNull).forEach(discScpInst ->
                            {
                                var nfType = "DISCOVERED_SCP";
                                var id = discScpInst.getNfInstanceId();
                                var nfName = discScpInst.getName();
                                if (id != null)
                                {
                                    dataMap.put(id, List.of(threshold, nfType, nfName, inst.getName()));
                                }
                            });
                        }
                    });
                }
            });
        }

        log.debug("Map of Instance IDs and Thersholds {} ", dataMap);
        return dataMap;

    }

    public static ProxyRoutingAction createRoutingActionBase(final IfActionRouteBase ra,
                                                             final String clusterSuffix)
    {
        var routingAction = new ProxyRoutingAction();

        if (ra.getTargetNfPool() != null)
        {
            Optional.ofNullable(ra.getTargetNfPool().getNfPoolRef()).ifPresent(name -> routingAction.setDestinationPoolRef(name + clusterSuffix));
            Optional.ofNullable(ra.getTargetNfPool().getVarName()).ifPresent(routingAction::setDestinationVarName);
        }

        return routingAction;
    }

    public static ProxyRoutingAction createRoutingActionAddHeader(final String failoverProfileName)
    {
        var proxyRoutingAction = new ProxyRoutingAction();
        proxyRoutingAction.setActionAddHeader(Optional.of(new ProxyActionAddHeader(HEADER_FAILOVER_PROFILE, IfExists.REPLACE, failoverProfileName)));
        return proxyRoutingAction;
    }

    public static ProxyRoutingAction createRoutingActionRejectMessage(final Integer status,
                                                                      final Optional<String> title,
                                                                      final Optional<String> detail,
                                                                      final Optional<String> cause,
                                                                      String format)
    {
        var proxyRoutingAction = new ProxyRoutingAction();
        proxyRoutingAction.setActionRejectMessage(Optional.of(new ProxyActionRejectMessage(status, title, detail, cause, format)));
        return proxyRoutingAction;
    }

    public static ProxyRoutingAction createRoutingActionLog(final String text,
                                                            final String logLevel,
                                                            final Integer maxLogMessageLength)
    {
        var proxyRoutingAction = new ProxyRoutingAction();

        proxyRoutingAction.setActionLog(Optional.of(new ProxyActionLog(parseLogValuesFromText(text), logLevel, maxLogMessageLength)));

        return proxyRoutingAction;
    }

    public static ProxyRoutingAction createRoutingActionDropMessage(final Optional<Boolean> drop)
    {
        var proxyRoutingAction = new ProxyRoutingAction();
        proxyRoutingAction.setActionDropMessage(drop);
        return proxyRoutingAction;
    }

    public static ProxyRoutingAction buildRoutingActionTarget(final ProxyRoutingAction pra,
                                                              final IfActionRouteTarget ra)
    {
        String header = null;

        if (ra.getFromTargetApiRootHeader() != null)
        {
            header = TARGET_API_ROOT_HEADER;
        }
        else if (ra.getFromAuthorityHeader() != null)
        {
            header = AUTHORITY_HEADER;
        }

        return pra.setFromHeader(header).setFromVarName(ra.getFromVarName());

    }

    public static ProxyRoutingAction buildRoutingActionLastResort(final ProxyRoutingAction pra,
                                                                  final String lastResortPoolRef)
    {
        var tempPra = pra;

        if (lastResortPoolRef != null)
        {
            tempPra.setLastResortPoolRef(lastResortPoolRef);
        }

        return tempPra;
    }

    public static Optional<IfNfPool> tryGetLastResortPool(final String lastResortPool,
                                                          final IfRoutingAction routingAction,
                                                          final IfNfInstance nfInst)
    {
        if (lastResortPool != null)
        {
            var pool = Utils.getByName(nfInst.getNfPool(), lastResortPool);

            if (pool == null)
            {
                throw new BadConfigurationException("Last-resort-nf-pool {} could not be found for routing-action {}", lastResortPool, routingAction.getName());
            }

            return Optional.ofNullable(pool);
        }

        return Optional.empty();
    }

    /**
     * Returns a string, uniquely identifying an nf-service. At the time written,
     * static-nf-instances/services use the name as key, while discovered ones use
     * the id.
     * 
     * @param inst
     * @param svc
     * @return
     */
    public static String getUniqueIdForSvc(IfTypedNfInstance inst,
                                           IfTypedNfAddressProperties svc)
    {
        if (svc instanceof com.ericsson.sc.sepp.model.DiscoveredNfService || svc instanceof com.ericsson.sc.scp.model.DiscoveredNfService)
        {
            return inst.getNfInstanceId() + ":" + ((IfTypedNfService) svc).getNfServiceId();
        }
        else
        {
            return inst.getName() + ":" + svc.getName();
        }
    }

    public static String getUniqueIdForScpDomain(IfTypedScpInstance inst,
                                                 IfTypedNfAddressProperties domainInfo)
    {
        return inst.getName() + ":" + domainInfo.getName();
    }

    public static Optional<Integer> findMaxTimeoutBudget(IfNfInstance nfInst)
    {
        return nfInst.getFailoverProfile().stream().map(IfFailoverProfile::getRequestTimeBudget).max(Integer::compare);
    }

    /**
     * Build a cluster name suffix. This includes: #!_#LRP:<lrp-name> where it's an
     * aggregate cluster #!_#tls tls and no-tls suffix extension. TMO is using this
     * functionality for dyn forwarding
     * 
     * @optional lastResortPoolName
     * @param wantTls
     * 
     * @return String with the cluster name suffix
     */
    public static String buildClusterNameSuffix(Optional<String> lastResortPoolName,
                                                boolean wantTls)
    {
        var joiner = new StringBuilder(buildClusterNameSuffix(lastResortPoolName));
        joiner.append(String.format(CLUSTER_NAME_SUFFIX_PATTERN, TLS, wantTls ? "on" : "off"));
        return joiner.toString();

    }

    /**
     * Build a cluster name suffix. This includes: #!_#LRP:<lrp-name> where it's an
     * aggregate cluster if a last resort pool name is provided
     * 
     * @optional lastResortPoolName
     * @param wantTls
     * 
     * @return String with the cluster name suffix
     */
    public static String buildClusterNameSuffix(Optional<String> lastResortPoolName)
    {
        var joiner = new StringBuilder();
        lastResortPoolName.ifPresent(lrp -> joiner.append(String.format(CLUSTER_NAME_SUFFIX_PATTERN, LAST_RESORT_POOL_TYPE, lrp)));
        return joiner.toString();
    }

    public static String getKvTableNameForSlf(String rrName,
                                              String slfRef)
    {
        return rrName + "_" + slfRef;
    }

    /**
     * Return the egress connection profile referenced within the nf-instance or
     * within the pool
     * 
     * @param pool
     * @param nfInst Instance
     * @return the egress connection profile referenced within the nf-instance or
     *         within the pool
     */
    public static IfEgressConnectionProfile getReferencedEgressConnectionProfile(IfNfPool pool,
                                                                                 IfNfInstance nfInst)
    {
        // Get egress-connection-profile-ref
        String ecpRef = pool.getEgressConnectionProfileRef() != null ? pool.getEgressConnectionProfileRef() : nfInst.getEgressConnectionProfileRef();
        return Utils.getByName(nfInst.getEgressConnectionProfile(), ecpRef);
    }

    public static String formatIpv4Ipv6Address(String addr)
    {
        if (addr.contains("["))
        {
            return addr;
        }
        if (addr.contains(":"))
        {
            return "[" + addr + "]";
        }
        return addr;
    }

    public static String formatIpv4Ipv6AddressForRegex(String addr)
    {
        String fAddr;
        if (addr.contains("["))
        {
            fAddr = addr;
        }
        else if (addr.contains(":"))
        {
            fAddr = "[" + addr + "]";
        }
        else
        {
            fAddr = addr;
        }

        return fAddr.replace(".", "\\.").replace("[", "\\[").replace("]", "\\]");
    }

    public static ProxyLocalRateLimiter createProxyLocalRateLimitFilterFromNetwork(String statPrefix,
                                                                                   IfLocalRateLimitProfile profile)
    {
        var tb = profile.getTokenBucket();
        // AddResponseHeader uses the name as a key so entries with the same name are
        // not allowed, map is fine here
        var headers = profile.getAddResponseHeader().stream().collect(Collectors.toMap(IfAddResponseHeader::getName, IfAddResponseHeader::getValue));
        return new ProxyLocalRateLimiter(statPrefix, tb.getMaxTokens(), tb.getTokensPerFill(), tb.getFillInterval(), headers);
    }

    public static List<Float> getGrlPriorityPercentages()
    {
        final String priorityPercentagesJson = EnvVars.get("PRIORITY_PERCENTAGES", value -> value.length() < 2048); // Should be enough space for
                                                                                                                    // '[' + 32 *
                                                                                                                    // '{"priority":12,"percentage":12.12345}' +
                                                                                                                    // 31
        // * ',' + ']'

        final var numberOfPriorityPercentages = 32;
        final List<Float> percentages = new ArrayList<>();

        try
        {
            if (priorityPercentagesJson == null) // Most probably too many watermarks defined
            {
                throw new BadConfigurationException("The configured global rate limit mapping of priorities to token bucket percentages must contain exactly "
                                                    + numberOfPriorityPercentages + " priorities based on 3GPP standard.");
            }

            final JSONArray priorityPercentages = new JSONArray(priorityPercentagesJson);
            Float percentagesSum = 0.0f; // Check: That the list does not contain any element with value out of range
            // [0,100]

            for (int i = 0; i < priorityPercentages.length(); i++)
            {
                final var priorityPercentage = priorityPercentages.getJSONObject(i);

                if (i > 0)
                    percentagesSum += priorityPercentage.getFloat("percentage");

                percentages.add(percentagesSum);
            }

            // Check1:That the sum of the element values in the list provided does not sum
            // over 100
            if (percentagesSum > 100.0f)
            {
                throw new BadConfigurationException("The configured global rate limit mapping of priorities to token bucket percentages must not sum-up over 100%. Percentages: {}",
                                                    percentages);
            }

            // Check2 : That the list contains 32 elements, e.g. is compliant with 3GPP
            // standard
            if (percentages.size() != numberOfPriorityPercentages)
            {
                throw new BadConfigurationException("The configured global rate limit mapping of priorities to token bucket percentages must contain exactly "
                                                    + numberOfPriorityPercentages + " priorities based on 3GPP standard.");
            }
        }
        catch (Exception e)
        {
            log.warn("Could not create global rate limit mapping of priorities to token bucket percentages from configuration. Falling back to default priority percentages. Cause: {}",
                     e.toString());

            final Float delta = 45.0f / numberOfPriorityPercentages; // 45 is the default percentage of the lowest priority (31).

            for (var i = 0; i < numberOfPriorityPercentages; i++)
            {
                if (i == 0)
                    percentages.add(0.0f);
                else
                    percentages.add(percentages.get(i - 1) + delta);
            }
        }

        log.info("Priority percentages list: {}", percentages);

        return percentages;
    }

    /**
     * @param nfInst
     * @param network
     * @return
     */
    public static Optional<ProxyVtapSettings> readVtapConfig(IfNfInstance nfInst,
                                                             IfNetwork network)
    {
        if (nfInst.getVtap() != null && nfInst.getVtap().getEnabled() && nfInst.getVtap().getVtapConfiguration() != null
            && nfInst.getVtap().getVtapConfiguration().getProxy() != null && nfInst.getVtap().getVtapConfiguration().getProxy().getIngress() != null
            && !nfInst.getVtap().getVtapConfiguration().getProxy().getIngress().isEmpty())
        {

            return nfInst.getVtap()
                         .getVtapConfiguration()
                         .getProxy()
                         .getIngress()
                         .stream()
                         .filter(ingress -> isNetworkForTap(network, ingress))
                         .map(ingress -> new ProxyVtapSettings(ingress.getName(), nfInst.getName(), ingress.getEnabled(), ProxyVtapSettings.Direction.INGRESS))
                         .findAny();
        }
        return Optional.empty();
    }

    /**
     * @param network
     * @param ingress
     * @return
     */
    private static boolean isNetworkForTap(IfNetwork network,
                                           IfVtapIngress ingress)
    {
        if (network instanceof ExternalNetwork)
        {
            return ((com.ericsson.sc.sepp.model.Ingress) ingress).getExternalNetworkRef().stream().anyMatch(ref -> network.getName().equals(ref));
        }

        return ingress.getOwnNetworkRef().stream().anyMatch(ref -> network.getName().equals(ref));
    }

    // take the rlfService error action from helm parameters and returns a
    // RateLimitActionProfile based on the parameters
    public static ProxyRateLimitActionProfile getSvcNotFoundErrorAction()
    {
        var enVarName = "GRL_RLF_SERVICE_ERROR";
        final var rlfServiceErrorJson = EnvVars.get(enVarName, "{action:forward}");
        final var rlfServiceErrorJsonObj = new JSONObject(rlfServiceErrorJson);

        var action = rlfServiceErrorJsonObj.optString("action", "forward");

        if (action.equals("forward"))
        {
            return new ProxyRateLimitActionProfile(Type.PASS);
        }
        else if (action.equals("reject"))
        {
            // initialize parameters from helm or get default values
            var status = rlfServiceErrorJsonObj.optInt("status", 500);
            var title = rlfServiceErrorJsonObj.optString("title", "Internal server error");
            var detail = rlfServiceErrorJsonObj.optString("detail", "rate_limiter_error");
            var cause = rlfServiceErrorJsonObj.optString("cause", "SYSTEM_FAILURE");

            log.debug("Rlf service not found error parameters: action:{} status:{} detail:{} title:{} cause:{}", action, status, detail, title, cause);

            return new ProxyRateLimitActionProfile(Type.REJECT,
                                                   status,
                                                   Optional.ofNullable(title),
                                                   Optional.ofNullable(detail),
                                                   Optional.ofNullable(cause),
                                                   "json",
                                                   "delay-seconds");
        }

        else if (action.equals("drop"))
        {
            return new ProxyRateLimitActionProfile(Type.DROP);
        }
        else
        {
            return new ProxyRateLimitActionProfile(Type.PASS);
        }
    }

    /**
     * Getting the targetPort from the k8s svc returns the name of the container
     * port(http-port/https-port/http-port2/https-port2). The actual port value is
     * saved as a manager env var with name 'WORKER_SVC_<contaner port name>'.
     * 
     * This function attempts to fetch said env var, throwing a
     * {@link com.ericsson.utilities.exceptions.BadConfigurationException.BadConfigurationException}
     * in case of failure
     */
    public static int getListenerTargetPortFromEnvVar(String portName)
    {
        var envVarName = "WORKER_SVC_" + portName.replace('-', '_').toUpperCase();
        var listenerPort = EnvVars.get(envVarName);
        if (listenerPort == null)
        {
            throw new BadConfigurationException("environment variable '{}' not found", envVarName);
        }
        return Integer.parseInt(listenerPort);
    }

    /**
     * Takes a string from an action-log and parses all the variables inside double
     * curly braces. For every static substring and every variable, one LogValue
     * entry is created. Each variable type uses a specific LogValue term. For
     * example, strings use termString, and request headers use termReqheader. This
     * method assumes that the syntax of the text has already been checked and is
     * valid
     * 
     * @param text the string to parse
     * @return the list of LogValues generated from the text
     */
    public static List<LogValue> parseLogValuesFromText(String text)
    {
        List<LogValue> logValues = new ArrayList<>();
        String str = "";
        String var = "";

        int index = 0;
        int open = text.indexOf("{{");
        int close = text.indexOf("}}");
        int length = text.length();

        while (index < length)
        {
            if (index != open) // the next term is a string
            {
                if (open == -1) // the string reaches to the end
                {
                    str = text.substring(index, length);
                    LogValue valStr = LogValue.newBuilder().setTermString(str).build();
                    logValues.add(valStr);
                    index = length + 1; // to exit the loop
                }
                else // the string goes up until 'open', then a variable starts
                {
                    str = text.substring(index, open);
                    LogValue valStr = LogValue.newBuilder().setTermString(str).build();
                    logValues.add(valStr);
                    index = open;
                }
            }
            else // the next term is a variable
            {
                var = text.substring(index + 2, close);
                LogValue valVar = null;
                if (var.startsWith("var."))
                {
                    valVar = LogValue.newBuilder().setTermVar(var.substring(4)).build(); // drop "var." from the substr and only keep the value
                }
                else if (var.startsWith("req."))
                {
                    if (var.startsWith("req.header"))
                    {
                        valVar = LogValue.newBuilder().setTermReqheader(var.substring(12, var.length() - 2)).build(); // drop "req.header['" from the substr and
                                                                                                                      // only keep the header name
                    }
                    else if (var.startsWith("req.body"))
                    {
                        valVar = LogValue.newBuilder().setTermBody("request").build(); // termBody is used for printing body, with the value being
                                                                                       // request/response
                    }
                    else // req.method or req.path
                    {
                        valVar = LogValue.newBuilder().setTermReqheader(":" + var.substring(4)).build();
                    }

                }
                else if (var.startsWith("resp."))
                {
                    if (var.startsWith("resp.header"))
                    {
                        valVar = LogValue.newBuilder().setTermRespheader(var.substring(13, var.length() - 2)).build();// drop "resp.header['" from the substr
                                                                                                                      // and only keep the header name
                    }
                    else if (var.startsWith("resp.body"))
                    {
                        valVar = LogValue.newBuilder().setTermBody("response").build(); // termBody is used for printing body, with the value being
                                                                                        // request/response
                    } // responses don't have a method or path
                }
                logValues.add(valVar);
                index = close + 2;
                open = text.indexOf("{{", index);
                close = text.indexOf("}}", index);
            }
        }

        return logValues;
    }

    public enum NfType
    {
        STATIC("static"),
        DISCOVERED("discovered"),
        STATIC_SCP_WITHOUT_DOMAIN("static-scp-no-domain"),
        STATIC_SCP("static-scp"),
        DISC_SCP("discovered-scp"),
        STATIC_SEPP("static-sepp");

        String value;

        private NfType(String value)
        {
            this.value = value;
        }

        @Override
        public String toString()
        {
            return this.value;
        }
    }

    public enum RdnKeys
    {
        SEPP_SCP_NF_INSTANCE("sepp-scp-nf-instance"),
        POOL("nf-pool"),
        NF_INSTANCE("nf-instance"),
        NF_SERVICE("nf-service"),
        IP_ADDRESS("ipAddress"),
        NF_TYPE("nf-type");

        String value;

        private RdnKeys(String value)
        {
            this.value = value;
        }

        @Override
        public String toString()
        {
            return this.value;
        }
    }

    public enum DeploymentType
    {
        SEPP,
        SCP;
    }

    /**
     * Helper class used only by SEPP for N32-C purposes
     */
    public static class SeppDatum
    {
        private String seppStatus;
        private Boolean seppTarSupport;

        public SeppDatum(String seppStatus,
                         boolean seppTarSupport)
        {
            this.seppStatus = seppStatus;
            this.seppTarSupport = seppTarSupport;
        }

        public String getSeppStatus()
        {
            return this.seppStatus;
        }

        public Boolean getSeppTarSupport()
        {
            return this.seppTarSupport;
        }
    }

}
