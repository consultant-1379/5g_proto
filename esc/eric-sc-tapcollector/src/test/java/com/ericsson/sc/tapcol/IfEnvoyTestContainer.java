package com.ericsson.sc.tapcol;

import org.testcontainers.containers.output.OutputFrame;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public interface IfEnvoyTestContainer
{

    String ENVOY_IMAGE_NAME = "armdocker.rnd.ericsson.se/proj-5g-bsf/envoy/eric-scp-envoy-base:1.21.1-16-vtap-multiplexed-2";

    // new
    // "armdocker.rnd.ericsson.se/proj-5g-bsf/envoy/eric-scp-envoy-base:1.23.0-4_vtap_enh_sp2";
    void start(String tapcolIpAddress,
               int tapcolPort) throws UnsupportedOperationException, IOException, InterruptedException;

    void stop();

    Integer getMappedPort(int port);

    String getHost();

    void setLogConsumers(List<Consumer<OutputFrame>> logConsumers);

    String getContainerIpAddress();

    String getGatewayIpAddress();

}
