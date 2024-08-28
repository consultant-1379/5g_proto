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
 * Created on: Jan 24, 2019
 *     Author: eedstl
 */

package com.ericsson.sc.nrf.r17;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cnal.common.CertificateObserver.Secret;
import com.ericsson.cnal.common.NrfCertificateInfo;
import com.ericsson.cnal.nrf.r17.NrfAdapter;
import com.ericsson.cnal.nrf.r17.NrfAdapter.RequestContext;
import com.ericsson.cnal.nrf.r17.NrfAdapter.Result;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.custom.SearchResult;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFProfile;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFService;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.ServiceName;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.PatchItem;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.PatchOperation;
import com.ericsson.sc.common.alarm.AlarmHandler.Alarm;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.common.IfCountProvider;
import com.ericsson.utilities.common.IfNamedListItem;
import com.ericsson.utilities.common.Rdn;
import com.ericsson.utilities.common.Triplet;
import com.ericsson.utilities.exceptions.BadConfigurationException;
import com.ericsson.utilities.exceptions.Utils;
import com.ericsson.utilities.http.Url;
import com.ericsson.utilities.json.Json;
import com.ericsson.utilities.logger.LogThrottler;
import com.ericsson.utilities.metrics.MetricRegister;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

/**
 * Representation of an NRF.
 */
public class Nrf
{
    public static class Configuration implements IfNamedListItem
    {
        private final Rdn rdn;
        private final Integer priority;
        private final Integer requestNumRetries;
        private final Integer requestTimeout;
        private final NFProfile nfProfile;
        private final List<Url> urls;
        private final String srcSbiNfPeerInfo;
        private final String userAgent;

        public Configuration(final Rdn rdn,
                             final Integer priority,
                             final Integer requestNumRetries,
                             final Integer requestTimeout,
                             final List<Url> urls,
                             final NFProfile nfProfile,
                             final String srcSbiNfPeerInfo,
                             final String userAgent)
        {
            this.rdn = rdn;
            this.priority = priority;
            this.requestNumRetries = requestNumRetries;
            this.requestTimeout = requestTimeout;
            this.nfProfile = nfProfile;
            this.urls = urls;
            this.srcSbiNfPeerInfo = srcSbiNfPeerInfo;
            this.userAgent = userAgent;
        }

        public String getName()
        {
            return this.rdn.toString();
        }

        public NFProfile getNfProfile()
        {
            return this.nfProfile;
        }

        public Integer getPriority()
        {
            return this.priority;
        }

        public Rdn getRdn()
        {
            return this.rdn;
        }

        public Integer getRequestNumRetries()
        {
            return this.requestNumRetries;
        }

        public Integer getRequestTimeout()
        {
            return this.requestTimeout;
        }

        public String getSrcSbiNfPeerInfo()
        {
            return this.srcSbiNfPeerInfo;
        }

        public List<Url> getUrls()
        {
            return this.urls;
        }

        public String getUserAgent()
        {
            return this.userAgent;
        }

        @Override
        public String toString()
        {
            return new StringBuilder().append("{").append("rdn=").append(this.rdn.toString()).append("}").toString();
        }
    }

    public static class FlowController
    {
        private static final int DEFAULT_REQUEST_NUM_RETRIES_MAX = 0;
        private static final int DEFAULT_REQUEST_TIMEOUT_MILLIS = 1000;

        private int requestNumRetriesMax;
        private int requestTimeoutMillis;
        private long beginOfAttemptMillis;
        private long beginOfFirstAttemptMillis;

        private int requestNumRetries;

        public FlowController()
        {
            this.requestNumRetriesMax = DEFAULT_REQUEST_NUM_RETRIES_MAX;
            this.requestTimeoutMillis = DEFAULT_REQUEST_TIMEOUT_MILLIS;
            this.beginOfAttemptMillis = System.currentTimeMillis();

            this.requestNumRetries = this.requestNumRetriesMax;
        }

        /**
         * Method markBeginOfFirstAttempt() must have been called prior to this method.
         * 
         * @return The point in time[ms] of the begin of the first attempt.
         */
        public synchronized long getBeginOfFirstAttemptMillis()
        {
            return this.beginOfFirstAttemptMillis;
        }

        /**
         * Method markBeginOfAttempt() must have been called prior to this method.
         * 
         * Calculates the effective time to wait before the next attempt. The effective
         * time to wait is the result of the difference of the request timeout and the
         * actual duration of this attempt. If the result is negative, zero is returned.
         * 
         * @return The time[ms] to wait before the next attempt.
         */
        public synchronized long getDelayBeforeNextAttemptMillis()
        {
            long delay = Math.max(0, this.requestTimeoutMillis - (System.currentTimeMillis() - this.beginOfAttemptMillis));
            log.debug("delayBeforeNextAttemptMillis={}", delay);
            return delay;
        }

        public synchronized Integer getRequestNumRetriesMax()
        {
            log.debug("requestNumRetriesMax={}", this.requestNumRetriesMax);
            return this.requestNumRetriesMax;
        }

        public synchronized Integer getRequestTimeoutMillis()
        {
            log.debug("requestTimeoutMillis={}", this.requestTimeoutMillis);
            return this.requestTimeoutMillis;
        }

        /**
         * Stores the point in time of the begin of this attempt. Must be called prior
         * to method getDelayBeforeNextAttemptMillis().
         */
        public synchronized FlowController markBeginOfAttempt()
        {
            this.beginOfAttemptMillis = System.currentTimeMillis();
            return this;
        }

        /**
         * Stores the point in time of the first attempt. Must be called prior to method
         * getBeginOfFirstAttemptMillis().
         */
        public synchronized FlowController markBeginOfFirstAttempt()
        {
            this.beginOfFirstAttemptMillis = System.currentTimeMillis();
            return this;
        }

        /**
         * @return Decremented value of requestNumRetries.
         */
        public int requestNumRetriesDecrement()
        {
            return --this.requestNumRetries;
        }

        public void requestNumRetriesReset()
        {
            this.requestNumRetries = this.requestNumRetriesMax;
        }

        public synchronized FlowController setRequestNumRetriesMax(Integer requestNumRetriesMax)
        {
            log.debug("requestNumRetriesMax={}", requestNumRetriesMax);
            this.requestNumRetriesMax = requestNumRetriesMax;
            return this;
        }

        public synchronized FlowController setRequestTimeoutMillis(Integer requestTimeoutMillis)
        {
            log.debug("requestTimeoutMillis={}", requestTimeoutMillis);
            this.requestTimeoutMillis = requestTimeoutMillis;
            return this;
        }
    }

    public static class Pool
    {
        public static class Configuration
        {
            final boolean registrationRequired;
            final boolean nrfPoolIteraterResetRequired;
            final List<Nrf.Configuration> configs;

            public Configuration(final boolean registrationRequired,
                                 final boolean nrfPoolIteraterResetRequired,
                                 final List<Nrf.Configuration> configs)
            {
                this.registrationRequired = registrationRequired;
                this.nrfPoolIteraterResetRequired = nrfPoolIteraterResetRequired;
                this.configs = configs;
            }

            public Configuration(final boolean registrationRequired,
                                 final List<Nrf.Configuration> configs)
            {
                this.registrationRequired = registrationRequired;
                this.nrfPoolIteraterResetRequired = true;
                this.configs = configs;
            }

