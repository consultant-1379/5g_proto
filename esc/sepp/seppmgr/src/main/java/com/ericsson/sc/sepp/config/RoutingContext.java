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

package com.ericsson.sc.sepp.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.configutil.CommonConfigUtils;
import com.ericsson.sc.configutil.ServiceConfig;
import com.ericsson.sc.glue.IfNetwork;
import com.ericsson.sc.glue.IfRoutingContext;
import com.ericsson.sc.proxy.ProxyConstants.METADATA;
import com.ericsson.sc.proxyal.proxyconfig.MetadataDoubleValue;
import com.ericsson.sc.proxyal.proxyconfig.MetadataListValue;
import com.ericsson.sc.proxyal.proxyconfig.MetadataStringValue;
import com.ericsson.sc.proxyal.proxyconfig.ProxyMetadataMap;
import com.ericsson.sc.proxyal.proxyconfig.ProxyMetadataBuilder.MetaDataType;
import com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig.ProxyRoute;
import com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig.ProxyRoute.RouteTypes;
import com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig.ProxyRouteMatch;
import com.ericsson.sc.sepp.model.ExternalNetwork;
import com.ericsson.sc.sepp.model.NfInstance;
import com.ericsson.sc.sepp.model.OperationalState;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.common.Triplet;
import com.ericsson.utilities.common.Utils;

/**
 * 
 */
public class RoutingContext implements IfRoutingContext
{

    private static final Logger log = LoggerFactory.getLogger(RoutingContext.class);

    private final NfInstance seppInst;
    private final Optional<Map<String, Triplet<String, String, Boolean>>> n32cStateData;
    private int ineligibleSansVersion;
    // Service routes are keyed to the VHost-Name
    private Map<String, Map<String, List<ProxyRoute>>> serviceRoutesPerVHostNamePerListener = new HashMap<>();
    private Map<String, Pair<IfNetwork, Boolean>> networkPerListener = new HashMap<>();

    static final String RP_SERVICE_PREFIX = "rp_";
    static final String RP_SERVICE_POSTFIX = "_service";
    private static final String LISTENER_N32C = "internal_n32c_listener";
    private static final Boolean N32C_INIT_TLS_ENABLED = Boolean.parseBoolean(EnvVars.get("N32C_INIT_TLS_ENABLED", true));

