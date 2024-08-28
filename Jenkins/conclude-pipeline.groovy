def HELM_CMD
def GIT_CMD
def PYTHON_CMD
def values

//////////////////////////////////////////////////////////////////////////
//
// first run on SERO_GIC buildslaves, decide which buildslave(s) to use

node('DSC_BuildSlave_GIC_1||DSC_BuildSlave_GIC_2||DSC_BuildSlave_GIC_3||DSC_BuildSlave_GIC_4') {

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

} //node

//
//////////////////////////////////////////////////////////////////////////

@Library('Shared-Libs-SC') _

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

		CHART_NAME="${env.CHART_NAME}"
		CHART_REPO="${env.CHART_REPO}"
		CHART_VERSION="${env.CHART_VERSION}"
		KUBE_HOST ="${KUBE_HOST}"
		NIGHTLY = "${env.NIGHTLY}"
		BRANCH = "${env.BRANCH}"
		TROUBLESHOOTING = "${env.TROUBLESHOOTING}"
		TEST_BRANCH = "${env.TEST_BRANCH}"
		TEST_COMMIT = "${env.TEST_COMMIT}"
	}

	stages{

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
			} //steps
		} //stage

		stage('Cleaning WS on Buildcomputes'){
			// only if no other instance of the same job is running in parallel
			// if another instance is running this procedure would delete its WS and make it failing
			when {
				expression { currentBuild.getPreviousBuildInProgress() == null }
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
/*				script {
					//GIT_CMD = "${DOCKER_RUN}  -v ${env.WORKSPACE}:${env.WORKSPACE} -w ${env.WORKSPACE} ${BUILDER_DOCKER_IMAGE} git "
				}
*/
				echo "CHART NAME: ${env.CHART_NAME}"
				echo "CHART REPO: ${env.CHART_REPO}"
				echo "CHART VERSION: ${env.CHART_VERSION}"
				echo "KUBE HOST: ${env.KUBE_HOST}"
				echo "NAMESPACE: ${env.NAMESPACE}"

				script {
					// set displayed build name to "BUILD_NUMBER - COMMIT"
					currentBuild.displayName = "#${BUILD_NUMBER} - ${env.COMMIT}"
					// set displayed description to "NODE_NAME"
					currentBuild.description = "$NODE_NAME";

					PYTHON_CMD = "${DOCKER_RUN} ${BUILDER_DOCKER_VOLUMES} -w ${env.WORKSPACE} ${BUILDER_DOCKER_ENV} ${BUILDER_DOCKER_IMAGE} python "
					HELM_CMD = "${DOCKER_RUN} ${BUILDER_DOCKER_VOLUMES} ${BUILDER_DOCKER_ENV} ${BUILDER_DOCKER_IMAGE} helm"
				}
			}
		}

		stage('Init bob'){
			 steps{
				echo pwd()
				sh 'ls -ltrh'
				sh 'git submodule update --init --recursive'
				//sh './bob/bob init:set-build-proxy'
				sh './bob/bob base.init:set-kube-config'
				sh './bob/bob base.init:set-namespace'
				sh './bob/bob base.init:api-tokens'
				//sh './bob/bob init:set-ingressHost'
				sh './bob/bob init:cxp-number'
				sh './bob/bob init:cxp-rev-number'
				sh './bob/bob init:scp-cxc-number'
				sh './bob/bob init:scp-cxc-rev-number'
				sh './bob/bob init:sepp-cxc-number'
				sh './bob/bob init:sepp-cxc-rev-number'
				sh './bob/bob init:bsf-cxc-number'
				sh './bob/bob init:bsf-cxc-rev-number'
				sh './bob/bob init:bsf-diameter-cxc-number'
				sh './bob/bob init:bsf-diameter-cxc-rev-number'
				sh './bob/bob init:nlf-cxc-number'
				sh './bob/bob init:nlf-cxc-rev-number'				
				sh './bob/bob init:slf-cxc-number'
				sh './bob/bob init:slf-cxc-rev-number'
				sh './bob/bob init:rlf-cxc-number'
				sh './bob/bob init:rlf-cxc-rev-number'
				sh './bob/bob init:monitor-cxc-number'
				sh './bob/bob init:monitor-cxc-rev-number'
				//sh './bob/bob init:cleanup-devenv-values'
				sh """if ( test '${env.CHART_NAME}' = 'eric-sc' );
					then
						echo ${env.CHART_VERSION} > ./.bob/var.bsf-version
						echo ${env.CHART_VERSION} > ./.bob/var.bsf-diameter-version
						echo ${env.CHART_VERSION} > ./.bob/var.nlf-version
						echo ${env.CHART_VERSION} > ./.bob/var.scp-version
						echo ${env.CHART_VERSION} > ./.bob/var.sepp-version
						echo ${env.CHART_VERSION} > ./.bob/var.slf-version
						echo ${env.CHART_VERSION} > ./.bob/var.rlf-version
						echo ${env.CHART_VERSION} > ./.bob/var.esc-version
						echo ${env.CHART_VERSION} > ./.bob/var.monitor-version
					else
						tac esc/helm/eric-sc-umbrella/requirements.yaml | grep -m1 'version' | cut -d ' ' -f4 > ./.bob/var.esc-version
						echo "the esc version is set to : `cat ./.bob/var.esc-version` !"
					fi;"""
				sh './bob/bob init:product-info'
			}
		}

		stage('Update baseline'){
			when {
				allOf{
					environment name: 'NIGHTLY', value: 'FALSE';   // not to be done in case of nightly CI
					environment name: 'DEVPIPE', value: 'FALSE';   // not to be done in case of a development pipe
				};
			}
			steps{
				// sh "${PYTHON_CMD} ${env.WORKSPACE}/baseline_scripts/scripts/update_chart_requirements.py --appChartDir=${env.WORKSPACE}/esc/helm/eric-sc-umbrella --chartName=${CHART_NAME} --chartRepo=${CHART_REPO} --chartVersion=${CHART_VERSION}"
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
					else
						./bob/bob update-umbrella:update-product-numbers;
						./bob/bob update-umbrella-generic:service;
					fi;"""
			}
		}

		stage('Undeploy') {
			when {
				environment name: 'CLEANCLUSTER', value: 'TRUE'
			}
			steps {
				script {
					try {
						timeout(time: 600000, unit: 'MILLISECONDS') {
							sh "/home/eiffelesc/scripts/cleanCluster ${env.NAMESPACE} ${env.KUBE_HOST}"
						}
					} catch (exc) {
						sh """for i in `kubectl get pods -n ${env.NAMESPACE} | grep -i Terminating | cut -d' ' -f1`; 
							do 
								kubectl delete pods \$i -n ${env.NAMESPACE} --force --grace-period=0;
							done;"""
					}
				}
			}
		}

		stage('Push chart, label test-commit') {
			when {
				allOf{
					environment name: 'NIGHTLY', value: 'FALSE';   // not to be done in case of nightly CI
					environment name: 'DEVPIPE', value: 'FALSE';   // not to be done in case of a development pipe
				} // allOf
			} // when
			steps {
				echo "Commit and push chart to dev-repo:"
				sh """cp .bob/eric-sc-umbrella/requirements.yaml esc/helm/eric-sc-umbrella/requirements.yaml;

					if ( ! (git diff --quiet -- esc/helm/eric-sc-umbrella/requirements.yaml) ); 
					then 
						git remote set-url origin --push https://eiffelesc@gerrit.ericsson.se/a/MC_5G/5g_proto
						git add esc/helm/eric-sc-umbrella/requirements.yaml; 
						git commit -m 'Automatic new version in baseline ${env.CHART_NAME} ${env.CHART_VERSION}, ${env.COMMIT}'; 
						git status
						git log -n 10 --oneline --decorate --graph --all
						git pull origin ${env.BRANCH} --no-rebase
						git push origin HEAD:${env.BRANCH}
					else 
						echo 'Nothing to be commited.' 
					fi; """
				echo "Checkout test-branch and label test-commit:"

				checkout scmGit(
					branches: [[name: '${TEST_COMMIT}']],
					extensions: [
						submodule(parentCredentials: true, recursiveSubmodules: true),
					   [$class: 'RelativeTargetDirectory', relativeTargetDir: '5g_test_ci']],
					userRemoteConfigs: [[
						name: 'origin',
						credentialsId: 'eiffelesc-user-password',
						url: 'https://eiffelesc@gerritmirror-direct.sero.gic.ericsson.se/a/MC_5G/5g_test_ci']]
				) //checkout

				dir('5g_test_ci') {
					sh """
						git remote set-url origin --push https://eiffelesc@gerrit.ericsson.se/a/MC_5G/5g_test_ci
						git status;
						git tag --sort=-refname | head -n 10;
						if (git rev-parse "smoked-$TEST_BRANCH-$CHART_VERSION" >/dev/null 2>&1);
						then
							echo 'tag already exists, will be removed now for moving it to the current commit'
							git push --delete origin smoked-$TEST_BRANCH-$CHART_VERSION
							git tag  --delete smoked-$TEST_BRANCH-$CHART_VERSION
							git tag --sort=-refname | head -n 10;
						else
							echo 'tag does not yet exist, will be pushed to the current commit now'
						fi;
						git tag -a smoked-$TEST_BRANCH-$CHART_VERSION -m \'commit successfully smoke-tested\' $TEST_COMMIT;
						git push origin smoked-$TEST_BRANCH-$CHART_VERSION;
						git tag --sort=-refname | head -n 10;
					"""
				} // dir
			} // steps
		}
	}
}

