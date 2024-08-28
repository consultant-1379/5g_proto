#!/bin/sh

export GIT_PATH="/local/git"
export MINIKUBE_NODE_IP=192.168.99.100
export NAMESPACE=5g-bsf-$USER

###############################################################################################################################
# useful Aliases ##############################################################################################################
###############################################################################################################################

alias chfsimlog='~/bin/stern -n $NAMESPACE chfsim'
alias dpika='ssh eccd@10.117.19.203'
alias deev='ssh eccd@10.221.169.167'
unalias k
alias k='kubectl -n $NAMESPACE'
alias kdp='k describe pod'
alias kgc='k get crds'
alias kgn='kubectl get nodes -o wide'
alias kgp='k get pods'
alias kgpg='k get pods |grep '
alias kgs='k get svc'
alias kgsg='k get svc |grep '
alias kl='k logs'
alias ks='kubectl -n kube-system'

alias ktdp='kubectl -n tools describe pod'
alias ktgp='kubectl -n tools get pods'
alias ktgpg='kubectl -n tools get pods |grep '
alias ktgs='kubectl -n tools get svc'
alias ktgsg='kubectl -n tools get svc |grep '
alias ktl='kubectl -n tools logs'

alias ke='kubectl -n 5g-bsf-eiffelesc-1'
alias kedp='kubectl -n 5g-bsf-eiffelesc-1 describe pod'
alias kegp='kubectl -n 5g-bsf-eiffelesc-1 get pods'
alias kegpg='kubectl -n 5g-bsf-eiffelesc-1 get pods |grep '
alias kegs='kubectl -n 5g-bsf-eiffelesc-1 get svc'
alias kegsg='kubectl -n 5g-bsf-eiffelesc-1 get svc |grep '
alias kel='kubectl -n 5g-bsf-eiffelesc-1 logs'


###############################################################################################################################
#Help Functions################################################################################################################
###############################################################################################################################


function setEnvVar()
{
  export NAMESPACE=5g-bsf-$USER

  setEnvVarNoNamespace;
}

function setEnvVarNoNamespace()
{
  export KUBE_NAMESPACE=$NAMESPACE
  export NODE_IP=$(kubectl get nodes --namespace $NAMESPACE -o jsonpath="{.items[3].status.addresses[0].address}")
  export CMM_PORT=$(kubectl get --namespace $NAMESPACE -o jsonpath="{.spec.ports[0].nodePort}" services eric-cm-mediator)
  #export CMM_CONFIG_NAME=ericsson-scp

  if kubectl get --namespace $NAMESPACE -o jsonpath="{.spec.ports[1].nodePort}" services eric-cm-yang-provider-ipv4  2> /dev/null > /dev/null; then
    export YP_PORT=$(kubectl get --namespace $NAMESPACE -o jsonpath="{.spec.ports[0].nodePort}" services eric-cm-yang-provider-ipv4)  # take the port mapped to 830
  else
    export YP_PORT=$(kubectl get --namespace $NAMESPACE -o jsonpath="{.spec.ports[0].nodePort}" services eric-cm-yang-provider-ipv4-external)  # take the port mapped to 830
  fi

  if kubectl get --namespace $NAMESPACE -o jsonpath="{.spec.ports[1].nodePort}" services eric-cm-yang-provider-ipv4  2> /dev/null > /dev/null; then
    export CLI_PORT=$(kubectl get --namespace $NAMESPACE -o jsonpath="{.spec.ports[1].nodePort}" services eric-cm-yang-provider-ipv4)  # take the port mapped to 22
  else
    export CLI_PORT=$(kubectl get --namespace $NAMESPACE -o jsonpath="{.spec.ports[1].nodePort}" services eric-cm-yang-provider-ipv4-external)  # take the port mapped to 22
  fi
  export SCP_PORT=$(kubectl get --namespace $NAMESPACE -o jsonpath="{.spec.ports[0].nodePort}" services eric-scp-worker)
  export SCP_POD=$(kubectl get pods --namespace $NAMESPACE -l "app=eric-scp-worker" -o jsonpath="{.items[0].metadata.name}")
  export SCP_MANAGER_POD=$(kubectl get pods --namespace $NAMESPACE -l "app=eric-scp-manager" -o jsonpath="{.items[0].metadata.name}")
  export PM_PORT=$(kubectl get --namespace $NAMESPACE -o jsonpath="{.spec.ports[0].nodePort}" services eric-pm-server)
  #export CHFSIM_PORT=$(kubectl get --namespace $NAMESPACE -o jsonpath="{.spec.ports[0].nodePort}" services eric-chfsim-1)
  #export CHFSIM_PORT1=$(kubectl get --namespace $NAMESPACE -o jsonpath="{.spec.ports[0].nodePort}" services eric-chfsim-1)
  #export CHFSIM_PORT2=$(kubectl get --namespace $NAMESPACE -o jsonpath="{.spec.ports[0].nodePort}" services eric-chfsim-2)
  #export CHFSIM_PORT3=$(kubectl get --namespace $NAMESPACE -o jsonpath="{.spec.ports[0].nodePort}" services eric-chfsim-3)
  export NRFSIM_PORT=$(kubectl get --namespace $NAMESPACE -o jsonpath="{.spec.ports[0].nodePort}" services eric-nrfsim)
  export MONITOR_PORT=$(kubectl get --namespace $NAMESPACE  -o jsonpath="{.spec.ports[0].nodePort}" services eric-sc-monitor)
  export CNOM_PORT=$(kubectl get --namespace $NAMESPACE -o jsonpath="{.spec.ports[0].nodePort}" services eric-tm-ingress-controller-cr-ipv4)
  export SEPP_PORT=$(kubectl get --namespace $NAMESPACE -o jsonpath="{.spec.ports[0].nodePort}" services eric-sepp-worker)
  echo "ENV VAR SET"
}

