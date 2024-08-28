/**
 * COPYRIGHT ERICSSON GMBH 2023
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: May 4, 2023
 *     Author: eedstl
 */

package com.ericsson.sc.common.alarm;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.fm.FmAlarmService;
import com.ericsson.sc.fm.model.fi.AdditionalInformation;
import com.ericsson.sc.fm.model.fi.FaultIndication;
import com.ericsson.sc.fm.model.fi.FaultIndication.FaultIndicationBuilder;
import com.ericsson.sc.fm.model.fi.FaultIndication.Severity;
import com.ericsson.utilities.exceptions.Utils;
import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

/**
 * Encapsulates the generic handling of an alarm (aka fault indication). In
 * order to avoid hanging alarms (raised but not ceased any more for some
 * reason), all alarms are raised with TTL and are updated regularly (raised
 * again) at an interval < TTL.
 * <p>
 * Input is coming via a queue (Subject) of fault indications.
 */
public class AlarmHandler implements IfAlarmHandler
{
    @JsonPropertyOrder({ "fault", "expirationTime", "isExpired", "nextDueTime", "isDue" })
    public static class Alarm
    {
        public static class Context implements IfAlarmPublisher
        {
            /**
             * @param alarmHandler
             * @param alarmPrefix  Examples: Scp, Sepp, ScpSlf, ScNlf, ...
             * @param serviceName  Examples: ericsson-scp, ericsson-sepp, ...
             */
            public static Context of(final IfAlarmHandler alarmHandler,
                                     final String alarmPrefix,
                                     final String serviceName)
            {
                return new Context(alarmHandler, alarmPrefix, serviceName);
            }

            final IfAlarmHandler alarmHandler;
            final String alarmPrefix;
            final String serviceName;
            final String serviceFunction; // Examples: scp-function, sepp-function, bsf-function

            private Context(final IfAlarmHandler alarmHandler,
                            final String alarmPrefix,
                            final String serviceName)
            {
                this.alarmHandler = alarmHandler;
                this.alarmPrefix = alarmPrefix;
                this.serviceName = serviceName;
                this.serviceFunction = serviceName.substring(serviceName.indexOf("-") + 1) + "-function";
            }

            /**
             * @return Examples: Scp, Sepp, ScpSlf, ScNlf, ...
             */
            public String getAlarmPrefix()
            {
                return this.alarmPrefix;
            }

            /**
             * @return Examples: scp-function, sepp-function, bsf-function
             */
            public String getServiceFunction()
            {
                return this.serviceFunction;
            }

            /**
             * @return Examples: ericsson-scp, ericsson-sepp, ...
             */
            public String getServiceName()
            {
                return this.serviceName;
            }

            @Override
            public void publish(Alarm alarm)
            {
                this.alarmHandler.publish(alarm);
            }

            @Override
            public void publish(Map<String, ? extends Collection<Alarm>> alarms)
            {
                this.alarmHandler.publish(alarms);
            }
        }

        /**
         * Alarms are raised with a time-to-live (ALARM_TTL_SECS) and have to be
         * re-raised before they expire. This is done periodically (period is
         * ALARM_UPDATE_INTERVAL_SECS) until the refresh expiration time (the time when
         * the alarm handler should stop refreshing the alarm so that it automatically
         * expires) has passed.
         */
        public static final long ALARM_TTL_SECS = 60;
        private static final long ALARM_UPDATE_INTERVAL_SECS = ALARM_TTL_SECS * 10 / 12;

        /**
         * Creates an alarm with severity CLEAR and immediate expiration. <br>
         * Used for ceasing.
         * 
         * @param faultName
         * @param serviceName
         * @param faultyResource
         * @return The newly created alarm.
         */
        public static Alarm of(final String faultName,
                               final String serviceName,
                               final String faultyResource)
        {
            return new Alarm(faultName, serviceName, faultyResource);
        }

        /**
         * Creates an alarm from the original alarm and with the given severity.<br>
         * Used for raising with changed severity or ceasing (if severity == CLEAR).
         * 
         * @param orig
         * @param severity
         * @return The newly created alarm.
         */
        public static Alarm of(final Alarm orig,
                               final Severity severity)
        {
            return new Alarm(orig, severity);
        }

