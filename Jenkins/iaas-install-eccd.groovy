def emailbody_start="Hi,"
def emailbody_end="\n\nBR,\nYour humble Jenkins"
def emailbody_abort_job_1="\nthere is still a ${JOB_BASE_NAME} job type running on ${IAAS_NODE}. Please wait until that job has finished."
def emailbody_abort_job_2="\nThis one (build number ${BUILD_NUMBER}) will stop now."

pipeline
{
    agent
    {
        node
        {
            label '5G-SC'
            customWorkspace "/home/jenkins/workspace/5G-ESC/IaaS/${JOB_BASE_NAME}_${IAAS_NODE}"
        }
    }

    environment
    {
        // variables from initial form
        IAAS_NODE = "${IAAS_NODE}"
        I_HAVE_READ_THE_WARNING = "${I_HAVE_READ_THE_WARNING}"
        NW_CONFIG_FILE = "${NW_CONFIG_FILE}"
        ECCD_TEMPLATE_FILE = "${ECCD_TEMPLATE_FILE}"
        ECCD_ENVIRONMENT_FILE = "${ECCD_ENVIRONMENT_FILE}"
        ECCD_STACK_NAME = "${ECCD_STACK_NAME}"
        ECCD_STACK_NAME_TO_BE_DELETED = "${ECCD_STACK_NAME_TO_BE_DELETED}"
        ECCD_STACK_TYPE = "${ECCD_STACK_TYPE}"
        OPENSTACK_TIMEOUT = "${OPENSTACK_TIMEOUT}"
        MAIL = "${MAIL}"
        SKIP_STACK_DELETE = "${SKIP_STACK_DELETE}"
        SKIP_STACK_INSTALLATION = "${SKIP_STACK_INSTALLATION}"
        DAFT_DIR = "./daft"
        NW_CONFIG_FILE_DIR = "${DAFT_DIR}/network_config_files"
        DAFT_LOGS_DIR = "./workspaces"
        JENKINS_INSTANCE = "https://fem1s10-eiffel029.eiffel.gic.ericsson.se:8443/jenkins/job/5G-ESC/job/IaaS/job"
        JENKINS_INSTANCE_NEW = "https://fem3s10-eiffel029.eiffel.gic.ericsson.se:8443/jenkins/job/5G-ESC/job/IaaS/job"
    }

    stages
    {
        stage('Check confirmation message')
        {
            steps
            {
                script
                {
                    if ("${env.I_HAVE_READ_THE_WARNING}".contains("no"))
                    {
                        echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! "
                        echo "!!! You did not confirm that you have read the WARNING message by setting parameter I_HAVE_READ_THE_WARNING=yes !!! "
                        echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! "
                        sh """exit 1"""
                    }
                }
            }
        }

        stage('Workspace check')
        {
            steps
            {
                script
                {

                    FULL_WORKSAPCE = """${sh( returnStdout: true, script: "pwd")}""".trim()
                    OWN_WORKSPACE = """${sh( returnStdout: true, script: "pwd | cut -d '/' -f7")}""".trim()
                }

                script
                {
                    if ("${OWN_WORKSPACE}" != "${JOB_BASE_NAME}_${IAAS_NODE}")
                    {
                        if ("${env.MAIL}" != 'null')
                        {
                            emailext body: "${emailbody_start}"+"${emailbody_abort_job_1}"+"${emailbody_abort_job_2}"+"${emailbody_end}",
                            subject: 'Attempt to run parallel jobs on the same node aborted', to: "${env.MAIL}"
                        }

                        echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! "
                        echo "!!! There is still a ${JOB_BASE_NAME} job type running on ${IAAS_NODE}. Please wait until that job has finished !!! "
                        echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! "
                        sh """exit 1"""
                    }
                }
            }
        }
/*
        stage('Freeing up workspace on other buildslaves')
        {
            steps
            {
                sh ''' /bin/bash -x
                        IP_ADDRESS_TMP=`hostname -i`;
                        BUILD_PATH_TMP=`pwd`;
                        for i in `seq 60 62`;
                        do
                            TEMP_IP="10.210.174.$i";

                            if [ "${IP_ADDRESS_TMP}" != "${TEMP_IP}" ];
                            then
                                ssh eiffelesc@${TEMP_IP} "if [ -d ${BUILD_PATH_TMP} ]; then rm -rf ${BUILD_PATH_TMP}*; else echo \"Nothing to clean\"; fi;"
                            fi;
                        done;'''
            }
        }
 */
        stage('Get needed info')
        {
            steps
            {
                sh "sudo rm -rf ../${JOB_BASE_NAME}_${IAAS_NODE}@2*"

                getData()

                wrap([$class: 'BuildUser'])
                {
                    script
                    {
                        triggering_user = """${sh( returnStdout: true, script: "echo ${BUILD_USER_ID}")}""".trim()
                    }
                }
            }
        }

        stage('Trigger eccd installation')
        {
            steps
            {
                script
                {
                    echo "Trigger eccd installation"

                    if ("${env.ECCD_STACK_TYPE}" == 'non-capo')
                    {
                        if ("${env.ECCD_STACK_NAME_TO_BE_DELETED}" != 'null')
                        {
                            sh "${DAFT_DIR}/perl/bin/execute_playlist.pl -p 802_ECCD_Installation -c no \
                                -j ${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}_Install_ECCD \
                                -n ${NW_CONFIG_FILE_FULL_PATH} \
                                -v EXPECT_DEFAULT_TIMEOUT=14400 \
                                -v ECCD_TEMPLATE_FILE=${ECCD_TEMPLATE_FILE} \
                                -v ECCD_ENVIRONMENT_FILE=${ECCD_ENVIRONMENT_FILE} \
                                -v ECCD_STACK_NAME=${ECCD_STACK_NAME} \
                                -v ECCD_STACK_NAME_TO_BE_DELETED=${ECCD_STACK_NAME_TO_BE_DELETED} \
                                -v ECCD_STACK_TYPE=${ECCD_STACK_TYPE} \
                                -v OPENSTACK_TIMEOUT=${OPENSTACK_TIMEOUT} \
                                -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=yes \
                                -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=yes \
                                -v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=yes \
                                -v INPUT_AVAILABLE=no \
                                -v SKIP_STACK_DELETE=${SKIP_STACK_DELETE} \
                                -v SKIP_STACK_INSTALLATION=${SKIP_STACK_INSTALLATION} \
                                -w ${DAFT_LOGS_DIR}"
                        } else {
                            sh "${DAFT_DIR}/perl/bin/execute_playlist.pl -p 802_ECCD_Installation -c no \
                                -j ${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}_Install_ECCD \
                                -n ${NW_CONFIG_FILE_FULL_PATH} \
                                -v EXPECT_DEFAULT_TIMEOUT=14400 \
                                -v ECCD_TEMPLATE_FILE=${ECCD_TEMPLATE_FILE} \
                                -v ECCD_ENVIRONMENT_FILE=${ECCD_ENVIRONMENT_FILE} \
                                -v ECCD_STACK_NAME=${ECCD_STACK_NAME} \
                                -v ECCD_STACK_TYPE=${ECCD_STACK_TYPE} \
                                -v OPENSTACK_TIMEOUT=${OPENSTACK_TIMEOUT} \
                                -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=yes \
                                -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=yes \
                                -v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=yes \
                                -v INPUT_AVAILABLE=no \
                                -v SKIP_STACK_DELETE=${SKIP_STACK_DELETE} \
                                -v SKIP_STACK_INSTALLATION=${SKIP_STACK_INSTALLATION} \
                                -w ${DAFT_LOGS_DIR}"
                        }
                    } else {
                        if ("${env.ECCD_STACK_NAME_TO_BE_DELETED}" != 'null')
                        {
                            sh "${DAFT_DIR}/perl/bin/execute_playlist.pl -p 802_ECCD_Installation -c no \
                                -j ${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}_Install_ECCD \
                                -n ${NW_CONFIG_FILE_FULL_PATH} \
                                -v EXPECT_DEFAULT_TIMEOUT=14400 \
                                -v ECCD_ENVIRONMENT_FILE=${ECCD_ENVIRONMENT_FILE} \
                                -v ECCD_STACK_NAME=${ECCD_STACK_NAME} \
                                -v ECCD_STACK_NAME_TO_BE_DELETED=${ECCD_STACK_NAME_TO_BE_DELETED} \
                                -v ECCD_STACK_TYPE=${ECCD_STACK_TYPE} \
                                -v OPENSTACK_TIMEOUT=${OPENSTACK_TIMEOUT} \
                                -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=yes \
                                -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=yes \
                                -v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=yes \
                                -v INPUT_AVAILABLE=no \
                                -v SKIP_STACK_DELETE=${SKIP_STACK_DELETE} \
                                -v SKIP_STACK_INSTALLATION=${SKIP_STACK_INSTALLATION} \
                                -w ${DAFT_LOGS_DIR}"
                        } else {
                            sh "${DAFT_DIR}/perl/bin/execute_playlist.pl -p 802_ECCD_Installation -c no \
                                -j ${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}_Install_ECCD \
                                -n ${NW_CONFIG_FILE_FULL_PATH} \
                                -v EXPECT_DEFAULT_TIMEOUT=14400 \
                                -v ECCD_ENVIRONMENT_FILE=${ECCD_ENVIRONMENT_FILE} \
                                -v ECCD_STACK_NAME=${ECCD_STACK_NAME} \
                                -v ECCD_STACK_TYPE=${ECCD_STACK_TYPE} \
                                -v OPENSTACK_TIMEOUT=${OPENSTACK_TIMEOUT} \
                                -v CREATE_PACKED_WORKSPACE_FILE_AT_FAILURE=yes \
                                -v CREATE_PACKED_WORKSPACE_FILE_AT_SUCCESS=yes \
                                -v PACK_TROUBLESHOOTING_LOGS_SEPARATELY=yes \
                                -v INPUT_AVAILABLE=no \
                                -v SKIP_STACK_DELETE=${SKIP_STACK_DELETE} \
                                -v SKIP_STACK_INSTALLATION=${SKIP_STACK_INSTALLATION} \
                                -w ${DAFT_LOGS_DIR}"
                        }
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
                            def emailbody_rollback_failure_1="\nthe ECCD installation process via DAFT on node ${IAAS_NODE} has failed.\n\nDetailed logs regarding the ECCD installation can be found"
                            def emailbody_rollback_failure_2=" under the folder: ${FULL_WORKSAPCE}/workspaces/${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}_Install_ECCD"
                            emailext body: "${emailbody_start}"+"${emailbody_rollback_failure_1}"+"${emailbody_rollback_failure_2}"+"${emailbody_end}",
                            subject: 'ECCD installation process during automatic IaaS ECCD installation failed', to: "${env.MAIL}"
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
                        def emailbody_job_finished_1="\nthe automatic ECCD installation via DAFT on node ${IAAS_NODE} has concluded.\n\nDetailed logs for the ECCD installation itself can be found on the"
                        def emailbody_job_finished_2=" under the folder: ${FULL_WORKSAPCE}/workspaces/${IAAS_NODE}_${JOB_BASE_NAME}-${BUILD_NUMBER}_Install_ECCD"
                        def emailbody_job_finished_3="\nDetailed logs for the overall  jenkins job can be found under: ${LOGS_LINK}/$BUILD_NUMBER/consoleFull"
                        emailext body: "${emailbody_start}"+"${emailbody_job_finished_1}"+"${emailbody_job_finished_2}"+
                        "${emailbody_job_finished_3}"+"${emailbody_end}",
                        subject: 'Automatic IaaS ECCD installation concluded', to: "${env.MAIL}"
                    }
                }
            }
        }
    }
    post
    {
        always
        {
            archiveArtifacts allowEmptyArchive: true, artifacts: 'workspaces/*.tar.bz2', onlyIfSuccessful: false
            cleanWs()
        }
        failure
        {
            script
            {
                if ("${env.MAIL}" != 'null')
                {
                    def emailbody_job_failure_1="\nthe job installing ECCD for the IaaS node ${IAAS_NODE} has failed.\n\nDetailed logs for the overall ECCD installation jenkins"
                    def emailbody_job_failure_2=" job can be found under: ${LOGS_LINK}/$BUILD_NUMBER/consoleFull"
                    emailext body: "${emailbody_start}"+"${emailbody_job_failure_1}"+"${emailbody_job_failure_2}"+"${emailbody_end}",
                    subject: 'Automatic IaaS ECCD installation failed', to: "${env.MAIL}"
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

        // get the nw config file
        sh """  if [ '${env.NW_CONFIG_FILE}' = 'null' ];
        then
            echo "${NW_CONFIG_FILE_DIR}/${IAAS_NODE}.xml"  > ./var.nw-config-file
        else
            echo "${env.NW_CONFIG_FILE}"  > ./var.nw-config-file
        fi;"""

        NW_CONFIG_FILE_FULL_PATH = """${sh( returnStdout: true, script: "cat ./var.nw-config-file")}""".trim()
        BRANCH_VERSION = "master"
    }
}
