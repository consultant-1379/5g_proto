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
    vus: 1,
//    iterations: 1
    duration: "3m"          
};

export default function() {

    const smf_ip = `192.168.0.${__VU}`;
    const supi = `nai-${__VU}${__ITER}@ericsson.com`;
    const csa = "https://csa.ericsson.se:30270/";  // <--- modify this line, change SCP_PORT

    //notify

    const method = "POST";
    const uri = "notify"
    const headers = {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json",
//            "x-notify-uri": "http://hellokube.5g-bsf-enubars.svc.cluster.local"
            "x-notify-uri": "http://10.210.156.5:30720/nchf-spendinglimitcontrol/v1/subscription  // <--- modify this line, change port of CHFSim
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
