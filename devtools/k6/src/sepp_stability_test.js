/**
 * @author xkonska, eedcsi, eyossan
 */

import http from "k6/http";
import {
    check
} from "k6";
import { Counter } from 'k6/metrics';


var sepp_host = `${__ENV.SEPP_HOST}`;
var sepp = `${__ENV.SEPP_HOST}:${__ENV.SEPP_PORT}/`;
var sepp_ip = `${__ENV.SEPP_IP}`;
var encoded_host = `${__ENV.ENCODED_HOST}`;
var encoded_sepp_host = `${__ENV.ENCODED_HOST}.${__ENV.SEPP_HOST}`;
var hostsObject = {};
var duration = `${__ENV.DURATION}`;
var apiPrefixSR = "";
var apiPrefixPR = "";

let timeUnit = `${__ENV.TIMEUNIT}`;
let rate = parseInt(`${__ENV.RATE}`);
let preAllocatedVus = 5;
let maxVus = 20;
let nrfTrafficNotify = `${__ENV.NRFTRAFFICNOTIFY}`;
let nrfTrafficDisc = `${__ENV.NRFTRAFFICDISC}`;
let nrf_port = `${__ENV.PORT_TH_DISC}`;

const counternfUdm13_2xx = new Counter('nfUdm13_successful_responses_with_scheme_http');
const counternfUdm14_2xx = new Counter('nfUdm14_successful_responses_with_scheme_https');

hostsObject[sepp_host] = sepp_ip;

if (encoded_host && encoded_sepp_host !== "null") {
    hostsObject[encoded_sepp_host] = sepp_ip;
}

if(`${__ENV.TARGET_HOST_1}` === "nfUdm12") {
    apiPrefixSR = "/a/b/c";
}

if(`${__ENV.TARGET_HOST_2}` === "nfUdm12") {
    apiPrefixPR = "/a/b/c";
}

export let options = {
    scenarios: {
        load_test_scp: {
            executor: 'constant-arrival-rate',
            timeUnit: timeUnit,
            rate: rate,
            duration: duration,
            preAllocatedVUs: preAllocatedVus, // how large the initial pool of VUs would be
            maxVUs: maxVus, // if the preAllocatedVUs are not enough, we can initialize more
            //exec: 'scp_function', // same function as the scenario above, but with different env vars
        },
    },
    hosts: hostsObject,
    tlsAuth: [{
        domains: [`${__ENV.SEPP_HOST}`],
        cert: __ENV.CERT,
        key: __ENV.KEY
    }],
    thresholds: { nfUdm13_successful_responses_with_scheme_http: ['count>0'], nfUdm14_successful_responses_with_scheme_https: ['count>0'] }
    //vus: 1,
    //duration: duration
};

const nfPeerInfoEnabled = `${__ENV.NF_PEER_INFO_ENABLED}`;

const uri_n8 = "nudm-sdm/v2/imsi-460001357924675/nssai";
const uri_n27 = "nnrf-disc/v1/nf-instances?requester-nf-type=AMF&target-nf-type=UDM&supi=imsi-460001357924675";
const uri_nudm_sdm = "nudm-sdm/v2/shared-data-subscriptions";

const fireWallEnabled = `${__ENV.FIREWALL_ENABLED}`;

//const uri_n27_target_amf = "nnrf-disc/v1/nf-instances?requester-nf-type=AMF&target-nf-type=AMF";


const headers_sr = {
    headers: {
        "Connection": "keep-alive",
        "Content-Type": "application/json",
        "Accept": "application/json",
        "3gpp-Sbi-target-apiRoot": `${__ENV.SCHEME}://${__ENV.TARGET_HOST_1}.${__ENV.TARGET_DOMAIN_1}:${__ENV.TARGET_NF_PORT_1}` + apiPrefixSR
    }
};

const headers_sr_dynfwd = {
    headers: {
        "Connection": "keep-alive",
        "Content-Type": "application/json",
        "Accept": "application/json",
        "3gpp-Sbi-target-apiRoot": `http://${__ENV.TARGET_HOST_3}.${__ENV.TARGET_DOMAIN_3}:${__ENV.TARGET_NF_PORT_3}/notify`,
        "3gpp-Sbi-Callback": "notification"
    }
};

