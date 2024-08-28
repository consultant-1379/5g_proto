# Author: eedbjhe
# This script either clones the envoy repository from github or uses the local sc_envoy repository.
# It checks out the version tag that has been specified as parameter when the builder was started,
# compiles envoy, and moves the binary to the .bob folder in your working directory
# Debug symbols and the source code are collected in tar files and added to the debug image

#!/bin/bash

USER_HOME_DIR=$(pwd)
PATCH_DIR=$USER_HOME_DIR/devtools/envoy-builder/envoy_patches
ENVOY_SRC_DIR=$USER_HOME_DIR/devtools/envoy-builder/sc_envoy
BAZEL_OUTPUT_BASE=$USER_HOME_DIR/$ENVOY_BAZEL_OUTPUT_DIR/v$ENVOY_GIT_TAG
BAZEL_REPOSITORY_CACHE=$USER_HOME_DIR/$ENVOY_BAZEL_OUTPUT_DIR/cache

export LDFLAGS=-fuse-ld=bfd

#Default flags for each build
#TODO: --sandbox-debug disabled for now due to log spamming on Ubuntu VDI
BAZEL_FLAGS=" --copt=-Wno-error=uninitialized --define exported_symbols=enabled --verbose_failures"
BAZEL_FLAGS_REMOTE="--remote_cache=grpc://127.0.0.1:8981 --remote_executor=grpc://127.0.0.1:8981"

#This flag seems to be removed in the latest bazel version used by 1.23.x
if [[ $ENVOY_GIT_TAG =~ "1.10." ]] || [[ $ENVOY_GIT_TAG =~ "1.11." ]] || [[ $ENVOY_GIT_TAG =~ "1.12." ]] || \
   [[ $ENVOY_GIT_TAG =~ "1.13." ]] || [[ $ENVOY_GIT_TAG =~ "1.14." ]] || [[ $ENVOY_GIT_TAG =~ "1.15." ]] || \
   [[ $ENVOY_GIT_TAG =~ "1.16." ]] || [[ $ENVOY_GIT_TAG =~ "1.17." ]] || [[ $ENVOY_GIT_TAG =~ "1.18." ]] || \
   [[ $ENVOY_GIT_TAG =~ "1.19." ]] || [[ $ENVOY_GIT_TAG =~ "1.20." ]] || [[ $ENVOY_GIT_TAG =~ "1.21." ]] || \
   [[ $ENVOY_GIT_TAG =~ "1.22." ]]; then
  BAZEL_FLAGS="$BAZEL_FLAGS --show_task_finish"
else
  BAZEL_FLAGS="$BAZEL_FLAGS"
fi

if [ $ENVOY_BAZEL_BUILD = "remote" ]; then
  BAZEL_FLAGS="$BAZEL_FLAGS $BAZEL_FLAGS_REMOTE"
  echo "Opening tunnel"
  ssh -o "UserKnownHostsFile=/dev/null" -o "StrictHostKeyChecking=no"  -L 8981:127.0.0.1:8980 -Nf seroius05062.sero.gic.ericsson.se
fi

echo ""
echo -n "Building on "
if [ $ENVOY_BAZEL_BUILD = "remote" ]; then
  echo "remote build server"
else 
  echo "local server"  
fi

#The sizeopt flag is supported from version 1.11.0 onwards
if [ $ENVOY_GIT_TAG = "1.10.0" ]; then
   echo "Removing size optimization flag for Envoy 1.10.0"
   SIZEOPT_FLAG=""
fi

#Check if BoringSSL should be build with FIPS
if [ $ENVOY_BORING_SSL_FIPS = "1" ]; then
   echo "Building BoringSSL with FIPS"
   BAZEL_FLAGS="$BAZEL_FLAGS --define boringssl=fips"
else
  #Explicitly set here due to the inclusion of the variable in the version info file
  ENVOY_BORING_SSL_FIPS=0
  echo "Building BoringSSL without FIPS"
fi

if [ $ENVOY_SSL_KEY_LOGGING = "1" ]; then
   echo "Building with SSL Key Logging"
   BAZEL_FLAGS="$BAZEL_FLAGS --define with_ssl_key_logging=yes"
else
  #Explicitly set here due to the inclusion of the variable in the version info file
  ENVOY_SSL_KEY_LOGGING=0
  echo "Building without SSL Key Logging"   
fi

if [ $ENVOY_LOCAL_BUILD = "1" ]; then
   ENVOY_TYPE="sc_envoy"
   #Suppress warning for latest git version
   git config --global --add safe.directory $ENVOY_SRC_DIR
