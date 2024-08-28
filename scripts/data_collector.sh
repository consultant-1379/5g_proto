#!/bin/bash

############################################################################
# Organization "Ericsson AB";                                              #
# Description "Script to collect data for troubleshooting"                 #
# Copyright (c) 2020-2023 Ericsson AB. All rights reserved"                #
############################################################################
# Version: 1.11.0-1                                                        #
# Script to collect data from Kubernetes Cluster                           #
#     For usage, execute data_collector.sh -h                              #
# Corrected version by SC                                                  #
############################################################################

# Use "C" locale to make regular expression patterns like [a-z] work as expected
export LC_ALL=C
if [ -z "${BASH_VERSION}" ]; then
    echo "This script should be run with bash"
    exit 1
fi

REQUIRED_COMMANDS=(
    "basename" "cat" "cp" "cut" "date" "gawk" "grep" "head" \
    "helm" "kubectl" "ls" "md5sum" "mkdir" "mktemp" "renice" \
    "rm" "sed" "sleep" "sort" "tail" "tar" "tee" "tr" "wc")

OPTIONAL_COMMANDS=(
    "base64" "dd"  "jq" "json_pp" "xxd")

if ! type "${REQUIRED_COMMANDS[@]}" >& /dev/null; then
    echo "Required command missing:"
    echo
    type "${REQUIRED_COMMANDS[@]}"
    exit 1
fi

if ! type "${OPTIONAL_COMMANDS[@]}" >& /dev/null; then
    echo -e "\e[33mWarning: optional command missing:" # change the color to yello
    echo
    type "${OPTIONAL_COMMANDS[@]}"
    echo -e "Some features might not work properly\e[0m" # reset the default color
fi
print_usage_and_exit()
{
   # Display Help
   echo "Usage: $(basename "$0") [OPTION]... <NAMESPACE>"
   echo "Collect log entries from the Kubernetes namespace <NAMESPACE>"
   echo
   echo "-h                Print this help and exit."
   echo "-v                Print version and exit."
   echo "-e <script_file>  External script to be called. This option can be specified"
   echo "                  multiple times."
   echo "-s                Collect logs containing sensitive data. Without this option,"
   echo "                  CM configuration and CM Yang logs are not collected."
   echo "-t <time_period>  Collect the last <time_period> of log entries in <NAMESPACE>."
   echo "                  Works only with collection mode k8s. Time period can be given"
   echo "                  in seconds (s), minutes (m) or hours (h). Examples: 45s, 30m"
   echo "                  or 2h."
   echo "-m <mode>         Collection mode. Must be one of all, stream, container and k8s."
   echo "                  The default is k8s:"
   echo "                  * all:    Collect logs from Kubernetes pods, streamed logs from"
   echo "                    Search Engine, and from logshipper containers."
   echo "                  * stream:    Collect streamed logs from Search Engine and from"
   echo "                    logshipper containers."
   echo "                  * container: Collect streamed logs from logshipper containers"
   echo "                    only."
   echo "                  * k8s:       Collect logs from Kubernetes pods."
   echo

   exit 1
}

print_version_and_exit()
{
   # Display version
   echo "1.11.0-1"

   exit 1
}

time=0s
collectionMode="k8s"
collectionModeSpecified="false"

while getopts "e:hm:st:v" opt; do
  case $opt in
     e)
       external_scripts+=("$OPTARG")
       ;;
     h)
       print_usage_and_exit
       ;;
     m)
       collectionMode="$OPTARG"
       collectionModeSpecified="true"
       ;;
     s)
       collectSensitiveData="true"
       ;;
     t)
       time=$OPTARG
       ;;
     v)
       print_version_and_exit
       ;;
     *)
       print_usage_and_exit
       ;;
  esac
done

shift $((OPTIND-1))

if [[ $# -ne 1 ]]; then
    echo "Command line argument <NAMESPACE> is missing"
    print_usage_and_exit
fi

namespace=$1

if [[ $(echo "$namespace" | wc -l) -ne 1 ]] || ! echo "$1" | grep -qxE '[a-z0-9]([-a-z0-9]{,61}[a-z0-9])?'; then
    echo "Illegal namespace argument"
    print_usage_and_exit
fi

if ! { [[ $(echo "$time" | wc -l) -eq 1 ]] && echo "$time" | grep -qxE '[0-9]+[hms]'; }; then
    echo "Illegal time argument"
    print_usage_and_exit
fi

if [[ $collectionMode != "all" && $collectionMode != "stream" && $collectionMode != "container" && $collectionMode != "k8s" ]]; then
    echo "Collection mode must be one of all, stream, container and k8s"
    print_usage_and_exit
fi

if [[ ( $collectionMode == "stream" || $collectionMode == "container" ) && $time != "0s" ]]; then
    echo "Option -t can only be used together with option -m k8s or -m all"
    print_usage_and_exit
fi

# Validate namespace
if ! kubectl get namespace "${namespace}" > /dev/null; then
    echo "ERROR: The namespace ${namespace} does not exist. You can use \"kubectl get namespace\" command to verify your namespace"
    echo -e "${USAGE}"
    exit 1
fi

#Create a directory for placing the logs
log_base_dir=logs_"${namespace}"_$(date "+%Y-%m-%d-%H-%M-%S")
log_base_path=${PWD}/"${log_base_dir}"
mkdir "${log_base_dir}"

# Redirect errors to the file execution.log
execution_log_file="${log_base_dir}"/execution.log
exec 2> "${execution_log_file}"

# Change the shell's nice value to the maximum possible
renice -n 39 $$ >> "${execution_log_file}"

# Log a message both on the console and to the execution log file.
log_message() {
    echo "$@"
    echo "$@" >> "${execution_log_file}"
}

kubectl_cmd() {
    kubectl --namespace "${namespace}" "$@"
}

search_engine_ingest_node=$(kubectl_cmd get pods -l "app=eric-data-search-engine,role in (ingest-tls,ingest)" -o jsonpath="{.items[0].metadata.name}")

esRest_cmd() {
    kubectl --namespace "${namespace}" exec -c ingest "${search_engine_ingest_node}" -- /bin/esRest "$@"
}

#Define number of rows to gather from SE
registers=50000

# Define variables for version and operation handling
# 1. Version of the script
Version="1.11.0-1"

# 2. Number of parallel heavier describe operations
heavyOper=15

# 3. Number of parallel lighter describe operations
lightOper=20

# 4. Delay between the waves of operations
waitTime=0.5

# 5. Number of parallel jobs fetching logs from search engine
streamOper=1

if [[ $collectionModeSpecified != "false" ]]; then
    log_message "Collection mode set to \"${collectionMode}\""
else
    log_message "Collection mode not specified, using default mode \"${collectionMode}\""
fi

# Wait until number of background jobs does not exceed the parameter max_jobs
wait_for_jobs() {
    local max_jobs
    max_jobs="$1"
    while [[ $(jobs -pr | wc -l) -ge "${max_jobs}" ]]
    do
        sleep ${waitTime}
    done
}

#hash_seed=$(dd if=/dev/random bs=1 count=8 | xxd | cut -d" " -f2-5 | tr -d " ")
hash_seed=$(tr -dc 'a-f0-9' < /dev/urandom | head -c16)

#Find all [priv] tags and hash the content before saving the log
filter_priv_tags() {
    gawk -F'\n' -v hash_seed="${hash_seed}" -- '{
        line = $0
        filtered = ""
        while (match(line, /\[priv([0-9]{1,3})\]/, array) != 0) {
            privDataPos = RSTART + RLENGTH
            endTagPos = index(substr(line, privDataPos), "[/priv" array[1] "]")
            if (endTagPos != 0) {
                endTagPos = privDataPos + (endTagPos - 1)
            } else {
                endTagPos = length(line) + 1
            }

            cmd = "md5sum"
            printf "%s%s", hash_seed, substr(line, privDataPos, endTagPos - privDataPos) |& cmd
            close(cmd, "to")
            cmd |& getline encoded
            close(cmd)
            filtered = filtered substr(line, 1, privDataPos - 1) substr(encoded, 1, 32)
            line = substr(line, endTagPos)
        }
        print filtered line
    }'
}

# Removes special shell characters from stdin and print sanitized text on stdout
# Keep space as word separator
sanitize() {
    tr -Cd ' a-zA-Z0-9_-'
}

# Remember Search Engine Exit code. At the end of the script execution, we will print a message
# if logs could not be fetched from Search Engine.
search_engine_exit_code=0
get_describe_info() {
    log_message "-Getting resources describe info-"

    des_dir="${log_base_path}"/describe
    mkdir -p "${des_dir}"/OTHER
    # List of attributes in alphabetical order, but with "secrets"
    # last since fetching secrets last seems to reduce cpu load
    attrList=(
        cassandracluster configmap crd daemonsets deployments endpoints \
        geodecluster httpproxy ingresses internalCertificates jobs nodes \
        persistentvolumeclaims persistentvolumes pods rediscluster replicasets \
        rolebindings roles serviceaccounts services statefulsets \
        storageclasses secrets)

    # checking all other resources
    to_exclude="events"$(printf "|%s" "${attrList[@]}")
    others=$(kubectl_cmd api-resources --verbs=list --namespaced --no-headers --sort_by=kind | \
                grep -viE "$to_exclude" | sed 's/.*true \+//' | uniq | xargs)
    read -r -a otherList <<< "${others}"

    for attr in "${attrList[@]}" "${otherList[@]}"
    do
        wait_for_jobs "${heavyOper}"
        if [[ "${attrList[*]}" =~ ${attr} ]]; then
            get_describe_for_one_resource "$attr" "${des_dir}"
        else
            get_describe_for_one_resource "$attr" "${des_dir}/OTHER"
        fi
    done
    wait
}

get_describe_for_one_resource() {
    attr=$1
    rsrc_dir=$2

    dir=$(echo "${attr}" | tr '[:lower:]' '[:upper:]')
    mkdir -p "${rsrc_dir}/${dir}"
    kubectl_cmd get "${attr}" -o wide > "${rsrc_dir}/${dir}/${attr}.txt"
    if [ ! -s "${rsrc_dir}/${dir}/${attr}.txt" ]; then
        log_message "Could not retrieve ${dir} resources ..."
        return
    fi
    objects=$(gawk '$1 !~ /NAME/ { print $1}' "${rsrc_dir}/${dir}/${attr}.txt" | xargs)
    read -r -a objectList <<< "${objects}"
    log_message "Getting describe information on" "${#objectList[*]}" "${dir} ..."

    kubectl_cmd describe "${attr}" | gawk -v OFS='' -v folder="${rsrc_dir}/$dir/" '{
      if ($0 ~ /^Name: /) { name=$2; }
      print $0 >> folder name".yaml"
    }' &
}
get_events() {
    log_message "-Getting list of events -"
    event_dir=${log_base_path}/describe/EVENTS
    mkdir -p "${event_dir}"

    kubectl_cmd get events > "${event_dir}"/events.txt
}

