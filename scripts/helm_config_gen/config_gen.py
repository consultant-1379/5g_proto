#!/usr/bin/env python3.10
import os
import logging
import sys
import yaml
import dpath
from dpath.types import MergeType
from argparse import ArgumentParser, RawTextHelpFormatter
import textwrap
from kubernetes import client, config

log = logging.getLogger(__name__)
HOME = os.path.abspath( os.path.dirname( __file__ ) )

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

    parser.add_argument("-n", dest="namespace", help="Set the namespace to be used.", required=True)
    parser.add_argument("-i", "--input", dest="input_path", help="Set the path where eric-sc-values.yaml exists. This will be used as main template", required=True)    
    parser.add_argument("-o", "--output", dest="output_path", help="Set the output path where the generated .yaml file will be placed", required=True)    
    parser.add_argument("-e", dest="env", help="Set the deployment environment", required=True)
    parser.add_argument("-f", "--file", dest="files", action="append", help=file_help, required=False)
    parser.add_argument("-s", "--set", dest="sets", action="append", help="\nSet a parameter value that needs to be added in the generated yaml file", required=False)
    parser.add_argument("-c", dest="customer", help="Set this parameter to true if customer setting will be used for deployment", required=False)
    parser.add_argument("-r", "--resources", dest="resources", help="Define what resource template to use.")
    parser.add_argument("--ip-version-ext", dest="ip_version_ext", help="Define what IP Version (4 or 6) to use for external interfaces.")
    parser.add_argument("--ip-version-int", dest="ip_version_int", help="Define what IP Version (4 or 6) to use for internal interfaces.")
    parser.add_argument("--pvtb", dest="pvtb", help="Set this to enable PVTB.", required=False)
    parser.add_argument("--tap", dest="tapagent", help="Set this to enable the vTAP agent.", required=False)
    parser.add_argument("--tapcol", dest="tapcollector", help="Set this to enable the tap-collector.", required=False)
    parser.add_argument("--scp-tls", dest="scp_tls", help="Set this parameter to true for enabling tls on scp", required=False)
    parser.add_argument("--dced-tls", dest="dced_tls", help="Set this parameter to true for enabling tls on dced", required=False)
    parser.add_argument("-dc1", "--datacenter1", dest="dc1", help="Set this to define namespace for geored datacenter1.", required=False)
    parser.add_argument("-dc2", "--datacenter2", dest="dc2", help="Set this to define geored datacenter2 to generate values for.", required=False)
    parser.add_argument("--sepp-secrets", dest="sepp_secrets", help="Set this parameter to true for enabling creation of additional secrets for sepp (CI ONLY)", required=False)
    parser.add_argument("--pm-rw", dest="pm_remote_write", help="Set this parameter to true for enabling internal pm remote write to internal influx database", required=False)
    parser.add_argument("--metrics", dest="metrics", help="Set this to false to disable all metrics sidecars.", required=False)
    # parser.add_argument("--lm-asih", dest="lm_asih", help="Set this parameter to false for random generation of application-id by lm instead of fetching it from asih", required=False)
    script_input = dict()

    # check if input path is valid
    if not os.path.exists(parser.parse_args().input_path):
        exit_and_fail("The input path: " + str(parser.parse_args().input_path) + " is not valid!")
    script_input["input_path"] = parser.parse_args().input_path

    # check if output path is valid
    if not os.path.exists(parser.parse_args().output_path):
        exit_and_fail("The output path: " + str(parser.parse_args().output_path) + " is not valid!")
    script_input["output_path"] = parser.parse_args().output_path

           
    # parse scp-tls variable and use boolean value
    if parser.parse_args().scp_tls in ("TRUE", "True", "true", True):
        script_input["scp_tls"] = True
    else:
        script_input["scp_tls"] = False
        
    # parse pm-rw variable and use boolean value
    if parser.parse_args().pm_remote_write in ("TRUE", "True", "true", True):
        script_input["pm_remote_write"] = True
    else:
        script_input["pm_remote_write"] = False

    # parse lm-asih variable and use boolean value
    # if parser.parse_args().lm_asih in ("TRUE", "True", "true", True):
        # script_input["lm_asih"] = True
    # else:
        # script_input["lm_asih"] = False

    #parse sepp-secret variable and use boolean value
    if parser.parse_args().sepp_secrets in ("False", "False", "false", False):
        script_input["sepp_secrets"] = True
    else:
        script_input["sepp_secrets"] = False

    # parse customer variable and use boolean value
    if parser.parse_args().customer in ("TRUE", "True", "true", True):
        script_input["customer"] = True
    else:
        script_input["customer"] = False

    # parse tap variable and use boolean value
    if parser.parse_args().tapagent in ("TRUE", "True", "true", True):
        script_input["tapagent"] = True
    else:
        script_input["tapagent"] = False

    # parse tap variable and use boolean value
    if parser.parse_args().pvtb in ("TRUE", "True", "true", True):
        script_input["pvtb"] = True
    else:
        script_input["pvtb"] = False

    if parser.parse_args().tapcollector in ("TRUE", "True", "true", True):
        script_input["tapcollector"] = True
    else:
        script_input["tapcollector"] = False

    script_input["resources"] = parser.parse_args().resources

    script_input["ip_version_ext"] = parser.parse_args().ip_version_ext
    script_input["ip_version_int"] = parser.parse_args().ip_version_int

    script_input["namespace"] = parser.parse_args().namespace

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

    if parser.parse_args().metrics in ("TRUE", "True", "true", True):
        script_input["metrics"] = True
    else:
        script_input["metrics"] = False
        
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
            if(svc.metadata.name == "eric-data-wide-column-database-cd-datacenter1-rack1-0"
            or svc.metadata.name == "eric-data-wide-column-database-cd-datacenter2-rack1-0"):
                rmsd1=svc.status.load_balancer.ingress[0].ip
            if(svc.metadata.name == "eric-data-wide-column-database-cd-datacenter1-rack1-1"
            or svc.metadata.name == "eric-data-wide-column-database-cd-datacenter2-rack1-1"):
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
        return address_pools[0]
    elif dc == "DC-2":
        return address_pools[1]
    else:
        return address_pools[0]

