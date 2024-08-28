/**
 * COPYRIGHT ERICSSON GMBH 2020
 * <p>
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 * <p>
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 * <p>
 * Created on: Aug 8, 2022
 * Author: enocakh
 */
package com.ericsson.sc.proxy.endpoints;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.configutil.CommonConfigUtils;
import com.ericsson.sc.glue.IfAddress;
import com.ericsson.sc.glue.IfNfInstance;
import com.ericsson.sc.glue.IfNfPool;
import com.ericsson.sc.glue.IfStaticNfInstanceDatum;
import com.ericsson.sc.glue.IfTypedNfAddressProperties;
import com.ericsson.sc.glue.IfTypedNfInstance;
import com.ericsson.sc.glue.IfTypedNfService;
import com.ericsson.sc.nfm.model.Scheme;
import com.ericsson.sc.proxy.ConfigHelper;
import com.ericsson.sc.proxy.ProxyConstants.METADATA;
import com.ericsson.sc.proxyal.proxyconfig.proxyendpointconfig.ProxyEndpoint;
import com.ericsson.sc.sepp.model.RequiredForNfType;
import com.ericsson.utilities.common.Utils;
import com.ericsson.sc.proxyal.proxyconfig.MetadataStringValue;
import com.ericsson.sc.proxyal.proxyconfig.MetadataBooleanValue;
import com.ericsson.sc.proxyal.proxyconfig.MetadataListValue;
import com.ericsson.sc.proxyal.proxyconfig.MetadataMapValue;
import com.ericsson.sc.proxyal.proxyconfig.ProxyMetadataBuilder;
import com.ericsson.sc.proxyal.proxyconfig.ProxyMetadataBuilder.MetaDataType;
import com.ericsson.sc.proxyal.proxyconfig.ProxyMetadataMap;

public abstract class EndpointCollector
{
    private static final Logger log = LoggerFactory.getLogger(EndpointCollector.class);

    protected List<ProxyEndpoint> endpoints = new ArrayList<>();

    protected IfNfInstance configInst;
    protected IfNfPool pool;
    protected List<IfNfPool> pools;
    public static final String VTAP_ENABLED = "vtap_enabled";
    private ProxyMetadataMap clusterMetadataMap = new ProxyMetadataMap();

    protected EndpointCollector(IfNfInstance configInst,
                                IfNfPool pool)
    {
        this.configInst = configInst;
        this.pool = pool;
    }

    /**
     * After execution all the ProxyEndpoints from the target pool are added to the
     * list {@link #getEndpoints()}
     */
    public abstract void createEndpoints();

    /**
     * @return A list with all the endpoints from the target nf-pool.
     */
    public List<ProxyEndpoint> getEndpoints()
    {
        return endpoints;
    }

    public ProxyMetadataMap getClusterMetadata()
    {
        return clusterMetadataMap;
    }

    protected List<ProxyEndpoint> createEndpointsForServices(Set<Entry<Integer, List<IfTypedNfAddressProperties>>> nfServicesSet,
                                                             String poolName,
                                                             boolean keepOriginalAuthorityHeader)
    {
        List<ProxyEndpoint> pxEndpoints = new ArrayList<>();

        for (var entry : nfServicesSet)
        {
            pxEndpoints.addAll(createEndpointsFromServiceStream(entry.getValue().stream(), entry.getKey(), poolName, keepOriginalAuthorityHeader));
        }

        return pxEndpoints;
    }

