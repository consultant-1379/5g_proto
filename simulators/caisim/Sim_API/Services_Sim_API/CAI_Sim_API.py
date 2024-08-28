import os
import sys
import time
import re
import random
from threading import RLock
from flask import make_response, request, json, jsonify
# pointing to root path of simulator
sys.path.append(os.path.join(os.path.dirname(os.path.dirname(os.path.realpath(__file__))), os.path.pardir))
from Sim_Objects.CAI_Objects import *
from Simulator_Service import app
import settings
import time
from itertools import tee

#Constant to Byte->MByte conversion
MB = 1000000.0

#storing latest report
some_rlock = RLock()
latestReceivedReport = {}

#List of time sim has received requests by ASIH
receivedReport = []
def pair(iterable):
    a, b = tee(iterable)
    next(b, None)
    return zip(a, b)

# Accept POST requests (reports).
# Added check on type of parameters of request. After passed checks, the response is built with the content of the policy.
@app.route(settings.cai_path_base + '/applicationInfoReport', methods=['POST'])
def post_report():
    if (not settings.policyMode):
        json_request = request.get_json()
        lenght_request = float((len(json.dumps(json_request)))/MB)  # Accept POST requests (reports) up to 1 MB
        if (lenght_request>1.0):
            body = json.loads('{"message": "Body request too large","resource": "NeLS"}')
            res = make_response(jsonify(body), 413)
            return res
        else:
            report = Report_CAI_POST(json_request)  # Instantiates the incoming request as a "Report_CAI_POST" object
            print("Received POST Usage-Reports {}\n".format(report))
            if (report.validate_report()):
               with some_rlock:
                  global latestReceivedReport
                  named_tuple = time.localtime() # get struct_time
                  time_string = time.strftime("%H%M%S", named_tuple)
                  latestReceivedReport = '{ "time": "'+time_string+ '","message": '+json.dumps(json_request)+'}'
                  res = make_response("", 200)
            else:
               body = json.loads('{"message": "Malformed content","resource": "NeLS"}')
               res = make_response(jsonify(body), 400)
            return res
    else:
           # Set policyMode to simulate Server unavailability through /setPolicy API
           body = json.loads('{"message": "Server unavailable error - NeLS Server cannot handle request, due to a temporary problem. Request can be retried.","resource": "NeLS"}')
           res = make_response(jsonify(body), 503)
           global receivedReport
           receivedReport.append(time.time())
           return res
       
# Get Latest requests (reports).
@app.route(settings.cai_path_base + '/applicationInfoReport', methods=['GET'])
def get_report():
    with some_rlock:
      global latestReceivedReport
      return make_response(latestReceivedReport, 200)
  
@app.route(settings.cai_path_base + '/checkRetryReport', methods=['GET'])
def get_time_retry():
  #Ths endpoint is used only to verify that ASIH is retrying
  #the send of the report each N seconds set in the helm chart
  check = True
  global receivedReport
  for v, w in pair(receivedReport):
    print("Time report (w): ", w)
    print("Time report (v): ", v)
    time_diff = w-v
    print("time_diff (w-v): ", time_diff)
    #The following is set to evaluate a network delay of 2 seconds set in TEST configuration
    #To be centralize with hardcoded value set in ASIH
    #    retrySend                 = 10
    check &= (round(time_diff) <= 19)
    print("time_diff round(w-v): ", round(time_diff))
  if(check):
    return make_response(jsonify('OK'), 200)
  else:
    return make_response(jsonify('Not OK'), 412)

@app.route(settings.cai_path_base + '/setPolicy', methods=['POST'])
def post_set_policy():
     print('Enabling Policy Mode Simulator')
     settings.policyMode = True
     res = make_response('Policy Mode Enabled!\n', 200)
     return res
 
@app.route(settings.cai_path_base + '/setPolicy', methods=['DELETE'])
def delete_set_policy():
     print('Disabling Policy Mode Simulator')
     settings.policyMode = False
     res = make_response('Policy Mode Disabled!\n', 200)
     global receivedReport
     receivedReport = []
     return res