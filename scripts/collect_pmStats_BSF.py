#INSTRUCTIONS:
#----------------------------
#1) port-forward the pm server to 8080 localhost
#2) module load python/3.11-addons-requests-2.28.2 (use "modules avail | grep requests" and load the latest)
#3) python3 collect_pmStats_BSF.py -n 5g-bsf-$USER -m -c -t bsf --startTime "21/09/2023 09:28:40" --duration 800
#4) 3 files will be created stats-cpu.csv, stats-memory.csv, stats-traffic.csv
#5) if the files do not contain the expected values, it is quite possible the script's time window is incorrect
#----------------------------

import datetime
import time
try:
    import requests
except ImportError:
    print("Module python/requests is not loaded. Please follow the instructions at the top of the script before executing.")
    sys.exit(-1)
from pprint import pprint
import os
import argparse
import time
import csv
import math
from datetime import datetime


parser = argparse.ArgumentParser()

parser.add_argument('-n', '--namespace', type=str, help="namespace for pm-server deployment", default='5g-bsf-' + os.environ['USER'])
parser.add_argument('--statsNamespace', type=str, help="namespace for pods that are measured")
parser.add_argument('-m', '--memory', help="show measurements for memory usage", action="store_true")
parser.add_argument('-c', '--cpu', help="show measurements for cpu usage", action="store_true")
parser.add_argument('-t', '--traffic', type=str, help="show latency and tps statistics about envoy traffic. possible values are bsf")
parser.add_argument('--startTime', type=str, help="the start time for measurements in <DD/MM/YYYY HH:MM:SS> or <unix_timestamp> form")
parser.add_argument('--duration', type=int, help="the duration of the measurements in seconds")
parser.add_argument('-e', '--export', type=str, help="export measurements to csv files with the specified filename", default="stats")


args = parser.parse_args()

if not (args.cpu or args.memory or args.traffic) :
    print ("You have to specify at least one measurement type.")
    exit()

trafficIsSepp = False;
trafficIsBsf = False;

workerName = "worker"

if args.traffic:
    if not (args.traffic == "sepp" or args.traffic == "scp" or args.traffic == "bsf"):
        print ("Traffic type needs to be sepp or scp")
        exit()
    if args.traffic == "sepp":
        trafficIsSepp = True
    if args.traffic == "bsf":
        trafficIsBsf = True    
        

if args.startTime:
    if not args.startTime.isdigit(): #if starttime is only digits, then it is a unix timestamp, no need to parse.
        parsedTime = datetime.strptime(args.startTime, '%d/%m/%Y %H:%M:%S')
        startTime = (parsedTime - datetime(1970, 1, 1)).total_seconds() #parsedTime is in local time, have to convert it to utc.
    else:
        startTime = int(args.startTime)
    if args.duration:
        endTime = startTime + args.duration
    else:
        endTime = int(time.time())
        args.duration = int(endTime - startTime)
else:
    endTime = int(time.time())
    if not args.duration:
        args.duration = 3600
    startTime = endTime - args.duration


print ('startTime:', datetime.utcfromtimestamp(startTime).strftime('%Y-%m-%d %H:%M:%S') + ' UTC')
print ('  endTime:', datetime.utcfromtimestamp(endTime).strftime('%Y-%m-%d %H:%M:%S') + ' UTC')


print (' duration: ', end='')
if(args.duration >= 3600*24):
    print (str(int(args.duration/(3600*24))) + 'd', end=' ')
if(args.duration%(3600*24) >= 3600):
    print (str(int(args.duration%(3600*24)/3600)) + 'h', end=' ')
if (args.duration%3600 >= 60):
    print (str(int(args.duration%3600/60)) + 'm', end=' ')
if(args.duration%60):
    print (str(int(args.duration%60)) + 's')

if startTime > time.time() or endTime > time.time():
    print ("Time range cannot exceed current time")
    exit()

if not args.statsNamespace:
    args.statsNamespace = args.namespace

namespace = args.namespace
statsNamespace = args.statsNamespace