    /**
     * Create and return a List of ProxyEndpoints from a stream of nfServices.
     *
     * @param nfServStream the stream of nfServices (ServiceHelper Objects)
     * @param prio         the priority to be used for the endpoints
     * @param poolName     the name of the pool these services were taken from, to
     *                     be used in endpoint metadata
     * @return a List of proxy endpoints
     */
    protected List<ProxyEndpoint> createEndpointsFromServiceStream(Stream<? extends IfTypedNfAddressProperties> nfServStream,
                                                                   Integer prio,
                                                                   String poolName,
                                                                   boolean preferredHostInAuthorityHeader)
    {

        List<ProxyEndpoint> pxEndpoints = new ArrayList<>();

        nfServStream.forEach(nfServ ->
        {
            List<ProxyEndpoint> pxEndpointsPerService = new ArrayList<>();

            // address is mandatory since fqdn inside it is
            var address = nfServ.getAddress();
            if (address.getMultipleIpEndpoint() == null || address.getMultipleIpEndpoint().isEmpty())
            {
                if (address.getFqdn() == null)
                {
                    log.warn("nf-service {} that has neither an fqdn nor an IP address defined is ignored for envoy configuration.", nfServ.getName());
                }
                else
                {
                    pxEndpointsPerService.add(createProxyEndpointFromFqdn(address, Optional.empty(), prio, poolName, nfServ, preferredHostInAuthorityHeader));
                }
            }
            else
            {

                address.getMultipleIpEndpoint().forEach(multIpEp ->
                {
                    List<String> ips = Stream.concat(multIpEp.getIpv4Address().stream(), multIpEp.getIpv6Address().stream()).collect(Collectors.toList());
                    if (ips.isEmpty())
                    {
                        if (address.getFqdn() == null)
                        {
                            log.warn("nf-service {} that has neither an fqdn nor an IP address defined is ignored for envoy configuration.", nfServ.getName());
                        }
                        else
                        {
                            pxEndpointsPerService.add(createProxyEndpointFromFqdn(address,
                                                                                  Optional.ofNullable(multIpEp.getPort()),
                                                                                  prio,
                                                                                  poolName,
                                                                                  nfServ,
                                                                                  preferredHostInAuthorityHeader));
                        }
                    }
                    else
                    {
                        ips.forEach(ipAddr -> pxEndpointsPerService.add(createProxyEndpointFromIp(ipAddr,
                                                                                                  address,
                                                                                                  Optional.ofNullable(multIpEp.getPort()),
                                                                                                  prio,
                                                                                                  poolName,
                                                                                                  nfServ,
                                                                                                  preferredHostInAuthorityHeader)));
                    }
                });
            }
            var capacity = nfServ.getCapacity() != null ? nfServ.getCapacity() : 100;
            pxEndpointsPerService.forEach(ep -> ep.setLoadBalancingWeight((int) capacity / pxEndpointsPerService.size()));
            pxEndpoints.addAll(pxEndpointsPerService);
        });

        return pxEndpoints;
    }

