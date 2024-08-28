package com.ericsson.sim.loadgen;

import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sim.loadgen.LoadGenerator.Configuration;
import com.ericsson.sim.loadgen.LoadGenerator.Configuration.Rlf;
import com.ericsson.sim.loadgen.LoadGenerator.Configuration.Rlf.PullRequest;

class LoadGeneratorTest
{
    private static final Logger log = LoggerFactory.getLogger(LoadGeneratorTest.class);

//    @Test
    void test_0_General() throws IOException, InterruptedException
    {
        final LoadGenerator server = new LoadGenerator(Optional.of(new Configuration().setRlf(new Rlf().setNumInstances(1)
                                                                                                       .setPullRequest(new PullRequest().setRate(60)))));

        log.info("config={}", server.getConfiguration());

        // Inform everyone interested about the changes:
        server.setConfiguration(server.getConfiguration());

        new Thread(server::run).start();

        Thread.sleep(5000);

        log.info("Started load generator.");
    }
}
