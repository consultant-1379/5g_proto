package com.ericsson.sc.diameter;

import com.ericsson.gs.tm.diameter.service.grpc.OutgoingRequest;
import com.ericsson.gs.tm.diameter.service.grpc.OutgoingRequestResult;

import io.reactivex.Single;

public interface DiaGrpcClientContext
{

    long getServiceId();

    PeerTable getPeerTable();

    boolean isInitialSyncDone();

    Single<OutgoingRequestResult> sendRequest(OutgoingRequest rxRequest);

}