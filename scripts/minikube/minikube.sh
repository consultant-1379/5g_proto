#!/bin/bash

#Author: eedbjhe

#TODO:
#- Create storage class network-block
#- Add download & unpackaging of /proj zip pack
#  Where to store public zipfile?
#- Do ssh setup (e.g. copy keys that are put in a dir and set proper permissions)
#- cadvisor: cpu-shares vs. cpu-quota? Check. https://github.com/google/cadvisor/issues/2220
#- BSF diameter (Network issue)
#- Add /etc/resolf.conf:
#  nameserver 8.8.8.8
#- add https://github.com/davidB/kubectl-view-allocations
#- add: dive
#- Introduce ECCD profiles
#  https://eteamspace.internal.ericsson.com/display/CPFE/Ericsson+Cloud+Container+Distribution+2.25.0+Release+Notes
#- Get codename with lsb_release -cs
#- Check docker suggested packages: aufs-tools cgroupfs-mount | cgroup-lite


# Changelog
# 0.1 Initial version
# 0.2 Added K3D
# 0.3 Install procps
# 0.4 Install pip/pyyaml
# 0.5 Print version
#     Set iptables-legacy for Ubuntu 20
# 0.6 Update k3s image to 1.24.2
#     Uprade k3d to 5.4.8
#     Upgrade kubectl to 1.24.2
# 0.7 Update k3d to 5.4.9
#     Install unzip
# 0.8 Update k3d to 5.5.1
#     Add first support to switch from flanel to calico to support new updateHostFile script
# 0.9 Cleanup
#     IPtables legacy setup for Ubuntu 20/22
#     Support of DOCKER_NETWORK parameter for testing (Needed for BSF Diameter)
#     Updated k3d to 5.5.2
#     Updated jdk to 17
#     Removed support for Ubuntu 18.04
#     Added support for Ubuntu 22.04
# 0.10 Add jq, libswt-gtk-4-jni
#      Upgrade to kubernetes 1.25.5
#      Upgrade to kubectl 1.25.5
# 0.11 Upgrade to kubernetes/kubectl 1.27.6
# 0.12 Upgrade to kubernetes/kubectl 1.28.4
#      Increase cluster memory
# 0.13 Add metallb installation incl. config

#Get Ubuntu codename for Docker version
source /etc/os-release

OUTPUT_DIR=.ericsson-minikube

VERSION=0.13
#List versions: apt-cache madison docker-ce | awk '{ print $3 }'
DOCKER_VERSION=5:24.0.3-1~ubuntu.$VERSION_ID~$VERSION_CODENAME
HELM_VERSION=v3.10.2
K3D_VERSION=v5.6.0
KUBERNETES_VERSION=v1.28.4
KUBECTL_VERSION=$KUBERNETES_VERSION
METALLB_VERSION=v0.14.3

#Available images: https://hub.docker.com/r/rancher/k3s/tags

#ECCD 2.27.2 - 1.28.2
#ECCD 2.26.4 - 1.27.4
#ECCD 2.25.0 - 1.26.1

#K3D_KUBERNETES_IMAGE=docker.io/rancher/k3s:v1.25.3-k3s1-amd64
#K3D_KUBERNETES_IMAGE=docker.io/rancher/k3s:v1.25.5-k3s1-amd64
#K3D_KUBERNETES_IMAGE=docker.io/rancher/k3s:v1.27.6-k3s1-amd64
K3D_KUBERNETES_IMAGE=docker.io/rancher/k3s:$KUBERNETES_VERSION-k3s2-amd64


#Global Protect DNS Servers
DNS_01=193.181.14.10
DNS_02=193.181.14.11
#At home: Router / At work: ? tbd
#DNS_03=192.168.1.1 #Probably not needed?

WSL_IP=$(ifconfig eth0 | grep 'inet ' | awk '{ print $2}')

INFO_DOCKER=$'Shutdown your WSL session now via a windows shell and restart it:
wsl --shutdown
If you have opened another shell of the running WSL session your DNS settings have been reset and you need
to run the network-setup again.
After the first login after, you need to run
sudo service docker start
once. All subsequent starts of the WSL session will have the docker service enabled.'

INFO_ULIMIT=$'To use the cluster you need to manually raise the ulimit for open files by giving:
sudo sh -c "ulimit -n 65535 && exec su $USER"'

mkdir -p $OUTPUT_DIR

