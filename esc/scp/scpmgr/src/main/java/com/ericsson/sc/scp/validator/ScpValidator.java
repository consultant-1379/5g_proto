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
 *     Author: echaias
 */

package com.ericsson.sc.scp.validator;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.validator.Rule;
import com.ericsson.adpal.cm.validator.ValidationResult;
import com.ericsson.adpal.cm.validator.Validator;
import com.ericsson.sc.scp.model.EricssonScp;

import io.kubernetes.client.openapi.models.V1Service;
import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * Validator implementation for the {@link EricssonScp} POJO Schema
 */
public class ScpValidator implements Validator<EricssonScp>
{

    private static final Logger log = LoggerFactory.getLogger(ScpValidator.class);
    private final String schemaName;
    private final List<Rule<EricssonScp>> rules;

    public ScpValidator(String schemaName,
                        final List<V1Service> k8sServiceList)
    {
        this.schemaName = schemaName;
        this.rules = new ScpRules(k8sServiceList).getRules();
    }

    @Override
    public Single<ValidationResult> validate(EricssonScp ericssonScp)
    {
        return Flowable.fromIterable(rules)
                       .flatMapSingle(scpRule -> scpRule.apply(ericssonScp))
                       .doOnError(e -> log.error("Error occured while validating", e))
                       .toList()
                       .map(list ->
                       {
                           ValidationResult vr = new ValidationResult(list, this.schemaName);
                           log.debug("Validation result is: {}. Validation body is: {}", vr.getResult(), vr.getBody());
                           return vr;
                       });
    }
}
