
//////////////////////////////////////////////////////////////////////////
//
// first run on SERO_GIC buildslaves, decide which buildslave(s) to use

def AGENT_LABEL = null

node('DSC_BuildSlave_GIC_1||DSC_BuildSlave_GIC_2||DSC_BuildSlave_GIC_3||DSC_BuildSlave_GIC_4') {

	stage('set agent'){

		if (env.IP_VERSION == '6') {	// move to IPv6 buildslave(s)
			AGENT_LABEL = '5G-SC-IPv6'
		} else	{						// move to IPv4 buildslave(s)
			AGENT_LABEL = '5G-SC'
		} //if

	} //stage
} //node

//
//////////////////////////////////////////////////////////////////////////


//////////////////////////////////////////////////////////////////////////
// Set email body

def email_color=""
def email_text1="Hi ${COMMITTER},<br><br>Smoke Test results for commit: ${COMMIT}"
def email_jcatlogs=""
def email_verdict=""
def email_buildUrl="Jcat logs:<br>${env.BUILD_URL}"
def email_text2="Thank you for pushing.<br><br>BR,<br>A-Team"


//////////////////////////////////////////////////////////////////////////
//   Subroutines

// look for suite name in latestjcatlogs_$nr/dt.xml
String retrieveSuite(String nr) {
	env.someVar=nr
	echo "looking for suite name in latestjcatlogs_${env.someVar}/dt.xml"
	suite= sh (returnStdout: true,
		script:"awk -F '\"' '/suite name/ {print \$2}' latestjcatlogs_${env.someVar}/dt.xml")
	echo "Found suite: $suite"
	return (suite)
}

// look for suite info in latestjcatlogs_$nr/dt.xml
String retrieveInfo(String nr) {
	env.someVar2=nr
	echo "looking for suite info in latestjcatlogs_${env.someVar2}/dt.xml"
	info= sh (returnStdout: true,
		//script:"cat latestjcatlogs_${env.someVar2}/dt.xml | grep -o -P \"suite name.*ExecutionFinished\"")
		script:"awk '/suite name/ {res=\$0; count=0} /Skip/ {count=count+1} END {print res\" Skipped=\"count}' latestjcatlogs_${env.someVar2}/dt.xml | sed -E \"s/(<|ExecutionFinished.*>)//g\"")
	echo "Found info: $info"
	return (info)
}

//////////////////////////////////////////////////////////////////////////
//   Subroutines

String findCommitters(){

	committers = ""
	catchError(buildResult: 'SUCCESS', message: 'Error in subroutine findCommitters', stageResult: 'UNSTABLE') {
		// get author emails from URL and pipe into file
		sh "curl --netrc  ${env.BUILD_URL}api/json -o culprits.json"
		// get from the changeset (changes for this build) the full name of the authors
		sh 'cat ./culprits.json | jq -c \'[.changeSets[].items[].author.fullName]\' > culprits.txt'

		// Remove Eiffel commit
		sh 'cat ./culprits.txt'
		sh 'cat ./culprits.txt  | sed -E \'s/"Functional account for Eiffel029",?//g\' > culprits1.txt'
		sh 'cat ./culprits1.txt'

		// find lines containing "" then  #1 remove "" #2 remove [] #3 remove \n #remove , at the end of the line #change , to ,<space>
		committers = """${sh( returnStdout: true,
				script: "cat culprits1.txt | grep '\"' | tr -d '\"[]\n' | sed 's/,\$//' | sed 's/,/, /g' "
		)}""".trim()
	} //catchError

	echo "Sub: $committers"
	return (committers)
}


String findCommittersEmail(){

	committers_email = ""
	catchError(buildResult: 'SUCCESS', message: 'Error in subroutine findCommittersEmail', stageResult: 'UNSTABLE') {
		// get author emails from URL and pipe into file
		sh "curl --netrc ${env.BUILD_URL}api/json -o culprits_email1.json"
		// get from the changeset (changes for this build) the email addresses
		sh 'cat ./culprits_email1.json | jq -c \'[.changeSets[].items[].authorEmail]\' > culprits_email2.txt'

		// Remove Eiffel commit
		sh 'cat ./culprits_email2.txt'
		sh 'cat ./culprits_email2.txt | sed -E \'s/"Eiffel Jenkins PDU-MC-DSCeiffeldsc-noreply@ericsson.com",?//g\' > culprits_email3.txt'
		sh 'cat ./culprits_email3.txt'

		// find lines containing "" then  #1 remove "" #2 remove [] #3 remove \n #change , to ;<space>
		committers_email = """${sh( returnStdout: true,
				script: "cat culprits_email3.txt | grep '\"' | tr -d '\"[]\n' | sed 's/,/; /g'"
		)}""".trim()
	} //catchError

	echo "Sub: $committers_email"
	return (committers_email)
}

//  end subroutines
//////////////////////////////////////////////////////////////////////////

