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
 * Created on: Mar 29, 2019
 *     Author: eedrak, eedstl
 */

package com.ericsson.sc.proxyal.service;

import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.proxyal.proxyconfig.ProxyCfg;
import com.ericsson.utilities.common.AtomicRef;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.reactivex.VertxInstance;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.protobuf.Any;
import com.google.protobuf.Value;
import com.google.rpc.Code;
import com.google.rpc.Status;

import io.envoyproxy.envoy.config.core.v3.ControlPlane;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryRequest;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

public class EnvoyAdsLogic
{
    public static class Config
    {
        private final EnvoyCfg configuration;
        private final Counter version;

        public Config(EnvoyCfg config,
                      Long version,
                      Long offset)
        {
            this.configuration = config;
            this.version = new Counter(version, offset);
        }

        public final EnvoyCfg getConfig()
        {
            return this.configuration;
        }

        public final Counter getVersion()
        {
            return this.version;
        }

        public String toString()
        {
            return new StringBuilder().append("{")
                                      .append("version=")
                                      .append(this.version)
                                      .append(", proxyConfig=")
                                      .append(this.configuration.toString())
                                      .append("}")
                                      .toString();
        }
    }

    private static class Context
    {
        private final Counter currentNonce;
        private final Counter lastAckVersion;

        public Context(Long versionOffset)
        {
            this.currentNonce = new Counter(versionOffset, versionOffset);
            this.lastAckVersion = new Counter(versionOffset - 1L, versionOffset);
        }

        public Counter getCurrentNonce()
        {
            return this.currentNonce;
        }

        public Counter getLastAckVersion()
        {
            return this.lastAckVersion;
        }

        public String toString()
        {
            return new StringBuilder().append("{")
                                      .append("currentNonce=")
                                      .append(this.currentNonce)
                                      .append(", lastAckVersion=")
                                      .append(this.lastAckVersion)
                                      .append("}")
                                      .toString();
        }
    }

    private static class Counter
    {
        private final AtomicLong count;
        private final Long offset;

        public Counter(Long init,
                       Long offSet)
        {
            this.count = new AtomicLong(init);
            this.offset = offSet;
        }

        public Long delta(String s)
        {
            Long r = s.isEmpty() ? this.offset : Long.valueOf(s);
            Long l = this.count.get();
            Long result = l - r;
            log.debug("absolute delta, l={}, r={}, result={}, offSet={}", l, r, result, this.offset);
            return result;
        }

        public Long get()
        {
            return this.count.get();
        }

        public Long incrementAndGet()
        {
            return this.count.incrementAndGet();
        }

        public Long offset()
        {
            return this.offset;
        }

        public void set(String value)
        {
            try
            {
                this.count.set(value.isEmpty() ? this.offset : Long.valueOf(value));
            }
            catch (NumberFormatException e)
            {
                log.error("Exception caught", e);
                this.count.set(this.offset);
            }
        }

        public String toString()
        {
            return String.valueOf(this.count);
        }
    }

    private static class StreamContext
    {
        private final AtomicRef<Optional<Config>> currentConfig = new AtomicRef<>(Optional.<Config>empty());
        private final AtomicRef<DiscoveryRequest> currentRequest = new AtomicRef<>(null);
        private final BehaviorSubject<Optional<DiscoveryRequest>> requestsForRetry = BehaviorSubject.createDefault(Optional.<DiscoveryRequest>empty());
        private final AtomicRef<Optional<DiscoveryRequest>> currentRetry = new AtomicRef<>(Optional.<DiscoveryRequest>empty());

        private final Map<String, Context> contexts = new HashMap<>();

        private final Flowable<Optional<Config>> configFlow;
        private final Counter cfgVersion;

        /**
         * Only one request per request type can be pending, as older requests are
         * regarded stale and shall be skipped.
         */
        private final AtomicRef<DiscoveryRequest> pendingRequestCds = new AtomicRef<>(null);
        private final AtomicRef<DiscoveryRequest> pendingRequestEds = new AtomicRef<>(null);

