#!/bin/bash

#
# This scripts adds the 'k8s-registry.eccd.local' host name to the same line as 'nodelocal-api.eccd.local'.
#

if grep "k8s-registry.eccd.local" /etc/hosts; then
    echo "No need to update the file"
else
    if sudo -n sed -i '/nodelocal-api.eccd.local/ s/$/ k8s-registry.eccd.local/' /etc/hosts; then
        echo "File updated"
    else
        echo "Failed to update the file"
        exit 1
    fi
fi
