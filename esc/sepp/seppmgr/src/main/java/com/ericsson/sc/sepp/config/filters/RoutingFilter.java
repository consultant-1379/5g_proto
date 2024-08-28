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
 * Created on: Jan 7, 2022
 *     Author: eaoknkr
 */

package com.ericsson.sc.sepp.config.filters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.configutil.CommonConfigUtils;
import com.ericsson.sc.expressionparser.ConditionParser;
import com.ericsson.sc.glue.IfNetwork;
import com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig.ProxySeppFilter;
import com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig.ProxySeppFilter.Network;
import com.ericsson.sc.proxyal.proxyconfig.proxyroutingconfig.ProxyFilterCase;
import com.ericsson.sc.proxyal.proxyconfig.proxyroutingconfig.ProxyFilterData;
import com.ericsson.sc.proxyal.proxyconfig.proxyroutingconfig.ProxyFilterRule;
import com.ericsson.sc.proxyal.proxyconfig.proxyroutingconfig.ProxyRoamingPartner;
import com.ericsson.sc.proxyal.proxyconfig.proxyroutingconfig.ProxyRoutingAction;
import com.ericsson.sc.sepp.config.ConfigUtils;
import com.ericsson.sc.sepp.manager.TfqdnCallbackUriDefaults;
import com.ericsson.sc.sepp.model.ExternalNetwork;
import com.ericsson.sc.sepp.model.NfInstance;
import com.ericsson.sc.sepp.model.NfPool;
import com.ericsson.sc.sepp.model.OwnNetwork;
import com.ericsson.sc.sepp.model.RoamingPartner;
import com.ericsson.sc.sepp.model.RoamingPartner.SupportsTargetApiroot;
import com.ericsson.sc.sepp.model.RoutingCase;
import com.ericsson.sc.sepp.model.RoutingRule;
import com.ericsson.sc.sepp.model.ServiceAddress;
import com.ericsson.sc.sepp.model.TopologyHidingWithAdminState;
import com.ericsson.utilities.common.Utils;
import com.ericsson.utilities.exceptions.BadConfigurationException;

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.RoutingBehaviour;

/**
 * 
 */
public class RoutingFilter
{
    private ProxySeppFilter seppFilter;
    private final NfInstance seppInst;
    private final ServiceAddress svcAddress;
    private final boolean isTls;
    private final IfNetwork network;
    private static final String TABLE_NAME_RP_DOMAINS_NAMES = "domain_names_to_roaming_partners";
    private static final String CALLBACK_URI_KLVTABLE_NAME = "___callback_uris";
    private static final Logger log = LoggerFactory.getLogger(RoutingFilter.class);

    public RoutingFilter(ProxySeppFilter seppFilter,
                         NfInstance seppInstance,
                         ServiceAddress svcAddress,
                         boolean isTls,
                         IfNetwork network)
    {
        this.seppFilter = seppFilter;
        this.seppInst = seppInstance;
        this.svcAddress = svcAddress;
        this.isTls = isTls;
        this.network = network;
    }

    /**
     * Create the SEPP routing filter based on the given configuration.
     *
     */
    public void create()
    {
        var nwType = network instanceof ExternalNetwork ? Network.EXTERNAL : Network.INTERNAL;

        var defaultRoutingCase = network.getRoutingCaseRef();
        seppFilter.setRoutingRef(defaultRoutingCase);

        var routingRef = ConfigUtils.getReferencedRoutingCases(network);

        if (nwType.equals(Network.EXTERNAL))
        {
            ConfigUtils.createRoutingKvTableForRPs(seppFilter, seppInst, ProxySeppFilter.INTERNAL_ROUTING_TABLE_NAME);
            // create kv table for RP domain-names to names
            if (!ConfigUtils.getAllRoamingPartners(seppInst.getExternalNetwork()).isEmpty())
            {
                seppFilter.setRpNameTable(TABLE_NAME_RP_DOMAINS_NAMES);
                ConfigUtils.createKvTableRPDomainsToNames(seppFilter, seppInst, TABLE_NAME_RP_DOMAINS_NAMES);
            }

        }

        // create the referenced proxy routing cases
        seppInst.getRoutingCase().stream().filter(rc -> routingRef.contains(rc.getName())).forEach(rc ->
        {
            var proxyRoutingCase = createProxyRoutingCase(rc);

            seppFilter.addRoutingCase(proxyRoutingCase);

            this.appendRoamingPartners(seppInst, seppFilter, nwType);
        });

        // own-fqdn
        this.configureOwnFqdn(seppFilter);

        // set the own plmn-ids
        this.setProxyOwnPlmnIds(seppFilter, nwType);

        // add the list of nf-types that require Telescopic FQDN
        this.configureTfqdn(seppFilter);

        // add the list of nf-types that require Telescopic FQDN
        this.configureFqdnMap(seppFilter);

        // add the default allowed service operations list
        this.configureFirewallCat1(seppFilter);
    }

