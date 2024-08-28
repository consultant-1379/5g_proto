
//////////////////////////////////////////////////////////////////////////
//
// first run on SERO_GIC buildslaves, decide which buildslave(s) to use

def AGENT_LABEL = null

node('DSC_BuildSlave_GIC_1||DSC_BuildSlave_GIC_2||DSC_BuildSlave_GIC_3||DSC_BuildSlave_GIC_4||DSC_BuildSlave_GIC_5') {

  stage('set agent'){

     if (env.IP_VERSION == '6') {   // move to IPv6 buildslave(s)
        AGENT_LABEL = '5G-SC-IPv6'

     } else                         // move to IPv4 buildslave(s)
     {
         AGENT_LABEL = '5G-SC'      // in the future
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

	// get author emails from URL and pipe into file
	sh "curl -u eiffelesc:noH9pjRngEqj7\\&zw  ${env.BUILD_URL}api/json -o culprits.json" // for testing only
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
	sh "curl -u eiffelesc:noH9pjRngEqj7\\&zw ${env.BUILD_URL}api/json -o culprits_email1.json" // (this is working)
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

// set the displayed build name to BUILD_NUMBER | COMMIT | buildslave
currentBuild.displayName = "#${BUILD_NUMBER} - ${env.COMMIT}"
//currentBuild.displayName = "#${BUILD_NUMBER} - ${env.COMMIT} ($NODE_NAME)" // temp

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
		//CSA = true
		//SCP = true
		//BSF_TLS = false
		//CSA_TLS = false
		//SCP_TLS = false
		LOGGING = true
		PRODUCTION = true
		RESOURCES = "full"

		// Pipe Variables Used in Jenkins (sorted alphabetically)
		ARM_ARTIFACTORY_TOKEN = "${ARM_ARTIFACTORY_TOKEN}"
		BRANCH = "${env.BRANCH}"
		COMMIT = "${COMMIT}"
		//COMMITTER = "${env.COMMITTER}"
		//EMAIL_COMMITTER = "${EMAIL_COMMITTER}"
		COMMITTERS = findCommitters()
		EMAIL_COMMITTERS = findCommittersEmail()
		KUBE_HOST = "${env.KUBE_HOST}"
		NAMESPACE = "${env.NAMESPACE}"
		PRODUCT = ''
		REDIS_CONTAINER = "eric-chfsim-redis"
		RERUN = "${env.RERUN}"
		TROUBLESHOOTING = "${env.TROUBLESHOOTING}"
		TEST_BRANCH = "${env.TEST_BRANCH}"
		VERSION = ''
	}

	stages {
		stage('Freeing up workspace on other buildslaves') {
			steps{
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
						default:
							checkout scmGit(
								branches: [[name: '*/${TEST_BRANCH}']],
								userRemoteConfigs: [[credentialsId: 'eiffelesc-user-password', name: 'origin', url: 'https://eiffelesc@gerritmirror-direct.sero.gic.ericsson.se/a/MC_5G/5g_test_ci']],
								extensions: [submodule(parentCredentials: true, recursiveSubmodules: true, reference: '', shallow: false), [$class: 'RelativeTargetDirectory', relativeTargetDir: '5g_test_ci']])
							break
					} //switch
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

					// set Test Statistics for CI and SmokeTest
					if ((env.TEST_TYPE == 'CI') || (env.TEST_TYPE == 'SmokeTest')) {
						env.LOGDB = "true"
						echo "Set Test Statistics setting, LOGDB= ${env.LOGDB}"
					} //if
				} //script
			} //steps
		} //stage

		stage('Set suites for CI') {
			steps {
				dir('5g_test_ci/jcat-esc/EscTestProperties/'){
					script {

						echo "Cockpit Control = $env.CI_VIA_COCKPIT"
						if (env.CI_VIA_COCKPIT == 'TRUE') {

							// testng-modifyier only required for SmokeTest and CI
							//   for LoadTest only env.TESTNG is needed (already set via Spinnaker regex)
							//   for TeamCI the Cockpit is not at all used
							if ((env.TEST_TYPE == 'CI') || (env.TEST_TYPE == 'SmokeTest')) {

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

								// default no suites
//								env.TESTNG = ""
//								env.TESTNG_BSF = ""
//								env.TESTNG_SEPP = ""
//								env.TESTNG_SCP = ""
//								env.TESTNG_COMMON = "testng_default_common.xml"

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
							} //if

						} else {
							echo "CI suites set according to Jenkins Job / Spinnaker"
						} //if
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

								// run the test:
								sh "./bob/bob adptest:ft-set-redis-name"
								sh "./bob/bob adptest:ft-start-redis"
								sh "./bob/bob adptest:jenkinsft"
								break
							// all system test activities
							case [ 'Stability', 'Load', 'Robustness' ]:
								// prepare the <cluster>.properties file:
								sh "./bob/bob adptest:prepareload"
								// run the test (env.TESTNG already set via Spinnaker regex):
								sh "./bob/bob adptest:jenkinsload"
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
					sh '''
						# define local variables
						l_TESTNG_SCP=\'""\';
						l_TESTNG_BSF=\'""\';
						l_TESTNG_SEPP=\'""\';
						l_TESTNG_COMMON=\'""\';
						
						if [ -f ./jcat-esc/scp-testcases/target/failsafe-reports/testng-failed.xml ]        # check if testng_failed exists
						then 
							# set the local testng var to new testng file, cp testng file to WS root, and rename it 
							l_TESTNG_SCP="testng-SCP-failed.xml";
							l_TESTNG_COMMON="testng_default_common.xml";
							cp ./jcat-esc/scp-testcases/target/failsafe-reports/testng-failed.xml ./jcat-esc/EscTestProperties/testng-SCP-failed.xml;
							#cp /home/eiffelesc/testng-failed.xml testng-SCP-failed.xml;
							sed -i -e '1d' -e '2d' -e 's@Failed suite \\[@@g' -e 's@\\]@-rerun@g' ./jcat-esc/EscTestProperties/testng-SCP-failed.xml;
						fi;
						if [ -f ./jcat-esc/bsf-testcases/target/failsafe-reports/testng-failed.xml ];
						then
							l_TESTNG_BSF="testng-BSF-failed.xml";
							cp ./jcat-esc/bsf-testcases/target/failsafe-reports/testng-failed.xml ./jcat-esc/EscTestProperties/testng-BSF-failed.xml;
							sed -i -e '1d' -e '2d' -e 's@Failed suite \\[@@g' -e 's@\\]@-rerun@g' ./jcat-esc/EscTestProperties/testng-BSF-failed.xml;
						fi;
						if [ -f ./jcat-esc/sepp-testcases/target/failsafe-reports/testng-failed.xml ]
						then 
							l_TESTNG_SEPP="testng-SEPP-failed.xml";
							l_TESTNG_COMMON="testng_default_common.xml";
							cp ./jcat-esc/sepp-testcases/target/failsafe-reports/testng-failed.xml ./jcat-esc/EscTestProperties/testng-SEPP-failed.xml;
							sed -i -e '1d' -e '2d' -e 's@Failed suite \\[@@g' -e 's@\\]@-rerun@g' ./jcat-esc/EscTestProperties/testng-SEPP-failed.xml;
						fi;
						if [ -f ./jcat-esc/common-testcases/target/failsafe-reports/testng-failed.xml ];
						then 
							l_TESTNG_COMMON="testng-COMMON-failed.xml";
							cp ./jcat-esc/common-testcases/target/failsafe-reports/testng-failed.xml ./jcat-esc/EscTestProperties/testng-COMMON-failed.xml;
							sed -i -e '1d' -e '2d' -e 's@Failed suite \\[@@g' -e 's@\\]@-rerun@g' ./jcat-esc/EscTestProperties/testng-COMMON-failed.xml;
						fi;
						
						if [ "${l_TESTNG_SCP}" != \'""\' ] || [ "${l_TESTNG_BSF}" != \'""\' ] || [ "${l_TESTNG_SEPP}" != \'""\' ] || [ "${l_TESTNG_COMMON}" != \'""\' ] ;
						then
							# set environment variables for the sub-shell and trigger the re-run
							export TESTNG=${l_TESTNG_SCP};
							export TESTNG_BSF=${l_TESTNG_BSF};
							export TESTNG_SEPP=${l_TESTNG_SEPP};
							export TESTNG_COMMON=${l_TESTNG_COMMON};
							echo "Executing re-run of failed suites";
							./bob/bob adptest:jenkinsft;
						else
							echo "All TCs were successful, no re-run triggered"; 
						fi;
					   '''
				} //dir
			} //steps
		} //stage

		stage('Checking test results') {
			steps{
				dir('5g_test_ci'){
					sh returnStatus: false, script: '''
						FAILED=0;
						SKIPPED=0;
						for i in common-testcases scp-testcases bsf-testcases sepp-testcases; 
						do 
							if [ "$FAILED" -eq 0 ] && [ "$SKIPPED" -eq 0 ];
							then
								if [ -f ./jcat-esc/$i/target/failsafe-reports/testng-results.xml ];
								then
									FAILED=$(grep -oE "failed=\\"[0-9]{1,}\\"" ./jcat-esc/$i/target/failsafe-reports/testng-results.xml | cut -d\'=\' -f2 | sed "s/\\"//g"); 
									SKIPPED=$(grep -oE "skipped=\\"[0-9]{1,}\\"" ./jcat-esc/$i/target/failsafe-reports/testng-results.xml | cut -d\'=\' -f2 | sed "s/\\"//g"); 
								fi;
							fi;
						done;
						
						if [ "$FAILED" -eq 0 ] && [ "$SKIPPED" -eq 0 ]; 
						then
							echo "All TCs were successful"; 
							echo "TROUBLESHOOTING=FALSE" > ../concl.properties; 
						else 
							echo "Obviously some TCs failed!!"; 
							echo "TROUBLESHOOTING=TRUE" > ../concl.properties; 
							../scripts/collect_ADP_logs.sh -c ~/.kube/${KUBE_HOST}.config -n ${NAMESPACE}; 
						fi;
						
						echo "TEST_BRANCH=${TEST_BRANCH}" >> ../concl.properties; 
						echo "TEST_COMMIT=$(git log -n1 --abbrev-commit --format="%h")" >> ../concl.properties; '''

				} //dir
				// eedmti_smoke: archiveArtifacts allowEmptyArchive: true, artifacts: 'concl.properties', onlyIfSuccessful: false
				archiveArtifacts allowEmptyArchive: true, artifacts: '5g_test_ci/logs_${NAMESPACE}_*.tgz', onlyIfSuccessful: false
			} //steps
		} //stage

		stage('Set verdict') {
			steps {
				script {
					def exists = fileExists 'concl.properties'
					if (exists) {
						def data = readFile(file: 'concl.properties')
						if (data.contains("TROUBLESHOOTING=FALSE")) {
							email_color = "<body style=\"background-color: LightGreen;\">";				// body in LightGreen
							email_verdict = "Verdict: <b><font color=\"#228B22\"> Success </font></b>"	// in green
						} else {
							email_color = "<body style=\"background-color: Salmon;\">";					// body in Salmon
							email_verdict = "Verdict: <b><font color=\"#FF0000\"> Failed </font></b>"	// in red
						} //if
					} else {
						email_verdict = "Verdict: <b>undefined</b>"
					} //if
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
						DIRS = sh (
							script: 'cd jcat-esc/jcatlogs/$(date +%Y)*; j=1; for i in $(ls); do mkdir ../../../latestjcatlogs_$j ; cp -R ./$i/. ../../../latestjcatlogs_$j/. ; j=$((j+1)) ; done; echo $j;' ,
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

						// update concl.properties (eedmti_smoke)
						email_jcatlog_props = email_jcatlogs.replaceAll("\n","") // remove the \n
						readContent = readFile '../concl.properties'
						writeFile file: '../concl.properties', text: readContent +"\rJCAT_LOGS=$email_jcatlog_props"+"\rBUILD_URL=$email_buildUrl"
					} //script
				} //dir

				archiveArtifacts allowEmptyArchive: true, artifacts: 'concl.properties', onlyIfSuccessful: false                    // eedmti_smoke

			} //steps
		} //stage
	} //stages

	post{
		always{
			// releasing ports applies to all test-types
			dir('5g_test_ci') {
				echo("Releasing ports ...")
				sh "./bob/bob adptest:ft-release-ports"
			} //dir
			
			echo "TEST_TYPE is $TEST_TYPE"
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
					case ['Stability', 'Load', 'Robustness']:
						//echo("Nothing to do here (yet)...")
						break
					default:
						//echo("Nothing to do here ...")
						break
				} //switch
			} //script
		} //always

/* eedcla, 27.05.21, port cleaning moved to 'always'
		// cleanup of ports needed in case job was aborted
		// needed in order to avoid filling up of file ports_used_CI
		aborted {
			dir('5g_test_ci') {
				// decision made based on the existence of the different port variables
				sh """ if ( ls .bob | grep -q "port" );
					then
						echo "Need to clean some ports";
						./bob/bob adptest:ft-remove-redis;
						./bob/bob adptest:ft-release-ports;
					fi;"""
			} //dir
		} //aborted
*/

		failure{
			// collecting ADP logs in case of pipeline failures removed (agreed in A-Team workshop)
			script {
				// clean cluster due to failure in pipeline
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
							to: "${DEV_EMAIL_COMMITTER}, ${EMAIL_COMMITTERS}, DSCAteam@ericsson.onmicrosoft.com, f37b6763.ericsson.onmicrosoft.com@emea.teams.ms"  //Teams: Master State
						break
					default:
						echo('The pipeline failed!')
						break
				} //switch
			} //script
		} //failure
	} //post
} //pipeline


