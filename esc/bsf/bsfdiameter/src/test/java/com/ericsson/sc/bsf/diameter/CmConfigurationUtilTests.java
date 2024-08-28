package com.ericsson.sc.bsf.diameter;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.ericsson.esc.bsf.worker.CmConfigurationUtil;
import com.ericsson.sc.bsf.model.AvpCombination;
import com.ericsson.sc.bsf.model.BsfService;
import com.ericsson.sc.bsf.model.Combination;
import com.ericsson.sc.bsf.model.Combination_;
import com.ericsson.sc.bsf.model.DiameterLookup;
import com.ericsson.sc.bsf.model.DiameterLookup.ResolutionType;
import com.ericsson.sc.bsf.model.DiameterRouting;
import com.ericsson.sc.bsf.model.HttpLookup;
import com.ericsson.sc.bsf.model.LookupProfile;
import com.ericsson.sc.bsf.model.MultipleBindingResolution;
import com.ericsson.sc.bsf.model.NfInstance;
import com.ericsson.sc.bsf.model.QueryParameterCombination;
import com.ericsson.sc.bsf.model.Route;
import com.ericsson.sc.bsf.model.Route.RouteType;
import com.ericsson.sc.bsf.model.ServiceAddress;

public class CmConfigurationUtilTests
{
    /*
     * Generates a simple nfInstance with one serviceAddress and one bsfService.
     * DiameterRouting in bsfService comes from the input of this method and can be
     * null if it is desired.
     * 
     */
    private NfInstance generateNfInstance(DiameterRouting dr)
    {

        ServiceAddress serviceAddress = new ServiceAddress().withName("west1SA")//
                                                            .withFqdn("bsf.ericsson.se")//
                                                            .withPort(31315);

        List<ServiceAddress> serviceAddresses = new ArrayList<>();
        serviceAddresses.add(serviceAddress);

        BsfService bsfService = new BsfService().withName("serviceName1").withBindingTimeout(720);

        if (dr != null)
        {
            bsfService.setDiameterRouting(dr);
        }

        List<BsfService> bsfServices = new ArrayList<>();
        bsfServices.add(bsfService);

        return new NfInstance().withName("bsf1")//
                               .withBsfService(bsfServices)//
                               .withServiceAddress(serviceAddresses);

    }

    /**
     * Generates a DiameterRouting object. This object contains one lookupProfile
     * and one Route (with lookup-profile-reference) Or one lookupProfile without
     * Route.
     * 
     */
    private DiameterRouting generateDiameterRouting(String host,
                                                    String realm,
                                                    boolean withRoute)
    {
        LookupProfile lookupProfile = new LookupProfile().withName("DSC");

        if (host != null)
        {
            lookupProfile.setFallbackDestinationHost(host);
        }

        if (realm != null)
        {
            lookupProfile.setFallbackDestinationRealm(realm);
        }

        List<LookupProfile> lookupProfiles = new ArrayList<>();
        lookupProfiles.add(lookupProfile);

        List<Route> routes = new ArrayList<>();

        if (withRoute)
        {
            Route route = new Route().withName("DSC_route")//
                                     .withRouteType(RouteType.DEFAULT)//
                                     .withLookupProfileRef("DSC");
            routes.add(route);
        }

        return new DiameterRouting().withLookupProfile(lookupProfiles).withRoute(routes);

    }

    /**
     * Generates a DiameterRouting object. This object can contain more than one
     * lookupProfiles and one Route (with a lookup-profile-reference) This function
     * takes a list of LookupProfiles. One and only one profile should have as name
     * "DSC" which is used as lookup-profile-reference is Route
     * 
     */
    private DiameterRouting generateDiameterRouting(List<LookupProfile> lookupProfiles)
    {

        Route route = new Route().withName("DSC_route")//
                                 .withRouteType(RouteType.DEFAULT)//
                                 .withLookupProfileRef("DSC");

        List<Route> routes = new ArrayList<>();
        routes.add(route);

        return new DiameterRouting().withLookupProfile(lookupProfiles).withRoute(routes);

    }

