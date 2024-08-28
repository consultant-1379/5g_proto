#!/bin/bash -x

if [ ! -z .bob/var.namespace ]; then
	export KUBE_NAMESPACE=`cat .bob/var.namespace`
else
	export KUBE_NAMESPACE="5g-bsf-${USER}"
fi

create_configmaps() {
	delete_configmaps

	# .bob/faultmappings/ folder is created via config_gen_v2.py script and contains all faultmapping json files
	kubectl --kubeconfig ${KUBECONFIG} --namespace ${KUBE_NAMESPACE} create configmap eric-fh-alarm-handler-faultmappings --from-file=.bob/faultmappings
	if [ $? -ne 0 ]; then
		echo "Failed to create configmap eric-fh-alarm-handler-faultmappings"
		exit 1
	fi
}

delete_configmaps() {
	kubectl --kubeconfig ${KUBECONFIG} --namespace ${KUBE_NAMESPACE} delete configmap eric-fh-alarm-handler-faultmappings --ignore-not-found
	if [ $? -ne 0 ]; then
		echo "Failed to delete configmap eric-fh-alarm-handler-faultmappings"
		exit 1
	fi
}

if [ "$1" = "create" ]; then
	create_configmaps
elif [ "$1" = "delete" ]; then
	delete_configmaps
else
  	echo "Unsupported argument!"
fi