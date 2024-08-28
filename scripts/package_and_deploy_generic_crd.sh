#!/bin/bash 

me="`basename "$0"`"
umbrellaPath="esc/helm/eric-sc-umbrella/"
bobPath=".bob/"
spiderChartDir="eric-sc-spider-charts/"

#parse options..
args=( "$@" )
while [ "$#" -gt 0 ]; do
	case "$1" in
		-h|--help)
			echo "This script is used for CRDs"
			echo "========================================================================="
			echo -e "case 1 : extract crds version from a variable named CHART_NAME"
			echo -e "If it is not specified a version,it extracts from requirements.yaml\n"
			echo "========================================================================="
			echo -e "case 2 : not only extract crds version but also deploy it in the cluster\n"
			exit 0;
			;;
		-s=*|--service=*)
			serviceToUpgrade="${1#*=}";			
        	;;
		-s|--service)
			serviceToUpgrade="$2";
			shift;;
		-n=*|--namespace=*)
			namespace="${1#*=}";			
        	;;
		-n|--namespace)
			namespace="$2";
			shift;;
		-v=*|--version=*)
			serviceVersion="${1#*=}";			
        	;;
		-v|--version)
			serviceVersion="$2";
			shift;;
		-cluster=*|--cluster=*)
			cluster="${1#*=}";			
        	;;
		-cluster|--cluster)
			cluster="$2";
			shift;;
		-f=*|--file=*)
			file="${1#*=}";			
        	;;
		-f|--file)
			file="$2";
			shift;;
		--deploy)
			deploy=true;
			;;
		--cncs)
			cncs_crds=true;
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

get_line_number_in_chart(){
	if [[ $debug ]]; then echo "Extracting line from Chart yaml or requirements yaml file with ${serviceToUpgrade}"; fi
	lscharts=$(ls ${bobPath}${spiderChartDir})
	for spiderChart in $lscharts
	do
		lineNumber=$(cat ${bobPath}${spiderChartDir}${spiderChart}/Chart.yaml | grep -n -m 1 ${serviceToUpgrade} | awk -F ":" '{print $1}')
		if [ ! -z "$lineNumber" ]
		then
			break
		fi
	done
}

construct_url_and_download(){
	if [[ $debug ]]; then echo "Extracting line from requirements yaml file with ${serviceToUpgrade}"; fi
	reqLine=$(cat ${umbrellaPath}requirements.yaml | grep -n -m 1 ${serviceToUpgrade} | awk -F ":" '{print $1}')
	reqLine=$(( reqLine + 1 ))
	
	if [[ $debug ]]; then echo "Extracting repository url from line ${reqLint} of requirements yaml file"; fi
	serviceRepoPath=$(sed "${reqLine}q;d" ${umbrellaPath}requirements.yaml | sed 's/.*\(https.*\/\).*/\1/g')
	if [[ $debug ]]; then echo "Service repository URL: ${serviceRepoPath}";fi
	
	url=$(echo ${serviceRepoPath}${serviceToUpgrade}/${serviceToUpgrade}-${serviceVersion}.tgz)
	if [[ $debug ]]; then echo "Reconstructing helm fetch URL: ${url}"; fi
	
	helmRepoPath=$(sed "${reqLine}q;d" ${umbrellaPath}requirements.yaml | sed 's/.*\(https.*artifactory\/\).*/\1/g')
	if [[ $debug ]]; then echo "Fetching helm repo credentials for ${helmRepoPath}"; fi
	
	
	if [[ $debug ]]; then echo "Fetching helm chart from ${url} in ${bobPath}"; fi
	helm fetch ${url}   -d "${bobPath}"
	return $?
}

unpack_and_extract_crd_version(){
	tar zxf ${bobPath}${serviceToUpgrade}-${serviceVersion}.tgz --directory ${bobPath}
	crdVersion=$(ls ${bobPath}${serviceToUpgrade}/eric-crd/ 2> /dev/null | awk -F "-crd-" '{print $2}' | awk -F ".tgz" '{print $1}')
}

cleanup(){
	rm -rf ${bobPath}${serviceToUpgrade}-${serviceVersion}.tgz ${bobPath}${serviceToUpgrade}
}

construct_CRD_url_and_download(){
	url=$(echo ${serviceRepoPath}${serviceToUpgrade}-crd/${serviceToUpgrade}-crd-${crdVersion}.tgz)
	if [[ $debug ]]; then echo "Reconstructing helm fetch URL: ${url}"; fi
	if [[ $debug ]]; then echo "Fetching now CRD helm chart from ${url} in ${bobPath}"; fi
	helm fetch ${url}  -d "${bobPath}"
	return $?
}

