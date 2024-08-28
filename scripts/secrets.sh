#!/bin/bash

if [ ! -z .bob/var.namespace ];
then
	export KUBE_NAMESPACE=`cat .bob/var.namespace`
else
	export KUBE_NAMESPACE="5g-bsf-${USER}"
fi

# Generate/Remove secrets which are not included in the umbrella helm chart

echo "KUBE config: ${KUBECONFIG}"	
echo "NAMESPACE: ${KUBE_NAMESPACE}"

create_secrets(){

    delete_secrets
    # kubectl --namespace $KUBE_NAMESPACE create -f scripts/external_secrets/pg_secret.yaml # remove pg secret - not needed for mTLS
    # kubectl --kubeconfig ${KUBECONFIG} --namespace $KUBE_NAMESPACE create -f scripts/external_secrets/object-storage-secret.yaml
    # kubectl --kubeconfig ${KUBECONFIG} --namespace $KUBE_NAMESPACE create -f scripts/external_secrets/sftp-bragent-secret.yaml
    # kubectl --kubeconfig ${KUBECONFIG} --namespace $KUBE_NAMESPACE create -f scripts/external_secrets/distributed-coordinator-sc-secret.yaml # remove dced secret - not needed for mTLS
    # kubectl --kubeconfig ${KUBECONFIG} --namespace $KUBE_NAMESPACE create -f scripts/external_secrets/distributed-coordinator-secret.yaml # remove dced secret - not needed for mTLS
    
    # Add snmp trap secret
    kubectl --kubeconfig ${KUBECONFIG} --namespace $KUBE_NAMESPACE create secret generic snmp-alarm-provider-config --from-file=scripts/external_secrets/config.json
    
    # Add day-0 admin user
    kubectl --kubeconfig ${KUBECONFIG} --namespace $KUBE_NAMESPACE create -f scripts/external_secrets/ldap-secret.yaml
    
    # Add BSF Cassandra secrets
    kubectl --kubeconfig ${KUBECONFIG} --namespace $KUBE_NAMESPACE create -f scripts/external_secrets/bsf-wcdbcd-admin-secret.yaml
    kubectl --kubeconfig ${KUBECONFIG} --namespace $KUBE_NAMESPACE create -f scripts/external_secrets/bsf-wcdbcd-day0-secret.yaml
    kubectl --kubeconfig ${KUBECONFIG} --namespace $KUBE_NAMESPACE create -f scripts/external_secrets/bsf-db-secret.yaml
    
    # Add SC Diameter Cassandra secrets
    kubectl --kubeconfig ${KUBECONFIG} --namespace $KUBE_NAMESPACE create -f scripts/external_secrets/sc-diameter-wcdbcd-admin-secret.yaml
    kubectl --kubeconfig ${KUBECONFIG} --namespace $KUBE_NAMESPACE create -f scripts/external_secrets/sc-diameter-wcdbcd-day0-secret.yaml
    kubectl --kubeconfig ${KUBECONFIG} --namespace $KUBE_NAMESPACE create -f scripts/external_secrets/sc-diameter-db-secret.yaml
    
    # Add SC Monitor external user secret
    kubectl --kubeconfig ${KUBECONFIG} --namespace $KUBE_NAMESPACE create -f scripts/external_secrets/monitor-secret.yaml
    
    # Add SC oam user secret (NBI CNOM)
    kubectl --kubeconfig ${KUBECONFIG} --namespace $KUBE_NAMESPACE create -f scripts/external_secrets/sc-oam-user-secret.yaml
    
    # Add day-0 certificate secret (optional)
    if [ -f ".bob/eric-sec-certm-deployment-configuration.json" ] ; then
        kubectl --kubeconfig ${KUBECONFIG} --namespace $KUBE_NAMESPACE create secret generic eric-sec-certm-deployment-configuration --from-file=.bob/eric-sec-certm-deployment-configuration.json
    fi

}

