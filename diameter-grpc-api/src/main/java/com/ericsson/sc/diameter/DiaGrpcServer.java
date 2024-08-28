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

import com.ericsson.gs.tm.diameter.service.grpc.RxDiameterClientGrpc.DiameterClientImplBase;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public final class DiaGrpcServer
{

    final Server server;

    private DiaGrpcServer(Server server)
    {
        this.server = server;
    }

    public static <T extends ServerBuilder<T>> Single<DiaGrpcServer> start(ServerBuilder<T> serverBuilder,
                                                                           DiameterClientImplBase callback)
    {
        return Single.defer(() ->
        {
            final var server = serverBuilder.addService(callback).build();
            return start(server);
        });
    }

    public Completable shutdown()
    {
        return Completable.fromAction(() -> this.server.shutdown().awaitTermination()).subscribeOn(Schedulers.io());
    }

    private static Single<DiaGrpcServer> start(Server server)
    {
        return Single.defer(() ->
        {
            final var ds = new DiaGrpcServer(server);
            // Probably a blocking call
            return Completable.fromAction(server::start).subscribeOn(Schedulers.io()).toSingleDefault(ds);
        });
    }

}
