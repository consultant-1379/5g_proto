package com.ericsson.esc.bsf.manager;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.internal.core.auth.PlainTextAuthProvider;
import com.ericsson.adpal.cm.actions.ActionInput;
import com.ericsson.esc.bsf.db.DbConfiguration;
import com.ericsson.esc.bsf.db.DbConfiguration.SizeTieredCompactionStrategy;
import com.ericsson.sc.bsf.model.Datacenter;
import com.ericsson.sc.bsf.model.EricssonBsfBsfFunctionNfInstanceBsfServiceBindingDatabaseCheckDbSchemaOutput;
import com.ericsson.sc.bsf.model.EricssonBsfDatacenter;
import com.ericsson.sc.bsf.model.Input;
import com.ericsson.sc.fm.FmAlarmHandler;
import com.ericsson.utilities.cassandra.CassandraTestServer;
import com.ericsson.utilities.cassandra.RxSession;
import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.reactivex.Single;

public class ActionHandlerTest
{
    private static final String KEYSPACE = "myKeyspace";
    private static final String LOCAL_DC_NAME = "datacenter1";
    private static final String NEW_LOCAL_DC_NAME = "datacenter2";
    private static final String DNN_SNSSAI_VIEW = "ue_dnn_snssai_mv";
    private static final Logger log = LoggerFactory.getLogger(ActionHandlerTest.class);
    private final CassandraTestServer testBed = new CassandraTestServer();
    private RxSession rxSession;
    private List<EricssonBsfDatacenter> datacenters;
    private List<EricssonBsfDatacenter> updatedDatacenters;
    private DbConfiguration dbConfig;
    private static final String ERICSSON_BSF_SCHEMA_NAME = "ericsson-bsf";

    @Test(groups = "functest")
    public void initDbTest()
    {
        var alarmHandler = new FmAlarmHandler(null, null, 0, false);
        var initializeDb = new InitializeDbActionHandler(ERICSSON_BSF_SCHEMA_NAME, rxSession, dbConfig, new FmAlarmServiceImplDummy(alarmHandler));
        final var actionParams = new Input();

        actionParams.setEricssonBsfDatacenter(datacenters);
        final var actionResult = initializeDb.executeAction(Single.just(new ActionInput("configuratioName",
                                                                                        "context",
                                                                                        "etag",

                                                                                        Jackson.om().valueToTree(actionParams))));

        final var result = actionResult.blockingGet();
        log.info("ActionResult: {}", result);

        // Assert that action result is success
        assertTrue(result.getError().isEmpty());

    }

    @Test(groups = "functest", dependsOnMethods = { "initDbTest" })
    public void checkDbTest() throws JsonProcessingException
    {
        final var checkDbSchema = new CheckDbSchemaActionHandler(rxSession, dbConfig);
        final var actionParams = List.of();

        final var actionResult = checkDbSchema.executeAction(Single.just(new ActionInput("configuratioName",
                                                                                         "context",
                                                                                         "etag",
                                                                                         Jackson.om().valueToTree(actionParams))));
        final var result = actionResult.blockingGet();

        log.info("ActionResult: {}", result);

        // Assert no errors
        assertTrue(result.getError().isEmpty());
        final var parsedOutput = Jackson.om()
                                        .treeToValue(result.getOutput().get(),
                                                     EricssonBsfBsfFunctionNfInstanceBsfServiceBindingDatabaseCheckDbSchemaOutput.class);
        // Ensure replication factors/datacenters are the same as the one used in
        // initialization of the schema by previous testcase
        assertEquals(parsedOutput.getEricssonBsfTopology().getDatacenter(), convert(this.datacenters));

        // Assert schema ready
        assertTrue(parsedOutput.getEricssonBsfStatus().getReady());

        // Assert there is no error message
        assertNull(parsedOutput.getEricssonBsfStatus().getInfo());

    }

    @Test(groups = "functest", dependsOnMethods = { "initDbTest" })
    public void updateDbTest()
    {
        var updateDbTopology = new UpdateDbTopologyActionHandler(rxSession, dbConfig);
        final var actionParams = new Input();

        actionParams.setEricssonBsfDatacenter(updatedDatacenters);
        ;

        final var actionResult = updateDbTopology.executeAction(Single.just(new ActionInput("configuratioName",
                                                                                            "context",
                                                                                            "etag",

                                                                                            Jackson.om().valueToTree(actionParams))));

        final var result = actionResult.blockingGet();
        log.info("UpdateResult: {}", result);

        // Assert that action result is success
        assertTrue(result.getError().isEmpty());

        // Ensure that DB can be successfully re-initialized
        this.initDbTest();
    }

