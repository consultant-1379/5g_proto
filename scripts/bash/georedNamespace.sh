#!/bin/bash
export NAMESPACE1="$1";
export NAMESPACE2="$2";

export KUBE_CFG="--kubeconfig ${HOME}/.kube/${KUBE_HOST}.config";


res=$(kubectl $KUBE_CFG get ns | grep $NAMESPACE1)
if [ -z "$res" ]; then 
echo "Namespace $NAMESPACE1 is missing, creating"; 
kubectl $KUBE_CFG create ns $NAMESPACE1
else 
echo "Found $NAMESPACE1"; 
fi

res=$(kubectl $KUBE_CFG get ns | grep $NAMESPACE2)
if [ -z "$res" ]; then 
echo "Namespace $NAMESPACE2 is missing, creating"; 
kubectl $KUBE_CFG create ns $NAMESPACE2
else 
echo "Found $NAMESPACE2"; 
fi