    /**
     * If a firewall profile with cat1 checks validation is referenced at least
     * once, then we consider it as configured and the default allowed operations
     * list is passed towards envoy
     *
     */
    private void configureFirewallCat1(ProxySeppFilter seppFilter)
    {
        boolean val = false;

        val = seppInst.getExternalNetwork()
                      .stream()
                      .filter(ext -> ext.getFirewallProfileRef() != null)
                      .anyMatch(extf -> Utils.getByName(seppInst.getFirewallProfile(), extf.getFirewallProfileRef()).getRequest() != null
                                        && Utils.getByName(seppInst.getFirewallProfile(), extf.getFirewallProfileRef())
                                                .getRequest()
                                                .getValidateServiceOperation() != null);

        if (val)
        {
            seppFilter.setIsFirewallCat1Conf(val);
            return;
        }

        val = ConfigUtils.getAllRoamingPartners(seppInst.getExternalNetwork())
                         .stream()
                         .filter(rp -> rp.getFirewallProfileRef() != null)
                         .anyMatch(rpf -> Utils.getByName(seppInst.getFirewallProfile(), rpf.getFirewallProfileRef()).getRequest() != null
                                          && Utils.getByName(seppInst.getFirewallProfile(), rpf.getFirewallProfileRef())
                                                  .getRequest()
                                                  .getValidateServiceOperation() != null);

        seppFilter.setIsFirewallCat1Conf(val);

    }

    private void configureFqdnMap(ProxySeppFilter seppFilter)
    {
        HashMap<String, String> mappingTable = new HashMap<>();
        HashMap<String, String> unMappingTable = new HashMap<>();
        seppInst.getNrfFqdnMappingTable().forEach(entry ->
        {
            mappingTable.put(entry.getInternalFqdn(), entry.getExternalFqdn());
            unMappingTable.put(entry.getExternalFqdn(), entry.getInternalFqdn());
        });

        if (!mappingTable.isEmpty())
        {
            seppFilter.addKvTable(ProxySeppFilter.INTERNAL_FQDN_MAPPING_TABLE_NAME, mappingTable);
            seppFilter.addKvTable(ProxySeppFilter.INTERNAL_FQDN_UNMAPPING_TABLE_NAME, unMappingTable);
        }

    }

    /**
     * @param seppFilter
     * 
     */
    private void configureTfqdn(ProxySeppFilter seppFilter)
    {
        if (seppInst.getTelescopicFqdn() != null)
        {
            var defaultCbUriList = new TfqdnCallbackUriDefaults().getCallbackUriDefaults();
            var tf = seppInst.getTelescopicFqdn();
            if (tf.getRequiredForNfType() != null && !tf.getRequiredForNfType().isEmpty())
            {
                for (var nf : tf.getRequiredForNfType())
                {
                    seppFilter.addNfTypesRequiringTFqdn(nf.toString());
                }
            }

            if (tf.getCallbackUri() != null && !tf.getCallbackUri().isEmpty())
            {
                seppFilter.addKlvTable(CALLBACK_URI_KLVTABLE_NAME, ConfigUtils.getKlvTableCallbackUris(tf.getCallbackUri(), defaultCbUriList));
            }
            else
            {
                seppFilter.addKlvTable(CALLBACK_URI_KLVTABLE_NAME, ConfigUtils.getKlvTableCallbackUris(defaultCbUriList));
            }
        }
    }

