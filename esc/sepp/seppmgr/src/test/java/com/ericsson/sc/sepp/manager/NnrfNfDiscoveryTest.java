package com.ericsson.sc.sepp.manager;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cnal.common.OpenApiObjectMapper;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.custom.NFProfile;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.IpEndPoint;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFStatus;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFType;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.ScpDomainInfo;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.ScpInfo;
import com.ericsson.sc.nfm.model.Scheme;
import com.ericsson.sc.nfm.model.Transport;
import com.ericsson.sc.sepp.model.Address;
import com.ericsson.sc.sepp.model.DiscoveredScpDomainInfo;
import com.ericsson.sc.sepp.model.MultipleIpEndpoint;
import com.fasterxml.jackson.core.JsonProcessingException;

class NnrfNfDiscoveryTest
{
    private static final Logger log = LoggerFactory.getLogger(NnrfNfDiscoveryTest.class);

    private String toJson(final Object o)
    {
        try
        {
            return OpenApiObjectMapper.singleton().writeValueAsString(o);
        }
        catch (JsonProcessingException e)
        {
            log.error("Error generating JSON string from object", e);
        }

        return "";
    }

    @Test
    void testCreateNfServiceList_01() throws IOException
    {
        final NFProfile p = new NFProfile().capacity(10)
                                           .priority(11)
                                           .nfType(NFType.PCF)
                                           .nfStatus(NFStatus.REGISTERED)
                                           .fqdn("pcf.ericsson.com")
                                           .ipv4Addresses(List.of("1.1.1.1", "1.1.1.2"));

        final String expected = toJson(List.of());
        final String actual = toJson(NnrfNfDiscovery.createNfServiceList(p));

        log.info("act: {}", actual);
        log.info("exp: {}", expected);

        Assertions.assertTrue(actual.equals(expected));
    }

    @Test
    void testCreateScpDomainInfoList_02() throws IOException
    {
        final ScpInfo scpInfo = new ScpInfo();
        final NFProfile p = new NFProfile().capacity(10)
                                           .priority(11)
                                           .nfType(NFType.SCP)
                                           .nfStatus(NFStatus.REGISTERED)
                                           .fqdn("scp.ericsson.com")
                                           .ipv4Addresses(List.of("1.1.1.1", "1.1.1.2"))
                                           .scpInfo(scpInfo);

        final MultipleIpEndpoint ep = new MultipleIpEndpoint().withName("TCP-80")
                                                              .withTransport(Transport.TCP)
                                                              .withIpv4Address(p.getIpv4Addresses())
                                                              .withPort(80);
        final Address addr = new Address().withFqdn(p.getFqdn()).withScheme(Scheme.HTTP).withMultipleIpEndpoint(List.of(ep));
        final DiscoveredScpDomainInfo domainInfoNonTls = new DiscoveredScpDomainInfo().withName("default-non-tls")
                                                                                      .withDomain(null)
                                                                                      .withCapacity(p.getCapacity())
                                                                                      .withPriority(p.getPriority())
                                                                                      .withAddress(addr);

        final String expected = toJson(List.of(domainInfoNonTls).stream().collect(Collectors.toCollection(TreeSet::new)));

        final String actual = toJson(NnrfNfDiscovery.createScpDomainInfoList(p));

        log.info("act: {}", actual);
        log.info("exp: {}", expected);

        Assertions.assertTrue(actual.equals(expected));
    }

    @Test
    void testCreateScpDomainInfoList_03() throws IOException
    {
        final ScpInfo scpInfo = new ScpInfo().scpPorts(Map.of("http", 8080, "https", 50443));
        final NFProfile p = new NFProfile().capacity(10)
                                           .priority(11)
                                           .nfType(NFType.SCP)
                                           .nfStatus(NFStatus.REGISTERED)
                                           .fqdn("scp.ericsson.com")
                                           .ipv4Addresses(List.of("1.1.1.1", "1.1.1.2"))
                                           .scpInfo(scpInfo);

        final MultipleIpEndpoint epNonTls = new MultipleIpEndpoint().withName("TCP-8080")
                                                                    .withTransport(Transport.TCP)
                                                                    .withIpv4Address(p.getIpv4Addresses())
                                                                    .withPort(8080);
        final Address addrNonTls = new Address().withFqdn(p.getFqdn()).withScheme(Scheme.HTTP).withMultipleIpEndpoint(List.of(epNonTls));
        final DiscoveredScpDomainInfo domainInfoNonTls = new DiscoveredScpDomainInfo().withName("default-non-tls")
                                                                                      .withDomain(null)
                                                                                      .withCapacity(p.getCapacity())
                                                                                      .withPriority(p.getPriority())
                                                                                      .withAddress(addrNonTls);

        final MultipleIpEndpoint epTls = new MultipleIpEndpoint().withName("TCP-50443")
                                                                 .withTransport(Transport.TCP)
                                                                 .withIpv4Address(p.getIpv4Addresses())
                                                                 .withPort(50443);
        final Address addrTls = new Address().withFqdn(p.getFqdn()).withScheme(Scheme.HTTPS).withMultipleIpEndpoint(List.of(epTls));
        final DiscoveredScpDomainInfo domainInfoTls = new DiscoveredScpDomainInfo().withName("default-tls")
                                                                                   .withDomain(null)
                                                                                   .withCapacity(p.getCapacity())
                                                                                   .withPriority(p.getPriority())
                                                                                   .withAddress(addrTls);

        final String expected = toJson(List.of(domainInfoNonTls, domainInfoTls).stream().collect(Collectors.toCollection(TreeSet::new)));

        final String actual = toJson(NnrfNfDiscovery.createScpDomainInfoList(p));

        log.info("act: {}", actual);
        log.info("exp: {}", expected);

        Assertions.assertTrue(actual.equals(expected));
    }

    @Test
    void testCreateScpDomainInfoList_04() throws IOException
    {
        final ScpInfo scpInfo = new ScpInfo();
        final NFProfile p = new NFProfile().capacity(10)
                                           .priority(11)
                                           .nfType(NFType.SCP)
                                           .nfStatus(NFStatus.REGISTERED)
                                           .fqdn("scp.ericsson.com")
                                           .ipv4Addresses(List.of("1.1.1.1", "1.1.1.2"))
                                           .scpDomains(List.of("ericsson.se", "ericsson.com"))
                                           .scpInfo(scpInfo);

        // Do not generate one DiscoveredScpInfo for each SCP domain, as they would be
        // equal, generate only one default ScpDomainInfo covering all domains.

        final MultipleIpEndpoint epNonTls = new MultipleIpEndpoint().withName("TCP-80")
                                                                    .withTransport(Transport.TCP)
                                                                    .withIpv4Address(p.getIpv4Addresses())
                                                                    .withPort(80);
        final Address addrNonTls = new Address().withFqdn(p.getFqdn()).withScheme(Scheme.HTTP).withMultipleIpEndpoint(List.of(epNonTls));
        final DiscoveredScpDomainInfo domainInfoNonTls = new DiscoveredScpDomainInfo().withName("default-non-tls")
                                                                                      .withDomain(null)
                                                                                      .withCapacity(p.getCapacity())
                                                                                      .withPriority(p.getPriority())
                                                                                      .withAddress(addrNonTls);

        final String expected = toJson(List.of(domainInfoNonTls).stream().collect(Collectors.toCollection(TreeSet::new)));

        final String actual = toJson(NnrfNfDiscovery.createScpDomainInfoList(p));

        log.info("act: {}", actual);
        log.info("exp: {}", expected);

        Assertions.assertTrue(actual.equals(expected));
    }

    @Test
    void testCreateScpDomainInfoList_05() throws IOException
    {
        final ScpInfo scpInfo = new ScpInfo().scpPorts(Map.of("http", 8080));
        final NFProfile p = new NFProfile().capacity(10)
                                           .priority(11)
                                           .nfType(NFType.SCP)
                                           .nfStatus(NFStatus.REGISTERED)
                                           .fqdn("scp.ericsson.com")
                                           .ipv4Addresses(List.of("1.1.1.1", "1.1.1.2"))
                                           .scpDomains(List.of("ericsson.se", "ericsson.com"))
                                           .scpInfo(scpInfo);

        // Do not generate one DiscoveredScpInfo for each SCP domain, as they would be
        // equal, generate only one default ScpDomainInfo covering all domains.

        final MultipleIpEndpoint epNonTls = new MultipleIpEndpoint().withName("TCP-8080")
                                                                    .withTransport(Transport.TCP)
                                                                    .withIpv4Address(p.getIpv4Addresses())
                                                                    .withPort(8080);
        final Address addrNonTls = new Address().withFqdn(p.getFqdn()).withScheme(Scheme.HTTP).withMultipleIpEndpoint(List.of(epNonTls));
        final DiscoveredScpDomainInfo domainInfoNonTls = new DiscoveredScpDomainInfo().withName("default-non-tls")
                                                                                      .withDomain(null)
                                                                                      .withCapacity(p.getCapacity())
                                                                                      .withPriority(p.getPriority())
                                                                                      .withAddress(addrNonTls);

        final String expected = toJson(List.of(domainInfoNonTls).stream().collect(Collectors.toCollection(TreeSet::new)));

        final String actual = toJson(NnrfNfDiscovery.createScpDomainInfoList(p));

        log.info("act: {}", actual);
        log.info("exp: {}", expected);

        Assertions.assertTrue(actual.equals(expected));
    }

