#!/bin/bash
#Script that will fetch only needed dependencies, not those, that are already existing.
#It looks at the dep available inside the crds folder and checks with the requirements.yaml if anything is missing/ wrong version.

destination=.bob/eric-sc-crds
output=$(helm dep list $destination | grep -Ev 'ok')

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
	IFS='	' read -a array <<< $line
	if [ ${array[0]} != "NAME" ]
	then
		if [[ ${array[2]} == *"proj-5g-bsf-helm"* ]]
		then
			url=$(echo "${array[2]}${array[0]}-${array[1]}.tgz" | tr -d '[:space:]')
		else
			url=$(echo "${array[2]}${array[0]}/${array[0]}-${array[1]}.tgz" | tr -d '[:space:]')
		fi
		echo "helm fetch $url"
		rm -f $destination/charts/$(echo "${array[0]}" | tr -d '[:space:]')-[0-9]*
		IFS='/' read -a repository <<< ${array[2]} #get address of repository
		username=$(python scripts/helm_repositories.py -u --repository ${repository[2]})
		password=$(python scripts/helm_repositories.py -p --repository ${repository[2]})
		helm fetch $url --destination $destination/charts/ --username $username --password $password
	fi
done <<< "$output"
