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
 * Created on: Jan 8, 2022
 *     Author: eaoknkr
 */

package com.ericsson.supreme.defaultscenario;

import java.nio.file.Path;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.supreme.api.GeneratedCert;
import com.ericsson.supreme.config.Configuration;
import com.ericsson.supreme.exceptions.CertificateIOException;
import com.ericsson.supreme.exceptions.CertificateInstallationException;
import com.ericsson.supreme.exceptions.DefaultScenarioException;
import com.ericsson.supreme.exceptions.NetconfClientException;
import com.ericsson.supreme.kernel.CertificateIO;
import com.ericsson.supreme.kernel.KubernetesClient;
import com.ericsson.supreme.kernel.Utils;

/**
 * 
 */
public class CommonRootCa extends AbstractDefaultScenario implements DefaultScenario, Processor
{
    private GeneratedCert commonCa;
    private Processor nextProcessor;
    private boolean install = true;

    private static final Logger log = LoggerFactory.getLogger(CommonRootCa.class);

    private static final String NAME = "rootca";

    /**
     * @param config
     * @throws DefaultScenarioException
     */
    public CommonRootCa(Configuration config)
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
    public void createCerts() throws DefaultScenarioException
    {
        this.commonCa = super.defaultCreateCa(NAME, "testca");
    }

    @Override
    public void installCerts(KubernetesClient kubeClient) throws DefaultScenarioException
    {
        try
        {
            Utils.getNetconfInstaller(config.getAdmin(), kubeClient).installCertificateAuthorityAtNetconf("sc-trusted-default-cas", "CA_cert_1", this.commonCa);
        }
        catch (CertificateInstallationException | NetconfClientException e)
        {
            throw new DefaultScenarioException(NAME + " certificate installation error.", e);
        }
    }

    public GeneratedCert getCa()
    {
        return this.commonCa;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.sc.supreme.defaultscenario.DefaultScenario#createCerts(api.
     * GeneratedCert)
     */
    @Override
    public void createCerts(GeneratedCert ca)
    {
        // Root ca is self-signed
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.sc.supreme.defaultscenario.DefaultScenario#readCerts()
     */
    @Override
    public void readCerts() throws DefaultScenarioException
    {
        try
        {
            var caOptional = CertificateIO.readGeneratedCert(NAME, Path.of(this.config.getDefaultScenarios().getOutputDir(), NAME));

            if (caOptional.isPresent())
            {
                this.commonCa = caOptional.get();
            }
            else
            {
                log.info("Certificates for {} were not found under the specified directory. Creating new ones", NAME);
                this.createCerts();
            }
        }
        catch (CertificateIOException e)
        {
            throw new DefaultScenarioException("Unable to read certificate for " + NAME, e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.sc.supreme.defaultscenario.Processor#process(boolean,
     * boolean, com.ericsson.sc.supreme.defaultscenario.Processor)
     */
    @Override
    public void process(boolean generate,
                        Optional<KubernetesClient> kubeClient,
                        GeneratedCert commonCa) throws DefaultScenarioException
    {
        this.readCerts();

        if (kubeClient.isPresent() && this.install)
        {
            this.installCerts(kubeClient.get());
        }

        if (nextProcessor != null)
        {
            nextProcessor.process(generate, kubeClient, this.commonCa);
        }
    }

    public void setInstallation(boolean install)
    {
        this.install = install;
    }

}