    @Test
    void testCreateScpDomainInfoList_06() throws IOException
    {
        final ScpInfo scpInfo = new ScpInfo().scpPorts(Map.of("http", 8080, "https", 50443));
        final NFProfile p = new NFProfile().capacity(10)
                                           .priority(11)
                                           .nfType(NFType.SCP)
                                           .nfStatus(NFStatus.REGISTERED)
                                           .fqdn("scp.ericsson.com")
                                           .ipv4Addresses(List.of("1.1.1.1", "1.1.1.2"))
                                           .scpDomains(List.of("ericsson.se", "ericsson.com"))
                                           .scpInfo(scpInfo);

        final MultipleIpEndpoint epNonTls = new MultipleIpEndpoint().withName("TCP-8080")
                                                                    .withTransport(Transport.TCP)
                                                                    .withIpv4Address(p.getIpv4Addresses())
                                                                    .withPort(8080);
        final Address addrNonTls = new Address().withFqdn(p.getFqdn()).withScheme(Scheme.HTTP).withMultipleIpEndpoint(List.of(epNonTls));
        final DiscoveredScpDomainInfo domainInfoNonTls = new DiscoveredScpDomainInfo().withName("default-non-tls")
                                                                                      .withDomain(null)
                                                                                      .withCapacity(p.getCapacity())
                                                                                      .withPriority(p.getPriority())
                                                                                      .withAddress(addrNonTls);

        final MultipleIpEndpoint epTls = new MultipleIpEndpoint().withName("TCP-50443")
                                                                 .withTransport(Transport.TCP)
                                                                 .withIpv4Address(p.getIpv4Addresses())
                                                                 .withPort(50443);
        final Address addrTls = new Address().withFqdn(p.getFqdn()).withScheme(Scheme.HTTPS).withMultipleIpEndpoint(List.of(epTls));
        final DiscoveredScpDomainInfo domainInfoTls = new DiscoveredScpDomainInfo().withName("default-tls")
                                                                                   .withDomain(null)
                                                                                   .withCapacity(p.getCapacity())
                                                                                   .withPriority(p.getPriority())
                                                                                   .withAddress(addrTls);

        final String expected = toJson(List.of(domainInfoNonTls, domainInfoTls).stream().collect(Collectors.toCollection(TreeSet::new)));

        final String actual = toJson(NnrfNfDiscovery.createScpDomainInfoList(p));

        log.info("act: {}", actual);
        log.info("exp: {}", expected);

        Assertions.assertTrue(actual.equals(expected));
    }

    @Test
    void testCreateScpDomainInfoList_07() throws IOException
    {
        final ScpDomainInfo scpDomainInfo = new ScpDomainInfo();
        final ScpInfo scpInfo = new ScpInfo().scpPorts(Map.of("https", 50443))
                                             .scpDomainInfoList(Map.of("ericsson.se", scpDomainInfo, "ericsson.com", scpDomainInfo));
        final NFProfile p = new NFProfile().capacity(10)
                                           .priority(11)
                                           .nfType(NFType.SCP)
                                           .nfStatus(NFStatus.REGISTERED)
                                           .fqdn("scp.ericsson.com")
                                           .ipv4Addresses(List.of("1.1.1.1", "1.1.1.2"))
                                           .scpDomains(List.of("ericsson.se", "ericsson.com"))
                                           .scpInfo(scpInfo);

        final MultipleIpEndpoint epTls = new MultipleIpEndpoint().withName("TCP-50443")
                                                                 .withTransport(Transport.TCP)
                                                                 .withIpv4Address(p.getIpv4Addresses())
                                                                 .withPort(50443);
        final Address addrTls = new Address().withFqdn(p.getFqdn()).withScheme(Scheme.HTTPS).withMultipleIpEndpoint(List.of(epTls));
        final DiscoveredScpDomainInfo domainInfoTls = new DiscoveredScpDomainInfo().withName("default-tls")
                                                                                   .withDomain(null)
                                                                                   .withCapacity(p.getCapacity())
                                                                                   .withPriority(p.getPriority())
                                                                                   .withAddress(addrTls);

        final String expected = toJson(List.of(domainInfoTls).stream().collect(Collectors.toCollection(TreeSet::new)));

        final String actual = toJson(NnrfNfDiscovery.createScpDomainInfoList(p));

        log.info("act: {}", actual);
        log.info("exp: {}", expected);

        Assertions.assertTrue(actual.equals(expected));
    }

    @Test
    void testCreateScpDomainInfoList_08() throws IOException
    {
        final ScpDomainInfo scpDomainInfo = new ScpDomainInfo();
        final ScpInfo scpInfo = new ScpInfo().scpPorts(Map.of("http", 8080, "https", 50443)).scpDomainInfoList(Map.of("ericsson.se", scpDomainInfo));
        final NFProfile p = new NFProfile().capacity(10)
                                           .priority(11)
                                           .nfType(NFType.SCP)
                                           .nfStatus(NFStatus.REGISTERED)
                                           .fqdn("scp.ericsson.com")
                                           .ipv4Addresses(List.of("1.1.1.1", "1.1.1.2"))
                                           .scpDomains(List.of("ericsson.se", "ericsson.com", "ericsson.de"))
                                           .scpInfo(scpInfo);

        final MultipleIpEndpoint epNonTls = new MultipleIpEndpoint().withName("TCP-8080")
                                                                    .withTransport(Transport.TCP)
                                                                    .withIpv4Address(p.getIpv4Addresses())
                                                                    .withPort(8080);
        final Address addrNonTls = new Address().withFqdn(p.getFqdn()).withScheme(Scheme.HTTP).withMultipleIpEndpoint(List.of(epNonTls));
        final DiscoveredScpDomainInfo domainInfoNonTls = new DiscoveredScpDomainInfo().withName("default-non-tls")
                                                                                      .withDomain(null)
                                                                                      .withCapacity(p.getCapacity())
                                                                                      .withPriority(p.getPriority())
                                                                                      .withAddress(addrNonTls);

        final MultipleIpEndpoint epTls = new MultipleIpEndpoint().withName("TCP-50443")
                                                                 .withTransport(Transport.TCP)
                                                                 .withIpv4Address(p.getIpv4Addresses())
                                                                 .withPort(50443);
        final Address addrTls = new Address().withFqdn(p.getFqdn()).withScheme(Scheme.HTTPS).withMultipleIpEndpoint(List.of(epTls));
        final DiscoveredScpDomainInfo domainInfoTls = new DiscoveredScpDomainInfo().withName("default-tls")
                                                                                   .withDomain(null)
                                                                                   .withCapacity(p.getCapacity())
                                                                                   .withPriority(p.getPriority())
                                                                                   .withAddress(addrTls);

        final String expected = toJson(List.of(domainInfoNonTls, domainInfoTls).stream().collect(Collectors.toCollection(TreeSet::new)));

        final String actual = toJson(NnrfNfDiscovery.createScpDomainInfoList(p));

        log.info("act: {}", actual);
        log.info("exp: {}", expected);

        Assertions.assertTrue(actual.equals(expected));
    }

    @Test
    void testCreateScpDomainInfoList_09() throws IOException
    {
        final ScpDomainInfo scpDomainInfoEricssonSe = new ScpDomainInfo().scpFqdn("scp.ericsson.se")
                                                                         .scpIpEndPoints(List.of(new IpEndPoint().ipv4Address("1.1.1.3").port(50444),
                                                                                                 new IpEndPoint().ipv4Address("1.1.1.9"))); // <- should be
                                                                                                                                            // ignored (does not
                                                                                                                                            // have a port)

        final ScpDomainInfo scpDomainInfoEricssonCom = new ScpDomainInfo().scpIpEndPoints(List.of(new IpEndPoint().ipv4Address("1.1.1.4")));

        final ScpInfo scpInfo = new ScpInfo().scpPorts(Map.of("http", 50443))
                                             .scpDomainInfoList(Map.of("ericsson.se", scpDomainInfoEricssonSe, "ericsson.com", scpDomainInfoEricssonCom));
        final NFProfile p = new NFProfile().capacity(10)
                                           .priority(11)
                                           .nfType(NFType.SCP)
                                           .nfStatus(NFStatus.REGISTERED)
                                           .fqdn("scp.ericsson.com")
                                           .ipv4Addresses(List.of("1.1.1.1", "1.1.1.2"))
                                           .scpDomains(List.of("ericsson.se", "ericsson.com"))
                                           .scpInfo(scpInfo);

        final MultipleIpEndpoint epNonTlsEricssonSe = new MultipleIpEndpoint().withName("TCP-50444")
                                                                              .withTransport(Transport.TCP)
                                                                              .withIpv4Address(List.of("1.1.1.3"))
                                                                              .withPort(50444);
        final Address addrNonTlsEricssonSe = new Address().withFqdn("scp.ericsson.se")
                                                          .withScheme(Scheme.HTTP)
                                                          .withMultipleIpEndpoint(List.of(epNonTlsEricssonSe));
        final DiscoveredScpDomainInfo domainInfoNonTlsEricssonSe = new DiscoveredScpDomainInfo().withName("ericsson.se-non-tls")
                                                                                                .withDomain("ericsson.se")
                                                                                                .withCapacity(p.getCapacity())
                                                                                                .withPriority(p.getPriority())
                                                                                                .withAddress(addrNonTlsEricssonSe);

        final MultipleIpEndpoint epTlsEricssonSe = new MultipleIpEndpoint().withName("TCP-50444")
                                                                           .withTransport(Transport.TCP)
                                                                           .withIpv4Address(List.of("1.1.1.3"))
                                                                           .withPort(50444);
        final Address addrTlsEricssonSe = new Address().withFqdn("scp.ericsson.se").withScheme(Scheme.HTTPS).withMultipleIpEndpoint(List.of(epTlsEricssonSe));
        final DiscoveredScpDomainInfo domainInfoTlsEricssonSe = new DiscoveredScpDomainInfo().withName("ericsson.se-tls")
                                                                                             .withDomain("ericsson.se")
                                                                                             .withCapacity(p.getCapacity())
                                                                                             .withPriority(p.getPriority())
                                                                                             .withAddress(addrTlsEricssonSe);

        final MultipleIpEndpoint epNonTlsEricssonCom = new MultipleIpEndpoint().withName("TCP-50443")
                                                                               .withTransport(Transport.TCP)
                                                                               .withIpv4Address(List.of("1.1.1.4"))
                                                                               .withPort(50443);
        final Address addrTlsEricssonCom = new Address().withScheme(Scheme.HTTP).withMultipleIpEndpoint(List.of(epNonTlsEricssonCom));
        final DiscoveredScpDomainInfo domainInfoNonTlsEricssonCom = new DiscoveredScpDomainInfo().withName("ericsson.com-non-tls")
                                                                                                 .withDomain("ericsson.com")
                                                                                                 .withCapacity(p.getCapacity())
                                                                                                 .withPriority(p.getPriority())
                                                                                                 .withAddress(addrTlsEricssonCom);

        final String expected = toJson(List.of(domainInfoNonTlsEricssonSe, domainInfoTlsEricssonSe, domainInfoNonTlsEricssonCom)
                                           .stream()
                                           .collect(Collectors.toCollection(TreeSet::new)));

        final String actual = toJson(NnrfNfDiscovery.createScpDomainInfoList(p));

        log.info("act: {}", actual);
        log.info("exp: {}", expected);

        Assertions.assertTrue(actual.equals(expected));
    }

