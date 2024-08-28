#!/bin/bash

start_time=$(date +%s.%N)
echo "Staring cleanup of deployment resources [${start_time}]" > /tmp/${myDate}_force_clean.log
me="`basename "$0"`"
myDate=$(date +%Y_%m_%d_%H_%M_%S)
#parse options..
args=( "$@" )
while [ "$#" -gt 0 ]; do
	case "$1" in
		-h|--help)
			echo "Script used for the uninstall of helm releases and removal of deployment resources"
			echo "./scripts/force-clean.sh --cluster <cluster-name> --namespace <namespace> [--debug] [--dry-run]"
			echo "Optional parameters:"
			echo "-c|--cluster, set the cluster name to be used (connected with the kubeconfig file location, eg ~/.kube/<name>.config"
			echo "-n|--namespace, set the namespace to be used (eg 5g-bsf-eiffelesc-1, 5g-bsf-xevakot)"
			echo "-dn|--delete-namespace, true; delete/create namespace or false/defalut: clean namespaced resources"
			echo "--dry-run, execute the script without deletion of resources"
			echo "--debug, print debug logs (troubleshooting mode)"
			echo "Examples:"
			echo "./scripts/force-clean.sh -c hart074 --namespace 5g-bsf-eiffelesc-1"
			echo "./scripts/force-clean.sh --cluster hart074 --namespace 5g-bsf-eiffelesc-1 --debug --dry-run"
			echo "./scripts/force-clean.sh -c hart074 -n 5g-bsf-eiffelesc-1 -dn"
			exit 0;
			;;
		-c=*|--cluster=*)
			kubeHost="${1#*=}";			
			;;
		-c|--cluster)
			kubeHost="$2";
			shift;;
		-n=*|--namespace=*)
			namespace="${1#*=}";			
			;;
		-n|--namespace)
			namespace="$2";
			shift;;
		-dn|--delete-namespace)
			deleteNamespace=true;
			shift;;
		--dry-run)
			dryRun=true;
			;;
		--debug)
			debug=true;
			;;
		*)
			echo "Error running ${me}, Unknown option: $1";
			exit 1;
			;;
	esac
	((argscount++));
	shift
done

# function to uninstall helm releases
function deleteHelmRelease()
{
	echo "Uninstall helm release $1"
	if [ $dryRun ]; then
		echo "Skip uninstall of helm release $1" >> /tmp/${myDate}_force_clean.log
	else
		helm uninstall $1 --kubeconfig ~/.kube/${kubeHost}.config --namespace ${namespace};
		if [ $? -ne 0 ]; then
			echo "Error: Failed to uninstall helm release: $1";
		fi
		# wait 5 seconds till helm install actions initiate
		sleep 5
	fi
}

# function to list namespaced resources based on helm release label
function getNamespacedResources()
{
	resources=()
	label_selector="app.kubernetes.io/instance=${1}"  # Modify this line with your desired label selector
	echo "Extract additional k8s namespaced resources with label '$label_selector' not removed with helm uninstall"
	nr=($(kubectl api-resources --verbs=list --namespaced=true -o name --kubeconfig ~/.kube/${kubeHost}.config 2>> /tmp/${myDate}_force_clean.log))
	echo "Identified ${#nr[@]} k8s namespace scoped api-resources" >> /tmp/${myDate}_force_clean.log
	for nrIndex in "${!nr[@]}"; do
		echo "Checking api-resource [${nrIndex}]: ${nr[$nrIndex]}" >> /tmp/${myDate}_force_clean.log
		dummy=($(kubectl get ${nr[$nrIndex]} --show-kind --ignore-not-found --selector="${label_selector}" -o name --kubeconfig ~/.kube/${kubeHost}.config -A 2>> /tmp/${myDate}_force_clean.log))
		if [ $? -ne 0 ]; then
			echo "Error: Failed to extract ${nr[$nrIndex]} resource with label '$label_selector'"
		fi
		echo "Found ${#dummy[@]} resources of type ${nr[$nrIndex]}" >> /tmp/${myDate}_force_clean.log
		resources+=(${dummy[@]})
	done
	totalNamespacedResources+=(${resources[@]})
	echo "Identified ${#resources[@]} resources to cleanup for ${1} helm release"
}

