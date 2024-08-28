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
 * Created on: Oct 19, 2022
 *     Author: echaias
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
import com.ericsson.supreme.exceptions.NetconfClientException;
import com.ericsson.supreme.kernel.KubernetesClient;
import com.ericsson.supreme.kernel.Utils;
import com.ericsson.utilities.common.Pair;

/*-
 * Modify certificate generation for the N32C scenario. The topology is as
 * follows:
 * c,  p  -> Signed by the rootCA
 * p1, p2 -> belong to RP1. signed with the rp1 ca
 * p3, p4, p5 -> belong to RP2, signed with the rp2 ca
 * p6, p7, p8 -> belong to RP3, signed with the rp3 ca
 */
public class SeppsimN32c extends AbstractDefaultScenario implements DefaultScenario, Processor
{
    private List<Pair<String, GeneratedCert>> certs = new ArrayList<>();
    private Processor nextProcessor;
    private GeneratedCert ca;

    private GeneratedCert rp1Ca;
    private GeneratedCert rp2Ca;
    private GeneratedCert rp3Ca;

    private static final String CERTS_PARENT_DIR = "seppsim-n32c/";
    private static final String EXT_CA_DIR_NAME = "seppwrk-ext-ca";
    private static final String RP1_CA_LIST_NAME = "sc-traf-root-ca-rp1";
    private static final String RP2_CA_LIST_NAME = "sc-traf-root-ca-rp2";
    private static final String RP3_CA_LIST_NAME = "sc-traf-root-ca-rp3";

    private static final String SECRET_NAME = "seppsim-certificates";

    private static final Map<String, List<Pair<String, String>>> entries = Map.of("rootca",
                                                                                  List.of(Pair.of("eric-seppsim-c", "c"), Pair.of("eric-seppsim-p", "p")),
                                                                                  "rp1",
                                                                                  List.of(Pair.of("*.5gc.mnc033.mcc206.3gppnetwork.org", "p1"),
                                                                                          Pair.of("*.5gc.mnc033.mcc206.3gppnetwork.org", "p2")),
                                                                                  "rp2",
                                                                                  List.of(Pair.of("*.5gc.mnc345.mcc543.3gppnetwork.org", "p3"),
                                                                                          Pair.of("*.5gc.mnc345.mcc543.3gppnetwork.org", "p4"),
                                                                                          Pair.of("*.5gc.mnc345.mcc543.3gppnetwork.org", "p5")),
                                                                                  "rp3",
                                                                                  List.of(Pair.of("*.5gc.mnc678.mcc876.3gppnetwork.org", "p6"),
                                                                                          Pair.of("*.5gc.mnc678.mcc876.3gppnetwork.org", "p7"),
                                                                                          Pair.of("*.5gc.mnc678.mcc876.3gppnetwork.org", "p8")));

    /**
     * @param config
     */
    public SeppsimN32c(Configuration config)
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
    public void createCerts(GeneratedCert ca)
    {

    }

    @Override
    public void installCerts(KubernetesClient kubeClient) throws DefaultScenarioException
    {
        try
        {
            // first install the sc-traf-root-ca-rp[1,2,3] on truststore
            var netconfInstaller = Utils.getNetconfInstaller(this.config.getAdmin(), kubeClient);
            netconfInstaller.installCertificateAuthorityAtNetconf(RP1_CA_LIST_NAME, "CA_cert_rp1", this.rp1Ca);
            netconfInstaller.installCertificateAuthorityAtNetconf(RP2_CA_LIST_NAME, "CA_cert_rp2", this.rp2Ca);
            netconfInstaller.installCertificateAuthorityAtNetconf(RP3_CA_LIST_NAME, "CA_cert_rp3", this.rp3Ca);

            var installer = Utils.getKubernetesInstaller(kubeClient);
            // these secrets are mounted on seppsims which need the CA used to generate the
            // certificate
            // presented by envoy towards the upstream n32c clusters
            // This has been made by SeppWorker default scenario

            var extCa = super.defaultReadCerts(EXT_CA_DIR_NAME);

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
                                 Utils.toPemString(extCa.getX509Certificate()));
                installer.installCertificateAtGenericSecret(secretName, map);
            }
        }
        catch (CertificateInstallationException | CertificateIOException e)
        {
            throw new DefaultScenarioException(CERTS_PARENT_DIR + " certificate installation error.", e);
        }
        catch (NetconfClientException e)
        {
            throw new DefaultScenarioException("RP trusted CA list installation error.", e);

        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.sc.supreme.defaultscenario.DefaultScenario#createCerts()
     */
    @Override
    public void createCerts() throws DefaultScenarioException
    {
        this.rp1Ca = super.defaultCreateCa("rp1ca", "rp1ca");
        this.rp2Ca = super.defaultCreateCa("rp2ca", "rp2ca");
        this.rp3Ca = super.defaultCreateCa("rp3ca", "rp3ca");

        for (var entry : entries.get("rp1"))
        {
            var names = getCnSanPair(entry);
            this.certs.add(Pair.of(entry.getSecond(),
                                   super.defaultCreateCerts(CERTS_PARENT_DIR + entry.getSecond(), names.getFirst(), names.getSecond(), rp1Ca)));
        }

        for (var entry : entries.get("rp2"))
        {
            var names = getCnSanPair(entry);
            this.certs.add(Pair.of(entry.getSecond(),
                                   super.defaultCreateCerts(CERTS_PARENT_DIR + entry.getSecond(), names.getFirst(), names.getSecond(), rp2Ca)));
        }

        for (var entry : entries.get("rp3"))
        {
            var names = getCnSanPair(entry);
            this.certs.add(Pair.of(entry.getSecond(),
                                   super.defaultCreateCerts(CERTS_PARENT_DIR + entry.getSecond(), names.getFirst(), names.getSecond(), rp3Ca)));
        }

        for (var entry : entries.get("rootca"))
        {

            this.certs.add(Pair.of(entry.getSecond(),
                                   super.defaultCreateCerts(CERTS_PARENT_DIR + entry.getSecond(), entry.getFirst(), List.of(entry.getFirst()), ca)));
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
        // TODO
    }

    private static Pair<String, List<String>> getCnSanPair(Pair<String, String> p)
    {
        // CN is seppsim-p1.5gc.mnc012.mcc210.3gppnetwork.org
        var cn = new StringBuilder("seppsim-").append(p.getSecond()).append(p.getFirst().startsWith("*") ? p.getFirst().substring(1) : p.getFirst()).toString();
        var sans = List.of(cn, p.getSecond());
        return Pair.of(cn, sans);

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

        // only generate for now
        this.ca = commonCa;
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

        if (this.nextProcessor != null)
        {
            this.nextProcessor.process(generate, kubeClient, commonCa);
        }

    }
}
