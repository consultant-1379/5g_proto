/**
 * @author cardinulls
 */
import http from "k6/http";
import { check, sleep } from "k6";

let duration = `${__ENV.DURATION}`;

export let options = {
    duration: duration,
    setupTimeout: "100s", // time available for setup function
};


/// init code: runs once per VU ///
//////////////////////////////////

// env variables from jcat
const bsf = `http://${__ENV.BSF_WORKER_IP}:${__ENV.BSF_WORKER_PORT}/`;
const numberOfBindings = `${__ENV.NUMBER_OF_BINDINGS}`;


//const bsf = "https://10.63.143.35:32367/"
//const numberOfBindings = 200;

const uri = "nbsf-management/v1/pcfBindings";
const postEndpoint= bsf + uri;
const ipGetEndpoint= bsf + uri + "?ipv4Addr=";


const headers = {
        headers: {
            "Connection": "keep-alive",
            "Content-Type": "application/json",
        }
    };

/// setup function : runs once from one VU ///
////////////////////////////////////////////////

export function setup() {
    make_post_requests_rangeA(numberOfBindings);
}

/// default function : runs repeatedly per VU ///
////////////////////////////////////////////////

export default function() {
    make_get_requests(numberOfBindings); // those that are posted within range A
    let bindingdIds = make_post_requests_rangeB(numberOfBindings);
    make_delete_requests(bindingdIds);
}


/// helper functions ///
///////////////////////

// generate addresses
function intToIP(int) {
    var part1 = int & 255;
    var part2 = ((int >> 8) & 255);
    var part3 = ((int >> 16) & 255);
    var part4 = ((int >> 24) & 255);
    return part4 + "." + part3 + "." + part2 + "." + part1;
}

function create_body(i){
    let ip = intToIP(i);
    return create_json(ip);
}

function create_json(ip) {
    var pcfbinding = new Object();
    pcfbinding.supi = 'imsi-310150123456789';
    pcfbinding.gpsi = 'msisdn-918369110173';
    pcfbinding.ipv4Addr = ip;
    pcfbinding.ipDomain = "vu" + `${__VU}`;
    pcfbinding.dnn = "null";
    pcfbinding.pcfFqdn = 'pcf.ericsson.se';
    pcfbinding.pcfDiamHost="pcf-diamhost.com";
    pcfbinding.pcfDiamRealm="pcf-diamrealm.com";
    pcfbinding.snssai = {"sst":3,"sd":"DEADF9"};
    var jsonStringBody = JSON.stringify(pcfbinding);
    return jsonStringBody;
}

function select_ip(n)
{
    let r = getRandomInt(n);
    return intToIP(r)
}

// range A: ips generated with [0, n)
function make_post_requests_rangeA(n) {

    let requests = [];

    for(let i = 0; i < n; i++) // range A
    {
        let body = create_body(i);
        requests[i] = ["POST", postEndpoint, body, headers];
    }

    let responses = http.batch(requests);

    let resp_length = Object.keys(responses).length;
    for(let i=0;i<resp_length;i++)
    {
       const result = check(responses[i], {
         "POST: status was 201": (r) => r.status == 201,
       });
       /*
       if (result){
            deleteEndpoints.push(responses[i].headers["Location"]);
          //console.log("THE LOCATION:   "+ responses[i].headers["Location"]);
         //console.log("status code was *not* 201 - POST failed")
         //console.log(responses[i].request.url)
         //console.log(responses[i].body)
       }
       */
    }
}

// range B: ips generated in [n, 2n)
function make_post_requests_rangeB(n) {

    let requests = [];

    for(let i = 0; i < n; i++)
    {
        let body = create_body(i + n); // [n, 2n)
        requests[i] = ["POST", postEndpoint, body, headers];
    }

    let responses = http.batch(requests);

    let resp_length = Object.keys(responses).length;
    for(let i=0;i<resp_length;i++)
    {
       const result = check(responses[i], {
         "POST: status was 201": (r) => r.status == 201
       });
      //if (!result){
      //   console.log("status code was *not* 201 - POST failed")
      //   console.log(responses[i].request.url)
      //   console.log(responses[i].body)
      //}
    }

    let ids = process_response_headers(responses);
    return ids;
}

function make_get_requests(n) {

    let requests = [];
    for(let i = 0; i < n; i++)
    {
        let ip = select_ip(n);
        requests[i] = ["GET", ipGetEndpoint + ip, null, headers];
    }

    let responses = http.batch(requests);

    let resp_length = Object.keys(responses).length;
    for(let i=0;i<resp_length;i++)
    {
       const result = check(responses[i], {
         "GET: status was 200": (r) => r.status == 200
       });
     //  if (!result){
      //    console.log("status code was *not* 200 - GET failed")
       //   console.log(responses[i].request.url)
        //  console.log(responses[i].body)
      // }
    }
}

function make_delete_requests(bindingLocations) {

    let requests = [];
    for(let i = 0; i < bindingLocations.length; i++)
    {
        // disable temporarily
        //if(deleteEndpoints[i] != null){
        //    requests[i] = ["DELETE", deleteEndpoints[i], null, headers];
        //}else{
        //    requests[i] = ["DELETE", ipDeleteEndpoint + uuid(), null, headers];
        //}
        requests[i] = ["DELETE", bindingLocations[i], null, headers];
    }

    let responses = http.batch(requests);

    let resp_length = Object.keys(responses).length;
    for(let i=0;i<resp_length;i++)
    {
       const result = check(responses[i], {
         "DELETE: status was 204": (r) => r.status == 204
       });
       //if (!result){
       //   console.log("status code was *not* 204 - DELETE failed")
       //   console.log(responses[i].status)
       //   console.log(responses[i].request.url)
       //   console.log(responses[i].body)
       //}
    }
}

function process_response_headers(responses) {

    let resp_length = Object.keys(responses).length;

    let ids = [];
    for (let i=0;i<resp_length;i++) {
      let location = responses[i].headers["Location"];
      if (location != null) {
         ids[i] = location;
      }
    }
    return ids;
}

// https://stackoverflow.com/questions/105034/how-to-create-guid-uuid
// todo: rm
//function uuid() {
//     return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
//        var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
//        return v.toString(16);
//      });
//}

// returns a random int in [0, max)
function getRandomInt(max) {
  return Math.floor(Math.random() * Math.floor(max));
}
