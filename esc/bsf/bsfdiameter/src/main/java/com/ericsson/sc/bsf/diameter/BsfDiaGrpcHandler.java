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
 * Created on: May 1, 2020
 *     Author: echfari
 */
package com.ericsson.sc.bsf.diameter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.oss.driver.api.core.RequestThrottlingException;
import com.ericsson.esc.bsf.openapi.model.DiscoveryQuery;
import com.ericsson.esc.bsf.openapi.model.Ipv6Prefix;
import com.ericsson.esc.bsf.openapi.model.UeAddress;
import com.ericsson.esc.bsf.worker.BindingCleanupManager;
import com.ericsson.esc.bsf.worker.NBsfManagementService;
import com.ericsson.esc.bsf.worker.NBsfManagementService.DiscoveryResult;
import com.ericsson.gs.tm.diameter.service.grpc.DeliveryResult;
import com.ericsson.gs.tm.diameter.service.grpc.DiameterAvp;
import com.ericsson.gs.tm.diameter.service.grpc.DiameterMessage;
import com.ericsson.gs.tm.diameter.service.grpc.IncomingRequest;
import com.ericsson.gs.tm.diameter.service.grpc.IncomingRequestResult;
import com.ericsson.gs.tm.diameter.service.grpc.OutgoingRequest;
import com.ericsson.gs.tm.diameter.service.grpc.OutgoingRequestResult;
import com.ericsson.sc.bsf.etcd.PcfDbView;
import com.ericsson.sc.bsf.etcd.PcfNf;
import com.ericsson.sc.diameter.DiaGrpcClientContext;
import com.ericsson.sc.diameter.DiaGrpcHandler;
import com.ericsson.sc.diameter.avp.AvpException;
import com.ericsson.sc.diameter.avp.Avps;
import com.ericsson.sc.diameter.avp.CommandCode;
import com.ericsson.sc.diameter.avp.MessageParser;
import com.ericsson.sc.diameter.avp.ParsedDiameterMessage;
import com.ericsson.sc.diameter.avp.ResultCode;
import com.ericsson.sc.util.LogLimitter;
import com.ericsson.sc.util.LogLimitter.Loggers;
import com.google.protobuf.ByteString;

import io.prometheus.client.Counter;
import io.prometheus.client.Counter.Child;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Handles Diameter messages related to BSF diameter Functionality
 */
public class BsfDiaGrpcHandler implements DiaGrpcHandler
{
    /**
     * log limiter labels
     */
    private enum Lbl
    {
        THROTTLE,
        REJECT1,
        DROP,
        AAR_ROUTING,
        NON_AAR_ROUTING,
        INVALID_STATE,
        FORWARD,
        FAILOVER_2,
        FAILOVER_3,
        FAILOVER_4,
        FAILOVER_1,
        INCOMING_REQUEST,
        SEND_ANSWER,
        DISCOVERY_QUERY
    }

    private static final Logger log = LoggerFactory.getLogger(BsfDiaGrpcHandler.class);
    private Loggers<Lbl> safeLog = LogLimitter.create(Lbl.class, log, 2000);
    private final MessageParser mp = Avps.messageParser();
    private final Flowable<Optional<BsfDiameterCfg>> cfgObservable;
    private final NBsfManagementService bsfDb;
    private final BindingCleanupManager bindingCleanup;
    private final Flowable<PcfDbView> pcfDbFlow;
    private final Counters counters = new Counters();

    public BsfDiaGrpcHandler(NBsfManagementService bsfDb,
                             BindingCleanupManager bindingCleanup,
                             Flowable<Optional<BsfDiameterCfg>> cfg,
                             Flowable<PcfDbView> pcfDbFlow)
    {
        this.bsfDb = bsfDb;
        this.bindingCleanup = bindingCleanup;
        this.cfgObservable = cfg;
        this.pcfDbFlow = pcfDbFlow;
    }

