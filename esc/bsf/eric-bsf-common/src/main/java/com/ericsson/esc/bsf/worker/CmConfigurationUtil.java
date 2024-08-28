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
 * Created on: May 05, 2020
 *     Author: eevagal
 */

package com.ericsson.esc.bsf.worker;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.ericsson.sc.bsf.model.BindingDatabase;
import com.ericsson.sc.bsf.model.BsfService;
import com.ericsson.sc.bsf.model.DiameterLookup;
import com.ericsson.sc.bsf.model.DiameterLookup.ResolutionType;
import com.ericsson.sc.bsf.model.DiameterRouting;
import com.ericsson.sc.bsf.model.EricssonBsf;
import com.ericsson.sc.bsf.model.HttpLookup;
import com.ericsson.sc.bsf.model.IngressConnectionProfile;
import com.ericsson.sc.bsf.model.LookupProfile;
import com.ericsson.sc.bsf.model.MultipleBindingResolution;
import com.ericsson.sc.bsf.model.NfInstance;
import com.ericsson.sc.bsf.model.NfPoolDiscovery;
import com.ericsson.sc.bsf.model.Nrf;
import com.ericsson.sc.bsf.model.NrfGroup;
import com.ericsson.sc.bsf.model.PcfRecoveryTime;
import com.ericsson.sc.bsf.model.Route.RouteType;
import com.ericsson.sc.bsf.model.ServiceAddress;
import com.ericsson.sc.bsf.model.StaticDestinationProfile;
import com.ericsson.sc.nfm.model.AllowedPlmn;
import com.ericsson.sc.nfm.model.NfProfile;
import com.ericsson.sc.nfm.model.Oauth2KeyProfile;
import com.ericsson.sc.nfm.model.Plmn;
import com.ericsson.sc.nfm.model.Snssai1;

public final class CmConfigurationUtil
{

    private CmConfigurationUtil()
    {

    }

    /**
     * Extract NfInstance from BSF configuration
     *
     * @param bsfCfg A non-null BSF configuration object
     * @return The NfInstance, if exists
     */
    public static Optional<NfInstance> getNfInstance(EricssonBsf bsfCfg)
    {
        return Optional.ofNullable(bsfCfg.getEricssonBsfBsfFunction())
                       .flatMap(bsfFunction -> Optional.ofNullable(bsfFunction.getNfInstance()))
                       .filter(nfInstance -> nfInstance.size() == 1)
                       .map(nfInstance -> nfInstance.get(0));
    }

    /**
     * Extract BsfService from BSF configuration
     *
     * @param bsfCfg an Optional BSF configuration object
     * @return The BsfService, if exists
     */
    public static Optional<BsfService> getBsfService(Optional<EricssonBsf> bsfCfg)
    {
        return bsfCfg.flatMap(CmConfigurationUtil::getNfInstance)
                     .flatMap(nfInstance -> Optional.ofNullable(nfInstance.getBsfService()))
                     .filter(bsfService -> bsfService.size() == 1)
                     .map(bsfService -> bsfService.get(0));
    }

    /**
     * Extract serviceAddress from BSF configuration
     *
     * @param bsfCfg an Optional BSF configuration object
     * @return The serviceAddress, if exists
     */
    public static Optional<ServiceAddress> getServiceAddr(Optional<EricssonBsf> bsfCfg)
    {
        return bsfCfg.flatMap(CmConfigurationUtil::getNfInstance)
                     .flatMap(nfInstance -> Optional.ofNullable(nfInstance.getServiceAddress()))
                     .filter(serviceAddr -> serviceAddr.size() == 1)
                     .map(serviceAddr -> serviceAddr.get(0));
    }

    public static List<String> getNfManagementNrfGroup(Optional<EricssonBsf> bsfCfg)
    {
        var nrfGroupRef = bsfCfg.flatMap(CmConfigurationUtil::getNfInstance)
                                .flatMap(nfInstance -> Optional.ofNullable(nfInstance.getNrfService()))
                                .flatMap(nrfService -> Optional.ofNullable(nrfService.getNfManagement()))
                                .flatMap(nfMng -> Optional.ofNullable(nfMng.getNrfGroupRef()));

        return nrfGroupRef.orElse(Collections.<String>emptyList());
    }

