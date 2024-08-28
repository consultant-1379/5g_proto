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
 * Created on: Mar 31, 2021
 *     Author: eaoknkr
 */

package com.ericsson.sc.sepp.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cnal.internal.nrlf.ratelimiting.BucketConfig;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.custom.NFProfile;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.custom.SearchResult;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.PlmnId;
import com.ericsson.sc.configutil.CommonConfigUtils;
import com.ericsson.sc.configutil.CommonConfigUtils.RateLimiting.RateLimitType;
import com.ericsson.sc.configutil.CommonConfigUtils.RateLimiting.RateLimitedEntity;
import com.ericsson.sc.configutil.ServiceConfig;
import com.ericsson.sc.expressionparser.ConditionParser;
import com.ericsson.sc.glue.IfFailoverProfile;
import com.ericsson.sc.glue.IfNetwork;
import com.ericsson.sc.glue.IfNfInstance;
import com.ericsson.sc.glue.IfNfPool;
import com.ericsson.sc.glue.IfStaticNfInstanceDatum;
import com.ericsson.sc.glue.IfTypedNfInstance;
import com.ericsson.sc.nlf.client.NlfConfigurator;
import com.ericsson.sc.proxyal.proxyconfig.ProxyTls;
import com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig.ProxySeppFilter;
import com.ericsson.sc.proxyal.proxyconfig.proxymessagescreenigconfig.ProxyScreeningAction;
import com.ericsson.sc.proxyal.proxyconfig.proxymessagescreenigconfig.ProxyScreeningAction.ScreeningActionType;
import com.ericsson.sc.proxyal.proxyconfig.proxymessagescreenigconfig.ProxyScreeningRule;
import com.ericsson.sc.proxyal.proxyconfig.proxymessagevalidationconfig.ProxyActionOnFailure;
import com.ericsson.sc.proxyal.proxyconfig.proxymessagevalidationconfig.ProxyActionOnFailure.ActionOnFailureType;
import com.ericsson.sc.proxyal.proxyconfig.proxymessagevalidationconfig.ProxyCheckHeaders;
import com.ericsson.sc.proxyal.proxyconfig.proxymessagevalidationconfig.ProxyCheckHeaders.policyType;
import com.ericsson.sc.proxyal.proxyconfig.proxymessagevalidationconfig.ProxyCheckJsonDepth;
import com.ericsson.sc.proxyal.proxyconfig.proxymessagevalidationconfig.ProxyCheckJsonLeaves;
import com.ericsson.sc.proxyal.proxyconfig.proxymessagevalidationconfig.ProxyCheckJsonSyntax;
import com.ericsson.sc.proxyal.proxyconfig.proxymessagevalidationconfig.ProxyCheckMessageBytes;
import com.ericsson.sc.proxyal.proxyconfig.proxymessagevalidationconfig.ProxyCheckServiceOperations;
import com.ericsson.sc.proxyal.proxyconfig.proxymessagevalidationconfig.ProxyMessageSelector;
import com.ericsson.sc.proxyal.proxyconfig.proxymessagevalidationconfig.ProxyMessageValidation;
import com.ericsson.sc.proxyal.proxyconfig.proxyroutingconfig.IfProxyTopologyHiding;
import com.ericsson.sc.proxyal.proxyconfig.proxyroutingconfig.ProxyEncryptionProfile;
import com.ericsson.sc.proxyal.proxyconfig.proxyroutingconfig.ProxyFqdnHiding;
import com.ericsson.sc.proxyal.proxyconfig.proxyroutingconfig.ProxyIpAddressHiding;
import com.ericsson.sc.proxyal.proxyconfig.proxyroutingconfig.ProxyIpAddressHiding.Action;
import com.ericsson.sc.proxyal.proxyconfig.proxyroutingconfig.ProxyPseudoSearchResult;
import com.ericsson.sc.proxyal.proxyconfig.proxyroutingconfig.ProxyRoamingPartner;
import com.ericsson.sc.proxyal.proxyconfig.proxyroutingconfig.ProxyRoutingAction;
import com.ericsson.sc.rlf.client.RlfConfigurator;
import com.ericsson.sc.sepp.model.ActionRoutePreferred;
import com.ericsson.sc.sepp.model.ActionRouteRoundRobin;
import com.ericsson.sc.sepp.model.ActionRouteStrict;
import com.ericsson.sc.sepp.model.AsymmetricKey;
import com.ericsson.sc.sepp.model.CallbackUriDefault;
import com.ericsson.sc.sepp.model.CallbackUrus;
import com.ericsson.sc.sepp.model.EricssonSeppSeppFunction;
import com.ericsson.sc.sepp.model.ExternalNetwork;
import com.ericsson.sc.sepp.model.FailoverProfile;
import com.ericsson.sc.sepp.model.FqdnMapping;
import com.ericsson.sc.sepp.model.FqdnScrambling;
import com.ericsson.sc.sepp.model.FqdnScramblingTable;
import com.ericsson.sc.sepp.model.IpAddressHiding;
import com.ericsson.sc.sepp.model.NfInstance;
import com.ericsson.sc.sepp.model.NfPool;
import com.ericsson.sc.sepp.model.OwnNetwork;
import com.ericsson.sc.sepp.model.PseudoSearchResult;
import com.ericsson.sc.sepp.model.RequiredForNfType;
import com.ericsson.sc.sepp.model.RespondWithError.Format;
import com.ericsson.sc.sepp.model.RetryCondition;
import com.ericsson.sc.sepp.model.RoamingPartner;
import com.ericsson.sc.sepp.model.RoamingPartner.SupportsTargetApiroot;
import com.ericsson.sc.sepp.model.RoutingAction;
import com.ericsson.sc.sepp.model.RoutingCase;
import com.ericsson.sc.sepp.model.ScramblingKey;
import com.ericsson.sc.sepp.model.ServiceAddress;
import com.ericsson.sc.sepp.model.TargetRoamingPartner;
import com.ericsson.sc.sepp.model.TopologyHiding;
import com.ericsson.sc.sepp.model.TopologyHidingWithAdminState;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.common.Rdn;
import com.ericsson.utilities.common.Utils;
import com.ericsson.utilities.exceptions.BadConfigurationException;
import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.re2j.Pattern;

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.MessageBodyType;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.PlmnIdInfo;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.PreserveIfIndirect;

public class ConfigUtils
{
    /**
     * Watermark [%] of the lowest priority possible (31);
     */
    private static final Logger log = LoggerFactory.getLogger(ConfigUtils.class);
    private static final JsonMapper om = Jackson.om();

    private static final String SCR_RULE_UNSUCCESSFUL_MAPPING = "UnSuccessFullOpMapping";
    private static final String SCR_RULE_UNSUCCESSFUL_SCRAMBLING = "UnSuccessFullOpScrambling";
    private static final String SCR_ACTION_UNSUCCESSFUL = "UnSuccessFullAction";

    private static final String CR_PREFIX = EnvVars.get("GLOBAL_ERIC_SEPP_NAME") + "-sepp-extcert-";

    /*
     * NOTE: All headers set in the allowed request and response check validation
     * should be defined with lowercase in envoy configuration
     */
    private static final List<String> defaultRequestHeaders = List.of("Accept",
                                                                      "Accept-Encoding",
                                                                      "Content-Length",
                                                                      "Content-Type",
                                                                      "Via",
                                                                      "3gpp-Sbi-Message-Priority",
                                                                      "3gpp-Sbi-Callback",
                                                                      "3gpp-Sbi-Target-apiRoot",
                                                                      "3gpp-Sbi-Routing-Binding",
                                                                      "3gpp-Sbi-Binding",
                                                                      "3gpp-Sbi-Oci",
                                                                      "3gpp-Sbi-Client-Credentials",
                                                                      "3gpp-Sbi-Source-NF-Client-Credentials",
                                                                      "3gpp-Sbi-Max-Forward-Hops",
                                                                      "3gpp-Sbi-Originating-Network-Id",
                                                                      "3gpp-Sbi-NF-Peer-Info",
                                                                      ":method",
                                                                      ":scheme",
                                                                      ":authority",
                                                                      ":path",
                                                                      "x-forwarded-proto",
                                                                      "x-request-id",
                                                                      "user-agent");
    private static final List<String> defaultResponseHeaders = List.of("Content-Length",
                                                                       "Content-Type",
                                                                       "Content-Encoding",
                                                                       "Via",
                                                                       "Server",
                                                                       "3gpp-Sbi-Message-Priority",
                                                                       "3gpp-Sbi-Target-apiRoot",
                                                                       "3gpp-Sbi-Binding",
                                                                       "3gpp-Sbi-Producer-Id",
                                                                       "3gpp-Sbi-Oci",
                                                                       "3gpp-Sbi-Lci",
                                                                       "3gpp-Sbi-Target-Nf-Group-Id",
                                                                       "3gpp-Sbi-NF-Peer-Info",
                                                                       ":status");

    private ConfigUtils()
    {
    }