def generate_geored_dc_config(mode, dc1_namespace):
    
    print("Generating for " + mode)
    # Load default geored config
    default_geo_cfg = read_yaml(HOME+"/templates/geored-values.yaml")

    if mode == "DC-1":
        datacenter = "datacenter1"
    else:
        datacenter = "datacenter2"

    # Set contact points for bsf
    update_value(default_geo_cfg,"eric-bsf.cassandra.contact_point=eric-data-wide-column-database-cd-" + datacenter + "-rack1:9042")
    update_value(default_geo_cfg,"eric-bsf.cassandra.datacenter=" + datacenter)

    # Set contact points for bsf-diameter
    update_value(default_geo_cfg,"eric-bsf-diameter.cassandra.contact_point=eric-data-wide-column-database-cd-" + datacenter + "-rack1:9042")
    update_value(default_geo_cfg,"eric-bsf-diameter.cassandra.datacenter=" + datacenter)

    # Set wide-column datacenter name
    update_value(default_geo_cfg,"eric-data-wide-column-database-cd.dataCenters.0.name=" + datacenter)

    # Get addressPoolName for dc1
    addr_pool = get_address_pools(mode)

    # Set wide-column datacenter poolName
    update_value(default_geo_cfg,"eric-data-wide-column-database-cd.dataCenters.0.service.externalIP.annotations.addressPoolName="+addr_pool)


    # Get remoteSeedNodes of DC-1
    # Applicable only for generating configuration for DC-2
    if dc1_namespace != None:
        remote_seed1, remote_seed2 = get_remoteseeds(dc1_namespace)
        
        # If datacenter1 is deployed and has remoteSeeds update remoteSeedNodes of datacenter2.
        if (remote_seed1 != None and remote_seed2 != None):
            dpath.new(default_geo_cfg, 'eric-data-wide-column-database-cd/cassandra/remoteSeedNodes', [])
            dpath.new(default_geo_cfg, 'eric-data-wide-column-database-cd/cassandra/remoteSeedNodes/0', remote_seed1)
            dpath.new(default_geo_cfg, 'eric-data-wide-column-database-cd/cassandra/remoteSeedNodes/1', remote_seed2)
        else:
            print("Cannot find RemoteSeedNodes of DC-1! \nExiting...")
            exit(1)

    return default_geo_cfg

