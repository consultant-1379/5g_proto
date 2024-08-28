#!/usr/bin/env sh

# trap SIGTERM SIGINT signals and propagate them to child process (envoy)
trap 'kill -TERM $ENVOY_PID' TERM INT

# Get pod's id and set it as Envoy name
# Substitute values in the static config based on deployment parameters

POD_UID=$(hostname | cut -d'-' -f5,6)

echo "{\"version\":\"1.1.0\",\"timestamp\":\"$(date +"%Y-%m-%dT%T.%3N%:z")\",\"service_id\":\"eric-$NF-worker\",\"severity\":\"info\",\"message\":\"Starting $NF-worker, version info: $(cat /usr/local/bin/VERSION_INFO)\"}"

# Start Envoy
/usr/local/bin/envoy -c /etc/envoy/envoy.yaml --config-yaml "node: {id: 'envoy-$POD_UID'}" --drain-strategy immediate --drain-time-s 20 --disable-hot-restart --concurrency $CONCURRENCY --log-format "{\"version\":\"1.1.0\",\"timestamp\":\"%Y-%m-%dT%T.%e%z\",\"severity\":\"%l\",\"service_id\":\"eric-$NF-worker\",\"message\":\"%+\",\"metadata\":{\"proc_id\":\"%P\",\"ul_id\":\"%U\"},\"extra_data\":{\"location\":{\"src_file\":\"%g\",\"src_line\":\"%#\"},\"thread_info\":{\"thread_id\":\"%t\"},\"stream_id\":\"%Q\",\"connection_id\":\"%K\"}}" &

ENVOY_PID=$!
wait "$ENVOY_PID"
trap - TERM INT
wait "$ENVOY_PID"
