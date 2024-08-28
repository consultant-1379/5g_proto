package com.ericsson.esc.common.proxyal.proxyconfig;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig.ProxyCluster;
import com.ericsson.sc.proxyal.proxyconfig.proxyendpointconfig.ProxyEndpoint;

class ProxyClusterTest
{

    @BeforeEach
    void setUp() throws Exception
    {
    }

    @Test
    void test()
    {
        var cluster = new ProxyCluster("test1");
        assert (cluster.getName().equals("test1"));
        assert (cluster.getConnectTimeout() == 2.0); // default value

        // Add endpoints

        cluster.addEndpoint(new ProxyEndpoint("10.0.1.5", 80, 1));
        cluster.addEndpoint(new ProxyEndpoint("10.0.2.6", 80, 2));
        cluster.addEndpoint(new ProxyEndpoint("10.0.0.1", 80, 0));
        cluster.addEndpoint(new ProxyEndpoint("10.0.1.2", 80, 1));
        cluster.addEndpoint(new ProxyEndpoint("10.0.0.3", 80, 0));
        cluster.addEndpoint(new ProxyEndpoint("10.0.1.4", 80, 1));
        cluster.addEndpoint(new ProxyEndpoint("10.0.2.7", 80, 2));
        cluster.addEndpoint(new ProxyEndpoint("10.0.0.8", 80, 0));

        assert (cluster.getEndpoints().size() == 8);

        var epByPrio = cluster.getEndpointsByPriorities();
        assert (epByPrio.keySet().size() == 3);
        assert (epByPrio.keySet().contains(0));
        assert (epByPrio.keySet().contains(1));
        assert (epByPrio.keySet().contains(2));
        final var sizeForPrio = Arrays.asList(3, 3, 2);
        epByPrio.forEach((prio,
                          epList) ->
        {
            assert (epList.size() == sizeForPrio.get(prio));
        });
    }

}
