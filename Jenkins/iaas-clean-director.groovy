def AGENT_LABEL = null
def EXCLUSIVE_BS_LABEL = '5G-ESC-001'
def POOL_BS_LABEL = '5G-SC'

node('DSC_BuildSlave_GIC_1||DSC_BuildSlave_GIC_2||DSC_BuildSlave_GIC_3||DSC_BuildSlave_GIC_4||DSC_BuildSlave_GIC_5||SERO_GIC')
{
    stage('set agent')
    {
        script
        {

            online_node = nodesByLabel label: """$EXCLUSIVE_BS_LABEL""", offline: false
            if (online_node)
            {
                echo """Agent with label $EXCLUSIVE_BS_LABEL is online. The job will run on it."""
                AGENT_LABEL = EXCLUSIVE_BS_LABEL
            }
            else
            {
                echo """Agent with label $EXCLUSIVE_BS_LABEL is offline. The job will be deviated to the pool of build slaves. with label $POOL_BS_LABEL"""
                AGENT_LABEL = POOL_BS_LABEL
            }
        }
    }
}

def emailbody_start="Hi,"
def emailbody_end="\n\nBR,\nYour humble Jenkins"
def emailbody_abort_job_1="\nthere might still be a ${JOB_BASE_NAME} job type running on ${IAAS_NODE}."
def emailbody_abort_job_2="\nThis one (build number ${BUILD_NUMBER}) might create an interference."