    public RoutingContext(NfInstance seppInst,
                          Optional<Map<String, Triplet<String, String, Boolean>>> n32cStateData,
                          int ineligibleSansVersion)
    {
        this.seppInst = seppInst;
        this.n32cStateData = n32cStateData;
        this.ineligibleSansVersion = ineligibleSansVersion;

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
     */
    private void createRoutes()
    {
        Stream.concat(seppInst.getOwnNetwork().stream(), seppInst.getExternalNetwork().stream()).forEach(nw ->
        {

            var svcAddr = Utils.getByName(seppInst.getServiceAddress(), nw.getServiceAddressRef());
            if (svcAddr.getPort() != null)
            {
                var listenerName = CommonConfigUtils.getListenerName(svcAddr, false);
                networkPerListener.put(listenerName, Pair.of(nw, Boolean.valueOf(false)));
                serviceRoutesPerVHostNamePerListener.put(listenerName, new HashMap<>());
            }
            if (svcAddr.getTlsPort() != null)
            {
                var listenerName = CommonConfigUtils.getListenerName(svcAddr, true);
                networkPerListener.put(listenerName, Pair.of(nw, Boolean.valueOf(true)));
                serviceRoutesPerVHostNamePerListener.put(listenerName, new HashMap<>());
            }

        });

        // Add n32c listener
        if (seppInst.getN32C() != null && !seppInst.getN32C().getOwnSecurityData().isEmpty())
        {
            var listenerNameN32c = LISTENER_N32C + (N32C_INIT_TLS_ENABLED.booleanValue() ? "_tls" : "");
            serviceRoutesPerVHostNamePerListener.put(listenerNameN32c, new HashMap<>());
        }

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
     * of our network to the x-eric-proxy filter so that the SEPP service processing
     * can be done. This is a catch-all route at the beginning that has to be there
     * because Envoy doesn't send any request to the filter unless a route matches.
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
     * @return
     */
    private ProxyRoute getCatchAllRoute(String listener)
    {
        var catchAllRoute = new ProxyRoute("catch_all",
                                           new ProxyRouteMatch().addPresentValueHeader(Egress.HEADER_ERIC_PROXY, false).setPrefix("/"),
                                           null,
                                           null,
                                           "not_used",
                                           RouteTypes.ROUND_ROBIN,
                                           ProxyRoute.RoutePriorities.PRI_SEPP_CATCH_ALL,
                                           null,
                                           30.0);

        var networkInfo = networkPerListener.get(listener);
        if (networkInfo != null && networkInfo.t2 && networkInfo.t1 instanceof ExternalNetwork && n32cStateData.isPresent())// only puts route metadata for n32c
                                                                                                                            // on tls listener for external
        {

            Set<String> fqdns = new HashSet<>();
            var md = new HashMap<String, List<String>>();
            n32cStateData.get().entrySet().forEach(data ->
            {
                var nfInstanceRef = data.getKey();
                var operState = data.getValue().getSecond();
                if (!operState.equals(OperationalState.Value.ACTIVE.toString()))
                {
                    seppInst.getStaticNfInstanceData().forEach(instance ->
                    {
                        var inst = Utils.getByName(instance.getStaticNfInstance(), nfInstanceRef);

                        if (inst != null)
                            inst.getStaticNfService().forEach(service ->
                            {
                                log.debug("FQDN added to metadata: {}", service.getAddress().getFqdn());
                                fqdns.add(service.getAddress().getFqdn());
                            });
                    });

                    seppInst.getStaticSeppInstanceData().forEach(instance ->
                    {
                        var inst = Utils.getByName(instance.getStaticSeppInstance(), nfInstanceRef);

                        if (inst != null)
                        {
                            log.debug("FQDN added to metadata: {}", inst.getAddress().getFqdn());
                            fqdns.add(inst.getAddress().getFqdn());
                        }
                    });
                }
            });
            // only set metadata in the catch all route if there actually are ineligible
            // sans to be compared
            var ineligibleSans = fqdns.stream().map(fqdn -> new MetadataStringValue(fqdn)).collect(Collectors.toList());
            if (!ineligibleSans.isEmpty())
            {
                var mdMap = new ProxyMetadataMap();
                mdMap.addMetadata(MetaDataType.ERIC_PROXY, METADATA.INELIGIBLE_SANS, new MetadataListValue<MetadataStringValue>(ineligibleSans));
                mdMap.addMetadata(MetaDataType.ERIC_PROXY, METADATA.INELIGIBLE_SANS_VERSION, new MetadataDoubleValue(Double.valueOf(ineligibleSansVersion)));
                log.debug("metadata added to catchAllRoute: {}", mdMap);
                catchAllRoute.setRouteMetadata(mdMap);
            }

        }
        return catchAllRoute;
    }

    /**
     * Create and return the name for a VHost for a given roaming-partner
     * 
     * @param rpName
     * @return the name for the vhost
     */
    public static String getRpVHostName(String rpName)
    {
        return RP_SERVICE_PREFIX + rpName + RP_SERVICE_POSTFIX;
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
        var networkInfo = networkPerListener.get(listenerName);
        if (networkInfo != null)
        {
            return Optional.ofNullable(networkInfo.t1);
        }
        return Optional.empty();
    }

    // container class to keep
    private static final class Pair<T1, T2>
    {
        public final T1 t1;
        public final T2 t2;

        private Pair(T1 t1,
                     T2 t2)
        {
            this.t1 = t1;
            this.t2 = t2;
        }

        public static <T1, T2> Pair<T1, T2> of(T1 t1,
                                               T2 t2)
        {
            return new Pair<>(t1, t2);
        }
    }

}