        private final AtomicRef<DiscoveryRequest> pendingRequestLds = new AtomicRef<>(null);
        private final AtomicRef<DiscoveryRequest> pendingRequestRds = new AtomicRef<>(null);

        private String sender = null;

        public StreamContext(final Flowable<Optional<EnvoyCfg>> evConfigFlow,
                             final Counter configVersion)
        {
            Long offset = configVersion.offset();
            this.cfgVersion = new Counter(configVersion.get(), offset);

            this.configFlow = evConfigFlow.map(o -> o.map(ev -> new Config(ev, this.cfgVersion.incrementAndGet(), offset)));
        }

        public Counter getCfgVersion()
        {
            return this.cfgVersion;
        }

        public Flowable<Optional<Config>> getConfigFlow()
        {
            return this.configFlow;
        }

        public Map<String, Context> getContexts()
        {
            return this.contexts;
        }

        public AtomicRef<Optional<Config>> getCurrentConfig()
        {
            return this.currentConfig;
        }

        public AtomicRef<DiscoveryRequest> getCurrentRequest()
        {
            return this.currentRequest;
        }

        public AtomicRef<Optional<DiscoveryRequest>> getCurrentRetry()
        {
            return this.currentRetry;
        }

        public AtomicRef<DiscoveryRequest> getPendingRequestCds()
        {
            return this.pendingRequestCds;
        }

        public AtomicRef<DiscoveryRequest> getPendingRequestEds()
        {
            return this.pendingRequestEds;
        }

        public AtomicRef<DiscoveryRequest> getPendingRequestLds()
        {
            return this.pendingRequestLds;
        }

        public AtomicRef<DiscoveryRequest> getPendingRequestRds()
        {
            return this.pendingRequestRds;
        }

        public BehaviorSubject<Optional<DiscoveryRequest>> getRequestsForRetry()
        {
            return this.requestsForRetry;
        }

        public String getSender()
        {
            return this.sender;
        }

        public StreamContext setSender(String sender)
        {
            this.sender = sender;
            return this;
        }
    }

    private static final Logger log = LoggerFactory.getLogger(EnvoyAdsLogic.class);

    // Alarms are raised with a time-to-live (TTL) and have to be re-raised before
    // they expire. We have to add a saftety margin to the TTL compared to the timer
    // that triggers the periodic update in case the periodic update job is delayed.
    // If that would happen, then the AlarmHandlerImpl would auomtatically cease the
    // alarm and we would very shortly after that raise the alarm again, causing
    // flapping alarms.
    private static final Long ALARM_TTL_SEC = 60L;
    private static final int MAX_ALARM_ERROR_MESSAGE = 80;
    private static final String PROTOCOL_EXCEPTION_MESSAGE = "(%s): Different versions between worker(%s) and manager(%s).";
    private static final String ENVOY_VERSION_METADATA = "version";
    private static final String ENV_APP_VERSION_STRING = "APP_VERSION";
    private final AdsAlarm badConfigurationAlarm;
    private Long alarmUpdateTimerId = null;

    private final Flowable<Optional<ProxyCfg>> pxCfg;
    BehaviorSubject<Optional<EnvoyCfg>> configFlow = BehaviorSubject.createDefault(Optional.empty());

    private final Long versionOffset;
    private final Counter configVersion;
    private final AtomicInteger activeStreams;
    private final Optional<String> appVersionOptional;
    private PublishSubject<String> envoyDisconnections;
    private Disposable disposable;

    EnvoyAdsLogic(AdsAlarm badConfigurationAlarm,
                  Flowable<Optional<ProxyCfg>> config)
    {
        this(badConfigurationAlarm, config, 0L);
    }

