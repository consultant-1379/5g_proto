#######################################################################################################
#
# COPYRIGHT ERICSSON GMBH 2023-2024
#
# The copyright to the computer program(s) herein is the property of Ericsson GmbH, Germany.
#
# The program(s) may be used and/or copied only with the written permission of Ericsson GmbH in
# accordance with the terms and conditions stipulated in the agreement/contract under which the
# program(s) have been supplied.
#
#######################################################################################################
DEBUG = False
CHECK_SHEET_TO_YAML = False
CHECK_YAML_TO_SHEET = False
CHECK_SHEET_TO_VNFD = False
CHECK_DEFAULT_VALUES = True

# function that returns all values of a given column and sheet name of a given Excel workbook
def get_params_from_sheet(workbook, sheet_name, column_name, column_filter="", filter_val=""):
    # import modules
    import pandas as pd
    import os

    column_values_str = []

    # create a dataframe from the excel sheet
    df = pd.read_excel(workbook, sheet_name=sheet_name)

    if column_name in df.keys():
        column_values = df[column_name].tolist()
        column_values_str = []

        if column_filter == "":
            # convert values to string
            for value in column_values:
                column_values_str.append(str(value))
        else:
            # we have a filter
            column_filter_values = df[column_filter].tolist()

            row_no = 0
            for value in column_values:
                if str(column_filter_values[row_no]) == filter_val:
                    column_values_str.append(str(value))
                row_no += 1

    return column_values_str


# function that recursively decent to fetch all map keys and values of a Python data structure
# special function particulary defined for VNF descriptor
def get_params_and_vals_from_yaml(yaml_params_val_result, yaml_data, param_head, ends_with_filter=""):
    if isinstance(yaml_data, dict):
        if len(param_head) > 0:
            param_head = param_head + '.'
            
        for mapKey in yaml_data.keys():
            get_params_and_vals_from_yaml(yaml_params_val_result, yaml_data[mapKey], param_head + mapKey, ends_with_filter)
    else:
        if ends_with_filter == "" or param_head.endswith(ends_with_filter):
            yaml_params_val_result[yaml_data] = param_head


# function that recursively decents to fetch all map keys of a Python data structure
def get_params_from_yaml(yaml_params_result, yaml_data, param_head):
    if isinstance(yaml_data, dict):
        if len(param_head) > 0:
            param_head = param_head + '.'
            
        for mapKey in yaml_data.keys():
            get_params_from_yaml(yaml_params_result, yaml_data[mapKey], param_head + mapKey)
    else:
        yaml_params_result.append(param_head)


# function fetching all map keys of an all chart data structure
def get_params_from_charts(all_charts, umbrella_chart):
    yaml_params = []

    for chart_name in all_charts.keys():
        if chart_name == umbrella_chart:
            prefix = ""
        else:
            prefix = chart_name
        
        get_params_from_yaml(yaml_params, all_charts[chart_name], prefix)

    return yaml_params



# function that checks if a dotted string is a given data structure
def key_is_in(dotted_string, yaml_data):
    if DEBUG: print("   >>> dotted_string = \"{}\"".format(dotted_string))

    # split the string at the dots
    split_string = dotted_string.split('.')

    matchingPrefix = ''

    # iterate over the split string
    for item in split_string:
        curKeys = yaml_data.keys()
        if DEBUG: print("   >>> item = \"{}\"".format(item))
        if DEBUG: print("   >>> curKeys = \"{}\"".format(curKeys))
        # check if item is in yaml_data
        if item not in curKeys:
            return False, matchingPrefix, "<NIL>"
        else:
            yaml_data = yaml_data[item]
            matchingPrefix += item + '.'

        if DEBUG: print("         >>>>>> matchingPrefix=\'{}\'".format(matchingPrefix))

    # return True if all items are in yaml_data
    return True, matchingPrefix, yaml_data


def get_chart_by_param(param, all_charts, umbrella_chart):
    chart_name = param.split('.')[0]

    if chart_name in all_charts.keys():
        return chart_name, param.split('.',1)[1]
    else:
        return umbrella_chart, param
    

