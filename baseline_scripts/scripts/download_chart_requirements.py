#!/usr/bin/env python
"""This module downloads all of the charts requirements locally."""
import argparse
import logging
from common_functions import run_cmd, read_yaml
import subprocess

LOG = logging.getLogger(__name__)
logging.basicConfig(format="%(asctime)s %(levelname)s %(message)s", level=logging.INFO)


def main():
    """This is the main function that does all of the work."""
    parser = argparse.ArgumentParser()
    parser.add_argument(
        '--appChartDir',
        help="""Full path to the application chart directory
        """,
        required=True
    )
    parser.add_argument(
        '--helmCommand',
        help="""The helm command to use
        """,
        required=True
    )
    parser.add_argument(
        '--dryRun',
        help="""Do a dry run of the commands
        """,
        action="store_true",
        required=False
    )
    args = parser.parse_args()

    requirements_yaml_file = "%s/requirements.yaml" % args.appChartDir
    requirements = read_yaml(requirements_yaml_file)

    # Add dependecy repos
    for dependency in requirements['dependencies']:
        LOG.info(dependency)
	username = subprocess.check_output("python ../../scripts/helm_repositories.py -r %s -u" % dependency['repository'], shell=True) 
	password = subprocess.check_output("python ../../scripts/helm_repositories.py -r %s -p" % dependency['repository'], shell=True)
        run_cmd(args.appChartDir, "%s repo add %s %s --username %s --password %s" % (args.helmCommand, dependency['name'], dependency['repository'], username, password), args.dryRun)

    run_cmd(args.appChartDir, "%s dependency update %s" % (args.helmCommand, args.appChartDir), args.dryRun)


if __name__ == "__main__":
    main()
