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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.envoyproxy.envoy.data.tap.v3.SocketStreamedTraceSegment;
import io.envoyproxy.envoy.data.tap.v3.TraceWrapper;
import io.reactivex.BackpressureOverflowStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.net.NetClient;
import io.vertx.reactivex.core.net.NetSocket;

public class TapcolTraceSink implements TraceSink
{
    private static final Logger log = LoggerFactory.getLogger(TapcolTraceSink.class);

    private enum State
    {
        IDLE,
        CONNECTING,
        CONNECTED
    }

    AtomicReference<State> state = new AtomicReference<>(State.IDLE);
    AtomicReference<Single<NetSocket>> tcpSocket = new AtomicReference<>();

    final NetClient tcpClient;
    final int port;
    final String host;

    public TapcolTraceSink(Vertx vertx,
                           String host,
                           int port)
    {
        this.port = port;
        this.host = host;
        tcpClient = vertx.createNetClient();
    }

    @Override
    public Completable consumeTrace(Flowable<SocketStreamedTraceSegment> traceFlow)
    {
        return traceFlow.map(TapcolTraceSink::toBuffer)
                        .concatMapCompletable(buff -> send(buff) //
                                                                .doOnError(err -> log.warn("Send error, reconnecting", err))
                                                                .doOnError(err -> reconnect()))
                        .doOnError(err -> log.error("Stop sending traces", err));
    }

    private Single<NetSocket> createNewConnection()
    {
        final var newConnection = tcpClient //
                                           .rxConnect(port, host)
                                           .doOnSuccess(s -> this.state.set(State.CONNECTED))
                                           .cache();
        newConnection.subscribe();
        return newConnection;
    }

    public Completable init()
    {
        return Completable.defer(() ->
        {
            if (this.state.compareAndSet(State.IDLE, State.CONNECTING))
            {
                final var newConnection = createNewConnection();
                this.tcpSocket.getAndSet(newConnection);
                return newConnection.ignoreElement();
            }
            throw new IllegalStateException();
        });
    }

    private boolean reconnect()
    {
        if (this.state.compareAndSet(State.CONNECTED, State.CONNECTING))
        {
            final var previous = this.tcpSocket.getAndSet(createNewConnection());
            if (previous != null)
            {
                previous.flatMapCompletable(s -> s.rxClose()).subscribe();
            }
            return true;
        }
        else
        {
            return false;
        }
    }

    private Completable send(Buffer ssts)
    {
        return this.tcpSocket.get().flatMapCompletable(socket -> socket.rxWrite(ssts));
    }

    private static final Buffer toBuffer(SocketStreamedTraceSegment ssts) throws IOException
    {
        final var baos = new ByteArrayOutputStream();
        TraceWrapper.newBuilder().setSocketStreamedTraceSegment(ssts).build().writeDelimitedTo(baos);
        return Buffer.buffer(baos.toByteArray());
    }

}
