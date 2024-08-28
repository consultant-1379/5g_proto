/**
 * @author enubars
 */

import http from "k6/http";
import {
    check
} from "k6";

export let options = {
    hosts: {
        "csa.ericsson.se": "10.210.156.5"
    },
    tlsAuth: [{
        domains: ["csa.ericsson.se"],
        cert: open("/certs/K6.crt"),
        key: open("/certs/K6.key")
    }],
   vus: 10,
    iterations: 1
};

export default function() {

    const smf_ip = `192.168.0.${__VU}`;
    const supi = `nai-${__VU}${__ITER}@ericsson.com`;
    const csa = "https://csa.ericsson.se:31793/";

    // spending limit controlT

    const method = "POST";
    const uri = "nchf-spendinglimitcontrol/v1/subscriptions"
    const headers = {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json"
        }
    }
    const body = JSON.stringify({
        "supi":"imsi-310310140000120",
        "gpsi": "msisdn-12001400120",
        "policyCounterIds" : ["71008", "71009", "71300", "71000"],
        "notifyUri": "/npcf/callbacks/nchf-spendinglimitcontrol/subscrptions/n28-imsi-310310140000120.1556051876639.1555342164.12.6680143728469868556"
    }, null, "   ");

    let responses = http.batch([

        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers]

    ]);

    check(responses[0], {
        "protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
        "status was 201": (r) => r.status == 201
    });

}
