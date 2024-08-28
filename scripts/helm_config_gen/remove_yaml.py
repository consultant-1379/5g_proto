#!/usr/bin/env python3.10

import sys
import yaml

def remove_component(input_file, output_file, components):
    with open(input_file, 'r') as file:
        data = yaml.safe_load(file)
        for component in components:
            data.pop(component)
    with open(output_file, 'w') as file:
        yaml.safe_dump(data, file)


input_file = sys.argv[1]
output_file = sys.argv[2]
components = sys.argv[3:]
    
remove_component(input_file, output_file, components)