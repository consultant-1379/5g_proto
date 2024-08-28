#!/usr/bin/env python3

import yaml
import sys
import os
import datetime
from subprocess import call
from argparse import ArgumentParser
from argparse import RawTextHelpFormatter
from shutil import copyfile
import textwrap
from dpath import util as dpath_util
from createXlsx import generateXlsx
from createCSV import generateCSV
from envoy import envoyDependency



#THE TAGS IN THE FOSS FILES CAN NOT BE EMPTY
#IF ManualSpecificFOSSEvaluation VALUE IS YES THEN DE DEFAULT VALUES FROM THE defaultTemplate.yaml ARE NOT INSERTED IN
#THE FOSS REPORT FOR THIS DEPENDENCY
#FOR NON-MAVEN DEPENDENCIES THE TAG MavenDependency MUST NOT BE SET TO NO

#DEFINE GLOBAL VARIABLES
HOME = os.path.abspath(os.path.join(os.path.dirname(__file__), '..')) +"/"
print("HOME: " + HOME)
REPORT_FILE_TEMPLATE= HOME+"templates/defaultTemplate.yaml"
FILE_LIST_TEMPLATE= HOME+"templates/fileListTemplate.yaml"
SC_REPORT_NAME="Maven_Non-Maven_Report/"
ENVOY_REPORT_NAME="Envoy_Report/"
MVN_FILE="maven-sc.yaml"				#MAVEN DEPENDENCY FILE

fossFilesDir="" 		#SC FOSS FILES DIRECTORY
fossFilesDirEnvoy=""    #Envoy FOSS FILES DIRECTORY
fossFilesDirSC=""    #Envoy FOSS FILES DIRECTORY
nonMvnFile=""			#NON-MAVEN DEPENDENCY FILE
urlsFile=""

repositoryLocations=""
createXlsx=False
createCSV=False

envoyScript=HOME+"scripts/envoy.py"


LicenseObligationText="Check Free and Open Source Software license agreement for license obligations."
licensesDict = {"MIT License": "{{ChoiceOfLicense}}. "+LicenseObligationText,
"Apache License v 2.0": "{{ChoiceOfLicense}}. "+LicenseObligationText,
"Simplified BSD License": "{{ChoiceOfLicense}}. "+LicenseObligationText,
"New BSD, BSD License": "{{ChoiceOfLicense}}. "+LicenseObligationText,
"Eclipse Public License (EPL)": "{{ChoiceOfLicense}}. "+LicenseObligationText,
"Eclipse Public License version 2.0": "{{ChoiceOfLicense}}. "+LicenseObligationText,
"Lesser General Public License v 3.0 (LGPL)": "{{ChoiceOfLicense}}. "+LicenseObligationText,
"LGPL3, GNU Lesser General Public License 3": "{{ChoiceOfLicense}}. "+LicenseObligationText,
"MPL 2.0 / Mozilla Public License v 1.1 (MPL)": "{{ChoiceOfLicense}}. "+LicenseObligationText,
"LGPL2.1, GNU Lesser General Public License 2.1": "{{ChoiceOfLicense}}. "+LicenseObligationText,
"CDDL 1.0": "{{ChoiceOfLicense}}. "+LicenseObligationText,
"CDDL 1.1": "{{ChoiceOfLicense}}. "+LicenseObligationText,
"GNU General Public License with classpath except": "{{ChoiceOfLicense}}. "+LicenseObligationText,
"GPL2 with classpath exception": "{{ChoiceOfLicense}}. "+LicenseObligationText,
"GPL3, GNU General Public License 3.0": "{{ChoiceOfLicense}}. "+LicenseObligationText,
"GPL2, GNU General Public License 2.0": "{{ChoiceOfLicense}}. "+LicenseObligationText}


#PROJECTS FROM THE DEVELOMPMENT DIRECTORY TO GATHER THE DEPENDENCIES
devProjectList=["esc/bsf/bsfwrk",
                "esc/bsf/cddjmxexporter",
                "esc/bsf/bsfmgr",
                "esc/bsf/bsfdiameter",
                "esc/spr",
                "esc/scp/scpmgr",
                "envoyproxy/sds",
                "esc/bragent",
                "esc/hcagent",
                "esc/monitor",
                "esc/scmanager/eric-sc-manager",
                "esc/scmanager/lmconsumer",
                "esc/nlf",
                "esc/rlf",
                "esc/slf",
                "esc/sepp/seppmgr",
                "esc/certnotifier"]