    /**
     * Create a ProxyEndpoint based on fqdn. The "host" metadatum contains only the
     * fqdn:port
     *
     * @param address  the address of the service an endpoint is made for,
     *                 containing the fqdn and scheme of the service
     * @param ipEpPort the port used for the endpoint
     * @param priority the priority to be used for the endpoint
     * @param poolName the name of the pool the service was taken from, to be used
     *                 in endpoint metadata
     * @return a ProxyEndpoint
     */
    protected ProxyEndpoint createProxyEndpointFromFqdn(IfAddress address,
                                                        Optional<Integer> ipEpPort,
                                                        Integer priority,
                                                        String poolName,
                                                        IfTypedNfAddressProperties nfServ,

                                                        boolean preferredHostInAuthorityHeader)
    {
        int port = ipEpPort.orElse(address.getScheme() == Scheme.HTTPS ? 443 : 80);
        var pxEp = new ProxyEndpoint(address.getFqdn(),
                                     port,
                                     priority,
                                     Optional.ofNullable(address.getFqdn()),//
                                     address.getScheme() == Scheme.HTTPS); // envoy

        var metadataMap = new ProxyMetadataMap();

        List<String> hostMetadata = new ArrayList<>();
        var fqdnPort = address.getFqdn().toLowerCase() + ":" + port;
        hostMetadata.add(fqdnPort);

        var mdHostValue = hostMetadata.stream().map(MetadataStringValue::new).toList();
        metadataMap.addMetadata(MetaDataType.LB, METADATA.HOST, new MetadataListValue<>(mdHostValue));

        var mdPoolValue = List.of(poolName).stream().map(MetadataStringValue::new).toList();
        metadataMap.addMetadata(MetaDataType.LB, METADATA.POOL, new MetadataListValue<>(mdPoolValue));

        boolean matchTLS = address.getScheme() == Scheme.HTTPS;

        pxEp.setMatchTLS(matchTLS);

        var mdTlsValue = new MetadataStringValue(Boolean.toString(matchTLS));
        metadataMap.addMetadata(MetaDataType.TRANSPORT_SOCKET, METADATA.MATCH_TLS, new MetadataListValue<>(List.of(mdTlsValue)));

        var ericProxyMetaData = getSupportMetadata(nfServ, preferredHostInAuthorityHeader);

        ericProxyMetaData.put(METADATA.NF_INSTANCE_ID, List.of(nfServ.getNfInstanceId() != null ? nfServ.getNfInstanceId() : ""));
        if (nfServ.getNfType() != null)
        {
            getNfTypeMetadata(ericProxyMetaData, nfServ.getNfType());
        }

        if (address.getInterPlmnFqdn() != null)
        {
            var interPlmnfqdnAndPort = address.getInterPlmnFqdn().toLowerCase() + ":" + port;

            var clusterMdMap = getClusterMetadata().getMetadataMap().get(MetaDataType.CLUSTER);

            @SuppressWarnings("unchecked")
            var mdSet = clusterMdMap != null
                        && clusterMdMap.containsKey(interPlmnfqdnAndPort) ? (MetadataListValue<MetadataMapValue>) clusterMdMap.get(interPlmnfqdnAndPort)
                                                                          : new MetadataListValue<MetadataMapValue>();
            var mdMap = new MetadataMapValue(Map.of("fqdn", fqdnPort));

            if (!mdSet.getListValue().contains(mdMap))
            {
                mdSet.addInList(mdMap);
            }

            getClusterMetadata().addMetadata(MetaDataType.CLUSTER, interPlmnfqdnAndPort, mdSet);

            ericProxyMetaData.put(METADATA.INTER_PLMN_FQDN, List.of(interPlmnfqdnAndPort));
        }

        // per nf counter metadata
        metadataMap.addMetadata(MetaDataType.ERIC_PROXY_ENDPOINT,
                                METADATA.PER_NF_COUNTER,
                                Boolean.TRUE.equals(pool.getEnableStatsPerNfInstance()) ? new MetadataBooleanValue(true) : new MetadataBooleanValue(false));

        // prefix metadata
        if (nfServ.getPrefix() != null && !nfServ.getPrefix().isEmpty())
        {
            metadataMap.addMetadata(MetaDataType.ERIC_PROXY_ENDPOINT, METADATA.PREFIX, new MetadataStringValue(nfServ.getPrefix()));
        }

        ericProxyMetaData.entrySet().forEach(entry ->
        {
            var listMdString = entry.getValue().stream().map(MetadataStringValue::new).toList();
            metadataMap.addMetadata(MetaDataType.ERIC_PROXY_ENDPOINT, entry.getKey(), new MetadataListValue<>(listMdString));
        });

        pxEp.setEndpointMetadata(metadataMap);

        return pxEp;
    }

