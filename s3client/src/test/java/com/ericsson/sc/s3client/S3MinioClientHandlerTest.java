package com.ericsson.sc.s3client;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.utility.Base58;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ericsson.sc.s3client.S3Parameters.ClientAccess;
import com.ericsson.sc.s3client.S3Parameters.ClientTimeout;
import com.ericsson.sc.s3client.S3Parameters.EndpointData;
import com.ericsson.sc.s3client.S3Parameters.ObjectStorageData;

public class S3MinioClientHandlerTest
{
    private static final Logger log = LoggerFactory.getLogger(S3MinioClientHandlerTest.class);
    private static final String ADMIN_ACCESS_KEY = "admin";
    private static final String ADMIN_SECRET_KEY = "12345678";
    private MinioContainer minioServer;
    private MinioContainer tlsMinioServer;
    private String minioIpAddress;
    private String tlsMinioIpAddress;
    private Integer minioPort;
    private Integer tlsMinioPort;
    private S3Parameters parameters;
    private S3Parameters tlsParameters;
    private static String POD_DIR = "src/test/resources/coredumps";
    private static final Integer TIMEOUT_300 = 300;
    private static final Long DEBOUNCE_3000MS = 3000L;
    private static final Long THROTTLE_3S = 3L;
    private static final String BUCKET_NAME = "ERIC-SC-COREDUMPS";
    private static final String OBJECT_PREFIX = "eric-sc-pod-qw23ert12y-as77df";
    private static final String CLIENT_CA_PATH = "src/test/resources/certificates/ca/public.crt";
    private static final int DEFAULT_PORT = 9000;
    private static final String MINIO_ACCESS_KEY = "MINIO_ACCESS_KEY";
    private static final String MINIO_SECRET_KEY = "MINIO_SECRET_KEY";
    private static final String DEFAULT_STORAGE_DIRECTORY = "/data";
    private static final String HEALTH_ENDPOINT = "/minio/health/ready";
    private static final String ARMDOCKERHUB_IMAGE = "armdockerhub.rnd.ericsson.se/minio/minio:RELEASE.2021-06-17T00-10-46Z";
    private static final String SERVER_CERTIFICATES_PATH = "/root/.minio/certs";
    private static final String SERVER_CERTIFICATES = "certificates/server";
//    private static final String SERVER_CA_PATH = "/root/.minio/certs/CAs";
//    private static final String SERVER_CA = "certificates/ca";
    private String monitorDirectoryPath;
    private static final String TMP_FILE = "challenge.tmp";
    private static final String CONTAINER_1_DIR = "container-1";
    private Path file1;
    private Path file2;

    public class MinioContainer extends GenericContainer<MinioContainer>
    {
        @SuppressWarnings("deprecation")
        public MinioContainer(String image,
                              CredentialsProvider credentials,
                              Boolean tlsEnabled) throws InterruptedException
        {
            if (image == null)
                image = ARMDOCKERHUB_IMAGE;
            log.debug("Image: {}", image);
            setDockerImageName(image);
            withNetworkAliases("minio-" + Base58.randomString(6));
            addExposedPort(DEFAULT_PORT);
            if (credentials != null)
            {
                withEnv(MINIO_ACCESS_KEY, credentials.getAccessKey());
                withEnv(MINIO_SECRET_KEY, credentials.getSecretKey());
            }
            else
            {
                withEnv(MINIO_ACCESS_KEY, ADMIN_ACCESS_KEY);
                withEnv(MINIO_SECRET_KEY, ADMIN_SECRET_KEY);
            }
            withCommand("server", DEFAULT_STORAGE_DIRECTORY);

            if (tlsEnabled)
            {
                // TLS Enabled - server authentication
                withClasspathResourceMapping(SERVER_CERTIFICATES, SERVER_CERTIFICATES_PATH, BindMode.READ_WRITE);
//                withClasspathResourceMapping(SERVER_CA, SERVER_CA_PATH, BindMode.READ_WRITE);
            }
            else
            {
                setWaitStrategy(new HttpWaitStrategy().forPath(HEALTH_ENDPOINT).forPort(DEFAULT_PORT).withStartupTimeout(Duration.ofSeconds(60)));
            }

        }

        public String getIpAddress()
        {
            return getContainerIpAddress();
        }

        public Integer getPort()
        {
            return getMappedPort(DEFAULT_PORT);
        }