    /**
     * Create service for testing purposes
     * 
     * @param ah
     * @param config
     * @param versionOffset
     */
    EnvoyAdsLogic(AdsAlarm badConfigurationAlarm,
                  Flowable<Optional<ProxyCfg>> config,
                  Long versionOffset)
    {
        this.badConfigurationAlarm = badConfigurationAlarm;
        this.versionOffset = versionOffset;
        this.pxCfg = config;
        this.appVersionOptional = Optional.ofNullable(EnvVars.get(ENV_APP_VERSION_STRING));

        this.configVersion = new Counter(versionOffset, versionOffset);
        this.activeStreams = new AtomicInteger();

        this.envoyDisconnections = PublishSubject.create();
        this.disposable = null;
    }

    public Completable start()
    {
        return Completable.fromAction(() ->
        {
            if (disposable == null)
            {
                disposable = this.pxCfg.filter(Optional::isPresent) //
                                       .map(Optional::get) //
                                       .subscribe(cfg ->
                                       {
                                           try
                                           {
                                               log.info("New ProxyCfg received.");

                                               var evCfg = new EnvoyCfg(cfg);
                                               this.badConfigurationAlarm.presetFaultyResource(evCfg.getRdnOfNfInstance().toString(false));

                                               if (this.activeStreams.get() > 0)
                                               {
                                                   final Long cfgVersion = this.configVersion.incrementAndGet();
                                                   log.info("Stepped service level configuration version to {}.", cfgVersion);
                                               }
                                               log.info("Publishing new EnvoyCfg.");
                                               configFlow.onNext(Optional.of(evCfg));

                                           }
                                           catch (Exception e)
                                           {
                                               log.error("Failed to process configuration change", e);
                                               // Ignore unexpected errors, so that processing is not permanently terminated
                                           }
                                       }, err -> log.error("AggregatedDiscoveryService configuration change processing terminated unexpectedly", err));

                var delay = EnvoyAdsLogic.ALARM_TTL_SEC * 1000;
                this.alarmUpdateTimerId = VertxInstance.get().setPeriodic(delay, id ->
                {
                    try
                    {
                        updateAlarms();
                    }
                    catch (JsonProcessingException e)
                    {
                        log.error("Failed to raise alarm every {}ms", delay, e);
                    }
                });
            }
        });
    }

    public Completable stop()
    {
        return Completable.fromAction(() ->
        {
            if (this.disposable != null)
            {
                this.disposable.dispose();
                this.disposable = null;
            }
            if (this.alarmUpdateTimerId != null)
            {
                VertxInstance.get().cancelTimer(this.alarmUpdateTimerId);
                this.alarmUpdateTimerId = null;
            }
        });
    }

    /**
     * Periodically called function to re-raise alarms
     * 
     * @throws JsonProcessingException
     */
    public void updateAlarms() throws JsonProcessingException
    {
        log.debug("Periodic alarm update");
        this.badConfigurationAlarm.reRaiseMajor();
    }

    /*
     * publishes the ID of disconnecting Envoy PODs
     */
    public PublishSubject<String> getEnvoyDisconnections()
    {
        return this.envoyDisconnections;
    }