get_pods_logs() {
    log_message "-Getting logs from Kubernetes pods-"

    logs_dir="${log_base_path}"/logs
    mkdir -p "${logs_dir}"/env
    kubectl_cmd get pods -o wide > "${logs_dir}"/kube_podstolog.txt
    kubectl_cmd get pods -o custom-columns="NAME:.metadata.name,CONTAINERS:.spec.containers[*].name,"`
        `"INITCONTAINERS:.spec.initContainers[*].name,RESTARTS:.status.containerStatuses[*].restartCount" \
      > "${logs_dir}"/podsWcontainers.txt
    pods_nr=$(grep -cv "^NAME" "${logs_dir}"/kube_podstolog.txt)

    gawk 'NR>1 {print $0}' "${logs_dir}"/kube_podstolog.txt | while read -r entry
    do
        pod_i=$((pod_i+1))
        # entry columns (-o wide): NAME, READY, STATUS, RESTARTS, AGE, IP, NODE, NOMINATED NODE, READINESS GATES
        pod_name=$(echo "${entry}" | gawk '{ print $1}')
        pod_status=$(echo "${entry}" | gawk '{ print $3}')
        pod_with_containers=$(grep "^$pod_name" "${logs_dir}"/podsWcontainers.txt)
        if [ "${pod_with_containers}" == "" ]; then
            log_message "Could not find containers for ${pod_name}"
            continue
        fi

        # pod_with_containers columns: NAME, CONTAINERS, INITCONTAINERS, RESTARTS
        read -r -a containers <<< "$(echo "$pod_with_containers" | gawk '{
          containers=($3 == "<none>") ? $2 : $2","$3;
          gsub(","," ",containers);
          print containers;
        }')"
        read -r -a restarts <<< "$(echo "$pod_with_containers" | gawk '{
          restarts=$4;
          gsub(","," ",restarts);
          print restarts;
        }')"

        nr_of_containers="${#containers[@]}"
        log_message "---Collecting logs for pod (${pod_i} of ${pods_nr}): ${pod_name}"
        for (( i=0; i < "${nr_of_containers}"; i++ ));
        do
            container="${containers[$i]}"
            restart="${restarts[$i]}"
            restart="${restart:0}" # set 0 as default for initContainers

            wait_for_jobs "${lightOper}"
            kubectl_cmd logs "${pod_name}" -c "${container}" --since="${time}" \
                | filter_priv_tags > "${logs_dir}"/"${pod_name}"_"${container}".txt &

            if [ "${restart}" != "0" ]; then
                kubectl_cmd logs "${pod_name}" -c "${container}" -p \
                    | filter_priv_tags > "${logs_dir}"/"${pod_name}"_"${container}"_prev.txt &
            fi
        done

        # Envvars only running pods
        if [[ "${pod_status}" == "Running" ]]; then
            kubectl_cmd exec "${pod_name}" -c "${container}" -- env \
                > "${logs_dir}"/env/"${pod_name}"_"${container}"_env.txt &
        fi
    done
    wait
}

