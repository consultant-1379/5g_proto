#!/bin/bash

## build_app_layer
##
##   Build application layer, same as with Dockerfile
##
#TODO: one function, parameters

#Should enabled layer caching, doesn't seem to work
#export BUILDAH_LAYERS=true


#build_dockerfile() {
#    buildah build -f esc/sc-sles/Dockerfile --build-arg CBOS_IMG="armdocker.rnd.ericsson.se/proj-ldc/common_base_os_micro/sles:5.4.0-16" --build-arg CBOS_HARDENING_ARCHIVE=$CBOS_HARDENING_ARCHIVE --build-arg CBOS_HARDENING_REPO=$CBOS_HARDENING_REPO --build-arg CBOS_JAVA=$CBOS_JAVA --build-arg CBOS_OPENSSL=$CBOS_OPENSSL --build-arg CBOS_REPO=$CBOS_REPO -t $REGISTRY/$IMAGE_NAME:$VERSION-dockerfile
#}


build_layer() {

    zypper -n --installroot "$rootdir" install --no-recommends $PACKAGES &&\
    hardening &&\
    # Save info about the packages
    rpm --root "$rootdir" -qa > "$rootdir"/.app-rpms

#    buildah config \
#         --layers=true
#        --label org.opencontainers.image.title="Example" \
#        --label maintainer="Example example@example.com" \
#        --user 12345 \
#        --entrypoint '[ "/usr/bin/catatonit", "--"]' "$container"

}


## mount_microcbo_container
##
## Create a container from microcbo
##
## The root directory is availabe with $rootdir
##
mount_microcbo_container() {
    zypper ar --no-check --gpgcheck-strict -f ${CBO_REPO}/${CBO_VERSION} CBO_REPO
    zypper ar --no-check --gpgcheck-strict -f ${CBO_DEVENV_REPO}/${CBO_DEVENV_VERSION} CBO_DEVENV
    zypper --gpg-auto-import-keys refresh

    container=$(buildah from --authfile /config.json "$MICROCBO_IMAGE")
    rootdir=$(buildah mount "$container")
    mkdir -p "$rootdir/proc/" "$rootdir/dev/"
    mount -t proc /proc "$rootdir/proc/"
    mount --rbind /dev "$rootdir/dev/"
    cp /etc/host.conf "$rootdir/etc/"
}


## upload_image
##
##   commit and upload the created application image
##
upload_image() {
    echo "# Pushing image: $REGISTRY/$IMAGE_NAME:$VERSION"
    echo "############################"
    umount "$rootdir/proc/"
    umount -l "$rootdir/dev/"
    buildah commit -f docker "$container" "$REGISTRY/$IMAGE_NAME:$VERSION"
    skopeo copy --dest-authfile=/config.json \
        containers-storage:"$REGISTRY/$IMAGE_NAME:$VERSION" \
        "docker://$REGISTRY/$IMAGE_NAME:$VERSION"
}

## create_builder
##
##   Create builder layer
##
create_builder() {
    zypper ar --no-check --gpgcheck-strict -f ${CBO_DEVENV_REPO}/${CBO_DEVENV_VERSION} CBO_DEVENV
    zypper --gpg-auto-import-keys refresh
    # install the required tools
    zypper -n install --no-recommends -l buildah skopeo util-linux curl xz
    sed -i 's/^driver =.*/driver="vfs"/' /etc/containers/storage.conf
    zypper rr CBO_DEVENV
}


hardening(){
    curl "${CBOS_HARDENING_REPO}/${CBOS_HARDENING_ARCHIVE}" | tar xz &&\
    chmod 750 ./cbo-harden.sh &&\
    ./cbo-harden.sh &&\
    rm ./cbo-harden.sh
}

create_builder
mount_microcbo_container
IMAGE_NAME=$1
PACKAGES=$2
build_layer
echo "############################"
echo "# Installed packages: $PACKAGES"
upload_image
