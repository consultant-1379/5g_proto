#!/bin/bash 

me="`basename "$0"`"

#parse options..
args=( "$@" )
while [ "$#" -gt 0 ]; do
	case "$1" in
		-h|--help)
			echo "Script used for check if crds exist in a cluster"
			exit 0;
			;;
		-cluster=*|--cluster=*)
			cluster="${1#*=}";
			;;
		-cluster|--cluster)
			cluster="$2";
			shift;;
		-n=*|--namespace=*)
			crdsNamespace="${1#*=}";
			;;
		-n|--namespace)
			crdsNamespace="$2";
			shift;;
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

crdsResourcesExist(){
	export crds_resources=$(kubectl -n $crdsNamespace get crds | grep -cE "cassandraclusters.wcdbcd.data.ericsson.com|certificateauthorities.com.ericsson.sec.tls|clientcertificates.com.ericsson.sec.tls|extensionservices.projectcontour.io|externalcertificates.certm.sec.ericsson.com|externalcertificates.com.ericsson.sec.certm|httpproxies.projectcontour.io|internalcertificates.siptls.sec.ericsson.com|internalusercas.siptls.sec.ericsson.com|servercertificates.com.ericsson.sec.tls|tlscertificatedelegations.projectcontour.io");
	if [[ $debug ]]; then echo "Number of CRDS: ${crds_resources}"; fi
	
	if [ ${crds_resources} -lt 8 ]; then
		if [[ $debug ]]; then  echo "CRDs missing" ; fi
		crdsResourceExist="true"
	else
		if [[ $debug ]]; then  echo "CRDs found in cluster" ; fi	
		crdsResourceExist="false"
	fi
}

crdsReleasesExist(){
	export crds_releases=$(helm list  -n $crdsNamespace | grep -cE "eric-sec-sip-tls-crd|eric-sec-certm-crd|eric-tm-ingress-controller-cr-crd|eric-data-wide-column-database-cd-crd");
	if [[ $debug ]]; then echo "Number of CRDS: ${crds_releases}"; fi
	
	if [ ${crds_releases} -lt 3 ]; then
		if [[ $debug ]]; then  echo "CRD releases missing" ; fi
		crdsReleaseExist="true"
	else
		if [[ $debug ]]; then  echo "CRD releases found" ; fi	
		crdsReleaseExist="false"
	fi
}

crdsReleasesCorrupted(){
	export crds_releases=$(helm -n $crdsNamespace list -a | grep -v deployed | grep -cE "eric-sec-sip-tls-crd|eric-sec-certm-crd|eric-tm-ingress-controller-cr-crd|eric-data-wide-column-database-cd-crd");
	if [[ $debug ]]; then echo "Number of CRDS: ${crds_releases}"; fi
	
	if [ ${crds_releases} -gt 0 ]; then
		if [[ $debug ]]; then  echo "CRD releases corrupted" ; fi
		crdsCorrupted="true"
	else
		if [[ $debug ]]; then  echo "CRD releases incorrupted" ; fi	
		crdsCorrupted="false"
	fi
}
crdsReleasesExist
crdsResourcesExist
crdsReleasesCorrupted

if [ "$crdsReleaseExist" == true ] || [ "$crdsResourceExist" == true ] || [ "$crdsCorrupted" == true ]; then
	echo "true"
else 
	echo "false"
fi