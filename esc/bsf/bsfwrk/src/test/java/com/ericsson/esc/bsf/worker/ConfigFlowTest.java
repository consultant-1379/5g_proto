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

import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.testng.annotations.Test;

import com.ericsson.sc.bsf.model.BindingDatabase;
import com.ericsson.sc.bsf.model.BindingDatabaseScan;
import com.ericsson.sc.bsf.model.BindingDatabaseScan.Configuration;
import com.ericsson.sc.bsf.model.BsfService;
import com.ericsson.sc.bsf.model.CheckUponLookup;
import com.ericsson.sc.bsf.model.EricssonBsf;
import com.ericsson.sc.bsf.model.EricssonBsfBsfFunction;
import com.ericsson.sc.bsf.model.HttpLookup;
import com.ericsson.sc.bsf.model.IngressConnectionProfile;
import com.ericsson.sc.bsf.model.MultipleBindingResolution;
import com.ericsson.sc.bsf.model.NfInstance;
import com.ericsson.sc.bsf.model.NfManagement;
import com.ericsson.sc.bsf.model.NfPeerInfo;
import com.ericsson.sc.bsf.model.NfPeerInfo.OutMessageHandling;
import com.ericsson.sc.bsf.model.Nrf;
import com.ericsson.sc.bsf.model.NrfGroup;
import com.ericsson.sc.bsf.model.NrfService;
import com.ericsson.sc.bsf.model.PcfRecoveryTime;
import com.ericsson.sc.bsf.model.ServiceAddress;
import com.ericsson.sc.bsf.model.Vtap;
import com.ericsson.sc.nfm.model.AllowedPlmn;
import com.ericsson.sc.nfm.model.NfProfile;
import com.ericsson.sc.nfm.model.NfService;
import com.ericsson.sc.nfm.model.Oauth2KeyProfile;
import com.ericsson.sc.nfm.model.Oauth2KeyProfile.Type;
import com.ericsson.sc.nfm.model.Plmn;
import com.ericsson.sc.nfm.model.Scheme;
import com.ericsson.sc.nfm.model.Snssai1;

import io.reactivex.Observable;

/**
 * Unit Tests for various functions that extract configuration from Ericsson BSF
 * Configuration.
 */
public class ConfigFlowTest
{
    private final BsfCfgController bsfCfgController = new BsfCfgController();

    @Test
    private void extractBsfConfigFromEricBsfTest()
    {
        final var config = new EricssonBsf().withEricssonBsfBsfFunction(new EricssonBsfBsfFunction().withNfInstance(List.of(new NfInstance().withName("Bsf1")
                                                                                                                                            .withBsfService(List.of(new BsfService().withBindingDatabase(new BindingDatabase().withBindingTimeout(1))))
                                                                                                                                            .withVtap(new Vtap().withEnabled(true)))));

        final var configFlow = Observable.just(Optional.of(config));

        final var bsfCmConfigFlow = bsfCfgController.extractBsfConfigFromEricBsf(configFlow, 5).blockingFirst().get();

        assertEquals(config.getEricssonBsfBsfFunction().getNfInstance().get(0).getName(), bsfCmConfigFlow.getNfInstanceName());
    }

    @Test
    private void extractSrvAddrConfigFromEricBsfTest()
    {
        final var serviceAddress = new ServiceAddress().withName("serviceAddress1").withFqdn("bsf.ericsson.com").withIpv4Address("192.168.1.1").withPort(8080);

        final var config = new EricssonBsf().withEricssonBsfBsfFunction(new EricssonBsfBsfFunction().withNfInstance(List.of(new NfInstance().withServiceAddress(List.of(serviceAddress)))));

        final var configFlow = Observable.just(Optional.of(config));

        final var bsfCmConfigFlow = bsfCfgController.extractSrvAddrConfigFromEricBsf(configFlow).blockingFirst().get();

        assertEquals(config.getEricssonBsfBsfFunction().getNfInstance().get(0).getServiceAddress().get(0).getName(), bsfCmConfigFlow.getName());
        assertEquals(config.getEricssonBsfBsfFunction().getNfInstance().get(0).getServiceAddress().get(0).getFqdn(), bsfCmConfigFlow.getFqdn());
        assertEquals(config.getEricssonBsfBsfFunction().getNfInstance().get(0).getServiceAddress().get(0).getIpv4Address(), bsfCmConfigFlow.getIpv4Address());
        assertEquals(config.getEricssonBsfBsfFunction().getNfInstance().get(0).getServiceAddress().get(0).getPort(), bsfCmConfigFlow.getPort());

    }

    @Test
    private void extractVtapConfigFromEricBsfTest()
    {
        final var vtap = new Vtap().withEnabled(true);

        final var config = new EricssonBsf().withEricssonBsfBsfFunction(new EricssonBsfBsfFunction().withNfInstance(List.of(new NfInstance().withVtap(vtap))));

        final var configFlow = Observable.just(Optional.of(config));

        final var bsfCmConfigFlow = bsfCfgController.extractVtapConfigFromEricBsf(configFlow).blockingFirst();

        assertEquals(config.getEricssonBsfBsfFunction().getNfInstance().get(0).getVtap().getEnabled(), bsfCmConfigFlow);

    }

    @Test
    private void extractVtapConfigFromEricBsfUpgradeTest() // Check that Vtap is disabled when Vtap does not exist in an SC upgrade
                                                           // scenario.
    {
        Vtap vtap = null;

        final var config = new EricssonBsf().withEricssonBsfBsfFunction(new EricssonBsfBsfFunction().withNfInstance(List.of(new NfInstance().withVtap(vtap))));

        final var configFlow = Observable.just(Optional.of(config));

        final var bsfCmConfigFlow = bsfCfgController.extractVtapConfigFromEricBsf(configFlow).blockingFirst();

        assertFalse(bsfCmConfigFlow, "Vtap Enabled Was True");
    }

