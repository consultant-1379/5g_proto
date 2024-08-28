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

package com.ericsson.adpal.cm.validator;

import io.reactivex.Single;

/**
 * Validator interface that offers the {@link #validate(T)} method. Implementors
 * have to override the {@link #validate(T)} method with the desired business
 * logic for each Network Function
 */
public interface Validator<T>
{
    /**
     * Validates the configuration against the implemented Rules of the given
     * {@link T} Network Function POJO Schema
     */
    Single<ValidationResult> validate(T nf);
}
