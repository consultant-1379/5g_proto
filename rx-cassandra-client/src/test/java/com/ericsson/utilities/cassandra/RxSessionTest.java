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
 * Created on: Jul 31, 2020
 *     Author: emldpng
 */

package com.ericsson.utilities.cassandra;

import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.ericsson.utilities.reactivex.RetryFunction;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;

/**
 * Functional testing for RxSession.
 */
public class RxSessionTest
{
    private static final Logger log = LoggerFactory.getLogger(RxSessionTest.class);

    @SuppressWarnings("rawtypes")
    private GenericContainer cassandra;
    private DriverConfigLoader defaultDriverConfig;
    private static final String LOCAL_DC_NAME = "datacenter1";

    @BeforeClass
    public void beforeClass()
    {
        log.info("Executing Before Class... ");

        // Note: It is vital to have a fixed mapped port to Cassandra's exposed port in
        // order to be able to test the retry mechanism of the RxSession. The only way
        // to do that, is to use this deprecated method.
        @SuppressWarnings("deprecation")
        Consumer<CreateContainerCmd> cmd = c -> c.withPortBindings(new PortBinding(Ports.Binding.bindPort(9042), new ExposedPort(9042)));
        final var cassandraDockerImage = DockerImageName.parse("armdockerhub.rnd.ericsson.se/cassandra:3.11.2").asCompatibleSubstituteFor("cassandra");
        cassandra = new CassandraContainer<>(cassandraDockerImage).withExposedPorts(9042).withCreateContainerCmdModifier(cmd);
        // cassandra.addEnv("JVM_OPTS", "-Xmx4048M");
        cassandra.addEnv("JVM_OPTS", "-Xms1024M -Xmx2048M");
        cassandra.start();

        // Store the contact point information of the container.
        final var contactPoint = cassandra.getHost() + ":" + cassandra.getFirstMappedPort();

        // Create a default driver configuration for all RxSessions.
        defaultDriverConfig = DriverConfigLoader.programmaticBuilder() //
                                                .withStringList(DefaultDriverOption.CONTACT_POINTS, Arrays.asList(contactPoint))
                                                .withString(DefaultDriverOption.LOAD_BALANCING_LOCAL_DATACENTER, LOCAL_DC_NAME)
                                                .withBoolean(DefaultDriverOption.RECONNECT_ON_INIT, true)
                                                .build();
    }

    @AfterClass
    public void afterClass()
    {
        cassandra.stop();
    }

    @Test(groups = "functest")
    public void testCreateCloseSessionSuccess()
    {
        RxSession session = null;

        try
        {
            // Create RxSession and assert that it was established.
            session = RxSession.builder().withConfig(defaultDriverConfig).build();
            var closed = session.sessionHolder().blockingGet().isClosed();
            log.info("Session closed: {}", closed);
            assertTrue(!closed, "The session was not established successfully!");

            // Close RxSession and assert that it was terminated.
            session.close().blockingGet(5, TimeUnit.SECONDS);
            closed = session.sessionHolder().blockingGet().isClosed();
            log.info("Session closed: {}", closed);
            assertTrue(closed, "The session was not terminated successfully!");
        }
        finally
        {
            if (session != null)
                session.close().blockingGet(5, TimeUnit.SECONDS);
        }
    }

    @Test(groups = "functest")
    public void testFetchDefaultKeyspacesSuccess()
    {
        RxSession session = null;

        try
        {
            var keyspaceList = List.of("system", "system_auth", "system_schema", "system_distributed", "system_traces");

            // Create RxSession and fetch the default keyspaces.
            session = RxSession.builder().withConfig(defaultDriverConfig).build();
            var statement = SimpleStatement.builder("SELECT * FROM system_schema.keyspaces;").build();
            var rxRows = session.sessionHolder() //
                                .flatMapPublisher(sh -> sh.executeReactive(statement))
                                .toList()
                                .blockingGet();
            var fetchedKeyspaces = rxRows.stream().map(row -> row.getString(0)).collect(Collectors.toList());

            // Assertions.
            assertTrue(keyspaceList.size() == fetchedKeyspaces.size(), "Wrong number of keyspaces fetched.");
            for (String keyspace : keyspaceList)
                assertTrue(fetchedKeyspaces.contains(keyspace), "Keyspace " + keyspace + "was not fetched.");
        }
        finally
        {
            if (session != null)
                session.close().blockingGet(5, TimeUnit.SECONDS);
        }
    }

