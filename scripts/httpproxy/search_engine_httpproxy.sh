#!/bin/bash

KUBE_HOST=$2
SC_HTTPPROXY="eric-sc-cs-nbi"

if [ -s .bob/var.namespace ];
then
	export KUBE_NAMESPACE=`cat .bob/var.namespace`
elif [ -n "$KUBE_NAMESPACE" ]; then
    # Use already existing namespace, useful when executing the script from DAFT
    echo "Reusing already set namespace $KUBE_NAMESPACE"
else
	export KUBE_NAMESPACE="5g-bsf-${USER}"
fi

if [ -s .bob/${KUBE_HOST}.admin.conf ];
then
	export KUBE_CONFIG=".bob/${KUBE_HOST}.admin.conf"
else
	export KUBE_CONFIG="/home/${USER}/.kube/config"
fi

create_search_engine_httpproxy(){
	ingress_class=$(kubectl get httpproxy ${SC_HTTPPROXY} --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE -o jsonpath='{.metadata.annotations.kubernetes\.io/ingress\.class}')
	cat scripts/httpproxy/search-engine-httpproxy.yaml > scripts/httpproxy/temp_search_engine_httpproxy.yaml
	sed -i 's/namespace_template/'${KUBE_NAMESPACE}'/g' scripts/httpproxy/temp_search_engine_httpproxy.yaml
	sed -i 's/hostname_template/'${KUBE_HOST}'/g' scripts/httpproxy/temp_search_engine_httpproxy.yaml
	sed -i 's/ingress_class_template/'${ingress_class}'/g' scripts/httpproxy/temp_search_engine_httpproxy.yaml
	kubectl --kubeconfig $KUBE_CONFIG  --namespace $KUBE_NAMESPACE create -f scripts/httpproxy/temp_search_engine_httpproxy.yaml
	rm -rf scripts/httpproxy/temp_search_engine_httpproxy.yaml
}

delete_search_engine_httpproxy(){
	kubectl --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE delete httpproxy eric-data-search-engine --ignore-not-found
}

# Arguments: Node name, node fqdn output-directory
if [ "$1" = "create" ]
then
	delete_search_engine_httpproxy
	create_search_engine_httpproxy
elif [ "$1" = "delete" ]
then
	delete_search_engine_httpproxy
else
  	echo "Unsupported argument!"
fi
