#!/bin/bash

#######################################################################################################
#
# COPYRIGHT ERICSSON GMBH 2020
#
# The copyright to the computer program(s) herein is the property of Ericsson GmbH, Germany.
#
# The program(s) may be used and/or copied only with the written permission of Ericsson GmbH in
# accordance with the terms and conditions stipulated in the agreement/contract under which the
# program(s) have been supplied.
#
#######################################################################################################

############################################################################
#                                                                          #
# Script to collect logfiles and configuration for the                     #
# Signaling Controller (SC).                                               #
# The script will also collect HELM charts configuration.                  #
#                                                                          #
# Usage:                                                                   #
# collect_ADP_logs.sh [-n <namespace>] [-c <kubernetes_cluster_config>]    #
#                     [-o <output_dir>] [-s]                               #
#                                                                          #
############################################################################

scriptVersion="R1A27"

############################################################################
#                          History                                         #
#                                                                          #
# 2024-02-13 EEDCSI        R1A27: collect DCED SC database                 #
#                                 nerdctl commands additional to docker    #
#                                                                          #
# 2023-11-24 EEDCSI        R1A26: added fetching BRO information           #
#                                 added fetching YANG schemas              #
#                                                                          #
# 2023-10-17 EEDCSI        R1A25: modified PM XML file fetching from       #
#                                 object store                             #
#                                                                          #
# 2023-08-31 EEDCSI        R1A24: Added collection of LDAP configuration   #
#                                                                          #
# 2023-07-14 EEDCSI        R1A23: Fixed broken helm collection             #
#                                                                          #
# 2023-06-02 EEDCSI        R1A22: LDAP additional tls logs                 #
#                                 Considered the case that user doesn't    #
#                                 have access to the complete cluster (-A) #
#                                 Included ADP script changes from 1.0.13  #
#                                                                          #
# 2023-03-21 EEDCSI        R1A21: Fault fix: discovery of running pods     #
#                                                                          #
# 2023-03-07 EEDCSI        R1A20: make pod discovery more robust           #
#                                 Collect some internal TLS secrets        #
#                                 Scripts in SIP-TLS changed names         #
#                                                                          #
# 2022-12-07 EEDCSI        R1A19: bugfix DCED needs added container        #
#                                                                          #
# 2022-11-18 EEDCSI        R1A18: Collect LDAP info from both pods         #
#                                 Collect also TLS secrets for traffic     #
#                                 Added option to change the output dir    #
#                                                                          #
# 2022-10-21 EEDCSI        R1A17: Collect more info from Search Engine     #
#                                                                          #
# 2022-02-23 EEDCSI        R1A16: sip_dced_logs improved                   #
#                                 get_pm_xml_objectst.. only conditionally #
#                                 Included ADP script changes from 1.0.8   #
#                                   Organizing describe directory          #
#                                   limiting KVDB stat                     #
#                                                                          #
# 2022-02-03 EEDCSI        R1A15: Included ADP script changes from 1.0.3   #
#                                 ss7 logs collected                       #
#                                 get_kvdb_ag_logs                         #
#                                 Fetch addional info from Postgress DB    #
#                                                                          #
# 2021-11-24 EEDCSI        R1A14: Added helm info collection for failed    #
#                                 Added additional method to fetch PM XML  #
#                                   files from ObjectStore                 #
#                                 Added check on namespace to stop if      #
#                                   namespaces does not exist              #
#                                                                          #
# 2021-09-06 EEDCSI        R1A13: Simplification in helm part              #
#                                 Fixed parts where $kube_cmd was not used #
#                                 --use-protocol-buffers added to 'top'    #
#                                                                          #
# 2021-08-25 EEDCSI        R1A12: Included ADP script changes by EPRGGGZ   #
#                                 Improved logic to only gather prev logs  #
#                                                            if restart    #
#                                 Added grep for disk and latency errors   #
#                                                               under err  #
#                                                                          #
# 2021-06-15 EEDCSI        R1A11: added data collection for DCED           #
#                                 Removed v option in tar command          #
#                                 Added check POD status before invoke     #
#                                                            exec the POD  #
#                                 Added describe of InternalCertificates   #
#                                                                          #
# 2021-02-15 EEDCSI        R1A10: helm3 directory is split into namespaces #
#                                 without jq                               #
#                                 also way to detect helm version improved #
#                                                                          #
# 2020-12-22 EEDCSI        R1A09: helm3 directory is split into namespaces #
#                                                                          #
# 2020-12-08 EEDCSI        R1A08: added kubectl top output                 #
#                                 Added information for ssd for CMYP       #
#                                 Added information from SearchEngine      #
#                                                                          #
# 2020-09-22 EEDCSI        R1A07: helm3 support modified to support        #
#                                 ECCD 1.12                                #
#                                                                          #
# 2020-09-22 EKONPAP       R1A06: STM Diameter info collected              #
#            EKOTEVA              Init Containers added and all schemas    #
#            EEDCSI               Less errors displayed                    #
#                                                                          #
# 2020-09-10 EEDCSI        R1A05: helm3 support                            #
#                                 system files collected if option -s used #
#                                                                          #
# 2020-08-19 EEDCSI        R1A04: Merged updates from official script      #
#                          NOTE: helm 3 updates not done yet               #
#                                                                          #
# 2020-09-02 EKONPAP       Adds support for collecting ADP logs            #
#                          when BSF also deployed                          #
#                                                                          #
# 2020-06-10 EISSKOI       R1A03: fixes in order to specify                #
#                          the kubernetes config to be used                #
#                                                                          #
# 2020-05-25 EEDCSI        R1A02: Fault fixes                              #
#                                                                          #
# 2020-04-06 EKOTEVA       R1A01: Alignment for readability                #
#                          Add logs from new SIP-TLS crds                  #
#                          Add ietf-trustore configuration printout        #
#                          Add httpproxies                                 #
#                                                                          #
# 2019-10-31 EEDCSI        Activated SIP-TLS collection                    #
#                          and config from all users of CM mediator        #
#                                                                          #
# 2019-10-31 EEDKLR        Bugfixes and improvements                       #
#                                                                          #
# 2019-09-23 ENUBARS       Added sc_logs, yang_config                      #
#                          Added Bulk_reporter                             #
#                          Added cmm_config                                #
#                                                                          #
# 2019-09-10  EEDCSI       Improvements                                    #
#                                                                          #
# 2019-08-14  EEDALA       Parallelized the script for faster execution    #
#                                                                          #
# 2019-07-23  EPRGGGZ      Added the log collection for SIP-TLS            #
#                          and CMyang provider                             #
#                                                                          #
# 2019-01-25  EPRGGGZ      Fixed bug with events                           #
#                          Added PV                                        #
#                          Added cmm_logs for CM Mediator                  #
#                                                                          #
# 2019-01-23   Keith Liu   fix bug when get logs of pod which may have     #
#                          more than one container                         #
#                          add more resources for describe logs            #
#                          add timestamp in the log folder name and some   #
#                          improvement                                     #
#                                                                          #
# Original version:        EPRGGGZ Gustavo Garcia G.                       #
############################################################################

#Fail if empty argument received
export HELP=" Usage: collect_ADP_logs.sh [-n <Kubernetes_namespace>] [-c <kubernetes_cluster_config>] [-o outputdir] [-s][-h] \n\n Do not provide a namespace if you defined the namespace in the cluster context of your config file\n\n If option -s or --system is used, also kubernetes logs from system namespaces will be collected.\n\n Example:\n $0 -n sc -c ~/.kube/cluster.config"

if (($# == 0)); then
    echo "No arguments specified. The script will proceed with default values."
    namespace="";
    config="";
elif (($# > 7));
then
    echo "Too many arguments specified."
    echo -e "$HELP"
    exit 1;
else
    echo ""
    echo "      Starting Datacollection"
    echo ""
    echo "This process might take a few minutes"
    echo "====================================="
    echo ""
    echo ""
fi


while (( "$#" ));
do
    case "$1" in
        -n|--namespace)
            case "$2" in
                 -c|--config) echo -e "The parameter succeeding the -n|--namespace parameter is not set properly. Aborting."; exit 1; ;;
                *) export namespace="$2"; shift; shift; ;;
            esac

        ;;
        -c|--config)
            case "$2" in
                -n|--namespace) echo -e "The parameter succeeding the -c|--config parameter is not set properly. Aborting."; exit 1; ;;
                *) export config="$2"; shift; shift; ;;
            esac
        ;;
        -o|--outdir)
            case "$2" in
                 -c|--config) echo -e "The parameter succeeding the -o|--outdir parameter is not set properly. Aborting."; exit 1; ;;
                *) export outdir="$2"; shift; shift; ;;
            esac
        ;;
        -s|--system) export collect_system_logs=1; shift;
        ;;
        -h|--help) echo -e "$HELP"; exit 0
        ;;
         *) echo -e "'$1' is an invalid parameter."; echo -e "$HELP"; exit 1 ;;
    esac

done;

if [ "$config" == "" ] && [ ! -f "$config" ];
then
    echo "The config parameter is empty or non existent. Hence the default config (/home/$USER/.kube/config) will be used."
        export config="/home/$USER/.kube/config"

fi
echo "The config parameter \"$config\" will be used."

if [ "$namespace" == ""  ];
then
    echo "Checking if a namespace is specified in the default kube config."
    if (( $(grep -c "namespace" $config) ));
    then
        echo "The namespace included in the context of the cluster config will be used."
        export kube_cmd="kubectl --kubeconfig $config"
    else
        echo
        echo "ERROR: The namespace was not specified. The collection of the logs will be aborted."
        exit 0;
    fi;
else
    export kube_cmd="kubectl --kubeconfig $config --namespace $namespace"

    $kube_cmd get ns $namespace &>/dev/null
    if [ $? -eq 0 ]; then
        echo "The specified namespace \"$namespace\" will be used."
    else
        echo
        echo "ERROR: The specified namespace does not exit. The collection of the logs will be aborted."
        exit 0;
    fi;
fi;

if ! [[ $(${kube_cmd} get pods -A 2>&1 ) =~ "Error from server" ]] &>/dev/null; then
    collect_all_ns=1
    echo "Using kubectl option -A when possible."
else # Error from server (Forbidden): pods is forbidden: User "bsf_admin" cannot list resource "pods" in API group "" at the cluster scope
    collect_all_ns=0
    echo "Not using kubectl option -A since the user does not have this authority."
fi

if [ "$outdir" == ""  ];
then
    export outdir=$PWD
fi;
echo "Writing output to $outdir"
echo

#determine helm versions
export helm2_cmd=""
export helm3_cmd=""

if [ -x "$(command -v helm2)" ] && [[ $(helm2 version 2>&1) =~ "Ver:\"v2" ]] && [[ $(helm2 version 2>&1) =~ "Server" ]]; then
    export helm2_cmd="helm2 --kubeconfig $config"
fi;

if [ -x "$(command -v helm3)" ] && [[ $(helm3 version) =~ "Version:\"v3" ]]; then
    export helm3_cmd="helm3 --kubeconfig $config"
fi;

if [ -x "$(command -v helm)" ] && [[ $(helm version 2>&1) =~ "Ver:\"v2" ]] && [[ $(helm2 version 2>&1) =~ "Server" ]]; then
    export helm2_cmd="helm --kubeconfig $config"
