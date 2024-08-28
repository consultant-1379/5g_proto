#!/usr/bin/env python3.10

import os
import logging
import sys
import yaml
import dpath
from dpath.types import MergeType
# from dpath import util as dpath
from argparse import ArgumentParser, RawTextHelpFormatter
import textwrap
import shutil
from kubernetes import client, config

log = logging.getLogger(__name__)
HOME = os.path.abspath( os.path.dirname( __file__ ) )

PROFILES_FOLDER = ".bob/deploy-profiles" # relative to bob working directory
DYNAMIC_CONFIG_FOLDER = "dynamic-configuration"
IHC_REDUCED_RESOURCES_FOLDER = "profiles/small_footprint_resources.yaml"

## INTEGRATION HELM CHARTS ##
CNCS_BASE_NAME = "eric-cloud-native-base"
CNCS_ADD_NAME = "eric-cloud-native-nf-additions"
SC_CS_NAME = "eric-sc-cs"
SC_BSF_NAME = "eric-sc-bsf"
SC_SCP_NAME = "eric-sc-scp"
SC_SEPP_NAME = "eric-sc-sepp"
SC_DSC_NAME = "eric-dsc"

## PROFILES-CONFIGURATION-FILES ##
DEFAULT_FILE = "default.yaml"
NO_METRICS_FILE = "no-metrics.yaml"
NO_LOGGING_FILE = "no-logging.yaml"
LOG_STREAMING_DIRECT_FILE = "log-streaming-direct.yaml"
LOG_STREAMING_INDIRECT_FILE = "log-streaming-indirect.yaml"
LOG_STREAMING_DUAL_FILE = "log-streaming-dual.yaml"
PM_RW_FILE = "pm-remote-write.yaml"
SYSLOG_FILE = "syslog.yaml"
IPV4_INT_FILE = "ipv4-internal.yaml"
IPV4_EXT_FILE = "ipv4-external.yaml"
IPV6_INT_FILE = "ipv6-internal.yaml"
IPV6_EXT_FILE = "ipv6-external.yaml"
DS_INT_FILE = "dualstack-internal.yaml"
DS_EXT_FILE = "dualstack-external.yaml"
LJ_SINGLE_FILE = "lumberjack.yaml"
LJ_MULTIPLE_FILE = "lumberjack-multiple.yaml"
ROP_FILES_OPT2_FILE = "rop-files-option2.yaml"
LB_NP_FILE = "allocate-loadbalancer-nodeports.yaml"
REDUCED_RESOURCES_FILE = "small_footprint_resources.yaml"
NO_HIGH_AVAILABILITY_FILE = "noHA.yaml"
TAPAGENT_FILE = "tapagent.yaml"
TAPCOLLECTOR_FILE = "tapcollector.yaml"
NLF_FILE = "nlf.yaml"
RLF_FILE = "rlf.yaml"
VTAP_FILE = "vtap.yaml"
VTAP_LITE_FILE = "vtap-lite.yaml"
VTAP_DTLS_FILE = "vtap-dtls.yaml"
BSF_GEORED_FILE = "bsf-geored.yaml"
MULTI_VPN_FILE = "multi-vpn.yaml"
BSF_DIAMETER_FILE = "bsf-diameter.yaml"
SLF_FILE = "slf.yaml"
SHARED_DBPG_FILE="shared-dbpg.yaml"

BOB_VAR_SUFFIX = "profiles"
FINAL_VALUES_SUFFIX = "values-final.yaml"

def exit_and_fail(msg):
    if msg:
        log.error(msg)
    sys.exit(msg)

def read_yaml(yaml_file):
    if not os.path.exists(yaml_file):
        exit_and_fail("Couldn't find file: " + yaml_file)
    with open(yaml_file, 'r') as stream:
        try:
            doc = yaml.safe_load(stream)
        except yaml.YAMLError as exc:
            exit_and_fail("Not a yaml file: " + exc)

    return doc

#todo
def read_var(var_file):
    var =""
    if not os.path.exists(var_file):
        exit_and_fail("Couldn't find file: " + var_file)
    else:
        try:
            file = open(var_file, 'r')
        except OSError as exc:
            exit_and_fail("Could not open file: " + str(var_file) + " due to error: " + str(exc))
        else:
            var = file.read().strip()

    return var

