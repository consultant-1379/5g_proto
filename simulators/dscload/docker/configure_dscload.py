"""
 * COPYRIGHT ERICSSON GMBH 2021
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Mar 8, 2021
 *     Author: emldpng
"""

import ipaddress
import json
import logging as log
import os
import re
import requests
import subprocess
import sys
import time

# Set up log level and formatting.
log.basicConfig(level = log.INFO,
                format='%(relativeCreated)6d %(levelname)s - %(message)s')

# Kubernetes API communication.
KUBE_API_IP = os.getenv('KUBERNETES_SERVICE_HOST', 'kubernetes.default.svc')
KUBE_API_PORT = os.getenv('KUBERNETES_PORT_443_TCP_PORT', 443)
KUBE_CA_PATH = '/var/run/secrets/kubernetes.io/serviceaccount/ca.crt'
KUBE_TOKEN = open('/var/run/secrets/kubernetes.io/serviceaccount/token', 'r').read()
WAIT_SERVICE_TIMEOUT = 300
DIAM_SERVICE_NAME_4 ='eric-stm-diameter-traffic-tcp-ipv4'
DIAM_SERVICE_NAME_6 ='eric-stm-diameter-traffic-tcp-ipv6'
local_pod_ip_4= ""
local_pod_ip_6= ""

# Configuration parameters for dsc-load.
AF_DIAM_REALM = os.getenv('AF_DIAMETER_REALM', 'af-diamrealm')
DIAM_SERVICE_IP = os.getenv('DIAMETER_SERVICE_IP')
DIAM_SERVICE_NAME = os.getenv('DIAMETER_SERVICE_NAME', 'eric-stm-diameter-traffic-tcp-ipv4')
DIAM_SERVICE_PORT = os.getenv('DIAMETER_SERVICE_PORT')
DIAM_SERVICE_TLS_PORT = os.getenv('DIAMETER_SERVICE_TLS_PORT')
DIAM_TPS = os.getenv('DIAMETER_TPS', 0)
IP_VERSION = os.getenv('IP_VERSION', '4')
TLS = str(os.getenv('TLS')).lower() == 'true'
ENV_LOCAL_POD_IP = os.getenv('POD_IP')
ENV_LOCAL_POD_IPS = os.getenv('POD_IPS')
LOCAL_POD_NAME = os.getenv('POD_NAME')
NAMESPACE = os.getenv('NAMESPACE')
PCF_DIAM_HOST = os.getenv('PCF_DIAMETER_HOST', 'pcf-diamhost.com')
PCF_DIAM_REALM = os.getenv('PCF_DIAMETER_REALM', 'pcf-diamrealm.com')
SINGLE_PEER_TEMPLATE = str(os.getenv('SINGLE_PEER_TEMPLATE')).lower() == 'true'

IPV4_TEMPLATE_FILE = 'templates/load-template-IPv4-single.cfg' if SINGLE_PEER_TEMPLATE else 'templates/load-template-IPv4.cfg'
IPV6_TEMPLATE_FILE = 'templates/load-template-IPv6-single.cfg' if SINGLE_PEER_TEMPLATE else'templates/load-template-IPv6.cfg'
DS_TEMPLATE_FILE = 'templates/load-template-DS-single.cfg' if SINGLE_PEER_TEMPLATE else'templates/load-template-DS.cfg'

ip_list = ENV_LOCAL_POD_IPS.split(",")
if ENV_LOCAL_POD_IPS is None:
    LOCAL_POD_IP = ENV_LOCAL_POD_IP

elif IP_VERSION == "DS": #then we are in a DS cluster    
    for IP in ip_list:
        if "." in IP:
            local_pod_ip_4 = IP
        else:
            local_pod_ip_6 = IP
    
elif IP_VERSION == "4":
    if "." in ENV_LOCAL_POD_IPS:        
        for IP in ip_list:
                if "." in IP:
                    LOCAL_POD_IP = IP
                    break
    else:
        log.info("There is no valid IPv4 POD_IP available")
        
elif IP_VERSION == "6":
    if ":" in ENV_LOCAL_POD_IPS:        
        for IP in ip_list:
                if ":" in IP:
                    LOCAL_POD_IP = IP
                    break
    else:
        log.info("There is no valid IPv6 POD_IP available")







