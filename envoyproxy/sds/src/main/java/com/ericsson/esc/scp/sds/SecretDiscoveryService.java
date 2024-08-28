/**
 * COPYRIGHT ERICSSON GMBH 2020java.enet.ProtocolException: Expected HTTP 101 response but was '400 Bad Request'

 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jan 10, 2020
 *     Author: eaoknkr
 */

package com.ericsson.esc.scp.sds;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.utilities.common.AtomicRef;
import com.ericsson.utilities.file.CertificateWatch.MonitoredCertificates;
import com.google.protobuf.Any;
import com.google.rpc.Code;

import io.envoyproxy.envoy.config.core.v3.ControlPlane;
import io.envoyproxy.envoy.config.core.v3.DataSource;
import io.envoyproxy.envoy.extensions.transport_sockets.tls.v3.CertificateValidationContext;
import io.envoyproxy.envoy.extensions.transport_sockets.tls.v3.Secret;
import io.envoyproxy.envoy.extensions.transport_sockets.tls.v3.TlsCertificate;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryRequest;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;
import io.envoyproxy.envoy.service.secret.v3.RxSecretDiscoveryServiceGrpc.SecretDiscoveryServiceImplBase;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.subjects.BehaviorSubject;

/**
 * 
 */
public class SecretDiscoveryService extends SecretDiscoveryServiceImplBase
{
    private static final Logger log = LoggerFactory.getLogger(SecretDiscoveryService.class);

    protected final Flowable<MonitoredCertificates> certificateWatcher;
    protected final Counter secretsVersion;
    protected final AtomicInteger activeStreams;
    protected static final Long VERSION_OFFSET = 0L;

    /**
     * @param events
     */
    public SecretDiscoveryService(Flowable<MonitoredCertificates> certificateWatcher)
    {
        this.certificateWatcher = certificateWatcher.replay(1).autoConnect();
        this.secretsVersion = new Counter(VERSION_OFFSET, VERSION_OFFSET);
        this.activeStreams = new AtomicInteger();
    }

    @Override
    public Flowable<DiscoveryResponse> streamSecrets(Flowable<DiscoveryRequest> request)
    {
        final var streamContext = new StreamContext(this.certificateWatcher, this.secretsVersion);
        log.debug("New stream opened. Total number of active streams = {}", this.activeStreams.incrementAndGet());
        return Flowable.combineLatest(streamContext.getSecretsFlow(),
                                      request,
                                      streamContext.requestsForRetry.toFlowable(BackpressureStrategy.LATEST),
                                      (secret,
                                       req,
                                       rty) ->
                                      {
                                          if (!req.getResourceNamesList().isEmpty())
                                          {
                                              var secretName = req.getResourceNames(0);
                                              log.debug("Processing secret for {}", secretName);
                                              return process(req, secret, streamContext, rty, secretName);
                                          }
                                          return Optional.<DiscoveryResponse>empty();
                                      })
                       .filter(Optional::isPresent)
                       .map(Optional::get)
                       .doOnSubscribe(s -> log.info("Secret Discovery Service: Subscribing."))
                       .doOnNext(resp -> log.debug("{}: Sending response \nVersionInfo:{}\nNonce:{}\n",
                                                   resp.getControlPlane().getIdentifier(),
                                                   resp.getVersionInfo(),
                                                   resp.getNonce()))
                       .doOnCancel(() ->
                       {
                           log.info("Secret Discovery Service: Cancelling.");
                           log.debug("Remaining streams={}", this.activeStreams.decrementAndGet());
                       })
                       .doOnError(e ->
                       {
                           log.error("Secret Discovery Service: Error processing request ", e);
                           log.debug("Remaining streams={}", this.activeStreams.decrementAndGet());
                       })
                       .doOnComplete(() -> log.info("Secret Discovery Service: Completed."));
    }

    protected Optional<DiscoveryResponse> process(final DiscoveryRequest req,
                                                  final SecretWrap secret,
                                                  final StreamContext sc,
                                                  final Optional<DiscoveryRequest> rty,
                                                  final String secretName)
    {
        final String sender = sc.setSender(req.getNode().getId()).getSender();
        if (rty.isPresent() && sc.getCurrentRetry().setIfChanged(rty))
        {
            log.debug("{}: Retry after secret {} change.", sender, secretName);
            return this.processRequestRetry(rty.get(), secret, sc, secretName);
        }

        if (sc.getCurrentSecret().setIfChanged(secret))
        {
            log.debug("{}: New secret received: {}", sender, sc.getCurrentSecret().get());
            final DiscoveryRequest pendingRequest = sc.getPendingRequest().getAndSet(null);
            if (pendingRequest != null)
            {
                log.debug("{}: Pushing request for {} for retry", sender, secretName);
                sc.getRequestsForRetry().toSerialized().onNext(Optional.of(pendingRequest));
                return Optional.<DiscoveryResponse>empty();
            }
        }
        if (sc.getCurrentRequest().setIfChanged(req))
        {
            log.debug("{}: New request for {} received.", sender, secretName);
            return this.processRequest(req, secret, sc, secretName);
        }
        return Optional.<DiscoveryResponse>empty();
    }