pipeline {

	agent { label "${AGENT_LABEL}"   } // set label according to IP_VERSION parameter

	options{
		// eedcla, 21.06.21: commented out due to aborted suite taking onger than 30 hrs
		// the pipeline has a timeout also
		// timeout(time: 1800, unit: 'MINUTES')

		timestamps()
	}

	environment{
		// Variables needed for the ruleset
		//BSF = true
		//SCP = true
		//BSF_TLS = false
		//SCP_TLS = false
		LOGGING = true
		PRODUCTION = true
		RESOURCES = "full"

		// Pipe Variables Used in Jenkins (sorted alphabetically)
		ARM_ARTIFACTORY_TOKEN = "${ARM_ARTIFACTORY_TOKEN}"
		BRANCH = "${env.BRANCH}"
		COMMIT = "${COMMIT}"
		T_COMMITTERS = ""
		T_EMAIL_COMMITTERS = ""
		KUBE_HOST = "${env.KUBE_HOST}"
		NAMESPACE = "${env.NAMESPACE}"
		PRODUCT = ''
		REDIS_CONTAINER = "eric-chfsim-redis"
		RERUN = "${env.RERUN}"
		TROUBLESHOOTING = "${env.TROUBLESHOOTING}"
		TEST_BRANCH = "${env.TEST_BRANCH}"
		USE_WORKER_IP = "${env.USE_WORKER_IP}"
		VERSION = ''
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

		stage('Cleaning WS on Buildslaves'){
			// only if no other instance of the same job is running in parallel
			// if another instance is running this procedure would delete its WS and make it failing
			when {
				expression { currentBuild.getPreviousBuildInProgress() == null }
			}
			steps{
				// remove all (!) content (including the git repo)
				sh '''#!/bin/bash -x
					IP_ADDRESS_TMP=`hostname -i`;
					BUILD_PATH_TMP=`pwd`;

					for ip in 10.210.174.60 10.210.174.61 10.210.155.84 10.210.155.137 10.210.174.209;
					do
						if [ "${IP_ADDRESS_TMP}" != "${ip}" ];
						then
							ssh eiffelesc@${ip} "if [ -d ${BUILD_PATH_TMP} ]; then rm -rf ${BUILD_PATH_TMP}/; else echo \"Nothing to clean\"; fi;"
						fi;
					done;'''
			} //steps
		} //stage

		stage('Clone test repository') {
			steps{
				echo 'Checking user'
				sh 'whoami'
				echo 'check path'
				echo pwd()
				sh 'git submodule update --init --recursive'
				sh './bob/bob base.init:create-output-dir'
				sh './bob/bob base.init:set-namespace'
				sh './bob/bob base.init:set-kube-config'
				sh './bob/bob base.init:set-build-proxy'
				sh './bob/bob base.init:set-supreme-properties'
				sh './bob/bob base.init:policy-api-version'
				sh './bob/bob base.init:api-tokens'
				sh './bob/bob base.init:git-properties'
				sh './bob/bob base.init:mvn-args'
				sh './bob/bob init:set-ingressHost'
				sh './bob/bob init:generate-versions'
				sh './bob/bob init:product-info'
				sh './bob/bob init:cxp-number'
				sh './bob/bob init:cxp-rev-number'
				sh './bob/bob init:scp-cxc-number'
				sh './bob/bob init:scp-cxc-rev-number'
				sh './bob/bob init:sepp-cxc-number'
				sh './bob/bob init:sepp-cxc-rev-number'
				sh './bob/bob init:bsf-cxc-number'
				sh './bob/bob init:bsf-cxc-rev-number'
				sh './bob/bob init:nlf-cxc-number'
				sh './bob/bob init:nlf-cxc-rev-number'
				sh './bob/bob init:rlf-cxc-number'
				sh './bob/bob init:rlf-cxc-rev-number'
				sh './bob/bob init:slf-cxc-number'
				sh './bob/bob init:slf-cxc-rev-number'
				sh './bob/bob init:monitor-cxc-number'
				sh './bob/bob init:monitor-cxc-rev-number'
				sh './bob/bob init:bsf-diameter-cxc-number'
				sh './bob/bob init:bsf-diameter-cxc-rev-number'
				sh './bob/bob init:set-dirty-package'
				sh './bob/bob build-libs-ci'

				echo 'Creating test repo directory'
				dir('5g_test_ci') {
					deleteDir()
				}
				echo pwd()
				echo 'Trying to clone the 5G test repository'
				script{
					switch (env.TEST_TYPE) {
						case [ 'CI' ]:
							checkout scmGit(
								branches: [[name: 'refs/tags/smoked-$TEST_BRANCH-$CHART_VERSION']],
								userRemoteConfigs: [[credentialsId: 'eiffelesc-user-password', name: 'origin', url: 'https://eiffelesc@gerritmirror-direct.sero.gic.ericsson.se/a/MC_5G/5g_test_ci']],
								extensions: [submodule(parentCredentials: true, recursiveSubmodules: true, reference: '', shallow: false), [$class: 'RelativeTargetDirectory', relativeTargetDir: '5g_test_ci']])
							break
						case [ 'Stability', 'Robustness' ]:
							if (env.TESTNG ==~ /.*nightly.*/) {
								checkout scmGit(
									branches: [[name: 'refs/tags/smoked-$TEST_BRANCH-$CHART_VERSION']],
									userRemoteConfigs: [[credentialsId: 'eiffelesc-user-password', name: 'origin', url: 'https://eiffelesc@gerritmirror-direct.sero.gic.ericsson.se/a/MC_5G/5g_test_ci']],
									extensions: [submodule(parentCredentials: true, recursiveSubmodules: true, reference: '', shallow: false), [$class: 'RelativeTargetDirectory', relativeTargetDir: '5g_test_ci']])
							}
							else{
							checkout scmGit(
								branches: [[name: '*/${TEST_BRANCH}']],
								userRemoteConfigs: [[credentialsId: 'eiffelesc-user-password', name: 'origin', url: 'https://eiffelesc@gerritmirror-direct.sero.gic.ericsson.se/a/MC_5G/5g_test_ci']],
								extensions: [submodule(parentCredentials: true, recursiveSubmodules: true, reference: '', shallow: false), [$class: 'RelativeTargetDirectory', relativeTargetDir: '5g_test_ci']])
							}
							break
						default:
							checkout scmGit(
								branches: [[name: '*/${TEST_BRANCH}']],
								userRemoteConfigs: [[credentialsId: 'eiffelesc-user-password', name: 'origin', url: 'https://eiffelesc@gerritmirror-direct.sero.gic.ericsson.se/a/MC_5G/5g_test_ci']],
								extensions: [submodule(parentCredentials: true, recursiveSubmodules: true, reference: '', shallow: false), [$class: 'RelativeTargetDirectory', relativeTargetDir: '5g_test_ci']])
							break
					} //switch
					// find committers in this build (5g_proto & 5g_test_ci)
					T_COMMITTERS = findCommitters()
					T_EMAIL_COMMITTERS = findCommittersEmail()
				} //script
				echo 'check content'
				dir('5g_test_ci') {
					echo pwd()
					sh 'git log -n 10'
					sh 'ls -ltrh'
					sh 'git submodule update --init --recursive'
				} //dir
				dir('scripts') {
					echo 'Creating the certificates in order to enable tls for CI (currently not applied, 2021.03.)'
					//sh './install_certs.sh -ni chfsim'
					//sh 'cp ./certificates/certm_worker/keys/rootCA.crt ../5g_test_ci/jcat-esc/EscTestProperties/chf/ca.pem'
					//sh 'cp ./certificates/certm_worker/keys/chf/cert.pem ../5g_test_ci/jcat-esc/EscTestProperties/chf/certificate.pem'
					//sh 'cp ./certificates/certm_worker/keys/chf/key.pem ../5g_test_ci/jcat-esc/EscTestProperties/chf/key.pem'
				} //dir
			} //steps
		} //stage

		stage('Test Statistics check') {
			steps {
				script{
					env.LOGDB="false"
					echo "Default Test Statistics setting, LOGDB= ${env.LOGDB}"
					echo "TEST_TYPE: ${env.TEST_TYPE}"
					echo "COCKPIT_CONTROL: ${env.COCKPIT_CONTROL}"

					// set Test Statistics for cockpit-controlled test and SmokeTest
					if ((env.COCKPIT_CONTROL == 'TRUE') || (env.TEST_TYPE == 'SmokeTest')) {
						env.LOGDB = "true"
						echo "Set Test Statistics setting, LOGDB= ${env.LOGDB}"
					} //if
				} //script
			} //steps
		} //stage

		stage('Set suites for CI') {
			when {
				allOf {
					environment name: 'COCKPIT_CONTROL', value: 'TRUE'
					environment name: 'TEST_TYPE', value: 'CI'
					// testng-modifyier only required for (nightly)CI.
					//   for SmokeTest: parameter COCKPIT_CONTROL doesn't exist, the suites are hardcoded in the Jenkins jobs, Spinnaker doesn't change them
					//   for StabilityTest: only env.TESTNG is needed (set in cockpit, handed over to Spinnaker, injected to Jenkins job)
					//   for TeamCI: the Cockpit is not at all used
				} // allOf
			} // when
			steps {
				dir('5g_test_ci/jcat-esc/EscTestProperties/'){
					script {
						echo "Set CI suites according to cockpit settings"

						// copy needed tool and input file from /proj/DSC_CI
						sh "cp /proj/DSC_CI/jenkins/tools/testng_modifier/testng_modifier_5G.pl ."
						sh "cp /proj/DSC_CI/jenkins/tools/testng_modifier/includes_5G.xml ."
						sh 'ls *5G*'

						echo "Cockpit settings:"
						echo "BSF:    $env.SUITE_BSF"
						echo "SCP:    $env.SUITE_SCP"
						echo "SEPP:   $env.SUITE_SEPP"
						echo "COMMON: $env.SUITE_COMMON"

						echo "generate suites:"
						if (!env.SUITE_BSF.equals("NONE")) {
							sh "./testng_modifier_5G.pl BSF $env.SUITE_BSF"
							env.TESTNG_BSF = "testng_bsf_gen.xml"
						}
						if (!env.SUITE_SEPP.equals("NONE")) {
							sh "./testng_modifier_5G.pl SEPP $env.SUITE_SEPP"
							env.TESTNG_SEPP = "testng_sepp_gen.xml"
						}
						if (!env.SUITE_SCP.equals("NONE")) {
							sh "./testng_modifier_5G.pl SCP $env.SUITE_SCP"
							env.TESTNG_SCP = "testng_scp_gen.xml"
							env.TESTNG = "testng_scp_gen.xml"
						}
						if (!env.SUITE_COMMON.equals("NONE")) {
							sh "./testng_modifier_5G.pl COMMON $env.SUITE_COMMON"
							env.TESTNG_COMMON = "testng_common_gen.xml"
						}

						echo "Suite settings:"
						echo "old SCP:$env.TESTNG"
						echo "BSF:    $env.TESTNG_BSF"
						echo "SCP:    $env.TESTNG_SCP"
						echo "SEPP:   $env.TESTNG_SEPP"
						echo "COMMON: $env.TESTNG_COMMON"
					} //script
				} // dirs
			} // steps
		} // stage

		stage('Trigger test suite(s)') {
			steps {
				dir('5g_test_ci') {
					sh "./bob/bob init"
					//withCredentials([string(credentialsId: 'd4f2bcb6-6efe-4723-89dd-6182e0e9621a', variable: 'ARM_ARTIFACTORY_TOKEN')]) {
					//	sh "./bob/bob build:fetchlibraries"
					//}
					script{
						// allocate ports and write to .bob/var.xxx files
						sh "./bob/bob adptest:ft-set-ports"
						switch (env.TEST_TYPE) {
							// all function test (ft) activities
							case [ 'SmokeTest', 'CI', 'TeamCI', 'JenkinsJob' ]:
								// prepare the <cluster>.properties file:
								if (env.IP_VERSION == '6') {
									sh "./bob/bob adptest:prepareft6"
								} else{
									sh "./bob/bob adptest:prepareft"
								} //else

								// update the file /etc/hosts:
								sh "./bob/bob adptest:update-hostfile"

								// run the test:
								sh "./bob/bob adptest:ft-set-redis-name"
								sh "./bob/bob adptest:ft-start-redis"
								sh "export TS_LOG_URL=${env.BUILD_URL}; ./bob/bob adptest:jenkinsft"
								break
							// all system test activities
							case [ 'Stability', 'Robustness', 'IaaS_Stability', 'IaaS_Stability_tools_on_separate_workers', 'Load', 'Robustness' ]:
								// if specified, set the test-duration to specified value
								sh """
									if [ ${env.DURATION} != "" ] && [ ${env.DURATION} != "null" ]
									then
										loadConfFile=\$(xmllint --xpath "string(/suite//parameter[@name='loadConfigFileName']/@value)" jcat-esc/EscTestProperties/${env.TESTNG});
										yq -i eval '.duration = ${env.DURATION}' jcat-esc/EscTestProperties/\${loadConfFile};
										echo "Test duration set to ${env.DURATION}" seconds;
									fi;
									"""
// eedcla, 11.11.22: 
//		for the time being the loadconfig file is not modified anymore. Therefore this part of the code is commented out.
//		it is not possible to use the block comment out /* */ because of the regular expression below.
//
//								switch (env.TESTNG) {
//									case ~/.*(bsf|BSF|Bsf).*/:
										// update of k6 metrics in loadConfigFile
//										echo "no update of k6 metrix required for BSF"
										// scale k6 replicas acc. to needs
										// DND-34136: Remove K6 from BSF Stability Deployments (Nov. 2022)
										//echo "scaling k6 replicas for BSF to 1:"
										//sh "kubectl --kubeconfig /home/eiffelesc/.kube/${env.KUBE_HOST}.config -n ${env.NAMESPACE} scale deployment eric-k6-deployment --replicas=1;"
//										break
//									case ~/.*(scp|SCP|Scp).*/:
										// update of k6 metrics in loadConfigFile
										// the inMemory is only for debugging purposes and should be avoided in long runs. It would be best to implement a Jenkins parameter for selection,
										// but this takes a bit more effort. For the time being the "none" choice is the best way for the pipe runs
										// update 10.08.22: SCP shall use influxDB as well now
// update 11.11.22: the default "none" shall apply again
//										sh """
//											echo "updating k6 metrix from 'none' to 'influxDB' for SCP:"
//											loadConfFile=\$(xmllint --xpath "string(/suite//parameter[@name='loadConfigFileName']/@value)" jcat-esc/EscTestProperties/${env.TESTNG});
//											echo "loadConfigFileName is: "\$loadConfFile;
//											yq -i eval '.k6.metrics = "influxDB"' jcat-esc/EscTestProperties/\${loadConfFile};
//											cat jcat-esc/EscTestProperties/\${loadConfFile};
//										"""
										// scale k6 replicas acc. to needs
										// 4 is the default. However, if BSF is the 1st suite out of a sequence then k6 needs to be scaled to the needs again
										// update 20.10.22: the test code scales the k6 pods now, so no need anymore to do it here
										//echo "scaling k6 replicas for SCP to 4:"
										//sh "kubectl --kubeconfig /home/eiffelesc/.kube/${env.KUBE_HOST}.config -n ${env.NAMESPACE} scale deployment eric-k6-deployment --replicas=4;"
//										break
//									case ~/.*(sepp|SEPP|Sepp).*/:
										// update of k6 metrics in loadConfigFile
// update 11.11.22: the default "none" shall apply again
//										sh """
//											echo "updating k6 metrix from 'none' to 'influxDB' for SEPP:"
//											loadConfFile=\$(xmllint --xpath "string(/suite//parameter[@name='loadConfigFileName']/@value)" jcat-esc/EscTestProperties/${env.TESTNG});
//											echo "loadConfigFileName is: "\$loadConfFile;
//											yq -i eval '.k6.metrics = "influxDB"' jcat-esc/EscTestProperties/\${loadConfFile};
//											cat jcat-esc/EscTestProperties/\${loadConfFile};
//										"""
										// scale k6 replicas acc. to needs
										// 4 is the default. However, if BSF is the 1st suite out of a sequence then k6 needs to be scaled to the needs again
										// update 20.10.22: the test code scales the k6 pods now, so no need anymore to do it here
										//echo "scaling k6 replicas for SEPP to 4:"
										//sh "kubectl --kubeconfig /home/eiffelesc/.kube/${env.KUBE_HOST}.config -n ${env.NAMESPACE} scale deployment eric-k6-deployment --replicas=4;"
//										break
//									default:
//										error('No NF specified in the testng file-name), aborting the build.')
//										break
//								} //switch

								// prepare the <cluster>.properties file:
								sh "./bob/bob adptest:prepareload"
								// update the file /etc/hosts:
								sh "./bob/bob adptest:update-hostfile"
								// run the test (env.TESTNG already set via Spinnaker regex):
								sh "export TS_LOG_URL=${env.BUILD_URL}; ./bob/bob adptest:jenkinsload"
								break
							default:
								error('No proper TEST_TYPE entered, aborting the build.')
								break
						} //switch
					} //script
				} //dir
			} //steps
		} //stage

		stage('Re-run failed TCs') {
			when{
				environment name: 'RERUN', value: 'TRUE'
			}
			steps {
				dir('5g_test_ci'){
						sh label: '', returnStatus: true, script: '''# define local variables
						l_TESTNG_SCP=\'""\';
						l_TESTNG_BSF=\'""\';
						l_TESTNG_SEPP=\'""\';
						l_TESTNG_COMMON=\'""\';

						if [ -f ./jcat-esc/proxy-testcases/target-scp/failsafe-reports/testng-failed.xml ]        # check if testng_failed exists
						then
							# set the local testng var to new testng file, cp testng file to WS root, and rename it
							l_TESTNG_SCP="testng-SCP-failed.xml";
							cp ./jcat-esc/proxy-testcases/target-scp/failsafe-reports/testng-failed.xml ./jcat-esc/EscTestProperties/testng-SCP-failed.xml;
							sed -i -e \'1d\' -e \'2d\' -e \'s@Failed suite \\[@@g\' -e \'s@\\]@-rerun@g\' ./jcat-esc/EscTestProperties/testng-SCP-failed.xml;
						fi;
						if [ -f ./jcat-esc/proxy-testcases/target-sepp/failsafe-reports/testng-failed.xml ]
						then
							l_TESTNG_SEPP="testng-SEPP-failed.xml";
							cp ./jcat-esc/proxy-testcases/target-sepp/failsafe-reports/testng-failed.xml ./jcat-esc/EscTestProperties/testng-SEPP-failed.xml;
							sed -i -e \'1d\' -e \'2d\' -e \'s@Failed suite \\[@@g\' -e \'s@\\]@-rerun@g\' ./jcat-esc/EscTestProperties/testng-SEPP-failed.xml;
						fi;
						if [ -f ./jcat-esc/bsf-testcases/target/failsafe-reports/testng-failed.xml ];
						then
							l_TESTNG_BSF="testng-BSF-failed.xml";
							cp ./jcat-esc/bsf-testcases/target/failsafe-reports/testng-failed.xml ./jcat-esc/EscTestProperties/testng-BSF-failed.xml;
							sed -i -e \'1d\' -e \'2d\' -e \'s@Failed suite \\[@@g\' -e \'s@\\]@-rerun@g\' ./jcat-esc/EscTestProperties/testng-BSF-failed.xml;
						fi;
						if [ -f ./jcat-esc/common-testcases/target/failsafe-reports/testng-failed.xml ];
						then
							l_TESTNG_COMMON="testng-COMMON-failed.xml";
							cp ./jcat-esc/common-testcases/target/failsafe-reports/testng-failed.xml ./jcat-esc/EscTestProperties/testng-COMMON-failed.xml;
							sed -i -e \'1d\' -e \'2d\' -e \'s@Failed suite \\[@@g\' -e \'s@\\]@-rerun@g\' ./jcat-esc/EscTestProperties/testng-COMMON-failed.xml;
						fi;

						if [ "${l_TESTNG_SCP}" != \'""\' ] || [ "${l_TESTNG_BSF}" != \'""\' ] || [ "${l_TESTNG_SEPP}" != \'""\' ] || [ "${l_TESTNG_COMMON}" != \'""\' ] ;
						then
							# set environment variables for the sub-shell and trigger the re-run
							export TESTNG_SCP=${l_TESTNG_SCP};
							export TESTNG_BSF=${l_TESTNG_BSF};
							export TESTNG_SEPP=${l_TESTNG_SEPP};
							export TESTNG_COMMON=${l_TESTNG_COMMON};
							echo "Executing re-run of failed suites";

							MONITOR_USER=`kubectl get secret eric-sc-monitor-secret --namespace $NAMESPACE --kubeconfig ~/.kube/$KUBE_HOST.config -o jsonpath=\'{.data.username}\' | base64 -d`;
							MONITOR_PWD=`kubectl get secret eric-sc-monitor-secret --namespace $NAMESPACE --kubeconfig ~/.kube/$KUBE_HOST.config -o jsonpath=\'{.data.password}\' | base64 -d`;
							MONITOR_URL=`kubectl get httpproxy --namespace $NAMESPACE --kubeconfig ~/.kube/$KUBE_HOST.config | grep httpprox  | awk \'{print $2}\'`;
							MONITOR_PORT=`kubectl get services eric-tm-ingress-controller-cr --namespace $NAMESPACE --kubeconfig ~/.kube/$KUBE_HOST.config -o jsonpath="{.spec.ports[1].nodePort}" `;
							echo $MONITOR_USER;
							echo $MONITOR_PWD;
							echo $MONITOR_URL;
							echo $MONITOR_PORT;
							LIST_WORKERS=`kubectl --kubeconfig ~/.kube/$KUBE_HOST.config --namespace $NAMESPACE get pods | grep worker | awk \'{print $1}\'`;
							for i in ${LIST_WORKERS}
							do
								if [[ $i == *"bsf"* ]];
								then
									curl -k -u ${MONITOR_USER}:${MONITOR_PWD} -X PUT "https://${MONITOR_URL}:${MONITOR_PORT}/monitor/api/v0/commands?target=eric-bsf-w&command=log&logger=ROOT&level=DEBUG" > file_a.txt;
									curl -k -u ${MONITOR_USER}:${MONITOR_PWD} -X PUT "https://${MONITOR_URL}:${MONITOR_PORT}/monitor/api/v0/commands?target=eric-bsf-w&command=log&logger=com.ericsson.esc.bsf.worker.BsfWorker&level=DEBUG" > file_b.txt;
									curl -k -u ${MONITOR_USER}:${MONITOR_PWD} -X PUT "https://${MONITOR_URL}:${MONITOR_PORT}/monitor/api/v0/commands?target=eric-bsf&command=log&logger=com.ericsson.esc.bsf&level=DEBUG" > file_c.txt;
								else
									j=${i%%w*}
									kubectl --kubeconfig ~/.kube/$KUBE_HOST.config --namespace $NAMESPACE get pods | grep worker;
									kubectl --kubeconfig ~/.kube/$KUBE_HOST.config --namespace $NAMESPACE exec -ti $i -c ${j}worker -- curl http://localhost:9901/config_dump > $i-config;
									kubectl --kubeconfig ~/.kube/$KUBE_HOST.config --namespace $NAMESPACE exec -ti $i -c ${j}worker -- curl -X POST http://localhost:9901/logging?level=debug;
									kubectl --kubeconfig ~/.kube/$KUBE_HOST.config --namespace $NAMESPACE logs $i ${j}worker > $i-log-DEBUG;
								fi
							done;

							export TS_LOG_URL=$BUILD_URL;
							./bob/bob adptest:jenkinsft;

							for i in ${LIST_WORKERS}
							do
								if [[ $i == *"bsf"* ]];
								then
									curl -k -u ${MONITOR_USER}:${MONITOR_PWD} -X PUT "https://${MONITOR_URL}:${MONITOR_PORT}/monitor/api/v0/commands?target=eric-bsf-w&command=log&logger=ROOT&level=INFO" > file_d.txt;
									curl -k -u ${MONITOR_USER}:${MONITOR_PWD} -X PUT "https://${MONITOR_URL}:${MONITOR_PORT}/monitor/api/v0/commands?target=eric-bsf-w&command=log&logger=com.ericsson.esc.bsf.worker.BsfWorker&level=INFO" > file_e.txt;
									curl -k -u ${MONITOR_USER}:${MONITOR_PWD} -X PUT "https://${MONITOR_URL}:${MONITOR_PORT}/monitor/api/v0/commands?target=eric-bsf&command=log&logger=com.ericsson.esc.bsf&level=INFO" > file_f.txt;
								else
									j=${i%%w*}
									kubectl --kubeconfig ~/.kube/$KUBE_HOST.config --namespace $NAMESPACE get pods | grep worker;
									kubectl --kubeconfig ~/.kube/$KUBE_HOST.config --namespace $NAMESPACE exec -ti $i -c ${j}worker -- curl http://localhost:9901/config_dump > $i-config;
									kubectl --kubeconfig ~/.kube/$KUBE_HOST.config --namespace $NAMESPACE exec -ti $i -c ${j}worker -- curl -X POST http://localhost:9901/logging?level=info;
									kubectl --kubeconfig ~/.kube/$KUBE_HOST.config --namespace $NAMESPACE logs $i ${j}worker > $i-log-INFO;
								fi
							done;
						else
							echo "All TCs were successful, no re-run triggered";
						fi;'''

				} //dir
			} //steps
		} //stage

		stage('Checking test results') {
			steps{
				dir('5g_test_ci'){
					script{
						statusCode = sh returnStatus: true, script: '''
							FAILED=0;
							SKIPPED=0;

							if [ ! -z "$TESTNG" ] &&  [ -z "$TESTNG_BSF" ] && [ -z "$TESTNG_SCP" ] && [ -z "$TESTNG_SEPP" ] && [ -z "$TESTNG_COMMON" ];
							then
								RESULTS=0; #set RESULTS=0 in case of single suite, for instance Stability/Robustness
							else
								RESULTS=1; #set RESULTS=1 in case of multiple suites or SmokeTest
							fi;
                                                        
							NFPATH=("$TESTNG_COMMON" "./jcat-esc/common-testcases/target" "$TESTNG_SCP" "./jcat-esc/proxy-testcases/target-scp" "$TESTNG_SEPP" "./jcat-esc/proxy-testcases/target-sepp" "$TESTNG_BSF" "./jcat-esc/bsf-testcases/target") 
							for ((i=0; i<${#NFPATH[*]}; i=i+2));
							do

								if [ ! -z "${NFPATH[$i]}" ] && [ "${NFPATH[$i]}" != "NONE" ] && [ "${NFPATH[$i]}" != "\\"\\"" ] && [ ! -f ${NFPATH[$i+1]}/failsafe-reports/testng-results.xml ];
								then
									RESULTS=0;
									echo "Testng result file ${NFPATH[$i+1]}/failsafe-reports/testng-results.xml not found"; 
								fi;     

								if [ "$FAILED" -eq 0 ] && [ "$SKIPPED" -eq 0 ];
								then
									if [ -f ${NFPATH[$i+1]}/failsafe-reports/testng-results.xml ];
									then
										FAILED=$(grep -oE "failed=\\"[0-9]{1,}\\"" ${NFPATH[$i+1]}/failsafe-reports/testng-results.xml | cut -d\'=\' -f2 | sed "s/\\"//g");
										SKIPPED=$(grep -oE "skipped=\\"[0-9]{1,}\\"" ${NFPATH[$i+1]}/failsafe-reports/testng-results.xml | cut -d\'=\' -f2 | sed "s/\\"//g");

										if [ ! -z "$TESTNG" ] &&  [ -z "$TESTNG_BSF" ] && [ -z "$TESTNG_SCP" ] && [ -z "$TESTNG_SEPP" ] && [ -z "$TESTNG_COMMON" ];
										then
											RESULTS=1;
										fi;
									fi;
								fi;
							done;

							if [ "$FAILED" -eq 0 ] && [ "$SKIPPED" -eq 0 ] && [ "$RESULTS" -eq 1 ];
							then
								echo "All TCs were successful";
								echo "TROUBLESHOOTING=FALSE" > ../concl.properties;
								if [ $TEST_TYPE == "Stability" ] || [ $TEST_TYPE == "Robustness" ]
								then
									echo "Collecting logs for archiving ...";
									../scripts/collect_ADP_logs.sh -c ~/.kube/${KUBE_HOST}.config -n ${NAMESPACE};
								fi;
							else
								echo "Obviously some TCs failed!!";
								echo "TROUBLESHOOTING=TRUE" > ../concl.properties;
								../scripts/collect_ADP_logs.sh -c ~/.kube/${KUBE_HOST}.config -n ${NAMESPACE};
							fi;

							echo "TEST_BRANCH=${TEST_BRANCH}" >> ../concl.properties;
							echo "TEST_COMMIT=$(git log -n1 --abbrev-commit --format="%h")" >> ../concl.properties; 

							if [ "$RESULTS" -eq 0 ];
							then
								echo "Obviously a Test suite and TCs failed!!";
								exit 1;
							fi;'''
						if (statusCode != 0) { currentBuild.result = 'UNSTABLE' }
					} //script
				} //dir
				// eedmti_smoke: archiveArtifacts allowEmptyArchive: true, artifacts: 'concl.properties', onlyIfSuccessful: false
				archiveArtifacts allowEmptyArchive: true, artifacts: '5g_test_ci/logs_${NAMESPACE}_*.tgz', onlyIfSuccessful: false
			} //steps
		} //stage

		stage('Set verdict') {
			steps {
				script {
					def exists = fileExists 'concl.properties'
					catchError(buildResult: 'SUCCESS', message: 'TROUBLESHOOTING = TRUE', stageResult: 'UNSTABLE') {
						if (exists) {
							def data = readFile(file: 'concl.properties')
							if (data.contains("TROUBLESHOOTING=FALSE")) {
								email_color = "<body style=\"background-color: LightGreen;\">";				// body in LightGreen
								email_verdict = "Verdict: <b><font color=\"#228B22\"> Success </font></b>"	// in green
							} else {
								email_color = "<body style=\"background-color: Salmon;\">";					// body in Salmon
								email_verdict = "Verdict: <b><font color=\"#FF0000\"> Failed </font></b>"	// in red
								error "Verdict: Failed";
							} //if
						} else {
							email_verdict = "Verdict: <b>undefined</b>"
							error "Verdict: Undefined";
						} //if
					} //catcherror
					// update concl.properties (eedmti_smoke)
					readContent = readFile 'concl.properties'
					writeFile file: 'concl.properties', text: readContent +"\rVERDICT=$email_verdict"
				} //script

			} //steps
		} //stage

		stage('Archiving JCAT logs') {
			environment{
				DIRS=0
			}
			steps{
				dir('5g_test_ci') {
					script{
						// copy jcat reports and count them
						// To prevent the day 01 of the month issue, the current day is printed and checked.
						// To simplify the date arithmetic, the current day is counted in seconds since 1/1/1970.
						// Substracting 86400s (24h/1day) allows to return the date of the previous day.
						// The rest is directory listing, creation and copy operations.
						DIRS = sh (
							script: ''' #!/bin/bash -xe

									export DOM=`date +%d`;
									export LOG_DIRS="";

									if [ "$DOM" == "01"  ];
									then
										export DOM_SEC=`date +%s`;
										YESTERDAY_SEC=$(( ${DOM_SEC} - 86400));
										export LAST_MONTH=`date --date="@${YESTERDAY_SEC}" +%Y%m`;

										for DIR in `ls ./jcat-esc/jcatlogs/${LAST_MONTH}`;
										do
										LOG_DIRS="$LOG_DIRS ./jcat-esc/jcatlogs/${LAST_MONTH}/$DIR";
										done;
									fi;

									CUR_MONTH=`date +%Y%m`;

									for DIR in `ls ./jcat-esc/jcatlogs/${CUR_MONTH}`;
									do
										LOG_DIRS="$LOG_DIRS ./jcat-esc/jcatlogs/${CUR_MONTH}/$DIR";
									done;

									j=1;

									for i in ${LOG_DIRS};
									do
										mkdir ./latestjcatlogs_$j ;
										cp -R ./$i/. ./latestjcatlogs_$j/. ;
										j=$((j+1)) ;
									done;

									echo $j;''' ,
							returnStdout: true).trim()

						last1 = "${DIRS}"
						echo "$last1"

						// define the lists
						def SUITES=[]
						def INFO=[]

						// retrieve information from the dt.xml files from the latestjcatlog_$x dirs
						for (def x=1;x<"$last1".toInteger();x++) {
							SUITES.add(retrieveSuite("$x"))
							//INFO.add(retrieveInfo("$x").replace("ExecutionFinished",""))
							INFO.add(retrieveInfo("$x"))
						}

						// set the base data for the html report
						def baseReportDir="latestjcatlogs_"
						def reportdir=""
						def baseReportName="jcatReport_"
						def reportName=""

						// for each run
						for (def x=1;x<"$last1".toInteger();x++) {
							// set info for the run into the email
							script{
								email_jcatlogs = email_jcatlogs + INFO.get(x-1) + "<br>"
							} //script

							reportDir="$baseReportDir"+"$x" // add suite nr to basedir
							//echo "$reportDir"

							reportName=SUITES.get(x-1)      // reportname is suite name
							//echo "$reportName"

							indexName="index.html"          // index name
							//echo "$indexName"

							publishHTML([allowMissing: false,
								alwaysLinkToLastBuild: false,
								keepAll: true,
								reportDir: "$reportDir",
								reportFiles: "$indexName",
								reportName: "$reportName",
								reportTitles: "$reportName",
								escapeUnderscores: false])
						} //for

						// publish charts for any load test activity
						if (env.TEST_TYPE == 'Stability' || env.TEST_TYPE == 'Robustness' || env.TEST_TYPE == 'IaaS_Stability' || env.TEST_TYPE == 'IaaS_Stability_tools_on_separate_workers' || env.TEST_TYPE == 'Load') {
							publishHTML([allowMissing: true,
								alwaysLinkToLastBuild: false,
								keepAll: true,
								reportDir: "jcat-esc/jcatlogs/charts",
								reportFiles: "charts.html",
								reportName: "Load Charts",
								reportTitles: "Load Charts",
								escapeUnderscores: false])
						} //if

						// update concl.properties (eedmti_smoke)
						email_jcatlog_props = email_jcatlogs.replaceAll("\n","") // remove the \n
						readContent = readFile '../concl.properties'
						writeFile file: '../concl.properties', text: readContent +"\rJCAT_LOGS=$email_jcatlog_props"+"\rBUILD_URL=$email_buildUrl"

						// delete jcatlogs_x after publishing (clean-up of workspace) 
						sh 'rm -rf latestjcatlogs_*'

					} //script
				} //dir

				archiveArtifacts allowEmptyArchive: true, artifacts: 'concl.properties', onlyIfSuccessful: false                    // eedmti_smoke

			} //steps
		} //stage
	} //stages

	post{
		// Run the steps in the post section regardless of the completion status of the Pipelineâ€™s or stageâ€™s run.
		always{
			script{
				if ( fileExists("/proj/DSC_CI/jenkins/tools/help_scripts/concurrentBuilds.sh") ) {
					// Log current job
					sh "/proj/DSC_CI/jenkins/tools/help_scripts/concurrentBuilds.sh -stop"
				} // if
			} // script

			// releasing ports applies to all test-types
			dir('5g_test_ci') {
				echo("Releasing ports ...")
				sh "./bob/bob adptest:ft-release-ports"
			} //dir

			echo "TEST_TYPE is $TEST_TYPE"

			script {
				// verify if we have committers in the changeSet
				if (env.T_COMMITTERS == null){
					echo "No committers found"
					COMMITTERS="No Committers found"
					EMAIL_COMMITTERS=""
				} //if
				else{
					COMMITTERS = env.T_COMMITTERS
					EMAIL_COMMITTERS = env.T_EMAIL_COMMITTERS
				}
			} //script

			echo "TEST COMMITTER(S): ${COMMITTERS}"
			echo "EMAIL TEST COMMITTER(S): ${EMAIL_COMMITTERS}"

			script {
				switch (env.TEST_TYPE) {
					case ['SmokeTest']:
/*						script{
							// only send mail for smoketest
							emailext body: "${email_color}${email_text1}<br>${email_verdict}<br><br>${email_jcatlogs}<br>${email_buildUrl}<br><br>${email_text2}",
							mimeType: 'text/html',
							subject: "5G Smoke test results for  ${env.PROJECT} : ${COMMIT}",
							to: "${EMAIL_COMMITTER}, DSCAteam@ericsson.onmicrosoft.com, f37b6763.ericsson.onmicrosoft.com@emea.teams.ms"  //Teams: Master State
							// no break here because the removal of redis below shall happen also
						} //script*/
						// mail sending move to external job (eedmti)
					case ['CI', 'TeamCI', 'JenkinsJob']:
						dir('5g_test_ci') {
							echo("Removing redis ...")
							sh "./bob/bob adptest:ft-remove-redis"
						} //dir
						break
					case ['Stability', 'Robustness', 'IaaS_Stability', 'IaaS_Stability_tools_on_separate_workers', 'Load']:
						//echo("Nothing to do here (yet)...")
						break
					default:
						//echo("Nothing to do here ...")
						break
				} //switch
			} //script
		} //always

		// Only run the steps in post if the current Pipelineâ€™s or stageâ€™s run has a "failed" status, typically denoted by red in the web UI.
		failure{
			script {
				// clean cluster due to error in pipeline
				if (env.CLEANCLUSTER.equals("TRUE")) {
					sh "/home/eiffelesc/scripts/cleanCluster `cat .bob/var.namespace` ${env.KUBE_HOST}"
				}

				switch (env.TEST_TYPE) {
					case ['SmokeTest']:
						// send mail to dev committer in case of job failure
						def DEV_COMMITTER = """${sh(returnStdout: true, script: 'git log --format="%cN" | head -1')}""".trim()
						def DEV_EMAIL_COMMITTER = """${sh(returnStdout: true, script: 'git log --format="%cE" | head -1')}""".trim()
						def COLOR = "<body style=\"background-color: LightYellow;\">"

						echo "Sending email to ${DEV_COMMITTER} ${COMMITTERS} via ${DEV_EMAIL_COMMITTER} ${EMAIL_COMMITTERS} due to pipeline failure"

						emailext body: "$COLOR " + "Hi ${DEV_COMMITTER},<p> the pipeline failed, maybe due to your commit? <p> Please check:<br> ${email_buildUrl}<br><br>${email_text2}",
							mimeType: 'text/html',
							subject: "5G Smoke test failed for  ${env.PROJECT} : ${COMMIT}",
							to: "${DEV_EMAIL_COMMITTER}, ${EMAIL_COMMITTERS}, DSCAteam@ericsson.onmicrosoft.com, f37b6763.ericsson.onmicrosoft.com@emea.teams.ms"  //Teams: Master Channel
							//to: "${DEV_EMAIL_COMMITTER}, ${EMAIL_COMMITTERS}, DSCAteam@ericsson.onmicrosoft.com, 7352f6e6.ericsson.onmicrosoft.com@emea.teams.ms"  //Teams: Release Channel
						break
					default:
						echo('The pipeline failed!')
						break
				} //switch
			} //script
		} //failure

		// Only run the steps in post if the current Pipelineâ€™s run has an "unstable" status, usually caused by test failures, code violations, etc. 
		// This is typically denoted by yellow in the web UI.
		unstable{
			script {
				switch (env.TEST_TYPE) {
					case ['SmokeTest']:
						// clean cluster due to a NF compilation error, smoke test only; for CI the next test execution is to start
						if (env.CLEANCLUSTER.equals("TRUE")) {
							sh "/home/eiffelesc/scripts/cleanCluster `cat .bob/var.namespace` ${env.KUBE_HOST}"
						}

						// send mail to dev committer in case of job failure
						def DEV_COMMITTER = """${sh(returnStdout: true, script: 'git log --format="%cN" | head -1')}""".trim()
						def DEV_EMAIL_COMMITTER = """${sh(returnStdout: true, script: 'git log --format="%cE" | head -1')}""".trim()
						def COLOR = "<body style=\"background-color: LightYellow;\">"

						echo "Sending email to ${DEV_COMMITTER} ${COMMITTERS} via ${DEV_EMAIL_COMMITTER} ${EMAIL_COMMITTERS} due to pipeline failure"

						emailext body: "$COLOR " + "Hi ${DEV_COMMITTER},<p> the pipeline failed, maybe due to your commit? <p> Please check:<br> ${email_buildUrl}<br><br>${email_text2}",
							mimeType: 'text/html',
							subject: "5G Smoke test failed for  ${env.PROJECT} : ${COMMIT}",
							to: "${DEV_EMAIL_COMMITTER}, ${EMAIL_COMMITTERS}, DSCAteam@ericsson.onmicrosoft.com, f37b6763.ericsson.onmicrosoft.com@emea.teams.ms"  //Teams: Master Channel
							//to: "${DEV_EMAIL_COMMITTER}, ${EMAIL_COMMITTERS}, DSCAteam@ericsson.onmicrosoft.com, 7352f6e6.ericsson.onmicrosoft.com@emea.teams.ms"  //Teams: Release Channel
						break
					default:
						echo('The pipeline failed!')
						break
				} //switch
			} //script
		} //unstable
	} //post
} //pipeline


