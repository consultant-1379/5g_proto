/**
 * @author eyossan
 */

import http from "k6/http";
import {
    check
} from "k6";

export let options = {
    hosts: {
        "scp.ericsson.se":`${__ENV.SCP_IP}`
    },
    tlsAuth: [{
        domains: [`${__ENV.SCP_HOST}`],
        cert: open("/certs/K6.crt"),
        key: open("/certs/K6.key")
    }],
    vus: 1,
    duration: 10
};


const uri = "nchf-convergedcharging/v1/chargingdata/";
const headers = {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json"
        }
    };

/** Send batch messages of 
 *  CC_NOTIFY  x cc_notify
 *  requests 
 */
export default function() {

    const smf_host = `smf${__VU}.ericsson.se`;
    const scp = `https://${__ENV.SCP_HOST}:${__ENV.SCP_PORT}/`;

    let ids = notify_operation(scp);

//converged charging NOTIFY operation
function notify_operation(ids, scp) {


let notify = `${__ENV.CC_NOTIFY}`;
if(notify>0){
    const method = "POST";

    let requests = [[]];

    let counter=0; // counter to check that we do not get out of bounds for create requests
    const uri_chfsim_a = "http://" + `${__ENV.SCP_IP}:${__ENV.CG_PORT_A}` + "/nchf-convergedcharging/notify-cg/v2/chargingdata/notify_cc_receiver";
    const uri_chfsim_b = "http://" + `${__ENV.SCP_IP}:${__ENV.CG_PORT_B}` + "/nchf-convergedcharging/notify-cg/v2/chargingdata/notify_cc_receiver";
    const uri_chfsim_c = "http://" + `${__ENV.SCP_IP}:${__ENV.CG_PORT_C}` + "/nchf-convergedcharging/notify-cg/v2/chargingdata/notify_cc_receiver";

    const headers_chfsim_a = {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json",
            "authority": `${__ENV.SCP_IP}:${__ENV.CG_PORT_A}`
        }
    }

    const headers_chfsim_b = {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json",
            "authority": `${__ENV.SCP_IP}:${__ENV.CG_PORT_B}`
        }
    }
    
    const headers_chfsim_c = {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json",
            "authority": `${__ENV.SCP_IP}:${__ENV.CG_PORT_C}`
        }
    }
        
    let notify_body = get_notify_body(smf_host);

    //console.log("Sending x CC-Notify")
    let list_resp = [[]];
    for(let i=0;i<notify;i++) {
        list_resp[i] = [method, uri_chfsim_a, notify_body, headers_chfsim_a];
        i++;
        if(i<notify){
        	list_resp[i] = [method, uri_chfsim_b, notify_body, headers_chfsim_b];
        }
        i++;
        if(i<notify){
        	list_resp[i] = [method, uri_chfsim_c, notify_body, headers_chfsim_c];
        }
    }
    
    //sending 100 release requests in parallel in every iteration. This batch is executed by each VU.
    let responses = http.batch(list_resp);
    let resp_length = Object.keys(responses).length;
   
    for(let i=0;i<resp_length;i++)
    {
       check(responses[i], {
           "cc notify: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
           "cc notify: status was 204": (r) => r.status == 204
       });
    }
  }
}

function get_notify_body(smf_host) {
        return JSON.stringify(
                {
                    "notificationType": "ABORT_CHARGING"
                }, null, " ");
}

}

