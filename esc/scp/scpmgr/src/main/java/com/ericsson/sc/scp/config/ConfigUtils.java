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

package com.ericsson.sc.scp.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cnal.internal.nrlf.ratelimiting.BucketConfig;
import com.ericsson.sc.configutil.CommonConfigUtils;
import com.ericsson.sc.configutil.CommonConfigUtils.RateLimiting.RateLimitType;
import com.ericsson.sc.configutil.CommonConfigUtils.RateLimiting.RateLimitedEntity;
import com.ericsson.sc.expressionparser.ConditionParser;
import com.ericsson.sc.glue.IfNetwork;
import com.ericsson.sc.glue.IfNfPool;
import com.ericsson.sc.nlf.client.NlfConfigurator;
import com.ericsson.sc.proxy.ConfigHelper;
import com.ericsson.sc.proxyal.proxyconfig.proxyactionconfig.ProxyActionModifyVariable;
import com.ericsson.sc.proxyal.proxyconfig.proxyactionconfig.ProxyActionNfDiscovery;
import com.ericsson.sc.proxyal.proxyconfig.proxyactionconfig.ProxyActionRejectMessage;
import com.ericsson.sc.proxyal.proxyconfig.proxyactionconfig.ProxyActionSlfLookup;
import com.ericsson.sc.proxyal.proxyconfig.proxyactionconfig.ProxyActionSlfLookup.IdentityType;
import com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig.ProxySeppFilter;
import com.ericsson.sc.proxyal.proxyconfig.proxyroutingconfig.ProxyFilterCase;
import com.ericsson.sc.proxyal.proxyconfig.proxyroutingconfig.ProxyFilterData;
import com.ericsson.sc.proxyal.proxyconfig.proxyroutingconfig.ProxyFilterRule;
import com.ericsson.sc.proxyal.proxyconfig.proxyroutingconfig.ProxyRoutingAction;
import com.ericsson.sc.rlf.client.RlfConfigurator;
import com.ericsson.sc.scp.model.ActionNfDiscovery;
import com.ericsson.sc.scp.model.ActionRoutePreferred;
import com.ericsson.sc.scp.model.ActionRouteRemotePreferred;
import com.ericsson.sc.scp.model.ActionRouteRemoteRoundRobin;
import com.ericsson.sc.scp.model.ActionRouteRoundRobin;
import com.ericsson.sc.scp.model.ActionRouteStrict;
import com.ericsson.sc.scp.model.ActionSlfLookup;
import com.ericsson.sc.scp.model.DnsProfile;
import com.ericsson.sc.scp.model.EndpointIpFamily;
import com.ericsson.sc.scp.model.EricssonScpScpFunction;
import com.ericsson.sc.scp.model.MessageDatum;
import com.ericsson.sc.scp.model.NfInstance;
import com.ericsson.sc.scp.model.PreserveIfIndirectRouting;
import com.ericsson.sc.scp.model.RoutingAction;
import com.ericsson.sc.scp.model.RoutingRule;
import com.ericsson.sc.scp.model.SlfLookupProfile;
import com.ericsson.sc.scp.model.TargetNfPool;
import com.ericsson.sc.utilities.dns.IfDnsLookupContext;
import com.ericsson.sc.utilities.dns.IpFamily;
import com.ericsson.sc.utilities.dns.ResolutionResult;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.common.IfNamedListItem;
import com.ericsson.utilities.common.Rdn;
import com.ericsson.utilities.common.Triplet;
import com.ericsson.utilities.common.Utils;
import com.ericsson.utilities.exceptions.BadConfigurationException;

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.IPFamily;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.PreserveIfIndirect;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.RoutingBehaviour;

public class ConfigUtils
{
    private static final Logger log = LoggerFactory.getLogger(ConfigUtils.class);

    private ConfigUtils()
    {
    }

    public static com.ericsson.cnal.internal.nnlf.nfdiscovery.oam.Config mapToNlfConfig(final EricssonScpScpFunction inConfig,
                                                                                        final Map<String, IfDnsLookupContext> dnsResults)
    {
        // Map the SCP configuration to the configuration for NLF.

        if (inConfig == null || inConfig.getNfInstance() == null || inConfig.getNfInstance().isEmpty())
            return new com.ericsson.cnal.internal.nnlf.nfdiscovery.oam.Config();

        final List<com.ericsson.cnal.internal.nnlf.nfdiscovery.oam.NrfGroup> groups = inConfig.getNfInstance().stream().flatMap(instance ->
        {
            return instance.getNrfGroup()
                           .stream()
                           .map(group -> new com.ericsson.cnal.internal.nnlf.nfdiscovery.oam.NrfGroup().name(group.getName())
                                                                                                       .source("ericsson-scp")
                                                                                                       .path(new Rdn("nf",
                                                                                                                     "scp-function").add("nf-instance",
                                                                                                                                         instance.getName())
                                                                                                                                    .toString(false))
                                                                                                       .nrf(group.getNrf().stream().map(nrf ->
                                                                                                       {
                                                                                                           final List<com.ericsson.cnal.internal.nnlf.nfdiscovery.oam.IpEndpoint> endpoints = new ArrayList<>();

                                                                                                           nrf.getIpEndpoint().forEach(ep ->
                                                                                                           {
                                                                                                               final com.ericsson.cnal.internal.nnlf.nfdiscovery.oam.IpEndpoint endpoint =//
                                                                                                                       new com.ericsson.cnal.internal.nnlf.nfdiscovery.oam.IpEndpoint().name(ep.getName())
                                                                                                                                                                                       .port(ep.getPort())
                                                                                                                                                                                       .ipv4Address(ep.getIpv4Address())
                                                                                                                                                                                       .ipv6Address(ep.getIpv6Address());

                                                                                                               endpoints.add(endpoint);

                                                                                                               if (endpoint.getIpv4Address() == null
                                                                                                                   && endpoint.getIpv6Address() == null)
                                                                                                               {
                                                                                                                   final Set<IpFamily> ipFamilies = new HashSet<>();

                                                                                                                   if (group.getDnsProfileRef() != null)
                                                                                                                   {
                                                                                                                       final DnsProfile dnsProfile = Utils.getByName(instance.getDnsProfile(),
                                                                                                                                                                     group.getDnsProfileRef());

                                                                                                                       if (dnsProfile != null)
                                                                                                                           dnsProfile.getIpFamilyResolution()
                                                                                                                                     .forEach(r -> ipFamilies.add(com.ericsson.sc.utilities.dns.IpFamily.fromValue(r.value())));
                                                                                                                   }

                                                                                                                   if (ipFamilies.isEmpty())
                                                                                                                       ipFamilies.addAll(CommonConfigUtils.getDefaultIpFamilies(instance));

                                                                                                                   final IfDnsLookupContext dnsResult = dnsResults.get(nrf.getFqdn());

                                                                                                                   if (dnsResult != null)
                                                                                                                   {
                                                                                                                       ipFamilies.forEach(ipFamily ->
                                                                                                                       {
                                                                                                                           final ResolutionResult ip = dnsResult.getIpAddr(ipFamily);

                                                                                                                           if (ip != null && ip.isResolvedOk())
                                                                                                                           {
                                                                                                                               log.info("Using resolved IP '{}' for FQDN '{}'",
                                                                                                                                        ip.get(),
                                                                                                                                        nrf.getFqdn());

                                                                                                                               if (ipFamily.equals(IpFamily.IPV4))
                                                                                                                                   endpoint.ipv4Address(ip.get());
                                                                                                                               else if (ipFamily.equals(IpFamily.IPV6))
                                                                                                                                   endpoint.ipv6Address(ip.get());
                                                                                                                           }
                                                                                                                       });
                                                                                                                   }
                                                                                                               }
                                                                                                           });

                                                                                                           return new com.ericsson.cnal.internal.nnlf.nfdiscovery.oam.Nrf().name(nrf.getName())
                                                                                                                                                                           .fqdn(nrf.getFqdn())
                                                                                                                                                                           .ipEndpoint(endpoints)
                                                                                                                                                                           .priority(nrf.getPriority())
                                                                                                                                                                           .requestTimeout(nrf.getRetryTimeout())
                                                                                                                                                                           .scheme(com.ericsson.cnal.internal.nnlf.nfdiscovery.oam.Nrf.SchemeEnum.fromValue(nrf.getScheme()
                                                                                                                                                                                                                                                               .value()))
                                                                                                                                                                           .srcSbiNfPeerInfo(com.ericsson.sc.scp.model.glue.NfInstance.getSrcSbiNfPeerInfo(instance,
                                                                                                                                                                                                                                                           group,
                                                                                                                                                                                                                                                           nrf));
                                                                                                       }).collect(Collectors.toList())));
        }).collect(Collectors.toList());

        final List<com.ericsson.cnal.internal.nnlf.nfdiscovery.oam.NrfGroup> sortedGroups = Stream.of(groups)
                                                                                                  .flatMap(List::stream)
                                                                                                  .sorted((l,
                                                                                                           r) -> l.getName().compareTo(r.getName()))
                                                                                                  .collect(Collectors.toList());
        NlfConfigurator.dispatchNrfGroupIds(sortedGroups);

        return new com.ericsson.cnal.internal.nnlf.nfdiscovery.oam.Config().nrfGroup(sortedGroups);
    }

