#!/bin/bash


get_top() {
    kubectl_cmd top pods --containers=true  > ${log_base_path}/logs/top_pod_containers.txt
}

get_extra_helm_info() {
    #works for helm3 and helm2

    log_message "- Getting extra Helm Information -"

    helm_dir="${log_base_path}"/helm
    mkdir -p "${helm_dir}"

    for deployment in `helm list -n ${namespace} -a | grep -v NAME | awk '{print $1}' | sort | uniq `
    do
        #helm get all $deployment --namespace $namespace > ${helm_dir}/${deployment}_get.txt  2> /dev/null
        helm get hooks $deployment --namespace $namespace > ${helm_dir}/${deployment}_get_hooks.txt  2> /dev/null
        helm get manifest $deployment --namespace $namespace > ${helm_dir}/${deployment}_get_manifest.txt  2> /dev/null
        helm get values $deployment --namespace $namespace > ${helm_dir}/${deployment}_get_values.txt  2> /dev/null
        helm get notes $deployment --namespace $namespace > ${helm_dir}/${deployment}_get_notes.txt  2> /dev/null
        helm status $deployment --namespace $namespace > ${helm_dir}/${deployment}_status.txt   2> /dev/null  # needed by PM server
        helm history $deployment --namespace $namespace > ${helm_dir}/${deployment}_history.txt   2> /dev/null  # needed by PM server
    done

}


get_document_db_pg_logs() {

#from data collection document:
#kubectl cp eric-data-document-database-pg-0:/var/lib/postgresql/data/pgdata/log/ ./eric-data-document-database-pg-0_pgdata_log/ --namespace=<pod_namespace>

    log_message "- Getting Document Database PG logs -"
    local db_pg_dir=${log_base_path}/db_pg
    mkdir ${db_pg_dir}

    if ! [[ $(kubectl_cmd get pods -l app=eric-data-document-database-pg --field-selector=status.phase=Running --no-headers 2>&1) =~ "No resources found" ]]
    then
        # echo "Document Database PG pod found running."
        for pod in `kubectl_cmd get pods -l app=eric-data-document-database-pg -o jsonpath='{.items[*].metadata.name}' 2> /dev/null`
        do
            #echo $pod
            kubectl_cmd cp -c eric-data-document-database-pg ${pod}:/var/lib/postgresql/data/pgdata/ ${db_pg_dir}/${pod}_log/ &> /dev/null
            rm -rf ${db_pg_dir}/${pod}_log/pg_wal ${db_pg_dir}/${pod}_log/base  2> /dev/null
    # added by SC:
            kubectl_cmd exec -c eric-data-document-database-pg ${pod} -- psql -U postgres -c '\l' -c '\c sc_database' -c '\dt+' -c '\du+'  > ${db_pg_dir}/${pod}_log/psql_sc_database.log  2> /dev/null
            kubectl_cmd exec -c eric-data-document-database-pg ${pod} -- psql -U postgres -c '\l' -c '\c postgres' -c '\dt+' -c '\du+'  > ${db_pg_dir}/${pod}_log/psql_postgres.log  2> /dev/null
            kubectl_cmd exec -c eric-data-document-database-pg ${pod} -- pg_dumpall -U postgres > ${db_pg_dir}/${pod}_log/pg_dumpall.txt  2> /dev/null
        done
    elif ! [[ $(kubectl_cmd get pods -l app=eric-cm-mediator-db-pg --field-selector=status.phase=Running --no-headers 2>&1) =~ "No resources found" ]]  # SC 1.15+
    then
        # echo "Document Database PG pod found running."
        for pod in `kubectl_cmd get pods -l app=eric-cm-mediator-db-pg -o jsonpath='{.items[*].metadata.name}' 2> /dev/null`
        do
            #echo $pod
            kubectl_cmd cp -c eric-cm-mediator-db-pg ${pod}:/var/lib/postgresql/data/pgdata/ ${db_pg_dir}/${pod}_log/ &> /dev/null
            rm -rf ${db_pg_dir}/${pod}_log/pg_wal ${db_pg_dir}/${pod}_log/base  2> /dev/null
    # added by SC:
            kubectl_cmd exec -c eric-cm-mediator-db-pg ${pod} -- psql -U postgres -c '\l' -c '\c sc_database' -c '\dt+' -c '\du+'  > ${db_pg_dir}/${pod}_log/psql_sc_database.log  2> /dev/null
            kubectl_cmd exec -c eric-cm-mediator-db-pg ${pod} -- psql -U postgres -c '\l' -c '\c postgres' -c '\dt+' -c '\du+'  > ${db_pg_dir}/${pod}_log/psql_postgres.log  2> /dev/null
            kubectl_cmd exec -c eric-cm-mediator-db-pg ${pod} -- pg_dumpall -U postgres > ${db_pg_dir}/${pod}_log/pg_dumpall.txt  2> /dev/null
        done
    else
        echo "Document Database PG pod not found or not running, doing nothing" > ${db_pg_dir}/Not_found.txt
    fi
}


