import logging
import sys
from utils import printer
import subprocess
import shlex
import os
from shell import shellcommands


def kubecommand(func):
    def comm_wrapper(*args, **kwargs):
        command = func(*args, **kwargs)
        logging.debug("Command: " + command)

        try:
            proc = subprocess.Popen(shlex.split(command),
                                    shell=False,
                                    stdout=subprocess.PIPE,
                                    stderr=subprocess.PIPE)
            res, error = proc.communicate()
            result = res.decode()
            err = error.decode()
            logging.debug("Result: " + result)
            if err:
                printer.error("Kubernetes: " + err.strip())
                return ""
            return result.strip()
        except KeyboardInterrupt:
            pass
    return comm_wrapper


@kubecommand
def get_envoy_port():
    return "kubectl -n 5g-bsf-" + os.environ['USER'] + " get svc eric-scp-worker " +\
            "--template=\'{{index (index .spec.ports 0).nodePort}}{{\"\\n\"}}\'"


@kubecommand
def get_kubeproxy_ip():
    return "kubectl get nodes --namespace 5g-bsf-" + os.environ['USER'] +\
            " -o jsonpath=\"{.items[0].status.addresses[0].address}\""


@kubecommand
def get_cmm_port():
    return "kubectl get --namespace 5g-bsf-" + os.environ['USER'] +\
            " -o jsonpath=\"{.spec.ports[0].nodePort}\" " +\
            "services eric-cm-mediator"


def get_cmm_config_name():
    return "ericsson-scp"


@kubecommand
def envoy_port_forward(worker):
    return "kubectl port-forward " + worker +\
            " 9901:9901 --namespace 5g-bsf-" + os.environ['USER']


def get_first_scp_worker():
    return get_scp_workers()[0]


@kubecommand
def get_all_pods():
    return "kubectl get pods --namespace 5g-bsf-" + os.environ['USER']


def get_scp_workers():
    pods = get_all_pods()
    return shellcommands.awk(pods, "scp-worker")


@kubecommand
def get_chfsim_port(num):
    return "kubectl -n 5g-bsf-" + os.environ['USER'] +\
           " get svc eric-chfsim-" + num +\
           " --template=\'{{index (index .spec.ports 0).nodePort}}{{\"\\n\"}}\'"