def check_args():

    file_help="""Use a yaml file as input. Its contents will be added in generated yaml.
    Caution: This will overwrite any already existed values. If more than one will be used,
    the latest will overwrite its values on the previous one in case of collision"""

    parser = ArgumentParser(

        description='This script generates a yaml file that will be used as input during helm install procedure.\n'
                    'This file contains properties regarding deployment environment and modules.\n'
                    'These properties can be configured via input parameters of the script.\n'
                    'There are two ways to configure these parameters. The first way is by giving a specific\n'
                    'variable and its value using -s or --set.\n'
                    'The second way is by using a .yaml file as input using -f or --file.\n'
                    'Beware that the set-parameters (-s or --set) overwrites theirs values over the input files\n'
                    'that given (-f or --file) if a conflict occurs.\n'
                    'The same applies also if multiple files are given as input. The second will overwrite its\n'
                    'values on the first one in case of conflict and so on.\n'
                    'If the script called without any optional input parameters, then the default configuration will be used.',
        epilog=textwrap.dedent('''\
                Usage examples:
                config_gen -i <input_path> -o <output_path> -e minikube --set global.ericsson.scp.enabled=false
                config_gen -i <input_path> -o <output_path> -e hahn034 -s global.ericsson.scp.enabled=false -c true
                config_gen -i <input_path> -o <output_path> -e minikube -f ../.bob/values.yaml --bsf-tls true --set global.ericsson.bsf.enabled=true -c false
                config_gen -i <input_path> -o <output_path> -e minikube -f ../.bob/values.yaml -f ../.bob/devenv_values.yaml
                config_gen -i <input_path> -o <output_path> -e minikube -f ../.bob/values.yaml -s global.ericsson.scp.enabled=false'''),
        formatter_class=RawTextHelpFormatter)
        
    parser.add_argument("--ihc", dest="ihc", help="Set the integration helm chart name to be used.", required=True)

    parser.add_argument("-n", dest="namespace", help="Set the namespace to be used.", required=True)
    parser.add_argument("-i", "--input", dest="input_path", help="Set the path where the integration chart's values file exists. This will be used as main template", required=True)
    parser.add_argument("-o", "--output", dest="output_path", help="Set the output path where the generated .yaml file will be placed", required=True)
    parser.add_argument("-e", dest="env", help="Set the deployment environment", required=True)
    parser.add_argument("-f", "--file", dest="files", action="append", help=file_help, required=False)
    parser.add_argument("-s", "--set", dest="sets", action="append", help="\nSet a parameter value that needs to be added in the generated yaml file", required=False)
    parser.add_argument("-c", dest="customer", help="Set this parameter to true if customer setting will be used for deployment", required=False)
    parser.add_argument("-r", "--resources", dest="resources", help="Define what resource template to use.")
    parser.add_argument("-dc1", "--datacenter1", dest="dc1", help="Set this to define namespace for geored datacenter1.", required=False)
    parser.add_argument("-dc2", "--datacenter2", dest="dc2", help="Set this to define geored datacenter2 to generate values for.", required=False)

    parser.add_argument("--ip-version-ext", dest="ip_version_ext", help="Define what IP Version (4, 6 or DS) to use for external interfaces.", required=False)
    parser.add_argument("--ip-version-int", dest="ip_version_int", help="Define what IP Version (4, 6 or DS) to use for internal interfaces.", required=False)
    parser.add_argument("--pm-rw", dest="pm_remote_write", help="Set this parameter to true for enabling internal pm remote write to internal influx database", required=False)
    parser.add_argument("--metrics", dest="metrics", help="Set this to false to disable all metrics sidecars.", required=False)
    parser.add_argument("--syslog", dest="syslog", help="Set this to true to enable syslog interface for log transformer", required=False)
    parser.add_argument("--lj", dest="lumberjack", help="Set this to true to enable one lumberjack interface for log transformer", required=False)
    parser.add_argument("--lj-m", dest="lumberjack_m", help="Set this to true to enable multiple lumberjack interfaces for log transformer", required=False)
    parser.add_argument("--logging", dest="logging", help="Set this to false to disable the logging-related services", required=False)
    parser.add_argument("--log-stream", dest="log_stream", help="Set this to direct, indirect or dual to enabled the respective log streaming method", required=False)
    parser.add_argument("--rop2", dest="rop", help="Set this to true to enable adp sftp server as rop file storage", required=False)
    parser.add_argument("--lb-np", dest="lb_np", help="Set this to true to allocate LoadBalancer NodePorts", required=False)
    parser.add_argument("--multi-vpn", dest="multi_vpn", help="Set this to true to enable multi-VPN", required=False)
    parser.add_argument("--ha", dest="high_availability", help="Set this to false to deploy with no-high-availability", required=False)
    parser.add_argument("--vtap", dest="vtap", help="Set this to enable PVTB, Vtaprecorder, tapcollector.", required=False)
    parser.add_argument("--vtap-l", dest="vtap_l", help="Set this to enable the tapagent.", required=False)
    parser.add_argument("--vtap-dtls", dest="vtap_dtls", help="Set this to enable the DTLS for PVTB.", required=False)
    parser.add_argument("--rlf", dest="rlf", help="Set this to enable RLF and respective configuration.", required=False)
    parser.add_argument("--nlf", dest="nlf", help="Set this to enable NLF and respective configuration.", required=False)
    parser.add_argument("--bsf-diameter", dest="bsf_diameter", help="Set this to enable stm/bsf-diameter and the respective bsf manager configuration.", required=False)
    parser.add_argument("--slf", dest="slf", help="Set this to enable slf and the respective scp configuration.", required=False)
    parser.add_argument("--shared-dbpg", dest="shared_dbpg", help="Set this to false to deploy dedicated dbpg servicse", required=False)

    script_input = dict()

    script_input["resources"] = parser.parse_args().resources

    script_input["namespace"] = parser.parse_args().namespace

    script_input["ihc"] = parser.parse_args().ihc

    script_input["log_stream"] = parser.parse_args().log_stream

    # check if input path is valid
    if not os.path.exists(parser.parse_args().input_path):
        exit_and_fail("The input path: " + str(parser.parse_args().input_path) + " is not valid!")
    script_input["input_path"] = parser.parse_args().input_path

    # check if output path is valid
    if not os.path.exists(parser.parse_args().output_path):
        exit_and_fail("The output path: " + str(parser.parse_args().output_path) + " is not valid!")
    script_input["output_path"] = parser.parse_args().output_path

    # parse pm-rw variable and use boolean value
    if parser.parse_args().pm_remote_write in ("TRUE", "True", "true", True):
        script_input["pm_remote_write"] = True
    else:
        script_input["pm_remote_write"] = False

    # parse customer variable and use boolean value
    if parser.parse_args().customer in ("TRUE", "True", "true", True):
        script_input["customer"] = True
    else:
        script_input["customer"] = False

    # check if internal IP version is valid
    script_input["ip_version_int"] = parser.parse_args().ip_version_int
    if (script_input["ip_version_int"] is not None) and (script_input["ip_version_int"] not in ("4", "6", "DS")):
            exit_and_fail("Wrong input for internal IP version, valid input values: 4, 6 and DS.")

    # check if external IP version is valid
    script_input["ip_version_ext"] = parser.parse_args().ip_version_ext
    if (script_input["ip_version_ext"] is not None) and (script_input["ip_version_ext"] not in ("4", "6", "DS")):
            exit_and_fail("Wrong input for external IP version, valid input values: 4, 6 and DS.")

    #check customer setting are going to be deployed in minikube environment
    if parser.parse_args().env == "minikube" and script_input["customer"] == True:
        exit_and_fail("Customer settings in "+str(parser.parse_args().env) + " environment can not be used. Please either disable customer settings (set -c to false) or use another environment")
    script_input["env"] = parser.parse_args().env

    # check if the file paths are valid
    if parser.parse_args().files != None:
        for file in parser.parse_args().files:
            if not os.path.isfile(file):
                exit_and_fail("the file: %s does not exist" %file)
    script_input["files"] = parser.parse_args().files

    # check format of every parameter
    if parser.parse_args().sets != None:
        for set_var in parser.parse_args().sets:
            if '=' not in set_var:
                exit_and_fail("There is no '=' operator in %s" %set_var)
    script_input["sets"] = parser.parse_args().sets

    if parser.parse_args().dc1 == "false":
        script_input["dc1_namespace"] = None
    else:
        script_input["dc1_namespace"] = parser.parse_args().dc1

    if parser.parse_args().dc2 == "false":
        script_input["dc2_namespace"] = None
    else:
        script_input["dc2_namespace"] = parser.parse_args().dc2

    # parse syslog variable and use boolean value
    if parser.parse_args().metrics in ("TRUE", "True", "true", True):
        script_input["metrics"] = True
    else:
        script_input["metrics"] = False

    # parse syslog variable and use boolean value
    if parser.parse_args().syslog in ("TRUE", "True", "true", True):
        script_input["syslog"] = True
    else:
        script_input["syslog"] = False

    # parse lumberjack variable and use boolean value
    if parser.parse_args().lumberjack in ("TRUE", "True", "true", True):
        script_input["lumberjack"] = True
    else:
        script_input["lumberjack"] = False

    # parse lumberjack_m variable and use boolean value
    if parser.parse_args().lumberjack_m in ("TRUE", "True", "true", True):
        script_input["lumberjack_m"] = True
    else:
        script_input["lumberjack_m"] = False

    # parse logging variable and use boolean value
    if parser.parse_args().logging in ("TRUE", "True", "true", True):
        script_input["logging"] = True
    else:
        script_input["logging"] = False

    # parse rop variable and use boolean value
    if parser.parse_args().rop in ("TRUE", "True", "true", True):
        script_input["rop"] = True
    else:
        script_input["rop"] = False

    # parse lb_np variable and use boolean value
    if parser.parse_args().lb_np in ("TRUE", "True", "true", True):
        script_input["lb_np"] = True
    else:
        script_input["lb_np"] = False

    # parse high_availability variable and use boolean value
    if parser.parse_args().high_availability in ("TRUE", "True", "true", True):
        script_input["high_availability"] = True
    else:
        script_input["high_availability"] = False

    # parse shared_dbpg variable and use boolean value
    if parser.parse_args().shared_dbpg in ("TRUE", "True", "true", True):
        script_input["shared_dbpg"] = True
    else:
        script_input["shared_dbpg"] = False
        
    # parse multi_vpn variable and use boolean value
    if parser.parse_args().multi_vpn in ("TRUE", "True", "true", True):
        script_input["multi_vpn"] = True
    else:
        script_input["multi_vpn"] = False

    # parse vtap variable and use boolean value
    if parser.parse_args().vtap in ("TRUE", "True", "true", True):
        script_input["vtap"] = True
    else:
        script_input["vtap"] = False

    # parse vtap light variable and use boolean value
    if parser.parse_args().vtap_l in ("TRUE", "True", "true", True):
        script_input["vtap_l"] = True
    else:
        script_input["vtap_l"] = False

    # parse rate limit variable and use boolean value
    if parser.parse_args().rlf in ("TRUE", "True", "true", True):
        script_input["rlf"] = True
    else:
        script_input["rlf"] = False

    # parse nrf lookup variable and use boolean value
    if parser.parse_args().nlf in ("TRUE", "True", "true", True):
        script_input["nlf"] = True
    else:
        script_input["nlf"] = False

    # parse bsf diameter variable and use boolean value
    if parser.parse_args().bsf_diameter in ("TRUE", "True", "true", True):
        script_input["bsf_diameter"] = True
    else:
        script_input["bsf_diameter"] = False

    # parse subscriberlocation variable and use boolean value
    if parser.parse_args().slf in ("TRUE", "True", "true", True):
        script_input["slf"] = True
    else:
        script_input["slf"] = False

    # parse vtap_dtls variable and use boolean value
    if parser.parse_args().vtap_dtls in ("TRUE", "True", "true", True):
        script_input["vtap_dtls"] = True
    else:
        script_input["vtap_dtls"] = False
        
    return script_input


