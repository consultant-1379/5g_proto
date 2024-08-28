

def emailbodyStart="Hi "+ "<br><br>Sonarqube static analysis results for branch : ${BRANCH} "
def emailbodyMain="<br><br> Sonarqube analysis results <br> https://sonarqube.lmera.ericsson.se/dashboard?id=com.ericsson%3Aesc-parent"
def emailbodySuccess="<br><br> Sonarqube static analysis was successfull ! <br><br> "
def emailbodyFail="<br><br> Sonarqube static analysis has been failed ! It needs troubleshooting actions ! <br><br>"
def emailbodyEnd ="<br><br>BR,<br>Sonarqube analysis by Jenkins<br><br>"


pipeline {
    agent {
        node {
            label 'esc-docker||esc-docker-1||esc-docker-2'
        }
    }
    environment
    {
        BRANCH = "${env.BRANCH}"
    }
    options{
        timeout(time: 30, unit: 'MINUTES')
        timestamps()
    }

    stages{
        stage('Init Bob'){
            steps{
                echo 'Executing: bob init:set-build-proxy'
                echo pwd()
                sh 'ls -ltrh'
                sh 'git submodule update --init --recursive'
                sh './bob/bob base.init:set-build-proxy'
            }
        }
        stage('Update settings.xml'){
        	steps{
        		sh """
        		echo "Update sonar projectVersion with ${BRANCH}";
        		sed -i "s/projectVersion>latest/projectVersion>${BRANCH}/g" settings.xml"""
        	}
        }
        stage('Scan source code'){
            steps{
                echo 'Running: bob sonar'
                    sh 'ls -ltrh;'
                    sh './bob/bob sonar'
            }
        }
    }
    post{

        success{

            echo "The scan analysis completed successfully"
            
            script{
                emailext body: "${emailbodyStart}"+"${emailbodyMain}"+"${emailbodySuccess}"+"${emailbodyEnd}",
    		    mimeType: 'text/html',
    		    subject: "5G ${BRANCH} Sonarqube static analysis results for 5g_proto repo",
    		    to: "nikos.argyriou@ericsson.com"

            } // script
        } // success

	    failure{

            echo "Something bad happened. Troubleshooting needed."
            script{
                emailext body: "${emailbodyStart}"+"${emailbodyMain}"+"${emailbodyFail}"+"${emailbodyEnd}",
    		    mimeType: 'text/html',
    		    subject: "5G ${BRANCH} Sonarqube static analysis results for 5g_proto repo",
    		    to: "nikos.argyriou@ericsson.com"

            } // script

	    } // failure

    } // post
}

