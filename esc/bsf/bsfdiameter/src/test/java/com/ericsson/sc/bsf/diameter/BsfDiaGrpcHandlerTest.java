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
 * Created on: Apr 12, 2021
 *     Author: echfari
 */
package com.ericsson.sc.bsf.diameter;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.net.Inet4Address;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFStatus;
import com.ericsson.esc.bsf.db.MockNbsfManagementService.State;
import com.ericsson.esc.bsf.openapi.model.PcfBinding;
import com.ericsson.esc.bsf.worker.BindingCleanupManager;
import com.ericsson.gs.tm.diameter.service.grpc.DeliveryResult;
import com.ericsson.gs.tm.diameter.service.grpc.DiameterMessage;
import com.ericsson.gs.tm.diameter.service.grpc.DiameterMessageHeader;
import com.ericsson.gs.tm.diameter.service.grpc.IncomingRequest;
import com.ericsson.gs.tm.diameter.service.grpc.IncomingRequestResult;
import com.ericsson.gs.tm.diameter.service.grpc.OutgoingRequest;
import com.ericsson.gs.tm.diameter.service.grpc.OutgoingRequestResult;
import com.ericsson.sc.bsf.etcd.PcfDbView;
import com.ericsson.sc.bsf.etcd.PcfNf;
import com.ericsson.sc.bsf.etcd.PcfNfRecord;
import com.ericsson.sc.diameter.DiaGrpcClientContext;
import com.ericsson.sc.diameter.PeerTable;
import com.ericsson.sc.diameter.avp.Avps;
import com.ericsson.sc.diameter.avp.CommandCode;
import com.ericsson.sc.diameter.avp.MessageParser;
import com.ericsson.sc.diameter.avp.ResultCode;
import com.ericsson.utilities.cassandra.RxSession;
import com.google.protobuf.ByteString;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Test the BSF diameter GRPC handler
 */
public class BsfDiaGrpcHandlerTest
{
    private static final Logger log = LoggerFactory.getLogger(BsfDiaGrpcHandlerTest.class);
    private int serviceId = 2;
    private final String afHost = "afHost";
    private final String afRealm = "afRealm";
    private final String fallbackHost = "fallbackHost";
    private final String fallbackRealm = "fallbackRealm";
    private final String staticDestHost = "staticDestHost";
    private final String staticDestRealm = "staticDestRealm";
    private final String nfInstance = "nfInstance";
    private final MessageParser mp = Avps.messageParser();
    private final RxSession rxSession = RxSession.builder().withConfig(DriverConfigLoader.programmaticBuilder().build()).build();
    private final String keyspace = "sample_keyspace";
    private String destHost;
    private String destRealm;