    /**
     * Create a ProxyEndpoint based on an IP. The "host" metadatum contains the
     * fqdn:port and ip:port
     *
     * @param ipAddr   the configured IP of the service the endpoint is made for
     * @param address  the address of the service an endpoint is made for,
     *                 containing the fqdn and scheme of the service
     * @param ipEpPort an optional containing the port used for the endpoint. if no
     *                 port is configured, defaults are used
     * @param prio     the priority to be used for the endpoint
     * @param poolName the name of the pool the service was taken from, to be used
     *                 in endpoint metadata
     * @return a ProxyEndpoint
     */
    protected ProxyEndpoint createProxyEndpointFromIp(String ipAddr,
                                                      IfAddress address,
                                                      Optional<Integer> ipEpPort,
                                                      Integer prio,
                                                      String poolName,
                                                      IfTypedNfAddressProperties nfServ,
                                                      boolean preferredHostInAuthorityHeader)
    {

        int port = ipEpPort.orElse(address.getScheme() == Scheme.HTTPS ? 443 : 80);
        var pxEp = new ProxyEndpoint(ipAddr,
                                     // use configured port, if it does not exist use the well known ports based on
                                     // configured scheme
                                     port,
                                     prio,
                                     Optional.ofNullable(address.getFqdn()),
                                     address.getScheme() == Scheme.HTTPS);

        var metadataMap = new ProxyMetadataMap();

        List<String> hostMetadata = new ArrayList<>();

        var ipAndPort = CommonConfigUtils.formatIpv4Ipv6Address(ipAddr) + ":" + port;
        var fqdnAndPort = Optional.ofNullable(address.getFqdn()).map(val -> val + ":" + port);
        hostMetadata.add(ipAndPort);
        if (fqdnAndPort.isPresent())
        {
            hostMetadata.add(fqdnAndPort.get().toLowerCase());
        }

        var mdHostValue = hostMetadata.stream().map(MetadataStringValue::new).toList();
        metadataMap.addMetadata(MetaDataType.LB, METADATA.HOST, new MetadataListValue<>(mdHostValue));

        var mdPoolValue = List.of(poolName).stream().map(MetadataStringValue::new).toList();
        metadataMap.addMetadata(MetaDataType.LB, METADATA.POOL, new MetadataListValue<>(mdPoolValue));

        boolean matchTLS = address.getScheme() == Scheme.HTTPS;
        pxEp.setMatchTLS(matchTLS);

        var mdTlsValue = new MetadataStringValue(Boolean.toString(matchTLS));
        metadataMap.addMetadata(MetaDataType.TRANSPORT_SOCKET, METADATA.MATCH_TLS, new MetadataListValue<>(List.of(mdTlsValue)));

        var ericProxyMetaData = getSupportMetadata(nfServ, preferredHostInAuthorityHeader);
        ericProxyMetaData.put(METADATA.NF_INSTANCE_ID, List.of(nfServ.getNfInstanceId() != null ? nfServ.getNfInstanceId() : ""));
        if (nfServ.getNfType() != null)
        {
            getNfTypeMetadata(ericProxyMetaData, nfServ.getNfType());
        }

        // this endpoint is addressable via interplmn fqdn -> include this mapping to
        // the clustermetadata and host metadata
        if (address.getInterPlmnFqdn() != null)
        {
            var interPlmnfqdnAndPort = address.getInterPlmnFqdn().toLowerCase() + ":" + port;

            var clusterMdMap = getClusterMetadata().getMetadataMap().get(MetaDataType.CLUSTER);

            var mappings = new HashMap<String, String>();
            mappings.put("ip", ipAndPort);
            fqdnAndPort.ifPresent(fqdnPort -> mappings.put("fqdn", fqdnPort.toLowerCase()));

            @SuppressWarnings("unchecked")
            var mdSet = clusterMdMap != null
                        && clusterMdMap.containsKey(interPlmnfqdnAndPort) ? (MetadataListValue<MetadataMapValue>) clusterMdMap.get(interPlmnfqdnAndPort)
                                                                          : new MetadataListValue<MetadataMapValue>();
            var mdMap = new MetadataMapValue(mappings);

            if (!mdSet.getListValue().contains(mdMap))
            {
                mdSet.addInList(mdMap);
            }

            getClusterMetadata().addMetadata(MetaDataType.CLUSTER, interPlmnfqdnAndPort, mdSet);

            ericProxyMetaData.put(METADATA.INTER_PLMN_FQDN, List.of(interPlmnfqdnAndPort));
        }

        // per nf counter metadata
        metadataMap.addMetadata(MetaDataType.ERIC_PROXY_ENDPOINT,
                                METADATA.PER_NF_COUNTER,
                                Boolean.TRUE.equals(pool.getEnableStatsPerNfInstance()) ? new MetadataBooleanValue(true) : new MetadataBooleanValue(false));

        // prefix metadata
        if (nfServ.getPrefix() != null && !nfServ.getPrefix().isEmpty())
        {
            metadataMap.addMetadata(MetaDataType.ERIC_PROXY_ENDPOINT, METADATA.PREFIX, new MetadataStringValue(nfServ.getPrefix()));
        }

        ericProxyMetaData.entrySet().forEach(entry ->
        {
            var listMdString = entry.getValue().stream().map(MetadataStringValue::new).toList();
            metadataMap.addMetadata(MetaDataType.ERIC_PROXY_ENDPOINT, entry.getKey(), new MetadataListValue<>(listMdString));
        });

        pxEp.setEndpointMetadata(metadataMap);

        return pxEp;
    }

