package com.ericsson.supreme.kernel;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.supreme.api.CertificateInstaller;
import com.ericsson.supreme.api.GeneratedCert;
import com.ericsson.supreme.config.Admin;
import com.ericsson.supreme.exceptions.CertificateIOException;
import com.ericsson.supreme.exceptions.KubernetesClientException;
import com.ericsson.supreme.exceptions.NetconfClientException;

public class Utils
{
    static
    {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    private Utils()
    {
    }

    /**
     * Converts a string instance into a Base-64 encoded string
     */
    public static String toBase64(String input)
    {
        return Base64.getEncoder().encodeToString(input.getBytes());
    }

    public static String toBase64(byte[] input)
    {
        return Base64.getEncoder().encodeToString(input);
    }

    public static String toPemString(Certificate cert) throws CertificateIOException
    {
        var writer = new StringWriter();

        try (var jcaPEMWriter = new JcaPEMWriter(writer))
        {
            jcaPEMWriter.writeObject(cert);
            jcaPEMWriter.flush();

            return writer.toString();
        }
        catch (IOException e)
        {
            throw new CertificateIOException("Cannot convert certificate to pem format.", e);
        }
    }

    public static String toPemString(PrivateKey key) throws CertificateIOException
    {
        var writer = new StringWriter();
        try (var jcaPEMWriter = new JcaPEMWriter(writer))
        {
            jcaPEMWriter.writeObject(key);
            jcaPEMWriter.flush();

            return writer.toString();
        }
        catch (IOException e)
        {
            throw new CertificateIOException("Cannot convert private key to pem format.", e);
        }
    }

    public static List<String> getSubjectAltNames(X509Certificate certificate)
    {
        List<String> result = new ArrayList<>();
        try
        {
            Collection<?> subjectAltNames = certificate.getSubjectAlternativeNames();
            if (subjectAltNames == null)
            {
                return Collections.emptyList();
            }
            for (Object subjectAltName : subjectAltNames)
            {
                List<?> entry = (List<?>) subjectAltName;
                if (entry != null && entry.size() > 1)
                {
                    Integer altNameType = (Integer) entry.get(0);
                    if (altNameType != null && (altNameType == 2 || altNameType == 7))
                    {
                        String altName = (String) entry.get(1);
                        if (altName != null)
                        {
                            result.add(altName);
                        }
                    }
                }

            }
            return result;
        }
        catch (CertificateParsingException e)
        {
            return Collections.emptyList();
        }
    }

    /**
     * Gets the key length of supported keys
     * 
     * @param pk PublicKey used to derive the keysize
     * @return -1 if key is unsupported, otherwise a number >= 0. 0 usually means
     *         the length can not be calculated, for example if the key is an EC key
     *         and the "implicitlyCA" encoding is used.
     */
    public static int getKeyLength(final PublicKey pk)
    {
        int len = -1;
        if (pk instanceof RSAPublicKey)
        {
            final RSAPublicKey rsapub = (RSAPublicKey) pk;
            len = rsapub.getModulus().bitLength();
        }
        return len;
    }

    public static Path createDirs(Path targetDir) throws CertificateIOException
    {
        try
        {
            return Files.createDirectories(targetDir);
        }
        catch (IOException e)
        {
            throw new CertificateIOException("Could not create directory " + targetDir, e);
        }
    }

    public static byte[] toPkcs12(GeneratedCert cert) throws CertificateIOException
    {
        // --- create a new pkcs12 key store in memory
        try (ByteArrayOutputStream p12 = new ByteArrayOutputStream())
        {
            var pkcs12 = KeyStore.getInstance("PKCS12", CertificateTool.BC_PROVIDER);
            pkcs12.load(null, null);

            // --- create entry in PKCS12
            pkcs12.setKeyEntry(CertificateTool.PASSWORD,
                               cert.getPrivateKeyPrivateKey(),
                               CertificateTool.PASSWORD.toCharArray(),
                               new Certificate[] { cert.getX509Certificate() });

            pkcs12.store(p12, CertificateTool.PASSWORD.toCharArray());
            return p12.toByteArray();
        }
        catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException | NoSuchProviderException e)
        {
            throw new CertificateIOException("Cannot convert certificate " + cert.getName() + " to pkcs12 format.", e);
        }
    }

    public static boolean isCa(GeneratedCert cert)
    {
        return cert.getX509Certificate().getBasicConstraints() != -1;
    }

    public static CertificateInstaller getGenericInstaller(Admin admin,
                                                           KubernetesClient kubeClient) throws NetconfClientException
    {
        var installer = new CertificateInstaller();

        if (admin.getYangProvider() != null)
        {
            var yangProvider = admin.getYangProvider();
            var client = yangProvider.getIp() != null && yangProvider.getPort() != 0
                                                                                     ? new NetconfClient(yangProvider.getIp(),
                                                                                                         yangProvider.getPort(),
                                                                                                         yangProvider.getUsername(),
                                                                                                         yangProvider.getPassword())
                                                                                     : new NetconfClient(kubeClient,
                                                                                                         yangProvider.getUsername(),
                                                                                                         yangProvider.getPassword());
            installer.withNetconfConfiguration(client);
        }

        installer.withKubernetesConfig(kubeClient);

        return installer;
    }

    public static CertificateInstaller getNetconfInstaller(Admin admin,
                                                           KubernetesClient kubeClient) throws NetconfClientException
    {
        var installer = new CertificateInstaller();

        if (admin.getYangProvider() != null)
        {
            var yangProvider = admin.getYangProvider();
            var client = yangProvider.getIp() != null && yangProvider.getPort() != 0
                                                                                     ? new NetconfClient(yangProvider.getIp(),
                                                                                                         yangProvider.getPort(),
                                                                                                         yangProvider.getUsername(),
                                                                                                         yangProvider.getPassword())
                                                                                     : new NetconfClient(kubeClient,
                                                                                                         yangProvider.getUsername(),
                                                                                                         yangProvider.getPassword());
            installer.withNetconfConfiguration(client);
        }
        else
        {
            throw new NetconfClientException("No yang provider info defined in properties file");
        }

        return installer;
    }

    public static CertificateInstaller getKubernetesInstaller(KubernetesClient kubeClient)
    {
        return new CertificateInstaller().withKubernetesConfig(kubeClient);
    }

    public static KubernetesClient getKubernetesClient(String namespace,
                                                       String kubeconfig) throws KubernetesClientException
    {
        return kubeconfig != null ? new KubernetesClient(namespace, kubeconfig) : new KubernetesClient(namespace);
    }

}