#PROJECTS FROM THE TEST DIRECTORY TO GATHER THE DEPENDENCIES
testProjectList=["jcat-esc/scp-testcases"]

testDevProjectList=["simulators/chfsim",        #THESE ARE PROJECTS FROM DEV ENVIRONMENT USED BY THE TEST PROJECT
                 "simulators/nrfsim"]


def checkArgs():
#CHECKING THE ARGUMENTS
    global fossFilesDirSC, nonMvnFile, urlsFile, createXlsx, repositoryLocations, fossFilesDirEnvoy, createCSV

    parser = ArgumentParser(
        # description='This script generates the FOSS report from a set of FOSS files and from the Maven and Non-Maven dependency lists. '
        #             'Foss files directory, Maven and Non-Maven dependency files must be provided as input to the script. '
        #             'Creation of the maven dependency list can be initiated via -dev for the development '
        #             'project (e.g. 5g_proto) or via -test and -D for the test_ci develoment project.'
        #             'The Specific FOSS Evaluation .xlsx file can be created via the -xlsx option.',
        #             'The Specific FOSS Evaluation .csv file can be created via the -csv option.',

        description='This script generates the FOSS report from a set of FOSS files for the development, test or envoy '
                    'projects depending on the option of the script. The script stored the report and the generated '
                    'xlsx files (if -xlsx is present) into a directory.',
        epilog=textwrap.dedent('''\
                Usage examples: 
                Report.py -f fossFiles/ -dev /local/username/5g_proto -m dev_maven.yaml -n nonMvnFile -xlsx 
                Report.py -f fossFiles/ -m dev_maven.yaml
                
                Report.py -f fossFiles/ -test /local/username/5g_test_ci -D /local/username/5g_proto -m test_maven.yaml -n nonvmn.yaml -xlsx 
                Report.py -f fossFiles/ -m test_maven.yaml
                                
                Report.py -f fossFiles/ -rep /local/username/envoy/bazel/repository_locations.bzl -urls urls.yaml -xlsx
                Report.py -f fossFiles/ -urls urls.yaml '''),
        formatter_class=RawTextHelpFormatter)

    groupEnvoy = parser.add_argument_group('Create dependency file with urls for the envoy project')
    groupEnvoy.add_argument("-rep", dest="rep_path", help="The filename and path of the repository_locations.bzl.")


    parser.add_argument("-fsc", dest="foss_Files_Dir_SC", help="Directory name of the SC FOSS files.", required=False)
    parser.add_argument("-fenv", dest="foss_Files_Dir_Envoy", help="Directory name of the Envoy FOSS files.", required=False)

    parser.add_argument("-xlsx", help="Create Specific FOSS Evaluation .xlsx files from the Report", required=False,action='store_true')
    
    parser.add_argument("-csv", help="Create Specific FOSS Evaluation .csv files from the Report for Mimer", required=False,action='store_true')

    parser.add_argument("-n", dest="non_Mvn_File", help="Non-maven dependency file name.", required=False)


    #ONLY ONE OF THE TREE GROUPS SHOULD EXIST
    # if parser.parse_args().dev_path!=None and \
    #         (parser.parse_args().test_path != None  or parser.parse_args().rep_path != None):
    #     parser.print_help()
    #     sys.exit(1)

    # if parser.parse_args().rep_path != None and \
    #         (parser.parse_args().test_path != None or parser.parse_args().dev_path!=None):
    #     parser.print_help()
    #     sys.exit(1)

    #

    if parser.parse_args().foss_Files_Dir_SC == None and parser.parse_args().foss_Files_Dir_Envoy == None:
        parser.print_help()
        parser.print_usage("At least one of -fsc, -fenv should exist")
        sys.exit(1)

    if parser.parse_args().foss_Files_Dir_SC != None:
        fossFilesDirSC = parser.parse_args().foss_Files_Dir_SC
    
    if parser.parse_args().foss_Files_Dir_Envoy != None:
        fossFilesDirEnvoy = parser.parse_args().foss_Files_Dir_Envoy

    if parser.parse_args().non_Mvn_File != None:
        nonMvnFile = parser.parse_args().non_Mvn_File

    if parser.parse_args().xlsx:
        createXlsx=True
       
    if parser.parse_args().csv:
        createCSV=True   

    if parser.parse_args().rep_path != None:
        repositoryLocations = parser.parse_args().rep_path