    @Test
    public void aarRoutingNoFailover() throws Throwable
    {
        final int commandCode = CommandCode.AUTHORIZE_AUTHENTICATE.value(); // AAR
        // AAR is received, found in database, forwarded,answer returned back.
        final BsfMockDb mockDb = new BsfMockDb(2, 2);

        log.info("BsfMock database: {}", mockDb.getDb());
        final var cmSubject = BehaviorSubject.<Optional<BsfDiameterCfg>>create();
        final var cm = cmSubject.toFlowable(BackpressureStrategy.BUFFER);

        final var handler = new BsfDiaGrpcHandler(mockDb.getDb(),
                                                  new BindingCleanupManager(this.rxSession,
                                                                            this.keyspace,
                                                                            cm.map(conf -> conf.map(BsfDiameterCfg::getNfInstanceName).orElse("unknown"))),
                                                  cm,
                                                  Flowable.just(createPcfDbView(mockDb.getBindings(), 3)));
        cmSubject.onNext(Optional.of(BsfDiameterCfg.create(null, null, true, null, null, null, List.of()))); // Inject new CM configuration

        final var binding = mockDb.getBinding(0);
        final var mockMsg = buildMockRequest(commandCode, afHost, afRealm, null, null, binding.getIpv4Addr(), binding.getIpDomain()).build();
        log.info("Mock request: {}", mockMsg);
        final var incomingRequest = IncomingRequest.newBuilder().setServiceId(this.serviceId).setMessage(mockMsg).build();

        // Routing successful, PCF in binding is selected as destination
        {
            final var ctx = new SingleRequestContext(binding.getPcfDiamHoststr(), binding.getPcfDiamRealmstr());
            final var result = handler.processRequest(Single.just(incomingRequest), Observable.just(ctx)).blockingGet();
            log.info("Response to mock request: {}", result);
            ctx.assertOk();
            assertIncomingRequestResult(result, binding.getPcfDiamHoststr(), binding.getPcfDiamRealmstr());
        }

        // Binding Database is down, error response is returned
        {
            mockDb.getDb().setState(State.ERROR);
            final var ctx = new NoForwardingContext();
            final var result = handler.processRequest(Single.just(incomingRequest), Observable.just(ctx)).blockingGet();
            log.info("Response to mock request: {}", result);
            ctx.assertOk();
            assertIncomingRequestResult(result, ResultCode.DIAMETER_UNABLE_TO_DELIVER.getCode());
        }

        // Binding not found, no-binding-case is configured, design base fallback
        // destination is configured
        {
            cmSubject.onNext(Optional.of(BsfDiameterCfg.create(fallbackHost, fallbackRealm, true, nfInstance, staticDestHost, staticDestRealm, List.of()))); // Inject
                                                                                                                                                             // new
                                                                                                                                                             // CM
            // configuration
            mockDb.getDb().setState(State.NEVER_FOUND);
            final var ctx = new SingleRequestContext(staticDestHost, staticDestRealm);
            final var result = handler.processRequest(Single.just(incomingRequest), Observable.just(ctx)).blockingGet();
            log.info("Response to mock request: {}", result);
            ctx.assertOk();
            assertIncomingRequestResult(result, staticDestHost, staticDestRealm);
        }

        // Binding not found, no-binding-case is configured, design base fallback
        // destination is not configured
        {
            cmSubject.onNext(Optional.of(BsfDiameterCfg.create(null, null, true, nfInstance, staticDestHost, staticDestRealm, List.of()))); // Inject
                                                                                                                                            // new
                                                                                                                                            // CM
            // configuration
            mockDb.getDb().setState(State.NEVER_FOUND);
            final var ctx = new SingleRequestContext(staticDestHost, staticDestRealm);
            final var result = handler.processRequest(Single.just(incomingRequest), Observable.just(ctx)).blockingGet();
            log.info("Response to mock request: {}", result);
            ctx.assertOk();
            assertIncomingRequestResult(result, staticDestHost, staticDestRealm);
        }

        // Binding not found, no-binding-case is not configured, design base fallback
        // destination is selected
        {
            cmSubject.onNext(Optional.of(BsfDiameterCfg.create(fallbackHost, fallbackRealm, true, nfInstance, null, null, List.of()))); // Inject new CM
                                                                                                                                        // configuration
            mockDb.getDb().setState(State.NEVER_FOUND);
            final var ctx = new SingleRequestContext(fallbackHost, fallbackRealm);
            final var result = handler.processRequest(Single.just(incomingRequest), Observable.just(ctx)).blockingGet();
            log.info("Response to mock request: {}", result);
            ctx.assertOk();
            assertIncomingRequestResult(result, fallbackHost, fallbackRealm);
        }

        // Binding not found, alternative destination not configured, error answer is
        // returned
        {
            cmSubject.onNext(Optional.of(BsfDiameterCfg.create(null, null, true, null, null, null, List.of()))); // Inject new CM configuration
            mockDb.getDb().setState(State.NEVER_FOUND);
            final var ctx = new NoForwardingContext();
            final var result = handler.processRequest(Single.just(incomingRequest), Observable.just(ctx)).blockingGet();
            log.info("Response to mock request: {}", result);
            ctx.assertOk();
            assertIncomingRequestResult(result, ResultCode.UNABLE_TO_COMPLY.getCode());
        }

        // Multiple bindings found, fallback destination is selected
        {
            cmSubject.onNext(Optional.of(BsfDiameterCfg.create(fallbackHost, fallbackRealm, true, nfInstance, null, null, List.of()))); // Inject new CM
                                                                                                                                        // configuration
            mockDb.getDb().setState(State.MULTIPLE_FOUND);
            final var ctx = new SingleRequestContext(fallbackHost, fallbackRealm);
            final var result = handler.processRequest(Single.just(incomingRequest), Observable.just(ctx)).blockingGet();
            log.info("Response to mock request: {}", result);
            ctx.assertOk();
            assertIncomingRequestResult(result, fallbackHost, fallbackRealm);
        }

        // Multiple bindings found, alternative destination not configured error answer
        // is returned
        {
            cmSubject.onNext(Optional.of(BsfDiameterCfg.create(null, null, true, nfInstance, null, null, List.of()))); // Inject new CM configuration
            mockDb.getDb().setState(State.MULTIPLE_FOUND);
            final var ctx = new NoForwardingContext();
            final var result = handler.processRequest(Single.just(incomingRequest), Observable.just(ctx)).blockingGet();
            log.info("Response to mock request: {}", result);
            ctx.assertOk();
            assertIncomingRequestResult(result, ResultCode.UNABLE_TO_COMPLY.getCode());
        }

        // Binding not found, fallback destination is selected, configured failover
        // status code different from result code
        {
            final var diameterCommandCode = 3001;
            cmSubject.onNext(Optional.of(BsfDiameterCfg.create(fallbackHost, fallbackRealm, true, nfInstance, null, null, List.of(diameterCommandCode)))); // Inject
                                                                                                                                                           // new
                                                                                                                                                           // CM
            // configuration
            mockDb.getDb().setState(State.NEVER_FOUND);
            final var ctx = new SingleRequestContext(fallbackHost, fallbackRealm);
            final var result = handler.processRequest(Single.just(incomingRequest), Observable.just(ctx)).blockingGet();
            log.info("Response to mock request: {}", result);
            ctx.assertOk();
            assertIncomingRequestResult(result, fallbackHost, fallbackRealm);
        }

    }