def accept_as_default(value, default_value, sheet_param, sheet_name, sheet_param_id, errors, warnings):
    if DEBUG: print()
    if DEBUG: print("                         [0] OOOOOOOOO value=\"{}\", type={}".format(value, type(value)))
    if DEBUG: print("                         [1]   default_value=\"{}\", type={}".format(default_value, type(default_value)))
    if DEBUG: print("                         [1]           param=\"{}\", type={}".format(param, type(param)))
    if value == default_value:
        if DEBUG: print("                         [1] TRUE")
        return True
    
    if value == None:
        value = ""
    
    if DEBUG: print("                         [2] OOOOOOOOO")
    # Remove default:
    if default_value.startswith("default:"):
        default_value = default_value.removeprefix("default:").strip()
        if value == default_value:
            if DEBUG: print("                         [2] TRUE")
            return True
    else:
        default_value = default_value.strip()

    if DEBUG: print("                         [3] OOOOOOOOO value=\"{}\", type={}".format(value, type(value)))
    if DEBUG: print("                         [3]   default_value=\"{}\", type={}".format(default_value, type(default_value)))
    # Check if value is dict or list
    if isinstance(value, list) or isinstance(value, dict):
        if DEBUG: print("                         [3.5.1] OOOOOOOOO Try YAML interpretation of default value=")
        if DEBUG: print(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>")
        if DEBUG: print(default_value)
        if DEBUG: print("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<")

        default_value_yaml = {}

        try:
            default_value_yaml = yaml.safe_load(default_value)
        except Exception as e:
            if DEBUG: print("                         [3.6] SYNTAX ERROR! (IGNORED)")
            if DEBUG: print("                         [3.6] ==> {}".format(e))
            print('< WARNING: Cannot check default value of param {}! Syntax error found in sheet {}/No.{}/Col.\"Value\":'.format(sheet_param, sheet_name, sheet_param_id))
            print('{}'.format(default_value))
            print('-------------------------')
            print('Error was:')
            print('{}'.format(e))
            print('-------------------------')
            warnings += 1

        if DEBUG: print("                         [3.5.2]   default_value_yaml=\"{}\", type={}".format(default_value_yaml, type(default_value_yaml)))
        # accepted = value == default_value_yaml[value.rplit('.', 1)[0]]
        accepted = value == default_value_yaml
        if DEBUG: print("                         [3.5.3] OOOOOOOOO")
        if DEBUG: print("                         [3.5] {}".format(accepted))

        # Attempt fallback for the case in which the sheet's default value wrongly repeats the top mapping parameter.
        #
        # For example consider parameter 'eric-sc-rlf.spec.rlf.tolerations' which should have the default value to the left below,
        # however in the the Excel workbook you'll find the one including 'tolerations':
        #
        #                                                           tolerations:
        #   - key: node.kubernetes.io/not-ready                       - key: node.kubernetes.io/not-ready
        #     operator: Exists                                          operator: Exists
        #     effect: NoExecute                                         effect: NoExecute    
        #     tolerationSeconds: 0                                      tolerationSeconds: 0
        #   - key: node.kubernetes.io/unreachable                     - key: node.kubernetes.io/unreachable
        #     operator: Exists                                          operator: Exists
        #     effect: NoExecute                                         effect: NoExecute
        #     tolerationSeconds: 0                                      tolerationSeconds: 0
        #
        #
        if not accepted and '.' in sheet_param:
            trailing_param_path = sheet_param.rsplit('.', 1)[1]

            if trailing_param_path in default_value_yaml:
                default_value_yaml_sub = default_value_yaml[trailing_param_path]

                if DEBUG: print("                         [3.7.1] OOOOOOOOO")
                if DEBUG: print("                         [3.7.2]   default_value_yaml_sub=\"{}\", type={}".format(default_value_yaml_sub, type(default_value_yaml_sub)))
                if DEBUG: print("                         [3.7.3]                    value=\"{}\", type={}".format(value, type(value)))
                accepted = value == default_value_yaml_sub
                if DEBUG: print("                         [3.7.4] {}".format(accepted))

        return accepted


    # convert to string then
    value = str(value)

    # value is a string from this point on
    if DEBUG: print("                         [5] OOOOOOOOO value=\"{}\", type={}".format(value, type(value)))
    if DEBUG: print("                         [5]   default_value=\"{}\", type={}".format(default_value, type(default_value)))

    if value == default_value:
        if DEBUG: print("                         [5] TRUE")
        return True

    # Remove surrounding quotes
    if default_value.startswith('"') and default_value.endswith('"'):
        default_value = default_value.removeprefix('"')[:-1]
        if DEBUG: print("                         [6.5] OOOOOOOOO value=\"{}\", type={}".format(value, type(value)))
        if DEBUG: print("                         [6.5]   default_value=\"{}\", type={}".format(default_value, type(default_value)))

        if value == default_value:
            if DEBUG: print("                         [6.5] TRUE")
            return True

    if value.lower() == "true" and default_value.lower() == "true" or value.lower() == "false" and default_value.lower() == "false":
        if DEBUG: print("                         [5.5] TRUE")
        return True

    if DEBUG: print("                         [6] OOOOOOOOO value=\"{}\", type={}".format(value, type(value)))
    if DEBUG: print("                         [6]   default_value=\"{}\", type={}".format(default_value, type(default_value)))

    if DEBUG: print("                         [8] OOOOOOOOO value=\"{}\", type={}".format(value, type(value)))
    if DEBUG: print("                         [8]   default_value=\"{}\", type={}".format(default_value, type(default_value)))
    if DEBUG: print("                         [8] FALSE")
    return False

# convenience for printing to stderr
def eprint(*args, **kwargs):
    import sys

    print(*args, file=sys.stderr, **kwargs)


# main
if __name__ == '__main__':
    errors = 0
    warnings = 0

    # import modules
    import os
    import argparse
    import yaml

    # create an argument parser
    parser = argparse.ArgumentParser(description='Compares the day-0 parameters documented in the SC day-0 param Excel workbook against values.yaml files and the VNF descriptor.')

    # add arguments
    parser.add_argument('-c', '--check', choices=['sheets', 'yamls', 'vnfd', 'all'], default='all', help='Use to run selected checks. Default: all', required=False)
    parser.add_argument('--skip', action='store_false', help='Skip checking correctness of default values (Columns \"Values\" in workbook)', required=False)
    parser.add_argument('--debug', action='store_true', help='Debug mode', required=False)
    parser.add_argument('WORKBOOK', help='Name of input Excel workbook.')
    parser.add_argument('SHEETS', help='Comma separated list of workbook sheets to check.')
    parser.add_argument('CHARTS_PATH', help='Common path to all input Chart.yaml files.')
    parser.add_argument('CHARTS', help='Comma separated list of Chart.yaml files starting with the umbrella chart.')
    parser.add_argument('-v','--vnf-descriptor', help='Path to VNF descriptor. If present check mandatory workbook parameters.', required=False)
    
    # parse the arguments
    args = parser.parse_args()

    VNFD = args.vnf_descriptor

    match args.check:
        case "sheets":
            CHECK_SHEET_TO_YAML = True
        case "yamls":
            CHECK_YAML_TO_SHEET = True
        case "vnfd":
            CHECK_SHEET_TO_VNFD = True
            if VNFD is None or len(VNFD) == 0:
                eprint("Option '--check vnfd' requires --vnf-descriptor")
                exit(1)
        case _:
            CHECK_SHEET_TO_YAML = True
            CHECK_YAML_TO_SHEET = True
            CHECK_SHEET_TO_VNFD = VNFD is not None and len(VNFD) > 0

    CHECK_DEFAULT_VALUES = args.skip
    DEBUG = args.debug

    if DEBUG:
        print("Started in debug mode.")

#    sheet_file_out = os.path.join(args.sheet_path_out, args.sheet_file_out)
#    os.system('cp {} {}'.format(sheet_file_in, sheet_file_out))

    all_charts = {}

    chart_no = 0
    umbrella_chart = ""

    sheet_no = 0
    print("Sheets:")
    for sheet_name in args.SHEETS.split(','):
        sheet_no += 1
        print("[{}] {}:{}".format(sheet_no, args.WORKBOOK, sheet_name))


    print()
    print("Charts:")

    for chart_file_name in args.CHARTS.split(','):
        chart_no += 1
        chart_file = os.path.join(args.CHARTS_PATH, chart_file_name)

        # open Chart.yaml file
        with open(chart_file, 'r') as stream:
            try:
                chart_data = yaml.safe_load(stream)
            except yaml.YAMLError as exc:
                print(exc)

        chart_name = chart_data["name"]

        # open corresponding values.yaml
        values_yaml_path = os.path.dirname(chart_file)
        values_yaml_file = os.path.join(values_yaml_path, "values.yaml")

        with open(values_yaml_file, 'r') as stream:
            try:
                chart_values = yaml.safe_load(stream)
            except yaml.YAMLError as exc:
                print(exc)


        print("[{}] {}:{}.".format(chart_no, chart_name, values_yaml_file))
        all_charts[chart_name] = chart_values

        if chart_no == 1:
            umbrella_chart = chart_name

        

    print("Umbrella chart: \"{}\"".format(umbrella_chart))

    if DEBUG:
        print()
        print("   > > >   C H E C K I N G   A L L   S H E E T S   A G A I N S T   A L L   V A L U E S . Y A M L")
        print()
        print("CHECK_SHEET_TO_YAML={}",format(CHECK_SHEET_TO_YAML))
    # iterate through all comma separated tuples of sheet_yaml_compare

    all_workbook_day0_params = []
    all_workbook_day0_params_metadata = {}
    all_mandatory_workbook_day0_params = []

    if CHECK_SHEET_TO_YAML: print()

    for sheet_name in args.SHEETS.split(','):
        # print what is compared including sheet_file_in, sheet_name and yaml_file
        if DEBUG: print("@@@ {}:{}:".format(args.WORKBOOK, sheet_name))

        all_sheet_day0_ids = get_params_from_sheet(args.WORKBOOK, sheet_name, 'No.')

        # be robust against different column namings of 'No.'
        if len(all_sheet_day0_ids) == 0:
            all_sheet_day0_ids = get_params_from_sheet(args.WORKBOOK, sheet_name, 'No')

        all_sheet_day0_params = get_params_from_sheet(args.WORKBOOK, sheet_name, 'Parameter Name')
        all_workbook_day0_params = all_workbook_day0_params + all_sheet_day0_params
        all_sheet_day0_defaults = get_params_from_sheet(args.WORKBOOK, sheet_name, 'Value')

        item_no = 0
        for cur_param in all_sheet_day0_params:
            all_workbook_day0_params_metadata[cur_param] = {'sheet_name': sheet_name,
                                                            'param_id': all_sheet_day0_ids[item_no],
                                                            'value': all_sheet_day0_defaults[item_no]}
            item_no += 1

        # collect mandatory params separately
        all_mandatory_sheet_day0_params = get_params_from_sheet(args.WORKBOOK, sheet_name, 'Parameter Name', 'Mandatory', 'Yes')
        all_mandatory_workbook_day0_params = all_mandatory_workbook_day0_params + all_mandatory_sheet_day0_params

        if CHECK_SHEET_TO_YAML:
            #### COMPARE SHEET WITH YAML ####
            itemNo = 0

            # iterate over all sheet values
            for sheet_param in all_sheet_day0_params:
                chart, param = get_chart_by_param(sheet_param, all_charts, umbrella_chart)
 
                # return True, matchingPrefix, yaml_data
                if DEBUG: print("key_is_in(param=\"{}\", all_charts[\"{}\"])".format(param, chart))
                is_in, matchingPrefix, yaml_value = key_is_in(param, all_charts[chart])

                sheet_param_id = all_sheet_day0_ids[itemNo]
                sheet_default = all_sheet_day0_defaults[itemNo]

                if is_in:
                    # check if default value complies to what is in yaml
                    if CHECK_DEFAULT_VALUES and not accept_as_default(yaml_value, sheet_default, sheet_param, sheet_name, sheet_param_id, errors, warnings):
                        print('< WARNING: Sheet param {} ({}/No.{}) claims \'{}\' not complying to \'{}\' in {}!'.format(sheet_param, sheet_name, sheet_param_id, sheet_default, yaml_value, chart))
                        warnings += 1
                else:
                    # otherwise print it is not
                    print('<   ERROR: Sheet param {} ({}/No.{}) missing in any given YAML'.format(sheet_param, sheet_name, sheet_param_id), end='')

                    if len(matchingPrefix) > 0:
                        print(" (best prefix match \'{}\' found in {})!".format(matchingPrefix, chart))
                    else:
                        print("!")

                    errors += 1

                itemNo += 1


    # create a list of all yaml keys
    if CHECK_YAML_TO_SHEET:
        if DEBUG:
            print()
            print("   < < <   C H E C K I N G   A L L   Y A M L   P A R A M S   A G A I N S T   A L L   S H E E T S")
            print()
        yaml_keys = get_params_from_charts(all_charts, umbrella_chart)

        '''
        print(">>>>>>>>>>>>>>>>>>>>>>>>>>>")
        print(yaml_keys)
        print("<<<<<<<<<<<<<<<<<<<<<<<<<<<")

        exit(0)
        '''

        # interate over all yaml keys and check if they are in all_sheet_values
        for key in yaml_keys:
            if key not in all_workbook_day0_params:
                print('>   ERROR: YAML param {} missing in workbook/sheets!'.format(key))
                errors += 1

    vnfd_data = {}
    if CHECK_SHEET_TO_VNFD:
        if DEBUG:
            print()
            print("   < < <   C H E C K I N G   A L L   S H E E T S   A G A I N S T   V N F   D E S C R I P T O R")
            print()
        # print(all_mandatory_workbook_day0_params)
        # exit(0)

        with open(VNFD, 'r') as stream:
            try:
                vnfd_data = yaml.safe_load(stream)
            except yaml.YAMLError as exc:
                eprint("SYNTAX ERROR in {}!".format(VNFD))
                eprint(exc)
                exit(3)

        all_vnfd_params_vals = {}
        get_params_and_vals_from_yaml(all_vnfd_params_vals, vnfd_data, "", "chart_param")


        all_vnfd_params = all_vnfd_params_vals.keys()

        for day0_param in all_vnfd_params:
            if day0_param in all_workbook_day0_params:
                if day0_param not in all_mandatory_workbook_day0_params:
                    sheet_name = all_workbook_day0_params_metadata[day0_param]["sheet_name"]
                    param_id = all_workbook_day0_params_metadata[day0_param]["param_id"]
                    print("> WARNING {} not marked as mandatory in sheet {}/No.{}.".format(day0_param, sheet_name, param_id))
                    warnings += 1
            else:
                print(">   ERROR {} not present in workbook.".format(day0_param))
                errors += 1

        for cur_param in all_mandatory_workbook_day0_params:
            if cur_param not in all_vnfd_params:
                sheet_name = all_workbook_day0_params_metadata[cur_param]["sheet_name"]
                param_id = all_workbook_day0_params_metadata[cur_param]["param_id"]
                print("<   ERROR Mandatory {} in sheet {}/No.{} not present in VNF descriptor.".format(cur_param, sheet_name, param_id))
                errors += 1



    #
    #   R E S U L T S
    #
    # exist with 0 if no errors and no warnings were found output numbers otherwise
    if errors == 0 and warnings == 0:
        print('No errors or warnings.')
        exit(0)
    else:
        print('{} errors/{} warnings.'.format(errors, warnings))
        exit(2)
