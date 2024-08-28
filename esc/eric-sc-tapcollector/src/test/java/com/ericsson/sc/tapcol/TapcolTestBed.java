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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

/**
 * This class provides all necessary functionality for integration tests which
 * are based on tapcollector package. It deploys an Envoy container in order to
 * use it as a server instance and creates a tapcollector running instance.
 */
public class TapcolTestBed implements AutoCloseable
{
    private static final Logger log = LoggerFactory.getLogger(TapcolTestBed.class);

    // Envoy configuration
    private static final String CONFIG_NAME = "envoy-config-grpc.yaml";
    private static final String CONFIG_NAME_IP6 = "envoy-config-grpc-ip6.yaml";

    // Errors
    private static final String ERROR_TEMP_DELETE = "Error in temporary directory deletion process";

    private final IfEnvoyTestContainer container;

    private final Path envoyTempDirectory = Files.createTempDirectory("envoyconfig");
    private final Path configFilePath = Path.of("src", "test", "resources");

    private boolean ipv6 = false;

    /**
     * Runs an Envoy container and applies the proper configuration to it.
     *
     * @param tapCollectorIpAddress
     * @throws UnsupportedOperationException
     * @throws IOException
     * @throws InterruptedException
     */
    public TapcolTestBed(String tapCollectorIpAddress,
                         int tapCollectorPort,
                         boolean ipv6) throws UnsupportedOperationException, IOException, InterruptedException
    {
        this.ipv6 = ipv6;
        // Copy config file to temp path
        var configName = Path.of(configFilePath.toString(), ipv6 ? CONFIG_NAME_IP6 : CONFIG_NAME);
        Files.copy(configName, Path.of(this.envoyTempDirectory.toString(), CONFIG_NAME), StandardCopyOption.REPLACE_EXISTING);

        this.setTapColListeningPort(tapCollectorIpAddress, tapCollectorPort);

        this.container = ipv6 ? new EnvoyTestContainerIpv6(this.envoyTempDirectory) : new EnvoyTestContainer(this.envoyTempDirectory);
        this.container.setLogConsumers(List.of(f ->
        {
            System.out.print(f.getUtf8String());
        }));
        this.startEnvoyContainer(tapCollectorIpAddress, tapCollectorPort);
    }

    /**
     * Starts the Envoy container.
     *
     * @param tapCollectorIpAddress The IP address in which tapcollector listens to.
     * @param tapCollectorPort      The tap collector listening port
     * @throws UnsupportedOperationException
     * @throws IOException
     * @throws InterruptedException
     */
    public void startEnvoyContainer(String tapCollectorIpAddress,
                                    int tapCollectorPort) throws UnsupportedOperationException, IOException, InterruptedException
    {
        this.container.start(tapCollectorIpAddress, tapCollectorPort);
    }

    /**
     * Stops the Envoy container.
     */
    private void stopEnvoyContainer()
    {
        this.container.stop();
    }

    public IfEnvoyTestContainer getEnvoyContainer()
    {
        return this.container;
    }

    /**
     * Get the target port that can be used to send traffic to the Envoy server.
     *
     * @return The port.
     */
    public int getTargetPort()
    {
        return ipv6 ? this.container.getMappedPort(8091) : this.container.getMappedPort(8090);
    }

    /**
     * Get the target IP address that can be used to send traffic to the Envoy
     * server.
     *
     * @return The IP address.
     */
    public String getTargetIpAddress()
    {
        return this.container.getHost();
    }

    /**
     * Get the container IP address in its docker network.
     *
     * @return The container IP address.
     */
    @SuppressWarnings("deprecation")
    public String getContainerIpAddress()
    {
        return this.container.getContainerIpAddress();
    }

    /**
     * Get the IP address of the gateway in the docker network where this container
     * belongs.
     *
     * @return
     */
    @SuppressWarnings("deprecation")
    public String getGatewayIpAddress()
    {
        return this.container.getGatewayIpAddress();
    }

    public void deleteTemporaryDirectory(Path tempDirectory)
    {

        try (Stream<Path> walk = Files.walk(tempDirectory))
        {
            walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
        catch (IOException e)
        {
            log.error(ERROR_TEMP_DELETE, e);
        }
    }

    public Path getEnvoyDirectory()
    {
        return this.envoyTempDirectory;
    }

    /**
     * Stops the envoy container and deletes the temporary directory that contains
     * the configuration file.
     */
    @Override
    public void close()
    {
        this.stopEnvoyContainer();
        this.deleteTemporaryDirectory(this.envoyTempDirectory);
    }

    /**
     * Modifies the tapcollector listening port in the configuration file that is
     * used by Envoy.
     *
     * @param port The tapcollector port.
     * @throws IOException
     */
    public void setTapColListeningPort(String ip,
                                       int port) throws IOException
    {
        final var mapper = new ObjectMapper(new YAMLFactory());

        final var jsonMapper = new ObjectMapper();

        final var configFile = new File(Path.of(this.envoyTempDirectory.toString(), CONFIG_NAME).toString());

        final var jsonString = jsonMapper.writeValueAsString(mapper.readValue(configFile, Object.class));

        final var newConfigJson = jsonString.replaceAll("tapcol:[0-9]+", String.format("tapcol:%s", port));

        final var newConfigString = new YAMLMapper().writeValueAsString(new ObjectMapper().readTree(newConfigJson));

        final var inputStream = new ByteArrayInputStream(newConfigString.getBytes());

        Files.copy(inputStream, Path.of(this.envoyTempDirectory.toString(), CONFIG_NAME), StandardCopyOption.REPLACE_EXISTING);
    }

}