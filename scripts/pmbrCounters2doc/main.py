import argparse
import json
import logging
import os
import sys

from MdCreator import create_md
from csvJobCreator import create_csv_job
from csvMetricsCreator import create_csv_metrics

supported_types = ["sepp", "scp", "bsf", "rlf", "nlf", "slf"]

LOG = logging.getLogger(__name__)
logging.basicConfig(format="%(asctime)s %(levelname)s %(message)s", level=logging.INFO)


def exit_and_fail(msg):
    """This function logs the given error, and then exits with a 1 exit code."""
    if msg:
        LOG.error(msg)
    sys.exit(1)


def read_json(file_name, component_name):
    """This function reads the given JSON pmbr file and returns it as an object."""
    doc = None
    if not os.path.exists(file_name):
        exit_and_fail("Could not find %s" % file_name)
    with open(file_name, 'r') as stream:
        try:
            doc = json.load(stream)
            for el in doc:
                el['component-name'] = component_name
        except json.JSONDecodeError as exc:
            LOG.error("Error while parsing JSON file " + file_name + "\n" + str(exc))
    return doc


def read_json_data_from_dirs(dir_names, file_name):
    data = []
    for pmbr_dir, name in zip(dir_names, supported_types):
        if not pmbr_dir:
            continue
        file = os.path.join(pmbr_dir, file_name)
        if os.path.isfile(file):
            LOG.info("Processing file: " + file)
            data += read_json(file, name)
        else:
            LOG.error(f'Path: {file} does not exist')
    return data


def main():
    LOG.info("Starting the script")
    parser = argparse.ArgumentParser()
    parser.add_argument(
        '--format',
        help="Output strategy: md, csv, all",
        required=True)
    parser.add_argument(
        '--sepp',
        help="sepp pmbr dir",
        required=False)
    parser.add_argument(
        '--scp',
        help="scp pmbr dir",
        required=False)
    parser.add_argument(
        '--bsf',
        help="bsf pmbr dir",
        required=False)
    parser.add_argument(
        '--rlf',
        help="rlf pmbr dir",
        required=False)
    parser.add_argument(
        '--nlf',
        help="nlf pmbr dir",
        required=False)
    parser.add_argument(
        '--slf',
        help="slf pmbr dir",
        required=False)
    parser.add_argument("-n",
                        "--num",
                        help="The document number of the generated document",
                        default='999/190 84-AXB 250 19')
    parser.add_argument("-r",
                        "--rev",
                        help="The revision of the generated document",
                        default='PA99')
    args = parser.parse_args()

    if args.format == 'all' or args.format == '':
        formats = ["markdown", "csv-metrics", "csv-jobs"]
    elif args.format == 'markdown':
        formats = ["markdown"]
    elif args.format == 'csv-metrics':
        formats = ["csv-metrics"]
    elif args.format == 'csv-jobs':
        formats = ["csv-jobs"]
    elif args.format == 'csv-both':
        formats = ["csv-metrics", "csv-jobs"]
    else:
        formats = []

    pmbr_configs_dirs = [args.sepp, args.scp, args.bsf, args.rlf, args.nlf, args.slf] # Update supported_types!
    group_json_data = read_json_data_from_dirs(pmbr_configs_dirs, "group.json")
    job_json_data = read_json_data_from_dirs(pmbr_configs_dirs, "job.json")

    if "markdown" in formats:
        create_md(group_json_data, job_json_data, args)
    if "csv-metrics" in formats:
        create_csv_metrics(group_json_data)
    if "csv-jobs" in formats:
        create_csv_job(job_json_data)


if __name__ == "__main__":
    print(os.getcwd())  # Prints the current working directory
    main()