            public List<Nrf.Configuration> getConfigs()
            {
                return this.configs;
            }

            public boolean isNrfPoolIteratorResetRequired()
            {
                return this.nrfPoolIteraterResetRequired;
            }

            public boolean isRegistrationRequired()
            {
                return this.registrationRequired;
            }

            @Override
            public String toString()
            {
                final StringBuilder b = new StringBuilder().append("{").append("registrationRequired=").append(this.registrationRequired).append(",configs=[");
                final AtomicBoolean first = new AtomicBoolean(true);

                this.configs.forEach(config ->
                {
                    if (!first.get())
                        b.append(",");

                    first.set(false);

                    b.append(config.toString());
                });

                b.append("]}");

                return b.toString();
            }
        }

        private static class Selector
        {
            public static class Session
            {
                private final Long id;
                private final Nrf data;

                public Session(final Long id,
                               final Nrf data)
                {
                    this.id = id;
                    this.data = data;
                }

                public Nrf getData()
                {
                    return this.data;
                }

                public Long getId()
                {
                    return this.id;
                }
            }

            private static final io.prometheus.client.Counter ccNrfGroupFailoverTotal = MetricRegister.singleton()
                                                                                                      .register(io.prometheus.client.Counter.build()
                                                                                                                                            .namespace("nrf")
                                                                                                                                            .name("failovers_total")
                                                                                                                                            .labelNames("service",
                                                                                                                                                        "nf",
                                                                                                                                                        "nf_instance",
                                                                                                                                                        "nrf_group",
                                                                                                                                                        "from",
                                                                                                                                                        "to",
                                                                                                                                                        "cause")
                                                                                                                                            .help("Number of failovers between NRF instances within an NRF group")
                                                                                                                                            .register());

            private static void stepCcNrfGroupFailoverTotal(final String service,
                                                            final String indexToCauseInLog,
                                                            final Rdn prev,
                                                            final Rdn curr)
            {
                final List<String> labelValues = new ArrayList<>();
                labelValues.add(service);
                labelValues.addAll(MetricRegister.rdnToLabelValues(prev)); // nf, nf_instance, nrf_group, from
                labelValues.add(curr != null ? curr.value() : "end-of-pool"); // to
                labelValues.add("Refer to index in log: " + indexToCauseInLog); // cause

                ccNrfGroupFailoverTotal.labels(labelValues.toArray(new String[0])).inc();
            }

            private final Object poolLock = new Object();
            private final SortedMap<String, Nrf> pool;
            private final LogThrottler logThrottler;

            private Long sessionCnt;
            private Iterator<Nrf> it;
            private Nrf cur;
            private Nrf end;

            public Selector(final SortedMap<String, Nrf> pool)
            {
                this.pool = pool;
                this.logThrottler = new LogThrottler();

                this.sessionCnt = 0l;
                this.it = null;
                this.cur = Nrf.Null;
                this.end = Nrf.Null;

            }

            public void commit()
            {
                synchronized (this.poolLock)
                {
                    this.end = this.cur;
                    log.debug("Nrf.Pool.Selector: session={}, cur={}, end={}", this.sessionCnt, this.cur, this.end);
                }
            }

            public Session get()
            {
                synchronized (this.poolLock)
                {
                    log.debug("Nrf.Pool.Selector: session={}, cur={}, end={}", this.sessionCnt, this.cur, this.end);
                    return new Session(this.sessionCnt, this.cur);
                }
            }

            public boolean next(final Throwable e,
                                final Session s,
                                final String service)
            {
                synchronized (this.poolLock)
                {
                    final boolean loggingIsDue = this.logThrottler.loggingIsDue(log.isDebugEnabled());

                    if (s != null && !s.getId().equals(this.sessionCnt)) // Do not iterate if session is stale.
                    {
                        log.debug("session={}, failoverRequest={}, ignoring stale request.", this.sessionCnt, s.getId());
                        return this.cur != this.end;
                    }

                    this.sessionCnt++; // Start the next session.

                    final Nrf prev = this.cur;

                    if (!this.it.hasNext())
                        this.it = this.pool.values().iterator();

                    this.cur = this.it.hasNext() ? this.it.next() : Nrf.Null;

                    final boolean hasNext = this.cur != this.end;

                    if (loggingIsDue)
                        log.info("Nrf.Pool.Selector: session={}, hasNext={}, cur={}, end={}", this.sessionCnt, hasNext, this.cur, this.end);

                    final String cause = Utils.toString(e, log.isDebugEnabled());
                    final String index = "#-" + String.format("%02d", Math.abs(cause.hashCode() % 20) + 1) + "-#";

                    if (hasNext)
                    {
                        if (loggingIsDue)
                            log.warn("Error updating '{}', failing-over to next in NRF-pool: '{}'. Cause: '{}: {}'",
                                     prev.getRdn(),
                                     this.cur.getRdn(),
                                     index,
                                     cause);

                        stepCcNrfGroupFailoverTotal(service, index, prev.getRdn(), this.cur.getRdn());
                    }
                    else if (prev != Nrf.Null)
                    {
                        if (loggingIsDue)
                            log.warn("Error updating '{}', end of NRF-pool encountered. Cause: '{}: {}'", prev.getRdn(), index, cause);

                        stepCcNrfGroupFailoverTotal(service, index, prev.getRdn(), null);
                    }
                    else
                    {
                        if (loggingIsDue)
                            log.error("Encountered empty NRF-pool. Cause: '{}: {}'", index, cause);
                    }

                    return hasNext;
                }
            }

            public Selector reset()
            {
                synchronized (this.poolLock)
                {
                    this.sessionCnt++;
                    this.it = this.pool.values().iterator();
                    this.cur = this.it.hasNext() ? this.it.next() : Nrf.Null;
                    this.end = this.cur;
                    log.debug("Nrf.Pool.Selector: cur={}, end={}", this.cur, this.end);
                }

                return this;
            }
        }

        /**
         * Check configurations passed for equality. Return true if they are equal,
         * otherwise false.
         * 
         * Two configurations are considered equal if 1. The FQDNs are equal 2. The URIs
         * are the same 3. The NFProfiles are the same.
         * 
         * Priority is not considered intentionally. Changing the priority of an active
         * NRF should not yield a re-registration.
         * 
         * @param lhs The left-hand-side of the equation lhs == rhs.
         * @param rhs The right-hand-side of the equation lhs == rhs.
         * @return true if both configurations are equal, otherwise false.
         */
        private static boolean isEqual(final Nrf.Configuration lhs,
                                       final Nrf.Configuration rhs)
        {
            if (lhs == rhs)
                return true;

            if (rhs == null)
                return false;

            if (!lhs.getRdn().toString().equals(rhs.getRdn().toString()) || !com.ericsson.utilities.common.Utils.isEqual(lhs.getUrls(), rhs.getUrls()))
            {
                log.debug("isEqual=false, lhs.rdn='{}', rhs.rdn='{}', lhs.nrf='{}', rhs.nrf='{}'", lhs.rdn, rhs.rdn, lhs.getUrls(), rhs.getUrls());
                return false;
            }

            boolean isEqual = false;

            try
            {
                isEqual = Json.isEqual(lhs.getNfProfile(), rhs.getNfProfile());
            }
            catch (final IOException e)
            {
                log.error("Failed to compare NFProfiles. Cause: {}", Utils.toString(e, log.isDebugEnabled()));
            }

            return isEqual;
        }

