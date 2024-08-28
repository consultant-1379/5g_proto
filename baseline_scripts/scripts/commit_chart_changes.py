#!/usr/bin/env python
"""This module commits changes made to the helm chart, back to git with a new version tag."""
import argparse

from common_functions import run_cmd, read_yaml


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
        '--dryRun',
        help="""Do a dry run of the commands
        """,
        action="store_true",
        required=False
    )
    args = parser.parse_args()

    # Initialize variables
    chart_yaml_path = "%s/Chart.yaml" % args.appChartDir

    # Read Chart.yaml
    chart = read_yaml(chart_yaml_path)

    # Push back to git
    run_cmd(args.appChartDir, "git add %s" % (args.appChartDir), args.dryRun)
    run_cmd(args.appChartDir, "git commit -m '[adp-auto] Automatic new version %s'" % (chart["version"]), args.dryRun)
    run_cmd(args.appChartDir, "git push origin HEAD:master", args.dryRun)
    run_cmd(args.appChartDir, "git tag -a %s -m 'Automatic new version %s'" % (chart["version"], chart["version"]), args.dryRun)


if __name__ == "__main__":
    main()