    /**
     * 
     */
    private void configureOwnFqdn(ProxySeppFilter seppFilter)
    {
        if (svcAddress.getFqdn() != null)
        {
            seppFilter.setOwnFqdn(svcAddress.getFqdn());
        }

        if (network instanceof OwnNetwork)
        {
            if (isTls)
            {
                seppFilter.setOwnInternalPort(svcAddress.getTlsPort());
            }
            else
            {
                seppFilter.setOwnInternalPort(svcAddress.getPort());
            }
        }
        else
        {
            if (isTls)
            {
                seppFilter.setOwnExternalPort(svcAddress.getTlsPort());
            }
            else
            {
                seppFilter.setOwnExternalPort(svcAddress.getPort());
            }
        }
    }

    private void appendRoamingPartners(NfInstance seppInst,
                                       ProxySeppFilter seppFilter,
                                       Network nwType)
    {
        seppInst.getExternalNetwork().forEach(extNtw ->
        {
            var rpList = ConfigUtils.getAllRoamingPartners(List.of(extNtw));
            var thFromExtNtw = extNtw.getTopologyHidingRef();
            var firewallProfileFromExtNw = extNtw.getFirewallProfileRef();
            var thAdminStateFromExtNw = extNtw.getTopologyHidingWithAdminState();
            var serviceAddreesRef = extNtw.getServiceAddressRef();

            rpList.forEach(rp ->
            {

                // Get routing action that references this Roaming Partner
                var routingActions = ConfigUtils.fetchRoutingActionsTargetRoamingPartner(rp.getName(), seppInst.getRoutingCase());

                /*
                 * If a routing action exists, ProxyRoamingPartner should have nfPool and a
                 * suffix in name. Otherwise, create ProxyRoamingPartner with just the name
                 */
                if (routingActions.isEmpty())
                {
                    var proxyRp = new ProxyRoamingPartner(rp.getName());
                    setProxyRoamingPartnerTopologyHiding(rp, thFromExtNtw, thAdminStateFromExtNw, proxyRp);
                    setProxyRoamingPartnerFirewall(rp, firewallProfileFromExtNw, proxyRp);
                    setProxyRoamingPartnerOwnNetworkFqdn(serviceAddreesRef, proxyRp, nwType);
                    setProxyRoamingPartnerPlmnIds(rp, proxyRp, nwType);
                    if (!seppFilter.getRoamingPartners().contains(proxyRp))
                    {
                        seppFilter.addRoamingPartner(proxyRp);
                    }
                    log.debug("Adding simple RP to KVT");
                    return;
                }

                routingActions.forEach(ra ->
                {
                    var suffix = CommonConfigUtils.buildClusterNameSuffix(Optional.ofNullable(ra.getActionRouteRoundRobin().getLastResortNfPoolRef()));
                    var pool = ConfigUtils.fetchNfPoolFromRoamingPartner(rp.getName(), seppInst.getNfPool())
                                          .orElseThrow(() -> new BadConfigurationException("Nf-pool could not be found for the roaming partner {} in routing-action {}",
                                                                                           rp.getName(),
                                                                                           ra.getName()));
                    var proxyRp = new ProxyRoamingPartner(ra.getActionRouteRoundRobin().getTargetRoamingPartner().getRoamingPartnerRef() + suffix,
                                                          pool.getName() + suffix);
                    log.debug("Adding proxy RP {} from action {}", proxyRp, ra.getName());
                    setProxyRoamingPartnerTopologyHiding(rp, thFromExtNtw, thAdminStateFromExtNw, proxyRp);
                    setProxyRoamingPartnerFirewall(rp, firewallProfileFromExtNw, proxyRp);
                    setProxyRoamingPartnerOwnNetworkFqdn(serviceAddreesRef, proxyRp, nwType);
                    setProxyRoamingPartnerPlmnIds(rp, proxyRp, nwType);
                    if (!seppFilter.getRoamingPartners().contains(proxyRp))
                    {
                        seppFilter.addRoamingPartner(proxyRp);
                    }
                });

                var pool = ConfigUtils.fetchNfPoolFromRoamingPartner(rp.getName(), seppInst.getNfPool())
                                      .orElseThrow(() -> new BadConfigurationException("Nf-pool could not be found for the roaming partner {}", rp.getName()));
                var proxyRp = new ProxyRoamingPartner(rp.getName(), pool.getName());
                setProxyRoamingPartnerTopologyHiding(rp, thFromExtNtw, thAdminStateFromExtNw, proxyRp);
                setProxyRoamingPartnerFirewall(rp, firewallProfileFromExtNw, proxyRp);
                setProxyRoamingPartnerOwnNetworkFqdn(serviceAddreesRef, proxyRp, nwType);
                setProxyRoamingPartnerPlmnIds(rp, proxyRp, nwType);
                if (!seppFilter.getRoamingPartners().contains(proxyRp))
                {
                    seppFilter.addRoamingPartner(proxyRp);
                }
            });
        });
    }

