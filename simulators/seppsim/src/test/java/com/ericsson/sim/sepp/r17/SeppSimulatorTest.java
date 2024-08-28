package com.ericsson.sim.sepp.r17;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cnal.openapi.r17.ts29571.commondata.PlmnId;
import com.ericsson.cnal.openapi.r17.ts29573.n32.handshake.N32fContextInfo;
import com.ericsson.cnal.openapi.r17.ts29573.n32.handshake.N32fErrorInfo;
import com.ericsson.cnal.openapi.r17.ts29573.n32.handshake.N32fErrorType;
import com.ericsson.cnal.openapi.r17.ts29573.n32.handshake.SecNegotiateReqData;
import com.ericsson.cnal.openapi.r17.ts29573.n32.handshake.SecParamExchReqData;
import com.ericsson.sim.nrf.r17.NrfSimulator;
import com.ericsson.sim.sepp.r17.SeppSimulator.Configuration;
import com.ericsson.sim.sepp.r17.SeppSimulator.Configuration.Api;
import com.ericsson.sim.sepp.r17.SeppSimulator.Configuration.RoamingPartner;
import com.ericsson.sim.sepp.r17.SeppSimulator.Configuration.Sepp;
import com.ericsson.sim.sepp.r17.SeppSimulator.N32Handshake;
import com.ericsson.sim.sepp.r17.SeppSimulator.NfInstance;
import com.ericsson.utilities.http.openapi.OpenApiTask.DataIndex;

import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.client.HttpResponse;

class SeppSimulatorTest
{
    private static final Logger log = LoggerFactory.getLogger(SeppSimulatorTest.class);

