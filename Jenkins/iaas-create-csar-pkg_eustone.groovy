// Check which BS to use

def AGENT_LABEL = null

node('DSC_BuildSlave_GIC_1||DSC_BuildSlave_GIC_2||DSC_BuildSlave_GIC_3||DSC_BuildSlave_GIC_4||DSC_BuildSlave_GIC_5')
{
    stage('set agent')
    {
        if (BS_LABEL == 'null')
        {
            AGENT_LABEL = '5G-SC' // the user didn't select a specific bs
        }
        else
        {
            AGENT_LABEL = "${BS_LABEL}"      // take the bs selected by the user
        }
    }
}

def emailbody_start="Hi,"
def emailbody_end="\n\nBR,\nYour humble Jenkins"

pipeline
{
    agent
    {
        node
        {
            label "${AGENT_LABEL}"
/*
            label '5G-SC'
            label '5G-LMVL-1'
            label 'esc-docker || esc-docker-1 || esc-docker-2'
*/
        }
    }
    options
    {
        timeout(time: 90, unit: 'MINUTES')
        timestamps()
    }
    environment
    {
// variables needed for the ruleset
        OUTPUT_DIR=".bob"
        KUBE_HOST = "hahn138" /* iaas: hard coded to hahn138. That cluster is not needed any more, but just to pass the bob-init rule. */
        NAMESPACE = "${NAMESPACE}" /* iaas: will be null; only needed to pass the bob-init rule and not to search for a free namespace. */
        RELEASE = true  /* iaas: hard coded to yes. We want to have a legible version number on the generated csar pkg. */
        PACKAGING = false
        CSAR_CHARTS = true
        CSAR_DETAIL = false
// variables from initial form
        ARTIFACTORY_TOKEN = "Token for armdocker" /* iaas: not shown in the initial form as always gets this value */
        BRANCH = "${BRANCH}"
        REPO = "proj-5g-bsf-generic-local"
        REPO_PATH = "https://arm.seli.gic.ericsson.se/artifactory/${REPO}/eiffelesc"
        MAIL = "${MAIL}"
        BS_LABEL ="${BS_LABEL}"
        ADD_MSG = "If you want to create a package at this stage, perform an empty push on the branch so to step the version and trigger a new csar package creation."

        // Variables needed for the ruleset (DND-30125)
        BSF = true
        SCP = true
        SEPP = true

        // Preparation for moving to another Jenkins instance
        JENKINS_INSTANCE = "https://fem1s10-eiffel029.eiffel.gic.ericsson.se:8443/jenkins/job/5G-ESC/job/CSAR/view/All/job"
        JENKINS_INSTANCE_NEW = "https://fem1s30-eiffel029.eiffel.gic.ericsson.se:8443/jenkins/job/5G-ESC/job/CSAR/view/All/job"

        // Variables for the Supreme tool
        WANTED_SUPREME_VERSION = "1.0.8"
        CSAR_PACKAGE_VERSION = "1.14.25+9997"
    }
    stages
    {
/*
        stage('Prune Images')
        {
            steps
            {
                echo 'Removing not used images'
                sh 'docker image prune -a -f'
            }
        }
*/
        stage('Additional info')
        {
            steps
            {
                script
                {
                    LOGS_LINK = """${sh( returnStdout: true,
                    script: "echo ${JENKINS_INSTANCE}/${JOB_BASE_NAME}")}""".trim()

                }
                script
                {
                    FREE_DISK_SPACE = """${sh( returnStdout: true,
                    script: "df -BG | grep -e /home/jenkins/workspace -e /local |awk '{print \$4}' | cut -d 'G' -f1")}""".trim()
                    if ("${FREE_DISK_SPACE}".toInteger() < 35)
                    {
                        if ("${env.MAIL}" != 'null')
                        {
                            def emailbody_disk_space_failure_1="\nthere are currently ${FREE_DISK_SPACE}GB free on $NODE_NAME. This is not enough for creating"
                            def emailbody_disk_space_failure_2=" a csar package as during the process at least 35GB are needed. Please make more disk space available."
                            def emailbody_disk_space_failure_3="\nAfterwards you might rebuild this job."

                            emailext body: "${emailbody_start}"+"${emailbody_disk_space_failure_1}"+"${emailbody_disk_space_failure_2}"+
                            "${emailbody_disk_space_failure_3}"+"${emailbody_end}",
                            subject: 'Not enough disk space for creating a csar package',
                            to: "${env.MAIL};issa.diomansy.koite@ericsson.com; claus.ortmann@ericsson.com"
                        }

                        echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! "
                        echo "!!!   There are currently ${FREE_DISK_SPACE}GB disk space free on $NODE_NAME. This is not enough for the csar package creation.    !!!"
                        echo "!!!   Please make more disk space available.     !!!"
                        echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! "
                        sh """exit 1"""
                    }
                }
                echo "There are currently ${FREE_DISK_SPACE}GB disk space free on $NODE_NAME. This should be enough for the csar package creation."

//              Check space for images
                script
                {
                    FREE_IMAGES_SPACE = """${sh( returnStdout: true,
                    script: "df -BG | grep /dev/mapper |awk '{print \$4}' | cut -d 'G' -f1")}""".trim()
                    if ("${FREE_IMAGES_SPACE}".toInteger() < 15)
                    {
                        if ("${env.MAIL}" != 'null')
                        {
                            def emailbody_images_space_failure_1="\nthere are currently ${FREE_IMAGES_SPACE}GB free for loading images on $NODE_NAME."
                            def emailbody_images_space_failure_2=" This is not enough space for the csar package creation as during the process up to"
                            def emailbody_images_space_failure_3=" 15GB are needed for images storage. Please proceed to prune images which are not needed."
                            def emailbody_images_space_failure_4="\nAfterwards you might rebuild this job."

                            emailext body: "${emailbody_start}"+"${emailbody_images_space_failure_1}"+"${emailbody_images_space_failure_2}"+
                            "${emailbody_images_space_failure_3}"+"${emailbody_images_space_failure_4}"+"${emailbody_end}",
                            subject: 'Not enough image storage space for creating a csar package',
                            to: "${env.MAIL};issa.diomansy.koite@ericsson.com; claus.ortmann@ericsson.com"
                        }

                        echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! "
                        echo "!!! There are currently ${FREE_IMAGES_SPACE}GB free for loading images on $NODE_NAME. !!! "
                        echo "!!! This is not enough space for the csar package creation. Please proceed to prune images which are not needed.  !!! "
                        echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! "
                        sh """exit 1"""
                    }  // if
                } // script
                echo "There are currently ${FREE_IMAGES_SPACE}GB free on $NODE_NAME for loading images. This should be enough for the csar package creation."
            }
        }
        stage('Init Bob')
        {
            steps
            {
                echo 'Executing: bob init'
                echo pwd()
                sh 'ls -ltrh'
                sh 'git submodule update --init --recursive'
				sh './bob/bob base.init:create-output-dir'
				sh './bob/bob base.init:copy-docker-config'
				sh './bob/bob base.init:copy-helm-credentials'
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
//              sh './bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml init:chfsim-version'
//              sh './bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml init:redis-version'
//              sh './bob/bob -r rulesets/ruleset2.0-eric-seppsim.yaml init:version'
//              sh './bob/bob -r rulesets/ruleset2.0-eric-nrfsim.yaml init:version'
//              sh './bob/bob -r rulesets/ruleset2.0-eric-dscload.yaml init:dscload-version'
//              sh './bob/bob -r rulesets/ruleset2.0-eric-dscload.yaml init:set-artifactory-token'
//              sh './bob/bob -r rulesets/ruleset2.0-eric-k6.yaml init:influxdb-version'
//              sh './bob/bob -r rulesets/ruleset2.0-eric-k6.yaml init:k6-version'
//              sh './bob/bob -r rulesets/ruleset2.0-eric-sftp.yaml init:sftp-version'
            }
        }
//      stage('Generate released versions')
//      {
//          steps
//          {
//              echo 'Running: bob release:generate-released-version'
//              sh 'ls -ltrh;'
//              sh './bob/bob release:generate-released-version'
//              script
//              {
//                  CSAR_PACKAGE_VERSION = """${sh( returnStdout: true, script: "cat ./${OUTPUT_DIR}/var.esc-version")}""".trim()
//  				// set displayed description to "build slave, branch, csar pkg version"
//  				currentBuild.description = "${env.NODE_NAME}, ${BRANCH}, ${CSAR_PACKAGE_VERSION}"
//              }
//              // Check that there is no other pkg in artifactory with the same version
//               script
//              {
//                  withCredentials([string(credentialsId: 'armseligic-armdocker-eiffelesc-identity-token-for-Smoke-CI', variable: 'ARTIFACTORY_TOKEN')])
//                  {
//                      sh """ curl -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" \
//                      https://armdocker.rnd.ericsson.se/artifactory/api/search/aql \
//                      -d 'items.find({"repo":{"\$eq":"'"${REPO}"'"}},{"type":{"\$eq":"folder"}},{"name":{"\$match":"'"${CSAR_PACKAGE_VERSION}"'"}})' \
//                      -H "content-type:text/plain" > ./PKG_SEARCH.txt"""
//                  }
//                  PKG_EXISTS = """${sh( returnStdout: true, script: "cat ./PKG_SEARCH.txt | grep ${CSAR_PACKAGE_VERSION} 2>/dev/null || true")}""".trim()

//                  if ( "${PKG_EXISTS}".contains("${CSAR_PACKAGE_VERSION}"))
//                  {
//                      PKG_PATH = REPO + "/" + """${sh( returnStdout: true, script: "cat ./PKG_SEARCH.txt | grep path | cut -d '\"' -f4 ")}""".trim()
//                      ADD_MAIL_MSG =". Package version already exists"
//                      error ("This package would get \"${CSAR_PACKAGE_VERSION}\" as version. There exists already a csar package with the same version stored at \"https://arm.seli.gic.ericsson.se/artifactory/${PKG_PATH}/${CSAR_PACKAGE_VERSION}\".\n${ADD_MSG}")
//                  }
//                  else
//                  {
//                      echo 'No csar package found in artifactory with that version; package creation continues.'
//                      ADD_MAIL_MSG =""
//                  }
//              }
//          }
//      }
//      stage('Build source code')
//      {
//          steps
//          {
//              echo 'Running: bob build'
//              sh 'ls -ltrh;'
//              sh './bob/bob build'
//          }
//      }
//      stage('Generate JSON Schemas and Yang Archives for BSF Manager'){
//          environment {
//              COMPONENT = 'BSF'
//          }
//          steps{
//              script {
//                  env.COMPONENT = 'BSF'
//               }
//              echo 'Executing: Generating JSON Schemas and Yang Archives for BSF Manager'
//                 sh """
//                 ./bob/bob -r rulesets/ruleset2.0-yang.yaml init;
//                 ./bob/bob -r rulesets/ruleset2.0-yang.yaml generate;
//                 ./bob/bob -r rulesets/ruleset2.0-yang.yaml copy-output;
//                 """
//          }
//      }
//      stage('Generate JSON Schemas and Yang Archives for SEPP Manager'){
//          environment {
//              COMPONENT = 'SEPP'
//          }
//          steps{
//              script {
//                  env.COMPONENT = 'SEPP'
//               }
//              echo 'Executing: Generating JSON Schemas and Yang Archives for SEPP Manager'
//                 sh """
//                 ./bob/bob -r rulesets/ruleset2.0-yang.yaml init;
//                 ./bob/bob -r rulesets/ruleset2.0-yang.yaml generate;
//                 ./bob/bob -r rulesets/ruleset2.0-yang.yaml copy-output;
//                 """
//          }
//      }
//      stage('Build docker image')
//      {
//          steps
//          {
//              echo 'Executing: bob build-images'
//              sh 'ls -ltrh;'
//              sh './bob/bob build-images'
//          }
//      }
//      stage('Test Helm Chart Design Rules')
//      {
//          steps
//          {
//              echo 'Executing: bob test -rule-dry-release --namespace adp-staging-designrule-check'
//          }
//      }
//      stage('Push docker images')
//      {
//          steps
//          {
//              echo 'Executing: bob push-images'
//              sh "./bob/bob push-images"
//          }
//      }
//      stage('Update Helm Chart Values')
//      {
//          steps
//          {
//              echo 'Executing: bob update-helm'
//              sh 'ls -ltrh; ./bob/bob update-helm'
//          }
//      }
//       stage('Generate JSON Schemas and Yang Archives for SCP Manager'){
//          environment {
//              COMPONENT = 'SCP'
//          }
//          steps{
//              script {
//                  env.COMPONENT = 'SCP'
//               }
//              echo 'Executing: Generating JSON Schemas and Yang Archives for SCP Manager'
//                 sh """
//                 ./bob/bob -r rulesets/ruleset2.0-yang.yaml init;
//                 ./bob/bob -r rulesets/ruleset2.0-yang.yaml generate;
//                 ./bob/bob -r rulesets/ruleset2.0-yang.yaml copy-output;
//                 """
//          }
//      }
//      stage('Generate JSON Schema for VTAP'){
//          environment {
//              COMPONENT = 'PVTB'
//          }
//          steps{
//              script {
//                  env.COMPONENT = 'PVTB'
//               }
//              echo 'Executing: Generating JSON Schema for VTAP'
//                 sh """
//                 ./bob/bob -r rulesets/ruleset2.0-yang.yaml init;
//                 ./bob/bob -r rulesets/ruleset2.0-yang.yaml generate;
//                 ./bob/bob -r rulesets/ruleset2.0-yang.yaml copy-output;
//                 """
//          }
//      }
//      stage('Lint Markdown and Helm')
//      {
//          steps
//          {
//              echo 'Executing: bob lint'
//              sh 'ls -ltrh; ./bob/bob lint'
//          }
//      }
//      stage('Create Helm Package')
//      {
//          steps
//          {
//              echo 'Executing: bob package-helm'
//              sh 'ls -ltrh; ./bob/bob package-helm'
//          }
//      }
//      stage(' Push Helm chart')
//      {
//          steps
//          {
//              echo 'Executing: bob push-helm'
//              sh "./bob/bob push-helm"
//          }
//      }
//      stage('Create umbrella package')
//      {
//          steps
//          {
//              echo 'Executing: bob update-umbrella'
//              sh "./bob/bob update-umbrella"
//          }
//      }
        stage('Simulators')
        {
            steps
            {
                catchError(buildResult: 'FAILURE', stageResult: 'FAILURE')
                {
//                  echo 'Creating CHFsim'
//                  sh """
//                  ./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml build;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml image:build-eric-chfsim;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml package:copy-helm-chart-chfsim;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml package:helm-chart-chfsim;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml push:docker-image-chfsim;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml push:helm-chart-chfsim;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml image:build-eric-chfsim-redis;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml package:helm-chart-redis;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml push:docker-image-redis;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml push:helm-chart-redis;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml export:folders;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml export:helm;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-chfsim.yaml export:image;"""

//                  echo 'Creating SEPPsim'
//                  sh """
//                  ./bob/bob -r rulesets/ruleset2.0-eric-seppsim.yaml build;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-seppsim.yaml image;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-seppsim.yaml package-full;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-seppsim.yaml push:docker-image;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-seppsim.yaml push:helm-chart;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-seppsim.yaml export:folders;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-seppsim.yaml export:helm;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-seppsim.yaml export:image;"""

//                  echo 'Creating NRFsim'
//                  sh """
//                  ./bob/bob -r rulesets/ruleset2.0-eric-nrfsim.yaml build;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-nrfsim.yaml image;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-nrfsim.yaml package;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-nrfsim.yaml push:docker-image;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-nrfsim.yaml push:helm-chart;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-nrfsim.yaml export:folders;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-nrfsim.yaml export:helm;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-nrfsim.yaml export:image;"""

//                  echo 'Creating DSCLOAD'
//                  sh """
//                  ./bob/bob -r rulesets/ruleset2.0-eric-dscload.yaml image:build-dscload;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-dscload.yaml package:helm-chart-dscload;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-dscload.yaml push:docker-image-dscload;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-dscload.yaml push:helm-chart-dscload;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-dscload.yaml export:folders;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-dscload.yaml export:helm;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-dscload.yaml export:image;"""

//                  echo 'Creating K6 and INFLUXDB'
//                  sh """
//                  ./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml image:build-k6;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml package:helm-chart-k6;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml push:docker-image-k6;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml push:helm-chart-k6;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml image:build-influxdb;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml package:helm-chart-influxdb;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml push:docker-image-influxdb;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml push:helm-chart-influxdb;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml export:folders;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml export:helm;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-k6.yaml export:image;"""

//                  echo 'Creating SFTP'
//                  sh """
//                  ./bob/bob -r rulesets/ruleset2.0-eric-sftp.yaml image:build-sftp;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-sftp.yaml package:helm-chart-sftp;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-sftp.yaml push:docker-image-sftp;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-sftp.yaml push:helm-chart-sftp;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-sftp.yaml export:folders;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-sftp.yaml export:helm;
//                  ./bob/bob -r rulesets/ruleset2.0-eric-sftp.yaml export:image;"""
                }
            }
        }
//      stage('Create repository index.yaml file ')
//      {
//          steps
//          {
//              echo 'Executing: bob repository-index'
//              sh "./bob/bob repository-index"
//          }
//      }
//      stage('Create baseline package')
//      {
//          steps
//          {
//              sh "./bob/bob package-umbrella"
//          }
//      }
//      stage('Check versions')
//      {
//          steps
//          {
//              sh "./bob/bob check-charts-versions"
//          }
//      }
//      stage('Upload baseline package')
//      {
//          steps
//          {
//              echo 'Executing: bob push-umbrella'
//              sh "./bob/bob push-umbrella"
//          }
//      }
//      stage('Update CRDs package')
//      {
//          steps
//          {
//              sh "./bob/bob fetch-crds"
//          }
//      }
//      stage('Parallel Stages')
//      {
//          failFast true
//          parallel
//          {
//              stage('Monitor disk space')
//              {
//                  steps
//                  {
//                      timeout(time: 30, unit: 'MINUTES') /* if after 30 mins the csar pkg hasn't been created the process will be aborted */
//                      {
//                          script
//                          {
//                              def disk_space_full = false
//                              echo "Wait 3 mins to start the scanning for the disk space usage"
//                              sleep (180)

//                              while ( !disk_space_full )
//                              {
//                                  if (fileExists("./${OUTPUT_DIR}/eric-sc-${CSAR_PACKAGE_VERSION}.csar.md5"))
//                                  {
//                                      echo "The csar pkg has been created. No need for further scanning"
//                                      disk_space_full = true
//                                  }
//                                  else
//                                  {
//                                      USED_DISK_SPACE = """${sh( returnStdout: true,
//                                      script: "df -h | grep -e /home/jenkins/workspace -e /local |awk '{print \$5}' | cut -d 'G' -f1 | cut -d '%' -f1")}""".trim()

//                                      if ("${USED_DISK_SPACE}".toInteger() > 97) // stop the process if the workspace is occupied more than 97%
//                                      {
//                                          echo "The disk space used on $NODE_NAME is ${USED_DISK_SPACE}%. The process for creating a new csar package will stop to avoid 100% disk usage."
//                                          if ("${env.MAIL}" != 'null')
//                                          {
//                                              def emailbody_job_disk_full_1="\nthe disk space used on $NODE_NAME is ${USED_DISK_SPACE}%. The process "
//                                              def emailbody_job_disk_full_2="for creating a new csar package will stop to avoid 100% disk usage."

//                                              emailext body: "${emailbody_start}"+"${emailbody_job_disk_full_1}"+"${emailbody_job_disk_full_2}"+
//                                              "${emailbody_end}", subject: "Automatic csar package creation stopped for branch ${env.BRANCH}", to: "${env.MAIL}"
//                                          }
//                                          sh """exit 1"""
//                                      }
//                                      else
//                                      {
//                                          echo " Current disk space usage: ${USED_DISK_SPACE}%. New scan in 30 sec "
//                                          sleep (30)
//                                      }
//                                  }
//                              }
//                          }
//                      }
//                  }
//              }
//              stage('Create & upload csar package')
//              {
//                  steps
//                  {

//                      echo 'Executing: bob csar'
//                      sh "./bob/bob csar"

//                  }

//              }
//          }
//      }
//      stage('Upload config files to artifactory')
//      {
//          steps
//          {
//              script
//              {
//                  ARTIFACTORY_PATH = "https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/eiffelesc/${env.BRANCH}/${CSAR_PACKAGE_VERSION}/sc-config-sample/"
//                  BSF_INIT = ARTIFACTORY_PATH + "action_bsf_init_db,user=bsf-admin.netconf"
//                  BSF_CONFIG = ARTIFACTORY_PATH + "sample_bsf_func_and_diameter,user=bsf-admin.netconf"
//                  CSA_CONFIG = ARTIFACTORY_PATH + "sample_config_nrf_csa,user=csa-admin.netconf"
//                  PVTB_CONFIG = ARTIFACTORY_PATH + "sample_broker_config,user=expert.netconf"
//                  SCP_CONFIG = ARTIFACTORY_PATH + "sample_config_scp,user=scp-admin.netconf"
//                  SEPP_CONFIG = ARTIFACTORY_PATH + "sample_sepp_poc_config,user=sepp-admin.netconf"
//                  CONFIGURATION_PROVISIONING_DIR = "./Jenkins/PipeConfig"
//              }
//              withCredentials([string(credentialsId: 'armseligic-armdocker-eiffelesc-identity-token-for-Smoke-CI', variable: 'ARTIFACTORY_TOKEN')])
//              {
//                  sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" \\
//                  --upload-file ${CONFIGURATION_PROVISIONING_DIR}/action_bsf_init_db.netconf ${BSF_INIT}"""
//                  sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" \\
//                  --upload-file ${CONFIGURATION_PROVISIONING_DIR}/sample_bsf_func_and_diameter.netconf ${BSF_CONFIG}"""
//                  sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" \\
//                  --upload-file ${CONFIGURATION_PROVISIONING_DIR}/sample_config_nrf_csa.netconf ${CSA_CONFIG}"""
//                  sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" \\
//                  --upload-file ${CONFIGURATION_PROVISIONING_DIR}/sample_broker_config.netconf ${PVTB_CONFIG}"""
//                  sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" \\
//                  --upload-file ${CONFIGURATION_PROVISIONING_DIR}/sample_config_scp.netconf ${SCP_CONFIG}"""
//                  sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" \\
//                  --upload-file ${CONFIGURATION_PROVISIONING_DIR}/sample_sepp_poc_config.netconf ${SEPP_CONFIG}"""
//              }
//          }
//      }
        stage('Upload base stability traffic files to artifactory')
        {
            steps
            {
                script
                {
                    ARTIFACTORY_PATH = "https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/eiffelesc/${env.BRANCH}/${CSAR_PACKAGE_VERSION}"
                    CONFIGURATION_DIR = "./daft/templates/base_stability_traffic"
                }
                withCredentials([string(credentialsId: 'armseligic-armdocker-eiffelesc-identity-token-for-Smoke-CI', variable: 'ARTIFACTORY_TOKEN')])
                {
                    sh """perl ./daft/perl/bin/artifactory_base_stability_traffic_files.pl --token="$ARTIFACTORY_TOKEN" --full-artifactory-path=$ARTIFACTORY_PATH"""
//                  sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ${CONFIGURATION_DIR}/base_stability_traffic_bsf_scp_sepp.config ${ARTIFACTORY_PATH}/base_stability_traffic_bsf_scp_sepp.config"""
//                  sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ${CONFIGURATION_DIR}/traffic_config/action_bsf_init_db.netconf ${ARTIFACTORY_PATH}/traffic_config/action_bsf_init_db.netconf"""
//                  sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ${CONFIGURATION_DIR}/traffic_config/bsf_DT_Template.netconf ${ARTIFACTORY_PATH}/traffic_config/bsf_DT_Template.netconf"""
//                  sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ${CONFIGURATION_DIR}/traffic_config/scp_DT_Template.netconf ${ARTIFACTORY_PATH}/traffic_config/scp_DT_Template.netconf"""
//                  sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ${CONFIGURATION_DIR}/traffic_config/sepp_DT_Template.netconf ${ARTIFACTORY_PATH}/traffic_config/sepp_DT_Template.netconf"""
//                  sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ${CONFIGURATION_DIR}/traffic_scripts/k6/bsf_stability_test.js ${ARTIFACTORY_PATH}/traffic_scripts/k6/bsf_stability_test.js"""
//                  sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ${CONFIGURATION_DIR}/traffic_scripts/k6/scp_stability_test.js ${ARTIFACTORY_PATH}/traffic_scripts/k6/scp_stability_test.js"""
//                  sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ${CONFIGURATION_DIR}/traffic_scripts/k6/sepp_stability_test.js ${ARTIFACTORY_PATH}/traffic_scripts/k6/sepp_stability_test.js"""
//                  sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/var.chfsim-version ${ARTIFACTORY_PATH}/traffic_simulators/versions/"""
//                  sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/var.redis-version ${ARTIFACTORY_PATH}/traffic_simulators/versions/"""
//                  sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/export-chfsim/docker/eric-chfsim-1*.tar ${ARTIFACTORY_PATH}/traffic_simulators/docker/"""
//                  sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/export-chfsim/docker/eric-chfsim-redis-*.tar ${ARTIFACTORY_PATH}/traffic_simulators/docker/"""
//                  sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/export-chfsim/helm/eric-chfsim-1*.tgz ${ARTIFACTORY_PATH}/traffic_simulators/helm/"""
//                  sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/export-chfsim/helm/eric-chfsim-redis-*.tgz ${ARTIFACTORY_PATH}/traffic_simulators/helm/"""
//                  sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/var.seppsim-version ${ARTIFACTORY_PATH}/traffic_simulators/versions/"""
//                  sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/export-seppsim/docker/eric-seppsim-*.tar ${ARTIFACTORY_PATH}/traffic_simulators/docker/"""
//                  sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/export-seppsim/helm/eric-seppsim-*.tgz ${ARTIFACTORY_PATH}/traffic_simulators/helm/"""
//                  sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/var.nrfsim-version ${ARTIFACTORY_PATH}/traffic_simulators/versions/"""
//                  sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/export-nrfsim/docker/eric-nrfsim-*.tar ${ARTIFACTORY_PATH}/traffic_simulators/docker/"""
//                  sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/export-nrfsim/helm/eric-nrfsim-*.tgz ${ARTIFACTORY_PATH}/traffic_simulators/helm/"""
//                  sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/var.dscload-version ${ARTIFACTORY_PATH}/traffic_simulators/versions/"""
//                  sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/export-dscload/docker/eric-dscload-*.tar ${ARTIFACTORY_PATH}/traffic_simulators/docker/"""
//                  sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/export-dscload/helm/eric-dscload-*.tgz ${ARTIFACTORY_PATH}/traffic_simulators/helm/"""
//                  sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/var.k6-version ${ARTIFACTORY_PATH}/traffic_simulators/versions/"""
//                  sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/export-k6/docker/eric-k6-*.tar ${ARTIFACTORY_PATH}/traffic_simulators/docker/"""
//                  sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/export-k6/helm/eric-k6-*.tgz ${ARTIFACTORY_PATH}/traffic_simulators/helm/"""
//                  sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/var.influxdb-version ${ARTIFACTORY_PATH}/traffic_simulators/versions/"""
//                  sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/export-k6/docker/eric-influxdb-*.tar ${ARTIFACTORY_PATH}/traffic_simulators/docker/"""
//                  sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/export-k6/helm/eric-influxdb-*.tgz ${ARTIFACTORY_PATH}/traffic_simulators/helm/"""
//                  sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/var.sftp-version ${ARTIFACTORY_PATH}/traffic_simulators/versions/"""
//                  sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/export-sftp/docker/eric-atmoz-sftp-*.tar ${ARTIFACTORY_PATH}/traffic_simulators/docker/"""
//                  sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ./${OUTPUT_DIR}/export-sftp/helm/eric-atmoz-sftp-*.tgz ${ARTIFACTORY_PATH}/traffic_simulators/helm/"""
                }
            }
        }
//      stage('Pull supreme docker image and upload file to artifactory')
//      {
//          steps
//          {
//              script
//              {
//                  env.SUPREME_PULL_RESULT = "SUCCESS"
//                  env.SUPREME_SAVE_RESULT = "SUCCESS"
//                  env.SUPREME_UPLOAD_RESULT = "SUCCESS"
//                  env.OUTPUT_DIR_ABSPATH = """${sh( returnStdout: true, script: 'realpath ${OUTPUT_DIR}' )}""".trim();
//                  echo "OUTPUT_DIR_ABSPATH=${OUTPUT_DIR_ABSPATH}"
//                  try
//                  {
//                      sh """docker image pull armdocker.rnd.ericsson.se/proj-5g-bsf/supreme/supreme:${WANTED_SUPREME_VERSION}"""
//                  }
//                  catch(err)
//                  {
//                      env.SUPREME_PULL_RESULT = "FAILURE"
//                      env.SUPREME_SAVE_RESULT = "FAILURE"
//                      env.SUPREME_UPLOAD_RESULT = "FAILURE"
//                      env.MESSAGE = "Failed to pull supreme docker image"
//                  }
//                  if ("${SUPREME_PULL_RESULT}" == "SUCCESS")
//                  {
//                      try
//                      {
//                          sh """docker image save armdocker.rnd.ericsson.se/proj-5g-bsf/supreme/supreme:${WANTED_SUPREME_VERSION} -o ${OUTPUT_DIR_ABSPATH}/eric-supreme-${WANTED_SUPREME_VERSION}.tar"""
//                      }
//                      catch(err)
//                      {
//                          env.SUPREME_SAVE_RESULT = "FAILURE"
//                          env.SUPREME_UPLOAD_RESULT = "FAILURE"
//                          env.MESSAGE = "Failed to save supreme docker image"
//                      }

//                      if ("${SUPREME_SAVE_RESULT}" == "SUCCESS")
//                      {
//                          ARTIFACTORY_PATH = "https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/eiffelesc/${env.BRANCH}/${CSAR_PACKAGE_VERSION}/tools/docker/"
//                          try
//                          {
//                              withCredentials([string(credentialsId: 'armseligic-armdocker-eiffelesc-identity-token-for-Smoke-CI', variable: 'ARTIFACTORY_TOKEN')])
//                              {
//                                  sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file ${OUTPUT_DIR_ABSPATH}/eric-supreme-${WANTED_SUPREME_VERSION}.tar ${ARTIFACTORY_PATH}"""
//                              }
//                          }
//                          catch(err)
//                          {
//                              env.SUPREME_UPLOAD_RESULT = "FAILURE"
//                              env.MESSAGE = "Failed to upload the supreme docker file to artifactory"
//                          }
//                      }
//                  }
//                  echo "SUPREME_PULL_RESULT=${SUPREME_PULL_RESULT}"
//                  echo "SUPREME_SAVE_RESULT=${SUPREME_SAVE_RESULT}"
//                  echo "SUPREME_UPLOAD_RESULT=${SUPREME_UPLOAD_RESULT}"
//                  if ("${SUPREME_UPLOAD_RESULT}" == "FAILURE")
//                  {
//                      catchError(buildResult: 'FAILURE', stageResult: 'FAILURE')
//                      {
//                          echo "${MESSAGE}"
//                          sh "exit 1"
//                      }
//                  }
//              }
//          }
//      }
//      stage('Upload default certificate files to artifactory')
//      {
//          steps
//          {
//              script
//              {
//                  env.CERTIFICATE_CHECK_RESULT = "SUCCESS"
//                  ARTIFACTORY_PATH = "https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/eiffelesc/"
//                  try
//                  {
//                      withCredentials([string(credentialsId: 'armseligic-armdocker-eiffelesc-identity-token-for-Smoke-CI', variable: 'ARTIFACTORY_TOKEN')])
//                      {
//                          sh """/proj/DSC/rebels/bin/artifactory_certificate_files.pl --token=$ARTIFACTORY_TOKEN --url-base-development=$ARTIFACTORY_PATH --directory-path=/proj/DSC/rebels/certificates --release=$env.BRANCH --build=$CSAR_PACKAGE_VERSION"""
//                      }
//                  }
//                  catch(err)
//                  {
//                      env.CERTIFICATE_CHECK_RESULT = "FAILURE"
//                      env.MESSAGE = "Failed to upload the certificat files to artifactory"
//                  }
//                  echo "CERTIFICATE_CHECK_RESULT=${CERTIFICATE_CHECK_RESULT}"
//                  if ("${CERTIFICATE_CHECK_RESULT}" == "FAILURE")
//                  {
//                      catchError(buildResult: 'FAILURE', stageResult: 'FAILURE')
//                      {
//                          echo "${MESSAGE}"
//                          sh "exit 1"
//                      }
//                  }
//              }
//          }
//      }
//      stage('Run cbos-age-tool and upload files to artifactory')
//      {
//          steps
//          {
//              script
//              {
//                  env.CBOS_CHECK_RESULT = "SUCCESS"
//                  env.OUTPUT_DIR_ABSPATH = """${sh( returnStdout: true, script: 'realpath ${OUTPUT_DIR}' )}""".trim();
//                  echo "OUTPUT_DIR_ABSPATH=${OUTPUT_DIR_ABSPATH}"
//                  try
//                  {
//                      sh """docker run --rm --volume /home/eiffelesc:/home/eiffelesc --volume ${OUTPUT_DIR_ABSPATH}:${OUTPUT_DIR_ABSPATH} --workdir ${OUTPUT_DIR_ABSPATH} armdocker.rnd.ericsson.se/proj-adp-cicd-drop/common-library-adp-helm-dr-check:latest cbos-age-tool -a=${OUTPUT_DIR_ABSPATH}/eric-sc-${CSAR_PACKAGE_VERSION}.csar -o=${OUTPUT_DIR_ABSPATH} -C -s="Age check of eric-sc-${CSAR_PACKAGE_VERSION}.csar" -Dhelmdrck.credential.file.path=/home/eiffelesc/.artifactory/helm_repositories.yaml"""
//                  }
//                  catch(err)
//                  {
//                      env.CBOS_CHECK_RESULT = "FAILURE"
//                      env.MESSAGE = "Failed to execute cbos-age-tool"
//                  }
//                  if ("${CBOS_CHECK_RESULT}" == "SUCCESS")
//                  {
//                      ARTIFACTORY_PATH = "https://arm.seli.gic.ericsson.se/artifactory/proj-5g-bsf-generic-local/eiffelesc/${env.BRANCH}/${CSAR_PACKAGE_VERSION}/cbos-age-reports/"
//                      try
//                      {
//                          withCredentials([string(credentialsId: 'armseligic-armdocker-eiffelesc-identity-token-for-Smoke-CI', variable: 'ARTIFACTORY_TOKEN')])
//                          {
//                              sh """curl -f -k -H "X-JFrog-Art-Api:$ARTIFACTORY_TOKEN" --upload-file "{\$(echo ${OUTPUT_DIR_ABSPATH}/cbos-age-report-* | tr ' ' ',')}" ${ARTIFACTORY_PATH}"""
//                              // The files are called something like this:
//                              // cbos-age-report-eric-sc-umbrella-1.7.25+627-202201171545.html
//                              // cbos-age-report-eric-sc-umbrella-1.7.25+627-202201171545.json
//                              // cbos-age-report-eric-sec-certm-crd-3.11.0+81-202201171545.html
//                              // cbos-age-report-eric-sec-certm-crd-3.11.0+81-202201171545.json
//                              // cbos-age-report-eric-sec-sip-tls-crd-2.10.0+40-202201171545.html
//                              // cbos-age-report-eric-sec-sip-tls-crd-2.10.0+40-202201171545.json
//                              // cbos-age-report-eric-tm-ingress-controller-cr-crd-6.1.0+55-202201171544.html
//                              // cbos-age-report-eric-tm-ingress-controller-cr-crd-6.1.0+55-202201171544.json
//                          }
//                      }
//                      catch(err)
//                      {
//                          env.CBOS_CHECK_RESULT = "FAILURE"
//                          env.MESSAGE = "Failed to upload the report files to artifactory"
//                      }
//                  }
//                  echo "CBOS_CHECK_RESULT=${CBOS_CHECK_RESULT}"
//                  if ("${CBOS_CHECK_RESULT}" == "FAILURE")
//                  {
//                      catchError(buildResult: 'FAILURE', stageResult: 'FAILURE')
//                      {
//                          echo "${MESSAGE}"
//                          sh "exit 1"
//                      }
//                  }
//              }
//          }
//      }
        stage('Archiving & Mail notification csar pkg creation')
        {
            steps
            {
                script
                {
                    DISPLAY_COMMIT = """${sh( returnStdout: true, script: 'git log -n1 --abbrev-commit --format="%h"')}"""
                }
                script
                {
                    DISPLAY_COMMITTER = """${sh( returnStdout: true, script: 'git log --format="%cN" | head -1')}""".trim()
                }

                sh """echo "CSAR_PACKAGE_LATEST=${CSAR_PACKAGE_VERSION}" > ./artifact.properties"""
                sh """echo "BRANCH=${env.BRANCH}" >> ./artifact.properties"""
                archiveArtifacts allowEmptyArchive: false, artifacts: 'artifact.properties', onlyIfSuccessful: false

                script
                {
                    if ("${env.MAIL}" != 'null')
                    {
                        def emailbody_csar_created_1="\na new csar package called eric-sc-${CSAR_PACKAGE_VERSION}.csar has been created from"
                        def emailbody_csar_created_2=" the development branch: ${env.BRANCH} based on dev commit: ${DISPLAY_COMMIT} by user: ${DISPLAY_COMMITTER}."
                        def emailbody_csar_created_3="\nIt is located under the folder ${CSAR_PACKAGE_VERSION} on ${env.REPO_PATH}/${env.BRANCH}/"

                        emailext body: "${emailbody_start}"+"${emailbody_csar_created_1}"+"${emailbody_csar_created_2}"+"${emailbody_csar_created_3}"+"${emailbody_end}",
                        subject: "New csar package automatically created for branch ${env.BRANCH}", to: "${env.MAIL}"
                    }
                }
            }
        }
    }
    post
    {
        always
        {
            echo 'Removing the newly created images to free space in the build slave registry'
            script
                {
                    IMAGES_VERSION = """${sh( returnStdout: true, script: "cat ./${OUTPUT_DIR}/var.esc-version | sed s/+/-/")}""".trim()
                }
            sh """docker images | grep ${IMAGES_VERSION} | awk '{print \$1 ":" \$2}' | xargs docker rmi 2>/dev/null || true"""

            echo 'Cleaning space in the build slave'
            sh "sudo bash -c 'rm -rf ./${OUTPUT_DIR}/source ./${OUTPUT_DIR}/docker.tar'"

            echo 'Removing the csar pkg to free space in the build slave'
            sh "sudo bash -c 'rm -rf ./${OUTPUT_DIR}/eric-sc-${CSAR_PACKAGE_VERSION}.csar'"

            echo 'Removing any tmp folder which might have root rights and prevents upcoming packages creation on the same build slave workspace'
            sh "sudo bash -c 'rm -rf ./${OUTPUT_DIR}/tmp*'"

        }
        aborted
        {
            script
            {
                if ("${env.MAIL}" != 'null')
                {
                    def emailbody_job_aborted_1="\nthe job creating a new csar package aborted due to a timeout during the final package creation."
                    def emailbody_job_aborted_2="\n\nPlease check the log under: ${LOGS_LINK}/$BUILD_NUMBER/consoleFull"

                    emailext body: "${emailbody_start}"+"${emailbody_job_aborted_1}"+"${emailbody_job_aborted_2}"+
                    "${emailbody_end}", subject: "Automatic csar package creation aborted for branch ${env.BRANCH}", to: "${env.MAIL}"
                }
            }
        }
        failure
        {
            script
            {
                if ("${env.MAIL}" != 'null')
                {
                    def emailbody_job_failure_1="\nthe job creating a new csar package has failed."
                    def emailbody_job_failure_2="\n\nPlease check the log under:\n${LOGS_LINK}/$BUILD_NUMBER/consoleFull"

                    emailext body: "${emailbody_start}"+"${emailbody_job_failure_1}"+"${emailbody_job_failure_2}"+"${emailbody_end}",
                    subject: "Automatic csar package creation failed for branch ${env.BRANCH}${ADD_MAIL_MSG}", to: "${env.MAIL}"
                }
            }
        }
    }
}
