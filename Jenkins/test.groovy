def AGENT_LABEL = null

node('DSC_BuildSlave_GIC_1 ||DSC_BuildSlave_GIC_2||DSC_BuildSlave_GIC_3||DSC_BuildSlave_GIC_4||DSC_BuildSlave_GIC_5') {

        AGENT_LABEL = 'esc-docker-2' //||esc-docker-1||esc-docker-2'

} //node

//
//////////////////////////////////////////////////////////////////////////

def TEST_VAR = "Comp Build Up"


pipeline {

    agent { label "${AGENT_LABEL}" } // set label according to IP_VERSION parameter

    options{
        timeout(time: 1800, unit: 'MINUTES')
        timestamps()
    }

    environment{
        REQUIREMENTS = "esc/helm/eric-sc-umbrella/requirements.yaml"
        //local vars
        CONFILE = ''
        cnt = 1
        // general variable
        GROOVY = "groovy"
        MASTER = ''
        // Variables needed for the ruleset

        // Jenkins Variables
    }
    stages{
        stage('Freeing up workspace on other buildslaves'){
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

                }
            }
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
                      //userRemoteConfigs: [[url: 'https://eiffelesc@gerrit.ericsson.se/a/MC_5G/5g_proto']]
                        userRemoteConfigs: [[credentialsId: 'eiffelesc-user-password', name: 'origin', url: 'https://eiffelesc@gerritmirror-direct.sero.gic.ericsson.se/a/MC_5G/5g_proto']]
                    ) // checkout
                    // set the displayed build name to "BUILD_NUMBER - COMMIT"
                    currentBuild.displayName = "#${BUILD_NUMBER} - ${env.COMMIT}"
                } // script
            }  // steps 
        }//stage
        stage('Merging latest running Master for 5g_proto'){
            steps{
                withCredentials([usernamePassword(credentialsId: '4d8761fe-e925-4638-bf28-97744f277be6', usernameVariable: 'USER', passwordVariable: 'PASSWORD')]) {
                    sh 'wget -O- --auth-no-challenge --user=$USER --password=$PASSWORD https://fem1s10-eiffel029.eiffel.gic.ericsson.se:8443/jenkins/job/5G-ESC/job/CI_on_Master/job/5G-SC-CI-master-test-pipeline/lastSuccessfulBuild/api/json > json.file'
                }
			    script{
		            MASTER = """${sh( returnStdout: true, script: 'cat json.file | jq -r .actions[3].buildsByBranchName.detached.marked.SHA1')}""".trim();

                    //sh 'git merge --abort'
    				//sh 'git fetch origin/chall-adp-lift'
    				sh 'git checkout master'
    				sh 'git pull'
    				sh 'git checkout chall-dnd26417'
    				sh 'git pull'
    			    sh 'git submodule update --init --recursive'
    				try{
    				    echo "Merging commit from master: ${MASTER}"
    				    sh "git merge master ${MASTER}"
    				} catch(err) {
    				    env.CONFLICT = """${sh( returnStdout: true, script: 'git diff --name-only --diff-filter=U')}""".trim();
    				    sh 'echo ----------'
    				    echo "conflict: ${CONFLICT}"
    				    if("${CONFLICT}" != "esc/helm/eric-sc-umbrella/requirements.yaml") {
    				        sh 'git status'
    				        sh './bob/bob conflict-resolve:other-conflicts-resolve'
    				        sh 'git status'
    				        CONFLICT = "esc/helm/eric-sc-umbrella/requirements.yaml"
    				    } 
    				    if("${CONFLICT}" == "esc/helm/eric-sc-umbrella/requirements.yaml") {
    				        echo 'fixing conflicts in ${CONFLICT}'
        					sh 'git show :2:esc/helm/eric-sc-umbrella/requirements.yaml > requirements.ours' //file from adp-lift
        					sh 'git show :3:esc/helm/eric-sc-umbrella/requirements.yaml > requirements.theirs' //file from master
        				    
                            // sh 'ls -ltrh'
        					sh './bob/bob conflict-resolve:requirements-conflicts-resolve'
        					sh 'cat requirements.yaml'
        					sh 'mv -f requirements.yaml esc/helm/eric-sc-umbrella'
        					// sh 'git merge --abort'
    				    }
    				    sh 'git add -A'
    				    sh 'git commit -m "merge master into adp-lift-master with fixed Conflicts" '
        				sh 'git push'
    				}
				    sh 'git log -n 10 --oneline --decorate --graph --all'
                } //script
            } //steps
        } //stage
        stage('Clone test repository'){
            steps{
           
                echo 'Creating test repo directory'
                dir('5g_test_ci'){
                    deleteDir()
                }

                echo pwd()
                echo 'Trying to clone the 5G prototype test repository'
                checkout scmGit(
                branches: [[name: '*/${TEST_BRANCH}']],
                userRemoteConfigs: [[credentialsId: 'eiffelesc-user-password', name: 'origin', url: 'https://eiffelesc@gerritmirror-direct.sero.gic.ericsson.se/a/MC_5G/5g_test_ci']],
                extensions: [submodule(parentCredentials: true, recursiveSubmodules: true, reference: '', shallow: false), [$class: 'RelativeTargetDirectory', relativeTargetDir: '5g_test_ci']])
                echo 'check content'
                dir('5g_test_ci'){
                    echo pwd()
                    sh 'git log -n 10'
                    sh 'ls -ltrh'
                    sh 'git submodule update --init --recursive'
                    
                }
            }
        }
        stage('Merging latest running commit Master for 5g_test_ci'){
            steps{
                withCredentials([usernamePassword(credentialsId: '4d8761fe-e925-4638-bf28-97744f277be6', usernameVariable: 'USER', passwordVariable: 'PASSWORD')]) {
                    sh 'wget -O- --auth-no-challenge --user=$USER --password=$PASSWORD https://fem1s10-eiffel029.eiffel.gic.ericsson.se:8443/jenkins/job/5G-ESC/job/CI_on_Master/job/5G-SC-CI-master-test-pipeline/lastSuccessfulBuild/api/json > json.file'
                }
                dir('5g_test_ci'){
                    script{
                        TEST_MASTER = """${sh( returnStdout: true, script: 'cat ../json.file | jq -r \'.actions[10].buildsByBranchName.\"refs/remotes/origin/master\".marked.SHA1\' ')}""".trim();
                        sh 'git checkout master'
                        sh 'git pull'
                        sh 'git checkout ${TEST_BRANCH}'
                        sh 'git pull'
                        try{
        				    echo "Merging commit from master: ${TEST_MASTER}"
        				    sh "git merge master ${TEST_MASTER}"
        				} catch(err) {
        				    env.CONFLICT = """${sh( returnStdout: true, script: 'git diff --name-only --diff-filter=U')}""".trim();
        				    sh 'echo ----------'
        				    echo "conflict: ${CONFLICT}"
        				    if("${CONFLICT}" != "") {
        				        sh 'git status'
        				        sh './bob/bob conflict-resolve:other-conflicts-resolve'
        				        sh 'git status'
        				        CONFLICT = "esc/helm/eric-sc-umbrella/requirements.yaml"
        				    }
        				    sh 'git merge --abort'
        				}
                    }
    			}		    
    	    }
        } //stage
        stage('Creating .properties file'){
            steps{
    			// you need to set all the parameters into the *.properties file otherwise they will not be seen by Spinnaker with 
    
    			sh 'echo "KUBE_HOST=$(echo ${KUBE_HOST})" > test.properties'
    			sh 'echo "NAMESPACE=$(echo ${NAMESPACE})" >> test.properties'
    			sh 'echo "BRANCH=$(echo ${BRANCH})" >> test.properties'
    			sh 'echo "RUN_GS=$(echo ${RUN_GS})" >> test.properties'
    
    			sh 'echo "TEST_BRANCH=$(echo ${TEST_BRANCH})" >> test.properties'
    			sh 'echo "PROPERTIES=$(echo ${PROPERTIES})" >> test.properties'
    			sh 'echo "TROUBLESHOOTING=$(echo ${TROUBLESHOOTING})" >> test.properties'
    
    			sh 'echo "CHART_NAME=$(echo ${CHART_NAME})" >> test.properties'
    			sh 'echo "CHART_VERSION=$(echo ${CHART_VERSION})" >> test.properties'
    			sh 'echo "CHART_REPO=$(echo ${CHART_REPO})" >> test.properties'
    
                archiveArtifacts artifacts: 'test.properties', onlyIfSuccessful: true
    			// echo "NAMESPACE=`cat ${env.OUTPUT_DIR}/var.namespace`" >> test.properties;"""
    		} // steps
        } // stage
    } //stages
} //pipeline