#EXECUTE THE MVN DEPENDENCY SCRIPT
# def runMvnScript():
#     try:


#         if not os.path.isfile(mvnDependencyScript):
#             print("The script " + mvnDependencyScript + " is not found. Exiting...")
#             exit(1)


#         executionStr=[]                                 #PREPARE THE EXECUTION STRING FOR CALLING THE MVN.PY SCRIPT
#         executionStr.append(mvnDependencyScript)
#         executionStr.append("-o")
#         executionStr.append(mvnFile)

#         if len(protoDirectory)>0:						#EXECUTING FOR THE DEVELOPMENT PROJECT

#             executionStr.append("-dev")
#             executionStr.append(protoDirectory)

#             for prj in devProjectList:
#                 executionStr.append("-i")
#                 executionStr.append(protoDirectory+"/"+prj)

#             Result = call(executionStr)
#             if Result!=0:
#                 print("")
#                 print("Failed creating MVN dependency file. Exiting...")
#                 print("")
#                 exit(1)

#         elif len(testDirectory)>0:						#EXECUTING FOR THE TEST PROJECT
#             executionStr.append("-test")
#             executionStr.append(testDirectory)
#             executionStr.append("-D")
#             executionStr.append(testDevDirectory)


#             for prj in testProjectList:                 #ADDING TEST PROJECTS
#                 executionStr.append("-i")
#                 executionStr.append(testDirectory+"/"+prj)

#             for prj in testDevProjectList:               #ADDING DEV PROJECTS USED BY THE TEST PROJECT
#                 executionStr.append("-i")
#                 executionStr.append(testDevDirectory+"/"+prj)


#             Result = call(executionStr)
#             if Result!=0:
#                 print("")
#                 print("Failed creating MVN dependency file. Exiting...")
#                 print("")
#                 exit(1)




    # except IOError as e:
    #     print("Could not execute the mvn dependency list generation script :",mvnDependencyScript)
    #     sys.exit(1)

# def runEnvoyScript():
    # try:

    #     if not os.path.isfile(envoyScript):
    #         print("The envoy script " + envoyScript + " is not found. Exiting...")
    #         exit(1)

    #     executionStr=[]                                 #PREPARE THE EXECUTION STRING FOR CALLING THE MVN.PY SCRIPT
    #     executionStr.append(envoyScript)
    #     executionStr.append("-o")
    #     executionStr.append(urlsFile)

    #     if repositoryLocations:						#EXECUTING FOR THE DEVELOPMENT PROJECT

    #         executionStr.append("-r")
    #         executionStr.append(repositoryLocations)


    #         Result = call(executionStr)
    #         if Result!=0:
    #             print("")
    #             print("Failed creating Envoy dependency urls file. Exiting...")
    #             print("")
    #             exit(1)


    # except IOError as e:
    #     print("Could not execute the mvn dependency list generation script :", envoyScript)
    #     sys.exit(1)

def replaceTags(value,fossObj):

    for tag in fossObj:
        if (isinstance(value, (str))):
            if "{{"+tag+"}}.strip()" in value:
                value = value.replace("{{" + tag + "}}.strip()", str(fossObj[tag]).split("/")[1])
                value = value.replace("{{" + tag + "}}", str(fossObj[tag]))

            else:
                value = value.replace("{{" + tag + "}}", str(fossObj[tag]))
                # print(value, tag,fossObj[tag]) ZSTOIOA Ioannis Stoltidis # ChoiceOfLicense # FAL1159004/20 (Apache License 2.0 (Apache-2.0)) 


    return value

def updateBrackets(fossObj):
    #UPDATE THE BRACKETS WITH THE CORRESPONDING VALUES
    for tag in fossObj:
        try:
            #if tag== "MavenDependency":

            if (isinstance(fossObj[tag], (list))):  # EXCLUDING WHEN THE VALUE IS YES/NO
                for i in range(0, len(fossObj[tag])):  # NAVEN DEPENDENCY FIELD HAS IS A LIST
                    fossObj[tag][i]=replaceTags(fossObj[tag][i],fossObj)            #REPLACE THE BRACKET FIELD WITH VALUE

            else: #ALL TAGS EXCEPT MavenDependency
                fossObj[tag] = replaceTags(fossObj[tag], fossObj)

        except IOError as e:
            print("Failed updating the double brackets for " +tag + " in "+fossObj+". Exiting...")
            print(fossObj['FOSSName'], fossObj['FOSSVersion'],fossObj['PRIMNumber(CAX/CTX)'],fossObj['ChoiceOfLicense'])
            print("" % e)
            sys.exit()

    return fossObj