const headers_pr = {
    headers: {
        "Connection": "keep-alive",
        "Content-Type": "application/json",
        "Accept": "application/json",
        "3gpp-Sbi-target-apiRoot": `${__ENV.SCHEME}://${__ENV.TARGET_HOST_2}.${__ENV.TARGET_DOMAIN_2}:${__ENV.TARGET_NF_PORT_2}` + apiPrefixPR
    }
};

const headers_rr = {
    headers: {
        "Connection": "keep-alive",
        "Content-Type": "application/json",
        "Accept": "application/json",
        "3gpp-Sbi-Discovery-nf-set-id": `${__ENV.NF_SET_ID}`
    }
};

const headers_rr_th = {
    headers: {
        "Connection": "keep-alive",
        "Content-Type": "application/json",
        "Accept": "application/json",
        "3gpp-Sbi-target-apiRoot": `${__ENV.SCHEME}://${__ENV.TARGET_HOST_TH}.${__ENV.TARGET_DOMAIN_TH}:${__ENV.TARGET_NF_PORT_TH}`
    }
};
const headers_nrf_th_disc = {
    headers: {
        "Connection": "keep-alive",
        "Content-Type": "application/json",
        "Accept": "application/json",
        "3gpp-Sbi-target-apiRoot": `${__ENV.SCHEME}://${__ENV.HOST_TH_DISC}.${__ENV.DOMAIN_TH_DISC}:${__ENV.PORT_TH_DISC}`
    }
};


const headers_tfqdn = {
    headers: {
        "Connection": "keep-alive",
        "Content-Type": "application/json",
        "Accept": "application/json",
    }
};
const headers_nrf_replace = {
    headers: {
        "Connection": "keep-alive",
        "Content-Type": "application/json",
        "3gpp-Sbi-target-apiRoot": `${__ENV.SCHEME_TH_NOTIFY}://${__ENV.HOST_TH_NOTIFY}.${__ENV.DOMAIN_TH_NOTIFY}:${__ENV.PORT_TH_NOTIFY}`,
        "3gpp-Sbi-Callback": "Nnrf_NFManagement_NFStatusNotify"
    }
};

const headers_inter_udm_sdm_init_nf_filter = {
    headers: {
        "Connection": "keep-alive",
        "Content-Type": "application/json",
        "Accept": "application/json",
        "3gpp-Sbi-Target-apiRoot": `http://sepp8.${__ENV.NAMESPACE}.sepp.5gc.mnc567.mcc765.3gppnetwork.org`
    }
};

//Update headers in case of 3gpp-sbi-nf-peer-info
if (nfPeerInfoEnabled === "true") {

    let sbiNfPeerInfoHeader = `srcinst=2ec8ac0b-265e-4165-86e9-e0735e6ce307;dstinst=2ec8ac0b-265e-4165-86e9-e0735e6ce309;dstsepp=${__ENV.SEPP_HOST}`;

    headers_sr["headers"]["3gpp-sbi-nf-peer-info"] = sbiNfPeerInfoHeader;
    headers_sr_dynfwd["headers"]["3gpp-sbi-nf-peer-info"] = sbiNfPeerInfoHeader;
    headers_pr["headers"]["3gpp-sbi-nf-peer-info"] = sbiNfPeerInfoHeader;
    headers_rr_th["headers"]["3gpp-sbi-nf-peer-info"] = sbiNfPeerInfoHeader;
    headers_rr["headers"]["3gpp-sbi-nf-peer-info"] = sbiNfPeerInfoHeader;
    headers_tfqdn["headers"]["3gpp-sbi-nf-peer-info"] = sbiNfPeerInfoHeader;

}
//Add dummy header in case USFW is configured
if (fireWallEnabled === "true") {
    headers_sr.headers["dummy-firewall-header"] = "send";
}


/** Send batch messages of 
 *  SR x static routing_taffic
 *  PR x preferred routing_taffic
 *  RR x round-robin_routing_taffic 
 *  TFQDN x Telescopic FQDN traffic
 *  requests 
 */
