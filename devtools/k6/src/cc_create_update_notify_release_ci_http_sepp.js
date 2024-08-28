/**
 * @author xkonska
 */

import http from "k6/http";
import {
	check
} from "k6";

export let options = {
	rps: 100,
	duration: __ENV.DURATION,
	vus: 10,
	hosts: {
		[`${__ENV.SEPP_HOST}`]: `${__ENV.SEPP_IP}`,
	},
};

const uri = "nchf-convergedcharging/v1/chargingdata/";
const token = "Bearer " + `${__ENV.JWT}`;
const headers = {
	headers: {
		"Connection": "keep-alive",
		"Content-Type": "application/json",
		"Accept": "application/json",
		"Authorization": token
	}
};

export default function () {

	var sepp = `http://${__ENV.SEPP_HOST}:${__ENV.SEPP_PORT}/`;
	const smf_host = `smf${__VU}.ericsson.se`;

	let ids = create_operation(smf_host, sepp);
	if (ids.length > 0) {
		update_operation(ids, smf_host, sepp);
		release_operation(ids, sepp);
	}
}



//Converged charging create operation
function create_operation(smf_host, sepp) {

	let create = `${__ENV.CC_CREATE}`;
	if (create > 0) {
		const method = "POST";

		let requests = [[]];

		let create_body = get_create_body(smf_host);

		//create the batch for create operation
		for (let i = 0; i < create; i++) {
			requests[i] = [method, sepp + uri, create_body, headers];
		}

		let responses = http.batch(requests);

		let resp_length = Object.keys(responses).length;
		for (let i = 0; i < resp_length; i++) {
			check(responses[i], {
				"cc create: protocol is HTTP/1.1": (r) => r.proto === 'HTTP/1.1',
				"cc create: status was 201": (r) => r.status == 201
			});
		}

		let ids = process_response_headers(responses);
		return ids;
	}
	return 0;
}

function process_response_headers(responses) {

	let resp_length = Object.keys(responses).length;

	let ids = [];
	for (let i = 0; i < resp_length; i++) {
		let location = responses[i].headers["Location"];
		if (location != null && location.includes("occ")) {
			let array = location.split("/");
			ids[i] = array[array.length - 1];
		}
	}
	return ids;
}


//converged charging update operation
function update_operation(ids, smf_host, sepp) {

	let update = `${__ENV.CC_UPDATE}`;

	if (update > 0) {
		const method = "POST";

		let requests = [[]];

		let update_body = get_update_body(smf_host);

		let counter = 0; // counter to check that we do not get out of bounds for create requests

		for (let i = 0; i < update; i++) {
			if (counter >= ids.length) {
				counter = 0;
			}
			requests[i] = [method, sepp + uri + ids[counter] + "/update", update_body, headers];
			counter++;
		}
		//sending 100 update requests in parallel in every iteration. This batch is executed by each VU.
		let responses = http.batch(requests);
		let resp_length = Object.keys(responses).length;

		for (let i = 0; i < resp_length; i++) {
			check(responses[i], {
				"cc update: protocol is HTTP/1.1": (r) => r.proto === 'HTTP/1.1',
				"cc update: status was 200": (r) => r.status == 200
			});
		}
	}
}

//converged charging release operation
function release_operation(ids, sepp) {

	let release = `${__ENV.CC_RELEASE}`;
	if (release > 0) {
		const method = "POST";

		let requests = [[]];

		for (let i = 0; i < release; i++) {
			requests[i] = [method, sepp + uri + ids[i] + "/release", {}, headers];
		}

		//sending 100 release requests in parallel in every iteration. This batch is executed by each VU.
		let responses = http.batch(requests);
		let resp_length = Object.keys(responses).length;

		for (let i = 0; i < resp_length; i++) {
			check(responses[i], {
				"cc release: protocol is HTTP/1.1": (r) => r.proto === 'HTTP/1.1',
				"cc release: status was 204": (r) => r.status == 204
			});
		}
	}
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
				"servedGPSI": "msisdn-77117778",
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
				"supi": "nai-771177@ericsson.com"
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
					"servedGPSI": "msisdn-77117778",
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