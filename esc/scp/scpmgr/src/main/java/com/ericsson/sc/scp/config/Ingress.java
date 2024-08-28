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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.configutil.CommonConfigUtils;
import com.ericsson.sc.glue.IfIngress;
import com.ericsson.sc.glue.IfIngressConnectionProfile;
import com.ericsson.sc.proxyal.proxyconfig.ProxyTls;
import com.ericsson.sc.proxyal.proxyconfig.ProxyVtapSettings;
import com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig.ProxyAccessLog;
import com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig.ProxyCdnLoop;
import com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig.ProxyDynamicForwardProxy;
import com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig.ProxyHttpRouter;
import com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig.ProxyLocalReplyConfig;
import com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig.ProxyRds;
import com.ericsson.sc.proxyal.proxyconfig.proxylistenerconfig.ProxyListener;
import com.ericsson.sc.scp.config.filters.EricProxyFilterFactory;
import com.ericsson.sc.scp.config.filters.EricProxyFilterFactory.EricProxyFilterType;
import com.ericsson.sc.scp.model.NfInstance;
import com.ericsson.sc.scp.model.OwnNetwork;
import com.ericsson.sc.scp.model.ServiceAddress;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.common.IP_VERSION;
import com.ericsson.utilities.common.RuntimeEnvironment;
import com.ericsson.utilities.common.Utils;
import com.ericsson.utilities.exceptions.BadConfigurationException;

import io.envoyproxy.envoy.config.cluster.v3.Cluster.DnsLookupFamily;
import io.kubernetes.client.openapi.models.V1Service;

/**
 * Responsible to create the proxyListener with all VHosts underneath (but no
 * routes).
 */
public class Ingress implements IfIngress
{

    /**
     * 
     */
    private static final int DEFAULT_MAX_STREAM_DURATION = 500 * 1000;

    private static final Logger log = LoggerFactory.getLogger(Ingress.class);

    private static final String PROTOCOL_VERSION = "2.0";

    private NfInstance scpInst;

    private final Set<ProxyListener> listeners = new HashSet<>();
    private final HashMap<ServiceAddress, OwnNetwork> serviceAdressToNetworks = new HashMap<>();
    private final Map<String, Integer> listenerTargetPorts = new HashMap<>();
    private final List<V1Service> k8sServiceList;

    /**
     * @param scpInst
     */
    public Ingress(NfInstance scpInst,
                   final List<V1Service> k8sServiceList)
    {
        this.scpInst = scpInst;
        this.k8sServiceList = k8sServiceList;
    }

    /**
     * From the configuration, generate all ProxyListeners.
     * 
     */
    @Override
    public void convertConfig()
    {
        for (ServiceAddress svcAddr : scpInst.getServiceAddress())
        {
            scpInst.getOwnNetwork().stream().forEach(nw ->
            {
                if (nw.getServiceAddressRef().equals(svcAddr.getName()))
                {
                    this.serviceAdressToNetworks.put(svcAddr, nw);
                }

            });
        }

        // create the map containing the resulting listeners' target ports
        this.createK8sServicePortMap();

        for (var entry : serviceAdressToNetworks.entrySet())
        {
            ServiceAddress svcAddress = entry.getKey();

            // It's a configuration error if the referenced ownVpn is not defined.
            if (svcAddress != null)
            {
                var vtapSettings = CommonConfigUtils.readVtapConfig(scpInst, entry.getValue());
                List<ProxyListener> listenersForServiceAddr = new ArrayList<>();
                // no-tls listener
                if (svcAddress.getPort() != null)
                {
                    listenersForServiceAddr.add(createIngressListener(svcAddress, Optional.empty(), vtapSettings));
                }
                // tls listener
                if (svcAddress.getTlsPort() != null)
                {
                    listenersForServiceAddr.add(createIngressListener(svcAddress, Optional.of(new ProxyTls()), vtapSettings));
                }

                listenersForServiceAddr.forEach(l ->
                {
                    var nw = entry.getValue();

                    // Get ingress-connection-profile-ref
                    String icpRef = nw.getIngressConnectionProfileRef() != null ? nw.getIngressConnectionProfileRef()
                                                                                : scpInst.getIngressConnectionProfileRef();
                    // Get ingress-connection-profile
                    IfIngressConnectionProfile icp = Utils.getByName(scpInst.getIngressConnectionProfile(), icpRef);

                    CommonConfigUtils.setMaxConnectionDuration(l, icp);
                    CommonConfigUtils.setHpackTableSize(l, icp);
                    CommonConfigUtils.setMaxConcurrentStreams(l, icp);
                    CommonConfigUtils.setIdleTimeout(l, icp);
                    CommonConfigUtils.setTcpKeepalive(l, icp);

                    createConnManagerConfigs(l, entry);
                    CommonConfigUtils.addIntVHostToListener(svcAddress, l);
                    CommonConfigUtils.addIntForwardingVHostToListener(l);
                    l.setDscpMarking(icp.getDscpMarking());
                });
                this.listeners.addAll(listenersForServiceAddr);

            }
            else
            {
                throw new BadConfigurationException("Cannot convert ingress-related configuration: The service-address '{}' referenced in the configuration  does not exist.",
                                                    entry.getKey());
            }
        }

    }

