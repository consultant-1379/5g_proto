/**
 * @author xkonska, eedcsi
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

const uri = "nchf-spendinglimitcontrol/v1/subscriptions/";
const token = "Bearer " + `${__ENV.TOKEN}`;
const headers = {
    headers: {
        "Connection": "keep-alive",
        "Content-Type": "application/json",
        "Accept": "application/json",
        "Authorization": token
    }
};

/** Send batch messages of 
 *  100 slc subscribe
 *  100 slc_modify
 *  100 slc_notify
 *  100 slc_unsubscribe
 *  requests 
 */
export default function() {

    const smf_host = `smf${__VU}.ericsson.se`;
    const supi = `nai-${__VU}${__ITER}@ericsson.com`;
    const csa = `https://${__ENV.SCP_HOST}:${__ENV.SCP_PORT}/`;

    var ids = subscribe_operation(smf_host, supi, csa);
    if (ids.length > 0) {
    	modify_operation(ids, smf_host, supi, csa);
        notify_operation(csa);
    	unsubscribe_operation(ids, smf_host, supi, csa);
    }
}

//spending limit control subscribe operation
function subscribe_operation(smf_host, supi, csa) {

    const method = "POST";
   
    var requests = [[]];

    const body = JSON.stringify({
        "supi":"imsi-310310140000120",
        "gpsi": "msisdn-12001400120",
        "policyCounterIds" : ["71008", "71009", "71300", "71000"],
	"notifyUri": "http://" + smf_host + ":8080/rar"
    }, null, "   ");

    for(var i=0;i<100;i++) {
        requests[i] = [method, csa + uri, body, headers];
    }

    //console.log("Sending 100 x SLC-Subscribe")

    let responses = http.batch(requests);

    var resp_length = Object.keys(responses).length;
    for(var i=0;i<resp_length;i++)
    {
       check(responses[i], {
         "slc subscribe: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
         "slc subscribe: status was 201": (r) => r.status == 201
       });
    }

    var ids = process_response_headers(responses);
    return ids;
}

function process_response_headers(responses) {

   var resp_length = Object.keys(responses).length;

   var ids = [];
   for (var i=0;i<resp_length;i++) {
      var location = responses[i].headers["Location"];
      if (location != null && location.includes("occ")) {
         var array = location.split("/");
         ids[i] = array[array.length - 1];
      }
   }
   return ids;
}


//spending limit control modify operation
function modify_operation(ids, smf_host, supi, csa) {

    const method = "PUT";

    const body = JSON.stringify({
        "statusInfos" : {
           "71008" : {
              "policyCounterId":"71008",
              "currentStatus":"active"
           },
           "71009":{
              "policyCounterId":"71009",
              "currentStatus":"active"
           },
          "71300":{
             "policyCounterId":"71300",
             "currentStatus":"active"
           },
           "71000":{
              "policyCounterId":"71000",
              "currentStatus":"active"
           }
        },
        "supi":"imsi-310310140000120"
        }, null, "   ");

    var requests = [[]];

    for(var i=0;i<ids.length;i++) {
        requests[i] = [method, csa + uri + ids[i], body, headers];
    }

    //console.log("Sending 100 x SLC-Modify")

    let responses = http.batch(requests);

    var resp_length = Object.keys(responses).length;    
    for(var i=0;i<resp_length;i++)
    {
       check(responses[i], {
         "slc modify: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
         "slc modify: status was 200": (r) => r.status == 200
       });
    }
}


//spending limit control unsubscribe operation
function unsubscribe_operation(ids, smf_host, supi, csa) {

    const method = "DELETE";

    var requests = [[]];

    for(var i=0;i<ids.length;i++) {
        requests[i] = [method, csa + uri + ids[i], {}, headers];
    }

    //console.log("Sending 100 x SLC-Unsubscribe")

    let responses = http.batch(requests);
    
    var resp_length = Object.keys(responses).length;    
    for(var i=0;i<resp_length;i++)
    {
        check(responses[i], {
           "slc unsubscribe: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
           "slc unsubscribe: status was 204": (r) => r.status == 204
        });
    }
}

//spending limit control NOTIFY operation
function notify_operation(csa){
    const method = "POST";
    const uri = "notify"
    const headers_chfsim_1 = {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json",
            "x-notify-uri": "http://eric-chfsim-1/nchf-spendinglimitcontrol/v1/subscriptions/notify"
//          "x-notify-uri": "http://eric-chfsim-1/nchf-convergedcharging/v1/chargingdata/notify_cc_receiver"    
        }
    }
	
    const headers_chfsim_2 = {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json",
            "x-notify-uri": "http://eric-chfsim-2/nchf-spendinglimitcontrol/v1/subscriptions/notify"
        }
    }

    const headers_chfsim_3 = {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json",
            "x-notify-uri": "http://eric-chfsim-3/nchf-spendinglimitcontrol/v1/subscriptions/notify"
        }
    }
	
    //console.log("Sending 100 x SLC-Notify")

    //sending 100 SLC-Notify requests in parallel in every iteration. This batch is executed by each VU.
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
	
    var resp_length = Object.keys(responses).length;    

    for(var i=0;i<resp_length;i++)
    {
        check(responses[i], {
           "slc notify: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
           "slc notify: status was 204": (r) => r.status == 204
        });
    }
}

