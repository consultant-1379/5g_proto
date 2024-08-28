/**
 * COPYRIGHT ERICSSON GMBH 2021
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Aug 28, 2021
 *     Author: eedstl
 */

package com.ericsson.cnal.nrf.r17;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cnal.common.OpenApiObjectMapper;
import com.ericsson.cnal.nrf.r17.NnrfDiscSearchResultDb2.Item;
import com.ericsson.cnal.nrf.r17.NrfAdapter.Query;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.custom.NFProfile;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.custom.SearchResult;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.ChfInfo;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFStatus;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFType;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.SupiRange;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class NnrfDiscSearchResultDbTest
{
    private static final String NRF_GROUP = "nrfGroup";

    private static class TestSimilarToTc60InCi
    {
        private static final long SUPI_START_PRE = 4601000000L; // SUPI range: "460020000000000" - "460020000099999"
        private static final String SUPI_START_SUFF_MIN = "00000";
        private static final String SUPI_START_SUFF_MAX = "99999";
        private static final Integer SUPI_RANGE_MAX = 500000; // 500000;
        private static final ChfInfo[] chfInfo_List_Anhui = new ChfInfo[SUPI_RANGE_MAX];
        private static final SearchResult[] result_List_Anhui_IPv6IPv4Fqdn_P1 = new SearchResult[SUPI_RANGE_MAX];

        private static final List<String> AnhuiPrio_1_Ipv6Addresses = Arrays.asList(new String[] { "fdf8:f53b:82e4::53", "fdf8:f53b:82e4::54" });
        private static final List<String> AnhuiPrio_1_Ipv4Addresses = Arrays.asList(new String[] { "192.168.1.1", "192.168.1.2" });
        private static final String AnhuiPrio_1_Fqdn = "Anhui-1";

        private static final List<String> ShaanxiPrio_1_Ipv6Addresses = Arrays.asList(new String[] { "fdf8:f53b:82e4::63", "fdf8:f53b:82e4::64" });
        private static final List<String> ShaanxiPrio_1_Ipv4Addresses = Arrays.asList(new String[] { "192.168.2.1", "192.168.2.2" });
        private static final String ShaanxiPrio_1_Fqdn = "Shaanxi-1";
        private static final List<String> ShaanxiPrio_2_Ipv6Addresses = Arrays.asList(new String[] { "fdf8:f53b:82e4::65", "fdf8:f53b:82e4::66" });;
        private static final String ShaanxiPrio_2_Fqdn = "Shaanxi-2";

        private static final NnrfDiscSearchResultDb2 db = new NnrfDiscSearchResultDb2();

        private static NFProfile createChfNFProfileWithIPv6IPv4FqdnForSlf(UUID nfInstanceId,
                                                                          List<String> ipv6Addresses,
                                                                          List<String> ipv4Addresses,
                                                                          String fqdn,
                                                                          Integer priority,
                                                                          ChfInfo chfInfo,
                                                                          Boolean nfServicePersistence)
        {
            final NFProfile nfProfile = new NFProfile();

            nfProfile.setNfInstanceId(nfInstanceId);
            nfProfile.setNfType(NFType.CHF);
            nfProfile.setNfStatus(NFStatus.REGISTERED);
            nfProfile.setIpv6Addresses(ipv6Addresses);
            nfProfile.setIpv4Addresses(ipv4Addresses);
            nfProfile.setFqdn(fqdn);
            nfProfile.setPriority(priority);
            nfProfile.setCapacity(null);
            nfProfile.setLoad(null);
            nfProfile.setLocality(null);
            nfProfile.setChfInfo(chfInfo);
            nfProfile.setCustomInfo(null);
            nfProfile.setRecoveryTime(null);
            nfProfile.setNfServicePersistence(nfServicePersistence);

            // List which are not used
            // =======================
            nfProfile.setPlmnList(null);
            nfProfile.setsNssais(null);
            nfProfile.setPerPlmnSnssaiList(null);
            nfProfile.setNsiList(null);
            nfProfile.setDefaultNotificationSubscriptions(null);

            // Lists still to be provided:
            // ==========================
//      nfProfile.setIpv4Addresses(ipv4AddressList); StringList
//      nfProfile.setNfServices(nfServiceList);

            return nfProfile;
        }

        private static void test_0_SetupSearchResults()
        {
            final Instant start = Instant.now();

            for (int i = 0; i < SUPI_RANGE_MAX; i++)
            {
                String supi_range_start = (SUPI_START_PRE + i) + SUPI_START_SUFF_MIN;
                String supi_range_stop = (SUPI_START_PRE + i) + SUPI_START_SUFF_MAX;

                // Do not print log if SUPI_RANGE_MAX has huge value

                chfInfo_List_Anhui[i] = new ChfInfo();
                chfInfo_List_Anhui[i].addSupiRangeListItem(new SupiRange().start(supi_range_start).end(supi_range_stop));

                final NFProfile p = createChfNFProfileWithIPv6IPv4FqdnForSlf(UUID.randomUUID(),
                                                                             AnhuiPrio_1_Ipv6Addresses,
                                                                             AnhuiPrio_1_Ipv4Addresses,
                                                                             AnhuiPrio_1_Fqdn,
                                                                             1,
                                                                             chfInfo_List_Anhui[i], // chfInfo_Anhui_SUPI_ranges,
                                                                             false);

                final SearchResult result = new SearchResult();
                result.setValidityPeriod(300);
                result.addNfInstancesItem(p);

                result_List_Anhui_IPv6IPv4Fqdn_P1[i] = result;
//                log.info("r={}", result_List_Anhui_IPv6IPv4Fqdn_P1[i]);
            }

            log.info("Used {} for setting-up {} SearchResults.", Duration.between(start, Instant.now()), SUPI_RANGE_MAX);
        }

        private static void test_1_FillDbWithSearchResults()
        {
            final Instant start = Instant.now();

            for (int i = 0; i < SUPI_RANGE_MAX; i++)
                db.add(new Item(NRF_GROUP, "requester-nf-type,supi,target-nf-type", result_List_Anhui_IPv6IPv4Fqdn_P1[i]));

            log.info("Used {} for filling DB with {} SearchResults. Statistics: {}",
                     Duration.between(start, Instant.now()),
                     SUPI_RANGE_MAX,
                     db.getStatistics());
        }

        private static void test_2_GetSearchResultsFromDb()
        {
            final Instant start = Instant.now();

            String subscriberId;

            for (int i = 0; i < SUPI_RANGE_MAX; i++)
            {
                subscriberId = "supi=imsi-" + (SUPI_START_PRE + i) + SUPI_START_SUFF_MAX;

//                if (i % 1000 == 0)
//                    log.info("subscriberId : {}", subscriberId);

                final String[] tokens = subscriberId.split("=");
                final Query.Builder qb = new Query.Builder().add("requester-nf-type", NFType.SMF).add("target-nf-type", NFType.CHF).add(tokens[0], tokens[1]);
                final Set<Item> items = db.get(NRF_GROUP, qb.build());

                if (items.isEmpty())
                    log.info("{} --> not found", tokens[1]);
            }

            log.info("Used {} for getting {} SearchResults from DB. Statistics: {}",
                     Duration.between(start, Instant.now()),
                     SUPI_RANGE_MAX,
                     db.getStatistics());
        }

        public static void performTest()
        {
            TestSimilarToTc60InCi.test_0_SetupSearchResults();
            TestSimilarToTc60InCi.test_1_FillDbWithSearchResults();
            TestSimilarToTc60InCi.test_2_GetSearchResultsFromDb();
        }
    }

    private static final String RESULT_ANHUI = "{\"validityPeriod\":86400,\"nfInstances\":[{\"nfInstanceId\":\"d7ba6ad9-0ddd-4c9b-baba-c4becb1e18f7\",\"load\":22,\"nfStatus\":\"REGISTERED\",\"nfType\":\"CHF\",\"plmnList\":[{\"mnc\":\"11\",\"mcc\":\"460\"}],\"nfServicePersistence\":false,\"capacity\":9,\"fqdn\":\"scpanhui\",\"sNssais\":[{\"sst\":1,\"sd\":\"000000\"},{\"sst\":1,\"sd\":\"040000\"},{\"sst\":1,\"sd\":\"080000\"},{\"sst\":1,\"sd\":\"f01001\"},{\"sst\":1,\"sd\":\"000001\"}],\"chfInfo\":{\"supiRangeList\":[{\"start\":\"460110230000000\",\"end\":\"460110250999999\"},{\"start\":\"460110250000000\",\"end\":\"460110259999999\"},{\"start\":\"460110251000000\",\"end\":\"460110255299999\"},{\"start\":\"460110255520000\",\"end\":\"460110255529999\"},{\"start\":\"460110674010000\",\"end\":\"460110674509999\"},{\"start\":\"460110678510000\",\"end\":\"460110678699999\"},{\"start\":\"460110678690000\",\"end\":\"460110678709999\"},{\"start\":\"460110691360000\",\"end\":\"460110691539999\"},{\"start\":\"460110703620000\",\"end\":\"460110704219999\"},{\"start\":\"460110719610000\",\"end\":\"460110720119999\"},{\"start\":\"460110734520000\",\"end\":\"460110735519999\"},{\"start\":\"460110745830000\",\"end\":\"460110745949999\"},{\"start\":\"460110753550000\",\"end\":\"460110754349999\"},{\"start\":\"460110760850000\",\"end\":\"460110761049999\"},{\"start\":\"460110766000000\",\"end\":\"460110766199999\"},{\"start\":\"460110785730000\",\"end\":\"460110787629999\"},{\"start\":\"460110798490000\",\"end\":\"460110798579999\"},{\"start\":\"460110811910000\",\"end\":\"460110812309999\"},{\"start\":\"460110825910000\",\"end\":\"460110826309999\"},{\"start\":\"460110853800000\",\"end\":\"460110853939999\"},{\"start\":\"460110861510000\",\"end\":\"460110861709999\"},{\"start\":\"460110865090000\",\"end\":\"460110865109999\"},{\"start\":\"460110888530000\",\"end\":\"460110890429999\"},{\"start\":\"460110911670000\",\"end\":\"460110911679999\"},{\"start\":\"460110934770000\",\"end\":\"460110936869999\"},{\"start\":\"460110955330000\",\"end\":\"460110955419999\"},{\"start\":\"460110983930000\",\"end\":\"460110986529999\"},{\"start\":\"460111215600000\",\"end\":\"460111215689999\"},{\"start\":\"460111235510000\",\"end\":\"460111237809999\"},{\"start\":\"460111402380000\",\"end\":\"460111402949999\"},{\"start\":\"460111425120000\",\"end\":\"460111426019999\"},{\"start\":\"460115460000000\",\"end\":\"460115501699999\"},{\"start\":\"460115934350000\",\"end\":\"460115939349999\"},{\"start\":\"460119991040000\",\"end\":\"460119991059999\"}]}}],\"nrfSupportedFeatures\":\"12\"}";
    private static final String RESULT_BEIJING = "{\"validityPeriod\":86400,\"nfInstances\":[{\"nfInstanceId\":\"03d69e36-7153-497a-9d44-ac37589fb9c0\",\"load\":22,\"nfStatus\":\"REGISTERED\",\"nfType\":\"CHF\",\"plmnList\":[{\"mnc\":\"11\",\"mcc\":\"460\"}],\"nfServicePersistence\":false,\"capacity\":9,\"fqdn\":\"scpbeijing\",\"sNssais\":[{\"sst\":1,\"sd\":\"080000\"},{\"sst\":1,\"sd\":\"f01001\"},{\"sst\":1,\"sd\":\"000001\"},{\"sst\":1,\"sd\":\"000000\"},{\"sst\":1,\"sd\":\"040000\"}],\"chfInfo\":{\"supiRangeList\":[{\"start\":\"460110010000000\",\"end\":\"460110020999999\"},{\"start\":\"460110020000000\",\"end\":\"460110029999999\"},{\"start\":\"460110021000000\",\"end\":\"460110022999999\"},{\"start\":\"460110668500000\",\"end\":\"460110668999999\"},{\"start\":\"460110676710000\",\"end\":\"460110677009999\"},{\"start\":\"460110678710000\",\"end\":\"460110679029999\"},{\"start\":\"460110692290000\",\"end\":\"460110692819999\"},{\"start\":\"460110695940000\",\"end\":\"460110696039999\"},{\"start\":\"460110708620000\",\"end\":\"460110709219999\"},{\"start\":\"460110722120000\",\"end\":\"460110722419999\"},{\"start\":\"460110739520000\",\"end\":\"460110740119999\"},{\"start\":\"460110744420000\",\"end\":\"460110744429999\"},{\"start\":\"460110746030000\",\"end\":\"460110746039999\"},{\"start\":\"460110756350000\",\"end\":\"460110756749999\"},{\"start\":\"460110761700000\",\"end\":\"460110761799999\"},{\"start\":\"460110767080000\",\"end\":\"460110767379999\"},{\"start\":\"460110791930000\",\"end\":\"460110792929999\"},{\"start\":\"460110799740000\",\"end\":\"460110799759999\"},{\"start\":\"460110814910000\",\"end\":\"460110815309999\"},{\"start\":\"460110817810000\",\"end\":\"460110818009999\"},{\"start\":\"460110828310000\",\"end\":\"460110828709999\"},{\"start\":\"460110848660000\",\"end\":\"460110849689999\"},{\"start\":\"460110854040000\",\"end\":\"460110854389999\"},{\"start\":\"460110863210000\",\"end\":\"460110863409999\"},{\"start\":\"460110865110000\",\"end\":\"460110865509999\"},{\"start\":\"460110899900000\",\"end\":\"460110901349999\"},{\"start\":\"460110911780000\",\"end\":\"460110911789999\"},{\"start\":\"460110911840000\",\"end\":\"460110912379999\"},{\"start\":\"460110946900000\",\"end\":\"460110946929999\"},{\"start\":\"460110956390000\",\"end\":\"460110956469999\"},{\"start\":\"460110957490000\",\"end\":\"460110957529999\"},{\"start\":\"460111207740000\",\"end\":\"460111208089999\"},{\"start\":\"460111404570000\",\"end\":\"460111405269999\"},{\"start\":\"460111427920000\",\"end\":\"460111430119999\"},{\"start\":\"460114998000000\",\"end\":\"460114998999999\"},{\"start\":\"460114998040000\",\"end\":\"460114998059999\"},{\"start\":\"460114998120000\",\"end\":\"460114998149999\"},{\"start\":\"460114998160000\",\"end\":\"460114998169999\"},{\"start\":\"460114998200000\",\"end\":\"460114998209999\"},{\"start\":\"460114998290000\",\"end\":\"460114998299999\"},{\"start\":\"460114998330000\",\"end\":\"460114998349999\"},{\"start\":\"460114998410000\",\"end\":\"460114998419999\"},{\"start\":\"460114998470000\",\"end\":\"460114998489999\"},{\"start\":\"460114998540000\",\"end\":\"460114998559999\"},{\"start\":\"460114998610000\",\"end\":\"460114998619999\"},{\"start\":\"460114998630000\",\"end\":\"460114998639999\"},{\"start\":\"460114998660000\",\"end\":\"460114998679999\"},{\"start\":\"460114998790000\",\"end\":\"460114998799999\"},{\"start\":\"460114998810000\",\"end\":\"460114998829999\"},{\"start\":\"460114999000000\",\"end\":\"460114999099999\"},{\"start\":\"460114999010000\",\"end\":\"460114999029999\"},{\"start\":\"460114999100000\",\"end\":\"460114999399999\"},{\"start\":\"460114999330000\",\"end\":\"460114999339999\"},{\"start\":\"460114999360000\",\"end\":\"460114999379999\"},{\"start\":\"460114999400000\",\"end\":\"460114999489999\"},{\"start\":\"460115854000000\",\"end\":\"460115874899999\"},{\"start\":\"460115956550000\",\"end\":\"460115956979999\"},{\"start\":\"460115964680000\",\"end\":\"460115964779999\"},{\"start\":\"460115965590000\",\"end\":\"460115965599999\"},{\"start\":\"460119991100000\",\"end\":\"460119991119999\"}]}}],\"nrfSupportedFeatures\":\"12\"}";
    private static final String RESULT_CHONGQING = "{\"validityPeriod\":86400,\"nfInstances\":[{\"nfInstanceId\":\"bce4baef-f63a-404e-99f7-9ad4ae3fd53d\",\"load\":22,\"nfStatus\":\"REGISTERED\",\"nfType\":\"CHF\",\"plmnList\":[{\"mnc\":\"11\",\"mcc\":\"460\"}],\"nfServicePersistence\":false,\"capacity\":9,\"fqdn\":\"scpchongqing\",\"sNssais\":[{\"sst\":1,\"sd\":\"f01001\"},{\"sst\":1,\"sd\":\"000001\"},{\"sst\":1,\"sd\":\"000000\"},{\"sst\":1,\"sd\":\"040000\"},{\"sst\":1,\"sd\":\"080000\"}],\"chfInfo\":{\"supiRangeList\":[{\"start\":\"460110490000000\",\"end\":\"460110490999999\"},{\"start\":\"460110490000000\",\"end\":\"460110499999999\"},{\"start\":\"460110491000000\",\"end\":\"460110498999999\"},{\"start\":\"460110675010000\",\"end\":\"460110675609999\"},{\"start\":\"460110686090000\",\"end\":\"460110686189999\"},{\"start\":\"460110691610000\",\"end\":\"460110691829999\"},{\"start\":\"460110705020000\",\"end\":\"460110705819999\"},{\"start\":\"460110720820000\",\"end\":\"460110721019999\"},{\"start\":\"460110737020000\",\"end\":\"460110737319999\"},{\"start\":\"460110754450000\",\"end\":\"460110754549999\"},{\"start\":\"460110761150000\",\"end\":\"460110761249999\"},{\"start\":\"460110766300000\",\"end\":\"460110766499999\"},{\"start\":\"460110788430000\",\"end\":\"460110789429999\"},{\"start\":\"460110812710000\",\"end\":\"460110813109999\"},{\"start\":\"460110826310000\",\"end\":\"460110827309999\"},{\"start\":\"460110845570000\",\"end\":\"460110847259999\"},{\"start\":\"460110862010000\",\"end\":\"460110862509999\"},{\"start\":\"460110892980000\",\"end\":\"460110894179999\"},{\"start\":\"460110911760000\",\"end\":\"460110911769999\"},{\"start\":\"460110940520000\",\"end\":\"460110942419999\"},{\"start\":\"460110956010000\",\"end\":\"460110956099999\"},{\"start\":\"460110998150000\",\"end\":\"460110999999999\"},{\"start\":\"460111200000000\",\"end\":\"460111202749999\"},{\"start\":\"460111242790000\",\"end\":\"460111244689999\"},{\"start\":\"460111403050000\",\"end\":\"460111403179999\"},{\"start\":\"460111426320000\",\"end\":\"460111426519999\"},{\"start\":\"460115564000000\",\"end\":\"460115589399999\"},{\"start\":\"460115946250000\",\"end\":\"460115948849999\"},{\"start\":\"460119991560000\",\"end\":\"460119991579999\"}]}}],\"nrfSupportedFeatures\":\"12\"}";
    private static final String RESULT_FUJIAN = "{\"validityPeriod\":86400,\"nfInstances\":[{\"nfInstanceId\":\"cb3ed37b-67a8-42ab-8367-8ca4d6838cda\",\"load\":22,\"nfStatus\":\"REGISTERED\",\"nfType\":\"CHF\",\"plmnList\":[{\"mnc\":\"11\",\"mcc\":\"460\"}],\"nfServicePersistence\":false,\"capacity\":9,\"fqdn\":\"scpfujian\",\"sNssais\":[{\"sst\":1,\"sd\":\"f01001\"},{\"sst\":1,\"sd\":\"000001\"},{\"sst\":1,\"sd\":\"000000\"},{\"sst\":1,\"sd\":\"040000\"},{\"sst\":1,\"sd\":\"080000\"}],\"chfInfo\":{\"supiRangeList\":[{\"start\":\"460110260000000\",\"end\":\"460110280999999\"},{\"start\":\"460110280000000\",\"end\":\"460110289999999\"},{\"start\":\"460110281000000\",\"end\":\"460110284999999\"},{\"start\":\"460110670610000\",\"end\":\"460110671109999\"},{\"start\":\"460110679030000\",\"end\":\"460110679329999\"},{\"start\":\"460110690010000\",\"end\":\"460110690409999\"},{\"start\":\"460110699320000\",\"end\":\"460110699919999\"},{\"start\":\"460110716010000\",\"end\":\"460110716309999\"},{\"start\":\"460110729020000\",\"end\":\"460110730019999\"},{\"start\":\"460110745650000\",\"end\":\"460110745669999\"},{\"start\":\"460110764500000\",\"end\":\"460110764999999\"},{\"start\":\"460110778830000\",\"end\":\"460110780029999\"},{\"start\":\"460110799970000\",\"end\":\"460110799989999\"},{\"start\":\"460110805510000\",\"end\":\"460110806709999\"},{\"start\":\"460110821010000\",\"end\":\"460110821409999\"},{\"start\":\"460110856410000\",\"end\":\"460110856709999\"},{\"start\":\"460110876820000\",\"end\":\"460110878419999\"},{\"start\":\"460110911610000\",\"end\":\"460110911629999\"},{\"start\":\"460110911740000\",\"end\":\"460110911749999\"},{\"start\":\"460110957390000\",\"end\":\"460110957489999\"},{\"start\":\"460110963590000\",\"end\":\"460110963789999\"},{\"start\":\"460111400920000\",\"end\":\"460111401089999\"},{\"start\":\"460111422270000\",\"end\":\"460111423069999\"},{\"start\":\"460115206000000\",\"end\":\"460115239199999\"},{\"start\":\"460115916250000\",\"end\":\"460115917049999\"},{\"start\":\"460119991060000\",\"end\":\"460119991079999\"}]}}],\"nrfSupportedFeatures\":\"12\"}";
    private static final String RESULT_GUANGDONG = "{\"validityPeriod\":86400,\"nfInstances\":[{\"nfInstanceId\":\"5bc4a201-99a4-4f03-b36e-75bbb6382acf\",\"load\":22,\"nfStatus\":\"REGISTERED\",\"nfType\":\"CHF\",\"plmnList\":[{\"mnc\":\"11\",\"mcc\":\"460\"}],\"nfServicePersistence\":false,\"capacity\":9,\"fqdn\":\"scpguangdong\",\"sNssais\":[{\"sst\":1,\"sd\":\"f01001\"},{\"sst\":1,\"sd\":\"000001\"},{\"sst\":1,\"sd\":\"000000\"},{\"sst\":1,\"sd\":\"040000\"},{\"sst\":1,\"sd\":\"080000\"}],\"chfInfo\":{\"supiRangeList\":[{\"start\":\"460110400000000\",\"end\":\"460110459999999\"},{\"start\":\"460110669000000\",\"end\":\"460110669559999\"},{\"start\":\"460110679390000\",\"end\":\"460110680389999\"},{\"start\":\"460110686190000\",\"end\":\"460110687669999\"},{\"start\":\"460110696220000\",\"end\":\"460110697219999\"},{\"start\":\"460110713600000\",\"end\":\"460110714199999\"},{\"start\":\"460110723920000\",\"end\":\"460110725919999\"},{\"start\":\"460110744940000\",\"end\":\"460110745639999\"},{\"start\":\"460110746250000\",\"end\":\"460110746949999\"},{\"start\":\"460110759550000\",\"end\":\"460110759749999\"},{\"start\":\"460110762500000\",\"end\":\"460110762899999\"},{\"start\":\"460110770280000\",\"end\":\"460110772129999\"},{\"start\":\"460110797830000\",\"end\":\"460110797929999\"},{\"start\":\"460110799770000\",\"end\":\"460110799809999\"},{\"start\":\"460110802110000\",\"end\":\"460110803009999\"},{\"start\":\"460110818010000\",\"end\":\"460110818809999\"},{\"start\":\"460110847260000\",\"end\":\"460110848659999\"},{\"start\":\"460110866240000\",\"end\":\"460110869899999\"},{\"start\":\"460110911520000\",\"end\":\"460110911529999\"},{\"start\":\"460110911700000\",\"end\":\"460110911729999\"},{\"start\":\"460110912420000\",\"end\":\"460110914719999\"},{\"start\":\"460110954630000\",\"end\":\"460110954679999\"},{\"start\":\"460110955600000\",\"end\":\"460110955749999\"},{\"start\":\"460110957530000\",\"end\":\"460110959859999\"},{\"start\":\"460111215240000\",\"end\":\"460111215329999\"},{\"start\":\"460111216160000\",\"end\":\"460111221329999\"},{\"start\":\"460111419920000\",\"end\":\"460111420219999\"},{\"start\":\"460115000000000\",\"end\":\"460115085299999\"},{\"start\":\"460115909500000\",\"end\":\"460115910849999\"},{\"start\":\"460119990600000\",\"end\":\"460119990639999\"}]}}],\"nrfSupportedFeatures\":\"12\"}";
    private static final String RESULT_GUANGXI = "{\"validityPeriod\":86400,\"nfInstances\":[{\"nfInstanceId\":\"426bcdcf-3bf8-46d9-a0f0-068de3a67b13\",\"load\":22,\"nfStatus\":\"REGISTERED\",\"nfType\":\"CHF\",\"plmnList\":[{\"mnc\":\"11\",\"mcc\":\"460\"}],\"nfServicePersistence\":false,\"capacity\":9,\"fqdn\":\"scpguangxi\",\"sNssais\":[{\"sst\":1,\"sd\":\"080000\"},{\"sst\":1,\"sd\":\"f01001\"},{\"sst\":1,\"sd\":\"000001\"},{\"sst\":1,\"sd\":\"000000\"},{\"sst\":1,\"sd\":\"040000\"}],\"chfInfo\":{\"supiRangeList\":[{\"start\":\"460110460000000\",\"end\":\"460110470999999\"},{\"start\":\"460110470000000\",\"end\":\"460110479999999\"},{\"start\":\"460110471000000\",\"end\":\"460110475049999\"},{\"start\":\"460110674510000\",\"end\":\"460110675009999\"},{\"start\":\"460110680390000\",\"end\":\"460110680469999\"},{\"start\":\"460110691540000\",\"end\":\"460110691589999\"},{\"start\":\"460110704220000\",\"end\":\"460110704719999\"},{\"start\":\"460110720120000\",\"end\":\"460110720419999\"},{\"start\":\"460110735520000\",\"end\":\"460110736219999\"},{\"start\":\"460110745950000\",\"end\":\"460110745969999\"},{\"start\":\"460110754350000\",\"end\":\"460110754449999\"},{\"start\":\"460110761050000\",\"end\":\"460110761149999\"},{\"start\":\"460110766200000\",\"end\":\"460110766299999\"},{\"start\":\"460110787630000\",\"end\":\"460110788029999\"},{\"start\":\"460110800170000\",\"end\":\"460110800189999\"},{\"start\":\"460110812310000\",\"end\":\"460110812509999\"},{\"start\":\"460110861710000\",\"end\":\"460110862009999\"},{\"start\":\"460110890430000\",\"end\":\"460110891679999\"},{\"start\":\"460110911680000\",\"end\":\"460110911689999\"},{\"start\":\"460110936870000\",\"end\":\"460110939819999\"},{\"start\":\"460110986530000\",\"end\":\"460110997229999\"},{\"start\":\"460111215690000\",\"end\":\"460111215779999\"},{\"start\":\"460111237810000\",\"end\":\"460111241909999\"},{\"start\":\"460111402950000\",\"end\":\"460111403049999\"},{\"start\":\"460111426020000\",\"end\":\"460111426319999\"},{\"start\":\"460115502000000\",\"end\":\"460115539099999\"},{\"start\":\"460115939350000\",\"end\":\"460115944049999\"},{\"start\":\"460119991380000\",\"end\":\"460119991399999\"}]}}],\"nrfSupportedFeatures\":\"12\"}";
    private static final String RESULT_GUIZHOU = "{\"validityPeriod\":86400,\"nfInstances\":[{\"nfInstanceId\":\"7efeab41-5d76-4311-aecd-62992967bc4f\",\"load\":22,\"nfStatus\":\"REGISTERED\",\"nfType\":\"CHF\",\"plmnList\":[{\"mnc\":\"11\",\"mcc\":\"460\"}],\"nfServicePersistence\":false,\"capacity\":9,\"fqdn\":\"scpguizhou\",\"sNssais\":[{\"sst\":1,\"sd\":\"040000\"},{\"sst\":1,\"sd\":\"080000\"},{\"sst\":1,\"sd\":\"f01001\"},{\"sst\":1,\"sd\":\"000001\"},{\"sst\":1,\"sd\":\"000000\"}],\"chfInfo\":{\"supiRangeList\":[{\"start\":\"460110530000000\",\"end\":\"460110540999999\"},{\"start\":\"460110540000000\",\"end\":\"460110549999999\"},{\"start\":\"460110541000000\",\"end\":\"460110542799999\"},{\"start\":\"460110680470000\",\"end\":\"460110680819999\"},{\"start\":\"460110692060000\",\"end\":\"460110692209999\"},{\"start\":\"460110707120000\",\"end\":\"460110707519999\"},{\"start\":\"460110721620000\",\"end\":\"460110721919999\"},{\"start\":\"460110738420000\",\"end\":\"460110738919999\"},{\"start\":\"460110756050000\",\"end\":\"460110756249999\"},{\"start\":\"460110766700000\",\"end\":\"460110766799999\"},{\"start\":\"460110790830000\",\"end\":\"460110791129999\"},{\"start\":\"460110814010000\",\"end\":\"460110814409999\"},{\"start\":\"460110828010000\",\"end\":\"460110828209999\"},{\"start\":\"460110896780000\",\"end\":\"460110897779999\"},{\"start\":\"460110945620000\",\"end\":\"460110946019999\"},{\"start\":\"460111205210000\",\"end\":\"460111206979999\"},{\"start\":\"460111249530000\",\"end\":\"460111250759999\"},{\"start\":\"460111403490000\",\"end\":\"460111403679999\"},{\"start\":\"460111426920000\",\"end\":\"460111427319999\"},{\"start\":\"460115641000000\",\"end\":\"460115660599999\"},{\"start\":\"460115951950000\",\"end\":\"460115955749999\"},{\"start\":\"460119991500000\",\"end\":\"460119991519999\"}]}}],\"nrfSupportedFeatures\":\"12\"}";
    private static final String RESULT_HAINAN = "{\"validityPeriod\":86400,\"nfInstances\":[{\"nfInstanceId\":\"73edfb11-ade6-4ae8-83e6-66f170f6f009\",\"load\":22,\"nfStatus\":\"REGISTERED\",\"nfType\":\"CHF\",\"plmnList\":[{\"mnc\":\"11\",\"mcc\":\"460\"}],\"nfServicePersistence\":false,\"capacity\":9,\"fqdn\":\"scphainan\",\"sNssais\":[{\"sst\":1,\"sd\":\"f01001\"},{\"sst\":1,\"sd\":\"000001\"},{\"sst\":1,\"sd\":\"000000\"},{\"sst\":1,\"sd\":\"040000\"},{\"sst\":1,\"sd\":\"080000\"}],\"chfInfo\":{\"supiRangeList\":[{\"start\":\"460110480000000\",\"end\":\"460110480999999\"},{\"start\":\"460110480000000\",\"end\":\"460110489999999\"},{\"start\":\"460110481000000\",\"end\":\"460110483499999\"},{\"start\":\"460110676410000\",\"end\":\"460110676609999\"},{\"start\":\"460110680820000\",\"end\":\"460110680879999\"},{\"start\":\"460110692210000\",\"end\":\"460110692249999\"},{\"start\":\"460110707520000\",\"end\":\"460110707919999\"},{\"start\":\"460110721920000\",\"end\":\"460110722019999\"},{\"start\":\"460110738920000\",\"end\":\"460110739219999\"},{\"start\":\"460110761500000\",\"end\":\"460110761599999\"},{\"start\":\"460110766800000\",\"end\":\"460110766999999\"},{\"start\":\"460110791130000\",\"end\":\"460110791429999\"},{\"start\":\"460110814410000\",\"end\":\"460110814609999\"},{\"start\":\"460110852060000\",\"end\":\"460110852489999\"},{\"start\":\"460110862810000\",\"end\":\"460110863009999\"},{\"start\":\"460110897780000\",\"end\":\"460110898449999\"},{\"start\":\"460110911770000\",\"end\":\"460110911779999\"},{\"start\":\"460110946020000\",\"end\":\"460110946349999\"},{\"start\":\"460110956100000\",\"end\":\"460110956189999\"},{\"start\":\"460111206980000\",\"end\":\"460111207079999\"},{\"start\":\"460111403680000\",\"end\":\"460111404009999\"},{\"start\":\"460111427320000\",\"end\":\"460111427619999\"},{\"start\":\"460115661000000\",\"end\":\"460115668099999\"},{\"start\":\"460115955750000\",\"end\":\"460115955949999\"},{\"start\":\"460119991400000\",\"end\":\"460119991419999\"}]}}],\"nrfSupportedFeatures\":\"12\"}";
    private static final String RESULT_HEBEI = "{\"validityPeriod\":86400,\"nfInstances\":[{\"nfInstanceId\":\"cfc9b7cd-db2f-4b52-ad08-790801129a26\",\"load\":22,\"nfStatus\":\"REGISTERED\",\"nfType\":\"CHF\",\"plmnList\":[{\"mnc\":\"11\",\"mcc\":\"460\"}],\"nfServicePersistence\":false,\"capacity\":9,\"fqdn\":\"scphebei\",\"sNssais\":[{\"sst\":1,\"sd\":\"080000\"},{\"sst\":1,\"sd\":\"f01001\"},{\"sst\":1,\"sd\":\"000001\"},{\"sst\":1,\"sd\":\"000000\"},{\"sst\":1,\"sd\":\"040000\"}],\"chfInfo\":{\"supiRangeList\":[{\"start\":\"460110040000000\",\"end\":\"460110050999999\"},{\"start\":\"460110050000000\",\"end\":\"460110059999999\"},{\"start\":\"460110051000000\",\"end\":\"460110058999999\"},{\"start\":\"460110677910000\",\"end\":\"460110678509999\"},{\"start\":\"460110680880000\",\"end\":\"460110681279999\"},{\"start\":\"460110694280000\",\"end\":\"460110694579999\"},{\"start\":\"460110711120000\",\"end\":\"460110712119999\"},{\"start\":\"460110723320000\",\"end\":\"460110723919999\"},{\"start\":\"460110742220000\",\"end\":\"460110743419999\"},{\"start\":\"460110746180000\",\"end\":\"460110746189999\"},{\"start\":\"460110757750000\",\"end\":\"460110759449999\"},{\"start\":\"460110762100000\",\"end\":\"460110762399999\"},{\"start\":\"460110769180000\",\"end\":\"460110769579999\"},{\"start\":\"460110794530000\",\"end\":\"460110797529999\"},{\"start\":\"460110799280000\",\"end\":\"460110799379999\"},{\"start\":\"460110800600000\",\"end\":\"460110800829999\"},{\"start\":\"460110816410000\",\"end\":\"460110817309999\"},{\"start\":\"460110829410000\",\"end\":\"460110830009999\"},{\"start\":\"460110831170000\",\"end\":\"460110835369999\"},{\"start\":\"460110854650000\",\"end\":\"460110854669999\"},{\"start\":\"460110865900000\",\"end\":\"460110866029999\"},{\"start\":\"460110906240000\",\"end\":\"460110908589999\"},{\"start\":\"460110911810000\",\"end\":\"460110911819999\"},{\"start\":\"460110912380000\",\"end\":\"460110912419999\"},{\"start\":\"460110950310000\",\"end\":\"460110953359999\"},{\"start\":\"460111212230000\",\"end\":\"460111213479999\"},{\"start\":\"460111253470000\",\"end\":\"460111255019999\"},{\"start\":\"460111412810000\",\"end\":\"460111414259999\"},{\"start\":\"460111441120000\",\"end\":\"460111442919999\"},{\"start\":\"460115763000000\",\"end\":\"460115804799999\"},{\"start\":\"460115959880000\",\"end\":\"460115962979999\"},{\"start\":\"460119991180000\",\"end\":\"460119991199999\"}]}}],\"nrfSupportedFeatures\":\"12\"}";
    private static final String RESULT_HENAN = "{\"validityPeriod\":86400,\"nfInstances\":[{\"nfInstanceId\":\"2fd7ed8f-9a99-435b-825a-65920ce624ab\",\"load\":22,\"nfStatus\":\"REGISTERED\",\"nfType\":\"CHF\",\"plmnList\":[{\"mnc\":\"11\",\"mcc\":\"460\"}],\"nfServicePersistence\":false,\"capacity\":9,\"fqdn\":\"scphenan\",\"sNssais\":[{\"sst\":1,\"sd\":\"080000\"},{\"sst\":1,\"sd\":\"f01001\"},{\"sst\":1,\"sd\":\"000001\"},{\"sst\":1,\"sd\":\"000000\"},{\"sst\":1,\"sd\":\"040000\"}],\"chfInfo\":{\"supiRangeList\":[{\"start\":\"460110330000000\",\"end\":\"460110340999999\"},{\"start\":\"460110340000000\",\"end\":\"460110349999999\"},{\"start\":\"460110341000000\",\"end\":\"460110342999999\"},{\"start\":\"460110677010000\",\"end\":\"460110677909999\"},{\"start\":\"460110681280000\",\"end\":\"460110681739999\"},{\"start\":\"460110693710000\",\"end\":\"460110693979999\"},{\"start\":\"460110709920000\",\"end\":\"460110710919999\"},{\"start\":\"460110722720000\",\"end\":\"460110723319999\"},{\"start\":\"460110740920000\",\"end\":\"460110741819999\"},{\"start\":\"460110746140000\",\"end\":\"460110746159999\"},{\"start\":\"460110756750000\",\"end\":\"460110757749999\"},{\"start\":\"460110761800000\",\"end\":\"460110761999999\"},{\"start\":\"460110768480000\",\"end\":\"460110768779999\"},{\"start\":\"460110793130000\",\"end\":\"460110794329999\"},{\"start\":\"460110800300000\",\"end\":\"460110800469999\"},{\"start\":\"460110815810000\",\"end\":\"460110816409999\"},{\"start\":\"460110828810000\",\"end\":\"460110829409999\"},{\"start\":\"460110849690000\",\"end\":\"460110850539999\"},{\"start\":\"460110863410000\",\"end\":\"460110864009999\"},{\"start\":\"460110903290000\",\"end\":\"460110905289999\"},{\"start\":\"460110911800000\",\"end\":\"460110911809999\"},{\"start\":\"460110948660000\",\"end\":\"460110950159999\"},{\"start\":\"460110956190000\",\"end\":\"460110956279999\"},{\"start\":\"460110956870000\",\"end\":\"460110956949999\"},{\"start\":\"460111210130000\",\"end\":\"460111211889999\"},{\"start\":\"460111252130000\",\"end\":\"460111253469999\"},{\"start\":\"460111409220000\",\"end\":\"460111410989999\"},{\"start\":\"460111437920000\",\"end\":\"460111439319999\"},{\"start\":\"460115716000000\",\"end\":\"460115744799999\"},{\"start\":\"460115958180000\",\"end\":\"460115959579999\"},{\"start\":\"460119991460000\",\"end\":\"460119991479999\"},{\"start\":\"460119992030000\",\"end\":\"460119992039999\"},{\"start\":\"460119992460000\",\"end\":\"460119992469999\"}]}}],\"nrfSupportedFeatures\":\"12\"}";
    private static final String RESULT_HUBEI = "{\"validityPeriod\":86400,\"nfInstances\":[{\"nfInstanceId\":\"32cda77f-5ec0-4552-8e3d-de7deb5006d8\",\"load\":22,\"nfStatus\":\"REGISTERED\",\"nfType\":\"CHF\",\"plmnList\":[{\"mnc\":\"11\",\"mcc\":\"460\"}],\"nfServicePersistence\":false,\"capacity\":9,\"fqdn\":\"scphubei\",\"sNssais\":[{\"sst\":1,\"sd\":\"040000\"},{\"sst\":1,\"sd\":\"080000\"},{\"sst\":1,\"sd\":\"f01001\"},{\"sst\":1,\"sd\":\"000001\"},{\"sst\":1,\"sd\":\"000000\"}],\"chfInfo\":{\"supiRangeList\":[{\"start\":\"460110350000000\",\"end\":\"460110372199999\"},{\"start\":\"460110672210000\",\"end\":\"460110672809999\"},{\"start\":\"460110681940000\",\"end\":\"460110682179999\"},{\"start\":\"460110690640000\",\"end\":\"460110690899999\"},{\"start\":\"460110701220000\",\"end\":\"460110701819999\"},{\"start\":\"460110718310000\",\"end\":\"460110718809999\"},{\"start\":\"460110731520000\",\"end\":\"460110732419999\"},{\"start\":\"460110745690000\",\"end\":\"460110745789999\"},{\"start\":\"460110750850000\",\"end\":\"460110751449999\"},{\"start\":\"460110760450000\",\"end\":\"460110760549999\"},{\"start\":\"460110765100000\",\"end\":\"460110765299999\"},{\"start\":\"460110783030000\",\"end\":\"460110783229999\"},{\"start\":\"460110800030000\",\"end\":\"460110800129999\"},{\"start\":\"460110809210000\",\"end\":\"460110810109999\"},{\"start\":\"460110823510000\",\"end\":\"460110824309999\"},{\"start\":\"460110853610000\",\"end\":\"460110853799999\"},{\"start\":\"460110858710000\",\"end\":\"460110859409999\"},{\"start\":\"460110881670000\",\"end\":\"460110883469999\"},{\"start\":\"460110911640000\",\"end\":\"460110911649999\"},{\"start\":\"460110925330000\",\"end\":\"460110927229999\"},{\"start\":\"460110976690000\",\"end\":\"460110977529999\"},{\"start\":\"460111215420000\",\"end\":\"460111215509999\"},{\"start\":\"460111225700000\",\"end\":\"460111228159999\"},{\"start\":\"460111401330000\",\"end\":\"460111401559999\"},{\"start\":\"460111423670000\",\"end\":\"460111424069999\"},{\"start\":\"460115321000000\",\"end\":\"460115357099999\"},{\"start\":\"460115923050000\",\"end\":\"460115924649999\"},{\"start\":\"460119991440000\",\"end\":\"460119991459999\"}]}}],\"nrfSupportedFeatures\":\"12\"}";
    private static final String RESULT_HUNAN = "{\"validityPeriod\":86400,\"nfInstances\":[{\"nfInstanceId\":\"cdbc27d8-bce1-4d5c-91e4-f78c20694d43\",\"load\":22,\"nfStatus\":\"REGISTERED\",\"nfType\":\"CHF\",\"plmnList\":[{\"mnc\":\"11\",\"mcc\":\"460\"}],\"nfServicePersistence\":false,\"capacity\":9,\"fqdn\":\"scphunan\",\"sNssais\":[{\"sst\":1,\"sd\":\"f01001\"},{\"sst\":1,\"sd\":\"000001\"},{\"sst\":1,\"sd\":\"000000\"},{\"sst\":1,\"sd\":\"040000\"},{\"sst\":1,\"sd\":\"080000\"}],\"chfInfo\":{\"supiRangeList\":[{\"start\":\"460110379300000\",\"end\":\"460110379959999\"},{\"start\":\"460110380000000\",\"end\":\"460110390999999\"},{\"start\":\"460110390000000\",\"end\":\"460110399999999\"},{\"start\":\"460110391000000\",\"end\":\"460110399599999\"},{\"start\":\"460110672810000\",\"end\":\"460110673409999\"},{\"start\":\"460110682180000\",\"end\":\"460110682379999\"},{\"start\":\"460110690900000\",\"end\":\"460110691099999\"},{\"start\":\"460110701820000\",\"end\":\"460110702519999\"},{\"start\":\"460110718810000\",\"end\":\"460110719309999\"},{\"start\":\"460110732420000\",\"end\":\"460110733319999\"},{\"start\":\"460110745790000\",\"end\":\"460110745829999\"},{\"start\":\"460110751450000\",\"end\":\"460110752549999\"},{\"start\":\"460110760550000\",\"end\":\"460110760649999\"},{\"start\":\"460110765300000\",\"end\":\"460110765499999\"},{\"start\":\"460110783230000\",\"end\":\"460110784529999\"},{\"start\":\"460110800130000\",\"end\":\"460110800169999\"},{\"start\":\"460110801030000\",\"end\":\"460110801129999\"},{\"start\":\"460110810110000\",\"end\":\"460110811109999\"},{\"start\":\"460110824310000\",\"end\":\"460110825209999\"},{\"start\":\"460110830410000\",\"end\":\"460110830509999\"},{\"start\":\"460110852490000\",\"end\":\"460110852839999\"},{\"start\":\"460110859410000\",\"end\":\"460110860209999\"},{\"start\":\"460110883470000\",\"end\":\"460110885369999\"},{\"start\":\"460110911750000\",\"end\":\"460110911759999\"},{\"start\":\"460110927230000\",\"end\":\"460110930829999\"},{\"start\":\"460110955840000\",\"end\":\"460110955929999\"},{\"start\":\"460110977530000\",\"end\":\"460110980599999\"},{\"start\":\"460111215510000\",\"end\":\"460111215599999\"},{\"start\":\"460111228160000\",\"end\":\"460111234289999\"},{\"start\":\"460111401560000\",\"end\":\"460111401759999\"},{\"start\":\"460111424070000\",\"end\":\"460111424469999\"},{\"start\":\"460115358000000\",\"end\":\"460115401599999\"},{\"start\":\"460115924650000\",\"end\":\"460115931849999\"},{\"start\":\"460115964780000\",\"end\":\"460115965579999\"},{\"start\":\"460119991420000\",\"end\":\"460119991439999\"}]}}],\"nrfSupportedFeatures\":\"12\"}";
    private static final String RESULT_JIANGSU = "{\"validityPeriod\":86400,\"nfInstances\":[{\"nfInstanceId\":\"2b951375-36a3-4658-a593-7a580a4d4b91\",\"load\":22,\"nfStatus\":\"REGISTERED\",\"nfType\":\"CHF\",\"plmnList\":[{\"mnc\":\"11\",\"mcc\":\"460\"}],\"nfServicePersistence\":false,\"capacity\":9,\"fqdn\":\"scpjiangsu\",\"sNssais\":[{\"sst\":1,\"sd\":\"000000\"},{\"sst\":1,\"sd\":\"040000\"},{\"sst\":1,\"sd\":\"080000\"},{\"sst\":1,\"sd\":\"f01001\"},{\"sst\":1,\"sd\":\"000001\"}],\"chfInfo\":{\"supiRangeList\":[{\"start\":\"460110140000000\",\"end\":\"460110189999999\"},{\"start\":\"460110184780000\",\"end\":\"460110187779999\"},{\"start\":\"460110669560000\",\"end\":\"460110670109999\"},{\"start\":\"460110682500000\",\"end\":\"460110683239999\"},{\"start\":\"460110688130000\",\"end\":\"460110689109999\"},{\"start\":\"460110697720000\",\"end\":\"460110698719999\"},{\"start\":\"460110714500000\",\"end\":\"460110715299999\"},{\"start\":\"460110726420000\",\"end\":\"460110727919999\"},{\"start\":\"460110745640000\",\"end\":\"460110745649999\"},{\"start\":\"460110746950000\",\"end\":\"460110748249999\"},{\"start\":\"460110759750000\",\"end\":\"460110759949999\"},{\"start\":\"460110763100000\",\"end\":\"460110763799999\"},{\"start\":\"460110772930000\",\"end\":\"460110777529999\"},{\"start\":\"460110799810000\",\"end\":\"460110799909999\"},{\"start\":\"460110803410000\",\"end\":\"460110804509999\"},{\"start\":\"460110819310000\",\"end\":\"460110820309999\"},{\"start\":\"460110839220000\",\"end\":\"460110842429999\"},{\"start\":\"460110854810000\",\"end\":\"460110855709999\"},{\"start\":\"460110871150000\",\"end\":\"460110874549999\"},{\"start\":\"460110911540000\",\"end\":\"460110911589999\"},{\"start\":\"460110915870000\",\"end\":\"460110917369999\"},{\"start\":\"460110954690000\",\"end\":\"460110954969999\"},{\"start\":\"460110957290000\",\"end\":\"460110957389999\"},{\"start\":\"460110962410000\",\"end\":\"460110962809999\"},{\"start\":\"460111081000000\",\"end\":\"460111089999999\"},{\"start\":\"460111221580000\",\"end\":\"460111221779999\"},{\"start\":\"460111400200000\",\"end\":\"460111400709999\"},{\"start\":\"460111420820000\",\"end\":\"460111421469999\"},{\"start\":\"460113100000000\",\"end\":\"460113399999999\"},{\"start\":\"460113500000000\",\"end\":\"460113799999999\"},{\"start\":\"460115086000000\",\"end\":\"460115158799999\"},{\"start\":\"460115913150000\",\"end\":\"460115915249999\"},{\"start\":\"460116350000000\",\"end\":\"460116499999999\"},{\"start\":\"460119990300000\",\"end\":\"460119990309999\"},{\"start\":\"460119990300000\",\"end\":\"460119990399999\"},{\"start\":\"460119990310000\",\"end\":\"460119990339999\"},{\"start\":\"460119999000000\",\"end\":\"460119999009999\"}]}}],\"nrfSupportedFeatures\":\"12\"}";
    private static final String RESULT_JIANGXI = "{\"validityPeriod\":86400,\"nfInstances\":[{\"nfInstanceId\":\"791bc37f-9770-4448-984f-ecbc8f8b044b\",\"load\":22,\"nfStatus\":\"REGISTERED\",\"nfType\":\"CHF\",\"plmnList\":[{\"mnc\":\"11\",\"mcc\":\"460\"}],\"nfServicePersistence\":false,\"capacity\":9,\"fqdn\":\"scpjiangxi\",\"sNssais\":[{\"sst\":1,\"sd\":\"000001\"},{\"sst\":1,\"sd\":\"000000\"},{\"sst\":1,\"sd\":\"040000\"},{\"sst\":1,\"sd\":\"080000\"},{\"sst\":1,\"sd\":\"f01001\"}],\"chfInfo\":{\"supiRangeList\":[{\"start\":\"460110290000000\",\"end\":\"460110300999999\"},{\"start\":\"460110300000000\",\"end\":\"460110309999999\"},{\"start\":\"460110301000000\",\"end\":\"460110302999999\"},{\"start\":\"460110675610000\",\"end\":\"460110675909999\"},{\"start\":\"460110683240000\",\"end\":\"460110683439999\"},{\"start\":\"460110691830000\",\"end\":\"460110692009999\"},{\"start\":\"460110705820000\",\"end\":\"460110706319999\"},{\"start\":\"460110721020000\",\"end\":\"460110721219999\"},{\"start\":\"460110737320000\",\"end\":\"460110737719999\"},{\"start\":\"460110745970000\",\"end\":\"460110745989999\"},{\"start\":\"460110754550000\",\"end\":\"460110754649999\"},{\"start\":\"460110761250000\",\"end\":\"460110761399999\"},{\"start\":\"460110766500000\",\"end\":\"460110766599999\"},{\"start\":\"460110789430000\",\"end\":\"460110790129999\"},{\"start\":\"460110813110000\",\"end\":\"460110813509999\"},{\"start\":\"460110827310000\",\"end\":\"460110827609999\"},{\"start\":\"460110862510000\",\"end\":\"460110862609999\"},{\"start\":\"460110894180000\",\"end\":\"460110895379999\"},{\"start\":\"460110942420000\",\"end\":\"460110944119999\"},{\"start\":\"460111202750000\",\"end\":\"460111204309999\"},{\"start\":\"460111215780000\",\"end\":\"460111215859999\"},{\"start\":\"460111244690000\",\"end\":\"460111248229999\"},{\"start\":\"460111403180000\",\"end\":\"460111403299999\"},{\"start\":\"460111426520000\",\"end\":\"460111426619999\"},{\"start\":\"460115590000000\",\"end\":\"460115614499999\"},{\"start\":\"460115948850000\",\"end\":\"460115950049999\"},{\"start\":\"460119991080000\",\"end\":\"460119991099999\"}]}}],\"nrfSupportedFeatures\":\"12\"}";
    private static final String RESULT_LIAONING = "{\"validityPeriod\":86400,\"nfInstances\":[{\"nfInstanceId\":\"7946c5a5-57ca-4b78-a8d4-4e2525af213e\",\"load\":22,\"nfStatus\":\"REGISTERED\",\"nfType\":\"CHF\",\"plmnList\":[{\"mnc\":\"11\",\"mcc\":\"460\"}],\"nfServicePersistence\":false,\"capacity\":9,\"fqdn\":\"scpliaoning\",\"sNssais\":[{\"sst\":1,\"sd\":\"040000\"},{\"sst\":1,\"sd\":\"080000\"},{\"sst\":1,\"sd\":\"f01001\"},{\"sst\":1,\"sd\":\"000001\"},{\"sst\":1,\"sd\":\"000000\"}],\"chfInfo\":{\"supiRangeList\":[{\"start\":\"460110080000000\",\"end\":\"460110093499999\"},{\"start\":\"460110683440000\",\"end\":\"460110683739999\"},{\"start\":\"460110693980000\",\"end\":\"460110694279999\"},{\"start\":\"460110710920000\",\"end\":\"460110711119999\"},{\"start\":\"460110741820000\",\"end\":\"460110742219999\"},{\"start\":\"460110746160000\",\"end\":\"460110746179999\"},{\"start\":\"460110762000000\",\"end\":\"460110762099999\"},{\"start\":\"460110768780000\",\"end\":\"460110769179999\"},{\"start\":\"460110794330000\",\"end\":\"460110794529999\"},{\"start\":\"460110799080000\",\"end\":\"460110799279999\"},{\"start\":\"460110800470000\",\"end\":\"460110800599999\"},{\"start\":\"460110801330000\",\"end\":\"460110801609999\"},{\"start\":\"460110830970000\",\"end\":\"460110831169999\"},{\"start\":\"460110853410000\",\"end\":\"460110853609999\"},{\"start\":\"460110854590000\",\"end\":\"460110854649999\"},{\"start\":\"460110865750000\",\"end\":\"460110865899999\"},{\"start\":\"460110905290000\",\"end\":\"460110906239999\"},{\"start\":\"460110950160000\",\"end\":\"460110950309999\"},{\"start\":\"460110956950000\",\"end\":\"460110957009999\"},{\"start\":\"460111211890000\",\"end\":\"460111212229999\"},{\"start\":\"460111410990000\",\"end\":\"460111412809999\"},{\"start\":\"460111439320000\",\"end\":\"460111441119999\"},{\"start\":\"460115745000000\",\"end\":\"460115762399999\"},{\"start\":\"460115959580000\",\"end\":\"460115959879999\"},{\"start\":\"460117430400000\",\"end\":\"460117430409999\"},{\"start\":\"460119991160000\",\"end\":\"460119991179999\"}]}}],\"nrfSupportedFeatures\":\"12\"}";
    private static final String RESULT_NEIMENGGU = "{\"validityPeriod\":86400,\"nfInstances\":[{\"nfInstanceId\":\"c4ccda6a-be9d-4749-bd65-74dcd02a7cd5\",\"load\":22,\"nfStatus\":\"REGISTERED\",\"nfType\":\"CHF\",\"plmnList\":[{\"mnc\":\"11\",\"mcc\":\"460\"}],\"nfServicePersistence\":false,\"capacity\":9,\"fqdn\":\"scpneimenggu\",\"sNssais\":[{\"sst\":1,\"sd\":\"080000\"},{\"sst\":1,\"sd\":\"f01001\"},{\"sst\":1,\"sd\":\"000001\"},{\"sst\":1,\"sd\":\"000000\"},{\"sst\":1,\"sd\":\"040000\"}],\"chfInfo\":{\"supiRangeList\":[{\"start\":\"460110070000000\",\"end\":\"460110070999999\"},{\"start\":\"460110070000000\",\"end\":\"460110079999999\"},{\"start\":\"460110071000000\",\"end\":\"460110079199999\"},{\"start\":\"460110683740000\",\"end\":\"460110683839999\"},{\"start\":\"460110694750000\",\"end\":\"460110694849999\"},{\"start\":\"460110696110000\",\"end\":\"460110696209999\"},{\"start\":\"460110712420000\",\"end\":\"460110712719999\"},{\"start\":\"460110713520000\",\"end\":\"460110713599999\"},{\"start\":\"460110743620000\",\"end\":\"460110743919999\"},{\"start\":\"460110746190000\",\"end\":\"460110746249999\"},{\"start\":\"460110759450000\",\"end\":\"460110759549999\"},{\"start\":\"460110769680000\",\"end\":\"460110769779999\"},{\"start\":\"460110800870000\",\"end\":\"460110800909999\"},{\"start\":\"460110801810000\",\"end\":\"460110802109999\"},{\"start\":\"460110830610000\",\"end\":\"460110830619999\"},{\"start\":\"460110854730000\",\"end\":\"460110854809999\"},{\"start\":\"460110866090000\",\"end\":\"460110866189999\"},{\"start\":\"460110909290000\",\"end\":\"460110909989999\"},{\"start\":\"460110953760000\",\"end\":\"460110953859999\"},{\"start\":\"460110956280000\",\"end\":\"460110956379999\"},{\"start\":\"460110957010000\",\"end\":\"460110957089999\"},{\"start\":\"460111214200000\",\"end\":\"460111214599999\"},{\"start\":\"460111415130000\",\"end\":\"460111416509999\"},{\"start\":\"460111444720000\",\"end\":\"460111445919999\"},{\"start\":\"460115817000000\",\"end\":\"460115829099999\"},{\"start\":\"460115963780000\",\"end\":\"460115964079999\"},{\"start\":\"460119991220000\",\"end\":\"460119991239999\"}]}}],\"nrfSupportedFeatures\":\"12\"}";
    private static final String RESULT_SHAANXI = "{\"validityPeriod\":86400,\"nfInstances\":[{\"nfInstanceId\":\"5effc228-7c34-45d5-802e-e0d1079743b7\",\"load\":22,\"nfStatus\":\"REGISTERED\",\"nfType\":\"CHF\",\"plmnList\":[{\"mnc\":\"11\",\"mcc\":\"460\"}],\"nfServicePersistence\":false,\"capacity\":9,\"fqdn\":\"scpshaanxi\",\"sNssais\":[{\"sst\":1,\"sd\":\"f01001\"},{\"sst\":1,\"sd\":\"000001\"},{\"sst\":1,\"sd\":\"000000\"},{\"sst\":1,\"sd\":\"040000\"},{\"sst\":1,\"sd\":\"080000\"}],\"chfInfo\":{\"supiRangeList\":[{\"start\":\"460110580000000\",\"end\":\"460110599999999\"},{\"start\":\"460110673410000\",\"end\":\"460110674009999\"},{\"start\":\"460110684720000\",\"end\":\"460110684859999\"},{\"start\":\"460110691100000\",\"end\":\"460110691259999\"},{\"start\":\"460110702520000\",\"end\":\"460110703319999\"},{\"start\":\"460110719310000\",\"end\":\"460110719609999\"},{\"start\":\"460110733320000\",\"end\":\"460110734119999\"},{\"start\":\"460110752550000\",\"end\":\"460110753549999\"},{\"start\":\"460110760650000\",\"end\":\"460110760849999\"},{\"start\":\"460110765500000\",\"end\":\"460110765699999\"},{\"start\":\"460110784530000\",\"end\":\"460110785729999\"},{\"start\":\"460110811110000\",\"end\":\"460110811709999\"},{\"start\":\"460110825210000\",\"end\":\"460110825709999\"},{\"start\":\"460110835370000\",\"end\":\"460110839219999\"},{\"start\":\"460110860210000\",\"end\":\"460110861109999\"},{\"start\":\"460110885370000\",\"end\":\"460110886999999\"},{\"start\":\"460110886280000\",\"end\":\"460110886429999\"},{\"start\":\"460110887000000\",\"end\":\"460110887229999\"},{\"start\":\"460110911650000\",\"end\":\"460110911659999\"},{\"start\":\"460110930830000\",\"end\":\"460110933169999\"},{\"start\":\"460110955240000\",\"end\":\"460110955329999\"},{\"start\":\"460110980600000\",\"end\":\"460110981869999\"},{\"start\":\"460111234290000\",\"end\":\"460111234369999\"},{\"start\":\"460111401760000\",\"end\":\"460111402269999\"},{\"start\":\"460111424470000\",\"end\":\"460111425069999\"},{\"start\":\"460115402000000\",\"end\":\"460115438899999\"},{\"start\":\"460115931850000\",\"end\":\"460115932649999\"},{\"start\":\"460119991240000\",\"end\":\"460119991259999\"}]}}],\"nrfSupportedFeatures\":\"12\"}";
    private static final String RESULT_SHANDONG = "{\"validityPeriod\":86400,\"nfInstances\":[{\"nfInstanceId\":\"c2e1bb25-5204-4e2e-864b-cce6544c926f\",\"load\":22,\"nfStatus\":\"REGISTERED\",\"nfType\":\"CHF\",\"plmnList\":[{\"mnc\":\"11\",\"mcc\":\"460\"}],\"nfServicePersistence\":false,\"capacity\":9,\"fqdn\":\"scpshandong\",\"sNssais\":[{\"sst\":1,\"sd\":\"f01001\"},{\"sst\":1,\"sd\":\"000001\"},{\"sst\":1,\"sd\":\"000000\"},{\"sst\":1,\"sd\":\"040000\"},{\"sst\":1,\"sd\":\"080000\"}],\"chfInfo\":{\"supiRangeList\":[{\"start\":\"460110310000000\",\"end\":\"460110320999999\"},{\"start\":\"460110320000000\",\"end\":\"460110329999999\"},{\"start\":\"460110321000000\",\"end\":\"460110327999999\"},{\"start\":\"460110683920000\",\"end\":\"460110684519999\"},{\"start\":\"460110693040000\",\"end\":\"460110693709999\"},{\"start\":\"460110696040000\",\"end\":\"460110696109999\"},{\"start\":\"460110709420000\",\"end\":\"460110709919999\"},{\"start\":\"460110722420000\",\"end\":\"460110722719999\"},{\"start\":\"460110740320000\",\"end\":\"460110740919999\"},{\"start\":\"460110746040000\",\"end\":\"460110746139999\"},{\"start\":\"460110767680000\",\"end\":\"460110768479999\"},{\"start\":\"460110792930000\",\"end\":\"460110793129999\"},{\"start\":\"460110798780000\",\"end\":\"460110799079999\"},{\"start\":\"460110800190000\",\"end\":\"460110800299999\"},{\"start\":\"460110815410000\",\"end\":\"460110815809999\"},{\"start\":\"460110828710000\",\"end\":\"460110828809999\"},{\"start\":\"460110830820000\",\"end\":\"460110830969999\"},{\"start\":\"460110865710000\",\"end\":\"460110865749999\"},{\"start\":\"460110901820000\",\"end\":\"460110903289999\"},{\"start\":\"460110946930000\",\"end\":\"460110948659999\"},{\"start\":\"460110956550000\",\"end\":\"460110956869999\"},{\"start\":\"460111208260000\",\"end\":\"460111210129999\"},{\"start\":\"460111251100000\",\"end\":\"460111252129999\"},{\"start\":\"460111290000000\",\"end\":\"460111299999999\"},{\"start\":\"460111405670000\",\"end\":\"460111409219999\"},{\"start\":\"460111431320000\",\"end\":\"460111437919999\"},{\"start\":\"460115688000000\",\"end\":\"460115715399999\"},{\"start\":\"460115957080000\",\"end\":\"460115958179999\"},{\"start\":\"460119991360000\",\"end\":\"460119991379999\"}]}}],\"nrfSupportedFeatures\":\"12\"}";
    private static final String RESULT_SHANGHAI = "{\"validityPeriod\":86400,\"nfInstances\":[{\"nfInstanceId\":\"b5cdc439-f328-448c-9ec9-142612dfb73c\",\"load\":22,\"nfStatus\":\"REGISTERED\",\"nfType\":\"CHF\",\"plmnList\":[{\"mnc\":\"11\",\"mcc\":\"460\"}],\"nfServicePersistence\":false,\"capacity\":9,\"fqdn\":\"scpshanghai\",\"sNssais\":[{\"sst\":1,\"sd\":\"040000\"},{\"sst\":1,\"sd\":\"080000\"},{\"sst\":1,\"sd\":\"f01001\"},{\"sst\":1,\"sd\":\"000001\"},{\"sst\":1,\"sd\":\"000000\"}],\"chfInfo\":{\"supiRangeList\":[{\"start\":\"460110120000000\",\"end\":\"460110130999999\"},{\"start\":\"460110130000000\",\"end\":\"460110139999999\"},{\"start\":\"460110131000000\",\"end\":\"460110138999999\"},{\"start\":\"460110656500000\",\"end\":\"460110661999999\"},{\"start\":\"460110684860000\",\"end\":\"460110685159999\"},{\"start\":\"460110687670000\",\"end\":\"460110688129999\"},{\"start\":\"460110695040000\",\"end\":\"460110695639999\"},{\"start\":\"460110697220000\",\"end\":\"460110697719999\"},{\"start\":\"460110714200000\",\"end\":\"460110714499999\"},{\"start\":\"460110725920000\",\"end\":\"460110726419999\"},{\"start\":\"460110744430000\",\"end\":\"460110744929999\"},{\"start\":\"460110762900000\",\"end\":\"460110763099999\"},{\"start\":\"460110772130000\",\"end\":\"460110772929999\"},{\"start\":\"460110803010000\",\"end\":\"460110803409999\"},{\"start\":\"460110818810000\",\"end\":\"460110819309999\"},{\"start\":\"460110869900000\",\"end\":\"460110871149999\"},{\"start\":\"460110911530000\",\"end\":\"460110911539999\"},{\"start\":\"460110914720000\",\"end\":\"460110915869999\"},{\"start\":\"460110954680000\",\"end\":\"460110954689999\"},{\"start\":\"460110959860000\",\"end\":\"460110962409999\"},{\"start\":\"460111221330000\",\"end\":\"460111221579999\"},{\"start\":\"460111400000000\",\"end\":\"460111400199999\"},{\"start\":\"460111420220000\",\"end\":\"460111420819999\"},{\"start\":\"460115875000000\",\"end\":\"460115902199999\"},{\"start\":\"460115910850000\",\"end\":\"460115913149999\"},{\"start\":\"460119990000000\",\"end\":\"460119990019999\"}]}}],\"nrfSupportedFeatures\":\"12\"}";
    private static final String RESULT_SICHUAN = "{\"validityPeriod\":86400,\"nfInstances\":[{\"nfInstanceId\":\"13a4461e-70ae-4d71-b6dd-ff6aa51c0d9d\",\"load\":22,\"nfStatus\":\"REGISTERED\",\"nfType\":\"CHF\",\"plmnList\":[{\"mnc\":\"11\",\"mcc\":\"460\"}],\"nfServicePersistence\":false,\"capacity\":9,\"fqdn\":\"scpsichuan\",\"sNssais\":[{\"sst\":1,\"sd\":\"f01001\"},{\"sst\":1,\"sd\":\"000001\"},{\"sst\":1,\"sd\":\"000000\"},{\"sst\":1,\"sd\":\"040000\"},{\"sst\":1,\"sd\":\"080000\"}],\"chfInfo\":{\"supiRangeList\":[{\"start\":\"460110500000000\",\"end\":\"460110529999999\"},{\"start\":\"460110671110000\",\"end\":\"460110672209999\"},{\"start\":\"460110685160000\",\"end\":\"460110685359999\"},{\"start\":\"460110690410000\",\"end\":\"460110690639999\"},{\"start\":\"460110695640000\",\"end\":\"460110695739999\"},{\"start\":\"460110699920000\",\"end\":\"460110701219999\"},{\"start\":\"460110713220000\",\"end\":\"460110713319999\"},{\"start\":\"460110716310000\",\"end\":\"460110718309999\"},{\"start\":\"460110730020000\",\"end\":\"460110731519999\"},{\"start\":\"460110745670000\",\"end\":\"460110745689999\"},{\"start\":\"460110748950000\",\"end\":\"460110750849999\"},{\"start\":\"460110760150000\",\"end\":\"460110760449999\"},{\"start\":\"460110765000000\",\"end\":\"460110765099999\"},{\"start\":\"460110780030000\",\"end\":\"460110783029999\"},{\"start\":\"460110797930000\",\"end\":\"460110798189999\"},{\"start\":\"460110799760000\",\"end\":\"460110799769999\"},{\"start\":\"460110799990000\",\"end\":\"460110800029999\"},{\"start\":\"460110806710000\",\"end\":\"460110809209999\"},{\"start\":\"460110821410000\",\"end\":\"460110823509999\"},{\"start\":\"460110830110000\",\"end\":\"460110830409999\"},{\"start\":\"460110830620000\",\"end\":\"460110830819999\"},{\"start\":\"460110842430000\",\"end\":\"460110845569999\"},{\"start\":\"460110856710000\",\"end\":\"460110858709999\"},{\"start\":\"460110865040000\",\"end\":\"460110865089999\"},{\"start\":\"460110878420000\",\"end\":\"460110881669999\"},{\"start\":\"460110911630000\",\"end\":\"460110911639999\"},{\"start\":\"460110911820000\",\"end\":\"460110911839999\"},{\"start\":\"460110917600000\",\"end\":\"460110925329999\"},{\"start\":\"460110955150000\",\"end\":\"460110955239999\"},{\"start\":\"460110963790000\",\"end\":\"460110976689999\"},{\"start\":\"460111215330000\",\"end\":\"460111215419999\"},{\"start\":\"460111222400000\",\"end\":\"460111225699999\"},{\"start\":\"460111401090000\",\"end\":\"460111401329999\"},{\"start\":\"460111423070000\",\"end\":\"460111423669999\"},{\"start\":\"460115240000000\",\"end\":\"460115320199999\"},{\"start\":\"460115917050000\",\"end\":\"460115923049999\"},{\"start\":\"460119991540000\",\"end\":\"460119991559999\"}]}}],\"nrfSupportedFeatures\":\"12\"}";
    private static final String RESULT_YUNNAN = "{\"validityPeriod\":86400,\"nfInstances\":[{\"nfInstanceId\":\"b902b476-621f-41cd-bf98-ff38a23c6557\",\"load\":22,\"nfStatus\":\"REGISTERED\",\"nfType\":\"CHF\",\"plmnList\":[{\"mnc\":\"11\",\"mcc\":\"460\"}],\"nfServicePersistence\":false,\"capacity\":9,\"fqdn\":\"scpyunnan\",\"sNssais\":[{\"sst\":1,\"sd\":\"040000\"},{\"sst\":1,\"sd\":\"080000\"},{\"sst\":1,\"sd\":\"f01001\"},{\"sst\":1,\"sd\":\"000001\"},{\"sst\":1,\"sd\":\"000000\"}],\"chfInfo\":{\"supiRangeList\":[{\"start\":\"460110550000000\",\"end\":\"460110560999999\"},{\"start\":\"460110560000000\",\"end\":\"460110569999999\"},{\"start\":\"460110561000000\",\"end\":\"460110562499999\"},{\"start\":\"460110685520000\",\"end\":\"460110685589999\"},{\"start\":\"460110691260000\",\"end\":\"460110691359999\"},{\"start\":\"460110696210000\",\"end\":\"460110696219999\"},{\"start\":\"460110703320000\",\"end\":\"460110703619999\"},{\"start\":\"460110734120000\",\"end\":\"460110734519999\"},{\"start\":\"460110765700000\",\"end\":\"460110765999999\"},{\"start\":\"460110798190000\",\"end\":\"460110798489999\"},{\"start\":\"460110799580000\",\"end\":\"460110799669999\"},{\"start\":\"460110811710000\",\"end\":\"460110811909999\"},{\"start\":\"460110825710000\",\"end\":\"460110825909999\"},{\"start\":\"460110861110000\",\"end\":\"460110861509999\"},{\"start\":\"460110887230000\",\"end\":\"460110888529999\"},{\"start\":\"460110911660000\",\"end\":\"460110911669999\"},{\"start\":\"460110933170000\",\"end\":\"460110934769999\"},{\"start\":\"460110981870000\",\"end\":\"460110983929999\"},{\"start\":\"460111216060000\",\"end\":\"460111216159999\"},{\"start\":\"460111234370000\",\"end\":\"460111235509999\"},{\"start\":\"460111402270000\",\"end\":\"460111402379999\"},{\"start\":\"460111425070000\",\"end\":\"460111425119999\"},{\"start\":\"460115439000000\",\"end\":\"460115459699999\"},{\"start\":\"460115909000000\",\"end\":\"460115909499999\"},{\"start\":\"460115932650000\",\"end\":\"460115934349999\"},{\"start\":\"460119991480000\",\"end\":\"460119991499999\"}]}}],\"nrfSupportedFeatures\":\"12\"}";
    private static final String RESULT_ZHEJIANG = "{\"validityPeriod\":86400,\"nfInstances\":[{\"nfInstanceId\":\"bf5bab7e-34f2-4ec3-8037-35ca2c5fc9cd\",\"load\":22,\"nfStatus\":\"REGISTERED\",\"nfType\":\"CHF\",\"plmnList\":[{\"mnc\":\"11\",\"mcc\":\"460\"}],\"nfServicePersistence\":false,\"capacity\":9,\"fqdn\":\"scpzhejiang\",\"sNssais\":[{\"sst\":1,\"sd\":\"f01001\"},{\"sst\":1,\"sd\":\"000001\"},{\"sst\":1,\"sd\":\"000000\"},{\"sst\":1,\"sd\":\"040000\"},{\"sst\":1,\"sd\":\"080000\"}],\"chfInfo\":{\"supiRangeList\":[{\"start\":\"460110190000000\",\"end\":\"460110220999999\"},{\"start\":\"460110220000000\",\"end\":\"460110229999999\"},{\"start\":\"460110221000000\",\"end\":\"460110223599999\"},{\"start\":\"460110670110000\",\"end\":\"460110670609999\"},{\"start\":\"460110685590000\",\"end\":\"460110686089999\"},{\"start\":\"460110689110000\",\"end\":\"460110690009999\"},{\"start\":\"460110698720000\",\"end\":\"460110699319999\"},{\"start\":\"460110715300000\",\"end\":\"460110716009999\"},{\"start\":\"460110727920000\",\"end\":\"460110729019999\"},{\"start\":\"460110748250000\",\"end\":\"460110748949999\"},{\"start\":\"460110759950000\",\"end\":\"460110760149999\"},{\"start\":\"460110763800000\",\"end\":\"460110764499999\"},{\"start\":\"460110777530000\",\"end\":\"460110778829999\"},{\"start\":\"460110799910000\",\"end\":\"460110799969999\"},{\"start\":\"460110804510000\",\"end\":\"460110805509999\"},{\"start\":\"460110820310000\",\"end\":\"460110821009999\"},{\"start\":\"460110851110000\",\"end\":\"460110851589999\"},{\"start\":\"460110855710000\",\"end\":\"460110856409999\"},{\"start\":\"460110874550000\",\"end\":\"460110876819999\"},{\"start\":\"460110911590000\",\"end\":\"460110911609999\"},{\"start\":\"460110911730000\",\"end\":\"460110911739999\"},{\"start\":\"460110917370000\",\"end\":\"460110917599999\"},{\"start\":\"460110954970000\",\"end\":\"460110955149999\"},{\"start\":\"460110955750000\",\"end\":\"460110955839999\"},{\"start\":\"460110962810000\",\"end\":\"460110963589999\"},{\"start\":\"460111221780000\",\"end\":\"460111222399999\"},{\"start\":\"460111400710000\",\"end\":\"460111400919999\"},{\"start\":\"460111421470000\",\"end\":\"460111422269999\"},{\"start\":\"460115159000000\",\"end\":\"460115205199999\"},{\"start\":\"460115915250000\",\"end\":\"460115916249999\"},{\"start\":\"460119991000000\",\"end\":\"460119991039999\"}]}}],\"nrfSupportedFeatures\":\"12\"}";
    private static final String RESULT_1I_1R = "{\"validityPeriod\":86400,\"nfInstances\":[{\"nfInstanceId\":\"bf5bab7e-34f2-4ec3-8037-35ca2c5fc9cd\",\"nfStatus\":\"REGISTERED\",\"nfType\":\"CHF\",\"fqdn\":\"sr_1i_1r\",\"chfInfo\":{\"supiRangeList\":[{\"start\":\"10\",\"end\":\"19\"}]}}]}";
    private static final String RESULT_1I_2R = "{\"validityPeriod\":86400,\"nfInstances\":[{\"nfInstanceId\":\"bf5bab7e-34f2-4ec3-8037-35ca2c5fc9cd\",\"nfStatus\":\"REGISTERED\",\"nfType\":\"CHF\",\"fqdn\":\"sr_1i_2r\",\"chfInfo\":{\"supiRangeList\":[{\"start\":\"10\",\"end\":\"19\"},{\"start\":\"30\",\"end\":\"39\"}]}}]}";
    private static final String RESULT_1I_3R = "{\"validityPeriod\":86400,\"nfInstances\":[{\"nfInstanceId\":\"bf5bab7e-34f2-4ec3-8037-35ca2c5fc9cd\",\"nfStatus\":\"REGISTERED\",\"nfType\":\"CHF\",\"fqdn\":\"sr_1i_2r\",\"chfInfo\":{\"supiRangeList\":[{\"start\":\"10\",\"end\":\"19\"},{\"start\":\"20\",\"end\":\"29\"},{\"start\":\"30\",\"end\":\"39\"}]}}]}";
    private static final String RESULT_2I_1R = "{\"validityPeriod\":86400,\"nfInstances\":[{\"nfInstanceId\":\"bf5bab7e-34f2-4ec3-8037-35ca2c5fc9cd\",\"nfStatus\":\"REGISTERED\",\"nfType\":\"CHF\",\"fqdn\":\"sr_2i_1r_1\",\"chfInfo\":{\"supiRangeList\":[{\"start\":\"10\",\"end\":\"19\"}]}},{\"nfInstanceId\":\"bf5bab7e-34f2-4ec3-8037-35ca2c5fc9ee\",\"nfStatus\":\"REGISTERED\",\"nfType\":\"CHF\",\"fqdn\":\"sr_2i_1r_2\",\"chfInfo\":{\"supiRangeList\":[{\"start\":\"10\",\"end\":\"19\"}]}}]}";

    private static final List<String> results = Arrays.asList(RESULT_ANHUI,
                                                              RESULT_BEIJING,
                                                              RESULT_CHONGQING,
                                                              RESULT_FUJIAN,
                                                              RESULT_GUANGDONG,
                                                              RESULT_GUANGXI,
                                                              RESULT_GUIZHOU,
                                                              RESULT_HAINAN,
                                                              RESULT_HEBEI,
                                                              RESULT_HENAN,
                                                              RESULT_HUBEI,
                                                              RESULT_HUNAN,
                                                              RESULT_JIANGSU,
                                                              RESULT_JIANGXI,
                                                              RESULT_LIAONING,
                                                              RESULT_NEIMENGGU,
                                                              RESULT_SHAANXI,
                                                              RESULT_SHANDONG,
                                                              RESULT_SHANGHAI,
                                                              RESULT_SICHUAN,
                                                              RESULT_YUNNAN,
                                                              RESULT_ZHEJIANG);

    private static final List<String> queries = Arrays.asList("supi=imsi-460110203512480",
                                                              "supi=imsi-460110249170796",
                                                              "supi=imsi-460115331363951",
                                                              "supi=imsi-460110047552506",
                                                              "supi=imsi-460110202401979",
                                                              "supi=imsi-460110261427719",
                                                              "supi=imsi-460110338515131",
                                                              "supi=imsi-460110213729165",
                                                              "supi=imsi-460115871873064",
                                                              "supi=imsi-460110247620742",
                                                              "supi=imsi-460115871811651",
                                                              "supi=imsi-460110795373205",
                                                              "supi=imsi-460115326360134",
                                                              "supi=imsi-460110331188789",
                                                              "supi=imsi-460115241117880",
                                                              "supi=imsi-460110125417328",
                                                              "supi=imsi-460115271291679",
                                                              "supi=imsi-460110267920088",
                                                              "supi=imsi-460110797138289",
                                                              "supi=imsi-460110335776886",
                                                              "supi=imsi-460110291201299",
                                                              "supi=imsi-460110195901125",
                                                              "supi=imsi-460110194971219",
                                                              "supi=imsi-460110199085754",
                                                              "supi=imsi-460115871912918",
                                                              "supi=imsi-460110261201762",
                                                              "supi=imsi-460115263615381",
                                                              "supi=imsi-460115332408999",
                                                              "supi=imsi-460115312530545",
                                                              "supi=imsi-460110267321952",
                                                              "supi=imsi-460115331425676",
                                                              "supi=imsi-460110311596908",
                                                              "supi=imsi-460110263339978",
                                                              "supi=imsi-460115344570492",
                                                              "supi=imsi-460110557301400",
                                                              "supi=imsi-460110297404994",
                                                              "supi=imsi-460115823889806",
                                                              "supi=imsi-460110336314430",
                                                              "supi=imsi-460110294301940",
                                                              "supi=imsi-460110261427719",
                                                              "supi=imsi-460115337236910",
                                                              "supi=imsi-460110558498650",
                                                              "supi=imsi-460115294131660",
                                                              "supi=imsi-460115330894713",
                                                              "supi=imsi-460110047936141",
                                                              "supi=imsi-460110333135636",
                                                              "supi=imsi-460110272676880",
                                                              "supi=imsi-460110249560547",
                                                              "supi=imsi-460110239872655",
                                                              "supi=imsi-460115661508343",
                                                              "supi=imsi-460110218270240",
                                                              "supi=imsi-460110211744020",
                                                              "supi=imsi-460110338648728",
                                                              "supi=imsi-460115871466838",
                                                              "supi=imsi-460115332202625",
                                                              "supi=imsi-460115294118135",
                                                              "supi=imsi-460115323563574",
                                                              "supi=imsi-460110333430216",
                                                              "supi=imsi-460115241117880",
                                                              "supi=imsi-460110339504395",
                                                              "supi=imsi-460110267599179",
                                                              "supi=imsi-460115871828228",
                                                              "supi=imsi-460115817974877",
                                                              "supi=imsi-460110272225298",
                                                              "supi=imsi-460110842207719",
                                                              "supi=imsi-460115661278654",
                                                              "supi=imsi-460115871816409",
                                                              "supi=imsi-460110206786115",
                                                              "supi=imsi-460110335429712",
                                                              "supi=imsi-460115271291679",
                                                              "supi=imsi-460115259473569",
                                                              "supi=imsi-460110272781330",
                                                              "supi=imsi-460110272753968",
                                                              "supi=imsi-460110263443616",
                                                              "supi=imsi-460110212567476",
                                                              "supi=imsi-460110219703594",
                                                              "supi=imsi-460110337620045",
                                                              "supi=imsi-460110297404994",
                                                              "supi=imsi-460115827365577",
                                                              "supi=imsi-460110314135735",
                                                              "supi=imsi-460110461221044",
                                                              "supi=imsi-460115332370774",
                                                              "supi=imsi-460110261427719",
                                                              "supi=imsi-460115331425676",
                                                              "supi=imsi-460115303694409",
                                                              "supi=imsi-460110533702571",
                                                              "supi=imsi-460110710054198",
                                                              "supi=imsi-460110249170796",
                                                              "supi=imsi-460110244159908",
                                                              "supi=imsi-460115871876976",
                                                              "supi=imsi-460110710225341",
                                                              "supi=imsi-460115863576262",
                                                              "supi=imsi-460115326275909",
                                                              "supi=imsi-460110268888994",
                                                              "supi=imsi-460110335634008",
                                                              "supi=imsi-460110199851872",
                                                              "supi=imsi-460110199350891",
                                                              "supi=imsi-460110797138289",
                                                              "supi=imsi-460110312831101",
                                                              "supi=imsi-460115862536039",
                                                              "supi=imsi-460115871618890",
                                                              "supi=imsi-460115862976973",
                                                              "supi=imsi-460110193701555",
                                                              "supi=imsi-460110338515131",
                                                              "supi=imsi-460110203923411",
                                                              "supi=imsi-460110905311747",
                                                              "supi=imsi-460110213083552",
                                                              "supi=imsi-460115871594997",
                                                              "supi=imsi-460110826669342",
                                                              "supi=imsi-460115661981627",
                                                              "supi=imsi-460110297858258",
                                                              "supi=imsi-460110842207719",
                                                              "supi=imsi-460115331495264",
                                                              "supi=imsi-460115294118135",
                                                              "supi=imsi-460115872200239",
                                                              "supi=imsi-460115294331161",
                                                              "supi=imsi-460110198915217",
                                                              "supi=imsi-460110554681212",
                                                              "supi=imsi-460110261411940",
                                                              "supi=imsi-460110291878080",
                                                              "supi=imsi-460110335776886",
                                                              "supi=imsi-460115302773448",
                                                              "supi=imsi-460110249170796",
                                                              "supi=imsi-460110465700007",
                                                              "supi=imsi-460115241117880",
                                                              "supi=imsi-460110297404994",
                                                              "supi=imsi-460115265495050",
                                                              "supi=imsi-460110212403226",
                                                              "supi=imsi-460110194365749",
                                                              "supi=imsi-460115261591738",
                                                              "supi=imsi-460115333935148",
                                                              "supi=imsi-460110195871569",
                                                              "supi=imsi-460115872206084",
                                                              "supi=imsi-460110121767586",
                                                              "supi=imsi-460115332137581",
                                                              "supi=imsi-460110295607753",
                                                              "supi=imsi-460110207467917",
                                                              "supi=imsi-460110335553120",
                                                              "supi=imsi-460110384137686",
                                                              "supi=imsi-460115661508343",
                                                              "supi=imsi-460115872401202",
                                                              "supi=imsi-460110211744020",
                                                              "supi=imsi-460115294131660",
                                                              "supi=imsi-460115316482109",
                                                              "supi=imsi-460115308416899",
                                                              "supi=imsi-460115273036347",
                                                              "supi=imsi-460115328715487",
                                                              "supi=imsi-460110246856194",
                                                              "supi=imsi-460110553811390",
                                                              "supi=imsi-460115518778924",
                                                              "supi=imsi-460110268778918",
                                                              "supi=imsi-460110235913392",
                                                              "supi=imsi-460115348220596",
                                                              "supi=imsi-460110462152015",
                                                              "supi=imsi-460115259473569",
                                                              "supi=imsi-460110046942182",
                                                              "supi=imsi-460110338648728",
                                                              "supi=imsi-460115272894456",
                                                              "supi=imsi-460110262402110",
                                                              "supi=imsi-460110202037382",
                                                              "supi=imsi-460115337589843",
                                                              "supi=imsi-460110217813664",
                                                              "supi=imsi-460115241854767",
                                                              "supi=imsi-460115335792946",
                                                              "supi=imsi-460110195901125",
                                                              "supi=imsi-460110218791776",
                                                              "supi=imsi-460110125194574",
                                                              "supi=imsi-460115303065907",
                                                              "supi=imsi-460110279564945",
                                                              "supi=imsi-460110339828619",
                                                              "supi=imsi-460110206640962",
                                                              "supi=imsi-460110331474257",
                                                              "supi=imsi-460110294741997",
                                                              "supi=imsi-460115347966095",
                                                              "supi=imsi-460115872401206",
                                                              "supi=imsi-460110336251389",
                                                              "supi=imsi-460110298934843",
                                                              "supi=imsi-460110313987482",
                                                              "supi=imsi-460110725646959",
                                                              "supi=imsi-460110204747835",
                                                              "supi=imsi-460110216481316",
                                                              "supi=imsi-460110314862252",
                                                              "supi=imsi-460115241117880",
                                                              "supi=imsi-460110194078145",
                                                              "supi=imsi-460110275696739",
                                                              "supi=imsi-460110297404994",
                                                              "supi=imsi-460110216581174",
                                                              "supi=imsi-460110293858161",
                                                              "supi=imsi-460110217789672",
                                                              "supi=imsi-460115303364363",
                                                              "supi=imsi-460110193485606",
                                                              "supi=imsi-460110466776317",
                                                              "supi=imsi-460110319763614",
                                                              "supi=imsi-460110214291837",
                                                              "supi=imsi-460115272126512",
                                                              "supi=imsi-460110211968973",
                                                              "supi=imsi-460110246856194",
                                                              "supi=imsi-460110195884121",
                                                              "supi=imsi-460110262699811",
                                                              "supi=imsi-460110128408115",
                                                              "supi=imsi-460110272225298",
                                                              "supi=imsi-460110233813722",
                                                              "supi=imsi-460110318349754",
                                                              "supi=imsi-460110338515131",
                                                              "supi=imsi-460110335808939",
                                                              "supi=imsi-460115348220596",
                                                              "supi=imsi-460110129448379",
                                                              "supi=imsi-460115312910310",
                                                              "supi=imsi-460110196464803",
                                                              "supi=imsi-460115344514764",
                                                              "supi=imsi-460110202783263",
                                                              "supi=imsi-460110312139585",
                                                              "supi=imsi-460110710091586",
                                                              "supi=imsi-460115324448927",
                                                              "supi=imsi-460110389605540",
                                                              "supi=imsi-460115871485248",
                                                              "supi=imsi-460115337582265",
                                                              "supi=imsi-460115872193268",
                                                              "supi=imsi-460115344570492",
                                                              "supi=imsi-460110249170796",
                                                              "supi=imsi-460115331461757",
                                                              "supi=imsi-460115343729495",
                                                              "supi=imsi-460115871515040",
                                                              "supi=imsi-460110315360100",
                                                              "supi=imsi-460115872186869",
                                                              "supi=imsi-460110313987482",
                                                              "supi=imsi-460115337493181",
                                                              "supi=imsi-460110264787692",
                                                              "supi=imsi-460115446992519",
                                                              "supi=imsi-460110268496801",
                                                              "supi=imsi-460110558498650",
                                                              "supi=imsi-460115303065907",
                                                              "supi=imsi-460110337711887",
                                                              "supi=imsi-460110204449745",
                                                              "supi=imsi-460110265084455",
                                                              "supi=imsi-460110193718355",
                                                              "supi=imsi-460110194078145",
                                                              "supi=imsi-460115506885243",
                                                              "supi=imsi-460115249434053",
                                                              "supi=imsi-460110211107212",
                                                              "supi=imsi-460110217214523",
                                                              "supi=imsi-460115331270621",
                                                              "supi=imsi-460110319888453",
                                                              "supi=imsi-460115259473569",
                                                              "supi=imsi-460110466776317",
                                                              "supi=imsi-460115821378772",
                                                              "supi=imsi-460115873080098",
                                                              "supi=imsi-460110314332301",
                                                              "supi=imsi-460115265957474",
                                                              "supi=imsi-460110218791776",
                                                              "supi=imsi-460115331644306",
                                                              "supi=imsi-460115263508494",
                                                              "supi=imsi-460115241117880",
                                                              "supi=imsi-460110298461383",
                                                              "supi=imsi-460115312530545",
                                                              "supi=imsi-460110389071749",
                                                              "supi=imsi-460110204671175",
                                                              "supi=imsi-460115271168399",
                                                              "supi=imsi-460110338398963",
                                                              "supi=imsi-460110335776886",
                                                              "supi=imsi-460115871492823",
                                                              "supi=imsi-460110211235083",
                                                              "supi=imsi-460115518492900",
                                                              "supi=imsi-460110249170796",
                                                              "supi=imsi-460110297404994",
                                                              "supi=imsi-460110332041669",
                                                              "supi=imsi-460110213188375",
                                                              "supi=imsi-460110203215253",
                                                              "supi=imsi-460115249680328",
                                                              "supi=imsi-460110293983212",
                                                              "supi=imsi-460110128986219",
                                                              "supi=imsi-460110212735869",
                                                              "supi=imsi-460115259269899",
                                                              "supi=imsi-460110295379525",
                                                              "supi=imsi-460115302921558",
                                                              "supi=imsi-460110269499316",
                                                              "supi=imsi-460110315441830",
                                                              "supi=imsi-460110246856194",
                                                              "supi=imsi-460110338325570",
                                                              "supi=imsi-460115275261021",
                                                              "supi=imsi-460115293118781",
                                                              "supi=imsi-460110195418188",
                                                              "supi=imsi-460110466776317",
                                                              "supi=imsi-460110316276688",
                                                              "supi=imsi-460110299589431",
                                                              "supi=imsi-460110264787692",
                                                              "supi=imsi-460110272225298",
                                                              "supi=imsi-460110201825656",
                                                              "supi=imsi-460110805359961",
                                                              "supi=imsi-460115330340218",
                                                              "supi=imsi-460110124878629",
                                                              "supi=imsi-460115324539786",
                                                              "supi=imsi-460115332170575",
                                                              "supi=imsi-460110338648728",
                                                              "supi=imsi-460110338515131",
                                                              "supi=imsi-460115518778924",
                                                              "supi=imsi-460110204631726",
                                                              "supi=imsi-460115241117880",
                                                              "supi=imsi-460115302772829",
                                                              "supi=imsi-460110268558762",
                                                              "supi=imsi-460110041843839",
                                                              "supi=imsi-460115249939410",
                                                              "supi=imsi-460110207698310",
                                                              "supi=imsi-460110265084455",
                                                              "supi=imsi-460110332041669",
                                                              "supi=imsi-460110827058193",
                                                              "supi=imsi-460110897510803",
                                                              "supi=imsi-460110261503140",
                                                              "supi=imsi-460110215893517",
                                                              "supi=imsi-460110339709471",
                                                              "supi=imsi-460110552995661",
                                                              "supi=imsi-460115268261740",
                                                              "supi=imsi-460115350222128",
                                                              "supi=imsi-460110246856194",
                                                              "supi=imsi-460115271168399",
                                                              "supi=imsi-460110218270240",
                                                              "supi=imsi-460110339125177",
                                                              "supi=imsi-460115869584279",
                                                              "supi=imsi-460110298461383",
                                                              "supi=imsi-460115303065907",
                                                              "supi=imsi-460110213239379",
                                                              "supi=imsi-460110333430216",
                                                              "supi=imsi-460110128846907",
                                                              "supi=imsi-460110245397192",
                                                              "supi=imsi-460110839516099",
                                                              "supi=imsi-460110333523920",
                                                              "supi=imsi-460110264838282",
                                                              "supi=imsi-460115871467830",
                                                              "supi=imsi-460110272225298",
                                                              "supi=imsi-460110248250718",
                                                              "supi=imsi-460115335791497",
                                                              "supi=imsi-460115330568254",
                                                              "supi=imsi-460110278034151",
                                                              "supi=imsi-460110246529741",
                                                              "supi=imsi-460110048135749",
                                                              "supi=imsi-460115328912398",
                                                              "supi=imsi-460110840452688",
                                                              "supi=imsi-460110339828619",
                                                              "supi=imsi-460115302772829",
                                                              "supi=imsi-460115255809845",
                                                              "supi=imsi-460110332043310",
                                                              "supi=imsi-460110805064330",
                                                              "supi=imsi-460110734849561",
                                                              "supi=imsi-460110195418188",
                                                              "supi=imsi-460115259269899",
                                                              "supi=imsi-460110788680058",
                                                              "supi=imsi-460110710091586",
                                                              "supi=imsi-460115249609559",
                                                              "supi=imsi-460110333507972",
                                                              "supi=imsi-460115259473569",
                                                              "supi=imsi-460115275266455",
                                                              "supi=imsi-460115355820840",
                                                              "supi=imsi-460110207698310",
                                                              "supi=imsi-460115324448927",
                                                              "supi=imsi-460110334360137",
                                                              "supi=imsi-460110297404994",
                                                              "supi=imsi-460115249936031",
                                                              "supi=imsi-460110267117888",
                                                              "supi=imsi-460110244204449",
                                                              "supi=imsi-460110333177636",
                                                              "supi=imsi-460115350222128",
                                                              "supi=imsi-460115324539786",
                                                              "supi=imsi-460110294593612",
                                                              "supi=imsi-460115275107314",
                                                              "supi=imsi-460110235444134",
                                                              "supi=imsi-460110897510803",
                                                              "supi=imsi-460110249170796",
                                                              "supi=imsi-460110336643632",
                                                              "supi=imsi-460110206621601",
                                                              "supi=imsi-460110335462477",
                                                              "supi=imsi-460115870715173",
                                                              "supi=imsi-460115350303825",
                                                              "supi=imsi-460110311275397",
                                                              "supi=imsi-460110795373205",
                                                              "supi=imsi-460115271129654",
                                                              "supi=imsi-460110216344779",
                                                              "supi=imsi-460110249560547",
                                                              "supi=imsi-460110194365749",
                                                              "supi=imsi-460115347966095",
                                                              "supi=imsi-460115266158676",
                                                              "supi=imsi-460110558498650",
                                                              "supi=imsi-460110219009803",
                                                              "supi=imsi-460110041843839",
                                                              "supi=imsi-460115662332011",
                                                              "supi=imsi-460115331363951",
                                                              "supi=imsi-460115275266455",
                                                              "supi=imsi-460115249680328",
                                                              "supi=imsi-460110554681212",
                                                              "supi=imsi-460115331461757",
                                                              "supi=imsi-460110216939534",
                                                              "supi=imsi-460110244777275",
                                                              "supi=imsi-460110192785454",
                                                              "supi=imsi-460110243756015",
                                                              "supi=imsi-460110246856194",
                                                              "supi=imsi-460110293534416",
                                                              "supi=imsi-460110238590850",
                                                              "supi=imsi-460110195843404",
                                                              "supi=imsi-460110195884121",
                                                              "supi=imsi-460110203215253",
                                                              "supi=imsi-460110205127937",
                                                              "supi=imsi-460110191242032",
                                                              "supi=imsi-460110333135636",
                                                              "supi=imsi-460110249170796",
                                                              "supi=imsi-460110203625196",
                                                              "supi=imsi-460115332912701",
                                                              "supi=imsi-460110335776886",
                                                              "supi=imsi-460110123294715",
                                                              "supi=imsi-460115294331161",
                                                              "supi=imsi-460115351450802",
                                                              "supi=imsi-460115303065907",
                                                              "supi=imsi-460110265084455",
                                                              "supi=imsi-460110337711887",
                                                              "supi=imsi-460110339828619",
                                                              "supi=imsi-460115269122023",
                                                              "supi=imsi-460115259432469",
                                                              "supi=imsi-460110195830763",
                                                              "supi=imsi-460110710091586",
                                                              "supi=imsi-460110338648728",
                                                              "supi=imsi-460110264193660",
                                                              "supi=imsi-460110196464803",
                                                              "supi=imsi-460115871475990",
                                                              "supi=imsi-460115337670352",
                                                              "supi=imsi-460115241117880",
                                                              "supi=imsi-460115271921519",
                                                              "supi=imsi-460110216382768",
                                                              "supi=imsi-460115275261021",
                                                              "supi=imsi-460115259473569",
                                                              "supi=imsi-460110246529741",
                                                              "supi=imsi-460110796060701",
                                                              "supi=imsi-460115330321864",
                                                              "supi=imsi-460110193701555",
                                                              "supi=imsi-460115275107314",
                                                              "supi=imsi-460115326360134",
                                                              "supi=imsi-460110267599179",
                                                              "supi=imsi-460110203132877",
                                                              "supi=imsi-460115348220596",
                                                              "supi=imsi-460110202457656",
                                                              "supi=imsi-460110234371251",
                                                              "supi=imsi-460110313085230",
                                                              "supi=imsi-460110233705001",
                                                              "supi=imsi-460110233495437",
                                                              "supi=imsi-460110192775321",
                                                              "supi=imsi-460110197888730",
                                                              "supi=imsi-460115331265647",
                                                              "supi=imsi-460115445968318",
                                                              "supi=imsi-460115871588844",
                                                              "supi=imsi-460115273479267",
                                                              "supi=imsi-460115347209492",
                                                              "supi=imsi-460110192048014",
                                                              "supi=imsi-460110314237326",
                                                              "supi=imsi-460115870485283",
                                                              "supi=imsi-460115350222128",
                                                              "supi=imsi-460115872419330",
                                                              "supi=imsi-460115328912398",
                                                              "supi=imsi-460110710091586",
                                                              "supi=imsi-460115873981418",
                                                              "supi=imsi-460110195830763",
                                                              "supi=imsi-460110318262655",
                                                              "supi=imsi-460110195504812",
                                                              "supi=imsi-460115518858456",
                                                              "supi=imsi-460115355820840",
                                                              "supi=imsi-460110318699174",
                                                              "supi=imsi-460110246529741",
                                                              "supi=imsi-460115871460172",
                                                              "supi=imsi-460110532993784",
                                                              "supi=imsi-460115347955803",
                                                              "supi=imsi-460115661972171",
                                                              "supi=imsi-460115241117880",
                                                              "supi=imsi-460110315624603",
                                                              "supi=imsi-460110262196762",
                                                              "supi=imsi-460115302921558",
                                                              "supi=imsi-460115337670352",
                                                              "supi=imsi-460110335429712",
                                                              "supi=imsi-460110796060701",
                                                              "supi=imsi-460110042679841",
                                                              "supi=imsi-460115333635270",
                                                              "supi=imsi-460115344625033",
                                                              "supi=imsi-460110719774173",
                                                              "supi=imsi-460115312685194",
                                                              "supi=imsi-460110710225341",
                                                              "supi=imsi-460110232348250",
                                                              "supi=imsi-460110295033573",
                                                              "supi=imsi-460110333523920",
                                                              "supi=imsi-460110267599179",
                                                              "supi=imsi-460115308416899",
                                                              "supi=imsi-460110199217448",
                                                              "supi=imsi-460110243478600",
                                                              "supi=imsi-460110558498650",
                                                              "supi=imsi-460115350222128",
                                                              "supi=imsi-460110212307993",
                                                              "supi=imsi-460110211235083",
                                                              "supi=imsi-460110249170796",
                                                              "supi=imsi-460110203132877",
                                                              "supi=imsi-460110264787692",
                                                              "supi=imsi-460110204327093",
                                                              "supi=imsi-460115328715487",
                                                              "supi=imsi-460115355820840",
                                                              "supi=imsi-460115348220596",
                                                              "supi=imsi-460110195830763",
                                                              "supi=imsi-460110294571305",
                                                              "supi=imsi-460110319009535",
                                                              "supi=imsi-460110216382768",
                                                              "supi=imsi-460110294416062",
                                                              "supi=imsi-460115863250082",
                                                              "supi=imsi-460115294590775",
                                                              "supi=imsi-460110298037307",
                                                              "supi=imsi-460110293983222",
                                                              "supi=imsi-460115326172263",
                                                              "supi=imsi-460115333424626",
                                                              "supi=imsi-460115332363683",
                                                              "supi=imsi-460115862838864",
                                                              "supi=imsi-460110268193216",
                                                              "supi=imsi-460115259473569",
                                                              "supi=imsi-460110532993784",
                                                              "supi=imsi-460110294224214",
                                                              "supi=imsi-460110213747881",
                                                              "supi=imsi-460115265499409",
                                                              "supi=imsi-460115450935904",
                                                              "supi=imsi-460115265957474",
                                                              "supi=imsi-460115249596396",
                                                              "supi=imsi-460110195872200",
                                                              "supi=imsi-460115346446730",
                                                              "supi=imsi-460110552265045",
                                                              "supi=imsi-460115827383805",
                                                              "supi=imsi-460110265084455",
                                                              "supi=imsi-460110551151984",
                                                              "supi=imsi-460110268558762",
                                                              "supi=imsi-460110293534416",
                                                              "supi=imsi-460110296067846",
                                                              "supi=imsi-460115332912701",
                                                              "supi=imsi-460110337086132",
                                                              "supi=imsi-460115312685194",
                                                              "supi=imsi-460115348274473",
                                                              "supi=imsi-460110335776886",
                                                              "supi=imsi-460110819597071",
                                                              "supi=imsi-460115869586537",
                                                              "supi=imsi-460110249170796",
                                                              "supi=imsi-460110335595819",
                                                              "supi=imsi-460110261092270",
                                                              "supi=imsi-460110243268538",
                                                              "supi=imsi-460115241117880",
                                                              "supi=imsi-460115344570492",
                                                              "supi=imsi-460110205024758",
                                                              "supi=imsi-460110203650112",
                                                              "supi=imsi-460110207180716",
                                                              "supi=imsi-460115347955803",
                                                              "supi=imsi-460110291746807",
                                                              "supi=imsi-460115331425676",
                                                              "supi=imsi-460110294925771",
                                                              "supi=imsi-460110796060701",
                                                              "supi=imsi-460115870492274",
                                                              "supi=imsi-460115308416899",
                                                              "supi=imsi-460110206793351",
                                                              "supi=imsi-460110538198363",
                                                              "supi=imsi-460110558000156",
                                                              "supi=imsi-460115350828123",
                                                              "supi=imsi-460110295883073",
                                                              "supi=imsi-460115273636807",
                                                              "supi=imsi-460110195645070",
                                                              "supi=imsi-460110216382768",
                                                              "supi=imsi-460110461055644",
                                                              "supi=imsi-460110207535657",
                                                              "supi=imsi-460115355820840",
                                                              "supi=imsi-460115265499409",
                                                              "supi=imsi-460110272225298",
                                                              "supi=imsi-460115871582030",
                                                              "supi=imsi-460110199910182",
                                                              "supi=imsi-460115261285279",
                                                              "supi=imsi-460110338325570",
                                                              "supi=imsi-460115871567506",
                                                              "supi=imsi-460110191335282",
                                                              "supi=imsi-460115255809845",
                                                              "supi=imsi-460110335808939",
                                                              "supi=imsi-460110218567148",
                                                              "supi=imsi-460110335926893",
                                                              "supi=imsi-460110263757183",
                                                              "supi=imsi-460110249560547",
                                                              "supi=imsi-460115337589843",
                                                              "supi=imsi-460110338648728",
                                                              "supi=imsi-460110311596908",
                                                              "supi=imsi-460115330475345",
                                                              "supi=imsi-460110261540256",
                                                              "supi=imsi-460115348274473",
                                                              "supi=imsi-460110294741997",
                                                              "supi=imsi-460115261591738",
                                                              "supi=imsi-460110272667957",
                                                              "supi=imsi-460115333972342",
                                                              "supi=imsi-460110337554971",
                                                              "supi=imsi-460110268558762",
                                                              "supi=imsi-460110123483883",
                                                              "supi=imsi-460115346446730",
                                                              "supi=imsi-460110312139585",
                                                              "supi=imsi-460110207591767",
                                                              "supi=imsi-460115308416899",
                                                              "supi=imsi-460115266125227",
                                                              "supi=imsi-460115259473569",
                                                              "supi=imsi-460115661278654",
                                                              "supi=imsi-460110218522975",
                                                              "supi=imsi-460110121139504",
                                                              "supi=imsi-460115271451318",
                                                              "supi=imsi-460110195177665",
                                                              "supi=imsi-460110335776886",
                                                              "supi=imsi-460115275107314",
                                                              "supi=imsi-460115336977050",
                                                              "supi=imsi-460115508165766",
                                                              "supi=imsi-460110211968973",
                                                              "supi=imsi-460110335691125",
                                                              "supi=imsi-460110199910182",
                                                              "supi=imsi-460115348220596",
                                                              "supi=imsi-460110207180716",
                                                              "supi=imsi-460110272782709",
                                                              "supi=imsi-460110905315282",
                                                              "supi=imsi-460115259389714",
                                                              "supi=imsi-460110239828188",
                                                              "supi=imsi-460110235427502",
                                                              "supi=imsi-460110299111427",
                                                              "supi=imsi-460110043887559",
                                                              "supi=imsi-460110249170796",
                                                              "supi=imsi-460110191242032",
                                                              "supi=imsi-460110796060701",
                                                              "supi=imsi-460110339214661",
                                                              "supi=imsi-460110123483883",
                                                              "supi=imsi-460115661297930",
                                                              "supi=imsi-460115333972342",
                                                              "supi=imsi-460110294571305",
                                                              "supi=imsi-460115303661189",
                                                              "supi=imsi-460110336314430",
                                                              "supi=imsi-460110219031871",
                                                              "supi=imsi-460110243478600",
                                                              "supi=imsi-460115346446730",
                                                              "supi=imsi-460115241117880",
                                                              "supi=imsi-460115872223102",
                                                              "supi=imsi-460115332170575",
                                                              "supi=imsi-460110789051665",
                                                              "supi=imsi-460110333665447",
                                                              "supi=imsi-460115263615381",
                                                              "supi=imsi-460110249560547",
                                                              "supi=imsi-460110794808191",
                                                              "supi=imsi-460110466776317",
                                                              "supi=imsi-460115259463428",
                                                              "supi=imsi-460115249304372",
                                                              "supi=imsi-460110461221044",
                                                              "supi=imsi-460110194806261",
                                                              "supi=imsi-460115273636807",
                                                              "supi=imsi-460115249609559",
                                                              "supi=imsi-460110216382768",
                                                              "supi=imsi-460110207180716",
                                                              "supi=imsi-460110207481729",
                                                              "supi=imsi-460110232489738",
                                                              "supi=imsi-460115344625033",
                                                              "supi=imsi-460110262060421",
                                                              "supi=imsi-460110272706116",
                                                              "supi=imsi-460110239828188",
                                                              "supi=imsi-460110196024307",
                                                              "supi=imsi-460115348220596",
                                                              "supi=imsi-460115265957474",
                                                              "supi=imsi-460110249170796",
                                                              "supi=imsi-460110262519963",
                                                              "supi=imsi-460110197351112",
                                                              "supi=imsi-460115344527039",
                                                              "supi=imsi-460115871582608",
                                                              "supi=imsi-460110295033573",
                                                              "supi=imsi-460115331606305",
                                                              "supi=imsi-460110213958820",
                                                              "supi=imsi-460115862964670",
                                                              "supi=imsi-460110338648728",
                                                              "supi=imsi-460110335776886",
                                                              "supi=imsi-460110123924412",
                                                              "supi=imsi-460110202296877",
                                                              "supi=imsi-460115259473569",
                                                              "supi=imsi-460110206831320",
                                                              "supi=imsi-460110819597071",
                                                              "supi=imsi-460115872170766",
                                                              "supi=imsi-460115332202943",
                                                              "supi=imsi-460110268622630",
                                                              "supi=imsi-460115333641459",
                                                              "supi=imsi-460115518492900",
                                                              "supi=imsi-460110315360100",
                                                              "supi=imsi-460110339747858",
                                                              "supi=imsi-460110249560547",
                                                              "supi=imsi-460115241117880",
                                                              "supi=imsi-460110201389452",
                                                              "supi=imsi-460110333177636",
                                                              "supi=imsi-460110796060701",
                                                              "supi=imsi-460110333135636",
                                                              "supi=imsi-460115872428421",
                                                              "supi=imsi-460110128547285",
                                                              "supi=imsi-460115337487564",
                                                              "supi=imsi-460115344570492",
                                                              "supi=imsi-460110333523920",
                                                              "supi=imsi-460110203926646",
                                                              "supi=imsi-460110246856194",
                                                              "supi=imsi-460110275501153",
                                                              "supi=imsi-460110291201299",
                                                              "supi=imsi-460110338515131",
                                                              "supi=imsi-460115272873654",
                                                              "supi=imsi-460110293795856",
                                                              "supi=imsi-460110218270240",
                                                              "supi=imsi-460115870923879",
                                                              "supi=imsi-460110339128763",
                                                              "supi=imsi-460110214651504",
                                                              "supi=imsi-460115350222128",
                                                              "supi=imsi-460110337604578",
                                                              "supi=imsi-460110335862606",
                                                              "supi=imsi-460115249511276",
                                                              "supi=imsi-460110266654466",
                                                              "supi=imsi-460115261769817",
                                                              "supi=imsi-460110233991134",
                                                              "supi=imsi-460110218453874",
                                                              "supi=imsi-460110554865994",
                                                              "supi=imsi-460110339833076",
                                                              "supi=imsi-460110213083552",
                                                              "supi=imsi-460115872186869",
                                                              "supi=imsi-460110277905752",
                                                              "supi=imsi-460115326273345",
                                                              "supi=imsi-460110248250718",
                                                              "supi=imsi-460110291892980",
                                                              "supi=imsi-460110124878629",
                                                              "supi=imsi-460110839437892",
                                                              "supi=imsi-460115344570492",
                                                              "supi=imsi-460115516607267",
                                                              "supi=imsi-460115255867723",
                                                              "supi=imsi-460110268496801",
                                                              "supi=imsi-460110233705001",
                                                              "supi=imsi-460115259389714",
                                                              "supi=imsi-460110049768677",
                                                              "supi=imsi-460110231434669",
                                                              "supi=imsi-460110216382768",
                                                              "supi=imsi-460110207698310",
                                                              "supi=imsi-460110249560547",
                                                              "supi=imsi-460110335776886",
                                                              "supi=imsi-460110218567148",
                                                              "supi=imsi-460110195498751",
                                                              "supi=imsi-460115871511340",
                                                              "supi=imsi-460110045138545",
                                                              "supi=imsi-460110192716460",
                                                              "supi=imsi-460110123483883",
                                                              "supi=imsi-460110291746807",
                                                              "supi=imsi-460110315917501",
                                                              "supi=imsi-460115275261021",
                                                              "supi=imsi-460110193011631",
                                                              "supi=imsi-460115350222128",
                                                              "supi=imsi-460115870291890",
                                                              "supi=imsi-460110207467917",
                                                              "supi=imsi-460110212567476",
                                                              "supi=imsi-460110201471288",
                                                              "supi=imsi-460110214177485",
                                                              "supi=imsi-460110195418188",
                                                              "supi=imsi-460110335926893",
                                                              "supi=imsi-460110461543271",
                                                              "supi=imsi-460110249170796",
                                                              "supi=imsi-460115259473569",
                                                              "supi=imsi-460110261632161",
                                                              "supi=imsi-460110316779534",
                                                              "supi=imsi-460115328715487",
                                                              "supi=imsi-460110333665447",
                                                              "supi=imsi-460110128682393",
                                                              "supi=imsi-460110195901125",
                                                              "supi=imsi-460115268541039",
                                                              "supi=imsi-460110796783408",
                                                              "supi=imsi-460110124198193",
                                                              "supi=imsi-460115241117880",
                                                              "supi=imsi-460110196319843",
                                                              "supi=imsi-460115873354238",
                                                              "supi=imsi-460115871907932",
                                                              "supi=imsi-460115244141382",
                                                              "supi=imsi-460115331425676",
                                                              "supi=imsi-460110202545228",
                                                              "supi=imsi-460115249590807",
                                                              "supi=imsi-460110296067846",
                                                              "supi=imsi-460110267175048",
                                                              "supi=imsi-460110753325053",
                                                              "supi=imsi-460110267599179",
                                                              "supi=imsi-460115321129018",
                                                              "supi=imsi-460110246856194",
                                                              "supi=imsi-460110293867031",
                                                              "supi=imsi-460115871459765",
                                                              "supi=imsi-460110204489320",
                                                              "supi=imsi-460110269499316",
                                                              "supi=imsi-460115294015659",
                                                              "supi=imsi-460110267746979",
                                                              "supi=imsi-460110338648728",
                                                              "supi=imsi-460115331820988",
                                                              "supi=imsi-460115870454557",
                                                              "supi=imsi-460110234442398",
                                                              "supi=imsi-460110191017581",
                                                              "supi=imsi-460115265902648",
                                                              "supi=imsi-460115271018350",
                                                              "supi=imsi-460110201471288",
                                                              "supi=imsi-460110465260022",
                                                              "supi=imsi-460115249712441",
                                                              "supi=imsi-460110295943435",
                                                              "supi=imsi-460115271966885",
                                                              "supi=imsi-460110296098182",
                                                              "supi=imsi-460110194293010",
                                                              "supi=imsi-460115330894713",
                                                              "supi=imsi-460110249170796",
                                                              "supi=imsi-460115538294492",
                                                              "supi=imsi-460115871454192",
                                                              "supi=imsi-460110192383104",
                                                              "supi=imsi-460110335776886",
                                                              "supi=imsi-460115348220596",
                                                              "supi=imsi-460110316343018",
                                                              "supi=imsi-460110198921786",
                                                              "supi=imsi-460110337711887",
                                                              "supi=imsi-460115871506423",
                                                              "supi=imsi-460110233705001",
                                                              "supi=imsi-460110246856194",
                                                              "supi=imsi-460110216382768",
                                                              "supi=imsi-460115272873654",
                                                              "supi=imsi-460110195336828",
                                                              "supi=imsi-460115241117880",
                                                              "supi=imsi-460110318793353",
                                                              "supi=imsi-460110292719863",
                                                              "supi=imsi-460110804621768",
                                                              "supi=imsi-460110338243312",
                                                              "supi=imsi-460115265884120",
                                                              "supi=imsi-460115302921558",
                                                              "supi=imsi-460115259398255",
                                                              "supi=imsi-460110383133179",
                                                              "supi=imsi-460110311635438",
                                                              "supi=imsi-460110533702571",
                                                              "supi=imsi-460110796783408",
                                                              "supi=imsi-460110196464803",
                                                              "supi=imsi-460110243478600",
                                                              "supi=imsi-460115663383184",
                                                              "supi=imsi-460110212403226",
                                                              "supi=imsi-460115332405364",
                                                              "supi=imsi-460110234570045",
                                                              "supi=imsi-460110046154420",
                                                              "supi=imsi-460115261676016",
                                                              "supi=imsi-460115294626717",
                                                              "supi=imsi-460115865703484",
                                                              "supi=imsi-460110335975530",
                                                              "supi=imsi-460115244141382",
                                                              "supi=imsi-460110332895302",
                                                              "supi=imsi-460110292003281",
                                                              "supi=imsi-460115817974877",
                                                              "supi=imsi-460110294380299",
                                                              "supi=imsi-460110333135636",
                                                              "supi=imsi-460115870489118",
                                                              "supi=imsi-460115246757880",
                                                              "supi=imsi-460110294267366",
                                                              "supi=imsi-460115871597053",
                                                              "supi=imsi-460115259473569",
                                                              "supi=imsi-460115324655747",
                                                              "supi=imsi-460110264787692",
                                                              "supi=imsi-460110128822498",
                                                              "supi=imsi-460110192716460",
                                                              "supi=imsi-460110233123680",
                                                              "supi=imsi-460110266485807",
                                                              "supi=imsi-460110193296183",
                                                              "supi=imsi-460110756801454",
                                                              "supi=imsi-460110314896052",
                                                              "supi=imsi-460110296067846",
                                                              "supi=imsi-460110272708802",
                                                              "supi=imsi-460115342510454",
                                                              "supi=imsi-460110216382768",
                                                              "supi=imsi-460115662579627",
                                                              "supi=imsi-460110314135735",
                                                              "supi=imsi-460115348220596",
                                                              "supi=imsi-460110293867031",
                                                              "supi=imsi-460110246856194",
                                                              "supi=imsi-460115259226046",
                                                              "supi=imsi-460110197156155",
                                                              "supi=imsi-460115274463847",
                                                              "supi=imsi-460110191384093",
                                                              "supi=imsi-460110272772546",
                                                              "supi=imsi-460115249936031",
                                                              "supi=imsi-460110272666077",
                                                              "supi=imsi-460110383348079",
                                                              "supi=imsi-460115527246322",
                                                              "supi=imsi-460110204525778",
                                                              "supi=imsi-460110191017581",
                                                              "supi=imsi-460110202362539",
                                                              "supi=imsi-460110293605437",
                                                              "supi=imsi-460115259398255",
                                                              "supi=imsi-460110125417328",
                                                              "supi=imsi-460110246529741",
                                                              "supi=imsi-460110272781330",
                                                              "supi=imsi-460115516601005",
                                                              "supi=imsi-460115344625247",
                                                              "supi=imsi-460110249170796",
                                                              "supi=imsi-460115343804160",
                                                              "supi=imsi-460110294331803",
                                                              "supi=imsi-460110262079984",
                                                              "supi=imsi-460110711873670",
                                                              "supi=imsi-460115275261021",
                                                              "supi=imsi-460110203520726",
                                                              "supi=imsi-460110193458175",
                                                              "supi=imsi-460110271238422",
                                                              "supi=imsi-460110335776886",
                                                              "supi=imsi-460110314896052",
                                                              "supi=imsi-460115455482147",
                                                              "supi=imsi-460110243478600",
                                                              "supi=imsi-460115871577858",
                                                              "supi=imsi-460110214732702",
                                                              "supi=imsi-460115869827659",
                                                              "supi=imsi-460115871522819",
                                                              "supi=imsi-460115356117755",
                                                              "supi=imsi-460115871932941",
                                                              "supi=imsi-460110338648728",
                                                              "supi=imsi-460110268604537",
                                                              "supi=imsi-460110264787692",
                                                              "supi=imsi-460115515640839",
                                                              "supi=imsi-460110464347892",
                                                              "supi=imsi-460115266158676",
                                                              "supi=imsi-460115331363951");

    private static final Logger log = LoggerFactory.getLogger(NnrfDiscSearchResultDbTest.class);
    private static final ObjectMapper mapper = OpenApiObjectMapper.singleton();

    private static void findAllEveryWhere(final List<String> results) throws JsonMappingException, JsonProcessingException
    {
        final NnrfDiscSearchResultDb2 db = new NnrfDiscSearchResultDb2();

        for (int i = 0; i < results.size(); ++i)
        {
            final Item item = new Item(NRF_GROUP, "", mapper.readValue(results.get(results.size() - 1 - i), SearchResult.class));
            db.add(item);
        }

        double durationTotal = 0;
        long numGetsTotal = 0;

        for (int i = 0; i < results.size(); ++i)
        {
            final SearchResult result = mapper.readValue(results.get(i), SearchResult.class);

            final NFProfile nfProfile = result.getNfInstances().get(0);

            log.info("Checking all ranges of {} ({})", nfProfile.getFqdn(), nfProfile.getChfInfo().getSupiRangeList().size());

            double duration = 0;
            long numGets = 0;

            for (final SupiRange range : nfProfile.getChfInfo().getSupiRangeList())
            {
                final long lo = Long.parseLong(range.getStart());
                final long hi = Long.parseLong(range.getEnd());

                log.info("Checking range {}-{}, size={}", lo, hi, hi - lo + 1);

                for (long j = lo; j <= hi; j += 100)
                {
                    final Query query = new Query.Builder().add("requester-nf-type", NFType.SMF)
                                                           .add("target-nf-type", NFType.CHF)
                                                           .add("supi", String.valueOf(j))
                                                           .build();
                    long start = System.currentTimeMillis();
                    final Set<Item> items = db.get(NRF_GROUP, query);
                    duration += System.currentTimeMillis() - start;
                    ++numGets;

                    if (items.isEmpty())
                    {
                        log.info("{} not found in {}", j, nfProfile.getFqdn());
                    }
                    else
                    {
                        for (Item item : items)
                        {
                            if (!item.getData().getNfInstances().get(0).getFqdn().equals(nfProfile.getFqdn()))
                                log.info("{} not found in {} but in {}", j, nfProfile.getFqdn(), item.getData().getNfInstances().get(0).getFqdn());
//                            else
//                                log.info("{} --> found in {}", j, item.getData().getNfInstances().get(0).getFqdn());
                        }
                    }
                }
            }

            durationTotal += duration;
            numGetsTotal += numGets;
            log.info("Average time for getting an item from the DB [ms]: {}", duration / numGets);
        }

        log.info("Total average time for getting an item from the DB [ms]: {}", durationTotal / numGetsTotal);
    }

    private static void findEveryWhere(final List<String> results,
                                       final boolean expectedResult) throws JsonMappingException, JsonProcessingException
    {
        final NnrfDiscSearchResultDb2 db = new NnrfDiscSearchResultDb2();

        for (int i = 0; i < results.size(); ++i)
        {
            final Item item = new Item(NRF_GROUP, "", mapper.readValue(results.get(i), SearchResult.class));
            db.add(item);
        }

//        log.info("results={}", results);
//        log.info("db={}", db);

        for (int i = 0; i < queries.size(); ++i)
        {
            final String[] tokens = queries.get(i).split("=");
            final Query.Builder qb = new Query.Builder().add("requester-nf-type", NFType.SMF).add("target-nf-type", NFType.CHF).add(tokens[0], tokens[1]);
            final Set<Item> items = db.get(NRF_GROUP, qb.build());

            if (items.isEmpty())
            {
                log.info("{} --> not found", tokens[1]);
            }
            else
            {
                items.stream()//
                     .flatMap(item -> item.getData().getNfInstances().stream())
                     .forEach(p -> log.info("{} --> found in {}", tokens[1], p.getFqdn()));
            }

            if (expectedResult)
                Assertions.assertFalse(items.isEmpty(), tokens[1] + " not found");
            else
                Assertions.assertTrue(items.isEmpty(), tokens[1] + " found");
        }
    }

    private static void findInEveryWhereIncrementally(final List<String> results) throws JsonMappingException, JsonProcessingException
    {
        for (int i = 0; i < results.size(); ++i)
        {
            final NnrfDiscSearchResultDb2 db = new NnrfDiscSearchResultDb2();

            final Item item = new Item(NRF_GROUP, "", NnrfDiscSearchResultDbTest.mapper.readValue(results.get(i), SearchResult.class));
            db.add(item);

//            log.info("db={}", db);

            final String[] tokens = NnrfDiscSearchResultDbTest.queries.get(0).split("=");
            final Query.Builder qb = new Query.Builder().add("requester-nf-type", NFType.SMF).add("target-nf-type", NFType.CHF).add(tokens[0], tokens[1]);
            final Set<Item> items = db.get(NRF_GROUP, qb.build());

            if (items.isEmpty())
            {
//                log.info("{} not found {} in {}", NnrfDiscSearchResultDb2Test.queries.get(0), results.get(i));
            }
            else
            {
                log.info("{} --> found in {}", NnrfDiscSearchResultDbTest.queries.get(0), results.get(i));
            }
        }
    }

    private static List<File> getAllFilesFromResourceFolder(final String folder) throws URISyntaxException, IOException
    {
        return Files.walk(Paths.get(NnrfDiscSearchResultDbTest.class.getClassLoader().getResource(folder).toURI()))
                    .filter(Files::isRegularFile)
                    .map(x -> x.toFile())
//                    .filter(f -> f.getName().contains("Shanghai"))
                    .collect(Collectors.toList());
    }

    private static void storeDuplicate() throws JsonMappingException, JsonProcessingException
    {
        final Map<Item, Item> cache = new HashMap<>();
        final NnrfDiscSearchResultDb2 db = new NnrfDiscSearchResultDb2();

        final Query qr1 = new Query.Builder().add("requester-nf-type", NFType.SMF).add("target-nf-type", NFType.CHF).add("supi", "imsi-11").build();
        final Query qr2 = new Query.Builder().add("requester-nf-type", NFType.SMF).add("target-nf-type", NFType.CHF).add("supi", "imsi-21").build();
        final Query qr3 = new Query.Builder().add("requester-nf-type", NFType.SMF).add("target-nf-type", NFType.CHF).add("supi", "imsi-31").build();
        Set<Item> results;

        final Item item1i2r = new Item(NRF_GROUP, "", mapper.readValue(RESULT_1I_2R, SearchResult.class));

        {
            final Item replacedItem = cache.put(item1i2r, item1i2r);

            if (replacedItem != null)
                db.remove(replacedItem);

            db.add(item1i2r);

            log.info("db={}", db);
            results = db.get(NRF_GROUP, qr1);
            log.info("size={}, results={}", results.size(), results);
            results = db.get(NRF_GROUP, qr2);
            log.info("size={}, results={}", results.size(), results);
            results = db.get(NRF_GROUP, qr3);
            log.info("size={}, results={}", results.size(), results);
        }

        {
            final Item replacedItem = cache.put(item1i2r, item1i2r);

            if (replacedItem != null)
                db.remove(replacedItem);

            db.add(item1i2r);

            log.info("db={}", db);
            results = db.get(NRF_GROUP, qr1);
            log.info("size={}, results={}", results.size(), results);
            results = db.get(NRF_GROUP, qr2);
            log.info("size={}, results={}", results.size(), results);
            results = db.get(NRF_GROUP, qr3);
            log.info("size={}, results={}", results.size(), results);
        }

        final Item item1i1r = new Item(NRF_GROUP, "", mapper.readValue(RESULT_1I_1R, SearchResult.class));

        {
            final Item replacedItem = cache.put(item1i1r, item1i1r);

            if (replacedItem != null)
                db.remove(replacedItem);

            db.add(item1i1r);

            log.info("db={}", db);
            results = db.get(NRF_GROUP, qr1);
            log.info("size={}, results={}", results.size(), results);
            results = db.get(NRF_GROUP, qr2);
            log.info("size={}, results={}", results.size(), results);
            results = db.get(NRF_GROUP, qr3);
            log.info("size={}, results={}", results.size(), results);
        }

        {
            final Item replacedItem = cache.put(item1i1r, item1i1r);

            if (replacedItem != null)
                db.remove(replacedItem);

            db.add(item1i1r);

            log.info("db={}", db);
            results = db.get(NRF_GROUP, qr1);
            log.info("size={}, results={}", results.size(), results);
            results = db.get(NRF_GROUP, qr2);
            log.info("size={}, results={}", results.size(), results);
            results = db.get(NRF_GROUP, qr3);
            log.info("size={}, results={}", results.size(), results);
        }

        final Item item1i3r = new Item(NRF_GROUP, "", mapper.readValue(RESULT_1I_3R, SearchResult.class));

        {
            final Item replacedItem = cache.put(item1i3r, item1i3r);

            if (replacedItem != null)
                db.remove(replacedItem);

            db.add(item1i3r);

            log.info("db={}", db);
            results = db.get(NRF_GROUP, qr1);
            log.info("size={}, results={}", results.size(), results);
            results = db.get(NRF_GROUP, qr2);
            log.info("size={}, results={}", results.size(), results);
            results = db.get(NRF_GROUP, qr3);
            log.info("size={}, results={}", results.size(), results);
        }

        {
            final Item replacedItem = cache.put(item1i3r, item1i3r);

            if (replacedItem != null)
                db.remove(replacedItem);

            db.add(item1i3r);

            log.info("db={}", db);
            results = db.get(NRF_GROUP, qr1);
            log.info("size={}, results={}", results.size(), results);
            results = db.get(NRF_GROUP, qr2);
            log.info("size={}, results={}", results.size(), results);
            results = db.get(NRF_GROUP, qr3);
            log.info("size={}, results={}", results.size(), results);
        }

        final Item item2i1r = new Item(NRF_GROUP, "", mapper.readValue(RESULT_2I_1R, SearchResult.class));

        {
            final Item replacedItem = cache.put(item2i1r, item2i1r);

            if (replacedItem != null)
                db.remove(replacedItem);

            db.add(item2i1r);

            log.info("db={}", db);
            results = db.get(NRF_GROUP, qr1);
            log.info("size={}, results={}", results.size(), results);
            results = db.get(NRF_GROUP, qr2);
            log.info("size={}, results={}", results.size(), results);
            results = db.get(NRF_GROUP, qr3);
            log.info("size={}, results={}", results.size(), results);
        }

        {
            final Item replacedItem = cache.put(item2i1r, item2i1r);

            if (replacedItem != null)
                db.remove(replacedItem);

            db.add(item2i1r);

            log.info("db={}", db);
            results = db.get(NRF_GROUP, qr1);
            log.info("size={}, results={}", results.size(), results);
            results = db.get(NRF_GROUP, qr2);
            log.info("size={}, results={}", results.size(), results);
            results = db.get(NRF_GROUP, qr3);
            log.info("size={}, results={}", results.size(), results);
        }

    }

    @Test
    void testFindInOriginalSearchResults() throws URISyntaxException, IOException
    {
        log.info("testFindInOriginalSearchResults");

        final String folder = "com/ericsson/cnal/nrf/r16/scds450/searchresults/original";
        final List<String> results = getAllFilesFromResourceFolder(folder).stream()//
                                                                          .map(file ->
                                                                          {
                                                                              try
                                                                              {
//                                                                                  log.info("Reading file {}", file.toPath());
                                                                                  return Files.readString(file.toPath());
                                                                              }
                                                                              catch (IOException e)
                                                                              {
                                                                                  e.printStackTrace();
                                                                                  return null;
                                                                              }
                                                                          })
                                                                          .collect(Collectors.toList());

        findEveryWhere(results, true); // With the corrections (splitRanges and stickyWildcard) all SUPIS should be
                                       // found
    }

    @Test
    void testFindInUpdatedNfProfiles() throws URISyntaxException, IOException
    {
        log.info("testFindInUpdatedNfProfiles");

        final String folder = "com/ericsson/cnal/nrf/r16/scds450/nfprofiles/converted";
        final List<String> results = getAllFilesFromResourceFolder(folder).stream()//
                                                                          .map(file ->
                                                                          {
                                                                              try
                                                                              {
                                                                                  return mapper.writeValueAsString(new SearchResult().validityPeriod(86400)
                                                                                                                                     .nrfSupportedFeatures("12")
                                                                                                                                     .nfInstances(Arrays.asList(mapper.readValue(mapper.writeValueAsString(mapper.readValue(Files.readString(file.toPath()),
                                                                                                                                                                                                                            com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFProfile.class)),
                                                                                                                                                                                 NFProfile.class))));
                                                                              }
                                                                              catch (IOException e)
                                                                              {
                                                                                  e.printStackTrace();
                                                                                  return null;
                                                                              }
                                                                          })
                                                                          .collect(Collectors.toList());

        findEveryWhere(results, true); // All of the SUPIs must be found.
    }

    @Test
    void testFindInZhejiang() throws JsonMappingException, JsonProcessingException
    {
        log.info("testFindInZhejiang");

        final NnrfDiscSearchResultDb2 db = new NnrfDiscSearchResultDb2();

        final Item item = new Item(NRF_GROUP, "", mapper.readValue(RESULT_ZHEJIANG, SearchResult.class));
        db.add(item);

//        log.info("db={}", db);

        final String[] tokens = queries.get(0).split("=");
        final Query.Builder qb = new Query.Builder().add("requester-nf-type", NFType.SMF).add("target-nf-type", NFType.CHF).add(tokens[0], tokens[1]);
        final Set<Item> results = db.get(NRF_GROUP, qb.build());

        Assertions.assertFalse(results.isEmpty(), tokens[1] + " not found");
    }

//    @Test
    void testSimilarToTc60InCi()
    {
        TestSimilarToTc60InCi.performTest();
    }
}
