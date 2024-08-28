/**
 * @author enubars
 *
 *      
 *
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
    iterations: 1
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
    const csa = "https://csa.ericsson.se:31793/";
    const method = "PUT";

    const occ_uri_1 = "nchf-spendinglimitcontrol/v1/subscriptions/occ1-"
    const occ_uri_2 = "nchf-spendinglimitcontrol/v1/subscriptions/occ2-"
    const occ_uri_3 = "nchf-spendinglimitcontrol/v1/subscriptions/occ3-"

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

    const body = JSON.stringify({
        "statusInfos" : {
           "71008" : {
              "policyCounterId":"71008",
              "currentStatus":"active"
           },
           "71009":{
              "policyCounterId":"71009",
              "currentStatus":"active"
           },
          "71300":{
             "policyCounterId":"71300",
             "currentStatus":"active"
           },
           "71000":{
              "policyCounterId":"71000",
              "currentStatus":"active"
           }
        },
        "supi":"imsi-310310140000120"
        }, null, "   ");

    //sending 100 update requests in parallel in every iteration. This batch is executed by each VU.
    let responses = http.batch([
        [method, csa + occ_uri_1 + (occ1_ref + 1) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 1) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 1) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 2) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 2) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 2) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 3) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 3) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 3) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 4) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 4) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 4) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 5) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 5) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 5) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 6) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 6) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 6) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 7) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 7) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 7) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 8) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 8) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 8) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 9) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 9) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 9) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 10) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 10) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 10) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 11) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 11) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 11) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 12) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 12) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 12) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 13) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 13) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 13) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 14) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 14) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 14) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 15) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 15) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 15) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 16) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 16) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 16) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 17) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 17) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 17) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 18) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 18) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 18) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 19) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 19) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 19) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 20) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 20) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 20) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 21) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 21) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 21) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 22) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 22) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 22) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 23) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 23) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 23) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 24) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 24) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 24) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 25) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 25) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 25) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 26) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 26) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 26) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 27) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 27) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 27) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 28) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 28) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 28) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 29) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 29) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 29) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 30) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 30) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 30) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 31) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 31) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 31) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 32) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 32) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 32) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 33) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 33) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 33) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 34) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 34) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 34) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 35) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 35) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 35) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 36) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 36) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 36) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 37) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 37) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 37) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 38) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 38) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 38) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 39) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 39) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 39) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 40) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 40) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 40) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 41) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 41) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 41) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 42) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 42) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 42) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 43) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 43) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 43) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 44) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 44) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 44) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 45) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 45) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 45) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 46) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 46) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 46) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 47) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 47) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 47) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 48) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 48) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 48) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 49) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 49) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 49) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 50) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 50) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 50) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 51) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 51) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 51) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 52) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 52) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 52) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 53) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 53) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 53) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 54) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 54) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 54) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 55) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 55) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 55) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 56) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 56) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 56) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 57) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 57) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 57) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 58) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 58) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 58) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 59) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 59) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 59) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 60) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 60) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 60) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 61) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 61) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 61) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 62) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 62) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 62) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 63) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 63) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 63) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 64) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 64) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 64) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 65) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 65) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 65) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 66) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 66) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 66) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 67) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 67) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 67) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 68) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 68) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 68) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 69) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 69) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 69) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 70) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 70) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 70) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 71) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 71) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 71) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 72) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 72) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 72) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 73) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 73) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 73) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 74) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 74) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 74) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 75) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 75) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 75) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 76) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 76) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 76) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 77) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 77) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 77) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 78) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 78) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 78) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 79) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 79) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 79) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 80) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 80) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 80) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 81) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 81) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 81) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 82) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 82) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 82) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 83) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 83) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 83) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 84) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 84) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 84) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 85) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 85) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 85) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 86) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 86) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 86) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 87) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 87) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 87) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 88) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 88) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 88) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 89) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 89) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 89) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 90) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 90) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 90) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 91) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 91) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 91) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 92) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 92) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 92) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 93) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 93) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 93) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 94) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 94) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 94) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 95) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 95) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 95) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 96) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 96) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 96) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 97) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 97) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 97) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 98) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 98) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 98) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 99) , body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 99) , body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 99) , body, headers],
        [method, csa + occ_uri_1 + (occ1_ref + 100), body, headers],
        [method, csa + occ_uri_2 + (occ2_ref + 100), body, headers],
        [method, csa + occ_uri_3 + (occ3_ref + 100), body, headers]

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
