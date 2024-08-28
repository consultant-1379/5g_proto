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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.cm.CmmPatch;
import com.ericsson.sc.configutil.CommonConfigUtils;
import com.ericsson.sc.configutil.ServiceConfig;
import com.ericsson.sc.proxyal.proxyconfig.ProxyCfg;
import com.ericsson.sc.rxkms.KmsException;
import com.ericsson.sc.sepp.config.Egress;
import com.ericsson.sc.sepp.config.Ingress;
import com.ericsson.sc.sepp.config.RoutingContext;
import com.ericsson.sc.sepp.config.ScramblingKeys;
import com.ericsson.sc.sepp.model.EricssonSepp;
import com.ericsson.sc.sepp.model.NfInstance;
import com.ericsson.utilities.common.Pair;
import com.ericsson.utilities.common.Triplet;
import com.ericsson.utilities.reactivex.RetryFunction;

import io.kubernetes.client.openapi.models.V1Service;
import io.reactivex.Flowable;

public class SeppCfgMapper
{
    private static final Logger log = LoggerFactory.getLogger(SeppCfgMapper.class);
    private static final String NF_TYPE = "SEPP";
    private static final long KMS_ERROR_INTERVAL = 60L;
    private static AtomicInteger ineligibleSansVersion = new AtomicInteger(0);

    /**
     * Private constructor because it only contains static functions, no objects
     * should be instantiated from this class.
     */
    private SeppCfgMapper()
    {
        throw new IllegalStateException("Utility class -- only static functions");
    }

    /**
     * Return a Flowable that converts SCP configuration from CM-format to
     * proxy-config.
     * 
     * @param ericSeppFlow
     * @return
     */
    public static Flowable<Optional<ProxyCfg>> toProxyCfg(Flowable<Pair<Optional<EricssonSepp>, Optional<Map<String, Triplet<String, String, Boolean>>>>> ericSeppFlow,
                                                          final CmmPatch cmPatch,
                                                          final List<V1Service> k8sServiceList)
    {
        ScramblingKeys keys = ScramblingKeys.getInstance();
        return ericSeppFlow.switchMap(seppCfgPair -> keys.updateConfig(tryToGetNfInstance(seppCfgPair.getFirst())).timeout(10, TimeUnit.SECONDS).doOnError(e ->
        {
            log.debug("Failed to decrypt keys, will drop configuration if persists.");
            keys.decryptionFailed();
        })
                                                         .retryWhen(new RetryFunction().withDelay(KMS_ERROR_INTERVAL * 1000L)
                                                                                       .withRetries(-1)
                                                                                       .withPredicate(e -> e instanceof KmsException fse ? (int) fse.statusCode() == 403
                                                                                                                                           || (int) fse.statusCode()
                                                                                                                                              / 100 != 4
                                                                                                                                         : true)
                                                                                       .withRetryAction((error,
                                                                                                         retry) ->
                                                                                       {
                                                                                           keys.decryptionSuccess();
                                                                                           if (error instanceof TimeoutException)
                                                                                           {
                                                                                               log.error("Unable to decrypt scrambling key, KMS client not ready, error: ",
                                                                                                         error);
                                                                                           }
                                                                                           else
                                                                                           {
                                                                                               log.error("Unable to decrypt scrambling key, error: ", error);
                                                                                           }
                                                                                       })
                                                                                       .create())
                                                         .onErrorComplete()
                                                         .andThen(Flowable.just(seppCfgPair).map(cfgPair ->
                                                         {
                                                             log.debug("Mapping EricssonSepp to ProxyCfg");
                                                             if (!keys.getDecryptionStatus())
                                                             {
                                                                 log.debug("Decryption of keys failed. Dropping configuration");
                                                                 return Optional.empty();
                                                             }

                                                             try
                                                             {
                                                                 return Optional.of(SeppCfgMapper.getProxyCfg(seppCfgPair, cmPatch, k8sServiceList));
                                                             }
                                                             catch (final Exception e)
                                                             {
                                                                 log.warn("Ignoring new configuration. Cause: {}",
                                                                          com.ericsson.utilities.exceptions.Utils.toString(e, log.isDebugEnabled()));
                                                                 return Optional.empty();
                                                             }
                                                         })));
    }

    private static ProxyCfg getProxyCfg(Pair<Optional<EricssonSepp>, Optional<Map<String, Triplet<String, String, Boolean>>>> seppCfgPair,
                                        final CmmPatch cmPatch,
                                        final List<V1Service> k8sServiceList)
    {
        log.info("proxyy");

        final int version = ineligibleSansVersion.incrementAndGet();

        if (version > 200000)// Arbitrary value to prevent overflow. The value itself doesn't matter and it
                             // is only used to distinguish updates
        {
            ineligibleSansVersion.set(0);
        }

        final ProxyCfg pxCfg = new ProxyCfg(NF_TYPE);
        final Optional<EricssonSepp> seppCfg = seppCfgPair.getFirst();
        final Optional<Map<String, Triplet<String, String, Boolean>>> stateData = seppCfgPair.getSecond();

        if (seppCfg.isPresent()) // if not present, it's a DELETE request
        {
            log.debug("seppCfg:\n{}\n", seppCfg);

            final Optional<NfInstance> seppInst = tryToGetNfInstance(seppCfg.get());

            if (seppInst.isPresent())
            {
                pxCfg.addDefaultIpFamilies(CommonConfigUtils.getDefaultIpFamilies(seppInst.get()));

                final ServiceConfig seppServiceCfg = new ServiceConfig(new Ingress(seppInst.get(), cmPatch, k8sServiceList),
                                                                       stateData.isPresent() ? new RoutingContext(seppInst.get(), stateData, version)
                                                                                             : new RoutingContext(seppInst.get(), Optional.empty(), 0),
                                                                       stateData.isPresent() ? new Egress(seppInst.get(), stateData)
                                                                                             : new Egress(seppInst.get(), Optional.empty()),
                                                                       seppInst.get(),
                                                                       "sepp");
                seppServiceCfg.convertConfig();
                seppServiceCfg.addListeners(pxCfg);
                seppServiceCfg.addClusters(pxCfg);
                seppServiceCfg.addListenerRoutes(pxCfg);
            }
        }

        log.debug("Configuration:\n{}\n", pxCfg);
        return pxCfg;
    }

    private static Optional<NfInstance> tryToGetNfInstance(EricssonSepp seppCfg)
    {
        var seppFunct = seppCfg.getEricssonSeppSeppFunction();

        if (seppFunct == null)
        {
            log.warn("sepp-function is not configured.");
            return Optional.empty();
        }
        else if (seppFunct.getNfInstance().isEmpty())
        {
            log.warn("nf-instance is not configured.");
            return Optional.empty();
        }
        else
        {
            return seppFunct.getNfInstance().stream().findFirst();
        }
    }

    private static Optional<NfInstance> tryToGetNfInstance(Optional<EricssonSepp> seppCfg)
    {
        if (seppCfg.isEmpty())
            return Optional.empty();
        return tryToGetNfInstance(seppCfg.get());
    }
}