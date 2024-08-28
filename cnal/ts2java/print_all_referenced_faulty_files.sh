#!/bin/bash
#
# Recursively print all Java files directly and indirectly referenced by the src_file passed.
#
# Synopsis: print_all_referenced_faulty_files.sh <path-to-5g_proto> <src-file> <skip-preparation> <verbose>
#
# Example when in folder ts2java: ./print_all_referenced_faulty_files.sh ../../ tmp/meta/ts29510/nnrf/nfdiscovery/NFProfile.java false true

echo "$0: starting ..."

#set -e # Any subsequent commands which fail will cause the shell script to exit immediately

pushd "${1:-.}/cnal/ts2java"

ROOT=tmp
META_DIR=$ROOT/meta
SRC_ROOT=$ROOT/src/main/java/com/ericsson/cnal/openapi/r17

path_to_5g_proto=${1}
src_file=${2}
skip_preparation=${3}
verbose=${4}

declare -A faulty_files

print()
{
	if [ "${verbose}" == "true" ]
	then
		printf "${1}\n"
	fi
}

collect_all_faulty_files()
{
	for faulty_file in `grep -l -r "Objects.hash()" tmp/tmp`
	do
		ff=${faulty_file/*\/r[0-9][0-9]/${META_DIR}}
		
		faulty_files[${ff}]=${faulty_file}
	done

	for faulty_file in "${!faulty_files[@]}"
	do
		print "$0: faulty file: ${faulty_file} --> ${faulty_files[${faulty_file}]}"
	done
}

print_all_referenced_faulty_files()
{
	print "$0: current source file: ${1}"
 
 	if [ ${faulty_files[${1}]} ]
	then
		printf "$0: found referenced faulty file: ${2}\n"
	fi
 	
	for referenced_file in `cat "${1}" | xargs`
	do
		print_all_referenced_faulty_files "${referenced_file}" "${referenced_file}\n$0: from: ${3}${2}" "${3} "
	done
}

if [ -z "${skip_preparation}" -o "${skip_preparation}" == "false" ]
then
	# Extract meta data from TS files.
	#
	prepare_meta_data.sh "${path_to_5g_proto}" "${SRC_ROOT##$ROOT/src/main/java/}" "check"
fi

# Collect all faulty files needed for the subsequent check for referenced faulty files. 
#
collect_all_faulty_files

# Print all faulty files referenced directly and indirectly by the source file passed.
#
print_all_referenced_faulty_files ${src_file} ${src_file}

popd

echo "$0: finished."

