/**
 * COPYRIGHT ERICSSON GMBH 2020java.enet.ProtocolException: Expected HTTP 101 response but was '400 Bad Request'

 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jan 10, 2020
 *     Author: eaoknkr
 */

package com.ericsson.esc.scp.sds;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.certmcrhandler.data.SecretTlsDataList;
import com.ericsson.sc.sepp.model.EricssonSepp;
import com.ericsson.sc.sepp.model.NfInstance;
import com.ericsson.sc.sepp.model.NfPool;
import com.ericsson.sc.sepp.model.RoamingPartner;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.common.Utils;
import com.ericsson.utilities.file.CertificateWatch.AsymmetricKey;
import com.ericsson.utilities.file.CertificateWatch.CaCert;
import com.ericsson.utilities.file.CertificateWatch.MonitoredCertificates;
import com.google.protobuf.Any;
import com.google.re2j.Pattern;

import io.envoyproxy.envoy.config.core.v3.DataSource;
import io.envoyproxy.envoy.config.core.v3.TypedExtensionConfig;
import io.envoyproxy.envoy.extensions.transport_sockets.tls.v3.CertificateValidationContext;
import io.envoyproxy.envoy.extensions.transport_sockets.tls.v3.SEPPCertValidatorConfig;
import io.envoyproxy.envoy.extensions.transport_sockets.tls.v3.SEPPCertValidatorConfig.TrustStores;
import io.envoyproxy.envoy.extensions.transport_sockets.tls.v3.Secret;
import io.envoyproxy.envoy.extensions.transport_sockets.tls.v3.SubjectAltNameMatcher;
import io.envoyproxy.envoy.extensions.transport_sockets.tls.v3.SubjectAltNameMatcher.SanType;
import io.envoyproxy.envoy.extensions.transport_sockets.tls.v3.TlsCertificate;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryRequest;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;
import io.envoyproxy.envoy.type.matcher.v3.RegexMatcher;
import io.envoyproxy.envoy.type.matcher.v3.StringMatcher;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.subjects.BehaviorSubject;

/**
 * 
 */
public class SeppSecretDiscoveryService extends SecretDiscoveryService
{
    private static final Logger log = LoggerFactory.getLogger(SeppSecretDiscoveryService.class);

    private final BehaviorSubject<Optional<EricssonSepp>> configFlow;
    private final BehaviorSubject<SecretTlsDataList> extSecretWatcherFlow;

    private static final String NAME_CONVENTION = "sepp-extcert";
    private static final String CR_PREFIX = EnvVars.get("GLOBAL_ERIC_SEPP_NAME") + "-sepp-extcert-";

    /**
     *
     * @param certificateWatcher
     * @param configFlow
     */
    public SeppSecretDiscoveryService(Flowable<MonitoredCertificates> certificateWatcher,
                                      BehaviorSubject<SecretTlsDataList> extCertWatcher,
                                      BehaviorSubject<Optional<EricssonSepp>> configFlow)
    {
        super(certificateWatcher);
        this.configFlow = configFlow;
        this.extSecretWatcherFlow = extCertWatcher;
    }

    @Override
    public Flowable<DiscoveryResponse> streamSecrets(Flowable<DiscoveryRequest> request)
    {
        final var streamContext = new SeppStreamContext(this.certificateWatcher, this.extSecretWatcherFlow, this.configFlow, this.secretsVersion);
        log.debug("New stream opened. Total number of active streams = {}", this.activeStreams.incrementAndGet());
        return Flowable.combineLatest(streamContext.getSeppSecretsFlow(),
                                      request,
                                      streamContext.requestsForRetry.toFlowable(BackpressureStrategy.LATEST),
                                      (secret,
                                       req,
                                       rty) ->
                                      {
                                          if (!req.getResourceNamesList().isEmpty())
                                          {
                                              var secretName = req.getResourceNames(0);
                                              log.debug("Processing secret for {}", secretName);
                                              return process(req, secret, streamContext, rty, secretName);
                                          }
                                          return Optional.<DiscoveryResponse>empty();
                                      })
                       .filter(Optional::isPresent)
                       .map(Optional::get)
                       .doOnSubscribe(s -> log.info("Secret Discovery Service: Subscribing."))
                       .doOnNext(resp -> log.debug("{}: Sending response \nVersionInfo:{}\nNonce:{}\n",
                                                   resp.getControlPlane().getIdentifier(),
                                                   resp.getVersionInfo(),
                                                   resp.getNonce()))
                       .doOnCancel(() ->
                       {
                           log.info("Secret Discovery Service: Cancelling.");
                           log.debug("Remaining streams={}", this.activeStreams.decrementAndGet());
                       })
                       .doOnError(e ->
                       {
                           log.error("Secret Discovery Service: Error processing request ", e);
                           log.debug("Remaining streams={}", this.activeStreams.decrementAndGet());
                       })
                       .doOnComplete(() -> log.info("Secret Discovery Service: Completed."));
    }