        private final UUID nfInstanceId;
        private final NrfGroupUnavailableAlarmHandler alarmNrfGroupUnavailable;
        private final IfCountProvider loadMeter;
        private final Flowable<Secret> secrets;
        private final Flowable<NrfCertificateInfo> nrfExtCertInfo;
        private final Rdn rdn;
        private final Subject<Optional<Configuration>> config;
        private final SortedMap<String, Nrf> pool;
        private final Selector selector;
        private final Queue<Optional<Configuration>> pendingConfigurations;

        private Configuration prev = null;
        private Disposable updater = null;

        public Pool(final UUID nfInstanceId,
                    final Alarm.Context alarmCtx,
                    final IfCountProvider loadMeter,
                    final Flowable<Secret> secrets,
                    final Rdn rdn)
        {
            this.nfInstanceId = nfInstanceId;
            this.alarmNrfGroupUnavailable = new NrfGroupUnavailableAlarmHandler(alarmCtx, rdn.toString(false));
            this.rdn = rdn;
            this.loadMeter = loadMeter;
            this.secrets = secrets;
            this.nrfExtCertInfo = null;
            this.config = BehaviorSubject.createDefault(Optional.<Configuration>empty());
            this.pool = new TreeMap<>();
            this.selector = new Selector(this.pool).reset();
            this.pendingConfigurations = new ConcurrentLinkedQueue<>();
        }

        public Pool(final UUID nfInstanceId,
                    final Alarm.Context alarmCtx,
                    final IfCountProvider loadMeter,
                    final Flowable<Secret> secrets,
                    final Flowable<NrfCertificateInfo> nrfExtCertInfo,
                    final Rdn rdn)
        {
            this.nfInstanceId = nfInstanceId;
            this.alarmNrfGroupUnavailable = new NrfGroupUnavailableAlarmHandler(alarmCtx, rdn.toString(false));
            this.rdn = rdn;
            this.loadMeter = loadMeter;
            this.secrets = secrets;
            this.nrfExtCertInfo = nrfExtCertInfo;
            this.config = BehaviorSubject.createDefault(Optional.<Configuration>empty());
            this.pool = new TreeMap<>();
            this.selector = new Selector(this.pool).reset();
            this.pendingConfigurations = new ConcurrentLinkedQueue<>();
        }

        public UUID getNfInstanceId()
        {
            return this.nfInstanceId;
        }

        public Rdn getRdn()
        {
            return this.rdn;
        }

        public Single<Result<SearchResult>> nfInstancesSearch(final RequestContext context)
        {
            final AtomicReference<Selector.Session> session = new AtomicReference<>();

            return Single.fromCallable(this.selector::get)//
                         .doOnSuccess(session::set)
                         .flatMap(s -> s.getData().nfInstancesSearch(context))
                         .filter(r ->
                         {
                             log.debug("problem={}, status={}, body={}", r.hasProblem(), r.getStatusCode(), r.getBodyAsString());

                             boolean success = !r.hasProblem() || r.getStatusCode() < 500;

                             if (success)
                             {
                                 this.selector.commit();
                             }
                             else
                             {
                                 final RuntimeException e = new RuntimeException("Server reported error: '"
                                                                                 + HttpResponseStatus.valueOf(r.getStatusCode()).reasonPhrase() + ": '"
                                                                                 + r.getBodyAsString() + "'.");

                                 if (!this.selector.next(e, session.get(), ServiceName.NNRF_DISC))
                                 {
                                     this.alarmNrfGroupUnavailable.raise(e);
                                     success = true;
                                 }
                             }

                             return success;
                         })
                         .switchIfEmpty(Single.just(1).flatMap(i -> this.nfInstancesSearch(context)))
                         .retryWhen(errors -> errors.flatMap(e ->
                         {
                             if (!this.selector.next(e, session.get(), ServiceName.NNRF_DISC))
                             {
                                 this.alarmNrfGroupUnavailable.raise(e);
                                 return Flowable.error(e);
                             }

                             return Flowable.just(1);
                         }));
        }

        public void publish(final Optional<Configuration> optional)
        {
            this.config.toSerialized().onNext(optional);
        }

        public Completable start()
        {
            return Completable.fromAction(() ->
            {
                if (this.updater == null)
                {
                    this.updater = Flowable.combineLatest(this.config.toFlowable(BackpressureStrategy.LATEST),
                                                          Single.just(1).repeatWhen(handler -> handler.delay(1, TimeUnit.SECONDS)),
                                                          (config,
                                                           tick) ->
                                                          {
                                                              this.pendingConfigurations.add(config);
                                                              return this.update().subscribe(() ->
                                                              {
                                                              }, t -> log.error("Error updating NRF. Cause: {}", Utils.toString(t, log.isDebugEnabled())));
                                                          })
                                           .ignoreElements()
                                           .doOnSubscribe(d -> log.info("Started updating NRF."))
                                           .doOnDispose(() -> log.info("Stopped updating NRF."))
                                           .subscribe(() -> log.info("Stopped updating NRF."),
                                                      t -> log.error("Stopped updating NRF. Cause: {}", Utils.toString(t, log.isDebugEnabled())));
                }
            });
        }

        public Completable stop()
        {
            return Completable.fromAction(() ->
            {
                if (this.updater != null)
                {
                    this.updater.dispose();
                    this.updater = null;
                }
            });
        }

        public Single<Result<SearchResult>> subscriptionsCreate(final RequestContext context)
        {
            final AtomicReference<Selector.Session> session = new AtomicReference<>();

            return Single.fromCallable(this.selector::get)//
                         .doOnSuccess(session::set)
                         .flatMap(s -> s.getData().nfInstancesSearch(context))
                         .filter(r ->
                         {
                             log.debug("problem={}, status={}, body={}", r.hasProblem(), r.getStatusCode(), r.getBodyAsString());

                             boolean success = !r.hasProblem() || r.getStatusCode() < 500;

                             if (success)
                             {
                                 this.selector.commit();
                             }
                             else
                             {
                                 final RuntimeException e = new RuntimeException("Server reported error: '"
                                                                                 + HttpResponseStatus.valueOf(r.getStatusCode()).reasonPhrase() + ": '"
                                                                                 + r.getBodyAsString() + "'.");

                                 if (!this.selector.next(e, session.get(), ServiceName.NNRF_DISC))
                                 {
                                     this.alarmNrfGroupUnavailable.raise(e);
                                     success = true;
                                 }
                             }

                             return success;
                         })
                         .switchIfEmpty(Single.just(1).flatMap(i -> this.nfInstancesSearch(context)))
                         .retryWhen(errors -> errors.flatMap(e ->
                         {
                             if (!this.selector.next(e, session.get(), ServiceName.NNRF_DISC))
                             {
                                 this.alarmNrfGroupUnavailable.raise(e);
                                 return Flowable.error(e);
                             }

                             return Flowable.just(1);
                         }));
        }

