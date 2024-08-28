#!/bin/bash

conflicts=$(git diff --name-only --diff-filter=U)

echo $conflicts
cnt=1
filename=$(echo $conflicts | awk '{print $1}')



while [[ ! -z "$filename" ]]; do
	echo $filename
	if [[ $filename == "devtools/envoy-builder/sc_envoy" ]]; then
		git checkout master $filename
		echo 'checkout master sc_envoy conflict'
		echo $conflicts
	elif [[ $filename == "Jenkins/"*"-adp-staging.groovy" ]]; then
		echo 'Conflicts in adp-staging groovy files'
		git checkout --ours $filename
	elif [[ $filename == "Jenkins/sc-staging"*".jenkinsfile" ]]; then
		echo 'Conflicts in adp-staging groovy files'
		git checkout --ours $filename
	elif [[ $filename == "esc/helm/eric-sc-umbrella/requirements.yaml" ]]; then
		echo "$filename is a special case"
	elif [[ $filename == "esc/helm/eric-sc-umbrella/values.yaml" || $filename == "esc/release_artifacts/eric-sc-values.yaml" ]]; then
		echo "Conflicts in values or eric-sc-values files. Nothing to do about that!"
	elif [[ $filename == "esc/helm/eric-sc-crds/requirements.yaml" ]]; then
		echo "$filename is another special case that we keep our side"
		git checkout --ours $filename
		git add $filename
	else
		echo "keeping master side and adding file: $filename"
#		git show :3:$filename > $filename
		git checkout --theirs $filename
		git add $filename
		echo $conflicts
	fi
	cnt=$(( $cnt+1 ))
	filename=$(echo $conflicts | awk -v var="$cnt" '{print $var}')
done
