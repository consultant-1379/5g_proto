package com.ericsson.supreme.kernel;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Optional;

import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JcaPKCS8Generator;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCSException;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.supreme.api.GeneratedCert;
import com.ericsson.supreme.exceptions.CertificateIOException;

public class CertificateIO
{
    private CertificateIO()
    {
    }

    protected static final Logger log = LoggerFactory.getLogger(CertificateIO.class);

    /* ================ Public methods ================ */

    /**
     * Export the input certificate in the selected path as PEM formated file. The
     * certificate is stored as cert.pem and the private key as key.pem
     * 
     * @param cert the certificate to be stored
     * @param path the path of the folder to store the PEM formated files
     * @throws CertificateIOException
     * @throws IOException
     */
    public static void exportCertificate(GeneratedCert cert,
                                         Path path) throws CertificateIOException
    {
        exportCertificate(cert, path, path);
    }

    /**
     * Read a certificate and private key and store them as {@link GenertedCert}
     * 
     * @param path - The path of the folder that contains key.pem cert.pem
     * @return GeneratedCert - Empty optional if the folder or key.pem or cert.pem
     *         do not exist, otherwise the GeneratedCert
     * @throws CertificateIOException
     * @throws CertificateException      - if cert.pem is invalid
     * @throws NoSuchAlgorithmException  - if key.pem is invalid
     * @throws InvalidKeySpecException   - if key.pem is invalid
     * @throws IOException               - if key.pem or cert.pem are missing
     * @throws PKCSException
     * @throws OperatorCreationException
     * @throws KeyStoreException
     */
    public static Optional<GeneratedCert> readGeneratedCert(String name,
                                                            Path path) throws CertificateIOException
    {
        log.debug("Trying to read path {}", path);
        var key = path.resolve(CertificateTool.DEFAULT_KEY_NAME).toAbsolutePath();
        var cert = path.resolve(CertificateTool.DEFAULT_CERT_NAME).toAbsolutePath();

        if (!Files.exists(path) || !Files.exists(key) || !Files.exists(cert))
        {
            log.debug("Path does not exist");
            return Optional.empty();
        }
        log.info("Reading existing certificate {}", name);
        return Optional.of(readGeneratedCert(name, key, cert));
    }

    /**
     * Read a certificate and private key and store them as {@link GenertedCert}
     * 
     * @param privatekey  - The contents of cert.pem
     * @param certificate - The contents of cert.pem
     * @return GeneratedCert
     * @throws CertificateIOException
     */
    public static Optional<GeneratedCert> readGeneratedCert(String name,
                                                            String privateKey,
                                                            String certificate) throws CertificateIOException
    {
        var x509cert = readCertificate(certificate);
        var rsaPrivateKey = readPrivateKey(privateKey);
        return Optional.of(new GeneratedCert(name, rsaPrivateKey, x509cert));
    }

    /* ================ Private methods ================ */

    /**
     * Export private key in PKCS#8 format
     * 
     * @param privKey
     * @param path
     * @throws CertificateIOException
     * @throws IOException
     */
    private static void exportPrivateKey(PrivateKey privKey,
                                         Path path) throws CertificateIOException
    {

        var absPath = Utils.createDirs(path.toAbsolutePath());

        try (var jcaPEMWriter = new JcaPEMWriter(new FileWriter(absPath.resolve(CertificateTool.DEFAULT_KEY_NAME).toString())))
        {
            jcaPEMWriter.writeObject(new JcaPKCS8Generator(privKey, null));
        }
        catch (IOException e)
        {
            throw new CertificateIOException("Could not export private key to path " + path.toString(), e);
        }

    }

    private static void exportCertificate(X509Certificate cert,
                                          Path path) throws CertificateIOException
    {
        var absPath = Utils.createDirs(path.toAbsolutePath());

        try (var jcaPEMWriter = new JcaPEMWriter(new FileWriter(absPath.resolve(CertificateTool.DEFAULT_CERT_NAME).toString())))
        {
            jcaPEMWriter.writeObject(cert);
        }
        catch (IOException e)
        {
            throw new CertificateIOException("Could not export certificate to path " + path.toString(), e);
        }
    }

    private static void exportPrivateKeyCertificate(PrivateKey privKey,
                                                    X509Certificate cert,
                                                    Path path) throws CertificateIOException
    {
        var absPath = Utils.createDirs(path.toAbsolutePath());

        try (var jcaPEMWriter = new JcaPEMWriter(new FileWriter(absPath.resolve(CertificateTool.DEFAULT_CERT_KEY_NAME).toString())))
        {
            jcaPEMWriter.writeObject(cert);
            jcaPEMWriter.writeObject(privKey);
        }
        catch (IOException e)
        {
            throw new CertificateIOException("Could not export key and certificate to path " + path.toString(), e);
        }
    }

