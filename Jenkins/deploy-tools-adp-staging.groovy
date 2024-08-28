
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
				sh 'ls -ltrh'
				sh 'git submodule update --init --recursive'
				sh './bob/bob init:set-build-proxy'
				sh './bob/bob init:set-kube-config'
				sh './bob/bob init:set-namespace'
				sh './bob/bob base.init:api-tokens'
				sh './bob/bob init:set-ingressHost'
				sh './bob/bob -r rulesets/ruleset2.0-eric-tools.yaml init:credentials'
				sh './bob/bob -r rulesets/ruleset2.0-eric-tools.yaml init:token2'
			} // steps
		} //stage
		stage("Install NeLS simulator") {
			when {
			    environment name: 'NELS', value: 'true'
			}
      steps {
          sh "./bob/bob -r rulesets/ruleset2.0-eric-nels.yaml create-nels-dir"
           // withCredentials([string(credentialsId: 'c9bf8e3d-476c-4be9-b26a-73d0275975bd', variable: 'ARM_ARTIFACTORY_TOKEN')])
          // {
   					sh "./bob/bob -r rulesets/ruleset2.0-eric-nels.yaml get-nels"
          // }
          sh "./bob/bob -r rulesets/ruleset2.0-eric-nels.yaml deploy"
      } // steps
    } // stage

    stage("Update tools-generic"){
        steps{
            sh "./bob/bob -r rulesets/ruleset2.0-eric-tools.yaml update-tools-generic:copy"
        }
    }

		stage("Init versions"){
			steps{
				sh """
					./bob/bob -r rulesets/ruleset2.0-eric-tools.yaml init:tools-version

					if [ ${CHFSIM} = \"true\" ];then
						./bob/bob -r rulesets/ruleset2.0-eric-tools.yaml init:chfsim-version
					fi
					if [ ${DSCLOAD} = \"true\" ];then
					   ./bob/bob -r rulesets/ruleset2.0-eric-tools.yaml init:dscload-version;
					fi
					if [ ${K6} = \"true\" ];then
				       ./bob/bob -r rulesets/ruleset2.0-eric-tools.yaml init:k6-version;
					fi
					if [ ${SEPPSIM} = \"true\" ];then
					   ./bob/bob -r rulesets/ruleset2.0-eric-tools.yaml init:seppsim-version;
					fi
					if [ ${REDIS} = \"true\" ];then
					   ./bob/bob -r rulesets/ruleset2.0-eric-tools.yaml init:redis-version;
					fi
					if [ ${ATMOZ_SFTP} = \"true\" ];then
					   ./bob/bob -r rulesets/ruleset2.0-eric-tools.yaml init:sftp-version;
					fi
					if [ ${SYSLOG} = \"true\" ];then
					   ./bob/bob -r rulesets/ruleset2.0-eric-tools.yaml init:syslog-version;
					fi
					if [ ${NRFSIM} = \"true\" ];then
					   ./bob/bob -r rulesets/ruleset2.0-eric-tools.yaml init:nrfsim-version;
					fi

					./bob/bob -r rulesets/ruleset2.0-eric-tools.yaml init:set-docker-repo
					./bob/bob -r rulesets/ruleset2.0-eric-tools.yaml init:set-helm-repo
				"""
			}
		}
		stage("Build"){
			when{
			    environment name: 'CHFSIM', value: 'true';
			}
		    steps{
				sh """
					if [ ${CHFSIM} = \"true\" ];then
		        		./bob/bob -r rulesets/ruleset2.0-eric-tools.yaml build:chfsim;
					fi
				"""
		    }
		}
		stage("Build & Push images"){
			steps{
				withCredentials([string(credentialsId: 'c9bf8e3d-476c-4be9-b26a-73d0275975bd', variable: 'ARTIFACTORY_TOKEN')])
				{

					sh  """

						if [ ${CHFSIM} = \"true\" ];then
							./bob/bob -r rulesets/ruleset2.0-eric-tools.yaml build-images:chfsim push-images:chfsim
						fi
						if [ ${SEPPSIM} = \"true\" ];then
							./bob/bob -r rulesets/ruleset2.0-eric-tools.yaml build-images:seppsim push-images:seppsim
						fi
						if [ ${K6} = \"true\" ];then
							./bob/bob -r rulesets/ruleset2.0-eric-tools.yaml build-images:k6 push-images:k6
						fi
						if [ ${ATMOZ_SFTP} = \"true\" ];then
							./bob/bob -r rulesets/ruleset2.0-eric-tools.yaml build-images:sftp push-images:sftp;
						fi
						if [ ${NRFSIM} = \"true\" ];then
							./bob/bob -r rulesets/ruleset2.0-eric-tools.yaml build-images:nrfsim push-images:nrfsim
						fi
						if [ ${SYSLOG} = \"true\" ];then
							./bob/bob -r rulesets/ruleset2.0-eric-tools.yaml build-images:syslog push-images:syslog
						fi
						"""
				}

				withCredentials([string(credentialsId: 'fd214478-e2ff-493d-a2f6-144fb9db691b', variable: 'ARM_ARTIFACTORY_TOKEN')])
				{
					sh"""
						if [ ${DSCLOAD} = \"true\" ];then
						./bob/bob -r rulesets/ruleset2.0-eric-tools.yaml init:set-artifactory-token
						./bob/bob -r rulesets/ruleset2.0-eric-tools.yaml build-images:dscload
						fi
					"""
				}
				withCredentials([string(credentialsId: 'c9bf8e3d-476c-4be9-b26a-73d0275975bd', variable: 'ARTIFACTORY_TOKEN')])
				{
					sh"""
						if [ ${DSCLOAD} = \"true\" ];then
						./bob/bob -r rulesets/ruleset2.0-eric-tools.yaml push-images:dscload
						fi
					"""
				}
			}


		}
		stage("Package each tool individually"){
			steps{
				withCredentials([string(credentialsId: 'c9bf8e3d-476c-4be9-b26a-73d0275975bd', variable: 'ARTIFACTORY_TOKEN')])
	            {
					sh  """
						if [ ${DSCLOAD} = \"true\" ];then
						./bob/bob -r rulesets/ruleset2.0-eric-tools.yaml package-helm:dscload push-helm:dscload
						fi
						if [ ${CHFSIM} = \"true\" ];then
						./bob/bob -r rulesets/ruleset2.0-eric-tools.yaml package-helm:chfsim push-helm:chfsim
						fi
						if [ ${SYSLOG} = \"true\" ];then
						./bob/bob -r rulesets/ruleset2.0-eric-tools.yaml package-helm:syslog push-helm:syslog
						fi
						if [ ${SEPPSIM} = \"true\" ];then
						./bob/bob -r rulesets/ruleset2.0-eric-tools.yaml package-helm:seppsim push-helm:seppsim
						fi
						if [ ${K6} = \"true\" ];then
						./bob/bob -r rulesets/ruleset2.0-eric-tools.yaml package-helm:k6 push-helm:k6
						fi
						if [ ${ATMOZ_SFTP} = \"true\" ];then
						./bob/bob -r rulesets/ruleset2.0-eric-tools.yaml package-helm:sftp push-helm:sftp
						fi
					 """
				}

			}
		}
		stage("Update tools"){
			steps{
				sh  """
						if [ ${DSCLOAD} = \"true\" ];then
						./bob/bob -r rulesets/ruleset2.0-eric-tools.yaml update-tools:dscload
						fi
						if [ ${CHFSIM} = \"true\" ];then
						./bob/bob -r rulesets/ruleset2.0-eric-tools.yaml update-tools:chfsim
						fi
						if [ ${SYSLOG} = \"true\" ];then
						./bob/bob -r rulesets/ruleset2.0-eric-tools.yaml update-tools:syslog
						fi
						if [ ${SEPPSIM} = \"true\" ];then
						./bob/bob -r rulesets/ruleset2.0-eric-tools.yaml update-tools:seppsim
						fi
						if [ ${K6} = \"true\" ];then
						./bob/bob -r rulesets/ruleset2.0-eric-tools.yaml update-tools:k6
						fi
						if [ ${ATMOZ_SFTP} = \"true\" ];then
						./bob/bob -r rulesets/ruleset2.0-eric-tools.yaml update-tools:sftp
						fi
					 """
			}
		}
		stage("Configure values/requirements!"){
			steps {
				sh "cp esc/helm/eric-sc-simtools/values.yaml .bob/eric-sc-simtools/values.yaml"
				sh 'bob/bob -r rulesets/ruleset2.0-eric-tools.yaml modify-selected:influxdb2;'
				sh 'bob/bob -r rulesets/ruleset2.0-eric-tools.yaml modify-selected:grafana;'
				sh 'bob/bob -r rulesets/ruleset2.0-eric-tools.yaml modify-selected:telegraf;'
				sh """ if [ ${CHFSIM} = \"false\" ]
				    then
						echo Disabling chfsim....
						bob/bob -r rulesets/ruleset2.0-eric-tools.yaml modify-selected:chfsim;
					fi;
				    if [ ${K6} = \"false\" ]
				    then
						echo Disabling k6....
						bob/bob -r rulesets/ruleset2.0-eric-tools.yaml modify-selected:k6;
					fi ;
					if [ ${REDIS} = \"false\" ]
				    then
						echo Disabling redis....
						bob/bob -r rulesets/ruleset2.0-eric-tools.yaml modify-selected:redis;
					fi;
                    if [ ${ATMOZ_SFTP} = \"false\" ]
				    then
						echo Disabling atmoz sftp ....
						bob/bob -r rulesets/ruleset2.0-eric-tools.yaml modify-selected:sftp;
					fi;
					if [ ${SYSLOG} = \"false\" ]
				    then
						echo Disabling syslog ....
						bob/bob -r rulesets/ruleset2.0-eric-tools.yaml modify-selected:syslog;
					fi;
					if [ ${DSCLOAD} = \"false\" ]
				    then
						echo Disabling dscload....
						bob/bob -r rulesets/ruleset2.0-eric-tools.yaml modify-selected:dscload;
					fi;
					if [ ${SEPPSIM} = \"false\" ]
				    then
						echo Disabling seppsim....
						bob/bob -r rulesets/ruleset2.0-eric-tools.yaml modify-selected:seppsim;
					fi;
					if [ ${NRFSIM} = \"false\" ]
				    then
						echo Disabling nrfsim....
						bob/bob -r rulesets/ruleset2.0-eric-tools.yaml modify-selected:nrfsim;
					fi;
					if [ ${NRFSIM} = \"false\" ]
				    then
						echo Disabling nrfsim....
						bob/bob -r rulesets/ruleset2.0-eric-tools.yaml modify-selected:nrfsim;
					fi;

					YamlFile='.bob/eric-sc-simtools/values.yaml';
					yq -i eval ".global.k6.enabled = ${K6}" \${YamlFile};
					yq -i eval ".global.sftp.enabled = ${ATMOZ_SFTP}" \${YamlFile};
					yq -i eval ".global.syslog.enabled = ${SYSLOG}" \${YamlFile};
					yq -i eval ".global.chfsim.enabled = ${CHFSIM}" \${YamlFile};
					yq -i eval ".global.dscload.enabled = ${DSCLOAD}" \${YamlFile};
					yq -i eval ".global.nrfsim.enabled = ${NRFSIM}" \${YamlFile};
					yq -i eval ".global.redis.enabled = ${REDIS}" \${YamlFile};
					yq -i eval ".global.seppsim.enabled = ${SEPPSIM}" \${YamlFile};
					"""
			} // steps
		}

		stage("Package and deploy tools"){
			steps{
				sh """./bob/bob -r rulesets/ruleset2.0-eric-tools.yaml package-tools:all;
				    ./bob/bob -r rulesets/ruleset2.0-eric-tools.yaml package-tools:tools"""

	            withCredentials([string(credentialsId: 'c9bf8e3d-476c-4be9-b26a-73d0275975bd', variable: 'ARTIFACTORY_TOKEN')])
	            {
	            	sh "./bob/bob -r rulesets/ruleset2.0-eric-tools.yaml push-helm:tools"
	            }
				sh "./bob/bob -r rulesets/ruleset2.0-eric-tools.yaml deploy-tools"
			}//steps
		}
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

							sh "./bob/bob clean-resources:delete-namespace"
							sh "./bob/bob clean-resources:create-namespace"
							sh "./bob/bob clean-resources:remove-cluster-resources"

						} // timeout
					} // try
					catch (exc) {
						TROUBLESHOOTING_FAILURE_REASON += ("Failed to clean cluster forcefully after sim-tools failure")
					} // catch
				} // if



				// TEST_TYPE is AdpStaging -> inform Challengers
				if ("$TEST_TYPE".contains("AdpStaging")) {
					emailext body: "Hello ADP CICD owners,<br><br> Simulated tools deployment, needed for SC application verification, failed.<br>Please cleanup ${env.KUBE_HOST} immediately!<br><br>Following are the possible Troubleshooting reasons: ${TROUBLESHOOTING_FAILURE_REASON} <br><br>.Please check:<br>"+"${emailbody4}",
					mimeType: 'text/html',
					subject: "SC ADP CICD Staging deploy failed",
					to: "IXG-ChallengersTeam@ericsson.onmicrosoft.com"
				} // if
			} // script
		} // failure
		aborted{
				sh 'echo Pipeline aborted due to timeout'
				sh './bob/bob clean-resources'
        }// abort
	} // post
} //pipeline