export default function () {

    // const amf_host = `amf${__VU}.${__ENV.OWN_DOMAIN}`;
    const scheme = `https://`;
    const scheme_http = `http://`;
    const sepp = `${__ENV.SEPP_HOST}:${__ENV.SEPP_PORT}/`;
    const sepp_http = `${__ENV.SEPP_HOST}:80/`;
    const encoded_sepp_host = `${__ENV.ENCODED_HOST}.${__ENV.SEPP_HOST}:${__ENV.SEPP_PORT}/`;
    const sepp_th = `${__ENV.SEPP_HOST}:${__ENV.SEPP_PORT_TH_NOTIFY}/`;
    const th = `${__ENV.TH_ENABLED}`;
    const ingressMSHeaders = `${__ENV.MS_INGRESS_HEADERS}`;
    const egressMSHeaders = `${__ENV.MS_EGRESS_HEADERS}`;
    const ingressMSBody = `${__ENV.MS_INGRESS_BODY}`;
    const egressMSBody = `${__ENV.MS_EGRESS_BODY}`;
    const combinedMS = `${__ENV.COMBINED_MS}`;

    send_sr_n8_traffic(scheme, sepp, headers_sr, `${__ENV.SR}`);
    send_sr_dynfwd_n8_traffic(scheme, sepp, headers_sr_dynfwd, `${__ENV.SR}`);
    send_pr_n8_traffic(scheme, sepp, headers_pr, `${__ENV.PR}`);
    send_rr_n8_traffic(scheme, sepp, (th === "false") ? headers_rr : headers_rr_th, `${__ENV.RR}`);
    send_sr_n27_traffic(scheme, sepp);
    send_pr_n27_traffic(scheme, sepp);
    send_rr_n27_traffic(scheme, sepp);
    send_tfqdn_sr_n8_traffic(scheme, encoded_sepp_host);
    send_tfqdn_sr_n27_traffic(scheme, encoded_sepp_host);

    if (ingressMSHeaders > 0) {
        send_ms_n8_traffic(scheme, sepp, "MS_IngressHeaders", ingressMSHeaders, th);
    }

    if (egressMSHeaders > 0) {
        send_ms_n8_traffic(scheme, sepp, "MS_EgressHeaders", egressMSHeaders, th);
    }

    if (ingressMSBody > 0) {
        send_ms_n8_traffic(scheme, sepp, "MS_IngressBody", ingressMSBody, th);
    }

    if (egressMSBody > 0) {
        send_ms_n8_traffic(scheme, sepp, "MS_EgressBody", egressMSBody, th);
    }

    if (combinedMS > 0) {
        send_ms_n8_traffic(scheme, sepp, "MS_Combined", combinedMS, th);
    }

    if (nrfTrafficNotify > 0) {
        send_nrf_notify_traffic(scheme_http, sepp_th, nrfTrafficNotify)
    }
    if (nrfTrafficDisc > 0) {
        send_nrf_disc_traffic(scheme, sepp, nrfTrafficDisc)
    }

    // traffic only form RPs this also runs with DS
    var sepp_ip6 = sepp;
    if (__ENV.SEPP_IP6 !== "null") {
        sepp_ip6 = `[${__ENV.SEPP_IP6}]:${__ENV.SEPP_PORT}/`;
    }
    if ((`${__ENV.TARGET_DOMAIN_1}` === `${__ENV.NAMESPACE}.sepp.5gc.mnc567.mcc765.3gppnetwork.org`) && (`${__ENV.NF_FILTER_ENABLED}` === "true")) {
        nudm_sdm_subscribe_nf_filter(scheme, sepp_ip6, `${__ENV.RR}`);
    }
}


//Function to send traffic per message screening param
function send_ms_n8_traffic(scheme, sepp, msHeader, traffic, th) {
    let headers_ms_sr = headers_sr;
    headers_ms_sr["headers"]["ms-header"] = msHeader;
    headers_ms_sr["headers"]["dummy-header-2"] = "";
    send_sr_n8_traffic(scheme, sepp, headers_ms_sr, traffic / 3);

    let headers_ms_pr = headers_pr;
    headers_ms_pr["headers"]["ms-header"] = msHeader;
    headers_ms_pr["headers"]["dummy-header-2"] = "";
    send_pr_n8_traffic(scheme, sepp, headers_ms_pr, traffic / 3);

    let headers_ms_rr = (th === "false") ? headers_rr : headers_rr_th;
    headers_ms_rr["headers"]["ms-header"] = msHeader;
    headers_ms_rr["headers"]["dummy-header-2"] = "";
    send_rr_n8_traffic(scheme, sepp, headers_ms_rr, traffic / 3);
}