    public Flowable<DiscoveryResponse> streamAggregatedResources(Flowable<DiscoveryRequest> request)
    {
        // Print the Envoy request before a "combineLatest()"-operation happens.
        // Needed for troubleshooting.
        request = request.doOnNext(req ->
        {

            if (log.isDebugEnabled())
            {
                log.debug("{}: Received request ({} {} (version {}, nonce {}):\n{}",
                          req.getNode().getId(),
                          req.getNode()
                             .getMetadata()
                             .getFieldsOrDefault(ENVOY_VERSION_METADATA, Value.newBuilder().setStringValue("").build())
                             .getStringValue(),
                          lastWordOf(req.getTypeUrl()),
                          req.getVersionInfo(),
                          req.getResponseNonce(),
                          req);
            }
            else
            {
                log.info("{}: Received request ({} {} (version {}, nonce {})",
                         req.getNode().getId(),
                         req.getNode().getMetadata().getFieldsOrDefault(ENVOY_VERSION_METADATA, Value.newBuilder().setStringValue("").build()).getStringValue(),
                         lastWordOf(req.getTypeUrl()),
                         req.getVersionInfo(),
                         req.getResponseNonce());
            }
        });

        final var sc = new StreamContext(this.configFlow.toFlowable(BackpressureStrategy.LATEST), this.configVersion);
        var actStr = this.activeStreams.incrementAndGet();
        log.info("New stream opened. Number of active streams = {}", actStr);

        return Flowable.combineLatest(sc.getConfigFlow(),
                                      request,
                                      sc.requestsForRetry.toFlowable(BackpressureStrategy.LATEST),
                                      (cfg,
                                       req,
                                       rty) ->
                                      {
                                          final String sender = sc.setSender(req.getNode().getId()).getSender();

                                          if (rty.isPresent() && sc.getCurrentRetry().setIfChanged(rty) && cfg.isPresent())
                                          {
                                              log.debug("{}: Retry after configuration change.", sender);
                                              // Always cease unconditionally old alarm before applying the new
                                              // configuration. Alarm will be re-raised if the error persists.
                                              this.badConfigurationAlarm.cease(sender);
                                              return this.processRequestRetry(rty.get(), cfg.get(), sc);
                                          }

                                          if (sc.getCurrentConfig().setIfChanged(cfg))
                                          {
                                              log.debug("{}: New configuration received: {}", sender, sc.getCurrentConfig().get());
                                              // Always cease unconditionally old alarm before applying the new
                                              // configuration. Alarm will be re-raised if the error persists.
                                              this.badConfigurationAlarm.cease(sender);

                                              final DiscoveryRequest pendingRequestCds = sc.getPendingRequestCds().get();

                                              if (pendingRequestCds != null)
                                              {
                                                  log.debug("{}: Pushing CDS request for retry:\n{}", sender, pendingRequestCds);
                                                  sc.getRequestsForRetry().toSerialized().onNext(Optional.of(pendingRequestCds));

                                                  final DiscoveryRequest pendingRequestEds = sc.getPendingRequestEds().get();

                                                  if (pendingRequestEds != null)
                                                  {
                                                      log.debug("{}: Pushing EDS request for retry:\n{}", sender, pendingRequestEds);
                                                      sc.getRequestsForRetry().toSerialized().onNext(Optional.of(pendingRequestEds));
                                                  }

                                                  return Optional.<DiscoveryResponse>empty();
                                              }
                                              final DiscoveryRequest pendingRequestEds = sc.getPendingRequestEds().get();

                                              if (pendingRequestEds != null)
                                              {
                                                  log.debug("{}: Pushing EDS request for retry:\n{}", sender, pendingRequestEds);
                                                  sc.getRequestsForRetry().toSerialized().onNext(Optional.of(pendingRequestEds));
                                                  return Optional.<DiscoveryResponse>empty();
                                              }
                                          }

                                          if (cfg.isEmpty() && sc.getContexts().get(req.getTypeUrl()) == null)
                                          {
                                              // cfg has not yet been initialized, so store any incoming request from envoy to
                                              // reply when configuration is available
                                              return this.sendInitRequestForRetry(sc, req, sender);
                                          }

                                          if (cfg.isPresent() && sc.getCurrentRequest().setIfChanged(req))
                                          {
                                              return this.processRequest(req, cfg.get(), sc);
                                          }

                                          log.info("{}: Empty discovery response is returned for request ({} (version {}, nonce {})",
                                                   req.getNode().getId(),
                                                   lastWordOf(req.getTypeUrl()),
                                                   req.getVersionInfo(),
                                                   req.getResponseNonce());

                                          return Optional.<DiscoveryResponse>empty();
                                      })
                       .filter(Optional::isPresent)
                       .map(Optional::get)
                       .doOnSubscribe(s -> log.info("Service: Subscribing."))
                       .doOnNext(resp ->
                       {

                           log.debug("{}: Sending response ({}, version {}):\n{}",
                                     resp.getControlPlane().getIdentifier(),
                                     lastWordOf(resp.getTypeUrl()),
                                     resp.getVersionInfo(),
                                     resp);

                       })
                       .doOnCancel(() ->
                       {
                           this.badConfigurationAlarm.cease(sc.getSender());
                           var remActStr = this.activeStreams.decrementAndGet();
                           log.info("Service: {} Cancelling. Remaining streams={}", sc.getSender(), remActStr);
                           this.envoyDisconnections.onNext(sc.sender);
                       })
                       .doOnError(e ->
                       {
                           this.badConfigurationAlarm.cease(sc.getSender());
                           log.error("Service: Error processing request: {}", log.isDebugEnabled() ? e : e.toString());
                           var remActStr = this.activeStreams.decrementAndGet();
                           log.info("Remaining streams={}", remActStr);
                       })
                       .doOnComplete(() ->
                       {
                           this.badConfigurationAlarm.cease(sc.getSender());
                           log.info("Service: Completed.");
                       });
    }