    @Test
    void testCreateScpDomainInfoList_10() throws IOException
    {
        final ScpDomainInfo scpDomainInfoEricssonSe = new ScpDomainInfo().scpFqdn("scp.ericsson.se").scpPorts(Map.of("https", 50444));

        final ScpDomainInfo scpDomainInfoEricssonDe = new ScpDomainInfo().scpFqdn("scp.ericsson.de")
                                                                         .scpIpEndPoints(List.of(new IpEndPoint().ipv4Address("1.1.1.3")))
                                                                         .scpPorts(Map.of("https", 50444));

        final ScpDomainInfo scpDomainInfoEricssonCom = new ScpDomainInfo().scpIpEndPoints(List.of(new IpEndPoint().ipv4Address("1.1.1.4")))
                                                                          .scpPorts(Map.of("http", 50445, "https", 50446));

        final ScpInfo scpInfo = new ScpInfo().scpPorts(Map.of("https", 50443))
                                             .scpDomainInfoList(Map.of("ericsson.se",
                                                                       scpDomainInfoEricssonSe,
                                                                       "ericsson.de",
                                                                       scpDomainInfoEricssonDe,
                                                                       "ericsson.com",
                                                                       scpDomainInfoEricssonCom));
        final NFProfile p = new NFProfile().capacity(10)
                                           .priority(11)
                                           .nfType(NFType.SCP)
                                           .nfStatus(NFStatus.REGISTERED)
                                           .fqdn("scp.ericsson.com")
                                           .ipv4Addresses(List.of("1.1.1.1", "1.1.1.2"))
                                           .scpDomains(List.of("ericsson.se", "ericsson.com", "ericsson.de"))
                                           .scpInfo(scpInfo);

        final MultipleIpEndpoint epTlsEricssonSe = new MultipleIpEndpoint().withName("TCP-50444").withTransport(Transport.TCP).withPort(50444);
        final Address addrTlsEricssonSe = new Address().withFqdn("scp.ericsson.se").withScheme(Scheme.HTTPS).withMultipleIpEndpoint(List.of(epTlsEricssonSe));
        final DiscoveredScpDomainInfo domainInfoTlsEricssonSe = new DiscoveredScpDomainInfo().withName("ericsson.se-tls")
                                                                                             .withDomain("ericsson.se")
                                                                                             .withCapacity(p.getCapacity())
                                                                                             .withPriority(p.getPriority())
                                                                                             .withAddress(addrTlsEricssonSe);

        final MultipleIpEndpoint epTlsEricssonDe = new MultipleIpEndpoint().withName("TCP-50444")
                                                                           .withTransport(Transport.TCP)
                                                                           .withIpv4Address(List.of("1.1.1.3"))
                                                                           .withPort(50444);
        final Address addrTlsEricssonDe = new Address().withFqdn("scp.ericsson.de").withScheme(Scheme.HTTPS).withMultipleIpEndpoint(List.of(epTlsEricssonDe));
        final DiscoveredScpDomainInfo domainInfoTlsEricssonDe = new DiscoveredScpDomainInfo().withName("ericsson.de-tls")
                                                                                             .withDomain("ericsson.de")
                                                                                             .withCapacity(p.getCapacity())
                                                                                             .withPriority(p.getPriority())
                                                                                             .withAddress(addrTlsEricssonDe);

        final MultipleIpEndpoint epNonTlsEricssonCom = new MultipleIpEndpoint().withName("TCP-50445")
                                                                               .withTransport(Transport.TCP)
                                                                               .withIpv4Address(List.of("1.1.1.4"))
                                                                               .withPort(50445);
        final Address addrNonTlsEricssonCom = new Address().withScheme(Scheme.HTTP).withMultipleIpEndpoint(List.of(epNonTlsEricssonCom));
        final DiscoveredScpDomainInfo domainInfoNonTlsEricssonCom = new DiscoveredScpDomainInfo().withName("ericsson.com-non-tls")
                                                                                                 .withDomain("ericsson.com")
                                                                                                 .withCapacity(p.getCapacity())
                                                                                                 .withPriority(p.getPriority())
                                                                                                 .withAddress(addrNonTlsEricssonCom);

        final MultipleIpEndpoint epTlsEricssonCom = new MultipleIpEndpoint().withName("TCP-50446")
                                                                            .withTransport(Transport.TCP)
                                                                            .withIpv4Address(List.of("1.1.1.4"))
                                                                            .withPort(50446);
        final Address addrTlsEricssonCom = new Address().withScheme(Scheme.HTTPS).withMultipleIpEndpoint(List.of(epTlsEricssonCom));
        final DiscoveredScpDomainInfo domainInfoTlsEricssonCom = new DiscoveredScpDomainInfo().withName("ericsson.com-tls")
                                                                                              .withDomain("ericsson.com")
                                                                                              .withCapacity(p.getCapacity())
                                                                                              .withPriority(p.getPriority())
                                                                                              .withAddress(addrTlsEricssonCom);

        final String expected = toJson(List.of(domainInfoTlsEricssonSe, domainInfoTlsEricssonDe, domainInfoNonTlsEricssonCom, domainInfoTlsEricssonCom)
                                           .stream()
                                           .collect(Collectors.toCollection(TreeSet::new)));

        final String actual = toJson(NnrfNfDiscovery.createScpDomainInfoList(p));

        log.info("act: {}", actual);
        log.info("exp: {}", expected);

        Assertions.assertTrue(actual.equals(expected));
    }

    @Test
    void testCreateScpDomainInfoList_11() throws IOException
    {
        final ScpDomainInfo scpDomainInfoEricssonSe = new ScpDomainInfo().scpFqdn("scp.ericsson.se").scpPorts(Map.of("https", 50444));

        final ScpDomainInfo scpDomainInfoEricssonDe = new ScpDomainInfo().scpFqdn("scp.ericsson.de")
                                                                         .scpIpEndPoints(List.of(new IpEndPoint().ipv4Address("1.1.1.3")))
                                                                         .scpPorts(Map.of("https", 50444));

        final ScpDomainInfo scpDomainInfoEricssonCom = new ScpDomainInfo().scpIpEndPoints(List.of(new IpEndPoint().ipv4Address("1.1.1.4")))
                                                                          .scpPorts(Map.of("http", 50445, "https", 50446));

        final ScpInfo scpInfo = new ScpInfo().scpDomainInfoList(Map.of("ericsson.se",
                                                                       scpDomainInfoEricssonSe,
                                                                       "ericsson.de",
                                                                       scpDomainInfoEricssonDe,
                                                                       "ericsson.com",
                                                                       scpDomainInfoEricssonCom));
        final NFProfile p = new NFProfile().capacity(10)
                                           .priority(11)
                                           .nfType(NFType.SCP)
                                           .nfStatus(NFStatus.REGISTERED)
                                           .fqdn("scp.ericsson.com")
                                           .ipv4Addresses(List.of("1.1.1.1", "1.1.1.2"))
                                           .scpDomains(List.of("ericsson.se", "ericsson.com", "ericsson.de", "ericsson.dk", "ericsson.be"))
                                           .scpInfo(scpInfo);

        final MultipleIpEndpoint epTlsEricssonSe = new MultipleIpEndpoint().withName("TCP-50444").withTransport(Transport.TCP).withPort(50444);
        final Address addrTlsEricssonSe = new Address().withFqdn("scp.ericsson.se").withScheme(Scheme.HTTPS).withMultipleIpEndpoint(List.of(epTlsEricssonSe));
        final DiscoveredScpDomainInfo domainInfoTlsEricssonSe = new DiscoveredScpDomainInfo().withName("ericsson.se-tls")
                                                                                             .withDomain("ericsson.se")
                                                                                             .withCapacity(p.getCapacity())
                                                                                             .withPriority(p.getPriority())
                                                                                             .withAddress(addrTlsEricssonSe);

        final MultipleIpEndpoint epTlsEricssonDe = new MultipleIpEndpoint().withName("TCP-50444")
                                                                           .withTransport(Transport.TCP)
                                                                           .withIpv4Address(List.of("1.1.1.3"))
                                                                           .withPort(50444);
        final Address addrTlsEricssonDe = new Address().withFqdn("scp.ericsson.de").withScheme(Scheme.HTTPS).withMultipleIpEndpoint(List.of(epTlsEricssonDe));
        final DiscoveredScpDomainInfo domainInfoTlsEricssonDe = new DiscoveredScpDomainInfo().withName("ericsson.de-tls")
                                                                                             .withDomain("ericsson.de")
                                                                                             .withCapacity(p.getCapacity())
                                                                                             .withPriority(p.getPriority())
                                                                                             .withAddress(addrTlsEricssonDe);

        final MultipleIpEndpoint epNonTlsEricssonCom = new MultipleIpEndpoint().withName("TCP-50445")
                                                                               .withTransport(Transport.TCP)
                                                                               .withIpv4Address(List.of("1.1.1.4"))
                                                                               .withPort(50445);
        final Address addrNonTlsEricssonCom = new Address().withScheme(Scheme.HTTP).withMultipleIpEndpoint(List.of(epNonTlsEricssonCom));
        final DiscoveredScpDomainInfo domainInfoNonTlsEricssonCom = new DiscoveredScpDomainInfo().withName("ericsson.com-non-tls")
                                                                                                 .withDomain("ericsson.com")
                                                                                                 .withCapacity(p.getCapacity())
                                                                                                 .withPriority(p.getPriority())
                                                                                                 .withAddress(addrNonTlsEricssonCom);

        final MultipleIpEndpoint epTlsEricssonCom = new MultipleIpEndpoint().withName("TCP-50446")
                                                                            .withTransport(Transport.TCP)
                                                                            .withIpv4Address(List.of("1.1.1.4"))
                                                                            .withPort(50446);
        final Address addrTlsEricssonCom = new Address().withScheme(Scheme.HTTPS).withMultipleIpEndpoint(List.of(epTlsEricssonCom));
        final DiscoveredScpDomainInfo domainInfoTlsEricssonCom = new DiscoveredScpDomainInfo().withName("ericsson.com-tls")
                                                                                              .withDomain("ericsson.com")
                                                                                              .withCapacity(p.getCapacity())
                                                                                              .withPriority(p.getPriority())
                                                                                              .withAddress(addrTlsEricssonCom);

        final MultipleIpEndpoint epNonTls = new MultipleIpEndpoint().withName("TCP-80")
                                                                    .withTransport(Transport.TCP)
                                                                    .withIpv4Address(p.getIpv4Addresses())
                                                                    .withPort(80);
        final Address addrNonTls = new Address().withFqdn(p.getFqdn()).withScheme(Scheme.HTTP).withMultipleIpEndpoint(List.of(epNonTls));
        final DiscoveredScpDomainInfo domainInfoNonTls = new DiscoveredScpDomainInfo().withName("default-non-tls")
                                                                                      .withDomain(null)
                                                                                      .withCapacity(p.getCapacity())
                                                                                      .withPriority(p.getPriority())
                                                                                      .withAddress(addrNonTls);

        final String expected = toJson(List.of(domainInfoTlsEricssonSe,
                                               domainInfoTlsEricssonDe,
                                               domainInfoNonTlsEricssonCom,
                                               domainInfoTlsEricssonCom,
                                               domainInfoNonTls)
                                           .stream()
                                           .collect(Collectors.toCollection(TreeSet::new)));

        final String actual = toJson(NnrfNfDiscovery.createScpDomainInfoList(p));

        log.info("act: {}", actual);
        log.info("exp: {}", expected);

        Assertions.assertTrue(actual.equals(expected));
    }

