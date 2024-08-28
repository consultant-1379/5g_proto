#!/usr/bin/env python

__author__ = "eaoknkr"

"""This module updates a given chart name, repo, version in the charts requirements.yaml."""
import sys
import argparse
import logging
import urllib2, base64
import re
import random
import yaml
import subprocess
import os
import csv
from getpass import getpass
from common_functions import read_yaml, write_doc_to_yaml, update_requirements, exit_and_fail
from itertools import imap
import collections
import datetime

##### INITIALIZATIONS #####

LOG = logging.getLogger(__name__)
logging.basicConfig(format="%(asctime)s %(levelname)s %(message)s", level=logging.INFO)

excluded_dependencies = ["sc", "scp", "bsf", "sepp", "csa"]


PREL_pattern = r"\d*\.\d*\.\d*\-\d*"
PRA_pattern = r"\d*\.\d*\.\d*\+\d*"
PREL_printout = r"%d.%d.%d-%d"
PRA_printout = r"%d.%d.%d+%d"

class Bcolors:
    HEADER = '\033[95m'
    OKBLUE = '\033[94m'
    OKGREEN = '\033[92m'
    WARNING = '\033[93m'
    FAIL = '\033[91m'
    ENDC = '\033[0m'
    BOLD = '\033[1m'
    UNDERLINE = '\033[4m'

loading_messages = [
    'We\'re testing your patience...',
    'As if you had any other choice...',
    'Follow the white rabbit...',
    'Why don\'t you order a sandwich?...',
    'While the satellite moves into position...',
    'Spinning the wheel of fortune...',
    'Granting wishes...',
    'Time flies when you\'re having fun...',
    'Convincing AI not to turn evil...',
    'Do not run! We are your friends!...',
    'Do you come here often?...',
    'Warning: Don\'t set yourself on fire...',
    'We\'re making you a cookie...',
    'Just count to 10...',
    'Why so serious?...',
    'Counting backwards from Infinity...'   
]

table_headers = [
    "Service Name",
    "Current version",
    "Latest PRA",
    "Latest PREL",
    "Upgrade Status"
]

######################################################

def normalize(v, pattern):
    """
    Converts a version string into an array of integers 
    """
    res = re.search(pattern, v).group()
    if res is not None:
        res = re.split("\.|\-|\+", res)
        res = [int(i) for i in res if i is not ""]
    return res

def get_latest_version(versions, pattern, printout):
    """
    Filters a list of versions based on the given pattern (PRA or PREL)
    and returns the a list of strings and a sorted list of integers that match the pattern. If there are
    no matches then \"-\" is returned
    """

    exp = re.compile(pattern)
    requested_versions = filter(exp.search, versions)
    
    requested_versions_int = []

    for v in requested_versions:
        requested_versions_int.append(normalize(v, pattern))

    if len(requested_versions_int) > 0:
        requested_versions_int.sort()
        return printout % tuple(requested_versions_int[-1]), requested_versions_int[-1]
    else:
        return "-", requested_versions_int

def is_upgradable(current_version, version):
    """
    Checks whether current_version is different than version
    """
    if cmp(current_version, version) == -1:
        return "True"
    return "False"

def get_available_versions(url, username=None, password=None):
    """
    Connects to the given remote repository, and fetches all of the available versions
    """
    try:
        request = urllib2.Request(url)
        if username and password is not None:
            base64string = base64.b64encode('%s:%s' % (username, password))
            request.add_header("Authorization", "Basic %s" % base64string)
        else:
            excluded_dependencies.append("cnom")

        html = urllib2.urlopen(request).read()
        versions = re.findall("(>)(.*.tgz)(<)", html, flags=re.MULTILINE)
        return [str(v[1]) for v in versions]

    except (ValueError, urllib2.URLError) as e:
        print("Failed to fetch version from " + url)
        exit_and_fail(e)

def create_dict_entry(current_version=None, pra=None, prel=None, upgradable=None, other_v=None):
    """
    Creates a dictionary entry for the given information
    """
    ds = collections.OrderedDict()
    ds["current_version"] = current_version if current_version is not None else "-"
    ds["pra"] = pra if pra is not None else "-"
    ds["prel"] = prel if prel is not None else "-"
    ds["upgradable"] = upgradable if upgradable is not None else "-"
    ds["other_v"] = other_v if other_v is not None else "-"

    return ds