get_ldap_configuration() {

    log_message "- Getting data from eric-sec-ldap-server - "

    local ldap_config_dir=${log_base_path}/ldap_config

    mkdir ${ldap_config_dir}

    if ! [[ $(kubectl_cmd get pods -l app=eric-sec-ldap-server --field-selector=status.phase=Running --no-headers 2>&1) =~ "No resources found" ]]
    then
        #echo "ldap-server pod(s) found running"
        for pod in `kubectl_cmd get pods -l app=eric-sec-ldap-server -o jsonpath='{.items[*].metadata.name}' 2> /dev/null`
        do
            mkdir -p ${ldap_config_dir}/${pod}_log
            kubectl_cmd exec ${pod} -c ldap -- ldapsearch -Y EXTERNAL -H ldapi://%2Frun%2Fslapd%2Fslapd.sock -b "ou=people,dc=la,dc=adp,dc=ericsson" > ${ldap_config_dir}/${pod}_log/ldap_users.txt  2> /dev/null
            kubectl_cmd exec ${pod} -c ldap -- ldapsearch -Y EXTERNAL -H ldapi://%2Frun%2Fslapd%2Fslapd.sock -b "ou=peopleextraattrs,dc=la,dc=adp,dc=ericsson" > ${ldap_config_dir}/${pod}_log/ldap_users_lastLoginTime.txt  2> /dev/null
            kubectl_cmd exec ${pod} -c ldap -- ldapsearch -Y EXTERNAL -H ldapi://%2Frun%2Fslapd%2Fslapd.sock -b "ou=policies,dc=la,dc=adp,dc=ericsson" > ${ldap_config_dir}/${pod}_log/ldap_policies.txt  2> /dev/null
            kubectl_cmd exec ${pod} -c ldap -- ldapsearch -Y EXTERNAL -H ldapi://%2Frun%2Fslapd%2Fslapd.sock -b "olcDatabase={1}ldap,cn=config" > ${ldap_config_dir}/${pod}_log/ldap_config.txt  2> /dev/null
            kubectl_cmd exec ${pod} -c ldap -- cat /proc/net/tcp > ${ldap_config_dir}/${pod}_log/proc_net_tcp.txt  2> /dev/null
            kubectl_cmd exec ${pod} -c ldap -- openssl s_client -connect localhost:1389 > ${ldap_config_dir}/${pod}_log/openssl_localhost_1389.txt  2> /dev/null
            #kubectl_cmd exec ${pod} -- ldapsearch -x -b '' -s base '(objectclass=top)' namingContexts > ${ldap_config_dir}/${pod}_log/ldap_health.txt  2> /dev/null
        done
    else
        echo "ldap server pod not found or not running, doing nothing" > ${ldap_config_dir}/Not_found_LDAP_server.txt
    fi

    if ! [[ $(kubectl_cmd get pods -l app=eric-sec-ldap-server-proxy --field-selector=status.phase=Running --no-headers 2>&1) =~ "No resources found" ]]
    then
        for pod in `kubectl_cmd get pods -l app=eric-sec-ldap-server-proxy -o jsonpath='{.items[*].metadata.name}' 2> /dev/null`
        do
            mkdir -p ${ldap_config_dir}/${pod}_log
            kubectl_cmd exec ${pod} -c ldapproxy -- openssl s_client -connect eric-sec-ldap-server-0.eric-sec-ldap-server-peer:1389 > ${ldap_config_dir}/${pod}_log/openssl_ldap_server_1389.txt  2> /dev/null
        done
    else
        echo "ldap proxy pod not found or not running, doing nothing" > ${ldap_config_dir}/Not_found_LDAP_proxy.txt
    fi

    if ! [[ $(kubectl_cmd get pods -l app=eric-sec-admin-user-management --field-selector=status.phase=Running --no-headers 2>&1) =~ "No resources found" ]]
    then
        for pod in `kubectl_cmd get pods -l app=eric-sec-admin-user-management -o jsonpath='{.items[*].metadata.name}' 2> /dev/null`
        do
            mkdir -p ${ldap_config_dir}/${pod}_log
            timeout 2 kubectl_cmd exec ${pod} -- openssl s_client -connect eric-sec-ldap-server:636 > ${ldap_config_dir}/${pod}_log/openssl_ldap_service_636.txt  2> /dev/null
        done
    else
        echo "AUM pod not found or not running, doing nothing" > ${ldap_config_dir}/Not_found_AUM.txt
    fi
}