    public static List<BucketConfig> mapToRlfConfig(final EricssonScpScpFunction config)
    {
        // Map the SCP configuration to the configuration for RLF.
        var GRLEnabled = Boolean.parseBoolean(EnvVars.get("GLOBAL_RATE_LIMIT_ENABLED", false));
        if (GRLEnabled == false)
        {
            log.debug("GRL is disabled");
            return List.of();

        }
        if (config == null || config.getNfInstance() == null || config.getNfInstance().isEmpty())
            return List.of();

        final float lowerPrioPercentage = CommonConfigUtils.getGrlPriorityPercentages().get(CommonConfigUtils.getGrlPriorityPercentages().size() - 1);

        // Scan all own networks for rate limits.

        final List<BucketConfig> bucketsOwnNetwork = config.getNfInstance().stream().flatMap(instance ->
        {
            return instance.getOwnNetwork().stream().flatMap(network ->
            {
                return network.getGlobalIngressRateLimitProfileRef().stream().flatMap(ref ->
                {
                    return instance.getGlobalRateLimitProfile().stream().filter(limit -> limit.getName().equals(ref)).map(limit ->
                    {
                        final String name = CommonConfigUtils.RateLimiting.createBucketName(RateLimitedEntity.OWN_NETWORK,
                                                                                            network.getName(),
                                                                                            RateLimitType.INGRESS,
                                                                                            limit.getName());
                        final long capacity = Math.round(100f * limit.getMaxBurstSize() / (100f - lowerPrioPercentage));
                        final double fillRate = (double) limit.getSustainableRate();

                        return new BucketConfig().name(name).fillRate(fillRate).capacity(capacity);
                    });
                });
            });
        }).collect(Collectors.toList());

        log.info("bucketsOwnNetwork={}", bucketsOwnNetwork);

        final List<BucketConfig> buckets = Stream.of(bucketsOwnNetwork)
                                                 .flatMap(List::stream)
                                                 .sorted((l,
                                                          r) -> l.getName().compareTo(r.getName()))
                                                 .collect(Collectors.toList());
        RlfConfigurator.dispatchBucketIds(buckets);

        return buckets;
    }