def get_int_value(value):

    try:
        int_value = int(value)
    except ValueError:
        if value == "True" or value == "true" or value == "TRUE":
            int_value = True
        elif value == "False" or value == "false" or value == "FALSE":
            int_value = False
        else:
            int_value = value

    return int_value

def update_value(y_dict, set_var):
    set_var = set_var.strip()

    set_var = set_var.split('=')
    path = set_var[0]
    value =  get_int_value(set_var[1])

    #dpath limitation: cannot create full tree only 1 level
    try:
        dpath.get(y_dict, path, '.')
    except KeyError:
        # if the path doesn't exist create a new one
        dpath.new(y_dict, path, value, '.')
    else:
        # else just update the value
        dpath.set(y_dict, path, value, '.')
    return y_dict

def get_remoteseeds(namespace):
    rmsd1 = None
    rmsd2 = None

    config.load_kube_config(os.environ.get('KUBECONFIG'))
    v1 = client.CoreV1Api()
    svcs=v1.list_namespaced_service(namespace)

    if svcs.items:
        for svc in svcs.items:
            if(svc.metadata.name == "eric-bsf-wcdb-cd-datacenter1-rack1-0"
            or svc.metadata.name == "eric-bsf-wcdb-cd-datacenter2-rack1-0"):
                rmsd1=svc.status.load_balancer.ingress[0].ip
            if(svc.metadata.name == "eric-bsf-wcdb-cd-datacenter1-rack1-1"
            or svc.metadata.name == "eric-bsf-wcdb-cd-datacenter2-rack1-1"):
                rmsd2=svc.status.load_balancer.ingress[0].ip

    return rmsd1, rmsd2


