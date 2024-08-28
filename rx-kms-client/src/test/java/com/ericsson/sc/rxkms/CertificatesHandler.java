package com.ericsson.sc.rxkms;

import com.ericsson.supreme.api.CertificateGenerator;
import com.ericsson.supreme.api.CertificateInstaller;
import com.ericsson.supreme.api.GeneratedCert;
import com.ericsson.supreme.exceptions.CertificateCreationException;
import com.ericsson.supreme.exceptions.CertificateInstallationException;

import java.util.List;
import java.io.File;

public class CertificatesHandler
{

    private GeneratedCert commonCaCert;
    private static final Integer CERT_BITS = 3072;
    private static final Integer CERT_EXPIRATION_DAYS = 365;
    private static final String CA_CN = "trustedca";
    private FileHandlerTest fht;

    CertificatesHandler()
    {
        try
        {
            commonCaCert = CertificateGenerator.createCertificateAuthority("trustedcaname", CERT_BITS, CERT_EXPIRATION_DAYS, CA_CN);
        }
        catch (CertificateCreationException e)
        {
            e.printStackTrace();
        }
        this.fht = new FileHandlerTest();
    }

    CertificatesHandler(String cacn)
    {
        try
        {
            commonCaCert = CertificateGenerator.createCertificateAuthority("trustedcaname", CERT_BITS, CERT_EXPIRATION_DAYS, cacn);
        }
        catch (CertificateCreationException e)
        {
            e.printStackTrace();
        }
        this.fht = new FileHandlerTest();
    }

    public void writeCaCert(String filePath)
    {
        File certPath = new File(filePath);

        this.fht.createFile(certPath, this.commonCaCert.getCertificate());
    }

    public void createSignedCert(String filePathCert,
                                 String filePathKey,
                                 String cn,
                                 List<String> sans)
    {
        try
        {
            GeneratedCert cert = CertificateGenerator.createCertificateSignedByRoot("certname", CERT_BITS, CERT_EXPIRATION_DAYS, cn, sans, this.commonCaCert);
            File certFile = new File(filePathCert);
            File keyFile = new File(filePathKey);
            this.fht.createFile(certFile, cert.getCertificate());
            this.fht.createFile(keyFile, cert.getPrivateKey());
        }
        catch (CertificateCreationException e)
        {
            // TODO
            e.printStackTrace();
        }
    }

}