    /**
     * @return
     * 
     */
    private Optional<DiscoveryResponse> sendInitRequestForRetry(StreamContext sc,
                                                                DiscoveryRequest req,
                                                                String sender)
    {
        var context = sc.getContexts().get(req.getTypeUrl());

        if (log.isDebugEnabled())
        {
            log.debug("{}: Current state: context={}, versionOffset={}, request: {} (version {}, nonce {})\n{}",
                      sender,
                      context,
                      this.versionOffset,
                      lastWordOf(req.getTypeUrl()),
                      req.getVersionInfo(),
                      req.getResponseNonce(),
                      req);
        }
        else
        {
            log.info("{}: Current state: context={}, versionOffset={}, request: {} (version {}, nonce {})",
                     sender,
                     context,
                     this.versionOffset,
                     lastWordOf(req.getTypeUrl()),
                     req.getVersionInfo(),
                     req.getResponseNonce());
        }

        if (log.isInfoEnabled())
            log.info("{}: Initial request for type {}", sender, lastWordOf(req.getTypeUrl()));

        context = new Context(this.versionOffset);
        context.getCurrentNonce().set(String.valueOf(this.versionOffset));
        context.getLastAckVersion().set(String.valueOf(this.versionOffset));

        sc.getContexts().put(req.getTypeUrl(), context);

        log.info("{}: New {} initial request, store as pending and wait for initial configuration.", sender, req.getTypeUrl());
        this.storeAsPendingRequest(req, sc);

        return Optional.<DiscoveryResponse>empty();

    }

    private Optional<DiscoveryResponse> buildDiscoveryResponse(final DiscoveryRequest request,
                                                               final Config config,
                                                               final Context context) throws ProtocolException
    {
        /**
         * Check if the envoy that sends the discovery request has the same version as
         * the manager. If not, ProtocolException is thrown and the configuration stream
         * closes.
         */
        var workerAppVersion = request.getNode()
                                      .getMetadata()
                                      .getFieldsOrDefault(ENVOY_VERSION_METADATA, Value.newBuilder().setStringValue("").build())
                                      .getStringValue();
        var workerId = request.getNode().getId();
        if (appVersionOptional.isPresent())
        {
            var managerAppVersion = appVersionOptional.get();
            if (!managerAppVersion.equals(workerAppVersion))
            {
                throw new ProtocolException(String.format(PROTOCOL_EXCEPTION_MESSAGE, workerId, workerAppVersion, managerAppVersion));
            }
        }
        else
        {
            if (workerAppVersion != null && !workerAppVersion.isEmpty())
            {
                throw new ProtocolException(String.format(PROTOCOL_EXCEPTION_MESSAGE, workerId, workerAppVersion, "not-defined"));
            }
        }

        List<Any> resources = buildResources(request, config);

        // Build a response to the discovery request:
        var discoveryResp = DiscoveryResponse.newBuilder() //
                                             .setNonce(context.getCurrentNonce().incrementAndGet().toString()) //
                                             .setVersionInfo(config.getVersion().toString()) //
                                             .setTypeUrl(request.getTypeUrl()) //
                                             .setControlPlane(ControlPlane.newBuilder().setIdentifier(request.getNode().getId()).build());
        // ...and add all generated resources:
        for (var resource : resources)
        {
            discoveryResp.addResources(resource);
        }

        log.debug("Returning DiscoveryResponse for {} with version {}", request.getTypeUrl(), discoveryResp.getVersionInfo());
        return Optional.of(discoveryResp.build());
    }

