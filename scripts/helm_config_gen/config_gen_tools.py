#!/usr/bin/env python3

import os
import logging
import sys
import yaml
import dpath
from dpath.types import MergeType
from argparse import ArgumentParser, RawTextHelpFormatter
import textwrap


log = logging.getLogger(__name__)


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
    parser.add_argument("-v", "--version_ip", dest="version_ip", help="Define what IP Version (4 or 6) to use.")
    parser.add_argument("--bsf-tls", dest="bsf_tls", help="Set this parameter to true for enabling tls on bsf", required=False)    
    parser.add_argument("--scp-tls", dest="scp_tls", help="Set this parameter to true for enabling tls on scp", required=False)
    parser.add_argument("--dced-tls", dest="dced_tls", help="Set this parameter to true for enabling tls on dced", required=False)    
    parser.add_argument("--sepp-secrets", dest="sepp_secrets", help="Set this parameter to true for enabling creation of additional secrets for sepp (CI ONLY)", required=False)    
    script_input = dict()
    
    # check if input path is valid
    if not os.path.exists(parser.parse_args().input_path):
        exit_and_fail("The input path: " + str(parser.parse_args().input_path) + " is not valid!")
    script_input["input_path"] = parser.parse_args().input_path
    
    # check if output path is valid
    if not os.path.exists(parser.parse_args().output_path):
        exit_and_fail("The output path: " + str(parser.parse_args().output_path) + " is not valid!")
    script_input["output_path"] = parser.parse_args().output_path
        
    # parse bsf-tls variable and use boolean value
    if parser.parse_args().bsf_tls in ("TRUE", "True", "true", True):
        script_input["bsf_tls"] = True
    else:
        script_input["bsf_tls"] = False 
           
    # parse scp-tls variable and use boolean value
    if parser.parse_args().scp_tls in ("TRUE", "True", "true", True):
        script_input["scp_tls"] = True
    else:
        script_input["scp_tls"] = False

    # parse dced-tls variable and use boolean value
    if parser.parse_args().dced_tls in ("TRUE", "True", "true", True):
        script_input["dced_tls"] = True
    else:
        script_input["dced_tls"] = False

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
    
    script_input["resources"] = parser.parse_args().resources
    
    script_input["version_ip"] = parser.parse_args().version_ip

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
    
    
    return script_input
        
def get_real_value(value):
    
    try:
        real_value = float(value)
    except ValueError:
        if value == "True" or value == "true" or value == "TRUE":
            real_value = True
        elif value == "False" or value == "false" or value == "FALSE":
            real_value = False
        else:
            real_value = value
            
    return real_value
    
def update_value(y_dict, set_var):
    set_var = set_var.strip()
    
    set_var = set_var.split('=')
    path = set_var[0]
    value =  get_real_value(set_var[1])
       
    #dpath limitation: cannot create full tree only 1 level
    try:
        dpath.get(y_dict, path, '.')
    except:
        # if the path doesn't exist create a new one
        dpath.new(y_dict, path, value, '.')
    else:
        # else just update the value
        dpath.set(y_dict, path, value, '.')
    
    return y_dict    

script_input = check_args()
      
mainValues = read_yaml(script_input["input_path"] + "/eric-sc-values.yaml")

# apply deployment enviroment's settings

