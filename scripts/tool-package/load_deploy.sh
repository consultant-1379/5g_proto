#!/bin/sh

#Loads, tags and deploys images on the target system
#author: eedbjhe

KUBE_NAMESPACE=$1

REGISTRY_PORT=$(kubectl get svc ingress-nginx -n ingress-nginx -o jsonpath='{.spec.ports[?(@.name=="https")].nodePort}')

CHFSIM_VERSION=$(cat versions/var.chfsim-version)
REDIS_VERSION=$(cat versions/var.redis-version)
K6_VERSION=$(cat versions/var.k6-version)
INFLUXDB_VERSION=$(cat versions/var.influxdb-version)
NRFSIM_VERSION=$(cat versions/var.nrfsim-version)
USER_ID=$(cat versions/user_id)

user=$(kubectl -n kube-system get secret cr-registry-credentials -o json | jq '.data."custom-user"' -r | base64 -d)
pass=$(kubectl -n kube-system get secret cr-registry-credentials -o json | jq '.data."custom-pass"' -r | base64 -d)
echo "Use user: '$user' und password: '$pass' when asked"

load_image(){

  IMAGE_NAME=$1
  IMAGE_VERSION=$2
  NAME=$3
  
  sudo docker load -i docker/$IMAGE_NAME-$IMAGE_VERSION.tar
  sudo docker login k8s-registry.eccd.local:$REGISTRY_PORT
  sudo docker tag armdocker.rnd.ericsson.se/proj-5g-bsf/$USER_ID/$IMAGE_NAME:$IMAGE_VERSION k8s-registry.eccd.local:$REGISTRY_PORT/proj-5g-bsf/$USER_ID/$IMAGE_NAME:$IMAGE_VERSION
  sudo docker push k8s-registry.eccd.local:$REGISTRY_PORT/proj-5g-bsf/$USER_ID/$IMAGE_NAME:$IMAGE_VERSION
  
  helm install -n $NAME --namespace $KUBE_NAMESPACE helm/$IMAGE_NAME-$IMAGE_VERSION.tgz -f values-local.yaml 

}

#Load docker images, comment (#) the lines that you don't want to deploy
load_image eric-chfsim $CHFSIM_VERSION chfsim
load_image eric-chfsim-redis $REDIS_VERSION redis
load_image eric-k6 $K6_VERSION k6
load_image eric-influxdb $INFLUXDB_VERSION influxdb
load_image eric-nrfsim $NRFSIM_VERSION nrfsim

 
