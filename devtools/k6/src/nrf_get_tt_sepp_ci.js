/**
 * @author eyossan
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

    //Converged charging create operation

    const method = "GET";
    const uri = "nnrf-disc/v1/nf-instances?requester-nf-type=SMF&target-nf-type=CHF&supi=imsi-460001357924675"
    const headers = {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json",
            "3gpp-Sbi-target-apiRoot": "eric-seppsim-p3-mcc-262-mnc-73"
        }
    }

    const body = JSON.stringify({
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
         "status was 200": (r) => r.status == 200
       });
    }
}