    @Test
    public void aarFailover() throws Throwable
    {
        final var commandCode = CommandCode.AUTHORIZE_AUTHENTICATE.value(); // AAR
        failoverTest(commandCode, true, 3002, null); // UNABLE_TO_DELIVER
        failoverTest(commandCode, true, 5012, null); // UNABLE_TO_COMPLY

        failoverTest(commandCode, true, null, DeliveryResult.Timeout);
        failoverTest(commandCode, true, null, DeliveryResult.UndefinedError);
        failoverTest(commandCode, true, null, DeliveryResult.NoConnection);
    }

    @Test
    public void aarFailoverConfigurableStatusCode() throws Throwable
    {
        final var commandCode = CommandCode.AUTHORIZE_AUTHENTICATE.value(); // AAR
        failoverTest(commandCode, false, 3004, null); // DIAMETER_TOO_BUSY

        failoverTest(commandCode, false, null, DeliveryResult.Timeout);
        failoverTest(commandCode, false, null, DeliveryResult.UndefinedError);
        failoverTest(commandCode, false, null, DeliveryResult.NoConnection);
    }

    @Test
    public void strFailover() throws Throwable
    {
        this.destHost = "diamHost0.ericsson.se";
        this.destRealm = "diamRealm.se";
        final var commandCode = CommandCode.SESSION_TERMINATION.value(); // STR
        failoverTest(commandCode, true, 3002, null); // UNABLE_TO_DELIVER
        failoverTest(commandCode, true, 5012, null); // UNABLE_TO_COMPLY

        failoverTest(commandCode, true, null, DeliveryResult.Timeout);
        failoverTest(commandCode, true, null, DeliveryResult.UndefinedError);
        failoverTest(commandCode, true, null, DeliveryResult.NoConnection);
    }

    @Test
    public void strFailoverConfigurableStatusCode() throws Throwable
    {
        this.destHost = "diamHost0.ericsson.se";
        this.destRealm = "diamRealm.se";
        final var commandCode = CommandCode.SESSION_TERMINATION.value(); // STR
        failoverTest(commandCode, false, 3004, null); // DIAMETER_TOO_BUSY

        failoverTest(commandCode, false, null, DeliveryResult.Timeout);
        failoverTest(commandCode, false, null, DeliveryResult.UndefinedError);
        failoverTest(commandCode, false, null, DeliveryResult.NoConnection);
    }

    @Test
    public void strFailoverWithoutDestinationHost() throws Throwable
    {
        final var commandCode = CommandCode.SESSION_TERMINATION.value(); // STR
        failoverTest(commandCode, true, 3002, null); // UNABLE_TO_DELIVER
        failoverTest(commandCode, true, 5012, null); // UNABLE_TO_COMPLY

        failoverTest(commandCode, true, null, DeliveryResult.Timeout);
        failoverTest(commandCode, true, null, DeliveryResult.UndefinedError);
        failoverTest(commandCode, true, null, DeliveryResult.NoConnection);
    }

    @Test
    public void strFailoverWithoutDestinationHostConfigurableStatusCode() throws Throwable
    {
        final var commandCode = CommandCode.SESSION_TERMINATION.value(); // STR
        failoverTest(commandCode, false, 3004, null); // DIAMETER_TOO_BUSY

        failoverTest(commandCode, false, null, DeliveryResult.Timeout);
        failoverTest(commandCode, false, null, DeliveryResult.UndefinedError);
        failoverTest(commandCode, false, null, DeliveryResult.NoConnection);
    }

