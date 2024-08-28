package com.ericsson.supreme.api;

import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Optional;

import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.operator.OperatorCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.supreme.exceptions.CertificateCreationException;
import com.ericsson.supreme.exceptions.CertificateIOException;
import com.ericsson.supreme.kernel.CertificateIO;
import com.ericsson.supreme.kernel.CertificateTool;

public class CertificateGenerator
{
    private static final Logger log = LoggerFactory.getLogger(CertificateGenerator.class);

    private CertificateGenerator()
    {
    }

    public static GeneratedCert createSelfSignedCertificate(String name,
                                                            Integer bits,
                                                            Integer expirationDays,
                                                            String commonName,
                                                            List<String> sans) throws CertificateCreationException
    {
        log.info("Creating self-signed certificate for {}", name);
        try
        {
            return CertificateTool.createSelfSignedCertificate(name, bits, expirationDays, commonName, sans);
        }
        catch (NoSuchAlgorithmException | OperatorCreationException | CertificateException | CertificateIOException | CertIOException e)
        {
            throw new CertificateCreationException("Failed to create self-signed certificate for " + name, e);
        }
    }

    public static GeneratedCert createSelfSignedCertificate(String name,
                                                            Integer expirationDays,
                                                            String commonName,
                                                            List<String> sans) throws CertificateCreationException
    {
        return CertificateGenerator.createSelfSignedCertificate(name, 3072, expirationDays, commonName, sans);
    }

    public static GeneratedCert createCertificateSignedByRoot(String name,
                                                              Integer bits,
                                                              Integer expirationDays,
                                                              String commonName,
                                                              List<String> sans,
                                                              GeneratedCert ca) throws CertificateCreationException
    {
        log.info("Creating certificate for {} signed by {}", name, ca.getName());
        try
        {
            return CertificateTool.createCertificateSignedByRoot(name, bits, expirationDays, commonName, sans, ca);

        }
        catch (NoSuchAlgorithmException | OperatorCreationException | CertificateException | CertIOException | CertificateIOException e)
        {
            throw new CertificateCreationException("Failed to create certificate for " + name, e);
        }
    }

    public static GeneratedCert createCertificateSignedByRoot(String name,
                                                              Integer expirationDays,
                                                              String commonName,
                                                              List<String> sans,
                                                              GeneratedCert ca) throws CertificateCreationException
    {
        return CertificateGenerator.createCertificateSignedByRoot(name, 3072, expirationDays, commonName, sans, ca);
    }

    public static GeneratedCert createCertificateAuthority(String name,
                                                           Integer bits,
                                                           Integer expirationDays,
                                                           String commonName) throws CertificateCreationException
    {
        log.info("Creating certificate authority for {}", name);
        try
        {
            return CertificateTool.createCertificateAuthority(name, bits, expirationDays, commonName);
        }
        catch (NoSuchAlgorithmException | OperatorCreationException | CertificateException | CertificateIOException | CertIOException e)
        {
            throw new CertificateCreationException("Failed to create certificate authority for " + name, e);
        }

    }

    public static GeneratedCert createCertificateAuthority(String name,
                                                           Integer expirationDays,
                                                           String commonName) throws CertificateCreationException
    {
        return CertificateGenerator.createCertificateAuthority(name, 3072, expirationDays, commonName);
    }

    public static Optional<GeneratedCert> importGeneratedCertificate(String name,
                                                                     Path path) throws CertificateIOException
    {
        return CertificateIO.readGeneratedCert(name, path);
    }

    public static Optional<GeneratedCert> importGeneratedCertificate(String name,
                                                                     String privateKey,
                                                                     String certificate) throws CertificateIOException
    {
        return CertificateIO.readGeneratedCert(name, privateKey, certificate);
    }

    public static void exportGeneratedCertificate(GeneratedCert cert,
                                                  Path path) throws CertificateIOException
    {
        CertificateIO.exportCertificate(cert, path);
    }
}
