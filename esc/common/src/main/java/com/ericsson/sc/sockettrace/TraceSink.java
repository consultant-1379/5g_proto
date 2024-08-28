/**
 * COPYRIGHT ERICSSON GMBH 2019
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Oct 21, 2022
 *     Author: echfari
 */
package com.ericsson.sc.sockettrace;

import io.envoyproxy.envoy.data.tap.v3.SocketStreamedTraceSegment;
import io.reactivex.Completable;
import io.reactivex.Flowable;

public interface TraceSink
{
    Completable consumeTrace(Flowable<SocketStreamedTraceSegment> traceFlow);
}
