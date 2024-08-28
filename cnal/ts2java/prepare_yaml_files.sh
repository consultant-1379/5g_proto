#!/bin/bash
#
# Copy all the TSs to tmp/yaml and replace enums by string everywhere.
#
# Synopsis: prepare_yaml_files.sh <path-to-5g_proto>

echo "$0: starting ..."

#set -e # Any subsequent commands which fail will cause the shell script to exit immediately

pushd "${1:-.}/cnal/ts2java"

TS_ROOT=../src/main/resources/3gpp_r17
#TS_ROOT=5G_APIs
YAML_DIR=tmp/yaml

rm -rf "$YAML_DIR"
mkdir -p "$YAML_DIR"

for ts in $TS_ROOT/*.yaml
#for ts in $TS_ROOT/TS29510_Nnrf_NFManagement.yaml
do
	if [[ -d $ts ]]
	then
		echo "$0: Skipping directory $ts"
		continue
	fi

	echo "$0: Preparing TS: $ts"

	dos2unix "$ts" 2> /dev/null # Needed to convert \r\n to \n
	
	dest_file="${ts/[^\/]*\//$YAML_DIR/}"
	echo "$0: Replacing enums by string in TS: $dest_file"	
	cat "$ts" | gawk -f replace_enums_by_string.awk > "$dest_file"
done

popd

echo "$0: finished."