    public static List<NrfGroup> getNrfGroups(Optional<EricssonBsf> bsfCfg)
    {
        var nrfGroup = bsfCfg.flatMap(CmConfigurationUtil::getNfInstance).map(NfInstance::getNrfGroup);

        return nrfGroup.orElse(Collections.<NrfGroup>emptyList());
    }

    public static List<Nrf> getNrfs(Optional<EricssonBsf> bsfCfg)
    {
        return CmConfigurationUtil.getNrfGroups(bsfCfg).stream().flatMap(nrfGroup -> nrfGroup.getNrf().stream()).toList();
    }

    public static List<NfProfile> getNfProfile(Optional<EricssonBsf> bsfCfg)
    {
        var nfProfile = bsfCfg.flatMap(CmConfigurationUtil::getNfInstance).map(NfInstance::getNfProfile);

        return nfProfile.orElse(Collections.<NfProfile>emptyList());
    }

    public static List<Oauth2KeyProfile> getKeyProfileList(Optional<EricssonBsf> bsfCfg)
    {
        return bsfCfg.flatMap(CmConfigurationUtil::getNfInstance).map(NfInstance::getOauth2KeyProfile).orElse(Collections.<Oauth2KeyProfile>emptyList());
    }

    public static List<String> getNfSetId(List<NfProfile> nfProfiles)
    {
        return nfProfiles.stream().flatMap(nfProfile -> nfProfile.getNfSetId().stream()).toList();
    }

    public static List<Plmn> getPlmn(List<NfProfile> nfProfiles)
    {
        return nfProfiles.stream().flatMap(nfProfile -> nfProfile.getPlmn().stream()).toList();
    }

    public static List<Snssai1> getSnssai1(List<NfProfile> nfProfiles)
    {
        return nfProfiles.stream().flatMap(nfProfile -> nfProfile.getSnssai1().stream()).toList();
    }

    public static List<String> getNsi(List<NfProfile> nfProfiles)
    {
        return nfProfiles.stream().flatMap(nfProfile -> nfProfile.getNsi().stream()).toList();
    }

    public static List<AllowedPlmn> getAllowedPlmn(List<NfProfile> nfProfiles)
    {
        final var nfServiceAllowedplmn = getAllowedPlmnNfservice(nfProfiles);

        // If the allowed plmn in nfService level is null then we should check the
        // allowed plmn in nfProfile level. The allowed plmn in nfService level
        // overrides the one in nfProfile.
        return nfServiceAllowedplmn.isEmpty() ? nfProfiles.stream().flatMap(nfProfile -> nfProfile.getAllowedPlmn().stream()).toList() : nfServiceAllowedplmn;
    }

    public static List<AllowedPlmn> getAllowedPlmnNfservice(List<NfProfile> nfProfiles)
    {
        return nfProfiles.stream().flatMap(nfProf -> nfProf.getNfService().stream()).flatMap(r -> r.getAllowedPlmn().stream()).toList();

    }

    /**
     * Get Binding-Timeout value from BSF configuration
     *
     * @param bsfCfg a non null BSF configuration object
     * @return An empty {@link Optional} if BSF configuration is incomplete, an
     *         Optional with the configured timeout in hours, or an Optional with
     *         null value if there is no configured timeout
     * 
     */

    public static Optional<Integer> getBindingTimeout(NfInstance nfInstance)

    {
        Objects.requireNonNull(nfInstance);

        return Optional.ofNullable(nfInstance.getBsfService())
                       .filter(bsfService -> bsfService.size() == 1)
                       .map(bsfService -> bsfService.get(0))
                       .map(BsfService::getBindingDatabase)
                       .map(BindingDatabase::getBindingTimeout);
    }

