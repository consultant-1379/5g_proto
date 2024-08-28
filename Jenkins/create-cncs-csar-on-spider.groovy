// Check which BS to use
def AGENT_LABEL = null

node('DSC_BuildSlave_GIC_1||DSC_BuildSlave_GIC_2||DSC_BuildSlave_GIC_3||DSC_BuildSlave_GIC_4||DSC_BuildSlave_GIC_5')
{
    stage('set agent')
    {
        if (BS_LABEL == 'null')
        {
            AGENT_LABEL = '5G-SC' // the user didn't select a specific bs
        }
        else
        {
            AGENT_LABEL = "${BS_LABEL}"      // take the bs selected by the user
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
/*
            label '5G-SC'
            label '5G-LMVL-1'
            label 'esc-docker || esc-docker-1 || esc-docker-2'
*/
        }
    }
    options
    {
        timeout(time: 45, unit: 'MINUTES')
        timestamps()
    }
    environment
    {
        ARMSERO_ARTIFACTORY_URL = "https://arm.sero.gic.ericsson.se/artifactory"
        SPIDER_CHART_HELM_REPO = "${SPIDER_CHART_HELM_REPO}"
        HELM_REPO_URL = "${ARMSERO_ARTIFACTORY_URL}/${SPIDER_CHART_HELM_REPO}"
        REPO = "proj-5g-bsf-generic-local"
        REPO_PATH = "https://arm.seli.gic.ericsson.se/artifactory/${REPO}/cncs/eiffelesc"
        RELEASE = true  /* iaas: hard coded to yes. We want to have a legible version number on the generated csar pkg. */
        //RELEASE = false  /* TEST for getting uniqueness versions for the same pkg charts numbering... */
        OUTPUT_DIR=".bob"
        KUBE_HOST = "kohn907" /* iaas: hard coded to hahn138. That cluster is not needed any more, but just to pass the bob-init rule. */
        //NAMESPACE = "${NAMESPACE}" /* iaas: will be null; only needed to pass the bob-init rule and not to search for a free namespace. */
        //ARTIFACTORY_TOKEN = "Token for armdocker" /* iaas: not shown in the initial form as always gets this value */
        BRANCH = "${BRANCH}"
        COMMIT = "${COMMIT}"
        SPIDER_CHART_TAG = "${SPIDER_CHART_TAG}"
        MAIL = "${MAIL}"
        BS_LABEL ="${BS_LABEL}"
//        ADD_MSG = "If you want to create a package at this stage, perform an empty push on the branch so to step the version, then create and upload a new eric-sc-spider chart to the corresponding helm repo and trigger a new csar package creation giving the tag version of the spider chart as input parameter."

        SIGNATURE = "${SIGNATURE}"
        // Preparation for moving to another Jenkins instance
        JENKINS_INSTANCE = "https://fem1s10-eiffel029.eiffel.gic.ericsson.se:8443/jenkins/job/5G-ESC/job/CSAR/view/All/job"
        JENKINS_INSTANCE_NEW = "https://fem1s30-eiffel029.eiffel.gic.ericsson.se:8443/jenkins/job/5G-ESC/job/CSAR/view/All/job"

        // Variables for the Supreme tool
        WANTED_SUPREME_VERSION = "1.0.12"
    }
    stages
    {
        stage('Fail for parallel running creation')
        {
            steps
            {
                script
                {

                    jobs_array = ["create-csar-pkg-general","csar-from-spider","csar-from-spider-RC"] // pipeline for csar-from-spider-RC hasn't been craeted yet
                    for (i=0; i<2; i++) //the last value is not being checked right now as its pipeline hasn't been craeted yet
                    {
                        CURRENT_JOB =jobs_array.get(i)
                        if ("$CURRENT_JOB" != "$JOB_BASE_NAME")
                        {
                            checkParallelCreation(CURRENT_JOB)
                        }
                    }
                }
            }
        }
        stage('Input Validation and cluster checks')
        {
            steps
            {
                script
                {
                    if (( !"${env.COMMIT}".contains("origin")) && ("${env.SPIDER_CHART_TAG}" != 'null')) // COMMIT and SPIDER_CHART_TAG cannot be filled simultaneously
                    {
                        error ("COMMIT and SPIDER_CHART_TAG cannot be filled simultaneously. Please fill out only one and retrigger.")
                    }
                    if (( "${env.SPIDER_CHART_HELM_REPO}".contains("Choose")) && ("${env.SPIDER_CHART_TAG}" != 'null')) // SPIDER_CHART_HELM_REPO has to be filled for SPIDER_CHART_TAG
                    {
                        error ("Choose the repo from this drop-down menu As SPIDER_CHART_TAG has been filled out, please choose the repo where the Spider chart is stored and retrigger.")
                    }
                    BRANCH_TYPE = """${sh(returnStdout: true, script: "echo ${BRANCH} | sed 's/^SC[0-9]*.[0-9]*.[0-9]*/RELEASE_BRANCH/g'")}""".trim()
                    if (("${JOB_BASE_NAME}" == "csar-from-spider-RC") && ("${BRANCH_TYPE}" != "RELEASE_BRANCH")) // Pipeline csar-from-spider-RC must be triggered for a release branch
                    {
                        error("Pipeline csar-from-spider-RC must be triggered for a release branch. For branch \"${BRANCH}\" please trigger the pipeline \"https://fem1s10-eiffel029.eiffel.gic.ericsson.se:8443/jenkins/job/5G-ESC/job/CSAR/job/csar-from-spider\"")
                    }
                    if (( "${env.SPIDER_CHART_TAG}" == "NIGHTLY-CNCS-CI") && ("${BRANCH}" != "master")) // TAG "NIGHTLY-CNCS-CI" only attached to master builds.
                    {
                        error("The tag \"${env.SPIDER_CHART_TAG}\" it is not attached to builds on branch \"${BRANCH}\". Please either replace that tag with an appropriate one or remove the value completely.")
                    }
                    if (( "${env.SPIDER_CHART_TAG}" == "NIGHTLY-CNCS-CI") && ("${SPIDER_CHART_HELM_REPO}" != "proj-sc-all-internal-helm")) // TAG "NIGHTLY-CNCS-CI" only for internal repo
                    {
                        error("The tag \"${env.SPIDER_CHART_TAG}\" should check for spider charts stored on the repo \"proj-sc-all-internal-helm\" but in this build repo \"${SPIDER_CHART_HELM_REPO}\" has been given. Please check if the value provided was correct.")
                    }
                    if (( "${env.SPIDER_CHART_TAG}" == "RELEASE-CNCS-CI") && ("${BRANCH_TYPE}" != "RELEASE_BRANCH")) // TAG "RELEASE-CNCS-CI" only attached to release branch
                    {
                        error("The tag \"${env.SPIDER_CHART_TAG}\" must be triggered for a release branch and branch \"${BRANCH}\" does not follow that pattern. Please check if the value provided was correct.")
                    }
                    /* deactivated until usage for drop repo is clarified
                    if (( "${env.SPIDER_CHART_TAG}" == "RELEASE-CNCS-CI") && ("${SPIDER_CHART_HELM_REPO}" != "proj-sc-all-drop-helm")) // TAG "RELEASE-CNCS-CI" only for drop repo
                    {
                        error("The tag \"${env.SPIDER_CHART_TAG}\" should check for spider charts stored on the repo \"proj-sc-all-drop-helm\" but in this build repo \"${SPIDER_CHART_HELM_REPO}\" has been given. Please check if the value provided was correct.")
                    }
                     */
                    if ("${env.SPIDER_CHART_TAG}" != 'null') // Check on the validity between repo where the spider chart is stored and branch
                    {
                        switch (SPIDER_CHART_HELM_REPO)
                        {
                            case [ 'proj-sc-all-internal-helm' ]: // Smoke build: branch master
                        /* deactivated until usage for drop repo is clarified
                                if ("${BRANCH}" != "master")
                                {
                                    error("The repo \"${env.SPIDER_CHART_HELM_REPO}\" it is associated for builds on master but branch \"${BRANCH}\" is given as parameter instead. Please check input correctness.")
                                }

                         */
                                break
                            case [ 'proj-sc-all-drop-helm' ]: // Release Candidate build: "RELEASE_BRANCH" type
                                if ("${BRANCH_TYPE}" != "RELEASE_BRANCH")
                                {
                                    error("The repo \"${env.SPIDER_CHART_HELM_REPO}\" it is associated for release candidate builds but branch \"${BRANCH}\" is not a release branch type. Please check input correctness.")
                                }
                                break
                            case [ 'proj-sc-all-released-helm' ]: // Currently not logic is implemented
                                break
                            default: // The rest is Team-CI build: branch cannot be master neither "RELEASE_BRANCH" type
                                if (("${BRANCH_TYPE}" == "RELEASE_BRANCH") || ("${BRANCH}" == "master"))
                                {
                                    error("The repo \"${env.SPIDER_CHART_HELM_REPO}\" it is associated for Team-CI builds. The value for parameter branch cannot be \"master\" neither a release branch type. Please check input correctness.")
                                }
                                break
                        } //switch
                    }
                    if ("${env.SPIDER_CHART_TAG}" != 'null')
                    {
                        MAIL_INFO= "from tag: $SPIDER_CHART_TAG "
                        SUBJECT_INFO= "from tag $SPIDER_CHART_TAG"
                        FILE_BRANCH_INFO = "${BRANCH}"
                    }
                    else
                    {
                        if ("${env.COMMIT}".contains("origin"))
                        {
                            MAIL_INFO= "from the development branch: ${BRANCH} "
                            SUBJECT_INFO= "from the development branch ${BRANCH}"
                            FILE_BRANCH_INFO = "${BRANCH}"
                        }
                        else
                        {
                            MAIL_INFO= ""
                            SUBJECT_INFO= "from commit ${env.COMMIT}"
                            FILE_BRANCH_INFO = "${BRANCH}, but irrelevant, as specific commit has been given at triggering"
                        }
                    }
                    LOGS_LINK = """${sh( returnStdout: true,
                    script: "echo ${JENKINS_INSTANCE}/${JOB_BASE_NAME}")}""".trim()
                    OWN_WORKSPACE = """${sh( returnStdout: true, script: "pwd | sed 's?^.*/??'")}""".trim() // If no SPIDER_CHART_TAG has been given, the bob rules will be executed from the current directory.
                                                                                                            // Otherwise the EXECUTING_DIR will be the one cloned after the SPIDER_CHART_TAG commit has been found.
                    EXECUTING_DIR ="../$OWN_WORKSPACE"
                    RELATIVE_DIR = OUTPUT_DIR
                    ADD_MAIL_MSG=" unexpectedly at the first steps. Please check"
                }
                script
                {
                    FREE_DISK_SPACE = """${sh( returnStdout: true,
                    script: "df -BG | grep -e /home/jenkins/workspace -e /local |awk '{print \$4}' | cut -d 'G' -f1")}""".trim()
                    if ("${FREE_DISK_SPACE}".toInteger() < 35)
                    {
                        if ("${env.MAIL}" != 'null')
                        {
                            def emailbody_disk_space_failure_1="\nthere are currently ${FREE_DISK_SPACE}GB free on $NODE_NAME. This is not enough for creating"
                            def emailbody_disk_space_failure_2=" a csar package as during the process at least 35GB are needed. Please make more disk space available."
                            def emailbody_disk_space_failure_3="\nAfterwards you might rebuild this job."

                            emailext body: "${emailbody_start}"+"${emailbody_disk_space_failure_1}"+"${emailbody_disk_space_failure_2}"+
                            "${emailbody_disk_space_failure_3}"+"${emailbody_end}",
                            subject: 'Not enough disk space for creating a csar package',
                            to: "${env.MAIL};issa.diomansy.koite@ericsson.com; claus.ortmann@ericsson.com"
                        }

                        echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! "
                        echo "!!!   There are currently ${FREE_DISK_SPACE}GB disk space free on $NODE_NAME. This is not enough for the csar package creation.    !!!"
                        echo "!!!   Please make more disk space available.     !!!"
                        echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! "
                        sh """exit 1"""
                    }
                }
                echo "There are currently ${FREE_DISK_SPACE}GB disk space free on $NODE_NAME. This should be enough for the csar package creation."
            }
        }
        stage('Get commit from Spider chart tag')
        {
            when
            { not { environment name: 'SPIDER_CHART_TAG', value: '' } }
            steps
            {
                dir('5g_proto_spider_commit')
                {
                    deleteDir()
                }
                script
                {
                    getSpiderCommit()
                    EXECUTING_DIR ="5g_proto_spider_commit" // The bob rules are to be executed from this directory
                    RELATIVE_DIR= "${EXECUTING_DIR}/${OUTPUT_DIR}"
                }
            }
        }
        stage('Initialize')
        {
            steps
            {
                dir("$EXECUTING_DIR")
                {
                    echo 'Creating output directory'
                    echo pwd()
                    sh 'ls -ltrh'
                    sh 'git submodule update --init --recursive'
                    sh './bob/bob sc.init:delete-output-dir'
                    sh './bob/bob base.init:create-output-dir'
                    sh './bob/bob base.init:copy-docker-config'
                    sh './bob/bob base.init:copy-helm-credentials'
                    sh './bob/bob base.init:set-kube-config'
                    sh './bob/bob base.init:api-tokens'
                }
            }
        }
        stage('Check numbering uniqueness')
        {
            steps
            {

                echo 'Get csar package number'
                dir("$EXECUTING_DIR")
                {
                sh './bob/bob -r csar/ruleset2.0.yaml init-csar'
                }
                script
                {
                    CSAR_PACKAGE_VERSION = """${sh( returnStdout: true, script: "cat ./${RELATIVE_DIR}/var.esc-version")}""".trim()

                    if ("${env.COMMIT}".contains("origin"))
                    {
                        dir("$EXECUTING_DIR")
                        {
                            COMMIT = """${sh( returnStdout: true, script: "git log --format=\"%h\" | head -1 ")}""".trim()
                        }
                    }
                    // set the displayed build name to "BUILD_NUMBER - COMMIT - TAG"
                    if ("${env.SPIDER_CHART_TAG}" != 'null')
                    {
                        DISPLAY_TAG = SPIDER_CHART_TAG
                    }
                    else
                    {
                        DISPLAY_TAG = "without tag"
                    }
                    currentBuild.displayName = "#${env.BUILD_NUMBER} - ${COMMIT} - ${DISPLAY_TAG}"

                    // set displayed description to "build slave, branch, csar pkg version"
                    currentBuild.description = "${env.NODE_NAME}, ${BRANCH}, ${CSAR_PACKAGE_VERSION}"

                    PATCH_TYPE = """${sh(returnStdout: true, script: "echo ${CSAR_PACKAGE_VERSION} | cut -d '+' -f1 | sed 's/^[0-9]*.[0-9]*.25/MASTER_PATTERN/g'")}""".trim()
                    if (("${PATCH_TYPE}" != "MASTER_PATTERN") && ("${BRANCH}" == "master")) // branch master expects pkgs format X.XX.25
                    {
                        error("Branch \"${BRANCH}\" expects packages format numbering X.XX.25 but this one gets value \"${CSAR_PACKAGE_VERSION}\". Please check whether the branch value the package is being created from is correct.")
                    }
                    if (("${BRANCH_TYPE}" == "RELEASE_BRANCH") && ("${PATCH_TYPE}" == "MASTER_PATTERN")) // release type branches do not expect pkgs format X.XX.25
                    {
                        error("Branch \"${BRANCH}\" is a release type branch and does not expect packages format numbering X.XX.25 but this one gets value \"${CSAR_PACKAGE_VERSION}\". Please check for correctness.")
                    }
                    // Check that there is no other pkg in artifactory with the same version
                    withCredentials([string(credentialsId: 'armseligic-armdocker-eiffelesc-identity-token-for-Smoke-CI', variable: 'ARTIFACTORY_TOKEN')])
                    {
                        sh """ curl -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" \
                        https://armdocker.rnd.ericsson.se/artifactory/api/search/aql \
                        -d 'items.find({"repo":{"\$eq":"'"${REPO}"'"}},{"type":{"\$eq":"folder"}},{"name":{"\$match":"'"${CSAR_PACKAGE_VERSION}"'"}})' \
                        -H "content-type:text/plain" > ./PKG_SEARCH.txt"""
                    }
                    PKG_EXISTS = """${sh( returnStdout: true, script: "cat ./PKG_SEARCH.txt | grep ${CSAR_PACKAGE_VERSION} 2>/dev/null || true")}""".trim()

                    if ( "${PKG_EXISTS}".contains("${CSAR_PACKAGE_VERSION}"))
                    {
                        PKG_PATH = REPO + "/" + """${sh( returnStdout: true, script: "cat ./PKG_SEARCH.txt | grep path | grep eiffelesc | cut -d '\"' -f4 ")}""".trim()
                        ADD_MAIL_MSG =". Package version already exists"
//                        error ("This package would get \"${CSAR_PACKAGE_VERSION}\" as version. There exists already a csar package with the same version stored at \"https://arm.seli.gic.ericsson.se/artifactory/${PKG_PATH}/${CSAR_PACKAGE_VERSION}\".\n${ADD_MSG}")
                        error ("This package would get \"${CSAR_PACKAGE_VERSION}\" as version. There already exists a csar package with the same version stored at \"https://arm.seli.gic.ericsson.se/artifactory/${PKG_PATH}/${CSAR_PACKAGE_VERSION}\".")
                    }
                    else
                    {
                        println ("No csar package found in artifactory with version \"${CSAR_PACKAGE_VERSION}\". Package creation continues.")
                        ADD_MAIL_MSG =""
                    }
                }
            }
        }
        stage('Spider information & dependencies versions')
        {
            steps
            {
                dir("$EXECUTING_DIR")
                {
                    script
                    {
                        if ("${env.SPIDER_CHART_TAG}" != 'null')
                        {
                            env.SPIDER_CHART_VERSION = SPIDER_CHART_TAG_VERSION
                            echo "Downloading the Spider chart (version ${env.SPIDER_CHART_VERSION}) from artifactory (${HELM_REPO_URL})"
                            withCredentials([usernamePassword(credentialsId: 'eiffelesc-credentials', usernameVariable: 'GERRIT_USERNAME', passwordVariable: 'GERRIT_PASSWORD'),
                                             usernamePassword(credentialsId: 'eiffelesc-armsero-token-credentials', usernameVariable: 'HELM_USER', passwordVariable: 'ARM_API_TOKEN')])
                            {
                                sh './bob/bob -r csar/ruleset2.0.yaml fetch-spider'
                            } // withCredentials
                        }
                        else
                        {
                            echo 'Getting the Spider chart info from the branch'
                            withCredentials([usernamePassword(credentialsId: 'eiffelesc-credentials', usernameVariable: 'GERRIT_USERNAME', passwordVariable: 'GERRIT_PASSWORD'),
                                             usernamePassword(credentialsId: 'eiffelesc-armsero-token-credentials', usernameVariable: 'HELM_USER', passwordVariable: 'ARM_API_TOKEN')])
                            {
                                sh './bob/bob -r csar/ruleset2.0.yaml fetch-dependencies-ci'
                            } // withCredentials
                        }
                        echo 'Update dependencies versions'
                        sh './bob/bob -r csar/ruleset2.0.yaml dependencies-versions'
                        echo 'The Spider Chart.yaml contains the following data:\n'
                        echo '**************************************************\n'
                        sh "cat ./${OUTPUT_DIR}/eric-sc-spider_tmp/eric-sc-spider/Chart.yaml"
                        echo '**************************************************\n'
                    }
                }
            }
        }
        stage('Check charts versions correctness')
        {
            steps
            {
                script
                {
                    charts_names=       ["eric-cloud-native-base","eric-cloud-native-nf-additions",  "eric-sc-cs"   ,   "eric-sc-bsf"  ,   "eric-sc-scp"  ,  "eric-sc-sepp"  ,  "eric-dsc"   ]
                    charts_variables=   [  "var.cncs-base-chart" ,        "var.cncs-add-chart"    ,"var.sc-cs-chart","var.sc-bsf-chart","var.sc-scp-chart","var.sc-sepp-chart", "var.dsc-chart"]
                    pointer=0
                    array_length = "${charts_names.size()}".toInteger()
                    // Check that the amount of chart names matches with the amount of dependencies shown (so to avoid that more dependencies appear, which are not included in this array check (eg when dsc joins and the array here has not been expanded)
                    dependencies_length = """${sh( returnStdout: true, script: "cat ./${RELATIVE_DIR}/eric-sc-spider_tmp/eric-sc-spider/Chart.yaml | grep '\\- name' | wc -l ")}""".trim().toInteger()

                    if (array_length != dependencies_length)
                    {
                        error("The logic expects $array_length chart dependencies but $dependencies_length are found in the Spider Chart. Process will stop now. Please check")
                    }


                    while ( pointer < array_length )
                    {
                       // example: spider_version = """${sh( returnStdout: true, script: 'awk \'f{print $2;f=0} /^\\s*pvtb:/{f=1}\' eric-sc-values.yaml')}""".trim()
                        //bup: current_chart = charts_names.get(pointer)
                        spider_version = """${sh( returnStdout: true, script: "awk \'c&&!--c;/${charts_names.get(pointer)}/{c=2}\' ./${RELATIVE_DIR}/eric-sc-spider_tmp/eric-sc-spider/Chart.yaml | sed 's/^.*: //' ")}""".trim()
                        check_helm_chart(charts_names.get(pointer),charts_variables.get(pointer),spider_version)
                        pointer=pointer+1
                    }
                    if ("${env.SPIDER_CHART_TAG}" != 'null')
                    {
                        // Check that the spider chart version in the Chart.yaml file matches the version of the chart downloaded from artifactory
                        SPIDER_CHART_YAML_VERSION = """${sh( returnStdout: true, script: "awk \'c&&!--c;/type/{c=1}\' ./${RELATIVE_DIR}/eric-sc-spider_tmp/eric-sc-spider/Chart.yaml | sed 's/^.*: //' ")}""".trim()
                        if ( "${SPIDER_CHART_YAML_VERSION}" != "${SPIDER_CHART_TAG_VERSION}")
                        {
                            error("The Spider chart version in the Chart.yaml file \"${SPIDER_CHART_YAML_VERSION}\" it is not the same as the version downloaded from artifactory \"${SPIDER_CHART_TAG_VERSION}\". Process will stop now. Please check")
                        }
                    }
                    else
                    {
                        SPIDER_CHART_YAML_VERSION="no spider chart downloaded"
                        ADD_INFO ="which is stored in the file \\\"Chart.yaml\\\" under \\\"5g_proto/eric-sc-spider/charts/eric-sc-spider\\\" for this commit ($COMMIT)"
                        ADD_INFO_MAIL ="which is stored in the file \"Chart.yaml\" under \"5g_proto/eric-sc-spider/charts/eric-sc-spider\" for this commit ($COMMIT)"
                    }
                    // update displayed description to "build slave, branch, csar pkg version, spider chart version"
                    currentBuild.description = "${env.NODE_NAME}, ${BRANCH}, ${CSAR_PACKAGE_VERSION}, ${SPIDER_CHART_YAML_VERSION}"
                }
            }
        }
        stage('Fetch CRDs')
        {
            steps
            {
                echo 'Fetch CRDs'
                dir("$EXECUTING_DIR")
                {
                    sh './bob/bob -r csar/ruleset2.0.yaml fetch-crds'
                }
            }
        }
        stage('Simulators')
        {
            steps
            {
/*
                script
                {
                    ADD_MAIL_MSG =". The package itself was created but might be incomplete (eg simulators,...). Please check"
                }
 */
//                catchError(buildResult: 'FAILURE', stageResult: 'FAILURE')
//                {
                    dir("$EXECUTING_DIR")
                    {
                        echo 'Initialization for simulators creation'
                        sh './bob/bob base.init:set-build-proxy'
                        sh './bob/bob base.init:mvn-args'
                        sh './bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml init:chfsim-version'
                        sh './bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml init:redis-version'
                        sh './bob/bob -r rulesets/ruleset2.0-eric-seppsim.yaml init:version'
                        sh './bob/bob -r rulesets/ruleset2.0-eric-nrfsim.yaml init:version'
                        sh './bob/bob -r rulesets/ruleset2.0-eric-dscload.yaml init:dscload-version'
                        sh './bob/bob -r rulesets/ruleset2.0-eric-dscload.yaml init:set-artifactory-token'
                        sh './bob/bob -r rulesets/ruleset2.0-eric-k6.yaml init:influxdb-version'
                        sh './bob/bob -r rulesets/ruleset2.0-eric-k6.yaml init:k6-version'
                        sh './bob/bob -r rulesets/ruleset2.0-eric-sftp.yaml init:sftp-version'

                        echo 'Creating CHFsim'
                        sh """
                        ./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml build;
                        ./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml image:build-eric-chfsim;
                        ./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml package:copy-helm-chart-chfsim;
                        ./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml package:helm-chart-chfsim;
                        ./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml push:docker-image-chfsim;
                        ./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml push:helm-chart-chfsim;
                        ./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml image:build-eric-chfsim-redis;
                        ./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml package:helm-chart-redis;
                        ./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml push:docker-image-redis;
                        ./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml push:helm-chart-redis;
                        ./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml export:folders;
                        ./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml export:helm;
                        ./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml export:image;"""

                        echo 'Creating SEPPsim'
                        sh """
                        ./bob/bob -r rulesets/ruleset2.0-eric-seppsim.yaml build;
                        ./bob/bob -r rulesets/ruleset2.0-eric-seppsim.yaml image;
                        ./bob/bob -r rulesets/ruleset2.0-eric-seppsim.yaml package-full;
                        ./bob/bob -r rulesets/ruleset2.0-eric-seppsim.yaml push:docker-image;
                        ./bob/bob -r rulesets/ruleset2.0-eric-seppsim.yaml push:helm-chart;
                        ./bob/bob -r rulesets/ruleset2.0-eric-seppsim.yaml export:folders;
                        ./bob/bob -r rulesets/ruleset2.0-eric-seppsim.yaml export:helm;
                        ./bob/bob -r rulesets/ruleset2.0-eric-seppsim.yaml export:image;"""

                        echo 'Creating NRFsim'
                        sh """
                        ./bob/bob -r rulesets/ruleset2.0-eric-nrfsim.yaml build;
                        ./bob/bob -r rulesets/ruleset2.0-eric-nrfsim.yaml image;
                        ./bob/bob -r rulesets/ruleset2.0-eric-nrfsim.yaml package;
                        ./bob/bob -r rulesets/ruleset2.0-eric-nrfsim.yaml push:docker-image;
                        ./bob/bob -r rulesets/ruleset2.0-eric-nrfsim.yaml push:helm-chart;
                        ./bob/bob -r rulesets/ruleset2.0-eric-nrfsim.yaml export:folders;
                        ./bob/bob -r rulesets/ruleset2.0-eric-nrfsim.yaml export:helm;
                        ./bob/bob -r rulesets/ruleset2.0-eric-nrfsim.yaml export:image;"""

                        echo 'Creating DSCLOAD'
                        sh """
                        ./bob/bob -r rulesets/ruleset2.0-eric-dscload.yaml image:build-dscload;
                        ./bob/bob -r rulesets/ruleset2.0-eric-dscload.yaml package:helm-chart-dscload;
                        ./bob/bob -r rulesets/ruleset2.0-eric-dscload.yaml push:docker-image-dscload;
                        ./bob/bob -r rulesets/ruleset2.0-eric-dscload.yaml push:helm-chart-dscload;
                        ./bob/bob -r rulesets/ruleset2.0-eric-dscload.yaml export:folders;
                        ./bob/bob -r rulesets/ruleset2.0-eric-dscload.yaml export:helm;
                        ./bob/bob -r rulesets/ruleset2.0-eric-dscload.yaml export:image;"""

                        echo 'Creating K6 and INFLUXDB'
                        sh """
                        ./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml image:build-k6;
                        ./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml package:helm-chart-k6;
                        ./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml push:docker-image-k6;
                        ./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml push:helm-chart-k6;
                        ./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml image:build-influxdb;
                        ./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml package:helm-chart-influxdb;
                        ./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml push:docker-image-influxdb;
                        ./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml push:helm-chart-influxdb;
                        ./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml export:folders;
                        ./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml export:helm;
                        ./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml export:image;"""

                        echo 'Creating SFTP'
                        sh """
                        ./bob/bob -r rulesets/ruleset2.0-eric-sftp.yaml image:build-sftp;
                        ./bob/bob -r rulesets/ruleset2.0-eric-sftp.yaml package:helm-chart-sftp;
                        ./bob/bob -r rulesets/ruleset2.0-eric-sftp.yaml push:docker-image-sftp;
                        ./bob/bob -r rulesets/ruleset2.0-eric-sftp.yaml push:helm-chart-sftp;
                        ./bob/bob -r rulesets/ruleset2.0-eric-sftp.yaml export:folders;
                        ./bob/bob -r rulesets/ruleset2.0-eric-sftp.yaml export:helm;
                        ./bob/bob -r rulesets/ruleset2.0-eric-sftp.yaml export:image;"""
//                    } //catch(err)
                }
            }
        }
        stage('Parallel Stages')
        {
            failFast true
            parallel
            {
                stage('Package creation and upload')
                {
                    steps
                    {
                        dir("$EXECUTING_DIR")
                        {
                            echo 'Creating cncs csar package'
                            sh './bob/bob -r csar/ruleset2.0.yaml create-csar'
                            echo 'Create additional files, remove older packages und upload the new one'
                            sh './bob/bob -r csar/ruleset2.0.yaml push-csar'
                        }
                    }
                }
                stage('Monitor disk space')
                {
                    steps
                    {
                        script
                        {
                            def disk_space_full = false
                            echo "Wait 3 mins to start the scanning for the disk space usage."
                            sleep (180)

                            while ( !disk_space_full )
                            {
                                CSAR_PACKAGE_NAME = """${sh( returnStdout: true, script: "ls ./${RELATIVE_DIR}/ | grep '^.*.unsigned.csar' 2>/dev/null || true")}""".trim()
                                if ("${CSAR_PACKAGE_NAME}".contains(".csar"))
                                {
                                    echo "The csar pkg (${CSAR_PACKAGE_NAME}) has been created. No need for further scanning on disk space usage."
                                    disk_space_full = true
                                }
                                else
                                {
                                    USED_DISK_SPACE = """${sh( returnStdout: true,
                                    script: "df -h | grep -e /home/jenkins/workspace -e /local |awk '{print \$5}' | cut -d 'G' -f1 | cut -d '%' -f1")}""".trim()

                                    if ("${USED_DISK_SPACE}".toInteger() > 97) // stop the process if the workspace is occupied more than 97%
                                    {
                                        echo "The disk space used on $NODE_NAME is ${USED_DISK_SPACE}%. The process for creating a new csar package will stop to avoid 100% disk usage."
                                        if ("${env.MAIL}" != 'null')
                                        {
                                            def emailbody_job_disk_full_1="\nthe disk space used on $NODE_NAME is ${USED_DISK_SPACE}%. The process "
                                            def emailbody_job_disk_full_2="for creating a new cncs csar package will stop to avoid 100% disk usage."

                                            emailext body: "${emailbody_start}"+"${emailbody_job_disk_full_1}"+"${emailbody_job_disk_full_2}"+
                                            "${emailbody_end}", subject: "Automatic cncs csar package creation stopped for branch ${BRANCH}", to: "${env.MAIL}"
                                        }
                                        sh """exit 1"""
                                    }
                                    else
                                    {
                                        echo " Check disk space usage in order to avoid reaching 100% and provoke outage of the build slave: currently it is at ${USED_DISK_SPACE}%. New scan in 30 sec "
                                        sleep (30)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        stage('Upload config files to artifactory')
        {
            steps
            {
                script
                {
                    functionConfig()
                }
            }
        }
        stage('Upload base stability traffic files to artifactory')
        {
            steps
            {
                dir("$EXECUTING_DIR")
                {
                    script
                    {
                        ARTIFACTORY_PATH = "https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/cncs/eiffelesc/${BRANCH}/${CSAR_PACKAGE_VERSION}"
                    }
                    withCredentials([string(credentialsId: 'armseligic-armdocker-eiffelesc-identity-token-for-Smoke-CI', variable: 'ARTIFACTORY_TOKEN')])
                    {
                        sh """perl ./daft/perl/bin/artifactory_base_stability_traffic_files.pl --token="$ARTIFACTORY_TOKEN" --full-artifactory-path=$ARTIFACTORY_PATH"""
                        sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/var.chfsim-version ${ARTIFACTORY_PATH}/base_stability_traffic/traffic_simulators/versions/"""
                        sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/var.redis-version ${ARTIFACTORY_PATH}/base_stability_traffic/traffic_simulators/versions/"""
                        sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/export-chfsim/docker/eric-chfsim-1*.tar ${ARTIFACTORY_PATH}/base_stability_traffic/traffic_simulators/docker/"""
                        sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/export-chfsim/docker/eric-chfsim-redis-*.tar ${ARTIFACTORY_PATH}/base_stability_traffic/traffic_simulators/docker/"""
                        sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/export-chfsim/helm/eric-chfsim-1*.tgz ${ARTIFACTORY_PATH}/base_stability_traffic/traffic_simulators/helm/"""
                        sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/export-chfsim/helm/eric-chfsim-redis-*.tgz ${ARTIFACTORY_PATH}/base_stability_traffic/traffic_simulators/helm/"""
                        sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/var.seppsim-version ${ARTIFACTORY_PATH}/base_stability_traffic/traffic_simulators/versions/"""
                        sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/export-seppsim/docker/eric-seppsim-*.tar ${ARTIFACTORY_PATH}/base_stability_traffic/traffic_simulators/docker/"""
                        sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/export-seppsim/helm/eric-seppsim-*.tgz ${ARTIFACTORY_PATH}/base_stability_traffic/traffic_simulators/helm/"""
                        sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/var.nrfsim-version ${ARTIFACTORY_PATH}/base_stability_traffic/traffic_simulators/versions/"""
                        sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/export-nrfsim/docker/eric-nrfsim-*.tar ${ARTIFACTORY_PATH}/base_stability_traffic/traffic_simulators/docker/"""
                        sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/export-nrfsim/helm/eric-nrfsim-*.tgz ${ARTIFACTORY_PATH}/base_stability_traffic/traffic_simulators/helm/"""
                        sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/var.dscload-version ${ARTIFACTORY_PATH}/base_stability_traffic/traffic_simulators/versions/"""
                        sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/export-dscload/docker/eric-dscload-*.tar ${ARTIFACTORY_PATH}/base_stability_traffic/traffic_simulators/docker/"""
                        sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/export-dscload/helm/eric-dscload-*.tgz ${ARTIFACTORY_PATH}/base_stability_traffic/traffic_simulators/helm/"""
                        sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/var.k6-version ${ARTIFACTORY_PATH}/base_stability_traffic/traffic_simulators/versions/"""
                        sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/export-k6/docker/eric-k6-*.tar ${ARTIFACTORY_PATH}/base_stability_traffic/traffic_simulators/docker/"""
                        sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/export-k6/helm/eric-k6-*.tgz ${ARTIFACTORY_PATH}/base_stability_traffic/traffic_simulators/helm/"""
                        sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/var.influxdb-version ${ARTIFACTORY_PATH}/base_stability_traffic/traffic_simulators/versions/"""
                        sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/export-k6/docker/eric-influxdb-*.tar ${ARTIFACTORY_PATH}/base_stability_traffic/traffic_simulators/docker/"""
                        sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/export-k6/helm/eric-influxdb-*.tgz ${ARTIFACTORY_PATH}/base_stability_traffic/traffic_simulators/helm/"""
                        sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/var.sftp-version ${ARTIFACTORY_PATH}/base_stability_traffic/traffic_simulators/versions/"""
                        sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/export-sftp/docker/eric-atmoz-sftp-*.tar ${ARTIFACTORY_PATH}/base_stability_traffic/traffic_simulators/docker/"""
                        sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/export-sftp/helm/eric-atmoz-sftp-*.tgz ${ARTIFACTORY_PATH}/base_stability_traffic/traffic_simulators/helm/"""
                    }
                }
            }
        }
        stage('Pull supreme docker image and upload file to artifactory')
        {
            steps
            {
                script
                {
                    functionSupreme()
                }
            }
        }
        stage('Upload default certificate files to artifactory')
        {
            steps
            {
                script
                {
                    functionCertificates()
                }
            }
        }
        stage('Run cbos-age-tool and upload files to artifactory')
        {
            steps
            {
                script
                {
                    functionCbos()
                }
            }
        }
        stage('Archiving & Mail notification csar pkg creation')
        {
            steps
            {
                dir("$EXECUTING_DIR")
                {
                    script
                     {
                        DISPLAY_COMMIT = """${sh( returnStdout: true, script: 'git log -n1 --abbrev-commit --format="%h"')}"""
                        DISPLAY_COMMITTER = """${sh( returnStdout: true, script: 'git log --format="%cN" | head -1')}""".trim()
                     }
                }
                sh """echo "APPLICATION=SC-CXP_904_4189_1"                   > ./artifact.properties"""
                sh """echo "PACKAGE_VERSION=${CSAR_PACKAGE_VERSION}"        >> ./artifact.properties"""
                sh """echo "BRANCH=${FILE_BRANCH_INFO}"                     >> ./artifact.properties"""
                sh """echo "COMMIT=${COMMIT}"                               >> ./artifact.properties"""
                sh """echo "SPIDER_CHART_TAG=${env.SPIDER_CHART_TAG}"       >> ./artifact.properties"""
                sh """echo "MAIL=${env.MAIL}"                               >> ./artifact.properties"""
                sh """echo "SIGNATURE=${SIGNATURE}"                         >> ./artifact.properties"""
                sh """echo "\nThis package has been created from the spider $ADD_INFO and contains the following data:\n" >> ./artifact.properties"""
                sh "cat ./${RELATIVE_DIR}/eric-sc-spider_tmp/eric-sc-spider/Chart.yaml >> ./artifact.properties"

                sh """  sed -i 's/=null/=/' ./artifact.properties"""

                archiveArtifacts allowEmptyArchive: false, artifacts: 'artifact.properties', onlyIfSuccessful: false

                sh """echo "This package has been created from the spider $ADD_INFO and contains the following data:\n" > ./info_${CSAR_PACKAGE_VERSION}.txt"""
                sh "cat ./${RELATIVE_DIR}/eric-sc-spider_tmp/eric-sc-spider/Chart.yaml >> ./info_${CSAR_PACKAGE_VERSION}.txt"

                withCredentials([string(credentialsId: 'armseligic-armdocker-eiffelesc-identity-token-for-Smoke-CI', variable: 'ARTIFACTORY_TOKEN')])
                {
                    sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./info_${CSAR_PACKAGE_VERSION}.txt https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/cncs/eiffelesc/${BRANCH}/${CSAR_PACKAGE_VERSION}/"""
                }
                script
                {
                    if ("${env.MAIL}" != 'null')
                    {
                        def emailbody_csar_created_1="\na new csar package called ${CSAR_PACKAGE_NAME} has been created $MAIL_INFO"
                        def emailbody_csar_created_2="based on dev commit: ${DISPLAY_COMMIT} by user: ${DISPLAY_COMMITTER}."
                        def emailbody_csar_created_3="\nIt is located under the folder ${CSAR_PACKAGE_VERSION} on ${env.REPO_PATH}/${BRANCH}/"
                        def emailbody_csar_created_4="\n\nThis package has been created from the spider $ADD_INFO_MAIL and contains the following data:\n"
                        def CHART_INFO_MAIL = """${sh(returnStdout: true, script: "cat ./${RELATIVE_DIR}/eric-sc-spider_tmp/eric-sc-spider/Chart.yaml")}""".trim()

                        emailext body: "${emailbody_start}"+"${emailbody_csar_created_1}"+"${emailbody_csar_created_2}"+"${emailbody_csar_created_3}"+"${emailbody_csar_created_4}"+"${CHART_INFO_MAIL}"+"${emailbody_end}",
                        subject: "New csar package automatically created ${SUBJECT_INFO}", to: "${env.MAIL}"
                    }
                }
            }
        }
    }
    post
    {
        always
        {
            echo 'Removing the newly created images to free space in the build slave registry'
            script
                {
                    IMAGES_VERSION = """${sh( returnStdout: true, script: "cat ./${RELATIVE_DIR}/var.esc-version | sed s/+/-/")}""".trim()
                }
            sh """docker images | grep ${IMAGES_VERSION} | grep -e sim -e k6 -e dscload -e influxdb -e atmoz | awk '{print \$1 ":" \$2}' | xargs docker rmi 2>/dev/null || true"""

            echo 'Removing the csar pkg to free space in the build slave'
            sh "sudo bash -c 'rm -rf ./${RELATIVE_DIR}/*.csar'"

            echo 'Removing any tmp folder which might have root rights and prevents upcoming packages creation on the same build slave workspace'
            sh "sudo bash -c 'rm -rf ./${RELATIVE_DIR}/tmp*'"
        }
        success
        {
            cleanWs()
        }
        aborted
        {
            script
            {
                if ("${env.MAIL}" != 'null')
                {
                    def emailbody_job_aborted_1="\nthe job creating a new csar package aborted due to a timeout."
                    def emailbody_job_aborted_2="\n\nPlease check the log under: ${LOGS_LINK}/$BUILD_NUMBER/consoleFull"

                    emailext body: "${emailbody_start}"+"${emailbody_job_aborted_1}"+"${emailbody_job_aborted_2}"+
                    "${emailbody_end}", subject: "Automatic cncs csar package creation aborted ${SUBJECT_INFO}", to: "${env.MAIL}"
                }
            }
        }
        failure
        {
            script
            {
                if ("${env.MAIL}" != 'null')
                {
                    def emailbody_job_failure_1="\nthe job creating a new csar package has failed."
                    def emailbody_job_failure_2="\n\nPlease check the log under:\n${LOGS_LINK}/$BUILD_NUMBER/consoleFull"

                    emailext body: "${emailbody_start}"+"${emailbody_job_failure_1}"+"${emailbody_job_failure_2}"+"${emailbody_end}",
                    subject: "Automatic csar package creation failed ${SUBJECT_INFO}${ADD_MAIL_MSG}", to: "${env.MAIL}"
                }
            }
        }
    }
}
def functionConfig()
{
    script
    {
        ARTIFACTORY_PATH = "https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/cncs/eiffelesc/${BRANCH}/${CSAR_PACKAGE_VERSION}/sc-config-sample/"
        BSF_INIT = ARTIFACTORY_PATH + "action_bsf_init_db,user=bsf-admin.netconf"
        BSF_CONFIG = ARTIFACTORY_PATH + "sample_bsf_func_and_diameter,user=bsf-admin.netconf"
        CSA_CONFIG = ARTIFACTORY_PATH + "sample_config_nrf_csa,user=csa-admin.netconf"
        PVTB_CONFIG = ARTIFACTORY_PATH + "sample_broker_config,user=expert.netconf"
        SCP_CONFIG = ARTIFACTORY_PATH + "sample_config_scp,user=scp-admin.netconf"
        SEPP_CONFIG = ARTIFACTORY_PATH + "sample_sepp_poc_config,user=sepp-admin.netconf"

        if ("${env.SPIDER_CHART_TAG}" != 'null')
        {
            CONFIGURATION_PROVISIONING_DIR = "./5g_proto_spider_commit/Jenkins/PipeConfig"
        }
        else
        {
            CONFIGURATION_PROVISIONING_DIR = "./Jenkins/PipeConfig"
        }

        withCredentials([string(credentialsId: 'armseligic-armdocker-eiffelesc-identity-token-for-Smoke-CI', variable: 'ARTIFACTORY_TOKEN')])
        {
            sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" \\
            --upload-file ${CONFIGURATION_PROVISIONING_DIR}/action_bsf_init_db.netconf ${BSF_INIT}"""
            sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" \\
            --upload-file ${CONFIGURATION_PROVISIONING_DIR}/sample_bsf_func_and_diameter.netconf ${BSF_CONFIG}"""
            sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" \\
            --upload-file ${CONFIGURATION_PROVISIONING_DIR}/sample_config_nrf_csa.netconf ${CSA_CONFIG}"""
            sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" \\
            --upload-file ${CONFIGURATION_PROVISIONING_DIR}/sample_broker_config.netconf ${PVTB_CONFIG}"""
            sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" \\
            --upload-file ${CONFIGURATION_PROVISIONING_DIR}/sample_config_scp.netconf ${SCP_CONFIG}"""
            sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" \\
            --upload-file ${CONFIGURATION_PROVISIONING_DIR}/sample_sepp_poc_config.netconf ${SEPP_CONFIG}"""
        }
    }
}
def functionSupreme()
{
    script
    {
        env.SUPREME_PULL_RESULT = "SUCCESS"
        env.SUPREME_SAVE_RESULT = "SUCCESS"
        env.SUPREME_UPLOAD_RESULT = "SUCCESS"
        env.OUTPUT_DIR_ABSPATH = """${sh( returnStdout: true, script: "realpath ${RELATIVE_DIR}" )}""".trim();
        echo "OUTPUT_DIR_ABSPATH=${OUTPUT_DIR_ABSPATH}"
        try
        {
            sh """docker image pull armdocker.rnd.ericsson.se/proj-5g-bsf/supreme/supreme:${WANTED_SUPREME_VERSION}"""
        }
        catch(err)
        {
            env.SUPREME_PULL_RESULT = "FAILURE"
            env.SUPREME_SAVE_RESULT = "FAILURE"
            env.SUPREME_UPLOAD_RESULT = "FAILURE"
            env.MESSAGE = "Failed to pull supreme docker image"
        }
        if ("${SUPREME_PULL_RESULT}" == "SUCCESS")
        {
            try
            {
                sh """docker image save armdocker.rnd.ericsson.se/proj-5g-bsf/supreme/supreme:${WANTED_SUPREME_VERSION} -o ${OUTPUT_DIR_ABSPATH}/eric-supreme-${WANTED_SUPREME_VERSION}.tar"""
            }
            catch(err)
            {
                env.SUPREME_SAVE_RESULT = "FAILURE"
                env.SUPREME_UPLOAD_RESULT = "FAILURE"
                env.MESSAGE = "Failed to save supreme docker image"
            }

            if ("${SUPREME_SAVE_RESULT}" == "SUCCESS")
            {
                ARTIFACTORY_PATH = "https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/cncs/eiffelesc/${BRANCH}/${CSAR_PACKAGE_VERSION}/tools/docker/"
                try
                {
                    withCredentials([string(credentialsId: 'armseligic-armdocker-eiffelesc-identity-token-for-Smoke-CI', variable: 'ARTIFACTORY_TOKEN')])
                    {
                        sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ${OUTPUT_DIR_ABSPATH}/eric-supreme-${WANTED_SUPREME_VERSION}.tar ${ARTIFACTORY_PATH}"""
                    }
                }
                catch(err)
                {
                    env.SUPREME_UPLOAD_RESULT = "FAILURE"
                    env.MESSAGE = "Failed to upload the supreme docker file to artifactory"
                }
            }
        }
        echo "SUPREME_PULL_RESULT=${SUPREME_PULL_RESULT}"
        echo "SUPREME_SAVE_RESULT=${SUPREME_SAVE_RESULT}"
        echo "SUPREME_UPLOAD_RESULT=${SUPREME_UPLOAD_RESULT}"
        if ("${SUPREME_UPLOAD_RESULT}" == "FAILURE")
        {
//            catchError(buildResult: 'FAILURE', stageResult: 'FAILURE')
//            {
                echo "${MESSAGE}"
                sh "exit 1"
//            }
        }
    }
}
def functionCertificates()
{
    script
    {
        env.CERTIFICATE_CHECK_RESULT = "SUCCESS"
        ARTIFACTORY_PATH = "https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/cncs/eiffelesc/"
        try
        {
            withCredentials([string(credentialsId: 'armseligic-armdocker-eiffelesc-identity-token-for-Smoke-CI', variable: 'ARTIFACTORY_TOKEN')])
            {
                sh """/proj/DSC/rebels/bin/artifactory_certificate_files.pl --token=$ARTIFACTORY_TOKEN --url-base-development=$ARTIFACTORY_PATH --directory-path=/proj/DSC/rebels/certificates --release=$BRANCH --build=$CSAR_PACKAGE_VERSION"""
            }
        }
        catch(err)
        {
            env.CERTIFICATE_CHECK_RESULT = "FAILURE"
            env.MESSAGE = "Failed to upload the certificates files to artifactory"
        }
        echo "CERTIFICATE_CHECK_RESULT=${CERTIFICATE_CHECK_RESULT}"
        if ("${CERTIFICATE_CHECK_RESULT}" == "FAILURE")
        {
//            catchError(buildResult: 'FAILURE', stageResult: 'FAILURE')
//            {
                echo "${MESSAGE}"
                sh "exit 1"
//            }
        }
    }
}
def functionCbos()
{
    script
    {
        env.CBOS_CHECK_RESULT = "SUCCESS"
        env.OUTPUT_DIR_ABSPATH = """${sh( returnStdout: true, script: "realpath ${RELATIVE_DIR}" )}""".trim();
        echo "OUTPUT_DIR_ABSPATH=${OUTPUT_DIR_ABSPATH}"
        try
        {
            sh """docker run --rm --volume /home/eiffelesc:/home/eiffelesc --volume ${OUTPUT_DIR_ABSPATH}:${OUTPUT_DIR_ABSPATH} --workdir ${OUTPUT_DIR_ABSPATH} armdocker.rnd.ericsson.se/proj-adp-cicd-drop/common-library-adp-helm-dr-check:latest cbos-age-tool -a=${OUTPUT_DIR_ABSPATH}/${CSAR_PACKAGE_NAME} -o=${OUTPUT_DIR_ABSPATH} -C -s="Age check of ${CSAR_PACKAGE_NAME}" -Dhelmdrck.credential.file.path=/home/eiffelesc/.artifactory/helm_repositories.yaml"""
        }
        catch(err)
        {
            env.CBOS_CHECK_RESULT = "FAILURE"
            env.MESSAGE = "Failed to execute cbos-age-tool"
        }
        if ("${CBOS_CHECK_RESULT}" == "SUCCESS")
        {
            ARTIFACTORY_PATH = "https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/cncs/eiffelesc/${BRANCH}/${CSAR_PACKAGE_VERSION}/cbos-age-reports/"
            try
            {
                withCredentials([string(credentialsId: 'armseligic-armdocker-eiffelesc-identity-token-for-Smoke-CI', variable: 'ARTIFACTORY_TOKEN')])
                {
                    sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file "{\$(echo ${OUTPUT_DIR_ABSPATH}/cbos-age-report-* | tr ' ' ',')}" ${ARTIFACTORY_PATH}"""
                    // The files are called something like this:
                    // cbos-age-report-eric-sc-umbrella-1.7.25+627-202201171545.html
                    // cbos-age-report-eric-sc-umbrella-1.7.25+627-202201171545.json
                    // cbos-age-report-eric-sec-certm-crd-3.11.0+81-202201171545.html
                    // cbos-age-report-eric-sec-certm-crd-3.11.0+81-202201171545.json
                    // cbos-age-report-eric-sec-sip-tls-crd-2.10.0+40-202201171545.html
                    // cbos-age-report-eric-sec-sip-tls-crd-2.10.0+40-202201171545.json
                    // cbos-age-report-eric-tm-ingress-controller-cr-crd-6.1.0+55-202201171544.html
                    // cbos-age-report-eric-tm-ingress-controller-cr-crd-6.1.0+55-202201171544.json
                }
            }
            catch(err)
            {
                env.CBOS_CHECK_RESULT = "FAILURE"
                env.MESSAGE = "Failed to upload the report files to artifactory"
            }
        }
        echo "CBOS_CHECK_RESULT=${CBOS_CHECK_RESULT}"
        if ("${CBOS_CHECK_RESULT}" == "FAILURE")
        {
//            catchError(buildResult: 'FAILURE', stageResult: 'FAILURE')
//            {
                echo "${MESSAGE}"
                sh "exit 1"
//            }
        }
    }
}

def check_helm_chart(chart_name,chart_variable,spider_version)
{
    script
    {
        chart_version = """${sh( returnStdout: true, script: "cat ./${RELATIVE_DIR}/${chart_variable}  | sed 's/${chart_name}-//' | sed 's/.tgz//' ")}""".trim()
        if ("${chart_version}" == "${spider_version}")
        {
            echo "Chart version for $chart_name matches Spider version: ${spider_version}"
        }
        else
        {
            error("Chart version for $chart_name (${chart_version}) does not match its Spider version (${spider_version}). This is not expected; process will stop now.")
        }
    }
}
def getSpiderCommit()
{
    script
    {
        // Find the commit whose tag contains the value for SPIDER_CHART_TAG
        SPIDER_CHART_TAG_COMMIT = """${sh( returnStdout: true, script: "git rev-list ${SPIDER_CHART_TAG} -1 --abbrev-commit")}""".trim()

        checkout scmGit(
                branches: [[name: "refs/tags/$SPIDER_CHART_TAG"]],
                userRemoteConfigs: [[credentialsId: 'eiffelesc-user-password', name: 'origin', url: 'https://eiffelesc@gerritmirror-direct.sero.gic.ericsson.se/a/MC_5G/5g_proto']],
                extensions: [submodule(parentCredentials: true, recursiveSubmodules: true, reference: '', shallow: false), [$class: 'RelativeTargetDirectory', relativeTargetDir: '5g_proto_spider_commit']])

        // If "NIGHTLY-CNCS-CI" was given as value for "SPIDER_CHART_TAG" we need to get the value of the other tag, which is the corresponding spider chart version; tHe same applies for "RELEASE-CNCS-CI"
        // otherwise the value of "SPIDER_CHART_TAG" contents already the spider chart version


        if ("${SPIDER_CHART_TAG}".contains("-CNCS-CI"))
        {
/*
            switch (SPIDER_CHART_TAG)
            {
                case [ 'NIGHTLY-CNCS-CI' ]:
                    MASK = "NIGHTLY"
                    break
                case [ 'RELEASE-CNCS-CI' ]:
                    MASK = "NIGHTLY" //To be filled
                    break
                default: // The rest is Team-CI build: branch cannot be master neither "RELEASE_BRANCH" type
                    error("There is no logic for this type of label \"${SPIDER_CHART_TAG}\". Please check.")
                    break
            } //switch

 */
            echo "Looking for a CHART_VERSION tag at commit ${SPIDER_CHART_TAG_COMMIT}"
//            SPIDER_CHART_TAG_VERSION = """${sh( returnStdout: true, script: "git tag --sort=-taggerdate --points-at $SPIDER_CHART_TAG_COMMIT | sed /${SPIDER_CHART_TAG}/d | sed -n 1p")}""".trim()
            SPIDER_CHART_TAG_VERSION = """${sh( returnStdout: true, script: "git tag --sort=-taggerdate --points-at ${SPIDER_CHART_TAG_COMMIT} | sed /\$(head -n 1 VERSION_PREFIX)/d | sed /$SPIDER_CHART_TAG/d | sed -n 1p")}""".trim()
            echo "Found CHART_VERSION ${SPIDER_CHART_TAG_VERSION}"
            // if we know exactly the way it is formed the mapping might be more accurate:
            // eg (1.1.0-3-hd6769f6)                                         1. xx    .xx     - 1234  - 123abc
            //SPIDER_CHART_TAG_VERSION = """${sh( returnStdout: true, script: "git tag --points-at $SPIDER_CHART_TAG_COMMIT | grep -P -o \"\\d.\\d{1,2}.\\d{1,2}-\\d{1,4}-[0-9a-f]{6,8}\"")}""".trim()
        }
        else
        {
            SPIDER_CHART_TAG_VERSION = SPIDER_CHART_TAG
        }
        println("The chart eric-sc-spider-${SPIDER_CHART_TAG_VERSION}.tgz will be downloaded from artifactory")
        ADD_INFO ="chart eric-sc-spider-${SPIDER_CHART_TAG_VERSION}.tgz downloaded from the artifactory \\\"$HELM_REPO_URL\\\""
        ADD_INFO_MAIL ="chart eric-sc-spider-${SPIDER_CHART_TAG_VERSION}.tgz downloaded from the artifactory \"$HELM_REPO_URL\""
    }
}
def checkParallelCreation(THIS_JOB)
{
    script
    {
        withCredentials([usernameColonPassword(credentialsId: '7b8a9097-73ab-4e79-8b06-b8dcbbf65c3a', variable: 'API_TOKEN')])
        {
            PARALLEL_CREATION = sh(script: """curl -u $API_TOKEN  https://fem1s10-eiffel029.eiffel.gic.ericsson.se:8443/jenkins/job/5G-ESC/job/CSAR/job/$THIS_JOB/lastBuild/api/json | jq -r . | grep inProgress """, returnStdout: true).trim()
        }
        if ("${PARALLEL_CREATION}".contains("true"))
        {
            if ("${env.MAIL}" != 'null')
            {
                def emailbody_job_failure_1="\nthere is another csar package creation ongoing by the job \"$THIS_JOB\" (https://fem1s10-eiffel029.eiffel.gic.ericsson.se:8443/jenkins/job/5G-ESC/job/CSAR/job/$THIS_JOB/lastBuild/consoleFull)"
                def emailbody_job_failure_2="\n\nPlease wait until that one has finished and retrigger it again"

                emailext body: "${emailbody_start}"+"${emailbody_job_failure_1}"+"${emailbody_job_failure_2}"+"${emailbody_end}",
                subject: "Automatic csar package creation failed as parallel creation is ongoing", to: "${env.MAIL}"
            }
            println("There is another csar package creation ongoing by the job \"$THIS_JOB\" (https://fem1s10-eiffel029.eiffel.gic.ericsson.se:8443/jenkins/job/5G-ESC/job/CSAR/job/$THIS_JOB/lastBuild/consoleFull)")
            println("This job will stop now. Please wait until that one has finished and retrigger this one again.")
            error("Job terminated as parallel csar creation might end up in overwriting of packages.")
        }
        else
        {
            println ("There is no other csar package creation ongoing by the job \"$THIS_JOB\"; process continues.")
        }
    }
}