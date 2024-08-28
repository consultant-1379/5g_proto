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
 * Created on: Apr 07, 2020
 *     Author: eedrak
 */
package com.ericsson.sc.proxyal.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.proxyal.proxyconfig.MetadataListValue;
import com.ericsson.sc.proxyal.proxyconfig.MetadataStringValue;
import com.ericsson.sc.proxyal.proxyconfig.ProxyCfg;
import com.ericsson.sc.proxyal.proxyconfig.ProxyMetadataBuilder.MetaDataType;
import com.ericsson.sc.proxyal.proxyconfig.proxyendpointconfig.ProxyEndpoint;
import com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig.ProxySeppFilter;
import com.ericsson.sc.proxyal.proxyconfig.proxylistenerconfig.ProxyListener;
import com.ericsson.sc.proxyal.proxyconfig.proxyroutingconfig.ProxyFqdnHiding;
import com.ericsson.sc.utilities.dns.DnsCache;
import com.ericsson.sc.utilities.dns.IfDnsCache;
import com.ericsson.sc.utilities.dns.IfDnsLookupContext;
import com.ericsson.sc.utilities.dns.IpFamily;
import com.ericsson.sc.utilities.dns.ResolutionResult;
import com.ericsson.utilities.common.AtomicRef;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.vertx.core.impl.ConcurrentHashSet;

public class ProxyCfgMapper
{
    private static final Logger log = LoggerFactory.getLogger(ProxyCfgMapper.class);

    private final IfDnsCache dnsCache;
    private Flowable<Map<String, IfDnsLookupContext>> dnsFlow;

    private Set<IfDnsLookupContext> requestedHosts = new ConcurrentHashSet<>();

    private final AtomicRef<Optional<ProxyCfg>> lastRcvdConfig = new AtomicRef<>(Optional.<ProxyCfg>empty());

    private final AtomicRef<Map<String, IfDnsLookupContext>> currentCache = new AtomicRef<>(new HashMap<>());

    private Optional<ProxyCfg> lastSentCfg = Optional.empty();

    private final Flowable<Optional<ProxyCfg>> origProxyConfigs;
    private Optional<Flowable<Optional<PvtbConfig>>> pvtbConfigs;
    private BehaviorSubject<Optional<ProxyCfg>> mappedProxyConfigs = BehaviorSubject.create();

    private Disposable pxCfgUpdater;
    private Disposable dnsCacheUpdater;

    public ProxyCfgMapper(IfDnsCache dnsCache,
                          Flowable<Optional<ProxyCfg>> origProxyConfigs)
    {
        this.dnsCache = dnsCache;
        this.dnsFlow = dnsCache.getResolvedHosts();
        this.origProxyConfigs = origProxyConfigs;
    }

    public ProxyCfgMapper(IfDnsCache dnsCache,
                          Flowable<Optional<ProxyCfg>> origProxyConfigs,
                          Optional<Flowable<Optional<PvtbConfig>>> pvtbConfigs)
    {
        this(dnsCache, origProxyConfigs);
        this.pvtbConfigs = pvtbConfigs;
    }

    public Completable start()
    {
        return Completable.fromAction(() ->
        {
            log.info("Starting ProxyConfigMapper.");

            if (pxCfgUpdater == null)
            {
                pxCfgUpdater = createPxCfgUpdater();
            }
            if (dnsCacheUpdater == null)
            {
                dnsCacheUpdater = createDnsCacheUpdater();
            }

            log.info("Started ProxyConfigMapper.");
        });
    }

    public Completable stop()
    {
        return Completable.fromAction(() ->
        {
            if (this.pxCfgUpdater != null)
            {
                this.pxCfgUpdater.dispose();
                this.pxCfgUpdater = null;
            }

            if (this.dnsCacheUpdater != null)
            {
                this.dnsCacheUpdater.dispose();
                this.dnsCacheUpdater = null;
            }
        });
    }