def get_address_pools(dc):

    config.load_kube_config(os.environ.get('KUBECONFIG'))
    v1 = client.CoreV1Api()

    supported_cluster_cm = [
        "metallb-config", #mc-ipv4-5576, bsf-geo-red
        "ecfe-ccdadm", #n293-cnis
        "ccm-config" #hall131
    ]

    config_map = next(filter(lambda cm: ( cm.metadata.name in supported_cluster_cm),  v1.list_namespaced_config_map("kube-system").items), None)

    if config_map is None:
        print("No supported configmap found. Is this a new cluster for geored?")
        sys.exit(101)


    if config_map.metadata.name == "ccm-config":
        return "pool0"

    config_data=yaml.safe_load(config_map.data["config"])

    address_pools=[]
    for pool in config_data["address-pools"]:
        address_pools.append(pool["name"])

    if len(address_pools) == 0:
        print("No address pools found in configmap")
        sys.exit(102)
    elif len(address_pools) == 1:
        return address_pools[0]

    if dc == "DC-1":
        return address_pools[1]
    elif dc == "DC-2":
        return address_pools[2]
    else:
        return address_pools[0]


def generate_geored_dc_config(mode, dc1_namespace):

    print("Generating for " + mode)
    # Load default geored config
    default_geo_cfg = read_yaml(HOME+"/profiles/eric-sc-bsf/bsf-geored.yaml")

    if mode == "DC-1":
        datacenter = "datacenter1"
    else:
        datacenter = "datacenter2"

    # Set datacenter name for bsf
    update_value(default_geo_cfg,"eric-bsf.cassandra.datacenter=" + datacenter)

    # Set datacenter name for bsf-diameter
    update_value(default_geo_cfg,"eric-bsf-diameter.cassandra.datacenter=" + datacenter)

    # Set wide-column datacenter name
    update_value(default_geo_cfg,"eric-bsf-wcdb-cd.dataCenters.0.name=" + datacenter)

    # Get addressPoolName for dc1
    addr_pool = get_address_pools(mode)

    # Set wide-column datacenter poolName
    update_value(default_geo_cfg,"eric-bsf-wcdb-cd.dataCenters.0.service.externalIP.annotations.addressPoolName="+addr_pool)


    # Get remoteSeedNodes of DC-1
    # Applicable only for generating configuration for DC-2
    if dc1_namespace != None:
        remote_seed1, remote_seed2 = get_remoteseeds(dc1_namespace)

        # If datacenter1 is deployed and has remoteSeeds update remoteSeedNodes of datacenter2.
        if (remote_seed1 != None and remote_seed2 != None):
            dpath.new(default_geo_cfg, 'eric-bsf-wcdb-cd/cassandra/remoteSeedNodes', [])
            dpath.new(default_geo_cfg, 'eric-bsf-wcdb-cd/cassandra/remoteSeedNodes/0', remote_seed1)
            dpath.new(default_geo_cfg, 'eric-bsf-wcdb-cd/cassandra/remoteSeedNodes/1', remote_seed2)
        else:
            print("Cannot find RemoteSeedNodes of DC-1! \nExiting...")
            exit(1)

    return default_geo_cfg