        /**
         * Creates an alarm for the given parameters.<br>
         * Used for raising.
         * <p>
         * The alarm will be created with 60 seconds time-to-live. <br>
         * It will be refreshed by the alarm handler for expirationSec seconds.
         * 
         * @param faultName
         * @param serviceName
         * @param faultyResource
         * @param severity
         * @param description
         * @param expirationSecs
         * @param additionalInformation
         * @return The newly created alarm.
         */
        public static Alarm of(final String faultName,
                               final String serviceName,
                               final String faultyResource,
                               final FaultIndication.Severity severity,
                               final String description,
                               final Long expirationSecs,
                               final AdditionalInformation additionalInformation)
        {
            return new Alarm(faultName, serviceName, faultyResource, severity, description, expirationSecs, additionalInformation);
        }

        /**
         * Compose the alarm name from the parameters passed.
         * 
         * @param prefix    Service specific prefix for the alarm name. Examples: Scp,
         *                  Sepp, Bsf, ScpSlf, ScNlf, ...
         * @param faultName The fault's name. Example: NrfGroupUnabailable
         * @return The alarm name. Example: ScpSlfNrfGroupUnavailable
         */
        public static String toAlarmName(final String prefix,
                                         final String faultName)
        {
            return prefix + faultName;
        }

        /**
         * Combines the base fault description with the details passed.
         * 
         * @param base    The base fault description.
         * @param details The details.
         * @return The result of the operation.
         */
        public static String toDescription(String base,
                                           String details)
        {
            if (base == null)
                base = "";

            if (details == null)
                details = "";

            base = base.trim();
            details = details.trim();

            final StringBuilder b = new StringBuilder(base);

            b.append(base.endsWith(".") ? "" : ".");

            if (!details.isEmpty())
            {
                b.append(" Details: ").append(details);
                b.append(details.endsWith(".") ? "" : ".");
            }

            return b.toString();
        }

        @JsonIgnore
        private FaultIndication fault;

        @JsonProperty("expirationTime")
        private final Instant expirationTime;

        @JsonProperty("nextDueTime")
        private final AtomicReference<Instant> nextDueTime;

        @JsonIgnore
        private boolean isPublished = false;

        /**
         * Constructs an alarm with severity CLEAR and immediate expiration.<br>
         * Used for ceasing.
         * 
         * @param faultName
         * @param serviceName
         * @param faultyResource
         */
        private Alarm(final String faultName,
                      final String serviceName,
                      final String faultyResource)
        {
            this.fault = new FaultIndicationBuilder().withFaultName(faultName)
                                                     .withServiceName(serviceName)
                                                     .withFaultyResource(faultyResource)
                                                     .withSeverity(Severity.CLEAR)
                                                     .withExpiration(0L)
                                                     .build();

            this.expirationTime = Instant.now();
            this.nextDueTime = new AtomicReference<>(Instant.now().minusSeconds(1)); // isDue == true
        }

        private Alarm(final Alarm orig,
                      final Severity severity)
        {
            this.fault = new FaultIndicationBuilder(orig.getFault()).withSeverity(severity).build();

            this.expirationTime = Instant.now();
            this.nextDueTime = new AtomicReference<>(Instant.now().minusSeconds(1)); // isDue == true
        }

        private Alarm(final String faultName,
                      final String serviceName,
                      final String faultyResource,
                      final FaultIndication.Severity severity,
                      final String description,
                      final Long expirationSecs,
                      final AdditionalInformation additionalInformation)
        {
            this.fault = new FaultIndicationBuilder().withFaultName(faultName)
                                                     .withServiceName(serviceName)
                                                     .withFaultyResource(faultyResource)
                                                     .withSeverity(severity)
                                                     .withDescription(description.length() > 200 ? description.substring(0, 197) + "..." : description)
                                                     .withExpiration(ALARM_TTL_SECS)
                                                     .withAdditionalInformation(additionalInformation)
                                                     .build();

            this.expirationTime = Instant.now().plusSeconds(Math.max(0, expirationSecs));
            this.nextDueTime = new AtomicReference<>(Instant.now().minusSeconds(1)); // isDue == true
        }

