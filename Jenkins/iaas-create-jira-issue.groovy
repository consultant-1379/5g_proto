//  POSSIBILITY OF USAGE BY DIFFERENT VALUE COMBINATIONS OF THE INPUT PARAMETERS (F=FILLED / NF=NOT FILLED)
//  =======================================================================================================
//
//  TEST_PLAN   RUN_ULR   RUN_STATUS   LOGS_TO_LEXICON   JIRA_DEPLOY_ID     SCENARIO DESCRIPTION
//      F          F          F               F               N/A           DND is created, artifacts are attached, logs are sent to Lexicon
//
//      F          F          F               NF              N/A           DND is created, artifacts are attached, logs are not sent to Lexicon
//
//      NF         F          NF              NF              F             Artifacts are attached ONLY (to DND specified in JIRA_DEPLOY_ID)
//                                                                          DND has already been created, logs are not sent to Lexicon
//
//      NF         F          F               F               NF            Artifacts to be sent to Lexicon. NO DND is created
//                                                                          To be used for positive cases so for Lexicon to learn,
//                                                                          but of no interest for troubleshooters
//                                                                          (example: successful check on running traffic for 1 minute)
//
//  **************************************************************** Future design if needed ****************************************************************
//
//      NF         F          F               F               F             It could be meant two possibilities (both have issues to solve):
//                                                                          (1)
//                                                                          Artifacts to be sent to Lexicon, BUT appending the result to a DND
//                                                                          To be used when a DND was already created but info was not sent to
//                                                                          Lexicon yet, as not all logs were created AND the DND doesn't need
//                                                                          those files.
//                                                                          Note: the comment indicating that logs have been sent to Lexicon would have to be
//                                                                          written afterwards and not at DND creation time.
//                                                                          Problem here is that ONLY
//                                                                          the logs under the current artifact list will be sent, but the present
//                                                                          on the created DND will not be there.
//
//                                                                          (2)
//      NF         F          F               F               F             Attach atifacts and send to Lexicon at the same time. Similar to (1) to
//                                                                          be used when a DND was already created but info was not sent to
//                                                                          Lexicon yet, as not all logs were created BUT here the artifacts are
//                                                                          needed for the DND.
//                                                                          Note: the comment indicating that logs have been sent to Lexicon would have to be
//                                                                          written afterwards and not at DND creation time.
//                                                                          Similar problem to (1) exists. For example, the console log of the
//                                                                          original run wouldn't be present
//
//                                                                          Both (1) and (2) have the same combination of input parameters values.
//                                                                          This means and additional flag/value will have to be created in order
//                                                                          to distinguish both (eg, for (1) get a new value for LOGS_TO_LEXICON
//                                                                          "retroactively" and by that not attaching artifacts to the DND, but
//                                                                          just sending them to Lexicon...)



