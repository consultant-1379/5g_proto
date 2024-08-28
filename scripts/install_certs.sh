#!/bin/bash 

ROOT_DIR=`dirname $0`
CERT_DIR=$ROOT_DIR/certificates/certm_worker
KEYS_DIR=$CERT_DIR/keys
RESOURCES_DIR=$CERT_DIR/resources
ROOTCA_DIR=$KEYS_DIR/rootca

BSF_MANAGER_INSTALL_RPC=$CERT_DIR/resources/bsfmgr_install_keys
BSF_WORKER_INSTALL_RPC=$CERT_DIR/resources/bsfwrk_install_keys
CSA_MANAGER_INSTALL_RPC=$RESOURCES_DIR/csa_manager_install_keys
CSA_WORKER_INSTALL_RPC=$CERT_DIR/resources/csa_worker_install_keys
SEPP_MANAGER_INSTALL_RPC=$RESOURCES_DIR/sepp_manager_install_keys
SEPP_WORKER_INSTALL_RPC=$CERT_DIR/resources/sepp_worker_install_keys
SCP_MANAGER_INSTALL_RPC=$RESOURCES_DIR/scp_manager_install_keys
SCP_WORKER_INSTALL_RPC=$CERT_DIR/resources/scp_worker_install_keys
WCDB_INTERNODE_INSTALL_RPC=$CERT_DIR/resources/wcdb_internode_install_keys
WCDB_SERVER_INSTALL_RPC=$CERT_DIR/resources/wcdb_server_external_install_keys
WCDB_CLIENT_INSTALL_RPC=$CERT_DIR/resources/wcdb_client_external_install_keys
WCDB_DAY0_TEMPLATE=$CERT_DIR/resources/eric-sec-certm-deployment-configuration.json
NBI_INSTALL_RPC=$CERT_DIR/resources/nbi_install_keys
DIAMETER_INSTALL_RPC=$CERT_DIR/resources/diameter_install_keys
LDAP_INSTALL_RPC=$CERT_DIR/resources/ldap_install_keys
REFERRAL_LDAP_INSTALL_RPC=$CERT_DIR/resources/referral_ldap_install_keys
PM_REMOTE_WRITE_INSTALL_RPC=$CERT_DIR/resources/pm_remote_write_install_keys
INFLUXDB_INSTALL_RPC=$CERT_DIR/resources/influxdb_install_keys
SLF_INSTALL_RPC=$CERT_DIR/resources/slf_install_keys
TRANSFORMER_INSTALL_RPC=$CERT_DIR/resources/transformer_install_keys
TRUST_INSTALL_RPC=$CERT_DIR/resources/trust_install_keys
SEPP_TRUST_INSTALL_RPC=$CERT_DIR/resources/sepp_trust_install_keys
SYSLOG_INSTALL_RPC=$CERT_DIR/resources/syslog_install_keys

CHFSIM_CNF=$RESOURCES_DIR/san.cnf
SEPP_CNF=$RESOURCES_DIR/san.cnf
SCP_CNF=$RESOURCES_DIR/san.cnf

K6_CNF=$RESOURCES_DIR/san.cnf
BSFLOAD_CNF=$RESOURCES_DIR/san.cnf
ETCD_UT_CNF=$RESOURCES_DIR/san.cnf
NBI_CNF=$RESOURCES_DIR/san.cnf
OSDB_CNF=$RESOURCES_DIR/san.cnf
LDAP_CNF=$RESOURCES_DIR/san.cnf
PM_REMOTE_WRITE_CNF=$RESOURCES_DIR/san.cnf
SYSLOG_CNF=$RESOURCES_DIR/san.cnf

GLOBAL_PASSWORD=rootroot

NETCONF_USER=sec-admin
NETCONF_PASS=secsec
NETCONF_PORT=830
NETCONF_LOCAL_PORT=8888

cleanup_certificates()
{
	certs=("${KEYS_DIR}/influxdb" "${KEYS_DIR}/pm_remote_write" "${KEYS_DIR}/referral-ldap" "${KEYS_DIR}/ldap" "${KEYS_DIR}/nbi" "${KEYS_DIR}/syslog" "${KEYS_DIR}/bsfload" "${KEYS_DIR}/k6" "${KEYS_DIR}/sepp" "${KEYS_DIR}/rp1" "${KEYS_DIR}/rp2" "${KEYS_DIR}/rp3" "${KEYS_DIR}/slf" "${KEYS_DIR}/seppwrk-A" "${KEYS_DIR}/seppwrk-B" "${KEYS_DIR}/seppmgr" "${KEYS_DIR}/scpwrk" "${KEYS_DIR}/csawrk" "${KEYS_DIR}/transformer" "${KEYS_DIR}/scpmgr" "${KEYS_DIR}/wcdb/internode" "${KEYS_DIR}/wcdb/client" "${KEYS_DIR}/wcdb/server" "${KEYS_DIR}/csamgr" "${KEYS_DIR}/bsfmgr" "${KEYS_DIR}/bsfwrk" "${KEYS_DIR}/dscload" "${KEYS_DIR}/diameter" "${KEYS_DIR}/seppsim" "${KEYS_DIR}/seppsim-scp" "${KEYS_DIR}/nrf" "${KEYS_DIR}/chf" "${KEYS_DIR}/object-storage/ca" "${KEYS_DIR}/object-storage/client" "${KEYS_DIR}/object-storage/server" "${KEYS_DIR}/etcd-ut/ca/client" "${KEYS_DIR}/etcd-ut/ca/server" "${KEYS_DIR}/etcd-ut/client" "${KEYS_DIR}/etcd-ut/server")
	for cert in ${certs[@]}; do
		delete_folder_contents $cert
	done
	delete_folder_contents $ROOTCA_DIR
}

