

def emailbodyStart="Hi "+ "<br><br>Sonarqube static analysis results for branch : ${BRANCH} "
def emailbodyMain="<br><br> Sonarqube analysis results <br> https://sonarqube.lmera.ericsson.se/dashboard?id=sc-aat"
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
		AAT_BRANCH = "${env.AAT_BRANCH}"
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
                    userRemoteConfigs: [[credentialsId: 'eiffelesc-user-password', name: 'origin', url: 'https://eiffelesc@gerritmirror.sero.gic.ericsson.se/a/MC_5G/5g_proto']],
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
        stage('Clone AAT repository'){
            steps{
                echo 'Creating AAT repo directory'
                dir('5G_AAT'){
                    deleteDir()
                }
                echo pwd()
                echo 'Trying to clone the 5G prototype AAT repository'
                checkout scmGit(
                    branches: [[name: '*/${AAT_BRANCH}']],
                    userRemoteConfigs: [[credentialsId: 'eiffelesc-user-password', name: 'origin', url: 'https://eiffelesc@gerrit.ericsson.se/a/MC_5G/5G_AAT']],
                    extensions: [submodule(parentCredentials: true, recursiveSubmodules: true, reference: '', shallow: false), [$class: 'RelativeTargetDirectory', relativeTargetDir: '5G_AAT']])
                echo 'check content'
                dir('5G_AAT'){
                    echo pwd()
                    sh 'git log -n 10'
                    sh 'ls -ltrh'
                    sh 'git submodule update --init --recursive'
                    sh './bob/bob -r ruleset2.0-eric-aat.yaml init:set-build-proxy'
                    sh './bob/bob -r ruleset2.0-eric-aat.yaml init:version'
                }
				
			}
        }
        stage('Update settings.xml'){
        	steps{
                dir('5G_AAT'){
		    		sh """
		    		echo "Update sonar projectVersion with ${AAT_BRANCH}";
		    		sed -i "s/projectVersion>latest/projectVersion>${AAT_BRANCH}/g" settings.xml"""
                }
        	}
        }
        stage('Trigger sonar scan') {
            steps {
                dir('5G_AAT'){
                    withCredentials([string(credentialsId: 'c9bf8e3d-476c-4be9-b26a-73d0275975bd', variable: 'ARM_ARTIFACTORY_TOKEN')]){
                    	sh "./bob/bob -r ruleset2.0-eric-aat.yaml clean"
                        sh "./bob/bob -r ruleset2.0-eric-aat.yaml init:set-build-proxy init:sync-libs-dev"
    				    sh "./bob/bob -r ruleset2.0-eric-aat.yaml sonar"
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
    		    subject: "5G ${BRANCH} Sonarqube static analysis results for AAT",
    		    to: "c91010d0.ericsson.onmicrosoft.com@emea.teams.ms"

            } // script
        } // success

	    failure{

            echo "Something bad happened. Troubleshooting needed."
            script{

                emailext body: "${emailbodyStart}"+"${emailbodyMain}"+"${emailbodyFail}"+"${emailbodyEnd}",
    		    mimeType: 'text/html',
    		    subject: "5G ${BRANCH} Sonarqube static analysis results for AAT",
    		    to: "c91010d0.ericsson.onmicrosoft.com@emea.teams.ms"

            } // script

	    } // failure

    } // post
}
