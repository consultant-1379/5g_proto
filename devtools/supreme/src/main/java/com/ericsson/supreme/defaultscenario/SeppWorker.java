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

import com.ericsson.supreme.api.GeneratedCert;
import com.ericsson.supreme.config.Configuration;
import com.ericsson.supreme.exceptions.CertificateInstallationException;
import com.ericsson.supreme.exceptions.DefaultScenarioException;
import com.ericsson.supreme.exceptions.NetconfClientException;
import com.ericsson.supreme.kernel.KubernetesClient;
import com.ericsson.supreme.kernel.Utils;

public final class SeppWorker extends AbstractDefaultScenario implements DefaultScenario, Processor
{
    private GeneratedCert extCa;
    private GeneratedCert extCert;
    private GeneratedCert intCert;
    private GeneratedCert intCa;

    private Processor nextProcessor;

    private static final String INT_DIR_NAME = "seppwrk-int";
    private static final String EXT_DIR_NAME = "seppwrk-ext";
    private static final String INT_CA_DIR_NAME = "seppwrk-int-ca";
    private static final String EXT_CA_DIR_NAME = "seppwrk-ext-ca";

    private static final String INT_LIST_NAME = "sc-traf-root-ca-list1";
    private static final String EXT_LIST_NAME = "sc-traf-root-ca-list2";

    private static final String INT_KEY_NAME = "sc-traf-default-key1";
    private static final String INT_CERT_NAME = "sc-traf-default-cert1";

    private static final String EXT_KEY_NAME = "sc-traf-default-key2";
    private static final String EXT_CERT_NAME = "sc-traf-default-cert2";

    /**
     * @param config
     */
    public SeppWorker(Configuration config)
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
        // create internal network certificate
        try
        {
            this.intCa = super.defaultReadCerts(INT_CA_DIR_NAME);
        }
        catch (DefaultScenarioException e)
        {
            // the ca is not present so create it
            this.intCa = super.defaultCreateCa(INT_CA_DIR_NAME, "seppint");
        }
        this.intCert = super.defaultCreateCerts(INT_DIR_NAME,
                                                "sepp.5gc.mnc567.mcc765.3gppnetwork.org",
                                                List.of("sepp.5gc.mnc567.mcc765.3gppnetwork.org"),
                                                this.intCa);
        // create external network CA and certificate
        try
        {
            this.extCa = super.defaultReadCerts(EXT_CA_DIR_NAME);
        }
        catch (DefaultScenarioException e)
        {
            // the ca is not present so create it
            this.extCa = super.defaultCreateCa(EXT_CA_DIR_NAME, "seppext");
        }

        this.extCert = super.defaultCreateCerts(EXT_DIR_NAME, "sepp.ericsson.se", List.of("sepp.ericsson.se"), this.extCa);
    }

    public void readCerts() throws DefaultScenarioException
    {
        this.intCa = super.defaultReadCerts(INT_CA_DIR_NAME);
        this.intCert = super.defaultReadCerts(INT_DIR_NAME);
        this.extCa = super.defaultReadCerts(EXT_CA_DIR_NAME);
        this.extCert = super.defaultReadCerts(EXT_DIR_NAME);
    }

    @Override
    public void installCerts(KubernetesClient kubeClient) throws DefaultScenarioException
    {
        try
        {
            var installer = Utils.getNetconfInstaller(this.config.getAdmin(), kubeClient);
            installer.installCertificateAuthorityAtNetconf(INT_LIST_NAME, "CA_cert_1", this.intCa);
            installer.installCertificateAuthorityAtNetconf(EXT_LIST_NAME, "CA_cert_1", this.extCa);
            installer.installCertificateAtNetconf(INT_KEY_NAME, INT_CERT_NAME, this.intCert);
            installer.installCertificateAtNetconf(EXT_KEY_NAME, EXT_CERT_NAME, this.extCert);
        }
        catch (CertificateInstallationException | NetconfClientException e)
        {
            throw new DefaultScenarioException("SEPP worker certificate installation error.", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.sc.supreme.defaultscenario.DefaultScenario#createCerts()
     */
    @Override
    public void createCerts(GeneratedCert ca)
    {
        // SEPP Worker uses its own root ca
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
        if (generate)
        {
            this.createCerts();
        }
        else
        {
            this.readCerts();
        }

        if (kubeClient.isPresent())
        {
            this.installCerts(kubeClient.get());
        }

        if (nextProcessor != null)
        {
            nextProcessor.process(generate, kubeClient, commonCa);
        }
        // last item in chain
    }
}