    @Override
    public Single<IncomingRequestResult> processRequest(Single<IncomingRequest> request,
                                                        Observable<DiaGrpcClientContext> diaGrpcClientInstance)
    {
        try
        {

            return request //
                          .doOnSuccess(req -> safeLog.log(Lbl.INCOMING_REQUEST, logger -> logger.debug("IncomingRequest message : {}", req)))
                          .toFlowable()
                          .withLatestFrom(diaGrpcClientInstance.toFlowable(BackpressureStrategy.LATEST), cfgObservable, pcfDbFlow, RequestContext::new)
                          .flatMapSingle(req -> req.processRequest()
                                                   .doOnSuccess(res -> safeLog.log(Lbl.SEND_ANSWER, logger -> logger.debug("Sending answer {}", res)))
                                                   .doOnError(err ->
                                                   {
                                                       if (err instanceof RequestThrottlingException) // TODO handle this exception in database layer
                                                       {
                                                           safeLog.log(Lbl.THROTTLE,
                                                                       logger -> logger.warn("Rejecting ingress request due to database overload, originHost: {}, originRealm: {}",
                                                                                             req.parsedDm.getOriginHost(),
                                                                                             req.parsedDm.getOriginRealm()));
                                                       }
                                                       else
                                                       {
                                                           safeLog.log(Lbl.REJECT1,
                                                                       logger -> logger.warn("Error while processing incoming request, diameter message will be rejected, originHost: {}, originRealm: {}",
                                                                                             req.parsedDm.getOriginHost(),
                                                                                             req.parsedDm.getOriginRealm(),
                                                                                             err));
                                                       }
                                                   })
                                                   .onErrorResumeNext(req::reject)) // Reject the diameter message
                          .singleOrError()
                          .doOnError(e -> safeLog.log(Lbl.DROP, logger -> logger.error("Unexpeceted error while processing IncomingRequest gRPC message", e)));
        }
        catch (Exception e)
        {
            // This should never happen
            log.error("Exception while processing IncomingRequest gRPC message", e);
            return Single.error(e);
        }
    }

    private final class RequestContext
    {
        private final DiaGrpcClientContext context;
        private final ParsedDiameterMessage parsedDm;
        private final Optional<BsfDiameterCfg> cfg;
        private final PcfDbView pcfDb;
        private final boolean allowFailover;
        private final List<Integer> reRouteError;

        RequestContext(IncomingRequest request,
                       DiaGrpcClientContext context,
                       Optional<BsfDiameterCfg> cfg,
                       PcfDbView pcfDb)
        {
            this.context = context;
            this.cfg = cfg;
            this.parsedDm = mp.parse(request.getMessage());
            this.pcfDb = pcfDb;
            this.allowFailover = cfg.map(BsfDiameterCfg::isFailoverEnabled).orElse(false);
            this.reRouteError = cfg.map(BsfDiameterCfg::getReRouteError).orElse(List.of());

        }

        private Single<IncomingRequestResult> reject(ResultCode resultCode)
        {
            return Single.fromCallable(() -> IncomingRequestResult.newBuilder()
                                                                  .setMessage(parsedDm.createErrorAnswerFromRequest(resultCode, Collections.emptyList()))
                                                                  .build())
                         .doOnSubscribe(sub -> log.debug("Rejecting Diameter message with result code: {}", resultCode));
        }

        private Single<IncomingRequestResult> reject(Throwable t)
        {
            return Single.fromCallable(() ->
            {
                log.debug("Rejecting Diameter message due to exception: {}", t.getMessage());

                final List<DiameterAvp> failedAvps = (t instanceof AvpException) ? List.of(((AvpException) t).getFailedAvp()) : Collections.emptyList();
                final ResultCode resultCode;
                if (t instanceof AvpException)
                {
                    resultCode = ((AvpException) t).getResultCode();
                }
                else if (t instanceof RequestThrottlingException)
                {
                    resultCode = ResultCode.DIAMETER_TOO_BUSY;
                }
                else
                {
                    resultCode = ResultCode.DIAMETER_UNABLE_TO_DELIVER;
                }
                return IncomingRequestResult.newBuilder()
                                            .setMessage(parsedDm.createErrorAnswerFromRequest(resultCode, failedAvps)) //
                                            .build();
            });
        }

