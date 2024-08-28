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

package com.ericsson.sc.common.alarm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.common.alarm.AlarmHandler.Alarm;
import com.ericsson.sc.fm.model.fi.FaultIndication.Severity;
import com.ericsson.sc.utilities.dns.DnsCache;
import com.ericsson.sc.utilities.dns.IfDnsLookupContext;
import com.ericsson.sc.utilities.dns.IpFamily;
import com.ericsson.sc.utilities.dns.ResolutionResult;
import com.ericsson.utilities.common.Rdn;
import com.ericsson.utilities.exceptions.Utils;

import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;

public class UnresolvableHostsAlarmHandler
{
    private static final String UNRESOLVABLE_HOSTS = "UnresolvableHosts";
    private static final String UNRESOLVABLE_HOSTS_DESCR = "One or more hostnames cannot be resolved";

    private static final Logger log = LoggerFactory.getLogger(UnresolvableHostsAlarmHandler.class);

    private final Alarm.Context alarmCtx;
    private final String faultyResource;
    private final String alarmName;
    private final List<Disposable> disposables = new ArrayList<>();

    public UnresolvableHostsAlarmHandler(final Alarm.Context alarmCtx)
    {
        this.alarmCtx = alarmCtx;
        this.faultyResource = new Rdn("nf", this.alarmCtx.getServiceFunction()).toString(false);
        this.alarmName = Alarm.toAlarmName(alarmCtx.getAlarmPrefix(), UNRESOLVABLE_HOSTS);
    }

    public Completable start()
    {
        return Completable.complete()//
                          .andThen(Completable.fromAction(() ->
                          {
//                              final Function<Set<String>, Completable> publishAsAlarms = //
//                                      hosts -> Flowable.fromIterable(hosts)
//                                                       .map(host -> new Alarm(alarmName,
//                                                                              this.serviceName,
//                                                                              new Rdn("nf", this.serviceType + "-function").add("host", host).toString(false),
//                                                                              Severity.MAJOR,
//                                                                              Alarm.toDescription(UNRESOLVABLE_HOSTS_DESCR,
//                                                                                                  "unresolved hosts: " + Set.of(host)),
//                                                                              Alarm.ALARM_TTL_SECS,
//                                                                              null))
//                                                       .collect(() -> new HashSet<Alarm>(),
//                                                                (alarms,
//                                                                 alarm) -> alarms.add(alarm))
//                                                       .map(alarms -> Map.of(alarmName, alarms))
//                                                       .doOnSuccess(this.alarmHandler::publish)
//                                                       .ignoreElement();

                              this.disposables.add(DnsCache.getInstance()
                                                           .getUnresolvedHosts()//
                                                           .map(hosts ->
                                                           {
                                                               /**
                                                                * If the lookup result has results for more than one IP family, then append the
                                                                * IP family for which the host name could not be resolved to the host name. But
                                                                * If the host name could not be resolved for all IP families, do not append
                                                                * them.
                                                                * <p>
                                                                * Example: host1(ipv4)
                                                                */
                                                               Function<Entry<String, IfDnsLookupContext>, String> toHostNameWithIpFamily = e ->
                                                               {
                                                                   if (e.getValue().getIpAddrs().size() > 1)
                                                                   {
                                                                       final List<Entry<IpFamily, ResolutionResult>> list = e.getValue()
                                                                                                                             .getIpAddrs()
                                                                                                                             .entrySet()
                                                                                                                             .stream()
                                                                                                                             .filter(ei -> !ei.getValue()
                                                                                                                                              .isResolvedOk())
                                                                                                                             .toList();

                                                                       return e.getKey() + (list.size() == 1 ? "(" + list.get(0).getKey() + ")" : "");
                                                                   }

                                                                   return e.getKey();
                                                               };

                                                               return hosts.isEmpty() ? Set.<Alarm>of()
                                                                                      : Set.of(Alarm.of(this.alarmName,
                                                                                                        this.alarmCtx.getServiceName(),
                                                                                                        this.faultyResource,
                                                                                                        Severity.MAJOR,
                                                                                                        Alarm.toDescription(UNRESOLVABLE_HOSTS_DESCR,
                                                                                                                            ": " + hosts.entrySet()//
                                                                                                                                        .stream()
                                                                                                                                        .map(toHostNameWithIpFamily)
                                                                                                                                        .toList()),
                                                                                                        0L, // Do not update, rely on triggers from DnsCache
                                                                                                            // updates.
                                                                                                        null));
                                                           })
                                                           .map(alarms -> Map.of(this.alarmName, alarms))
                                                           .doOnNext(alarms -> log.debug("Publishing alarms for unresolved hosts: {}", alarms))
                                                           .doOnNext(this.alarmCtx::publish)
                                                           .ignoreElements()
//                                                         .flatMapCompletable(publishAsAlarms)
                                                           .doOnError(t -> log.error("Error processing DNS resolution errors. Cause: {}",
                                                                                     Utils.toString(t, log.isDebugEnabled())))
                                                           .retry()
                                                           .doOnSubscribe(d -> log.info("Started waiting for DNS resolution errors."))
                                                           .subscribe(() -> log.info("Stopped waiting for DNS resolution errors."),
                                                                      t -> log.error("Stopped waiting for DNS resolution errors. Cause: {}",
                                                                                     Utils.toString(t, log.isDebugEnabled()))));
                          }));
    }

    public Completable stop()
    {
        return Completable.fromAction(() -> this.disposables.stream().forEach(d -> d.dispose()));
    }
}