    @Test
    void testCreateScpDomainInfoList_12() throws IOException
    {
        final ScpDomainInfo scpDomainInfoEricssonSe = new ScpDomainInfo().scpFqdn("scp.ericsson.se").scpPorts(Map.of("https", 50444));

        final ScpDomainInfo scpDomainInfoEricssonDe = new ScpDomainInfo().scpFqdn("scp.ericsson.de")
                                                                         .scpIpEndPoints(List.of(new IpEndPoint().ipv4Address("1.1.1.3")))
                                                                         .scpPorts(Map.of("https", 50444));

        final ScpDomainInfo scpDomainInfoEricssonCom = new ScpDomainInfo().scpIpEndPoints(List.of(new IpEndPoint().ipv4Address("1.1.1.4")))
                                                                          .scpPorts(Map.of("http", 50445, "https", 50446));

        final ScpInfo scpInfo = new ScpInfo().scpPorts(Map.of("https", 50443))
                                             .scpDomainInfoList(Map.of("ericsson.se",
                                                                       scpDomainInfoEricssonSe,
                                                                       "ericsson.de",
                                                                       scpDomainInfoEricssonDe,
                                                                       "ericsson.com",
                                                                       scpDomainInfoEricssonCom));
        final NFProfile p = new NFProfile().capacity(10)
                                           .priority(11)
                                           .nfType(NFType.SCP)
                                           .nfStatus(NFStatus.REGISTERED)
                                           .fqdn("scp.ericsson.com")
                                           .ipv4Addresses(List.of("1.1.1.1", "1.1.1.2"))
                                           .scpDomains(List.of("ericsson.se", "ericsson.com", "ericsson.de", "ericsson.dk", "ericsson.be"))
                                           .scpInfo(scpInfo);

        final MultipleIpEndpoint epTlsEricssonSe = new MultipleIpEndpoint().withName("TCP-50444").withTransport(Transport.TCP).withPort(50444);
        final Address addrTlsEricssonSe = new Address().withFqdn("scp.ericsson.se").withScheme(Scheme.HTTPS).withMultipleIpEndpoint(List.of(epTlsEricssonSe));
        final DiscoveredScpDomainInfo domainInfoTlsEricssonSe = new DiscoveredScpDomainInfo().withName("ericsson.se-tls")
                                                                                             .withDomain("ericsson.se")
                                                                                             .withCapacity(p.getCapacity())
                                                                                             .withPriority(p.getPriority())
                                                                                             .withAddress(addrTlsEricssonSe);

        final MultipleIpEndpoint epTlsEricssonDe = new MultipleIpEndpoint().withName("TCP-50444")
                                                                           .withTransport(Transport.TCP)
                                                                           .withIpv4Address(List.of("1.1.1.3"))
                                                                           .withPort(50444);
        final Address addrTlsEricssonDe = new Address().withFqdn("scp.ericsson.de").withScheme(Scheme.HTTPS).withMultipleIpEndpoint(List.of(epTlsEricssonDe));
        final DiscoveredScpDomainInfo domainInfoTlsEricssonDe = new DiscoveredScpDomainInfo().withName("ericsson.de-tls")
                                                                                             .withDomain("ericsson.de")
                                                                                             .withCapacity(p.getCapacity())
                                                                                             .withPriority(p.getPriority())
                                                                                             .withAddress(addrTlsEricssonDe);

        final MultipleIpEndpoint epNonTlsEricssonCom = new MultipleIpEndpoint().withName("TCP-50445")
                                                                               .withTransport(Transport.TCP)
                                                                               .withIpv4Address(List.of("1.1.1.4"))
                                                                               .withPort(50445);
        final Address addrNonTlsEricssonCom = new Address().withScheme(Scheme.HTTP).withMultipleIpEndpoint(List.of(epNonTlsEricssonCom));
        final DiscoveredScpDomainInfo domainInfoNonTlsEricssonCom = new DiscoveredScpDomainInfo().withName("ericsson.com-non-tls")
                                                                                                 .withDomain("ericsson.com")
                                                                                                 .withCapacity(p.getCapacity())
                                                                                                 .withPriority(p.getPriority())
                                                                                                 .withAddress(addrNonTlsEricssonCom);

        final MultipleIpEndpoint epTlsEricssonCom = new MultipleIpEndpoint().withName("TCP-50446")
                                                                            .withTransport(Transport.TCP)
                                                                            .withIpv4Address(List.of("1.1.1.4"))
                                                                            .withPort(50446);
        final Address addrTlsEricssonCom = new Address().withScheme(Scheme.HTTPS).withMultipleIpEndpoint(List.of(epTlsEricssonCom));
        final DiscoveredScpDomainInfo domainInfoTlsEricssonCom = new DiscoveredScpDomainInfo().withName("ericsson.com-tls")
                                                                                              .withDomain("ericsson.com")
                                                                                              .withCapacity(p.getCapacity())
                                                                                              .withPriority(p.getPriority())
                                                                                              .withAddress(addrTlsEricssonCom);

        final MultipleIpEndpoint epTls = new MultipleIpEndpoint().withName("TCP-50443")
                                                                 .withTransport(Transport.TCP)
                                                                 .withIpv4Address(p.getIpv4Addresses())
                                                                 .withPort(50443);
        final Address addrTls = new Address().withFqdn(p.getFqdn()).withScheme(Scheme.HTTPS).withMultipleIpEndpoint(List.of(epTls));
        final DiscoveredScpDomainInfo domainInfoTls = new DiscoveredScpDomainInfo().withName("default-tls")
                                                                                   .withDomain(null)
                                                                                   .withCapacity(p.getCapacity())
                                                                                   .withPriority(p.getPriority())
                                                                                   .withAddress(addrTls);

        final String expected = toJson(List.of(domainInfoTlsEricssonSe,
                                               domainInfoTlsEricssonDe,
                                               domainInfoNonTlsEricssonCom,
                                               domainInfoTlsEricssonCom,
                                               domainInfoTls)
                                           .stream()
                                           .collect(Collectors.toCollection(TreeSet::new)));

        final String actual = toJson(NnrfNfDiscovery.createScpDomainInfoList(p));

        log.info("act: {}", actual);
        log.info("exp: {}", expected);

        Assertions.assertTrue(actual.equals(expected));
    }

