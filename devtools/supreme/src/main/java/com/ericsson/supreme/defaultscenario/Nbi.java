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
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.supreme.api.GeneratedCert;
import com.ericsson.supreme.config.Configuration;
import com.ericsson.supreme.exceptions.CertificateInstallationException;
import com.ericsson.supreme.exceptions.DefaultScenarioException;
import com.ericsson.supreme.exceptions.KubernetesClientException;
import com.ericsson.supreme.exceptions.NetconfClientException;
import com.ericsson.supreme.kernel.KubernetesClient;
import com.ericsson.supreme.kernel.Utils;

public final class Nbi extends AbstractDefaultScenario implements DefaultScenario, Processor
{
    private GeneratedCert cert;
    private Processor nextProcessor;

    private static final String NAME = "nbi";

    private static final String HTTP_PROXY_NAME = "eric-sc-cs-nbi";

    private static final String KEY_NAME = "sc-nbi-default-key";
    private static final String CERT_NAME = "sc-nbi-default-cert";

    private static final String DEFAULT_FQDN = "nbi.ericsson.se";

    private static final Logger log = LoggerFactory.getLogger(Nbi.class);

    /**
     * @param config
     */
    public Nbi(Configuration config)
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
        try
        {
            var k8sClient = Utils.getKubernetesClient(this.config.getAdmin().getNamespace(), this.config.getAdmin().getKubeconfig());
            var fqdn = k8sClient.fetchHttpProxyFqdn(HTTP_PROXY_NAME);
            log.info("Fqdn to be used for NBI: {} .", fqdn);
            this.cert = super.defaultCreateCerts(NAME, fqdn, List.of(fqdn), ca);
        }
        catch (KubernetesClientException e)
        {
            log.warn("The fqdn of http-proxy was not found:", e);
            log.info("Will use default http-proxy fqdn: " + DEFAULT_FQDN);
            this.cert = super.defaultCreateCerts(NAME, DEFAULT_FQDN, List.of(DEFAULT_FQDN, "*.ericsson.se"), ca);
        }

    }

    @Override
    public void installCerts(KubernetesClient kubeClient) throws DefaultScenarioException
    {
        try
        {
            Utils.getNetconfInstaller(this.config.getAdmin(), kubeClient).installCertificateAtNetconf(KEY_NAME, CERT_NAME, this.cert);
        }
        catch (CertificateInstallationException | NetconfClientException e)
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