# Read Search Engine json query result from STDIN, split log records
# into one file per pod, and output scroll_id and hits on STDOUT. Note
# that pod_name was not mandatory until DR-D1114-011, so if pod_name
# is not present in the log record, we use service_id instead.
split_logs_per_pod()
{
    gawk -F'\n' -v logs_dir="${logs_dir}" -- 'BEGINFILE {
    if (logs_dir == "") {
        exit 1
    }
    RS = "      },\n      {\n"
    delete found_pods
}
{
    log_record = $0
    if (FNR == 1) {
        scroll_id="0"
        hits="0"
        if (match(log_record, /\n[ \t]+"_scroll_id" : "([^" \t\n]+)",?\n/, array) != 0) {
            scroll_id = array[1]
        }
        if (match(log_record, /\n  "hits" : {\n    "total" : {\n      "value" : ([0-9]+),?\n/, array) != 0) {
            hits = array[1]
        }
        print scroll_id "\t" hits
        sub(/^.*\n    "hits" : \[\n      {\n/, "", log_record)
    }
    if (RT == "") {
        sub(/      }\n    ]\n  }\n}\n$/, "", log_record)
    }
    if (match(log_record, /\n[ \t]+"pod_name" : "([a-z0-9]([-a-z0-9]{,61}[a-z0-9])?)",?\n/, array) != 0) {
        pod_name = array[1]
    } else if (match(log_record, /\n[ \t]+"service_id" : "([a-zA-Z0-9]([-_a-zA-Z0-9]{,61}[a-zA-Z0-9])?)",?\n/, array) != 0) {
        pod_name = array[1]
    } else {
        next
    }
    file_name =  logs_dir "/" pod_name "/" pod_name ".txt"
    if (!(pod_name in found_pods)) {
        found_pods[pod_name] = "1"
        system("mkdir -p \"" logs_dir "\"/\"" pod_name "\"")
    }
    printf "      {\n%s      },\n", log_record >> file_name
}
ENDFILE {
    for (pod_name in found_pods) {
        close(file_name)
    }
}'
}

stream_logs_from_pod()
{
    local j
    local pod_name
    local pod_status
    pod_name="$1"
    mkdir "${logs_dir}"/"${pod_name}"/
    for j in $(kubectl_cmd get pod "${pod_name}" -o jsonpath='{.spec.containers[*].name}')
    do
        pod_status=$(kubectl_cmd get pod "${pod_name}" -o jsonpath='{.status.phase}')
        if [[ "${j}" == "logshipper" ]]; then
            kubectl_cmd exec "${pod_name}" -c "${j}" -- tar cf - /logs | tar xf - -C /"${logs_dir}"/"${pod_name}"/
            #Filter and hash priv tags
            grep -lr "\[priv" /"${logs_dir}"/"${pod_name}"/ | while read -r file_itr
            do
               log_message "podstream, file name containing priv tag: ${file_itr}"
                mv "${file_itr}" "${file_itr}".tmp
                filter_priv_tags < "${file_itr}".tmp > "${file_itr}"
                rm "${file_itr}".tmp
            done
        fi
        # Only exec Pod in Running state
        if [[ "${pod_status}" == "Running" ]]; then
            kubectl_cmd exec "${pod_name}" -c "${j}" -- env > "${logs_dir}"/env/"${pod_name}"_"${j}"_env.txt &
        fi
    done
}

stream_logs_from_service() {
    local counter
    local hits_count
    local j
    local k
    local scroll_identifier
    local service_id
    local sp
    local file_no
    local result
    service_id="$1"
    result=$(esRest_cmd POST "/_search?scroll=1m&sort=@timestamp:desc&&pretty" -H 'Content-Type: application/json' -d '{
    "size" : 10000,
    "query" : {
        "term" : {
            "service_id.keyword" : "'"${service_id}"'"
        }
    }
}' | filter_priv_tags | split_logs_per_pod)
    # elaborating the first batch
    scroll_identifier=$(echo "${result}" | cut -f1)
    hits_count=$(echo "${result}" | cut -f2)

    log_message "hits-count:${hits_count} for ${service_id}"
    if [[ ${hits_count} -le 0 ]]; then
        log_message "No data in search engine for ${service_id}"
    fi
    k=0
    counter=10000
    sp="/-\|"
    echo -n ' '
    if (( "${hits_count}" > 30000 )); then
        hits_count=${registers}
    fi

    file_no=1
    while  (( "${hits_count}" > "${counter}" ))
    do
        printf "\b%s" "${sp:k++%${#sp}:1}"
        scroll_identifier=$(esRest_cmd POST "/_search/scroll?pretty" -H 'Content-Type: application/json' -d '{
    "scroll" : "1m",
    "scroll_id" : "'"${scroll_identifier}"'"
}' | filter_priv_tags | split_logs_per_pod | cut -f1)

        (( counter = counter + 10000 ))
        (( file_no = file_no + 1 ))
        log_message ${counter}
    done
    # delete scroll
    esRest_cmd DELETE "/_search/scroll" -H 'Content-Type: application/json' -d '{
    "scroll_id" : "'"${scroll_identifier}"'"
}' >> "${execution_log_file}"
}

stream_logs() {
    local i
    local pods_to_stream
    local service_id
    log_message "-Getting streamed logs from pod containers-"

    pods_to_stream="$1"
    logs_dir="${log_base_path}"/logs
    mkdir -p "${logs_dir}"/env
    kubectl_cmd get pods -o wide > "${logs_dir}"/kube_podstolog.txt
    for i in $(kubectl_cmd get pods | grep -v NAME | gawk '{print $1}')
    do
        wait_for_jobs "${heavyOper}"
        stream_logs_from_pod "${i}" &
    done
    wait

    log_message "-Getting streamed logs from Search Engine-"

    esRest_cmd GET "/_search?pretty&&filter_path=aggregations" -H 'Content-Type: application/json' -d '{
    "aggs": {
        "services": {
            "terms": {
                "field": "service_id.keyword",
                "size": 200
            }
        }
    }
}' > "${logs_dir}"/kube_servicestolog.txt
    search_engine_exit_code=$?
    if [[ $search_engine_exit_code -ne 0 ]]; then
        log_message "Error: Search Engine is down. Cannot fetch streamed logs from it."
        return
    fi

    if [[ "${pods_to_stream}" = "cont" ]]; then
        for service_id in eric-ctrl-bro eric-sec-certm; do
            wait_for_jobs "${streamOper}"
            stream_logs_from_service "${service_id}" &
        done
        wait
    else
        grep -Ehx '[ \t]+"key" : "[a-zA-Z0-9]([-_a-zA-Z0-9]{,61}[a-zA-Z0-9])?",?' "${logs_dir}"/kube_servicestolog.txt | cut -d\" -f4 | while read -r service_id; do
            wait_for_jobs "${streamOper}"
            stream_logs_from_service "${service_id}" &
        done
        wait
    fi
}
get_helm_info() {
    log_message "-Getting Helm Charts for the deployments-"

    helm_dir="${log_base_path}"/helm
    mkdir "${helm_dir}"
    helm --namespace "${namespace}" list -a > "${helm_dir}"/helm_deployments.txt

    #Check if there is helm2  or helm3 deployment

    echo "Data Collector ${Version}" > "${log_base_path}"/script_version.txt
    helm version | head -1 > "${log_base_path}"/helm_version.txt
    if eval ' grep v3 ${log_base_path}/helm_version.txt'
    then
        log_message "HELM 3 identified"
        HELM='helm get all --namespace='"${namespace}"
    else
        HELM='helm get --namespace='"${namespace}"
        log_message "${HELM}"
    fi

    for i in $(helm --namespace "${namespace}" list -a | grep -v NAME | gawk '{print $1}')
    do
        ${HELM} "${i}" > "${helm_dir}"/"${i}".txt
        log_message "${HELM}" "${i}"
    done
}