###############################################################################################################################
# environment variables for SCP ###############################################################################################
###############################################################################################################################

setEnvVar;


###############################################################################################################################
#Troubleshooting stuff#########################################################################################################
###############################################################################################################################

function portForwardingEnvoy () 
{

   k port-forward pod/$SCP_POD 9901:9901 &
   echo "Port Forwarding is now set, open localhost:9901 in browser....."
}


checkPodStatus () 
{ 
    kubectl -n $NAMESPACE get po | grep -v Completed |grep -v eric-data-search-engine-curator | awk -F"[ /]+" 'BEGIN{found=0} !/NAME/ {if ($2!=$3) { found=1; print $0}} END { if (!found) print "All containers are up"}'
}

###############################################################################################################################
# Alarm related stuff #########################################################################################################
###############################################################################################################################
alarm () 
{ 
    echo "Fetching current alarms";
    alarmPod=$(kubectl -n $NAMESPACE get pods | grep -m 1 'fh-alarm' | awk '{ print $1 }');
    kubectl -n $NAMESPACE exec -it $alarmPod -- ah_alarm_list.sh
    #kubectl -n $NAMESPACE exec -it $alarmPod -- curl -i -sS -X GET https://localhost:5006/ah/api/v0/alarms?outputFormat=ShortAlarmList\&pp --cacert /etc/sip-tls-ca/cacertbundle.pem --cert /etc/sip-tls-client/clicert.pem --key /etc/sip-tls-client/cliprivkey.pem

    #kubectl -n $NAMESPACE exec -it $alarmPod -- curl -i -sS -X GET https://localhost:5006/ah/api/v0/alarms?outputFormat=ShortAlarmList\&pp --cacert /var/run/secrets/siptls-root/cacertbundle.pem --cert /var/run/secrets/client-cert/clicert.pem --key /var/run/secrets/client-cert/cliprivkey.pem

}

###############################################################################################################################
# Configuration related stuff #################################################################################################
###############################################################################################################################





function scpGetModelJson ()
{
	setEnvVar;        
	#improve to read out dynamically
        curl -X GET "http://$NODE_IP:$CMM_PORT/cm/api/v1.1/schemas/ericsson-scp" | jq 
}

function bsfGetModelJson ()
{
	#improve to read out dynamically
        curl -X GET "http://$NODE_IP:$CMM_PORT/cm/api/v1.1/schemas/ericsson-bsf" | jq 
}


function scpGetModel ()
{
	#improve to read out dynamically 
	if hash sshpass 2>/dev/null 
	then
	        cat $GIT_PATH/5g_test_ci/jcat-esc/common-extensions/src/main/resources/yang/hello.txt $GIT_PATH/5g_test_ci/jcat-esc/common-extensions/src/main/resources/yang/schemasGetSCP.txt | sshpass -p rootroot ssh -t -p $YP_PORT expert@$NODE_IP -s netconf
	else
	        cat $GIT_PATH/5g_test_ci/jcat-esc/common-extensions/src/main/resources/yang/hello.txt $GIT_PATH/5g_test_ci/jcat-esc/common-extensions/src/main/resources/yang/schemasGetSCP.txt | ssh -t -p $YP_PORT expert@$NODE_IP -s netconf
	fi

}