pmbr_osmn = read_var(script_input["output_path"] + "/var.pmbr-osmn")
pmbr_osmn = pmbr_osmn.strip('"')
update_value(mainValues, "eric-pm-bulk-reporter.objectStorage.enabled="+pmbr_osmn)
    
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
    env_settings = read_yaml("scripts/helm_config_gen/templates/"+resource_profile)

    dpath.merge(mainValues,env_settings, flags=MergeType.REPLACE)
    lm_settings = read_yaml("scripts/helm_config_gen/templates/license-server-array.yaml")
    dpath.merge(mainValues,lm_settings, flags=MergeType.REPLACE)
    script_input["files"] = None
    update_value(mainValues, "eric-fh-snmp-alarm-provider.ingress.enabled=true")
    update_value(mainValues, "eric-fh-snmp-alarm-provider.service.secretName=snmp-alarm-provider-config")
    update_value(mainValues, "eric-sc.routes.nbi.fqdn=nbi."+ingress_host)
    update_value(mainValues, "eric-lm-combined-server.licenseServerClient.licenseServer.thrift.host=eric-test-nels-simulator")
    update_value(mainValues, "eric-fh-alarm-handler.imageCredentials.registry.url=selndocker.mo.sw.ericsson.se")
    update_value(mainValues, "eric-fh-alarm-handler.imageCredentials.eric-fh-alarm-handler.registry.url=selndocker.mo.sw.ericsson.se")
    update_value(mainValues, "eric-fh-alarm-handler.imageCredentials.topic-creator.registry.url=selndocker.mo.sw.ericsson.se")
    update_value(mainValues, "eric-fh-alarm-handler.imageCredentials.logshipper.registry.url=selndocker.mo.sw.ericsson.se")
    update_value(mainValues, "eric-fh-alarm-handler.imageCredentials.ericsecoauthproxy.registry.url=selndocker.mo.sw.ericsson.se")
    update_value(mainValues, "eric-fh-alarm-handler.imageCredentials.ericsecoauthsap.registry.url=selndocker.mo.sw.ericsson.se")
    update_value(mainValues, "eric-fh-snmp-alarm-provider.imageCredentials.registry.url=selndocker.mo.sw.ericsson.se")
    #update_value(mainValues, "eric-sec-admin-user-management.imageCredentials.image1.registry.url=selndocker.mo.sw.ericsson.se")
    update_value(mainValues, "eric-cnom-server.imageCredentials.registry.url=selndocker.mo.sw.ericsson.se")
    update_value(mainValues, "eric-fh-snmp-alarm-provider.ingress.hostname=snmp."+script_input["namespace"]+"."+script_input["env"]+".rnd.gic.ericsson.se")
    update_value(mainValues, "eric-data-object-storage-mn.environment.MINIO_BROWSER=on")
    update_value(mainValues, "eric-log-transformer.egress.syslog.certificates.asymmetricKeyCertificateName=syslog-default-key-cert")
    update_value(mainValues, "eric-log-transformer.egress.syslog.certificates.trustedCertificateListName=sc-trusted-default-cas")
    update_value(mainValues, "eric-lm-combined-server.licenseServerClient.timer.licenseRemovalGracePeriod=0")
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
    env_settings = read_yaml("scripts/helm_config_gen/templates/values-minikube-diff.yaml") if script_input["env"] == "minikube" else  read_yaml("scripts/helm_config_gen/templates/"+resource_profile)
    dpath.merge(mainValues,env_settings, flags=MergeType.REPLACE)

    # sepp_rp_settings = read_yaml("scripts/helm_config_gen/templates/sepp-rp-cas.yaml")
    # dpath.merge(mainValues,sepp_rp_settings, flags=MergeType.ADDITIVE)

    lm_settings = read_yaml("scripts/helm_config_gen/templates/license-server-array.yaml")
    dpath.merge(mainValues,lm_settings, flags=MergeType.REPLACE)
    update_value(mainValues, "eric-fh-alarm-handler.imageCredentials.registry.url=selndocker.mo.sw.ericsson.se")
    update_value(mainValues, "eric-fh-snmp-alarm-provider.imageCredentials.registry.url=selndocker.mo.sw.ericsson.se")
    update_value(mainValues, "eric-fh-alarm-handler.imageCredentials.eric-fh-alarm-handler.registry.url=selndocker.mo.sw.ericsson.se")
    update_value(mainValues, "eric-fh-alarm-handler.imageCredentials.topic-creator.registry.url=selndocker.mo.sw.ericsson.se")
    update_value(mainValues, "eric-fh-alarm-handler.imageCredentials.logshipper.registry.url=selndocker.mo.sw.ericsson.se")
    update_value(mainValues, "eric-fh-alarm-handler.imageCredentials.ericsecoauthproxy.registry.url=selndocker.mo.sw.ericsson.se")
    update_value(mainValues, "eric-fh-alarm-handler.imageCredentials.ericsecoauthsap.registry.url=selndocker.mo.sw.ericsson.se")
    #update_value(mainValues, "eric-sec-admin-user-management.imageCredentials.image1.registry.url=selndocker.mo.sw.ericsson.se")
    update_value(mainValues, "eric-cnom-server.imageCredentials.registry.url=selndocker.mo.sw.ericsson.se")
    update_value(mainValues, "eric-fh-snmp-alarm-provider.ingress.hostname=snmp."+script_input["namespace"]+"."+script_input["env"]+".rnd.gic.ericsson.se")
    update_value(mainValues, "eric-data-object-storage-mn.environment.MINIO_BROWSER=on")

    update_value(mainValues, "eric-log-transformer.egress.syslog.certificates.asymmetricKeyCertificateName=syslog-default-key-cert")
    update_value(mainValues, "eric-log-transformer.egress.syslog.certificates.trustedCertificateListName=sc-trusted-default-cas")
    update_value(mainValues, "eric-lm-combined-server.licenseServerClient.timer.licenseRemovalGracePeriod=0")
    
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
if script_input["sets"] !=  None:
    for set_var in script_input["sets"]:
        update_value(mainValues, set_var)