    @Test
    void testCreateScpDomainInfoList_13() throws IOException
    {
        final ScpDomainInfo scpDomainInfoEricssonSe = new ScpDomainInfo().scpFqdn("scp.ericsson.se").scpPorts(Map.of("https", 50444));

        final ScpDomainInfo scpDomainInfoEricssonDe = new ScpDomainInfo().scpFqdn("scp.ericsson.de")
                                                                         .scpIpEndPoints(List.of(new IpEndPoint().ipv4Address("1.1.1.3")))
                                                                         .scpPorts(Map.of("https", 50444));

        final ScpDomainInfo scpDomainInfoEricssonCom = new ScpDomainInfo().scpIpEndPoints(List.of(new IpEndPoint().ipv4Address("1.1.1.4")))
                                                                          .scpPorts(Map.of("http", 50445, "https", 50445));

        final ScpInfo scpInfo = new ScpInfo().scpPorts(Map.of("https", 50443))
                                             .scpDomainInfoList(Map.of("ericsson.se",
                                                                       scpDomainInfoEricssonSe,
                                                                       "ericsson.de",
                                                                       scpDomainInfoEricssonDe,
                                                                       "ericsson.com",
                                                                       scpDomainInfoEricssonCom));
        final NFProfile p = new NFProfile().capacity(10)
                                           .priority(11)
                                           .nfType(NFType.SCP)
                                           .nfStatus(NFStatus.REGISTERED)
                                           .fqdn("scp.ericsson.com")
                                           .ipv4Addresses(List.of("1.1.1.1", "1.1.1.2"))
                                           .scpDomains(List.of("ericsson.se", "ericsson.com", "ericsson.de", "ericsson.dk", "ericsson.be"))
                                           .scpInfo(scpInfo);

        final MultipleIpEndpoint epTlsEricssonSe = new MultipleIpEndpoint().withName("TCP-50444").withTransport(Transport.TCP).withPort(50444);
        final Address addrTlsEricssonSe = new Address().withFqdn("scp.ericsson.se").withScheme(Scheme.HTTPS).withMultipleIpEndpoint(List.of(epTlsEricssonSe));
        final DiscoveredScpDomainInfo domainInfoTlsEricssonSe = new DiscoveredScpDomainInfo().withName("ericsson.se-tls")
                                                                                             .withDomain("ericsson.se")
                                                                                             .withCapacity(p.getCapacity())
                                                                                             .withPriority(p.getPriority())
                                                                                             .withAddress(addrTlsEricssonSe);

        final MultipleIpEndpoint epTlsEricssonDe = new MultipleIpEndpoint().withName("TCP-50444")
                                                                           .withTransport(Transport.TCP)
                                                                           .withIpv4Address(List.of("1.1.1.3"))
                                                                           .withPort(50444);
        final Address addrTlsEricssonDe = new Address().withFqdn("scp.ericsson.de").withScheme(Scheme.HTTPS).withMultipleIpEndpoint(List.of(epTlsEricssonDe));
        final DiscoveredScpDomainInfo domainInfoTlsEricssonDe = new DiscoveredScpDomainInfo().withName("ericsson.de-tls")
                                                                                             .withDomain("ericsson.de")
                                                                                             .withCapacity(p.getCapacity())
                                                                                             .withPriority(p.getPriority())
                                                                                             .withAddress(addrTlsEricssonDe);

        final MultipleIpEndpoint epNonTlsEricssonCom = new MultipleIpEndpoint().withName("TCP-50445")
                                                                               .withTransport(Transport.TCP)
                                                                               .withIpv4Address(List.of("1.1.1.4"))
                                                                               .withPort(50445);
        final Address addrNonTlsEricssonCom = new Address().withScheme(Scheme.HTTP).withMultipleIpEndpoint(List.of(epNonTlsEricssonCom));
        final DiscoveredScpDomainInfo domainInfoNonTlsEricssonCom = new DiscoveredScpDomainInfo().withName("ericsson.com-non-tls")
                                                                                                 .withDomain("ericsson.com")
                                                                                                 .withCapacity(p.getCapacity())
                                                                                                 .withPriority(p.getPriority())
                                                                                                 .withAddress(addrNonTlsEricssonCom);

        final MultipleIpEndpoint epTlsEricssonCom = new MultipleIpEndpoint().withName("TCP-50445")
                                                                            .withTransport(Transport.TCP)
                                                                            .withIpv4Address(List.of("1.1.1.4"))
                                                                            .withPort(50445);
        final Address addrTlsEricssonCom = new Address().withScheme(Scheme.HTTPS).withMultipleIpEndpoint(List.of(epTlsEricssonCom));
        final DiscoveredScpDomainInfo domainInfoTlsEricssonCom = new DiscoveredScpDomainInfo().withName("ericsson.com-tls")
                                                                                              .withDomain("ericsson.com")
                                                                                              .withCapacity(p.getCapacity())
                                                                                              .withPriority(p.getPriority())
                                                                                              .withAddress(addrTlsEricssonCom);

        final MultipleIpEndpoint epTls = new MultipleIpEndpoint().withName("TCP-50443")
                                                                 .withTransport(Transport.TCP)
                                                                 .withIpv4Address(p.getIpv4Addresses())
                                                                 .withPort(50443);
        final Address addrTls = new Address().withFqdn(p.getFqdn()).withScheme(Scheme.HTTPS).withMultipleIpEndpoint(List.of(epTls));
        final DiscoveredScpDomainInfo domainInfoTls = new DiscoveredScpDomainInfo().withName("default-tls")
                                                                                   .withDomain(null)
                                                                                   .withCapacity(p.getCapacity())
                                                                                   .withPriority(p.getPriority())
                                                                                   .withAddress(addrTls);

        final String expected = toJson(List.of(domainInfoTlsEricssonSe,
                                               domainInfoTlsEricssonDe,
                                               domainInfoNonTlsEricssonCom,
                                               domainInfoTlsEricssonCom,
                                               domainInfoTls)
                                           .stream()
                                           .collect(Collectors.toCollection(TreeSet::new)));

        final String actual = toJson(NnrfNfDiscovery.createScpDomainInfoList(p));

        log.info("act: {}", actual);
        log.info("exp: {}", expected);

        Assertions.assertTrue(actual.equals(expected));
    }

    @Test
    void testCreateScpDomainInfoList_14() throws IOException
    {
        final ScpDomainInfo scpDomainInfoEricssonSe = new ScpDomainInfo().scpFqdn("scp.ericsson.se").scpPorts(Map.of("https", 50444));

        final ScpDomainInfo scpDomainInfoEricssonDe = new ScpDomainInfo().scpFqdn("scp.ericsson.de")
                                                                         .scpIpEndPoints(List.of(new IpEndPoint().ipv4Address("1.1.1.3")))
                                                                         .scpPorts(Map.of("https", 50444));

        final ScpDomainInfo scpDomainInfoEricssonCom = new ScpDomainInfo().scpIpEndPoints(List.of(new IpEndPoint().ipv4Address("1.1.1.4").port(50445)));

        final ScpInfo scpInfo = new ScpInfo().scpPorts(Map.of("https", 50443))
                                             .scpDomainInfoList(Map.of("ericsson.se",
                                                                       scpDomainInfoEricssonSe,
                                                                       "ericsson.de",
                                                                       scpDomainInfoEricssonDe,
                                                                       "ericsson.com",
                                                                       scpDomainInfoEricssonCom));
        final NFProfile p = new NFProfile().capacity(10)
                                           .priority(11)
                                           .nfType(NFType.SCP)
                                           .nfStatus(NFStatus.REGISTERED)
                                           .fqdn("scp.ericsson.com")
                                           .ipv4Addresses(List.of("1.1.1.1", "1.1.1.2"))
                                           .scpDomains(List.of("ericsson.se", "ericsson.com", "ericsson.de", "ericsson.dk", "ericsson.be"))
                                           .scpInfo(scpInfo);

        final MultipleIpEndpoint epTlsEricssonSe = new MultipleIpEndpoint().withName("TCP-50444").withTransport(Transport.TCP).withPort(50444);
        final Address addrTlsEricssonSe = new Address().withFqdn("scp.ericsson.se").withScheme(Scheme.HTTPS).withMultipleIpEndpoint(List.of(epTlsEricssonSe));
        final DiscoveredScpDomainInfo domainInfoTlsEricssonSe = new DiscoveredScpDomainInfo().withName("ericsson.se-tls")
                                                                                             .withDomain("ericsson.se")
                                                                                             .withCapacity(p.getCapacity())
                                                                                             .withPriority(p.getPriority())
                                                                                             .withAddress(addrTlsEricssonSe);

        final MultipleIpEndpoint epTlsEricssonDe = new MultipleIpEndpoint().withName("TCP-50444")
                                                                           .withTransport(Transport.TCP)
                                                                           .withIpv4Address(List.of("1.1.1.3"))
                                                                           .withPort(50444);
        final Address addrTlsEricssonDe = new Address().withFqdn("scp.ericsson.de").withScheme(Scheme.HTTPS).withMultipleIpEndpoint(List.of(epTlsEricssonDe));
        final DiscoveredScpDomainInfo domainInfoTlsEricssonDe = new DiscoveredScpDomainInfo().withName("ericsson.de-tls")
                                                                                             .withDomain("ericsson.de")
                                                                                             .withCapacity(p.getCapacity())
                                                                                             .withPriority(p.getPriority())
                                                                                             .withAddress(addrTlsEricssonDe);

        final MultipleIpEndpoint epNonTlsEricssonCom = new MultipleIpEndpoint().withName("TCP-50445")
                                                                               .withTransport(Transport.TCP)
                                                                               .withIpv4Address(List.of("1.1.1.4"))
                                                                               .withPort(50445);
        final Address addrNonTlsEricssonCom = new Address().withScheme(Scheme.HTTP).withMultipleIpEndpoint(List.of(epNonTlsEricssonCom));
        final DiscoveredScpDomainInfo domainInfoNonTlsEricssonCom = new DiscoveredScpDomainInfo().withName("ericsson.com-non-tls")
                                                                                                 .withDomain("ericsson.com")
                                                                                                 .withCapacity(p.getCapacity())
                                                                                                 .withPriority(p.getPriority())
                                                                                                 .withAddress(addrNonTlsEricssonCom);

        final MultipleIpEndpoint epTlsEricssonCom = new MultipleIpEndpoint().withName("TCP-50445")
                                                                            .withTransport(Transport.TCP)
                                                                            .withIpv4Address(List.of("1.1.1.4"))
                                                                            .withPort(50445);
        final Address addrTlsEricssonCom = new Address().withScheme(Scheme.HTTPS).withMultipleIpEndpoint(List.of(epTlsEricssonCom));
        final DiscoveredScpDomainInfo domainInfoTlsEricssonCom = new DiscoveredScpDomainInfo().withName("ericsson.com-tls")
                                                                                              .withDomain("ericsson.com")
                                                                                              .withCapacity(p.getCapacity())
                                                                                              .withPriority(p.getPriority())
                                                                                              .withAddress(addrTlsEricssonCom);

        final MultipleIpEndpoint epTls = new MultipleIpEndpoint().withName("TCP-50443")
                                                                 .withTransport(Transport.TCP)
                                                                 .withIpv4Address(p.getIpv4Addresses())
                                                                 .withPort(50443);
        final Address addrTls = new Address().withFqdn(p.getFqdn()).withScheme(Scheme.HTTPS).withMultipleIpEndpoint(List.of(epTls));
        final DiscoveredScpDomainInfo domainInfoTls = new DiscoveredScpDomainInfo().withName("default-tls")
                                                                                   .withDomain(null)
                                                                                   .withCapacity(p.getCapacity())
                                                                                   .withPriority(p.getPriority())
                                                                                   .withAddress(addrTls);

        final String expected = toJson(List.of(domainInfoTlsEricssonSe,
                                               domainInfoTlsEricssonDe,
                                               domainInfoNonTlsEricssonCom,
                                               domainInfoTlsEricssonCom,
                                               domainInfoTls)
                                           .stream()
                                           .collect(Collectors.toCollection(TreeSet::new)));

        final String actual = toJson(NnrfNfDiscovery.createScpDomainInfoList(p));

        log.info("act: {}", actual);
        log.info("exp: {}", expected);

        Assertions.assertTrue(actual.equals(expected));
    }