def check_exec_path(path="5g_proto"):
    """
    Checks whether the directory path where the script is executed is ~/5g_proto/esc.
    """
    current_path=os.getcwd()
    if path in current_path:
        return True,current_path
    else:
        return False,current_path

def check_git_repo(git_repo=".git"):
    """
    Checks whether the directory where the script is executed is a git repo.
    If it is a git repo, then checks whether it is the specified one
    """
    try:
        output = subprocess.check_output(["git", "rev-parse", "--git-dir"])
        if git_repo in output:
            return True
    except subprocess.CalledProcessError as err:
        print("GIT is not accessible/n" + err)
        return False

def check_upgrade_status(current_version, last_pra, last_prel):
    """
    """
    if current_version < last_pra:
        return "PRA"
    elif current_version < last_prel:
        return "PREL"
    else:
        return "-"
    
def parse_arguments():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        '-a', '--appChartDir',
        help="""Full path to the application chart directory
        """,
        required=True
    )
    parser.add_argument(
        '-c', '--cnomCheck',
        help="""Include cnom check. This will require your username and password
        """,
        action='store_true',
        required=False
    )
    parser.add_argument(
        '-b', '--branch',
        help="""Print out a column with the versions of the specified branch""",
        required=False,
    )
    parser.add_argument(
        '-e', '--export',
        help="""Exports the output in a CSV file. This will require the path and the name of the file.""",
        required=False,
    ) 
    parser.add_argument(
        '-u', '--update',
        help="""Include update,commit,push of requirements file.""",
        action='store_true',
        required=False,
    ) 
    return parser.parse_args()
    
def main():
    """This is the main function that does all of the work."""

    args = parse_arguments()

    (result,cpath) = check_exec_path()
    if not result:
        print("Executed in path " + cpath + " while expect to be executed from path inside 5g_proto git repo.")
        exit(1)
    
    if not check_git_repo():
        print("Problem occurred while trying to access git for branch: " + args.branch)
        exit(1)

    requirements_yaml_file = "%s/requirements.yaml" % args.appChartDir
    requirements = read_yaml(requirements_yaml_file)

    username = None
    password = None

    if args.cnomCheck:
        username = raw_input("Username: ")
        password = getpass()

    print("\n" + random.choice(loading_messages) + "\n")
    current_dt = datetime.datetime.now()
    d = collect_reqs(username, password, requirements)
    
    if args.update:
        old_requirements = requirements
        if update_versions(requirements, username, password):
            export_requirements(old_requirements, requirements_yaml_file + "_OLD")
            export_requirements(requirements, requirements_yaml_file)
            git_commit("Automatic upgrade of adp services")
            git_push()
        else:
            print("There are no new ADP service versions to upgrade!")
    
    if args.branch is not None:
        table_headers.append(args.branch) 
        d = compare_branch(args.branch, d)

    sorted_d = sorted(d.items())
    pretty_print(table_headers, sorted_d)
    
    if args.export:
        export_csv(table_headers, sorted_d, args.export)
    
    #2020-11-18T15:35:27.6666666Z
    print("\nCurrent printout generated: " + current_dt.strftime("%Y-%m-%dT%H:%M:%S.%f") + "\n")
    
def git_commit(message):
    try:
        output = subprocess.check_output(["git", "commit", "-a", "-m", message])
    except subprocess.CallProcessError as err:
        print("Could not commit changes to GIT." + err)
        exit(1)
    if "1 file changed" not in output:
        print("Error during GIT commit of changes\n" + output)
        exit(1)
    print("ADP services upgrades changes committed successfully.\n" + output)
    
def git_push():
    try:
        output = subprocess.check_output(["git", "push"])
    except subprocess.CallProcessError as err:
        print("Could not push changes to GIT." + err)
        exit(1)
    print("ADP services upgrades changes pushed successfully.\n" + output)
        
