import logging
import os
import subprocess
from curl.response import Response
import shlex
from utils import printer


def request(func):
    def req_wrapper(*args, **kwargs):
        url = func(*args, **kwargs)
        logging.debug("Request URL: " + url)
        try:
            proc = subprocess.Popen(shlex.split(url),
                                    shell=False,
                                    stdout=subprocess.PIPE,
                                    stderr=subprocess.PIPE)
            body, headers = proc.communicate()
            response = Response(headers.decode(), body.decode())
            return response
        except KeyboardInterrupt:
            pass
    return req_wrapper

tls = " --cert /certs/smf.eed.ericsson.se.crt " +\
           "--key certs/smf.eed.ericsson.se.key --cacert /certs/rootCA.crt "


@request
def cc_create_request(worker_port, ip):
    """ Sends a create request for a new charging reference """

    return "curl -v --resolve csa.ericsson.se:" + worker_port + ":" + ip +\
           " -X POST http://csa.ericsson.se:" + worker_port +\
           "/nchf-convergedcharging/v1/chargingdata/"


@request
def cc_create_request_notify(worker_port, ip, client):
    """ Sends a create request for a new charging reference with a notifyUri"""

    uri = "http://" + client + ".5g-bsf-" + os.environ['USER'] +\
          ".svc.cluster.local"

    return "curl -v --resolve csa.ericsson.se:" + worker_port + ":" + ip +\
           " -X POST -H \"Content-Type:application/json\" -d " +\
           "'{\"notifyUri\": \"" + uri + "\"}' http://csa.ericsson.se:" +\
           worker_port + "/nchf-convergedcharging/v1/chargingdata/"


@request
def cc_create_request_notify_tls(worker_port, ip, client):
    """
    Sends a create request for a new charging
    reference with a notifyUri and TLS
    """

    uri = "https://" + client + ".5g-bsf-" + os.environ['USER'] +\
          ".svc.cluster.local"

    return "docker run curl -v --resolve csa.ericsson.se:" + worker_port +\
           ":" + ip +\
           tls +\
           " -X POST -H \"Content-Type:application/json\" -d " +\
           "'{\"notifyUri\": \"" + uri + "\"}' https://csa.ericsson.se:" +\
           worker_port + "/nchf-convergedcharging/v1/chargingdata/"


@request
def cc_create_request_tls(worker_port, ip):
    """ Sends a create request for a new charging reference with TLS"""
    
    return "docker run curl -v --resolve csa.ericsson.se:" + worker_port +\
           ":" + ip + tls + " -X POST " +\
           "https://csa.ericsson.se:" + worker_port +\
           "/nchf-convergedcharging/v1/chargingdata/"


@request
def cc_update_request(worker_port, ip, ref):
    return "curl -v --resolve csa.ericsson.se:" + worker_port + ":" + ip +\
           " -X POST http://csa.ericsson.se:" + worker_port +\
           "/nchf-convergedcharging/v1/chargingdata/" + ref + "/update"


@request
def cc_update_request_tls(worker_port, ip, ref):
    return "docker run curl -v --resolve csa.ericsson.se:" +\
           worker_port + ":" + ip + tls +\
           " -X POST https://csa.ericsson.se:" + worker_port +\
           "/nchf-convergedcharging/v1/chargingdata/" + ref + "/update"


@request
def cc_update_request_notify(worker_port, ip, ref, client):
    uri = "http://" + client + ".5g-bsf-" + os.environ['USER'] +\
          ".svc.cluster.local"
    return "curl -v --resolve csa.ericsson.se:" + worker_port + ":" + ip +\
           " -X POST -H \"Content-Type: application/json\" -d " +\
           "'{\"notifyUri\": \"" + uri + "\"}' http://csa.ericsson.se:" +\
           worker_port + "/nchf-convergedcharging/v1/chargingdata/" + ref +\
           "/update"


@request
def cc_update_request_notify_tls(worker_port, ip, ref, client):
    uri = "https://" + client + ".5g-bsf-" + os.environ['USER'] +\
          ".svc.cluster.local"
    return "docker run curl -v --resolve csa.ericsson.se:" + worker_port +\
           ":" + ip + tls +\
           " -X POST -H \"Content-Type: application/json\" -d " +\
           "'{\"notifyUri\": \"" + uri + "\"}' https://csa.ericsson.se:" +\
           worker_port + "/nchf-convergedcharging/v1/chargingdata/" + ref +\
           "/update"


@request
def cc_release_request(worker_port, ip, ref):
    return "curl -v --resolve csa.ericsson.se:" + worker_port + ":" + ip +\
           " -X POST http://csa.ericsson.se:" + worker_port +\
           "/nchf-convergedcharging/v1/chargingdata/" + ref + "/delete"


@request
def cc_release_request_tls(worker_port, ip, ref):
    return "docker run curl -v --resolve csa.ericsson.se:" + worker_port +\
           ":" + ip + tls +\
           " -X POST https://csa.ericsson.se:" + worker_port +\
           "/nchf-convergedcharging/v1/chargingdata/" + ref + "/delete"


@request
def slc_subscribe_request(worker_port, ip):
    """ Sends a create request for a new charging reference """
    return "curl -v --resolve csa.ericsson.se:" + worker_port + ":" + ip +\
           " -X POST http://csa.ericsson.se:" + worker_port +\
           "/nchf-spendinglimitcontrol/v1/subscriptions"


