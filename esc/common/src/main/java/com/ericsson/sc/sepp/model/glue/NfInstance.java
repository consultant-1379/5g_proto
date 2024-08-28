/**
 * COPYRIGHT ERICSSON GMBH 2019
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: May 9, 2019
 *     Author: eedstl
 */

package com.ericsson.sc.sepp.model.glue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFProfile;
import com.ericsson.sc.configutil.CommonConfigUtils;
import com.ericsson.sc.glue.NFProfileBuilder;
import com.ericsson.sc.nfm.model.NfProfile;
import com.ericsson.sc.nfm.model.NfService;
import com.ericsson.sc.nfm.model.Scheme;
import com.ericsson.sc.sepp.model.DnsProfile;
import com.ericsson.sc.sepp.model.NfDiscovery;
import com.ericsson.sc.sepp.model.NfManagement;
import com.ericsson.sc.sepp.model.NfPeerInfo.OutMessageHandling;
import com.ericsson.sc.sepp.model.Nrf;
import com.ericsson.sc.sepp.model.NrfGroup;
import com.ericsson.sc.sepp.model.NrfService;
import com.ericsson.sc.sepp.model.ServiceAddress;
import com.ericsson.sc.utilities.dns.DnsCache;
import com.ericsson.sc.utilities.dns.IpFamily;
import com.ericsson.utilities.common.Rdn;
import com.ericsson.utilities.common.Utils;
import com.ericsson.utilities.exceptions.BadConfigurationException;
import com.ericsson.utilities.http.Url;
import com.ericsson.utilities.metrics.MetricRegister;

public class NfInstance
{
    public static class Pool
    {
        final NfFunction parent;
        final BiFunction<Pool, Rdn, NfInstance> creator;
        final Consumer<Rdn> terminator;

        private final Map<String, NfInstance> pool;
        private final List<NfInstance> list;

        public Pool(final NfFunction parent,
                    final BiFunction<Pool, Rdn, NfInstance> creator)
        {
            this(parent, creator, null);
        }

        public Pool(final NfFunction parent,
                    final BiFunction<Pool, Rdn, NfInstance> creator,
                    final Consumer<Rdn> terminator)
        {
            this.parent = parent;
            this.creator = creator;
            this.terminator = terminator;
            this.pool = new ConcurrentHashMap<>();
            this.list = new CopyOnWriteArrayList<>();
        }

        public NfInstance get(final int index)
        {
            return !this.list.isEmpty() ? this.list.get(index) : null;
        }

        public void stop()
        {
            this.update(null, true, false);
        }

        public void update(final List<com.ericsson.sc.sepp.model.NfInstance> instances,
                           final boolean userRelated,
                           final boolean dnsRelated)
        {
            final Set<String> configuredKeys = new HashSet<>();

            if (instances != null)
            {
                instances.forEach(instance ->
                {
                    configuredKeys.add(instance.getName());

                    if (!this.pool.containsKey(instance.getName()))
                    {
                        final Rdn rdn = this.parent.rdn.add("nf-instance", instance.getName());

                        try
                        {
                            final NfInstance nfInstance = this.creator.apply(this, rdn);
                            this.pool.put(instance.getName(), nfInstance);
                        }
                        catch (Exception t)
                        {
                            log.error("Error creating new NfInstance '{}'. Cause: {}", rdn, log.isDebugEnabled() ? t : t.toString());
                        }
                    }
                });
            }

            final Set<String> keysNotInConfig = new HashSet<>();

            this.pool.entrySet().forEach(entry ->
            {
                if (!configuredKeys.contains(entry.getKey()))
                {
                    keysNotInConfig.add(entry.getKey());

                    final NfInstance nfInstance = this.pool.get(entry.getKey());
                    nfInstance.stop();

                    if (this.terminator != null)
                    {
                        try
                        {
                            this.terminator.accept(nfInstance.getRdn());
                        }
                        catch (Exception t)
                        {
                            log.error("Error terminating NfInstance '{}'. Cause: {}", nfInstance.getRdn(), log.isDebugEnabled() ? t : t.toString());
                        }
                    }
                }
            });

            this.pool.keySet().removeIf(keysNotInConfig::contains);
            this.list.clear();

            if (instances != null)
            {
                instances.forEach(instance ->
                {
                    if (this.pool.containsKey(instance.getName()))
                    {
                        final NfInstance nfInstance = this.pool.get(instance.getName());
                        nfInstance.update(instance, userRelated, dnsRelated);
                        this.list.add(nfInstance);
                    }
                });
            }
        }
    }