def compare_branch(branch, collected_reqs):
    try:
        output = subprocess.check_output(["git", "show", "origin/" + branch + ":esc/helm/eric-sc-umbrella/requirements.yaml"])
    except subprocess.CalledProcessError as err:
        print("GIT is not accessible/n" + err)
        exit(1)
        
    branch_reqs = yaml.safe_load(output)
    for dependency in branch_reqs["dependencies"]:
        if all(e not in dependency["name"] for e in excluded_dependencies):
            other_version = dependency["version"]
            service = dependency["name"]
            
            if service in collected_reqs:
                collected_reqs[service]["other_v"] = other_version
            else:
                collected_reqs[service] = create_dict_entry(other_v=other_version)
    return collected_reqs
    
def collect_reqs(username, password, requirements):
    d = collections.OrderedDict()
    for dependency in requirements["dependencies"]:
        if all(e not in dependency["name"] for e in excluded_dependencies):
            versions_normilized = normalize(dependency["version"], PRA_pattern if "+" in dependency["version"] else PREL_pattern)
            versions = get_available_versions(dependency["repository"] + dependency["name"], username, password)
            pra, pra_int = get_latest_version(versions, PRA_pattern, PRA_printout)
            prel, prel_int = get_latest_version(versions, PREL_pattern, PREL_printout)
            d[dependency["name"]] = create_dict_entry(current_version=dependency["version"], pra=pra, prel=prel, upgradable=check_upgrade_status(versions_normilized, pra_int, prel_int))
    return d

def update_versions(requirements, username, password):
    new_versions = None
    for dependency in requirements["dependencies"]:
        if all(e not in dependency["name"] for e in excluded_dependencies):
            versions = get_available_versions(dependency["repository"] + dependency["name"], username, password)
            pra, pra_int = get_latest_version(versions, PRA_pattern, PRA_printout)
            prel, prel_int = get_latest_version(versions, PREL_pattern, PREL_printout)
            if "cnom" in dependency["name"]:
                if dependency["version"] not in prel:
                    print("Upgrading adp service " + dependency["name"] + " from " + dependency["version"] + " to " + prel + ".")
                    dependency["version"] = prel
                    new_versions = True
            else:
                if dependency["version"] not in pra:
                    print("Upgrading adp service " + dependency["name"] + " from " + dependency["version"] + " to " + pra + ".")
                    dependency["version"] = pra
                    new_versions = True
    return new_versions
    
def export_requirements(data, path_name):
    with open(path_name, 'w') as file:
        yaml.dump(data, file, default_flow_style=False)
    
def export_csv(headers, data, path_name):
    with open(path_name, 'w') as csvfile:
        writer = csv.writer(csvfile, delimiter=",")
        writer.writerow(headers)
        for service, details in data:
            newlist = []
            newlist.append(service)
             
            for detail in details.values():
                newlist.append(detail)    
            writer.writerow(newlist)
    
def pretty_print(headers, data):
    if len(headers) == 6:
        format_row = "{:<40} {:<20} {:<15} {:<15} {:<15} {:<15}"
        pra_format_row = "{:<40} {:<20} {:<15} {:<15} \033[92m{:<15}\033[00m {:<15}"
        header_format_row = Bcolors.HEADER + Bcolors.BOLD + "{:<40} {:<20} {:<15} {:<15} {:<15} {:<15}" + Bcolors.ENDC
    else:
        format_row = "{:<40} {:<20} {:<15} {:<15} {:<15}"
        pra_format_row = "{:<40} {:<20} {:<15} {:<15} \033[92m{:<15}\033[00m"
        header_format_row = Bcolors.HEADER + Bcolors.BOLD + "{:<40} {:<20} {:<15} {:<15} {:<15}" + Bcolors.ENDC

    underline=[]
    for i in headers:
        underline.append("-" * len(i))
    print(header_format_row.format(*headers))
    print(format_row.format(*underline))
    
    for service, details in data:
        if details["upgradable"] is "PRA":
            print(pra_format_row.format(service, *details.values()))
        else:
            print(format_row.format(service, *details.values()))

if __name__ == "__main__":
    main()