    @Test
    private void extractMultipleBindingResolutionFromEricBsfTest()
    {
        final var httpLookup = new HttpLookup().withDeletionUponLookup(false);

        final var multipleBindingConfig = new MultipleBindingResolver(httpLookup);

        final var config = new EricssonBsf().withEricssonBsfBsfFunction(new EricssonBsfBsfFunction().withNfInstance(List.of(new NfInstance().withBsfService(List.of(new BsfService().withMultipleBindingResolution(new MultipleBindingResolution().withHttpLookup(httpLookup)))))));

        final var configFlow = Observable.just(Optional.of(config));

        final var bsfCmConfigFlow = bsfCfgController.extractMultipleBindingResolutionHttpLookUpFromEricBsf(configFlow).blockingFirst();

        assertEquals(multipleBindingConfig.getDeleteMultipleBindings(), bsfCmConfigFlow.getDeleteMultipleBindings());
        assertFalse(bsfCmConfigFlow.getDeleteMultipleBindings());
    }

    @Test
    private void extractRtConfigFromEricBsfTest()
    {
        final var checkUponLookup = new CheckUponLookup().withDeletionUponLookup(false);

        final var bindingDatabaseScan = new BindingDatabaseScan().withConfiguration(Configuration.DISABLED);

        final var rtConfig = new RecoveryTimeConfig(checkUponLookup, bindingDatabaseScan);

        final var config = new EricssonBsf().withEricssonBsfBsfFunction(new EricssonBsfBsfFunction().withNfInstance(List.of(new NfInstance().withBsfService(List.of(new BsfService().withPcfRecoveryTime(new PcfRecoveryTime().withCheckUponLookup(checkUponLookup)
                                                                                                                                                                                                                              .withBindingDatabaseScan(bindingDatabaseScan)))))));
        final var configFlow = Observable.just(Optional.of(config));

        final var bsfCmConfigFlow = bsfCfgController.extractRtConfigFromEricBsf(configFlow).blockingFirst();

        assertEquals(rtConfig.getDeletionUponLookup(), bsfCmConfigFlow.getDeletionUponLookup());
        assertFalse(bsfCmConfigFlow.getDeletionUponLookup());
        assertEquals(bindingDatabaseScan.getConfiguration(), bsfCmConfigFlow.getScanConfig());

    }

    @Test
    private void extractIngressConnectionProfileDefaultValue()
    {
        final var config = new EricssonBsf().withEricssonBsfBsfFunction(new EricssonBsfBsfFunction().withNfInstance(List.of(new NfInstance().withName("Bsf1"))));

        final var configFlow = Observable.just(Optional.of(config));

        final var expectedValue = new IngressConnectionProfile().withName("Profile1").withDscpMarking(0).withHpackTableSize(4096L);
        bsfCfgController.extractIngressConnectionProfileFromEricBsf(configFlow).test().assertValue(expectedValue).assertNoErrors();
    }

    @Test
    private void extractIngressConnectionProfile()
    {
        final var ingressConnectionProfile = new IngressConnectionProfile().withName("Pro").withDscpMarking(4).withHpackTableSize(8000L);
        final var config = new EricssonBsf().withEricssonBsfBsfFunction(new EricssonBsfBsfFunction()//
                                                                                                    .withNfInstance(List.of(new NfInstance().withName("Bsf1")
                                                                                                                                            .withIngressConnectionProfile(List.of(ingressConnectionProfile))
                                                                                                                                            .withIngressConnectionProfileRef("Pro"))));

        final var configFlow = Observable.just(Optional.of(config));

        bsfCfgController.extractIngressConnectionProfileFromEricBsf(configFlow).test().assertValue(ingressConnectionProfile).assertNoErrors();
    }

    @Test
    private void extractIngressConnectionProfileWrongName()
    {
        final var ingressConnectionProfile = new IngressConnectionProfile().withName("Pro").withDscpMarking(4).withHpackTableSize(8000L);
        final var config = new EricssonBsf().withEricssonBsfBsfFunction(new EricssonBsfBsfFunction()//
                                                                                                    .withNfInstance(List.of(new NfInstance().withName("Bsf1")
                                                                                                                                            .withIngressConnectionProfile(List.of(ingressConnectionProfile))
                                                                                                                                            .withIngressConnectionProfileRef("Profile"))));

        final var configFlow = Observable.just(Optional.of(config));

        final var expectedValue = new IngressConnectionProfile().withName("Profile1").withDscpMarking(0).withHpackTableSize(4096L);

        bsfCfgController.extractIngressConnectionProfileFromEricBsf(configFlow).test().assertValue(expectedValue).assertNoErrors();
    }

    @Test
    private void extractIngressConnectionProfileSelectSecond()
    {
        final var ingressConnectionProfile1 = new IngressConnectionProfile().withName("Pro").withDscpMarking(4).withHpackTableSize(8000L);
        final var ingressConnectionProfile2 = new IngressConnectionProfile().withName("Pro2").withDscpMarking(6).withHpackTableSize(8002L);
        final var config = new EricssonBsf().withEricssonBsfBsfFunction(new EricssonBsfBsfFunction()//
                                                                                                    .withNfInstance(List.of(new NfInstance().withName("Bsf1")
                                                                                                                                            .withIngressConnectionProfile(List.of(ingressConnectionProfile1,
                                                                                                                                                                                  ingressConnectionProfile2))
                                                                                                                                            .withIngressConnectionProfileRef("Pro2"))));

        final var configFlow = Observable.just(Optional.of(config));

        bsfCfgController.extractIngressConnectionProfileFromEricBsf(configFlow).test().assertValue(ingressConnectionProfile2).assertNoErrors();
    }