package_and_deploy(){
	releaseName=$(echo eric-$(echo ${serviceToUpgrade}-crd |  awk -F "eric-" '{print $2}'))
	if [[ $debug ]]; then echo "Installing now release with name ${releaseName} with version: ${crdVersion}"; fi
	if [ -z "$file" ]; then
		helm upgrade --install --kubeconfig $bobPath$cluster.admin.conf  ${releaseName} ${bobPath}/${serviceToUpgrade}-crd-${crdVersion}.tgz --timeout 500s --namespace ${namespace} --atomic;
    else
		helm upgrade --install --kubeconfig $bobPath$cluster.admin.conf  ${releaseName} ${bobPath}/${serviceToUpgrade}-crd-${crdVersion}.tgz -f $file --timeout 500s --namespace ${namespace} --atomic;
	fi                        
}

extractLatestCommitVersion(){
	if [[ $debug ]]; then echo "Extracting line from requirements yaml file with ${serviceToUpgrade}"; fi
	reqLine=$(cat ${umbrellaPath}requirements.yaml | grep -n -m 1 ${serviceToUpgrade} | awk -F ":" '{print $1}')
	versionLine=$(( reqLine + 2 ))
	serviceVersion=$(sed "${versionLine}q;d" ${umbrellaPath}requirements.yaml |  sed 's/.*version:.//g')
}

copy_crd_to_bob_and_extract_version(){
	if [[ $debug ]]; then echo "Copying ${serviceToUpgrade} crd to ${bobPath}"; fi
	cp ${bobPath}${spiderChartDir}${spiderChart}/charts/${serviceToUpgrade}/eric-crd/* ${bobPath}
	crdVersion=$(ls ${bobPath} | grep ${serviceToUpgrade} 2> /dev/null | awk -F "-crd-" '{print $2}' | awk -F ".tgz" '{print $1}')
}

if [ -z "$cncs_crds" ] && [ ! -d "$umbrellaPath" ]; then
	echo "Path to umbrella chart is not valid"
	exit 1
fi

if [ "$cncs_crds" ] && [ ! -d "${bobPath}${spiderChartDir}" ]; then
	echo "Path to spider chart is not valid"
	exit 1
fi

if [ -z "$serviceToUpgrade" ]; then
	echo "Mandatory parameter CRD to upgrade is not set"
	exit 1
fi

if [ -z "$serviceVersion" ] && [ -z "$cncs_crds" ]; then
	if [[ $debug ]];then echo -e "No serviceVersion specified \nTaking the OLD version from Chart yaml or requirements yaml";fi
	extractLatestCommitVersion
fi

if [ -z "$file" ]; then
	if [[ $debug ]];then echo -e "No values.yaml specified \nWe will use the default values from the ${serviceToUpgrade} CRD chart";fi
fi

if [ "$cncs_crds" ]; then
get_line_number_in_chart
else
construct_url_and_download
fi
if [ $? -eq 1 ]; then
	echo "Error running ${me}, Failed to fetch ${serviceToUpgrade}:${serviceVersion}";
	exit 1
fi

if [ "$cncs_crds" ]; then
	copy_crd_to_bob_and_extract_version
else
	unpack_and_extract_crd_version
fi
if [ -z "${crdVersion}" ]; then
	echo "No crd version specified.Exiting with no further actions!"
	exit 0
else
	if [[ $debug ]]; then echo "Extracted CRD dependent version from ${serviceToUpgrade}:${serviceVersion} ---> ${crdVersion}";fi
	echo ${serviceToUpgrade}-crd-${crdVersion}.tgz
fi
if [ -z "$cncs_crds" ]; then
	construct_CRD_url_and_download
	if [ $? -eq 1 ]; then
		echo "Error running ${me}, Failed to fetch crd chart ${serviceToUpgrade}-crd:${crdVersion}";
		exit 1
	fi
fi

if [[ $deploy ]];then 
	package_and_deploy
	if [ -z "${namespace}" ]; then
		echo "Mandatory parameter CRD namespace  is not set"
		exit 1
	fi
	if [ $? -eq 1 ]; then
		echo "Error running ${me}, Failed to deploy crd chart ${serviceToUpgrade}-crd:${crdVersion}";
		exit 1
	fi
fi
cleanup