        /**
         * This method must be called for an alarm that is being published to set the
         * time when the next update is due.
         * 
         * @return This alarm.
         */
        public Alarm advanceNextDueTime()
        {
            this.isPublished = true; // This method is called only when the alarm is published.
            this.nextDueTime.set(Instant.now().plusSeconds(ALARM_UPDATE_INTERVAL_SECS));
            return this;
        }

        /**
         * Calculates the next due time. This method is called for new alarms only. The
         * idea is that a new alarm is published rather soon after its arrival. The next
         * due time is set to the minimum of the due time of the alarm passed and
         * <code>now +
         * ALARM_UPDATE_INTERVAL_SECS * 0.3</code> (with empirical factor 0.3):
         * <p>
         * <code>
         * min(dueTime, now + ALARM_UPDATE_INTERVAL_SECS * 0.3)
         * </code>
         * 
         * @param existingAlarm The alarm that existed already when this alarm arrived.
         * @return This alarm.
         */
        public Alarm calculateNextDueTime(final Alarm existingAlarm)
        {
            final Instant soon = Instant.now().plusSeconds(ALARM_UPDATE_INTERVAL_SECS * 3 / 10);
            this.nextDueTime.set(soon.isBefore(existingAlarm.nextDueTime.get()) ? soon : existingAlarm.nextDueTime.get());
            return this;
        }

        /**
         * Only considers faultName, serviceName, faultyResource. These are the
         * properties identifying an alarm or fault indication, resp.
         */
        @Override
        public boolean equals(final Object other)
        {
            if (other == this)
                return true;

            if (!(other instanceof Alarm))
                return false;

            final Alarm that = (Alarm) other;
            final FaultIndication thisFi = this.fault;
            final FaultIndication thatFi = that.fault;

            return (thisFi.getFaultName() == thatFi.getFaultName() || thisFi.getFaultName() != null && thisFi.getFaultName().equals(thatFi.getFaultName()))
                   && (thisFi.getServiceName() == thatFi.getServiceName()
                       || thisFi.getServiceName() != null && thisFi.getServiceName().equals(thatFi.getServiceName()))
                   && (thisFi.getFaultyResource() == thatFi.getFaultyResource() || thisFi.getFaultyResource().equals(thatFi.getFaultyResource()));
        }

        @JsonProperty("fault")
        public FaultIndication getFault()
        {
            return this.fault;
        }

        /**
         * Only considers faultName, serviceName, faultyResource. These are the
         * properties identifying an Alarm or FaultIndication, resp.
         */
        @Override
        public int hashCode()
        {
            int result = 1;
            final FaultIndication fi = this.fault;
            result = result * 31 + (fi.getFaultName() == null ? 0 : fi.getFaultName().hashCode());
            result = result * 31 + (fi.getServiceName() == null ? 0 : fi.getServiceName().hashCode());
            result = result * 31 + fi.getFaultyResource().hashCode();
            return result;
        }

        /**
         * When an alarm is due, it means that is must be updated (published again).
         * 
         * @return True if the alarm's due time has passed.
         */
        @JsonProperty("isDue")
        public boolean isDue()
        {
            return Instant.now().isAfter(this.nextDueTime.get());
        }

        /**
         * Note: alarms that have not been published cannot expire.
         * 
         * @return True if the alarm has been published AND its expiration time has
         *         passed.
         */
        @JsonProperty("isExpired")
        public boolean isExpired()
        {
            return this.isPublished && Instant.now().isAfter(this.expirationTime);
        }

        /**
         * An alarm is said to be stale if it is expired but would be due again if it
         * hadn't.
         * 
         * @return True if the alarm is expired but would be due again if it hadn't.
         */
        @JsonIgnore
        public boolean isStale()
        {
            return this.isExpired() && this.isDue();
        }

        @Override
        public String toString()
        {
            try
            {
                return json.writeValueAsString(this);
            }
            catch (JsonProcessingException e)
            {
                return e.toString();
            }
        }
    }

