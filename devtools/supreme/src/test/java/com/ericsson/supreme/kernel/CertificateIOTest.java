package com.ericsson.supreme.kernel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.supreme.api.GeneratedCert;
import com.ericsson.supreme.kernel.CertificateIO;
import com.ericsson.supreme.kernel.CertificateTool;

class CertificateIOTest
{
    protected static final Logger log = LoggerFactory.getLogger(CertificateIOTest.class);

    public static Path testDataPath(String a)
    {
        return Path.of("./src/test/resources/testdata", a);
    }

    @Test
    void testExportSelfSignedCertificate()
    {
        var path = testDataPath("test1/testCertificate");
        try
        {
            GeneratedCert crt = CertificateTool.createSelfSignedCertificate("Test", 3075, 365, "cnName", List.of());
            CertificateIO.exportCertificate(crt, path);

            var keyPath = path.resolve(CertificateTool.DEFAULT_KEY_NAME);
            var certPath = path.resolve(CertificateTool.DEFAULT_CERT_NAME);

            assertTrue(Files.exists(keyPath));
            assertTrue(Files.exists(certPath));
            System.out.println(Files.readString(keyPath));
            System.out.println(Files.readString(certPath));
            assertTrue(Files.readString(keyPath).contains(CertificateTool.BEGIN_PRIVATE_KEY));
            assertTrue(Files.readString(keyPath).contains(CertificateTool.END_PRIVATE_KEY));

        }
        catch (Exception e)
        {
            fail("Not yet implemented" + e.getMessage());
        }
    }

    @Test
    void testExportCa()
    {
        var path = testDataPath("test1/testCertificate");
        try
        {
            GeneratedCert crt = CertificateTool.createCertificateAuthority("Test", 3075, 365, "cnName");
            CertificateIO.exportCertificate(crt, path);

            var keyPath = path.resolve(CertificateTool.DEFAULT_KEY_NAME);
            var certPath = path.resolve(CertificateTool.DEFAULT_CERT_NAME);

            assertTrue(Files.exists(keyPath));
            assertTrue(Files.exists(certPath));
            System.out.println(Files.readString(keyPath));
            System.out.println(Files.readString(certPath));
            assertTrue(Files.readString(keyPath).contains(CertificateTool.BEGIN_PRIVATE_KEY));
            assertTrue(Files.readString(keyPath).contains(CertificateTool.END_PRIVATE_KEY));

        }
        catch (Exception e)
        {
            fail("Not yet implemented" + e.getMessage());
        }
    }

    @Test
    void testReadNonExistingCertificate()
    {
        try
        {
            var path = testDataPath("test1/testCertificate");
            var res = CertificateIO.readGeneratedCert("Test", path);

            assertTrue(res.isEmpty());
        }
        catch (Exception e)
        {
            fail("Test failed" + e.getMessage());
        }
    }

    @Test
    void testReadExistingCertificate()
    {
        var path = testDataPath("test1/testCertificate");
        try
        {
            GeneratedCert crt = CertificateTool.createSelfSignedCertificate("Test", 3075, 365, "cnName", List.of());
            CertificateIO.exportCertificate(crt, path);

            var keyPath = path.resolve(CertificateTool.DEFAULT_KEY_NAME);
            var certPath = path.resolve(CertificateTool.DEFAULT_CERT_NAME);

            assertTrue(Files.exists(keyPath));
            assertTrue(Files.exists(certPath));

            var readCrt = CertificateIO.readGeneratedCert("Test", path);

            assertTrue(readCrt.isPresent());
            assertEquals(crt, readCrt.get());
        }
        catch (Exception e)
        {
            fail("Test failed" + e.getMessage());
        }
    }

    @Test
    void testReadExistingCa()
    {
        var path = testDataPath("test1/testCertificate");
        try
        {
            GeneratedCert crt = CertificateTool.createCertificateAuthority("Test", 3075, 365, "cnName");
            CertificateIO.exportCertificate(crt, path);

            var keyPath = path.resolve(CertificateTool.DEFAULT_KEY_NAME);
            var certPath = path.resolve(CertificateTool.DEFAULT_CERT_NAME);

            assertTrue(Files.exists(keyPath));
            assertTrue(Files.exists(certPath));

            var readCrt = CertificateIO.readGeneratedCert("Test", path);

            assertTrue(readCrt.isPresent());
            assertEquals(crt, readCrt.get());
        }
        catch (Exception e)
        {
            fail("Test failed" + e.getMessage());
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