    @Test(groups = "functest")
    public void testCreateDeleteKeyspaceSuccess()
    {
        RxSession session = null;
        final var keyspace = "test_keyspace";

        try
        {
            // Create RxSession and create a test keyspace.
            session = RxSession.builder().withConfig(defaultDriverConfig).build();
            final var createKS = "CREATE KEYSPACE " + keyspace + " WITH REPLICATION = {'class' : 'SimpleStrategy', 'replication_factor' : 1};";
            final var statement = SimpleStatement.builder(createKS).build();
            session.sessionHolder().flatMapPublisher(sh -> sh.executeReactive(statement)).blockingSubscribe(); // CREATE statement returns no results

            // Check that the test keyspace was created successfully.
            var fetchKS = "SELECT * FROM system_schema.keyspaces WHERE keyspace_name = '" + keyspace + "';";
            var statementFetch = SimpleStatement.builder(fetchKS).build();
            var rows = session.sessionHolder().flatMapPublisher(sh -> sh.executeReactive(statementFetch)).toList().blockingGet();

            assertTrue(rows.get(0).size() > 0 && rows.get(0).getString(0).equals(keyspace), "The keyspace was not created successfully!");

            // Delete the test keyspace.
            var deleteKS = "DROP KEYSPACE " + keyspace + ";";
            var statementDelete = SimpleStatement.builder(deleteKS).build();
            session.sessionHolder().flatMapPublisher(sh -> sh.executeReactive(statementDelete)).blockingSubscribe();

            // Check that the test keyspace was deleted.
            rows = session.sessionHolder().flatMapPublisher(sh -> sh.executeReactive(statementFetch)).toList().blockingGet();
            assertTrue(rows.size() == 0, "The keyspace was not deleted successfully!");

        }
        finally
        {
            if (session != null)
            {
                // Cleanup the keyspace if it was not deleted properly.
                var deleteKS = "DROP KEYSPACE IF EXISTS " + keyspace + ";";
                var statementDelete = SimpleStatement.builder(deleteKS).build();
                session.sessionHolder().flatMapPublisher(sh -> sh.executeReactive(statementDelete)).blockingSubscribe();
                // Close the RxSession.
                session.close().blockingGet(5, TimeUnit.SECONDS);
            }
        }
    }

