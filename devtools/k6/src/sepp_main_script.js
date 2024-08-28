/**
 * @author zonoktk
 */

import http from "k6/http";
import {
    check
} from "k6";


var duration = `${__ENV.DURATION}`;

let timeUnit =`${__ENV.TIMEUNIT}`;
let rate = parseInt(`${__ENV.RATE}`);
let preAllocatedVus = 5;
let maxVus = 20;


var sepp_host_from_rp1 = `${__ENV.RP1_SEPP_HOST}`;
var sepp_ip_from_rp1 = `${__ENV.RP1_SEPP_IP}`;
var encoded_host_from_rp1 = `${__ENV.RP1_ENCODED_HOST}`;
var encoded_sepp_host_from_rp1 = `${__ENV.RP1_ENCODED_HOST}.${__ENV.RP1_SEPP_HOST}`;

var sepp_host_from_rp2 = `${__ENV.RP2_SEPP_HOST}`;
var sepp_ip_from_rp2 = `${__ENV.RP2_SEPP_IP}`;
var encoded_host_from_rp2 = `${__ENV.RP2_ENCODED_HOST}`;
var encoded_sepp_host_from_rp2 = `${__ENV.RP2_ENCODED_HOST}.${__ENV.RP2_SEPP_HOST}`;

var sepp_host_from_own_to_rp1 = `${__ENV.O1_SEPP_HOST}`;
var sepp_ip_from_own_to_rp1 = `${__ENV.O1_SEPP_IP}`;
var encoded_host_from_own_to_rp1 = `${__ENV.O1_ENCODED_HOST}`;
var encoded_sepp_host_from_own_to_rp1 = `${__ENV.O1_ENCODED_HOST}.${__ENV.O1_SEPP_HOST}`;

var sepp_host_from_own_to_rp2 = `${__ENV.O2_SEPP_HOST}`;
var sepp_ip_from_own_to_rp2 = `${__ENV.O2_SEPP_IP}`;
var encoded_host_from_own_to_rp2 = `${__ENV.O2_ENCODED_HOST}`;
var encoded_sepp_host_from_own_to_rp2 = `${__ENV.O2_ENCODED_HOST}.${__ENV.O2_SEPP_HOST}`;


var hostsObject = {};

hostsObject[sepp_host_from_rp1]= sepp_ip_from_rp1;

if(encoded_host_from_rp1 && encoded_sepp_host_from_rp1 !== "null") {
    hostsObject[encoded_sepp_host_from_rp1] = sepp_ip_from_rp1;
}

hostsObject[sepp_host_from_rp2]= sepp_ip_from_rp2;

if(encoded_host_from_rp2 && encoded_sepp_host_from_rp2 !== "null") {
    hostsObject[encoded_sepp_host_from_rp2] = sepp_ip_from_rp2;
}

hostsObject[sepp_host_from_own_to_rp1]= sepp_ip_from_own_to_rp1;

if(encoded_host_from_own_to_rp1 && encoded_sepp_host_from_own_to_rp1 !== "null") {
    hostsObject[encoded_sepp_host_from_own_to_rp1] = sepp_ip_from_own_to_rp1;
}

hostsObject[sepp_host_from_own_to_rp2]= sepp_ip_from_own_to_rp2;

if(encoded_host_from_own_to_rp2 && encoded_sepp_host_from_own_to_rp2 !== "null") {
    hostsObject[encoded_sepp_host_from_own_to_rp2] = sepp_ip_from_own_to_rp2;
}

var k6Rp1Cert = open("/certs/k6Rp1Cert.txt");
var k6Rp1Key = open("/certs/k6Rp1Key.txt");

var k6Rp2Cert = open("/certs/k6Rp2Cert.txt");
var k6Rp2Key = open("/certs/k6Rp2Key.txt");

var SeppWrkIntCert = open("/certs/SeppWrkIntCert.txt");
var seppWrkIntKey = open("/certs/seppWrkIntKey.txt");