    @Override
    protected Any buildEnvoySecret(final SecretWrap secret,
                                   final String secretName)
    {
        log.debug("Building response for secret={}", secretName);
        var seppSecret = (SeppSecretWrap) secret;
        if (seppSecret.getCertificateEvent().isAsymmetric(secretName))
        {
            var certificateEvent = seppSecret.getCertificateEvent().getAsymmetricKey(secretName);
            return Any.pack(Secret.newBuilder()
                                  .setName(secretName) //
                                  .setTlsCertificate(TlsCertificate.newBuilder()
                                                                   .setPrivateKey(DataSource.newBuilder().setInlineString(certificateEvent.getKey()).build())
                                                                   .setCertificateChain(DataSource.newBuilder()
                                                                                                  .setInlineString(certificateEvent.getCertificate())
                                                                                                  .build())
                                                                   .build())
                                  .build());
        }
        else if (seppSecret.getCertificateEvent().isCa(secretName))
        {
            var certificateEvent = seppSecret.getCertificateEvent().getCaCert(secretName);
            var seppSecretConfigEvent = seppSecret.getConfigEvent();
            if (seppSecretConfigEvent.isPresent())
            {
                EricssonSepp config = seppSecretConfigEvent.get();
                var allTrustedCaListNamesOfAllRps = collectTrustedCaListNamesForAllRPs(config);
                var trustedCaListNamePerRp = collectTrustedCaListNamePeRp(config);
                List<String> rpName = new ArrayList<>();

                // Find all rps that reference the trusted certificate list with name secretName
                trustedCaListNamePerRp.entrySet().stream().filter(entry -> entry.getValue().equals(secretName)).forEach(entry -> rpName.add(entry.getKey()));

                List<String> rpsWithSanCheck = collectRpsWithSanCheck(config);
                /*
                 * check if SAN checking on egress is enabled in at least one nf-pool that
                 * references an RP and has the secretName as trusted certificatelist then the
                 * SAN checking on egress will be enabled for all nf-pools that reference RPs
                 * which have the same secretName as trusted certificate list TODO: Use the
                 * combined_validation_context logic in CommonTlsContext level to be able to use
                 * the same secret towards clusters wit SAN checking enabled and disabled
                 * respectively
                 */
                boolean atLeastOneIsEnabled = rpsWithSanCheck.stream().anyMatch(rpName::contains);

                // set match_typed_subject_alt_names to enabled SAN checking on egress
                if (allTrustedCaListNamesOfAllRps.contains(secretName) && atLeastOneIsEnabled)
                {
                    var fqdnsOFRP = this.collectFqdnsOfRP(config, rpName);
                    var sansForRP = this.buildSanForRP(fqdnsOFRP);

                    Iterable<SubjectAltNameMatcher> sansForRPIterable = sansForRP;
                    return Any.pack(Secret.newBuilder()
                                          .setName(secretName)
                                          .setValidationContext(CertificateValidationContext.newBuilder()
                                                                                            .setTrustedCa(DataSource.newBuilder()
                                                                                                                    .setInlineString(certificateEvent.getCa())
                                                                                                                    .build())
                                                                                            .addAllMatchTypedSubjectAltNames(sansForRPIterable)
                                                                                            .build())
                                          .build());
                }
                else
                {
                    return Any.pack(Secret.newBuilder()
                                          .setName(secretName)
                                          .setValidationContext(CertificateValidationContext.newBuilder()
                                                                                            .setTrustedCa(DataSource.newBuilder()
                                                                                                                    .setInlineString(certificateEvent.getCa())
                                                                                                                    .build())
                                                                                            .build())
                                          .build());
                }
            }
        }
        else if (secretName.contains("#!_#"))
        {
            var seppSecretConfigEvent = seppSecret.getConfigEvent();
            if (secretName.contains("EXT:") && seppSecretConfigEvent.isPresent())
            {
                var extNwName = secretName.replace("EXT:#!_#", "");
                var casPerRP = collectCertificateAuthoritiesForExtNwPerRP(seppSecretConfigEvent.get(), extNwName);
                log.debug("Building response for listener {} with SAN verification and multiple CAs", extNwName);
                // this is the case for SEPP where we might have one listener for RPs with all
                // of their trusted authorities. For each RP we create a different Trust Store
                // Each trust store contains the trusted certificate authority for the specific
                // rp and
                // all the SubjectAltNameMatchers (one SubjectAltNameMatcher per each RP's
                // domain)
                // in order to know which domain-list to pick from the configuration, the secret
                // request contains the 'EXT' tag
                // along with the external network name that uses that domain-list.

                log.debug("CA names per RP {} of ext-ntw {}", casPerRP, extNwName);
                List<String> cas = casPerRP.values().stream().toList();
                if (cas.stream().noneMatch(name -> seppSecret.getCertificateEvent().isCa(name)))
                {
                    log.debug("All CA secrets for external network {} are empty: {}", extNwName, casPerRP);
                    return Any.pack(Secret.getDefaultInstance()); // certificates missing
                }

                var domainNamesPerRP = this.collectDomainNamesForExtNwPerRP(seppSecretConfigEvent.get(), extNwName);
                var sansPerRP = this.buildSanPerRP(domainNamesPerRP);
                log.debug("Domain names to be set for SAN verification for external-network {}: {}", extNwName, domainNamesPerRP);
                List<TrustStores> trustStores = new ArrayList<>();

                // create one Trustore per RP
                casPerRP.forEach((rpName,
                                  trustedCA) ->
                {
                    var sansForRP = sansPerRP.get(rpName); // get all san matches of RP with "rpName" name
                    Iterable<SubjectAltNameMatcher> sansForRPIterable = sansForRP;
                    var caForRP = casPerRP.get(rpName);  // get ca of RP with "rpName" name
                    trustStores.add(TrustStores.newBuilder()
                                               .setName(rpName)
                                               .addAllMatchers(sansForRPIterable)
                                               .setTrustedCa(DataSource.newBuilder()
                                                                       .setInlineString(getCaContents(caForRP, seppSecret.getCertificateEvent()))
                                                                       .build())
                                               .build());
                });
                Iterable<TrustStores> trustoresIterable = trustStores;
                return Any.pack(Secret.newBuilder()
                                      .setName(secretName)
                                      .setValidationContext(CertificateValidationContext.newBuilder()
                                                                                        .setCustomValidatorConfig(TypedExtensionConfig.newBuilder()
                                                                                                                                      .setName("envoy.tls.cert_validator.sepp")
                                                                                                                                      .setTypedConfig(Any.pack(SEPPCertValidatorConfig.newBuilder()
                                                                                                                                                                                      .addAllTrustStores(trustoresIterable)
                                                                                                                                                                                      .build()))
                                                                                                                                      .build())
                                                                                        .build())
                                      .build());
            }
        }
        log.info("No info found for secret={}", secretName);
        return Any.pack(Secret.getDefaultInstance()); // certificates missing
    }

