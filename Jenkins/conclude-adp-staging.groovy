//////////////////////////////////////////////////////////////////////////
//
// first run on SERO_GIC buildslaves, decide which buildslave(s) to use

def AGENT_LABEL = null
def emailbody4="<br>Logs:<br>"+"${env.BUILD_URL}"+"<br><br><br><br>Thank you for pushing.<br><br>BR,<br>SC ADP Staging"

node('DSC_BuildSlave_GIC_1||DSC_BuildSlave_GIC_2||DSC_BuildSlave_GIC_3||DSC_BuildSlave_GIC_4||DSC_BuildSlave_GIC_5') {

  stage('set agent'){

     if (env.IP_VERSION == '6') {   // move to IPv6 buildslave(s)
        AGENT_LABEL = '5G-SC-IPv6'
     } else                         // move to IPv4 buildslave(s)
     {
         AGENT_LABEL = '5G-SC'      // in the future
//         AGENT_LABEL = 'esc-docker||esc-docker-1||esc-docker-2'
     } //if

   } //stage

} //node

//
//////////////////////////////////////////////////////////////////////////
def TROUBLESHOOTING_FAILURE_REASON=[]

// set the displayed build name to BUILD_NUMBER | COMMIT
currentBuild.displayName = "#${BUILD_NUMBER} - ${env.COMMIT}"