    private static final Logger log = LoggerFactory.getLogger(NfInstance.class);

    public static String getSrcSbiNfPeerInfo(final com.ericsson.sc.sepp.model.NfInstance instance,
                                             final NrfGroup group,
                                             final Nrf nrf)
    {
        String srcSbiNfPeerInfo = null;

        if (instance.getNfPeerInfo() != null && instance.getNfPeerInfo().getOutMessageHandling() == OutMessageHandling.ON)
        {
            final Function<NfProfile, Optional<String>> getFqdn = profile ->
            {
                if (profile != null)
                {
                    final ServiceAddress address = Utils.getByName(instance.getServiceAddress(), profile.getServiceAddressRef());

                    if (address != null)
                        return Optional.ofNullable(address.getFqdn());
                }

                return Optional.empty();
            };

            final String fqdn = getFqdn.apply(Utils.getByName(instance.getNfProfile(), nrf.getNfProfileRef()))
                                       .orElse(getFqdn.apply(Utils.getByName(instance.getNfProfile(), group.getNfProfileRef())).orElse(null));

            final StringBuilder b = new StringBuilder();

            if (fqdn != null)
                b.append("srcsepp=SEPP-").append(fqdn);

            if (nrf.getNrfInstanceId() != null)
            {
                if (b.length() > 0)
                    b.append(";");

                b.append("dstinst=").append(nrf.getNrfInstanceId());
            }

            if (b.length() > 0)
                srcSbiNfPeerInfo = b.toString();
        }

        log.debug("srcSbiNfPeerInfo={}", srcSbiNfPeerInfo);

        return srcSbiNfPeerInfo;
    }

    /**
     * Gathers and returns all NRF groups that are referenced in the configuration
     * at <code>
     * <ul>
     * <li>nrf-service/nf-management/nrf-group-ref</li>
     * <li>nrf-service/nf-discovery/nrf-group-ref</li>
     * <li>nf-pool/nf-pool-discovery/nrf-group-ref</li>
     * <li>nf-pool/nf-pool-discovery/nrf-query/nrf-group-ref</li>
     * </ul>
     * </code>
     * 
     * @param instance
     * @return The set of all referenced NRF groups
     */
    private static Set<String> getReferencedNrfGroups(final com.ericsson.sc.sepp.model.NfInstance instance)
    {
        final Set<String> referencedNrfGroups = new HashSet<>();

        Optional.ofNullable(instance.getNrfService()).ifPresent(nrfService ->
        {
            referencedNrfGroups.addAll(Optional.ofNullable(nrfService.getNfManagement()).map(NfManagement::getNrfGroupRef).orElse(List.of()));
            referencedNrfGroups.addAll(Optional.ofNullable(nrfService.getNfDiscovery())
                                               .stream()
                                               .map(NfDiscovery::getNrfGroupRef)
                                               .filter(Objects::nonNull)
                                               .collect(Collectors.toSet()));
        });

        referencedNrfGroups.addAll(instance.getNfPool()
                                           .stream()
                                           .flatMap(pool -> pool.getNfPoolDiscovery().stream())
                                           .flatMap(pd -> pd.getNrfGroupRef().stream())
                                           .collect(Collectors.toSet()));

        referencedNrfGroups.addAll(instance.getNfPool()
                                           .stream()
                                           .flatMap(pool -> pool.getNfPoolDiscovery().stream())
                                           .flatMap(pd -> pd.getNrfQuery().stream())
                                           .flatMap(query -> query.getNrfGroupRef().stream())
                                           .collect(Collectors.toSet()));

        log.debug("referencedNrfGroups={}", referencedNrfGroups);

        return referencedNrfGroups;
    }