        /**
         * This method does not throw any exceptions
         * 
         * @return
         */
        public Single<IncomingRequestResult> processRequest()
        {
            return this.cfg.map(bsfDiaCfg ->
            {
                try
                {

                    final var destHost = parsedDm.uniqueAvpValue(Avps.DESTINATION_HOST);
                    final var destRealm = parsedDm.uniqueAvpValue(Avps.DESTINATION_REALM);
                    if (destHost.isPresent())
                    {
                        safeLog.log(Lbl.NON_AAR_ROUTING, logger -> logger.debug("Routing message using Destination-Host AVP: {}", destHost));
                        // Let Diameter stack route message
                        final var rd = new RoutingDecision(destHost.get(), destRealm.orElse(null), null); // FIXME handle null destination realm?
                        return forwardMessage(parsedDm.getDiameterMessage(), rd) //
                                                                                .map(this::processForwardMessageResult);
                    }
                    else if (parsedDm.getDiameterMessage().getHeader().getCommandCode() == CommandCode.AUTHORIZE_AUTHENTICATE.value())
                    {
                        // This is an AAR message
                        return queryDatabase() //
                                              .map(discoveryResult -> this.processDbResult(discoveryResult, bsfDiaCfg))
                                              .doOnSuccess(rd -> safeLog.log(Lbl.AAR_ROUTING,
                                                                             logger -> logger.debug("Routing AAR message using PCF binding, destinationHost: {} destinationRealm: {}",
                                                                                                    rd.destinationHost,
                                                                                                    rd.destinationRealm)))
                                              .filter(rd -> rd.destinationHost != null || rd.destinationRealm != null)

                                              .flatMapSingleElement(rd -> forwardMessage(createRoutedDm(rd, parsedDm),
                                                                                         rd).map(this::processForwardMessageResult))
                                              .switchIfEmpty(reject(ResultCode.UNABLE_TO_COMPLY) // Failed to route message, db lookup was not performed.
                                                                                                .doOnSubscribe(disp -> safeLog.log(Lbl.AAR_ROUTING,
                                                                                                                                   logger -> logger.warn("Unable to route AAR request, rejected. originHost:{}, originRealm:{} ",
                                                                                                                                                         parsedDm.getOriginHost(),
                                                                                                                                                         parsedDm.getOriginRealm()))));
                    }
                    else
                    {
                        return reject(ResultCode.UNABLE_TO_COMPLY) //
                                                                  .doOnSubscribe(disp -> safeLog.log(Lbl.NON_AAR_ROUTING,
                                                                                                     logger -> logger.warn("Rejected non-AAR request without Destination-Host AVP. originHost: {}, originRealm: {}",
                                                                                                                           parsedDm.getOriginHost(),
                                                                                                                           parsedDm.getOriginRealm()

                                                                                                     )));
                    }
                }
                catch (Exception e)
                {
                    return Single.<IncomingRequestResult>error(e);
                }
            })
                           .orElseGet(() -> this.reject(ResultCode.UNABLE_TO_COMPLY)
                                                .doOnSubscribe(disp -> safeLog.log(Lbl.INVALID_STATE,
                                                                                   logger -> logger.warn("Rejected Diameter request, BSF configuration incomplete")))); // No
                                                                                                                                                                        // configuration
                                                                                                                                                                        // exists,
                                                                                                                                                                        // message
                                                                                                                                                                        // cannot
                                                                                                                                                                        // be
                                                                                                                                                                        // routed
        }