script_input = check_args()
      
mainValues = read_yaml(script_input["input_path"] + "/eric-sc-values.yaml")

# apply deployment enviroment's settings
default_settings = read_yaml(HOME+"/templates/values-default.yaml")
dpath.merge(mainValues, default_settings, flags=MergeType.REPLACE)
    
if script_input["customer"] == True:
    if script_input["resources"] == "micro":
        resource_profile = "values-kaas-micro-diff.yaml"
    elif script_input["resources"] == "mini":
        resource_profile = "values-kaas-mini-diff.yaml"            
    elif script_input["resources"] == "normal":
        resource_profile = "values-kaas-diff.yaml"    
    else:
        resource_profile = "values-kaas-full-diff.yaml"    

    ingress_host = read_var(script_input["output_path"] + "/var.ingress-host")
    ingress_host = ingress_host.strip('"')
    
    env_settings = read_yaml(HOME+"/templates/"+resource_profile)
    dpath.merge(mainValues,env_settings, flags=MergeType.REPLACE)
    
    script_input["files"] = None
    
    update_value(mainValues, "eric-sc.routes.nbi.fqdn=nbi."+ingress_host)
    
    # overwrite alarm-handler parameters
    update_value(mainValues, "eric-fh-alarm-handler.imageCredentials.alarmhandler.registry.url=selndocker.mo.sw.ericsson.se")
    update_value(mainValues, "eric-fh-alarm-handler.imageCredentials.topiccreator.registry.url=selndocker.mo.sw.ericsson.se")
    update_value(mainValues, "eric-fh-alarm-handler.imageCredentials.hooklauncher.registry.url=selndocker.mo.sw.ericsson.se")
    update_value(mainValues, "eric-fh-alarm-handler.imageCredentials.pullSecret=regcred")
    
    # overwrite snmp alarm-provider parameters
    update_value(mainValues, "eric-fh-snmp-alarm-provider.ingress.enabled=true")
    update_value(mainValues, "eric-fh-snmp-alarm-provider.ingress.hostname=snmp."+script_input["namespace"]+"."+script_input["env"]+".rnd.gic.ericsson.se")
    update_value(mainValues, "eric-fh-snmp-alarm-provider.service.secretName=snmp-alarm-provider-config")
    update_value(mainValues, "eric-fh-snmp-alarm-provider.imageCredentials.snmpAP.registry.url=selndocker.mo.sw.ericsson.se")
    update_value(mainValues, "eric-fh-snmp-alarm-provider.imageCredentials.hooklauncher.registry.url=selndocker.mo.sw.ericsson.se")
    update_value(mainValues, "eric-fh-snmp-alarm-provider.imageCredentials.pullSecret=regcred")
    
    # overwrite cnom parameters
    update_value(mainValues, "eric-cnom-server.imageCredentials.server.registry.url=serodocker.sero.gic.ericsson.se")
    update_value(mainValues, "eric-cnom-server.imageCredentials.hooklauncher.registry.url=serodocker.sero.gic.ericsson.se")
    update_value(mainValues, "eric-cnom-server.imageCredentials.pullSecret=regcred")
    
    # overwrite osmn parameters
    update_value(mainValues, "eric-data-object-storage-mn.imageCredentials.init.registry.url=serodocker.sero.gic.ericsson.se")
    update_value(mainValues, "eric-data-object-storage-mn.imageCredentials.osmn.registry.url=serodocker.sero.gic.ericsson.se")
    update_value(mainValues, "eric-data-object-storage-mn.imageCredentials.kes.registry.url=serodocker.sero.gic.ericsson.se")
    
    # overwrite lm combined server parameters
    update_value(mainValues, "eric-lm-combined-server.licenseServerClient.timer.licenseRemovalGracePeriod=0")
    update_value(mainValues, "eric-lm-combined-server.licenseServerClient.licenseServer.thrift.host=eric-test-nels-simulator")

    # overwrite pvtb parameters
    update_value(mainValues, "eric-probe-virtual-tap-broker.imageCredentials.VirtualTapBroker.registry.url=selndocker.mo.sw.ericsson.se")
    update_value(mainValues, "eric-probe-virtual-tap-broker.imageCredentials.pullSecret=regcred")

    # overwrite eric-odca-diagnostic-data-collector parameters
    update_value(mainValues, "eric-odca-diagnostic-data-collector.imageCredentials.registry.url=serodocker.sero.gic.ericsson.se")
    update_value(mainValues, "eric-odca-diagnostic-data-collector.imageCredentials.ddc.registry.url=serodocker.sero.gic.ericsson.se")
    update_value(mainValues, "eric-odca-diagnostic-data-collector.imageCredentials.hooklauncher.registry.url=serodocker.sero.gic.ericsson.se")

    # overwrite eric-data-sftp-server parameters
    update_value(mainValues, "eric-data-sftp-server.imageCredentials.sftp.registry.url=serodocker.sero.gic.ericsson.se")