    /*
     * Test peerInfo handling during creation of BsfCmConfig instance
     */
    @Test
    private void bsfCmConfigPeerTest()
    {

        final var defaultZeroBindingTimeout = 10212;
        final var nfInstance = new NfInstance().withName("instanceName").withNfInstanceId("nfInstanceId");

        final var bsfFunction = new EricssonBsfBsfFunction().withNfInstance(List.of(nfInstance));
        final var config = new EricssonBsf().withEricssonBsfBsfFunction(bsfFunction);
        final var controller = new BsfCfgController();

        final var bsfCfgNullPeerInfo = controller.cmConfigToBsfConfig(Optional.of(config), defaultZeroBindingTimeout);
        assertFalse(bsfCfgNullPeerInfo.get().isOutMessageHandling(), "outMessageHandling should be false when peerInfo is null, but it isn't");

        nfInstance.setNfPeerInfo(new NfPeerInfo());
        final var bsfFunctionNewPeerInfo = new EricssonBsfBsfFunction().withNfInstance(List.of(nfInstance));
        final var configNewPeerInfo = new EricssonBsf().withEricssonBsfBsfFunction(bsfFunctionNewPeerInfo);

        final var bsfCfgNewPeerInfo = controller.cmConfigToBsfConfig(Optional.of(configNewPeerInfo), defaultZeroBindingTimeout);
        assertFalse(bsfCfgNewPeerInfo.get().isOutMessageHandling(), "outMessageHandling should be false when peerInfo is null, but it isn't");

        nfInstance.setNfPeerInfo(new NfPeerInfo().withOutMessageHandling(OutMessageHandling.OFF));
        final var bsfFunctionPeerOff = new EricssonBsfBsfFunction().withNfInstance(List.of(nfInstance));
        final var configPeerOff = new EricssonBsf().withEricssonBsfBsfFunction(bsfFunctionPeerOff);

        final var bsfCfgOff = controller.cmConfigToBsfConfig(Optional.of(configPeerOff), defaultZeroBindingTimeout);
        assertFalse(bsfCfgOff.get().isOutMessageHandling(), "outMessageHandling should be false when OutMessageHandling is OFF, but it isn't");

        nfInstance.setNfPeerInfo(new NfPeerInfo().withOutMessageHandling(OutMessageHandling.ON));
        final var bsfFunctionPeerOn = new EricssonBsfBsfFunction().withNfInstance(List.of(nfInstance));
        final var configPeerOn = new EricssonBsf().withEricssonBsfBsfFunction(bsfFunctionPeerOn);

        final var bsfCfgOn = controller.cmConfigToBsfConfig(Optional.of(configPeerOn), defaultZeroBindingTimeout);
        assertTrue("outMessageHandling should be true when OutMessageHandling is ON, but it isn't", bsfCfgOn.get().isOutMessageHandling());
    }

    @Test
    private void bsfCmConfigOAuthTest()
    {

        final var nfmanagement = new NfManagement().withNrfGroupRef(List.of("nrf1"));
        final var nrfService = new NrfService().withNfManagement(nfmanagement);
        final var nrf = new Nrf().withName("nrf");
        final var nrfGroup = new NrfGroup().withName("nrf1").withNfProfileRef("profile1").withNrf(List.of(nrf));
        final var serviceAddress = new ServiceAddress().withName("service1").withPort(80);
        final var nfService = new NfService().withServiceInstanceId("bsf1")
                                             .withOauth2Required(true)
                                             .withServiceAddressRef(List.of("service1"))
                                             .withScheme(Scheme.HTTP);
        final var snssai1 = new Snssai1().withName("snsaai").withSd("sd").withSst(0);
        final var plmn = new Plmn().withMcc("mcc").withMnc("mnc");
        final var allowedPlmn = new AllowedPlmn().withMcc("allowedMcc").withMnc("allowedMnc");
        final var nfProfile = new NfProfile().withName("profile1")
                                             .withNfSetId(List.of("nfsetid"))
                                             .withNfService(List.of(nfService))
                                             .withSnssai1(List.of(snssai1))
                                             .withNsi(List.of("Nsi"))
                                             .withPlmn(List.of(plmn))
                                             .withAllowedPlmn(List.of(allowedPlmn));

        final var keyProfile = new Oauth2KeyProfile().withKeyId("keyid").withValue("keyValue").withType(Type.PEM);
        HashMap<String, Oauth2KeyProfile> keyProfileMap = new HashMap<>();
        keyProfileMap.put("keyid", keyProfile);
        final var nfInstance = new NfInstance().withName("instanceName")
                                               .withNfInstanceId("nfInstanceId")
                                               .withNrfService(nrfService)
                                               .withNrfGroup(List.of(nrfGroup))
                                               .withNfProfile(List.of(nfProfile))
                                               .withServiceAddress(List.of(serviceAddress))
                                               .withOauth2KeyProfile(List.of(keyProfile));

        final var config = new EricssonBsf().withEricssonBsfBsfFunction(new EricssonBsfBsfFunction().withNfInstance(List.of(nfInstance)));
        final var controller = new BsfCfgController();
        final var cmToBsfConfig = controller.cmConfigToBsfConfig(Optional.of(config), 0).get();

        assertEquals(cmToBsfConfig.getNrfs(), List.of(nrf));
        assertEquals(cmToBsfConfig.getSnssai1(), List.of(snssai1));
        assertEquals(cmToBsfConfig.getNsi(), List.of("Nsi"));
        assertEquals(cmToBsfConfig.getPlmn(), List.of(plmn));
        assertEquals(cmToBsfConfig.getAllowedPlmn(), List.of(allowedPlmn));
        assertEquals(cmToBsfConfig.getNfSetId(), List.of("nfsetid"));
        assertEquals(cmToBsfConfig.getOauth().getValue(0), Boolean.TRUE);
        assertEquals(cmToBsfConfig.getOAuthkeyProfilesMap(), keyProfileMap);

    }