        private Maybe<DiscoveryResult> queryDatabase()
        {

            final var framedIpv4 = parsedDm.uniqueAvpValue(Avps.FRAMED_IP_ADDRESS);

            final Supplier<Optional<UeAddress>> framedIpv6 = () -> parsedDm.uniqueAvpValue(Avps.FRAMED_IPV6_PREFIX)
                                                                           .map(framedIpv6Prefix -> new Ipv6Prefix(framedIpv6Prefix.getPrefix(),
                                                                                                                   (short) framedIpv6Prefix.getPrefixLength()))
                                                                           .filter(prefix -> prefix.getPrefixLength() == 128)
                                                                           .map(prefix -> UeAddress.ipv6Prefix64tranformer(prefix)) // FIXME Remove this hack,
                                                                                                                                    // prfix transformation
                                                                                                                                    // should happen somewhere
                                                                                                                                    // else
                                                                           .map(UeAddress::new);
            final var ueAddress = framedIpv4 //
                                            .map(ipv4Addr -> new UeAddress(ipv4Addr, parsedDm.uniqueAvpValue(Avps.IP_DOMAIN_ID).map(ByteString::toStringUtf8)))
                                            .or(framedIpv6);

            final var dq = ueAddress.map(DiscoveryQuery.UeAddr::new);
            return dq //
                     .map(query -> bsfDb.discovery(query)
                                        .doOnSuccess(res -> safeLog.log(Lbl.DISCOVERY_QUERY,
                                                                        logger -> logger.debug("NbsfManagement discovery query: {} result {}", query, res)))
                                        .toMaybe())
                     .orElseGet(Maybe::empty); // Appropriate database query could not be constructed from diameter message
        }

        /**
         * Forwards diameter message, performing failover if required
         * 
         * @param dm
         * @param rd
         * @return
         */
        private Single<OutgoingRequestResult> forwardMessage(DiameterMessage dm,
                                                             RoutingDecision rd)
        {
            return tryForwardMessage(dm) //
                                        .flatMap(forwardingResult -> isFailoverApplicable(forwardingResult) ? failover(forwardingResult, rd)
                                                                                                            : Single.just(forwardingResult));
        }

        /**
         * Forwards diameter message without attempting failover
         * 
         * @param dm
         * @return
         */
        private Single<OutgoingRequestResult> tryForwardMessage(DiameterMessage dm)
        {
            return context.sendRequest(OutgoingRequest //
                                                      .newBuilder()
                                                      .setMessage(dm)
                                                      .setServiceId(context.getServiceId())
                                                      .build());
        }

        /**
         * 
         * @param forwardingResult
         * @return true if failover should be attempted
         */
        private boolean isFailoverApplicable(OutgoingRequestResult forwardingResult)
        {
            if (!this.allowFailover)
            {
                // Failovers are disabled, no failover should be performed
                return false;
            }
            if (forwardingResult.getErrorCode() != DeliveryResult.Success)
            {
                // Diameter stack failed to forward message, failover applies
                return true;
            }

            // Message was forwarded, remote peer sent an error answer, failover might be
            // applicable

            final var parsedReponse = mp.parse(forwardingResult.getMessage()); // Parse Diameter answer
            return parsedReponse.uniqueAvpValue(Avps.RESULT_CODE) // Extract result code
                                .filter(Objects::nonNull)
                                .map(this::checkResultCodes)
                                .orElseGet(() ->
                                {
                                    // answer contains no result code
                                    log.debug("No Result-Code AVP from diameter answer, failover will not be attempted {}", forwardingResult.getMessage());
                                    return false;
                                });
        }

        private boolean checkResultCodes(final int resultCode)
        {
            return reRouteError.isEmpty() ? designBaseFailoverChecker(resultCode) : fromYangFailoverChecker(resultCode);
        }

        private boolean designBaseFailoverChecker(final int resultCode)
        {
            return resultCode == ResultCode.DIAMETER_UNABLE_TO_DELIVER.getCode() || resultCode == (ResultCode.UNABLE_TO_COMPLY.getCode());
        }

        private boolean fromYangFailoverChecker(final int resultCode)
        {
            return reRouteError.contains(resultCode);
        }