export let options = {
    scenarios: {
        load_test: {
            executor: 'constant-arrival-rate',
            timeUnit: timeUnit, 
            rate: rate,
            duration: duration,
            preAllocatedVUs: preAllocatedVus, // how large the initial pool of VUs would be
            maxVUs: maxVus, // if the preAllocatedVUs are not enough, we can initialize more
        },
    },
    hosts:hostsObject,
    tlsAuth: [{
        domains: [sepp_host_from_own_to_rp1, sepp_host_from_own_to_rp2],
        cert: SeppWrkIntCert,
        key: seppWrkIntKey
    },{
        domains: [sepp_host_from_rp1],
        cert: k6Rp1Cert,
        key: k6Rp1Key
    },{
        domains: [sepp_host_from_rp2],
        cert: k6Rp2Cert,
        key: k6Rp2Key
    }
    ],
    
    //vus: 1,
   //duration: duration
};

const uri_n8 = "nudm-sdm/v2/imsi-460001357924675/nssai";
const uri_n27 = "nnrf-disc/v1/nf-instances?requester-nf-type=AMF&target-nf-type=UDM&supi=imsi-460001357924675";


    	
/** Send batch messages of 
 *  SR x static routing_taffic
 *  PR x preferred routing_taffic
 *  RR x round-robin_routing_taffic 
 *  TFQDN x Telescopic FQDN traffic
 *  requests 
 */
export default function() {
	sepp_stability_from_rp1_to_own();
	sepp_stability_from_rp2_to_own();
	sepp_stability_from_own_to_rp1();
    sepp_stability_from_own_to_rp2();
}