get_sc_config_workers() {

    log_message "- Collecting SC Envoy Information -"

    local sc_config_dir=${log_base_path}/sc_config

    declare -a nf=("scp" "sepp" "csa")

    mkdir -p ${sc_config_dir}
	mkdir -p ${sc_config_dir}/dced

    #The DECD Database for SC contains for example the N32-c negotiated state
    for i in {0..2}; do kubectl_cmd exec eric-data-distributed-coordinator-ed-sc-${i} -c dced -- etcdctl get / --prefix  > ${sc_config_dir}/dced/dced_db_dump_ed-sc-${i}.txt  2> /dev/null; done

    # Loop through the Network Functions that have Envoy configuration
    for nf in "${nf[@]}"
    do
        # log_message "$nf"
        # or do whatever with individual element of the array

        if ! [[ $(kubectl_cmd get pods -l app=eric-$nf-worker --field-selector=status.phase=Running --no-headers 2>&1) =~ "No resources found" ]]
        then
            log_message "- Getting data from eric-$nf-worker containers"

            mkdir -p ${sc_config_dir}/$nf

            #log_message "$nf-worker pods found running."
            for i in `kubectl_cmd get pods -l app=eric-$nf-worker --field-selector=status.phase=Running -o jsonpath='{.items[*].metadata.name}' 2> /dev/null `
            do
                #log_message $i
                mkdir -p ${sc_config_dir}/$nf/$i
                kubectl_cmd exec $i -c eric-$nf-worker -i -- curl -s http://localhost:9901/certs > ${sc_config_dir}/$nf/$i/certs.txt  2> /dev/null
                kubectl_cmd exec $i -c eric-$nf-worker -i -- curl -s http://localhost:9901/clusters > ${sc_config_dir}/$nf/$i/clusters.txt  2> /dev/null
                kubectl_cmd exec $i -c eric-$nf-worker -i -- curl -s http://localhost:9901/config_dump?include_eds > ${sc_config_dir}/$nf/$i/config_dump.txt  2> /dev/null
                kubectl_cmd exec $i -c eric-$nf-worker -i -- curl -s http://localhost:9901/contention > ${sc_config_dir}/$nf/$i/contention.txt  2> /dev/null
                kubectl_cmd exec $i -c eric-$nf-worker -i -- curl -s http://localhost:9901/memory > ${sc_config_dir}/$nf/$i/memory.txt  2> /dev/null
                kubectl_cmd exec $i -c eric-$nf-worker -i -- curl -s http://localhost:9901/hot_restart_version > ${sc_config_dir}/$nf/$i/hot_restart_version.txt  2> /dev/null

                kubectl_cmd exec $i -c eric-$nf-worker -i -- curl -s http://localhost:9901/listeners > ${sc_config_dir}/$nf/$i/listeners.txt  2> /dev/null
                kubectl_cmd exec $i -c eric-$nf-worker -i -- curl -s http://localhost:9901/ready > ${sc_config_dir}/$nf/$i/ready.txt  2> /dev/null
                kubectl_cmd exec $i -c eric-$nf-worker -i -- curl -s http://localhost:9901/runtime > ${sc_config_dir}/$nf/$i/runtime.txt  2> /dev/null
                kubectl_cmd exec $i -c eric-$nf-worker -i -- curl -s http://localhost:9901/server_info > ${sc_config_dir}/$nf/$i/server_info.txt  2> /dev/null
                kubectl_cmd exec $i -c eric-$nf-worker -i -- curl -s http://localhost:9901/stats > ${sc_config_dir}/$nf/$i/stats.txt  2> /dev/null
                kubectl_cmd exec $i -c eric-$nf-worker -i -- curl -s http://localhost:9901/stats/prometheus > ${sc_config_dir}/$nf/$i/prometheus_stats.txt  2> /dev/null
                kubectl_cmd exec $i -c eric-$nf-worker -i -- curl -s -X POST http://localhost:9901/logging > ${sc_config_dir}/$nf/$i/logging_status.txt

            done
        else
            log_message "$nf-worker pods not found or not running, doing nothing" > ${sc_config_dir}/${nf}_worker_not_found.txt
        fi

    done
}

