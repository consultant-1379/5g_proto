
//////////////////////////////////////////////////////////////////////////
//
// first run on SERO_GIC buildslaves, decide which buildslave(s) to use

def AGENT_LABEL = null

node('DSC_BuildSlave_GIC_1||DSC_BuildSlave_GIC_2||DSC_BuildSlave_GIC_3||DSC_BuildSlave_GIC_4||DSC_BuildSlave_GIC_5') {

  stage('set agent'){

     if (env.IP_VERSION == '6') { // move to IPv6 buildslave(s)
        AGENT_LABEL = '5G-SC-IPv6'

     } else // move to IPv4 buildslave(s)
     {
         AGENT_LABEL = '5G-SC' // in the future
     } //if

   } //stage

} //node

//
//////////////////////////////////////////////////////////////////////////


//////////////////////////////////////////////////////////////////////////
// Set email body

def emailbody0=""
def emailbody1="Hi team, "+ "<br><br>ADP CI/CD Test results for ADP Service: ${CHART_NAME} ${CHART_VERSION} <br><br>"
def emailbody2=""
def emailbody3="<br>"
def emailbody4="<br>Jcat logs:<br>"+"${env.BUILD_URL}"+"<br><br><br><br>Thank you for testing.<br><br>BR,<br>Team Challengers"

//////////////////////////////////////////////////////////////////////////
// Deployment is ready and test can be run on it
def ENV_READY = false

def TROUBLESHOOTING_FAILURE_REASON=""


//////////////////////////////////////////////////////////////////////////
//   Subroutines

// look for suite name in latestjcatlogs_$nr/dt.xml
String retrieveSuite(String nr) {
    env.someVar=nr
    echo "looking for suite name in latestjcatlogs_${env.someVar}/dt.xml"
    suite= sh (returnStdout: true,
        script:"cat latestjcatlogs_${env.someVar}/dt.xml | grep -o -P \"5G.*Suite(-rerun)?\"")
    echo "Found suite: $suite"  
    return (suite)
}

// look for suite info in latestjcatlogs_$nr/dt.xml
String retrieveInfo(String nr) {
    env.someVar2=nr
    echo "looking for suite info in latestjcatlogs_${env.someVar2}/dt.xml"
    info= sh (returnStdout: true,
        script:"cat latestjcatlogs_${env.someVar2}/dt.xml | grep -o -P \"suite name.*ExecutionFinished\"")
    echo "Found info: $info"  
    return (info)
}
//
//////////////////////////////////////////////////////////////////////////

// set the displayed build name to BUILD_NUMBER | COMMIT
currentBuild.displayName = "#${BUILD_NUMBER} - ${env.COMMIT}"

