def AGENT_LABEL = null

node('DSC_BuildSlave_GIC_1 ||DSC_BuildSlave_GIC_2||DSC_BuildSlave_GIC_3||DSC_BuildSlave_GIC_4||DSC_BuildSlave_GIC_5') {

        AGENT_LABEL = 'esc-docker||esc-docker-1||esc-docker-2'

} //node

//
//////////////////////////////////////////////////////////////////////////

pipeline {

    agent { label "${AGENT_LABEL}" } // set label according to IP_VERSION parameter

    options{
        timeout(time: 10, unit: 'MINUTES')
        timestamps()
    }

    // environmental variables of Jenkins
    environment{
        REQUIREMENTS = "esc/helm/eric-sc-umbrella/requirements.yaml"
        //local vars
        CONFILE = ''
        REQ_FLAG = ""
        MASTER = ''
        OUTPUT_DIR=".bob"
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
                    sh 'git submodule update --init --recursive'
 //                 sh "git remote set-url origin --push https://eiffelesc@gerrit.ericsson.se/a/MC_5G/5g_proto"

                    // set the displayed build name to "BUILD_NUMBER - COMMIT"
                    currentBuild.displayName = "#${BUILD_NUMBER} - ${env.COMMIT}"
                } // script
            }  // steps 
        }//stage
        stage('Explicit handling for chart version') {
            steps {
                script{
                    if ("${CHART_NAME}" == "eric-sc")  { // case running from master is
                        // update chart variables
                        echo "Checking out master"
                        sh "git checkout master"
                        sh 'echo CHART_VERSION=$(echo $(cat esc/helm/eric-sc-umbrella/requirements.yaml | grep $(cat VERSION_PREFIX) | tail -n 1  | awk -F \"version: \" \'{print $2}\')) >> test.properties'
                        sh "git checkout ${env.BRANCH}"
                        
                    }
                    else{
                        sh 'echo "CHART_VERSION=$(echo ${CHART_VERSION})" >> test.properties'
                    }
                } // script
            } // steps 
        } //stage
      
        stage("Checking namespace"){
            
            // this scripts sets in .bob/var.namespace our namespace 
            // if there is deploy in eiffelesc-1 , namespace is setted in eiffelesc-2
            steps{
                sh './bob/bob init:set-kube-config'
                sh './bob/bob init:set-namespace'
                sh "./bob/bob clean-resources:create-namespace"
            }
            
        } //stage
        
        stage("Checking crds existence"){
            when{
                environment name: 'MERGE_MASTER', value: 'false';
            } //when
            steps{
                withCredentials([string(credentialsId: 'c9bf8e3d-476c-4be9-b26a-73d0275975bd', variable: 'ARM_ARTIFACTORY_TOKEN')]) {
                    // existense with CHART 
                    sh './bob/bob check-crds:existense'

                    // Incoming CHART has crd
                    sh './bob/bob check-crds:is-crd'
                }
                sh 'echo "UPGRADE_CRDS=$(cat ${OUTPUT_DIR}/var.is-crd )" >> test.properties'
                sh 'echo "CLEAN_CRDS=$(cat ${OUTPUT_DIR}/var.no-crds)" >> test.properties'
                
            
            } //steps
        } //stage
        stage('Creating .properties file'){
            steps{
                // you need to set all the parameters into the *.properties file otherwise they will not be seen by Spinnaker 
                sh 'echo "KUBE_HOST=$(echo ${KUBE_HOST})" >> test.properties'
                
                sh 'echo "BRANCH=$(echo ${BRANCH})" >> test.properties'
                

                sh 'echo "NAMESPACE=$(cat ${OUTPUT_DIR}/var.namespace )" >> test.properties'

                sh 'echo "TEST_BRANCH=$(echo ${TEST_BRANCH})" >> test.properties'
                
                sh 'echo "TROUBLESHOOTING=$(echo ${TROUBLESHOOTING})" >> test.properties'
                sh 'echo "TEST_TYPE=$(echo ${TEST_TYPE})" >> test.properties'
    
                sh 'echo "CHART_NAME=$(echo ${CHART_NAME})" >> test.properties'
                // sh 'echo "CHART_VERSION=$(echo ${CHART_VERSION})" >> test.properties'
                sh 'echo "CHART_REPO=$(echo ${CHART_REPO})" >> test.properties'
    
                archiveArtifacts artifacts: 'test.properties', onlyIfSuccessful: true
                
            } // steps
        } // stage
    } //stages
    post{
        failure{
            echo 'Something went wrong!!!!'
            sh 'git merge --abort'
            // no need to undeploy something, no actions needed
            
        } //failures
    } //post
} //pipeline
