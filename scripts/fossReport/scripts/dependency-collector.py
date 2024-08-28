#!/usr/bin/env python3

from os import chdir, getcwd
import os
from re import search, sub
from subprocess import call
from datetime import datetime
from argparse import ArgumentParser
from argparse import RawTextHelpFormatter
import sys
import textwrap
import shutil

#PROJECTS FROM THE DEVELOMPMENT DIRECTORY TO GATHER THE DEPENDENCIES
DEV_PROJECT_LIST=["/esc/bsf/bsfwrk",
                "/esc/bsf/cddjmxexporter",
                "/esc/bsf/bsfmgr",
                "/esc/bsf/bsfdiameter",
                "/esc/scp/scpmgr",
                "/envoyproxy/sds",
                "/esc/monitor",
                "/esc/nlf",
                "/esc/rlf",
                "/esc/slf",
                "/esc/nlf",
                "/esc/sepp/seppmgr",
                "/esc/certnotifier",
                "/esc/eric-sc-tapcollector",
                "/esc/eric-sc-tlskeylogagent",
                "/fm-lib"]

# Utility functions.
_valid_dependency = lambda x: True if search("(.*\.)+(.*:)+.*", x) else False
_exclude_dependency = lambda x: True if search(":test", x) or search(":runtime", x) or search("com.ericsson", x) or search("se.ericsson", x) or search("netconf.rfc4741", x) else False
_remove_trail = lambda x: sub(":compile.*$", "", x) 

# Set of distinct valid dependencies.
dependencies = set()


def _sanitize(dependency):
  """
  Sanitize the given dependency:
  1. Strip any leading or trailing whitespaces
  2. Exclude the dependency, if it is invalid or has :test or :runtime
  3. Remove the dependency class (e.g :compile)
  """
  if _valid_dependency(dependency):  
    dependency = dependency.strip()
    if _exclude_dependency(dependency):
      return None
    else:    
      return _remove_trail(dependency)


def _append_list(fileName):
  """
  Get a filename of a file with maven dependencies.
  Sanitize each dependency and if it is a valid one
  add it to the Set structure that ensures no 
  duplication.
  """
  file = open(fileName,"r")
  for line in file:
    dependency = _sanitize(line)
    if dependency:
      dependencies.add(dependency)
  file.close()

###### Deprigated ##########
def _maven_install_dev(devDir):
  """
  Cleans maven project and maven installs all
  required dependencies for development.
  """
  chdir(devDir)
  print("Running MVN clean")
  Result=call(["mvn", "clean"])
  if Result !=0:
    sys.exit(1)

  print("Installing MVN for all projects...")

  maven_targets = [str(devDir)] 

  for target in maven_targets:
    chdir(target)
    print("Running MVN install in " + target)
    Result=call(["mvn", "install"])
    if Result !=0:
      sys.exit(1)

def _maven_install_test(testDir: object) -> object:
  """
  Cleans maven project and maven installs all
  required dependencies for test.
  """
  chdir(testDir + "/jcat-esc")
  print("Running MVN clean")
  Result=call(["mvn", "clean"])
  if Result !=0:
    sys.exit(1)

  print("Installing MVN for all projects...")

  maven_targets = [str(testDir) + "/jcat-esc"] 

  for target in maven_targets:
    chdir(target)
    print("Running MVN install in " + target)
    Result=call(["mvn", "install", "-DskipTests"])
    if Result !=0:
      sys.exit(1)
############################

def main():

  parser = ArgumentParser(description='Given a list of pom.xml files a maven dependency list is produced.',
                          epilog=textwrap.dedent('''\
                                      Usage examples: 
                                      mvn.py -dev /local/username/5g_proto -o mvn.yaml -i /local/username/5g_proto/esc/bsf/bsfmgr
                                      mvn.py -test /local/username/5g_test_ci -D /local/username/5g_proto -o mvn.yaml -i /local/username/5g_test_ci/jcat-esc/scp-testcases '''),
                          formatter_class=RawTextHelpFormatter)

  # groupA=parser.add_mutually_exclusive_group()
  groupA=parser.add_argument_group('groupA')

  groupA.add_argument("-dev", dest="devPath", help="Directory for the 5g_proto project", required=True)

  groupB=parser.add_argument_group('groupB')
  groupB.add_argument("-test", dest="testPath",help="Directory for the 5g_test_ci project")
  groupB.add_argument("-D", dest="D",help="Directory for the 5g_proto project")


  parser.add_argument("-i", "--input", dest="input", action="append",
                      help="Directory to search for a pom.xml file and extract dependencies. Multiple instances of -i may be present.", required=False)
  parser.add_argument("-o", "--output", dest="out",
                      help="The output file with the maven dependencies", required=False)

  args = parser.parse_args()


  # Read the arguments.
  devDir = args.devPath

  chdir("../")
  home = getcwd()
  # Create target pom path list
  pomPathList=[]
  for pomPath in DEV_PROJECT_LIST:
    pomPathList.append(devDir+pomPath)

  # Set output dir
  outputDir = home+"/outputDir"
  if os.path.exists(outputDir):
    print("Folder 'outputDir' Exists! Deleting....")
    shutil.rmtree(outputDir)
  os.mkdir(outputDir)

  # Collect maven dependencies.
  print("Collecting maven dependencies.")
  i = 1
  for pom in pomPathList:
    chdir(pom)
    shutil.copyfile('target/resolved-dependencies.txt', outputDir + "/" + str(i) + ".txt")
    i += 1

  # chdir(outputDir)
  print("All maven dependencies collected!")

  # Process the maven dependency lists.
  i = 1
  for pom in pomPathList:
    _append_list(outputDir + "/" + str(i) + ".txt")
    i += 1

  # Get current date-time for the final output file.
  date = datetime.now()
  date_str = str(date.year) + "-" + str(date.month) + "-" + str(date.day) + " " + str(date.hour) + ":" + str(date.minute)

  # Create the final report.
  reportPath = home+"/maven-sc.yaml"
  print("Reporting in: " + reportPath)
  output = open(reportPath, "w")
  output.write("Version: v1\n")
  output.write("Description: List of dependencies\n")
  output.write("LastUpdate: " + date_str + "\n")
  output.write("Dependency:\n")

  # Add all valid dependencies.
  for dependency in sorted(dependencies):
    output.write("  - " + dependency + "\n")

  output.close()

  shutil.rmtree(outputDir)

main()
