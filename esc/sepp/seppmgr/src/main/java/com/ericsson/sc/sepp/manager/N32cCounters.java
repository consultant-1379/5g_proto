/**
 * COPYRIGHT ERICSSON GMBH 2022
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Nov 10, 2022
 *     Author: erlxiui
 */

package com.ericsson.sc.sepp.manager;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.prometheus.client.Counter;
import io.vertx.core.http.HttpMethod;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ericsson.utilities.metrics.MetricRegister;

public class N32cCounters
{
    private N32cCounters()
    {

    }

    private static final Logger log = LoggerFactory.getLogger(N32cCounters.class);
    private static final Counter ccInitiatingN32cOutReq = MetricRegister.singleton()
                                                                        .register(Counter.build()
                                                                                         .namespace("n32c")
                                                                                         .name("out_requests_total")
                                                                                         .labelNames("nf", "nf_instance", "roaming_partner", "method", "path")
                                                                                         .help("Number of outgoing SEPP N32c handshake Requests of Initiating SEPP")
                                                                                         .register());

    private static final Counter ccInitiatingN32cInSuccResp = MetricRegister.singleton()
                                                                            .register(Counter.build()
                                                                                             .namespace("n32c")
                                                                                             .name("in_responses_success_total")
                                                                                             .labelNames("nf",
                                                                                                         "nf_instance",
                                                                                                         "roaming_partner",
                                                                                                         "method",
                                                                                                         "path",
                                                                                                         "status")
                                                                                             .help("Number of incoming SEPP N32c handshake with Successful Responses of Initiating SEPP")
                                                                                             .register());
    private static final Counter ccInitiatingN32cFailures = MetricRegister.singleton()
                                                                          .register(Counter.build()
                                                                                           .namespace("n32c")
                                                                                           .name("in_responses_failure_total")
                                                                                           .labelNames("nf", "nf_instance", "roaming_partner", "error_body")
                                                                                           .help("Number of SEPP N32c handshake failures of Initiating SEPP")
                                                                                           .register());
    private static final Counter ccInitiatingN32cProtocolFailures = MetricRegister.singleton()
                                                                                  .register(Counter.build()
                                                                                                   .namespace("n32c")
                                                                                                   .name("in_responses_protocol_failure_total")
                                                                                                   .labelNames("nf", "nf_instance", "roaming_partner", "cause")
                                                                                                   .help("Number of SEPP N32c handshake protocol failures of Initiating SEPP")
                                                                                                   .register());
    private static final Counter ccRespondingN32cInReq = MetricRegister.singleton()
                                                                       .register(Counter.build()
                                                                                        .namespace("n32c")
                                                                                        .name("in_requests_total")
                                                                                        .labelNames("nf", "nf_instance", "roaming_partner")
                                                                                        .help("Number of incoming SEPP N32c handshake Requests of Responding SEPP")
                                                                                        .register());

    private static final Counter ccRespondingN32cOutSuccResp = MetricRegister.singleton()
                                                                             .register(Counter.build()
                                                                                              .namespace("n32c")
                                                                                              .name("out_responses_success_total")
                                                                                              .labelNames("nf", "nf_instance", "roaming_partner", "status")
                                                                                              .help("Number of outgoing SEPP N32c handshake Successful Responses of Responding SEPP")
                                                                                              .register());
    private static final Counter ccRespondingN32cFailures = MetricRegister.singleton()
                                                                          .register(Counter.build()
                                                                                           .namespace("n32c")
                                                                                           .name("out_responses_failure_total")
                                                                                           .labelNames("nf", "nf_instance", "roaming_partner", "error_body")
                                                                                           .help("Number of SEPP N32c handshake failures of Responding SEPP")
                                                                                           .register());