    private List<String> collectTrustedCaListNamesForAllRPs(EricssonSepp config)
    {
        var seppnfInst = config.getEricssonSeppSeppFunction().getNfInstance().stream().toList();
        return config.getEricssonSeppSeppFunction()
                     .getNfInstance()
                     .stream()
                     .flatMap(nfInstance -> nfInstance.getExternalNetwork().stream())
                     .flatMap(ext -> ext.getRoamingPartner().stream())
                     .map(rp -> this.getTrustedCaList(seppnfInst, rp))
                     .map(Optional::get)
                     .filter(Objects::nonNull)
                     .toList();
    }

    private Optional<String> getTrustedCaList(final List<NfInstance> seppInst,
                                              final RoamingPartner rp)
    {
        AtomicReference<Optional<String>> trustedCaListString = new AtomicReference<>(Optional.empty());
        seppInst.forEach(instance -> Optional.ofNullable(Utils.getByName(instance.getTrustedCertList(), rp.getTrustedCertInListRef()))
                                             .ifPresentOrElse(tcInList -> trustedCaListString.set(Optional.of(CR_PREFIX + tcInList.getTrustedCertListRef()
                                                                                                              + "-ca-certificate")),
                                                              () -> Optional.ofNullable(rp.getTrustedCertificateList())
                                                                            .ifPresent(tcaList -> trustedCaListString.set(Optional.of(tcaList)))));
        return trustedCaListString.get();
    }

