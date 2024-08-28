#!/bin/bash

nbicerts()
{
./install_certs.sh nbi
}

init()
{
	if [ ! -z ../.bob/var.namespace ];
	then
		export KUBE_NAMESPACE=`cat ../.bob/var.namespace`
	else
		export KUBE_NAMESPACE="5g-bsf-${USER}"	
	fi

	export KUBE_CONFIG="../.bob/${KUBE_HOST}.admin.conf"

	kubectl get secret eric-sc-monitor-secret --namespace ${KUBE_NAMESPACE} --kubeconfig ${KUBE_CONFIG} &> /dev/null

	if [ $? -eq 0 ];
	then
		export MONITOR_USER=$(kubectl get secret eric-sc-monitor-secret --namespace ${KUBE_NAMESPACE} --kubeconfig ${KUBE_CONFIG} -o jsonpath='{.data.username}' | base64 -d)

		export MONITOR_PWD=$(kubectl get secret eric-sc-monitor-secret --namespace ${KUBE_NAMESPACE} --kubeconfig ${KUBE_CONFIG} -o jsonpath='{.data.password}' | base64 -d)
	else
		echo "eric-sc-monitor secret not found, exiting"
		exit -1
	fi

	export MONITOR_PORT=$(kubectl get services eric-tm-ingress-controller-cr --namespace ${KUBE_NAMESPACE} --kubeconfig ${KUBE_CONFIG} -o jsonpath="{.spec.ports[1].nodePort}")
	export MONITOR_URL=$(kubectl get httpproxies.projectcontour.io eric-sc-cs-nbi --namespace ${KUBE_NAMESPACE} --kubeconfig ${KUBE_CONFIG} -o jsonpath="{.spec.virtualhost.fqdn}")
}

dumpPcfRtDbEtcd()
{
	init	
	curl -u ${MONITOR_USER}:${MONITOR_PWD} -X GET "https://${MONITOR_URL}:${MONITOR_PORT}/monitor/api/v0/commands?target=eric-bsf-m&command=dumpPcfRtDb" -k | jq
}

truncatePcfRtDbEtcd()
{
	init
	curl -u ${MONITOR_USER}:${MONITOR_PWD} -X PUT "https://${MONITOR_URL}:${MONITOR_PORT}/monitor/api/v0/commands?target=eric-bsf-m&command=truncatePcfRtDb" -k | jq
}

if [ "$#" -ne 1 ];
then
	echo "Only one argument is permitted"
	exit -1	
else
	if [ $1 = "dump" ];
	then
		dumpPcfRtDbEtcd
	elif [ $1 = "truncate" ]
	then
		truncatePcfRtDbEtcd
	elif [ $1 = "certs" ]
	then
		nbicerts
	else
		echo "Unsupported argument"
		exit -1	
	fi
fi
