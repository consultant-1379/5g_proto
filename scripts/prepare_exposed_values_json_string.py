#!/usr/bin/env python

"""
----------------------------------------------------------------------------
=============Tool to convert a yaml file into a json file===================
This tool takes as input parameter a yaml file and a json file. The json
file is the file that will be produced after the conversion and processing
of the data of the yaml file.

Developed by IXG6 Team
----------------------------------------------------------------------------
"""

import argparse
import json
import logging
import os
import sys
import yaml

logging.basicConfig(format='%(asctime)s | %(name)s | %(levelname)s: %(message)s', level=logging.INFO)
logger = logging.getLogger('yaml to json converter')


def read_input_parameters():
    """
    Parses the input parameters of the script
    :return: An object that contains the values of the input parameters
    """
    args_parser = argparse.ArgumentParser(
        prog='yaml_to_json',
        description='Takes as input a yaml file and converts it to json',
        epilog='Developed by IXG6 Team')
    args_parser.add_argument('--input-yaml-file', dest='input_file', required=True, type=str,
                             help='The path to the input yaml file.')
    args_parser.add_argument('--output-json-file', dest='output_file', required=True, type=str,
                             help='The path to the input yaml file.')
    args = args_parser.parse_args()
    return args


def convert_unicode_to_string(value):
    """
    In case that the python version is 2 the conversion of JSON to string
    returns a unicode type that needs to be converted to string type
    :param value: The JSON string
    :return: Converted JSON string to str type
    """
    # this is required for python 2 compatibility
    if sys.version_info[0] < 3 and type(value) is unicode:
        return value.encode('utf-8')
    elif type(value) is str:
        return value
    else:
        logger.error('Invalid data type. Found {} expected unicode or str.'.format(type(value)))
        sys.exit(1)


def read_yaml_file(yaml_file):
    """
    Reads a yaml file
    :param yaml_file: Path of yaml file
    :return: The contents of yaml file as dictionary
    """
    if os.path.isfile(yaml_file):
        with open(yaml_file, 'r') as yf:
            try:
                yaml_data = yaml.safe_load(yf)
            except Exception:
                logger.error('Fail to read the input yaml file {}.'.format(yaml_file))
                sys.exit(1)
            return yaml_data
    else:
        logger.info('The provided yaml file {} does not exist.'.format(yaml_file))


def write_json_file(json_file, json_string):
    """
    Writes to a json file the data as JSON string
    :param json_file: The path of the JSON file that will be created
    :param json_string: The data to be written to the file
    """
    with open(json_file, 'w') as js:
        try:
            js.write(json_string)
        except Exception:
            logger.error('Unable to write data to file {}.'.format(json_file))


def filter_format_string(json_string):
    """
    Escapes the double quotes and backslashes and removes the new line character
    :param json_string: Data in JSON string
    :return: The initial data after the processing
    """
    if type(json_string) is str:
        processed_string = json_string.strip()
        # remove new lines
        processed_string = processed_string.replace('\n', '')
        processed_string = processed_string.replace('\r', '')
        # Escape back slashes
        processed_string = processed_string.replace('\\', '\\\\')
        # If there were escaped double quotes unescape them because now they are double escaped from previous step
        processed_string = processed_string.replace('\\"', '"')
        # escape the double quotes
        processed_string = processed_string.replace('"', '\\"')
        return processed_string
    else:
        logger.error('The provided type is not string.')
        sys.exit(1)


def main():
    """
    This is the main function
    """
    args = read_input_parameters()
    logger.info('Reading yaml file and convert its data to JSON string.')
    json_string_data = json.dumps(read_yaml_file(args.input_file))
    logger.info('Convert unicode data type to str. This conversion si necessary for compatibility with Python 2.')
    json_string_data = convert_unicode_to_string(json_string_data)
    logger.info('Removing new lines and escape double quotes and back slashes.')
    json_string_data = filter_format_string(json_string_data)
    logger.info('Saving result to output file.')
    write_json_file(args.output_file, json_string_data)
    logger.info('Conversion completed successfully.')
    sys.exit(0)


if __name__ == '__main__':
    main()
