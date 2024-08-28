#!/bin/bash -x

export DEFAULT_CONFIG_PATH="/home/eiffelesc/.kube";
export CONFIG_PATH=$1;
[[ -z ${CONFIG_PATH} ]] && [[ -d ${DEFAULT_CONFIG_PATH} ]] && export CONFIG_PATH="${DEFAULT_CONFIG_PATH}";
[[ -z ${KUBECONFIG} ]] && export KUBECONFIG="${CONFIG_PATH}/${KUBE_HOST}.config";
[[ -z ${NAMESPACE} ]] && echo "ERROR: variable NAMESPACE not set/defined. Exiting" && exit 1 ;
[[ -z ${BSF} && -z ${CSA} && -z ${SCP} && -z ${SEPP} ]] && echo "ERROR: NFs not set" && exit 1;

export NFS="";
export SUFFIXES="-external -ipv4 -ipv6";
export NR_CONFIGS="";

checkNeededNfs(){
    [[ "$BSF" == true ]] && NFS="$NFS bsf"; 
    [[ "$CSA" == true ]] && NFS="$NFS csa"; 
    [[ "$SCP" == true ]] && NFS="$NFS scp"; 
    [[ "$SEPP" == true ]] && [[ "$IP_VERSION" != "6" ]] && NFS="$NFS sepp"; 
    echo "NFS=$NFS";
};

extractCmValues(){
    export SVC_BASE_NAME="eric-cm-yang-provider";
    for SUFFIX in $SUFFIXES; do
        export VSUF=$(echo $SUFFIX | tr 'a-z,-' 'A-Z,_' );
        export SVC_NAME="${SVC_BASE_NAME}${SUFFIX}";
        export SVC_AVAIL=`kubectl get svc ${SVC_NAME} --kubeconfig $KUBECONFIG --namespace ${NAMESPACE}`;
        if [ "${SVC_AVAIL}" != "" ]; then
            export SVC_TYPE=`kubectl get svc ${SVC_NAME} --kubeconfig $KUBECONFIG --namespace ${NAMESPACE} -o 'jsonpath={.spec.type}'`;
            if [ "${SVC_TYPE}" == "LoadBalancer" ]; then
                export CMM_IP$VSUF=`kubectl get svc ${SVC_NAME} --kubeconfig $KUBECONFIG --namespace ${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[*].ip}'`;
                export CMM_PORT$VSUF=`kubectl get svc ${SVC_NAME} --kubeconfig $KUBECONFIG --namespace ${NAMESPACE} -o jsonpath="{.spec.ports[?(@.name == \"netconf\")].port}"`;
                TEMP_CMM_IP="CMM_IP$VSUF";
                if [ "${!TEMP_CMM_IP}" == "" ]; then
                    echo "LoadBalancer seems to be in state <Pending> due to port conflicts on 80 or 443. Falling back to combination of <NodeIP>:<NodePort>.";
                    export CMM_IP$VSUF=`kubectl get nodes --kubeconfig $KUBECONFIG --namespace ${NAMESPACE} -l '!node-role.kubernetes.io/master' -o jsonpath='{.items[0].status.addresses[?(@.type == "InternalIP" )].address}'`;
                    export CMM_PORT$VSUF=`kubectl get svc ${SVC_NAME} --kubeconfig $KUBECONFIG --namespace ${NAMESPACE} -o jsonpath="{.spec.ports[?(@.name == \"netconf\")].nodePort}"`;
                fi
            elif [ "${SVC_TYPE}" == "NodePort" ]; then
                export CMM_IP$VSUF=`kubectl get nodes --kubeconfig $KUBECONFIG --namespace ${NAMESPACE} -l '!node-roleEscTestPropertiesrnetes.io/master' -o jsonpath='{.items[0].status.addresses[?(@.type == "InternalIP" )].address}'`;
                export CMM_PORT$VSUF=`kubectl get svc ${SVC_NAME} --kubeconfig $KUBECONFIG --namespace ${NAMESPACE} -o jsonpath="{.spec.ports[?(@.name == \"netconf\")].nodePort}"`;
            else
                echo "Error: The service seems not to be accessible from outside the cluster.";
                exit 1;
            fi
        fi
    done
}

