#!/bin/sh -x

TASK=$1

if [ ! -z ../.bob/var.namespace ]; then
	export KUBE_NAMESPACE=`cat ../.bob/var.namespace`
else
	export KUBE_NAMESPACE="5g-bsf-${USER}"
fi

echo "Selected namespace $KUBE_NAMESPACE"

KUBE_CONFIG="../.bob/${KUBE_HOST}.admin.conf"

create_sftp_secret(){
	#Create new sftp secret
	kubectl --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE create secret generic sftp-server-config --from-file=external_secrets/sftpConfig.json
	if [ $? -ne 0 ]; then
		echo "Failed to create configmap eric-sc-tap-config"
		exit 1
	fi
}

create_configmaps(){
	kubectl --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE create configmap eric-sc-tap-config --from-file tapagent_configmap/tap_config.json
	if [ $? -ne 0 ]; then
		echo "Failed to create configmap eric-sc-tap-config"
		exit 1
	fi
}

# Arguments: task to be executed
if [ "$1" = "sftp-secret" ]; then
	create_sftp_secret
elif [ "$1" = "configmaps" ]; then
	create_configmaps
else
	echo "Unsupported argument!"
fi