pipeline {

   agent { label "${AGENT_LABEL}"   } // set label according to IP_VERSION parameter

   options{
      timeout(time: 50, unit: 'MINUTES')
      timestamps()
   }

   environment{
     

      // Pipe Variables Used in Jenkins
      VERSION = ''
      PRODUCT = ''
      KUBE_HOST = "${env.KUBE_HOST}"
      NAMESPACE = "${env.NAMESPACE}"
      ARM_ARTIFACTORY_TOKEN = "${ARM_ARTIFACTORY_TOKEN}"
      TROUBLESHOOTING = "${env.TROUBLESHOOTING}"
       BRANCH = "${env.BRANCH}"
       TEST_BRANCH = "${env.TEST_BRANCH}"
      REDIS_CONTAINER = "eric-chfsim-redis"
       COMMIT = "${COMMIT}"
      RERUN = "${env.RERUN}"
   }

   stages {
      stage('Print env vars') {
            steps {
                sh 'printenv | sort'
                script{
                    // set displayed build name to "BUILD_NUMBER - COMMIT"
                    currentBuild.displayName = "#${BUILD_NUMBER} - ${env.COMMIT}"
                    // set displayed description to "NODE_NAME, KUBE_HOST, CHART_VERSION"
                    currentBuild.description = "${env.NODE_NAME}, ${env.KUBE_HOST}, ${env.CHART_VERSION}";

                    if ( fileExists("/proj/DSC_CI/jenkins/tools/help_scripts/concurrentBuilds.sh") ) {
                        // Log current job 
                        sh "/proj/DSC_CI/jenkins/tools/help_scripts/concurrentBuilds.sh -start"
                    } // if
                } //script
            } //steps
        } //stage
      stage('Freeing up workspace on other buildslaves'){
         steps{
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

      stage('Clone test repository'){
         steps{
            echo 'Checking user'
            sh 'whoami'
            echo 'check path'
            echo pwd()
            sh 'git submodule update --init --recursive'
            sh './bob/bob init:set-build-proxy'
            sh './bob/bob init:set-kube-config'
            sh './bob/bob build-libs-ci'
            /*dir('esc'){
               echo 'Building the Libs'
               sh 'make clean'
               sh 'make libs'
            } */
            echo 'Creating test repo directory'
            dir('5g_test_ci'){
               deleteDir()
            }
            echo pwd()
            echo 'Trying to clone the 5G prototype test repository'
            checkout scmGit(
            branches: [[name: '*/${TEST_BRANCH}']],
            userRemoteConfigs: [[credentialsId: 'eiffelesc-user-password', name: 'origin', url: 'https://eiffelesc@gerrit.ericsson.se/a/MC_5G/5g_test_ci']],
            extensions: [submodule(parentCredentials: true, recursiveSubmodules: true, reference: '', shallow: false), [$class: 'RelativeTargetDirectory', relativeTargetDir: '5g_test_ci']])
            echo 'check content'
            dir('5g_test_ci'){
               echo pwd()
               sh 'git log -n 10'
               sh 'ls -ltrh'
               sh 'git submodule update --init --recursive'
            } // dir
         } // steps
      } // stage

      stage('Test Statistics check') {
            steps {
               script{
                  env.LOGDB = "true"
                  echo "Set Test Statistics setting, LOGDB= ${env.LOGDB}"
               } // script
           } //steps
      } // stage

      stage('Test Preparation') {
         steps {
            dir('5g_test_ci'){
               sh "./bob/bob init"
               script{
                     if (env.IP_VERSION == '6') {   
                         sh "./bob/bob adptest:ft-set-ports"
                          sh "./bob/bob adptest:prepareft6"
                     } // if
                     else{
                         sh "./bob/bob adptest:ft-set-ports"
                          sh "./bob/bob adptest:prepareft"
                     } // if
                 } //script
               sh "./bob/bob adptest:ft-set-redis-name"
               sh "./bob/bob adptest:ft-start-redis"
            } // dir
         } // steps
      } // stage

      stage('Trigger function test suites') {
         steps {
            script {
               //echo ENV_READY
               //if ( ENV_READY == true ) {
                  dir('5g_test_ci') {
                     sh "./bob/bob adptest:jenkinsft"
                  } // dir
               //} // if
            } // script
         } // steps
      } // stage

      stage('Test Restoration') {
         steps {
            dir('5g_test_ci'){
               sh "./bob/bob adptest:ft-remove-redis;"
               sh "./bob/bob adptest:ft-release-ports;"
            } // dir
         } // steps
      } // stage

      stage('Checking test results'){
         steps{
            dir('5g_test_ci'){
               sh returnStatus: false, script: '''FAILED=0;
               SKIPPED=0;
                  for i in ./jcat-esc/common-testcases/target ./jcat-esc/proxy-testcases/target-scp ./jcat-esc/proxy-testcases/target-sepp ./jcat-esc/bsf-testcases/target;
                  do 
                     if [ "$FAILED" -eq 0 ] && [ "$SKIPPED" -eq 0 ];
                     then
                        if [ -f $i/failsafe-reports/testng-results.xml ];
                        then
                               FAILED=$(grep -oE "failed=\\"[0-9]{1,}\\"" $i/failsafe-reports/testng-results.xml | cut -d\'=\' -f2 | sed "s/\\"//g");
                               SKIPPED=$(grep -oE "skipped=\\"[0-9]{1,}\\"" $i/failsafe-reports/testng-results.xml | cut -d\'=\' -f2 | sed "s/\\"//g");
                        fi;
                     fi;
                  done;
                  if [ "$FAILED" -eq 0 ];
                  then 
                      echo "All TCs were successful";
                      echo "TROUBLESHOOTING=FALSE" > ../concl.properties;
                  else
                      echo "Obviously some TCs failed!!";
                      echo "TROUBLESHOOTING=TRUE" > ../concl.properties;
                  fi;
                  echo "TEST_BRANCH=${TEST_BRANCH}" >> ../concl.properties; 
                  echo "TEST_COMMIT=$(git log -n1 --abbrev-commit --format="%h")" >> ../concl.properties;
                  echo "CHART_NAME=${CHART_NAME}" >> ../concl.properties;
                  echo "CHART_REPO=${CHART_REPO}" >> ../concl.properties;
                  echo "CHART_VERSION=${CHART_VERSION}" >> ../concl.properties;'''
            } // dir
            archiveArtifacts allowEmptyArchive: true, artifacts: 'concl.properties', onlyIfSuccessful: false
            archiveArtifacts allowEmptyArchive: true, artifacts: '5g_test_ci/logs_${NAMESPACE}_*.tgz', onlyIfSuccessful: false
         } // steps
      } //stage

         stage('Set verdict'){
         steps {
            script {
               def exists = fileExists 'concl.properties'
               if (exists) {
                  def data = readFile(file: 'concl.properties')
                  if (data.contains("TROUBLESHOOTING=FALSE")) {
                     emailbody0=emailbody0+"<body style=\"background-color: LightGreen;\">"; // body in LightGreen
                     emailbody3=emailbody3+"ADP CI/CD test verdict: <b><font color=\"#228B22\"> Success</font></b><br>" // in green
                  } // if
                  else {
                     emailbody0=emailbody0+"<body style=\"background-color: Salmon;\">"; // body in Salmon
                     emailbody3=emailbody3+"ADP CI/CD test verdict: <b><font color=\"#FF0000\"> Failed </font></b><br>" // in red
                  } // else
               } //if   
               else {
                       emailbody3=emailbody3+"ADP CI/CD test verdict: <b>undefined</b><br>"
               } //else
            } // script
         } // steps
      } // stage

       stage('Archiving JCAT logs'){
         environment{
            DIRS=0
         }
         steps{
            dir('5g_test_ci'){
               script{  
                  // copy jcat reports and count them
                  DIRS = sh (
                     script: ' pwd; ls; cd jcat-esc/jcatlogs/$(date +%Y)*; pwd; ls; j=1; for i in $(ls); do echo $i ; mkdir ../../../latestjcatlogs_$j ; cp -R ./$i/. ../../../latestjcatlogs_$j/. ; echo $j ; j=$((j+1)) ; done; echo $j;' ,
                     returnStdout: true).trim()
                  last1 = "${DIRS}"[-1..-1] // Last 1 symbol
                  echo "$last1"
                  // define the lists 
                  def SUITES=[]
                  def INFO=[]
                  // retrieve information from the dt.xml files from the latestjcatlog_$x dirs
                  for (def x=1;x<"$last1".toInteger();x++){
                     SUITES.add(retrieveSuite("$x"))
                     INFO.add(retrieveInfo("$x").replace("ExecutionFinished",""))
                  } // for
                  // set the base data for the html report
                  def baseReportDir="latestjcatlogs_"
                  def reportdir=""
                  def baseReportName="jcatReport_"
                  def reportName=""
                  // for each run
                  for (def x=1;x<"$last1".toInteger();x++){
                     // set info for the run into the email
                      script{
                          emailbody2=emailbody2+INFO.get(x-1)+"<br>"
                       } //script
                     reportDir="$baseReportDir"+"$x" // add suite nr to basedir
                     //echo "$reportDir"
                     reportName=SUITES.get(x-1) // reportname is suite name
                     //echo "$reportName"
                     indexName="index.html" // index name
                     //echo "$indexName"
                     publishHTML([allowMissing: false,
                                  alwaysLinkToLastBuild: false,
                                  keepAll: true,
                                  reportDir: "$reportDir",
                                  reportFiles: "$indexName",
                                  reportName: "$reportName",
                                  reportTitles: "$reportName", 
                                  escapeUnderscores: false])
                  } // for
               } // script
            } //dir
         }//steps  
      }//stage
   } // stages

    post{
      always {
            script{
                // Log current job
                if ( fileExists("/proj/DSC_CI/jenkins/tools/help_scripts/concurrentBuilds.sh") ) {
                    sh "/proj/DSC_CI/jenkins/tools/help_scripts/concurrentBuilds.sh -stop"
                } // if
            } // script
        } // always
        failure{
         script{
            dir('5g_test_ci'){
               try {
                  sh "./bob/bob adptest:ft-remove-redis"
                  sh "./bob/bob adptest:ft-release-ports"
               } catch (exc) {
                  TROUBLESHOOTING_FAILURE_REASON += ("Exception caught while trying to remove redis")
                  echo "${exc}"
                  
               }
            } //dir
            sh "./bob/bob init:set-namespace" // needs to be checked if needed
            try{
               sh "./scripts/collect_ADP_logs.sh -c ~/.kube/${env.KUBE_HOST}.config -n `cat .bob/var.namespace`"
               archiveArtifacts allowEmptyArchive: true, artifacts: 'logs_*.tgz', onlyIfSuccessful: false
            } catch(exc) {
               TROUBLESHOOTING_FAILURE_REASON += ("Exception caught while collecting logs")
            }
            
            echo "$TEST_TYPE"
            
            // clean cluster due to failure in pipeline
            if (env.CLEAN_CLUSTER != 'FALSE') {
               try {
                  timeout(time: 8, unit: 'MINUTES') {
                     sh "./bob/bob clean-resources:delete-namespace"
                     sh "./bob/bob clean-resources:create-namespace"
                     sh "./bob/bob clean-resources:remove-namespace-resources"
                     sh "./bob/bob clean-resources:remove-cluster-resources"
                  } // timeout
               } catch (exc) {
                           TROUBLESHOOTING_FAILURE_REASON += ("Failed to clean cluster forcefully after verification failure")
               } // try
                  
               if ("$TEST_TYPE".contains("AdpStaging")) {
                  if (env.UPGRADE_CRDS == 'true') { 
                     try {
                              timeout(time: 600000, unit: 'MILLISECONDS') {
                                 // Delete crds
                                 sh "./bob/bob undeploy-crds"
                              } // timeout
                           } catch (exc) {
                              TROUBLESHOOTING_FAILURE_REASON += ("Failed to clean CRDS after verification failure")

                           } // try
                  } //if
               } //if
            } //if
               
            // send mail to test team in case of job failure
            if ("$TEST_TYPE".contains("AdpStaging")) {
               def COLOR="<body style=\"background-color: Salmon;\">"
               echo "Sending email to Team Challengers due to pipeline failure"
               emailext body: "Hello ADP CICD owners,<br><br> SC application deployment failed while testing ${CHART_NAME} ${CHART_REPO} ${CHART_VERSION}.<br>Please take appropriate actions!<br><br> .Folowing are the possible Troubleshooting reasons: ${TROUBLESHOOTING_FAILURE_REASON} <br><br>.Please check:<br>"+"${emailbody4}",
               mimeType: 'text/html',
               subject: "SC ADP CICD Staging test failed for ${CHART_NAME} ${CHART_REPO} ${CHART_VERSION}",
               to: "IXG-ChallengersTeam@ericsson.onmicrosoft.com"
            } // if
            
            } // script
        } // failure

       // cleanup of ports needed in case job was aborted
        // needed in order to avoid filling up of file ports_used_CI
      aborted{
         script{
            dir('5g_test_ci'){
               try{
                  // decision made based on the existence of the different port variables
                  """if ( ls .bob | grep -q "port" );
                     then
                        echo "Need to clean some ports";
                        ./bob/bob adptest:ft-remove-redis;
                        ./bob/bob adptest:ft-release-ports;
                     fi;
                  """
               }catch (exc) {
                  sh " kubectl  delete ns ${env.NAMESPACE} --namespace ${env.NAMESPACE} --kubeconfig ~/.kube/${KUBE_HOST}.config "
                  sh " kubectl  create ns ${env.NAMESPACE} --namespace ${env.NAMESPACE} --kubeconfig ~/.kube/${KUBE_HOST}.config "
               } // catch 
            } //dir
            dir('5g_proto'){
               try{
                  timeout(time: 10, unit: 'MINUTES') {
                     // clean cluster due to failure in pipeline
                     sh "./bob/bob clean-resources:delete-namespace"
                     sh "./bob/bob clean-resources:create-namespace"
                     sh "./bob/bob clean-resources:remove-cluster-resources"
                  } // timeout
               }catch (exc) {
                  TROUBLESHOOTING_FAILURE_REASON += ("Failed to clean cluster forcefully after verification abort")
                  //sh " kubectl  delete ns ${env.NAMESPACE} --namespace ${env.NAMESPACE} --kubeconfig ~/.kube/${KUBE_HOST}.config " 
                  //sh " kubectl  create ns ${env.NAMESPACE} --namespace ${env.NAMESPACE} --kubeconfig ~/.kube/${KUBE_HOST}.config " 
               } // catch 
            } //dir
         }//script
           
      } // aborted
        
   } // post

} // pipeline