function sepp_stability_from_rp1_to_own(){
	const headers_sr = {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json",
            "3gpp-Sbi-target-apiRoot": `${__ENV.RP1_SCHEME}://${__ENV.RP1_TARGET_HOST_1}.${__ENV.RP1_TARGET_DOMAIN_1}:${__ENV.RP1_TARGET_NF_PORT_1}`			
        }
    };

	const headers_pr = {
	        headers: {
	            "Connection": "keep-alive",
	            "Content-Type": "application/json",
	            "Accept": "application/json",
	            "3gpp-Sbi-target-apiRoot": `${__ENV.RP1_SCHEME}://${__ENV.RP1_TARGET_HOST_2}.${__ENV.RP1_TARGET_DOMAIN_2}:${__ENV.RP1_TARGET_NF_PORT_2}`			
	        }
	    };
	
	const headers_rr = {
	        headers: {
	            "Connection": "keep-alive",
	            "Content-Type": "application/json",
	            "Accept": "application/json",
	            "3gpp-Sbi-Discovery-nf-set-id": `${__ENV.RP1_NF_SET_ID}`
	        }
	    };
	
	const headers_rr_th = {
	        headers: {                                                                                                                          
	            "Connection": "keep-alive",                                                                                                     
	            "Content-Type": "application/json",                                                                                             
	            "Accept": "application/json",                                                                                                  
	            "3gpp-Sbi-target-apiRoot": `${__ENV.RP1_SCHEME}://${__ENV.RP1_TARGET_HOST_TH}.${__ENV.RP1_TARGET_DOMAIN_TH}:${__ENV.RP1_TARGET_NF_PORT_TH}`                                  
	        }                                                                                                                                   
	    }; 
	
	const headers_tfqdn = {
	        headers: {
	            "Connection": "keep-alive",
	            "Content-Type": "application/json",
	            "Accept": "application/json",
	        }
	    };

    // const amf_host = `amf${__VU}.${__ENV.RP1_OWN_DOMAIN}`;
    const scheme = `https://`;
    const sepp = `${__ENV.RP1_SEPP_HOST}:${__ENV.RP1_SEPP_PORT}/`;
    const encoded_sepp_host = `${__ENV.RP1_ENCODED_HOST}.${__ENV.RP1_SEPP_HOST}:${__ENV.RP1_SEPP_PORT}/`;
    
    const pr = `${__ENV.RP1_PR}`;
    const rr = `${__ENV.RP1_RR}`;
	const sr = `${__ENV.RP1_SR}`;
    const tfqdn = `${__ENV.RP1_TFQDN}`;

    const th = `${__ENV.RP1_TH_ENABLED}`;
    const ingressMSHeaders = `${__ENV.RP1_MS_INGRESS_HEADERS}`;
    const egressMSHeaders = `${__ENV.RP1_MS_EGRESS_HEADERS}`;
    const ingressMSBody = `${__ENV.RP1_MS_INGRESS_BODY}`;
    const egressMSBody = `${__ENV.RP1_MS_EGRESS_BODY}`;
    const combinedMS = `${__ENV.RP1_COMBINED_MS}`;
    
	
    send_sr_n8_traffic(scheme, sepp, headers_sr, sr);
    send_pr_n8_traffic(scheme, sepp, headers_pr, pr);
    send_rr_n8_traffic(scheme, sepp, (th === "false") ? headers_rr : headers_rr_th, rr);
    send_sr_n27_traffic(scheme, sepp, headers_sr, sr);
    send_pr_n27_traffic(scheme, sepp, headers_pr, pr);
    send_rr_n27_traffic(scheme, sepp, headers_rr, rr);
    send_tfqdn_sr_n8_traffic(scheme, encoded_sepp_host, encoded_host_from_rp1, headers_tfqdn, tfqdn);
    send_tfqdn_sr_n27_traffic(scheme, encoded_sepp_host, encoded_host_from_rp1, headers_tfqdn, tfqdn);

    if(ingressMSHeaders>0){
     send_ms_n8_traffic(scheme, sepp, "MS_IngressHeaders", headers_sr, headers_pr, headers_rr, headers_rr_th, ingressMSHeaders, th);
    }

    if(egressMSHeaders>0){
     send_ms_n8_traffic(scheme, sepp, "MS_EgressHeaders", headers_sr, headers_pr, headers_rr, headers_rr_th, egressMSHeaders, th);
    }

    if(ingressMSBody>0){
     send_ms_n8_traffic(scheme, sepp,"MS_IngressBody",  headers_sr, headers_pr, headers_rr, headers_rr_th, ingressMSBody, th);
    }

    if(egressMSBody>0){
     send_ms_n8_traffic(scheme, sepp, "MS_EgressBody",  headers_sr, headers_pr, headers_rr, headers_rr_th, egressMSBody, th);
    }

    if(combinedMS>0){
     send_ms_n8_traffic(scheme, sepp, "MS_Combined", headers_sr, headers_pr, headers_rr, headers_rr_th, combinedMS, th);
    }
}


