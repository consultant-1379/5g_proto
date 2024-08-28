//////////////////////////////////////////////////////////////////////////
//
// first run on SERO_GIC buildslaves, decide which buildslave(s) to use


node('SERO_GIC') {

	stage('Check pre-conditions'){

		// parameter-value is 'null' if no Jenkins-parameter exists:
		// printing parameters in order to see which have value 'null'
		echo "env.BSF:  ${env.BSF}\nenv.NLF:  ${env.NLF}\nenv.SCP:  ${env.SCP}\nenv.SEPP: ${env.SEPP}\nenv.SLF:  ${env.SLF}\nenv.RLF:  ${env.RLF}"
		echo "env.CHFSIM:  ${env.CHFSIM}\nenv.NRFSIM:  ${env.NRFSIM}\nenv.SEPPSIM: ${env.SEPPSIM}\nenv.PVTB:  ${env.PVTB}\nwith DTLS: ${env.PVTB_DTLS}"

        // Applications checks:
        // if no Jenkins-parameters exists or all set to false the default will be to deploy all
        if (env.BSF != 'true' && env.SCP != 'true' && env.SEPP != 'true') {
            echo('no application selected; BSF, SCP, SEPP, SLF and RLF will be deployed!')
            BSF = true;
            NLF = true;
            SCP = true;
            SEPP = true;
            SLF = true;
            RLF = true;
        } // if

        //if (( env.SLF == 'true' || env.RLF == 'true' ) && env.SCP != 'true') {
        //    error('SLF and RLF require SCP to be deployed as well, build aborted!')
        //} // if

        //if (env.IP_VERSION == '6' && env.SEPP == 'true') {
        //    error('SEPP is not working on IPv6, build aborted!')
        //}
		
        if ( "${env.SCP}" == 'true' || "${SCP}" == 'true' || "${env.SEPP}" == 'true' || "${SEPP}" == 'true' || "${env.BSF}" == 'true' || "${BSF}" == 'true'){
            PVTB = true;
            TAPCOLLECTOR = true;
            VTAPRECORDER = true;
        }

        // DSCload tool is needed for BSF for all test types
        // for CI DSCLOAD is not known and must be set
        if (env.DSCLOAD != 'true' && (env.BSF == 'true' || BSF == true)){
            DSCLOAD = true;
        }

	}// stage

	stage('Set agent'){
		if (env.AGENT_LABEL != null && env.AGENT_LABEL != '') {
			AGENT_LABEL = "${env.AGENT_LABEL}"
		} else {
			if (env.IP_VERSION.contains('6')) {	// move to IPv6 buildslave(s)
				AGENT_LABEL = '5G-SC-IPv6'
			} else	{				// move to IPv4 buildslave(s)
				AGENT_LABEL = '5G-SC'
			} //if
		} //if
	} // stage

} // node
//////////////////////////////////////////////////////////////////////////

def HELM_CMD
def GIT_CMD
def PYTHON_CMD
def values

