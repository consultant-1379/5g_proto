package com.ericsson.supreme.kernel;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCSException;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.util.IPAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import com.ericsson.supreme.api.GeneratedCert;
import com.ericsson.supreme.exceptions.CertificateIOException;
import com.ericsson.utilities.common.Pair;

public class CertificateTool
{
    static
    {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }
    protected static final Logger log = LoggerFactory.getLogger(CertificateTool.class);

    private static final String SUBJECT = "C=GE, L=Aachen, O=Ericsson, OU=EDD, CN=";
    public static final String KEY_ALGORITHM = "RSA";
    public static final String BC_PROVIDER = "BC";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    public static final String DEFAULT_CERT_NAME = "cert.pem";
    public static final String DEFAULT_KEY_NAME = "key.pem";
    public static final String DEFAULT_CERT_KEY_NAME = "certKey.pem";
    public static final String DEFAULT_PKCS12_NAME = "container.p12";
    public static final String DEFAULT_PKCS12_BASE64_NAME = "container-base64.p12";
    public static final String DEFAULT_LOG_FILE_NAME = "cert-info.log";

    public static final String BEGIN_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----";
    public static final String END_PRIVATE_KEY = "-----END PRIVATE KEY-----";

    public static final String PASSWORD = "rootroot";

    private CertificateTool()
    {

    }

    private static X509Certificate signCertificate(X509v3CertificateBuilder certBuilder,
                                                   ContentSigner contentSigner) throws CertificateException
    {
        // Create a cert holder and export to X509Certificate
        var certHolder = certBuilder.build(contentSigner);
        return new JcaX509CertificateConverter().setProvider(BC_PROVIDER).getCertificate(certHolder);
    }

    private static ContentSigner createSigner(KeyPair keyPair) throws OperatorCreationException
    {
        return new JcaContentSignerBuilder(SIGNATURE_ALGORITHM).setProvider(BC_PROVIDER).build(keyPair.getPrivate());
    }

    private static void convertToCertificateAuthority(JcaX509v3CertificateBuilder caBuilder,
                                                      KeyPair keyPair) throws CertIOException, NoSuchAlgorithmException
    {
        // Add Extensions
        // A BasicConstraint to mark root certificate as CA certificate
        var certExtUtils = new JcaX509ExtensionUtils();
        caBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
        caBuilder.addExtension(Extension.subjectKeyIdentifier, false, certExtUtils.createSubjectKeyIdentifier(keyPair.getPublic()));
    }

    private static void convertToCertificate(X509v3CertificateBuilder certBuilder,
                                             List<String> sans) throws CertIOException
    {

        // Add Extensions
        // Use BasicConstraints to say that this Cert is not a CA
        certBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(false));

        List<ASN1Encodable> gn = new ArrayList<>();

        for (var san : sans)
        {
            if (IPAddress.isValidIPv6(san) || IPAddress.isValidIPv4(san))
                gn.add(new GeneralName(GeneralName.iPAddress, san));
            else if (san.contains("DirName"))
                gn.add(new GeneralName(new X500Name(san.replace("DirName:", ""))));
            else
                gn.add(new GeneralName(GeneralName.dNSName, san));
        }

