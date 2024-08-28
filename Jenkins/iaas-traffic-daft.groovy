// Check which BS to use

def AGENT_LABEL = null
def WS_START = null
def POSITION = null
def EXCLUSIVE_BS_LABEL = '5G-ESC-001'
def POOL_BS_LABEL = '5G-SC'

node('DSC_BuildSlave_GIC_1||DSC_BuildSlave_GIC_2||DSC_BuildSlave_GIC_3||DSC_BuildSlave_GIC_4||DSC_BuildSlave_GIC_5||SERO_GIC')
{
    stage('set agent')
    {
/* no further need to deviate IPv6 traffic to a dedicated build slave, as all of them are supposed to be able to handle IPv6
        if (("${IAAS_NODE}".contains("n103")) || ("${IAAS_NODE}".contains("dsc8991")))
        {
            AGENT_LABEL = '5G-SC-IPv6' // for IPv6 nodes choose the corresponding build slaves
            WS_START = '/local'
            POSITION = '6'
        }
        else
        {

 */
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

            WS_START = '/home/jenkins'
            POSITION = '7'
//        }
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
//          label 'esc-docker || esc-docker-1|| esc-docker-2'
//            label '5G-SC'
            label "${AGENT_LABEL}"
	        customWorkspace "${WS_START}/workspace/5G-ESC/IaaS/${JOB_BASE_NAME}_${IAAS_NODE}"
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
        KUBE_HOST = "${KUBE_HOST}"
        NW_CONFIG_FILE = "${NW_CONFIG_FILE}"
        DEPLOYMENT_VARIANT ="${DEPLOYMENT_VARIANT}"
        CSAR_PKG_VERSION = "${CSAR_PKG_VERSION}"
        TOOLS_NAMESPACE = "${TOOLS_NAMESPACE}"
        TEST_TYPE = "${TEST_TYPE}"
        TRAFFIC_DURATION = "${TRAFFIC_DURATION}"
        TRAFFIC_MODEL = "${TRAFFIC_MODEL}"
        // right now certificates to be generated are hard coded
        DSCLOAD = "${DSCLOAD}"
        K6 = "${K6}"
        CHFSIM = "${CHFSIM}"
 	 	NRFSIM = "${NRFSIM}"
 	 	SEPPSIM = "${SEPPSIM}"
        SC_MONITOR = "${SC_MONITOR}"
        SFTP = "${SFTP}"
        MAIL = "${MAIL}"
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
                    OWN_WORKSPACE = """${sh( returnStdout: true, script: "pwd | cut -d '/' -f${POSITION}")}""".trim()
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
                // Assign paths to the logs
                script
                {
                    // Assign paths to the logs
                    LOGS_LINK = """${sh( returnStdout: true,
                    script: "echo ${JENKINS_INSTANCE}/${JOB_BASE_NAME}")}""".trim()

                    // Assign director-0 ip address
                    sh "./scripts/bash/get_directors_ip.bash ${IAAS_NODE}"

                    NODE_DIRECTOR_IP = """${sh( returnStdout: true, script: "cat ./var.DIRECTOR-0-IP")}""".trim()
                    wrap([$class: 'BuildUser'])
                    {
                        script
                        {
                            triggering_user = """${sh( returnStdout: true, script: "echo ${BUILD_USER_ID}")}""".trim()
                        }
                    }
                    // set displayed description to "build slave, node, triggering_user, csar pkg, deployment variant"
                    script
                    {
                        currentBuild.description = "${env.NODE_NAME}, ${IAAS_NODE}, ${triggering_user}, ${CSAR_PKG_VERSION}, ${DEPLOYMENT_VARIANT}"
                    }
                    // get appropriate settings for calling the nw config file

                    VALUE_NS="value"
                    VALUE_RELEASE="value"

                    if ("${DEPLOYMENT_VARIANT}" != 'Legacy')
                    {
                        NW_CONFIG_OPTION = "--network-config-option=${DEPLOYMENT_VARIANT}"
                    }
                    else
                    {
                        NW_CONFIG_OPTION=""
                    }

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
                    DAFT_NS_DIR = "/home/eccd/daft_ns_${DAFT_NS}"

                    if ( "${TRAFFIC_MODEL}" =="base")
                    {
                        MODEL_CHOSEN =""
                    }
                    else
                    {
                        MODEL_CHOSEN ="_$TRAFFIC_MODEL"
                    }
                    // Initialization for DAFT generated folders
                    LOGS_FOLDER_CERTIFICATES ="undefined"
                    LOGS_FOLDER_SIMULATORS ="undefined"
                    LOGS_FOLDER_TRAFFIC_CONFIG ="undefined"
                    LOGS_FOLDER_TRAFFIC_RUN ="undefined"
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
        stage('Certificates and secrets')
        {
            steps
            {
                script
                {
                    echo "Generate and install certificates and secrets"

                // Concatenate the certificates to generate (right now hard coded)
                    CERTIFICATES_TO_GENERATE = "rootca,scpmgr,scpwrk,seppmgr,seppwrk,nbi,diameter,transformer"

                    sh "${DAFT_DIR}/expect/bin/send_command_to_ssh.exp --timeout=3600  --user=eccd --ip=${NODE_DIRECTOR_IP} --password='notneeded' \
                    --stop-on-error --command='perl  ${DAFT_NS_DIR}/perl/bin/execute_playlist.pl -p 102_Supreme_Certificate_Management -c no \
                    -n ${DAFT_NS_DIR}/${NW_CONFIG_FILE_NAME} ${NW_CONFIG_OPTION} -v GENERATE_CERTIFICATES=yes -v CERTIFICATES_TO_GENERATE=${CERTIFICATES_TO_GENERATE} \
                    -v NAMESPACE=${DAFT_NS} -v INSTALL_CERTIFICATES=yes -v CERTIFICATE_DIRECTORY=/home/eccd/download/${CSAR_PKG_VERSION}/certificates -v CERTIFICATE_VALIDITY_DAYS=7200 \
                    -j ${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}_certificates-secrets -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=yes -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=yes \
                    -v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=yes -v INPUT_AVAILABLE=no -v IGNORE_ALARMS=no -v SKIP_PRE_HEALTHCHECK=no -v SKIP_POST_HEALTHCHECK=no \
                    -v SOFTWARE_DIR=/home/eccd/download/${CSAR_PKG_VERSION} -v COLLECT_LOGS_AT_SUCCESS=no -w ${DAFT_LOGS_DIR}'"
                } //script
            }
            post
            {
                always
                {
                    script
                    {
                        LOGS_FOLDER_CERTIFICATES = """${sh( returnStdout: true,
                            script: "ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP} 'find ${DAFT_LOGS_DIR} -type d -name *${JOB_BASE_NAME}-${BUILD_NUMBER}_certificates-secrets*'")}""".trim()
                    }
                }
                failure
                {
                    script
                    {
                        if ("${env.MAIL}" != 'null')
                        {
                            emailbody_certificates_1="\nthe generation and installation of certificates and secrets via DAFT"
                            emailbody_certificates_2=" on node ${IAAS_NODE} has failed.\n\nDetailed logs can be found on the"
                            emailbody_certificates_3=" director of node ${IAAS_NODE} (IP address of director: ${NODE_DIRECTOR_IP}) under the folder: ${LOGS_FOLDER_CERTIFICATES}"

                            emailext body: "${emailbody_start}"+"${emailbody_certificates_1}"+"${emailbody_certificates_2}"+"${emailbody_certificates_3}"+"${emailbody_end}",
                            subject: "Generation and installation of certificates and secrets via DAFT failed", to: "${env.MAIL}"
                        }
                    }
                }
            }
        }
        stage('Simulators')
        {
            steps
            {
                script
                {
                   echo "Install the needed tools"
                // Concatenate the tools to install
                    TOOLS_TO_INSTALL = ""

                    if ("${DSCLOAD}" == "true")
                    {
                        TOOLS_TO_INSTALL = TOOLS_TO_INSTALL + "dscload,"
                    }
                    if ("${K6}" == "true")
                    {
                        TOOLS_TO_INSTALL = TOOLS_TO_INSTALL + "k6,"
                    }
                    if ("${CHFSIM}" == "true")
                    {
                        TOOLS_TO_INSTALL = TOOLS_TO_INSTALL + "chfsim,"
                    }
                    if ("${NRFSIM}" == "true")
                    {
                        TOOLS_TO_INSTALL = TOOLS_TO_INSTALL + "nrfsim,"
                    }
                    if ("${SEPPSIM}" == "true")
                    {
                        TOOLS_TO_INSTALL = TOOLS_TO_INSTALL + "seppsim,"
                    }
                    if ("${SC_MONITOR}" == "true")
                    {
                    TOOLS_TO_INSTALL = TOOLS_TO_INSTALL + "sc-monitor,"
                    }
                    if ("${SFTP}" == "true")
                    {
                        TOOLS_TO_INSTALL = TOOLS_TO_INSTALL + "sftp"
                    }
                    TOOLS_TO_INSTALL = """${sh( returnStdout: true, script: " echo ${TOOLS_TO_INSTALL} | sed  's/,\$//' ")}""".trim()

                    BS_DIR = """${sh( returnStdout: true, script: "pwd")}""".trim()


                    if ("${TEST_TYPE}" == "IaaS_Stability_tools_on_separate_workers")
                    {
                        NODESELECTOR ="-v TOOLS_ON_SEPARATE_WORKERS=yes"
                    }
                    else
                    {
                        NODESELECTOR =""
                    }
                    sh "${DAFT_DIR}/expect/bin/send_command_to_ssh.exp --timeout=3600  --user=eccd --ip=${NODE_DIRECTOR_IP} --password='notneeded' \
                    --stop-on-error --command='perl  ${DAFT_NS_DIR}/perl/bin/execute_playlist.pl -p 109_Traffic_Tools_Install -c no \
                    -n ${DAFT_NS_DIR}/${NW_CONFIG_FILE_NAME} ${NW_CONFIG_OPTION} -v PASSWORD_EXPIRE=no -v TOOLS_TO_INSTALL=${TOOLS_TO_INSTALL} -v TOOLS_NAMESPACE=${TOOLS_NAMESPACE} \
                    ${NODESELECTOR} -v CERTIFICATE_VALIDITY_DAYS=7200 -v CERTIFICATE_DIRECTORY=/home/eccd/download/${CSAR_PKG_VERSION}/certificates -j ${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}_simulators \
                    -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=yes -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=yes -v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=yes -v COLLECT_LOGS_AT_SUCCESS=yes \
                    -v INPUT_AVAILABLE=no -v SOFTWARE_DIR=/home/eccd/download/${CSAR_PKG_VERSION} -v SPECIAL_IDENTIFIER=${DAFT_NS} -v SKIP_LOADING_SC_MONITOR_IMAGE=yes -w ${DAFT_LOGS_DIR}'"

                }
            }
            post
            {
                always
                {
                    script
                    {
                        LOGS_FOLDER_SIMULATORS = """${sh( returnStdout: true,
                                script: "ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP} 'find ${DAFT_LOGS_DIR} -type d -name *${JOB_BASE_NAME}-${BUILD_NUMBER}_simulators*'")}""".trim()
                    }
                }
                failure
                {
                    script
                    {
                        if ("${env.MAIL}" != 'null')
                        {
                            emailbody_simulators_1="\nthe installation of the needed tools via DAFT"
                            emailbody_simulators_2=" on node ${IAAS_NODE} has failed.\n\nDetailed logs can be found on the"
                            emailbody_simulators_3=" director of node ${IAAS_NODE} (IP address of director: ${NODE_DIRECTOR_IP}) under the folder: ${LOGS_FOLDER_SIMULATORS}"

                            emailext body: "${emailbody_start}"+"${emailbody_simulators_1}"+"${emailbody_simulators_2}"+"${emailbody_simulators_3}"+"${emailbody_end}",
                            subject: "Install the needed tools failed", to: "${env.MAIL}"
                        }
                    }
                }
            }
        }
        stage('Configure traffic')
        {
            steps
            {
                script
                {
                    echo "Configure traffic"

                    sh "${DAFT_DIR}/expect/bin/send_command_to_ssh.exp --timeout=3600  --user=eccd --ip=${NODE_DIRECTOR_IP} --password='notneeded' \
                    --stop-on-error --command='perl  ${DAFT_NS_DIR}/perl/bin/execute_playlist.pl -p 107_Traffic_Tools_Configure_And_Start -c no \
                    -n ${DAFT_NS_DIR}/${NW_CONFIG_FILE_NAME} ${NW_CONFIG_OPTION} -v PASSWORD_EXPIRE=no -v TOOLS_NAMESPACE=${TOOLS_NAMESPACE} -v TRAFFIC_TO_CONFIGURE=automatic \
                    -v TRAFFIC_CONFIG_FILE=/home/eccd/download/${CSAR_PKG_VERSION}/base_stability_traffic/base_stability_traffic_bsf_scp_sepp${MODEL_CHOSEN}.config \
                    -j ${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}_configure-traffic -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=yes -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=yes \
                    -v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=yes -v INPUT_AVAILABLE=no -v IGNORE_ALARMS=no -v SKIP_PRE_HEALTHCHECK=no -v SKIP_POST_HEALTHCHECK=no \
                    -v COLLECT_LOGS_AT_SUCCESS=no -w ${DAFT_LOGS_DIR}'"
                } //script
            }
            post
            {
                always
                {
                    script
                    {
                        LOGS_FOLDER_TRAFFIC_CONFIG = """${sh( returnStdout: true,
                            script: "ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP} 'find ${DAFT_LOGS_DIR} -type d -name *${JOB_BASE_NAME}-${BUILD_NUMBER}_configure-traffic*'")}""".trim()
                    }
                }
                failure
                {
                    script
                    {
                        if ("${env.MAIL}" != 'null')
                        {
                            emailbody_traffic_1="\nthe traffic configuration via DAFT"
                            emailbody_traffic_2=" on node ${IAAS_NODE} has failed.\n\nDetailed logs can be found on the"
                            emailbody_traffic_3=" director of node ${IAAS_NODE} (IP address of director: ${NODE_DIRECTOR_IP}) under the folder: ${LOGS_FOLDER_TRAFFIC_CONFIG}"

                            emailext body: "${emailbody_start}"+"${emailbody_traffic_1}"+"${emailbody_traffic_2}"+"${emailbody_traffic_3}"+"${emailbody_end}",
                            subject: "Traffic configuration via DAFT failed", to: "${env.MAIL}"
                        }
                    }
                }
            }
        }
        stage('Start traffic')
        {
            steps
            {
                script
                {
                    echo "Handle traffic via DAFT"

                    sh "${DAFT_DIR}/expect/bin/send_command_to_ssh.exp --timeout=3600  --user=eccd --ip=${NODE_DIRECTOR_IP} --password='notneeded' \
                    --stop-on-error --command='perl ${DAFT_NS_DIR}/perl/bin/execute_playlist.pl -p 107_Traffic_Tools_Configure_And_Start -c no -v CHECK_TRAFFIC_SUCCESS_RATE=no \
                    -n ${DAFT_NS_DIR}/${NW_CONFIG_FILE_NAME} ${NW_CONFIG_OPTION} -v PASSWORD_EXPIRE=no -v SC_NAMESPACE=${DAFT_NS} -v TOOLS_NAMESPACE=${TOOLS_NAMESPACE} \
                    -v TRAFFIC_TO_START=automatic -v TRAFFIC_DURATION=${TRAFFIC_DURATION} -j ${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}_traffic-run \
                    -v TRAFFIC_CONFIG_FILE=/home/eccd/download/${CSAR_PKG_VERSION}/base_stability_traffic/base_stability_traffic_bsf_scp_sepp${MODEL_CHOSEN}.config \
                    -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=yes -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=yes -v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=yes \
                    -v INPUT_AVAILABLE=no -v IGNORE_ALARMS=no -v SKIP_PRE_HEALTHCHECK=no -v SKIP_POST_HEALTHCHECK=no -v COLLECT_LOGS_AT_SUCCESS=no -w ${DAFT_LOGS_DIR}'"
                } //script
            }
            post
            {
                always
                {
                    script
                    {
                        LOGS_FOLDER_TRAFFIC_RUN = """${sh( returnStdout: true,
                            script: "ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP} 'find ${DAFT_LOGS_DIR} -type d -name *${JOB_BASE_NAME}-${BUILD_NUMBER}_traffic-run*'")}""".trim()
                    }
                }
                failure
                {
                    script
                    {
                        if ("${env.MAIL}" != 'null')
                        {
                            emailbody_traffic_1="\nthe start & run of traffic via DAFT"
                            emailbody_traffic_2=" on node ${IAAS_NODE} has failed.\n\nDetailed logs can be found on the"
                            emailbody_traffic_3=" director of node ${IAAS_NODE} (IP address of director: ${NODE_DIRECTOR_IP}) under the folder: ${LOGS_FOLDER_TRAFFIC_RUN}"

                            emailext body: "${emailbody_start}"+"${emailbody_traffic_1}"+"${emailbody_traffic_2}"+"${emailbody_traffic_3}"+"${emailbody_end}",
                            subject: "Start and run of traffic via DAFT failed", to: "${env.MAIL}"
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
                        def emailbody_job_finished_1="\nthe automatic process for preparing & running traffic via DAFT on node ${IAAS_NODE} has concluded."
                        def emailbody_job_finished_2="\n\nDetailed logs can be found on the director of node ${IAAS_NODE} (IP address of director:"
                        def emailbody_job_finished_3=" ${NODE_DIRECTOR_IP}) under the folders: \n${LOGS_FOLDER_CERTIFICATES}\n${LOGS_FOLDER_SIMULATORS}"
                        def emailbody_job_finished_4="\n${LOGS_FOLDER_TRAFFIC_CONFIG}\n${LOGS_FOLDER_TRAFFIC_RUN}\n\nDetailed logs for the overall jenkins job can be found under: ${LOGS_LINK}/$BUILD_NUMBER/consoleFull"

                        emailext body: "${emailbody_start}"+"${emailbody_job_finished_1}"+"${emailbody_job_finished_2}"+
                        "${emailbody_job_finished_3}"+"${emailbody_job_finished_4}"+"${emailbody_end}",
                        subject: "Automatic process for preparing & running traffic via DAFT concluded", to: "${env.MAIL}"
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
            script
            {
                if ( fileExists("/proj/DSC_CI/jenkins/tools/help_scripts/concurrentBuilds.sh") )
                {
					sh "/proj/DSC_CI/jenkins/tools/help_scripts/concurrentBuilds.sh -stop"
				} // if
			} // script
            artifactList()
            sh """echo "ARTIFACT_LIST=${ARTIFACT_LIST}" >> ./parameters.properties"""

            archiveArtifacts allowEmptyArchive: true, artifacts: '*.tar.bz2', onlyIfSuccessful: false
            archiveArtifacts allowEmptyArchive: true, artifacts: 'parameters.properties', onlyIfSuccessful: false
            sh "rm -f ./*.tar.bz2"

// with the latest implementation spinnaker will search for all generated artifacts, so currently neither this logic nor the archiving of this file is needed
//          script
//          {
//              RELATIVE_FOLDER_PATH = """${sh( returnStdout: true, script: "echo ${LOGS_FOLDER_CERTIFICATES} | sed 's?^.*/??'")}""".trim()
//              if (fileExists("./${RELATIVE_FOLDER_PATH}.tar.bz2"))
//              {
//                  LOGS_FOLDER_CERTIFICATES_FILE_NAME = RELATIVE_FOLDER_PATH + ".tar.bz2"
//              }
//              else
//              {
//                  LOGS_FOLDER_CERTIFICATES_FILE_NAME = ""
//              }
//              RELATIVE_FOLDER_PATH = """${sh( returnStdout: true, script: "echo ${LOGS_FOLDER_SIMULATORS} | sed 's?^.*/??'")}""".trim()
//              if (fileExists("./${RELATIVE_FOLDER_PATH}.tar.bz2"))
//              {
//                  LOGS_FOLDER_SIMULATORS_FILE_NAME = RELATIVE_FOLDER_PATH + ".tar.bz2"
//              }
//              else
//              {
//                  LOGS_FOLDER_SIMULATORS_FILE_NAME = ""
//              }
//              RELATIVE_FOLDER_PATH = """${sh( returnStdout: true, script: "echo ${LOGS_FOLDER_TRAFFIC_CONFIG} | sed 's?^.*/??'")}""".trim()
//              if (fileExists("./${RELATIVE_FOLDER_PATH}.tar.bz2"))
//              {
//                  LOGS_FOLDER_TRAFFIC_CONFIG_FILE_NAME = RELATIVE_FOLDER_PATH + ".tar.bz2"
//              }
//              else
//              {
//                  LOGS_FOLDER_TRAFFIC_CONFIG_FILE_NAME = ""
//              }
//              RELATIVE_FOLDER_PATH = """${sh( returnStdout: true, script: "echo ${LOGS_FOLDER_TRAFFIC_RUN} | sed 's?^.*/??'")}""".trim()
//              if (fileExists("./${RELATIVE_FOLDER_PATH}.tar.bz2"))
//              {
//                  LOGS_FOLDER_TRAFFIC_RUN_FILE_NAME = RELATIVE_FOLDER_PATH + ".tar.bz2"
//              }
//              else
//              {
//                  LOGS_FOLDER_TRAFFIC_RUN_FILE_NAME = ""
//              }
//          }
//          sh """echo "LOGS_FOLDER_CERTIFICATES_FILE_NAME=${LOGS_FOLDER_CERTIFICATES_FILE_NAME}"        > ./DAFT-folders.properties"""
//          sh """echo "LOGS_FOLDER_SIMULATORS_FILE_NAME=${LOGS_FOLDER_SIMULATORS_FILE_NAME}"           >> ./DAFT-folders.properties"""
//          sh """echo "LOGS_FOLDER_TRAFFIC_CONFIG_FILE_NAME=${LOGS_FOLDER_TRAFFIC_CONFIG_FILE_NAME}"   >> ./DAFT-folders.properties"""
//          sh """echo "LOGS_FOLDER_TRAFFIC_RUN_FILE_NAME=${LOGS_FOLDER_TRAFFIC_RUN_FILE_NAME}"         >> ./DAFT-folders.properties"""
//
//          archiveArtifacts allowEmptyArchive: false, artifacts: 'DAFT-folders.properties', onlyIfSuccessful: false

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
                    def emailbody_job_failure_1="\nthe automatic process for preparing traffic via DAFT"
                    def emailbody_job_failure_2=" on the IaaS node ${IAAS_NODE} has failed.\n\nDetailed logs for the overall jenkins job can be"
                    def emailbody_job_failure_3=" found under: ${LOGS_LINK}/$BUILD_NUMBER/consoleFull"

                    emailext body: "${emailbody_start}"+"${emailbody_job_failure_1}"+"${emailbody_job_failure_2}"+"${emailbody_job_failure_3}"+"${emailbody_end}",
                    subject: "Automatic process for preparing traffic via DAFT failed", to: "${env.MAIL}"
                }
            }
        }
    }
}

def uploadAndCheckExpect ()
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
