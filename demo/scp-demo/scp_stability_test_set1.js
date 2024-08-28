/**
 * @author xkonska, eedcsi, eyossan
 */

import http from "k6/http";
import {
    check
} from "k6";


const method_n8 = "PUT";
const uri_n8 = "/nudm-uecm/v1/imsi-2089300007487/registrations/amf-3gpp-access";

const headers_rr = {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json",
            "3gpp-Sbi-Discovery-nf-set-id": `${__ENV.SET}`
        }
    };
	

let rps = parseInt(`${__ENV.RPS}`)/100;
let holdduration = `${__ENV.HOLDDURATION}`;
let rampduration = `${__ENV.RAMPDURATION}`;

export let options = {
    scenarios: {
        load_test: {
            executor: 'ramping-arrival-rate',
            startTime: '0s', // the ramping API test starts directly
            startRate: 0,    // we start at 1 iterations per second = 0 RPS 
            timeUnit: '1s', 
            stages: [
                { target: rps, duration: rampduration  }, // go from 0 RPS to wanted RPS value
                { target: rps, duration: holdduration }, // hold at set RPS
            ],
            preAllocatedVUs: 10, // how large the initial pool of VUs would be
            maxVUs: 400, // if the preAllocatedVUs are not enough, we can initialize more

            exec: 'default', // same function as the scenario above, but with different env vars
        },
    },  
    //thresholds: {
    //    // the rate of successful checks should be higher than 90%
    //    checks: ['rate>0.99'],
    //  dropped_iterations: ['count < 10'],
    //  //dropped_iterations: [{ threshold: 'count < 10', abortOnFail: true, delayAbortEval: '10s' }]
    //},
    hosts: {
        "scp1.region1.scp.5gc.mnc073.mcc262.3gppnetwork.org":`${__ENV.SCP_IP}`
    },
    tlsAuth: [{
        domains: [`${__ENV.SCP_HOST}`],
        cert: open("/certs/K6.crt"),
        key: open("/certs/K6.key")
    }]
};

/** Send batch messages of 
 *  RR x round_robin_taffic 
 *  requests 
 */
export default function() {

    const SCP = `https://scp1.region1.scp.5gc.mnc073.mcc262.3gppnetwork.org:${__ENV.SCP_PORT}`;

    send_rr_traffic(SCP, headers_rr, uri_n8, method_n8, 100);

}

//Function to send forward traffic 
function send_rr_traffic(SCP, headers, uri, method, amount) {

    if(amount>0){
        
        let rr_requests = [[]];
     
        let body = get_create_body();
        
        //create the batch for traffic
        for(let i=0;i<amount;i++) {
            rr_requests[i] = [method, SCP + uri, body, headers];
        }
    
        let rr_responses = http.batch(rr_requests);
    
        let resp_length = Object.keys(rr_responses).length;
        for(let i=0;i<resp_length;i++)
        {
            check(rr_responses[i], {
                "FW traffic: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
                "FW traffic: status was 201": (r) => r.status == 201
            });	
        }
    }
    return 0;
}


function get_create_body() { 
    return JSON.stringify(
	{
		"amfInstanceId": "fde21b56-2e47-49dd-9a1f-2769e5a8f45d",
		"deregCallbackUri": "http://nfAmf.region1.amf.5gc.mnc073.mcc262.3gppnetwork.org",
		"guami": {
			"plmnId": {
				"mcc": "262",
				"mnc": "073"
			},
			"amfId": "Fc4E30"
		},
		"ratType": "NR"
	}
	, null, "   ");
}
