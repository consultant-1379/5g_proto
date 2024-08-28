/**
 * @author zathsok
 */

import http from "k6/http";
import {
    check
} from "k6";

const uri_cc = "nchf-convergedcharging/v2/chargingdata/";
const uri_slc = "nchf-spendinglimitcontrol/v1/subscriptions/";
const uri_nudm_sdm = "nudm-sdm/v2/shared-data-subscriptions";
const nfPeerInfoEnabled = `${__ENV.NF_PEER_INFO_ENABLED}`;

const scpIps = [`https://${__ENV.SCP_IP}:${__ENV.SCP_PORT}/`];
if (__ENV.SCP_IP6 !== "null")
{
    scpIps.push(`https://[${__ENV.SCP_IP6}]:${__ENV.SCP_PORT}/`);
}

const headers_inter_udm_sdm_init_10 = {
    headers: {
        "Connection": "keep-alive",
        "Content-Type": "application/json",
        "Accept": "application/json",
        "3gpp-sbi-discovery-target-plmn-list": "\[\{\"mcc\":\"412\",\"mnc\":\"088\"\}\]",
        "3gpp-sbi-discovery-target-nf-type": "UDM",
        "3gpp-sbi-discovery-requester-nf-type": "SMF",
        "3gpp-sbi-discovery-service-names": "nudm-sdm",
        "3gpp-sbi-discovery-requester-plmn-list": "\[\{\"mnc\":\"012\",\"mcc\":\"380\"\}\]",
        "3gpp-sbi-discovery-target-nf-set-id": "setD"
    }
};

const headers_inter_nudm_sdm_subseq_20 = {
    headers: {
        "Connection": "keep-alive",
        "Content-Type": "application/json",
        "Accept": "application/json",
        "3gpp-Sbi-Target-apiRoot": "",
        "3gpp-sbi-Routing-Binding": "bl=nf-set;nfset=setD",
    }
};

const headers_inter_nudm_sdm_subseq_30 = {
    headers: {
        "Connection": "keep-alive",
        "Content-Type": "application/json",
        "Accept": "application/json",
        "3gpp-Sbi-Target-apiRoot": "",
        "3gpp-sbi-discovery-target-nf-type": "UDM",
        "3gpp-sbi-discovery-requester-nf-type": "SMF",
        "3gpp-sbi-discovery-target-nf-set-id": "setD"
    }
};

const headers_inter_nudm_sdm_init_c_45 = {
    headers: {
        "Connection": "keep-alive",
        "Content-Type": "application/json",
        "Accept": "application/json",
        "3gpp-Sbi-Target-apiRoot": "http://eric-seppsim-p8",
        "3gpp-sbi-discovery-target-nf-set-id": "setD",
        "3gpp-Sbi-Discovery-target-nf-type": "UDM",
        "3gpp-sbi-discovery-target-plmn-list": "\[\{\"mcc\":\"412\",\"mnc\":\"088\"\}\]",
    }
};

const headers_cc_create_50 = {
    headers: {
        "Connection": "keep-alive",
        "Content-Type": "application/json",
        "Accept": "application/json",
        "3gpp-sbi-discovery-target-nf-type": "CUSTOM_CHF",
        "3gpp-sbi-discovery-requester-nf-type": "SMF",
        "3gpp-sbi-discovery-service-names": "nchf-convergedcharging"
    }
};

const headers_slc_subscribe_60 = {
    headers: {
        "Connection": "keep-alive",
        "Content-Type": "application/json",
        "Accept": "application/json",
        "3gpp-sbi-discovery-target-nf-type": "CUSTOM_CHF",
        "3gpp-sbi-discovery-requester-nf-type": "PCF",
        "3gpp-sbi-discovery-service-names": "nchf-spendinglimitcontrol",
        "3gpp-sbi-discovery-preferred-locality": "Canada"
    }
};

const headers_cc_update_100_130 = {
    headers: {
        "Connection": "keep-alive",
        "Content-Type": "application/json",
        "Accept": "application/json",
        "3gpp-Sbi-Target-apiRoot": "",
        "3gpp-sbi-Routing-Binding": "bl=nf-set;nfset=setA",
        "3gpp-sbi-discovery-service-names": "nchf-convergedcharging"
    }
};

