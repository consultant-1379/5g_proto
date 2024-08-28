package com.ericsson.sc.slf;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cnal.nrf.r17.NnrfDiscSearchResultDb2.Item;
import com.ericsson.cnal.nrf.r17.NrfAdapter.Query;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.custom.NFProfile;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.custom.SearchResult;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.ChfInfo;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFStatus;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFType;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.SupiRange;
import com.ericsson.sc.slf.SlfWorker.Cache;
import com.ericsson.utilities.http.Url;
import com.ericsson.utilities.reactivex.VertxInstance;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpVersion;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.reactivex.ext.web.client.WebClient;

class SlfWorkerCacheTest
{
    private static class Latency
    {
        private static Map<Double, Long> histogram = new ConcurrentSkipListMap<>();

        public static void main(String[] args) throws InterruptedException
        {
            Latency l;

            l = new Latency();
            Thread.sleep(1000);
            l.stop();

            Latency.printHistogram();

            l = new Latency();
            Thread.sleep(1500);
            l.stop();

            Latency.printHistogram();

            l = new Latency();
            Thread.sleep(287);
            l.stop();

            Latency.printHistogram();

            l = new Latency();
            Thread.sleep(634);
            l.stop();

            Latency.printHistogram();

            l = new Latency();
            Thread.sleep(1000);
            l.stop();

            Latency.printHistogram();
        }

        public static void printHistogram()
        {
            log.info("{}", histogram);
        }

        private long start = 0l;

        public Latency()
        {
            this.start = System.currentTimeMillis();
        }

        public void stop()
        {
            double latency = Math.round((System.currentTimeMillis() - this.start) / 10d) / 100d; // 1287 ms -> 1.29 s
            histogram.compute(latency,
                              (k,
                               v) -> (v == null) ? 1 : v + 1);
        }
    }

    private static class TestSimilarToTc60InCi
    {
        private static final long SUPI_START_PRE = 4601000000L; // SUPI range: "460020000000000" - "460020000099999"
        private static final String SUPI_START_SUFF_MIN = "00000";
        private static final String SUPI_START_SUFF_MAX = "99999";
        private static final Integer SUPI_RANGE_MAX = 500000; // 500000;
        private static final ChfInfo[] chfInfo_List_Anhui = new ChfInfo[SUPI_RANGE_MAX];
        private static final SearchResult[] result_List_Anhui_IPv6IPv4Fqdn_P1 = new SearchResult[SUPI_RANGE_MAX];
        private static final Query[] queries = new Query[SUPI_RANGE_MAX];

        private static final List<String> AnhuiPrio_1_Ipv6Addresses = Arrays.asList(new String[] { "fdf8:f53b:82e4::53", "fdf8:f53b:82e4::54" });
        private static final List<String> AnhuiPrio_1_Ipv4Addresses = Arrays.asList(new String[] { "192.168.1.1", "192.168.1.2" });
        private static final String AnhuiPrio_1_Fqdn = "Anhui-1";

        private static final List<String> ShaanxiPrio_1_Ipv6Addresses = Arrays.asList(new String[] { "fdf8:f53b:82e4::63", "fdf8:f53b:82e4::64" });
        private static final List<String> ShaanxiPrio_1_Ipv4Addresses = Arrays.asList(new String[] { "192.168.2.1", "192.168.2.2" });
        private static final String ShaanxiPrio_1_Fqdn = "Shaanxi-1";
        private static final List<String> ShaanxiPrio_2_Ipv6Addresses = Arrays.asList(new String[] { "fdf8:f53b:82e4::65", "fdf8:f53b:82e4::66" });;
        private static final String ShaanxiPrio_2_Fqdn = "Shaanxi-2";

        private static Cache db = new Cache(500000, 2000, 20);

        static
        {
            db.start().subscribe();
        }