else
   ENVOY_TYPE="envoy"
fi

echo "Removing existing envoy binaries from $OUTPUT_DIR"
rm -rf $USER_HOME_DIR/$OUTPUT_DIR/envoy-static.stripped
rm -rf $USER_HOME_DIR/$OUTPUT_DIR/envoy-static
rm -rf $USER_HOME_DIR/$OUTPUT_DIR/envoy-static.stripped.version
rm -rf $USER_HOME_DIR/$OUTPUT_DIR/envoy-static.version

echo "Removing existing envoy binaries from $ENVOY_WORKER_DIR"
rm -rf $USER_HOME_DIR/$ENVOY_WORKER_DIR:/envoy-static.stripped
rm -rf $USER_HOME_DIR/$ENVOY_WORKER_DIR:/envoy-static
rm -rf $USER_HOME_DIR/$ENVOY_WORKER_DIR:/envoy-static.stripped.version
rm -rf $USER_HOME_DIR/$ENVOY_WORKER_DIR:/envoy-static.version

echo "Removing previously compiled envoy from bazel-bin"
rm -f $ENVOY_SRC_DIR/bazel-bin/source/exe/envoy-static.stripped
rm -f $ENVOY_SRC_DIR/bazel-bin/source/exe/envoy-static

echo "Removing debug symbols and source code archives"
rm -rf $USER_HOME_DIR/$OUTPUT_DIR/$ENVOY_DEBUG_SYMBOLS_ARCHIVE
rm -rf $USER_HOME_DIR/$OUTPUT_DIR/$ENVOY_SOURCE_ARCHIVE

echo "Binaries output dir: $OUTPUT_DIR"
echo "User home dir: $USER_HOME_DIR"
echo "I am running in $(pwd)"
echo "Build type: $ENVOY_BUILD_TYPE"
echo "Run tests: $ENVOY_RUN_TESTS"
echo "Jobs: $ENVOY_BAZEL_JOBS"

#Setup the proper Bazel version for this build
#From 1.12.0 onwards, the Envoy source contains information on which Bazel Version to use,
#Bazelisk will download and install the correct Bazel version.
#As versions prior to 1.12 do not have a .bazelversion file,
#Bazelisk will read the version via the environment variable USE_BAZEL_VERSION
echo "Setting up Bazel for Envoy $ENVOY_GIT_TAG"
if [ $ENVOY_GIT_TAG = "1.10.0" ]; then
    export USE_BAZEL_VERSION=0.24.1
elif [ $ENVOY_GIT_TAG = "1.11.0" ] || [ $ENVOY_GIT_TAG = "1.11.1" ] || [ $ENVOY_GIT_TAG = "1.11.2" ]; then
    export USE_BAZEL_VERSION=0.28.1
fi

#Clones envoy repository from github and builds the vanilla envoy
if [ $ENVOY_LOCAL_BUILD = "0" ]; then
   BAZEL_OUTPUT_BASE=$BAZEL_OUTPUT_BASE-official-$ENVOY_BUILD_TYPE
   if [ $ENVOY_BORING_SSL_FIPS = "1" ]; then
       BAZEL_OUTPUT_BASE=$BAZEL_OUTPUT_BASE-FIPS
   fi
   cd /build
   git clone https://github.com/envoyproxy/envoy.git
   cd envoy
   git checkout tags/v$ENVOY_GIT_TAG
else
   BAZEL_OUTPUT_BASE=$BAZEL_OUTPUT_BASE-local-$ENVOY_BUILD_TYPE
   if [ $ENVOY_BORING_SSL_FIPS = 1 ]; then
       BAZEL_OUTPUT_BASE=$BAZEL_OUTPUT_BASE-FIPS
   fi
   if [ ! -d "envoy" ]; then
       ln -s $ENVOY_SRC_DIR envoy
   fi
   cd envoy
fi

echo "Bazel Output Directory: $BAZEL_OUTPUT_BASE"

if [ $ENVOY_LOCAL_BUILD = "0" ]; then
    for patch in $(ls $PATCH_DIR/v$ENVOY_GIT_TAG)
        do
            echo "Applying envoy patch: v$ENVOY_GIT_TAG/$patch"
            git apply $PATCH_DIR/v$ENVOY_GIT_TAG/$patch
        done
fi

git status