    @Test(groups = "functest", dependsOnMethods = { "checkDbTest" })
    public void checkDbFailTestMissingKeyspace() throws JsonProcessingException
    {
        dropKeyspace();

        final var checkDbSchema = new CheckDbSchemaActionHandler(rxSession, dbConfig);
        final var actionParams = List.of();

        final var actionResult = checkDbSchema.executeAction(Single.just(new ActionInput("configuratioName",
                                                                                         "context",
                                                                                         "etag",
                                                                                         Jackson.om().valueToTree(actionParams))));
        final var result = actionResult.blockingGet();

        // Assert no errors
        log.info("ActionResult: {}", result);
        // Assert action performed successfully
        assertTrue(result.getError().isEmpty());

        final var parsedOutput = Jackson.om()
                                        .treeToValue(result.getOutput().get(),
                                                     EricssonBsfBsfFunctionNfInstanceBsfServiceBindingDatabaseCheckDbSchemaOutput.class);
        parsedOutput.getEricssonBsfTopology();

        // Assert schema not ready
        assertFalse(parsedOutput.getEricssonBsfStatus().getReady());
        assertNotNull(parsedOutput.getEricssonBsfStatus().getInfo());

        // Ensure that DB can be successfully re-initialized
        this.initDbTest();

    }

    @Test(groups = "functest", dependsOnMethods = { "checkDbTest" })
    public void checkDbFailTestMissingMaterializedView() throws JsonProcessingException
    {
        dropMaterializedView();

        final var checkDbSchema = new CheckDbSchemaActionHandler(rxSession, dbConfig);
        final var actionParams = List.of();

        final var actionResult = checkDbSchema.executeAction(Single.just(new ActionInput("configuratioName",
                                                                                         "context",
                                                                                         "etag",
                                                                                         Jackson.om().valueToTree(actionParams))));
        final var result = actionResult.blockingGet();

        // Assert no errors
        log.info("ActionResult: {}", result);
        // Assert action performed successfully
        assertTrue(result.getError().isEmpty());

        final var parsedOutput = Jackson.om()
                                        .treeToValue(result.getOutput().get(),
                                                     EricssonBsfBsfFunctionNfInstanceBsfServiceBindingDatabaseCheckDbSchemaOutput.class);
        parsedOutput.getEricssonBsfTopology();

        // Assert schema not ready
        assertFalse(parsedOutput.getEricssonBsfStatus().getReady());
        assertNotNull(parsedOutput.getEricssonBsfStatus().getInfo());

        // Ensure that DB can be successfully re-initialized
        this.initDbTest();

    }

    @Test(groups = "functest", dependsOnMethods = { "checkDbTest" })
    public void checkDbFailTestMissingBsfUser() throws JsonProcessingException
    {
        dropBsfUser();

        final var checkDbSchema = new CheckDbSchemaActionHandler(rxSession, dbConfig);
        final var actionParams = List.of();

        final var actionResult = checkDbSchema.executeAction(Single.just(new ActionInput("configuratioName",
                                                                                         "context",
                                                                                         "etag",
                                                                                         Jackson.om().valueToTree(actionParams))));
        final var result = actionResult.blockingGet();

        // Assert no errors
        log.info("ActionResult: {}", result);
        // Assert action performed successfully
        assertTrue(result.getError().isEmpty());

        final var parsedOutput = Jackson.om()
                                        .treeToValue(result.getOutput().get(),
                                                     EricssonBsfBsfFunctionNfInstanceBsfServiceBindingDatabaseCheckDbSchemaOutput.class);
        parsedOutput.getEricssonBsfTopology();

        // Assert schema not ready
        assertFalse(parsedOutput.getEricssonBsfStatus().getReady());
        assertNotNull(parsedOutput.getEricssonBsfStatus().getInfo());

        // Ensure that DB can be successfully re-initialized
        this.initDbTest();

    }

    @Test(groups = "functest", dependsOnMethods = { "checkDbTest" })
    public void checkDbFailTestBsfUserWithNoPermissions() throws JsonProcessingException
    {
        revokePermissionsFromBsfUser();

        final var checkDbSchema = new CheckDbSchemaActionHandler(rxSession, dbConfig);
        final var actionParams = List.of();

        final var actionResult = checkDbSchema.executeAction(Single.just(new ActionInput("configuratioName",
                                                                                         "context",
                                                                                         "etag",
                                                                                         Jackson.om().valueToTree(actionParams))));
        final var result = actionResult.blockingGet();

        // Assert no errors
        log.info("ActionResult: {}", result);
        // Assert action performed successfully
        assertTrue(result.getError().isEmpty());

        final var parsedOutput = Jackson.om()
                                        .treeToValue(result.getOutput().get(),
                                                     EricssonBsfBsfFunctionNfInstanceBsfServiceBindingDatabaseCheckDbSchemaOutput.class);
        parsedOutput.getEricssonBsfTopology();

        // Assert schema not ready
        assertFalse(parsedOutput.getEricssonBsfStatus().getReady());
        assertNotNull(parsedOutput.getEricssonBsfStatus().getInfo());

        // Ensure that DB can be successfully re-initialized
        this.initDbTest();

    }