    private static void exportPkcs12(byte[] pkcs12,
                                     Path path) throws CertificateIOException
    {
        var absPath = Utils.createDirs(path.toAbsolutePath());

        try (FileOutputStream pkcs12Writer = new FileOutputStream(absPath.resolve(CertificateTool.DEFAULT_PKCS12_NAME).toString()))
        {
            pkcs12Writer.write(pkcs12);
        }
        catch (IOException e)
        {
            throw new CertificateIOException("Could not export pkcs12 certificate to path " + path.toString(), e);
        }
    }

    private static void exportPkcs12(String pkcs12InBase64Format,
                                     Path path) throws CertificateIOException
    {
        var absPath = Utils.createDirs(path.toAbsolutePath());

        try (FileWriter pkcs12Writer = new FileWriter(absPath.resolve(CertificateTool.DEFAULT_PKCS12_BASE64_NAME).toString()))
        {
            pkcs12Writer.write(pkcs12InBase64Format);
        }
        catch (IOException e)
        {
            throw new CertificateIOException("Could not export pkcs12 certificate in base64 format to path " + path.toString(), e);
        }
    }

    private static void exportCertificate(GeneratedCert cert,
                                          Path keyPath,
                                          Path certPath) throws CertificateIOException
    {
        exportPrivateKey(cert.getPrivateKeyPrivateKey(), keyPath);
        exportCertificate(cert.getX509Certificate(), certPath);
        exportPrivateKeyCertificate(cert.getPrivateKeyPrivateKey(), cert.getX509Certificate(), certPath);
        exportPkcs12(cert.getPkcs12(), certPath);
        exportPkcs12(cert.getPkcs12Base64Format(), certPath);
    }

    private static GeneratedCert readGeneratedCert(String name,
                                                   Path keyPath,
                                                   Path certPath) throws CertificateIOException
    {
        var x509cert = readCertificate(certPath);
        var rsaPrivateKey = readPrivateKey(keyPath);
        return new GeneratedCert(name, rsaPrivateKey, x509cert);
    }

    private static X509Certificate readCertificate(Path cert) throws CertificateIOException
    {
        try (InputStream in = new FileInputStream(cert.toFile()))
        {
            var factory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) factory.generateCertificate(in);
        }
        catch (CertificateException | IOException e)
        {
            throw new CertificateIOException("Unable to read certificate at " + cert.toString(), e);
        }
    }

    private static X509Certificate readCertificate(String cert) throws CertificateIOException
    {
        try (InputStream in = new ByteArrayInputStream(cert.getBytes()))
        {
            var factory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) factory.generateCertificate(in);
        }
        catch (CertificateException | IOException e)
        {
            throw new CertificateIOException("Unable to read certificate at " + cert, e);
        }
    }

    private static PrivateKey readPrivateKey(Path privKeyPath) throws CertificateIOException
    {
        try
        {
            var key = Files.readString(privKeyPath);

            String privateKeyPEM = key.replace(CertificateTool.BEGIN_PRIVATE_KEY, "")
                                      .replaceAll(System.lineSeparator(), "")
                                      .replace(CertificateTool.END_PRIVATE_KEY, "");

            KeyFactory keyFactory = KeyFactory.getInstance(CertificateTool.KEY_ALGORITHM);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decode(privateKeyPEM));
            return keyFactory.generatePrivate(keySpec);
        }
        catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e)
        {
            throw new CertificateIOException("Unable to read private key at " + privKeyPath.toString(), e);
        }

    }

    private static PrivateKey readPrivateKey(String privKeyPath) throws CertificateIOException
    {
        try
        {
            String privateKeyPEM = privKeyPath.replace(CertificateTool.BEGIN_PRIVATE_KEY, "")
                                              .replaceAll(System.lineSeparator(), "")
                                              .replace(CertificateTool.END_PRIVATE_KEY, "");

            KeyFactory keyFactory = KeyFactory.getInstance(CertificateTool.KEY_ALGORITHM);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decode(privateKeyPEM));
            return keyFactory.generatePrivate(keySpec);
        }
        catch (NoSuchAlgorithmException | InvalidKeySpecException e)
        {
            throw new CertificateIOException("Unable to read private key at " + privKeyPath, e);
        }

    }
}
