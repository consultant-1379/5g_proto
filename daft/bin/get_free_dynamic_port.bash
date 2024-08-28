#!/bin/bash

# Generate a random list of free dynamic ports on the system.
# Call with a number of free ports to generate, or 1 if not specified.
#
# Example code found at: https://unix.stackexchange.com/questions/55913/whats-the-easiest-way-to-find-an-unused-local-port

num_ports=$1
if [ -z $num_ports ]; then
    num_ports=1
fi
comm -23 <(seq 49152 65535 | sort) <(ss -Htan | awk '{print $4}' | cut -d':' -f2 | sort -u) | shuf -n $num_ports
