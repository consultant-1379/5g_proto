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
 * Created on: Jan 22, 2021
 *     Author: eiiarlf
 */

package com.ericsson.esc.bsf.manager;

import java.util.List;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.cm.actions.ActionHandler;
import com.ericsson.adpal.cm.actions.ActionInput;
import com.ericsson.adpal.cm.actions.ActionResult;
import com.ericsson.adpal.cm.actions.ActionResult.ErrorType;
import com.ericsson.esc.bsf.db.DbConfiguration;
import com.ericsson.sc.bsf.model.EricssonBsfDatacenter;
import com.ericsson.sc.bsf.model.Input;
import com.ericsson.utilities.cassandra.RxSession;
import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.reactivex.Single;

/**
 * The UpdateDbTopologyActionHandler class implements a handler for the
 * update-db-topology YANG action under
 * ericsson-bsf:bsf-function::nf-instance::bsf-service::binding-database::update-db-topology
 * path.
 */
public class UpdateDbTopologyActionHandler implements ActionHandler
{
    private static final Logger log = LoggerFactory.getLogger(UpdateDbTopologyActionHandler.class);
    private static final ObjectMapper om = Jackson.om();
    private static final ActionResult SUCCESS_RESULT = ActionResult.success(om.createObjectNode().put("ericsson-bsf:info", "Database updated successfully"));
    private static final ActionResult SCHEMA_UPDATE_FAILURE = ActionResult.error(ErrorType.INTERNAL_SERVER_ERROR, "Database schema update failed");

    private final BsfSchemaHandler dbSchemaHandler;

    /**
     * Creates an UpdateDbTopologyActionHandler with specific cassandra driver
     * wrapper and bsf manager parameters.
     * 
     * @param cassandraDbSession The RxJava cassandra driver instance.
     * @param params             The parameters of the database configuration.
     */
    public UpdateDbTopologyActionHandler(RxSession cassandraDbSession,
                                         DbConfiguration params)
    {
        this.dbSchemaHandler = new BsfSchemaHandler(cassandraDbSession, params);
    }

    /**
     * Executes the update-db-topology YANG action.
     * 
     * @param actionContext The input and context of update-db-topology action.
     * @return The result of the update-db YANG action.
     */
    @Override
    public Single<ActionResult> executeAction(Single<ActionInput> actionContext)
    {
        return actionContext.doOnSubscribe(disp -> log.info("Action update-db-topology triggered"))
                            .map(ActionInput::getInput)
                            .map(propertyInputInJson -> om.treeToValue(propertyInputInJson, Input.class))
                            .map(propertyInput -> getReplFactorSettings(propertyInput.getEricssonBsfDatacenter()))
                            .flatMap(replicationFactorSettings ->
                            {
                                log.debug("replicationFactorSettings : {}", replicationFactorSettings);
                                return updateDb(replicationFactorSettings);
                            });
    }

    private static String getReplFactorSettings(List<EricssonBsfDatacenter> datacenters)
    {
        var datacentersReplicationFactors = new StringJoiner(", ");
        datacenters.forEach(datacenter -> datacentersReplicationFactors.add(String.join(":",
                                                                                        "'" + datacenter.getName() + "'",
                                                                                        String.valueOf(datacenter.getReplicationFactor()))));

        return datacentersReplicationFactors.toString();
    }

    private Single<ActionResult> updateDb(String replicationFactorSettings)
    {
        return dbSchemaHandler.updateReplication(replicationFactorSettings)
                              .map(dbUpdateResult -> Boolean.TRUE.equals(dbUpdateResult) ? SUCCESS_RESULT : SCHEMA_UPDATE_FAILURE);
    }
}
