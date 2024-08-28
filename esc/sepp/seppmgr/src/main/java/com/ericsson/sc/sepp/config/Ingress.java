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

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.cm.CmmPatch;
import com.ericsson.adpal.cm.PatchItem;
import com.ericsson.adpal.cm.PatchOperation;
import com.ericsson.sc.configutil.CommonConfigUtils;
import com.ericsson.sc.glue.IfIngress;
import com.ericsson.sc.glue.IfIngressConnectionProfile;
import com.ericsson.sc.glue.IfNetwork;
import com.ericsson.sc.proxyal.proxyconfig.ProxyTls;
import com.ericsson.sc.proxyal.proxyconfig.ProxyVtapSettings;
import com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig.ProxyAccessLog;
import com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig.ProxyCdnLoop;
import com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig.ProxyDynamicForwardProxy;
import com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig.ProxyHeaderToMetadata;
import com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig.ProxyHttpRouter;
import com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig.ProxyLocalReplyConfig;
import com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig.ProxyRds;
import com.ericsson.sc.proxyal.proxyconfig.proxylistenerconfig.ProxyListener;
import com.ericsson.sc.sepp.config.filters.EricProxyFilterFactory;
import com.ericsson.sc.sepp.config.filters.EricProxyFilterFactory.EricProxyFilterType;
import com.ericsson.sc.sepp.model.NfInstance;
import com.ericsson.sc.sepp.model.ServiceAddress;
import com.ericsson.sc.sepp.model.TopologyHiding;
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
    private static final int DEFAULT_MAX_STREAM_DURATION = 500 * 1000; // 500 s

    private static final Logger log = LoggerFactory.getLogger(Ingress.class);

    private static final String PROTOCOL_VERSION = "2.0";
    private NfInstance seppInst;
    private final HashMap<ServiceAddress, IfNetwork> serviceAdressToNetworks = new HashMap<>();
    private final Set<ProxyListener> listeners = new HashSet<>();
    private final Map<String, Integer> listenerTargetPorts = new HashMap<>();
    private final CmmPatch cmPatch;
    private final List<V1Service> k8sServiceList;
    private static final String LISTENER_N32C = "internal_n32c_listener";
    private static final int PORT_REST_N32C = 8043;
    private static boolean enableAccessLogging = false;

    // initializing sepp scenario tls enable indicator
    // This only concerns the internal part of the communication (manager-> worker)
    // disabled if global tls is disabled
    // optionally may be disabled if global tls is enabled
    private static final Boolean N32C_INIT_TLS_ENABLED = Boolean.parseBoolean(EnvVars.get("N32C_INIT_TLS_ENABLED", true));

    /**
     * @param seppInst
     * @throws IOException
     */
    public Ingress(NfInstance seppInst,
                   final CmmPatch cmPatch,
                   final List<V1Service> k8sServiceList)
    {
        this.seppInst = seppInst;
        this.cmPatch = cmPatch;
        this.k8sServiceList = k8sServiceList;
    }

    /**
     * From the configuration, generate all ProxyListeners.
     * 
     * @throws BadConfigurationException when the own-vpn-ref refers to an undefined
     *                                   vpn MO
     */
    @Override
    public void convertConfig()
    {

        if (seppInst.getTopologyHiding() != null)
        {
            seppInst.getTopologyHiding().stream().filter(tph -> tph.getPseudoSearchResult() != null).forEachOrdered(this::createTopoHidingNfInstanceId);
        }

        // Store all services addresses in a map so we can find an address by its name
        // when it is referenced from somewhere.
        for (ServiceAddress svcAddr : seppInst.getServiceAddress())
        {
            Stream.concat(seppInst.getOwnNetwork().stream(), seppInst.getExternalNetwork().stream()).forEach(nw ->
            {
                if (nw.getServiceAddressRef().equals(svcAddr.getName()))
                {
                    this.serviceAdressToNetworks.put(svcAddr, nw);
                }

            });
        }

        // create a mapping of service-address names to networks (own or external) that
        // reference said addresses

        // create the map containing the resulting listeners' target ports
        this.createK8sServicePortMap();
        // Check if access logging should be enabled
        if (seppInst.getExternalNetwork() != null && !seppInst.getExternalNetwork().isEmpty())
        {
            enableAccessLogging = CommonConfigUtils.enableAccessLogging(seppInst.getExternalNetwork().get(0), seppInst);
        }
        // It's a configuration error if the referenced ownVpn is not defined.
        for (var entry : serviceAdressToNetworks.entrySet())
        {
            ServiceAddress svcAddress = entry.getKey();

            if (svcAddress != null)
            {
                var vtapSettings = CommonConfigUtils.readVtapConfig(seppInst, entry.getValue());
                List<ProxyListener> listenersForServiceAddr = new ArrayList<>();
                // no-tls listener
                if (svcAddress.getPort() != null)
                {
                    listenersForServiceAddr.add(createIngressListener(svcAddress, Optional.empty(), vtapSettings));
                }
                // tls listener
                if (svcAddress.getTlsPort() != null)
                {
                    var proxyTls = ConfigUtils.createTlsForListener(svcAddress, this.seppInst, entry.getValue().getName());
                    listenersForServiceAddr.add(createIngressListener(svcAddress, proxyTls, vtapSettings));
                }

                listenersForServiceAddr.forEach(l ->
                {
                    var nw = entry.getValue();

                    // Get ingress-connection-profile-ref
                    String icpRef = nw.getIngressConnectionProfileRef() != null ? nw.getIngressConnectionProfileRef()
                                                                                : seppInst.getIngressConnectionProfileRef();
                    // Get ingress-connection-profile
                    IfIngressConnectionProfile icp = Utils.getByName(seppInst.getIngressConnectionProfile(), icpRef);

                    CommonConfigUtils.setMaxConnectionDuration(l, icp);
                    CommonConfigUtils.setHpackTableSize(l, icp);
                    CommonConfigUtils.setMaxConcurrentStreams(l, icp);
                    CommonConfigUtils.setIdleTimeout(l, icp);
                    CommonConfigUtils.setTcpKeepalive(l, icp);

                    l.setAccessLogEnabled(enableAccessLogging);

                    createConnManagerConfigs(l, entry.getValue(), svcAddress);
                    CommonConfigUtils.addIntVHostToListener(svcAddress, l);
                    CommonConfigUtils.addIntForwardingVHostToListener(l);
                    l.setDscpMarking(icp.getDscpMarking());
                });
                this.listeners.addAll(listenersForServiceAddr);
            }
            else
            {
                throw new BadConfigurationException("Cannot convert ingress-related configuration: The service-address '{}' referenced in the configuration does not exist.",
                                                    entry.getKey());
            }
        }

        // Add the n32c listener which receives requests from the sepp-manager during
        // the initiation of n32c handshake
        if (ConfigUtils.isN32cConfiguredNfInstance(seppInst))
        {
            ProxyListener n32c = createN32cListenerInternal();
            createConnManagerConfigs(n32c);
            this.listeners.add(n32c);
        }
    }

    private void createConnManagerConfigs(ProxyListener listener,
                                          IfNetwork network,
                                          ServiceAddress svcAddress)
    {
        listener.addHttpFilter(new ProxyRds());
        var rlProfile = Utils.getByName(this.seppInst.getLocalRateLimitProfile(), network.getLocalRateLimitProfileRef());
        if (rlProfile != null)
        {
            /*
             * Local rate limit emits statistics under statprefix.http_local_rate_limit.
             * Prefix is adapted to be able to distinguish betweeen: nf_instance (n8e)
             * service-address (s6a6) network(_tls) (n5k)
             */
            var statPrefix = String.format("http.lrl.n8e.%s.g3p.ingress.s6a6.%s.n5k.%s%s",
                                           seppInst.getName(),
                                           svcAddress.getName(),
                                           network.getName(),
                                           listener.getTls().isPresent() ? "_tls" : "");

            listener.addHttpFilter(CommonConfigUtils.createProxyLocalRateLimitFilterFromNetwork(statPrefix, rlProfile));
        }
        listener.addHttpFilter(new ProxyHttpRouter());

        // Activating the dynamic forward proxy filter.
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

        var proxyFilterFactory = new EricProxyFilterFactory(this.seppInst, svcAddress, listener.getTls().isPresent(), network);
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
     * @param ingress
     * @return
     */
    private ProxyListener createIngressListener(ServiceAddress svcAddr,
                                                Optional<ProxyTls> proxyTls,
                                                Optional<ProxyVtapSettings> vtapSettings)
    {
        String proxyListenerName = CommonConfigUtils.getListenerName(svcAddr, proxyTls.isPresent());
        Integer targetPort = this.listenerTargetPorts.get(proxyListenerName);
        if (targetPort == null)
        {
            throw new BadConfigurationException("One or more service-addresses are wrongly configured, please re-load configuration");
        }
        String envoyListenerName = CommonConfigUtils.getEnvoyListenerName(proxyListenerName, targetPort);

        var ipVersion = svcAddr.getIpv4Address() != null
                        && svcAddr.getIpv6Address() != null ? IP_VERSION.IPV4_IPV6 : svcAddr.getIpv4Address() != null ? IP_VERSION.IPV4 : IP_VERSION.IPV6;

        final var listener = new ProxyListener(proxyListenerName, envoyListenerName, targetPort, ipVersion, seppInst.getName(), svcAddr.getName());

        // Set the max stream duration equal to failover profile's max timeout budget
        // plus 1 second
        // In case failover profile is not configured, default value of 500s is used
        var maxRequestTimeBudget = CommonConfigUtils.findMaxTimeoutBudget(this.seppInst).map(num -> num + 1000).orElse(DEFAULT_MAX_STREAM_DURATION);
        listener.setMaxStreamDuration(maxRequestTimeBudget);
        listener.setTls(proxyTls);
        listener.setVtapSettings(vtapSettings);

        // Set the type of the DNS lookup for the dyn. forward proxy filter. This also
        // puts the filter into the filter-chain of the listener.
        IP_VERSION internalIpVersion = RuntimeEnvironment.getDeployedIpVersion();
        listener.setDnsLookupFamily(internalIpVersion.equals(IP_VERSION.IPV4) ? DnsLookupFamily.V4_ONLY
                                                                              : internalIpVersion.equals(IP_VERSION.IPV6) ? DnsLookupFamily.V6_ONLY
                                                                                                                          : DnsLookupFamily.ALL);

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
     * kubernetes eric-sepp-worker services The combination of IP:port in a
     * service-address be either <k8sService>.<externalIp>:<k8sService>.<Port> or
     * <k8sWorkerIP>:<k8sService>.<NodePort>
     * 
     * @return A Map with listener names and their corresponding target ports
     */
    private void createK8sServicePortMap()
    {
        this.seppInst.getServiceAddress().forEach(svcAddr ->
        {
            var k8sServiceMatch = k8sServiceList.stream().filter(Objects::nonNull).filter(k8sSvc ->
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

    private void createTopoHidingNfInstanceId(TopologyHiding tph)
    {
        try
        {
            if (tph.getPseudoSearchResult() != null && tph.getPseudoSearchResult().getNfProfile() != null
                && tph.getPseudoSearchResult().getNfProfile().getNfInstanceId() == null)
            {
                String uriPath = "/ericsson-sepp:sepp-function/nf-instance/0/topology-hiding/" + seppInst.getTopologyHiding().indexOf(tph)
                                 + "/pseudo-search-result/nf-profile/nf-instance-id";

                var patch = new PatchItem(PatchOperation.ADD, uriPath, "", UUID.randomUUID());

                this.cmPatch.patch("/cm/api/v1/configurations/ericsson-sepp", List.of(patch))
                            .doOnComplete(() -> log.info("Updated topology hiding nfInstanceId in configuration ericsson-sepp."))
                            .doOnError(e -> log.warn("Could not update topology hiding nfInstanceId in configuration ericsson-sepp. Cause: {}", e.toString()))
                            .onErrorComplete()
                            .subscribe();
            }
        }
        catch (final Exception t)
        {
            log.warn("Ignoring new configuration. Cause: {}", com.ericsson.utilities.exceptions.Utils.toString(t, log.isDebugEnabled()));
        }
    }

    /**
     * Create and return the single internal Listener that Envoy has for N32c i/f
     * 
     * @param ingress
     * @return
     */
    private ProxyListener createN32cListenerInternal()
    {
        log.info("create n32c listener");

        // tls listener
        String proxyListenerName = LISTENER_N32C + (N32C_INIT_TLS_ENABLED.booleanValue() ? "_tls" : "");
        Integer targetPort = PORT_REST_N32C;

        String envoyListenerName = CommonConfigUtils.getEnvoyListenerName(proxyListenerName, targetPort);

        final var listener = new ProxyListener(proxyListenerName,
                                               envoyListenerName,
                                               targetPort,
                                               RuntimeEnvironment.getDeployedIpVersion(),
                                               seppInst.getName(),
                                               LISTENER_N32C);

        // Set the max stream duration equal to default value of 500s.
        //
        listener.setMaxStreamDuration(DEFAULT_MAX_STREAM_DURATION);
        listener.setMaxConcurrentStreams(Integer.MAX_VALUE);

        if (N32C_INIT_TLS_ENABLED.booleanValue())
        {
            listener.setTls(Optional.of(new ProxyTls("internal_n32c_client_ca", "n32c_server_cert")));
        }

        CommonConfigUtils.addIntN32cVHostToListener(listener);

        // service-addresses and networks do not apply on this internal listener, as
        // it's used only by n32c requests
        var proxyFilterFactory = new EricProxyFilterFactory(this.seppInst, null, listener.getTls().isPresent(), null);
        proxyFilterFactory.getFilter(EricProxyFilterType.N32C_EGRESS_SCREENING).create().ifPresent(listener::addHttpFilter);

        log.info("Created ProxyListener: {}", listener.getScpServiceAddrName());

        return listener;
    }

    private void createConnManagerConfigs(ProxyListener listener)
    {
        listener.addHttpFilter(new ProxyRds());
        listener.addHttpFilter(new ProxyHeaderToMetadata()); // The Load Balancer match "x-host" header to find the correct endpoint.
        listener.addHttpFilter(new ProxyHttpRouter());

        if (listener.isAccessLogEnabled())
        {
            listener.addHttpFilter(new ProxyAccessLog());
        }
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