    private static NfProfile merge(final NfProfile base,
                                   final NfProfile override)
    {
        if (base == null)
            return override;

        if (override == null)
            return base;

        // Merge base and override profiles such, that
        // o all existing simple values in override replace those in base
        // o all non-empty lists in override replace those in base (lists exist always)

        final NfProfile result = new NfProfile();

        result.setAdminState(override.getAdminState() == null ? base.getAdminState() : override.getAdminState());
        result.setAllowedNfDomain(override.getAllowedNfDomain().isEmpty() ? base.getAllowedNfDomain() : override.getAllowedNfDomain());
        result.setAllowedNfType(override.getAllowedNfType().isEmpty() ? base.getAllowedNfType() : override.getAllowedNfType());
        result.setAllowedNssai(override.getAllowedNssai().isEmpty() ? base.getAllowedNssai() : override.getAllowedNssai()); // Deprecated
        result.setAllowedNssai1(override.getAllowedNssai1().isEmpty() ? base.getAllowedNssai1() : override.getAllowedNssai1());
        result.setAllowedPlmn(override.getAllowedPlmn().isEmpty() ? base.getAllowedPlmn() : override.getAllowedPlmn());
        result.setCapacity(override.getCapacity() == null ? base.getCapacity() : override.getCapacity());
        result.setLocality(override.getLocality() == null ? base.getLocality() : override.getLocality());
        result.setName(override.getName() == null ? base.getName() : override.getName());
        result.setNfInstanceName(override.getNfInstanceName() == null ? base.getNfInstanceName() : override.getNfInstanceName());
        result.setNfService(override.getNfService().isEmpty() ? base.getNfService() : override.getNfService());
        result.setNfSetId(override.getNfSetId().isEmpty() ? base.getNfSetId() : override.getNfSetId());
        result.setNsi(override.getNsi().isEmpty() ? base.getNsi() : override.getNsi());
        result.setNfSpecificInfo(override.getNfSpecificInfo() == null ? base.getNfSpecificInfo() : override.getNfSpecificInfo()); // Deprecated
        result.setNfType(override.getNfType() == null ? base.getNfType() : override.getNfType());
        result.setPlmn(override.getPlmn().isEmpty() ? base.getPlmn() : override.getPlmn());
        result.setRequestedHeartbeatTimer(override.getRequestedHeartbeatTimer() == null ? base.getRequestedHeartbeatTimer()
                                                                                        : override.getRequestedHeartbeatTimer());
        result.setServiceAddressRef(override.getServiceAddressRef() == null ? base.getServiceAddressRef() : override.getServiceAddressRef());
        result.setServicePriority(override.getServicePriority() == null ? base.getServicePriority() : override.getServicePriority());
        result.setSnssai(override.getSnssai().isEmpty() ? base.getSnssai() : override.getSnssai()); // Deprecated
        result.setSnssai1(override.getSnssai1().isEmpty() ? base.getSnssai1() : override.getSnssai1());
        result.setScpDomain(override.getScpDomain().isEmpty() ? base.getScpDomain() : override.getScpDomain());
        result.setSeppInfo(override.getSeppInfo() == null ? base.getSeppInfo() : override.getSeppInfo());

        return result;
    }

    final Pool parent;
    private final Rdn rdn;
    private final NFProfileBuilder profileBuilder;
    private final Map<String, com.ericsson.sc.nrf.r17.Nrf.Pool> nrfGroups;

    private com.ericsson.sc.nrf.r17.Nrf.Pool nrfGroupForNfDiscovery;

    public NfInstance(final Pool parent,
                      final Rdn rdn,
                      final NFProfileBuilder profileBuilder)
    {
        this.parent = parent;
        this.rdn = rdn;
        this.profileBuilder = profileBuilder;
        this.nrfGroups = new ConcurrentHashMap<>();
        this.nrfGroupForNfDiscovery = null;
    }

    public synchronized com.ericsson.sc.nrf.r17.Nrf.Pool getNrfGroupForNfDiscovery()
    {
        return this.nrfGroupForNfDiscovery;
    }

    public Map<String, com.ericsson.sc.nrf.r17.Nrf.Pool> getNrfGroups()
    {
        return this.nrfGroups;
    }

    public Rdn getRdn()
    {
        return this.rdn;
    }

    public void stop()
    {
        this.update(null, true, false);
    }