const headers_cc_update_110_140 = {
    headers: {
        "Connection": "keep-alive",
        "Content-Type": "application/json",
        "Accept": "application/json",
        "3gpp-Sbi-Target-apiRoot": "",
        "3gpp-sbi-discovery-target-nf-type": "CUSTOM_CHF",
        "3gpp-sbi-discovery-requester-nf-type": "SMF",
        "3gpp-sbi-discovery-target-nf-set-id": "setA",
        "3gpp-sbi-discovery-service-names": "nchf-convergedcharging"
    }
};

const headers_intra_udm_sdm_init_150_160 = {
    headers: {
        "Connection": "keep-alive",
        "Content-Type": "application/json",
        "Accept": "application/json",
        "3gpp-sbi-discovery-target-nf-type": "UDM",
        "3gpp-sbi-discovery-requester-nf-type": "SMF",
        "3gpp-sbi-discovery-service-names": "nudm-sdm",
        "3gpp-sbi-discovery-preferred-locality": "Canada",
        "3gpp-sbi-discovery-target-nf-set-id": "setC"
    }
};

const headers_intra_udm_sdm_subseq_170 = {
    headers: {
        "Connection": "keep-alive",
        "Content-Type": "application/json",
        "Accept": "application/json",
        "3gpp-Sbi-Target-apiRoot": "",
        "3gpp-sbi-Routing-Binding": "bl=nf-set;nfset=setC"
    }
};

const headers_intra_udm_sdm_subseq_180 = {
    headers: {
        "Connection": "keep-alive",
        "Content-Type": "application/json",
        "Accept": "application/json",
        "3gpp-Sbi-Target-apiRoot": "",
        "3gpp-sbi-discovery-target-nf-type": "UDM",
        "3gpp-sbi-discovery-requester-nf-type": "SMF",
        "3gpp-sbi-discovery-target-nf-set-id": "setC"
    }
};

//Update headers in case of 3gpp-sbi-nf-peer-info
if (nfPeerInfoEnabled === "true") {

    let sbiNfPeerInfoHeader = `srcinst=2ec8ac0b-265e-4165-86e9-e0735e6ce307;dstinst=2ec8ac0b-265e-4165-86e9-e0735e6ce309;dstscp=${__ENV.SCP_HOST}`;

    headers_inter_udm_sdm_init_10["headers"]["3gpp-sbi-nf-peer-info"] = sbiNfPeerInfoHeader;
    headers_inter_nudm_sdm_subseq_20["headers"]["3gpp-sbi-nf-peer-info"] = sbiNfPeerInfoHeader;
    headers_inter_nudm_sdm_subseq_30["headers"]["3gpp-sbi-nf-peer-info"] = sbiNfPeerInfoHeader;
    headers_inter_nudm_sdm_init_c_45["headers"]["3gpp-sbi-nf-peer-info"] = sbiNfPeerInfoHeader;
    headers_cc_create_50["headers"]["3gpp-sbi-nf-peer-info"] = sbiNfPeerInfoHeader;
    headers_slc_subscribe_60["headers"]["3gpp-sbi-nf-peer-info"] = sbiNfPeerInfoHeader;
    headers_cc_update_100_130["headers"]["3gpp-sbi-nf-peer-info"] = sbiNfPeerInfoHeader;
    headers_cc_update_110_140["headers"]["3gpp-sbi-nf-peer-info"] = sbiNfPeerInfoHeader;
    headers_intra_udm_sdm_init_150_160["headers"]["3gpp-sbi-nf-peer-info"] = sbiNfPeerInfoHeader;
    headers_intra_udm_sdm_subseq_170["headers"]["3gpp-sbi-nf-peer-info"] = sbiNfPeerInfoHeader;
    headers_intra_udm_sdm_subseq_180["headers"]["3gpp-sbi-nf-peer-info"] = sbiNfPeerInfoHeader;
}