    /**
     * Creates a lookupProfile with fallbackDestinationHost only.
     * FallbackDestinationRealm is null. This lookupProfile is part on a nfInstace.
     * Then the getFallbackDestinationHost and getFallbackDestinationRealm functions
     * are tested.
     */
    @Test(enabled = true)
    public void onlyFallbackDestinatioHostTest()
    {
        String actualHost = "pcf1";
        String actualRealm = null;

        var diameterRouting = generateDiameterRouting(actualHost, actualRealm, true);
        var nfInstForTest = generateNfInstance(diameterRouting);

        DiameterRouting dr = CmConfigurationUtil.getDiameterRouting(nfInstForTest).orElse(null);

        String host = CmConfigurationUtil.getFallbackDestHostDefRoute(dr);
        String realm = CmConfigurationUtil.getFallbackDestRealmDefRoute(dr);

        assertEquals(host, actualHost, "Invalid host");
        assertEquals(realm, actualRealm, "Invalid Realm");

    }

    /**
     * Creates a lookupProfile with fallbackDestinationRealm only.
     * FallbackDestinationHost is null. This lookupProfile is part on a nfInstace.
     * Then the getFallbackDestinationHost and getFallbackDestinationRealm functions
     * are tested.
     */
    @Test(enabled = true)
    public void onlyFallbackDestinatioRealmTest()
    {
        String actualHost = null;
        String actualRealm = "pcf-diamrealm";

        var diameterRouting = generateDiameterRouting(actualHost, actualRealm, true);
        var nfInstForTest = generateNfInstance(diameterRouting);

        DiameterRouting dr = CmConfigurationUtil.getDiameterRouting(nfInstForTest).orElse(null);

        String host = CmConfigurationUtil.getFallbackDestHostDefRoute(dr);
        String realm = CmConfigurationUtil.getFallbackDestRealmDefRoute(dr);

        assertEquals(host, actualHost, "Invalid host");
        assertEquals(realm, actualRealm, "Invalid Realm");

    }

    /**
     * Creates a lookupProfile with fallbackDestinationRealm and
     * FallbackDestinationHost. This lookupProfile is part on a nfInstace. Then the
     * getFallbackDestinationHost and getFallbackDestinationRealm functions are
     * tested.
     */
    @Test(enabled = true)
    public void bsfServiceWithLookupProfileTest()
    {
        String actualHost = "pcf1";
        String actualRealm = "pcf-diamrealm";

        var diameterRouting = generateDiameterRouting(actualHost, actualRealm, true);
        var nfInstForTest = generateNfInstance(diameterRouting);

        DiameterRouting dr = CmConfigurationUtil.getDiameterRouting(nfInstForTest).orElse(null);

        String host = CmConfigurationUtil.getFallbackDestHostDefRoute(dr);
        String realm = CmConfigurationUtil.getFallbackDestRealmDefRoute(dr);

        assertEquals(host, actualHost, "Invalid host");
        assertEquals(realm, actualRealm, "Invalid Realm");

    }

    /**
     * Creates 3 lookupProfiles with fallbackDestinationHost and
     * fallbackDestinationRealm. These lookupProfiles are part on a nfInstace. Then
     * the getFallbackDestinationHost and getFallbackDestinationRealm functions are
     * tested. It is expected both fallbackDestinationHost and
     * fallbackDestinationRealm of the second lookupProfile (the one with name DSC)
     * will be returned.
     */
    @Test(enabled = true)
    public void multipleLookupProfileTest()
    {
        String actualHost = "pcf1";
        String actualRealm = "pcf-diamrealm";

        LookupProfile lookupProfile1 = new LookupProfile().withName("DSC1")//
                                                          .withFallbackDestinationHost("wrongHostDsc1")//
                                                          .withFallbackDestinationRealm("wrongRealmDsc1");

        LookupProfile lookupProfile2 = new LookupProfile().withName("DSC")//
                                                          .withFallbackDestinationHost(actualHost)//
                                                          .withFallbackDestinationRealm(actualRealm);

        LookupProfile lookupProfile3 = new LookupProfile().withName("DSC2")//
                                                          .withFallbackDestinationHost("wrongHostDsc2")//
                                                          .withFallbackDestinationRealm("wrongRealmDsc2");

        List<LookupProfile> lookupProfiles = new ArrayList<>();
        lookupProfiles.add(lookupProfile1);
        lookupProfiles.add(lookupProfile2);
        lookupProfiles.add(lookupProfile3);

        var diameterRouting = generateDiameterRouting(lookupProfiles);
        var nfInstForTest = generateNfInstance(diameterRouting);

        DiameterRouting dr = CmConfigurationUtil.getDiameterRouting(nfInstForTest).orElse(null);

        String host = CmConfigurationUtil.getFallbackDestHostDefRoute(dr);
        String realm = CmConfigurationUtil.getFallbackDestRealmDefRoute(dr);

        assertEquals(host, actualHost, "Invalid host");
        assertEquals(realm, actualRealm, "Invalid Realm");

    }

