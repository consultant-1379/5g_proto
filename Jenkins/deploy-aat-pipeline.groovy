
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

	stage('set agent'){

		if (env.IP_VERSION == '6') {	// move to IPv6 buildslave(s)
		 	AGENT_LABEL = '5G-SC-IPv6'
		} // if
		else {							// move to IPv4 buildslave(s)
	 		AGENT_LABEL = '5G-SC'		// in the future
		} // else

	} // stage

} // node
//////////////////////////////////////////////////////////////////////////

def values
def email_buildUrl="Jcat logs:<br>${env.BUILD_URL}"

pipeline {

	agent { label "${AGENT_LABEL}"   } // set label according to IP_VERSION parameter

	options{
		//timeout(time: 30, unit: 'MINUTES')
		timestamps()
    } // options

	environment {
		// Variables needed for the ruleset
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

		CHART_NAME = "${CHART_NAME}"
		CHART_REPO = "${CHART_REPO}"
		//CHART_REPO = "https://armdocker.rnd.ericsson.se/artifactory/proj-5g-bsf-helm"
		//CHART_VERSION = "${CHART_VERSION}"   the chart version is always injected by Spinnaker or Jenkins, no need to define it here
		KUBE_HOST = "${KUBE_HOST}"
		ARTIFACTORY_TOKEN = "${ARTIFACTORY_TOKEN}"
		NIGHTLY = "${NIGHTLY}"
		NAMESPACE = "${NAMESPACE}"
		PACKAGING = "${PACKAGING}"
		RELEASE = "${RELEASE}"
		//COMMITTER = "${COMMITTER}"
		PROJECT = "${PROJECT}"
		TEST_BRANCH = "${env.TEST_BRANCH}"
	}

	stages {
		stage('Freeing up workspace on other buildslaves') {
			steps {
				sh ''' /bin/bash -x 
					IP_ADDRESS_TMP=`hostname -i`;
					BUILD_PATH_TMP=`pwd`;
					for ip in `seq -f "10.210.174.%g" 60 62`;
					do
						if [ "${IP_ADDRESS_TMP}" != "${ip}" ];
						then
							ssh eiffelesc@${ip} "if [ -d ${BUILD_PATH_TMP} ]; then sudo rm -rf ${BUILD_PATH_TMP}/; else echo \"Nothing to clean\"; fi;"
						fi;
					done;'''
			} // steps
		} // stage

		stage('Initialize') {
			steps {
 				echo "BRANCH: ${env.BRANCH}"
				echo "COMMIT: ${env.COMMIT}"
				echo "CHART NAME: ${env.CHART_NAME}"
				echo "CHART REPO: ${env.CHART_REPO}"
				echo "CHART VERSION: ${env.CHART_VERSION}"
				echo "ARTIFACTORY TOKEN: ${env.ARTIFACTORY_TOKEN}"
				echo "KUBE HOST: ${env.KUBE_HOST}"
				echo "NAMESPACE: ${env.NAMESPACE}"
				echo "NIGHTLY: ${env.NIGHTLY}"
				echo "TEST BRANCH: ${env.TEST_BRANCH}"
				echo "PACKAGING: ${env.PACKAGING}"
				echo "RELEASE: ${env.RELEASE}"

				sh 'git submodule update --init --recursive'
				sh './bob/bob base.init:set-kube-config'
				sh './bob/bob base.init:set-namespace'

				script {
					// set the displayed build name to "BUILD_NUMBER - COMMIT"
					currentBuild.displayName = "#${BUILD_NUMBER} - ${env.COMMIT}"
				} // script
			} // steps
		} // stage

		stage('Clone test repository') {
			steps{
				echo 'Checking user'
				sh 'whoami'
				echo 'check path'
				echo pwd()
				sh 'git submodule update --init --recursive'
				sh './bob/bob base.init:set-build-proxy'
				sh './bob/bob build-libs-ci'

				echo 'Creating test repo directory'
				dir('5G_AAT') {
					deleteDir()
				}
				echo pwd()
				echo 'Trying to clone the SC AAT repository'
				script{
					checkout scmGit(
						branches: [[name: '*/${TEST_BRANCH}']],
						userRemoteConfigs: [[credentialsId: 'eiffelesc-user-password', name: 'origin', url: 'https://eiffelesc@gerritmirror-direct.sero.gic.ericsson.se/a/MC_5G/5G_AAT']],
						extensions: [submodule(parentCredentials: true, recursiveSubmodules: true, reference: '', shallow: false), [$class: 'RelativeTargetDirectory', relativeTargetDir: '5G_AAT']])
				} //script
				echo 'check content'
				dir('5G_AAT') {
					echo pwd()
					sh 'git log -n 10'
					sh 'ls -ltrh'
					sh 'git submodule update --init --recursive'
				} //dir
			} //steps
		} //stage

		stage('Init AAT') {
			steps {
				dir('5G_AAT/') {
					sh """./bob/bob -r ruleset2.0-eric-aat.yaml init:set-build-proxy;
						./bob/bob -r ruleset2.0-eric-aat.yaml init:version init:aat-cxu-number init:aat-cxu-rev-number;
						./bob/bob -r ruleset2.0-eric-aat.yaml init:set-kube-config;
						./bob/bob -r ruleset2.0-eric-aat.yaml init:sync-libs-dev;
						./bob/bob -r ruleset2.0-eric-aat.yaml init:set-base-image;
						cp -f ./scripts/install_certs.sh ../scripts/install_certs.sh;
						../scripts/install_certs.sh bsfload;
						./bob/bob -r ruleset2.0-eric-aat.yaml init:copy-docker-config;
						./bob/bob -r ruleset2.0-eric-aat.yaml init:api-tokens"""
				}
				
			}
		}

		stage('Check if AAT deployed'){
			steps {
				dir('5G_AAT/') {
					script{
						AAT_CORE_DEPLOYED = (sh(returnStatus: true, script: '(helm list --namespace ${NAMESPACE} --kubeconfig ~/.kube/$KUBE_HOST.config | grep aat-core)'))
					}
					echo "AAT_CORE_DEPLOYED: ${AAT_CORE_DEPLOYED}"
					script{
						AAT_TS_DEPLOYED = (sh(returnStatus: true, script: '(helm list --namespace ${NAMESPACE} --kubeconfig ~/.kube/$KUBE_HOST.config | grep aat-ts)'))
					}
					echo "AAT_TS_DEPLOYED: ${AAT_TS_DEPLOYED}"
				}
 			} //steps
		} //stage	

		stage('Build and install AAT') {
		    when {expression { AAT_CORE_DEPLOYED != 0 || AAT_TS_DEPLOYED != 0}
			}
			steps {
				echo 'Starting to build and install...'
				dir('5G_AAT/') {

					sh "./bob/bob -r ruleset2.0-eric-aat.yaml deploy:create-secret deploy:aat-core deploy:aat-core-ca-bundle deploy:create-aat-keystore deploy:create-mtls-secret"

					
						sh "./bob/bob -r ruleset2.0-eric-aat.yaml update-umbrella:copy build:local update-helm:aat package-helm:aat"
					
					withCredentials([string(credentialsId: 'c9bf8e3d-476c-4be9-b26a-73d0275975bd', variable: 'ARTIFACTORY_TOKEN')]) {
					    sh "./bob/bob -r ruleset2.0-eric-aat.yaml push-helm:aat"
					}

					sh "./bob/bob -r ruleset2.0-eric-aat.yaml fetch-jcat update-umbrella:aat package-umbrella-fast deploy:values-generation deploy:aat"
				}
			}
		} // stage
		
		stage("Wait for deployed resources") {
			steps {
				sh """./bob/bob wait-for-deployed-resources:services;
				      sleep 60;"""
			} // steps
		} // stage

		stage("Show deployed pods") {
			steps {
				sh "kubectl get pods -n \${NAMESPACE} --kubeconfig ~/.kube/\$KUBE_HOST.config;"
			}  // steps
		} // stage

		stage("Show deployed services") {
			steps {
				sh "kubectl get svc -n \${NAMESPACE} --kubeconfig ~/.kube/\$KUBE_HOST.config;"
			}  // steps
		} // stage

		// stage ("Load SCP Configuration") {
		// 	steps {
		// 		dir('5G_AAT') {
		// 			sh """#!/bin/bash
		// 				./bob/bob -r ruleset2.0-eric-aat.yaml loadconfig:scp-AAT
		// 				"""
		// 		}
		// 	} // steps
		// } // stage

		stage("Archiving parameters for downstream Test pipeline") {
			steps {
				sh 'echo "KUBE_HOST=$(echo ${KUBE_HOST})" > test.properties'
				sh 'echo "NAMESPACE=$(echo ${NAMESPACE})" >> test.properties;'
				sh """echo "VERSION=`cat \${env.OUTPUT_DIR}/var.esc-version`" >> test.properties;"""
				sh """echo "COMMIT=`echo \${env.COMMIT}`" >> test.properties;"""
				sh """echo "CHART_VERSION=`cat \${env.OUTPUT_DIR}/var.esc-version`" >> test.properties;"""
				//sh 'echo "COMMITTER=$(echo ${COMMITTER})" >> test.properties;'
				sh 'echo "PROJECT=$(echo ${PROJECT})" >> test.properties;'
				archiveArtifacts artifacts: 'test.properties', onlyIfSuccessful: true
			} // steps
		} // stage
	} // stages

	post{
		failure{

			sh "./scripts/collect_ADP_logs.sh -c ~/.kube/${env.KUBE_HOST}.config -n \${NAMESPACE}"
			archiveArtifacts allowEmptyArchive: true, artifacts: 'logs_*.tgz', onlyIfSuccessful: false

			script {
                                // do not undeply for troubleshooting purposes
                               if ((env.CLEANCLUSTER != 'FALSE')){
                                        try {
                                                timeout(time: 600000, unit: 'MILLISECONDS') {
                                                        // clean cluster due to failure in pipeline
                                                       sh "/home/eiffelesc/scripts/cleanCluster \${NAMESPACE} ${env.KUBE_HOST}"
                                                } // timeout
                                        } catch (exc) {
                                               sh """kubectl delete ns \${NAMESPACE} --kubeconfig ${env.OUTPUT_DIR}/${env.KUBE_HOST}.admin.conf;
                                                         kubectl create ns \${NAMESPACE} --kubeconfig ${env.OUTPUT_DIR}/${env.KUBE_HOST}.admin.conf;"""
                                        } // catch
                                } // if				// only send mail for smoketest

				def COLOR = "<body style=\"background-color: LightYellow;\">"
				echo "$COLOR"
				emailext body: "$COLOR"+"Hello AAT team,<br><br> AAT test suite results for ${BRANCH} ${CHART_VERSION}.<br>Please check: ${email_buildUrl} <br><br> Deploy AAT pipeline has <b><font color=\"#FF0000\"> Failed </font></b>",
				mimeType: 'text/html',
				subject: "AAT test suite results ${BRANCH} ${CHART_VERSION}",
				to: "c91010d0.ericsson.onmicrosoft.com@emea.teams.ms"
			
			} // script
		} // failure
	} // post
} // pipeline