    /**
     * @param routingAction
     * @return
     */
    public static List<ProxyRoutingAction> createRoutingAction(final RoutingAction routingAction,
                                                               final String rrName,
                                                               final NfInstance scpInst)
    {
        // OPTION D: Routing action used to perform delegated NF discovery by querying
        // the NRF with the parameters from the received request
        if (isNfDiscoveryRoutingRule(routingAction))
        {
            var raList = new ArrayList<ProxyRoutingAction>();
            var actionNfDiscovery = routingAction.getActionNfDiscovery();

            // List of triplets to hold Discovery parameters to add to the request to the
            // NLF
            // first: parameter name
            // second: true if third is value, false if third is variable name
            // third: parameter value or variable name
            List<Triplet<String, Boolean, String>> addDiscoveryParametersAll = new ArrayList<>();

            boolean requesterNfTypeFound = false;

            for (var entry : actionNfDiscovery.getAddDiscoveryParameter())
            {

                addDiscoveryParametersAll.add(Triplet.of(entry.getName(),
                                                         entry.getValue() != null,
                                                         entry.getValue() != null ? entry.getValue() : entry.getFromVarName()));

                // SCP should have the requester-nf-type with default value "SCP" unless it
                // receives this 3gpp-sbi-discovery-requester-nf-type header in the request or
                // it is configured as part of nf-discovery routing action
                if (entry.getName().equalsIgnoreCase("requester-nf-type"))
                    requesterNfTypeFound = true;
            }

            if (!requesterNfTypeFound)
                addDiscoveryParametersAll.add(Triplet.of("requester-nf-type", true, "SCP"));

            var discRoutingAction = createRoutingActionNfDiscovery(actionNfDiscovery.getNrfGroupRef(), // Name of the NRF-Group that shall handle this discovery
                                                                   actionNfDiscovery.getRequestTimeout(), // NRF/NLF query timeout in ms
                                                                   selectEndpointIpFamily(actionNfDiscovery), // IP Family (IPv4 or IPv6 or dualStack) to
                                                                                                              // take into account while creating endpoints when
                                                                                                              // FQDN is not present
                                                                   actionNfDiscovery.getUseDiscoveryParameter() != null // Envoy will use the listed
                                                                                                                        // parameters only
                                                                                                             && actionNfDiscovery.getUseDiscoveryParameter()
                                                                                                                                 .getUseSelected() != null
                                                                                                             && actionNfDiscovery.getUseDiscoveryParameter()
                                                                                                                                 .getUseSelected()
                                                                                                                                 .getName() != null
                                                                                                             && !actionNfDiscovery.getUseDiscoveryParameter()
                                                                                                                                  .getUseSelected()
                                                                                                                                  .getName()
                                                                                                                                  .isEmpty() ? actionNfDiscovery.getUseDiscoveryParameter()
                                                                                                                                                                .getUseSelected()
                                                                                                                                                                .getName()
                                                                                                                                             : null,
                                                                   // Flag to indicate if all 3gpp-Sbi-Discovery-* parameters shall be used
                                                                   actionNfDiscovery.getUseDiscoveryParameter() != null && actionNfDiscovery.getUseDiscoveryParameter()
                                                                                                                                            .getUseAll() != null,
                                                                   addDiscoveryParametersAll, // Discovery parameters to add to the request to the NLF.
                                                                   // Variables needed in order to perform delegated discovery based on NF
                                                                   // priority.
                                                                   // An NF is randomly selected from the discovered NF-profiles and the host-name
                                                                   // of the selected NF is stored in 'variable-name-selected-host'.
                                                                   actionNfDiscovery.getNfSelectionOnPriority() != null ? actionNfDiscovery.getNfSelectionOnPriority()
                                                                                                                                           .getVariableNameSelectedHost()
                                                                                                                        : null,
                                                                   // Similarly , the nf-set of the selected NF is stored in ‘variable-name-nfset'
                                                                   actionNfDiscovery.getNfSelectionOnPriority() != null ? actionNfDiscovery.getNfSelectionOnPriority()
                                                                                                                                           .getVariableNameNfset()
                                                                                                                        : null);
            raList.add(discRoutingAction);

            return raList;
        }
        else if (CommonConfigUtils.isStrictRoutingRule(routingAction))
        {
            var raList = new ArrayList<ProxyRoutingAction>();
            var actionStrict = routingAction.getActionRouteStrict();

            if (actionStrict.getFailoverProfileRef() != null)
            {
                var addHeaderAction = CommonConfigUtils.createRoutingActionAddHeader(actionStrict.getFailoverProfileRef());
                raList.add(addHeaderAction);
            }

            // Create the routing action for tls and non-tls ports with
            // the specific suffixes based on the service address configuration
            // TMO is using this functionality
            if (isEmptyDynFwdPool(routingAction, scpInst))
            {
                for (boolean wantTls : Egress.getDynFwdTlsEnabledOptions(scpInst))
                {

                    var suffix = CommonConfigUtils.buildClusterNameSuffix(Optional.empty(), wantTls);

                    var proxyRoutingAction = CommonConfigUtils.createRoutingActionBase(actionStrict, suffix);
                    proxyRoutingAction.setKeepAuthorityHeader(false);

                    buildRoutingActionStrict(proxyRoutingAction, actionStrict);
                    raList.add(proxyRoutingAction);
                }
                return raList;
            }

            var proxyRoutingAction = CommonConfigUtils.createRoutingActionBase(actionStrict, "");

            if (actionStrict.getPreserveIfIndirectRouting() != null)
            {
                var value = actionStrict.getPreserveIfIndirectRouting() == PreserveIfIndirectRouting.ABSOLUTE_URI_PATH ? PreserveIfIndirect.ABSOLUTE_PATH
                                                                                                                       : PreserveIfIndirect.TARGET_API_ROOT;
                proxyRoutingAction.setPreserveIfIndirect(value);
            }
            var keepAuthorityHeader = actionStrict.getKeepAuthorityHeader() != null;
            proxyRoutingAction.setKeepAuthorityHeader(keepAuthorityHeader);

            buildRoutingActionStrict(proxyRoutingAction, actionStrict);
            raList.add(proxyRoutingAction);

            return raList;
        }
        else if (CommonConfigUtils.isRoundRobinRule(routingAction))
        {
            var raList = new ArrayList<ProxyRoutingAction>();
            var actionRoundRobin = routingAction.getActionRouteRoundRobin();

            if (actionRoundRobin.getFailoverProfileRef() != null)
            {
                var addHeaderAction = CommonConfigUtils.createRoutingActionAddHeader(actionRoundRobin.getFailoverProfileRef());
                raList.add(addHeaderAction);
            }

            var suffix = CommonConfigUtils.buildClusterNameSuffix(Optional.ofNullable(actionRoundRobin.getLastResortNfPoolRef()));

            var proxyRoutingAction = CommonConfigUtils.createRoutingActionBase(actionRoundRobin,
                                                                               actionRoundRobin.getLastResortNfPoolRef() != null ? suffix : "");

            if (actionRoundRobin.getPreserveIfIndirectRouting() != null)
            {
                var value = actionRoundRobin.getPreserveIfIndirectRouting() == PreserveIfIndirectRouting.ABSOLUTE_URI_PATH ? PreserveIfIndirect.ABSOLUTE_PATH
                                                                                                                           : PreserveIfIndirect.TARGET_API_ROOT;
                proxyRoutingAction.setPreserveIfIndirect(value);
            }

            // The discovery parameters of the received request to preserve in case of
            // indirect routing.
            // By default no parameters are preserved
            if (actionRoundRobin.getPreserveDiscParamIfIndirect() != null)
            {

                if (actionRoundRobin.getPreserveDiscParamIfIndirect().getPreserveAll() != null)
                {
                    proxyRoutingAction.setPreserveAllDiscParams(true);
                }
                else if (actionRoundRobin.getPreserveDiscParamIfIndirect().getPreserveSelected() != null
                         && !actionRoundRobin.getPreserveDiscParamIfIndirect().getPreserveSelected().getName().isEmpty())
                {
                    proxyRoutingAction.setPreserveSelectedDiscParams(actionRoundRobin.getPreserveDiscParamIfIndirect().getPreserveSelected().getName());
                }
            }

            var keepAuthorityHeader = actionRoundRobin.getKeepAuthorityHeader() != null;
            proxyRoutingAction.setKeepAuthorityHeader(keepAuthorityHeader);

            buildRoutingActionRoundRobin(proxyRoutingAction, actionRoundRobin);
            raList.add(proxyRoutingAction);

            return raList;
        }

        // OPTION_D: Routing action to select the host name to include in the
        // target-api-root header and route the request to any of the hosts in the
        // specified target-nf-pool. The target-api-root header is replaced on reselects
        // in a round-robin fashion. Remote route robin action is implemented similarly
        // as Round Robin.
        else if (ConfigUtils.isRemoteRoundRobinAction(routingAction))
        {
            var raList = new ArrayList<ProxyRoutingAction>();
            var actionRemoteRoundRobin = routingAction.getActionRouteRemoteRoundRobin();

            var suffix = CommonConfigUtils.buildClusterNameSuffix(Optional.ofNullable(actionRemoteRoundRobin.getLastResortNfPoolRef()));

            var proxyRoutingAction = createRoutingActionRRR(actionRemoteRoundRobin, suffix);

            if (actionRemoteRoundRobin.getFailoverProfileRef() != null)
            {
                var addHeaderAction = CommonConfigUtils.createRoutingActionAddHeader(actionRemoteRoundRobin.getFailoverProfileRef());
                raList.add(addHeaderAction);
                var failoverProf = Utils.getByName(scpInst.getFailoverProfile(), actionRemoteRoundRobin.getFailoverProfileRef());
                proxyRoutingAction.setNumOfRemoteReselections(failoverProf.getTargetNfPoolReselectsMax());
                proxyRoutingAction.setNumOfRemoteRetries(failoverProf.getPreferredHostRetriesMax());
            }
            else
            {
                var defaultFp = ConfigHelper.getScpDefaultFailoverProfile(Optional.ofNullable(actionRemoteRoundRobin.getLastResortNfPoolRef()).isPresent());
                proxyRoutingAction.setNumOfRemoteReselections(defaultFp.getTargetNfPoolReselectsMax());
            }

            // The discovery parameters of the received request to preserve in case of
            // indirect routing.
            // By default no parameters are preserved
            if (actionRemoteRoundRobin.getPreserveDiscParamIfIndirect() != null)
            {
                if (actionRemoteRoundRobin.getPreserveDiscParamIfIndirect().getPreserveAll() != null)
                {
                    proxyRoutingAction.setPreserveAllDiscParams(true);
                }
                else if (actionRemoteRoundRobin.getPreserveDiscParamIfIndirect().getPreserveSelected() != null
                         && !actionRemoteRoundRobin.getPreserveDiscParamIfIndirect().getPreserveSelected().getName().isEmpty())
                {
                    proxyRoutingAction.setPreserveSelectedDiscParams(actionRemoteRoundRobin.getPreserveDiscParamIfIndirect().getPreserveSelected().getName());
                }
            }

            // Keep authority header is not present
            buildRoutingActionRemoteRoundRobin(proxyRoutingAction, actionRemoteRoundRobin);
            raList.add(proxyRoutingAction);

            return raList;
        }
        else if (ConfigUtils.isRemotePreferredAction(routingAction))
        {
            var raList = new ArrayList<ProxyRoutingAction>();
            var actionRouteRemotePreferred = routingAction.getActionRouteRemotePreferred();

            var suffix = CommonConfigUtils.buildClusterNameSuffix(Optional.ofNullable(actionRouteRemotePreferred.getLastResortNfPoolRef()));
            var proxyRoutingAction = CommonConfigUtils.createRoutingActionBase(actionRouteRemotePreferred, suffix);

            if (actionRouteRemotePreferred.getFailoverProfileRef() != null)
            {
                var addHeaderAction = CommonConfigUtils.createRoutingActionAddHeader(actionRouteRemotePreferred.getFailoverProfileRef());
                raList.add(addHeaderAction);

                var failoverProf = Utils.getByName(scpInst.getFailoverProfile(), actionRouteRemotePreferred.getFailoverProfileRef());
                proxyRoutingAction.setNumOfRemoteReselections(failoverProf.getTargetNfPoolReselectsMax());
                proxyRoutingAction.setNumOfRemoteRetries(failoverProf.getPreferredHostRetriesMax());
            }
            else
            {
                var defaultFp = ConfigHelper.getScpDefaultFailoverProfile(Optional.ofNullable(actionRouteRemotePreferred.getLastResortNfPoolRef()).isPresent());
                proxyRoutingAction.setNumOfRemoteReselections(defaultFp.getTargetNfPoolReselectsMax());
                proxyRoutingAction.setNumOfRemoteRetries(defaultFp.getPreferredHostRetriesMax());
            }

            if (actionRouteRemotePreferred.getPreserveIfIndirectRouting() != null)
            {
                var value = actionRouteRemotePreferred.getPreserveIfIndirectRouting() == PreserveIfIndirectRouting.ABSOLUTE_URI_PATH ? PreserveIfIndirect.ABSOLUTE_PATH
                                                                                                                                     : PreserveIfIndirect.TARGET_API_ROOT;
                proxyRoutingAction.setPreserveIfIndirect(value);
            }

            if (actionRouteRemotePreferred.getPreserveDiscParamIfIndirect() != null)
            {
                if (actionRouteRemotePreferred.getPreserveDiscParamIfIndirect().getPreserveAll() != null)
                    proxyRoutingAction.setPreserveAllDiscParams(true);
                else if (actionRouteRemotePreferred.getPreserveDiscParamIfIndirect().getPreserveSelected() != null
                         && !actionRouteRemotePreferred.getPreserveDiscParamIfIndirect().getPreserveSelected().getName().isEmpty())
                    proxyRoutingAction.setPreserveSelectedDiscParams(actionRouteRemotePreferred.getPreserveDiscParamIfIndirect()
                                                                                               .getPreserveSelected()
                                                                                               .getName());
            }
            proxyRoutingAction.setKeepAuthorityHeader(false);

            buildRoutingActionRouteRemotePreferred(proxyRoutingAction, actionRouteRemotePreferred);
            raList.add(proxyRoutingAction);

            return raList;
        }
        else if (CommonConfigUtils.isPreferredRoutingRule(routingAction))
        {
            var raList = new ArrayList<ProxyRoutingAction>();
            var actionPreferred = routingAction.getActionRoutePreferred();

            if (actionPreferred.getFailoverProfileRef() != null)
            {
                var addHeaderAction = CommonConfigUtils.createRoutingActionAddHeader(actionPreferred.getFailoverProfileRef());
                raList.add(addHeaderAction);
            }

            var suffix = CommonConfigUtils.buildClusterNameSuffix(Optional.ofNullable(actionPreferred.getLastResortNfPoolRef()));
            var proxyRoutingAction = CommonConfigUtils.createRoutingActionBase(actionPreferred, actionPreferred.getLastResortNfPoolRef() != null ? suffix : "");

            if (actionPreferred.getPreserveIfIndirectRouting() != null)
            {
                var value = actionPreferred.getPreserveIfIndirectRouting() == PreserveIfIndirectRouting.ABSOLUTE_URI_PATH ? PreserveIfIndirect.ABSOLUTE_PATH
                                                                                                                          : PreserveIfIndirect.TARGET_API_ROOT;
                proxyRoutingAction.setPreserveIfIndirect(value);
            }
            // The discovery parameters of the received request to preserve in case of
            // indirect routing.
            // By default no parameters are preserved
            if (actionPreferred.getPreserveDiscParamIfIndirect() != null)
            {

                if (actionPreferred.getPreserveDiscParamIfIndirect().getPreserveAll() != null)
                {
                    proxyRoutingAction.setPreserveAllDiscParams(true);
                }
                else if (actionPreferred.getPreserveDiscParamIfIndirect().getPreserveSelected() != null
                         && !actionPreferred.getPreserveDiscParamIfIndirect().getPreserveSelected().getName().isEmpty())
                {
                    proxyRoutingAction.setPreserveSelectedDiscParams(actionPreferred.getPreserveDiscParamIfIndirect().getPreserveSelected().getName());
                }
            }

            var keepAuthorityHeader = actionPreferred.getKeepAuthorityHeader() != null;
            proxyRoutingAction.setKeepAuthorityHeader(keepAuthorityHeader);
            buildRoutingActionPreferred(proxyRoutingAction, actionPreferred);
            raList.add(proxyRoutingAction);

            return raList;
        }
        else if (isSlfRoutingRule(routingAction))
        {
            // first create the SLF action
            var actionSlf = routingAction.getActionSlfLookup();

            var slfLookup = Utils.getByName(scpInst.getSlfLookupProfile(), actionSlf.getSlfLookupProfileRef());
            var proxySlfLookup = createProxySlfLookupData(slfLookup, scpInst);

            var proxyRoutingAction = new ProxyRoutingAction().setSlfLookup(proxySlfLookup);

            // then create the modify var action

            var modifyVarRoutingAction = ConfigUtils.createRoutingActionModifyVariable(proxySlfLookup.getResult(),
                                                                                       CommonConfigUtils.getKvTableNameForSlf(rrName,
                                                                                                                              actionSlf.getSlfLookupProfileRef()),
                                                                                       proxySlfLookup.getResult());

            return List.of(proxyRoutingAction, modifyVarRoutingAction);
        }
        else if (routingAction.getActionRejectMessage() != null)
        {
            var actionRejectMessage = routingAction.getActionRejectMessage();
            var proxyActionRejectMessage = CommonConfigUtils.createRoutingActionRejectMessage(actionRejectMessage.getStatus(),
                                                                                              Optional.ofNullable(actionRejectMessage.getTitle()),
                                                                                              Optional.ofNullable(actionRejectMessage.getDetail()),
                                                                                              Optional.ofNullable(actionRejectMessage.getCause()),
                                                                                              actionRejectMessage.getFormat().value());
            return List.of(proxyActionRejectMessage);
        }
        else if (routingAction.getActionLog() != null)
        {
            var actionLog = routingAction.getActionLog();

            var proxyActionLog = CommonConfigUtils.createRoutingActionLog(actionLog.getText(),
                                                                          actionLog.getLogLevel().value(),
                                                                          actionLog.getMaxLogMessageLength());
            return List.of(proxyActionLog);
        }
        else if (routingAction.getActionDropMessage() != null)
        {
            var actionDropMessage = routingAction.getActionDropMessage() != null;
            var proxyActionDropMessage = CommonConfigUtils.createRoutingActionDropMessage(Optional.ofNullable(actionDropMessage));
            return List.of(proxyActionDropMessage);
        }
        else
        {
            throw new BadConfigurationException("Unknown route action set for routing action {}", routingAction.getName());
        }
    }

