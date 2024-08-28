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
package com.ericsson.sc.proxy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ericsson.sc.configutil.CommonConfigUtils;
import com.ericsson.sc.glue.IfNfInstance;
import com.ericsson.sc.glue.IfNfPool;
import com.ericsson.sc.glue.IfPriorityGroup;
import com.ericsson.sc.glue.IfStaticNfInstanceDatum;
import com.ericsson.sc.glue.IfStaticScpInstance;
import com.ericsson.sc.glue.IfStaticScpInstanceDatum;
import com.ericsson.sc.glue.IfStaticSeppInstance;
import com.ericsson.sc.glue.IfStaticSeppInstanceDatum;
import com.ericsson.sc.glue.IfTypedNfAddressProperties;
import com.ericsson.sc.glue.IfTypedNfInstance;
import com.ericsson.sc.glue.IfTypedNfService;
import com.ericsson.sc.glue.IfTypedScpInstance;
import com.ericsson.sc.glue.IfTypedSeppInstance;
import com.ericsson.sc.nfm.model.Scheme;
import com.ericsson.sc.proxy.ProxyConstants.METADATA;
import com.ericsson.sc.proxyal.proxyconfig.ProxyVtapSettings;
import com.ericsson.sc.scp.model.Address;
import com.ericsson.sc.scp.model.MultipleIpEndpoint;
import com.ericsson.sc.scp.model.StaticNfInstance;
import com.ericsson.sc.scp.model.StaticNfService;
import com.ericsson.sc.scp.model.StaticScpDomainInfo;
import com.ericsson.sc.scp.model.StaticScpInstance;
import com.ericsson.sc.scp.model.StaticSeppInstance;
import com.ericsson.sc.sepp.model.NfInstance;
import com.ericsson.sc.sepp.model.RequiredForNfType;
import com.ericsson.utilities.common.Utils;
import com.ericsson.utilities.exceptions.BadConfigurationException;

public class ConfigHelper
{

    private ConfigHelper()
    {
        // Private constructor
    }

    /**
     * Check if the given pool has static-scp-instance-ref.
     *
     * @param pool (the relevant primary or last resort nf-pool)
     * @return true if pool contains static-scp-reference or a priority-group with
     *         static-scp-reference
     */
    public static boolean hasScpInPool(Optional<IfNfPool> pool)
    {
        if (pool.isEmpty())
        {
            return false;
        }

        return ((pool.get().getStaticScpInstanceDataRef() != null && !pool.get().getStaticScpInstanceDataRef().isEmpty())
                // check for static-sc-instance-data-ref in priority-groups
                || (Utils.streamIfExists(pool.get().getPriorityGroup())
                         .anyMatch(priorityGroup -> priorityGroup.getStaticScpInstanceDataRef() != null
                                                    && !priorityGroup.getStaticScpInstanceDataRef().isEmpty())));
    }

    /**
     * Returns a string, uniquely identifying an nf-service. At the time written,
     * static-nf-instances/services use the name as key, while discovered ones use
     * the id.
     *
     * @param inst
     * @param svc
     * @return the unique identifier for a NF service instance
     */
    public static String getUniqueIdForSvc(IfTypedNfInstance inst,
                                           IfTypedNfService svc)
    {
        if (svc instanceof com.ericsson.sc.sepp.model.DiscoveredNfService || svc instanceof com.ericsson.sc.scp.model.DiscoveredNfService)
        {
            return inst.getNfInstanceId() + ":" + svc.getNfServiceId();
        }
        else
        {
            return inst.getName() + ":" + svc.getName();
        }
    }

    /**
     * This function finds static SCPs/SEPPs configured on a nf-pool level and
     * returns a list containing their respective services. It also sets the nftype
     * of said services to "scp"/"sepp"
     *
     * @param pool          The nf-pool
     * @param seppOrScpInst
     * @return List of ServiceHelper
     */
    public static List<IfTypedNfAddressProperties> getStaticProxyServices(IfNfPool pool,
                                                                          IfNfInstance seppOrScpInst)
    {
        if (pool.getStaticScpInstanceDataRef() != null && pool.getStaticScpInstanceDataRef().size() > 0)
        {
            return getStaticScpServices(pool.getStaticScpInstanceDataRef(), seppOrScpInst);
        }
        else if (pool.getStaticSeppInstanceDataRef() != null)
        {
            return getStaticSeppServices(pool.getStaticSeppInstanceDataRef(), seppOrScpInst);
        }
        return Collections.emptyList();
    }

    /**
     * This function finds static SCPs/SEPPs configured on a nf-pool's priority
     * groups and returns a list containing their respective services. It also sets
     * the nftype of said services to "scp"/"sepp"
     *
     * @param prioGroup     The priority group
     * @param seppOrScpInst
     * @return List of nfServices (of type ServiceHelper)
     */
    public static List<IfTypedNfAddressProperties> getStaticProxyServices(IfPriorityGroup prioGroup,
                                                                          IfNfInstance seppOrScpInst)
    {
        if (prioGroup.getStaticScpInstanceDataRef() != null && prioGroup.getStaticScpInstanceDataRef().size() > 0)
        {
            return getStaticScpServices(prioGroup.getStaticScpInstanceDataRef(), seppOrScpInst);
        }
        else if (prioGroup.getStaticSeppInstanceDataRef() != null && prioGroup.getStaticSeppInstanceDataRef().size() > 0)
        {
            return getStaticSeppServices(prioGroup.getStaticSeppInstanceDataRef(), seppOrScpInst);
        }
        return Collections.emptyList();
    }