extractNfValues(){
    export NF_NAME=$1;
    export NF_NAME_CAP=$(echo ${NF_NAME} | tr 'a-z' 'A-Z' );
    export SVC_BASE_NAME="eric-${NF_NAME}-worker";

    for SUFFIX in $SUFFIXES; do
        export VSUF=$(echo $SUFFIX | tr 'a-z,-' 'A-Z,_' );
        export SVC_NAME="${SVC_BASE_NAME}${SUFFIX}";
        export SVC_AVAIL=`kubectl get svc ${SVC_NAME} --kubeconfig $KUBECONFIG --namespace ${NAMESPACE}`;
        if [ "${SVC_AVAIL}" != "" ]; then
            export SVC_TYPE=`kubectl get svc ${SVC_NAME} --kubeconfig $KUBECONFIG --namespace ${NAMESPACE} -o 'jsonpath={.spec.type}'`;
            if [ "${SVC_TYPE}" == "LoadBalancer" ]; then
                export ${NF_NAME_CAP}_WRKR_IP$VSUF=`kubectl get svc ${SVC_NAME} --kubeconfig $KUBECONFIG --namespace ${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[*].ip}'`;
                export ${NF_NAME_CAP}_WRKR_PORT$(echo $VSUF)=`kubectl get svc ${SVC_NAME} --kubeconfig $KUBECONFIG --namespace ${NAMESPACE} -o jsonpath="{.spec.ports[?(@.name == \"unencrypted-port\")].port}"`;
                export ${NF_NAME_CAP}_WRKR_TLS_PORT$(echo $VSUF)=`kubectl get svc ${SVC_NAME} --kubeconfig $KUBECONFIG --namespace ${NAMESPACE} -o jsonpath="{.spec.ports[?(@.name == \"encrypted-port\")].port}"`;
                TEMP_IP="${NF_NAME_CAP}_WRKR_IP${VSUF}"
                if [ "${!TEMP_IP}" == "" ]; then
                    echo "LoadBalancer seems to be in state <Pending> due to port conflicts on 80 or 443. Falling back to combination of <NodeIP>:<NodePort>.";
                    export ${NF_NAME_CAP}_WRKR_IP$VSUF=`kubectl get nodes --kubeconfig $KUBECONFIG --namespace ${NAMESPACE} -l '!node-role.kubernetes.io/master' -o jsonpath='{.items[0].status.addresses[?(@.type == "InternalIP" )].address}'`;
                    export ${NF_NAME_CAP}_WRKR_PORT$VSUF=`kubectl get svc ${SVC_NAME} --kubeconfig $KUBECONFIG --namespace ${NAMESPACE} -o jsonpath="{.spec.ports[?(@.name == \"unencrypted-port\")].nodePort}"`;
                    export ${NF_NAME_CAP}_WRKR_TLS_PORT$VSUF=`kubectl get svc ${SVC_NAME} --kubeconfig $KUBECONFIG --namespace ${NAMESPACE} -o jsonpath="{.spec.ports[?(@.name == \"encrypted-port\")].nodePort}"`;
                fi
            elif [ "${SVC_TYPE}" == "NodePort" ]; then
                export ${NF_NAME_CAP}_WRKR_IP$VSUF=`kubectl get nodes --kubeconfig $KUBECONFIG --namespace ${NAMESPACE} -l '!node-role.kubernetes.io/master' -o jsonpath='{.items[0].status.addresses[?(@.type == "InternalIP" )].address}'`;
                export ${NF_NAME_CAP}_WRKR_PORT$VSUF=`kubectl get svc ${SVC_NAME} --kubeconfig $KUBECONFIG --namespace ${NAMESPACE} -o jsonpath="{.spec.ports[?(@.name == \"unencrypted-port\")].nodePort}"`;
                export ${NF_NAME_CAP}_WRKR_TLS_PORT$VSUF=`kubectl get svc ${SVC_NAME} --kubeconfig $KUBECONFIG --namespace ${NAMESPACE} -o jsonpath="{.spec.ports[?(@.name == \"encrypted-port\")].nodePort}"`;
            else
                echo "Error: The service seems not to be accessible from outside the cluster.";
                exit 1;
            fi
        fi
        TEMP_IP="${NF_NAME_CAP}_WRKR_IP${VSUF}"
        TEMP_PORT="${NF_NAME_CAP}_WRKR_PORT${VSUF}"
        TEMP_PORT_TLS="${NF_NAME_CAP}_WRKR_TLS_PORT${VSUF}"  
        echo "Extracted NF values:";
        echo IP$VSUF for $NF      : ${!TEMP_IP};
        echo PORT$VSUF for $NF    : ${!TEMP_PORT};
        echo TLS_PORT$VSUF for $NF: ${!TEMP_PORT_TLS};
    done
}