    /**
     * If vTap is enabled, combine the proxy configuration with the PVTB
     * configuration. If new proxy configuration is received, modify it according to
     * the latest PVTB configuration. If new PVTB configuration is received, modify
     * the latest proxy config and re-emit it.
     * 
     * @return A ProxyCfg Flowable
     */
    private Flowable<Optional<ProxyCfg>> createPxCfgFlow()
    {
        return this.pvtbConfigs.isPresent() ? Flowable.combineLatest(this.origProxyConfigs,
                                                                     ScramblingKeysRotationService.getInstance().getFlowable().delay(1, TimeUnit.SECONDS),
                                                                     this.pvtbConfigs.get().distinctUntilChanged(),
                                                                     (origCfg,
                                                                      date,
                                                                      pvtbConfig) ->
                                                                     {
                                                                         log.debug("Combining proxy with PVTB config {}", pvtbConfig);
                                                                         if (pvtbConfig.isPresent() && pvtbConfig.get().getIsConfigured().equals(Boolean.FALSE))
                                                                         {
                                                                             origCfg = disableVtapConfiguration(origCfg);
                                                                         }
                                                                         else if (pvtbConfig.isPresent()
                                                                                  && pvtbConfig.get().getIsConfigured().equals(Boolean.TRUE))
                                                                         {
                                                                             origCfg = enableVtapConfiguration(origCfg);
                                                                         }

                                                                         return updateConfigWithDecryptedKeys(origCfg);

                                                                     })
                                            : Flowable.combineLatest(this.origProxyConfigs,
                                                                     ScramblingKeysRotationService.getInstance().getFlowable().delay(1, TimeUnit.SECONDS),
                                                                     (origCfg,
                                                                      date) -> updateConfigWithDecryptedKeys(origCfg));
    }

    /**
     * @param origCfg
     * @return
     */
    private Optional<ProxyCfg> updateConfigWithDecryptedKeys(Optional<ProxyCfg> origCfg)
    {
        if (origCfg.isEmpty())
            return Optional.empty();

        var activeKeysPerRp = ScramblingKeysRotationService.getInstance().getActiveKeys();

        origCfg.get()
               .getListeners()
               .forEach(pxListener -> pxListener.getHttpFilterSet()
                                                .stream()
                                                .filter(ifHttpFilter -> ifHttpFilter.getClass().equals(ProxySeppFilter.class))
                                                .map(ProxySeppFilter.class::cast)
                                                .forEach(seppFilter -> seppFilter.getRoamingPartners()
                                                                                 .stream()
                                                                                 .forEach(rp -> activeKeysPerRp.entrySet()
                                                                                                               .stream()
                                                                                                               .filter(entry -> rp.getName()
                                                                                                                                  .contains(entry.getKey()))
                                                                                                               .findFirst()
                                                                                                               .ifPresent(entry -> rp.getTopologyHiding()
                                                                                                                                     .ifPresent(tphProfList -> tphProfList.forEach(tphProf ->
                                                                                                                                     {
                                                                                                                                         if (tphProf.getClass()
                                                                                                                                                    .equals(ProxyFqdnHiding.class))
                                                                                                                                         {
                                                                                                                                             ((ProxyFqdnHiding) tphProf).setActiveKey(entry.getValue());
                                                                                                                                         }
                                                                                                                                     }))))));
        return origCfg;
    }

    private Disposable createPxCfgUpdater()
    {

        return createPxCfgFlow().subscribe(origCfg ->
        {
            try
            {
                log.info("new ProxyCfg received: {}", log.isDebugEnabled() ? origCfg : "<not shown on info level>");
                this.lastRcvdConfig.setIfChanged(origCfg);

                // Initial/deleted configuration
                if (this.lastRcvdConfig.get().get().getClusters().isEmpty())
                {
                    log.info("The ProxyCfg has no clusters, no DNS resolution needed.");
                    this.setReqHostsInDnsCache(Set.of());
                    this.mappedProxyConfigs.onNext(Optional.of(this.lastRcvdConfig.get().get()));
                    return;
                }

                // Update the DnsCache with the changed Set of LookupContexts
                final Set<IfDnsLookupContext> hosts = this.getAllHostsFromConfig(origCfg.get());

                if (!hosts.equals(this.requestedHosts))
                {
                    log.debug("Starting DNS lookups.");
                    this.setReqHostsInDnsCache(hosts);
                }

                if (this.areAllHostsResolved(hosts, this.currentCache.get()))
                {
                    // We have all IPs we need
                    ProxyCfg mappedCfg = this.mapHostNamesInCfg(this.lastRcvdConfig.get().get(), this.currentCache.get());
                    this.lastSentCfg = Optional.of(mappedCfg);
                    log.debug("Sending the mapped configuration.");
                    this.mappedProxyConfigs.onNext(this.lastSentCfg);
                    return;
                }
            }
            catch (Exception e)
            {
                log.error("Failed to process ProxyCfg configuration change", e);
                // Ignore unexpected errors, so that processing is not permanently terminated
            }
        }, err -> log.error("ProxyCfgMapper ProxyCfg change processing terminated unexpectedly", err));
    }

