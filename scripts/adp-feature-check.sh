#!/bin/bash

#######################################################################################################
#
# COPYRIGHT ERICSSON GMBH 2021
#
# The copyright to the computer program(s) herein is the property of Ericsson GmbH, Germany.
#
# The program(s) may be used and/or copied only with the written permission of Ericsson GmbH in
# accordance with the terms and conditions stipulated in the agreement/contract under which the
# program(s) have been supplied.
#
#######################################################################################################

# actual name user
me="`basename "$0"`"
centralAuthConfig=("system ldap security simple-authenticated bind-dn cn=admin,dc=la,dc=adp,dc=ericsson" "system ldap security simple-authenticated bind-password" "system ldap security user-base-dn ou=people,dc=ericsson,dc=com" "system ldap options enable-referrals true" "system ldap server eric-referral-ldap")
clearConfig="% No entries found."
fileCommandsDDC="template_commandsDDC.txt"

#parse options..
args=( "$@" )
while [ "$#" -gt 0 ]; do
	case "$1" in
		-h|--help)
			echo -e "Script used to check adp additional features\n"
			echo "Please comply with the following rules:"
			echo "1. addition of complexity is prohibited"
			echo "2. 100+ additional lines for each feature check actions introduce compexity"
			echo "3. Contact Challengers Team prior any change in the script!"
			echo
			echo "Notes:"
			echo "Set logLevel by using '--logLevel' followed by <trace> or <debug>"
			echo
			echo "Central-Authentication examples"
			echo "-------------------------------"
			echo -e "Add tls configuration and verify:\n./adp-feature-check.sh --cauth --ip 10.120.218.239 --port 22 --user sec-admin --pass secsec --type cliss --verify --tls"
			echo -e "Add non-tls configuration and verify:\n./adp-feature-check.sh --cauth --ip 10.120.218.239 --port 22 --user sec-admin --pass secsec --type cliss --verify"
			echo -e "Remove configuration and verify:\n./adp-feature-check.sh --cauth --ip 10.120.218.239 --port 22 --user sec-admin --pass secsec --type cliss --clear --verify"
			echo -e "Add DDC configuration and verify manual export of diagnostic data:\n./adp-feature-check.sh --ddc --ip 10.120.218.239 --port 22 --user sys-admin --pass syssys --type cliss --sftpIp 10.120.218.175 --sftpPort 31636 --ssk c3NoLXJzYSBBQUFBQjNOemFDMXljMkVBQUFBREFRQUJBQUFDQVFDcXorWGFTeEMzSEpLbEJMZlJRbURGaVRwS1N5YlU1TzVsVzE4Wkd4bWp5VW9YYU5YTGlldG5VTnpHTk42c1VGMTdPRlhONjNGM3dKOUhrUUl3NWdSTzR6ZGR0cFJKMmZnaTBucE1FaXR3Q0FlMjJVM1Q2clhUdGRTcnVvTlBHMSs2dVcremdPWHgzWU13UTFSeFU2ZUVmUGVOYXdoVVArZlRKUm9wMTlCZUNFU3YxOVBzaTdwbU9WK29rcHk5T3BXZ216OUtUOEhlclp1YThLSEVLV2xpVWI5NUlFYkFkTmxUNmZlcmNNMmIzY0cyYXplNjhCNHhRMDZWOHVFdkJTWmVOejlBMEVReFV5U1htZW84YmVsQjdOSVFPYm13RGdpblE5MW5mdmJaY25HVDZRL0JnQ1dZRnd5WkIySGF2d04yMExZSmdHK1prbEQ4M0pQK1VCQnhvcnhVVE00TTJnSkh4alBWTXdHbTFpV0hwc3hqZ3ZGUkkxQmtlMXgyMVdkS0xvZUIyRkxyUmx2NEYxaE9Gbmh5aGszR09kS0hlU2locmdsdzdzUmlTb2toK1czQXJleVUra3FRODU2aitnOXhyeVBnMmZwWXhVUGR6Y2N1dFAzWWZGRWZGM2tLOTgra3Frdnp0Z3B5N1V4RER6TVArWmQ5ZnNxQWJuYmpYYlFOdDRWYktKV0dtclluSjdnUzdsbWp2VVVRam1ZRDEzTFVBVVNuMW1uT1dsSGJLWDRkcTZoOE9FUk0vSXUxa2MwTklGK2Flc2luV0NSbWZjR0pkQWcyNy9mZFNoOFMvVlp0NlBUN2lrZ1Q0MUlqRHVpQVhuc2xNeUpsREIzWUxacmNBRWFJNDJhU2dKSm5sdFl0TjlvL1ViVlFmcndWOEJuRzRxRmhKK2I1NlE9PSByb290QGVyaWMtYXRtb3otc2Z0cC02NDc1ZDRmODY4LTl6am5zCg=="
			exit 0;
			;;
		--cauth)
			## central-auth feature
			cauth=true;			
        	;;
        --ddc)
			## ddc feature
            ddc=true;
            ;;
		-ip=*|--ip=*)
			## nbi ssh ip
			ip="${1#*=}";			
        	;;
		-ip|--ip)
			## nbi ssh ip
			ip="$2";
			shift;;
		-port=*|--port=*)
			## nbi ssh port
			port="${1#*=}";			
        	;;
		-port|--port)
			## nbi ssh port
			port="$2";
			shift;;
		-user=*|--user=*)
			## nbi ssh user name
			user="${1#*=}";			
        	;;
		-user|--user)
			## nbi ssh user name
			user="$2";
			shift;;
		-pass=*|--pass=*)
			## nbi ssh user pass
			pass="${1#*=}";			
        	;;
		-pass|--pass)
			## nbi ssh user pass
			pass="$2";
			shift;;
		-type=*|--type=*)
			## ssh type cli/netconf
			type="${1#*=}";			
        	;;
		-type|--type)
			## ssh type cli/netconf
			type="$2";
			shift;;
		-sftpIp=*|--sftpIp=*)
			## internal sftp tool ip
			ipSftp="${1#*=}";			
        	;;
		-sftpIp|--sftpIp)
			## internal sftp tool ip
			ipSftp="$2";
			shift;;
		--sftpPort=*|-sftpPort=*)
			## internal sftp tool port
			sftpPort="${1#*=}";
			;;
		--sftpPort|-sftpPort)
			## internal sftp tool port
			sftpPort="$2";
			shift;;
		-ssk=*|--ssk=*)
			## sftp ssh key
			ssk="${1#*=}";	
        	;;
		-ssk|--ssk)
			## sftp ssh key
			ssk="$2";
			shift;;
		--verify)
			verify=true;
			;;
		--clear)
			clear=true;
			;;
		--tls)
			tls=true;
			;;
		-logLevel=*|--logLevel=*)
			logLevel="${1#*=}";		
        	;;
		-logLevel|--logLevel)
			logLevel="$2";
			shift;;
		*)
			echo "ERROR: $me Unknown option: $1";  
			exit 1;
			;;
	esac
	((argscount++));
	shift