delete_folder_contents()
{
	DIR="$1"
	if [ -d "$DIR" ]
	then
		if [ "$(ls -A ${DIR})" ]; then
			echo "Cleanup all generated files in ${DIR}"
			rm ${DIR}/*
		else
			echo "${DIR} is empty"
		fi
	else
		echo "${DIR} does not exist"
	fi
}

if [ "$1" = "cleanup" ]
then
	echo "Cleanup of all certificates"
	cleanup_certificates
	echo "Please delete manually all install certificates from CMYP"
	exit 0
fi

# This script's purpose is to automate a simple use case that installs key and certificate to Certificate Management ADP for envoy traffic encryption
# It can be manually executed, or from tls_certs.sh that is called from deploy:certificates rule in the ruleset
if [[ $1 == "-ni" ]];
then 
	export NO_INSTALL="true"; 
	echo "Only generation and no installation will be executed as NO_INSTALL is set to: $NO_INSTALL"; 
	shift; 

    if [[ $1 != "chfsim" ]] && [[ $1 != "csawrk" ]] && [[ $1 != "nrfsim" ]] && [[ $1 != "seppsim" ]] && [[ $1 != "seppsim-scp" ]] && [[ $1 != "scpwrk" ]];
    then
		echo "This option currently does not apply for $1. Exiting ...";
		exit 1;            
	fi; 
fi;

if [ ! -z ../.bob/var.namespace ];
then
	export KUBE_NAMESPACE=`cat ../.bob/var.namespace`
else
	export KUBE_NAMESPACE="5g-bsf-${USER}"
fi
echo "Selected namespace $KUBE_NAMESPACE"

export KUBE_CONFIG="../.bob/${KUBE_HOST}.admin.conf"
echo

# deprecated check moved in makefile
#if [[ $SKIP_CERTS == "true" ]]; then
#	echo "WARNING: No external TLS certificates generated for $1 in your current deployment!"
#	exit 0;
#fi

echo "INFO: Generating certificates for $1 in your current deployment!"

# Extract NBI fqdn
nbiFqdn=$(kubectl get httpproxy/eric-sc-cs-nbi --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE -o jsonpath='{.spec.virtualhost.fqdn}'  )

# Give some time to deployment. 
# It is not clear why, but if yang action comes too early after installation it fails - even if every critical pod is in Running state.
if [[ $NO_INSTALL != "true" ]]
then
	echo "Executing the use case including the installation";
	if [ "$1" != "syslog" ] && [ "$1" != "k6" ] && [ "$1" != "bsfload" ]&& [ "$1" != "chfsim" ] && [ "$1" != "nrfsim" ] && [ "$1" != "seppsim" ] && [ "$1" != "seppsim-scp" ] && [ "$1" != "slf" ] && [ "$1" != "csamgr" ] && [ "$1" != "scpmgr" ] && [ "$1" != "seppmgr" ]
	then
		state=$(kubectl --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE get pods | awk '/eric-cm-yang-provider/{print $2}')
		loop=0
		yangStatus=($(echo ${state} | sed s/\\//\\n/g))
		until [ ${yangStatus[0]} == ${yangStatus[1]} ]
		do
			echo "Waiting for eric-cm-yang-provider to start up: $state"
			sleep 10;
			state=$(kubectl --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE get pods | awk '/eric-cm-yang-provider/{print $2}')
			loop=1
			yangStatus=($(echo ${state} | sed s/\\//\\n/g))
		done

		[ $loop -eq 1 ] && echo "Waiting for eric-cm-yang-provider to start up: $state, waiting another 5 seconds." && sleep 5
	fi
fi;

##########################
# REPLACED BY BELOW CODE #
##########################

## incase of an IPv6 cluster select the LB instead of the k8s worker IP
#IPV6_NODE=`kubectl get configmap/calico-config -o jsonpath='{.data.cni_network_config}' -n kube-system | grep -v CNI_MTU | jq -r '.plugins[0].ipam.assign_ipv6' `

#if kubectl get svc --namespace $KUBE_NAMESPACE | grep eric-cm-yang-provider-ipv6;
#then
#    echo "The node is IPv6, fetch ipaddress and port from loadbalancer eric-cm-yang-provider-ipv6"
#    NODE_IP=$(kubectl get svc --namespace $KUBE_NAMESPACE eric-cm-yang-provider-ipv6 -o jsonpath="{.status.loadBalancer.ingress[0].ip}")
#    CMYP_PORT=$(kubectl get svc --namespace $KUBE_NAMESPACE eric-cm-yang-provider-ipv6 -o jsonpath="{.spec.ports[0].port}")
#else
#    if [ $IPV6_NODE == "true" ];
#    then
#        echo "The node is IPv6, fetch ipaddress and port from loadbalancer eric-cm-yang-provider"
#        NODE_IP=$(kubectl get svc --namespace $KUBE_NAMESPACE eric-cm-yang-provider -o jsonpath="{.status.loadBalancer.ingress[0].ip}")
#        CMYP_PORT=$(kubectl get svc --namespace $KUBE_NAMESPACE eric-cm-yang-provider -o jsonpath="{.spec.ports[0].port}")
#    else
#        echo "The node is not ipv6"
#        NODE_IP=$(kubectl get nodes --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE  -o jsonpath="{.items[3].status.addresses[0].address}")
#        CMYP_PORT=$(kubectl get --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE  -o jsonpath="{.spec.ports[0].nodePort}" services eric-cm-yang-provider)
#    fi
#fi;

    ##################
    # Extract CMM_IP #
    ##################
    export SVC_BASE_NAME="eric-cm-yang-provider";
	export SUFFIXES="0 -ipv4 -ipv6"

    for SUFFIX in $SUFFIXES;
    do
        if [ "$SUFFIX" == "0" ];
        then
            export SUFFIX="";
        fi;

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

                export CMM_IP$VSUF=`kubectl get svc ${SVC_NAME} --kubeconfig ${KUBE_CONFIG} --namespace ${KUBE_NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[*].ip}'`;
                export CMM_PORT$VSUF=`kubectl get svc ${SVC_NAME} --kubeconfig ${KUBE_CONFIG} --namespace ${KUBE_NAMESPACE} -o jsonpath="{.spec.ports[?(@.name == \"netconf\")].port}"`;

                TEMP_CMM_IP="CMM_IP$VSUF";
				TEMP_CMM_PORT="CMM_PORT$VSUF";
				
                if [ "${!TEMP_CMM_IP}" == "" ];
                then
                    echo "LoadBalancer seems to be in state <Pending> due to port conflicts on 80 or 443. Falling back to combination of <NodeIP>:<NodePort>.";
                    export CMM_IP$VSUF=`kubectl get nodes --kubeconfig ${KUBE_CONFIG} --namespace ${KUBE_NAMESPACE} -l '!node-role.kubernetes.io/master' -o jsonpath='{.items[0].status.addresses[?(@.type == "InternalIP" )].address}'`;
                    export CMM_PORT$VSUF=`kubectl get svc ${SVC_NAME} --kubeconfig ${KUBE_CONFIG} --namespace ${KUBE_NAMESPACE} -o jsonpath="{.spec.ports[?(@.name == \"netconf\")].nodePort}"`;
                    TEMP_CMM_IP="CMM_IP$VSUF";
                    TEMP_CMM_PORT="CMM_PORT$VSUF"
                fi;

                echo " Loadbalancer associated IP: ${!TEMP_CMM_IP} ";
                echo " Loadbalancer associated Port: ${!TEMP_CMM_PORT} ";

            elif [ "${SVC_TYPE}" == "NodePort" ];
            then
                echo "Identified Service Type: ${SVC_TYPE}"; 

                export CMM_IP$VSUF=`kubectl get nodes --kubeconfig ${KUBE_CONFIG} --namespace ${KUBE_NAMESPACE} -l '!node-roleEscTestPropertiesrnetes.io/master' -o jsonpath='{.items[0].status.addresses[?(@.type == "InternalIP" )].address}'`;
                export CMM_PORT$VSUF=`kubectl get svc ${SVC_NAME} --kubeconfig ${KUBE_CONFIG} --namespace ${KUBE_NAMESPACE} -o jsonpath="{.spec.ports[?(@.name == \"netconf\")].nodePort}"`;
                TEMP_CMM_IP="CMM_IP$VSUF";
                TEMP_CMM_PORT="CMM_PORT$VSUF"

                echo " Loadbalancer associated IP: ${!TEMP_CMM_IP} ";
                echo " Loadbalancer associated Port: ${!TEMP_CMM_PORT} ";

            else

                    echo "Error: The service seems not to be accessible from outside the cluster."; exit 1;
            fi;
        fi;

    done;

    if [ "${CMM_IP_IPV6}" != "" ] || [ "${CMM_IP_IPV4}" != "" ];
    then 
        export STACK="2"; #DUAL STACK
    else
        echo "${CMM_IP}" | grep -e "^[a-f,0-9]\{1,4\}:\{1,\}\([a-f,0-9]\{0,\}:\{0,\}\)\{1,\}[a-f,0-9]\{1,4\}$";
        
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
            export CMM_IP="${CMM_IP_IPV6}";
            export CMM_PORT="${CMM_PORT_IPV6}";
        elif [ "${STACK}" == "1" ];
        then
            echo "This is an IPv6 deployment on a single stack. Using Address for CM YANG PROVIDER without suffix \"-ipv6\"." 
        else
            echo "ERROR: The parameter IP_VERSION was set to 6, but no IPv6 address could be identified. Exiting"; exit 1;
        fi;
    else
        if [ ! -z ${CMM_IP_IPV4} ];
        then
            export CMM_IP="${CMM_IP_IPV4}";
            export CMM_PORT="${CMM_PORT_IPV4}";
        else
            echo "Using the values found via standard service eric-cm-yang-provider";
        fi;

    fi;

export NODE_IP="${CMM_IP}";
export CMYP_PORT="${CMM_PORT}";

echo "Node IP: $NODE_IP"
echo "CMYP port: $CMYP_PORT"


rootca_exists()
{
	[ -d $ROOTCA_DIR ] && [ -f $ROOTCA_DIR/rootCA.key ] && [ -f $ROOTCA_DIR/rootCA.crt ] && [ -f $ROOTCA_DIR/rootCA-base64.p12 ] && [ -f $ROOTCA_DIR/rootCAsepp.key ] && [ -f $ROOTCA_DIR/rootCAsepp.crt ] && [ -f $ROOTCA_DIR/rootCAsepp-base64.p12 ]
}

#Generate self-signed CA certificate
if ! rootca_exists
then
	echo "Creating root CA files."
	mkdir -p $ROOTCA_DIR
	openssl genrsa -out ${ROOTCA_DIR}/rootCA.key 3072
	openssl req -new -x509 -days 730 -key $ROOTCA_DIR/rootCA.key -out $ROOTCA_DIR/rootCA.crt -subj "/C=GR/L=Thessaloniki/O=Intracom-Telecom/OU=IXG/CN=rootca"
        openssl pkcs12 -export -out ${ROOTCA_DIR}/truststore.pfx -in $ROOTCA_DIR/rootCA.crt -inkey $ROOTCA_DIR/rootCA.key
        base64 $ROOTCA_DIR/rootCA.crt | tr -d \\n > $ROOTCA_DIR/rootCA-base64.p12
	
	# This is used only in SEPP, where multiple certificates are supported
	openssl genrsa -out ${ROOTCA_DIR}/rootCAsepp.key 3072
	openssl req -new -x509 -days 730 -key $ROOTCA_DIR/rootCAsepp.key -out $ROOTCA_DIR/rootCAsepp.crt -subj "/C=GR/L=Thessaloniki/O=Intracom-Telecom/OU=IXG/CN=testcasepp"
	base64 $ROOTCA_DIR/rootCAsepp.crt | tr -d \\n > $ROOTCA_DIR/rootCAsepp-base64.p12
else
	echo "CA files found."
	base64 $ROOTCA_DIR/rootCA.crt | tr -d \\n > $ROOTCA_DIR/rootCA-base64.p12  # <-- just to make sure that this file exists (backward compatibility)
	base64 $ROOTCA_DIR/rootCAsepp.crt | tr -d \\n > $ROOTCA_DIR/rootCAsepp-base64.p12  # <-- just to make sure that this file exists (backward compatibility)
fi

create_certificate()
{
	node=$1 
	fqdn=$2
	outdir=$3

	echo "##############################"
	echo "Creating certificate for $node"
	echo "##############################"

	if [ $node != "K6" ] &&	[ $node != "bsfload" ] &&[ $node != "SCPMGR" ] &&
		[ $node != "WCDB" ] && [ $node != "DIAM" ] && [ $node != "DSCLOAD" ] &&
		[ $node != "CSAMGR" ] && [ $node != "CSAWRK" ] && [ $node != "SCPWRK" ] &&
		[ $node != "SEPPMGR" ] && [ $node != "SEPPWRK-A" ]  && [ $node != "SEPPWRK-B" ] &&
		[ $node != "SLF" ] && [ $node != "BSFWRK" ] && [ $node != "BSFMGR" ] &&
		[ $node != "rootca" ] && [ $node != "INTERNALLDAP" ] && [ $node != "EXTERNALLDAP" ] &&
		[ $node != "ETCDUT" ] && [ $node != "PM_REMOTE_WRITE" ] && [ $node != "INFLUXDB" ] &&
		[ $node != "SYSLOG" ] && [ $node != "TRANSFORMER" ]
	then
	   fqdn=$fqdn.$KUBE_NAMESPACE
	fi

	mkdir -p $KEYS_DIR/$outdir

	# create the certificate request and sign it
	if [ $node == "CHF" ]
	then
		cnf=$(sed "s|{fqdn}|$fqdn|; s|{dns_fqdn}|eric-chfsim-1|" $CHFSIM_CNF)
		cnf="$cnf\nDNS.2  = eric-chfsim-2\nDNS.3  = eric-chfsim-3\nDNS.4  = eric-chfsim-4\nDNS.5  = eric-chfsim-5\nDNS.6  = eric-chfsim-6\nDNS.7  = eric-chfsim-7\nDNS.8  = eric-chfsim-8"

		openssl req -nodes -newkey rsa:3072 -keyout $KEYS_DIR/$outdir/key.pem -out $KEYS_DIR/$outdir/cert.pem -subj "/C=GR/L=Thessaloniki/O=Intracom-Telecom/OU=IXG/CN=$fqdn" -config <(printf "$cnf") -extensions 'v3_req'
		openssl x509 -req -in $KEYS_DIR/$outdir/cert.pem -CA $ROOTCA_DIR/rootCA.crt -CAkey $ROOTCA_DIR/rootCA.key -CAcreateserial -days 365 -out $KEYS_DIR/$outdir/cert.pem -extensions 'v3_req' -extfile <(printf "$cnf")
	elif [ $node == "CHFSEPP" ]
	then
		cnf=$(sed "s|{fqdn}|$fqdn|; s|{dns_fqdn}|eric-chfsim-1-mnc-123-mcc-123|" $CHFSIM_CNF)
		cnf="$cnf\nDNS.2  = eric-chfsim-2-mnc-123-mcc-123\nDNS.3  = eric-chfsim-3-mnc-456-mcc-456\nDNS.4  = eric-chfsim-4-mnc-456-mcc-456\nDNS.5  = eric-chfsim-5-mnc-456-mcc-456\nDNS.6  = eric-chfsim-6-mnc-456-mcc-456\nDNS.7  = eric-chfsim-7-mnc-456-mcc-456\nDNS.8  = eric-chfsim-8-mnc-456-mcc-456"

		openssl req -nodes -newkey rsa:3072 -keyout $KEYS_DIR/$outdir/key.pem -out $KEYS_DIR/$outdir/cert.pem -subj "/C=GR/L=Thessaloniki/O=Intracom-Telecom/OU=IXG/CN=$fqdn" -config <(printf "$cnf") -extensions 'v3_req'
		openssl x509 -req -in $KEYS_DIR/$outdir/cert.pem -CA $ROOTCA_DIR/rootCA.crt -CAkey $ROOTCA_DIR/rootCA.key -CAcreateserial -days 365 -out $KEYS_DIR/$outdir/cert.pem -extensions 'v3_req' -extfile <(printf "$cnf")
	elif [ $node == "CHFCI" ]
	then
	    eth0_ip=$(ip -4 -f inet a show eth0 |  grep -Eo 'inet ([0-9]*\.){3}[0-9]*' |  grep -Eo '([0-9]*\.){3}[0-9]*')
		cnf=$(sed "s|{fqdn}|$fqdn|; s|{dns_fqdn}|$eth0_ip|" $CHFSIM_CNF)

		openssl req -nodes -newkey rsa:3072 -keyout $KEYS_DIR/$outdir/key.pem -out $KEYS_DIR/$outdir/cert.pem -subj "/C=GR/L=Thessaloniki/O=Intracom-Telecom/OU=IXG/CN=$fqdn" -config <(printf "$cnf") -extensions 'v3_req'
		openssl x509 -req -in $KEYS_DIR/$outdir/cert.pem -CA $ROOTCA_DIR/rootCA.crt -CAkey $ROOTCA_DIR/rootCA.key -CAcreateserial -days 365 -out $KEYS_DIR/$outdir/cert.pem -extensions 'v3_req' -extfile <(printf "$cnf")
    elif [ $node == "SCPWRK" ]
    then 
        cnf=$(sed "s|{fqdn}|$fqdn|; s|{dns_fqdn}|$fqdn|" $SCP_CNF)
        openssl req -nodes -newkey rsa:3072 -keyout $KEYS_DIR/$outdir/key.pem -out $KEYS_DIR/$outdir/cert.pem -subj "/C=GR/L=Thessaloniki/O=Intracom-Telecom/OU=IXG/CN=$fqdn" -config <(printf "$cnf") -extensions 'v3_req'
		openssl x509 -req -in $KEYS_DIR/$outdir/cert.pem -CA $ROOTCA_DIR/rootCA.crt -CAkey $ROOTCA_DIR/rootCA.key -CAcreateserial -days 365 -out $KEYS_DIR/$outdir/cert.pem -extensions 'v3_req' -extfile <(printf "$cnf")
	
	elif [ $node == "SEPPWRK-A" ]
    then 
        cnf=$(sed "s|{fqdn}|$fqdn|; s|{dns_fqdn}|$fqdn|" $SEPP_CNF)
        openssl req -nodes -newkey rsa:3072 -keyout $KEYS_DIR/$outdir/key.pem -out $KEYS_DIR/$outdir/cert.pem -subj "/C=GR/L=Thessaloniki/O=Intracom-Telecom/OU=IXG/CN=$fqdn" -config <(printf "$cnf") -extensions 'v3_req'
		openssl x509 -req -in $KEYS_DIR/$outdir/cert.pem -CA $ROOTCA_DIR/rootCA.crt -CAkey $ROOTCA_DIR/rootCA.key -CAcreateserial -days 365 -out $KEYS_DIR/$outdir/cert.pem -extensions 'v3_req' -extfile <(printf "$cnf")
	elif [ $node == "SEPPWRK-B" ]
    then 
        cnf=$(sed "s|{fqdn}|$fqdn|; s|{dns_fqdn}|$fqdn|" $SEPP_CNF)
        openssl req -nodes -newkey rsa:3072 -keyout $KEYS_DIR/$outdir/key.pem -out $KEYS_DIR/$outdir/cert.pem -subj "/C=GR/L=Thessaloniki/O=Intracom-Telecom/OU=IXG/CN=$fqdn" -config <(printf "$cnf") -extensions 'v3_req'
		openssl x509 -req -in $KEYS_DIR/$outdir/cert.pem -CA $ROOTCA_DIR/rootCAsepp.crt -CAkey $ROOTCA_DIR/rootCAsepp.key -CAcreateserial -days 365 -out $KEYS_DIR/$outdir/cert.pem -extensions 'v3_req' -extfile <(printf "$cnf")
	elif [ $node = "ETCDUT" ]
	then
		mkdir ${KEYS_DIR}/$outdir/ca/
		mkdir ${KEYS_DIR}/$outdir/ca/client
		mdkir ${KEYS_DIR}/$outdir/ca/server
		echo "Creating CA for etcd unit tests."
		openssl genrsa -out ${KEYS_DIR}/$outdir/ca/rootCA.key 2048
		openssl req -new -x509 -days 25550 -key ${KEYS_DIR}/$outdir/ca/rootCA.key -out ${KEYS_DIR}/$outdir/ca/rootCA.crt -subj "/C=GR/L=Thessaloniki/O=Intracom-Telecom/OU=IXG/CN=${fqdn}:2379"
		base64 ${KEYS_DIR}/$outdir/ca/rootCA.crt | tr -d \\n > ${KEYS_DIR}/$outdir/ca/rootCA-base64.p12
	
		echo "Creating CERTIFICATES for etcd unit tests."
		cnf=$(sed "s|{fqdn}|$fqdn|; s|{dns_fqdn}|*|" $ETCD_UT_CNF)
		#cnf="$cnf\nDNS.2  = localhost"
		#cnf="$cnf\nDNS.3  = *localhost"
		#cnf="$cnf\nDNS.4  = *localhost*"
		#cnf="$cnf\nDNS.5  = localhost*"
		#cnf="$cnf\nDNS.3  = *"
		
		openssl req -nodes -newkey rsa:2048 -keyout $KEYS_DIR/$outdir/client/key.pem -out $KEYS_DIR/$outdir/client/cert.pem -subj "/C=GR/L=Thessaloniki/O=Intracom-Telecom/OU=IXG/CN=$fqdn" -config <(printf "$cnf") -extensions 'v3_req'
		openssl x509 -req -in $KEYS_DIR/$outdir/client/cert.pem -CA ${KEYS_DIR}/$outdir/ca/rootCA.crt -CAkey ${KEYS_DIR}/$outdir/ca/rootCA.key -CAcreateserial -days 25550 -out $KEYS_DIR/$outdir/client/cert.pem -extensions 'v3_req' -extfile <(printf "$cnf")
		openssl req -nodes -newkey rsa:2048 -keyout $KEYS_DIR/$outdir/server/key.pem -out $KEYS_DIR/$outdir/server/cert.pem -subj "/C=GR/L=Thessaloniki/O=Intracom-Telecom/OU=IXG/CN=$fqdn" -config <(printf "$cnf") -extensions 'v3_req'
		openssl x509 -req -in $KEYS_DIR/$outdir/server/cert.pem -CA ${KEYS_DIR}/$outdir/ca/rootCA.crt -CAkey ${KEYS_DIR}/$outdir/ca/rootCA.key -CAcreateserial -days 25550 -out $KEYS_DIR/$outdir/server/cert.pem -extensions 'v3_req' -extfile <(printf "$cnf")
		
		# cert file and rootCA create a certificate chain
		cat $KEYS_DIR/$outdir/client/cert.pem $KEYS_DIR/$outdir/server/cert.pem $KEYS_DIR/$outdir/ca/rootCA.crt > $KEYS_DIR/$outdir/cert.chain.pem
			
		# Format according to Certificate Management interface
		cat $KEYS_DIR/$outdir/client/key.pem > $KEYS_DIR/$outdir/client.txt

		cat $KEYS_DIR/$outdir/client/cert.pem >> $KEYS_DIR/$outdir/client.txt
		cat $KEYS_DIR/$outdir/server/key.pem > $KEYS_DIR/$outdir/server.txt
		cat $KEYS_DIR/$outdir/server/cert.pem >> $KEYS_DIR/$outdir/server.txt
		
		# create p12 archive which contains private key and certificate
		openssl pkcs12 -export -in $KEYS_DIR/$outdir/client.txt -out $KEYS_DIR/$outdir/client-container.p12 -passout pass:"$GLOBAL_PASSWORD" -passin pass:"$GLOBAL_PASSWORD"
		base64 $KEYS_DIR/$outdir/client-container.p12 | tr -d \\n > $KEYS_DIR/$outdir/client-container-base64.p12
		openssl pkcs12 -export -in $KEYS_DIR/$outdir/server.txt -out $KEYS_DIR/$outdir/server-container.p12 -passout pass:"$GLOBAL_PASSWORD" -passin pass:"$GLOBAL_PASSWORD"
		base64 $KEYS_DIR/$outdir/server-container.p12 | tr -d \\n > $KEYS_DIR/$outdir/server-container-base64.p12
		rm -rf $KEYS_DIR/$outdir/client.txt; rm -rf $KEYS_DIR/$outdir/server.txt; rm -rf $KEYS_DIR/$outdir/cert.chain.pem
		
		fix_permissions
		
		echo "Copying certificates to etcd unit tests resources."
		cp ${KEYS_DIR}/$outdir/ca/rootCA.crt ../rx-etcd-client/src/test/resources/old_certs/trustCA/cert1.pem
		cp $KEYS_DIR/$outdir/client/cert.pem ../rx-etcd-client/src/test/resources/old_certs/client-cert.pem
		cp $KEYS_DIR/$outdir/client/key.pem ../rx-etcd-client/src/test/resources/old_certs/client-key.pem
		cp $KEYS_DIR/$outdir/server/cert.pem ../rx-etcd-client/src/test/resources/old_certs/server-cert.pem
		cp $KEYS_DIR/$outdir/server/key.pem ../rx-etcd-client/src/test/resources/old_certs/server-key.pem
		exit 0
	elif [ $node == "NBI" ]
	then
		if [ -z "$nbiFqdn" ]; then
			openssl req -nodes -newkey rsa:3072 -keyout $KEYS_DIR/$outdir/key.pem -out $KEYS_DIR/$outdir/cert.pem -subj "/C=GR/L=Thessaloniki/O=Intracom-Telecom/OU=IXG/CN=$fqdn"
			openssl x509 -req -in $KEYS_DIR/$outdir/cert.pem -CA $ROOTCA_DIR/rootCA.crt -CAkey $ROOTCA_DIR/rootCA.key -CAcreateserial -days 365 -out $KEYS_DIR/$outdir/cert.pem
		else
			cnf=$(sed "s|{fqdn}|${nbiFqdn}|; s|{dns_fqdn}|${nbiFqdn}|" $NBI_CNF)
			cnf="$cnf\nDNS.2  = *.ericsson.se"
			openssl req -nodes -newkey rsa:3072 -keyout $KEYS_DIR/$outdir/key.pem -out $KEYS_DIR/$outdir/cert.pem -subj "/C=GR/L=Thessaloniki/O=Intracom-Telecom/OU=IXG/CN=${nbiFqdn}" -config <(printf "$cnf") -extensions 'v3_req'
			openssl x509 -req -in $KEYS_DIR/$outdir/cert.pem -CA $ROOTCA_DIR/rootCA.crt -CAkey $ROOTCA_DIR/rootCA.key -CAcreateserial -days 365 -out $KEYS_DIR/$outdir/cert.pem -extensions 'v3_req' -extfile <(printf "$cnf")
		fi
	elif [ $node = "OSDB" ]
	then
		echo "Creating CA for object storage."
		openssl genrsa -out ${KEYS_DIR}/$outdir/ca/rootCA.key 3072
		openssl req -new -x509 -days 25550 -key ${KEYS_DIR}/$outdir/ca/rootCA.key -out ${KEYS_DIR}/$outdir/ca/rootCA.crt -subj "/C=GR/L=Thessaloniki/O=Intracom-Telecom/OU=IXG/CN=$fqdn"
		base64 ${KEYS_DIR}/$outdir/ca/rootCA.crt | tr -d \\n > ${KEYS_DIR}/$outdir/ca/rootCA-base64.p12
	
		echo "Creating CERTIFICATES for object storage."
		cnf=$(sed "s|{fqdn}|$fqdn|; s|{dns_fqdn}|localhost|" $OSDB_CNF)
		openssl req -nodes -newkey rsa:3072 -keyout $KEYS_DIR/$outdir/client/key.pem -out $KEYS_DIR/$outdir/client/cert.pem -subj "/C=GR/L=Thessaloniki/O=Intracom-Telecom/OU=IXG/CN=$fqdn" -config <(printf "$cnf") -extensions 'v3_req'
		openssl x509 -req -in $KEYS_DIR/$outdir/client/cert.pem -CA ${KEYS_DIR}/$outdir/ca/rootCA.crt -CAkey ${KEYS_DIR}/$outdir/ca/rootCA.key -CAcreateserial -days 25550 -out $KEYS_DIR/$outdir/client/cert.pem -extensions 'v3_req' -extfile <(printf "$cnf")
		openssl req -nodes -newkey rsa:3072 -keyout $KEYS_DIR/$outdir/server/key.pem -out $KEYS_DIR/$outdir/server/cert.pem -subj "/C=GR/L=Thessaloniki/O=Intracom-Telecom/OU=IXG/CN=$fqdn" -config <(printf "$cnf") -extensions 'v3_req'
		openssl x509 -req -in $KEYS_DIR/$outdir/server/cert.pem -CA ${KEYS_DIR}/$outdir/ca/rootCA.crt -CAkey ${KEYS_DIR}/$outdir/ca/rootCA.key -CAcreateserial -days 25550 -out $KEYS_DIR/$outdir/server/cert.pem -extensions 'v3_req' -extfile <(printf "$cnf")
		
		# cert file and rootCA create a certificate chain
		cat $KEYS_DIR/$outdir/client/cert.pem $KEYS_DIR/$outdir/server/cert.pem $KEYS_DIR/$outdir/ca/rootCA.crt > $KEYS_DIR/$outdir/cert.chain.pem
			
		# Format according to Certificate Management interface
		cat $KEYS_DIR/$outdir/client/key.pem > $KEYS_DIR/$outdir/client.txt
		cat $KEYS_DIR/$outdir/client/cert.pem >> $KEYS_DIR/$outdir/client.txt
		cat $KEYS_DIR/$outdir/server/key.pem > $KEYS_DIR/$outdir/server.txt
		cat $KEYS_DIR/$outdir/server/cert.pem >> $KEYS_DIR/$outdir/server.txt
		
		# create p12 archive which contains private key and certificate
		openssl pkcs12 -export -in $KEYS_DIR/$outdir/client.txt -out $KEYS_DIR/$outdir/client-container.p12 -passout pass:"$GLOBAL_PASSWORD" -passin pass:"$GLOBAL_PASSWORD"
		base64 $KEYS_DIR/$outdir/client-container.p12 | tr -d \\n > $KEYS_DIR/$outdir/client-container-base64.p12
		openssl pkcs12 -export -in $KEYS_DIR/$outdir/server.txt -out $KEYS_DIR/$outdir/server-container.p12 -passout pass:"$GLOBAL_PASSWORD" -passin pass:"$GLOBAL_PASSWORD"
		base64 $KEYS_DIR/$outdir/server-container.p12 | tr -d \\n > $KEYS_DIR/$outdir/server-container-base64.p12
		rm -rf $KEYS_DIR/$outdir/client.txt; rm -rf $KEYS_DIR/$outdir/server.txt; rm -rf $KEYS_DIR/$outdir/cert.chain.pem
		
		fix_permissions
		
		echo "Copying certificates to s3client."
		cp ${KEYS_DIR}/$outdir/ca/rootCA.crt ../esc/s3client/src/test/resources/certificates/ca/public.crt
		cp $KEYS_DIR/$outdir/server/cert.pem ../esc/s3client/src/test/resources/certificates/server/public.crt
		cp $KEYS_DIR/$outdir/server/key.pem ../esc/s3client/src/test/resources/certificates/server/private.key
		exit 0
	elif [ $node == "K6" ]
	then
        cnf=$(sed "s|{fqdn}|$fqdn|; s|{dns_fqdn}|$fqdn|" $K6_CNF)
        openssl req -nodes -newkey rsa:3072 -keyout $KEYS_DIR/$outdir/key$4.pem -out $KEYS_DIR/$outdir/cert$4.pem -subj "/C=GR/L=Thessaloniki/O=Intracom-Telecom/OU=IXG/CN=$fqdn" -config <(printf "$cnf") -extensions 'v3_req'
		openssl x509 -req -in $KEYS_DIR/$outdir/cert$4.pem -CA $ROOTCA_DIR/rootCA.crt -CAkey $ROOTCA_DIR/rootCA.key -CAcreateserial -days 365 -out $KEYS_DIR/$outdir/cert$4.pem -extensions 'v3_req' -extfile <(printf "$cnf")
	elif ([ $node == "INTERNALLDAP" ] || [ $node == "EXTERNALLDAP" ])
	then
		cnf=$(sed "s|{fqdn}|$fqdn|; s|{dns_fqdn}|$fqdn|" $LDAP_CNF)
		openssl req -nodes -newkey rsa:3072 -keyout $KEYS_DIR/$outdir/key.pem -out $KEYS_DIR/$outdir/cert.pem -subj "/C=GR/L=Thessaloniki/O=Intracom-Telecom/OU=IXG/CN=$fqdn" -config <(printf "$cnf") -extensions 'v3_req'
		openssl x509 -req -in $KEYS_DIR/$outdir/cert$4.pem -CA $ROOTCA_DIR/rootCA.crt -CAkey $ROOTCA_DIR/rootCA.key -CAcreateserial -days 365 -out $KEYS_DIR/$outdir/cert.pem -extensions 'v3_req' -extfile <(printf "$cnf")
	elif [ $node == "bsfload" ]
	then
        cnf=$(sed "s|{fqdn}|$fqdn|; s|{dns_fqdn}|$fqdn|" $BSFLOAD_CNF)
        openssl req -nodes -newkey rsa:3072 -keyout $KEYS_DIR/$outdir/key.pem -out $KEYS_DIR/$outdir/cert.pem -subj "/C=GR/L=Thessaloniki/O=Intracom-Telecom/OU=IXG/CN=$fqdn" -config <(printf "$cnf") -extensions 'v3_req'
		openssl x509 -req -in $KEYS_DIR/$outdir/cert.pem -CA $ROOTCA_DIR/rootCA.crt -CAkey $ROOTCA_DIR/rootCA.key -CAcreateserial -days 365 -out $KEYS_DIR/$outdir/cert.pem -extensions 'v3_req' -extfile <(printf "$cnf")
	elif ([ $node == "PM_REMOTE_WRITE" ] || [ $node == "INFLUXDB" ])
	then
		cnf=$(sed "s|{fqdn}|$fqdn|; s|{dns_fqdn}|$fqdn|" $PM_REMOTE_WRITE_CNF)
		openssl req -nodes -newkey rsa:3072 -keyout $KEYS_DIR/$outdir/key.pem -out $KEYS_DIR/$outdir/cert.pem -subj "/C=GR/L=Thessaloniki/O=Intracom-Telecom/OU=IXG/CN=$fqdn" -config <(printf "$cnf") -extensions 'v3_req'
		openssl x509 -req -in $KEYS_DIR/$outdir/cert.pem -CA $ROOTCA_DIR/rootCA.crt -CAkey $ROOTCA_DIR/rootCA.key -CAcreateserial -days 365 -out $KEYS_DIR/$outdir/cert.pem -extensions 'v3_req' -extfile <(printf "$cnf")
	elif ([ $node == "TRANSFORMER" ] || [ $node == "SYSLOG" ])
	then
		cnf=$(sed "s|{fqdn}|$fqdn|; s|{dns_fqdn}|$fqdn|" $SYSLOG_CNF)
		cnf="$cnf\nDNS.2  = *"
		openssl req -nodes -newkey rsa:3072 -keyout $KEYS_DIR/$outdir/key.pem -out $KEYS_DIR/$outdir/cert.pem -subj "/C=GR/L=Thessaloniki/O=Intracom-Telecom/OU=IXG/CN=$fqdn" -config <(printf "$cnf") -extensions 'v3_req'
		openssl x509 -req -in $KEYS_DIR/$outdir/cert.pem -CA $ROOTCA_DIR/rootCA.crt -CAkey $ROOTCA_DIR/rootCA.key -CAcreateserial -days 365 -out $KEYS_DIR/$outdir/cert.pem -extensions 'v3_req' -extfile <(printf "$cnf")
	else
		openssl req -nodes -newkey rsa:3072 -keyout $KEYS_DIR/$outdir/key$4.pem -out $KEYS_DIR/$outdir/cert$4.pem -subj "/C=GR/L=Thessaloniki/O=Intracom-Telecom/OU=IXG/CN=$2"
		openssl x509 -req -in $KEYS_DIR/$outdir/cert$4.pem -CA $ROOTCA_DIR/rootCA.crt -CAkey $ROOTCA_DIR/rootCA.key -CAcreateserial -days 365 -out $KEYS_DIR/$outdir/cert$4.pem
	fi

	# cert file and rootCA create a certificate chain
	cat $KEYS_DIR/$outdir/cert.pem $ROOTCA_DIR/rootCA.crt > $KEYS_DIR/$outdir/cert.chain.pem
		
	# Format according to Certificate Management interface
	cat $KEYS_DIR/$outdir/key.pem > $KEYS_DIR/$outdir/worker.txt
	cat $KEYS_DIR/$outdir/cert.pem >> $KEYS_DIR/$outdir/worker.txt
	# create p12 archive which contains private key and certificate
	openssl pkcs12 -export -in $KEYS_DIR/$outdir/worker.txt -out $KEYS_DIR/$outdir/container.p12 -passout pass:"$GLOBAL_PASSWORD" -passin pass:"$GLOBAL_PASSWORD"
	base64 $KEYS_DIR/$outdir/container.p12 | tr -d \\n > $KEYS_DIR/$outdir/container-base64.p12
	rm -rf $KEYS_DIR/$outdir/worker.txt; rm -rf $KEYS_DIR/$outdir/cert.chain.pem
	fix_permissions
}

fix_permissions()
{
	# Fix user ownership for locally generated files, if running inside container as root
	if [ $(id -u) = 0 ]
	then
		find $KEYS_DIR/ -type d -exec chmod 0777 {} \;
		find $KEYS_DIR/ -type f -exec chmod 0777 {} \;
	fi
}

install_action()
{
  action=$1
  fix_permissions
  for i in `seq 1 5`;
  do
    echo "./send_command_to_ssh_standalone.exp --user=$NETCONF_USER --password=$NETCONF_PASS --shell=netconf --command-file=$action --ip=$NODE_IP --port=$CMYP_PORT"
    ./send_command_to_ssh_standalone.exp --user=$NETCONF_USER --password=$NETCONF_PASS --shell=netconf --command-file=$action --ip=$NODE_IP --port=$CMYP_PORT
    if [ $? -eq 0 ]
    then
      # success!
      return
    fi
    echo "\\nFailed to install key, retrying in 2 seconds..."
    sleep 2
    echo "\\nRemoving keys, just in case."
    ssh-keygen -R [$NODE_IP]:$CMYP_PORT
  done
  echo "Cannot install key, giving up.  Exiting..."
  exit 1
}

# This is the old/unused function that used port-forwardings. It has been replaced because it was unreliable (the port-fwd).
install_action_old()
{
	action=$1

	kubectl port-forward --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE services/eric-cm-yang-provider $NETCONF_LOCAL_PORT:$NETCONF_PORT &
	NETCONF_PID=$!
	sleep 20

	# Create Netconf message to trigger install action
	echo "Sending install keys yang action..."
	echo $action
	/usr/bin/expect <( cat << EOS

	set timeout 180
	
	spawn ssh -o LogLevel=quiet -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -t -p $NETCONF_LOCAL_PORT $NETCONF_USER@localhost -s netconf
	expect "Password:"
	send "$NETCONF_PASS\r"

	sleep 3
	expect "]]>]]>"
	send [ exec cat $action ]
	expect "]]>]]>"
EOS
)
	sleep 2
	if [[ $NO_INSTALL != "true" ]]
	then
		kill -9 $NETCONF_PID
	fi
} 

# Arguments: node name, node fqdn, output-directory

if [ "$1" = "etcd-ut" ]
then
	create_certificate ETCDUT localhost etcd-ut
elif [ "$1" = "osdb" ]
then
	create_certificate OSDB eric object-storage
elif [ "$1" = "chfsim" ] # direct installation via kubectl
then
	create_certificate CHF eric-chfsim chf
	if [[ $NO_INSTALL != "true" ]];
	then
		kubectl -n $KUBE_NAMESPACE delete secret chf-certificates
		kubectl -n $KUBE_NAMESPACE create secret generic chf-certificates --from-file=$KEYS_DIR/chf/key.pem --from-file=$KEYS_DIR/chf/cert.pem --from-file=$ROOTCA_DIR/rootCA.crt
	fi;
elif [ "$1" = "chfsim-sepp" ] # direct installation via kubectl
then
	create_certificate CHFSEPP eric-chfsim chf
	if [[ $NO_INSTALL != "true" ]];
	then
		kubectl -n $KUBE_NAMESPACE delete secret chf-sepp-certificates
		kubectl -n $KUBE_NAMESPACE create secret generic chf-sepp-certificates --from-file=$KEYS_DIR/chf/key.pem --from-file=$KEYS_DIR/chf/cert.pem --from-file=$ROOTCA_DIR/rootCA.crt
	fi;
elif [ "$1" = "chfsim-ci" ] # direct installation via kubectl
then
	create_certificate CHFCI eric-chfsim chf
elif [ "$1" = "nrfsim" ] # direct installation via kubectl
then
	create_certificate NRF eric-nrfsim nrf
	if [[ $NO_INSTALL != "true" ]];
	then
		kubectl -n $KUBE_NAMESPACE delete secret nrf-certificates
		kubectl -n $KUBE_NAMESPACE create secret generic nrf-certificates --from-file=$KEYS_DIR/nrf/key.pem --from-file=$KEYS_DIR/nrf/cert.pem --from-file=$ROOTCA_DIR/rootCA.crt
	fi;
elif [ "$1" = "seppsim" ] # direct installation via kubectl
then
	create_certificate SEPPSIM eric-seppsim seppsim
	create_certificate SEPPSIM eric-seppsim-c seppsim c
	create_certificate SEPPSIM eric-seppsim-p seppsim p
	create_certificate SEPPSIM *.5gc.mnc012.mcc210.3gppnetwork.org seppsim p1
	create_certificate SEPPSIM *.5gc.mnc012.mcc210.3gppnetwork.org seppsim p2
	create_certificate SEPPSIM *.5gc.mnc123.mcc321.3gppnetwork.org seppsim p3
	create_certificate SEPPSIM *.5gc.mnc123.mcc321.3gppnetwork.org seppsim p4
	create_certificate SEPPSIM *.5gc.mnc567.mcc765.3gppnetwork.org seppsim p5
	create_certificate SEPPSIM *.5gc.mnc567.mcc765.3gppnetwork.org seppsim p6
	create_certificate SEPPSIM *.5gc.mnc567.mcc765.3gppnetwork.org seppsim p7
	create_certificate SEPPSIM *.5gc.mnc567.mcc765.3gppnetwork.org seppsim p8
	if [[ $NO_INSTALL != "true" ]];
	then
		kubectl -n $KUBE_NAMESPACE delete secret seppsim-certificates
		kubectl -n $KUBE_NAMESPACE delete secret seppsim-certificates-c
		kubectl -n $KUBE_NAMESPACE delete secret seppsim-certificates-p
		kubectl -n $KUBE_NAMESPACE delete secret seppsim-certificates-p1
		kubectl -n $KUBE_NAMESPACE delete secret seppsim-certificates-p2
		kubectl -n $KUBE_NAMESPACE delete secret seppsim-certificates-p3
		kubectl -n $KUBE_NAMESPACE delete secret seppsim-certificates-p4
		kubectl -n $KUBE_NAMESPACE delete secret seppsim-certificates-p5
		kubectl -n $KUBE_NAMESPACE delete secret seppsim-certificates-p6
		kubectl -n $KUBE_NAMESPACE delete secret seppsim-certificates-p7
		kubectl -n $KUBE_NAMESPACE delete secret seppsim-certificates-p8
		kubectl -n $KUBE_NAMESPACE create secret generic seppsim-certificates --from-file=$KEYS_DIR/seppsim/key.pem --from-file=$KEYS_DIR/seppsim/cert.pem --from-file=$ROOTCA_DIR/rootCA.crt
		kubectl -n $KUBE_NAMESPACE create secret generic seppsim-certificates-c --from-file=key.pem=$KEYS_DIR/seppsim/keyc.pem --from-file=cert.pem=$KEYS_DIR/seppsim/certc.pem --from-file=$ROOTCA_DIR/rootCA.crt
		kubectl -n $KUBE_NAMESPACE create secret generic seppsim-certificates-p --from-file=key.pem=$KEYS_DIR/seppsim/keyp.pem --from-file=cert.pem=$KEYS_DIR/seppsim/certp.pem --from-file=$ROOTCA_DIR/rootCA.crt
		kubectl -n $KUBE_NAMESPACE create secret generic seppsim-certificates-p1 --from-file=key.pem=$KEYS_DIR/seppsim/keyp1.pem --from-file=cert.pem=$KEYS_DIR/seppsim/certp1.pem --from-file=$ROOTCA_DIR/rootCA.crt
		kubectl -n $KUBE_NAMESPACE create secret generic seppsim-certificates-p2 --from-file=key.pem=$KEYS_DIR/seppsim/keyp2.pem --from-file=cert.pem=$KEYS_DIR/seppsim/certp2.pem --from-file=$ROOTCA_DIR/rootCA.crt
		kubectl -n $KUBE_NAMESPACE create secret generic seppsim-certificates-p3 --from-file=key.pem=$KEYS_DIR/seppsim/keyp3.pem --from-file=cert.pem=$KEYS_DIR/seppsim/certp3.pem --from-file=$ROOTCA_DIR/rootCA.crt
		kubectl -n $KUBE_NAMESPACE create secret generic seppsim-certificates-p4 --from-file=key.pem=$KEYS_DIR/seppsim/keyp4.pem --from-file=cert.pem=$KEYS_DIR/seppsim/certp4.pem --from-file=$ROOTCA_DIR/rootCA.crt
		kubectl -n $KUBE_NAMESPACE create secret generic seppsim-certificates-p5 --from-file=key.pem=$KEYS_DIR/seppsim/keyp5.pem --from-file=cert.pem=$KEYS_DIR/seppsim/certp5.pem --from-file=$ROOTCA_DIR/rootCA.crt
		kubectl -n $KUBE_NAMESPACE create secret generic seppsim-certificates-p6 --from-file=key.pem=$KEYS_DIR/seppsim/keyp6.pem --from-file=cert.pem=$KEYS_DIR/seppsim/certp6.pem --from-file=$ROOTCA_DIR/rootCA.crt
		kubectl -n $KUBE_NAMESPACE create secret generic seppsim-certificates-p7 --from-file=key.pem=$KEYS_DIR/seppsim/keyp7.pem --from-file=cert.pem=$KEYS_DIR/seppsim/certp7.pem --from-file=$ROOTCA_DIR/rootCA.crt
		kubectl -n $KUBE_NAMESPACE create secret generic seppsim-certificates-p8 --from-file=key.pem=$KEYS_DIR/seppsim/keyp8.pem --from-file=cert.pem=$KEYS_DIR/seppsim/certp8.pem --from-file=$ROOTCA_DIR/rootCA.crt
	fi;
elif [ "$1" = "seppsim-scp" ] # direct installation via kubectl
then
        create_certificate SEPPSIM eric-seppsim seppsim
        create_certificate SEPPSIM eric-seppsim-c seppsim c
        create_certificate SEPPSIM eric-seppsim-p seppsim p
        create_certificate SEPPSIM eric-seppsim-p1-mcc-206-mnc-33 seppsim p1
        create_certificate SEPPSIM eric-seppsim-p2-mcc-206-mnc-33 seppsim p2
        create_certificate SEPPSIM eric-seppsim-p3-mcc-262-mnc-73 seppsim p3
        create_certificate SEPPSIM eric-seppsim-p4-mcc-262-mnc-73 seppsim p4
        create_certificate SEPPSIM eric-seppsim-p5-mcc-262-mnc-73 seppsim p5
        create_certificate SEPPSIM eric-seppsim-p6-mcc-262-mnc-73 seppsim p6
        create_certificate SEPPSIM eric-seppsim-p7-mcc-262-mnc-73 seppsim p7
        create_certificate SEPPSIM eric-seppsim-p8-mcc-262-mnc-73 seppsim p8
        if [[ $NO_INSTALL != "true" ]];
        then
                kubectl -n $KUBE_NAMESPACE delete secret seppsim-certificates
                kubectl -n $KUBE_NAMESPACE delete secret seppsim-certificates-c
                kubectl -n $KUBE_NAMESPACE delete secret seppsim-certificates-p
                kubectl -n $KUBE_NAMESPACE delete secret seppsim-certificates-p1
                kubectl -n $KUBE_NAMESPACE delete secret seppsim-certificates-p2
                kubectl -n $KUBE_NAMESPACE delete secret seppsim-certificates-p3
                kubectl -n $KUBE_NAMESPACE delete secret seppsim-certificates-p4
                kubectl -n $KUBE_NAMESPACE delete secret seppsim-certificates-p5
                kubectl -n $KUBE_NAMESPACE delete secret seppsim-certificates-p6
                kubectl -n $KUBE_NAMESPACE delete secret seppsim-certificates-p7
                kubectl -n $KUBE_NAMESPACE delete secret seppsim-certificates-p8
                kubectl -n $KUBE_NAMESPACE create secret generic seppsim-certificates --from-file=$KEYS_DIR/seppsim/key.pem --from-file=$KEYS_DIR/seppsim/cert.pem --from-file=$ROOTCA_DIR/rootCA.crt
                kubectl -n $KUBE_NAMESPACE create secret generic seppsim-certificates-c --from-file=key.pem=$KEYS_DIR/seppsim/keyc.pem --from-file=cert.pem=$KEYS_DIR/seppsim/certc.pem --from-file=$ROOTCA_DIR/rootCA.crt
                kubectl -n $KUBE_NAMESPACE create secret generic seppsim-certificates-p --from-file=key.pem=$KEYS_DIR/seppsim/keyp.pem --from-file=cert.pem=$KEYS_DIR/seppsim/certp.pem --from-file=$ROOTCA_DIR/rootCA.crt
                kubectl -n $KUBE_NAMESPACE create secret generic seppsim-certificates-p1 --from-file=key.pem=$KEYS_DIR/seppsim/keyp1.pem --from-file=cert.pem=$KEYS_DIR/seppsim/certp1.pem --from-file=$ROOTCA_DIR/rootCA.crt
                kubectl -n $KUBE_NAMESPACE create secret generic seppsim-certificates-p2 --from-file=key.pem=$KEYS_DIR/seppsim/keyp2.pem --from-file=cert.pem=$KEYS_DIR/seppsim/certp2.pem --from-file=$ROOTCA_DIR/rootCA.crt
                kubectl -n $KUBE_NAMESPACE create secret generic seppsim-certificates-p3 --from-file=key.pem=$KEYS_DIR/seppsim/keyp3.pem --from-file=cert.pem=$KEYS_DIR/seppsim/certp3.pem --from-file=$ROOTCA_DIR/rootCA.crt
                kubectl -n $KUBE_NAMESPACE create secret generic seppsim-certificates-p4 --from-file=key.pem=$KEYS_DIR/seppsim/keyp4.pem --from-file=cert.pem=$KEYS_DIR/seppsim/certp4.pem --from-file=$ROOTCA_DIR/rootCA.crt
                kubectl -n $KUBE_NAMESPACE create secret generic seppsim-certificates-p5 --from-file=key.pem=$KEYS_DIR/seppsim/keyp5.pem --from-file=cert.pem=$KEYS_DIR/seppsim/certp5.pem --from-file=$ROOTCA_DIR/rootCA.crt
                kubectl -n $KUBE_NAMESPACE create secret generic seppsim-certificates-p6 --from-file=key.pem=$KEYS_DIR/seppsim/keyp6.pem --from-file=cert.pem=$KEYS_DIR/seppsim/certp6.pem --from-file=$ROOTCA_DIR/rootCA.crt
                kubectl -n $KUBE_NAMESPACE create secret generic seppsim-certificates-p7 --from-file=key.pem=$KEYS_DIR/seppsim/keyp7.pem --from-file=cert.pem=$KEYS_DIR/seppsim/certp7.pem --from-file=$ROOTCA_DIR/rootCA.crt
                kubectl -n $KUBE_NAMESPACE create secret generic seppsim-certificates-p8 --from-file=key.pem=$KEYS_DIR/seppsim/keyp8.pem --from-file=cert.pem=$KEYS_DIR/seppsim/certp8.pem --from-file=$ROOTCA_DIR/rootCA.crt
        fi;
elif [ "$1" = "diameter" ]
then
	#create_certificate DIAM eric-data-wide-column-database-cd diameter
	create_certificate DIAM bsf.ericsson.se diameter
	template=$(sed "s|container-base64.p12|$(cat $KEYS_DIR/diameter/container-base64.p12)|; s|pkcs12password|$GLOBAL_PASSWORD|" $DIAMETER_INSTALL_RPC)
	echo $template > $KEYS_DIR/diameter/rpc.netconf
	install_action $KEYS_DIR/diameter/rpc.netconf
elif [ "$1" = "dscload" ] # direct installation via kubectl
then
	create_certificate DSCLOAD eric-dscload dscload
	kubectl -n $KUBE_NAMESPACE delete secret dscload-certificates-secret
	kubectl -n $KUBE_NAMESPACE create secret generic dscload-certificates-secret --from-file=$KEYS_DIR/dscload/key.pem --from-file=$KEYS_DIR/dscload/cert.pem
elif [ "$1" = "bsf" ]
then
	create_certificate BSFMGR bsf.ericsson.se bsfmgr
	template=$(sed "s|container-base64.p12|$(cat $KEYS_DIR/bsfmgr/container-base64.p12)|; s|pkcs12password|$GLOBAL_PASSWORD|" $BSF_MANAGER_INSTALL_RPC)
	echo $template > $KEYS_DIR/bsfmgr/rpc.netconf
	install_action $KEYS_DIR/bsfmgr/rpc.netconf

	create_certificate BSFWRK bsf.ericsson.se bsfwrk
	template=$(sed "s|container-base64.p12|$(cat $KEYS_DIR/bsfwrk/container-base64.p12)|; s|pkcs12password|$GLOBAL_PASSWORD|" $BSF_WORKER_INSTALL_RPC)
	echo $template > $KEYS_DIR/bsfwrk/rpc.netconf
	install_action $KEYS_DIR/bsfwrk/rpc.netconf
elif [ "$1" = "csamgr" ]
then
 	create_certificate CSAMGR csa.ericsson.se csamgr
	template=$(sed "s|container-base64.p12|$(cat $KEYS_DIR/csamgr/container-base64.p12)|; s|pkcs12password|$GLOBAL_PASSWORD|" $CSA_MANAGER_INSTALL_RPC)
	echo $template > $KEYS_DIR/csamgr/rpc.netconf
	install_action $KEYS_DIR/csamgr/rpc.netconf
elif [ "$1" = "csamgr-direct" ] # direct installation via kubectl
then
    create_certificate CSAMGR csa.ericsson.se csamgr
    mv $KEYS_DIR/csamgr/key.pem $KEYS_DIR/csamgr/tls.key
    mv $KEYS_DIR/csamgr/cert.pem $KEYS_DIR/csamgr/tls.crt
    kubectl -n $KUBE_NAMESPACE delete secret nrf-cert-secret
    kubectl -n $KUBE_NAMESPACE create secret generic nrf-cert-secret --from-file=$KEYS_DIR/csamgr/tls.key --from-file=$KEYS_DIR/csamgr/tls.crt
    kubectl -n $KUBE_NAMESPACE delete secret trusted-cas-secret
    kubectl -n $KUBE_NAMESPACE create secret generic trusted-cas-secret --from-file=cert1.pem=$ROOTCA_DIR/rootCA.crt
    kubectl -n $KUBE_NAMESPACE describe secret trusted-cas-secret
elif [ "$1" = "wcdb" ]
then
	create_certificate WCDB eric-data-wide-column-database-cd wcdb/internode
	template=$(sed "s|container-base64.p12|$(cat $KEYS_DIR/wcdb/internode/container-base64.p12)|; s|pkcs12password|$GLOBAL_PASSWORD|" $WCDB_INTERNODE_INSTALL_RPC)
	echo $template > $KEYS_DIR/wcdb/internode/rpc.netconf
	install_action $KEYS_DIR/wcdb/internode/rpc.netconf

	create_certificate WCDB eric-data-wide-column-database-cd wcdb/client
	template=$(sed "s|container-base64.p12|$(cat $KEYS_DIR/wcdb/client/container-base64.p12)|; s|pkcs12password|$GLOBAL_PASSWORD|" $WCDB_CLIENT_INSTALL_RPC)
	echo $template > $KEYS_DIR/wcdb/client/rpc.netconf
	install_action $KEYS_DIR/wcdb/client/rpc.netconf

	create_certificate WCDB eric-data-wide-column-database-cd wcdb/server
	template=$(sed "s|container-base64.p12|$(cat $KEYS_DIR/wcdb/server/container-base64.p12)|; s|pkcs12password|$GLOBAL_PASSWORD|" $WCDB_SERVER_INSTALL_RPC)
	echo $template > $KEYS_DIR/wcdb/server/rpc.netconf
	install_action $KEYS_DIR/wcdb/server/rpc.netconf
elif [ "$1" = "wcdbd0" ]
then
	#create_certificate WCDB eric-data-wide-column-database-cd wcdb/day0/internode
	#create_certificate WCDB eric-data-wide-column-database-cd wcdb/day0/client
	#create_certificate WCDB eric-data-wide-column-database-cd wcdb/day0/server

	internode64P12=$(cat $KEYS_DIR/wcdb/day0/internode/container-base64.p12)
	server64P12=$(cat $KEYS_DIR/wcdb/day0/server/container-base64.p12)
	clientP12=$(cat $KEYS_DIR/wcdb/day0/client/container-base64.p12)

	rootcaCrt=$(sed '1d;$d' $ROOTCA_DIR/rootCA.crt | tr --delete '\n')

	jsonsecret=$(sed "s|internode-pkcs12|$internode64P12|; s|client-pkcs12|$clientP12|; s|server-pkcs12|$server64P12|; s|rootca|$rootcaCrt|; s|passwrd|$GLOBAL_PASSWORD|" $RESOURCES_DIR/eric-sec-certm-deployment-configuration.json)
	echo $jsonsecret > $KEYS_DIR/wcdb/day0/eric-sec-certm-deployment-configuration.json
elif [ "$1" = "scpmgr" ]
then
 	create_certificate SCPMGR csa.ericsson.se scpmgr
	template=$(sed "s|container-base64.p12|$(cat $KEYS_DIR/scpmgr/container-base64.p12)|; s|pkcs12password|$GLOBAL_PASSWORD|" $SCP_MANAGER_INSTALL_RPC)
	echo $template > $KEYS_DIR/scpmgr/rpc.netconf
	install_action $KEYS_DIR/scpmgr/rpc.netconf
elif [ "$1" = "scpmgr-direct" ] # direct installation via kubectl
then
    create_certificate SCPMGR csa.ericsson.se scpmgr
    mv $KEYS_DIR/scpmgr/key.pem $KEYS_DIR/scpmgr/tls.key
    mv $KEYS_DIR/scpmgr/cert.pem $KEYS_DIR/scpmgr/tls.crt
    kubectl -n $KUBE_NAMESPACE delete secret nrf-cert-secret
    kubectl -n $KUBE_NAMESPACE create secret generic nrf-cert-secret --from-file=$KEYS_DIR/scpmgr/tls.key --from-file=$KEYS_DIR/scpmgr/tls.crt
    kubectl -n $KUBE_NAMESPACE delete secret trusted-cas-secret
    kubectl -n $KUBE_NAMESPACE create secret generic trusted-cas-secret --from-file=cert1.pem=$ROOTCA_DIR/rootCA.crt
    kubectl -n $KUBE_NAMESPACE describe secret trusted-cas-secret
elif [ "$1" = "transformer" ]
then
	echo "Create and install certificate/key for log-transformer syslog interface"
 	create_certificate TRANSFORMER eric-log-transformer transformer
	template=$(sed "s|container-base64.p12|$(cat $KEYS_DIR/transformer/container-base64.p12)|; s|pkcs12password|$GLOBAL_PASSWORD|" $TRANSFORMER_INSTALL_RPC)
	echo $template > $KEYS_DIR/transformer/rpc.netconf
	install_action $KEYS_DIR/transformer/rpc.netconf
elif [ "$1" = "csawrk" ]
then
 	create_certificate CSAWRK csa.ericsson.se csawrk
	template=$(sed "s|container-base64.p12|$(cat $KEYS_DIR/csawrk/container-base64.p12)|; s|pkcs12password|$GLOBAL_PASSWORD|" $CSA_WORKER_INSTALL_RPC)
	echo $template > $KEYS_DIR/csawrk/rpc.netconf
	if [[ $NO_INSTALL != "true" ]];
	then
	    install_action $KEYS_DIR/csawrk/rpc.netconf
    fi;
elif [ "$1" = "scpwrk" ]
then
 	create_certificate SCPWRK scp.ericsson.se scpwrk
	template=$(sed "s|container-base64.p12|$(cat $KEYS_DIR/scpwrk/container-base64.p12)|; s|pkcs12password|$GLOBAL_PASSWORD|" $SCP_WORKER_INSTALL_RPC)
	echo $template > $KEYS_DIR/scpwrk/rpc.netconf
	if [[ $NO_INSTALL != "true" ]];
	then
	    install_action $KEYS_DIR/scpwrk/rpc.netconf
    fi;
elif [ "$1" = "seppmgr" ]
then
    create_certificate SEPPMGR csa.ericsson.se seppmgr
    template=$(sed "s|container-base64.p12|$(cat $KEYS_DIR/seppmgr/container-base64.p12)|; s|pkcs12password|$GLOBAL_PASSWORD|" $SEPP_MANAGER_INSTALL_RPC)
    echo $template > $KEYS_DIR/seppmgr/rpc.netconf
    install_action $KEYS_DIR/seppmgr/rpc.netconf
elif [ "$1" = "seppmgr-direct" ] # direct installation via kubectl
then
    create_certificate SEPPMGR csa.ericsson.se seppmgr
    mv $KEYS_DIR/seppmgr/key.pem $KEYS_DIR/seppmgr/tls.key
    mv $KEYS_DIR/seppmgr/cert.pem $KEYS_DIR/seppmgr/tls.crt
    kubectl -n $KUBE_NAMESPACE delete secret nrf-cert-secret
    kubectl -n $KUBE_NAMESPACE create secret generic nrf-cert-secret --from-file=$KEYS_DIR/seppmgr/tls.key --from-file=$KEYS_DIR/seppmgr/tls.crt
    kubectl -n $KUBE_NAMESPACE delete secret trusted-cas-secret
    kubectl -n $KUBE_NAMESPACE create secret generic trusted-cas-secret --from-file=cert1.pem=$ROOTCA_DIR/rootCA.crt
    kubectl -n $KUBE_NAMESPACE describe secret trusted-cas-secret
elif [ "$1" = "seppwrk" ]
then
	create_certificate SEPPWRK-B sepp.ericsson.se seppwrk-B
	create_certificate SEPPWRK-A sepp.5gc.mnc567.mcc765.3gppnetwork.org seppwrk-A
	template=$(sed "s|container-base64-A.p12|$(cat $KEYS_DIR/seppwrk-A/container-base64.p12)|; s|container-base64-B.p12|$(cat $KEYS_DIR/seppwrk-B/container-base64.p12)|; s|pkcs12password|$GLOBAL_PASSWORD|" $SEPP_WORKER_INSTALL_RPC)
	
	echo $template > $KEYS_DIR/seppwrk-A/rpc.netconf
	install_action $KEYS_DIR/seppwrk-A/rpc.netconf
	
	template=$(sed "s|certificate-authority1|$(cat $ROOTCA_DIR/rootCA-base64.p12)|; s|certificate-authority2|$(cat $ROOTCA_DIR/rootCAsepp-base64.p12)|" $SEPP_TRUST_INSTALL_RPC)
	echo $template > $ROOTCA_DIR/sepprootCArpc.netconf
	install_action $ROOTCA_DIR/sepprootCArpc.netconf

elif [ "$1" = "slf-direct" ] # direct installation via kubectl
then
	create_certificate SLF eric-sc-slf slf
	mv $KEYS_DIR/slf/key.pem $KEYS_DIR/slf/tls.key
	mv $KEYS_DIR/slf/cert.pem $KEYS_DIR/slf/tls.crt
	kubectl -n $KUBE_NAMESPACE delete secret slf-nrf-cert-secret
	kubectl -n $KUBE_NAMESPACE create secret generic slf-nrf-cert-secret --from-file=$KEYS_DIR/slf/tls.key --from-file=$KEYS_DIR/slf/tls.crt
elif [ "$1" = "slf" ]
then
	create_certificate SLF eric-sc-slf slf
	template=$(sed "s|container-base64.p12|$(cat $KEYS_DIR/slf/container-base64.p12)|; s|pkcs12password|$GLOBAL_PASSWORD|" $SLF_INSTALL_RPC)
	echo $template > $KEYS_DIR/slf/rpc.netconf
	install_action $KEYS_DIR/slf/rpc.netconf
elif [ "$1" = "k6" ]
then
	create_certificate K6 scp.ericsson.se k6 
	create_certificate K6 sepp.5gc.mnc567.mcc765.3gppnetwork.org k6 sepp
	create_certificate K6 pSepp11.5gc.mnc012.mcc210.3gppnetwork.org k6 rp1
	create_certificate K6 pSepp21.5gc.mnc123.mcc321.3gppnetwork.org k6 rp2
	create_certificate K6 pSepp31.5gc.mnc234.mcc432.3gppnetwork.org k6 rp3
elif [ "$1" = "bsfload" ]
then
	create_certificate bsfload eric-sc-bsfload bsfload
	mv $KEYS_DIR/bsfload/key.pem $KEYS_DIR/bsfload/tls.key
	mv $KEYS_DIR/bsfload/cert.pem $KEYS_DIR/bsfload/tls.crt
	kubectl -n $KUBE_NAMESPACE create secret generic bsfload-cert-secret --from-file=$KEYS_DIR/bsfload/tls.key --from-file=$KEYS_DIR/bsfload/tls.crt
elif [ "$1" = "syslog" ]
then
	echo "Create certificate/key for external devtool/syslog"
	create_certificate SYSLOG eric-syslog syslog
	template=$(sed "s|container-base64.p12|$(cat $KEYS_DIR/syslog/container-base64.p12)|; s|pkcs12password|$GLOBAL_PASSWORD|" $SYSLOG_INSTALL_RPC)
	echo $template > $KEYS_DIR/syslog/rpc.netconf
	install_action $KEYS_DIR/syslog/rpc.netconf
elif [ "$1" = "nbi" ]
then
	echo "Create and install certificate/key for NBI eric-sc-cs-nbi"
	create_certificate NBI nbi.ericsson.se nbi
	template=$(sed "s|container-base64.p12|$(cat $KEYS_DIR/nbi/container-base64.p12)|; s|pkcs12password|$GLOBAL_PASSWORD|" $NBI_INSTALL_RPC)
	echo $template > $KEYS_DIR/nbi/rpc.netconf
	install_action $KEYS_DIR/nbi/rpc.netconf
elif [ "$1" = "rootca" ]
then
#	create_certificate rootca csa.ericsson.se 
#	echo "Create root CA for generic SC functions"
	template=$(sed "s|certificate-authority|$(cat $ROOTCA_DIR/rootCA-base64.p12)|" $TRUST_INSTALL_RPC)
	echo $template > $ROOTCA_DIR/rootCArpc.netconf
	install_action $ROOTCA_DIR/rootCArpc.netconf
elif [ "$1" = "internal-ldap" ]
then
	echo "Create and install certificate/key for internal LDAP (eric-sec-ldap-server)"
	create_certificate INTERNALLDAP eric-sec-ldap-server ldap
	template=$(sed "s|container-base64.p12|$(cat $KEYS_DIR/ldap/container-base64.p12)|; s|pkcs12password|$GLOBAL_PASSWORD|" $LDAP_INSTALL_RPC)
	echo $template > $KEYS_DIR/ldap/rpc.netconf
	install_action $KEYS_DIR/ldap/rpc.netconf
elif [ "$1" = "referral-ldap" ]
then
	echo "Create certificate/key for external devtool/referral-ldap"
	create_certificate EXTERNALLDAP eric-referral-ldap referral-ldap
	template=$(sed "s|container-base64.p12|$(cat $KEYS_DIR/referral-ldap/container-base64.p12)|; s|pkcs12password|$GLOBAL_PASSWORD|" $REFERRAL_LDAP_INSTALL_RPC)
	echo $template > $KEYS_DIR/referral-ldap/rpc.netconf
	install_action $KEYS_DIR/referral-ldap/rpc.netconf
elif [ "$1" = "pm_remote_write" ]
then
	echo "Create and install certificate/key for PM Remote Write (pm_remote_write)"
	create_certificate PM_REMOTE_WRITE pm_remote_write pm_remote_write
	template=$(sed "s|container-base64.p12|$(cat $KEYS_DIR/pm_remote_write/container-base64.p12)|; s|pkcs12password|$GLOBAL_PASSWORD|" $PM_REMOTE_WRITE_INSTALL_RPC)
	echo $template > $KEYS_DIR/pm_remote_write/rpc.netconf
	install_action $KEYS_DIR/pm_remote_write/rpc.netconf
elif [ "$1" = "influxdb" ]
then
	echo "Create certificate/key for external devtool/influxdb"
	create_certificate INFLUXDB eric-influxdb influxdb
	template=$(sed "s|container-base64.p12|$(cat $KEYS_DIR/influxdb/container-base64.p12)|; s|pkcs12password|$GLOBAL_PASSWORD|" $INFLUXDB_INSTALL_RPC)
	echo $template > $KEYS_DIR/influxdb/rpc.netconf
	install_action $KEYS_DIR/influxdb/rpc.netconf
elif [ "$1" = "cleanup" ]
then
	echo "Cleanup of all certificates"
	cleanup_certificates
else
	echo "usage: $me bsf|diameter|dscload|chfsim|chfsim-ci|chfsim-sepp|nbi|csamgr|csawrk|k6|bsfload|nrfsim|rootca|scpmgr|scpmgr-direct|scpwrk|seppmgr|seppmgr-direct|seppwrk|slf-direct|syslog|transformer|internal-ldap|referral-ldap|pm_remote_write|influxdb"
	exit -1
fi
fix_permissions