//Function to send strict routing traffic
function send_sr_n8_traffic(scheme, sepp, headers, traffic) {

    if (traffic > 0) {

        const method = "GET";

        let requests = [[]];

        let body = get_create_body();

        //create the batch for traffic
        for (let i = 0; i < traffic; i++) {
            requests[i] = [method, scheme + sepp + uri_n8, body, headers];
        }

        let responses = http.batch(requests);

        let resp_length = Object.keys(responses).length;
        for (let i = 0; i < resp_length; i++) {
            check(responses[i], {
                "SR/N8 traffic: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
                "SR/N8 traffic: status was 200": (r) => r.status == 200
            });
        }

    }
    return 0;
}

//Function to send strict routing traffic after DNS resolution
function send_sr_dynfwd_n8_traffic(scheme, sepp, headers, traffic) {
    var nfudm13 = `${__ENV.TARGET_HOST_3}`;

    if (traffic > 0 && nfudm13 !== "null") {

        const method = "POST";

        let requests = [[]];

        let body = create_body_notify();

        //create the batch for traffic
        for (let i = 0; i < traffic; i++) {
            requests[i] = [method, scheme + sepp + "notify", body, headers];
        }

        let responses = http.batch(requests);

        let resp_length = Object.keys(responses).length;
        for (let i = 0; i < resp_length; i++) {
            check(responses[i], {
                "SR/N8 traffic: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
                "SR/N8 traffic: status was 204": (r) => r.status == 204
            });
        }

    }
    return 0;
}

function send_sr_n27_traffic(scheme, sepp) {

    let sr = `${__ENV.SR}`;

    if (sr > 0) {

        const method = "GET";

        let requests = [[]];

        let body = get_create_body();

        //create the batch for traffic
        for (let i = 0; i < sr; i++) {
            requests[i] = [method, scheme + sepp + uri_n27, body, headers_sr];
        }

        let responses = http.batch(requests);

        let resp_length = Object.keys(responses).length;
        for (let i = 0; i < resp_length; i++) {
            check(responses[i], {
                "SR/N27 traffic: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
                "SR/N27 traffic: status was 200": (r) => r.status == 200
            });
        }

    }
    return 0;
}

//Function to send preferred routing traffic 
function send_pr_n8_traffic(scheme, sepp, headers, traffic) {

    if (traffic > 0) {

        const method = "GET";

        let requests = [[]];

        let body = get_create_body();

        //create the batch for traffic
        for (let i = 0; i < traffic; i++) {
            requests[i] = [method, scheme + sepp + uri_n8, body, headers];
        }

        let responses = http.batch(requests);

        let resp_length = Object.keys(responses).length;
        for (let i = 0; i < resp_length; i++) {
            check(responses[i], {
                "PR/N8 traffic: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
                "PR/N8 traffic: status was 200": (r) => r.status == 200
            });
        }
    }
    return 0;
}

function send_pr_n27_traffic(scheme, sepp) {

    let pr = `${__ENV.PR}`;

    if (pr > 0) {

        const method = "GET";

        let requests = [[]];

        let body = get_create_body();

        //create the batch for traffic
        for (let i = 0; i < pr; i++) {
            requests[i] = [method, scheme + sepp + uri_n27, body, headers_pr];
        }

        let responses = http.batch(requests);

        let resp_length = Object.keys(responses).length;
        for (let i = 0; i < resp_length; i++) {
            check(responses[i], {
                "PR/N27 traffic: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
                "PR/N27 traffic: status was 200": (r) => r.status == 200
            });
        }
    }
    return 0;
}

//Function to send forward traffic 
function send_rr_n8_traffic(scheme, sepp, headers, traffic) {

    if (traffic > 0) {

        const method = "GET";

        let requests = [[]];

        let body = get_create_body();

        //create the batch for traffic
        for (let i = 0; i < traffic; i++) {
            requests[i] = [method, scheme + sepp + uri_n8, body, headers];
        }

        let responses = http.batch(requests);
        let resp_length = Object.keys(responses).length;
        for (let i = 0; i < resp_length; i++) {
            // console.log(responses[i].headers['Location']);
            const regexPatternHttpUdm13 = /^http:\/\/nfUdmTH-RP1/; // This regex pattern matches strings that start with "http://nfUdmTH-RP1 the nfudm13 is hidden behind
            const regexPatternHttpsUdm14 = /^https:\/\/nfUdmTH-RP1/; // This regex pattern matches strings that start with "https://nfUdmTH-RP1 the nfudm14 is hidden behind

            //check if the Location contains scheme http://nfUdmTH-RP1 and the status code is 200
            if (regexPatternHttpUdm13.test(responses[i].headers['Location']) && responses[i].status == 200) {
                // console.log('nfUdm13 with scheme http found and get a successful response');
                counternfUdm13_2xx.add(1);
            }
            //check if the Location contains scheme "https://nfUdmTH-RP1 and the status code is 200
            if (regexPatternHttpsUdm14.test(responses[i].headers['Location']) && responses[i].status == 200) {
                // console.log('nfUdm14 with scheme https found and get a successful response');
                counternfUdm14_2xx.add(1);
            }

            check(responses[i], {
                "RR traffic: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
                "RR traffic: status was 200": (r) => r.status == 200
            });
        }
    }
    return 0;
}