else:
    if script_input["resources"] == "micro":
        resource_profile = "values-kaas-micro-diff.yaml"
    elif script_input["resources"] == "mini":
        resource_profile = "values-kaas-mini-diff.yaml"            
    elif script_input["resources"] == "normal":
        resource_profile = "values-kaas-diff.yaml"    
    else:
        resource_profile = "values-kaas-full-diff.yaml"    
    
    ingress_host = read_var(script_input["output_path"] + "/var.ingress-host")    
    ingress_host = ingress_host.strip('"')
    env_settings = read_yaml(HOME+"/templates/values-minikube-diff.yaml") if script_input["env"] == "minikube"  else  read_yaml(HOME+"/templates/"+resource_profile)
    dpath.merge(mainValues,env_settings, flags=MergeType.REPLACE)
    
    # overwrite alarm-handler parameters
    update_value(mainValues, "eric-fh-alarm-handler.imageCredentials.alarmhandler.registry.url=selndocker.mo.sw.ericsson.se")
    update_value(mainValues, "eric-fh-alarm-handler.imageCredentials.topiccreator.registry.url=selndocker.mo.sw.ericsson.se")
    update_value(mainValues, "eric-fh-alarm-handler.imageCredentials.hooklauncher.registry.url=selndocker.mo.sw.ericsson.se")
    update_value(mainValues, "eric-fh-alarm-handler.imageCredentials.pullSecret=regcred")
    
    # overwrite snmp alarm-provider parameters
    update_value(mainValues, "eric-fh-snmp-alarm-provider.ingress.hostname=snmp."+script_input["namespace"]+"."+script_input["env"]+".rnd.gic.ericsson.se")
    update_value(mainValues, "eric-fh-snmp-alarm-provider.imageCredentials.registry.url=selndocker.mo.sw.ericsson.se")
    update_value(mainValues, "eric-fh-snmp-alarm-provider.imageCredentials.pullSecret=regcred")
    
    # overwrite cnom parameters
    update_value(mainValues, "eric-cnom-server.imageCredentials.server.registry.url=serodocker.sero.gic.ericsson.se")
    update_value(mainValues, "eric-cnom-server.imageCredentials.hooklauncher.registry.url=serodocker.sero.gic.ericsson.se")
    update_value(mainValues, "eric-cnom-server.imageCredentials.pullSecret=regcred")
    
    # overwrite osmn parameters
    update_value(mainValues, "eric-data-object-storage-mn.imageCredentials.init.registry.url=serodocker.sero.gic.ericsson.se")
    update_value(mainValues, "eric-data-object-storage-mn.imageCredentials.osmn.registry.url=serodocker.sero.gic.ericsson.se")
    update_value(mainValues, "eric-data-object-storage-mn.imageCredentials.kes.registry.url=serodocker.sero.gic.ericsson.se")
    
    # overwrite lm combined server parameters
    update_value(mainValues, "eric-lm-combined-server.licenseServerClient.timer.licenseRemovalGracePeriod=0")
    
    # overwrite pvtb parameters
    update_value(mainValues, "eric-probe-virtual-tap-broker.imageCredentials.VirtualTapBroker.registry.url=selndocker.mo.sw.ericsson.se")
    update_value(mainValues, "eric-probe-virtual-tap-broker.imageCredentials.pullSecret=regcred")
    
    # overwrite eric-odca-diagnostic-data-collector parameters
    update_value(mainValues, "eric-odca-diagnostic-data-collector.imageCredentials.registry.url=serodocker.sero.gic.ericsson.se")
    update_value(mainValues, "eric-odca-diagnostic-data-collector.imageCredentials.ddc.registry.url=serodocker.sero.gic.ericsson.se")
    update_value(mainValues, "eric-odca-diagnostic-data-collector.imageCredentials.hooklauncher.registry.url=serodocker.sero.gic.ericsson.se")

    # overwrite eric-data-sftp-server parameters
    update_value(mainValues, "eric-data-sftp-server.imageCredentials.sftp.registry.url=serodocker.sero.gic.ericsson.se")

    if ingress_host:
        update_value(mainValues, "eric-sc.routes.nbi.fqdn=nbi."+ingress_host)

