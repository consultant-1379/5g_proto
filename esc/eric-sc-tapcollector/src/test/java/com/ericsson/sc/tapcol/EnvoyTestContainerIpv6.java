package com.ericsson.sc.tapcol;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.exception.NotModifiedException;
import com.github.dockerjava.api.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.output.OutputFrame;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public class EnvoyTestContainerIpv6 implements IfEnvoyTestContainer
{
    private static final Logger log = LoggerFactory.getLogger(EnvoyTestContainerIpv6.class);

    enum State
    {
        STARTED,
        STOPPED
    }

    private static final String CONTAINER_CONFIG_PATH = "/config/envoy-config.yaml";
    private static final String CONTAINER_EXEC_PATH = "/usr/local/bin/envoy";
    private static final String CONFIG_NAME = "envoy-config-grpc.yaml";

    private final DockerClient dockerClient;
    private String containerId;
    private final CreateContainerCmd containerCmd;
    private String gateway = "::1";
    private String containerIpAddress = "::3";
    private State currentState = State.STOPPED;

    public EnvoyTestContainerIpv6(Path tempDirectory)
    {
        this.dockerClient = DockerClientFactory.instance().client();
        this.containerCmd = this.dockerClient.createContainerCmd(ENVOY_IMAGE_NAME);
        log.info("Using temporary directory {}", tempDirectory);
        initializeContainer(tempDirectory);
    }

    private void initializeContainer(Path tempDirectory)
    {
        PortBinding pbIpv6 = new PortBinding(Ports.Binding.bindIp("::"), new ExposedPort(8091));

        var b = new Bind(Path.of(tempDirectory.toString(), CONFIG_NAME).toString(), new Volume(CONTAINER_CONFIG_PATH));
        var entrypoint = String.format("%s --config-path %s --base-id 1 --service-cluster front-envoy --service-node front-envoy --concurrency 4 --log-level debug",
                                       CONTAINER_EXEC_PATH,
                                       CONTAINER_CONFIG_PATH);
        log.info("Entrypoint command {}", entrypoint);
        this.containerCmd.withExposedPorts(new ExposedPort(8091)).withEntrypoint(entrypoint.split(" "));
        this.containerCmd.getHostConfig().withNetworkMode("host").withPortBindings(List.of(pbIpv6)).withBinds(List.of(b));
//                .withEntrypoint("sleep", "infinity");
    }

    @Override
    public void start(String tapcolIpAddress,
                      int tapcolPort) throws UnsupportedOperationException
    {
        if (this.currentState.equals(State.STARTED))
        {
            log.info("Container already started");
            return;
        }
        this.containerCmd.withEnv(tapcolIpAddress + "=" + tapcolIpAddress,
                                  "ERIC_TAP_IP_VERSION" + "=" + "6",
                                  "ERIC_TAP_COLLECTOR_HOST" + "=" + "[::1]",
                                  "ERIC_TAP_COLLECTOR_PORT" + "=" + tapcolPort);
//        this.containerCmd.getHostConfig().withExtraHosts("tapcol:10.63.183.142");
//        this.containerCmd.getHostConfig().withExtraHosts("tapcol:2001:1b70:8230:605:f816:3eff:fed7:9fe1");

        log.debug("Container creation cmd: {}", containerCmd);
        var container = containerCmd.exec();
        this.containerId = container.getId();
        log.info("Created container with id {}", containerId);

        dockerClient.startContainerCmd(containerId).exec();
        log.info("Started container with id {}", containerId);

        this.currentState = State.STARTED;

        try
        {
            Thread.sleep(2 * 1000L);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        var containerInfo = dockerClient.inspectContainerCmd(containerId).exec();
        this.gateway = containerInfo.getNetworkSettings().getGateway();
        this.containerIpAddress = containerInfo.getNetworkSettings().getIpAddress();
    }

    @Override
    public void stop()
    {
        if (State.STOPPED.equals(currentState))
        {
            log.info("Already stopped");
            return;
        }

        log.info("Stopping container {}", containerId);
        try
        {
            dockerClient.stopContainerCmd(containerId).exec();
        }
        catch (NotModifiedException e)
        {
            log.error("Container already stopped by daemon");
        }
        this.currentState = State.STOPPED;
        try
        {
            Thread.sleep(2 * 1000L);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public Integer getMappedPort(int port)
    {
        /*
         * For IPv6, host network type is used, so no port-forwarding happens
         */
        return port;
    }

    @Override
    public String getHost()
    {
        return "::";
    }

    @Override
    public void setLogConsumers(List<Consumer<OutputFrame>> logConsumers)
    {
        log.warn("Log streaming not implemented");
    }

    @Override
    public String getContainerIpAddress()
    {
        return "::1";// this.containerIpAddress;
    }

    @Override
    public String getGatewayIpAddress()
    {
        return "::1"; // this.gateway;
    }

}