    private Disposable createDnsCacheUpdater()
    {
        return this.dnsFlow.observeOn(Schedulers.io()).subscribe(dnsCfg ->
        {
            try
            {
                log.info("new dnsCache received: {}", log.isDebugEnabled() ? dnsCfg : "<not shown on info level>");
                this.currentCache.setIfChanged(dnsCfg);

                if (!this.currentCache.get().isEmpty())
                {
                    log.debug("The DnsCache is not empty. Mapping hostnames to IP adresses.");
                    ProxyCfg mappedCfg = mapHostNamesInCfg(this.lastRcvdConfig.get().get(), this.currentCache.get());

                    if (this.lastSentCfg.isEmpty())
                    {
                        log.debug("Sending the mapped configuration (last sent was empty).");
                        this.lastSentCfg = Optional.of(mappedCfg);
                        this.mappedProxyConfigs.onNext(lastSentCfg);
                        return;
                    }

                    if (mappedCfg.equals(this.lastSentCfg.get()))
                    {
                        log.debug("This configuration was already sent. Waiting for DNS or config updates.");
                        return;
                    }
                    else
                    {
                        log.debug("Sending the mapped configuration.");
                        this.lastSentCfg = Optional.of(mappedCfg);
                        this.mappedProxyConfigs.onNext(lastSentCfg);
                        return;
                    }
                }
            }
            catch (Exception e)
            {
                log.error("Failed to process dnsCache change", e);
                // Ignore unexpected errors, so that processing is not permanently terminated
            }
        }, err -> log.error("ProxyCfgMapper dnsCache change processing terminated unexpectedly", err));
    }

    /**
     * @return the mappedProxyConfigs
     */
    public Flowable<Optional<ProxyCfg>> getMappedProxyConfigs()
    {
        return mappedProxyConfigs.toFlowable(BackpressureStrategy.LATEST);
    }

    private void setReqHostsInDnsCache(Set<IfDnsLookupContext> set)
    {
        this.requestedHosts = set;
        dnsCache.publishHostsToResolve(set);
    }

    private Set<IfDnsLookupContext> getAllHostsFromConfig(ProxyCfg pxCfg)
    {
        return pxCfg.getClusters()
                    .stream()
                    .flatMap(cluster -> cluster.getEndpoints()//
                                               .stream()
                                               .map(ep -> DnsCache.LookupContext.of(ep.getAddress(),
                                                                                    cluster.getClusterIpFamilies().isEmpty() ? pxCfg.getDefaultIpFamilies()
                                                                                                                             : cluster.getClusterIpFamilies())))
                    .collect(Collectors.toSet());
    }

    private boolean areAllHostsResolved(final Set<IfDnsLookupContext> hosts,
                                        final Map<String, IfDnsLookupContext> dnscfg)
    {
        if (hosts.isEmpty())
            return true;

        if (dnscfg.isEmpty())
            return false;

        for (IfDnsLookupContext ctx : hosts)
        {
            if (!dnscfg.containsKey(ctx.getHost()))
                return false;
        }

        return true;
    }

