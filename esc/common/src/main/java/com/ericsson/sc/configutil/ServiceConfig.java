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
 * Created on: Aug 24, 2020
 *     Author: eedrak
 */

package com.ericsson.sc.configutil;

import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.app.FieldMethodizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.glue.ConfigConverter;
import com.ericsson.sc.glue.IfEgress;
import com.ericsson.sc.glue.IfIngress;
import com.ericsson.sc.glue.IfNfInstance;
import com.ericsson.sc.glue.IfRoutingContext;
import com.ericsson.sc.proxyal.proxyconfig.ProxyCfg;
import com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig.ProxyCluster;
import com.ericsson.sc.proxyal.proxyconfig.proxylistenerconfig.ProxyListener;
import com.ericsson.sc.sepp.model.OwnNetwork;

/**
 * Convert CM-configuration into proxy-configuration for a specific service.
 * This class is the base class for all services/handlers.
 */
public class ServiceConfig implements ConfigConverter
{
    public static final String INT_SERVICE = "int_service";
    public static final String INT_FORWARD_SERVICE = "int_fwd_service";
    public static final String INT_N32C_SERVICE = "int_n32c_service";
    static final String RP_SERVICE_PREFIX = "rp_";
    static final String RP_SERVICE_POSTFIX = "_service";
    static final String N32C_MATCH_PREFIX = "/n32c-handshake/v1/exchange-capability";

    private final IfIngress ingress;
    private final IfRoutingContext routingCtx;
    private final IfEgress egress;
    private final IfNfInstance nfInst;
    private final String instanceName;

    private static final Logger log = LoggerFactory.getLogger(ServiceConfig.class);

    /**
     * 
     * @param ingress
     * @param routingCtx
     * @param egress
     */
    public ServiceConfig(IfIngress ingress,
                         IfRoutingContext routingCtx,
                         IfEgress egress,
                         IfNfInstance nfInstance,
                         String instanceName)
    {
        this.ingress = ingress;
        this.routingCtx = routingCtx;
        this.egress = egress;
        this.nfInst = nfInstance;
        this.instanceName = instanceName.toLowerCase();
    }

    /**
     * Add all proxyListeners to the proxy-configuration
     * 
     * @param pxCfg is updated with the configuration (output parameter)
     */
    public void addListeners(final ProxyCfg pxCfg)
    {
        log.debug("Mapping Ericsson-{} to Listener(s)", this.instanceName);

        for (ProxyListener pxListener : ingress.getListeners())
        {
            // The routingContext is data for the LUA filter
            // pxListener.setRoutingContext(getLuaRoutingContext());
            pxCfg.addListener(pxListener);
        }

    }

    /**
     * The LUA Routing Context holds all data required by the LUA filter. It is
     * passed into the Velocity template for the LUA filter.
     * 
     * @return the lua-routing-context
     */
    private Map<String, Object> getLuaRoutingContext()
    {
        HashMap<String, Object> routingContext = new HashMap<>();
        routingContext.put("egress", this.egress);
        // The field-methodizer exports all public-static constants from the given
        // class:

        routingContext.put("egressConst", new FieldMethodizer(String.format("com.ericsson.sc.%s.config.Egress", this.instanceName)));
        routingContext.put("ingress", this.ingress);
        routingContext.put("routingctx", this.routingCtx);
        routingContext.put(String.format("%scfg", this.instanceName), this.nfInst);
        return routingContext;

    }

    /**
     * 
     * Add ProxyClusters for all loaded ConfigPlugins to the proxy-configuration
     * 
     * @param pxCfg Clusters are added to it (output parameter)
     */
    public void addClusters(ProxyCfg pxCfg)
    {
        for (ProxyCluster pxCluster : egress.getClusters())
        {
            pxCfg.addCluster(pxCluster);
        }
    }

    /**
     * Add ProxyRoutes (from egress and routingCtx) to the VHosts in the
     * ProxyListeners.
     * 
     * @param pxCfg Routes are added to it (output parameter)
     */
    public void addListenerRoutes(ProxyCfg pxCfg)
    {
        for (ProxyListener listener : pxCfg.getListeners())
        {
            for (var vHost : listener.getVirtualHosts())
            {
                var vHostName = vHost.getvHostName();
                log.debug("adding routes for vHost:{}.", vHostName);
                // we need add egress routes in order to attach falioverBehaviour to the
                // pool/cluster
                if (egress.getRoutesForVHostName(vHostName) != null)
                {
                    for (var route : egress.getRoutesForVHostName(vHostName))
                    {
                        // Route matching n32c handshakes should only be added on vHost belonging to TLS
                        // listeners of an external network
                        if (route.getMatch().getPrefix().isPresent())
                        {
                            var prefix = route.getMatch().getPrefix().get();
                            // if it's the n32c route and the listener is not tls or it comes from an
                            // internal network, skip the route
                            if (prefix.equals(N32C_MATCH_PREFIX)
                                && (listener.getTls().isEmpty() || routingCtx.getNetworkForListener(listener.getName()).get() instanceof OwnNetwork))
                            {
                                log.debug("ingoring n32c route for vHost:{}, for nonTLS listener:{}.", vHostName, listener.getName());
                                continue;
                            }
                        }
                        vHost.addRoute(route);
                        log.debug("added egress route:{}, for vHost:{}.", route, vHostName);
                    }
                }
                if (routingCtx.getServiceRoutesForListener(listener.getName(), vHostName) != null)
                {
                    for (var route : routingCtx.getServiceRoutesForListener(listener.getName(), vHostName))
                    {
                        vHost.addRoute(route);
                        log.debug("added service routes for vHost:{}.", vHostName);
                    }
                }
            }
        }
    }

    /**
     * 
     */
    @Override
    public void convertConfig()
    {
        ingress.convertConfig();
        egress.convertConfig();
        routingCtx.convertConfig();
    }

}
