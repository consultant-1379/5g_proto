#!/bin/bash

me="`basename "$0"`"
umbrellaPath="esc/helm/eric-sc-umbrella/"
bobPath=".bob/"
crds_cervices=("eric-sec-certm" "eric-sec-sip-tls" "eric-tm-ingress-controller-cr")

#parse options..
args=( "$@" )
while [ "$#" -gt 0 ]; do
	case "$1" in
		-h|--help)
			echo "Script used for the extraction of crds"
			exit 0;
			;;
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

construct_url_and_download(){
	if [[ $debug ]]; then echo "Extracting line from requirements yaml file with ${serviceToUpgrade}"; fi
	reqLine=$(cat ${umbrellaPath}requirements.yaml | grep -n ${serviceToUpgrade} | awk -F ":" '{print $1}')
	reqLine=$(( reqLine + 1 ))
	
	if [[ $debug ]]; then echo "Extracting repository url from line ${reqLint} of requirements yaml file"; fi
	serviceRepoPath=$(sed "${reqLine}q;d" ${umbrellaPath}requirements.yaml | sed 's/.*\(https.*\/\).*/\1/g')
	echo "Service repository URL: ${serviceRepoPath}"
	
	url=$(echo ${serviceRepoPath}${serviceToUpgrade}/${serviceToUpgrade}-${serviceVersion}.tgz)
	if [[ $debug ]]; then echo "Reconstructing helm fetch URL: ${url}"; fi
	
	helmRepoPath=$(sed "${reqLine}q;d" ${umbrellaPath}requirements.yaml | sed 's/.*\(https.*artifactory\/\).*/\1/g')
	if [[ $debug ]]; then echo "Fetching helm repo credentials for ${helmRepoPath}"; fi
	username=$(python scripts/helm_repositories.py -u --repository ${helmRepoPath})
	password=$(python scripts/helm_repositories.py -p --repository ${helmRepoPath})

	if [[ $debug ]]; then echo "Fetching helm chart from ${url} in ${bobPath}"; fi
	helm fetch ${url} --username $username --password $password  -d "${bobPath}"
	return $?
}

unpack_and_extract_crd_version(){
	tar zxf ${bobPath}${serviceToUpgrade}-${serviceVersion}.tgz --directory ${bobPath}
	crdVersion=$(ls ${bobPath}${serviceToUpgrade}/eric-crd/ | awk -F "-crd-" '{print $2}' | awk -F ".tgz" '{print $1}')
}

cleanup(){
	rm -rf ${bobPath}${serviceToUpgrade}-${serviceVersion}.tgz ${bobPath}${serviceToUpgrade}
}

check_crd_deps(){  
	for t in ${crds_cervices[@]}; do
		if [ "${serviceToUpgrade}" == "$t" ]; then
			return 0
		fi
	done
	return 1
}

if ! [ -z "$serviceToUpgrade" ]; then
	check_crd_deps
	if [ $? -eq 1 ]; then
		echo "Service ${serviceToUpgrade} has no CRD dependencies"
		exit 0
	fi
else
	echo "Mandatory parameter CRD to upgrade is not set"
	exit 1
fi

if [ -z "$serviceVersion" ]; then
	echo "Error running ${me}, Mandatory parameter service version not set"
	exit 1
fi

construct_url_and_download
if [ $? -eq 1 ]; then
	echo "Error running ${me}, Failed to fetch ${serviceToUpgrade}:${serviceVersion}";
	exit 1
fi

unpack_and_extract_crd_version
echo "Extracted CRD dependent version from ${serviceToUpgrade}:${serviceVersion} ---> ${crdVersion}"
echo "${crdVersion}" > ${bobPath}var.crd-version
cleanup