done

#  CHECK SCRIPT INPUT
#
if [[ $cauth ]] && [[ $ddc ]]; then
	echo "ERROR: multiple features checked the same time"
	exit 1;
fi

if ( !( [[ $cauth ]] || [[ $ddc ]]) ); then
	echo "ERROR: Choose either central-auth or ddc features to be checked"
	exit 1;
fi


if [[ "${logLevel}" == "trace" ]]; then
	set -x
elif [[ "${logLevel}" == "debug" ]]; then
	debug=true
else
	debug=false
fi

#
#  Functions
#

#  Send commands via SSH to NBI i/f
sendNbiCommand(){
	commandType=$1
	command=$2
	if [[ ${commandType} == "cmd-file" ]]; then
		./send_command_to_ssh_standalone.exp  --user=${user} --password=${pass} --shell=${type} --ip=${ip} --port=${port} --command-file="${command}"
	else
		./send_command_to_ssh_standalone.exp  --user=${user} --password=${pass} --shell=${type} --ip=${ip} --port=${port} --command="${command}"
	fi
	if [ $? -ne 0 ]; then
		echo "ERROR: $me Failed to send command $1";
		return 1;
	fi
}

# Create central-authentication configuration
# and store it to adpFeatureCheckTemp file
createCentralAutConfig(){
	option=$1
	case "$option" in
	new)
		cp adp_feature_check/template_central_auth_configuration adpFeatureCheckTemp
		if [[ $tls ]]; then
			sed -i "s/<CLEAR_OR_TLS>/tls/g" adpFeatureCheckTemp
			sed -i "s/<LDAP_OR_LDAPS>/ldaps/g" adpFeatureCheckTemp
			sed -i "s/<389_OR_636>/636/g" adpFeatureCheckTemp
		else
			sed -i "s/<CLEAR_OR_TLS>/clear/g" adpFeatureCheckTemp
			sed -i "s/<LDAP_OR_LDAPS>/ldap/g" adpFeatureCheckTemp
			sed -i "s/<389_OR_636>/389/g" adpFeatureCheckTemp
		fi
		;;
	
	clear)
		cp adp_feature_check/template_configuration_clear adpFeatureCheckTemp
		sed -i "s/<PATH_TO_CLEAR>/system ldap/g" adpFeatureCheckTemp
		;;
	
	show)
		cp adp_feature_check/template_configuration_show adpFeatureCheckTemp
		sed -i "s/<PATH_TO_SHOW>/system ldap/g" adpFeatureCheckTemp
		;;
	*)
		echo "Invalid option for the creation of central authentication configuration"
		return 1;
		;;
	esac
}

