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
 * Created on: May 1, 2020
 *     Author: echfari
 */
package com.ericsson.sc.diameter;

import com.ericsson.gs.tm.diameter.service.grpc.IncomingRequest;
import com.ericsson.gs.tm.diameter.service.grpc.IncomingRequestResult;
import com.ericsson.sc.diameter.DiaGrpcClient.Context;

import io.reactivex.Observable;
import io.reactivex.Single;

public interface DiaGrpcHandler
{
    default Single<IncomingRequestResult> processRequest(Single<IncomingRequest> request,
                                                         Observable<DiaGrpcClientContext> context)
    {
        throw new io.grpc.StatusRuntimeException(io.grpc.Status.UNIMPLEMENTED);
    }

}