export default function () {

    const smf_host = `smf${__VU}.ericsson.se`;
    const scp = scpIps[Math.floor(Math.random() * scpIps.length)];
	
    let res_inter = nudm_sdm_subscribe_10(smf_host, scp);
    if (__ENV.NUDM_SDM_INTER_SUBSCRIBE_OPTION_D > 0 && res_inter.get("ids").length > 0) {
        nudm_sdm_unsubscribe_20_30(res_inter, scp);
    }

    nudm_sdm_subscribe_c_45(smf_host, scp); 

    slc_subscribe_60(smf_host, scp);
    let res_cc = cc_create_50(smf_host, scp);
    if (__ENV.CC_CREATE_OPTION_D > 0 && res_cc.get("ids").length > 0) {
        cc_update_100_130_110_140(res_cc, smf_host, scp);
    }

    let res_sdm = nudm_sdm_subscribe_150_160(smf_host, scp);
    if (__ENV.NUDM_SDM_SUBSCRIBE_OPTION_D > 0 && res_sdm.get("ids").length > 0) {
        nudm_sdm_unsubscribe_170_180(res_sdm, scp);
    }
}

function nudm_sdm_subscribe_10(smf_host, scp) {
    let traffic = `${__ENV.NUDM_SDM_INTER_SUBSCRIBE_OPTION_D}`;

    if (traffic > 0) {

        const method = "POST";

        let requests = [[]];

        let body = get_create_body(smf_host);

        //create the batch for traffic
        for (let i = 0; i < traffic; i++) {
            requests[i] = [method, scp + uri_nudm_sdm, body, headers_inter_udm_sdm_init_10];
        }

        let responses = http.batch(requests);

        let resp_length = Object.keys(responses).length;
        for (let i = 0; i < resp_length; i++) {
            check(responses[i], {
                "Option-D NUDM-SDM (inter) Subscribe traffic: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
                "Option-D NUDM-SDM (inter) Subscribe traffic: status was 201": (r) => r.status == 201
            });
        }

        let res = process_response_headers_subscriptionId(responses);
        return res;

    }
    return 0;
}

function nudm_sdm_unsubscribe_20_30(res, scp) {

    let ids = res.get("ids");
    let tars = res.get("tars");

    let traffic = ids.length;

    if (traffic > 0) {
        const method = "DELETE";

        let requests = [[]];

        //create the batch for traffic
        for (let i = 0; i < traffic; i++) {
            // Update the tar header
            if (i % 2 === 0) {
                headers_inter_nudm_sdm_subseq_20.headers["3gpp-Sbi-Target-apiRoot"] = "http://" + tars[i];
                requests[i] = [method, scp + uri_nudm_sdm + "/" + ids[i], null, headers_inter_nudm_sdm_subseq_20];
            }
            else {
                headers_inter_nudm_sdm_subseq_30.headers["3gpp-Sbi-Target-apiRoot"] = "http://" + tars[i];
                requests[i] = [method, scp + uri_nudm_sdm + "/" + ids[i], null, headers_inter_nudm_sdm_subseq_30];
            }

        }
        let responses = http.batch(requests);

        let resp_length = Object.keys(responses).length;
        for (let i = 0; i < resp_length; i++) {
            check(responses[i], {
                "Option-D NUDM-SDM (inter) Unsubscribe traffic: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
                "Option-D NUDM-SDM (inter) Unsubscribe traffic: status was 204": (r) => r.status == 204
            });
        }

    }
    return 0;
}

function nudm_sdm_subscribe_c_45(smf_host, scp) {
    let traffic = `${__ENV.NUDM_SDM_INTER_SUBSCRIBE_OPTION_C}`;

    if (traffic > 0) {

        const method = "POST";

        let requests = [[]];

        let body = get_create_body(smf_host);

        //create the batch for traffic
        for (let i = 0; i < traffic; i++) {
            requests[i] = [method, scp + uri_nudm_sdm, body, headers_inter_nudm_sdm_init_c_45];
        }

        let responses = http.batch(requests);

        let resp_length = Object.keys(responses).length;
        for (let i = 0; i < resp_length; i++) {
            check(responses[i], {
                "Option-C NUDM-SDM (inter) Subscribe traffic: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
                "Option-C NUDM-SDM (inter) Subscribe traffic: status was 201": (r) => r.status == 201
            });
        }

        let res = process_response_headers_subscriptionId(responses);
        return res;

    }
    return 0;
}