def merge_profiles(profiles):
    path = PROFILES_FOLDER+"/"+SC_BSF_NAME+"/"
    root_profile = read_yaml(path+profiles[0])

    dc_name=""

    if script_input["dc1_namespace"] == script_input["namespace"]:
        dc_name="-dc1"
    if script_input["dc2_namespace"] == script_input["namespace"]:
        dc_name="-dc2"
    for profile in profiles[1:]:
        file_path = path + profile
        if os.path.exists(file_path):
            profile_dict=read_yaml(file_path)
            dpath.merge(root_profile,profile_dict,flags=MergeType.REPLACE)

    yaml_output = open(script_input["output_path"] + "/" + "values-final"+dc_name+".yaml", 'w')
    yaml.dump(root_profile, yaml_output,default_flow_style=False)

"""
Based on the provided input profile parameters, it prepare a list
of all profile files for the respective integration helm chart
"""
def add_profiles() -> list:
    profiles = []

    profiles.append(DEFAULT_FILE)

    if not script_input["metrics"]:
        profiles.append(NO_METRICS_FILE)
    if not script_input["high_availability"]:
        profiles.append(NO_HIGH_AVAILABILITY_FILE)

    if script_input["syslog"]:
        profiles.append(SYSLOG_FILE)
    if script_input["pm_remote_write"]:
        profiles.append(PM_RW_FILE)
    if script_input["lumberjack"]:
        profiles.append(LJ_SINGLE_FILE)
    if script_input["lumberjack_m"]:
        profiles.append(LJ_MULTIPLE_FILE)
    if script_input["rop"]:
        profiles.append(ROP_FILES_OPT2_FILE)
    if script_input["lb_np"]:
        profiles.append(LB_NP_FILE)
    if script_input["vtap"]:
        profiles.append(VTAP_FILE)
    if script_input["vtap_l"]:
        profiles.append(VTAP_LITE_FILE)
    if script_input["multi_vpn"]:
        profiles.append(MULTI_VPN_FILE)
    if script_input["nlf"]:
        profiles.append(NLF_FILE)
    if script_input["rlf"]:
        profiles.append(RLF_FILE)
    if script_input["bsf_diameter"]:
        profiles.append(BSF_DIAMETER_FILE)
    if script_input["vtap_dtls"]:
        profiles.append(VTAP_DTLS_FILE)
    if script_input["slf"]:
        profiles.append(SLF_FILE)

    if script_input["logging"]:
        match script_input["log_stream"]:
            case "dual":
                profiles.append(LOG_STREAMING_DUAL_FILE)
            case "indirect":
                profiles.append(LOG_STREAMING_INDIRECT_FILE)
            case "direct":
                profiles.append(LOG_STREAMING_DIRECT_FILE)
            case default:
                exit_and_fail(script_input["log_stream"] + " is an invalid log streaming value! Supported values for LOG_STREAMING: direct, indirect, dual.")
    else:
        profiles.append(NO_LOGGING_FILE)

    if script_input["ip_version_int"] is not None:
        if (script_input["ip_version_int"] == "4"):
            profiles.append(IPV4_INT_FILE)
        elif (script_input["ip_version_int"] == "6"):
            profiles.append(IPV6_INT_FILE)
        else:
            profiles.append(DS_INT_FILE)

    if script_input["ip_version_ext"] is not None:
        if (script_input["ip_version_ext"] == "4"):
            profiles.append(IPV4_EXT_FILE)
        elif (script_input["ip_version_ext"] == "6"):
            profiles.append(IPV6_EXT_FILE)
        else:
            profiles.append(DS_EXT_FILE)
            
    if script_input["resources"] != "full":
        if script_input["resources"] == "ucc":
            profiles.append(REDUCED_RESOURCES_FILE)
        profiles.append(script_input["resources"] + "_resources.yaml")

    if script_input["shared_dbpg"]:
        profiles.append(SHARED_DBPG_FILE)
        
    if script_input["dc1_namespace"] is not None :
        print("Adding geored profile")
        profiles.append(BSF_GEORED_FILE)

    return profiles


"""
For the respective integration chart it creates a bob variable with name "var.<IHC_NAME>-profiles"
that contains a string of the form: "-f <profile_1.yaml> -f <profile_2.yaml> ... -f <profile_N.yaml>",
which contains all profiles' files to be supplied during helm install. The bob variable of each
integration chart is defined in the rulesets/ruleset2.0-eric-sc-configuration.yaml.
"""
def create_bob_var(profiles, ihc_name) -> None:
    ihc_profile_folder = PROFILES_FOLDER + "/" + ihc_name
    ihc_bob_var_name = "var." + ihc_name + "-" + BOB_VAR_SUFFIX
    with open(script_input["output_path"] + "/" + ihc_bob_var_name, 'w') as out_file:
        for file in profiles:
            file_path = ihc_profile_folder + "/" + file
            if os.path.exists(file_path):
                out_file.write(" -f " + file_path)

"""
Writes the final values to an output file
"""
def write_final_values(out_file_name:str, main_values):
    yaml_output = open(script_input["output_path"] + "/" + out_file_name, 'w')
    yaml.dump(main_values, yaml_output, default_flow_style=False)