def checkFiles():

    if not os.path.isfile(REPORT_FILE_TEMPLATE):
        print("The file report template" + REPORT_FILE_TEMPLATE + " is not found. Exiting...")
        sys.exit()

    if not os.path.isfile(MVN_FILE) and MVN_FILE:
        print("The file mvn file" + MVN_FILE + " is not found. Exiting...")
        sys.exit()

    if not os.path.isfile(urlsFile) and urlsFile:
        print("The file urls file" + urlsFile + " is not found. Exiting...")
        sys.exit()

    if (not os.path.isfile(nonMvnFile)) and (nonMvnFile):
        print("The file " + nonMvnFile + " is not found. Exiting...")
        sys.exit()

    if not os.path.isfile(FILE_LIST_TEMPLATE):
        print("The file file list template" + FILE_LIST_TEMPLATE + " is not found. Exiting...")
        sys.exit()

def createReport(obj,fileListObj, reportForEnvoy, reportPath):
    

    reportFileName = "ReportFoss.yaml"
    reportFileList = "ReportFileList.yaml"

# COPY FILES USED AS INPUT TO REPORT FOLDER
    if reportForEnvoy:
        if urlsFile:
            copyfile(urlsFile, reportPath + os.path.basename(urlsFile))
    else:    
        if MVN_FILE:
            copyfile(MVN_FILE, reportPath+os.path.basename(MVN_FILE))

        if nonMvnFile:
            copyfile(nonMvnFile, reportPath+os.path.basename(nonMvnFile))

    with open(reportPath+reportFileName, 'w') as ymlfile:             #WRITE TO FOSS REPORT
        ymlfile.write(yaml.dump(obj, default_flow_style=False))
    ymlfile.close()

    with open(reportPath+reportFileList, 'w') as ymlfile:             #WRITE TO FOSS FILE LIST
        ymlfile.write(yaml.dump(fileListObj, default_flow_style=False))
    ymlfile.close()




#MATCH THE ChoiceOfLicense AND RETURN THE CORRESPONDING FulfillmentOfLicenseObligations
def getLicenseObligation(ChoiceOfLicense):

    for license in licensesDict:

        if ChoiceOfLicense==license:
            return licensesDict.get(license)
    return "None"

#CREATING INDEX FOR LIST WITH DEPENDENCY NAME AND FOSS FILE NAME FOR THE FOSSES WITH MAVENDEPENDENCY TAG CONTAINING A LIST
#FOR THE FOSSES WITH MAVENDEPENDENCY SET TO NO THE FOSSURL AND FOSS FILE NAME ARE INSERTED INTO THE INDEX
def createFossIndex(fossFilesDir):
    try:
        index={}
        for fossFile in os.listdir(fossFilesDir):
            with open(fossFilesDir + "/" + fossFile, 'r') as fossFileID:
                fossFileObj = yaml.safe_load(fossFileID)  # READ EACH FOSS FILE INTO A YAML OBJECT
            fossFileID.close()

            fossFileObj['FOSS']=updateBrackets(fossFileObj['FOSS'])

            if (isinstance(fossFileObj['FOSS']['MavenDependency'], (list))):  # EXCLUDING WHEN THE VALUE IS YES/NO
                for fossDependency in fossFileObj['FOSS']['MavenDependency']:
                    index.update({fossDependency: fossFile})
            elif str(fossFileObj['FOSS']['MavenDependency']).lower()== "no" or (not fossFileObj['FOSS']['MavenDependency']):
                index.update({fossFileObj['FOSS']['FOSSName']+":"+str(fossFileObj['FOSS']['FOSSVersion']) : fossFile})
                index.update({fossFileObj['FOSS']['FOSSURL']: fossFile})
            else:

                raise Exception('Failed to create Foss file index. check '+ fossFile+'. Exiting...')

    except IOError as e:
        print("Failed to create Foss file index. Exiting..." % e);
        sys.exit()

    return index

