/**
 * @author xkonska, eedcsi, eyossan, esavpar, eavvann
 */

import http from "k6/http";
import {
    check
} from "k6";

var host_v4 = __ENV.SEPP_HOST_V4;
var ip_v4 = `${__ENV.SEPP_IPV4}`;
var host_v6 = __ENV.SEPP_HOST_V6;
var ip_v6 = `${__ENV.SEPP_IPV6}`;

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
const uri_n27 = "nnrf-disc/v1/nf-instances?requester-nf-type=AMF&target-nf-type=UDM&supi=imsi-460001357924675";

const headers_st = {
    headers: {
        "Connection": "keep-alive",
        "Content-Type": "application/json",
        "Accept": "application/json",
        "3gpp-Sbi-target-apiRoot": "eric-seppsim-p7-mcc-262-mnc-73"      
    }
};

const headers_tt = {
    headers: {
        "Connection": "keep-alive",
        "Content-Type": "application/json",
        "Accept": "application/json",
        "3gpp-Sbi-target-apiRoot": "eric-seppsim-p3-mcc-262-mnc-73"      
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
    
  if (host_v4 && host_v4 !== "null") {
      const sepp_v4 = `https://${host_v4}:${__ENV.SEPP_PORT_V4}/`;
      const th = `${__ENV.TH_ENABLED}`;
      
      send_traffic(sepp_v4, uri_n8, headers_st, `${__ENV.ST_V4}`/2, "ST");
      send_traffic(sepp_v4, uri_n8, headers_tt, `${__ENV.TT_V4}`/2, "TT");
      send_traffic(sepp_v4, uri_n8, (th === "false") ? headers_fw : headers_fw_th, `${__ENV.FW_V4}`/2, "FW");
      send_traffic(sepp_v4, uri_n27, headers_st, `${__ENV.ST_V4}`/2, "ST");
      send_traffic(sepp_v4, uri_n27, headers_tt, `${__ENV.TT_V4}`/2, "TT");
      send_traffic(sepp_v4, uri_n27, headers_fw, `${__ENV.FW_V4}`/2, "FW");
  }
    
  if (host_v6 && host_v6 !== "null") {
      const sepp_v6 = `https://${host_v6}:${__ENV.SEPP_PORT_V6}/`;
      
      send_traffic(sepp_v6, uri_n8, headers_st, `${__ENV.ST_V6}`/2, "ST");
      send_traffic(sepp_v6, uri_n8, headers_tt, `${__ENV.TT_V6}`/2, "TT");
      send_traffic(sepp_v6, uri_n8, headers_fw, `${__ENV.FW_V6}`/2), "FW";
      send_traffic(sepp_v6, uri_n27, headers_st, `${__ENV.ST_V6}`/2, "ST");
      send_traffic(sepp_v6, uri_n27, headers_tt, `${__ENV.TT_V6}`/2, "TT");
      send_traffic(sepp_v6, uri_n27, headers_fw, `${__ENV.FW_V6}`/2, "FW");
  }
}

//Function to send target traffic 
function send_traffic(sepp, uri, headers, requestsNumber, routingMethod) {

    if(requestsNumber>0){
    
        const method = "GET";
        
        let requests = [[]];
     
        let body = get_create_body();
        
        //create the batch for traffic
        for(let i=0; i<requestsNumber; i++) {
            requests[i] = [method, sepp + uri, body, headers];
        }
        let responses = http.batch(requests);
    
        let resp_length = Object.keys(responses).length;
        
        let trafficType = "";
        if (uri.includes("nudm-sdm")) {
            trafficType = "N8";
        }
        else if (uri.includes("nnrf-disc")) {
            trafficType = "N27";
        }
        
        let msg1 = routingMethod + "/" + trafficType + " traffic: protocol is HTTP/2";
        let msg2 = routingMethod + "/" + trafficType + " traffic: status was 200";
        
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