    public static boolean isSlfRoutingRule(final RoutingAction routingAction)
    {
        return routingAction.getActionSlfLookup() != null;
    }

    public static boolean isNfDiscoveryRoutingRule(final RoutingAction routingAction)
    {
        return routingAction.getActionNfDiscovery() != null;
    }

    /**
     * @param routingAction
     * @return
     */
    public static boolean isTerminalAction(final RoutingAction routingAction)
    {
        return routingAction.getActionRejectMessage() != null || routingAction.getActionRouteStrict() != null || routingAction.getActionRoutePreferred() != null
               || routingAction.getActionRouteRoundRobin() != null || routingAction.getActionDropMessage() != null
               || routingAction.getActionRouteRemoteRoundRobin() != null || routingAction.getActionRouteRemotePreferred() != null;
    }

    public static boolean isNfDiscoveryAction(final RoutingAction routingAction)
    {
        return routingAction.getActionNfDiscovery() != null;
    }

    public static boolean isRemoteRoundRobinAction(final RoutingAction routingAction)
    {
        return routingAction.getActionRouteRemoteRoundRobin() != null;
    }

    public static boolean isRemotePreferredAction(final RoutingAction routingAction)
    {
        return routingAction.getActionRouteRemotePreferred() != null;
    }

    /**
     * @param slfLookup
     * @return ProxySlfLookup object
     */
    private static ProxyActionSlfLookup createProxySlfLookupData(SlfLookupProfile slfLookup,
                                                                 NfInstance scpInst)
    {
        var pxMessageData = createProxyMessageData(Utils.getByName(scpInst.getMessageData(), slfLookup.getMessageDataRef()));

        var identityVar = pxMessageData.getVariableName()
                                       .or(() -> extractVarName(pxMessageData.getExtractorRegex()))
                                       .orElseThrow(() -> new BadConfigurationException("No named capture group defined in the extractor regex {} in the message-data referenced by the slf-lookup-profile",
                                                                                        pxMessageData.getExtractorRegex().get()));

        return new ProxyActionSlfLookup(slfLookup.getName(),
                                        slfLookup.getRequesterNfType(),
                                        slfLookup.getTargetNfType(),
                                        slfLookup.getNrfGroupRef(),
                                        Enum.valueOf(IdentityType.class, slfLookup.getIdentityType().value().toUpperCase()),
                                        slfLookup.getRequestTimeout(),
                                        identityVar,
                                        slfLookup.getResultVariableName(),
                                        slfLookup.getRoutingCaseIdentityNotFound(),
                                        slfLookup.getRoutingCaseDestinationUnknown(),
                                        slfLookup.getRoutingCaseIdentityMissing(),
                                        slfLookup.getRoutingCaseLookupFailure());
    }

