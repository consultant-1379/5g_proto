/**
 * COPYRIGHT ERICSSON GMBH 2023
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Mar 28, 2023
 *     Author: zgraioa
 */

package com.ericsson.esc.bsf.worker;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.bsf.model.EricssonBsf;
import com.ericsson.sc.bsf.model.IngressConnectionProfile;
import com.ericsson.sc.bsf.model.NfPeerInfo;
import com.ericsson.sc.bsf.model.Nrf;
import com.ericsson.sc.bsf.model.ServiceAddress;
import com.ericsson.sc.nfm.model.NfService;
import com.ericsson.sc.nfm.model.Scheme;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;

/**
 * 
 */
public class BsfCfgController
{
    private static final Logger log = LoggerFactory.getLogger(BsfCfgController.class);

    Observable<Optional<BsfCmConfig>> extractBsfConfigFromEricBsf(Observable<Optional<EricssonBsf>> configFlow,
                                                                  int bindingTimeout)
    {
        return configFlow.map(cfg -> cmConfigToBsfConfig(cfg, bindingTimeout))
                         .distinctUntilChanged() // Only emit changes
                         .doOnNext(cfg -> log.info("BSF CM configuration changed:  {}", cfg))
                         .replay(1)
                         .refCount();
    }

    Flowable<Optional<ServiceAddress>> extractSrvAddrConfigFromEricBsf(Observable<Optional<EricssonBsf>> configFlow)
    {
        return configFlow.map(CmConfigurationUtil::getServiceAddr)
                         .distinctUntilChanged() // Only emit changes
                         .toFlowable(BackpressureStrategy.LATEST);
    }

    Flowable<IngressConnectionProfile> extractIngressConnectionProfileFromEricBsf(Observable<Optional<EricssonBsf>> configFlow)
    {
        return configFlow.map(CmConfigurationUtil::getIngressConnectionProfile)
                         .distinctUntilChanged() // Only emit changes
                         .toFlowable(BackpressureStrategy.LATEST);

    }

    Flowable<Boolean> extractVtapConfigFromEricBsf(Observable<Optional<EricssonBsf>> configFlow)
    {
        return configFlow.map(cfg -> cfg.flatMap(CmConfigurationUtil::getNfInstance)//
                                        .map(CmConfigurationUtil::isVtapEnabled))
                         .filter(Optional::isPresent)
                         .map(Optional::get)
                         .distinctUntilChanged() // Only emit changes
                         .toFlowable(BackpressureStrategy.LATEST);
    }

    Flowable<MultipleBindingResolver> extractMultipleBindingResolutionHttpLookUpFromEricBsf(Observable<Optional<EricssonBsf>> configFlow)
    {
        return configFlow.map(cfg -> CmConfigurationUtil.getBsfService(cfg) //
                                                        .map(CmConfigurationUtil::getHttpLookup))
                         .filter(Optional::isPresent)
                         .map(Optional::get)
                         .distinctUntilChanged() // Only emit changes
                         .map(httpLookUp -> new MultipleBindingResolver(httpLookUp))
                         .toFlowable(BackpressureStrategy.LATEST);
    }

    Flowable<RecoveryTimeConfig> extractRtConfigFromEricBsf(Observable<Optional<EricssonBsf>> configFlow)
    {
        return configFlow.map(cfg -> CmConfigurationUtil.getBsfService(cfg)
                                                        .map(CmConfigurationUtil::getPcfRecoveryTime)
                                                        .orElse(DefaultConfigItems.generateDefaultPcfRt()))
                         .distinctUntilChanged() // Only emit changes
                         .map(rs -> new RecoveryTimeConfig(rs.getCheckUponLookup(), rs.getBindingDatabaseScan()))
                         .toFlowable(BackpressureStrategy.LATEST);
    }