        @SuppressWarnings("unused")
        public static void main(String[] args)
        {
            performTest();

            try
            {
                Thread.sleep(10000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        public static void performTest()
        {
            TestSimilarToTc60InCi.test_0_SetupSearchResults();
            TestSimilarToTc60InCi.test_1_FillDbWithSearchResults();
            TestSimilarToTc60InCi.test_2_BuildQueries();
            TestSimilarToTc60InCi.test_3_GetSearchResultsFromDb();
        }

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
                db.put(new Item(NRF_GROUP, "requester-nf-type,supi,target-nf-type", result_List_Anhui_IPv6IPv4Fqdn_P1[i]));

            log.info("Used {} for filling DB with {} SearchResults: {}", Duration.between(start, Instant.now()), SUPI_RANGE_MAX, db.getStatistics());
        }

        private static void test_2_BuildQueries()
        {
            final Instant start = Instant.now();

            String subscriberId;

            for (int i = 0; i < SUPI_RANGE_MAX; i++)
            {
                subscriberId = "supi=imsi-" + (SUPI_START_PRE + i) + SUPI_START_SUFF_MAX;

//                if (i % 1000 == 0)
//                    log.info("subscriberId : {}", subscriberId);

                final String[] tokens = subscriberId.split("=");
                queries[i] = new Query.Builder().add("requester-nf-type", NFType.SMF).add("target-nf-type", NFType.CHF).add(tokens[0], tokens[1]).build();
            }

            log.info("Used {} for building {} Queries.", Duration.between(start, Instant.now()), SUPI_RANGE_MAX);
        }

        private static void test_3_GetSearchResultsFromDb()
        {
            final Instant start = Instant.now();

            String subscriberId;

            for (int i = 0; i < SUPI_RANGE_MAX; i++)
            {
                subscriberId = "imsi-" + (SUPI_START_PRE + i) + SUPI_START_SUFF_MAX;
                final Item item = db.get(NRF_GROUP, queries[i]);

                if (item == null)
                    log.info("{} --> not found", subscriberId);
            }

            log.info("Used {} for getting {} SearchResults from DB: {}", Duration.between(start, Instant.now()), SUPI_RANGE_MAX, db.getStatistics());
        }
    }

    private static class TestSlfCommunication
    {
        public static void loop(final long iterations)
        {
//            final TestSlfCommunication c = new TestSlfCommunication();
//            c.test();
            final AtomicLong numbers = new AtomicLong(0);

            Flowable.generate(emitter ->
            {
                final long number = numbers.getAndIncrement();

                if (number < iterations)
                {
                    emitter.onNext(number);
                }
                else
                {
                    emitter.onComplete();
                }
            }).doOnNext(number ->
            {
                log.info("i={}", number);
                Thread.sleep(1000);
            }).blockingSubscribe();

//            .blockingSubscribe(i ->
//            {
//                log.info("i={}", i);
////                Thread.sleep(1000);
//            }, e -> log.error("Error", e), () -> log.info("Complete"));
        }

        public static void main(String[] args)
        {
            loop(12);
        }

        final WebClientOptions options = new WebClientOptions().setProtocolVersion(HttpVersion.HTTP_2)
                                                               .setHttp2ClearTextUpgrade(false)
                                                               .setEnabledSecureTransportProtocols(Set.of("TLSv1.2", "TLSv1.3"));
        final WebClient client = WebClient.create(VertxInstance.get(), options);

        final AtomicInteger cnt = new AtomicInteger(0);

        final AtomicLong start = new AtomicLong(0);

        public void test()
        {
            final int MAX = 1000;

            for (int i = 0; i < MAX; i++)
            {

//                final Url url = new Url("127.0.0.1", 55081, "/nslf-disc/v0/addresses?requester-nf-type=SMF&target-nf-type=CHF&supi=imsi-12345" + i);
                final Url url = new Url("10.63.139.31", 32072, "/nnrf-disc/v1/nf-instances?requester-nf-type=SMF&target-nf-type=CHF&supi=imsi-12345" + i);

                this.nfInstanceSearch(url).subscribe();
            }

            try
            {
                Thread.sleep(10000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        private Completable nfInstanceSearch(final Url url)
        {
            return Completable.defer(() -> this.client.requestAbs(HttpMethod.GET, url.getAddr(), url.getUrl().toString()).rxSend().subscribeOn(Schedulers.io())
//                                                      .timeout(5, TimeUnit.SECONDS)
                                                      .filter(r -> (cnt.incrementAndGet() % 1000) == 0)
//                                                      .doOnSuccess(resp -> log.info("cnt={}, url={}, resp={}", cnt.get(), url, resp.bodyAsString()))
                                                      .doOnSuccess(r ->
                                                      {
                                                          final long now = System.currentTimeMillis();
                                                          final double delta = (now - this.start.get()) / 1000d;
                                                          final long cnt = this.cnt.get();
                                                          log.info("cnt={}, duration={} s, rate={} Hz", cnt, delta, Math.round(cnt / delta));
                                                      })
                                                      .doOnError(err -> log.error("Error sending HTTP request to {}: {}", url, err))
                                                      .doOnSubscribe(d -> this.start.set(System.currentTimeMillis()))
                                                      .ignoreElement());
        }
    }

    private static final String NRF_GROUP = "nrfGroup";

    private static final Logger log = LoggerFactory.getLogger(SlfWorkerCacheTest.class);

    @Test
    void test_0_General() throws IOException, InterruptedException
    {
        final int iterations = 166666;

        final List<Query> queries = new ArrayList<>();

        for (int i = 0; i < iterations; ++i)
        {
            queries.add(new Query.Builder().add("target-nf-type", "CHF").add("supi", String.format("imsi-46003010%06d0", i)).build());
            queries.add(new Query.Builder().add("target-nf-type", "CHF").add("supi", String.format("imsi-46003040%06d0", i)).build());
            queries.add(new Query.Builder().add("target-nf-type", "CHF").add("supi", String.format("imsi-46003070%06d0", i)).build());
        }

        final List<Query> queriesInRange = new ArrayList<>();

        for (int i = 0; i < iterations; ++i)
        {
            queriesInRange.add(new Query.Builder().add("target-nf-type", "CHF").add("supi", String.format("imsi-46003010%06d5", i)).build());
            queriesInRange.add(new Query.Builder().add("target-nf-type", "CHF").add("supi", String.format("imsi-46003040%06d5", i)).build());
            queriesInRange.add(new Query.Builder().add("target-nf-type", "CHF").add("supi", String.format("imsi-46003070%06d5", i)).build());
        }

        final List<Query> queriesNotInRange = new ArrayList<>();

        for (int i = 0; i < iterations; ++i)
        {
            queriesNotInRange.add(new Query.Builder().add("target-nf-type", "CHF").add("supi", String.format("imsi-46003010%06d9", i)).build());
            queriesNotInRange.add(new Query.Builder().add("target-nf-type", "CHF").add("supi", String.format("imsi-46003040%06d9", i)).build());
            queriesNotInRange.add(new Query.Builder().add("target-nf-type", "CHF").add("supi", String.format("imsi-46003070%06d9", i)).build());
        }

        Cache c = new Cache(500000, 2000, 20);
        c.start().subscribe();

        for (int a = 0; a < 1; ++a)
        {
            final List<NFProfile> pa = new ArrayList<>();
            final List<NFProfile> pb = new ArrayList<>();
            final List<NFProfile> pc = new ArrayList<>();

            for (int i = 0; i < iterations; i++)
            {
                pa.add(new NFProfile().nfInstanceId(UUID.fromString(String.format("635d8add-dac5-43b6-80fa-a0000%06d0", i)))
                                      .nfType(NFType.CHF)
                                      .chfInfo(new ChfInfo().addSupiRangeListItem(new SupiRange().start(String.format("46003010%06d0", i))
                                                                                                 .end(String.format("46003010%06d8", i)))));
                pb.add(new NFProfile().nfInstanceId(UUID.fromString(String.format("635d8add-dac5-43b6-80fa-b0000%06d0", i)))
                                      .nfType(NFType.CHF)
                                      .chfInfo(new ChfInfo().addSupiRangeListItem(new SupiRange().start(String.format("46003040%06d0", i))
                                                                                                 .end(String.format("46003040%06d8", i)))));
                pc.add(new NFProfile().nfInstanceId(UUID.fromString(String.format("635d8add-dac5-43b6-80fa-c0000%06d0", i)))
                                      .nfType(NFType.CHF)
                                      .chfInfo(new ChfInfo().addSupiRangeListItem(new SupiRange().start(String.format("46003070%06d0", i))
                                                                                                 .end(String.format("46003070%06d8", i)))));
            }

            Instant start;

            start = Instant.now();

            log.info("start={}", start);

            for (int i = 0; i < 3 * iterations; i += 3)
            {
                c.put(NRF_GROUP,
                      queries.get(i + 0),
                      new com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.custom.SearchResult().validityPeriod(10)
                                                                                                      .nfInstances(Arrays.asList(pa.get(i / 3))));

                c.put(NRF_GROUP,
                      queries.get(i + 2),
                      new com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.custom.SearchResult().validityPeriod(10)
                                                                                                      .nfInstances(Arrays.asList(pc.get(i / 3))));

                c.put(NRF_GROUP,
                      queries.get(i + 1),
                      new com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.custom.SearchResult().validityPeriod(10)
                                                                                                      .nfInstances(Arrays.asList(pb.get(i / 3))));

                if (((i + 1) % 100000) == 0)
                {
                    log.info("Number of SearchResults put so far: {}, duration: {}", (i + 1), Duration.between(start, Instant.now()));
                    log.info("cache={}", c);
                }

                // Thread.sleep(1);
            }

            log.info("Putting {} SearchResults took {}", iterations, Duration.between(start, Instant.now()));

            log.info("heapMemoryUsage={}", java.lang.management.ManagementFactory.getMemoryMXBean().getHeapMemoryUsage());
            log.info("nonHeapMemoryUsage={}", java.lang.management.ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage());

            log.info("cache={}", c);

            for (int j = 0; j < 2; ++j)
            {
                start = Instant.now();

                for (int i = 0; i < 3 * iterations; i += 3)
                {
                    final Item resultA = c.get(NRF_GROUP, queries.get(i + 0));
                    // log.info("resultA={}", resultA);
                    final Item resultB = c.get(NRF_GROUP, queries.get(i + 1));
                    // log.info("resultB={}", resultB);
                    final Item resultC = c.get(NRF_GROUP, queries.get(i + 2));
                    // log.info("resultC={}", resultC);
                }

                log.info("Getting {} times SearchResults (same query) in cache took {}", iterations, Duration.between(start, Instant.now()));

                start = Instant.now();

                for (int i = 0; i < 3 * iterations; i += 3)
                {
                    final Item resultA = c.get(NRF_GROUP, queriesInRange.get(i + 0));
                    // log.info("resultA={}", resultA);
                    final Item resultB = c.get(NRF_GROUP, queriesInRange.get(i + 1));
                    // log.info("resultB={}", resultB);
                    final Item resultC = c.get(NRF_GROUP, queriesInRange.get(i + 2));
                    // log.info("resultC={}", resultC);
                }

                log.info("Getting {} times SearchResults (in range) in cache took {}", iterations, Duration.between(start, Instant.now()));

                start = Instant.now();

                for (int i = 0; i < 3 * iterations; i += 3)
                {
                    final Item resultA = c.get(NRF_GROUP, queriesNotInRange.get(i + 0));
                    // if (resultA != null)
                    // log.info("resultA={}", resultA);
                    final Item resultB = c.get(NRF_GROUP, queriesNotInRange.get(i + 1));
                    // if (resultB != null)
                    // log.info("resultB={}", resultB);
                    final Item resultC = c.get(NRF_GROUP, queriesNotInRange.get(i + 2));
                    // if (resultC != null)
                    // log.info("resultC={}", resultC);
                }

                log.info("Getting {} times SearchResults not in cache took {}", iterations, Duration.between(start, Instant.now()));
            }

            // Thread.sleep(12000);

            log.info("cache={}", c);
        }

        // c.stop().blockingAwait();

        queries.clear();
        queriesInRange.clear();
        queriesNotInRange.clear();

        log.info("Waiting 2 s -------");
        Thread.sleep(2000);

        log.info("heapMemoryUsage={}", java.lang.management.ManagementFactory.getMemoryMXBean().getHeapMemoryUsage());
        log.info("nonHeapMemoryUsage={}", java.lang.management.ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage());
        System.gc();
        log.info("heapMemoryUsage={}", java.lang.management.ManagementFactory.getMemoryMXBean().getHeapMemoryUsage());
        log.info("nonHeapMemoryUsage={}", java.lang.management.ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage());

        log.info("cache={}", c);

        // c.stop().blockingAwait();
        // c.clear();
        log.info("Waiting 10 s -------");
        Thread.sleep(10000);

        log.info("heapMemoryUsage={}", java.lang.management.ManagementFactory.getMemoryMXBean().getHeapMemoryUsage());
        log.info("nonHeapMemoryUsage={}", java.lang.management.ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage());
        System.gc();
        log.info("heapMemoryUsage={}", java.lang.management.ManagementFactory.getMemoryMXBean().getHeapMemoryUsage());
        log.info("nonHeapMemoryUsage={}", java.lang.management.ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage());

        log.info("cache={}", c);
    }

    @Test
    void testSimilarToTc60InCi()
    {
        TestSimilarToTc60InCi.performTest();
    }
//
//        @SuppressWarnings("unused")
//        public static void main1(String[] args) throws InterruptedException
//        {
//            final Cache c = new Cache(500000, 2000, 20);
//            c.start().subscribe();
//
//            while (true)
//            {
//                putProfilesWithMultipleSupiRanges(c);
//
//                log.info("cache={}", c);
//
//                Thread.sleep(6000);
//
//                log.info("cache={}", c);
//            }
//        }
//
//        private static void putProfilesWithMultipleSupiRanges(final Cache c)
//        {
//            // 1000000000 SUPIs
//            // 500000 ranges
//            // -> 2000 SUPIs / range
//            // 33 regions
//            // 2 SCP / region (but active/standby)
//            // -> 15000 ranges / SCP
//            // Every SCP is stand-in for its buddy region
//            // -> 7500 ranges of own region + 7500 of buddy region -> 15000
//
//            // Region11 = "start": "46003 01 0000 0000", "end": "46003 01 0000 1999"
//            // ... (n=7500)
//            // Region1n = "start": "46003 01 7499 0000", "end": "46003 01 7499 1999"
//            //
//            // Region21 = "start": "46003 02 0000 0000", "end": "46003 02 0000 1999"
//            // ... (n=7500)
//            // Region2n = "start": "46003 02 7499 0000", "end": "46003 02 7499 1999"
//            // ... (m=33)
//            // Regionm1 = "start": "46003 33 0000 0000", "end": "46003 33 0000 1999"
//            // ... (n=7500)
//            // Regionmn = "start": "46003 33 7499 0000", "end": "46003 33 7499 1999"
//
//            final int numRegions = 3300; // 33;
//            final int numRangesPerRegion = 75; // 7500; // Every SCP knows about itself and its buddy.
//
//            final Map<Integer, NFProfile> profiles = new HashMap<>();
//            final Map<Integer, NFProfile> buddyProfiles = new HashMap<>();
//
//            for (int i = 0; i < numRegions; ++i)
//            {
//                final ChfInfo chfInfo = new ChfInfo();
//
//                for (int j = 0; j < numRangesPerRegion; ++j)
//                {
//                    chfInfo.addSupiRangeListItem(new SupiRange().start(String.format("46003%02d%04d0000", i, j)).end(String.format("46003%02d%04d1999", i, j)));
//                }
//
//                profiles.put(i,
//                             new NFProfile().nfInstanceId(UUID.fromString(String.format("635d8add-dac5-43b6-80fa-a000000%02d", i)))
//                                            .nfType(NFType.CHF)
//                                            .chfInfo(chfInfo));
//                buddyProfiles.put(i,
//                                  new NFProfile().nfInstanceId(UUID.fromString(String.format("635d8add-dac5-43b6-80fa-a000000%02d", ((i + 1) % numRegions))))
//                                                 .nfType(NFType.CHF)
//                                                 .chfInfo(chfInfo));
//            }
//
//            Instant start;
//
//            start = Instant.now();
//
//            for (int i = 0; i < numRegions; ++i)
//            {
//                c.put(new Query.Builder().add("target-nf-type", "CHF").add("supi", String.format("imsi-46003%02d00000001", i)).build(),
//                      new com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.custom.SearchResult().validityPeriod(3600)
//                                                                                       .nfInstances(Arrays.asList(profiles.get(i), buddyProfiles.get(i))));
//            }
//
//            log.info("Putting {} SupiRanges for each of {} Regions took {} s", 2 * numRangesPerRegion, numRegions, Duration.between(start, Instant.now()));
//
//            for (int i = 0; i < numRegions; ++i)
//            {
//                start = Instant.now();
//
//                for (int j = 0; j < numRangesPerRegion; ++j)
//                {
//                    final Query query = new Query.Builder().add("target-nf-type", "CHF").add("supi", String.format("imsi-46003%02d%04d0001", i, j)).build();
//                    final com.ericsson.cnal.nrf.r17.NnrfDiscSearchResultDb.Item item = c.get(query);
//
//                    if (item == null)
//                        log.info("Item not found for query {}", query);
//                    // else
//                    // log.info("SearchResult contains {}",
//                    // item.getData().getNfInstances().stream().map(p ->
//                    // p.getNfInstanceId()).collect(Collectors.toList()));
//                }
//
//                log.info("Getting 1 SUPI from {} SupiRanges in one Region took {} s", 2 * numRangesPerRegion, Duration.between(start, Instant.now()));
//                log.info("cache={}", c);
//            }
//        }
}