#CHECK FOR EMPTY TAGS WITHING A DICT, THE MavenDependency TAG IS EXCLUDED
def checkFossTags(fossFileObj):
    for tag in fossFileObj:

        if (not str(fossFileObj[tag])) or (str(fossFileObj[tag]) == "None"):
            print("")
            print("The tag ",tag," is empty... Exiting")
            return 1
    return 0

def readFile(fileName):
    try:
        with open(fileName, 'r') as fileToOpen:
            fileObject = yaml.safe_load(fileToOpen)
        fileToOpen.close()
    except IOError as e:
        print("Couldn't open and read file (%s)." % e)
        exit(1)

    return fileObject

def generateReportFileObj(dependencyList, fossFilesIndex, matchedFosslist, fossFilesDir):
    
    dependencyCounter=0
    numberOfMatchedDependencies=0
    undefinedDependencyList=[]		#KEEPING HTE UNDEFINED DEPENDENCIES

    defaultFossFile = readFile(REPORT_FILE_TEMPLATE)

    reportFileObj = readFile(REPORT_FILE_TEMPLATE)
    reportFileObj['FOSS'] = {}				#SETTING AS DICT
    reportFileObj['Undefined'] = []			#SETTING AS LIST

    print("Searching the Dependencies in FOSS files... \n")
    for dependency in dependencyList:  # FOR EVERY MVN DEPENDENCY IN THE DEPENDENCY LIST SEARCH FOR MATCHING
                                        #DEPENDENCIES IN THE FOSS FILES INDEX. IF MATCHED FOUND THEN ADD TO THE FOSS
                                        # REPORT FILE UNDER FOSS TAG ELSE ADD UNDER UNDEFINED TAG.

        dependencyCounter+=1
        sys.stdout.write("                                                                                          \r")
        sys.stdout.write("Dependency %d of %d: %s \r" % (dependencyCounter,len(dependencyList),dependency))
        sys.stdout.flush()

        matched=False				# THE CURRENT DEPENDECY IS NOT YET MATCHED IN DEPENDENCY LIST

        for fossFilesDependency in fossFilesIndex:					#SEARCH THE FOSS FILE INDEX FOR THE CURRENT DEPENDENCY
            fossFileName=fossFilesIndex.get(fossFilesDependency)

            if str(dependency).lower() == str(fossFilesDependency).lower():  # IS THE CURRENT DEPENDENCY IN THE FOSS FILE INDEX
                matched = True
                matchedFossFile = fossFileName
                break

        if matched: 												#THE CURRENT DEPENDENCY WAS FOUND IN THE FOSS FILE INDEX

            numberOfMatchedDependencies+=1			#INCREASE THE COUNTER OF MATCHED DEPENDENCIES

            fossFileExists=False
            for i in matchedFosslist:									#CHECKING IF THIS MATCHED FOSS HAS BEEN ALREADY FOUND
                if i == matchedFossFile:
                    fossFileExists = True

            if 	not fossFileExists:										#FOSS FILENAME IS NOT IN THE MATCHED FOSS LIST HENCE WILL BE INSERTED IN THE REPORT

                matchedFosslist.append(matchedFossFile)					# PUTTING FOSS FILENAME IN THE MATCHED FOSS LIST

                fossFileObj=readFile(fossFilesDir + "/" + matchedFossFile)

                matchedFoss = updateBrackets(fossFileObj['FOSS'])


                #UPDATE THE FOSS REPORT OBJECT WITH THE CURRENT DEPENDECY
                reportFileObj['FOSS'][matchedFoss['PRIMNumber(CAX/CTX)']]=matchedFoss

                #MANUAL EVALUATION FOR THIS FOSS, THE DEFAULT FOSS VALUES WILL NOT BE INSERTED INTO THE REPORT FOR THIS DEPENDENCY
                #ONLY VALUES FROM THE FOSS FILE WILL BE ADDED INTO THE FOSS REPORT FOR THIS DEPENDENCY
                if not (((str(matchedFoss['ManualSpecificFOSSEvaluation']).lower())== "yes") or (matchedFoss['ManualSpecificFOSSEvaluation'])):    # IF IS YES OR TRUE
                    reportFileObj['FOSS'][matchedFoss['PRIMNumber(CAX/CTX)']].update(defaultFossFile['FOSS'])
                    reportFileObj['FOSS'][matchedFoss['PRIMNumber(CAX/CTX)']]['FulfillmentOfLicenseObligations']=getLicenseObligation(reportFileObj['FOSS'][matchedFoss['PRIMNumber(CAX/CTX)']]['ChoiceOfLicense'])

                #UPDATE THE BRACKETS FOR THE CURRENT FOSS
                reportFileObj['FOSS'][matchedFoss['PRIMNumber(CAX/CTX)']]=updateBrackets(reportFileObj['FOSS'][matchedFoss['PRIMNumber(CAX/CTX)']])

        else:														#THE DEPENDENCY WAS NOT FOUND IN FOSS FILES HENCE ADDING AS UNDEFINED IN THE FOSS REPORT
            undefinedExists=False
            for i in undefinedDependencyList:
                if i == dependency:
                    undefinedExists=True
            if not undefinedExists:
                undefinedDependencyList.append(dependency)

                reportFileObj['Undefined'].append(dependency)

    reportFileObj['Description'] = "Foss Report"
    reportFileObj['CreationDate']=datetime.datetime.now().strftime("%Y-%m-%d")+datetime.datetime.now().strftime("_%H:%M")
    
    return reportFileObj, numberOfMatchedDependencies, undefinedDependencyList;

