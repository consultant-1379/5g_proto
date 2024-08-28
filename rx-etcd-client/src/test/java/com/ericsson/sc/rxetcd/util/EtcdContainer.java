/**
 * COPYRIGHT ERICSSON GMBH 2020
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jan 23, 2020
 *     Author: emldpng
 */

package com.ericsson.sc.rxetcd.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.SelinuxContext;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.images.builder.dockerfile.DockerfileBuilder;

import io.reactivex.Observable;

/**
 * This class represents a Docker container that contains an ETCD instance.
 * Methods are provided in order to start, stop and restart programmatically an
 * ETCD container for integration tests.
 */
public class EtcdContainer
{
    enum State
    {
        STARTED,
        STOPPED
    }

    private static final Logger log = LoggerFactory.getLogger(EtcdContainer.class);

    private static final int ETCD_EXPOSED_PORT = 2379;
    private static final int ETCD_INTERNAL_PORT = 2379;
    private static final String ETCD_CLIENT_USERNAME = "root";
    private static final String ETCD_CLIENT_PASSWORD = "rootroot";
    private static final String ETCD_DOCKER_IMAGE_NAME = "armdockerhub.rnd.ericsson.se/bitnami/etcd";
    private static final String ETCD_DOCKER_IMAGE_VERSION = "3.4.3";
    private static final String ETCD_DOCKER_DATA_DIR = "/etcd.data";
    private static final String ETCD_LOCAL_DATA_DIR_PREFIX = "etcd.temp.data";
    private static final String ETCD_READINESS_LOG_MESSAGE = ".*ready to serve client requests.*";
    private final FixedHostPortGenericContainer<?> container;
    private final Path dataDirectory;
    private State currentState = State.STOPPED;

    /**
     * Initializes the EtcdContainer for non-TLS scenarios
     * 
     * @param stateful Set to true if a restart of the ETCD container is needed, in
     *                 order to maintain the data.
     */
    public EtcdContainer(boolean stateful)
    {
        var dockerImage = prepareDockerImage();
        this.container = new FixedHostPortGenericContainer<>(dockerImage);
        this.container.withFixedExposedPort(ETCD_EXPOSED_PORT, 2379);
        this.container.withEnv("ALLOW_NONE_AUTHENTICATION", "yes");
        if (stateful)
        {
            dataDirectory = createLocalDirectory(ETCD_LOCAL_DATA_DIR_PREFIX);
            container.addFileSystemBind(dataDirectory.toString(), ETCD_DOCKER_DATA_DIR, BindMode.READ_WRITE, SelinuxContext.SHARED);
            this.container.withEnv("ETCD_DATA_DIR", ETCD_DOCKER_DATA_DIR);
        }
        else
        {
            dataDirectory = null;
        }
        log.info("XXXXX Completing the constructor of the container XXXXX");
    }

    /**
     * Initializes the EtcdContainer that is also configured for TLS. By using this
     * constructor it is implied that TLS is enabled by default.
     * 
     * @param stateful   Set to true if a restart of the ETCD container is needed,
     *                   in order to maintain the data.
     * @param serverCert Server certificate value
     * @param serverKey  Server private key value
     * @param rootCa     RootCA value
     * @throws IOException In case of wrong server certificate setup
     */
    public EtcdContainer(boolean stateful,
                         String serverCert,
                         String serverKey,
                         String rootCa) throws IOException
    {
        // By default, we call the non-tls constructor first
        this(stateful);

        // Create a temporary directory
        final String certsPath = Files.createTempDirectory("servercertspath").toString();

        // Create certificates in temp
        Files.writeString(Path.of(certsPath, "server-cert.pem"), serverCert, StandardOpenOption.CREATE);
        Files.writeString(Path.of(certsPath, "server-key.pem"), serverKey, StandardOpenOption.CREATE);
        Files.writeString(Path.of(certsPath, "trustCA.pem"), rootCa, StandardOpenOption.CREATE);

        this.container.addFileSystemBind(certsPath, "/etc/certificate/", BindMode.READ_WRITE);

        log.info("XXXXX Adding environment variables XXXXX");
        this.container.withEnv("ETCD_AUTO_TLS", "false");

        this.container.withEnv("ETCD_CERT_FILE", "/etc/certificate/server-cert.pem");
        this.container.withEnv("ETCD_KEY_FILE", "/etc/certificate/server-key.pem");
        this.container.withEnv("ETCD_CLIENT_CERT_AUTH", "false");
        this.container.withEnv("ETCD_TRUSTED_CA_FILE", "/etc/certificate/trustCA.pem");
        this.container.withEnv("ETCD_CA_FILE", "/etc/certificate/trustCA.pem");
        this.container.withEnv("ETCD_LISTEN_CLIENT_URLS", "https://0.0.0.0:2379");
        this.container.withEnv("ETCD_ADVERTISE_CLIENT_URLS", "https://localhost:2379");
        this.container.withEnv("ETCDCTL_CACERT", "/etc/certificate/trustCA.pem");
        this.container.withEnv("ETCDCTL_CERT", "/etc/certificate/server-cert.pem");
        this.container.withEnv("ETCDCTL_INSECURE_TRANSPORT", "false");
        this.container.withEnv("ETCDCTL_KEY", "/etc/certificate/server-key.pem");
        this.container.withEnv("ETCDCTL_INSECURE_SKIP_TLS_VERIFY", "true");

    }