    public static com.ericsson.cnal.internal.nnlf.nfdiscovery.oam.Config mapToNlfConfig(final EricssonSeppSeppFunction inConfig)
    {
        // Map the SEPP configuration to the configuration for NLF.

        if (inConfig == null || inConfig.getNfInstance() == null || inConfig.getNfInstance().isEmpty())
            return new com.ericsson.cnal.internal.nnlf.nfdiscovery.oam.Config();

        final List<com.ericsson.cnal.internal.nnlf.nfdiscovery.oam.NrfGroup> groups = inConfig.getNfInstance().stream().flatMap(instance ->
        {
            return instance.getNrfGroup()
                           .stream()
                           .map(group -> new com.ericsson.cnal.internal.nnlf.nfdiscovery.oam.NrfGroup().name(group.getName())
                                                                                                       .source("ericsson-sepp")
                                                                                                       .path(new Rdn("nf",
                                                                                                                     "sepp-function").add("nf-instance",
                                                                                                                                          instance.getName())
                                                                                                                                     .toString())
                                                                                                       .nrf(group.getNrf()
                                                                                                                 .stream()
                                                                                                                 .map(nrf -> new com.ericsson.cnal.internal.nnlf.nfdiscovery.oam.Nrf().name(nrf.getName())
                                                                                                                                                                                      .fqdn(nrf.getFqdn())
                                                                                                                                                                                      .ipEndpoint(nrf.getIpEndpoint()
                                                                                                                                                                                                     .stream()
                                                                                                                                                                                                     .map(ep -> new com.ericsson.cnal.internal.nnlf.nfdiscovery.oam.IpEndpoint().name(ep.getName())
                                                                                                                                                                                                                                                                                .port(ep.getPort())
                                                                                                                                                                                                                                                                                .ipv4Address(ep.getIpv4Address())
                                                                                                                                                                                                                                                                                .ipv6Address(ep.getIpv6Address()))
                                                                                                                                                                                                     .toList())
                                                                                                                                                                                      .priority(nrf.getPriority())
                                                                                                                                                                                      .requestTimeout(nrf.getRetryTimeout())
                                                                                                                                                                                      .scheme(com.ericsson.cnal.internal.nnlf.nfdiscovery.oam.Nrf.SchemeEnum.fromValue(nrf.getScheme()
                                                                                                                                                                                                                                                                          .value()))
                                                                                                                                                                                      .srcSbiNfPeerInfo(com.ericsson.sc.sepp.model.glue.NfInstance.getSrcSbiNfPeerInfo(instance,
                                                                                                                                                                                                                                                                       group,
                                                                                                                                                                                                                                                                       nrf)))
                                                                                                                 .toList()));
        }).toList();

        final List<com.ericsson.cnal.internal.nnlf.nfdiscovery.oam.NrfGroup> sortedGroups = Stream.of(groups)
                                                                                                  .flatMap(List::stream)
                                                                                                  .sorted((l,
                                                                                                           r) -> l.getName().compareTo(r.getName()))
                                                                                                  .toList();
        NlfConfigurator.dispatchNrfGroupIds(sortedGroups);

        return new com.ericsson.cnal.internal.nnlf.nfdiscovery.oam.Config().nrfGroup(sortedGroups);
    }

