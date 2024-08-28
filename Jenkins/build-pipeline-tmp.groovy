
//////////////////////////////////////////////////////////////////////////
//   Subroutines

String findCommitters(){

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

	echo "Sub: $committers"
	return (committers)
}

String findCommittersEmail(){

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

	echo "Sub: $committers_email"
	return (committers_email)
}

//  end subroutines
//////////////////////////////////////////////////////////////////////////

pipeline {
	agent {
		node {
			label '5G-SC'
		}
	}
	options{
		timeout(time: 90, unit: 'MINUTES')
		timestamps()
	}
	environment{
		// Variables needed for the ruleset
		BSF = true
		BSF_DIAMETER = true
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

	stages{
		stage('Cleaning WS on Buildslaves'){
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

			} // steps
		} // stage

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
					//		if COMMIT was set to "origin/branch" then COMMIT gets the hash here (latest of the branch)
					//		if COMMIT was set to a hash already then it gets the same value again here
					env.COMMIT = sh( returnStdout: true, script: "git log --format=\"%h\" | head -1 | tr -d '\n'")
					env.COMMITTER = findCommitters()
					env.EMAIL_COMMITTER = findCommittersEmail()
					env.CHART_VERSION = sh( returnStdout: true, script: "cat ./.bob/var.esc-version | tr -d '\n'")

					// set the displayed build name to "BUILD_NUMBER - COMMIT"
					currentBuild.displayName = "#${env.BUILD_NUMBER} - ${env.COMMIT}"
					// set displayed description to "NODE_NAME, KUBE_HOST, CHART_VERSION"
					currentBuild.description = "${env.NODE_NAME}, ${env.KUBE_HOST}, ${env.CHART_VERSION}";
				} // script
				sh 'printenv | sort'
			} // steps
		} // stage

/*
		stage("check for errors in eric-sc-values/sc_vnf_descriptor"){
			steps{
				withCredentials([usernamePassword(credentialsId: '4d8761fe-e925-4638-bf28-97744f277be6', usernameVariable: 'USER', passwordVariable: 'PASSWORD')]) {
					 sh ''' echo $USER > .bob/var.username ; echo $PASSWORD > .bob/var.password '''
					 sh '''./bob/bob run-check_values'''
				}
			} //steps
		} //stage

		stage('Clean'){
			steps{
				echo 'Executing: bob clean'
			}
		}
		stage('Generate version'){
			steps{
				echo 'Executing: bob version'
			}
		}
		stage('Generate docs'){
			steps{
				echo 'Executing: bob generate'
			}
		}
*/
		stage('Build Source Code'){
			steps{
				echo 'Running: bob build actions'
				sh 'ls -ltrh;'
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
		stage('Build and Push Docker Images'){
			steps {
				parallel (
					"bsf" : {
								sh './bob/bob build-images:bsf push-images:bsf'
                                sh './bob/bob build-images:bsf-diameter push-images:bsf-diameter'
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

		stage('Generate JSON Schemas and Yang Archives'){
			steps{
				script {
					def comps = ['BSF', 'SCP', 'SEPP', 'PVTB']
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
					./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml image:build-eric-chfsim;
					./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml package:copy-helm-chart-chfsim;
					./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml package:helm-chart-chfsim;
					./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml push:docker-image-chfsim;
					./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml push:helm-chart-chfsim;
				"""
				echo 'Building SEPPsim:'
				sh """
					./bob/bob -r rulesets/ruleset2.0-eric-seppsim.yaml build;
					./bob/bob -r rulesets/ruleset2.0-eric-seppsim.yaml image;
					./bob/bob -r rulesets/ruleset2.0-eric-seppsim.yaml package-full;
					./bob/bob -r rulesets/ruleset2.0-eric-seppsim.yaml push:docker-image;
					./bob/bob -r rulesets/ruleset2.0-eric-seppsim.yaml push:helm-chart;
				"""
				echo 'Building NRFsim:'
				sh """
					./bob/bob -r rulesets/ruleset2.0-eric-nrfsim.yaml build;
					./bob/bob -r rulesets/ruleset2.0-eric-nrfsim.yaml image;
					./bob/bob -r rulesets/ruleset2.0-eric-nrfsim.yaml package;
					./bob/bob -r rulesets/ruleset2.0-eric-nrfsim.yaml push:docker-image;
					./bob/bob -r rulesets/ruleset2.0-eric-nrfsim.yaml push:helm-chart;
				"""
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
//						string(name: 'CHART_NAME', value: "${env.CHART_NAME}"), 
						string(name: 'CHART_VERSION', value: "${env.CHART_VERSION}"), 
//						string(name: 'CHART_REPO', value: "${env.CHART_REPO}"),
						string(name: 'TEAMNAME', value: "${env.TEAMNAME}"),
//						string(name: 'DEVPIPE', value: "${env.DEVPIPE}"),
//						string(name: 'PIPE', value: "${env.PIPE}"),
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
					//echo "${env.EMAIL_COMMITTER}"

					emailext body: "$COLOR" + "Hi ${env.COMMITTER},\n" +
											  "<p>the build failed, maybe due to your commit?\n" +
											  "<p>Please check:\n" +
											  "<br>${env.BUILD_URL}\n" +
											  "<br><br>Thank you for pushing.\n" +
											  "<br><br>BR,<br>A-Team",

						mimeType: 'text/html',
						subject: "5G Smoke-test build failed for ${env.PROJECT} : ${env.COMMIT}",
						to: "${env.EMAIL_COMMITTER}, DSCAteam@ericsson.onmicrosoft.com, f37b6763.ericsson.onmicrosoft.com@emea.teams.ms"
				} //if
			} //script
		} //failure
	} //post
} //pipeline