        private Set<Nrf> performHouseKeeping(final SortedMap<String, Nrf> sortedPool,
                                             final List<Nrf.Configuration> curr)
        {
            // Perform house-keeping:

            // Add all new NRFs to our pool.

            final Set<String> configuredKeysUnsorted = new HashSet<>();
            final Set<String> configuredKeysSorted = new HashSet<>();
            final Map<String, Nrf> unsortedPool = new HashMap<>();
            this.pool.values().forEach(v -> unsortedPool.put(v.getRdn().toString(), v));

            curr.forEach(config ->
            {
                if (config.getUrls() != null && !config.getUrls().isEmpty()) // Consider only registrations having NRF endpoints.
                {
                    final String keyUnsorted = config.getRdn().toString();
                    final String keySorted = String.format("%02d", config.getPriority()) + "," + keyUnsorted; // 0 <= priority < 100.

                    configuredKeysUnsorted.add(keyUnsorted);
                    configuredKeysSorted.add(keySorted);

                    if (!sortedPool.containsKey(keySorted))
                    {
                        log.debug("newKey={}", keySorted);
                        sortedPool.put(keySorted,
                                       unsortedPool.containsKey(keyUnsorted) ? unsortedPool.get(keyUnsorted)
                                                                             : this.nrfExtCertInfo != null ? new Nrf(this.nfInstanceId,
                                                                                                                     this.loadMeter,
                                                                                                                     this.secrets,
                                                                                                                     this.nrfExtCertInfo,
                                                                                                                     config.getRdn())
                                                                                                           : new Nrf(this.nfInstanceId,
                                                                                                                     this.loadMeter,
                                                                                                                     this.secrets,
                                                                                                                     config.getRdn()));
                    }
                    else
                    {
                        log.debug("oldKey={}", keySorted);
                    }

                    sortedPool.get(keySorted)
                              .setSbiNfPeerInfo(config.getSrcSbiNfPeerInfo())
                              .setUserAgent(config.getUserAgent())
                              .getFlowController()
                              .setRequestNumRetriesMax(config.getRequestNumRetries())
                              .setRequestTimeoutMillis(config.getRequestTimeout())
                              .requestNumRetriesReset();
                }
            });

            // Clean-up the pool, i.e. remove all keys that are not configured anymore.

            final Set<String> sortedKeysNotInConfig = new HashSet<>();

            for (Entry<String, Nrf> entry : sortedPool.entrySet())
            {
                if (!configuredKeysSorted.contains(entry.getKey()))
                {
                    log.debug("deletedKey={}", entry.getKey());
                    sortedKeysNotInConfig.add(entry.getKey());
                }
            }

            sortedPool.keySet().removeIf(sortedKeysNotInConfig::contains);

            // Return all NRFs that are not configured anymore. These are needed for the
            // cleanup of metrics for the deleted NRFs.

            final Set<Nrf> unconfiguredNrfs = new HashSet<>();

            for (Entry<String, Nrf> entry : unsortedPool.entrySet())
            {
                if (!configuredKeysUnsorted.contains(entry.getKey()))
                    unconfiguredNrfs.add(entry.getValue());
            }

            return unconfiguredNrfs;
        }

        private synchronized Nrf prepare()
        {
            log.debug("Preparing update. Pending configurations: {}.", this.pendingConfigurations.size());

            final boolean doNotRegisterAtNrf = Boolean.parseBoolean(EnvVars.get("DO_NOT_REGISTER_AT_NRF"));
            final Optional<Configuration> optional = this.pendingConfigurations.peek();
            final Configuration curr = optional != null && optional.isPresent() ? optional.get() : null;

            boolean handlerCouldBeSeized = true;
            Nrf deleted = Nrf.Null;

            if (this.prev != curr)
            {
                if (curr != null) // New config
                {
                    try
                    {
                        log.info("Configuration has been changed.");

                        // The sortedPool is the working-copy of the current pool and will be adjusted
                        // to the new configuration. It will be copied to the current pool in case a
                        // handler could be seized for deregistration and/or registration.

                        final SortedMap<String, Nrf> sortedPool = new TreeMap<>();
                        sortedPool.putAll(this.pool);

                        final Set<Nrf> unconfiguredNrfs = this.performHouseKeeping(sortedPool, curr.getConfigs());

                        // Perform synchronization regarding old/new active NRF:

                        final Nrf active = this.selector.get().getData();
                        final Nrf newActive = curr.isNrfPoolIteratorResetRequired() ? new Selector(sortedPool).reset().get().getData() : active;

                        if (!doNotRegisterAtNrf)
                        {
                            if (newActive == active)
                            {
                                if (active != Nrf.Null)
                                {
                                    // Check if there are changes in the configuration that have to be conveyed to
                                    // the currently active NRF.

                                    final Nrf.Configuration configPrev = com.ericsson.utilities.common.Utils.getByName(this.prev.getConfigs(),
                                                                                                                       active.getRdn().toString());
                                    final Nrf.Configuration configCurr = com.ericsson.utilities.common.Utils.getByName(curr.getConfigs(),
                                                                                                                       active.getRdn().toString());

                                    if (!isEqual(configCurr, configPrev))
                                    {
                                        if (curr.isRegistrationRequired())
                                        {
                                            handlerCouldBeSeized = newActive.seizeForNfInstanceRegister();
                                        }
                                        else if (this.prev.isRegistrationRequired())
                                        {
                                            handlerCouldBeSeized = active.seizeForNfInstanceDeregister();
                                            deleted = active;
                                        }
                                    }
                                    else
                                    {
                                        if (this.prev.isRegistrationRequired() && !curr.isRegistrationRequired())
                                        {
                                            handlerCouldBeSeized = active.seizeForNfInstanceDeregister();
                                            deleted = active;
                                        }
                                        else if (!this.prev.isRegistrationRequired() && curr.isRegistrationRequired())
                                        {
                                            handlerCouldBeSeized = newActive.seizeForNfInstanceRegister();
                                        }
                                    }
                                }
                            }
                            else
                            {
                                // Deregister from the currently active NRF and register at the new NRF that
                                // shall become the active one.

                                if (active != Nrf.Null && this.prev.isRegistrationRequired())
                                {
                                    handlerCouldBeSeized = active.seizeForNfInstanceDeregister();
                                    deleted = active;
                                }

                                if (newActive != Nrf.Null && curr.isRegistrationRequired())
                                    handlerCouldBeSeized = handlerCouldBeSeized && newActive.seizeForNfInstanceRegister();
                            }
                        }

                        // If a handler could be seized, apply the configuration change.

                        if (handlerCouldBeSeized)
                        {
                            // Bug_DND-31032
                            // The new configuration must always be applied. For the correction it has been
                            // considered that in the case of a discovery related configuration update
                            // (isNrfPoolIteratorResetRequired == false) the NRF groups and NRFs are never
                            // changed, i.e. only the NFProfile that is used for registration must be
                            // updated (no connections, NRF pools or the like).

                            curr.getConfigs().forEach(config ->
                            {
                                if (config.getUrls() != null && !config.getUrls().isEmpty()) // Consider only registrations having NRF endpoints.
                                {
                                    sortedPool.get(new StringBuilder(String.format("%02d", config.getPriority())).append(",")
                                                                                                                 .append(config.getRdn().toString())
                                                                                                                 .toString())
                                              .update(config, !curr.isNrfPoolIteratorResetRequired());
                                }
                            });

                            if (curr.isNrfPoolIteratorResetRequired())
                            {
                                unconfiguredNrfs.forEach(nrf ->
                                {
                                    MetricRegister.singleton().registerForRemoval(nrf.getRdn());
                                    nrf.close();
                                });

                                this.pool.clear();
                                this.pool.putAll(sortedPool);
                                this.selector.reset();
                            }
                        }
                    }
                    catch (final Exception e)
                    {
                        log.warn("Ignoring new configuration. Cause: {}", Utils.toString(e, log.isDebugEnabled()));
                    }
                }
                else // Config deleted
                {
                    log.info("Configuration has been deleted.");

                    deleted = this.selector.get().getData();

                    if (this.prev.isRegistrationRequired() && !doNotRegisterAtNrf)
                        handlerCouldBeSeized = deleted.seizeForNfInstanceDeregister();

                    if (handlerCouldBeSeized)
                    {
                        this.pool.values().forEach(Nrf::close);
                        this.pool.clear();
                        this.selector.reset();
                    }
                }
            }
            else if (curr != null) // No config change
            {
                log.debug("Configuration has not been changed.");

                if (curr.isRegistrationRequired() && !doNotRegisterAtNrf)
                    this.selector.get().getData().seizeForNfInstanceUpdate();
            }
            else
            {
                log.debug("Configuration does not exist.");
            }

            if (handlerCouldBeSeized)
            {
                this.pendingConfigurations.poll(); // Removes head of queue, no exception if empty.
                this.prev = curr;
            }

            log.debug("Preparing update done.");

            return deleted;
        }

