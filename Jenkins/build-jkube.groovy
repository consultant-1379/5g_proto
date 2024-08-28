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
        VERSION = "${env.VERSION}"
        KUBE_HOST = "${env.KUBE_HOST}"
        ARTIFACTORY_TOKEN = "${env.ARTIFACTORY_TOKEN}"
        BRANCH = "${env.BRANCH}"
        TEST_BRANCH = "${env.TEST_BRANCH}"
        ERIC_CHFSIM_REDIS_TGZ=""  //eedmti
        ERIC_JKUBE_TGZ="" // eedmti

    }
    stages{
        stage('Freeing up workspace on other buildslaves'){
             steps{

             cleanWs()

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

        stage('Clone Development repository'){
            steps{
                echo 'Creating Dev repo directory'
                sh """if [ -d 5g_proto ];
                      then
                          rm -rf 5g_proto/*;
                      else
                          mkdir -p 5g_proto/;
                      fi;"""

                echo pwd()
                echo 'Trying to clone the 5G prototype dev repository'
                checkout scmGit(
                          branches: [[name: 'origin/${BRANCH}']],
                          userRemoteConfigs: [[credentials: 'eiffel-user-password', name: 'origin', url: 'https://eiffelesc@gerrit.ericsson.se/a/MC_5G/5g_proto']],
                          extensions: [submodule(parentCredentials: true, recursiveSubmodules: true, reference: '', shallow: false), [$class: 'RelativeTargetDirectory', relativeTargetDir: '5g_proto']])
                dir('5g_proto'){
                    echo pwd()
                    echo 'check content'
                    sh 'ls -ltrh'
                    sh 'git submodule update --init --recursive'
                    script{
                        SHORT_HASH = """${sh( returnStdout: true,
                                script: "git log --format=\"%h\" | head -1"
                        )}""".trim()
                    }
                    script {
//                        JKUBE_VERSION = "0.0.${VERSION}-${SHORT_HASH}"
                        JKUBE_VERSION = "${PROJECT}-${VERSION}-${SHORT_HASH}"
                    }
                    echo "The JKUBE version will be set to ${JKUBE_VERSION}"
                    echo "Updating the POM files with the proper JKUBE version"

                    echo "Initiating via BOB"
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

                    echo "Building the libs"
                    sh './bob/bob build-libs-ci'

		            echo "Building the redis helm and image"
		            sh "if [ -e ./.bob/eric-chfsim-redis*.tgz ]; then rm -f ./.bob/eric-chfsim-redis*.tgz; fi;"
		            sh "./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml init image:build-eric-chfsim-redis package:helm-chart-redis-jkube push:docker-image-redis push:helm-chart-redis"
                }
            }
        }
        stage('Clone test repository'){
            steps{
                echo 'checking path to verify that execution will continue at root level'
                echo pwd()
                echo 'Creating test repo directory'
                sh """if [ -d 5g_test_ci ];
                      then
                          rm -rf 5g_test_ci/*;
                      else
                          mkdir -p 5g_test_ci/;
                      fi;"""

                echo 'Trying to clone the 5G prototype test repository'

                checkout scmGit(
                    branches: [[name: 'origin/${TEST_BRANCH}']],
                    userRemoteConfigs: [[credentialsId: 'eiffelesc-user-password', name: 'origin', url: 'https://eiffelesc@gerrit.ericsson.se/a/MC_5G/5g_test_ci']],
                    extensions: [submodule(parentCredentials: true, recursiveSubmodules: true, reference: '', shallow: false), [$class: 'RelativeTargetDirectory', relativeTargetDir: '5g_test_ci']])

                dir('5g_test_ci'){
                    echo 'check content'
                    echo pwd()
                    sh 'ls -ltrh'
                    sh 'git submodule update --init --recursive'
                    sh 'ln -s 5g_proto/.m2 ../.m2'
                    sh """./bob/bob init"""
                    echo "Checking if the libraries are updated in /home/$USER/.m2/repository/com/ericsson"
                    sh "ls -lR /home/$USER/.m2/repository/com/ericsson"
                    echo "Adding the JKUBE version to var.version in .bob folder"
                    sh """echo ${JKUBE_VERSION} > .bob/var.version"""
                    echo "Checking if the update of var.version was successful!"
                    sh "cat .bob/var.version"
                    echo "clean target directory"
                    sh "./bob/bob install:clean-target"
                    echo "Updating the proxy-testcases POM file with the right JKUBE version"
                    sh "sed -i \"s@<finalName>eric-jkube-0.0.2</finalName>@<finalName>eric-jkube-ts-${JKUBE_VERSION}</finalName>@g\" ./jcat-esc/proxy-testcases/pom.xml"
                    echo "Checking if the update of the POM was successful!"
                    sh "git diff -- ./jcat-esc/proxy-testcases/pom.xml"

                }
            }
        }
        stage('Building the JKUBE package'){
            steps {
                dir('5g_test_ci') {
                    sh """./bob/bob install:package-maven-project;
                      ls -ltrh ./jcat-esc/proxy-testcases/target/
                      ./bob/bob install:copyPropertiesFile;
                      ./bob/bob install:copyResources;
                      ./bob/bob install:build-eric-jkube;
                      ./bob/bob install:helm-chart;"""
                    withCredentials([string(credentialsId: 'c9bf8e3d-476c-4be9-b26a-73d0275975bd', variable: 'ARTIFACTORY_TOKEN')]) {
                        sh "./bob/bob install:upload-helm-chart;"
                    }
                      sh """./bob/bob install:push-docker-image;"""
                }
            }

        }
        stage('Pulling and saving the docker image'){
            steps {
                dir('5g_test_ci') {
                    echo "Pulling the docker image"
                    sh """sudo docker pull armdocker.rnd.ericsson.se/proj-5g-bsf/eiffelesc/eric-jkube:${JKUBE_VERSION}"""
                    echo "Creating the saving directory for the image"
                    sh """if [ -d .bob/img ];
                          then
                              rm -rf .bob/img/*;
                          else
                              mkdir -p .bob/img/;
                          fi;"""
                echo "Saving the docker image"
                sh """docker save armdocker.rnd.ericsson.se/proj-5g-bsf/eiffelesc/eric-jkube:${JKUBE_VERSION} -o .bob/img/eric-jkube-${JKUBE_VERSION}.tar"""
                }
            }
        }
        stage('archiving the JKUBE image and the Helm chart'){
            steps {
                echo "The current path is " + pwd()
                archiveArtifacts allowEmptyArchive: true, artifacts: '5g_test_ci/.bob/img/*.tar, 5g_test_ci/.bob/*.tgz, 5g_test_ci/jcat-esc/proxy-testcases/target/*.jar, ./5g_proto/.bob/eric-chfsim-redis*.tgz', onlyIfSuccessful: true
            }
        }


//        stage (' Start Spinnaker-trigger job'){
//            environment {
//
//	        ERIC_CHFSIM_REDIS_TGZ = """${sh (returnStdout: true, script:'ls ./5g_proto/.bob/ | grep -i chfsim-redis-')}"""
//	        ERIC_JKUBE_TGZ = """${sh (returnStdout: true, script:'ls ./5g_test_ci/.bob/ | grep -i jkube-')}"""
//            NEXTJOB = "2-Spinnaker-trigger-JKube"
//	    }
//            steps {
//
//                echo pwd()
//                build job: "${env.NEXTJOB}",
//                      parameters: [string(name: 'ERIC_CHFSIM_REDIS_TGZ', value: "${env.ERIC_CHFSIM_REDIS_TGZ}"),
//				                   string(name: 'ERIC_JKUBE_TGZ', value: "${env.ERIC_JKUBE_TGZ}"),
//                                   string(name: 'BRANCH', value: "${env.BRANCH}"),
//                                   string(name: 'KUBE_HOST', value: "${env.KUBE_HOST}"),
//                                   string(name: 'NAMESPACE', value: "${env.NAMESPACE}")],
//                      wait: false
//	          }
//        }

        stage("Archiving parameters for downstream pipelines"){

            environment {

                ERIC_CHFSIM_REDIS_TGZ = """${sh (returnStdout: true, script:'ls ./5g_proto/.bob/ | grep -i chfsim-redis-')}"""
                ERIC_JKUBE_TGZ = """${sh (returnStdout: true, script:'ls ./5g_test_ci/.bob/ | grep -i jkube-')}"""
            }
            steps {

                sh 'echo "KUBE_HOST=${KUBE_HOST}" > jkube.properties'
                sh 'echo "NAMESPACE=${NAMESPACE}" >> jkube.properties'
                sh 'echo "VERSION=${VERSION}" >> jkube.properties'
                sh 'echo "BRANCH=${BRANCH}" >> jkube.properties'
                sh 'echo "TEST_BRANCH=${TEST_BRANCH}" >> jkube.properties'
                sh 'echo "ERIC_CHFSIM_REDIS_TGZ=${ERIC_CHFSIM_REDIS_TGZ}" >> jkube.properties'
                sh 'echo "ERIC_JKUBE_TGZ=${ERIC_JKUBE_TGZ}" >> jkube.properties'

                archiveArtifacts artifacts: 'jkube.properties', onlyIfSuccessful: true
            } // steps
        } //stage

        stage('Post processing instruction'){
            steps {


                echo "############################"
                echo "STEPS TO BE PERFORMED MANUALLY "
                echo "############################"

                echo "Fetch the artifacts on home page of Jenkins job Build Eric-Jkube"
                echo "Send the file to person requiring the JKUBE by any means attachment folder, mail, skype, ftp ..."
            }

        }

    }
}