cmm_log() {

    log_message "-Verifying for CM logs -"
    cmm_log_dir="${log_base_path}"/logs/cmm_log
    if (kubectl_cmd get pods | grep -i cm-med | grep Running)
    then
        mkdir -p "${cmm_log_dir}"
        log_message "CM Pods found running, gathering cmm_logs.."
        for i in $(kubectl_cmd get pods | grep -i cm-med | gawk '{print $1}')
        do
            log_message "${i}"
            kubectl_cmd exec "${i}" -- collect_logs > "${cmm_log_dir}"/cmmlog_"${i}".tgz
        done
        #Checking for schemas and configurations
        POD_NAME=$(kubectl_cmd get pods | grep cm-mediator | grep -vi notifier | head -1 | gawk '{print $1}')
        # handle no HTTPS support in cmm
        CURL_BASE_URL="http://eric-cm-mediator:5003"
        CURL_OPTS=()
            
        if kubectl_cmd exec ${POD_NAME} -- ls /etc/sip-tls-ca/cacertbundle.pem
        then
            log_message "CM logs, using /etc/sip-tls-ca/cacertbundle.pem"
            CURL_BASE_URL="https://localhost:5004"
            CURL_OPTS=("--cacert" "/etc/sip-tls-ca/cacertbundle.pem" "--cert" "/etc/sip-tls-client/clicert.pem" "--key" "/etc/sip-tls-client/cliprivkey.pem")
        fi
        
        if kubectl_cmd exec ${POD_NAME} -- ls /run/secrets/eric-sec-sip-tls-trusted-root-cert/ca.crt
        then
            log_message "CM logs, using /run/secrets/eric-sec-sip-tls-trusted-root-cert/ca.crt"
            CURL_BASE_URL="https://localhost:5004"
            CURL_OPTS=("--cacert" "/run/secrets/eric-sec-sip-tls-trusted-root-cert/ca.crt" "--cert" "/run/secrets/eric-cm-mediator-tls-client-secret/clicert.pem" "--key" "/run/secrets/eric-cm-mediator-tls-client-secret/cliprivkey.pem")
        fi

        kubectl_cmd exec "${POD_NAME}" -- curl -X GET "${CURL_OPTS[@]}" "${CURL_BASE_URL}/cm/api/v1/schemas" > "${cmm_log_dir}"/schemas.json
        #Due to security considerations, CMM configuration data is by default not allowed to be collected since it contain sensitive data
        if [[ "$collectSensitiveData" ]] ;
        then
            log_message "CM logs, that might contain sensitive data, will be collected"
            kubectl_cmd exec "${POD_NAME}" -- curl -X GET "${CURL_OPTS[@]}" "${CURL_BASE_URL}/cm/api/v1/configurations" > "${cmm_log_dir}"/configurations.json  # > was missing
            #configurations_list=$(grep -h \"name\" "${cmm_log_dir}"/configurations.json | cut -d : -f 2 | tr -d \",)
            for i in `sed -E 's/\},\s*\{/\},\n\{/g' ${cmm_log_dir}/configurations.json | awk -F \" '{print $4}'`
            do
                log_message "Collecting configuration of ${i}"
                kubectl_cmd exec "${POD_NAME}" -- curl -X GET "${CURL_OPTS[@]}" "${CURL_BASE_URL}/cm/api/v1/configurations/${i}" > "${cmm_log_dir}"/config_"${i}".json
            done
        fi
    else
        log_message "CM Containers not found or not running, doing nothing"
    fi
}

siptls_logs() {
    local siptls_log_dir
    local siptls_pods_file
    local i
    local pod_name
    local kmsspbr

    log_message "-Verifying for SIP-TLS logs -"

    mkdir -p "${log_base_path}/logs"
    siptls_log_dir="${log_base_path}"/logs/sip_kms_dced
    siptls_pods_file="${log_base_path}"/logs/sip_tls_pods.txt

    if kubectl_cmd get pods | grep -i sip-tls > "${siptls_pods_file}"
    then
        cat "${siptls_pods_file}"
        cat "${siptls_pods_file}" >> "${execution_log_file}"
        mkdir -p "${siptls_log_dir}"
        log_message "SIP-TLS Pods found, gathering siptls_logs.."
        gawk '{print $1}' < "${siptls_pods_file}" | while read -r i
        do
            log_message "${i}"
            kubectl_cmd exec "${i}" -c sip-tls -- /bin/bash /sip-tls/sip-tls-alive.sh && echo $? > "${siptls_log_dir}"/alive_log_"${i}".out
            kubectl_cmd exec "${i}" -c sip-tls -- /bin/bash /sip-tls/sip-tls-ready.sh && echo $? > "${siptls_log_dir}"/ready_log_"${i}".out
            kubectl_cmd logs "${i}" sip-tls | filter_priv_tags > "${siptls_log_dir}"/sip-tls_log_"${i}".out
            kubectl_cmd logs "${i}" sip-tls --previous | filter_priv_tags > "${siptls_log_dir}"/sip-tls-previous_log_"${i}".out
            kubectl_cmd exec "${i}" -c sip-tls -- env > "${siptls_log_dir}"/env_log_"${i}".out
        done

        kubectl_cmd exec eric-sec-key-management-main-0 -c kms -- bash  -c 'vault status -tls-skip-verify' > "${siptls_log_dir}"/vault_status_kms.out
        kubectl_cmd exec eric-sec-key-management-main-0 -c shelter -- bash -c  'vault status -tls-skip-verify' > "${siptls_log_dir}"/vault_status_shelter.out
        kubectl_cmd get crd servercertificates.com.ericsson.sec.tls -o yaml  > "${siptls_log_dir}"/servercertificates_crd.yaml
        kubectl_cmd get servercertificates -o yaml  > "${siptls_log_dir}"/servercertificates.yaml
        kubectl_cmd get crd clientcertificates.com.ericsson.sec.tls -o yaml  > "${siptls_log_dir}"/clientcertificates_crd.yaml
        kubectl_cmd get clientcertificates -o yaml  > "${siptls_log_dir}"/clientcertificates.out
        kubectl_cmd get crd certificateauthorities.com.ericsson.sec.tls -o yaml  > "${siptls_log_dir}"/certificateauthorities_crd.yaml
        kubectl_cmd get certificateauthorities -o yaml  > "${siptls_log_dir}"/certificateauthorities.out
        kubectl_cmd get internalcertificates.siptls.sec.ericsson.com  -o yaml  > "${siptls_log_dir}"/internalcertificates.yaml
        kubectl_cmd get internalusercas.siptls.sec.ericsson.com  -o yaml  > "${siptls_log_dir}"/internalusercas.yaml
        kubectl_cmd get secret -l com.ericsson.sec.tls/created-by=eric-sec-sip-tls > "${siptls_log_dir}"/secrets_created_by_eric_sip.out
        pod_name=$(kubectl_cmd get po -l app=eric-sec-key-management -o jsonpath="{.items[0].metadata.name}")
        kubectl_cmd exec "${pod_name}" -c kms -- env VAULT_SKIP_VERIFY=true vault status > "${siptls_log_dir}"/kms_status_.out

        if kubectl_cmd get pods | grep -i eric-sec-key-management-main-1
        then
            log_message "Gathering information to check split brain on KMS"

            kmsspbr=${siptls_log_dir}/KMS_splitbrain_check
            mkdir "${kmsspbr}"
            {
                kubectl_cmd exec eric-sec-key-management-main-0 -c kms -- bash -c "date;export VAULT_ADDR=http://localhost:8202;echo 'KMS-0';vault status -tls-skip-verify|grep 'HA Enabled' -A3"
                kubectl_cmd exec eric-sec-key-management-main-1 -c kms -- bash -c "export VAULT_ADDR=http://localhost:8202;echo 'KMS-1';vault status -tls-skip-verify|grep 'HA Enabled' -A3"
                kubectl_cmd exec eric-sec-key-management-main-0 -c shelter -- bash -c "export VAULT_ADDR=http://localhost:8212;echo 'SHELTER-0';vault status -tls-skip-verify|grep 'HA Enabled' -A3"
                kubectl_cmd exec eric-sec-key-management-main-1 -c shelter -- bash -c "export VAULT_ADDR=http://localhost:8212;echo 'SHELTER-1';vault status -tls-skip-verify|grep 'HA Enabled' -A3"
            } > "${kmsspbr}"/vault_Stat_HA.log

            {
                kubectl_cmd logs eric-sec-key-management-main-0 -c kms | grep -e "active operation" -e "standby mode" | filter_priv_tags
                kubectl_cmd logs eric-sec-key-management-main-1 -c kms | grep -e "active operation" -e "standby mode" | filter_priv_tags
                kubectl_cmd logs eric-sec-key-management-main-0 -c shelter | grep -e "active operation" -e "standby mode" | filter_priv_tags
                kubectl_cmd logs eric-sec-key-management-main-1 -c shelter | grep -e "active operation" -e "standby mode" | filter_priv_tags
            } > "${kmsspbr}"/active_operation.log
        else
            log_message "Key Management Pod not found"
        fi
    else
        log_message "SIP-TLS Containers not found or not running, doing nothing"
    fi
    rm "${siptls_pods_file}"
}

