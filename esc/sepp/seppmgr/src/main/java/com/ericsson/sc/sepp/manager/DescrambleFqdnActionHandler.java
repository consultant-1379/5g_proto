/**
 * COPYRIGHT ERICSSON GMBH 2023
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jul 21, 2023
 *     Author: xzinale
 */

package com.ericsson.sc.sepp.manager;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.cm.actions.ActionHandler;
import com.ericsson.adpal.cm.actions.ActionInput;
import com.ericsson.adpal.cm.actions.ActionResult;
import com.ericsson.adpal.cm.actions.ActionResult.ErrorType;
import com.ericsson.sc.sepp.model.DescrambleFqdnInput;
import com.ericsson.sc.sepp.model.EricssonSeppSeppFunction;
import com.ericsson.utilities.common.Pair;
import com.ericsson.utilities.exceptions.BadConfigurationException;
import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.reactivex.Single;

/**
 * 
 */
public class DescrambleFqdnActionHandler implements ActionHandler
{
    private static final Logger log = LoggerFactory.getLogger(DescrambleFqdnActionHandler.class);
    private static final ObjectMapper om = Jackson.om();
    private static final ActionResult DESCRAMBLE_FQDN_FAILURE = ActionResult.error(ErrorType.INTERNAL_SERVER_ERROR, "Descrambling of the FQDN falied");

    // the format of scrambled FQDNs is: VENIDSCRAMBLEDFQDN, where: V: 1 character
    // for version and ENID: 4 character for encryption identifier
    private static final int VERSION_INDEX = 1;
    private static final int PREFIX_LENGTH = 5;

    private String scrambledFQDN;
    private EricssonSeppSeppFunction seppFunction;
    private String keyID;
    private String fqdnRegex = "^.+\\.5gc\\.mnc\\d{3}\\.mcc\\d{3}\\.3gppnetwork\\.org$";

    public Single<ActionResult> executeAction(Single<ActionInput> actionContext)
    {
        return actionContext.doOnSubscribe(disp -> log.info("Action descramble-fqdn triggered"))
                            .map(ActionInput::getInput)
                            .map(propertyInputInJson -> om.treeToValue(propertyInputInJson, DescrambleFqdnInput.class))
                            .map(this::retrieveKeyAndIv)
                            .map(this::deScrambleFqdnOutput)
                            .doOnError(e -> log.error("Exception during descrambling procedure was caught. Cause: {}", e.getMessage()))
                            .onErrorReturnItem(DESCRAMBLE_FQDN_FAILURE);
    }

    private Pair<Optional<String>, Optional<String>> retrieveKeyAndIv(DescrambleFqdnInput input)
    {
        if (input.getEricssonSeppScrambledFqdn() == null)
        {
            throw new BadConfigurationException("The scrambled FQDN must be provided as input for the FQDN descrambling command");
        }
        this.scrambledFQDN = input.getEricssonSeppScrambledFqdn();

        try
        {
            this.keyID = input.getEricssonSeppScrambledFqdn().substring(VERSION_INDEX, PREFIX_LENGTH);
        }
        catch (IndexOutOfBoundsException e)
        {
            throw new BadConfigurationException("No encryption identifier found in the provided FQDN. The syntax of the scrambled FQDN must be the following: 1 character for the version, currently 'A' followed by 4 capital characters for the encryption identifier followed by the scrambled FQDN");
        }

        if (input.getEricssonSeppDescramblingKey() != null && input.getEricssonSeppInitialVector() != null)
        {
            return Pair.of(Optional.ofNullable(input.getEricssonSeppDescramblingKey()), Optional.ofNullable(input.getEricssonSeppInitialVector()));
        }
        var descramblingKeyTable = seppFunction.getNfInstance()
                                               .get(0)
                                               .getFqdnScramblingTable()
                                               .stream()
                                               .filter(keyTable -> keyTable.getId().equals(this.keyID))
                                               .findFirst();

        if (descramblingKeyTable.isEmpty())
        {
            throw new BadConfigurationException("No key found using the retrieved encryption identifier");
        }

        var key = Optional.of(descramblingKeyTable.get().getKey());
        var initialVector = Optional.of(descramblingKeyTable.get().getInitialVector());
        return Pair.of(key, initialVector);
    }

    private ActionResult deScrambleFqdnOutput(Pair<Optional<String>, Optional<String>> pair)
    {
        final var actionOutputNode = om.createObjectNode();
        var fqdnScrambleApi = FqdnScramblingApi.getInstance();
        pair.getFirst().ifPresent(fqdnScrambleApi::setKeyId);
        pair.getSecond().ifPresent(fqdnScrambleApi::setInitialVector);
        fqdnScrambleApi.setKeyId(this.keyID);

        if (!this.scrambledFQDN.matches(fqdnRegex))
        {
            throw new BadConfigurationException("The scrambled fqdn provided is invalid");
        }
        var descrambledFqdn = fqdnScrambleApi.descramble(this.scrambledFQDN);
        descrambledFqdn.ifPresent(v -> actionOutputNode.put("ericsson-sepp:fqdn", v));
        actionOutputNode.put("ericsson-sepp:key-id", fqdnScrambleApi.getKeyId());

        return ActionResult.success(actionOutputNode);
    }

    public void setSeppFunction(EricssonSeppSeppFunction seppFunction)
    {
        this.seppFunction = seppFunction;

    }

}