    /**
     * Creates a lookupProfile with fallbackDestinationRealm and
     * FallbackDestinationHost. The DiameterRouting doesn't contain Route (Route is
     * empty list) The getFallbackDestinationHost and getFallbackDestinationRealm
     * functions are tested and should return null for both fallbackDestinationRealm
     * and FallbackDestinationHost.
     */
    @Test(enabled = true)
    public void diameterRoutingWithoutRouteTest()
    {

        var diameterRouting = generateDiameterRouting("pcf1", "pcf-diamrealm", false);
        var nfInstForTest = generateNfInstance(diameterRouting);

        DiameterRouting dr = CmConfigurationUtil.getDiameterRouting(nfInstForTest).orElse(null);

        String host = CmConfigurationUtil.getFallbackDestHostDefRoute(dr);
        String realm = CmConfigurationUtil.getFallbackDestRealmDefRoute(dr);

        assertEquals(host, null, "Invalid host");
        assertEquals(realm, null, "Invalid Realm");

    }

    @Test(enabled = true)
    public void multipleBindingResolutionTest()
    {
        final var bsfService = new BsfService().withName("bsfService").withMultipleBindingResolution(null);

        assertEquals(CmConfigurationUtil.getMultipleBindingResolution(bsfService), CmConfigurationUtil.defaultMultipleBindingResolution());
        assertEquals(CmConfigurationUtil.getDiameterLookup(bsfService), CmConfigurationUtil.defaultMultipleBindingResolution().getDiameterLookup());
        assertEquals(CmConfigurationUtil.getHttpLookup(bsfService), CmConfigurationUtil.defaultMultipleBindingResolution().getHttpLookup());
        assertEquals(CmConfigurationUtil.getMultipleBindingResolution(bsfService), CmConfigurationUtil.defaultMultipleBindingResolution());

        bsfService.setMultipleBindingResolution(new MultipleBindingResolution().withDiameterLookup(new DiameterLookup().withResolutionType(ResolutionType.REJECT)
                                                                                                                       .withAvpCombination(List.of(new AvpCombination().withName("myCombination")
                                                                                                                                                                       .withCombination(List.of(Combination_.IPV_4)))))
                                                                               .withHttpLookup(null));
        assertEquals(CmConfigurationUtil.getDiameterLookup(bsfService),
                     new DiameterLookup().withResolutionType(ResolutionType.REJECT)
                                         .withAvpCombination(List.of(new AvpCombination().withName("myCombination")
                                                                                         .withCombination(List.of(Combination_.IPV_4)))));
        assertEquals(CmConfigurationUtil.getHttpLookup(bsfService), CmConfigurationUtil.defaultMultipleBindingResolution().getHttpLookup());

        bsfService.setMultipleBindingResolution(new MultipleBindingResolution().withDiameterLookup(null)
                                                                               .withHttpLookup(new HttpLookup().withResolutionType(HttpLookup.ResolutionType.REJECT)
                                                                                                               .withQueryParameterCombination(List.of(new QueryParameterCombination().withName("myQueryCombination")
                                                                                                                                                                                     .withCombination(List.of(Combination.IPV_4_ADDR,
                                                                                                                                                                                                              Combination.IPV_6_PREFIX))))));
        assertEquals(CmConfigurationUtil.getHttpLookup(bsfService),
                     new HttpLookup().withResolutionType(HttpLookup.ResolutionType.REJECT)
                                     .withQueryParameterCombination(List.of(new QueryParameterCombination().withName("myQueryCombination")
                                                                                                           .withCombination(List.of(Combination.IPV_4_ADDR,
                                                                                                                                    Combination.IPV_6_PREFIX)))));
        assertEquals(CmConfigurationUtil.getDiameterLookup(bsfService), CmConfigurationUtil.defaultMultipleBindingResolution().getDiameterLookup());
    }

}