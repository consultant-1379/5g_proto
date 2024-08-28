/**
 * @author enubars
 */

import http from "k6/http";
import {
    check
} from "k6";

export let options = {
    hosts: {
        "csa.ericsson.se":`${__ENV.SCP_IP}`
    },
    tlsAuth: [{
        domains: [`${__ENV.SCP_HOST}`],
        cert: open("/certs/K6.crt"),
        key: open("/certs/K6.key")
    }],
    vus: 1,
    duration: 10
};

export default function() {

    const smf_host = `smf${__VU}.ericsson.se`;
    const supi = `nai-${__VU}${__ITER}@ericsson.com`;
    const csa = `https://${__ENV.SCP_HOST}:${__ENV.SCP_PORT}/`;    

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
	"notifyUri": "http://" + smf_host + ":8080/rar"
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

    for(var i=0;i<100;i++)
    {
       check(responses[i], {
         "protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
         "status was 201": (r) => r.status == 201
       });
    }
}
