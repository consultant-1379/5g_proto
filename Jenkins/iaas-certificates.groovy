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
    environment
    {
        IAAS_NODE = "${IAAS_NODE}"
        NW_CONFIG_FILE = "${NW_CONFIG_FILE}"
        INTERNAL_CERTIFICATE_RENEWAL_RATIO = "${INTERNAL_CERTIFICATE_RENEWAL_RATIO}"
        INTERNAL_CERTIFICATE_TTL = "${INTERNAL_CERTIFICATE_TTL}"
        MAIL = "${MAIL}"
        DAFT_DIR = "./daft"
        NW_CONFIG_FILE_DIR = "${DAFT_DIR}/network_config_files"
        DAFT_LOGS_DIR = "/home/eccd/workspaces"
        JENKINS_INSTANCE = "https://fem1s10-eiffel029.eiffel.gic.ericsson.se:8443/jenkins/job/5G-ESC/job/IaaS/job"
        JENKINS_INSTANCE_NEW = "https://fem3s10-eiffel029.eiffel.gic.ericsson.se:8443/jenkins/job/5G-ESC/job/IaaS/job"
    }
    stages
    {
        stage('Get needed info')
        {
            steps
            {
                script
                {
                    if ( fileExists("/proj/DSC_CI/jenkins/tools/help_scripts/concurrentBuilds.sh") )
                    {
						// Log current job
						sh "/proj/DSC_CI/jenkins/tools/help_scripts/concurrentBuilds.sh -start"
					} // if
                    // Assign paths to the logs
                    LOGS_LINK = """${sh( returnStdout: true,
                    script: "echo ${JENKINS_INSTANCE}/${JOB_BASE_NAME}")}""".trim()

                    // Assign director-0 ip address
                    sh "./scripts/bash/get_directors_ip.bash ${IAAS_NODE}"

                    NODE_DIRECTOR_IP = """${sh( returnStdout: true, script: "cat ./var.DIRECTOR-0-IP")}""".trim()

                    // get the nw config file
                    sh """  if [ '${env.NW_CONFIG_FILE}' = 'null' ];
                        then
                            echo "${NW_CONFIG_FILE_DIR}/${IAAS_NODE}.xml"  > ./var.nw-config-file
                        else
                            echo "${env.NW_CONFIG_FILE}"  > ./var.nw-config-file
                        fi;"""

                    NW_CONFIG_FILE_FULL_PATH = """${sh( returnStdout: true, script: "cat ./var.nw-config-file")}""".trim()
                    NW_CONFIG_FILE_NAME = """${sh( returnStdout: true, script: "cat ./var.nw-config-file | sed 's?^.*/??'")}""".trim()
                    VALUE_NS = "value" /* same for SC or EVNFM deployment. In case cnDSC would be triggered then the logic via "RELEASE_TYPE_DEPLOYED" (as in the other scripts) have to be used */
              	    if (fileExists("${NW_CONFIG_FILE_FULL_PATH}"))
                    {
                        DAFT_NS = """${sh( returnStdout: true, script: "sed -n '/<parameter name=\"sc_namespace\"/,/>/p' ${NW_CONFIG_FILE_FULL_PATH} | grep ${VALUE_NS}= | cut -d '\"\' -f2")}""".trim()
                    }
                    else
                    {
                    	echo "The file ${NW_CONFIG_FILE_FULL_PATH} couldn't be read or found. If the file exists please provide at least read permissions for the 'Others Rights group' for the whole path chain up to including the file."
                    	exit 1
                    }
                    DAFT_NS_DIR = "/home/eccd/daft_ns_${DAFT_NS}-TTL-certificates"
                    TTL_RESULT = "<not_executed>" // initialization
                    sh """echo "parameters.properties"    > ./names_array_split.txt"""
                }
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
        stage('Trigger internal TTL certificates')
        {
            steps
            {
                script
                {
                    echo 'Trigger internal TTL certificates'
                    sh "${DAFT_DIR}/expect/bin/send_command_to_ssh.exp --timeout=3600  --user=eccd --ip=${NODE_DIRECTOR_IP} --password='notneeded' \
                    --stop-on-error --command='perl  ${DAFT_NS_DIR}/perl/bin/execute_playlist.pl -p 807_Reduced_TTL -c no -n ${DAFT_NS_DIR}/${NW_CONFIG_FILE_NAME} \
                    -v INTERNAL_CERTIFICATE_RENEWAL_RATIO=${INTERNAL_CERTIFICATE_RENEWAL_RATIO} -v INTERNAL_CERTIFICATE_TTL=${INTERNAL_CERTIFICATE_TTL} \
                    -j ${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER} -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=yes -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=yes \
                    -v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=yes -v INPUT_AVAILABLE=no -w ${DAFT_LOGS_DIR} '"
                } //script
            }
            post
            {
                always
                {
                    script
                    {
                        LOGS_FOLDER_CERTIFICATES = """${sh( returnStdout: true,
                            script: "ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP} 'find ${DAFT_LOGS_DIR} -type d -name *${JOB_BASE_NAME}-${BUILD_NUMBER}*'")}""".trim()
                    }
                }
                failure
                {
                    script
                    {
                        TTL_RESULT = "FAILURE-AT-EXECUTION"
                        if ("${env.MAIL}" != 'null')
                        {
                            def emailbody_certif_failure_1="\nthe automatic triggering of internal TTL certificates via DAFT"
                            def emailbody_certif_failure_2=" on node ${IAAS_NODE} has failed.\n\nDetailed logs can be found on the"
                            def emailbody_certif_failure_3=" director of node ${IAAS_NODE} (IP address of director: ${NODE_DIRECTOR_IP}) under the folder: ${LOGS_FOLDER_CERTIFICATES}"

                            emailext body: "${emailbody_start}"+"${emailbody_certif_failure_1}"+"${emailbody_certif_failure_2}"+"${emailbody_certif_failure_3}"+"${emailbody_end}",
                            subject: "Automatic IaaS execution for internal TTL certificates failed", to: "${env.MAIL}"
                        }
                    }
                }
            }
        }
        stage('Evaluate KPI')
        {
            steps
            {
                script
                {
                    FAIL_JOB=true
                    SEARCH_STRING = "Overall KPI Verdict was successful"
                    prepareExitReport()
                    scanError(LOGS_FOLDER_CERTIFICATES)
                    if (FAIL_JOB)
                    {
                        TTL_RESULT = "FAILURE-AT-EXIT_VERDICT"
                        if ("${env.MAIL}" != 'null')
                        {
                            def emailbody_exc_failure_1="\nthe exit verdict for the automatic triggering of internal TTL certificates via DAFT on node ${IAAS_NODE} has failed."
                            def emailbody_exc_failure_2="\n\nDetailed logs can be found on the director of node ${IAAS_NODE} (IP address of director:"
                            def emailbody_exc_failure_3=" ${NODE_DIRECTOR_IP}) under the folder: ${LOGS_FOLDER_CERTIFICATES}"

                            emailext body: "${emailbody_start}"+"${emailbody_exc_failure_1}"+"${emailbody_exc_failure_2}"+"${emailbody_exc_failure_3}"+"${emailbody_end}",
                            subject: "Automatic Exit verdict for internal TTL certificates TC failed", to: "${MAIL}"
                        }
                        error('Jenkins job will report failure to Spinnaker as the exit verdict was unsuccessful.')
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
                    TTL_RESULT = "SUCCESS"
                    if ("${env.MAIL}" != 'null')
                    {
                        def emailbody_job_finished_1="\nthe automatic triggering of internal TTL certificates via DAFT on node ${IAAS_NODE} has concluded."
                        def emailbody_job_finished_2="\n\nDetailed logs can be found on the director of node ${IAAS_NODE} (IP address of director:"
                        def emailbody_job_finished_3=" ${NODE_DIRECTOR_IP}) under the folder: ${LOGS_FOLDER_CERTIFICATES} \nDetailed logs for the overall"
                        def emailbody_job_finished_4=" jenkins job can be found under: ${LOGS_LINK}/$BUILD_NUMBER/consoleFull"

                        emailext body: "${emailbody_start}"+"${emailbody_job_finished_1}"+"${emailbody_job_finished_2}"+
                        "${emailbody_job_finished_3}"+"${emailbody_job_finished_4}"+"${emailbody_end}",
                        subject: "Automatic IaaS internal TTL certificates concluded", to: "${env.MAIL}"
                    }
                }
            }
        }
    }
    post
    {
        always
        {
            sh "scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP}:${DAFT_LOGS_DIR}/*${JOB_BASE_NAME}-${BUILD_NUMBER}*.tar.bz2 ./. 2>/dev/null || true"
            sh """echo "TTL_RESULT=${TTL_RESULT}" > ./parameters.properties"""

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
                    def emailbody_job_failure_1="\nthe job for executing internal TTL certificates"
                    def emailbody_job_failure_2=" on the IaaS node ${IAAS_NODE} has failed.\n\nDetailed logs for the overall job can be"
                    def emailbody_job_failure_3=" found under: ${LOGS_LINK}/$BUILD_NUMBER/consoleFull"

                    emailext body: "${emailbody_start}"+"${emailbody_job_failure_1}"+"${emailbody_job_failure_2}"+"${emailbody_job_failure_3}"+"${emailbody_end}",
                    subject: "Automatic IaaS internal TTL certificates failed", to: "${env.MAIL}"
                }
            }
        }
    }
}
def uploadAndCheckExpect () {

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
def scanError(LOGS_FOLDER_TMP)
{
    script
    {
        OUTCOME_STRING = """${sh( returnStdout: true,
                            script: """ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP} "cat ${LOGS_FOLDER_TMP}/summary.txt | grep '${SEARCH_STRING}'" 2>/dev/null || true""")}""".trim()

        if ("${OUTCOME_STRING}".contains("${SEARCH_STRING}"))
        {
            FAIL_JOB=false
        }
    }
}
def prepareExitReport()
{
    sh "scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP}:${LOGS_FOLDER_CERTIFICATES}/logfiles/kpi_statistics_*/kpi_verdict_summary.txt ./${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}_exit_verdict.txt 2>/dev/null || true"
    sh """ls *${BUILD_NUMBER}_exit_verdict.txt >> ./names_array_split.txt 2>/dev/null || true"""

    archiveArtifacts allowEmptyArchive: true, artifacts: "*${BUILD_NUMBER}_exit_verdict.txt", onlyIfSuccessful: false
}