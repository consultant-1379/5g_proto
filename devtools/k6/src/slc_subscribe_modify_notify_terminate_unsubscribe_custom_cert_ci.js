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
        cert: `${__ENV.CERT}`,
        key: `${__ENV.KEY}`
    }],
    vus: 1,
    duration: 10
};

const uri = "nchf-spendinglimitcontrol/v1/subscriptions/";
const headers = {
headers: {
    "Connection": "keep-alive",
    "Content-Type": "application/json",
    "Accept": "application/json",
    "Host": `${__ENV.SCP_HOST}`+":"+`${__ENV.SCP_PORT}`
}
};

/** Send batch messages of 
 *  100 slc subscribe
 *  100 slc_modify
 *  100 slc_notify
 *  100 slc_terminate
 *  100 slc_unsubscribe
 *  requests 
 */
export default function() {

    const smf_host = `smf${__VU}.ericsson.se`;
    const supi = `nai-${__VU}${__ITER}@ericsson.com`;
    const csa = `https://${__ENV.SCP_HOST}:${__ENV.SCP_PORT}/`;

    let ids = subscribe_operation(smf_host, supi, csa);
    if (ids.length > 0) {
    	modify_operation(ids, smf_host, supi, csa);
        notify_operation(csa);
        terminate_operation(csa);
    	unsubscribe_operation(ids, smf_host, supi, csa);
    }
}

//spending limit control subscribe operation
function subscribe_operation(smf_host, supi, csa) {

const slc_subscribe = `${__ENV.SLC_SUBSCRIBE}`;

if(slc_subscribe>0){
	
    const method = "POST";
   
    let requests = [[]];

    const body = JSON.stringify({
        "supi":"imsi-310310140000120",
        "gpsi": "msisdn-12001400120",
        "policyCounterIds" : ["71008", "71009", "71300", "71000"],
	"notifyUri": "https://" + smf_host + ":8080/rar"
    }, null, "   ");

    
    for(let i=0;i<slc_subscribe;i++) {
        requests[i] = [method, csa + uri, body, headers];
    }

    //console.log("Sending x SLC-Subscribe")

    let responses = http.batch(requests);

    let resp_length = Object.keys(responses).length;
    for(let i=0;i<resp_length;i++)
    {
       check(responses[i], {
         "slc subscribe: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
         "slc subscribe: status was 201": (r) => r.status == 201
       });
    }

    let ids = process_response_headers(responses);
    return ids;
}
return 0;
}

function process_response_headers(responses) {

   let resp_length = Object.keys(responses).length;

   let ids = [];
   for (let i=0;i<resp_length;i++) {
      let location = responses[i].headers["Location"];
      if (location != null && location.includes("occ")) {
         let array = location.split("/");
         ids[i] = array[array.length - 1];
      }
   }
   return ids;
}


//spending limit control modify operation
function modify_operation(ids, smf_host, supi, csa) {

const slc_modify = `${__ENV.SLC_MODIFY}`;

if(slc_modify>0){
	
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

    let requests = [[]];
    let counter = 0;
    
    for(let i=0;i<slc_modify;i++) {
    	if(counter>=ids.length){
    		counter=0;
    	}
        requests[i] = [method, csa + uri + ids[counter], body, headers];
        counter++;
    }

    //console.log("Sending x SLC-Modify")

    let responses = http.batch(requests);

    let resp_length = Object.keys(responses).length;    
    for(let i=0;i<resp_length;i++)
    {
       check(responses[i], {
         "slc modify: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
         "slc modify: status was 200": (r) => r.status == 200
       });
    }
  }
}


//spending limit control unsubscribe operation
function unsubscribe_operation(ids, smf_host, supi, csa) {
	
const slc_unsubscribe = `${__ENV.SLC_UNSUBSCRIBE}`;

if(slc_unsubscribe>0){
	
    const method = "DELETE";

    let requests = [[]];
    
    for(let i=0;i<slc_unsubscribe;i++) {
        requests[i] = [method, csa + uri + ids[i], null, headers];
    }

    //console.log("Sending 100 x SLC-Unsubscribe")

    let responses = http.batch(requests);
    
    let resp_length = Object.keys(responses).length;    
    for(let i=0;i<resp_length;i++)
    {
        check(responses[i], {
           "slc unsubscribe: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
           "slc unsubscribe: status was 204": (r) => r.status == 204
        });
    }
  }
}

