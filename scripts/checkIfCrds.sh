#!/bin/bash -x

me="`basename "$0"`"
umbrellaPath="esc/helm/eric-sc-umbrella/"
bobPath=".bob"

#parse options..
args=( "$@" )
while [ "$#" -gt 0 ]; do
	case "$1" in
		-h|--help)
			echo "Script used for the extraction of crds"
			exit 0;
			;;
		-s=*|--service=*)
			name="${1#*=}";
			;;
		-s|--service)
			name="$2";
			shift;;
		-v=*|--version=*)
			version="${1#*=}";
			;;
		-v|--version)
			version="$2";
			shift;;
		-rep=*|--repo=*)
			repo="${1#*=}";
			;;
		-rep|--repo)
			repo="$2";
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

check_if_new_crd_version() {
	# Get directory's name
	crdDirName=$(ls ${bobPath}/${name} | grep crd)
	crdName=$(ls ${bobPath}/${name}/${crdDirName})
	newCrdVersion=$(echo ${crdName} | sed -r 's/'${name}'-crd-//g' | sed -r 's/.tgz//g')
	runningCrdVersion=$(helm list -n eric-crds | grep ${name} | awk '{print $9}' | sed -r 's/'${name}'-crd-//g')
	[ "${runningCrdVersion}" != "${newCrdVersion}" ]; return $?
}

construct_url_and_download() {
	url=$(echo ${repo}/${name}/${name}-${version}.tgz)
	if [[ $debug ]]; then echo "Reconstructing helm fetch URL: ${url}"; fi
	
	if [[ $debug ]]; then echo "Fetching helm repo credentials for ${repo}"; fi
	username=$(python scripts/helm_repositories.py -u --repository ${repo})
	password=$(python scripts/helm_repositories.py -p --repository ${repo})

	if [[ $debug ]]; then echo "Fetching helm chart from ${url} in ${bobPath}"; fi
	helm fetch ${url} --username $username --password $password  -d "${bobPath}/"
	return $?
}

cleanup() {
	rm -rf ${bobPath}/${name}-${version}.tgz ${bobPath}/${name}
}

check_if_crds() {
	tar zxf ${bobPath}/${name}-${version}.tgz --directory ${bobPath}/;
	ls ${bobPath}/${name}| grep crd > /dev/null;
	return $?
}

if [ -z "$version" ]; then
	echo "Error running ${me}, Mandatory parameter service version not set"
	exit 1
fi

construct_url_and_download
if [ $? -eq 1 ]; then
	echo "Error running ${me}, Failed to fetch ${name}:${version}";
	exit 1
fi

check_if_crds && check_if_new_crd_version
if [ $? -eq 0 ]; then
	if [[ $debug ]]; then echo "Service ${name} in ${version} detected with needed crd!"; fi
	echo "true"
else
	if [[ $debug ]]; then echo "Service ${name} in ${version} NOT detected with needed crd!"; fi
	echo "false"
fi
cleanup