    @AfterMethod
    public void cleanup()
    {
        this.destHost = null;
        this.destRealm = null;
    }

    public void failoverTest(int commandCode,
                             boolean designBaseConfig,
                             Integer failureResultCode,
                             DeliveryResult deliveryResult) throws Throwable
    {
        // AAR is received, found in database, forwarded,answer returned back.

        final int failovers = 100;
        final BsfMockDb mockDb = new BsfMockDb(1, 1);

        log.info("BsfMock database: {}", mockDb.getDb());
        final List<Integer> failureRsCode = failureResultCode != null ? List.of(failureResultCode) : List.of();
        final var cm = designBaseConfig ? Flowable.just(Optional.of(BsfDiameterCfg.create(fallbackHost,
                                                                                          fallbackRealm,
                                                                                          true,
                                                                                          nfInstance,
                                                                                          null,
                                                                                          null,
                                                                                          List.of())))
                                        : Flowable.just(Optional.of(BsfDiameterCfg.create(fallbackHost,
                                                                                          fallbackRealm,
                                                                                          true,
                                                                                          nfInstance,
                                                                                          null,
                                                                                          null,
                                                                                          failureRsCode)));

        BsfDiaGrpcHandler handler = new BsfDiaGrpcHandler(mockDb.getDb(),
                                                          new BindingCleanupManager(this.rxSession,
                                                                                    this.keyspace,
                                                                                    cm.map(conf -> conf.map(BsfDiameterCfg::getNfInstanceName)
                                                                                                       .orElse("unknown"))),
                                                          cm,
                                                          Flowable.just(createPcfDbView(mockDb.getBindings(), failovers)));

        final var binding = mockDb.getBinding(0);
        final var failoverPeers = createPcfNf(binding, failovers).stream()
                                                                 .map(pcf -> Pair.with(pcf.pcfNf().getRxDiamHost(), pcf.pcfNf().getRxDiamRealm()))
                                                                 .collect(Collectors.toList());

        final var aarMsg = buildMockRequest(commandCode, afHost, afRealm, destHost, destRealm, binding.getIpv4Addr(), binding.getIpDomain()).build();

        log.info("Created aar: {}", aarMsg);
        final var incomingRequest = IncomingRequest.newBuilder().setServiceId(this.serviceId).setMessage(aarMsg).build();

        final var context = new FailoverContext(failoverPeers, failureResultCode, deliveryResult);

        final var result = handler.processRequest(Single.just(incomingRequest), Observable.just(context)).blockingGet();

        log.info("Got result: {}", result);
        log.info("Remaining peers: {}", context.remaining());

        if (commandCode != CommandCode.AUTHORIZE_AUTHENTICATE.value() && this.destHost == null && this.destRealm == null)
        {
            context.assertOk();
            assertIncomingRequestResult(result, ResultCode.UNABLE_TO_COMPLY.getCode());
        }
        else
        {
            context.assertOk();
            assertEquals(context.remaining().size(), 0);
            assertIncomingRequestResult(result, context.selectedPeer.getValue0(), context.selectedPeer.getValue1());
        }

    }

    void assertIncomingRequestResult(IncomingRequestResult result,
                                     String diaHost,
                                     String diaRealm)
    {
        final var diaMsg = result.getMessage();
        assertFalse(diaMsg.getHeader().getFlagR()); // Diameter answer
        assertFalse(diaMsg.getHeader().getFlagE()); // Not Diameter error answer

        final var parsedDiaMsg = mp.parse(diaMsg);
        final var resultDestHost = parsedDiaMsg.uniqueAvpValue(Avps.DESTINATION_HOST).get();
        final var resultDestRealm = parsedDiaMsg.uniqueAvpValue(Avps.DESTINATION_REALM).get();
        assertEquals(resultDestHost, diaHost);
        assertEquals(resultDestRealm, diaRealm);
    }

    void assertIncomingRequestResult(IncomingRequestResult result,
                                     Integer expectedResultCode)

    {
        final var diaMsg = result.getMessage();
        assertFalse(diaMsg.getHeader().getFlagR()); // Diameter answer
        assertTrue(diaMsg.getHeader().getFlagE()); // Diameter error answer

        final var parsedDiaMsg = mp.parse(diaMsg);
        final var resultCode = parsedDiaMsg.uniqueAvpValue(Avps.RESULT_CODE).get();
        assertEquals(resultCode, expectedResultCode);
    }

