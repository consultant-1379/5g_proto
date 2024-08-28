/**
 * @author eyossan, esavpar, eavvann
 */

import http from "k6/http";
import {
    check
} from "k6";

var host_v4 = __ENV.SCP_HOST_V4;
var ip_v4 = `${__ENV.SCP_IPV4}`;
var host_v6 = __ENV.SCP_HOST_V6;
var ip_v6 = `${__ENV.SCP_IPV6}`;

var hostsObject = {};
var domainsObject = [];

if(host_v4 && host_v4 !== "null") {
    hostsObject[host_v4] = ip_v4;
    domainsObject.push(host_v4);
}

if(host_v6 && host_v6 !== "null") {  
    hostsObject[host_v6] = ip_v6;          
    domainsObject.push(host_v6);
}  

export let options = {
    hosts: hostsObject,
    tlsAuth: [{
        domains: domainsObject,
        cert: open("/certs/K6.crt"),
        key: open("/certs/K6.key")
   }],
    vus: 1,
    duration: 10
};

const uri_n8 = "nudm-sdm/v2/imsi-460001357924675/nssai";

const headers_sr1 = {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json",
            "3gpp-Sbi-target-apiRoot": "eric-seppsim-p7-mcc-262-mnc-73"      
        }
    };

const headers_sr2 = {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json",
            "3gpp-Sbi-target-apiRoot": "eric-seppsim-p8-mcc-262-mnc-73"
        }
    };

const headers_pr1 = {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json",
            "3gpp-Sbi-target-apiRoot": "eric-seppsim-p5-mcc-262-mnc-73"
        }
    };

const headers_pr2 = {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json",
            "3gpp-Sbi-target-apiRoot": "eric-seppsim-p6-mcc-262-mnc-73"
        }
    };

const headers_rr = {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json",
            "3gpp-Sbi-Discovery-nf-set-id": "nf-set_1"
        }
    };

const headers_ms_add = {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json",
            "3gpp-Sbi-Discovery-nf-set-id": "MS_addHeader",
            "dummy-header-1": ""
        }
    }

const headers_ms_modify = {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json",
            "3gpp-Sbi-Discovery-nf-set-id": "MS_modifyHeader",
            "dummy-header-1": "dummy-header-value-1",
            "dummy-header-2": "2"
        }
    }

const headers_ms_remove = {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json",
            "3gpp-Sbi-Discovery-nf-set-id": "MS_removeHeader",
            "dummy-header-1": "dummy-header-value-1"
        }
    }

/** Send batch messages of 
 *  SR x taffic_for_strict_routing
 *  PR x taffic_for_preferred_routing
 *  RR x trafic_for_round-robin
 *  MS_ADD    x traffic_for_message_screening_with_adding_header
 *  MS_MODIFY x traffic_for_message_screening_with_modifying_header
 *  MS_REMOVE x traffic_for_message_screening_with_removing_header
 *  requests 
 */
export default function() {

    const smf_host = `smf${__VU}.ericsson.se`;
    
    if(host_v4 && host_v4 !== "null") {
      const scp_v4 = `https://${host_v4}:${__ENV.SCP_PORT_V4}/`;

        send_traffic(scp_v4, uri_n8, headers_sr1, `${__ENV.SR_V4}`/2, "SR");
        send_traffic(scp_v4, uri_n8, headers_sr2, `${__ENV.SR_V4}`/2, "SR");
        send_traffic(scp_v4, uri_n8, headers_pr1, `${__ENV.PR_V4}`/2, "PR");
        send_traffic(scp_v4, uri_n8, headers_pr2, `${__ENV.PR_V4}`/2, "PR");
        send_traffic(scp_v4, uri_n8, headers_rr, `${__ENV.RR_V4}`, "RR");
        send_traffic(scp_v4, uri_n8, headers_ms_add, `${__ENV.MS_ADD_V4}`, "RR with MS");
        send_traffic(scp_v4, uri_n8, headers_ms_modify, `${__ENV.MS_MODIFY_V4}`, "RR with MS");
        send_traffic(scp_v4, uri_n8, headers_ms_remove, `${__ENV.MS_REMOVE_V4}`, "RR with MS");
    }
        
    if(host_v6 && host_v6 !== "null") {
      const scp_v6 = `https://${host_v6}:${__ENV.SCP_PORT_V6}/`;
      
      send_traffic(scp_v6, uri_n8, headers_sr1, `${__ENV.SR_V6}`/2, "SR");
      send_traffic(scp_v6, uri_n8, headers_sr2, `${__ENV.SR_V6}`/2, "SR");
      send_traffic(scp_v6, uri_n8, headers_pr1, `${__ENV.PR_V6}`/2, "PR");
      send_traffic(scp_v6, uri_n8, headers_pr2, `${__ENV.PR_V6}`/2, "PR");
      send_traffic(scp_v6, uri_n8, headers_rr, `${__ENV.RR_V6}`, "RR");
      send_traffic(scp_v6, uri_n8, headers_ms_add, `${__ENV.MS_ADD_V6}`, "RR with MS");
      send_traffic(scp_v6, uri_n8, headers_ms_modify, `${__ENV.MS_MODIFY_V6}`, "RR with MS");
      send_traffic(scp_v6, uri_n8, headers_ms_remove, `${__ENV.MS_REMOVE_V6}`, "RR with MS");
    }

}

//Function to send traffic for strict routing
function send_traffic(scp, uri, headers, requestsNumber, routingMethod) {

    if(requestsNumber>0){
    
        const method = "GET";
        
        let requests = [[]];
     
        let body = get_create_body();
        
        //create the batch for traffic
        for(let i=0; i<requestsNumber; i++) {
            requests[i] = [method, scp + uri, body, headers];
        }
    
        let responses = http.batch(requests);
    
        let resp_length = Object.keys(responses).length;
        
        let msg1 = routingMethod + "/N8 traffic: protocol is HTTP/2";
        let msg2 = routingMethod + "/N8 traffic: status was 200";
        
        for(let i=0;i<resp_length;i++)
        {
            check(responses[i], {
                [msg1]: (r) => r.proto === 'HTTP/2.0',
                [msg2]: (r) => r.status == 200
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