//Converged charging create operation
function cc_create_50(smf_host, scp) {

    let traffic = `${__ENV.CC_CREATE_OPTION_D}`;
    if (traffic > 0) {
        const method = "POST";

        let requests = [[]];

        let create_body = get_create_body(smf_host);

        //create the batch for create operation
        for (let i = 0; i < traffic; i++) {
            requests[i] = [method, scp + uri_cc, create_body, headers_cc_create_50];
        }

        let responses = http.batch(requests);

        let resp_length = Object.keys(responses).length;
        for (let i = 0; i < resp_length; i++) {
            check(responses[i], {
                "Option-D CC Create: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
                "Option-D CC Create: status was 201": (r) => r.status == 201
            });
        }

        let res = process_response_headers(responses);

        return res;

    }
    return 0;
}

//spending limit control subscribe operation
function slc_subscribe_60(smf_host, scp) {

    let traffic = `${__ENV.SLC_SUBSCRIBE_OPTION_D}`;
    if (traffic > 0) {

        const method = "POST";

        let requests = [[]];

        const body = JSON.stringify({
            "supi": "imsi-310310140000120",
            "gpsi": "msisdn-12001400120",
            "policyCounterIds": ["71008", "71009", "71300", "71000"],
            "notifyUri": "http://" + smf_host + ":8080/rar"
        }, null, "   ");


        for (let i = 0; i < traffic; i++) {
            requests[i] = [method, scp + uri_slc, body, headers_slc_subscribe_60];
        }

        let responses = http.batch(requests);

        let resp_length = Object.keys(responses).length;
        for (let i = 0; i < resp_length; i++) {
            check(responses[i], {
                "Option-D SLC Subscribe: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
                "Option-D SLC Subscribe: status was 201": (r) => r.status == 201
            });
        }

        let res = process_response_headers(responses);

        return res;
    }
    return 0;
}

//converged charging update operation
function cc_update_100_130_110_140(res, smf_host, scp) {

    let ids = res.get("ids");
    let tars = res.get("tars");
    let setIds = res.get("setIds");

    let traffic = ids.length;

    if (traffic > 0) {
        const method = "POST";

        let requests = [[]];

        let update_body = get_update_body(smf_host);

        for (let i = 0; i < traffic; i++) {
            if (i % 2 === 0) {
                // Update the headers
                headers_cc_update_100_130.headers["3gpp-Sbi-Target-apiRoot"] = tars[i];
                //headers_cc_update_100_130.headers["3gpp-sbi-Routing-Binding"] = "bl=nf-set;nfset=" + setIds[i];

                requests[i] = [method, scp + uri_cc + ids[i] + "/update", update_body, headers_cc_update_100_130];
            }
            else {
                // Update the headers
                headers_cc_update_110_140.headers["3gpp-Sbi-Target-apiRoot"] = tars[i];
                //headers_cc_update_110_140.headers["3gpp-sbi-discovery-target-nf-set-id"] = setIds[i]

                requests[i] = [method, scp + uri_cc + ids[i] + "/update", update_body, headers_cc_update_110_140];
            }
        }
        let responses = http.batch(requests);
        let resp_length = Object.keys(responses).length;

        for (let i = 0; i < resp_length; i++) {
            check(responses[i], {
                "Option-D CC Update: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
                "option-D CC Update: status was 200": (r) => r.status == 200
            });
        }
    }
}

function nudm_sdm_subscribe_150_160(smf_host, scp) {

    let traffic = `${__ENV.NUDM_SDM_SUBSCRIBE_OPTION_D}`;

    if (traffic > 0) {

        const method = "POST";

        let requests = [[]];

        let body = get_create_body(smf_host);

        //create the batch for traffic
        for (let i = 0; i < traffic; i++) {
            requests[i] = [method, scp + uri_nudm_sdm, body, headers_intra_udm_sdm_init_150_160];
        }

        let responses = http.batch(requests);

        let resp_length = Object.keys(responses).length;
        for (let i = 0; i < resp_length; i++) {
            check(responses[i], {
                "Option-D NUDM-SDM Subscribe traffic: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
                "Option-D NUDM-SDM Subscribe traffic: status was 201": (r) => r.status == 201
            });
        }

        let res = process_response_headers_subscriptionId(responses);
        return res;

    }
    return 0;
}

