/**
 * COPYRIGHT ERICSSON GMBH 2020
 * <p>
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 * <p>
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 * <p>
 * Created on: Oct 7, 2020
 * Author: evouioa
 */

package com.ericsson.esc.bsf.manager.validator;

import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.cm.validator.ValidationResult;
import com.ericsson.adpal.cm.validator.Validator;
import com.ericsson.sc.bsf.model.EricssonBsf;

import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * Validator implementation for the
 * {@link com.ericsson.sc.bsf.model.EricssonBsf} POJO Schema
 */
public class BsfValidator implements Validator<EricssonBsf>
{

    static final EnumSet<BsfRule> ruleSet = EnumSet.allOf(BsfRule.class);

    private static final Logger log = LoggerFactory.getLogger(BsfValidator.class);
    private final String schemaName;

    public BsfValidator(String schemaName)
    {
        this.schemaName = schemaName;
    }

    @Override
    public Single<ValidationResult> validate(EricssonBsf ericssonBsf)
    {
        return Flowable.fromIterable(ruleSet).flatMapSingle(scpRule -> scpRule.validateOn(ericssonBsf)).toList().map(list ->
        {
            var vr = new ValidationResult(list, this.schemaName);
            log.debug("Validation result is: {}. Validation body is: {}", vr.getResult(), vr.getBody());
            return vr;
        });
    }
}