    /*
     * returns the resources needed to respond to a request for the given typeUrl
     * with the given configuration as input
     */
    private List<Any> buildResources(final DiscoveryRequest request,
                                     final Config config)
    {
        if (request.getTypeUrl().equals(CdsHelper.TYPE_URL))
            return config.getConfig().getCdsResources();

        else if (request.getTypeUrl().equals(EdsHelper.TYPE_URL))
            return config.getConfig().getEdsResources();

        else if (request.getTypeUrl().equals(LdsHelper.TYPE_URL))
            return config.getConfig().getLdsResources();

        else if (request.getTypeUrl().equals(RdsHelper.TYPE_URL))
            return config.getConfig().getRdsResources();

        else
        {
            log.error("Cannot handle request of type {}", request.getTypeUrl());
            return new ArrayList<>();
        }

    }

    private synchronized Optional<DiscoveryResponse> processRequest(final DiscoveryRequest request,
                                                                    final Config config,
                                                                    final StreamContext sc) throws ProtocolException, JsonProcessingException
    {
        final String sender = sc.getSender();
        Context context = sc.getContexts().get(request.getTypeUrl());

        if (log.isDebugEnabled())
        {
            log.debug("{}: Current state: context={}, versionOffset={}, request: {} (version {}, nonce {})\n{}",
                      sender,
                      context,
                      this.versionOffset,
                      lastWordOf(request.getTypeUrl()),
                      request.getVersionInfo(),
                      request.getResponseNonce(),
                      request);
        }
        else
        {
            log.info("{}: Current state: context={}, versionOffset={}, request: {} (version {}, nonce {})",
                     sender,
                     context,
                     this.versionOffset,
                     lastWordOf(request.getTypeUrl()),
                     request.getVersionInfo(),
                     request.getResponseNonce());
        }

        final String typeUrl = request.getTypeUrl();
        if (context == null)
        {
            if (log.isInfoEnabled())
                log.info("{}: Initial request for type {}", sender, lastWordOf(request.getTypeUrl()));

            final Long version = config.getVersion().get();

            context = new Context(sc.getCfgVersion().offset());
            context.getCurrentNonce().set(String.valueOf(version - 1));
            context.getLastAckVersion().set(String.valueOf(version - 1));

            sc.getContexts().put(typeUrl, context);

            // Listener and Route responses have to be delayed until CDS ACK is received
            // i.e. "cluster warming" in envoy needs to be completed first
            if (typeUrl.equals(LdsHelper.TYPE_URL) || typeUrl.equals(RdsHelper.TYPE_URL))
            {

                final var cdsContext = sc.getContexts().get(CdsHelper.TYPE_URL);
                if (cdsContext == null)
                {
                    log.info("{}: New {} initial request, store as pending and wait for initial CDS sequence.", sender, typeUrl);
                    this.storeAsPendingRequest(request, sc);
                    return Optional.<DiscoveryResponse>empty();
                }

                if (cdsContext.lastAckVersion.delta(config.getVersion().toString()) != 0)
                {
                    log.info("{}: New {} initial request, store as pending and wait for CDS ACK.", sender, typeUrl);
                    this.storeAsPendingRequest(request, sc);
                    return Optional.<DiscoveryResponse>empty();
                }
            }

            return this.buildDiscoveryResponse(request, config, context);
        }

        final String reqVersion = request.getVersionInfo();
        final String respNonce = request.getResponseNonce();

        // Is the request older than what we have already sent?
        if (context.getCurrentNonce().delta(respNonce) > this.versionOffset)
        {
            if (log.isDebugEnabled())
                log.debug("{}: Dropping stale request:\n{}", sender, request);
            else
                log.debug("{}: Dropping stale request.", sender);

            return Optional.<DiscoveryResponse>empty();
        }

        if (context.getLastAckVersion().delta(reqVersion) < this.versionOffset) // ACK
        {
            context.getLastAckVersion().set(reqVersion);

            // Request with a version which is up to date
            if (config.getVersion().delta(reqVersion) <= this.versionOffset) // config-version is up-to-date (why <= and not == ?)
            {
                // CDS ack?
                if (request.getTypeUrl().equals(CdsHelper.TYPE_URL))
                {
                    final DiscoveryRequest pendingRequestLds = sc.getPendingRequestLds().get();
                    final DiscoveryRequest pendingRequestRds = sc.getPendingRequestRds().get();

                    if (pendingRequestLds != null)
                    {
                        // CDS ACK, trigger processing of pending LDS request
                        log.debug("{}: Pushing LDS request for retry:\n{}", sender, pendingRequestLds);
                        sc.getRequestsForRetry().toSerialized().onNext(Optional.of(pendingRequestLds));
                    }
                    if (pendingRequestRds != null)
                    {
                        // CDS ACK, trigger processing of pending RDS request
                        log.debug("{}: Pushing RDS request for retry:\n{}", sender, pendingRequestRds);
                        sc.getRequestsForRetry().toSerialized().onNext(Optional.of(pendingRequestRds));
                    }
                }

                // All acks
                this.badConfigurationAlarm.cease(sender);

                if (log.isInfoEnabled())
                    log.info("{}: {} (version {}) ACK: Waiting for next configuration change...",
                             sender,
                             lastWordOf(request.getTypeUrl()),
                             request.getVersionInfo());

                this.storeAsPendingRequest(request, sc);
                return Optional.<DiscoveryResponse>empty();
            }
            else // New Request, xDS is not yet updated
            {
                // Park follow-up LDS request until CDS has been ack'ed
                if (sc.getContexts().get(LdsHelper.TYPE_URL) != null && request.getTypeUrl().equals(LdsHelper.TYPE_URL))
                {
                    log.info("{}: New LDS request, store as pending and wait for CDS ACK.", sender);
                    this.storeAsPendingRequest(request, sc);
                    return Optional.<DiscoveryResponse>empty();
                }

                // Park follow-up-RDS request until CDS has been ack'ed
                if (sc.getContexts().get(RdsHelper.TYPE_URL) != null && request.getTypeUrl().equals(RdsHelper.TYPE_URL))
                {
                    log.info("{}: New RDS request, store as pending and wait for CDS ACK.", sender);
                    this.storeAsPendingRequest(request, sc);
                    return Optional.<DiscoveryResponse>empty();
                }

                // Reply to CDS and EDS in all cases (initial request and follow-ups), and to
                // LDS and RDS for initial requests only
                // = all cases that don't have to wait:
                return this.buildDiscoveryResponse(request, config, context);
            }

        }
        else if (context.getLastAckVersion().delta(reqVersion) <= this.versionOffset)
        {
            log.info("{}: Subsequent ACK will be ignored.", sender);
            return Optional.<DiscoveryResponse>empty();
        }
        else // NACK
        {
            final Status status = request.getErrorDetail();
            final String reqType = lastWordOf(request.getTypeUrl());

            String details = "Unspecified error";

            if (status != null && status.getCode() != Code.OK_VALUE)
            {
                log.info("{}: {} NACK: code={}, error={}", sender, reqType, status.getCode(), status.getMessage());
                String errorMsg = status.getMessage();
                if (errorMsg.length() > EnvoyAdsLogic.MAX_ALARM_ERROR_MESSAGE)
                {
                    errorMsg = errorMsg.substring(0, Math.min(EnvoyAdsLogic.MAX_ALARM_ERROR_MESSAGE, errorMsg.length())) + "...";
                }
                details = new StringBuilder().append("request-type: '")
                                             .append(reqType)
                                             .append("', status-code: '")
                                             .append(status.getCode())
                                             .append("', error: '")
                                             .append(errorMsg) // limit to 80 characters
                                             .append("'")
                                             .toString();
            }

            this.badConfigurationAlarm.raiseMajor(sender, details);

            log.info("{}: {} (version {}, nonce {}) NACK: Waiting for next configuration change...",
                     sender,
                     reqType,
                     request.getVersionInfo(),
                     request.getResponseNonce());
            this.storeAsPendingRequest(request, sc);
            context.getLastAckVersion().set(Integer.parseInt(reqVersion) - 1 + "");
            return Optional.<DiscoveryResponse>empty();
        }
    }