def discover_target_service():
    """
    Finds the ClusterIP and the port of the target service.

    It is assumed that the eric-dsc-load Pod is in the same cluster and the
    same namespace as the target service.
    """
    diam_service = dict()
    if IP_VERSION == "DS":
        response_4 = read_service_from_api(DIAM_SERVICE_NAME_4)
        service_dict_4 = json.loads(response_4.content)
        response_6 = read_service_from_api(DIAM_SERVICE_NAME_6)
        service_dict_6 = json.loads(response_6.content)

        log.info('service dict 4 {}', service_dict_4)
        log.info('service dict 6 {}', service_dict_6)
        diam_service["ip-4"] = service_dict_4["spec"]["clusterIP"]
        diam_service["ip-6"] = service_dict_6["spec"]["clusterIP"]
        diam_service["port"] = service_dict_4["spec"]["ports"][0]["port"]
        diam_service["tls_port"] = service_dict_4["spec"]["ports"][1]["port"] if len(service_dict_4["spec"]["ports"]) > 1 else service_dict_4["spec"]["ports"][0]["port"]
    else:
        response = read_service_from_api(DIAM_SERVICE_NAME)
        service_dict = json.loads(response.content)
        diam_service["ip"] = service_dict["spec"]["clusterIP"]
        diam_service["port"] = service_dict["spec"]["ports"][0]["port"]
        diam_service["tls_port"] = service_dict_4["spec"]["ports"][1]["port"] if len(service_dict_4["spec"]["ports"]) > 1 else service_dict_4["spec"]["ports"][0]["port"]

    return diam_service


def generate_config(diam_service):
    """
    Produces a configuration file from a template.

    Args:
      diam_service: A dictionary with the target diameter service end-points.
    """

    # Select template file according to the selected IP version.
    log.info(f'Selected IP version: {IP_VERSION}')
    if IP_VERSION == "DS":
        template_file = DS_TEMPLATE_FILE
    elif IP_VERSION == "4":
        template_file = IPV4_TEMPLATE_FILE
    else:
        template_file = IPV6_TEMPLATE_FILE

    # Dsc-load produces half TPS of what is configured in load.cfg.
    normalized_tps = (int(DIAM_TPS) * 2) if SINGLE_PEER_TEMPLATE else int(int(DIAM_TPS)/20) #division by 20 peers

    encryption_protocol = "TLS" if TLS else "TCP"
    port_to_use = diam_service["tls_port"] if TLS else diam_service["port"]
    dscload_config = []
    if IP_VERSION == "DS":
        subs = [{"tag": "<af-diameter-realm>", "replace": AF_DIAM_REALM},
                {"tag": "<diameter-tps>", "replace": str(normalized_tps)},
                {"tag": "<own-ip-4>", "replace": local_pod_ip_4},
                {"tag": "<own-ip-6>", "replace": local_pod_ip_6},
                {"tag": "<own-pod-name>", "replace": LOCAL_POD_NAME},
                {"tag": "<pcf-diameter-host>", "replace": PCF_DIAM_HOST},
                {"tag": "<pcf-diameter-realm>", "replace": PCF_DIAM_REALM},
                {"tag": "<target-service-ip-4>", "replace": diam_service["ip-4"]},
                {"tag": "<target-service-ip-6>", "replace": diam_service["ip-6"]},
                {"tag": "<target-service-port>", "replace": str(port_to_use)},
                {"tag": "<encryption>", "replace": encryption_protocol}]
    else:
        subs = [{"tag": "<af-diameter-realm>", "replace": AF_DIAM_REALM},
                {"tag": "<diameter-tps>", "replace": str(normalized_tps)},
                {"tag": "<own-ip>", "replace": LOCAL_POD_IP},
                {"tag": "<own-pod-name>", "replace": LOCAL_POD_NAME},
                {"tag": "<pcf-diameter-host>", "replace": PCF_DIAM_HOST},
                {"tag": "<pcf-diameter-realm>", "replace": PCF_DIAM_REALM},
                {"tag": "<target-service-ip>", "replace": diam_service["ip"]},
                {"tag": "<target-service-port>", "replace": str(port_to_use)},
                {"tag": "<encryption>", "replace": encryption_protocol}]

    # Generate the configuration and store it in a list.
    try:
        with open (template_file, 'r') as f:
            line = f.read()
            for sub in subs:
                line = re.sub(sub["tag"], sub["replace"], line)

            dscload_config.append(line)
    except FileNotFoundError:
        log.error('Template configuration file is missing')
        sys.exit(1)
    except:
        log.error('Unexpected error while generating the configuration: ' \
                  f'{sys.exc_info()}')
        sys.exit(1)

    return dscload_config