function scpGetConfigJson ()
{
	#improve to read out dynamically
	curl -X GET "http://$NODE_IP:$CMM_PORT/cm/api/v1.1/configurations/$CMM_CONFIG_NAME" -H "Content-Type: application/json"  | jq 
}

function scpGetConfig ()
{
	#improve to read out dynamically
	if hash sshpass 2>/dev/null 
	then
	        cat $GIT_PATH/5g_test_ci/jcat-esc/common-extensions/src/main/resources/yang/hello.txt $GIT_PATH/5g_test_ci/jcat-esc/common-extensions/src/main/resources/yang/configGetRunningScpFull.txt | sshpass -p scpscp  ssh -t -p $YP_PORT scp-admin@$NODE_IP -s netconf
	else
	        cat $GIT_PATH/5g_test_ci/jcat-esc/common-extensions/src/main/resources/yang/hello.txt $GIT_PATH/5g_test_ci/jcat-esc/common-extensions/src/main/resources/yang/configGetRunningScpFull.txt | ssh -t -p $YP_PORT scp-admin@$NODE_IP -s netconf
	fi


}


function scGetConfigName ()
{
        curl -X GET "http://$NODE_IP:$CMM_PORT/cm/api/v1.1/configurations" | jq
}


function scpPostConfig ()
{
        #improve: add file with configuration to be loaded as input parameter
        echo "Password for user 'scp-admin'"
        echo "Make sure that you have update the PORTS correctly in the yaml config file!!!!"
	if hash sshpass 2>/dev/null 
	then
	        cat $GIT_PATH/5g_proto/esc/scp/sample_config.netconf | sshpass -p scpscp ssh -t -p $YP_PORT scp-admin@$NODE_IP -s netconf | grep -i ok
	else
	        cat $GIT_PATH/5g_proto/esc/scp/sample_config.netconf | ssh -t -p $YP_PORT scp-admin@$NODE_IP -s netconf | grep -i ok
	fi


}

function scpPostConfigNrf ()
{
        #improve: add file with configuration to be loaded as input parameter
        echo "Password for user 'scp-admin'"
        echo "Make sure that you have update the PORTS correctly in the yaml config file!!!!"
	if hash sshpass 2>/dev/null 
	then
	        cat $GIT_PATH/5g_proto/esc/scp/sample_config_nrf.netconf | sshpass -p scpscp ssh -t -p $YP_PORT scp-admin@$NODE_IP -s netconf | grep -i ok
	else
	        cat $GIT_PATH/5g_proto/esc/scp/sample_config_nrf.netconf | ssh -t -p $YP_PORT scp-admin@$NODE_IP -s netconf | grep -i ok
	fi

}

function scpPostConfigPoolNrf ()
{
        #improve: add file with configuration to be loaded as input parameter
        echo "Password for user 'scp-admin'"
        echo "Make sure that you have update the PORTS correctly in the yaml config file!!!!"
	if hash sshpass 2>/dev/null 
	then
	        cat $GIT_PATH/5g_proto/esc/scp/sample_config_3_pools_nrf.netconf | sshpass -p scpscp ssh -t -p $YP_PORT scp-admin@$NODE_IP -s netconf | grep -i ok
	else
	        cat $GIT_PATH/5g_proto/esc/scp/sample_config_3_pools_nrf.netconf | ssh -t -p $YP_PORT scp-admin@$NODE_IP -s netconf | grep -i ok
	fi

}

function scpPostConfigRCC ()
{
        #improve: add file with configuration to be loaded as input parameter
        echo "Password for user 'scp-admin'"
        echo "Make sure that you have update the PORTS correctly in the yaml config file!!!!"
	if hash sshpass 2>/dev/null 
	then
	        cat $GIT_PATH/5g_proto/esc/scp/sample_config_rcc_nrf.netconf | sshpass -p scpscp ssh -t -p $YP_PORT scp-admin@$NODE_IP -s netconf | grep -i ok
	else
	        cat $GIT_PATH/5g_proto/esc/scp/sample_config_rcc_nrf.netconf | ssh -t -p $YP_PORT scp-admin@$NODE_IP -s netconf | grep -i ok
	fi

}

function scpDeleteConfigJson ()
{
        curl -X DELETE "http://$NODE_IP:$CMM_PORT/cm/api/v1.1/configurations/ericsson-scp" | jq
        echo "Deleted configuration ericsson-scp"
}

function bsfDeleteConfigJson ()
{
        curl -X DELETE "http://$NODE_IP:$CMM_PORT/cm/api/v1.1/configurations/ericsson-bsf" | jq
        echo "Deleted configuration ericsson-bsf"
}