    public static boolean isTlsEnabled(NfInstance nfInstance)
    {
        return nfInstance.getServiceAddress().stream().anyMatch(srv -> srv.getTlsPort() != null);
    }

    public static boolean isHttpEnabled(NfInstance nfInstance)
    {
        return nfInstance.getServiceAddress().stream().anyMatch(srv -> srv.getPort() != null);
    }

    public static boolean isVtapEnabled(NfInstance nfInstance)
    {
        return Objects.nonNull(nfInstance.getVtap()) && Boolean.TRUE.equals(nfInstance.getVtap().getEnabled());
    }

    /**
     * Get DiameterRouting Container from BSF configuration
     *
     * @param The NfInstance, if exists
     * @return The DiameterRouting, if exists
     * 
     */

    public static Optional<DiameterRouting> getDiameterRouting(NfInstance nfInstance)
    {
        Objects.requireNonNull(nfInstance);

        return Optional.ofNullable(nfInstance.getBsfService())
                       .filter(bsfService -> bsfService.size() == 1)
                       .map(bsfService -> bsfService.get(0))
                       .map(BsfService::getDiameterRouting);
    }

    /**
     * 
     * @param bsfService
     * @return The configured HttpLookup or a default value if not configured
     */
    public static HttpLookup getHttpLookup(BsfService bsfService)
    {
        Objects.requireNonNull(bsfService);

        final var hl = getMultipleBindingResolution(bsfService).getHttpLookup();
        return hl != null ? hl : defaultMultipleBindingResolution().getHttpLookup();
    }

    /**
     * 
     * @param bsfService
     * @return The configured DiameterLookup or a default value if not configured
     */
    public static DiameterLookup getDiameterLookup(BsfService bsfService)
    {
        Objects.requireNonNull(bsfService);

        final var dl = getMultipleBindingResolution(bsfService).getDiameterLookup();
        return dl != null ? dl : defaultMultipleBindingResolution().getDiameterLookup();
    }

    /**
     * 
     * @param bsfService
     * @return The configured MultipleBindingResolution or a default value if not
     *         configured
     */
    public static MultipleBindingResolution getMultipleBindingResolution(BsfService bsfService)
    {
        return bsfService.getMultipleBindingResolution() != null ? bsfService.getMultipleBindingResolution() : defaultMultipleBindingResolution();
    }

    public static PcfRecoveryTime getPcfRecoveryTime(BsfService bsfService)
    {
        return bsfService.getPcfRecoveryTime();
    }

    public static Optional<NfPoolDiscovery> getNfPoolDiscovery(final Optional<EricssonBsf> bsfCfg)
    {
        return getBsfService(bsfCfg).map(BsfService::getNfPool)
                                    .filter(nfPoolList -> !nfPoolList.isEmpty())
                                    .map(nfPoolList -> nfPoolList.get(0).getNfPoolDiscovery())
                                    .filter(nfPoolDiscoveryList -> !nfPoolDiscoveryList.isEmpty())
                                    .map(nfPoolDiscovery -> nfPoolDiscovery.get(0));
    }

    /**
     * 
     * @return The default MultipleBindingResolution, to be used if not configured
     */
    public static MultipleBindingResolution defaultMultipleBindingResolution()
    {

        return new MultipleBindingResolution() //
                                              .withDiameterLookup(new DiameterLookup() //
                                                                                      .withAvpCombination(null)
                                                                                      .withResolutionType(ResolutionType.MOST_RECENT))//
                                              .withHttpLookup(new HttpLookup() //
                                                                              .withQueryParameterCombination(null)
                                                                              .withResolutionType(HttpLookup.ResolutionType.MOST_RECENT));
    }

    /**
     * Get LookupProfile that is referenced in route if this route has also
     * "default" RouteType
     *
     * @param The NfInstance, if exists
     * @return The LookupProfile that matches the criteria, if exists
     * 
     */

