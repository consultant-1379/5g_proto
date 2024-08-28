#!/bin/bash

# Start child process with arguments, propagating SIGINT and SIGTERM signals to child
CMD="$1"
shift
trap 'kill ${JPID}; wait ${JPID}' SIGINT SIGTERM;
${CMD} "$@" &

JPID="$!"
wait ${JPID}