    private List<PcfNfRecord> createPcfNf(PcfBinding binding,
                                          int alternatives)
    {
        return IntStream.range(0, alternatives + 1).<PcfNfRecord>mapToObj(i ->
        {
            final var rxDiamHost = i == 0 ? binding.getPcfDiamHoststr() : String.format("altPcf%s.", i) + binding.getPcfDiamHoststr();
            final var uuid = i == 0 ? binding.getPcfId() : UUID.randomUUID();
            return new PcfNfRecord(PcfNf.newBuilder()
                                        .withNfInstanceId(uuid)
                                        .withNfStatus(NFStatus.REGISTERED)
                                        .withRxDiamHost(rxDiamHost)
                                        .withRxDiamRealm(binding.getPcfDiamRealmstr())
                                        .withNfSetIdList(List.of(binding.getPcfSetId()))
                                        .build(),
                                   i);
        }).collect(Collectors.toList());

    }

    /**
     * Create an alternative destination for each binding
     * 
     * @param bindings
     * @param alternatives
     * @return
     */
    private PcfDbView createPcfDbView(Collection<PcfBinding> bindings,
                                      int alternatives)
    {
        final var pcfMap = bindings.stream()
                                   .flatMap(binding -> createPcfNf(binding, alternatives).stream())
                                   .collect(Collectors.groupingBy(pcfNf -> pcfNf.pcfNf().getNfSetIdList().get(0), Collectors.toSet()));
        log.info("PcfMap {}", pcfMap);
        return new PcfDbView(pcfMap);
    }

    private static DiameterMessage.Builder buildMockRequest(int commandCode,
                                                            String originHost,
                                                            String originRealm,
                                                            String destHost,
                                                            String destRealm,
                                                            Inet4Address inetAddress,
                                                            String ipDomain)
    {

        final var diameterMessage = buildMockBaseRequest(commandCode, originHost, originRealm, inetAddress, ipDomain);
        return (destHost != null && destRealm != null) ? diameterMessage.addAvps(Avps.DESTINATION_HOST.withValue(destHost))
                                                                        .addAvps(Avps.DESTINATION_REALM.withValue(destRealm))
                                                       : diameterMessage;
    }

    private static DiameterMessage.Builder buildMockBaseRequest(int commandCode,
                                                                String originHost,
                                                                String originRealm,
                                                                Inet4Address inetAddress,
                                                                String ipDomain)
    {

        return DiameterMessage.newBuilder() //
                              .setHeader(DiameterMessageHeader.newBuilder().setFlagR(true).setFlagE(false).setCommandCode(commandCode))
                              .addAvps(Avps.ORIGIN_HOST.withValue(originHost))
                              .addAvps(Avps.ORIGIN_REALM.withValue(originRealm))
                              .addAvps(Avps.FRAMED_IP_ADDRESS.withValue(inetAddress))
                              .addAvps(Avps.IP_DOMAIN_ID.withValue(ByteString.copyFromUtf8(ipDomain)));
    }

    private static DiameterMessage.Builder mockErrorMsg(DiameterMessage.Builder msg,
                                                        int resultCode)
    {
        return msg.setHeader(msg.getHeaderBuilder().setFlagR(false).setFlagE(true)) //
                  .addAvps(Avps.RESULT_CODE.withValue(resultCode));
    }

    private static DiameterMessage.Builder mockResponseMsg(DiameterMessage.Builder msg)
    {
        return msg.setHeader(msg.getHeaderBuilder().setFlagR(false).setFlagE(false)); //
    }

    abstract class MockContext implements DiaGrpcClientContext
    {
        Throwable error;

        public void assertOk() throws Throwable
        {
            if (error != null)
                throw error;
        }

        protected void fail(Throwable e)
        {
            this.error = e;
        }

        final PeerTable peerTable = new PeerTable();

        @Override
        public long getServiceId()
        {
            return serviceId;
        }

        @Override
        public PeerTable getPeerTable()
        {
            return peerTable;
        }

        @Override
        public boolean isInitialSyncDone()
        {
            return true;
        }

        @Override
        public Single<OutgoingRequestResult> sendRequest(OutgoingRequest rxRequest)
        {
            try
            {
                return sendRequestMock(rxRequest).doOnError(e -> fail(e));
            }
            catch (Throwable e)
            {
                fail(e);
                throw e;
            }
        }