get_sc_config_and_logs(){

    echo "- Getting SC Config from CM_Mediator"

    local sc_config_dir=${log_base_path}/sc_config

    declare -a configuration=("ericsson-bsf" "ericsson-diameter-adp" "ericsson-scp" "ericsson-sepp" "ericsson-dsc-oam-mom" "ericsson-sctp-adp")

    mkdir -p ${sc_config_dir}

    if ! [[ $(kubectl_cmd get pods -l app=eric-cm-mediator --field-selector=status.phase=Running --no-headers 2>&1) =~ "No resources found" ]]
    then
        local cm_pod=$(kubectl_cmd get pods -l app=eric-cm-mediator --field-selector=status.phase=Running -o jsonpath='{.items[0].metadata.name}' 2> /dev/null )
        local cm_port=$(kubectl_cmd get -o jsonpath="{.spec.ports[0].port}" services eric-cm-mediator  2> /dev/null)

        # Loop through the SC configuration
        for config in "${configuration[@]}"
        do
            kubectl_cmd exec ${cm_pod} -i -c eric-cm-mediator -- curl -s "http://localhost:${cm_port}/cm/api/v1/configurations/${config}" > ${sc_config_dir}/${config}_config.json  2> /dev/null
	        if [ $? -ne 0 ]; then
	        	kubectl_cmd exec ${cm_pod} -i -c eric-cm-mediator -- curl -s "https://localhost:${cm_port}/cm/api/v1/configurations/${config}" --cacert /run/secrets/eric-sec-sip-tls-trusted-root-cert/ca.crt --cert /run/secrets/eric-cm-mediator-tls-client-secret/clicert.pem --cert-type PEM --key /run/secrets/eric-cm-mediator-tls-client-secret/cliprivkey.pem --key-type PEM > ${sc_config_dir}/${config}_config.json  2> /dev/null
	            if [ $? -ne 0 ]; then
	            	kubectl_cmd exec ${cm_pod} -i -c eric-cm-mediator -- curl -s "https://localhost:${cm_port}/cm/api/v1/configurations/${config}" --cacert /run/secrets/eric-sec-sip-tls-trusted-root-cert/ca.crt --cacert /etc/sip-tls-ca/ca.crt --cert /etc/sip-tls-client/clicert.pem --cert-type PEM --key /etc/sip-tls-client/cliprivkey.pem --key-type PEM > ${sc_config_dir}/${config}_config.json  2> /dev/null
	            fi
	        fi
        done
    fi
    
    
    if ! [[ $(kubectl_cmd get pods -l global_app=eric-bsf-wcdb-cd --field-selector=status.phase=Running --no-headers 2>&1) =~ "No resources found" ]]
    then
        for pod in `kubectl_cmd get pods -l global_app=eric-bsf-wcdb-cd -o jsonpath='{.items[*].metadata.name}' 2> /dev/null`
        do
            mkdir -p ${sc_config_dir}/bsf/$pod
            kubectl_cmd cp $pod:/usr/share/cassandra/logs/ -c cassandra ${sc_config_dir}/bsf/${pod} &> /dev/null
            gzip ${sc_config_dir}/bsf/$pod/*
            kubectl_cmd exec eric-bsf-wcdb-cd-datacenter1-rack1-0 -c cassandra -- sh -c "cd /etc/cassandra/conf && tar chf - cassandra*" | tar xf - -C ${sc_config_dir}/bsf/${pod} &> /dev/null            
            kubectl_cmd exec eric-bsf-wcdb-cd-datacenter1-rack1-0 -c cassandra -- sh -c "cd /etc/cassandra/conf && tar chf - *.options" | tar xf - -C ${sc_config_dir}/bsf/${pod} &> /dev/null
        done
    fi
    
}


get_wireshark_hosts_file(){

    log_message "- Getting hosts file for Wireshark - "

    local hosts_dir=${log_base_path}/hosts_file_for_wireshark

    mkdir -p ${hosts_dir}

    if [ "$collect_all_ns" == "1"  ] ; then
        kubectl_cmd get pods -A -o=custom-columns=:.status.podIP,:.metadata.name  2> /dev/null >  ${hosts_dir}/hosts  2> /dev/null
        kubectl_cmd get svc  -A -o=custom-columns=:.spec.clusterIP,:.metadata.name 2> /dev/null | grep -v None >> ${hosts_dir}/hosts  2> /dev/null
    else
        kubectl_cmd get pods -o=custom-columns=:.status.podIP,:.metadata.name  2> /dev/null >  ${hosts_dir}/hosts  2> /dev/null
        kubectl_cmd get svc  -o=custom-columns=:.spec.clusterIP,:.metadata.name 2> /dev/null | grep -v None >> ${hosts_dir}/hosts  2> /dev/null
    fi
    kubectl_cmd get nodes   -o=custom-columns=:.status.addresses[0].address,:.metadata.name 2> /dev/null >> ${hosts_dir}/hosts  2> /dev/null

}


get_alarms(){

    log_message "- Getting alarms -"

    local alarms_dir=${log_base_path}/alarms

    mkdir ${alarms_dir}

    AH_POD=$(kubectl_cmd get pods -l app=eric-fh-alarm-handler --field-selector=status.phase=Running -o jsonpath="{.items[0].metadata.name}" 2> /dev/null )

    kubectl_cmd exec ${AH_POD} -- ah_alarm_list.sh > ${alarms_dir}/active_alarms.json 2> /dev/null

    kubectl_cmd exec ${AH_POD} -- ah_alarm_list.sh -f > ${alarms_dir}/active_alarms_full.json 2> /dev/null
}


get_pm_logs(){
    log_message "- Getting PM Server logs -"

    local pm_server_dir=${log_base_path}/pm_server_files
    mkdir -p ${pm_server_dir}

    #get CMYP Pod, since it has curl and access to pm-server
    CMYP_POD=$(kubectl_cmd get pods -l app=eric-cm-yang-provider --field-selector=status.phase=Running -o jsonpath="{.items[0].metadata.name}" 2> /dev/null )

    kubectl_cmd exec -c yang-engine ${CMYP_POD} -- curl -s http://eric-pm-server:9090/api/v1/targets  > ${pm_server_dir}/api_targets.json  2> /dev/null
    kubectl_cmd exec -c yang-engine ${CMYP_POD} -- curl -s http://eric-pm-server:9090/api/v1/status/tsdb  > ${pm_server_dir}/api_status_tsdb.json  2> /dev/null
    kubectl_cmd exec -c yang-engine ${CMYP_POD} -- curl -s http://eric-pm-server:9090/api/v1/status/runtimeinfo  > ${pm_server_dir}/api_status_runtimeinfo.json  2> /dev/null
    kubectl_cmd exec -c yang-engine ${CMYP_POD} -- curl -s http://eric-pm-server:9090/api/v1/status/flags  > ${pm_server_dir}/api_status_flags.json  2> /dev/null
    kubectl_cmd exec -c yang-engine ${CMYP_POD} -- curl -s http://eric-pm-server:9090/api/v1/status/buildinfo  > ${pm_server_dir}/api_status_buildinfo.json  2> /dev/null
    kubectl_cmd exec -c yang-engine ${CMYP_POD} -- curl -s http://eric-pm-server:9090/api/v1/status/walreplay  > ${pm_server_dir}/api_status_walreplay.json  2> /dev/null
}


get_pm_xml_files(){

    log_message "- Getting PM XML file - "

    local pm_dir=${log_base_path}/pm_xml_files

    mkdir -p ${pm_dir}

    PMBR_POD=$(kubectl_cmd get pods -l app=eric-pm-bulk-reporter --field-selector=status.phase=Running -o jsonpath="{.items[0].metadata.name}" 2> /dev/null )

    kubectl_cmd exec -c eric-pm-bulk-reporter  ${PMBR_POD} -- ls -la /PerformanceManagementReportFiles | head -n -1 | tail -n +4  > ${pm_dir}/PerformanceManagementReportFiles.txt

    kubectl_cmd cp -c eric-pm-bulk-reporter ${PMBR_POD}:/PerformanceManagementReportFiles ${pm_dir}/PerformanceManagementReportFiles &> /dev/null
    if [ $? -ne 0 ]; then
      # If the previous command didn't work (like in SC 1.13), read file names into an array
      IFS=$'\n' read -d '' -r -a lines < ${pm_dir}/PerformanceManagementReportFiles.txt

      # and fetch each file separately
      for line in "${lines[@]}"
      do
        file=$(log_message $line | awk '{print $9}' | tr -d '\r\t\n'  ) # remove line feed characters from string
        #log_message "/PerformanceManagementReportFiles/${file}"
        kubectl_cmd exec -i -c eric-pm-bulk-reporter ${PMBR_POD} -- cat /PerformanceManagementReportFiles/${file} > ${pm_dir}/${file}
      done
    fi;
}


get_pm_xml_objectstore_files(){

    log_message "- Getting PM XML file from ObjectStore - "

    local pm_dir=${log_base_path}/pm_xml_files

    mkdir -p ${pm_dir}

    OBJ_POD=$(kubectl_cmd get pods -l app=eric-data-object-storage-mn-mgt --field-selector=status.phase=Running -o jsonpath='{.items[0].metadata.name}' 2> /dev/null )

    if [ $? -eq 0 ]; then
      if [ ! -z "$OBJ_POD" ]; then
        export MINIO_ACCESS_KEY=$(kubectl_cmd get secret eric-data-object-storage-mn-secret -o jsonpath='{.data.accesskey}' 2> /dev/null | base64 -d)
        export MINIO_SECRET_KEY=$(kubectl_cmd get secret eric-data-object-storage-mn-secret -o jsonpath='{.data.secretkey}' 2> /dev/null | base64 -d)
        kubectl_cmd exec $OBJ_POD -c manager -i -- bash -c "mc config host add pmbr https://eric-data-object-storage-mn:9000 ${MINIO_ACCESS_KEY} ${MINIO_SECRET_KEY} --insecure &>/dev/null ; mc cp pmbr/eric-pmbr-rop-file-store /dev/shm/  --recursive --insecure &>/dev/null" 2> /dev/null

        kubectl_cmd exec -c manager ${OBJ_POD} -- ls -la /dev/shm/eric-pmbr-rop-file-store | head -n -1 | tail -n +4  > ${pm_dir}/PerformanceManagementReportFiles.txt

        kubectl_cmd cp ${OBJ_POD}:/dev/shm/eric-pmbr-rop-file-store ${pm_dir}/eric-pmbr-rop-file-store &> /dev/null
        if [ $? -ne 0 ]; then
          # If the previous command didn't work (like in SC 1.15), read file names into an array
          IFS=$'\n' read -d '' -r -a lines < ${pm_dir}/PerformanceManagementReportFiles.txt

          # and fetch each file separately
          for line in "${lines[@]}"
          do
            file=$(echo $line | awk '{print $9}' | tr -d '\r\t\n'  ) # remove line feed characters from string
            #echo "/PerformanceManagementReportFiles/${file}"
            kubectl_cmd exec -i -c manager ${OBJ_POD} -- cat /dev/shm/eric-pmbr-rop-file-store/${file} > ${pm_dir}/${file}
          done
        fi;
          
        kubectl_cmd exec $OBJ_POD -c manager -i -- bash -c "rm -rf /dev/shm/eric-pmbr-rop-file-store &>/dev/null"
      fi
    fi

}


get_pm_rop_files(){
    PMBR_CONTAINERS=($(kubectl_cmd get deploy eric-pm-bulk-reporter -o jsonpath="{.spec.template.spec.containers[*].name}"))

    if [[ ${PMBR_CONTAINERS[@]} =~ "eric-pm-sftp" ]]; then
        get_pm_xml_files
    else
        get_pm_xml_objectstore_files
    fi
}

get_eccd_logs(){

  log_message "- Getting ECCD logs - "

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

  log_message "- Getting BRO logs - "

  local bro_dir=${log_base_path}/bro

  mkdir ${bro_dir}

  if ! [[ $(kubectl_cmd get pods -l app.kubernetes.io/name=eric-ctrl-bro --field-selector=status.phase=Running --no-headers 2>&1) =~ "No resources found" ]]
  then
      #log_message "BRO Pods found running, getting all configurations ..."
      for pod in `kubectl_cmd get pods -l app.kubernetes.io/name=eric-ctrl-bro -o jsonpath='{.items[*].metadata.name}' 2> /dev/null`
      do
          kubectl_cmd cp -c eric-ctrl-bro ${pod}:/bro/backupManagers/DEFAULT/schedulerInformation.json  ${bro_dir}/${pod}_schedulerInformation.json &> /dev/null
          kubectl_cmd cp -c eric-ctrl-bro ${pod}:/bro/backupManagers/DEFAULT/periodic-events  ${bro_dir}/${pod}_periodic-events &> /dev/null
      done
  else
      echo "BRO containers not found or not running, doing nothing" > ${bro_dir}/Not_found.txt
  fi
}

postprocessing1(){

    log_message "- Postprocessing 1 - "

    find ${log_base_path} > ${log_base_path}/directory_content.txt

}

###############################################

function pre_collect_hook(){
    log_message "nothing to collect in pre_collect_hook"
}

function extra_collect_hook(){
    get_top
    get_extra_helm_info
    get_document_db_pg_logs
    get_ldap_configuration
    get_sc_config_workers
    get_sc_config_and_logs
    get_wireshark_hosts_file
    get_alarms
    get_pm_logs
    get_pm_rop_files
    get_eccd_logs
    get_bro_logs
}

function pre_compress_hook(){
    postprocessing1
}

function post_compress_hook(){
       #echo "nothing to collect in post_compress_hook"
}