function send_rr_n27_traffic(scheme, sepp) {

    let rr = `${__ENV.RR}`;

    if (rr > 0) {

        const method = "GET";

        let requests = [[]];

        let body = get_create_body();

        //create the batch for traffic
        for (let i = 0; i < rr; i++) {
            requests[i] = [method, scheme + sepp + uri_n27, body, headers_rr];
        }

        let responses = http.batch(requests);

        let resp_length = Object.keys(responses).length;
        for (let i = 0; i < resp_length; i++) {
            check(responses[i], {
                "RR/N27 traffic: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
                "RR/N27 traffic: status was 200": (r) => r.status == 200
            });
        }
    }
    return 0;
}

//Function to send T-FQDN traffic
function send_tfqdn_sr_n8_traffic(scheme, encoded_sepp_host) {

    let tfqdn = `${__ENV.TFQDN}`;

    if (tfqdn > 0 && encoded_host !== "null") {

        const method = "GET";

        let requests = [[]];

        let body = get_create_body();

        //create the batch for traffic
        for (let i = 0; i < tfqdn; i++) {
            requests[i] = [method, scheme + encoded_sepp_host + uri_n8, body, headers_tfqdn];
        }

        let responses = http.batch(requests);

        let resp_length = Object.keys(responses).length;
        for (let i = 0; i < resp_length; i++) {
            check(responses[i], {
                "TFQDN/N8 traffic: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
                "TFQDN/N8 traffic: status was 200": (r) => r.status == 200
            });
        }

    }
    return 0;
}

//Function to send T-FQDN traffic
function send_tfqdn_sr_n27_traffic(scheme, encoded_sepp_host) {

    let tfqdn = `${__ENV.TFQDN}`;

    if (tfqdn > 0 && encoded_host !== "null") {

        const method = "GET";

        let requests = [[]];

        let body = get_create_body();

        //create the batch for traffic
        for (let i = 0; i < tfqdn; i++) {
            requests[i] = [method, scheme + encoded_sepp_host + uri_n27, body, headers_tfqdn];
        }

        let responses = http.batch(requests);

        let resp_length = Object.keys(responses).length;
        for (let i = 0; i < resp_length; i++) {
            check(responses[i], {
                "TFQDN/N27 traffic: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
                "TFQDN/N27 traffic: status was 200": (r) => r.status == 200
            });
        }

    }
    return 0;
}

function send_nrf_notify_traffic(scheme, encoded_sepp_host, traffic) {
    const method = "POST";

    let requests = [[]];

    let body = create_body_nrf_replace();

    //create the batch for traffic
    for (let i = 0; i < traffic; i++) {
        requests[i] = [method, scheme + encoded_sepp_host + "dummy", body, headers_nrf_replace];
    }

    let responses = http.batch(requests);

    let resp_length = Object.keys(responses).length;
    for (let i = 0; i < resp_length; i++) {
        check(responses[i], {
            "NRF NOTIFY traffic: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
            "NRF NOTIFY traffic: status was 204": (r) => r.status == 204
        });
    }
    return 0;
}

function send_nrf_disc_traffic(scheme, encoded_sepp_host, traffic) {
    const method = "GET";

    let requests = [[]];


    //create the batch for traffic                                                                                                         
    for (let i = 0; i < traffic; i += 3) {
        requests[i] = [method, scheme + encoded_sepp_host + 'nnrf-disc/v1/nf-instances?requester-nf-type=UDM&target-nf-type=AMF&target-nf-fqdn=AID01KOY4USZSPV4JXONN.5gc.mnc151.mcc115.3gppnetwork.org', undefined, headers_nrf_th_disc];
        requests[i + 1] = [method, scheme + encoded_sepp_host + 'nnrf-disc/v1/searches/-1', undefined, headers_nrf_th_disc];
        requests[i + 2] = [method, scheme + encoded_sepp_host + 'nnrf-disc/v1/searches/-1/complete', undefined, headers_nrf_th_disc];
    }
    let responses = http.batch(requests);

    let resp_length = Object.keys(responses).length;
    for (let i = 0; i < resp_length; i++) {
        check(responses[i], {
            "NRF DISCOVERY traffic: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
            "NRF DISCOVERY traffic: status was 200": (r) => r.status == 200
        });
    }
    return 0;
}

