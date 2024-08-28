# Envoy-related functions

import json
import re

import jmespath
import yaml

from utils.port_forwarding_utils import *
from libs.httprequests import *


# ------------------------------------------------------------------------


# Public
def setup_envoy_port_forwarding(envoy_pods):
    """Given a list of Envoy Pod names, set up a port-forwarding
    for their admin-port (9901) to localhost, ports 9901..."""
    if 'envoy_admin_ports' not in g.globalDict:
        g.globalDict['envoy_admin_ports'] = []
    prepare_port_forwarding(envoy_pods, 9901, 9901, set_envoy_admin_ports)


def set_envoy_admin_ports(podname, port, proc):
    """Helper function for setupEnvoyPortForwarding() to insert
    the ports into the global dictionary"""
    g.globalDict['envoy_admin_ports'].append(port)  # Public
    g.globalDict['envoy_one_admin_port'] = port  # Public


# Public
def show_envoy_log_level(envoy_pods, first=True):
    """Show the log-level for the given pods. If "first" is true,
    only show the first one (and assume all others are the same),
    if it is false, show it for all Envoys."""
    if first:
        envoy_pods = [envoy_pods[0]]
    out = ""
    for podname in envoy_pods:
        port = g.globalDict['port-forwardings'][podname]
        out += post_request(f"http://localhost:{port}/logging", 5)
    return out


# Public
def get_envoy_config(envoy_pod):
    """Fetch the Envoy config and return it as a dict. Re-try
    in case the port-forwarding setup takes a bit longer."""
    port = g.globalDict['port-forwardings'][envoy_pod]
    config_str = get_single_request(f"http://localhost:{port}/config_dump?include_eds", 5).text
    return json.loads(config_str)


# Public
def get_envoy_config_in_loadable_format(envoy_pod):
    """Get the Envoy configuration and modify it so that it can
    be loaded into an Envoy via "envoy -c <configfile>". This is slightly
    different from the format you get when going to the admin interface
    /config_dump"""
    data = get_envoy_config(envoy_pod)

    # Output config is collected in "out":
    out = {'static_resources': {'clusters': [],
                                'listeners': [],
                                'routes': []}}
    # out['static_resources']['clusters'] = []
    # out['static_resources']['listeners'] = []
    # out['static_resources']['routes'] = []

    # No idea if the order of the Envoy config is always in the same
    # order -> go through all sections and do the right thing per section
    # (bootstrap config, listeners, clusters, routes (ignore routes, they are
    # also in the listener section)
    for section in data['configs']:
        if 'bootstrap' in section:
            out['admin'] = section['bootstrap']['admin']
        if 'static_clusters' in section:
            for cluster in section['static_clusters']:
                out['static_resources']['clusters'].append(cluster['cluster'])
        if 'dynamic_active_clusters' in section:
            for cluster in section['dynamic_active_clusters']:
                out['static_resources']['clusters'].append(cluster['cluster'])
        if 'dynamic_active_listeners' in section:
            for listener in section['dynamic_active_listeners']:
                out['static_resources']['listeners'].append(listener['listener'])
        if 'dynamic_listeners' in section:
            for listener in section['dynamic_listeners']:
                out['static_resources']['listeners'].append(listener)
        if 'dynamic_route_configs' in section:
            for route in section['dynamic_route_configs']:
                out['static_resources']['routes'].append(route['route_config'])

    # Save to file. "default_flow_style=False" saves a multi-line YAML
    # (not everything on one line with {})
    yaml.Dumper.ignore_aliases = lambda *args: True
    return yaml.dump(out)


# Public
def print_envoy_config(envoy_pods):
    """Fetch the Envoy configuration from all given Envoys and
    print the JSON in a readable way"""
    if type(envoy_pods) != list:
        envoy_pods = [envoy_pods]
    out = ""
    for envoyPod in envoy_pods:
        out += f"Envoy Pod ID: {envoyPod}\n"
        config = get_envoy_config(envoyPod)
        out += json.dumps(config, sort_keys=True, indent=2)
        if len(envoy_pods) > 1:
            out += "\n\n"
    return out


# Public
def get_envoy_listeners(envoy_pods):
    """Fetch the Envoy config and print the active listeners"""
    if type(envoy_pods) != list:
        envoy_pods = [envoy_pods]
    out = ""
    for envoyPod in envoy_pods:
        out += f"Envoy Pod ID: {envoyPod}\n"
        config = get_envoy_config(envoyPod)
        dynamic_active_listeners = jmespath.search("configs[].dynamic_listeners[]", config)
        out += json.dumps(dynamic_active_listeners, sort_keys=True, indent=2)
        if len(envoy_pods) > 1:
            out += "\n\n"
    return out