    /**
     * Give a text with words separated by dots, return the last word. This is
     * typically used to get the subsystem to be configured from a typeUrl, for
     * example: TypeUrl: type.googleapis.com/envoy.api.v2.Cluster Returns: Cluster
     *
     * @param text
     * @return last word of text
     */
    private String lastWordOf(final String text)
    {
        return text.substring(text.lastIndexOf('.') + 1);
    }

    private synchronized Optional<DiscoveryResponse> processRequestRetry(final DiscoveryRequest request,
                                                                         final Config config,
                                                                         final StreamContext sc) throws ProtocolException
    {
        final String sender = sc.getSender();
        final Context context = sc.getContexts().get(request.getTypeUrl());

        log.debug("{}: Current state: context={}, request: {} (version {}, nonce {})\n{}",
                  sender,
                  context,
                  lastWordOf(request.getTypeUrl()),
                  request.getVersionInfo(),
                  request.getResponseNonce(),
                  request);

        // Is the request older than what we have already sent?
        if ((context.getLastAckVersion().delta(config.getVersion().get().toString()) > this.versionOffset))
        {
            if (log.isDebugEnabled())
                log.debug("{}: Dropping stale request:\n{}", sender, request);
            else
                log.debug("{}: Dropping stale request.", sender);

            return Optional.<DiscoveryResponse>empty();
        }

        return this.buildDiscoveryResponse(request, config, context);
    }