    @Test(groups = "functest", dependsOnMethods = { "initDbTest" })
    public void updateDbTestMissingKeyspace()
    {
        this.dropKeyspace();

        var updateDbTopology = new UpdateDbTopologyActionHandler(rxSession, dbConfig);
        final var actionParams = new Input();

        actionParams.setEricssonBsfDatacenter(updatedDatacenters);
        ;

        final var actionResult = updateDbTopology.executeAction(Single.just(new ActionInput("configuratioName",
                                                                                            "context",
                                                                                            "etag",

                                                                                            Jackson.om().valueToTree(actionParams))));

        final var result = actionResult.blockingGet();
        log.info("UpdateResult: {}", result);

        // Assert that action result is success
        assertTrue(result.getError().isPresent());

        // Ensure that DB can be successfully re-initialized
        this.initDbTest();
    }

    private List<Datacenter> convert(List<EricssonBsfDatacenter> ddc)
    {
        return ddc.stream().map(x ->
        {
            final var y = new Datacenter();
            y.setName(x.getName());
            y.setReplicationFactor(x.getReplicationFactor());
            return y;
        }).collect(Collectors.toList());
    }

    private void dropKeyspace()
    {
        this.rxSession.sessionHolder() //
                      .flatMapPublisher(sh -> sh.executeReactive(SimpleStatement.newInstance("drop keyspace " + KEYSPACE)))
                      .blockingSubscribe();

    }

    private void dropMaterializedView()
    {
        this.rxSession.sessionHolder() //
                      .flatMapPublisher(sh -> sh.executeReactive(SimpleStatement.newInstance("drop materialized view " + KEYSPACE + "." + DNN_SNSSAI_VIEW)))
                      .blockingSubscribe();

    }

    private void dropBsfUser()
    {
        this.rxSession.sessionHolder() //
                      .flatMapPublisher(sh -> sh.executeReactive(SimpleStatement.newInstance("drop role '" + this.dbConfig.getUser() + "'")))
                      .blockingSubscribe();

    }

    private void revokePermissionsFromBsfUser()
    {
        this.rxSession.sessionHolder() //
                      .flatMapPublisher(sh -> sh.executeReactive(SimpleStatement.newInstance("revoke modify permission on keyspace " + KEYSPACE + " FROM '"
                                                                                             + this.dbConfig.getUser() + "'")))
                      .blockingSubscribe();

    }

    private DbConfiguration createDbConfiguration()
    {
        final var dbConfig = new DbConfiguration(KEYSPACE, List.of(testBed.getContactPoint()), "datacenter1");
        dbConfig.setAdminCredentials(testBed.getUsername(), testBed.getPassword());
        dbConfig.setConsistency("ONE");
        dbConfig.setUserCredentials("bsfUser", "bsfPassword");
        dbConfig.setGcGrace(864000);
        dbConfig.setCompactionStrategy(new SizeTieredCompactionStrategy(2, 0.5, 32, 3)); // default compaction
        return dbConfig;
    }

    private List<EricssonBsfDatacenter> createDc()
    {
        final var dc = new EricssonBsfDatacenter();
        dc.setName(LOCAL_DC_NAME);
        dc.setReplicationFactor(2);
        return List.of(dc);
    }

    private List<EricssonBsfDatacenter> updateDc()
    {
        final var dc = new EricssonBsfDatacenter();
        dc.setName(NEW_LOCAL_DC_NAME);
        dc.setReplicationFactor(3);
        return List.of(dc);
    }

    @BeforeClass
    public void beforeClass()
    {
        log.info("Executing Before Class... ");
        testBed.startCassandra();

        /*
         * final var cassandraCfg = new DbConfiguration(KEYSPACE, null, "datacenter1");
         * cassandraCfg.setConsistency("ONE"); cassandraCfg.setGcGrace(1);
         * cassandraCfg.setMemTableFlushPeriod(300000);
         */
        final var cassandraDriverConf = DriverConfigLoader.programmaticBuilder() //
                                                          .withStringList(DefaultDriverOption.CONTACT_POINTS, Arrays.asList(testBed.getContactPoint()))
                                                          .withBoolean(DefaultDriverOption.RECONNECT_ON_INIT, true)
                                                          .withString(DefaultDriverOption.LOAD_BALANCING_LOCAL_DATACENTER, LOCAL_DC_NAME)
                                                          .withString(DefaultDriverOption.REQUEST_CONSISTENCY, "ONE")
                                                          .withClass(DefaultDriverOption.AUTH_PROVIDER_CLASS, PlainTextAuthProvider.class)
                                                          .withString(DefaultDriverOption.AUTH_PROVIDER_USER_NAME, testBed.getUsername())
                                                          .withString(DefaultDriverOption.AUTH_PROVIDER_PASSWORD, testBed.getPassword())
                                                          .withDuration(DefaultDriverOption.REQUEST_TIMEOUT, Duration.ofSeconds(30))
                                                          .build();
        this.rxSession = RxSession.builder().withConfig(cassandraDriverConf).build();

        this.datacenters = createDc();

        this.dbConfig = createDbConfiguration();

        this.updatedDatacenters = updateDc();

    }

}
