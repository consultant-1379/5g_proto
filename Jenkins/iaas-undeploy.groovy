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
//      variables from initial form
        IAAS_NODE = "${IAAS_NODE}"
        NW_CONFIG_FILE = "${NW_CONFIG_FILE}"
        RELEASE_TYPE_TO_UNDEPLOY = "${RELEASE_TYPE_TO_UNDEPLOY}"
        CLEAN_REGISTRY ="${CLEAN_REGISTRY}"
        TERMINATE_METHOD="${TERMINATE_METHOD}"
        CLEANUP_RESOURCES="${CLEANUP_RESOURCES}"
        STACK_TYPE="${STACK_TYPE}"
        SKIP_UNDEPLOY_CRD = "${SKIP_UNDEPLOY_CRD}"
        MAIL = "${MAIL}"
        DAFT_DIR = "./daft"
        NW_CONFIG_FILE_DIR = "${DAFT_DIR}/network_config_files"
        DAFT_LOGS_DIR = "/home/eccd/workspaces"
        ALTERNATIVE_NAMESPACE = "${ALTERNATIVE_NAMESPACE}"
        REMOVE_NODE_LABELS_TAINTS = "${REMOVE_NODE_LABELS_TAINTS}"
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
        stage('Create & transfer files to IaaS node')
        {
            steps
            {
                echo 'Remove ssh key from known hosts'
                sh "ssh-keygen -R ${NODE_DIRECTOR_IP}"

                uploadAndCheckExpect()

                echo 'Create DAFT package'
                sh "cd ${DAFT_DIR} && make clean && make daft"

                script
                {
                    ESC_PACKAGE_NAME = """${sh( returnStdout: true, script: 'ls ${DAFT_DIR} | grep ESC')}""".trim()
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
            }
        }
        stage('Remove node labels and taints')
        {
            when { environment name: 'REMOVE_NODE_LABELS_TAINTS', value: 'yes' }
            steps
            {
                script
                {
                    labels_array = ["tools","deployment1","deployment2"]
                    for (i=0; i<3; i++)
                    {
                        CURRENT_LABEL =labels_array.get(i)
                        echo "Removing any node labels and taints type $CURRENT_LABEL on the node"
                        rewoveLabel(CURRENT_LABEL)
                    }
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
                            LOGS_FOLDER_REMOVE_LABELS_TAINTS = """${sh( returnStdout: true,
                            script: "ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP} 'find ${DAFT_LOGS_DIR} -type d -name *${JOB_BASE_NAME}-${BUILD_NUMBER}_Remove_Node_Label_${CURRENT_LABEL}*'"
                            )}""".trim()

                            def emailbody_failed_1="\nthe automatic removal of existing labels and taints type $CURRENT_LABEL via DAFT has failed."
                            def emailbody_failed_2="\nDetailed logs forh this part can be found on the director of node ${IAAS_NODE}"
                            def emailbody_failed_3=" (IP address of director: ${NODE_DIRECTOR_IP}) under the folder: ${LOGS_FOLDER_REMOVE_LABELS_TAINTS}"

                            emailext body: "${emailbody_start}"+"${emailbody_failed_1}"+"${emailbody_failed_2}"+
                            "${emailbody_failed_3}"+"${emailbody_end}",
                            subject: "Removal of existing labels and taints type $CURRENT_LABEL during automatic IaaS undeployment failed", to: "${env.MAIL}"
                        }
                    }
                }
            }
        }
        stage('Trigger undeployment')
        {
            steps
            {
                script
                {
                    if ("${RELEASE_TYPE_TO_UNDEPLOY}" == 'EVNFM')
                    {
                        echo "Terminate deployment from jenkins on namespace ${DAFT_NS}"
                        sh "${DAFT_DIR}/expect/bin/send_command_to_ssh.exp --timeout=1800 --user=eccd --ip=${NODE_DIRECTOR_IP} --password='notneeded' --stop-on-error \
                        --command='perl ${DAFT_NS_DIR}/perl/bin/execute_playlist.pl -p 206_EVNFM_Terminate_SC -c no \
                        -n ${DAFT_NS_DIR}/${NW_CONFIG_FILE_NAME} ${NW_CONFIG_OPTION} \
                        -j ${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}_Terminate_${RELEASE_TYPE_TO_UNDEPLOY} \
                        -v INPUT_AVAILABLE=no \
                        -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=yes -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=yes -v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=yes \
                        -v TERMINATE_METHOD=${TERMINATE_METHOD} -v STACK_TYPE=${STACK_TYPE} -v CLEANUP_RESOURCES=${CLEANUP_RESOURCES} \
                        -w ${DAFT_LOGS_DIR}' "
                    }
                    else
                    {
                        echo "Cleanup any possible existing deployment from jenkins on namespace ${DAFT_NS}"
                        sh "${DAFT_DIR}/expect/bin/send_command_to_ssh.exp --timeout=1800 --user=eccd --ip=${NODE_DIRECTOR_IP} --password='notneeded' --stop-on-error \
                        --command='perl ${DAFT_NS_DIR}/perl/bin/execute_playlist.pl -p 003_Undeploy_SC -c no -n ${DAFT_NS_DIR}/${NW_CONFIG_FILE_NAME} ${NW_CONFIG_OPTION} ${ALTERNATIVE_NS}\
                        -j ${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}_Undeploy_${RELEASE_TYPE_TO_UNDEPLOY}  -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=yes \
                        -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=yes -v INPUT_AVAILABLE=no \
                        -v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=yes -v SKIP_UNDEPLOY_CRD=${SKIP_UNDEPLOY_CRD} -v CLEAN_REGISTRY=${CLEAN_REGISTRY} -w ${DAFT_LOGS_DIR}' "
                    }
                }
            }
            post
            {
                always
                {
                    script
                    {
                        LOGS_FOLDER_UNDEPLOY = """${sh( returnStdout: true,
                            script: "ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP} 'find ${DAFT_LOGS_DIR} -type d -name *${JOB_BASE_NAME}-${BUILD_NUMBER}_${ACTION}*'"
                        )}""".trim()
                    }
                }
                failure
                {
                    script
                    {
                        if ("${env.MAIL}" != 'null')
                        {
                            def emailbody_undeploy_failed_1="\nthe automatic clean up of the existing deployment from jenkins on namespace ${DAFT_NS} via DAFT has failed."
                            def emailbody_undeploy_failed_2="\nDetailed logs regarding the undeployment can be found on the director of node ${IAAS_NODE}"
                            def emailbody_undeploy_failed_3=" (IP address of director: ${NODE_DIRECTOR_IP}) under the folder: ${LOGS_FOLDER_UNDEPLOY}"

                            emailext body: "${emailbody_start}"+"${emailbody_undeploy_failed_1}"+"${emailbody_undeploy_failed_2}"+
                            "${emailbody_undeploy_failed_3}"+"${emailbody_end}",
                            subject: "Cleanup of an existing deployment (${RELEASE_TYPE_TO_UNDEPLOY}) from jenkins on namespace ${DAFT_NS} failed", to: "${env.MAIL}"
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
                        def emailbody_job_finished_1="\nthe automatic cleanup of an existing deployment from jenkins on namespace ${DAFT_NS}"
                        def emailbody_job_finished_2=" on node ${IAAS_NODE} has concluded.\nDetailed logs can be found on the"
                        def emailbody_job_finished_3=" director of node ${IAAS_NODE} (IP address of director: ${NODE_DIRECTOR_IP}) under the folder:"
                        def emailbody_job_finished_4=" ${LOGS_FOLDER_UNDEPLOY}\nDetailed logs for the overall undeployment jenkins job"
                        def emailbody_job_finished_5=" can be found under: ${LOGS_LINK}/$BUILD_NUMBER/consoleFull"

                        emailext body: "${emailbody_start}"+"${emailbody_job_finished_1}"+"${emailbody_job_finished_2}"+"${emailbody_job_finished_3}"+
                        "${emailbody_job_finished_4}"+"${emailbody_job_finished_5}"+"${emailbody_end}", subject: "Automatic IaaS undeployment (${RELEASE_TYPE_TO_UNDEPLOY}) concluded", to: "${env.MAIL}"
                    }
                }
            }
        }
    }
    post
    {
        always
        {
            echo 'Remove any DAFT stamp leftover for registry concurrency'
            sh "ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP} 'rm -f /tmp/DAFT_docker_busy_with_id_${DAFT_NS}_*'"

            sh "scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP}:${DAFT_LOGS_DIR}/*${JOB_BASE_NAME}-${BUILD_NUMBER}*.tar.bz2 ./. 2>/dev/null || true"
            sh """echo "parameters.properties"   > ./names_array_split.txt"""

            sh """ls *${JOB_BASE_NAME}-${BUILD_NUMBER}*.tar.bz2 | grep -v troubleshooting_logs >> ./names_array_split.txt 2>/dev/null || true"""
            script
            {
                NUMBER_ADP_GROUPS = """${sh( returnStdout: true, script:""" ls *${JOB_BASE_NAME}-${BUILD_NUMBER}*.tar.bz2 | grep troubleshooting_logs | wc -l 2>/dev/null || true""")}""".trim()

                if ("${NUMBER_ADP_GROUPS}".toInteger() !=0)
                {
                unpackADPfiles("${NUMBER_ADP_GROUPS}".toInteger())
                }
            }
            artifactList()
            sh """echo "ARTIFACT_LIST=${ARTIFACT_LIST}" >> ./parameters.properties"""

            archiveArtifacts allowEmptyArchive: true, artifacts: '*.tar.bz2', onlyIfSuccessful: false
            archiveArtifacts allowEmptyArchive: true, artifacts: 'parameters.properties', onlyIfSuccessful: false
            sh "rm -f ./*.tar.bz2"
            script
            {
                if ( fileExists("/proj/DSC_CI/jenkins/tools/help_scripts/concurrentBuilds.sh") )
                {
					sh "/proj/DSC_CI/jenkins/tools/help_scripts/concurrentBuilds.sh -stop"
				} // if
			} // script
            cleanWs()
        }
        failure
        {
            script
            {
                if ("${env.MAIL}" != 'null')
                {
                    def emailbody_job_failure_1="\nthe job for automatic cleanup of an existing deployment from jenkins on namespace ${DAFT_NS} on the IaaS node ${IAAS_NODE} has failed."
                    def emailbody_job_failure_2="\n\nDetailed logs for the overall deployment jenkins job can be found under:\n${LOGS_LINK}/$BUILD_NUMBER/consoleFull"

                    emailext body: "${emailbody_start}"+"${emailbody_job_failure_1}"+"${emailbody_job_failure_2}"+"${emailbody_end}",
                    subject: "Automatic IaaS undeployment (${RELEASE_TYPE_TO_UNDEPLOY}) failed", to: "${env.MAIL}"
                }
            }
        }
    }
}
def getData()
{
// Assign paths to the logs
    script
    {
        LOGS_LINK = """${sh( returnStdout: true,
        script: "echo ${JENKINS_INSTANCE}/${JOB_BASE_NAME}")}""".trim()

// Assign director-0 ip address
        sh "./scripts/bash/get_directors_ip.bash ${IAAS_NODE}"

        NODE_DIRECTOR_IP = """${sh( returnStdout: true, script: "cat ./var.DIRECTOR-0-IP")}""".trim()

// get appropriate settings for calling the nw config file

        switch (RELEASE_TYPE_TO_UNDEPLOY)
        {
            case [ 'EVNFM' ]:
                script
                {
                    NW_CONFIG_OPTION="--network-config-option=EVNFM"
                    VALUE_NS="value"
                    ACTION="Terminate"
                }
                break
            case [ 'cnDSC' ]:
                script
                {
                    NW_CONFIG_OPTION="--network-config-option=DSC"
                    VALUE_NS="value_DSC"
                    ACTION="Undeploy"
                }
                break
            default:
                script
                {
                    NW_CONFIG_OPTION=""
                    VALUE_NS="value"
                    ACTION="Undeploy"
                }
                break
        } //switch


// get the nw config file
        sh """  if [ '${env.NW_CONFIG_FILE}' = 'null' ];
        then
            echo "${NW_CONFIG_FILE_DIR}/${IAAS_NODE}.xml"  > ./var.nw-config-file
        else
            echo "${env.NW_CONFIG_FILE}"  > ./var.nw-config-file
        fi;"""

        NW_CONFIG_FILE_FULL_PATH = """${sh( returnStdout: true, script: "cat ./var.nw-config-file")}""".trim()
        NW_CONFIG_FILE_NAME = """${sh( returnStdout: true, script: "cat ./var.nw-config-file | sed 's?^.*/??'")}""".trim()
        if (fileExists("${NW_CONFIG_FILE_FULL_PATH}"))
        {
            DAFT_NS = """${sh( returnStdout: true, script: "sed -n '/<parameter name=\"sc_namespace\"/,/>/p' ${NW_CONFIG_FILE_FULL_PATH} | grep ${VALUE_NS}= | cut -d '\"\' -f2")}""".trim()
        }
        else
        {
            echo "The file ${NW_CONFIG_FILE_FULL_PATH} couldn't be read or found. If the file exists please provide at least read permissions for the 'Others Rights group' for the whole path chain up to including the file."
            exit 1
        }
// The NS is stored at ${DAFT_NS}.

        if ("${env.ALTERNATIVE_NAMESPACE}" == 'null')
        {
            ALTERNATIVE_NS =""
        }
        else
        {
            ALTERNATIVE_NS ="-v SC_NAMESPACE=${ALTERNATIVE_NAMESPACE}"
            DAFT_NS = "${ALTERNATIVE_NAMESPACE}"
        }
        DAFT_NS_DIR = "/home/eccd/daft_ns_${DAFT_NS}"
        DAFT_NS_DIR_ESCAPED = "\\/home\\/eccd\\/daft_ns_${DAFT_NS}"
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
def artifactList()
{
    script
    {
        sh """sed 's/\$/ /g' ./names_array_split.txt > ./names_array_split_blanks.txt"""
        sh """cat ./names_array_split_blanks.txt | tr -d '\n' > ./content_oneline_blanks.txt"""
        ARTIFACT_LIST = """${sh( returnStdout: true, script: "cat ./content_oneline_blanks.txt")}""".trim()
    }
}
def unpackADPfiles(f)
{
    script
    {
        while ( f >0 )
        {// start from the higher value and continue decreasing
            ADP_FOLDER_NAME = """${sh(returnStdout: true, script: "ls *${JOB_BASE_NAME}-${BUILD_NUMBER}*.tar.bz2 | grep troubleshooting_logs | head -$f | tail -1")}""".trim()
        //unpack the .tgz files
            sh """tar xf ${ADP_FOLDER_NAME} 2>/dev/null || true"""
            echo "unpacking file ${ADP_FOLDER_NAME}"
        // decrease the list to go to the previous element
            f = f-1
        }
        sh """mv troubleshooting_logs/*.tgz . 2>/dev/null || true"""
        sh """ls *.tgz >> ./names_array_split.txt 2>/dev/null || true"""
        archiveArtifacts allowEmptyArchive: true, artifacts: '*.tgz', onlyIfSuccessful: false
        sh "rm -f ./*.tgz"
        sh "rm -f ./*troubleshooting_logs.tar.bz2"
        sh "rm -rf ./troubleshooting_logs"
    }
}
def rewoveLabel(THIS_LABEL)
{
    script
    {
        sh "${DAFT_DIR}/expect/bin/send_command_to_ssh.exp --timeout=1800 --user=eccd --ip=${NODE_DIRECTOR_IP} --password='notneeded' --stop-on-error \
                --command='perl ${DAFT_NS_DIR}/perl/bin/execute_playlist.pl -p 103_Tools_Management -c no -v DO_REMOVE_TAINT=yes -v DO_REMOVE_LABEL=yes \
                -v NODE_TAINTS=NoSchedule -v NODE_LABEL=usage=${THIS_LABEL} -j ${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}_Remove_Node_Label_${THIS_LABEL} \
                -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=yes -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=no -v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=yes -v INPUT_AVAILABLE=no -w ${DAFT_LOGS_DIR}' "
    }
}