    @Test(groups = "functest")
    public void testCreateTableSuccess()
    {
        RxSession session = null;
        var keyspace = "test_keyspace";
        var table = "test_table";

        try
        {

            // Create RxSession and create a test keyspace.
            session = RxSession.builder().withConfig(defaultDriverConfig).build();
            var createKS = "CREATE KEYSPACE " + keyspace + " WITH REPLICATION = {'class' : 'SimpleStrategy', 'replication_factor' : 1};";
            var statement = SimpleStatement.builder(createKS).build();
            session.sessionHolder() //
                   .flatMapPublisher(sh -> sh.executeReactive(statement))
                   .blockingSubscribe();

            // Check that the test keyspace was created successfully.
            var fetchKS = "SELECT * FROM system_schema.keyspaces WHERE keyspace_name = '" + keyspace + "';";
            var statementFetch = SimpleStatement.builder(fetchKS).build();
            var rows = session.sessionHolder() //
                              .flatMapPublisher(sh -> sh.executeReactive(statementFetch))
                              .toList()
                              .blockingGet();

            assertTrue(rows.get(0).size() > 0 && rows.get(0).getString(0).equals(keyspace), "The keyspace was not created successfully!");

            // Create a test table.
            var createTb = "CREATE TABLE " + keyspace + "." + table + " (id int PRIMARY KEY, lastname text, firstname text);";
            var statement1 = SimpleStatement.builder(createTb).build();
            session.sessionHolder() //
                   .flatMapPublisher(sh -> sh.executeReactive(statement1))
                   .blockingSubscribe();

            var fetchTb2 = "INSERT INTO " + keyspace + "." + table + " (id, lastname, firstname) VALUES (1, 'Fotis', 'Soldatos');";
            var statementFetch_2 = SimpleStatement.builder(fetchTb2).build();
            session.sessionHolder() //
                   .flatMapPublisher(sh -> sh.executeReactive(statementFetch_2))
                   .blockingSubscribe();

            // Check that the test table was created successfully.
            var fetchTb = "SELECT * FROM " + keyspace + "." + table + ";";
            var statementFetch_1 = SimpleStatement.builder(fetchTb).build();
            var rows1 = session.sessionHolder()//
                               .flatMapPublisher(sh -> sh.executeReactive(statementFetch_1))
                               .toList()
                               .blockingGet();
            log.info("rows1: " + rows1.get(0).getString(1));

            assertTrue(rows1.size() > 0, "The table was not created successfully!");

            // Delete the test keyspace.
            var deleteKS = "DROP KEYSPACE " + keyspace + ";";
            var statementDelete = SimpleStatement.builder(deleteKS).build();
            session.sessionHolder() //
                   .flatMapPublisher(sh -> sh.executeReactive(statementDelete))
                   .blockingSubscribe();

            // Check that the test keyspace was deleted.
            rows = session.sessionHolder() //
                          .flatMapPublisher(sh -> sh.executeReactive(statementFetch))
                          .toList()
                          .blockingGet();
            assertTrue(rows.size() == 0, "The keyspace was not deleted successfully!");

        }
        finally
        {

            if (session != null)
            {
                // Cleanup the keyspace if it was not deleted properly.
                var deleteKS = "DROP KEYSPACE IF EXISTS " + keyspace + ";";
                var statementDelete = SimpleStatement.builder(deleteKS).build();
                session.sessionHolder() //
                       .flatMapPublisher(sh -> sh.executeReactive(statementDelete))
                       .blockingSubscribe();
                // Close the RxSession.
                session.close().blockingGet(5, TimeUnit.SECONDS);
            }

        }

    }

    @Test(groups = "functest")
    public void testCreateSessionDropReconnectSuccess()
    {
        RxSession session = null;

        try
        {

            // Create RxSession.
            session = RxSession.builder().withConfig(defaultDriverConfig).build();

            // Check that Keyspaces are fetched successfully.
            var fetchTb = "SELECT * FROM system_schema.keyspaces;";
            var statementFetch_1 = SimpleStatement.builder(fetchTb).build();
            var rows1 = session.sessionHolder().flatMapPublisher(sh -> sh.executeReactive(statementFetch_1)).toList().blockingGet();
            log.info("rows1: " + rows1.get(0).getString(0));

            assertTrue(rows1.size() > 0, "Keyspaces were not fetched successfully!");

            // Ensure Cassandra is not running.
            if (cassandra.isRunning())
            {
                log.info("Cassandra container is currently running, so it must be stopped for this test.");
                log.info("Stopping Cassandra container");
                cassandra.stop();
            }

            // Start now the Cassandra container.
            log.info("Starting the Cassandra container");
            cassandra.start();

            // Check that the RxSession is still established (after Cassandra reconnection).
            var closed = session.sessionHolder().blockingGet().isClosed();
            log.info("The session is still established : {}", !closed);
            assertTrue(!closed, "The session is not established");

            // Check that the query fetches Keyspaces successfully again.
            var fetchKS1 = "SELECT * FROM system_schema.keyspaces;";
            var statementFetch2 = SimpleStatement.builder(fetchKS1).build();
            var rows3 = session.sessionHolder() //
                               .flatMapPublisher(sh -> sh.executeReactive(statementFetch2))
                               .retryWhen(new RetryFunction().withDelay(1000).withRetries(30).create())
                               .toList()
                               .blockingGet();

            assertTrue(rows3.get(0).size() > 0, "The query did not fetched data successfully!");
            log.info("rows3: " + rows3.get(0).getString(0));
            log.info("Keyspaces were fetched successfully again!");

        }
        finally
        {

            if (session != null)
            {

                // Close the RxSession.
                session.close().blockingGet(5, TimeUnit.SECONDS);
            }

            // If the container is still down start it, to avoid test disruption.
            if (!cassandra.isRunning())
            {

                cassandra.start();

            }

        }
    }

