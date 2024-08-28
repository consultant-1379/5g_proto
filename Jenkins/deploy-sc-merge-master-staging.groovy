def AGENT_LABEL = null
//def SEPP = true
node('DSC_BuildSlave_GIC_1||DSC_BuildSlave_GIC_2||DSC_BuildSlave_GIC_3||DSC_BuildSlave_GIC_4||DSC_BuildSlave_GIC_5') {
	stage('set agent'){
		if (env.IP_VERSION == '6') {   // move to IPv6 buildslave(s)
			AGENT_LABEL = '5G-SC-IPv6'
			DEPLOY_SEPP = false
			DEPLOY_CSA  = false
		}
		else                         // move to IPv4 buildslave(s)
		{
			AGENT_LABEL = '5G-SC'      // in the future
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

pipeline {

	agent {
	 	//Set label according to IP_VERSION parameter
		label "${AGENT_LABEL}"
	}

	options{
        timeout(time: 45, unit: 'MINUTES')
        timestamps()
    }

	environment {
		// Variables needed for the ruleset
		BSF = true
		CSA = "${DEPLOY_CSA}"   // depends on IPv6 (future)
		NLF = true
		SCP = true  
		SLF = true
		RLF = true
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
		//COMMITTER = "${COMMITTER}"
		PROJECT = "${PROJECT}"
		DEPLOY_CRD = false
	}

	stages {
		stage('Freeing up workspace on other buildslaves') {
			steps {
				sh '''	/bin/bash -x 
						IP_ADDRESS_TMP=`hostname -i`;
						BUILD_PATH_TMP=`pwd`;
						for ip in `seq -f "10.210.174.%g" 60 62`;
						do
							if [ "${IP_ADDRESS_TMP}" != "${ip}" ];
							then
								ssh eiffelesc@${ip} "if [ -d ${BUILD_PATH_TMP} ]; then rm -rf ${BUILD_PATH_TMP}/*; else echo \"Nothing to clean\"; fi;"
							fi;
						done;'''
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

                    // set the displayed build name to "BUILD_NUMBER - COMMIT"
                    currentBuild.displayName = "#${BUILD_NUMBER} - ${env.COMMIT}"

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
				echo("NIGHTLY: ${env.NIGHTLY}")
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
				sh './bob/bob init:set-build-proxy'
				sh './bob/bob init:set-kube-config'
				sh './bob/bob init:set-namespace'
				sh './bob/bob init:set-ingressHost'
                sh  """  tac esc/helm/eric-sc-umbrella/requirements.yaml | grep -m1 'version' | cut -d ' ' -f4 > ./.bob/var.esc-version
						echo "the esc version is set to : `cat ./.bob/var.esc-version` !" 
                    """
                sh './bob/bob init:cxp-number'
				sh './bob/bob init:cxp-rev-number'
                sh './bob/bob init:set-pmbr-object-storage-enabled'
				sh './bob/bob init:set-supreme-properties'
				
                
            } //steps
        } //stage
        stage('Create SC Umbrella package') {
			steps {
                sh "./bob/bob update-umbrella:copy"
                sh "./bob/bob update-umbrella:update-product-numbers"
                sh "./bob/bob package-umbrella-fast"
            } //steps
        } //stage
        stage('Install SC Umbrella package') {
            steps {
                sh "echo \"Starting to install... \""
                sh "./bob/bob deploy"
			} // steps 
		} //stage
		stage("Wait for base resources") {
			steps {
				sh """	./bob/bob wait-for-deployed-resources:services;
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
				//sh """ ./bob/bob install-default-users:no-pwdReset; sleep 60;"""
			} // steps 
		} //stage
		stage("Install Default Certificates") {
			steps {
				sh """	./bob/bob install-certificates:rootca;
						./bob/bob install-certificates:csa-manager;
						./bob/bob install-certificates:csa-worker;
						./bob/bob install-certificates:nlf;
						./bob/bob install-certificates:scp-manager;
						./bob/bob install-certificates:scp-worker;
						./bob/bob install-certificates:slf;
						./bob/bob install-certificates:rlf;
						./bob/bob install-certificates:sepp-manager;
						./bob/bob install-certificates:sepp-worker;
						sleep 60;  """
			} // steps 
		} //stage
		stage("Load Default Configuration") {
			steps {
				sh """	echo "IPv6: ";
						kubectl get configmap/calico-config -o jsonpath='{.data.cni_network_config}' -n kube-system  --kubeconfig ~/.kube/${env.KUBE_HOST}.config | grep -v CNI_MTU | jq -r '.plugins[0].ipam.assign_ipv6'
						./bob/bob loadConfig:sc;
						 """
            } // steps 
		} //stage
		stage("Wait for SC resources") {
			steps {
				script {
					if (env.IP_VERSION == '6') { // check all resource, but sepp, to come up
						sh "./bob/bob wait-for-deployed-resources:seppless;"
					} // if
					else {
						
						sh "echo Waiting for resources to be ready! Ingnoring init sw-inventory"
						// sh """./bob/bob wait-for-deployed-resources:adp-staging;"""
						sh """./bob/bob wait-for-deployed-resources:selectedApps;"""
					} // if
				} // script
			} // steps 
		} //stage
		stage("Archiving parameters for downstream Test pipeline") {
			steps {
				sh 'echo "KUBE_HOST=$(echo ${KUBE_HOST})" > test.properties'
				sh """echo "NAMESPACE=`cat ${env.OUTPUT_DIR}/var.namespace`" >> test.properties;"""
				sh """echo "VERSION=`cat ${env.OUTPUT_DIR}/var.esc-version`" >> test.properties;"""
				sh """echo "COMMIT=`echo ${env.COMMIT}`" >> test.properties;"""
				sh """echo "CHART_VERSION=`cat ${env.OUTPUT_DIR}/var.esc-version`" >> test.properties;"""
				//sh 'echo "COMMITTER=$(echo ${COMMITTER})" >> test.properties;'
				sh 'echo "PROJECT=$(echo ${PROJECT})" >> test.properties;'
				archiveArtifacts artifacts: 'test.properties', onlyIfSuccessful: true
			} // steps
		} //stage
	} // stages
	
	post {
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
						timeout(time: 600000, unit: 'MILLISECONDS') {
							// clean cluster due to failure in pipeline
							
							sh "./bob/bob cleanAdpStaging:cleanClusterAdpStaging"
						} // timeout
					} // try
					catch (exc) {
						sh """	kubectl delete ns \$(cat .bob/var.namespace) --kubeconfig ~/.kube/${env.KUBE_HOST}.config;
								kubectl create ns \$(cat .bob/var.namespace) --kubeconfig ~/.kube/${env.KUBE_HOST}.config;"""
					} // catch
				} // if

				// TEST_TYPE is SmokeTest -> inform A-Team
				if ("$TEST_TYPE".contains("SmokeTest")) {
					emailext body: "hi ${env.COMMITTER},<br><br> the deploy failed, maybe due to your commit? <br><br> Please check:<br>"+"${emailbody4}", 
					mimeType: 'text/html',
					subject: "5G Smoke test deploy failed for ${COMMIT} (test)", 
					to: "${env.EMAIL_COMMITTER}" +", DSCAteam@ericsson.onmicrosoft.com"
				} // if
				
				// TEST_TYPE is AdpStaging -> inform Challengers
				if ("$TEST_TYPE".contains("AdpStaging")) {
					emailext body: "Hello ADP CICD owners,<br><br> SC application deployment failed while testing ${CHART_NAME} ${CHART_REPO} ${CHART_VERSION}.<br>Please take appropriate actions!<br><br> Please check:<br>"+"${emailbody4}", 
					mimeType: 'text/html',
					subject: "SC ADP CICD Staging deploy failed", 
					to: "IXG-ChallengersTeam@ericsson.onmicrosoft.com"
				} // if
			} // script
		} // failure
		aborted{
				sh 'echo Pipeline aborted due to timeout'
				sh './bob/bob cleanAdpStaging:cleanClusterAdpStaging'
        }// abort
	} // post
} // pipeline