# function to list cluster scoped resources based on input helm release label
function getClusterResources()
{
	resources=()
	label_selector="app.kubernetes.io/instance=${1}"  # Modify this line with your desired label selector
	echo "Extract additional k8s cluster resources with label '$label_selector' not removed with helm uninstall"
	cr=($(kubectl api-resources --verbs=list --namespaced=false -o name --kubeconfig ~/.kube/${kubeHost}.config 2>> /tmp/${myDate}_force_clean.log))
	echo "Identified ${#cr[@]} k8s cluster scoped api-resources" >> /tmp/${myDate}_force_clean.log
	for crIndex in "${!cr[@]}"; do
		echo "Checking api-resource [${crIndex}]: ${cr[$crIndex]}" >> /tmp/${myDate}_force_clean.log
		dummy=($(kubectl get ${cr[$crIndex]} --show-kind --ignore-not-found --selector="${label_selector}" -o name --kubeconfig ~/.kube/${kubeHost}.config -A 2>> /tmp/${myDate}_force_clean.log))
		if [ $? -ne 0 ]; then
			echo "Error: Failed to extract ${cr[$crIndex]} resource with label '$label_selector'"
		fi
		echo "Found ${#dummy[@]} resources of type ${cr[$crIndex]}" >> /tmp/${myDate}_force_clean.log
		resources+=(${dummy[@]})
	done
	totalClusterResources+=(${resources[@]})
	echo "Identified ${#resources[@]} resources to cleanup for ${1} helm release"
}

# function to check if there are terminating POD resources
# Wait till all terminating resources disappear
function checkTerminating()
{
	label_selector="app.kubernetes.io/instance=${1}"  # Modify this line with your desired label selector
	while true; do
		terminating_pods="$(kubectl get pods -n "$namespace" --field-selector=status.phase=Terminating --selector="${label_selector}" -o jsonpath='{.items}' --kubeconfig ~/.kube/${kubeHost}.config -A)"
		if [ -z "$terminating_pods" ] || [[ "$terminating_pods" == "[]" ]]; then
			echo "No pods in Terminating state with label '${label_selector}'. Continuing..."
			break
		fi
		echo "Pods in Terminating state with label '$label_selector':"
		echo "$terminating_pods"
		echo "Waiting for all pods to terminate..."
		sleep 5
	done
}

# function to reorder resources to cleanup based on matched keywords
# Move matched resources on top and make sure that those will be deleted first
function reOrder()
{
	first=()
	second=()
	for element in "${@:2}"; do
		if [[ $element == ${1}* ]]; then
			first+=("$element")
		else
			second+=("$element")
		fi
	done
	echo "Found ${#first[@]} match keywords with ${1}, reset ${#second[@]} moved in the end" >> /tmp/${myDate}_force_clean.log
}

function deleteNamespaceResources()
{
	kubectl delete namespace ${namespace} --kubeconfig ~/.kube/${kubeHost}.config;
	if [ "$?" -ne 0 ]; then
		echo "Error: Failed to delete k8s namespace ${namespace}"
		exit 1
	else
		kubectl create namespace ${namespace} --kubeconfig ~/.kube/${kubeHost}.config;
		if [ "$?" -ne 0 ]; then
			echo "Error: Failed to create k8s namespace ${namespace}"
			exit 1
		fi
	fi
}

if [ -z "$kubeHost" ]; then
	echo "Error running ${me}, Mandatory parameter cluster name not set"
	exit 1
fi

if [ -z "$namespace" ]; then
	echo "Error running ${me}, Mandatory parameter namespace not set"
	exit 1
fi

if [ -z "$debug" ]; then
	echo "Execution without debug mode"  >> /tmp/${myDate}_force_clean.log
else
	echo "Execution with debug mode"  >> /tmp/${myDate}_force_clean.log
	set -x
fi

echo "Checking helm releases on namespace ${namespace} of cluster ${kubeHost}"
helmReleases=($(helm list --kubeconfig ~/.kube/${kubeHost}.config --namespace ${namespace} -qa --kubeconfig ~/.kube/${kubeHost}.config))

