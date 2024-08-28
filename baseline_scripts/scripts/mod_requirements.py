#!/usr/bin/env python
"""This module updates a given chart name, repo, version in the charts requirements.yaml."""
import sys
import argparse
import logging
import yaml
from common_functions import read_yaml, write_doc_to_yaml, modify_requirements

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
        '--name',
        help="""The name of the updated chart.
        """,
        required=True
    )
    args = parser.parse_args()

    # Initialize variables
    requirements_yaml_file = "%s/requirements.yaml" % args.appChartDir
    requirements = read_yaml(requirements_yaml_file)
    # LOG.info(requirements["dependencies"])

    # Update requirements.yaml
    LOG.info("Removing " + args.name + " from requirements.yaml with ")
    requirements = modify_requirements(requirements, args.name)
    yaml.dump(requirements, sys.stdout, default_flow_style=False)
    write_doc_to_yaml(requirements, requirements_yaml_file)


if __name__ == "__main__":
    main()