function sepp_stability_from_rp2_to_own(){
	const headers_sr= {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json",
            "3gpp-Sbi-target-apiRoot": `${__ENV.RP2_SCHEME}://${__ENV.RP2_TARGET_HOST_1}.${__ENV.RP2_TARGET_DOMAIN_1}:${__ENV.RP2_TARGET_NF_PORT_1}`			
        }
    };

	const headers_pr = {
	        headers: {
	            "Connection": "keep-alive",
	            "Content-Type": "application/json",
	            "Accept": "application/json",
	            "3gpp-Sbi-target-apiRoot": `${__ENV.RP2_SCHEME}://${__ENV.RP2_TARGET_HOST_2}.${__ENV.RP2_TARGET_DOMAIN_2}:${__ENV.RP2_TARGET_NF_PORT_2}`			
	        }
	    };
	
	const headers_rr = {
	        headers: {
	            "Connection": "keep-alive",
	            "Content-Type": "application/json",
	            "Accept": "application/json",
	            "3gpp-Sbi-Discovery-nf-set-id": `${__ENV.RP2_NF_SET_ID}`
	        }
	    };
	
	const headers_rr_th = {
	        headers: {                                                                                                                          
	            "Connection": "keep-alive",                                                                                                     
	            "Content-Type": "application/json",                                                                                             
	            "Accept": "application/json",                                                                                                  
	            "3gpp-Sbi-target-apiRoot": `${__ENV.RP2_SCHEME}://${__ENV.RP2_TARGET_HOST_TH}.${__ENV.RP2_TARGET_DOMAIN_TH}:${__ENV.RP2_TARGET_NF_PORT_TH}`                                  
	        }                                                                                                                                   
	    }; 
	
	const headers_tfqdn = {
	        headers: {
	            "Connection": "keep-alive",
	            "Content-Type": "application/json",
	            "Accept": "application/json",
	        }
	    };

    // const amf_host = `amf${__VU}.${__ENV.RP2_OWN_DOMAIN}`;
    const scheme = `https://`;
    const sepp = `${__ENV.RP2_SEPP_HOST}:${__ENV.RP2_SEPP_PORT}/`;
    const encoded_sepp_host = `${__ENV.RP2_ENCODED_HOST}.${__ENV.RP2_SEPP_HOST}:${__ENV.RP2_SEPP_PORT}/`;
    
    const pr = `${__ENV.RP2_PR}`;
    const rr = `${__ENV.RP2_RR}`;
	const sr = `${__ENV.RP2_SR}`;
    const tfqdn = `${__ENV.RP2_TFQDN}`;

    const th = `${__ENV.RP2_TH_ENABLED}`;
    const ingressMSHeaders = `${__ENV.RP2_MS_INGRESS_HEADERS}`;
    const egressMSHeaders = `${__ENV.RP2_MS_EGRESS_HEADERS}`;
    const ingressMSBody = `${__ENV.RP2_MS_INGRESS_BODY}`;
    const egressMSBody = `${__ENV.RP2_MS_EGRESS_BODY}`;
    const combinedMS = `${__ENV.RP2_COMBINED_MS}`;
    
	
    send_sr_n8_traffic(scheme, sepp, headers_sr, sr);
    send_pr_n8_traffic(scheme, sepp, headers_pr, pr);
    send_rr_n8_traffic(scheme, sepp, (th === "false") ? headers_rr : headers_rr_th, rr);
    send_sr_n27_traffic(scheme, sepp, headers_sr, sr);
    send_pr_n27_traffic(scheme, sepp, headers_pr, pr);
    send_rr_n27_traffic(scheme, sepp, headers_rr, rr);
    send_tfqdn_sr_n8_traffic(scheme, encoded_sepp_host, encoded_host_from_rp2, headers_tfqdn, tfqdn);
    send_tfqdn_sr_n27_traffic(scheme, encoded_sepp_host, encoded_host_from_rp2, headers_tfqdn, tfqdn);

    if(ingressMSHeaders>0){
     send_ms_n8_traffic(scheme, sepp, "MS_IngressHeaders", headers_sr, headers_pr, headers_rr, headers_rr_th, ingressMSHeaders, th);
    }

    if(egressMSHeaders>0){
     send_ms_n8_traffic(scheme, sepp, "MS_EgressHeaders", headers_sr, headers_pr, headers_rr, headers_rr_th, egressMSHeaders, th);
    }

    if(ingressMSBody>0){
     send_ms_n8_traffic(scheme, sepp,"MS_IngressBody",  headers_sr, headers_pr, headers_rr, headers_rr_th, ingressMSBody, th);
    }

    if(egressMSBody>0){
     send_ms_n8_traffic(scheme, sepp, "MS_EgressBody",  headers_sr, headers_pr, headers_rr, headers_rr_th, egressMSBody, th);
    }

    if(combinedMS>0){
     send_ms_n8_traffic(scheme, sepp, "MS_Combined", headers_sr, headers_pr, headers_rr, headers_rr_th, combinedMS, th);
    }
}