function bsfDeleteSchemaJson ()
{
        curl -X DELETE "http://$NODE_IP:$CMM_PORT/cm/api/v1.1/schemas/ericsson-bsf" | jq
        echo "Deleted Schema ericsson-bsf"
}

function scpDeleteConfig ()
{
        echo "Password for user 'expert'"
	if hash sshpass 2>/dev/null 
	then
	        cat $GIT_PATH/5g_test_ci/jcat-esc/common-extensions/src/main/resources/yang/hello.txt $GIT_PATH/5g_test_ci/jcat-esc/common-extensions/src/main/resources/yang/scpConfigEraseRunning.txt | sshpass -p rootroot ssh -t -p $YP_PORT expert@$NODE_IP -s netconf | grep -i ok
	else
	        cat $GIT_PATH/5g_test_ci/jcat-esc/common-extensions/src/main/resources/yang/hello.txt $GIT_PATH/5g_test_ci/jcat-esc/common-extensions/src/main/resources/yang/scpConfigEraseRunning.txt | ssh -t -p $YP_PORT expert@$NODE_IP -s netconf | grep -i ok
	fi

        echo 
        echo "Deleted running and candidate configuration (SCP)"
}

function scpRunNetconf ()
{
        echo "Password for user 'expert'"
	if hash sshpass 2>/dev/null 
	then
	        cat $GIT_PATH/5g_test_ci/jcat-esc/common-extensions/src/main/resources/yang/hello.txt $GIT_PATH/5g_test_ci/jcat-esc/common-extensions/src/main/resources/yang/$1 | sshpass -p rootroot ssh -t -p $YP_PORT expert@$NODE_IP -s netconf | grep -i ok
	else
	        cat $GIT_PATH/5g_test_ci/jcat-esc/common-extensions/src/main/resources/yang/hello.txt $GIT_PATH/5g_test_ci/jcat-esc/common-extensions/src/main/resources/yang/$1 | ssh -t -p $YP_PORT expert@$NODE_IP -s netconf | grep -i ok
	fi

        echo 
        echo "Running Netconf script $1"
}


##### SEPP #######

function seppPostConfig ()
{
        echo "Password for user 'sepp-admin'"
	if hash sshpass 2>/dev/null 
	then
	        cat $GIT_PATH/5g_proto/esc/sepp/sample_poc_config.netconf | sshpass -p seppsepp ssh -t -p $YP_PORT sepp-admin@$NODE_IP -s netconf | grep -i ok
	else
	        cat $GIT_PATH/5g_proto/esc/sepp/sample_poc_config.netconf | ssh -t -p $YP_PORT sepp-admin@$NODE_IP -s netconf | grep -i ok
	fi

}



function seppGetConfig ()
{
        echo "Password for user 'sepp-admin'"
	if hash sshpass 2>/dev/null 
	then
	        cat $GIT_PATH/5g_test_ci/jcat-esc/common-extensions/src/main/resources/yang/hello.txt $GIT_PATH/5g_test_ci/jcat-esc/common-extensions/src/main/resources/yang/configGetRunningSeppFull.txt | sshpass -p seppsepp ssh -t -p $YP_PORT sepp-admin@$NODE_IP -s netconf
	else
	        cat $GIT_PATH/5g_test_ci/jcat-esc/common-extensions/src/main/resources/yang/hello.txt $GIT_PATH/5g_test_ci/jcat-esc/common-extensions/src/main/resources/yang/configGetRunningSeppFull.txt | ssh -t -p $YP_PORT sepp-admin@$NODE_IP -s netconf
	fi

}




############## SEPP DEMO ##############

function seppResetSeppsims ()
{
        echo "Resetting 5 SEPPsim PODs"
        k delete pods -l app=eric-seppsim-p1 &
        k delete pods -l app=eric-seppsim-p2 &
        k delete pods -l app=eric-seppsim-p3 &
        k delete pods -l app=eric-seppsim-p4 &
        k delete pods -l app=eric-seppsim-p5 &

}

function seppRemoveSeppsimDisturbances ()
{
        echo "Removing all disturbances on SEPPsim PODs"
        export MONITOR_PORT=$(kubectl get --namespace $NAMESPACE -o jsonpath="{.spec.ports[0].nodePort}" services eric-sc-monitor)
        curl -X PUT "http://$NODE_IP:$MONITOR_PORT/monitor/api/v0/commands?target=eric-seppsim-p&command=config" -d '{"ownDomain":"region1.amf.5gc.mnc073.mcc262.3gppnetwork.org"}' | jq .results[].config.api.nudmUeContextManagement
}