print ("namespace: " + namespace)
print ("statsNamespace: " + statsNamespace)
if args.traffic:
    print ("traffic type: " + args.traffic)

k = 'kubectl -n ' + namespace

#read pm server address
pmServerAddress = None
stream = os.popen(k + ' get httpproxy')
for proxy in stream.readlines():
    pmServerAddress = proxy.split()[1];
    # if "eric-pm-server" in proxy :
    #     pmServerAddress = proxy.split()[1];

if pmServerAddress is None:
    print ("Prometheus not found in httpproxy.")
    exit()

#read port
port = None
stream = os.popen(k + ' get svc')
for svc in stream.readlines():
    if "eric-tm-ingress-controller-cr" in svc and "contour" not in svc :
        portDetails = svc.split()[4]
        port = portDetails.split("/")[0][3:]

if port is None:
    print ("Ingress controller port not found.")
    exit()

#PROMETHEUS = "http://" + pmServerAddress + ':' + port
PROMETHEUS = "http://localhost:9090"
print ("prometheus address: " + PROMETHEUS )
print()


#############################################################
#functions
#############################################################

#calculate the max cpu usage measured for every container in the specified time range
def get_cpu_max_metrics() :
    #get cpu usage values for the specified duration
    responseContainersCpu = requests.get(PROMETHEUS + '/api/v1/query_range', params={'query': "rate(container_cpu_usage_seconds_total{namespace='" + args.statsNamespace + "'}[1m])", 'start':startTime, 'end':endTime, 'step':'60s'})
    resultsContainersCpu = responseContainersCpu.json()['data']['result']

    #for each measured container, keep the max value in containerMaxValuesCpu
    #the dict has a line for each pod name, so for each pod a new inner dict has to be instanciated, and this inner dict holds the measurements for each container
    containerMaxValuesCpu = {}
    for result in resultsContainersCpu:
        if result['metric']['pod'] not in containerMaxValuesCpu : #create dict for this pod
            containerMaxValuesCpu[result['metric']['pod']] = {}
        if 'container' in result['metric'] :  #read values of container
            if result['metric']['container'] != 'POD' :
                containerMaxValuesCpu[result['metric']['pod']][result['metric']['container']] = round( max([float(val[1]) for val in result['values']]), 3)
        else:  #no container specified, refers to total pod measurement
            if 'name' not in result['metric'] : #there are 2 metrics for each pod (without container specified), but one is always 0, so we filter it out
                containerMaxValuesCpu[result['metric']['pod']]['total'] = round( max([float(val[1]) for val in result['values']]), 3)

    return containerMaxValuesCpu

#calculate the average cpu usage measured for every container in the specified time range
def get_cpu_avg_metrics() :
    #get cpu usage average values. Average is calculated in timestamp of endTime, for the duration  [endTime - duration, endtime]
    responseContainersCpu = requests.get(PROMETHEUS + '/api/v1/query', params={'query': "rate(container_cpu_usage_seconds_total{namespace='" + args.statsNamespace + "'}[" + str(args.duration) + "s])", 'time':endTime})
    resultsContainersCpu = responseContainersCpu.json()['data']['result']

    #for each measured container, keep the average value in containerAvgValuesCpu
    #the dict has a line for each pod name, so for each pod a new inner dict has to be instanciated, and this inner dict holds the measurements for each container
    containerAvgValuesCpu = {}
    for result in resultsContainersCpu:
        if result['metric']['pod'] not in containerAvgValuesCpu : #create dict for this pod
            containerAvgValuesCpu[result['metric']['pod']] = {}
        if 'container' in result['metric'] :  #read values of container
            if result['metric']['container'] != 'POD' :
                containerAvgValuesCpu[result['metric']['pod']][result['metric']['container']] = round( float(result['value'][1]), 3)
        else :  #no container specified, refers to total pod measurement
            if 'name' not in result['metric'] : #there are 2 metrics for each pod (without container specified), but one is always 0, so we filter it out
                containerAvgValuesCpu[result['metric']['pod']]['total'] = round( float(result['value'][1]), 3)

    return containerAvgValuesCpu

