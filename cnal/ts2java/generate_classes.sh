#!/bin/bash
#
# Build all classes that are defined in the TSs.
#
# Synopsis: generate_classes.sh <path-to-5g_proto>

echo "$0: starting ..."

set -e # Any subsequent commands which fail will cause the shell script to exit immediately

pushd "${1:-.}/cnal/ts2java"

#set +e # Reset to default again: for the time being, allow compilation errors.

#COMPILER="`pwd`/openapi-generator-cli-5.4.0.jar"
COMPILER="`pwd`/openapi-generator-cli-6.2.1.jar"
TMP_DIR="`pwd`/tmp"
TS_DIR="$TMP_DIR/yaml"
DEST_DIR="$TMP_DIR/src/main/java/com/ericsson/cnal/openapi/r17"

rm -rf "$TMP_DIR"

prepare_yaml_files.sh "$1"

#for ts in 5G_APIs/*.yaml
#for ts in 5G_APIs/TS29673_Nucmf_UERCM.yaml 5G_APIs/TS29675_Nucmf_Provisioning.yaml 5G_APIs/TS32291_Nchf_ConvergedCharging.yaml 5G_APIs/TS32291_Nchf_OfflineOnlyCharging.yaml
#for ts in "$TS_DIR"/TS29571_CommonData.yaml
#for ts in "$TS_DIR"/TS29575_Nadrf_DataManagement.yaml
#for ts in "$TS_DIR"/TS29510_Nnrf_NFDiscovery.yaml
for ts in "$TS_DIR"/TS*
do
    echo "$0: $ts"                                   # $TS_DIR/TS29503_Nudm_PP.yaml
    package=${ts##*/}                                # TS29503_Nudm_PP.yaml
    package=${package%.yaml}                         # TS29503_Nudm_PP
#    package=${package/TS[0-9][0-9][0-9][0-9][0-9]_/} # Nudm_PP
    package=${DEST_DIR##*java/}"/"$package
    package=${package//\//.}                         # Replace all '/' by '.'
    package=${package//_/.}                          # Replace all '_' by '.'
    package=${package,,}                             # Convert to lower case
    echo "$0: $package"

    java -jar "$COMPILER" generate -g java --library vertx --model-package "$package" -i "$ts" -o "$TMP_DIR" --skip-validate-spec
done

popd

echo "$0: finished."