    /**
     * @param extractorRegex
     * @return
     */
    private static Optional<String> extractVarName(Optional<String> extractorRegex)
    {
        return extractorRegex.flatMap(str ->
        {
            var pattern = Pattern.compile("\\?P<(.+?)>");
            var matcher = pattern.matcher(str);
            if (!matcher.find())
            {
                return Optional.empty();
            }

            return Optional.of(matcher.group(1));
        });
    }

    /**
     * @param messageData
     * @return ProxyFilterData object
     */
    public static ProxyFilterData createProxyMessageData(MessageDatum messageData)
    {
        return new ProxyFilterData(messageData.getName(),
                                   messageData.getVariableName(),
                                   messageData.getExtractorRegex(),
                                   messageData.getPath() != null,
                                   messageData.getBodyJsonPointer(),
                                   messageData.getHeader(),
                                   messageData.getRequestHeader(),
                                   messageData.getResponseHeader());
    }

    /**
     * @param proxyRoutingAction
     * @param actionPreferred
     */
    private static void buildRoutingActionPreferred(ProxyRoutingAction proxyRoutingAction,
                                                    ActionRoutePreferred actionPreferred)
    {
        CommonConfigUtils.buildRoutingActionTarget(proxyRoutingAction, actionPreferred);
        CommonConfigUtils.buildRoutingActionLastResort(proxyRoutingAction, actionPreferred.getLastResortNfPoolRef());
    }

