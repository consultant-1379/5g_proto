#!/usr/bin/env python3


import yaml
from re import search, sub
import re

from argparse import ArgumentParser
from argparse import RawTextHelpFormatter
import sys
import datetime

# _exclude_dependency = lambda x: True if search(":test$", x) or search(":runtime$", x) or search("com.ericsson", x) or search("se.ericsson", x) or search("netconf.rfc4741", x) else False
_is_urls = lambda x: True if search("urls =", x) else False
_remove_trail = lambda x: sub(":a-zA-Z", "", x)

outputFile = "urls.yaml"


def dumpYaml(list, file):
    obj = {}
    obj['Description'] = "List of URLS for the envoy dependencies"
    obj['CreationDate'] = datetime.datetime.now().strftime("%Y-%m-%d") + datetime.datetime.now().strftime("_%H:%M")

    obj['URLS'] = list
    try:

        with open(file, 'w') as ymlfile:
            ymlfile.write(yaml.dump(obj, default_flow_style=False))
        ymlfile.close()

    except IOError as e:
        print("Couldn't open and write to the file " % e, file);
        sys.exit(1)

    return obj


def envoyDependency(repositoryLocFile):
    urslList = []
    version = ""
    protobuf_version = ""
    bracketOpened = False
    urls = ""
    excluded_use_categories = ["build", "test_only"]
    repoFile = open(repositoryLocFile, "r")

    for line in repoFile:

        if not line.lstrip().startswith('#'):  # IGNORE BAZEL COMMENTED INPUT

            if search("PROTOBUF_VERSION =", line):  # FIND THE LINE WITH THE PROTOBUF_VERSION STRING IN IT
                protobuf_version = re.findall(r'"(.*?)"', line)[0]  # GET THE TEXT BETWEEN THE DOUBLE QUOTES

            if search("version =", line) and not search("version = PROTOBUF_VERSION",
                                                        line):  # FIND THE LINE WITH THE VERSION STRING IN IT
                version = re.findall(r'"(.*?)"', line)[0]  # GET THE TEXT BETWEEN THE DOUBLE QUOTES

            if search("version = PROTOBUF_VERSION", line):  # FIND THE LINE WITH THE VERSION STRING IN IT
                version = protobuf_version  # GET THE TEXT BETWEEN THE DOUBLE QUOTES

            if search("urls =", line) or search("urls=", line):  # FIND THE LINE WITH THE URLS STRING IN IT
                bracketOpened = True

            if bracketOpened:
                urls = urls + line
                if search("]", line):
                    bracketOpened = False
                    urls = urls.strip()
                    # STRIP THE STRING
                    urls = re.sub('{version}', version, str(urls))  # replace '{version}' with version
                    urls = re.sub('{underscore_version}', re.sub("[.]", "_", version),
                                  str(urls))  # replace '{underscore_version}' with version und replace dots with underscores

                    urls = re.findall(r'"(.*?)"', str(urls))  # GET THE TEXT BETWEEN THE DOUBLE QUOTES

                    version = ""

            if search("use_category =", line):  # FIND THE USE CATEGORY
                use_categories = re.findall(r'"(.*?)"', line)  # GET THE TEXT BETWEEN THE DOUBLE QUOTES
                print("checking use_categories =", use_categories);
                if not "eric_sc_excluded" in use_categories:
                    for use_category in use_categories:
                        if use_category not in excluded_use_categories:
                            for s in urls:  # ADD TO THE URLS LIST
                                urslList.append(s)
                                print("added url:", s);
                            break
                urls = "";
                version = ""
                bracketOpened = False

    urlsObj = dumpYaml(urslList, outputFile)
    print("")
    print("Yaml file with URLs list is created:", outputFile)

    return urlsObj