    @Test
    private void extractOauthWithNrfGroupNfProfileRefNrfTest()
    {

        final var nfmanagement = new NfManagement().withNrfGroupRef(List.of("nrf1"));
        final var nrfService = new NrfService().withNfManagement(nfmanagement);
        final var nrf = new Nrf().withName("nrf").withNfProfileRef("profile1");
        final var nrfGroup = new NrfGroup().withName("nrf1").withNrf(List.of(nrf));
        final var serviceAddress = new ServiceAddress().withName("service1").withPort(80);
        final var nfService = new NfService().withServiceInstanceId("bsf1")
                                             .withOauth2Required(true)
                                             .withServiceAddressRef(List.of("service1"))
                                             .withScheme(Scheme.HTTP);
        final var snssai1 = new Snssai1().withName("snsaai").withSd("sd").withSst(0);
        final var plmn = new Plmn().withMcc("mcc").withMnc("mnc");
        final var allowedPlmn = new AllowedPlmn().withMcc("allowedMcc").withMnc("allowedMnc");
        final var nfProfile = new NfProfile().withName("profile1")
                                             .withNfSetId(List.of("nfsetid"))
                                             .withNfService(List.of(nfService))
                                             .withSnssai1(List.of(snssai1))
                                             .withNsi(List.of("Nsi"))
                                             .withPlmn(List.of(plmn))
                                             .withAllowedPlmn(List.of(allowedPlmn));

        final var keyProfile = new Oauth2KeyProfile().withKeyId("keyid").withValue("keyValue").withType(Type.PEM);
        HashMap<String, Oauth2KeyProfile> keyProfileMap = new HashMap<>();
        keyProfileMap.put("keyid", keyProfile);
        final var nfInstance = new NfInstance().withName("instanceName")
                                               .withNfInstanceId("nfInstanceId")
                                               .withNrfService(nrfService)
                                               .withNrfGroup(List.of(nrfGroup))
                                               .withNfProfile(List.of(nfProfile))
                                               .withServiceAddress(List.of(serviceAddress))
                                               .withOauth2KeyProfile(List.of(keyProfile));

        final var config = new EricssonBsf().withEricssonBsfBsfFunction(new EricssonBsfBsfFunction().withNfInstance(List.of(nfInstance)));
        final var controller = new BsfCfgController();
        final var cmToBsfConfig = controller.cmConfigToBsfConfig(Optional.of(config), 0).get();

        assertEquals(cmToBsfConfig.getNrfs(), List.of(nrf));
        assertEquals(cmToBsfConfig.getSnssai1(), List.of(snssai1));
        assertEquals(cmToBsfConfig.getNsi(), List.of("Nsi"));
        assertEquals(cmToBsfConfig.getPlmn(), List.of(plmn));
        assertEquals(cmToBsfConfig.getAllowedPlmn(), List.of(allowedPlmn));
        assertEquals(cmToBsfConfig.getNfSetId(), List.of("nfsetid"));
        assertEquals(cmToBsfConfig.getOauth().getValue(0), Boolean.TRUE);
        assertEquals(cmToBsfConfig.getOAuthkeyProfilesMap(), keyProfileMap);

    }

    @Test
    private void extractOauthWithNrfGroupNfProfileRefTest()
    {

        final var nfmanagement = new NfManagement().withNrfGroupRef(List.of("nrf1"));
        final var nrfService = new NrfService().withNfManagement(nfmanagement);
        final var nrf = new Nrf().withName("nrf").withNfProfileRef("profile1");
        final var nrfGroup = new NrfGroup().withName("nrf1").withNfProfileRef("dummy").withNrf(List.of(nrf));
        final var serviceAddress = new ServiceAddress().withName("service1").withPort(80);
        final var nfService = new NfService().withServiceInstanceId("bsf1")
                                             .withOauth2Required(true)
                                             .withServiceAddressRef(List.of("service1"))
                                             .withScheme(Scheme.HTTP);
        final var snssai1 = new Snssai1().withName("snsaai").withSd("sd").withSst(0);
        final var plmn = new Plmn().withMcc("mcc").withMnc("mnc");
        final var allowedPlmn = new AllowedPlmn().withMcc("allowedMcc").withMnc("allowedMnc");
        final var nfProfile = new NfProfile().withName("profile1")
                                             .withNfSetId(List.of("nfsetid"))
                                             .withNfService(List.of(nfService))
                                             .withSnssai1(List.of(snssai1))
                                             .withNsi(List.of("Nsi"))
                                             .withPlmn(List.of(plmn))
                                             .withAllowedPlmn(List.of(allowedPlmn));

        final var keyProfile = new Oauth2KeyProfile().withKeyId("keyid").withValue("keyValue").withType(Type.PEM);
        HashMap<String, Oauth2KeyProfile> keyProfileMap = new HashMap<>();
        keyProfileMap.put("keyid", keyProfile);
        final var nfInstance = new NfInstance().withName("instanceName")
                                               .withNfInstanceId("nfInstanceId")
                                               .withNrfService(nrfService)
                                               .withNrfGroup(List.of(nrfGroup))
                                               .withNfProfile(List.of(nfProfile))
                                               .withServiceAddress(List.of(serviceAddress))
                                               .withOauth2KeyProfile(List.of(keyProfile));

        final var config = new EricssonBsf().withEricssonBsfBsfFunction(new EricssonBsfBsfFunction().withNfInstance(List.of(nfInstance)));
        final var controller = new BsfCfgController();
        final var cmToBsfConfig = controller.cmConfigToBsfConfig(Optional.of(config), 0).get();

        assertEquals(cmToBsfConfig.getNrfs(), List.of(nrf));
        assertEquals(cmToBsfConfig.getSnssai1(), List.of(snssai1));
        assertEquals(cmToBsfConfig.getNsi(), List.of("Nsi"));
        assertEquals(cmToBsfConfig.getPlmn(), List.of(plmn));
        assertEquals(cmToBsfConfig.getAllowedPlmn(), List.of(allowedPlmn));
        assertEquals(cmToBsfConfig.getNfSetId(), List.of("nfsetid"));
        assertEquals(cmToBsfConfig.getOauth().getValue(0), Boolean.TRUE);
        assertEquals(cmToBsfConfig.getOAuthkeyProfilesMap(), keyProfileMap);

    }

    @Test
    private void extractOAuthWithNullNrfSeviceTest()
    {
        final var nfInstance = new NfInstance().withName("instanceName").withNfInstanceId("nfInstanceId");

        final var config = new EricssonBsf().withEricssonBsfBsfFunction(new EricssonBsfBsfFunction().withNfInstance(List.of(nfInstance)));

        final var configFlow = (Optional.of(config));

        assertFalse(bsfCfgController.isOAuthEnabled(configFlow, false));
        assertFalse(bsfCfgController.isOAuthEnabled(configFlow, true));
    }