check_ulimit(){

  ulimit=$(ulimit -Hn)
  if [ "$ulimit" != "65535" ]
  then
    echo "Ulimit is not properly set."
	echo $INFO_ULIMIT
	exit 0
  fi

}

packages_install(){  

  echo "## Performing system update and installing necessary packages."
  sudo apt -qq -o=Dpkg::Use-Pty=0 update
  sudo apt -qq -o=Dpkg::Use-Pty=0 upgrade -y
  sudo apt -qq -o=Dpkg::Use-Pty=0 install -y net-tools openjdk-17-jdk expect git make curl procps unzip jq libswt-gtk-4-jni
  if [ "$VERSION_ID" == "20.04" ]
  then
      echo "Ubuntu 20"
      sudo apt -qq -o=Dpkg::Use-Pty=0 install -y python python3.8 python3-pip      
      sudo update-alternatives --quiet --install /usr/bin/python python /usr/bin/python2.7 1
      sudo update-alternatives --quiet --install /usr/bin/python python /usr/bin/python3.8 2
      sudo update-alternatives --quiet --set python /usr/bin/python3.8  
  elif [ "$VERSION_ID" == "22.04" ]
  then 
      echo "Ubuntu 22"
	  sudo apt -qq -o=Dpkg::Use-Pty=0 install -y python3-pip
      sudo update-alternatives --quiet --install /usr/bin/python python /usr/bin/python3.10 1      
      sudo update-alternatives --quiet --install /usr/bin/python3 python3 /usr/bin/python3.10 1
      sudo update-alternatives --quiet --set python /usr/bin/python3.10
      sudo update-alternatives --quiet --set python3 /usr/bin/python3.10
   else
      echo "Unsupported Ubuntu Version."
   fi
   pip3 install pyyaml
   
}
network_install(){

  echo "## Adding Global Protect DNS servers"
  echo -e "[network]\ngenerateResolvConf = false" | sudo tee /etc/wsl.conf > /dev/null
  resolv_original=$(cat /run/resolvconf/resolv.conf)
  sudo rm -rf /etc/resolv.conf
  sudo touch /etc/resolv.conf
  echo "## Setting DNS servers:"
  echo "nameserver $DNS_01" | sudo tee -a /etc/resolv.conf
  echo "nameserver $DNS_02" | sudo tee -a  /etc/resolv.conf
  #echo "nameserver $DNS_03" | sudo tee -a  /etc/resolv.conf 
  echo "$resolv_original" | sudo tee -a /etc/resolv.conf
 
}

#Enables systemd and increases open file limit for the user
system_setup(){

  echo "## Enabling systemd"
  echo -e "[boot]\nsystemd = true" | sudo tee -a /etc/wsl.conf > /dev/null
  #Increase #of open files for the user.
  #Workaround for WSL Bug: https://github.com/Microsoft/WSL/issues/1688
  #Needed to fix elasticsearch: max file descriptors [4096] for opensearch process is too low, increase to at least [65535]  
  #Allows to raise the ulimit -n hardlimit for the elasticsearch pods to run properly
  #TODO: Check if it is already set
  echo "## Raising numer of open files for user $USER"
  echo -e "$USER soft nproc 65535\n$USER hard nproc 65535\n$USER soft nofile 65536\n$USER hard nofile 65536\n" | sudo tee -a /etc/security/limits.conf >> /dev/null
  
}

docker_install(){

  echo "## Installing docker $DOCKER_VERSION"
  sudo apt -qq -o=Dpkg::Use-Pty=0 install -y ca-certificates curl gnupg lsb-release
  sudo mkdir -p /etc/apt/keyrings
  curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
  echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
  sudo apt -qq -o=Dpkg::Use-Pty=0 update
  sudo apt -qq -o=Dpkg::Use-Pty=0 install -y docker-ce=$DOCKER_VERSION docker-ce-cli=$DOCKER_VERSION containerd.io docker-compose-plugin
  sudo usermod -aG docker $USER
  echo "%docker ALL=(ALL)  NOPASSWD: /usr/bin/dockerd" | sudo tee -a /etc/sudoers
  sudo service docker start
  sudo systemctl enable docker.service
  sudo systemctl enable containerd.service
  
  #iptables reconfiguration is needed for Ubuntu 20/22
  sudo update-alternatives --set iptables /usr/sbin/iptables-legacy
  sudo update-alternatives --set ip6tables /usr/sbin/ip6tables-legacy  

}


