
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
		if (env.IP_VERSION == '6') {   // move to IPv6 buildslave(s)
			AGENT_LABEL = '5G-SC-IPv6'

		}
		else                         // move to IPv4 buildslave(s)
		{
			AGENT_LABEL = '5G-SC'      // in the future

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

	options{
        timeout(time: 20, unit: 'MINUTES')
        timestamps()
    }
	agent {
	 	//Set label according to IP_VERSION parameter
		label "${AGENT_LABEL}"
	}

	environment {
		// Pipe Variables Used in Jenkins
		DOCKER_RUN="docker run --rm  --user \$(id -u):\$(id -g) "
		BUILDER_WORK_DIR = " -w ${env.WORKSPACE} "
		BUILDER_DOCKER_IMAGE = "armdocker.rnd.ericsson.se/sandbox/adp-staging/adp-cicd/bob-py3kubehelmbuilder:fd49f94"
		BUILDER_DOCKER_VOLUMES ="-v ${env.WORKSPACE}/helm-home:/home/jenkins/.helm -v ${env.WORKSPACE}:${env.WORKSPACE}"
		BUILDER_DOCKER_ENV = "--env KUBECONFIG=${env.WORKSPACE}/.bob/${env.KUBE_HOST}.admin.conf"
		HELM_ARGS="--home /home/jenkins/.helm"
		HELM_RELEASE_NAME="bsf-app-staging-trial"
		OUTPUT_DIR=".bob"

		K6="${K6}"
		CHFSIM="${CHFSIM}"
		REDIS="${REDIS}"
		ATMOZ_SFTP="${SFTP}"
		SYSLOG="${SYSLOG}"
		NRFSIM="${NRFSIM}"
		SEPPSIM="${SEPPSIM}"
		DSCLOAD="${DSCLOAD}"

		KUBE_HOST = "${KUBE_HOST}"
		ARTIFACTORY_TOKEN = "${ARTIFACTORY_TOKEN}"
		NIGHTLY = "${NIGHTLY}"
		NAMESPACE = "${NAMESPACE}"
		PACKAGING = "${PACKAGING}"

	}

	stages {
		stage('Print env vars') {
			steps {
				sh 'printenv | sort'
				script{
					// set displayed build name to "BUILD_NUMBER - COMMIT"
					currentBuild.displayName = "#${BUILD_NUMBER} - ${env.COMMIT}"
					// set displayed description to "NODE_NAME, KUBE_HOST, CHART_VERSION"
					currentBuild.description = "${env.NODE_NAME}, ${env.KUBE_HOST}, ${env.CHART_VERSION}";

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
		stage('Initialize') {
			steps {

				echo "ARTIFACTORY TOKEN: ${env.ARTIFACTORY_TOKEN}"
				echo "KUBE HOST: ${env.KUBE_HOST}"
				echo "NAMESPACE: ${env.NAMESPACE}"
				echo "CHFSIM: ${env.CHFSIM}"
				echo "K6: ${env.K6}"
				echo "DSCLOAD: ${env.DSCLOAD}"
				echo "REDIS=${env.REDIS}"
				echo "ATMOZ_SFTP=${env.SFTP}"
				echo "SYSLOG=${env.SYSLOG}"
				echo "NRFSIM=${env.NRFSIM}"
				echo "SEPPSIM=${env.SEPPSIM}"
				script {
					PYTHON_CMD = "${DOCKER_RUN} ${BUILDER_DOCKER_VOLUMES} -w ${env.WORKSPACE} ${BUILDER_DOCKER_ENV} ${BUILDER_DOCKER_IMAGE} python"
					HELM_CMD = "${DOCKER_RUN} ${BUILDER_DOCKER_VOLUMES} ${BUILDER_DOCKER_ENV} ${BUILDER_DOCKER_IMAGE} helm"
				} // script
			} // steps
		} //stage
		stage('Init bob'){
			steps{
				echo pwd()
				sh """
				ls -ltrh
				git submodule update --init --recursive
				./bob/bob init:set-build-proxy
				./bob/bob init:set-kube-config
				./bob/bob init:set-namespace
				./bob/bob init:set-ingressHost
				./bob/bob -r rulesets/ruleset2.0-eric-tools.yaml init:credentials
				./bob/bob -r rulesets/ruleset2.0-eric-tools.yaml init:token2
				./bob/bob init:set-supreme-properties
				
                echo ${env.CHART_VERSION} > ./.bob/var.chfsim-version;
                echo ${env.CHART_VERSION} > ./.bob/var.nrfsim-version;
                echo ${env.CHART_VERSION} > ./.bob/var.seppsim-version; 
                """
			} // steps
		} //stage
		stage("Install SFTP atmoz") {
		    when{
			    environment name: 'SFTP', value: 'true';
			}
            steps {
                sh "./bob/bob -r rulesets/ruleset2.0-eric-sftp.yaml init:sftp-version image package;"

                withCredentials([string(credentialsId: 'c9bf8e3d-476c-4be9-b26a-73d0275975bd', variable: 'ARTIFACTORY_TOKEN')]) {
                    sh "./bob/bob -r rulesets/ruleset2.0-eric-sftp.yaml push;"
                }
                sh "./bob/bob -r rulesets/ruleset2.0-eric-sftp.yaml deploy;"
             } // steps
         } // stage
		stage("Install k6") {
		    when{
			    environment name: 'K6', value: 'true';
			}
            steps {
                // the certificates are installed before packaged into a docker image
                // init:create-output-dir done already in stage 'Init bob', rule init:set-build-proxy
                sh """./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml install-certs;
                    ./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml init:k6-version;
                    ./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml image:create-certs-dir;
                    ./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml image:build-k6;
                    ./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml package:helm-chart-k6;
                    ./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml push:docker-image-k6;"""

                withCredentials([string(credentialsId: 'c9bf8e3d-476c-4be9-b26a-73d0275975bd', variable: 'ARTIFACTORY_TOKEN')]) {
                    sh "./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml push:helm-chart-k6;"
                }
                sh "./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml deploy:deploy-k6;"
                // The default of 4 replica will be deployed. The number of replica will be scaled-in/out in the verification pipeleine
                // according to the selected stability suite.
                // deploy-k6:    4 replica (default)
                // bsf needs:    1 replica
                // scp needs:    9 replica
                // ci  needs:    2 replica
            } // steps
        } // stage
        stage("Install NeLS simulator") {
            when{
			    environment name: 'NELS', value: 'true';
			}
            steps {
                sh "./bob/bob -r rulesets/ruleset2.0-eric-nels.yaml create-nels-dir"

                withCredentials([string(credentialsId: 'c9bf8e3d-476c-4be9-b26a-73d0275975bd', variable: 'ARM_ARTIFACTORY_TOKEN')]) {
                    sh "./bob/bob -r rulesets/ruleset2.0-eric-nels.yaml get-nels"
                }
                sh "./bob/bob -r rulesets/ruleset2.0-eric-nels.yaml deploy"
            } // steps
        } // stage
        stage("Install Bsf Load") {
            when{
			    environment name: 'BSFLOAD', value: 'true';
			}
            steps {
                sh """./bob/bob -r rulesets/ruleset2.0-eric-bsf-load.yaml init:bsf-load-version;
                    ./bob/bob -r rulesets/ruleset2.0-eric-bsf-load.yaml init:set-artifactory-token;
                    ./bob/bob -r rulesets/ruleset2.0-eric-bsf-load.yaml build:bsf-load;
                    ./bob/bob -r rulesets/ruleset2.0-eric-bsf-load.yaml image:build-bsf-load;
                    ./bob/bob -r rulesets/ruleset2.0-eric-bsf-load.yaml lint:helm-bsf-load;
                    ./bob/bob -r rulesets/ruleset2.0-eric-bsf-load.yaml package:helm-bsf-load;
                    ./bob/bob -r rulesets/ruleset2.0-eric-bsf-load.yaml push:bsf-load-image;"""

                withCredentials([string(credentialsId: 'c9bf8e3d-476c-4be9-b26a-73d0275975bd', variable: 'ARTIFACTORY_TOKEN')]) {
                    sh "./bob/bob -r rulesets/ruleset2.0-eric-bsf-load.yaml package:helm-bsf-load;"
                }
                sh """./bob/bob -r rulesets/ruleset2.0-eric-bsf-load.yaml deploy:deploy-cert-secrets;
                    ./bob/bob -r rulesets/ruleset2.0-eric-bsf-load.yaml deploy:bsf-load;"""
            } // steps
        } // stage

        stage("Install dscload") {
            when{
			    environment name: 'DSCLOAD', value: 'true';
			}
            steps {
                sh """./bob/bob -r rulesets/ruleset2.0-eric-dscload.yaml init:dscload-version;
                    ./bob/bob -r rulesets/ruleset2.0-eric-dscload.yaml init:set-artifactory-token;
                    ./bob/bob -r rulesets/ruleset2.0-eric-dscload.yaml image:build-dscload;
                    ./bob/bob -r rulesets/ruleset2.0-eric-dscload.yaml package:helm-chart-dscload;
                    ./bob/bob -r rulesets/ruleset2.0-eric-dscload.yaml push:docker-image-dscload;"""

                withCredentials([string(credentialsId: 'c9bf8e3d-476c-4be9-b26a-73d0275975bd', variable: 'ARTIFACTORY_TOKEN')]) {
                    sh "./bob/bob -r rulesets/ruleset2.0-eric-dscload.yaml package:helm-chart-dscload;"
                }
                sh "./bob/bob -r rulesets/ruleset2.0-eric-dscload.yaml deploy:deploy-dscload;"
            } // steps
        } // stage
        stage("Install CHFsim") {
            when{
			    environment name: 'CHFSIM', value: 'true';
			}
            steps {
                withCredentials([string(credentialsId: 'c9bf8e3d-476c-4be9-b26a-73d0275975bd', variable: 'ARTIFACTORY_TOKEN')]) {
                    sh "./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml download:download-chfsim;"
                }
                sh "cp eric-chfsim-* .bob/. "
                sh """./bob/bob -r ruleset2.0-eric-esc.yaml install-certificates:chfsim;
                    ./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml deploy:deploy-chfsim;"""
            } // steps
        } // stage

        stage("Install NRFsim") {
            when{
			    environment name: 'NRFSIM', value: 'true';
			}
            steps {
                withCredentials([string(credentialsId: 'c9bf8e3d-476c-4be9-b26a-73d0275975bd', variable: 'ARTIFACTORY_TOKEN')]) {
                    sh "./bob/bob -r rulesets/ruleset2.0-eric-nrfsim.yaml download:download-nrfsim;"
                }
                sh "cp eric-nrfsim-* .bob/. "
                sh """./bob/bob -r ruleset2.0-eric-esc.yaml install-certificates:nrfsim;
                    ./bob/bob -r rulesets/ruleset2.0-eric-nrfsim.yaml deploy;"""
            } // steps
        } // stage
        stage("Install SEPPsim") {
            when{
			    environment name: 'SEPPSIM', value: 'true';
			}
            steps {
                withCredentials([string(credentialsId: 'c9bf8e3d-476c-4be9-b26a-73d0275975bd', variable: 'ARTIFACTORY_TOKEN')]) {
                    sh "./bob/bob -r rulesets/ruleset2.0-eric-seppsim.yaml download:download-seppsim;"
                }
                sh "cp eric-seppsim-* .bob/. "
                sh """./bob/bob -r ruleset2.0-eric-esc.yaml install-certificates:seppsim;
                    ./bob/bob -r rulesets/ruleset2.0-eric-seppsim.yaml deploy;"""
            } // steps
        } // stage
	} //stages
	post {
		always {
			script{
				// Log current job
				if ( fileExists("/proj/DSC_CI/jenkins/tools/help_scripts/concurrentBuilds.sh") ) { 
					sh "/proj/DSC_CI/jenkins/tools/help_scripts/concurrentBuilds.sh -stop"
				} // if
			} // script
		} // always
	} // post
} //pipeline
