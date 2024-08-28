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
//      Variables common to all Robustness TCs
        IAAS_NODE = "${IAAS_NODE}"
/* former input params
        NAMESPACE = "${NAMESPACE}"
        RELEASE_NAME = "${RELEASE_NAME}"
 */
        NW_CONFIG_FILE = "${NW_CONFIG_FILE}"
        RELEASE_TYPE_DEPLOYED = "${RELEASE_TYPE_DEPLOYED}"
        MAIL = "${MAIL}"
//      Variables used in 301_Robustness_Test_Timezone_Change
        TIMEZONES = "${TIMEZONES}"
        CSAR_PKG_FOLDER = "${CSAR_PKG_FOLDER}"
        SKIP_RESTORE_TIMEZONE = "${SKIP_RESTORE_TIMEZONE}"
//      Variables used in 302_Robustness_Test_K8s_Master_Restart, 303_Robustness_Test_K8s_Worker_Restart, 304_Robustness_Test_K8s_Worker_Drain
        TIMEOUT_NODE_REACHABLE = "${TIMEOUT_NODE_REACHABLE}"
//      Variables used in 305_Robustness_Test_Moderate_Network_Impairment
        TOOLS_NAMESPACE = "${TOOLS_NAMESPACE}"
        IMPAIRMENT_DELAY_VALUES = "${IMPAIRMENT_DELAY_VALUES}"
        WAIT_TIME_BETWEEN_IMPAIRMENTS = "${WAIT_TIME_BETWEEN_IMPAIRMENTS}"
//      Variables used in 403_Maintainability_Test_Scaling
        CNF_TYPE = "${CNF_TYPE}"
        SCALE_INCREMENT = "${SCALE_INCREMENT}" //after EVNFM LCM based introduction this parameter is not available in the initial form
        WAIT_TIME_BETWEEN_SCALE_OPERATIONS = "${WAIT_TIME_BETWEEN_SCALE_OPERATIONS}"
        DEPLOYMENT_VARIANT ="${DEPLOYMENT_VARIANT}"
//      common variables
        ENTRY_VERDICT = "${ENTRY_VERDICT}"
        ENTRY_MIN_THRESHOLD = "${ENTRY_MIN_THRESHOLD}"
