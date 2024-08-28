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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.configutil.CommonConfigUtils;
import com.ericsson.sc.configutil.ServiceConfig;
import com.ericsson.sc.proxyal.proxyconfig.ProxyCfg;
import com.ericsson.sc.scp.config.Egress;
import com.ericsson.sc.scp.config.Ingress;
import com.ericsson.sc.scp.config.RoutingContext;
import com.ericsson.sc.scp.model.EricssonScp;
import com.ericsson.sc.scp.model.EricssonScpScpFunction;
import com.ericsson.sc.scp.model.NfInstance;

import io.kubernetes.client.openapi.models.V1Service;
import io.reactivex.Flowable;

public class ScpCfgMapper
{
    private static final Logger log = LoggerFactory.getLogger(ScpCfgMapper.class);
    private static final String NF_TYPE = "SCP";

    /**
     * Private constructor because it only contains static functions, no objects
     * should be instantiated from this class.
     */
    private ScpCfgMapper()
    {
        throw new IllegalStateException("Utility class -- only static functions");
    }

    /**
     * Return a Flowable that converts SCP configuration from CM-format to
     * proxy-config.
     * 
     * @param ericScpFlow
     * @return
     */
    public static Flowable<Optional<ProxyCfg>> toProxyCfg(Flowable<Optional<EricssonScp>> ericScpFlow,
                                                          final List<V1Service> k8sServiceList)
    {
        return ericScpFlow.map(scpCfg ->
        {
            log.debug("Mapping EricssonScp to ProxyCfg");

            try
            {
                final ProxyCfg pxCfg = new ProxyCfg(NF_TYPE);

                if (scpCfg.isPresent()) // if not present, it's a DELETE request
                {
                    log.debug("scpCfg:\n{}\n", scpCfg);

                    final Optional<NfInstance> scpInst = tryToGetNfInstance(scpCfg.get());

                    if (scpInst.isPresent())
                    {
                        pxCfg.addDefaultIpFamilies(CommonConfigUtils.getDefaultIpFamilies(scpInst.get()));

                        final ServiceConfig scpServiceCfg = new ServiceConfig(new Ingress(scpInst.get(), k8sServiceList),
                                                                              new RoutingContext(scpInst.get()),
                                                                              new Egress(scpInst.get()),
                                                                              scpInst.get(),
                                                                              "scp");
                        scpServiceCfg.convertConfig();
                        scpServiceCfg.addListeners(pxCfg);
                        scpServiceCfg.addClusters(pxCfg);
                        scpServiceCfg.addListenerRoutes(pxCfg);
                    }
                }

                log.debug("Configuration:\n{}\n", pxCfg);
                return Optional.of(pxCfg);
            }
            catch (final Exception e)
            {
                log.warn("Ignoring new configuration. Cause: {}", com.ericsson.utilities.exceptions.Utils.toString(e, log.isDebugEnabled()));
                return Optional.empty();
            }
        });
    }

    private static Optional<NfInstance> tryToGetNfInstance(EricssonScp scpCfg)
    {
        EricssonScpScpFunction scpFunct = scpCfg.getEricssonScpScpFunction();

        if (scpFunct == null)
        {
            log.warn("scp-function is not configured.");
            return Optional.empty();
        }
        else if (scpFunct.getNfInstance().isEmpty())
        {
            log.warn("nf-instance is not configured.");
            return Optional.empty();
        }
        else
        {
            return scpFunct.getNfInstance().stream().findFirst();
        }
    }
}