delete_secrets(){

	secretsBefore=($(kubectl -- kubeconfig  ${KUBECONFIG} --namespace $KUBE_NAMESPACE get secret | tail -n +2 | awk '{print $1}'))
    echo "Starting the deletion of ${#secretsBefore[@]} secrets."
    kubectl --kubeconfig ${KUBECONFIG} --namespace $KUBE_NAMESPACE delete secret -l com.ericsson.sec.tls/created-by=eric-sec-sip-tls    
    
    # temporary cleanup pg secret to support upgrade procedures
    kubectl --kubeconfig ${KUBECONFIG} --namespace $KUBE_NAMESPACE delete secret eric-data-document-database-pg-sc
    
    # temporary cleanup dced secrets to support upgrade procedures
    kubectl --kubeconfig ${KUBECONFIG} --namespace $KUBE_NAMESPACE delete secret eric-data-distributed-coordinator-creds
    kubectl --kubeconfig ${KUBECONFIG} --namespace $KUBE_NAMESPACE delete secret eric-data-distributed-coordinator-creds-sc
    
    # temporary cleanup osmn secrets to support upgrade procedures
    kubectl --namespace $KUBE_NAMESPACE delete secret object-storage-secret
    
    kubectl --kubeconfig ${KUBECONFIG} --namespace $KUBE_NAMESPACE delete secret eric-sc-monitor-secret
    kubectl --kubeconfig ${KUBECONFIG} --namespace $KUBE_NAMESPACE delete secret snmp-alarm-provider-config
    kubectl --kubeconfig ${KUBECONFIG} --namespace $KUBE_NAMESPACE delete secret eric-sec-ldap-server-creds
    kubectl --kubeconfig ${KUBECONFIG} --namespace $KUBE_NAMESPACE delete secret nbi-cert-secret
    kubectl --kubeconfig ${KUBECONFIG} --namespace $KUBE_NAMESPACE delete secret scp-traf-cert-secret
    kubectl --kubeconfig ${KUBECONFIG} --namespace $KUBE_NAMESPACE delete secret sepp-traf-cert-secret
    kubectl --kubeconfig ${KUBECONFIG} --namespace $KUBE_NAMESPACE delete secret trusted-cas-secret
    kubectl --kubeconfig ${KUBECONFIG} --namespace $KUBE_NAMESPACE delete secret scp-nrf-cert-secret
    kubectl --kubeconfig ${KUBECONFIG} --namespace $KUBE_NAMESPACE delete secret sepp-nrf-cert-secret
    kubectl --kubeconfig ${KUBECONFIG} --namespace $KUBE_NAMESPACE delete secret eric-sc-nlf-dced-client-certificate eric-sc-nlf-dced-client-certificate-emergency eric-sc-nlf-server-certificate eric-sc-nlf-server-certificate-emergency eric-sc-nlf-tls-client-ca-secret eric-sc-nlf-mediator-client-certificate nlf-mediator-client-if-certificate-secret-emergency nlf-oam-server-if-certificate-secret nlf-oam-server-if-certificate-secret-emergency
    kubectl --kubeconfig ${KUBECONFIG} --namespace $KUBE_NAMESPACE delete secret slf-nrf-cert-secret
    kubectl --kubeconfig ${KUBECONFIG} --namespace $KUBE_NAMESPACE delete secret eric-data-wide-column-database-cd-admin-creds
    kubectl --kubeconfig ${KUBECONFIG} --namespace $KUBE_NAMESPACE delete secret eric-data-wide-column-database-cd-day0-creds
    kubectl --kubeconfig ${KUBECONFIG} --namespace $KUBE_NAMESPACE delete secret eric-bsf-db-creds
    kubectl --kubeconfig ${KUBECONFIG} --namespace $KUBE_NAMESPACE delete secret bsf-nrf-cert-secret
    kubectl --kubeconfig ${KUBECONFIG} --namespace $KUBE_NAMESPACE delete secret bsf-traf-cert-secret
    kubectl --kubeconfig ${KUBECONFIG} --namespace $KUBE_NAMESPACE delete secret syslog-cert-secret 
    kubectl --kubeconfig ${KUBECONFIG} --namespace $KUBE_NAMESPACE delete secret csa-nrf-cert-secret
    kubectl --kubeconfig ${KUBECONFIG} --namespace $KUBE_NAMESPACE delete secret csa-traf-cert-secret
    kubectl --kubeconfig ${KUBECONFIG} --namespace $KUBE_NAMESPACE delete secret eric-sc-oam-user-secret
    kubectl --kubeconfig ${KUBECONFIG} --namespace $KUBE_NAMESPACE delete secret eric-sec-certm-deployment-configuration
    
    secretsAfter=($(kubectl --kubeconfig ${KUBECONFIG} --namespace $KUBE_NAMESPACE get secret | tail -n +2 | awk '{print $1}'))
    if [ ${#secretsAfter[@]} -gt 0 ]; then
    	echo "Unable to delete ${#secretsAfter[@]} secrets!"
    	echo ${secretsAfter[@]}
    fi
}

# Arguments: Node name, node fqdn output-directory
if [ "$1" = "create" ]
then
	create_secrets	
elif [ "$1" = "delete" ]
then
	delete_secrets
else
  	echo "Unsupported argument!"
fi