#calculate the max memory usage measured for every container in the specified time range
def get_memory_max_metrics() :
    #get memory usage values for the specified duration
    responseContainersMemory = requests.get(PROMETHEUS + '/api/v1/query_range', params={'query': "container_memory_working_set_bytes{namespace='" + args.statsNamespace + "'}", 'start':startTime, 'end':endTime, 'step':'60s'})
    resultsContainersMemory = responseContainersMemory.json()['data']['result']

    #for each measured container, keep the max value in containerMaxValuesMemory
    #the dict has a line for each pod name, so for each pod a new inner dict has to be instanciated, and this inner dict holds the measurements for each container
    containerMaxValuesMemory = {}
    for result in resultsContainersMemory:
        if result['metric']['pod'] not in containerMaxValuesMemory : #create dict for this pod
            containerMaxValuesMemory[result['metric']['pod']] = {}
        if 'container' in result['metric'] :  #read values of container
            if result['metric']['container'] != 'POD' :
                containerMaxValuesMemory[result['metric']['pod']][result['metric']['container']] = int( max([int(val[1]) for val in result['values']]) /1024/1024)
        else :  #no container specified, refers to total pod measurement
            if 'name' not in result['metric'] :
                containerMaxValuesMemory[result['metric']['pod']]['total'] = int( max([int(val[1]) for val in result['values']]) /1024/1024)

    return containerMaxValuesMemory

#calculate the average cpu usage measured for every container in the specified time range
def get_memory_avg_metrics() :
    #get memory usage average values. Average is calculated in timestamp of endTime, for the duration  [endTime - duration, endtime]
    responseContainersMemory = requests.get(PROMETHEUS + '/api/v1/query', params={'query': "avg_over_time(container_memory_working_set_bytes{namespace='" + args.statsNamespace + "'}[" + str(args.duration) + "s])", 'time':endTime})
    resultsContainersMemory = responseContainersMemory.json()['data']['result']
    

    #for each measured container, keep the average value in containerAvgValuesMemory
    #the dict has a line for each pod name, so for each pod a new inner dict has to be instanciated, and this inner dict holds the measurements for each container
    containerAvgValuesMemory = {}
    for result in resultsContainersMemory:
        if result['metric']['pod'] not in containerAvgValuesMemory : #create dict for this pod
            containerAvgValuesMemory[result['metric']['pod']] = {}
        if 'container' in result['metric'] :  #read values of container
            if result['metric']['container'] != 'POD' :
                containerAvgValuesMemory[result['metric']['pod']][result['metric']['container']] = int(round(float(result['value'][1])) /1024/1024)
        else :  #no container specified, refers to total pod measurement
            if 'name' not in result['metric'] :
                containerAvgValuesMemory[result['metric']['pod']]['total'] = int(round(float(result['value'][1])) /1024/1024)

    return containerAvgValuesMemory

#query for average pm samples per second in the specified time range
def get_pm_metrics() :
    responsePmSamples = requests.get(PROMETHEUS + '/api/v1/query', params={'query': "rate(prometheus_tsdb_head_samples_appended_total[" + str(args.duration) + "s])", 'time':endTime})
    
    
    resultsPmSamples = responsePmSamples.json()['data']['result'][0]
    pmSamples = int(round(float(resultsPmSamples['value'][1])))

    return pmSamples

#query for average tps in the specified time range
def get_tps_avg_metrics() :

    queryFilters = ""
    if trafficIsSepp: #sepp traffic
        queryFilters = "envoy_response_code_class='2',nf_type='sepp',group='ingress',envoy_listener_address='',roaming_partner=''"
    else: #scp traffic
        queryFilters = "envoy_response_code_class='2',nf_type='scp',group='ingress',envoy_listener_address=''"
        
    responsePodsTps = requests.get(PROMETHEUS + '/api/v1/query', params={'query': "rate(envoy_downstream_rq_xx{" + queryFilters + "}[" + str(args.duration) + "s])", 'time':endTime})

    resultsPodsTps = responsePodsTps.json()['data']['result']

    podsAvgValuesTps = {}
    i = 1
    for result in resultsPodsTps :
        podsAvgValuesTps[workerName + str(i)] = int(round(float(result['value'][1])))
        i += 1

    return podsAvgValuesTps

