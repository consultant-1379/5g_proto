#!/bin/bash

me="`basename "$0"`"
output_dir=".bob/"
internal_values_dir="csar/internal/"
internal_values_output_dir="internal/"
chart_dependencies_file_path=".bob/eric-sc-spider_tmp/eric-sc-spider/Chart.yaml"

#parse options..
args=( "$@" )
while [ "$#" -gt 0 ]; do
  case "$1" in
    -h|--help)
      echo "This script is used for the internal values yaml files that are related to a specific chart"
      echo "It copies the files into an output directory and renames them to [chart_name]-[chart-version].yaml"
      exit 0;
      ;;
    --debug)
      debug=true;
      ;;
  esac
  ((argscount++));
  shift
done

# Check if dir exist
if [[ ! -d ${internal_values_dir} ]]; then echo "Path to internal values yaml files is not valid"; exit 1; fi
 
# Clean up previous internal output directory
if [[  -d "${output_dir}${internal_values_output_dir}" ]];
then
  if [[ $debug ]]; then echo "Deleting old ${output_dir}${internal_values_output_dir}"; fi
  rm -rf ${output_dir}${internal_values_output_dir};
fi;
if [[ $debug ]]; then echo "Creating new ${output_dir}${internal_values_output_dir}"; fi
mkdir "${output_dir}${internal_values_output_dir}"

for i in ${internal_values_dir}*.yaml
do
  if [[ -f ${i} ]]
  then
    # Get chart version
    chart_name=$(echo $(basename $i) | sed 's/-values.yaml//') 
    name_line=$(cat ${chart_dependencies_file_path} | grep -n -m 1 ${chart_name} | awk -F ":" '{print $1}')
    version_line=$(( name_line + 2 ))
    chart_version=$(sed "${version_line}q;d" ${chart_dependencies_file_path} |  sed 's/.*version:.//g')

    # Copy and rename values yaml file with their chart's version
    if [[ $debug ]]; then echo "Copying and renaming file ${i} to ${output_dir}${internal_values_output_dir}${chart_name}-${chart_version}.yaml"; fi
    cp ${i} "${output_dir}${internal_values_output_dir}${chart_name}-${chart_version}.yaml"
  fi
done
