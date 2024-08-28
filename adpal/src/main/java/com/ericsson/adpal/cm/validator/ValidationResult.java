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
 * Created on: Oct 7, 2020
 *     Author: evouioa
 */

package com.ericsson.adpal.cm.validator;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Holds the result of the validation process of the combined applied instances
 * of {@link com.ericsson.adpal.cm.model.Rule}. It is aligned with the CM
 * Dynamic Validation API
 */
public class ValidationResult
{
    private JsonObject bodyResponse;
    private boolean result;

    private static final Logger log = LoggerFactory.getLogger(ValidationResult.class);

    public ValidationResult(List<RuleResult> ruleResults,
                            String schemaName)
    {
        this.result = ruleResults.stream().allMatch(RuleResult::getResult); // true if no validation results are returned
        this.bodyResponse = new JsonObject();
        createJsonBody(ruleResults, schemaName);
    }

    public boolean getResult()
    {
        return this.result;
    }

    private void createJsonBody(List<RuleResult> ruleResults,
                                String schemaName)
    {
        var errorsArray = new JsonArray();
        log.debug("RuleResult list size: {}", ruleResults.size());
        ruleResults.stream().filter(ruleResult -> !ruleResult.getResult()).forEach(failedResult ->
        {
            var error = new JsonObject();
            error.put("jsonPointer", schemaName);
            error.put("errorText", failedResult.getErrorMessage());
            errorsArray.add(error);
            this.bodyResponse.put("validationPassed", failedResult.getResult());
        });
        this.bodyResponse.put("errors", errorsArray);
    }

    public String getBody()
    {
        return this.bodyResponse.toString();
    }

}
