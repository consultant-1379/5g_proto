
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
node('DSC_BuildSlave_GIC_1||DSC_BuildSlave_GIC_2||DSC_BuildSlave_GIC_3||DSC_BuildSlave_GIC_4') {

	stage('check pre-conditions:'){

		// parameter-value is 'null' if no Jenkins-parameter exists:
		// printing parameters in order to see which have value 'null'
		echo "env.BSF:  ${env.BSF}\nenv.NLF:  ${env.NLF}\nenv.SCP:  ${env.SCP}\nenv.SEPP: ${env.SEPP}\nenv.SLF:  ${env.SLF}\nenv.RLF:  ${env.RLF}"
		echo "env.CHFSIM:  ${env.CHFSIM}\nenv.NRFSIM:  ${env.NRFSIM}\nenv.SEPPSIM: ${env.SEPPSIM}"

		// Applications checks:
		// if no Jenkins-parameters exists or all set to false the default will be to deploy all
		if (env.BSF != 'true' && env.SCP != 'true' && env.SEPP != 'true') {
			echo('no application selected; BSF, NLF, SCP, SEPP, SLF and RLF will be deployed!')
			BSF = true;
			NLF = true;
			SCP = true;
			SEPP = true;
			SLF = true;
			RLF = true;
		} // if
		//if (( env.SLF == 'true' || env.RLF == 'true' ) && env.SCP != 'true') {
		//	error('SLF and RLF require SCP to be deployed as well, build aborted!')
		//} // if
		if (env.IP_VERSION == '6' && env.SEPP == 'true') {
			error('SEPP is not working on IPv6, build aborted!')
		}

	}// stage

	stage('set agent'){

		if (env.IP_VERSION == '6') {	// move to IPv6 buildslave(s)
			AGENT_LABEL = '5G-SC-IPv6'
		} // if
		else {							// move to IPv4 buildslave(s)
			AGENT_LABEL = '5G-SC'
		} // else

	} // stage

} // node
//////////////////////////////////////////////////////////////////////////

def HELM_CMD
def GIT_CMD
def PYTHON_CMD
def values

