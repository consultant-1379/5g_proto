/**
 * @author enubars, eedcsi
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
//    iterations: 1
    duration: "3m"          
};

export default function() {

    const csa = `https://${__ENV.SCP_HOST}:${__ENV.SCP_PORT}/`
    const method = "POST";
    const uri = "notify"
    const headers_chfsim_1 = {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json",
//          "x-notify-uri": "http://eric-chfsim-1/nchf-spendinglimitcontrol/v1/subscriptions/notify"
            "x-notify-uri": "http://eric-chfsim-1/nchf-convergedcharging/v1/chargingdata/notify_cc_receiver"    
//            "x-notify-uri": "http://eric-chfsim-1/notify"
//            "x-notify-uri": "http://eric-chfsim-1//notify_cc_receiver"
        }
    }
	
    const headers_chfsim_2 = {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json",
            "x-notify-uri": "http://eric-chfsim-2/nchf-convergedcharging/v1/chargingdata/notify_cc_receiver"    
        }
    }

    const headers_chfsim_3 = {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json",
            "x-notify-uri": "http://eric-chfsim-3/nchf-convergedcharging/v1/chargingdata/notify_cc_receiver"    
        }
    }
	
	
    //sending 100 CC-Notify requests in parallel in every iteration. This batch is executed by each VU.
    let responses = http.batch([

        [method, csa + uri, null, headers_chfsim_1],
        [method, csa + uri, null, headers_chfsim_2],
        [method, csa + uri, null, headers_chfsim_3],
        [method, csa + uri, null, headers_chfsim_1],
        [method, csa + uri, null, headers_chfsim_2],
        [method, csa + uri, null, headers_chfsim_3],
        [method, csa + uri, null, headers_chfsim_1],
        [method, csa + uri, null, headers_chfsim_2],
        [method, csa + uri, null, headers_chfsim_3],
        [method, csa + uri, null, headers_chfsim_1],
        [method, csa + uri, null, headers_chfsim_2],
        [method, csa + uri, null, headers_chfsim_3],
        [method, csa + uri, null, headers_chfsim_1],
        [method, csa + uri, null, headers_chfsim_2],
        [method, csa + uri, null, headers_chfsim_3],
        [method, csa + uri, null, headers_chfsim_1],
        [method, csa + uri, null, headers_chfsim_2],
        [method, csa + uri, null, headers_chfsim_3],
        [method, csa + uri, null, headers_chfsim_1],
        [method, csa + uri, null, headers_chfsim_2],
        [method, csa + uri, null, headers_chfsim_3],
        [method, csa + uri, null, headers_chfsim_1],
        [method, csa + uri, null, headers_chfsim_2],
        [method, csa + uri, null, headers_chfsim_3],
        [method, csa + uri, null, headers_chfsim_1],
        [method, csa + uri, null, headers_chfsim_2],
        [method, csa + uri, null, headers_chfsim_3],
        [method, csa + uri, null, headers_chfsim_1],
        [method, csa + uri, null, headers_chfsim_2],
        [method, csa + uri, null, headers_chfsim_3],
        [method, csa + uri, null, headers_chfsim_1],
        [method, csa + uri, null, headers_chfsim_2],
        [method, csa + uri, null, headers_chfsim_3],
        [method, csa + uri, null, headers_chfsim_1],
        [method, csa + uri, null, headers_chfsim_2],
        [method, csa + uri, null, headers_chfsim_3],
        [method, csa + uri, null, headers_chfsim_1],
        [method, csa + uri, null, headers_chfsim_2],
        [method, csa + uri, null, headers_chfsim_3],
        [method, csa + uri, null, headers_chfsim_1],
        [method, csa + uri, null, headers_chfsim_2],
        [method, csa + uri, null, headers_chfsim_3],
        [method, csa + uri, null, headers_chfsim_1],
        [method, csa + uri, null, headers_chfsim_2],
        [method, csa + uri, null, headers_chfsim_3],
        [method, csa + uri, null, headers_chfsim_1],
        [method, csa + uri, null, headers_chfsim_2],
        [method, csa + uri, null, headers_chfsim_3],
        [method, csa + uri, null, headers_chfsim_1],
        [method, csa + uri, null, headers_chfsim_2],
        [method, csa + uri, null, headers_chfsim_3],
        [method, csa + uri, null, headers_chfsim_1],
        [method, csa + uri, null, headers_chfsim_2],
        [method, csa + uri, null, headers_chfsim_3],
        [method, csa + uri, null, headers_chfsim_1],
        [method, csa + uri, null, headers_chfsim_2],
        [method, csa + uri, null, headers_chfsim_3],
        [method, csa + uri, null, headers_chfsim_1],
        [method, csa + uri, null, headers_chfsim_2],
        [method, csa + uri, null, headers_chfsim_3],
        [method, csa + uri, null, headers_chfsim_1],
        [method, csa + uri, null, headers_chfsim_2],
        [method, csa + uri, null, headers_chfsim_3],
        [method, csa + uri, null, headers_chfsim_1],
        [method, csa + uri, null, headers_chfsim_2],
        [method, csa + uri, null, headers_chfsim_3],
        [method, csa + uri, null, headers_chfsim_1],
        [method, csa + uri, null, headers_chfsim_2],
        [method, csa + uri, null, headers_chfsim_3],
        [method, csa + uri, null, headers_chfsim_1],
        [method, csa + uri, null, headers_chfsim_2],
        [method, csa + uri, null, headers_chfsim_3],
        [method, csa + uri, null, headers_chfsim_1],
        [method, csa + uri, null, headers_chfsim_2],
        [method, csa + uri, null, headers_chfsim_3],
        [method, csa + uri, null, headers_chfsim_1],
        [method, csa + uri, null, headers_chfsim_2],
        [method, csa + uri, null, headers_chfsim_3],
        [method, csa + uri, null, headers_chfsim_1],
        [method, csa + uri, null, headers_chfsim_2],
        [method, csa + uri, null, headers_chfsim_3],
        [method, csa + uri, null, headers_chfsim_1],
        [method, csa + uri, null, headers_chfsim_2],
        [method, csa + uri, null, headers_chfsim_3],
        [method, csa + uri, null, headers_chfsim_1],
        [method, csa + uri, null, headers_chfsim_2],
        [method, csa + uri, null, headers_chfsim_3],
        [method, csa + uri, null, headers_chfsim_1],
        [method, csa + uri, null, headers_chfsim_2],
        [method, csa + uri, null, headers_chfsim_3],
        [method, csa + uri, null, headers_chfsim_1],
        [method, csa + uri, null, headers_chfsim_2],
        [method, csa + uri, null, headers_chfsim_3],
        [method, csa + uri, null, headers_chfsim_1],
        [method, csa + uri, null, headers_chfsim_2],
        [method, csa + uri, null, headers_chfsim_3],
        [method, csa + uri, null, headers_chfsim_1],
        [method, csa + uri, null, headers_chfsim_2],
        [method, csa + uri, null, headers_chfsim_3],
        [method, csa + uri, null, headers_chfsim_1]

    ]);

    for(var i=0;i<100;i++)
    {
        check(responses[i], {
           "protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
           "status was 204": (r) => r.status == 204
        });
    }

}