    /**
     * Enable oAuth when it is received on an end-point that exists in an
     * nf-service, registered towards an NRF-group, where oAuthrequired is true
     * 
     * @param configFlow The flow of the configuration
     * @param tls        Tls or non-Tls endpoint
     * @return oAuthEnabled
     */
    boolean isOAuthEnabled(Optional<EricssonBsf> config,
                           boolean tls)
    {

        return config.map(cfg ->
        {
            final var nrfGroupRef = CmConfigurationUtil.getNfManagementNrfGroup(Optional.of(cfg));

            final var nfProfileRef = CmConfigurationUtil.getNrfGroups(Optional.of(cfg))

                                                        .stream()

                                                        .filter(group -> nrfGroupRef.contains(group.getName()))
                                                        .flatMap(nrfGroup ->
                                                        {
                                                            final var nrfGroupNfProfileRef = nrfGroup.getNfProfileRef() != null ? List.of(nrfGroup.getNfProfileRef())
                                                                                                                                : List.of();

                                                            final var nfProfileRefFromNrfs = nrfGroup.getNrf()
                                                                                                     .stream()
                                                                                                     .filter(nrf -> nrf.getNfProfileRef() != null)
                                                                                                     .map(Nrf::getNfProfileRef)
                                                                                                     .toList();
                                                            return Objects.isNull(nfProfileRefFromNrfs)
                                                                   || nfProfileRefFromNrfs.isEmpty() ? nrfGroupNfProfileRef.stream()
                                                                                                     : nfProfileRefFromNrfs.stream();
                                                        })
                                                        .toList();

            final var nfProfiles = CmConfigurationUtil.getNfProfile(Optional.of(cfg)).stream().filter(prof -> nfProfileRef.contains(prof.getName())).toList();

            final var srvAddress = CmConfigurationUtil.getServiceAddr(Optional.of(cfg));

            return srvAddress.isPresent()
                   && nfProfiles.stream().anyMatch(prof -> hasOAuthConfig(prof.getServiceAddressRef(), srvAddress.get(), prof.getNfService(), tls));
        }).orElse(false);

    }

    /**
     * Transform CM Configuration to BSF configuration
     *
     * @param bsfCfg a BSF configuration object
     * @return The BSF configuration
     */
    public Optional<BsfCmConfig> cmConfigToBsfConfig(Optional<EricssonBsf> bsfCfg,
                                                     int defaultZeroBindingTimeout)
    {

        return bsfCfg.map(CmConfigurationUtil::getNfInstance).orElse(Optional.empty()).map(nfInstance ->
        {
            final var oauthNonTls = isOAuthEnabled(bsfCfg, false);
            final var oauthTls = isOAuthEnabled(bsfCfg, true);
            final var oauth = new Pair<Boolean, Boolean>(oauthNonTls, oauthTls);
            final var timeoutInHours = CmConfigurationUtil.getBindingTimeout(nfInstance);
            final var timeoutSeconds = ((timeoutInHours.isPresent() && timeoutInHours.get() != 0) ? timeoutInHours.get() : defaultZeroBindingTimeout) * 60 * 60;
            final var outMessageHandling = Objects.nonNull(nfInstance.getNfPeerInfo()) //
                                           && nfInstance.getNfPeerInfo().getOutMessageHandling().equals(NfPeerInfo.OutMessageHandling.ON);

            return new BsfCmConfig.Builder().nfInstanceName(nfInstance.getName())
                                            .nfInstanceId(nfInstance.getNfInstanceId())
                                            .oAuth(oauth)
                                            .oAuthkeyProfiles(nfInstance.getOauth2KeyProfile())
                                            .bindingTimeout(timeoutSeconds)
                                            .outMessageHandling(outMessageHandling)
                                            .nrfs(CmConfigurationUtil.getNrfs(bsfCfg))
                                            .nfProfiles(CmConfigurationUtil.getNfProfile(bsfCfg))
                                            .build();

        });

    }

    private boolean hasOAuthConfig(String profileSrvAddressRef,
                                   ServiceAddress srvAddress,
                                   List<NfService> nfServices,
                                   boolean tls)
    {
        final var scheme = tls ? Scheme.HTTPS : Scheme.HTTP;

        return nfServices.stream()
                         .anyMatch(nfSrv -> Boolean.TRUE.equals(nfSrv.getOauth2Required()) && scheme.equals(nfSrv.getScheme())
                                            && serviceAddressPortExist(tls, srvAddress)
                                            && (profileSrvAddressRef != null
                                                || (nfSrv.getServiceAddressRef() != null && !nfSrv.getServiceAddressRef().isEmpty())));

    }

    private boolean serviceAddressPortExist(boolean tls,
                                            ServiceAddress srvAddress)
    {

        return tls ? Objects.nonNull(srvAddress.getTlsPort()) : Objects.nonNull(srvAddress.getPort());
    }

}
