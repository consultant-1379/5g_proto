package com.ericsson.sc.nrf.r17;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ConfigComparatorTest
{
    private static final Logger log = LoggerFactory.getLogger(ConfigComparatorTest.class);

    @Test
    void testPathSelectorNnrfDisc() throws IOException
    {
        final List<String> positives = List.of("/",
                                               "/ericsson-scp:scp-function",
                                               "/ericsson-scp:scp-function/nf-instance",
                                               "/ericsson-scp:scp-function/nf-instance/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/nrf-service",
                                               "/ericsson-scp:scp-function/nf-instance/0/nrf-service/nf-discovery",
                                               "/ericsson-scp:scp-function/nf-instance/0/nrf-service/nf-discovery/nrf-group-ref",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/nf-match-condition",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/scp-match-condition",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/nf-pool-discovery",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/nf-pool-discovery/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/nf-pool-discovery/0/nrf-group-ref/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/nf-pool-discovery/0/nrf-query/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/nf-pool-discovery/0/nrf-query/0/nrf-group-ref",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/nf-pool-discovery/0/update-interval");

        final List<String> negatives = List.of("/ericsson-scp:scp-function/nf-instance/0/name",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/name",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/nf-pool-discovery/0/discovered-nf-instance",
                                               "/ericsson-scp:scp-function/nf-instance/0/nrf-service/nf-management",
                                               "/ericsson-scp:scp-function/nf-instance/0/slf-lookup-profile",
                                               "/ericsson-scp:scp-function/nf-instance/0/slf-lookup-profile/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/slf-lookup-profile/0/nrf-group-ref",
                                               "/ericsson-scp:scp-function/nf-instance/0/failover-profile/0/retry-condition/reset",
                                               "/ericsson-scp:scp-function/nf-instance/0/failover-profile/0/retry-condition/connect-failure",
                                               "/ericsson-scp:scp-function/nf-instance/0/failover-profile/0/retry-condition/refused-stream",
                                               "/ericsson-scp:scp-function/nf-instance/0/vtap/enabled");

        for (int i = 0; i < positives.size(); ++i)
        {
            log.info("positives[{}]={}", i, positives.get(i));
            Assertions.assertTrue(positives.get(i).matches(ConfigComparators.PATH_SELECTOR_NNRF_DISC), positives.get(i));
        }

        for (int i = 0; i < negatives.size(); ++i)
        {
            log.info("negatives[{}]={}", i, negatives.get(i));
            Assertions.assertFalse(negatives.get(i).matches(ConfigComparators.PATH_SELECTOR_NNRF_DISC), negatives.get(i));
        }
    }

    @Test
    void testPathSelectorNnrfDiscBsf() throws IOException
    {
        final List<String> positives = List.of("/",
                                               "/ericsson-bsf:bsf-function",
                                               "/ericsson-bsf:bsf-function/nf-instance",
                                               "/ericsson-bsf:bsf-function/nf-instance/0",
                                               "/ericsson-bsf:bsf-function/nf-instance/0/nrf-service",
                                               "/ericsson-bsf:bsf-function/nf-instance/0/nrf-service/nf-discovery",
                                               "/ericsson-bsf:bsf-function/nf-instance/0/nrf-service/nf-discovery/nrf-group-ref",
                                               "/ericsson-bsf:bsf-function/nf-instance/0/bsf-service",
                                               "/ericsson-bsf:bsf-function/nf-instance/0/bsf-service/0",
                                               "/ericsson-bsf:bsf-function/nf-instance/0/bsf-service/0/nf-pool",
                                               "/ericsson-bsf:bsf-function/nf-instance/0/bsf-service/0/nf-pool/0",
                                               "/ericsson-bsf:bsf-function/nf-instance/0/bsf-service/0/nf-pool/0/nf-pool-discovery",
                                               "/ericsson-bsf:bsf-function/nf-instance/0/bsf-service/0/nf-pool/0/nf-pool-discovery/0",
                                               "/ericsson-bsf:bsf-function/nf-instance/0/bsf-service/0/nf-pool/0/nf-pool-discovery/0/update-interval");

        final List<String> negatives = List.of("/ericsson-bsf:bsf-function/nf-instance/0/name",
                                               "/ericsson-bsf:bsf-function/nf-instance/0/bsf-service/0/nf-pool/0/nf-pool-discovery/0/nrf-group-ref/0",
                                               "/ericsson-bsf:bsf-function/nf-instance/0/bsf-service/0/nf-pool/0/nf-pool-discovery/0/nrf-query/0",
                                               "/ericsson-bsf:bsf-function/nf-instance/0/bsf-service/0/nf-pool/0/nf-pool-discovery/0/nrf-query/0/nrf-group-ref",
                                               "/ericsson-bsf:bsf-function/nf-instance/0/nf-pool/0/name",
                                               "/ericsson-bsf:bsf-function/nf-instance/0/nf-pool/0/nf-pool-discovery/0/discovered-nf-instance",
                                               "/ericsson-bsf:bsf-function/nf-instance/0/nrf-service/nf-management",
                                               "/ericsson-bsf:bsf-function/nf-instance/0/slf-lookup-profile",
                                               "/ericsson-bsf:bsf-function/nf-instance/0/slf-lookup-profile/0",
                                               "/ericsson-bsf:bsf-function/nf-instance/0/slf-lookup-profile/0/nrf-group-ref",
                                               "/ericsson-bsf:bsf-function/nf-instance/0/failover-profile/0/retry-condition/reset",
                                               "/ericsson-bsf:bsf-function/nf-instance/0/failover-profile/0/retry-condition/connect-failure",
                                               "/ericsson-bsf:bsf-function/nf-instance/0/failover-profile/0/retry-condition/refused-stream",
                                               "/ericsson-bsf:bsf-function/nf-instance/0/vtap/enabled");

        for (int i = 0; i < positives.size(); ++i)
        {
            log.info("positives[{}]={}", i, positives.get(i));
            Assertions.assertTrue(positives.get(i).matches(ConfigComparators.PATH_SELECTOR_NNRF_DISC_BSF), positives.get(i));
        }

        for (int i = 0; i < negatives.size(); ++i)
        {
            log.info("negatives[{}]={}", i, negatives.get(i));
            Assertions.assertFalse(negatives.get(i).matches(ConfigComparators.PATH_SELECTOR_NNRF_DISC_BSF), negatives.get(i));
        }
    }

    @Test
    void testPathSelectorNnrfDiscInstances() throws IOException
    {
        final List<String> positives = List.of("/",
                                               "/ericsson-scp:scp-function",
                                               "/ericsson-scp:scp-function/nf-instance",
                                               "/ericsson-scp:scp-function/nf-instance/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/nf-pool-discovery",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/nf-pool-discovery/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/nf-pool-discovery/0/discovered-nf-instance",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/nf-pool-discovery/0/discovered-nf-instance/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/nf-pool-discovery/0/discovered-nf-instance/0/discovered-nf-service",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/nf-pool-discovery/0/discovered-nf-instance/0/discovered-nf-service/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/nf-pool-discovery/0/discovered-scp-instance",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/nf-pool-discovery/0/discovered-scp-instance/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/nf-pool-discovery/0/discovered-scp-instance/0/discovered-scp-domain-info",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/nf-pool-discovery/0/discovered-scp-instance/0/discovered-scp-domain-info/0");

        final List<String> negatives = List.of("/ericsson-scp:scp-function/nf-instance/0/name",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/name",
                                               "/ericsson-scp:scp-function/nf-instance/0/failover-profile/0/retry-condition/reset",
                                               "/ericsson-scp:scp-function/nf-instance/0/failover-profile/0/retry-condition/connect-failure",
                                               "/ericsson-scp:scp-function/nf-instance/0/failover-profile/0/retry-condition/refused-stream",
                                               "/ericsson-scp:scp-function/nf-instance/0/vtap/enabled");

        for (int i = 0; i < positives.size(); ++i)
        {
            log.info("positives[{}]={}", i, positives.get(i));
            Assertions.assertTrue(positives.get(i).matches(ConfigComparators.PATH_SELECTOR_NNRF_DISC_INSTANCES), positives.get(i));
        }

        for (int i = 0; i < negatives.size(); ++i)
        {
            log.info("negatives[{}]={}", i, negatives.get(i));
            Assertions.assertFalse(negatives.get(i).matches(ConfigComparators.PATH_SELECTOR_NNRF_DISC_INSTANCES), negatives.get(i));
        }
    }

    @Test
    void testPathSelectorNnrfDiscCapacity() throws IOException
    {
        final List<String> positives = List.of("/",
                                               "/ericsson-scp:scp-function",
                                               "/ericsson-scp:scp-function/nf-instance",
                                               "/ericsson-scp:scp-function/nf-instance/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/nf-pool-discovery",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/nf-pool-discovery/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/nf-pool-discovery/0/discovered-nf-instance",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/nf-pool-discovery/0/discovered-nf-instance/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/nf-pool-discovery/0/discovered-nf-instance/0/nf-status",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/nf-pool-discovery/0/discovered-nf-instance/0/discovered-nf-service",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/nf-pool-discovery/0/discovered-nf-instance/0/discovered-nf-service/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/nf-pool-discovery/0/discovered-nf-instance/0/discovered-nf-service/0/capacity",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/nf-pool-discovery/0/discovered-nf-instance/0/discovered-nf-service/0/status");

        final List<String> negatives = List.of("/ericsson-scp:scp-function/nf-instance/0/name",
                                               "/ericsson-scp:scp-function/nf-instance/0/nrf-group/0/nf-instance-id",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/nf-pool-discovery/0/nrf-query/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/nf-pool-discovery/0/discovered-nf-instance/0//0/name",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/nf-pool-discovery/0/discovered-nf-instance/0/discovered-nf-service/0/priority");

        for (int i = 0; i < positives.size(); ++i)
        {
            log.info("positives[{}]={}", i, positives.get(i));
            Assertions.assertTrue(positives.get(i).matches(ConfigComparators.PATH_SELECTOR_NNRF_DISC_CAPACITY), positives.get(i));
        }

        for (int i = 0; i < negatives.size(); ++i)
        {
            log.info("negatives[{}]={}", i, negatives.get(i));
            Assertions.assertFalse(negatives.get(i).matches(ConfigComparators.PATH_SELECTOR_NNRF_DISC_CAPACITY), negatives.get(i));
        }
    }

    @Test
    void testPathSelectorNnrfNfm() throws IOException
    {
        final List<String> positives = List.of("/",
                                               "/ericsson-scp:scp-function",
                                               "/ericsson-scp:scp-function/nf-instance",
                                               "/ericsson-scp:scp-function/nf-instance/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-instance-id",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-peer-info",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-peer-info/out-message-handling",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-profile",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-profile/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-profile/0/name",
                                               "/ericsson-scp:scp-function/nf-instance/0/nrf-group",
                                               "/ericsson-scp:scp-function/nf-instance/0/nrf-group/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/nrf-group/0/name",
                                               "/ericsson-scp:scp-function/nf-instance/0/nrf-service",
                                               "/ericsson-scp:scp-function/nf-instance/0/nrf-service/name",
                                               "/ericsson-scp:scp-function/nf-instance/0/service-address",
                                               "/ericsson-scp:scp-function/nf-instance/0/service-address/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/service-address/0/name",
                                               "/ericsson-scp:scp-function/nf-instance/0/external-network",
                                               "/ericsson-scp:scp-function/nf-instance/0/external-network/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/external-network/0/roaming-partner",
                                               "/ericsson-scp:scp-function/nf-instance/0/external-network/0/roaming-partner/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/external-network/0/roaming-partner/0/n32-c",
                                               "/ericsson-scp:scp-function/nf-instance/0/external-network/0/roaming-partner/0/n32-c/xyz");

        final List<String> negatives = List.of("/ericsson-scp:scp-function/nf-instance/0/name",
                                               "/ericsson-scp:scp-function/nf-instance/0/nrf-group/0/nf-instance-id",

                                               // CSA related
                                               "/ericsson-scp:scp-function/nf-instance/0/dynamic-producer-registration",
                                               "/ericsson-scp:scp-function/nf-instance/0/dynamic-producer-pool");

        for (int i = 0; i < positives.size(); ++i)
        {
            log.info("positives[{}]={}", i, positives.get(i));
            Assertions.assertTrue(positives.get(i).matches(ConfigComparators.PATH_SELECTOR_NNRF_NFM), positives.get(i));
        }

        for (int i = 0; i < negatives.size(); ++i)
        {
            log.info("negatives[{}]={}", i, negatives.get(i));
            Assertions.assertFalse(negatives.get(i).matches(ConfigComparators.PATH_SELECTOR_NNRF_NFM), negatives.get(i));
        }
    }

    @Test
    void testPathSelectorNnrfNfmNrfGroupInstId() throws IOException
    {
        final List<String> positives = List.of("/ericsson-scp:scp-function/nf-instance/0/nrf-group/0/nf-instance-id");

        final List<String> negatives = List.of("/",
                                               "/ericsson-scp:scp-function",
                                               "/ericsson-scp:scp-function/nf-instance",
                                               "/ericsson-scp:scp-function/nf-instance/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-instance-id",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-peer-info",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-peer-info/out-message-handling",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-profile",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-profile/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-profile/0/name",
                                               "/ericsson-scp:scp-function/nf-instance/0/nrf-group",
                                               "/ericsson-scp:scp-function/nf-instance/0/nrf-group/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/nrf-group/0/name",
                                               "/ericsson-scp:scp-function/nf-instance/0/nrf-service",
                                               "/ericsson-scp:scp-function/nf-instance/0/nrf-service/name",
                                               "/ericsson-scp:scp-function/nf-instance/0/service-address",
                                               "/ericsson-scp:scp-function/nf-instance/0/service-address/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/service-address/0/name",
                                               "/ericsson-scp:scp-function/nf-instance/0/external-network",
                                               "/ericsson-scp:scp-function/nf-instance/0/external-network/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/external-network/0/roaming-partner",
                                               "/ericsson-scp:scp-function/nf-instance/0/external-network/0/roaming-partner/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/external-network/0/roaming-partner/0/n32-c",
                                               "/ericsson-scp:scp-function/nf-instance/0/external-network/0/roaming-partner/0/n32-c/xyz");

        for (int i = 0; i < positives.size(); ++i)
        {
            log.info("positives[{}]={}", i, positives.get(i));
            Assertions.assertTrue(positives.get(i).matches(ConfigComparators.PATH_SELECTOR_NNRF_NFM_NRF_GROUP_INST_ID), positives.get(i));
        }

        for (int i = 0; i < negatives.size(); ++i)
        {
            log.info("negatives[{}]={}", i, negatives.get(i));
            Assertions.assertFalse(negatives.get(i).matches(ConfigComparators.PATH_SELECTOR_NNRF_NFM_NRF_GROUP_INST_ID), negatives.get(i));
        }
    }

    @Test
    void testPathSelectorNnlfDisc() throws IOException
    {
        final List<String> positives = List.of("/",
                                               "/ericsson-scp:scp-function",
                                               "/ericsson-scp:scp-function/nf-instance",
                                               "/ericsson-scp:scp-function/nf-instance/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/name",
                                               "/ericsson-scp:scp-function/nf-instance/0/nrf-group",
                                               "/ericsson-scp:scp-function/nf-instance/0/nrf-group/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/nrf-group/0/name",
                                               "/ericsson-scp:scp-function/nf-instance/0/nrf-group/0/nrf",
                                               "/ericsson-scp:scp-function/nf-instance/0/nrf-group/0/nrf/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/nrf-group/0/nrf/0/name",

                                               // Information related to header 3gpp-Sbi-NF-Peer-Info
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-peer-info",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-peer-info/out-message-handling",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-profile/0/service-address-ref",
                                               "/ericsson-scp:scp-function/nf-instance/0/service-address/0/fqdn");

        final List<String> negatives = List.of("/ericsson-scp:scp-function/nf-instance/0/own-network/0/abc",
                                               "/ericsson-scp:scp-function/nf-instance/0/external-network/0/abc",
                                               "/ericsson-scp:scp-function/nf-instance/0/external-network/0/roaming-partner/0/abc",
                                               "/ericsson-scp:scp-function/nf-instance/0/nrf-group/0/nf-instance-id",

                                               // Information related to header 3gpp-Sbi-NF-Peer-Info
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-profile",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-profile/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-profile/0/abc",
                                               "/ericsson-scp:scp-function/nf-instance/0/service-address",
                                               "/ericsson-scp:scp-function/nf-instance/0/service-address/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/service-address/0/abc");

        for (int i = 0; i < positives.size(); ++i)
        {
            log.info("positives[{}]={}", i, positives.get(i));
            Assertions.assertTrue(positives.get(i).matches(ConfigComparators.PATH_SELECTOR_NNLF_DISC), positives.get(i));
        }

        for (int i = 0; i < negatives.size(); ++i)
        {
            log.info("negatives[{}]={}", i, negatives.get(i));
            Assertions.assertFalse(negatives.get(i).matches(ConfigComparators.PATH_SELECTOR_NNLF_DISC), negatives.get(i));
        }
    }

    @Test
    void testPathSelectorNrlfRateLimiting() throws IOException
    {
        final List<String> positives = List.of("/",
                                               "/ericsson-scp:scp-function",
                                               "/ericsson-scp:scp-function/nf-instance",
                                               "/ericsson-scp:scp-function/nf-instance/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/global-rate-limit-profile",
                                               "/ericsson-scp:scp-function/nf-instance/0/global-rate-limit-profile/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/global-rate-limit-profile/0/name",
                                               "/ericsson-scp:scp-function/nf-instance/0/own-network",
                                               "/ericsson-scp:scp-function/nf-instance/0/own-network/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/own-network/0/name",
                                               "/ericsson-scp:scp-function/nf-instance/0/own-network/0/global-ingress-rate-limit-profile-ref",
                                               "/ericsson-scp:scp-function/nf-instance/0/own-network/0/global-ingress-rate-limit-profile-ref/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/own-network/0/global-ingress-rate-limit-profile-ref/0/name",
                                               "/ericsson-scp:scp-function/nf-instance/0/external-network",
                                               "/ericsson-scp:scp-function/nf-instance/0/external-network/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/external-network/0/name",
                                               "/ericsson-scp:scp-function/nf-instance/0/external-network/0/global-ingress-rate-limit-profile-ref",
                                               "/ericsson-scp:scp-function/nf-instance/0/external-network/0/global-ingress-rate-limit-profile-ref/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/external-network/0/global-ingress-rate-limit-profile-ref/0/name",
                                               "/ericsson-scp:scp-function/nf-instance/0/external-network/0/roaming-partner",
                                               "/ericsson-scp:scp-function/nf-instance/0/external-network/0/roaming-partner/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/external-network/0/roaming-partner/0/name",
                                               "/ericsson-scp:scp-function/nf-instance/0/external-network/0/roaming-partner/0/global-ingress-rate-limit-profile-ref",
                                               "/ericsson-scp:scp-function/nf-instance/0/external-network/0/roaming-partner/0/global-ingress-rate-limit-profile-ref/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/external-network/0/roaming-partner/0/global-ingress-rate-limit-profile-ref/0/name");

        final List<String> negatives = List.of("/ericsson-scp:scp-function/nf-instance/0/name",
                                               "/ericsson-scp:scp-function/nf-instance/0/own-network/0/abc",
                                               "/ericsson-scp:scp-function/nf-instance/0/external-network/0/abc",
                                               "/ericsson-scp:scp-function/nf-instance/0/external-network/0/roaming-partner/0/abc");

        for (int i = 0; i < positives.size(); ++i)
        {
            log.info("positives[{}]={}", i, positives.get(i));
            Assertions.assertTrue(positives.get(i).matches(ConfigComparators.PATH_SELECTOR_NRLF_RATELIMITING), positives.get(i));
        }

        for (int i = 0; i < negatives.size(); ++i)
        {
            log.info("negatives[{}]={}", i, negatives.get(i));
            Assertions.assertFalse(negatives.get(i).matches(ConfigComparators.PATH_SELECTOR_NRLF_RATELIMITING), negatives.get(i));
        }
    }

    @Test
    void testPathSelectorNnrfDiscNrfGroupRef() throws IOException
    {
        final List<String> positives = List.of("/",
                                               "/ericsson-scp:scp-function",
                                               "/ericsson-scp:scp-function/nf-instance",
                                               "/ericsson-scp:scp-function/nf-instance/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/name",
                                               "/ericsson-scp:scp-function/nf-instance/0/nrf-service",
                                               "/ericsson-scp:scp-function/nf-instance/0/nrf-service/nf-discovery",
                                               "/ericsson-scp:scp-function/nf-instance/0/nrf-service/nf-discovery/nrf-group-ref",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/nf-pool-discovery",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/nf-pool-discovery/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/nf-pool-discovery/0/nrf-group-ref",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/nf-pool-discovery/0/nrf-group-ref/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/nf-pool-discovery/0/nrf-query",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/nf-pool-discovery/0/nrf-query/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/nf-pool-discovery/0/nrf-query/0/nrf-group-ref",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/nf-pool-discovery/0/nrf-query/0/nrf-group-ref/0");

        final List<String> negatives = List.of("/ericsson-scp:scp-function/nf-instance/0/own-network/0/abc",
                                               "/ericsson-scp:scp-function/nf-instance/0/external-network/0/abc",
                                               "/ericsson-scp:scp-function/nf-instance/0/external-network/0/roaming-partner/0/abc",
                                               "/ericsson-scp:scp-function/nf-instance/0/nrf-service/nf-management",
                                               "/ericsson-scp:scp-function/nf-instance/0/slf-lookup-profile",
                                               "/ericsson-scp:scp-function/nf-instance/0/slf-lookup-profile/0",
                                               "/ericsson-scp:scp-function/nf-instance/0/slf-lookup-profile/0/nrf-group-ref",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/nf-pool-discovery/0/xxx",
                                               "/ericsson-scp:scp-function/nf-instance/0/nf-pool/0/nf-pool-discovery/0/nrf-query/0/xxx");
        for (int i = 0; i < positives.size(); ++i)
        {
            log.info("positives[{}]={}", i, positives.get(i));
            Assertions.assertTrue(positives.get(i).matches(ConfigComparators.PATH_SELECTOR_NNRF_DISC_NRF_GROUP_REF), positives.get(i));
        }

        for (int i = 0; i < negatives.size(); ++i)
        {
            log.info("negatives[{}]={}", i, negatives.get(i));
            Assertions.assertFalse(negatives.get(i).matches(ConfigComparators.PATH_SELECTOR_NNRF_DISC_NRF_GROUP_REF), negatives.get(i));
        }
    }
}
