#!/usr/bin/env sh


localhost="localhost"
adminPort="$1"
metricsPort="$2"
adminHost="${localhost}:${adminPort}"
max_duration=600  # maximum duration in seconds

activeConnectionsURL="http://${adminHost}/stats?usedonly&filter=downstream_cx_active$"
gracefulDrainURL="http://${adminHost}/drain_listeners?graceful"

# Define internal listeners as an associative array to be used for lookups
declare -A internalListeners=(
  ["listener.admin.downstream_cx_active"]=1     #admin i/f
  ["listener.admin.main_thread.downstream_cx_active"]=1
  ["listener.0.0.0.0_${metricsPort}.downstream_cx_active"]=1 # tls listener for PM
  ["listener.[__]_${metricsPort}.downstream_cx_active"]=1
  ["listener.[__]_8043.downstream_cx_active"]=1 # internal n32c listener
  ["listener.0.0.0.0_8043.downstream_cx_active"]=1
)

function echoJson(){
  echo "{\"version\":\"1.1.0\",\"timestamp\":\"$(date +%Y-%m-%dT%T.%3N%:z)\",\"severity\":\"info\",\"id\":\"worker_shutdown.sh\",\"message\":\"$1\"}"
}

function getActiveConnections() {
  local stats=$(curl -s "$activeConnectionsURL")
  if [ -z "$stats" ]; then
    echoJson "Unable to get listener stats from Envoy" >&2
    return 1
  fi

  local activeConnections=0
  while IFS= read -r line; do
    IFS=':' read -ra parts <<< "$line"
    if [ "${#parts[@]}" -ne 2 ]; then
      echoJson "worker stat line is missing separator. line: $line" >&2
      continue
    fi

    if [[ ! "${parts[0]}" =~ ^listener\. || "${parts[0]}" =~ worker_ ]]; then
      continue
    fi

    # Skip known internal listeners
    if [[ ${internalListeners[${parts[0]}]} ]]; then
      continue
    fi

    # val is expected to be a number
    val=$(echo "${parts[1]}" | tr -d '[:space:]')
    if ! [[ "$val" =~ ^[0-9]+$ ]]; then
      echoJson "failed parsing worker stat ${parts[0]} line: $line" >&2
      continue
    fi
    activeConnections=$((activeConnections + val))
  done <<< "$stats"
  echo "$activeConnections" 

}

# This script is defined as a preStop hook for the worker container, effectively using the
# default value (30sec) of 'terminationGracePeriodSeconds'.
# A TERM signal will arrive after script completion or 30sec elapsing, whichever comes
# first
max_duration=21  # maximum duration in seconds

# start drain procedure
resp=$(curl -s -w"%{http_code}" -X POST "$gracefulDrainURL")
statusCode=${resp: -3}  # Get the last 3 characters (HTTP status code)
if [ "$statusCode" -ne 200 ]; then
  echoJson "Listener drain request failed with status code $statusCode" >&2
  exit 1
fi

# monitor active downstream connections for a maximum of 30sec
# until they are closed.
start_time=$(date +%s)
current_time=$(date +%s)
while [ $current_time -le $((start_time + max_duration)) ]; do
  activeConnections=$(getActiveConnections)

  if [ $? -eq 1 ]; then
    exit 1
  fi
  if [ "$activeConnections" -eq "0" ]; then
    break
  fi
  echoJson "Number of active connections: $activeConnections, retrying after 1s"
  sleep 1
  current_time=$(date +%s)
done


echoJson "Script finished after $((current_time - start_time)) seconds. Active connections: $activeConnections"

