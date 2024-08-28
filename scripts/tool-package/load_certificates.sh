#!/bin/sh

#Helper script to load/remove certificates for the K6/CHFSim package on the verification system.

echo "This script is outdated and deprecated. Please see documentation how to properly create the secrets."
#eedcsi: Why is it deprecated?

KUBE_NAMESPACE=$1

if [ "$2" = "create" ]
then
    kubectl --namespace $KUBE_NAMESPACE create secret generic k6-certificates --from-file=certificates/k6
    kubectl --namespace $KUBE_NAMESPACE create secret generic chf-certificates --from-file=certificates/chfsim
    kubectl --namespace $KUBE_NAMESPACE create secret generic nrf-certificates --from-file=certificates/nrfsim

elif [ "$2" = "delete" ]
then
    kubectl --namespace $KUBE_NAMESPACE delete secret/k6-certificates
    kubectl --namespace $KUBE_NAMESPACE delete secret/chf-certificates
    kubectl --namespace $KUBE_NAMESPACE delete secret/nrf-certificates
else
    echo "Usage: load_certificates.sh <namespace> <create|delete>"
fi
