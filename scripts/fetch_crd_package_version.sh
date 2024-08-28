#!/bin/bash
#Script that will fetch only needed crd chart.
#It looks at the dep available inside the umbrella chart and fetches the relevant crd package.

dependencies=.bob/eric-sc-umbrella
destination=.bob

repo_list=$(helm repo list | grep -Ev 'ok')
adp_artifactory="https://arm.sero.gic.ericsson.se/artifactory/proj-adp-gs-all-helm"

[ -z "$1" ] && echo "No input given. Script will now exit." && exit 0

while read -r rline; do
    add_repo=true
	IFS=' ' read -a array <<< $rline
	if [ "${array[0]}" != "NAME" ]
	then
        if [[ "${array[1]}" == "$adp_artifactory" ]]
		then
            #echo "Repository: ${array[1]} is already included."
            add_repo=false
        fi
    fi
done <<< "$repo_list"

if $add_repo; then 
    #echo "$adp_artifactory added in the repository list."
    helm repo add "arm.sero.gic.ericsson.se" "$adp_artifactory" > /dev/null
fi

output=$(helm dep list $dependencies)

#echo $output

while read -r line; do
	[ -z "$line" ] && continue
	if [[ $line == *"WARNING"* ]]
	then
		IFS='"' read -a tmp <<< $line
		IFS='/' read -a chart <<< ${tmp[1]}
		echo "Removing ${chart[-1]} "
		rm -f $destination/charts/${chart[-1]}
		continue
	fi
	IFS=' ' read -a array <<< $(echo $line)
	if [ "${array[0]}" != "NAME" ]
	then
		if [[ "${array[0]}" == "$1" ]]
		then
            url=$(echo "${array[2]}${array[0]}/${array[0]}-${array[1]}.tgz" | tr -d '[:space:]')
            url_crd=$(echo "${array[2]}${array[0]}-crd/${array[0]}-crd" | tr -d '[:space:]')
            pkg=$(echo "${array[0]}-${array[1]}.tgz" | tr -d '[:space:]' )
        fi
	fi
done <<< "$output"

#echo "Fetching $url"
helm fetch $url --destination $destination/

crd_version=$(tar -tvf $destination/$pkg | grep "tgz" | sed 's/.*crd-\([0-9].*[0-9]\).*/\1/g')
crd_pkg="$1-crd-$crd_version.tgz"
#echo "Fetching $crd_pkg ..."
[ ! -z "$crd_version" ] && helm fetch "$url_crd-$crd_version.tgz" --destination $destination/ || echo "$1 CRD version could not be retrieved"

echo $crd_pkg

rm -f $destination/$pkg