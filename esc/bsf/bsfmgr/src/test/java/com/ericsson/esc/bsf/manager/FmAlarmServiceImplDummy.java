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

package com.ericsson.esc.bsf.manager;

import com.ericsson.sc.fm.FmAlarmHandler;
import com.ericsson.sc.fm.FmAlarmService;
import com.ericsson.sc.fm.model.fi.FaultIndication;
import com.ericsson.sc.fm.model.fi.FaultIndication.FaultIndicationBuilder;
import com.ericsson.sc.fm.model.fi.FaultIndication.Severity;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.reactivex.Completable;

/**
 * Actions needed for the raise/cease of alarms towards Alarm Handler service
 */
public class FmAlarmServiceImplDummy implements FmAlarmService
{
    private static final Long CEASE_EXPIRATION = 0L;
    private final FmAlarmHandler alarmHandler;

    public FmAlarmServiceImplDummy(FmAlarmHandler alarmHandler)
    {
        this.alarmHandler = alarmHandler;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.sc.fm.alarm.Actions#raise(com.ericsson.sc.fm.model.
     * FaultIndication)
     */
    @Override
    public Completable raise(FaultIndication faultIndication) throws JsonProcessingException
    {
        return this.update(new FaultIndicationBuilder(faultIndication).build());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.sc.fm.alarm.Actions#cease(com.ericsson.sc.fm.model.
     * FaultIndication)
     */
    @Override
    public Completable cease(FaultIndication faultIndication) throws JsonProcessingException
    {
        return this.update(new FaultIndicationBuilder(faultIndication).withSeverity(Severity.CLEAR) //
                                                                      .withExpiration(CEASE_EXPIRATION) //
                                                                      .build());
    }

    private Completable update(FaultIndication faultIndication) throws JsonProcessingException
    {
        return Completable.complete();
    }

}