    private void createConnManagerConfigs(ProxyListener listener,
                                          Entry<ServiceAddress, OwnNetwork> entry)
    {
        var network = entry.getValue();
        listener.addHttpFilter(new ProxyRds());
        var rlProfile = Utils.getByName(this.scpInst.getLocalRateLimitProfile(), network.getLocalRateLimitProfileRef());
        if (rlProfile != null)
        {
            /*
             * Local rate limit emits statistics under statprefix.http_local_rate_limit.
             * Prefix is adapted to be able to distinguish betweeen: nf_instance (n8e)
             * service-address (s6a6) network(_tls) (n5k)
             */
            var statPrefix = String.format("http.lrl.n8e.%s.g3p.ingress.s6a6.%s.n5k.%s%s",
                                           scpInst.getName(),
                                           entry.getKey().getName(),
                                           network.getName(),
                                           listener.getTls().isPresent() ? "_tls" : "");

            listener.addHttpFilter(CommonConfigUtils.createProxyLocalRateLimitFilterFromNetwork(statPrefix, rlProfile));
        }
        listener.addHttpFilter(new ProxyHttpRouter());

        // The dynamic forward proxy filter is enabled
        listener.addHttpFilter(new ProxyDynamicForwardProxy());

        listener.addHttpFilter(new ProxyLocalReplyConfig());

        List<String> scpAddresses = listener.getScpServiceAddress();
        if (scpAddresses.size() == 1)
        {
            var cdnId = PROTOCOL_VERSION + " " + scpAddresses.get(0);
            listener.addHttpFilter(new ProxyCdnLoop(cdnId));
        }
        else if (scpAddresses.size() == 2)
        {
            var cdnId = PROTOCOL_VERSION + " " + scpAddresses.get(0) + "; " + PROTOCOL_VERSION + " " + scpAddresses.get(1);
            listener.addHttpFilter(new ProxyCdnLoop(cdnId));
        }
        else // is this applicable? probably not
        {
            var cdnId = PROTOCOL_VERSION;
            listener.addHttpFilter(new ProxyCdnLoop(cdnId));
        }

        if (listener.isAccessLogEnabled())
        {
            listener.addHttpFilter(new ProxyAccessLog());
        }

        var proxyFilterFactory = new EricProxyFilterFactory(this.scpInst, listener.getTls().isPresent(), network);
        proxyFilterFactory.getFilter(EricProxyFilterType.RATE_LIMIT).create().ifPresent(listener::addHttpFilter);
        proxyFilterFactory.getFilter(EricProxyFilterType.ROUTING_SCREENING).create().ifPresent(listener::addHttpFilter);
    }

    /**
     * Return all proxyListeners that were created.
     * 
     * @return proxyListeners
     */
    @Override
    public Collection<ProxyListener> getListeners()
    {
        return this.listeners;
    }