    @Test(groups = "functest")
    public void testInitialConnectionRetriesSuccess()
    {
        RxSession session = null;

        try
        {
            // Ensure Cassandra is not running.
            if (cassandra.isRunning())
            {
                log.info("Cassandra container is currently running, so it must be stopped for this test.");
                log.info("Stopping Cassandra container");
                cassandra.stop();
            }

            // Create RxSession and subscribe. Since the Cassandra container is not up the
            // retry mechanism will be activated.
            session = RxSession.builder().withConfig(defaultDriverConfig).build();
            session.sessionHolder().subscribe();

            // Start now the Cassandra container.
            log.info("Starting the Cassandra container");
            cassandra.start();

            // Check that the RxSession was established successfully.
            var closed = session.sessionHolder().blockingGet().isClosed();
            log.info("The session was established: {}", !closed);
            assertTrue(!closed, "The session was not established after the retries!");
        }
        finally
        {
            if (session != null)
                session.close().blockingGet(5, TimeUnit.SECONDS);

            // If the container is still down start it, to avoid test disruption.
            if (!cassandra.isRunning())
                cassandra.start();
        }
    }

    /**
     * Ensure variable configuration is applied correctly and old sessions are
     * properly closed
     */

    /*
     * @Test(groups = "functest") public void testSessionChange() {
     * BehaviorSubject<DriverConfigLoader> configs = BehaviorSubject.create();
     * 
     * final var rxCassandra =
     * RxSession.builder().withConfigFlow(configs.toFlowable(BackpressureStrategy.
     * BUFFER)).build();
     * 
     * final TestSubscriber<RxSession.Sessions> sessions = rxCassandra.run().test();
     * sessions.assertNoValues();
     * 
     * configs.onNext(this.defaultDriverConfig); sessions.awaitCount(1); final var
     * sessions1 = (RxSession.Sessions) sessions.getEvents().get(0).get(0);
     * assertTrue(sessions1.getPrevious().isEmpty());
     * rxCassandra.executeReactive(SimpleStatement.
     * newInstance("SELECT host_id FROM system.local")).toList().blockingGet().get(0
     * ).getUuid(0);
     * 
     * configs.onNext(defaultDriverConfig); sessions.awaitCount(2); final var
     * sessions2 = (RxSession.Sessions) sessions.getEvents().get(0).get(1);
     * assertEquals(sessions2.getPrevious().get(), sessions1.getCurrent());
     * 
     * rxCassandra.executeReactive(SimpleStatement.
     * newInstance("SELECT host_id FROM system.local")).toList().blockingGet().get(0
     * ).getUuid(0); final var closed =
     * sessions2.getPrevious().get().waitUntilClosed().blockingAwait(10,
     * TimeUnit.SECONDS); assertTrue(closed);
     * assertTrue(sessions2.getPrevious().get().getCqlSession().isClosed());
     * 
     * configs.onNext(defaultDriverConfig); sessions.awaitCount(3); final var
     * sessions3 = (RxSession.Sessions) sessions.getEvents().get(0).get(2);
     * assertEquals(sessions3.getPrevious().get(), sessions2.getCurrent());
     * 
     * rxCassandra.executeReactive(SimpleStatement.
     * newInstance("SELECT host_id FROM system.local")).toList().blockingGet().get(0
     * ).getUuid(0);
     * assertTrue(sessions3.getPrevious().get().waitUntilClosed().blockingAwait(10,
     * TimeUnit.SECONDS));
     * assertTrue(sessions3.getPrevious().get().getCqlSession().isClosed());
     * 
     * rxCassandra.close().blockingAwait();
     * 
     * assertTrue(sessions3.getCurrent().getCqlSession().isClosed());
     * 
     * assertEquals(sessions.getEvents().get(0).size(), 3);
     * sessions.assertComplete();
     * 
     * sessions.dispose();
     * 
     * assertEquals(rxCassandra.run().blockingLast(), sessions3);
     * 
     * }
     */

}
