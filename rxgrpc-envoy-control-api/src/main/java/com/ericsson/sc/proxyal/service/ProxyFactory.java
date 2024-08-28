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
 * Created on: Jun 23, 2020
 *     Author: eedrak
 */

package com.ericsson.sc.proxyal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.ericsson.adpal.fm.Alarm;
import com.ericsson.sc.proxyal.healtchecklogservice.HealthCheckServiceSeppScp;
import com.ericsson.sc.proxyal.outlierlogservice.OutlierLogServiceSeppScp;
import com.ericsson.sc.proxyal.proxyconfig.ProxyCfg;
import com.ericsson.sc.utilities.dns.DnsCache;

import io.grpc.BindableService;
import io.reactivex.Flowable;

public class ProxyFactory
{

    public static final Long ALARM_TTL_SEC = 60L;
    public static final double ALARM_SAFETY_MARGIN = 1.5;

    private ProxyFactory()
    {
    } // class provides only static factory methods

    public static IProxyService getConfigService(Alarm badConfigurationAlarm,
                                                 Flowable<Optional<ProxyCfg>> config,
                                                 Optional<Flowable<Optional<PvtbConfig>>> pvtbConfigs,
                                                 int servicePort)
    {

        var badConfigurationAdsAlarm = new AdsAlarm(badConfigurationAlarm);

        final var cfgFlow = config;

        var pxCfgMapper = new ProxyCfgMapper(DnsCache.getInstance(), cfgFlow, pvtbConfigs);

        var pxCfgMapped = pxCfgMapper.getMappedProxyConfigs();

        var adsLogic = new EnvoyAdsLogic(badConfigurationAdsAlarm, pxCfgMapped);

        var outlierService = new OutlierLogServiceSeppScp();
        var healthCheckLogService = new HealthCheckServiceSeppScp();
        final List<BindableService> grpcServices = new ArrayList<>();

        grpcServices.add(new ADSv2Impl(adsLogic));
        grpcServices.add(new ADSv3Impl(adsLogic));
        grpcServices.add((BindableService) outlierService);
        grpcServices.add((BindableService) healthCheckLogService);

        var rxServer = new RxServer(servicePort, grpcServices);

        return new EnvoyConfigService(pxCfgMapper, adsLogic, outlierService, healthCheckLogService, rxServer);

    }

}