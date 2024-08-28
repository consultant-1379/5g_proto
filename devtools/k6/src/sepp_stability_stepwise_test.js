/**

 * @author eavvann
 */

import http from "k6/http";
import {
    check
} from "k6";

// each iteration has many requests 
let st = parseInt(`${__ENV.ST}`);
let tt = parseInt(`${__ENV.TT}`);
let fw = parseInt(`${__ENV.FW}`); 
let total = st+tt+fw;
  
let floatIterations = parseInt(`${__ENV.RPS}`)/ total;
let iterations = Math.ceil(floatIterations);
let duration = `${__ENV.DURATION}`;
let maxVirtualUsers = parseInt(`${__ENV.MAX_VUS}`);
let diff = floatIterations/iterations;

st = Math.ceil(st * diff);
tt = Math.ceil(tt * diff);
fw = Math.ceil(fw * diff);

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
        "sepp.ericsson.se":`${__ENV.SEPP_IP}`
    },
    tlsAuth: [{
        domains: [`${__ENV.SEPP_HOST}`],
        cert: __ENV.CERT,
        key: __ENV.KEY
    }]
};

const uri_n8 = "nudm-sdm/v2/imsi-460001357924675/nssai";
const uri_n27 = "nnrf-disc/v1/nf-instances?requester-nf-type=AMF&target-nf-type=UDM&supi=imsi-460001357924675";

const headers_st1 = {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json",
            "3gpp-Sbi-target-apiRoot": "eric-seppsim-p7-mcc-262-mnc-73"			
        }
    };
    
const headers_st2 = {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json",
            "3gpp-Sbi-target-apiRoot": "eric-seppsim-p8-mcc-262-mnc-73"			
        }
    };

const headers_tt1 = {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json",
            "3gpp-Sbi-target-apiRoot": "eric-seppsim-p3-mcc-262-mnc-73"			
        }
    };
    
const headers_tt2 = {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json",
            "3gpp-Sbi-target-apiRoot": "eric-seppsim-p4-mcc-262-mnc-73"			
        }
    };

const headers_fw = {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json",
            "3gpp-Sbi-Discovery-nf-set-id": "nf-set_1"
        }
    };

const headers_fw_th = {
        headers: {                                                                                                                          
            "Connection": "keep-alive",                                                                                                     
            "Content-Type": "application/json",                                                                                             
            "Accept": "application/json",                                                                                                  
            "3gpp-Sbi-target-apiRoot": `${__ENV.SCHEME}://${__ENV.TARGET_HOST_TH}.${__ENV.TARGET_DOMAIN_TH}:${__ENV.TARGET_NF_PORT_TH}`                                  
        }                                                                                                                                   
    };
	
/** Send batch messages of 
 *  ST x strickt_target_taffic
 *  TT x try_target_taffic
 *  FW x forward_taffic 
 *  requests 
 */
export default function() {

		const smf_host = `smf${__VU}.ericsson.se`;
		const sepp = `https://${__ENV.SEPP_HOST}:${__ENV.SEPP_PORT}/`;
                const th = `${__ENV.TH_ENABLED}`;
	
		send_st_n8_traffic(sepp, headers_st1, headers_st2, uri_n8);
		send_tt_n8_traffic(sepp, headers_tt1, headers_tt2, uri_n8);
		send_fw_n8_traffic(sepp, (th === "false") ? headers_fw : headers_fw_th, uri_n8, `${__ENV.FW}`);
		send_st_n27_traffic(sepp, headers_st1, headers_st2, uri_n27);
		send_tt_n27_traffic(sepp, headers_tt1, headers_tt2, uri_n27);
		send_fw_n27_traffic(sepp, headers_fw, uri_n27, `${__ENV.FW}`);
}

//Function to send strickt target traffic 
function send_st_n8_traffic(sepp, headers_st1, headers_st2, uri_n8) {

    if(st>0){
    
    	const method = "GET";
    		
        let requests = [[]];
     
        let body = get_create_body();
        
        //create the batch for traffic
        for(let i=0;i<st/2;i++) {
            if(i%2==0) {
                requests[i] = [method, sepp + uri_n8, body, headers_st1];
            }
            else {
                requests[i] = [method, sepp + uri_n8, body, headers_st2];
            }
        }
    
        let responses = http.batch(requests);
    
        let resp_length = Object.keys(responses).length;
        for(let i=0;i<resp_length;i++)
        {
            check(responses[i], {
                "ST/N8 traffic: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
                "ST/N8 traffic: status was 200": (r) => r.status == 200
            });		
        }
    
    }
    return 0;
}