    /**
     * @param proxyRoutingAction
     * @param actionRoundRobin
     */
    private static void buildRoutingActionRoundRobin(ProxyRoutingAction proxyRoutingAction,
                                                     ActionRouteRoundRobin actionRoundRobin)
    {
        CommonConfigUtils.buildRoutingActionLastResort(proxyRoutingAction, actionRoundRobin.getLastResortNfPoolRef());
    }

    /**
     * @param proxyRoutingAction
     * @param actionRemoteRoundRobin
     */
    private static void buildRoutingActionRemoteRoundRobin(ProxyRoutingAction proxyRoutingAction,
                                                           ActionRouteRemoteRoundRobin actionRemoteRoundRobin)
    {
        CommonConfigUtils.buildRoutingActionLastResort(proxyRoutingAction, actionRemoteRoundRobin.getLastResortNfPoolRef());
    }

    private static void buildRoutingActionRouteRemotePreferred(ProxyRoutingAction proxyRoutingAction,
                                                               ActionRouteRemotePreferred actionRemotePreferred)
    {
        proxyRoutingAction.setFromHeader(CommonConfigUtils.TARGET_API_ROOT_HEADER);
        CommonConfigUtils.buildRoutingActionLastResort(proxyRoutingAction, actionRemotePreferred.getLastResortNfPoolRef());
    }

    /**
     * @param proxyRoutingAction
     * @param actionStrict
     */
    private static void buildRoutingActionStrict(ProxyRoutingAction proxyRoutingAction,
                                                 ActionRouteStrict actionStrict)
    {
        CommonConfigUtils.buildRoutingActionTarget(proxyRoutingAction, actionStrict);
    }

    private static ProxyRoutingAction createRoutingActionModifyVariable(String name,
                                                                        String tableName,
                                                                        String key)
    {
        var proxyRoutingAction = new ProxyRoutingAction();
        proxyRoutingAction.setActionModifyVariable(Optional.of(new ProxyActionModifyVariable(name, tableName, key)));
        return proxyRoutingAction;
    }

    public static ProxyRoutingAction createRoutingActionNfDiscovery(final String nrfGroup,
                                                                    final Integer requestTimeout,
                                                                    final IPFamily endpointIpFamily,
                                                                    final List<String> useDiscoveryParameter,
                                                                    final Boolean useAllDiscoveryParameters,
                                                                    final List<Triplet<String, Boolean, String>> addDiscoveryParameter,
                                                                    final String preferredHostVar,
                                                                    final String nfSetidVar)

    {
        var proxyRoutingAction = new ProxyRoutingAction();

        proxyRoutingAction.setActionNfDiscovery(Optional.of(new ProxyActionNfDiscovery(nrfGroup,
                                                                                       requestTimeout,
                                                                                       endpointIpFamily,
                                                                                       useDiscoveryParameter,
                                                                                       useAllDiscoveryParameters,
                                                                                       addDiscoveryParameter,
                                                                                       preferredHostVar,
                                                                                       nfSetidVar)));
        return proxyRoutingAction;
    }

