package com.ericsson.supreme;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertThrows;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.constructor.ConstructorException;

import com.ericsson.supreme.api.CertificateGenerator;
import com.ericsson.supreme.api.GeneratedCert;
import com.ericsson.supreme.config.ConfigurationSerializer;
import com.ericsson.supreme.exceptions.CertificateCreationException;
import com.ericsson.supreme.exceptions.CertificateIOException;
import com.ericsson.supreme.kernel.CertificateIO;
import com.ericsson.supreme.kernel.CertificateTool;

class CLITest
{
    protected static final Logger log = LoggerFactory.getLogger(CLITest.class);

    @ParameterizedTest
    @ValueSource(strings = { "generate -p properties.yaml",
                             "install -p propeties.yaml",
                             "generate install -p propeties.yaml",
                             "generate -d k6",
                             "generate -d k6,seppmgr,scpwrk",
                             "generate install -d k6,seppmgr,scpwrk",
                             "generate -d k6 -p properties.yaml",
                             "generate -d k6,seppsim -p properties.yaml", })
    void testValidInput(String input)
    {
        try
        {
            CLI cli = new CLI(input.split(" "));
            cli.validateInput();
            var cmd = cli.getCmd();
            assertTrue(cmd.getArgList().size() > 0, "Invalid arguments for option");
        }
        catch (ParseException e)
        {
            fail("Exception should not be thown " + e.getMessage());
        }

    }

    @ParameterizedTest
    @ValueSource(strings = { "",
                             "-p properties.yaml",
                             "unknownaction -p propeties.yaml",
                             "generate unknownaction -p propeties.yaml",
                             "-d unknown default",
                             "generate -d k6 seppmgr install",
                             "generate -d k6,seppmgr install",
                             "generate -d k6,install",
                             "generate -d k6,unknown",
                             "generate -d k6 install",
                             "-d k6" })
    void testInvalidInput(String input)
    {

        CLI cli = new CLI(input.split(" "));
        assertThrows(ParseException.class, cli::validateInput);

    }

    public static Path testDataPath(String a)
    {
        return Path.of("./src/test/resources/testdata", a);
    }

    public static Path testResourcesPath(String a)
    {
        return Path.of("./src/test/resources", a);
    }

