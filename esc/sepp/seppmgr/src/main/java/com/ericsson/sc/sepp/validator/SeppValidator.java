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

package com.ericsson.sc.sepp.validator;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.cm.validator.ValidationResult;
import com.ericsson.adpal.cm.validator.Validator;
import com.ericsson.sc.sepp.model.EricssonSepp;
import com.ericsson.sc.validator.Rule;

import io.kubernetes.client.openapi.models.V1Service;
import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * Validator implementation for the {@link EricssonSepp} POJO Schema
 */
public class SeppValidator implements Validator<EricssonSepp>
{

    private static final Logger log = LoggerFactory.getLogger(SeppValidator.class);
    private final String schemaName;
    private final List<Rule<EricssonSepp>> rules;

    public SeppValidator(String schemaName,
                         final List<V1Service> k8sServiceList)
    {
        this.schemaName = schemaName;
        this.rules = new SeppRules(k8sServiceList).getRules();

    }

    @Override
    public Single<ValidationResult> validate(EricssonSepp ericssonSepp)
    {
        return Flowable.fromIterable(rules)
                       .flatMapSingle(seppRule -> seppRule.apply(ericssonSepp))
                       .doOnError(e -> log.error("Error occured while validating", e))
                       .toList()
                       .map(list ->
                       {
                           var vr = new ValidationResult(list, this.schemaName);
                           log.debug("Validation result is: {}. Validation body is: {}", vr.getResult(), vr.getBody());
                           return vr;
                       });
    }
}
