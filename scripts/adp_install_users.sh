#!/bin/bash

function check_for_cm_mediator_schemas(){

    echo "----Checking CMM for ietf-system schema existence";
    for (( index=1; index<=${retries}; index++ )); do
        if [ $index -eq ${retries} ]; then
            echo "ERROR: Unable to identify ietf-system schema in CMM after $((retries * delay)) minutes";
            return 1;
        fi
        sleep ${delay}
        cmmPodArray=($(kubectl get pod --namespace ${KUBE_NAMESPACE} --kubeconfig $KUBE_CONFIG -l "app=eric-cm-mediator" | grep "mediator" | grep -v "notif" | grep -v "key" | awk '{print $1}'))
        output=$(kubectl exec -ti pod/${cmmPodArray[0]} --namespace $KUBE_NAMESPACE --kubeconfig $KUBE_CONFIG -c eric-cm-mediator -- curl --tlsv1.2 -s -S -X GET https://localhost:${CMM_PORT}/cm/api/v1/schemas --cert /run/secrets/eric-cm-mediator-tls-client-secret/clicert.pem --cert-type PEM --key /run/secrets/eric-cm-mediator-tls-client-secret/cliprivkey.pem --key-type PEM --cacert /run/secrets/eric-sec-sip-tls-trusted-root-cert/ca.crt)

        if [ $? -ne 0 ]; then
            echo "ERROR: Cannot fetch CMM schemas. retrying...";
            continue
        fi
        echo "${output}"
        if [[ $output == *"ietf-system"* ]]; then
            echo "  ietf-system schema identified in CMM.";
            break
        fi
    done

}

function check_for_cm_mediator_local_auth_admin(){


    echo "----Checking CMM for local-authentication-admin in ietf-netconf-acm configuration existence";
    for (( index=1; index<=${retries}; index++ )); do
        if [ $index -eq ${retries} ]; then
            echo "ERROR: Unable to verify local-authentication-admin in ietf-netconf-acm configuration after $((retries * delay)) minutes";
            return 1;
        fi
        sleep ${delay}
        cmmPodArray=($(kubectl get pod --namespace ${KUBE_NAMESPACE} --kubeconfig $KUBE_CONFIG -l "app=eric-cm-mediator" | grep "mediator" | grep -v "notif" | grep -v "key" | awk '{print $1}'))
        output=$(kubectl exec -ti pod/${cmmPodArray[0]} --namespace $KUBE_NAMESPACE --kubeconfig $KUBE_CONFIG -c eric-cm-mediator -- curl --tlsv1.2 -s -S -X GET https://localhost:${CMM_PORT}/cm/api/v1/configurations/ietf-netconf-acm --cert /run/secrets/eric-cm-mediator-tls-client-secret/clicert.pem --cert-type PEM --key /run/secrets/eric-cm-mediator-tls-client-secret/cliprivkey.pem --key-type PEM --cacert /run/secrets/eric-sec-sip-tls-trusted-root-cert/ca.crt)

        if [ $? -ne 0 ]; then
            echo "WARNING: Cannot fetch CMM ietf-netconf-acm configuration. retrying...";
            continue
        fi
        if [[ $output == *"\"local-authentication-admin\""* ]]; then
            echo "  local-authentication-admin found in ietf-netconf-acm configuration.";
            break
        fi
        echo "${output}"
        echo "WARNING: local-authentication-admin not present in ietf-netconf-acm configuration. retrying...";
    done

}

function check_for_namespace(){

    if [ ! -z ../.bob/var.namespace ];
    then
        export KUBE_NAMESPACE=`cat ../.bob/var.namespace`
    else
        export KUBE_NAMESPACE="5g-bsf-${USER}"
    fi

}

function check_for_config(){

    if [ ! -z ../.bob/${KUBE_HOST}.admin.conf ];
    then
        export KUBE_CONFIG="../.bob/${KUBE_HOST}.admin.conf"
    else
        export KUBE_CONFIG="/home/${USER}/.kube/config"
    fi
}

function add_users()
{
	echo
#   changing the used port and IP Address to verify if the Tunelling to CMYP is needed:
#   CMYP_LOCAL_PORT changed to CMYP_PORT, localhost to CMYP_IP
    echo "----Adding users via CMYP NETCONF i/f"
    /usr/bin/expect <( cat << EOS

    set timeout 180
    spawn ssh -o LogLevel=quiet -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -t -p ${CMYP_PORT} ${DAY0_ADMIN}@${CMYP_IP_EXTERNAL} -s netconf
    expect "Password:"
    send "rootroot\r"

    sleep 10
    expect "]]>]]>"
    send [ exec cat ../scripts/default_users/hello.netconf ]
    expect "]]>]]>"
    send [ exec cat ../scripts/default_users/users.netconf ]
    expect "]]>]]>"
    send [ exec cat ../scripts/default_users/close.netconf ]
    expect "]]>]]>"

EOS
    )
}

function verify_user
{
    echo
    echo
    echo "--Verify user1 addition"
    sleep ${delay}
    cmmPodArray=($(kubectl get pod --namespace ${KUBE_NAMESPACE} --kubeconfig $KUBE_CONFIG -l "app=eric-cm-mediator" | grep "mediator" | grep -v "notif" | grep -v "key" | awk '{print $1}'))
    output=$(kubectl exec -ti pod/${cmmPodArray[0]} --namespace $KUBE_NAMESPACE --kubeconfig $KUBE_CONFIG -c eric-cm-mediator -- curl --tlsv1.2 -s -S -X GET https://localhost:${CMM_PORT}/cm/api/v1/configurations/ietf-system --cert /run/secrets/eric-cm-mediator-tls-client-secret/clicert.pem --cert-type PEM --key /run/secrets/eric-cm-mediator-tls-client-secret/cliprivkey.pem --key-type PEM --cacert /run/secrets/eric-sec-sip-tls-trusted-root-cert/ca.crt)

    result=$?
    if [ $result -ne 0 ]; then
        echo "ERROR: Cannot fetch CMM ietf-system configuration.";
        return 1;
    else
        if [[ $output == *"\"name\": \"user1\""* ]]; then
            echo "    user1 found in ietf-system configuration.";
            return 0;
        else
            echo "${output}"
            echo "ERROR: user1 not present in ietf-system configuration.";
            return 1;
        fi
    fi
}

function extractCmypValues() {

    ###################
    # Extract CMYP_IP #
    ###################
    export SVC_BASE_NAME="eric-cm-yang-provider";

    for SUFFIX in $SUFFIXES;
    do

        export VSUF=$(echo $SUFFIX | tr 'a-z,-' 'A-Z,_' );
        export SVC_NAME="${SVC_BASE_NAME}${SUFFIX}";
        export SVC_AVAIL=`kubectl get svc ${SVC_NAME} --kubeconfig ${KUBE_CONFIG} --namespace ${KUBE_NAMESPACE}`;

        if [ "${SVC_AVAIL}" != "" ];
        then

            echo "Service ${SVC_NAME} is available. Now checking the Service Type.";

            ################################
            # Determining the Service Type #
            ################################

            export SVC_TYPE=`kubectl get svc ${SVC_NAME} --kubeconfig ${KUBE_CONFIG} --namespace ${KUBE_NAMESPACE} -o 'jsonpath={.spec.type}'`;

            if [ "${SVC_TYPE}" == "LoadBalancer" ];
            then

                echo "Identified Service Type: ${SVC_TYPE}";

                export CMYP_IP$VSUF=`kubectl get svc ${SVC_NAME} --kubeconfig ${KUBE_CONFIG} --namespace ${KUBE_NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[*].ip}'`;
                export CMYP_PORT$VSUF=`kubectl get svc ${SVC_NAME} --kubeconfig ${KUBE_CONFIG} --namespace ${KUBE_NAMESPACE} -o jsonpath="{.spec.ports[?(@.name == \"netconf\")].port}"`;

                TEMP_CMYP_IP="CMYP_IP$VSUF";
                TEMP_CMYP_PORT="CMYP_PORT$VSUF"
                if [ "${!TEMP_CMYP_IP}" == "" ];
                then
                    echo "LoadBalancer seems to be in state <Pending> due to port conflicts on 80 or 443. Falling back to combination of <NodeIP>:<NodePort>.";
                    export CMYP_IP$VSUF=`kubectl get nodes --kubeconfig ${KUBE_CONFIG} --namespace ${KUBE_NAMESPACE} -l '!node-role.kubernetes.io/master' -o jsonpath='{.items[0].status.addresses[?(@.type == "InternalIP" )].address}'`;
                    export CMYP_PORT$VSUF=`kubectl get svc ${SVC_NAME} --kubeconfig ${KUBE_CONFIG} --namespace ${KUBE_NAMESPACE} -o jsonpath="{.spec.ports[?(@.name == \"netconf\")].nodePort}"`;
                    TEMP_CMYP_IP="CMYP_IP$VSUF";
                    TEMP_CMYP_PORT="CMYP_PORT$VSUF"
                fi;


                echo " Loadbalancer associated IP: ${!TEMP_CMYP_IP} ";
                echo " Loadbalancer associated Port: ${!TEMP_CMYP_PORT} ";

            elif [ "${SVC_TYPE}" == "NodePort" ];
            then
                echo "Identified Service Type: ${SVC_TYPE}";

                export CMYP_IP$VSUF=`kubectl get nodes --kubeconfig ${KUBE_CONFIG} --namespace ${KUBE_NAMESPACE} -l '!node-roleEscTestPropertiesrnetes.io/master' -o jsonpath='{.items[0].status.addresses[?(@.type == "InternalIP" )].address}'`;
                export CMYP_PORT$VSUF=`kubectl get svc ${SVC_NAME} --kubeconfig ${KUBE_CONFIG} --namespace ${KUBE_NAMESPACE} -o jsonpath="{.spec.ports[?(@.name == \"netconf\")].nodePort}"`;

                TEMP_CMYP_IP="CMYP_IP$VSUF";
                TEMP_CMYP_PORT="CMYP_PORT$VSUF"

                echo " Loadbalancer associated IP: ${!TEMP_CMYP_IP} ";
                echo " Loadbalancer associated Port: ${!TEMP_CMYP_PORT} ";

            else

                    echo "Error: The service seems not to be accessible from outside the cluster."; exit 1;
            fi;
        fi;

    done;

}

function setCmypParameterValues() {

    if [ "${CMYP_IP_IPV6}" != "" ] || [ "${CMYP_IP_IPV4}" != "" ];
    then
        export STACK="2"; #DUAL STACK
    else
        echo "${CMYP_IP_EXTERNAL}" | grep -e "^[a-f,0-9]\{1,4\}:\{1,\}\([a-f,0-9]\{0,\}:\{0,\}\)\{1,\}[a-f,0-9]\{1,4\}$";

        if (( $? ));
        then
            export STACK="0"; #SINGLE STACK IPV4
        else
            export STACK="1"; #SINGLE STACK IPv6
        fi;
    fi;

    if [ "$IP_VERSION" == "6" ];
    then
        if [ "${STACK}" == "2" ];
        then
            export CMYP_IP="${CMYP_IP_IPV6}";
            export CMYP_PORT="${CMYP_PORT_IPV6}";
        elif [ "${STACK}" == "1" ];
        then
            echo "This is an IPv6 deployment on a single stack. Using Address for CM YANG PROVIDER without suffix \"-ipv6\"."
        else
            echo "ERROR: The parameter IP_VERSION was set to 6, but no IPv6 address could be identified. Exiting"; exit 1;
        fi;
    else
        if [ ! -z ${CMYP_IP_IPV4} ];
        then
            export CMYP_IP="${CMYP_IP_IPV4}";
            export CMYP_PORT="${CMYP_PORT_IPV4}";
        else
            echo "Using the values found via standard service eric-cm-yang-provider-external";
            export CMYP_IP="${CMYP_IP_EXTERNAL}";
            export CMYP_PORT="${CMYP_PORT_EXTERNAL}";
        fi;

    fi;

    echo "Using Following values for CMYP IP (${CMYP_IP}) and CMYP PORT (${CMYP_PORT})";

}

function get_replicaset_desired_replicas() {

    #When the pods are just starting, the value can be empty and cause a warning in check_replicas
    LABEL=$1
	result=$(kubectl --namespace $KUBE_NAMESPACE --kubeconfig $KUBE_CONFIG get replicaset -l $LABEL -o=jsonpath='{.items[*].spec.replicas}')
	if [ "$result" == "" ]
	then
	   echo "0"
	else
	   echo $result
	fi

}

function get_replicaset_ready_replicas() {

    #When the pods are just starting, the value can be empty and cause a warning in check_replicas
    LABEL=$1
	result=$(kubectl --namespace $KUBE_NAMESPACE --kubeconfig $KUBE_CONFIG get replicaset -l $LABEL -o=jsonpath='{.items[*].status.readyReplicas}')
	if [ "$result" == "" ]
	then
	   echo "0"
	else
	   echo $result
	fi

}

function check_replicas() {

   LABEL=$1
   DESIRED_REPLICAS=$(get_replicaset_desired_replicas $1)
   READY_REPLICAS=$(get_replicaset_ready_replicas $1)

   if [[ $DESIRED_REPLICAS -eq $READY_REPLICAS && $DESIRED_REPLICAS -ne 0 ]]
   then
       echo "Pod(s) ready."
	   #This sleep prevents the script from stalling several minutes at the ssh password prompt
	   sleep 5
       return 0
   else
       echo -n "."
       return 1
   fi

}

function stop_script() {

     echo "ERROR: $1 is taking too long to start, check your deployment"
     exit

}

# ******************* MAIN *****************

DAY0_ADMIN=admin
NETCONF_PORT=830
CMM_PORT=5004
CMM_LOCAL_PORT=8666
CMYP_PORT=830
CMYP_LOCAL_PORT=8667
export SUFFIXES="-external -ipv4 -ipv6";

DELAY_RETRY_ADD_USER=6

delay=3 # seconds

retryPeriod=1 #

retries=$(( $retryPeriod * 60 / $delay ))


check_for_namespace
check_for_config

echo "Create default SC users for namespace ${KUBE_NAMESPACE}"

extractCmypValues
setCmypParameterValues

SECONDS=0
echo "Waiting for cm-yang-provider pod(s) to be ready"
echo "Checking interval: ${DELAY_RETRY_ADD_USER}s"
while ! check_replicas "app=eric-cm-yang-provider"
do
    sleep $DELAY_RETRY_ADD_USER
	((SECONDS < 20*60)) || stop_script "eric-cm-yang-provider"
done

echo "Adding users to cm-yang-provider"
add_users

sleep 5

while ! verify_user
do
    sleep ${DELAY_RETRY_ADD_USER}
    add_users
done

SECONDS=0
echo "Waiting for cm-mediator pod(s) to be ready"
echo "Checking interval: ${DELAY_RETRY_ADD_USER}s"
while ! check_replicas "app=eric-cm-mediator"
do
    sleep $DELAY_RETRY_ADD_USER
	((SECONDS < 20*60)) || stop_script "eric-cm-mediator"
done

check_for_cm_mediator_schemas
check_for_cm_mediator_local_auth_admin