fi;

if [ -x "$(command -v helm)" ] && [[ $(helm version) =~ "Version:\"v3" ]]; then
    export helm3_cmd="helm --kubeconfig $config"
fi;


echo "The kube command to be used will be: $kube_cmd"
echo "The helm2 command to be used will be: $helm2_cmd"
echo "The helm3 command to be used will be: $helm3_cmd"
echo "-----------------------"


#exit 0;

#Create a directory for placing the logs
log_base_dir=logs_${namespace}_$(date "+%Y-%m-%d-%H-%M-%S")
log_base_path=${outdir}/${log_base_dir}
mkdir ${log_base_path}

echo "Version: ${scriptVersion}" > ${log_base_path}/log_collection_version.txt


get_describe_info() {
    #echo "---------------------------------------"
    echo "- Getting kubernetes describe info -"
    #echo "---------------------------------------"
    #echo "---------------------------------------"

    local des_dir=${log_base_path}/k_describe
    mkdir -p ${des_dir}/other_resources

    $kube_cmd version -o json >  ${des_dir}/kubernetes_version.json

    if $kube_cmd version --short &>/dev/null
	then
        export k_major_version=$(kubectl version --short 2> /dev/null | grep Client  | sed 's/.*v\([0-9]*\).\([0-9]*\).\([0-9]*\)/\1/' )
        export k_minor_version=$(kubectl version --short 2> /dev/null | grep Client  | sed 's/.*v\([0-9]*\).\([0-9]*\).\([0-9]*\)/\2/' )
    else
        export k_major_version=$(kubectl version 2> /dev/null | grep Client  | sed 's/.*v\([0-9]*\).\([0-9]*\).\([0-9]*\)/\1/' )
        export k_minor_version=$(kubectl version 2> /dev/null | grep Client  | sed 's/.*v\([0-9]*\).\([0-9]*\).\([0-9]*\)/\2/' )
	fi

    # \cp ${config} ${des_dir}/

    $kube_cmd cluster-info > ${des_dir}/kubernetes_cluster_info.txt
    #kubectl cluster-info dump > > ${des_dir}/kubernetes_cluster_info_dump.txt &

    #TOP
    if (( $k_major_version >= 1 && $k_minor_version >= 21 )); then
     if [ "$collect_all_ns" == "1"  ] ; then
      $kube_cmd top pod -A --use-protocol-buffers > ${des_dir}/kubernetes_top_pod-A.txt
      $kube_cmd top pod -A --containers=true --use-protocol-buffers > ${des_dir}/kubernetes_top_pod_containers-A.txt
      $kube_cmd top node --use-protocol-buffers   > ${des_dir}/kubernetes_top_node.txt
     else
      $kube_cmd top pod --use-protocol-buffers > ${des_dir}/kubernetes_top_pod.txt
      $kube_cmd top pod --containers=true --use-protocol-buffers > ${des_dir}/kubernetes_top_pod_containers.txt
      $kube_cmd top node --use-protocol-buffers   > ${des_dir}/kubernetes_top_node.txt
     fi
    else
     if [ "$collect_all_ns" == "1"  ] ; then
      $kube_cmd top pod -A  > ${des_dir}/kubernetes_top_pod-A.txt
      $kube_cmd top pod -A --containers=true > ${des_dir}/kubernetes_top_pod_containers-A.txt
      $kube_cmd top node    > ${des_dir}/kubernetes_top_node.txt
     else
      $kube_cmd top pod > ${des_dir}/kubernetes_top_pod.txt
      $kube_cmd top pod --containers=true > ${des_dir}/kubernetes_top_pod_containers.txt
      $kube_cmd top node > ${des_dir}/kubernetes_top_node.txt
     fi
    fi

    for attr in statefulsets internalCertificates crds deployments services replicasets endpoints daemonsets persistentvolumeclaims configmaps pods nodes jobs persistentvolumes rolebindings roles secrets serviceaccounts storageclasses ingresses httpproxies

    #for attr in $(kubectl api-resources --verbs=list --namespaced --no-headers |grep -v events| sed 's/true/;/g'| awk -F\; '{print $2}'|sort -u) crd storageclass persistentvolume node
    do
        (
         if ! [[ $(${kube_cmd} get $attr) =~ "No resources found" ]] &>/dev/null; then
             local dir=`echo $attr | tr '[:lower:]' '[:upper:]'`
             mkdir ${des_dir}/$dir
             if [ "$collect_all_ns" == "1"  ] ; then
               $kube_cmd get $attr -A -o wide > ${des_dir}/$dir/kubectl_get_${attr}_-A_-o_wide.txt 2> /dev/null
             else
               $kube_cmd get $attr -o wide > ${des_dir}/$dir/kubectl_get_${attr}_-o_wide.txt 2> /dev/null
             fi

             #echo "Getting describe information on $dir.."
             for i in `$kube_cmd get $attr 2> /dev/null | grep -v NAME | awk '{print $1}'`
             do
               $kube_cmd  describe  $attr  $i > ${des_dir}/$dir/$i.txt 2> /dev/null;
             done

             #echo "Getting content of traffic related certificates ..."
             if [ "$attr" == "secrets" ]; then
                 for i in `$kube_cmd get externalcertificates.certm.sec.ericsson.com  -o jsonpath='{.items[*].spec.generated-secret-name}' 2> /dev/null `
                 do
                     $kube_cmd  get $attr  $i -o json > ${des_dir}/$dir/${i}_content.json 2> /dev/null;
                 done
             fi

             #echo "Getting content of internal certificates ..."
             if [ "$attr" == "secrets" ]; then
                 for i in `$kube_cmd get internalcertificates.siptls.sec.ericsson.com  -o jsonpath='{.items[*].spec.generated-secret-name}' 2> /dev/null `
                 do
                     $kube_cmd  get $attr  $i -o json > ${des_dir}/$dir/${i}_content.json 2> /dev/null;
                 done
             fi

             #echo "Getting content of internal certificates ..."
             if [ "$attr" == "secrets" ]; then
                 for i in `$kube_cmd get internalusercas.siptls.sec.ericsson.com  -o jsonpath='{.items[*].spec.generated-secret-name}' 2> /dev/null `
                 do
                     $kube_cmd  get $attr  $i -o json > ${des_dir}/$dir/${i}_content.json 2> /dev/null;
                 done
             fi


         fi
        ) &
    done
    wait

    for attr in $( kubectl api-resources --verbs=list --namespaced --no-headers| sed 's/true/;/g'| awk -F\; '{print $2}'|sort -u |egrep -vi "event|statefulset|internalCertificate|deployment|service|replicaset|endpoint|daemonset|persistentvolumeclaim|configmap|pod|node|job|persistentvolume|rolebinding|role|secret|serviceaccount|storageclasse|ingress|httpproxy") extensionservice endpointslice cronjob
    do
        (
         if ! [[ $(${kube_cmd} get $attr) =~ "No resources found" ]] &>/dev/null; then
             local dir=`echo $attr | tr '[:lower:]' '[:upper:]'`
             mkdir ${des_dir}/other_resources/$dir
             if [ "$collect_all_ns" == "1"  ] ; then
                 $kube_cmd get $attr -A -o wide > ${des_dir}/other_resources/$dir/kubectl_get_${attr}_-A_-o_wide.txt 2> /dev/null
             else
                 $kube_cmd get $attr -o wide > ${des_dir}/other_resources/$dir/kubectl_get_${attr}_-o_wide.txt 2> /dev/null
             fi
             #echo "Getting describe information on $dir.."
             for i in `$kube_cmd get $attr 2> /dev/null | grep -v NAME | awk '{print $1}'`
             do
               $kube_cmd  describe  $attr  $i > ${des_dir}/other_resources/$dir/$i.txt 2> /dev/null;
             done
         fi
        ) &
    done
    wait
}

get_system_describe_info() {
    #echo "---------------------------------------"
    echo "- Getting kubernetes describe info from system namespaces -"
    #echo "---------------------------------------"
    #echo "---------------------------------------"

    local des_dir=${log_base_path}/k_descr_sys
    mkdir ${des_dir}
    mkdir ${des_dir}/ks
    mkdir ${des_dir}/mo
    mkdir ${des_dir}/in
    mkdir ${des_dir}/de


    #for attr in statefulsets internalCertificates deployments services replicasets endpoints daemonsets persistentvolumeclaims configmap pods jobs rolebindings roles secrets serviceaccounts storageclasses ingresses httpproxies
    for attr in $(kubectl api-resources --verbs=list --namespaced --no-headers |grep -v events| sed 's/true/;/g'| awk -F\; '{print $2}'|sort -u) crd storageclass persistentvolume node
    do
        (
         local dir=`echo $attr | tr '[:lower:]' '[:upper:]'`
         mkdir ${des_dir}/ks/$dir
         mkdir ${des_dir}/mo/$dir
         mkdir ${des_dir}/in/$dir
         mkdir ${des_dir}/de/$dir

         echo -e "Logs for namespace kube-system are in direcotry 'ks', for monitoring in 'mo', for ingress-nginx in 'in' and for default in 'de'\nCrds, persistentvolumes and nodes are not collected, see directory k_describe instead" > ${des_dir}/README.txt

         $kube_cmd get $attr -n kube-system -o wide > ${des_dir}/ks/$dir/kubectl_get_${attr}_-o_wide.txt  2> /dev/null
         $kube_cmd get $attr -n monitoring -o wide >> ${des_dir}/mo/$dir/kubectl_get_${attr}_-o_wide.txt  2> /dev/null
         $kube_cmd get $attr -n ingress-nginx -o wide >> ${des_dir}/in/$dir/kubectl_get_${attr}_-o_wide.txt  2> /dev/null
         $kube_cmd get $attr -n default -o wide >> ${des_dir}/de/$dir/kubectl_get_${attr}_-o_wide.txt  2> /dev/null

         #echo "Getting describe information on $dir.."
         for i in `$kube_cmd -n kube-system get $attr   2> /dev/null | grep -v NAME | awk '{print $1}'`
         do
           $kube_cmd  describe  $attr  $i -n kube-system > ${des_dir}/ks/$dir/$i.txt  2> /dev/null;
         done

         for i in ` $kube_cmd -n monitoring get $attr  2> /dev/null | grep -v NAME | awk '{print $1}'`
         do
           $kube_cmd  describe  $attr  $i -n monitoring > ${des_dir}/mo/$dir/$i.txt  2> /dev/null;
         done

         for i in `$kube_cmd -n ingress-nginx get $attr 2> /dev/null | grep -v NAME | awk '{print $1}'`
         do
           $kube_cmd  describe  $attr  $i -n ingress-nginx > ${des_dir}/in/$dir/$i.txt  2> /dev/null;
         done

         for i in `$kube_cmd -n default get $attr 2> /dev/null | grep -v NAME | awk '{print $1}'`
         do
           $kube_cmd  describe  $attr  $i -n default > ${des_dir}/de/$dir/$i.txt  2> /dev/null;
         done

        ) &
    wait

    done
}

get_events() {
    echo "- Getting list of events -"
    local des_dir=${log_base_path}/k_events
    mkdir ${des_dir}

    if [ "$collect_all_ns" == "1"  ] ; then
        $kube_cmd get events -A > ${des_dir}/events_-A.txt
    else
        $kube_cmd get events > ${des_dir}/events.txt
    fi
}