    /**
     * The interval used for polling the active alarms for due updates.
     */
    private static final int POLLING_INTERVAL_SECS = 2;

    private static final ObjectMapper json = Jackson.om();
    private static final Logger log = LoggerFactory.getLogger(AlarmHandler.class);

    public static IfAlarmHandler of(final FmAlarmService alarmService)
    {
        return new AlarmHandler(alarmService);
    }

    private final FmAlarmService alarmService;
    private final Map<String, Map<Alarm, Alarm>> alarms = new ConcurrentHashMap<>();
    private final Map<String, Subject<Alarm>> alarmChannels = new ConcurrentHashMap<>();
    private final Map<String, Subject<Collection<Alarm>>> alarmsChannels = new ConcurrentHashMap<>();
    private final Subject<Alarm> alarmStream = BehaviorSubject.create();
    private final Subject<Alarm> ceaseAlarmStream = BehaviorSubject.create();
    private final Subject<Alarm> raiseAlarmStream = BehaviorSubject.create();
    private final List<Disposable> disposables = new ArrayList<>();

    private AlarmHandler(final FmAlarmService alarmService)
    {
        this.alarmService = alarmService;
    }

    /**
     * Publishes the Alarm passed to the input queue of the Alarm handler.
     * 
     * @param alarm
     */
    @Override
    public void publish(final Alarm alarm)
    {
        if (alarm != null)
        {
            this.alarmChannels.computeIfAbsent(alarm.getFault().getFaultName(), faultName ->
            {
                final Subject<Alarm> alarmChannel = BehaviorSubject.<Alarm>create();
                this.disposables.add(this.alarmProcessor(alarmChannel)
                                         .doOnSubscribe(d -> log.info("Started waiting for {} alarms.", faultName))
                                         .doOnDispose(() -> log.info("Stopped waiting for {} alarms.", faultName))
                                         .subscribe(() -> log.info("Stopped waiting for {} alarms to be processed.", faultName),
                                                    t -> log.error("Stopped waiting for {} alarms to be processed. Cause: {}",
                                                                   faultName,
                                                                   Utils.toString(t, log.isDebugEnabled()))));

                return alarmChannel;
            }).toSerialized().onNext(alarm);
        }
    }

    @Override
    public void publish(final Map<String, ? extends Collection<Alarm>> alarms)
    {
        alarms.entrySet().forEach(e ->
        {
            this.alarmsChannels.computeIfAbsent(e.getKey(), k ->
            {
                final Subject<Collection<Alarm>> alarmChannel = BehaviorSubject.<Collection<Alarm>>createDefault(new HashSet<>());
                this.disposables.add(this.alarmsProcessor(alarmChannel)
                                         .doOnSubscribe(d -> log.info("Started waiting for set of {} alarms.", e.getKey()))
                                         .doOnDispose(() -> log.info("Stopped waiting for set of {} alarms.", e.getKey()))
                                         .subscribe(() -> log.info("Stopped waiting for set of {} alarms to be processed.", e.getKey()),
                                                    t -> log.error("Stopped waiting for set of {} alarms to be processed. Cause: {}",
                                                                   e.getKey(),
                                                                   Utils.toString(t, log.isDebugEnabled()))));

                return alarmChannel;
            }).toSerialized().onNext(e.getValue());
        });
    }

    @Override
    public Completable start()
    {
        return Completable.complete()//
                          .andThen(Completable.fromAction(() ->
                          {
                              this.disposables.add(this.alarmStream.flatMapCompletable(this::raiseOrCease).subscribe());
                              this.disposables.add(this.ceaseAlarmStream.flatMapCompletable(this::cease).subscribe());
                              this.disposables.add(this.raiseAlarmStream.flatMapCompletable(this::raise).subscribe());
                          }));
    }

    @Override
    public Completable stop()
    {
        return Completable.fromAction(() -> this.disposables.stream().forEach(d -> d.dispose()));
    }