if script_input["bsf_tls"]:
    update_value(mainValues, "eric-bsf.service.manager.tls=true")
    update_value(mainValues, "eric-bsf.service.worker.tls=true")



if script_input["scp_tls"]:
	update_value(mainValues, "eric-scp.service.manager.tls=true")
	update_value(mainValues, "eric-scp.service.worker.tls=true")

if script_input["dced_tls"]:
    # DND-27223 
    # default endpoint is with http prefix
    
    update_value(mainValues, "eric-csa.etcd.endpoint=https://eric-data-distributed-coordinator-ed-sc:2379")
    update_value(mainValues, "eric-csa.service.manager.client.interfaces.dced.tls.enabled=true")
    update_value(mainValues, "eric-bsf-diameter.etcd.endpoint=https://eric-data-distributed-coordinator-ed-sc:2379")
    update_value(mainValues, "eric-bsf-diameter.service.diameter.client.interfaces.dced.tls.enabled=true")
    update_value(mainValues, "eric-bsf.service.manager.client.interfaces.dced.tls.enabled=true")
    update_value(mainValues, "eric-bsf.etcd.endpoint=https://eric-data-distributed-coordinator-ed-sc:2379")
    update_value(mainValues, "eric-sc-spr.service.spr.client.interfaces.dced.tls.enabled=true")
    update_value(mainValues, "eric-sc-spr.etcd.endpoint=https://eric-data-distributed-coordinator-ed-sc:2379")
    update_value(mainValues, "eric-data-distributed-coordinator-ed-sc.security.dced.certificates.enabled=true")

if script_input["sepp_secrets"]:
    ## external secrets needed by sepp for multiple roaming partners"
    sepp_rp_settings = read_yaml("scripts/helm_config_gen/templates/sepp-rp-cas.yaml")
    dpath.merge(mainValues,sepp_rp_settings, flags=MergeType.ADDITIVE)

if script_input["version_ip"] == "6":
	ipv6_template = read_yaml("scripts/helm_config_gen/templates/values-ipv6.yaml")
	dpath.merge(mainValues,ipv6_template, flags=MergeType.REPLACE)

yaml_output = open(script_input["output_path"] + "/values-final.yaml",'w')
yaml.dump(mainValues, yaml_output,default_flow_style=False)

print("Yaml generation complete!")

# 
