/**
 * COPYRIGHT ERICSSON GMBH 2021
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Mar 15, 2021
 *     Author: eevagal
 */

package com.ericsson.esc.bsf.manager;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.internal.core.auth.PlainTextAuthProvider;
import com.ericsson.adpal.pm.PmAdapter;
import com.ericsson.esc.bsf.db.DbConfiguration;
import com.ericsson.esc.bsf.db.DbConfiguration.SizeTieredCompactionStrategy;
import com.ericsson.utilities.cassandra.CassandraTestServer;
import com.ericsson.utilities.cassandra.RxSession;
import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.reactivex.Observable;

public class DbPermissionsControllerTest
{
    private static final Logger log = LoggerFactory.getLogger(DbPermissionsControllerTest.class);
    private static final String KEYSPACE = "nbsf_management_keyspace";
    private static final String LOCAL_DC_NAME = "datacenter1";
    private static final String STORAGE_UTILIZATION_LOW = "40.0";
    private static final String STORAGE_UTILIZATION_HIGH = "86.5";
    private static final Integer STORAGE_MAX_PERCENTAGE = 85;
    private static final Map<String, Object> replicationFactorSettings = Map.of("class", "NetworkTopologyStrategy", "datacenter1", 2);
    // "'datacenter1':2";
    private static ObjectMapper om = Jackson.om();
    private final CassandraTestServer testBed = new CassandraTestServer();
    private RxSession rxSession;
    private DbConfiguration dbConfig;

    @Test(groups = "functest")
    public void testDbPermissionsLowStorageUtilization() throws JsonProcessingException
    {

        var dbStorUtilController = new DbPermissionsController(getDbStorageUtilizationData(STORAGE_UTILIZATION_LOW), rxSession, dbConfig);

        dbStorUtilController.controlDbModifyPermissions().blockingAwait();

        final var verifyDbModifyPermissions = new BsfUserHandler(rxSession,
                                                                 dbConfig.getKeyspace(),
                                                                 dbConfig.getUser(),
                                                                 dbConfig.getPassword()).verifyBsfUserModifyPermissions().blockingGet();

        assertTrue(verifyDbModifyPermissions);
    }

    @Test(groups = "functest")
    public void testDbPermissionsHighStorageUtilization() throws JsonProcessingException
    {

        var dbStorUtilController = new DbPermissionsController(getDbStorageUtilizationData(STORAGE_UTILIZATION_HIGH), rxSession, dbConfig);

        dbStorUtilController.controlDbModifyPermissions().blockingAwait();

        final var verifyDbModifyPermissions = new BsfUserHandler(rxSession,
                                                                 dbConfig.getKeyspace(),
                                                                 dbConfig.getUser(),
                                                                 dbConfig.getPassword()).verifyBsfUserModifyPermissions().blockingGet();

        assertFalse(verifyDbModifyPermissions);
    }

    /**
     * Creates PM Data in JSON format using Jackson ObjectMapper as they are
     * received in PmAdapter (by Prometheus) Example:
     * data=[{\"resultType\":\"VECTOR\",\"result\":[{\"metric\":{},\"value\":[1.61,35.78]}]}]"}
     * The only crucial attribute for our algorithm is the 2nd element of "value"
     * array
     * 
     * @param currentStorageUtilizationValue
     * @return An observable with PM Data that include current DB Storage
     *         Utilization
     * @throws JsonProcessingException
     */
    private Observable<List<PmAdapter.Query.Response.Data>> getDbStorageUtilizationData(final String currentStorageUtilizationValue) throws JsonProcessingException
    {
        ObjectNode pmDataNode = om.createObjectNode();

        pmDataNode.put("resultType", "vector");

        final String resultData = String.format("{\"metric\":{},\"value\":[0.0, %s]}", currentStorageUtilizationValue);
        final JsonNode resultDataNode = Jackson.om().readTree(resultData);
        pmDataNode.putArray("result").add(resultDataNode);
        String pmDataJsonString = om.writerWithDefaultPrettyPrinter().writeValueAsString(pmDataNode);

        JsonNode pmDataJsonNode = om.readTree(pmDataJsonString);

        PmAdapter.Query.Response.Data pmData = om.treeToValue(pmDataJsonNode, PmAdapter.Query.Response.Data.class);

        log.info("PmData: " + pmData);

        List<PmAdapter.Query.Response.Data> dataList = new ArrayList<>();
        dataList.add(pmData);

        Observable<List<PmAdapter.Query.Response.Data>> dbStorageUtilizationData = Observable.create(emitter ->
        {
            emitter.onNext(dataList);
            emitter.onComplete();
        });

        return dbStorageUtilizationData;
    }

    private DbConfiguration createDbConfiguration()
    {
        final var dbConfig = new DbConfiguration(KEYSPACE, List.of(testBed.getContactPoint()), "datacenter1");

        dbConfig.setUserCredentials(testBed.getUsername(), testBed.getPassword());
        dbConfig.setConsistency("ONE");
        dbConfig.setUserCredentials("bsfUser", "bsfPassword");
        dbConfig.setGcGrace(864000);
        dbConfig.setStorageMaxPercentage(STORAGE_MAX_PERCENTAGE);
        dbConfig.setCompactionStrategy(new SizeTieredCompactionStrategy(2, 0.5, 32, 3)); // default compaction

        return dbConfig;
    }

    @BeforeClass
    public void beforeClass()
    {
        log.info("Initializing test environment ");
        testBed.startCassandra();
        dbConfig = createDbConfiguration();

        final var cassandraDriverConf = DriverConfigLoader.programmaticBuilder() //
                                                          .withStringList(DefaultDriverOption.CONTACT_POINTS, Arrays.asList(testBed.getContactPoint()))
                                                          .withBoolean(DefaultDriverOption.RECONNECT_ON_INIT, true)
                                                          .withString(DefaultDriverOption.LOAD_BALANCING_LOCAL_DATACENTER, LOCAL_DC_NAME)
                                                          .withString(DefaultDriverOption.REQUEST_CONSISTENCY, dbConfig.getCL())
                                                          .withClass(DefaultDriverOption.AUTH_PROVIDER_CLASS, PlainTextAuthProvider.class)
                                                          .withString(DefaultDriverOption.AUTH_PROVIDER_USER_NAME, testBed.getUsername())
                                                          .withString(DefaultDriverOption.AUTH_PROVIDER_PASSWORD, testBed.getPassword())
                                                          .withDuration(DefaultDriverOption.REQUEST_TIMEOUT, Duration.ofSeconds(30))
                                                          .build();
        rxSession = RxSession.builder().withConfig(cassandraDriverConf).build();

        final var successSchemaCreation = new BsfSchemaHandler(rxSession, dbConfig).createAndVerifySchema(replicationFactorSettings).blockingGet();
        assertTrue(successSchemaCreation);
        log.info("Database schema created successfully");
    }

    @AfterClass
    public void afterClass()
    {
        log.info("Closing testBed session");
        rxSession.close().blockingAwait();
        assertTrue(rxSession.sessionHolder() //
                            .map(RxSession.SessionHolder::isClosed)
                            .blockingGet());
        testBed.stopCassandra();
    }

    @BeforeMethod
    public void beforeMethod()
    {
        final var successDbUserCreation = new BsfUserHandler(rxSession, dbConfig.getKeyspace(), dbConfig.getUser(), dbConfig.getPassword()).createBsfUser()
                                                                                                                                           .blockingGet();
        assertTrue(successDbUserCreation);
        log.info("DB User created and permissions assigned (or re-assigned) successfully");
    }
}
