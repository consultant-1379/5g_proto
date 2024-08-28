#!/bin/sh

trap 'kill ${JPID}; wait ${JPID}' SIGINT SIGTERM;

JPID="$!"
wait ${JPID}

if [ -z "$SERVICE_PORT" ]; then
  SERVICE_PORT=5556
fi

if [ -z "$JVM_OPTS" ]; then
  JVM_OPTS="-Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.port=5555 -Dcom.sun.management.jmxremote.host=127.0.0.1"
fi

# check if env var is set or fallback to default config map
[ -z "$CONFIG_YML" ] && CONFIG_YML=/opt/jmx_exporter/config.yml

# cleck if config map was mounted successfully or fallback to default
[ -f "$CONFIG_YML" ] || CONFIG_YML=/opt/jmx_exporter/config.yml

java $JVM_OPTS -Djava.util.logging.config.file=/opt/jmx_exporter/logging.properties -cp /java-exec/libs/*:/java-exec/classes io.prometheus.jmx.WebServer $SERVICE_PORT $CONFIG_YML &

JPID="$!"
wait ${JPID}