    /**
     * No scp-domains; no scp-info -> take information from nf-profile:
     * 
     * <pre>
     * nf-profile
     *   nf-type = SCP
     *   ipv4-addresses = [1.1.1.1]
     *   scp-domains = null
     *   scp-info = null
     * 
     * "discovered-scp-domain-info":
     * [
     *   {
     *     "name": "default-non-tls",
     *     "address": {
     *       "scheme": "http",
     *       "multiple-ip-endpoint": [
     *         {
     *           "name": "TCP-80",
     *           "port": 80,
     *           "ipv4-address": [
     *             "1.1.1.1"
     *           ],
     *         }
     *       ]
     *     }
     *   }
     * ]
     * </pre>
     */
    @Test
    void testCreateScpDomainInfoList_100() throws IOException
    {
        final NFProfile p = new NFProfile().capacity(10)
                                           .priority(11)
                                           .nfType(NFType.SCP)
                                           .nfStatus(NFStatus.REGISTERED)
                                           .fqdn("scp.ericsson.com")
                                           .ipv4Addresses(List.of("1.1.1.1"));

        final MultipleIpEndpoint epNonTlsDefault = new MultipleIpEndpoint().withName("TCP-80")
                                                                           .withTransport(Transport.TCP)
                                                                           .withIpv4Address(List.of("1.1.1.1"))
                                                                           .withPort(80);
        final Address addrNonTlsDefault = new Address().withFqdn("scp.ericsson.com").withScheme(Scheme.HTTP).withMultipleIpEndpoint(List.of(epNonTlsDefault));
        final DiscoveredScpDomainInfo domainInfoNonTlsDefault = new DiscoveredScpDomainInfo().withName("default-non-tls")
                                                                                             .withDomain(null)
                                                                                             .withCapacity(p.getCapacity())
                                                                                             .withPriority(p.getPriority())
                                                                                             .withAddress(addrNonTlsDefault);

        final String expected = toJson(List.of(domainInfoNonTlsDefault).stream().collect(Collectors.toCollection(TreeSet::new)));

        final String actual = toJson(NnrfNfDiscovery.createScpDomainInfoList(p));

        log.info("act: {}", actual);
        log.info("exp: {}", expected);

        Assertions.assertTrue(actual.equals(expected));
    }

    /**
     * No scp-domains; scp-info; no scp-domain-info -> take information from
     * scp-info or nf-profile:
     * 
     * <pre>
     * nf-profile =
     *   nf-type = SCP
     *   fqdn = scp.ericsson.com
     *   ipv4-addresses = [1.1.1.1]
     *   scp-domains = null
     *   scp-info = 
     *     scp-ports = [http, 8080]
     *     scp-domain-info-list = null
     * 
     * "discovered-scp-domain-info":
     * [
     *   {
     *     "name": "default-non-tls",
     *     "address": {
     *       "scheme": "http",
     *       "fqdn": "scp.ericsson.com",
     *       "multiple-ip-endpoint": [
     *         {
     *           "name": "TCP-8080",
     *           "port": 8080,
     *           "ipv4-address": [
     *             "1.1.1.1"
     *           ],
     *         }
     *       ]
     *     }
     *   }
     * ]
     * </pre>
     */
    @Test
    void testCreateScpDomainInfoList_101() throws IOException
    {
        final ScpInfo scpInfo = new ScpInfo().scpPorts(Map.of("http", 8080));

        final NFProfile p = new NFProfile().capacity(10)
                                           .priority(11)
                                           .nfType(NFType.SCP)
                                           .nfStatus(NFStatus.REGISTERED)
                                           .fqdn("scp.ericsson.com")
                                           .ipv4Addresses(List.of("1.1.1.1"))
                                           .scpInfo(scpInfo);

        final MultipleIpEndpoint epNonTlsDefault = new MultipleIpEndpoint().withName("TCP-8080")
                                                                           .withTransport(Transport.TCP)
                                                                           .withIpv4Address(List.of("1.1.1.1"))
                                                                           .withPort(8080);
        final Address addrNonTlsDefault = new Address().withFqdn("scp.ericsson.com").withScheme(Scheme.HTTP).withMultipleIpEndpoint(List.of(epNonTlsDefault));
        final DiscoveredScpDomainInfo domainInfoNonTlsDefault = new DiscoveredScpDomainInfo().withName("default-non-tls")
                                                                                             .withDomain(null)
                                                                                             .withCapacity(p.getCapacity())
                                                                                             .withPriority(p.getPriority())
                                                                                             .withAddress(addrNonTlsDefault);

        final String expected = toJson(List.of(domainInfoNonTlsDefault).stream().collect(Collectors.toCollection(TreeSet::new)));

        final String actual = toJson(NnrfNfDiscovery.createScpDomainInfoList(p));

        log.info("act: {}", actual);
        log.info("exp: {}", expected);

        Assertions.assertTrue(actual.equals(expected));
    }

    /**
     * scp-domains; no scp-info -> take information from nf-profile. As all
     * discovered-scp-domain-infos would look the same, just generate a default
     * discovered-scp-domain-info:
     * 
     * <pre>
     * nf-profile
     *   nf-type = SCP
     *   fqdn = scp.ericsson.com
     *   ipv4-addresses = [1.1.1.1]
     *   scp-domains = [a.com, b.com]
     *   scp-info = null
     * 
     * "discovered-scp-domain-info":
     * [
     *   {
     *     "name": "default-non-tls",
     *     "capacity": 10,
     *     "priority": 11,
     *     "address": {
     *       "scheme": "http",
     *       "fqdn": "scp.ericsson.com",
     *       "multiple-ip-endpoint": [
     *         {
     *           "name": "TCP-80",
     *           "transport": "tcp",
     *           "port": 80,
     *           "ipv4-address": [
     *             "1.1.1.1"
     *           ],
     *           "ipv6-address": []
     *         }
     *       ]
     *     }
     *   }
     * ]
     * </pre>
     */
    @Test
    void testCreateScpDomainInfoList_102() throws IOException
    {
        final NFProfile p = new NFProfile().capacity(10)
                                           .priority(11)
                                           .nfType(NFType.SCP)
                                           .nfStatus(NFStatus.REGISTERED)
                                           .fqdn("scp.ericsson.com")
                                           .ipv4Addresses(List.of("1.1.1.1"))
                                           .scpDomains(List.of("a.com", "b.com"));

        final MultipleIpEndpoint epNonTlsDefault = new MultipleIpEndpoint().withName("TCP-80")
                                                                           .withTransport(Transport.TCP)
                                                                           .withIpv4Address(List.of("1.1.1.1"))
                                                                           .withPort(80);
        final Address addrNonTlsDefault = new Address().withFqdn("scp.ericsson.com").withScheme(Scheme.HTTP).withMultipleIpEndpoint(List.of(epNonTlsDefault));
        final DiscoveredScpDomainInfo domainInfoNonTlsDefault = new DiscoveredScpDomainInfo().withName("default-non-tls")
                                                                                             .withDomain(null)
                                                                                             .withCapacity(p.getCapacity())
                                                                                             .withPriority(p.getPriority())
                                                                                             .withAddress(addrNonTlsDefault);

        final String expected = toJson(List.of(domainInfoNonTlsDefault).stream().collect(Collectors.toCollection(TreeSet::new)));

        final String actual = toJson(NnrfNfDiscovery.createScpDomainInfoList(p));

        log.info("act: {}", actual);
        log.info("exp: {}", expected);

        Assertions.assertTrue(actual.equals(expected));
    }

