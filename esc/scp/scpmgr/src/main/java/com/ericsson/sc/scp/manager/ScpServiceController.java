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

package com.ericsson.sc.scp.manager;

import java.util.List;
import java.util.Optional;

import com.ericsson.adpal.fm.Alarm;
//import com.ericsson.adpal.fm.AlarmHandler;
import com.ericsson.sc.fm.FmAlarmService;
import com.ericsson.sc.proxyal.outlierlogservice.EnvoyStatus;
import com.ericsson.sc.proxyal.service.IProxyService;
import com.ericsson.sc.proxyal.service.ProxyFactory;
import com.ericsson.sc.proxyal.service.PvtbConfig;
//import com.ericsson.esc.scp.manager.ScpCfgMapper;
import com.ericsson.sc.scp.model.EricssonScp;
import com.ericsson.utilities.common.Rdn;

import io.kubernetes.client.openapi.models.V1Service;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * 
 */
public class ScpServiceController
{
    private final IProxyService proxyConfigService;

    public ScpServiceController(String serviceName,
                                FmAlarmService alarmService,
                                Observable<Optional<EricssonScp>> config,
                                final List<V1Service> k8sServiceList,
                                final Optional<Flowable<Optional<PvtbConfig>>> pvtbConfigs)
    {
        final var cfgFlowable = config.toFlowable(BackpressureStrategy.LATEST).observeOn(Schedulers.io()).distinctUntilChanged(); // Remove consecutive
                                                                                                                                  // identical marbles

        var pxCfgOut = ScpCfgMapper.toProxyCfg(cfgFlowable, k8sServiceList).filter(Optional::isPresent);

        var alarmBadConfiguration = new Alarm(serviceName,
                                              alarmService,
                                              "ScpBadConfiguration",
                                              new Rdn("nf", "scp-function").toString(false),
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
