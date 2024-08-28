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
 * Created on: Nov 20, 2020
 *     Author: eevagal
 */

package com.ericsson.esc.bsf.manager;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.cm.actions.ActionHandler;
import com.ericsson.adpal.cm.actions.ActionInput;
import com.ericsson.adpal.cm.actions.ActionResult;
import com.ericsson.adpal.cm.actions.ActionResult.ErrorType;
import com.ericsson.esc.bsf.db.DbConfiguration;
import com.ericsson.sc.bsf.model.EricssonBsfDatacenter;
import com.ericsson.sc.bsf.model.Input;
import com.ericsson.sc.fm.FmAlarmService;
import com.ericsson.sc.fm.model.fi.FaultIndication.FaultIndicationBuilder;
import com.ericsson.sc.fm.model.fi.FaultIndication.Severity;
import com.ericsson.utilities.cassandra.RxSession;
import com.ericsson.utilities.common.Rdn;
import com.ericsson.utilities.json.Jackson;

import io.reactivex.Single;

public class InitializeDbActionHandler implements ActionHandler
{
    private static final Logger log = LoggerFactory.getLogger(InitializeDbActionHandler.class);
    private static final String INSERTION_ALARM_NAME = "BsfUnsuccDbSchemaInsertion";

    private final DbConfiguration params;
    private final FmAlarmService alarmService;
    private final RxSession cassandraDbSession;
    private final BsfSchemaHandler dbSchemaHandler;
    private final BsfUserHandler dbUserHandler;
    private final String serviceName;

    private static final ActionResult SUCCESS_RESULT = ActionResult.success(Jackson.om()
                                                                                   .createObjectNode()
                                                                                   .put("ericsson-bsf:info", "Database initialized successfully"));
    private static final ActionResult SCHEMA_CREATION_FAILURE = ActionResult.error(ErrorType.INTERNAL_SERVER_ERROR, "Database schema creation failed");
    private static final ActionResult SCHEMA_ALTERATION_FAILURE = ActionResult.error(ErrorType.INTERNAL_SERVER_ERROR, "Database schema alteration failed");
    private static final ActionResult USER_CREATION_FAILURE = ActionResult.error(ErrorType.INTERNAL_SERVER_ERROR, "Database user creation failed");

    public InitializeDbActionHandler(String serviceName,
                                     RxSession cassandraDbSession,
                                     DbConfiguration params,
                                     FmAlarmService alarmService)
    {
        this.serviceName = serviceName;
        this.cassandraDbSession = cassandraDbSession;
        this.params = params;
        this.alarmService = alarmService;

        this.dbSchemaHandler = new BsfSchemaHandler(this.cassandraDbSession, this.params);
        this.dbUserHandler = new BsfUserHandler(this.cassandraDbSession, this.params.getKeyspace(), this.params.getUser(), this.params.getPassword());

    }

    @Override
    public Single<ActionResult> executeAction(Single<ActionInput> actionContext)
    {
        final Single<ActionResult> initializationResult = actionContext.doOnSubscribe(disp -> log.info("Action initialize-db triggered"))
                                                                       .map(ActionInput::getInput)
                                                                       .map(propertyInputInJson -> Jackson.om().treeToValue(propertyInputInJson, Input.class))
                                                                       .map(propertyInput -> getReplFactorSettings(propertyInput.getEricssonBsfDatacenter()))
                                                                       .flatMap(replicationFactorSettings ->
                                                                       {
                                                                           log.debug("replicationFactorSettings : {}", replicationFactorSettings);
                                                                           return initializeDb(replicationFactorSettings);
                                                                       });
        return initializationResult // Raise or Cease alarm according to action result
                                   .doOnSuccess(res ->
                                   {
                                       var fiBuilder = new FaultIndicationBuilder().withFaultName(INSERTION_ALARM_NAME) //
                                                                                   .withServiceName(this.serviceName) //
                                                                                   .withFaultyResource(new Rdn("nf", "bsf-function").toString(false));

                                       if (res == SUCCESS_RESULT)
                                       {
                                           fiBuilder.withDescription("Unsuccessful Cassandra Database schema insertion");
                                           alarmService.cease(fiBuilder.build()).subscribe(() ->
                                           {
                                           }, t -> log.error("Error ceasing alarm {}: ", INSERTION_ALARM_NAME, t));
                                       }
                                       else
                                       {
                                           fiBuilder.withSeverity(Severity.CRITICAL) //
                                                    .withDescription(res == USER_CREATION_FAILURE ? "Cassandra user creation failure"
                                                                                                  : "Cassandra BSF schema insertion failure") //
                                                    .withExpiration(0L);
                                           alarmService.raise(fiBuilder.build()).subscribe(() ->
                                           {
                                           }, t -> log.error("Error raising alarm {}: ", INSERTION_ALARM_NAME, t));
                                       }
                                   });

    }

    private static Map<String, Object> getReplFactorSettings(List<EricssonBsfDatacenter> datacenters)
    {
        HashMap<String, Object> replicationFactorSettings = new HashMap<>();
        replicationFactorSettings.put("class", "NetworkTopologyStrategy");
        datacenters //
                   .forEach(dc -> replicationFactorSettings.put(dc.getName(), dc.getReplicationFactor()));

        return Collections.unmodifiableMap(replicationFactorSettings);
    }

    private Single<ActionResult> initializeDb(Map<String, Object> replicationFactorSettings)
    {
        return dbSchemaHandler.createAndVerifySchema(replicationFactorSettings) //
                              .filter(Boolean.TRUE::equals)
                              .flatMapSingleElement(res -> dbSchemaHandler.alterSchema()
                                                                          .filter(Boolean.TRUE::equals)
                                                                          .flatMapSingleElement(r -> dbUserHandler.createBsfUser() //
                                                                                                                  .map(dbUserCreationResult -> (boolean) dbUserCreationResult ? SUCCESS_RESULT
                                                                                                                                                                              : USER_CREATION_FAILURE))
                                                                          .toSingle(SCHEMA_ALTERATION_FAILURE))
                              .toSingle(SCHEMA_CREATION_FAILURE);

    }

}