    /**
     * scp-domains; scp-info; no scp-domain-info -> take information from scp-info
     * or nf-profile. As all discovered-scp-domain-infos would look the same, just
     * generate a default discovered-scp-domain-info:
     * 
     * <pre>
     * nf-profile =
     *   nf-type = SCP
     *   fqdn = scp.ericsson.com
     *   ipv4-addresses = [1.1.1.1]
     *   scp-domains = [a.com, b.com]
     *   scp-info = 
     *     scp-ports = [http, 8080]
     *     scp-domain-info-list = null
     * 
     * "discovered-scp-domain-info":
     * [
     *   {
     *     "name": "default-non-tls",
     *     "capacity": 10,
     *     "priority": 11,
     *     "address": {
     *       "scheme": "http",
     *       "fqdn": "scp.ericsson.com",
     *       "multiple-ip-endpoint": [
     *         {
     *           "name": "TCP-8080",
     *           "transport": "tcp",
     *           "port": 8080,
     *           "ipv4-address": [
     *             "1.1.1.1"
     *           ],
     *           "ipv6-address": []
     *         }
     *       ]
     *     }
     *   }
     * ]
     * </pre>
     */
    @Test
    void testCreateScpDomainInfoList_103() throws IOException
    {
        final ScpInfo scpInfo = new ScpInfo().scpPorts(Map.of("http", 8080));

        final NFProfile p = new NFProfile().capacity(10)
                                           .priority(11)
                                           .nfType(NFType.SCP)
                                           .nfStatus(NFStatus.REGISTERED)
                                           .fqdn("scp.ericsson.com")
                                           .ipv4Addresses(List.of("1.1.1.1"))
                                           .scpDomains(List.of("a.com", "b.com"))
                                           .scpInfo(scpInfo);

        final MultipleIpEndpoint epNonTls = new MultipleIpEndpoint().withName("TCP-8080")
                                                                    .withTransport(Transport.TCP)
                                                                    .withIpv4Address(List.of("1.1.1.1"))
                                                                    .withPort(8080);
        final Address addrNonTlsDefault = new Address().withFqdn("scp.ericsson.com").withScheme(Scheme.HTTP).withMultipleIpEndpoint(List.of(epNonTls));
        final DiscoveredScpDomainInfo domainInfoNonTlsDefault = new DiscoveredScpDomainInfo().withName("default-non-tls")
                                                                                             .withDomain(null)
                                                                                             .withCapacity(p.getCapacity())
                                                                                             .withPriority(p.getPriority())
                                                                                             .withAddress(addrNonTlsDefault);

        final String expected = toJson(List.of(domainInfoNonTlsDefault).stream().collect(Collectors.toCollection(TreeSet::new)));

        final String actual = toJson(NnrfNfDiscovery.createScpDomainInfoList(p));

        log.info("act: {}", actual);
        log.info("exp: {}", expected);

        Assertions.assertTrue(actual.equals(expected));
    }

    /**
     * scp-domains; scp-info; scp-domain-info -> take information from
     * scp-domain-info or scp-info or nf-profile. Generate scp-domain specific
     * discovered-scp-domain-infos only for those scp-domains that have a
     * scp-domain-info in scp-info. For all the others, generate just one default
     * discovered-scp-domain-info, as they would all look the same:
     * 
     * <pre>
     * nf-profile =
     *   nf-type = SCP
     *   fqdn = scp.ericsson.com
     *   ipv4-addresses = [1.1.1.1]
     *   scp-domains = [a.com, b.com]
     *   scp-info = 
     *     scp-ports = [http, 8080]
     *     scp-domain-info-list = [a.com, [https, 8443]]
     *       
     * 
     * "discovered-scp-domain-info":
     * [
     *   {
     *     "name": "a.com-tls",
     *     "domain": "a.com",
     *     "capacity": 10,
     *     "priority": 11,
     *     "address": {
     *       "scheme": "https",
     *       "fqdn": "scp.ericsson.com",
     *       "multiple-ip-endpoint": [
     *         {
     *           "name": "TCP-8443",
     *           "transport": "tcp",
     *           "port": 8443,
     *           "ipv4-address": [
     *             "1.1.1.1"
     *           ],
     *           "ipv6-address": []
     *         }
     *       ]
     *     }
     *   },
     *   {
     *     "name": "default-non-tls",
     *     "capacity": 10,
     *     "priority": 11,
     *     "address": {
     *       "scheme": "http",
     *       "fqdn": "scp.ericsson.com",
     *       "multiple-ip-endpoint": [
     *         {
     *           "name": "TCP-8080",
     *           "transport": "tcp",
     *           "port": 8080,
     *           "ipv4-address": [
     *             "1.1.1.1"
     *           ],
     *           "ipv6-address": []
     *         }
     *       ]
     *     }
     *   }
     * ]
     * </pre>
     */
    @Test
    void testCreateScpDomainInfoList_104() throws IOException
    {
        final ScpDomainInfo scpDomainInfoAcom = new ScpDomainInfo().scpPorts(Map.of("https", 8443));
        final ScpInfo scpInfo = new ScpInfo().scpPorts(Map.of("http", 8080)).scpDomainInfoList(Map.of("a.com", scpDomainInfoAcom));

        final NFProfile p = new NFProfile().capacity(10)
                                           .priority(11)
                                           .nfType(NFType.SCP)
                                           .nfStatus(NFStatus.REGISTERED)
                                           .fqdn("scp.ericsson.com")
                                           .ipv4Addresses(List.of("1.1.1.1"))
                                           .scpDomains(List.of("a.com", "b.com"))
                                           .scpInfo(scpInfo);

        final MultipleIpEndpoint epNonTlsAcom = new MultipleIpEndpoint().withName("TCP-8443")
                                                                        .withTransport(Transport.TCP)
                                                                        .withIpv4Address(List.of("1.1.1.1"))
                                                                        .withPort(8443);
        final Address addrTlsAcom = new Address().withFqdn("scp.ericsson.com").withScheme(Scheme.HTTPS).withMultipleIpEndpoint(List.of(epNonTlsAcom));
        final MultipleIpEndpoint epNonTlsDefault = new MultipleIpEndpoint().withName("TCP-8080")
                                                                           .withTransport(Transport.TCP)
                                                                           .withIpv4Address(List.of("1.1.1.1"))
                                                                           .withPort(8080);
        final Address addrNonTlsBcom = new Address().withFqdn("scp.ericsson.com").withScheme(Scheme.HTTP).withMultipleIpEndpoint(List.of(epNonTlsDefault));
        final DiscoveredScpDomainInfo domainInfoTlsAcom = new DiscoveredScpDomainInfo().withName("a.com-tls")
                                                                                       .withDomain("a.com")
                                                                                       .withCapacity(p.getCapacity())
                                                                                       .withPriority(p.getPriority())
                                                                                       .withAddress(addrTlsAcom);
        final DiscoveredScpDomainInfo domainInfoNonTlsBcom = new DiscoveredScpDomainInfo().withName("default-non-tls")
                                                                                          .withDomain(null)
                                                                                          .withCapacity(p.getCapacity())
                                                                                          .withPriority(p.getPriority())
                                                                                          .withAddress(addrNonTlsBcom);

        final String expected = toJson(List.of(domainInfoTlsAcom, domainInfoNonTlsBcom).stream().collect(Collectors.toCollection(TreeSet::new)));

        final String actual = toJson(NnrfNfDiscovery.createScpDomainInfoList(p));

        log.info("act: {}", actual);
        log.info("exp: {}", expected);

        Assertions.assertTrue(actual.equals(expected));
    }

    /**
     * scp-domains; scp-info; scp-domain-info with two scp-ip-endpoints
     * <p>
     * a.com: should result in one discovered-scp-domain-info with two ip-addresses
     * per multiple-ip-endpoint
     * <p>
     * b.com: use defaults from scp-info or nf-profile
     * 
     * <pre>
     * nf-profile =
     *   nf-type = SCP
     *   fqdn = scp.ericsson.com
     *   ipv4-addresses = [1.1.1.1]
     *   scp-domains = [a.com, b.com]
     *   scp-info = 
     *     scp-ports = [http, 8080]
     *     scp-domain-info-list = [a.com, {scp-fqdn = scp.ericsson.com, scp-ip-endpoints = [{1.1.1.2, 8081}, {1.1.1.3, 8082}]}]
     *       
     * 
     * "discovered-scp-domain-info":
     * [
     *   {
     *     "name": "a.com-non-tls",
     *     "domain": "a.com",
     *     "capacity": 10,
     *     "priority": 11,
     *     "address": {
     *       "scheme": "http",
     *       "fqdn": "scp.ericsson.com",
     *       "multiple-ip-endpoint": [
     *         {
     *           "name": "TCP-8081",
     *           "transport": "tcp",
     *           "port": 8081,
     *           "ipv4-address": [
     *             "1.1.1.2"
     *           ],
     *           "ipv6-address": []
     *         },
     *         {
     *           "name": "TCP-8082",
     *           "transport": "tcp",
     *           "port": 8082,
     *           "ipv4-address": [
     *             "1.1.1.3"
     *           ],
     *           "ipv6-address": []
     *         }
     *       ]
     *     }
     *   },
     *   {
     *     "name": "a.com-tls",
     *     "domain": "a.com",
     *     "capacity": 10,
     *     "priority": 11,
     *     "address": {
     *       "scheme": "https",
     *       "fqdn": "scp.ericsson.com",
     *       "multiple-ip-endpoint": [
     *         {
     *           "name": "TCP-8081",
     *           "transport": "tcp",
     *           "port": 8081,
     *           "ipv4-address": [
     *             "1.1.1.2"
     *           ],
     *           "ipv6-address": []
     *         },
     *         {
     *           "name": "TCP-8082",
     *           "transport": "tcp",
     *           "port": 8082,
     *           "ipv4-address": [
     *             "1.1.1.3"
     *           ],
     *           "ipv6-address": []
     *         }
     *       ]
     *     }
     *   },
     *   {
     *     "name": "default-non-tls",
     *     "capacity": 10,
     *     "priority": 11,
     *     "address": {
     *       "scheme": "http",
     *       "fqdn": "scp.ericsson.com",
     *       "multiple-ip-endpoint": [
     *         {
     *           "name": "TCP-8080",
     *           "transport": "tcp",
     *           "port": 8080,
     *           "ipv4-address": [
     *             "1.1.1.1"
     *           ],
     *           "ipv6-address": []
     *         }
     *       ]
     *     }
     *   }
     * ]
     * </pre>
     */
    @Test
    void testCreateScpDomainInfoList_105() throws IOException
    {
        final ScpDomainInfo scpDomainInfoAcom = new ScpDomainInfo().scpFqdn("scp.ericsson.com")
                                                                   .scpIpEndPoints(List.of(new IpEndPoint().ipv4Address("1.1.1.2").port(8081),
                                                                                           new IpEndPoint().ipv4Address("1.1.1.3").port(8081)));

        final ScpInfo scpInfo = new ScpInfo().scpPorts(Map.of("http", 8080)).scpDomainInfoList(Map.of("a.com", scpDomainInfoAcom));

        final NFProfile p = new NFProfile().capacity(10)
                                           .priority(11)
                                           .nfType(NFType.SCP)
                                           .nfStatus(NFStatus.REGISTERED)
                                           .fqdn("scp.ericsson.com")
                                           .ipv4Addresses(List.of("1.1.1.1"))
                                           .scpDomains(List.of("a.com", "b.com"))
                                           .scpInfo(scpInfo);

        final MultipleIpEndpoint ep1Acom = new MultipleIpEndpoint().withName("TCP-8081")
                                                                   .withTransport(Transport.TCP)
                                                                   .withIpv4Address(List.of("1.1.1.2", "1.1.1.3"))
                                                                   .withPort(8081);
        final Address addrNonTlsAcom = new Address().withFqdn("scp.ericsson.com").withScheme(Scheme.HTTP).withMultipleIpEndpoint(List.of(ep1Acom));
        final Address addrTlsAcom = new Address().withFqdn("scp.ericsson.com").withScheme(Scheme.HTTPS).withMultipleIpEndpoint(List.of(ep1Acom));
        final MultipleIpEndpoint epNonTlsDefault = new MultipleIpEndpoint().withName("TCP-8080")
                                                                           .withTransport(Transport.TCP)
                                                                           .withIpv4Address(List.of("1.1.1.1"))
                                                                           .withPort(8080);
        final Address addrNonTlsBcom = new Address().withFqdn("scp.ericsson.com").withScheme(Scheme.HTTP).withMultipleIpEndpoint(List.of(epNonTlsDefault));
        final DiscoveredScpDomainInfo domainInfoNonTlsAcom = new DiscoveredScpDomainInfo().withName("a.com-non-tls")
                                                                                          .withDomain("a.com")
                                                                                          .withCapacity(p.getCapacity())
                                                                                          .withPriority(p.getPriority())
                                                                                          .withAddress(addrNonTlsAcom);
        final DiscoveredScpDomainInfo domainInfoTlsAcom = new DiscoveredScpDomainInfo().withName("a.com-tls")
                                                                                       .withDomain("a.com")
                                                                                       .withCapacity(p.getCapacity())
                                                                                       .withPriority(p.getPriority())
                                                                                       .withAddress(addrTlsAcom);
        final DiscoveredScpDomainInfo domainInfoNonTlsBcom = new DiscoveredScpDomainInfo().withName("default-non-tls")
                                                                                          .withDomain(null)
                                                                                          .withCapacity(p.getCapacity())
                                                                                          .withPriority(p.getPriority())
                                                                                          .withAddress(addrNonTlsBcom);

        final String expected = toJson(List.of(domainInfoNonTlsAcom, domainInfoTlsAcom, domainInfoNonTlsBcom)
                                           .stream()
                                           .collect(Collectors.toCollection(TreeSet::new)));

        final String actual = toJson(NnrfNfDiscovery.createScpDomainInfoList(p));

        log.info("act: {}", actual);
        log.info("exp: {}", expected);

        Assertions.assertTrue(actual.equals(expected));
    }

