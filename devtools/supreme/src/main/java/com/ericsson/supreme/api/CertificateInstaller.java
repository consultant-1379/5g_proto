package com.ericsson.supreme.api;

import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.supreme.exceptions.CertificateIOException;
import com.ericsson.supreme.exceptions.CertificateInstallationException;
import com.ericsson.supreme.exceptions.KubernetesClientException;
import com.ericsson.supreme.exceptions.NetconfClientException;
import com.ericsson.supreme.kernel.KubernetesClient;
import com.ericsson.supreme.kernel.NetconfClient;
import com.ericsson.supreme.kernel.Utils;

public class CertificateInstaller
{
    private NetconfClient netconfClient;
    private KubernetesClient kubeClient;
    private static final Logger log = LoggerFactory.getLogger(CertificateInstaller.class);

    public CertificateInstaller()
    {
        // empty installer
    }

    public CertificateInstaller withNetconfConfiguration(NetconfClient netconfClient)
    {
        this.netconfClient = netconfClient;
        return this;
    }

    public CertificateInstaller withNetconfConfiguration(String ip,
                                                         int port,
                                                         String username,
                                                         String password)
    {
        this.netconfClient = new NetconfClient(ip, port, username, password);
        return this;
    }

    public CertificateInstaller withKubernetesConfig(KubernetesClient kubeClient)
    {
        this.kubeClient = kubeClient;
        return this;
    }

    public boolean installCertificateAtTlsSecret(String secretName,
                                                 GeneratedCert certificate) throws CertificateInstallationException
    {
        log.info("Secret certificate install: name {}", secretName);
        Objects.requireNonNull(this.kubeClient);

        try
        {
            if (this.kubeClient.secretExists(secretName))
            {
                log.debug("Secret exists. Deleting it first");
                this.kubeClient.deleteSecret(secretName);
            }
            this.kubeClient.exportCertToTlsSecret(Utils.toPemString(certificate.getX509Certificate()),
                                                  Utils.toPemString(certificate.getPrivateKeyPrivateKey()),
                                                  secretName);
        }
        catch (KubernetesClientException | CertificateIOException e)
        {
            throw new CertificateInstallationException("Installation of secret " + secretName + " for certificate " + certificate.getName() + " has failed", e);
        }

        return true;
    }

    public boolean installCertificateAtGenericSecret(String secretName,
                                                     GeneratedCert ca,
                                                     GeneratedCert certificate) throws CertificateInstallationException, CertificateIOException
    {
        var map = Map.of("key.pem",
                         Utils.toPemString(certificate.getPrivateKeyPrivateKey()),
                         "cert.pem",
                         Utils.toPemString(certificate.getX509Certificate()),
                         "rootCA.crt",
                         Utils.toPemString(ca.getX509Certificate()));
        return installCertificateAtGenericSecret(secretName, map);
    }

    public boolean installCertificateAtGenericSecret(String secretName,
                                                     GeneratedCert ca1,
                                                     GeneratedCert ca2,
                                                     GeneratedCert certificate) throws CertificateInstallationException, CertificateIOException
    {
        var map = Map.of("key.pem",
                         Utils.toPemString(certificate.getPrivateKeyPrivateKey()),
                         "cert.pem",
                         Utils.toPemString(certificate.getX509Certificate()),
                         "rootCA.crt",
                         Utils.toPemString(ca1.getX509Certificate()) + Utils.toPemString(ca2.getX509Certificate()));
        return installCertificateAtGenericSecret(secretName, map);
    }

    public boolean installCertificateAtGenericSecret(String secretName,
                                                     Map<String, String> dataMap) throws CertificateInstallationException
    {
        log.info("Secret certificate install: name {}", secretName);
        Objects.requireNonNull(this.kubeClient);

        try
        {
            if (this.kubeClient.secretExists(secretName))
            {
                log.debug("Secret exists. Deleting it first");
                this.kubeClient.deleteSecret(secretName);
            }

            this.kubeClient.exportCertToGenericSecret(dataMap, secretName);
        }
        catch (KubernetesClientException e)
        {
            throw new CertificateInstallationException("Installation of secret " + secretName, e);
        }

        return true;
    }

    public boolean installCertificateAuthorityAtSecret(String secretName,
                                                       GeneratedCert certificate) throws CertificateInstallationException
    {
        log.info("Secret ca install: name {}", secretName);
        Objects.requireNonNull(this.kubeClient);

        try
        {
            if (this.kubeClient.secretExists(secretName))
            {
                log.debug("Secret exists. Deleting it first");
                this.kubeClient.deleteSecret(secretName);
            }
            this.kubeClient.exportCaToSecret(Utils.toPemString(certificate.getX509Certificate()), secretName);
        }
        catch (KubernetesClientException | CertificateIOException e)
        {
            throw new CertificateInstallationException("Installation of secret " + secretName + " for certificate authority " + certificate.getName()
                                                       + " has failed",
                                                       e);
        }

        return true;

    }

    public boolean installCertificateAtNetconf(String keystoreAsymKeyName,
                                               String keystoreCertificateName,
                                               GeneratedCert certificate) throws CertificateInstallationException
    {
        log.info("Netconf install: asymKey {}, certificateName {}", keystoreAsymKeyName, keystoreCertificateName);
        Objects.requireNonNull(this.netconfClient);

        try
        {
            return this.netconfClient.installCertificate(keystoreAsymKeyName, keystoreCertificateName, certificate);
        }
        catch (NetconfClientException e)
        {
            throw new CertificateInstallationException("Installation of asymKey " + keystoreAsymKeyName + " and certificateName " + keystoreCertificateName
                                                       + " has failed",
                                                       e);
        }
    }

    public boolean installCertificateAuthorityAtNetconf(String truststoreCaListName,
                                                        String truststoreCaName,
                                                        GeneratedCert certificate) throws CertificateInstallationException
    {
        log.info("Netconf install: caListName {}, caName {}", truststoreCaListName, truststoreCaName);
        Objects.requireNonNull(this.netconfClient);

        var res1 = true;
        var res2 = true;

        try
        {
            res1 = this.netconfClient.installCaList(truststoreCaListName);
        }
        catch (NetconfClientException e)
        {
            throw new CertificateInstallationException("Installation of certificate authority list " + truststoreCaListName + " has failed", e);
        }

        try
        {
            res2 = this.netconfClient.installCaItem(truststoreCaListName, truststoreCaName, certificate);
        }
        catch (NetconfClientException e)
        {
            throw new CertificateInstallationException("Installation of certificate authority " + truststoreCaName + " has failed", e);

        }

        return res1 && res2;
    }

}