        private Single<OutgoingRequestResult> failover(OutgoingRequestResult failedReason,
                                                       RoutingDecision failedDecision)
        {
            // sanity check
            if (failedDecision.destinationHost == null || failedDecision.destinationRealm == null)
            {
                safeLog.log(Lbl.FAILOVER_1, logger -> logger.error("Cannot perform failover with routing decision {}", failedDecision));
                return Single.just(failedReason);
            }

            // find alternative destinations

            final var alternativePcfs = pcfDb.findPcfsInSet(failedDecision.destinationHost, failedDecision.destinationRealm, failedDecision.pcfSetId)
                                             .stream() //
                                             .filter(pcf -> !failedDecision.destinationHost.equals(pcf.getRxDiamHost())
                                                            && !failedDecision.destinationRealm.equals(pcf.getRxDiamHost()))
                                             .toList();

            safeLog.log(Lbl.FAILOVER_2,
                        logger -> logger.info("Will attempt failover for routing decision {} to {} PCFs {}",
                                              failedDecision,
                                              alternativePcfs.size(),
                                              alternativePcfs));

            final var availablePcfs = new ConcurrentLinkedQueue<PcfNf>(alternativePcfs);
            final var pcfSetId = failedDecision.pcfSetId;

            final var singleFailover = Maybe.defer(() ->
            {
                final var nextPcf = availablePcfs.poll(); // pop next available PCF
                if (nextPcf == null)
                {
                    return Maybe.empty();
                }
                final var newHost = nextPcf.getRxDiamHost();
                final var newRealm = nextPcf.getRxDiamRealm();
                final var newDm = this.createRoutedDm(new RoutingDecision(newHost, newRealm, pcfSetId), this.parsedDm); // Adds pcfSetId for completeness
                return tryForwardMessage(newDm) //
                                               .doOnSuccess(result ->
                                               {

                                                   final var tryAgain = isFailoverApplicable(result);
                                                   if (log.isDebugEnabled())
                                                   {
                                                       final var remainingAttempts = availablePcfs.size();
                                                       log.debug("Failover response tryAgain {} alternatives remaining {} ", tryAgain, remainingAttempts);
                                                   }
                                                   if (!tryAgain)
                                                   {
                                                       availablePcfs.clear();
                                                   }
                                               })
                                               .doOnError(err ->
                                               {
                                                   safeLog.log(Lbl.FAILOVER_3, logger -> logger.warn("Failover stopped unexpectedly", err)); // Failover stops
                                                                                                                                             // upon
                                                   // unexpected error
                                                   availablePcfs.clear(); // Not really needed because the loop will terminate upon error
                                               })
                                               .toMaybe();
            });
            return singleFailover.repeatUntil(availablePcfs::isEmpty) //
                                 .lastElement()
                                 .toSingle(failedReason)
                                 .doOnError(err -> safeLog.log(Lbl.FAILOVER_4,
                                                               logger -> logger.info("Unexpected error while attempting failover for {}", failedDecision, err)))
                                 .onErrorReturnItem(failedReason)
                                 .doOnSuccess(result -> log.debug("Failover finished, result: {}", result));
        }

        private IncomingRequestResult processForwardMessageResult(OutgoingRequestResult forwardingResult)
        {
            final var errorCode = forwardingResult.getErrorCode();
            if (errorCode == DeliveryResult.Success)
            {
                return IncomingRequestResult //
                                            .newBuilder()
                                            .setMessage(forwardingResult.getMessage())
                                            .build();
            }
            else
            {
                final var errorAnswer = IncomingRequestResult //
                                                             .newBuilder()
                                                             .setMessage(this.parsedDm.createErrorAnswerFromRequest(ResultCode.DIAMETER_UNABLE_TO_DELIVER,
                                                                                                                    Collections.emptyList()))
                                                             .build();
                safeLog.log(Lbl.FORWARD,
                            logger -> logger.warn("Failed to forward Diameter Message , errorCode: {} >>>>>\n {} \n<<<<<",
                                                  errorCode,
                                                  this.parsedDm.getDiameterMessage()));
                return errorAnswer;
            }
        }