    protected Map<String, List<String>> getSupportMetadata(IfTypedNfAddressProperties nfServ,
                                                           boolean preferredHostInAuthorityHeader)
    {
        var supportsMetaData = new HashMap<String, List<String>>();
        if (nfServ.getNfType().equalsIgnoreCase(METADATA.NF_TYPE_SCP) || nfServ.getNfType().equalsIgnoreCase(METADATA.NF_TYPE_SEPP_INDIRECT)
            || nfServ.getNfType().equalsIgnoreCase("seppTar"))
        {
            supportsMetaData.put(METADATA.SUPPORT, List.of(METADATA.INDIRECT));
        }
        else if (nfRequiresTfqdn(this.configInst, nfServ.getNfType()))
        {
            supportsMetaData.put(METADATA.SEPP_SUPPORT, List.of(METADATA.SUPPORT_TFQDN));
        }
        else if (nfServ.getNfType().equalsIgnoreCase("seppNone"))
        {
            supportsMetaData.put(METADATA.SEPP_SUPPORT, List.of(METADATA.INDIRECT));
        }
        else
        {
            supportsMetaData.put(METADATA.SUPPORT, List.of(METADATA.SUPPORT_NF));
        }

        return supportsMetaData;
    }

    protected static void getNfTypeMetadata(Map<String, List<String>> supportsMetaData,
                                            String nfType)
    {
        if (nfType.equalsIgnoreCase(METADATA.NF_TYPE_SCP))
        {
            supportsMetaData.put(METADATA.NF_TYPE, List.of(nfType.toUpperCase()));
        }
        else if (nfType.equalsIgnoreCase(METADATA.NF_TYPE_SEPP) || nfType.equalsIgnoreCase("seppTar") || nfType.equalsIgnoreCase("seppNone")
                 || nfType.equalsIgnoreCase(METADATA.NF_TYPE_SEPP_INDIRECT))
        {
            supportsMetaData.put(METADATA.NF_TYPE, List.of(METADATA.NF_TYPE_SEPP.toUpperCase()));
        }
        else
        {
            supportsMetaData.put(METADATA.NF_TYPE, List.of(METADATA.NF_TYPE_NF));
        }
    }

    protected List<ProxyEndpoint> createEndpointsFromServiceStream(Map<IfTypedNfInstance, Set<IfTypedNfService>> scpsWithServices,
                                                                   String poolName,
                                                                   boolean preferredHostInAuthorityHeader)
    {
        final List<ProxyEndpoint> pxEndpoints = new ArrayList<>();
        scpsWithServices.forEach((scp,
                                  nfServices) ->
        {
            var eps = createEndpointsFromServiceStream(scp.fetchNfService().stream(), 0, poolName, preferredHostInAuthorityHeader);

            List<String> lbHostMetadata = new ArrayList<>();

            nfServices.stream().map(IfTypedNfService::getAddress).forEach(addr ->
            {
                var fqdn = addr.getFqdn();

                var multEnd = addr.getMultipleIpEndpoint();
                if (multEnd != null && !multEnd.isEmpty())
                {
                    multEnd.forEach(multIpEp ->
                    {
                        var ips = Stream.concat(multIpEp.getIpv4Address().stream(), multIpEp.getIpv6Address().stream()).collect(Collectors.toList());
                        var defaultPort = addr.getScheme() == Scheme.HTTPS ? "443" : "80";
                        var port = multIpEp.getPort() == null ? defaultPort : multIpEp.getPort().toString();
                        if (ips.isEmpty())
                        {
                            // multipleIpEndpoint contains no ips -> get fqdn and defined port
                            lbHostMetadata.add(fqdn + ":" + port);
                        }
                        else
                        {
                            ips.forEach(ip -> lbHostMetadata.add(ip + ":" + port));
                        }
                    });

                }
                else
                {
                    // no multipleIpEps defined for this address, use default ports based on scheme
                    lbHostMetadata.add(fqdn + ":" + (addr.getScheme() == Scheme.HTTPS ? "443" : "80"));

                }

            });

            // override METADATA.HOST values of each scp with the hosts that are under its
            // domain
            var hostMetadata = lbHostMetadata.stream().map(MetadataStringValue::new).toList();
            eps.forEach(ep ->
            {
                ep.getEndpointMetadata().addMetadata(MetaDataType.LB, METADATA.HOST, new MetadataListValue<>(hostMetadata));
            });

            pxEndpoints.addAll(eps);
        });

        return pxEndpoints;
    }

