/**
 * @author eedcsi
 * -e CERT=RP_206033.cert -e KEY=RP_206033.key -e RPS=200 -e DURATION=10 -e MAX_VUS=40 -e SEPP_IP=$NODE_IP -e SEPP_HOST="sepp1.region1.sepp.5gc.mnc073.mcc262.3gppnetwork.org" -e SEPP_PORT=$SEPP_TLS_PORT -e TAR="http://nfudm1.region1.udm.5gc.mnc073.mcc262.3gppnetwork.org"
 */

import http from "k6/http";
import {
    check
} from "k6";

let certPath = "/certs/K6.crt";
let keyPath = "/certs/K6.key";
let cert = `${__ENV.CERT}`;
let key = `${__ENV.KEY}`;

if(cert && cert !== "null" && key && key !== "null"){
    certPath = `/certs/${cert}`;
    keyPath = `/certs/${key}`;
}

// each iteration has many requests
// let st = parseInt(`${__ENV.ST}`);
// let tt = parseInt(`${__ENV.TT}`);
// let fw = parseInt(`${__ENV.FW}`);
// let total = st+tt+fw;
let total = 10;

let floatIterations = parseInt(`${__ENV.RPS}`)/ total;
let iterations = Math.ceil(floatIterations);  // round up to the next integer
let duration = `${__ENV.DURATION}`;
let maxVirtualUsers = parseInt(`${__ENV.MAX_VUS}`);
let diff = floatIterations/iterations;

//console.log("RPS:");
//console.log(`${__ENV.RPS}`);
//console.log("iterations:");
//console.log(iterations);

export let options = {
    scenarios: {
        load_test: {
            executor: 'constant-arrival-rate',
            timeUnit: '1s',
            rate: iterations,
            duration: duration,
            preAllocatedVUs: 10, // how large the initial pool of VUs would be
            maxVUs: maxVirtualUsers, // if the preAllocatedVUs are not enough, we can initialize more

            exec: 'default', // same function as the scenario above, but with different env vars
        },
    },
    hosts: {
        "sepp1.region1.sepp.5gc.mnc073.mcc262.3gppnetwork.org":`${__ENV.SEPP_IP}`
    },
    tlsAuth: [{
        domains: [`${__ENV.SEPP_HOST}`],
        cert: open(certPath),
        key: open(keyPath)
    }],
};

// # sent from RP_BE  (SAN: *.region1.amf.5gc.mnc033.mcc206.3gppnetwork.org Domain-name: *.region1.amf.5gc.mnc033.mcc206.3gppnetwork.org)
// curl -v --http2-prior-knowledge  -X PUT --resolve "sepp1.region1.sepp.5gc.mnc073.mcc262.3gppnetwork.org:$SEPP_TLS_PORT:$NODE_IP"  \
// "https://sepp1.region1.sepp.5gc.mnc073.mcc262.3gppnetwork.org:$SEPP_TLS_PORT/nudm-uecm/v1/imsi-2620730007487/registrations/amf-3gpp-access" \
// -H "3gpp-Sbi-target-apiRoot:http://nfudm1.region1.udm.5gc.mnc073.mcc262.3gppnetwork.org" -H "Content-Type: application/json" \
// -d '{"amfInstanceId": "fde21b56-2e47-49dd-9a1f-2769e5a8f45d", "deregCallbackUri": "http://nfamf.region1.amf.5gc.mnc033.mcc206.3gppnetwork.org", \
// "guami":{"plmnId":{"mcc":"206","mnc":"033"},"amfId":"Fc4E30"}, "ratType":"NR"}' -k --cert ~/demo/sepp_demo/RP_206033.crt --key ~/demo/sepp_demo/RP_206033.key

const uri_n8 = "/nudm-uecm/v1/imsi-2620730007487/registrations/amf-3gpp-access";
const rp_traffic = total;

const headers_sr1 = {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json",
            "3gpp-Sbi-target-apiRoot": "https://nfudm1.region1.udm.5gc.mnc073.mcc262.3gppnetwork.org:30926"
        }
    };

const headers_sr2 = {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json",
            "3gpp-Sbi-target-apiRoot": "http://nfudm2.region1.udm.5gc.mnc073.mcc262.3gppnetwork.org:31854"
        }
    };

const headers_sr3 = {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json",
            "3gpp-Sbi-target-apiRoot": "https://nfamf.region1.amf.5gc.mnc073.mcc262.3gppnetwork.org:32620"
        }
    };




/** Send batch messages of
 *  'total'
 *  requests
 */
export default function() {

    //const smf_host = `smf${__VU}.ericsson.se`;
    const sepp = `https://${__ENV.SEPP_HOST}:${__ENV.SEPP_PORT}/`;
    let body = get_create_body();

    if(cert && cert !== "null" && key && key !== "null"){
        send_rp_traffic(sepp, headers_sr1, uri_n8, body);
        send_rp_traffic(sepp, headers_sr2, uri_n8, body);
        send_rp_traffic(sepp, headers_sr3, uri_n8, body);
    }
}

//Function to send unique handling per rp traffic
function send_rp_traffic(sepp, headers, uri, body) {

    const method = "PUT";

    let requests = [[]];

    //let body = get_create_body();

    //create the batch for traffic
    for(let i=0;i<rp_traffic;i++) {
        requests[i] = [method, sepp + uri, body, headers];
    }

    let responses = http.batch(requests);
    let resp_length = Object.keys(responses).length;

    let trafficType = "";
    if (uri.includes("nudm-uecm")) {
        trafficType = "N8";
    }
    else if (uri.includes("nnrf-disc")) {
        trafficType = "N27";
    }

    let msg1 = "RP/" + trafficType + " traffic: protocol is HTTP/2";
    let msg2 = "RP/" + trafficType + " traffic: status was 201";

    for(let i=0;i<resp_length;i++)
    {
        check(responses[i], {
            [msg1]: (r) => r.proto === 'HTTP/2.0',
            [msg2]: (r) => r.status == 201
        });
    }

    return 0;
}


function get_create_body() {
    return JSON.stringify({
        "amfInstanceId": "fde21b56-2e47-49dd-9a1f-2769e5a8f45d",
                "deregCallbackUri": "http://nfamf.region1.amf.5gc.mnc033.mcc206.3gppnetwork.org",
                "guami":{
                    "plmnId":{
                            "mcc":"206",
                                "mnc":"033"
                    },
                        "amfId":"Fc4E30"
                },
                "ratType":"NR"
        }, null, "   ");
}
