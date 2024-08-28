
//////////////////////////////////////////////////////////////////////////
//   Subroutines

String findCommitters() {

	catchError(buildResult: currentBuild.result, message: 'Error in subroutine findCommitters', stageResult: 'UNSTABLE') {
		// get the changes for the actual build from URL and store it in a json file
		sh "curl --netrc  ${env.BUILD_URL}api/json -o culprits.json"

		// get the authors' full name or eMail from the json file, and remove ][ brackets from the string
		sh 'cat culprits.json | jq -c \'[.changeSets[].items[].author.fullName]\' | tr -d "][" > culprits_tmp.txt';
		sh 'cat culprits.json | jq -c \'[.changeSets[].items[].authorEmail]\' | tr -d "][" > culprits_email_tmp.txt';
		//sh 'cat culprits_tmp.txt';
		//sh 'cat culprits_email_tmp.txt';

		// remove (eE)iffel commit
		//    awk: print each committer if string doesn't contain (eE)iffel, also remove double-squotes; this introduces line breaks
		//    sort -u: remove double entries
		sh 'cat culprits_tmp.txt | awk -F"," \'{for (i=1; i<=NF; i++) {if($i !~ ".iffel"){gsub("\\"",""); print $i}}}\' | sort -u > culprits.txt; rm -f culprits_tmp.txt'
		sh 'cat culprits_email_tmp.txt | awk -F"," \'{for (i=1; i<=NF; i++) {if($i !~ ".iffel"){gsub("\\"",""); print $i}}}\' | sort -u > culprits_email.txt; rm -f culprits_email_tmp.txt'

		//sh 'echo "Culprits: "; cat culprits.txt; echo "eMails: "; cat culprits_email.txt'
	} //catchError
}

//  end subroutines
//////////////////////////////////////////////////////////////////////////



if (env.AGENT_LABEL != null && env.AGENT_LABEL != '') {
	AGENT_LABEL = "${env.AGENT_LABEL}"
} else {
	AGENT_LABEL = '5G-SC'
}

@Library('Shared-Libs-SC') _

