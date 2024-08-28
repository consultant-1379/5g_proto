/**
 * @author eyossan
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
        cert: open("/certs/K6.crt"),
        key: open("/certs/K6.key")
    }],
    vus: 1,
    duration: 10
};

export default function() {

    const smf_host = `smf${__VU}.ericsson.se`;
    const supi = `nai-${__VU}${__ITER}@ericsson.com`;
    const csa = `https://${__ENV.SCP_HOST}:${__ENV.SCP_PORT}/`;

    //Converged charging create operation

    const method = "POST";
    const uri = "nchf-convergedcharging/v1/chargingdata"
    const headers = {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json"
        }
    }
    const body = JSON.stringify({
        "subscriberIdentifier": {
            "subscriberIdentityType": "SUPI",
            "supi": "nai-77117777@ericsson.com"
            //  "supi": supi
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
        //"notifyUri":"http://${OCC_IP}:8080/rar",
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

    let responses = http.batch([

        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers],
        [method, csa + uri, body, headers]

    ]);

    for(var i=0;i<100;i++)
    {
       check(responses[i], {
         "protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
         "status was 201": (r) => r.status == 201
       });
    }
}