    public static List<BucketConfig> mapToRlfConfig(final EricssonSeppSeppFunction config)
    {
        // Map the SEPP configuration to the configuration for RLF.
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
                        final double fillRate = limit.getSustainableRate();

                        return new BucketConfig().name(name).fillRate(fillRate).capacity(capacity);
                    });
                });
            });
        }).toList();

        // Scan all external networks for rate limits.

        final List<BucketConfig> bucketsExternalNetwork = config.getNfInstance().stream().flatMap(instance ->
        {
            return instance.getExternalNetwork().stream().flatMap(network ->
            {
                return network.getGlobalIngressRateLimitProfileRef().stream().flatMap(ref ->
                {
                    return instance.getGlobalRateLimitProfile().stream().filter(limit -> limit.getName().equals(ref)).map(limit ->
                    {
                        final String name = CommonConfigUtils.RateLimiting.createBucketName(RateLimitedEntity.EXTERNAL_NETWORK,
                                                                                            network.getName(),
                                                                                            RateLimitType.INGRESS,
                                                                                            limit.getName());
                        final long capacity = Math.round(100f * limit.getMaxBurstSize() / (100f - lowerPrioPercentage));
                        final double fillRate = limit.getSustainableRate();

                        return new BucketConfig().name(name).fillRate(fillRate).capacity(capacity);
                    });
                });
            });
        }).toList();

        // Scan all roaming partners of all external networks for rate limits.

        final List<BucketConfig> bucketsExternalNetworkRoamingPartner = config.getNfInstance().stream().flatMap(instance ->
        {
            return instance.getExternalNetwork().stream().flatMap(network ->
            {
                return network.getRoamingPartner().stream().flatMap(roamingPartner ->
                {
                    return roamingPartner.getGlobalIngressRateLimitProfileRef().stream().flatMap(ref ->
                    {
                        return instance.getGlobalRateLimitProfile().stream().filter(limit -> limit.getName().equals(ref)).map(limit ->
                        {
                            final String name = CommonConfigUtils.RateLimiting.createBucketName(RateLimitedEntity.ROAMING_PARTNER,
                                                                                                roamingPartner.getName(),
                                                                                                RateLimitType.INGRESS,
                                                                                                limit.getName());
                            final long capacity = Math.round(100f * limit.getMaxBurstSize() / (100f - lowerPrioPercentage));
                            final double fillRate = limit.getSustainableRate();

                            return new BucketConfig().name(name).fillRate(fillRate).capacity(capacity);
                        });
                    });
                });
            });
        }).toList();

        log.info("bucketsOwnNetwork={}", bucketsOwnNetwork);
        log.info("bucketsExternalNetwork={}", bucketsExternalNetwork);
        log.info("bucketsExternalNetworkRoamingPartner={}", bucketsExternalNetworkRoamingPartner);

        final List<BucketConfig> buckets = Stream.of(bucketsOwnNetwork, bucketsExternalNetwork, bucketsExternalNetworkRoamingPartner)
                                                 .flatMap(List::stream)
                                                 .sorted((l,
                                                          r) -> l.getName().compareTo(r.getName()))
                                                 .toList();
        RlfConfigurator.dispatchBucketIds(buckets);

        return buckets;
    }

    /**
     * @param routingAction
     * @return
     */
    public static List<ProxyRoutingAction> createRoutingAction(final RoutingAction routingAction)
    {
        if (CommonConfigUtils.isStrictRoutingRule(routingAction))
        {
            var raList = new ArrayList<ProxyRoutingAction>();
            var actionStrict = routingAction.getActionRouteStrict();

            if (actionStrict.getFailoverProfileRef() != null)
            {
                var addHeaderAction = CommonConfigUtils.createRoutingActionAddHeader(actionStrict.getFailoverProfileRef());
                raList.add(addHeaderAction);
            }

            var proxyRoutingAction = CommonConfigUtils.createRoutingActionBase(actionStrict, "");
            proxyRoutingAction.setPreserveIfIndirect(PreserveIfIndirect.TARGET_API_ROOT);
            proxyRoutingAction.setKeepAuthorityHeader(false);
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
            var proxyRoutingAction = CommonConfigUtils.createRoutingActionBase(actionRoundRobin, suffix);
            proxyRoutingAction.setPreserveIfIndirect(PreserveIfIndirect.TARGET_API_ROOT);
            proxyRoutingAction.setKeepAuthorityHeader(false);
            buildRoutingActionRoundRobin(proxyRoutingAction, actionRoundRobin, suffix);
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
            var proxyRoutingAction = CommonConfigUtils.createRoutingActionBase(actionPreferred, suffix);
            proxyRoutingAction.setPreserveIfIndirect(PreserveIfIndirect.TARGET_API_ROOT);
            proxyRoutingAction.setKeepAuthorityHeader(false);
            buildRoutingActionPreferred(proxyRoutingAction, actionPreferred);
            raList.add(proxyRoutingAction);

            return raList;
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

    public static boolean isRejectMessageRule(final RoutingAction routingAction)
    {
        return routingAction.getActionRejectMessage() != null;
    }

    /**
     * @param routingAction
     * @return
     */
    public static boolean isTerminalAction(final RoutingAction routingAction)
    {
        return routingAction.getActionRejectMessage() != null || routingAction.getActionRouteStrict() != null || routingAction.getActionRoutePreferred() != null
               || routingAction.getActionRouteRoundRobin() != null || routingAction.getActionDropMessage() != null;
    }

    public static Optional<ProxyMessageValidation> createRequestMessageValidation(NfInstance seppInst,
                                                                                  String firewallProfileRef)
    {
        var firewallProfiles = seppInst.getFirewallProfile();
        if (firewallProfiles == null || firewallProfiles.isEmpty() || firewallProfileRef == null || firewallProfileRef.isEmpty())
            return Optional.empty();
        var firewallprofile = Optional.ofNullable(Utils.getByName(firewallProfiles, firewallProfileRef));
        ProxyCheckMessageBytes pxCheckMessageBytes = null;
        ProxyCheckJsonLeaves pxCheckJsonLeaves = null;
        ProxyCheckJsonDepth pxCheckJsonDepth = null;
        ProxyCheckJsonSyntax pxCheckJsonSyntax = null;
        ProxyCheckHeaders pxCheckHeaders = null;
        ProxyCheckServiceOperations pxCheckServiceOps = null;
        ProxyActionOnFailure pxActiononFailure = null;

        if (firewallprofile != null && !firewallprofile.isEmpty())
        {
            var request = firewallprofile.get().getRequest();
            if (request != null)
            {

                var validateMessageBodySize = request.getValidateMessageBodySize();

                if (validateMessageBodySize != null)
                {
                    if (validateMessageBodySize.getActionDropMessage() != null)
                    {
                        pxActiononFailure = new ProxyActionOnFailure(ActionOnFailureType.DROP, null, null, null, null, "json");
                    }
                    else
                    {
                        var actionRejectMessage = validateMessageBodySize.getActionRejectMessage();
                        pxActiononFailure = new ProxyActionOnFailure(ActionOnFailureType.REJECT,
                                                                     actionRejectMessage.getStatus(),
                                                                     Optional.ofNullable(actionRejectMessage.getTitle()),
                                                                     Optional.ofNullable(actionRejectMessage.getCause()),
                                                                     Optional.ofNullable(actionRejectMessage.getDetail()),
                                                                     actionRejectMessage.getFormat().toString());
                    }
                    pxCheckMessageBytes = new ProxyCheckMessageBytes(Optional.of(validateMessageBodySize.getMaxBytes()),
                                                                     validateMessageBodySize.getReportEvent(),
                                                                     pxActiononFailure);

                }
                var validateJsonLeaves = request.getValidateMessageJsonBodyLeaves();

                if (validateJsonLeaves != null)
                {
                    if (validateJsonLeaves.getActionDropMessage() != null)
                    {
                        pxActiononFailure = new ProxyActionOnFailure(ActionOnFailureType.DROP, null, null, null, null, "json");
                    }
                    else
                    {
                        var actionRejectMessage = validateJsonLeaves.getActionRejectMessage();
                        pxActiononFailure = new ProxyActionOnFailure(ActionOnFailureType.REJECT,
                                                                     actionRejectMessage.getStatus(),
                                                                     Optional.ofNullable(actionRejectMessage.getTitle()),
                                                                     Optional.ofNullable(actionRejectMessage.getCause()),
                                                                     Optional.ofNullable(actionRejectMessage.getDetail()),
                                                                     actionRejectMessage.getFormat().toString());
                    }
                    pxCheckJsonLeaves = new ProxyCheckJsonLeaves(Optional.of(validateJsonLeaves.getMaxLeaves()),
                                                                 validateJsonLeaves.getReportEvent(),
                                                                 pxActiononFailure);

                }
                var validateJsonDepth = request.getValidateMessageJsonBodyDepth();

                if (validateJsonDepth != null)
                {
                    if (validateJsonDepth.getActionDropMessage() != null)
                    {
                        pxActiononFailure = new ProxyActionOnFailure(ActionOnFailureType.DROP, null, null, null, null, "json");
                    }
                    else
                    {
                        var actionRejectMessage = validateJsonDepth.getActionRejectMessage();
                        pxActiononFailure = new ProxyActionOnFailure(ActionOnFailureType.REJECT,
                                                                     actionRejectMessage.getStatus(),
                                                                     Optional.ofNullable(actionRejectMessage.getTitle()),
                                                                     Optional.ofNullable(actionRejectMessage.getCause()),
                                                                     Optional.ofNullable(actionRejectMessage.getDetail()),
                                                                     actionRejectMessage.getFormat().toString());
                    }
                    pxCheckJsonDepth = new ProxyCheckJsonDepth(Optional.of(validateJsonDepth.getMaxNestingDepth()),
                                                               validateJsonDepth.getReportEvent(),
                                                               pxActiononFailure);

                }

                var validateJsonSyntax = request.getValidateMessageJsonBodySyntax();

                if (validateJsonSyntax != null)
                {
                    if (validateJsonSyntax.getActionDropMessage() != null)
                    {
                        pxActiononFailure = new ProxyActionOnFailure(ActionOnFailureType.DROP, null, null, null, null, "json");
                    }
                    else if (validateJsonSyntax.getActionForwardUnmodifiedMessage() != null)
                    {
                        pxActiononFailure = new ProxyActionOnFailure(ActionOnFailureType.FRW_UNMODIFIED, null, null, null, null, "json");
                    }
                    else
                    {
                        var actionRejectMessage = validateJsonSyntax.getActionRejectMessage();
                        pxActiononFailure = new ProxyActionOnFailure(ActionOnFailureType.REJECT,
                                                                     actionRejectMessage.getStatus(),
                                                                     Optional.ofNullable(actionRejectMessage.getTitle()),
                                                                     Optional.ofNullable(actionRejectMessage.getCause()),
                                                                     Optional.ofNullable(actionRejectMessage.getDetail()),
                                                                     actionRejectMessage.getFormat().toString());
                    }
                    pxCheckJsonSyntax = new ProxyCheckJsonSyntax(validateJsonSyntax.getReportEvent(), pxActiononFailure);

                }

                var validateHeaders = request.getValidateMessageHeaders();

                if (validateHeaders != null)
                {
                    if (validateHeaders.getActionDropMessage() != null)
                    {
                        pxActiononFailure = new ProxyActionOnFailure(ActionOnFailureType.DROP, null, null, null, null, "json");
                    }
                    else if (validateHeaders.getActionForwardUnmodifiedMessage() != null)
                    {
                        pxActiononFailure = new ProxyActionOnFailure(ActionOnFailureType.FRW_UNMODIFIED, null, null, null, null, "json");
                    }
                    else if (validateHeaders.getActionForwardModifiedMessage() != null)
                    {
                        pxActiononFailure = new ProxyActionOnFailure(ActionOnFailureType.FRW_MODIFIED, null, null, null, null, "json");
                    }
                    else
                    {
                        var actionRejectMessage = validateHeaders.getActionRejectMessage();
                        pxActiononFailure = new ProxyActionOnFailure(ActionOnFailureType.REJECT,
                                                                     actionRejectMessage.getStatus(),
                                                                     Optional.ofNullable(actionRejectMessage.getTitle()),
                                                                     Optional.ofNullable(actionRejectMessage.getCause()),
                                                                     Optional.ofNullable(actionRejectMessage.getDetail()),
                                                                     actionRejectMessage.getFormat().toString());
                    }
                    if (validateHeaders.getAllowed() != null)
                    {
                        var reqHeadersList = Stream.concat(defaultRequestHeaders.stream().map(String::toLowerCase),
                                                           validateHeaders.getAllowed().getAdditionalAllowedHeader().stream().map(String::toLowerCase))
                                                   .toList();
                        pxCheckHeaders = new ProxyCheckHeaders(policyType.allowed, reqHeadersList, validateHeaders.getReportEvent(), pxActiononFailure);

                    }
                    else // if getDenied()!=null
                    {
                        pxCheckHeaders = new ProxyCheckHeaders(policyType.denied,
                                                               validateHeaders.getDenied().getDeniedHeader().stream().map(String::toLowerCase).toList(),
                                                               validateHeaders.getReportEvent(),
                                                               pxActiononFailure);

                    }

                }

                var validateServiceOps = request.getValidateServiceOperation();

                if (validateServiceOps != null)
                {
                    if (validateServiceOps.getActionDropMessage() != null)
                    {
                        pxActiononFailure = new ProxyActionOnFailure(ActionOnFailureType.DROP, null, null, null, null, "json");
                    }
                    else if (validateServiceOps.getActionForwardUnmodifiedMessage() != null)
                    {
                        pxActiononFailure = new ProxyActionOnFailure(ActionOnFailureType.FRW_UNMODIFIED, null, null, null, null, "json");
                    }
                    else
                    {
                        var actionRejectMessage = validateServiceOps.getActionRejectMessage();
                        pxActiononFailure = new ProxyActionOnFailure(ActionOnFailureType.REJECT,
                                                                     actionRejectMessage.getStatus(),
                                                                     Optional.ofNullable(actionRejectMessage.getTitle()),
                                                                     Optional.ofNullable(actionRejectMessage.getCause()),
                                                                     Optional.ofNullable(actionRejectMessage.getDetail()),
                                                                     actionRejectMessage.getFormat().toString());
                    }

                    var customAllowedOps = validateServiceOps.getAdditionalAllowedOperations()
                                                             .stream()
                                                             .map(op -> new ProxyMessageSelector(op.getServiceName(),
                                                                                                 op.getServiceVersion(),
                                                                                                 op.getResource(),
                                                                                                 op.getNotificationMessage(),
                                                                                                 op.getHttpMethod().stream().map(Enum::toString).toList()))
                                                             .toList();

                    var defaultDeniedOps = validateServiceOps.getRemovedDefaultOperations()
                                                             .stream()
                                                             .map(op -> new ProxyMessageSelector(op.getServiceName(),
                                                                                                 op.getServiceVersion(),
                                                                                                 op.getResource(),
                                                                                                 op.getNotificationMessage(),
                                                                                                 op.getHttpMethod().stream().map(Enum::toString).toList()))
                                                             .toList();

                    pxCheckServiceOps = new ProxyCheckServiceOperations(customAllowedOps,
                                                                        defaultDeniedOps,
                                                                        validateServiceOps.getReportEvent(),
                                                                        pxActiononFailure);

                }

                return Optional.ofNullable(new ProxyMessageValidation(pxCheckMessageBytes,
                                                                      pxCheckJsonLeaves,
                                                                      pxCheckJsonDepth,
                                                                      pxCheckJsonSyntax,
                                                                      pxCheckHeaders,
                                                                      pxCheckServiceOps));
            }
        }
        return Optional.empty();
    }

    public static Optional<ProxyMessageValidation> createResponseMessageValidation(NfInstance seppInst,
                                                                                   String firewallProfileRef)
    {
        var firewallProfiles = seppInst.getFirewallProfile();
        if (firewallProfiles == null || firewallProfiles.isEmpty() || firewallProfileRef == null || firewallProfileRef.isEmpty())
            return Optional.empty();
        var firewallprofile = Optional.ofNullable(Utils.getByName(firewallProfiles, firewallProfileRef));
        ProxyCheckMessageBytes pxCheckMessageBytes = null;
        ProxyCheckJsonLeaves pxCheckJsonLeaves = null;
        ProxyCheckJsonDepth pxCheckJsonDepth = null;
        ProxyCheckJsonSyntax pxCheckJsonSyntax = null;
        ProxyCheckHeaders pxCheckHeaders = null;
        ProxyActionOnFailure pxActiononFailure = null;

        if (firewallprofile != null && !firewallprofile.isEmpty())
        {
            var response = firewallprofile.get().getResponse();
            if (response != null)
            {
                var validateMessageBodySize = response.getValidateMessageBodySize();

                if (validateMessageBodySize != null)
                {

                    var actionRespondWithError = validateMessageBodySize.getActionRespondWithError();
                    pxActiononFailure = new ProxyActionOnFailure(ActionOnFailureType.REJECT,
                                                                 actionRespondWithError.getStatus(),
                                                                 Optional.ofNullable(actionRespondWithError.getTitle()),
                                                                 Optional.ofNullable(actionRespondWithError.getCause()),
                                                                 Optional.ofNullable(actionRespondWithError.getDetail()),
                                                                 actionRespondWithError.getFormat().toString());

                    pxCheckMessageBytes = new ProxyCheckMessageBytes(Optional.of(validateMessageBodySize.getMaxBytes()),
                                                                     validateMessageBodySize.getReportEvent(),
                                                                     pxActiononFailure);

                }
                var validateJsonLeaves = response.getValidateMessageJsonBodyLeaves();

                if (validateJsonLeaves != null)
                {

                    var actionRespondWithError = validateJsonLeaves.getActionRespondWithError();
                    pxActiononFailure = new ProxyActionOnFailure(ActionOnFailureType.REJECT,
                                                                 actionRespondWithError.getStatus(),
                                                                 Optional.ofNullable(actionRespondWithError.getTitle()),
                                                                 Optional.ofNullable(actionRespondWithError.getCause()),
                                                                 Optional.ofNullable(actionRespondWithError.getDetail()),
                                                                 actionRespondWithError.getFormat().toString());

                    pxCheckJsonLeaves = new ProxyCheckJsonLeaves(Optional.of(validateJsonLeaves.getMaxLeaves()),
                                                                 validateJsonLeaves.getReportEvent(),
                                                                 pxActiononFailure);

                }
                var validateJsonDepth = response.getValidateMessageJsonBodyDepth();

                if (validateJsonDepth != null)
                {

                    var actionRespondWithError = validateJsonDepth.getActionRespondWithError();
                    pxActiononFailure = new ProxyActionOnFailure(ActionOnFailureType.REJECT,
                                                                 actionRespondWithError.getStatus(),
                                                                 Optional.ofNullable(actionRespondWithError.getTitle()),
                                                                 Optional.ofNullable(actionRespondWithError.getCause()),
                                                                 Optional.ofNullable(actionRespondWithError.getDetail()),
                                                                 actionRespondWithError.getFormat().toString());

                    pxCheckJsonDepth = new ProxyCheckJsonDepth(Optional.of(validateJsonDepth.getMaxNestingDepth()),
                                                               validateJsonDepth.getReportEvent(),
                                                               pxActiononFailure);

                }

                var validateJsonSyntax = response.getValidateMessageJsonBodySyntax();

                if (validateJsonSyntax != null)
                {
                    if (validateJsonSyntax.getActionForwardUnmodifiedMessage() != null)
                    {
                        pxActiononFailure = new ProxyActionOnFailure(ActionOnFailureType.FRW_UNMODIFIED, null, null, null, null, "json");
                    }
                    else
                    {
                        var actionRespondWithError = validateJsonSyntax.getActionRespondWithError();
                        pxActiononFailure = new ProxyActionOnFailure(ActionOnFailureType.REJECT,
                                                                     actionRespondWithError.getStatus(),
                                                                     Optional.ofNullable(actionRespondWithError.getTitle()),
                                                                     Optional.ofNullable(actionRespondWithError.getCause()),
                                                                     Optional.ofNullable(actionRespondWithError.getDetail()),
                                                                     actionRespondWithError.getFormat().toString());
                    }
                    pxCheckJsonSyntax = new ProxyCheckJsonSyntax(validateJsonSyntax.getReportEvent(), pxActiononFailure);

                }

                var validateHeaders = response.getValidateMessageHeaders();

                if (validateHeaders != null)
                {
                    if (validateHeaders.getActionForwardUnmodifiedMessage() != null)
                    {
                        pxActiononFailure = new ProxyActionOnFailure(ActionOnFailureType.FRW_UNMODIFIED, null, null, null, null, "json");
                    }
                    else if (validateHeaders.getActionForwardModifiedMessage() != null)
                    {
                        pxActiononFailure = new ProxyActionOnFailure(ActionOnFailureType.FRW_MODIFIED, null, null, null, null, "json");
                    }
                    else
                    {
                        var actionRespondWithError = validateHeaders.getActionRespondWithError();
                        pxActiononFailure = new ProxyActionOnFailure(ActionOnFailureType.REJECT,
                                                                     actionRespondWithError.getStatus(),
                                                                     Optional.ofNullable(actionRespondWithError.getTitle()),
                                                                     Optional.ofNullable(actionRespondWithError.getCause()),
                                                                     Optional.ofNullable(actionRespondWithError.getDetail()),
                                                                     actionRespondWithError.getFormat().toString());
                    }
                    if (validateHeaders.getAllowed() != null)
                    {
                        var respHeadersList = Stream.concat(defaultResponseHeaders.stream().map(String::toLowerCase),
                                                            validateHeaders.getAllowed().getAdditionalAllowedHeader().stream().map(String::toLowerCase))
                                                    .toList();
                        pxCheckHeaders = new ProxyCheckHeaders(policyType.allowed, respHeadersList, validateHeaders.getReportEvent(), pxActiononFailure);

                    }
                    else // if getDenied()!=null
                    {
                        pxCheckHeaders = new ProxyCheckHeaders(policyType.denied,
                                                               validateHeaders.getDenied().getDeniedHeader().stream().map(String::toLowerCase).toList(),
                                                               validateHeaders.getReportEvent(),
                                                               pxActiononFailure);

                    }

                }
                return Optional.ofNullable(new ProxyMessageValidation(pxCheckMessageBytes,
                                                                      pxCheckJsonLeaves,
                                                                      pxCheckJsonDepth,
                                                                      pxCheckJsonSyntax,
                                                                      pxCheckHeaders));
            }
        }
        return Optional.empty();

    }

    /**
     * @param topologyHiding
     * @param topoHidingRef
     * @return
     * @return
     * @return
     */
    public static Optional<List<IfProxyTopologyHiding>> createTopologyHiding(NfInstance seppInst,
                                                                             List<String> topoHidingRef,
                                                                             List<TopologyHidingWithAdminState> topoWithAdminRef)

    {
        var topologyHiding = seppInst.getTopologyHiding();

        if (topologyHiding == null || topologyHiding.isEmpty()
            || ((topoHidingRef == null || topoHidingRef.isEmpty()) && (topoWithAdminRef == null || topoWithAdminRef.isEmpty())))
            return Optional.empty();

        List<IfProxyTopologyHiding> pxTpHList;
        var pxPseudo = new ProxyPseudoSearchResult();
        var pxIpH = new ProxyIpAddressHiding();
        var pxFqdnHiding = new ProxyFqdnHiding();

        if (topoHidingRef != null && !topoHidingRef.isEmpty())
        {
            topoHidingRef.forEach(thRef ->
            {
                var th = Optional.ofNullable(Utils.getByName(topologyHiding, thRef));
                th.ifPresent(v ->
                {
                    String nfType;
                    if (v.getCondition() != null)
                    {
                        nfType = extractNfType(v);
                    }
                    else
                    {
                        throw new BadConfigurationException("Condition is missing from Topology Hiding");
                    }
                    if (v.getPseudoSearchResult() != null)
                    {
                        addPseudoSearchResult(pxPseudo, v, nfType);
                    }
                    else if (v.getIpAddressHiding() != null)
                    {
                        var ipAddressHiding = v.getIpAddressHiding();
                        var action = new ProxyIpAddressHiding.Action();

                        setIpAddressHidingAction(ipAddressHiding, action);

                        pxIpH.putToMapFQDNAbsense(nfType, action);

                        if (ipAddressHiding.getOnNfProfileAbsence() != null)
                        {
                            setNfProfAbsense(pxIpH, ipAddressHiding, nfType);
                        }
                        else
                        {
                            throw new BadConfigurationException("On nf profile absense container is missing");
                        }
                    }

                });

            });
        }

        if (topoWithAdminRef != null && !topoWithAdminRef.isEmpty())
        {
            topoWithAdminRef.forEach(thAdminRef ->
            {
                var th = Optional.ofNullable(Utils.getByName(topologyHiding, thAdminRef.getTphProfileRef()));

                th.ifPresent(v ->
                {
                    if (v.getFqdnMapping() != null)
                    {
                        var fqdnMapping = v.getFqdnMapping();
                        var mapLocators = new ThLocatorsMapping(thAdminRef.getAdminState().value(), SCR_RULE_UNSUCCESSFUL_MAPPING, seppInst);

                        var fqdnMapDefaultSelector = mapLocators.createDefaultSelectors();

                        if (fqdnMapping.getCustomFqdnLocator() != null && !fqdnMapping.getCustomFqdnLocator().isEmpty())
                        {
                            ThLocators.setCustomFqdnLocator((fqdnMapping.getCustomFqdnLocator()), fqdnMapDefaultSelector);
                        }
                        setFqdnMapUnsuccessfulAction(fqdnMapping, pxFqdnHiding);
                        mapLocators.addCustomFqdnLocators(fqdnMapDefaultSelector, pxFqdnHiding);
                    }

                    if (v.getFqdnScrambling() != null)
                    {
                        var fqdnScrambling = v.getFqdnScrambling();
                        var scrmLocators = new ThLocatorsScrambling(thAdminRef.getAdminState().value(), SCR_RULE_UNSUCCESSFUL_SCRAMBLING, seppInst);

                        var fqdnScrambleDefaultSelector = scrmLocators.createDefaultSelectors(seppInst);

                        if (fqdnScrambling.getCustomFqdnLocator() != null && !fqdnScrambling.getCustomFqdnLocator().isEmpty())
                        {
                            ThLocators.setCustomFqdnLocator((fqdnScrambling.getCustomFqdnLocator()), fqdnScrambleDefaultSelector);
                        }

                        setFqdnScrambleUnsuccessfulAction(fqdnScrambling, pxFqdnHiding);
                        setFqdnScramblingEncryptionProfiles(seppInst.getFqdnScramblingTable(), thAdminRef.getScramblingKey(), pxFqdnHiding);
                        scrmLocators.addCustomFqdnLocators(fqdnScrambleDefaultSelector, pxFqdnHiding);
                    }
                });
            });
        }
        pxTpHList = new ArrayList<>();
        pxTpHList.add(pxPseudo);
        pxTpHList.add(pxIpH);
        pxTpHList.add(pxFqdnHiding);

        return Optional.of(pxTpHList);
    }

    private static void setNfProfAbsense(ProxyIpAddressHiding pxIpH,
                                         IpAddressHiding ipAddressHiding,
                                         String nfType)
    {
        var bothMissing = true;
        if (ipAddressHiding.getOnNfProfileAbsence().getRemoveIpv4AddressRange() != null
            && !ipAddressHiding.getOnNfProfileAbsence().getRemoveIpv4AddressRange().isEmpty())
        {
            pxIpH.putToMapSubnetPerNfIpv4(nfType, ipAddressHiding.getOnNfProfileAbsence().getRemoveIpv4AddressRange());
            bothMissing = false;
        }
        if (ipAddressHiding.getOnNfProfileAbsence().getRemoveIpv6AddressRange() != null
            && !ipAddressHiding.getOnNfProfileAbsence().getRemoveIpv6AddressRange().isEmpty())
        {
            pxIpH.putToMapSubnetPerNfIpv6(nfType, ipAddressHiding.getOnNfProfileAbsence().getRemoveIpv6AddressRange());
            bothMissing = false;
        }
        if (bothMissing)
            throw new BadConfigurationException("Both IPv4 and IPv6 subnet lists are empty");

    }

    private static void setIpAddressHidingAction(IpAddressHiding ipAddressHiding,
                                                 Action action)
    {
        if (ipAddressHiding.getOnFqdnAbsence() != null)
        {
            if (ipAddressHiding.getOnFqdnAbsence().getDropMessage() != null)
            {
                action.setActionOption(ProxyIpAddressHiding.Action.ActionOption.DROP);
            }
            else if (ipAddressHiding.getOnFqdnAbsence().getForwardMessage() != null)
            {
                action.setActionOption(ProxyIpAddressHiding.Action.ActionOption.FORWARD);
            }
            else if (ipAddressHiding.getOnFqdnAbsence().getRespondWithError() != null)
            {
                var messageBodyFormat = MessageBodyType.JSON;

                if (ipAddressHiding.getOnFqdnAbsence().getRespondWithError().getFormat() != null
                    && ipAddressHiding.getOnFqdnAbsence().getRespondWithError().getFormat().compareTo(Format.TEXT) == 0)
                {
                    messageBodyFormat = MessageBodyType.PLAIN_TEXT;
                }

                if (ipAddressHiding.getOnFqdnAbsence().getRespondWithError().getStatus() == null)
                    throw new BadConfigurationException("Status is missing from respond with error action");

                action.setActionOption(ProxyIpAddressHiding.Action.ActionOption.ERROR);
                action.setError(ipAddressHiding.getOnFqdnAbsence().getRespondWithError().getStatus(),
                                ipAddressHiding.getOnFqdnAbsence().getRespondWithError().getTitle(),
                                ipAddressHiding.getOnFqdnAbsence().getRespondWithError().getDetail(),
                                ipAddressHiding.getOnFqdnAbsence().getRespondWithError().getCause(),
                                messageBodyFormat);
            }
        }

        else
        {
            action.setActionOption(ProxyIpAddressHiding.Action.ActionOption.HIDE);
        }

    }

    private static void setFqdnScrambleUnsuccessfulAction(FqdnScrambling fqdnScrambling,
                                                          ProxyFqdnHiding pxFqdnScramble)
    {
        if (fqdnScrambling.getOnFqdnScramblingUnsuccessful() != null)
        {
            if (fqdnScrambling.getOnFqdnScramblingUnsuccessful().getDropMessage() != null)
            {

                pxFqdnScramble.addUnsuccessfullAction(new ProxyScreeningRule(SCR_RULE_UNSUCCESSFUL_SCRAMBLING,
                                                                             ConditionParser.parse("true"),
                                                                             List.of(new ProxyScreeningAction(SCR_ACTION_UNSUCCESSFUL,
                                                                                                              ScreeningActionType.ActionDropMessage))));
            }
            else if (fqdnScrambling.getOnFqdnScramblingUnsuccessful().getRespondWithError() != null)
            {
                var messageBodyFormat = "json";

                var respondErr = fqdnScrambling.getOnFqdnScramblingUnsuccessful().getRespondWithError();

                if (respondErr.getStatus() == null)
                    throw new BadConfigurationException("Status is missing from respond with error action");

                if (respondErr.getFormat() != null && respondErr.getFormat().compareTo(Format.TEXT) == 0)
                {
                    messageBodyFormat = "text";
                }

                var unsuccessfullAction = new ProxyScreeningAction(SCR_ACTION_UNSUCCESSFUL,
                                                                   ScreeningActionType.ActionRejectMessage).setStatusCode(respondErr.getStatus())
                                                                                                           .setTitle(respondErr.getTitle())
                                                                                                           .setDetail(respondErr.getDetail())
                                                                                                           .setCause(respondErr.getCause())
                                                                                                           .setFormat(messageBodyFormat);

                pxFqdnScramble.addUnsuccessfullAction(new ProxyScreeningRule(SCR_RULE_UNSUCCESSFUL_SCRAMBLING,
                                                                             ConditionParser.parse("true"),
                                                                             List.of(unsuccessfullAction)));
            }
        }
    }

    private static void setFqdnMapUnsuccessfulAction(FqdnMapping fqdnMapping,
                                                     ProxyFqdnHiding pxFqdnMap)
    {
        if (fqdnMapping.getOnFqdnMappingUnsuccessful() != null)
        {
            if (fqdnMapping.getOnFqdnMappingUnsuccessful().getDropMessage() != null)
            {

                pxFqdnMap.addUnsuccessfullAction(new ProxyScreeningRule(SCR_RULE_UNSUCCESSFUL_MAPPING,
                                                                        ConditionParser.parse("true"),
                                                                        List.of(new ProxyScreeningAction(SCR_ACTION_UNSUCCESSFUL,
                                                                                                         ScreeningActionType.ActionDropMessage))));
            }
            else if (fqdnMapping.getOnFqdnMappingUnsuccessful().getRespondWithError() != null)
            {
                var messageBodyFormat = "json";

                var respondErr = fqdnMapping.getOnFqdnMappingUnsuccessful().getRespondWithError();

                if (respondErr.getStatus() == null)
                    throw new BadConfigurationException("Status is missing from respond with error action");

                if (respondErr.getFormat() != null && respondErr.getFormat().compareTo(Format.TEXT) == 0)
                {
                    messageBodyFormat = "text";
                }

                var unsuccessfullAction = new ProxyScreeningAction(SCR_ACTION_UNSUCCESSFUL,
                                                                   ScreeningActionType.ActionRejectMessage).setStatusCode(respondErr.getStatus())
                                                                                                           .setTitle(respondErr.getTitle())
                                                                                                           .setDetail(respondErr.getDetail())
                                                                                                           .setCause(respondErr.getCause())
                                                                                                           .setFormat(messageBodyFormat);

                pxFqdnMap.addUnsuccessfullAction(new ProxyScreeningRule(SCR_RULE_UNSUCCESSFUL_MAPPING,
                                                                        ConditionParser.parse("true"),
                                                                        List.of(unsuccessfullAction)));
            }
        }
    }

    private static void setFqdnScramblingEncryptionProfiles(List<FqdnScramblingTable> scramblingKeyTable,
                                                            List<ScramblingKey> scramblingkey,
                                                            ProxyFqdnHiding pxFqdnScramble)
    {
        if (scramblingKeyTable != null && !scramblingKeyTable.isEmpty() && scramblingkey != null && !scramblingkey.isEmpty())
        {
            var encryptionProfilesList = new ArrayList<ProxyEncryptionProfile>();

            scramblingkey.forEach(p ->
            {
                var pxEncryptProfile = new ProxyEncryptionProfile();
                var keyId = p.getKeyIdRef();
                var keyTableEntry = scramblingKeyTable.stream().filter(n -> n.getId().equals(keyId)).findFirst();

                pxEncryptProfile.setEncryptionIdentifier(keyId);

                keyTableEntry.ifPresent(n ->
                {
                    pxEncryptProfile.setScramblingKey(n.getKey());
                    pxEncryptProfile.setInitialVector(n.getInitialVector());
                });

                encryptionProfilesList.add(pxEncryptProfile);
            });
            pxFqdnScramble.setEncryptionProfiles(Optional.of(encryptionProfilesList));
        }
    }

    // With this function the fqdn of the own or external network is set under the
    // roaming partner in envoy configuration
    public static void setOwnNetworkFqdn(NfInstance seppInst,
                                         String serviceAddreesRef,
                                         ProxyRoamingPartner proxyRp)
    {

        seppInst.getServiceAddress()
                .stream()
                .filter(Objects::nonNull)
                .filter(svc -> svc.getName().equals(serviceAddreesRef))
                .findFirst()
                .ifPresent(svc -> proxyRp.setOwnNetworkFqdn(svc.getFqdn()));
    }

    private static String extractNfType(TopologyHiding v)
    {
        var regex = ".*'([^']*)'.*";
        var pattern = Pattern.compile(regex);
        var matcher = pattern.matcher(v.getCondition());
        String nfType = "";
        if (matcher.find())
        {
            nfType = matcher.group(1);
        }
        else
        {
            throw new BadConfigurationException("Cannot extract NF type from topology hiding condition");
        }
        return nfType;
    }

    private static void addPseudoSearchResult(ProxyPseudoSearchResult pxPseudo,
                                              TopologyHiding v,
                                              String nfType)
    {
        var pseudoSearchResult = v.getPseudoSearchResult();
        if (pseudoSearchResult.getNfProfile().getNfInstanceId() != null)
        {
            var searchResult = createTphSearchResult(pseudoSearchResult);
            pxPseudo.putToMap(nfType, convertTphSearchResultToJson(searchResult));
            pxPseudo.addFqdn(v.getPseudoSearchResult().getNfProfile().getFqdn());
        }

    }

    private static String convertTphSearchResultToJson(Object obj)
    {
        try
        {
            var jsonRootNode = om.valueToTree(obj);

            JsonNode instancesNodes = jsonRootNode.get("nfInstances");

            for (var node : instancesNodes)
            {
                ((ObjectNode) node).remove("olcHSupportInd");
                ((ObjectNode) node).remove("lcHSupportInd");
                ((ObjectNode) node).remove("nfServicePersistence");
            }

            return om.writeValueAsString(jsonRootNode);
        }
        catch (IOException e)
        {
            throw new BadConfigurationException("Object mapping to JSON object has failed", e);
        }
    }

    /**
     * @param pseudoSearchResult
     */
    private static SearchResult createTphSearchResult(PseudoSearchResult pseudoSearchResult)
    {
        var pseudoNfProfile = pseudoSearchResult.getNfProfile();

        return new SearchResult().validityPeriod(pseudoSearchResult.getValidityPeriod())
                                 .addNfInstancesItem(new NFProfile().fqdn(pseudoNfProfile.getFqdn())
                                                                    .nfType(pseudoNfProfile.getNfType())
                                                                    .nfInstanceId(UUID.fromString(pseudoNfProfile.getNfInstanceId()))
                                                                    .nfStatus(pseudoNfProfile.getNfStatus().value().toUpperCase()));
    }

    private static ProxyRoutingAction buildRoutingActionRoamingPartner(final ProxyRoutingAction pra,
                                                                       final TargetRoamingPartner dra,
                                                                       final String clusterSuffix)
    {
        var tempPra = pra;

        if (dra != null)
        {
            tempPra.setDestinationRoamingPartner(dra.getRoamingPartnerRef() + clusterSuffix);
        }

        return tempPra;
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
                                                     ActionRouteRoundRobin actionRoundRobin,
                                                     String clusterSuffix)
    {
        buildRoutingActionRoamingPartner(proxyRoutingAction, actionRoundRobin.getTargetRoamingPartner(), clusterSuffix);
        CommonConfigUtils.buildRoutingActionLastResort(proxyRoutingAction, actionRoundRobin.getLastResortNfPoolRef());
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

    public static List<String> collectVHosts()
    {
        ArrayList<String> vHostList = new ArrayList<>();

        vHostList.add(ServiceConfig.INT_SERVICE);
        return vHostList;

    }

    /**
     * This method is used to add the own plmnd ids that are configured under the
     * N32C container to own_plmn_ids object under RoutingFilter class
     *
     * @param seppFilter
     * @param seppInst
     */
    public static void setOwnPlmnIds(NfInstance seppInst,
                                     ProxySeppFilter seppFilter)
    {
        var plmnIdsInfoBuilder = PlmnIdInfo.newBuilder();
        var plmnIdsPairBuilder = PlmnIdInfo.PlmnIdPair.newBuilder();

        var ownN32c = seppInst.getN32C();
        if (ownN32c != null && !ownN32c.getOwnSecurityData().isEmpty())
        {
            var ownPrimaryIdMcc = ownN32c.getOwnSecurityData().get(0).getPrimaryPlmnIdMcc();
            var ownPrimaryIdMnc = ownN32c.getOwnSecurityData().get(0).getPrimaryPlmnIdMnc();

            plmnIdsPairBuilder.setMcc(ownPrimaryIdMcc).setMnc(ownPrimaryIdMnc);
            plmnIdsInfoBuilder.setPrimaryPlmnId(plmnIdsPairBuilder.build());

            var additionalPlmnIds = ownN32c.getOwnSecurityData().get(0).getAdditionalPlmnId();
            if (additionalPlmnIds != null && !additionalPlmnIds.isEmpty())
            {
                additionalPlmnIds.stream().filter(Objects::nonNull).forEach(plmnId ->
                {
                    var ownAdditionalMcc = plmnId.getMcc();
                    var ownAdditionalMnc = plmnId.getMnc();
                    plmnIdsPairBuilder.clear();
                    plmnIdsPairBuilder.setMcc(ownAdditionalMcc).setMnc(ownAdditionalMnc);
                    plmnIdsInfoBuilder.addAdditionalPlmnIds(plmnIdsPairBuilder.build());

                });
            }
        }
        seppFilter.setPlmnIdInfo(plmnIdsInfoBuilder.build());
    }

    /**
     * This method is used to add the roaming partner plmnd ids that are configured
     * under the N32C container of each RP to plmnIds object under
     * ProxyRoamingPartner class
     *
     * @param seppInst
     * @param roamingPartner
     * @param proxyRp
     */
    public static void setRoamingPartnerPlmnIds(NfInstance seppInst,
                                                RoamingPartner roamingPartner,
                                                ProxyRoamingPartner proxyRp)
    {

        var plmnIdsInfoBuilder = PlmnIdInfo.newBuilder();
        var plmnIdsPairBuilder = PlmnIdInfo.PlmnIdPair.newBuilder();

        var extNws = seppInst.getExternalNetwork();
        List<RoamingPartner> roamingPartners = extNws.stream().flatMap(nw -> nw.getRoamingPartner().stream()).filter(rp -> rp.getN32C() != null).toList();

        roamingPartners.stream().filter(rp -> Objects.equals(rp.getName(), roamingPartner.getName())).findAny().ifPresent(rp ->
        {
            var rpPrimaryIdMcc = rp.getN32C().getAllowPlmn().getPrimaryIdMcc();
            var rpPrimaryIdMnc = rp.getN32C().getAllowPlmn().getPrimaryIdMnc();

            plmnIdsPairBuilder.setMcc(rpPrimaryIdMcc).setMnc(rpPrimaryIdMnc);
            plmnIdsInfoBuilder.setPrimaryPlmnId(plmnIdsPairBuilder.build());

            var additionalPlmnIds = rp.getN32C().getAllowPlmn().getAdditionalId();

            if (additionalPlmnIds != null && !additionalPlmnIds.isEmpty())
            {

                additionalPlmnIds.stream().filter(Objects::nonNull).forEach(plmnId ->
                {
                    var rpAdditionalMcc = plmnId.getMcc();
                    var rpAdditionalMnc = plmnId.getMnc();
                    plmnIdsPairBuilder.clear();
                    plmnIdsPairBuilder.setMcc(rpAdditionalMcc).setMnc(rpAdditionalMnc);
                    plmnIdsInfoBuilder.addAdditionalPlmnIds(plmnIdsPairBuilder.build());

                });
            }
        });
        proxyRp.setPlmnIdInfo(plmnIdsInfoBuilder.build());
    }

    /*
     * The following methods check the defined roaming partners under an
     * external-network. If a RP contains a different routing/screening case ref
     * different than the default defined at external-network level, the RP level
     * one is returned.
     * 
     * However, if the network corresponds to a non-tls listener, the defaults are
     * always chosen
     * 
     * Currently, only RPs with the exact same refs are supported (checked by the
     * validator)
     *
     */

    public static String overrideRoutingCase(IfNetwork nw,
                                             boolean tls)
    {
        if (!tls)
        {
            return nw.getRoutingCaseRef();
        }

        if (nw instanceof ExternalNetwork)
        {
            var extNw = (ExternalNetwork) nw;
            if (!extNw.getRoamingPartner().isEmpty())
            {
                var rp = extNw.getRoamingPartner().get(0);
                return rp.getRoutingCaseRef() != null ? rp.getRoutingCaseRef() : extNw.getRoutingCaseRef();

            }
        }
        return nw.getRoutingCaseRef();
    }

    public static String overrideInRequestScreeningCase(IfNetwork nw,
                                                        boolean tls)
    {

        if (!tls)
        {
            return nw.getInRequestScreeningCaseRef();
        }
        if (nw instanceof ExternalNetwork)
        {
            var extNw = (ExternalNetwork) nw;
            if (!extNw.getRoamingPartner().isEmpty())
            {
                var rp = extNw.getRoamingPartner().get(0);
                return rp.getInRequestScreeningCaseRef() != null ? rp.getInRequestScreeningCaseRef() : extNw.getInRequestScreeningCaseRef();

            }
        }
        return nw.getInRequestScreeningCaseRef();
    }

    public static String overrideOutResponseScreeningCase(IfNetwork nw,
                                                          boolean tls)
    {
        if (!tls)
        {
            return nw.getOutResponseScreeningCaseRef();
        }
        if (nw instanceof ExternalNetwork)
        {
            var extNw = (ExternalNetwork) nw;
            if (!extNw.getRoamingPartner().isEmpty())
            {
                var rp = extNw.getRoamingPartner().get(0);
                return rp.getOutResponseScreeningCaseRef() != null ? rp.getOutResponseScreeningCaseRef() : extNw.getOutResponseScreeningCaseRef();

            }
        }
        return nw.getOutResponseScreeningCaseRef();
    }

    public static boolean hasInRequestScreeningCase(IfNetwork nw)
    {
        if (nw.getInRequestScreeningCaseRef() != null)
        {
            return true;
        }

        if (nw instanceof ExternalNetwork)
        {
            var extNw = (ExternalNetwork) nw;
            if (!extNw.getRoamingPartner().isEmpty())
            {
                Optional<RoamingPartner> rpMatch = extNw.getRoamingPartner().stream().filter(rp -> rp.getInRequestScreeningCaseRef() != null).findAny();
                return !rpMatch.isEmpty();

            }
        }
        return false;
    }

    public static boolean hasOutResponseScreeningCase(IfNetwork nw)
    {
        if (nw.getOutResponseScreeningCaseRef() != null)
        {
            return true;
        }

        if (nw instanceof ExternalNetwork)
        {
            var extNw = (ExternalNetwork) nw;
            if (!extNw.getRoamingPartner().isEmpty())
            {
                Optional<RoamingPartner> rpMatch = extNw.getRoamingPartner().stream().filter(rp -> rp.getOutResponseScreeningCaseRef() != null).findAny();
                return !rpMatch.isEmpty();

            }
        }
        return false;
    }

    public static Set<String> getReferencedInRequestScreeningCases(IfNetwork network)
    {
        var inReqScreeningRef = new HashSet<String>();

        if (network.getInRequestScreeningCaseRef() != null)
        {
            inReqScreeningRef.add(network.getInRequestScreeningCaseRef());
        }

        if (network instanceof ExternalNetwork)
        {
            var extNw = (ExternalNetwork) network;
            if (!extNw.getRoamingPartner().isEmpty())
            {
                extNw.getRoamingPartner()
                     .stream()
                     .filter(rp -> rp.getInRequestScreeningCaseRef() != null)
                     .forEach(rp -> inReqScreeningRef.add(rp.getInRequestScreeningCaseRef()));
            }
        }

        return inReqScreeningRef;
    }

    public static Set<String> getReferencedOutResponseScreeningCases(IfNetwork network)
    {
        var outRespScreeningRef = new HashSet<String>();

        if (network.getOutResponseScreeningCaseRef() != null)
        {
            outRespScreeningRef.add(network.getOutResponseScreeningCaseRef());
        }

        if (network instanceof ExternalNetwork)
        {
            var extNw = (ExternalNetwork) network;

            extNw.getRoamingPartner()
                 .stream()
                 .filter(rp -> rp.getOutResponseScreeningCaseRef() != null)
                 .forEach(rp -> outRespScreeningRef.add(rp.getOutResponseScreeningCaseRef()));
        }

        return outRespScreeningRef;

    }

    /**
     * Given a list of NfPools, returns a Set containing their
     * OutRequestScreeningCase references. Used for MessageScreeningFilter
     * configuration
     * 
     * @param pools
     * @return Set of OutRequestScreeningCase references
     */
    public static Set<String> getReferencedOutRequestScreeningCases(List<NfPool> pools)
    {
        var outReqScreeningRef = new HashSet<String>();
        pools.stream()
             .filter(pool -> pool.getOutRequestScreeningCaseRef() != null)
             .forEach(pool -> outReqScreeningRef.add(pool.getOutRequestScreeningCaseRef()));

        return outReqScreeningRef;
    }

    /**
     * Given a list of NfPools, returns a Set containing their
     * InResponseScreeningCase references. Used for MessageScreeningFilter
     * configuration
     * 
     * @param pools
     * @return Set of InResponseScreeningCase references
     */
    public static Set<String> getReferencedInResponseScreeningCases(List<NfPool> pools)
    {
        var inRespScreeningRef = new HashSet<String>();

        pools.stream()
             .filter(pool -> pool.getInResponseScreeningCaseRef() != null)
             .forEach(pool -> inRespScreeningRef.add(pool.getInResponseScreeningCaseRef()));

        return inRespScreeningRef;
    }

    public static Set<String> getReferencedRoutingCases(IfNetwork network)
    {
        var routingRef = new HashSet<String>();

        routingRef.add(network.getRoutingCaseRef());

        if (network instanceof ExternalNetwork)
        {
            var extNw = (ExternalNetwork) network;

            extNw.getRoamingPartner().stream().filter(rp -> rp.getRoutingCaseRef() != null).forEach(rp -> routingRef.add(rp.getRoutingCaseRef()));
        }

        return routingRef;
    }

    public static Optional<NfPool> fetchNfPoolFromRoamingPartner(String rpName,
                                                                 List<NfPool> nfPools)
    {
        return nfPools.stream().filter(pool -> pool.getRoamingPartnerRef() != null && pool.getRoamingPartnerRef().equals(rpName)).findFirst();
    }

    /**
     * Given a RoamingParther name, return the routing action, where roaming partner
     * ref contains the given RP name. It is assumed that only one action
     * 
     * @param rpName The name of the RP
     * 
     * @param rcList The routing case applied for the RP
     * 
     * @return The first routing action it finds, that references the given RP,
     *         Optional.empty() otherwise.
     */
    public static List<RoutingAction> fetchRoutingActionsTargetRoamingPartner(String rpName,
                                                                              List<RoutingCase> rcList)
    {
        return rcList.stream().flatMap(rc -> rc.getRoutingRule().stream()).flatMap(rr -> rr.getRoutingAction().stream()).filter(ra ->
        {
            if (ra.getActionRouteRoundRobin() == null)
            {
                return false;
            }
            var rr = ra.getActionRouteRoundRobin();
            if (rr.getTargetRoamingPartner() == null)
            {
                return false;
            }
            var tRp = rr.getTargetRoamingPartner();
            if (tRp.getRoamingPartnerRef() == null)
            {
                return false;
            }
            return tRp.getRoamingPartnerRef().equals(rpName);
        }).toList();
    }

    public static <T extends IfNetwork> Optional<T> fetchNetwork(List<T> networks,
                                                                 String name)
    {
        return networks.stream().filter(on -> on.getName().equals(name)).findFirst();
    }

    public static Optional<String> getAsymetricKeyForSvcAddress(final NfInstance seppInst,
                                                                final ServiceAddress svcAddress)
    {
        AtomicReference<Optional<String>> asymmetricKeyString = new AtomicReference<>(Optional.empty());

        Optional.ofNullable(Utils.getByName(seppInst.getAsymKeyList(), svcAddress.getAsymKeyInRef())).ifPresentOrElse(akIn ->
        {
            asymmetricKeyString.set(Optional.of(CR_PREFIX + akIn.getAsymmetricKey() + "-" + akIn.getCertificate() + "-certificate"));

        }, () ->
        {
            Optional<AsymmetricKey> asymKey = Optional.ofNullable(Utils.getByName(seppInst.getAsymmetricKey(), svcAddress.getAsymmetricKeyRef()));
            asymKey.ifPresent(ak ->
            {
                var asymKeyJoiner = new StringJoiner("#!_#");
                asymmetricKeyString.set(Optional.of(asymKeyJoiner.add(ak.getPrivateKey()).add(ak.getCertificate()).toString()));
            });
        });

        return asymmetricKeyString.get();
    }

    public static Optional<String> getTrustedCaList(final ExternalNetwork ext)
    {
        var trustedAuthorityJoiner = new StringJoiner("#!_#");
        String trustedCaListString = null;

        if (ext.getRoamingPartner().stream().anyMatch(rp -> (rp.getTrustedCertInListRef() != null || (rp.getTrustedCertificateList() != null))))
        {
            trustedCaListString = trustedAuthorityJoiner.add("EXT:").add(ext.getName()).toString();
        }

        return Optional.ofNullable(trustedCaListString);
    }

    public static Optional<String> getTrustedCaList(final NfInstance seppInst,
                                                    final OwnNetwork own)
    {
        AtomicReference<Optional<String>> trustedCaListString = new AtomicReference<>(Optional.empty());

        Optional.ofNullable(Utils.getByName(seppInst.getTrustedCertList(), own.getTrustedCertInListRef())).ifPresentOrElse(tcInList ->
        {
            trustedCaListString.set(Optional.of(CR_PREFIX + tcInList.getTrustedCertListRef() + "-ca-certificate"));

        }, () -> Optional.ofNullable(own.getTrustedCertificateList()).ifPresent(tcaList -> trustedCaListString.set(Optional.of(tcaList))));

        return trustedCaListString.get();
    }

    public static Optional<ProxyTls> createTlsForListener(final ServiceAddress svcAddress,
                                                          final NfInstance seppInst,
                                                          final String networkName)
    {
        return ConfigUtils.getAsymetricKeyForSvcAddress(seppInst, svcAddress).map(asymKey ->
        {
            AtomicReference<Optional<String>> trustedCaList = new AtomicReference<>(Optional.empty());
            ConfigUtils.fetchNetwork(seppInst.getOwnNetwork(), networkName).map(on -> ConfigUtils.getTrustedCaList(seppInst, on)).ifPresent(trustedCaList::set);
            ConfigUtils.fetchNetwork(seppInst.getExternalNetwork(), networkName).map(ConfigUtils::getTrustedCaList).ifPresent(trustedCaList::set);

            return new ProxyTls(trustedCaList.get().get(), asymKey);
        });
    }

    public static List<RoamingPartner> getAllRoamingPartners(final List<ExternalNetwork> extNws)
    {
        return extNws.stream().flatMap(nw -> nw.getRoamingPartner().stream()).toList();
    }

    /**
     * Given the external networks list, this function returns all the roaming
     * partners that have n32-c enabled.
     * 
     * @param extNws The external network
     * 
     * @return List<RoamingPartner>
     * 
     */
    public static List<RoamingPartner> getAllRoamingPartnersWithN32C(final List<ExternalNetwork> extNws)
    {
        return extNws.stream().flatMap(nw -> nw.getRoamingPartner().stream()).filter(rp -> rp.getN32C() != null && rp.getN32C().getEnabled()).toList();
    }

    /**
     * Given the external network, this function returns the roaming partners that
     * have n32-c enabled per network.
     *
     * @param extNws The external network
     *
     * @return List<RoamingPartner>
     *
     */
    public static List<RoamingPartner> getRoamingPartnersWithN32C(final ExternalNetwork extNws)
    {
        return extNws.getRoamingPartner().stream().filter(rp -> rp.getN32C() != null && rp.getN32C().getEnabled()).toList();
    }

    /**
     * Given the external network, and roaming partner name, this function returns
     * the allow-plmn-id list from the roaming partner that have n32-c enabled per
     * network.
     *
     * @param extNws             The external network
     * @param roamingPartnerName
     * @return List<PlmnId>
     *
     */
    public static List<PlmnId> getAllowPlmnIdListFromRoamingPartner(final List<ExternalNetwork> extNws,
                                                                    final String roamingPartnerName)
    {
        List<PlmnId> allowPlmnIdList = new ArrayList<>();
        getAllRoamingPartnersWithN32C(extNws).stream().filter(rp -> Objects.equals(rp.getName(), roamingPartnerName)).forEach(rp ->
        {
            String rpPrimaryIdMcc = rp.getN32C().getAllowPlmn().getPrimaryIdMcc();
            String rpPrimaryIdMnc = rp.getN32C().getAllowPlmn().getPrimaryIdMnc();
            allowPlmnIdList.add(new PlmnId().mcc(rpPrimaryIdMcc).mnc(rpPrimaryIdMnc));

            rp.getN32C().getAllowPlmn().getAdditionalId().stream().forEach(additionalPlmnId ->

            allowPlmnIdList.add(new PlmnId().mcc(additionalPlmnId.getMcc()).mnc(additionalPlmnId.getMnc())));

        });

        return allowPlmnIdList;
    }

    public static void createKvTableRPDomainsToNames(ProxySeppFilter seppFilter,
                                                     final NfInstance seppInst,
                                                     final String tableName)
    {
        var kvTable = new HashMap<String, String>();
        seppInst.getExternalNetwork()
                .stream()
                .flatMap(nw -> nw.getRoamingPartner().stream())
                .forEach(roamingPartner -> roamingPartner.getDomainName().stream().forEach(domainName -> kvTable.put(domainName, roamingPartner.getName())));

        seppFilter.addKvTable(tableName, kvTable);
    }

    public static void createRoutingKvTableForRPs(ProxySeppFilter seppFilter,
                                                  final NfInstance seppInst,
                                                  final String tableName)
    {
        var kvTable = new HashMap<String, String>();
        seppInst.getExternalNetwork()
                .stream()
                .flatMap(nw -> nw.getRoamingPartner().stream())
                .filter(rp -> rp.getRoutingCaseRef() != null)
                .forEach(rp -> kvTable.put(rp.getName(), rp.getRoutingCaseRef()));

        if (kvTable.size() > 0)
        {
            seppFilter.addIntKvTable(tableName, kvTable);
        }
    }

    public static void createInScreeningKvTableForRPs(ProxySeppFilter seppFilter,
                                                      final NfInstance seppInst,
                                                      final String tableName)
    {
        var kvTable = new HashMap<String, String>();

        seppInst.getExternalNetwork()
                .stream()
                .flatMap(nw -> nw.getRoamingPartner().stream())
                .filter(rp -> rp.getInRequestScreeningCaseRef() != null)
                .forEach(rp -> kvTable.put(rp.getName(), rp.getInRequestScreeningCaseRef()));

        if (kvTable.size() > 0)
        {
            seppFilter.addIntKvTable(tableName, kvTable);
        }
    }

    public static void createOutScreeningKvTableForRPs(ProxySeppFilter seppFilter,
                                                       final NfInstance seppInst,
                                                       final String tableName)
    {
        var kvTable = new HashMap<String, String>();

        seppInst.getExternalNetwork()
                .stream()
                .flatMap(nw -> nw.getRoamingPartner().stream())
                .filter(rp -> rp.getOutResponseScreeningCaseRef() != null)
                .forEach(rp -> kvTable.put(rp.getName(), rp.getOutResponseScreeningCaseRef()));

        if (kvTable.size() > 0)
        {
            seppFilter.addIntKvTable(tableName, kvTable);
        }
    }

    public static void createOutRequestScreeningKvTableForPools(ProxySeppFilter seppFilter,
                                                                final NfInstance seppInst,
                                                                final String tableName)
    {
        createOutRequestScreeningKvTableForPools(seppFilter, seppInst, tableName, Optional.empty());
    }

    /**
     * creates an IntKvTable containing mappings of pool names to their
     * corresponding Out-request-screening-case-ref and attaches it (if not empty)
     * to the provided PRoxySeppFilter object.
     * 
     * @param seppFilter
     * @param seppInst
     * @param tableName
     * @param pools      Optional containing a List of pools to be considered. If
     *                   empty, all configured pools are examined
     */

    public static void createOutRequestScreeningKvTableForPools(ProxySeppFilter seppFilter,
                                                                final NfInstance seppInst,
                                                                final String tableName,
                                                                final Optional<List<NfPool>> pools)
    {
        var inspectedPools = pools.isPresent() ? pools.get() : seppInst.getNfPool();
        var kvTable = inspectedPools.stream()
                                    .filter(pool -> pool.getOutRequestScreeningCaseRef() != null)
                                    .collect(Collectors.toMap(NfPool::getName, NfPool::getOutRequestScreeningCaseRef));
        if (!kvTable.isEmpty())
        {
            seppFilter.addIntKvTable(tableName, kvTable);
        }

    }

    public static void createInResponseScreeningKvTableForPools(ProxySeppFilter seppFilter,
                                                                final NfInstance seppInst,
                                                                final String tableName)
    {
        createInResponseScreeningKvTableForPools(seppFilter, seppInst, tableName, Optional.empty());
    }

    /**
     * creates an IntKvTable containing mappings of pool names to their
     * corresponding In-response-screening-case-ref and attaches it (if not empty)
     * to the provided PRoxySeppFilter object.
     * 
     * @param seppFilter
     * @param seppInst
     * @param tableName
     * @param pools      Optional containing a List of pools to be considered. If
     *                   empty, all configured pools are examined
     */
    public static void createInResponseScreeningKvTableForPools(ProxySeppFilter seppFilter,
                                                                final NfInstance seppInst,
                                                                final String tableName,
                                                                final Optional<List<NfPool>> pools)
    {
        var inspectedPools = pools.isPresent() ? pools.get() : seppInst.getNfPool();

        var kvTable = inspectedPools.stream()
                                    .filter(pool -> pool.getInResponseScreeningCaseRef() != null)
                                    .collect(Collectors.toMap(NfPool::getName, NfPool::getInResponseScreeningCaseRef));

        if (!kvTable.isEmpty())
        {
            seppFilter.addIntKvTable(tableName, kvTable);
        }
    }

    public static List<IfTypedNfInstance> getAllNfInstancesRp(IfNfPool pool,
                                                              IfNfInstance seppInst,
                                                              String support)
    {
        var staticNfInstances = Utils.streamIfExists(pool.getNfPoolDiscovery())
                                     .flatMap(p -> Utils.streamIfExists(p.getStaticNfInstanceDataRef()))
                                     .<IfStaticNfInstanceDatum>map(nfInstance -> Utils.getByName(seppInst.getStaticNfInstanceData(), nfInstance))
                                     .filter(Objects::nonNull)
                                     .<IfTypedNfInstance>flatMap(datum -> Utils.streamIfExists(datum.getStaticNfInstance()));

        var discoveredNfInstances = Utils.streamIfExists(pool.getNfPoolDiscovery())
                                         .<IfTypedNfInstance>flatMap(poolDisc -> Utils.streamIfExists(poolDisc.getDiscoveredNfInstance()));

        var nfInstList = Stream.concat(staticNfInstances, discoveredNfInstances).toList();

        nfInstList.stream()
                  .forEach(nfInstance -> Utils.streamIfExists(nfInstance.fetchNfService())
                                              .forEach(svc -> svc.setNfType(support.equalsIgnoreCase("true") ? "seppTar" : "seppNone")));

        return nfInstList;
    }

    public static boolean nfRequiresTfqdn(NfInstance seppInst,
                                          String nfType)
    {
        if (seppInst.getTelescopicFqdn() == null)
        {
            return false;
        }

        var requiredInstances = seppInst.getTelescopicFqdn().getRequiredForNfType();

        try
        {
            return requiredInstances.contains(RequiredForNfType.fromValue(nfType.toLowerCase()));
        }
        catch (IllegalArgumentException e)
        {
            return false;
        }
    }

    public static boolean isRoamingPartnerPool(NfInstance seppInst,
                                               String poolName)
    {
        var pool = Utils.getByName(seppInst.getNfPool(), poolName);

        return pool.getRoamingPartnerRef() != null;
    }

    public static boolean roamingPartnerRequiresTfqdn(NfInstance seppInst,
                                                      String poolName)
    {
        var pool = Utils.getByName(seppInst.getNfPool(), poolName);

        if (pool.getRoamingPartnerRef() != null)
        {
            var rp = Utils.getByName(seppInst.getExternalNetwork().stream().flatMap(enw -> enw.getRoamingPartner().stream()).toList(),
                                     pool.getRoamingPartnerRef());
            return rp.getSupportsTargetApiroot() != SupportsTargetApiroot.TRUE;
        }

        // Should not reach here
        return false;
    }

    /**
     * 
     * @param callbackUriList
     * @param callbackUriDefaultsList
     * 
     *                                • A user can configure elements in the
     *                                callback-uri list • If an element with the
     *                                same apiName + apiVersion is present in both
     *                                lists, the callback-uri-json-pointer in the
     *                                callback-uri list is used and whatever is in
     *                                callback-uri-defaults is ignored • If a user
     *                                configures a list with a single element where
     *                                the callback-uri-json-pointer is the empty
     *                                string (not an empty list!), it means that no
     *                                callback-uri translation is done for that
     *                                apiName + apiVersion (= it “deletes” the
     *                                default entry)
     * 
     * @return
     */
    public static Map<String, List<String>> getKlvTableCallbackUris(List<CallbackUrus> callbackUriList,
                                                                    List<CallbackUriDefault> callbackUriDefaultsList)
    {
        var klvTableCallbackUris = new HashMap<String, List<String>>();

        for (var callbackUriDefault : callbackUriDefaultsList)
        {
            klvTableCallbackUris.put(callbackUriDefault.getApiName() + "/v" + callbackUriDefault.getApiVersion(),
                                     callbackUriDefault.getCallbackUriJsonPointer());
        }

        for (var callbackUri : callbackUriList)
        {
            String key = callbackUri.getApiName() + "/v" + callbackUri.getApiVersion();

            if (klvTableCallbackUris.containsKey(key))
            {
                if (callbackUri.getCallbackUriJsonPointer() == null || callbackUri.getCallbackUriJsonPointer().isEmpty()
                    || callbackUri.getCallbackUriJsonPointer().get(0).isEmpty())
                {
                    klvTableCallbackUris.remove(key);
                }
                else
                {
                    klvTableCallbackUris.replace(key, callbackUri.getCallbackUriJsonPointer());
                }
            }
            else
            {
                klvTableCallbackUris.put(key, callbackUri.getCallbackUriJsonPointer());
            }
        }

        return klvTableCallbackUris;
    }

    public static Map<String, List<String>> getKlvTableCallbackUris(List<CallbackUriDefault> callbackUriDefaultsList)
    {
        var klvTableCallbackUris = new HashMap<String, List<String>>();

        for (var callbackUriDefault : callbackUriDefaultsList)
        {
            klvTableCallbackUris.put(callbackUriDefault.getApiName() + "/v" + callbackUriDefault.getApiVersion(),
                                     callbackUriDefault.getCallbackUriJsonPointer());
        }

        return klvTableCallbackUris;
    }

    /**
     * Return the values of the referenced failover-profile, given that it is
     * configured properly. If a failover-profile is not configured, return the
     * default values (same as in the Yang Model) in case a failover-profile is not
     * configured by the user Note: Timeouts are in milliseconds
     * 
     */
    public static IfFailoverProfile getConfiguredOrDefaultFailoverProfile(Optional<String> failoverProfName,
                                                                          List<FailoverProfile> failoverProfiles,
                                                                          boolean lastResortPoolPresent)
    {
        if (failoverProfName.isPresent())
        {
            var failoverProf = CommonConfigUtils.tryToGetFailoverProfile(failoverProfName.get(), failoverProfiles);
            if (failoverProf.isEmpty())
            {
                throw new BadConfigurationException("failover-profile {} could not be found in the failover-profile list", failoverProfName.get());
            }
            else
            {
                var failoverProfile = new FailoverProfile(failoverProf.get());

                if (!lastResortPoolPresent)
                {
                    failoverProfile.setLastResortNfPoolReselectsMax(0);
                }

                return failoverProfile;
            }
        }
        else
        {
            var retryCondition = new RetryCondition();
            var httpStatusDefault = List.of(500, 501, 502, 503, 504);
            retryCondition.setConnectFailure(true);
            retryCondition.setRefusedStream(true);
            retryCondition.setReset(true);
            retryCondition.setHttpStatus(httpStatusDefault);

            var fop = new FailoverProfile();
            fop.setName(null);
            fop.setRequestTimeBudget(2000);
            fop.setRetryCondition(retryCondition);
            fop.setTargetTimeout(2000);
            fop.setPreferredHostRetriesMax(3);
            fop.setTargetNfPoolReselectsMax(3);
            fop.setLastResortNfPoolReselectsMax(3);

            if (!lastResortPoolPresent)
            {
                fop.setLastResortNfPoolReselectsMax(0);
            }

            return fop;
        }
    }

    /**
     * @param nw
     */
    public static boolean isRateLimitConfiguredOnRP(ExternalNetwork nw)
    {
        var rpWithRl = nw.getRoamingPartner().stream().filter(rp -> !rp.getGlobalIngressRateLimitProfileRef().isEmpty()).toList();
        return !rpWithRl.isEmpty();
    }

    public static boolean isRateLimitConfigured(NfInstance seppInst)
    {
        for (var ownNw : seppInst.getOwnNetwork())
        {
            if (!ownNw.getGlobalIngressRateLimitProfileRef().isEmpty())
                return true;
        }
        for (var extNw : seppInst.getExternalNetwork())
        {
            if (!extNw.getGlobalIngressRateLimitProfileRef().isEmpty())
                return true;
            else if (isRateLimitConfiguredOnRP(extNw))
                return true;
        }
        return false;
    }

    /**
     * This function returns whether N32-c is enabled. Both on local n32-c
     * configuration and at least one roaming partner should have the n32-c
     * container configured.
     * 
     * @param seppInst The sepp instance
     * 
     * @return boolean
     * 
     */
    public static boolean isN32cConfigured(NfInstance seppInst)
    {
        var nfInstanceWithN32c = seppInst.getN32C() != null && !seppInst.getN32C().getOwnSecurityData().isEmpty();
        var extNwWithN32c = !getAllRoamingPartnersWithN32C(seppInst.getExternalNetwork()).isEmpty();
        return nfInstanceWithN32c && extNwWithN32c;
    }

    /**
     * This function returns whether N32-c is enabled on nf-instance
     * 
     * @param seppInst The sepp instance
     * 
     * @return boolean
     * 
     */
    public static boolean isN32cConfiguredNfInstance(NfInstance seppInst)
    {
        return seppInst.getN32C() != null && !seppInst.getN32C().getOwnSecurityData().isEmpty();
    }
}
