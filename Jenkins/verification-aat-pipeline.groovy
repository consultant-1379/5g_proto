
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
//
//////////////////////////////////////////////////////////////////////////

// set the displayed build name to BUILD_NUMBER | COMMIT | buildslave
currentBuild.displayName = "#${BUILD_NUMBER} - ${env.COMMIT}"
//currentBuild.displayName = "#${BUILD_NUMBER} - ${env.COMMIT} ($NODE_NAME)" // temp

def archiveAndVerdict(String testSuite)
{
	env.function1=testSuite
	dir('5g_test_ci'){
					sh returnStatus: false, script: '''
							../scripts/collect_ADP_logs.sh -c ~/.kube/\$KUBE_HOST.config -n \$NAMESPACE; '''
				} 
	archiveArtifacts allowEmptyArchive: true, artifacts: '5g_test_ci/logs_\$NAMESPACE*.tgz', onlyIfSuccessful: false
	script {
					def exists = fileExists 'concl.properties'
					if (exists) {
						def data = readFile(file: 'concl.properties')
						if (env.function1 == "CSA FT" || env.function1 == "CSA NFT" ||env.function1 == "SCP NFT" ||env.function1 == "SEPP NFT")
						{
							if (data.contains("FAILED=0") || data.contains("FAILED=1")|| data.contains("FAILED=2")|| data.contains("FAILED=3")|| data.contains("FAILED=4")|| data.contains("FAILED=5")|| data.contains("FAILED=6")) {   // The 16 failed TCs are these with TLS, manual check for now 
								email_color = "<body style=\"background-color: LightGreen;\">";				// body in LightGreen
								email_verdict = "<b><font color=\"#228B22\"> Success </font></b>"	// in green
							} else {
								email_color = "<body style=\"background-color: Salmon;\">";					// body in Salmon
								email_verdict = "<b><font color=\"#FF0000\"> Failed </font></b>"	// in red
							} //if
						}
						else if (env.function1 == "SEPP_FT")
						{
							if (data.contains("FAILED=1")) {   
								email_color = "<body style=\"background-color: LightGreen;\">";				// body in LightGreen
								email_verdict = "<b><font color=\"#228B22\"> Success </font></b>"	// in green
							} else {
								email_color = "<body style=\"background-color: Salmon;\">";					// body in Salmon
								email_verdict = "<b><font color=\"#FF0000\"> Failed </font></b>"	// in red
							} //if
						}
						else
						{
							if (data.contains("FAILED=0")) {   // The 16 failed TCs are these with TLS, manual check for now 
								email_color = "<body style=\"background-color: LightGreen;\">";				// body in LightGreen
								email_verdict = "<b><font color=\"#228B22\"> Success </font></b>"	// in green
							} else {
								email_color = "<body style=\"background-color: Salmon;\">";					// body in Salmon
								email_verdict = "<b><font color=\"#FF0000\"> Failed </font></b>"	// in red
							} //if
						}
					} else {
						email_verdict = "Verdict: <b>undefined</b>"
					} //if
					// update concl.properties (eedmti_smoke)
					readContent = readFile 'concl.properties'
					writeFile file: 'concl.properties', text: readContent +"\rVERDICT ${env.function1} =$email_verdict"
				} //script			
	archiveArtifacts allowEmptyArchive: true, artifacts: 'resultFile.json', onlyIfSuccessful: false
	archiveArtifacts allowEmptyArchive: true, artifacts: 'concl.properties', onlyIfSuccessful: false			
}

