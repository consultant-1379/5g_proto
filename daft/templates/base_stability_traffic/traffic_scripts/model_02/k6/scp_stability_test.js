/**
 * @author eyossan
 */

import http from "k6/http";
import {
    check
} from "k6";
import { Counter } from 'k6/metrics';


const uri_n8 = "nudm-sdm/v2/imsi-460001357924675/nssai";
const uri_n8_information_retrieval = "nudm-uecm/v1/msisdn-460030100000000/registrations/smf-registrations";
const uri_nudm_sdm = "nudm-sdm/v2/shared-data-subscriptions";

const egressTlsEnabled = `${__ENV.EGRESS_TLS_ENABLED}`;
const nfPeerInfoEnabled = `${__ENV.NF_PEER_INFO_ENABLED}`;
const counterSeppSimP3_2xx = new Counter('seppsim_p3_successful_responses_with_scheme_http');
const counterSeppSimP4_2xx = new Counter('seppsim_p4_successful_responses_with_scheme_https');

const scpIps = [`https://${__ENV.SCP_IP}:${__ENV.SCP_PORT}/`];
if (__ENV.SCP_IP6 !== "null") {
    scpIps.push(`https://[${__ENV.SCP_IP6}]:${__ENV.SCP_PORT}/`);
}

export const options = {
    thresholds: { seppsim_p3_successful_responses_with_scheme_http: ['count>0'], seppsim_p4_successful_responses_with_scheme_https: ['count>0'] }
};


const headers_sr1 = {
    headers: {
        "Connection": "keep-alive",
        "Content-Type": "application/json",
        "Accept": "application/json",
	"3gpp-Sbi-target-apiRoot": (egressTlsEnabled === "true") ? `https://eric-seppsim-p7-mcc-262-mnc-73:${__ENV.SRHOSTPORT1}` : `http://eric-seppsim-p7-mcc-262-mnc-73:${__ENV.SRHOSTPORT1}`
    }
};

const headers_sr2 = {
    headers: {
        "Connection": "keep-alive",
        "Content-Type": "application/json",
        "Accept": "application/json",
        "3gpp-Sbi-target-apiRoot": (egressTlsEnabled === "true") ? `https://eric-seppsim-p8-mcc-262-mnc-73:${__ENV.SRHOSTPORT2}` : `http://eric-seppsim-p8-mcc-262-mnc-73:${__ENV.SRHOSTPORT2}`
    }
};

const headers_sr3_interPlmn = {
    headers: {
        "Connection": "keep-alive",
        "Content-Type": "application/json",
        "Accept": "application/json",
        "3gpp-Sbi-target-apiRoot": (egressTlsEnabled === "true") ? `https://nfudm1.mnc073.mcc262.3gppnetwork.org:${__ENV.SRHOSTPORT1}` : `http://nfudm1.mnc073.mcc262.3gppnetwork.org:${__ENV.SRHOSTPORT1}`
    }
};

const headers_pr1 = {
    headers: {
        "Connection": "keep-alive",
        "Content-Type": "application/json",
        "Accept": "application/json",
        "3gpp-Sbi-target-apiRoot": (egressTlsEnabled === "true") ? "https://eric-seppsim-p5-mcc-262-mnc-73" : "http://eric-seppsim-p5-mcc-262-mnc-73"
    }
};

const headers_pr2 = {
    headers: {
        "Connection": "keep-alive",
        "Content-Type": "application/json",
        "Accept": "application/json",
        "3gpp-Sbi-target-apiRoot": (egressTlsEnabled === "true") ? "https://eric-seppsim-p6-mcc-262-mnc-73" : "http://eric-seppsim-p6-mcc-262-mnc-73"
    }
};

const headers_pr3_interPlmn = {
    headers: {
        "Connection": "keep-alive",
        "Content-Type": "application/json",
        "Accept": "application/json",
        "3gpp-Sbi-target-apiRoot": (egressTlsEnabled === "true") ? "https://nfudm2.mnc073.mcc262.3gppnetwork.org" : "http://nfudm2.mnc073.mcc262.3gppnetwork.org"
    }
};

