/*
<===== MATCHING QUERIES:   ======>
${trigger['parameters']['SCHEDULER_TYPE']=='node-scheduler'}
evaluateExpression?expression=%24%7Btrigger%5B'parameters'%5D%5B'SCHEDULER_TYPE'%5D%3D%3D'node-scheduler'%7D

 $    {   trigger  [          'parameters'    ]  [       'SCHEDULER_TYPE'   ]     ==     'node-scheduler'   }
%24  %7B  trigger  %5B        'parameters'  %5D%5B       'SCHEDULER_TYPE'  %5D  %3D%3D   'node-scheduler'  %7D

$   %24
{   %7B
[   %5B
]   %5D
=   %3D
}   %7D
#   %23
&   %26
!   ! (same)
b   %20
"   %22
:   %3A (needed for REASON)
 */

def AGENT_LABEL = null
def EXCLUSIVE_BS_LABEL = '5G-ESC-001'
def POOL_BS_LABEL = '5G-SC'

node('DSC_BuildSlave_GIC_1||DSC_BuildSlave_GIC_2||DSC_BuildSlave_GIC_3||DSC_BuildSlave_GIC_4||DSC_BuildSlave_GIC_5||SERO_GIC')
{
    stage('set agent')
    {
        script
        {

            online_node = nodesByLabel label: """$EXCLUSIVE_BS_LABEL""", offline: false
            if (online_node)
            {
                echo """Agent with label $EXCLUSIVE_BS_LABEL is online. The job will run on it."""
                AGENT_LABEL = EXCLUSIVE_BS_LABEL
            }
            else
            {
                echo """Agent with label $EXCLUSIVE_BS_LABEL is offline. The job will be deviated to the pool of build slaves. with label $POOL_BS_LABEL"""
                AGENT_LABEL = POOL_BS_LABEL
            }
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
        }
    }
    options
    {
        timeout(time: 15, unit: 'MINUTES')
        timestamps()
    }
    environment
    {
        SCHEDULER_TYPE = "${SCHEDULER_TYPE}"
        PIPELINE = "${PIPELINE}"
        NODE = "${NODE}"
        REQUEST_ID = "${REQUEST_ID}"
        DELETE_STAMP_SCHEDULER = "${DELETE_STAMP_SCHEDULER}"
        END_TIME = "${END_TIME}"

        SCHEDULER_PATH="/proj/DSC/rebels/IaaS"
        JENKINS_INSTANCE = "https://fem1s10-eiffel029.eiffel.gic.ericsson.se:8443/jenkins/job/5G-ESC/job/IaaS/job"
        PIPELINE_STATUS="RUNNING"
        SED_CMD = "sed 's/\\\$/%24/g' | sed 's/{/%7B/g' | sed 's/\\[/%5B/g' | sed 's/]/%5D/g' | sed 's/=/%3D/g' | sed 's/}/%7D/g' | sed 's/#/%23/g' | sed 's/&/%26/g' | sed 's/!/!/g' | sed 's/ /%20/g' | sed 's/\"/%22/g' | sed 's/:/%3A/g'"
    }
    stages
    {
        stage('Check user')
        {
             steps
            {
                wrap([$class: 'BuildUser'])
                {
                    script
                    {
                        triggering_user = """${sh( returnStdout: true, script: "echo ${BUILD_USER_ID}")}""".trim()
                    }
                }
                script
                {
                    if ("${triggering_user}" != "eiffelesc")
                    {
                        error("As there are more actions connected to this job, please do not trigger it as stand alone but trigger it via Spinnaker pipeline IaaS-pipeline-cancelation.")
                    }
                }
            }
        }
        stage('Get pipelines-id')
        {
            steps
            {
                sh """ rm -rf *.*"""
                script
                {
                    LOGS_LINK = """${sh( returnStdout: true, script: "echo ${JENKINS_INSTANCE}/${JOB_BASE_NAME}")}""".trim()

                    if ("${env.END_TIME}" == 'null')
                    {
                        CURRENT_DATE_MS = sh(script: "date +%s%N | cut -b1-13", returnStdout: true).trim()
                    }
                    else
                    {
                        long receivedTime
                        long substract = 3000 // In order to avoid the deletion of an OWN Solution-Trigger pipeline, the check for running pipelines is done up to 3000 ms prior the triggering of the pipeline
                        long operatedTime
                        receivedTime="${END_TIME}".toLong()
                        operatedTime = receivedTime - substract
                        CURRENT_DATE_MS="${operatedTime}".toString().trim()
                    }
                    ARTIFACT_STATEMENT ="no" // only set to "yes" in case the deletion of pipe type Sol-CI for a node takes place -> This will indicate to Spinnaker not to trigger further deletion on that node.
                    withCredentials([usernameColonPassword(credentialsId: '7b8a9097-73ab-4e79-8b06-b8dcbbf65c3a', variable: 'API_TOKEN')])
                    {
                        EXECUTION_DETAILS = sh(script: "curl -H \"accept: */*\" -X GET -u $API_TOKEN \"https://spinnaker-api.rnd.gic.ericsson.se/applications/esc5gtest/executions/search?expand=false&pipelineName=${PIPELINE}&reverse=false&size=10&startIndex=0&statuses=${PIPELINE_STATUS}&triggerTimeEndBoundary=${CURRENT_DATE_MS}&triggerTimeStartBoundary=0\"", returnStdout: true).trim()
                    }
                    if ( ! ("${EXECUTION_DETAILS}".contains("$PIPELINE")))
                    {
                        PIPELINES_RUNNING = "no"
                        println("Currently there are no pipelines type $PIPELINE running. No cancelation for this type will be triggered.")
                    }
                    else
                    {
                        PIPELINES_RUNNING = "yes"
                        writeFile(file: "execution_details_json.txt", text: EXECUTION_DETAILS)
                        sh """cat ./execution_details_json.txt | tr "," \\\\n > ./execution_details_json_structured.txt"""

                        NUMBER_RUNNING_PIPELINES = """${sh(returnStdout: true, script: "cat ./execution_details_json_structured.txt | grep $PIPELINE | wc -l")}""".trim().toInteger()
                        println("There are $NUMBER_RUNNING_PIPELINES pipelines type $PIPELINE running.")

                        // sh( returnStdout: true, script: "sed -n '/PIPELINE/{n;p;}' ./execution_details_json_structured.txt > ./list-of-pipelines.txt") //This one is not working when Solution-CI is running. It is taken the next line after PIPELINE.
                        // sh( returnStdout: true, script: "sed '1N; \$!N; /.*\\n.*\\n.*$PIPELINE/P; D' ./execution_details_json_structured.txt > ./list-of-pipelines.txt") //This one picks the 2nd line before the name of hte pipeline is matched.
                        // When triggering the jobs via Jenkins worked BUT via Spinnaker there are more lines added, so it is not the 2nd but last line, but the 5th last before the match

                        sh( returnStdout: true, script: "grep -B 5 $PIPELINE ./execution_details_json_structured.txt > ./execution_details_json_structured_reduced.txt") // Here each line of the matching pipeline-name is copied AND its previous 5 lines too !!!
                        sh( returnStdout: true, script: "cat ./execution_details_json_structured_reduced.txt | grep id > ./list-of-pipelines.txt") // Here only each line containing the id is transferred

                        pipelines_id_array=[]
                        int RANKING = NUMBER_RUNNING_PIPELINES
                        a=0
                        while ( RANKING > 0 )
                        { // start from the higher value and continue decreasing
                            element = """${sh(returnStdout: true, script: "cat ./list-of-pipelines.txt | head -$RANKING | tail -1 | cut -d '\"' -f4")}""".trim()
                            pipelines_id_array[a] = element
                            a=a+1
                            RANKING = RANKING -1 // decrease the list to go to the previous element
                        }
                    }
                }
            }
        }
        stage('Cancel applicable pipelines')
        {
            when {expression {PIPELINES_RUNNING =="yes"}}
            steps
            {
                script
                {
                    QUERY_SCHEDULER = "\\\${trigger[\\'parameters\\'][\\'SCHEDULER_TYPE\\']==\\'${env.SCHEDULER_TYPE}\\'}"
                    QUERY_NODE = "\\\${trigger[\\'parameters\\'][\\'NODE\\']==\\'${env.NODE}\\'}"
                    QUERY_SOLUTION_CI = "\\\${#stage\\(\\'Trace Scheduler\\'\\)[\\'outputs\\'][\\'SCHEDULER_ORIGIN\\']==\\'${env.SCHEDULER_TYPE}\\'}"

                    REASON= "Triggered by Spinnaker pipeline IaaS-pipeline-cancelation with REQUEST_ID=${REQUEST_ID}"
                    PIPELINE = """${sh( returnStdout: true, script: "echo ${PIPELINE}")}""".trim() //w/o trim the content value for the switch operation is different than the original value

                    if ("${env.NODE}" != 'null')
                    {
                        MSG_INFO = "for node ${NODE}"
                        switch (PIPELINE)
                        {
                            case [ 'IaaS-SC-Solution-trigger', 'IaaS-verification-CNF' ]:
                                QUERY = QUERY_NODE
                                break
                            default:
                                error("It is not expected to have pipeline $PIPELINE for this query !!! Please check.")
                                break
                        } //switch
                    }
                    else
                    {
                        MSG_INFO = "for scheduler type ${SCHEDULER_TYPE}"
                        switch (PIPELINE)
                        {
                            case [ 'IaaS-node-scheduler', 'IaaS-verification-CNF' ]:
                                QUERY = QUERY_SCHEDULER
                                break
                            case [ 'IaaS-SC-Solution-trigger' ]:
                                QUERY = QUERY_SOLUTION_CI
                                break
                            default:
                                error("It is not expected to have pipeline $PIPELINE for this query !!! Please check.")
                                break
                        } //switch
                    }

                    REASON_TO_CURL= """${sh( returnStdout: true, script: "echo ${REASON} | ${SED_CMD}")}""".trim()
                    QUERY_TO_CURL= """${sh( returnStdout: true, script: "echo ${QUERY} | ${SED_CMD}")}""".trim()

                    a=0
                    d=0
                    while (a < NUMBER_RUNNING_PIPELINES)
                    {
                        PIPELINE_ID = pipelines_id_array.get(a)
                        withCredentials([usernameColonPassword(credentialsId: '7b8a9097-73ab-4e79-8b06-b8dcbbf65c3a', variable: 'API_TOKEN')])
                        {
                            QUERY_RESULT = sh(script: "curl -H \"accept: */*\" -X GET -u $API_TOKEN \"https://spinnaker-api.rnd.gic.ericsson.se/pipelines/${PIPELINE_ID}/evaluateExpression?expression=${QUERY_TO_CURL}\"", returnStdout: true).trim()
                        }
                        if ( "${QUERY_RESULT}".contains("true"))
                        {
                            withCredentials([usernameColonPassword(credentialsId: '7b8a9097-73ab-4e79-8b06-b8dcbbf65c3a', variable: 'API_TOKEN')])
                            {
                                sh(script: "curl -H \"accept: */*\" -X PUT -u $API_TOKEN \"https://spinnaker-api.rnd.gic.ericsson.se/pipelines/${PIPELINE_ID}/cancel?force=false&reason=${REASON_TO_CURL}\"", returnStdout: true).trim()
                            }
                            d=d+1
                            println(" Pipeline type $PIPELINE with id:$PIPELINE_ID has been canceled (it matched the query to cancel pipelines running $MSG_INFO).")

                            if("${env.NODE}" != 'null')
                            {
                                // free node in case scheduler is running, so that the next file can be scheduled.
                                // we delete the stamps in the 2 possible schedulers. A more accurate process would be possible by making a query on the pipeline deleted about its scheduler type and delete the stamp for the given value (in case it was different than none)
                                // This can only be done if the calling process is NOT at the beginning of the nigthly RV clean up activities, as otherwise the proper marking for the 1st nightly run would be removed

                                if("${DELETE_STAMP_SCHEDULER}" == "yes")
                                {
                                    sh """rm -f ${SCHEDULER_PATH}/node-scheduler/${NODE}-In_Use.tmp"""
                                    sh """rm -f ${SCHEDULER_PATH}/additional-node-scheduler/${NODE}-In_Use.tmp"""
                                }

                                if("${PIPELINE}" == 'IaaS-SC-Solution-trigger')
                                {
                                    ARTIFACT_STATEMENT ="yes" // it is not needed to execute the next stage (deleting IaaS-verification-CNF for this ndde) in Spinnaker
                                    // as this takes place automatically when canceling the parent pipeline (IaaS-SC-Solution-trigger), which has just taken place right now.
                                }
                            }
                            sleep(3)
                        }
                        a=a+1
                    }
                    if (d==0)
                    {
                        println(" No pipeline type $PIPELINE has been canceled (it didn't match the query to cancel pipelines running $MSG_INFO).")
                    }
                }
            }
        }
    }
    post
    {
        always
        {
            script
            {
                if("${PIPELINE}" == 'IaaS-SC-Solution-trigger')
                {
                    sh """echo "PARENT_SOLUTION-CI_PIPELINE_DELETED=${ARTIFACT_STATEMENT}" > ./artifact.properties"""
                    archiveArtifacts allowEmptyArchive: true, artifacts: "artifact.properties", onlyIfSuccessful: false
                }
            }
            cleanWs()
        }
        failure
        {
            script
            {
                    def emailbody_job_failure_1="\nthe job triggered by Spinnaker pipeline \"IaaS-pipeline-cancelation\" with REQUEST_ID=${REQUEST_ID} has failed."
                    def emailbody_job_failure_2="\n\nDetailed logs for the Jenkins job can be found under:\n${LOGS_LINK}/$BUILD_NUMBER/consoleFull"

                    emailext body: "${emailbody_start}"+"${emailbody_job_failure_1}"+"${emailbody_job_failure_2}"+"${emailbody_end}",
                    subject: 'Job for canceling pipelines failed', to: "eedjoz@eed.ericsson.se"
            }
        }
    }
}