function seppAddSeppsimDisturbance ()
{
        echo "Adding disturbances (503) on SEPPsim POD eric-seppsim-p$1"
        export MONITOR_PORT=$(kubectl get --namespace $NAMESPACE -o jsonpath="{.spec.ports[0].nodePort}" services eric-sc-monitor)
        curl -X PUT "http://$NODE_IP:$MONITOR_PORT/monitor/api/v0/commands?target=eric-seppsim-p$1&command=config" -d '{"ownDomain":"region1.udm.5gc.mnc073.mcc262.3gppnetwork.org","api":{"nudmUeContextManagement":{"disturbances":[{"status":503}]}}}'  | jq .results[0].config.api.nudmUeContextManagement

}



function seppSetTLS ()
{
        echo "Setting TLS port in VPN config for SEPP"
        export SEPP_TLS_PORT=$(kubectl get --namespace $NAMESPACE -o jsonpath="{.spec.ports[1].nodePort}" services eric-sepp-worker)
        sed -i -E "s/(<port operation=\"merge\">).*(<\/port>)/\1$SEPP_TLS_PORT\2/" ~/demo/sepp_demo/sepp_set_vpn_to_tls.xml
        cat  ~/demo/sepp_demo/sepp_set_vpn_to_tls.xml | sshpass -p seppsepp ssh -t -p $YP_PORT sepp-admin@$NODE_IP -s netconf | grep rpc-reply
}


function seppUnsetTLS ()
{
        echo "Removing TLS port in VPN config for SEPP"
        export SEPP_PORT=$(kubectl get --namespace $NAMESPACE -o jsonpath="{.spec.ports[0].nodePort}" services eric-sepp-worker)
        sed -i -E "s/(<port operation=\"merge\">).*(<\/port>)/\1$SEPP_PORT\2/" ~/demo/sepp_demo/sepp_set_vpn_to_non_tls.xml
        cat ~/demo/sepp_demo/sepp_set_vpn_to_non_tls.xml | sshpass -p seppsepp ssh -t -p $YP_PORT sepp-admin@$NODE_IP -s netconf | grep rpc-reply
}


function seppShowEnvoyUDMClusters ()
{
	export SEPP_POD=$(kubectl get pods --namespace $NAMESPACE -l "app=eric-sepp-worker" -o jsonpath="{.items[0].metadata.name}")
	k exec -it $SEPP_POD  -c eric-sepp-worker --  curl -X POST "http://localhost:9901/clusters"  | grep -i udm_

}


############################

logNRF_1() {
    k logs -f "$1" | grep -A 2 -m 1 'RegisterNFInstance request' | sed 's/handleRegisterNfInstance      /\n/;T;D' | sed 's/|Received UpdateNFInstance request.//;T;D'| sed 's/c.e.utilities.common.Registry:87  |lambda$start$0                /\n/;T;D'| GREP_COLOR='1;34' grep --color=always -A 1 -E 'RegisterNFInstance|Updating'
}

logBSF() {
    k logs -f eric-bsf-manager-0  | grep -A 1 -m 1 'com.ericsson.esc.nrf.Nrf:247 |' | sed 's/|prepare                       /\n/;T;D' | sed 's/da$nfInstanceRegisterCreate$18/\n/;T;D' | sed 's/da$nfInstanceRegisterCreate$19/\n/;T;D' | GREP_COLOR='1;34' grep -A 1 -B 1 --color=always -E 'Registering'
} 


showRegisteredNFs() {
    curl -X GET "http://$NODE_IP:$NRFSIM_PORT/nnrf-nfm/v1/nf-instances" | jq
}


showRegisteredNF() {
    #export NRF_instance=$(curl -X GET "http://$NODE_IP:$NRFSIM_PORT/nnrf-nfm/v1/nf-instances" | jq -r  _links.item.[1].href)
    #curl -X GET "http://$NODE_IP:$NRFSIM_PORT/nnrf-nfm/v1/nf-instances/"$1"" | jq | GREP_COLOR='1;34' grep --color=always -E 'nfInstanceId|nfType|nfStatus'
    curl -X GET "http://$NODE_IP:$NRFSIM_PORT/nnrf-nfm/v1/nf-instances/"$1"" | jq
}


logNRF() {
    export NRF_POD=$(kubectl get pods --namespace 5g-bsf-demo -l "app=eric-nrfsim" -o jsonpath="{.items[0].metadata.name}")
    logNRF_1  $NRF_POD
}


