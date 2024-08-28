/**
 * COPYRIGHT ERICSSON GMBH 2023
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Dec 4, 2023
 *     Author: zmavioa
 */

package com.ericsson.supreme.defaultscenario;

import java.util.List;
import java.util.Optional;

import com.ericsson.supreme.api.GeneratedCert;
import com.ericsson.supreme.config.Configuration;
import com.ericsson.supreme.exceptions.CertificateInstallationException;
import com.ericsson.supreme.exceptions.DefaultScenarioException;
import com.ericsson.supreme.exceptions.NetconfClientException;
import com.ericsson.supreme.kernel.KubernetesClient;
import com.ericsson.supreme.kernel.Utils;

public class ProbeVirtualTapBroker extends AbstractDefaultScenario implements DefaultScenario, Processor
{
    private GeneratedCert cert;
    private GeneratedCert certProbeConsumer;
    private GeneratedCert certCA;

    private Processor nextProcessor;

    private static final String CERT_DIR_NAME = "pvtb";

    // consumer-ca is mounted to PVTB
    private static final String CERT_CA_DIR_NAME = "consumer-ca";

    private static final String CERT_PROBE_CONSUMER = "cert-probe-consumer";

    private static final String NAME = "pvtb";

    private static final String KEY_NAME = "probe-vtap-udp-client";
    private static final String CERT_NAME = "probe-vtap-udp-client";
    private static final String VTAP_UDP_CLIENT = "probe-vtap-udp-client";

    private static final String CN = "pvtb";

    /**
     * @param config
     */
    public ProbeVirtualTapBroker(Configuration config)
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ericsson.supreme.defaultscenario.DefaultScenario#createCerts(com.ericsson
     * .supreme.api.GeneratedCert)
     */
    @Override
    public void createCerts(GeneratedCert ca) throws DefaultScenarioException
    {
        try
        {
            this.certCA = super.defaultReadCerts(CERT_CA_DIR_NAME);
        }
        catch (DefaultScenarioException e)
        {
            this.certCA = super.defaultCreateCa(CERT_CA_DIR_NAME, "pvtb");
        }
        this.cert = super.defaultCreateCerts(CERT_DIR_NAME, CN, List.of(CN), this.certCA);
        this.certProbeConsumer = super.defaultCreateCerts(CERT_PROBE_CONSUMER, CERT_PROBE_CONSUMER, List.of(CERT_PROBE_CONSUMER), this.certCA);
    }

    @Override
    public void installCerts(KubernetesClient kubeClient) throws DefaultScenarioException
    {
        try
        {
            var installer = Utils.getNetconfInstaller(this.config.getAdmin(), kubeClient);
            installer.installCertificateAtNetconf(KEY_NAME, CERT_NAME, this.cert);
            installer.installCertificateAuthorityAtNetconf(VTAP_UDP_CLIENT, "CA_cert_1", this.certCA);
            installer.withKubernetesConfig(kubeClient);
            installer.installCertificateAtTlsSecret(CERT_PROBE_CONSUMER, this.certProbeConsumer);
            Utils.getKubernetesInstaller(kubeClient).installCertificateAuthorityAtSecret("dtls-ca", this.certCA);
        }
        catch (CertificateInstallationException | NetconfClientException e)
        {
            throw new DefaultScenarioException(NAME + " certificate installation error.", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.sc.supreme.defaultscenario.DefaultScenario#readCerts()
     */
    @Override
    public void readCerts() throws DefaultScenarioException
    {
        this.cert = super.defaultReadCerts(CERT_DIR_NAME);
        this.certCA = super.defaultReadCerts(CERT_CA_DIR_NAME);
        this.certProbeConsumer = super.defaultReadCerts(CERT_PROBE_CONSUMER);
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

    @Override
    public void createCerts()
    {
        // SCP worker uses common root ca
    }

}