//spending limit control NOTIFY operation
function notify_operation(csa){

const slc_notify = `${__ENV.SLC_NOTIFY}`;

if(slc_notify>0){
    const method = "POST";
    const uri = "notify"
    const headers_chfsim_1 = {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json",
            "x-notify-uri": "https://eric-chfsim-1/nchf-spendinglimitcontrol/v1/subscriptions/notify",
//          "x-notify-uri": "https://eric-chfsim-1/nchf-convergedcharging/v1/chargingdata/notify_cc_receiver",
            "Host": `${__ENV.SCP_HOST}`+":"+`${__ENV.SCP_PORT}`
        }
    }
	
    const headers_chfsim_2 = {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json",
            "x-notify-uri": "https://eric-chfsim-2/nchf-spendinglimitcontrol/v1/subscriptions/notify",
            "Host": `${__ENV.SCP_HOST}`+":"+`${__ENV.SCP_PORT}`
        }
    }

    const headers_chfsim_3 = {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json",
            "x-notify-uri": "https://eric-chfsim-3/nchf-spendinglimitcontrol/v1/subscriptions/notify",
            "Host": `${__ENV.SCP_HOST}`+":"+`${__ENV.SCP_PORT}`
        }
    }
	
    //console.log("Sending x number of batches SLC-Notify")

    let list_resp = [[]];
    for(let i=0;i<slc_notify;i++) {
        list_resp[i] = [method, csa + uri,null, headers_chfsim_1];
        i++;
        if(i<slc_notify){
        	list_resp[i] = [method, csa + uri,null, headers_chfsim_2];
        }
        i++;
        if(i<slc_notify){
        	list_resp[i] = [method, csa + uri,null, headers_chfsim_3];
        }
    }
    

    let responses = http.batch(list_resp);
    
    let resp_length = Object.keys(responses).length;    

    for(let i=0;i<resp_length;i++)
    {
        check(responses[i], {
           "slc notify: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
           "slc notify: status was 204": (r) => r.status == 204
        });
    }
  }//end if slc_notify>0
}

//spending limit control TERMINATE operation
function terminate_operation(csa){

const slc_terminate = `${__ENV.SLC_TERMINATE}`;

if(slc_terminate>0){
	
    const method = "POST";
    const uri = "notify"

    const headers_chfsim_1 = {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json",
            "x-notify-uri": "https://eric-chfsim-1/nchf-spendinglimitcontrol/v1/subscriptions/terminate"
//          "x-notify-uri": "https://eric-chfsim-1/nchf-convergedcharging/v1/subscriptions/notify_cc_receiver"    
        }
    }

    const headers_chfsim_2 = {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json",
            "x-notify-uri": "https://eric-chfsim-2/nchf-spendinglimitcontrol/v1/subscriptions/terminate"
        }
    }

    const headers_chfsim_3 = {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json",
            "x-notify-uri": "https://eric-chfsim-3/nchf-spendinglimitcontrol/v1/subscriptions/terminate"
        }
    }
	
    let list_resp = [[]];
    for(let i=0;i<slc_terminate;i++) {
        list_resp[i] = [method, csa + uri, null, headers_chfsim_1];
        i++;
        if(i<slc_terminate){
        	list_resp[i] = [method, csa + uri, null, headers_chfsim_2];
        }
        i++;
        if(i<slc_terminate){
        	list_resp[i] = [method, csa + uri, null, headers_chfsim_3];
        }
    }

    let responses = http.batch(list_resp);
    
    let resp_length = Object.keys(responses).length;    

    for(let i=0;i<resp_length;i++)
    {
        check(responses[i], {
           "slc terminate: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
           "slc terminate: status was 204": (r) => r.status == 204
        });
    }
   }//end if slc_terminate>0
}