if [ ${#helmReleases[@]} -eq 0 ]; then
	echo "No resources to clean";
	exit 0;
else
	echo "Identified ${#helmReleases[@]} helm releases: ${helmReleases[@]}"
fi

totalResources=()
totalNamespacedResources=()
totalClusterResources=()
# Uninstall helm releases
for hrIndex in  "${!helmReleases[@]}"; do
	deleteHelmRelease ${helmReleases[$hrIndex]}
done

# wait for resources to terminate
for hrIndex in  "${!helmReleases[@]}"; do
	checkTerminating ${helmReleases[$hrIndex]}
done

# Clean resources
for hrIndex in  "${!helmReleases[@]}"; do
	if [ $deleteNamespace ]; then
		deleteNamespaceResources
	else
		getNamespacedResources ${helmReleases[$hrIndex]}
	fi
	getClusterResources ${helmReleases[$hrIndex]}
done

if ! [ $deleteNamespace ]; then
	reOrder "httpproxies" ${totalNamespacedResources[@]}
	totalNamespacedResources=("${first[@]}" "${second[@]}")
	reOrder "serviceaccounts" ${totalNamespacedResources[@]}
	totalNamespacedResources=("${first[@]}" "${second[@]}")
	reOrder "clusterroles" ${totalNamespacedResources[@]}
	totalNamespacedResources=("${first[@]}" "${second[@]}")
	reOrder "pods" ${totalNamespacedResources[@]}
	totalNamespacedResources=("${first[@]}" "${second[@]}")
	reOrder "statefulsets" ${totalNamespacedResources[@]}
	totalNamespacedResources=("${first[@]}" "${second[@]}")
	reOrder "daemonsets" ${totalNamespacedResources[@]}
	totalNamespacedResources=("${first[@]}" "${second[@]}")
	reOrder "cronjobs" ${totalNamespacedResources[@]}
	totalNamespacedResources=("${first[@]}" "${second[@]}")
	reOrder "jobs" ${totalNamespacedResources[@]}
	totalNamespacedResources=("${first[@]}" "${second[@]}")
	reOrder "replicasets" ${totalNamespacedResources[@]}
	totalNamespacedResources=("${first[@]}" "${second[@]}")
	reOrder "deployments" ${totalNamespacedResources[@]}
	totalNamespacedResources=("${first[@]}" "${second[@]}")
	reOrder "persistentvolumeclaims" ${totalNamespacedResources[@]}
	totalNamespacedResources=("${first[@]}" "${second[@]}")
	reOrder "services" ${totalNamespacedResources[@]}
	totalNamespacedResources=("${first[@]}" "${second[@]}")

	echo "Deleting ${#totalNamespacedResources[@]} additional k8s namespaced resources"
	echo "Deleting ${#totalNamespacedResources[@]} additional k8s namespaced resources: ${totalNamespacedResources[@]}" >> /tmp/${myDate}_force_clean.log
	for rIndex in "${!totalNamespacedResources[@]}"; do
		echo "Deleting totalNamespacedResources[${rIndex}]: ${totalNamespacedResources[$rIndex]}" >> /tmp/${myDate}_force_clean.log
		if [ $dryRun ]; then
			echo "skip deletion of k8s namespaced resource ${totalNamespacedResources[$rIndex]}" >> /tmp/${myDate}_force_clean.log
		else
			echo "Deleting k8s namespaced resource ${totalNamespacedResources[$rIndex]}" >> /tmp/${myDate}_force_clean.log
			kubectl delete ${totalNamespacedResources[$rIndex]} --ignore-not-found --timeout 15s --namespace ${namespace} --kubeconfig ~/.kube/${kubeHost}.config;
			if [ "$?" -ne 0 ]; then
				echo "Error: Failed to delete k8s namespaced resource: ${totalNamespacedResources[$rIndex]}"
			fi
		fi
	done
fi

echo "Deleting ${#totalClusterResources[@]} additional k8s cluster resources"
echo "Deleting ${#totalClusterResources[@]} additional k8s cluster resources: ${totalClusterResources[@]}" >> /tmp/${myDate}_force_clean.log
for rIndex in "${!totalClusterResources[@]}"; do
	echo "Deleting totalClusterResources[${rIndex}]: ${totalClusterResources[$rIndex]}" >> /tmp/${myDate}_force_clean.log
	if [ $dryRun ]; then
		echo "skip deletion of k8s cluster resource ${totalClusterResources[$rIndex]}" >> /tmp/${myDate}_force_clean.log
	else
		echo "Deleting k8s cluster resource ${totalClusterResources[$rIndex]}" >> /tmp/${myDate}_force_clean.log
		kubectl delete ${totalClusterResources[$rIndex]} --ignore-not-found --timeout 15s -A --kubeconfig ~/.kube/${kubeHost}.config;
		if [ "$?" -ne 0 ]; then
			echo "Error: Failed to delete k8s cluster resource: ${totalClusterResources[$rIndex]}"
		fi
	fi
done

end_time=$(date +%s.%N)
elapsed_time=$(echo "$end_time - $start_time" | bc)
echo "Complete cleanup of deployment resources [${start_time}], total execution time: ${elapsed_time}" >> /tmp/${myDate}_force_clean.log

echo "Cleaning up old log files (keep always the last 10)"
force_clean_logs=($(ls -t /tmp | grep force_clean | tail -n +11))
for force_clean_log in ${force_clean_logs[@]}; do
	rm -rf /tmp/${force_clean_log}
done
echo "Execution time: ${elapsed_time} seconds, logs can be found in /tmp/${myDate}_force_clean.log"