###############################################################################################################################
#Switchbetween minikube/hahn011################################################################################################
###############################################################################################################################


function s2m()
{
  if [ $KUBE_HOST = "minikube" ]; then
        echo "Already set to Minikube environment, doing it anyway"
  else
        echo "Switching to Minikube setup..."
  fi

  ln -sf /home/$USER/.5g.devenv.profile_minikube /home/$USER/.5g.devenv.profile 
  source /home/$USER/.5g.devenv.profile;
  ln -sf $GIT_PATH/5g_proto/devenv/CONFIG_minikube $GIT_PATH/5g_proto/devenv/CONFIG
  \cp -f /home/$USER/.kube/minikube.config /home/$USER/.kube/config

  export NAMESPACE=5g-bsf-$USER
  export KUBE_NAMESPACE=$NAMESPACE
  export NODE_IP=$MINIKUBE_NODE_IP
#  export CMM_PORT=5003
  export CMM_PORT=$(kubectl get --namespace $NAMESPACE -o jsonpath="{.spec.ports[0].nodePort}" services eric-cm-mediator)
  export CMM_CONFIG_NAME=ericsson-scp
  #export CMM_CONFIG_NAME=$(curl -s -X GET "http://$NODE_IP:$CMM_PORT/cm/api/v1.1/configurations" | jq -r .[0].name)
  export YP_PORT=$(kubectl get --namespace $NAMESPACE -o jsonpath="{.spec.ports[0].nodePort}" services eric-cm-yang-provider-ipv4)  # take the port mapped to 830
  export CLI_PORT=$(kubectl get --namespace $NAMESPACE -o jsonpath="{.spec.ports[1].nodePort}" services eric-cm-yang-provider-ipv4)  # take the port mapped to 22
  export SCP_PORT=$(kubectl get --namespace $NAMESPACE -o jsonpath="{.spec.ports[0].nodePort}" services eric-scp-worker)
  export SCP_POD=$(kubectl get pods --namespace $NAMESPACE -l "app=eric-scp-worker" -o jsonpath="{.items[0].metadata.name}")
  export PM_PORT=$(kubectl get --namespace $NAMESPACE -o jsonpath="{.spec.ports[0].nodePort}" services eric-pm-server)
  export CHFSIM_PORT=$(kubectl get --namespace $NAMESPACE -o jsonpath="{.spec.ports[0].nodePort}" services eric-chfsim-1)
  export CHFSIM_PORT1=$(kubectl get --namespace $NAMESPACE -o jsonpath="{.spec.ports[0].nodePort}" services eric-chfsim-1)
  export CHFSIM_PORT2=$(kubectl get --namespace $NAMESPACE -o jsonpath="{.spec.ports[0].nodePort}" services eric-chfsim-2)
  export CHFSIM_PORT3=$(kubectl get --namespace $NAMESPACE -o jsonpath="{.spec.ports[0].nodePort}" services eric-chfsim-3)
  export NRFSIM_PORT=$(kubectl get --namespace $NAMESPACE -o jsonpath="{.spec.ports[0].nodePort}" services eric-nrfsim)
  export MONITOR_PORT=$(kubectl get --namespace $NAMESPACE  -o jsonpath="{.spec.ports[0].nodePort}" services eric-sc-monitor)
  export CNOM_PORT=$(kubectl get --namespace $NAMESPACE -o jsonpath="{.spec.ports[0].nodePort}" services eric-tm-ingress-controller-cr-ipv4)
  echo "Done."
}


function s2h11()
{
  if [ $KUBE_HOST = "hahn011" ]; then
        echo "Already set to Hahn011 environment, doing it anyway"
  else
        echo "Switching to Hahn011 setup..."
  fi

  ln -sf /home/$USER/.5g.devenv.profile_hahn011 /home/$USER/.5g.devenv.profile 
  source /home/$USER/.5g.devenv.profile;
  ln -sf $GIT_PATH/5g_proto/devenv/CONFIG_hahn011 $GIT_PATH/5g_proto/devenv/CONFIG
  \cp -f /home/$USER/.kube/hahn011.config /home/$USER/.kube/config

  setEnvVar;        

}

function s2h047()
{
  if [ $KUBE_HOST = "hall047" ]; then
        echo "Already set to Hall047 environment, doing it anyway"
  else
        echo "Switching to Hall047 setup..."
  fi

  ln -sf /home/$USER/.5g.devenv.profile_hall047 /home/$USER/.5g.devenv.profile 
  source /home/$USER/.5g.devenv.profile;
  ln -sf $GIT_PATH/5g_proto/devenv/CONFIG_hall047 $GIT_PATH/5g_proto/devenv/CONFIG
  \cp -f /home/$USER/.kube/hall047.config /home/$USER/.kube/config

  setEnvVar;        

}