get_pods_logs() {
    #echo "---------------------------------------"
    echo "- Getting logs per POD -"
    #echo "---------------------------------------"
    #echo "---------------------------------------"

    local logs_dir=${log_base_path}/pod_logs
    mkdir ${logs_dir}
    $kube_cmd get pods -o wide > ${logs_dir}/kubernetes_pods_to_log.txt
    for pod in `$kube_cmd get pods -o jsonpath='{.items[*].metadata.name}' 2> /dev/null `
    do
        pod_status=$($kube_cmd get pod $pod -o jsonpath='{.status.phase}' 2> /dev/null )
        pod_restarts=$($kube_cmd get pod $pod 2> /dev/null  |grep -vi restarts|awk '{print $4}')
        for container in `$kube_cmd get pod $pod -o jsonpath='{.spec.containers[*].name}' 2> /dev/null`
        do
          $kube_cmd logs $pod -c $container > ${logs_dir}/${pod}_${container}.txt 2> /dev/null
          if [[ "$pod_restarts" > "0" ]]; then
            $kube_cmd logs $pod -c $container -p > ${logs_dir}/${pod}_${container}_previous.txt 2> /dev/null
          fi
        done

        for initContainer in `$kube_cmd get pod $pod -o jsonpath='{.spec.initContainers[*].name}' 2> /dev/null`
        do
          $kube_cmd logs $pod -c $initContainer > ${logs_dir}/${pod}_${initContainer}.txt 2> /dev/null
          if [[ "$pod_restarts" > "0" ]]; then
            $kube_cmd logs $pod -c $initContainer -p > ${logs_dir}/${pod}_${initContainer}_previous.txt 2> /dev/null
          fi
        done

    done
}

get_system_pods_logs() {
    #echo "---------------------------------------"
    echo "- Getting logs per POD from system namespaces -"
    #echo "---------------------------------------"
    #echo "---------------------------------------"

    local logs_dir=${log_base_path}/pod_logs_sys
    mkdir ${logs_dir}

    echo "Logs for namespace kube-system are in direcotry 'ks', for monitoring in 'mo' and for ingress-nginx in 'in'" > ${logs_dir}/README.txt

    mkdir ${logs_dir}/ks
    mkdir ${logs_dir}/mo
    mkdir ${logs_dir}/in
    mkdir ${logs_dir}/de

    $kube_cmd get pods -n kube-system > ${logs_dir}/ks/kubernetes_pods_to_log.txt
    $kube_cmd get pods -n monitoring >> ${logs_dir}/mo/kubernetes_pods_to_log.txt
    $kube_cmd get pods -n ingress-nginx  >> ${logs_dir}/in/kubernetes_pods_to_log.txt
    $kube_cmd get pods -n default  >> ${logs_dir}/de/kubernetes_pods_to_log.txt

    for pod in `$kube_cmd get pods -n kube-system -o jsonpath='{.items[*].metadata.name}' 2> /dev/null `
    do
        for container in `$kube_cmd get pod $pod -n kube-system -o jsonpath='{.spec.containers[*].name}'  2> /dev/null`
        do
          $kube_cmd logs $pod -n kube-system -c $container -p 2> /dev/null > ${logs_dir}/ks/${pod}_${container}_previous.txt   2> /dev/null
          $kube_cmd logs $pod -n kube-system -c $container 2> /dev/null > ${logs_dir}/ks/${pod}_${container}.txt   2> /dev/null
        done

        for initContainer in `$kube_cmd get pod $pod -n kube-system -o jsonpath='{.spec.initContainers[*].name}' 2> /dev/null`
        do
          $kube_cmd logs $pod -n kube-system -c $initContainer -p 2> /dev/null > ${logs_dir}/ks/${pod}_${initContainer}_previous.txt   2> /dev/null
          $kube_cmd logs $pod -n kube-system -c $initContainer 2> /dev/null > ${logs_dir}/ks/${pod}_${initContainer}.txt   2> /dev/null
        done
    done

    for pod in `$kube_cmd get pods -n monitoring -o jsonpath='{.items[*].metadata.name}' 2> /dev/null`
    do
        for container in `$kube_cmd get pod $pod -n monitoring -o jsonpath='{.spec.containers[*].name}'  2> /dev/null`
        do
          $kube_cmd logs $pod -n monitoring -c $container -p 2> /dev/null > ${logs_dir}/mo/${pod}_${container}_previous.txt   2> /dev/null
          $kube_cmd logs $pod -n monitoring -c $container 2> /dev/null > ${logs_dir}/mo/${pod}_${container}.txt  2> /dev/null
        done

        for initContainer in `$kube_cmd get pod $pod -n kube-system -o jsonpath='{.spec.initContainers[*].name}' 2> /dev/null`
        do
          $kube_cmd logs $pod -n monitoring -c $initContainer -p 2> /dev/null > ${logs_dir}/ks/${pod}_${initContainer}_previous.txt   2> /dev/null
          $kube_cmd logs $pod -n monitoring -c $initContainer 2> /dev/null > ${logs_dir}/ks/${pod}_${initContainer}.txt   2> /dev/null
        done
    done

    for pod in `$kube_cmd get pods -n ingress-nginx -o jsonpath='{.items[*].metadata.name}' 2> /dev/null`
    do
        for container in `$kube_cmd get pod $pod -n ingress-nginx -o jsonpath='{.spec.containers[*].name}'  2> /dev/null`
        do
          $kube_cmd logs $pod -n ingress-nginx -c $container -p 2> /dev/null > ${logs_dir}/in/${pod}_${container}_previous.txt  2> /dev/null
          $kube_cmd logs $pod -n ingress-nginx -c $container 2> /dev/null > ${logs_dir}/in/${pod}_${container}.txt  2> /dev/null
        done

        for initContainer in `$kube_cmd get pod $pod -n kube-system -o jsonpath='{.spec.initContainers[*].name}' 2> /dev/null`
        do
          $kube_cmd logs $pod -n ingress-nginx -c $initContainer -p 2> /dev/null > ${logs_dir}/ks/${pod}_${initContainer}_previous.txt   2> /dev/null
          $kube_cmd logs $pod -n ingress-nginx -c $initContainer 2> /dev/null > ${logs_dir}/ks/${pod}_${initContainer}.txt   2> /dev/null
        done
    done

    for pod in `$kube_cmd get pods -n default -o jsonpath='{.items[*].metadata.name}' 2> /dev/null`
    do
        for container in `$kube_cmd get pod $pod -n default -o jsonpath='{.spec.containers[*].name}'  2> /dev/null`
        do
          $kube_cmd logs $pod -n default -c $container -p 2> /dev/null > ${logs_dir}/de/${pod}_${container}_previous.txt  2> /dev/null
          $kube_cmd logs $pod -n default -c $container 2> /dev/null > ${logs_dir}/de/${pod}_${container}.txt  2> /dev/null
        done

        for initContainer in `$kube_cmd get pod $pod -n kube-system -o jsonpath='{.spec.initContainers[*].name}' 2> /dev/null`
        do
          $kube_cmd logs $pod -n default -c $initContainer -p 2> /dev/null > ${logs_dir}/ks/${pod}_${initContainer}_previous.txt   2> /dev/null
          $kube_cmd logs $pod -n default -c $initContainer 2> /dev/null > ${logs_dir}/ks/${pod}_${initContainer}.txt   2> /dev/null
        done
    done

}


get_pods_env() {
    #echo "---------------------------------------"
    echo "- Getting environment variables per POD -"
    #echo "---------------------------------------"
    #echo "---------------------------------------"

    local env_dir=${log_base_path}/pod_env
    mkdir ${env_dir}
    $kube_cmd get pods > ${env_dir}/kubernetes_pods_to_log.txt
    for pod in `$kube_cmd get pods | grep -v NAME | awk '{print $1}'`
    do
        pod_status=$($kube_cmd get pod $pod -o jsonpath='{.status.phase}' 2> /dev/null )

        for container in `$kube_cmd get pod $pod -o jsonpath='{.spec.containers[*].name}'  2> /dev/null`
        do
            # Only exec Pod in Running state
            if [[ "$pod_status" == "Running" ]]; then
                $kube_cmd exec $pod -c $container -- env  > ${env_dir}/${pod}_${container}_env.txt  2> /dev/null
	    fi
        done

        for initContainer in `$kube_cmd get pod $pod -o jsonpath='{.spec.initContainers[*].name}' 2> /dev/null`
        do
            $kube_cmd exec $pod -c $initContainer -- env  > ${env_dir}/${pod}_${initContainer}_env.txt  2> /dev/null
        done

    done
}

get_helm_info() {

    if [[ ! -z "$helm2_cmd" ]]; then
        #echo "-----------------------------------------"
        echo "- Getting Helm2 logs -"
        #echo "-----------------------------------------"
        #echo "-----------------------------------------"


        local helm_dir=${log_base_path}/helm2
        mkdir ${helm_dir}
        $helm2_cmd list 2> /dev/null > ${helm_dir}/helm_deployments.txt
        $helm2_cmd version > ${helm_dir}/helm_version.txt

        for deployment in `$helm2_cmd list 2> /dev/null | grep -v NAME | awk '{print $1}'`
        do
            #echo $deployment
            $helm2_cmd get $deployment > ${helm_dir}/${deployment}_get.txt  2> /dev/null
            $helm2_cmd get hooks $deployment > ${helm_dir}/${deployment}_get_hooks.txt  2> /dev/null
            $helm2_cmd get manifest $deployment > ${helm_dir}/${deployment}_get_manifest.txt  2> /dev/null
            $helm2_cmd get values $deployment > ${helm_dir}/${deployment}_get_values.txt  2> /dev/null
            $helm2_cmd get notes $deployment > ${helm_dir}/${deployment}_get_notes.txt  2> /dev/null
            $helm2_cmd status $deployment > ${helm_dir}/${deployment}_status.txt   2> /dev/null  # needed by PM server
            $helm2_cmd history $deployment > ${helm_dir}/${deployment}_history.txt   2> /dev/null # needed by PM server
        done
    fi
}

