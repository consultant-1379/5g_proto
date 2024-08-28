#!/bin/bash -x

KUBE_HOST=$2
SC_HTTPPROXY="eric-sc-cs-nbi"
letters=({a..z})

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

createHttpproxy(){
	echo "Fill up httpproxy template with following data and generate temporary file ${1}_httpproxy.yaml"
	echo "name: ${1}"
	echo "namespace: ${2}"
	echo "hostname: ${3}"
	echo "ingress class: ${4}"
	echo "fqdn prefix: ${5}"
	echo "service: ${6}"

	cat scripts/httpproxy/worker-envoy-admin-httpproxy.yaml > scripts/httpproxy/${1}_httpproxy.yaml
	sed -i 's/httpproxyname_template/'${1}'/g' scripts/httpproxy/${1}_httpproxy.yaml
	sed -i 's/namespace_template/'${2}'/g' scripts/httpproxy/${1}_httpproxy.yaml
	sed -i 's/hostname_template/'${3}'/g' scripts/httpproxy/${1}_httpproxy.yaml
	sed -i 's/ingress_class_template/'${4}'/g' scripts/httpproxy/${1}_httpproxy.yaml
	sed -i 's/prefix_template/'${5}'/g' scripts/httpproxy/${1}_httpproxy.yaml
	sed -i 's/service_template/'${6}'/g' scripts/httpproxy/${1}_httpproxy.yaml

	echo "Create httpproxy from temporary file ${1}_httpproxy.yaml"
	#cat ${1}_httpproxy.yaml
	kubectl create -f scripts/httpproxy/${1}_httpproxy.yaml --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE

	echo "Delete httpproxy temporary file ${1}_httpproxy.yaml"
	rm -rf scripts/httpproxy/${1}_httpproxy.yaml
}