    public synchronized void update(final com.ericsson.sc.sepp.model.NfInstance instance,
                                    final boolean userRelated,
                                    final boolean dnsRelated)
    {
        // If the update is due to DNS resolution, only the IP address has to be dealt
        // with, the configuration has not been changed. Hence, skip house-keeping.

        if (!dnsRelated)
        {
            final Set<String> configuredKeys = new HashSet<>();

            if (instance != null)
            {
                final Set<String> referencedNrfGroups = getReferencedNrfGroups(instance);
                final Set<String> usedNfInstanceIds = new HashSet<>();

                instance.getNrfGroup().forEach(group ->
                {
                    final NrfService nrfService = instance.getNrfService();

                    if (referencedNrfGroups.contains(group.getName()))
                    {
                        configuredKeys.add(group.getName());

                        /**
                         * If there is an nf-instance/nf-instance-id, take it for all
                         * nrf-group/nf-instance-id.
                         * 
                         * Otherwise, make sure that all nrf-groups have a different nf-instance-id.
                         * 
                         * Simultaneously set the nf-instance-id also in the model. This is needed later
                         * for updating the configuration in CMM.
                         */

                        if (instance.getNfInstanceId() != null)
                        {
                            group.withNfInstanceId(instance.getNfInstanceId());
                        }
                        else if (group.getNfInstanceId() != null)
                        {
                            if (!usedNfInstanceIds.add(group.getNfInstanceId()))
                            {
                                while (!usedNfInstanceIds.add(group.withNfInstanceId(UUID.randomUUID().toString()).getNfInstanceId()))
                                    ;
                            }
                        }
                        else
                        {
                            while (!usedNfInstanceIds.add(group.withNfInstanceId(UUID.randomUUID().toString()).getNfInstanceId()))
                                ;
                        }

                        final com.ericsson.sc.nrf.r17.Nrf.Pool existingPool = this.nrfGroups.get(group.getName());
                        final Consumer<NrfGroup> addNewNrfPool = nrfGroup ->
                        {
                            final com.ericsson.sc.nrf.r17.Nrf.Pool newPool = new com.ericsson.sc.nrf.r17.Nrf.Pool(UUID.fromString(nrfGroup.getNfInstanceId()),
                                                                                                                  this.parent.parent.alarmCtx,
                                                                                                                  this.parent.parent.loadMeter,
                                                                                                                  this.parent.parent.secrets,
                                                                                                                  this.parent.parent.nrfExtCertInfo,
                                                                                                                  this.rdn.add("nrf-group",
                                                                                                                               nrfGroup.getName()));

                            this.nrfGroups.put(group.getName(), newPool);
                            this.parent.parent.nrfExtCertInfo.firstElement().ignoreElement().onErrorComplete().andThen(newPool.start()).subscribe(() ->
                            {
                            }, t -> log.error("Error starting NRF-group. Cause: {}", log.isDebugEnabled() ? t : t.toString()));
                        };

                        if (existingPool != null)
                        {
                            if (!existingPool.getNfInstanceId().toString().equals(group.getNfInstanceId()))
                            {
                                existingPool.publish(Optional.<com.ericsson.sc.nrf.r17.Nrf.Pool.Configuration>empty());
                                existingPool.stop()
                                            .subscribe(() -> MetricRegister.singleton().registerForRemoval(existingPool.getRdn()),
                                                       t -> log.error("Error stopping NRF-group. Cause: {}", log.isDebugEnabled() ? t : t.toString()));

                                addNewNrfPool.accept(group);
                            }
                        }
                        else
                        {
                            addNewNrfPool.accept(group);
                        }
                    }
                });
            }

            final Set<String> keysNotInConfig = new HashSet<>();

            this.nrfGroups.entrySet().forEach(entry ->
            {
                if (!configuredKeys.contains(entry.getKey()))
                {
                    keysNotInConfig.add(entry.getKey());
                    final com.ericsson.sc.nrf.r17.Nrf.Pool pool = entry.getValue();
                    pool.publish(Optional.<com.ericsson.sc.nrf.r17.Nrf.Pool.Configuration>empty());
                    pool.stop()
                        .subscribe(() -> MetricRegister.singleton().registerForRemoval(entry.getValue().getRdn()),
                                   t -> log.error("Error stopping NRF-group. Cause: {}", log.isDebugEnabled() ? t : t.toString()));
                }
            });

            this.nrfGroups.keySet().removeIf(keysNotInConfig::contains);
        }
        else
        {
            log.info("Update due to DNS resolution, configuration unchanged.");
        }

        if (instance != null)
        {
            instance.getNrfGroup().forEach(g ->
            {
                final NrfGroup group = (NrfGroup) g;

                if (this.nrfGroups.containsKey(group.getName()))
                {
                    final List<com.ericsson.sc.nrf.r17.Nrf.Configuration> allNrfs = new ArrayList<>();
                    final Map<Integer, com.ericsson.sc.sepp.model.Nrf> priorities = new HashMap<>();

                    final NfManagement nfManagement = instance.getNrfService() != null ? ((NrfService) instance.getNrfService()).getNfManagement() : null;
                    final boolean nrfGroupUsedForNfManagementAndNotEmpty = nfManagement != null && nfManagement.getNrfGroupRef() != null
                                                                           && nfManagement.getNrfGroupRef().contains(group.getName())
                                                                           && !group.getNrf().isEmpty();
                    final boolean nrfGroupHasNfProfile = !group.getNrf().isEmpty() && (group.getNfProfileRef() != null && !group.getNfProfileRef().isEmpty());
                    final boolean allNrfsHaveNfProfile = group.getNrf()
                                                              .stream()
                                                              .allMatch(nrf -> nrf.getNfProfileRef() != null && !nrf.getNfProfileRef().isEmpty());

                    final boolean registrationRequired = nrfGroupUsedForNfManagementAndNotEmpty && (nrfGroupHasNfProfile || allNrfsHaveNfProfile);

                    group.getNrf().forEach(nrf ->
                    {
                        final Integer priority = nrf.getPriority();

                        if (priority == null)
                            throw new BadConfigurationException("The priority of NRF '{}' has not been defined.", nrf.getName());

                        if (priority < 0 || priority > 99)
                            throw new BadConfigurationException("The priority '{}' of NRF '{}' must be in the range '0 <= priority < 100'.",
                                                                nrf.getPriority(),
                                                                nrf.getName());

                        if (priorities.containsKey(priority))
                            throw new BadConfigurationException("The priority '{}' of NRF '{}' is already used by NRF '{}'.",
                                                                nrf.getPriority(),
                                                                nrf.getName(),
                                                                priorities.get(priority).getName());

                        priorities.put(priority, nrf);

                        // Catch the case that a configuration change has removed the NF-instance-IDs.
                        // If that has happened, just take the one from the NRF-group and set it in the
                        // model which will be pushed to CMM later.
                        if (group.getNfInstanceId() == null)
                            group.setNfInstanceId(this.nrfGroups.get(group.getName()).getNfInstanceId().toString());

                        final com.ericsson.sc.nrf.r17.Nrf.Configuration convertedNrf = this.convert(registrationRequired,
                                                                                                    nrf,
                                                                                                    group,
                                                                                                    instance,
                                                                                                    this.nrfGroups.get(group.getName()));

                        if (convertedNrf != null)
                            allNrfs.add(convertedNrf);
                    });

                    final com.ericsson.sc.nrf.r17.Nrf.Pool nrfGroup = this.nrfGroups.get(group.getName());
                    nrfGroup.publish(Optional.of(new com.ericsson.sc.nrf.r17.Nrf.Pool.Configuration(registrationRequired, userRelated || dnsRelated, allNrfs)));
                }
            });

            final NfDiscovery nfDiscovery = instance.getNrfService() != null ? ((NrfService) instance.getNrfService()).getNfDiscovery() : null;

            if (nfDiscovery != null && nfDiscovery.getNrfGroupRef() != null)
            {
                this.nrfGroupForNfDiscovery = this.nrfGroups.get(nfDiscovery.getNrfGroupRef());

                if (this.nrfGroupForNfDiscovery == null)
                {
                    throw new BadConfigurationException("The nrf-group '{}' referenced by nrf-service/nf-discovery has not been configured.",
                                                        nfDiscovery.getNrfGroupRef());
                }
            }
            else
            {
                this.nrfGroupForNfDiscovery = null;
            }
        }
    }