function sepp_stability_from_own_to_rp1(){
	const headers_sr= {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json",
            "3gpp-Sbi-target-apiRoot": `${__ENV.O1_SCHEME}://${__ENV.O1_TARGET_HOST_1}.${__ENV.O1_TARGET_DOMAIN_1}:${__ENV.O1_TARGET_NF_PORT_1}`			
        }
    };

	const headers_pr = {
	        headers: {
	            "Connection": "keep-alive",
	            "Content-Type": "application/json",
	            "Accept": "application/json",
	            "3gpp-Sbi-target-apiRoot": `${__ENV.O1_SCHEME}://${__ENV.O1_TARGET_HOST_2}.${__ENV.O1_TARGET_DOMAIN_2}:${__ENV.O1_TARGET_NF_PORT_2}`			
	        }
	    };
	
	const headers_rr = {
	        headers: {
	            "Connection": "keep-alive",
	            "Content-Type": "application/json",
	            "Accept": "application/json",
	            "3gpp-Sbi-Discovery-nf-set-id": `${__ENV.O1_NF_SET_ID}`
	        }
	    };
	
	const headers_rr_th = {
	        headers: {                                                                                                                          
	            "Connection": "keep-alive",                                                                                                     
	            "Content-Type": "application/json",                                                                                             
	            "Accept": "application/json",                                                                                                  
	            "3gpp-Sbi-target-apiRoot": `${__ENV.O1_SCHEME}://${__ENV.O1_TARGET_HOST_TH}.${__ENV.O1_TARGET_DOMAIN_TH}:${__ENV.O1_TARGET_NF_PORT_TH}`                                  
	        }                                                                                                                                   
	    }; 
	
	const headers_tfqdn = {
	        headers: {
	            "Connection": "keep-alive",
	            "Content-Type": "application/json",
	            "Accept": "application/json",
	        }
	    };

    // const amf_host = `amf${__VU}.${__ENV.O1_OWN_DOMAIN}`;
    const scheme = `https://`;
    const sepp = `${__ENV.O1_SEPP_HOST}:${__ENV.O1_SEPP_PORT}/`;
    const encoded_sepp_host = `${__ENV.O1_ENCODED_HOST}.${__ENV.O1_SEPP_HOST}:${__ENV.O1_SEPP_PORT}/`;
    
    const pr = `${__ENV.O1_PR}`;
    const rr = `${__ENV.O1_RR}`;
	const sr = `${__ENV.O1_SR}`;
    const tfqdn = `${__ENV.O1_TFQDN}`;

    const th = `${__ENV.O1_TH_ENABLED}`;
    const ingressMSHeaders = `${__ENV.O1_MS_INGRESS_HEADERS}`;
    const egressMSHeaders = `${__ENV.O1_MS_EGRESS_HEADERS}`;
    const ingressMSBody = `${__ENV.O1_MS_INGRESS_BODY}`;
    const egressMSBody = `${__ENV.O1_MS_EGRESS_BODY}`;
    const combinedMS = `${__ENV.O1_COMBINED_MS}`;
    
	
    send_sr_n8_traffic(scheme, sepp, headers_sr, sr);
    send_pr_n8_traffic(scheme, sepp, headers_pr, pr);
    send_rr_n8_traffic(scheme, sepp, (th === "false") ? headers_rr : headers_rr_th, rr);
    send_sr_n27_traffic(scheme, sepp, headers_sr, sr);
    send_pr_n27_traffic(scheme, sepp, headers_pr, pr);
    send_rr_n27_traffic(scheme, sepp, headers_rr, rr);
    send_tfqdn_sr_n8_traffic(scheme, encoded_sepp_host, encoded_host_from_own_to_rp1, headers_tfqdn, tfqdn);
    send_tfqdn_sr_n27_traffic(scheme, encoded_sepp_host, encoded_host_from_own_to_rp1, headers_tfqdn, tfqdn);

    if(ingressMSHeaders>0){
     send_ms_n8_traffic(scheme, sepp, "MS_IngressHeaders", headers_sr, headers_pr, headers_rr, headers_rr_th, ingressMSHeaders, th);
    }

    if(egressMSHeaders>0){
     send_ms_n8_traffic(scheme, sepp, "MS_EgressHeaders", headers_sr, headers_pr, headers_rr, headers_rr_th, egressMSHeaders, th);
    }

    if(ingressMSBody>0){
     send_ms_n8_traffic(scheme, sepp,"MS_IngressBody",  headers_sr, headers_pr, headers_rr, headers_rr_th, ingressMSBody, th);
    }

    if(egressMSBody>0){
     send_ms_n8_traffic(scheme, sepp, "MS_EgressBody",  headers_sr, headers_pr, headers_rr, headers_rr_th, egressMSBody, th);
    }

    if(combinedMS>0){
     send_ms_n8_traffic(scheme, sepp, "MS_Combined", headers_sr, headers_pr, headers_rr, headers_rr_th, combinedMS, th);
    }
}