def read_service_from_api(diameter_svc_name):
    """
    Requests the target service info from the K8s API.
    """
    url = ""
    if "." in KUBE_API_IP:
        url = f'https://{KUBE_API_IP}:{KUBE_API_PORT}/api/v1/' \
              f'namespaces/{NAMESPACE}/services/{diameter_svc_name}'
    elif ":" in KUBE_API_IP:
        url = f'https://[{KUBE_API_IP}]:{KUBE_API_PORT}/api/v1/' \
              f'namespaces/{NAMESPACE}/services/{diameter_svc_name}'
    headers = {'Authorization': f'Bearer {KUBE_TOKEN}'}

    return requests.get(url, headers=headers, verify=KUBE_CA_PATH)


def wait_for_target_service():
    """
    Waits until the target service is available.
    """
    timeout = time.time() + WAIT_SERVICE_TIMEOUT
    while time.time() < timeout:
        try:
            response = read_service_from_api(DIAM_SERVICE_NAME)
            code = response.status_code
            if code == 200:
                log.info('Target service is available.')
                return
            elif code == 404:
                log.info('Target service is not deployed yet. Retrying...')
            else:
                log.info(f'Target service is not available due to status-code: {code}')
        except:
            log.error('Unexpected error while waiting for the target service: ' \
                      f'{sys.exc_info()}')
            break

        time.sleep(5)

    log.error('Target service end-points are not available. Exiting...')
    sys.exit(1)


def write_config(dscload_config):
    """
    Stores the generated configuration as load.cfg.

    Args:
      dscload_config: The configuration as a list of lines.
    """
    try:
        with open ('load.cfg', 'w') as f:
            for entry in dscload_config:
                f.write(entry)
    except:
        log.error('Unexpected error while trying to update the configuration: ' \
                  f'{sys.exc_info()}')
        sys.exit(1)

def copy_ca_certificate():

    # Check if rootCA certificate exists and wait until it is available
    file_exists = False

    while not file_exists:
        time.sleep(5)
        # Check if file exists
        if os.path.exists("/root/certificates/trustCA/sc-trusted-default-cas.pem"):
            file_exists = True
            log.info("RootCA found!")
        else:
            log.info("RootCA missing, retrying")

    # Copy certificate
    # On success returns 0 which gets translated to False
    copy_was_successful = not bool(subprocess.call('cp /root/certificates/trustCA/sc-trusted-default-cas.pem /opt/dsc-load/rootCA', shell=True))
    if copy_was_successful:
        log.info("Certificate copied successfully!")
    else:
        log.error("Copy operation failed for sc-trusted-default-cas.pem. Please run this operation manually.")

    # Rehash certificate
    rehash_was_successful = not bool(subprocess.call('c_rehash .' , cwd="/opt/dsc-load/rootCA", shell=True))
    if rehash_was_successful:
        log.info("Rehash completed successfully!")
    else:
        log.error("Rehash operation Failed. Please run this operation manually.")

def main():
    """
    Configures dsc-load tool.
    """
    diam_service = dict()
    if DIAM_SERVICE_IP and DIAM_SERVICE_PORT:
        log.info("The end-points of the target diameter service were provided.")
        diam_service["ip"] = DIAM_SERVICE_IP
        diam_service["port"] = DIAM_SERVICE_PORT
        diam_service["tls_port"] = DIAM_SERVICE_TLS_PORT
    else:
        # Auto-discover the end-points of the given service.
        log.info(f'Discovering the end-points of the target diameter service: ' \
                 f'"{DIAM_SERVICE_NAME}" via the Kubernetes API')
        wait_for_target_service()

        # Fetch info
        log.info('Fetching the end-points of the target diameter service')
        diam_service = discover_target_service()

    # Copy CA certificates
    if TLS:
        copy_ca_certificate()

    # Produce the dsc-load configuration file from a template.
    log.info('Generating the dsc-load configuration')
    dscload_config = generate_config(diam_service)

    # Store produced configuration.
    write_config(dscload_config)
    log.info('Dsc-load tool was configured successfully!')


if __name__ == '__main__':
    main()