"""
1) Creates cncs-base final values yaml
2) Prepares cncs-base deployment profiles
"""
def cncs_base_actions(main_values):

    """
    Prepare profiles
    """
    # Create bob variable with additional feature files
    create_bob_var(add_profiles(), CNCS_BASE_NAME)

    """
    Prepare final values
    """

    # Configure ah ingress hostname
    update_value(main_values, "eric-fh-snmp-alarm-provider.ingress.hostname=snmp."+script_input["namespace"]+"."+script_input["env"]+".rnd.gic.ericsson.se")

    # Add services' configmaps for pm-server
    pm_rr_config_folder = HOME + "/" + DYNAMIC_CONFIG_FOLDER + "/" + CNCS_BASE_NAME + "/" + "pm-recording-rules/"
    sccs_pm_rr_config = read_yaml(pm_rr_config_folder + "sc-cs.yaml")
    dpath.merge(main_values, sccs_pm_rr_config, flags=MergeType.ADDITIVE)
    if BSF_ENABLED:
        bsf_pm_rr_config = read_yaml(pm_rr_config_folder + "sc-bsf.yaml")
        dpath.merge(main_values, bsf_pm_rr_config, flags=MergeType.ADDITIVE)
        if script_input["bsf_diameter"]:
            bsfdiameter_pm_rr_config = read_yaml(pm_rr_config_folder + "bsf-diameter.yaml")
            dpath.merge(main_values, bsfdiameter_pm_rr_config, flags=MergeType.ADDITIVE)
    if SCP_ENABLED:
        scp_pm_rr_config = read_yaml(pm_rr_config_folder + "scp.yaml")
        dpath.merge(main_values, scp_pm_rr_config, flags=MergeType.ADDITIVE)
        if script_input["slf"]:
            slf_pm_rr_config = read_yaml(pm_rr_config_folder + "slf.yaml")
            dpath.merge(main_values, slf_pm_rr_config, flags=MergeType.ADDITIVE)
    if SEPP_ENABLED:
        sepp_pm_rr_config = read_yaml(pm_rr_config_folder + "sepp.yaml")
        dpath.merge(main_values, sepp_pm_rr_config, flags=MergeType.ADDITIVE)
    if DIAMETER_ENABLED:
        dsc_pm_rr_config = read_yaml(pm_rr_config_folder + "dsc.yaml")
        dpath.merge(main_values, dsc_pm_rr_config, flags=MergeType.ADDITIVE)
    if script_input["rlf"]:
        rlf_pm_cm_config = read_yaml(pm_rr_config_folder + "rlf.yaml")
        dpath.merge(main_values, rlf_pm_cm_config, flags=MergeType.ADDITIVE)
    if script_input["nlf"]:
        nlf_pm_cm_config = read_yaml(pm_rr_config_folder + "nlf.yaml")
        dpath.merge(main_values, nlf_pm_cm_config, flags=MergeType.ADDITIVE)
    if script_input["vtap"]:
        pvtb_pm_cm_config = read_yaml(pm_rr_config_folder + "pvtb.yaml")
        dpath.merge(main_values, pvtb_pm_cm_config, flags=MergeType.ADDITIVE)

    # Write the updated based values in the output folder
    write_final_values(CNCS_BASE_NAME + "-" + FINAL_VALUES_SUFFIX, main_values)


"""
1) Creates cncs-additions final values yaml
2) Prepares cncs-additions deployment profiles
"""
def cncs_add_actions(main_values):
    """
    Prepare profiles
    """
    # Create bob variable with additional feature files
    create_bob_var(add_profiles(), CNCS_ADD_NAME)

    """
    Prepare final values
    """
    # Write the updated based values in the output folder
    write_final_values(CNCS_ADD_NAME + "-" + FINAL_VALUES_SUFFIX, main_values)