        public abstract Single<OutgoingRequestResult> sendRequestMock(OutgoingRequest rxRequest);

    }

    class FailoverContext extends MockContext
    {

        final String diamHost = null;
        final String diamRealm = null;
        final Integer errorResultCode;
        final DeliveryResult deliveryResult;
        Set<Pair<String, String>> alternativePcfs;
        Pair<String, String> selectedPeer;

        public FailoverContext(Collection<Pair<String, String>> alternativePcfs,
                               Integer errorResultCode,
                               DeliveryResult deliveryResult)
        {
            if (deliveryResult != null && errorResultCode != null || (deliveryResult == null && errorResultCode == null))
                throw new IllegalArgumentException();

            this.errorResultCode = errorResultCode;
            this.deliveryResult = deliveryResult;
            this.alternativePcfs = new HashSet<>(alternativePcfs);
            this.selectedPeer = null;
        }

        public Set<Pair<String, String>> remaining()
        {
            synchronized (this)
            {
                return new HashSet<>(alternativePcfs);
            }
        }

        @Override
        public Single<OutgoingRequestResult> sendRequestMock(OutgoingRequest rxRequest)
        {
            return Single.fromCallable(() ->
            {
                synchronized (this)
                {
                    final var parsed = mp.parse(rxRequest.getMessage());
                    final var destHost = parsed.uniqueAvpValue(Avps.DESTINATION_HOST).get();
                    final var destRealm = parsed.uniqueAvpValue(Avps.DESTINATION_REALM).get();
                    log.info("Received forwarding request for destHost={} destRealm={}", destHost, destRealm);
                    final var destinationFound = alternativePcfs.remove(Pair.with(destHost, destRealm));
                    assertTrue(destinationFound, "Unexpected destHost=" + destHost + " destRealm=" + destRealm);

                    if (alternativePcfs.size() > 0)
                    {
                        return deliveryResult == null ? OutgoingRequestResult //
                                                                             .newBuilder()
                                                                             .setMessage(mockErrorMsg(rxRequest.getMessage().toBuilder(), errorResultCode))
                                                                             .build()
                                                      : OutgoingRequestResult //
                                                                             .newBuilder()
                                                                             .setErrorCode(deliveryResult)
                                                                             .build();
                    }
                    else
                    {
                        assertNull(selectedPeer, "Unexpected forwarding request destHost=" + destHost + " destRealm=" + destRealm);

                        log.info("Successfull message forwarding");
                        selectedPeer = Pair.with(destHost, destRealm);
                        return OutgoingRequestResult //
                                                    .newBuilder()
                                                    .setMessage(mockResponseMsg(rxRequest.getMessage().toBuilder()))
                                                    .build();
                    }
                }
            });
        }
    }

    class SingleRequestContext extends MockContext
    {
        final String diamHost;
        final String diamRealm;
        final int resultCode;
        volatile boolean replied = false;

        public SingleRequestContext(String diamHost,
                                    String diamRealm,
                                    int resultCode)
        {
            this.diamHost = diamHost;
            this.diamRealm = diamRealm;
            this.resultCode = resultCode;
        }

        public SingleRequestContext(String diamHost,
                                    String diamRealm)
        {
            this(diamHost, diamRealm, -1);
        }

        @Override
        public Single<OutgoingRequestResult> sendRequestMock(OutgoingRequest rxRequest)
        {
            return Single.fromCallable(() ->
            {
                final var parsed = mp.parse(rxRequest.getMessage());
                final var destHost = parsed.uniqueAvpValue(Avps.DESTINATION_HOST).get();
                final var destRealm = parsed.uniqueAvpValue(Avps.DESTINATION_REALM).get();
                assertEquals(destHost, diamHost);
                assertEquals(destRealm, diamRealm);
                assertFalse(replied);
                return resultCode <= 0 ? OutgoingRequestResult //
                                                              .newBuilder()
                                                              .setMessage(mockResponseMsg(rxRequest.getMessage().toBuilder()))
                                                              .build()
                                       : OutgoingRequestResult //
                                                              .newBuilder()
                                                              .setMessage(mockErrorMsg(rxRequest.getMessage().toBuilder(), resultCode))
                                                              .build();
            });
        };
    }

    class NoForwardingContext extends MockContext
    {
        @Override
        public Single<OutgoingRequestResult> sendRequestMock(OutgoingRequest rxRequest)
        {
            throw new AssertionError("Expected not to be called");
        }
    }
}
