/**
 * @author ehsacah
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


//converged charging release operation
export default function() {

    const smf_ip = `192.168.0.${__VU}`;
    const supi = `nai-${__VU}${__ITER}@ericsson.com`;
    const csa = "https://csa.ericsson.se:30867/";
    const method = "POST";

    const occ_uri_1 = "nchf-convergedcharging/v1/chargingdata/occ1-"
    const occ_uri_2 = "nchf-convergedcharging/v1/chargingdata/occ2-"
    const occ_uri_3 = "nchf-convergedcharging/v1/chargingdata/occ3-"

    //console.log(csa + occ_uri_1 + occ1_ref + "/delete" + "----OCC1----")
    //console.log(csa + occ_uri_2 + occ2_ref + "/delete" + "----OCC2----")
    //console.log(csa + occ_uri_3 + occ3_ref + "/delete" + "----OCC3----")

    //sending 100 release requests in parallel in every iteration. This batch is executed by each VU.
    let responses = http.batch([

        [method, csa + occ_uri_1 + (occ1_ref + 1) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 1) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 1) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 2) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 2) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 2) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 3) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 3) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 3) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 4) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 4) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 4) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 5) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 5) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 5) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 6) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 6) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 6) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 7) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 7) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 7) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 8) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 8) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 8) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 9) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 9) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 9) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 10) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 10) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 10) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 11) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 11) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 11) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 12) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 12) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 12) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 13) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 13) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 13) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 14) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 14) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 14) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 15) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 15) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 15) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 16) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 16) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 16) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 17) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 17) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 17) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 18) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 18) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 18) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 19) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 19) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 19) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 20) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 20) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 20) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 21) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 21) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 21) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 22) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 22) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 22) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 23) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 23) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 23) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 24) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 24) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 24) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 25) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 25) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 25) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 26) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 26) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 26) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 27) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 27) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 27) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 28) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 28) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 28) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 29) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 29) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 29) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 30) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 30) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 30) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 31) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 31) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 31) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 32) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 32) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 32) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 33) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 33) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 33) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 34) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 34) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 34) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 35) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 35) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 35) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 36) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 36) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 36) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 37) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 37) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 37) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 38) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 38) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 38) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 39) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 39) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 39) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 40) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 40) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 40) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 41) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 41) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 41) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 42) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 42) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 42) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 43) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 43) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 43) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 44) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 44) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 44) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 45) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 45) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 45) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 46) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 46) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 46) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 47) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 47) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 47) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 48) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 48) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 48) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 49) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 49) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 49) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 50) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 50) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 50) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 51) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 51) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 51) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 52) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 52) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 52) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 53) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 53) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 53) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 54) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 54) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 54) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 55) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 55) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 55) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 56) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 56) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 56) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 57) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 57) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 57) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 58) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 58) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 58) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 59) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 59) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 59) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 60) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 60) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 60) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 61) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 61) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 61) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 62) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 62) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 62) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 63) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 63) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 63) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 64) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 64) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 64) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 65) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 65) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 65) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 66) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 66) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 66) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 67) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 67) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 67) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 68) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 68) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 68) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 69) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 69) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 69) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 70) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 70) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 70) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 71) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 71) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 71) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 72) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 72) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 72) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 73) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 73) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 73) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 74) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 74) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 74) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 75) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 75) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 75) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 76) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 76) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 76) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 77) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 77) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 77) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 78) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 78) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 78) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 79) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 79) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 79) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 80) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 80) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 80) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 81) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 81) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 81) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 82) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 82) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 82) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 83) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 83) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 83) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 84) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 84) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 84) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 85) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 85) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 85) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 86) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 86) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 86) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 87) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 87) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 87) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 88) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 88) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 88) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 89) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 89) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 89) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 90) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 90) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 90) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 91) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 91) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 91) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 92) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 92) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 92) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 93) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 93) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 93) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 94) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 94) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 94) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 95) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 95) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 95) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 96) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 96) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 96) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 97) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 97) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 97) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 98) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 98) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 98) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 99) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 99) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 99) + "/delete"],
        [method, csa + occ_uri_1 + (occ1_ref + 100) + "/delete"],
        [method, csa + occ_uri_2 + (occ2_ref + 100) + "/delete"],
        [method, csa + occ_uri_3 + (occ3_ref + 100) + "/delete"],

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
