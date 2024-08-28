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
 * Created on: Jan 29, 2019
 *     Author: zmelpan
 */

package com.ericsson.esc.bsf.openapi.model;

import static org.testng.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.ericsson.esc.lib.ValidationException;

public class PcfBindingTest
{
    private final String validSupi;
    private final String validGpsi;
    private final String validIpv4Addr;
    private final Ipv6Prefix validIpv6Prefix;
    private final String validIpDomain;
    private final MacAddr48 validMacAddr48;
    private final String validDnn;
    private final String validPcfFqdn;
    private final List<IpEndPoint> validPcfIpEndPoints;
    private final DiameterIdentity validPcfDiamHost;
    private final DiameterIdentity validPcfDiamRealm;
    private final String validPcfDiamHostStr;
    private final String validPcfDiamRealmStr;
    private final Snssai validSnssai;
    private final MacAddr48 validmacAddr48_2;
    private final MacAddr48 validmacAddr48_3;
    private final String validmacAddr48_2_str;
    private final String validmacAddr48_3_str;
    private final String validIpv6Prefix_2;
    private final String validIpv6Prefix_3;
    private final List<MacAddr48> validAddMacAddrs;
    private final List<String> validAddMacAddrsStr;
    private final List<String> validIpv6PrefixesStr;
    private final BindingLevel validBindLevel;

    public PcfBindingTest()
    {
        validSupi = "imsi-310150123456789";
        validGpsi = "msisdn-918369110173";
        validIpv4Addr = "144.241.174.78";
        validIpv6Prefix = new Ipv6Prefix("2001:1234:5678:1234::/64");
        validIpDomain = "ericsson.se";
        validMacAddr48 = new MacAddr48("e7-52-e4-63-b6-90");
        validDnn = "valid.ericsson.se";
        validPcfFqdn = "pcf1.cluster1.5gc.mnc012.mcc345.3gppnetwork.org";
        validPcfDiamHost = new DiameterIdentity("public-host.ericsson.se");
        validPcfDiamRealm = new DiameterIdentity("ericsson.se");
        validPcfDiamHostStr = new String("public-host.ericsson.se");
        validPcfDiamRealmStr = new String("ericsson.se");
        validSnssai = Snssai.create(112, "E8F44A");
        validmacAddr48_2 = new MacAddr48("10-65-30-69-46-8D");
        validmacAddr48_3 = new MacAddr48("10-65-30-69-47-8D");
        validAddMacAddrs = new ArrayList<MacAddr48>();
        validAddMacAddrs.add(validmacAddr48_2);
        validAddMacAddrs.add(validmacAddr48_3);
        validmacAddr48_2_str = new String("10-65-30-69-46-8D");
        validmacAddr48_3_str = new String("10-65-30-69-47-8D");
        validAddMacAddrsStr = new ArrayList<String>();
        validAddMacAddrsStr.add(validmacAddr48_2_str);
        validAddMacAddrsStr.add(validmacAddr48_3_str);
        validIpv6Prefix_2 = new String("2002:1234:5678:1234::/64");
        validIpv6Prefix_3 = new String("2003:1234:5678:1234::/64");
        validIpv6PrefixesStr = new ArrayList<String>();
        validIpv6PrefixesStr.add(validIpv6Prefix_2);
        validIpv6PrefixesStr.add(validIpv6Prefix_3);
        validBindLevel = new BindingLevel("NF_INSTANCE");

        IpEndPoint iep1 = IpEndPoint.createJson("202.129.59.212", null, "TCP", 3952);
        IpEndPoint iep2 = IpEndPoint.createJson(null, "44a1:92a5:384b:8534:363e:cd09:d55e:e445", "TCP", 3953);
        validPcfIpEndPoints = new ArrayList<IpEndPoint>();
        validPcfIpEndPoints.add(iep1);
        validPcfIpEndPoints.add(iep2);
    }