def get_tps_avg_metrics_bsf_post() :

    queryFilters = "method='POST'"
            
    responsePodsTps = requests.get(PROMETHEUS + '/api/v1/query', params={'query': "rate(bsf_in_requests_total{" + queryFilters + "}[" + str(args.duration) + "s])", 'time':endTime})
    
    #{" + queryFilters + "}
    resultsPodsTps = responsePodsTps.json()['data']['result']

    podsAvgValuesTps = {}
    i = 1
    if resultsPodsTps :
        for result in resultsPodsTps :
            podsAvgValuesTps[workerName + str(i)] = int(round(float(result['value'][1])))
            i += 1

    return podsAvgValuesTps

def get_tps_avg_metrics_bsf_get() :

    queryFilters = "method='GET'"
            
    responsePodsTps = requests.get(PROMETHEUS + '/api/v1/query', params={'query': "rate(bsf_in_requests_total{" + queryFilters + "}[" + str(args.duration) + "s])", 'time':endTime})
    
    #{" + queryFilters + "}
    resultsPodsTps = responsePodsTps.json()['data']['result']

    podsAvgValuesTps = {}
    i = 1
    if resultsPodsTps :
        for result in resultsPodsTps :
            podsAvgValuesTps[workerName + str(i)] = int(round(float(result['value'][1])))
            i += 1

    return podsAvgValuesTps


def get_latency_avg_metrics_bsf_post() :

    queryFilters = "method='POST', code='201', app='eric-bsf-load'"
            
    responseAvgLatency = requests.get(PROMETHEUS + '/api/v1/query', params={'query': "rate(vertx_http_client_response_time_seconds_sum{" + queryFilters + "}[" + str(args.duration) + "s])/rate(vertx_http_client_response_time_seconds_count[" + str(args.duration) + "s])", 'time':endTime})
    
    #{" + queryFilters + "}
    resultsAvgLatency = {}
    getResponse = responseAvgLatency.json()['data']['result']
    podsAvgValuesLatency = -1
    if getResponse:
        resultsAvgLatency = responseAvgLatency.json()['data']['result'][0]
        if not(math.isnan(float(resultsAvgLatency['value'][1]))) :
            podsAvgValuesLatency = round(float(resultsAvgLatency['value'][1])*1000, 5)
            print(resultsAvgLatency)
    
    
    
    
    

    return podsAvgValuesLatency

def get_latency_avg_metrics_bsf_get() :

    queryFilters = "method='GET', code='200', app='eric-bsf-load'"
            
    responseAvgLatency = requests.get(PROMETHEUS + '/api/v1/query', params={'query': "rate(vertx_http_client_response_time_seconds_sum{" + queryFilters + "}[" + str(args.duration) + "s])/rate(vertx_http_client_response_time_seconds_count[" + str(args.duration) + "s])", 'time':endTime})
    #{" + queryFilters + "}
    resultsAvgLatency = {}


    getResponse = responseAvgLatency.json()['data']['result']
    podsAvgValuesLatency = -1
    if getResponse:
        resultsAvgLatency = responseAvgLatency.json()['data']['result'][0]
        podsAvgValuesLatency = round(float(resultsAvgLatency['value'][1])*1000, 5)
        print(resultsAvgLatency)
    
    return podsAvgValuesLatency

#query for average downstream latency in the specified time range
def get_latency_avg_metrics() :
    responseAvgLatency = requests.get(PROMETHEUS + '/api/v1/query', params={'query': "rate(envoy_downstream_rq_time_sum{}[" + str(args.duration) + "s])/rate(envoy_downstream_rq_time_count{}[" + str(args.duration) + "s])", 'time':endTime})

    resultsAvgLatency = responseAvgLatency.json()['data']['result']

    podsAvgValuesLatency = {}
    i = 1
    for result in resultsAvgLatency :
        podsAvgValuesLatency[workerName + str(i)] = round(float(result['value'][1]), 2)
        i += 1

    return podsAvgValuesLatency

