/**
 * @author enubars, eedcsi
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
    vus: 1,
//    iterations: 1
    duration: "3m"          
};

export default function() {

    const smf_ip = `192.168.0.${__VU}`;
    const supi = `nai-${__VU}${__ITER}@ericsson.com`;
    const csa = "https://csa.ericsson.se:31733/";

    //notify

    const method = "POST";
    const uri = "notify"
    const headers = {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json",
            "x-notify-uri": "http://eric-chfsim-1/nchf-spendinglimitcontrol/v1/subscriptions/notify"
        }
    }

    let responses = http.batch([

        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers],
        [method, csa + uri, null, headers]

    ]);

    check(responses[0], {
        "protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
        "status was 204": (r) => r.status == 204
    });

}