    public static void stepCcInitiatingN32cOutReq(final String seppName,
                                                  final String rpName,
                                                  final HttpMethod method,
                                                  final String path)
    {
        log.debug("seppName={}, rpName={}, method={}, path={}", seppName, rpName, method, path);

        final List<String> labelValues = new ArrayList<>();

        labelValues.add("sepp"); // nf
        labelValues.add(seppName); // sepp Name
        labelValues.add(rpName); // rp Name
        labelValues.add(method.name()); // method
        labelValues.add(path); // path

        ccInitiatingN32cOutReq.labels(labelValues.toArray(new String[0])).inc();

    }

    public static void stepCcInitiatingN32cInSuccResp(final String seppName,
                                                      final String rpName,
                                                      final HttpMethod method,
                                                      final String path,
                                                      final Integer statusCode)
    {
        log.debug("seppName={}, rpName={}, method={}, path={}, statusCode={}", seppName, rpName, method, path, statusCode);

        final List<String> labelValues = new ArrayList<>();
        labelValues.add("sepp"); // nf
        labelValues.add(seppName); // sepp Name
        labelValues.add(rpName); // rp Name
        labelValues.add(method.name()); // method
        labelValues.add(path); // path
        labelValues.add(HttpResponseStatus.valueOf(statusCode).toString()); // status

        ccInitiatingN32cInSuccResp.labels(labelValues.toArray(new String[0])).inc();

    }

    public static void stepCcInitiatingN32cFailures(final String seppName,
                                                    final String rpName,
                                                    final String error)
    {
        log.debug("seppName={}, rpName={}, cause={}", seppName, rpName, error);
        final List<String> labelValues = new ArrayList<>();
        labelValues.add("sepp"); // nf
        labelValues.add(seppName); // sepp Name
        labelValues.add(rpName); // rp Name
        labelValues.add(error); // error_body

        ccInitiatingN32cFailures.labels(labelValues.toArray(new String[0])).inc();

    }

    public static void stepCcInitiatingN32cProtocolFailures(final String seppName,
                                                            final String rpName,
                                                            final String error)
    {
        log.debug("seppName={}, rpName={}, cause={}", seppName, rpName, error);
        final List<String> labelValues = new ArrayList<>();
        labelValues.add("sepp"); // nf
        labelValues.add(seppName); // sepp Name
        labelValues.add(rpName); // rp Name
        labelValues.add(error); // cause

        ccInitiatingN32cProtocolFailures.labels(labelValues.toArray(new String[0])).inc();

    }

    public static void stepCcRespondingN32cInReq(final String seppName,
                                                 final String rpName)
    {
        log.debug("seppName={}, rpName={}", seppName, rpName);

        final List<String> labelValues = new ArrayList<>();

        labelValues.add("sepp"); // nf
        labelValues.add(seppName); // sepp Name
        labelValues.add(rpName); // rp Name

        ccRespondingN32cInReq.labels(labelValues.toArray(new String[0])).inc();

    }

    public static void stepCcRespondingN32cOutSuccResp(final String seppName,
                                                       final String rpName,
                                                       final Integer statusCode)
    {
        log.debug("seppName={}, rpName={}, statusCode={}", seppName, rpName, statusCode);

        final List<String> labelValues = new ArrayList<>();
        labelValues.add("sepp"); // nf
        labelValues.add(seppName); // sepp Name
        labelValues.add(rpName); // rp Name
        labelValues.add(HttpResponseStatus.valueOf(statusCode).toString()); // status

        ccRespondingN32cOutSuccResp.labels(labelValues.toArray(new String[0])).inc();

    }

    public static void stepCcRespondingN32cFailures(final String seppName,
                                                    final String rpName,
                                                    final String error)
    {
        log.debug("seppName={}, rpName={}, cause={}", seppName, rpName, error);
        final List<String> labelValues = new ArrayList<>();
        labelValues.add("sepp"); // nf
        labelValues.add(seppName); // sepp Name
        labelValues.add(rpName); // rp Name
        labelValues.add(error); // error_body

        ccRespondingN32cFailures.labels(labelValues.toArray(new String[0])).inc();

    }

}