function nudm_sdm_unsubscribe_170_180(res, scp) {

    let ids = res.get("ids");
    let tars = res.get("tars");

    let traffic = ids.length;

    if (traffic > 0) {
        const method = "DELETE";

        let requests = [[]];

        //create the batch for traffic
        for (let i = 0; i < traffic; i++) {
            // Update the tar header
            if (i % 2 === 0) {
                headers_intra_udm_sdm_subseq_170.headers["3gpp-Sbi-Target-apiRoot"] = "http://" + tars[i];
                requests[i] = [method, scp + uri_nudm_sdm + "/" + ids[i], null, headers_intra_udm_sdm_subseq_170];
            }
            else {
                headers_intra_udm_sdm_subseq_180.headers["3gpp-Sbi-Target-apiRoot"] = "http://" + tars[i];
                requests[i] = [method, scp + uri_nudm_sdm + "/" + ids[i], null, headers_intra_udm_sdm_subseq_180];
            }

        }
        let responses = http.batch(requests);

        let resp_length = Object.keys(responses).length;
        for (let i = 0; i < resp_length; i++) {
            check(responses[i], {
                "Option-D NUDM-SDM Unsubscribe traffic: protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
                "Option-D NUDM-SDM Unsubscribe traffic: status was 204": (r) => r.status == 204
            });
        }

    }
    return 0;
}

function process_response_headers_subscriptionId(responses) {

    let ids = [];
    let tars = [];

    let resp_length = Object.keys(responses).length;

    for (let i = 0; i < resp_length; i++) {

        let location = responses[i].headers["Location"];
        let xorigin = responses[i].headers["X-Origin"];

        if (location != null && location.includes("subscription-")) {
            let array = location.split("/");
            ids[i] = array[array.length - 1];

            array = xorigin.split("-");
            tars[i] = array.slice(0, 3).join("-");
        }
    }

    var resultsHashMap = new Map();
    resultsHashMap.set("ids", ids);
    resultsHashMap.set("tars", tars);

    return resultsHashMap;
}

function process_response_headers(responses) {

    let resp_length = Object.keys(responses).length;

    let ids = [];
    let tars = [];
    let setIds = [];

    for (let i = 0; i < resp_length; i++) {
        let location = responses[i].headers["Location"];
        if (location != null && location.includes("occ") || location.includes("p9") || location.includes("p10") || location.includes("p11") || location.includes("p12")) {
            let array = location.split("/");
            ids[i] = array[array.length - 1];
            tars[i] = array.slice(0, 3).join('/');

            if (location.endsWith("p9") || location.endsWith("p10")) {
                setIds[i] = "setA";
            }
            else if (location.endsWith("p11") || location.endsWith("p12")) {
                setIds[i] = "setB";
            }
        }
    }

    var resultsHashMap = new Map();
    resultsHashMap.set("ids", ids);
    resultsHashMap.set("tars", tars);
    resultsHashMap.set("setIds", setIds);

    return resultsHashMap;
}