get_helm3_info() {

    if  [[ ! -z "$helm3_cmd" ]]; then

      #echo "-----------------------------------------"
      echo "- Getting Helm3 logs -"
      #echo "-----------------------------------------"
      #echo "-----------------------------------------"

      local helm3_dir=${log_base_path}/helm3
      mkdir ${helm3_dir}

      $helm3_cmd version > ${helm3_dir}/helm3_version.txt

      if [ "$collect_all_ns" == "1"  ] ; then
          $helm3_cmd list -a -A 2> /dev/null > ${helm3_dir}/helm3_deployments_-a_-A.txt

          for namesp in `$helm3_cmd list -a -A | grep -v NAME | awk '{print $2}' | sort | uniq`
          do
              mkdir ${helm3_dir}/${namesp}

              for deployment in `$helm3_cmd list -n ${namesp} -a | grep -v NAME | awk '{print $1}' | sort | uniq `
              do
                  $helm3_cmd get all $deployment --namespace $namesp > ${helm3_dir}/${namesp}/${deployment}_get.txt  2> /dev/null
                  $helm3_cmd get hooks $deployment --namespace $namesp > ${helm3_dir}/${namesp}/${deployment}_get_hooks.txt  2> /dev/null
                  $helm3_cmd get manifest $deployment --namespace $namesp > ${helm3_dir}/${namesp}/${deployment}_get_manifest.txt  2> /dev/null
                  $helm3_cmd get values $deployment --namespace $namesp > ${helm3_dir}/${namesp}/${deployment}_get_values.txt  2> /dev/null
                  $helm3_cmd get notes $deployment --namespace $namesp > ${helm3_dir}/${namesp}/${deployment}_get_notes.txt  2> /dev/null
                  $helm3_cmd status $deployment --namespace $namesp > ${helm3_dir}/${namesp}/${deployment}_status.txt   2> /dev/null  # needed by PM server
                  $helm3_cmd history $deployment --namespace $namesp > ${helm3_dir}/${namesp}/${deployment}_history.txt   2> /dev/null  # needed by PM server
              done
          done

      else
          $helm3_cmd list -a 2> /dev/null > ${helm3_dir}/helm3_deployments_-a.txt

          export namesp=$namespace

          mkdir ${helm3_dir}/${namesp}

          for deployment in `$helm3_cmd list -n ${namesp} -a | grep -v NAME | awk '{print $1}' | sort | uniq `
          do
              $helm3_cmd get all $deployment --namespace $namesp > ${helm3_dir}/${namesp}/${deployment}_get.txt  2> /dev/null
              $helm3_cmd get hooks $deployment --namespace $namesp > ${helm3_dir}/${namesp}/${deployment}_get_hooks.txt  2> /dev/null
              $helm3_cmd get manifest $deployment --namespace $namesp > ${helm3_dir}/${namesp}/${deployment}_get_manifest.txt  2> /dev/null
              $helm3_cmd get values $deployment --namespace $namesp > ${helm3_dir}/${namesp}/${deployment}_get_values.txt  2> /dev/null
              $helm3_cmd get notes $deployment --namespace $namesp > ${helm3_dir}/${namesp}/${deployment}_get_notes.txt  2> /dev/null
              $helm3_cmd status $deployment --namespace $namesp > ${helm3_dir}/${namesp}/${deployment}_status.txt   2> /dev/null  # needed by PM server
              $helm3_cmd history $deployment --namespace $namesp > ${helm3_dir}/${namesp}/${deployment}_history.txt   2> /dev/null  # needed by PM server
          done

      fi

    fi
}

cmm_log() {

    #echo "-----------------------------------------"
    echo "- Getting CM logs -"
    #echo "-----------------------------------------"
    #echo "-----------------------------------------"

    # NOTE, the CM-Yang provider doesn't have a script 'collect_logs', only CM-Mediator

    local cmm_log_dir=${log_base_path}/cmm_log
    mkdir ${cmm_log_dir}

    if ! [[ $($kube_cmd get pods -l app.kubernetes.io/name=eric-cm-mediator --field-selector=status.phase=Running --no-headers 2>&1) =~ "No resources found" ]]
    then
        #echo "CM Pods found running, gathering cmm_logs.."
        for pod in `$kube_cmd get pods -l app.kubernetes.io/name=eric-cm-mediator  --field-selector=status.phase=Running -o jsonpath='{.items[*].metadata.name}' 2> /dev/null`
        do
          #echo $pod
          $kube_cmd exec ${pod} -- collect_logs > ${cmm_log_dir}/cmmlog_${pod}.tgz  2> /dev/null
        done
    else
        echo "CM containers not found or not running, doing nothing" > ${cmm_log_dir}/Not_found.txt
    fi
}


sip_dced_logs() {
  # not sure if everything is covered by the new script

    #echo "-----------------------------------------"
    echo "- Getting SIP-TLS, DCED and KMS logs -"
    #echo "-----------------------------------------"
    #echo "-----------------------------------------"

    local siptls_log_dir=${log_base_path}/sip_kms_dced
    mkdir ${siptls_log_dir}

    if ! [[ $($kube_cmd get pods -l app=eric-sec-sip-tls --field-selector=status.phase=Running --no-headers 2>&1) =~ "No resources found" ]]
    then
        mkdir ${siptls_log_dir}/siptls

        #echo "SIP-TLS Pods found, gathering siptls_logs.."
        for pod in `$kube_cmd get pods -l app=eric-sec-sip-tls -o jsonpath='{.items[*].metadata.name}' 2> /dev/null`
        do
            #echo $pod
            $kube_cmd exec $pod -c sip-tls -- /bin/bash /sip-tls/sip-tls-alive.sh 2> /dev/null && echo $? >> ${siptls_log_dir}/siptls/liveness_log_${pod}.txt  2> /dev/null
            $kube_cmd exec $pod -c sip-tls -- /bin/bash /sip-tls/sip-tls-liveness.sh 2> /dev/null && echo $? >> ${siptls_log_dir}/siptls/liveness_log_${pod}.txt  2> /dev/null

            $kube_cmd exec $pod -c sip-tls -- /bin/bash /sip-tls/sip-tls-readiness.sh 2> /dev/null && echo $? >> ${siptls_log_dir}/siptls/readiness_log_${pod}.txt  2> /dev/null
            $kube_cmd exec $pod -c sip-tls -- /bin/bash /sip-tls/sip-tls-ready.sh 2> /dev/null && echo $? >> ${siptls_log_dir}/siptls/readiness_log_${pod}.txt  2> /dev/null
            echo "POD logs and environment variables are found in the standard log directories 'pod_logs' and 'pod_env'" > ${siptls_log_dir}/siptls/README.txt
        done

        $kube_cmd get crd servercertificates.com.ericsson.sec.tls -o yaml  > ${siptls_log_dir}/siptls/servercertificates_crd.txt  2> /dev/null
        $kube_cmd get servercertificates -o yaml  > ${siptls_log_dir}/siptls/servercertificates.txt  2> /dev/null
        $kube_cmd get crd clientcertificates.com.ericsson.sec.tls -o yaml  > ${siptls_log_dir}/siptls/clientcertificates_crd.txt  2> /dev/null
        $kube_cmd get clientcertificates -o yaml  > ${siptls_log_dir}/siptls/clientcertificates.txt  2> /dev/null
        $kube_cmd get crd certificateauthorities.com.ericsson.sec.tls -o yaml  > ${siptls_log_dir}/siptls/certificateauthorities_crd.txt  2> /dev/null
        $kube_cmd get certificateauthorities -o yaml  > ${siptls_log_dir}/siptls/certificateauthorities.txt  2> /dev/null
        $kube_cmd get crd internalcertificates.siptls.sec.ericsson.com -o yaml  > ${siptls_log_dir}/siptls/internalcertificates_crd.txt  2> /dev/null
        $kube_cmd get internalcertificates -o yaml  > ${siptls_log_dir}/siptls/internalcertificates.txt  2> /dev/null
        $kube_cmd get crd internalusercas.siptls.sec.ericsson.com -o yaml  > ${siptls_log_dir}/siptls/internalusercas_crd.txt  2> /dev/null
        $kube_cmd get internalusercas -o yaml  > ${siptls_log_dir}/siptls/internalusercas.txt  2> /dev/null
        $kube_cmd get secret -l com.ericsson.sec.tls/created-by=eric-sec-sip-tls > ${siptls_log_dir}/siptls/secrets_created_by_eric_sip.txt  2> /dev/null
        pod_name=$($kube_cmd get po -l app=eric-sec-key-management -o jsonpath="{.items[0].metadata.name}"  2> /dev/null)
        $kube_cmd exec $pod_name -c kms -- env VAULT_SKIP_VERIFY=true vault status > ${siptls_log_dir}/siptls/kms_status.txt  2> /dev/null
    else
        echo "SIP-TLS containers not found or not running, doing nothing" > ${siptls_log_dir}/Not_found.txt
    fi

    if ! [[ $($kube_cmd get pods -l app=eric-data-distributed-coordinator-ed --field-selector=status.phase=Running --no-headers 2>&1) =~ "No resources found" ]]
    then
        mkdir ${siptls_log_dir}/dced

        for i in {0..2}; do $kube_cmd exec eric-data-distributed-coordinator-ed-${i} -c dced -- bash -c "export ETCDCTL_ENDPOINTS=https://localhost:2379 ; etcdctl member list --insecure-skip-tls-verify=true -w json"  > ${siptls_log_dir}/dced/member_list_ed-${i}.json 2> /dev/null ; done
        for i in {0..2}; do $kube_cmd exec eric-data-distributed-coordinator-ed-${i} -c dced -- bash -c "export ETCDCTL_ENDPOINTS=https://localhost:2379 ; etcdctl user list --insecure-skip-tls-verify=true -w json"  > ${siptls_log_dir}/dced/user_list_ed-${i}.json 2> /dev/null ; done
        for i in {0..2}; do $kube_cmd exec eric-data-distributed-coordinator-ed-${i} -c dced -- bash -c "export ETCDCTL_ENDPOINTS=https://localhost:2379 ; etcdctl alarm list --insecure-skip-tls-verify=true -w json"  > ${siptls_log_dir}/dced/alarm_list_ed-${i}.json 2> /dev/null ; done

        for i in {0..2}; do $kube_cmd exec eric-data-distributed-coordinator-ed-${i} -c dced -- bash -c "export ETCDCTL_ENDPOINTS=https://localhost:2379 ; etcdctl endpoint status --insecure-skip-tls-verify=true -w json"  > ${siptls_log_dir}/dced/endpoint_status_ed-${i}.json 2> /dev/null ; done
        for i in {0..2}; do $kube_cmd exec eric-data-distributed-coordinator-ed-${i} -c dced -- bash -c "export ETCDCTL_ENDPOINTS=https://localhost:2379 ; etcdctl endpoint health --insecure-skip-tls-verify=true -w json"  > ${siptls_log_dir}/dced/endpoint_health_ed-${i}.json 2> /dev/null ; done

        for i in {0..2}; do $kube_cmd exec eric-data-distributed-coordinator-ed-${i} -c dced -- bash -c "export ETCDCTL_ENDPOINTS=https://localhost:2379 ; etcdctl lease list --insecure-skip-tls-verify=true -w json"  > ${siptls_log_dir}/dced/lease_list_ed-${i}.json 2> /dev/null ; done

        for i in {0..2}; do $kube_cmd exec eric-data-distributed-coordinator-ed-${i} -c dced -- bash -c "export ETCDCTL_ENDPOINTS=https://localhost:2379 ; etcdctl get /kms/core/lock --prefix --insecure-skip-tls-verify=true -w json"  > ${siptls_log_dir}/dced/kms_core_lock_ed-${i}.json 2> /dev/null ; done
        for i in {0..2}; do $kube_cmd exec eric-data-distributed-coordinator-ed-${i} -c dced -- bash -c "export ETCDCTL_ENDPOINTS=https://localhost:2379 ; etcdctl get /kms/core/leader --prefix --keys-only --insecure-skip-tls-verify=true -w json"  > ${siptls_log_dir}/dced/kms_core_leader_ed-${i}.json 2> /dev/null ; done
        for i in {0..2}; do $kube_cmd exec eric-data-distributed-coordinator-ed-${i} -c dced -- bash -c "export ETCDCTL_ENDPOINTS=https://localhost:2379 ; etcdctl get /shelter/core/lock --prefix --insecure-skip-tls-verify=true -w json"  > ${siptls_log_dir}/dced/shelter_core_lock_ed-${i}.json 2> /dev/null ; done
        for i in {0..2}; do $kube_cmd exec eric-data-distributed-coordinator-ed-${i} -c dced -- bash -c "export ETCDCTL_ENDPOINTS=https://localhost:2379 ; etcdctl get /shelter/core/leader --prefix --keys-only --insecure-skip-tls-verify=true -w json"  > ${siptls_log_dir}/dced/shelter_core_leader_ed-${i}.json 2> /dev/null ; done
        for i in {0..2}; do $kube_cmd exec eric-data-distributed-coordinator-ed-${i} -c dced -- bash -c "export ETCDCTL_ENDPOINTS=https://localhost:2379 ; etcdctl get / --prefix --insecure-skip-tls-verify=true -w json"  > ${siptls_log_dir}/dced/dced_db_dump_ed-${i}.json 2> /dev/null ; done

        for i in {0..2}; do $kube_cmd exec eric-data-distributed-coordinator-ed-${i} -c dced -- bash -c "export ETCDCTL_ENDPOINTS=https://localhost:2379 ; etcdctl lease list --insecure-skip-tls-verify=true -w json"  > ${siptls_log_dir}/dced/lease_list_ed-${i}.json 2> /dev/null ; done

        for i in {0..2}; do $kube_cmd exec eric-data-distributed-coordinator-ed-${i} -c dced -- df -h > ${siptls_log_dir}/dced/df_ed-${i}.txt 2> /dev/null ; done

        for i in {0..2}; do $kube_cmd exec eric-data-distributed-coordinator-ed-${i} -c dced -- bash  -c 'du -sh data/*;du -sh data/member/*;du -sh data/member/snap/db' > ${siptls_log_dir}/dced/du_ed-${i}.txt 2> /dev/null ; done

        for i in {0..2}; do $kube_cmd exec eric-data-distributed-coordinator-ed-${i} -c dced -- find /data/member -printf "%p %k KB\n" > ${siptls_log_dir}/dced/ls_ed-${i}.txt 2> /dev/null ; done

    else
        echo "DCED POD not found or not running, doing nothing" > ${siptls_log_dir}/DECD_not_found.txt
    fi

    if ! [[ $($kube_cmd get pods -l app=eric-sec-key-management --field-selector=status.phase=Running --no-headers 2>&1) =~ "No resources found" ]]
    then
           #echo "Gathering information to check split brain on KMS"

           kmsspbr=${siptls_log_dir}/KMS_splitbrain_check
           mkdir ${kmsspbr}

           $kube_cmd exec eric-sec-key-management-main-0 -c kms -- bash -c "date;export VAULT_ADDR=http://localhost:8202;echo 'KMS-0';vault status -tls-skip-verify|grep 'HA Enabled' -A3" >> ${kmsspbr}/vault_Stat_HA.log 2> /dev/null
           $kube_cmd exec eric-sec-key-management-main-1 -c kms -- bash -c "export VAULT_ADDR=http://localhost:8202;echo 'KMS-1';vault status -tls-skip-verify|grep 'HA Enabled' -A3" >> ${kmsspbr}/vault_Stat_HA.log 2> /dev/null
           $kube_cmd exec eric-sec-key-management-main-0 -c shelter -- bash -c "export VAULT_ADDR=http://localhost:8212;echo 'SHELTER-0';vault status -tls-skip-verify|grep 'HA Enabled' -A3" >> ${kmsspbr}/vault_Stat_HA.log 2> /dev/null
           $kube_cmd exec eric-sec-key-management-main-1 -c shelter -- bash -c "export VAULT_ADDR=http://localhost:8212;echo 'SHELTER-1';vault status -tls-skip-verify|grep 'HA Enabled' -A3" >> ${kmsspbr}/vault_Stat_HA.log 2> /dev/null

           echo "echo 'KMS-0'" >> ${kmsspbr}/active_operation.log; $kube_cmd logs eric-sec-key-management-main-0 -c kms 2> /dev/null | grep -e "active operation" -e "standby mode" >> ${kmsspbr}/active_operation.log
           echo "echo 'KMS-1'" >> ${kmsspbr}/active_operation.log; $kube_cmd logs eric-sec-key-management-main-1 -c kms 2> /dev/null | grep -e "active operation" -e "standby mode" >> ${kmsspbr}/active_operation.log
           echo "echo 'SHELTER-0'" >> ${kmsspbr}/active_operation.log; $kube_cmd logs eric-sec-key-management-main-0 -c shelter 2> /dev/null | grep -e "active operation" -e "standby mode" >> ${kmsspbr}/active_operation.log
           echo "echo 'SHELTER-1'" >> ${kmsspbr}/active_operation.log; $kube_cmd logs eric-sec-key-management-main-1 -c shelter 2> /dev/null | grep -e "active operation" -e "standby mode" >> ${kmsspbr}/active_operation.log

    else
        echo "KMS PODs not found or not running, doing nothing" > ${siptls_log_dir}/KMS_not_found.txt
    fi

}


