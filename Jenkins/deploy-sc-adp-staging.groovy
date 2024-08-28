
/////////////////////////////////////////////////////////////////////////
//                                                                     //
// set jenkins var CLEANCLUSTER=FALSE to not undeploy after an error   //
//                                                                     //
/////////////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////
//
// first run on SERO_GIC buildslaves, decide which buildslave(s) to use

def AGENT_LABEL = null
//def SEPP = true
node('DSC_BuildSlave_GIC_1||DSC_BuildSlave_GIC_2||DSC_BuildSlave_GIC_3||DSC_BuildSlave_GIC_4||DSC_BuildSlave_GIC_5') {
    stage('set agent'){
        if (env.IP_VERSION == '6') { // move to IPv6 buildslave(s)
            AGENT_LABEL = '5G-SC-IPv6'
            DEPLOY_SEPP = false
            DEPLOY_CSA  = false
        }
        else // move to IPv4 buildslave(s)
        {
            AGENT_LABEL = '5G-SC' // in the future
            DEPLOY_SEPP = true
            DEPLOY_CSA  = false
        } //if
    } //stage
} //node
//////////////////////////////////////////////////////////////////////////

def HELM_CMD
def GIT_CMD
def PYTHON_CMD
def values
def emailbody4="<br>Logs:<br>"+"${env.BUILD_URL}"+"<br><br><br><br>Thank you for pushing.<br><br>BR,<br>SC ADP Staging"
def TROUBLESHOOTING_FAILURE_REASON=[]

