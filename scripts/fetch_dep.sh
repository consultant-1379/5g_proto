#!/bin/bash
#Script that will fetch only needed dependencies, not those, that are already existing.
#It looks at the dep available inside the umbrella folder and checks with the requirements.yaml if anything is missing/ wrong version.

destination=.bob/$1
output=$(helm dep list $destination | grep -Ev '\W(ok)\W')

# mapping 
# adp_servive -> repository 


# init hashmap

input_file="$destination/requirements.yaml"
declare -A dependencies

#echo "number of line is $(wc -l ${input_file} | awk '{ print $1 }')"
while IFS= read -r line
do
    if echo $line | grep -oq "name:" 
	then
	   name_of_the_service=$(echo $line | awk -F  "name:" '{print $2}')
	   #echo "Name of service --- > '${name_of_the_service}'"
    fi
	if echo $line | grep -oq "repository:" 
	then
		repository=$(echo $line | awk -F  "repository:" '{print $2}' )
		#echo "Repository of the service --- > ${repository}"

        #echo "Saving the repository of the service : '${name_of_the_service}'"
		dependencies["$(echo ${name_of_the_service//[[:blank:]]/})"]=$repository
	fi
done < "${input_file}"

#echo "number of elements: ${#dependencies[*]}"
#for k in "${!dependencies[@]}"
#do
#  printf "%s\n" "${k} -> ${dependencies[$k]}"
#done

#echo "${dependencies['eric-cm-mediator']}"
repeat() {
	char=$1
	end=$2
	for ((i=1; i<=${end}; i++)); do echo -n "${char}"; done
}

echo "Fetching HELM charts initiated."
while read -r line; do
	[ -z "$line" ] && continue
	
	# the following line is used only for debugging purposes
	# echo "line: $line" 
	if [[ $line == *"WARNING"* ]]
	then
		IFS='"' read -a tmp <<< $line
		IFS='/' read -a chart <<< ${tmp[1]}
		#echo "Removing ${chart[-1]} "
		rm -f $destination/charts/${chart[-1]}
		continue
	fi
	
	IFS='	' read -a array <<< $line
	if [ ${array[0]} != "NAME" ]
	then
		version=$(echo "${array[1]}")
		name=$(echo "${array[0]}")
		name_no_space=$(echo "${name//[[:blank:]]/}")
		repository=$(echo ${dependencies[${name_no_space}]})
		#echo "The name of the service is ==>  '${name}'"
		#echo "the name no space is ==> '${name_no_space}'"
		#echo "The repository of the service is ==> '${repository}'"
		#echo "The version of the service is ==>  '${version}'"

		#echo "***** Constructing url *****"
		if [[ ${repository} == *"proj-5g-bsf-helm"* ]];then 
			#echo "This is a SC service!"
			url=$(echo "${repository}${name_no_space}-${version}.tgz" | tr -d '[:space:]')
        else 
			#echo "This is not a SC service!"
			url=$(echo "${repository}${name_no_space}/${name_no_space}-${version}.tgz" | tr -d '[:space:]')

		fi
		echo "url: $url"

		rm -f $destination/charts/$(echo "${array[0]}" | tr -d '[:space:]')-[0-9]*
		IFS='/' read -a repository <<< ${array[2]} #get address of repository

		#echo "repository to access credentials ==> ${repository[2]}"
		username=$(python3.10 scripts/helm_repositories.py -u --repository ${repository[2]})
		password=$(python3.10 scripts/helm_repositories.py -p --repository ${repository[2]})

		nameLength=${#name_no_space}
		charsLength=$((35 - nameLength - 1))
		chars=$(repeat "-" "${charsLength}")
		echo "${name_no_space} ${chars} ${url}"
		helm fetch $url --destination $destination/charts/ --username $username --password $password
	fi
done <<< "$output"
echo "Fetching HELM charts complete."