in_file = dict()

if script_input["files"] != None:
    for file in script_input["files"]:
        file_obj = read_yaml(file)
        # replace the destination 'in_file' with the source 'file'
        dpath.merge(in_file, file_obj, flags=MergeType.REPLACE)

if in_file != {}:
    dpath.merge(mainValues, in_file, flags=MergeType.REPLACE)

# apply set values
if script_input["pm_remote_write"]:
    default_settings = read_yaml("scripts/helm_config_gen/templates/values-pm-remote-write.yaml")
    dpath.merge(mainValues, default_settings, flags=MergeType.REPLACE)
    
# apply set values
# if script_input["lm_asih"]:
    # default_settings = read_yaml("scripts/helm_config_gen/templates/values-lm-asih.yaml")
    # dpath.merge(mainValues, default_settings, flags=MergeType.REPLACE)
    
if script_input["sets"] !=  None:
    for set_var in script_input["sets"]:
        update_value(mainValues, set_var)

if script_input["scp_tls"]:
	update_value(mainValues, "eric-scp.service.manager.tls=true")
	update_value(mainValues, "eric-scp.service.worker.tls=true")

if script_input["sepp_secrets"]:
    ## external secrets needed by sepp for multiple roaming partners"
    sepp_rp_settings = read_yaml(HOME+"/templates/sepp-rp-cas.yaml")
    dpath.merge(mainValues,sepp_rp_settings, flags=MergeType.ADDITIVE)

# Dual Stack adaptations. Need to be update once components become compliant 
# to Dual Stack design rules
if script_input["ip_version_ext"] == "4":
	ip_version_ext_template = read_yaml(HOME+"/templates/values-ipv4-ext.yaml")
elif script_input["ip_version_ext"] == "6":
    ip_version_ext_template = read_yaml(HOME+"/templates/values-ipv6-ext.yaml")
else:
    ip_version_ext_template = read_yaml(HOME+"/templates/values-dualstack-ext.yaml")
    
dpath.merge(mainValues,ip_version_ext_template, flags=MergeType.REPLACE)

if script_input["ip_version_int"] == "4":
	ip_version_int_template = read_yaml(HOME+"/templates/values-ipv4-int.yaml")
elif script_input["ip_version_int"] == "6":
    ip_version_int_template = read_yaml(HOME+"/templates/values-ipv6-int.yaml")
else:
    ip_version_int_template = read_yaml(HOME+"/templates/values-dualstack-int.yaml")
    
dpath.merge(mainValues,ip_version_int_template, flags=MergeType.REPLACE)

if script_input["pvtb"]:
    update_value(mainValues, "global.ericsson.pvtb.enabled=true")
    update_value(mainValues, "eric-stm-diameter.initialConfig.dsl.pvtbClient.enabled=true")

if script_input["tapagent"]:
	tapagent_template = read_yaml("scripts/helm_config_gen/templates/tapagent.yaml")
	dpath.merge(mainValues,tapagent_template, flags=MergeType.ADDITIVE)

if script_input["tapcollector"]:
	tapcollector_template = read_yaml("scripts/helm_config_gen/templates/tapcollector.yaml")
	dpath.merge(mainValues,tapcollector_template, flags=MergeType.ADDITIVE)

if not script_input["metrics"]:
	metrics_template = read_yaml("scripts/helm_config_gen/templates/no-metrics.yaml")
	dpath.merge(mainValues,metrics_template, flags=MergeType.ADDITIVE)