setCmParameterValues(){
    if [ "${CMM_IP_IPV6}" != "" ] || [ "${CMM_IP_IPV4}" != "" ]; then
        # dual stack
        export STACK="2";
    else
        echo "${CMM_IP}" | grep -e "^[a-f,0-9]\{1,4\}:\{1,\}\([a-f,0-9]\{0,\}:\{0,\}\)\{1,\}[a-f,0-9]\{1,4\}$";
        if (( $? )); then
            # single stack ipv4
            export STACK="0";
        else
            # signle stack ipv6
            export STACK="1";
        fi
    fi
    if [ "$IP_VERSION" == "6" ]; then
        if [ "${STACK}" == "2" ]; then
            export CMM_IP="${CMM_IP_IPV6}";
            export CMM_PORT="${CMM_PORT_IPV6}";
        elif [ "${STACK}" == "1" ]; then
            echo "This is an IPv6 deployment on a single stack. Using Address for CM YANG PROVIDER without suffix \"-ipv6\".";
        else
            echo "ERROR: The parameter IP_VERSION was set to 6, but no IPv6 address could be identified. Exiting";
            exit 1;
        fi
    else
        if [ ! -z ${CMM_IP_IPV4} ]; then
            export CMM_IP="${CMM_IP_IPV4}";
            export CMM_PORT="${CMM_PORT_IPV4}";
        else
            echo "Using the values found via standard service eric-cm-yang-provider-external";
            export CMM_IP="${CMM_IP_EXTERNAL}";
            export CMM_PORT="${CMM_PORT_EXTERNAL}";
        fi
    fi
    echo "Using Following values for CMYP IP (${CMM_IP}) and CMYP PORT (${CMM_PORT})";
}

