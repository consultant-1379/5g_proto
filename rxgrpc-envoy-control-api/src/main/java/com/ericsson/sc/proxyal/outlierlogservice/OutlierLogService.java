/**
 * COPYRIGHT ERICSSON GMBH 2021
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Mar 23, 2021
 *     Author: echaias
 */

package com.ericsson.sc.proxyal.outlierlogservice;

import io.reactivex.Flowable;
import io.reactivex.subjects.PublishSubject;
import io.scp.api.v1.OutlierLogEvent;
import io.scp.api.v1.OutlierLogEventResponse;

/**
 * 
 */
public interface OutlierLogService
{
    public PublishSubject<EnvoyStatus> getOutlierEventStream();

    public Flowable<OutlierLogEventResponse> streamOutlierLogEvents(Flowable<OutlierLogEvent> event);

}