# Public
def get_envoy_routes(envoy_pods):
    """Fetch the Envoy config and print the route"""
    if type(envoy_pods) != list:
        envoy_pods = [envoy_pods]
    out = ""
    for envoyPod in envoy_pods:
        out += f"Envoy Pod ID: {envoyPod}\n"
        config = get_envoy_config(envoyPod)
        dynamic_routes = jmespath.search("configs[].dynamic_route_configs[]", config)
        out += json.dumps(dynamic_routes, sort_keys=True, indent=2)
        out += "\n\n"
        # At the end, print an overview table:
        rds_routes = jmespath.search("configs[].dynamic_route_configs[].route_config.[name, virtual_hosts[][]"
                                     ".[name, domains, routes]]", config)
        for rdsRoute in rds_routes:
            out += f'\n• RDS name: {rdsRoute[0]}\n'
            for vhost in rdsRoute[1]:
                out += f'  └ vhost: name: {vhost[0]:<20.20s},  domains: {vhost[1]}\n'
                if vhost[2]:
                    for route in vhost[2]:
                        out += format_route(route)
        if len(envoy_pods) > 1:
            out += "\n\n"
    return out


def format_route(route_data):
    """Given route configuration data, format it
    for printing and return a formatted string"""
    match = ""
    route = ""
    print(f"route data: {route_data['match']}")
    if 'safe_regex' in route_data["match"]:
        match += f"regex=\"{route_data['match']['safe_regex']['regex']}\" "
    if 'prefix' in route_data["match"]:
        match += f"prefix=\"{route_data['match']['prefix']}\" "
    if 'headers' in route_data["match"]:
        match += format_route_header_matches(route_data['match']['headers'])

    if 'direct_response' in route_data:
        route = f"direct response :{route_data['direct_response']['status']}"
    elif 'cluster_header' in route_data["route"]:
        route = f"cluster-header=\"{route_data['route']['cluster_header']}\""
    elif 'cluster' in route_data["route"]:
        route = f"cluster=\"{route_data['route']['cluster']}\""

    out = f'    └ route: {match}\n              ----> {route}\n'
    return out

def format_route_header_matches(headers):
    text = "headers: "
    for pair in headers:
        text += pair['name']
        if 'exact_match' in pair:
            text += f"==\"{pair['exact_match']}\" "
        else:
            text += "(present) "
    return text

# Public
def get_envoy_clusters(envoy_pods):
    """Fetch the Envoy config and print the active clusters"""
    if type(envoy_pods) != list:
        envoy_pods = [envoy_pods]
    out = ""
    for envoyPod in envoy_pods:
        out += f"Envoy Pod ID: {envoyPod}\n"
        config = get_envoy_config(envoyPod)
        dynamic_active_clusters = jmespath.search("configs[].dynamic_active_clusters[]", config)
        out += json.dumps(dynamic_active_clusters, sort_keys=True, indent=2)
        out += "\n\n"
        # At the end, print an overview table:
        out += f"{'Name'.center(30)} | {'Alt-Stat-Name'.center(60)} | {'Type'.center(37)}\n"
        out += "-"*31 + "|" + "-"*62 + "|" + "-" * 38 + "\n"
        overview = jmespath.search("configs[].dynamic_active_clusters[].cluster | []"
                                   ".[name, alt_stat_name, type, cluster_type.name]", config)
        for (name, stat_name, ctype, cltype) in overview:
            stat_name = '' if stat_name is None else stat_name
            merged_type = ctype if ctype is not None else cltype
            out += f"{name:>30.30s} | {stat_name:>60.60s} | {merged_type:<37.37s}\n"
        if len(envoy_pods) > 1:
            out += "\n\n"
    return out


# Public
def get_lua_code(envoy_pods):
    """Fetch the LUA filter and pretty-print it"""
    if type(envoy_pods) != list:
        envoy_pods = [envoy_pods]
    out = ""
    for envoyPod in envoy_pods:
        out += f"Envoy Pod ID: {envoyPod}\n"
        config = get_envoy_config(envoyPod)
        lua = jmespath.search("configs[].dynamic_listeners[].active_state.listener.filter_chains[].filters[]"
                              ".typed_config.http_filters[].typed_config.inline_code", config)
        out += format_lua(lua)
        if len(envoy_pods) > 1:
            out += "\n\n"
    return out


