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
 * Created on: Dec 22, 2020
 *     Author: eevagal
 */

package com.ericsson.esc.bsf.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.cm.actions.ActionHandler;
import com.ericsson.adpal.cm.actions.ActionInput;
import com.ericsson.adpal.cm.actions.ActionResult;
import com.ericsson.adpal.cm.actions.ActionResult.ErrorType;
import com.ericsson.esc.bsf.db.DbConfiguration;
import com.ericsson.sc.bsf.model.EricssonBsfStatus;
import com.ericsson.sc.bsf.model.EricssonBsfTopology;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ericsson.utilities.cassandra.RxSession;
import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.reactivex.Single;

/**
 * 
 */
public class CheckDbSchemaActionHandler implements ActionHandler
{
    private static final Logger log = LoggerFactory.getLogger(CheckDbSchemaActionHandler.class);

    private static final ObjectMapper om = Jackson.om();
    private final DbConfiguration params;
    private final RxSession cassandraDbSession;
    private final BsfSchemaHandler dbSchemaHandler;
    private final BsfUserHandler dbUserHandler;

    private static final ActionResult SCHEMA_CHECK_FAILURE = ActionResult.error(ErrorType.INTERNAL_SERVER_ERROR, "Database schema check failed");

    public CheckDbSchemaActionHandler(RxSession cassandraDbSession,
                                      DbConfiguration params)
    {
        this.cassandraDbSession = cassandraDbSession;
        this.params = params;

        this.dbSchemaHandler = new BsfSchemaHandler(this.cassandraDbSession, this.params);
        this.dbUserHandler = new BsfUserHandler(this.cassandraDbSession, this.params.getKeyspace(), this.params.getUser(), this.params.getPassword());
    }

    @Override
    public Single<ActionResult> executeAction(Single<ActionInput> actionContext)
    {
        return actionContext.doOnSubscribe(disp -> log.info("Action check-db-schema triggered")).ignoreElement().andThen(checkDbSchema());
    }

    private Single<ActionResult> checkDbSchema()
    {
        return Single.zip(dbUserHandler.verifyBsfUserModifyPermissions(),
                          dbSchemaHandler.getSchemaStatus(),
                          dbSchemaHandler.getSchemaTopology(),
                          BsfSchemaHandler.verifySchemaAgreement(cassandraDbSession).toSingleDefault(true).onErrorReturnItem(false),
                          this::generateCheckDbActionOutput)
                     .onErrorReturnItem(SCHEMA_CHECK_FAILURE);
    }

    private ActionResult generateCheckDbActionOutput(Boolean dbUserStatus,
                                                     EricssonBsfStatus dbSchemaStatus,
                                                     EricssonBsfTopology dbSchemaTopology,
                                                     boolean schemaAgreement)
    {
        ObjectNode actionOutputNode = om.createObjectNode();

        if (Boolean.FALSE.equals(dbUserStatus))
        {
            dbSchemaStatus.setReady(false);
            dbSchemaStatus.setInfo("Database User does not exist or does not have the required permissions");
        }
        actionOutputNode.putPOJO("ericsson-bsf:status", dbSchemaStatus);

        if (!dbSchemaTopology.getDatacenter().isEmpty())
        {
            actionOutputNode.putPOJO("ericsson-bsf:topology", dbSchemaTopology);
        }

        if (!schemaAgreement)
        {
            dbSchemaStatus.setReady(false);
            dbSchemaStatus.setInfo("All Database Nodes that are currently up do not agree on the schema definition");
        }

        JsonNode result = om.createObjectNode().setAll(actionOutputNode);

        return ActionResult.success(result);
    }

}
