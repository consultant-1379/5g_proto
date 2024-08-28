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
 * Created on: Sep 6, 2019
 *     Author: eedala
 */

package com.ericsson.sc.proxyal.outlierlogservice;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Timestamp;
import com.google.protobuf.UInt64Value;

import io.scp.api.v1.OutlierLogEvent;
import io.scp.api.v1.OutlierLogEventResponse;
import io.envoyproxy.envoy.data.cluster.v3.Action;
import io.envoyproxy.envoy.data.cluster.v3.OutlierDetectionEvent;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subscribers.TestSubscriber;

/**
 * 
 */
class OutlierLogServiceTest
{
    private static final Logger log = LoggerFactory.getLogger(OutlierLogServiceTest.class);

    @Test
    public void testFlowWithoutClient() throws InterruptedException
    {
        var ols = new OutlierLogServiceCsa();

        final PublishSubject<OutlierLogEvent> reqFlow = PublishSubject.create();

        Flowable<OutlierLogEventResponse> respFlow = ols.streamOutlierLogEvents(reqFlow.toFlowable(BackpressureStrategy.LATEST));
        // The responses are empty, but we can count them later:
        TestSubscriber<OutlierLogEventResponse> responseTester = respFlow.test();

        PublishSubject<EnvoyStatus> eventStream = ols.getOutlierEventStream();
        eventStream.doOnNext(ev -> log.info("Event" + ev)).subscribe();

        TestObserver<EnvoyStatus> eventTester = new TestObserver<>();
        eventStream.subscribe(eventTester);

        // TODO: (DND-23554) clean-up tests when envoy patch is removed

        // Eject with different upstream URLs
        reqFlow.onNext(buildEvent("envoy-a7rl4r", "10.1.2.3:80|occ-1", Action.EJECT, true)); // [0]
        reqFlow.onNext(buildEvent("envoy-a7rl4r", "10.1.2.3|occ-1", Action.EJECT, true)); // [1]
        reqFlow.onNext(buildEvent("envoy-a7rl4r", "10.1.2.4:90|", Action.EJECT, true)); // [2]
        reqFlow.onNext(buildEvent("envoy-a7rl4r", "10.1.2.5:940", Action.EJECT, true)); // [3] original Envoy

        // Eject that is not enforced, only logged not counted in eventTester:
        reqFlow.onNext(buildEvent("envoy-a7rl4r", "10.1.2.3:80|occ-1", Action.EJECT, false));

        // Uneject:
        reqFlow.onNext(buildEvent("envoy-a7rl4r", "10.1.2.3:80|occ-1", Action.UNEJECT, false)); // [4]
        // Uneject, enforce = true is wrong but should not matter
        reqFlow.onNext(buildEvent("envoy-a7rl4r", "10.1.2.3:80|occ-1", Action.UNEJECT, true)); // [5]

        // Eject with ipV6 URLs
        reqFlow.onNext(buildEvent("envoy-a7rl4r", "[240e:180:20:800::1502]:80|occ-1", Action.EJECT, true)); // [6]

        eventTester.awaitCount(7);
        // Check that count is ok, both responses to Envoy and also events to
        // consolidator:
        responseTester.assertValueCount(8);
        eventTester.assertValueCount(7);

        // Check individual fields:
        eventTester.assertValueAt(0, e -> e.getEnvoyId().equals("envoy-a7rl4r"));
        eventTester.assertValueAt(0, e -> e.getUrl().equals("10.1.2.3:80"));
        eventTester.assertValueAt(0, e -> e.getCluster().equals("rr_West1_slcHandler"));
        eventTester.assertValueAt(0, e -> e.getOperationalState().equals(OperationalState.BLOCKED));

        // Only check producer URLs, the other fields have been tested above:
        eventTester.assertValueAt(1, e -> e.getUrl().equals("10.1.2.3"));
        eventTester.assertValueAt(2, e -> e.getUrl().equals("10.1.2.4:90"));
        eventTester.assertValueAt(3, e -> e.getUrl().equals("10.1.2.5:940"));

        // Only test the op-states, the other fields have been tested above:
        // eventTester.assertValueAt(4, e ->
        // e.getOperationalState().equals(OperationalState.UNREACHABLE));
        eventTester.assertValueAt(4, e -> e.getOperationalState().equals(OperationalState.REACHABLE));
        eventTester.assertValueAt(5, e -> e.getOperationalState().equals(OperationalState.REACHABLE));

        // Only check producer URLs, the other fields have been tested above:
        eventTester.assertValueAt(6, e -> e.getUrl().equals("[240e:180:20:800::1502]:80"));

        // General tests at the end
        responseTester.assertNotComplete();
        responseTester.assertNoErrors();
        responseTester.assertNoTimeout();
    }

    /**
     * Helper function to build an Event for testing
     * 
     * @param envoyId
     * @param upstreamUrl
     * @param action
     * @param enforced
     * @return
     */
    public OutlierLogEvent buildEvent(String envoyId,
                                      String upstreamUrl,
                                      Action action,
                                      boolean enforced)
    {
        return OutlierLogEvent.newBuilder() //
                              .setEnvoyId(envoyId) //
                              .setEventDetails(OutlierDetectionEvent.newBuilder() //
                                                                    .setAction(action) //
                                                                    .setEnforced(enforced) //
                                                                    .setUpstreamUrl(upstreamUrl) //
                                                                    .setClusterName("rr_West1_slcHandler") //
                                                                    .setNumEjections(3) //
                                                                    .setSecsSinceLastAction(UInt64Value.of(23)) //
                                                                    .setTimestamp(Timestamp.newBuilder() //
                                                                                           .setSeconds(11) //
                                                                                           .setNanos(3) //
                                                                                           .build()) //
                                                                    .build())
                              .build();
    }
}
