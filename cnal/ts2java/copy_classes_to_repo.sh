#!/bin/bash
#
# Copy the generated and normalized Java files to the right place in the repository.
#
# Synopsis: copy_classes_to_repo.sh <path-to-5g_proto>

echo "$0: starting ..."

set -e # Any subsequent commands which fail will cause the shell script to exit immediately

pushd "${1:-.}/cnal/ts2java"

REL_PATH=src/main/java/com/ericsson/cnal/openapi/r17
SRC_PATH=tmp/tmp/$REL_PATH
DEST_PATH=../$REL_PATH

# Remove old files from repository.
#
echo "$0: Removing old files from repository."
rm -rf "$DEST_PATH"/*

# Copy generated and normalized Java file to repository."
#
echo "$0: Copying generated and normalized Java file to repository."
cp -r "$SRC_PATH"/* "$DEST_PATH"

popd

echo "$0: finished."