    private void storeAsPendingRequest(final DiscoveryRequest request,
                                       final StreamContext sc)
    {
        final String sender = sc.getSender();

        if (request.getTypeUrl().equals(CdsHelper.TYPE_URL))
        {
            sc.getPendingRequestCds()
              .setIfChanged(request,
                            sender + (log.isDebugEnabled() ? ": More recent CDS request received:\n{}\nSkipping stale request:\n{}"
                                                           : ": More recent CDS request received. Skipping stale request."));
        }
        else if (request.getTypeUrl().equals(EdsHelper.TYPE_URL))
        {
            sc.getPendingRequestEds()
              .setIfChanged(request,
                            sender + (log.isDebugEnabled() ? ": More recent EDS request received:\n{}\nSkipping stale request:\n{}"
                                                           : ": More recent EDS request received. Skipping stale request."));
        }
        else if (request.getTypeUrl().equals(LdsHelper.TYPE_URL))
        {
            sc.getPendingRequestLds()
              .setIfChanged(request,
                            sender + (log.isDebugEnabled() ? ": More recent LDS request received:\n{}\nSkipping stale request:\n{}"
                                                           : ": More recent LDS request received. Skipping stale request."));
        }
        else if (request.getTypeUrl().equals(RdsHelper.TYPE_URL))
        {
            sc.getPendingRequestRds()
              .setIfChanged(request,
                            sender + (log.isDebugEnabled() ? ": More recent RDS request received:\n{}\nSkipping stale request:\n{}"
                                                           : ": More recent RDS request received. Skipping stale request."));
        }
    }
}