"""
1) Creates sc-cs final values yaml
2) Prepares sc-cs deployment profiles
"""
def sc_cs_actions(main_values):

    """
    Prepare profiles
    """
    # Create bob variable with additional feature files
    create_bob_var(add_profiles(), SC_CS_NAME)

    """
    Prepare final values
    """
    # Configure nbi fqdn based on the deployment namespace
    ingress_host = read_var(script_input["output_path"] + "/var.ingress-host")
    ingress_host = ingress_host.strip('"')
    if ingress_host:
        update_value(main_values, "ingress.nbi.fqdn=nbi."+ingress_host)
    
    # Configure which NF's certificates sc-manager should monitor
    update_value(main_values, "eric-sc-manager.features.monitoring.certificates.functions.bsf="+str(BSF_ENABLED))
    update_value(main_values, "eric-sc-manager.features.monitoring.certificates.functions.scp="+str(SCP_ENABLED))
    update_value(main_values, "eric-sc-manager.features.monitoring.certificates.functions.sepp="+str(SEPP_ENABLED))

    # Add services' severities configmaps for hcagent
    hcagent_severities_folder = HOME + "/" + DYNAMIC_CONFIG_FOLDER + "/" + SC_CS_NAME + "/" + "hcagent-severities/"
    dpath.merge(main_values, read_yaml(hcagent_severities_folder + "sc-cs.yaml"), flags=MergeType.ADDITIVE)
    if BSF_ENABLED:
        bsf_sev_config = read_yaml(hcagent_severities_folder + "sc-bsf.yaml")
        dpath.merge(main_values, bsf_sev_config, flags=MergeType.ADDITIVE)
    if SCP_ENABLED:
        scp_sev_config = read_yaml(hcagent_severities_folder + "sc-scp.yaml")
        dpath.merge(main_values, scp_sev_config, flags=MergeType.ADDITIVE)
    if SEPP_ENABLED:
        sepp_sev_config = read_yaml(hcagent_severities_folder + "sc-sepp.yaml")
        dpath.merge(main_values, sepp_sev_config, flags=MergeType.ADDITIVE)
    if DIAMETER_ENABLED:
        diameter_sev_config = read_yaml(hcagent_severities_folder + "dsc.yaml")
        dpath.merge(main_values, diameter_sev_config, flags=MergeType.ADDITIVE)

    # Add services' configmaps for recording rules in SC-CS prometheus configmap
    sccs_rr_config_folder = HOME + "/" + DYNAMIC_CONFIG_FOLDER + "/" + SC_CS_NAME + "/" + "pm-recording-rules/"
    sccs_pm_rr_config = read_yaml(sccs_rr_config_folder + "sc-cs.yaml")
    dpath.merge(main_values, sccs_pm_rr_config, flags=MergeType.ADDITIVE)
    if BSF_ENABLED:
        bsf_pm_rr_config = read_yaml(sccs_rr_config_folder + "sc-bsf.yaml")
        dpath.merge(main_values, bsf_pm_rr_config, flags=MergeType.ADDITIVE)
        if script_input["bsf_diameter"]:
            bsfdiameter_pm_rr_config = read_yaml(sccs_rr_config_folder + "bsf-diameter.yaml")
            dpath.merge(main_values, bsfdiameter_pm_rr_config, flags=MergeType.ADDITIVE)
    if SCP_ENABLED:
        scp_pm_rr_config = read_yaml(sccs_rr_config_folder + "scp.yaml")
        dpath.merge(main_values, scp_pm_rr_config, flags=MergeType.ADDITIVE)
        if script_input["slf"]:
            slf_pm_rr_config = read_yaml(sccs_rr_config_folder + "slf.yaml")
            dpath.merge(main_values, slf_pm_rr_config, flags=MergeType.ADDITIVE)
    if SEPP_ENABLED:
        sepp_pm_rr_config = read_yaml(sccs_rr_config_folder + "sepp.yaml")
        dpath.merge(main_values, sepp_pm_rr_config, flags=MergeType.ADDITIVE)
    if DIAMETER_ENABLED:
        dsc_pm_rr_config = read_yaml(sccs_rr_config_folder + "dsc.yaml")
        dpath.merge(main_values, dsc_pm_rr_config, flags=MergeType.ADDITIVE)
    if script_input["rlf"]:
        rlf_pm_cm_config = read_yaml(sccs_rr_config_folder + "rlf.yaml")
        dpath.merge(main_values, rlf_pm_cm_config, flags=MergeType.ADDITIVE)
    if script_input["nlf"]:
        nlf_pm_cm_config = read_yaml(sccs_rr_config_folder + "nlf.yaml")
        dpath.merge(main_values, nlf_pm_cm_config, flags=MergeType.ADDITIVE)
    if script_input["vtap"]:
        pvtb_pm_cm_config = read_yaml(sccs_rr_config_folder + "pvtb.yaml")
        dpath.merge(main_values, pvtb_pm_cm_config, flags=MergeType.ADDITIVE)

    # write the updated based values in the output folder
    write_final_values(SC_CS_NAME + "-" + FINAL_VALUES_SUFFIX, main_values)


"""
1) Creates sc-scp final values yaml
2) Prepares sc-scp deployment profiles
"""
def sc_scp_actions(main_values):
    """
    Prepare profiles
    """
    # Create bob variable with additional feature files
    create_bob_var(add_profiles(), SC_SCP_NAME)

    """
    Prepare final values
    """
    # write the updated based values in the output folder
    write_final_values(SC_SCP_NAME + "-" + FINAL_VALUES_SUFFIX, main_values)


"""
1) Creates sc-sepp final values yaml
2) Prepares sc-sepp deployment profiles
"""
def sc_sepp_actions(main_values):
    """
    Prepare profiles
    """
    # Create bob variable with additional feature files
    create_bob_var(add_profiles(), SC_SEPP_NAME)

    """
    Prepare final values
    """
    # Write the updated based values in the output folder
    write_final_values(SC_SEPP_NAME + "-" + FINAL_VALUES_SUFFIX, main_values)


"""
1) Creates sc-dsc final values yaml
2) Prepares sc-dsc deployment profiles
"""
def sc_dsc_actions(main_values):
    """
    Prepare profiles
    """
    # Create bob variable with additional feature files
    create_bob_var(add_profiles(), SC_DSC_NAME)

    """
    Prepare final values
    """
    # Write the updated based values in the output folder
    write_final_values(SC_DSC_NAME + "-" + FINAL_VALUES_SUFFIX, main_values)