cmy_log() {

    #echo "-----------------------------------------"
    echo "- Getting CM Yang logs -"
    #echo "-----------------------------------------"
    #echo "-----------------------------------------"

    # NOTE, not needed, fetched above already, also the output is plain text, not *.tgz

    local cmy_log_dir=${log_base_path}/cmy_log
    mkdir -p ${cmy_log_dir}

    if ! [[ $($kube_cmd get pods -l app=eric-cm-yang-provider --field-selector=status.phase=Running --no-headers 2>&1) =~ "No resources found" ]]
      then
        # echo "CM Yang Pods found running, gathering cmyang_logs.."
          for i in `$kube_cmd get pods  -l app=eric-cm-yang-provider -o jsonpath='{.items[*].metadata.name}' 2> /dev/null`
            do
              # echo $i
              mkdir -p ${cmy_log_dir}/sssd_$i/
              $kube_cmd cp $i:/var/log/sssd   ${cmy_log_dir}/sssd_$i/ -c sshd &> /dev/null
            done
    else
         echo "CM Yang containers not found or not running, doing nothing" > ${cmy_log_dir}/Not_found.txt
    fi
}

get_document_db_pg_logs() {

#from data collection document:
#kubectl cp eric-data-document-database-pg-0:/var/lib/postgresql/data/pgdata/log/ ./eric-data-document-database-pg-0_pgdata_log/ --namespace=<pod_namespace>

    echo "- Getting Document Database PG logs -"
    local db_pg_dir=${log_base_path}/db_pg
    mkdir ${db_pg_dir}

    if ! [[ $($kube_cmd get pods -l app=eric-data-document-database-pg --field-selector=status.phase=Running --no-headers 2>&1) =~ "No resources found" ]]
    then
        # echo "Document Database PG pod found running."
        for pod in `$kube_cmd get pods -l app=eric-data-document-database-pg -o jsonpath='{.items[*].metadata.name}' 2> /dev/null`
        do
            #echo $pod
            $kube_cmd cp -c eric-data-document-database-pg ${pod}:/var/lib/postgresql/data/pgdata/ ${db_pg_dir}/${pod}_log/ &> /dev/null
            rm -rf ${db_pg_dir}/${pod}_log/pg_wal ${db_pg_dir}/${pod}_log/base  2> /dev/null
    # added by SC:
            $kube_cmd exec -c eric-data-document-database-pg -it ${pod} -- psql -U postgres -c '\l' -c '\c sc_database' -c '\dt+' -c '\du+'  > ${db_pg_dir}/${pod}_log/psql_sc_database.log  2> /dev/null
            $kube_cmd exec -c eric-data-document-database-pg -it ${pod} -- psql -U postgres -c '\l' -c '\c postgres' -c '\dt+' -c '\du+'  > ${db_pg_dir}/${pod}_log/psql_postgres.log  2> /dev/null
            $kube_cmd exec -c eric-data-document-database-pg -it ${pod} -- pg_dumpall -U postgres > ${db_pg_dir}/${pod}_log/pg_dumpall.txt  2> /dev/null
        done
    elif ! [[ $($kube_cmd get pods -l app=eric-cm-mediator-db-pg --field-selector=status.phase=Running --no-headers 2>&1) =~ "No resources found" ]]  # SC 1.15+
    then
        # echo "Document Database PG pod found running."
        for pod in `$kube_cmd get pods -l app=eric-cm-mediator-db-pg -o jsonpath='{.items[*].metadata.name}' 2> /dev/null`
        do
            #echo $pod
            $kube_cmd cp -c eric-cm-mediator-db-pg ${pod}:/var/lib/postgresql/data/pgdata/ ${db_pg_dir}/${pod}_log/ &> /dev/null
            rm -rf ${db_pg_dir}/${pod}_log/pg_wal ${db_pg_dir}/${pod}_log/base  2> /dev/null
    # added by SC:
            $kube_cmd exec -c eric-cm-mediator-db-pg -it ${pod} -- psql -U postgres -c '\l' -c '\c sc_database' -c '\dt+' -c '\du+'  > ${db_pg_dir}/${pod}_log/psql_sc_database.log  2> /dev/null
            $kube_cmd exec -c eric-cm-mediator-db-pg -it ${pod} -- psql -U postgres -c '\l' -c '\c postgres' -c '\dt+' -c '\du+'  > ${db_pg_dir}/${pod}_log/psql_postgres.log  2> /dev/null
            $kube_cmd exec -c eric-cm-mediator-db-pg -it ${pod} -- pg_dumpall -U postgres > ${db_pg_dir}/${pod}_log/pg_dumpall.txt  2> /dev/null
        done
    else
        echo "Document Database PG pod not found or not running, doing nothing" > ${db_pg_dir}/Not_found.txt
    fi
}