@request
def slc_subscribe_request_tls(worker_port, ip):
    """ Sends a create request for a new charging reference """
    return "docker run curl -v --resolve csa.ericsson.se:" + worker_port +\
           ":" + ip + tls +\
           " -X POST https://csa.ericsson.se:" + worker_port +\
           "/nchf-spendinglimitcontrol/v1/subscriptions"


@request
def slc_subscribe_request_notify(worker_port, ip, client):
    """ Sends a create request for a new charging reference with a notifyUri"""
    uri = "http://" + client + ".5g-bsf-" + os.environ['USER'] +\
          ".svc.cluster.local"
    return "curl -v --resolve csa.ericsson.se:" + worker_port + ":" + ip +\
           " -X POST -H \"Content-Type:application/json\" -d " +\
           "'{\"notifyUri\": \"" + uri + "\"}' http://csa.ericsson.se:" +\
           worker_port + "/nchf-spendinglimitcontrol/v1/subscriptions"


@request
def slc_subscribe_request_notify_tls(worker_port, ip, client):
    """ Sends a create request for a new charging reference with a notifyUri"""
    uri = "https://" + client + ".5g-bsf-" + os.environ['USER'] +\
          ".svc.cluster.local"
    return "docker run curl -v --resolve csa.ericsson.se:" + worker_port +\
           ":" + ip + tls +\
           " -X POST -H \"Content-Type:application/json\" -d " +\
           "'{\"notifyUri\": \"" + uri + "\"}' https://csa.ericsson.se:" +\
           worker_port + "/nchf-spendinglimitcontrol/v1/subscriptions"


@request
def slc_modify_request(worker_port, ip, ref):
    return "curl -v --resolve csa.ericsson.se:" + worker_port + ":" + ip +\
           " -X PUT http://csa.ericsson.se:" + worker_port +\
           "/nchf-spendinglimitcontrol/v1/subscriptions/" + ref


@request
def slc_modify_request_tls(worker_port, ip, ref):
    return "docker run curl -v --resolve csa.ericsson.se:" + worker_port +\
           ":" + ip + tls +\
           " -X PUT https://csa.ericsson.se:" + worker_port +\
           "/nchf-spendinglimitcontrol/v1/subscriptions/" + ref


@request
def slc_modify_request_notify(worker_port, ip, ref, client):
    uri = "http://" + client + ".5g-bsf-" + os.environ['USER'] +\
          ".svc.cluster.local"
    return "curl -v --resolve csa.ericsson.se:" + worker_port + ":" + ip +\
           " -X PUT -H \"Content-Type:application/json\" -d " +\
           "'{\"notifyUri\": \"" + uri + "\"}' http://csa.ericsson.se:" +\
           worker_port + "/nchf-spendinglimitcontrol/v1/subscriptions/" + ref


@request
def slc_modify_request_notify_tls(worker_port, ip, ref, client):
    uri = "http://" + client + ".5g-bsf-" + os.environ['USER'] +\
          ".svc.cluster.local"
    return "docker run curl -v --resolve csa.ericsson.se:" + worker_port +\
           ":" + ip + tls +\
           " -X PUT -H \"Content-Type:application/json\" -d " +\
           "'{\"notifyUri\": \"" + uri + "\"}' http://csa.ericsson.se:" +\
           worker_port + "/nchf-spendinglimitcontrol/v1/subscriptions/" + ref


@request
def slc_unsubscribe_request(worker_port, ip, ref):
    return "curl -v --resolve csa.ericsson.se:" + worker_port + ":" + ip +\
           " -X DELETE http://csa.ericsson.se:" + worker_port +\
           "/nchf-spendinglimitcontrol/v1/subscriptions/" + ref


@request
def slc_unsubscribe_request_tls(worker_port, ip, ref):
    return "docker run curl -v --resolve csa.ericsson.se:" + worker_port +\
           ":" + ip + tls +\
           " -X DELETE https://csa.ericsson.se:" + worker_port +\
           "/nchf-spendinglimitcontrol/v1/subscriptions/" + ref


@request
def chfsim_set_envoy_port_request(ip, chfsim_port, worker_port):
    return "curl -v -X POST http://" + ip + ":" + chfsim_port +\
           "/admin/v1/set_envoy_port/" + worker_port


@request
def chfsim_notify_request(ip, chfsim_port, ref):
    return "curl -v -X POST http://" + ip + ":" + chfsim_port +\
           "/admin/v1/notify/" + ref


@request
def post_configuration(cmm_ip, cmm_port, conf_filename):
    return "curl -X POST http://" + cmm_ip + ":" + cmm_port +\
           "/cm/api/v1.1/configurations" +\
           " -H \"Content-Type: application/json\" -d @" +\
           conf_filename


@request
def update_configuration(cmm_ip, cmm_port, cmm_config_name, conf_filename):
    return "curl -X PUT http://" + cmm_ip + ":" + cmm_port +\
           "/cm/api/v1.1/configurations/" + cmm_config_name +\
           " -H \"Content-Type: application/json\" -d @" +\
           conf_filename


@request
def delete_configuration(cmm_ip, cmm_port, cmm_config_name):
    return "curl -X DELETE http://" + cmm_ip + ":" + cmm_port +\
           "/cm/api/v1.1/configurations/" + cmm_config_name