    @Test
    private void extractOAuthWithNfmanagementTtest()
    {
        final var nrfService = new NrfService().withNfManagement(null);
        final var nfInstance = new NfInstance().withName("instanceName").withNfInstanceId("nfInstanceId").withNrfService(nrfService);

        final var config = new EricssonBsf().withEricssonBsfBsfFunction(new EricssonBsfBsfFunction().withNfInstance(List.of(nfInstance)));

        final var configFlow = (Optional.of(config));

        assertFalse(bsfCfgController.isOAuthEnabled(configFlow, false));
        assertFalse(bsfCfgController.isOAuthEnabled(configFlow, true));
    }

    @Test
    private void extractOAuthWithNrfGroupRefNullTest()
    {
        final var nfmanagement = new NfManagement().withNrfGroupRef(null);
        final var nrfService = new NrfService().withNfManagement(nfmanagement);
        final var nfInstance = new NfInstance().withName("instanceName").withNfInstanceId("nfInstanceId").withNrfService(nrfService);

        final var config = new EricssonBsf().withEricssonBsfBsfFunction(new EricssonBsfBsfFunction().withNfInstance(List.of(nfInstance)));

        final var configFlow = (Optional.of(config));

        assertFalse(bsfCfgController.isOAuthEnabled(configFlow, false));
        assertFalse(bsfCfgController.isOAuthEnabled(configFlow, true));
    }

    @Test
    private void extractOAuthWithNrfGroupRefEmptyTest()
    {
        final var nfmanagement = new NfManagement().withNrfGroupRef(Collections.emptyList());
        final var nrfService = new NrfService().withNfManagement(nfmanagement);
        final var nfInstance = new NfInstance().withName("instanceName").withNfInstanceId("nfInstanceId").withNrfService(nrfService);

        final var config = new EricssonBsf().withEricssonBsfBsfFunction(new EricssonBsfBsfFunction().withNfInstance(List.of(nfInstance)));

        final var configFlow = (Optional.of(config));

        assertFalse(bsfCfgController.isOAuthEnabled(configFlow, false));
        assertFalse(bsfCfgController.isOAuthEnabled(configFlow, true));
    }

    @Test
    private void extractOAuthWithNrfGroupNullTest()
    {
        final var nfmanagement = new NfManagement().withNrfGroupRef(List.of("nrf1"));
        final var nrfService = new NrfService().withNfManagement(nfmanagement);
        final var nfInstance = new NfInstance().withName("instanceName").withNfInstanceId("nfInstanceId").withNrfService(nrfService).withNrfGroup(null);

        final var config = new EricssonBsf().withEricssonBsfBsfFunction(new EricssonBsfBsfFunction().withNfInstance(List.of(nfInstance)));

        final var configFlow = (Optional.of(config));

        assertFalse(bsfCfgController.isOAuthEnabled(configFlow, false));
        assertFalse(bsfCfgController.isOAuthEnabled(configFlow, true));
    }

    @Test
    private void extractOAuthWithNrfGroupEmptyTest()
    {
        final var nfmanagement = new NfManagement().withNrfGroupRef(List.of("nrf1"));
        final var nrfService = new NrfService().withNfManagement(nfmanagement);
        final var nfInstance = new NfInstance().withName("instanceName")
                                               .withNfInstanceId("nfInstanceId")
                                               .withNrfService(nrfService)
                                               .withNrfGroup(Collections.emptyList());

        final var config = new EricssonBsf().withEricssonBsfBsfFunction(new EricssonBsfBsfFunction().withNfInstance(List.of(nfInstance)));

        final var configFlow = (Optional.of(config));

        assertFalse(bsfCfgController.isOAuthEnabled(configFlow, false));
        assertFalse(bsfCfgController.isOAuthEnabled(configFlow, true));
    }

    @Test
    private void extractOAuthWithSameNrfGroupRefTest()
    {
        final var nfmanagement = new NfManagement().withNrfGroupRef(List.of("nrf1"));
        final var nrfService = new NrfService().withNfManagement(nfmanagement);
        final var nrfGroup = new NrfGroup().withName("nrf1");
        final var nfInstance = new NfInstance().withName("instanceName")
                                               .withNfInstanceId("nfInstanceId")
                                               .withNrfService(nrfService)
                                               .withNrfGroup(List.of(nrfGroup));

        final var config = new EricssonBsf().withEricssonBsfBsfFunction(new EricssonBsfBsfFunction().withNfInstance(List.of(nfInstance)));

        final var configFlow = (Optional.of(config));

        assertFalse(bsfCfgController.isOAuthEnabled(configFlow, false));
        assertFalse(bsfCfgController.isOAuthEnabled(configFlow, true));
    }

    @Test
    private void extractOAuthTestWithDiffNrfGroupRef()
    {
        final var nfmanagement = new NfManagement().withNrfGroupRef(List.of("nrf2"));
        final var nrfService = new NrfService().withNfManagement(nfmanagement);
        final var nrfGroup = new NrfGroup().withName("nrf1");
        final var nfInstance = new NfInstance().withName("instanceName")
                                               .withNfInstanceId("nfInstanceId")
                                               .withNrfService(nrfService)
                                               .withNrfGroup(List.of(nrfGroup));

        final var config = new EricssonBsf().withEricssonBsfBsfFunction(new EricssonBsfBsfFunction().withNfInstance(List.of(nfInstance)));

        final var configFlow = (Optional.of(config));

        assertFalse(bsfCfgController.isOAuthEnabled(configFlow, false));
        assertFalse(bsfCfgController.isOAuthEnabled(configFlow, true));
    }

