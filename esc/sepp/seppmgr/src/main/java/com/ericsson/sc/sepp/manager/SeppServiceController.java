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
 * Created on: Aug 20, 2020
 *     Author: eedrak
 */

package com.ericsson.sc.sepp.manager;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.cm.CmmPatch;
import com.ericsson.adpal.fm.Alarm;
import com.ericsson.sc.fm.FmAlarmService;
import com.ericsson.sc.proxyal.outlierlogservice.EnvoyStatus;
import com.ericsson.sc.proxyal.service.IProxyService;
import com.ericsson.sc.proxyal.service.ProxyFactory;
import com.ericsson.sc.proxyal.service.PvtbConfig;
import com.ericsson.sc.sepp.model.EricssonSepp;
import com.ericsson.utilities.common.Pair;
import com.ericsson.utilities.common.Rdn;
import com.ericsson.utilities.common.Triplet;

import io.kubernetes.client.openapi.models.V1Service;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

/**
 * 
 */
public class SeppServiceController
{

    private final IProxyService proxyConfigService;

    private static final Logger log = LoggerFactory.getLogger(SeppServiceController.class);
    private final BehaviorSubject<Optional<Map<String, Triplet<String, String, Boolean>>>> etcdSubject = BehaviorSubject.<Optional<Map<String, Triplet<String, String, Boolean>>>>createDefault(Optional.empty());

    public SeppServiceController(String serviceName,
                                 String namespace,
                                 FmAlarmService alarmService,
                                 Observable<Optional<EricssonSepp>> config,
                                 final CmmPatch cmPatch,
                                 final List<V1Service> k8sServiceList,
                                 final Optional<Flowable<Optional<PvtbConfig>>> pvtbConfigs,
                                 final N32cInterface n32cInterface) throws IOException
    {
        final var cfgFlowable = config.toFlowable(BackpressureStrategy.LATEST).observeOn(Schedulers.io()).distinctUntilChanged(); // Remove consecutive //

        // identical marbles
        n32cInterface.watchSecurityNegotiationData()//
                     .throttleLast(1L, TimeUnit.SECONDS)
                     .doOnNext(etcdNotify ->
                     {
                         etcdNotify.ifPresent(currState ->
                         {
                             this.etcdSubject.getValue()//
                                             .ifPresentOrElse(prevState ->  // if previous state exists
                                             {
                                                 if (!currState.equals(prevState)) // check if previous=new state
                                                 {
                                                     log.debug("State Data Changes detected");
                                                     this.etcdSubject.toSerialized().onNext(etcdNotify);
                                                 }
                                                 else
                                                 {
                                                     log.debug("State Data Changes NOT detected");
                                                 }
                                             }, () ->
                                             {
                                                 this.etcdSubject.toSerialized().onNext(etcdNotify);
                                             });
                         });
                     })
                     .doOnNext(o -> log.debug("db notification received:{}", o))
                     .doOnError(e -> log.error("Unexpected error {}", e.getMessage()))
                     .doOnSubscribe(s -> log.info("Start watching etcd for n32c changes"))
                     .doOnComplete(() -> log.info("Stop watching etcd for n32c changes"))
                     .subscribeOn(Schedulers.io())
                     .subscribe(s ->
                     {
                     }, e -> log.error("Unexpected error {}", e.getMessage()));

        var pxCfgOut = SeppCfgMapper.toProxyCfg(Flowable.combineLatest(cfgFlowable, etcdSubject.toFlowable(BackpressureStrategy.LATEST), Pair::of),
                                                cmPatch,
                                                k8sServiceList)
                                    .filter(Optional::isPresent);

        var alarmBadConfiguration = new Alarm(serviceName,
                                              alarmService,
                                              "SeppBadConfiguration",
                                              new Rdn("nf", "sepp-function").toString(false),
                                              "The configuration provided is faulty.",
                                              (long) (ProxyFactory.ALARM_TTL_SEC * ProxyFactory.ALARM_SAFETY_MARGIN));

        this.proxyConfigService = ProxyFactory.getConfigService(alarmBadConfiguration, pxCfgOut, pvtbConfigs, 9900);
    }

    public Completable start()
    {
        return Completable.fromAction(() -> this.proxyConfigService.start().blockingAwait());
    }

    public Completable stop()
    {
        return Completable.fromAction(() -> this.proxyConfigService.stop().blockingAwait());
    }

    /*
     * publishes the ID of disconnecting Proxy Worker PODs
     */
    public PublishSubject<String> getDisconnections()
    {
        return this.proxyConfigService.getEnvoyDisconnections();
    }

    /*
     * publishes the EnvoyStatus updates trigger by outlier detection
     */
    public PublishSubject<EnvoyStatus> getOutlierEventStream()
    {
        return this.proxyConfigService.getOutlierEventStream();
    }

    /*
     * publishes the EnvoyStatus updates trigger by active health check
     */
    public PublishSubject<com.ericsson.sc.proxyal.healtchecklogservice.EnvoyStatus> getHealthCheckEventStream()
    {
        return this.proxyConfigService.getHealthCheckEventStream();
    }

}