    /**
     * If RoamingPartner has its own Topology Hiding configured, use those.
     * Otherwise use Topology Hiding from external-network. If both are not
     * configured do nothing.
     * 
     * @param rp
     * @param thFromExtNtw
     * @param proxyRp
     */
    private void setProxyRoamingPartnerTopologyHiding(RoamingPartner rp,
                                                      List<String> thFromExtNtw,
                                                      List<TopologyHidingWithAdminState> thAdminStateFromExtNtw,
                                                      ProxyRoamingPartner proxyRp)
    {
        var thFromRp = rp.getTopologyHidingRef();
        var thAdminStateFromRp = rp.getTopologyHidingWithAdminState();

        var proxyTh = ConfigUtils.createTopologyHiding(seppInst, thFromRp, thAdminStateFromRp);
        if (proxyTh.isEmpty())
        {
            proxyTh = ConfigUtils.createTopologyHiding(seppInst, thFromExtNtw, thAdminStateFromExtNtw);
        }

        if (proxyTh.isPresent())
        {
            proxyRp.setTopologyHiding(proxyTh);
        }
    }

    private void setProxyRoamingPartnerFirewall(RoamingPartner rp,
                                                String firewallRefFromExtNtw,
                                                ProxyRoamingPartner proxyRp)
    {
        var firewallProfOfRp = rp.getFirewallProfileRef() != null ? rp.getFirewallProfileRef() : firewallRefFromExtNtw;

        var requestMessageValidation = ConfigUtils.createRequestMessageValidation(seppInst, firewallProfOfRp);
        var responseMessageValidation = ConfigUtils.createResponseMessageValidation(seppInst, firewallProfOfRp);

        if (requestMessageValidation.isPresent())
        {
            proxyRp.setRequestMessageValidation(requestMessageValidation.get());
        }
        if (responseMessageValidation.isPresent())
        {
            proxyRp.setResponseMessageValidation(responseMessageValidation.get());
        }

    }

    private void setProxyRoamingPartnerOwnNetworkFqdn(String serviceAddreesRef,
                                                      ProxyRoamingPartner proxyRp,
                                                      Network nwType)
    {
        if (nwType.equals(Network.EXTERNAL))
        {
            serviceAddreesRef = seppInst.getOwnNetwork().get(0).getServiceAddressRef();
        }
        ConfigUtils.setOwnNetworkFqdn(seppInst, serviceAddreesRef, proxyRp);
    }

    private void setProxyRoamingPartnerPlmnIds(RoamingPartner rp,
                                               ProxyRoamingPartner proxyRp,
                                               Network nwType)
    {
        if (nwType.equals(Network.EXTERNAL))
        {
            ConfigUtils.setRoamingPartnerPlmnIds(seppInst, rp, proxyRp);
        }
    }

    private void setProxyOwnPlmnIds(ProxySeppFilter seppFilter,
                                    Network nwType)
    {
        if (nwType.equals(Network.INTERNAL))
        {
            ConfigUtils.setOwnPlmnIds(seppInst, seppFilter);
        }
    }

