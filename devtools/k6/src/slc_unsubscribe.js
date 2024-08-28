/**
 * @author enubars
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
  //  duration: "3m"
};

//initialise chargingdata refs
var occ1_ref = 10000000;
var occ2_ref = 20000000;
var occ3_ref = 30000000;


export function setup() {
    // setup code
}


//converged charging release operation
export default function() {

    const smf_ip = `192.168.0.${__VU}`;
    const supi = `nai-${__VU}${__ITER}@ericsson.com`;
    const csa = "https://csa.ericsson.se:31793/";
    const method = "DELETE";

    const occ_uri_1 = "nchf-spendinglimitcontrol/v1/subscriptions/occ1-"
    const occ_uri_2 = "nchf-spendinglimitcontrol/v1/subscriptions/occ2-"
    const occ_uri_3 = "nchf-spendinglimitcontrol/v1/subscriptions/occ3-"

    //console.log(csa + occ_uri_1 + occ1_ref + "/delete" + "----OCC1----")
    //console.log(csa + occ_uri_2 + occ2_ref + "/delete" + "----OCC2----")
    //console.log(csa + occ_uri_3 + occ3_ref + "/delete" + "----OCC3----")

    //sending 100 release requests in parallel in every iteration. This batch is executed by each VU.
    let responses = http.batch([

        [method, csa + occ_uri_1 + (occ1_ref + 1) ],
        [method, csa + occ_uri_2 + (occ2_ref + 1) ],
        [method, csa + occ_uri_3 + (occ3_ref + 1) ],
        [method, csa + occ_uri_1 + (occ1_ref + 2) ],
        [method, csa + occ_uri_2 + (occ2_ref + 2) ],
        [method, csa + occ_uri_3 + (occ3_ref + 2) ],
        [method, csa + occ_uri_1 + (occ1_ref + 3) ],
        [method, csa + occ_uri_2 + (occ2_ref + 3) ],
        [method, csa + occ_uri_3 + (occ3_ref + 3) ],
        [method, csa + occ_uri_1 + (occ1_ref + 4) ],
        [method, csa + occ_uri_2 + (occ2_ref + 4) ],
        [method, csa + occ_uri_3 + (occ3_ref + 4) ],
        [method, csa + occ_uri_1 + (occ1_ref + 5) ],
        [method, csa + occ_uri_2 + (occ2_ref + 5) ],
        [method, csa + occ_uri_3 + (occ3_ref + 5) ],
        [method, csa + occ_uri_1 + (occ1_ref + 6) ],
        [method, csa + occ_uri_2 + (occ2_ref + 6) ],
        [method, csa + occ_uri_3 + (occ3_ref + 6) ],
        [method, csa + occ_uri_1 + (occ1_ref + 7) ],
        [method, csa + occ_uri_2 + (occ2_ref + 7) ],
        [method, csa + occ_uri_3 + (occ3_ref + 7) ],
        [method, csa + occ_uri_1 + (occ1_ref + 8) ],
        [method, csa + occ_uri_2 + (occ2_ref + 8) ],
        [method, csa + occ_uri_3 + (occ3_ref + 8) ],
        [method, csa + occ_uri_1 + (occ1_ref + 9) ],
        [method, csa + occ_uri_2 + (occ2_ref + 9) ],
        [method, csa + occ_uri_3 + (occ3_ref + 9) ],
        [method, csa + occ_uri_1 + (occ1_ref + 10) ],
        [method, csa + occ_uri_2 + (occ2_ref + 10) ],
        [method, csa + occ_uri_3 + (occ3_ref + 10) ],
        [method, csa + occ_uri_1 + (occ1_ref + 11) ],
        [method, csa + occ_uri_2 + (occ2_ref + 11) ],
        [method, csa + occ_uri_3 + (occ3_ref + 11) ],
        [method, csa + occ_uri_1 + (occ1_ref + 12) ],
        [method, csa + occ_uri_2 + (occ2_ref + 12) ],
        [method, csa + occ_uri_3 + (occ3_ref + 12) ],
        [method, csa + occ_uri_1 + (occ1_ref + 13) ],
        [method, csa + occ_uri_2 + (occ2_ref + 13) ],
        [method, csa + occ_uri_3 + (occ3_ref + 13) ],
        [method, csa + occ_uri_1 + (occ1_ref + 14) ],
        [method, csa + occ_uri_2 + (occ2_ref + 14) ],
        [method, csa + occ_uri_3 + (occ3_ref + 14) ],
        [method, csa + occ_uri_1 + (occ1_ref + 15) ],
        [method, csa + occ_uri_2 + (occ2_ref + 15) ],
        [method, csa + occ_uri_3 + (occ3_ref + 15) ],
        [method, csa + occ_uri_1 + (occ1_ref + 16) ],
        [method, csa + occ_uri_2 + (occ2_ref + 16) ],
        [method, csa + occ_uri_3 + (occ3_ref + 16) ],
        [method, csa + occ_uri_1 + (occ1_ref + 17) ],
        [method, csa + occ_uri_2 + (occ2_ref + 17) ],
        [method, csa + occ_uri_3 + (occ3_ref + 17) ],
        [method, csa + occ_uri_1 + (occ1_ref + 18) ],
        [method, csa + occ_uri_2 + (occ2_ref + 18) ],
        [method, csa + occ_uri_3 + (occ3_ref + 18) ],
        [method, csa + occ_uri_1 + (occ1_ref + 19) ],
        [method, csa + occ_uri_2 + (occ2_ref + 19) ],
        [method, csa + occ_uri_3 + (occ3_ref + 19) ],
        [method, csa + occ_uri_1 + (occ1_ref + 20) ],
        [method, csa + occ_uri_2 + (occ2_ref + 20) ],
        [method, csa + occ_uri_3 + (occ3_ref + 20) ],
        [method, csa + occ_uri_1 + (occ1_ref + 21) ],
        [method, csa + occ_uri_2 + (occ2_ref + 21) ],
        [method, csa + occ_uri_3 + (occ3_ref + 21) ],
        [method, csa + occ_uri_1 + (occ1_ref + 22) ],
        [method, csa + occ_uri_2 + (occ2_ref + 22) ],
        [method, csa + occ_uri_3 + (occ3_ref + 22) ],
        [method, csa + occ_uri_1 + (occ1_ref + 23) ],
        [method, csa + occ_uri_2 + (occ2_ref + 23) ],
        [method, csa + occ_uri_3 + (occ3_ref + 23) ],
        [method, csa + occ_uri_1 + (occ1_ref + 24) ],
        [method, csa + occ_uri_2 + (occ2_ref + 24) ],
        [method, csa + occ_uri_3 + (occ3_ref + 24) ],
        [method, csa + occ_uri_1 + (occ1_ref + 25) ],
        [method, csa + occ_uri_2 + (occ2_ref + 25) ],
        [method, csa + occ_uri_3 + (occ3_ref + 25) ],
        [method, csa + occ_uri_1 + (occ1_ref + 26) ],
        [method, csa + occ_uri_2 + (occ2_ref + 26) ],
        [method, csa + occ_uri_3 + (occ3_ref + 26) ],
        [method, csa + occ_uri_1 + (occ1_ref + 27) ],
        [method, csa + occ_uri_2 + (occ2_ref + 27) ],
        [method, csa + occ_uri_3 + (occ3_ref + 27) ],
        [method, csa + occ_uri_1 + (occ1_ref + 28) ],
        [method, csa + occ_uri_2 + (occ2_ref + 28) ],
        [method, csa + occ_uri_3 + (occ3_ref + 28) ],
        [method, csa + occ_uri_1 + (occ1_ref + 29) ],
        [method, csa + occ_uri_2 + (occ2_ref + 29) ],
        [method, csa + occ_uri_3 + (occ3_ref + 29) ],
        [method, csa + occ_uri_1 + (occ1_ref + 30) ],
        [method, csa + occ_uri_2 + (occ2_ref + 30) ],
        [method, csa + occ_uri_3 + (occ3_ref + 30) ],
        [method, csa + occ_uri_1 + (occ1_ref + 31) ],
        [method, csa + occ_uri_2 + (occ2_ref + 31) ],
        [method, csa + occ_uri_3 + (occ3_ref + 31) ],
        [method, csa + occ_uri_1 + (occ1_ref + 32) ],
        [method, csa + occ_uri_2 + (occ2_ref + 32) ],
        [method, csa + occ_uri_3 + (occ3_ref + 32) ],
        [method, csa + occ_uri_1 + (occ1_ref + 33) ],
        [method, csa + occ_uri_2 + (occ2_ref + 33) ],
        [method, csa + occ_uri_3 + (occ3_ref + 33) ],
        [method, csa + occ_uri_1 + (occ1_ref + 34) ],
        [method, csa + occ_uri_2 + (occ2_ref + 34) ],
        [method, csa + occ_uri_3 + (occ3_ref + 34) ],
        [method, csa + occ_uri_1 + (occ1_ref + 35) ],
        [method, csa + occ_uri_2 + (occ2_ref + 35) ],
        [method, csa + occ_uri_3 + (occ3_ref + 35) ],
        [method, csa + occ_uri_1 + (occ1_ref + 36) ],
        [method, csa + occ_uri_2 + (occ2_ref + 36) ],
        [method, csa + occ_uri_3 + (occ3_ref + 36) ],
        [method, csa + occ_uri_1 + (occ1_ref + 37) ],
        [method, csa + occ_uri_2 + (occ2_ref + 37) ],
        [method, csa + occ_uri_3 + (occ3_ref + 37) ],
        [method, csa + occ_uri_1 + (occ1_ref + 38) ],
        [method, csa + occ_uri_2 + (occ2_ref + 38) ],
        [method, csa + occ_uri_3 + (occ3_ref + 38) ],
        [method, csa + occ_uri_1 + (occ1_ref + 39) ],
        [method, csa + occ_uri_2 + (occ2_ref + 39) ],
        [method, csa + occ_uri_3 + (occ3_ref + 39) ],
        [method, csa + occ_uri_1 + (occ1_ref + 40) ],
        [method, csa + occ_uri_2 + (occ2_ref + 40) ],
        [method, csa + occ_uri_3 + (occ3_ref + 40) ],
        [method, csa + occ_uri_1 + (occ1_ref + 41) ],
        [method, csa + occ_uri_2 + (occ2_ref + 41) ],
        [method, csa + occ_uri_3 + (occ3_ref + 41) ],
        [method, csa + occ_uri_1 + (occ1_ref + 42) ],
        [method, csa + occ_uri_2 + (occ2_ref + 42) ],
        [method, csa + occ_uri_3 + (occ3_ref + 42) ],
        [method, csa + occ_uri_1 + (occ1_ref + 43) ],
        [method, csa + occ_uri_2 + (occ2_ref + 43) ],
        [method, csa + occ_uri_3 + (occ3_ref + 43) ],
        [method, csa + occ_uri_1 + (occ1_ref + 44) ],
        [method, csa + occ_uri_2 + (occ2_ref + 44) ],
        [method, csa + occ_uri_3 + (occ3_ref + 44) ],
        [method, csa + occ_uri_1 + (occ1_ref + 45) ],
        [method, csa + occ_uri_2 + (occ2_ref + 45) ],
        [method, csa + occ_uri_3 + (occ3_ref + 45) ],
        [method, csa + occ_uri_1 + (occ1_ref + 46) ],
        [method, csa + occ_uri_2 + (occ2_ref + 46) ],
        [method, csa + occ_uri_3 + (occ3_ref + 46) ],
        [method, csa + occ_uri_1 + (occ1_ref + 47) ],
        [method, csa + occ_uri_2 + (occ2_ref + 47) ],
        [method, csa + occ_uri_3 + (occ3_ref + 47) ],
        [method, csa + occ_uri_1 + (occ1_ref + 48) ],
        [method, csa + occ_uri_2 + (occ2_ref + 48) ],
        [method, csa + occ_uri_3 + (occ3_ref + 48) ],
        [method, csa + occ_uri_1 + (occ1_ref + 49) ],
        [method, csa + occ_uri_2 + (occ2_ref + 49) ],
        [method, csa + occ_uri_3 + (occ3_ref + 49) ],
        [method, csa + occ_uri_1 + (occ1_ref + 50) ],
        [method, csa + occ_uri_2 + (occ2_ref + 50) ],
        [method, csa + occ_uri_3 + (occ3_ref + 50) ],
        [method, csa + occ_uri_1 + (occ1_ref + 51) ],
        [method, csa + occ_uri_2 + (occ2_ref + 51) ],
        [method, csa + occ_uri_3 + (occ3_ref + 51) ],
        [method, csa + occ_uri_1 + (occ1_ref + 52) ],
        [method, csa + occ_uri_2 + (occ2_ref + 52) ],
        [method, csa + occ_uri_3 + (occ3_ref + 52) ],
        [method, csa + occ_uri_1 + (occ1_ref + 53) ],
        [method, csa + occ_uri_2 + (occ2_ref + 53) ],
        [method, csa + occ_uri_3 + (occ3_ref + 53) ],
        [method, csa + occ_uri_1 + (occ1_ref + 54) ],
        [method, csa + occ_uri_2 + (occ2_ref + 54) ],
        [method, csa + occ_uri_3 + (occ3_ref + 54) ],
        [method, csa + occ_uri_1 + (occ1_ref + 55) ],
        [method, csa + occ_uri_2 + (occ2_ref + 55) ],
        [method, csa + occ_uri_3 + (occ3_ref + 55) ],
        [method, csa + occ_uri_1 + (occ1_ref + 56) ],
        [method, csa + occ_uri_2 + (occ2_ref + 56) ],
        [method, csa + occ_uri_3 + (occ3_ref + 56) ],
        [method, csa + occ_uri_1 + (occ1_ref + 57) ],
        [method, csa + occ_uri_2 + (occ2_ref + 57) ],
        [method, csa + occ_uri_3 + (occ3_ref + 57) ],
        [method, csa + occ_uri_1 + (occ1_ref + 58) ],
        [method, csa + occ_uri_2 + (occ2_ref + 58) ],
        [method, csa + occ_uri_3 + (occ3_ref + 58) ],
        [method, csa + occ_uri_1 + (occ1_ref + 59) ],
        [method, csa + occ_uri_2 + (occ2_ref + 59) ],
        [method, csa + occ_uri_3 + (occ3_ref + 59) ],
        [method, csa + occ_uri_1 + (occ1_ref + 60) ],
        [method, csa + occ_uri_2 + (occ2_ref + 60) ],
        [method, csa + occ_uri_3 + (occ3_ref + 60) ],
        [method, csa + occ_uri_1 + (occ1_ref + 61) ],
        [method, csa + occ_uri_2 + (occ2_ref + 61) ],
        [method, csa + occ_uri_3 + (occ3_ref + 61) ],
        [method, csa + occ_uri_1 + (occ1_ref + 62) ],
        [method, csa + occ_uri_2 + (occ2_ref + 62) ],
        [method, csa + occ_uri_3 + (occ3_ref + 62) ],
        [method, csa + occ_uri_1 + (occ1_ref + 63) ],
        [method, csa + occ_uri_2 + (occ2_ref + 63) ],
        [method, csa + occ_uri_3 + (occ3_ref + 63) ],
        [method, csa + occ_uri_1 + (occ1_ref + 64) ],
        [method, csa + occ_uri_2 + (occ2_ref + 64) ],
        [method, csa + occ_uri_3 + (occ3_ref + 64) ],
        [method, csa + occ_uri_1 + (occ1_ref + 65) ],
        [method, csa + occ_uri_2 + (occ2_ref + 65) ],
        [method, csa + occ_uri_3 + (occ3_ref + 65) ],
        [method, csa + occ_uri_1 + (occ1_ref + 66) ],
        [method, csa + occ_uri_2 + (occ2_ref + 66) ],
        [method, csa + occ_uri_3 + (occ3_ref + 66) ],
        [method, csa + occ_uri_1 + (occ1_ref + 67) ],
        [method, csa + occ_uri_2 + (occ2_ref + 67) ],
        [method, csa + occ_uri_3 + (occ3_ref + 67) ],
        [method, csa + occ_uri_1 + (occ1_ref + 68) ],
        [method, csa + occ_uri_2 + (occ2_ref + 68) ],
        [method, csa + occ_uri_3 + (occ3_ref + 68) ],
        [method, csa + occ_uri_1 + (occ1_ref + 69) ],
        [method, csa + occ_uri_2 + (occ2_ref + 69) ],
        [method, csa + occ_uri_3 + (occ3_ref + 69) ],
        [method, csa + occ_uri_1 + (occ1_ref + 70) ],
        [method, csa + occ_uri_2 + (occ2_ref + 70) ],
        [method, csa + occ_uri_3 + (occ3_ref + 70) ],
        [method, csa + occ_uri_1 + (occ1_ref + 71) ],
        [method, csa + occ_uri_2 + (occ2_ref + 71) ],
        [method, csa + occ_uri_3 + (occ3_ref + 71) ],
        [method, csa + occ_uri_1 + (occ1_ref + 72) ],
        [method, csa + occ_uri_2 + (occ2_ref + 72) ],
        [method, csa + occ_uri_3 + (occ3_ref + 72) ],
        [method, csa + occ_uri_1 + (occ1_ref + 73) ],
        [method, csa + occ_uri_2 + (occ2_ref + 73) ],
        [method, csa + occ_uri_3 + (occ3_ref + 73) ],
        [method, csa + occ_uri_1 + (occ1_ref + 74) ],
        [method, csa + occ_uri_2 + (occ2_ref + 74) ],
        [method, csa + occ_uri_3 + (occ3_ref + 74) ],
        [method, csa + occ_uri_1 + (occ1_ref + 75) ],
        [method, csa + occ_uri_2 + (occ2_ref + 75) ],
        [method, csa + occ_uri_3 + (occ3_ref + 75) ],
        [method, csa + occ_uri_1 + (occ1_ref + 76) ],
        [method, csa + occ_uri_2 + (occ2_ref + 76) ],
        [method, csa + occ_uri_3 + (occ3_ref + 76) ],
        [method, csa + occ_uri_1 + (occ1_ref + 77) ],
        [method, csa + occ_uri_2 + (occ2_ref + 77) ],
        [method, csa + occ_uri_3 + (occ3_ref + 77) ],
        [method, csa + occ_uri_1 + (occ1_ref + 78) ],
        [method, csa + occ_uri_2 + (occ2_ref + 78) ],
        [method, csa + occ_uri_3 + (occ3_ref + 78) ],
        [method, csa + occ_uri_1 + (occ1_ref + 79) ],
        [method, csa + occ_uri_2 + (occ2_ref + 79) ],
        [method, csa + occ_uri_3 + (occ3_ref + 79) ],
        [method, csa + occ_uri_1 + (occ1_ref + 80) ],
        [method, csa + occ_uri_2 + (occ2_ref + 80) ],
        [method, csa + occ_uri_3 + (occ3_ref + 80) ],
        [method, csa + occ_uri_1 + (occ1_ref + 81) ],
        [method, csa + occ_uri_2 + (occ2_ref + 81) ],
        [method, csa + occ_uri_3 + (occ3_ref + 81) ],
        [method, csa + occ_uri_1 + (occ1_ref + 82) ],
        [method, csa + occ_uri_2 + (occ2_ref + 82) ],
        [method, csa + occ_uri_3 + (occ3_ref + 82) ],
        [method, csa + occ_uri_1 + (occ1_ref + 83) ],
        [method, csa + occ_uri_2 + (occ2_ref + 83) ],
        [method, csa + occ_uri_3 + (occ3_ref + 83) ],
        [method, csa + occ_uri_1 + (occ1_ref + 84) ],
        [method, csa + occ_uri_2 + (occ2_ref + 84) ],
        [method, csa + occ_uri_3 + (occ3_ref + 84) ],
        [method, csa + occ_uri_1 + (occ1_ref + 85) ],
        [method, csa + occ_uri_2 + (occ2_ref + 85) ],
        [method, csa + occ_uri_3 + (occ3_ref + 85) ],
        [method, csa + occ_uri_1 + (occ1_ref + 86) ],
        [method, csa + occ_uri_2 + (occ2_ref + 86) ],
        [method, csa + occ_uri_3 + (occ3_ref + 86) ],
        [method, csa + occ_uri_1 + (occ1_ref + 87) ],
        [method, csa + occ_uri_2 + (occ2_ref + 87) ],
        [method, csa + occ_uri_3 + (occ3_ref + 87) ],
        [method, csa + occ_uri_1 + (occ1_ref + 88) ],
        [method, csa + occ_uri_2 + (occ2_ref + 88) ],
        [method, csa + occ_uri_3 + (occ3_ref + 88) ],
        [method, csa + occ_uri_1 + (occ1_ref + 89) ],
        [method, csa + occ_uri_2 + (occ2_ref + 89) ],
        [method, csa + occ_uri_3 + (occ3_ref + 89) ],
        [method, csa + occ_uri_1 + (occ1_ref + 90) ],
        [method, csa + occ_uri_2 + (occ2_ref + 90) ],
        [method, csa + occ_uri_3 + (occ3_ref + 90) ],
        [method, csa + occ_uri_1 + (occ1_ref + 91) ],
        [method, csa + occ_uri_2 + (occ2_ref + 91) ],
        [method, csa + occ_uri_3 + (occ3_ref + 91) ],
        [method, csa + occ_uri_1 + (occ1_ref + 92) ],
        [method, csa + occ_uri_2 + (occ2_ref + 92) ],
        [method, csa + occ_uri_3 + (occ3_ref + 92) ],
        [method, csa + occ_uri_1 + (occ1_ref + 93) ],
        [method, csa + occ_uri_2 + (occ2_ref + 93) ],
        [method, csa + occ_uri_3 + (occ3_ref + 93) ],
        [method, csa + occ_uri_1 + (occ1_ref + 94) ],
        [method, csa + occ_uri_2 + (occ2_ref + 94) ],
        [method, csa + occ_uri_3 + (occ3_ref + 94) ],
        [method, csa + occ_uri_1 + (occ1_ref + 95) ],
        [method, csa + occ_uri_2 + (occ2_ref + 95) ],
        [method, csa + occ_uri_3 + (occ3_ref + 95) ],
        [method, csa + occ_uri_1 + (occ1_ref + 96) ],
        [method, csa + occ_uri_2 + (occ2_ref + 96) ],
        [method, csa + occ_uri_3 + (occ3_ref + 96) ],
        [method, csa + occ_uri_1 + (occ1_ref + 97) ],
        [method, csa + occ_uri_2 + (occ2_ref + 97) ],
        [method, csa + occ_uri_3 + (occ3_ref + 97) ],
        [method, csa + occ_uri_1 + (occ1_ref + 98) ],
        [method, csa + occ_uri_2 + (occ2_ref + 98) ],
        [method, csa + occ_uri_3 + (occ3_ref + 98) ],
        [method, csa + occ_uri_1 + (occ1_ref + 99) ],
        [method, csa + occ_uri_2 + (occ2_ref + 99) ],
        [method, csa + occ_uri_3 + (occ3_ref + 99) ],
        [method, csa + occ_uri_1 + (occ1_ref + 100) ],
        [method, csa + occ_uri_2 + (occ2_ref + 100) ],
        [method, csa + occ_uri_3 + (occ3_ref + 100) ],

    ]);

    updateChargingDataRefs((occ1_ref + 100), (occ2_ref + 100), (occ3_ref + 100))

    check(responses[0], {
        "protocol is HTTP/2": (r) => r.proto === 'HTTP/2.0',
        "status was 204": (r) => r.status == 204
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