    protected Any buildEnvoySecret(final SecretWrap secret,
                                   final String secretName)
    {
        log.debug("Building response for secret={}", secretName);
        if (secret.getCertificateEvent().isAsymmetric(secretName))
        {
            var certificateEvent = secret.getCertificateEvent().getAsymmetricKey(secretName);
            return Any.pack(Secret.newBuilder()
                                  .setName(secretName) //
                                  .setTlsCertificate(TlsCertificate.newBuilder()
                                                                   .setPrivateKey(DataSource.newBuilder().setInlineString(certificateEvent.getKey()).build())
                                                                   .setCertificateChain(DataSource.newBuilder()
                                                                                                  .setInlineString(certificateEvent.getCertificate())
                                                                                                  .build())
                                                                   .build())
                                  .build());
        }
        else if (secret.getCertificateEvent().isCa(secretName))
        {
            var certificateEvent = secret.getCertificateEvent().getCaCert(secretName);
            return Any.pack(Secret.newBuilder()
                                  .setName(secretName)
                                  .setValidationContext(CertificateValidationContext.newBuilder()
                                                                                    .setTrustedCa(DataSource.newBuilder()
                                                                                                            .setInlineString(certificateEvent.getCa())
                                                                                                            .build())
                                                                                    .build())
                                  .build());
        }
        log.warn("No info found for secret={}", secretName);
        return Any.pack(Secret.getDefaultInstance()); // certificates missing
    }

    private Optional<DiscoveryResponse> buildDiscoveryResponse(final DiscoveryRequest request,
                                                               final SecretWrap secret,
                                                               final Context context,
                                                               final String secretName)
    {
        final var constructedSecret = buildEnvoySecret(secret, secretName);
        // Build a response to the discovery request:
        final var discoveryResp = DiscoveryResponse.newBuilder()
                                                   .setNonce(context.getCurrentNonce().incrementAndGet().toString())
                                                   .setVersionInfo(secret.getVersion().toString())
                                                   .setTypeUrl(request.getTypeUrl())
                                                   .setControlPlane(ControlPlane.newBuilder().setIdentifier(request.getNode().getId()).build())
                                                   .addResources(constructedSecret)
                                                   .build();
        return Optional.of(discoveryResp);
    }

    private Optional<DiscoveryResponse> processRequest(final DiscoveryRequest request,
                                                       final SecretWrap secret,
                                                       final StreamContext sc,
                                                       final String secretName)
    {
        final var sender = sc.getSender();
        log.debug("{}: Current state: secret={}, secretName={}, context={}", sender, secret, secretName, sc.getContext());
        if (sc.getContext() == null)
        {
            log.debug("{}: Initial request for secret={}, secretName={}", sender, secret, secretName);

            final var version = secret.getVersion().get();
            sc.setContext(new Context(secret.getVersion().offset));
            sc.getContext().getCurrentNonce().set(String.valueOf(version - 1));
            sc.getContext().getLastAckVersion().set(String.valueOf(version - 1));
            return this.buildDiscoveryResponse(request, secret, sc.getContext(), secretName);
        }

        final var reqVersion = request.getVersionInfo();
        final var respNonce = request.getResponseNonce();
        // Is the request older than what we have already sent?
        if (sc.getContext().getCurrentNonce().delta(respNonce) > VERSION_OFFSET)
        {
            log.info("{}: Dropping stale request for secret={}", sender, secretName);
            return Optional.<DiscoveryResponse>empty();
        }
        if (sc.getContext().getLastAckVersion().delta(reqVersion) < VERSION_OFFSET) // ACK
        {
            sc.getContext().getLastAckVersion().set(reqVersion);
            if (secret.getVersion().delta(reqVersion).equals(VERSION_OFFSET)) // secret-version is up-to-date
            {
                log.info("{}: secret={}, ACK: Waiting for next secret change...", sender, secretName);
                sc.getPendingRequest()
                  .setIfChanged(request,
                                sender + (log.isDebugEnabled() ? ": More recent request received:\n{}\nSkipping stale request:\n{}"
                                                               : ": More recent request received. Skipping stale request."));
                return Optional.<DiscoveryResponse>empty();
            }
        }
        else // NACK
        {
            final var status = request.getErrorDetail();
            if (status == null)
                log.info("{}: secret={}, NACK: Waiting for next secret change...", sender, secretName);
            else if (status.getCode() != Code.OK_VALUE)
                log.info("{}: secret={}, NACK: code={}, error={}. Waiting for next secret change...",
                         sender,
                         secretName,
                         status.getCode(),
                         status.getMessage());

            sc.getPendingRequest()
              .setIfChanged(request,
                            sender + (log.isDebugEnabled() ? ": More recent request received:\n{}\nSkipping stale request:\n{}"
                                                           : ": More recent request received. Skipping stale request."));
            return Optional.<DiscoveryResponse>empty();
        }
        return this.buildDiscoveryResponse(request, secret, sc.getContext(), secretName);
    }