    private static List<IfTypedNfAddressProperties> getStaticScpServices(List<String> refs,
                                                                         IfNfInstance seppOrScpInst)
    {
        Stream<IfStaticScpInstance> scpInstances = Utils.streamIfExists(refs)
                                                        .<IfStaticScpInstanceDatum>map(instanceRef -> Utils.getByName(seppOrScpInst.getStaticScpInstanceData(),
                                                                                                                      instanceRef))
                                                        .filter(Objects::nonNull)
                                                        .<IfStaticScpInstance>flatMap(datum -> Utils.streamIfExists(datum.getStaticScpInstance()));

        return scpInstances.flatMap(inst ->
        {

            inst.fetchNfService().forEach(svc ->
            {
                svc.setNfInstanceId(inst.getNfInstanceId() != null ? inst.getNfInstanceId() : "");
                svc.setNfType(METADATA.NF_TYPE_SCP);
            });
            inst.fetchScpDomainInfo().forEach(domainInfo ->
            {
                domainInfo.setNfInstanceId(inst.getNfInstanceId() != null ? inst.getNfInstanceId() : "");
                domainInfo.setNfType(METADATA.NF_TYPE_SCP);
            });

            return Stream.concat(Utils.streamIfExists(inst.fetchNfService()), Utils.streamIfExists(inst.fetchScpDomainInfo()));
        }).collect(Collectors.toList());
    }

    public static List<IfStaticScpInstance> getStaticScpInstances(List<String> refs,
                                                                  IfNfInstance seppOrScpInst)
    {
        List<IfStaticScpInstanceDatum> scpInstanceData = new ArrayList<>();
        refs.stream().forEach(ref -> scpInstanceData.add(Utils.getByName(seppOrScpInst.getStaticScpInstanceData().stream().toList(), ref)));

        List<IfStaticScpInstance> scpInstances = new ArrayList<>();
        scpInstanceData.stream().forEach(dat -> scpInstances.addAll(dat.getStaticScpInstance()));

        scpInstances.forEach(inst ->
        {
            inst.fetchNfService().forEach(svc ->
            {
                svc.setNfInstanceId(inst.getNfInstanceId() != null ? inst.getNfInstanceId() : "");
                svc.setNfType(METADATA.NF_TYPE_SCP);
            });
            inst.fetchScpDomainInfo().forEach(domainInfo ->
            {
                domainInfo.setNfInstanceId(inst.getNfInstanceId() != null ? inst.getNfInstanceId() : "");
                domainInfo.setNfType(METADATA.NF_TYPE_SCP);
            });
        });
        return scpInstances;
    }

    public static List<IfTypedNfAddressProperties> getStaticSeppServices(List<String> refs,
                                                                         IfNfInstance seppOrScpInst)
    {
        Stream<IfStaticSeppInstance> seppInstances = Utils.streamIfExists(refs)
                                                          .<IfStaticSeppInstanceDatum>map(instanceRef -> Utils.getByName(seppOrScpInst.getStaticSeppInstanceData(),
                                                                                                                         instanceRef))
                                                          .filter(Objects::nonNull)
                                                          .<IfStaticSeppInstance>flatMap(datum -> Utils.streamIfExists(datum.getStaticSeppInstance()));

        List<IfTypedNfAddressProperties> seppServiceList = new ArrayList<>();

        seppInstances.forEach(inst ->

        {
            inst.setNfType(METADATA.NF_TYPE_SEPP_INDIRECT);
            inst.setNfInstanceName(inst.getName());
            seppServiceList.add(inst);
        });

        return seppServiceList;
    }

    public static List<IfTypedScpInstance> getDiscoveredScpInstances(IfNfPool pool)
    {
        var discoveredScpInstances = Utils.streamIfExists(pool.getNfPoolDiscovery())
                                          .<IfTypedScpInstance>flatMap(poolDisc -> Utils.streamIfExists(poolDisc.getDiscoveredScpInstance()))
                                          .collect(Collectors.toList());
        discoveredScpInstances.stream().forEach(inst -> inst.fetchScpDomainInfo().forEach(domain -> domain.setNfType(METADATA.NF_TYPE_SCP)));
        return discoveredScpInstances;
    }

    public static List<IfTypedSeppInstance> getStaticSeppInstances(List<String> refs,
                                                                   IfNfInstance seppOrScpInst)
    {

        List<IfStaticSeppInstanceDatum> seppInstanceData = new ArrayList<>();
        refs.stream().forEach(ref -> seppInstanceData.add(Utils.getByName(seppOrScpInst.getStaticSeppInstanceData().stream().toList(), ref)));

        List<IfTypedSeppInstance> seppInstances = new ArrayList<>();
        seppInstanceData.stream().forEach(dat -> seppInstances.addAll(dat.getStaticSeppInstance()));

        seppInstances.forEach(inst -> inst.setNfType(METADATA.NF_TYPE_SEPP_INDIRECT));

        return seppInstances;
    }