function send_st_n27_traffic(sepp, headers_st1, headers_st2, uri_n27) {

    if(st>0){
    
    	const method = "GET";
    		
        let requests = [[]];
     
        let body = get_create_body();
        
        //create the batch for traffic
        for(let i=0;i<st/2;i++) {
            if(i%2==0) {
                requests[i] = [method, sepp + uri_n27, body, headers_st1];
            }
            else {
                requests[i] = [method, sepp + uri_n27, body, headers_st2];
            }
        }
    
        let responses = http.batch(requests);
    
        let resp_length = Object.keys(responses).length;
        for(let i=0;i<resp_length;i++)
        {
            check(responses[i], {
                "ST/N27 traffic: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
                "ST/N27 traffic: status was 200": (r) => r.status == 200
            });		
        }
    
    }
    return 0;
}

//Function to send try target traffic 
function send_tt_n8_traffic(sepp, headers_tt1, headers_tt2, uri_n8) {

    if(tt>0){
        
        const method = "GET";
        
        let requests = [[]];
     
        let body = get_create_body();
        
        //create the batch for traffic
        for(let i=0;i<tt/2;i++) {
            if(i%2==0) {
                requests[i] = [method, sepp + uri_n8, body, headers_tt1];
            }
            else {
                requests[i] = [method, sepp + uri_n8, body, headers_tt2];
            }
        }
    
        let responses = http.batch(requests);
    
        let resp_length = Object.keys(responses).length;
        for(let i=0;i<resp_length;i++)
        {
            check(responses[i], {
                "TT/N8 traffic: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
                "TT/N8 traffic: status was 200": (r) => r.status == 200
            });	
        }
    }
    return 0;
}

function send_tt_n27_traffic(sepp, headers_tt1, headers_tt2, uri_n27) {

    if(tt>0){
        
        const method = "GET";
        
        let requests = [[]];
     
        let body = get_create_body();
        
        //create the batch for traffic
        for(let i=0;i<tt/2;i++) {
            if(i%2==0) {
                requests[i] = [method, sepp + uri_n8, body, headers_tt1];
            }
            else {
                requests[i] = [method, sepp + uri_n8, body, headers_tt2];
            }
        }
    
        let responses = http.batch(requests);
    
        let resp_length = Object.keys(responses).length;
        for(let i=0;i<resp_length;i++)
        {
            check(responses[i], {
                "TT/N27 traffic: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
                "TT/N27 traffic: status was 200": (r) => r.status == 200
            });	
        }
    }
    return 0;
}

//Function to send forward traffic 
function send_fw_n8_traffic(sepp, headers_fw, uri_n8) {

    if(fw>0){
        
        const method = "GET";
        
        let requests = [[]];
     
        let body = get_create_body();
        
        //create the batch for traffic
        for(let i=0;i<fw/2;i++) {
            requests[i] = [method, sepp + uri_n8, body, headers_fw];
        }
    
        let responses = http.batch(requests);
    
        let resp_length = Object.keys(responses).length;
        for(let i=0;i<resp_length;i++)
        {
            check(responses[i], {
                "FW traffic: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
                "FW traffic: status was 200": (r) => r.status == 200
            });	
        }
    }
    return 0;
}

function send_fw_n27_traffic(sepp, headers_fw, uri_n27) {

    if(fw>0){
        
        const method = "GET";
        
        let requests = [[]];
     
        let body = get_create_body();
        
        //create the batch for traffic
        for(let i=0;i<fw/2;i++) {
            requests[i] = [method, sepp + uri_n27, body, headers_fw];
        }
    
        let responses = http.batch(requests);
    
        let resp_length = Object.keys(responses).length;
        for(let i=0;i<resp_length;i++)
        {
            check(responses[i], {
                "FW/N27 traffic: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
                "FW/N27 traffic: status was 200": (r) => r.status == 200
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
        //"notifyUri": "http://" + smf_host + ":8080/rar",
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