    @Test
    private void extractOAuthNonTlsTest()
    {
        final var nfmanagement = new NfManagement().withNrfGroupRef(List.of("nrf1"));
        final var nrfService = new NrfService().withNfManagement(nfmanagement);
        final var nrfGroup = new NrfGroup().withName("nrf1").withNfProfileRef("profile1");
        final var serviceAddress = new ServiceAddress().withName("service1").withPort(80);
        final var nfService = new NfService().withServiceInstanceId("bsf1")
                                             .withOauth2Required(true)
                                             .withServiceAddressRef(List.of("service1"))
                                             .withScheme(Scheme.HTTP);
        final var nfProfile = new NfProfile().withName("profile1").withNfService(List.of(nfService));
        final var nfInstance = new NfInstance().withName("instanceName")
                                               .withNfInstanceId("nfInstanceId")
                                               .withNrfService(nrfService)
                                               .withNrfGroup(List.of(nrfGroup))
                                               .withNfProfile(List.of(nfProfile))
                                               .withServiceAddress(List.of(serviceAddress));

        final var config = new EricssonBsf().withEricssonBsfBsfFunction(new EricssonBsfBsfFunction().withNfInstance(List.of(nfInstance)));

        final var configFlow = (Optional.of(config));

        assertTrue(bsfCfgController.isOAuthEnabled(configFlow, false));
        assertFalse(bsfCfgController.isOAuthEnabled(configFlow, true));
    }

    @Test
    private void extractOAuthNoPortTest()
    {
        final var nfmanagement = new NfManagement().withNrfGroupRef(List.of("nrf1"));
        final var nrfService = new NrfService().withNfManagement(nfmanagement);
        final var nrfGroup = new NrfGroup().withName("nrf1").withNfProfileRef("profile1");
        final var serviceAddress = new ServiceAddress().withName("service1").withTlsPort(443);
        final var nfService = new NfService().withServiceInstanceId("bsf1")
                                             .withOauth2Required(true)
                                             .withServiceAddressRef(List.of("service1"))
                                             .withScheme(Scheme.HTTP);
        final var nfProfile = new NfProfile().withName("profile1").withNfService(List.of(nfService));
        final var nfInstance = new NfInstance().withName("instanceName")
                                               .withNfInstanceId("nfInstanceId")
                                               .withNrfService(nrfService)
                                               .withNrfGroup(List.of(nrfGroup))
                                               .withNfProfile(List.of(nfProfile))
                                               .withServiceAddress(List.of(serviceAddress));

        final var config = new EricssonBsf().withEricssonBsfBsfFunction(new EricssonBsfBsfFunction().withNfInstance(List.of(nfInstance)));

        final var configFlow = (Optional.of(config));

        assertFalse(bsfCfgController.isOAuthEnabled(configFlow, false));
        assertFalse(bsfCfgController.isOAuthEnabled(configFlow, true));
    }

    @Test
    private void extractOAuthDisabledNonTlsTest()
    {
        final var nfmanagement = new NfManagement().withNrfGroupRef(List.of("nrf1"));
        final var nrfService = new NrfService().withNfManagement(nfmanagement);
        final var nrfGroup = new NrfGroup().withName("nrf1").withNfProfileRef("profile1");
        final var serviceAddress = new ServiceAddress().withName("service1").withPort(80);
        final var nfService = new NfService().withServiceInstanceId("bsf1")
                                             .withOauth2Required(false)
                                             .withServiceAddressRef(List.of("service1"))
                                             .withScheme(Scheme.HTTP);
        final var nfProfile = new NfProfile().withName("profile1").withNfService(List.of(nfService));
        final var nfInstance = new NfInstance().withName("instanceName")
                                               .withNfInstanceId("nfInstanceId")
                                               .withNrfService(nrfService)
                                               .withNrfGroup(List.of(nrfGroup))
                                               .withNfProfile(List.of(nfProfile))
                                               .withServiceAddress(List.of(serviceAddress));

        final var config = new EricssonBsf().withEricssonBsfBsfFunction(new EricssonBsfBsfFunction().withNfInstance(List.of(nfInstance)));

        final var configFlow = (Optional.of(config));

        assertFalse(bsfCfgController.isOAuthEnabled(configFlow, false));
        assertFalse(bsfCfgController.isOAuthEnabled(configFlow, true));
    }

    @Test
    private void extractOAuthTlsTest()
    {
        final var nfmanagement = new NfManagement().withNrfGroupRef(List.of("nrf1"));
        final var nrfService = new NrfService().withNfManagement(nfmanagement);
        final var nrfGroup = new NrfGroup().withName("nrf1").withNfProfileRef("profile1");
        final var serviceAddress = new ServiceAddress().withName("service1").withTlsPort(443);
        final var nfService = new NfService().withServiceInstanceId("bsf1")
                                             .withOauth2Required(true)
                                             .withServiceAddressRef(List.of("service1"))
                                             .withScheme(Scheme.HTTPS);
        final var nfProfile = new NfProfile().withName("profile1").withNfService(List.of(nfService));
        final var nfInstance = new NfInstance().withName("instanceName")
                                               .withNfInstanceId("nfInstanceId")
                                               .withNrfService(nrfService)
                                               .withNrfGroup(List.of(nrfGroup))
                                               .withNfProfile(List.of(nfProfile))
                                               .withServiceAddress(List.of(serviceAddress));

        final var config = new EricssonBsf().withEricssonBsfBsfFunction(new EricssonBsfBsfFunction().withNfInstance(List.of(nfInstance)));

        final var configFlow = (Optional.of(config));

        assertFalse(bsfCfgController.isOAuthEnabled(configFlow, false));
        assertTrue(bsfCfgController.isOAuthEnabled(configFlow, true));
    }

