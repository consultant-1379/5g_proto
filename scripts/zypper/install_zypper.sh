#!/bin/bash

CBOS_VERSION=
CBOS_REPO=https://arm.sero.gic.ericsson.se/artifactory/proj-ldc-repo-rpm-local/common_base_os/sles/$CBOS_VERSION
NAMESPACE=$1
POD=$2
[[ ! -z "$3" ]] && CONTAINER="-c $3"

#Put files into containter
#Tar has to be transferred without using kubectl cp
echo "Copying tar binary to container."
cat tar | kubectl -n $NAMESPACE $CONTAINER exec -i $POD -- tee /usr/bin/tar >/dev/null
echo "Setting permissions for tar binary."
kubectl -n $NAMESPACE $CONTAINER exec -it $POD -- /bin/bash -c "chmod 755 /usr/bin/tar"
echo "Copying rpm tarball to container."
kubectl -n $NAMESPACE $CONTAINER cp zypper.tar $POD:/zypper.tar
echo "Extracting rpm tarball in container."
kubectl exec -it -n $NAMESPACE $CONTAINER $POD -- /bin/bash -c "tar -xf /zypper.tar -C /"

#Install rpms/zypper
echo "Installing Zypper in container."
kubectl exec -it -n $NAMESPACE $CONTAINER $POD -- /bin/bash -c "rpm --quiet -i /zypper-rpm/*"

#Setup CBOS Repo
echo "Setting up SLES repo in container."
kubectl exec -it -n $NAMESPACE $CONTAINER $POD -- /bin/bash -c "zypper addrepo --gpgcheck-strict $CBOS_REPO CBOS_REPO"
kubectl exec -it -n $NAMESPACE $CONTAINER $POD -- /bin/bash -c "zypper --gpg-auto-import-keys refresh -f"