def format_lua(text):
    """ Convert LUA filter into readable format.
    Author: Michelle Klein"""
    text = text[0].splitlines()

    # Add line breaks
    for line in range(0, len(text)):
        text[line] = text[line] + "\n"

    # Replace all ' "' into '"'
    for line in range(0, len(text)):
        for o in range(0, 500):
            text[line] = text[line].replace('" ', '"')

    # Replace all' \\' into '\\'
    for line in range(0, len(text)):
        for o in range(0, 500):
            text[line] = text[line].replace(' \\', '\\')

    # Replace all '\\' into ''
    for line in range(0, len(text)):
        for o in range(0, 500):
            text[line] = text[line].replace('\\', '')

    return ''.join(text)


# ------------------------------------------------------------------------
# Envoy Statistics

# Public
def reset_one_envoy_stats(url):
    """Reset the statistics counters for the given Envoy"""
    r = post_single_request(url, 5)
    return r.text


# Public
def reset_all_envoy_stats(urls):
    """Reset the statistics counters of all given Envoys"""
    if type(urls) != list:
        urls = [urls]
    result = ""
    for url in urls:
        result += reset_one_envoy_stats(url)
    return result


# Public
def get_one_envoy_stats(url):
    """Fetch the statistics from Envoy at the given URL.
    Return a dict with the name of the counter as key and
    the value as the value.
    """
    stats = {}
    r = get_request(url, 5)
    lines = r.split("\n")
    for line in lines:
        words = line.split(": ")
        if len(words) > 1:
            try:
                stats[words[0]] = int(words[1])
            except:  # was not an integer
                stats[words[0]] = words[1]
    return stats


# Public
def get_all_envoy_stats(envoy_pods):
    """Fetch the statistics from all givenEnvoys.
    Return a dict with the name of the counter as key and
    the value as the value.
    """
    # All stats from all Envoys:
    consolidated_stats = {}
    for podname in envoy_pods:
        port = 9901
        if 'port-forwardings' in g.globalDict:
            port = g.globalDict['port-forwardings'][podname]
        url = f"http://localhost:{port}/stats"
        stats = get_one_envoy_stats(url)
        # Add each of the new stats to the consolidated stats
        for name, value in stats.items():
            if name in consolidated_stats:  # Already exists -> add value
                consolidated_stats[name] += value
            else:  # Not seen before -> insert
                consolidated_stats[name] = value
    return consolidated_stats


def format_envoy_stats(stats):
    """Pretty-print Envoy stats"""
    ret = ""
    for name, value in stats.items():
        ret += ("%s: %s\n" % (name, str(value)))
    return ret


def remove_non_integer_stats(stats):
    """ Given a dict with statistics, remove those which have a non-integer
    value """
    return {name: value for name, value in stats.items()
            if isinstance(value, int)}


def remove_zero_stats(stats):
    """Given a dict with statistics, remove those which are zero"""
    return {name: value for name, value in stats.items() if
            ((not isinstance(value, int)) or (value != 0))}


def remove_non_matching_names(stats, name_filter="stream_"):
    """ Given a dict with statistics, only show the counters whose name
    matches the nameFilter supplied to this function."""
    return {name: value for name, value in stats.items() if
            re.search(name_filter, name)}


# --------------------------------------
# Public
def get_hosts_data_for_wireshark():
    """Collect all IP addresses from services and pods and
    create a 'hosts' file that you can use with Wireshark
    to translate addresses to hostnames"""
    out = ""
    pods = g.globalDict["k8s"]["pod"]
    for pod in pods:
        out += f"{pod['pod_ip']} {pod['name']}\n"
    services = g.globalDict["k8s"]["service"]
    for service in services:
        if service['cluster_ip'] != "None":
            out += f"{service['cluster_ip']} {service['name']}\n"
    return out


# Public
def write_wireshark_hosts_file():
    """Overwrite the Wireshark hosts file in ~/.config/wireshark
    with data from the current cluster"""
    hosts = f"{g.globalDict['home']}/.config/wireshark/hosts"
    with open(hosts, 'w') as out:
        out.write(get_hosts_data_for_wireshark())
        return f"Hosts written to {hosts}"

# vim: tabstop=4 expandtab shiftwidth=4 softtabstop=4
