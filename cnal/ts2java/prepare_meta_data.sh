#!/bin/bash
#
# Analyze all the TSs for the types they define and store the result as meta data as a base for subsequent processing.
#
# Synopsis: prepare_meta_data.sh <path-to-5g_proto> <src_root> <purpose := [ "generation" | "check" ]>

echo "$0: starting ..."

#set -e # Any subsequent commands which fail will cause the shell script to exit immediately

pushd "${1:-.}/cnal/ts2java"

TS_ROOT=tmp/yaml
#TS_ROOT=5G_APIs
META_DIR=tmp/meta

rm -rf "$META_DIR"

# Step 1:
# Scan all TSs for classes they define themselves or reuse from other TSs.
# For each TS, create a folder named like the TS.
# For each own class, touch a file in that folder.
# For each referenced class, create a Java import line and append it to the touched file.
#
#for ts in $TS_ROOT/TS29510_Nnrf_NFManagement.yaml
#for ts in $TS_ROOT/TS29522_ServiceParameter.yaml
for ts in $TS_ROOT/*.yaml
do
	if [[ -d $ts ]]
	then
		echo "$0: Skipping directory $ts"
		continue
	fi

	echo "$0: Analyzing TS: $ts"

	dos2unix "$ts" 2> /dev/null # Needed to convert \r\n to \n

	parent_ts_name=`echo "$ts" | awk -v ts="${ts#[^/]*/}" 'match($ts, /(TS[0-9]+_[^.]+)[.]yaml/, a) { print(a[1]); }'`
	parent_ts_name=${parent_ts_name,,}      # To lower case
	parent_ts_name=${parent_ts_name//_/\/}  # Replace all '_' by '/'
	path="$META_DIR/$parent_ts_name"
#	echo "$0: $path"
	mkdir -p "$path"

	if [ -z "${3}" -o "${3}" == "generation" ]
	then
		cat "$ts" | awk -v path="$path" -v pkg_root="${2//\//.}" -v also_local_refs=0 -v write_as="imports" -f write_meta_data.awk
	elif [ "${3}" == "check" ]
	then
		cat "$ts" | awk -v path="$path" -v pkg_root="${2//\//.}" -v also_local_refs=1 -v write_as="paths" -f write_meta_data.awk
	fi
done

popd

echo "$0: finished."
