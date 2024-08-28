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

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.cm.actions.ActionHandler;
import com.ericsson.adpal.cm.actions.ActionInput;
import com.ericsson.adpal.cm.actions.ActionResult;
import com.ericsson.adpal.cm.actions.ActionResult.ErrorType;
import com.ericsson.sc.sepp.model.EricssonSeppSeppFunction;
import com.ericsson.sc.sepp.model.ScrambleFqdnInput;
import com.ericsson.utilities.common.Pair;
import com.ericsson.utilities.exceptions.BadConfigurationException;
import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.reactivex.Single;

/**
 * 
 */
public class ScrambleFqdnActionHandler implements ActionHandler
{
    private static final Logger log = LoggerFactory.getLogger(ScrambleFqdnActionHandler.class);
    private static final ObjectMapper om = Jackson.om();
    private static final ActionResult SCRAMBLE_FQDN_FAILURE = ActionResult.error(ErrorType.INTERNAL_SERVER_ERROR, "Scrambling of the FQDN failed");
    private EricssonSeppSeppFunction seppFunction;
    private String fqdn;
    private String keyID;
    private String fqdnRegex = "^.+\\.5gc\\.mnc\\d{3}\\.mcc\\d{3}\\.3gppnetwork\\.org$";

    public Single<ActionResult> executeAction(Single<ActionInput> actionContext)
    {

        return actionContext.doOnSubscribe(disp -> log.info("Action scramble-fqdn triggered"))
                            .map(ActionInput::getInput)
                            .map(propertyInputInJson -> om.treeToValue(propertyInputInJson, ScrambleFqdnInput.class))
                            .map(this::retrieveKeyAndIv)
                            .map(this::scrambleFqdnOutput)
                            .doOnError(e -> log.error("Exception during scrambling procedure was caught. Cause: {}", e.getMessage()))
                            .onErrorReturnItem(SCRAMBLE_FQDN_FAILURE);

    }

    private Pair<Optional<String>, Optional<String>> retrieveKeyAndIv(ScrambleFqdnInput input)
    {
        if (input.getEricssonSeppFqdn() == null)
        {
            throw new BadConfigurationException("The FQDN must be provided as input for the FQDN scrambling command");
        }

        this.fqdn = input.getEricssonSeppFqdn();

        if (input.getEricssonSeppKeyId() != null)
        {
            return retrieveKeyAndIvIdRef(input);
        }

        if (input.getEricssonSeppRoamingPartnerRef() != null)
        {
            return retrieveKeyAndIvRpRef(input);
        }

        if (input.getEricssonSeppKeyPlainText().getId() == null)
        {
            throw new BadConfigurationException("The encryption identifier must be provided as input for the FQDN scrambling command in case of plain text");
        }
        this.keyID = input.getEricssonSeppKeyPlainText().getId();
        var encKey = Optional.ofNullable(input.getEricssonSeppKeyPlainText().getKey());
        var iv = Optional.ofNullable(input.getEricssonSeppKeyPlainText().getInitialVector());
        return Pair.of(encKey, iv);
    }

    private Pair<Optional<String>, Optional<String>> retrieveKeyAndIvIdRef(ScrambleFqdnInput input)
    {

        var tableEntry = seppFunction.getNfInstance()
                                     .get(0)
                                     .getFqdnScramblingTable()
                                     .stream()
                                     .filter(keyTable -> keyTable.getId().equals(input.getEricssonSeppKeyId()))
                                     .findAny();
        if (tableEntry.isEmpty())
        {
            throw new BadConfigurationException("No key found using the given ID");
        }

        if (input.getEricssonSeppKeyId() == null)
        {
            throw new BadConfigurationException("The encryption identifier must be provided as input for the FQDN scrambling command in case of key ID");
        }
        this.keyID = tableEntry.get().getId();
        var key = Optional.of(tableEntry.get().getKey());
        var initialVector = Optional.of(tableEntry.get().getInitialVector());
        return Pair.of(key, initialVector);
    }

