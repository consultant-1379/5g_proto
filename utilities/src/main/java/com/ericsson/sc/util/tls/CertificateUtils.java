/** 
 * COPYRIGHT ERICSSON GMBH 2019
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Oct 21, 2021
 *     Author: echfari
 */
package com.ericsson.sc.util.tls;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import io.vertx.core.net.impl.pkcs1.PrivateKeyParser;

/**
 * Contains certificate/key PEM file utility method(s).
 */
public final class CertificateUtils
{

    private static final Pattern KEY_PATTERN = Pattern.compile("-+BEGIN\\s+(.*\\s+)?PRIVATE\\s+KEY[^-]*-+\\s+" + // Header
                                                               "([a-z0-9+/=\\r\\n]+)" + // Base64 text
                                                               "-+END\\s+.*PRIVATE\\s+KEY[^-]*-+", // Footer
                                                               Pattern.CASE_INSENSITIVE);
    private static final Pattern CERT_PATTERN = Pattern.compile("-+BEGIN\\s+.*CERTIFICATE[^-]*-+\\s+" + // Header
                                                                "([a-z0-9+/=\\r\\n]+)" + // Base64 text
                                                                "-+END\\s+.*CERTIFICATE[^-]*-+", // Footer
                                                                Pattern.CASE_INSENSITIVE);

    private CertificateUtils()
    {
    }

    /**
     * Generates X509Certificate array from a PEM file. The PEM file should contain
     * one or more items in Base64 encoding, each with plain-text headers and
     * footers (e.g. -----BEGIN CERTIFICATE----- and -----END CERTIFICATE-----).
     *
     * @param inputStream is a {@link InputStream} from the certificate files
     */
    public static X509Certificate[] getX509Certificates(String in) throws CertificateException
    {

        Collection<? extends Certificate> certs = parseCertificates(readCertificates(in));
        return certs.toArray(new X509Certificate[0]);
    }

    public static PrivateKey getPrivateKey(String pemKey) throws NoSuchAlgorithmException, InvalidKeySpecException, KeyException
    {
        final var m = KEY_PATTERN.matcher(pemKey);
        if (!m.find())
        {
            throw new KeyException("could not find a PKCS #8 or PKCS #1 private key in input stream"
                                   + " (see https://netty.io/wiki/sslcontextbuilder-and-private-key.html for more information)");
        }
        byte[] decodedKeyBytes;
        try
        {
            decodedKeyBytes = java.util.Base64.getMimeDecoder().decode(m.group(2));
        }
        catch (Exception e)
        {
            throw new KeyException("Could not decode PEM PKCS#8 or PKCS#1 private key");
        }
        if (m.group(1) != null && m.group(1).contains("RSA"))
        {
            // FIXME We should probably NOT support non PKCS9 PEM, although Vertx is more
            // flexible
            return parsePkcs1PrivateKey(decodedKeyBytes);
        }
        else
        {
            final var keySpec = new PKCS8EncodedKeySpec(decodedKeyBytes);
            return parsePkcs8PrivateKey(keySpec);
        }
    }

    private static List<byte[]> readCertificates(String in) throws CertificateException
    {
        final var certs = new ArrayList<byte[]>();
        final var matcher = CERT_PATTERN.matcher(in);
        var start = 0;
        for (;;)
        {
            if (!matcher.find(start))
            {
                break;
            }
            final var derCert = Base64.getMimeDecoder().decode(matcher.group(1));
            certs.add(derCert);

            start = matcher.end();
        }

        if (certs.isEmpty())
        {
            throw new CertificateException("found no certificates in input stream");
        }

        return certs;
    }

    private static List<X509Certificate> parseCertificates(List<byte[]> certs) throws CertificateException
    {
        final var cf = CertificateFactory.getInstance("X.509");
        final var result = new ArrayList<X509Certificate>();
        for (final var cert : certs)
        {
            try (final var bais = new ByteArrayInputStream(cert))
            {
                result.add((X509Certificate) cf.generateCertificate(bais));
            }
            catch (IOException e)
            {
                throw new CertificateException("Unexpected exception", e);
            }
        }
        return result;
    }

    private static PrivateKey parsePkcs1PrivateKey(byte[] derBytes) throws InvalidKeySpecException, NoSuchAlgorithmException
    {
        final var keySpec = PrivateKeyParser.getRSAKeySpec(derBytes);
        return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
    }

    private static PrivateKey parsePkcs8PrivateKey(PKCS8EncodedKeySpec keySpec) throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        try
        {
            return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
        }
        catch (InvalidKeySpecException ignore)
        {
            try
            {
                return KeyFactory.getInstance("DSA").generatePrivate(keySpec);
            }
            catch (InvalidKeySpecException ignore2)
            {
                try
                {
                    return KeyFactory.getInstance("EC").generatePrivate(keySpec);
                }
                catch (InvalidKeySpecException e)
                {
                    throw new InvalidKeySpecException("Neither RSA, DSA nor EC worked", e);
                }
            }
        }
    }
}