def archiveLogs(String testSuite, String emailURL)
{
	env.function2=testSuite
	env.emailUrl=emailURL
	dir('5g_test_ci') {
						echo("Releasing ports ...")
						sh "./bob/bob adptest:ft-release-ports"
					} //dir
					
	echo "TEST_TYPE is $TEST_TYPE"
	script {
		def COLOR = "<body style=\"background-color: LightYellow;\">"
		echo "$COLOR"
		readContent = readFile 'concl.properties'
		echo "Content $readContent"
		emailext body: "$COLOR"+"Hello AAT team,<br><br> AAT test suite results for ${BRANCH} ${CHART_VERSION}.<br>Please check: ${env.emailUrl} <br><br> ${env.function2} Test Suite: <br><br> ${readContent} ",
		mimeType: 'text/html',
		subject: "AAT test suite results ${BRANCH} ${CHART_VERSION}",
		to: "c91010d0.ericsson.onmicrosoft.com@emea.teams.ms"
	}
}

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
		COMMITTER = "${env.COMMITTER}"
		EMAIL_COMMITTER = "${EMAIL_COMMITTER}"
		KUBE_HOST = "${env.KUBE_HOST}"
		NAMESPACE = "${env.NAMESPACE}"
		PRODUCT = ''
		REDIS_CONTAINER = "eric-chfsim-redis"
		RERUN = "${env.RERUN}"
		TROUBLESHOOTING = "${env.TROUBLESHOOTING}"
		TEST_BRANCH = "${env.TEST_BRANCH}"
		VERSION = ''
		//Variables in order to execute the corresponding Test Suite
		BSF_FT_TEST_SUITE = "${env.BSF_FT}"
		BSF_NFT_TEST_SUITE = "${env.BSF_NFT}"
		SCP_FT_TEST_SUITE = "${env.SCP_FT}"
		SCP_NFT_TEST_SUITE = "${env.SCP_NFT}"
		CSA_FT_TEST_SUITE = "${env.CSA_FT}"
		CSA_NFT_TEST_SUITE = "${env.CSA_NFT}"
		// CSA_TEST_SUITE = "${env.CSA_TEST_SUITE}"
		SEPP_FT_TEST_SUITE = "${env.SEPP_FT}"
		SEPP_NFT_TEST_SUITE = "${env.SEPP_NFT}"
		SC_DIAMETER_FT_TEST_SUITE = "${env.SC_DIAMETER_FT}"
		SC_DIAMETER_NFT_TEST_SUITE = "${env.SC_DIAMETER_NFT}"

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
				sh './bob/bob init:set-build-proxy'

				echo 'Creating test repo directory'
				dir('5g_test_ci') {
					deleteDir()
				}
				echo pwd()
				echo 'Trying to clone the 5G test repository'
					script{
					checkout scmGit(
						branches: [[name: '*/${TEST_CI_BRANCH}']],
						userRemoteConfigs: [[credentialsId: 'eiffelesc-user-password', name: 'origin', url: 'https://eiffelesc@gerritmirror-direct.sero.gic.ericsson.se/a/MC_5G/5g_test_ci']],
						extensions: [submodule(parentCredentials: true, recursiveSubmodules: true, reference: '', shallow: false), [$class: 'RelativeTargetDirectory', relativeTargetDir: '5g_test_ci']])
				} //script
				echo 'check content'
				dir('5g_test_ci') {
					echo pwd()
					sh 'git log -n 10'
					sh 'ls -ltrh'
					sh 'git submodule update --init --recursive'
				} //dir
			} //steps
		} //stage

		stage('Clone AAT test repository') {
			steps{
				echo 'Checking user'
				sh 'whoami'
				echo 'check path'
				echo pwd()
				sh 'git submodule update --init --recursive'
				sh './bob/bob init:set-build-proxy'
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

					sh './bob/bob -r ruleset2.0-eric-aat.yaml init:version init:aat-cxu-number init:aat-cxu-rev-number;'
					sh './bob/bob -r ruleset2.0-eric-aat.yaml init:set-kube-config;'
				} //dir
			} //steps
		} //stage

		stage('Clone CDD repository') {
			steps{
				echo 'Checking user'
				sh 'whoami'
				echo 'check path'
				echo pwd()
				sh 'git submodule update --init --recursive'

				echo 'Creating CDD repo directory'
				dir('5G_CDD') {
					deleteDir()
				}
				echo pwd()
				echo 'Trying to clone the 5G CDD repository'
					script{
					checkout scmGit(
						branches: [[name: '*/${CDD_BRANCH}']],
						userRemoteConfigs: [[credentialsId: 'eiffelesc-user-password', name: 'origin', url: 'https://eiffelesc@gerritmirror-direct.sero.gic.ericsson.se/a/MC_5G/5G_CDD']],
						extensions: [submodule(parentCredentials: true, recursiveSubmodules: true, reference: '', shallow: false), [$class: 'RelativeTargetDirectory', relativeTargetDir: '5G_CDD']])
				} //script
				echo 'check content'
				dir('5G_CDD') {
					echo pwd()
					sh 'git log -n 10'
					sh 'ls -ltrh'
					sh 'git submodule update --init --recursive'
				} //dir
			} //steps
		} //stage

		stage('Deploy SC Diameter') {
			when {
				expression {
					return (SC_DIAMETER_FT_TEST_SUITE == 'true') || (SC_DIAMETER_NFT_TEST_SUITE == 'true');
				}
			}
			steps{
				echo 'Fetch SC Diameter Pipeline'
				build job: '../../cnDSC/Teams/0-Clean-KaaS-cluster-cnDSC/', parameters: [string(name: 'NAMESPACE', value: 'dsc-ci'), string(name: 'KUBE_HOST', value: "${KUBE_HOST}"), string(name: 'TAG', value: 'NIGHTLY-CI'), string(name: 'RESOURCES', value: 'full'), string(name: 'BRANCH', value: 'cnDSC'), string(name: 'PROPERTY_FILE', value: 'deploy.properties')], wait: true
			} //steps
		} //stage

		stage('Wait SC Diameter Deployment') {
			when {
				expression {
					return (SC_DIAMETER_FT_TEST_SUITE == 'true') || (SC_DIAMETER_NFT_TEST_SUITE == 'true');
				}
			}
			steps{
				// sh './bob/bob -r ruleset2.0-eric-esc.yaml undeploy:all;'
				sh 'helm uninstall eric-sc-eiffelesc-1 --timeout 600s --namespace \$NAMESPACE --kubeconfig ~/.kube/${KUBE_HOST}.config --ignore-not-found=true;'
				dir('5G_AAT') {
					echo pwd()
					sh 'sleep 300;'
					sh './bob/bob -r ruleset2.0-eric-aat.yaml wait-for-scdiameter-deployed-resources:adp;'
				} //dir
			} //steps
		} //stage

		//Deploy k6
		stage('Deploy k6') {
			when {
				expression {
					return (SCP_NFT_TEST_SUITE == 'true') || (CSA_NFT_TEST_SUITE == 'true') || (SEPP_NFT_TEST_SUITE == 'true')|| (BSF_NFT_TEST_SUITE == 'true') ;
				}
			}
			steps {
				dir('5G_AAT/scripts/k6_ci') {
					sh """
					pwd
					./install_k6_ci.sh

					helm delete --kubeconfig ~/.kube/${KUBE_HOST}.config --namespace ${NAMESPACE} eric-k6 || true; helm install eric-k6 --kubeconfig ~/.kube/${KUBE_HOST}.config --namespace ${NAMESPACE} eric-k6-aat-*.tgz
					"""
				}

			} // steps
		} //stage

		//Deploy k6
		stage('Install certificates for Traffic and TLS') {
			when {
				expression {
					return (SC_DIAMETER_FT_TEST_SUITE == 'false') && (SC_DIAMETER_NFT_TEST_SUITE == 'false');
				}
			}
			steps {
				sh './bob/bob base.init;'
				sh './bob/bob -r ruleset2.0-eric-esc.yaml create-certificates;'
				sh './bob/bob -r ruleset2.0-eric-esc.yaml install-certificates:rootca;'
				sh './bob/bob -r ruleset2.0-eric-esc.yaml install-certificates:chfsim;'
				sh './bob/bob -r ruleset2.0-eric-esc.yaml install-certificates:chfsim-sepp;'
				sh './bob/bob -r ruleset2.0-eric-esc.yaml install-certificates:nrfsim;'
				sh './bob/bob -r ruleset2.0-eric-esc.yaml install-certificates:seppsim;'
				sh './bob/bob -r ruleset2.0-eric-esc.yaml install-certificates:seppsim-scp;'
				sh './bob/bob -r ruleset2.0-eric-esc.yaml install-certificates:bsf-worker;'
				sh './bob/bob -r ruleset2.0-eric-esc.yaml install-certificates:bsf-manager;'
				sh './bob/bob -r ruleset2.0-eric-esc.yaml install-certificates:scp-worker;'
				sh './bob/bob -r ruleset2.0-eric-esc.yaml install-certificates:scp-manager;'
				sh './bob/bob -r ruleset2.0-eric-esc.yaml install-certificates:sepp-manager;'
				sh './bob/bob -r ruleset2.0-eric-esc.yaml install-certificates:sepp-worker;'
				sh './bob/bob -r ruleset2.0-eric-esc.yaml install-certificates:nlf;'
				sh './bob/bob -r ruleset2.0-eric-esc.yaml install-certificates:slf;'
				sh './bob/bob -r ruleset2.0-eric-esc.yaml install-certificates:nbi;'
				sh './bob/bob -r ruleset2.0-eric-esc.yaml install-certificates:k6;'
				sh './bob/bob -r ruleset2.0-eric-esc.yaml install-certificates:transformer;'
				sh './bob/bob -r ruleset2.0-eric-esc.yaml install-certificates:syslog;'
				sh './bob/bob -r ruleset2.0-eric-esc.yaml install-certificates:internal-ldap;'
				sh './bob/bob -r ruleset2.0-eric-esc.yaml install-certificates:referral-ldap;'
				sh './bob/bob -r ruleset2.0-eric-esc.yaml install-certificates:diameter;'
				sh './bob/bob -r ruleset2.0-eric-esc.yaml install-certificates:dscload;'
				sh './bob/bob -r ruleset2.0-eric-esc.yaml install-certificates:influxdb;'
				sh './bob/bob -r ruleset2.0-eric-esc.yaml install-certificates:telegraf;'
				sh './bob/bob -r ruleset2.0-eric-esc.yaml install-certificates:pmrw;'
				sh './bob/bob -r ruleset2.0-eric-esc.yaml install-certificates:ext-lumberjack;'
				sh './bob/bob -r ruleset2.0-eric-esc.yaml install-certificates:ext-lumberjack-x;'
				sh './bob/bob -r ruleset2.0-eric-esc.yaml install-certificates:yang-provider;'


			} // steps
		} //stage

		//Deploy k6
		stage('Install certificates for n32 SEPP') {
			when {
				expression {
					return (SEPP_FT_TEST_SUITE == 'true') || (SEPP_NFT_TEST_SUITE == 'true') ;
				}
			}
			steps {
				sh './bob/bob -r ruleset2.0-eric-esc.yaml install-certificates:seppsim-n32c;' 
			} // steps
		} //stage

		//BSF FT
		stage ("Load BSF FT/NFT Configuration") {
			when {
				expression {
					return (BSF_FT_TEST_SUITE == 'true') || (BSF_NFT_TEST_SUITE == 'true') ;
				}
			}
			steps {
				dir('5G_AAT') {
					sh """
					./bob/bob -r ruleset2.0-eric-aat.yaml loadconfig:bsf-AAT
					./bob/bob -r ruleset2.0-eric-aat.yaml deploy:update-cert-suite
					"""
				}

			} // steps
		} // stage

        stage('Set BSF FT suite for AAT') {
			when {
				expression {
					return BSF_FT_TEST_SUITE == 'true';
				}
			}
            steps {
				script {
					sh """cp /proj/DSC/5g_aat/jenkins/AAT1.9.0/AAT_SC_Sample_Test_Suite_BSF_FT.tar .
					tar -xf AAT_SC_Sample_Test_Suite_BSF_FT.tar --wildcards 'data/jcatsut/*.json'
					export CMM_IP=`kubectl get svc eric-cm-yang-provider-external --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[*].ip}'`;
							export CMM_PORT=`kubectl get svc eric-cm-yang-provider-external --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath="{.spec.ports[0].port}"`;
							export BSF_IP=`kubectl get svc eric-bsf-worker --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[*].ip}'`;
							export BSF_WRKR_PORT=`kubectl get svc eric-bsf-worker --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath="{.spec.ports[0].port}"`;
							export STM_IP=`kubectl get svc eric-stm-diameter-traffic-tcp --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[*].ip}'`;
							export STM_PORT=`kubectl get svc eric-stm-diameter-traffic-tcp --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath="{.spec.ports[0].port}"`;
							export TLS_PORT=443;
							sed -i '/"port":/ s/"port":[^,]*/"port":\'\$BSF_WRKR_PORT\'/' data/jcatsut/*.json;
							sed -i '/"tls-port":/ s/"tls-port":[^}]*/"tls-port":\'\$TLS_PORT\'/' data/jcatsut/*.json;
							sed -i '/"ipv4-address":/ s/"ipv4-address":[^,]*/"ipv4-address":"\'\$BSF_IP\'"/' data/jcatsut/*.json;
							sed -i '/{"ipv4-address":/ s/{"ipv4-address":[^,]*/{"ipv4-address":"\'\$CMM_IP\'"/' data/jcatsut/*.json;
							sed -i '/"port":/ s/"port":[^,]*/"port":\'\$CMM_PORT\'/2' data/jcatsut/*.json;
							sed -i '/"nameSpace":/ s/"nameSpace":[^,]*/"nameSpace":"\'\${NAMESPACE}\'"/' data/jcatsut/*.json;
							sed -i '/"stmPort"}:/ s/"stmPort":[^}]*/"stmPort"}:"\'\$STM_PORT\'"/' data/jcatsut/*.json;
							sed -i '/{"stmAddress":/ s/{"stmAddress":[^,]*/{"stmAddress":"\'\$STM_IP\'"/' data/jcatsut/*.json;

							export FILENAME=`ls data/jcatsut/*.json`;
							tar -uf AAT_SC_Sample_Test_Suite_BSF_FT.tar \$FILENAME;
					
						"""
					dir('5G_AAT') {	
						echo pwd()
						echo("Update the BSF FT Suite with the TLS Certificates ...")
						sh 'ls -al scripts'
						// sh "./bob/bob -r ruleset2.0-eric-aat.yaml deploy:update-cert-suite"
					}
					sh "cat data/jcatsut/*.json"

				} //script
            } // steps
        } // stage

		stage('Execute BSF FT Tests') {
			when {
				expression {
					return BSF_FT_TEST_SUITE == 'true';
				}
			}
			steps {
				dir('5g_test_ci') {
					script{
						// allocate ports and write to .bob/var.xxx files
						
						sh """./bob/bob adptest:ft-set-ports
						
						"""
					}
				}
				dir('5G_CDD/'){
					script {
						sh """export AAT_IP=`kubectl get svc aat-south-service --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[*].ip}'`;
							export AAT_PORT=`kubectl get svc aat-south-service --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath="{.spec.ports[0].port}"`;
							chmod +x swdp-5G-SC/5G-SC/src/aat_jenkins.py;
							
							DOCKER_RUN="docker run --rm  --user `id -u`:`id -g`"
                            BUILDER_DOCKER_IMAGE="armdocker.rnd.ericsson.se/sandbox/adp-staging/adp-cicd/bob-py3kubehelmbuilder:1.5.0-12"
                            BUILDER_DOCKER_VOLUMES="-v \${WORKSPACE}/helm-home:/home/jenkins/.helm -v \${WORKSPACE}:\${WORKSPACE}"
                            BUILDER_WRK_ENV="-w \${WORKSPACE}/5G_CDD/swdp-5G-SC/5G-SC/src"
							PYTHON_CMD="\$DOCKER_RUN \$BUILDER_DOCKER_VOLUMES \$BUILDER_WRK_ENV \$BUILDER_DOCKER_ENV  \$BUILDER_DOCKER_IMAGE"
							
							\$PYTHON_CMD  /bin/bash -c "pip install urllib3==1.25.11 requests==2.22.0 && python aat_jenkins.py https://\$AAT_IP:\$AAT_PORT \${WORKSPACE}/AAT_SC_Sample_Test_Suite_BSF_FT.tar"
							mv \${WORKSPACE}/5G_CDD/swdp-5G-SC/5G-SC/src/resultFile.json \${WORKSPACE}
							mv \${WORKSPACE}/5G_CDD/swdp-5G-SC/5G-SC/src/concl.properties \${WORKSPACE}
						
						"""
					} //script
				} // dirs
				
			} // steps
			post{
				always{
					archiveAndVerdict("BSF FT")
					archiveLogs("BSF FT", "${email_buildUrl}")
				}
			}
		} // stage

		//BSF NFT
		stage('Set BSF NFT suite for AAT') {
			when {
				expression {
					return BSF_NFT_TEST_SUITE == 'true';
				}
			}
            steps {
				script {
					sh """cp /proj/DSC/5g_aat/jenkins/AAT1.9.0/AAT_SC_Sample_Test_Suite_BSF_NFT.tar .
					tar -xf AAT_SC_Sample_Test_Suite_BSF_NFT.tar --wildcards 'data/jcatsut/*.json'
					export CMM_IP=`kubectl get svc eric-cm-yang-provider-external --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[*].ip}'`;
							export CMM_PORT=`kubectl get svc eric-cm-yang-provider-external --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath="{.spec.ports[0].port}"`;
							export BSF_IP=`kubectl get svc eric-bsf-worker --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[*].ip}'`;
							export BSF_WRKR_PORT=`kubectl get svc eric-bsf-worker --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath="{.spec.ports[0].port}"`;
							export STM_IP=`kubectl get svc eric-stm-diameter-traffic-tcp --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[*].ip}'`;
							export STM_PORT=`kubectl get svc eric-stm-diameter-traffic-tcp --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath="{.spec.ports[0].port}"`;
							export TLS_PORT=443;
							sed -i '/"port":/ s/"port":[^,]*/"port":\'\$BSF_WRKR_PORT\'/' data/jcatsut/*.json;
							sed -i '/"tls-port":/ s/"tls-port":[^}]*/"tls-port":\'\$TLS_PORT\'/' data/jcatsut/*.json;
							sed -i '/"ipv4-address":/ s/"ipv4-address":[^,]*/"ipv4-address":"\'\$BSF_IP\'"/' data/jcatsut/*.json;
							sed -i '/{"ipv4-address":/ s/{"ipv4-address":[^,]*/{"ipv4-address":"\'\$CMM_IP\'"/' data/jcatsut/*.json;
							sed -i '/"port":/ s/"port":[^,]*/"port":\'\$CMM_PORT\'/2' data/jcatsut/*.json;
							sed -i '/"nameSpace":/ s/"nameSpace":[^,]*/"nameSpace":"\'\${NAMESPACE}\'"/' data/jcatsut/*.json;
							sed -i '/"stmPort"}:/ s/"stmPort":[^}]*/"stmPort"}:"\'\$STM_PORT\'"/' data/jcatsut/*.json;
							sed -i '/{"stmAddress":/ s/{"stmAddress":[^,]*/{"stmAddress":"\'\$STM_IP\'"/' data/jcatsut/*.json;

							export FILENAME=`ls data/jcatsut/*.json`;
							tar -uf AAT_SC_Sample_Test_Suite_BSF_NFT.tar \$FILENAME;
					
						"""
					dir('5G_AAT') {	
						echo pwd()
						echo("Update the BSF NFT Suite with the TLS Certificates ...")
						sh 'ls -al scripts'
						// sh "./bob/bob -r ruleset2.0-eric-aat.yaml deploy:update-cert-suite"
					}
					sh "cat data/jcatsut/*.json"

				} //script
            } // steps
        } // stage

		stage('Execute BSF NFT Tests') {
			when {
				expression {
					return BSF_NFT_TEST_SUITE == 'true';
				}
			}
			steps {
				dir('5g_test_ci') {
					script{
						// allocate ports and write to .bob/var.xxx files
						
						sh """./bob/bob adptest:ft-set-ports
						
						"""
					}
				}
				dir('5G_CDD/'){
					script {
						sh """export AAT_IP=`kubectl get svc aat-south-service --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[*].ip}'`;
							export AAT_PORT=`kubectl get svc aat-south-service --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath="{.spec.ports[0].port}"`;
							chmod +x swdp-5G-SC/5G-SC/src/aat_jenkins.py;
							
							DOCKER_RUN="docker run --rm  --user `id -u`:`id -g`"
                            BUILDER_DOCKER_IMAGE="armdocker.rnd.ericsson.se/sandbox/adp-staging/adp-cicd/bob-py3kubehelmbuilder:1.5.0-12"
                            BUILDER_DOCKER_VOLUMES="-v \${WORKSPACE}/helm-home:/home/jenkins/.helm -v \${WORKSPACE}:\${WORKSPACE}"
                            BUILDER_WRK_ENV="-w \${WORKSPACE}/5G_CDD/swdp-5G-SC/5G-SC/src"
							PYTHON_CMD="\$DOCKER_RUN \$BUILDER_DOCKER_VOLUMES \$BUILDER_WRK_ENV \$BUILDER_DOCKER_ENV  \$BUILDER_DOCKER_IMAGE"
							
							\$PYTHON_CMD  /bin/bash -c "pip install urllib3==1.25.11 requests==2.22.0 && python aat_jenkins.py https://\$AAT_IP:\$AAT_PORT \${WORKSPACE}/AAT_SC_Sample_Test_Suite_BSF_NFT.tar"
							mv \${WORKSPACE}/5G_CDD/swdp-5G-SC/5G-SC/src/resultFile.json \${WORKSPACE}
							mv \${WORKSPACE}/5G_CDD/swdp-5G-SC/5G-SC/src/concl.properties \${WORKSPACE}
						
						"""
					} //script
				} // dirs
			
			} // steps
			post{
				always{
					archiveAndVerdict("BSF NFT")
					archiveLogs("BSF NFT", "${email_buildUrl}")
				}
			}
		} // stage

		//CSA FT
		stage ("Load CSA FT/NFT Configuration") {
			when {
				expression {
					return (CSA_FT_TEST_SUITE == 'true') || (CSA_NFT_TEST_SUITE == 'true');
				}
			}
			steps {
				dir('5G_AAT') {
					sh """
					./bob/bob -r ruleset2.0-eric-aat.yaml loadconfig:csa-AAT
					"""
				}

			} // steps
		} // stage

        stage('Set CSA FT suite for AAT') {
			when {
				expression {
					return CSA_FT_TEST_SUITE == 'true';
				}
			}
            steps {
				script {
					sh """cp /proj/DSC/5g_aat/jenkins/AAT1.9.0/AAT_SC_Sample_Test_Suite_CSA_FT.tar .
					tar -xf AAT_SC_Sample_Test_Suite_CSA_FT.tar --wildcards 'data/jcatsut/*.json'
					tar -xf AAT_SC_Sample_Test_Suite_CSA_FT.tar --wildcards 'data/global_parameter/*.json'
					export CMM_IP=`kubectl get svc eric-cm-yang-provider-external --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[*].ip}'`;
							export CMM_PORT=`kubectl get svc eric-cm-yang-provider-external --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath="{.spec.ports[0].port}"`;
							export SCP_IP=`kubectl get svc eric-scp-worker --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[*].ip}'`;
							export SCP_WRKR_PORT=`kubectl get svc eric-scp-worker --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath="{.spec.ports[0].port}"`;
							export TLS_PORT=443;
							export CHFSIM1_IP=`kubectl get svc eric-chfsim-1 --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath="{.spec.clusterIP}"`;
							export CHFSIM2_IP=`kubectl get svc eric-chfsim-2 --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath="{.spec.clusterIP}"`;
							export CHFSIM3_IP=`kubectl get svc eric-chfsim-3 --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath="{.spec.clusterIP}"`; 
							sed -i '/"port":/ s/"port":[^,]*/"port":\'\$SCP_WRKR_PORT\'/' data/jcatsut/*.json;
							sed -i '/"tls-port":/ s/"tls-port":[^}]*/"tls-port":\'\$TLS_PORT\'/' data/jcatsut/*.json;
							sed -i '/"ipv4-address":/ s/"ipv4-address":[^,]*/"ipv4-address":"\'\$SCP_IP\'"/' data/jcatsut/*.json;
							sed -i '/{"ipv4-address":/ s/{"ipv4-address":[^,]*/{"ipv4-address":"\'\$CMM_IP\'"/' data/jcatsut/*.json;
							sed -i '/"port":/ s/"port":[^,]*/"port":\'\$CMM_PORT\'/2' data/jcatsut/*.json;
							sed -i '/"nameSpace":/ s/"nameSpace":[^,]*/"nameSpace":"\'\${NAMESPACE}\'"/' data/jcatsut/*.json;
							sed -i 's/, {"value": "[^"]*", "key": "sim1ip", "group": ""*/, {"value": "\'\$CHFSIM1_IP\'", "key": "sim1ip", "group": ""/' data/global_parameter/*.json;
							sed -i 's/, {"value": "[^"]*", "key": "sim2ip", "group": ""*/, {"value": "\'\$CHFSIM2_IP\'", "key": "sim2ip", "group": ""/' data/global_parameter/*.json;
							sed -i 's/, {"value": "[^"]*", "key": "sim3ip", "group": ""*/, {"value": "\'\$CHFSIM3_IP\'", "key": "sim3ip", "group": ""/' data/global_parameter/*.json;

							export FILENAME=`ls data/jcatsut/*.json`;
							tar -uf AAT_SC_Sample_Test_Suite_CSA_FT.tar \$FILENAME;
							export FILENAME=`ls data/global_parameter/*.json`;
							tar -uf AAT_SC_Sample_Test_Suite_CSA_FT.tar \$FILENAME;
						"""
					dir('5G_AAT') {	
						echo pwd()
						echo("Update the CSA FT Suite with the TLS Certificates ...")
						sh 'ls -al scripts'
						// sh "./bob/bob -r ruleset2.0-eric-aat.yaml deploy:update-cert-suite"
					}
					sh "cat data/jcatsut/*.json"

				} //script
            } // steps
        } // stage

		stage('Execute CSA FT Tests') {
			when {
				expression {
					return CSA_FT_TEST_SUITE == 'true';
				}
			}
			steps {
				dir('5g_test_ci') {
					script{
						// allocate ports and write to .bob/var.xxx files
						
						sh """./bob/bob adptest:ft-set-ports
						
						"""
					}
				}
				dir('5G_CDD/'){
					script {
						sh """export AAT_IP=`kubectl get svc aat-south-service --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[*].ip}'`;
							export AAT_PORT=`kubectl get svc aat-south-service --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath="{.spec.ports[0].port}"`;
							chmod +x swdp-5G-SC/5G-SC/src/aat_jenkins.py;
							
							DOCKER_RUN="docker run --rm  --user `id -u`:`id -g`"
                            BUILDER_DOCKER_IMAGE="armdocker.rnd.ericsson.se/sandbox/adp-staging/adp-cicd/bob-py3kubehelmbuilder:1.5.0-12"
                            BUILDER_DOCKER_VOLUMES="-v \${WORKSPACE}/helm-home:/home/jenkins/.helm -v \${WORKSPACE}:\${WORKSPACE}"
                            BUILDER_WRK_ENV="-w \${WORKSPACE}/5G_CDD/swdp-5G-SC/5G-SC/src"
							PYTHON_CMD="\$DOCKER_RUN \$BUILDER_DOCKER_VOLUMES \$BUILDER_WRK_ENV \$BUILDER_DOCKER_ENV  \$BUILDER_DOCKER_IMAGE"
							
							\$PYTHON_CMD  /bin/bash -c "pip install urllib3==1.25.11 requests==2.22.0 && python aat_jenkins.py https://\$AAT_IP:\$AAT_PORT \${WORKSPACE}/AAT_SC_Sample_Test_Suite_CSA_FT.tar"
							mv \${WORKSPACE}/5G_CDD/swdp-5G-SC/5G-SC/src/resultFile.json \${WORKSPACE}
							mv \${WORKSPACE}/5G_CDD/swdp-5G-SC/5G-SC/src/concl.properties \${WORKSPACE}
						
						"""
					} //script
				} // dirs
			
			} // steps
			post{
				always{
					archiveAndVerdict("CSA FT")
					archiveLogs("CSA FT", "${email_buildUrl}")
				}
			}
		} // stage

		//CSA NFT
		stage('Set CSA NFT suite for AAT') {
			when {
				expression {
					return CSA_NFT_TEST_SUITE == 'true';
				}
			}
            steps {
				script {
					sh """cp /proj/DSC/5g_aat/jenkins/AAT1.9.0/AAT_SC_Sample_Test_Suite_CSA_NFT.tar .
					tar -xf AAT_SC_Sample_Test_Suite_CSA_NFT.tar --wildcards 'data/jcatsut/*.json'
					tar -xf AAT_SC_Sample_Test_Suite_CSA_NFT.tar --wildcards 'data/tlsCertificate/*.json'
					export CMM_IP=`kubectl get svc eric-cm-yang-provider-external --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[*].ip}'`;
							export CMM_PORT=`kubectl get svc eric-cm-yang-provider-external --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath="{.spec.ports[0].port}"`;
							export SCP_IP=`kubectl get svc eric-scp-worker --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[*].ip}'`;
							export SCP_WRKR_PORT=`kubectl get svc eric-scp-worker --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath="{.spec.ports[0].port}"`;
							export TLS_PORT=443;
							export CERT=\$(cat .certificates/rootca/cert.pem)
							export KEY=\$(cat .certificates/rootca/key.pem)
							jq --arg CERT "\$CERT" '.certificate = \$CERT' data/tlsCertificate/*.json > testaki.json && mv -f testaki.json data/tlsCertificate/*.json
							jq --arg KEY "\$KEY" '.privateKey = \$KEY' data/tlsCertificate/*.json > testaki.json && mv -f testaki.json data/tlsCertificate/*.json

							sed -i '/"port":/ s/"port":[^,]*/"port":\'\$SCP_WRKR_PORT\'/' data/jcatsut/*.json;
							sed -i '/"tls-port":/ s/"tls-port":[^}]*/"tls-port":\'\$TLS_PORT\'/' data/jcatsut/*.json;
							sed -i '/"ipv4-address":/ s/"ipv4-address":[^,]*/"ipv4-address":"\'\$SCP_IP\'"/' data/jcatsut/*.json;
							sed -i '/{"ipv4-address":/ s/{"ipv4-address":[^,]*/{"ipv4-address":"\'\$CMM_IP\'"/' data/jcatsut/*.json;
							sed -i '/"port":/ s/"port":[^,]*/"port":\'\$CMM_PORT\'/2' data/jcatsut/*.json;
							sed -i '/"nameSpace":/ s/"nameSpace":[^,]*/"nameSpace":"\'\${NAMESPACE}\'"/' data/jcatsut/*.json;

							export FILENAME=`ls data/jcatsut/*.json`;
							tar -uf AAT_SC_Sample_Test_Suite_CSA_NFT.tar \$FILENAME;
							export FILENAME=`ls data/tlsCertificate/*.json`;
							tar -uf AAT_SC_Sample_Test_Suite_CSA_NFT.tar \$FILENAME;
					
						"""
					dir('5G_AAT') {	
						echo pwd()
						echo("Update the CSA NFT Suite with the TLS Certificates ...")
						sh 'ls -al scripts'
						// sh "./bob/bob -r ruleset2.0-eric-aat.yaml deploy:update-cert-suite"
					}
					sh "cat data/jcatsut/*.json"

				} //script
            } // steps
        } // stage

		stage('Execute CSA NFT Tests') {
			when {
				expression {
					return CSA_NFT_TEST_SUITE == 'true';
				}
			}
			steps {
				dir('5g_test_ci') {
					script{
						// allocate ports and write to .bob/var.xxx files
						
						sh """./bob/bob adptest:ft-set-ports
						
						"""
					}
				}
				dir('5G_CDD/'){
					script {
						sh """export AAT_IP=`kubectl get svc aat-south-service --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[*].ip}'`;
							export AAT_PORT=`kubectl get svc aat-south-service --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath="{.spec.ports[0].port}"`;
							chmod +x swdp-5G-SC/5G-SC/src/aat_jenkins.py;
							
							DOCKER_RUN="docker run --rm  --user `id -u`:`id -g`"
                            BUILDER_DOCKER_IMAGE="armdocker.rnd.ericsson.se/sandbox/adp-staging/adp-cicd/bob-py3kubehelmbuilder:1.5.0-12"
                            BUILDER_DOCKER_VOLUMES="-v \${WORKSPACE}/helm-home:/home/jenkins/.helm -v \${WORKSPACE}:\${WORKSPACE}"
                            BUILDER_WRK_ENV="-w \${WORKSPACE}/5G_CDD/swdp-5G-SC/5G-SC/src"
							PYTHON_CMD="\$DOCKER_RUN \$BUILDER_DOCKER_VOLUMES \$BUILDER_WRK_ENV \$BUILDER_DOCKER_ENV  \$BUILDER_DOCKER_IMAGE"
							
							\$PYTHON_CMD  /bin/bash -c "pip install urllib3==1.25.11 requests==2.22.0 && python aat_jenkins.py https://\$AAT_IP:\$AAT_PORT \${WORKSPACE}/AAT_SC_Sample_Test_Suite_CSA_NFT.tar"
							mv \${WORKSPACE}/5G_CDD/swdp-5G-SC/5G-SC/src/resultFile.json \${WORKSPACE}
							mv \${WORKSPACE}/5G_CDD/swdp-5G-SC/5G-SC/src/concl.properties \${WORKSPACE}
						
						"""
					} //script
				} // dirs
			
			} // steps
			post{
				always{
					archiveAndVerdict("CSA NFT")
					archiveLogs("CSA NFT", "${email_buildUrl}")
				}
			}
		} // stage

		//SCP
				stage ("Load SCP Configuration") {
			// when {
			// 	expression {
			// 		return SCP_TEST_SUITE == 'true';
			// 	}
			// }
			when {
				expression {
					return (SCP_FT_TEST_SUITE == 'true') || (SCP_NFT_TEST_SUITE == 'true');
				}
			}
			steps {
				dir('5G_AAT') {
					sh """
					./bob/bob -r ruleset2.0-eric-aat.yaml loadconfig:scp-AAT
					./bob/bob -r ruleset2.0-eric-aat.yaml deploy:update-cert-suite
					"""
				}

			} // steps
		} // stage

		//SCP NFT
        stage('Set SCP NFT suite for AAT') {
			when {
				expression {
					return SCP_NFT_TEST_SUITE == 'true';
				}
			}
            steps {
				script {
					sh """cp /proj/DSC/5g_aat/jenkins/AAT1.9.0/AAT_SC_Sample_Test_Suite_SCP_NFT.tar .
					tar -xf AAT_SC_Sample_Test_Suite_SCP_NFT.tar --wildcards 'data/jcatsut/*.json'
					tar -xf AAT_SC_Sample_Test_Suite_SCP_NFT.tar --wildcards 'data/tlsCertificate/*.json'
					tar -xf AAT_SC_Sample_Test_Suite_SCP_NFT.tar --wildcards 'data/clusterConfiguration/*.json'


					export CMM_IP=`kubectl get svc eric-cm-yang-provider-external --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[*].ip}'`;
							export CMM_PORT=`kubectl get svc eric-cm-yang-provider-external --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath="{.spec.ports[0].port}"`;
							export SCP_IP=`kubectl get svc eric-scp-worker --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[*].ip}'`;
							export SCP_WRKR_PORT=`kubectl get svc eric-scp-worker --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath="{.spec.ports[0].port}"`;
							export TLS_PORT=443;

							export CERT=\$(cat .certificates/rootca/cert.pem)
							export KEY=\$(cat .certificates/rootca/key.pem)
							export CONF=\$(cat ~/.kube/\$KUBE_HOST.config)
							jq --arg CERT "\$CERT" '.certificate = \$CERT' data/tlsCertificate/*.json > testaki.json && mv -f testaki.json data/tlsCertificate/*.json
							jq --arg KEY "\$KEY" '.privateKey = \$KEY' data/tlsCertificate/*.json > testaki.json && mv -f testaki.json data/tlsCertificate/*.json
							jq --arg CONF "\$CONF" '.clusterConfig = \$CONF' data/clusterConfiguration/*.json > testaki.json && mv -f testaki.json data/clusterConfiguration/*.json

							sed -i '/"port":/ s/"port":[^,]*/"port":\'\$SCP_WRKR_PORT\'/' data/jcatsut/*.json;
							sed -i '/"tls-port":/ s/"tls-port":[^}]*/"tls-port":\'\$TLS_PORT\'/' data/jcatsut/*.json;
							sed -i '/"ipv4-address":/ s/"ipv4-address":[^,]*/"ipv4-address":"\'\$SCP_IP\'"/' data/jcatsut/*.json;
							sed -i '/{"ipv4-address":/ s/{"ipv4-address":[^,]*/{"ipv4-address":"\'\$CMM_IP\'"/' data/jcatsut/*.json;
							sed -i '/"port":/ s/"port":[^,]*/"port":\'\$CMM_PORT\'/2' data/jcatsut/*.json;
							sed -i '/"nameSpace":/ s/"nameSpace":[^,]*/"nameSpace":"\'\${NAMESPACE}\'"/' data/jcatsut/*.json;

							export FILENAME=`ls data/jcatsut/*.json`;
							tar -uf AAT_SC_Sample_Test_Suite_SCP_NFT.tar \$FILENAME;
							export FILENAME=`ls data/tlsCertificate/*.json`;
							tar -uf AAT_SC_Sample_Test_Suite_SCP_NFT.tar \$FILENAME;
							export FILENAME=`ls data/clusterConfiguration/*.json`;
							tar -uf AAT_SC_Sample_Test_Suite_SCP_NFT.tar \$FILENAME;

						"""
					dir('5G_AAT') {	
						echo pwd()
						echo("Update the SCP NFT Suite with the TLS Certificates ...")
						sh 'ls -al scripts'
						// sh "./bob/bob -r ruleset2.0-eric-aat.yaml deploy:update-cert-suite"
					}
					sh "cat data/jcatsut/*.json"

				} //script
            } // steps
        } // stage

		stage('Execute SCP NFT Tests') {
			when {
				expression {
					return SCP_NFT_TEST_SUITE == 'true';
				}
			}
			steps {
				dir('5g_test_ci') {
					script{
						// allocate ports and write to .bob/var.xxx files
						
						sh """./bob/bob adptest:ft-set-ports
						
						"""
					}
				}
				dir('5G_CDD/'){
					script {
						sh """export AAT_IP=`kubectl get svc aat-south-service --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[*].ip}'`;
							export AAT_PORT=`kubectl get svc aat-south-service --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath="{.spec.ports[0].port}"`;
							chmod +x swdp-5G-SC/5G-SC/src/aat_jenkins.py;
							
							DOCKER_RUN="docker run --rm  --user `id -u`:`id -g`"
                            BUILDER_DOCKER_IMAGE="armdocker.rnd.ericsson.se/sandbox/adp-staging/adp-cicd/bob-py3kubehelmbuilder:1.5.0-12"
                            BUILDER_DOCKER_VOLUMES="-v \${WORKSPACE}/helm-home:/home/jenkins/.helm -v \${WORKSPACE}:\${WORKSPACE}"
                            BUILDER_WRK_ENV="-w \${WORKSPACE}/5G_CDD/swdp-5G-SC/5G-SC/src"
							PYTHON_CMD="\$DOCKER_RUN \$BUILDER_DOCKER_VOLUMES \$BUILDER_WRK_ENV \$BUILDER_DOCKER_ENV  \$BUILDER_DOCKER_IMAGE"
							
							\$PYTHON_CMD  /bin/bash -c "pip install urllib3==1.25.11 requests==2.22.0 && python aat_jenkins.py https://\$AAT_IP:\$AAT_PORT \${WORKSPACE}/AAT_SC_Sample_Test_Suite_SCP_NFT.tar"
							mv \${WORKSPACE}/5G_CDD/swdp-5G-SC/5G-SC/src/resultFile.json \${WORKSPACE}
							mv \${WORKSPACE}/5G_CDD/swdp-5G-SC/5G-SC/src/concl.properties \${WORKSPACE}
						
						"""
					} //script
				} // dirs
			
			} // steps
			post{
				always{
					archiveAndVerdict("SCP NFT")
					archiveLogs("SCP NFT", "${email_buildUrl}")
				}
			}
		} // stage

		//SCP FT
        stage('Set SCP FT suite for AAT') {
			when {
				expression {
					return SCP_FT_TEST_SUITE == 'true';
				}
			}
            steps {
				script {
					sh """cp /proj/DSC/5g_aat/jenkins/AAT1.9.0/AAT_SC_Sample_Test_Suite_SCP_FT.tar .
					tar -xf AAT_SC_Sample_Test_Suite_SCP_FT.tar --wildcards 'data/jcatsut/*.json'
					tar -xf AAT_SC_Sample_Test_Suite_SCP_FT.tar --wildcards 'data/tlsCertificate/*.json'

					export CMM_IP=`kubectl get svc eric-cm-yang-provider-external --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[*].ip}'`;
							export CMM_PORT=`kubectl get svc eric-cm-yang-provider-external --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath="{.spec.ports[0].port}"`;
							export SCP_IP=`kubectl get svc eric-scp-worker --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[*].ip}'`;
							export SCP_WRKR_PORT=`kubectl get svc eric-scp-worker --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath="{.spec.ports[0].port}"`;
							export TLS_PORT=443;
							export CERT=\$(cat .certificates/rootca/cert.pem)
							export KEY=\$(cat .certificates/rootca/key.pem)
							jq --arg CERT "\$CERT" '.certificate = \$CERT' data/tlsCertificate/*.json > testaki.json && mv -f testaki.json data/tlsCertificate/*.json
							jq --arg KEY "\$KEY" '.privateKey = \$KEY' data/tlsCertificate/*.json > testaki.json && mv -f testaki.json data/tlsCertificate/*.json
							sed -i '/"port":/ s/"port":[^,]*/"port":\'\$SCP_WRKR_PORT\'/' data/jcatsut/*.json;
							sed -i '/"tls-port":/ s/"tls-port":[^}]*/"tls-port":\'\$TLS_PORT\'/' data/jcatsut/*.json;
							sed -i '/"ipv4-address":/ s/"ipv4-address":[^,]*/"ipv4-address":"\'\$SCP_IP\'"/' data/jcatsut/*.json;
							sed -i '/{"ipv4-address":/ s/{"ipv4-address":[^,]*/{"ipv4-address":"\'\$CMM_IP\'"/' data/jcatsut/*.json;
							sed -i '/"port":/ s/"port":[^,]*/"port":\'\$CMM_PORT\'/2' data/jcatsut/*.json;
							sed -i '/"nameSpace":/ s/"nameSpace":[^,]*/"nameSpace":"\'\${NAMESPACE}\'"/' data/jcatsut/*.json;

							export FILENAME=`ls data/jcatsut/*.json`;
							tar -uf AAT_SC_Sample_Test_Suite_SCP_FT.tar \$FILENAME;
							export FILENAME=`ls data/tlsCertificate/*.json`;
							tar -uf AAT_SC_Sample_Test_Suite_SCP_FT.tar \$FILENAME;
					
						"""
					dir('5G_AAT') {	
						echo pwd()
						echo("Update the SCP FT Suite with the TLS Certificates ...")
						sh 'ls -al scripts'
						// sh "./bob/bob -r ruleset2.0-eric-aat.yaml deploy:update-cert-suite"
					}
					sh "cat data/jcatsut/*.json"

				} //script
            } // steps
        } // stage

		stage('Execute SCP FT Tests') {
			when {
				expression {
					return SCP_FT_TEST_SUITE == 'true';
				}
			}
			steps {
				dir('5g_test_ci') {
					script{
						// allocate ports and write to .bob/var.xxx files
						
						sh """./bob/bob adptest:ft-set-ports
						
						"""
					}
				}
				dir('5G_CDD/'){
					script {
						sh """export AAT_IP=`kubectl get svc aat-south-service --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[*].ip}'`;
							export AAT_PORT=`kubectl get svc aat-south-service --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath="{.spec.ports[0].port}"`;
							chmod +x swdp-5G-SC/5G-SC/src/aat_jenkins.py;
							
							DOCKER_RUN="docker run --rm  --user `id -u`:`id -g`"
                            BUILDER_DOCKER_IMAGE="armdocker.rnd.ericsson.se/sandbox/adp-staging/adp-cicd/bob-py3kubehelmbuilder:1.5.0-12"
                            BUILDER_DOCKER_VOLUMES="-v \${WORKSPACE}/helm-home:/home/jenkins/.helm -v \${WORKSPACE}:\${WORKSPACE}"
                            BUILDER_WRK_ENV="-w \${WORKSPACE}/5G_CDD/swdp-5G-SC/5G-SC/src"
							PYTHON_CMD="\$DOCKER_RUN \$BUILDER_DOCKER_VOLUMES \$BUILDER_WRK_ENV \$BUILDER_DOCKER_ENV  \$BUILDER_DOCKER_IMAGE"
							
							\$PYTHON_CMD  /bin/bash -c "pip install urllib3==1.25.11 requests==2.22.0 && python aat_jenkins.py https://\$AAT_IP:\$AAT_PORT \${WORKSPACE}/AAT_SC_Sample_Test_Suite_SCP_FT.tar"
							mv \${WORKSPACE}/5G_CDD/swdp-5G-SC/5G-SC/src/resultFile.json \${WORKSPACE}
							mv \${WORKSPACE}/5G_CDD/swdp-5G-SC/5G-SC/src/concl.properties \${WORKSPACE}
						
						"""
					} //script
				} // dirs
			
			} // steps
			post{
				always{
					archiveAndVerdict("SCP FT")
					archiveLogs("SCP FT", "${email_buildUrl}")
				}
			}
		} // stage

		//SEPP NFT
		stage ("Load SEPP Configuration") {
			when {
				expression {
					return (SEPP_FT_TEST_SUITE == 'true') || (SEPP_NFT_TEST_SUITE == 'true');
				}
			}
			steps {
				dir('5G_AAT') {
					sh """
					./bob/bob -r ruleset2.0-eric-aat.yaml loadconfig:sepp-AAT
					./bob/bob -r ruleset2.0-eric-aat.yaml deploy:update-cert-suite
					"""
				}

			} // steps
		} // stage

		
        stage('Set SEPP NFT suite for AAT') {
			when {
				expression {
					return SEPP_NFT_TEST_SUITE == 'true';
				}
			}
            steps {
				script {
					sh """cp /proj/DSC/5g_aat/jenkins/AAT1.9.0/AAT_SC_Sample_Test_Suite_SEPP_NFT.tar .
					tar -xf AAT_SC_Sample_Test_Suite_SEPP_NFT.tar --wildcards 'data/jcatsut/*.json'
					tar -xf AAT_SC_Sample_Test_Suite_SEPP_NFT.tar --wildcards 'data/tlsCertificate/*.json'
					files=(\$(find data/tlsCertificate -type f -name "*.json"))

					export CMM_IP=`kubectl get svc eric-cm-yang-provider-external --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[*].ip}'`;
							export CMM_PORT=`kubectl get svc eric-cm-yang-provider-external --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath="{.spec.ports[0].port}"`;
							export SEPP_IP=`kubectl get svc eric-sepp-worker --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[*].ip}'`;
							export SEPP_WRKR_PORT=`kubectl get svc eric-sepp-worker --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath="{.spec.ports[0].port}"`;
							export SEPP_IP_2=`kubectl get svc eric-sepp-worker-2 --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[*].ip}'`;
							export SEPP_WRKR_PORT_2=`kubectl get svc eric-sepp-worker-2 --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath="{.spec.ports[0].port}"`;
							export TLS_PORT=443;
							export CERT_INT=\$(cat .certificates/seppwrk-int-ca/cert.pem)
							export KEY_INT=\$(cat .certificates/seppwrk-int-ca/key.pem)
							export CERT_EXT=\$(cat .certificates/seppwrk-ext-ca/cert.pem)
							export KEY_EXT=\$(cat .certificates/seppwrk-ext-ca/key.pem)

							for file in "\${files[@]}"; do if grep -q "int" "\$file"; then jq --arg CERT_INT "\$CERT_INT" '.certificate = \$CERT_INT' \$file > testaki.json && mv -f testaki.json \$file; fi; done
							for file in "\${files[@]}"; do if grep -q "int" "\$file"; then jq --arg KEY_INT "\$KEY_INT" '.privateKey = \$KEY_INT' \$file > testaki.json && mv -f testaki.json \$file; fi; done
							for file in "\${files[@]}"; do if grep -q "ext" "\$file"; then jq --arg CERT_EXT "\$CERT_EXT" '.certificate = \$CERT_EXT' \$file > testaki.json && mv -f testaki.json \$file; fi; done
							for file in "\${files[@]}"; do if grep -q "ext" "\$file"; then jq --arg KEY_EXT "\$KEY_EXT" '.privateKey = \$KEY_EXT' \$file > testaki.json && mv -f testaki.json \$file; fi; done

							sed -i '/"port":/ s/"port":[^,]*/"port":\'\$SEPP_WRKR_PORT\'/' data/jcatsut/*.json;
							sed -i '/"tls-port":/ s/"tls-port":[^}]*/"tls-port":\'\$TLS_PORT\'/' data/jcatsut/*.json;
							sed -i '/"ipv4-address":/ s/"ipv4-address":[^,]*/"ipv4-address":"\'\$SEPP_IP\'"/' data/jcatsut/*.json;
							sed -i '/{"ipv4-address":/ s/{"ipv4-address":[^,]*/{"ipv4-address":"\'\$CMM_IP\'"/' data/jcatsut/*.json;
							sed -i '/"port":/ s/"port":[^,]*/"port":\'\$CMM_PORT\'/2' data/jcatsut/*.json;
							sed -i '/"nameSpace":/ s/"nameSpace":[^,]*/"nameSpace":"\'\${NAMESPACE}\'"/' data/jcatsut/*.json;

							export FILENAME=`ls data/jcatsut/*.json`;
							tar -uf AAT_SC_Sample_Test_Suite_SEPP_NFT.tar \$FILENAME;
							for file in "\${files[@]}"; do tar -uf AAT_SC_Sample_Test_Suite_SEPP_FT.tar \$file; done
					
						"""
					dir('5G_AAT') {	
						echo pwd()
						echo("Update the SEPP NFT Suite with the TLS Certificates ...")
						sh 'ls -al scripts'
						// sh "./bob/bob -r ruleset2.0-eric-aat.yaml deploy:update-cert-suite"
					}
					sh "cat data/jcatsut/*.json"

				} //script
            } // steps
        } // stage

		stage('Execute SEPP NFT Tests') {
			when {
				expression {
					return SEPP_NFT_TEST_SUITE == 'true';
				}
			}
			steps {
				dir('5g_test_ci') {
					script{
						// allocate ports and write to .bob/var.xxx files
						
						sh """./bob/bob adptest:ft-set-ports
						
						"""
					}
				}
				dir('5G_CDD/'){
					script {
						sh """export AAT_IP=`kubectl get svc aat-south-service --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[*].ip}'`;
							export AAT_PORT=`kubectl get svc aat-south-service --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath="{.spec.ports[0].port}"`;
							chmod +x swdp-5G-SC/5G-SC/src/aat_jenkins.py;
							
							DOCKER_RUN="docker run --rm  --user `id -u`:`id -g`"
                            BUILDER_DOCKER_IMAGE="armdocker.rnd.ericsson.se/sandbox/adp-staging/adp-cicd/bob-py3kubehelmbuilder:1.5.0-12"
                            BUILDER_DOCKER_VOLUMES="-v \${WORKSPACE}/helm-home:/home/jenkins/.helm -v \${WORKSPACE}:\${WORKSPACE}"
                            BUILDER_WRK_ENV="-w \${WORKSPACE}/5G_CDD/swdp-5G-SC/5G-SC/src"
							PYTHON_CMD="\$DOCKER_RUN \$BUILDER_DOCKER_VOLUMES \$BUILDER_WRK_ENV \$BUILDER_DOCKER_ENV  \$BUILDER_DOCKER_IMAGE"
							
							\$PYTHON_CMD  /bin/bash -c "pip install urllib3==1.25.11 requests==2.22.0 && python aat_jenkins.py https://\$AAT_IP:\$AAT_PORT \${WORKSPACE}/AAT_SC_Sample_Test_Suite_SEPP_NFT.tar"
							mv \${WORKSPACE}/5G_CDD/swdp-5G-SC/5G-SC/src/resultFile.json \${WORKSPACE}
							mv \${WORKSPACE}/5G_CDD/swdp-5G-SC/5G-SC/src/concl.properties \${WORKSPACE}
						
						"""
					} //script
				} // dirs
			
			} // steps
			post{
				always{
					archiveAndVerdict("SEPP NFT")
					archiveLogs("SEPP NFT", "${email_buildUrl}")
				}
			}
		} // stage

		//SEPP FT
        stage('Set SEPP FT suite for AAT') {
			when {
				expression {
					return SEPP_FT_TEST_SUITE == 'true';
				}
			}
            steps {
				script {
					sh """cp /proj/DSC/5g_aat/jenkins/AAT1.9.0/AAT_SC_Sample_Test_Suite_SEPP_FT.tar .
					tar -xf AAT_SC_Sample_Test_Suite_SEPP_FT.tar --wildcards 'data/jcatsut/*.json'
					tar -xf AAT_SC_Sample_Test_Suite_SEPP_FT.tar --wildcards 'data/tlsCertificate/*.json'
					files=(\$(find data/tlsCertificate -type f -name "*.json"))

					export CMM_IP=`kubectl get svc eric-cm-yang-provider-external --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[*].ip}'`;
							export CMM_PORT=`kubectl get svc eric-cm-yang-provider-external --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath="{.spec.ports[0].port}"`;
							export SEPP_IP=`kubectl get svc eric-sepp-worker --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[*].ip}'`;
							export SEPP_WRKR_PORT=`kubectl get svc eric-sepp-worker --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath="{.spec.ports[0].port}"`;
							export SEPP_IP_2=`kubectl get svc eric-sepp-worker-2 --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[*].ip}'`;
							export SEPP_WRKR_PORT_2=`kubectl get svc eric-sepp-worker-2 --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath="{.spec.ports[0].port}"`;
							export TLS_PORT=443;
							export CERT_INT=\$(cat .certificates/seppwrk-int-ca/cert.pem)
							export KEY_INT=\$(cat .certificates/seppwrk-int-ca/key.pem)
							export CERT_EXT=\$(cat .certificates/seppwrk-ext-ca/cert.pem)
							export KEY_EXT=\$(cat .certificates/seppwrk-ext-ca/key.pem)

							for file in "\${files[@]}"; do if grep -q "int" "\$file"; then jq --arg CERT_INT "\$CERT_INT" '.certificate = \$CERT_INT' \$file > testaki.json && mv -f testaki.json \$file; fi; done
							for file in "\${files[@]}"; do if grep -q "int" "\$file"; then jq --arg KEY_INT "\$KEY_INT" '.privateKey = \$KEY_INT' \$file > testaki.json && mv -f testaki.json \$file; fi; done
							for file in "\${files[@]}"; do if grep -q "ext" "\$file"; then jq --arg CERT_EXT "\$CERT_EXT" '.certificate = \$CERT_EXT' \$file > testaki.json && mv -f testaki.json \$file; fi; done
							for file in "\${files[@]}"; do if grep -q "ext" "\$file"; then jq --arg KEY_EXT "\$KEY_EXT" '.privateKey = \$KEY_EXT' \$file > testaki.json && mv -f testaki.json \$file; fi; done

							sed -i '/"port":/ s/"port":[^,]*/"port":\'\$SEPP_WRKR_PORT\'/' data/jcatsut/*.json;
							sed -i '/"tls-port":/ s/"tls-port":[^}]*/"tls-port":\'\$TLS_PORT\'/' data/jcatsut/*.json;
							sed -i '/"ipv4-address":/ s/"ipv4-address":[^,]*/"ipv4-address":"\'\$SEPP_IP\'"/' data/jcatsut/*.json;
							sed -i '/{"ipv4-address":/ s/{"ipv4-address":[^,]*/{"ipv4-address":"\'\$CMM_IP\'"/' data/jcatsut/*.json;
							sed -i '/"port":/ s/"port":[^,]*/"port":\'\$CMM_PORT\'/2' data/jcatsut/*.json;
							sed -i '/"nameSpace":/ s/"nameSpace":[^,]*/"nameSpace":"\'\${NAMESPACE}\'"/' data/jcatsut/*.json;

							export FILENAME=`ls data/jcatsut/*.json`;
							tar -uf AAT_SC_Sample_Test_Suite_SEPP_FT.tar \$FILENAME;
							for file in "\${files[@]}"; do tar -uf AAT_SC_Sample_Test_Suite_SEPP_FT.tar \$file; done
					
						"""
					dir('5G_AAT') {	
						echo pwd()
						echo("Update the SEPP FT Suite with the TLS Certificates ...")
						sh 'ls -al scripts'
						// sh "./bob/bob -r ruleset2.0-eric-aat.yaml deploy:update-cert-suite"
					}
					sh "cat data/jcatsut/*.json"

				} //script
            } // steps
        } // stage

		stage('Execute SEPP FT Tests') {
			when {
				expression {
					return SEPP_FT_TEST_SUITE == 'true';
				}
			}
			steps {
				dir('5g_test_ci') {
					script{
						// allocate ports and write to .bob/var.xxx files
						
						sh """./bob/bob adptest:ft-set-ports
						
						"""
					}
				}
				dir('5G_CDD/'){
					script {
						sh """export AAT_IP=`kubectl get svc aat-south-service --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[*].ip}'`;
							export AAT_PORT=`kubectl get svc aat-south-service --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath="{.spec.ports[0].port}"`;
							chmod +x swdp-5G-SC/5G-SC/src/aat_jenkins.py;
							
							DOCKER_RUN="docker run --rm  --user `id -u`:`id -g`"
                            BUILDER_DOCKER_IMAGE="armdocker.rnd.ericsson.se/sandbox/adp-staging/adp-cicd/bob-py3kubehelmbuilder:1.5.0-12"
                            BUILDER_DOCKER_VOLUMES="-v \${WORKSPACE}/helm-home:/home/jenkins/.helm -v \${WORKSPACE}:\${WORKSPACE}"
                            BUILDER_WRK_ENV="-w \${WORKSPACE}/5G_CDD/swdp-5G-SC/5G-SC/src"
							PYTHON_CMD="\$DOCKER_RUN \$BUILDER_DOCKER_VOLUMES \$BUILDER_WRK_ENV \$BUILDER_DOCKER_ENV  \$BUILDER_DOCKER_IMAGE"
							
							\$PYTHON_CMD  /bin/bash -c "pip install urllib3==1.25.11 requests==2.22.0 && python aat_jenkins.py https://\$AAT_IP:\$AAT_PORT \${WORKSPACE}/AAT_SC_Sample_Test_Suite_SEPP_FT.tar"
							mv \${WORKSPACE}/5G_CDD/swdp-5G-SC/5G-SC/src/resultFile.json \${WORKSPACE}
							mv \${WORKSPACE}/5G_CDD/swdp-5G-SC/5G-SC/src/concl.properties \${WORKSPACE}
						
						"""
					} //script
				} // dirs
			
			} // steps
			post{
				always{
					archiveAndVerdict("SEPP FT")
					archiveLogs("SEPP FT", "${email_buildUrl}")
				}
			}
		} // stage

		//SC_Diameter NFT
		stage ("Load SC Diameter Configuration") {
			when {
				expression {
					return (SC_DIAMETER_FT_TEST_SUITE == 'true') || (SC_DIAMETER_NFT_TEST_SUITE == 'true');
				}
			}
			steps {
				dir('5G_AAT') {
					sh """
					sleep 300;
					./bob/bob -r ruleset2.0-eric-aat.yaml loadconfig:sc_diameter-AAT
					./bob/bob -r ruleset2.0-eric-aat.yaml deploy:update-cert-suite
					"""
				}

			} // steps
		} // stage

		
        stage('Set SC Diameter NFT suite for AAT') {
			when {
				expression {
					return SC_DIAMETER_NFT_TEST_SUITE == 'true';
				}
			}
            steps {
				script {
					sh """cp /proj/DSC/5g_aat/jenkins/AAT1.9.0/AAT_SC_Sample_Test_Suite_SC_Diameter_NFT.tar .
					tar -xf AAT_SC_Sample_Test_Suite_SC_Diameter_NFT.tar --wildcards 'data/jcatsut/*.json'
					export CMM_IP=`kubectl get svc eric-cm-yang-provider-external --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace dsc-ci -o jsonpath='{.status.loadBalancer.ingress[*].ip}'`;
							export CMM_PORT=`kubectl get svc eric-cm-yang-provider-external --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace dsc-ci -o jsonpath="{.spec.ports[0].port}"`;
							export FDR_IP=`kubectl get svc eric-dsc-fdr --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace dsc-ci -o jsonpath='{.status.loadBalancer.ingress[*].ip}'`;
							export FDR_PORT=`kubectl get svc eric-dsc-fdr --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace dsc-ci -o jsonpath="{.spec.ports[0].port}"`;
							export FDR_SCTP_IP=`kubectl get svc eric-dsc-fdr-sctp --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace dsc-ci -o jsonpath='{.status.loadBalancer.ingress[*].ip}'`;
							export FDR_SCTP_PORT=`kubectl get svc eric-dsc-fdr-sctp --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace dsc-ci -o jsonpath="{.spec.ports[0].port}"`;
							export TLS_PORT=443;
							
							sed -i '/"port":/ s/"port":[^,]*/"port":\'\$FDR_PORT\'/' data/jcatsut/*.json;
							sed -i '/"tls-port":/ s/"tls-port":[^}]*/"tls-port":\'\$TLS_PORT\'/' data/jcatsut/*.json;
							sed -i '/"ipv4-address":/ s/"ipv4-address":[^,]*/"ipv4-address":"\'\$FDR_IP\'"/' data/jcatsut/*.json;
							sed -i '/{"ipv4-address":/ s/{"ipv4-address":[^,]*/{"ipv4-address":"\'\$CMM_IP\'"/' data/jcatsut/*.json;
							sed -i '/"port":/ s/"port":[^,]*/"port":\'\$CMM_PORT\'/2' data/jcatsut/*.json;
							sed -i '/"nameSpace":/ s/"nameSpace":[^,]*/"nameSpace":"dsc-ci"/' data/jcatsut/*.json;

							export FILENAME=`ls data/jcatsut/*.json`;
							tar -uf AAT_SC_Sample_Test_Suite_SC_Diameter_NFT.tar \$FILENAME;
					
						"""
					dir('5G_AAT') {	
						echo pwd()
						echo("Update the SC Diameter NFT Suite with the TLS Certificates ...")
						sh 'ls -al scripts'
						// sh "./bob/bob -r ruleset2.0-eric-aat.yaml deploy:update-cert-suite"
					}
					sh "cat data/jcatsut/*.json"

				} //script
            } // steps
        } // stage

		stage('Execute SC Diameter NFT Tests') {
			when {
				expression {
					return SC_DIAMETER_NFT_TEST_SUITE == 'true';
				}
			}
			steps {
				dir('5g_test_ci') {
					script{
						// allocate ports and write to .bob/var.xxx files
						
						sh """./bob/bob adptest:ft-set-ports
						
						"""
					}
				}
				dir('5G_CDD/'){
					script {
						sh """export AAT_IP=`kubectl get svc aat-south-service --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[*].ip}'`;
							export AAT_PORT=`kubectl get svc aat-south-service --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath="{.spec.ports[0].port}"`;
							chmod +x swdp-5G-SC/5G-SC/src/aat_jenkins.py;
							
							DOCKER_RUN="docker run --rm  --user `id -u`:`id -g`"
                            BUILDER_DOCKER_IMAGE="armdocker.rnd.ericsson.se/sandbox/adp-staging/adp-cicd/bob-py3kubehelmbuilder:1.5.0-12"
                            BUILDER_DOCKER_VOLUMES="-v \${WORKSPACE}/helm-home:/home/jenkins/.helm -v \${WORKSPACE}:\${WORKSPACE}"
                            BUILDER_WRK_ENV="-w \${WORKSPACE}/5G_CDD/swdp-5G-SC/5G-SC/src"
							PYTHON_CMD="\$DOCKER_RUN \$BUILDER_DOCKER_VOLUMES \$BUILDER_WRK_ENV \$BUILDER_DOCKER_ENV  \$BUILDER_DOCKER_IMAGE"
							
							\$PYTHON_CMD  /bin/bash -c "pip install urllib3==1.25.11 requests==2.22.0 && python aat_jenkins.py https://\$AAT_IP:\$AAT_PORT \${WORKSPACE}/AAT_SC_Sample_Test_Suite_SC_Diameter_NFT.tar"
							mv \${WORKSPACE}/5G_CDD/swdp-5G-SC/5G-SC/src/resultFile.json \${WORKSPACE}
							mv \${WORKSPACE}/5G_CDD/swdp-5G-SC/5G-SC/src/concl.properties \${WORKSPACE}
						
						"""
					} //script
				} // dirs
			
			} // steps
			post{
				always{
					archiveAndVerdict("SC Diameter NFT")
					archiveLogs("SC Diameter NFT", "${email_buildUrl}")
				}
			}
		} // stage

		//SC Diameter FT
        stage('Set SC Diameter FT suite for AAT') {
			when {
				expression {
					return SC_DIAMETER_FT_TEST_SUITE == 'true';
				}
			}
            steps {
				script {
					sh """cp /proj/DSC/5g_aat/jenkins/AAT1.9.0/AAT_SC_Sample_Test_Suite_SC_Diameter_FT.tar .
					tar -xf AAT_SC_Sample_Test_Suite_SC_Diameter_FT.tar --wildcards 'data/jcatsut/*.json'
					export CMM_IP=`kubectl get svc eric-cm-yang-provider-external --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace dsc-ci -o jsonpath='{.status.loadBalancer.ingress[*].ip}'`;
							export CMM_PORT=`kubectl get svc eric-cm-yang-provider-external --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace dsc-ci -o jsonpath="{.spec.ports[0].port}"`;
							export FDR_IP=`kubectl get svc eric-dsc-fdr --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace dsc-ci -o jsonpath='{.status.loadBalancer.ingress[*].ip}'`;
							export FDR_PORT=`kubectl get svc eric-dsc-fdr --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace dsc-ci -o jsonpath="{.spec.ports[0].port}"`;
							export FDR_SCTP_IP=`kubectl get svc eric-dsc-fdr-sctp --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace dsc-ci -o jsonpath='{.status.loadBalancer.ingress[*].ip}'`;
							export FDR_SCTP_PORT=`kubectl get svc eric-dsc-fdr-sctp --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace dsc-ci -o jsonpath="{.spec.ports[0].port}"`;
							export TLS_PORT=443;
							
							sed -i '/"port":/ s/"port":[^,]*/"port":\'\$FDR_PORT\'/' data/jcatsut/*.json;
							sed -i '/"tls-port":/ s/"tls-port":[^}]*/"tls-port":\'\$TLS_PORT\'/' data/jcatsut/*.json;
							sed -i '/"ipv4-address":/ s/"ipv4-address":[^,]*/"ipv4-address":"\'\$FDR_IP\'"/' data/jcatsut/*.json;
							sed -i '/{"ipv4-address":/ s/{"ipv4-address":[^,]*/{"ipv4-address":"\'\$CMM_IP\'"/' data/jcatsut/*.json;
							sed -i '/"port":/ s/"port":[^,]*/"port":\'\$CMM_PORT\'/2' data/jcatsut/*.json;
							sed -i '/"nameSpace":/ s/"nameSpace":[^,]*/"nameSpace":"dsc-ci"/' data/jcatsut/*.json;

							export FILENAME=`ls data/jcatsut/*.json`;
							tar -uf AAT_SC_Sample_Test_Suite_SC_Diameter_FT.tar \$FILENAME;
					
						"""
					dir('5G_AAT') {	
						echo pwd()
						echo("Update the SC Diameter FT Suite with the TLS Certificates ...")
						sh 'ls -al scripts'
						// sh "./bob/bob -r ruleset2.0-eric-aat.yaml deploy:update-cert-suite"
					}
					sh "cat data/jcatsut/*.json"

				} //script
            } // steps
        } // stage

		stage('Execute SC Diameter FT Tests') {
			when {
				expression {
					return SC_DIAMETER_FT_TEST_SUITE == 'true';
				}
			}
			steps {
				dir('5g_test_ci') {
					script{
						// allocate ports and write to .bob/var.xxx files
						
						sh """./bob/bob adptest:ft-set-ports
						
						"""
					}
				}
				dir('5G_CDD/'){
					script {
						sh """export AAT_IP=`kubectl get svc aat-south-service --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[*].ip}'`;
							export AAT_PORT=`kubectl get svc aat-south-service --kubeconfig ~/.kube/\$KUBE_HOST.config --namespace \${NAMESPACE} -o jsonpath="{.spec.ports[0].port}"`;
							chmod +x swdp-5G-SC/5G-SC/src/aat_jenkins.py;
							
							DOCKER_RUN="docker run --rm  --user `id -u`:`id -g`"
                            BUILDER_DOCKER_IMAGE="armdocker.rnd.ericsson.se/sandbox/adp-staging/adp-cicd/bob-py3kubehelmbuilder:1.5.0-12"
                            BUILDER_DOCKER_VOLUMES="-v \${WORKSPACE}/helm-home:/home/jenkins/.helm -v \${WORKSPACE}:\${WORKSPACE}"
                            BUILDER_WRK_ENV="-w \${WORKSPACE}/5G_CDD/swdp-5G-SC/5G-SC/src"
							PYTHON_CMD="\$DOCKER_RUN \$BUILDER_DOCKER_VOLUMES \$BUILDER_WRK_ENV \$BUILDER_DOCKER_ENV  \$BUILDER_DOCKER_IMAGE"
							
							\$PYTHON_CMD  /bin/bash -c "pip install urllib3==1.25.11 requests==2.22.0 && python aat_jenkins.py https://\$AAT_IP:\$AAT_PORT \${WORKSPACE}/AAT_SC_Sample_Test_Suite_SC_Diameter_FT.tar"
							mv \${WORKSPACE}/5G_CDD/swdp-5G-SC/5G-SC/src/resultFile.json \${WORKSPACE}
							mv \${WORKSPACE}/5G_CDD/swdp-5G-SC/5G-SC/src/concl.properties \${WORKSPACE}
						
						"""
					} //script
				} // dirs
			
			} // steps
			post{
				always{
					archiveAndVerdict("SC Diameter FT")
					archiveLogs("SC Diameter FT", "${email_buildUrl}" )
				}
			}
		} // stage
	}

	post{
		failure{
			// collecting ADP logs in case of pipeline failures removed (agreed in A-Team workshop)
			script {
				// clean cluster due to failure in pipeline
				if (env.CLEANCLUSTER.equals("TRUE")) {
					sh "/home/eiffelesc/scripts/cleanCluster `cat .bob/var.namespace` ${env.KUBE_HOST}"
				}

				def COLOR = "<body style=\"background-color: LightYellow;\">"
				echo "$COLOR"
				emailext body: "$COLOR"+"Hello AAT team,<br><br> AAT test suite results for ${BRANCH} ${CHART_VERSION}.<br>Please check: ${email_buildUrl} <br><br> Verification pipeline has <b><font color=\"#FF0000\"> Failed </font></b>",
				mimeType: 'text/html',
				subject: "AAT test suite results ${BRANCH} ${CHART_VERSION}",
				to: "c91010d0.ericsson.onmicrosoft.com@emea.teams.ms"
			} //script
		} //failure
	} //post
} //pipeline


