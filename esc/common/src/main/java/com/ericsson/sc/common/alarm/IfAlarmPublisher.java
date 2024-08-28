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
 * Created on: Sep 14, 2023
 *     Author: eedstl
 */

package com.ericsson.sc.common.alarm;

import java.util.Collection;
import java.util.Map;

import com.ericsson.sc.common.alarm.AlarmHandler.Alarm;

public interface IfAlarmPublisher
{

    void publish(Alarm alarm);

    void publish(Map<String, ? extends Collection<Alarm>> alarms);

}