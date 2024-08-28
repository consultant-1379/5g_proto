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
 * Created on: May 3, 2023
 *     Author: eedstl
 */

package com.ericsson.sc.nrf.r17;

import com.ericsson.sc.common.alarm.AlarmHandler.Alarm;
import com.ericsson.sc.fm.model.fi.FaultIndication.Severity;

/**
 * Alarms of this type are never actively ceased, they will be ceased
 * automatically by the fault handler when they expire.
 */
public class NrfGroupUnavailableAlarmHandler
{
    private static final String NRF_GROUP_UNAVAILABLE = "NrfGroupUnavailable";
    private static final String NRF_GROUP_UNAVAILABLE_DESCR = "None of the NRFs in the group either can be contacted or indicate successful processing of requests";

    private final Alarm.Context alarmCtx;
    private final String faultyResource;
    private final String alarmName;

    public NrfGroupUnavailableAlarmHandler(final Alarm.Context alarmCtx,
                                           final String faultyResource)
    {
        this.alarmCtx = alarmCtx;
        this.faultyResource = faultyResource;
        this.alarmName = Alarm.toAlarmName(alarmCtx.getAlarmPrefix(), NRF_GROUP_UNAVAILABLE);
    }

    public void raise(final Throwable error)
    {
        this.alarmCtx.publish(Alarm.of(this.alarmName,
                                       this.alarmCtx.getServiceName(),
                                       this.faultyResource,
                                       Severity.MAJOR,
                                       Alarm.toDescription(NRF_GROUP_UNAVAILABLE_DESCR, error.getMessage()),
                                       0L, // Do not update, rely on external triggers.
                                       null));
    }
}
