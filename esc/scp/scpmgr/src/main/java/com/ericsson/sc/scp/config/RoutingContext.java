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
 * Created on: Sep 1, 2020
 *     Author: eedrak
 */

package com.ericsson.sc.scp.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.configutil.CommonConfigUtils;
import com.ericsson.sc.configutil.ServiceConfig;
import com.ericsson.sc.glue.IfNetwork;
import com.ericsson.sc.glue.IfRoutingContext;
import com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig.ProxyRoute;
import com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig.ProxyRoute.RouteTypes;
import com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig.ProxyRouteMatch;
import com.ericsson.sc.scp.model.NfInstance;
import com.ericsson.sc.scp.model.OwnNetwork;
import com.ericsson.utilities.common.Utils;

/**
 * 
 */
public class RoutingContext implements IfRoutingContext
{

    private static final Logger log = LoggerFactory.getLogger(RoutingContext.class);
    private final NfInstance scpInst;

    // Service routes are keyed to the VHost-Name
    private Map<String, Map<String, List<ProxyRoute>>> serviceRoutesPerVHostNamePerListener = new HashMap<>();
    private Map<String, OwnNetwork> ownNetworkPerListener = new HashMap<>();

    public RoutingContext(NfInstance scpInst)
    {
        this.scpInst = scpInst;
    }

    @Override
    public void convertConfig()
    {
        createRoutes();
    }

    /**
     * Return all service routes for a given VHost/service-name and the given
     * listener
     * 
     * @param listenerName
     * @param serviceName
     * @return
     */
    @Override
    public List<ProxyRoute> getServiceRoutesForListener(String listenerName,
                                                        String serviceName)
    {
        return this.serviceRoutesPerVHostNamePerListener.get(listenerName).get(serviceName);
    }

    /**
     * Create all required service routes, i.e. routes not configured by the user,
     * but required to provide the service both to internal clients and our
     * roaming-partners.
     * 
     */
    private void createRoutes()
    {
        scpInst.getOwnNetwork().forEach(nw ->
        {

            var svcAddr = Utils.getByName(scpInst.getServiceAddress(), nw.getServiceAddressRef());
            if (svcAddr.getPort() != null)
            {
                var listenerName = CommonConfigUtils.getListenerName(svcAddr, false);
                ownNetworkPerListener.put(listenerName, nw);
                serviceRoutesPerVHostNamePerListener.put(listenerName, new HashMap<>());
            }
            if (svcAddr.getTlsPort() != null)
            {
                var listenerName = CommonConfigUtils.getListenerName(svcAddr, true);
                ownNetworkPerListener.put(listenerName, nw);
                serviceRoutesPerVHostNamePerListener.put(listenerName, new HashMap<>());
            }

        });

        addInternalServiceRoute();
        addInternalForward400Route();
        addInternalForwardRoute();
    }

    /**
     * Add a dummy 418 route under the internal-forwarding-VHost ("*"-domain
     * catch-all VHost) to send a direct reply to incoming messages that do not
     * match the own-VHost nor any roaming-partner-VHosts.
     * 
     * Added to every listener
     */
    private void addInternalForward400Route()
    {
        var route400 = new ProxyRoute("not_found",
                                      new ProxyRouteMatch().setPrefix("/"),
                                      418, // the status code 418 will be mapped to 400 in local reply config
                                      "Status code will be mapped to '400' when response will be re-written in "
                                          + "the local reply filter, with detail Route not found (No VHost match)",
                                      ProxyRoute.RoutePriorities.PRI_SEPP_NOT_FOUND,
                                      null);

        this.serviceRoutesPerVHostNamePerListener.keySet().stream().forEach(listener ->
        {
            var list = this.serviceRoutesPerVHostNamePerListener.get(listener).getOrDefault(ServiceConfig.INT_FORWARD_SERVICE, new ArrayList<>());
            list.add(route400);
            this.serviceRoutesPerVHostNamePerListener.get(listener).put(ServiceConfig.INT_FORWARD_SERVICE, list);
        });
    }

    /**
     * Add an internal service route that forwards traffic coming in from the inside
     * of our network to the LUA so that the SEPP service processing can be done.
     * This is a catch-all route at the end that has to be there because Envoy
     * doesn't send any request to LUA unless a route matches.
     * 
     * This route is different for every listener, based on the own-network that is
     * using it
     */
    private void addInternalServiceRoute()
    {
        this.serviceRoutesPerVHostNamePerListener.keySet().stream().forEach(listener ->
        {
            var list = this.serviceRoutesPerVHostNamePerListener.get(listener).getOrDefault(ServiceConfig.INT_SERVICE, new ArrayList<>());
            list.add(getCatchAllRoute(listener));

            this.serviceRoutesPerVHostNamePerListener.get(listener).put(ServiceConfig.INT_SERVICE, list);
        });
    }

    /**
     * Add an internal forward route that forwards traffic coming in from the inside
     * of our network to the x-eric-proxy filter so that the SCP service processing
     * can be done. This is a catch-all route at the beginning that has to be there
     * because Envoy doesn't send any request to the filter unless a route matches.
     */
    private void addInternalForwardRoute()
    {
        this.serviceRoutesPerVHostNamePerListener.keySet().stream().forEach(listener ->
        {
            var list = this.serviceRoutesPerVHostNamePerListener.get(listener).getOrDefault(ServiceConfig.INT_FORWARD_SERVICE, new ArrayList<>());
            list.add(getCatchAllRoute(listener));

            this.serviceRoutesPerVHostNamePerListener.get(listener).put(ServiceConfig.INT_FORWARD_SERVICE, list);
        });
    }

    /**
     * Create and return a catch-all-route at the beginning of the routing table.
     * This route is there to catch the request when coming in and direct it to
     * x-eric-proxy filter. After the filter processing a cluster should be the
     * result and the routes are evaluated again by Envoy, this time selecting a
     * different route. In case the filter processing does not find a route, a 404
     * is returned.
     * 
     * This route is different for every listener, based on the own-network that is
     * using it
     * 
     * @return
     */
    private ProxyRoute getCatchAllRoute(String listener)
    {
        return new ProxyRoute("catch_all",
                              new ProxyRouteMatch().addPresentValueHeader(Egress.HEADER_ERIC_PROXY, false).setPrefix("/"),
                              null,
                              null,
                              "not_used",
                              RouteTypes.ROUND_ROBIN,
                              ProxyRoute.RoutePriorities.PRI_SEPP_CATCH_ALL,
                              null,
                              30.0);
    }

    /**
     * We have manipulate input data here, such as duplicating backslashes.
     * 
     * @param regexp
     * @return
     */
    public String getNormalizedExtractorRegexp(String regexp)
    {
        log.debug("input regexp:{}.", regexp);
        var nomalizedRegexp = CommonConfigUtils.normalizeExtractorRegexp(regexp);

        log.debug("nomalizedRegexp regexp:{}.", nomalizedRegexp);
        return nomalizedRegexp;
    }

    @Override
    public Optional<IfNetwork> getNetworkForListener(String listenerName)
    {
        return Optional.ofNullable(ownNetworkPerListener.get(listenerName));
    }

}