    public static ProxyRoutingAction createRoutingActionRRR(final ActionRouteRemoteRoundRobin ra,
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

    public static void createOutRequestScreeningKvTableForPools(ProxySeppFilter scpFilter,
                                                                final NfInstance scpInst,
                                                                final String tableName)
    {
        var kvTable = new HashMap<String, String>();

        if (!scpInst.getNfPool().isEmpty())
        {
            scpInst.getNfPool()
                   .stream()
                   .filter(pool -> pool.getOutRequestScreeningCaseRef() != null)
                   .forEach(pool -> kvTable.put(pool.getName(), pool.getOutRequestScreeningCaseRef()));
        }

        if (kvTable.size() > 0)
        {
            scpFilter.addIntKvTable(tableName, kvTable);
        }
    }

    public static void createInResponseScreeningKvTableForPools(ProxySeppFilter scpFilter,
                                                                final NfInstance scpInst,
                                                                final String tableName)
    {
        var kvTable = new HashMap<String, String>();

        if (!scpInst.getNfPool().isEmpty())
        {
            scpInst.getNfPool()
                   .stream()
                   .filter(pool -> pool.getInResponseScreeningCaseRef() != null)
                   .forEach(pool -> kvTable.put(pool.getName(), pool.getInResponseScreeningCaseRef()));
        }

        if (kvTable.size() > 0)
        {
            scpFilter.addIntKvTable(tableName, kvTable);
        }
    }

    public static Set<String> getReferencedOutRequestScreeningCases(NfInstance scpInst)
    {
        var outReqScreeningRef = new HashSet<String>();

        if (!scpInst.getNfPool().isEmpty())
        {
            scpInst.getNfPool()
                   .stream()
                   .filter(pool -> pool.getOutRequestScreeningCaseRef() != null)
                   .forEach(pool -> outReqScreeningRef.add(pool.getOutRequestScreeningCaseRef()));
        }

        return outReqScreeningRef;
    }

    public static Set<String> getReferencedInResponseScreeningCases(NfInstance scpInst)
    {
        var inRespScreeningRef = new HashSet<String>();

        if (!scpInst.getNfPool().isEmpty())
        {
            scpInst.getNfPool()
                   .stream()
                   .filter(pool -> pool.getInResponseScreeningCaseRef() != null)
                   .forEach(pool -> inRespScreeningRef.add(pool.getInResponseScreeningCaseRef()));
        }

        return inRespScreeningRef;
    }

    public static Set<String> getReferencedRoutingCases(NfInstance scpInst,
                                                        IfNetwork network)
    {
        var routingRef = new HashSet<String>();

        routingRef.add(network.getRoutingCaseRef());

        // routing cases referenced in slf lookup profile
        var slfLookupProfileRef = scpInst.getRoutingCase()
                                         .stream()
                                         .filter(rc -> routingRef.contains(rc.getName()))
                                         .flatMap(rc -> rc.getRoutingRule().stream())
                                         .flatMap(rr -> rr.getRoutingAction().stream())
                                         .filter(ConfigUtils::isSlfRoutingRule)
                                         .map(ra -> ra.getActionSlfLookup().getSlfLookupProfileRef())
                                         .collect(Collectors.toSet());

        slfLookupProfileRef.stream().map(ref -> Utils.getByName(scpInst.getSlfLookupProfile(), ref)).forEach(slf -> addSlfRoutingRef(routingRef, slf));

        return routingRef;
    }

    public static Set<String> addSlfRoutingRef(Set<String> slfRoutingRef,
                                               SlfLookupProfile slf)
    {
        if (slf.getRoutingCaseDestinationUnknown() != null
            && !slf.getRoutingCaseDestinationUnknown().equals(ProxyActionSlfLookup.DEFAULT_ROUTING_CASE_DESTINATION_UNKNOWN))
        {
            slfRoutingRef.add(slf.getRoutingCaseDestinationUnknown());
        }

        if (slf.getRoutingCaseIdentityMissing() != null
            && !slf.getRoutingCaseIdentityMissing().equals(ProxyActionSlfLookup.DEFAULT_ROUTING_CASE_IDENTITY_MISSING))
        {
            slfRoutingRef.add(slf.getRoutingCaseIdentityMissing());
        }

        if (slf.getRoutingCaseIdentityNotFound() != null
            && !slf.getRoutingCaseIdentityNotFound().equals(ProxyActionSlfLookup.DEFAULT_ROUTING_CASE_IDENTITY_NOT_FOUND))
        {
            slfRoutingRef.add(slf.getRoutingCaseIdentityNotFound());
        }

        if (slf.getRoutingCaseLookupFailure() != null && !slf.getRoutingCaseLookupFailure().equals(ProxyActionSlfLookup.DEFAULT_ROUTING_CASE_LOOKUP_FAILURE))
        {
            slfRoutingRef.add(slf.getRoutingCaseLookupFailure());
        }

        return slfRoutingRef;
    }

    /**
     * Checks whether Slf is present in the Routing Actions of a Routing Rule
     *
     * @param rr
     * @return
     */
    public static boolean isSlfPresent(RoutingRule rr)
    {
        return rr.getRoutingAction().stream().anyMatch(ConfigUtils::isSlfRoutingRule);
    }

    public static void createKvTableForSlf(RoutingRule rr,
                                           ProxySeppFilter scpFilter,
                                           NfInstance scpInst)
    {
        var slfRoutingRuleRa = rr.getRoutingAction().stream().filter(ConfigUtils::isSlfRoutingRule).findFirst();
        var ruleKvTable = new HashMap<String, String>();

        rr.getRoutingAction().stream().filter(Predicate.not(ConfigUtils::isSlfRoutingRule)).forEach(ra ->
        {
            if (CommonConfigUtils.isStrictRoutingRule(ra))
            {
                var rrStrict = ra.getActionRouteStrict();

                populateKvTableForPool(rrStrict.getTargetNfPool(), ruleKvTable, Optional.empty(), scpInst);
            }
            else if (CommonConfigUtils.isRoundRobinRule(ra))
            {
                var rrRoundRobin = ra.getActionRouteRoundRobin();
                var lastResortPool = CommonConfigUtils.tryGetLastResortPool(ra.getActionRouteRoundRobin().getLastResortNfPoolRef(), ra, scpInst);
                populateKvTableForPool(rrRoundRobin.getTargetNfPool(),
                                       ruleKvTable,
                                       Optional.ofNullable(CommonConfigUtils.buildClusterNameSuffix(lastResortPool.map(IfNfPool::getName))),
                                       scpInst);
            }
            else if (isRemoteRoundRobinAction(ra))
            {
                var rrrRoundRobin = ra.getActionRouteRemoteRoundRobin();
                var lastResortPool = CommonConfigUtils.tryGetLastResortPool(ra.getActionRouteRoundRobin().getLastResortNfPoolRef(), ra, scpInst);

                populateKvTableForPool(rrrRoundRobin.getTargetNfPool(),
                                       ruleKvTable,
                                       Optional.ofNullable(CommonConfigUtils.buildClusterNameSuffix(lastResortPool.map(IfNfPool::getName))),
                                       scpInst);
            }
            else if (isRemotePreferredAction(ra))
            {
                var rrPreferred = ra.getActionRouteRemotePreferred();
                var lastResortPool = CommonConfigUtils.tryGetLastResortPool(ra.getActionRoutePreferred().getLastResortNfPoolRef(), ra, scpInst);
                populateKvTableForPool(rrPreferred.getTargetNfPool(),
                                       ruleKvTable,
                                       Optional.ofNullable(CommonConfigUtils.buildClusterNameSuffix(lastResortPool.map(IfNfPool::getName))),
                                       scpInst);
            }

            else if (CommonConfigUtils.isPreferredRoutingRule(ra))
            {
                var rrPreferred = ra.getActionRoutePreferred();
                var lastResortPool = CommonConfigUtils.tryGetLastResortPool(ra.getActionRoutePreferred().getLastResortNfPoolRef(), ra, scpInst);
                populateKvTableForPool(rrPreferred.getTargetNfPool(),
                                       ruleKvTable,
                                       Optional.ofNullable(CommonConfigUtils.buildClusterNameSuffix(lastResortPool.map(IfNfPool::getName))),
                                       scpInst);
            }
        });

        slfRoutingRuleRa.ifPresent(slfRa -> scpFilter.addKvTable(CommonConfigUtils.getKvTableNameForSlf(rr.getName(),
                                                                                                        slfRa.getActionSlfLookup().getSlfLookupProfileRef()),
                                                                 ruleKvTable));
    }

    private static void populateKvTableForPool(TargetNfPool targetPool,
                                               HashMap<String, String> kvTable,
                                               Optional<String> clusterSuffix,
                                               NfInstance scpInst)
    {
        if (targetPool != null)
        {
            StringBuilder ss = new StringBuilder();
            if (targetPool.getNfPoolRef() != null)
            {
                ss.append(targetPool.getNfPoolRef());
                clusterSuffix.ifPresent(val -> ss.append(val));
                var poolName = targetPool.getNfPoolRef();
                kvTable.put(poolName, ss.toString());
            }
            else if (targetPool.getVarName() != null)
            {
                scpInst.getNfPool().stream().map(IfNamedListItem::getName).forEach(poolName ->
                {
                    ss.replace(0, ss.length(), poolName);
                    clusterSuffix.ifPresent(val -> ss.append(val));
                    kvTable.put(poolName, ss.toString());
                });
            }
        }
    }

    /**
     * Check if the given pool does not include static scp instance data nor nf pool
     * discovery. Such an empty pool is used by the dynamic forwarding function
     * where envoy resolves the DNS address of the target host. TMO is currently
     * using this functionality.
     *
     * @param routingAction
     * @param scpInst
     * @return
     */
    private static boolean isEmptyDynFwdPool(RoutingAction routingAction,
                                             NfInstance scpInst)
    {
        var actionStrict = routingAction.getActionRouteStrict();
        if (actionStrict == null)
            return false;
        if (actionStrict.getTargetNfPool() == null)
            return false;

        var poolReference = actionStrict.getTargetNfPool().getNfPoolRef();
        if (poolReference == null)
            return false;

        var pool = Utils.getByName(scpInst.getNfPool(), poolReference);
        if (pool == null)
        {
            throw new BadConfigurationException("Nf-pool {} could not be found for routing-action {}",
                                                actionStrict.getTargetNfPool().getNfPoolRef(),
                                                routingAction.getName());
        }

        var dynFwdPool = false;
        if ((pool.getStaticScpInstanceDataRef() == null || pool.getStaticScpInstanceDataRef().isEmpty())
            && (pool.getStaticSeppInstanceDataRef() == null || pool.getStaticSeppInstanceDataRef().isEmpty())
            && (pool.getNfPoolDiscovery() == null || pool.getNfPoolDiscovery().isEmpty()))
            dynFwdPool = true;

        return dynFwdPool;

    }

    static class Pair
    {
        public final String first;
        public final Integer second;

        Pair(String first,
             Integer second)
        {
            this.first = first;
            this.second = second;
        }

        public static Pair of(String first,
                              Integer second)
        {
            return new Pair(first, second);
        }
    }

    /**
     * @return List<ProxyFilterCase>
     */
    public static List<ProxyFilterCase> createDefaultRoutingCasesForSlf()
    {
        var rcDestinationUnknown = new ProxyFilterCase(ProxyActionSlfLookup.DEFAULT_ROUTING_CASE_DESTINATION_UNKNOWN);
        var rejMsgDestinationUnknown = new ProxyActionRejectMessage(500,
                                                                    Optional.of("Destination unknown"),
                                                                    Optional.of("SLF interrogation result does not exist."),
                                                                    Optional.empty(),
                                                                    "json");
        var raDestinationUnknown = new ProxyRoutingAction().setActionRejectMessage(Optional.of(rejMsgDestinationUnknown));
        var rrDestinationUnknown = new ProxyFilterRule("#!_#default-routing-rule-destination-unknown",
                                                       ConditionParser.parse("true"),
                                                       RoutingBehaviour.UNRECOGNIZED,
                                                       List.of(raDestinationUnknown));
        rcDestinationUnknown.addFilterRule(rrDestinationUnknown);

        var rcIdentityMissing = new ProxyFilterCase(ProxyActionSlfLookup.DEFAULT_ROUTING_CASE_IDENTITY_MISSING);
        var rejMsgIdentityMissing = new ProxyActionRejectMessage(400,
                                                                 Optional.of("Missing identity"),
                                                                 Optional.of("Identity cannot be retrieved from the request message."),
                                                                 Optional.empty(),
                                                                 "json");
        var raIdentityMissing = new ProxyRoutingAction().setActionRejectMessage(Optional.of(rejMsgIdentityMissing));
        var rrIdentityMissing = new ProxyFilterRule("#!_#default-routing-rule-identity-missing",
                                                    ConditionParser.parse("true"),
                                                    RoutingBehaviour.UNRECOGNIZED,
                                                    List.of(raIdentityMissing));
        rcIdentityMissing.addFilterRule(rrIdentityMissing);

        var rcIdentityNotFound = new ProxyFilterCase(ProxyActionSlfLookup.DEFAULT_ROUTING_CASE_IDENTITY_NOT_FOUND);
        var rejMsgIdentityNotFound = new ProxyActionRejectMessage(400,
                                                                  Optional.of("Identiy not found"),
                                                                  Optional.of("SUPI/GPSI cannot be found in the SLF lookup."),
                                                                  Optional.empty(),
                                                                  "json");
        var raIdentityNotFound = new ProxyRoutingAction().setActionRejectMessage(Optional.of(rejMsgIdentityNotFound));
        var rrIdentityNotFound = new ProxyFilterRule("#!_#default-routing-rule-identity-not-found",
                                                     ConditionParser.parse("true"),
                                                     RoutingBehaviour.UNRECOGNIZED,
                                                     List.of(raIdentityNotFound));
        rcIdentityNotFound.addFilterRule(rrIdentityNotFound);

        var rcLookupFailure = new ProxyFilterCase(ProxyActionSlfLookup.DEFAULT_ROUTING_CASE_LOOKUP_FAILURE);
        var rejMsgLookupFailure = new ProxyActionRejectMessage(500,
                                                               Optional.of("Lookup failure"),
                                                               Optional.of("SLF interrogation was failed."),
                                                               Optional.empty(),
                                                               "json");
        var raLookupFailure = new ProxyRoutingAction().setActionRejectMessage(Optional.of(rejMsgLookupFailure));
        var rrLookupFailure = new ProxyFilterRule("#!_#default-routing-rule-lookup-failure",
                                                  ConditionParser.parse("true"),
                                                  RoutingBehaviour.UNRECOGNIZED,
                                                  List.of(raLookupFailure));
        rcLookupFailure.addFilterRule(rrLookupFailure);

        return List.of(rcDestinationUnknown, rcIdentityMissing, rcIdentityNotFound, rcLookupFailure);
    }

    public static boolean isRateLimitConfigured(NfInstance seppInst)
    {
        for (var ownNw : seppInst.getOwnNetwork())
        {
            if (!ownNw.getGlobalIngressRateLimitProfileRef().isEmpty())
                return true;
        }
        return false;
    }

    public static void copySlfMessageDataToRoutingCase(RoutingRule rr,
                                                       NfInstance scpInst,
                                                       ProxyFilterCase filterCase)
    {
        var slfLookupRef = rr.getRoutingAction()
                             .stream()
                             .map(RoutingAction::getActionSlfLookup)
                             .filter(Objects::nonNull)
                             .map(ActionSlfLookup::getSlfLookupProfileRef)
                             .findFirst();

        var slfLookup = slfLookupRef.map(ref -> Utils.getByName(scpInst.getSlfLookupProfile(), ref));

        var messageDataRef = slfLookup.map(SlfLookupProfile::getMessageDataRef);
        var messageData = messageDataRef.map(ref -> Utils.getByName(scpInst.getMessageData(), ref));
        messageData.ifPresent(md ->
        {
            var name = md.getName();
            if (filterCase.getFilterData().stream().map(ProxyFilterData::getName).collect(Collectors.toList()).contains(name))
            {
                return;
            }

            filterCase.getFilterData().add(createProxyMessageData(md));
        });
    }

    private static IPFamily selectEndpointIpFamily(ActionNfDiscovery actionNfDiscovery)
    {

        var endpointIpFamily = actionNfDiscovery.getEndpointIpFamily().stream().toList();

        if (endpointIpFamily.size() == 2)
        {
            return IPFamily.DualStack;
        }
        else if (endpointIpFamily.size() == 1)
        {
            if (endpointIpFamily.get(0).equals(EndpointIpFamily.IPV_4))
            {
                return IPFamily.IPv4;
            }
            else if (endpointIpFamily.get(0).equals(EndpointIpFamily.IPV_6))
            {
                return IPFamily.IPv6;
            }
        }
        return IPFamily.IPv4;
    }
}
