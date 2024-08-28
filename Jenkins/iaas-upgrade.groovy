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
        ARTIFACTORY_TOKEN = "Token for armdocker" /* iaas: not shown in the initial form as always gets this value */
//      variables from initial form
        IAAS_NODE = "${IAAS_NODE}"
        CSAR_PACKAGE_VERSION = "${CSAR_PACKAGE_VERSION}"
        NW_CONFIG_FILE = "${NW_CONFIG_FILE}"
        ERIC_SC_VALUES_YAML_FILE ="${ERIC_SC_VALUES_YAML_FILE}"
        DEPLOYMENT_VARIANT ="${DEPLOYMENT_VARIANT}"
        EXTRA_CONFIG_YAML_FILE ="${EXTRA_CONFIG_YAML_FILE}"
        ENABLED_CNF = "${ENABLED_CNF}"
        RESOURCES = "${RESOURCES}"
        IGNORE_ALARMS = "${IGNORE_ALARMS}"
        SKIP_DEPLOY_UPGRADE_CRD = "${SKIP_DEPLOY_UPGRADE_CRD}"
        SKIP_PRE_HEALTHCHECK = "${SKIP_PRE_HEALTHCHECK}"
        SKIP_POST_HEALTHCHECK = "${SKIP_POST_HEALTHCHECK}"
        COLLECT_ADP_LOGS_BEFORE_AND_AFTER_UPGRADE = "${COLLECT_ADP_LOGS_BEFORE_AND_AFTER_UPGRADE}"
        MAIL = "${MAIL}"
        REDUCED_RESOURCES_DIR ="./csar/external"
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
                // Assign paths to the logs
                script
                {
                    // Assign paths to the logs
                    LOGS_LINK = """${sh( returnStdout: true,
                    script: "echo ${JENKINS_INSTANCE}/${JOB_BASE_NAME}")}""".trim()

                    // Assign director-0 ip address
                    sh "./scripts/bash/get_directors_ip.bash ${IAAS_NODE}"

                    NODE_DIRECTOR_IP = """${sh( returnStdout: true, script: "cat ./var.DIRECTOR-0-IP")}""".trim()

                    // CSAR_PACKAGE_VERSION cannot be left empty
                    sh """if [ '${env.CSAR_PACKAGE_VERSION}' = 'null' ];
                    then
                          echo 'Please indicate the csar package the job should use'
                          exit 1
                    fi;"""

                    VALUES_YAML_FILE_NAME = """${sh( returnStdout: true, script: "echo '${env.ERIC_SC_VALUES_YAML_FILE}' | sed 's?^.*/??'")}""".trim()
                    EXTRA_CONFIG_YAML_FILE_NAME = """${sh( returnStdout: true, script: "echo '${env.EXTRA_CONFIG_YAML_FILE}' | sed 's?^.*/??'")}""".trim()

                    // get the nw config file
                    sh """  if [ '${env.NW_CONFIG_FILE}' = 'null' ];
                        then
                            echo "${NW_CONFIG_FILE_DIR}/${IAAS_NODE}.xml"  > ./var.nw-config-file
                        else
                            echo "${env.NW_CONFIG_FILE}"  > ./var.nw-config-file
                        fi;"""

                    if (("${ENABLED_CNF}" == "dsc") && (getPackageType() == "non-cncs")) // For pure cnDSC (non-cncs pkg) design base applies
                    {
                        ENABLED_CNF = "cnDSC" // This assignment does not have an impact in the logic. It is just to show in all messages and markings to the outside that it is a dsc non-cncs situation
                        APP="cnDSC"
                        ADD_CNF_CMD=""
                        TRIGGER_MSG="Trigger the cnDSC upgrade"
                        USE_PLAYLIST = "010_Upgrade_${APP}"
                        NW_CONFIG_OPTION="--network-config-option=DSC"
                        VALUE_NS="value_DSC"
                        VALUE_RELEASE="value_DSC"
                        REPO_LINK = "-l https://arm.seli.gic.ericsson.se/artifactory/proj-cndsc-generic-local/eiffelesc/"
                    }
                    else /* it is SC or EVNFM scenario */
                    {
                        APP = "SC"
                        ADD_CNF_CMD="-v ENABLED_CNF=${ENABLED_CNF}"
                        if ("${ENABLED_CNF}".contains("dsc"))
                        {
                            ADD_CNF_CMD = ADD_CNF_CMD + " -o OBJECTSTORAGE"
                        }
                        VALUE_NS="value"
                        VALUE_RELEASE="value"
                        REPO_LINK = ""
                        if ("${DEPLOYMENT_VARIANT}".contains("EVNFM")) /* it has to be distinguished between EVNFM or SC trigger */
                        {
                            TRIGGER_MSG="Trigger evnfm upgrade"
                            USE_PLAYLIST = "204_EVNFM_Upgrade_${APP}"
                        }
                        else
                        {
                            TRIGGER_MSG="Trigger the SC upgrade"
                            USE_PLAYLIST = "002_Upgrade_${APP}"
                            NW_CONFIG_OPTION=""
                        }
                    }
                    if ("${DEPLOYMENT_VARIANT}" != 'Legacy')
                    {
                        NW_CONFIG_OPTION = "--network-config-option=${DEPLOYMENT_VARIANT}"
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
                    DAFT_NS_DIR_ESCAPED = "\\/home\\/eccd\\/daft_ns_${DAFT_NS}"

                    // The NS is stored at ${DAFT_NS}.
                    // The sc release name is extracted from the nw config file
                    SC_NAME = """${sh( returnStdout: true, script: "sed -n '/<parameter name=\"sc_release_name\"/,/>/p' ${NW_CONFIG_FILE_FULL_PATH} | grep ${VALUE_RELEASE}= | cut -d '\"\' -f2")}""".trim()

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
/*
                    if ("${ENABLED_CNF}".contains("bsf"))
                    {
                        ENABLED_CNF = ENABLED_CNF + "-wcdb"
                    }
                    if (("${ENABLED_CNF}".contains("scp")) || ("${ENABLED_CNF}".contains("sepp")))
                    {
                        ENABLED_CNF = ENABLED_CNF + "-rlf"
                    }

 */
                    // Verdicts initialization
                    UPGRADE_ITSELF = "<not_executed>"
                    KPI_UPGRADE_SUCCESS_RATE = "<not_executed>"
                    KPI_UPGRADE_EXIT_SUCCESS_RATE = "<not_executed>"
                    TRAFFIC_PERFORMANCE_CHECK = "<not_executed>"
/*
                    Check whether the upgrade takes place under traffic. The logic works as follows:
                    Irrespective of whether the traffic is triggered via CI/DAFT, the sftp server is always deployed. The only difference is the ns where it is deployed:
                        -For CI is on the same ns as the main deployment
                        -For DAFT is on the ns as the main deployment followed by "-tools"
                    We will check if sftp pod is deployed in one of the two
 */
                    TRAFFIC_POD_CI = "kubectl -n ${DAFT_NS} get pods | grep eric-atmoz-sftp"
                    TRAFFIC_POD_EXISTENCE_CI = """${sh( returnStdout: true,
                            script: "ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP} '${TRAFFIC_POD_CI}' 2>/dev/null || true")}""".trim()

                    TRAFFIC_POD_DAFT = "kubectl -n ${DAFT_NS}-tools get pods | grep eric-atmoz-sftp"
                    TRAFFIC_POD_EXISTENCE_DAFT = """${sh( returnStdout: true,
                            script: "ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP} '${TRAFFIC_POD_DAFT}' 2>/dev/null || true")}""".trim()

                    if ( ("${TRAFFIC_POD_EXISTENCE_CI}" == '') && ("${TRAFFIC_POD_EXISTENCE_DAFT}" == '') ) /* No sftp pod found in any of the 2 ns. It is an upgrade without traffic */
                    {
                        UPGRADE_UNDER_TRAFFIC = "NO"
                        ADD_WAIT_CMD="-v SKIP_TRAFFIC_DELAY=yes"
                    }
                    else
                    {
                        UPGRADE_UNDER_TRAFFIC = "YES"
                        ADD_WAIT_CMD=""
                    }
// create file for artifact list
                    sh """echo "daft.properties"   > ./names_array_split.txt"""
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
                                echo " If the file exists please provide at least read permissions for the 'Others Rights group' for the whole path chain up to including the file.";
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

                script
                {
                    if ("${env.RESOURCES}".contains("ucc"))
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
                }
                script
                {
                    ADD_YAML_FILE_CMD = """${sh( returnStdout: true, script: "cat ./var.add-yaml-file-cmd")}""".trim()
                }
            }
        }
        stage('Trigger upgrade')
        {
            steps
            {
                script
                {
/*
                    With the introduction of cncs this way to get the deployed pkg is not valid anymore, as there is neither a "single" eric-sc anymore nor a proper version associated to the related csar pkg
                    Example:
                    eccd@eevee-capo-controlplane-kkpkk:~> helm list -n eiffelesc
                    NAME                          	NAMESPACE	REVISION	UPDATED                                	STATUS  	CHART                                 	APP VERSION
                    eric-cloud-native-base        	eiffelesc	1       	2024-04-19 09:29:47.27957739 +0000 UTC 	deployed	eric-cloud-native-base-152.2.0        	152.2.0
                    eric-cloud-native-nf-additions	eiffelesc	1       	2024-04-19 09:30:41.267513151 +0000 UTC	deployed	eric-cloud-native-nf-additions-48.10.0	48.10.0
                    eric-sc-bsf                   	eiffelesc	1       	2024-04-19 09:31:12.077653743 +0000 UTC	deployed	eric-sc-bsf-1.1.0-3-h007f4af          	1.1.0-3
                    eric-sc-cs                    	eiffelesc	1       	2024-04-19 09:31:02.661288087 +0000 UTC	deployed	eric-sc-cs-2.0.0-23-h0e0dfed          	2.0.0-23
                    eric-sc-scp                   	eiffelesc	1       	2024-04-19 09:39:41.307352452 +0000 UTC	deployed	eric-sc-scp-1.1.0-4-h007f4af          	1.1.0-4
                    eric-sc-sepp                  	eiffelesc	1       	2024-04-19 09:39:44.588991522 +0000 UTC	deployed	eric-sc-sepp-1.1.0-3-h007f4af         	1.1.0-3

                    This particular searched info is already displayed in the summary of the Jira exec run associated to the specific upgrade, example:
                    ISSU SEPP with Base SC 1.14 with Traffic via CI to 1.15.25+825 at node sc-flexikube-39357 filed on 2024/04/20 04:47:48

                    Hence it will be omitted here and the related needed logic will not be executed.
                    The corresponding pre-appending part is changed
                    from    -j ${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}_Upgrade_${NF}_from_${DEPLOYED_VERSION}_to_${CSAR_PACKAGE_VERSION}
                    to      -j ${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}_Upgrade_${NF}_to_${CSAR_PACKAGE_VERSION}

                    if ("${USE_DEPLOY_METHOD}" == 'cnDSC')
                    {
                        HELM_CMD = "helm ls -n ${DAFT_NS} | grep eric-dsc | cut -f 7"
                    }
                    else // it is SC or EVNFM scenario
                    {
                        HELM_CMD = "helm ls -n ${DAFT_NS} | grep eric-sc | cut -f 6 | cut -d- -f4"
                    }

                    DEPLOYED_VERSION = """${sh( returnStdout: true,
                    script: "ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP} '${HELM_CMD}' 2>/dev/null || true")}""".trim()

                    if ("${DEPLOYED_VERSION}" == '')
                    {
                        DEPLOYED_VERSION = """${sh( returnStdout: true, script: "echo 'unknown'")}""".trim()
                    }
*/
                    echo "${TRIGGER_MSG}"

                    sh "${DAFT_DIR}/expect/bin/send_command_to_ssh.exp --timeout=3600  --user=eccd --ip=${NODE_DIRECTOR_IP} --password='notneeded' \
                    --stop-on-error --command='perl  ${DAFT_NS_DIR}/perl/bin/execute_playlist.pl -p ${USE_PLAYLIST} -c no -v SOFTWARE_DIR=/home/eccd/download/${CSAR_PACKAGE_VERSION}/ \
                    -n ${DAFT_NS_DIR}/${NW_CONFIG_FILE_NAME} ${NW_CONFIG_OPTION} ${ADD_CNF_CMD} ${ADD_WAIT_CMD} ${ADD_YAML_FILE_CMD} \
                    -j ${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}_Upgrade_${NF}_to_${CSAR_PACKAGE_VERSION} \
                    -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=yes -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=yes -v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=yes -v INPUT_AVAILABLE=no \
                    -v IGNORE_ALARMS=${IGNORE_ALARMS} -v SKIP_PRE_HEALTHCHECK=${SKIP_PRE_HEALTHCHECK} -v SKIP_POST_HEALTHCHECK=${SKIP_POST_HEALTHCHECK} \
                    -v COLLECT_LOGS_BEFORE_UPGRADE=${COLLECT_ADP_LOGS_BEFORE_AND_AFTER_UPGRADE} -v SKIP_DEPLOY_UPGRADE_CRD=${SKIP_DEPLOY_UPGRADE_CRD} \
                    -v COLLECT_LOGS_AT_SUCCESS=${COLLECT_ADP_LOGS_BEFORE_AND_AFTER_UPGRADE} -v SPECIAL_IDENTIFIER=${DAFT_NS} -w ${DAFT_LOGS_DIR}'"
                } //script
            }
            post
            {
                always
                {
                    script
                    {
                        LOGS_FOLDER_UPGRADE = """${sh( returnStdout: true,
                            script: "ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP} 'find ${DAFT_LOGS_DIR} -type d -name *${JOB_BASE_NAME}-${BUILD_NUMBER}_Upgrade*'")}""".trim()

                        if ("${UPGRADE_UNDER_TRAFFIC}" == 'YES')
                        {
                            sh "scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP}:${LOGS_FOLDER_UPGRADE}/summary.txt ./. 2>/dev/null || true"

                            sh "scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP}:${LOGS_FOLDER_UPGRADE}/logfiles/kpi_statistics/difference_entry_exit_kpi.txt ./difference_entry_exit_kpi-upgrade-${BUILD_NUMBER}.txt 2>/dev/null || true"
                            sh """ls *entry_exit_kpi*.txt >> ./names_array_split.txt 2>/dev/null || true"""

                            archiveArtifacts allowEmptyArchive: true, artifacts: '*entry_exit_kpi*.txt', onlyIfSuccessful: false

                            sh "scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP}:${LOGS_FOLDER_UPGRADE}/logfiles/kpi_statistics/exit_verdict/kpi_verdict_summary.txt ./kpi_upgrade_exit_verdict_summary-upgrade-${BUILD_NUMBER}.txt 2>/dev/null || true"
                            sh "scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o loglevel=ERROR eccd@${NODE_DIRECTOR_IP}:${LOGS_FOLDER_UPGRADE}/logfiles/kpi_statistics/upgrade_verdict/kpi_verdict_summary.txt ./kpi_upgrade_verdict_summary-upgrade-${BUILD_NUMBER}.txt 2>/dev/null || true"
                            sh """ls *_verdict_summary*.txt >> ./names_array_split.txt 2>/dev/null || true"""
                            archiveArtifacts allowEmptyArchive: true, artifacts: '*_verdict_summary*.txt', onlyIfSuccessful: false
                        }
                    }
                }
                success
                {
                    script
                    {
                        UPGRADE_ITSELF ="SUCCEEDED"
                    }
                }
                failure
                {
                    script
                    {
                        UPGRADE_ITSELF ="FAILED"
                        if ("${env.MAIL}" != 'null')
                        {
                            def emailbody_upgrade_failure_1="\nthe automatic upgrade via DAFT of the csar package called ${SC_NAME}-${CSAR_PACKAGE_VERSION}.csar"
                            def emailbody_upgrade_failure_2=" on node ${IAAS_NODE} has failed.\n\nDetailed logs regarding the upgrade can be found on the"
                            def emailbody_upgrade_failure_3=" director of node ${IAAS_NODE} (IP address of director: ${NODE_DIRECTOR_IP}) under the folder: ${LOGS_FOLDER_UPGRADE}"

                            emailext body: "${emailbody_start}"+"${emailbody_upgrade_failure_1}"+"${emailbody_upgrade_failure_2}"+"${emailbody_upgrade_failure_3}"+"${emailbody_end}",
                            subject: "Upgrade process of a csar package (${APP}) during automatic IaaS upgrade failed", to: "${env.MAIL}"
                        }
                    }
                }
            }
        }
        stage('Evaluate KPI')
        {
            when
            {
                expression {UPGRADE_UNDER_TRAFFIC=="YES"}
            }
            steps
            {
                script
                {
                    SEARCH_STRING = "Upgrade KPI Success Rate Verdict was successful"
                    FAILING_PART = """${sh( returnStdout: true, script:""" cat ./summary.txt | grep '${SEARCH_STRING}' 2>/dev/null || true""")}""".trim()

                    if ("${FAILING_PART}".contains("${SEARCH_STRING}"))
                    {
                        KPI_UPGRADE_SUCCESS_RATE = "SUCCEEDED"
                    }
                    else
                    {
                        KPI_UPGRADE_SUCCESS_RATE = "FAILED"
                    }

                    SEARCH_STRING = "Upgrade Exit KPI Success Rate Verdict was successful"
                    FAILING_PART = """${sh( returnStdout: true, script:""" cat ./summary.txt | grep '${SEARCH_STRING}' 2>/dev/null || true""")}""".trim()

                    if ("${FAILING_PART}".contains("${SEARCH_STRING}"))
                    {
                        KPI_UPGRADE_EXIT_SUCCESS_RATE = "SUCCEEDED"
                    }
                    else
                    {
                        KPI_UPGRADE_EXIT_SUCCESS_RATE = "FAILED"
                    }

                    SEARCH_STRING = "Traffic Performance Check was successful"
                    SEARCH_STRING_2 = "KPI Compare Check was successful"
                    FAILING_PART = """${sh( returnStdout: true, script:""" cat ./summary.txt | grep '${SEARCH_STRING}' 2>/dev/null || true""")}""".trim()
                    FAILING_PART_2 = """${sh( returnStdout: true, script:""" cat ./summary.txt | grep '${SEARCH_STRING_2}' 2>/dev/null || true""")}""".trim()

                    if (("${FAILING_PART}".contains("${SEARCH_STRING}")) && ("${FAILING_PART_2}".contains("${SEARCH_STRING_2}")))
                    {
                        TRAFFIC_PERFORMANCE_CHECK = "SUCCEEDED"
                    }
                    else
                    {
                        TRAFFIC_PERFORMANCE_CHECK = "FAILED"
                    }
                    if (("${KPI_UPGRADE_SUCCESS_RATE}" == 'FAILED') || ("${KPI_UPGRADE_EXIT_SUCCESS_RATE}" == 'FAILED') )
                    {
                        if ("${env.MAIL}" != 'null')
                        {
                            def emailbody_exc_failure_1="\nthe KPIs verdict for the automatic upgrade via DAFT of the csar package called ${SC_NAME}-${CSAR_PACKAGE_VERSION}.csar on node ${IAAS_NODE} has failed.\n\nDetailed logs regarding"
                            def emailbody_exc_failure_2=" the KPIs result can be found the director of node ${IAAS_NODE} (IP address of director: ${NODE_DIRECTOR_IP}) under the files:"
                            def emailbody_exc_failure_3=" ${LOGS_FOLDER_UPGRADE}/logfiles/kpi_statistics/upgrade_verdict/kpi_verdict_summary.txt and ${LOGS_FOLDER_UPGRADE}/logfiles/kpi_statistics/exit_verdict/kpi_verdict_summary.txt"

                            emailext body: "${emailbody_start}"+"${emailbody_exc_failure_1}"+"${emailbody_exc_failure_2}"+"${emailbody_exc_failure_3}"+"${emailbody_end}",
                                    subject: "Automatic KPIs verdict for IaaS upgrade (${APP}) failed", to: "${MAIL}"
                        }
                        error('Jenkins job will report failure to Spinnaker as the KPIs verdict was unsucessful.')
                    }
                }
            }
        }
        stage('Upload DAFT to artifactory')
        {
            when {expression {APP !="cnDSC"}}
            steps
            {
                withCredentials([string(credentialsId: 'armseligic-armdocker-eiffelesc-identity-token-for-Smoke-CI', variable: 'ARTIFACTORY_TOKEN')])
                {
                    sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" \\
                    --upload-file ${DAFT_DIR}/${ESC_PACKAGE_NAME} \\
                    https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/esc-daft/${ESC_PACKAGE_NAME}"""
                }
                sh """echo "DAFT_PKG_STORED_ARTIFACTORY=${ESC_PACKAGE_NAME}" > ./daft.properties"""
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
                        def emailbody_job_finished_1="\nthe automatic upgrade via DAFT of the csar package called ${SC_NAME}-${CSAR_PACKAGE_VERSION}.csar on node ${IAAS_NODE} has concluded."
                        def emailbody_job_finished_2="\n\nDetailed logs for the upgrade itself can be found on the director of node ${IAAS_NODE} (IP address of director:"
                        def emailbody_job_finished_3=" ${NODE_DIRECTOR_IP}) under the folder: ${LOGS_FOLDER_UPGRADE} \nDetailed logs for the overall upgrade"
                        def emailbody_job_finished_4=" jenkins job can be found under: ${LOGS_LINK}/$BUILD_NUMBER/consoleFull"

                        emailext body: "${emailbody_start}"+"${emailbody_job_finished_1}"+"${emailbody_job_finished_2}"+
                        "${emailbody_job_finished_3}"+"${emailbody_job_finished_4}"+"${emailbody_end}",
                        subject: "Automatic IaaS upgrade (${APP}) concluded", to: "${env.MAIL}"
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
            sh """ls *${JOB_BASE_NAME}-${BUILD_NUMBER}*.tar.bz2 | grep -v troubleshooting_logs >> ./names_array_split.txt 2>/dev/null || true"""

            sh """echo "UPGRADE_UNDER_TRAFFIC=${UPGRADE_UNDER_TRAFFIC}"                  > ./upgrade-result.txt"""
            sh """echo "UPGRADE_ITSELF=${UPGRADE_ITSELF}"                               >> ./upgrade-result.txt"""
            sh """echo "KPI_UPGRADE_SUCCESS_RATE=${KPI_UPGRADE_SUCCESS_RATE}"           >> ./upgrade-result.txt"""
            sh """echo "KPI_UPGRADE_EXIT_SUCCESS_RATE=${KPI_UPGRADE_EXIT_SUCCESS_RATE}" >> ./upgrade-result.txt"""
            sh """echo "TRAFFIC_PERFORMANCE_CHECK=${TRAFFIC_PERFORMANCE_CHECK}"         >> ./upgrade-result.txt"""

            sh """mv ./upgrade-result.txt ./VERDICT-upgrade-${BUILD_NUMBER}.txt"""
            sh """ls VERDICT-upgrade-${BUILD_NUMBER}.txt >> ./names_array_split.txt 2>/dev/null || true"""
            script
            {
                NUMBER_ADP_GROUPS = """${sh( returnStdout: true, script:""" ls *${JOB_BASE_NAME}-${BUILD_NUMBER}*.tar.bz2 | grep troubleshooting_logs | wc -l 2>/dev/null || true""")}""".trim()

                if ("${NUMBER_ADP_GROUPS}".toInteger() !=0)
                {
                unpackADPfiles("${NUMBER_ADP_GROUPS}".toInteger())
                }
            }
            artifactList()
            sh """echo "ARTIFACT_LIST=${ARTIFACT_LIST}" >> ./VERDICT-upgrade-${BUILD_NUMBER}.txt"""
            sh """echo "ARTIFACT_LIST=${ARTIFACT_LIST}" >> ./daft.properties"""

            archiveArtifacts allowEmptyArchive: true, artifacts: '*.tar.bz2', onlyIfSuccessful: false
            archiveArtifacts allowEmptyArchive: true, artifacts: 'VERDICT*.txt', onlyIfSuccessful: false
            archiveArtifacts allowEmptyArchive: true, artifacts: 'daft.properties', onlyIfSuccessful: false

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
                    def emailbody_job_failure_1="\nthe job for upgrading the csar package called ${SC_NAME}-${CSAR_PACKAGE_VERSION}.csar from artifactory"
                    def emailbody_job_failure_2=" on the IaaS node ${IAAS_NODE} has failed.\n\nDetailed logs for the overall upgrade jenkins job can be"
                    def emailbody_job_failure_3=" found under: ${LOGS_LINK}/$BUILD_NUMBER/consoleFull"

                    emailext body: "${emailbody_start}"+"${emailbody_job_failure_1}"+"${emailbody_job_failure_2}"+"${emailbody_job_failure_3}"+"${emailbody_end}",
                    subject: "Automatic IaaS upgrade (${APP}) failed", to: "${env.MAIL}"
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