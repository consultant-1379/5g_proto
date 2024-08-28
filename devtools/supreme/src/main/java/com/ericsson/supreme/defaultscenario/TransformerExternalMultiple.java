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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.ericsson.supreme.api.GeneratedCert;
import com.ericsson.supreme.config.Configuration;
import com.ericsson.supreme.exceptions.CertificateInstallationException;
import com.ericsson.supreme.exceptions.DefaultScenarioException;
import com.ericsson.supreme.exceptions.NetconfClientException;
import com.ericsson.supreme.kernel.KubernetesClient;
import com.ericsson.supreme.kernel.Utils;

public final class TransformerExternalMultiple extends AbstractDefaultScenario implements DefaultScenario, Processor
{
    private GeneratedCert cert;
    private ArrayList<GeneratedCert> certs = new ArrayList<>();
    private Processor nextProcessor;

    private static final String NAME = "ext-lj-count";

    private static final String KEY_NAME = "ext-ljcount-key-cert";
    private static final String CERT_NAME = "ext-ljcount-key-cert";

    private static final String FQDN = "eric-ext-log-transformer-count";

    private static final int EXT_LOG_TRANSFORMERS = 5;
    private static final String NAME_HOOK = "count";

    /**
     * @param config
     */
    public TransformerExternalMultiple(Configuration config)
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
        for (var i = 1; i <= EXT_LOG_TRANSFORMERS; i++)
        {
            this.cert = super.defaultCreateCerts(NAME.replace(NAME_HOOK, String.valueOf(i)), //
                                                 FQDN.replace(NAME_HOOK, String.valueOf(i)), //
                                                 List.of(FQDN.replace(NAME_HOOK, String.valueOf(i)), "*"),
                                                 ca);
            this.certs.add(this.cert);
        }
    }

    @Override
    public void installCerts(KubernetesClient kubeClient) throws DefaultScenarioException
    {

        for (var i = 1; i <= EXT_LOG_TRANSFORMERS; i++)
        {
            this.installCert(kubeClient,
                             NAME.replace(NAME_HOOK, String.valueOf(i)),
                             KEY_NAME.replace(NAME_HOOK, String.valueOf(i)),
                             CERT_NAME.replace(NAME_HOOK, String.valueOf(i)),
                             this.certs.get(i - 1));
        }
    }

    private void installCert(KubernetesClient kubeClient,
                             String name,
                             String keystoreAsymKeyName,
                             String keystoreCertificateName,
                             GeneratedCert certificate) throws DefaultScenarioException
    {
        try
        {
            Utils.getNetconfInstaller(this.config.getAdmin(), kubeClient)
                 .installCertificateAtNetconf(keystoreAsymKeyName, keystoreCertificateName, certificate);
        }
        catch (CertificateInstallationException | NetconfClientException e)
        {
            throw new DefaultScenarioException(name + " certificate installation error.", e);
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
        this.certs.clear();
        for (var i = 1; i <= EXT_LOG_TRANSFORMERS; i++)
        {
            this.certs.add(super.defaultReadCerts(NAME.replace(NAME_HOOK, String.valueOf(i))));
        }
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
            this.createCerts(commonCa);
        else
            this.readCerts();

        if (kubeClient.isPresent())
            this.installCerts(kubeClient.get());

        if (this.nextProcessor != null)
            this.nextProcessor.process(generate, kubeClient, commonCa);

    }
}