# Remove temporary file with central-authentication configuration
removeCentralAuthConfigFile(){
	rm -fr adpFeatureCheckTemp
	if [[ $? -ne 0 ]]; then
		echo "ERROR: Failed to delete adpFeatureCheckTemp file"
		return 1
	fi
}

# Verify existing central authentication config
verifyCentralAuthConfig(){
	config=$1
	if [[ $clear ]]; then
		if [[ "${config}" =~ "${clearConfig[@]}" ]]; then
			if [[ "${debug}" == true ]]; then
				echo "DEBUG: Config is empty as expected"
			fi
		else
			echo "ERROR: Failed to identify value ${value}"
			return 1;
		fi
	else
		for value in "${centralAuthConfig[@]}"; do
			if [[ "${config}" =~ "${value}" ]]; then
				if [[ "${debug}" == true ]]; then
					echo "DEBUG: Value ${value} identified successfully"
				fi
			else
				echo "ERROR: Failed to identify value ${value}"
				return 1;
			fi
		done
	fi
	return 0
}

# functions for DDC
setSftp(){

	# create a file with places for sed
	cp adp_feature_check/template_ddc_configuration adpFeatureCheckTemp
	sed -i "s/<SFTP_IP>/${ipSftp}/g" adpFeatureCheckTemp
	sed -i "s/<SFTP_PORT>/${sftpPort}/g" adpFeatureCheckTemp
	sed -i "s/<SFTP_KEY>/${ssk}/g" adpFeatureCheckTemp

	echo -e "The following commands will be issued to CLI interface!\n"
	cat adpFeatureCheckTemp

    sendNbiCommand cmd-file adpFeatureCheckTemp
	
	command="diagnostic-data-collection collect-ddb profile HelmChartValues"
	output=($(sendNbiCommand cmd "diagnostic-data-collection collect-ddb profile HelmChartValues"))
	id=$(echo ${output} |  tail -n 3 | head -n 1 | awk -F  "return-value " '{print $2}' )
	echo "Progress ID : ${id}"

	sleep 60
	output=$(sendNbiCommand cmd "show diagnostic-data-collection progress-report ${id}")

	echo ${output} | grep success
	if [ $? -ne 0 ]; then
		echo "ERROR: collection failed!"
		exit 1
	else
		echo "Success: collection was successfully completed!"
	fi

	rm -rf adpFeatureCheckTemp
}

#
#  MAIN ACTIONS
#

#
#  Verify Central Authentication
#
if [[ "${cauth}" == true ]]; then
	if [[ "${clear}" == true ]]; then
		createCentralAutConfig clear
		if [ $? -ne 0 ]; then
			echo "ERROR: Failed to create <clear> central authentication configuration"
			exit 1;
		fi
	else
		createCentralAutConfig new
		if [ $? -ne 0 ]; then
			echo "ERROR: Failed to create <new> central authentication configuration"
			exit 1;
		fi
		echo "Adding configuration for referral-ldap."
		echo "Try to login in NBI with the following users:"
		echo "user		pass		group"
		echo "bob		bob_pw		system-admin"
		echo "alice		alice_pw	system-security-admin"
		echo "guest		guest_pw	system-read-only"
		echo "scpAdmin	scpscp		scp-admin"
		echo "bsfAdmin	bsfbsf		bsf-admin"
		echo "seppAdmin	seppsepp	sepp-admin"
	fi
	
	sendNbiCommand cmd-file adpFeatureCheckTemp
	if [ $? -ne 0 ]; then
		echo "ERROR: Failed to apply changes in system module configuration"
		exit 1
	fi
	
	if [[ "${verify}" == true ]]; then
		createCentralAutConfig show
		if [ $? -ne 0 ]; then
			echo "ERROR: Failed to create <show> central authentication configuration"
			exit 1;
		fi
		output=$(sendNbiCommand cmd-file adpFeatureCheckTemp)
		if [ $? -ne 0 ]; then
			echo "ERROR: Failed to apply changes in system module configuration"
			exit 1
		fi
		verifyCentralAuthConfig "${output}"
	fi
	
	removeCentralAuthConfigFile
	if [ $? -ne 0 ]; then
		echo "WARNING: Failed to delete tmp created files, manual actions might be required"
	fi
fi

#
#  Verify DDC export
#
if [[ "${ddc}" == true ]]; then
	setSftp
fi