pipeline
{
    agent
    {
        node
        {
            label "${AGENT_LABEL}"
            customWorkspace "/home/jenkins/workspace/5G-ESC/IaaS/${JOB_BASE_NAME}_${IAAS_NODE}"
        }
    }
    options
    {
        timeout(time: 2, unit: 'HOURS')
        timestamps()
    }
    environment
    {
        // variables from initial form
        IAAS_NODE = "${IAAS_NODE}"
        PRUNE_IMAGES = "${PRUNE_IMAGES}"
        CLEAN_REGISTRY = "${CLEAN_REGISTRY}"
        CLEAN_EVNFM = "${CLEAN_EVNFM}"
        MAIL = "${MAIL}"
        DAFT_DIR = "./daft"
        NW_CONFIG_FILE_DIR = "${DAFT_DIR}/network_config_files"
        DAFT_LOGS_DIR = "/home/eccd/workspaces"
        BIN_DIR = "/home/eccd/bin/"
        SCRIPTS_DIR = "./scripts/"
        SCHEDULER_TYPE = "${SCHEDULER_TYPE}"
        SCHEDULER_PATH="/proj/DSC/rebels/IaaS/${SCHEDULER_TYPE}"
        JENKINS_INSTANCE = "https://fem1s10-eiffel029.eiffel.gic.ericsson.se:8443/jenkins/job/5G-ESC/job/IaaS/job"
        JENKINS_INSTANCE_NEW = "https://fem3s10-eiffel029.eiffel.gic.ericsson.se:8443/jenkins/job/5G-ESC/job/IaaS/job"
    }

    stages
    {
        stage('Workspace check')
        {
            steps
            {
                script
                {
                    OWN_WORKSPACE = """${sh( returnStdout: true, script: "pwd | cut -d '/' -f7")}""".trim()
                }

                script
                {
/*
                    if ("${OWN_WORKSPACE}" != "${JOB_BASE_NAME}_${IAAS_NODE}")
                    {
                        RECIPIENT = "eedjoz@eed.ericsson.se;" + env.MAIL
                        emailext body: "${emailbody_start}"+"${emailbody_abort_job_1}"+"${emailbody_abort_job_2}"+"${emailbody_end}",
                        subject: 'Potential attempt to run parallel jobs on the same node', to: "${RECIPIENT}"

                        echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! "
                        echo "!!!!!!!!!!!!!!!!! ATTENTION: there might still a ${JOB_BASE_NAME} job type running on ${IAAS_NODE} !!!!!!!!!!!!!!!!! "
                        echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! "
                    }
    */
                    if ( fileExists("/proj/DSC_CI/jenkins/tools/help_scripts/concurrentBuilds.sh") )
                    {
						// Log current job
						sh "/proj/DSC_CI/jenkins/tools/help_scripts/concurrentBuilds.sh -start"
					} // if
                }
            }
        }
        stage('Get needed info')
        {
            steps
            {
//                sh "sudo rm -rf ../${JOB_BASE_NAME}_${IAAS_NODE}@2*"

                getData()
            }
        }
        stage('Prune docker images')
        {
            when { environment name: 'PRUNE_IMAGES', value: 'yes' }
            steps
            {
                echo 'Prune docker images to free up space'
                script
                {
                    CONTAINER_ENGINE = """${sh( returnStdout: true,
                            script: "ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP} 'which docker; echo \$?' ")}""".trim()
                    if ( "${CONTAINER_ENGINE}".contains("1"))
                    {
                        CONTAINER_ENGINE_USED = "nerdctl"
                    }
                    else
                    {
                        CONTAINER_ENGINE_USED = "docker"
                    }
                }
                sh "${DAFT_DIR}/expect/bin/send_command_to_ssh.exp --timeout=1800 --user=eccd --ip=${NODE_DIRECTOR_IP} --password='notneeded' --stop-on-error \
                    --command='sudo ${CONTAINER_ENGINE_USED} system prune -a -f' "
            }
            post
            {
                failure
                {
                    script
                    {
                        if ("${env.MAIL}" != 'null')
                        {
                            def emailbody_job_failure_1="\nhe pruning of images on node ${IAAS_NODE} has failed.\n\nDetailed logs for the overall clean director jenkins"
                            def emailbody_job_failure_2=" job can be found under: ${LOGS_LINK}/$BUILD_NUMBER/consoleFull"
                            emailext body: "${emailbody_start}"+"${emailbody_job_failure_1}"+"${emailbody_job_failure_2}"+"${emailbody_end}",
                            subject: 'Pruning of images during automatic IaaS Clean Director failed', to: "${env.MAIL}"
                        }
                    }
                }
            }
        }
        stage('Remove intermediate csar packages & clean workspaces dir')
        {
            steps
            {
                echo 'Remove unwanted packages for downloads'
                sh "${DAFT_DIR}/expect/bin/send_command_to_ssh.exp --timeout=1800 --user=eccd --ip=${NODE_DIRECTOR_IP} --password='notneeded' --stop-on-error \
                    --command='mkdir -p /home/eccd/bin/'"
                sh "${DAFT_DIR}/expect/bin/scp_files.exp --from-data='${SCRIPTS_DIR}/clean_directory.pl' \
                    --to-data='eccd@${NODE_DIRECTOR_IP}:${BIN_DIR}';"
                sh "${DAFT_DIR}/expect/bin/scp_files.exp --from-data='${SCRIPTS_DIR}/protected_artifactory_files.txt' \
                    --to-data='eccd@${NODE_DIRECTOR_IP}:${BIN_DIR}';"
                sh "${DAFT_DIR}/expect/bin/send_command_to_ssh.exp --timeout=1800 --user=eccd --ip=${NODE_DIRECTOR_IP} --password='notneeded' --stop-on-error \
                    --command='perl ${BIN_DIR}/clean_directory.pl -d /home/eccd/download -f ${BIN_DIR}/protected_artifactory_files.txt' "

                echo 'Delete content of /home/eccd/workspaces/ dir older than 5 days'
                script
                {
                    CURRENT_DAYS_TO_DELETE = "5"
                    cleanWorkspacesDirectoryNode(CURRENT_DAYS_TO_DELETE)
                }
            }
            post
            {
                failure
                {
                    script
                    {
                        if ("${env.MAIL}" != 'null')
                        {
                            def emailbody_job_failure_1="\nthe removal of CSAR packages on node ${IAAS_NODE} has failed.\n\nDetailed logs for the overall clean director jenkins"
                            def emailbody_job_failure_2=" job can be found under: ${LOGS_LINK}/$BUILD_NUMBER/consoleFull"
                            emailext body: "${emailbody_start}"+"${emailbody_job_failure_1}"+"${emailbody_job_failure_2}"+"${emailbody_end}",
                            subject: 'Removal of CSAR packages during automatic IaaS Clean Director failed', to: "${env.MAIL}"
                        }
                    }
                }
            }
        }
        stage('Clean registry and remove core dumps')
        {
            steps
            {
                echo 'Execute needed steps in order to manage a registry cleaning'

                echo 'Remove ssh key from known hosts'
                sh "ssh-keygen -R ${NODE_DIRECTOR_IP}"
                uploadAndCheckExpect()

                echo 'Create DAFT package'
                sh "cd ${DAFT_DIR} && make clean && make daft"

                script
                {
                    ESC_PACKAGE_NAME = """${sh( returnStdout: true, script: 'ls ${DAFT_DIR} | grep ESC')}""".trim()
                    DAFT_NS_DIR = "/home/eccd/daft_ns_${IAAS_NODE}_clean-director"
                    NW_CONFIG_FILE_FULL_PATH = "${NW_CONFIG_FILE_DIR}/${IAAS_NODE}.xml"
                }
                echo 'Creating daft folder in IaaS node'

                sh "${DAFT_DIR}/expect/bin/send_command_to_ssh.exp --timeout=1800 --user=eccd --ip=${NODE_DIRECTOR_IP} --password='notneeded' --stop-on-error \
                --command='rm -rf ${DAFT_NS_DIR}' --command='mkdir ${DAFT_NS_DIR}'"

                echo 'Transfering files to IaaS node'
                sh "${DAFT_DIR}/expect/bin/scp_files.exp --from-data='${DAFT_DIR}/${ESC_PACKAGE_NAME}' --to-data='eccd@${NODE_DIRECTOR_IP}:${DAFT_NS_DIR}'"
                sh "${DAFT_DIR}/expect/bin/scp_files.exp --from-data='${NW_CONFIG_FILE_FULL_PATH}' --to-data='eccd@${NODE_DIRECTOR_IP}:${DAFT_NS_DIR}'"

                echo 'Unpacking DAFT inside the IaaS node'

                sh "${DAFT_DIR}/expect/bin/send_command_to_ssh.exp --timeout=1800 --user=eccd --ip=${NODE_DIRECTOR_IP} --password='notneeded' --stop-on-error \
                --command='tar -xvf ${DAFT_NS_DIR}/${ESC_PACKAGE_NAME} -C ${DAFT_NS_DIR}/' \
                --command='tar -xvf ${DAFT_NS_DIR}/DAFT*.tar.gz -C ${DAFT_NS_DIR}/' "


                echo "Perform a cleanup of the local registry (if selected) and removal of core dumps"
                sh "${DAFT_DIR}/expect/bin/send_command_to_ssh.exp --timeout=1800 --user=eccd --ip=${NODE_DIRECTOR_IP} --password='notneeded' --stop-on-error \
                --command='perl ${DAFT_NS_DIR}/perl/bin/execute_playlist.pl -p 108_Miscellaneous_Tasks -c no -n ${DAFT_NS_DIR}/${IAAS_NODE}.xml -v CLEAN_CORE_DUMPS=yes -v FETCH_CORE_DUMPS=no -v CLEAN_REGISTRY=${CLEAN_REGISTRY} \
                -j ${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}_clean-director -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=yes -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=yes -v INPUT_AVAILABLE=no \
                -v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=yes -w ${DAFT_LOGS_DIR}' "
            }
            post
            {
                always
                {
                    script
                    {
                        LOGS_CLEAN_DIRECTOR = """${sh( returnStdout: true,
                            script: "ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP} 'find ${DAFT_LOGS_DIR} -type d -name *${JOB_BASE_NAME}-${BUILD_NUMBER}_clean-director*'"
                        )}""".trim()
                    }
                }
                success
                {
                    script
                    {
                        if ("${env.MAIL}" != 'null')
                        {
                            def emailbody_registry_clean_job_finished_1="\nthe automatic registry cleanup"
                            def emailbody_registry_clean_job_finished_2=" on node ${IAAS_NODE} has concluded.\nDetailed logs can be found on the"
                            def emailbody_registry_clean_job_finished_3=" director of node ${IAAS_NODE} (IP address of director: ${NODE_DIRECTOR_IP}) under the folder:"
                            def emailbody_registry_clean_job_finished_4=" ${LOGS_CLEAN_DIRECTOR}\nDetailed logs for the overall undeployment jenkins job"
                            def emailbody_registry_clean_job_finished_5=" can be found under: ${LOGS_LINK}/$BUILD_NUMBER/consoleFull"

                            emailext body: "${emailbody_start}"+"${emailbody_registry_clean_job_finished_1}"+"${emailbody_registry_clean_job_finished_2}"+"${emailbody_registry_clean_job_finished_3}"+
                                    "${emailbody_registry_clean_job_finished_4}"+"${emailbody_registry_clean_job_finished_5}"+"${emailbody_end}", subject: "Automatic registry cleanup on node ${IAAS_NODE} concluded", to: "${env.MAIL}"
                        }
                    }
                }
                failure
                {
                    script
                    {
                        if ("${env.MAIL}" != 'null')
                        {
                            def emailbody_clean_director_failed_1="\nthe automatic clean up of local registry failed."
                            def emailbody_clean_director_failed_2="\nDetailed logs can be found on the director of node ${IAAS_NODE}"
                            def emailbody_clean_director_failed_3=" (IP address of director: ${NODE_DIRECTOR_IP}) under the folder: ${LOGS_CLEAN_DIRECTOR}"

                            emailext body: "${emailbody_start}"+"${emailbody_clean_director_failed_1}"+"${emailbody_clean_director_failed_2}"+
                            "${emailbody_clean_director_failed_3}"+"${emailbody_end}",
                            subject: "Cleanup of local registry on node ${IAAS_NODE} failed", to: "${env.MAIL}"
                        }
                    }
                }
            }
        }
        stage('Delete EVNFM Packages')
        {
            when { environment name: 'CLEAN_EVNFM', value: 'yes' }
            steps
            {
                echo 'Execute script to delete unused SC EVNFM packages'

                sh "${DAFT_DIR}/expect/bin/send_command_to_ssh.exp --timeout=1800 --user=eccd --ip=${NODE_DIRECTOR_IP} --password='notneeded' --stop-on-error \
                --command='perl ${DAFT_NS_DIR}/perl/bin/execute_playlist.pl -p 209_EVNFM_Delete_Package_SC -c no -n ${DAFT_NS_DIR}/${IAAS_NODE}.xml -v APP_TYPE=SC -v PACKAGE_CONTAINS=ALL \
                -j ${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}_EVNFM-Packages_clean-director -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=yes -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=yes -v INPUT_AVAILABLE=no \
                -v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=yes -w ${DAFT_LOGS_DIR}' "
            }
            post
            {
                always
                {
                    script
                    {
                        LOGS_CLEAN_DIRECTOR = """${sh( returnStdout: true,
                            script: "ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP} 'find ${DAFT_LOGS_DIR} -type d -name *${JOB_BASE_NAME}-${BUILD_NUMBER}_EVNFM-Packages_clean-director*'"
                        )}""".trim()
                    }
                }
                success
                {
                    script
                    {
                        if ("${env.MAIL}" != 'null')
                        {
                            def emailbody_delete_evnfm_packages_job_finished_1="\nthe automatic deletion of EVNFM packages"
                            def emailbody_delete_evnfm_packages_job_finished_2=" on node ${IAAS_NODE} has concluded.\nDetailed logs can be found on the"
                            def emailbody_delete_evnfm_packages_job_finished_3=" director of node ${IAAS_NODE} (IP address of director: ${NODE_DIRECTOR_IP}) under the folder:"
                            def emailbody_delete_evnfm_packages_job_finished_4=" ${LOGS_CLEAN_DIRECTOR}\nDetailed logs for the overall undeployment jenkins job"
                            def emailbody_delete_evnfm_packages_job_finished_5=" can be found under: ${LOGS_LINK}/$BUILD_NUMBER/consoleFull"

                            emailext body: "${emailbody_start}"+"${emailbody_delete_evnfm_packages_job_finished_1}"+"${emailbody_delete_evnfm_packages_job_finished_2}"+"${emailbody_delete_evnfm_packages_job_finished_3}"+
                                    "${emailbody_delete_evnfm_packages_job_finished_4}"+"${emailbody_delete_evnfm_packages_job_finished_5}"+"${emailbody_end}", subject: "Automatic EVNFM package deletion on node ${IAAS_NODE} concluded", to: "${env.MAIL}"
                        }
                    }
                }
                failure
                {
                    script
                    {
                        if ("${env.MAIL}" != 'null')
                        {
                            def emailbody_clean_director_failed_1="\nthe automatic deletion of EVNFM packages failed."
                            def emailbody_clean_director_failed_2="\nDetailed logs can be found on the director of node ${IAAS_NODE}"
                            def emailbody_clean_director_failed_3=" (IP address of director: ${NODE_DIRECTOR_IP}) under the folder: ${LOGS_CLEAN_DIRECTOR}"

                            emailext body: "${emailbody_start}"+"${emailbody_clean_director_failed_1}"+"${emailbody_clean_director_failed_2}"+
                            "${emailbody_clean_director_failed_3}"+"${emailbody_end}",
                            subject: "EVNFM deletion of packages on node ${IAAS_NODE} failed", to: "${env.MAIL}"
                        }
                    }
                }
            }
        }
        stage('Notification mail')
        {
            steps
            {
                script
                {
                    if ("${env.MAIL}" != 'null')
                    {
                        def emailbody_job_finished_1="\nthe job cleanup director for the IaaS node ${IAAS_NODE} has concluded.\n\nDetailed logs for the overall clean director jenkins"
                        def emailbody_job_finished_2=" job can be found under: ${LOGS_LINK}/$BUILD_NUMBER/consoleFull"
                        emailext body: "${emailbody_start}"+"${emailbody_job_finished_1}"+"${emailbody_job_finished_2}"+"${emailbody_end}",
                        subject: 'Automatic cleanup of director has concluded', to: "${env.MAIL}"
                    }
                }
            }
        }
    }
    post
    {
        always
        {
            script
            {
                if ("${env.SCHEDULER_TYPE}" != 'none')
                {
                    echo 'Remove stamp for wait node to be cleaned and create stamp for node cleaned notification'
                    NODE_TO_CLEAN = """${sh( returnStdout: true, script: "echo ${IAAS_NODE} | sed 's/_.*//'")}""".trim()

                    sh """touch ${SCHEDULER_PATH}/${NODE_TO_CLEAN}-Director_cleaned.tmp 2>/dev/null || true"""
                    sh """rm -f ${SCHEDULER_PATH}/${NODE_TO_CLEAN}-Wait-Finishing-Director_cleaned.tmp"""
                }
                if ( fileExists("/proj/DSC_CI/jenkins/tools/help_scripts/concurrentBuilds.sh") )
                {
					sh "/proj/DSC_CI/jenkins/tools/help_scripts/concurrentBuilds.sh -stop"
				} // if

                println("Free disk information prior to the cleanup activities")
                sh "ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP} 'df -h' "
                println("\nIn detail:")
                sh "ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP} 'du -hs /home/eccd/* | sort -hr' "
			} // script
            cleanWs()
        }
        failure
        {
            script
            {
                if ("${env.MAIL}" != 'null')
                {
                    def emailbody_job_failure_1="\nthe job cleanup director for the IaaS node ${IAAS_NODE} has failed.\n\nDetailed logs for the overall clean director jenkins"
                    def emailbody_job_failure_2=" job can be found under: ${LOGS_LINK}/$BUILD_NUMBER/consoleFull"
                    emailext body: "${emailbody_start}"+"${emailbody_job_failure_1}"+"${emailbody_job_failure_2}"+"${emailbody_end}",
                    subject: 'Automatic cleanup of director has failed', to: "${env.MAIL}"
                }
            }
        }
    }
}
def getData()
{
    script
    {
        // Assign paths to the logs
        LOGS_LINK = """${sh( returnStdout: true,
        script: "echo ${JENKINS_INSTANCE}/${JOB_BASE_NAME}")}""".trim()

        // Assign director-0 ip address
        sh "./scripts/bash/get_directors_ip.bash ${IAAS_NODE}"
        NODE_DIRECTOR_IP = """${sh( returnStdout: true, script: "cat ./var.DIRECTOR-0-IP")}""".trim()

        println("Free disk information prior to the cleanup activities")
        sh "ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP} 'df -h' "
        println("\nIn detail:")
        sh "ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP} 'du -hs /home/eccd/* | sort -hr' "
    }
}
def uploadAndCheckExpect()
{
    echo 'Copy expect to node'
    sh "${DAFT_DIR}/expect/bin/scp_files.exp --from-data='-r /proj/DSC/rebels/expect_tcl_files' \
    --to-data='eccd@${NODE_DIRECTOR_IP}:/home/eccd/expect_tcl_files'"

    echo 'Run expect installation script'
    sh "${DAFT_DIR}/expect/bin/send_command_to_ssh.exp --timeout=1800 --user=eccd --ip=${NODE_DIRECTOR_IP} --password='notneeded' --stop-on-error \
    --command='sudo /home/eccd/expect_tcl_files/install_expect.bash'"

    echo 'Copy gnuplot to node'
    sh "${DAFT_DIR}/expect/bin/scp_files.exp --from-data='-r /proj/DSC/rebels/gnuplot_files' \
    --to-data='eccd@${NODE_DIRECTOR_IP}:/home/eccd/gnuplot_files'"

    echo 'Run gnuplot installation script'
    sh "${DAFT_DIR}/expect/bin/send_command_to_ssh.exp --timeout=1800 --user=eccd --ip=${NODE_DIRECTOR_IP} --password='notneeded' --stop-on-error \
    --command='sudo /home/eccd/gnuplot_files/install_gnuplot.bash'"
}
def cleanWorkspacesDirectoryNode(THIS_CURRENT_PERIOD)
{
    script
    {
        /* Connect to the directory in the node and create 3 files:
        list_all_elements.txt : it contains all elements in the directory older than $THIS_CURRENT_PERIOD days
        list_keep_elements.txt : from the above file identify those still in use (the corresponding element contains an associated .lock element). We will keep each pair
        list_remove_elements.txt: the content of list_all_elements.txt minus the content of list_keep_elements.txt
        */

        sh """ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP} "find $DAFT_LOGS_DIR -maxdepth 1 -mtime +$THIS_CURRENT_PERIOD > $DAFT_LOGS_DIR/list_all_elements.txt 2>/dev/null || true" """
        sh """ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP} "cat $DAFT_LOGS_DIR/list_all_elements.txt | grep .lock > $DAFT_LOGS_DIR/list_identified_elements.txt 2>/dev/null || true" """
        sh """ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP} "awk '{print \\\$0; gsub(\\\".lock\\\", \\\"\\\", \\\$0); print \\\$0}'  $DAFT_LOGS_DIR/list_identified_elements.txt > $DAFT_LOGS_DIR/list_keep_elements.txt" """
        sh """ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP} "grep -Fxv -f $DAFT_LOGS_DIR/list_keep_elements.txt $DAFT_LOGS_DIR/list_all_elements.txt > $DAFT_LOGS_DIR/list_remove_elements.txt 2>/dev/null || true" """

        // copy the 3 files to archive them
        sh "scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP}:$DAFT_LOGS_DIR/list_all_elements.txt ./. "
        sh "scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP}:$DAFT_LOGS_DIR/list_keep_elements.txt ./. "
        sh "scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP}:$DAFT_LOGS_DIR/list_remove_elements.txt ./. "

        archiveArtifacts allowEmptyArchive: true, artifacts: "list*.txt", onlyIfSuccessful: false

        // Proceed to delete
        sh """ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP} "tr '\\n' '\\0' < $DAFT_LOGS_DIR/list_remove_elements.txt | xargs -r0 rm -fr --" """
    }
}