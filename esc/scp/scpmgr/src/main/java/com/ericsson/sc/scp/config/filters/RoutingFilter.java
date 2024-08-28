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
 * Created on: Jan 18, 2022
 *     Author: eaoknkr
 */

package com.ericsson.sc.scp.config.filters;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.ericsson.sc.configutil.CommonConfigUtils;
import com.ericsson.sc.expressionparser.ConditionParser;
import com.ericsson.sc.glue.IfNetwork;
import com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig.ProxySeppFilter;
import com.ericsson.sc.proxyal.proxyconfig.proxyroutingconfig.ProxyFilterCase;
import com.ericsson.sc.proxyal.proxyconfig.proxyroutingconfig.ProxyFilterRule;
import com.ericsson.sc.proxyal.proxyconfig.proxyroutingconfig.ProxyRoutingAction;
import com.ericsson.sc.scp.config.ConfigUtils;
import com.ericsson.sc.scp.model.NfInstance;
import com.ericsson.sc.scp.model.NfPool;
import com.ericsson.sc.scp.model.RoutingAction;
import com.ericsson.sc.scp.model.RoutingCase;
import com.ericsson.sc.scp.model.RoutingRule;
import com.ericsson.utilities.common.Utils;

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.RoutingBehaviour;
import com.ericsson.utilities.exceptions.BadConfigurationException;

/**
 * 
 */
public class RoutingFilter
{
    private ProxySeppFilter scpFilter;
    private NfInstance scpInst;
    private final boolean isTls;
    private IfNetwork network;

    public RoutingFilter(ProxySeppFilter scpFilter,
                         NfInstance scpInstance,
                         boolean isTls,
                         IfNetwork network)
    {
        this.scpFilter = scpFilter;
        this.scpInst = scpInstance;
        this.isTls = isTls;
        this.network = network;
    }

    /**
     * Create the SCP routing filter based on the given configuration.
     *
     */
    public void create()
    {
        var defaultRoutingCase = network.getRoutingCaseRef();
        scpFilter.setRoutingRef(defaultRoutingCase);
        var routingRef = ConfigUtils.getReferencedRoutingCases(scpInst, network);

        if (this.scpInst.getSlfLookupProfile() != null && !this.scpInst.getSlfLookupProfile().isEmpty())
        {
            var defaultRcForSlf = ConfigUtils.createDefaultRoutingCasesForSlf();
            defaultRcForSlf.stream().forEach(scpFilter::addRoutingCase);
        }

        // create the proxy routing cases. Add all cases in the filter
        scpInst.getRoutingCase().stream().filter(rc -> routingRef.contains(rc.getName())).forEach(rc ->
        {
            var proxyRoutingCase = createProxyRoutingCase(rc);

            scpFilter.addRoutingCase(proxyRoutingCase);
        });

        // configure fqdn and port
        var svcAddress = Utils.getByName(scpInst.getServiceAddress(), network.getServiceAddressRef());

        if (svcAddress.getFqdn() != null)
        {
            scpFilter.setOwnFqdn(svcAddress.getFqdn());
        }

        if (isTls)
        {
            scpFilter.setOwnInternalPort(svcAddress.getTlsPort());
        }
        else
        {
            scpFilter.setOwnInternalPort(svcAddress.getPort());
        }
    }

    private ProxyFilterCase createProxyRoutingCase(RoutingCase rc)
    {
        final var proxyRoutingCase = new ProxyFilterCase(rc.getName());

        // ******ProxyRoutingCase: create the proxy routing data
        for (var rdName : rc.getMessageDataRef())
        {
            var rd = Utils.getByName(scpInst.getMessageData(), rdName);
            proxyRoutingCase.addFilterData(ConfigUtils.createProxyMessageData(rd));
        }
        // ******ProxyRoutingCase: and now create the proxy routing rules
        for (var rr : rc.getRoutingRule())
        {
            var expression = ConditionParser.parse(rr.getCondition());

            var hasNfDisc = rr.getRoutingAction().stream().anyMatch(ConfigUtils::isNfDiscoveryRoutingRule);

            if (hasNfDisc)
            {
                var raNfDiscList = createNfDiscRoutingActionList(rr);
                proxyRoutingCase.addFilterRule(new ProxyFilterRule(rr.getName(), expression, RoutingBehaviour.ROUND_ROBIN, raNfDiscList));
            }

            var hasSR = rr.getRoutingAction().stream().anyMatch(CommonConfigUtils::isStrictRoutingRule);

            // Strict routing is contains also the dynamic fowarding proxy case with
            // different routing behavior metadatum in envoy
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

            var hasRRR = rr.getRoutingAction().stream().anyMatch(ConfigUtils::isRemoteRoundRobinAction);

            if (hasRRR)
            {
                var raRRRList = createRRRRoutingActionList(rr);
                proxyRoutingCase.addFilterRule(new ProxyFilterRule(rr.getName(), expression, RoutingBehaviour.REMOTE_ROUND_ROBIN, raRRRList));
            }

            var hasRP = rr.getRoutingAction().stream().anyMatch(ConfigUtils::isRemotePreferredAction);

            if (hasRP)
            {
                var raRPList = createRPRoutingActionList(rr);

                proxyRoutingCase.addFilterRule(new ProxyFilterRule(rr.getName(), expression, RoutingBehaviour.REMOTE_PREFERRED, raRPList));

            }

            var hasOtherRoutingAction = rr.getRoutingAction().stream().anyMatch(RoutingFilter::isOtherRoutingAction);

            if (!hasSR && !hasPR && !hasRR && !hasRRR && !hasRP && !hasNfDisc && hasOtherRoutingAction)
            {
                var raOtherList = createOtherRoutingActionList(rr);
                proxyRoutingCase.addFilterRule(new ProxyFilterRule(rr.getName(), expression, RoutingBehaviour.ROUND_ROBIN, raOtherList));
            }

            if (ConfigUtils.isSlfPresent(rr))
            {
                ConfigUtils.createKvTableForSlf(rr, scpFilter, this.scpInst);
                ConfigUtils.copySlfMessageDataToRoutingCase(rr, this.scpInst, proxyRoutingCase);
            }
        }

        return proxyRoutingCase;
    }