#query for 95th percentile downstream latency in the specified time range
def get_latency_quantile95_metrics() :
    queryFilters = ""
    if trafficIsSepp: #sepp traffic
        queryFilters = "nf_type='sepp',"
    else: #scp traffic
        queryFilters = "nf_type='scp',"

    responseLatencyQuantile = requests.get(PROMETHEUS + '/api/v1/query', params={'query': "histogram_quantile(0.95, sum(rate(envoy_downstream_rq_time_bucket{" + queryFilters + "group='ingress'}[" + str(args.duration) + "s])) by (le))", 'time':endTime})

    resultsLatencyQuantile = responseLatencyQuantile.json()['data']['result'][0]
    latencyQuantile = round(float(resultsLatencyQuantile['value'][1]), 2)

    return latencyQuantile

#query for average envoy latency in the specified time range
def get_envoy_latency_avg_metrics() :
    responseAvgLatencyEnvoy = requests.get(PROMETHEUS + '/api/v1/query', params={'query': "rate(envoy_downstream_rq_time_sum{}[" + str(args.duration) + "s])/rate(envoy_downstream_rq_time_count{}[" + str(args.duration) + "s]) - on (instance) (avg by (instance) (rate(envoy_upstream_rq_time_sum{}[" + str(args.duration) + "s]))/avg by (instance) (rate(envoy_upstream_rq_time_count{}[" + str(args.duration) + "s])))", 'time':endTime})

    resultsAvgLatencyEnvoy = responseAvgLatencyEnvoy.json()['data']['result']

    podsAvgValuesLatency = {}
    i = 1
    for result in resultsAvgLatencyEnvoy :
        podsAvgValuesLatency[workerName + str(i)] = round(float(result['value'][1]), 2)
        i += 1

    return podsAvgValuesLatency

#query for 95th percentile envoy latency in the specified time range
def get_envoy_latency_quantile95_metrics() :
    queryFilters = ""
    if trafficIsSepp: #sepp traffic
        queryFilters = "nf_type='sepp',"
    else: #scp traffic
        queryFilters = "nf_type='scp',"

    responseLatencyEnvoy = requests.get(PROMETHEUS + '/api/v1/query', params={'query': "histogram_quantile(0.95, sum(rate(envoy_downstream_rq_time_bucket{" + queryFilters + "group='ingress'}[" + str(args.duration) + "s])) by (le)) - histogram_quantile(0.95, sum(rate(envoy_upstream_rq_time_bucket{" + queryFilters + "group='egress'}[" + str(args.duration) + "s])) by (le))", 'time':endTime})

    resultsLatencyEnvoy = responseLatencyEnvoy.json()['data']['result'][0]
    envoyLatencyQuantile = round(float(resultsLatencyEnvoy['value'][1]), 2)

    return envoyLatencyQuantile

#query to get cpu limits of every container. Useful to calculate cpu usage in %.
def get_cpu_limits() :
    responseContainersCpuLimits = requests.get(PROMETHEUS + '/api/v1/query', params={'query': "(container_spec_cpu_quota{namespace='" + statsNamespace + "'})/(container_spec_cpu_period{namespace='" + statsNamespace + "'})", 'time':endTime})
    resultsContainersCpuLimits = responseContainersCpuLimits.json()['data']['result']

    containerCpuLimits = {}
    for result in resultsContainersCpuLimits:
        if result['metric']['pod'] not in containerCpuLimits : #create dict for this pod
            containerCpuLimits[result['metric']['pod']] = {}
        if 'container' in result['metric'] :  #read values of container
            if result['metric']['container'] != 'POD' :
                containerCpuLimits[result['metric']['pod']][result['metric']['container']] = result['value'][1]
        else :  #no container specified, refers to total pod measurement
            if 'name' not in result['metric'] :
                containerCpuLimits[result['metric']['pod']]['total'] = result['value'][1]

    return containerCpuLimits