setNfParameterValues(){
    export NF_NAME=$1;
    export NF_NAME_CAP=$(echo ${NF_NAME} | tr 'a-z' 'A-Z' );
    export NF_FLAG="${NF_NAME_CAP}";
    export NF_IP=${NF_FLAG}_WRKR_IP;
    export NF_IP4=${NF_FLAG}_WRKR_IP_IPV4;
    export NF_IP6=${NF_FLAG}_WRKR_IP_IPV6;
    if [ "${!NF_IP6}" != "" ] || [ "${!NF_IP4}" != "" ]; then
        # dual stack
        export STACK="2";
    else
        echo "${!NF_IP}" | grep -e "^[a-f,0-9]\{1,4\}:\{1,\}\([a-f,0-9]\{0,\}:\{0,\}\)\{1,\}[a-f,0-9]\{1,4\}$";
        if (( $? )); then
            # single stack ipv4
            export STACK="0";
        else
            # single stack ipv6
            export STACK="1";
        fi
    fi
    if [ "$IP_VERSION" == "6" ]; then
        if [ "${!NF_FLAG}" == "true" ]; then 
            echo "NF_FLAG is equal to: ${!NF_FLAG}";
            if [ "${STACK}" == "2" ]; then
                NF_TEMP_IP=${NF_FLAG}_WRKR_IP_IPV6;
                NF_TEMP_PORT=${NF_FLAG}_WRKR_PORT_IPV6;
                NF_TEMP_PORT_TLS=${NF_FLAG}_WRKR_TLS_PORT_IPV6;
                export ${NF_NAME_CAP}_WRKR_IP="${!NF_TEMP_IP}";
                export ${NF_NAME_CAP}_WRKR_PORT="${!NF_TEMP_PORT}";
                export ${NF_NAME_CAP}_WRKR_TLS_PORT="${!NF_TEMP_PORT_TLS}";
            elif [ "${STACK}" == "1" ]; then
                echo "This is an IPv6 deployment on a single stack. Using Address for NF without suffix \"-ipv6\".";
            else
                echo "ERROR: The parameter IP_VERSION was set to 6, but no IPv6 address could be identified. Exiting";
                exit 1;
            fi
        fi
    else
        if [ "${!NF_FLAG}" == "true" ]; then
            if [ "${STACK}" == "2" ]; then 
                echo "NF_FLAG is equal to: ${!NF_FLAG} ";
                NF_TEMP_IP=${NF_FLAG}_WRKR_IP_IPV4;
                NF_TEMP_PORT=${NF_FLAG}_WRKR_PORT_IPV4;
                NF_TEMP_PORT_TLS=${NF_FLAG}_WRKR_TLS_PORT_IPV4;
                export ${NF_NAME_CAP}_WRKR_IP="${!NF_TEMP_IP}";
                export ${NF_NAME_CAP}_WRKR_PORT="${!NF_TEMP_PORT}";
                export ${NF_NAME_CAP}_WRKR_TLS_PORT="${!NF_TEMP_PORT_TLS}";
            else
                echo "Using the values found via standard service eric-cm-yang-provider";
            fi
        fi
    fi
    TEMP_IP=${NF_NAME_CAP}_WRKR_IP;
    TEMP_PORT=${NF_NAME_CAP}_WRKR_PORT;
    TEMP_PORT_TLS=${NF_NAME_CAP}_WRKR_TLS_PORT;
    echo "Values to be used:" 
    echo "${NF_NAME} IP: ${!TEMP_IP}";
    echo "${NF_NAME} PORT: ${!TEMP_PORT}";
    echo "${NF_NAME} TLS PORT: ${!TEMP_PORT_TLS}";
}