    /**
     * An Alarm processor is tight to one Alarm channel which is tight to one kind
     * of Alarm. Looping over the old Alarms of the same kind in order to refresh
     * them at every tick is doing the job.
     * 
     * @param alarmStream Stream of Alarms of the same kind (i.e. with the same
     *                    fault name).
     * @return The Alarm processor.
     */
    private Completable alarmProcessor(final Subject<Alarm> alarmStream)
    {
        return Flowable.combineLatest(alarmStream.toFlowable(BackpressureStrategy.LATEST),
                                      Flowable.interval(POLLING_INTERVAL_SECS, TimeUnit.SECONDS),
                                      (alarm,
                                       tick) -> alarm)
                       .onBackpressureBuffer()
                       .observeOn(Schedulers.single())
                       .scan((prev,
                              curr) ->
                       {
                           final Map<Alarm, Alarm> alarms = this.alarms.computeIfAbsent(curr.getFault().getFaultName(), v -> new ConcurrentHashMap<>());

                           if (prev != curr)
                           {
                               // A new alarm has arrived.

                               final Alarm existing = alarms.put(curr, curr);

                               if (existing != null)
                                   curr.calculateNextDueTime(existing);

                               log.debug("New: curr={}, prev={}", curr, prev);

                               if (curr.getFault().getSeverity().orElse(Severity.CLEAR).equals(Severity.CLEAR))
                               {
                                   if (existing != null)
                                   {
                                       alarms.remove(curr);
                                       this.alarmStream.onNext(curr);
                                   }
                               }
                               else
                               {
                                   if (curr.isDue())
                                       this.alarmStream.onNext(curr.advanceNextDueTime());
                               }
                           }
                           else
                           {
                               // No new Alarm has arrived.

                               log.debug("Old: curr={}", curr);

                               // Cleanup stale Alarms.

                               alarms.values().removeIf(Alarm::isStale);

                               // Update the old ones if due.

                               alarms.values()
                                     .stream()
                                     .filter(currAlarm -> !currAlarm.isExpired() && currAlarm.isDue())
                                     .forEach(currAlarm -> this.alarmStream.onNext(currAlarm.advanceNextDueTime()));
                           }

                           return curr;
                       })
                       .ignoreElements()
                       .doOnError(t -> log.error("Error processing alarms. Cause: {}", Utils.toString(t, log.isDebugEnabled())))
                       .retry();
    }

    private Completable alarmsProcessor(final Subject<Collection<Alarm>> alarmsStream)
    {
        return alarmsStream.toFlowable(BackpressureStrategy.LATEST)
                           .scan((prev,
                                  curr) ->
                           {
                               if (prev != curr)
                               {
                                   // New Alarms have arrived.

                                   // Cease previous Alarms not in current Alarms.

                                   prev.stream()
                                       .filter(prevAlarm -> !curr.contains(prevAlarm))
                                       .map(prevAlarm -> Alarm.of(prevAlarm, Severity.CLEAR))
                                       .forEach(this::publish);

                                   // Raise new Alarms.

                                   curr.stream().forEach(this::publish);
                               }

                               return curr;
                           })
                           .ignoreElements()
                           .doOnError(t -> log.error("Error processing alarms. Cause: {}", Utils.toString(t, log.isDebugEnabled())))
                           .retry();
    }

    private Completable cease(final Alarm alarm)
    {
        return Completable.defer(() -> this.alarmService.cease(alarm.getFault())
                                                        .doOnSubscribe(d -> log.debug("Ceasing alarm: {}", alarm))
                                                        .doOnError(e -> log.error("Error ceasing alarm. Cause: {}", e.toString()))
                                                        .onErrorComplete());
    }

    private Completable raise(final Alarm alarm)
    {
        return Completable.defer(() -> this.alarmService.raise(alarm.getFault())
                                                        .doOnSubscribe(d -> log.debug("Raising alarm: {}", alarm))
                                                        .doOnError(e -> log.error("Error raising alarm. Cause: {}", e.toString()))
                                                        .onErrorComplete());
    }

    private Completable raiseOrCease(final Alarm alarm)
    {
        return Completable.defer(() -> alarm.getFault().getSeverity().orElse(Severity.CLEAR).equals(Severity.CLEAR) ? this.cease(alarm) : this.raise(alarm));
    }
}
