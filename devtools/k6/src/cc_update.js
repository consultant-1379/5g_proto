/**
 * @author eyossan
 */

import http from "k6/http";
import {
    check
} from "k6";

export let options = {
    hosts: {
        "csa.ericsson.se": "10.210.156.5"
    },
    tlsAuth: [{
        domains: ["csa.ericsson.se"],
        cert: open("/certs/K6.crt"),
        key: open("/certs/K6.key")
    }],
    vus: 10,
    duration: "3m"
};

//initialise chargingdata refs
var occ1_ref = 10000000;
var occ2_ref = 20000000;
var occ3_ref = 30000000;


export function setup() {
    // setup code
}

//converged charging update operation
export default function() {

    const smf_ip = `192.168.0.${__VU}`;
    const supi = `nai-${__VU}${__ITER}@ericsson.com`;
    const csa = "https://csa.ericsson.se:30867/";
    const method = "POST";

    const occ_uri_1 = "nchf-convergedcharging/v1/chargingdata/occ1-"
    const occ_uri_2 = "nchf-convergedcharging/v1/chargingdata/occ2-"
    const occ_uri_3 = "nchf-convergedcharging/v1/chargingdata/occ3-"

    //console.log(csa + occ_uri_1 + occ1_ref + "/update" + "-----OCC1---")
    //console.log(csa + occ_uri_2 + occ2_ref + "/update" + "----OCC2----")
    //console.log(csa + occ_uri_3 + occ3_ref + "/update" + "----OCC3----")

    const headers = {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
            "Accept": "application/json"
        }
    }

    const body = JSON.stringify(

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
            "notifyUri": "http://${OCC_IP}:8080/rar",
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

    //sending 100 update requests in parallel in every iteration. This batch is executed by each VU.
    let responses = http.batch([
        [method, csa + occ_uri_1 + (occ1_ref + 1) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 1) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 1) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 2) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 2) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 2) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 3) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 3) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 3) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 4) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 4) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 4) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 5) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 5) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 5) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 6) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 6) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 6) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 7) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 7) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 7) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 8) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 8) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 8) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 9) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 9) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 9) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 10) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 10) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 10) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 11) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 11) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 11) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 12) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 12) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 12) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 13) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 13) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 13) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 14) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 14) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 14) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 15) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 15) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 15) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 16) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 16) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 16) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 17) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 17) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 17) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 18) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 18) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 18) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 19) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 19) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 19) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 20) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 20) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 20) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 21) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 21) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 21) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 22) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 22) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 22) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 23) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 23) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 23) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 24) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 24) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 24) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 25) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 25) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 25) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 26) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 26) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 26) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 27) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 27) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 27) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 28) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 28) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 28) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 29) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 29) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 29) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 30) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 30) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 30) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 31) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 31) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 31) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 32) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 32) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 32) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 33) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 33) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 33) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 34) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 34) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 34) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 35) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 35) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 35) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 36) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 36) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 36) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 37) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 37) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 37) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 38) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 38) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 38) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 39) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 39) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 39) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 40) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 40) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 40) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 41) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 41) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 41) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 42) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 42) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 42) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 43) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 43) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 43) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 44) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 44) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 44) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 45) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 45) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 45) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 46) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 46) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 46) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 47) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 47) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 47) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 48) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 48) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 48) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 49) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 49) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 49) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 50) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 50) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 50) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 51) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 51) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 51) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 52) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 52) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 52) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 53) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 53) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 53) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 54) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 54) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 54) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 55) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 55) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 55) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 56) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 56) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 56) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 57) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 57) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 57) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 58) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 58) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 58) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 59) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 59) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 59) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 60) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 60) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 60) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 61) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 61) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 61) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 62) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 62) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 62) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 63) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 63) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 63) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 64) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 64) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 64) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 65) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 65) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 65) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 66) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 66) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 66) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 67) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 67) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 67) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 68) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 68) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 68) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 69) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 69) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 69) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 70) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 70) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 70) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 71) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 71) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 71) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 72) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 72) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 72) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 73) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 73) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 73) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 74) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 74) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 74) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 75) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 75) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 75) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 76) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 76) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 76) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 77) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 77) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 77) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 78) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 78) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 78) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 79) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 79) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 79) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 80) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 80) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 80) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 81) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 81) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 81) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 82) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 82) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 82) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 83) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 83) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 83) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 84) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 84) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 84) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 85) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 85) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 85) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 86) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 86) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 86) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 87) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 87) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 87) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 88) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 88) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 88) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 89) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 89) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 89) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 90) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 90) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 90) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 91) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 91) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 91) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 92) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 92) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 92) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 93) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 93) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 93) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 94) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 94) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 94) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 95) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 95) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 95) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 96) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 96) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 96) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 97) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 97) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 97) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 98) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 98) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 98) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 99) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 99) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 99) + "/update", body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 100) + "/update", body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 100) + "/update", body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 100) + "/update", body, headers]

    ]);

    updateChargingDataRefs((occ1_ref + 100), (occ2_ref + 100), (occ3_ref + 100))

    check(responses[0], {
        "protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
        "status was 200": (r) => r.status == 200
    });

}

//re-initialise chargingdata ref for the next batch
function updateChargingDataRefs(occ1_updated, occ2_updated, occ3_updated) {

    occ1_ref = occ1_updated;
    occ2_ref = occ2_updated;
    occ3_ref = occ3_updated;

}

export function teardown() {
    //clean up code
}