        private synchronized Completable update()
        {
            return Completable.complete()
                              .doOnSubscribe(d -> log.debug("Updating."))
                              .andThen(Single.fromCallable(this::prepare)//
                                             .doOnSubscribe(d -> log.debug("Phase 1(2)."))
                                             .flatMap(Nrf::getHandler)
                                             .filter(r -> r != RESULT_NO_OPERATION)
                                             .switchIfEmpty(Maybe.empty())
                                             .doOnError(e -> log.error("Error updating NRF. Cause: {}", Utils.toString(e, log.isDebugEnabled())))
                                             .ignoreElement()
                                             .onErrorComplete()
                                             .doOnComplete(() -> log.debug("Phase 1(2) done.")))
                              .andThen(Single.fromCallable(this.selector::get)
                                             .doOnSubscribe(d -> log.debug("Phase 2(2)."))
                                             .flatMap(s -> s.getData().getHandler())
                                             .filter(r -> r != RESULT_NO_OPERATION)
                                             .switchIfEmpty(Maybe.empty())
                                             .doOnSuccess(r -> this.selector.commit())
//                                             .doOnSuccess(r -> this.alarmNrfGroupUnavailable.cease()) // Alarm will be auto-ceased after 60s
                                             .doOnError(e ->
                                             {
                                                 if (!this.selector.next(e, null, ServiceName.NNRF_NFM))
                                                     this.alarmNrfGroupUnavailable.raise(e);
                                             })
                                             .ignoreElement()
                                             .onErrorComplete()
                                             .doOnComplete(() -> log.debug("Phase 2(2) done.")))
                              .doOnComplete(() -> log.debug("Updating done."));
        }
    }

    private static class Connection implements Comparable<Connection>
    {
        public static class Pool
        {
            private static final io.prometheus.client.Counter ccNrfFailoverTotal = MetricRegister.singleton()
                                                                                                 .register(io.prometheus.client.Counter.build()
                                                                                                                                       .namespace("nrf")
                                                                                                                                       .name("endpoint_failovers_total")
                                                                                                                                       .labelNames("service",
                                                                                                                                                   "nf",
                                                                                                                                                   "nf_instance",
                                                                                                                                                   "nrf_group",
                                                                                                                                                   "nrf",
                                                                                                                                                   "from",
                                                                                                                                                   "to",
                                                                                                                                                   "cause")
                                                                                                                                       .help("Number of failovers between NRF internal endpoints")
                                                                                                                                       .register());

            private static void stepCcNrfFailoverTotal(final String service,
                                                       final String indexToCauseInLog,
                                                       final Rdn prev,
                                                       final Rdn curr)
            {
                final List<String> labelValues = new ArrayList<>();
                labelValues.add(service);
                labelValues.addAll(MetricRegister.rdnToLabelValues(prev)); // nf, nf_instance, nrf_group, nrf, from
                labelValues.add(curr != null ? curr.value() : "end-of-pool"); // to
                labelValues.add("Refer to index in log: " + indexToCauseInLog); // cause

                ccNrfFailoverTotal.labels(labelValues.toArray(new String[0])).inc();
            }

            private final Nrf parent;
            private final Object connectionsLock = new Object();
            private final LogThrottler logThrottler = new LogThrottler();

            private int cur = 0;
            private int end = 0;
            private List<Connection> connections = new ArrayList<>();

            public Pool(final Nrf parent)
            {
                this.parent = parent;
            }

            public void close()
            {
                synchronized (this.connectionsLock)
                {
                    this.connections.forEach(Connection::close);
                }
            }

            public void commit()
            {
                synchronized (this.connectionsLock)
                {
                    log.debug("Connection.Pool: cur={}, end={}", this.cur, this.end);
                    this.end = this.cur;
                    this.parent.getFlowController().requestNumRetriesReset();
                }
            }

            public Connection get()
            {
                synchronized (this.connectionsLock)
                {
                    log.debug("Connection.Pool: cur={}, end={}, connections.size={}", this.cur, this.end, this.connections.size());

                    if (this.connections.isEmpty())
                        throw new BadConfigurationException("Empty connection pool encountered, probably due to unresolved FQDN of the NRF.");

                    return this.connections.get(this.cur);
                }
            }

            public boolean next(final Throwable e,
                                final String service)
            {
                synchronized (this.connectionsLock)
                {
                    if (this.connections.isEmpty())
                        return false;

                    this.connections.get(this.cur).renew();

                    final boolean loggingIsDue = this.logThrottler.loggingIsDue(log.isDebugEnabled());

                    final int numRetries = this.parent.getFlowController().requestNumRetriesDecrement();

                    if (numRetries >= 0)
                    {
                        log.info("Retrying, remaining retries: {}", numRetries);
                        return true;
                    }

                    this.parent.getFlowController().requestNumRetriesReset();

                    final int prev = this.cur;

                    this.cur++;
                    this.cur %= this.connections.size();

                    log.debug("Connection.Pool: cur={}, end={}", this.cur, this.end);

                    final boolean hasNext = this.cur != this.end;

                    final String cause = Utils.toString(e, log.isDebugEnabled());
                    final String index = "#-" + String.format("%02d", Math.abs(cause.hashCode() % 20) + 1) + "-#";

                    if (hasNext)
                    {
                        if (loggingIsDue)
                            log.warn("Error connecting to '{}', failing-over to next in connection-pool: '{}'. Cause: '{}: {}'",
                                     this.connections.get(prev).getRdn(),
                                     this.connections.get(this.cur).getRdn(),
                                     index,
                                     cause);

                        stepCcNrfFailoverTotal(service, index, this.connections.get(prev).getRdn(), this.connections.get(this.cur).getRdn());
                    }
                    else if (prev < this.connections.size())
                    {
                        if (loggingIsDue)
                            log.warn("Error connecting to '{}', end of connection-pool encountered. Cause: '{}: {}'",
                                     this.connections.get(prev).getRdn(),
                                     index,
                                     cause);

                        stepCcNrfFailoverTotal(service, index, this.connections.get(prev).getRdn(), null);
                    }

                    return hasNext;
                }
            }

