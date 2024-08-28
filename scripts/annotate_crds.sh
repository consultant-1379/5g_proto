#!/bin/bash
#Script that will annotate the already installed CRDs in order to be attached to the new charts.


# List with SC CRDs
declare -a crdList=("eric-tm-ingress-controller-cr-crd" "eric-sec-sip-tls-crd" "eric-sec-certm-crd" )
 
for k in ${crdList[@]}; do 
    echo "===>"$k
    for i in $(kubectl get crd -l app.kubernetes.io/name=$k|grep -v NAME | awk '{ print $1 }'); do
        echo "===>"$i
        kubectl annotate crd $i meta.helm.sh/release-name=$k --overwrite
    done
done