    private ProxyFilterCase createProxyRoutingCase(RoutingCase rc)
    {
        var proxyRoutingCase = new ProxyFilterCase(rc.getName());

        // ******ProxyRoutingCase: create the proxy routing data
        for (var rdName : rc.getMessageDataRef())
        {
            var rd = Utils.getByName(seppInst.getMessageData(), rdName);
            proxyRoutingCase.addFilterData(new ProxyFilterData(rd.getName(),
                                                               rd.getVariableName(),
                                                               rd.getExtractorRegex(),
                                                               rd.getPath() != null,
                                                               rd.getBodyJsonPointer(),
                                                               rd.getHeader(),
                                                               rd.getRequestHeader(),
                                                               rd.getResponseHeader()));
        }

        // ******ProxyRoutingCase: and now create the proxy routing rules
        for (var rr : rc.getRoutingRule())
        {
            var expression = ConditionParser.parse(rr.getCondition());

            var hasSR = rr.getRoutingAction().stream().anyMatch(CommonConfigUtils::isStrictRoutingRule);

            if (hasSR)
            {
                var raSRList = createSRRoutingActionList(rr);
                if (raSRList.getLeft() != null && !raSRList.getLeft().isEmpty())
                    proxyRoutingCase.addFilterRule(new ProxyFilterRule(rr.getName(), expression, RoutingBehaviour.STRICT, raSRList.getLeft())); // STRICT
                if (raSRList.getRight() != null && !raSRList.getRight().isEmpty())
                    proxyRoutingCase.addFilterRule(new ProxyFilterRule(rr.getName(), expression, RoutingBehaviour.STRICT_DFP, raSRList.getRight())); // DYN FWD
            }

            var hasPR = rr.getRoutingAction().stream().anyMatch(CommonConfigUtils::isPreferredRoutingRule);

            if (hasPR)
            {
                var raPRList = createPRRoutingActionList(rr);
                proxyRoutingCase.addFilterRule(new ProxyFilterRule(rr.getName(), expression, RoutingBehaviour.PREFERRED, raPRList)); // PREFERRED
            }

            var hasRR = rr.getRoutingAction().stream().anyMatch(CommonConfigUtils::isRoundRobinRule);

            if (hasRR)
            {
                var raRRList = createRRRoutingActionList(rr);
                proxyRoutingCase.addFilterRule(new ProxyFilterRule(rr.getName(), expression, RoutingBehaviour.ROUND_ROBIN, raRRList));
            }

            var hasOtherRoutingAction = rr.getRoutingAction().stream().anyMatch(CommonConfigUtils::isOtherRoutingRule);

            if (!hasSR && !hasPR && !hasRR && hasOtherRoutingAction)
            {
                var raOtherList = createOtherRoutingActionList(rr);
                proxyRoutingCase.addFilterRule(new ProxyFilterRule(rr.getName(), expression, RoutingBehaviour.ROUND_ROBIN, raOtherList));
            }

        }
        return proxyRoutingCase;
    }

    /**
     * Creates a list of routing actions based on the provided routing Rule Used for
     * routing rules that are not under Preferred or Strict Routing behavior.
     * Routing actions targeting a RP who does not have TaR support are marked with
     * the kee_authority_header param, which is used by eric_proxy for proper
     * handling
     * 
     * @param routingRule
     * @return List<ProxyRoutingAction>
     */
    private List<ProxyRoutingAction> createRRRoutingActionList(RoutingRule routingRule)
    {
        return routingRule.getRoutingAction()
                          .stream()
                          .filter(ra -> CommonConfigUtils.isRoundRobinRule(ra) || CommonConfigUtils.isOtherRoutingRule(ra))
                          .flatMap(ra -> ConfigUtils.createRoutingAction(ra).stream())
                          .map(ra ->
                          {
                              // If the routing action targets a RP, check if the RP supports TaR. If not, set
                              // the keep_auth_header
                              // param on the routing action
                              if (ra.getDestinationRoamingPartner().isPresent())
                              {
                                  var rp = Utils.getByName(ConfigUtils.getAllRoamingPartners(seppInst.getExternalNetwork()),
                                                           ra.getDestinationRoamingPartner().get());
                                  if (rp != null && rp.getSupportsTargetApiroot() != null && rp.getSupportsTargetApiroot().equals(SupportsTargetApiroot.FALSE))
                                  {
                                      ra.setKeepAuthorityHeader(true);
                                  }
                              }
                              return ra;
                          })
                          .collect(Collectors.toList());

    }