pipeline {

	agent { label "${AGENT_LABEL}"   } // set label according to IP_VERSION parameter

	options{
		//timeout(time: 30, unit: 'MINUTES')
		timestamps()
	} // options

	environment {
		// Variables needed for the ruleset

		// If no Jenkins job-parameter and no environment variable is defined an access to ${env.<param>} is still possible within the pipe without an exception
		// because its value is 'null'. But accessing ${<param>} would throw an exception.
		// The below definitions are therefore not needed, but left in for an easy assignemt of value for test-purposes.

		BSF = "${env.BSF}"
		SCP = "${env.SCP}"
		SEPP = "${env.SEPP}"

        NLF = "${env.NLF}"
		SLF = "${env.SLF}"
		RLF = "${env.RLF}"
		WCDB = "${env.BSF}"	// WCDB only needed for BSF
		BSF_DIAMETER = "${env.BSF}"

		BSF_TLS = false
		SCP_TLS = false

		CHFSIM = "${env.CHFSIM}"
		NRFSIM = "${env.NRFSIM}"
		SEPPSIM = "${env.SEPPSIM}"
		DSCLOAD = "${env.DSCLOAD}"

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

		TAPAGENT = true

		CHART_NAME = "${CHART_NAME}"
		CHART_REPO = "${CHART_REPO}"
		//CHART_REPO = "https://armdocker.rnd.ericsson.se/artifactory/proj-5g-bsf-helm"
		//CHART_VERSION = "${CHART_VERSION}"   the chart version is always injected by Spinnaker or Jenkins, no need to define it here
		KUBE_HOST = "${KUBE_HOST}"
		NIGHTLY = "${NIGHTLY}"
		NAMESPACE = "${NAMESPACE}"
		PACKAGING = "${PACKAGING}"
		//COMMITTER = "${COMMITTER}"
		PROJECT = "${PROJECT}"
		PATH_REQYAML = "esc/helm/eric-sc-umbrella"
	}

	stages {

		stage('Print env vars') {
			steps {
				sh 'printenv | sort'
				script{
					// set displayed build name to "BUILD_NUMBER - COMMIT"
					currentBuild.displayName = "#${BUILD_NUMBER} - ${env.COMMIT}"
					// set displayed description to "NODE_NAME"
					currentBuild.description = "$NODE_NAME";
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
 				echo "BRANCH: ${env.BRANCH}"
				echo "COMMIT: ${env.COMMIT}"
				echo "CHART NAME: ${env.CHART_NAME}"
				echo "CHART REPO: ${env.CHART_REPO}"
				echo "CHART VERSION: ${env.CHART_VERSION}"
				echo "KUBE HOST: ${env.KUBE_HOST}"
				echo "NAMESPACE: ${env.NAMESPACE}"
				echo "NIGHTLY: ${env.NIGHTLY}"

				echo "The following will be deployed:\nBSF : ${env.BSF}\nNLF : ${env.NLF}\nSCP : ${env.SCP}\nSEPP: ${env.SEPP}\nSLF : ${env.SLF}\nRLF : ${env.RLF}\nWCDB: ${env.WCDB}\nBSF_DIAMETER: ${env.BSF_DIAMETER}" 

				script {
					// print settings for load activity
					if (env.TEST_TYPE == 'Stability' || env.TEST_TYPE == 'Robustness' || env.TEST_TYPE == 'Load') {
						echo "\n\nCHFsim: ${env.CHFSIM}\nNRFsim: ${env.NRFSIM}\nSEPPsim: ${env.SEPPSIM}\nDSCload: ${env.DSCLOAD}"

						//influxDB is only deployed for Load-test, and its usage is set in the verification-pipeline by updating the loadtestxxx.yaml file (test-repo)

					} // if
					
					PYTHON_CMD = "${DOCKER_RUN} ${BUILDER_DOCKER_VOLUMES} -w ${env.WORKSPACE} ${BUILDER_DOCKER_ENV} ${BUILDER_DOCKER_IMAGE} python "
					HELM_CMD = "${DOCKER_RUN} ${BUILDER_DOCKER_VOLUMES} ${BUILDER_DOCKER_ENV} ${BUILDER_DOCKER_IMAGE} helm"
				} // script
			} // steps
		} // stage

		stage('Init bob') {
			steps {
				echo pwd()
				sh 'ls -ltrh'
				sh 'git submodule update --init --recursive'
				sh './bob/bob base.init:create-output-dir'
				sh './bob/bob base.init:set-kube-config'
				sh './bob/bob base.init:set-build-proxy'
				sh './bob/bob base.init:set-namespace'
				sh './bob/bob base.init:set-supreme-properties'
				sh './bob/bob base.init:api-tokens'
				sh './bob/bob init:set-ingressHost'

				sh """#!/bin/bash
					if [ '${env.CHART_NAME}' = 'eric-sc' ];
					then 
						if [ '${env.CHART_VERSION}' = 'null' ];
						then
							tac ${env.PATH_REQYAML}/requirements.yaml | grep -B2 'name: eric-scp' | grep -m1 'version' | cut -d ' ' -f4 > ./.bob/var.esc-version;
							export CHART_VERSION=`cat ./.bob/var.esc-version`
							echo "the esc version is set to : `cat ./.bob/var.esc-version` !"
						else
							[[ "${env.BSF}" == "true" ]] && echo ${env.CHART_VERSION} > ./.bob/var.bsf-version;
							[[ "${env.BSF}" == "true" ]] && echo ${env.CHART_VERSION} > ./.bob/var.bsf-diameter-version;
							[[ "${env.NLF}" == "true" ]] && echo ${env.CHART_VERSION} > ./.bob/var.nlf-version;
							[[ "${env.SCP}" == "true" ]] && echo ${env.CHART_VERSION} > ./.bob/var.scp-version;
							[[ "${env.SLF}" == "true" ]] && echo ${env.CHART_VERSION} > ./.bob/var.slf-version;
							[[ "${env.RLF}" == "true" ]] && echo ${env.CHART_VERSION} > ./.bob/var.rlf-version;
							[[ "${env.SEPP}" == "true" ]] && echo ${env.CHART_VERSION} > ./.bob/var.sepp-version;

							
							echo ${env.CHART_VERSION} > ./.bob/var.hcagent-version;
							echo ${env.CHART_VERSION} > ./.bob/var.monitor-version;
							echo ${env.CHART_VERSION} > ./.bob/var.certnotifier-version;
							echo ${env.CHART_VERSION} > ./.bob/var.esc-version;
							echo ${env.CHART_VERSION} > ./.bob/var.chfsim-version;
							echo ${env.CHART_VERSION} > ./.bob/var.nrfsim-version;
							echo ${env.CHART_VERSION} > ./.bob/var.seppsim-version;
						fi;
					else
						tac ${env.PATH_REQYAML}/requirements.yaml | grep -m1 'version' | cut -d ' ' -f4 > ./.bob/var.esc-version
						echo "the esc version is set to : `cat ./.bob/var.esc-version` !"
					fi;"""

				sh './bob/bob init:product-info'
				sh './bob/bob init:cxp-number'
				sh './bob/bob init:cxp-rev-number'
				sh """#!/bin/bash
					[[ "${env.BSF}" == "true" ]] && ./bob/bob init:bsf-cxc-number && ./bob/bob init:bsf-cxc-rev-number;
					[[ "${env.BSF}" == "true" ]] && ./bob/bob init:bsf-diameter-cxc-number && ./bob/bob init:bsf-diameter-cxc-rev-number;
					[[ "${env.NLF}" == "true" ]] && ./bob/bob init:nlf-cxc-number && ./bob/bob init:nlf-cxc-rev-number;
					[[ "${env.SCP}" == "true" ]] && ./bob/bob init:scp-cxc-number && ./bob/bob init:scp-cxc-rev-number;
					[[ "${env.SLF}" == "true" ]] && ./bob/bob init:slf-cxc-number && ./bob/bob init:slf-cxc-rev-number;
					[[ "${env.RLF}" == "true" ]] && ./bob/bob init:rlf-cxc-number && ./bob/bob init:rlf-cxc-rev-number;
					[[ "${env.SEPP}" == "true" ]] && ./bob/bob init:sepp-cxc-number && ./bob/bob init:sepp-cxc-rev-number;
					echo '';"""
					// echo is needed, otherwise the pipe fails

				sh './bob/bob init:hcagent-cxc-number; ./bob/bob init:hcagent-cxc-rev-number'
				sh './bob/bob init:monitor-cxc-number; ./bob/bob init:monitor-cxc-rev-number'
				//sh './bob/bob init:cleanup-devenv-values'
			} // steps
		} // stage

		stage('Update baseline') {
			steps {
				sh "./bob/bob update-umbrella:copy"
				sh """#!/bin/bash 
					./bob/bob update-umbrella:update-product-numbers;
					if [ '${env.CHART_NAME}' = 'eric-sc' ];
					then
						[[ "${env.BSF}" == "true" ]] && ./bob/bob update-umbrella:bsf && ./bob/bob update-umbrella:bsf-diameter;
						[[ "${env.NLF}" == "true" ]] && ./bob/bob update-umbrella:nlf;
						[[ "${env.SCP}" == "true" ]] && ./bob/bob update-umbrella:scp;
						[[ "${env.SLF}" == "true" ]] && ./bob/bob update-umbrella:slf;
						[[ "${env.RLF}" == "true" ]] && ./bob/bob update-umbrella:rlf;
						[[ "${env.SEPP}" == "true" ]] && ./bob/bob update-umbrella:sepp;
						
						./bob/bob update-umbrella:hcagent;
						./bob/bob update-umbrella:monitor;
						
					else
						./bob/bob update-umbrella-generic:service;
					fi;
					"""
			} // steps
		} // stage

		stage('Create baseline package') {
			steps {
				sh "./bob/bob package-umbrella-fast"
			}
		} // stage
		stage('Check if node deployed'){
			steps {
				script{
					NODE_DEPLOYED = (sh(returnStatus: true, script: '(helm list --namespace $NAMESPACE --kubeconfig ~/.kube/$KUBE_HOST.config | grep eric-sc-umbrella)'))
				}
				echo "Node_deployed: ${NODE_DEPLOYED}"
				script{
					NODE_K6 = (sh(returnStatus: true, script: '(helm list --namespace $NAMESPACE --kubeconfig ~/.kube/$KUBE_HOST.config | grep eric-k6)'))
				}
				echo "Node_K6: ${NODE_K6}"
				script{
					NODE_atmoz_sftp = (sh(returnStatus: true, script: '(helm list --namespace $NAMESPACE --kubeconfig ~/.kube/$KUBE_HOST.config | grep eric-atmoz)'))
				}
				echo "Node_atmoz_sftp: ${NODE_atmoz_sftp}"
				script{
					NODE_nels_simulator = (sh(returnStatus: true, script: '(helm list --namespace $NAMESPACE --kubeconfig ~/.kube/$KUBE_HOST.config | grep eric-test-nels)'))
				}
				echo "Node_nels: ${NODE_nels_simulator}"
 			} // steps
		} // stage

		stage('Update WCDB config') {
			when {
				allOf {
					environment name: 'TEST_TYPE', value: 'Stability'
					environment name: 'BSF', value: 'true'
				} // allOf
			} // when
			steps {
				echo 'Updating Cassandra configuration to use full resources'
				sh """
					YamlFile='scripts/helm_config_gen/templates/values-kaas-full-diff.yaml';
					yq -i eval '.eric-data-wide-column-database-cd.resources.cassandra."*".cpu = "4"' \${YamlFile};
					yq -i eval '.eric-data-wide-column-database-cd.resources.cassandra."*".memory = "8Gi"' \${YamlFile};
					yq -i eval '.eric-data-wide-column-database-cd.persistence.dataVolume.persistentVolumeClaim.size = "40Gi"' \${YamlFile};
					"""
			} // steps
		} // stage

		stage('Enable tapagent side-container') {
			steps {
				echo 'Configure parameters to deploy tapagent side-container'
				sh """
					ResourcesYamlFile='scripts/helm_config_gen/templates/values-kaas-full-diff.yaml';
					TapAgentYamlFile='scripts/helm_config_gen/templates/tapagent.yaml';
					yq eval-all --inplace 'select(fileIndex == 0) * select(fileIndex == 1)' \${ResourcesYamlFile} \${TapAgentYamlFile};
					"""
			} // steps
		} // stage	

		stage("Install tapagent configuration") {
			steps {
				sh """#!/bin/bash
					./bob/bob config-tapagent:sftp-secret && ./bob/bob config-tapagent:configmaps;
					"""
			} // steps
		} // stage

		stage('Install Baseline') {
			when {expression { NODE_DEPLOYED != 0 }
			}
			steps {
				echo 'Starting to install...'
				sh "./bob/bob deploy"
			} // steps
		} // stage

		stage('Upgrade Baseline') {
			when {expression { NODE_DEPLOYED == 0 }
			}
			steps {
				echo 'Starting to upgrade...'
				sh "./bob/bob upgrade"
			} // steps
		} // stage

		// stage("Wait for deployed resources") {
		// 	steps {
		// 		sh """./bob/bob wait-for-deployed-resources:services;
		// 			sleep 60;"""
		// 	} // steps
		// } // stage

		stage("Show deployed pods") {
			steps {
				sh "kubectl get pods -n \$(cat .bob/var.namespace) --kubeconfig ${env.OUTPUT_DIR}/${env.KUBE_HOST}.admin.conf;"
			} // steps
		} // stage

		stage("Show deployed services") {
			steps {
				sh "kubectl get svc -n \$(cat .bob/var.namespace) --kubeconfig ${env.OUTPUT_DIR}/${env.KUBE_HOST}.admin.conf;"
			} // steps
		} // stage

		stage("Install Default Users") {
			when {expression { NODE_DEPLOYED != 0 }
			}
			steps {
				sh """ ./bob/bob install-default-users:default-users;
					#./bob/bob install-default-users-ci:no-pwdReset;
					sleep 60;"""
			} // steps
		} // stage

		stage("Install Default Certificates") {
			when {expression { NODE_DEPLOYED != 0 }
			}
			steps {
				sh """#!/bin/bash
					./bob/bob install-certificates:rootca;
					./bob/bob install-certificates:nbi;
					[[ "${env.NLF}" == "true" ]] && ./bob/bob install-certificates:nlf;
					[[ "${env.SCP}" == "true" ]] && ./bob/bob install-certificates:scp-manager && ./bob/bob install-certificates:scp-worker;
					[[ "${env.SLF}" == "true" ]] && ./bob/bob install-certificates:slf;
					[[ "${env.SEPP}" == "true" ]] && ./bob/bob install-certificates:sepp-manager && ./bob/bob install-certificates:sepp-worker;
					sleep 60;"""
//					[[ "${env.BSF}" == "true" ]] && ./bob/bob install-certificates:bsf && ./bob/bob install-certificates:diameter && ./bob/bob install-certificates:dscload;
			} // steps
		} // stage

		stage ("Install Default Configuration") {
			steps {
				sh """#!/bin/bash
					./bob/bob loadConfig:sc
					./bob/bob wait-for-deployed-resources:selectedApps;
					"""
					// For IPv6 SEPP has been set to false in the beginning
			} // steps
		} // stage

//		obsolete: eric-sc-monitor.spec.replicaCount=1 set in ruleset.
//		stage ("Scale eric-sc-monitor") {
//			steps {
//				sh """#!/bin/bash
//					kubectl --kubeconfig ${env.OUTPUT_DIR}/${env.KUBE_HOST}.admin.conf -n \$(cat .bob/var.namespace) get pods | grep monitor ;
//					kubectl --kubeconfig ${env.OUTPUT_DIR}/${env.KUBE_HOST}.admin.conf -n \$(cat .bob/var.namespace) scale deployments eric-sc-monitor --replicas=1;
//					sleep 15s;
//					kubectl --kubeconfig ${env.OUTPUT_DIR}/${env.KUBE_HOST}.admin.conf -n \$(cat .bob/var.namespace) get pods | grep monitor ;
//				"""
//			}
//		}

		stage("Install NeLS simulator") {
			when {
				allOf {
					expression { NODE_nels_simulator != 0 }
					allOf{
						not {
							environment name: 'TEST_TYPE', value: 'Stability'
						}
						not {
							environment name: 'TEST_TYPE', value: 'AAT'
						}
					}
				} // allOf
			} // when
			steps {
				sh "./bob/bob -r rulesets/ruleset2.0-eric-nels.yaml init:api-tokens create-nels-dir get-nels deploy"
			} // steps
		} // stage

		stage("Install SFTP atmoz") {
			when {
				allOf {
					expression { NODE_atmoz_sftp != 0 }
     				not {
						environment name: 'TEST_TYPE', value: 'AAT'
					}
				} //allOf
			} // when
			steps {
				sh "./bob/bob -r rulesets/ruleset2.0-eric-sftp.yaml init:sftp-version init:api-tokens image package push deploy;"
			 } // steps
 		} // stage
		
		stage("Install k6") {
			when {
				allOf {
					expression { NODE_K6 != 0 }
     				not {
						environment name: 'TEST_TYPE', value: 'AAT'
					}
				} //allOf
			} // when
			steps {
				// the certificates are installed before packaged into a docker image
				// init:create-output-dir done already in stage 'Init bob', rule init:set-build-proxy
				sh """./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml install-certs;
					./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml init:k6-version;
					./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml image:create-certs-dir;
					./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml image:build-k6;
					./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml package:helm-chart-k6;
					./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml push:docker-image-k6;
					./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml push:helm-chart-k6;
				    ./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml deploy:deploy-k6;"""
				// The default of 4 replica will be deployed. The number of replica will be scaled-in/out in the verification pipeleine
				// according to the selected stability suite.
				// deploy-k6:	4 replica (default)
				// bsf needs:	1 replica
				// scp needs:	9 replica
				// ci  needs:	2 replica
			} // steps
		} // stage

		stage("Install dscload") {
			when {
				allOf {
					environment name: 'TEST_TYPE', value: 'Stability'
					environment name: 'DSCLOAD', value: 'true'
				} // allOf
			} // when
			steps {
				sh """./bob/bob -r rulesets/ruleset2.0-eric-dscload.yaml init:dscload-version;
					//./bob/bob -r rulesets/ruleset2.0-eric-dscload.yaml init:set-artifactory-token
					./bob/bob -r rulesets/ruleset2.0-eric-dscload.yaml image:build-dscload;
					./bob/bob -r rulesets/ruleset2.0-eric-dscload.yaml package:helm-chart-dscload;
					./bob/bob -r rulesets/ruleset2.0-eric-dscload.yaml push:docker-image-dscload;
					./bob/bob -r rulesets/ruleset2.0-eric-dscload.yaml package:helm-chart-dscload;
				    ./bob/bob -r rulesets/ruleset2.0-eric-dscload.yaml deploy:deploy-dscload;"""
			} // steps
		} // stage

		stage("Install influxDB") {
			when {
				environment name: 'TEST_TYPE', value: 'Stability'
			} // when
			steps {
				sh """./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml init:influxdb-version;
					./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml image:build-influxdb;
					./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml package:helm-chart-influxdb;
					./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml push:docker-image-influxdb;
					./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml push:helm-chart-influxdb;
				    ./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml deploy:deploy-influxdb;"""
			} // steps
		} // stage

		stage("Install CHFsim") {
			when {
				allOf {
					environment name: 'TEST_TYPE', value: 'Stability'
					environment name: 'CHFSIM', value: 'true'
				} // allOf
			} // when
			steps {
				sh "./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml download:download-chfsim;"				
				sh "cp eric-chfsim-* .bob/. "
				sh """./bob/bob -r ruleset2.0-eric-esc.yaml install-certificates:chfsim;
					./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml deploy:deploy-chfsim;"""
			} // steps
		} // stage

		stage("Install NRFsim") {
			// Stability test: CSA, SCP, SCP+SLF+RLF, SEPP
			when {
				allOf {
					environment name: 'TEST_TYPE', value: 'Stability';
					environment name: 'NRFSIM', value: 'true'
				} // allOf
			} // when
			steps {				
				sh "./bob/bob -r rulesets/ruleset2.0-eric-nrfsim.yaml download:download-nrfsim;"				
				sh "cp eric-nrfsim-* .bob/. "
				sh """./bob/bob -r ruleset2.0-eric-esc.yaml install-certificates:nrfsim;
					./bob/bob -r rulesets/ruleset2.0-eric-nrfsim.yaml deploy;"""
			} // steps
		} // stage

		stage("Install SEPPsim") {
			// Stability test: CSA or SCP
			when {
				allOf {
					environment name: 'TEST_TYPE', value: 'Stability';
					environment name: 'SEPPSIM', value: 'true'
				} // allOf
			} // when
			steps {
                sh "./bob/bob -r rulesets/ruleset2.0-eric-seppsim.yaml download:download-seppsim;"
				sh "cp eric-seppsim-* .bob/. "
				sh """./bob/bob -r ruleset2.0-eric-esc.yaml install-certificates:seppsim;
					./bob/bob -r rulesets/ruleset2.0-eric-seppsim.yaml deploy;"""
			} // steps
		} // stage

		stage("Archiving parameters for downstream Test pipeline") {
			steps {
				sh 'echo "BRANCH=$(echo ${BRANCH})" > test.properties'
				sh 'echo "KUBE_HOST=$(echo ${KUBE_HOST})" >> test.properties'
				sh """echo "NAMESPACE=`cat ${env.OUTPUT_DIR}/var.namespace`" >> test.properties;"""
				sh """echo "VERSION=`cat ${env.OUTPUT_DIR}/var.esc-version`" >> test.properties;"""
				sh """echo "COMMIT=`echo ${env.COMMIT}`" >> test.properties;"""
				sh """echo "CHART_VERSION=`cat ${env.OUTPUT_DIR}/var.esc-version`" >> test.properties;"""
				//sh 'echo "COMMITTER=$(echo ${COMMITTER})" >> test.properties;'
				sh 'echo "PROJECT=$(echo ${PROJECT})" >> test.properties;'
				archiveArtifacts artifacts: 'test.properties', onlyIfSuccessful: true
			} // steps
		} // stage
	} // stages

	post{
		failure{

			sh "./scripts/collect_ADP_logs.sh -c ~/.kube/${env.KUBE_HOST}.config -n `cat .bob/var.namespace`"
			archiveArtifacts allowEmptyArchive: true, artifacts: 'logs_*.tgz', onlyIfSuccessful: false

			script {
				// do not undeploy for troubleshooting purposes
				if ((env.CLEANCLUSTER != 'FALSE') || ("$TEST_TYPE".contains("SmokeTest"))) {
					try {
						timeout(time: 600000, unit: 'MILLISECONDS') {
							// clean cluster due to failure in pipeline
							sh "/home/eiffelesc/scripts/cleanCluster `cat .bob/var.namespace` ${env.KUBE_HOST}"
						} // timeout
					} catch (exc) {
						sh """kubectl delete ns \$(cat .bob/var.namespace) --kubeconfig ${env.OUTPUT_DIR}/${env.KUBE_HOST}.admin.conf;
							kubectl create ns \$(cat .bob/var.namespace) --kubeconfig ${env.OUTPUT_DIR}/${env.KUBE_HOST}.admin.conf;"""
					} // catch
				} // if

				// only send mail for smoketest
				if ("$TEST_TYPE".contains("SmokeTest")) {
					COLOR = "<body style=\"background-color: LightYellow;\">"

					emailext body: "$COLOR" + "Hi ${env.COMMITTER},<p> the deploy failed, maybe due to your commit? <p> Please check:<br>${env.BUILD_URL}<br><br>Thank you for pushing.<br><br>BR,<br>A-Team",

					mimeType: 'text/html',
					subject: "5G Smoke-test deploy failed for ${COMMIT}",
					to: "${env.EMAIL_COMMITTER}, DSCAteam@ericsson.onmicrosoft.com, f37b6763.ericsson.onmicrosoft.com@emea.teams.ms"
				}  // if
			} // script
		} // failure
	} // post
} // pipeline
