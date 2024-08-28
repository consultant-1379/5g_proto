def AGENT_LABEL = null

node('DSC_BuildSlave_GIC_1 ||DSC_BuildSlave_GIC_2||DSC_BuildSlave_GIC_3||DSC_BuildSlave_GIC_4||DSC_BuildSlave_GIC_5') {

        AGENT_LABEL = 'esc-docker||esc-docker-1||esc-docker-2'

} //node

//
//////////////////////////////////////////////////////////////////////////

def TEST_VAR = "Comp Build Up"


pipeline {

    agent { label "${AGENT_LABEL}" } // set label according to IP_VERSION parameter

    options{
        timeout(time: 10, unit: 'MINUTES')
        timestamps()
    }

    stages{
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
		stage('checkout the correct commit') {
		     steps {
		        script {
		            echo "TEST_TYPE: ${env.TEST_TYPE}"
		            echo "COMMIT: ${env.COMMIT}"
		            echo "BRANCH: ${env.BRANCH}"

		            // set the commit according to the TEST_TYPE
		            // TEST_TYPE is CI -> take the commit and the chart-version(!) of the SmokeTested SW
		            if ("CI".equals(env.TEST_TYPE)) {
		               echo "TestType is ${env.TEST_TYPE}, reading COMMIT from the last SmokeTest push:"
		               env.COMMIT = """${sh( returnStdout: true, script: 'git log -n1 --grep="new version in baseline" --format="%B" | awk \'{print $8}\'' )}""".trim();
		               env.CHART_VERSION = """${sh( returnStdout: true, script: 'git log -n1 --grep="new version in baseline" --format="%B" | awk \'{gsub(",","",$0); print $7}\'' )}""".trim();
		               echo "CHART_VERSION: ${env.CHART_VERSION}"
		            // TEST_TYPE is SmokeTest|TeamCI|JenkinsJob -> the commit is set by Spinnaker or the Jenkins Job, nothing to do
		            } else if (("SmokeTest".equals(env.TEST_TYPE)) || "TeamCI".equals(env.TEST_TYPE) || "JenkinsJob".equals(env.TEST_TYPE)) {
		               echo "TestType is ${env.TEST_TYPE}, checking out specified commit:"
		            // everything else -> depends on the commit value
		            //} else if ("JenkinsJob".equals(env.TEST_TYPE)) {
		            //   echo "TestType is JenkinsJob, checking out specified commit:"
		            // TEST_TYPE is SmokeTest -> the commit is set by the build job and handed over to this job, nothing to do
		            } else {
		               echo "No or unknown TestType set, COMMIT ${env.COMMIT} is checked out, good luck!"
		            }// if
		            echo "Checking out ${env.COMMIT}"
		            checkout scmGit(
		                branches: [[name: env.COMMIT]],
		            	extensions: [submodule(parentCredentials: true, recursiveSubmodules: true, reference: '', shallow: false), [$class: 'WipeWorkspace']], 
		                userRemoteConfigs: [[credentialsId: 'eiffelesc-user-password', name: 'origin', url: 'https://eiffelesc@gerrit.ericsson.se/a/MC_5G/5g_proto']]
		            ) // checkout
	//                   sh "git remote set-url origin --push https://eiffelesc@gerrit.ericsson.se/a/MC_5G/5g_proto"
		            // set the displayed build name to "BUILD_NUMBER - COMMIT"
		            currentBuild.displayName = "#${BUILD_NUMBER} - ${env.COMMIT}"
		        } // script
		    }  // steps 
		}//stage
		stage('Collecting logs') {
			when {
				environment name: 'TROUBLESHOOTING', value: 'TRUE';
			}
			steps{
			    script {
					sh "echo TIMEOUT is set to 10 minutes ! Good luck! If fails Ignoe the fail!"
					sh "./scripts/collect_ADP_logs.sh -c ~/.kube/${env.KUBE_HOST}.config -n ${NAMESPACE}"
					archiveArtifacts allowEmptyArchive: true, artifacts: 'logs_*.tgz'
				} //script
			} //steps
		} //stage 
	} //stages
} //pipeline
