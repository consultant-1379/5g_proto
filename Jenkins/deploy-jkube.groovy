def COMMITTER
def SHORT_HASH
def JKUBE_VERSION


pipeline {
    agent {
        node {
            label '5G-SC'
        }
    }
    options{
        timeout(time: 30, unit: 'MINUTES')
        timestamps()
    }
    environment{
        //VERSION = "${env.VERSION}"
        KUBE_HOST = "${env.KUBE_HOST}"
        ARTIFACTORY_TOKEN = "${env.ARTIFACTORY_TOKEN}"
        //BRANCH = "${env.BRANCH}"
        //TEST_BRANCH = "${env.TEST_BRANCH}"
        NAMESPACE = "${env.NAMESPACE}"
		
    }
    stages{
	stage('Deploy chfsim-redis'){
            steps {
                echo "############################"
                echo " deploy chfsim-redis"
                echo "############################"
                echo "The current path is " + pwd()
                     script {
                          withCredentials([string(credentialsId: 'c9bf8e3d-476c-4be9-b26a-73d0275975bd', variable: 'ARTIFACTORY_TOKEN')]) {
                          // sh 'curl -O "https://arm.rnd.ki.sw.ericsson.se/artifactory/proj-bsf-test-generic-local/${ERIC_CHFSIM_REDIS_TGZ}"'
                           sh 'curl -f -k -H "X-JFrog-Art-Api:AKCp5btevebPP7ih6pPGjc2cpUsXDMfjaTXGLSibiFgRCCsF6FEwVVrZbPfY4U1raSYUNsDTU" -O https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-helm-local/${ERIC_CHFSIM_REDIS_TGZ}'                
                           }
	                   echo "eric-chfsim-redis: ${ERIC_CHFSIM_REDIS_TGZ}"
                           sh """helm install --kubeconfig /home/eiffelesc/.kube/${KUBE_HOST}.config --namespace ${env.NAMESPACE} eric-chfsim-redis-${env.NAMESPACE} ${ERIC_CHFSIM_REDIS_TGZ}"""
	             }	
	     }	
	}	
	
	stage('Deploy jkube'){
            steps {
                echo "############################"
                echo " deploy jkube"
                echo "############################"
                echo "The current path is " + pwd()
                // /home/jenkins/workspace/5G-ESC/JKube/Build-Eric-Jkube/5g_test_ci/.bob
		script{
		      withCredentials([string(credentialsId: 'c9bf8e3d-476c-4be9-b26a-73d0275975bd', variable: 'ARTIFACTORY_TOKEN')]) {
                          //sh 'curl -O "https://arm.rnd.ki.sw.ericsson.se/artifactory/proj-bsf-test-generic-local/${ERIC_JKUBE_TGZ}"'
                           sh 'curl -f -k -H "X-JFrog-Art-Api:AKCp5btevebPP7ih6pPGjc2cpUsXDMfjaTXGLSibiFgRCCsF6FEwVVrZbPfY4U1raSYUNsDTU" -O https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-helm-local/${ERIC_JKUBE_TGZ}'
                       }
                       echo "eric-jkube: ${ERIC_JKUBE_TGZ}"  
                       sh """helm install --kubeconfig /home/eiffelesc/.kube/${KUBE_HOST}.config --namespace ${env.NAMESPACE} eric-jkube-${env.NAMESPACE} $ERIC_JKUBE_TGZ"""
                }  
             }
         }		
    }
}