    public static boolean nfRequiresTfqdn(IfNfInstance configInst,
                                          String nfType)
    {
        NfInstance seppInst;
        if (configInst instanceof com.ericsson.sc.sepp.model.NfInstance)
        {
            seppInst = (NfInstance) configInst;
        }
        else
        {
            return false;
        }
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

    public static List<IfTypedNfAddressProperties> getAllNfServices(IfNfPool pool,
                                                                    IfNfInstance seppInst)
    {
        var staticNfServices = Utils.streamIfExists(pool.getNfPoolDiscovery())
                                    .flatMap(p -> Utils.streamIfExists(p.getStaticNfInstanceDataRef()))
                                    .<IfStaticNfInstanceDatum>map(nfInstance -> Utils.getByName(seppInst.getStaticNfInstanceData(), nfInstance))
                                    .filter(Objects::nonNull)
                                    .<IfTypedNfInstance>flatMap(datum -> Utils.streamIfExists(datum.getStaticNfInstance()))
                                    .<IfTypedNfAddressProperties>flatMap(nfInstance ->
                                    {
                                        var svcStream = Utils.streamIfExists(nfInstance.fetchNfService());
                                        var nfType = nfInstance.getNfType() != null ? nfInstance.getNfType() : "";
                                        var nfInstanceId = nfInstance.getNfInstanceId() != null ? nfInstance.getNfInstanceId() : "";
                                        var nfInstanceName = nfInstance.getName() != null ? nfInstance.getName() : "";

                                        return svcStream.map(svc ->
                                        {
                                            svc.setNfType(nfType);
                                            svc.setNfInstanceId(nfInstanceId);
                                            svc.setNfInstanceName(nfInstanceName);
                                            svc.setPrefix(svc.getApiPrefix() != null ? svc.getApiPrefix() : "");
                                            return svc;
                                        });
                                    });

        var discoveredNfServices = Utils.streamIfExists(pool.getNfPoolDiscovery())
                                        .<IfTypedNfInstance>flatMap(poolDisc -> Utils.streamIfExists(poolDisc.getDiscoveredNfInstance()))
                                        .<IfTypedNfAddressProperties>flatMap(nfInstance ->
                                        {
                                            var svcStream = Utils.streamIfExists(nfInstance.fetchNfService());
                                            var nfType = nfInstance.getNfType() != null ? nfInstance.getNfType() : "";
                                            var nfInstanceId = nfInstance.getNfInstanceId() != null ? nfInstance.getNfInstanceId() : "";
                                            var nfInstanceName = nfInstance.getName() != null ? nfInstance.getName() : "";
                                            return svcStream.map(svc ->
                                            {
                                                svc.setNfType(nfType);
                                                svc.setNfInstanceId(nfInstanceId);
                                                svc.setNfInstanceName(nfInstanceName);
                                                svc.setPrefix(svc.getApiPrefix() != null ? svc.getApiPrefix() : "");
                                                return svc;
                                            });
                                        });

        return Stream.concat(staticNfServices, discoveredNfServices).collect(Collectors.toList());
    }

    public static com.ericsson.sc.sepp.model.FailoverProfile getSeppFailoverProfileOrDefault(Optional<String> failoverProfName,
                                                                                             final List<com.ericsson.sc.sepp.model.FailoverProfile> failoverProfiles,
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
                var failoverProfile = new com.ericsson.sc.sepp.model.FailoverProfile(failoverProf.get());

                if (!lastResortPoolPresent)
                {
                    failoverProfile.setLastResortNfPoolReselectsMax(0);
                }

                return failoverProfile;
            }
        }
        else
        {
            var retryCondition = new com.ericsson.sc.sepp.model.RetryCondition();
            var httpStatusDefault = List.of(500, 501, 502, 503, 504);
            retryCondition.setConnectFailure(true);
            retryCondition.setRefusedStream(true);
            retryCondition.setReset(true);
            retryCondition.setHttpStatus(httpStatusDefault);

            var fop = new com.ericsson.sc.sepp.model.FailoverProfile();
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

    public static com.ericsson.sc.scp.model.FailoverProfile getScpFailoverProfileOrDefault(Optional<String> failoverProfName,
                                                                                           final List<com.ericsson.sc.scp.model.FailoverProfile> failoverProfiles,
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
                var failoverProfile = new com.ericsson.sc.scp.model.FailoverProfile(failoverProf.get());

                if (!lastResortPoolPresent)
                {
                    failoverProfile.setLastResortNfPoolReselectsMax(0);
                }

                return failoverProfile;
            }
        }
        else
        {
            return getScpDefaultFailoverProfile(lastResortPoolPresent);
        }
    }

    public static com.ericsson.sc.scp.model.FailoverProfile getScpDefaultFailoverProfile(boolean lastResortPoolPresent)
    {

        var retryCondition = new com.ericsson.sc.scp.model.RetryCondition();
        var httpStatusDefault = List.of(500, 501, 502, 503, 504);
        retryCondition.setConnectFailure(true);
        retryCondition.setRefusedStream(true);
        retryCondition.setReset(true);
        retryCondition.setHttpStatus(httpStatusDefault);

        var fop = new com.ericsson.sc.scp.model.FailoverProfile();
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

    /**
     * Return the alt_stat_name for a cluster to avoid mismatches between Listener
     * Routes and Cluster. All clusters under a service get the same alt_stat_name
     * in the Envoy config so that they end up in the same PM counter.
     *
     * @param nfInstanceName
     * @param poolName
     * @return String alt_stat_name
     */

    public static String getClusterAltStatName(String nfInstanceName,
                                               String poolName)
    {
        return "egress.n8e." + nfInstanceName + ".p2l." + poolName + ".g3p.egress";
    }

    public static boolean isScpConfiguration(IfNfInstance config)
    {
        return config instanceof com.ericsson.sc.scp.model.NfInstance;
    }

    public static com.ericsson.sc.sepp.model.NfInstance convertToSeppConfiguration(IfNfInstance config)
    {
        return (com.ericsson.sc.sepp.model.NfInstance) config;
    }

    public static com.ericsson.sc.scp.model.NfInstance convertToScpConfiguration(IfNfInstance config)
    {
        return (com.ericsson.sc.scp.model.NfInstance) config;
    }

    /**
     * @param poolName
     * @param nfInst
     * @return
     */
    public static Optional<ProxyVtapSettings> createVtapConfigForPool(String poolName,
                                                                      IfNfInstance nfInst)
    {

        var vtap = nfInst.getVtap();
        if (vtap == null || !vtap.getEnabled() || vtap.getVtapConfiguration() == null || vtap.getVtapConfiguration().getProxy() == null
            || vtap.getVtapConfiguration().getProxy().getEgress() == null || vtap.getVtapConfiguration().getProxy().getEgress().isEmpty())
        {
            return Optional.empty();
        }
        var egressList = vtap.getVtapConfiguration().getProxy().getEgress();
        var allNfPoolEgress = egressList.stream().filter(egress -> egress.getAllNfPools() != null && egress.getEnabled()).findFirst();

        if (allNfPoolEgress.isPresent())
        {
            return Optional.of(new ProxyVtapSettings(allNfPoolEgress.get().getName(),
                                                     nfInst.getName(),
                                                     allNfPoolEgress.get().getEnabled(),
                                                     ProxyVtapSettings.Direction.EGRESS));
        }
        return egressList.stream()
                         .filter(egress -> egress.getNfPoolRef().stream().anyMatch(poolName::equals))
                         .map(egress -> new ProxyVtapSettings(egress.getName(), nfInst.getName(), egress.getEnabled(), ProxyVtapSettings.Direction.EGRESS))
                         .findAny();

    }

    // Refactoring tbd
    // Used to breakdown scp instances to include only one endpoint
    public static List<IfTypedScpInstance> breakDownEndPointsScpInst(Stream<IfTypedScpInstance> streamInstances)
    {
        List<IfTypedScpInstance> scpInstanceList = new ArrayList<>();

        streamInstances.forEach(inst -> inst.fetchScpDomainInfo().stream().forEach(scpDomainInfo ->
        {
            var addr = scpDomainInfo.getAddress();
            var multEnd = addr.getMultipleIpEndpoint();
            var priority = scpDomainInfo.getPriority();
            var numberOfMultipleIpEndpoints = addr.getMultipleIpEndpoint().size();
            var capacity = scpDomainInfo.getCapacity() != null ? scpDomainInfo.getCapacity() : 100;
            if (multEnd != null && !multEnd.isEmpty())
            {
                multEnd.forEach(endpoint ->
                {
                    endpoint.getIpv4Address().stream().forEach(ipv4 ->
                    {
                        StaticScpInstance tmpInst = new StaticScpInstance();
                        tmpInst.setNfInstanceId(inst.getNfInstanceId());
                        tmpInst.setNfType(inst.getNfType());
                        tmpInst.setLocality(inst.getLocality());
                        tmpInst.setNfSetId(inst.getNfSetId());
                        tmpInst.setScpDomain(inst.getScpDomain());

                        StaticScpDomainInfo tmpDom = new StaticScpDomainInfo();

                        Address tmpAddr = new Address();
                        tmpAddr.setScheme(scpDomainInfo.getAddress().getScheme());
                        tmpAddr.setFqdn(scpDomainInfo.getAddress().getFqdn());

                        MultipleIpEndpoint tmpEndPoint = new MultipleIpEndpoint();
                        tmpEndPoint.setIpv4Address(List.of(ipv4));
                        tmpEndPoint.setPort(endpoint.getPort());
                        tmpEndPoint.setTransport(endpoint.getTransport());

                        tmpAddr.setMultipleIpEndpoint(List.of(tmpEndPoint));

                        tmpDom.setAddress(tmpAddr);
                        tmpDom.setPriority(priority);
                        tmpDom.setCapacity(numberOfMultipleIpEndpoints > 1 ? (int) capacity / numberOfMultipleIpEndpoints : capacity);
                        tmpInst.setStaticScpDomainInfo(List.of(tmpDom));
                        scpInstanceList.add(tmpInst);
                    });

                    endpoint.getIpv6Address().stream().forEach(ipv6 ->
                    {
                        StaticScpInstance tmpInst = new StaticScpInstance();
                        tmpInst.setNfInstanceId(inst.getNfInstanceId());
                        tmpInst.setNfType(inst.getNfType());
                        tmpInst.setLocality(inst.getLocality());
                        tmpInst.setNfSetId(inst.getNfSetId());
                        tmpInst.setScpDomain(inst.getScpDomain());

                        StaticScpDomainInfo tmpDom = new StaticScpDomainInfo();

                        Address tmpAddr = new Address();
                        tmpAddr.setScheme(scpDomainInfo.getAddress().getScheme());
                        tmpAddr.setFqdn(scpDomainInfo.getAddress().getFqdn());

                        MultipleIpEndpoint tmpEndPoint = new MultipleIpEndpoint();
                        tmpEndPoint.setIpv6Address(List.of(ipv6));
                        tmpEndPoint.setPort(endpoint.getPort());
                        tmpEndPoint.setTransport(endpoint.getTransport());

                        tmpAddr.setMultipleIpEndpoint(List.of(tmpEndPoint));

                        tmpDom.setAddress(tmpAddr);
                        tmpDom.setPriority(priority);
                        tmpDom.setCapacity(numberOfMultipleIpEndpoints > 1 ? (int) capacity / numberOfMultipleIpEndpoints : capacity);
                        tmpInst.setStaticScpDomainInfo(List.of(tmpDom));
                        scpInstanceList.add(tmpInst);
                    });

                });
            }
            else
            {
                StaticScpInstance tmpInst = new StaticScpInstance();
                tmpInst.setNfInstanceId(inst.getNfInstanceId());
                tmpInst.setNfType(scpDomainInfo.getNfType());
                tmpInst.setLocality(inst.getLocality());
                tmpInst.setNfSetId(inst.getNfSetId());
                tmpInst.setScpDomain(inst.getScpDomain());

                StaticScpDomainInfo tmpDom = new StaticScpDomainInfo();

                Address tmpAddr = new Address();
                tmpAddr.setScheme(scpDomainInfo.getAddress().getScheme());

                tmpAddr.setFqdn(scpDomainInfo.getAddress().getFqdn());
                tmpAddr.setMultipleIpEndpoint(List.of(new MultipleIpEndpoint().withPort(addr.getScheme() == Scheme.HTTPS ? 443 : 80)));
                tmpDom.setPriority(priority);
                tmpDom.setCapacity(numberOfMultipleIpEndpoints > 1 ? (int) capacity / numberOfMultipleIpEndpoints : capacity);
                tmpDom.setAddress(tmpAddr);
                tmpInst.setStaticScpDomainInfo(List.of(tmpDom));
                scpInstanceList.add(tmpInst);
            }
        }));
        scpInstanceList.forEach(inst ->
        {
            inst.fetchScpDomainInfo().forEach(domainInfo ->
            {
                domainInfo.setNfInstanceId(inst.getNfInstanceId() != null ? inst.getNfInstanceId() : "");
                domainInfo.setNfType(METADATA.NF_TYPE_SCP);
            });
        });
        return scpInstanceList;
    }

    public static List<IfStaticScpInstance> breakDownEndPointsStaticScpInst(List<IfStaticScpInstance> streamInstances)
    {
        List<IfStaticScpInstance> scpInstanceList = new ArrayList<>();
        streamInstances.forEach(inst ->
        {

            inst.fetchNfService().stream().forEach(nfSvc ->
            {
                var addr = nfSvc.getAddress();
                var multEnd = addr.getMultipleIpEndpoint();
                var priority = nfSvc.getPriority();
                var numberOfMultipleIpEndpoints = addr.getMultipleIpEndpoint().size();
                var capacity = nfSvc.getCapacity() != null ? nfSvc.getCapacity() : 100;
                if (multEnd != null && !multEnd.isEmpty())
                {
                    multEnd.forEach(endpoint ->
                    {
                        endpoint.getIpv4Address().stream().forEach(ipv4 ->
                        {
                            StaticScpInstance tmpInst = new StaticScpInstance();
                            tmpInst.setNfInstanceId(inst.getNfInstanceId());
                            tmpInst.setNfType(inst.getNfType());
                            tmpInst.setLocality(inst.getLocality());
                            tmpInst.setNfSetId(inst.getNfSetId());
                            tmpInst.setScpDomain(inst.getScpDomain());

                            StaticNfService tmpDom = new StaticNfService();

                            Address tmpAddr = new Address();
                            tmpAddr.setScheme(nfSvc.getAddress().getScheme());
                            tmpAddr.setFqdn(nfSvc.getAddress().getFqdn());

                            MultipleIpEndpoint tmpEndPoint = new MultipleIpEndpoint();
                            tmpEndPoint.setIpv4Address(List.of(ipv4));
                            tmpEndPoint.setPort(endpoint.getPort());
                            tmpEndPoint.setTransport(endpoint.getTransport());

                            tmpAddr.setMultipleIpEndpoint(List.of(tmpEndPoint));
                            tmpDom.setPriority(priority);
                            tmpDom.setCapacity(numberOfMultipleIpEndpoints > 1 ? (int) capacity / numberOfMultipleIpEndpoints : capacity);
                            tmpDom.setAddress(tmpAddr);
                            tmpInst.setStaticNfService(List.of(tmpDom));
                            scpInstanceList.add(tmpInst);
                        });

                        endpoint.getIpv6Address().stream().forEach(ipv6 ->
                        {
                            StaticScpInstance tmpInst = new StaticScpInstance();
                            tmpInst.setNfInstanceId(inst.getNfInstanceId());
                            tmpInst.setNfType(inst.getNfType());
                            tmpInst.setLocality(inst.getLocality());
                            tmpInst.setNfSetId(inst.getNfSetId());
                            tmpInst.setScpDomain(inst.getScpDomain());

                            StaticNfService tmpDom = new StaticNfService();

                            Address tmpAddr = new Address();
                            tmpAddr.setScheme(nfSvc.getAddress().getScheme());
                            tmpAddr.setFqdn(nfSvc.getAddress().getFqdn());

                            MultipleIpEndpoint tmpEndPoint = new MultipleIpEndpoint();
                            tmpEndPoint.setIpv6Address(List.of(ipv6));
                            tmpEndPoint.setPort(endpoint.getPort());
                            tmpEndPoint.setTransport(endpoint.getTransport());

                            tmpAddr.setMultipleIpEndpoint(List.of(tmpEndPoint));
                            tmpDom.setPriority(priority);
                            tmpDom.setCapacity(numberOfMultipleIpEndpoints > 1 ? (int) capacity / numberOfMultipleIpEndpoints : capacity);
                            tmpDom.setAddress(tmpAddr);
                            tmpInst.setStaticNfService(List.of(tmpDom));
                            scpInstanceList.add(tmpInst);
                        });
                    });
                }
                else
                {
                    StaticScpInstance tmpInst = new StaticScpInstance();
                    tmpInst.setNfInstanceId(inst.getNfInstanceId());
                    tmpInst.setNfType(inst.getNfType());
                    tmpInst.setLocality(inst.getLocality());
                    tmpInst.setNfSetId(inst.getNfSetId());
                    tmpInst.setScpDomain(inst.getScpDomain());

                    StaticNfService tmpDom = new StaticNfService();

                    Address tmpAddr = new Address();
                    tmpAddr.setScheme(nfSvc.getAddress().getScheme());
                    tmpAddr.setMultipleIpEndpoint(List.of(new MultipleIpEndpoint().withPort(addr.getScheme() == Scheme.HTTPS ? 443 : 80)));

                    tmpAddr.setFqdn(nfSvc.getAddress().getFqdn());
                    tmpDom.setAddress(tmpAddr);
                    tmpDom.setPriority(priority);
                    tmpDom.setCapacity(numberOfMultipleIpEndpoints > 1 ? (int) capacity / numberOfMultipleIpEndpoints : capacity);
                    tmpInst.setStaticNfService(List.of(tmpDom));
                    scpInstanceList.add(tmpInst);
                }
            });
            inst.fetchScpDomainInfo().stream().forEach(scpDomainInfo ->
            {

                var addr = scpDomainInfo.getAddress();
                var numberOfMultipleIpEndpoints = addr.getMultipleIpEndpoint().size();
                var capacity = scpDomainInfo.getCapacity() != null ? scpDomainInfo.getCapacity() : 100;
                var priority = scpDomainInfo.getPriority();
                var multEnd = addr.getMultipleIpEndpoint();
                if (multEnd != null && !multEnd.isEmpty())
                {
                    multEnd.forEach(endpoint ->
                    {
                        endpoint.getIpv4Address().stream().forEach(ipv4 ->
                        {
                            StaticScpInstance tmpInst = new StaticScpInstance();
                            tmpInst.setNfInstanceId(inst.getNfInstanceId());
                            tmpInst.setNfType(inst.getNfType());
                            tmpInst.setLocality(inst.getLocality());
                            tmpInst.setNfSetId(inst.getNfSetId());
                            tmpInst.setScpDomain(inst.getScpDomain());

                            StaticScpDomainInfo tmpDom = new StaticScpDomainInfo();

                            Address tmpAddr = new Address();
                            tmpAddr.setScheme(scpDomainInfo.getAddress().getScheme());
                            tmpAddr.setFqdn(scpDomainInfo.getAddress().getFqdn());

                            MultipleIpEndpoint tmpEndPoint = new MultipleIpEndpoint();
                            tmpEndPoint.setIpv4Address(List.of(ipv4));
                            tmpEndPoint.setPort(endpoint.getPort());
                            tmpEndPoint.setTransport(endpoint.getTransport());

                            tmpAddr.setMultipleIpEndpoint(List.of(tmpEndPoint));

                            tmpDom.setAddress(tmpAddr);
                            tmpDom.setPriority(priority);
                            tmpDom.setCapacity(numberOfMultipleIpEndpoints > 1 ? (int) capacity / numberOfMultipleIpEndpoints : capacity);
                            tmpInst.setStaticScpDomainInfo(List.of(tmpDom));
                            scpInstanceList.add(tmpInst);
                        });

                        endpoint.getIpv6Address().stream().forEach(ipv6 ->
                        {
                            StaticScpInstance tmpInst = new StaticScpInstance();
                            tmpInst.setNfInstanceId(inst.getNfInstanceId());
                            tmpInst.setNfType(inst.getNfType());
                            tmpInst.setLocality(inst.getLocality());
                            tmpInst.setNfSetId(inst.getNfSetId());
                            tmpInst.setScpDomain(inst.getScpDomain());

                            StaticScpDomainInfo tmpDom = new StaticScpDomainInfo();

                            Address tmpAddr = new Address();
                            tmpAddr.setScheme(scpDomainInfo.getAddress().getScheme());
                            tmpAddr.setFqdn(scpDomainInfo.getAddress().getFqdn());

                            MultipleIpEndpoint tmpEndPoint = new MultipleIpEndpoint();
                            tmpEndPoint.setIpv6Address(List.of(ipv6));
                            tmpEndPoint.setPort(endpoint.getPort());
                            tmpEndPoint.setTransport(endpoint.getTransport());

                            tmpAddr.setMultipleIpEndpoint(List.of(tmpEndPoint));

                            tmpDom.setAddress(tmpAddr);
                            tmpDom.setPriority(priority);
                            tmpDom.setCapacity(numberOfMultipleIpEndpoints > 1 ? (int) capacity / numberOfMultipleIpEndpoints : capacity);
                            tmpInst.setStaticScpDomainInfo(List.of(tmpDom));
                            scpInstanceList.add(tmpInst);
                        });
                    });
                }
                else
                {
                    StaticScpInstance tmpInst = new StaticScpInstance();
                    tmpInst.setNfInstanceId(inst.getNfInstanceId());
                    tmpInst.setNfType(inst.getNfType());
                    tmpInst.setLocality(inst.getLocality());
                    tmpInst.setNfSetId(inst.getNfSetId());
                    tmpInst.setScpDomain(inst.getScpDomain());

                    StaticScpDomainInfo tmpDom = new StaticScpDomainInfo();

                    Address tmpAddr = new Address();
                    tmpAddr.setScheme(scpDomainInfo.getAddress().getScheme());
                    tmpAddr.setMultipleIpEndpoint(List.of(new MultipleIpEndpoint().withPort(addr.getScheme() == Scheme.HTTPS ? 443 : 80)));

                    tmpAddr.setFqdn(scpDomainInfo.getAddress().getFqdn());
                    tmpDom.setPriority(priority);
                    tmpDom.setCapacity(numberOfMultipleIpEndpoints > 1 ? (int) capacity / numberOfMultipleIpEndpoints : capacity);
                    tmpDom.setAddress(tmpAddr);
                    tmpInst.setStaticScpDomainInfo(List.of(tmpDom));
                    scpInstanceList.add(tmpInst);
                }
            });
        });

        // Set the metadata
        scpInstanceList.forEach(inst ->
        {
            inst.fetchNfService().forEach(svc ->
            {
                svc.setNfInstanceId(inst.getNfInstanceId() != null ? inst.getNfInstanceId() : "");
                svc.setNfType(METADATA.NF_TYPE_SCP);
            });
            inst.fetchScpDomainInfo().forEach(domainInfo ->
            {
                domainInfo.setNfInstanceId(inst.getNfInstanceId() != null ? inst.getNfInstanceId() : "");
                domainInfo.setNfType(METADATA.NF_TYPE_SCP);
            });
        });
        return scpInstanceList;
    }

    // Used to breakdown nf instances to include only one endpoint
    public static List<IfTypedNfInstance> breakDownEndPointsNfInst(Stream<IfTypedNfInstance> streamInstances)
    {

        List<IfTypedNfInstance> nfInstanceList = new ArrayList<>();
        streamInstances.forEach(inst -> inst.fetchNfService().stream().forEach(nfSvc ->
        {

            var addr = nfSvc.getAddress();
            var priority = nfSvc.getPriority();
            var numberOfMultipleIpEndpoints = addr.getMultipleIpEndpoint().size();
            var capacity = nfSvc.getCapacity() != null ? nfSvc.getCapacity() : 100;

            var multEnd = addr.getMultipleIpEndpoint();
            if (multEnd != null && !multEnd.isEmpty())
            {
                multEnd.forEach(endpoint ->
                {
                    endpoint.getIpv4Address().stream().forEach(ipv4 ->
                    {
                        StaticNfInstance tmpInst = new StaticNfInstance();
                        tmpInst.setNfInstanceId(inst.getNfInstanceId());
                        tmpInst.setNfType(inst.getNfType());
                        tmpInst.setLocality(inst.getLocality());
                        tmpInst.setNfSetId(inst.getNfSetId());
                        tmpInst.setScpDomain(inst.getScpDomain());
                        StaticNfService tmpDom = new StaticNfService();

                        Address tmpAddr = new Address();
                        tmpAddr.setScheme(nfSvc.getAddress().getScheme());
                        tmpAddr.setFqdn(nfSvc.getAddress().getFqdn());

                        MultipleIpEndpoint tmpEndPoint = new MultipleIpEndpoint();
                        tmpEndPoint.setIpv4Address(List.of(ipv4));
                        tmpEndPoint.setPort(endpoint.getPort());
                        tmpEndPoint.setTransport(endpoint.getTransport());

                        tmpAddr.setMultipleIpEndpoint(List.of(tmpEndPoint));

                        tmpDom.setAddress(tmpAddr);
                        tmpDom.setPriority(priority);
                        tmpDom.setCapacity(numberOfMultipleIpEndpoints > 1 ? (int) capacity / numberOfMultipleIpEndpoints : capacity);

                        tmpInst.setStaticNfService(List.of(tmpDom));
                        nfInstanceList.add(tmpInst);
                    });
                    endpoint.getIpv6Address().stream().forEach(ipv6 ->
                    {
                        StaticNfInstance tmpInst = new StaticNfInstance();
                        tmpInst.setNfInstanceId(inst.getNfInstanceId());
                        tmpInst.setNfType(inst.getNfType());
                        tmpInst.setLocality(inst.getLocality());
                        tmpInst.setNfSetId(inst.getNfSetId());
                        tmpInst.setScpDomain(inst.getScpDomain());

                        StaticNfService tmpDom = new StaticNfService();

                        Address tmpAddr = new Address();
                        tmpAddr.setScheme(nfSvc.getAddress().getScheme());
                        tmpAddr.setFqdn(nfSvc.getAddress().getFqdn());

                        MultipleIpEndpoint tmpEndPoint = new MultipleIpEndpoint();
                        tmpEndPoint.setIpv6Address(List.of(ipv6));
                        tmpEndPoint.setPort(endpoint.getPort());
                        tmpEndPoint.setTransport(endpoint.getTransport());

                        tmpAddr.setMultipleIpEndpoint(List.of(tmpEndPoint));
                        tmpDom.setPriority(priority);
                        tmpDom.setCapacity(numberOfMultipleIpEndpoints > 1 ? (int) capacity / numberOfMultipleIpEndpoints : capacity);
                        tmpDom.setAddress(tmpAddr);
                        tmpInst.setStaticNfService(List.of(tmpDom));
                        nfInstanceList.add(tmpInst);
                    });
                });
            }
            else
            {
                StaticNfInstance tmpInst = new StaticNfInstance();
                tmpInst.setNfInstanceId(inst.getNfInstanceId());
                tmpInst.setNfType(inst.getNfType());
                tmpInst.setLocality(inst.getLocality());
                tmpInst.setNfSetId(inst.getNfSetId());
                tmpInst.setScpDomain(inst.getScpDomain());

                StaticNfService tmpDom = new StaticNfService();

                Address tmpAddr = new Address();
                tmpAddr.setScheme(nfSvc.getAddress().getScheme());
                tmpAddr.setFqdn(nfSvc.getAddress().getFqdn());

                tmpAddr.setMultipleIpEndpoint(List.of(new MultipleIpEndpoint().withPort(addr.getScheme() == Scheme.HTTPS ? 443 : 80)));
                tmpDom.setAddress(tmpAddr);
                tmpDom.setPriority(priority);
                tmpDom.setCapacity(numberOfMultipleIpEndpoints > 1 ? (int) capacity / numberOfMultipleIpEndpoints : capacity);
                tmpInst.setStaticNfService(List.of(tmpDom));
                nfInstanceList.add(tmpInst);
            }
        }));

        // Set the metadata
        nfInstanceList.stream().forEach(nfInstance ->
        {
            var svcStream = Utils.streamIfExists(nfInstance.fetchNfService());
            var nfType = nfInstance.getNfType() != null ? nfInstance.getNfType() : "nf-type-not-set";
            var nfInstanceId = nfInstance.getNfInstanceId() != null ? nfInstance.getNfInstanceId() : "";
            svcStream.forEach(svc ->
            {
                svc.setNfType(nfType);
                svc.setNfInstanceId(nfInstanceId);
            });
        });

        return nfInstanceList;
    }

    // Used to breakdown sepp instances to include only one endpoint
    public static List<IfTypedSeppInstance> breakDownEndPointsStaticSepp(Stream<IfTypedSeppInstance> streamInstances)
    {

        List<IfTypedSeppInstance> seppList = new ArrayList<>();

        streamInstances.forEach(instance ->
        {
            var addr = instance.getAddress();
            var multEnd = addr.getMultipleIpEndpoint();
            if (multEnd != null && !multEnd.isEmpty())
            {
                multEnd.forEach(endpoint ->
                {
                    endpoint.getIpv4Address().stream().forEach(ipv4 ->
                    {
                        StaticSeppInstance tmpInst = new StaticSeppInstance();
                        tmpInst.setNfInstanceId(instance.getNfInstanceId());
                        tmpInst.setNfType(instance.getNfType());
                        tmpInst.setLocality(instance.getLocality());
                        tmpInst.setNfSetId(instance.getNfSetId());
                        tmpInst.setScpDomain(instance.getScpDomain());

                        Address tmpAddr = new Address();
                        tmpAddr.setScheme(instance.getAddress().getScheme());
                        tmpAddr.setFqdn(instance.getAddress().getFqdn());

                        MultipleIpEndpoint tmpEndPoint = new MultipleIpEndpoint();
                        tmpEndPoint.setIpv4Address(List.of(ipv4));
                        tmpEndPoint.setPort(endpoint.getPort());
                        tmpEndPoint.setTransport(endpoint.getTransport());

                        tmpAddr.setMultipleIpEndpoint(List.of(tmpEndPoint));
                        tmpInst.setAddress(tmpAddr);
                        seppList.add(tmpInst);
                    });

                    endpoint.getIpv6Address().stream().forEach(ipv6 ->
                    {
                        StaticSeppInstance tmpInst = new StaticSeppInstance();
                        tmpInst.setNfInstanceId(instance.getNfInstanceId());
                        tmpInst.setNfType(instance.getNfType());
                        tmpInst.setLocality(instance.getLocality());
                        tmpInst.setNfSetId(instance.getNfSetId());
                        tmpInst.setScpDomain(instance.getScpDomain());

                        Address tmpAddr = new Address();
                        tmpAddr.setScheme(instance.getAddress().getScheme());
                        tmpAddr.setFqdn(instance.getAddress().getFqdn());

                        MultipleIpEndpoint tmpEndPoint = new MultipleIpEndpoint();
                        tmpEndPoint.setIpv4Address(List.of(ipv6));
                        tmpEndPoint.setPort(endpoint.getPort());
                        tmpEndPoint.setTransport(endpoint.getTransport());

                        tmpAddr.setMultipleIpEndpoint(List.of(tmpEndPoint));
                        tmpInst.setAddress(tmpAddr);
                        seppList.add(tmpInst);
                    });
                });
            }
            else
            {
                StaticSeppInstance tmpInst = new StaticSeppInstance();
                tmpInst.setNfInstanceId(instance.getNfInstanceId());
                tmpInst.setNfType(instance.getNfType());
                tmpInst.setLocality(instance.getLocality());
                tmpInst.setNfSetId(instance.getNfSetId());
                tmpInst.setScpDomain(instance.getScpDomain());

                Address tmpAddr = new Address();
                tmpAddr.setScheme(instance.getAddress().getScheme());
                tmpAddr.setMultipleIpEndpoint(List.of(new MultipleIpEndpoint().withPort(addr.getScheme() == Scheme.HTTPS ? 443 : 80)));
                tmpAddr.setFqdn(instance.getAddress().getFqdn());
                tmpInst.setAddress(tmpAddr);
                seppList.add(tmpInst);
            }

        });

        return seppList;
    }

}
