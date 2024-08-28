/**
 * COPYRIGHT ERICSSON GMBH 2024
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Apr 9, 2024
 *     Author: ztsakon
 */

package com.ericsson.sc.proxyal.healtchecklogservice;

import io.reactivex.Flowable;
import io.reactivex.subjects.PublishSubject;
import io.scp.api.v1.HealthCheckLogEvent;
import io.scp.api.v1.HealthCheckLogEventResponse;

/**
 * 
 */
public interface HealthCheckLogService
{
    public PublishSubject<EnvoyStatus> getHealthCheckEventStream();

    public Flowable<HealthCheckLogEventResponse> streamHealthCheckLogEvents(Flowable<HealthCheckLogEvent> event);
}
