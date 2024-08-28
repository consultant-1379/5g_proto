#!/usr/bin/env python
import argparse
import yaml
import sys
from common_functions import read_yaml, write_doc_to_yaml

#path = path of the script 
REPO = "https://armdocker.rnd.ericsson.se/artifactory/proj-5g-bsf-helm/"

def create_new_file(dir, filename):
	with open(filename, 'w') as outfile:
		yaml.dump(dir, outfile, default_flow_style=False)
		print("Successfully wrote content to file "+filename)


def main():
	parser = argparse.ArgumentParser()
	parser.add_argument(
		'--oursFile',
		help="""Adp-lift version of requirements.yaml.
		""",
		required=True
	)
	parser.add_argument(
		'--theirsFile',
		help="""Master version of requirements.yaml.
		""",
		required=True
	)
	args = parser.parse_args()

	req_file_lift = args.oursFile
	req_file_mast = args.theirsFile
	
	dataM = read_yaml(req_file_mast)
	dataL = read_yaml(req_file_lift)
	
	i = 0
	mergeFile = dataM

	for serv in dataL["dependencies"]:
		if serv["repository"] != REPO:
			print(mergeFile["dependencies"][i]["name"]+' -----> ' +mergeFile["dependencies"][i]["version"])
			mergeFile["dependencies"][i]["version"] = serv["version"]
			print(mergeFile["dependencies"][i]["name"]+' -----> ' +mergeFile["dependencies"][i]["version"])
		i = i + 1

	create_new_file(mergeFile, 'requirements.yaml')



if __name__ == "__main__":
	main()
