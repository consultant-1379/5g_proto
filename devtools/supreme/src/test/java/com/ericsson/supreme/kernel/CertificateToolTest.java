package com.ericsson.supreme.kernel;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import com.ericsson.supreme.api.GeneratedCert;
import com.ericsson.supreme.exceptions.CertificateIOException;

class CertificateToolTest

{
    protected static final Logger log = LoggerFactory.getLogger(CertificateToolTest.class);

    public static Path testDataPath(String a)
    {
        return Path.of("./src/test/resources/testdata", a);
    }

    @Test
    void createSelfSignedCertificateTest()
    {
        int days = 365;
        int keysize = 3072;
        String cnName = "cnName";
        ArrayList<String> sans = new ArrayList<>();
        sans.add("domain");
        sans.add("domain1");
        sans.add("63.239.160.132");
        sans.add("8f7:179:d1fc:9492:9f64:67c5:1d38:e3af");

        try
        {
            GeneratedCert crt = CertificateTool.createSelfSignedCertificate("testtest", keysize, days, cnName, sans);

            long diffInMillies = Math.abs(crt.getX509Certificate().getNotAfter().getTime() - crt.getX509Certificate().getNotBefore().getTime());
            long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

            System.out.println("Created cert: " + crt.getX509Certificate());

            assertEquals(days, diff);
            assertEquals("C=GE,L=Aachen,O=Ericsson,OU=EDD,CN=" + cnName, crt.getX509Certificate().getIssuerDN().toString());
            assertEquals("C=GE,L=Aachen,O=Ericsson,OU=EDD,CN=" + cnName, crt.getX509Certificate().getSubjectDN().toString());
            assertEquals(sans, Utils.getSubjectAltNames(crt.getX509Certificate()));
            assertEquals(keysize, ((RSAPublicKey) crt.getX509Certificate().getPublicKey()).getModulus().bitLength());
            assertEquals(-1, crt.getX509Certificate().getBasicConstraints());

        }
        catch (IOException | OperatorCreationException | CertificateException | NoSuchAlgorithmException | CertificateIOException e)
        {
            fail(e.getClass().toString() + e.getMessage());
        }
    }

    @Test
    void createSelfSignedCertificateEmptySanTest()
    {
        int days = 36536;
        int keysize = 2048;
        String cnName = "cnName";

        try
        {
            GeneratedCert crt = CertificateTool.createSelfSignedCertificate("testtest", keysize, days, cnName, new ArrayList<String>());

            long diffInMillies = Math.abs(crt.getX509Certificate().getNotAfter().getTime() - crt.getX509Certificate().getNotBefore().getTime());
            long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

            System.out.println("Created cert: " + crt.getX509Certificate());

            assertEquals(days, diff);
            assertEquals("C=GE,L=Aachen,O=Ericsson,OU=EDD,CN=" + cnName, crt.getX509Certificate().getIssuerDN().toString());
            assertEquals("C=GE,L=Aachen,O=Ericsson,OU=EDD,CN=" + cnName, crt.getX509Certificate().getSubjectDN().toString());
            assertTrue(Utils.getSubjectAltNames(crt.getX509Certificate()).isEmpty());
            assertEquals(keysize, ((RSAPublicKey) crt.getX509Certificate().getPublicKey()).getModulus().bitLength());
            assertEquals(-1, crt.getX509Certificate().getBasicConstraints());

        }
        catch (IOException | OperatorCreationException | CertificateException | NoSuchAlgorithmException | CertificateIOException e)
        {
            fail(e.getClass().toString() + e.getMessage());
        }
    }

    @Test
    void createCertificateAuthorityTest()
    {
        int days = 365;
        int keysize = 2048;
        String cnName = "thisisaca";

        try
        {
            GeneratedCert crt = CertificateTool.createCertificateAuthority("testtest", keysize, days, cnName);

            long diffInMillies = Math.abs(crt.getX509Certificate().getNotAfter().getTime() - crt.getX509Certificate().getNotBefore().getTime());
            long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

            System.out.println("Created cert: " + crt.getX509Certificate());

            assertEquals(days, diff);
            assertEquals("C=GE,L=Aachen,O=Ericsson,OU=EDD,CN=" + cnName, crt.getX509Certificate().getIssuerDN().toString());
            assertEquals("C=GE,L=Aachen,O=Ericsson,OU=EDD,CN=" + cnName, crt.getX509Certificate().getSubjectDN().toString());
            assertTrue(Utils.getSubjectAltNames(crt.getX509Certificate()).isEmpty());
            assertEquals(keysize, ((RSAPublicKey) crt.getX509Certificate().getPublicKey()).getModulus().bitLength());
            assertNotEquals(-1, crt.getX509Certificate().getBasicConstraints());

        }
        catch (IOException | OperatorCreationException | CertificateException | NoSuchAlgorithmException | CertificateIOException e)
        {
            fail(e.getClass().toString() + e.getMessage());
        }
    }

