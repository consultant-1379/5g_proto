#!/bin/bash

set -e
ARTIFACTORY_TOKEN="$1"
SOURCE_DIR="$2"
VERSION="$3"
echo "Working directory is $SOURCE_DIR"
echo "Version: $VERSION"

MAVEN_REPO="https://arm.seli.gic.ericsson.se/artifactory/proj-5g-sc-maven-dev-local"

upload()
{
  printf "Uploading artifacts\n$1\n$2\n$3\nto\n$4\n"
  #Even though the printout suggests otherwise, as it only displays one result, all 3 files are uploaded
  curl -s -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file "{$1,$2,$3}" "$4"
  echo "Done"
}

CORE_SOURCEDIR=$SOURCE_DIR/io/vertx/vertx-core/$VERSION
CORE_UPLOADDIR=$MAVEN_REPO/io/vertx/vertx-core/$VERSION/

CORE_JAR=$CORE_SOURCEDIR/vertx-core-$VERSION.jar
CORE_SOURCES=$CORE_SOURCEDIR/vertx-core-$VERSION-sources.jar
CORE_POM=$CORE_SOURCEDIR/vertx-core-$VERSION.pom

upload $CORE_JAR $CORE_JAR.md5 $CORE_JAR.sha1 $CORE_UPLOADDIR
upload $CORE_SOURCES $CORE_SOURCES.md5 $CORE_SOURCES.sha1 $CORE_UPLOADDIR
upload $CORE_POM $CORE_POM.md5 $CORE_POM.sha1 $CORE_UPLOADDIR

echo -e "\b\bSuccess"
