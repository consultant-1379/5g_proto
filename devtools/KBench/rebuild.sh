#!/bin/bash

GOVERSION="1.13.7"
MY_GOPATH="$1/KBench/go"
MY_GOROOT="$1/KBench/go-root/go"

DIR="$( cd "$(dirname "$0")" ; pwd -P )";

rebuild () {
    if [ -d "$1/KBench" ]; then
       echo "KBench not installed, aborting..."
       exit 1;
    fi

    export GOPATH=$MY_GOPATH
    export GOROOT=$MY_GOROOT
    cd $MY_GOPATH/src/k-bench
    echo "Re-installing k-bench..."
    $MY_GOROOT/bin/go install cmd/kbench.go
}

rebuild;