#query to get memory limits of every container. Useful to calculate memory usage in %.
def get_memory_limits() :
    responseContainersMemoryLimits = requests.get(PROMETHEUS + '/api/v1/query', params={'query': "container_spec_memory_limit_bytes{namespace='" + statsNamespace + "'}", 'time':endTime})
    resultsContainersMemoryLimits = responseContainersMemoryLimits.json()['data']['result']

    containerMemoryLimits = {}
    for result in resultsContainersMemoryLimits:
        if result['metric']['pod'] not in containerMemoryLimits : #create dict for this pod
            containerMemoryLimits[result['metric']['pod']] = {}
        if 'container' in result['metric'] :  #read values of container
            if result['metric']['container'] != 'POD' :
                containerMemoryLimits[result['metric']['pod']][result['metric']['container']] = int(result['value'][1]) /1024/1024
        else :  #no container specified, refers to total pod measurement
            if 'name' not in result['metric'] :
                containerMemoryLimits[result['metric']['pod']]['total'] = int(result['value'][1]) /1024/1024

    return containerMemoryLimits


def make_file(fileName, limits, maxMeasurements, maxPercentage, avgMeasurements, avgPercentage):
    try:
        with open(fileName + '.csv','w') as csv_file:
            writer = csv.writer(csv_file)
            writer.writerow(['POD/CONTAINER', 'limit', 'max', 'max %', 'avg', 'avg %'])

            #before writing to file, filter pods that are not active constantly and might report empty values
            #also, sort the pods by name so that the created sheet is more readable and consistent
            runningPods = sorted((pod for pod in limits if pod.find("sc-monitor") == -1), key=lambda x:x.lower())
            for podName in runningPods:
                row = []
                row.append(podName)
                row.append(limits[podName]["total"]) if "total" in limits[podName] else row.append('')
                row.append(maxMeasurements[podName]["total"])
                row.append(maxPercentage[podName]["total"]) if "total" in maxPercentage[podName] else row.append('')
                row.append(avgMeasurements[podName]["total"]) if "total" in avgMeasurements[podName] else row.append('')
                row.append(avgPercentage[podName]["total"]) if "total" in avgPercentage[podName] else row.append('')

                writer.writerow(row)    
                #writer.writerow([podName, limits[podName]["total"], maxMeasurements[podName]["total"], maxPercentage[podName]["total"], avgMeasurements[podName]["total"], avgPercentage[podName]["total"]])
                if len(maxMeasurements[podName]) > 2: #if list size is 2, this means that the list has one pod measurement and one container measurement, so the container measurement is skipped
                    for containerName in maxMeasurements[podName]:
                        if "total" not in containerName:
                            row = []
                            row.append("    container: " + containerName)
                            row.append(limits[podName][containerName]) if containerName in limits[podName] else row.append('')
                            row.append(maxMeasurements[podName][containerName])
                            row.append(maxPercentage[podName][containerName]) if containerName in maxPercentage[podName] else row.append('')
                            row.append(avgMeasurements[podName][containerName]) if containerName in avgMeasurements[podName] else row.append('')
                            row.append(avgPercentage[podName][containerName]) if containerName in avgPercentage[podName] else row.append('')
                            writer.writerow(row) 
                            #writer.writerow(["    container: " + containerName, limits[podName][containerName], maxMeasurements[podName][containerName], maxPercentage[podName][containerName], avgMeasurements[podName][containerName], avgPercentage[podName][containerName]])

    except IOError:
        print("I/O error")


