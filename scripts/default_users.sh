#!/bin/bash

function add_users
{
        echo
    echo "----Adding users via CMYP NETCONF i/f"
    /usr/bin/expect <( cat << EOS

    set timeout 180
    spawn ssh -o LogLevel=quiet -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -t -p ${CMYP_LOCAL_PORT} ${DAY0_ADMIN}@localhost -s netconf
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

function cmyp_ip_family_ext
(
    echo "Todo"
)

function verify_user
{
    echo
    echo
    echo "--Verify user1 addition"
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

# this is a workaround only and must be removed once the CMYP issue is fixed (ADPPRG-37014)
function zong_cmyp
{
        if [[ $SKIP_CMYP_ZONG == "true" ]]; then
                exitq 0;
        fi

        echo
        echo "--Restart eric-cm-yang-provider ***ZONG***"
        echo "--this is a workaround only and must be removed once the CMYP issue is fixed (ADPPRG-37014)"
        sleep ${delay}
        cmyp_pod_name=$(kubectl get pod --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE -l app=eric-cm-yang-provider -o jsonpath="{.items[0].metadata.name}")
        kubectl get pod ${cmyp_pod_name} --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE -o yaml | kubectl replace --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE --force -f -
        sleep 120
}

if [ ! -z ../.bob/var.namespace ];
then
        export KUBE_NAMESPACE=`cat ../.bob/var.namespace`
else
        export KUBE_NAMESPACE="5g-bsf-${USER}"
fi

if [ ! -z ../.bob/${KUBE_HOST}.admin.conf ];
then
        export KUBE_CONFIG="../.bob/${KUBE_HOST}.admin.conf"
else
        export KUBE_CONFIG="/home/${USER}/.kube/config"
fi

# deprecated check moved in makefile
if [[ $SKIP_DEFAULT_USERS == "true" ]]; then
	echo "WARNING: Only day-0 admin user exists in your current deployment!"
	exit 0;
fi

echo "Create default SC users for namespace ${KUBE_NAMESPACE}"

## in case of an IPv6 cluster select the LB instead of the k8s worker IP
#if kubectl get svc --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE | grep eric-cm-yang-provider-ipv6;
if (kubectl get svc --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE | grep eric-cm-yang-provider-ipv6) && (kubectl get svc --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE | grep eric-cm-yang-provider-ipv4);
then
    echo "The node is Dual Stack, using k8s worker IP"
    export MY_IP=$(kubectl get nodes --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE -o jsonpath="{.items[0].status.addresses[0].address}")
    export MY_PORT=$(kubectl get services eric-cm-yang-provider-ipv4 --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE -o jsonpath="{.spec.ports[0].nodePort}" )
elif kubectl get svc --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE | grep eric-cm-yang-provider-ipv6;
then
    echo "The node is IPv6, fetch ipaddress and port from loadbalancer"
    export MY_IP=$(kubectl get svc --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE eric-cm-yang-provider-ipv6 -o jsonpath="{.status.loadBalancer.ingress[0].ip}")
    export MY_PORT=$(kubectl get svc --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE eric-cm-yang-provider-ipv6 -o jsonpath="{.spec.ports[0].port}")
else
    echo "The node is not ipv6"
    export MY_IP=$(kubectl get nodes --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE -o jsonpath="{.items[0].status.addresses[0].address}")
    export MY_PORT=$(kubectl get services eric-cm-yang-provider-external --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE -o jsonpath="{.spec.ports[0].nodePort}" )
fi;

DAY0_ADMIN=admin
NETCONF_PORT=830
CMM_PORT=5004
CMYP_PORT=830
CMYP_LOCAL_PORT=8667

# Sleep delay in seconds
delay=$1
if [ -n "${delay}" ] && [ "${delay}" -eq "${delay}" ] 2>/dev/null; then
        echo "Setting delay to ${delay}"
else
        delay=5
        echo "Using default delay ${delay}"
fi

# Max period for retries in minutes
retryPeriod=10

# Retries
retries=$(( $retryPeriod * 60 / $delay ))

if [[ "${KUBE_NAMESPACE}" = *"eiffelesc"* ]]; then
        sleep 30
        echo "--Skip checks of deployed UM related ADP services status [CI]"
else
        echo "--Checking deployed UM related ADP services status";

        echo "----Checking CM-YANG-PROVIDER status";
        for (( index=1; index<=${retries}; index++ )); do
                if [ $index -eq ${retries} ]; then
                        echo "ERROR: Unable to verify CM-YANG-PROVIDER status after $((retries * delay)) minutes";
                        return 1;
                fi
                sleep ${delay}

                # Check status of pod
                getPodsStatus=($(kubectl --kubeconfig $KUBE_CONFIG --namespace ${KUBE_NAMESPACE} get pods --field-selector=status.phase=Running | grep eric-cm-yang-provider))
                result=$?

                if [ $result -ne 0 ]; then
                        echo "WARNING: Cannot fetch pods with status Running. retrying...";
                        continue
                fi
                if [[ "${getPodsStatus[0]}" != "eric-cm-yang-provider"* ]]; then
                        echo "WARNING: CM-YANG-PROVIDER not in status RUNNING. retrying...";
                        continue
                fi

                # Check status of containers
                echo "    CM-YANG-PROVIDER container status "${getPodsStatus[1]}" after "${getPodsStatus[4]}
                if [[ ${getPodsStatus[1]} == "${getPodsStatus[1]%/*}/${getPodsStatus[1]#*/}" ]] && [[ "${getPodsStatus[1]%/*}" == "${getPodsStatus[1]#*/}" ]]; then break; fi
        done

        # Check that AUM status is RUNNING
        echo "----Checking AUM status";
        for (( index=1; index<=${retries}; index++ )); do
                if [ $index -eq ${retries} ]; then
                        echo "ERROR: Unable to verify AUM status after $((retries * delay)) minutes";
                        return 1;
                fi
                sleep ${delay}
                getPodsStatus=($(kubectl --kubeconfig $KUBE_CONFIG --namespace ${KUBE_NAMESPACE} get pods --field-selector=status.phase=Running | grep eric-sec-admin-user-management))
                result=$?

                if [ $result -ne 0 ]; then
                        echo "WARNING: Cannot fetch pods with status Running. retrying...";
                        continue
                fi
                if [[ "${getPodsStatus[0]}" != "eric-sec-admin-user-management"* ]]; then
                        echo "WARNING: AUM not in status RUNNING. retrying...";
                        continue
                fi

                # Check status of containers
                echo "    AUM container status ${getPodsStatus[1]} after ${getPodsStatus[4]}"
                if [[ ${getPodsStatus[1]} == "${getPodsStatus[1]%/*}/${getPodsStatus[1]#*/}" ]] && [[ "${getPodsStatus[1]%/*}" == "${getPodsStatus[1]#*/}" ]]; then break; fi
        done


        # Check LDAP is RUNNING and all containers are READY

        ## --- Check if all pods are running ---##
        echo "----Checking LDAP status";
        expectedLdapPods=$(kubectl --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE get sts eric-sec-ldap-server -o jsonpath="{.spec.replicas}");
        for (( trialPods=1; trialPods<=${retries}; trialPods++ )); do

                sleep ${delay}

                readyLdapPods=$(kubectl --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE get sts eric-sec-ldap-server -o jsonpath="{.status.readyReplicas}");
                if [ -z $readyLdapPods ]; then
                        readyLdapPods=0;
                fi

                if [ ${readyLdapPods} -eq ${expectedLdapPods} ]; then
                        echo "INFO: All pods are running!"
                else
                        echo "WARNING: Only ${readyLdapPods}/${expectedLdapPods} pods are running. Waiting..."
                        continue;
                fi

                ## --- Check if all containers are ready ---##

                for (( podNum=0; podNum<${readyLdapPods}; podNum++ )); do

                        # containers: an array with elements the container names of the pod
                        # containersNumber: the number of containers in the pod by getting the length of the array
                        containers=($(kubectl --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE get pods eric-sec-ldap-server-$podNum -o jsonpath="{.status.containerStatuses[*].name}"));
                        containersNumber=${#containers[@]};

                        for (( trialContainers=1; trialContainers<=${retries}; trialContainers++ )); do
                                sleep ${delay}
                                for (( containerIndex=0; containerIndex<${containersNumber}; containerIndex++ )); do
                                        isContainerReady=$(kubectl --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE get pods eric-sec-ldap-server-$podNum -o jsonpath="{.status.containerStatuses[$containerIndex].ready}");
                                        if [ ! ${isContainerReady} ]; then
                                                break;
                                        fi
                                done
                                if [ $containerIndex -eq $containersNumber ]; then
                                        echo "INFO: All LDAP-$podNum containers are ready!"
                                        break;
                                else
                                        echo "WARNING: ${containers[$containerIndex]} container is not ready yet. Waiting..."
                                        continue;
                                fi
                        done

                        if [ ${trialContainers} -gt ${retries} ]; then
                                echo "ERROR: Unable to verify LDAP-$podNum containers' status";
                                return 1;
                        fi

                done

                if [ $podNum -eq $readyLdapPods ]; then
                        break;
                fi

        done

        if [ $trialPods -gt $retries ]; then
                echo "ERROR: Unable to verify LDAP status" # after $((retries * delay)) minutes
                return 1;
        else
                echo "INFO: LDAP status verified!"
        fi

fi

echo
# Port forward to access CMM
echo "--Creating tunnel to CMM..."

cmmPodArray=($(kubectl get pod --namespace ${KUBE_NAMESPACE} --kubeconfig $KUBE_CONFIG -l "app=eric-cm-mediator" | grep "mediator" | grep -v "notif" | grep -v "key" | awk '{print $1}'))
output=$(kubectl exec -ti pod/${cmmPodArray[0]} --namespace $KUBE_NAMESPACE --kubeconfig $KUBE_CONFIG -c eric-cm-mediator -- curl --tlsv1.2 -s -S -X GET https://localhost:${CMM_PORT}/cm/api/v1/schemas --cert /run/secrets/eric-cm-mediator-tls-client-secret/clicert.pem --cert-type PEM --key /run/secrets/eric-cm-mediator-tls-client-secret/cliprivkey.pem --key-type PEM --cacert /run/secrets/eric-sec-sip-tls-trusted-root-cert/ca.crt)
echo "----Checking CMM for ietf-system schema existence";
for (( index=1; index<=${retries}; index++ )); do
        if [ $index -eq ${retries} ]; then
               echo "ERROR: Unable to identify ietf-system schema in CMM after $((retries * delay)) minutes";
               return 1;
        fi
        sleep ${delay}
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

echo "----Checking CMM for local-authentication-admin in ietf-netconf-acm configuration existence";
for (( index=1; index<=${retries}; index++ )); do
        if [ $index -eq ${retries} ]; then
                echo "ERROR: Unable to verify local-authentication-admin in ietf-netconf-acm configuration after $((retries * delay)) minutes";
                return 1;
        fi
        sleep ${delay}
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

#Temporary workaround for DND-55562
#echo "----Checking CMM for DAY-0 admin user existence in ietf-system configuration";
#for (( index=1; index<=${retries}; index++ )); do
#        if [ $index -eq ${retries} ]; then
#                echo "ERROR: Unable to verify DAY-0 admin user in ietf-system configuration after $((retries * delay)) minutes";
#                return 1;
#        fi
#        sleep ${delay}
#        output=$(kubectl exec -ti pod/${cmmPodArray[0]} --namespace $KUBE_NAMESPACE --kubeconfig $KUBE_CONFIG -c eric-cm-mediator -- curl --tlsv1.2 -s -S -X GET https://localhost:${CMM_PORT}/cm/api/v1/configurations/ietf-system --cert /run/secrets/eric-cm-mediator-tls-client-secret/clicert.pem --cert-type PEM --key /run/secrets/eric-cm-mediator-tls-client-secret/cliprivkey.pem --key-type PEM --cacert /run/secrets/eric-sec-sip-tls-trusted-root-cert/ca.crt)
#        if [ $? -ne 0 ]; then
#                echo "WARNING: Cannot fetch CMM ietf-system configuration. retrying...";
#                continue
#        fi
#        if [[ $output == *"\"name\": \"admin\""* ]]; then
#                echo "    Day-0 admin user found in ietf-system configuration.";
#                break
#        fi
#        echo "${output}"
#        echo "WARNING: Day-0 admin user not presesnt in ietf-system configuration. retrying...";
#done

#Restart CMYP *** ZONG ***
# this is a workaround only and must be removed once the CMYP issue is fixed (ADPPRG-37014)
#zong_cmyp

echo
# Port forward to access CMYP
echo "--Creating tunnel to CMYP..."
#if kubectl get svc --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE | grep eric-cm-yang-provider-ipv6;
if (kubectl get svc --kubeconfig $KUBE_CONFIG  --namespace $KUBE_NAMESPACE | grep eric-cm-yang-provider-ipv6) && (kubectl get svc --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE | grep eric-cm-yang-provider-ipv4);
then
    echo "Node is Dual Stack, using IPv4"
    kubectl --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE port-forward service/eric-cm-yang-provider-ipv4 $CMYP_LOCAL_PORT:$CMYP_PORT &
    CMYP_PF_PID=$!
elif kubectl get svc --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE | grep eric-cm-yang-provider-ipv6;
then
    echo "The node is IPv6"
    kubectl --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE port-forward service/eric-cm-yang-provider-ipv6 $CMYP_LOCAL_PORT:$CMYP_PORT &
    CMYP_PF_PID=$!
else
    echo "The node is not ipv6"
    kubectl --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE port-forward service/eric-cm-yang-provider-external $CMYP_LOCAL_PORT:$CMYP_PORT &
    CMYP_PF_PID=$!
fi;

sleep 20

add_users

if verify_user
then
    echo ">>>>>>>>> Validation of user successful"
else
    # Try adding users again after a delay
    echo ">>>>>>>>> Validation of user was unsuccessful, trying again after a delay"
    sleep 120

    add_users

    if verify_user
    then
        echo ">>>>>>>>> Validation of user successful"
    else
        # Try adding users again after a delay
        echo ">>>>>>>>> Validation of user was unsuccessful"
    fi
fi

# Cleanup
#kill -9 $CMM_PF_PID
kill -9 $CMYP_PF_PID