function sepp_stability_from_own_to_rp2(){
	const headers_sr= {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json",
            "3gpp-Sbi-target-apiRoot": `${__ENV.O2_SCHEME}://${__ENV.O2_TARGET_HOST_1}.${__ENV.O2_TARGET_DOMAIN_1}:${__ENV.O2_TARGET_NF_PORT_1}`			
        }
    };

	const headers_pr = {
	        headers: {
	            "Connection": "keep-alive",
	            "Content-Type": "application/json",
	            "Accept": "application/json",
	            "3gpp-Sbi-target-apiRoot": `${__ENV.O2_SCHEME}://${__ENV.O2_TARGET_HOST_2}.${__ENV.O2_TARGET_DOMAIN_2}:${__ENV.O2_TARGET_NF_PORT_2}`			
	        }
	    };
	
	const headers_rr = {
	        headers: {
	            "Connection": "keep-alive",
	            "Content-Type": "application/json",
	            "Accept": "application/json",
	            "3gpp-Sbi-Discovery-nf-set-id": `${__ENV.O2_NF_SET_ID}`
	        }
	    };
	
	const headers_rr_th = {
	        headers: {                                                                                                                          
	            "Connection": "keep-alive",                                                                                                     
	            "Content-Type": "application/json",                                                                                             
	            "Accept": "application/json",                                                                                                  
	            "3gpp-Sbi-target-apiRoot": `${__ENV.O2_SCHEME}://${__ENV.O2_TARGET_HOST_TH}.${__ENV.O2_TARGET_DOMAIN_TH}:${__ENV.O2_TARGET_NF_PORT_TH}`                                  
	        }                                                                                                                                   
	    }; 
	
	const headers_tfqdn = {
	        headers: {
	            "Connection": "keep-alive",
	            "Content-Type": "application/json",
	            "Accept": "application/json",
	        }
	    };

    // const amf_host = `amf${__VU}.${__ENV.O2_OWN_DOMAIN}`;
    const scheme = `https://`;
    const sepp = `${__ENV.O2_SEPP_HOST}:${__ENV.O2_SEPP_PORT}/`;
    const encoded_sepp_host = `${__ENV.O2_ENCODED_HOST}.${__ENV.O2_SEPP_HOST}:${__ENV.O2_SEPP_PORT}/`;
    
    const pr = `${__ENV.O2_PR}`;
    const rr = `${__ENV.O2_RR}`;
	const sr = `${__ENV.O2_SR}`;
    const tfqdn = `${__ENV.O2_TFQDN}`;

    const th = `${__ENV.O2_TH_ENABLED}`;
    const ingressMSHeaders = `${__ENV.O2_MS_INGRESS_HEADERS}`;
    const egressMSHeaders = `${__ENV.O2_MS_EGRESS_HEADERS}`;
    const ingressMSBody = `${__ENV.O2_MS_INGRESS_BODY}`;
    const egressMSBody = `${__ENV.O2_MS_EGRESS_BODY}`;
    const combinedMS = `${__ENV.O2_COMBINED_MS}`;
    
	
    send_sr_n8_traffic(scheme, sepp, headers_sr, sr);
    send_pr_n8_traffic(scheme, sepp, headers_pr, pr);
    send_rr_n8_traffic(scheme, sepp, (th === "false") ? headers_rr : headers_rr_th, rr);
    send_sr_n27_traffic(scheme, sepp, headers_sr, sr);
    send_pr_n27_traffic(scheme, sepp, headers_pr, pr);
    send_rr_n27_traffic(scheme, sepp, headers_rr, rr);
    send_tfqdn_sr_n8_traffic(scheme, encoded_sepp_host, encoded_host_from_own_to_rp1, headers_tfqdn, tfqdn);
    send_tfqdn_sr_n27_traffic(scheme, encoded_sepp_host, encoded_host_from_own_to_rp1, headers_tfqdn, tfqdn);

    if(ingressMSHeaders>0){
     send_ms_n8_traffic(scheme, sepp, "MS_IngressHeaders", headers_sr, headers_pr, headers_rr, headers_rr_th, ingressMSHeaders, th);
    }

    if(egressMSHeaders>0){
     send_ms_n8_traffic(scheme, sepp, "MS_EgressHeaders", headers_sr, headers_pr, headers_rr, headers_rr_th, egressMSHeaders, th);
    }

    if(ingressMSBody>0){
     send_ms_n8_traffic(scheme, sepp,"MS_IngressBody",  headers_sr, headers_pr, headers_rr, headers_rr_th, ingressMSBody, th);
    }

    if(egressMSBody>0){
     send_ms_n8_traffic(scheme, sepp, "MS_EgressBody",  headers_sr, headers_pr, headers_rr, headers_rr_th, egressMSBody, th);
    }

    if(combinedMS>0){
     send_ms_n8_traffic(scheme, sepp, "MS_Combined", headers_sr, headers_pr, headers_rr, headers_rr_th, combinedMS, th);
    }
}


