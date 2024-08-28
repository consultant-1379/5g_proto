#!/bin/bash

GOVERSION="1.13.7"
MY_INSTALL_DIR="$1/KBench"
MY_GOPATH="$MY_INSTALL_DIR/go"
MY_GOROOT="$MY_INSTALL_DIR/go-root/go"
REDIS_POD_PATH="$MY_GOPATH/src/k-bench/config/dp_redis_density/redis_pod.yaml"

GOFILE="go$GOVERSION.linux-amd64.tar.gz"
GO_DOWNLOADLINK="https://dl.google.com/go/$GOFILE"
KKOMPOSE_DOWNLOADLINK="github.com/kubernetes/kompose"

DIR="$( cd "$(dirname "$0")" ; pwd -P )";

set_env () {
    echo "setting GOPATH and GOROOT env var..."
    export GOPATH=$MY_GOPATH
    export GOROOT=$MY_GOROOT
}

install_go () {
    GOFILE="go$GOVERSION.linux-amd64.tar.gz"
    echo ""
    echo "Installing local go package in folder $MY_INSTALL_DIR ..."
    if [ -d $MY_GOROOT ]; then
        echo "Installation directories already exist $MY_GOROOT"
        read -p "Would you like to overwrite (y/n)? " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            #exit 1
            echo "skip installing local go package..."
            return 1
        fi
    fi

    mkdir -p "$MY_GOROOT" "$MY_GOPATH" "$MY_GOPATH/src" "$MY_GOPATH/pkg" "$MY_GOPATH/bin"

    echo "Downloading go package..."
    sudo wget $GO_DOWNLOADLINK -O $1/$GOFILE
    if [ $? -ne 0 ]; then
        echo "Go download failed! Exiting."
        exit 1
    fi

    echo "Installing local go package..."

    tar -C $(dirname $MY_GOROOT) -xzf $1/$GOFILE
}

install_kb () {
    mkdir -p $MY_INSTALL_DIR
    mkdir -p $MY_GOPATH/src/
    cp -r $DIR/k-bench $MY_GOPATH/src/
    cd $MY_GOPATH/src/k-bench
    if [ -d "$MY_GOPATH/pkg/mod/github.com/kubernetes/kompose"* ]; then
       echo "Kompose already installed."
       read -p "Would you like to overwrite (y/n)? " -n 1 -r
       echo
       if [[ ! $REPLY =~ ^[Yy]$ ]]; then
           echo "skip installing local kubernetes kompose package..."
       else
           echo "Re-installing local kubernetes kompose package..."
           $MY_GOROOT/bin/go get -u $KKOMPOSE_DOWNLOADLINK
       fi
    else
       echo "Installing local kubernetes kompose package..."
       $MY_GOROOT/bin/go get -u $KKOMPOSE_DOWNLOADLINK
    fi
    
    echo "Installing k-bench..."
    $MY_GOROOT/bin/go install cmd/kbench.go

    sudo cp $MY_GOPATH/bin/kbench /usr/local/bin/
    if [ $? -ne 0 ]; then
        echo "Tried to copy kbench to /usr/local/bin but failed. If you are not root, run from $MY_GOPATH/bin/kbench"
    fi

    chmod -R 755 $MY_GOPATH/src/k-bench/config/*
    echo "k-bench configuration files can be found under folder $MY_GOPATH/src/k-bench/config"

    echo "Completed k-bench installation. To rebuild the benchmark, just run \"make rebuild\""
    echo "please note that the commands need to be executed from folder $MY_GOPATH/src/k-bench"
    echo "example:"
    echo "cd $MY_GOPATH/src/k-bench"
    echo "mkdir -p results"
    echo "kbench -benchconfig ./config/dp_network_internode/config.json -outdir ./results"
    echo ""
    echo "to run all scenarios execute following command"
    echo "cd $MY_GOPATH/src/k-bench"
    echo "./run.sh -t all"
}


post_installation () {
    sed -i 's,kbench -benchconfig,'$MY_GOPATH/bin/kbench' -benchconfig,' $MY_GOPATH/src/k-bench/run.sh
    mkdir -p $MY_GOPATH/src/k-bench/config/pv
    mkdir -p $MY_GOPATH/src/k-bench/config/pvc
    cp $MY_GOPATH/src/k-bench/config/cp_other_resources/pv_pvc/pv.yaml $MY_GOPATH/src/k-bench/config/pv
    cp $MY_GOPATH/src/k-bench/config/cp_other_resources/pv_pvc/pv_config.json $MY_GOPATH/src/k-bench/config/pv/config.json
    cp $MY_GOPATH/src/k-bench/config/cp_other_resources/pv_pvc/pvc.yaml $MY_GOPATH/src/k-bench/config/pvc
    cp $MY_GOPATH/src/k-bench/config/cp_other_resources/pv_pvc/pvc_config.json $MY_GOPATH/src/k-bench/config/pvc/config.json
    sed -i 's,'p_other_resources/pv_pvc',pv,' $MY_GOPATH/src/k-bench/config/pv/config.json
    sed -i 's,'p_other_resources/pv_pvc',pvc,' $MY_GOPATH/src/k-bench/config/pvc/config.json
    for f in $MY_GOPATH/src/k-bench/config/dp_*/*_pod.yaml; do sed -i "s/nginx/armdockerhub\.rnd\.ericsson\.se\/nginx\:latest/g" "$f"; done
    storage_class=$(kubectl get sc | grep -m1 'ceph.com/rbd' | awk '{print $1}')
    sed -i "s/wcp-policy/${storage_class}/g" $MY_GOPATH/src/k-bench/config/dp_fio/fio_pvc.yaml
    sed -i -e "$(grep -hn affinity $REDIS_POD_PATH | cut -d':' -f1), $(grep -hn vmware $REDIS_POD_PATH | cut -d':' -f1)d" $REDIS_POD_PATH
    sed -i "s/\"Cleanup\": true/\"Cleanup\": false/g" $MY_GOPATH/src/k-bench/config/default/config.json
}

set_env;

echo "Start to install the benchmark and tools...";

install_go;

install_kb;

post_installation;