def generateReportLstObj(matchedFosslist, unusedFossFiles):
    
    fileListObj=readFile(FILE_LIST_TEMPLATE)
    fileListObj['UsedFiles'] = []				#SETTING AS DICT
    fileListObj['UnusedFiles'] = []			#SETTING AS LIST

    #PREPARE FILES LIST REPORT TAGS
    fileListObj['Description']="Used and Unused FOSS files for generating the report"
    fileListObj['CreationDate']=datetime.datetime.now().strftime("%Y-%m-%d")+datetime.datetime.now().strftime("_%H:%M")

    fileListObj['UsedFiles']=sorted(matchedFosslist)
    fileListObj['UnusedFiles'] = sorted(unusedFossFiles)

    return fileListObj, matchedFosslist, unusedFossFiles

def createReportFolder(reportType):
    reportDirName = "FossReport" + datetime.datetime.now().strftime("_%Y-%m-%d") + datetime.datetime.now().strftime("_%H:%M")
    
    path = HOME +reportDirName+"/"

    if(os.path.exists(path)):
        print("Report directory already exists: ",path,". Exiting...")
        sys.exit()

    os.mkdir(path)
    
    print("")
    if reportType=="SC" or reportType=="Both":
        innerPath = path+SC_REPORT_NAME
        print("Creating report for SC to path: "+innerPath)
        os.mkdir(innerPath)
    print("")
    if reportType=="Envoy" or reportType=="Both":
        innerPath = path+ENVOY_REPORT_NAME
        print("Creating report for Envoy to path: "+innerPath)
        os.mkdir(innerPath)
    print("")
    
    return path

