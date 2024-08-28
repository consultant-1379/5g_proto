#!/bin/bash +x
# Gracefully shutdown with helm uninstall

NAMESPACE=$1
RESULT=0;
DELETE_API_RESOURCES=$2

if [ -z "$1" ]
then
	echo "Namespace input is mandatory"
	exit 1
fi

if [ -z $KUBECONFIG ]
then
	echo "Kubeconfig is mandatory parameter"
	exit 1
fi

if [ -z "$(helm ls --namespace ${NAMESPACE} -q  )" ]; then 
	echo "No release found in namespace ${NAMESPACE}"
	exit 1;
fi

for i in `helm ls --namespace ${NAMESPACE} -q`;
do
	echo "Deleting release $i"
	output=$(helm uninstall $i --namespace $NAMESPACE --timeout 600s --debug --kubeconfig ${KUBECONFIG});
	if [ "$?" -ne 0 ] && [[ ${output} == *"error"* ]]; then
		RESULT=1
		echo "Deletion of helm release $i for namespace ${NAMESPACE} failed!"
	fi
	if [[ "${DELETE_API_RESOURCES}" == "all" ]]; then
		echo "Deleting all resources for release ${i}"
		kubectl api-resources --kubeconfig ${KUBECONFIG} --verbs=list --namespaced=true -o name | xargs -n 1 --verbose kubectl delete --ignore-not-found -l app.kubernetes.io/instance=${i} --kubeconfig ${KUBECONFIG} -A;
		kubectl api-resources --kubeconfig ${KUBECONFIG} --verbs=list --namespaced=false -o name | xargs -n 1 --verbose kubectl delete --ignore-not-found -l app.kubernetes.io/instance=${i} --kubeconfig ${KUBECONFIG} -A;
	elif [[ "${DELETE_API_RESOURCES}" == "namespace" ]]; then
		echo "Deleting namespaced resources for release ${i}"
		kubectl api-resources --kubeconfig ${KUBECONFIG} --verbs=list --namespaced=true -o name | xargs -n 1 --verbose kubectl delete --ignore-not-found -l app.kubernetes.io/instance=${i} --kubeconfig ${KUBECONFIG} -A;
	elif [[ "${DELETE_API_RESOURCES}" == "cluster" ]]; then
		echo "Deleting all resources for release ${i}"
		kubectl api-resources --kubeconfig ${KUBECONFIG} --verbs=list --namespaced=false -o name | xargs -n 1 --verbose kubectl delete --ignore-not-found -l app.kubernetes.io/instance=${i} --kubeconfig ${KUBECONFIG} -A;
	else
		echo "Only helm uninstall actions executed without the deletion of namespaced/cluster resources using k8s api-resources"
	fi
done;
exit ${RESULT}