get_ldap_configuration() {

    echo "- Getting data from eric-sec-ldap-server - "

    local ldap_config_dir=${log_base_path}/ldap_config

    mkdir ${ldap_config_dir}

    if ! [[ $($kube_cmd get pods -l app=eric-sec-ldap-server --field-selector=status.phase=Running --no-headers 2>&1) =~ "No resources found" ]]
    then
        #echo "ldap-server pod(s) found running"
        for pod in `$kube_cmd get pods -l app=eric-sec-ldap-server -o jsonpath='{.items[*].metadata.name}' 2> /dev/null`
        do
            mkdir -p ${ldap_config_dir}/${pod}_log
            $kube_cmd exec -it ${pod} -c ldap -- ldapsearch -Y EXTERNAL -H ldapi://%2Frun%2Fslapd%2Fslapd.sock -b "ou=people,dc=la,dc=adp,dc=ericsson" > ${ldap_config_dir}/${pod}_log/ldap_users.txt  2> /dev/null
            $kube_cmd exec -it ${pod} -c ldap -- ldapsearch -Y EXTERNAL -H ldapi://%2Frun%2Fslapd%2Fslapd.sock -b "ou=peopleextraattrs,dc=la,dc=adp,dc=ericsson" > ${ldap_config_dir}/${pod}_log/ldap_users_lastLoginTime.txt  2> /dev/null
            $kube_cmd exec -it ${pod} -c ldap -- ldapsearch -Y EXTERNAL -H ldapi://%2Frun%2Fslapd%2Fslapd.sock -b "ou=policies,dc=la,dc=adp,dc=ericsson" > ${ldap_config_dir}/${pod}_log/ldap_policies.txt  2> /dev/null
            $kube_cmd exec -it ${pod} -c ldap -- ldapsearch -Y EXTERNAL -H ldapi://%2Frun%2Fslapd%2Fslapd.sock -b "olcDatabase={1}ldap,cn=config" > ${ldap_config_dir}/${pod}_log/ldap_config.txt  2> /dev/null
            $kube_cmd exec -it ${pod} -c ldap -- cat /proc/net/tcp > ${ldap_config_dir}/${pod}_log/proc_net_tcp.txt  2> /dev/null
            $kube_cmd exec -it ${pod} -c ldap -- openssl s_client -connect localhost:1389 > ${ldap_config_dir}/${pod}_log/openssl_localhost_1389.txt  2> /dev/null
            #$kube_cmd exec -it ${pod} -- ldapsearch -x -b '' -s base '(objectclass=top)' namingContexts > ${ldap_config_dir}/${pod}_log/ldap_health.txt  2> /dev/null
        done
    else
        echo "ldap server pod not found or not running, doing nothing" > ${ldap_config_dir}/Not_found_LDAP_server.txt
    fi

    if ! [[ $($kube_cmd get pods -l app=eric-sec-ldap-server-proxy --field-selector=status.phase=Running --no-headers 2>&1) =~ "No resources found" ]]
    then
        for pod in `$kube_cmd get pods -l app=eric-sec-ldap-server-proxy -o jsonpath='{.items[*].metadata.name}' 2> /dev/null`
        do
            mkdir -p ${ldap_config_dir}/${pod}_log
            $kube_cmd exec -it ${pod} -c ldapproxy -- openssl s_client -connect eric-sec-ldap-server-0.eric-sec-ldap-server-peer:1389 > ${ldap_config_dir}/${pod}_log/openssl_ldap_server_1389.txt  2> /dev/null
        done
    else
        echo "ldap proxy pod not found or not running, doing nothing" > ${ldap_config_dir}/Not_found_LDAP_proxy.txt
    fi

    if ! [[ $($kube_cmd get pods -l app=eric-sec-admin-user-management --field-selector=status.phase=Running --no-headers 2>&1) =~ "No resources found" ]]
    then
        for pod in `$kube_cmd get pods -l app=eric-sec-admin-user-management -o jsonpath='{.items[*].metadata.name}' 2> /dev/null`
        do
            mkdir -p ${ldap_config_dir}/${pod}_log
            timeout 2 $kube_cmd exec -it ${pod} -- openssl s_client -connect eric-sec-ldap-server:636 > ${ldap_config_dir}/${pod}_log/openssl_ldap_service_636.txt  2> /dev/null
        done
    else
        echo "AUM pod not found or not running, doing nothing" > ${ldap_config_dir}/Not_found_AUM.txt
    fi
}


get_config_cmm(){

    echo "- Getting config from CM_Mediator"

    local cmm_config_dir=${log_base_path}/cmm_config

    mkdir -p ${cmm_config_dir}

    if ! [[ $($kube_cmd get pods -l app=eric-cm-mediator --field-selector=status.phase=Running --no-headers 2>&1) =~ "No resources found" ]]
    then
        #echo "CM Pods found running, getting all configurations from CM..."
        local cm_pod=$($kube_cmd get pods -l app=eric-cm-mediator --field-selector=status.phase=Running -o jsonpath='{.items[0].metadata.name}' 2> /dev/null )
        local cm_port=$($kube_cmd get -o jsonpath="{.spec.ports[0].port}" services eric-cm-mediator  2> /dev/null)

        $kube_cmd exec ${cm_pod} -i -c eric-cm-mediator -- curl -s "http://localhost:${cm_port}/cm/api/v1/schemas" > ${cmm_config_dir}/schemas.json  2> /dev/null
        if [ $? -ne 0 ]; then
        	$kube_cmd exec ${cm_pod} -i -c eric-cm-mediator -- curl -s "https://localhost:${cm_port}/cm/api/v1/schemas" --cacert /run/secrets/eric-sec-sip-tls-trusted-root-cert/ca.crt --cert /run/secrets/eric-cm-mediator-tls-client-secret/clicert.pem --cert-type PEM --key /run/secrets/eric-cm-mediator-tls-client-secret/cliprivkey.pem --key-type PEM > ${cmm_config_dir}/schemas.json  2> /dev/null
            if [ $? -ne 0 ]; then
                $kube_cmd exec ${cm_pod} -i -c eric-cm-mediator -- curl -s "https://localhost:${cm_port}/cm/api/v1/schemas" --cacert /etc/sip-tls-ca/ca.crt --cert /etc/sip-tls-client/clicert.pem --cert-type PEM --key /etc/sip-tls-client/cliprivkey.pem --key-type PEM > ${cmm_config_dir}/schemas.json  2> /dev/null
            fi
        fi
        $kube_cmd exec ${cm_pod} -i -c eric-cm-mediator -- curl -s "http://localhost:${cm_port}/cm/api/v1/configurations" > ${cmm_config_dir}/configurations.json  2> /dev/null
        if [ $? -ne 0 ]; then
        	$kube_cmd exec ${cm_pod} -i -c eric-cm-mediator -- curl -s "https://localhost:${cm_port}/cm/api/v1/configurations" --cacert /run/secrets/eric-sec-sip-tls-trusted-root-cert/ca.crt --cert /run/secrets/eric-cm-mediator-tls-client-secret/clicert.pem --cert-type PEM --key /run/secrets/eric-cm-mediator-tls-client-secret/cliprivkey.pem --key-type PEM > ${cmm_config_dir}/configurations.json  2> /dev/null
            if [ $? -ne 0 ]; then
                $kube_cmd exec ${cm_pod} -i -c eric-cm-mediator -- curl -s "https://localhost:${cm_port}/cm/api/v1/configurations" --cacert /etc/sip-tls-ca/ca.crt --cert /etc/sip-tls-client/clicert.pem --cert-type PEM --key /etc/sip-tls-client/cliprivkey.pem --key-type PEM > ${cmm_config_dir}/configurations.json  2> /dev/null
            fi
        fi

        for config in `sed -E 's/\},\s*\{/\},\n\{/g' ${cmm_config_dir}/configurations.json | awk -F \" '{print $4}'`
        do
            $kube_cmd exec ${cm_pod} -i -c eric-cm-mediator -- curl -s "http://localhost:${cm_port}/cm/api/v1/configurations/${config}" > ${cmm_config_dir}/${config}_config.json  2> /dev/null
	        if [ $? -ne 0 ]; then
	        	$kube_cmd exec ${cm_pod} -i -c eric-cm-mediator -- curl -s "https://localhost:${cm_port}/cm/api/v1/configurations/${config}" --cacert /run/secrets/eric-sec-sip-tls-trusted-root-cert/ca.crt --cert /run/secrets/eric-cm-mediator-tls-client-secret/clicert.pem --cert-type PEM --key /run/secrets/eric-cm-mediator-tls-client-secret/cliprivkey.pem --key-type PEM > ${cmm_config_dir}/${config}_config.json  2> /dev/null
	            if [ $? -ne 0 ]; then
	            	$kube_cmd exec ${cm_pod} -i -c eric-cm-mediator -- curl -s "https://localhost:${cm_port}/cm/api/v1/configurations/${config}" --cacert /run/secrets/eric-sec-sip-tls-trusted-root-cert/ca.crt --cacert /etc/sip-tls-ca/ca.crt --cert /etc/sip-tls-client/clicert.pem --cert-type PEM --key /etc/sip-tls-client/cliprivkey.pem --key-type PEM > ${cmm_config_dir}/${config}_config.json  2> /dev/null
	            fi
	        fi
        done

        for schema in `sed -E 's/\},\s*\{/\},\n\{/g' ${cmm_config_dir}/schemas.json | awk -F \" '{print $4}'`
        do
            $kube_cmd exec ${cm_pod} -i -c eric-cm-mediator -- curl -s "http://localhost:${cm_port}/cm/api/v1/schemas/${schema}" > ${cmm_config_dir}/${schema}_schema.json  2> /dev/null
	        if [ $? -ne 0 ]; then
	        	$kube_cmd exec ${cm_pod} -i -c eric-cm-mediator -- curl -s "https://localhost:${cm_port}/cm/api/v1/schemas/${schema}" --cacert /run/secrets/eric-sec-sip-tls-trusted-root-cert/ca.crt --cert /run/secrets/eric-cm-mediator-tls-client-secret/clicert.pem --cert-type PEM --key /run/secrets/eric-cm-mediator-tls-client-secret/cliprivkey.pem --key-type PEM > ${cmm_config_dir}/${schema}_schema.json  2> /dev/null
	            if [ $? -ne 0 ]; then
	        	    $kube_cmd exec ${cm_pod} -i -c eric-cm-mediator -- curl -s "https://localhost:${cm_port}/cm/api/v1/schemas/${schema}" --cacert /run/secrets/eric-sec-sip-tls-trusted-root-cert/ca.crt --cacert /etc/sip-tls-ca/ca.crt --cert /etc/sip-tls-client/clicert.pem --cert-type PEM --key /etc/sip-tls-client/cliprivkey.pem --key-type PEM> ${cmm_config_dir}/${schema}_schema.json  2> /dev/null
	           fi
	        fi
        done

    else
         echo "CM containers not found or not running, doing nothing" > ${cmm_config_dir}/Not_found.txt
    fi
}