def main():

    combainedReportObj={}
    repeat=1
    reportOutputMsg=""

    checkArgs()

    if fossFilesDirSC and fossFilesDirEnvoy:
        repeat=2
        reportType="Both"
    elif fossFilesDirSC:
        reportType="SC"
    elif fossFilesDirEnvoy:
        reportType="Envoy"        

    reportFolderPath=createReportFolder(reportType)

    for turn in range(0,repeat):
        matchedFosslist=[]				#KEEPING THE CAX NUMBERS
        fossFilesIndex={}
        unusedFossFiles=[]
        reportFileObj={}
        fileListObj={}

        reportForEnvoy=False

        if turn==1 or (repeat==1 and fossFilesDirEnvoy):        #RUN THE LOOP FOR ENVOY REPORT
            reportForEnvoy=True
            fossFilesDir=HOME + fossFilesDirEnvoy
            reportPath=reportFolderPath+ENVOY_REPORT_NAME
            targetNameReport="Envoy"
            print("Running for Envoy")
        else:                                                   #RUN THE LOOP FOR SC REPORT
            reportPath=reportFolderPath+SC_REPORT_NAME
            fossFilesDir=HOME + fossFilesDirSC
            targetNameReport="Maven/Non-Maven"
            print("Running for SC")


        print("")
        print("Starting the FOSS report script...")
        print("")
        print("Checking Files..... ", end=" ")

        #OPEN AND READ THE FILES INTO YAML OBJECTS
        checkFiles()

        mvnFileObj=""
        if MVN_FILE and not reportForEnvoy:                                     #IF THE MVN DILE IS SET -M
            mvnFileObj=readFile(HOME + MVN_FILE)

        urlsFileObj=""
        if reportForEnvoy:                                    #IF THE URLS FILE IS SET -URLS
            urlsFileObj=envoyDependency(repositoryLocations)

        nonMvnFileObj=""
        if nonMvnFile and not reportForEnvoy:                                  #IF THE NON MVN FILE HAS BEEN SET
            nonMvnFileObj=readFile(HOME + nonMvnFile)

        for fossFile in os.listdir(fossFilesDir):
            fossFileObj=readFile(fossFilesDir + "/" + fossFile)

            if checkFossTags(fossFileObj['FOSS']) == 1 :
                print("")
                print("The FOSS file",fossFile," contains empty tags. Exiting...")
                sys.exit()    

        print("OK")

        #CREATE FOSS FILE INDEX WITH {DEPENDENCY : FILENAME }
        fossFilesIndex=createFossIndex(fossFilesDir)

        # CREATE DEPENDENCY LIST
        dependencyList=[]
        try:
            if mvnFileObj:
                for i in mvnFileObj['Dependency']:
                    dependencyList.append(i)

            if urlsFileObj:
                for i in urlsFileObj['URLS']:
                    dependencyList.append(i)

            if nonMvnFileObj:                                   #IF NONMVN DEPENDENCY FILE WAS SET
                for i in nonMvnFileObj['Dependency']:
                    dependencyList.append(i)

        except IOError as e:
            print("Failed reading the Dependency tag from dependency files. Exiting... " % e)
            sys.exit()

        reportFileObj, numberOfMatchedDependencies, undefinedDependencyList=generateReportFileObj(dependencyList, fossFilesIndex, matchedFosslist, fossFilesDir)

        for fossFile in os.listdir(fossFilesDir):      #GATHER THE FOSS FILES THAT WERE NOT MATCHED TO NONE OF THE DEPENDENCIES
            matched=False
            for matchedFoss in matchedFosslist:
                if matchedFoss == fossFile:
                    matched=True

            if not matched:
                unusedFossFiles.append(fossFile)

        fileListObj, matchedFosslist, unusedFossFiles=generateReportLstObj(matchedFosslist, unusedFossFiles)

        createReport(reportFileObj,fileListObj,reportForEnvoy,reportPath)     #CREATE THE REPORT

        dpath_util.merge(combainedReportObj,reportFileObj, flags=dpath_util.MERGE_ADDITIVE)

        reportOutputMsg=reportOutputMsg+("-" * 100)
        reportOutputMsg=reportOutputMsg+("\nNumber of " + targetNameReport + " Dependencies matched: " + str(numberOfMatchedDependencies))
        reportOutputMsg=reportOutputMsg+("\nNumber of " + targetNameReport + " Undefined dependencies: " + str(len(undefinedDependencyList)))
        reportOutputMsg=reportOutputMsg+("\nNumber of " + targetNameReport + " FOSS products included: " + str(len(matchedFosslist)))
        reportOutputMsg=reportOutputMsg+("\nNumber of " + targetNameReport + " Unused FOSS products: " + str(len(unusedFossFiles)))
        reportOutputMsg=reportOutputMsg+("\n\nFOSS files directory used: " + fossFilesDir)

        if MVN_FILE:
            reportOutputMsg=reportOutputMsg+("\nMaven dependency file used: " + MVN_FILE)
        if nonMvnFile:
            reportOutputMsg=reportOutputMsg+("\nNon-Maven dependency file used: " + nonMvnFile)
        if urlsFile:
            reportOutputMsg=reportOutputMsg+("\nEnvoy urls file used: " + urlsFile)

        reportOutputMsg=reportOutputMsg+"\n"
        reportOutputMsg=reportOutputMsg+("-" * 100)
        reportOutputMsg=reportOutputMsg+("\n " + targetNameReport + " FOSS Report \"" + reportFolderPath+"\" directory.\n")
        reportOutputMsg=reportOutputMsg+("-" * 100)
        reportOutputMsg=reportOutputMsg+"\n"


    print(reportOutputMsg)
    if createXlsx:                              # PROCEED WITH CREATING THE XLSX FILES
        generateXlsx(reportFolderPath, combainedReportObj)
        
    if createCSV:                              # PROCEED WITH CREATING THE CSV FILES
        generateCSV(reportFolderPath, combainedReportObj)   

    exit(0)
main()
