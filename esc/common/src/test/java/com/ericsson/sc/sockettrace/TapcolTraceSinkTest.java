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
 * Created on: Jan 3, 2023
 *     Author: znpvaap
 */
package com.ericsson.sc.sockettrace;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;

import com.ericsson.sc.sockettrace.TapcolTraceSink.State;
import com.ericsson.sc.tapcol.TapCol.DivisionMethod;
import com.ericsson.sc.tapcol.TapCollectorReceiverTcp;
import com.ericsson.sc.tapcol.file.FilePacketSink;
import com.ericsson.utilities.reactivex.VertxBuilder;
import io.envoyproxy.envoy.config.core.v3.Address;
import io.envoyproxy.envoy.config.core.v3.SocketAddress;
import io.envoyproxy.envoy.config.core.v3.SocketAddress.Protocol;
import io.envoyproxy.envoy.data.tap.v3.Connection;
import io.envoyproxy.envoy.data.tap.v3.SocketStreamedTraceSegment;
import io.reactivex.Flowable;
import io.vertx.reactivex.core.Vertx;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

public class TapcolTraceSinkTest
{
    private static final Logger log = LoggerFactory.getLogger(TapcolTraceSinkTest.class);
    private final Vertx vertx = VertxBuilder.newInstance().modifyRxSchedulers(false).build();

    private final FilePacketSink fps = new FilePacketSink();
    private final TapCollectorReceiverTcp tapcolReceiver = new TapCollectorReceiverTcp(9999, //
                                                                                       "127.0.0.2",
                                                                                       fps, //
                                                                                       false,
                                                                                       Flux.empty(),
                                                                                       DivisionMethod.TRUNCATE,
                                                                                       1000);

    @BeforeClass
    void startServer()
    {
        tapcolReceiver.start().block();
    }

    @AfterClass
    void closeServer()
    {
        tapcolReceiver.stop().block();
        log.info("Server is closed");
    }

    @Test(enabled = true)
    void validateConsumeTrace()
    {
        final var local = new InetSocketAddress("127.0.0.1", 1000);
        final var remote = new InetSocketAddress("127.0.0.2", 1002);

        final var ssts = SocketStreamedTraceSegment.newBuilder() //
                                                   .setTraceId(1)
                                                   .setConnection(Connection.newBuilder()
                                                                            .setLocalAddress(Address.newBuilder()
                                                                                                    .setSocketAddress(SocketAddress.newBuilder()
                                                                                                                                   .setProtocol(Protocol.TCP)
                                                                                                                                   .setAddress(local.getAddress()
                                                                                                                                                    .getHostAddress())
                                                                                                                                   .setPortValue(local.getPort())))
                                                                            .setRemoteAddress(Address.newBuilder()
                                                                                                     .setSocketAddress(SocketAddress.newBuilder()
                                                                                                                                    .setProtocol(Protocol.TCP)
                                                                                                                                    .setAddress(remote.getAddress()
                                                                                                                                                      .getHostAddress())
                                                                                                                                    .setPortValue(remote.getPort()))))
                                                   .build();

        final var traceSink = new TapcolTraceSink(vertx, "127.0.0.2", 9999, Flowable.just("bsf1"));

        final var flowableSsts = Flowable.just(ssts);

        traceSink.init().blockingAwait();

        final var response = traceSink.consumeTrace(flowableSsts).blockingGet();
        assertEquals(response, null, "No traces sent");
    }

    @Test(enabled = true)
    void invalidSsts()
    {
        final var invalidSsts = SocketStreamedTraceSegment.newBuilder() //
                                                          .setTraceId(1)
                                                          .build();

        final var traceSink = new TapcolTraceSink(vertx, "127.0.0.2", 9999, Flowable.just("bsf1"));

        final var flowableInvalidSsts = Flowable.just(invalidSsts);

        traceSink.init().blockingAwait();

        assertThrows(IllegalArgumentException.class, () -> traceSink.consumeTrace(flowableInvalidSsts).blockingAwait());

    }

    @Test(enabled = true)
    void validateReconnectFromIdle() throws InterruptedException
    {
        final var traceSink = new TapcolTraceSink(vertx, "127.0.0.2", 9999, Flowable.just("bsf1"));

        // IDLE state because init() is not called

        traceSink.reconnect();

        TimeUnit.SECONDS.sleep(1);

        assertEquals(traceSink.getState(), State.CONNECTED, "State should be CONNECTED");
    }

    @Test(enabled = true)
    void validateReconnectNegativeFromConnected() throws InterruptedException
    {
        final var traceSink = new TapcolTraceSink(vertx, "127.0.0.2", 9999, Flowable.just("bsf1"));

        traceSink.init().blockingAwait(); // Set the state to CONNECTED

        TimeUnit.SECONDS.sleep(1);

        traceSink.reconnect();

        assertEquals(traceSink.getState(), State.CONNECTING, "State should be CONNECTING");

        TimeUnit.SECONDS.sleep(1);

        assertEquals(traceSink.getState(), State.CONNECTED, "State should be CONNECTED");

    }

    @Test(enabled = true)
    void testNewConnection()
    {
        final var traceSink = new TapcolTraceSink(vertx, "127.0.0.2", 9999, Flowable.just("bsf1"));
        final var response = traceSink.init().blockingGet();
        assertEquals(response, null, "Connection did not setup");
    }

    @Test(enabled = true)
    void testNewConnectionNegative()
    {
        final var traceSink = new TapcolTraceSink(vertx, "127.0.0.2", 9999, Flowable.just("bsf1"));

        traceSink.init().blockingAwait();
        assertThrows(IllegalStateException.class, () -> traceSink.init().blockingAwait());
    }
}