            public void update(final List<Url> urls)
            {
                final Set<String> uniqueUris = new TreeSet<>();
                final List<Connection> connections = new ArrayList<>();

                final List<Url> currUrls = this.connections.stream().map(Connection::getUrl).collect(Collectors.toList());

                if (currUrls.size() == urls.size() && currUrls.containsAll(urls))
                {
                    log.info("Connections all unchanged.");
                    return;
                }

                urls.forEach(url ->
                {
                    try
                    {
                        if (url.getUrl().getHost() == null || url.getUrl().getPort() < 0)
                            throw new BadConfigurationException("URL '{}': must contain all of host and port.", url.getUrl());

                        if (!uniqueUris.contains(url.toString()))
                        {
                            uniqueUris.add(url.toString());

                            Connection found = null;

                            for (Connection c : this.connections)
                            {
                                if (c.getUrl().equals(url))
                                {
                                    found = c;
                                    break;
                                }
                            }

                            if (found != null)
                            {
                                log.info("Reusing connection {}", found.getRdn());
                                connections.add(found);
                            }
                            else
                            {
                                final Connection c = new Connection(this.parent.getRdn(),
                                                                    url,
                                                                    this.parent.getNrfExtCertInfo() != null ? new NrfAdapter(this.parent.getRdn(),
                                                                                                                             url,
                                                                                                                             this.parent.getSecrets(),
                                                                                                                             this.parent.getNrfExtCertInfo())
                                                                                                            : new NrfAdapter(this.parent.getRdn(),
                                                                                                                             url,
                                                                                                                             this.parent.getSecrets()));
                                log.info("Adding new connection {}", c.getRdn());
                                connections.add(c);
                            }
                        }
                    }
                    catch (final BadConfigurationException e)
                    {
                        Exceptions.propagate(e);
                    }
                    catch (final Exception e)
                    {
                        throw new BadConfigurationException(e, "NRF '{}': {}", url.toString(), Utils.toString(e, log.isDebugEnabled()));
                    }
                });

                synchronized (this.connectionsLock)
                {
                    log.debug("Connection.Pool: numConnections: new={}, old={}", connections.size(), this.connections.size());

                    // Find all connections in this.connections that are not in connections and
                    // close them.

                    for (final Connection c : this.connections)
                    {
                        if (!connections.contains(c))
                        {
                            log.info("Closing the connection {}", c.getRdn());
                            c.close();
                        }
                    }

                    this.connections = connections; // Don't clear the old connections, they are still needed for deregistration.
                    this.cur = 0;
                    this.end = this.cur;
                }
            }
        }

        private final Rdn rdn;
        private final Url url;
        private final NrfAdapter adapter;

        public Connection(final Rdn rdn,
                          final Url url,
                          final NrfAdapter adapter)
        {
            this.rdn = rdn.add("endpoint", new StringBuilder(url.getAddr().toString()).toString());
            this.url = url;
            this.adapter = adapter;
        }

        public void close()
        {
            this.adapter.close();
        }

        @Override
        public int compareTo(final Connection rhs)
        {
            return this.url.toString().compareTo(rhs.url.toString());
        }

        @Override
        public boolean equals(final Object rhs)
        {
            if (this == rhs)
                return true;

            if (rhs == null)
                return false;

            if (this.getClass() != rhs.getClass())
                return false;

            return this.getUrl().toString().equals(((Connection) rhs).getUrl().toString());
        }

        public NrfAdapter get()
        {
            return this.adapter;
        }

        public Rdn getRdn()
        {
            return this.rdn;
        }

        public Url getUrl()
        {
            return this.url;
        }

        @Override
        public int hashCode()
        {
            return this.getUrl().toString().hashCode();
        }

        public void renew()
        {
            this.adapter.renew();
        }
    }

    private static final String HD_USER_AGENT = "user-agent";
    private static final int RESULT_NO_OPERATION = 9999;

    private static final Logger log = LoggerFactory.getLogger(Nrf.class);
    private static final Single<Integer> HandlerNull = Single.just(RESULT_NO_OPERATION).doOnSubscribe(d -> log.debug("No operation."));
    private static final Nrf Null = new Nrf(null, null, null, null, null);

    private final FlowController failureHandler;

    private final UUID nfInstanceId;
    private final IfCountProvider loadMeter;
    private final Flowable<Secret> secrets;
    private final Flowable<NrfCertificateInfo> nrfExtCertInfo;
    private final Rdn rdn;
    private final Connection.Pool connections;
    private Object nfProfileLock = new Object();

    private NFProfile nfProfile;
    private long lastSuccessfulUpdateInMillis;
    private long heartbeatTimeoutInMillis;
    private Single<Integer> handler;
    private boolean isSeized;
    private boolean isRegistered;
    private AtomicReference<String> sbiNfPeerInfo;
    private AtomicReference<String> userAgent;

    public Nrf(final UUID nfInstanceId,
               final IfCountProvider loadMeter,
               final Flowable<Secret> secrets,
               final Rdn rdn)
    {
        this.failureHandler = new FlowController();
        this.nfInstanceId = nfInstanceId;
        this.loadMeter = loadMeter;
        this.secrets = secrets;
        this.nrfExtCertInfo = null;
        this.rdn = rdn;
        this.connections = new Connection.Pool(this);

        this.nfProfile = new NFProfile();
        this.lastSuccessfulUpdateInMillis = System.currentTimeMillis();
        this.heartbeatTimeoutInMillis = 1000; // Default: 1 Hz
        this.handler = Nrf.HandlerNull;
        this.isSeized = false;
        this.isRegistered = false;
        this.sbiNfPeerInfo = new AtomicReference<>(null);
        this.userAgent = new AtomicReference<>(null);
    }

    public Nrf(final UUID nfInstanceId,
               final IfCountProvider loadMeter,
               final Flowable<Secret> secrets,
               final Flowable<NrfCertificateInfo> nrfExtCertInfo,
               final Rdn rdn)
    {
        this.failureHandler = new FlowController();
        this.nfInstanceId = nfInstanceId;
        this.loadMeter = loadMeter;
        this.secrets = secrets;
        this.nrfExtCertInfo = nrfExtCertInfo;
        this.rdn = rdn;
        this.connections = new Connection.Pool(this);

        this.nfProfile = new NFProfile();
        this.lastSuccessfulUpdateInMillis = System.currentTimeMillis();
        this.heartbeatTimeoutInMillis = 1000; // Default: 1 Hz
        this.handler = Nrf.HandlerNull;
        this.isSeized = false;
        this.isRegistered = false;
        this.sbiNfPeerInfo = new AtomicReference<>(null);
        this.userAgent = new AtomicReference<>(null);
    }

    public void close()
    {
        this.connections.close();
    }

    public FlowController getFlowController()
    {
        return this.failureHandler;
    }

    public synchronized Single<Integer> getHandler()
    {
        final Single<Integer> handler = this.handler;
        this.handler = Nrf.HandlerNull; // A handler can only be used once.

        log.debug("Returned: isNullHandler={}, nrf={}", handler == Nrf.HandlerNull, this);

        return handler;
    }

    public Rdn getRdn()
    {
        return this.rdn;
    }

    public Flowable<Secret> getSecrets()
    {
        return this.secrets;
    }

    public Flowable<NrfCertificateInfo> getNrfExtCertInfo()
    {
        return this.nrfExtCertInfo;
    }