"""
1) Creates sc-bsf final values yaml
2) Prepares sc-bsf deployment profiles
"""
def sc_bsf_actions(main_values):
    profiles_for_bsf = add_profiles()
    create_bob_var(profiles_for_bsf, SC_BSF_NAME)
    
    ############ Enable diameter #########
    update_value(main_values, "eric-bsf-diameter.enabled="+str(script_input["bsf_diameter"]))
    update_value(main_values, "eric-stm-diameter.enabled="+str(script_input["bsf_diameter"]))
    if script_input["bsf_diameter"]:
        update_value(main_values, "eric-stm-diameter.initialConfig.dsl.pvtbClient.enabled="+str(script_input["vtap"]))
    ################### END #######################
    
    ################################ GEORED CONFIG #############################
    if script_input["dc1_namespace"] == None:
        print("GeoRed is disabled")
    # Generate for DC-1
    elif script_input["dc1_namespace"] == script_input["namespace"]:

        # Get remoteSeedNodes of DC-2
        remote_seed1, remote_seed2 = get_remoteseeds(script_input["dc2_namespace"])

        # If datacenter2 is deployed and has remoteSeeds update remoteSeedNodes of datacenter1.
        if (remote_seed1 == None and remote_seed2 == None):
            print("Datacenter2 is not deployed yet.\nGenerating configuration for Datacenter1 without remoteSeedNodes.")

            # Generate configuration for Datacenter1. Datacenter2 is not deployed so remoteSeedNodes are not needed.
            default_geo_cfg = generate_geored_dc_config("DC-1", None)

            # Apply configuration for Datacenter1
            # Save values also as dc1 for debug and reuse

            yaml_output = open(PROFILES_FOLDER + "/" + SC_BSF_NAME + "/" + BSF_GEORED_FILE,'w')
            yaml.dump(default_geo_cfg, yaml_output,default_flow_style=False)
            yaml_output_dc1 = open(PROFILES_FOLDER + "/" + SC_BSF_NAME + "/bsf-dc1-profile.yaml",'w')
            yaml.dump(default_geo_cfg, yaml_output_dc1,default_flow_style=False)
        else:
            print("Updating remoteSeedNodes for Datacenter1.")
            # Load values.yaml that used for dc-1 deployment
            geored_profile_dc1 = read_yaml(PROFILES_FOLDER + "/" + SC_BSF_NAME + "/bsf-dc1-profile.yaml")

            # Update remoteSeedNodes
            dpath.new(geored_profile_dc1, 'eric-bsf-wcdb-cd/cassandra/remoteSeedNodes', [])
            dpath.new(geored_profile_dc1, 'eric-bsf-wcdb-cd/cassandra/remoteSeedNodes/0', remote_seed1)
            dpath.new(geored_profile_dc1, 'eric-bsf-wcdb-cd/cassandra/remoteSeedNodes/1', remote_seed2)

            yaml_output = open(PROFILES_FOLDER + "/" + SC_BSF_NAME + "/" + BSF_GEORED_FILE,'w')
            yaml.dump(geored_profile_dc1, yaml_output,default_flow_style=False)
            yaml_output_dc1 = open(PROFILES_FOLDER + "/" + SC_BSF_NAME + "/bsf-dc1-profile.yaml",'w')
            yaml.dump(geored_profile_dc1, yaml_output_dc1,default_flow_style=False)
    # Generate for DC-2
    elif script_input["dc2_namespace"] == script_input["namespace"]:

        default_geo_cfg = generate_geored_dc_config("DC-2", script_input["dc1_namespace"])

        # Apply configuration for Datacenter2
        # Save values of dc2 for debug
        yaml_output = open(PROFILES_FOLDER + "/" + SC_BSF_NAME + "/" + BSF_GEORED_FILE,'w')
        yaml.dump(default_geo_cfg, yaml_output,default_flow_style=False)
        yaml_output_dc2 = open(PROFILES_FOLDER + "/" + SC_BSF_NAME + "/bsf-dc2-profile.yaml",'w')
        yaml.dump(default_geo_cfg, yaml_output_dc2,default_flow_style=False)
    else:
        print("GEORED is enabled with dc1_namespace: " +script_input["dc1_namespace"] + " and dc2_namespace: " + script_input["dc2_namespace"] + " while current namespace is: " + script_input["namespace"])
    ############################# END OF GEORED ################################

    out_file_name = SC_BSF_NAME + "-" + FINAL_VALUES_SUFFIX
    yaml_output = open(script_input["output_path"] + "/" + out_file_name, 'w')
    yaml.dump(main_values, yaml_output,default_flow_style=False)
    merge_profiles(profiles_for_bsf)


def run_ihc_actions():
    main_file = script_input["input_path"]
    main_values = read_yaml(main_file)

    if script_input["ihc"] == CNCS_BASE_NAME:
        cncs_base_actions(main_values)
    elif script_input["ihc"] == CNCS_ADD_NAME:
        cncs_add_actions(main_values)
    elif script_input["ihc"] == SC_CS_NAME:
        sc_cs_actions(main_values)
    elif script_input["ihc"] == SC_BSF_NAME:
        sc_bsf_actions(main_values)
    elif script_input["ihc"] == SC_SCP_NAME:
        sc_scp_actions(main_values)
    elif script_input["ihc"] == SC_SEPP_NAME:
        sc_sepp_actions(main_values)
    elif script_input["ihc"] == SC_DSC_NAME:
        sc_dsc_actions(main_values)
    else:
        exit_and_fail("The input values file does not belong to any integration helm chart")

BSF_ENABLED = os.getenv('BSF')
SCP_ENABLED = os.getenv('SCP')
SEPP_ENABLED = os.getenv('SEPP')
DIAMETER_ENABLED = os.getenv('DIAMETER')

if BSF_ENABLED in ("TRUE", "True", "true", True):
    BSF_ENABLED = True
else:
    BSF_ENABLED = False

if SCP_ENABLED in ("TRUE", "True", "true", True):
    SCP_ENABLED = True
else:
    SCP_ENABLED = False

if SEPP_ENABLED in ("TRUE", "True", "true", True):
    SEPP_ENABLED = True
else:
    SEPP_ENABLED = False

if DIAMETER_ENABLED in ("TRUE", "True", "true", True):
    DIAMETER_ENABLED = True
else:
    DIAMETER_ENABLED = False

script_input = check_args()
final_values = run_ihc_actions()

print("Yaml generation complete!")