create_services(){
	# delete previous worker-admin services
	delete_services

	# list worker pods to expose
	pods=($(kubectl get pods --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE | awk '{ print $1 }' | grep -E "scp-worker|sepp-worker"))
	echo "Identified ${#pods[@]} worker pods to expose"
	for pod in ${pods[@]}; do echo ${pod}; done

	## TODO: create function to avoid doublication of same content for each scp/sepp nf
	# check scp worker pods and expose them
	if [[ ${pods[@]} =~ "scp" ]]; then
		echo "SCP workers identified"
		scpPods=($(kubectl get pods --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE | awk '{ print $1 }' | grep "scp-worker"))

		echo "Exposing each scp worker pod"
		for (( i=0; i<${#scpPods[@]}; i++ )); do
			echo "Patching ${scpPods[j]} pod with label -> scp-worker=${letters[i]}"
			kubectl patch pod ${scpPods[i]} --type=json -p="[{\"op\": \"add\", \"path\": \"/metadata/labels/scp-worker\", \"value\": \"${letters[i]}\"}]" --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE
			echo "Exposing ${scpPods[j]} pod -> service eric-scp-worker-admin-${letters[i]}"
			kubectl expose pod ${scpPods[i]} --name eric-scp-worker-admin-${letters[i]} --selector "scp-worker=${letters[i]}" --type NodePort --protocol TCP --port 9901 --target-port 9901 --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE
		done
	else
		echo "No SCP workers identified"
	fi

	# check sepp worker pods and expose them
	if [[ ${pods[@]} =~ "sepp" ]]; then
		echo "SEPP workers identified"
		seppPods=($(kubectl get pods --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE | awk '{ print $1 }' | grep "sepp-worker"))

		echo "Exposing each sepp worker pod"
		for (( j=0; j<${#seppPods[@]}; j++ )); do
			echo "Patching ${seppPods[j]} pod with label -> sepp-worker=${letters[j]}"
			kubectl patch pod ${seppPods[j]} --type=json -p="[{\"op\": \"add\", \"path\": \"/metadata/labels/sepp-worker\", \"value\": \"${letters[j]}\"}]" --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE
			echo "Exposing ${seppPods[j]} pod -> service eric-sepp-worker-admin-${letters[j]}"
			kubectl expose pod ${seppPods[j]} --name eric-sepp-worker-admin-${letters[j]} --selector "sepp-worker=${letters[j]}" --type NodePort --protocol TCP --port 9901 --target-port 9901 --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE
		done
	else
		echo "No SEPP workers identified"
	fi

	# expose deployments
	#for pod in ${pods[@]}; do
		#echo "Exposing ${deployment} deployment -> service ${deployment}-admin"
		#kubectl expose deployment ${deployment} --name ${deployment}-admin --type NodePort --protocol TCP --port 9901 --target-port 9901 --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE
		#serviceName=$(echo "${pod}" | sed 's/worker-.*/worker/g')
		#kubectl expose pod ${pod} --name ${serviceName}-admin --type NodePort --protocol TCP --port 9901 --target-port 9901 --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE
	#done

	echo "List worker services including new admin services"
	kubectl get service --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE | grep -E "eric-scp-worker|eric-sepp-worker"
}

delete_services(){
	# check if worker pods already exposed
	services=($(kubectl get svc --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE | awk '{ print $1 }' | grep -E "eric-scp-worker-admin|eric-sepp-worker-admin"))

	# list identified worker-admin services
	echo "Identified ${#services[@]} worker-admin services"
	for service in ${services[@]}; do echo ${service}; done

	# cleanup worker-admin services if already exists
	if [[ ${#services[@]} -ne 0 ]]; then
		echo "Cleaning up existing worker-admin services"
		for service in ${services[@]}; do
			kubectl delete service ${service} --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE
		done
	fi
}

delete_pod_labels(){
	# list worker pods
	pods=($(kubectl get pods --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE | awk '{ print $1 }' | grep -E "scp-worker|sepp-worker"))
	echo "Identified ${#pods[@]} worker pods"
	for pod in ${pods[@]}; do echo ${pod}; done

	## TODO: create function to avoid doublication of same content for each scp/sepp nf
	# check scp worker pods and expose them
	if [[ ${pods[@]} =~ "scp" ]]; then
		echo "SCP workers identified"
		scpPods=($(kubectl get pods --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE | awk '{ print $1 }' | grep "scp-worker"))

		echo "Remove labels added during expose to each scp worker pod"
		for (( i=0; i<${#scpPods[@]}; i++ )); do
			echo "Patching ${scpPods[j]} pod remove label -> scp-worker"
			kubectl patch pod ${scpPods[i]} --type=json -p="[{\"op\": \"remove\", \"path\": \"/metadata/labels/scp-worker\"}]" --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE
			echo "List labels for pod ${scpPods[i]}"
			kubectl get pod ${scpPods[i]} --show-labels --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE
		done
	else
		echo "No SCP workers identified"
	fi

	# check sepp worker pods and expose them
	if [[ ${pods[@]} =~ "sepp" ]]; then
		echo "SEPP workers identified"
		seppPods=($(kubectl get pods --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE | awk '{ print $1 }' | grep "sepp-worker"))
		echo "Remove labels added during expose to each sepp worker pod"
		for (( j=0; j<${#seppPods[@]}; j++ )); do
			echo "Patching ${seppPods[j]} pod remove label -> sepp-worker"
			kubectl patch pod ${seppPods[j]} --type=json -p="[{\"op\": \"remove\", \"path\": \"/metadata/labels/sepp-worker\"}]" --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE
			echo "List labels for pod ${seppPods[j]}"
			kubectl get pod ${seppPods[j]} --show-labels --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE
		done
	else
		echo "No SEPP workers identified"
	fi
}

create_httpproxies(){
	delete_httpproxies

	workerServices=($(kubectl get service --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE | grep -E ".*worker-admin.*" | awk '{ print $1 }'))

	# remove eric- and worker- from eric-scp-worker-admin-a and transform it to scp-admin-a
	httpProxyPrefix=($(kubectl get service --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE | grep -E ".*worker-admin.*" | awk '{ print $1 }' | sed -e 's/eric-//g' -e 's/worker-//g'))

	if [[ ${#workerServices[@]} -gt 0 ]]; then
		# create httpproxy
		for (( k=0; k<${#workerServices[@]}; k++ )); do
			# create httprroxy with inputs:
			# httpproxy name(eg ${SC_HTTPPROXY}, scp-admin-a),
			# namespace(5g-bsf-ekoteva),
			# host name(eg hall022),
			# ingress_class(eg sc),
			# fqdn_prefix(eg pm, scp-admin-a),
			# service name (eg eric-pm-server, eric-scp-worker-admin-a)
			createHttpproxy ${httpProxyPrefix[k]} $KUBE_NAMESPACE ${KUBE_HOST} ${ingressClass} ${httpProxyPrefix[k]} ${workerServices[k]}
		done

		echo "List all httpproxies including new worker-admin httpproxies"
		kubectl get httpproxy --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE
	else
		echo "No worker-admin services identified. Skipping the creation of httpproxies."
	fi
}

delete_httpproxies(){
	# check if worker-admin httpproxy already present
	httpproxies=($(kubectl get httpproxy --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE | awk '{ print $1 }' | grep -E "scp-admin|sepp-admin"))

	# list identified worker-admin httpproxy
	echo "Identified ${#httpproxies[@]} worker-admin httpproxies"
	for httpproxy in ${httpproxies[@]}; do echo ${httpproxy}; done

	# cleanup worker-admin services if already exists
	if [[ ${#httpproxies[@]} -ne 0 ]]; then
		echo "Removing ${#httpproxies[@]} envoy-admin httpproxies"
		for httpproxy in ${httpproxies[@]}; do
			kubectl delete httpproxy ${httpproxy} --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE
		done
	fi
}

verify_httpproxies(){
	# list deployments to expose
	pods=($(kubectl get pods --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE | awk '{ print $1 }' | grep -E "scp-worker|sepp-worker"))
    if [ ${#pods[@]} -eq 0 ]; then
        echo "No pods found to expose";
        return 1;
    fi
	echo "Identified ${#pods[@]} pods to expose"
	for pod in ${pods[@]}; do echo ${pod}; done

	# extract iccr service name
	iccrServiceName=$(kubectl get svc --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE | awk '{ print $1 }' | grep -E ".*-cr$")
	if [ ! -z "$iccrHttpPort" ]; then
		echo "Failed to extract ICCR service name"
		return 1
	fi

	# extract iccr nodePort for port=80
	iccrHttpPort=$(kubectl get svc ${iccrServiceName} -o=jsonpath="{.spec.ports[?(@.port==80)].nodePort}")
	if [ ! -z "$iccrHttpPort" ]; then
		echo "Failed to extract ICCR http port"
		return 1
	fi

	# print the control_plane.identifier from each exposed worker admin interface
	httpproxyFqdns=($(kubectl get httpproxy --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE | grep -E "scp-admin|sepp-admin" | awk '{ print $2 }'))
	for httpproxyFqdn in ${httpproxyFqdns[@]}; do
		echo "Checking httpproxy: ${httpproxyFqdn}"
		curl -sX POST "http://${httpproxyFqdn}:${iccrHttpPort}/stats" | grep "control_plane.identifier"
	done
}

# identify iccr ingress class
ingressClass=$(kubectl get httpproxy ${SC_HTTPPROXY} --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE -o jsonpath='{.metadata.annotations.kubernetes\.io/ingress\.class}')
echo "ICCR ingress class identified: ${ingressClass}"

# Arguments: Node name, node fqdn output-directory
if [ "$1" = "create" ]; then
	# expose worker pods envoy admin interface and create needed services
	create_services

	# create httpproxy resources for each new service that corresponds to specific worker pod
	create_httpproxies

elif [ "$1" = "delete" ]; then
	# delete services for each exposed worker pod envoy admin interface
	delete_services
	echo "List worker services"
	kubectl get service --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE | grep -E "eric-scp-worker|eric-sepp-worker"

	# delete httpproxy resources
	delete_httpproxies
	echo "List all httpproxies"
	kubectl get httpproxy --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE

	# delete added pod labels introduced when worker pod envoy admin interface exposed
	delete_pod_labels
elif [ "$1" = "verify" ]; then
	# verify exposed admin interface works via ICCR
	# print pod name and envoy stats control_plane.identifier -> should match
	verify_httpproxies
else
	echo "Unsupported argument!"
fi