k3d_install() {

  echo "## Installing K3D $K3D_VERSION"
  curl -s https://raw.githubusercontent.com/k3d-io/k3d/main/install.sh | TAG=$K3D_VERSION bash
  
}

#Untested
k3d_uninstall() {

  sudo rm -rf /usr/local/bin/k3d

}

k3d_cluster_create() {
  
  sudo sysctl -w vm.max_map_count=262144
  #vm.swappiness = 1 not sure if needed
  sudo sysctl -w fs.inotify.max_user_instances=256
  #sudo sysctl -w fs.inotify.max_user_watches=100000 #Another possible fix for elastisearch
    
  #Kubernetes < 1.25
  #k3d cluster create ericsson --agents 1 --servers 1 --api-port $WSL_IP:8443 --image $K3D_KUBERNETES_IMAGE  --k3s-arg "--cluster-init@server:*" --k3s-arg "--no-deploy=servicelb@server:*" --k3s-arg="--kubelet-arg=cpu-cfs-quota=false@server:*" --k3s-arg="--kubelet-arg=cpu-cfs-quota=false@agent:*"

  #Kubernetes > 1.24
  #cluster-init: enables etcd backend (Default is SQLite)

  k3d cluster create ericsson \
  --agents 1 \
  --agents-memory 24GB \
  --servers 1 \
  --servers-memory 24GB \
  --api-port $WSL_IP:8443 \
  --image $K3D_KUBERNETES_IMAGE \
  --k3s-arg "--disable=traefik@server:0" \
  --k3s-arg "--disable=servicelb@server:0" \
  --k3s-arg "--cluster-init@server:0" \
  --k3s-arg "--kubelet-arg=cpu-cfs-quota=false@server:0" \
  --k3s-arg "--kubelet-arg=cpu-cfs-quota=false@agent:0"
  
  #Will keep all Pods on the worker node
  kubectl taint node k3d-ericsson-server-0 node-role.kubernetes.io/control-plane:NoSchedule
  kubectl taint node k3d-ericsson-server-0 node-role.kubernetes.io/master:NoSchedule
  #Necessary for CI port scripts to find the proper node
  kubectl label node k3d-ericsson-agent-0 node-role.kubernetes.io/node=  
  
  cp ~/.kube/config ~/.kube/minikube.config
  cp ~/.kube/config ~/5g_proto/devenv/kube-minikube.conf
  cp ~/.kube/config ~/5g_proto/.bob/minikube.admin.conf

  kubectl create namespace 5g-bsf-$USER
  kubectl create namespace eric-crds

  cd ~/5g_proto/esc
  make deploy-crds
  
  helm -n eric-crds install eric-data-key-value-database-rd-crd https://arm.sero.gic.ericsson.se/artifactory/proj-adp-gs-all-helm/eric-data-key-value-database-rd-crd/eric-data-key-value-database-rd-crd-1.1.0+1.tgz
  
  cd ~/5g_proto/scripts/minikube
  echo "Installing Metallb"
  kubectl apply -f https://raw.githubusercontent.com/metallb/metallb/$METALLB_VERSION/config/manifests/metallb-native.yaml
  cluster_name=k3d-ericsson
  cidr_block=$(docker network inspect $cluster_name | jq '.[0].IPAM.Config[0].Subnet' | tr -d '"')
  cidr_base_addr=${cidr_block%???}
  ingress_first_addr=$(echo $cidr_base_addr | awk -F'.' '{print $1,$2,255,0}' OFS='.')
  ingress_last_addr=$(echo $cidr_base_addr | awk -F'.' '{print $1,$2,255,255}' OFS='.')
  ingress_range=$ingress_first_addr-$ingress_last_addr  
  while true; do ready=$(kubectl -n metallb-system get daemonset speaker -o jsonpath="{.status.numberReady}"); [[ "$ready" != "2" ]] || break; echo "Waiting for metallb to start up. $ready of 2 pods ready."; sleep 2; done
  cat metallb-address-pool.yaml | sed 's/INGRESS_RANGE/'$ingress_range'/g' | kubectl apply -f -
  kubectl apply -f metallb-l2-advertisement.yaml
  
  echo $INFO_ULIMIT

}