function nudm_sdm_subscribe_nf_filter(scheme, sepp, traffic) {

    if (traffic > 0) {
        //replace header
        const p_index = Math.floor(Math.random() * 4) + 5;

        headers_inter_udm_sdm_init_nf_filter.headers["3gpp-Sbi-Target-apiRoot"] = "http://sepp" + p_index + `.${__ENV.NAMESPACE}.sepp.5gc.mnc567.mcc765.3gppnetwork.org`;
        console.log("updated header: " + headers_inter_udm_sdm_init_nf_filter.headers["3gpp-Sbi-Target-apiRoot"]);
 
        const method = "POST";
 
        let requests = [[]];
 
        let body = get_create_body();
 
        //create the batch for traffic
        for (let i = 0; i < traffic; i++) {
            requests[i] = [method, scheme + sepp + uri_nudm_sdm, body, headers_inter_udm_sdm_init_nf_filter];
        }
 
        let responses = http.batch(requests);
 
        let resp_length = Object.keys(responses).length;
        for (let i = 0; i < resp_length; i++) {
            check(responses[i], {
                "NUDM-SDM (nf-filter) Subscribe traffic: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
                "NUDM-SDM (nf-filter) Subscribe traffic: status was 201": (r) => r.status == 201
            });
        }
    }
    return 0;
}

function create_body_nrf_replace() {
    return JSON.stringify({
        "event": "NF_PROFILE_CHANGED",
        "nfInstanceUri": "nfInstanceUri_1",
        "profileChanges": [{
            "op": "REPLACE",
            "path": "/nfProfile/ipv4Addresses",
            "origValue": ["10.0.0.1", "10.0.0.2"],
            "newValue": ["128.0.0.1", "20.0.0.2"]
        }, {
            "op": "REPLACE",
            "path": "/nfProfile/ipv4Addresses/0",
            "origValue": "10.0.0.1",
            "newValue": "128.0.0.1"
        }, {
            "op": "REPLACE",
            "path": "/nfProfile/ipv6Addresses",
            "origValue": ["1000:aaaa:aaaa:aaaa:aaaa:aaaa:aaaa:0001", "1000:aaaa:aaaa:aaaa:aaaa:aaaa:aaaa:0002"],
            "newValue": ["2000:aaaa:aaaa:aaaa:aaaa:aaaa:aaaa:0001", "2000:aaaa:aaaa:aaaa:aaaa:aaaa:aaaa:0002"]
        }, {
            "op": "REPLACE",
            "path": "/nfProfile/ipv6Addresses/0",
            "origValue": "1000:aaaa:aaaa:aaaa:aaaa:aaaa:aaaa:0001",
            "newValue": "2000:aaaa:aaaa:aaaa:aaaa:aaaa:aaaa:0001"
        }]
    });
}