    private com.ericsson.sc.nrf.r17.Nrf.Configuration convert(final boolean registrationRequired,
                                                              final com.ericsson.sc.sepp.model.Nrf nrf,
                                                              final NrfGroup group,
                                                              final com.ericsson.sc.sepp.model.NfInstance instance,
                                                              final com.ericsson.sc.nrf.r17.Nrf.Pool nrfGroup)
    {
        NfProfile nfProfileBase = null;
        NfProfile nfProfileOverride = null;

        {
            // Consistency check.

            if (nrf.getScheme() == null)
                nrf.setScheme(Scheme.HTTP); // Default is HTTP.

            if (nrf.getScheme() == Scheme.HTTPS && nrf.getFqdn() == null)
                throw new BadConfigurationException("The fqdn of NRF '{}' must be configured if scheme is HTTPS.", nrf.getName());

            if (group.getNfProfileRef() != null && (nfProfileBase = Utils.getByName(instance.getNfProfile(), group.getNfProfileRef())) == null)
                throw new BadConfigurationException("The nf-profile '{}' referenced by the nrf-group '{}' has not been configured.",
                                                    group.getNfProfileRef(),
                                                    group.getName());

            if (nrf.getNfProfileRef() != null && (nfProfileOverride = Utils.getByName(instance.getNfProfile(), nrf.getNfProfileRef())) == null)
                throw new BadConfigurationException("The nf-profile '{}' referenced by nrf '{}' has not been configured.",
                                                    nrf.getNfProfileRef(),
                                                    nrf.getName());

            if (registrationRequired && nfProfileBase == null && nfProfileOverride == null)
                throw new BadConfigurationException("An nf-profile must be referenced by the nrf-group '{}', or by the nrf '{}', or by both.",
                                                    group.getName(),
                                                    nrf.getName());
        }

        NFProfile registrationProfile = null;

        if (registrationRequired)
        {
            final NfProfile nfProfile = merge(nfProfileBase, nfProfileOverride);

            final Map<String, List<ServiceAddress>> serviceAddrs = new HashMap<>();

            ServiceAddress serviceAddr = null;

            {
                // Consistency check.

                if (nfProfile.getServiceAddressRef() != null
                    && (serviceAddr = Utils.getByName(instance.getServiceAddress(), nfProfile.getServiceAddressRef())) == null)
                    throw new BadConfigurationException("The service-address '{}' referenced by nf-profile '{}' has not been configured.",
                                                        nfProfile.getServiceAddressRef(),
                                                        nfProfile.getName());

                if (serviceAddr == null)
                    throw new BadConfigurationException("A service-address must be referenced by the nf-profile '{}'.", nfProfile.getName());
            }

            serviceAddrs.put(nfProfile.getName(), Arrays.asList(serviceAddr));

            for (NfService service : nfProfile.getNfService())
            {
                {
                    // Consistency check.

                    final String key = nfProfile.getName() + "," + service.getServiceInstanceId();

                    serviceAddrs.put(key, new ArrayList<>()); // Always add empty list as default.

                    for (final String serviceAddrRef : service.getServiceAddressRef())
                    {
                        if ((serviceAddr = Utils.getByName(instance.getServiceAddress(), serviceAddrRef)) == null)
                            throw new BadConfigurationException("The service-address '{}' referenced by the nf-service with instance-id '{}' in the nf-profile '{}' has not been configured.",
                                                                serviceAddrRef,
                                                                service.getServiceInstanceId(),
                                                                nfProfile.getName());

                        serviceAddrs.get(key).add(serviceAddr);
                    }
                }
            }

            registrationProfile = this.profileBuilder.build(instance, nrfGroup.getNfInstanceId(), nfProfile, serviceAddrs);
        }

        final List<Url> urls = new ArrayList<>();

        nrf.getIpEndpoint().forEach(endpoint ->
        {
            if (endpoint.getIpv4Address() != null)
                urls.add(new Url(nrf.getScheme().value(), nrf.getFqdn(), endpoint.getPort(), "", endpoint.getIpv4Address()));

            if (endpoint.getIpv6Address() != null)
                urls.add(new Url(nrf.getScheme().value(), nrf.getFqdn(), endpoint.getPort(), "", endpoint.getIpv6Address()));

            if (urls.isEmpty())
            {
                final Set<IpFamily> ipFamilies = new HashSet<>();

                if (group.getDnsProfileRef() != null)
                {
                    final DnsProfile dnsProfile = Utils.getByName(instance.getDnsProfile(), group.getDnsProfileRef());

                    if (dnsProfile != null)
                        dnsProfile.getIpFamilyResolution().forEach(r -> ipFamilies.add(com.ericsson.sc.utilities.dns.IpFamily.fromValue(r.value())));
                }

                if (ipFamilies.isEmpty())
                    ipFamilies.addAll(CommonConfigUtils.getDefaultIpFamilies(instance));

                final Map<IpFamily, Optional<String>> ipsFromDnsCache = DnsCache.getInstance().toIp(nrf.getFqdn());

                ipFamilies.forEach(ipFamily -> ipsFromDnsCache.get(ipFamily)//
                                                              .ifPresent(ip ->
                                                              {
                                                                  log.info("Using resolved IP '{}' for FQDN '{}'", ip, nrf.getFqdn());
                                                                  urls.add(new Url(nrf.getScheme().value(), nrf.getFqdn(), endpoint.getPort(), "", ip));
                                                              }));
            }
        });

        return new com.ericsson.sc.nrf.r17.Nrf.Configuration(nrfGroup.getRdn().add("nrf", nrf.getName()),
                                                             nrf.getPriority(),
                                                             nrf.getMaxRetries(),
                                                             nrf.getRetryTimeout(),
                                                             urls,
                                                             registrationProfile,
                                                             getSrcSbiNfPeerInfo(instance, group, nrf),
                                                             "SEPP");
    }
}