k3d_cluster_start() {

  check_ulimit
  #max file descriptors [4096] for opensearch process is too low, increase to at least [65535]
  #if this doesn't work: /etc/security/limits.conf
  # https://askubuntu.com/questions/1049058/how-to-increase-max-open-files-limit-on-ubuntu-18-04
  #vdi: fs.file-max = 3138126  
  #Probably not needed anymore with ulimit workaround.  
  #sudo sysctl -w fs.file-max=131072 #262144 #2097152 #Working? Docker container of node shows correct value
  sudo sysctl -w vm.max_map_count=262144
  #vm.swappiness = 1 not sure if needed
  sudo sysctl -w fs.inotify.max_user_instances=256
  k3d cluster start ericsson
  
}


k3d_cluster_stop() {

  k3d cluster stop ericsson

}


k3d_cluster_delete() {

  k3d cluster delete ericsson

}


kubectl_install(){

  echo "## Installing kubectl $KUBECTL_VERSION"
  curl --silent -Lo $OUTPUT_DIR/kubectl https://dl.k8s.io/release/$KUBECTL_VERSION/bin/linux/amd64/kubectl
  sudo install -o root -g root -m 0755 $OUTPUT_DIR/kubectl /usr/local/bin/kubectl
  rm -rf $OUTPUT_DIR/kubectl
  
}

#Untested
kubectl_uninstall(){

 echo "## Uninstalling kubectl $KUBECTL_VERSION"
 sudo rm -rf /usr/local/bin/kubectl
 
}


helm_install(){

  echo "## Installing helm $HELM_VERSION"
  curl --silent -Lo $OUTPUT_DIR/helm-$HELM_VERSION-linux-amd64.tar.gz https://get.helm.sh/helm-$HELM_VERSION-linux-amd64.tar.gz
  tar -C $OUTPUT_DIR -xf $OUTPUT_DIR/helm-$HELM_VERSION-linux-amd64.tar.gz
  sudo install -o root -g root -m 0755 $OUTPUT_DIR/linux-amd64/helm /usr/local/bin/helm
  rm -rf $OUTPUT_DIR/helm-$HELM_VERSION-linux-amd64.tar.gz
  rm -rf $OUTPUT_DIR/linux-amd64/
  
}

#Untested
helm_uninstall(){
 
  echo "## Uninstalling helm"
  sudo rm -rf /usr/local/bin/helm
 
}



echo "SC Environment Installer $VERSION"
echo ""

if [ "$1" = "install" ]
then
  if [ "$2" = "docker" ]
  then
    docker_install
  elif [ "$2" = "network" ]
  then
    network_install
  elif [ "$2" = "helm" ]
  then
    helm_install
  elif [ "$2" = "kubectl" ]
  then
    kubectl_install
  elif [ "$2" = "k3d" ]
  then
    k3d_install
  elif [ "$2" = "all" ]
  then
    system_setup
	network_install
	packages_install
	docker_install
    kubectl_install
    helm_install
	k3d_install
  else
    echo "Unkown option."
  fi
fi

if [ "$1" = "uninstall" ]
then
  if [ "$2" = "helm" ]
  then
    helm_uninstall
  elif [ "$2" = "k3d" ]
  then
    k3d_uninstall
  elif [ "$2" = "kubectl" ]
  then
    kubectl_uninstall
  else
    echo "Unkown option."
  fi
fi

if [ "$1" = "k3d" ]
then
  if [ "$2" = "cluster" ]
  then
    if [ "$3" = "create" ]
	then
	  k3d_cluster_create
    elif [ "$3" = "delete" ]
	then
	  k3d_cluster_delete
    elif [ "$3" = "start" ]
	then
	  k3d_cluster_start
    elif [ "$3" = "stop" ]
	then
	  k3d_cluster_stop	  
	fi
  fi
fi


if [ "$1" = "kind-install" ]
then
  kind_install
elif [ "$1" = "kind-start" ]
then
  kind_start
elif [ "$1" = "metrics-install" ]
then
  metrics_server_install
elif [ "$1" = "packages-install" ]
then
  packages_install  
elif [ "$1" = "load-balancer-install" ]
then
  load_balancer_install
elif [ "$1" = "k3d-install" ]
then
  k3d_install  
elif [ "$1" = "k3d-cluster-stop" ]
then
  k3d_cluster_stop
elif [ "$1" = "k3d-cluster-create" ]
then
  k3d_cluster_create
elif [ "$1" = "k3d-cluster-start" ]
then
  k3d_cluster_start
elif [ "$1" = "check-ulimit" ]
then
  check_ulimit
elif [ "$1" = "all" ]
then
  system_setup
  network_install  
  packages_install
  docker_install
  kubectl_install
  helm_install
  #kind_install
  echo "$INFO_DOCKER"
fi  