cmy_log() {
    log_message "-Verifying for CM Yang logs -"

    cmy_log_dir="${log_base_path}"/logs/cmy_log

    #Due to security considerations, CM Yang logs are by default not allowed to be collected since they may contain sensitive data
    if [[ "$collectSensitiveData" ]] ;
    then
        log_message "CM Yang logs, that might contain sensitive data, will be collected"
        if kubectl_cmd get pods | grep -iq 'yang.*Running'
        then
            mkdir -p "${cmy_log_dir}"
            log_message "CM Yang Pods found running, gathering cmyang_logs.."
            for i in $(kubectl_cmd get pods | grep -i yang | gawk '{print $1}')
            do
                log_message "${i}"
                mkdir "${cmy_log_dir}"/sssd_"${i}"/
                kubectl_cmd cp "${i}":/var/log/sssd "${cmy_log_dir}"/sssd_"${i}"/ -c sshd
            done
        else
            log_message "CM Yang Containers not found or not running, doing nothing"
        fi
    fi

    cmyp_yang_schemas "${cmy_log_dir}"
    cmyp_json_schemas "${cmy_log_dir}"
}

cmyp_json_schemas() {
    log_message "-Collect JSON schemas and corresponding actions, validators and datasources-"

    cmy_log_dir=$1

    CM_GEN=$(grep -h CM_GENERATION "${log_base_path}"/describe/PODS/*cm-yang* | head -1 | gawk '{print $2}' | sanitize)
    log_message "Using CM Yang generation: " $CM_GEN
    if [[ ${CM_GEN} != genA ]]; then
      DBNAME=$(grep -h STORAGE_DBNAME: "${log_base_path}"/describe/PODS/*cm-backend* | head -1 | gawk '{print $2}' | sanitize)
      ddb_service=$(grep -h STORAGE_HOST: "${log_base_path}"/describe/PODS/*cm-backend* | head -1 | gawk '{print $2}' | sanitize)
    else
      DBNAME=$(grep -h POSTGRES_DBNAME: "${log_base_path}"/describe/PODS/*cm-yang* | head -1 | gawk '{print $2}' | sanitize)
      ddb_service=$(grep -h POSTGRES_SERVICE_HOST: "${log_base_path}"/describe/PODS/*cm-yang* | head -1 | gawk '{print $2}' | sanitize)
    fi
    app=$(kubectl_cmd describe svc "${ddb_service}" | grep app= | cut -d = -f2)
    ddb=$(kubectl_cmd get pod -l app="${app}" | grep Running | head -n 1 | awk '{print $1}' | sanitize)
    ddb_container=$(kubectl_cmd get pods "${ddb}" -o=jsonpath='{.spec.containers[0].name}' | sanitize)
    if [[ $ddb == "" ]]; then
      log_message "WARNING: No backend DB Running for CMYP"
      return
    fi

    JSON_PATH=$(mktemp -d -u "/tmp/jsonSchemas.XXXXXX")
    LOCAL_PATH=${cmy_log_dir}/schemas_${ddb}/
    mkdir -p "${LOCAL_PATH}"

    ddb_cmd "if [ -d \"${JSON_PATH}\" ]; then rm -rf \"${JSON_PATH}\"; fi; mkdir \"${JSON_PATH}\""
    case ${CM_GEN} in
      genC)
        jsonNames=$(ddb_cmd "echo \"SELECT name FROM models\" | /usr/bin/psql --quiet --tuples-only -d \"${DBNAME}\" -U postgres" | sanitize)
        # Collect Transformation rule, Action provider registration, State provider registration and Event Receiver registrations in genC
        TRANS_RULES_PATH=${JSON_PATH}/transrules_action_statedata
        ddb_cmd "mkdir -p \"${TRANS_RULES_PATH}\""
        sql="SELECT attributes FROM no_backup_objects WHERE name='eric-data-transformer-json'"
        file=${TRANS_RULES_PATH}/rules.json
        fetch_from_ddb "${sql}" "${DBNAME}" "${file}"
        ;;
      genB)
        jsonNames=$(ddb_cmd "echo \"SELECT name FROM models\" | /usr/bin/psql --quiet --tuples-only -d \"${DBNAME}\" -U postgres" | sanitize)
        ;;
      genA)
        jsonNames=$(ddb_cmd "echo \"SELECT name FROM schemas\" | /usr/bin/psql --quiet --tuples-only -d \"${DBNAME}\" -U postgres" | sanitize)
        ;;
      *)
        log_message "Invalid cm generation ${CM_GEN}"
        ;;
    esac
    for n in ${jsonNames}
    do
      log_message "fetch ${n}"
      jsonFile=${JSON_PATH}/${n}.json
      if [[ ${CM_GEN} != genA ]]; then
          sql="SELECT convert_from(model, 'UTF8') FROM models WHERE name='${n}'"
          fetch_from_ddb "${sql}" "${DBNAME}" "${jsonFile}"
      else
          sql="SELECT data->'schema' FROM schemas WHERE name='${n}'"
          fetch_from_ddb "${sql}" "${DBNAME}" "${jsonFile}"

          if [[ "${n}" != "__cmm" ]];
          then
              sql="SELECT data->'actions' FROM schemas WHERE name='${n}'"
              file=${JSON_PATH}/${n}_actions.json
              fetch_from_ddb "${sql}" "${DBNAME}" "${file}"

              sql="SELECT validators FROM schemas WHERE name='${n}'"
              file=${JSON_PATH}/${n}_validators.json
              fetch_from_ddb "${sql}" "${DBNAME}" "${file}"

              sql="SELECT datasources FROM schemas WHERE name='${n}'"
              file=${JSON_PATH}/${n}_datasources.json
              fetch_from_ddb "${sql}" "${DBNAME}" "${file}"
        fi
      fi
      log_message "fetch ${n} done"
    done

    ddb_cmd "cd \"${JSON_PATH}\" && tar -czf jsonSchemas.tar.gz *"
    kubectl_cmd cp "${ddb}":"${JSON_PATH}"/jsonSchemas.tar.gz "${LOCAL_PATH}"/jsonSchemas.tar.gz -c "${ddb_container}"

    tar xzvf "${LOCAL_PATH}"/jsonSchemas.tar.gz -C "${LOCAL_PATH}"/
    rm -f "${LOCAL_PATH}"/jsonSchemas.tar.gz
    ddb_cmd "rm -rf \"${JSON_PATH}\""

    if ! type jq >& /dev/null; then
        echo -e "\e[33m"
        log_message "Warning: command jq missed. Json Schemas in GenB/GenC are needed to split manually."
        echo -e "\e[0m"
        return
    fi
    if [[ ${CM_GEN} != genA ]]; then
        for n in ${jsonNames}
        do
            jsonFile=${LOCAL_PATH}/${n}.json
            jq ".schemas.\"${n}\"" "${jsonFile}" > "${jsonFile}".1
            mv "${jsonFile}".1 "${jsonFile}"
        done
    fi
}

cmyp_yang_schemas() {
    log_message "-Collect YANG schemas-"

    cmy_log_dir=$1

    CM_GEN=$(grep -h CM_GENERATION "${log_base_path}"/describe/PODS/*cm-yang* | head -1 | gawk '{print $2}' | sanitize)
    log_message "Using CM Yang generation: " $CM_GEN

    if [[ ${CM_GEN} == genB ]]; then
      # using CMB API v1/config/eric-cm-yang-provider to fetch yang models
      cmm_pod=$(kubectl_cmd get pods | grep cm-mediator | grep -vi notifier | head -1 | gawk '{print $1}')
      cmb_service=$(grep -h BACKEND_REST_BACKEND_HOST: "${log_base_path}"/describe/PODS/*cm-yang* | head -1 | gawk '{print $2}' | sanitize)
      cmb_port=$(grep -h BACKEND_REST_BACKEND_PORT: "${log_base_path}"/describe/PODS/*cm-yang* | head -1 | gawk '{print $2}' | sanitize)
      curl_base_url=https://${cmb_service}:${cmb_port}/v1/config/eric-cm-yang-provider
      cacertbundle=/run/secrets/eric-sec-sip-tls-trusted-root-cert/cacertbundle.pem
      clicert=/run/secrets/eric-cm-mediator-cm-backend-tls-client-secret/clicert.pem
      cliprivkey=/run/secrets/eric-cm-mediator-cm-backend-tls-client-secret/cliprivkey.pem
      curl_opts=("--cacert" "${cacertbundle}" "--cert" "${clicert}" "--key" "${cliprivkey}")
      if ! kubectl_cmd exec "${cmm_pod}" -- ls ${cacertbundle}
      then
        # TODO support non tls
        curl_opts=()
      fi
      ddb_service=$(grep -h STORAGE_HOST: "${log_base_path}"/describe/PODS/*cm-backend* | head -1 | gawk '{print $2}' | sanitize)
    elif [[ ${CM_GEN} == genC ]]; then
      DBNAME=$(grep -h POSTGRES_BACKEND: "${log_base_path}"/describe/PODS/*cm-yang* | head -1 | gawk '{print $2}')
      DBNAME=${DBNAME#*=}
      ddb_service=$(grep -h POSTGRES_BACKEND: "${log_base_path}"/describe/PODS/*cm-yang* | head -1 | gawk '{print $4}')
      ddb_service=${ddb_service#*=}
    else
      DBNAME=$(grep -h POSTGRES_DBNAME: "${log_base_path}"/describe/PODS/*cm-yang* | head -1 | gawk '{print $2}' | sanitize)
      ddb_service=$(grep -h POSTGRES_SERVICE_HOST: "${log_base_path}"/describe/PODS/*cm-yang* | head -1 | gawk '{print $2}' | sanitize)
    fi
    app=$(kubectl_cmd describe svc "$ddb_service" | grep app= | cut -d = -f2)
    ddb=$(kubectl_cmd get pod -l app="$app" | grep Running | head -n 1 | awk '{print $1}' | sanitize)
    ddb_container=$(kubectl_cmd get pods "${ddb}" -o=jsonpath='{.spec.containers[0].name}' | sanitize)
    if [[ $ddb == "" ]]; then
      log_message "WARNING: No backend DB Running for CMYP"
      return
    fi

    YANG_PATH=$(mktemp -d -u "/tmp/yangSchemas.XXXXXX")
    LOCAL_PATH=${cmy_log_dir}/schemas_${ddb}/
    mkdir -p "${LOCAL_PATH}"

    if [[ ${CM_GEN} == genB ]]; then
      log_message "To collect yang archives for ${CM_GEN}"
      LOCAL_PATH_GENB="${cmy_log_dir}"/genB_schemas/
      mkdir -p "${LOCAL_PATH_GENB}"
      kubectl_cmd exec "${cmm_pod}" -- curl -X GET "${curl_opts[@]}" "${curl_base_url}" | json_pp > "${LOCAL_PATH_GENB}"/encoded_yang_archives

      if ! type jq >& /dev/null; then
          echo -e "\e[33m"
          log_message "Warning: command jq missed. Yang Schemas in GenB are needed to be decoded manually."
          log_message "Decode each module using 'base64 -d' for modules in Json file encoded_yang_archives"
          echo -e "\e[0m"
          return
      fi

      if ! type base64 >& /dev/null; then
          echo -e "\e[33m"
          log_message "Warning: command base64 missed. Yang Schemas in GenB are needed to be decoded manually."
          log_message "Decode each module using 'base64 -d' for modules in Json file encoded_yang_archives"
          echo -e "\e[0m"
          return
      fi
      
      # decode the base64 format of yang archives
      jq -c '.modules[]' "${LOCAL_PATH_GENB}"/encoded_yang_archives | while IFS= read -r item; do
        fn=$(echo "${item}" | jq -r '.name')
        echo "${item}" | jq -r '.module' | base64 -d  > "${LOCAL_PATH_GENB}"/"${fn}".tgz
      done
      return
    elif [[ ${CM_GEN} == genC ]]; then
      yangNames=$(ddb_cmd "echo \"SELECT name FROM cmyp.yangarchives\" | /usr/bin/psql --quiet --tuples-only -d \"${DBNAME}\" -U postgres" | sanitize)
      dync_val_names=$(ddb_cmd "echo \"SELECT name FROM cmyp.dynamic_validations\" | /usr/bin/psql --quiet --tuples-only -d ${DBNAME} -U postgres" | sanitize)
      tx_aug_names=$(ddb_cmd "echo \"SELECT name FROM cmyp.augmentations\" | /usr/bin/psql --quiet --tuples-only -d ${DBNAME} -U postgres" | sanitize)
    else
      yangNames=$(ddb_cmd "echo \"SELECT name FROM yangschemas\" | /usr/bin/psql --quiet --tuples-only -d \"${DBNAME}\" -U postgres" | sanitize)
    fi

    ddb_cmd "if [ -d \"${YANG_PATH}\" ]; then rm -rf \"${YANG_PATH}\"; fi; mkdir \"${YANG_PATH}\""

    for n in ${yangNames}
    do
      if [[ ${CM_GEN} == genC ]]; then
        sql="SELECT data FROM cmyp.yangarchives WHERE name='${n}'"
      else
        sql="SELECT data FROM yangschemas WHERE name='${n}'"
      fi
      file=${YANG_PATH}/${n}
      fetch_from_ddb "${sql}" "${DBNAME}" "${file}"
      log_message "fetch ${n} done"
    done

    if [[ ${CM_GEN} == genC ]]; then
        DYNC_VAL_PATH=${YANG_PATH}/dynamic_validation_registration
        ddb_cmd "mkdir -p \"${DYNC_VAL_PATH}\""
        for n in ${dync_val_names}
        do
            sql="SELECT registration_blob FROM cmyp.dynamic_validations WHERE name='${n}'"
            file=${DYNC_VAL_PATH}/${n}.json
            fetch_from_ddb "${sql}" "${DBNAME}" "${file}"
            log_message "fetch dynamic validdation registration ${n} done"
        done

        TX_AUG_PATH=${YANG_PATH}/tx_augmentation_registration
        ddb_cmd "mkdir -p \"${TX_AUG_PATH}\""
        for n in ${tx_aug_names}
        do
            sql="SELECT registration_blob FROM cmyp.augmentations WHERE name='${n}'"
            file=${TX_AUG_PATH}/${n}.json
            fetch_from_ddb "${sql}" "${DBNAME}" "${file}"
            log_message "fetch tx augmentation registration ${n} done"
        done
    fi

    ddb_cmd "cd \"${YANG_PATH}\" && tar -czf yangSchemas.tar.gz *"
    kubectl_cmd cp "${ddb}":"${YANG_PATH}"/yangSchemas.tar.gz "${LOCAL_PATH}"/yangSchemas.tar.gz -c "${ddb_container}"

    tar xzvf "${LOCAL_PATH}"/yangSchemas.tar.gz -C "${LOCAL_PATH}"/
    rm -f "${LOCAL_PATH}"/yangSchemas.tar.gz
    
    if  [ -x "$(command -v xxd)" ]; then  # if xxd is not available, the transformation has to be done in the analysis phase "xxd -r -p < <yang_schema_file>"
        for f in "${LOCAL_PATH}"/*
        do
            [[ -e "$f" ]] || break  # handle the case of no files
            if [[ $f != *"dynamic_validation_registration" && $f != *"tx_augmentation_registration" ]]; then
                xxd -r -p < "${f}" > "${f}".tar.gz
                rm -f "${f}"
            fi
        done
    else 
        echo -e "\e[33m"
        log_message "Warning: command xxd missed. Yang Schemas are needed to split manually."
        echo -e "\e[0m"
        return
    fi

    ddb_cmd "rm -rf \"${YANG_PATH}\""
}

ddb_cmd() {
    kubectl_cmd exec "${ddb}" -c "${ddb_container}" -- /bin/bash -c "$@"
}

fetch_from_ddb() {
    if [ "$#" -ne 3 ]; then
        echo "Usage: fetch_from_ddb sql_cmd db_name stored_file"
        return 1
    fi

    local sql_cmd=$1
    local db_name=$2
    local file=$3

    fetch="echo \"${sql_cmd}\" | /usr/bin/psql --quiet --tuples-only -d \"${db_name}\" -U postgres > \"${file}\""
    ddb_cmd "${fetch}"
}

function diameter_log (){
    if (kubectl_cmd get pods | grep -i stm-diameter|grep Running)
    then
        DIA_POD=$(kubectl_cmd get pod -l app=eric-stm-diameter -o name)
        pod_status=$(kubectl_cmd get "${DIA_POD}" -o jsonpath='{.status.phase}')
        diacc=${log_base_path}/logs/dia/
        mkdir -p "${diacc}"/pod
        for i in $(kubectl_cmd get pod -l app=eric-stm-diameter -o name)
        do
            kubectl_cmd exec "${i}" -- curl -s http://localhost:20100/dumpState > "${diacc}"/"${i}"_dumpState.txt
            kubectl_cmd exec "${i}" -- curl -s http://localhost:20100/troubleshoot/transportDump/v2 > "${diacc}"/"${i}"_transport.txt
            kubectl_cmd exec "${i}" -- curl -s http://localhost:20100/dumpConfig > "${diacc}"/"${i}"_dumpConfig.txt
        done
    fi
}

basic_checks () {
    mkdir -p "${log_base_path}"/logs/err "${log_base_path}"/logs/SE
    for i in "${log_base_path}"/logs/*.txt
    do
        [[ -e "$i" ]] || break  # handle the case of no files
        filename=$(basename "${i}" .txt)
        if ! [[ -d ${i} ]]; then
            grep -hiE "err|warn|crit" "${i}" > "${log_base_path}"/logs/err/"${filename}".err.txt
            grep -hiE "failed to perform indices:data/write/bulk|latency|failed to send out heartbeat on time|disk|time out|timeout|timed out" "${i}" > "${log_base_path}"/logs/err/"${filename}".latency.txt
        fi
    done
    for i in "${log_base_path}"/describe/PODS/*
    do
        [[ -e "$i" ]] || break  # handle the case of no files
        version=$(grep -h "app.kubernetes.io/version" "${i}")
        filename=$(basename "${i}")
        echo "${filename}" "${version}" >>"${log_base_path}"/describe/PODS/pods_image_versions.txt
    done
    kubectl_cmd top pods > "${log_base_path}"/logs/top_pod_output.txt
    kubectl_cmd top node > "${log_base_path}"/logs/top_node_output.txt

    pod_status=$(kubectl_cmd get pods | grep -c search-engine)
    if [[ ${pod_status} -gt 0 ]]; then
        esRest_cmd GET /_cat/nodes?v>"${log_base_path}"/logs/SE/nodes.txt
        esRest_cmd GET /_cat/indices?v>"${log_base_path}"/logs/SE/indices.txt
        esRest_cmd GET /_cluster/health?pretty > "${log_base_path}"/logs/SE/health.txt
        esRest_cmd GET /_cluster/allocation/explain?pretty > "${log_base_path}"/logs/SE/allocation.txt
    fi
    mkdir -p "${log_base_path}"/logs/sip_kms_dced/DCED
    for i in $(kubectl_cmd get pod | grep data-distributed-coordinator-ed | grep -v agent | gawk '{print $1}')
    do
        log_message "${i}"
        kubectl_cmd exec "${i}" -c dced -- etcdctl member list -w fields >  "${log_base_path}"/logs/sip_kms_dced/DCED/memberlist_"${i}".txt
        kubectl_cmd exec "${i}" -c dced -- bash -c 'ls /data/member/snap -lh' >  "${log_base_path}"/logs/sip_kms_dced/DCED/sizedb_"${i}".txt
        kubectl_cmd exec "${i}" -c dced -- bash -c 'du -sh data/*;du -sh data/member/*;du -sh data/member/snap/db' >>  "${log_base_path}"/logs/sip_kms_dced/DCED/sizedb_"${i}".txt
        kubectl_cmd exec "${i}" -c dced -- bash -c 'unset ETCDCTL_ENDPOINTS; etcdctl endpoint status --endpoints=:2379 --insecure-skip-tls-verify=true -w fields'> "${log_base_path}"/logs/sip_kms_dced/DCED/endpoints_"${i}".txt
        kubectl_cmd exec "${i}" -c dced -- etcdctl user list > "${log_base_path}"/logs/sip_kms_dced/DCED/user_list"${i}".txt
    done
    if (kubectl_cmd get pods | grep -i kvdb-ag)
    then
        for i in $(cd "${log_base_path}"/describe/PODS/ || exit; grep -- eric-data-kvdb-ag * | grep Image: | gawk -F: '{print $1}' | sort -u | gawk -F\. '{print $1}')
        do
            mkdir -p "${log_base_path}"/logs/KVDBAG/"${i}"/logs "${log_base_path}"/logs/KVDBAG/"${i}"/stats
            for j in $(kubectl_cmd exec "${i}" -- ls -ltr /opt/dbservice/data/stats/ | tail -3 | grep -vi marker | gawk '{print $9}')
            do
                wait_for_jobs "${heavyOper}"
                kubectl_cmd cp "${i}":/opt/dbservice/data/stats/"${j}" "${log_base_path}"/logs/KVDBAG/"${i}"/stats/"${j}" &
            done
        done
        wait
    fi
}

get_ss7_cnf() {

    if kubectl_cmd get pods | grep -i ss7
    then
        # shellcheck disable=SC2016
        for i in $(kubectl_cmd get pod -o go-template='{{- range .items -}} {{- $name := .metadata.name -}} {{- range .spec.containers -}} {{- if eq .name "ss7" -}} {{ printf "%s" $name }} {{ end -}} {{- end -}} {{- end -}}')
        do
            cnfpath="${log_base_path}"/logs/ss7_cnf_"${i}"
            mkdir -p "${cnfpath}"
            kubectl_cmd cp "${i}":/opt/cnf-dir/ -c ss7 "${cnfpath}"
        done
    fi
}

sm_log() {

    log_message "-Verifying for SM logs -"

    sm_log_dir=${log_base_path}/logs/sm_log
    serviceMeshCustomResources=("adapters.config" "attributemanifests.config" "authorizationpolicies.security" "destinationrules.networking" "envoyfilters.networking" "gateways.networking" "handlers.config" "httpapispecbindings.config" "httpapispecs.config" "instances.config" "peerauthentications.security" "proxyconfigs.networking" "quotaspecbindings.config" "quotaspecs.config" "rbacconfigs.rbac" "requestauthentications.security" "rules.config" "serviceentries.networking" "servicerolebindings.rbac" "serviceroles.rbac" "sidecars.networking" "telemetries.telemetry" "templates.config" "virtualservices.networking" "wasmplugins.extensions" "workloadentries.networking" "workloadgroups.networking")
    istioDebugURL=("adsz" "syncz" "registryz" "endpointz" "instancesz" "endpointShardz" "configz" "cachez" "resourcesz" "authorizationz" "push_status" "inject" "mesh" "networkz")
    proxyDebugURL=("certs" "clusters" "config_dump?include_eds" "listeners" "memory" "server_info" "stats/prometheus" "runtime")
    if kubectl_cmd get pods --selector app=istiod | grep eric-mesh-controller | grep Running
    then
        mkdir -p "${sm_log_dir}"/istio
        log_message "SM Controller pods found running, gathering sm_log for controller pods..."
        for pod_name in $(kubectl_cmd get pods --selector app=istiod --no-headers | gawk -F " " '{print $1}')
        do
            mkdir -p "${sm_log_dir}"/istio/"${pod_name}"/debug
            for debug_path in "${istioDebugURL[@]}"
            do
                kubectl_cmd exec "${pod_name}" -c discovery -- curl --silent http://localhost:15014/debug/"${debug_path}" > "${sm_log_dir}"/istio/"${pod_name}"/debug/"${debug_path}"
            done
        done
        if kubectl_cmd get crd | grep -q istio.io
        then
            log_message "SM Controller CRDs have been found, looking for applied CRs..."
            for sm_crs in "${serviceMeshCustomResources[@]}"
            do
                if [[ $(kubectl_cmd get "${sm_crs}".istio.io --ignore-not-found) ]]
                then
                    sm_cr=$(echo "${sm_crs}" | gawk -F "." '{print $1}')
                    mkdir -p "${sm_log_dir}"/ServiceMeshCRs/"${sm_cr}"
                    log_message "Applied ${sm_cr} CR has been found, gathering sm_log for it..."
                    for resource in $(kubectl_cmd get "${sm_crs}".istio.io --no-headers | gawk -F " " '{print $1}')
                    do
                        kubectl_cmd get "${sm_crs}".istio.io "${resource}" -o yaml > "${sm_log_dir}"/ServiceMeshCRs/"${sm_cr}"/"${resource}".yaml
                    done
                fi
            done
        else
            log_message "No SM Controller CRD has been found!"
        fi
        if kubectl_cmd get pods -o jsonpath='{.items[*].spec.containers[*].name}' | grep istio-proxy
        then
            mkdir -p "${sm_log_dir}"/proxies
            log_message "Pods with istio-proxy container are found, gathering sm_log for pods with istio-proxy..."
            for pod_name in $(kubectl_cmd get pods -o custom-columns=NAME:.metadata.name,CONTAINERS:.spec.containers[*].name | grep istio-proxy | gawk '{print $1}')
            do
                mkdir "${sm_log_dir}"/proxies/"${pod_name}"
                for debug_path in "${proxyDebugURL[@]}"
                do
                    if [[ ${debug_path} == "stats/prometheus" ]]; then
                        mkdir "${sm_log_dir}"/proxies/"${pod_name}"/stats
                    fi
                    kubectl_cmd exec "${pod_name}" -c istio-proxy -- curl --silent http://localhost:15000/"${debug_path}" > "${sm_log_dir}"/proxies/"${pod_name}"/"${debug_path}"
                done
            done
        else
            log_message "Pods with istio-proxy containers are not found or not running, doing nothing"
        fi
    else
        log_message "ServiceMesh Controller pods are not found or not running, doing nothing"
    fi
}

compress_files() {
    log_message "Generating tar file and removing logs directory..."
    exec 2>&1
    if tar cfz "${PWD}"/"${log_base_dir}".tgz "${log_base_dir}"; then
        echo -e "\e[1m\e[31mGenerated file ${PWD}/${log_base_dir}.tgz\e[0m"
        rm -rf "${PWD:?}"/"${log_base_dir}"
    else
        echo -e "\e[1m\e[31mFAILED to generate the tar file, keeping collected logs in ${PWD}/${log_base_dir}\e[0m"
        rm -f "${PWD}"/"${log_base_dir}".tgz
    fi
}
# This function will be called before any data collection is started but after default script initialization.
# Applications can add custom checks, environment variables setup etc. in this hook.
# Note, do not change any global script variables nor remove/change any collected ADP-specific data
pre_collect_hook() {
    :
}

# This function will be called after default data collection is finished. Potentially, it can also run in parallel with default collectors.
# Applications can put the main data collection logic here.
# Note, do not change any global script variables nor remove/change any collected ADP-specific data
extra_collect_hook() {
    :
}

# This function will be called prio to data compression.
# Application can add collected data cleanup or other data transformation steps here.
# Note, do not change any global script variables nor remove/change any collected ADP-specific data
pre_compress_hook() {
    :
}

#This function will be called after data compression is finished.
#If needed, application can log the bundle generation event or perform manipulations with the generated bundle.
# Note, do not change any global script variables nor remove/change any collected ADP-specific data
post_compress_hook() {
    :
}

# This function will source any external script(s) that has been defined as input argument
# Script will be sourced in the order they were listed when calling the script
source_external_script() {
    if [[ ${#external_scripts[@]} -ne 0 ]];
    then
        for file in "${external_scripts[@]}" ; do
            # shellcheck disable=SC1090
            source "$file"
        done
    fi
}

source_external_script
pre_collect_hook
get_describe_info
get_events
if [[ $collectionMode == "all" ]]; then
    get_pods_logs
    stream_logs all
elif [[ $collectionMode == "k8s" ]]; then
    get_pods_logs
elif [[ $collectionMode == "container" ]]; then
    stream_logs cont
else
    stream_logs all
fi

get_helm_info
cmm_log
siptls_logs
cmy_log
diameter_log
basic_checks
get_ss7_cnf
sm_log
extra_collect_hook
pre_compress_hook
compress_files
post_compress_hook

if [[ $search_engine_exit_code -ne 0 ]]; then
  echo "Note: Streamed logs could not be collected from Search Engine."
fi
