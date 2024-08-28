#!/bin/bash 

me="`basename "$0"`"
umbrellaPath="esc/helm/eric-sc-umbrella/"
bobPath=".bob/"

#parse options..
args=( "$@" )
while [ "$#" -gt 0 ]; do
	case "$1" in
		-h|--help)
			echo "Script used for the cleanup of crds"
			echo "./scripts/cleanup_crds.sh --cluster hall022 --namespace kube-system"
			exit 0;
			;;
		-c=*|--cluster=*)
			KUBE_CONFIG="${1#*=}";			
        	;;
		-c|--cluster)
			KUBE_CONFIG="$2";
			shift;;
		-s=*|--service=*)
			serviceToUpgrade="${1#*=}";			
        	;;
		-s|--service)
			serviceToUpgrade="$2";
			shift;;
		-v=*|--version=*)
			serviceVersion="${1#*=}";			
        	;;
		-v|--version)
			serviceVersion="$2";
			shift;;
		-n=*|--namespace=*)
			KUBE_NAMESPACE="${1#*=}";			
        	;;
		-n|--namespace)
			KUBE_NAMESPACE="$2";
			shift;;
		--undeploy)
			undeploy=true;
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

# add -a to delete also corrupted releases
# update: 3/2/22
extractReleaseNames(){
	releasesCrds=$(helm list -A -a -f crd -o yaml  --kubeconfig ${bobPath}$KUBE_CONFIG.admin.conf | grep crd | grep name | awk -F "name: " '{print $2}')
	if [[ $debug ]]; then echo -e "Release Names found:  ${releasesCrds}"; fi
	
}

deleteReleases(){
	for releaseName in ${releasesCrds[@]}; do
		if [[ $debug ]]; then echo -e "Unistalling release of:  ${releaseName}"; fi
		helm uninstall --namespace $KUBE_NAMESPACE --kubeconfig ${bobPath}$KUBE_CONFIG.admin.conf ${releaseName}
	done
}

deleteReleasesX(){
	releaseName=$(echo eric-sc-$(echo $1 | awk -F "eric-" '{print $2}')-crd)
	if [[ $debug ]]; then echo -e "Unistalling release of:  ${releaseName}"; fi
	helm uninstall --namespace $KUBE_NAMESPACE --kubeconfig ${bobPath}$KUBE_CONFIG.admin.conf ${releaseName}

}

deleteKeptResources(){
	for releaseName in ${releasesCrds[@]}; do
		if [[ $debug ]]; then echo -e "Deleting kept resources of:  ${releaseName}"; fi
		kubectl delete crd -l app.kubernetes.io/instance==${releaseName} --namespace ${KUBE_NAMESPACE} --kubeconfig ${bobPath}$KUBE_CONFIG.admin.conf
	done
}

deleteKeptResourcesX(){
	releaseName=$(echo eric-sc-$(echo $1 | awk -F "eric-" '{print $2}')-crd)
	if [[ $debug ]]; then echo -e "Deleting kept resources of:  ${releaseName}"; fi
	kubectl delete crd -l app.kubernetes.io/instance==${releaseName} --namespace ${KUBE_NAMESPACE} --kubeconfig ${bobPath}$KUBE_CONFIG.admin.conf

}

delete_crds() {
	
	extractReleaseNames
	if [ $? -eq 1 ]; then
		echo "Error running ${me}, Failed to extract names of crds releases in namespace ${KUBE_NAMESPACE}";
		exit 1
	fi

	deleteReleases
	if [ $? -eq 1 ]; then
		echo "Error running ${me}, Failed to delete crds releases in namespace ${KUBE_NAMESPACE}";
		exit 1
	fi

	deleteKeptResources
	if [ $? -eq 1 ]; then
		echo "Error running ${me}, Failed to delete kept releases of  crds releases in namespace ${KUBE_NAMESPACE}";
		exit 1
	fi

}

if [  "$serviceToUpgrade" ]; then
	echo "Undeploy specific service"
	deleteReleasesX ${serviceToUpgrade}
	deleteKeptResourcesX ${serviceToUpgrade}
	exit 0
fi

delete_crds



