#!/usr/bin/env python3.10

import sys
import yaml
from dpath import util as dpath
import dpath
from dpath.types import MergeType

def merge_yaml_files(input_files, output_file):

    merged_data = {}
    
    for file_name in input_files:
        with open(file_name, 'r') as file:
            data = yaml.safe_load(file)
            print(file_name)
            dpath.merge(merged_data, data, flags=MergeType.ADDITIVE)

    with open(output_file, 'w') as outfile:
        yaml.dump(merged_data, outfile)

output_file = sys.argv[1]
input_files = sys.argv[2:]
    
merge_yaml_files(input_files, output_file)
