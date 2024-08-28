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


currentBuild.displayName = "#${BUILD_NUMBER} - ${env.COMMIT}"

pipeline {
    
    agent { label "${AGENT_LABEL}"   } // set label according to IP_VERSION parameter
    
    environment {
        // Variables needed for the ruleset
        BSF = true
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

    }

	stages{
        stage('Freeing up workspace on other buildslaves'){
            steps{

            sh ''' /bin/bash -x 
                       IP_ADDRESS_TMP=`hostname -i`;
                       BUILD_PATH_TMP=`pwd`;
                       for ip in `seq -f "10.210.174.%g" 60 62`;
                       do
                            if [ "${IP_ADDRESS_TMP}" != "${ip}" ];
                            then
                           
                                ssh eiffelesc@${ip} "if [ -d ${BUILD_PATH_TMP} ]; then rm -rf ${BUILD_PATH_TMP}/*; else echo \"Nothing to clean\"; fi;"
                           
                            fi;
                      
                       done;'''
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

				sh './bob/bob init:set-pmbr-object-storage-enabled'
			} // steps 
		} //stage
        stage('Undeploy') {
			steps {
				script {
					try {
						timeout(time: 600, unit: 'SECONDS') {
							sh "./bob/bob cleanAdpStaging:cleanClusterAdpStaging"
						} // timeout
					} // try
					catch (exc) {
						sh """	kubectl delete ns \$(cat .bob/var.namespace) --kubeconfig ~/.kube/${env.KUBE_HOST}.config;
								kubectl create ns \$(cat .bob/var.namespace) --kubeconfig ~/.kube/${env.KUBE_HOST}.config;"""
					} // catch
				} // script
			} // steps
		} //stage
        stage('Merging latest running Master for 5g_proto'){		
			when {
            	environment name: 'TROUBLESHOOTING', value: 'FALSE';   // not to be done in case of a development pipe 
                environment name: 'MERGE_MASTER', value: 'TRUE'; // when running on master for merging purposes             
            }
            steps{
			    script{
			    	
			        echo "Checking out ${env.BRANCH}"
                    sh "git checkout ${env.BRANCH}"
    				sh "git pull origin ${env.BRANCH}"
    				
    				echo "Checking out ${STAGING_BRANCH}"
    				sh "git checkout ${STAGING_BRANCH}"
    				sh "git pull origin ${STAGING_BRANCH}"
    				sh 'git log -n 10 --oneline --decorate --graph --all'
    				
				    echo "Merging master to ${STAGING_BRANCH}"
				    sh "git merge ${env.BRANCH} || true"
				    sh 'git diff --name-only --diff-filter=U'
			        CONFLICT = """${sh( returnStdout: true, script: 'git diff --name-only --diff-filter=U')}""".trim();
				    echo "conflicts: ${CONFLICT}"

				    REQ_FLAG = """${sh( returnStdout: true, script: "echo \"${CONFLICT}\" | grep -o requirements.yaml || true")}""".trim();

				    echo "req-flag: ${REQ_FLAG}"

				    sh 'git checkout baseline_scripts/scripts/conflictsResolve.sh --ours'

				    if("${CONFLICT}" != "") {
    				    if("${CONFLICT}" != "esc/helm/eric-sc-umbrella/requirements.yaml") {
    				        sh './bob/bob conflict-resolve:other-conflicts-resolve'
    				        echo "conflicts after script: ${CONFLICT}"
    				        sh 'git diff --name-only --diff-filter=U'
    				    } 
    				    if("${REQ_FLAG}" != "") {
    				        echo "fixing conflicts in Requirements.yaml file"
        					sh 'git show :2:esc/helm/eric-sc-umbrella/requirements.yaml > requirements.ours' //file from adp-lift
        					sh 'git show :3:esc/helm/eric-sc-umbrella/requirements.yaml > requirements.theirs' //file from master

        					sh './bob/bob conflict-resolve:requirements-conflicts-resolve'
        					sh 'cat requirements.yaml'
        					sh 'mv -f requirements.yaml esc/helm/eric-sc-umbrella'
        					sh 'git add esc/helm/eric-sc-umbrella/requirements.yaml'
    				    }
                        try {
    				        sh "git commit -m \"merge master into ${env.COMMIT} with fixed conflicts\" "
    				    } catch (exc) {
                            catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                                sh 'git merge --abort'
                                if ("$TEST_TYPE".contains("AdpStaging")) {
                                    echo 'MERGE FAILED!!!'
                                    emailext body: "Hello ADP CICD owners,<br><br> Merging on 5g_test_ci failed because of Conflicts.<br>Please proceed with manual merging of master!<br><br> Please check:<br>"+"${emailbody4}", 
                                    mimeType: 'text/html',
                                    subject: "SC ADP CICD Staging deploy failed", 
                                    to: "dimitris.mylonidis@ericsson.com"
                                }
                                sh "exit 1"
                            } //catchError
                        } //try
        			} // if                             
                } //script
            } //steps
        } //stage
        stage('Clone test repository'){
            when {
            	environment name: 'TROUBLESHOOTING', value: 'FALSE';   // not to be done in case of a development pipe 
                environment name: 'MERGE_MASTER', value: 'TRUE'; // when running on master for merging purposes             
            }
            steps{
           
                echo 'Creating test repo directory'
                dir('5g_test_ci'){
                    deleteDir()
                }

                echo pwd()
                echo 'Trying to clone the 5G prototype test repository'
                checkout scmGit(
                	branches: [[name: '*/${TEST_BRANCH}']],
                	userRemoteConfigs: [[credentialsId: 'eiffelesc-user-password', name: 'origin', url: 'https://eiffelesc@gerrit.ericsson.se/a/MC_5G/5g_test_ci']],
                	extensions: [submodule(parentCredentials: true, recursiveSubmodules: true, reference: '', shallow: false), [$class: 'RelativeTargetDirectory', relativeTargetDir: '5g_test_ci']]
                )
                echo 'check content'
                dir('5g_test_ci'){
                    echo pwd()
                    sh 'git log -n 10'
                    sh 'ls -ltrh'
                    sh 'git submodule update --init --recursive'
                    
                }
            }
        }
        stage('Merging latest running commit Master for 5g_test_ci'){
			when {
            	environment name: 'TROUBLESHOOTING', value: 'FALSE';   // not to be done in case of a development pipe 
                environment name: 'MERGE_MASTER', value: 'TRUE'; // when running on master for merging purposes             
            }
            steps{
                dir('5g_test_ci'){
                    script{
                        echo "Checking out ${env.BRANCH}."
                        sh "git checkout ${env.BRANCH}"
                        sh 'git pull'

                        echo "Checking out ${STAGING_BRANCH}."
                        sh "git checkout ${STAGING_BRANCH}"
                        sh 'git pull'

                        //try{
        				    echo "Merging master to ${STAGING_BRANCH}"
        				    sh "git merge ${env.BRANCH} || true"
        				    env.CONFLICT = """${sh( returnStdout: true, script: 'git diff --name-only --diff-filter=U')}""".trim();
        				    sh 'echo ----------'
        				    echo "conflict: ${CONFLICT}"
        				    if("${CONFLICT}" != "") {
        				        sh 'git status'
	    				        echo "There are Conflicts that should be dealt manually, merge aborted!"
        				        sh 'git merge --abort'
                                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                                    if ("$TEST_TYPE".contains("MergeMaster")) {
                    					emailext body: "Hello ADP CICD owners,<br><br> Merging on 5g_test_ci failed because of Conflicts.<br>Please proceed with manual merging of master!<br><br> Please check:<br>"+"${emailbody4}", 
                    					mimeType: 'text/html',
                    					subject: "SC ADP CICD Staging deploy failed", 
                    					to: "IXG-ChallengersTeam@ericsson.onmicrosoft.com"
                    				}
                                    sh "exit 1"
                                }
        				    }
			                else {
                               sh "git push origin ${STAGING_BRANCH}"
                            }
        				//}
        				sh 'git log -n 10 --oneline --decorate --graph --all'
                    }
    			} // dir
                echo "Pushing master commit too"
                sh "git push origin ${STAGING_BRANCH}"
                sh 'git log -n 10 --oneline --decorate --graph --all'	    
    	    } // steps 
        } //stage
	} // stages
} //pipeline
