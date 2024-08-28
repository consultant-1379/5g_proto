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

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.proxyal.healtchecklogservice.HealthCheckLogService;
import com.ericsson.sc.proxyal.outlierlogservice.EnvoyStatus;
import com.ericsson.sc.proxyal.outlierlogservice.OutlierLogService;
import com.ericsson.sc.proxyal.proxyconfig.ProxyCfg;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

public class EnvoyConfigService implements IProxyService
{
    private static final Logger log = LoggerFactory.getLogger(EnvoyConfigService.class);

    Flowable<Optional<ProxyCfg>> pxCfgMapped;

    private Disposable disposable;

    private final ProxyCfgMapper pxCfgMapper;
    private final EnvoyAdsLogic adsLogic;
    private final OutlierLogService outlierService;
    private final HealthCheckLogService healthCheckLogService;

    private final RxServer rxServer;

    /**
     * @param pxCfgMapper
     * @param adsLogic
     * @param outlierService
     * @param rxServer
     */
    EnvoyConfigService(ProxyCfgMapper pxCfgMapper,
                       EnvoyAdsLogic adsLogic,
                       OutlierLogService outlierService,
                       HealthCheckLogService healthCheckLogService,
                       RxServer rxServer)
    {

        this.pxCfgMapper = pxCfgMapper;
        this.pxCfgMapped = pxCfgMapper.getMappedProxyConfigs();

        this.adsLogic = adsLogic;
        this.rxServer = rxServer;
        this.outlierService = outlierService;
        this.healthCheckLogService = healthCheckLogService;
        this.disposable = null;

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.esc.common.proxyal.configservice.IProxyService#start()
     */
    @Override
    public Completable start()
    {
        return Completable.fromAction(() ->
        {
            if (disposable == null)
            {
                this.rxServer.start().blockingAwait();
                this.pxCfgMapper.start().blockingAwait();
                this.adsLogic.start().blockingAwait();
                disposable = this.pxCfgMapped.filter(Optional::isPresent) //
                                             .map(Optional::get) //
                                             .subscribe(pxCfg ->
                                             {
                                                 try
                                                 {
                                                     log.info("New ProxyCfg received.");
                                                 }
                                                 catch (Exception e)
                                                 {
                                                     log.error("Failed to process configuration change", e);
                                                     // Ignore unexpected errors, so that processing is not permanently terminated
                                                 }
                                             }, err -> log.error("AggregatedDiscoveryService configuration change processing terminated unexpectedly", err));

            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.esc.common.proxyal.configservice.IProxyService#stop()
     */
    @Override
    public Completable stop()
    {
        return Completable.fromAction(() ->
        {
            if (this.disposable != null)
            {
                this.disposable.dispose();
                this.disposable = null;
            }
            this.rxServer.stop().andThen(this.pxCfgMapper.stop()).andThen(this.adsLogic.stop());
        });
    }

    /*
     * publishes the ID of disconnecting Envoy PODs
     */
    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.esc.common.proxyal.configservice.IProxyService#
     * getEnvoyDisconnections()
     */
    @Override
    public PublishSubject<String> getEnvoyDisconnections()
    {
        return this.adsLogic.getEnvoyDisconnections();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.esc.common.proxyal.configservice.IProxyService#
     * getOutlierEventStream()
     */
    @Override
    public PublishSubject<EnvoyStatus> getOutlierEventStream()
    {
        return this.outlierService.getOutlierEventStream();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.esc.common.proxyal.configservice.IProxyService#
     * getOutlierEventStream()
     */
    @Override
    public PublishSubject<com.ericsson.sc.proxyal.healtchecklogservice.EnvoyStatus> getHealthCheckEventStream()
    {
        return this.healthCheckLogService.getHealthCheckEventStream();
    }

}
