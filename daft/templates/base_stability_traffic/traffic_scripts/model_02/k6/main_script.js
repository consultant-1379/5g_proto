/**
 * @author ekargia
 */

import scp_script from "./scp_stability_test.js";
//import rcc_script1 from "./cc_create_update_release_rcc_ci_region_a.js";
//import rcc_script2 from "./cc_create_update_release_rcc_ci_region_b.js";
//import rcc_script3 from "./cc_create_update_release_rcc_ci_region_c.js";
//import rcc_script4 from "./cc_notify_rcc_ci_no_tls.js";
//import cc_script from "./cc_create_update_notify_release_ci.js";
//import slc_script from "./slc_subscribe_modify_notify_terminate_unsubscribe_ci.js";
import optiond_script from "./option_d.js";

let timeUnit = `${__ENV.TIMEUNIT}`;
let rate = parseInt(`${__ENV.RATE}`);
let duration = `${__ENV.DURATION}`;
let preAllocatedVus = 50;
let maxVus = 200;

export let options = {
    scenarios: {
        load_test_scp: {
            executor: 'constant-arrival-rate',
            timeUnit: timeUnit,
            rate: rate,
            duration: duration,
            preAllocatedVUs: preAllocatedVus, // how large the initial pool of VUs would be
            maxVUs: maxVus, // if the preAllocatedVUs are not enough, we can initialize more
            exec: 'scp_function', // same function as the scenario above, but with different env vars
        },
	/*
        load_test_rcc1: {
            executor: 'constant-arrival-rate',
            timeUnit: timeUnit,
            rate: rate,
            duration: duration,
            preAllocatedVUs: preAllocatedVus, 
            maxVUs: maxVus,
            exec: 'rcc_function1', 
        },
        load_test_rcc2: {
            executor: 'constant-arrival-rate',
            timeUnit: timeUnit,
            rate: rate,
            duration: duration,
            preAllocatedVUs: preAllocatedVus,
            maxVUs: maxVus,
            exec: 'rcc_function2',
        },
        load_test_rcc3: {
            executor: 'constant-arrival-rate',
            timeUnit: timeUnit,
            rate: rate,
            duration: duration,
            preAllocatedVUs: preAllocatedVus, 
            maxVUs: maxVus, 
            exec: 'rcc_function3', 
        },
        load_test_rcc4: {
            executor: 'constant-arrival-rate',
            timeUnit: timeUnit, 
            rate: rate,
            duration: duration,
            preAllocatedVUs: preAllocatedVus, 
            maxVUs: maxVus,
            exec: 'rcc_function4',
        },
        load_test_cc1: {
            executor: 'constant-arrival-rate',
            timeUnit: timeUnit,
            rate: rate,
            duration: duration,
            preAllocatedVUs: preAllocatedVus,
            maxVUs: maxVus,
            exec: 'cc_function', 
        },
        load_test_cc2: {
            executor: 'constant-arrival-rate',
            timeUnit: timeUnit,
            rate: rate,
            duration: duration,
            preAllocatedVUs: preAllocatedVus,
            maxVUs: maxVus,
            exec: 'cc_function', 
        },
        load_test_slc1: {
            executor: 'constant-arrival-rate',
            timeUnit: timeUnit,
            rate: rate,
            duration: duration,
            preAllocatedVUs: preAllocatedVus,
            maxVUs: maxVus,
            exec: 'slc_function',
        },
        load_test_slc2: {
            executor: 'constant-arrival-rate',
            timeUnit: timeUnit,
            rate: rate,
            duration: duration,
            preAllocatedVUs: preAllocatedVus, 
            maxVUs: maxVus,
            exec: 'slc_function',
        },
	*/
        load_test_optiond: {
            executor: 'constant-arrival-rate',
            timeUnit: timeUnit,
            rate: rate,
            duration: duration,
            preAllocatedVUs: preAllocatedVus,
            maxVUs: maxVus,
            exec: 'optiond_function',
        },
    },
    tlsAuth: [{
        domains: [`${__ENV.SCP_HOST}`],
        cert: open(__ENV.CERT),
        key: open(__ENV.KEY)
    }],
};

export function optiond_function() {
    optiond_script();
}

export function scp_function() {
    scp_script();
}
/*
export function rcc_function1() {
    rcc_script1();
}


export function rcc_function2() {
    rcc_script2();
}

export function rcc_function3() {
    rcc_script3();
}

export function rcc_function4() {
    rcc_script4();
}

export function cc_function() {
    cc_script();
}

export function slc_function() {
    slc_script();
}
*/

export default function () {

}