function get_create_body() {
    return JSON.stringify({
        "subscriberIdentifier": "imsi-460030700000001",
        "nfConsumerIdentification": {
            "nFName": "123e-e8b-1d3-a46-421",
            "nFIPv4Address": "192.168.0.1",
            "nFIPv6Address": "2001:db8:85a3:8d3:1319:8a2e:370:7348",
            "nFPLMNID": {
                "mcc": "311",
                "mnc": "280"
            },
            "nodeFunctionality": "SMF"
        },
        "invocationTimeStamp": "2019-03-28T14:30:50Z",
        "invocationSequenceNumber": 0,
        //"notifyUri":"http://${OCC_IP}:8080/rar",
        //"notifyUri": "http://" + amf_host + ":8080/rar",
        "multipleUnitUsage": [{
            "ratingGroup": 100,
            "requestedUnit": {
                "time": 123,
                "totalVolume": 211,
                "uplinkVolume": 123,
                "downlinkVolume": 1234,
                "serviceSpecificUnits": 6543
            },
            "uPFID": "123e-e8b-1d3-a46-421"
        }],
        "pDUSessionChargingInformation": {
            "chargingId": 123,
            "userInformation": {
                "servedGPSI": "msisdn-77117777",
                "servedPEI": "imei-234567891098765",
                "unauthenticatedFlag": true,
                "roamerInOut": "OUT_BOUND"
            },
            "userLocationinfo": {
                "eutraLocation": {
                    "tai": {
                        "plmnId": {
                            "mcc": "374",
                            "mnc": "645"
                        },
                        "tac": "ab01"
                    },
                    "ecgi": {
                        "plmnId": {
                            "mcc": "374",
                            "mnc": "645"
                        },
                        "eutraCellId": "abcAB12"
                    },
                    "ageOfLocationInformation": 32766,
                    "ueLocationTimestamp": "2019-03-28T14:30:50Z",
                    "geographicalInformation": "234556ABCDEF2345",
                    "geodeticInformation": "ABCDEFAB123456789023",
                    "globalNgenbId": {
                        "plmnId": {
                            "mcc": "374",
                            "mnc": "645"
                        },
                        "n3IwfId": "ABCD123",
                        "ngRanNodeId": "MacroNGeNB-abc92"
                    }
                },
                "nrLocation": {
                    "tai": {
                        "plmnId": {
                            "mcc": "374",
                            "mnc": "645"
                        },
                        "tac": "ab01"
                    },
                    "ncgi": {
                        "plmnId": {
                            "mcc": "374",
                            "mnc": "645"
                        },
                        "nrCellId": "ABCabc123"
                    },
                    "ageOfLocationInformation": 1,
                    "ueLocationTimestamp": "2019-03-28T14:30:50Z",
                    "geographicalInformation": "AB12334765498F12",
                    "geodeticInformation": "AB12334765498F12ACBF",
                    "globalGnbId": {
                        "plmnId": {
                            "mcc": "374",
                            "mnc": "645"
                        },
                        "n3IwfId": "ABCD123",
                        "ngRanNodeId": "MacroNGeNB-abc92"
                    }
                },
                "n3gaLocation": {
                    "n3gppTai": {
                        "plmnId": {
                            "mcc": "374",
                            "mnc": "645"
                        },
                        "tac": "ab01"
                    },
                    "n3IwfId": "ABCD123",
                    "ueIpv4Addr": "192.168.0.1",
                    "ueIpv6Addr": "2001:db8:85a3:8d3:1319:8a2e:370:7348",
                    "portNumber": 1
                }
            },
            "userLocationTime": "2019-03-28T14:30:50Z",
            "uetimeZone": "+05:30",
            "pduSessionInformation": {
                "networkSlicingInfo": {
                    "sNSSAI": {
                        "sst": 0,
                        "sd": "Aaa123"
                    }
                },
                "pduSessionID": 1,
                "pduType": "IPV4",
                "sscMode": "SSC_MODE_1",
                "hPlmnId": {
                    "mcc": "374",
                    "mnc": "645"
                },
                "servingNodeID": [{
                    "plmnId": {
                        "mcc": "311",
                        "mnc": "280"
                    },
                    "amfId": "ABab09"
                }],
                "servingNetworkFunctionID": {
                    "servingNetworkFunctionName": "SMF",
                    "servingNetworkFunctionInstanceid": "SMF_Instanceid_1",
                    "gUAMI": {
                        "plmnId": {
                            "mcc": "311",
                            "mnc": "280"
                        },
                        "amfId": "ABab09"
                    }
                },
                "ratType": "EUTRA",
                "dnnId": "DN-AAA",
                "chargingCharacteristics": "AB",
                "chargingCharacteristicsSelectionMode": "HOME_DEFAULT",
                "startTime": "2019-03-28T14:30:50Z",
                "3gppPSDataOffStatus": "ACTIVE",
                "pduAddress": {
                    "pduIPv4Address": "192.168.0.1",
                    "pduIPv6Address": "2001:db8:85a3:8d3:1319:8a2e:370:7348",
                    "pduAddressprefixlength": 0,
                    "IPv4dynamicAddressFlag": true,
                    "IPv6dynamicAddressFlag": true
                },
                "qoSInformation": {
                    "5qi": 254
                },
                "servingCNPlmnId": {
                    "mcc": "311",
                    "mnc": "280"
                }
            },
            "unitCountInactivityTimer": 125
        }
    }, null, "   ");
}

function create_body_notify() {
    return JSON.stringify({
        "notifyItems": []
    });
}
