# Utility functions

import g  # Global variables
import subprocess


def prepare_port_forwarding(pod_list, local_port, remote_port, per_pod_function=None):
    """Common function for envoy-, manager- and other
    port-forwardings. 
    For each pod in the podList, it sets up a port-forwarding
    with the local port incrementing from the startvalue given
    in 'port'.  If the perPodFunction is specified, it is called
    for each pod with the parameters (podName, localPort, processId)."""
    if 'port-forwardings' not in g.globalDict:
        g.globalDict['port-forwardings'] = {}
    if 'port-forwarding-procs' not in g.globalDict:
        g.globalDict['port-forwarding-procs'] = []
    for podname in pod_list:
        proc = subprocess.Popen(["/usr/bin/xterm",
                                 "-T", f"Port-Forwarding {podname} {local_port}",
                                 "-e", "kubectl",
                                 "-n", g.globalDict['namespace'],
                                 "port-forward", podname, f"{local_port}:{remote_port}"])
        g.globalDict['port-forwardings'][podname] = local_port
        g.globalDict['port-forwarding-procs'].append(proc)
        if per_pod_function is not None:
            per_pod_function(podname, local_port, proc)
        local_port += 1
    return "Done"


# Public
def teardown_port_forwarding():
    """Tear down all currently active port-forwardings that this program
    started"""
    if 'port-forwarding-procs' in g.globalDict:
        for proc in g.globalDict['port-forwarding-procs']:
            proc.kill()
    return "Done"