    /**
     * Create and return the single Listener that Envoy has.
     * 
     * @param svcAddr
     * @param vtapEnabled
     * @return
     */
    private ProxyListener createIngressListener(ServiceAddress svcAddr,
                                                Optional<ProxyTls> tls,
                                                Optional<ProxyVtapSettings> vtapSettings)
    {

        String proxyListenerName = CommonConfigUtils.getListenerName(svcAddr, tls.isPresent());
        Integer targetPort = this.listenerTargetPorts.get(proxyListenerName);
        if (targetPort == null)
        {
            throw new BadConfigurationException("One or more service-addresses are wrongly configured, please re-load configuration");
        }
        String envoyListenerName = CommonConfigUtils.getEnvoyListenerName(proxyListenerName, targetPort);

        var ipVersion = svcAddr.getIpv4Address() != null
                        && svcAddr.getIpv6Address() != null ? IP_VERSION.IPV4_IPV6 : svcAddr.getIpv4Address() != null ? IP_VERSION.IPV4 : IP_VERSION.IPV6;

        var listener = new ProxyListener(proxyListenerName, envoyListenerName, targetPort, ipVersion, scpInst.getName(), svcAddr.getName());
        listener.setTls(tls);
        listener.setVtapSettings(vtapSettings);

        // Set the type of the DNS lookup for the dyn. forward proxy filter. This also
        // puts the filter into the filter-chain of the listener.
        IP_VERSION internalIpVersion = RuntimeEnvironment.getDeployedIpVersion();
        listener.setDnsLookupFamily(internalIpVersion.equals(IP_VERSION.IPV4) ? DnsLookupFamily.V4_ONLY
                                                                              : internalIpVersion.equals(IP_VERSION.IPV6) ? DnsLookupFamily.V6_ONLY
                                                                                                                          : DnsLookupFamily.ALL);

        // Set the max stream duration equal to failover profile's max timeout budget
        // plus 1 second
        // In case failover profile is not configured, default value of 500s is used
        var maxRequestTimeBudget = CommonConfigUtils.findMaxTimeoutBudget(this.scpInst).map(num -> num + 1000).orElse(DEFAULT_MAX_STREAM_DURATION);
        listener.setMaxStreamDuration((long) maxRequestTimeBudget);

        // set scp service address, used in cdn_loop protection
        // either ipv4 or ipv6 is mandatory
        List<String> scpServiceAddress = new ArrayList<>();
        if (svcAddr.getFqdn() != null)
        {
            scpServiceAddress.add(svcAddr.getFqdn());
        }
        else
        {
            if (svcAddr.getIpv4Address() != null)
                scpServiceAddress.add(svcAddr.getIpv4Address());
            if (svcAddr.getIpv6Address() != null)
                scpServiceAddress.add("[" + svcAddr.getIpv6Address() + "]");
        }

        listener.setScpServiceAddress(scpServiceAddress);

        log.info("Created ProxyListener: {}", listener.getScpServiceAddrName());

        return listener;
    }