#LLVM  will be used for >= 1.16.0 builds
if [[ $ENVOY_GIT_TAG =~ "1.10." ]] || [[ $ENVOY_GIT_TAG =~ "1.11." ]] || [[ $ENVOY_GIT_TAG =~ "1.12." ]] || \
   [[ $ENVOY_GIT_TAG =~ "1.13." ]] || [[ $ENVOY_GIT_TAG =~ "1.14." ]] || [[ $ENVOY_GIT_TAG =~ "1.15." ]]; then
    echo "No CLANG build."
    BAZEL_FLAGS="$BAZEL_FLAGS --copt=-Wno-error=maybe-uninitialized"
else
    echo "Setting up LLVM for CLANG build."
    BAZEL_FLAGS="$BAZEL_FLAGS --config=clang"
    if [[ $ENVOY_GIT_TAG =~ "1.16." ]] || [[ $ENVOY_GIT_TAG =~ "1.17." ]] || [[ $ENVOY_GIT_TAG =~ "1.18." ]] || [[ $ENVOY_GIT_TAG =~ "1.19." ]]; then
        LLVM_VERSION=11.0.1
        tar -C /llvm/$LLVM_VERSION/ -xJf /clang+llvm-$LLVM_VERSION-x86_64-linux-sles12.4.tar.xz --strip-components=1
        rm -rf clang+llvm-$LLVM_VERSION-x86_64-linux-sles12.4.tar.xz
    elif [[ $LLVM_VERSION == "12.0.0" ]]; then
        echo "Using precompiled llvm from llvm-project"
    else
        echo "Unpacking tar of LLVM $LLVM_VERSION"
        mkdir -p /llvm/$LLVM_VERSION
        tar -C /llvm/$LLVM_VERSION -xJf /llvm-packages/llvm-$LLVM_VERSION.tar.xz
    fi
    bazel/setup_clang.sh /llvm/$LLVM_VERSION
    echo "Using LLVM $LLVM_VERSION"
fi

echo "Setting up Python Version."
if [[ $ENVOY_GIT_TAG =~ "1.16." ]] || [[ $ENVOY_GIT_TAG =~ "1.17." ]] || [[ $ENVOY_GIT_TAG =~ "1.18." ]] || [[ $ENVOY_GIT_TAG =~ "1.19." ]]; then
    echo "Setting Python 2.7 as default"
    exec sudo -u root update-alternatives --set python /usr/local/bin/python2.7
else
    echo "Setting Python 3.11 as default"
    update-alternatives --set python /usr/bin/python3.11
    update-alternatives --set python3 /usr/bin/python3.11
fi
echo "Using Python Version: $(python --version)"
echo "Commit ID: " $(git rev-parse HEAD)
echo
echo "Starting build"
echo
echo "Choo choo!"
echo
echo "    ooOOOO"
echo "   oo      _____"
echo "  _I__n_n__||_||  _______"
echo ">(_________|_7_|-|_envoy_|"
echo " /o ()() ()() o   oo  oo"
echo