//releases_array= ["1.10","1.10.1","1.10.2","1.11","1.12","1.13"]
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
pipeline
{
    agent
    {
        node
        {
            label "${AGENT_LABEL}"
        }
    }
    options
    {
        timeout(time: 15, unit: 'MINUTES')
        timestamps()
    }
    environment
    {
        ARTIFACTORY_TOKEN = "Token for armdocker" /* iaas: not shown in the initial form as always gets this value */
        ARTIFACTORY_PATH_SC = "https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/sc-automated-troubleshooting-logs"
        ARTIFACTORY_PATH_LEXICON = "https://arm.sero.gic.ericsson.se/artifactory/proj-lexicon-generic-local/signaling_controller"
        TEST_PLAN = "${TEST_PLAN}"
        RUN_STATUS  = "${RUN_STATUS}"
        IAAS_NODE   = "${IAAS_NODE}"              // Needed for appending the node name at the beginning of the log file
        CSAR_PKG    = "${CSAR_PKG}"                //Needed for appending to the log file
        BASE_PKG    = "${BASE_PKG}"                //Needed for base info in summary when upgrading
        CNF_UNDER_TEST = "${CNF_UNDER_TEST}"
        RUN_URL     = "${RUN_URL}" //directly points to the correct path (despite whether it is IaaS or traffic scenario)
        SPINNAKER_EXEC_ID = "${SPINNAKER_EXEC_ID}"
        JIRA_DEPLOY_ID = "${JIRA_DEPLOY_ID}" // only used for tracking the deployment which triggered a stability scenario
        COLLECT_ADP_LOGS_ARTIFACT = "${COLLECT_ADP_LOGS_ARTIFACT}" // To be filled only for Stability scenario and in case the stability run failed. In this case ADP logs will be collected and here the full path to the artifact containing the info is given
        TRAFFIC_RUN = "${TRAFFIC_RUN}" // To be filled only for RTC cases. Needed in order to get the appropriate Jira TC when filling the issue
        TRAFFIC_BUILD ="${TRAFFIC_BUILD}"
        TRAFFIC_TYPE ="${TRAFFIC_TYPE}"
        JENKINS_INSTANCE = "https://fem1s10-eiffel029.eiffel.gic.ericsson.se:8443/jenkins/job/5G-ESC/job/"
        JENKINS_INSTANCE_NEW = "https://fem3s10-eiffel029.eiffel.gic.ericsson.se:8443/jenkins/job/5G-ESC/job/"
        TRAFFIC_ULR_START_CI = "${JENKINS_INSTANCE}/TeamCI/job/04-Verification-Pipeline-StabilityTest_Rebels/"
        TRAFFIC_ULR_START_CI_CNCS = "${JENKINS_INSTANCE}/TeamCI-CNCS/job/500-Verification-pipeline-TeamCI-CNCS-Rebels/"

        TRAFFIC_ULR_START_DAFT = "${JENKINS_INSTANCE}/IaaS/job/IaaS-traffic-DAFT/"
        TRAFFIC_PATH_CI="_04-Verification-Pipeline-StabilityTest_Rebels-"
        TRAFFIC_PATH_CI_CNCS="_500-Verification-pipeline-TeamCI-CNCS-Rebels-"

        TRAFFIC_PATH_DAFT="_IaaS-traffic-DAFT-"
        STAB_DURATION ="${STAB_DURATION}" // To be filled only for deploy-stability, so that the appropriate jira TC is selected
        SPINNAKER_ULR_START = "https://spinnaker.rnd.gic.ericsson.se/#/applications/esc5gtest/executions/details/"
        SPINNAKER_ULR_END = "?pipeline=IaaS-node-scheduler,IaaS-verification-CNF,IaaS-verification-looper,IaaS-verification-orchestrator&stage=0&step=0&details=evaluateVariablesConfig"
        JIRA_ULR_START = "https://eteamproject.internal.ericsson.com/browse/"
        LOGS_TO_LEXICON = "${LOGS_TO_LEXICON}"
        LOGS_START_TIME = "${LOGS_START_TIME}"
        LOGS_END_TIME = "${LOGS_END_TIME}"
        LOGS_FOLDER = "${LOGS_FOLDER}"  //here we give a unique value ("pipeline" _ "build number"). Eg: "IaaS_deploy_day0_day1_SC1.10.0_all_321_failed_continue"
                                                        // in the previous example, pipeline is "IaaS_deploy_day0_day1_SC1.10.0_all" and build number "321"
                                                        // so that any other run for deploying all at SC1.10.1 will have the same pipeline name and different build number
                                                        // other scenario examples: "IaaS_deploy_day0_SC1.8.1_sepp _5433", "IaaS_upgrade_with_traffic_scp-slf_base_SC1.8.1_to_SC1.10.0_5912_succeeded"
        TRAFFIC_VERDICT_SCENARIO = "${TRAFFIC_VERDICT_SCENARIO}"
        VARIANT = "${VARIANT}"
        ARTIFACT_LIST = "${ARTIFACT_LIST}"
        REBUILDS = "${REBUILDS}"
        WEBHOOK = "${WEBHOOK}"
    }
    stages
    {
        stage('Initialize')
        {
            steps
            {
                sh """ rm -rf *.*"""
                script
                {
                    CURRENT_DATE = sh(script: "date '+%Y/%m/%d %T'", returnStdout: true).trim()
                    YYYY = """${sh( returnStdout: true, script: "echo ${CURRENT_DATE} | cut -d '/' -f1")}""".trim()
                    MM = """${sh( returnStdout: true, script: "echo ${CURRENT_DATE} | cut -d '/' -f2")}""".trim()
                    UNEXPECTED_ERROR="no"
                    TRIGGER_LEXICON_ANALYSIS="no"
                    LEXICON_LINK="not-needed"

                    array=[]
                    a=0
                    element = """${sh( returnStdout: true, script: "echo ${env.ARTIFACT_LIST} | cut -d ' ' -f${a+1}")}""".trim()

                    if ("${env.ARTIFACT_LIST}" != 'null')
                    {
                        if ("${env.ARTIFACT_LIST}".contains(" ")) // there is more than one element
                        {
                            no_more_artifacts = false
                        }
                        else
                        {
                            no_more_artifacts = true
                            array[a] = element
                        }
                        e=0 /* protect from infinite loops. Stop in case 100 elements are reach */
                        while ( "${element}" != '' && "${element}" != 'null' && ( !no_more_artifacts ) && e<100)
                        {
                            array[a] = element
                            a=a+1
                            element = """${sh( returnStdout: true, script: "echo ${ARTIFACT_LIST} | cut -d ' ' -f${a+1}")}""".trim()
                            e=e+1
                        }
                        if (e==100)
                        {
                            error ("It looks like the artifact list entered into an infinite loop. Job will be aborted")
                        }
                    }
                    artifact_array_length = "${array.size()}".toInteger()

                    if (("${env.RUN_STATUS}" == 'SUCCEEDED') || ("${env.RUN_STATUS}" == 'FALSE')) // the value FALSE comes at Stability scenarios and takes the value for variable TROUBLESHOOTING. FALSE indicates that no troubleshooting is needed, hence the stability run was successful
                    {
                        TEST_EXEC_STATUS ="PASS"
                    }
                    else
                    {
                        TEST_EXEC_STATUS ="FAIL"
                    }

                    if ( (("${env.LOGS_TO_LEXICON}" == 'yes') || ("${env.LOGS_TO_LEXICON}".contains("${TEST_EXEC_STATUS}"))) &&  (! "${IAAS_NODE}".contains("TEST")) )
                    {
                        trigger_lexicon = true
                    }
                    else
                    {
                        trigger_lexicon = false
                    }
                    // in case of using the script only for attaching artifacts, the variables RUN_STATUS & TEST_PLAN are NOT filled in the initial form
                    if ("${env.TEST_PLAN}" != 'null')
                    {
                        script_action = "create DND"
                    }
                    else
                    {
                        if ("${env.RUN_STATUS}" == 'null')
                        {
                            script_action = "attach artifacts"
                        }
                        else
                        {
                            script_action = "lexicon" // At stage "Trigger job only for Lexicon" it is equivalent to ask for < environment name: 'TEST_PLAN', value: '' > or < script_action == "lexicon" >
                        }
                    }
                    LOGS_LINK = """${sh( returnStdout: true, script: "echo ${JENKINS_INSTANCE}/IaaS/job/${JOB_BASE_NAME}")}""".trim()
                    // Logic for getting the JOB_NAME. In principle this works both, for IaaS layouts (eg: https://fem1s10-eiffel029.eiffel.gic.ericsson.se:8443/jenkins/job/5G-ESC/job/IaaS/job/IaaS-deploy/3358/) or teamCI ones (eg: https://fem1s10-eiffel029.eiffel.gic.ericsson.se:8443/jenkins/job/5G-ESC/job/TeamCI/job/04-Verification-Pipeline-StabilityTest_Rebels/871/)
                    //                                                                                                                                                                                              after CNCS introduction: https://fem1s10-eiffel029.eiffel.gic.ericsson.se:8443/jenkins/job/5G-ESC/job/TeamCI-CNCS/view/Rebels-CNCS/job/500-Verification-pipeline-TeamCI-CNCS-Rebels/3/
                    LOG_JOB_NAME = """${sh(returnStdout: true, script: "echo  ${RUN_URL} | cut -d '/' -f10")}""".trim() //value: IaaS-deploy.
                    LOG_JOB_BUILD = """${sh(returnStdout: true, script: "echo ${RUN_URL} | cut -d '/' -f11")}""".trim() //value: 3192 (eg)

                    UC = """${sh( returnStdout: true, script: "echo ${LOG_JOB_NAME} | sed 's/IaaS-//' | sed 's/-cnDSC//' | sed 's/-evnfm//' | sed 's/-restore//' | sed 's/-recovery//' | sed 's/robustness-//' | sed 's/04-Verification-Pipeline-//' | sed 's/Test_Rebels//' | sed 's/500-Verification-pipeline-TeamCI-CNCS-Rebels/Stability/'")}""".trim() // eg. "upgrade", "nw_impairment", "Stability", "stability-verdict"

/* Marking for EVNFM under the parameter TestEnvironments in Jira
                    if ("${IAAS_NODE}".contains("EVNFM") || "${env.VARIANT}".contains("EVNFM"))
                    {
                        INITIAL_TAG = "EVNFM "
                    }
                    else
                    {

 */
                        INITIAL_TAG = ""
//                    }
                    IAAS_NODE = """${sh( returnStdout: true, script: "echo ${IAAS_NODE} | sed 's/_EVNFM//'")}""".trim()

                    if ("${env.REBUILDS}" == 'yes')
                    {
                        REBUILDS_MAIL_MARKING = " (this was a rebuild run)"
                    }
                    else
                    {
                        REBUILDS_MAIL_MARKING = ""
                    }
                    REBUILD_NEEDED = "no" /* Initialization; set to yes (if needed) in the post pipeline part */
                }
            }
        }
        stage('Create DND')
        {
        when { expression { script_action == 'create DND' }}
//        when { not {environment name: 'TEST_PLAN', value: ''}}
            steps
            {
                script
                {
                    withCredentials([usernameColonPassword(credentialsId: '7b8a9097-73ab-4e79-8b06-b8dcbbf65c3a', variable: 'API_TOKEN')])
                    {
                        echo 'Create a Test Execution with data fields content'

                        run_specific_data()

//                        CREATE_TEST_EXEC = sh(script: "curl -H \"Content-Type: application/json\" -X POST -u $API_TOKEN --data '{\"fields\":{\"project\":{\"key\": \"DND\"},\"issuetype\": {\"name\": \"Test Execution\"},\"summary\": \"${SUMMARY}\"}}' https://eteamproject.internal.ericsson.com/rest/api/2/issue/", returnStdout: true).trim()
//                        TEST_EXEC_JSON = sh(script: """echo "${CREATE_TEST_EXEC}" | jq -r  """, returnStdout: true).trim()
//                        writeFile(file: "test_execution.properties", text: TEST_EXEC_JSON)

                        CREATE_TEST_EXEC = sh(script: "curl -H \"Content-Type: application/json\" -X POST -u $API_TOKEN --data '{\"fields\":{\"project\":{\"key\": \"DND\"},\"issuetype\": {\"name\": \"Test Execution\"},\"summary\": \"${SUMMARY}\",\"environment\": \"${IAAS_NODE}\",\"customfield_18037\": \"${CSAR_PKG}\",\"components\": [{\"name\": \"${CNFs}\"}],\"customfield_18045\": [\"${TEST_PLAN_DND}\"],\"customfield_18043\": [\"${VARIANT}\"],\"labels\": [\"VerificationPipe\"],\"fixVersions\": [{\"name\": \"SC ${RELEASE}\"}]}}' https://eteamproject.internal.ericsson.com/rest/api/2/issue/", returnStdout: true).trim()

                        echo 'Get Test Execution DND'
                        TEST_EXEC_DND = """${sh( returnStdout: true, script: "echo ${CREATE_TEST_EXEC} | cut -d ' ' -f2 | cut -d ':' -f2")}""".trim()

                        echo 'Create a comment in the test execution'
                        jiraAddComment idOrKey: "$TEST_EXEC_DND", comment: "$comment", site: 'DSC_Node_Development'

                        echo 'Associate Test XRay to Test Execution'
                        sh(script: "curl -H \"Content-Type: application/json\" -X POST -u $API_TOKEN --data '{\"add\":[\"$TEST_XRAY_DND\"]}' https://eteamproject.internal.ericsson.com/rest/raven/1.0/api/testexec/$TEST_EXEC_DND/test", returnStdout: true).trim()

                        echo 'Get Test Run id' // needed in order to set the status of the test exec to PASS/FAIL

                        TEST_RUN_DATA = sh(script: "curl -H \"Content-Type: application/json\" -X GET -u $API_TOKEN https://eteamproject.internal.ericsson.com/rest/raven/1.0/api/testexec/$TEST_EXEC_DND/test?detailed=true", returnStdout: true).trim()
                        TEST_RUN_ID = """${sh( returnStdout: true, script: "echo ${TEST_RUN_DATA} | cut -d ':' -f2 | cut -d ']' -f1")}""".trim()

                        echo 'Set status of Test Execution'
                        sh (script: "curl -H \"Content-Type: application/json\" -X PUT -u $API_TOKEN https://eteamproject.internal.ericsson.com/rest/raven/1.0/api/testrun/$TEST_RUN_ID/status?status=$TEST_EXEC_STATUS", returnStdout: true).trim()

                        if ("${UC}" != 'Performance') // For 'Performance' no console.log is attached but we refer to the already created jira execution for the upgrade
                        {
                            if ( ("${UC}" == 'Traffic Verdict') || ((("${UC}" == 'upgrade') || ("${UC}" == 'scale') || ("${LOG_JOB_NAME}".contains("robustness")) ) && ("${env.TRAFFIC_RUN}" == 'true'))) // For 'Traffic Verdict', RTC under traffic, or upgrade-under-stability, the console.log from the traffic is attached
                            {
                            // for scenarios under DAFT traffic, the traffic job console.log will be attached during the process of adding artifacts
                                if ("${env.TRAFFIC_TYPE}" !="DAFT")
                                {
                                    echo 'Add traffic job console.log to attachments'
                                    RUN_URL_ORIGINAL = RUN_URL

                                    input_traffic_console_log()
                                    fetch_console_log()
                                    attach_console_log()

                                    RUN_URL = RUN_URL_ORIGINAL
                                }
                            }
                            if ("${UC}" != 'Traffic Verdict')
                            {
                                LOG_TAR_FILE_START= IAAS_NODE +"_" +LOG_JOB_NAME +"-" +LOG_JOB_BUILD
                                LOG_FILE_NAME = "Console_output_" +LOG_TAR_FILE_START +""".log"""
                                echo 'Add console.log to attachments'
                                fetch_console_log()
                                attach_console_log()
                            }
                        }
                    }
                }
            }
        }
        stage('Process artifacts and Lexicon')
        {
            steps
            {
                script
                {
                    no_more_artifacts = false
                    i = 0

                    if (trigger_lexicon==true) // Create info file if logs are to be sent to Lexicon
                    {
                        sh """touch ./run_info.yaml"""
                        // Upload the run console log to Lexicon
                        LOG_TAR_FILE_START= IAAS_NODE +"_" +LOG_JOB_NAME +"-" +LOG_JOB_BUILD
                        LOG_FILE_NAME = "Console_output_" +LOG_TAR_FILE_START +""".log"""
                        fetch_console_log()
                        ARTIFACT_NAME = LOG_FILE_NAME
                        ARTIFACTORY_PATH ="${ARTIFACTORY_PATH_LEXICON}"+"/${YYYY}/${MM}/${LOGS_FOLDER}"

                        if (!("${LOG_JOB_NAME}".contains("stability-verdict"))) //for stability-verdict the console log is not uploaded to Lexicon (the traffic console log tough yes)
                        {
                            upload_artifact_to_Lexicon()
                        }
                    }

                    if (script_action == "attach artifacts")
                    {
                        // for attaching artifacts, jump directly to the loop for attaching them
                        UC = "attach artifacts"                 // this UC as such does not exist, it is just for not entering into the coming queries
                        TEST_EXEC_DND = JIRA_DEPLOY_ID          // so to reuse the existing routines
                        // for scenarios under DAFT traffic, the traffic job console.log is attached now, during the process of adding artifacts
                        // we are able to retrieve this info, as variables TRAFFIC_TYPE and TRAFFIC_BUILD are provided as input parameters
                        if (("${env.TRAFFIC_TYPE}" =="DAFT") && ("${env.TRAFFIC_BUILD}" != 'null'))
                        {
                            RUN_URL_ORIGINAL = RUN_URL

                            input_traffic_console_log()
                            fetch_console_log()
                            attach_console_log()

                            RUN_URL = RUN_URL_ORIGINAL
                        }

                    }
                    if ((trigger_lexicon==true) && ("${LOG_JOB_NAME}".contains("stability-verdict"))) /* when ONLY lexicon sending is the scenario and it is activated, upload the traffic console log (both, for CI/DAFT) */
                    {                                                            // we are able to retrieve this info, as variables TRAFFIC_TYPE and TRAFFIC_BUILD are provided as input parameters
                                                                                 // we need to send to Lexicon the traffic console log AS WELL in case URL is stability-traffic (currently for dep-stab)

                        if (("${env.TRAFFIC_TYPE}" != 'null') && ("${env.TRAFFIC_BUILD}" != 'null'))
                        {
                            RUN_URL_ORIGINAL = RUN_URL

                            input_traffic_console_log()
                            fetch_console_log()
                            ARTIFACT_NAME = LOG_FILE_NAME
                            ARTIFACTORY_PATH ="${ARTIFACTORY_PATH_LEXICON}"+"/${YYYY}/${MM}/${LOGS_FOLDER}"
                            upload_artifact_to_Lexicon()

                            RUN_URL = RUN_URL_ORIGINAL
                        }
                    }
// in case the scenario is a stability run we need to check whether ADP logs where collected so to add them to the jira issue
                    if ("${UC}" == 'Stability')
                    {
                        if ("${env.COLLECT_ADP_LOGS_ARTIFACT}" == 'null')
                        {
                            echo """no ADP logs were collected for this scenario; none will be archived. """
                        }
                        else
                        {
                            fetch_artifact_ADP_logs()
                            if (script_action != "lexicon")
                            {
                                attach_artifact_ADP_logs()
                            }

                            if (trigger_lexicon==true)
                            {
                                ARTIFACT_NAME = "${LOG_TAR_FILE_START}-ADP-logs-after-stability.tgz"
                                ARTIFACTORY_PATH ="${ARTIFACTORY_PATH_LEXICON}"+"/${YYYY}/${MM}/${LOGS_FOLDER}"
                                upload_artifact_to_Lexicon()
                            }
                        }
                        if ("${env.TEST_PLAN}" != 'null') // currently the jcatlogs.zip are only attach to a being created DND
                        // if (script_action != "lexicon")
                        {
                            attach_artifact_html() //collect the html report in case it was provided
                                               // ATTENTION: this jcat logs are not uploaded to Lexicon for the time being. It is considered that the console.log, what the jcat logs are based on, is enough for analysis
                        }
                        no_more_artifacts = true //for stability there are no more artifacts to be attached
                    }
                    if ("${UC}" == 'Performance')  // For 'Performance' only the file called "difference_entry_exit_kpi.txt" created during the upgrade is attached
                    {                              // ATTENTION: this file is not to be uploaded to Lexicon
                        ARTIFACT_NAME = "difference_entry_exit_kpi-upgrade-${LOG_JOB_BUILD}.txt"
                        fetch_artifact()
                        if (script_action != "lexicon")
                        {
                            attach_artifact()
                        }
                        no_more_artifacts = true //for 'Performance' there are no more artifacts to be attached
                    }
                    while( ( !no_more_artifacts ) && ( i < artifact_array_length))
                    {
                            ARTIFACT_NAME = array.get(i)
                            fetch_artifact()
                            if (script_action != "lexicon")
                            {
                                ARTIFACT_SIZE = """${sh( returnStdout: true, script: "ls -s --block-size=1M ${ARTIFACT_NAME} | cut -d ' ' -f1")}""".trim()
                                if ("${ARTIFACT_SIZE}".toInteger() > 99) // File sized of 100MB and more cannot be stored in Jira
                                {
                                    ARTIFACTORY_PATH ="${ARTIFACTORY_PATH_SC}"
                                    upload_artifact_to_SC()
                                }
                                else
                                {
                                    attach_artifact()
                                }
                            }

                            if ((trigger_lexicon==true) && (("${ARTIFACT_NAME}".contains("tar.bz2")) || ("${ARTIFACT_NAME}".contains(".tgz")))) // Check if Logs should be sent to Lexicon. If yes only those created by DAFT are currently sent (both, workspace generated ones & .tgz files included inside the troubleshooting_logs)                             // The run console log has already been uploaded to Lexicon
                            {
                                ARTIFACTORY_PATH ="${ARTIFACTORY_PATH_LEXICON}"+"/${YYYY}/${MM}/${LOGS_FOLDER}"
                                upload_artifact_to_Lexicon()
                            }
                            i=i+1
                    }
                    if (trigger_lexicon==true) // Finalize info file for Lexicon
                    {
                        EMPTY_FILE_NAME = sh(script: "find -empty -name run_info.yaml", returnStdout: true).trim()
                        if ("${EMPTY_FILE_NAME}".contains("run_info.yaml"))
                        {
                            echo 'No file has been uploaded to the Lexicon artifactory'
                        }
                        else
                        {
                            ARTIFACTORY_PATH ="${ARTIFACTORY_PATH_LEXICON}"+"/${YYYY}/${MM}/${LOGS_FOLDER}"
                            if (script_action == "lexicon")
                            {
                                JIRA_INFO_LEXICON = "This run didn't trigger Jira execution creation"
                            }
                            else
                            {
                                JIRA_INFO_LEXICON = "${TEST_EXEC_DND}"
                                TRIGGER_LEXICON_ANALYSIS="yes"
                                LEXICON_LINK= LOGS_FOLDER
                            }
                            finalize_info_file_Lexicon()

                            if ((script_action != "lexicon") && ("${TRIGGER_LEXICON_ANALYSIS}" == "yes"))
                            {
                                echo 'Create a comment informing that the logs will be sent to Lexicon'
                                comment_lexicon ="Logs for this run have been sent to Lexicon for analysis."
                                jiraAddComment idOrKey: "$TEST_EXEC_DND", comment: "$comment_lexicon", site: 'DSC_Node_Development'
                            }
                        }
                    }

//Trial with wget but didn't work
//                    sh(script: """mkdir -p ./files-to-archive""", returnStdout: true).trim()
//                    sh(script: """wget --auth-no-challenge --execute="robots = off" --mirror --convert-links --no-parent --continue --wait=5 ${RUN_URL} -P ./files-to-archive""", returnStdout: true).trim()
                }
            }
        }
    }
    post
    {
        always
        {
            cleanWs()
        }
        success
        {
            script
            {
                if (script_action == "create DND")
//                if ("${env.TEST_PLAN}" != 'null')
                {
                    echo 'Archive the complete data for the new test execution'
                    def issue = jiraGetIssue idOrKey: "${TEST_EXEC_DND}", site: 'DSC_Node_Development'
                    def raw_data = issue.data.toString()
                    writeFile(file: "test_execution_${TEST_EXEC_DND}.txt", text: raw_data)

                    sh """echo "GENERATED_JIRA_ID=${TEST_EXEC_DND}"	                    >  ./jira_issue_id.txt"""
                    sh """echo "TRIGGER_LEXICON_ANALYSIS=${TRIGGER_LEXICON_ANALYSIS}"   >> ./jira_issue_id.txt"""
                    sh """echo "LEXICON_LINK=${LEXICON_LINK}"	                        >> ./jira_issue_id.txt"""

                    archiveArtifacts allowEmptyArchive: true, artifacts: '*.txt', onlyIfSuccessful: false
                }
            }
        }
        failure
        {
            script
            {
                if ((UNEXPECTED_ERROR=="yes") || (UNEXPECTED_ERROR=="TC-ID-NOT-FOUND"))
                {
                    if (UNEXPECTED_ERROR=="yes")
                    {
                        REBUILD_NEEDED = "yes"
                        emailbody_job_failure_1="\nthere were errors either with the console log or attaching files for the job link ${RUN_URL} triggered by the spinnaker execution ${SPINNAKER_ULR_START}${SPINNAKER_EXEC_ID}${SPINNAKER_ULR_END}."
                        LIST_ADDRESSEES ="eedjoz@eed.ericsson.se"
                    }
                    else
                    {
                        emailbody_job_failure_1="\nthere were errors while fetching the DND for the Test XRay for the job link ${RUN_URL} triggered by the spinnaker execution ${SPINNAKER_ULR_START}${SPINNAKER_EXEC_ID}${SPINNAKER_ULR_END}."
                        LIST_ADDRESSEES ="eedjoz@eed.ericsson.se;ehenkay@eed.ericsson.se"
                    }
                    def emailbody_job_failure_2="\n This is not expected !!!\n\nPlease have a look as no issue will be created for that build."
                    def emailbody_job_failure_3="\n\nDetailed log for the jira issue creation jenkins job can be found under:\n${LOGS_LINK}/$BUILD_NUMBER/consoleFull"

                    emailext body: "${emailbody_start}"+"${emailbody_job_failure_1}"+"${emailbody_job_failure_2}"+"${emailbody_job_failure_3}"+"${emailbody_end}",
                    subject: "${REASON}${REBUILDS_MAIL_MARKING}. Please check", to: "${LIST_ADDRESSEES}"

                }
                else
                {
                    REBUILD_NEEDED = "yes"
                    if (script_action == "attach artifacts")
                    {
                        def emailbody_job_failure_1="\nthe job for attaching additional artifacts from job ${RUN_URL} to the existing jira execution ${JIRA_ULR_START}${JIRA_DEPLOY_ID} triggered by the spinnaker execution"
                        def emailbody_job_failure_2=" ${SPINNAKER_ULR_START}${SPINNAKER_EXEC_ID}${SPINNAKER_ULR_END} has failed.\nAs this was not expected, please have a look."
                        def emailbody_job_failure_3="\n\nDetailed logs can be found under:\n${LOGS_LINK}/$BUILD_NUMBER/consoleFull"

                        emailext body: "${emailbody_start}"+"${emailbody_job_failure_1}"+"${emailbody_job_failure_2}"+"${emailbody_job_failure_3}"+"${emailbody_end}",
                        subject: "Addition of artifacts to an existing Jira execution run failed${REBUILDS_MAIL_MARKING}", to: "eedjoz@eed.ericsson.se"
                    }
                    else
                    {
                        def emailbody_job_failure_1="\nthe job for creating a Jira issue for job ${RUN_URL} triggered by the spinnaker execution ${SPINNAKER_ULR_START}${SPINNAKER_EXEC_ID}${SPINNAKER_ULR_END} has failed."
                        def emailbody_job_failure_2="\n\nAs no issue will be created for that build, please have a look in case this was not expected."
                        def emailbody_job_failure_3="\n\nDetailed log for the jira issue creation jenkins job can be found under:\n${LOGS_LINK}/$BUILD_NUMBER/consoleFull"

                        emailext body: "${emailbody_start}"+"${emailbody_job_failure_1}"+"${emailbody_job_failure_2}"+"${emailbody_job_failure_3}"+"${emailbody_end}",
                        subject: "Creation of Jira issue for job ${LOG_JOB_NAME} build #${LOG_JOB_BUILD} failed${REBUILDS_MAIL_MARKING}", to: "eedjoz@eed.ericsson.se"
                    }
                }

                //Trigger the rebuild of the run if needed (ONLY if REBUILD_NEEDED = "yes" AND it was not already a rebuilding run (REBUILDS !="yes"))
                if ((REBUILD_NEEDED=="yes") && ("${env.REBUILDS}" != 'yes'))
                {
                    triggerRebuildJira()
                }

                //Remove the new DND in case parts of it were created
                if ((script_action == "create DND") && ("${TEST_EXEC_DND}".contains("DND")))
                {
                    sleep (10)
                    withCredentials([usernameColonPassword(credentialsId: '7b8a9097-73ab-4e79-8b06-b8dcbbf65c3a', variable: 'API_TOKEN')])
                    {
                        sh(script: "curl -H \"Content-Type: application/json\" -X DELETE -u $API_TOKEN https://eteamproject.internal.ericsson.com/rest/api/2/issue/$TEST_EXEC_DND", returnStdout: true).trim()
                    }
                }
            }
        }
    }
}
def run_specific_data()
{
    script
    {
        switch (CNF_UNDER_TEST)
        {
            case ['sepp', 'bsf', 'scp', 'dsc']:
                CNFs = "${CNF_UNDER_TEST}".toUpperCase()
                break
            case ['cnDSC']:
                CNFs = "cnDSC"
                break
            case ['bsf-dsc']:
                CNFs = "BSF-DSC"
                break
            case ['bsf-scp-sepp']:
                CNFs = "SC"
                break
            case ['bsf-scp-sepp-dsc']:
                CNFs = "SC-DSC"
                break
            default:
                error("No logic for CNF \"$CNF_UNDER_TEST\" has been implemented")
                break
        } //switch

//        RELEASE = """${sh( returnStdout: true, script: "echo ${CSAR_PKG} | sed 's/\\.0.*//' | sed 's/\\.25.*//' | cut -d '+' -f1 | cut -d '-' -f1")}""".trim() // + for SC and - for cnDSC
        RELEASE = """${sh( returnStdout: true, script: "echo ${CSAR_PKG} | cut -d '+' -f1 | cut -d '-' -f1 | sed 's/\\.0\$/joker/' | sed 's/\\.25\$//' | sed 's/joker//'")}""".trim() // + for SC and - for cnDSC


// Possibilities for LOG_JOB_NAME:
// IaaS-deploy
// IaaS-deploy-cnDSC
// IaaS-robustness-nw_impairment
// 04-Verification-Pipeline-StabilityTest_Rebels / 500-Verification-pipeline-TeamCI-CNCS-Rebels (after CNCS adaptation)
// IaaS-stability-verdict
//
// from here the "exact" mask for UC has to be found so to get the unique element inside the file test-xray-dnd.txt:
//
// Deploy (Day 0)-BSF=DND-
// Deploy (Day 0 & Day 1)-SC=DND-33924
// Upgrade with Traffic-1.8.1-BSF=DND-
// Upgrade-1.8.1-SC=DND-33925
// Stability 1h after Deploy SEPP
// K8s Master_Restart-BSF=DND-
// K8s Worker_Restart-BSF=DND-

        SUMMARY_PKG_INFO ="(${CSAR_PKG})"
        SUMMARY_ADD_INFO =""
//        BASE_RELEASE = """${sh( returnStdout: true, script: "echo ${env.BASE_PKG} | sed 's/\\.0.*//' | sed 's/\\.25.*//' | cut -d '+' -f1 | cut -d '-' -f1")}""".trim() // eg. "1.7" or "1.8.1"
        BASE_RELEASE = """${sh( returnStdout: true, script: "echo ${env.BASE_PKG} | cut -d '+' -f1 | cut -d '-' -f1 | sed 's/\\.0\$/joker/' | sed 's/\\.25\$//' | sed 's/joker//'")}""".trim() // eg. "1.7" or "1.8.1"

//******************************** all scenarios should be extracted from here onwards ***********************
//************************************************************************************************************
        JIRA_TC_SUMMARY_CONTENT = ""

        if ("${UC}" == 'upgrade') // identify between upgrade or downgrade
        {
            UC = getAction()
        }
        if ("${UC}" == 'deploy')
        {
            if ("${env.TRAFFIC_RUN}" == 'true') // with traffic only Day 0 is used. In order to get the appropriate TC (depending on the release) extra fine tuning is needed
            {
                JIRA_TC_SUMMARY_CONTENT = "Deploy ${CNFs} with Day 0 using SC ${BASE_RELEASE}"
            }
            else
            {
                JIRA_TC_SUMMARY_CONTENT = "Deploy ${CNFs} with Day 0 & Day 1 using SC ${BASE_RELEASE}"
            }

            if ("${CNFs}" == 'cnDSC')// for cnDSC only Day 0 is used
            {
                JIRA_TC_SUMMARY_CONTENT = "Deploy ${CNFs} with Day 0"
            }
        }
        if ("${UC}" == 'undeploy')
        {
            UC = "Terminate"
            JIRA_TC_SUMMARY_CONTENT = "Terminate ${CNFs} using SC ${BASE_RELEASE}"
        }

        if ("${UC}" == 'upgrade')
        {
/*
            preparation for future XRay handling
            int upgrade_position_start = getReleasePosition(BASE_RELEASE)
            int upgrade_position_end = getReleasePosition(RELEASE)
            echo """The difference between releases ${BASE_RELEASE} and ${RELEASE} is of type N-${upgrade_position_end-upgrade_position_start}"""
*/
            SUMMARY_PKG_INFO ="to ${CSAR_PKG}"

            if ("${env.JIRA_DEPLOY_ID}" != 'null')
            {
                UC = "Performance"
                JIRA_TC_SUMMARY_CONTENT = "Performance Check ${CNFs} with Base SC ${BASE_RELEASE}"
            }
            else
            {
                JIRA_TC_SUMMARY_CONTENT = "ISSU ${CNFs} with Base SC ${BASE_RELEASE}"
            }

        }

        if ("${UC}" == 'rollback')
        {
            JIRA_TC_SUMMARY_CONTENT = "Rollback ${CNFs} to Target SC ${BASE_RELEASE}"
        }

        if ("${UC}" == 'downgrade')
        {
/*
            preparation for future XRay handling
            int upgrade_position_start = getReleasePosition(RELEASE)
            int upgrade_position_end = getReleasePosition(BASE_RELEASE)
            echo """The difference between releases ${RELEASE} and ${BASE_RELEASE} is of type N-${upgrade_position_end-upgrade_position_start}"""
*/
            JIRA_TC_SUMMARY_CONTENT = "Downgrade ${CNFs} to Target SC ${RELEASE}"
            CSAR_PKG = BASE_PKG // in case of downgrade the pkgs versions were swapped so to be able to understand the scenario. Now it is needed to swap again so to get the queries properly
            RELEASE = BASE_RELEASE // so that the "Fix Version" points to the correct release
        }

        if ("${LOG_JOB_NAME}".contains("robustness"))
        {
            switch (UC)
            {
                case [ 'nw_impairment' ]:
                    JIRA_TC_SUMMARY_CONTENT = "Network Impairment ${CNFs}"
                    break
                case [ 'worker_drain' ]:
                    JIRA_TC_SUMMARY_CONTENT = "Drain K8s Worker ${CNFs}"
                    break
                case [ 'worker_restart' ]:
                    JIRA_TC_SUMMARY_CONTENT = "K8s Worker Restart ${CNFs}"
                    break
                case [ 'master_restart' ]:
                    JIRA_TC_SUMMARY_CONTENT = "K8s Master Restart ${CNFs}"
                    break
                case [ 'TZ_change' ]:
                    JIRA_TC_SUMMARY_CONTENT = "TZC ${CNFs}"
                    break
                case [ 'pod_disturbance' ]:
                    JIRA_TC_SUMMARY_CONTENT = "Multi-Pod Disturbance ${CNFs}"
                    break
                default:
                    error("${UC} is not registered as RTC scenario. This is not expected !!! Please check.")
                    break
            } //switch
        }

        if ("${UC}" == 'scale')
        {
            JIRA_TC_SUMMARY_CONTENT = "Scale-out/Scale-in ${CNFs} Workloads"
        }

        if ("${UC}" == 'backup')
        {
            JIRA_TC_SUMMARY_CONTENT = "Backup and Restore ${CNFs}"
        }

        if ("${UC}" == 'disaster')
        {
            JIRA_TC_SUMMARY_CONTENT = "Disaster Recovery ${CNFs}"
        }

        // For terminate, rollback&downgrade, there is no specific TC for traffic, hence this marking will be setup on the summary content
        if ( ("${UC}" == 'downgrade') || ("${UC}" == 'rollback') || ("${UC}" == 'Terminate'))
        {
            if ("${env.TRAFFIC_RUN}" == 'true')
            {
                SUMMARY_ADD_INFO = " with Traffic"
            }
        }

        if ( ("${LOG_JOB_NAME}".contains("robustness")) || ("${UC}" == 'scale') || ("${UC}" == 'upgrade') || ("${UC}" == 'backup') || ("${UC}" == 'disaster')) // set proper mark for RTC scenarios, backup&restore, diaster&recovery, upgrade under stability so that the appropriate TC DND is taken for filling the jira issue
        {
            if ("${env.TRAFFIC_RUN}" == 'true')
            {
                JIRA_TC_SUMMARY_CONTENT = JIRA_TC_SUMMARY_CONTENT + " with Traffic"
            }
        }
        if ("${UC}" == 'Stability')
        {
            JIRA_TC_SUMMARY_CONTENT = "Stability ${STAB_DURATION} ${CNFs}"
            if ("${env.TRAFFIC_VERDICT_SCENARIO}".contains("TTL"))
            {
                JIRA_TC_SUMMARY_CONTENT = JIRA_TC_SUMMARY_CONTENT + TRAFFIC_VERDICT_SCENARIO
            }
        }
        if ("${UC}" == 'stability-verdict')
        {
            UC = "Traffic Verdict"
            JIRA_TC_SUMMARY_CONTENT = "Stability Verdict ${CNFs}"
            if ("${TRAFFIC_VERDICT_SCENARIO}".contains("TTL"))
            {
                JIRA_TC_SUMMARY_CONTENT = JIRA_TC_SUMMARY_CONTENT + " with reduced TTL for Internal Certificates"
            }
            JIRA_TC_SUMMARY_CONTENT = JIRA_TC_SUMMARY_CONTENT + " using SC ${BASE_RELEASE}"
            SUMMARY_ADD_INFO = """${sh( returnStdout: true, script: "echo ${TRAFFIC_VERDICT_SCENARIO} | sed 's/ TTL\$//'")}""".trim()
            SUMMARY_ADD_INFO = " " + SUMMARY_ADD_INFO
        }
        if ("${UC}".contains("TTL"))
        {
            JIRA_TC_SUMMARY_CONTENT = "Internal Certificate TTL Change ${CNFs}"
        }
//****************************** all scenarios should be extracted up to here **************************
//******************************************************************************************************

        JIRA_TC_SUMMARY_CONTENT = INITIAL_TAG + JIRA_TC_SUMMARY_CONTENT
        SUMMARY = JIRA_TC_SUMMARY_CONTENT+ " "+SUMMARY_PKG_INFO+ SUMMARY_ADD_INFO+ " at node " +IAAS_NODE+ " filed on " +CURRENT_DATE // So it would be a unique statement (even same pkg is deployed on the same node, as the date changes}
        if (("${env.TRAFFIC_TYPE}" != 'null')  && ("${SUMMARY}".contains("Traffic")))
        {
            SUMMARY = """${sh( returnStdout: true, script: "echo \"${SUMMARY}\" | sed 's/Traffic/Traffic via ${TRAFFIC_TYPE}/'")}""".trim()
        }
        if (("${UC}" == 'upgrade')  && ("${env.TRAFFIC_VERDICT_SCENARIO}".contains("TTL"))) //For upgrades with reduced TTL a mention should be done in the summary field
        {
            SUMMARY = """${sh( returnStdout: true, script: "echo \"${SUMMARY}\" | sed 's/${TRAFFIC_TYPE}/${TRAFFIC_TYPE} (and reduced TTL for Internal Certificates)/'")}""".trim()
        }
        if (("${UC}" == 'Performance')  && ("${env.TRAFFIC_VERDICT_SCENARIO}".contains("TTL"))) //For Performance Jira TCs under upgrade with reduced TTL, a mention should be done in the summary field
        {
            SUMMARY = """${sh( returnStdout: true, script: "echo \"${SUMMARY}\" | sed 's/to/(and reduced TTL for Internal Certificates) to/'")}""".trim()
        }
        // get the DND for the corresponding TEST_XRAY
        if ("${JIRA_TC_SUMMARY_CONTENT}" == "") // report error as no summary content has been found for this scenario
        {
            UNEXPECTED_ERROR="TC-ID-NOT-FOUND"
            error ("There is no logic implemented for handing the type of this scenario ULR: \"${LOG_JOB_NAME}\". Please include it and rebuild this run.")
        }
        JIRA_ISSUE_TYPE = "Test (Xray)"
        LABEL_CONTENT ="AND labels = \"VerificationPipe\""
        TEST_XRAY_DND = fetch_test_DND()

        // get the DND for the corresponding TEST_PLAN
        JIRA_TC_SUMMARY_CONTENT = "${TEST_PLAN}"
        JIRA_ISSUE_TYPE = "Test Plan"
        LABEL_CONTENT =""
        TEST_PLAN_DND = fetch_test_DND()

        comment_1="Detailed logs (console output and relevant files) can be found under the link: ${RUN_URL}\nThey all have been attached to this issue as well (with build number ${LOG_JOB_BUILD})."
        comment_2="\n\nThe run was part of the spinnaker execution ${SPINNAKER_ULR_START}${SPINNAKER_EXEC_ID}${SPINNAKER_ULR_END}."

        if (("${UC}" == 'Stability') && ("${env.JIRA_DEPLOY_ID}" != 'false')) /* there is a valid created jira exec run for the previous step of the scenario */
        {
            comment_3="\n\nThis stability scenario was run after the deployment performed on the node filed on the issue ${JIRA_ULR_START}${JIRA_DEPLOY_ID}"
        }
        else
        {
            comment_3=""
        }
        if ("${UC}" == 'Performance')
        {
            if ("${env.JIRA_DEPLOY_ID}" != 'false') /* there is a valid created jira exec run for the previous step of the scenario */
            {
                comment_1="This performance check scenario took place after the upgrade performed on the node filed on the issue ${JIRA_ULR_START}${JIRA_DEPLOY_ID}"
            }
            else
            {
                comment_1=" " /* blank space added so that subroutine does not crash because of "empty comment" */
            }
            comment_2=""
        }
        if ("${UC}" == 'Traffic Verdict')
        {
            if ("${env.JIRA_DEPLOY_ID}" != 'false') /* there is a valid created jira exec run for the previous step of the scenario */
            {
                if ("${TRAFFIC_VERDICT_SCENARIO}".contains("followed")) // scenario is B&R after an upggrade
                {
                    comment_1="This backup&restore with traffic was run after the upgrade performed on the node filed on the issue ${JIRA_ULR_START}${JIRA_DEPLOY_ID}"
                }
                else
                {
                    comment_1="This stability scenario was run after the deployment performed on the node filed on the issue ${JIRA_ULR_START}${JIRA_DEPLOY_ID}"
                }
            }
            else
            {
                comment_1=""
            }
            comment_2="\n\nThe entry traffic criteria and the traffic job console log (with build number ${TRAFFIC_BUILD}) have been attached to this issue as well.\n\nThe run was part of the spinnaker execution ${SPINNAKER_ULR_START}${SPINNAKER_EXEC_ID}${SPINNAKER_ULR_END}."
        }

        if (((("${UC}" == 'upgrade') && ("${env.TRAFFIC_TYPE}" !="DAFT"))|| ("${UC}" == 'scale') || ("${LOG_JOB_NAME}".contains("robustness")) ) && ("${env.TRAFFIC_RUN}" == 'true'))
        // in case of DAFT traffic all the info related to the console.log & tar files are stored on the traffic veridct jira prior to the upgrade. For CI traffic only the console.log is availbale and will be stored here as well
        {
            comment_3="\n\nThe traffic job console log (with build number ${TRAFFIC_BUILD}) is to be found under the attachments in case of need."
        }
        if (("${UC}" == 'downgrade') || ("${UC}" == 'rollback'))
        {
            if ("${env.JIRA_DEPLOY_ID}" != 'false') /* there is a valid created jira exec run for the previous step of the scenario */
            {
                comment_3="\n\nThis ${UC} was run after the upgrade performed on the node filed on the issue ${JIRA_ULR_START}${JIRA_DEPLOY_ID}"
            }
            else
            {
                comment_3=""
            }
        }
        if ("${UC}" == 'disaster')
        {
            if ("${env.JIRA_DEPLOY_ID}" != 'false') /* there is a valid created jira exec run for the previous step of the scenario */
            {
                comment_3="\n\nThe deployment which was present on the node (prior of executing this scenario) is filed on the issue ${JIRA_ULR_START}${JIRA_DEPLOY_ID}"
            }
            else
            {
                comment_3=""
            }
        }
        comment = comment_1 + comment_2 + comment_3
    }
}
def input_traffic_console_log()
{
    script
    {
        if ("${env.TRAFFIC_TYPE}" !="DAFT")
        {
            TRAFFIC_ULR_START = TRAFFIC_ULR_START_CI
            TRAFFIC_PATH = TRAFFIC_PATH_CI

            if ("${WEBHOOK}" =="eedjoz")
            {
                TRAFFIC_ULR_START = TRAFFIC_ULR_START_CI_CNCS
                TRAFFIC_PATH = TRAFFIC_PATH_CI_CNCS
            }
        }
        else
        {
            TRAFFIC_ULR_START = TRAFFIC_ULR_START_DAFT
            TRAFFIC_PATH = TRAFFIC_PATH_DAFT
        }
        LOG_FILE_NAME = "Console_output_" + IAAS_NODE + TRAFFIC_PATH + TRAFFIC_BUILD + ".log"
        RUN_URL = TRAFFIC_ULR_START + TRAFFIC_BUILD
    }
}
def fetch_console_log()
{
    script
    {
        withCredentials([usernameColonPassword(credentialsId: '4d8761fe-e925-4638-bf28-97744f277be6', variable: 'API_TOKEN')])
        {
            sh(script: "curl -u '$API_TOKEN' ${RUN_URL}/consoleText > ./${LOG_FILE_NAME}", returnStdout: true).trim()
        }
        HTML_PRESENT = """${sh( returnStdout: true, script: "file ./${LOG_FILE_NAME}")}""".trim()
        if (HTML_PRESENT.contains("HTML document")) // There is no console log found for that job link. This is not expected !!! Please check
        {
            UNEXPECTED_ERROR="yes"
            REASON="Assignment of console log/attachment of artifacts failed"
            error('There is no console log found for that job link. This is not expected !!! Please check.')  // Error is triggered so a notification mail is sent for information
        }
    }
}
def attach_console_log()
{
    script
    {

        echo 'Add traffic job console.log to attachments'
        jiraUploadAttachment idOrKey: "$TEST_EXEC_DND", file: "$LOG_FILE_NAME", site: 'DSC_Node_Development'
    }
}
def fetch_artifact()
{
    script
    {
        withCredentials([usernameColonPassword(credentialsId: '4d8761fe-e925-4638-bf28-97744f277be6', variable: 'API_TOKEN')])
        {
            sh(script: """curl -u '$API_TOKEN' -X PUT \"${RUN_URL}/artifact/${ARTIFACT_NAME}\" > ./${ARTIFACT_NAME}""", returnStdout: true).trim()
        }
        HTML_PRESENT = """${sh( returnStdout: true, script: "file ./${ARTIFACT_NAME}")}""".trim()
        if (HTML_PRESENT.contains("HTML document")) // Something went wrong; proceed to fail
        {
            echo """file ./${ARTIFACT_NAME} couldn't be accessed. No archive for it will be performed"""
            UNEXPECTED_ERROR="yes"
            REASON="Assignment of console log/attachment of artifacts failed"
            error('Errors while attaching artifacts. This is not expected !!! Please check.')  // Error is triggered so a notification mail is sent for information
        }
    }
}
def attach_artifact()
{
    script
    {

        jiraUploadAttachment idOrKey: "$TEST_EXEC_DND", file: "${ARTIFACT_NAME}", site: 'DSC_Node_Development'
    }
}
def fetch_artifact_ADP_logs()
{
    script
    {
        withCredentials([usernameColonPassword(credentialsId: '4d8761fe-e925-4638-bf28-97744f277be6', variable: 'API_TOKEN')])
        {
            sh(script: """curl -u '$API_TOKEN' -X PUT \"${COLLECT_ADP_LOGS_ARTIFACT}\" > ./${LOG_TAR_FILE_START}-ADP-logs-after-stability.tgz""", returnStdout: true).trim()
        }

        HTML_PRESENT = """${sh( returnStdout: true, script: "file ./${LOG_TAR_FILE_START}-ADP-logs-after-stability.tgz")}""".trim()
        if (HTML_PRESENT.contains("HTML document")) // Something went wrong; proceed to fail
        {
            echo """file ./${LOG_TAR_FILE_START}-ADP-logs-after-stability.tgz couldn't be accessed. No archive for it will be performed"""
            UNEXPECTED_ERROR="yes"
            REASON="Assignment of console log/attachment of artifacts failed"
            error('Errors while attaching artifacts. This is not expected !!! Please check.')  // Error is triggered so a notification mail is sent for information
        }
    }
}
def attach_artifact_ADP_logs()
{
    script
    {
        jiraUploadAttachment idOrKey: "$TEST_EXEC_DND", file: "${LOG_TAR_FILE_START}-ADP-logs-after-stability.tgz", site: 'DSC_Node_Development'
    }
}
def attach_artifact_html()
{
    script
    {
        withCredentials([usernameColonPassword(credentialsId: '4d8761fe-e925-4638-bf28-97744f277be6', variable: 'API_TOKEN')])
        {
            sh(script: """curl -u '$API_TOKEN' -X PUT \"${RUN_URL}/${CNFs}_20Stability_20test/*zip*/${CNFs}_20Stability_20test.zip\" > ./${LOG_TAR_FILE_START}-${CNFs}_jcatlogs.zip""", returnStdout: true).trim()

        }
        HTML_PRESENT = """${sh( returnStdout: true, script: "file ${LOG_TAR_FILE_START}-${CNFs}_jcatlogs.zip")}""".trim()
        if (HTML_PRESENT.contains("Zip archive data")) // There is a zip file and will be archived
        {
            jiraUploadAttachment idOrKey: "$TEST_EXEC_DND", file: "${LOG_TAR_FILE_START}-${CNFs}_jcatlogs.zip", site: 'DSC_Node_Development'
        }
        else
        {
            echo """no jcatlogs.zip file found for this scenario. No archive for it will be performed"""
        }
    }
}
def upload_artifact_to_SC()
{
    script
    {
        withCredentials([string(credentialsId: 'armseligic-armdocker-eiffelesc-identity-token-for-Smoke-CI', variable: 'ARTIFACTORY_TOKEN')])
        {
            sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ${ARTIFACT_NAME} ${ARTIFACTORY_PATH}/"""
        }
        echo 'Create a comment in the test execution about the upload in artifactory'
        comment_artifactory ="The troubleshooting logs inside the file called ${ARTIFACT_NAME} are stored in artifactory under the link ${ARTIFACTORY_PATH}/${ARTIFACT_NAME}"
        jiraAddComment idOrKey: "$TEST_EXEC_DND", comment: "$comment_artifactory", site: 'DSC_Node_Development'
    }
}
def upload_artifact_to_Lexicon()
{
    script
    {
        //withCredentials([usernameColonPassword(credentialsId: 'eiffelesc-armsero-token-credentials', variable: 'API_TOKEN')]) /* credentials access valid until 31.12.2023 */
        withCredentials([string(credentialsId: 'armserogic-selndocker-serodocker-eiffelesc-identity-token-for-Smoke-CI', variable: 'API_TOKEN')]) /* credentials access valid after 31.12.2023 */
        {
            //sh(script: "curl -u '$API_TOKEN' --upload-file ${ARTIFACT_NAME} ${ARTIFACTORY_PATH}/", returnStdout: true).trim() /* access type valid until 31.12.2023 */
            sh """curl -f -k -H "X-JFrog-Art-Api:$API_TOKEN" --upload-file ${ARTIFACT_NAME} ${ARTIFACTORY_PATH}/""" /* access type valid after 31.12.2023 */
        }
        echo 'Add the artifact to the yaml info file'
        sh """echo "- ${ARTIFACT_NAME}"	 >> ./run_info.yaml"""
    }
}
def finalize_info_file_Lexicon()
{
    script
    {
        // Insert the additional lines needed to the info file for Lexicon
        sh """sed -i '1s/^/artifacts:\\n/' run_info.yaml"""
        sh """sed -i "1s/^/end_timestamp: \\'${LOGS_END_TIME}\\'\\n/" run_info.yaml"""
        sh """sed -i "1s/^/start_timestamp: \\'${LOGS_START_TIME}\\'\\n/" run_info.yaml"""
        sh """sed -i '1s/^/status: ${TEST_EXEC_STATUS}\\n/' run_info.yaml"""
        sh """sed -i 's/PASS/SUCCEEDED/' run_info.yaml"""
        sh """sed -i 's/FAIL/FAILED/' run_info.yaml"""
        sh """echo "generated_Jira_ID: ${JIRA_INFO_LEXICON}"	 >> ./run_info.yaml"""

// Pipeline name construction (example):
// 1st sed: is the first part of the "LOGS_FOLDER"; example:   "IaaS_deploy_day0_day1_SC1.10.0_all_5341_succeeded" -> "IaaS_deploy_day0_day1_SC1.10.0_all" (same for all the builds with these characteristics).
// 2nd sed: is the removal of the release. As from our release to the next there will not be that "big" change (otherwise Lexion will be re-trained), we agreed not to pack the release in the pipeline id. example: -> "IaaS_deploy_day0_day1_all" (same for all the builds with these characteristics).
//

// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Cases being handled >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
// =========================================================================================================================

// The concrete masking to check against depends on the scenario in question. It is supposed ALL cases will start with IaaS_<case>_SCxxx *
// (1)
// Example: IaaS_deploy_day0_day1_SC1.13.25_bsf-scp-slf-sepp_12153_failed_continue
// Logic: IaaS_deploy_day0_day1_SC${(PKG_AT_DEPLOYMENT).split("\+")[0] }_${(trigger[  'parameters'  ][  'CNF_UNDER_TEST'  ]).split(" ")[0] }_${#stage("Deploy pkg without traffic")["context"]["buildNumber"]}_${((#stage("Deploy pkg without traffic")["status"]).toString()).toLowerCase()}
// Lexicon pipeline name: IaaS_deploy_day0_day1_bsf-scp-slf-sepp
// Status: in operation
// (2)
// Example: IaaS_upgrade_SC1.12.0_to_SC1.13.25_bsf-scp-slf-sepp_12153_failed_continue
// Logic: IaaS_upgrade_SC${(PKG_AT_DEPLOYMENT).split("\+")[0] }_to_SC${(CSAR_SUT).split("\+")[0] }_${(trigger[  'parameters'  ][  'CNF_UNDER_TEST'  ]).split(" ")[0] }_${#stage("Upgrade pkg under test")["context"]["buildNumber"]}_${((#stage("Upgrade pkg under test")["status"]).toString()).toLowerCase()}
// Lexicon pipeline name: ???
// Status: Logic included in 1 form; NOT activated
// (3)
// Example: IaaS_upgrade_(DAFT/CI)_traffic_SC1.12.0_to_SC1.13.25_bsf-scp-slf-sepp_12153_failed_continue
// Logic: IaaS_upgrade_${TRAFFIC_TYPE}_traffic_SC${(PKG_AT_DEPLOYMENT).split("\+")[0] }_to_SC${(CSAR_SUT).split("\+")[0] }_${(trigger[  'parameters'  ][  'CNF_UNDER_TEST'  ]).split(" ")[0] }_${#stage("Upgrade pkg under traffic")["context"]["buildNumber"]}_${((#stage("Upgrade pkg under traffic")["status"]).toString()).toLowerCase()}
// Lexicon pipeline name: ???
// Status: Logic included in 1 form; NOT activated
// (4)
// Example: IaaS_verdict_(DAFT/CI)_traffic_SC1.12.0_bsf-scp-slf-sepp_12153_failed_continue (right now for deploy-stability; it can be used as entry check for upgrade traffic; IN THIS CASE  I believe the stages names would be the same)
// Logic: IaaS_verdict_${TRAFFIC_TYPE}_traffic_SC${(PKG_AT_DEPLOYMENT).split("\+")[0] }_${(trigger[  'parameters'  ][  'CNF_UNDER_TEST'  ]).split(" ")[0] }_${#stage("Check on running traffic")["context"]["buildNumber"]}_${((#stage("Check on running traffic")["status"]).toString()).toLowerCase()}
// Lexicon pipeline name: IaaS_verdict_(DAFT/CI)_traffic_bsf-scp-slf-sepp
// Status: Logic included in 2 forms, in operation
// (5)
// Example: IaaS_stability_CI_SC1.12.0_scp-slf_12153_TRUE (TRUE occurs when stability failed and ADP logs are to be collected)
// Logic: IaaS_stability_${TRAFFIC_TYPE}_SC${(PKG_AT_DEPLOYMENT).split("\+")[0] }_${(trigger[  'parameters'  ][  'CNF_UNDER_TEST'  ]).split(" ")[0] }_${#stage(TRAFFIC_STAGE_NAME)["context"]["buildNumber"]}_${(#stage(TRAFFIC_STAGE_NAME)["context"]["TROUBLESHOOTING"]).replaceAll("TRUE","failed_continue").replaceAll("FALSE","succeeded")}
// Lexicon pipeline name: IaaS_stability_CI_scp-slf
// Status: Logic included in 1 form, in operation
// (6)
// Example:
// Logic:
// Lexicon pipeline name:
// Status:

// =========================================================================================================================
// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Activation sending logs to Lexicon >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
// =========================================================================================================================

// (1) Insert the logic for extracting LEXICON_PIPE_SCENARIO here down (otherwise it will stop with error)
// (2) Incorporate the new scenario into the variable SCENARIO_LEXICON_ELIGIBLE
// (3) Adjust the values for variables LOGS_FOR_LEXICON and LEXICON_TRIGGERING as required (eg equal to xxxx_FORCED or xxxx_VOLUNTARILY)
// (4) Incorporate variables LOGS_FOR_LEXICON and LEXICON_TRIGGERING in the Jira creation form
// (5) Fill the rest needed variables in the form (eg start time, end time, etc...)

// =========================================================================================================================
// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

        LEXICON_PIPE_SCENARIO = """${sh( returnStdout: true, script: "echo ${LOGS_FOLDER} | sed 's/IaaS_//' | sed 's/_SC.*_*\$//'")}""".trim()

        switch (LEXICON_PIPE_SCENARIO)
        {
            case [ 'deploy_day0_day1', 'verdict_CI_traffic', 'verdict_DAFT_traffic', 'stability_CI' ]:
                EXTRACT_LOGIC = "sed 's/_[0-9].*_.*\$//' | sed 's/SC.*_//'"
                break
            case [ 'deploy_day0', 'upgrade', 'upgrade_DAFT_traffic', 'upgrade_CI_traffic' ]:
                // to be defined as long as new scenarios are being considered
                break
            default:
                error("The structure for the naming of the LOGS_FOLDER for this current scenario: \"${LOGS_FOLDER}\" is not recognized under the registered patterns. This is not expected !!! Please check.")
                break
        } //switch

        PIPELINE_NAME = """${sh( returnStdout: true, script: "echo ${LOGS_FOLDER} | ${EXTRACT_LOGIC}")}""".trim()
        if ("${env.VARIANT}".contains("EVNFM"))
        {
            PIPELINE_NAME = """${sh( returnStdout: true, script: "echo ${PIPELINE_NAME} | sed 's/IaaS/IaaS_EVNFM/'")}""".trim()
        }
        sh """sed -i '1s/^/pipeline: ${PIPELINE_NAME}\\n/' run_info.yaml"""

        //withCredentials([usernameColonPassword(credentialsId: 'eiffelesc-armsero-token-credentials', variable: 'API_TOKEN')]) /* credentials access valid until 31.12.2023 */
        withCredentials([string(credentialsId: 'armserogic-selndocker-serodocker-eiffelesc-identity-token-for-Smoke-CI', variable: 'API_TOKEN')]) /* credentials access valid after 31.12.2023 */
        {
        //    sh(script: "curl -u '$API_TOKEN' --upload-file run_info.yaml ${ARTIFACTORY_PATH}/", returnStdout: true).trim() /* access type valid until 31.12.2023 */
            sh """curl -f -k -H "X-JFrog-Art-Api:$API_TOKEN" --upload-file run_info.yaml ${ARTIFACTORY_PATH}/""" /* access type valid after 31.12.2023 */
        }

// Check that the folder has been created inside the Lexicon repository
        //withCredentials([usernameColonPassword(credentialsId: 'eiffelesc-armsero-token-credentials', variable: 'API_TOKEN')]) /* credentials access valid until 31.12.2023 */
        withCredentials([string(credentialsId: 'armserogic-selndocker-serodocker-eiffelesc-identity-token-for-Smoke-CI', variable: 'API_TOKEN')]) /* credentials access valid after 31.12.2023 */
        {
            sh """ curl -H "X-JFrog-Art-Api:$API_TOKEN" \
            https://arm.sero.gic.ericsson.se/artifactory/api/search/aql \
            -d 'items.find({"repo":{"\$eq":"proj-lexicon-generic-local"}},{"type":{"\$eq":"folder"}},{"name":{"\$match":"'"${LOGS_FOLDER}"'"}})' \
            -H "content-type:text/plain" > ./FOLDER_SEARCH.txt"""
        }
        FOLDER_EXISTS = """${sh( returnStdout: true, script: "cat ./FOLDER_SEARCH.txt | grep ${LOGS_FOLDER} 2>/dev/null || true")}""".trim()
        if ( ! ("${FOLDER_EXISTS}".contains("${LOGS_FOLDER}"))) /* The folder was not created in Lexicon artifactory */
        {
            TRIGGER_LEXICON_ANALYSIS="no"
            def emailbody_job_failure_lexicon_1="\nthe folder with the logs for Lexicon was not uploaded to its artifactory. Please check."
            def emailbody_job_failure_lexicon_2="\n\nDetailed log for the jira issue creation jenkins job can be found under:\n${LOGS_LINK}/$BUILD_NUMBER/consoleFull"
            LIST_ADDRESSEES ="eedjoz@eed.ericsson.se"

            emailext body: "${emailbody_start}"+"${emailbody_job_failure_lexicon_1}"+"${emailbody_job_failure_lexicon_2}"+"${emailbody_end}",
            subject: "Folder with logs for Lexicon not uploaded to artifactory", to: "${LIST_ADDRESSEES}"
        }
    }
}
def getAction()
{
    script
    {
        i = 1 /* integer carrying the position of each digit for the semantic version of the pkg (major, minor, patch) */
        p = 5 /* integer carrying the power of 10 to apply to each digit. Eg: 1.12.0 -> 11200, 1.11.25 -> 11125 */
        BASE_PKG_ACCUM = 0
        CSAR_PKG_ACCUM = 0
        while ( i < 4 )
        {
            BASE_PKG_DIGIT = """${sh( returnStdout: true, script: "echo ${env.BASE_PKG} | cut -d '+' -f1 | cut -d '.' -f$i")}""".trim()
            CSAR_PKG_DIGIT = """${sh( returnStdout: true, script: "echo ${env.CSAR_PKG} | cut -d '+' -f1 | cut -d '.' -f$i")}""".trim()

            BASE_PKG_ACCUM = BASE_PKG_ACCUM + ( "${BASE_PKG_DIGIT}".toInteger() * (10**(p-i)) )
            CSAR_PKG_ACCUM = CSAR_PKG_ACCUM + ( "${CSAR_PKG_DIGIT}".toInteger() * (10**(p-i)) )
            i=i+1
            p=p-1
        }
        if ( CSAR_PKG_ACCUM > BASE_PKG_ACCUM )
        {
            ACTIVITY = "upgrade"
        }
        else
        {
            if ( CSAR_PKG_ACCUM < BASE_PKG_ACCUM )
            {
                ACTIVITY = "downgrade"
            }
            else // It is an intra release case, so the version itself has to be checked to understand whether it is an upgrade/downgrade
            {
                BASE_PKG_VERSION = """${sh( returnStdout: true, script: "echo ${env.BASE_PKG} | cut -d '+' -f2")}""".trim()
                CSAR_PKG_VERSION = """${sh( returnStdout: true, script: "echo ${env.CSAR_PKG} | cut -d '+' -f2")}""".trim()

                if ("${CSAR_PKG_VERSION}".toInteger() > "${BASE_PKG_VERSION}".toInteger())
                {
                    ACTIVITY = "upgrade"
                }
                else
                {
                    ACTIVITY = "downgrade"
                }
            }
        }
        return ACTIVITY
    }
}
def fetch_test_DND() // feed previously the fields JIRA_TC_SUMMARY_CONTENT and JIRA_ISSUE_TYPE
{
    script
    {
        echo """Searching in Jira the corresponding DND for issue type: \"${JIRA_ISSUE_TYPE}\" and summary: \"${JIRA_TC_SUMMARY_CONTENT}\""""
        def issue = jiraJqlSearch jql: """project = \"DSC Node Development\" AND issuetype = \"${JIRA_ISSUE_TYPE}\" ${LABEL_CONTENT} AND summary ~ \"\\\"${JIRA_TC_SUMMARY_CONTENT}\\\"\"""", site: 'DSC_Node_Development', failOnError: true
        def raw_data = issue.data.toString()
        writeFile(file: "test_data.txt", text: raw_data)
        sh """cat ./test_data.txt | tr "," \\\\n > ./test_data-one-line-structured.txt"""

        ITEMS_FOUND = """${sh(returnStdout: true, script: "cat ./test_data-one-line-structured.txt | grep total | head -1 | cut -d ':' -f2")}""".trim().toInteger() // It will store the amount of jira issues which contain the search
        // now additional logic is needed to find the exact match
        int RANKING=0
        int EXACT_MATCH_FOUND=0
        // establish a loop for all the items found to check whether any of them matches the complete search

        while ( ITEMS_FOUND > 0 )
        { // start from the higher value and continue decreasing
            POSSIBLE_SUMMARY = """${sh(returnStdout: true, script: "cat ./test_data-one-line-structured.txt | grep \"^ summary\" | cut -d ':' -f2 | head -$ITEMS_FOUND | tail -1")}""".trim()
            if ("${POSSIBLE_SUMMARY}" == "${JIRA_TC_SUMMARY_CONTENT}") // this element has exactly the same text as the summary we are looking for
            {
                    RANKING = ITEMS_FOUND       // mark the position of the item found
                    EXACT_MATCH_FOUND=EXACT_MATCH_FOUND+1   // mark how many exact matches there are (it should be only one)
            }
            ITEMS_FOUND = ITEMS_FOUND -1 // decrease the list to go to the previous element
        }
        // all elements have been scaned. THere should be only one match; otherwise error should be reported
        switch ((EXACT_MATCH_FOUND).toString())
        {
            case [ '0' ]:
                UNEXPECTED_ERROR="TC-ID-NOT-FOUND"
                REASON="Not unique Test XRay DND"
                error ("""No DND found in Jira for issue type: \"${JIRA_ISSUE_TYPE}\" and summary: \"${JIRA_TC_SUMMARY_CONTENT}\" !!! Please check. As a hint (in case it is already created): did you attach the label \"VerificationPipe\" to it?""")
                break
            case [ '1' ]:
                break
            default:
                UNEXPECTED_ERROR="TC-ID-NOT-FOUND"
                REASON="Not unique Test XRay DND"
                error ("""${EXACT_MATCH_FOUND} DNDs found in Jira for issue type: \"${JIRA_ISSUE_TYPE}\" and summary: \"${JIRA_TC_SUMMARY_CONTENT}\" !!! Please check.""")
                break
        } //switch

        ITEM_DND = """${sh(returnStdout: true, script: "cat ./test_data-one-line-structured.txt | grep key | head -$RANKING | tail -1 | cut -d ':' -f2")}""".trim()
        echo """${ITEM_DND} has been found as the jira issue type \"${JIRA_ISSUE_TYPE}\" which has as summary \"${JIRA_TC_SUMMARY_CONTENT}\""""
        return ITEM_DND
    }
}
def triggerRebuildJira()
{
    script
    {
        sh """curl "https://spinnaker-api.rnd.gic.ericsson.se/webhooks/webhook/IaaS_rebuild_Jira" -X POST -H "content-type: application/json" \
            -d ' { \
                    "FAILED_BUILD"              : "${LOGS_LINK}/$BUILD_NUMBER"                     , \
                    "TEST_PLAN"                 : "${eliminateNull(env.TEST_PLAN)}"                , \
                    "RUN_STATUS"                : "${eliminateNull(env.RUN_STATUS)}"               , \
                    "IAAS_NODE"                 : "${eliminateNull(env.IAAS_NODE)}"                , \
                    "CSAR_PKG"                  : "${eliminateNull(env.CSAR_PKG)}"                 , \
                    "BASE_PKG"                  : "${eliminateNull(env.BASE_PKG)}"                 , \
                    "CNF_UNDER_TEST"            : "${eliminateNull(env.CNF_UNDER_TEST)}"           , \
                    "RUN_URL"                   : "${eliminateNull(env.RUN_URL)}"                  , \
                    "SPINNAKER_EXEC_ID"         : "${eliminateNull(env.SPINNAKER_EXEC_ID)}"        , \
                    "JIRA_DEPLOY_ID"            : "${eliminateNull(env.JIRA_DEPLOY_ID)}"           , \
                    "COLLECT_ADP_LOGS_ARTIFACT" : "${eliminateNull(env.COLLECT_ADP_LOGS_ARTIFACT)}", \
                    "TRAFFIC_RUN"               : "${eliminateNull(env.TRAFFIC_RUN)}"              , \
                    "TRAFFIC_BUILD"             : "${eliminateNull(env.TRAFFIC_BUILD)}"            , \
                    "TRAFFIC_TYPE"              : "${eliminateNull(env.TRAFFIC_TYPE)}"             , \
                    "STAB_DURATION"             : "${eliminateNull(env.STAB_DURATION)}"            , \
                    "LOGS_TO_LEXICON"           : "${eliminateNull(env.LOGS_TO_LEXICON)}"          , \
                    "LOGS_START_TIME"           : "${eliminateNull(env.LOGS_START_TIME)}"          , \
                    "LOGS_END_TIME"             : "${eliminateNull(env.LOGS_END_TIME)}"            , \
                    "LOGS_FOLDER"               : "${eliminateNull(env.LOGS_FOLDER)}"              , \
                    "TRAFFIC_VERDICT_SCENARIO"  : "${eliminateNull(env.TRAFFIC_VERDICT_SCENARIO)}" , \
                    "VARIANT"                   : "${eliminateNull(env.VARIANT)}"                  , \
                    "ARTIFACT_LIST"             : "${eliminateNull(env.ARTIFACT_LIST)}"              \
                 } ' """
    }
}
def eliminateNull(inputValue)
{
   script
   {
       outputValue = """${sh( returnStdout: true, script: "echo ${inputValue} | sed 's/null//'")}""".trim()
       return outputValue
   }
}
def jira_query() //currently not being called upon, just to illustrate an example
{
    script
    {
        def issues = jiraJqlSearch jql: "project = \"DSC Node Development\" AND key = $TEST_EXEC_DND AND issuetype = 'test Execution' AND labels= VerificationPipe", fields: ["key"] , site: 'DSC_Node_Development'

        def raw_data = issues.data.toString()
        writeFile(file: "query-plugin.data", text: raw_data)
        archiveArtifacts allowEmptyArchive: true, artifacts: 'query-plugin.data', onlyIfSuccessful: false
    }
}
def getReleasePosition (release) //currently calling (4 times) is commented it out (preparation for future XRay handling based on N minus delta cases)
{
    script
    {
        pointer=0
        array_length = "${releases_array.size()}".toInteger()
        while ( pointer < array_length )
        {
            if ( "${releases_array.get(pointer)}" == release )
            {
                return pointer
            }
            else
            {
                pointer=pointer+1
            }
        }
        error ("Release ${release} is not stored inside the array information of releases.")
    }
}