        public class CredentialsProvider
        {
            private String accessKey;
            private String secretKey;

            public CredentialsProvider(String accessKey,
                                       String secretKey)
            {
                this.accessKey = accessKey;
                this.secretKey = secretKey;
            }

            public String getAccessKey()
            {
                return this.accessKey;
            }

            public String getSecretKey()
            {
                return this.secretKey;
            }
        }
    }

    @BeforeClass(groups = "functest")
    private void beforeClass() throws InterruptedException, IOException
    {
        log.debug("Setup environment prior execution of any method in this test class.");

        Files.walk(Paths.get(POD_DIR)) // scan the tree from POD_DIR
             .sorted(Comparator.reverseOrder()) // sort in reverse order
             .map(Path::toFile) // get the file objects
             .forEach(File::delete); // delete each file/directory
        assertFalse(Files.exists(Paths.get(POD_DIR)), "Error " + POD_DIR + " already exists.");

        String directoryPath = "";
        Path podDirPath = Files.createDirectories(Paths.get(POD_DIR));
        assertTrue(Files.exists(podDirPath));
        directoryPath = podDirPath.toFile().getAbsolutePath();
        this.monitorDirectoryPath = directoryPath;

        Path containerPath = Paths.get(this.monitorDirectoryPath, CONTAINER_1_DIR);
        containerPath.toFile().mkdir();

        this.file1 = Files.createTempFile(containerPath, TMP_FILE, null);
        log.debug("File {} created", file1);

        this.file2 = Files.createTempFile(containerPath, TMP_FILE, null);
        log.debug("File {} created", file2);

        /*
         * Initialize minio test container with default configuration
         */
        this.tlsMinioServer = new MinioContainer(null, null, true);
        this.minioServer = new MinioContainer(null, null, false);

        /*
         * Start minio test container
         */
        this.tlsMinioServer.start();
        this.minioServer.start();

        /*
         * Extract minio test container ip address
         */
        this.tlsMinioIpAddress = tlsMinioServer.getIpAddress();
        this.minioIpAddress = minioServer.getIpAddress();

        /*
         * Extract minio test container port
         */
        this.tlsMinioPort = tlsMinioServer.getPort();
        this.minioPort = minioServer.getPort();

        log.info("Minio Test Server with TLS Started with IP: {} and PORT: {}.", this.tlsMinioIpAddress, this.tlsMinioPort);

        ObjectStorageData objectStorageParams = new ObjectStorageData(this.monitorDirectoryPath, DEBOUNCE_3000MS, THROTTLE_3S, BUCKET_NAME, OBJECT_PREFIX);
        EndpointData endpointDataParams = new EndpointData(this.tlsMinioIpAddress, this.tlsMinioPort, true);
        ClientAccess clientAccessParams = new ClientAccess(ADMIN_ACCESS_KEY, ADMIN_SECRET_KEY);
        ClientTimeout clientTimeoutParams = new ClientTimeout(TIMEOUT_300, TIMEOUT_300, TIMEOUT_300);
        this.tlsParameters = S3Parameters.with(true, objectStorageParams, endpointDataParams, clientAccessParams, clientTimeoutParams);

        log.info("Minio Test Server without TLS Started with IP: {} and PORT: {}.", this.minioIpAddress, this.minioPort);
        objectStorageParams = new ObjectStorageData(this.monitorDirectoryPath, DEBOUNCE_3000MS, THROTTLE_3S, BUCKET_NAME, OBJECT_PREFIX);
        endpointDataParams = new EndpointData(this.minioIpAddress, this.minioPort, false);
        clientAccessParams = new ClientAccess(ADMIN_ACCESS_KEY, ADMIN_SECRET_KEY);
        clientTimeoutParams = new ClientTimeout(TIMEOUT_300, TIMEOUT_300, TIMEOUT_300);
        this.parameters = S3Parameters.with(false, objectStorageParams, endpointDataParams, clientAccessParams, clientTimeoutParams);

    }