        private RoutingDecision processDbResult(DiscoveryResult discoveryRes,
                                                BsfDiameterCfg bsfDiaCfg)
        {
            // Check staleBindingsList. If not empty delete each binding.
            if (!discoveryRes.getStaleBindings().isEmpty())
                discoveryRes.getStaleBindings().forEach(bindingCleanup::deleteBindingAsync);

            switch (discoveryRes.getResult())
            {
                case OK_MULTIPLE:
                    stepMultipleBindingsCounter("resolved", bsfDiaCfg);
                    break;
                case REJECT_MULTIPLE:
                    stepMultipleBindingsCounter("rejected", bsfDiaCfg);
                    break;
                case TOO_MANY:
                    stepMultipleBindingsCounter("too_many", bsfDiaCfg);
                    break;
                default:
                    break;
            }

            return discoveryRes //
                               .getPcfBinding()
                               .filter(b -> b.getPcfDiamHost() != null && b.getPcfDiamRealm() != null)
                               .map(b -> new RoutingDecision(b.getPcfDiamHoststr(), b.getPcfDiamRealmstr(), b.getPcfSetId()))
                               .orElseGet(() -> decideRoute(bsfDiaCfg));

        }

        private RoutingDecision decideRoute(BsfDiameterCfg bsfDiaCfg)
        {
            if (bsfDiaCfg.getStaticDestRealm() != null) // It indicates if the no-binding-case is configured in model
            {
                log.debug("Re-routing to the static profile, dest-host: {}, dest-realm: {}", bsfDiaCfg.getStaticDestHost(), bsfDiaCfg.getStaticDestRealm());
                return new RoutingDecision(bsfDiaCfg.getStaticDestHost(), bsfDiaCfg.getStaticDestRealm(), null);
            }
            else
            {
                log.debug("Re-routing to the look-up profile, dest-host: {}, dest-realm: {}",
                          bsfDiaCfg.getFallbackDestHost(),
                          bsfDiaCfg.getFallbackDestRealm());
                return new RoutingDecision(bsfDiaCfg.getFallbackDestHost(), bsfDiaCfg.getFallbackDestRealm(), null);
            }
        }

        private DiameterMessage createRoutedDm(RoutingDecision des,
                                               ParsedDiameterMessage pm)
        {
            var dm = pm.transform();

            if (des.destinationHost != null)
            {
                dm.addOrReplaceAvp(Avps.DESTINATION_HOST, des.destinationHost);
            }

            if (des.destinationRealm != null)
            {
                dm.addOrReplaceAvp(Avps.DESTINATION_REALM, des.destinationRealm);
            }

            return dm.buildMessage();

        }

        private void stepMultipleBindingsCounter(String resolution,
                                                 BsfDiameterCfg bsfDiaCfg)
        {
            final var nfInstanceName = bsfDiaCfg.getNfInstanceName();

            log.debug("Stepping multiple bindings counter, resolution: {}, nfInstanceName: {}", resolution, nfInstanceName);
            counters.getCcMultipleBindings(resolution, nfInstanceName).inc();
        }
    }

    private static final class RoutingDecision
    {

        final String destinationHost;
        final String destinationRealm;
        final String pcfSetId;

        public RoutingDecision(String destinationHost,
                               String destinationRealm,
                               String pcfSetId)
        {

            this.destinationHost = destinationHost;
            this.destinationRealm = destinationRealm;
            this.pcfSetId = pcfSetId;

        }

    }

    static class Counters
    {
        /**
         * Prometheus counter for multiple bindings resolution
         */
        private static final Counter ccMultipleBindings = io.prometheus.client.Counter.build()
                                                                                      .namespace("bsf")
                                                                                      .name("multiple_bindings_found_total")
                                                                                      .labelNames("resolution", "interface", "nf_instance", "nf")
                                                                                      .help("Number of times multiple bindings were retrieved")
                                                                                      .register();

        public Child getCcMultipleBindings(String resolution,
                                           String nfInstance)
        {
            return ccMultipleBindings.labels(resolution,        // resolution type
                                             "diameter_lookup", // interface
                                             nfInstance,        // nf_instance
                                             "bsf");            // nf
        }
    }

}
