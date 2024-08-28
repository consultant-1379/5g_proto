#!/usr/bin/env python3

import argparse
import datetime
import os
import subprocess
import time
from kubernetes import client, config
from kubernetes.client.rest import ApiException

def valid_file_path(file_path):
    if os.path.isfile(file_path):
        return file_path
    else:
        raise argparse.ArgumentTypeError('The value "' + file_path +
                                         '" provided is not a readable file')

def parse_args():
    parser = argparse.ArgumentParser(
        description='Test tool for HELM installation and upgrade')
    parser.add_argument('-k', '--kubernetes-admin-conf',
                        dest='kubernetes_admin_conf',
                        type=valid_file_path, required=True,
                        metavar="KUBECONFIG",
                        help="Kubernetes admin conf to use")

    parser.add_argument('-n', '--kubernetes-namespace',
                        dest='kubernetes_namespace', type=str, required=True,
                        metavar='NAMESPACE',
                        help='Kubernetes namespace to use')

    parser.add_argument('-pvc', '--persistent-volume-claim',
                        dest='pvc_name', type=str, required=True,
                        metavar='PVC',
                        help='PVC name to delete')

    args = parser.parse_args()

    return args


def d(t0):
    return str(datetime.datetime.now() - t0)


def log(*message):
    now = datetime.datetime.now()
    print(now.date().isoformat() + ' ' + now.time().isoformat() +
          ': ' + str(*message))


class KubernetesClient:
    def __init__(self, kubernetes_admin_conf):
        config.load_kube_config(config_file=kubernetes_admin_conf)
        self.core_v1 = client.CoreV1Api()
        self.apps_v1 = client.AppsV1beta2Api()

    def delete_pvc(self, namespace_name, pvc_name):
        deleteOptions = client.V1DeleteOptions()
        try:
            self.core_v1.delete_namespaced_persistent_volume_claim(pvc_name, namespace_name, deleteOptions)
        except ApiException as e:
            print("Exception when calling CoreV1Api->delete_namespaced_persistent_volume_claim: %s\n" % e)


def main():
    args = parse_args()
    target_namespace_name = args.kubernetes_namespace
    target_pvc_name = args.pvc_name
    kube = KubernetesClient(args.kubernetes_admin_conf)
    kube.delete_pvc(target_namespace_name, target_pvc_name)

if __name__ == "__main__":
    main()
