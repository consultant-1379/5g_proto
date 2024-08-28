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
  printf "Creating checksums and uploading artifacts\n$1\n$2\n$3\nto\n$4\n"
  create_checksums $1
  #Even though the printout suggests otherwise, as it only displays one result, all 3 files are uploaded
  curl -s -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file "{$1,$2,$3}" "$4"
  echo "Done"
}


#Create MD5 and SHA1 Checksums
#Since version 3, creating chechsums has been moved from the maven-install-plugin to the deploy plugin,
#hence -DcreateChecksum=true does not work anymore.
create_checksums()

{
  md5sum $1 | cut -d' ' -f1 > $1.md5
  sha1sum $1 | cut -d' ' -f1 > $1.sha1
}

CLASSES_SOURCEDIR=$SOURCE_DIR/io/netty/netty-tcnative-classes/$VERSION
CLASSES_UPLOADDIR=$MAVEN_REPO/io/netty/netty-tcnative-classes/$VERSION/
LIB_SOURCEDIR=$SOURCE_DIR/io/netty/netty-tcnative-boringssl-static/$VERSION
LIB_UPLOADDIR=$MAVEN_REPO/io/netty/netty-tcnative-boringssl-static/$VERSION/
PARENT_POM_SOURCEDIR=$SOURCE_DIR/io/netty/netty-tcnative-parent/$VERSION
PARENT_POM_UPLOADDIR=$MAVEN_REPO/io/netty/netty-tcnative-parent/$VERSION/

CLASSES_JAR=$CLASSES_SOURCEDIR/netty-tcnative-classes-$VERSION.jar
CLASSES_JAVADOC=$CLASSES_SOURCEDIR/netty-tcnative-classes-$VERSION-javadoc.jar
CLASSES_POM=$CLASSES_SOURCEDIR/netty-tcnative-classes-$VERSION.pom

LIB_JAR=$LIB_SOURCEDIR/netty-tcnative-boringssl-static-$VERSION.jar
LIB_LINUX_JAR=$LIB_SOURCEDIR/netty-tcnative-boringssl-static-$VERSION-linux-x86_64.jar
LIB_POM=$LIB_SOURCEDIR/netty-tcnative-boringssl-static-$VERSION.pom

PARENT_POM=$PARENT_POM_SOURCEDIR/netty-tcnative-parent-$VERSION.pom

# netty-tcnative-classes
upload $CLASSES_JAR $CLASSES_JAR.md5 $CLASSES_JAR.sha1 $CLASSES_UPLOADDIR
upload $CLASSES_JAVADOC $CLASSES_JAVADOC.md5 $CLASSES_JAVADOC.sha1 $CLASSES_UPLOADDIR
upload $CLASSES_POM $CLASSES_POM.md5 $CLASSES_POM.sha1 $CLASSES_UPLOADDIR

# netty-tcnative-boringssl-static
upload $LIB_JAR $LIB_JAR.md5 $LIB_JAR.sha1 $LIB_UPLOADDIR
upload $LIB_LINUX_JAR $LIB_LINUX_JAR.md5 $LIB_LINUX_JAR.sha1 $LIB_UPLOADDIR
upload $LIB_POM $LIB_POM.md5 $LIB_POM.sha1 $LIB_UPLOADDIR

# netty-tcnative-parent
upload $PARENT_POM $PARENT_POM.md5 $PARENT_POM.sha1 $PARENT_POM_UPLOADDIR

echo -e "\b\bSuccess"