    public void start()
    {

        if (this.currentState.equals(State.STOPPED))
        {
            log.info("Starting ETCD container.");
            container.start();

            EtcdContainer.prepareUserAuth(container);

            if (dataDirectory != null)
            {
                log.info("Setting data directory permissions.");
                EtcdContainer.setDataDirectoryPermissions(container);
            }

            this.currentState = State.STARTED;
        }
        else
        {
            log.info("Etcd container is already started.");
        }
    }

    public void stop()
    {
        if (this.currentState.equals(State.STARTED))
        {
            log.info("Stopping ETCD container.");
            container.stop();

            if (dataDirectory != null)
            {
                log.info("Cleaning up local binded directory.");
                EtcdContainer.deleteLocalDirectory(dataDirectory);
            }

            this.currentState = State.STOPPED;
        }
        else
        {
            log.info("Etcd container is already stopped.");
        }
    }

    /**
     * Restart the ETCD container. If the ETCD container was not initialized as
     * stateful, then a runtime exception will be thrown.
     * 
     * @param restartDelay Additional delay to be applied to the restart of the
     *                     container.
     */
    public void restart(int restartDelay)
    {
        if (dataDirectory == null)
        {
            throw new IllegalStateException("ETCD container is not stateful, there is no point restarting it.");
        }

        log.info("Restarting ETCD container with additional delay of {} sec.", restartDelay);

        this.container.stop();
        this.currentState = State.STOPPED;

        Observable.timer(restartDelay, TimeUnit.SECONDS).blockingFirst();

        this.container.start();
        this.currentState = State.STARTED;
    }

    /**
     * @return the client port where ETCD listens to
     */
    public static int getEtcdListeningPort()
    {

        return ETCD_EXPOSED_PORT;
    }

    public int getEtcdPort()
    {
        return ETCD_EXPOSED_PORT;
        // return this.container.getMappedPort(ETCD_EXPOSED_PORT);

    }

    public ExecResult getExec(String command) throws UnsupportedOperationException, IOException, InterruptedException
    {
        return this.container.execInContainer(command);

    }

    public String getEtcdIp()
    {
        return this.container.getContainerIpAddress();

    }

    public String getHost()
    {
        return this.container.getHost();
    }

    /**
     * @return the user name for ETCD
     */
    public static String getEtcdClientUsername()
    {
        return ETCD_CLIENT_USERNAME;
    }

    /**
     * @return the password for ETCD
     */
    public static String getEtcdClientPassword()
    {
        return ETCD_CLIENT_PASSWORD;
    }

    /**
     * Prepare the docker ETCD image.
     * 
     * @return The docker image.
     */
    private static String prepareDockerImage()
    {
        String image = null;
        var imageTag = ETCD_DOCKER_IMAGE_NAME + ":" + ETCD_DOCKER_IMAGE_VERSION;
        Consumer<DockerfileBuilder> dockerfile = builder -> builder.from(imageTag).user("root").build();

        try
        {
            image = new ImageFromDockerfile().withDockerfileFromBuilder(dockerfile).get();
        }
        catch (Exception e)
        {
            throw new ContainerLaunchException("Error while preparing docker image.", e);
        }

        return image;
    }

    /**
     * Create a root user inside the ETCD container, grant the user with root role
     * and enable authentication.
     * 
     * @param container The active container for which to prepare user
     *                  authentication.
     */
    private static void prepareUserAuth(FixedHostPortGenericContainer<?> container)
    {
        try
        {
            container.execInContainer("etcdctl", "user", "add", "root:rootroot");
            container.execInContainer("etcdctl", "role", "add", "root");
            container.execInContainer("etcdctl", "user", "grant-role", "root", "root");
            container.execInContainer("etcdctl", "auth", "enable");
        }
        catch (UnsupportedOperationException | IOException | InterruptedException e)
        {
            throw new ContainerLaunchException("Error while preparing user authentication.", e);
        }
    }

    /**
     * Create a local temporary directory to bind it to the ETCD data directory
     * inside the container.
     * 
     * @param pathPrefix The prefix to be used to create the local directory.
     * @return The path of the created local directory.
     */
    private static Path createLocalDirectory(String pathPrefix)
    {
        try
        {
            return Files.createTempDirectory(pathPrefix);
        }
        catch (IOException e)
        {
            throw new ContainerLaunchException("Error while creating local etcd data directory.", e);
        }
    }

    /**
     * Delete the local binded directory that holds the ETCD data.
     * 
     * @param directory The local ETCD data directory path.
     */
    private static void deleteLocalDirectory(Path directory)
    {
        try
        {
            Files.walk(directory).forEach(sub -> sub.toFile().delete());
        }
        catch (IOException e)
        {
            log.error("Couldn't clean up local etcd data directory.", e);
        }
    }

    /**
     * Set the appropriate permissions for the data directory, so it is possible to
     * clean up the local data directory after terminating the container without
     * root permissions.
     * 
     * @param container The active container for which to set appropriate
     *                  permissions.
     */
    private static void setDataDirectoryPermissions(FixedHostPortGenericContainer<?> container)
    {
        try
        {
            container.execInContainer("chmod", "o+rwx", "-R", ETCD_DOCKER_DATA_DIR);
        }
        catch (UnsupportedOperationException | IOException | InterruptedException e)
        {
            throw new ContainerLaunchException("Error while setting permissions to container etcd data directory.", e);
        }
    }

}
