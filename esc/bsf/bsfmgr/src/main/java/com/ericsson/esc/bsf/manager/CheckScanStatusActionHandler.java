/**
 * COPYRIGHT ERICSSON GMBH 2022
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Sep 1, 2022
 *     Author: eiiarlf
 */

package com.ericsson.esc.bsf.manager;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.cm.actions.ActionHandler;
import com.ericsson.adpal.cm.actions.ActionInput;
import com.ericsson.adpal.cm.actions.ActionResult;
import com.ericsson.adpal.cm.actions.ActionResult.ErrorType;
import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.reactivex.Single;

/**
 * 
 */
public class CheckScanStatusActionHandler implements ActionHandler
{
    private static final Logger log = LoggerFactory.getLogger(CheckScanStatusActionHandler.class);
    private static final ObjectMapper om = Jackson.om();

    private static final ActionResult SCAN_CHECK_FAILURE = ActionResult.error(ErrorType.INTERNAL_SERVER_ERROR, "Check scan status action failed");

    private final Optional<FullTableScanManager> scanManager;

    public CheckScanStatusActionHandler(Optional<FullTableScanManager> fullTableScanManager)
    {
        this.scanManager = fullTableScanManager;
    }

    /**
     * Executes check-db-scan-status action
     */
    @Override
    public Single<ActionResult> executeAction(Single<ActionInput> actionContext)
    {
        return actionContext.doOnSubscribe(disp -> log.info("Action check-scan-status triggered"))//
                            .map(input -> this.generateCheckScanStatusOutput())
                            .onErrorReturnItem(SCAN_CHECK_FAILURE);
    }

    private ActionResult generateCheckScanStatusOutput()
    {
        if (this.scanManager.isEmpty())
        {
            log.warn("Scan manager has not been initialized");
            return ActionResult.error(ErrorType.SERVICE_UNAVAILABLE, "Scanning has not been initialized");
        }
        final var currentScanState = this.scanManager.get().getCurrentScanState();

        final var lastScanState = this.scanManager.get().getLastScanState();

        final var actionOutputNode = om.createObjectNode();

        actionOutputNode.putPOJO("ericsson-bsf:current-scan", currentScanState);

        if (lastScanState.isPresent())
        {
            actionOutputNode.putPOJO("ericsson-bsf:last-scan", lastScanState.get());
        }
        final var result = om.createObjectNode().setAll(actionOutputNode);

        return ActionResult.success(result);
    }

}