    public synchronized boolean seizeForNfInstanceDeregister()
    {
        log.debug("nrf='{}'", this.rdn);
        return this.seize(this.nfInstanceDeregister());
    }

    public synchronized boolean seizeForNfInstanceRegister()
    {
        log.debug("nrf='{}'", this.rdn);
        return this.seize(this.nfInstanceRegister());
    }

    public synchronized boolean seizeForNfInstanceUpdate()
    {
        log.debug("nrf='{}'", this.rdn);
        return this.seize(this.isRegistered ? this.nfInstanceUpdate() : this.nfInstanceRegister());
    }

    public Nrf setSbiNfPeerInfo(final String srcSbiNfPeerInfo)
    {
        log.debug("srcSbiNfPeerInfo={}", srcSbiNfPeerInfo);

        // For the time being only the source fields shall be used. Field dstinst will
        // be set once an NF instance ID can be configured for an NRF.
        this.sbiNfPeerInfo.set(srcSbiNfPeerInfo);
        return this;
    }

    public Nrf setUserAgent(final String userAgent)
    {
        log.debug("userAgent={}", userAgent);

        this.userAgent.set(userAgent);
        return this;
    }

    public String toString()
    {
        return new StringBuilder().append("{")
                                  .append("this=")
                                  .append(System.identityHashCode(this))
                                  .append(", rdn=")
                                  .append(this.rdn)
                                  .append(", isRegistered=")
                                  .append(this.isRegistered)
                                  .append(", isSeized=")
                                  .append(this.isSeized)
                                  .append(", isNullHandler=")
                                  .append(this.handler == Nrf.HandlerNull)
                                  .append(", isNull=")
                                  .append(this == Nrf.Null)
                                  .append(", elapsedTime=")
                                  .append(System.currentTimeMillis() - this.lastSuccessfulUpdateInMillis)
                                  .append("}")
                                  .toString();
    }

    public synchronized void update(final Configuration config,
                                    final boolean updateProfileOnly)
    {
        if (!updateProfileOnly)
            this.connections.update(config.getUrls());

        this.setNfProfile(config.getNfProfile());
    }

    private NFProfile getNfProfile()
    {
        synchronized (this.nfProfileLock)
        {
            try
            {
                this.nfProfile = Json.patch(this.nfProfile, this.patchCreate(), NFProfile.class);
            }
            catch (Exception e)
            {
                log.error("Error patching NF-profile, skipping patch. Cause: {}", Utils.toString(e, log.isDebugEnabled()));
            }

            return this.nfProfile;
        }
    }

    private Single<Integer> nfInstanceDeregister()
    {
        return this.nfInstanceDeregisterCreate(RequestContext.of())//
                   .doOnSuccess(r -> this.release())
                   .doOnError(e -> this.release());
    }

    private Single<Integer> nfInstanceDeregisterCreate(final RequestContext context)
    {
        return Single.fromCallable(this.connections::get)
                     .subscribeOn(Schedulers.io())
                     .doOnSubscribe(d -> this.getFlowController().markBeginOfAttempt())
                     .flatMap(c -> c.get()
                                    .nfInstanceDeregister(context.setRequestTimeoutMillis(this.getFlowController().getRequestTimeoutMillis())
                                                                 .addHeader(HD_USER_AGENT, this.userAgent.get())
                                                                 .setSbiNfPeerInfo(this.sbiNfPeerInfo.get()),
                                                          this.nfInstanceId))
                     .doOnSuccess(r ->
                     {
                         this.connections.commit();
                         this.setRegistered(false);
                         this.heartbeatTimeoutInMillis = 1000; // Reset to initial value
                     })
                     .retryWhen(e -> e.flatMap(t -> Flowable.just(this.connections.next(t, ServiceName.NNRF_NFM))
                                                            .filter(hasNext -> hasNext)
                                                            .switchIfEmpty(Flowable.error(t))
                                                            .delay(this.getFlowController().getDelayBeforeNextAttemptMillis(), TimeUnit.MILLISECONDS)))
                     .doOnSubscribe(d -> log.info("Deregistering NF-instance '{}' from NRF '{}'.", this.nfInstanceId, this.rdn))
                     .map(Result::getStatusCode);
    }

    private Single<Integer> nfInstanceRegister()
    {
        return this.nfInstanceRegisterCreate(RequestContext.of())//
                   .doOnSuccess(r -> this.release())
                   .doOnError(e -> this.release());
    }

    private Single<Integer> nfInstanceRegisterCreate(final RequestContext context)
    {
        return Single.fromCallable(this.connections::get)
                     .subscribeOn(Schedulers.io())
                     .doOnSubscribe(d -> this.getFlowController().markBeginOfAttempt())
                     .flatMap(c -> c.get()
                                    .nfInstanceRegister(context.setRequestTimeoutMillis(this.getFlowController().getRequestTimeoutMillis())
                                                               .addHeader(HD_USER_AGENT, this.userAgent.get())
                                                               .setSbiNfPeerInfo(this.sbiNfPeerInfo.get()),
                                                        this.nfInstanceId,
                                                        this.getNfProfile()))
                     .doOnSuccess(r -> this.connections.commit())
                     .doOnError(e -> log.debug("doOnError: {}", Utils.toString(e, log.isDebugEnabled())))
                     .retryWhen(e -> e.flatMap(t -> Flowable.just(this.connections.next(t, ServiceName.NNRF_NFM))
                                                            .filter(hasNext -> hasNext)
                                                            .switchIfEmpty(Flowable.error(t))
                                                            .delay(this.getFlowController().getDelayBeforeNextAttemptMillis(), TimeUnit.MILLISECONDS)))
                     .doOnSubscribe(d ->
                     {
                         log.info("Registering NF-instance '{}' at NRF '{}'.", this.nfInstanceId, this.rdn);
                         this.getFlowController().markBeginOfFirstAttempt();
                     })
                     .doOnSuccess(r ->
                     {
                         if (r.getStatusCode() >= 200 && r.getStatusCode() < 300)
                         {
                             this.lastSuccessfulUpdateInMillis = this.getFlowController().getBeginOfFirstAttemptMillis();

                             final boolean ok = r.getBody() != null && r.getBody().getHeartBeatTimer() != null;

                             if (ok)
                                 this.heartbeatTimeoutInMillis = 1000L * Math.min(r.getBody().getHeartBeatTimer(), NrfAdapter.HTTP_KEEP_ALIVE_TIMEOUT_SECS - 1);

                             this.setRegistered(ok); // OK only if the heart-beat timer has been received. If not, re-register next
                                                     // time.
                         }
                         else
                         {
                             this.setRegistered(false);
                         }
                     })
                     .doOnError(e -> this.setRegistered(false))
                     .map(Result::getStatusCode);
    }

    private Single<Result<SearchResult>> nfInstancesSearch(final RequestContext context)
    {
        return this.nfInstancesSearchCreate(context)//
                   .doOnSuccess(r -> this.release())
                   .doOnError(e -> this.release());
    }

