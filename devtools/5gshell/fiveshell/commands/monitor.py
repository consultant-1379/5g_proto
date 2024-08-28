import json
import collections
from utils.port_forwarding_utils import *


def setup_monitor_port_forwarding(monitor_pods):
    """Set up the port-forwarding for the given monitor pods
    (usually just one)"""
    prepare_port_forwarding(monitor_pods, 50080, 80, set_monitor_port)


def set_monitor_port(podname, port, proc):
    """Helper function for setup_monitor_port_forwarding() to store
    the local port in the global dict"""
    g.globalDict['monitor_port'] = port  # Public


def hasher():
    return collections.defaultdict(hasher)


def format_per_code(vals):
    out = ""
    if len(vals.keys()) == 0:
        out += "All counters are zero (= were never stepped)"
    else:
        for proto in vals:
            out += format(f"Protocol: {proto}\n")
            for code in sorted(vals[proto]):
                out += format(f"{code}: {vals[proto][code]}\n")
            out += "\n"
    return out


def format_per_occ_and_code(vals):
    out = ""
    if len(vals.keys()) == 0:
        out += "All counters are zero (= were never stepped)"
    else:
        for proto in vals:
            out += format(f"proto: {proto}\n")
            for occ in sorted(vals[proto].keys()):
                for code in sorted(vals[proto][occ]):
                    out += format(f"{occ}:  {code}: {vals[proto][occ][code]}\n")
            out += "\n"
    return out


def process_chfsim_counters(counter_data):
    vals = hasher()
    vals_per_occ = hasher()
    data = json.loads(counter_data)
    for chfsim in data['results']:
        for counter in chfsim['counters']:
            if counter['name'] == "eric_chfsim_nchf_convergedcharging_http_out_answers_total":
                for instance in counter['instances']:
                    name = instance['name']
                    value = int(instance['value'])
                    oc = name.split('=')[1].replace('[', '').replace(']', '')
                    occ, code = oc.split(',')
                    if code in vals_per_occ['Converged Charging'][occ]:
                        vals_per_occ['Converged Charging'][occ][code] += value
                    else:
                        vals_per_occ['Converged Charging'][occ][code] = value

                    if code in vals['Converged Charging']:
                        vals['Converged Charging'][code] += value
                    else:
                        vals['Converged Charging'][code] = value

            if counter['name'] == "eric_chfsim_nchf_spendinglimitcontrol_http_out_answers_total":
                for instance in counter['instances']:
                    name = instance['name']
                    value = int(instance['value'])
                    oc = name.split('=')[1].replace('[', '').replace(']', '')
                    occ, code = oc.split(',')
                    if code in vals_per_occ['Spending Limit Control'][occ]:
                        vals_per_occ['Spending Limit Control'][occ][code] += value
                    else:
                        vals_per_occ['Spending Limit Control'][occ][code] = value

                    if code in vals['Spending Limit Control']:
                        vals['Spending Limit Control'][code] += value
                    else:
                        vals['Spending Limit Control'][code] = value

    # output = format_per_occ_and_code(vals_per_occ)
    # output += "\n"
    output = format_per_code(vals)
    return output