//Function to send traffic per message screening param
function send_ms_n8_traffic(scheme, sepp, msHeader, headers_sr, headers_pr, headers_rr, headers_rr_th, traffic, th) {
     let headers_ms_sr = headers_sr;
     headers_ms_sr["headers"]["ms-header"] = msHeader;
     headers_ms_sr["headers"]["dummy-header-2"] = "";
     send_sr_n8_traffic(scheme, sepp, headers_ms_sr, traffic/3);

     let headers_ms_pr = headers_pr;
     headers_ms_pr["headers"]["ms-header"] = msHeader;
     headers_ms_pr["headers"]["dummy-header-2"] = "";
     send_pr_n8_traffic(scheme, sepp, headers_ms_pr, traffic/3);

     let headers_ms_rr = (th === "false") ? headers_rr : headers_rr_th;
     headers_ms_rr["headers"]["ms-header"] = msHeader;
     headers_ms_rr["headers"]["dummy-header-2"] = "";
     send_rr_n8_traffic(scheme, sepp, headers_ms_rr, traffic/3);
}

//Function to send strict routing traffic
function send_sr_n8_traffic(scheme, sepp, headers, traffic) {

    if(traffic>0){
    
    	const method = "GET";

        let requests = [[]];
     
        let body = get_create_body();
        
        //create the batch for traffic
        for(let i=0;i<traffic;i++) {
            requests[i] = [method, scheme + sepp + uri_n8, body, headers];
        }
    
        let responses = http.batch(requests);
    
        let resp_length = Object.keys(responses).length;
        for(let i=0;i<resp_length;i++)
        {
            check(responses[i], {
                "SR/N8 traffic: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
                "SR/N8 traffic: status was 200": (r) => r.status == 200
            });		
        }
    
    }
    return 0;
}