    public static Optional<LookupProfile> getLookupProfUsedForDefRoute(DiameterRouting dr)
    {
        Objects.requireNonNull(dr);
        if (dr.getRoute() != null && !dr.getRoute().isEmpty() && RouteType.DEFAULT.equals(dr.getRoute().get(0).getRouteType()))
        {
            return dr.getLookupProfile()
                     .stream()//
                     .filter(profile -> dr.getRoute().get(0).getLookupProfileRef().equals(profile.getName()))
                     .findFirst();
        }
        else
            return Optional.empty();
    }

    /**
     * Get StaticDestinationProfile that is referenced in noBindingCase.
     *
     * @param DimaeterRouting
     * @return The StaticDestinationProfile that matches the criteria or else empty
     *         optional.
     * 
     */
    public static Optional<StaticDestinationProfile> getStaticDestinationProfile(DiameterRouting dr)
    {
        Objects.requireNonNull(dr);
        if (dr.getNoBindingCase() != null && !dr.getNoBindingCase().isEmpty())
        {
            return dr.getStaticDestinationProfile()
                     .stream()//
                     .filter(profile -> dr.getNoBindingCase().get(0).getDefaultStaticDestinationProfileRef().equals(profile.getName()))
                     .findFirst();
        }
        else
            return Optional.empty();
    }

    /**
     * Get StaticDestinationProfile that is referenced in noBindingCase.
     *
     * @param DimaeterRouting
     * @return The StaticDestinationProfile that matches the criteria or else empty
     *         optional.
     * 
     */
    public static List<Integer> getStatusCodeMatchCondition(DiameterRouting dr)
    {
        Objects.requireNonNull(dr);
        if (dr.getPcfReselectionCase() != null && !dr.getPcfReselectionCase().isEmpty())
        {
            return dr.getPcfReselectionCase().stream().flatMap(resel -> resel.getStatusCodeMatchCondition().stream()).toList();
        }
        else
            return List.of();
    }

    /**
     * Get fallBackDestinationRealm of LookupProfile used for Default Route
     *
     * @param The NfInstance, if exists
     * @return The fallBackDestinationRealm if exists or a null String
     * 
     */

    public static String getFallbackDestRealmDefRoute(DiameterRouting dr)
    {
        Objects.requireNonNull(dr);

        return getLookupProfUsedForDefRoute(dr).map(LookupProfile::getFallbackDestinationRealm).orElse(null);
    }

    /**
     * Get fallBackDestinationHost of LookupProfile used for Default Route
     *
     * @param The NfInstance, if exists
     * @return The fallBackDestinationHost if exists or a null String
     * 
     */

    public static String getFallbackDestHostDefRoute(DiameterRouting dr)
    {
        Objects.requireNonNull(dr);

        return getLookupProfUsedForDefRoute(dr).map(LookupProfile::getFallbackDestinationHost).orElse(null);
    }

    public static String getStaticDestinationHost(DiameterRouting dr)
    {
        Objects.requireNonNull(dr);

        return getStaticDestinationProfile(dr).map(StaticDestinationProfile::getDestinationHost).orElse(null);
    }

    public static String getStaticDestinationRealm(DiameterRouting dr)
    {
        Objects.requireNonNull(dr);

        return getStaticDestinationProfile(dr).map(StaticDestinationProfile::getDestinationRealm).orElse(null);
    }

    public static IngressConnectionProfile getIngressConnectionProfile(Optional<EricssonBsf> bsfCfg)
    {
        final var defaultProfile = new IngressConnectionProfile().withName("Profile1").withDscpMarking(0).withHpackTableSize(4096L);
        return bsfCfg.flatMap(CmConfigurationUtil::getNfInstance)
                     .flatMap(nfInstance -> Optional.ofNullable(nfInstance.getIngressConnectionProfileRef())
                                                    .flatMap(refName -> nfInstance.getIngressConnectionProfile()
                                                                                  .stream()
                                                                                  .filter(profile -> profile.getName().equals(refName))
                                                                                  .findAny()))
                     .orElse(defaultProfile);

    }

}