    /**
     * scp-domains; scp-info; scp-domain-info with two scp-ip-endpoints
     * <p>
     * a.com: should result in one discovered-scp-domain-info with one ip-addresses
     * per multiple-ip-endpoint
     * <p>
     * b.com: use defaults from scp-info or nf-profile
     * 
     * <pre>
     * nf-profile =
     *   nf-type = SCP
     *   fqdn = scp.ericsson.com
     *   ipv4-addresses = [1.1.1.1]
     *   scp-domains = [a.com, b.com]
     *   scp-info = 
     *     scp-ports = [http, 8080]
     *     scp-domain-info-list = [a.com, {scp-fqdn = scp.ericsson.com, scp-ip-endpoints = [{1.1.1.2, 8081}, {1.1.1.3, 8082}, {1.1.1.2, 8082}]}]
     *       
     * 
     * "discovered-scp-domain-info":
     * [
     *   {
     *     "name": "a.com-non-tls",
     *     "domain": "a.com",
     *     "capacity": 10,
     *     "priority": 11,
     *     "address": {
     *       "scheme": "http",
     *       "fqdn": "scp.ericsson.com",
     *       "multiple-ip-endpoint": [
     *         {
     *           "name": "TCP-8081",
     *           "transport": "tcp",
     *           "port": 8081,
     *           "ipv4-address": [
     *             "1.1.1.2"
     *           ],
     *           "ipv6-address": []
     *         },
     *         {
     *           "name": "TCP-8082",
     *           "transport": "tcp",
     *           "port": 8082,
     *           "ipv4-address": [
     *             "1.1.1.3",
     *             "1.1.1.2"
     *           ],
     *           "ipv6-address": []
     *         }
     *       ]
     *     }
     *   },
     *   {
     *     "name": "a.com-tls",
     *     "domain": "a.com",
     *     "capacity": 10,
     *     "priority": 11,
     *     "address": {
     *       "scheme": "https",
     *       "fqdn": "scp.ericsson.com",
     *       "multiple-ip-endpoint": [
     *         {
     *           "name": "TCP-8081",
     *           "transport": "tcp",
     *           "port": 8081,
     *           "ipv4-address": [
     *             "1.1.1.2"
     *           ],
     *           "ipv6-address": []
     *         },
     *         {
     *           "name": "TCP-8082",
     *           "transport": "tcp",
     *           "port": 8082,
     *           "ipv4-address": [
     *             "1.1.1.3",
     *             "1.1.1.2"
     *           ],
     *           "ipv6-address": []
     *         }
     *       ]
     *     }
     *   },
     *   {
     *     "name": "default-non-tls",
     *     "capacity": 10,
     *     "priority": 11,
     *     "address": {
     *       "scheme": "http",
     *       "fqdn": "scp.ericsson.com",
     *       "multiple-ip-endpoint": [
     *         {
     *           "name": "TCP-8080",
     *           "transport": "tcp",
     *           "port": 8080,
     *           "ipv4-address": [
     *             "1.1.1.1"
     *           ],
     *           "ipv6-address": []
     *         }
     *       ]
     *     }
     *   }
     * ]
     * </pre>
     */
    @Test
    void testCreateScpDomainInfoList_106() throws IOException
    {
        final ScpDomainInfo scpDomainInfoAcom = new ScpDomainInfo().scpFqdn("scp.ericsson.com")
                                                                   .scpIpEndPoints(List.of(new IpEndPoint().ipv4Address("1.1.1.2").port(8081),
                                                                                           new IpEndPoint().ipv4Address("1.1.1.3").port(8082),
                                                                                           new IpEndPoint().ipv4Address("1.1.1.2").port(8082)));

        final ScpInfo scpInfo = new ScpInfo().scpPorts(Map.of("http", 8080)).scpDomainInfoList(Map.of("a.com", scpDomainInfoAcom));

        final NFProfile p = new NFProfile().capacity(10)
                                           .priority(11)
                                           .nfType(NFType.SCP)
                                           .nfStatus(NFStatus.REGISTERED)
                                           .fqdn("scp.ericsson.com")
                                           .ipv4Addresses(List.of("1.1.1.1"))
                                           .scpDomains(List.of("a.com", "b.com"))
                                           .scpInfo(scpInfo);

        final MultipleIpEndpoint ep1Acom = new MultipleIpEndpoint().withName("TCP-8081")
                                                                   .withTransport(Transport.TCP)
                                                                   .withIpv4Address(List.of("1.1.1.2"))
                                                                   .withPort(8081);
        final MultipleIpEndpoint ep2Acom = new MultipleIpEndpoint().withName("TCP-8082")
                                                                   .withTransport(Transport.TCP)
                                                                   .withIpv4Address(List.of("1.1.1.3", "1.1.1.2"))
                                                                   .withPort(8082);
        final Address addrNonTlsAcom = new Address().withFqdn("scp.ericsson.com").withScheme(Scheme.HTTP).withMultipleIpEndpoint(List.of(ep1Acom, ep2Acom));
        final Address addrTlsAcom = new Address().withFqdn("scp.ericsson.com").withScheme(Scheme.HTTPS).withMultipleIpEndpoint(List.of(ep1Acom, ep2Acom));
        final MultipleIpEndpoint epNonTlsDefault = new MultipleIpEndpoint().withName("TCP-8080")
                                                                           .withTransport(Transport.TCP)
                                                                           .withIpv4Address(List.of("1.1.1.1"))
                                                                           .withPort(8080);
        final Address addrNonTlsBcom = new Address().withFqdn("scp.ericsson.com").withScheme(Scheme.HTTP).withMultipleIpEndpoint(List.of(epNonTlsDefault));
        final DiscoveredScpDomainInfo domainInfoNonTlsAcom = new DiscoveredScpDomainInfo().withName("a.com-non-tls")
                                                                                          .withDomain("a.com")
                                                                                          .withCapacity(p.getCapacity())
                                                                                          .withPriority(p.getPriority())
                                                                                          .withAddress(addrNonTlsAcom);
        final DiscoveredScpDomainInfo domainInfoTlsAcom = new DiscoveredScpDomainInfo().withName("a.com-tls")
                                                                                       .withDomain("a.com")
                                                                                       .withCapacity(p.getCapacity())
                                                                                       .withPriority(p.getPriority())
                                                                                       .withAddress(addrTlsAcom);
        final DiscoveredScpDomainInfo domainInfoNonTlsBcom = new DiscoveredScpDomainInfo().withName("default-non-tls")
                                                                                          .withDomain(null)
                                                                                          .withCapacity(p.getCapacity())
                                                                                          .withPriority(p.getPriority())
                                                                                          .withAddress(addrNonTlsBcom);

        final String expected = toJson(List.of(domainInfoNonTlsAcom, domainInfoTlsAcom, domainInfoNonTlsBcom)
                                           .stream()
                                           .collect(Collectors.toCollection(TreeSet::new)));

        final String actual = toJson(NnrfNfDiscovery.createScpDomainInfoList(p));

        log.info("act: {}", actual);
        log.info("exp: {}", expected);

        Assertions.assertTrue(actual.equals(expected));
    }
}