pipeline {

    agent {
         //Set label according to IP_VERSION parameter
        label "${AGENT_LABEL}"
    }

    options{
        timeout(time: 60, unit: 'MINUTES')
        timestamps()
    }

    environment {
        // Variables needed for the ruleset
        BSF = true
        CSA = "${DEPLOY_CSA}"   // depends on IPv6 (future)
        NLF = true
        RLF = true
        SCP = true  
        SLF = true
        SPR = false
        WCDB = true
	BSF_DIAMETER = true
        SEPP = "${DEPLOY_SEPP}"    // depends on Ipv6
        BSF_TLS = false
        CSA_TLS = false
        SCP_TLS = false
        LOGGING = true
        PRODUCTION = true
        RESOURCES = "${RESOURCES}"
        

        // Pipe Variables Used in Jenkins
        DOCKER_RUN="docker run --rm  --user \$(id -u):\$(id -g) "
        BUILDER_WORK_DIR = " -w ${env.WORKSPACE} "
        BUILDER_DOCKER_IMAGE = "armdocker.rnd.ericsson.se/sandbox/adp-staging/adp-cicd/bob-py3kubehelmbuilder:fd49f94"
        BUILDER_DOCKER_VOLUMES ="-v ${env.WORKSPACE}/helm-home:/home/jenkins/.helm -v ${env.WORKSPACE}:${env.WORKSPACE}"
        BUILDER_DOCKER_ENV = "--env KUBECONFIG=${env.WORKSPACE}/.bob/${env.KUBE_HOST}.admin.conf"
        HELM_ARGS="--home /home/jenkins/.helm"
        HELM_RELEASE_NAME="bsf-app-staging-trial"
        OUTPUT_DIR=".bob"

        // ADP staging CHART_X parameters injected by Spinnaker or Jenkins, no need to define it here
        //CHART_NAME = "${CHART_NAME}"
        //CHART_REPO = "${CHART_REPO}"
        //CHART_VERSION = "${CHART_VERSION}"
        
        KUBE_HOST = "${KUBE_HOST}"
        ARTIFACTORY_TOKEN = "${ARTIFACTORY_TOKEN}"
        NIGHTLY = "${NIGHTLY}"
        NAMESPACE = "${NAMESPACE}"
        PACKAGING = "${PACKAGING}"
        PROJECT = "${PROJECT}"
        DEPLOY_CRD = false
        PMBR_OBJECT_STORAGE = true
        OBJECT_STORAGE = true
        SFTP = true
    }

    stages {
        stage('Print env vars') {
            steps {
                sh 'printenv | sort'
                script{
                    // set displayed build name
                    currentBuild.displayName = "#${BUILD_NUMBER}>${env.CHART_NAME}:${env.CHART_VERSION}"
                    // set displayed description
                    currentBuild.description = "${env.NODE_NAME}, ${env.KUBE_HOST}<br>${env.CHART_REPO},  ${env.CHART_NAME}, ${env.CHART_VERSION}<br>${env.COMMIT}";
                    if ( fileExists("/proj/DSC_CI/jenkins/tools/help_scripts/concurrentBuilds.sh") ) { 
                        // Log current job 
                        sh "/proj/DSC_CI/jenkins/tools/help_scripts/concurrentBuilds.sh -start"
                    } // if                    
                } //script
            } //steps
        } //stage
        stage('Freeing up workspace on other buildslaves') {
            steps {
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
            } // steps 
        } // stage

        stage('checkout the correct commit') {
             steps {
                script {
                    echo "COMMIT: ${env.COMMIT}"
                    echo "BRANCH: ${env.BRANCH}"

                    echo "Checking out ${env.COMMIT}"
                    checkout scmGit(
                        branches: [[name: env.COMMIT]],
                        extensions: [submodule(parentCredentials: true, recursiveSubmodules: true, reference: '', shallow: false), [$class: 'WipeWorkspace']], 
                        userRemoteConfigs: [[credentialsId: 'eiffelesc-user-password', name: 'origin', url: 'https://eiffelesc@gerrit.ericsson.se/a/MC_5G/5g_proto']]
                      //userRemoteConfigs: [[url: 'https://eiffelesc@gerritmirror-direct.sero.gic.ericsson.se/a/MC_5G/5g_proto']]
                    ) // checkout

                } // script
             }  // steps 
         } // stage
         
        stage('Initialize') {
            steps {
                echo "CHART NAME: ${env.CHART_NAME}"
                echo "CHART REPO: ${env.CHART_REPO}"
                echo "CHART VERSION: ${env.CHART_VERSION}"
                echo "ARTIFACTORY TOKEN: ${env.ARTIFACTORY_TOKEN}"
                echo "KUBE HOST: ${env.KUBE_HOST}"
                echo "NAMESPACE: ${env.NAMESPACE}"
                echo "UPGRADE_CRDS: ${env.UPGRADE_CRDS}"
                echo "CLEAN_CRDS: ${env.CLEAN_CRDS}"
                echo "NIGHTLY: ${env.NIGHTLY}"
                
                script {
                    PYTHON_CMD = "${DOCKER_RUN} ${BUILDER_DOCKER_VOLUMES} -w ${env.WORKSPACE} ${BUILDER_DOCKER_ENV} ${BUILDER_DOCKER_IMAGE} python"
                    HELM_CMD = "${DOCKER_RUN} ${BUILDER_DOCKER_VOLUMES} ${BUILDER_DOCKER_ENV} ${BUILDER_DOCKER_IMAGE} helm"
                } // script
            } // steps 
        } //stage
        stage('Init bob'){
            steps{
                echo pwd()
                sh 'ls -ltrh'
                sh 'git submodule update --init --recursive'
                //sh './bob/bob base.base-image'
                sh './bob/bob init:set-build-proxy'
                sh './bob/bob init:set-kube-config'
                sh './bob/bob init:set-namespace'
                sh './bob/bob init:set-ingressHost'
                sh './bob/bob init:set-supreme-properties'
                sh ' printenv | sort'

                // extract esc version of build from requirements.yaml
                sh 'cat esc/helm/eric-sc-umbrella/requirements.yaml | grep $(cat VERSION_PREFIX) | tail -n 1 | awk -F \"version: \" \'{print $2}\' > .bob/var.esc-version' 
            
                
            } // steps 
        } //stage
         
        
        stage('Update SC Umbrella') {
            steps {
                // Copy umbrella chart folder to .bob directory
                sh "./bob/bob update-umbrella:copy"
                script{
                    if ("${CHART_NAME}"== "eric-sc") {    
                        sh """#!/bin/bash
                            [[ "${env.BSF}" == "true" ]] && echo ${env.CHART_VERSION} > ./.bob/var.bsf-version;
                            [[ "${env.BSF}" == "true" ]] && echo ${env.CHART_VERSION} > ./.bob/var.bsf-diameter-version;
                            [[ "${env.NLF}" == "true" ]] && echo ${env.CHART_VERSION} > ./.bob/var.nlf-version;
                            [[ "${env.SCP}" == "true" ]] && echo ${env.CHART_VERSION} > ./.bob/var.scp-version;
                            [[ "${env.SLF}" == "true" ]] && echo ${env.CHART_VERSION} > ./.bob/var.slf-version;
                            [[ "${env.SEPP}" == "true" ]] && echo ${env.CHART_VERSION} > ./.bob/var.sepp-version;
                            [[ "${env.RLF}" == "true" ]] && echo ${env.CHART_VERSION} > ./.bob/var.rlf-version;
                            echo ${env.CHART_VERSION} > ./.bob/var.bragent-version;
                            echo ${env.CHART_VERSION} > ./.bob/var.monitor-version;
                            echo ${env.CHART_VERSION} > ./.bob/var.certnotifier-version;
                            echo ${env.CHART_VERSION} > ./.bob/var.esc-version;
                            echo ${env.CHART_VERSION} > ./.bob/var.chfsim-version;
                            echo ${env.CHART_VERSION} > ./.bob/var.nrfsim-version;
                            echo ${env.CHART_VERSION} > ./.bob/var.seppsim-version;
                        """
                        sh """#!/bin/bash
                            [[ "${env.BSF}" == "true" ]] && ./bob/bob update-umbrella:bsf && ./bob/bob update-umbrella:bsf-diameter;
                            [[ "${env.NLF}" == "true" ]] && ./bob/bob update-umbrella:nlf;
                            [[ "${env.SCP}" == "true" ]] && ./bob/bob update-umbrella:scp;
                            [[ "${env.SLF}" == "true" ]] && ./bob/bob update-umbrella:slf;
                            [[ "${env.SEPP}" == "true" ]] && ./bob/bob update-umbrella:sepp;
                            [[ "${env.RLF}" == "true" ]] && ./bob/bob update-umbrella:rlf;
                            ./bob/bob update-umbrella:bragent;
                            ./bob/bob update-umbrella:monitor;
                        """
                    }
                    else { 
                        // Input from CHART_NAME, CHART_REPO, CHART_VERSION 
                        // Update chart name/repo/version
                        sh "./bob/bob update-umbrella-generic:service;"
                    } 
                    // Update umbrella package
                    // product:number -> cxp-number
                    // product:revision -> cxp-rev-number
                    sh './bob/bob init:cxp-number'
                    sh './bob/bob init:cxp-rev-number'
                    sh './bob/bob init:product-info'
                    sh "./bob/bob update-umbrella:update-product-numbers"
                }
            } // steps 
        } //stage
        stage('Package/Deploy CRDs'){
            when{
                anyOf{
                    environment name: 'CLEAN_CRDS', value: 'true';
                    environment name: 'UPGRADE_CRDS', value: 'true';
                }    
            } //when    
            steps{ 
                script{

                    // no crds in the cluster or corrupted,clean & install all that SC is using
                    // Uninstall releases 
                    // remove crds kept behind because of policy with label
                    if ("${CLEAN_CRDS}"== "true") {    
                        sh "./bob/bob undeploy-crds:clean-crds"
                        sh "./bob/bob fetch-crds:clean;"
                        sh "./bob/bob deploy-crds;"
                    } 

                    // check the new CHART and deploy its needed crd resource
                    // use generic helm install & upgrade only this one
                    // it uses CHART_NAME & CHART_VERSION from parameters 
                    // package and upgrade/install
                    if ("${UPGRADE_CRDS}"== "true") {
                        sh "echo this is a service that is a CRD one!"
                        sh "./bob/bob deploy-crds-generic:package-deploy-crd-generic"
                    }
                    
                } //script
            } // steps
        } //stage
        stage('Create SC Umbrella package') {
            steps {
                // Create needed charts folder
                // Fetch all dependencies and store in umbrella/charts
                // Package umbrella chart and store in .bob folder
                // Temporary store .tgz charts in 5g_proto/charts
                sh "./bob/bob package-umbrella-fast"

                // User .bob folder requirements and
                // add all chart repo to local repo
                // then helm package with dependency updates
                // and store tgz package in .bob folder
                //sh "./bob/bob package-umbrella"
            } // steps 
        } //stage
        stage('Install SC Umbrella package') {
            steps {
                // Install needed secrets
                echo 'Install day-0 secrets...'
                sh "./bob/bob deploy:adp-secrets"
                echo 'Refresh image pull secret...'
                withCredentials([string(credentialsId: 'c9bf8e3d-476c-4be9-b26a-73d0275975bd', variable: 'ARTIFACTORY_TOKEN'),
                                 string(credentialsId: 'eiffelesc-armsero-token', variable: 'ARMSERO_TOKEN')]) {
                    sh "./bob/bob deploy:imagePullSecret"
                }

                // Generate values-final based on eric-sc-values.yaml
                echo 'Generate day-0 configuration...'
                sh "./bob/bob generate-values"
                
                // helm install previously created sc umbrella package
                echo 'Starting to install SC integration chart...'
                sh "./bob/bob deploy:ihc"
                
                // Install internally used pm httpproxy
                // Install internally used osmn httpproxy
                echo 'Deploy httpproxy resources for local verification activities...'
                sh "./bob/bob deploy:pm-httpproxy deploy:osmn-httpproxy deploy:envoy-admin-httpproxy"
            } // steps 
        } //stage
        stage("Wait for base resources") {
            steps {
                sh """    ./bob/bob wait-for-deployed-resources:services;
                        sleep 60;"""
            } // steps 
        } //stage
        stage("Show deployed pods") {
            steps {
                
                sh "kubectl get pods -n \$(cat .bob/var.namespace) --kubeconfig ~/.kube/${env.KUBE_HOST}.config"
            }  // steps 
        } //stage
        stage("Install Default Users") {
            steps {
                sh """./bob/bob install-default-users:default-users; sleep 60;"""
            } // steps 
        } //stage
        stage("Install Default Certificates") {
            steps {
                sh """./bob/bob install-certificates:rootca;
                      ./bob/bob install-certificates:nlf;
                      ./bob/bob install-certificates:scp-manager;
                      ./bob/bob install-certificates:scp-worker;
                      ./bob/bob install-certificates:slf;
                      ./bob/bob install-certificates:sepp-manager;
                      ./bob/bob install-certificates:sepp-worker;
                      ./bob/bob install-certificates:nbi;
                      ./bob/bob install-certificates:transformer;
                      ./bob/bob install-certificates:syslog;
                      sleep 60;"""
            } // steps 
        } //stage
        stage("Install simulated tools certs") { //This stage is executed only for simulated tools in order to add certs
            when{
                environment name: 'TEST_TYPE', value: 'SimulatedTools';
            }
            steps {
                sh "cd scripts/; ./install_certs.sh chfsim-dynamic seppsim-dynamic k6-dynamic nrfsim-dynamic"  
                //TODO : adapt these with supreme tool   
            } // steps 
        } //stage
        stage("Load Default Configuration") {
            steps {
                sh """#!/bin/bash
                      ./bob/bob loadConfig:adp-staging;
                      """
            } // steps 
        } //stage
        stage("Wait for SC resources") {
            steps {
                script {
                    sh """#!/bin/bash
                      ./bob/bob wait-for-deployed-resources:selectedApps;
                      """
                } // script
            } // steps 
        } //stage
        stage("Checking tools existense!"){
            steps {
                
                script {
                    if ("$TEST_TYPE".contains("AdpStaging")) {
                        sh "kubectl --kubeconfig ~/.kube/${env.KUBE_HOST}.config -n  ${env.NAMESPACE} get pods | grep atmoz"
                        sh "kubectl --kubeconfig ~/.kube/${env.KUBE_HOST}.config -n  ${env.NAMESPACE} get pods | grep k6"
                        // sh "kubectl --kubeconfig ~/.kube/${env.KUBE_HOST}.config -n  ${env.NAMESPACE} get pods | grep nels"
                    } // if
                    if ("$TEST_TYPE".contains("SimulatedTools")) {
                        sh "kubectl --kubeconfig ~/.kube/${env.KUBE_HOST}.config -n  ${env.NAMESPACE} get pods | grep chfsim"
                        sh "kubectl --kubeconfig ~/.kube/${env.KUBE_HOST}.config -n  ${env.NAMESPACE} get pods | grep k6"
                        // sh "kubectl --kubeconfig ~/.kube/${env.KUBE_HOST}.config -n  ${env.NAMESPACE} get pods | grep nels"
                    } // if
                } //script
            } //steps
        } //stage
        stage("Archiving parameters for downstream Test pipeline") {
            steps {
                sh 'echo "KUBE_HOST=$(echo ${KUBE_HOST})" > test.properties'
                sh """echo "NAMESPACE=`cat ${env.OUTPUT_DIR}/var.namespace`" >> test.properties;"""
                sh """echo "COMMIT=`echo ${env.COMMIT}`" >> test.properties;"""
                archiveArtifacts artifacts: 'test.properties', onlyIfSuccessful: true
            } // steps
        } //stage
    } // stages
    
    post {
        always {
            script{
                // Log current job
                if ( fileExists("/proj/DSC_CI/jenkins/tools/help_scripts/concurrentBuilds.sh") ) { 
                    sh "/proj/DSC_CI/jenkins/tools/help_scripts/concurrentBuilds.sh -stop"
                } // if
            } // script
        } // always
        failure {
            script {
                try{
                    sh "./scripts/collect_ADP_logs.sh -c ~/.kube/${env.KUBE_HOST}.config -n `cat .bob/var.namespace`"
                    archiveArtifacts allowEmptyArchive: true, artifacts: 'logs_*.tgz', onlyIfSuccessful: false
                } catch(exc) {
                    sh 'echo Exception during collect adp logs script happened!'
                }

            
                // do not undeploy for troubleshooting purposes
                if (env.CLEANCLUSTER != 'FALSE') {          
                    try {
                        timeout(time: 20, unit: 'MINUTES') {
                            // clean cluster due to failure in pipeline
                            sh "./bob/bob clean-resources:delete-namespace"
                            sh "./bob/bob clean-resources:create-namespace"
                            sh "./bob/bob clean-resources:remove-cluster-resources"
                        } // timeout
                    } // try
                    catch (exc) {
                        TROUBLESHOOTING_FAILURE_REASON += ("Failed to clean cluster forcefully after failure")
                    } // catch
                } // if
                // TEST_TYPE is AdpStaging -> inform Challengers
                if ("$TEST_TYPE".contains("AdpStaging")) {
                    emailext body: "Hello ADP CICD owners,<br><br> SC application deployment failed while testing ${CHART_NAME} ${CHART_REPO} ${CHART_VERSION}.<br>Please take appropriate actions!<br><br> .Folowing are the possible Troubleshooting reasons: ${TROUBLESHOOTING_FAILURE_REASON} <br><br>.Please check:<br>"+"${emailbody4}", 
                    mimeType: 'text/html',
                    subject: "SC ADP CICD Staging deploy failed",
                    to: "IXG-ChallengersTeam@ericsson.onmicrosoft.com"
                } 
                // CHART_NAME is WCDB -> inform IXG3
                if ("$CHART_NAME".contains("eric-data-wide-column-database-cd")) {
                    emailext body: "Hello IXG3 team,<br><br> SC application deployment failed while testing ${CHART_NAME} ${CHART_REPO} ${CHART_VERSION}.<br>Please take appropriate actions!<br><br> .Folowing are the possible Troubleshooting reasons: ${TROUBLESHOOTING_FAILURE_REASON} <br><br>.Please check:<br>"+"${emailbody4}", 
                    mimeType: 'text/html',
                    subject: "WCDB deploy failed",
                    to: "dsc_ixg3@ericsson.onmicrosoft.com"
                }
                // CHART_NAME is stm-diameter -> inform IXG3
                else if ("$CHART_NAME".contains("eric-stm-diameter")) {
                    emailext body: "Hello IXG3 team,<br><br> SC application deployment failed while testing ${CHART_NAME} ${CHART_REPO} ${CHART_VERSION}.<br>Please take appropriate actions!<br><br> .Folowing are the possible Troubleshooting reasons: ${TROUBLESHOOTING_FAILURE_REASON} <br><br>.Please check:<br>"+"${emailbody4}", 
                    mimeType: 'text/html',
                    subject: "Stm diameter deploy failed",
                    to: "dsc_ixg3@ericsson.onmicrosoft.com"
                }
                if ("${UPGRADE_CRDS}"== "true") {
                    try {
                        timeout(time: 10, unit: 'MINUTES') {
                            // Install the old charts' CRD if it exists
                            // undeploy specific chart
                            sh "./bob/bob undeploy-crds-generic"
                            sh "./bob/bob deploy-crds-generic:package-deploy-crd-generic-old"
                        } // timeout
                    } // try
                    catch (exc) {
                        TROUBLESHOOTING_FAILURE_REASON += ("Failed to clean crds gracefully after failure")
                        sh "./bob/bob undeploy-crds"
                        sh "./bob/bob fetch-crds"
                        sh "./bob/bob deploy-crds;"
                    } // catch
                }
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
                    TROUBLESHOOTING_FAILURE_REASON += ("Failed to clean cluster forcefully after abort")
                } // catch
            }
        }// abort
    } // post
} // pipeline