function s2k008() #shared cluster
{
  if [ $KUBE_HOST = "kohn008" ]; then
        echo "Already set to Kohn008 environment, doing it anyway"
  else
        echo "Switching to Kohn008 setup..."
  fi

  ln -sf /home/$USER/.5g.devenv.profile_kohn008 /home/$USER/.5g.devenv.profile 
  source /home/$USER/.5g.devenv.profile;
  ln -sf $GIT_PATH/5g_proto/devenv/CONFIG_kohn008 $GIT_PATH/5g_proto/devenv/CONFIG
  \cp -f /home/$USER/.kube/kohn008.config /home/$USER/.kube/config

  setEnvVar;        

}


function s2h085() #shared cluster
{
  if [ $KUBE_HOST = "hart085" ]; then
        echo "Already set to Hart085 environment, doing it anyway"
  else
        echo "Switching to Hart085 setup..."
  fi

  ln -sf /home/$USER/.5g.devenv.profile_hart085 /home/$USER/.5g.devenv.profile
  source /home/$USER/.5g.devenv.profile;
  ln -sf $GIT_PATH/5g_proto/devenv/CONFIG_hart085 $GIT_PATH/5g_proto/devenv/CONFIG
  \cp -f /home/$USER/.kube/hart085.config /home/$USER/.kube/config

  setEnvVar;

}



function s2k009() #Innovation Day cluster
{
  if [ $KUBE_HOST = "kohn009" ]; then
        echo "Already set to Kohn009 environment, doing it anyway"
  else
        echo "Switching to Kohn009 setup..."
  fi

  ln -sf /home/$USER/.5g.devenv.profile_kohn009 /home/$USER/.5g.devenv.profile
  source /home/$USER/.5g.devenv.profile;
  ln -sf $GIT_PATH/5g_proto/devenv/CONFIG_kohn009 $GIT_PATH/5g_proto/devenv/CONFIG
  \cp -f /home/$USER/.kube/kohn009.config /home/$USER/.kube/config

  export NAMESPACE=sc
  alias k='kubectl -n $NAMESPACE'
  setEnvVarNoNamespace;
}


function s2h()
{
  s2h047
}


function s2h121()  #demo cluster
{
  if [ $KUBE_HOST = "hahn121" ]; then
        echo "Already set to Hahn121 environment, doing it anyway"
  else
        echo "Switching to Hahn121 setup..."
  fi

  ln -sf /home/$USER/.5g.devenv.profile_hahn121 /home/$USER/.5g.devenv.profile 
  source /home/$USER/.5g.devenv.profile;
  ln -sf $GIT_PATH/5g_proto/devenv/CONFIG_hahn121 $GIT_PATH/5g_proto/devenv/CONFIG
  \cp -f /home/$USER/.kube/hahn121.config /home/$USER/.kube/config

  setEnvVar;        
}

function s2h121two()  #demo cluster
{
  if [ $KUBE_HOST = "hahn121" ]; then
        echo "Already set to Hahn121 (eedtwo) environment, doing it anyway"
  else
        echo "Switching to Hahn121 (eedtwo) setup..."
  fi

  ln -sf /home/$USER/.5g.devenv.profile_hahn121 /home/$USER/.5g.devenv.profile 
  source /home/$USER/.5g.devenv.profile;
  ln -sf $GIT_PATH/5g_proto/devenv/CONFIG_hahn121 $GIT_PATH/5g_proto/devenv/CONFIG
  \cp -f /home/$USER/.kube/hahn121eedtwo.config /home/$USER/.kube/config

  export NAMESPACE=5g-bsf-eedtwo

  setEnvVarNoNamespace;

}

function s2h122()  #DJs cluster
{
  if [ $KUBE_HOST = "hahn122" ]; then
        echo "Already set to Hahn122 environment, doing it anyway"
  else
        echo "Switching to Hahn122 setup..."
  fi

  ln -sf /home/$USER/.5g.devenv.profile_hahn122 /home/$USER/.5g.devenv.profile 
  source /home/$USER/.5g.devenv.profile;
  ln -sf $GIT_PATH/5g_proto/devenv/CONFIG_hahn122 $GIT_PATH/5g_proto/devenv/CONFIG
  \cp -f /home/$USER/.kube/hahn122.config /home/$USER/.kube/config

  setEnvVar;        
}


