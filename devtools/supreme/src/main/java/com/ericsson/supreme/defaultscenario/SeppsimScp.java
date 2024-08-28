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
import java.util.Map;
import java.util.Optional;

import com.ericsson.supreme.api.GeneratedCert;
import com.ericsson.supreme.config.Configuration;
import com.ericsson.supreme.exceptions.CertificateIOException;
import com.ericsson.supreme.exceptions.CertificateInstallationException;
import com.ericsson.supreme.exceptions.DefaultScenarioException;
import com.ericsson.supreme.kernel.KubernetesClient;
import com.ericsson.supreme.kernel.Utils;
import com.ericsson.utilities.common.Pair;

public final class SeppsimScp extends AbstractDefaultScenario implements DefaultScenario, Processor
{
    private List<Pair<String, GeneratedCert>> certs = new ArrayList<>();
    private Processor nextProcessor;
    private GeneratedCert ca;

    private static final String NAME = "seppsim-scp/";

    private static final String SECRET_NAME = "seppsim-certificates";

    private static final List<Pair<String, String>> entries = List.of(Pair.of("eric-seppsim", ""),
                                                                      Pair.of("eric-seppsim-c", "c"),
                                                                      Pair.of("eric-seppsim-p", "p"),
                                                                      Pair.of("eric-seppsim-p1-mcc-206-mnc-33", "p1"),
                                                                      Pair.of("eric-seppsim-p2-mcc-206-mnc-33", "p2"),
                                                                      Pair.of("eric-seppsim-p3-mcc-262-mnc-73", "p3"),
                                                                      Pair.of("eric-seppsim-p4-mcc-262-mnc-73", "p4"),
                                                                      Pair.of("eric-seppsim-p5-mcc-262-mnc-73", "p5"),
                                                                      Pair.of("eric-seppsim-p6-mcc-262-mnc-73", "p6"),
                                                                      Pair.of("eric-seppsim-p7-mcc-262-mnc-73", "p7"),
                                                                      Pair.of("eric-seppsim-p8-mcc-262-mnc-73", "p8"));

    /**
     * @param config
     */
    public SeppsimScp(Configuration config)
    {
        super(config);
    }

    @Override
    public void createCerts(GeneratedCert ca) throws DefaultScenarioException
    {
        for (var entry : entries)
        {
            this.certs.add(Pair.of(entry.getSecond(), super.defaultCreateCerts(NAME + entry.getSecond(), entry.getFirst(), List.of(entry.getFirst()), ca)));
        }
    }

    @Override
    public void installCerts(KubernetesClient kubeClient) throws DefaultScenarioException
    {
        try
        {
            var installer = Utils.getKubernetesInstaller(kubeClient);

            for (var cert : this.certs)
            {
                var secretName = SECRET_NAME;
                if (!cert.getFirst().isEmpty())
                {
                    secretName = secretName + "-" + cert.getFirst();
                }

                var c = cert.getSecond();

                var map = Map.of("key.pem",
                                 Utils.toPemString(c.getPrivateKeyPrivateKey()),
                                 "cert.pem",
                                 Utils.toPemString(c.getX509Certificate()),
                                 "rootCA.crt",
                                 Utils.toPemString(this.ca.getX509Certificate()));
                installer.installCertificateAtGenericSecret(secretName, map);
            }
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
        for (var entry : entries)
        {
            this.certs.add(Pair.of(entry.getSecond(), super.defaultReadCerts(NAME + entry.getSecond())));
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
}