    @Test
    public void successfulCreation()
    {

        // Successful PCF binding with ipv4Addr + some optional attributes.
        PcfBinding pbIpv4 = PcfBinding.createJson(validSupi,
                                                  validGpsi,
                                                  validIpv4Addr,
                                                  null,
                                                  null,
                                                  null,
                                                  validDnn,
                                                  validPcfFqdn,
                                                  validPcfIpEndPoints,
                                                  validPcfDiamHostStr,
                                                  validPcfDiamRealmStr,
                                                  validSnssai,
                                                  null,
                                                  null,
                                                  null,
                                                  null,
                                                  null,
                                                  null,
                                                  validBindLevel);

        // Successful PCF binding with ipv6Prefix.
        PcfBinding pbIpv6 = PcfBinding.create(validSupi,
                                              validGpsi,
                                              null,
                                              validIpv6Prefix,
                                              null,
                                              null,
                                              validDnn,
                                              validPcfFqdn,
                                              validPcfIpEndPoints,
                                              validPcfDiamHost,
                                              validPcfDiamRealm,
                                              validSnssai,
                                              null,
                                              null,
                                              null,
                                              null,
                                              null,
                                              null,
                                              validBindLevel);

        // Successful PCF binding with macAddr48.
        PcfBinding pbMac = PcfBinding.create(validSupi,
                                             validGpsi,
                                             null,
                                             null,
                                             null,
                                             validMacAddr48,
                                             validDnn,
                                             validPcfFqdn,
                                             validPcfIpEndPoints,
                                             validPcfDiamHost,
                                             validPcfDiamRealm,
                                             validSnssai,
                                             null,
                                             null,
                                             null,
                                             null,
                                             null,
                                             null,
                                             validBindLevel);

        // Successful PCF binding with ipv4Addr and ipv6Prefix.
        PcfBinding pbIpv4v6 = PcfBinding.createJson(validSupi,
                                                    validGpsi,
                                                    validIpv4Addr,
                                                    validIpv6Prefix,
                                                    null,
                                                    null,
                                                    validDnn,
                                                    validPcfFqdn,
                                                    validPcfIpEndPoints,
                                                    validPcfDiamHostStr,
                                                    validPcfDiamRealmStr,
                                                    validSnssai,
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null);

        // Successful PCF binding with ipv4Addr and ipDomain.
        PcfBinding pbIpv4IpDomain = PcfBinding.createJson(validSupi,
                                                          validGpsi,
                                                          validIpv4Addr,
                                                          null,
                                                          validIpDomain,
                                                          null,
                                                          validDnn,
                                                          validPcfFqdn,
                                                          validPcfIpEndPoints,
                                                          validPcfDiamHostStr,
                                                          validPcfDiamRealmStr,
                                                          validSnssai,
                                                          null,
                                                          null,
                                                          null,
                                                          null,
                                                          null,
                                                          null,
                                                          null);

        // Successful PCF binding with only PcfDiamHost and PcfDiamRealm.
        PcfBinding pbOnePcf = PcfBinding.createJson(validSupi,
                                                    validGpsi,
                                                    validIpv4Addr,
                                                    null,
                                                    null,
                                                    null,
                                                    validDnn,
                                                    null,
                                                    null,
                                                    validPcfDiamHostStr,
                                                    validPcfDiamRealmStr,
                                                    validSnssai,
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null);

        // Successful PCF binding with no supi or gpsi.
        PcfBinding pbNoSupiNoGpsi = PcfBinding.createJson(null,
                                                          null,
                                                          validIpv4Addr,
                                                          null,
                                                          null,
                                                          null,
                                                          validDnn,
                                                          null,
                                                          null,
                                                          validPcfDiamHostStr,
                                                          validPcfDiamRealmStr,
                                                          validSnssai,
                                                          null,
                                                          null,
                                                          null,
                                                          null,
                                                          null,
                                                          null,
                                                          null);

        // Successful PCF binding with multiple macAddr48 addrs.
        PcfBinding pbAddMacs = PcfBinding.create(validSupi,
                                                 validGpsi,
                                                 null,
                                                 null,
                                                 null,
                                                 validMacAddr48,
                                                 validDnn,
                                                 validPcfFqdn,
                                                 validPcfIpEndPoints,
                                                 validPcfDiamHost,
                                                 validPcfDiamRealm,
                                                 validSnssai,
                                                 null,
                                                 null,
                                                 null,
                                                 null,
                                                 validAddMacAddrs,
                                                 null,
                                                 validBindLevel);

        // Successful PCF binding with addIpv6Prefixes only (no ipv4Addr, no
        // ipv6Prefix).
        PcfBinding pbAddIpv6Prefixes = PcfBinding.createJson(validSupi,
                                                             validGpsi,
                                                             null,
                                                             null,
                                                             null,
                                                             null,
                                                             validDnn,
                                                             validPcfFqdn,
                                                             validPcfIpEndPoints,
                                                             validPcfDiamHostStr,
                                                             validPcfDiamRealmStr,
                                                             validSnssai,
                                                             null,
                                                             null,
                                                             null,
                                                             validIpv6PrefixesStr,
                                                             null,
                                                             null,
                                                             validBindLevel);

        assertNotNull(pbIpv4, "Did not create succesfully PCF binding with ipv4addr");
        assertNotNull(pbIpv6, "Did not create succesfully PCF binding with ipv6Prefix");
        assertNotNull(pbMac, "Did not create succesfully PCF binding with macAddr48");
        assertNotNull(pbIpv4v6, "Did not create succesfully PCF binding with ipv4addr and ipv6Prefix");
        assertNotNull(pbIpv4IpDomain, "Did not create succesfully PCF binding with ipv4addr and ipDomain");
        assertNotNull(pbOnePcf, "Did not create succesfully PCF binding with only PcfDiamHost and PcfDiamRealm");
        assertNotNull(pbNoSupiNoGpsi, "Did not create succesfully PCF binding without supi and gpsi");
        assertNotNull(pbNoSupiNoGpsi, "Did not create succesfully PCF binding without supi and gpsi");
        assertNotNull(pbAddMacs, "Did not create succesfully PCF binding with multiple macAddr48 addrs");
        assertNotNull(pbAddIpv6Prefixes, "Did not create succesfully PCF binding with multiple IPv6 prefixes");
    }