createTempConfig(){
    [[ "$BSF" == "true" ]] && export BSF_DEFAULTCONFIG="./Jenkins/PipeConfig/sample_bsf_func_and_diameter.netconf" && export INIT_DB_ACTION="./Jenkins/PipeConfig/action_bsf_init_db.netconf";
    [[ "$BSF" == "true" ]] && [[ "$BSF_DIAMETER" == "false" ]] && export BSF_DEFAULTCONFIG="./Jenkins/PipeConfig/sample_bsf_func_without_diameter.netconf" && export INIT_DB_ACTION="./Jenkins/PipeConfig/action_bsf_init_db.netconf";

    if [ "$IP_VERSION" == "6" ];
    then
        [[ "$SCP" == "true" ]] && export SCP_DEFAULTCONFIG="./Jenkins/PipeConfig/sample_config_rcc_nrf_scp_ipv6.netconf";
    else        
        [[ "$SCP" == "true" ]] && export SCP_DEFAULTCONFIG="./Jenkins/PipeConfig/sample_config_scp.netconf";
        [[ "$SEPP" == "true" ]] && export SEPP_DEFAULTCONFIG="./Jenkins/PipeConfig/sample_sepp_poc_config.netconf";
        [[ "$PVTB" == "true" ]] && export PVTB_DEFAULTCONFIG="./Jenkins/PipeConfig/sample_broker_config.netconf";
    fi
    [[ ! -z "${BSF_DEFAULTCONFIG}" ]] && cat ${BSF_DEFAULTCONFIG} >> .bob/tempConfig.netconf;
    [[ ! -z "${INIT_DB_ACTION}" ]] && cat ${INIT_DB_ACTION} >> .bob/tempConfig.netconf;
    [[ ! -z "${SCP_DEFAULTCONFIG}" ]] && cat ${SCP_DEFAULTCONFIG} >> .bob/tempConfig.netconf;
    [[ ! -z "${SEPP_DEFAULTCONFIG}" ]] && cat ${SEPP_DEFAULTCONFIG} >> .bob/tempConfig.netconf;
    [[ ! -z "${PVTB_DEFAULTCONFIG}" ]] && cat ${PVTB_DEFAULTCONFIG} >> .bob/tempConfig.netconf;
    cat .bob/tempConfig.netconf;
    NR_CONFIGS=$(( `grep -c "/rpc" .bob/tempConfig.netconf` - `grep -c "<initialize" .bob/tempConfig.netconf` ));
}

replaceValuesConfig(){
    export NF_NAME=$1;
    export NF_NAME_CAP=$(echo ${NF_NAME} | tr 'a-z' 'A-Z' );
    NF_IP=${NF_NAME_CAP}_WRKR_IP;
    NF_PORT=${NF_NAME_CAP}_WRKR_PORT;
    NF_PORT_TLS=${NF_NAME_CAP}_WRKR_TLS_PORT;
    sed -i "s/<${NF_NAME_CAP}_WRKR_IP>/${!NF_IP}/g" .bob/tempConfig.netconf
    sed -i "s/<${NF_NAME_CAP}_WRKR_PORT>/${!NF_PORT}/g" .bob/tempConfig.netconf
    sed -i "s/<${NF_NAME_CAP}_WRKR_TLS_PORT>/${!NF_PORT_TLS}/g" .bob/tempConfig.netconf
}

loadConfig(){
    echo "Current working directory:";
    pwd;
    /usr/bin/expect <( cat << EOS
exp_internal 0
set timeout 120
spawn ssh -t -p $CMM_PORT sc-admin@$CMM_IP -s netconf
expect {
    "continue connecting (yes/no" {
        send "yes\r"
        expect "*Password: "
        send "scsc\r"
    }
    "*Password: " {
        send "scsc\r"
    }
}
sleep 10
expect "]]>]]>"
send [ exec cat ./Jenkins/PipeConfig/hello.netconf ]
set foundOk 0
set wait_time 2
set timeout 20
send_user "\rThis is the value of foundOK: \$foundOk ##### Zing ####"
while { [expr \$foundOk < $NR_CONFIGS ] } {
    send_user "\rNow in the while loop ###### Zong ####### "
    send_user "\rThe value of foundOk is: \$foundOk"
    if { [expr \$foundOk == 0 ] } {
        send [ exec cat .bob/tempConfig.netconf ]
    }
    sleep 8
    expect {
        "<ok/></rpc-reply>]]>]]>" {
            set foundOk "[expr \$foundOk + 1]"
            set wait_time 2
        }
        "</rpc-error></rpc-reply>]]>]]>" {
            puts "Received an error while loading the configuration. Exiting"
        }
    }
    send_user " The waiting time is: \$wait_time "
    sleep \$wait_time
    set wait_time "[expr 2 * \$wait_time]"
    if { \$wait_time > 128 } {
        send_user "\rWaiting Time exceed 4 min. The deploy will be aborted. Please check the ADP logs."
        send [ exec cat ./Jenkins/PipeConfig/close.netconf ]
        expect "]]>]]>"
        exit 1
    }
}
send [ exec cat ./Jenkins/PipeConfig/close.netconf ]
expect "]]>]]>"
EOS
)
    
    #rm -rf .bob/tempConfig.netconf
}