function s2h30()
{
  if [ $KUBE_HOST = "hahn030" ]; then
        echo "Already set to Hahn030 environment, doing it anyway"
  else
        echo "Switching to Hahn030 setup..."
  fi

  ln -sf /home/$USER/.5g.devenv.profile_hahn030 /home/$USER/.5g.devenv.profile 
  source /home/$USER/.5g.devenv.profile;
  ln -sf $GIT_PATH/5g_proto/devenv/CONFIG_hahn030 $GIT_PATH/5g_proto/devenv/CONFIG
  \cp -f /home/$USER/.kube/hahn030.config /home/$USER/.kube/config

  setEnvVar;        
}

function s2h35()
{
  if [ $KUBE_HOST = "hahn035" ]; then
        echo "Already set to Hahn035 environment, doing it anyway"
  else
        echo "Switching to Hahn035 setup..."
  fi

  ln -sf /home/$USER/.5g.devenv.profile_hahn035 /home/$USER/.5g.devenv.profile 
  source /home/$USER/.5g.devenv.profile;
  ln -sf $GIT_PATH/5g_proto/devenv/CONFIG_hahn035 $GIT_PATH/5g_proto/devenv/CONFIG
  \cp -f /home/$USER/.kube/hahn035.config /home/$USER/.kube/config

  setEnvVar;        
}




function s2t()
{
  if [ $KUBE_HOST = "todd044" ]; then
        echo "Already set to Todd044 environment, doing it anyway"
  else
        echo "Switching to Todd044 setup..."
  fi

  ln -sf /home/$USER/.5g.devenv.profile_todd044 /home/$USER/.5g.devenv.profile 
  source /home/$USER/.5g.devenv.profile;
  ln -sf $GIT_PATH/5g_proto/devenv/CONFIG_todd044 $GIT_PATH/5g_proto/devenv/CONFIG
  \cp -f /home/$USER/.kube/todd044.config /home/$USER/.kube/config

  setEnvVar;        
}

function s2n1031()
{
  if [ $KUBE_HOST = "n1031" ]; then
        echo "Already set to n1031 (dsc5429) environment, doing it anyway"
  else
        echo "Switching to n1031 (dsc5429) setup..."
  fi

  ln -sf /home/$USER/.5g.devenv.profile_n1031 /home/$USER/.5g.devenv.profile
  source /home/$USER/.5g.devenv.profile;
  ln -sf $GIT_PATH/5g_proto/devenv/CONFIG_n1031 $GIT_PATH/5g_proto/devenv/CONFIG
  \cp -f /home/$USER/.kube/n1031.config /home/$USER/.kube/config

#  export NAMESPACE=scpipv6
  #export KUBE_NAMESPACE=$NAMESPACE

  setEnvVar;     
  export NODE_IP=2001:1b70:6220:ad60:ffff:ffff:0:103
}


function s2n1032()
{
  if [ $KUBE_HOST = "n1032" ]; then
        echo "Already set to n1032 (dsc5672) environment, doing it anyway"
  else
        echo "Switching to n1032 (dsc5672) setup..."
  fi

  ln -sf /home/$USER/.5g.devenv.profile_n1032 /home/$USER/.5g.devenv.profile
  source /home/$USER/.5g.devenv.profile;
  ln -sf $GIT_PATH/5g_proto/devenv/CONFIG_n1032 $GIT_PATH/5g_proto/devenv/CONFIG
  \cp -f /home/$USER/.kube/n1032.config /home/$USER/.kube/config


#  export NAMESPACE=scpipv6
  #export KUBE_NAMESPACE=$NAMESPACE

  setEnvVar;     
  export NODE_IP=2001:1b70:6220:ad61:ffff:ffff:0:108
}
 
 
function s2eevee()
{
  if [ $KUBE_HOST = "n1032" ]; then
        echo "Already set to Eevee environment, doing it anyway"
  else
        echo "Switching to Eevee setup..."
  fi

  ln -sf /home/$USER/.5g.devenv.profile_n1032 /home/$USER/.5g.devenv.profile
  source /home/$USER/.5g.devenv.profile;
  ln -sf $GIT_PATH/5g_proto/devenv/CONFIG_n1032 $GIT_PATH/5g_proto/devenv/CONFIG
  \cp -f /home/$USER/.kube/eevee.config /home/$USER/.kube/config

  export NAMESPACE=eiffelesc
  setEnvVarNoNamespace;     
  export NODE_IP=10.221.169.151
}
