#!/bin/bash

echo "========================================"
echo "===== Custom Resource Definitions ======"
echo "========================================"
	
if [ ! -z .bob/var.namespace ]; then
	export KUBE_NAMESPACE=`cat .bob/var.namespace`
else
	export KUBE_NAMESPACE="5g-bsf-${USER}"
fi
echo "Selected namespace ${KUBE_NAMESPACE}"

if [[ $SKIP_CRDS == "true" ]]; then
	echo "WARNING: NO CRDs will be installed"
	exit 0;
fi;

echo "===== SIP-TLS CRDs configuration ======"
##### SIP-TLS #####
# sip-tls version from requirements.yaml [Format: <Major>.<Minor>.<Patch>(+/-)<Version>]
sip_tls_version=$(grep -m 1 -A 2 "eric-sec-sip-tls" esc/helm/eric-sc-umbrella/requirements.yaml | tail -n +3 | sed 's/  version\: //;s/ *$//g')
echo "Identified SIP-TLS version ${sip_tls_version} in current sc umbrella chart"

# expected downloaded location of sip-tls chart
charts_pwd=".bob/eric-sc-umbrella/charts"
sip_tls_pwd=".bob/eric-sc-umbrella/charts/eric-sec-sip-tls-"${sip_tls_version}".tgz"
echo "Prerequisite for installing SIP-TLS crds is the existence of file ${sip_tls_pwd}"