createGetConfig(){
    [[ "$BSF" == "true" ]] && export BSF_GETCONFIG="./Jenkins/PipeConfig/getConfBsf.netconf";
    [[ "$SCP" == "true" ]] && export SCP_GETCONFIG="./Jenkins/PipeConfig/getConfScp.netconf";
    [[ "$SEPP" == "true" ]] && export SEPP_GETCONFIG="./Jenkins/PipeConfig/getConfSepp.netconf";
    [[ ! -z "${BSF_GETCONFIG}" ]] && cat ${BSF_GETCONFIG} >> .bob/tempGetConfig.netconf;
    [[ ! -z "${SCP_GETCONFIG}" ]] && cat ${SCP_GETCONFIG} >> .bob/tempGetConfig.netconf;
    [[ ! -z "${SEPP_GETCONFIG}" ]] && cat ${SEPP_GETCONFIG} >> .bob/tempGetConfig.netconf;
    echo "Temporary GET netconf configuration:";
    cat .bob/tempGetConfig.netconf;
}

checkConfig(){
    /usr/bin/expect <( cat << EOS
exp_internal 0
set timeout 120
spawn ssh -t -p $CMM_PORT sc-admin@$CMM_IP -s netconf
expect {
    "continue connecting (yes/no" {
        send "yes\r"
        expect "*Password: "
        send "scsc\r"
    }
    "*Password: " {
        send "scsc\r"
    }
}
sleep 10
expect "]]>]]>"
send [ exec cat ./Jenkins/PipeConfig/hello.netconf ]
set foundOk "false"
set wait_time 2
set timeout 20
while { [string compare \$foundOk "false"] == 0 } {
    send [ exec cat .bob/tempGetConfig.netconf ]
    sleep 8
    expect {
        -re "\/(scp|bsf|sepp)-function><\/data><\/rpc-reply>]]>]]>" {
            set foundOk "true"
        }
        "<data></data></rpc-reply>]]>]]>" {
            puts "No config found. Aborting."
        }
    }
    sleep \$wait_time
    set wait_time "[expr 2 * \$wait_time]"
    if { \$wait_time > 128 } {
        send_user "Waiting Time exceed 4 min. The deploy will be aborted. Please check the ADP logs."
        send [ exec cat ./Jenkins/PipeConfig/close.netconf ]
        expect "]]>]]>"
        exit 1
    }
}
send [ exec cat ./Jenkins/PipeConfig/close.netconf ]
expect "]]>]]>"
EOS
)
    
    #rm -rf .bob/tempGetConfig.netconf
}

main(){
    checkNeededNfs;
    extractCmValues;
    for NF in $NFS; do
        extractNfValues $NF;
    done;
    setCmParameterValues;
    for NF in $NFS; do
        setNfParameterValues $NF;
    done;
    echo http://$CMM_IP:$CMM_PORT
    echo BSF-WorkerPorts:$BSF_WRKR_IP:$BSF_WRKR_PORT/$BSF_WRKR_TLS_PORT
    echo CSA-WorkerPorts:$CSA_WRKR_IP:$CSA_WRKR_PORT/$CSA_WRKR_TLS_PORT
    echo SCP-WorkerPorts:$SCP_WRKR_IP:$SCP_WRKR_PORT/$SCP_WRKR_TLS_PORT
    echo SEPP-WorkerPorts:$SEPP_WRKR_IP:$SEPP_WRKR_PORT/$SEPP_WRKR_TLS_PORT
    ssh-keygen -R [$CMM_IP]:$CMM_PORT
    sleep 3
    createTempConfig;
    for NF in $NFS;
    do
        replaceValuesConfig $NF;
    done;
    loadConfig;
    createGetConfig;
    sleep 9;
    checkConfig;
}

main;