//      Auxiliary variables
        DAFT_DIR = "./daft"
        NW_CONFIG_FILE_DIR = "${DAFT_DIR}/network_config_files"
        DAFT_LOGS_DIR = "/home/eccd/workspaces"
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
                    OWN_WORKSPACE = """${sh( returnStdout: true,
                    script: "pwd | cut -d '/' -f7")}""".trim()
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
                sh "./scripts/bash/get_directors_ip.bash ${IAAS_NODE}"
                // Assign paths to the logs
                script
                {
                    LOGS_LINK = """${sh( returnStdout: true,
                    script: "echo ${JENKINS_INSTANCE}/${JOB_BASE_NAME}")}""".trim()

                    // Assign director-0 ip address
                    NODE_DIRECTOR_IP = """${sh( returnStdout: true,
                    script: "cat ./var.DIRECTOR-0-IP")}""".trim()
// The following variables only get values for scaling
                    SCALE_INCREMENT ="" // 1 for the first iteration and 2 for the second (multiple)
                    DAFT_WS_APPEND ="" // "_multiple"
                    LOGS_FOLDER_ROBUSTNESS_ITERATION_1 =""

// Get the concretely triggered ROBUSTNESS_TC (values: TZ_change, pod_disturbance, master_restart, worker_restart, worker_drain, nw_impairment) or scale from Maintanibilty TCs
                    ROBUSTNESS_TC = """${sh( returnStdout: true, script: "echo ${JOB_BASE_NAME} | sed 's/IaaS-robustness-//' | sed 's/IaaS-//'")}""".trim()

                    if ("${ROBUSTNESS_TC}" == 'scale')
                    {
                        SLOGAN = "maintainability scale out/scale in"
                    }
                    else
                    {
                        SLOGAN = "robustness ${ROBUSTNESS_TC}"
                    }
                    echo "This job will execute the ${SLOGAN} TC."

// create file for artifact list
                    sh """echo "RTC-${ROBUSTNESS_TC}-result.properties"   > ./names_array_split.txt"""

                    // get appropriate settings for calling the nw config file

                    switch (RELEASE_TYPE_DEPLOYED)
                    {
                        case [ 'EVNFM' ]:
                            script
                            {
                                NW_CONFIG_OPTION=""
                                VALUE_NS="value"
                            }
                            break
                        case [ 'cnDSC' ]:
                            script
                            {
                                NW_CONFIG_OPTION="--network-config-option=DSC"
                                VALUE_NS="value_DSC"
                            }
                            break
                        default:
                            script
                            {
                                NW_CONFIG_OPTION=""
                                VALUE_NS="value"
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
                        NAMESPACE = """${sh( returnStdout: true, script: "sed -n '/<parameter name=\"sc_namespace\"/,/>/p' ${NW_CONFIG_FILE_FULL_PATH} | grep ${VALUE_NS}= | cut -d '\"\' -f2")}""".trim()
                    }
                    else
                    {
                        echo "The file ${NW_CONFIG_FILE_FULL_PATH} couldn't be read or found. If the file exists please provide at least read permissions for the 'Others Rights group' for the whole path chain up to including the file."
                        exit 1
                    }
// For the TZ_change & scale TCs, the SW directory parameter is mandatory
                    if ((("${ROBUSTNESS_TC}" == 'TZ_change') || (  ("${ROBUSTNESS_TC}" == 'scale') && ("${env.DEPLOYMENT_VARIANT}" != 'EVNFM') )  )&& ("${env.CSAR_PKG_FOLDER}" == 'null'))
                    {
                        error("For the ${SLOGAN} TC the variable CSAR_PKG_FOLDER must contain a value. Execution stopped!")
                    }
                    DAFT_NS_DIR = "/home/eccd/daft_ns_${NAMESPACE}-robustness-${ROBUSTNESS_TC}"

                    RTC_RESULT = "<not_executed>" // initialization
                    TRAFFIC = "without" // default value is to run w/o traffic. Otherwise internal logic will mark it to value "with". It is needed for mail notification details
                } //script
            }
        }
        stage('Create & transfer files to IaaS node')
        {
            steps            {
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
        stage('Prepare playlist input')
        {
            steps
            {
                echo 'Prepare the specific input for each playlist'
                script
                {
                     switch (ROBUSTNESS_TC)
                    {
                        case [ 'TZ_change', 'scale' ]:
                            getDataPL_301_403()
                            break
                        case ['master_restart', 'worker_restart', 'worker_drain']:
                            getDataPL_302_303_304()
                            break
                        case [ 'nw_impairment' ]:
                            getDataPL_305()
                            break
                           case [ 'pod_disturbance' ]:
                            getDataPL_306()
                            break
                        default:
                            error("There is no playlist handling the ${SLOGAN} TC. Execution stopped!")
                            break
                    } //switch
                } //script
            }
        }
        stage('Perform entry verdict if required')
        {
           when { not { environment name: 'ENTRY_VERDICT', value: 'none' } }
           steps
           {
               script
               {
                   if ("${ROBUSTNESS_TC}" == 'scale')
                   {
                       KPI_FOR_CNF = "${CNF_TYPE}" + ".+"
                   }
                   else
                   {
                       KPI_FOR_CNF = """${sh( returnStdout: true, script: "echo '${ENTRY_VERDICT}' | cut -d '_' -f4")}""".trim()
                       KPI_FOR_CNF = "(" + KPI_FOR_CNF + ").+"
                   }
                   TRAFFIC = "with"
               }
               sh """${DAFT_DIR}/expect/bin/send_command_to_ssh.exp --timeout=3600  --user=eccd --ip=${NODE_DIRECTOR_IP} --password='notneeded' \
               --stop-on-error --command='perl ${DAFT_NS_DIR}/perl/bin/system_health_check.pl -c kpi_success_rate -n ${NAMESPACE} -v kpi_success_rate_description=\"${KPI_FOR_CNF}\" \
               -v kpi_details_at_success=1 -v kpi_future=5m -v kpi_success_rate_threshold=${ENTRY_MIN_THRESHOLD} --no_color \
               --progress_type=none -f ${DAFT_LOGS_DIR}/${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}_Entry-Verdict-Details.log > ${DAFT_LOGS_DIR}/${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}_Entry-Verdict-Report.log'"""
// "tee" removed as otherwise no error is prompted when entry-criteria fails                --progress_type=none -f ${DAFT_LOGS_DIR}/${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}_Entry-Verdict-Details.log | tee ${DAFT_LOGS_DIR}/${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}_Entry-Verdict-Report.log'"""

           }
           post
           {
               always
               {
                   script
                   {
                        sh "scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP}:${DAFT_LOGS_DIR}/*${JOB_BASE_NAME}-${BUILD_NUMBER}_Entry-Verdict-Report.log ./. 2>/dev/null || true"
                        sh """sed -i '1s/^/Entry criteria checked: -c kpi_success_rate  -v kpi_success_rate_description=\\"${KPI_FOR_CNF}\\" -v kpi_future=5m -v kpi_success_rate_threshold=${ENTRY_MIN_THRESHOLD}\\n\\n\\n/' *_Entry-Verdict-Report.log"""
                        sh """ls *_Entry-Verdict-Report.log >> ./names_array_split.txt 2>/dev/null || true"""

                       archiveArtifacts allowEmptyArchive: true, artifacts: '*_Entry-Verdict-Report.log', onlyIfSuccessful: false
                   }
               }
               failure
               {
                   script
                   {
                       RTC_RESULT = "FAILURE-AT-ENTRY_VERDICT"
                       if ("${env.MAIL}" != 'null')
                       {
                           def emailbody_ec_failure_1="\nthe entry verdict for the ${SLOGAN} TC via DAFT on node ${IAAS_NODE} has failed.\n\nDetailed logs regarding"
                           def emailbody_ec_failure_2=" the check criteria case can be found the director of node ${IAAS_NODE} (IP address of director: ${NODE_DIRECTOR_IP}) under the file:"
                           def emailbody_ec_failure_3=" ${DAFT_LOGS_DIR}/${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}_Entry-Verdict-Report.log"

                           emailext body: "${emailbody_start}"+"${emailbody_ec_failure_1}"+"${emailbody_ec_failure_2}"+"${emailbody_ec_failure_3}"+"${emailbody_end}",
                           subject: "Automatic Entry verdict for a ${SLOGAN} TC failed", to: "${MAIL}"
                       }
                       // Next, ADP logs will be collected for trouble shooting
                       sh """echo "As Entry verdict failed, ADP logs will be collected now for trouble shooting." """
                       CURRENT_DIR = "${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}"
                       CURRENT_NS = "${NAMESPACE}"
                       CURRENT_FILE_START_NAME = CURRENT_DIR + "_Entry-Verdict-ADP"
                       ADPfileHandling(CURRENT_DIR, CURRENT_NS, CURRENT_FILE_START_NAME)
                    }
                   error ("Entry verdict has failed and job will be stopped.")
               }
           }
        }
        stage('Trigger the automated TC')
        {
            steps
            {
                script
                {
                    if ("${TRAFFIC}" == 'with')
                    {
                        KPI_REQUEST = "-v KPI_SUCCESS_RATE_DESCRIPTION=\"" + KPI_FOR_CNF + "\"" + " -v KPI_SUCCESS_RATE_THRESHOLD=${ENTRY_MIN_THRESHOLD}"
                    }
                    else
                    {
                        KPI_REQUEST = "-v SKIP_KPI_VERDICT_CHECK=yes"
                    }
                    if ("${ROBUSTNESS_TC}" == 'scale')
                    {
                        SCALE_INCREMENT ="1"
                    }
                }
                echo "Triggering the playlist ${PLAYLIST_NAME}"
                callPlaylist()
            }
            post
            {
                always
                {
                    script
                    {
                        LOGS_FOLDER_ROBUSTNESS = getFolderRobustness()
                        if ("${ROBUSTNESS_TC}" == 'scale')
                        {
                            LOGS_FOLDER_ROBUSTNESS_ITERATION_1 = LOGS_FOLDER_ROBUSTNESS
                        }
                    }
                }
                success
                {
                    script
                    {
                        if ("${TRAFFIC}" == 'with')
                        {
                            prepareExitReport()
                            if ("${ROBUSTNESS_TC}" == 'scale')
                            {
                                moveReportToFolder(DAFT_WS_APPEND, LOGS_FOLDER_ROBUSTNESS)
                            }
                        }
                    }
                }
                failure
                {
                    script
                    {
                        RTC_RESULT = "FAILURE-AT-EXECUTION"

                        if ("${TRAFFIC}" == 'with')
                        {
                            moveReportToFolder(DAFT_WS_APPEND, LOGS_FOLDER_ROBUSTNESS)
                        }
                        if ("${env.MAIL}" != 'null')
                        {
                            def emailbody_tc_failure_1="\nthe ${SLOGAN} TC via DAFT on node ${IAAS_NODE} has failed.\n\nDetailed logs regarding the test case can be found"
                            def emailbody_tc_failure_2=" on the director of node ${IAAS_NODE} (IP address of director: ${NODE_DIRECTOR_IP}) under the folder: ${LOGS_FOLDER_ROBUSTNESS}"
                            emailext body: "${emailbody_start}"+"${emailbody_tc_failure_1}"+"${emailbody_tc_failure_2}"+"${emailbody_end}",
                            subject: "Automatic IaaS execution for a ${SLOGAN} TC failed", to: "${MAIL}"
                        }
                    }
                }
            }
        }
        stage('Trigger multiple scaling')
        {
            when {expression {ROBUSTNESS_TC == "scale"}}
            steps
            {
                script
                {
                    SCALE_INCREMENT ="2"
                    DAFT_WS_APPEND = "_multiple"
                }
                echo "Triggering the playlist ${PLAYLIST_NAME} multiple scaling"
                callPlaylist()
            }
            post
            {
                always
                {
                    script
                    {
                        LOGS_FOLDER_ROBUSTNESS = getFolderRobustness()
                    }
                }
                success
                {
                    script
                    {
                        if ("${TRAFFIC}" == 'with')
                        {
                            prepareExitReport()
                        }
                    }
                }
                failure
                {
                    script
                    {
                        RTC_RESULT = "FAILURE-AT-EXECUTION"

                        if ("${TRAFFIC}" == 'with')
                        {
                            moveReportToFolder(DAFT_WS_APPEND, LOGS_FOLDER_ROBUSTNESS)
                        }
                        if ("${env.MAIL}" != 'null')
                        {
                            def emailbody_tc_failure_1="\nthe ${SLOGAN} TC via DAFT on node ${IAAS_NODE} has failed.\n\nDetailed logs regarding the test case can be found"
                            def emailbody_tc_failure_2=" on the director of node ${IAAS_NODE} (IP address of director: ${NODE_DIRECTOR_IP}) under the folder: ${LOGS_FOLDER_ROBUSTNESS}"
                            emailext body: "${emailbody_start}"+"${emailbody_tc_failure_1}"+"${emailbody_tc_failure_2}"+"${emailbody_end}",
                            subject: "Automatic IaaS execution for a ${SLOGAN} TC failed", to: "${MAIL}"
                        }
                    }
                }
            }
        }
        stage('Evaluate KPI')
        {
            when
            {
                expression { TRAFFIC=="with"}
            }
            steps
            {
                script
                {
                    FAIL_JOB=false
                    SEARCH_STRING = "Overall KPI Verdict was unsuccessful"
                    moveReportToFolder(DAFT_WS_APPEND, LOGS_FOLDER_ROBUSTNESS)
                    scanError(LOGS_FOLDER_ROBUSTNESS)
                    if ("${ROBUSTNESS_TC}" == 'scale') //scan for unsuccessful KPI on single scaling
                    {
                        scanError(LOGS_FOLDER_ROBUSTNESS_ITERATION_1)
                    }

                    if (FAIL_JOB)
                    {
                        RTC_RESULT = "FAILURE-AT-EXIT_VERDICT"
                        if ("${env.MAIL}" != 'null')
                        {
                            if ("${ROBUSTNESS_TC}" == 'scale')
                            {
                                emailbody_exc_failure_4 = " and ${LOGS_FOLDER_ROBUSTNESS_ITERATION_1}/logfiles/kpi_verdict.log"
                            }
                            else
                            {
                                emailbody_exc_failure_4 = ""
                            }
                            def emailbody_exc_failure_1="\nthe exit verdict for the ${SLOGAN} TC via DAFT on node ${IAAS_NODE} has failed.\n\nDetailed logs regarding"
                            def emailbody_exc_failure_2=" the check criteria case can be found the director of node ${IAAS_NODE} (IP address of director: ${NODE_DIRECTOR_IP}) under the file(s):"
                            def emailbody_exc_failure_3=" ${LOGS_FOLDER_ROBUSTNESS}/logfiles/kpi_verdict.log"

                            emailext body: "${emailbody_start}"+"${emailbody_exc_failure_1}"+"${emailbody_exc_failure_2}"+"${emailbody_exc_failure_3}"+"${emailbody_exc_failure_4}"+"${emailbody_end}",
                            subject: "Automatic Exit verdict for a ${SLOGAN} TC failed", to: "${MAIL}"
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
                    RTC_RESULT = "SUCCESS"
                    if ("${env.MAIL}" != 'null')
                    {
                        if ("${ROBUSTNESS_TC}" == 'scale')
                        {
                            emailbody_job_finished_3 = " and ${LOGS_FOLDER_ROBUSTNESS_ITERATION_1}"
                        }
                        else
                        {
                            emailbody_job_finished_3 = ""
                        }
                        def emailbody_job_finished_1="\nthe automatic ${SLOGAN} TC via DAFT on node ${IAAS_NODE} run ${TRAFFIC} traffic and has concluded.\n\nDetailed logs regarding the test case can be found on the"
                        def emailbody_job_finished_2=" director of node ${IAAS_NODE} (IP address of director: ${NODE_DIRECTOR_IP}) under the folder(s): ${LOGS_FOLDER_ROBUSTNESS}"
                        def emailbody_job_finished_4="\nDetailed logs for the overall ${SLOGAN} jenkins job can be found under: ${LOGS_LINK}/$BUILD_NUMBER/consoleFull"
                        emailext body: "${emailbody_start}"+"${emailbody_job_finished_1}"+"${emailbody_job_finished_2}"+
                        "${emailbody_job_finished_3}"+"${emailbody_job_finished_4}"+"${emailbody_end}",
                        subject: "Automatic IaaS execution for a ${SLOGAN} TC concluded", to: "${MAIL}"
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
            sh """ls *${JOB_BASE_NAME}-${BUILD_NUMBER}*.tar.bz2 | grep -v troubleshooting_logs >> ./names_array_split.txt 2>/dev/null || true"""
            sh """echo "RTC_RESULT=${RTC_RESULT}"        > ./RTC-${ROBUSTNESS_TC}-result.properties"""
            script
            {
                NUMBER_ADP_GROUPS = """${sh( returnStdout: true, script:""" ls *${JOB_BASE_NAME}-${BUILD_NUMBER}*.tar.bz2 | grep troubleshooting_logs | wc -l 2>/dev/null || true""")}""".trim()

                if ("${NUMBER_ADP_GROUPS}".toInteger() !=0)
                {
                unpackADPfiles("${NUMBER_ADP_GROUPS}".toInteger())
                }
            }
            script
            {
                if ( fileExists("/proj/DSC_CI/jenkins/tools/help_scripts/concurrentBuilds.sh") )
                {
					sh "/proj/DSC_CI/jenkins/tools/help_scripts/concurrentBuilds.sh -stop"
				} // if
                if (("${ENTRY_VERDICT}" != "none") && ("${RTC_RESULT}" != "SUCCESS")) // For unsuccessful traffic cases trigger ADP logs from tools namespace
                {
                    CURRENT_DIR = "${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}_Tools"
                    CURRENT_NS = "${NAMESPACE}-tools"
                    CURRENT_FILE_START_NAME = CURRENT_DIR + "-ADP"
                    ADPfileHandling(CURRENT_DIR, CURRENT_NS, CURRENT_FILE_START_NAME)
                }
            } // script
            artifactList()
            sh """echo "ARTIFACT_LIST=${ARTIFACT_LIST}" >> ./RTC-${ROBUSTNESS_TC}-result.properties"""

            archiveArtifacts allowEmptyArchive: true, artifacts: "RTC-${ROBUSTNESS_TC}-result.properties", onlyIfSuccessful: false
            archiveArtifacts allowEmptyArchive: true, artifacts: '*.tar.bz2', onlyIfSuccessful: false

            sh "rm -f ./*.tar.bz2"

            cleanWs()
        }
        failure
        {
            script
            {
                if ("${env.MAIL}" != 'null')
                {
                    def emailbody_job_failure_1="\nthe job for executing the ${SLOGAN} TC via DAFT on the IaaS node ${IAAS_NODE} run ${TRAFFIC} traffic and got ${RTC_RESULT}.\n\n"
                    def emailbody_job_failure_2="Detailed logs for the overall jenkins job can be found under: ${LOGS_LINK}/$BUILD_NUMBER/consoleFull"
                    emailext body: "${emailbody_start}"+"${emailbody_job_failure_1}"+"${emailbody_job_failure_2}"+"${emailbody_end}",
                    subject: "Automatic IaaS job for a ${SLOGAN} TC failed", to: "${MAIL}"
                }
            }
        }
    }
}//pipeline

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
def getDataPL_301_403()
{
    script
    {
        switch (ROBUSTNESS_TC)
        {
            case [ 'TZ_change' ]:
                PLAYLIST_NAME = "301_Robustness_Test_Timezone_Change"
                break
            case [ 'scale' ]:
                // To be filled distinguishing between DC/EVNFM scaling
                break
            default:
                error("This branch shouldn't have been executed. Execution stopped!")
                break
        } //switch

        if ("${env.DEPLOYMENT_VARIANT}" != 'EVNFM')
        {
            DIR_TYPE = """${sh( returnStdout: true, script: "echo ${CSAR_PKG_FOLDER} | cut -c1-1")}""".trim()

            if ("${DIR_TYPE}" == '/') // the full path is given for the software dir
            {
                PLAYLIST_PARAMETERS = "-v SOFTWARE_DIR=${CSAR_PKG_FOLDER} "
            }
            else // just the folder name is given for the software dir and it is supposed to be located under the standard location
            {
                PLAYLIST_PARAMETERS = "-v SOFTWARE_DIR=/home/eccd/download/${CSAR_PKG_FOLDER} "
            }
        }
        if ("${ROBUSTNESS_TC}" == 'TZ_change')
        {
            if ("${env.TIMEZONES}" != 'null')
            {
                PLAYLIST_PARAMETERS = PLAYLIST_PARAMETERS + "-v TIMEZONES=${TIMEZONES} "
            }
            if ("${env.SKIP_RESTORE_TIMEZONE}" != 'null')
            {
                PLAYLIST_PARAMETERS = PLAYLIST_PARAMETERS + "-v SKIP_RESTORE_TIMEZONE=${SKIP_RESTORE_TIMEZONE} "
            }
        }
        if ("${ROBUSTNESS_TC}" == 'scale')
        {
            // formerScaleLogic()
            if ("${DEPLOYMENT_VARIANT}" != 'EVNFM') //here no prefix env. is included as for this scenario DEPLOYMENT_VARIANT variable is filled in the initial form
            {
                PLAYLIST_NAME = "403_Maintainability_Test_Scaling"
                PLAYLIST_PARAMETERS = PLAYLIST_PARAMETERS + "-v SCALE_APPLICATION=${CNF_TYPE} "
                if ("${env.WAIT_TIME_BETWEEN_SCALE_OPERATIONS}" != 'null')
                {
                    PLAYLIST_PARAMETERS = PLAYLIST_PARAMETERS + "-v WAIT_TIME_BETWEEN_SCALE_OPERATIONS=${WAIT_TIME_BETWEEN_SCALE_OPERATIONS} "
                }
                PLAYLIST_PARAMETERS = PLAYLIST_PARAMETERS + "-v SCALE_INCREMENT="
            }
            else
            {
                PLAYLIST_NAME = "205_EVNFM_Scaling_SC"
                NF_ADAPTED = "${CNF_TYPE}".toLowerCase()
                PLAYLIST_PARAMETERS = "-v SCALING_ASPECT_ID=${NF_ADAPTED}_worker_scaling -v SCALING_TYPE=OUT_THEN_IN  -o EVNFM -v SCALING_STEPS="
            }
        }
    }
}
def getDataPL_302_303_304()
{
    script
    {
        switch (ROBUSTNESS_TC)
        {
            case [ 'master_restart' ]:
                PLAYLIST_NAME = "302_Robustness_Test_K8s_Master_Restart"
                break
            case [ 'worker_restart' ]:
                PLAYLIST_NAME = "303_Robustness_Test_K8s_Worker_Restart"
                break
            case [ 'worker_drain' ]:
                PLAYLIST_NAME = "304_Robustness_Test_K8s_Worker_Drain"
                break
            default:
                error("This branch shouldn't have been executed. Execution stopped!")
                break
        } //switch
        PLAYLIST_PARAMETERS =""
        if ("${env.TIMEOUT_NODE_REACHABLE}" != 'null')
        {
            PLAYLIST_PARAMETERS = PLAYLIST_PARAMETERS + "-v TIMEOUT_NODE_REACHABLE=${TIMEOUT_NODE_REACHABLE} "
        }
    } //script
}
def getDataPL_305()
{
    script
    {
        PLAYLIST_NAME = "305_Robustness_Test_Moderate_Network_Impairment"
        PLAYLIST_PARAMETERS ="-v TOOLS_NAMESPACE=${TOOLS_NAMESPACE}"

        if ("${env.IMPAIRMENT_DELAY_VALUES}" != 'null')
        {
            PLAYLIST_PARAMETERS = PLAYLIST_PARAMETERS + "-v IMPAIRMENT_DELAY_VALUES=${IMPAIRMENT_DELAY_VALUES} "
        }
        if ("${env.WAIT_TIME_BETWEEN_IMPAIRMENTS}" != 'null')
        {
            PLAYLIST_PARAMETERS = PLAYLIST_PARAMETERS + "-v WAIT_TIME_BETWEEN_IMPAIRMENTS=${WAIT_TIME_BETWEEN_IMPAIRMENTS} "
        }
    }
}
def getDataPL_306()
{
    script
    {
        PLAYLIST_NAME = "306_Robustness_Test_Pod_Disturbances"
        PLAYLIST_PARAMETERS =""
    } //script
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
def callPlaylist()
{
    sh "${DAFT_DIR}/expect/bin/send_command_to_ssh.exp --timeout=3600  --user=eccd --ip=${NODE_DIRECTOR_IP} --password='notneeded' \
    --stop-on-error --command='perl ${DAFT_NS_DIR}/perl/bin/execute_playlist.pl -p ${PLAYLIST_NAME} -n ${DAFT_NS_DIR}/${NW_CONFIG_FILE_NAME} ${NW_CONFIG_OPTION} \
    ${PLAYLIST_PARAMETERS}${SCALE_INCREMENT} -j ${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}${DAFT_WS_APPEND} -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=yes \
    -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=yes -v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=yes -v INPUT_AVAILABLE=no -c no -w ${DAFT_LOGS_DIR} ${KPI_REQUEST}'"
}
def getFolderRobustness()
{
    script
   {
        FOLDER_TMP = """${sh( returnStdout: true,
                script: "ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP} 'find ${DAFT_LOGS_DIR} -type d -name *${JOB_BASE_NAME}-${BUILD_NUMBER}${DAFT_WS_APPEND}*'")}""".trim()
        return FOLDER_TMP
   }
}
def moveReportToFolder(APPENDIX_TMP, LOGS_FOLDER_TMP)
{
    sh "ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP} 'mv ${DAFT_LOGS_DIR}/${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}${APPENDIX_TMP}_*-Verdict-Details.log ${LOGS_FOLDER_TMP}' 2>/dev/null || true"
    sh "ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP} 'mv ${DAFT_LOGS_DIR}/${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}${APPENDIX_TMP}_*-Verdict-Report.log ${LOGS_FOLDER_TMP}' 2>/dev/null || true"
}
def prepareExitReport()
{
    sh "scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP}:${LOGS_FOLDER_ROBUSTNESS}/logfiles/kpi_verdict.log ./${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}${DAFT_WS_APPEND}_Exit-Verdict-Report.log 2>/dev/null || true"
    sh """sed -i '1s/^/Exit criteria checked: ${KPI_REQUEST}\\n\\n\\n/' *${DAFT_WS_APPEND}_Exit-Verdict-Report.log"""
    sh """ls *${DAFT_WS_APPEND}_Exit-Verdict-Report.log >> ./names_array_split.txt 2>/dev/null || true"""

    archiveArtifacts allowEmptyArchive: true, artifacts: "*${DAFT_WS_APPEND}_Exit-Verdict-Report.log", onlyIfSuccessful: false
}
def scanError(LOGS_FOLDER_TMP)
{
    script
    {
        FAILING_PART = """${sh( returnStdout: true,
                            script: """ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP} "cat ${LOGS_FOLDER_TMP}/summary.txt | grep '${SEARCH_STRING}'" 2>/dev/null || true""")}""".trim()

        if ("${FAILING_PART}".contains("${SEARCH_STRING}"))
        {
            FAIL_JOB=true
        }
    }
}
def formerScaleLogic()
{
    script
    {
        PLAYLIST_PARAMETERS = PLAYLIST_PARAMETERS + "-v SCALE_APPLICATION=${CNF_TYPE} "

/*after EVNFM LCM based introduction we execute by default 2 scenarios inside the TC, increment 1 and 2.
Hence parameter SCALE_INCREMENT is not available in the initial form, but values are assigned inside this script

        if ("${env.SCALE_INCREMENT}" != 'null')
        {
            PLAYLIST_PARAMETERS = PLAYLIST_PARAMETERS + "-v SCALE_INCREMENT=${SCALE_INCREMENT} "
        }

 */
// The above means that we will always pass to DAFT the parameter SCALE_INCREMENT with different values
        PLAYLIST_PARAMETERS = PLAYLIST_PARAMETERS + "-v SCALE_INCREMENT="

        if ("${env.WAIT_TIME_BETWEEN_SCALE_OPERATIONS}" != 'null')
        {
            PLAYLIST_PARAMETERS = PLAYLIST_PARAMETERS + "-v WAIT_TIME_BETWEEN_SCALE_OPERATIONS=${WAIT_TIME_BETWEEN_SCALE_OPERATIONS} "
        }
    }
}

def formercollectADPlogs()
{
    script
    {
        sh "${DAFT_DIR}/expect/bin/send_command_to_ssh.exp --timeout=3600 --user=eccd --ip=${NODE_DIRECTOR_IP} --password='notneeded' --stop-on-error \
                        --command='export PS1=DAFT#' --command='mkdir ${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}' --command='cd ${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}' \
                        --command='${DAFT_NS_DIR}/bin/collect_ADP_logs.sh -n ${NAMESPACE}'"

        sh "scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP}:/home/eccd/${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}/*.tgz ./. 2>/dev/null || true"
        FILE_INITIAL_NAME = """${sh( returnStdout: true, script: "echo *.tgz")}""".trim()
        sh "mv ${FILE_INITIAL_NAME} ${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}_Entry-Verdict-ADP${FILE_INITIAL_NAME} 2>/dev/null || true"

        sh """ls *.tgz >> ./names_array_split.txt 2>/dev/null || true"""

        archiveArtifacts allowEmptyArchive: true, artifacts: '*.tgz', onlyIfSuccessful: false

        sh """rm -rf *.tgz""" // removed it from the workspace so not to be fetched twice on the artifact_list (here and at the post always section)

        sh "${DAFT_DIR}/expect/bin/send_command_to_ssh.exp --timeout=1800 --user=eccd --ip=${NODE_DIRECTOR_IP} --password='notneeded' --stop-on-error \
        --command='rm -rf ${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}'"
    }
}
def ADPfileHandling(THIS_DIR, THIS_NS, THIS_FILE_START_NAME)
{
    script
    {
        sh "${DAFT_DIR}/expect/bin/send_command_to_ssh.exp --timeout=3600 --user=eccd --ip=${NODE_DIRECTOR_IP} --password='notneeded' --stop-on-error \
                        --command='export PS1=DAFT#' --command='mkdir ${THIS_DIR}' --command='cd ${THIS_DIR}' \
                        --command='${DAFT_NS_DIR}/bin/collect_ADP_logs.sh -n ${THIS_NS}'"
        FILE_INITIAL_NAME_FULL_PATH = """${sh( returnStdout: true,
        script: """ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP} "find /home/eccd/${THIS_DIR}  -name '*.tgz' " """)}""".trim()

        FILE_INITIAL_NAME = """${sh( returnStdout: true, script: "echo ${FILE_INITIAL_NAME_FULL_PATH} | sed 's?^.*/??'")}""".trim()
        sh "scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP}:${FILE_INITIAL_NAME_FULL_PATH} ./. 2>/dev/null || true"

        FILE_FINAL_NAME = "${THIS_FILE_START_NAME}${FILE_INITIAL_NAME}"
        sh "mv ${FILE_INITIAL_NAME} ${FILE_FINAL_NAME} 2>/dev/null || true"
        sh """ls ${FILE_FINAL_NAME} >> ./names_array_split.txt 2>/dev/null || true"""
        archiveArtifacts allowEmptyArchive: true, artifacts: "${FILE_FINAL_NAME}", onlyIfSuccessful: false
        sh """rm -rf ${FILE_FINAL_NAME}""" // removed it from the workspace so not to be fetched twice on the artifact_list (here and at the post always section)
        sh "${DAFT_DIR}/expect/bin/send_command_to_ssh.exp --timeout=1800 --user=eccd --ip=${NODE_DIRECTOR_IP} --password='notneeded' --stop-on-error \
        --command='rm -rf ${THIS_DIR}'"
    }
}