    private Map<String, String> collectTrustedCaListNamePeRp(EricssonSepp config)
    {
        var seppnfInst = config.getEricssonSeppSeppFunction().getNfInstance().stream().toList();
        return config.getEricssonSeppSeppFunction()
                     .getNfInstance()
                     .stream()
                     .flatMap(nfInstance -> nfInstance.getExternalNetwork().stream())
                     .flatMap(ext -> ext.getRoamingPartner().stream())
                     .map(rp ->
                     {
                         var tcaList = getTrustedCaList(seppnfInst, rp);
                         if (tcaList.isEmpty())
                             return null;
                         else
                             return new AbstractMap.SimpleEntry<>(rp.getName(), tcaList.get());
                     })
                     .filter(Objects::nonNull)
                     .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    private Map<String, List<String>> collectDomainNamesForExtNwPerRP(EricssonSepp config,
                                                                      String extNwName)
    {
        return config.getEricssonSeppSeppFunction()
                     .getNfInstance()
                     .stream()
                     .flatMap(nfInstance -> nfInstance.getExternalNetwork().stream())
                     .filter(ext -> ext.getName().equals(extNwName))
                     .flatMap(ext -> ext.getRoamingPartner().stream())
                     .map(rp -> new AbstractMap.SimpleEntry<String, List<String>>(rp.getName(), rp.getDomainName()))
                     .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    private Set<String> collectFqdnsOfRP(EricssonSepp config,
                                         List<String> rpName)
    {
        var fqdnsFromStatic = collectStaticNfInstanceFqdnsOfRP(config, rpName);
        var fqdnsFromStaticSepp = collectStaticSeppInstanceFqdnsOfRP(config, rpName);
        var fqdnsFromDiscovered = collectDiscoveredNfInstanceFqdnsOfRP(config, rpName);
        Set<String> allFqdnsOfRp = new HashSet<>();
        allFqdnsOfRp.addAll(fqdnsFromStatic);
        allFqdnsOfRp.addAll(fqdnsFromStaticSepp);
        allFqdnsOfRp.addAll(fqdnsFromDiscovered);
        return allFqdnsOfRp;
    }

    private List<String> collectStaticNfInstanceNamesOfRP(EricssonSepp config,
                                                          List<String> rpName)
    {
        return config.getEricssonSeppSeppFunction()
                     .getNfInstance()
                     .stream()
                     .flatMap(nfInstance -> nfInstance.getNfPool().stream())
                     .filter(nfPool -> nfPool.getRoamingPartnerRef() != null && rpName.contains(nfPool.getRoamingPartnerRef()))
                     .flatMap(nfPool -> nfPool.getNfPoolDiscovery().stream())
                     .flatMap(nfPoolDiscovery -> nfPoolDiscovery.getStaticNfInstanceDataRef().stream())
                     .toList();
    }

    private List<String> collectStaticNfInstanceFqdnsOfRP(EricssonSepp config,
                                                          List<String> rpName)
    {
        return config.getEricssonSeppSeppFunction()
                     .getNfInstance()
                     .stream()
                     .flatMap(nfInstance -> nfInstance.getStaticNfInstanceData().stream())
                     .filter(nfInstanceData -> collectStaticNfInstanceNamesOfRP(config, rpName).contains(nfInstanceData.getName()))
                     .flatMap(nfInstanceData -> nfInstanceData.getStaticNfInstance().stream())
                     .flatMap(staticNfInstance -> staticNfInstance.getStaticNfService().stream())
                     .map(staticNfService -> staticNfService.getAddress().getFqdn())
                     .toList();
    }

    private List<String> collectStaticSeppInstanceNamesOfRP(EricssonSepp config,
                                                            List<String> rpName)
    {
        return config.getEricssonSeppSeppFunction()
                     .getNfInstance()
                     .stream()
                     .flatMap(nfInstance -> nfInstance.getNfPool().stream())
                     .filter(nfPool -> nfPool.getRoamingPartnerRef() != null && rpName.contains(nfPool.getRoamingPartnerRef()))
                     .flatMap(nfPool -> nfPool.getStaticSeppInstanceDataRef().stream())
                     .toList();
    }

    private List<String> collectStaticSeppInstanceFqdnsOfRP(EricssonSepp config,
                                                            List<String> rpName)
    {
        return config.getEricssonSeppSeppFunction()
                     .getNfInstance()
                     .stream()
                     .flatMap(nfInstance -> nfInstance.getStaticSeppInstanceData().stream())
                     .filter(nfInstanceData -> collectStaticSeppInstanceNamesOfRP(config, rpName).contains(nfInstanceData.getName()))
                     .flatMap(nfInstanceData -> nfInstanceData.getStaticSeppInstance().stream())
                     .map(staticSeppInstance -> staticSeppInstance.getAddress().getFqdn())
                     .toList();
    }

    private List<String> collectDiscoveredNfInstanceFqdnsOfRP(EricssonSepp config,
                                                              List<String> rpName)
    {
        return config.getEricssonSeppSeppFunction()
                     .getNfInstance()
                     .stream()
                     .flatMap(nfInstance -> nfInstance.getNfPool().stream())
                     .filter(nfPool -> nfPool.getRoamingPartnerRef() != null && rpName.contains(nfPool.getRoamingPartnerRef()))
                     .flatMap(nfPool -> nfPool.getNfPoolDiscovery().stream())
                     .filter(nfPoolDiscovery -> nfPoolDiscovery.getDiscoveredNfInstance() != null)
                     .flatMap(nfPoolDiscovery -> nfPoolDiscovery.getDiscoveredNfInstance().stream())
                     .filter(discoveredNfINstance -> discoveredNfINstance.getDiscoveredNfService() != null)
                     .flatMap(discoveredNfINstance -> discoveredNfINstance.getDiscoveredNfService().stream())
                     .map(discoveredNfService -> discoveredNfService.getAddress().getFqdn())
                     .toList();
    }

    private List<String> collectRpsWithSanCheck(EricssonSepp config)
    {
        return config.getEricssonSeppSeppFunction()
                     .getNfInstance()
                     .stream()
                     .flatMap(nfInstance -> nfInstance.getNfPool().stream())
                     .filter(nfPool -> nfPool.getCheckSanOnEgress() != null)
                     .map(NfPool::getRoamingPartnerRef)
                     .toList();
    }

    private Map<String, String> collectCertificateAuthoritiesForExtNwPerRP(EricssonSepp config,
                                                                           String extNwName)
    {
        var seppnfInst = config.getEricssonSeppSeppFunction().getNfInstance().stream().toList();
        return config.getEricssonSeppSeppFunction()
                     .getNfInstance()
                     .stream()
                     .flatMap(nfInstance -> nfInstance.getExternalNetwork().stream())
                     .filter(nw -> nw.getName().equals(extNwName))
                     .flatMap(nw -> nw.getRoamingPartner().stream())
                     .map(rp ->
                     {
                         var tcaList = getTrustedCaList(seppnfInst, rp);
                         if (tcaList.isEmpty())
                             return null;
                         else
                             return new AbstractMap.SimpleEntry<>(rp.getName(), tcaList.get());
                     })
                     .filter(Objects::nonNull)
                     .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    private Map<String, List<SubjectAltNameMatcher>> buildSanPerRP(Map<String, List<String>> domainNames)
    {
        Map<String, List<SubjectAltNameMatcher>> result = new HashMap<>();
        domainNames.entrySet().stream().forEach(domainsPerRP ->
        {
            List<SubjectAltNameMatcher> sansForRP = domainsPerRP.getValue().stream().map(dName ->
            {
                if (dName.contains("*"))
                {
                    var escapedName = Pattern.quote(dName);
                    escapedName = escapedName.replace("\\*", "*");
                    var firstItem = escapedName.split("[.]")[0];
                    var replacedValue = firstItem.equals("*") ? escapedName.replace("*", "[^.]+") : escapedName.replace("*", "[^.]*");
                    return SubjectAltNameMatcher.newBuilder()
                                                .setMatcher(StringMatcher.newBuilder().setSafeRegex(RegexMatcher.newBuilder().setRegex(replacedValue)).build())
                                                .setSanType(SanType.DNS)
                                                .build();
                }
                return SubjectAltNameMatcher.newBuilder().setMatcher(StringMatcher.newBuilder().setExact(dName).build()).setSanType(SanType.DNS).build();
            }).toList();
            result.put(domainsPerRP.getKey(), sansForRP);
        });
        return result;
    }

    private List<SubjectAltNameMatcher> buildSanForRP(Set<String> fqdns)
    {
        return fqdns.stream()
                    .map(fqdn -> SubjectAltNameMatcher.newBuilder()
                                                      .setMatcher(StringMatcher.newBuilder().setExact(fqdn).build())
                                                      .setSanType(SanType.DNS)
                                                      .build())
                    .toList();
    }

    private String getCaContents(String file,
                                 MonitoredCertificates mc)
    {
        if (mc.isCa(file))
            return mc.getCaCert(file).getCa();
        return null;
    }

    private class SeppStreamContext extends StreamContext
    {
        private final Flowable<SeppSecretWrap> secretsFlow;

        public SeppStreamContext(final Flowable<MonitoredCertificates> certificateEventsFlow,
                                 final BehaviorSubject<SecretTlsDataList> extCertificateEventsFlow,
                                 final BehaviorSubject<Optional<EricssonSepp>> configFlow,
                                 final Counter secretVersion)
        {
            super(certificateEventsFlow, secretVersion);
            Long offset = secretVersion.offset();
            this.secretsFlow = Flowable.combineLatest(certificateEventsFlow,
                                                      transformToMonitoredCertificates(extCertificateEventsFlow),
                                                      configFlow.toFlowable(BackpressureStrategy.LATEST).distinctUntilChanged(),
                                                      (secretEvent,
                                                       extSecretEvent,
                                                       configEvent) ->
                                                      {
                                                          secretEvent.merge(extSecretEvent);
                                                          return new SeppSecretWrap(secretEvent, configEvent, this.secretVersion.incrementAndGet(), offset);
                                                      });
        }

        public Flowable<SeppSecretWrap> getSeppSecretsFlow()
        {
            return this.secretsFlow;
        }

        /**
         * @param dataList
         * @return
         */
        private MonitoredCertificates filterDataList(SecretTlsDataList dataList)
        {
            var mc = new MonitoredCertificates();
            dataList.getSecrets().entrySet().stream().filter(entry -> entry.getKey().contains(NAME_CONVENTION) && entry.getValue() != null).forEach(secret ->
            {
                if (secret.getValue().getCaCertValue() != null)
                {
                    var base64 = new Base64();
                    String decodedCa = new String(base64.decode(secret.getValue().getCaCertValue().getBytes()));
                    var ca = new CaCert(secret.getKey(), decodedCa);
                    if (ca.hasContents())
                        mc.updateCa(ca);
                    else
                        mc.removeCa(ca);
                }
                else
                {
                    var base64 = new Base64();
                    String decodedKey = new String(base64.decode(secret.getValue().getKeyValue().getBytes()));
                    String decodedCert = new String(base64.decode(secret.getValue().getCertValue().getBytes()));
                    var asymKey = new AsymmetricKey(secret.getKey(), decodedKey, decodedCert);
                    if (asymKey.hasContents())
                        mc.updateAsymKeys(asymKey);
                    else
                        mc.removeAsymKey(asymKey);
                }
            });
            return mc;
        }

        public Flowable<MonitoredCertificates> transformToMonitoredCertificates(BehaviorSubject<SecretTlsDataList> extCertSecretFlow)
        {
            return extCertSecretFlow.toFlowable(BackpressureStrategy.LATEST).map(this::filterDataList);
        }

    }

    private class SeppSecretWrap extends SecretWrap
    {
        protected final Optional<EricssonSepp> configEvent;

        public SeppSecretWrap(MonitoredCertificates o,
                              Optional<EricssonSepp> configEvent,
                              Long version,
                              Long offset)
        {
            super(o, version, offset);
            this.configEvent = configEvent;
        }

        public final Optional<EricssonSepp> getConfigEvent()
        {
            return this.configEvent;
        }
    }
}