def make_file_various_metrics(fileName, latency, latencyQuantile95, envoyLatencyAvg, envoyLatencyQuantile95, tps, pmSamples):
    try:
        with open(fileName + '.csv','w') as csv_file:
            writer = csv.writer(csv_file)
            print(len(tps))
            pprint(latency)
            numOfWorkers = len(tps)
            titles = ['']
            tpsValues = ['TPS']
            latencyValues = ['Latency (ms)']
            totalTps = 0
            avgLatency = 0
            avgLatencyEnvoy = 0

            latency95th = ['Latency 95% (ms)']
            envoyLatencyAvgValues = ['Envoy Avg Latency (ms)']
            envoyLatency95th = ['Envoy Latency 95% (ms)']
            pmSamplesPrint = ['PM Samples']

            for i in range(1, numOfWorkers+1):
                titles.append("Worker "+str(i))
                tpsValues.append(tps[workerName + str(i)])
                latencyValues.append(latency[workerName + str(i)])
                envoyLatencyAvgValues.append(envoyLatencyAvg[workerName + str(i)])
                totalTps += tps[workerName + str(i)]
                avgLatency += latency[workerName + str(i)]
                avgLatencyEnvoy += envoyLatencyAvg[workerName + str(i)]
                latency95th.append('')
                envoyLatency95th.append('')
                pmSamplesPrint.append('')

            titles.append("Total")
            tpsValues.append(totalTps)
            avgLatency /= numOfWorkers
            avgLatencyEnvoy /= numOfWorkers

            latencyValues.append(avgLatency)
            envoyLatencyAvgValues.append(avgLatencyEnvoy)
            latency95th.append(latencyQuantile95)
            envoyLatency95th.append(envoyLatencyQuantile95)
            pmSamplesPrint.append(pmSamples)

            writer.writerow(titles)
            writer.writerow(tpsValues)
            writer.writerow(latencyValues)
            writer.writerow(latency95th)
            writer.writerow(envoyLatencyAvgValues)
            writer.writerow(envoyLatency95th)
            writer.writerow(pmSamplesPrint)

    except IOError:
        print("I/O error")

def make_file_various_metrics_bsf(fileName, pmSamples, tps_post, tps_get, latency_post, latency_get):
    try:
        with open(fileName + '.csv','w') as csv_file:
            writer = csv.writer(csv_file)
            #print(len(tps))
            #pprint(latency)
            numOfWorkersPost = len(tps_post)
            numOfWorkersGet = len(tps_get)
            titles = ['']
            tpsValuesPost = ['TPS_POST']
            tpsValuesGet = ['TPS_GET']
            latencyValuesPost = ['Latency Post(ms)']
            latencyValuesGet = ['Latency Get(ms)']
            totalTpsPost = 0
            totalTpsGet = 0
            avgLatencyPost = 0
            avgLatencyGet = 0
            #avgLatencyEnvoy = 0

            #latency95th = ['Latency 95% (ms)']
            #envoyLatencyAvgValues = ['Envoy Avg Latency (ms)']
            #envoyLatency95th = ['Envoy Latency 95% (ms)']
            pmSamplesPrint = ['PM Samples']

            numOfWorkers = max(numOfWorkersGet, numOfWorkersPost)

            for i in range(1, numOfWorkers+1):
                titles.append("Worker "+str(i))
                if i > (numOfWorkersPost):
                    tpsValuesPost.append("")
                else:
                    tpsValuesPost.append(tps_post[workerName + str(i)])
                    totalTpsPost += tps_post[workerName + str(i)]
                    
                
                if i > (numOfWorkersGet):
                    tpsValuesGet.append("")     
                else:
                    tpsValuesGet.append(tps_get[workerName + str(i)])
                    totalTpsGet += tps_get[workerName + str(i)]
                    
                    
                
                
                #envoyLatencyAvgValues.append(envoyLatencyAvg[workerName + str(i)])
                
                
                
                #avgLatencyEnvoy += envoyLatencyAvg[workerName + str(i)]
                #latency95th.append('')
                #envoyLatency95th.append('')
                pmSamplesPrint.append('')
                latencyValuesPost.append('')
                latencyValuesGet.append('')


            
            
            titles.append("Total")
            tpsValuesPost.append(totalTpsPost)
            tpsValuesGet.append(totalTpsGet)
            #making sure we never divide by 0
            
            #avgLatencyEnvoy /= numOfWorkers

            
            
            #envoyLatencyAvgValues.append(avgLatencyEnvoy)
            #latency95th.append(latencyQuantile95)
            #envoyLatency95th.append(envoyLatencyQuantile95)
            pmSamplesPrint.append(pmSamples)

            if latency_post == -1:
                latencyValuesPost.append('')
            else:
                latencyValuesPost.append(latency_post)
            

            if latency_get == -1:
                latencyValuesGet.append('')
            else:
                latencyValuesGet.append(latency_get)

            writer.writerow(titles)
            writer.writerow(tpsValuesPost)
            writer.writerow(tpsValuesGet)
            writer.writerow(latencyValuesPost)
            writer.writerow(latencyValuesGet)
            #writer.writerow(latency95th)
            #writer.writerow(envoyLatencyAvgValues)
            #writer.writerow(envoyLatency95th)
            writer.writerow(pmSamplesPrint)

    except IOError:
        print("I/O error")

