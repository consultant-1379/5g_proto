package com.ericsson.sim.nrf.r17;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cnal.nrf.r17.NrfAdapter;
import com.ericsson.cnal.nrf.r17.NrfAdapter.RequestContext;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.accesstoken.AccessTokenReq;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.accesstoken.AccessTokenReq.GrantTypeEnum;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.accesstoken.AccessTokenRsp;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.bootstrapping.BootstrappingInfo;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.ScpDomainRoutingInfoSubscription;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.ScpDomainRoutingInformation;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.StoredSearchResult;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.custom.SearchResult;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.ChfInfo;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.Links;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFProfile;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFStatus;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFType;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.SupiRange;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.PlmnId;
import com.ericsson.sim.nrf.r17.NrfSimulator.Configuration;
import com.ericsson.utilities.common.Rdn;
import com.ericsson.utilities.http.Url;
import com.ericsson.utilities.json.Json;

class NrfSimulatorTest
{
    private static final Logger log = LoggerFactory.getLogger(NrfSimulatorTest.class);
    private static final int REGISTRY_TIMEOUT_SECS = 60;
//    private static final int VALIDITY_PERIOD_SECS = 2 * 60 * 60; // 2 hours
    private static final int VALIDITY_PERIOD_SECS = 10;

    // @Test
    void test_0_General() throws IOException, InterruptedException
    {
        final NrfSimulator server = NrfSimulator.Builder.of("127.0.0.1", 8081).build();
        server.getConfiguration().getNnrfNfDiscovery().setValidityPeriodInSecs(VALIDITY_PERIOD_SECS);
        server.getConfiguration().getNnrfNfManagement().setHeartBeatTimerInSecs(REGISTRY_TIMEOUT_SECS);
        server.getConfiguration()
              .getNnrfNfManagement()
              .setProvisioning(List.of(new Configuration.NnrfNfManagement.Provisioning().setPatches(List.of(new Configuration.Modifier().setReplicator(6)
                                                                                                                                        .setPatches(List.of(Json.Patch.of()
                                                                                                                                                                      .op(Json.Patch.Operation.REPLACE)
                                                                                                                                                                      .path("/nfInstanceId")
                                                                                                                                                                      .value("2ec8ac0b-265e-4165-86e9-e073a1%06d"),
                                                                                                                                                            Json.Patch.of()
                                                                                                                                                                      .op(Json.Patch.Operation.REPLACE)
                                                                                                                                                                      .path("/chfInfo/supiRangeList/0/start")
                                                                                                                                                                      .value("460030101%06d"),
                                                                                                                                                            Json.Patch.of()
                                                                                                                                                                      .op(Json.Patch.Operation.REPLACE)
                                                                                                                                                                      .path("/chfInfo/supiRangeList/0/end")
                                                                                                                                                                      .value("460030101%06d")))))
                                                                                        .setNfProfile(new NFProfile().nfInstanceId(UUID.fromString("2ec8ac0b-265e-4165-86e9-e073a1000000"))
                                                                                                                     .nfType(NFType.CHF)
                                                                                                                     .nfStatus(NFStatus.REGISTERED)
                                                                                                                     .fqdn("RegionA")
                                                                                                                     .priority(1)
                                                                                                                     .nfServicePersistence(false)
                                                                                                                     .addNfSetIdListItem("set-1")
                                                                                                                     .addNfSetIdListItem("set-2")
                                                                                                                     .chfInfo(new ChfInfo().addSupiRangeListItem(new SupiRange().start("460030101000000")
                                                                                                                                                                                .end("460030101000000")))),
                                       new Configuration.NnrfNfManagement.Provisioning().setPatches(List.of(new Configuration.Modifier().setReplicator(1)
                                                                                                                                        .setPatches(List.of(Json.Patch.of()
                                                                                                                                                                      .op(Json.Patch.Operation.REPLACE)
                                                                                                                                                                      .path("/nfInstanceId")
                                                                                                                                                                      .value("2ec8ac0b-265e-4165-86e9-e073b1%06d")))))
                                                                                        .setNfProfile(new NFProfile().nfInstanceId(UUID.fromString("2ec8ac0b-265e-4165-86e9-e073b1000000"))
                                                                                                                     .nfType(NFType.PCF)
                                                                                                                     .nfStatus(NFStatus.REGISTERED)
                                                                                                                     .fqdn("RegionB")
                                                                                                                     .priority(1)
                                                                                                                     .nfServicePersistence(false)
                                                                                                                     .addNfSetIdListItem("set-1")
                                                                                                                     .addNfSetIdListItem("set-3")),
                                       new Configuration.NnrfNfManagement.Provisioning().setPatches(List.of(new Configuration.Modifier().setReplicator(1)
                                                                                                                                        .setPatches(List.of(Json.Patch.of()
                                                                                                                                                                      .op(Json.Patch.Operation.REPLACE)
                                                                                                                                                                      .path("/nfInstanceId")
                                                                                                                                                                      .value("2ec8ac0b-265e-4165-86e9-e073c1%06d")
//                                                                                                                                                            Json.Patch.of()
//                                                                                                                                                                      .op(Json.Patch.Operation.REPLACE)
//                                                                                                                                                                      .path("/chfInfo/supiRangeList/0/pattern")
//                                                                                                                                                                      .value("46003070[11%06d")
                                                                                                                                        ))))
                                                                                        .setNfProfile(new NFProfile().nfInstanceId(UUID.fromString("2ec8ac0b-265e-4165-86e9-e073c1000000"))
                                                                                                                     .nfType(NFType.CHF)
                                                                                                                     .nfStatus(NFStatus.REGISTERED)
                                                                                                                     .fqdn("RegionC")
                                                                                                                     .priority(1)
                                                                                                                     .nfServicePersistence(false)
                                                                                                                     .addNfSetIdListItem("set-2")
                                                                                                                     .addNfSetIdListItem("set-3")
                                                                                                                     .chfInfo(new ChfInfo().addSupiRangeListItem(new SupiRange().pattern("imsi-46003070[1]000000"))))));

        log.info("config={}", server.getConfiguration());

        // Inform everyone interested about the changes:
        server.setConfiguration(server.getConfiguration());
        server.setConfiguration(server.getConfiguration());

        new Thread(server::run).start();

        Thread.sleep(5000);
//        log.info("3 nfProfiles={}", server.nfProfileDb.toString());

        log.info("Started simulator.");
        log.info("nrfs={}", server.nfInstancesGet(null, null));

        final NrfAdapter nrf = new NrfAdapter(new Rdn("nf", "sepp-function").add("nf-instance", "instance_0").add("nrf-group", "group_0").add("nrf", "nrf_0"),
                                              new Url("127.0.0.1", 8081, ""));
        {
            NrfAdapter.Result<BootstrappingInfo> result = nrf.bootstrappingGet(1000).blockingGet();
            log.info("bootstrappingInfo={}", result.getBodyAsString());
        }
        {
            NrfAdapter.Result<Links> result = nrf.nfInstancesGet(RequestContext.of(1000), NFType.CHF, null).blockingGet();
            log.info("links={}", result.getBodyAsString());
        }
        {
            NrfAdapter.Result<SearchResult> result = nrf.nfInstancesSearch(RequestContext.of(5000,
                                                                                             new NrfAdapter.Query.Builder().add("requester-nf-type", "SMF")
                                                                                                                           .add("target-nf-type", "CHF")
                                                                                                                           .add("target-nf-set-id", "set-3")
                                                                                                                           .build()
                                                                                                                           .toString()))
                                                        .blockingGet();
            log.info("result={}", result.getBodyAsString());

            NrfAdapter.Result<StoredSearchResult> storedResult = nrf.nfInstancesRetrieveStored(5000, result.getBody().getSearchId(), false).blockingGet();
            log.info("storedResult={}", storedResult.getBodyAsString());
        }
        Thread.sleep(1100);
        {
            NrfAdapter.Result<SearchResult> result = nrf.nfInstancesSearch(RequestContext.of(5000,
                                                                                             new NrfAdapter.Query.Builder().add("requester-nf-type", "SMF")
                                                                                                                           .add("target-nf-type", "CHF")
                                                                                                                           .add("supi", "imsi-460030101000005")
                                                                                                                           .build()
                                                                                                                           .toString()))
                                                        .blockingGet();
            log.info("result={}", result.getBodyAsString());

            NrfAdapter.Result<StoredSearchResult> storedResult = nrf.nfInstancesRetrieveStored(5000, result.getBody().getSearchId(), true).blockingGet();
            log.info("storedResult={}", storedResult.getBodyAsString());
        }
        Thread.sleep(1100);
        {
            NrfAdapter.Result<Links> result = nrf.nfInstancesGet(RequestContext.of(1000), NFType.PCF, null).blockingGet();
            log.info("pcfs: result={}", result.getBodyAsString());
            nrf.nfInstanceDeregister(RequestContext.of(1000), UUID.fromString(result.getBody().getItem().get(0).getHref())).blockingGet();
            result = nrf.nfInstancesGet(RequestContext.of(1000), NFType.PCF, null).blockingGet();
            log.info("pcfs: result={}", result.getBodyAsString());
        }
        {
            NrfAdapter.Result<SearchResult> result = nrf.nfInstancesSearch(RequestContext.of(5000,
                                                                                             new NrfAdapter.Query.Builder().add("requester-nf-type", "SMF")
                                                                                                                           .add("target-nf-type", "PCF")
                                                                                                                           .add("supi", "imsi-460030101000005")
                                                                                                                           .add("target-nf-set-id", "set-3")
                                                                                                                           .build()
                                                                                                                           .toString()))
                                                        .blockingGet();
            log.info("-----result={}", result.getBodyAsString());

            NrfAdapter.Result<StoredSearchResult> storedResult = nrf.nfInstancesRetrieveStored(5000, result.getBody().getSearchId(), true).blockingGet();
            log.info("storedResult={}", storedResult.getBodyAsString());
        }
        Thread.sleep(1100);
        {
            NrfAdapter.Result<SearchResult> result = nrf.nfInstancesSearch(RequestContext.of(5000,
                                                                                             new NrfAdapter.Query.Builder().add("requester-nf-type", "SMF")
                                                                                                                           .add("target-nf-type", "CHF")
                                                                                                                           .add("supi", "imsi-460030101000005")
                                                                                                                           .add("target-nf-set-id", "set-3")
                                                                                                                           .build()
                                                                                                                           .toString()))
                                                        .blockingGet();
            log.info("result={}", result.getBodyAsString());

            NrfAdapter.Result<StoredSearchResult> storedResult = nrf.nfInstancesRetrieveStored(5000, result.getBody().getSearchId(), true).blockingGet();
            log.info("storedResult={}", storedResult.getBodyAsString());
        }
        {
            NrfAdapter.Result<SearchResult> result = nrf.nfInstancesSearch(RequestContext.of(5000,
                                                                                             new NrfAdapter.Query.Builder().add("requester-nf-type", "SMF")
                                                                                                                           .add("target-nf-type", "CHF")
                                                                                                                           .add("supi", "imsi-460030701000000")
                                                                                                                           .build()
                                                                                                                           .toString()))
                                                        .blockingGet();
            log.info("result={}", result.getBodyAsString());

            NrfAdapter.Result<StoredSearchResult> storedResult = nrf.nfInstancesRetrieveStored(5000, result.getBody().getSearchId(), false).blockingGet();
            log.info("storedResult={}", storedResult.getBodyAsString());

            NrfAdapter.Result<StoredSearchResult> storedResultForInvalidSearchId = nrf.nfInstancesRetrieveStored(5000, "invalidSearchId", false).blockingGet();
            log.info("storedResultForInvalidSearchId={}", storedResultForInvalidSearchId.getBodyAsString());
        }
        {
            NrfAdapter.Result<AccessTokenRsp> result = nrf.accessTokenRequest(5000,
                                                                              new AccessTokenReq().grantType(GrantTypeEnum.CLIENT_CREDENTIALS)
                                                                                                  .nfInstanceId(UUID.randomUUID())
                                                                                                  .scope("aa")
                                                                                                  .requesterPlmnList(List.of(new PlmnId().mcc("049").mnc("123"),
                                                                                                                             new PlmnId().mcc("049")
                                                                                                                                         .mnc("124")))
                                                                                                  .targetNsiList(List.of("nsi1", "nsi2")))
                                                          .blockingGet();
            log.info("accessTokenResponse={}", result.getBodyAsString());
        }
        {
            NrfAdapter.Result<ScpDomainRoutingInformation> result = nrf.scpDomainRoutingInfoGet(5000,
                                                                                                new NrfAdapter.Query.Builder().add("loacl", "true").build())
                                                                       .blockingGet();
            log.info("scpDomainRoutingInformation={}", result.getBodyAsString());

            NrfAdapter.Result<ScpDomainRoutingInfoSubscription> result2 = nrf.scpDomainRoutingInfoSubscriptionCreate(REGISTRY_TIMEOUT_SECS,
                                                                                                                     new ScpDomainRoutingInfoSubscription().callbackUri("callbackUri")
                                                                                                                                                           .localInd(true))
                                                                             .blockingGet();
            log.info("scpDomainRoutingInfoSubscription={}", result2.getBodyAsString());

            NrfAdapter.Result<ScpDomainRoutingInfoSubscription> result3 = nrf.scpDomainRoutingInfoSubscriptionRemove(REGISTRY_TIMEOUT_SECS,
                                                                                                                     new URL(result2.getHeader("location")))
                                                                             .blockingGet();

        }

        Thread.sleep(300000);
    }
}