################################ GEORED CONFIG #############################
############################################################################
if script_input["dc1_namespace"] == None or script_input["dc2_namespace"] == None:
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
        dpath.merge(mainValues, default_geo_cfg, flags=MergeType.REPLACE)

        # Save values also as dc1 for debug and reuse
        yaml_output = open(script_input["output_path"] + "/values-geored-dc1.yaml",'w')
        yaml.dump(mainValues, yaml_output,default_flow_style=False)
    else:
        print("Updating remoteSeedNodes for Datacenter1.")
        # Load values.yaml that used for dc-1 deployment
        mainValues = read_yaml(script_input["output_path"] + "/values-geored-dc1.yaml")

        # Update remoteSeedNodes
        dpath.new(mainValues, 'eric-data-wide-column-database-cd/cassandra/remoteSeedNodes', [])
        dpath.new(mainValues, 'eric-data-wide-column-database-cd/cassandra/remoteSeedNodes/0', remote_seed1)
        dpath.new(mainValues, 'eric-data-wide-column-database-cd/cassandra/remoteSeedNodes/1', remote_seed2)
				
        yaml_output = open(script_input["output_path"] + "/values-geored-dc1.yaml",'w')
        yaml.dump(mainValues, yaml_output,default_flow_style=False)
# Generate for DC-2
elif script_input["dc2_namespace"] == script_input["namespace"]:
    
    default_geo_cfg = generate_geored_dc_config("DC-2", script_input["dc1_namespace"])

    # Apply configuration for Datacenter2
    dpath.merge(mainValues, default_geo_cfg, flags=MergeType.REPLACE)

    # Save values of dc2 for debug
    yaml_output = open(script_input["output_path"] + "/values-geored-dc2.yaml",'w')
    yaml.dump(mainValues, yaml_output,default_flow_style=False)
else:
    print("No GEORED while trying for dc1_namespace: " +script_input["dc1_namespace"] + " and dc2_namespace: " + script_input["dc2_namespace"] + " while current namespace is: " + script_input["namespace"])
############################# END OF GEORED ################################

############################# Deploy on FlexLab ############################
#if "mc-ipv4-1015" in script_input["env"]:
#    print("Updating storageClassNames to 'erikube-rbd'")
#
#    list_of_storage_class_names = [
#        "definitions.oam_storage_class=erikube-rbd",
#        "eric-ctrl-bro.persistence.persistentVolumeClaim.storageClassName=erikube-rbd",
#        "eric-data-coordinator-zk.persistence.persistentVolumeClaim.storageClassName=erikube-rbd",
#        "eric-data-distributed-coordinator-ed.persistence.persistentVolumeClaim.storageClassName=erikube-rbd",
#        "eric-data-distributed-coordinator-ed-sc.persistence.persistentVolumeClaim.storageClassName=erikube-rbd",
#        "eric-data-document-database-pg.persistentVolumeClaim.storageClassName=erikube-rbd",
#        "eric-data-message-bus-kf.persistence.persistentVolumeClaim.storageClassName=erikube-rbd",
#        "eric-data-object-storage-mn.persistentVolumeClaim.storageClassName=erikube-rbd",
#        "eric-data-search-engine.persistence.data.persistentVolumeClaim.storageClassName=erikube-rbd",
#        "eric-data-search-engine.persistence.master.persistentVolumeClaim.storageClassName=erikube-rbd",
#        "eric-data-wide-column-database-cd.persistence.dataVolume.persistentVolumeClaim.storageClassName=erikube-rbd",
#        "eric-pm-bulk-reporter.persistentVolumeClaim.storageClassName=erikube-rbd",
#        "eric-pm-server.server.persistentVolume.storageClass=erikube-rbd",
#        "eric-sc-bragent.persistentVolumeClaim.storageClassName=erikube-rbd",
#        "eric-sec-ldap-server.persistentVolumeClaim.storageClassName=erikube-rbd"
#    ]
#
#    for name in list_of_storage_class_names:
#        update_value(mainValues, name)

yaml_output = open(script_input["output_path"] + "/values-final.yaml",'w')
yaml.dump(mainValues, yaml_output,default_flow_style=False)

print("Yaml generation complete!")