    private Single<Result<SearchResult>> nfInstancesSearchCreate(final RequestContext context)
    {
        return Single.fromCallable(this.connections::get)
                     .subscribeOn(Schedulers.io())
                     .doOnSubscribe(d -> this.getFlowController().markBeginOfAttempt())
                     .flatMap(c -> c.get()
                                    .nfInstancesSearch(context.setRequestTimeoutMillis(this.getFlowController().getRequestTimeoutMillis())
                                                              // Delegated discovery: forward user-agent received, otherwise use configured
                                                              // user-agent.
                                                              .addHeader(HD_USER_AGENT,
                                                                         context.getHeaders() == null
                                                                                        || context.getHeaders().get(HD_USER_AGENT) == null
                                                                                                                                           ? this.userAgent.get()
                                                                                                                                           : null)
                                                              .setSbiNfPeerInfo(this.sbiNfPeerInfo.get())))
                     .doOnSuccess(r -> this.connections.commit())
                     .doOnError(e -> log.debug("doOnError: {}", Utils.toString(e, log.isDebugEnabled())))
                     .retryWhen(e -> e.flatMap(t -> Flowable.just(this.connections.next(t, ServiceName.NNRF_DISC))
                                                            .filter(hasNext -> hasNext)
                                                            .switchIfEmpty(Flowable.error(t))
                                                            .delay(this.getFlowController().getDelayBeforeNextAttemptMillis(), TimeUnit.MILLISECONDS)))
                     .doOnSubscribe(d ->
                     {
                         log.debug("Searching NF-instances in NRF '{}'.", this.rdn);
                         this.getFlowController().markBeginOfFirstAttempt();
                     });
    }

    private Single<Integer> nfInstanceUpdate()
    {
        return Single.just(HttpResponseStatus.OK.code())
                     .filter(result -> System.currentTimeMillis() - this.lastSuccessfulUpdateInMillis < this.heartbeatTimeoutInMillis)
                     .switchIfEmpty(this.nfInstanceUpdateCreate(RequestContext.of())
                                        .filter(result -> result >= 200 && result < 300)
                                        .switchIfEmpty(this.nfInstanceRegisterCreate(RequestContext.of())))
                     .doOnSuccess(r -> this.release())
                     .doOnError(e -> this.release());
    }

    private Single<Integer> nfInstanceUpdateCreate(final RequestContext context)
    {
        return Single.fromCallable(this.connections::get)
                     .subscribeOn(Schedulers.io())
                     .doOnSubscribe(d -> this.getFlowController().markBeginOfAttempt())
                     .flatMap(c -> c.get()
                                    .nfInstanceUpdate(context.setRequestTimeoutMillis(this.getFlowController().getRequestTimeoutMillis())
                                                             .addHeader(HD_USER_AGENT, this.userAgent.get())
                                                             .setSbiNfPeerInfo(this.sbiNfPeerInfo.get()),
                                                      this.nfInstanceId,
                                                      this.patchCreate()))
                     .doOnSuccess(r -> this.connections.commit())
                     .doOnError(e -> log.debug("doOnError: {}", Utils.toString(e, log.isDebugEnabled())))
                     .retryWhen(e -> e.flatMap(t -> Flowable.just(this.connections.next(t, ServiceName.NNRF_NFM))
                                                            .filter(hasNext -> hasNext)
                                                            .switchIfEmpty(Flowable.error(t))
                                                            .delay(this.getFlowController().getDelayBeforeNextAttemptMillis(), TimeUnit.MILLISECONDS)))
                     .doOnSubscribe(d ->
                     {
                         log.debug("Updating NF-instance '{}' in NRF '{}'.", this.nfInstanceId, this.rdn);
                         this.getFlowController().markBeginOfFirstAttempt();
                     })
                     .doOnSuccess(r ->
                     {
                         if (r.getStatusCode() >= 200 && r.getStatusCode() < 300)
                         {
                             this.lastSuccessfulUpdateInMillis = this.getFlowController().getBeginOfFirstAttemptMillis();

                             if (r.getBody() != null && r.getBody().getHeartBeatTimer() != null)
                                 this.heartbeatTimeoutInMillis = 1000L * Math.min(r.getBody().getHeartBeatTimer(), NrfAdapter.HTTP_KEEP_ALIVE_TIMEOUT_SECS - 1);
                         }
                         else
                         {
                             this.setRegistered(false);
                         }
                     })
                     .doOnError(e -> this.setRegistered(false))
                     .map(Result::getStatusCode);
    }

    private PatchItem[] patchCreate()
    {
        final List<PatchItem> items = new ArrayList<>();

        synchronized (this.nfProfileLock)
        {
            PatchItem patch;

            patch = new PatchItem();
            patch.setOp(PatchOperation.REPLACE);
            patch.setPath("/nfStatus");
            patch.setValue(this.nfProfile.getNfStatus()); // Always take the NF status of the current NF profile.
            items.add(patch);

            // For the time being, use the same load value for overall-load and
            // per-service-load.
            final Integer loadInPercent = (int) Math.round(this.loadMeter != null ? this.loadMeter.getCount() : 0);

            patch = new PatchItem();
            patch.setOp(PatchOperation.ADD);
            patch.setPath("/load");
            patch.setValue(loadInPercent); // 0 <= value[%] <= 100.
            items.add(patch);

            final String loadTimeStamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

            patch = new PatchItem();
            patch.setOp(PatchOperation.ADD);
            patch.setPath("/loadTimeStamp");
            patch.setValue(loadTimeStamp);
            items.add(patch);

            for (int i = 0; this.nfProfile.getNfServices() != null && i < this.nfProfile.getNfServices().size(); ++i)
            {
                patch = new PatchItem();
                patch.setOp(PatchOperation.ADD);
                patch.setPath("/nfServices/" + i + "/load");
                patch.setValue(loadInPercent); // 0 <= value[%] <= 100
                items.add(patch);

                patch = new PatchItem();
                patch.setOp(PatchOperation.ADD);
                patch.setPath("/nfServices/" + i + "/loadTimeStamp");
                patch.setValue(loadTimeStamp);
                items.add(patch);
            }

            if (this.nfProfile.getNfServiceList() != null)
            {
                for (Entry<String, NFService> entry : this.nfProfile.getNfServiceList().entrySet())
                {
                    patch = new PatchItem();
                    patch.setOp(PatchOperation.ADD);
                    patch.setPath("/nfServiceList/" + entry.getKey() + "/load");
                    patch.setValue(loadInPercent); // 0 <= value[%] <= 100
                    items.add(patch);

                    patch = new PatchItem();
                    patch.setOp(PatchOperation.ADD);
                    patch.setPath("/nfServiceList/" + entry.getKey() + "/loadTimeStamp");
                    patch.setValue(loadTimeStamp);
                    items.add(patch);
                }
            }
        }

        return items.toArray(new PatchItem[0]);
    }

    private synchronized void release()
    {
        log.debug("nrf='{}'", this.rdn);

        if (this == Nrf.Null)
            return;

        this.isSeized = false;
        log.debug("nrf='{}'", this);
    }

    private boolean seize(final Single<Integer> handler)
    {
        if (this == Nrf.Null)
            return this.isSeized;

        if (this.isSeized)
            return this.isSeized;

        this.isSeized = true;
        this.handler = handler;
        log.debug("nrf='{}'", this);

        return this.isSeized;
    }

    private void setNfProfile(final NFProfile nfProfile)
    {
        synchronized (this.nfProfileLock)
        {
            this.nfProfile = nfProfile;
        }
    }

    private synchronized void setRegistered(final boolean isRegistered)
    {
        this.isRegistered = isRegistered;
    }
}