        if (!gn.isEmpty())
        {
            // Add DNS name is cert is to used for SSL
            certBuilder.addExtension(Extension.subjectAlternativeName, false, new DERSequence(gn.toArray(new ASN1Encodable[0])));
        }
    }

    private static KeyPair generateRsaKeyPair(int bits) throws NoSuchAlgorithmException
    {
        // Initialize a new KeyPair generator
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        keyPairGenerator.initialize(bits);
        return keyPairGenerator.generateKeyPair();
    }

    private static Pair<Date, Date> createValidityPeriod(Integer expirationDays)
    {
        var validFrom = Instant.now();
        var validUntil = validFrom.plus(expirationDays, ChronoUnit.DAYS);

        return Pair.of(Date.from(validFrom), Date.from(validUntil));
    }

    /**
     * Generates an RSA key pair, creates a validity period, creates the certificate
     * and self-signs it since it's a root CA.
     * 
     * @param expirationDays
     * @param commonName
     * @return
     * @throws NoSuchAlgorithmException
     * @throws CertIOException
     * @throws NoSuchProviderException
     * @throws OperatorCreationException
     * @throws CertificateException
     * @throws IOException
     * @throws KeyStoreException
     * @throws CertificateIOException
     * @throws PKCSException
     */
    public static GeneratedCert createCertificateAuthority(String name,
                                                           int bits,
                                                           Integer expirationDays,
                                                           String commonName) throws NoSuchAlgorithmException, CertIOException, OperatorCreationException, CertificateException, CertificateIOException
    {
        var rootKeyPair = generateRsaKeyPair(bits);

        // Setup validity
        var validity = createValidityPeriod(expirationDays);

        // Issued By and Issued To same for root certificate
        var rootCertIssuer = new X500Name(SUBJECT + commonName);
        var rootCertSubject = rootCertIssuer;

        var rootCertBuilder = new JcaX509v3CertificateBuilder(rootCertIssuer,
                                                              new BigInteger(Long.toString(new SecureRandom().nextLong())),
                                                              validity.getFirst(),
                                                              validity.getSecond(),
                                                              rootCertSubject,
                                                              rootKeyPair.getPublic());

        convertToCertificateAuthority(rootCertBuilder, rootKeyPair);

        var rootCertContentSigner = createSigner(rootKeyPair);
        var crt = signCertificate(rootCertBuilder, rootCertContentSigner);

        return new GeneratedCert(name, rootKeyPair.getPrivate(), crt);
    }

    public static GeneratedCert createCertificateSignedByRoot(String name,
                                                              int bits,
                                                              Integer expirationDays,
                                                              String commonName,
                                                              List<String> sans,
                                                              GeneratedCert rootCert) throws NoSuchAlgorithmException, OperatorCreationException, CertIOException, CertificateException, CertificateIOException
    {
        // Generate a new KeyPair and sign it using the Root Cert Private Key
        // by generating a CSR (Certificate Signing Request)
        var keyPair = generateRsaKeyPair(bits);

        // Setup validity
        var validity = createValidityPeriod(expirationDays);

        // Issued By and Issued To same for root certificate
        var issuedCertSubject = new X500Name(SUBJECT + commonName);
        var csrContentSigner = createSigner(rootCert.getKeyPair());

        var csr = new JcaPKCS10CertificationRequestBuilder(issuedCertSubject, keyPair.getPublic()).build(csrContentSigner);

        // Use the Signed KeyPair and CSR to generate an issued Certificate
        // Here serial number is randomly generated. In general, CAs use
        // a sequence to generate Serial number and avoid collisions
        var issuedCertBuilder = new X509v3CertificateBuilder(rootCert.getSubject(),
                                                             new BigInteger(Long.toString(new SecureRandom().nextLong())),
                                                             validity.getFirst(),
                                                             validity.getSecond(),
                                                             csr.getSubject(),
                                                             csr.getSubjectPublicKeyInfo());

        convertToCertificate(issuedCertBuilder, sans);
        var crt = signCertificate(issuedCertBuilder, csrContentSigner);

        return new GeneratedCert(name, keyPair.getPrivate(), crt);
    }

    public static GeneratedCert createSelfSignedCertificate(String name,
                                                            int bits,
                                                            Integer expirationDays,
                                                            String commonName,
                                                            List<String> sans) throws NoSuchAlgorithmException, OperatorCreationException, CertIOException, CertificateException, CertificateIOException
    {
        // Generate a new KeyPair and sign it using the Root Cert Private Key
        // by generating a CSR (Certificate Signing Request)
        var keyPair = generateRsaKeyPair(bits);
        // Setup validity
        var validity = createValidityPeriod(expirationDays);

        // Issued By and Issued To same for root certificate
        var issuedCertSubject = new X500Name(SUBJECT + commonName);
        var csrContentSigner = createSigner(keyPair);
        var csr = new JcaPKCS10CertificationRequestBuilder(issuedCertSubject, keyPair.getPublic()).build(csrContentSigner);

        // Use the Signed KeyPair and CSR to generate an issued Certificate
        // Here serial number is randomly generated.
        var issuedCertBuilder = new X509v3CertificateBuilder(csr.getSubject(),
                                                             new BigInteger(Long.toString(new SecureRandom().nextLong())),
                                                             validity.getFirst(),
                                                             validity.getSecond(),
                                                             csr.getSubject(),
                                                             csr.getSubjectPublicKeyInfo());

        convertToCertificate(issuedCertBuilder, sans);
        var crt = signCertificate(issuedCertBuilder, csrContentSigner);

        return new GeneratedCert(name, keyPair.getPrivate(), crt);
    }

    public static void createLogFile(GeneratedCert crt,
                                     Path path) throws CertificateIOException
    {

        Map<String, Object> logMap = new LinkedHashMap<>();
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH);

        logMap.put("Name", crt.getName());
        logMap.put("isCA", crt.getX509Certificate().getBasicConstraints() != -1);
        logMap.put("Algorithm", crt.getX509Certificate().getSigAlgName());
        logMap.put("Key size", Utils.getKeyLength(crt.getX509Certificate().getPublicKey()));
        logMap.put("From", df.format(crt.getX509Certificate().getNotBefore()));
        logMap.put("To", df.format(crt.getX509Certificate().getNotAfter()));
        // The issuer contains the equals character "=". We should consider using format
        // that would quote the values to avoid confusion.
        logMap.put("Issuer", crt.getX509Certificate().getIssuerX500Principal().getName());
        logMap.put("Subject DN", crt.getX509Certificate().getSubjectX500Principal().getName());
        logMap.put("SAN", Utils.getSubjectAltNames(crt.getX509Certificate()));
        logMap.put("SerialNumber", crt.getX509Certificate().getSerialNumber());

        var absPath = Utils.createDirs(path.toAbsolutePath());
        dumpYaml(absPath, logMap);
    }

    public static void dumpYaml(Path absPath,
                                Map<String, Object> logMap) throws CertificateIOException
    {
        final DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);

        final var yaml = new Yaml(options);

        // save a properties file
        try (var bw = Files.newBufferedWriter(absPath.resolve(DEFAULT_LOG_FILE_NAME)))
        {
            yaml.dump(logMap, bw);
        }
        catch (IOException e)
        {
            throw new CertificateIOException("The log file could not be created at " + absPath.toString(), e);
        }
    }

}