if [ $ENVOY_SKIP_BUILD != "1" ]; then

  #Clock the build time
  SECONDS=0

  #Certain "Warnings as errors" disabled to support building of versions > 12. TODO: Restrict disabling those to the files affected, not the whole build
  if [ $ENVOY_BUILD_TYPE = "RELEASE" ]; then
      bazel --output_user_root=$BAZEL_OUTPUT_BASE --bazelrc=/dev/null build --jobs=$ENVOY_BAZEL_JOBS -c opt //source/exe:envoy-static.stripped $BAZEL_FLAGS 
  elif [ $ENVOY_BUILD_TYPE = "FAST" ]; then
      bazel --output_user_root=$BAZEL_OUTPUT_BASE --bazelrc=/dev/null build --jobs=$ENVOY_BAZEL_JOBS //source/exe:envoy-static $BAZEL_FLAGS
  elif [ $ENVOY_BUILD_TYPE = "DEBUG" ]; then
      bazel --output_user_root=$BAZEL_OUTPUT_BASE --bazelrc=/dev/null build --jobs=$ENVOY_BAZEL_JOBS -c dbg //source/exe:envoy-static $BAZEL_FLAGS
  fi

  #Check if a binary was produced. If the buildprocess fails, the builder doesn't exist with a failure, which leads to all
  #further tasks being executed (Building images etc...)
  if [ ! -f "bazel-bin/source/exe/envoy-static.stripped" ]; then
      echo "Build process failed."
      exit 1
  fi


  commit_id=$(git rev-parse HEAD)
  build_date=$(date --utc --rfc-3339=seconds)

  echo -n "{\\\"version\\\":\\\"$ENVOY_VERSION-debug\\\",\\\"commit\\\":{\\\"id\\\":\\\"$commit_id\\\"},\\\"build\\\":{\\\"time\\\":\\\"$build_date\\\",\\\"builder\\\":\\\"$ENVOY_BUILDER_VERSION\\\",\\\"type\\\":\\\"$ENVOY_TYPE\\\",\\\"fips\\\":\\\"$ENVOY_BORING_SSL_FIPS\\\",\\\"ssl_key_logging\\\":\\\"$ENVOY_SSL_KEY_LOGGING\\\"}}" > $USER_HOME_DIR/$OUTPUT_DIR/envoy-static.version
  echo -n "{\\\"version\\\":\\\"$ENVOY_VERSION\\\",\\\"commit\\\":{\\\"id\\\":\\\"$commit_id\\\"},\\\"build\\\":{\\\"time\\\":\\\"$build_date\\\",\\\"builder\\\":\\\"$ENVOY_BUILDER_VERSION\\\",\\\"type\\\":\\\"$ENVOY_TYPE\\\",\\\"fips\\\":\\\"$ENVOY_BORING_SSL_FIPS\\\",\\\"ssl_key_logging\\\":\\\"$ENVOY_SSL_KEY_LOGGING\\\"}}" > $USER_HOME_DIR/$OUTPUT_DIR/envoy-static.stripped.version

  cp bazel-bin/source/exe/envoy-static.stripped $USER_HOME_DIR/$OUTPUT_DIR/envoy-static.stripped
  cp bazel-bin/source/exe/envoy-static $USER_HOME_DIR/$OUTPUT_DIR/envoy-static

  echo "Collecting debug information"
  #Enter this directory to make the collected output tar file start at /bazel-out
  #e.g. e5bf9c751cda01157a4d2829a59fc7db/execroot/envoy/
  cd $BAZEL_OUTPUT_BASE/$(ls $BAZEL_OUTPUT_BASE | grep -v cache | grep -v install)/execroot/envoy/
  echo -n "Checking directory: "
  pwd
  find -name '*.dwo' | tar czf $USER_HOME_DIR/$OUTPUT_DIR/$ENVOY_DEBUG_SYMBOLS_ARCHIVE -T -
  cd -

  echo "Collecting git repository"
  if [ $ENVOY_LOCAL_BUILD = "1" ]; then
      git archive --output=$USER_HOME_DIR/$OUTPUT_DIR/$ENVOY_SOURCE_ARCHIVE --format=tgz HEAD $ENVOY_SRC_DIR
  else
      git archive --output=$USER_HOME_DIR/$OUTPUT_DIR/$ENVOY_SOURCE_ARCHIVE --format=tgz HEAD /build/envoy
  fi

  duration=$SECONDS
  echo "Build time: $(($duration / 60)) minutes and $(($duration % 60)) seconds."
fi

#Still experimental
if [ $ENVOY_RUN_TESTS = 1 ]; then
    echo "Running Envoy Test Suite"
    echo "Storing bazel output in $BAZEL_OUTPUT_BASE-test"
    #Clock the test time
    SECONDS=0
    #TODO: Add check for envoy version
    #bazel test //test/...
    BAZEL_TEST_FLAGS=""
    if [ $ENVOY_BAZEL_BUILD = "remote" ]; then
      BAZEL_TEST_FLAGS="$BAZEL_FLAGS_REMOTE --jobs=20"
    fi
    bazel --output_user_root=$BAZEL_OUTPUT_BASE-test test '//test/extensions/filters/http/eric_proxy:*'  --copt="-Wno-error=uninitialized" --define exported_symbols=enabled --config=clang --sandbox_debug --test_arg="-l trace" $BAZEL_TEST_FLAGS
    duration=$SECONDS
    echo "Test time: $(($duration / 60)) minutes and $(($duration % 60)) seconds."
fi

#Still experimental
if [ $ENVOY_BUILD_DOCS = 1 ]; then
    echo "Running Envoy Documentation Build"
    echo "Storing bazel output in $BAZEL_OUTPUT_BASE-docs"
    #Clock the test time
    SECONDS=0
    pwd
    bazel run --//tools/tarball:target=//docs:html //tools/tarball:unpack "$PWD"/generated/docs/

    cd bazel-bin/docs
    tar -xf html.tar.gz

    duration=$SECONDS
    echo "Documentation build time: $(($duration / 60)) minutes and $(($duration % 60)) seconds."
fi
