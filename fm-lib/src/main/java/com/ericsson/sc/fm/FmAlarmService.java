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
 * Created on: Jul 21, 2022
 *     Author: ekoteva
 */

package com.ericsson.sc.fm;

import com.ericsson.sc.fm.model.fi.FaultIndication;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.reactivex.Completable;

/**
 * 
 */
public interface FmAlarmService
{
    /**
     * Raise alarm with additional info
     * 
     * @param faultIndication
     * @return
     * @throws JsonProcessingException
     */
    Completable raise(FaultIndication faultIndication) throws JsonProcessingException;

    /**
     * Cease alarm
     * 
     * @param faultIndication
     * @return
     */
    Completable cease(FaultIndication faultIndication) throws JsonProcessingException;
}