    private ProxyCfg mapHostNamesInCfg(final ProxyCfg pxCfgIn,
                                       final Map<String, IfDnsLookupContext> dnsCache)
    {
        log.debug("pxCfgIn={}", pxCfgIn);

        // Set the IP address for each endpoint based on Cache content.

        final ProxyCfg pxCfgOut = new ProxyCfg(pxCfgIn);

        pxCfgOut.getClusters().forEach(cluster ->
        {
            final List<ProxyEndpoint> ipv6Endpoints = new ArrayList<>();
            final Set<IpFamily> ipFamilies = cluster.getClusterIpFamilies().isEmpty() ? pxCfgIn.getDefaultIpFamilies() : cluster.getClusterIpFamilies();

            log.debug("ipFamilies={}", ipFamilies);

            cluster.getEndpoints().forEach(ep ->
            {
                final String hostName = ep.getAddress();

                log.debug("Before: ep={}", ep);

                final IfDnsLookupContext lookupContext = dnsCache.get(hostName);

                if (lookupContext != null)
                {
                    log.debug("lookupContext[{}]={}", hostName, lookupContext);

                    if (lookupContext.isNumericHost())
                    {
                        // Numeric hosts always belong to one single IP family. Therefore, a
                        // LookupContext with a numeric host will only contain one single
                        // ResolutionResult for that numeric host.

                        ep.setIpAddress(lookupContext.getIpAddrs().values().stream().findFirst().flatMap(ResolutionResult::toOptional));
                    }
                    else
                    {
                        switch (ipFamilies.size())
                        {
                            case 1: // Single stack
                            {
                                // In the current endpoint, set the ipv4 or ipv6 address from the DNS cache.
                                ipFamilies.forEach(ipFamily -> ep.setIpAddress(Optional.ofNullable(lookupContext.getIpAddr(ipFamily))
                                                                                       .flatMap(ResolutionResult::toOptional)));
                                break;
                            }

                            case 2: // Dual stack
                            {
                                // Copy current endpoint and set the ipv6 address from the DNS cache. The new
                                // endpoint is added later to the cluster endpoints.
                                final ProxyEndpoint ipv6Ep = new ProxyEndpoint(ep);
                                ipv6Ep.setIpAddress(Optional.ofNullable(lookupContext.getIpAddr(IpFamily.IPV6)).flatMap(ResolutionResult::toOptional));
                                ipv6Endpoints.add(ipv6Ep);

                                // In the current endpoint, set the ipv4 address from the DNS cache.
                                ep.setIpAddress(Optional.ofNullable(lookupContext.getIpAddr(IpFamily.IPV4)).flatMap(ResolutionResult::toOptional));
                                break;
                            }

                            default:
                                ep.setIpAddress(Optional.empty());
                                log.warn("No IP family, cannot associate IP address with endpoint {}", ep);
                                break;
                        }
                    }
                }
                else
                {
                    log.debug("Host name not in DNS cache: {}", hostName);
                    ep.setIpAddress(Optional.empty());
                }

                log.debug("After : ep={}", ep);
            });

            // Add all ipv6 endpoints in case of dual stack.
            cluster.getEndpoints().addAll(ipv6Endpoints);
        });

        log.debug("pxCfgOut={}", pxCfgOut);

        return pxCfgOut;
    }

    /**
     * Returns a modified copy of proxyCfg where vTap is disabled. The copy is
     * necessary to preserve the original vTap information.
     * 
     * @param proxyCfg
     * @return Modified copy of proxyCfg.
     */
    private Optional<ProxyCfg> disableVtapConfiguration(Optional<ProxyCfg> proxyCfg)
    {
        if (proxyCfg.isEmpty())
        {
            return proxyCfg;
        }
        var copyCfg = new ProxyCfg(proxyCfg.get());
        log.info("PVTB not configured, disabling vTap configuration");
        copyCfg.getListeners()
               .stream()
               .map(ProxyListener::getVtapSettings)
               .forEach(vtapSettings -> vtapSettings.ifPresent(settings -> settings.setVtapEnabled(false)));
        copyCfg.getClusters().stream().forEach(cluster ->
        {
            cluster.setVtapSettings(Optional.empty());
            cluster.getEndpoints().forEach(endpoint ->
            {
                endpoint.setEndpointVtapFlag(false);
                var mdString = new MetadataStringValue("false");
                endpoint.getEndpointMetadata().addMetadata(MetaDataType.TRANSPORT_SOCKET, "vtap_enabled", new MetadataListValue<>(List.of(mdString)));
            });
        });
        return Optional.of(copyCfg);
    }

    /**
     * Returns a modified copy of proxyCfg where the vTap endpoint metadata are
     * configured properly. The copy is necessary to preserve the original vTap
     * information.
     * 
     * @param proxyCfg
     * @return Modified copy of proxyCfg.
     */
    private Optional<ProxyCfg> enableVtapConfiguration(Optional<ProxyCfg> proxyCfg)
    {
        if (proxyCfg.isEmpty())
        {
            return proxyCfg;
        }
        var copyCfg = new ProxyCfg(proxyCfg.get());

        copyCfg.getClusters().forEach(cluster ->
        {
            if (cluster.getVtapSettings().isPresent() && cluster.getVtapSettings().get().getVtapEnabled())
            {
                log.info("PVTB is configured and vTap enabled on Egress. Set the appropriate endopoint metadata");
                cluster.getEndpoints().forEach(endpoint ->
                {
                    endpoint.setEndpointVtapFlag(true);
                    var mdString = new MetadataStringValue("true");
                    endpoint.getEndpointMetadata().addMetadata(MetaDataType.TRANSPORT_SOCKET, "vtap_enabled", new MetadataListValue<>(List.of(mdString)));
                });
            }
        });
        return Optional.of(copyCfg);
    }
}