    // @Test
    void test_0_General() throws IOException, InterruptedException
    {
        final AtomicReference<SeppSimulator> cSeppSim = new AtomicReference<>();

        final Thread cSepp = new Thread(() ->
        {
            try
            {
                final Sepp sepp1 = new Sepp().setName("sepp-1").setFqdn("sepp-1.rp-1.de").setIpv4Address("127.0.0.1").setScheme("http").setPort(8082);
                final RoamingPartner rp1 = new RoamingPartner().setName("rp-1").setSepp(Arrays.asList(sepp1));
                final Api api = new Api();
//Example of a weighted disturbance. For flexible disturbance, just remove the calls of setWeight(1d).
//              api.getNnrfNfDiscovery()
//              .setDisturbances(new Disturbance().setWeight(1d).setStatus(HttpResponseStatus.BAD_GATEWAY),
//                               new Disturbance().setWeight(1d),
//                               new Disturbance().setWeight(1d).setDelayInMillis(1000l).setStatus(HttpResponseStatus.BAD_REQUEST));
//                api.getNnrfNfDiscovery().setDisturbances(new Disturbance().setDropAndReplyWithRefuseStream());
                final Configuration cSeppConfig = new Configuration().setOwnDomain("rp-1.gr")
                                                                     .setOwnIpAddress("127.0.0.1")
                                                                     .setOwnRole(Configuration.Role.SEPP_ONLY)
                                                                     .setRoamingPartner(Arrays.asList(rp1))
                                                                     .setApi(api);

                log.info("cSeppConfig={}", cSeppConfig);

                cSeppSim.set(SeppSimulator.Builder.of(cSeppConfig.getOwnIpAddress(), 8081)
                                                  .withApiR17N32Handshake()
                                                  .withApiR17NnrfNfDiscovery()
                                                  .withApiR17Npcf()
                                                  .build()
                                                  .setConfiguration(cSeppConfig));
                cSeppSim.get().run();
            }
            catch (Exception e)
            {
                log.error("Error", e);
            }
        });

        final AtomicReference<SeppSimulator> pSeppSim = new AtomicReference<>();

        final Thread pSepp = new Thread(() ->
        {
            try
            {
                final Sepp sepp1 = new Sepp().setName("sepp-1").setFqdn("sepp-1.rp-1.gr").setIpv4Address("127.0.0.1").setScheme("http").setPort(8081);
                final RoamingPartner rp1 = new RoamingPartner().setName("rp-1").setSepp(Arrays.asList(sepp1));
                final Configuration pSeppConfig = new Configuration().setOwnDomain("rp-1.de")
                                                                     .setOwnIpAddress("127.0.0.1")
                                                                     .setOwnRole(Configuration.Role.SEPP_OR_NF)
                                                                     .setRoamingPartner(Arrays.asList(rp1))
                                                                     .setSupportedPlmn(List.of(List.of(// Vodafone Germany
                                                                                                       new PlmnId().mcc("262").mnc("02"),
                                                                                                       new PlmnId().mcc("262").mnc("04"),
                                                                                                       new PlmnId().mcc("262").mnc("09"),
                                                                                                       new PlmnId().mcc("262").mnc("42"))));

                log.info("pSeppConfig={}", pSeppConfig);

                pSeppSim.set(SeppSimulator.Builder.of(pSeppConfig.getOwnIpAddress(), 8082)
                                                  .withApiR17N32Handshake()
                                                  .withApiR17NnrfNfDiscovery()
                                                  .withApiR17Npcf()
                                                  .build()
                                                  .setConfiguration(pSeppConfig));
                pSeppSim.get().run();
            }
            catch (Exception e)
            {
                log.error("Error", e);
            }
        });

        final AtomicReference<SeppSimulator> nfSim = new AtomicReference<>();

        final Thread nf = new Thread(() ->
        {
            try
            {
                final Configuration nfConfig = new Configuration().setOwnDomain("rp-1.de").setOwnIpAddress("127.0.0.1").setOwnRole(Configuration.Role.NF_ONLY);

                log.info("nfConfig={}", nfConfig);

                nfSim.set(SeppSimulator.Builder.of(nfConfig.getOwnIpAddress(), 8083)
                                               .withApiR17N32Handshake()
                                               .withApiR17NnrfNfDiscovery()
                                               .withApiR17Npcf()
                                               .build()
                                               .setConfiguration(nfConfig));
                nfSim.get().run();
            }
            catch (Exception e)
            {
                log.error("Error", e);
            }
        });

        final Thread nrfSim = new Thread(() ->
        {
            try
            {
                NrfSimulator.Builder.of("127.0.0.1", 8084).build().run();
            }
            catch (Exception e)
            {
                log.error("Error", e);
            }
        });

        nrfSim.start();
        nf.start();
        cSepp.start();
        pSepp.start();

        Thread.sleep(20000);

        N32Handshake client = SeppSimulator.N32Handshake.createClient(cSeppSim.get());
        HttpResponse<Buffer> response;

        response = client.postExchangeCapabilityRequest(pSeppSim.get().getHostAndPort(),
                                                        null,
                                                        new SecNegotiateReqData().sender("") // Let the field be set automatically
                                                                                 .addSupportedSecCapabilityListItem("TLS")
                                                                                 ._3gppSbiTargetApiRootSupported(true)
                                                                                 .addPlmnIdListItem(new PlmnId().mcc("289").mnc("68"))
                                                                                 .targetPlmnId(new PlmnId().mcc("262").mnc("42")))
                         .blockingGet();

        log.info("postExchangeCapabilityResponse={}", response.bodyAsString());

        response = client.postExchangeParamsRequest(pSeppSim.get().getHostAndPort(),
                                                    null,
                                                    new SecParamExchReqData().sender("sepp-1.rp-1.gr") // Take this value
                                                                             .n32fContextId("0123456789abcDEF"))
                         .blockingGet();

        log.info("postExchangeParamsResponse={}", response.bodyAsString());

        response = client.postN32fErrorRequest(pSeppSim.get().getHostAndPort(),
                                               null,
                                               new N32fErrorInfo().n32fMessageId("message-1")//
                                                                  .n32fErrorType(N32fErrorType.CONTEXT_NOT_FOUND))
                         .blockingGet();

        log.info("postN32fErrorResponse={}", response.bodyAsString());

        response = client.postN32fErrorRequest(pSeppSim.get().getHostAndPort(),
                                               null,
                                               new N32fErrorInfo().n32fMessageId("message-1")//
                                                                  .n32fErrorType(N32fErrorType.INTEGRITY_CHECK_ON_MODIFICATIONS_FAILED))
                         .blockingGet();

        log.info("postN32fErrorResponse={}", response.bodyAsString());

        response = client.postN32fTerminateRequest(pSeppSim.get().getHostAndPort(), null, new N32fContextInfo().n32fContextId("0123456789abcABC"))
                         .blockingGet();

        log.info("postN32fTerminateResponse={}", response.bodyAsString());

        for (int i = 0; i < 10000; ++i)
        {
            {
                final Iterator<Entry<String, NfInstance>> iterator = cSeppSim.get().getNfInstances().iterator();

                while (cSeppSim != null && iterator.hasNext())
                {
                    iterator.next().getValue().getRequestBody().forEach(body -> log.info("cSeppSim: requestBody={}", body));
//                            .getContexts()
//                            .forEach(ctx -> log.info("cSeppSim: requestBody={}", (Object) ctx.get(DataIndex.REQUEST_BODY.name())));
                }
            }

            {
                final Iterator<Entry<String, NfInstance>> iterator = pSeppSim.get().getNfInstances().iterator();

                while (pSeppSim != null && iterator.hasNext())
                {
                    iterator.next()
                            .getValue()
                            .getContexts()
                            .forEach(ctx -> log.info("pSeppSim: requestBody={}", (Object) ctx.get(DataIndex.REQUEST_BODY.name())));
                }
            }

            {
                final Iterator<Entry<String, NfInstance>> iterator = nfSim.get().getNfInstances().iterator();

                while (nfSim != null && iterator.hasNext())
                {
                    iterator.next().getValue().getContexts().forEach(ctx -> log.info("nfSim: requestBody={}", (Object) ctx.get(DataIndex.REQUEST_BODY.name())));
                }
            }

            Thread.sleep(5000);
        }

        cSepp.join();
        pSepp.join();
        nf.join();
        nrfSim.join();
    }

    // @Test
    void test_1_LoadApis() throws IOException, InterruptedException
    {
        try
        {
            final Sepp sepp1 = new Sepp().setName("sepp-1").setFqdn("sepp-1.rp-1.de").setIpv4Address("127.0.0.1").setScheme("http").setPort(8082);
            final RoamingPartner rp1 = new RoamingPartner().setName("rp-1").setSepp(Arrays.asList(sepp1));
            final Api api = new Api();
            final Configuration cSeppConfig = new Configuration().setOwnDomain("rp-1.gr")
                                                                 .setOwnIpAddress("127.0.0.1")
                                                                 .setOwnRole(Configuration.Role.SEPP_ONLY)
                                                                 .setRoamingPartner(Arrays.asList(rp1))
                                                                 .setApi(api);

            SeppSimulator.Builder.of(cSeppConfig.getOwnIpAddress(), 8081).build().setConfiguration(cSeppConfig);
        }
        catch (Exception e)
        {
            log.error("Error", e);
        }
    }
}