    /**
     * Get all the static-nf-instance and discovered-nf-instances from the
     * nf-pool-discovery
     *
     * @param pool
     * @param seppInst
     * @return
     */
    public static List<IfTypedNfInstance> getAllNfInstances(IfNfPool pool,
                                                            IfNfInstance seppInst)
    {
        var staticNfInstances = Utils.streamIfExists(pool.getNfPoolDiscovery())
                                     .flatMap(p -> Utils.streamIfExists(p.getStaticNfInstanceDataRef()))
                                     .<IfStaticNfInstanceDatum>map(nfInstance -> Utils.getByName(seppInst.getStaticNfInstanceData(), nfInstance))
                                     .filter(Objects::nonNull)
                                     .<IfTypedNfInstance>flatMap(datum -> Utils.streamIfExists(datum.getStaticNfInstance()));

        var discoveredNfInstances = Utils.streamIfExists(pool.getNfPoolDiscovery())
                                         .<IfTypedNfInstance>flatMap(poolDisc -> Utils.streamIfExists(poolDisc.getDiscoveredNfInstance()));

        var nfInstList = Stream.concat(staticNfInstances, discoveredNfInstances).collect(Collectors.toList());

        nfInstList.stream().forEach(nfInstance ->
        {
            var svcStream = Utils.streamIfExists(nfInstance.fetchNfService());
            var nfType = nfInstance.getNfType() != null ? nfInstance.getNfType() : "nf-type-not-set";
            var nfInstanceId = nfInstance.getNfInstanceId() != null ? nfInstance.getNfInstanceId() : "";
            svcStream.forEach(svc ->
            {
                svc.setNfType(nfType);
                svc.setNfInstanceId(nfInstanceId);
                svc.setPrefix(svc.getApiPrefix() != null ? svc.getApiPrefix() : "");
            });
        });
        return nfInstList;
    }

    protected boolean nfRequiresTfqdn(IfNfInstance configInst,
                                      String nfType)
    {
        if (ConfigHelper.isScpConfiguration(configInst))
            return false;
        var seppInst = ConfigHelper.convertToSeppConfiguration(configInst);
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

    /**
     * Provided a list of ProxyEndpoints,sets the vtap endpoint flag on each of them
     * if vtap is configured for the provided pool
     * 
     * @param proxyEndpoints
     * @param poolName
     * @return
     */
    public void setEndpointVTapFlag(List<ProxyEndpoint> proxyEndpoints,
                                    String poolName)
    {
        var vtapSettings = ConfigHelper.createVtapConfigForPool(poolName, this.configInst);

        proxyEndpoints.forEach(ep ->
        {
            if (vtapSettings.isPresent() && vtapSettings.get().getVtapEnabled())
            {

                ep.setEndpointVtapFlag(true);

                var mdString = new MetadataStringValue("true");

                ep.getEndpointMetadata().addMetadata(MetaDataType.TRANSPORT_SOCKET, METADATA.VTAP_ENABLED, new MetadataListValue<>(List.of(mdString)));
            }
            else
            {
                ep.setEndpointVtapFlag(false);

                var mdString = new MetadataStringValue("false");

                ep.getEndpointMetadata().addMetadata(MetaDataType.TRANSPORT_SOCKET, METADATA.VTAP_ENABLED, new MetadataListValue<>(List.of(mdString)));
            }
        });

    }

}
