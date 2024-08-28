/**
 * COPYRIGHT ERICSSON GMBH 2022
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jan 5, 2022
 *     Author: esamioa
 */

package com.ericsson.supreme.defaultscenario;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.ericsson.supreme.api.GeneratedCert;
import com.ericsson.supreme.config.Configuration;
import com.ericsson.supreme.exceptions.CertificateIOException;
import com.ericsson.supreme.exceptions.CertificateInstallationException;
import com.ericsson.supreme.exceptions.DefaultScenarioException;
import com.ericsson.supreme.kernel.KubernetesClient;
import com.ericsson.supreme.kernel.Utils;

public final class Chfsim extends AbstractDefaultScenario implements DefaultScenario, Processor
{
    private GeneratedCert cert;
    private Processor nextProcessor;
    private GeneratedCert ca;

    private static final String NAME = "chfsim";

    private static final String SECRET_NAME = "chf-certificates";

    private static final String FQDN = "eric-chfsim-1";

    /**
     * @param config
     */
    public Chfsim(Configuration config)
    {
        super(config);
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
    public void createCerts(GeneratedCert ca) throws DefaultScenarioException
    {
        this.cert = super.defaultCreateCerts(NAME,
                                             FQDN,
                                             List.of(FQDN,
                                                     "eric-chfsim-2",
                                                     "eric-chfsim-3",
                                                     "eric-chfsim-4",
                                                     "eric-chfsim-5",
                                                     "eric-chfsim-6",
                                                     "eric-chfsim-7",
                                                     "eric-chfsim-8"),
                                             ca);
    }

    @Override
    public void installCerts(KubernetesClient kubeClient) throws DefaultScenarioException
    {
        try
        {
            var map = Map.of("key.pem",
                             Utils.toPemString(this.cert.getPrivateKeyPrivateKey()),
                             "cert.pem",
                             Utils.toPemString(this.cert.getX509Certificate()),
                             "rootCA.crt",
                             Utils.toPemString(this.ca.getX509Certificate()));
            Utils.getKubernetesInstaller(kubeClient).installCertificateAtGenericSecret(SECRET_NAME, map);
        }
        catch (CertificateInstallationException | CertificateIOException e)
        {
            throw new DefaultScenarioException(NAME + " certificate installation error.", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.sc.supreme.defaultscenario.DefaultScenario#createCerts()
     */
    @Override
    public void createCerts()
    {
        // SCP worker uses common root ca
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.sc.supreme.defaultscenario.DefaultScenario#readCerts()
     */
    @Override
    public void readCerts() throws DefaultScenarioException
    {
        this.cert = super.defaultReadCerts(NAME);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.sc.supreme.defaultscenario.Processor#process(boolean,
     * boolean, api.GeneratedCert)
     */
    @Override
    public void process(boolean generate,
                        Optional<KubernetesClient> kubeClient,
                        GeneratedCert commonCa) throws DefaultScenarioException
    {
        // if generate arg is issued, then we create a new cert. If not, then we search
        // for the default scenario in the directory and read the certificate in order
        // to install it
        this.ca = commonCa;

        if (generate)
        {
            this.createCerts(commonCa);
        }
        else
        {
            this.readCerts();
        }

        if (kubeClient.isPresent())
        {
            this.installCerts(kubeClient.get());
        }

        if (this.nextProcessor != null)
        {
            this.nextProcessor.process(generate, kubeClient, commonCa);
        }

    }
}
