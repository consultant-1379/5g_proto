#!/bin/bash

me="`basename "$0"`"
umbrellaPath="esc/helm/eric-sc-umbrella/"
bobPath=".bob/"
spiderChartDir="eric-sc-spider-dependencies/"

#parse options..
args=( "$@" )
while [ "$#" -gt 0 ]; do
	case "$1" in
		-h|--help)
			echo "This script is used for fetching CRDs from SC application dependencies in spider chart"
			echo "If any support needed contact Challengers team (IXG-ChallengersTeam@ericsson.onmicrosoft.com)."
			exit 0;
			;;
		-s=*|--service=*)
			serviceToUpgrade="${1#*=}";			
			;;
		-s|--service)
			serviceToUpgrade="$2";
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

# Enable debug mode
if [[ $debug ]]; then
	set -x;
fi

# Check if new spider chart path is available
if [ ! -d "${bobPath}${spiderChartDir}" ]; then
	echo "Error running ${me}, Path to spider chart ${bobPath}${spiderChartDir} is not valid"
	exit 1
fi
echo "Spider chart identified with versions:"
cat ${bobPath}${spiderChartDir}dependencies-list.json

# Check input service name to update CRDs
if [ -z "$serviceToUpgrade" ]; then
	echo "Error running ${me}, Mandatory parameter CRD to upgrade is not set"
	exit 1
fi
echo "Updating CRDs for service ${serviceToUpgrade}"

# Check input file values.yaml to be used for helm install/upgrade
if [ -z "$file" ]; then
	echo -e "No values.yaml specified \nWe will use the default values from the ${serviceToUpgrade} CRD chart deployment"
fi

listOfCrdPaths=($(find ${bobPath}${spiderChartDir} -type d -name eric-crd | grep ${serviceToUpgrade}))
if [[ ${#listOfCrdPaths[@]} -gt 1 ]]; then
	listOfCrd=()
	for crd in ${listOfCrdPaths[@]}; do
		listOfCrd+=("${crd}")
	done
	if [[ $(echo "${listOfCrd[@]}" | tr ' ' '\n' | sord -u | wc -l) -ne 1 ]]; then
		echo "Error running ${me}, Multiple CRD versions identified!!"
		exit 1
	fi
fi

echo "Copying ${serviceToUpgrade} crd to ${bobPath}"
cp ${listOfCrdPaths[0]}/* ${bobPath}
crdVersion=$(ls ${bobPath} | grep ${serviceToUpgrade}-crd 2> /dev/null | awk -F "-crd-" '{print $2}' | awk -F ".tgz" '{print $1}')
if [ -z "${crdVersion}" ]; then
	echo "Error running ${me}, No crd version identified. Exiting with no further actions!"
	exit 1
fi

echo "Extracting ${serviceToUpgrade}-crd-${crdVersion}.tgz to ${bobPath}${serviceToUpgrade}-crd-pkg"
echo ${serviceToUpgrade}-crd-${crdVersion}.tgz > ${bobPath}var.${serviceToUpgrade}-crd-pkg