pipeline {
	agent {
		node {
			label "${AGENT_LABEL}"
		}
	}
	options{
		timeout(time: 90, unit: 'MINUTES')
		timestamps()
	}
	environment{
		// Variables needed for the ruleset
		BSF = true
		CSA = false
		NLF = true
		SCP = true
		SLF = true
		RLF = true
		SEPP = true
		BSF_TLS = false 
		SCP_TLS = false
		LOGGING = true
		PRODUCTION = true
		RESOURCES = "full"

		// Variables only used in Build-job

		//CHART_NAME = 'eric-sc'															// is hardcoded in the Spinnaker-trigger job
		//CHART_REPO = 'https://armdocker.rnd.ericsson.se/artifactory/proj-5g-bsf-helm'		// is hardcoded in the Spinnaker-trigger job
		KUBE_HOST = "${env.KUBE_HOST}"
		//NAMESPACE = "${env.NAMESPACE}"					// not needed for the build, set in the KickOff job for the complete pipelien
		//DEVPIPE = "${env.DEVPIPE}"						// only used in conclude-job, set in Spinnaker
		TEAMNAME = "${env.TEAMNAME}"						//  
//		PIPE = "${env.PIPE}"								//
		PROJECT = "${env.PROJECT}"							// only used in Smoke-build, not in TeamCI-build
		ARTIFACTORY_TOKEN = "${env.ARTIFACTORY_TOKEN}"

		// COMMIT not set here but below in the pipleine in order to get the commit hash.
	}

	stages {
		
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

		stage('Init Environment'){
			steps{
				echo 'Executing: bob init'
				echo pwd()
				//sh 'ls -ltrh'
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
				sh './bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml init:chfsim-version'
				sh './bob/bob -r rulesets/ruleset2.0-eric-seppsim.yaml init:version init:seppsim-cxu-number init:seppsim-cxu-rev-number'
				sh './bob/bob -r rulesets/ruleset2.0-eric-nrfsim.yaml init:version init:nrfsim-cxu-number init:nrfsim-cxu-rev-number'
			
				// the esc-version is available after bob init
				script{
					// the commit is the one that has been checked out by Jenkins
					//    if COMMIT was set to "origin/branch" then COMMIT gets the hash here (latest of the branch)
					//    if COMMIT was set to a hash already then it gets the same value again here
					env.COMMIT = sh( returnStdout: true, script: "git log --format=\"%h\" | head -1 | tr -d '\n'")
					env.CHART_VERSION = sh( returnStdout: true, script: "cat ./.bob/var.esc-version | tr -d '\n'")
					// findCommitters() writes commiter names und eMails to files
					findCommitters();
					env.COMMITTER = sh( returnStdout: true, script: "cat culprits.txt | sed ':a;N;\$!ba;s/\\n/, /g' ")
					env.EMAIL_COMMITTER = sh( returnStdout: true, script: "cat culprits_email.txt | sed ':a;N;\$!ba;s/\\n/, /g' ")
					// sed: see https://stackoverflow.com/questions/1251999/how-can-i-replace-each-newline-n-with-a-space-using-sed

					// set the displayed build name to "BUILD_NUMBER - COMMIT"
					currentBuild.displayName = "#${env.BUILD_NUMBER} - ${env.COMMIT}"
					// set displayed description to "NODE_NAME, KUBE_HOST, CHART_VERSION"
					currentBuild.description = "${env.NODE_NAME}, ${env.KUBE_HOST}, ${env.CHART_VERSION}";
				} // script
				sh 'printenv | sort'
			} // steps
		} // stage

		stage('Build Source Code'){
			steps{
				echo 'Running: bob build actions'
				sh './bob/bob build:esc-parent'
				sh './bob/bob build:logfwdr'
				sh './bob/bob build:copy-ericsson-libs-to-user-mvn-repo'
			} // steps
		} // stage
/*
		stage('Test Helm Chart Design Rules'){
			steps{
				echo 'Executing: bob test -rule-dry-release --namespace adp-staging-designrule-check'
			}
		}
*/
		stage('Generate JSON Schemas and Yang Archives for BSF and SEPP'){
			steps{
				script {
					def comps = ['BSF', 'SEPP']
					for (int i = 0; i < comps.size(); ++i) {
						env.COMPONENT = "${comps[i]}"
						echo "Component is: ${env.COMPONENT}"
						echo "Executing: Generating JSON Schemas and Yang Archives for ${comps[i]} Manager"
						sh """
							./bob/bob -r rulesets/ruleset2.0-yang.yaml init;
							./bob/bob -r rulesets/ruleset2.0-yang.yaml generate;
							./bob/bob -r rulesets/ruleset2.0-yang.yaml copy-output;
						"""
					} // for
				} // script
			} // steps
		} // stage

		stage('Build and Push Docker Images'){
			steps {
				parallel (
					"bsf" : {
								sh './bob/bob build-images:bsf push-images:bsf build-images:bsf-diameter push-images:bsf-diameter'
					},
					"nlf" : {
								sh './bob/bob build-images:nlf push-images:nlf'
					},					
					"scp" : {
								sh './bob/bob build-images:scp push-images:scp'
					},
					"sepp" : {
								sh './bob/bob build-images:sepp push-images:sepp'
					},
					"slf" : {
								sh './bob/bob build-images:slf push-images:slf'
					},
					"rlf" : {
								sh './bob/bob build-images:rlf push-images:rlf'
					},
					"certnotifier" : {
								sh './bob/bob build-images:certnotifier push-images:certnotifier'
					},
					"logfwdr" : {
								// The logforwarder is built by calling the "build" rule in the "Build Source Code" stage.
								sh './bob/bob push-images:logfwdr'
					},
					"sds" : {
								sh './bob/bob build-images:sds push-images:sds'
					},
					"monitor" : {
								sh './bob/bob build-images:monitor push-images:monitor'
					},
					"tapagent" : {
								sh './bob/bob push-images:tapagent'
					},
					"tapcollector" : {

								sh './bob/bob build-images:tapcollector push-images:tapcollector'
					},
					"tlskeylogagent" : {
									sh './bob/bob build-images:tlskeylogagent push-images:tlskeylogagent'
					}
				) // parallel
			} // steps
		} //stage

		stage('Update Helm Chart Values'){
			steps{
				echo 'Executing: bob update-helm'
				sh 'ls -ltrh; ./bob/bob update-helm'
			} // steps
		} // stage

		stage('Generate JSON Schemas and Yang Archives for SCP and PVTB'){
			steps{
				script {
					def comps = ['SCP', 'PVTB']
					for (int i = 0; i < comps.size(); ++i) {
						env.COMPONENT = "${comps[i]}"
						echo "Component is: ${env.COMPONENT}"
						echo "Executing: Generating JSON Schemas and Yang Archives for ${comps[i]} Manager"
						sh """
							./bob/bob -r rulesets/ruleset2.0-yang.yaml init;
							./bob/bob -r rulesets/ruleset2.0-yang.yaml generate;
							./bob/bob -r rulesets/ruleset2.0-yang.yaml copy-output;
						"""
					} // for
				} // script
			} // steps
		} // stage

		stage('Lint Markdown and Helm'){
			steps{
				echo 'Executing: bob lint'
				sh 'ls -ltrh; ./bob/bob lint'
			} // steps
		} // stage

		stage('Create Helm Package'){
			steps{
				echo 'Executing: bob package-helm'
				sh 'ls -ltrh; ./bob/bob package-helm'
			} // steps
		} // stage

		stage('Push Helm chart'){
			steps{
				echo 'Executing: bob push-helm'
				sh "./bob/bob push-helm"
			} // steps
		} // stage

		stage('Create Umbrella Package') {
			steps {
				echo 'Executing: bob update-umbrella'
				sh "./bob/bob update-umbrella"
			} // steps
		} // stage

		stage('Build Simulator tools'){
			steps {
				echo 'Building CHFsim:'
				sh """
					./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml build;
					./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml image:build-eric-chfsim
					./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml package:copy-helm-chart-chfsim;"""
				// script {
				// 	if (!(env.TEST_TYPE == 'Stability' || env.TEST_TYPE == 'Robustness')) {
				// 		sh """
				// 			./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml tools:remove-deployments-non-stability
				// 		"""
				// 	} // script
				// }
				sh """
				    ./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml package:helm-chart-chfsim;
					./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml push:docker-image-chfsim;
					./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml push:helm-chart-chfsim;"""
				echo 'Building SEPPsim:'
				sh """
					./bob/bob -r rulesets/ruleset2.0-eric-seppsim.yaml build;
					./bob/bob -r rulesets/ruleset2.0-eric-seppsim.yaml image;
					./bob/bob -r rulesets/ruleset2.0-eric-seppsim.yaml package-full;
					./bob/bob -r rulesets/ruleset2.0-eric-seppsim.yaml push:docker-image;				
					./bob/bob -r rulesets/ruleset2.0-eric-seppsim.yaml push:helm-chart;"""
				echo 'Building NRFsim:'
				sh """
					./bob/bob -r rulesets/ruleset2.0-eric-nrfsim.yaml build;
					./bob/bob -r rulesets/ruleset2.0-eric-nrfsim.yaml image;
					./bob/bob -r rulesets/ruleset2.0-eric-nrfsim.yaml package-full;
					./bob/bob -r rulesets/ruleset2.0-eric-nrfsim.yaml push:docker-image;
					./bob/bob -r rulesets/ruleset2.0-eric-nrfsim.yaml push:helm-chart;"""
			} // steps
		} // stage

		stage('Cleanup & Archiving') {
			steps {
				sh """
					echo "Deleting docker images:"
					docker images -a | grep ${env.CHART_VERSION};
					docker rmi -f \$(docker images -q --filter=reference="armdocker.rnd.ericsson.se/proj-5g-bsf/eiffelesc/*:${env.CHART_VERSION}") || true;
					docker images -a | grep ${env.CHART_VERSION} || true;
				"""
				sh """
					echo "Archiving parameters for downstream:";
					echo "CHART_VERSION=$CHART_VERSION" > deploy.properties
				"""
				archiveArtifacts artifacts: 'deploy.properties', onlyIfSuccessful: true
			} // steps
		} // stage

		stage('Trigger Spinnaker Pipeline'){
			when{
				beforeAgent true
				allOf {
					expression { env.TEST_TYPE == "SmokeTest" }
 					anyOf {
						expression { env.COMMITTER != "jnkadm" }
						expression { env.COMMITTER != "eiffeldsc" }
					 } // anyOf
				} // allOf
			} // when

			steps{
				build job: "2-Spinnaker-trigger-${env.TEST_TYPE}-${env.TEAMNAME}",
					parameters: [
						string(name: 'CHART_VERSION', value: "${env.CHART_VERSION}"), 
						string(name: 'TEAMNAME', value: "${env.TEAMNAME}"),
						string(name: 'BRANCH', value: "${env.BRANCH}"),
						string(name: 'COMMIT', value: "${env.COMMIT}"),
						string(name: 'COMMITTER', value: "${env.COMMITTER}"),
						string(name: 'EMAIL_COMMITTER', value: "${env.EMAIL_COMMITTER}"),
						string(name: 'PROJECT', value: "${env.PROJECT}"),
						string(name: 'KUBE_HOST', value: "${env.KUBE_HOST}")],

					  wait: false
			} // steps
		} // stage
	} // stages

	post{
		failure {
			script {
				// only send mail for smoketest
				if ( env.TEST_TYPE == 'SmokeTest' ) {
					COLOR = "<body style=\"background-color: LightYellow;\">"

					//echo "${env.PROJECT}"
					//echo "${env.COMMIT}"
					//echo "${env.COMMITTER}"
					//echo "${env.EMAIL_COMMITTER}"

					emailext body: "$COLOR" + "Hi ${env.COMMITTER},\n" +
											  "<p>the build failed, maybe due to your commit?\n" +
											  "<p>Please check:\n" +
											  "<br>${env.BUILD_URL}\n" +
											  "<br><br>Thank you for pushing.\n" +
											  "<br><br>BR,<br>A-Team",

						mimeType: 'text/html',
						subject: "5G Smoke-test build failed for ${env.PROJECT} : ${env.COMMIT}",
						to: "${env.EMAIL_COMMITTER}, ${env.EMAIL_OTHER}"  // mail to committers and respective Teams channel
						//to: "${env.EMAIL_COMMITTER}, DSCAteam@ericsson.onmicrosoft.com, f37b6763.ericsson.onmicrosoft.com@emea.teams.ms"  //Teams: Master Channel
						//to: "${env.EMAIL_COMMITTER}, DSCAteam@ericsson.onmicrosoft.com, 7352f6e6.ericsson.onmicrosoft.com@emea.teams.ms"  //Teams: Release Channel
				} //if
			} //script
		} //failure
	} //post
} //pipeline