    private synchronized Optional<DiscoveryResponse> processRequestRetry(final DiscoveryRequest request,
                                                                         final SecretWrap secret,
                                                                         final StreamContext sc,
                                                                         final String secretName)
    {
        final String sender = sc.getSender();
        log.debug("{}: Current state: secret={}, secretName={}, context={}", sender, secret, secretName, sc.getContext());
        // Is the request older than what we have already sent?
        if ((sc.getContext().getLastAckVersion().delta(secret.getVersion().get().toString()) > VERSION_OFFSET))
        {
            log.info("{}: Dropping stale request for secret={}", sender, secret);
            return Optional.<DiscoveryResponse>empty();
        }
        return this.buildDiscoveryResponse(request, secret, sc.getContext(), secretName);
    }

    private class Context
    {
        private final Counter currentNonce;
        private final Counter lastAckVersion;

        public Context(Long versionOffset)
        {
            this.currentNonce = new Counter(versionOffset, versionOffset);
            this.lastAckVersion = new Counter(versionOffset - 1L, versionOffset);
        }

        public final Counter getCurrentNonce()
        {
            return this.currentNonce;
        }

        public final Counter getLastAckVersion()
        {
            return this.lastAckVersion;
        }

        public final String toString()
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

    protected class StreamContext
    {
        private final AtomicRef<SecretWrap> currentSecret = new AtomicRef<>(new SecretWrap());
        private final AtomicRef<DiscoveryRequest> currentRequest = new AtomicRef<>(null);
        protected final BehaviorSubject<Optional<DiscoveryRequest>> requestsForRetry = BehaviorSubject.createDefault(Optional.<DiscoveryRequest>empty());
        private final AtomicRef<Optional<DiscoveryRequest>> currentRetry = new AtomicRef<>(Optional.<DiscoveryRequest>empty());

        protected final Counter secretVersion;

        private String sender = null;

        private final Flowable<SecretWrap> secretsFlow;

        private final AtomicRef<DiscoveryRequest> pendingRequest = new AtomicRef<>(null);

        private Context context;

        public StreamContext(final Flowable<MonitoredCertificates> secretEventsFlow,
                             final Counter secretVersion)
        {
            Long offset = secretVersion.offset();
            this.secretVersion = new Counter(secretVersion.get(), offset);
            this.secretsFlow = secretEventsFlow.map(o -> new SecretWrap(o, this.secretVersion.incrementAndGet(), offset));
        }

        private Flowable<SecretWrap> getSecretsFlow()
        {
            return this.secretsFlow;
        }

        public Context getContext()
        {
            return this.context;
        }

        public void setContext(Context context)
        {
            this.context = context;
        }

        public AtomicRef<SecretWrap> getCurrentSecret()
        {
            return this.currentSecret;
        }

        public AtomicRef<DiscoveryRequest> getCurrentRequest()
        {
            return this.currentRequest;
        }

        public AtomicRef<Optional<DiscoveryRequest>> getCurrentRetry()
        {
            return this.currentRetry;
        }

        public AtomicRef<DiscoveryRequest> getPendingRequest()
        {
            return this.pendingRequest;
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

    protected class SecretWrap
    {
        private MonitoredCertificates certificateEvent;
        private final Counter version;

        public SecretWrap(MonitoredCertificates o,
                          Long version,
                          Long offset)
        {
            this.certificateEvent = o;
            this.version = new Counter(version, offset);
        }

        public SecretWrap()
        {
            this.version = new Counter(0L, 0L);
        }

        public final MonitoredCertificates getCertificateEvent()
        {
            return this.certificateEvent;
        }

        public final Counter getVersion()
        {
            return this.version;
        }

        public String toString()
        {
            return new StringBuilder().append("{")
                                      .append("Secret")
                                      .append(", version=")
                                      .append(this.version)
                                      .append(", certificateEvent=")
                                      .append(this.certificateEvent.toString())
                                      .append("}")
                                      .toString();
        }
    }

    protected class Counter
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
                log.error("Exception caught: {}", e.toString());
                this.count.set(this.offset);
            }
        }

        public String toString()
        {
            return String.valueOf(this.count);
        }
    }
}