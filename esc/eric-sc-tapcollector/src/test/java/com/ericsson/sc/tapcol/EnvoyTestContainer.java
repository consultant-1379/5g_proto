/**
 * COPYRIGHT ERICSSON GMBH 2022
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Apr 13, 2022
 *     Author: zpavcha
 */

package com.ericsson.sc.tapcol;

import java.io.IOException;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

/**
 * Envoy Test Container that is used as a traffic generator for testing
 * tapcollector
 */
public class EnvoyTestContainer extends GenericContainer<EnvoyTestContainer> implements IfEnvoyTestContainer
{
    enum State
    {
        STARTED,
        STOPPED
    }

    private static final Logger log = LoggerFactory.getLogger(EnvoyTestContainer.class);

    // Image
    private static final DockerImageName ENVOY_IMAGE = DockerImageName.parse(IfEnvoyTestContainer.ENVOY_IMAGE_NAME).asCompatibleSubstituteFor("envoy");
    private static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("envoy");

    // Configuration and executable - External files
    private static final String CONTAINER_CONFIG_PATH = "/config/envoy-config.yaml";
    private static final String CONTAINER_EXEC_PATH = "/usr/local/bin/envoy";
    private static final String CONFIG_NAME = "envoy-config-grpc.yaml";

    // Container states
    private static final String START = "Starting Envoy container.";
    private static final String ALREADY_STARTED = "Envoy container is already started.";
    private static final String STOP = "Stopping Envoy container.";
    private static final String ALREADY_STOPPED = "Envoy container is already stopped.";

    // Container commands
    private static final String HOSTS_CMD = "echo \"%s tapcol\" >> /etc/hosts";
    private static final String RUN_COMMAND = String.format("%s --config-path %s --service-cluster front-envoy --service-node front-envoy --concurrency 4 --log-level debug",
                                                            CONTAINER_EXEC_PATH,
                                                            CONTAINER_CONFIG_PATH);

    // String literals
    private static final String PORT_BINDINGS_LIT = "Port bindings: {} -> {} , {} -> {}";
    private static final String IP_ADDR_LIT = "Envoy Container IP address: {}";
    private static final String HOSTS_CMD_RESULT_LIT = "Result of updating hosts command: {}";
    private static final String HOSTS_CMD_LIT = "Running command: {}";

    private State currentState = State.STOPPED;

    /**
     * Initializes the Envoy container. In order for this container to run properly,
     * both a configuration and an executable file must be provided.
     *
     * @param tempDirectory The path of directory that contains the aforementioned
     *                      required external files.
     */
    public EnvoyTestContainer(Path tempDirectory)
    {
        super(ENVOY_IMAGE);
        final var envoyDockerImage = (ENVOY_IMAGE);
        envoyDockerImage.assertCompatibleWith(DEFAULT_IMAGE_NAME);

        this.configureContainer(tempDirectory);
    }

    /**
     * Starts the Envoy container.
     *
     * @param tapcolIpAddress The IP address of running tapcollector instance.
     * @throws UnsupportedOperationException
     * @throws IOException
     * @throws InterruptedException
     */
    public void start(String tapcolIpAddress,
                      int tapcolPort) throws UnsupportedOperationException, IOException, InterruptedException
    {
        if (this.currentState.equals(State.STOPPED))
        {
            log.info(START);
            this.addEnv(tapcolIpAddress, tapcolIpAddress);
            this.addEnv("ERIC_TAP_IP_VERSION", "4");
            this.addEnv("ERIC_TAP_COLLECTOR_HOST", tapcolIpAddress);
            this.addEnv("ERIC_TAP_COLLECTOR_PORT", String.valueOf(tapcolPort));
            super.start();

            log.info(PORT_BINDINGS_LIT, 8090, this.getMappedPort(8090), 9901, this.getMappedPort(9901));
            log.info(IP_ADDR_LIT, this.getContainerIpAddress());

            /*
             * final var execResult = this.execInContainer("sh", "-c",
             * String.format(HOSTS_CMD, tapcolIpAddress));
             *
             * log.info(HOSTS_CMD_RESULT_LIT, execResult.toString());
             */
            this.currentState = State.STARTED;
        }
        else
        {
            log.info(ALREADY_STARTED);
        }
    }

    /**
     * Stops the Envoy container.
     */
    @Override
    public void stop()
    {
        if (this.currentState.equals(State.STARTED))
        {
            log.info(STOP);
            super.stop();
            this.currentState = State.STOPPED;
        }
        else
        {
            log.info(ALREADY_STOPPED);
        }
    }

    /**
     * Applies the necessary configuration to the Envoy container.
     *
     * @param tempDirectory The path of directory that contains the required
     *                      external files.
     */
    private void configureContainer(Path tempDirectory)
    {
        this.withFileSystemBind(Path.of(tempDirectory.toString(), CONFIG_NAME).toString(), CONTAINER_CONFIG_PATH, BindMode.READ_WRITE)
            .withExposedPorts(8090, 9901)
            .withCommand(RUN_COMMAND);

        log.info(HOSTS_CMD_LIT, RUN_COMMAND);
    }

    @Override
    public Integer getMappedPort(int originalPort)
    {
        return super.getMappedPort(originalPort);
    }

    @Override
    public String getHost()
    {
        return super.getHost();
    }

    public String getContainerIpAddress()
    {
        return this.getContainerInfo().getNetworkSettings().getIpAddress();
    }

    public String getGatewayIpAddress()
    {
        return this.getContainerInfo().getNetworkSettings().getGateway();
    }
}