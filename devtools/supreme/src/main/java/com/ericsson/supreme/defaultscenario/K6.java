/**
 * COPYRIGHT ERICSSON GMBH 2022
 *
 * The copyright to the computer program(s) herein is the property of Ericsson
 * GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written permission of
 * Ericsson GmbH in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 *
 * Created on: Jan 5, 2022 Author: esamioa
 */

package com.ericsson.supreme.defaultscenario;

import static java.util.Map.entry;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.supreme.api.GeneratedCert;
import com.ericsson.supreme.config.Configuration;
import com.ericsson.supreme.exceptions.DefaultScenarioException;
import com.ericsson.supreme.kernel.KubernetesClient;

/**
 * This class handles the creation and installation of K6 certificates
 */

public final class K6 extends AbstractDefaultScenario implements DefaultScenario, Processor
{
    private static final String NAME = "k6/";
    private Processor nextProcessor;

    private static final Logger log = LoggerFactory.getLogger(K6.class);

    /**
     * @param config
     */
    public K6(Configuration config)
    {
        super(config);
    }

    private final Map<String, String> fqdnMapToNames = Map.ofEntries(entry("scp.ericsson.se", ""),
                                                                     entry("sepp.5gc.mnc567.mcc765.3gppnetwork.org", "sepp"),
                                                                     entry("pSepp11.5gc.mnc012.mcc210.3gppnetwork.org", "rp1"),
                                                                     entry("pSepp21.5gc.mnc123.mcc321.3gppnetwork", "rp2"),
                                                                     entry("pSepp31.5gc.mnc234.mcc432.3gppnetwork.org", "rp3"));

    @Override
    public void createCerts(GeneratedCert ca) throws DefaultScenarioException
    {
        for (var entry : this.fqdnMapToNames.entrySet())
        {
            super.defaultCreateCerts(NAME + entry.getValue(), entry.getKey(), List.of(entry.getKey()), ca);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.supreme.defaultscenario.DefaultScenario#nextProcessor()
     */
    @Override
    public void setNextProcessor(Processor nextProcessor)
    {
        this.nextProcessor = nextProcessor;
    }

    @Override
    public void installCerts(KubernetesClient kubeClient)
    { // Always return true as K6 certs installation is handled by the
      // devtools/k6/Makefile
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.sc.supreme.defaultscenario.DefaultScenario#readCerts()
     */
    @Override
    public void readCerts()
    {
        // Nothing to be read since we don't install
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.sc.supreme.defaultscenario.DefaultScenario#createCerts()
     */
    @Override
    public void createCerts()
    {
        // We always use certs signed by common root ca
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.sc.supreme.defaultscenario.Processor#process(boolean,
     * boolean)
     */
    @Override
    public void process(boolean generate,
                        Optional<KubernetesClient> kubeClient,
                        GeneratedCert commonCa) throws DefaultScenarioException
    {
        if (generate)
        {
            this.createCerts(commonCa);
        }

        if (kubeClient.isPresent())
        {
            log.warn("Installation is not supported for K6");
        }

        if (this.nextProcessor != null)
        {
            this.nextProcessor.process(generate, kubeClient, commonCa);
        }
    }

}