    /**
     * Attempts to match configured service addresses to their corresponding
     * kubernetes eric-scp-worker services The combination of IP:port in a
     * service-address be either <k8sService>.<externalIp>:<k8sService>.<Port> or
     * <k8sWorkerIP>:<k8sService>.<NodePort>
     * 
     * @return A Map with listener names and their corresponding target ports
     */
    private void createK8sServicePortMap()
    {

        this.scpInst.getServiceAddress().forEach(svcAddr ->
        {
            var k8sServiceMatch = this.k8sServiceList.stream().filter(Objects::nonNull).filter(k8sSvc ->
            {
                var unencryptedPort = k8sSvc.getSpec().getPorts().stream().filter(port -> port.getName().equals("unencrypted-port")).findAny();
                var encryptedPort = k8sSvc.getSpec().getPorts().stream().filter(port -> port.getName().equals("encrypted-port")).findAny();
                var lbIngress = k8sSvc.getStatus().getLoadBalancer().getIngress();
                // External ip checks.
                // Accepts only the combination <k8sService>.<externalIp>:<k8sService>.<Port>
                if (lbIngress != null && !lbIngress.isEmpty())
                {
                    try
                    {
                        InetAddress svcIpv4 = null;
                        InetAddress svcIpv6 = null;
                        if (svcAddr.getIpv4Address() != null && !svcAddr.getIpv4Address().isEmpty())
                        {
                            svcIpv4 = InetAddress.getByName(svcAddr.getIpv4Address());
                        }
                        if (svcAddr.getIpv6Address() != null && !svcAddr.getIpv6Address().isEmpty())
                        {
                            svcIpv6 = InetAddress.getByName(svcAddr.getIpv6Address());
                        }

                        List<InetAddress> externalIPList = new ArrayList<>();
                        for (var ingress : lbIngress)
                        {
                            InetAddress address = InetAddress.getByName(ingress.getIp());
                            externalIPList.add(address);
                        }

                        // check if ip matched
                        boolean isIpMatch = isIpMatched(svcIpv4, svcIpv6, externalIPList);
                        if (isIpMatch)
                        {
                            // Check if the ports associated with the svcAddr match the desired ports for
                            // IPv4 or IPv6

                            if (svcAddr.getPort() != null && svcAddr.getTlsPort() != null)
                            {
                                if (unencryptedPort.isPresent() && Objects.equals(svcAddr.getPort(), unencryptedPort.get().getPort())
                                    && encryptedPort.isPresent() && Objects.equals(svcAddr.getTlsPort(), encryptedPort.get().getPort()))
                                {
                                    return true;
                                }
                            }
                            else if (svcAddr.getPort() != null)
                            {
                                if (unencryptedPort.isPresent() && Objects.equals(svcAddr.getPort(), unencryptedPort.get().getPort()))
                                {
                                    return true;
                                }
                            }
                            // only tls defined
                            else
                            {
                                if (encryptedPort.isPresent() && Objects.equals(svcAddr.getTlsPort(), encryptedPort.get().getPort()))
                                {
                                    return true;
                                }
                            }
                            return false;

                        }
                    }
                    catch (UnknownHostException e)
                    {
                        // this is caught in the validator
                        throw new BadConfigurationException("A configured IP address in a service is not valid: {}", e.getMessage());
                    }
                }

                // Internal ip checks.
                // There is a limitation to check every internal ip because
                // cluster-ip and node-ip can be also defined. So, the check is only on the
                // internal ports.
                if (svcAddr.getPort() != null && svcAddr.getTlsPort() != null)
                {
                    if (unencryptedPort.isPresent() && Objects.equals(unencryptedPort.get().getNodePort(), svcAddr.getPort()) && encryptedPort.isPresent()
                        && Objects.equals(encryptedPort.get().getNodePort(), svcAddr.getTlsPort()))
                    {
                        return true;
                    }
                }
                else if (svcAddr.getPort() != null)
                {
                    if (unencryptedPort.isPresent() && Objects.equals(unencryptedPort.get().getNodePort(), svcAddr.getPort()))
                    {
                        return true;
                    }
                }
                // only tls defined
                else
                {
                    if (encryptedPort.isPresent() && Objects.equals(encryptedPort.get().getNodePort(), svcAddr.getTlsPort()))
                    {
                        return true;
                    }
                }

                return false;

            }).findAny();

            if (k8sServiceMatch.isPresent())
            {
                if (svcAddr.getPort() != null)
                {
                    k8sServiceMatch.get()
                                   .getSpec()
                                   .getPorts()
                                   .stream()
                                   .filter(port -> (port.getName().equals("unencrypted-port") && port.getTargetPort() != null))
                                   .findAny()
                                   .ifPresent(port -> this.listenerTargetPorts.put(CommonConfigUtils.getListenerName(svcAddr, false),
                                                                                   CommonConfigUtils.getListenerTargetPortFromEnvVar(port.getTargetPort()
                                                                                                                                         .getStrValue())));
                }

                if (svcAddr.getTlsPort() != null)
                {
                    k8sServiceMatch.get()
                                   .getSpec()
                                   .getPorts()
                                   .stream()
                                   .filter(port -> (port.getName().equals("encrypted-port") && port.getTargetPort() != null))
                                   .findAny()
                                   .ifPresent(port -> this.listenerTargetPorts.put(CommonConfigUtils.getListenerName(svcAddr, true),
                                                                                   CommonConfigUtils.getListenerTargetPortFromEnvVar(port.getTargetPort()
                                                                                                                                         .getStrValue())));
                }

            }

        });

    }

    private boolean isIpMatched(InetAddress svcIpv4,
                                InetAddress svcIpv6,
                                List<InetAddress> externalIPList) throws UnknownHostException
    {

        boolean ipv4Match = svcIpv4 != null && externalIPList.contains(svcIpv4);
        boolean ipv6Match = svcIpv6 != null && externalIPList.contains(svcIpv6);
        // dualstack
        if (svcIpv4 != null && svcIpv6 != null)
        {
            return ipv4Match && ipv6Match;
        }
        // singlestack
        return ipv4Match || ipv6Match;

    }

}
