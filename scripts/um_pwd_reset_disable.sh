#!/bin/bash -x

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

# Reset password prompt after first login
echo "Manually modify default users to avoid password reset after first login"

delay=5;
retries=3;

# Get the LDAP pods that are ready
expectedLdapPods=$(kubectl --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE get statefulset eric-sec-ldap-server -o jsonpath="{.status.replicas}");
readyLdapPods=$(kubectl --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE get statefulset eric-sec-ldap-server -o jsonpath="{.status.readyReplicas}");
if [ ${readyLdapPods} == ${expectedLdapPods} ]; then
	echo "INFO: All LDAP pods are ready!";
else
	echo "WARNING: Only ${readyLdapPods} pods are ready"
fi

# For each LDAP pod that is ready: execute and apply the ldap reset file via command ldapmodify
for (( podNumber=0; podNumber<${readyLdapPods}; podNumber++ )); do

	sleep 10;

	kubectl --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE exec eric-sec-ldap-server-${podNumber} -c ldap -- mkdir /opt/ldap/tmp
	kubectl --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE cp ../scripts/default_users/pwd_reset_disable.ldif eric-sec-ldap-server-${podNumber}:/opt/ldap/tmp/pwd_reset_disable.ldif -c ldap

	echo "----Verifying that it was pushed to ldap!";

	for (( trialNum=0; trialNum<${readyLdapPods}; trialNum++ )); do
		sleep ${delay}
		echo "WARNING: Trying to copy the pwd_reset_disable.ldif in eric-sec-ldap-server-${podNumber}/ldap container...This is attempt #${trialNum}";
		kubectl --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE exec -it eric-sec-ldap-server-${podNumber} -c ldap -- ls /opt/ldap/tmp | grep pwd_reset_disable.ldif
		result=$?
		if [ $result -eq 0 ]; then
			echo "File was pushed in ldap!";
			break;
		else 
			echo "WARNING: File was not pushed in ldap. Retrying...";
			kubectl --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE cp ../scripts/default_users/pwd_reset_disable.ldif eric-sec-ldap-server-${podNumber}:/opt/ldap/tmp/pwd_reset_disable.ldif -c ldap
		fi
	done

	echo "trial number is ${trialNum}"
	# If reties are not enough to copy the file, "trialNum" is equal to retries+1
	if [ ${trialNum} -gt ${retries} ]; then
		echo "ERROR: Unable to verify the pwd_reset_disable.ldif";
		exit 1
	fi

	kubectl --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE exec -it eric-sec-ldap-server-${podNumber} -c ldap -- ldapmodify -Y EXTERNAL -H ldapi://%2Frun%2Fslapd%2Fslapd.sock -f /opt/ldap/tmp/pwd_reset_disable.ldif
	result=$?

	# Exit code 16 means that the action has already been applied before and there are not reset flags present in current users
	if [ $result -eq 16 ]; then
		if [ ${podNumber} -eq 0 ]; then
			continue;
		else	
			exit 0
		fi
	fi
	
	# Check if the pwdReset flag has been deleted from each user
	# Copy the "pwdReset" flags of the users in pwdReset.txt. If the pwd_reset_disable.ldif has been applied successfully,
	# the created file must empty
	kubectl --kubeconfig $KUBE_CONFIG --namespace $KUBE_NAMESPACE exec -it eric-sec-ldap-server-${podNumber} -- ldapsearch -Y EXTERNAL -H ldapi://%2Frun%2Fslapd%2Fslapd.sock -b "ou=people,dc=la,dc=adp,dc=ericsson" pwdReset | grep "pwdReset: TRUE" > /local/${USER}/git/5g_proto/scripts/pwdReset.txt

	echo "pwdReset.txt created"

	if [ ! -z /local/${USER}/git/5g_proto/scripts/pwdReset.txt ]; then # check if the file has been created
		if [ -s /local/${USER}/git/5g_proto/scripts/pwdReset.txt ]; then  # check if the file is empty
			echo "INFO: The pwdReset flags of the users have been deleted successfully!"
		fi 	
		rm -rf /local/${USER}/git/5g_proto/scripts/pwdReset.txt;
	fi	

done
	

#k exec eric-sec-ldap-server-0 -c ldap -- mkdir /opt/ldap/tmp
#k cp ../scripts/default_users/pwd_reset_disable.ldif eric-sec-ldap-server-0:/opt/ldap/tmp/pwd_reset_disable.ldif -c ldap
#k exec -it eric-sec-ldap-server-0 -c ldap -- ldapmodify -Y EXTERNAL -H ldapi://%2Frun%2Fslapd%2Fslapd.sock -f /opt/ldap/tmp/pwd_reset_disable.ldif

# k exec -it eric-sec-ldap-server-1 -- ldapsearch -Y EXTERNAL -H ldapi://%2Frun%2Fslapd%2Fslapd.sock -b "ou=people,dc=la,dc=adp,dc=ericsson" pwdReset 