    @AfterClass(groups = "functest")
    public void shutDown() throws IOException
    {

        log.debug("Cleanup activities after the execution of all methods in this test class.");

        assertTrue(Files.exists(Paths.get(POD_DIR)), "Error " + POD_DIR + " does not exist");
        Files.walk(Paths.get(this.monitorDirectoryPath)) // scan the tree from monitored path
             .sorted(Comparator.reverseOrder()) // sort in reverse order
             .map(Path::toFile) // get the file objects
             .forEach(File::delete); // delete each file/directory
        assertFalse(Files.exists(Paths.get(POD_DIR)), "Failed to delete " + POD_DIR);

        if (this.minioServer.isRunning())
        {
            this.minioServer.stop();
        }
        if (this.tlsMinioServer.isRunning())
        {
            this.tlsMinioServer.stop();
        }
    }

    /**
     * Try to create bucket with TLS
     * 
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test(groups = "functest")
    public void tc001() throws InvalidKeyException, NoSuchAlgorithmException, IOException, InterruptedException
    {
        S3MinioClientHandler handler = new S3MinioClientHandler(this.tlsParameters, Paths.get(CLIENT_CA_PATH));
        handler.createBucket("challengers1");

        if (handler.bucketExists("challengers1"))
            log.debug("Bucket <challengers1> created successfully");
        else
            assertTrue(false, "Bucket challengers1 does not exist.");
    }

    /**
     * Try to create bucket with TLS and add 2 temporary files
     * 
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test(groups = "functest")
    public void tc002() throws InvalidKeyException, NoSuchAlgorithmException, IOException, InterruptedException
    {
        S3MinioClientHandler handler = new S3MinioClientHandler(this.tlsParameters, Paths.get(CLIENT_CA_PATH));
        handler.createBucket("challengers2");
        if (handler.bucketExists("challengers2"))
            log.debug("Bucket <challengers2> created successfully");
        else
            assertTrue(false, "Bucket challengers2 does not exist.");

        assertTrue(handler.bucketExists("challengers2"), "Bucket challengers2 does not exist.");
        assertTrue(handler.getObjectNames("challengers2").size() == 0, "Files exist in bucket");

        String newObject1 = "challengers2-" + this.file1.toFile().getName();
        log.debug("Uploading file: {} ", newObject1);
        handler.uploadFile("challengers2", newObject1, this.file1.toFile().getAbsolutePath());

        String newObject2 = "challengers2-" + this.file2.toFile().getName();
        log.debug("Uploading file: {} ", newObject2);
        handler.uploadFile("challengers2", newObject2, this.file2.toFile().getAbsolutePath());

        List<String> objects = handler.getObjectNames("challengers2");
        log.debug("Object identified: {}", objects);
        assertTrue((objects.size() == 2) && objects.contains(newObject1) && objects.contains(newObject2), "Files exist in bucket");
    }

    /**
     * Try to create bucket without TLS
     * 
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test(groups = "functest")
    public void tc003() throws InvalidKeyException, NoSuchAlgorithmException, IOException, InterruptedException
    {
        S3MinioClientHandler handler = new S3MinioClientHandler(this.parameters, null);
        handler.createBucket("challengers1");

        if (handler.bucketExists("challengers1"))
            log.debug("Bucket <challengers1> created successfully");
        else
            assertTrue(false, "Bucket challengers1 does not exist.");
    }

    /**
     * Try to create bucket with TLS and add 2 temporary files
     * 
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test(groups = "functest")
    public void tc004() throws InvalidKeyException, NoSuchAlgorithmException, IOException, InterruptedException
    {
        S3MinioClientHandler handler = new S3MinioClientHandler(this.parameters, null);
        handler.createBucket("challengers2");
        if (handler.bucketExists("challengers2"))
            log.debug("Bucket <challengers2> created successfully");
        else
            assertTrue(false, "Bucket challengers2 does not exist.");

        assertTrue(handler.bucketExists("challengers2"), "Bucket challengers2 does not exist.");
        assertTrue(handler.getObjectNames("challengers2").size() == 0, "Files exist in bucket");

        String newObject1 = "challengers2-" + this.file1.toFile().getName();
        log.debug("Uploading file: {} ", newObject1);
        handler.uploadFile("challengers2", newObject1, this.file1.toFile().getAbsolutePath());

        String newObject2 = "challengers2-" + this.file2.toFile().getName();
        log.debug("Uploading file: {} ", newObject2);
        handler.uploadFile("challengers2", newObject2, this.file2.toFile().getAbsolutePath());

        List<String> objects = handler.getObjectNames("challengers2");
        log.debug("Object identified: {}", objects);
        assertTrue((objects.size() == 2) && objects.contains(newObject1) && objects.contains(newObject2), "Files exist in bucket");
    }

}