pipeline {
    
    agent { label "${AGENT_LABEL}"   } // set label according to IP_VERSION parameter
    
    environment {
        // Variables needed for the ruleset
        BSF = true
        BSF_DIAMETER = true
        NLF = true
        SCP = true
        CSA = false
        SEPP = true
        BSF_TLS = false
        SCP_TLS = false
        LOGGING = true
        PRODUCTION = true
        RESOURCES = "full"

        // Pipe Variables Used in Jenkins
        DOCKER_RUN="docker run --rm  --user \$(id -u):\$(id -g) "
        BUILDER_WORK_DIR = " -w ${env.WORKSPACE} "
        BUILDER_DOCKER_IMAGE = "armdocker.rnd.ericsson.se/sandbox/adp-staging/adp-cicd/bob-py3kubehelmbuilder:fd49f94"
        BUILDER_DOCKER_VOLUMES ="-v ${env.WORKSPACE}/helm-home:/home/jenkins/.helm -v ${env.WORKSPACE}:${env.WORKSPACE}"
        BUILDER_DOCKER_ENV = "--env KUBECONFIG=${env.WORKSPACE}/.bob/${env.KUBE_HOST}.admin.conf"
        HELM_ARGS="--home /home/jenkins/.helm"
        HELM_RELEASE_NAME="bsf-app-staging-trial"
        OUTPUT_DIR=".bob"
        STAGING_BRANCH="adp-lift-master"

        CHART_NAME="${env.CHART_NAME}"
        CHART_REPO="${env.CHART_REPO}"
        CHART_VERSION="${env.CHART_VERSION}"
        KUBE_HOST ="${KUBE_HOST}"
        ARTIFACTORY_TOKEN = "${ARTIFACTORY_TOKEN}"
        NIGHTLY = "${env.NIGHTLY}"
        BRANCH = "${env.BRANCH}"
        TROUBLESHOOTING = "${env.TROUBLESHOOTING}"
        // TEST_BRANCH = "${env.TEST_BRANCH}"
        // TEST_COMMIT = "${env.TEST_COMMIT}"
    }

    stages{
        stage('Freeing up workspace on other buildslaves'){
            steps{

            sh ''' /bin/bash -x
                    IP_ADDRESS_TMP=`hostname -i`;
                    BUILD_PATH_TMP=`pwd`;
                    
                    if [ "${IP_ADDRESS_TMP}" != "10.128.96.132" ];
                    then
                        for ip in `seq -f "10.210.174.%g" 60 62`;
                        do
                            if [ "${IP_ADDRESS_TMP}" != "${ip}" ];
                            then
                                ssh eiffelesc@${ip} "if [ -d ${BUILD_PATH_TMP} ]; then sudo rm -rf ${BUILD_PATH_TMP}/; else echo \"Nothing to clean\"; fi;"
                            fi;
                        done;
                    fi;'''
            }
        }
        stage('checkout the correct commit') {
             steps {
                script {
                    echo "TEST_TYPE: ${env.TEST_TYPE}"
                    echo "COMMIT: ${env.COMMIT}"
                    echo "BRANCH: ${env.BRANCH}"
                    
                    echo "Checking out ${env.COMMIT}"
                    checkout scmGit(
                        branches: [[name: env.COMMIT]],
                        extensions: [submodule(parentCredentials: true, recursiveSubmodules: true, reference: '', shallow: false), [$class: 'WipeWorkspace']], 
                        userRemoteConfigs: [[credentialsId: 'eiffelesc-user-password', name: 'origin', url: 'https://eiffelesc@gerrit.ericsson.se/a/MC_5G/5g_proto']]
                    ) // checkout
                    // set the displayed build name to "BUILD_NUMBER - COMMIT"
                    currentBuild.displayName = "#${BUILD_NUMBER} - ${env.COMMIT}"
                } // script
            }  // steps 
        }//stage
        stage('Initialize') {
            steps {

                  echo "CHART NAME: ${env.CHART_NAME}"
                  echo "CHART REPO: ${env.CHART_REPO}"
                  echo "CHART VERSION: ${env.CHART_VERSION}"
                  echo "ARTIFACTORY TOKEN: ${env.ARTIFACTORY_TOKEN}"
                  echo "KUBE HOST: ${env.KUBE_HOST}"
                  echo "NAMESPACE: ${env.NAMESPACE}"  

                script {
                    PYTHON_CMD = "${DOCKER_RUN} ${BUILDER_DOCKER_VOLUMES} -w ${env.WORKSPACE} ${BUILDER_DOCKER_ENV} ${BUILDER_DOCKER_IMAGE} python "
                    HELM_CMD = "${DOCKER_RUN} ${BUILDER_DOCKER_VOLUMES} ${BUILDER_DOCKER_ENV} ${BUILDER_DOCKER_IMAGE} helm"
                 } //script
            } //steps
        } //stage
       stage('Init bob'){
            steps{
                echo pwd()
                sh 'ls -ltrh'
                sh 'git submodule update --init --recursive'
                sh './bob/bob init:set-build-proxy'
                sh './bob/bob init:set-kube-config'
                sh './bob/bob init:set-namespace'
                sh './bob/bob init:set-ingressHost'
                sh "tac esc/helm/eric-sc-umbrella/requirements.yaml | grep -m1 \'version\' | cut -d \' \' -f4 > ./.bob/var.esc-version"
                sh './bob/bob init:cxp-number'
                sh './bob/bob init:cxp-rev-number'
                sh './bob/bob init:product-info'
            } // steps 
        } //stage
        stage('Update baseline'){
            when {
                allOf {
                    environment name: 'TROUBLESHOOTING', value: 'FALSE';   // not to be done in case of a development pipe 
                    environment name: 'MERGE_MASTER', value: 'true'; // when running on master for merging purposes
                }
            }
            steps {    
                sh "./bob/bob update-umbrella:copy"              
                sh """if ( test '${env.CHART_NAME}' = 'eric-sc' );
                    then
                        ./bob/bob update-umbrella:update-product-numbers;
                        ./bob/bob update-umbrella:bsf;
                        ./bob/bob update-umbrella:bsf-diameter;
                        ./bob/bob update-umbrella:nlf;
                        ./bob/bob update-umbrella:scp;
                        ./bob/bob update-umbrella:sepp;
                        ./bob/bob update-umbrella:slf;
                        ./bob/bob update-umbrella:rlf;
                        ./bob/bob update-umbrella:monitor;
                    elif ( test '${env.CHART_NAME}' = 'eric-data-wide-column-database-cd' );
                    then
                        echo "Skip update of baseline, chart is eric-data-wide-column-database-cd";
                    elif ( test '${env.CHART_NAME}' = 'eric-stm-diameter' );
                    then
                        echo "Skip update of baseline, chart is eric-stm-diameter";
                    elif ( test '${env.CHART_NAME}' = 'eric-cnom-server' );
                    then
                        echo "Skip update of baseline, chart is eric-cnom-server";
                    else
                        ./bob/bob update-umbrella:update-product-numbers;
                        ./bob/bob update-umbrella-generic:service;
                    fi;"""
            }
        }

        stage('Undeploy') {
            steps {
                script {
                    try {
                        timeout(time: 20, unit: 'MINUTES') {
                            // clean cluster due to failure in pipeline
                            sh "./bob/bob clean-resources:clean-gracefully"
                            sh "./bob/bob undeploy:delete-secrets"
                            sh "./bob/bob undeploy:pm-httpproxy"
                            sh "./bob/bob undeploy:osmn-httpproxy"
                            sh "./bob/bob undeploy:envoy-admin-httpproxy"
                            sh "./bob/bob clean-resources:remove-namespace-resources"
                            sh "./bob/bob clean-resources:remove-cluster-resources"
                        } // timeout
                    } // try
                    catch (exc) {
                        TROUBLESHOOTING_FAILURE_REASON += ("Failed to clean cluster gracefully")

                    } // catch
                } // script
            } // steps
        } //stage
        stage('Undeploy CRDs') {
            when {
                anyOf {
                    environment name: 'UPGRADE_CRDS', value: 'true';
                    environment name: 'CLEAN_CRDS', value: 'true';
                }
            }
            steps {
                script {
                        if ("${CLEAN_CRDS}"== "true") {
                            // Warning: this target will leave cluster with no crds
                            // First, it will undeploy all CRDs releases
                            // then, it will delete all kept resources because of policy
                            sh "./bob/bob undeploy-crds:clean-crds" 
                        }
                        else {
                            if ("${UPGRADE_CRDS}"== "true") {
                                if ("${CHART_VERSION}".contains("-") || TROUBLESHOOTING=="TRUE") {
                                    try {
                                        timeout(time: 8, unit: 'MINUTES') {
                                            // Install the old charts' CRD if it exists
                                            // undeploy specific chart 
                                            sh "./bob/bob undeploy-crds-generic" 
                                            sh "./bob/bob deploy-crds-generic:package-deploy-crd-generic-old"
                                        } // timeout
                                    } // try
                                    catch (exc) {
                                        TROUBLESHOOTING_FAILURE_REASON += ("Failed to clean crds gracefully")
                                        sh "./bob/bob undeploy-crds"
                                        sh "./bob/bob fetch-crds"
                                        sh "./bob/bob deploy-crds;"
                                    } // catch
                                } // if
                            }
                        }
                        

                } // script
            } // steps
        } //stage
        stage('Commit and push chart') {
            when {
                allOf {
                    environment name: 'TROUBLESHOOTING', value: 'FALSE';   // not to be done in case of a development pipe 
                    environment name: 'MERGE_MASTER', value: 'true'; // when running on master for merging purposes
                }
            }
            steps {
                sh "git checkout ${env.BRANCH}"
                sh "\\cp -r ${OUTPUT_DIR}/eric-sc-umbrella/requirements.yaml esc/helm/eric-sc-umbrella"
                sh """  if  ( ! (git diff --quiet) ); then 
                            echo "There is a change in requirements.yaml";
                            if ( (echo "${env.CHART_VERSION}" | grep +) ); then
                                echo "New version is a PRA. It is pushed to ${COMMIT}!!!"
                                git log -n 10 --oneline --decorate --graph
                                git status
                                git add esc/helm/eric-sc-umbrella/requirements.yaml
                                git commit -m "Automatic new version in baseline ${env.CHART_NAME} ${env.CHART_VERSION}, ${env.COMMIT}"
                                git status
                                git pull origin ${env.BRANCH}
                                git push origin ${env.BRANCH}
                                git log -n 10 --oneline --decorate --graph
                            else
                                echo "This is a PREL version of the service ${env.CHART_NAME}. It will NOT be commited!!!"
                            fi 
                        else 
                            echo "Nothing to be commited." 
                        fi;"""
            } //steps
        } //stage
        stage('Notify teams for failure') {
            when {
                environment name: 'TROUBLESHOOTING', value: 'TRUE';
            }
            steps {
                script {
                    // TROUBLESHOOTING initially is true. If it changes to FALSE, it means that no troublshooting actions are needed
                    // CHART_NAME is WCDB -> inform IXG3
                    if ("${CHART_NAME}".contains("eric-data-wide-column-database-cd")) { 
                        emailext body: "Hello IXG3 team,<br><br> SC application deployment failed while testing ${CHART_NAME} ${CHART_REPO} ${CHART_VERSION}.<br>Please take appropriate actions!<br><br> .Folowing are the possible Troubleshooting reasons: ${TROUBLESHOOTING_FAILURE_REASON} <br><br>.Please check:<br>"+"${emailbody4}", 
                        mimeType: 'text/html',
                        subject: "WCDB deploy failed", 
                        to: "dsc_ixg3@ericsson.onmicrosoft.com"
                    }
                    // CHART_NAME is stm-diameter -> inform IXG3
                    else if ("${CHART_NAME}".contains("eric-stm-diameter")) { 
                        emailext body: "Hello IXG3 team,<br><br> SC application deployment failed while testing ${CHART_NAME} ${CHART_REPO} ${CHART_VERSION}.<br>Please take appropriate actions!<br><br> .Folowing are the possible Troubleshooting reasons: ${TROUBLESHOOTING_FAILURE_REASON} <br><br>.Please check:<br>"+"${emailbody4}", 
                        mimeType: 'text/html',
                        subject: "Stm diameter deploy failed", 
                        to: "dsc_ixg3@ericsson.onmicrosoft.com"
                    }
                    // CHART_NAME is cnom-server -> inform DJs
                    else if ("${CHART_NAME}".contains("eric-cnom-server")) { 
                        emailext body: "Hello DJs team,<br><br> SC application deployment failed while testing ${CHART_NAME} ${CHART_REPO} ${CHART_VERSION}.<br>Please take appropriate actions!<br><br> .Folowing are the possible Troubleshooting reasons: ${TROUBLESHOOTING_FAILURE_REASON} <br><br>.Please check:<br>"+"${emailbody4}", 
                        mimeType: 'text/html',
                        subject: "CNOM failed on Staging", 
                        to: "DSCDJsTeam@ericsson.onmicrosoft.com"
                    }
                }
            }
        }
    } // stages
    
    post {
        failure {
            script {
                // do not undeploy for troubleshooting purposes
                if (env.CLEANCLUSTER != 'FALSE') {
                    try {
                        timeout(time: 20, unit: 'MINUTES') {
                            // clean cluster due to failure in pipeline
                            sh "./bob/bob clean-resources:delete-namespace"
                            sh "./bob/bob clean-resources:create-namespace"
                            sh "./bob/bob clean-resources:remove-namespace-resources"
                            sh "./bob/bob clean-resources:remove-cluster-resources"
                        } // timeout
                    } // try
                    catch (exc) {
                        TROUBLESHOOTING_FAILURE_REASON += ("Failed to clean cluster forcefully after conclude failure")
                    } // catch
                } // if
            } // script
        } // failure
        aborted{
            script {
                sh 'echo Pipeline aborted due to timeout'
                try {
                    timeout(time: 20, unit: 'MINUTES') {
                        // clean cluster due to failure in pipeline
                        sh "./bob/bob clean-resources:delete-namespace"
                        sh "./bob/bob clean-resources:create-namespace"
                        sh "./bob/bob clean-resources:remove-namespace-resources"
                        sh "./bob/bob clean-resources:remove-cluster-resources"
                    } // timeout
                } // try
                catch (exc) {
                    TROUBLESHOOTING_FAILURE_REASON += ("Failed to clean cluster forcefully after conclude abort")
                } // catch
            }
        }// abort
    } // post
} //pipeline