# expected sip-tls crds
exp_sip_tls_crds=("certificateauthorities.com.ericsson.sec.tls" "clientcertificates.com.ericsson.sec.tls" "servercertificates.com.ericsson.sec.tls" "internalusercas.siptls.sec.ericsson.com" "internalcertificates.siptls.sec.ericsson.com")
echo "Expecting ${#exp_sip_tls_crds[@]} SIP-TLS CRDs:"
for (( i=0; i<${#exp_sip_tls_crds[@]}; i++ )); do
	echo " $((i+1)). ${exp_sip_tls_crds[i]}"
done

# sip-tls artifactory
artifactory=$(grep -m 1 -A 2 "eric-sec-sip-tls" esc/helm/eric-sc-umbrella/requirements.yaml | head -n 2 | tail -1 | sed 's/.*:\/\///;s/\/\//\//')
sip_tls_artifactory="https://"${artifactory}"eric-sec-sip-tls/eric-sec-sip-tls-"${sip_tls_version}".tgz"
echo "Backup image to be used for the installation of SIP-TLS crds: ${sip_tls_artifactory}"

echo "===== ICCR CRDs configuration ========="
##### ICCR #####
# iccr version from requirements.yaml [Format: <Major>.<Minor>.<Patch>(+/-)<Version>]
iccr_version=$(grep -m 1 -A 2 "eric-tm-ingress-controller-cr" esc/helm/eric-sc-umbrella/requirements.yaml | tail -n +3 | sed 's/  version\: //;s/ *$//g')
echo "Identified ICCR version ${iccr_version} in current sc umbrella chart"

# expected downloaded location of iccr chart
iccr_pwd=".bob/eric-sc-umbrella/charts/eric-tm-ingress-controller-cr-"${iccr_version}".tgz"
echo "Prerequisite for installing ICCR crds is the existence of file ${iccr_pwd}"

# expected iccr crds
exp_iccr_crds=("ingressroutes.contour.heptio.com" "tlscertificatedelegations.projectcontour.io" "httpproxies.projectcontour.io" "tlscertificatedelegations.contour.heptio.com")
echo "Expecting ${#exp_iccr_crds[@]} ICCR CRDs:"
for (( i=0; i<${#exp_iccr_crds[@]}; i++ )); do
	echo " $((i+1)). ${exp_iccr_crds[i]}"
done

# sip-tls artifactory
artifactory=$(grep -m 1 -A 2 "eric-tm-ingress-controller-cr" esc/helm/eric-sc-umbrella/requirements.yaml | head -n 2 | tail -1 | sed 's/.*:\/\///;s/\/\//\//')
iccr_artifactory="https://"${artifactory}"eric-tm-ingress-controller-cr/eric-tm-ingress-controller-cr-"${iccr_version}".tgz"
echo "Backup image to be used for the installation of ICCR crds: ${iccr_artifactory}"

echo "===== CERTM CRDs configuration ========"
##### CERTM #####
# certm version from requirements.yaml [Format: <Major>.<Minor>.<Patch>(+/-)<Version>]
certm_version=$(grep -m 1 -A 2 "eric-sec-certm" esc/helm/eric-sc-umbrella/requirements.yaml | tail -n +3 | sed 's/  version\: //;s/ *$//g')
echo "Identified CERTM version ${certm_version} in current sc umbrella chart"

# expected downloaded location of certm chart
certm_pwd=".bob/eric-sc-umbrella/charts/eric-sec-certm-"${certm_version}".tgz"
echo "Prerequisite for installing CERTM crds is the existence of file ${certm_pwd}"

# expected certm crds
exp_certm_crds=("externalcertificates.certm.sec.ericsson.com" "externalcertificates.com.ericsson.sec.certm")
echo "Expecting ${#exp_certm_crds[@]} CERTM CRDs:"
for (( i=0; i<${#exp_certm_crds[@]}; i++ )); do
	echo " $((i+1)). ${exp_certm_crds[i]}"
done

# certm artifactory
artifactory=$(grep -m 1 -A 2 "eric-sec-certm" esc/helm/eric-sc-umbrella/requirements.yaml | head -n 2 | tail -1 | sed 's/.*:\/\///;s/\/\//\//')
#certm_crd_artifactory="https://"${artifactory}"eric-sec-certm-crd"
certm_artifactory="https://"${artifactory}"eric-sec-certm/eric-sec-certm-"${certm_version}".tgz"
echo "Backup image to be used for the installation of CERTM crds: ${certm_artifactory}"

# namespace to be used for the installation of crds
if [ -z "$1" ]; then
	crd_namespace=$1
else
	crd_namespace="kube-system"
fi
echo "Selected namespace for the installation of crds: ${crd_namespace}"

create_crds() {
    
	echo "========================================"
	echo "===== Installing CRDs =================="
	echo "========================================"
	echo
	
	echo "===== Installing SIP-TLS CRDs =========="
	# SIP-TLS CRDs
	if [ -z "$sip_tls_version" ]; then
		echo "WARNING: SIP-TLS is missing from requirements.yaml. Skipping SIP-TLS CRDs!"
		sleep 1;
	else
	
		if ! [ -f "$sip_tls_crd_pwd" ]; then
			mkdir -p ${charts_pwd}
			if [ $? -ne 0 ]; then
				echo "ERROR: During the creation of the output directory"
				exit 1;
			fi
			
			if ! [ -f "$sip_tls_pwd" ]; then
				echo "Downloading SIP-TLS chart image from artifactory"
				wget -P ${charts_pwd} ${sip_tls_artifactory}
				if [ $? -ne 0 ]; then
					echo "ERROR: Failed to download SIP-TLS chart from artifactory"
					exit 1;
				fi
			fi
			
			echo "Unpacking SIP-TLS chart image"
			tar -xzf ${sip_tls_pwd} -C ${charts_pwd}
			if [ $? -ne 0 ]; then
				echo "ERROR: Failed to untar downloaded SIP-TLS chart"
				exit 1;
			fi
			sip_tls_crd=$(ls ${charts_pwd}/eric-sec-sip-tls/eric-crd)
			sip_tls_crd_pwd=${charts_pwd}/eric-sec-sip-tls/eric-crd/${sip_tls_crd}
		fi
		
		#Get current SIP-TLS CRDs
		initial_sip_tls_crds=($(kubectl get crd -l app.kubernetes.io/name=eric-sec-sip-tls-crd | tail -n +2 | awk '{print $1}'))
		echo "Initial ${#initial_sip_tls_crds[@]} sip-tls crds found"
		echo "        < ${initial_sip_tls_crds[@]} >"
		
		if [ ${#initial_sip_tls_crds[@]} -eq 0 ]; then
			echo "Installing SIP-TLS crds..."
			helm upgrade --install eric-sec-sip-tls-crd ${sip_tls_crd_pwd} --namespace ${crd_namespace} --atomic
			sleep 2;
		
		elif [ ${#initial_sip_tls_crds[@]} -eq ${#exp_sip_tls_crds[@]} ]; then
			upgrade_crds "sip-tls"
			
		else
			echo "ERROR: Corrupted SIP-TLS crds identified (consider manual recreation of crds)."
			exit 1;
		fi
		
		# Verify SIP-TLS CRDs deletion
		final_sip_tls_crds=($(kubectl get crd -l app.kubernetes.io/name=eric-sec-sip-tls-crd --namespace ${crd_namespace} | tail -n +2 | awk '{print $1}'))
		echo "Final ${#final_sip_tls_crds[@]} SIP-TLS crds exist"
		echo "      < ${final_sip_tls_crds[@]} >"
		if [ ${#final_sip_tls_crds[@]} -ne ${#exp_sip_tls_crds[@]} ]; then
			echo "Error: During the verification of SIP-TLS crds addition."
			exit 1;
		fi
	fi
	
	echo "===== Installing ICCR CRDs ============="
	# ICCR crds
	if [ -z "$iccr_version" ]; then
		echo "WARNING: ICCR is missing from requirements.yaml. Skipping ICCR CRDs!"
		sleep 1;
	else
		if ! [ -f "$iccr_crd_pwd" ]; then
			mkdir -p ${charts_pwd}
			if [ $? -ne 0 ]; then
				echo "ERROR: During the creation of the output directory"
				exit 1;
			fi
			
			if ! [ -f "$iccr_pwd" ]; then
				echo "Downloading ICCR chart image from artifactory"
				wget -P ${charts_pwd} ${iccr_artifactory}
				if [ $? -ne 0 ]; then
					echo "ERROR: Failed to download ICCR chart from artifactory"
					exit 1;
				fi
			fi
			
			echo "Unpacking ICCR chart image"
			tar -xzf ${iccr_pwd} -C ${charts_pwd}
			if [ $? -ne 0 ]; then
				echo "ERROR: Failed to untar downloaded ICCR chart"
				exit 1;
			fi
		
			iccr_crd=$(ls ${charts_pwd}/eric-tm-ingress-controller-cr/eric-crd)
			iccr_crd_pwd=${charts_pwd}/eric-tm-ingress-controller-cr/eric-crd/${iccr_crd}
		fi
		
		#Get current ICCR crds
		initial_iccr_crds=($(kubectl get crd -l app.kubernetes.io/name=eric-tm-ingress-controller-cr-crd | tail -n +2 | awk '{print $1}'))
		echo "Initial ${#initial_iccr_crds[@]} iccr crds found"
		echo "        < ${initial_iccr_crds[@]} >"
		
		if [ ${#initial_iccr_crds[@]} -eq 0 ]; then
			echo "Installing SIP-TLS crds..."
			helm upgrade --install eric-tm-ingress-controller-cr-crd  ${iccr_crd_pwd} --namespace ${crd_namespace} --atomic
			sleep 2;
		
		elif [ ${#initial_iccr_crds[@]} -eq ${#exp_iccr_crds[@]} ]; then
			upgrade_crds "iccr"
			
		else
			echo "ERROR: Corrupted ICCR crds identified (consider manual recreation of crds)."
			exit 1;
		fi
		
		# Verify ICCR crds deletion
		final_iccr_crds=($(kubectl get crd -l app.kubernetes.io/name=eric-tm-ingress-controller-cr-crd --namespace ${crd_namespace} | tail -n +2 | awk '{print $1}'))
		echo "Final ${#final_iccr_crds[@]} ICCR crds exist"
		echo "      < ${final_iccr_crds[@]} >"
		if [ ${#final_iccr_crds[@]} -ne ${#exp_iccr_crds[@]} ]; then
			echo "Error: During the verification of ICCR crds addition."
			exit 1;
		fi
	fi
	
	echo "===== Installing CERTM CRDs ============"
	# CERTM crds
	if [ -z "$certm_version" ]; then
		echo "Warning: CERTM is missing from requirements.yaml. Skipping CERTM CRDs!"
		sleep 1;
	else
		if ! [ -f "$certm_crd_pwd" ]; then
			mkdir -p ${charts_pwd}
			if [ $? -ne 0 ]; then
				echo "ERROR: During the creation of the output directory"
				exit 1;
			fi
			
			if ! [ -f "$certm_pwd" ]; then
				echo "Downloading CERTM chart image from artifactory"
				wget -P ${charts_pwd} ${certm_artifactory}
				if [ $? -ne 0 ]; then
					echo "ERROR: Failed to download CERTM chart from artifactory"
					exit 1;
				fi
			fi
			
			echo "Unpacking CERTM chart image"
			tar -xzf ${certm_pwd} -C ${charts_pwd}
			if [ $? -ne 0 ]; then
				echo "ERROR: Failed to untar downloaded CERTM chart"
				exit 1;
			fi
		
			certm_crd=$(ls ${charts_pwd}/eric-sec-certm/eric-crd)
			certm_crd_pwd=${charts_pwd}/eric-sec-certm/eric-crd/${certm_crd}
		fi
		
		#Get current CERTM crds
		initial_certm_crds=($(kubectl get crd -l app.kubernetes.io/name=eric-sec-certm-crd | tail -n +2 | awk '{print $1}'))
		echo "Initial ${#initial_certm_crds[@]} certm crds exist"
		echo "        < ${initial_certm_crds[@]} >"
		
		if [ ${#initial_certm_crds[@]} -eq 0 ]; then
			echo "Installing CERTM crds..."
			helm upgrade --install eric-sec-certm-crd ${certm_crd_pwd} --namespace ${crd_namespace} --atomic
			sleep 2;
		
		elif [ ${#initial_certm_crds[@]} -eq ${#exp_certm_crds[@]} ]; then
			upgrade_crds "certm"
			
		else
			echo "ERROR: Corrupted CERTM crds identified (consider manual recreation of crds)."
			exit 1;
		fi
		
		# Verify CERTM crds
		final_certm_crds=($(kubectl get crd -l app.kubernetes.io/name=eric-sec-certm-crd --namespace ${crd_namespace} | tail -n +2 | awk '{print $1}'))
		echo "Final ${#final_certm_crds[@]} CERTM crds exist"
		echo "      < ${final_certm_crds[@]} >"
		if [ ${#final_certm_crds[@]} -ne ${#exp_certm_crds[@]} ]; then
			echo "ERROR: During the verification of CERTM crds addition."
			exit 1;
		fi
	fi
}

delete_crds() {
	
	delete_charts
	    
	echo "========================================"
	echo "===== Removing CRDs ===================="
	echo "========================================"
	echo
	
	echo "===== Deletion of SIP-TLS CRDs ========="
	echo
		
	current_sip_tls_crds=($(kubectl get crd -l app.kubernetes.io/name=eric-sec-sip-tls-crd --namespace ${crd_namespace} | tail -n +2 | awk '{print $1}'))
	echo "Initial ${#current_sip_tls_crds[@]} SIP-TLS crds exist"
	echo "        < ${current_sip_tls_crds[@]} >"
	if [ ${#current_sip_tls_crds[@]} -eq 0 ]; then
    		echo "WARNING: No SIP-TLS crds exist."
	else
		for crd in ${current_sip_tls_crds[@]}; do
			kubectl --namespace ${crd_namespace} delete crd "${crd}"
		done
		
		# Verify SIP-TLS CRDs deletion
		current_sip_tls_crds=($(kubectl get crd -l app.kubernetes.io/name=eric-sec-sip-tls-crd --namespace ${crd_namespace} | tail -n +2 | awk '{print $1}'))
		echo "Final ${#current_sip_tls_crds[@]} SIP-TLS crds exist"
		echo "     < ${current_sip_tls_crds[@]} >"
		if [ ${#current_sip_tls_crds[@]} -ne 0 ]; then
			echo "ERROR: During the verification of SIP-TLS crds removal."
			exit 1;
		fi
	fi
    
	echo "===== Deletion of ICCR CRDs ============"
	echo
	
	current_iccr_crds=($(kubectl get crd -l app.kubernetes.io/name=eric-tm-ingress-controller-cr-crd --namespace ${crd_namespace} | tail -n +2 | awk '{print $1}'))
	echo "Initial ${#current_iccr_crds[@]} ICCR crds exist"
	echo "        < ${current_iccr_crds[@]} >"
	if [ ${#current_iccr_crds[@]} -eq 0 ]; then
		echo "WARNING: No ICCR crds exist."
	else
		for crd in ${current_iccr_crds[@]}; do
			kubectl --namespace ${crd_namespace} delete crd "${crd}"
		done
		
		# Verify ICCR crds deletion
		current_iccr_crds=($(kubectl get crd -l app.kubernetes.io/name=eric-tm-ingress-controller-cr --namespace ${crd_namespace} | tail -n +2 | awk '{print $1}'))
		echo "Final ${#current_iccr_crds[@]} ICCR crds exist"
		echo "      < ${current_iccr_crds[@]} >"
		if [ ${#current_iccr_crds[@]} -ne 0 ]; then
			echo "ERROR: During the verification of ICCR crds removal."
			exit 1;
		fi
	fi

	echo "===== Deletion of CERTM CRDs ==========="
	echo
	
	current_certm_crds=($(kubectl get crd -l app.kubernetes.io/name=eric-sec-certm-crd --namespace ${crd_namespace} | tail -n +2 | awk '{print $1}'))
	echo "Initial ${#current_certm_crds[@]} CERTM crds exist"
	echo "        < ${current_certm_crds[@]} >"
	if [ ${#current_certm_crds[@]} -eq 0 ]; then
		echo "WARNING: No CERTM crds exist."
	else
		for crd in ${current_certm_crds[@]}; do
			kubectl --namespace ${crd_namespace} delete crd "${crd}"
		done
		
		# Verify CERTM crds deletion
		current_certm_crds=($(kubectl get crd -l app.kubernetes.io/name=eric-sec-certm-crd --namespace ${crd_namespace} | tail -n +2 | awk '{print $1}'))
		echo "Final ${#current_certm_crds[@]} CERTM crds exist"
		echo "      < ${current_certm_crds[@]} >"
		if [ ${#current_certm_crds[@]} -ne 0 ]; then
			echo "ERROR: During the verification of CERTM crds removal."
			exit 1;
		fi
	fi
}

upgrade_crds() {
	
	if [ "$1" == "sip-tls" ]; then
	
		echo "========================================"
		echo "===== Upgrading SIP-TLS CRDs ==========="
		echo "========================================"
		echo
		
		if ! [ -f "$sip_tls_crd_pwd" ]; then
			echo "Downloading SIP-TLS from artifactory"
			mkdir -p ${charts_pwd}
			if [ $? -ne 0 ]; then
				echo "ERROR: During the creation of the output directory"
				exit 1;
			fi
			
			if ! [ -f "$sip_tls_pwd" ]; then
				wget -P ${charts_pwd} ${sip_tls_artifactory}
				if [ $? -ne 0 ]; then
					echo "ERROR: Failed to download SIP-TLS chart from artifactory"
					exit 1;
				fi
			fi
			
			tar -xzf ${sip_tls_pwd} -C ${charts_pwd}
			if [ $? -ne 0 ]; then
				echo "ERROR: Failed to untar downloaded SIP-TLS chart"
				exit 1;
			fi
			
			sip_tls_crd=$(ls ${charts_pwd}/eric-sec-sip-tls/eric-crd)
			sip_tls_crd_pwd=${charts_pwd}/eric-sec-sip-tls/eric-crd/${sip_tls_crd}
		fi
		
		echo "Upgrading SIP-TLS crds..."
		helm upgrade --install eric-sec-sip-tls-crd ${sip_tls_crd_pwd} --namespace ${crd_namespace} --atomic
		sleep 2;
		
	elif [ "$1" == "certm" ]; then

		echo "========================================"
		echo "===== Upgrading CERTM CRDs ============="
		echo "========================================"
		echo
		
		if ! [ -f "$certm_crd_pwd" ]; then
			echo "Downloading CERTM from artifactory"
			mkdir -p ${charts_pwd}
			if [ $? -ne 0 ]; then
				echo "ERROR: During the creation of the output directory"
				exit 1;
			fi
			
			if ! [ -f "$certm_pwd" ]; then
				wget -P ${charts_pwd} ${certm_artifactory}
				if [ $? -ne 0 ]; then
					echo "ERROR: Failed to download CERTM chart from artifactory"
					exit 1;
				fi
			fi
			
			tar -xzf ${certm_pwd} -C ${charts_pwd}
			if [ $? -ne 0 ]; then
				echo "ERROR: Failed to untar downloaded CERTM chart"
				exit 1;
			fi
			
			certm_crd=$(ls ${charts_pwd}/eric-sec-certm/eric-crd)
			certm_crd_pwd=${charts_pwd}/eric-sec-certm/eric-crd/${certm_crd}
		fi
		
		echo "Upgrading CERTM crds..."
		helm upgrade --install eric-sec-certm-crd ${certm_crd_pwd} --namespace ${crd_namespace} --atomic
		sleep 2;
		
	elif [ "$1" == "iccr" ]; then

		echo "========================================"
		echo "===== Upgrading ICCR CRDs =============="
		echo "========================================"
		echo
		
		if ! [ -f "$iccr_crd_pwd" ]; then
			echo "Downloading ICCR from artifactory"
			mkdir -p ${charts_pwd}
			if [ $? -ne 0 ]; then
				echo "ERROR: During the creation of the output directory"
				exit 1;
			fi
			
			if ! [ -f "$certm_pwd" ]; then
				wget -P ${charts_pwd} ${iccr_artifactory}
				if [ $? -ne 0 ]; then
					echo "ERROR: Failed to download ICCR chart from artifactory"
					exit 1;
				fi
			fi
			
			tar -xzf ${iccr_pwd} -C ${charts_pwd}
			if [ $? -ne 0 ]; then
				echo "ERROR: Failed to untar downloaded ICCR chart"
				exit 1;
			fi
			
			iccr_crd=$(ls ${charts_pwd}/eric-tm-ingress-controller-cr/eric-crd)
			iccr_crd_pwd=${charts_pwd}/eric-tm-ingress-controller-cr/eric-crd/${iccr_crd}
		fi
		
		echo "Upgrading SIP-TLS crds..."
		helm upgrade --install eric-tm-ingress-controller-cr-crd ${iccr_crd_pwd} --namespace ${crd_namespace} --atomic
		sleep 2;
		
	else
	     echo "ERROR: Unsupported option. Available options [sip-tls,certm,iccr]"
	     exit 1;
	fi

}

delete_charts() {
	    
	echo "========================================"
	echo "===== Removing CRD charts =============="
	echo "========================================"
	echo
	
	sip_tls_chart=$(helm list | grep "eric-sec-sip-tls-crd")
	certm_chart=$(helm list | grep "eric-sec-certm-crd")
	iccr_chart=$(helm list | grep "eric-tm-ingress-controller-cr-crd")


	echo "===== Removing SIPT-TLS CRD chart ======"
	echo
	if [ -z "$sip_tls_chart" ]; then
		echo "WARNING: SIP-TLS crd chart already deleted"
	else
		helm delete --purge eric-sec-sip-tls-crd
	fi

	echo "===== Removing CERTM CRD chart ========="
	echo
	if [ -z "$certm_chart" ]; then
		echo "WARNING: CERTM crd chart already deleted"
	else
		helm delete --purge eric-sec-certm-crd
	fi
	
	echo "===== Removing ICCR CRD chart =========="
	echo
	if [ -z "$iccr_chart" ]; then
		echo "WARNING: ICCR crd chart already deleted"
	else
		helm delete --purge eric-tm-ingress-controller-cr-crd
	fi
}

if [ "$1" = "create" ]
then
	create_crds	
elif [ "$1" = "delete" ]
then
	delete_crds
elif [ "$1" = "upgrade" ]
then
	upgrade_crds "$2"
elif [ "$1" = "recreate" ]
then
	delete_crds
	create_crds	
else
  	echo "ERROR: Unsupported argument! Options supported [create, delete, upgrade, recreate]"
  	exit 1
fi
