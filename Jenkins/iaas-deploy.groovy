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
        timestamps()
    }
    environment
    {
        ARTIFACTORY_TOKEN = "Token for armdocker" /* iaas: not shown in the initial form as always gets this value */
//      variables from initial form
        IAAS_NODE = "${IAAS_NODE}"
        CSAR_PACKAGE_VERSION = "${CSAR_PACKAGE_VERSION}"
        NW_CONFIG_FILE = "${NW_CONFIG_FILE}"
        ERIC_SC_VALUES_YAML_FILE = "${ERIC_SC_VALUES_YAML_FILE}"
        DEPLOYMENT_VARIANT ="${DEPLOYMENT_VARIANT}"
        EXTRA_CONFIG_YAML_FILE = "${EXTRA_CONFIG_YAML_FILE}"
        ENABLED_CNF = "${ENABLED_CNF}"
        REDUCED_RESOURCES = "${REDUCED_RESOURCES}"
        CLEAN_REGISTRY ="${CLEAN_REGISTRY}"
        IGNORE_ALARMS = "${IGNORE_ALARMS}"
        SKIP_DEPLOY_UPGRADE_CRD = "${SKIP_DEPLOY_UPGRADE_CRD}"
        SKIP_UNDEPLOY_CRD = "${SKIP_UNDEPLOY_CRD}"
        SKIP_PRE_HEALTHCHECK = "${SKIP_PRE_HEALTHCHECK}"
        SKIP_POST_HEALTHCHECK = "${SKIP_POST_HEALTHCHECK}"
        USERS_PROVISIONING = "${USERS_PROVISIONING}"
        USERS_PROVISIONING_TEXT = " "
        CONFIGURATION_PROVISIONING = "${CONFIGURATION_PROVISIONING}"
        CONFIGURATION_PROVISIONING_TEXT = " "
        CONFIGURATION_PROVISIONING_DIR = "./Jenkins/PipeConfig"
        PASSWORD_EXPIRE = "${PASSWORD_EXPIRE}"
        GENERATE_CERTIFICATES = "${GENERATE_CERTIFICATES}"
        GEORED_CERTIFICATES = "${GEORED_CERTIFICATES}"
        GENERATE_CERTIFICATES_TEXT = " "
        COLLECT_ADP_LOGS_AT_SUCCESS = "${COLLECT_ADP_LOGS_AT_SUCCESS}"
        SIMULATORS_ON_SPECIFIC_WORKER = "${SIMULATORS_ON_SPECIFIC_WORKER}"
        MAIL = "${MAIL}"
        REDUCED_RESOURCES_DIR ="./csar/external"
        DAFT_DIR = "./daft"
        NW_CONFIG_FILE_DIR = "${DAFT_DIR}/network_config_files"
        DAFT_LOGS_DIR = "/home/eccd/workspaces"
        TERMINATE_METHOD="${TERMINATE_METHOD}"
        CLEANUP_RESOURCES="${CLEANUP_RESOURCES}"
        STACK_TYPE="${STACK_TYPE}"
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

                wrap([$class: 'BuildUser'])
                {
                    script
                    {
                        triggering_user = """${sh( returnStdout: true, script: "echo ${BUILD_USER_ID}")}""".trim()
                    }
                }
                // set displayed description to "build slave, node, csar pkg version, cnfs, triggering_user"
                script
                {
                    currentBuild.description = "${env.NODE_NAME}, ${IAAS_NODE}, ${CSAR_PACKAGE_VERSION}, ${NF}, ${triggering_user}"
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
                creationAndTransfer()
            }
        }
        stage('Remove node labels and taints')
        {
            when
            {
                allOf
                {                                                                                                   // For second deployments of UCC not executed, as:
                    not { environment name: 'DEPLOYMENT_VARIANT', value: 'UCC1_UCC2' }                              //  UCC2 leaves the tools WKR intact
                    not { environment name: 'DEPLOYMENT_VARIANT', value: 'UCC1_SingleWorker_UCC2_SingleWorker' }    //  UCC2_SingleWorker leaves additionally ucc1 WKR intact
                }
            }
            steps
            {
                script
                {
                    labels_array = ["tools","deployment1","deployment2"]
                    for (i=0; i<3; i++)
                    {
                        CURRENT_LABEL =labels_array.get(i)
                        echo "Removing any node labels and taints type $CURRENT_LABEL on the node"
                        removeLabel(CURRENT_LABEL)
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
                            def emailbody_failed_2="\nDetailed logs for this part can be found on the director of node ${IAAS_NODE}"
                            def emailbody_failed_3=" (IP address of director: ${NODE_DIRECTOR_IP}) under the folder: ${LOGS_FOLDER_REMOVE_LABELS_TAINTS}"

                            emailext body: "${emailbody_start}"+"${emailbody_failed_1}"+"${emailbody_failed_2}"+
                            "${emailbody_failed_3}"+"${emailbody_end}",
                            subject: "Removal of existing labels and taints type $CURRENT_LABEL during automatic IaaS undeployment failed", to: "${env.MAIL}"
                        }
                    }
                }
            }
        }
        stage('Trigger additional ns deletion')
        {
            when
            {
                allOf
                {                                                                                                   // For second deployments of UCC not executed, as:
                    not { environment name: 'DEPLOYMENT_VARIANT', value: 'UCC1_UCC2' }                              //  UCC2 leaves the tools WKR intact
                    not { environment name: 'DEPLOYMENT_VARIANT', value: 'UCC1_SingleWorker_UCC2_SingleWorker' }    //  UCC2_SingleWorker leaves additionally ucc1 WKR intact
                }
            }
            steps
            {
                script
                {
                    ns_array = ["tools","ucc2"] /* for future design include "geo2" */
                    for (i=0; i<2; i++)
                    {
                        TARGET_NS =ns_array.get(i)
                        echo "Preventive cleanup of possible deployment on namespace ${DAFT_NS}-${TARGET_NS}"
                        removeAdditionalNS(TARGET_NS)
                    }
                }
            }
            post
            {
                always
                {
                    script
                    {
                        LOGS_FOLDER_DELETE_NS = """${sh( returnStdout: true,
                            script: "ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP} 'find ${DAFT_LOGS_DIR} -type d -name *${JOB_BASE_NAME}-${BUILD_NUMBER}_Delete-ns-${DAFT_NS}-${TARGET_NS}*'"
                        )}""".trim()
                    }
                }
                failure
                {
                    script
                    {
                        if ("${env.MAIL}" != 'null')
                        {
                            def emailbody_undeploy_failed_1="\nthe automatic deletion for namespace ${DAFT_NS}-${TARGET_NS} via DAFT has failed."
                            def emailbody_undeploy_failed_2="\nDetailed logs can be found on the director of node ${IAAS_NODE}"
                            def emailbody_undeploy_failed_3=" (IP address of director: ${NODE_DIRECTOR_IP}) under the folder: ${LOGS_FOLDER_DELETE_NS}"

                            emailext body: "${emailbody_start}"+"${emailbody_undeploy_failed_1}"+"${emailbody_undeploy_failed_2}"+
                            "${emailbody_undeploy_failed_3}"+"${emailbody_end}",
                            subject: "Automatic deletion for namespace ${DAFT_NS}-${TARGET_NS} failed", to: "${env.MAIL}"
                        }
                    }
                }
            }
        }
        stage('Trigger undeployment')
        {
            when
            {
                allOf
                {                                                                                                   // For second deployments of UCC not executed, as:
                    not { environment name: 'DEPLOYMENT_VARIANT', value: 'UCC1_UCC2' }                              //  UCC2 leaves the tools WKR intact
                    not { environment name: 'DEPLOYMENT_VARIANT', value: 'UCC1_SingleWorker_UCC2_SingleWorker' }    //  UCC2_SingleWorker leaves additionally ucc1 WKR intact
                }
            }
            steps
            {
                script
                {
                    if ("${DEPLOYMENT_VARIANT}".contains("EVNFM"))
                    {
                        echo "Check if evnfm termination is requested"
                        if ("${TERMINATE}" == 'Yes')
                        {
                            echo "Terminate deployment from jenkins on namespace ${DAFT_NS}"
                            sh "${DAFT_DIR}/expect/bin/send_command_to_ssh.exp --timeout=1800 --user=eccd --ip=${NODE_DIRECTOR_IP} --password='notneeded' --stop-on-error \
                            --command='perl ${DAFT_NS_DIR}/perl/bin/execute_playlist.pl -p 206_EVNFM_Terminate_SC -c no -v INPUT_AVAILABLE=no \
                            -n ${DAFT_NS_DIR}/${NW_CONFIG_FILE_NAME} ${NW_CONFIG_OPTION} \
                            -j ${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}_Terminate_${APP} -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=yes -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=yes \
                            -v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=yes -v COLLECT_LOGS_AT_SUCCESS=${COLLECT_ADP_LOGS_AT_SUCCESS} \
                            -v TERMINATE_METHOD=${TERMINATE_METHOD} -v STACK_TYPE=${STACK_TYPE} -v CLEANUP_RESOURCES=${CLEANUP_RESOURCES} \
                            -w ${DAFT_LOGS_DIR}' "
                        }
                    }
                    else
                    {
                        echo "Cleanup any possible existing deployment from jenkins on namespace ${DAFT_NS}"
                        sh "${DAFT_DIR}/expect/bin/send_command_to_ssh.exp --timeout=1800 --user=eccd --ip=${NODE_DIRECTOR_IP} --password='notneeded' --stop-on-error \
                        --command='perl ${DAFT_NS_DIR}/perl/bin/execute_playlist.pl -p 003_Undeploy_SC -c no -v INPUT_AVAILABLE=no -n ${DAFT_NS_DIR}/${NW_CONFIG_FILE_NAME} ${NW_CONFIG_OPTION} \
                        -j ${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}_Undeploy_${APP} -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=yes -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=yes \
                        -v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=yes -v COLLECT_LOGS_AT_SUCCESS=${COLLECT_ADP_LOGS_AT_SUCCESS} -v SKIP_UNDEPLOY_CRD=${SKIP_UNDEPLOY_CRD} \
                        -v CLEAN_REGISTRY=${CLEAN_REGISTRY} -w ${DAFT_LOGS_DIR}' "
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
                            def emailbody_undeploy_failed_1="\nthe automatic clean up of the existing deployment of jenkins (namespace ${DAFT_NS}) via DAFT has failed."
                            def emailbody_undeploy_failed_2="\nDetailed logs regarding the undeployment can be found on the director of node ${IAAS_NODE}"
                            def emailbody_undeploy_failed_3=" (IP address of director: ${NODE_DIRECTOR_IP}) under the folder: ${LOGS_FOLDER_UNDEPLOY}"

                            emailext body: "${emailbody_start}"+"${emailbody_undeploy_failed_1}"+"${emailbody_undeploy_failed_2}"+
                            "${emailbody_undeploy_failed_3}"+"${emailbody_end}",
                            subject: "Cleanup of an existing deployment (${APP}) during automatic IaaS deployment failed", to: "${env.MAIL}"
                        }
                    }
                }
            }
        }
        stage('Add node labels and taints')
        {
            when
            {
                anyOf
                {
                    environment name: 'SIMULATORS_ON_SPECIFIC_WORKER', value: 'yes'
                    environment name: 'DEPLOYMENT_VARIANT', value: 'UCC1_SingleWorker'                   //reserve worker#3 for UCC1
                    environment name: 'DEPLOYMENT_VARIANT', value: 'UCC1_SingleWorker_UCC2_SingleWorker' //reserve worker#4 for UCC2
                }
            }
            steps
            {
                script
                {
                    if ("${SIMULATORS_ON_SPECIFIC_WORKER}" == "yes")
                    {
                        CURRENT_LABEL="tools"
                        CURRENT_WORKER_POSITION="0" //Use the default logic in DAFT: the first 2 workers are reserved for tools
                        CURRENT_NODE_TAINT ="NoSchedule"
                        echo "Add node labels and taints type $CURRENT_LABEL on the node"
                        addLabel(CURRENT_WORKER_POSITION, CURRENT_LABEL, CURRENT_NODE_TAINT)
                    }
                    if ("${DEPLOYMENT_VARIANT}".contains("SingleWorker"))
                    {
                        CURRENT_NODE_TAINT ="PreferNoSchedule"
                        switch (DEPLOYMENT_VARIANT)
                        {
                            case ['UCC1_SingleWorker']:
                                CURRENT_LABEL="deployment1"
                                CURRENT_WORKER_POSITION="3"
                                break
                            case ['UCC1_SingleWorker_UCC2_SingleWorker']:
                                CURRENT_LABEL="deployment2"
                                CURRENT_WORKER_POSITION="4"
                                break
                            default:
                                error("There is no logic for the DEPLOYMENT_VARIANT \"$DEPLOYMENT_VARIANT\"; please check.")
                                break
                        }
                        echo "Add node labels and taints type $CURRENT_LABEL on the node"
                        addLabel(CURRENT_WORKER_POSITION, CURRENT_LABEL, CURRENT_NODE_TAINT)
                    }
                }
            }
            post
            {
                always
                {
                    script
                    {
                        LOGS_FOLDER_ADD_LABELS_TAINTS = """${sh( returnStdout: true,
                            script: "ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP} 'find ${DAFT_LOGS_DIR} -type d -name *${JOB_BASE_NAME}-${BUILD_NUMBER}_Add_Node_Label_${CURRENT_LABEL}*'"
                        )}""".trim()
                    }
                }
                failure
                {
                    script
                    {
                        if ("${env.MAIL}" != 'null')
                        {
                            def emailbody_failed_1="\nthe automatic addition of node labels and taints type $CURRENT_LABEL via DAFT has failed."
                            def emailbody_failed_2="\nDetailed logs regarding this failure can be found on the director of node ${IAAS_NODE}"
                            def emailbody_failed_3=" (IP address of director: ${NODE_DIRECTOR_IP}) under the folder: ${LOGS_FOLDER_ADD_LABELS_TAINTS}"

                            emailext body: "${emailbody_start}"+"${emailbody_failed_1}"+"${emailbody_failed_2}"+
                            "${emailbody_failed_3}"+"${emailbody_end}",
                            subject: "Addition of node labels and taints type $CURRENT_LABEL during automatic IaaS deployment failed", to: "${env.MAIL}"
                        }
                    }
                }
            }
        }
        stage('Trigger deployment')
        {
            steps
            {
                echo "${TRIGGER_MSG}"

                sh "${DAFT_DIR}/expect/bin/send_command_to_ssh.exp --timeout=3600 --user=eccd --ip=${NODE_DIRECTOR_IP} --password='notneeded' --stop-on-error \
                --command='perl ${DAFT_NS_DIR}/perl/bin/execute_playlist.pl -p ${USE_PLAYLIST} -c no -v INPUT_AVAILABLE=no -v SOFTWARE_DIR=/home/eccd/download/${CSAR_PACKAGE_VERSION}/ \
                -n ${DAFT_NS_DIR}/${NW_CONFIG_FILE_NAME} ${NW_CONFIG_OPTION} ${ADD_CNF_CMD} ${ADD_YAML_FILE_CMD} \
                -j ${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}_Deploy_${NF}_${CSAR_PACKAGE_VERSION} \
                -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=yes -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=yes -v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=yes -v COLLECT_LOGS_AT_SUCCESS=${COLLECT_ADP_LOGS_AT_SUCCESS} -v SKIP_DEPLOY_UPGRADE_CRD=${SKIP_DEPLOY_UPGRADE_CRD} \
                -v IGNORE_ALARMS=${IGNORE_ALARMS} -v SKIP_PRE_HEALTHCHECK=${SKIP_PRE_HEALTHCHECK} -v SKIP_POST_HEALTHCHECK=${SKIP_POST_HEALTHCHECK} -v SPECIAL_IDENTIFIER=${DAFT_NS} \
                -w ${DAFT_LOGS_DIR}' "
            }
            post
            {
                always
                {
                    script
                    {
                        LOGS_FOLDER_DEPLOY = """${sh( returnStdout: true,
                            script: "ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP} 'find ${DAFT_LOGS_DIR} -type d -name *${JOB_BASE_NAME}-${BUILD_NUMBER}_Deploy*'"
                        )}""".trim()
                    }
                }
                failure
                {
                    script
                    {
                        if ("${env.MAIL}" != 'null')
                        {
                            def emailbody_deploy_failed_1="\nthe automatic deployment via DAFT of the csar package called ${SC_NAME}-${CSAR_PACKAGE_VERSION}.csar on node ${IAAS_NODE} has failed."
                            def emailbody_deploy_failed_2="\nDetailed logs regarding the deployment can be found on the director of node ${IAAS_NODE}"
                            def emailbody_deploy_failed_3=" (IP address of director: ${NODE_DIRECTOR_IP}) under the folder: ${LOGS_FOLDER_DEPLOY}"

                            emailext body: "${emailbody_start}"+"${emailbody_deploy_failed_1}"+"${emailbody_deploy_failed_2}"+
                            "${emailbody_deploy_failed_3}"+"${emailbody_end}",
                            subject: "Deployment of a csar package (${APP}) during automatic IaaS deployment failed", to: "${env.MAIL}"
                        }
                    }
                }
            }
        }
        stage('Trigger users provisioning')
        {
            when { environment name: 'USERS_PROVISIONING', value: 'yes' }
            steps
            {
                echo 'Trigger the users provisioning after the deployment'
                sh "${DAFT_DIR}/expect/bin/send_command_to_ssh.exp --timeout=1800 --user=eccd --ip=${NODE_DIRECTOR_IP} --password='notneeded' --stop-on-error \
                --command='perl ${DAFT_NS_DIR}/perl/bin/execute_playlist.pl -p 005_User_Management -c no -v INPUT_AVAILABLE=no -v PASSWORD_EXPIRE=${PASSWORD_EXPIRE} \
                -n ${DAFT_NS_DIR}/${NW_CONFIG_FILE_NAME} ${NW_CONFIG_OPTION} -j ${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}_Users_${NF}_${CSAR_PACKAGE_VERSION} \
                -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=yes -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=yes -v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=yes -v COLLECT_LOGS_AT_SUCCESS=${COLLECT_ADP_LOGS_AT_SUCCESS} -w ${DAFT_LOGS_DIR}' "
            }
            post
            {
                always
                {
                    script
                    {
                        LOGS_FOLDER_USERS = """${sh( returnStdout: true,
                        script: "ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP} 'find ${DAFT_LOGS_DIR} -type d -name *${JOB_BASE_NAME}-${BUILD_NUMBER}_Users*'")}""".trim()

                        USERS_PROVISIONING_TEXT = """${sh( returnStdout: true,
                        script: "echo 'Detailed logs for the user provisioning part can be found on the director of node ${IAAS_NODE} (IP address of director: ${NODE_DIRECTOR_IP}) under the folder: ${LOGS_FOLDER_USERS}'")}""".trim()
                    }
                }
                failure
                {
                    script
                    {
                        if ("${env.MAIL}" != 'null')
                        {
                            def emailbody_users_failed_1="\nan error has ocurred while creating users via DAFT for the csar package called"
                            def emailbody_users_failed_2=" ${SC_NAME}-${CSAR_PACKAGE_VERSION}.csar on node ${IAAS_NODE}."
                            def emailbody_users_failed_3="\n${USERS_PROVISIONING_TEXT}"

                            emailext body: "${emailbody_start}"+"${emailbody_users_failed_1}"+"${emailbody_users_failed_2}"+
                            "${emailbody_users_failed_3}"+"${emailbody_end}",
                            subject: "Users provisioning (${APP}) during automatic IaaS deployment failed", to: "${env.MAIL}"
                        }
                    }
                }
            }
        }
        stage('Trigger certificates generation & loading')
        {
            when
            {
                allOf
                {
                    environment name: 'GENERATE_CERTIFICATES', value: 'yes'
                    not { environment name: 'ENABLED_CNF', value: 'dsc' }
                }
            }
            steps
            {
                echo 'Trigger certificates generation & loading'
                script
                {
                    // Concatenate the certificates to generate (right now hard coded)
                    CERTIFICATES_TO_GENERATE = "rootca,scpmgr,scpwrk,seppmgr,seppwrk,nbi,diameter,transformer"
                }
                sh "${DAFT_DIR}/expect/bin/send_command_to_ssh.exp --timeout=3600  --user=eccd --ip=${NODE_DIRECTOR_IP} --password='notneeded' \
                --stop-on-error --command='perl  ${DAFT_NS_DIR}/perl/bin/execute_playlist.pl -p 102_Supreme_Certificate_Management -c no \
                -n ${DAFT_NS_DIR}/${NW_CONFIG_FILE_NAME} ${NW_CONFIG_OPTION} -v GENERATE_CERTIFICATES=yes -v CERTIFICATES_TO_GENERATE=${CERTIFICATES_TO_GENERATE} \
                -v NAMESPACE=${DAFT_NS} -v INSTALL_CERTIFICATES=yes -v CERTIFICATE_DIRECTORY=/home/eccd/download/${CSAR_PACKAGE_VERSION}/certificates -v CERTIFICATE_VALIDITY_DAYS=7200 \
                -j ${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}_Certificates_${NF}_${CSAR_PACKAGE_VERSION} -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=yes \
                -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=yes -v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=yes -v INPUT_AVAILABLE=no -v IGNORE_ALARMS=no \
                -v SKIP_PRE_HEALTHCHECK=no -v SKIP_POST_HEALTHCHECK=no -v SOFTWARE_DIR=/home/eccd/download/${CSAR_PACKAGE_VERSION} \
                -v COLLECT_LOGS_AT_SUCCESS=${COLLECT_ADP_LOGS_AT_SUCCESS} -w ${DAFT_LOGS_DIR}' "
            }
            post
            {
                always
                {
                    script
                    {
                        LOGS_FOLDER_CERTIFICATES = """${sh( returnStdout: true,
                        script: "ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP} 'find ${DAFT_LOGS_DIR} -type d -name *${JOB_BASE_NAME}-${BUILD_NUMBER}_Certificates*'")}""".trim()

                        GENERATE_CERTIFICATES_TEXT = """${sh( returnStdout: true,
                        script: "echo 'Detailed logs for the certificates generation & loading part can be found on the director of node ${IAAS_NODE} (IP address of director: ${NODE_DIRECTOR_IP}) under the folder: ${LOGS_FOLDER_CERTIFICATES}'")}""".trim()
                    }
                }
                failure
                {
                    script
                    {
                        if ("${env.MAIL}" != 'null')
                        {
                            def emailbody_certificates_failed_1="\nan error has ocurred while generating certificates via DAFT for the csar package called"
                            def emailbody_certificates_failed_2=" ${SC_NAME}-${CSAR_PACKAGE_VERSION}.csar on node ${IAAS_NODE}."
                            def emailbody_certificates_failed_3="\n${GENERATE_CERTIFICATES_TEXT}"

                            emailext body: "${emailbody_start}"+"${emailbody_certificates_failed_1}"+"${emailbody_certificates_failed_2}"+
                            "${emailbody_certificates_failed_3}"+"${emailbody_end}",
                            subject: "Certificates generation & loading (${APP}) during automatic IaaS deployment failed", to: "${env.MAIL}"
                        }
                    }
                }
            }
        }
        stage('Trigger configuration provisioning')
        {
            when
            {
                allOf
                {
                    environment name: 'CONFIGURATION_PROVISIONING', value: 'yes'
                    not { environment name: 'ENABLED_CNF', value: 'dsc' }
                }
            }
            steps
            {
                getConfigFileNames()
                echo 'Execute playlist to load configuration files'
                sh """
                    ${DAFT_DIR}/expect/bin/send_command_to_ssh.exp --timeout=1800 --user=eccd --ip=${NODE_DIRECTOR_IP} --password='notneeded' --stop-on-error \
                    --command='perl ${DAFT_NS_DIR}/perl/bin/execute_playlist.pl -p 004_Config_Management -c no -v INPUT_AVAILABLE=no -v PASSWORD_EXPIRE=${PASSWORD_EXPIRE} -n ${DAFT_NS_DIR}/${NW_CONFIG_FILE_NAME} ${NW_CONFIG_OPTION} \
                    -j ${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}_Configure_${NF}_${CSAR_PACKAGE_VERSION} ${CMD_FOR_FILES} \
                    -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=yes -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=yes -v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=yes -v COLLECT_LOGS_AT_SUCCESS=${COLLECT_ADP_LOGS_AT_SUCCESS} -w ${DAFT_LOGS_DIR}';
                """
            }
            post
            {
                always
                {
                    script
                    {
                        LOGS_FOLDER_CONFIGURATION = """${sh( returnStdout: true,
                        script: "ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP} 'find ${DAFT_LOGS_DIR} -type d -name *${JOB_BASE_NAME}-${BUILD_NUMBER}_Configure*'")}""".trim()

                        CONFIGURATION_PROVISIONING_TEXT = """${sh( returnStdout: true,
                        script: "echo 'Detailed logs for the configuration provisioning part can be found on the director of node ${IAAS_NODE} (IP address of director: ${NODE_DIRECTOR_IP}) under the folder: ${LOGS_FOLDER_CONFIGURATION}'")}""".trim()
                    }
                }
                failure
                {
                    script
                    {
                        if ("${env.MAIL}" != 'null')
                        {
                            def emailbody_config_failed_1="\nan error has ocurred while loading configuration via DAFT for the csar package called"
                            def emailbody_config_failed_2=" ${SC_NAME}-${CSAR_PACKAGE_VERSION}.csar on node ${IAAS_NODE}."
                            def emailbody_config_failed_3="\n${CONFIGURATION_PROVISIONING_TEXT}"

                            emailext body: "${emailbody_start}"+"${emailbody_config_failed_1}"+"${emailbody_config_failed_2}"+
                            "${emailbody_config_failed_3}"+"${emailbody_end}",
                            subject: "Configuration loading (${APP}) during automatic IaaS deployment failed", to: "${env.MAIL}"
                        }
                    }
                }
            }
        }
        stage('Upload DAFT to artifactory & create properties file')
        {
            steps
            {
                withCredentials([string(credentialsId: 'armseligic-armdocker-eiffelesc-identity-token-for-Smoke-CI', variable: 'ARTIFACTORY_TOKEN')])
                {
                    sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" \\
                    --upload-file ${DAFT_DIR}/${ESC_PACKAGE_NAME} \\
                    https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/esc-daft/${ESC_PACKAGE_NAME}"""
                }
                sh """echo "DAFT_PKG_STORED_ARTIFACTORY=${ESC_PACKAGE_NAME}"	 > ./parameters.properties"""

                script
                {
                    if ("${triggering_user}" == "eiffelesc")
					{
// not valid any more as the swap from dsc (non-cncs) -> cnDSC has been done                        if ( ("${ENABLED_CNF}" == "dsc") && (getPackageType() == "non-cncs")) // For pure cnDSC (non-cncs pkg) design base applies
                        if  ("${ENABLED_CNF}" == "cnDSC")  // For pure cnDSC (non-cncs pkg) design base applies
                        {
							stabilityData_cnDSC()
						}
						else
						{
							stabilityData()
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
                        def emailbody_job_finished_1="\nthe automatic deployment via DAFT of the csar package called ${SC_NAME}-${CSAR_PACKAGE_VERSION}.csar"
                        def emailbody_job_finished_2=" on node ${IAAS_NODE} has concluded.\nDetailed logs for the undeployment part can be found on the"
                        def emailbody_job_finished_3=" director of node ${IAAS_NODE} (IP address of director: ${NODE_DIRECTOR_IP}) under the folder:"
                        def emailbody_job_finished_4=" ${LOGS_FOLDER_UNDEPLOY}\nDetailed logs for the deployment part can be found on the"
                        def emailbody_job_finished_5=" director of node ${IAAS_NODE} (IP address of director: ${NODE_DIRECTOR_IP}) under the folder:"
                        def emailbody_job_finished_6=" ${LOGS_FOLDER_DEPLOY}\n${USERS_PROVISIONING_TEXT}\n${CONFIGURATION_PROVISIONING_TEXT}"
                        def emailbody_job_finished_7="\n${GENERATE_CERTIFICATES_TEXT}\nDetailed logs for the overall deployment jenkins job"
                        def emailbody_job_finished_8=" can be found under: ${LOGS_LINK}/$BUILD_NUMBER/consoleFull"

                        emailext body: "${emailbody_start}"+"${emailbody_job_finished_1}"+"${emailbody_job_finished_2}"+"${emailbody_job_finished_3}"+
                        "${emailbody_job_finished_4}"+"${emailbody_job_finished_5}"+"${emailbody_job_finished_6}"+"${emailbody_job_finished_7}"+
                        "${emailbody_job_finished_8}"+"${emailbody_end}", subject: "Automatic IaaS deployment (${APP}) concluded", to: "${env.MAIL}"
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

            sh """echo "parameters.properties"   > ./names_array_split.txt"""
            sh "scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP}:${DAFT_LOGS_DIR}/*${JOB_BASE_NAME}-${BUILD_NUMBER}*.tar.bz2 ./. 2>/dev/null || true"
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
                    def emailbody_job_failure_1="\nthe job for deploying the csar package called ${SC_NAME}-${CSAR_PACKAGE_VERSION}.csar from artifactory on the IaaS node ${IAAS_NODE} has failed."
                    def emailbody_job_failure_2="\n\nDetailed logs for the overall deployment jenkins job can be found under:\n${LOGS_LINK}/$BUILD_NUMBER/consoleFull"

                    emailext body: "${emailbody_start}"+"${emailbody_job_failure_1}"+"${emailbody_job_failure_2}"+"${emailbody_end}",
                    subject: "Automatic IaaS deployment (${APP}) failed", to: "${env.MAIL}"
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

// CSAR_PACKAGE_VERSION cannot be left empty
        sh """  if [ '${env.CSAR_PACKAGE_VERSION}' = 'null' ];
        then
            echo 'Please indicate the csar package the job should use'
            exit 1
        fi;"""

        VALUES_YAML_FILE_NAME = """${sh( returnStdout: true, script: "echo '${env.ERIC_SC_VALUES_YAML_FILE}' | sed 's?^.*/??'")}""".trim()
        EXTRA_CONFIG_YAML_FILE_NAME = """${sh( returnStdout: true, script: "echo '${env.EXTRA_CONFIG_YAML_FILE}' | sed 's?^.*/??'")}""".trim()

        if (("${ENABLED_CNF}" == "dsc") && (getPackageType() == "non-cncs")) // For pure cnDSC (non-cncs pkg) design base applies
        {
            APP = "cnDSC"
            ADD_CNF_CMD = ""
            ENABLED_CNF = "cnDSC" // This assignment does not have an impact in the logic. It is just to show in all messages and markings to the outside that it is a dsc non-cncs situation
            TRIGGER_MSG = "Trigger the cnDSC deployment"
            USE_PLAYLIST = "009_Deploy_${APP}"
            VALUE_NS = "value_DSC"
            VALUE_RELEASE = "value_DSC"
            ACTION = "Undeploy"
            NW_CONFIG_OPTION = "--network-config-option=DSC"
            REPO_LINK = "-l https://arm.seli.gic.ericsson.se/artifactory/proj-cndsc-generic-local/eiffelesc/"
        }
        else /* SC or EVNFM scenario (it is a cncs (Even only dsc it is now part of the SC pkg itself) or non-cncs but design base NFs (no cnDSC)*/
        {
            APP = "SC"
            ADD_CNF_CMD = "-v ENABLED_CNF=${ENABLED_CNF}"
            if ("${ENABLED_CNF}".contains("dsc"))
            {
                ADD_CNF_CMD = ADD_CNF_CMD + " -o OBJECTSTORAGE"
            }
            VALUE_NS = "value"
            VALUE_RELEASE = "value"
            REPO_LINK = ""
            if ("${DEPLOYMENT_VARIANT}".contains("EVNFM")) /* it has to be distinguished between EVNFM or SC trigger */
            {
                TRIGGER_MSG = "Trigger the evnfm deployment"
                USE_PLAYLIST = "201_EVNFM_Deploy_${APP}"
                ACTION = "Terminate"
            }
            else // it is a SC trigger
            {
                TRIGGER_MSG = "Trigger the SC deployment"
                USE_PLAYLIST = "001_Deploy_${APP}"
                ACTION = "Undeploy"
                NW_CONFIG_OPTION = ""
            }
        }
        if ("${DEPLOYMENT_VARIANT}" != 'Legacy') {
            NW_CONFIG_OPTION = "--network-config-option=${DEPLOYMENT_VARIANT}"
        }
        if ("${DEPLOYMENT_VARIANT}".contains("UCC")) /* For any UCC scenario additional data has to be passed to DAFT */
        {
            NW_CONFIG_OPTION = "-o NO_RLF -o NO_PVTB -o UCC"

            // at Spinnaker level the UCC related variants:     UCC1, UCC1_SingleWorker, UCC1_UCC2, UCC1_SingleWorker_UCC2_SingleWorker
            // at Jenkins level stay the same but Spinnaker has done a timing distribution, as we need to know for each of them whether it is the 1st/2nd deployment and if it is a single worker case or not
            // Concretely looks as follows:

/*
            *Spinnaker  scenario         *********************************  Jenkins dep1    ****************************************************************************  Jenkins dep2    ******************************************
            *Deployment Variant          Variant             remove label&taints     undeploy (ucc too)      add ucc label&taints    *  Variant             remove label&taints     undeploy (ucc too)      add ucc label&taints   *
            * UCC1                       UCC1                       yes                      yes                      no             *    n/a                      n/a                   n/a                         n/a           *
            * UCC1_SingleWorker          UCC1_SingleWorker          yes                      yes                      yes            *    n/a                      n/a                   n/a                         n/a           *
            * UCC1_UCC2                  UCC1                       yes                      yes                      no             *    UCC1_UCC2                 no                    no                          no           *
            * UCC1_SingleWorker_UCC2_SW  UCC1_SingleWorker          yes                      yes                      yes            *    UCC1_SingleWorker_UCC2_SW no                    no                         yes           *
            ************************************************************************************************************************************************************************************************************************
*/
            switch (DEPLOYMENT_VARIANT)
            {
                case ['UCC1']:
                    break
                case ['UCC1_SingleWorker']:
                    NW_CONFIG_OPTION = NW_CONFIG_OPTION + " -o NODESELECTOR1"
                    break
                case ['UCC1_UCC2']:
                    NW_CONFIG_OPTION = NW_CONFIG_OPTION + " -o UCC2"
                    break
                case ['UCC1_SingleWorker_UCC2_SingleWorker']:
                    NW_CONFIG_OPTION = NW_CONFIG_OPTION + " -o UCC2 -o NODESELECTOR2"
                    break
                default:
                    error("There is no logic for the DEPLOYMENT_VARIANT \"$DEPLOYMENT_VARIANT\"; please check.")
                    break
            }
        }
        switch (ENABLED_CNF)
        {
            case ['sepp', 'bsf', 'scp', 'dsc']:
                NF = "${ENABLED_CNF}".toUpperCase()
                break
            case ['cnDSC']:
                NF = "cnDSC"
                break
            case ['bsf-dsc']:
                NF = "BSF-DSC"
                break
            case ['bsf-scp-sepp']:
                NF = "SC"
                break
            case ['bsf-scp-sepp-dsc']:
                NF = "SC-DSC"
                break
            default:
                error("No logic for CNF \"$ENABLED_CNF\" has been implemented")
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
        if ("${DEPLOYMENT_VARIANT}".contains("UCC2"))
        {
            DAFT_NS = DAFT_NS + "-ucc2"
        }
        DAFT_NS_DIR= "/home/eccd/daft_ns_${DAFT_NS}"
        DAFT_NS_DIR_ESCAPED = "\\/home\\/eccd\\/daft_ns_${DAFT_NS}"
// Regarding to spinnaker we need to have extracted the ns, the sc release name and the kube_host at hand:
// The NS is stored at ${DAFT_NS}.
// The sc release name is extracted from the nw config file
        SC_NAME = """${sh( returnStdout: true, script: "sed -n '/<parameter name=\"sc_release_name\"/,/>/p' ${NW_CONFIG_FILE_FULL_PATH} | grep ${VALUE_RELEASE}= | cut -d '\"\' -f2")}""".trim()
// For the kube host we need a new variable, stored at ${IAAS_KUBE_HOST}, as follows:
        IAAS_KUBE_HOST = """${sh( returnStdout: true, script: "echo ${IAAS_NODE} | sed -r 's/-2//' | sed -r 's/_EVNFM//'")}""".trim()
        VERSION = """${sh( returnStdout: true, script: "echo ${CSAR_PACKAGE_VERSION} | cut -d '+' -f 1")}""".trim()
        STABILITY_BRANCH = """${sh( returnStdout: true, script: "echo ${VERSION} |sed -r 's/[0-9]+.[0-9]+.25/master/' |sed -r 's/[0-9]+.[0-9]+.[0-9]+/SC${VERSION}/'")}""".trim()
// Determine IP Version
        IP_VERSION6 = "false"
        IP_VERSION6 = """${sh( returnStdout: true,
            script: "perl ${DAFT_DIR}/perl/bin/generate_network_config_file.pl --output-format one-line --strip-comments --strip-empty-lines -f ${NW_CONFIG_FILE_FULL_PATH} | grep enableIPv6 | head -1 | sed -r 's/.*value=\"(.*)\".*/\\1/' | sed -r 's/\".*//'")}""".trim()
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
def stabilityData()
{
    sh """echo "IAAS_KUBE_HOST=${IAAS_KUBE_HOST}"                               >> ./parameters.properties"""
    sh """echo "NODE_DIRECTOR_IP=${NODE_DIRECTOR_IP}"                           >> ./parameters.properties"""
    sh """echo "DEPLOYMENT_NAMESPACE=${DAFT_NS}"                                >> ./parameters.properties"""
    sh """echo "STABILITY_BRANCH=${STABILITY_BRANCH}"                           >> ./parameters.properties"""

    script
    {
        IP_VERSION ="4"
        if ("${IP_VERSION6}".contains("true"))
        {
           IP_VERSION ="6"
        }
        switch (DEPLOYMENT_VARIANT)
        {
            case [ 'Legacy', 'SigSsV4OamSsDiamSs', 'SigSsV4OamSsDiamDs' ]:
                IP_VERSION ="4"
                break
            case [ 'SigSsV6OamSsDiamSs' ]:
                IP_VERSION ="6"
                break
            case [ 'SigDsOamSsDiamDs', 'SigDsOamDsDiamDs', 'PreferDualStack', 'RequireDualStack' ]:
                IP_VERSION ="DS"
                break
            default:
                break
        } //switch
        sh """echo "IP_VERSION=${IP_VERSION}"                       >> ./parameters.properties"""

        CHART_VERSION = """${sh( returnStdout: true, script: "echo '${CSAR_PACKAGE_VERSION}' | sed 's/+/-/'")}""".trim()
        sh """echo "CHART_VERSION=${CHART_VERSION}"                  >> ./parameters.properties"""

        // Initialize
            CHFSIM ="false"  // only slf has it &&& for SC1.12, sepp too
            NRFSIM ="true"  // in principle only bsf didn't have it. After bug DND-52802 NRFSIM is set to true for any NF
            NRFSIM_DAFT ="false" // when triggering traffic via DAFT, bsf has it as well
            SEPPSIM ="false" // scp & sepp have it
            DSCLOAD ="false" // only bsf has it

            BSF ="false"
            NLF ="false"
            SCP ="false"
            SEPP ="false"
            SLF ="false"
            RLF ="false"

            NF_SELECTED ="no" // Check at the end that data has been found for the specific NF

        if ("${ENABLED_CNF}" == "scp")
        {
            NF_SELECTED ="yes"
            USED_NF = "SCP"
            SEPPSIM ="true"
            NRFSIM_DAFT ="true"
            SCP ="true"
            RLF ="true"
            NLF ="true" /* only when scp is deployed */
            CHFSIM ="true"
            SLF ="true"
        }
        if ("${ENABLED_CNF}" == "sepp")
        {
            NF_SELECTED ="yes"
            USED_NF = "SEPP"
            SEPPSIM ="true"
            NRFSIM_DAFT ="true"
            SEPP ="true"
            RLF ="true"
            if ("${CSAR_PACKAGE_VERSION}".contains("1.12"))
            {
                CHFSIM ="true"
            }
        }
        if ("${ENABLED_CNF}" == "bsf")
        {
            NF_SELECTED ="yes"
            USED_NF = "BSF"
            DSCLOAD ="true"
            NRFSIM_DAFT ="true"
            BSF ="true"
            BSFDIAMETER ="true"
        }
        if ("${ENABLED_CNF}" == "bsf-dsc")
        {
            NF_SELECTED ="yes"
         //   USED_NF = "BSF|DSC" Commented it out until traffic for DSC is automated
            USED_NF = "BSF"
            DSCLOAD ="true"
            NRFSIM_DAFT ="true"
            BSF ="true"
            BSFDIAMETER ="true"
            // Add dsc elements
        }
        if ("${ENABLED_CNF}" == "dsc")
        {
            NF_SELECTED ="yes"
            USED_NF = "DSC"

            // Add dsc elements
        }
        if ("${ENABLED_CNF}" == "bsf-scp-sepp") //this option is only executed triggering traffic via DAFT
        {
            NF_SELECTED ="yes"
            USED_NF = "BSF|SCP|SEPP"
            CHFSIM ="false"
            NRFSIM_DAFT ="true" // when triggering traffic via DAFT, bsf has it as well
            SEPPSIM ="true"
            DSCLOAD ="true"
            BSF ="true"
            BSFDIAMETER ="true"
            if ("${CSAR_PACKAGE_VERSION}".contains("1.12"))
            {
                CHFSIM ="true"
            }
        }
        if ("${ENABLED_CNF}" == "bsf-scp-sepp-dsc")
        {
            NF_SELECTED ="yes"
            // USED_NF = "BSF|SCP|SEPP|DSC" Commented it out until traffic for DSC is automated
            USED_NF = "BSF|SCP|SEPP"
            CHFSIM ="false"
            NRFSIM_DAFT ="true" // when triggering traffic via DAFT, bsf has it as well
            SEPPSIM ="true"
            DSCLOAD ="true"
            BSF ="true"
            BSFDIAMETER ="true"
            if ("${CSAR_PACKAGE_VERSION}".contains("1.12"))
            {
                CHFSIM ="true"
            }
            // Add needed parts for dsc
        }

        if ("${NF_SELECTED}" == "no")
        {
            error("Specific data about NF \"$ENABLED_CNF\" hasn't been written on the properties file. Please check")
        }

        sh """echo "STABILITY_TYPE=${USED_NF}"     >> ./parameters.properties"""
        sh """echo "BSF=${BSF}"                    >> ./parameters.properties"""
        sh """echo "NLF=${NLF}"                    >> ./parameters.properties"""
        sh """echo "SCP=${SCP}"                    >> ./parameters.properties"""
        sh """echo "SEPP=${SEPP}"                  >> ./parameters.properties"""
        sh """echo "SLF=${SLF}"                    >> ./parameters.properties"""
        sh """echo "RLF=${RLF}"                    >> ./parameters.properties"""

        sh """echo "CHFSIM=${CHFSIM}"                 >> ./parameters.properties"""
        sh """echo "NRFSIM=${NRFSIM}"                 >> ./parameters.properties"""
        sh """echo "NRFSIM_DAFT=${NRFSIM_DAFT}"       >> ./parameters.properties"""
        sh """echo "SEPPSIM=${SEPPSIM}"               >> ./parameters.properties"""
        sh """echo "DSCLOAD=${DSCLOAD}"               >> ./parameters.properties"""
    }
}
def stabilityData_cnDSC()
{
    sh """echo "IAAS_KUBE_HOST=${IAAS_KUBE_HOST}"                               >> ./parameters.properties"""
    sh """echo "NODE_DIRECTOR_IP=${NODE_DIRECTOR_IP}"                           >> ./parameters.properties"""
    sh """echo "DEPLOYMENT_NAMESPACE=${DAFT_NS}"                                >> ./parameters.properties"""

}
def getConfigFileNames()
{
    script
    {
        FILES_TO_LOAD = 1
        CMD_FOR_FILES = ""

        if ("${ENABLED_CNF}".contains("bsf"))
        {
            CMD_FOR_FILES = CMD_FOR_FILES + " -v CONFIG_DATA_FILE=/home/eccd/download/${CSAR_PACKAGE_VERSION}/sc-config-sample/sample_bsf_func_and_diameter,user=bsf-admin.netconf"
            CMD_FOR_FILES = CMD_FOR_FILES + " -v CONFIG_DATA_FILE_${FILES_TO_LOAD}=/home/eccd/download/${CSAR_PACKAGE_VERSION}/sc-config-sample/action_bsf_init_db,user=bsf-admin.netconf"
            FILES_TO_LOAD++
        }
        if (! "${DEPLOYMENT_VARIANT}".contains("UCC"))
        {
            if ("${ENABLED_CNF}".contains("bsf") ||"${ENABLED_CNF}".contains("scp") || "${ENABLED_CNF}".contains("sepp"))
            {
                if (CMD_FOR_FILES.isEmpty())
                {
                    CMD_FOR_FILES = CMD_FOR_FILES + " -v CONFIG_DATA_FILE=/home/eccd/download/${CSAR_PACKAGE_VERSION}/sc-config-sample/sample_broker_config,user=expert.netconf"
                }
                else
                {
                    CMD_FOR_FILES = CMD_FOR_FILES + " -v CONFIG_DATA_FILE_${FILES_TO_LOAD}=/home/eccd/download/${CSAR_PACKAGE_VERSION}/sc-config-sample/sample_broker_config,user=expert.netconf"
                    FILES_TO_LOAD++
                }
            }
        }
        if ("${ENABLED_CNF}".contains("scp"))
        {
            if (CMD_FOR_FILES.isEmpty())
            {
                CMD_FOR_FILES = CMD_FOR_FILES + " -v CONFIG_DATA_FILE=/home/eccd/download/${CSAR_PACKAGE_VERSION}/sc-config-sample/sample_config_scp,user=scp-admin.netconf"
            }
            else
            {
                CMD_FOR_FILES = CMD_FOR_FILES + " -v CONFIG_DATA_FILE_${FILES_TO_LOAD}=/home/eccd/download/${CSAR_PACKAGE_VERSION}/sc-config-sample/sample_config_scp,user=scp-admin.netconf"
                FILES_TO_LOAD++
            }
        }
        if ("${ENABLED_CNF}".contains("sepp"))
        {
            if (CMD_FOR_FILES.isEmpty())
            {
                CMD_FOR_FILES = CMD_FOR_FILES + " -v CONFIG_DATA_FILE=/home/eccd/download/${CSAR_PACKAGE_VERSION}/sc-config-sample/sample_sepp_poc_config,user=sepp-admin.netconf"
            }
            else
            {
                CMD_FOR_FILES = CMD_FOR_FILES + " -v CONFIG_DATA_FILE_${FILES_TO_LOAD}=/home/eccd/download/${CSAR_PACKAGE_VERSION}/sc-config-sample/sample_sepp_poc_config,user=sepp-admin.netconf"
                FILES_TO_LOAD++
            }
        }
    }
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
def removeAdditionalNS(THIS_NS)
{
    script
    {

        sh "${DAFT_DIR}/expect/bin/send_command_to_ssh.exp --timeout=1800 --user=eccd --ip=${NODE_DIRECTOR_IP} --password='notneeded' --stop-on-error \
        --command='perl ${DAFT_NS_DIR}/perl/bin/execute_playlist.pl -p 003_Undeploy_SC -c no -n ${DAFT_NS_DIR}/${NW_CONFIG_FILE_NAME} -v SC_NAMESPACE=${DAFT_NS}-${THIS_NS} \
        -j ${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}_Delete-ns-${DAFT_NS}-${THIS_NS} -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=no -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=no -v INPUT_AVAILABLE=no \
        -v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=no -v SKIP_UNDEPLOY_CRD=yes -v CLEAN_REGISTRY=no -w ${DAFT_LOGS_DIR}' "
    }
}
def removeLabel(THIS_LABEL)
{
    script
    {
        sh "${DAFT_DIR}/expect/bin/send_command_to_ssh.exp --timeout=1800 --user=eccd --ip=${NODE_DIRECTOR_IP} --password='notneeded' --stop-on-error \
        --command='perl ${DAFT_NS_DIR}/perl/bin/execute_playlist.pl -p 103_Tools_Management -c no -v INPUT_AVAILABLE=no -v DO_REMOVE_TAINT=yes -v DO_REMOVE_LABEL=yes \
        -v NODE_TAINTS=NoSchedule -v NODE_LABEL=usage=${THIS_LABEL} -j ${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}_Remove_Node_Label_${THIS_LABEL} \
        -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=yes -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=no -v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=no -v COLLECT_LOGS_AT_SUCCESS=no -w ${DAFT_LOGS_DIR}' "
    }
}
def addLabel(THIS_WORKER_POSITION, THIS_LABEL, THIS_NODE_TAINT)
{
    script
    {
        if ("${THIS_WORKER_POSITION}" == "0")
        {
            THIS_WORKER = ""
        }
        else
        {
            sh """ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP} "kubectl get nodes --no-headers -o custom-columns=NAME:.metadata.name --selector='node-role.kubernetes.io/worker'" > ./WORKERS_LIST.txt"""
            WORKER_NAME = """${sh(returnStdout: true, script: "cat ./WORKERS_LIST.txt | head -$THIS_WORKER_POSITION | tail -1")}""".trim()
            THIS_WORKER = "-v NODE_NAMES=\"$WORKER_NAME\""
        }
        sh "${DAFT_DIR}/expect/bin/send_command_to_ssh.exp --timeout=1800 --user=eccd --ip=${NODE_DIRECTOR_IP} --password='notneeded' --stop-on-error \
        --command='perl ${DAFT_NS_DIR}/perl/bin/execute_playlist.pl -p 103_Tools_Management -c no -v INPUT_AVAILABLE=no -v DO_ASSIGN_TAINT=yes -v DO_ASSIGN_LABEL=yes \
        -v NODE_TAINTS=${THIS_NODE_TAINT} -v NODE_LABEL=usage=${THIS_LABEL} ${THIS_WORKER} -j ${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}_Add_Node_Label_${THIS_LABEL} \
        -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=yes -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=yes -w ${DAFT_LOGS_DIR}' "
    }
}
def getPackageType()
{
    script
    {
        i = 1 /* integer carrying the position of each digit for the semantic version of the pkg (major, minor, patch) */
        p = 5 /* integer carrying the power of 10 to apply to each digit. Eg: 1.12.0 -> 11200, 1.11.25 -> 11125 */
        REFERENCE_PKG ="1.15.0" // Any package below is a non-cncs
        BASE_PKG_ACCUM = 0
        CSAR_PKG_ACCUM = 0
        while ( i < 4 )
        {
            BASE_PKG_DIGIT = """${sh( returnStdout: true, script: "echo ${REFERENCE_PKG} | cut -d '+' -f1 | cut -d '.' -f$i")}""".trim()
            CSAR_PKG_DIGIT = """${sh( returnStdout: true, script: "echo ${CSAR_PACKAGE_VERSION} | cut -d '+' -f1 | cut -d '.' -f$i")}""".trim()

            BASE_PKG_ACCUM = BASE_PKG_ACCUM + ( "${BASE_PKG_DIGIT}".toInteger() * (10**(p-i)) )
            CSAR_PKG_ACCUM = CSAR_PKG_ACCUM + ( "${CSAR_PKG_DIGIT}".toInteger() * (10**(p-i)) )
            i=i+1
            p=p-1
        }
        if ( CSAR_PKG_ACCUM < BASE_PKG_ACCUM )
        {
            RESULT = "non-cncs"
        }
        else
        {
            RESULT = "cncs"
        }
        return RESULT
    }
}
def creationAndTransfer()
{
    script
    {
        echo 'Create DAFT package'
        sh "cd ${DAFT_DIR} && make clean && make daft"

        ESC_PACKAGE_NAME = """${sh( returnStdout: true, script: 'ls ${DAFT_DIR} | grep ESC')}""".trim()

        echo 'Creating daft folder in IaaS node'

        sh "${DAFT_DIR}/expect/bin/send_command_to_ssh.exp --timeout=1800 --user=eccd --ip=${NODE_DIRECTOR_IP} --password='notneeded' --stop-on-error \
        --command='rm -rf ${DAFT_NS_DIR}' --command='mkdir ${DAFT_NS_DIR}'"

        echo 'Transfering files to IaaS node'
        sh "${DAFT_DIR}/expect/bin/scp_files.exp --from-data='${DAFT_DIR}/${ESC_PACKAGE_NAME}' --to-data='eccd@${NODE_DIRECTOR_IP}:${DAFT_NS_DIR}'"
        sh "${DAFT_DIR}/expect/bin/scp_files.exp --from-data='./scripts/download_csar.pl' --to-data='eccd@${NODE_DIRECTOR_IP}:${DAFT_NS_DIR}'"
        sh "${DAFT_DIR}/expect/bin/scp_files.exp --from-data='${NW_CONFIG_FILE_FULL_PATH}' --to-data='eccd@${NODE_DIRECTOR_IP}:${DAFT_NS_DIR}'"

        echo 'Unpacking DAFT inside the IaaS node & downloading csar pkg from artifactory'
        withCredentials([string(credentialsId: 'armseligic-armdocker-eiffelesc-identity-token-for-Smoke-CI', variable: 'ARTIFACTORY_TOKEN')])
        {
            sh "${DAFT_DIR}/expect/bin/send_command_to_ssh.exp --timeout=1800 --user=eccd --ip=${NODE_DIRECTOR_IP} --password='notneeded' --stop-on-error \
            --command='tar -xvf ${DAFT_NS_DIR}/${ESC_PACKAGE_NAME} -C ${DAFT_NS_DIR}/' \
            --command='tar -xvf ${DAFT_NS_DIR}/DAFT*.tar.gz -C ${DAFT_NS_DIR}/' \
            --command='mkdir -p /home/eccd/download/' --command='${DAFT_NS_DIR}/download_csar.pl --artifact-token $ARTIFACTORY_TOKEN --noprogress --color=no -p ${CSAR_PACKAGE_VERSION} ${REPO_LINK} \
            -t /home/eccd/download ' "
        }
        echo 'Check for existence of addtional .yaml files and transfer to the node'

        sh """  if [ '${env.ERIC_SC_VALUES_YAML_FILE}' = 'null' ];
                then
                    echo ' '  > ./var.add-yaml-file-cmd
                elif [ -r ${env.ERIC_SC_VALUES_YAML_FILE} ]
                    then
                        ${DAFT_DIR}/expect/bin/scp_files.exp --from-data='${env.ERIC_SC_VALUES_YAML_FILE}' \
                        --to-data='eccd@${NODE_DIRECTOR_IP}:${DAFT_NS_DIR}/${VALUES_YAML_FILE_NAME}';
                        echo '-v CONFIG_FILE_HELM_CHART=${DAFT_NS_DIR}/${VALUES_YAML_FILE_NAME}' > ./var.add-yaml-file-cmd;
                    else
                        echo "The file ${env.ERIC_SC_VALUES_YAML_FILE} couldn't be read or found.";
                        echo "If the file exists please provide at least read permissions for the 'Others Rights group' for the whole path chain up to including the file.";
                        exit 1;
                fi;"""

        sh """  if [ '${env.EXTRA_CONFIG_YAML_FILE}' = 'null' ];
                then
                    sed -i 's/\$/ /' ./var.add-yaml-file-cmd
                elif [ -r ${env.EXTRA_CONFIG_YAML_FILE} ]
                    then
                        ${DAFT_DIR}/expect/bin/scp_files.exp --from-data='${env.EXTRA_CONFIG_YAML_FILE}' \
                        --to-data='eccd@${NODE_DIRECTOR_IP}:${DAFT_NS_DIR}/${EXTRA_CONFIG_YAML_FILE_NAME}';
                        sed -i 's/\$/ -v CONFIG_FILE_HELM_CHART_1=${DAFT_NS_DIR_ESCAPED}\\/${EXTRA_CONFIG_YAML_FILE_NAME}/' ./var.add-yaml-file-cmd;
                    else
                        echo "The file ${env.EXTRA_CONFIG_YAML_FILE} couldn't be read or found.";
                        echo " If the file exists please provide at least read permissions for the 'Others Rights group' for the whole path chain up to including the file.";
                        exit 1;
                fi;"""

        if ("${REDUCED_RESOURCES}" == "yes")
        {
            sh """  if [ -f ${env.REDUCED_RESOURCES_DIR}/eric-sc-ucc-values.yaml ];
                    then
                        ${DAFT_DIR}/expect/bin/scp_files.exp --from-data='${env.REDUCED_RESOURCES_DIR}/eric-sc-ucc-values.yaml' \
                        --to-data='eccd@${NODE_DIRECTOR_IP}:${DAFT_NS_DIR}/.';
                        sed -i 's/\$/ -v CONFIG_FILE_HELM_CHART_2=${DAFT_NS_DIR_ESCAPED}\\/eric-sc-ucc-values.yaml/' ./var.add-yaml-file-cmd;
                    else
                        echo "The file eric-sc-ucc-values.yaml couldn't be found under directory ${env.REDUCED_RESOURCES_DIR}";
                        exit 1;
                    fi;"""
        }
        if ("${DEPLOYMENT_VARIANT}".contains("UCC")) /* For any UCC scenario additional data has to be passed to DAFT */
        {
            sh """sed -i 's/\$/ -v CONFIG_FILE_HELM_CHART_3=\\/home\\/eccd\\/download\\/${CSAR_PACKAGE_VERSION}\\/release-artifacts\\/eric-sc-ucc-values-${CSAR_PACKAGE_VERSION}.yaml/' ./var.add-yaml-file-cmd"""
        }

        ADD_YAML_FILE_CMD = """${sh( returnStdout: true, script: "cat ./var.add-yaml-file-cmd")}""".trim()
    }
}