const headers_gpsir = {
    headers: {
        "Connection": "keep-alive",
        "Content-Type": "application/json",
        "Accept": "application/json",
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

const headers_inter_udm_sdm_init_nf_filter = {
    headers: {
        "Connection": "keep-alive",
        "Content-Type": "application/json",
        "Accept": "application/json",
        "3gpp-Sbi-Target-apiRoot": 'http://eric-seppsim-p3-mcc-262-mnc-73.${__ENV.NAMESPACE}'
    }
};

//Update headers in case of 3gpp-sbi-nf-peer-info
if (nfPeerInfoEnabled === "true") {

    let sbiNfPeerInfoHeader = `srcinst=2ec8ac0b-265e-4165-86e9-e0735e6ce307;dstinst=2ec8ac0b-265e-4165-86e9-e0735e6ce309;dstscp=${__ENV.SCP_HOST}`;

    headers_sr1["headers"]["3gpp-sbi-nf-peer-info"] = sbiNfPeerInfoHeader;
    headers_sr2["headers"]["3gpp-sbi-nf-peer-info"] = sbiNfPeerInfoHeader;
    headers_pr1["headers"]["3gpp-sbi-nf-peer-info"] = sbiNfPeerInfoHeader;
    headers_pr2["headers"]["3gpp-sbi-nf-peer-info"] = sbiNfPeerInfoHeader;
    headers_rr["headers"]["3gpp-sbi-nf-peer-info"] = sbiNfPeerInfoHeader;

}


/** Send batch messages of 
 *  SR x traffic_for_strict_routing
 *  PR x traffic_for_preferred_routing
 *  GPSIR x traffic_for gpsi_routing
 *  RR x trafic_for_round-robin
 *  MS_ADD    x traffic_for_message_screening_with_adding_header
 *  MS_MODIFY x traffic_for_message_screening_with_modifying_header
 *  MS_REMOVE x traffic_for_message_screening_with_removing_header
 *  requests 
 */
export default function () {

    const smf_host = `smf${__VU}.ericsson.se`;
    const scp = scpIps[Math.floor(Math.random() * scpIps.length)];
    const ingressMSHeaders = `${__ENV.MS_INGRESS_HEADERS}`;
    const egressMSHeaders = `${__ENV.MS_EGRESS_HEADERS}`;
    const ingressMSBody = `${__ENV.MS_INGRESS_BODY}`;
    const egressMSBody = `${__ENV.MS_EGRESS_BODY}`;
    const combinedMS = `${__ENV.COMBINED_MS}`;

    send_sr_n8_traffic(scp, headers_sr1, headers_sr2, `${__ENV.SR}` / 2);
    send_sr_n8_traffic(scp, headers_sr3_interPlmn, headers_sr2, `${__ENV.SR}` / 2);
    send_pr_n8_traffic(scp, headers_pr1, headers_pr2, `${__ENV.PR}` / 2);
    send_pr_n8_traffic(scp, headers_pr3_interPlmn, headers_pr2, `${__ENV.PR}` / 2);
    send_gpsir_n8_traffic(scp, headers_gpsir, `${__ENV.GPSIR}`);
    send_rr_n8_traffic(scp, headers_rr, `${__ENV.RR}`);

    if (ingressMSHeaders > 0) {
        send_ms_n8_traffic(scp, "MS_IngressHeaders", ingressMSHeaders);
    }

    if (egressMSHeaders > 0) {
        send_ms_n8_traffic(scp, "MS_EgressHeaders", egressMSHeaders);
    }

    if (ingressMSBody > 0) {
        send_ms_n8_traffic(scp, "MS_IngressBody", ingressMSBody);
    }

    if (egressMSBody > 0) {
        send_ms_n8_traffic(scp, "MS_EgressBody", egressMSBody);
    }

    if (combinedMS > 0) {
        send_ms_n8_traffic(scp, "MS_Combined", combinedMS);
    }

    if (`${__ENV.NF_FILTER_ENABLED}` === "true") {
        nudm_sdm_subscribe_nf_filter(smf_host, scp, `${__ENV.RR}`);
    }
}

//Function to send traffic per message screening param
function send_ms_n8_traffic(scp, msHeader, traffic) {
    let headers_ms_sr1 = headers_sr1;
    headers_ms_sr1["headers"]["ms-header"] = msHeader;
    headers_ms_sr1["headers"]["dummy-header-2"] = "";
    let headers_ms_sr2 = headers_sr2;
    headers_ms_sr2["headers"]["ms-header"] = msHeader;
    headers_ms_sr2["headers"]["dummy-header-2"] = "";
    send_sr_n8_traffic(scp, headers_ms_sr1, headers_ms_sr2, traffic / 3);

    let headers_ms_pr1 = headers_pr1;
    headers_ms_pr1["headers"]["ms-header"] = msHeader;
    headers_ms_pr1["headers"]["dummy-header-2"] = "";
    let headers_ms_pr2 = headers_pr2;
    headers_ms_pr2["headers"]["ms-header"] = msHeader;
    headers_ms_pr2["headers"]["dummy-header-2"] = "";
    send_pr_n8_traffic(scp, headers_ms_pr1, headers_ms_pr2, traffic / 3);

    let headers_ms_rr = headers_rr;
    headers_ms_rr["headers"]["ms-header"] = msHeader;
    headers_ms_rr["headers"]["dummy-header-2"] = "";
    send_rr_n8_traffic(scp, headers_ms_rr, traffic / 3);
}

//Function to send traffic for strict routing
function send_sr_n8_traffic(scp, headers1, headers2, traffic) {

    if (traffic > 0) {

        const method = "GET";

        let requests = [[]];

        let body = get_create_body();

        //create the batch for traffic
        for (let i = 0; i < traffic; i++) {

            if ((i % 2) == 0) {
                requests[i] = [method, scp + uri_n8, body, headers1];
            }

            if ((i % 2) == 1) {
                requests[i] = [method, scp + uri_n8, body, headers2];
            }

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

//Function to send traffic for preferred routing
function send_pr_n8_traffic(scp, headers1, headers2, traffic) {

    if (traffic > 0) {

        const method = "GET";

        let requests = [[]];

        let body = get_create_body();

        //create the batch for traffic
        for (let i = 0; i < traffic; i++) {

            if ((i % 2) == 0) {
                requests[i] = [method, scp + uri_n8, body, headers1];
            }

            if ((i % 2) == 1) {
                requests[i] = [method, scp + uri_n8, body, headers2];
            }

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

//Function to send traffic for Gpsi routing
function send_gpsir_n8_traffic(scp, headers, traffic) {

    if (traffic > 0) {

        const method = "GET";

        let requests = [[]];

        let body = '{}';

        //create the batch for traffic
        for (let i = 0; i < traffic; i++) {
            requests[i] = [method, scp + uri_n8_information_retrieval, body, headers];
        }

        let responses = http.batch(requests);

        let resp_length = Object.keys(responses).length;
        for (let i = 0; i < resp_length; i++) {
            check(responses[i], {
                "GPSI/N8 traffic: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
                "GPSI/N8 traffic: status was 200": (r) => r.status == 200
            });
        }
    }
    return 0;
}

//Function to send traffic for round-robin
function send_rr_n8_traffic(scp, headers, traffic) {

    if (traffic > 0) {

        const method = "GET";

        let requests = [[]];

        let body = get_create_body();

        //create the batch for traffic
        for (let i = 0; i < traffic; i++) {
            requests[i] = [method, scp + uri_n8, body, headers];
        }

        let responses = http.batch(requests);

        let resp_length = Object.keys(responses).length;
        for (let i = 0; i < resp_length; i++) {
            const regexPattern_Http_SeppsimP3 = /^http:/; // This regex pattern matches strings that start with "http:"
            const regexPattern_Https_SeppsimP4 = /^https:/; // This regex pattern matches strings that start with "https:"
            const regexPattern_Xorigin_SeppsimP3 = /^eric-seppsim-p3/; // This regex pattern matches string that start with "eric-seppsim-p3"
            const regexPattern_Xorigin_SeppsimP4 = /^eric-seppsim-p4/; // This regex pattern matches strings that start with "eric-seppsim-p4"

            //check if the Location contains scheme http and the X-Origin contains the string eric-seppsim-p3 and the status code is 200
            if (regexPattern_Http_SeppsimP3.test(responses[i].headers['Location']) && regexPattern_Xorigin_SeppsimP3.test(responses[i].headers['X-Origin']) && responses[i].status == 200) {
                // console.log('seppsim-p3 with scheme http found and get a successful response');
                counterSeppSimP3_2xx.add(1);
            }
            //check if the Location contains scheme https and the X-Origin contains the string eric-seppsim-p4 and the status code is 200
            if (regexPattern_Https_SeppsimP4.test(responses[i].headers['Location']) && regexPattern_Xorigin_SeppsimP4.test(responses[i].headers['X-Origin']) && responses[i].status == 200) {
                // console.log('seppsim-p4 with scheme https found and get a successful response');
                counterSeppSimP4_2xx.add(1);
            }
            check(responses[i], {
                "RR traffic: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
                "RR traffic: status was 200": (r) => r.status == 200
            });
        }
    }
    return 0;
}

function nudm_sdm_subscribe_nf_filter(smf_host, scp, traffic) {
    if (traffic > 0) {
        //replace header
        const p_index = Math.floor(Math.random() * 4) + 3;
        headers_inter_udm_sdm_init_nf_filter.headers["3gpp-Sbi-Target-apiRoot"] = "http://eric-seppsim-p" + p_index + "-mcc-262-mnc-73";
        // console.log("updated header: " + headers_inter_udm_sdm_init_nf_filter.headers["3gpp-Sbi-Target-apiRoot"]);

        const method = "POST";

        let requests = [[]];

        let body = get_create_body(smf_host);

        //create the batch for traffic
        for (let i = 0; i < traffic; i++) {
            requests[i] = [method, scp + uri_nudm_sdm, body, headers_inter_udm_sdm_init_nf_filter];
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
                "servedGPSI": "msisdn-77117772",
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