function send_sr_n27_traffic(scheme, sepp, headers_sr, sr) {

    if(sr>0){
    
    	const method = "GET";
    		
        let requests = [[]];
     
        let body = get_create_body();
        
        //create the batch for traffic
        for(let i=0;i<sr;i++) {
            requests[i] = [method, scheme + sepp + uri_n27, body, headers_sr];
        }
    
        let responses = http.batch(requests);
    
        let resp_length = Object.keys(responses).length;
        for(let i=0;i<resp_length;i++)
        {
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

    if(traffic>0){
        
        const method = "GET";
        
        let requests = [[]];
     
        let body = get_create_body();
        
        //create the batch for traffic
        for(let i=0;i<traffic;i++) {
            requests[i] = [method, scheme + sepp + uri_n8, body, headers];
        }
    
        let responses = http.batch(requests);
    
        let resp_length = Object.keys(responses).length;
        for(let i=0;i<resp_length;i++)
        {
            check(responses[i], {
                "PR/N8 traffic: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
                "PR/N8 traffic: status was 200": (r) => r.status == 200
            });	
        }
    }
    return 0;
}

function send_pr_n27_traffic(scheme, sepp, headers_pr, pr) {

    if(pr>0){
        
        const method = "GET";
        
        let requests = [[]];
     
        let body = get_create_body();
        
        //create the batch for traffic
        for(let i=0;i<pr;i++) {
            requests[i] = [method, scheme + sepp + uri_n27, body, headers_pr];
        }
    
        let responses = http.batch(requests);
    
        let resp_length = Object.keys(responses).length;
        for(let i=0;i<resp_length;i++)
        {
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

    if(traffic>0){
        
        const method = "GET";
        
        let requests = [[]];
     
        let body = get_create_body();
        
        //create the batch for traffic
        for(let i=0;i<traffic;i++) {
            requests[i] = [method, scheme + sepp + uri_n8, body, headers];
        }
    
        let responses = http.batch(requests);
    
        let resp_length = Object.keys(responses).length;
        for(let i=0;i<resp_length;i++)
        {
            check(responses[i], {
                "RR traffic: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
                "RR traffic: status was 200": (r) => r.status == 200
            });	
        }
    }
    return 0;
}

function send_rr_n27_traffic(scheme, sepp, headers_rr, rr) {

    if(rr>0){
        
        const method = "GET";
        
        let requests = [[]];
     
        let body = get_create_body();
        
        //create the batch for traffic
        for(let i=0;i<rr;i++) {
            requests[i] = [method, scheme + sepp + uri_n27, body, headers_rr];
        }
    
        let responses = http.batch(requests);
    
        let resp_length = Object.keys(responses).length;
        for(let i=0;i<resp_length;i++)
        {
            check(responses[i], {
                "RR/N27 traffic: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
                "RR/N27 traffic: status was 200": (r) => r.status == 200
            });	
        }
    }
    return 0;
}

//Function to send T-FQDN traffic
function send_tfqdn_sr_n8_traffic(scheme, encoded_sepp_host, encoded_host, headers_tfqdn,tfqdn) {

    if(tfqdn>0 && encoded_host !== "null"){

                const method = "GET";

        let requests = [[]];

        let body = get_create_body();

        //create the batch for traffic
        for(let i=0;i<tfqdn;i++) {
            requests[i] = [method, scheme + encoded_sepp_host + uri_n8, body, headers_tfqdn];
        }

        let responses = http.batch(requests);

        let resp_length = Object.keys(responses).length;
        for(let i=0;i<resp_length;i++)
        {
            check(responses[i], {
                "TFQDN/N8 traffic: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
                "TFQDN/N8 traffic: status was 200": (r) => r.status == 200
            });
        }

    }
    return 0;
}

//Function to send T-FQDN traffic
function send_tfqdn_sr_n27_traffic(scheme, encoded_sepp_host, encoded_host, headers_tfqdn,tfqdn) {

    if(tfqdn>0 && encoded_host !== "null"){

                const method = "GET";

        let requests = [[]];

        let body = get_create_body();

        //create the batch for traffic
        for(let i=0;i<tfqdn;i++) {
            requests[i] = [method, scheme + encoded_sepp_host + uri_n27, body, headers_tfqdn];
        }

        let responses = http.batch(requests);

        let resp_length = Object.keys(responses).length;
        for(let i=0;i<resp_length;i++)
        {
            check(responses[i], {
                "TFQDN/N27 traffic: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
                "TFQDN/N27 traffic: status was 200": (r) => r.status == 200
            });
        }

    }
    return 0;
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

