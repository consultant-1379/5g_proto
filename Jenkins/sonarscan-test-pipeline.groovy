

def emailbodyStart="Hi "+ "<br><br>Sonarqube static analysis results for branch : ${BRANCH} "
def emailbodyMain="<br><br> Sonarqube analysis results <br> https://sonarqube.lmera.ericsson.se/dashboard?id=com.ericsson.jcat.esc%3Ajcat-esc"
def emailbodySuccess="<br><br> Sonarqube static analysis was successfull ! <br><br> "
def emailbodyFail="<br><br> Sonarqube static analysis has been failed ! It needs troubleshooting actions ! <br><br>"
def emailbodyEnd ="<br><br>BR,<br>Sonarqube analysis by Jenkins<br><br>"


pipeline {

    agent { label 'esc-docker||esc-docker-1||esc-docker-2'  } // sc-ipv6 for ipv6

    options{
        timeout(time: 30, unit: 'MINUTES')
        timestamps()
    }

    environment{
        ARM_ARTIFACTORY_TOKEN = "${ARM_ARTIFACTORY_TOKEN}"
        BRANCH = "${env.BRANCH}"
		TEST_BRANCH = "${env.TEST_BRANCH}"
    }

    stages {
		stage('Initialize Bob') {
            steps {
                echo 'Checking user'
                sh 'whoami'
                echo 'check path'
                echo pwd()
                checkout scmGit(
                    branches: [[name: '*/${BRANCH}']],
                    userRemoteConfigs: [[credentialsId: 'eiffelesc-user-password', name: 'origin', url: 'https://eiffelesc@gerritmirror-direct.sero.gic.ericsson.se/a/MC_5G/5g_proto']],
                    extensions: [submodule(parentCredentials: true, recursiveSubmodules: true, reference: '', shallow: false), [$class: 'RelativeTargetDirectory', relativeTargetDir: '5g_proto']])

                dir('5g_proto'){
                    sh 'git remote set-url origin --push https://eiffelesc@gerrit.ericsson.se/a/MC_5G/5g_proto'
                    sh 'git submodule update --init --recursive'
                    sh './bob/bob base.init:set-build-proxy'
                    echo 'Build Common libs'
                    sh './bob/bob build-libs-ci;'
                }
            }
        }
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
                    sh './bob/bob init:set-build-proxy'
                    sh './bob/bob init:version'
                    sh './bob/bob init:sync-libs-dev'
                    sh './bob/bob init:check-hooks'
                }
				
			}
        }
        stage('Update settings.xml'){
        	steps{
                dir('5g_test_ci'){
		    		sh """
		    		echo "Update sonar projectVersion with ${TEST_BRANCH}";
		    		sed -i "s/projectVersion>latest/projectVersion>${TEST_BRANCH}/g" settings.xml"""
                }
        	}
        }
        stage('Trigger sonar scan') {
            steps {
                dir('5g_test_ci'){
                    withCredentials([string(credentialsId: 'fd214478-e2ff-493d-a2f6-144fb9db691b', variable: 'ARM_ARTIFACTORY_TOKEN')]){
                    	sh "./bob/bob build:clean-target "
    				    sh "./bob/bob build:build-maven-project"
    				    sh "./bob/bob sonar"
                    }
                }
            } // steps
        } // stage
    } //stages
    post{

        success{

            echo "The scan analysis completed successfully"
            
            script{

                emailext body: "${emailbodyStart}"+"${emailbodyMain}"+"${emailbodySuccess}"+"${emailbodyEnd}",
    		    mimeType: 'text/html',
    		    subject: "5G ${BRANCH} Sonarqube static analysis results for 5g_test_ci repo",
    		    to: "nikos.argyriou@ericsson.com"

            } // script
        } // success

	    failure{

            echo "Something bad happened. Troubleshooting needed."
            script{

                emailext body: "${emailbodyStart}"+"${emailbodyMain}"+"${emailbodyFail}"+"${emailbodyEnd}",
    		    mimeType: 'text/html',
    		    subject: "5G ${BRANCH} Sonarqube static analysis results for 5g_test_ci repo",
    		    to: "nikos.argyriou@ericsson.com"

            } // script

	    } // failure

    } // post
}