    /**
     * Checks if the routing action is other than [Remote] Round-robin or [Remote]
     * Preferred or Strict or Nf Discovery
     * 
     * @param routingAction
     * @return
     */
    private static boolean isOtherRoutingAction(RoutingAction routingAction)
    {
        return ConfigUtils.isSlfRoutingRule(routingAction) || CommonConfigUtils.isOtherRoutingRule(routingAction);
    }

    /**
     * Used for routing rules that are not under Preferred or Strict Routing
     * behavior
     * 
     * @param routingRule
     * @return List<ProxyRoutingAction>
     */
    private List<ProxyRoutingAction> createRRRoutingActionList(RoutingRule routingRule)
    {
        return routingRule.getRoutingAction()
                          .stream()
                          .filter(ra -> CommonConfigUtils.isRoundRobinRule(ra) || isOtherRoutingAction(ra))
                          .flatMap(action -> ConfigUtils.createRoutingAction(action, routingRule.getName(), scpInst).stream())
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
                          .filter(ra -> CommonConfigUtils.isPreferredRoutingRule(ra) || isOtherRoutingAction(ra))
                          .flatMap(action -> ConfigUtils.createRoutingAction(action, routingRule.getName(), scpInst).stream())
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
                                 .filter(RoutingFilter::isOtherRoutingAction)
                                 .flatMap(action -> ConfigUtils.createRoutingAction(action, routingRule.getName(), scpInst).stream())
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
                var referencedPool = Utils.getByName(this.scpInst.getNfPool(), actionStrict.getTargetNfPool().getNfPoolRef());
                pools = List.of(referencedPool);
            }
            else
            {
                pools = this.scpInst.getNfPool();
            }

            // In case the referenced pool or if not configured, any of the scp instance
            // pools make the dynamic forwarding condition true, then we categorize this as
            // dynamic forwarding
            var isDynfwd = pools.stream()
                                .filter(pool -> (pool.getStaticScpInstanceDataRef() == null || pool.getStaticScpInstanceDataRef().isEmpty())
                                                && (pool.getStaticSeppInstanceDataRef() == null || pool.getStaticSeppInstanceDataRef().isEmpty())
                                                && (pool.getNfPoolDiscovery() == null || pool.getNfPoolDiscovery().isEmpty()))
                                .findFirst();

            if (isDynfwd.isPresent())
                dynFwdList.addAll(ConfigUtils.createRoutingAction(action, routingRule.getName(), scpInst));
            else
                srList.addAll(ConfigUtils.createRoutingAction(action, routingRule.getName(), scpInst));
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
                          .filter(RoutingFilter::isOtherRoutingAction)
                          .flatMap(action -> ConfigUtils.createRoutingAction(action, routingRule.getName(), scpInst).stream())
                          .collect(Collectors.toList());
    }

    /**
     * Used for routing rules that are under Remote Round Robin behavior
     * 
     * @param routingRule
     * @return List<ProxyRoutingAction>
     */
    private List<ProxyRoutingAction> createRRRRoutingActionList(RoutingRule routingRule)
    {
        return routingRule.getRoutingAction()
                          .stream()
                          .filter(ra -> ConfigUtils.isRemoteRoundRobinAction(ra) || isOtherRoutingAction(ra))
                          .flatMap(action -> ConfigUtils.createRoutingAction(action, routingRule.getName(), scpInst).stream())
                          .collect(Collectors.toList());
    }

    /**
     * Used for routing rules that are under Nf Discovery behavior
     * 
     * @param routingRule
     * @return List<ProxyRoutingAction>
     */
    private List<ProxyRoutingAction> createNfDiscRoutingActionList(RoutingRule routingRule)
    {
        return routingRule.getRoutingAction()
                          .stream()
                          .filter(ra -> ConfigUtils.isNfDiscoveryRoutingRule(ra) || isOtherRoutingAction(ra))
                          .flatMap(action -> ConfigUtils.createRoutingAction(action, routingRule.getName(), scpInst).stream())
                          .collect(Collectors.toList());
    }

    /**
     * Used for routing rules that are under Remote Preferred behavior
     * 
     * @param routingRule
     * @return List<ProxyRoutingAction>
     */
    private List<ProxyRoutingAction> createRPRoutingActionList(RoutingRule routingRule)
    {
        return routingRule.getRoutingAction()
                          .stream()
                          .filter(ra -> ConfigUtils.isRemotePreferredAction(ra) || isOtherRoutingAction(ra))
                          .flatMap(action -> ConfigUtils.createRoutingAction(action, routingRule.getName(), scpInst).stream())
                          .collect(Collectors.toList());
    }
}
