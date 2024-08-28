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
 * Created on: May 3, 2023
 *     Author: eedstl
 */

package com.ericsson.sc.nrf.r17;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.common.alarm.AlarmHandler.Alarm;
import com.ericsson.sc.fm.model.fi.FaultIndication.Severity;
import com.ericsson.sc.nrf.r17.NnrfNfDiscoveryValidator.ValidationError;
import com.ericsson.utilities.exceptions.Utils;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;

public class NnrfNfDiscoveryAlarmHandler
{
    private static final String NF_DISCOVERY_DATA_UPDATE_ERROR = "NfDiscoveryDataUpdateError";
    private static final String NF_DISCOVERY_DATA_UPDATE_ERROR_DESCR = "Unable to update discovered data in database";
    private static final String NF_INVALID_DISCOVERED_NF_INSTANCE = "InvalidDiscoveredNfInstance";
    private static final String NF_INVALID_DISCOVERED_NF_INSTANCE_DESCR = "Invalid discovered NF instance not updated in database";

    private static final long ALARM_TTL_SECS = 2 * 3600L;

    private static final Logger log = LoggerFactory.getLogger(NnrfNfDiscoveryAlarmHandler.class);

    private final List<Disposable> disposables = new ArrayList<>();
    private final Alarm.Context alarmCtx;

    public NnrfNfDiscoveryAlarmHandler(final Alarm.Context alarmCtx)
    {
        this.alarmCtx = alarmCtx;
    }

    public void ceaseAlarmNfDiscoveryDataUpdateError(final String faultyResource)
    {
        this.alarmCtx.publish(Alarm.of(Alarm.toAlarmName(this.alarmCtx.getAlarmPrefix(), NF_DISCOVERY_DATA_UPDATE_ERROR),
                                       this.alarmCtx.getServiceName(),
                                       faultyResource));
    }

    public void raiseAlarmNfDiscoveryDataUpdateError(final String faultyResource,
                                                     final Throwable error)
    {
        this.alarmCtx.publish(Alarm.of(Alarm.toAlarmName(this.alarmCtx.getAlarmPrefix(), NF_DISCOVERY_DATA_UPDATE_ERROR),
                                       this.alarmCtx.getServiceName(),
                                       faultyResource,
                                       Severity.MAJOR,
                                       Alarm.toDescription(NF_DISCOVERY_DATA_UPDATE_ERROR_DESCR,
                                                           (error.getMessage() != null ? error.getMessage() : "Unspecified error")),
                                       ALARM_TTL_SECS,
                                       null));
    }

    public Completable start()
    {
        return Completable.complete()//
                          .andThen(Completable.fromAction(() ->
                          {
                              final String alarmName = Alarm.toAlarmName(this.alarmCtx.getAlarmPrefix(), NF_INVALID_DISCOVERED_NF_INSTANCE);

                              final Function<List<ValidationError>, Completable> publishAsAlarms = //
                                      errors -> Flowable.fromIterable(errors)
                                                        .map(error -> Alarm.of(alarmName,
                                                                               this.alarmCtx.getServiceName(),
                                                                               error.getNfInstanceId(),
                                                                               Severity.MAJOR,
                                                                               Alarm.toDescription(NF_INVALID_DISCOVERED_NF_INSTANCE_DESCR, error.getDetails()),
                                                                               ALARM_TTL_SECS,
                                                                               null))
                                                        .collect(() -> new HashSet<Alarm>(), Set::add)
                                                        .map(alarms -> Map.of(alarmName, alarms))
                                                        .doOnSuccess(this.alarmCtx::publish)
                                                        .ignoreElement();

                              this.disposables.add(NnrfNfDiscoveryValidator.singleton()
                                                                           .getValidationErrorsStream()
                                                                           .toFlowable(BackpressureStrategy.LATEST)//
                                                                           .flatMapCompletable(publishAsAlarms)
                                                                           .doOnError(t -> log.error("Error processing NnrfNfDiscoveryValidation errors. Cause: {}",
                                                                                                     Utils.toString(t, log.isDebugEnabled())))
                                                                           .retry()
                                                                           .doOnSubscribe(d -> log.info("Started waiting for NnrfNfDiscoveryValidation errors."))
                                                                           .subscribe(() -> log.info("Stopped waiting for NnrfNfDiscoveryValidation errors."),
                                                                                      t -> log.error("Stopped waiting for NnrfNfDiscoveryValidation errors. Cause: {}",
                                                                                                     Utils.toString(t, log.isDebugEnabled()))));
                          }));
    }

    public Completable stop()
    {
        return Completable.fromAction(() -> this.disposables.stream().forEach(d -> d.dispose()));
    }
}