    private Pair<Optional<String>, Optional<String>> retrieveKeyAndIvRpRef(ScrambleFqdnInput input)
    {

        var tphAdminStateForRpRef = seppFunction.getNfInstance()
                                                .get(0)
                                                .getExternalNetwork()
                                                .stream()
                                                .filter(extNw -> extNw.getRoamingPartner()
                                                                      .stream()
                                                                      .anyMatch(rp -> rp.getName().equals(input.getEricssonSeppRoamingPartnerRef())))
                                                .flatMap(extNw -> extNw.getRoamingPartner()
                                                                       .stream()
                                                                       .filter(rp -> rp.getName().equals(input.getEricssonSeppRoamingPartnerRef()))
                                                                       .flatMap(rp ->
                                                                       {
                                                                           if (rp.getTopologyHidingWithAdminState() == null
                                                                               || rp.getTopologyHidingWithAdminState().isEmpty())
                                                                           {
                                                                               return extNw.getTopologyHidingWithAdminState().stream();
                                                                           }
                                                                           return rp.getTopologyHidingWithAdminState().stream();
                                                                       }))
                                                .collect(Collectors.toSet());

        if (tphAdminStateForRpRef == null || tphAdminStateForRpRef.isEmpty())
        {
            throw new BadConfigurationException("No Scrambling Keys are provided for the referenced roaming partner");
        }

        AtomicReference<Optional<String>> activeId = new AtomicReference<>();
        activeId.set(Optional.empty());
        AtomicReference<Date> activeDate = new AtomicReference<>(new Date(0L));
        var now = new Date();

        tphAdminStateForRpRef.forEach(tphAs -> tphAs.getScramblingKey().stream().forEach(scramblingKey ->
        {
            if (scramblingKey.getActivationDate().compareTo(now) <= 0 && scramblingKey.getActivationDate().compareTo(activeDate.get()) > 0)
            {
                activeDate.set(scramblingKey.getActivationDate());
                activeId.set(Optional.ofNullable(scramblingKey.getKeyIdRef()));
            }
        }));

        this.keyID = activeId.get()
                             .orElseThrow(() -> new BadConfigurationException("No active key found for the roaming partner: {} ",
                                                                              input.getEricssonSeppRoamingPartnerRef()));

        return seppFunction.getNfInstance()
                           .get(0)
                           .getFqdnScramblingTable()
                           .stream()
                           .filter(keyTable -> keyTable.getId().equals(activeId.get().get()))
                           .map(keyTable -> Pair.of(Optional.of(keyTable.getKey()), Optional.of(keyTable.getInitialVector())))
                           .findAny()
                           .orElseThrow();
    }

    private ActionResult scrambleFqdnOutput(Pair<Optional<String>, Optional<String>> pair)
    {
        final var actionOutputNode = om.createObjectNode();

        var fqdnScrambleApi = FqdnScramblingApi.getInstance();

        pair.getFirst().ifPresent(fqdnScrambleApi::setSymmetricKey);
        pair.getSecond().ifPresent(fqdnScrambleApi::setInitialVector);
        fqdnScrambleApi.setKeyId(this.keyID);

        if (!fqdn.matches(fqdnRegex))
        {
            throw new BadConfigurationException("The fqdn provided is invalid");
        }
        var scrambledFqdn = fqdnScrambleApi.scramble(fqdn);
        log.debug("Scrambled FQDN : {}", scrambledFqdn);
        scrambledFqdn.ifPresent(v -> actionOutputNode.put("ericsson-sepp:scrambled-fqdn", v));
        actionOutputNode.put("ericsson-sepp:key-id", this.keyID);

        return ActionResult.success(actionOutputNode);
    }

    public void setSeppFunction(EricssonSeppSeppFunction seppFunction)
    {
        this.seppFunction = seppFunction;

    }

}