    @Test
    void test()
    {
        var configFile = testResourcesPath("example.yaml");
        try
        {
            CLI cli = new CLI(new String[1]);
            var config = ConfigurationSerializer.getConfiguration(configFile.toString());
            cli.handleCustomGeneration(config);
        }
        catch (ConstructorException | FileNotFoundException | CertificateCreationException | CertificateIOException e)
        {
            fail(e.getClass().toString() + ":" + e.getMessage());
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Test
    void testGenerateRootCertificatesNonExistingCas()
    {
        var name1 = "ca1";
        var name2 = "ca2";

        var configFile = testResourcesPath("SupremeUT2.yaml");
        try
        {

            assertFalse(Files.exists(testDataPath("nonexisting1")));
            assertFalse(Files.exists(testDataPath("nonexisting2")));
            var config = ConfigurationSerializer.getConfiguration(configFile.toString());

            CLI cli = new CLI(new String[1]);
            var caStore = cli.generateRootCertificates(config);

            assertTrue(Files.exists(testDataPath("nonexisting1/key.pem")));
            assertTrue(Files.exists(testDataPath("nonexisting1/cert.pem")));
            assertTrue(Files.exists(testDataPath("nonexisting2/key.pem")));
            assertTrue(Files.exists(testDataPath("nonexisting2/cert.pem")));
            assertTrue(Files.exists(testDataPath("nonexisting1/" + CertificateTool.DEFAULT_LOG_FILE_NAME)));
            assertTrue(Files.exists(testDataPath("nonexisting2/" + CertificateTool.DEFAULT_LOG_FILE_NAME)));
            assertNotNull(caStore.get(name1));
            assertNotNull(caStore.get(name2));
            assertEquals(2, caStore.size());
        }
        catch (ConstructorException | FileNotFoundException | CertificateCreationException | CertificateIOException e)
        {
            fail(e.getClass().toString() + ":" + e.getMessage());
        }

    }

    @Test
    void testGenerateRootCertificatesMixed()
    {
        var name1 = "ca1";
        var name2 = "ca2";

        int days = 365;
        String cnName = "thisisaca";

        var path1 = testDataPath("existing1");
        var path2 = testDataPath("nonexisting2");

        var configFile = testResourcesPath("SupremeUT2.yaml");
        try
        {
            GeneratedCert ca = CertificateGenerator.createCertificateAuthority("testtest", days, cnName);
            CertificateIO.exportCertificate(ca, path1);
            CertificateTool.createLogFile(ca, path1);

            assertTrue(Files.exists(path1));
            assertFalse(Files.exists(path2));
            var config = ConfigurationSerializer.getConfiguration(configFile.toString());

            CLI cli = new CLI(new String[1]);
            var caStore = cli.generateRootCertificates(config);

            assertTrue(Files.exists(testDataPath("existing1/key.pem")));
            assertTrue(Files.exists(testDataPath("existing1/cert.pem")));
            assertTrue(Files.exists(testDataPath("nonexisting2/key.pem")));
            assertTrue(Files.exists(testDataPath("nonexisting2/cert.pem")));
            assertTrue(Files.exists(testDataPath("existing1/" + CertificateTool.DEFAULT_LOG_FILE_NAME)));
            assertTrue(Files.exists(testDataPath("nonexisting2/" + CertificateTool.DEFAULT_LOG_FILE_NAME)));
            assertNotNull(caStore.get(name1));
            assertNotNull(caStore.get(name2));
            assertEquals(2, caStore.size());
        }
        catch (ConstructorException | IOException | CertificateCreationException | CertificateIOException e)
        {
            fail(e.getClass().toString() + ":" + e.getMessage());
        }

    }

    @Test
    void testHandleGenerationMixed()
    {
        int days = 365;
        String cnName = "thisisaca";

        var path1 = testDataPath("existing1");
        var path2 = testDataPath("nonexisting2");

        var cert1 = testDataPath("sepp1");
        var cert2 = testDataPath("sepp2");
        var cert3 = testDataPath("sepp3");

        var configFile = testResourcesPath("SupremeUT3.yaml");
        try
        {
            GeneratedCert ca = CertificateGenerator.createCertificateAuthority("testtest", days, cnName);
            CertificateIO.exportCertificate(ca, path1);
            CertificateTool.createLogFile(ca, path1);

            assertTrue(Files.exists(path1));
            assertFalse(Files.exists(path2));

            assertFalse(Files.exists(cert1));
            assertFalse(Files.exists(cert2));
            assertFalse(Files.exists(cert3));

            var config = ConfigurationSerializer.getConfiguration(configFile.toString());

            CLI cli = new CLI(new String[1]);
            cli.handleCustomGeneration(config);

            assertTrue(Files.exists(testDataPath("existing1/key.pem")));
            assertTrue(Files.exists(testDataPath("existing1/cert.pem")));
            assertTrue(Files.exists(testDataPath("nonexisting2/key.pem")));
            assertTrue(Files.exists(testDataPath("nonexisting2/cert.pem")));
            assertTrue(Files.exists(testDataPath("existing1/" + CertificateTool.DEFAULT_LOG_FILE_NAME)));
            assertTrue(Files.exists(testDataPath("nonexisting2/" + CertificateTool.DEFAULT_LOG_FILE_NAME)));

            assertTrue(Files.exists(testDataPath("sepp1/key.pem")));
            assertTrue(Files.exists(testDataPath("sepp1/cert.pem")));
            assertTrue(Files.exists(testDataPath("sepp1/" + CertificateTool.DEFAULT_LOG_FILE_NAME)));

            assertTrue(Files.exists(testDataPath("sepp2/key.pem")));
            assertTrue(Files.exists(testDataPath("sepp2/cert.pem")));
            assertTrue(Files.exists(testDataPath("sepp2/" + CertificateTool.DEFAULT_LOG_FILE_NAME)));

            assertTrue(Files.exists(testDataPath("sepp3/key.pem")));
            assertTrue(Files.exists(testDataPath("sepp3/cert.pem")));
            assertTrue(Files.exists(testDataPath("sepp3/" + CertificateTool.DEFAULT_LOG_FILE_NAME)));

        }
        catch (ConstructorException | IOException | CertificateCreationException | CertificateIOException e)
        {
            fail(e.getClass().toString() + ":" + e.getMessage());
        }

    }

    @Test
    void testHandleGenerationCertificateOverride()
    {
        int days = 365;
        String cnName = "thisisaca";

        var cert1 = testDataPath("sepp1");
        var cert2 = testDataPath("sepp2");
        var cert3 = testDataPath("sepp3");

        var configFile = testResourcesPath("SupremeUT3.yaml");
        try
        {
            GeneratedCert oldCert = CertificateGenerator.createSelfSignedCertificate("testtest", days, cnName, List.of());
            CertificateIO.exportCertificate(oldCert, cert1);
            CertificateTool.createLogFile(oldCert, cert1);

            assertTrue(Files.exists(cert1));
            assertFalse(Files.exists(cert2));
            assertFalse(Files.exists(cert3));

            var config = ConfigurationSerializer.getConfiguration(configFile.toString());
            CLI cli = new CLI(new String[1]);
            cli.handleCustomGeneration(config);

            var newCert = CertificateIO.readGeneratedCert("testtest", cert1);

            assertNotEquals(newCert.get(), oldCert);

            assertTrue(Files.exists(testDataPath("sepp1/key.pem")));
            assertTrue(Files.exists(testDataPath("sepp1/cert.pem")));
            assertTrue(Files.exists(testDataPath("sepp1/" + CertificateTool.DEFAULT_LOG_FILE_NAME)));

            assertTrue(Files.exists(testDataPath("sepp2/key.pem")));
            assertTrue(Files.exists(testDataPath("sepp2/cert.pem")));
            assertTrue(Files.exists(testDataPath("sepp2/" + CertificateTool.DEFAULT_LOG_FILE_NAME)));

            assertTrue(Files.exists(testDataPath("sepp3/key.pem")));
            assertTrue(Files.exists(testDataPath("sepp3/cert.pem")));
            assertTrue(Files.exists(testDataPath("sepp3/" + CertificateTool.DEFAULT_LOG_FILE_NAME)));

        }
        catch (ConstructorException | IOException | CertificateCreationException | CertificateIOException e)
        {
            fail(e.getClass().toString() + ":" + e.getMessage());
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

            throw new RuntimeException("not deleted"); // TODO Auto-generated catch block
        }

    }

}