function get_create_body(smf_host) {
    return JSON.stringify({
        "subscriberIdentifier": {
            "subscriberIdentityType": "SUPI",
            "supi": "nai-77117777@ericsson.com"
        },
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
        "notifyUri": "http://" + smf_host + ":8080/rar",
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
                "servedGPSI": "msisdn-77117771",
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

function get_update_body(smf_host) {
    return JSON.stringify(
        {
            "subscriberIdentifier": {
                "subscriberIdentityType": "SUPI",
                "supi": "nai-77117777@ericsson.com"
            },
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
            "invocationTimeStamp": "2019-03-28T14:30:51Z",
            "invocationSequenceNumber": 1,
            "notifyUri": "http://" + smf_host + ":8080/rar",
            "multipleUnitUsage": [{
                "ratingGroup": 100,
                "requestedUnit": {
                    "time": 123,
                    "totalVolume": 211,
                    "uplinkVolume": 123,
                    "downlinkVolume": 1234,
                    "serviceSpecificUnits": 6543
                },
                "UsedUnitContainer": [{
                    "quotaManagementIndicator": "ONLINE_CHARGING",
                    "triggers": [{
                        "triggerType": "QUOTA_THRESHOLD",
                        "triggerCategory": "IMMEDIATE_REPORT",
                        "timeLimit": 1234,
                        "volumeLimit": 12345,
                        "maxNumberOfccc": 12
                    }],
                    "triggerTimestamp": "2019-03-28T14:30:51Z",
                    "time": 12345,
                    "eventTimeStamps": "2019-03-28T14:30:51Z",
                    "localSequenceNumber": 0,
                    "pDUContainerInformation": {
                        "timeofFirstUsage": "2019-03-28T14:30:51Z",
                        "timeofLastUsage": "2019-03-28T14:30:51Z",
                        "qoSInformation": {
                            "5qi": 45
                        },
                        "aFCorrelationInformation": "Correlation_Info",
                        "userLocationInformation": {
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
                                "ageOfLocationInformation": 1,
                                "ueLocationTimestamp": "2019-03-28T14:30:51Z",
                                "geographicalInformation": "AB12334765498F12",
                                "geodeticInformation": "ABCDEFAB123456789023",
                                "globalNgenbId": {
                                    "plmnId": {
                                        "mcc": "374",
                                        "mnc": "645"
                                    },
                                    "n3IwfId": "ABCabc123",
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
                                "ueLocationTimestamp": "2019-03-28T14:30:51Z",
                                "geographicalInformation": "AB12334765498F12",
                                "geodeticInformation": "AB12334765498F12ACBF",
                                "globalGnbId": {
                                    "plmnId": {
                                        "mcc": "374",
                                        "mnc": "645"
                                    },
                                    "n3IwfId": "ABCabc123",
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
                                "n3IwfId": "ABCabc123",
                                "ueIpv4Addr": "192.168.0.1",
                                "ueIpv6Addr": "2001:db8:85a3:8d3:1319:8a2e:370:7348",
                                "portNumber": 1
                            }
                        },
                        "uetimeZone": "+05:30",
                        "rATType": "NR",
                        "servingNodeID": [{
                            "plmnId": {
                                "mcc": "311",
                                "mnc": "280"
                            },
                            "amfId": "ABab09"
                        }],
                        "presenceReportingAreaInformation": {
                            "additional": {
                                "praId": "PRA_ID",
                                "presenceState": "IN_AREA",
                                "trackingAreaList": [{
                                    "plmnId": {
                                        "mcc": "374",
                                        "mnc": "645"
                                    },
                                    "tac": "ab01"
                                }],
                                "ecgiList": [{
                                    "plmnId": {
                                        "mcc": "374",
                                        "mnc": "645"
                                    },
                                    "eutraCellId": "abcAB12"
                                }],
                                "ncgiList": [{
                                    "plmnId": {
                                        "mcc": "374",
                                        "mnc": "645"
                                    },
                                    "nrCellId": "ABCabc123"
                                }],
                                "globalRanNodeIdList": [{
                                    "plmnId": {
                                        "mcc": "311",
                                        "mnc": "280"
                                    },
                                    "n3IwfId": "ABCabc123",
                                    "ngRanNodeId": "MacroNGeNB-abc92"
                                }]
                            }
                        },
                        "sponsorIdentity": "SP_Id",
                        "applicationserviceProviderIdentity": "ASP_ID",
                        "3gppPSDataOffStatus": "ACTIVE",
                        "chargingRuleBaseName": "PCC_Rules"
                    }
                }],
                "uPFID": "123e-e8b-1d3-a46-421"
            }],
            "pDUSessionChargingInformation": {
                "chargingId": 123,
                "userInformation": {
                    "servedGPSI": "msisdn-77117771",
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
                        "ueLocationTimestamp": "2019-03-28T14:30:51Z",
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
                        "ueLocationTimestamp": "2019-03-28T14:30:51Z",
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
                "userLocationTime": "2019-03-28T14:30:51Z",
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

function formatIpv6Address(addr) {
    if (addr.includes(":")) {
        return '[' + addr + ']'
    }
    return addr
}