@Library('Shared-Libs-SC') _

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

		BSF_LEADER_ELECTION = true
		SCP_LEADER_ELECTION = true
		SEPP_LEADER_ELECTION = true

		NLF = "${env.NLF}"
		SLF = "${env.SLF}"
		RLF = "${env.RLF}"
		WCDB = "${env.BSF}"    // WCDB only needed for BSF
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
		TAPCOLLECTOR = "${TAPCOLLECTOR}"
		PVTB="${PVTB}"
		VTAPRECORDER="${VTAPRECORDER}"

		CHART_NAME = "${CHART_NAME}"
		CHART_REPO = "${CHART_REPO}"
		//CHART_REPO = "https://armdocker.rnd.ericsson.se/artifactory/proj-5g-bsf-helm"
		//CHART_VERSION = "${CHART_VERSION}"   the chart version is always injected by Spinnaker or Jenkins, no need to define it here
		KUBE_HOST = "${KUBE_HOST}"
		//NIGHTLY = "${NIGHTLY}"
		NAMESPACE = "${NAMESPACE}"
		PACKAGING = "${PACKAGING}"
		//COMMITTER = "${COMMITTER}"
		PROJECT = "${PROJECT}"
		PATH_REQYAML = "esc/helm/eric-sc-umbrella"
	}

	stages {

		stage('Set/Print env vars') {
			steps {
				script{
					// If not defined, set IP_VERSION_INT and IP_VERSION_EXT according IP_VERSION 4 or 6 or DS
					if (env.IP_VERSION_INT == null && env.IP_VERSION_EXT == null && env.IP_VERSION != null) {
						if (env.IP_VERSION == '4' || env.IP_VERSION == '6' || env.IP_VERSION == 'DS') {
							env.IP_VERSION_INT = "${env.IP_VERSION}"
							env.IP_VERSION_EXT = "${env.IP_VERSION}"
						}
						else {
							error('No such IP_VERSION(4, 6, DS): ${env.IP_VERSION}, build aborted!')
						}
					}
				} //script
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

		stage('Cleaning WS on Buildcomputes'){
			// only if no other instance of the same job is running in parallel
			// if another instance is running this procedure would delete its WS and make it failing
			when {
				expression { currentBuild.getPreviousBuildInProgress() == null && !(env.NODE_NAME =~ /5G-LMVL/) }
			}
			steps{
				catchError(message: "Timeout while cleaning WS on Buildcomputes", buildResult: 'SUCCESS', stageResult: 'FAILURE') {
				// remove all (!) content (including the git repo)
		
					script {
						buildslaves.cleaningWS("${AGENT_LABEL}")
					} //script
				} //catchError
			} //steps
		} //stage

		stage('Initialize') {
			steps {
				echo ("BRANCH: $BRANCH \n"+
					"COMMIT: $COMMIT \n"+
					"CHART NAME: $CHART_NAME \n"+
					"CHART REPO: $CHART_REPO \n"+
					"CHART VERSION: $CHART_VERSION \n"+
					"KUBE HOST: $KUBE_HOST \n"+
					"NAMESPACE: $NAMESPACE\n"+
					"\n"+
					"The following will be deployed:\n"+
					"BSF : $BSF \n"+
					"NLF : $NLF \n"+
					"SCP : $SCP \n"+
					"SEPP: $SEPP \n"+
					"SLF : $SLF \n"+
					"RLF : $RLF \n"+
					"WCDB: $WCDB\n")

				script {
					// print settings for load activity
					if (env.TEST_TYPE == 'Stability' || env.TEST_TYPE == 'Robustness' || env.TEST_TYPE == 'Load') {
						echo ("\n\n"+
							"CHFsim: ${env.CHFSIM}\n"+
							"NRFsim: ${env.NRFSIM}\n"+
							"SEPPsim: ${env.SEPPSIM}\n"+
							"DSCload: ${env.DSCLOAD}")

						//influxDB is only deployed for Load-test, and its usage is set in the verification-pipeline by updating the loadtestxxx.yaml file (test-repo)
					} // if

					PYTHON_CMD = "${DOCKER_RUN} ${BUILDER_DOCKER_VOLUMES} -w ${env.WORKSPACE} ${BUILDER_DOCKER_ENV} ${BUILDER_DOCKER_IMAGE} python "
					HELM_CMD = "${DOCKER_RUN} ${BUILDER_DOCKER_VOLUMES} ${BUILDER_DOCKER_ENV} ${BUILDER_DOCKER_IMAGE} helm"
				} // script
			} // steps
		} // stage

		stage('Init bob') {
			steps {
				init_bob();
			} // steps
		} // stage

		stage('Update baseline') {
			 when {
				 expression { env.TEST_TYPE != 'IaaS_backup_restore' && env.TEST_TYPE != 'IaaS_Stability'&& env.TEST_TYPE != 'IaaS_Stability_tools_on_separate_workers'}
			} //when
			steps {
				sh "./bob/bob update-umbrella:copy"
				sh """#!/bin/bash -e
					./bob/bob update-umbrella:update-product-numbers;
					if [ '${env.CHART_NAME}' = 'eric-sc' ];
					then
						[[ "${env.BSF}" == "true" ]] && ./bob/bob update-umbrella:bsf && ./bob/bob update-umbrella:bsf-diameter;
						[[ "${env.NLF}" == "true" ]] && ./bob/bob update-umbrella:nlf;
						[[ "${env.SCP}" == "true" ]] && ./bob/bob update-umbrella:scp;
						[[ "${env.SLF}" == "true" ]] && ./bob/bob update-umbrella:slf;
						[[ "${env.RLF}" == "true" ]] && ./bob/bob update-umbrella:rlf;
						[[ "${env.SEPP}" == "true" ]] && ./bob/bob update-umbrella:sepp;

						./bob/bob update-umbrella:monitor;

					else
						./bob/bob update-umbrella-generic:service;
					fi;
					"""
			} // steps
		} // stage

		stage('Create baseline package') {
			 when {
				 expression { env.TEST_TYPE != 'IaaS_backup_restore' && env.TEST_TYPE != 'IaaS_Stability'&& env.TEST_TYPE != 'IaaS_Stability_tools_on_separate_workers'}
			} //when
			steps {
				sh "./bob/bob package-umbrella-fast"
			}
		} // stage
		stage('Check if node deployed'){
			steps {
				script{
					NODE_DEPLOYED = (sh(returnStatus: true, script: '(helm list --namespace $NAMESPACE --kubeconfig ~/.kube/$KUBE_HOST.config | grep eric-sc-umbrella)'))
					NODE_K6 = (sh(returnStatus: true, script: '(helm list --namespace $NAMESPACE --kubeconfig ~/.kube/$KUBE_HOST.config | grep eric-k6)'))
					NODE_atmoz_sftp = (sh(returnStatus: true, script: '(helm list --namespace $NAMESPACE --kubeconfig ~/.kube/$KUBE_HOST.config | grep eric-atmoz)'))
					NODE_nels_simulator = (sh(returnStatus: true, script: '(helm list --namespace $NAMESPACE --kubeconfig ~/.kube/$KUBE_HOST.config | grep eric-test-nels)'))
					NODE_vtap_recorder = (sh(returnStatus: true, script: '(helm list --namespace $NAMESPACE --kubeconfig ~/.kube/$KUBE_HOST.config | grep eric-vtaprecorder)'))
				}
				echo ("Node_deployed: $NODE_DEPLOYED \n"+
					"Node_K6: $NODE_K6 \n"+
					"Node_atmoz_sftp: $NODE_atmoz_sftp \n"+
					"Node_nels: $NODE_nels_simulator \n"+
					"Node_vtap_recorder: $NODE_vtap_recorder")
			 } // steps
		} // stage

		stage('Update WCDB config') {
			when {
				allOf {
					expression { env.TEST_TYPE == 'Stability' || env.TEST_TYPE == 'Robustness' || env.TEST_TYPE == 'IaaS_Stability' || env.TEST_TYPE == 'IaaS_Stability_tools_on_separate_workers' }
					environment name: 'BSF', value: 'true'
				} // allOf
			} // when
			steps {
				sh """
					echo 'Updating Cassandra configuration to use full resources';
					YamlFile='scripts/helm_config_gen/templates/values-kaas-full-diff.yaml';
					FullCassandraYamlFile='scripts/helm_config_gen/templates/fullCassandra.yaml';
					yq eval-all --inplace 'select(fileIndex == 0) * select(fileIndex == 1)' \${YamlFile} \${FullCassandraYamlFile};
					"""
			} // steps
		} // stage

		stage('Enable tapagent side-container') {
			when {
				expression { env.TEST_TYPE != 'IaaS_backup_restore' && env.TEST_TYPE != 'IaaS_Stability'&& env.TEST_TYPE != 'IaaS_Stability_tools_on_separate_workers'}
			} // when
			steps {
				sh """
					echo 'Configure parameters to deploy tapagent side-container';
					ResourcesYamlFile='scripts/helm_config_gen/templates/values-kaas-full-diff.yaml';
					TapAgentYamlFile='scripts/helm_config_gen/templates/tapagent.yaml';
					yq eval-all --inplace 'select(fileIndex == 0) * select(fileIndex == 1)' \${ResourcesYamlFile} \${TapAgentYamlFile};
					"""
			} // steps
		} // stage

		stage("Install tapagent configuration") {
			when {
				expression { env.TEST_TYPE != 'IaaS_backup_restore' && env.TEST_TYPE != 'IaaS_Stability'&& env.TEST_TYPE != 'IaaS_Stability_tools_on_separate_workers'}
			} // when
			steps {
				sh """#!/bin/bash -e
					./bob/bob config-tapagent:sftp-secret && ./bob/bob config-tapagent:configmaps;
					"""
			} // steps
		} // stage

		stage('Install Baseline') {
			environment {
				DIVISION_METHOD = "${env.DIVISION_METHOD}"
			}
			when {
				allOf {
					expression { NODE_DEPLOYED != 0 }
					expression { env.TEST_TYPE != 'IaaS_Stability'&& env.TEST_TYPE != 'IaaS_Stability_tools_on_separate_workers'&& env.TEST_TYPE != 'IaaS_backup_restore'}
				} // allOf
			} // when
			steps {
				sh "echo 'Creating day-0 certificates...'; ./bob/bob create-certificates:rootca create-certificates:netconf-tls"
				sh "echo 'Creating CERTM config json file for day-0 certificates...'; ./bob/bob deploy:create-certm-config"
				sh "echo 'Install day-0 secrets...'; ./bob/bob deploy:adp-secrets"

				sh "echo 'Fetching registry credentials...'; ./bob/bob install-regcred-automation:applyDefaultPullSecret"
				sh "echo 'Generate day-0 configuration...'; ./bob/bob generate-values"
				sh "echo 'Starting to install SC integration chart...'; ./bob/bob deploy:ihc"
				sh "echo 'Deploy httpproxy resources for local verification activities...'; ./bob/bob deploy:pm-httpproxy deploy:osmn-httpproxy deploy:envoy-admin-httpproxy deploy:search-engine-httpproxy"

			} // steps
		} // stage

		stage('Upgrade Baseline') {
			when {
				allOf {
					expression { NODE_DEPLOYED == 0 }
					expression { env.TEST_TYPE != 'IaaS_backup_restore' && env.TEST_TYPE != 'IaaS_Stability'&& env.TEST_TYPE != 'IaaS_Stability_tools_on_separate_workers'}
				} //allOf
			} // when
			steps {
				sh "echo 'Starting to upgrade...'; ./bob/bob upgrade"
				//sh "./bob/bob undeploy:pm-httpproxy undeploy:osmn-httpproxy undeploy:envoy-admin-httpproxy"
				//sh "./bob/bob deploy:pm-httpproxy deploy:osmn-httpproxy deploy:envoy-admin-httpproxy"
			} // steps
		} // stage

		stage("Wait for deployed resources") {
			when {
				expression { env.TEST_TYPE != 'IaaS_backup_restore' && env.TEST_TYPE != 'IaaS_Stability'&& env.TEST_TYPE != 'IaaS_Stability_tools_on_separate_workers'}
			} // when
			steps {
				sh """#!/bin/bash -e
					if [ '${env.PVTB_DTLS}' = 'true' ];
					then
						./bob/bob wait-for-deployed-resources:pvtbless;
					else
						./bob/bob wait-for-deployed-resources:services;
					fi;
					sleep 60;"""
			} // steps
		} // stage

		stage("Show deployed pods") {
			when {
				expression { env.TEST_TYPE != 'IaaS_backup_restore' && env.TEST_TYPE != 'IaaS_Stability'&& env.TEST_TYPE != 'IaaS_Stability_tools_on_separate_workers'}
			} // when
			steps {
				sh "kubectl get pods -n \$(cat .bob/var.namespace) --kubeconfig ${env.OUTPUT_DIR}/${env.KUBE_HOST}.admin.conf;"
			} // steps
		} // stage

		stage("Show deployed services") {
			when {
				expression { env.TEST_TYPE != 'IaaS_backup_restore' && env.TEST_TYPE != 'IaaS_Stability'&& env.TEST_TYPE != 'IaaS_Stability_tools_on_separate_workers'}
			} // when
			steps {
				sh "kubectl get svc -n \$(cat .bob/var.namespace) --kubeconfig ${env.OUTPUT_DIR}/${env.KUBE_HOST}.admin.conf;"
			} // steps
		} // stage

		stage("Install Default Users") {
			when {
				allOf {
					expression { NODE_DEPLOYED != 0 }
					expression { env.TEST_TYPE != 'IaaS_Stability'&& env.TEST_TYPE != 'IaaS_Stability_tools_on_separate_workers'&& env.TEST_TYPE != 'IaaS_backup_restore'}
				} // allOf
			} // when
			steps {
				sh """ ./bob/bob install-default-users-ci:default-users;
					#./bob/bob install-default-users-ci:no-pwdReset;
					sleep 60;"""
			} // steps
		} // stage

		stage("Install Default Certificates") {
			when {
				allOf {
					expression { NODE_DEPLOYED != 0 || env.TEST_TYPE == 'IaaS_Stability' || env.TEST_TYPE == 'IaaS_Stability_tools_on_separate_workers' }
					expression { env.TEST_TYPE != 'IaaS_backup_restore'}
				} //allOf
			}
			steps {
				sh """#!/bin/bash -e
					./bob/bob install-certificates:nbi;
					[[ "${env.NLF}" == "true" ]] && ./bob/bob install-certificates:nlf;
					[[ "${env.SCP}" == "true" ]] && ./bob/bob install-certificates:scp-manager && ./bob/bob install-certificates:scp-worker;
					[[ "${env.SLF}" == "true" ]] && ./bob/bob install-certificates:slf;
					[[ "${env.SEPP}" == "true" ]] && ./bob/bob install-certificates:sepp-manager && ./bob/bob install-certificates:sepp-worker;
					[[ "${PVTB}" == "true" && "${env.PVTB_DTLS}" == "true" ]] && ./bob/bob install-certificates:pvtb;
					sleep 60;"""
//					[[ "${env.BSF}" == "true" ]] && ./bob/bob install-certificates:bsf && ./bob/bob install-certificates:diameter;
			} // steps
		} // stage

		stage ("Install Default Configuration") {
			when {
				expression { env.TEST_TYPE != 'IaaS_backup_restore' && env.TEST_TYPE != 'IaaS_Stability'&& env.TEST_TYPE != 'IaaS_Stability_tools_on_separate_workers' && env.TEST_TYPE != 'AAT' }
			} // when
			steps {
				sh """#!/bin/bash -e
					./bob/bob loadConfig:sc
					./bob/bob wait-for-deployed-resources:selectedApps;
					"""
				sh "echo 'Deleting certm deployment configuration secret...'; ./bob/bob deploy:delete-certm-config-secret"
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

		stage("Install VTAP Recorder") {
			when {
				allOf {
					expression { NODE_vtap_recorder != 0 }
					expression { env.TEST_TYPE != 'AAT' && env.TEST_TYPE != 'IaaS_backup_restore' }
				} //allOf
			} // when
			steps {
				sh "./bob/bob -r rulesets/ruleset2.0-eric-vtaprecorder.yaml init:version dtlsServer image package;"
				sh "./bob/bob -r rulesets/ruleset2.0-eric-vtaprecorder.yaml push;"				
				sh "./bob/bob -r rulesets/ruleset2.0-eric-vtaprecorder.yaml deploy;"
			 } // steps
		 } // stage

		stage("Install NeLS simulator") {
			when {
				allOf {
					expression { NODE_nels_simulator != 0 }
					expression { env.TEST_TYPE != 'Stability' && env.TEST_TYPE != 'Robustness' && env.TEST_TYPE != 'AAT' && env.TEST_TYPE != 'IaaS_backup_restore' && env.TEST_TYPE != 'IaaS_Stability'&& env.TEST_TYPE != 'IaaS_Stability_tools_on_separate_workers'}
				} // allOf
			} // when
			steps {
				sh "./bob/bob -r rulesets/ruleset2.0-eric-nels.yaml create-nels-dir"
				sh "./bob/bob -r rulesets/ruleset2.0-eric-nels.yaml get-nels"
				sh "./bob/bob -r rulesets/ruleset2.0-eric-nels.yaml deploy"
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
				sh "./bob/bob -r rulesets/ruleset2.0-eric-sftp.yaml init:sftp-version image package;"
			    sh "./bob/bob -r rulesets/ruleset2.0-eric-sftp.yaml push;"
				sh "./bob/bob -r rulesets/ruleset2.0-eric-sftp.yaml deploy;"
			 } // steps
		 } // stage

		stage("Install k6") {
			when {
				allOf {
					// if K6 is not yet deployed (i.e. installation)
					expression { NODE_K6 != 0 }
					// for all test types except AAT and IaaS_backup_restore
					expression { env.TEST_TYPE != 'AAT' && env.TEST_TYPE != 'IaaS_backup_restore' }
					// only needed for SCP and SEPP, not BSF
					anyOf {
						environment name: 'SCP', value: 'true';
						environment name: 'SEPP', value: 'true'
					} //anyOff
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
				// deploy-k6:    4 replica (default)
				// bsf needs:    1 replica
				// scp needs:    9 replica
				// ci  needs:    2 replica
			} // steps
		} // stage

		stage("Install Bsf Load") {
			when {
				allOf {
					expression { env.TEST_TYPE == 'Stability' || env.TEST_TYPE == 'Robustness' || env.TEST_TYPE == 'IaaS_Stability' || env.TEST_TYPE == 'IaaS_Stability_tools_on_separate_workers' }
					environment name: 'BSF', value: 'true'
				} // allOf
			} // when
			steps {
				sh """./bob/bob -r rulesets/ruleset2.0-eric-bsf-load.yaml init:bsf-load-version;
					./bob/bob -r rulesets/ruleset2.0-eric-bsf-load.yaml build:bsf-load;
					./bob/bob -r rulesets/ruleset2.0-eric-bsf-load.yaml image:build-bsf-load;
					./bob/bob -r rulesets/ruleset2.0-eric-bsf-load.yaml lint:helm-bsf-load;
					./bob/bob -r rulesets/ruleset2.0-eric-bsf-load.yaml package:helm-bsf-load;
					./bob/bob -r rulesets/ruleset2.0-eric-bsf-load.yaml push:bsf-load-image;
					./bob/bob -r rulesets/ruleset2.0-eric-bsf-load.yaml package:helm-bsf-load;"""
				sh """./bob/bob -r rulesets/ruleset2.0-eric-bsf-load.yaml deploy:deploy-cert-secrets;
					./bob/bob -r rulesets/ruleset2.0-eric-bsf-load.yaml deploy:bsf-load;"""
			} // steps
		} // stage

		stage("Install dscload") {
			when {
				environment name: 'DSCLOAD', value: 'true'
			} // when
			steps {
				sh """./bob/bob -r rulesets/ruleset2.0-eric-dscload.yaml init:dscload-version;
				    ./bob/bob -r rulesets/ruleset2.0-eric-dscload.yaml init:set-artifactory-token;
					./bob/bob -r rulesets/ruleset2.0-eric-dscload.yaml image:build-dscload;
					./bob/bob -r rulesets/ruleset2.0-eric-dscload.yaml package:helm-chart-dscload;
					./bob/bob -r rulesets/ruleset2.0-eric-dscload.yaml push:docker-image-dscload;
					./bob/bob -r rulesets/ruleset2.0-eric-dscload.yaml package:helm-chart-dscload;
				    ./bob/bob -r rulesets/ruleset2.0-eric-dscload.yaml deploy:deploy-dscload;"""
			} // steps
		} // stage

		stage("Install influxDB") {
			when {
				expression { env.TEST_TYPE == 'Stability' || env.TEST_TYPE == 'Robustness' || env.TEST_TYPE == 'IaaS_Stability' || env.TEST_TYPE == 'IaaS_Stability_tools_on_separate_workers' }
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
					expression { env.TEST_TYPE == 'Stability' || env.TEST_TYPE == 'Robustness' || env.TEST_TYPE == 'IaaS_Stability' || env.TEST_TYPE == 'IaaS_Stability_tools_on_separate_workers' }
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
					expression { env.TEST_TYPE == 'Stability' || env.TEST_TYPE == 'Robustness' || env.TEST_TYPE == 'IaaS_Stability' || env.TEST_TYPE == 'IaaS_Stability_tools_on_separate_workers' }
					environment name: 'NRFSIM', value: 'true'
				} // allOf
			} // when
			steps {
				sh "./bob/bob -r rulesets/ruleset2.0-eric-nrfsim.yaml init:version init:nrfsim-cxu-number init:nrfsim-cxu-rev-number"
				sh "./bob/bob -r rulesets/ruleset2.0-eric-nrfsim.yaml build"
				sh "./bob/bob -r rulesets/ruleset2.0-eric-nrfsim.yaml image"
				sh "./bob/bob -r rulesets/ruleset2.0-eric-nrfsim.yaml package-full"
				sh "./bob/bob -r rulesets/ruleset2.0-eric-nrfsim.yaml push:docker-image"
				sh "./bob/bob -r rulesets/ruleset2.0-eric-nrfsim.yaml push:helm-chart"
				sh "./bob/bob -r rulesets/ruleset2.0-eric-nrfsim.yaml download:download-nrfsim"
				sh "./bob/bob -r ruleset2.0-eric-esc.yaml install-certificates:nrfsim"
				sh "cp eric-nrfsim-* .bob/.; ./bob/bob -r rulesets/ruleset2.0-eric-nrfsim.yaml deploy;"
			} // steps
		} // stage

		stage("Install SEPPsim") {
			// Stability test: CSA or SCP
			when {
				allOf {
					expression { env.TEST_TYPE == 'Stability' || env.TEST_TYPE == 'Robustness' || env.TEST_TYPE == 'IaaS_Stability' || env.TEST_TYPE == 'IaaS_Stability_tools_on_separate_workers' }
					environment name: 'SEPPSIM', value: 'true'
				} // allOf
			} // when
			steps {
				sh "./bob/bob -r rulesets/ruleset2.0-eric-seppsim.yaml download:download-seppsim;"
				sh """cp eric-seppsim-* .bob/.;
					./bob/bob -r ruleset2.0-eric-esc.yaml install-certificates:seppsim;
					./bob/bob -r rulesets/ruleset2.0-eric-seppsim.yaml deploy;"""
			} // steps
		} // stage

		stage('Reinstall Httpproxies') {
			when {
				allOf {
					expression { NODE_DEPLOYED == 0 }
					expression { env.TEST_TYPE != 'IaaS_backup_restore' }
				} //allOf
			} // when
			steps {
				sh "echo 'Reinstall httpproxies ...'; ./bob/bob undeploy:pm-httpproxy undeploy:osmn-httpproxy undeploy:envoy-admin-httpproxy"
				sh "./bob/bob deploy:pm-httpproxy deploy:osmn-httpproxy deploy:envoy-admin-httpproxy"
			} // steps
		} // stage

		stage("Archiving parameters for downstream Test pipeline") {
			when {
				expression { env.TEST_TYPE != 'IaaS_Stability' && env.TEST_TYPE != 'IaaS_backup_restore' && env.TEST_TYPE != 'IaaS_Stability_tools_on_separate_workers' }
			} //when
			steps {
				sh '''
					echo "BRANCH=$(echo ${BRANCH})" > test.properties;
					echo "KUBE_HOST=$(echo ${KUBE_HOST})" >> test.properties;
					echo "NAMESPACE=`cat ${OUTPUT_DIR}/var.namespace`" >> test.properties;
					echo "VERSION=`cat ${OUTPUT_DIR}/var.esc-version`" >> test.properties;
					echo "COMMIT=`echo ${COMMIT}`" >> test.properties;
					echo "CHART_VERSION=`cat ${OUTPUT_DIR}/var.esc-version`" >> test.properties;
					echo "PROJECT=$(echo ${PROJECT})" >> test.properties;
				'''
				archiveArtifacts artifacts: 'test.properties', onlyIfSuccessful: true
			} // steps
		} // stage
	} // stages

	post{
		always {
			script{
				// Log current job
				if ( fileExists("/proj/DSC_CI/jenkins/tools/help_scripts/concurrentBuilds.sh") ) {
					sh "/proj/DSC_CI/jenkins/tools/help_scripts/concurrentBuilds.sh -stop"
				} // if
			} // script
		} // always
		failure{
			script {
				sh "./scripts/collect_ADP_logs.sh -c ~/.kube/${env.KUBE_HOST}.config -n `cat .bob/var.namespace`"
				archiveArtifacts allowEmptyArchive: true, artifacts: 'logs_*.tgz', onlyIfSuccessful: false
				// do not undeploy for troubleshooting purposes
				if ((env.CLEANCLUSTER != 'FALSE') || ("$TEST_TYPE".contains("SmokeTest"))) {
					cleanCluster(env.KUBE_HOST, env.NAMESPACE);
				} // if

				// only send mail for smoketest
				if ("$TEST_TYPE".contains("SmokeTest")) {
					COLOR = "<body style=\"background-color: LightYellow;\">"

					emailext body: "$COLOR" + "Hi ${env.COMMITTER},<p> the deploy failed, maybe due to your commit? <p> Please check:<br>${env.BUILD_URL}<br><br>Thank you for pushing.<br><br>BR,<br>A-Team",

					mimeType: 'text/html',
					subject: "5G Smoke-test deploy failed for ${env.PROJECT} : ${COMMIT}",
					to: "${env.EMAIL_COMMITTER}, ${env.EMAIL_OTHER}"  // mail to committers and respective Teams channel
					//to: "${env.EMAIL_COMMITTER}, DSCAteam@ericsson.onmicrosoft.com, f37b6763.ericsson.onmicrosoft.com@emea.teams.ms"  //Teams: Master Channel
					//to: "${env.EMAIL_COMMITTER}, DSCAteam@ericsson.onmicrosoft.com, 7352f6e6.ericsson.onmicrosoft.com@emea.teams.ms"  //Teams: Release Channel
				}  // if
			} // script
		} // failure
		aborted {
			script {
				cleanCluster(env.KUBE_HOST, env.NAMESPACE);
			} // script
		} // aborted
	} // post
} // pipeline


//////////////////////////////////////////////////////////////////////////
// Subroutines

def init_bob(){
	sh 'pwd; ls -ltrh; git submodule update --init --recursive'
	sh './bob/bob base.init:create-output-dir'
	sh './bob/bob base.init:set-kube-config'
	sh './bob/bob base.init:set-build-proxy'
	sh './bob/bob base.init:set-namespace'
	sh './bob/bob base.init:set-supreme-properties'
	sh './bob/bob base.init:api-tokens'
	sh './bob/bob init:set-ingressHost'


	sh """#!/bin/bash -e
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
	sh """#!/bin/bash -e
		[[ "${env.BSF}" == "true" ]] && ./bob/bob init:bsf-cxc-number && ./bob/bob init:bsf-cxc-rev-number;
		[[ "${env.BSF}" == "true" ]] && ./bob/bob init:bsf-diameter-cxc-number && ./bob/bob init:bsf-diameter-cxc-rev-number;
		[[ "${env.NLF}" == "true" ]] && ./bob/bob init:nlf-cxc-number && ./bob/bob init:nlf-cxc-rev-number;
		[[ "${env.SCP}" == "true" ]] && ./bob/bob init:scp-cxc-number && ./bob/bob init:scp-cxc-rev-number;					
		[[ "${env.SLF}" == "true" ]] && ./bob/bob init:slf-cxc-number && ./bob/bob init:slf-cxc-rev-number;
		[[ "${env.RLF}" == "true" ]] && ./bob/bob init:rlf-cxc-number && ./bob/bob init:rlf-cxc-rev-number;
		[[ "${env.SEPP}" == "true" ]] && ./bob/bob init:sepp-cxc-number && ./bob/bob init:sepp-cxc-rev-number;
		echo '';"""
		// echo is needed, otherwise the pipe fails

	sh './bob/bob init:monitor-cxc-number; ./bob/bob init:monitor-cxc-rev-number'
	//sh './bob/bob init:cleanup-devenv-values'
}


def cleanCluster(String kubeHost, String namespace) {
	try {
		timeout(time: 600000, unit: 'MILLISECONDS') {
			// clean cluster due to abort
			sh "/home/eiffelesc/scripts/cleanCluster ${namespace} ${kubeHost}"
		} // timeout
	} catch (exc) {
		sh """
			kubectl delete ns ${namespace} --kubeconfig ${env.OUTPUT_DIR}/${kubeHost}.admin.conf;
			kubectl create ns ${namespace} --kubeconfig ${env.OUTPUT_DIR}/${kubeHost}.admin.conf;
		"""
	} // catch
}