    @Test
    void createCertificateSignedByRootTest()
    {
        int days = 365;
        int keysize = 2048;
        String rootCnName = "thisisaca";
        String certCnName = "commonName";
        ArrayList<String> sans = new ArrayList<>();
        sans.add("domain");
        sans.add("domain1");

        try
        {
            GeneratedCert rootCrt = CertificateTool.createCertificateAuthority("testca", keysize, days, rootCnName);
            GeneratedCert crt = CertificateTool.createCertificateSignedByRoot("testtest", keysize, days, certCnName, sans, rootCrt);

            long diffInMillies = Math.abs(crt.getX509Certificate().getNotAfter().getTime() - crt.getX509Certificate().getNotBefore().getTime());
            long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

            System.out.println("Created cert: " + crt.getX509Certificate());

            assertEquals(days, diff);
            assertEquals("C=GE,L=Aachen,O=Ericsson,OU=EDD,CN=" + rootCnName, crt.getX509Certificate().getIssuerDN().toString());
            assertEquals("C=GE,L=Aachen,O=Ericsson,OU=EDD,CN=" + certCnName, crt.getX509Certificate().getSubjectDN().toString());
            assertEquals(sans, Utils.getSubjectAltNames(crt.getX509Certificate()));
            assertEquals(keysize, ((RSAPublicKey) crt.getX509Certificate().getPublicKey()).getModulus().bitLength());
            assertEquals(-1, crt.getX509Certificate().getBasicConstraints());
            assertDoesNotThrow(() -> crt.getX509Certificate().verify(rootCrt.getX509Certificate().getPublicKey(), CertificateTool.BC_PROVIDER));

        }
        catch (IOException | OperatorCreationException | CertificateException | NoSuchAlgorithmException | CertificateIOException e)
        {
            fail(e.getClass().toString() + e.getMessage());
        }
    }

    @Test
    void createLogFileTest()
    {
        int days = 365;
        int keysize = 2048;
        String rootCnName = "thisisaca";
        String certCnName = "commonName";
        ArrayList<String> sans = new ArrayList<>();
        sans.add("domain");
        sans.add("domain1");
        var path = testDataPath("testLog1");

        try
        {
            GeneratedCert rootCrt = CertificateTool.createCertificateAuthority("testca", keysize, days, rootCnName);
            GeneratedCert crt = CertificateTool.createCertificateSignedByRoot("testtest", keysize, days, certCnName, sans, rootCrt);

            long diffInMillies = Math.abs(crt.getX509Certificate().getNotAfter().getTime() - crt.getX509Certificate().getNotBefore().getTime());
            long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

            System.out.println("Created cert: " + crt.getX509Certificate());

            assertEquals(days, diff);
            assertEquals("C=GE,L=Aachen,O=Ericsson,OU=EDD,CN=" + rootCnName, crt.getX509Certificate().getIssuerDN().toString());
            assertEquals("C=GE,L=Aachen,O=Ericsson,OU=EDD,CN=" + certCnName, crt.getX509Certificate().getSubjectDN().toString());
            assertEquals(sans, Utils.getSubjectAltNames(crt.getX509Certificate()));
            assertEquals(keysize, ((RSAPublicKey) crt.getX509Certificate().getPublicKey()).getModulus().bitLength());
            assertEquals(-1, crt.getX509Certificate().getBasicConstraints());

            CertificateTool.createLogFile(crt, path);

            Yaml yaml = new Yaml();
            Map<String, Object> prop = yaml.load(Files.newInputStream(path.resolve(CertificateTool.DEFAULT_LOG_FILE_NAME)));
            System.out.println(prop);

            // set key and value
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH);

            assertEquals(crt.getName(), prop.get("Name"));
            assertEquals(crt.getX509Certificate().getSigAlgName(), prop.get("Algorithm"));
            assertEquals(Utils.getKeyLength(crt.getX509Certificate().getPublicKey()), prop.get("Key size"));
            assertEquals(df.format(crt.getX509Certificate().getNotBefore()), prop.get("From"));
            assertEquals(df.format(crt.getX509Certificate().getNotAfter()), prop.get("To"));
            assertEquals(crt.getX509Certificate().getIssuerX500Principal().getName(), prop.get("Issuer"));
            assertEquals(crt.getX509Certificate().getBasicConstraints() != -1, prop.get("isCA"));
            assertEquals(crt.getX509Certificate().getSerialNumber(), BigInteger.valueOf((Long) prop.get("SerialNumber")));
            assertEquals(crt.getX509Certificate().getSubjectX500Principal().getName(), prop.get("Subject DN"));
            assertEquals(Utils.getSubjectAltNames(crt.getX509Certificate()), prop.get("SAN"));

        }
        catch (IOException | OperatorCreationException | CertificateException | NoSuchAlgorithmException | CertificateIOException e)
        {
            fail(e.getClass().toString() + e.getMessage());
        }
    }

    @AfterEach
    void cleanUp()
    {
        try
        {
            FileUtils.deleteDirectory(testDataPath("").toFile());
        }
        catch (IOException e)
        {
            log.error("{}", (Object[]) e.getStackTrace());

            throw new RuntimeException("not deleted");
            // TODO Auto-generated catch block
        }
    }

}