    @Test(enabled = true, expectedExceptions = ValidationException.class)
    public void missingArgsNoUeAddress()
    {
        // Create a PCF binding with no UE address.
        PcfBinding.createJson(validSupi,
                              validGpsi,
                              null,
                              null,
                              null,
                              null,
                              validDnn,
                              validPcfFqdn,
                              validPcfIpEndPoints,
                              validPcfDiamHostStr,
                              validPcfDiamRealmStr,
                              validSnssai,
                              null,
                              null,
                              null,
                              null,
                              null,
                              null,
                              null);
    }

    @Test(enabled = true, expectedExceptions = ValidationException.class)
    public void missingArgsNoDnn()
    {
        // Create a PCF binding with no dnn.
        PcfBinding.createJson(validSupi,
                              validGpsi,
                              validIpv4Addr,
                              null,
                              null,
                              null,
                              null,
                              validPcfFqdn,
                              validPcfIpEndPoints,
                              validPcfDiamHostStr,
                              validPcfDiamRealmStr,
                              validSnssai,
                              null,
                              null,
                              null,
                              null,
                              null,
                              null,
                              null);
    }

    @Test(enabled = true, expectedExceptions = ValidationException.class)
    public void missingArgsNoSnssai()
    {
        // Create a PCF binding with no snssai.
        PcfBinding.createJson(validSupi,
                              validGpsi,
                              validIpv4Addr,
                              null,
                              null,
                              null,
                              validDnn,
                              validPcfFqdn,
                              validPcfIpEndPoints,
                              validPcfDiamHostStr,
                              validPcfDiamRealmStr,
                              null,
                              null,
                              null,
                              null,
                              null,
                              null,
                              null,
                              null);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void missingArgsPcfIdentity()
    {
        // Create a PCF binding with no PCF identity.
        PcfBinding.createJson(validSupi,
                              validGpsi,
                              validIpv4Addr,
                              null,
                              null,
                              null,
                              validDnn,
                              null,
                              null,
                              null,
                              null,
                              validSnssai,
                              null,
                              null,
                              null,
                              null,
                              null,
                              null,
                              null);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void invalidArgsIpDomainWithoutIpv4Addr()
    {
        // Create a PCF binding with ipDomain and no ipv4Addr.
        PcfBinding.create(validSupi,
                          validGpsi,
                          null,
                          validIpv6Prefix,
                          validIpDomain,
                          null,
                          validDnn,
                          validPcfFqdn,
                          validPcfIpEndPoints,
                          validPcfDiamHost,
                          validPcfDiamRealm,
                          validSnssai,
                          null,
                          null,
                          null,
                          null,
                          null,
                          null,
                          null);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void invalidArgsMacWithIpv4Addr()
    {
        // Create a PCF binding with macAddr48 and ipv4Addr.
        PcfBinding.createJson(validSupi,
                              validGpsi,
                              validIpv4Addr,
                              null,
                              null,
                              validMacAddr48,
                              validDnn,
                              validPcfFqdn,
                              validPcfIpEndPoints,
                              validPcfDiamHostStr,
                              validPcfDiamRealmStr,
                              validSnssai,
                              null,
                              null,
                              null,
                              null,
                              null,
                              null,
                              null);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void invalidArgsMacWithIpv6Addr()
    {
        // Create a PCF binding with macAddr48 and ipv6Prefix.
        PcfBinding.create(validSupi,
                          validGpsi,
                          null,
                          validIpv6Prefix,
                          null,
                          validMacAddr48,
                          validDnn,
                          validPcfFqdn,
                          validPcfIpEndPoints,
                          validPcfDiamHost,
                          validPcfDiamRealm,
                          validSnssai,
                          null,
                          null,
                          null,
                          null,
                          null,
                          null,
                          null);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void invalidArgsAddMacAddrsWithIpv4Addr()
    {
        // Create a PCF binding with addMacAddrs and ipv4Addr.
        PcfBinding.createJson(validSupi,
                              validGpsi,
                              validIpv4Addr,
                              null,
                              null,
                              null,
                              validDnn,
                              validPcfFqdn,
                              validPcfIpEndPoints,
                              validPcfDiamHostStr,
                              validPcfDiamRealmStr,
                              validSnssai,
                              null,
                              null,
                              null,
                              null,
                              validAddMacAddrsStr,
                              null,
                              null);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void invalidArgsMacWithAddIpv6Prefixes()
    {
        // Create a PCF binding with macAddr48 and addIpv6Prefixes.
        PcfBinding.createJson(validSupi,
                              validGpsi,
                              null,
                              null,
                              null,
                              validMacAddr48,
                              validDnn,
                              validPcfFqdn,
                              validPcfIpEndPoints,
                              validPcfDiamHostStr,
                              validPcfDiamRealmStr,
                              validSnssai,
                              null,
                              null,
                              null,
                              validIpv6PrefixesStr,
                              null,
                              null,
                              null);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void invalidBindLevel()
    {
        // Create a PCF binding with an invalid value of BindLevel.
        PcfBinding.create(validSupi,
                          validGpsi,
                          null,
                          validIpv6Prefix,
                          null,
                          validMacAddr48,
                          validDnn,
                          validPcfFqdn,
                          validPcfIpEndPoints,
                          validPcfDiamHost,
                          validPcfDiamRealm,
                          validSnssai,
                          null,
                          null,
                          null,
                          null,
                          null,
                          null,
                          new BindingLevel("Blabla"));
    }

    @Test(expectedExceptions = ValidationException.class)
    public void invalidPcfDiamHost()
    {
        // Create a PCF binding with an invalid value of BindLevel.
        PcfBinding.create(validSupi,
                          validGpsi,
                          null,
                          validIpv6Prefix,
                          null,
                          validMacAddr48,
                          validDnn,
                          validPcfFqdn,
                          validPcfIpEndPoints,
                          new DiameterIdentity("pcfdiamhost"),
                          validPcfDiamRealm,
                          validSnssai,
                          null,
                          null,
                          null,
                          null,
                          null,
                          null,
                          null);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void invalidAddMacAddrList()
    {
        // Create a PCF binding with a list of additional Mac Addresses containing an
        // invalid Mac Address.
        PcfBinding.create(validSupi,
                          validGpsi,
                          null,
                          validIpv6Prefix,
                          null,
                          validMacAddr48,
                          validDnn,
                          validPcfFqdn,
                          validPcfIpEndPoints,
                          validPcfDiamHost,
                          validPcfDiamRealm,
                          validSnssai,
                          null,
                          null,
                          null,
                          null,
                          List.of(validmacAddr48_2, new MacAddr48("10-65-30-69-46-8DDDD")),
                          null,
                          validBindLevel);
    }
}
