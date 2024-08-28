#!/usr/bin/env python3
import yaml
import argparse
from os.path import expanduser
import os
import requests
import getpass
import json
import subprocess

repositories = ['https://arm.rnd.ki.sw.ericsson.se/artifactory/','https://arm.epk.ericsson.se/artifactory/','https://armdocker.rnd.ericsson.se/artifactory/','https://arm.sero.gic.ericsson.se/artifactory/','https://arm.seli.gic.ericsson.se/artifactory/']

def write_to_file(credentials, filename,dirName):
    if not os.path.exists(dirName):
        os.mkdir(dirName)
        print("Directory " , dirName ,  " Created ")
    
    with open(filename, 'w') as outfile:
        yaml.dump(credentials, outfile, default_flow_style=False)
        print("Successfully wrote content to file "+filename)

def retrieve_api_keys(username,password):
    repos = []
    for element in repositories:
            token = requests.get(element+'api/security/apiKey', auth=(username, password))
            result = json.loads(token.content)
            if token.status_code == 200:
                key = result['apiKey'].encode('ascii')
                key = str(key, 'utf-8')
                repo = {'url':element, 'username':username, 'password':key}
                repos.append(repo)
            else:
                print(token.text)
                return
    helm_repositories = {'repositories':repos}
    print(helm_repositories)
    return helm_repositories

def modify_url(url):
    if "https://" in url or "http://" in url:
        url = url[url.index("://")+3:]
    modified_url = url.split("/")[0]
    return modified_url

def get_repo_cred(url,filename):
    url = modify_url(url)
    with open(filename, 'r') as stream:
        try:
            repos = yaml.safe_load(stream)
        except yaml.YAMLError as exc:
            print(exc)
    if repos is None:
        sys.exit()
    for repo in repos.get("repositories"):
        if str(url) in repo.get('url'):
            return repo.get('username'),repo.get('password')
    return None, None

def parse_args():
    parser = argparse.ArgumentParser(
        description='Tool for getting secrets when using HELM installation and upgrade')
    parser.add_argument('-r', '--repository',
                        dest='url',
                        type=str, required=False,
                        metavar="REPOSITORY_URL",
                        help="Enter URL for Repository to use")
    parser.add_argument('-c','--createFile', action='store_true',
                        help="Creates file with Tokens")
    parser.add_argument('-u','--username', action='store_true',
                        help="Returns username for URL, only -u or -p possible at a time")
    parser.add_argument('-p', '--password',action='store_true',
                        help="Returns password for URL, only -u or -p possible at a time")
    parser.add_argument('-eu', '--enter-user',dest='enterUsername',
                        type=str, required=False,
                        metavar="ENTER_USERNAME",
                        help="Enter username to retrieve tokens")
    parser.add_argument('-ep', '--enter-password',dest='enterPassword',
                        type=str, required=False,
                        metavar="ENTER_PASSWORD",
                        help="Enter file where password to retrieve tokens is stored")
    args = parser.parse_args()

    return args

def main(args):
    args = parse_args()
    home = "/files"
    filename = home+"/.artifactory/helm_repositories.yaml"
    dirName = home+"/.artifactory"
    if args.createFile:
        if not args.enterPassword:
            password = getpass.getpass(prompt='Enter Artifactory password: ')
        else:
            password = open(args.enterPassword, "r").readline()
            password = password.strip()
        if not args.enterUsername:
            username = os.environ['USER']
        else:
            username = args.enterUsername
        credentials = retrieve_api_keys(username, password)
        if credentials:
            write_to_file(credentials,filename,dirName)
    if not os.path.exists(dirName):
        filename = expanduser("~")+"/.artifactory/helm_repositories.yaml"
    if args.username or args.password:
        user,password = get_repo_cred(args.url,filename)
    if args.username:
        print(user)
        return user
    if args.password:
        print(password)
        return password

if __name__ == "__main__":
    args = parse_args()
    main(args)