    /**
     * Used for routing rules with Preferred Routing behavior
     * 
     * @param routingRule
     * @return List<ProxyRoutingAction>
     */
    private List<ProxyRoutingAction> createPRRoutingActionList(RoutingRule routingRule)
    {
        return routingRule.getRoutingAction()
                          .stream()
                          .filter(ra -> CommonConfigUtils.isPreferredRoutingRule(ra) || CommonConfigUtils.isOtherRoutingRule(ra))
                          .flatMap(action -> ConfigUtils.createRoutingAction(action).stream())
                          .collect(Collectors.toList());

    }

    /**
     * Used for routing rules with Strict Routing behavior and Dynamic forward in
     * two different lists
     * 
     * @param routingRule
     * @return Pair<List<ProxyRoutingAction>, List<ProxyRoutingAction>>
     */
    private Pair<List<ProxyRoutingAction>, List<ProxyRoutingAction>> createSRRoutingActionList(RoutingRule routingRule)
    {
        List<ProxyRoutingAction> srList = new ArrayList<>();
        List<ProxyRoutingAction> dynFwdList = new ArrayList<>();

        srList.addAll(routingRule.getRoutingAction()
                                 .stream()
                                 .filter(CommonConfigUtils::isOtherRoutingRule)
                                 .flatMap(action -> ConfigUtils.createRoutingAction(action).stream())
                                 .collect(Collectors.toList()));

        routingRule.getRoutingAction().stream().filter(CommonConfigUtils::isStrictRoutingRule).forEach(action ->

        {
            List<NfPool> pools;
            var actionStrict = action.getActionRouteStrict();

            if (actionStrict.getTargetNfPool() == null)
            {
                throw new BadConfigurationException("No valid destination for routing rule  {}", action.getName());
            }

            if (actionStrict.getTargetNfPool().getNfPoolRef() != null)
            {
                var referencedPool = Utils.getByName(this.seppInst.getNfPool(), actionStrict.getTargetNfPool().getNfPoolRef());
                pools = List.of(referencedPool);
            }
            else
            {
                pools = this.seppInst.getNfPool();
            }

            // In case the referenced pool or if not configured, any of the sepp instance
            // pools make the dynamic forwarding condition true, then we categorize this as
            // dynamic forwarding
            var isDynfwd = pools.stream()
                                .filter(pool -> (pool.getStaticScpInstanceDataRef() == null || pool.getStaticScpInstanceDataRef().isEmpty())
                                                && (pool.getStaticSeppInstanceDataRef() == null || pool.getStaticSeppInstanceDataRef().isEmpty())
                                                && (pool.getNfPoolDiscovery() == null || pool.getNfPoolDiscovery().isEmpty()))
                                .findFirst();

            if (isDynfwd.isPresent())
                dynFwdList.addAll(ConfigUtils.createRoutingAction(action));
            else
                srList.addAll(ConfigUtils.createRoutingAction(action));
        });
        return Pair.of(srList, dynFwdList);
    }

    /**
     * Used for routing rules that are not Preferred or Strict or Round-robin
     * Routing behavior
     * 
     * @param routingRule
     * @return List<ProxyRoutingAction>
     */
    private List<ProxyRoutingAction> createOtherRoutingActionList(RoutingRule routingRule)
    {
        return routingRule.getRoutingAction()
                          .stream()
                          .filter(CommonConfigUtils::isOtherRoutingRule)
                          .flatMap(action -> ConfigUtils.createRoutingAction(action).stream())
                          .collect(Collectors.toList());
    }
}