cmyp_yang_schemas() {

    echo "- Collect YANG schemas -"

    local cmy_log_dir=${log_base_path}/cmy_log

    mkdir -p ${cmy_log_dir}

    DBNAME=$($kube_cmd describe pod -l "app=eric-cm-yang-provider" 2> /dev/null | grep -h POSTGRES_DBNAME: | head -1 | gawk '{print $2}' | tr -Cd ' a-zA-Z0-9_-')
    ddb_service=$($kube_cmd describe pod -l "app=eric-cm-yang-provider" 2> /dev/null | grep -h POSTGRES_SERVICE_HOST: | head -1 | gawk '{print $2}' | tr -Cd ' a-zA-Z0-9_-')
    ddb=$($kube_cmd  get pod -l app="$($kube_cmd  describe svc "$ddb_service" 2> /dev/null  | grep app= | cut -d = -f2)" 2> /dev/null | grep Running | head -n 1 | awk '{print $1}' | tr -Cd ' a-zA-Z0-9_-')

    echo
    if [[ $ddb == "" ]]; then
        echo "WARNING: No backend DB Running for CMYP"
        return
    fi

    DDB_CMD="$kube_cmd exec ${ddb} -c ${ddb_service} -- /bin/bash -c"
    YANG_PATH=$(mktemp -d -u "/tmp/yangSchemas.XXXXXX")
    LOCAL_PATH=${cmy_log_dir}/schemas_${ddb}/
    mkdir -p "${LOCAL_PATH}"

    yangNames=$(${DDB_CMD} "echo \"SELECT name FROM yangschemas\" | /usr/bin/psql --quiet --tuples-only -d ${DBNAME} -U postgres" )

    ${DDB_CMD} "if [ -d ${YANG_PATH} ]; then rm -rf ${YANG_PATH}; fi; mkdir -p ${YANG_PATH}"

    for n in ${yangNames}
    do
        fetch="echo \"SELECT data FROM yangschemas WHERE name='${n}'\" | /usr/bin/psql --quiet --tuples-only -d ${DBNAME} -U postgres > ${YANG_PATH}/${n}"
        ${DDB_CMD} "${fetch}"
    done

    ${DDB_CMD} "cd ${YANG_PATH} && tar -czf yangSchemas.tar.gz *"
    $kube_cmd cp "${ddb}":"${YANG_PATH}"/yangSchemas.tar.gz "${LOCAL_PATH}"/yangSchemas.tar.gz -c ${ddb_service} &> /dev/null

    tar xzf "${LOCAL_PATH}"/yangSchemas.tar.gz -C "${LOCAL_PATH}"/
    rm -f "${LOCAL_PATH}"/yangSchemas.tar.gz

    if command -v xxd &> /dev/null
    then
        for f in "${LOCAL_PATH}"/*
        do
            [[ -e "$f" ]] || break  # handle the case of no files
            xxd -r -p < "${f}" > "${f}".tar.gz
            rm -f "${f}"
        done
    fi

    ${DDB_CMD} "rm -rf ${YANG_PATH}"
}



get_diameter_logs (){
    #partly covered by new data_collector

    echo "- Getting config from STM Diameter"

    local dia_log_dir=${log_base_path}/dia_logs
    declare -a diameter_pods=("stm")
    declare -a diameter_containers=("diameter")   # dsl container does not give move information than diameter container

    mkdir ${dia_log_dir}

    for dp in "${diameter_pods[@]}"
    do
        if ! [[ $($kube_cmd get pods -l app=eric-$dp-diameter --field-selector=status.phase=Running --no-headers 2>&1) =~ "No resources found" ]]
        then

            for pod in `$kube_cmd get pods -l app=eric-$dp-diameter -o jsonpath='{.items[*].metadata.name}' 2> /dev/null`
            do
                mkdir ${dia_log_dir}/$pod
                if [[ ! $pod =~ "eric-stm-diameter-cm-" ]]
                then
                    for dc in "${diameter_containers[@]}"
                    do
                        # Loop through the containers and in the container
                        # - check the actual Diameter configuration
                        # - dump information about Diameter Peer connections
                        # - check the whole Diameter cluster state
                        # besides connection information, state dump does also
                        # include internal information of the open TCP connection and listening TCP ports
                        $kube_cmd exec $pod -c $dc -i -- curl -s localhost:20100/troubleshoot/transportDump/v2 > ${dia_log_dir}/$pod/${dc}_transportDump.txt  2> /dev/null
                        $kube_cmd exec $pod -c $dc -i -- curl -s localhost:20100/dumpState > ${dia_log_dir}/$pod/${dc}_dumpState.txt  2> /dev/null
                        $kube_cmd exec $pod -c $dc -i -- curl -s localhost:20100/dumpConfig > ${dia_log_dir}/$pod/${dc}_dumpConfig.txt  2> /dev/null

                        $kube_cmd exec $pod -c $dc -i -- ss -t > ${dia_log_dir}/$pod/${dc}_ss_t_logs.txt  2> /dev/null
                        $kube_cmd exec $pod -c $dc -i -- ss -l -t > ${dia_log_dir}/$pod/${dc}_ss_lt_logs.txt  2> /dev/null
                        $kube_cmd exec $pod -c $dc -i -- /opt/dia/bin/ip_resolver.sh > ${dia_log_dir}/$pod/${dc}_ip_resolver.txt  2> /dev/null
                    done
                fi

            done
        else
            echo "$dp-diameter pods not found or not running, doing nothing" > ${dia_log_dir}/${dp}_diameter_pods_not_found.txt
        fi
    done
}

get_ss7_cnf() {

  echo "- Getting config SS7 CNF"

  if ($kube_cmd get pods | grep -i ss7)
  then

    for i in `$kube_cmd get pod -o json | jq -r '.items[] | select(.spec.containers[].name=="ss7") | .metadata.name' 2> /dev/null`
    do
      cnfpath=${log_base_path}/ss7_cnf_$i
      mkdir $cnfpath
      $kube_cmd cp $i:/opt/cnf-dir/ -c ss7 ${cnfpath} &> /dev/null
    done
  fi
}

get_sc_config_and_logs() {

    # config from ENVOY and BSF
    local sc_config_dir=${log_base_path}/sc_config

    declare -a nf=("scp" "sepp" "csa")

    mkdir ${sc_config_dir}

	mkdir -p ${sc_config_dir}/dced
    for i in {0..2}; do $kube_cmd  exec -it eric-data-distributed-coordinator-ed-sc-${i} -c dced -- etcdctl get / --prefix  > ${sc_config_dir}/dced/dced_db_dump_ed-sc-${i}.txt  2> /dev/null; done

    # Loop through the Network Functions that have Envoy configuration
    for nf in "${nf[@]}"
    do
        # echo "$nf"
        # or do whatever with individual element of the array

        if ! [[ $($kube_cmd get pods -l app=eric-$nf-worker --field-selector=status.phase=Running --no-headers 2>&1) =~ "No resources found" ]]
        then
		    echo "- Getting data from eric-$nf-worker containers"

            mkdir -p ${sc_config_dir}/$nf

            #echo "$nf-worker pods found running."
            for i in `$kube_cmd get pods -l app=eric-$nf-worker --field-selector=status.phase=Running -o jsonpath='{.items[*].metadata.name}' 2> /dev/null `
            do
                #echo $i
                mkdir -p ${sc_config_dir}/$nf/$i
                $kube_cmd exec $i -c eric-$nf-worker -i -- curl -s http://localhost:9901/clusters > ${sc_config_dir}/$nf/$i/clusters.txt  2> /dev/null
                $kube_cmd exec $i -c eric-$nf-worker -i -- curl -s http://localhost:9901/config_dump?include_eds > ${sc_config_dir}/$nf/$i/config_dump.txt  2> /dev/null
                $kube_cmd exec $i -c eric-$nf-worker -i -- curl -s http://localhost:9901/contention > ${sc_config_dir}/$nf/$i/contention.txt  2> /dev/null
                $kube_cmd exec $i -c eric-$nf-worker -i -- curl -s http://localhost:9901/memory > ${sc_config_dir}/$nf/$i/memory.txt  2> /dev/null
                $kube_cmd exec $i -c eric-$nf-worker -i -- curl -s http://localhost:9901/hot_restart_version > ${sc_config_dir}/$nf/$i/hot_restart_version.txt  2> /dev/null

                $kube_cmd exec $i -c eric-$nf-worker -i -- curl -s http://localhost:9901/listeners > ${sc_config_dir}/$nf/$i/listeners.txt  2> /dev/null
                $kube_cmd exec $i -c eric-$nf-worker -i -- curl -s http://localhost:9901/ready > ${sc_config_dir}/$nf/$i/ready.txt  2> /dev/null
                $kube_cmd exec $i -c eric-$nf-worker -i -- curl -s http://localhost:9901/runtime > ${sc_config_dir}/$nf/$i/runtime.txt  2> /dev/null
                $kube_cmd exec $i -c eric-$nf-worker -i -- curl -s http://localhost:9901/server_info > ${sc_config_dir}/$nf/$i/server_info.txt  2> /dev/null
                $kube_cmd exec $i -c eric-$nf-worker -i -- curl -s http://localhost:9901/stats > ${sc_config_dir}/$nf/$i/stats.txt  2> /dev/null
                $kube_cmd exec $i -c eric-$nf-worker -i -- curl -s http://localhost:9901/stats/prometheus > ${sc_config_dir}/$nf/$i/prometheus_stats.txt  2> /dev/null
                $kube_cmd exec $i -c eric-$nf-worker -i -- curl -s -X POST http://localhost:9901/logging > ${sc_config_dir}/$nf/$i/logging_status.txt

            done
        else
            echo "$nf-worker pods not found or not running, doing nothing" > ${sc_config_dir}/${nf}_worker_not_found.txt
        fi

    done

    if ! [[ $($kube_cmd get pods -l global_app=eric-bsf-wcdb-cd --field-selector=status.phase=Running --no-headers 2>&1) =~ "No resources found" ]]
    then
        for pod in `$kube_cmd get pods -l global_app=eric-bsf-wcdb-cd -o jsonpath='{.items[*].metadata.name}' 2> /dev/null`
        do
            mkdir -p ${sc_config_dir}/bsf/$pod
            $kube_cmd cp $pod:/usr/share/cassandra/logs/ -c cassandra ${sc_config_dir}/bsf/${pod} &> /dev/null
            gzip ${sc_config_dir}/bsf/$pod/*
            $kube_cmd exec eric-bsf-wcdb-cd-datacenter1-rack1-0 -c cassandra -- sh -c "cd /etc/cassandra/conf && tar chf - cassandra*" | tar xf - -C ${sc_config_dir}/bsf/${pod} &> /dev/null            
            $kube_cmd exec eric-bsf-wcdb-cd-datacenter1-rack1-0 -c cassandra -- sh -c "cd /etc/cassandra/conf && tar chf - *.options" | tar xf - -C ${sc_config_dir}/bsf/${pod} &> /dev/null
        done
    fi
}


get_wireshark_hosts_file(){

    echo "- Getting hosts file for Wireshark - "

    local hosts_dir=${log_base_path}/hosts_file_for_wireshark

    mkdir ${hosts_dir}

    if [ "$collect_all_ns" == "1"  ] ; then
        $kube_cmd get pods -A -o=custom-columns=:.status.podIP,:.metadata.name  2> /dev/null >  ${hosts_dir}/hosts  2> /dev/null
        $kube_cmd get svc  -A -o=custom-columns=:.spec.clusterIP,:.metadata.name 2> /dev/null | grep -v None >> ${hosts_dir}/hosts  2> /dev/null
    else
        $kube_cmd get pods -o=custom-columns=:.status.podIP,:.metadata.name  2> /dev/null >  ${hosts_dir}/hosts  2> /dev/null
        $kube_cmd get svc  -o=custom-columns=:.spec.clusterIP,:.metadata.name 2> /dev/null | grep -v None >> ${hosts_dir}/hosts  2> /dev/null
    fi
    $kube_cmd get nodes   -o=custom-columns=:.status.addresses[0].address,:.metadata.name 2> /dev/null >> ${hosts_dir}/hosts  2> /dev/null

}

get_alarms(){

    echo "- Getting alarms -"

    local alarms_dir=${log_base_path}/alarms

    mkdir ${alarms_dir}

    AH_POD=$($kube_cmd get pods -l app=eric-fh-alarm-handler --field-selector=status.phase=Running -o jsonpath="{.items[0].metadata.name}" 2> /dev/null )

    $kube_cmd exec -it ${AH_POD} -- ah_alarm_list.sh > ${alarms_dir}/active_alarms.json 2> /dev/null

    $kube_cmd exec -it ${AH_POD} -- ah_alarm_list.sh -f > ${alarms_dir}/active_alarms_full.json 2> /dev/null
}


get_rop_files(){
    PMBR_CONTAINERS=($($kube_cmd get deploy eric-pm-bulk-reporter -o jsonpath="{.spec.template.spec.containers[*].name}"))
    if [[ ${PMBR_CONTAINERS[@]} =~ "eric-pm-sftp" ]]; then
        get_pm_xml_files
    else
        get_pm_xml_objectstore_files
    fi
}

get_pm_xml_files(){

    echo "- Getting PM XML file - "

    local pm_dir=${log_base_path}/pm_xml_files

    mkdir -p ${pm_dir}

    PMBR_POD=$($kube_cmd get pods -l app=eric-pm-bulk-reporter --field-selector=status.phase=Running -o jsonpath="{.items[0].metadata.name}" 2> /dev/null )

    $kube_cmd exec -it -c eric-pm-bulk-reporter  ${PMBR_POD} -- ls -la /PerformanceManagementReportFiles | head -n -1 | tail -n +4  > ${pm_dir}/PerformanceManagementReportFiles.txt

    $kube_cmd cp -c eric-pm-bulk-reporter ${PMBR_POD}:/PerformanceManagementReportFiles ${pm_dir}/PerformanceManagementReportFiles &> /dev/null
    if [ $? -ne 0 ]; then
      # If the previous command didn't work (like in SC 1.13), read file names into an array
      IFS=$'\n' read -d '' -r -a lines < ${pm_dir}/PerformanceManagementReportFiles.txt

      # and fetch each file separately
      for line in "${lines[@]}"
      do
        file=$(echo $line | awk '{print $9}' | tr -d '\r\t\n'  ) # remove line feed characters from string
        #echo "/PerformanceManagementReportFiles/${file}"
        $kube_cmd exec -i -c eric-pm-bulk-reporter ${PMBR_POD} -- cat /PerformanceManagementReportFiles/${file} > ${pm_dir}/${file}
      done
    fi;
}


get_pm_xml_objectstore_files(){

    echo "- Getting PM XML file from ObjectStore - "

    local pm_dir=${log_base_path}/pm_xml_files

    mkdir -p ${pm_dir}

    OBJ_POD=$($kube_cmd get pods -l app=eric-data-object-storage-mn-mgt --field-selector=status.phase=Running -o jsonpath='{.items[0].metadata.name}' 2> /dev/null )

    if [ $? -eq 0 ]; then
      if [ ! -z "$OBJ_POD" ]; then
        export MINIO_ACCESS_KEY=$($kube_cmd get secret eric-data-object-storage-mn-secret -o jsonpath='{.data.accesskey}' 2> /dev/null | base64 -d)
        export MINIO_SECRET_KEY=$($kube_cmd get secret eric-data-object-storage-mn-secret -o jsonpath='{.data.secretkey}' 2> /dev/null | base64 -d)
        $kube_cmd exec $OBJ_POD -c manager -i -- bash -c "mc config host add pmbr https://eric-data-object-storage-mn:9000 ${MINIO_ACCESS_KEY} ${MINIO_SECRET_KEY} --insecure &>/dev/null ; mc cp pmbr/eric-pmbr-rop-file-store /dev/shm/  --recursive --insecure &>/dev/null" 2> /dev/null

        $kube_cmd exec -c manager ${OBJ_POD} -- ls -la /dev/shm/eric-pmbr-rop-file-store | head -n -1 | tail -n +4  > ${pm_dir}/PerformanceManagementReportFiles.txt

        $kube_cmd cp ${OBJ_POD}:/dev/shm/eric-pmbr-rop-file-store ${pm_dir}/eric-pmbr-rop-file-store &> /dev/null
        if [ $? -ne 0 ]; then
          # If the previous command didn't work (like in SC 1.15), read file names into an array
          IFS=$'\n' read -d '' -r -a lines < ${pm_dir}/PerformanceManagementReportFiles.txt

          # and fetch each file separately
          for line in "${lines[@]}"
          do
            file=$(echo $line | awk '{print $9}' | tr -d '\r\t\n'  ) # remove line feed characters from string
            #echo "/PerformanceManagementReportFiles/${file}"
            $kube_cmd exec -i -c manager ${OBJ_POD} -- cat /dev/shm/eric-pmbr-rop-file-store/${file} > ${pm_dir}/${file}
          done
        fi;
          
        $kube_cmd exec $OBJ_POD -c manager -i -- bash -c "rm -rf /dev/shm/eric-pmbr-rop-file-store &>/dev/null"
      fi
    fi

}

get_eccd_logs(){

  echo "- Getting ECCD logs - "

  local eccd_dir=${log_base_path}/eccd

  mkdir ${eccd_dir}

  if [ -f "/etc/eccd/eccd_image_version.ini" ]; then
    cat /etc/eccd/eccd_image_version.ini > ${eccd_dir}/eccd_image_version.ini
    sudo docker images > ${eccd_dir}/docker_image.txt 2>/dev/null
    sudo docker ps -a > ${eccd_dir}/docker_ps-a.txt 2>/dev/null
    sudo nerdctl images > ${eccd_dir}/nerdctl_image.txt 2>/dev/null
    sudo nerdctl ps -a > ${eccd_dir}/nerdctl_ps-a.txt 2>/dev/null
  else
    echo "No ECCD environment found or command was not run from ECCD director" > ${eccd_dir}/No_ECCD_environment_found.txt
  fi
}


get_bro_logs(){

  echo "- Getting BRO logs - "

  local bro_dir=${log_base_path}/bro

  mkdir ${bro_dir}

  if ! [[ $($kube_cmd get pods -l app.kubernetes.io/name=eric-ctrl-bro --field-selector=status.phase=Running --no-headers 2>&1) =~ "No resources found" ]]
  then
      #echo "BRO Pods found running, getting all configurations ..."
      for pod in `$kube_cmd get pods -l app.kubernetes.io/name=eric-ctrl-bro -o jsonpath='{.items[*].metadata.name}' 2> /dev/null`
      do
          $kube_cmd cp -c eric-ctrl-bro ${pod}:/bro/backupManagers/DEFAULT/schedulerInformation.json  ${bro_dir}/${pod}_schedulerInformation.json &> /dev/null
          $kube_cmd cp -c eric-ctrl-bro ${pod}:/bro/backupManagers/DEFAULT/periodic-events  ${bro_dir}/${pod}_periodic-events &> /dev/null
      done
  else
      echo "BRO containers not found or not running, doing nothing" > ${bro_dir}/Not_found.txt
  fi
}


get_se_logs(){
  #partly covered by new data_collector

  echo "- Getting SearchEngine(SE) logs - "

  local se_dir=${log_base_path}/se_logs

  mkdir ${se_dir}

  esRest="$kube_cmd exec -c ingest $(${kube_cmd} get pods -l "app=eric-data-search-engine,role in (ingest-tls,ingest)" -o jsonpath="{.items[0].metadata.name}" 2> /dev/null) -- /bin/esRest"
  $esRest GET /_cat/nodes?v > ${se_dir}/nodes.txt  2> /dev/null
  $esRest GET /_cat/indices?v > ${se_dir}/indices.txt  2> /dev/null
  $esRest GET /_cat/shards?v > ${se_dir}/shards.txt  2> /dev/null
  $esRest GET /_cluster/settings?pretty > ${se_dir}/settings.txt  2> /dev/null
  $esRest GET /_cluster/health?pretty > ${se_dir}/health.txt  2> /dev/null
  $esRest GET /_cluster/allocation/explain?pretty > ${se_dir}/allocation.txt  2> /dev/null
  $esRest GET /_cluster/stats?pretty > ${se_dir}/stats.txt  2> /dev/null
  $esRest GET / > ${se_dir}/overview.txt  2> /dev/null
}

get_kvdb_ag_logs(){
  #partly covered by new data_collector

  # move to new way of detecting running pods
  if ($kube_cmd get pods | grep -i kvdb-ag)
  then
      local kv_dir=${log_base_path}/KVDBAG
      mkdir ${kv_dir}

      for i in `$kube_cmd get pods |grep -i kvdb-ag|awk '{print $1}'`
      do
          mkdir  ${kv_dir}/$i
          mkdir  ${kv_dir}/$i/logs
          mkdir  ${kv_dir}/$i/stats
          for j in `$kube_cmd exec $i -- ls /opt/dbservice/data/logs/ 2>/dev/null`
          do
                  $kube_cmd cp $i:/opt/dbservice/data/logs/$j ${kv_dir}/$i/logs/$j &> /dev/null
          done

          for j in `$kube_cmd exec $i -- ls -ltr /opt/dbservice/data/stats/ |tail -3|grep  -vi marker| awk '{print $9}' 2>/dev/null`
          do
                  $kube_cmd cp $i:/opt/dbservice/data/stats/$j ${kv_dir}/$i/stats/$j &> /dev/null
          done
      done
  fi
}

postprocessing1(){

    echo "- Postprocessing 1 - "

    for i in `ls ${log_base_path}/k_describe/PODS`
    do
        version=`cat ${log_base_path}/k_describe/PODS/$i |grep "app.kubernetes.io/version"`
        echo $i $version >> ${log_base_path}/k_describe/PODS/pods_image_versions.txt 2> /dev/null

    done

    find ${log_base_path} > ${log_base_path}/directory_content.txt

}

postprocessing2(){

    echo "- Postprocessing 2 - "

    mkdir  ${log_base_path}/pod_logs/err

    for i in `ls ${log_base_path}/pod_logs/`
    do
      filename=`echo $i| awk '{print substr($1,1,length($1)-4)}'`
      log_path="${log_base_path}/pod_logs/$i"
      if ! [ -d $log_path ]; then
        cat ${log_path} | egrep -i "err|warn|crit" > ${log_base_path}/pod_logs/err/$filename.err.txt
        cat ${log_path} | egrep -i "failed to perform indices:data/write/bulk|latency|failed to send out heartbeat on time|disk|time out|timeout|timed out" > ${log_base_path}/pod_logs/err/$filename.latency.txt
      fi
    done

	#remove empty files
	find ${log_base_path}/pod_logs/err/ -type f -size 0b -delete
}

compress_files() {
    echo "Generating tar file and removing logs directory..."
    cd ${outdir}
    tar cfz ${outdir}/${log_base_dir}.tgz ${log_base_dir}  > /dev/null 2>&1
    echo  -e "\e[1m\e[31mGenerated file ${outdir}/${log_base_dir}.tgz\nPlease collect and send to ADP or SC support!\e[0m"
    rm -r ${outdir}/${log_base_dir}
    cd -
}


get_describe_info &
get_events &
get_pods_logs &
get_pods_env &
get_helm_info &
get_helm3_info &
cmm_log &
sip_dced_logs &
cmy_log &
cmyp_yang_schemas &
get_document_db_pg_logs &
get_ldap_configuration &
##get_sc_config_yang &
get_config_cmm &
get_diameter_logs &
get_ss7_cnf &
get_sc_config_and_logs &
get_wireshark_hosts_file &
get_alarms &
get_rop_files &
get_eccd_logs &
get_bro_logs &
get_se_logs &
get_kvdb_ag_logs &


if [ "$collect_system_logs" == "1"  ] ; then
  get_system_describe_info &
  get_system_pods_logs &
fi;

wait

postprocessing1 &
postprocessing2 &
wait

compress_files