    @Test
    private void extractOAuthNoTlsPortTest()
    {
        final var nfmanagement = new NfManagement().withNrfGroupRef(List.of("nrf1"));
        final var nrfService = new NrfService().withNfManagement(nfmanagement);
        final var nrfGroup = new NrfGroup().withName("nrf1").withNfProfileRef("profile1");
        final var serviceAddress = new ServiceAddress().withName("service1").withPort(80);
        final var nfService = new NfService().withServiceInstanceId("bsf1")
                                             .withOauth2Required(true)
                                             .withServiceAddressRef(List.of("service1"))
                                             .withScheme(Scheme.HTTPS);
        final var nfProfile = new NfProfile().withName("profile1").withNfService(List.of(nfService));
        final var nfInstance = new NfInstance().withName("instanceName")
                                               .withNfInstanceId("nfInstanceId")
                                               .withNrfService(nrfService)
                                               .withNrfGroup(List.of(nrfGroup))
                                               .withNfProfile(List.of(nfProfile))
                                               .withServiceAddress(List.of(serviceAddress));

        final var config = new EricssonBsf().withEricssonBsfBsfFunction(new EricssonBsfBsfFunction().withNfInstance(List.of(nfInstance)));

        final var configFlow = (Optional.of(config));

        assertFalse(bsfCfgController.isOAuthEnabled(configFlow, false));
        assertFalse(bsfCfgController.isOAuthEnabled(configFlow, true));
    }

    @Test
    private void extractOAuthDisabledTlsTest()
    {
        final var nfmanagement = new NfManagement().withNrfGroupRef(List.of("nrf1"));
        final var nrfService = new NrfService().withNfManagement(nfmanagement);
        final var nrfGroup = new NrfGroup().withName("nrf1").withNfProfileRef("profile1");
        final var serviceAddress = new ServiceAddress().withName("service1").withTlsPort(443);
        final var nfService = new NfService().withServiceInstanceId("bsf1")
                                             .withOauth2Required(false)
                                             .withServiceAddressRef(List.of("service1"))
                                             .withScheme(Scheme.HTTPS);
        final var nfProfile = new NfProfile().withName("profile1").withNfService(List.of(nfService));
        final var nfInstance = new NfInstance().withName("instanceName")
                                               .withNfInstanceId("nfInstanceId")
                                               .withNrfService(nrfService)
                                               .withNrfGroup(List.of(nrfGroup))
                                               .withNfProfile(List.of(nfProfile))
                                               .withServiceAddress(List.of(serviceAddress));

        final var config = new EricssonBsf().withEricssonBsfBsfFunction(new EricssonBsfBsfFunction().withNfInstance(List.of(nfInstance)));

        final var configFlow = (Optional.of(config));

        assertFalse(bsfCfgController.isOAuthEnabled(configFlow, false));
        assertFalse(bsfCfgController.isOAuthEnabled(configFlow, true));
    }

    @Test
    private void extractOAuthTlsNoNrfRegistrationTest()
    {
        final var nfmanagement = new NfManagement().withNrfGroupRef(List.of("nrf1"));
        final var nrfService = new NrfService().withNfManagement(nfmanagement);
        final var nrfGroup = new NrfGroup().withName("nrf2").withNfProfileRef("profile1");
        final var nrfGroup2 = new NrfGroup().withName("nrf3").withNfProfileRef("profile1");
        final var serviceAddress = new ServiceAddress().withName("service1").withTlsPort(443);
        final var nfService = new NfService().withServiceInstanceId("bsf1")
                                             .withOauth2Required(true)
                                             .withServiceAddressRef(List.of("service1"))
                                             .withScheme(Scheme.HTTPS);
        final var nfProfile = new NfProfile().withName("profile1").withNfService(List.of(nfService));
        final var nfInstance = new NfInstance().withName("instanceName")
                                               .withNfInstanceId("nfInstanceId")
                                               .withNrfService(nrfService)
                                               .withNrfGroup(List.of(nrfGroup, nrfGroup2))
                                               .withNfProfile(List.of(nfProfile))
                                               .withServiceAddress(List.of(serviceAddress));

        final var config = new EricssonBsf().withEricssonBsfBsfFunction(new EricssonBsfBsfFunction().withNfInstance(List.of(nfInstance)));

        final var configFlow = (Optional.of(config));

        assertFalse(bsfCfgController.isOAuthEnabled(configFlow, false));
        assertFalse(bsfCfgController.isOAuthEnabled(configFlow, true));
    }

    @Test
    private void extractOAuthTlsMultipleNrfRegistrationTest()
    {
        final var nfmanagement = new NfManagement().withNrfGroupRef(List.of("nrf1"));
        final var nrfService = new NrfService().withNfManagement(nfmanagement);
        final var nrfGroup = new NrfGroup().withName("nrf1").withNfProfileRef("profile1");
        final var nrfGroup2 = new NrfGroup().withName("nrf2").withNfProfileRef("profile1");
        final var nrfGroup3 = new NrfGroup().withName("nrf3").withNfProfileRef("profile1");
        final var serviceAddress = new ServiceAddress().withName("service1").withTlsPort(443);
        final var nfService = new NfService().withServiceInstanceId("bsf1")
                                             .withOauth2Required(true)
                                             .withServiceAddressRef(List.of("service1"))
                                             .withScheme(Scheme.HTTPS);
        final var nfProfile = new NfProfile().withName("profile1").withNfService(List.of(nfService));
        final var nfInstance = new NfInstance().withName("instanceName")
                                               .withNfInstanceId("nfInstanceId")
                                               .withNrfService(nrfService)
                                               .withNrfGroup(List.of(nrfGroup, nrfGroup2, nrfGroup3))
                                               .withNfProfile(List.of(nfProfile))
                                               .withServiceAddress(List.of(serviceAddress));

        final var config = new EricssonBsf().withEricssonBsfBsfFunction(new EricssonBsfBsfFunction().withNfInstance(List.of(nfInstance)));

        final var configFlow = (Optional.of(config));

        assertFalse(bsfCfgController.isOAuthEnabled(configFlow, false));
        assertTrue(bsfCfgController.isOAuthEnabled(configFlow, true));
    }