#calculate cpu/memory usage percentage by dividing measured value with limits.
def calculate_percentage(containerValues, limits):
    containerValuesPercentage = {}
    for pod in containerValues :
        containerValuesPercentage[pod] = {}
        if pod in limits :
            for container in containerValues[pod] :
                if container in limits[pod] :
                    if limits[pod][container] != 0 :
                        containerValuesPercentage[pod][container] = round (float(containerValues[pod][container]) / float(limits[pod][container]), 2)
                    #else :
                        #print "container limits is 0 for " + pod + " " + container
                #else :
                    #print "container limits not specified for " + pod + " " + container
        #else:
            #print "pod limits not specified for " + pod

    return containerValuesPercentage

def main():
    if args.memory:
        containerMaxValues = get_memory_max_metrics()
        containerAvgValues = get_memory_avg_metrics()

        if args.export:
            fileName = args.export + '-memory'
            limits = get_memory_limits()
            containerMaxValuesPercentage = calculate_percentage(containerMaxValues, limits)
            containerAvgValuesPercentage = calculate_percentage(containerAvgValues,limits)
            make_file(fileName, limits, containerMaxValues, containerMaxValuesPercentage, containerAvgValues, containerAvgValuesPercentage)
    if args.cpu:
        containerMaxValues = get_cpu_max_metrics()
        containerAvgValues = get_cpu_avg_metrics()
        if args.export:
            fileName = args.export + '-cpu'
            limits = get_cpu_limits()
            containerMaxValuesPercentage = calculate_percentage(containerMaxValues, limits)
            containerAvgValuesPercentage = calculate_percentage(containerAvgValues,limits)
            make_file(fileName, limits, containerMaxValues, containerMaxValuesPercentage, containerAvgValues, containerAvgValuesPercentage)
    if args.traffic:
        pmSamples =  get_pm_metrics()
        if trafficIsBsf:
            avgTpsGet = get_tps_avg_metrics_bsf_get()
            avgTpsPost = get_tps_avg_metrics_bsf_post()
            avgLatencyGet = get_latency_avg_metrics_bsf_get()
            avgLatencyPost = get_latency_avg_metrics_bsf_post()
        else:
            avgLatency = get_latency_avg_metrics()
            avgTps = get_tps_avg_metrics()
            latencyQuantile95 = get_latency_quantile95_metrics()
            latencyEnvoyAvg = get_envoy_latency_avg_metrics()
            latencyEnvoyQuantile = get_envoy_latency_quantile95_metrics()
        fileName = args.export + '-traffic'
        if args.export:
            if trafficIsBsf:
                make_file_various_metrics_bsf(fileName, pmSamples, avgTpsPost, avgTpsGet, avgLatencyPost, avgLatencyGet)
            else:
                make_file_various_metrics(fileName, avgLatency, latencyQuantile95, latencyEnvoyAvg, latencyEnvoyQuantile, avgTps, pmSamples)


if __name__ == "__main__":
    main()
