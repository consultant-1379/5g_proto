#!/usr/bin/env python
"""This module increments the version in the Chart.yaml."""
import sys
import argparse
import logging
import yaml
import re
from common_functions import read_yaml, write_doc_to_yaml, step_version

LOG = logging.getLogger(__name__)
logging.basicConfig(format="%(asctime)s %(levelname)s %(message)s", level=logging.INFO)


def main():
    """This is the main function that does all of the work."""
    parser = argparse.ArgumentParser()
    parser.add_argument(
        '--appDir',
        help="""Full path to the application directory
        """,
        required=True
    )
    parser.add_argument(
        '--appChartDir',
        help="""Full path to the application chart directory
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

    # Read current version and increment
    chart_yaml_file = "%s/Chart.yaml" % args.appChartDir
    chart = read_yaml(chart_yaml_file)
    new_version = step_version(chart["version"])

    # Update Chart.yaml
    LOG.info("Updating Chart.yaml with new version: " + chart["version"])
    chart["version"] = new_version
    yaml.dump(chart, sys.stdout, default_flow_style=False)
    if not args.dryRun:
        write_doc_to_yaml(chart, chart_yaml_file)

    # Update values.yml
    values_yaml_file = "%s/values.yaml" % args.appChartDir
    with open(values_yaml_file, 'r+') as values_file:
        values = values_file.read()
        values = re.sub(
            r'soVersion: \d+.\d+.\d+',
            "soVersion: " + new_version,
            values
        )
        
        if not args.dryRun:
            values_file.seek(0)
            values_file.write(values)
            values_file.truncate()

    # Update values.yml
    artifact_properties_file = "%s/artifact.properties" % args.appDir
    with open(artifact_properties_file, 'w+') as artifact_file:
        if not args.dryRun:
            artifact_file.seek(0)
            artifact_file.write("SO_VERSION=%s" % new_version)
            artifact_file.truncate()


if __name__ == "__main__":
    main()