    @Test
    private void extractOAuthMultipleNfProfilesTest()
    {
        final var nfmanagement = new NfManagement().withNrfGroupRef(List.of("nrf1"));
        final var nrfService = new NrfService().withNfManagement(nfmanagement);
        final var nrfGroup = new NrfGroup().withName("nrf1").withNfProfileRef("profile1");
        final var serviceAddress = new ServiceAddress().withName("service1").withPort(80).withTlsPort(443);
        final var nfService = new NfService().withServiceInstanceId("bsf1")
                                             .withOauth2Required(true)
                                             .withServiceAddressRef(List.of("service1"))
                                             .withScheme(Scheme.HTTP);
        final var nfService2 = new NfService().withServiceInstanceId("bsf1")
                                              .withOauth2Required(true)
                                              .withServiceAddressRef(List.of("service1"))
                                              .withScheme(Scheme.HTTPS);
        final var nfProfile = new NfProfile().withName("profile1").withNfService(List.of(nfService, nfService2));
        final var nfInstance = new NfInstance().withName("instanceName")
                                               .withNfInstanceId("nfInstanceId")
                                               .withNrfService(nrfService)
                                               .withNrfGroup(List.of(nrfGroup))
                                               .withNfProfile(List.of(nfProfile))
                                               .withServiceAddress(List.of(serviceAddress));

        final var config = new EricssonBsf().withEricssonBsfBsfFunction(new EricssonBsfBsfFunction().withNfInstance(List.of(nfInstance)));

        final var configFlow = (Optional.of(config));

        assertTrue(bsfCfgController.isOAuthEnabled(configFlow, false));
        assertTrue(bsfCfgController.isOAuthEnabled(configFlow, true));
    }

    @Test
    private void extractOAuthMultipleNfProfilesNoTlsPortTest()
    {
        final var nfmanagement = new NfManagement().withNrfGroupRef(List.of("nrf1"));
        final var nrfService = new NrfService().withNfManagement(nfmanagement);
        final var nrfGroup = new NrfGroup().withName("nrf1").withNfProfileRef("profile1");
        final var serviceAddress = new ServiceAddress().withName("service1").withPort(80);
        final var nfService = new NfService().withServiceInstanceId("bsf1")
                                             .withOauth2Required(true)
                                             .withServiceAddressRef(List.of("service1"))
                                             .withScheme(Scheme.HTTP);
        final var nfService2 = new NfService().withServiceInstanceId("bsf1")
                                              .withOauth2Required(true)
                                              .withServiceAddressRef(List.of("service1"))
                                              .withScheme(Scheme.HTTPS);
        final var nfProfile = new NfProfile().withName("profile1").withNfService(List.of(nfService, nfService2));
        final var nfInstance = new NfInstance().withName("instanceName")
                                               .withNfInstanceId("nfInstanceId")
                                               .withNrfService(nrfService)
                                               .withNrfGroup(List.of(nrfGroup))
                                               .withNfProfile(List.of(nfProfile))
                                               .withServiceAddress(List.of(serviceAddress));

        final var config = new EricssonBsf().withEricssonBsfBsfFunction(new EricssonBsfBsfFunction().withNfInstance(List.of(nfInstance)));

        final var configFlow = (Optional.of(config));

        assertTrue(bsfCfgController.isOAuthEnabled(configFlow, false));
        assertFalse(bsfCfgController.isOAuthEnabled(configFlow, true));
    }

    @Test
    private void extractOAuthMultipleNfProfilesNoPortTest()
    {
        final var nfmanagement = new NfManagement().withNrfGroupRef(List.of("nrf1"));
        final var nrfService = new NrfService().withNfManagement(nfmanagement);
        final var nrfGroup = new NrfGroup().withName("nrf1").withNfProfileRef("profile1");
        final var serviceAddress = new ServiceAddress().withName("service1").withTlsPort(443);
        final var nfService = new NfService().withServiceInstanceId("bsf1")
                                             .withOauth2Required(true)
                                             .withServiceAddressRef(List.of("service1"))
                                             .withScheme(Scheme.HTTP);
        final var nfService2 = new NfService().withServiceInstanceId("bsf1")
                                              .withOauth2Required(true)
                                              .withServiceAddressRef(List.of("service1"))
                                              .withScheme(Scheme.HTTPS);
        final var nfProfile = new NfProfile().withName("profile1").withNfService(List.of(nfService, nfService2));
        final var nfInstance = new NfInstance().withName("instanceName")
                                               .withNfInstanceId("nfInstanceId")
                                               .withNrfService(nrfService)
                                               .withNrfGroup(List.of(nrfGroup))
                                               .withNfProfile(List.of(nfProfile))
                                               .withServiceAddress(List.of(serviceAddress));

        final var config = new EricssonBsf().withEricssonBsfBsfFunction(new EricssonBsfBsfFunction().withNfInstance(List.of(nfInstance)));

        final var configFlow = (Optional.of(config));

        assertFalse(bsfCfgController.isOAuthEnabled(configFlow, false));
        assertTrue(bsfCfgController.isOAuthEnabled(configFlow, true));
    }

    @Test
    private void extractOAuthExternalServiceAddressTest()
    {
        final var nfmanagement = new NfManagement().withNrfGroupRef(List.of("nrf1"));
        final var nrfService = new NrfService().withNfManagement(nfmanagement);
        final var nrfGroup = new NrfGroup().withName("nrf1").withNfProfileRef("profile1");
        final var serviceAddress = new ServiceAddress().withName("service1").withPort(80);
        final var nfService = new NfService().withServiceInstanceId("bsf1").withOauth2Required(true).withScheme(Scheme.HTTP);
        final var nfProfile = new NfProfile().withName("profile1").withNfService(List.of(nfService)).withServiceAddressRef("service1");
        final var nfInstance = new NfInstance().withName("instanceName")
                                               .withNfInstanceId("nfInstanceId")
                                               .withNrfService(nrfService)
                                               .withNrfGroup(List.of(nrfGroup))
                                               .withNfProfile(List.of(nfProfile))
                                               .withServiceAddress(List.of(serviceAddress));

        final var config = new EricssonBsf().withEricssonBsfBsfFunction(new EricssonBsfBsfFunction().withNfInstance(List.of(nfInstance)));

        final var configFlow = (Optional.of(config));

        assertTrue(bsfCfgController.isOAuthEnabled(configFlow, false));
        assertFalse(bsfCfgController.isOAuthEnabled(configFlow, true));
    }

}
